package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiScaffold;
import autismclient.api.ai.AiOperation;
import autismclient.util.BlockBreakSpeedHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

final class AiPrintOp implements AiOperation {
   private final Minecraft mc = Minecraft.getInstance();
   private final autismclient.ai.print.LivePrintEnv env = new autismclient.ai.print.LivePrintEnv();
   private final ClientLevel level = this.mc.level;
   private final LocalPlayer player = this.mc.player;
   private final autismclient.ai.print.PrintCore core;
   private final List<autismclient.ai.print.PrintTarget> targets;
   private final AiBuilder.Blueprint blueprint;
   private final AiBuilder.BuildOptions options;
   private final AiPrintSupply supply;
   private AiOperation child;
   private AiScaffold.JobScope jobScope;
   private BlockPos terrainCell;
   private boolean terrainDone;
   private boolean supportedAccessTried;
   private boolean resetActivityAfterRefill;
   private Item missingItem;
   private ItemStack requestedContents = ItemStack.EMPTY;
   private int missingItemHeld;
   private Item requestedItem;
   private int requestedItemHeld;
   private final Set<Long> skippedSoilCells = new HashSet<>();
   private boolean soilGatherActive;
   private int soilCollectedBanked;
   private int dirtAtGatherStart;
   private int soilBroken;
   private JsonObject externalWorkData;
   private boolean childNeedsReconciliation;
   private boolean machineNeedsReconciliation;
   private final AiGuardLayer guard = new AiGuardLayer(new AiGuard.GuardPolicy(true, true, true, 16.0F, 7.0F, 14), "schematic printer");
   private boolean guardWasActive;
   private final long startedAt = System.nanoTime();
   private final long timeoutNanos;
   private final Consumer<BlockPos> blockObserver;
   private final LongConsumer chunkObserver;
   private final int originalSlot;
   private boolean closeRequested;
   private boolean closed;
   private long tickCount;
   private long maxTickNanos;
   private static final int flush_I = 16384;
   private final AiScaffold.JobScope scaffold;
   private final ArrayDeque<BlockPos> revisitQueue = new ArrayDeque<>();
   private long nextPersistAt;

   void notifyBlocksChanged(Map<BlockPos, BlockState> blocks) {
      blocks.forEach(this.core::notifyBlockChanged);
   }

   void notifyBlockChanged(BlockPos position) {
      this.supply.notifyBlockChanged(position);
   }

   AiPrintOp(AiBuilder.Blueprint blueprint, int timeoutSeconds, AiBuilder.BuildOptions power) {
      this(blueprint, timeoutSeconds, power, AiPrintRules.targets(blueprint));
   }

   AiPrintOp(AiBuilder.Blueprint blueprint, int timeoutSeconds, AiBuilder.BuildOptions power, List<autismclient.ai.print.PrintTarget> preparedTargets) {
      this(
         blueprint,
         timeoutSeconds,
         power,
         preparedTargets,
         power.machine().machineSpec(blueprint.origin(), Minecraft.getInstance().level.registryAccess())
      );
   }

   AiPrintOp(
      AiBuilder.Blueprint blueprint,
      int timeoutSeconds,
      AiBuilder.BuildOptions power,
      List<autismclient.ai.print.PrintTarget> preparedTargets,
      AiSchematicMachinePlan.MachineSpec actions
   ) {
      this(blueprint, timeoutSeconds, power, preparedTargets, actions, null);
   }

   AiPrintOp(
      AiBuilder.Blueprint blueprint,
      int timeoutSeconds,
      AiBuilder.BuildOptions power,
      List<autismclient.ai.print.PrintTarget> preparedTargets,
      AiSchematicMachinePlan.MachineSpec actions,
      autismclient.ai.print.PrintCore.PreparedGeometry geometry
   ) {
      AiPrintSupply.validateOptions(power);
      this.blueprint = blueprint;
      this.options = power;
      this.supply = new AiPrintSupply(blueprint, power.clearTerrain());
      this.terrainDone = !power.clearTerrain();
      this.targets = preparedTargets;
      this.env.setFlightAllowed(power.assumeFly());
      this.core = autismclient.ai.print.PrintCore.create(this.env, AiPrintRules.INSTANCE, this.targets, geometry);
      this.scaffold = AiScaffold.openScope(AiBuilder.checkpointPath(blueprint), List.of());
      this.revisitQueue.addAll(this.scaffold.ownedCells());
      this.core.attachRailVehicles(actions.vehicles());
      this.core.attachItemFrames(actions.frames());
      this.core.attachMachineOutcomes(actions.outcomes());

      for (AiSchematicMachinePlan.ContainerPlan saved : actions.containers()) {
         this.core.attachMachineJob(new AiPrintContainer(this.env, saved));
      }

      for (autismclient.ai.print.PrintSkippedContents unavailable : actions.unavailable()) {
         this.core.attachMachineJob(unavailable);
      }

      for (autismclient.ai.print.PrintClockSpec clock : actions.clocks()) {
         this.core.attachMachineJob(AiPrintClock.create(this.env, clock));
      }

      this.core.trace = Boolean.getBoolean("autism.ai.print.trace");
      this.blockObserver = pos -> {
         this.env.notifyBlockChanged(pos);
         this.supply.noteChange(pos);
      };
      this.chunkObserver = key -> {
         this.env.notifyChunkChanged(key);
         this.supply.notifyChunkChanged(key);
      };
      this.timeoutNanos = Math.max(0, timeoutSeconds) * 1000000000L;
      this.originalSlot = this.mc.player == null ? 0 : this.mc.player.getInventory().getSelectedSlot();
      AiServerEvidence.addBlockObserver(this.blockObserver);
      AiServerEvidence.addChunkObserver(this.chunkObserver);
      AiPrintOp.PrintLog.info("[AI] print v2: {} cells", this.targets.size());
   }

   @Override
   public AiOperation.Result tick() {
      long started = System.nanoTime();

      AiOperation.Result result;
      try {
         result = this.tickBuild();
      } finally {
         if (!this.closed && System.nanoTime() >= this.nextPersistAt) {
            this.scaffold.persist();
            this.nextPersistAt = System.nanoTime() + 1000000000L;
         }

         this.maxTickNanos = Math.max(this.maxTickNanos, System.nanoTime() - started);
      }

      if (result.terminal()) {
         JsonObject data = AiJson.parseObject(result.dataJson());
         data.addProperty("maxOperationTickMs", this.maxTickNanos / 1000000L);
         return new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure());
      } else {
         return result;
      }
   }

   private AiOperation.Result tickBuild() {
      if (this.closeRequested) {
         return this.fail(AiOperation.Status.CANCELLED, "Build cancelled");
      }

      if (this.mc.player == null || this.mc.level == null) {
         return this.fail(AiOperation.Status.CANCELLED, "World unavailable");
      }

      if (this.mc.level != this.level || this.mc.player != this.player) {
         return this.fail(AiOperation.Status.CANCELLED, "World or player changed");
      }

      if (!this.mc.player.isAlive()) {
         return this.fail(AiOperation.Status.FAILED, "The player died");
      }

      if (this.timeoutNanos > 0L && System.nanoTime() - this.startedAt > this.timeoutNanos) {
         return this.fail(AiOperation.Status.TIMED_OUT, "Requested build deadline reached");
      }

      AiMenuClose.Inner_StreamUtil menuClose = AiMenuClose.receiptState();
      if (menuClose != AiMenuClose.Inner_StreamUtil.READY) {
         this.env.releaseControls();
         return menuClose == AiMenuClose.Inner_StreamUtil.UNCERTAIN
            ? this.fail(AiOperation.Status.UNCERTAIN, "The server did not reconcile the closed storage menu. Inspect the inventory before resuming.", AiFailure.UNKNOWN)
            : this.runningStatus("Waiting for the server to close the storage menu");
      }

      if (!this.env.isHandReady()) {
         this.env.releaseControls();
         return this.env.handSwapError() != null
            ? this.fail(this.env.hasPendingHandSwap() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, this.env.handSwapError(), AiFailure.UNKNOWN)
            : this.runningStatus("Waiting for the server to confirm the hand swap");
      }

      this.revisitQueuedCells();

      try {
         this.supply.prepare(16384);
      } catch (IllegalStateException failure) {
         return this.fail(AiOperation.Status.FAILED, failure.getMessage(), AiFailure.WORLD_UNKNOWN);
      }

      if (!this.supply.isPrepared()) {
         this.env.releaseControls();
         return this.runningStatus(this.supply.getStatus());
      }

      if (this.core.isPreparing()) {
         this.tickCount++;
         this.env.advanceTick();
         this.env.applyControls(this.core.tick());
         return this.runningStatus(this.core.getStatus());
      }

      if (this.mc.gui.screen() instanceof AbstractSignEditScreen editor) {
         try {
            if (this.env.consumePlacedSign(AiPrintSupply.signPos(editor))) {
               AiPrintSupply.fillSignText(editor, this.blueprint.origin(), this.options.signText());
               this.mc.gui.setScreen(null);
               this.env.clearRecentClick();
            }
         } catch (ReflectiveOperationException | RuntimeException failure) {
            return this.fail(AiOperation.Status.FAILED, "Could not apply the schematic's sign text", AiFailure.EXECUTION_FAILED);
         }
      }

      autismclient.ai.engine.AiRoutePolicy.blockCellsTemporarily(this.supply.ownedCells()::contains);
      if (!this.env.isCreativeMode()) {
         AiOperation.Result guarded = this.guard
            .tick(
               this,
               this::prepareForGuard,
               () -> this.timeoutNanos == 0L ? 120 : (int)Math.max(1L, Math.min(2147483647L, (this.timeoutNanos - (System.nanoTime() - this.startedAt)) / 1000000000L))
            );
         if (guarded != null) {
            this.guardWasActive = true;
            if (guarded.terminal()) {
               return this.fail(guarded.status(), guarded.message(), guarded.failure());
            }

            return this.runningStatus("Survival: " + guarded.message());
         }
      } else if (this.guard.isActive()) {
         boolean pending = this.guard.requiresReconciliation();
         this.guard.cancel();
         if (pending) {
            return this.fail(AiOperation.Status.UNCERTAIN, "Game mode changed during an unconfirmed survival action", AiFailure.UNKNOWN);
         }
      }

      if (this.guardWasActive) {
         this.guardWasActive = false;
         this.closeJobScope();
         this.core.resetActivity();
      }

      if (this.childNeedsReconciliation || this.machineNeedsReconciliation) {
         return this.fail(AiOperation.Status.UNCERTAIN, "Build maintenance was interrupted before recovery was confirmed", AiFailure.UNKNOWN);
      }

      if (this.child != null
         && this.missingItem != null
         && this.env.isCreativeMode()
         && this.child.canInterruptNow()
         && !this.child.requiresReconciliationAfterEmergency()) {
         this.child.cancel();
         this.child = null;
         if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
            AiMenuClose.closeMenu();
         }

         this.closeJobScope();
         this.supply.resetRefill();
         this.missingItem = null;
         this.core.resetActivity();
         return this.runningStatus("Creative inventory is available; resuming construction");
      } else {
         if (this.child != null) {
            return this.tickChild();
         }

         if (!this.core.hasPendingMachineJob()
            && this.mc.gui.screen() instanceof AbstractContainerScreen<?> opened
            && !(opened instanceof InventoryScreen)
            && this.mc.player.containerMenu != this.mc.player.inventoryMenu
            && this.env.hasRecentClick()) {
            AiPrintOp.PrintLog.info("[AI] print v2: closing {} the build opened", opened.getClass().getSimpleName());
            AiMenuClose.closeMenu();
            this.env.clearRecentClick();
            this.env.releaseControls();
            return this.runningStatus("Waiting for the server to close the storage menu");
         }

         if (this.mc.gui.screen() != null && !this.core.hasPendingMachineJob()) {
            this.env.releaseControls();
            return this.runningStatus("Paused while a screen is open");
         }

         if (this.env.isFlying() && !this.env.canUseFlight() && !this.core.hasPendingMachineJob()) {
            this.env.releaseControls();
            return this.runningStatus("Paused: land or disable native flight before continuing ground construction");
         }

         if (!this.env.isCreativeMode()
            && !this.core.hasUncertainMachineJob()
            && !this.core.trapdoorRecoveryInteracted()
            && this.mc.player.getFoodData().getFoodLevel() <= 14
            && !this.core.isRestingOnGround()) {
            AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
            if (vitals.hasFood() && vitals.hostilesNear() == 0 && AiSurvival.shouldEat(vitals.food())) {
               return this.runningTick("Settling before a meal");
            }
         }

         if (!this.terrainDone) {
            try {
               if (this.terrainCell == null) {
                  this.terrainCell = this.supply.nextTerrainCell();
               }
            } catch (IllegalStateException failure) {
               return this.fail(AiOperation.Status.FAILED, failure.getMessage(), AiFailure.UNREACHABLE);
            }

            if (this.terrainCell != null) {
               if (!this.core.isRestingOnGround()) {
                  return this.runningTick("Settling before clearing the site");
               }

               this.supportedAccessTried = false;
               this.startChild(autismclient.ai.engine.AiBreakBlock.breakBlock(this.terrainCell, 30, false, this.reservedCells(), false));
               return this.runningStatus("Clearing terrain from the schematic's air at " + this.terrainCell.toShortString());
            }

            if (!this.supply.isTerrainCleared()) {
               return this.runningStatus("Checking remaining terrain");
            }

            this.terrainDone = true;
            this.core.resetActivity();
            this.resetActivityAfterRefill = false;
         }

         if (this.resetActivityAfterRefill) {
            this.core.resetActivity();
            this.resetActivityAfterRefill = false;
         }

         if (this.requestedItem != null) {
            return this.recoverRequestedItem();
         }

         if (!this.requestedContents.isEmpty()) {
            return this.fetchShulkerContents();
         }

         if (this.missingItem != null) {
            return this.refillMissingItem();
         }

         this.tickCount++;
         this.env.advanceTick();
         autismclient.ai.print.PrintControls controls = this.core.tick();
         ItemStack requestedContents = this.core.getRequestedContents();
         if (!requestedContents.isEmpty()) {
            this.requestedContents = requestedContents;
            this.env.applyControls(controls);
            return this.runningStatus("Preparing carried stock for saved container contents");
         }

         if (this.core.getMissingHandItem() == Items.AIR && this.core.isRestingOnGround()) {
            return this.fail(AiOperation.Status.FAILED, "Free one inventory or offhand slot so the builder can use an empty hand", AiFailure.INVENTORY_FULL);
         }

         if (!this.env.isCreativeMode() && this.core.getRequestedItem() != null) {
            this.requestedItem = this.core.getRequestedItem();
            this.requestedItemHeld = AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.requestedItem);
            this.env.applyControls(controls);
            return this.runningStatus("Preparing to recover " + itemId(this.requestedItem));
         }

         if (!this.env.isCreativeMode() && this.core.hasItem(this.core.getMissingHandItem())) {
            this.env.applyControls(controls);
            return this.runningStatus("Recovering temporary " + itemId(this.core.getMissingHandItem()));
         }

         if (this.env.isCreativeMode() || !this.supply.canSupply(this.core.getMissingHandItem())) {
            this.env.applyControls(controls);
            if (this.tickCount % 4L == 0L) {
               AiVisualization.showPlan(this, this.blueprint.origin(), this.blueprint.cells(), this.core.placedCount(), false, true);
            }

            AiOperation.Result var10000;
            switch (this.core.getPhase()) {
               case DONE:
                  if (this.revisitQueue.isEmpty()) {
                     var10000 = this.fail(
                        AiOperation.Status.SUCCEEDED,
                        "Built "
                           + this.core.placedCount()
                           + " of "
                           + this.core.cellCount()
                           + " cells"
                           + (this.core.containersSkipped() == 0 ? "" : ". Skipped unavailable contents in " + this.core.containersSkipped() + " containers")
                           + (this.core.railVehiclesSkipped() == 0 ? "" : ". Off-rail minecarts still require assembly: " + this.core.railVehiclesSkipped())
                           + (this.core.railShapeDifferences() == 0 ? "" : ". Saved rail shapes still differ: " + this.core.railShapeDifferences())
                     );
                  } else {
                     this.core.revisitCell(this.revisitQueue.peekFirst());
                     var10000 = this.runningStatus("Loading saved construction blocks before final cleanup");
                  }
                  break;
               case FAILED:
                  var10000 = this.fail(
                     this.core.hasUncertainMachineWork() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
                     "Build stopped: " + this.core.getStopReason()
                  );
                  break;
               default:
                  var10000 = this.runningStatus(this.core.getStatus());
            }

            return var10000;
         } else {
            if (!this.options.shulkerStock()) {
               return this.fail(AiOperation.Status.FAILED, "Missing material: " + itemId(this.supply.getMissingItem()), AiFailure.MISSING_MATERIALS);
            }

            this.missingItem = this.supply.getMissingItem();
            this.env.applyControls(controls);
            return this.runningStatus("Preparing to fetch " + itemId(this.missingItem));
         }
      }
   }

   private void revisitQueuedCells() {
      int count = Math.min(128, this.revisitQueue.size());

      for (int checked = 0; checked < count; checked++) {
         BlockPos position = this.revisitQueue.removeFirst();
         if (!this.env.hasChunkAt(position)) {
            this.revisitQueue.addLast(position);
         } else {
            BlockState state = this.env.getBlockState(position);
            if (AiScaffold.matches(this.scaffold.recorded(position), state.getBlock())) {
               this.core.notifyBlockChanged(position, state);
            } else {
               AiScaffold.forget(position);
            }
         }
      }
   }

   private AiOperation.Result runningStatus(String message) {
      String phase;
      if (!this.supply.isPrepared() || this.core.isPreparing()) {
         phase = "Preparing site";
      } else if (!this.terrainDone) {
         phase = "Clearing terrain";
      } else if (this.missingItem != null || !this.requestedContents.isEmpty()) {
         phase = "Restocking";
      } else if (this.requestedItem != null) {
         phase = "Recovering drops";
      } else {
         String var10000;
         switch (this.core.getPhase()) {
            case FLUIDS:
               var10000 = "Placing fluids";
               break;
            case TEARDOWN:
               var10000 = "Removing supports";
               break;
            case VERIFY:
               var10000 = "Verifying build";
               break;
            default:
               var10000 = AiTaskStatus.phaseLabel(message, "Placing blocks");
         }

         phase = var10000;
      }

      AiVisualization.showCount(this, "Building schematic", phase, this.core.placedCount(), this.core.cellCount());
      return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressData().toString(), List.of());
   }

   private AiOperation.Result runningTick(String message) {
      this.tickCount++;
      this.env.advanceTick();
      this.resetActivityAfterRefill = true;
      autismclient.ai.print.PrintControls controls = this.core.refillHereControls();
      if (controls == null) {
         return this.fail(AiOperation.Status.FAILED, "No safe landing before the next build maintenance action", AiFailure.UNREACHABLE);
      }

      this.env.applyControls(controls);
      return this.runningStatus(message);
   }

   private AiOperation.Result refillMissingItem() {
      if ((this.env.isCreativeMode() || AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.missingItem) > 0) && this.core.isRestingOnGround()) {
         this.supply.resetRefill();
         this.missingItem = null;
         this.core.resetActivity();
         return this.runningStatus("Material is available; resuming construction");
      }

      this.tickCount++;
      this.env.advanceTick();
      boolean portableHere = this.core.isRestingOnGround()
         && this.supply.hasPortableStock()
         && AiCarriedShulker.init_BlockPos_Minecraft_Set(this.mc, this.reservedCells()) != null;
      autismclient.ai.print.PrintControls controls = portableHere ? this.core.refillHereControls() : this.core.stockRouteControls();
      if (controls == null) {
         return this.fail(AiOperation.Status.FAILED, "No safe route from the work station to shulker stock", AiFailure.UNREACHABLE);
      }

      this.env.applyControls(controls);
      if (!portableHere && !this.core.isAtStockStance()) {
         return this.runningStatus("Returning to ground for " + itemId(this.missingItem));
      }

      if (!this.env.isCreativeMode() && AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.missingItem) <= 0) {
         this.missingItemHeld = AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.missingItem);
         if (!this.supply.advanceStockScan(16384)) {
            return this.runningStatus("Checking shulker stock for " + itemId(this.missingItem));
         }

         AiOperation refill = this.supply.stockRefillOperation(this.core.constructionCells());
         if (refill == null && this.missingItem == Items.DIRT) {
            return this.startSoilGather();
         }

         if (refill == null) {
            return this.fail(
               AiOperation.Status.FAILED,
               "No usable carried or nearby shulker could supply "
                  + itemId(this.missingItem)
                  + ". Keep an inventory slot free for the withdrawn material and leave room to recover the box.",
               AiFailure.MISSING_MATERIALS
            );
         }

         this.startChild(refill);
         return this.runningStatus("Fetching " + itemId(this.missingItem) + " from shulker stock");
      } else {
         this.supply.resetRefill();
         this.missingItem = null;
         this.core.resetActivity();
         return this.runningStatus("Material is available; resuming construction");
      }
   }

   private AiOperation.Result startSoilGather() {
      this.soilGatherActive = true;
      this.dirtAtGatherStart = AiPrintSupply.countInInventory(this.mc.player.getInventory(), Items.DIRT);
      this.startChild(new AiPrintOp.LocalSoilGather(Math.max(this.missingItemHeld, this.supply.neededStock())));
      return this.runningStatus("Gathering soil nearby");
   }

   private int soilCollected() {
      return this.soilCollectedBanked + (this.soilGatherActive ? Math.max(0, AiPrintSupply.countInInventory(this.mc.player.getInventory(), Items.DIRT) - this.dirtAtGatherStart) : 0);
   }

   private void bankSoilCollected() {
      this.soilCollectedBanked = this.soilCollected();
      this.soilGatherActive = false;
   }

   private AiOperation.Result recoverRequestedItem() {
      int held = AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.requestedItem);
      if (held > this.requestedItemHeld) {
         this.core.recordSupplied(this.requestedItem, held - this.requestedItemHeld);
         this.requestedItem = null;
         this.core.resetActivity();
         return this.runningStatus("Recovered temporary material; resuming construction");
      } else {
         this.tickCount++;
         this.env.advanceTick();
         autismclient.ai.print.PrintControls controls = this.core.refillHereControls();
         if (controls == null) {
            return this.fail(AiOperation.Status.FAILED, "No safe landing before recovering temporary material", AiFailure.UNREACHABLE);
         } else {
            this.env.applyControls(controls);
            if (!this.core.isRestingOnGround()) {
               return this.runningStatus("Settling before recovering " + itemId(this.requestedItem));
            } else {
               int remaining = this.core.remainingNeed(this.requestedItem);
               if (remaining <= 0) {
                  this.core.recordSupplied(this.requestedItem, 0);
                  this.requestedItem = null;
                  this.core.resetActivity();
                  return this.runningStatus("Temporary material reconciled; resuming construction");
               } else {
                  this.startChild(
                     AiGameplay.pickupItems(
                        AiJson.objectOf(
                           "item", itemId(this.requestedItem), "count", Math.min(remaining, 2304), "additional", true, "radius", 16, "timeoutSeconds", 20
                        )
                     )
                  );
                  return this.runningStatus("Collecting dropped " + itemId(this.requestedItem));
               }
            }
         }
      }
   }

   private AiOperation.Result fetchShulkerContents() {
      if (!this.options.shulkerStock()) {
         return this.finishContentsRequest(false, "Carried shulker stock is disabled");
      }

      this.tickCount++;
      this.env.advanceTick();
      autismclient.ai.print.PrintControls controls = this.core.stockRouteControls();
      if (controls == null) {
         return this.finishContentsRequest(false, "No safe stock route for the optional saved contents");
      }

      this.env.applyControls(controls);
      if (!this.core.isAtStockStance()) {
         return this.runningStatus("Returning to a safe stock workstation for saved contents");
      }

      AiOperation refill = this.supply.refillOperation(this.requestedContents, this.requestedContents.getCount(), this.core.constructionCells());
      if (refill == null) {
         return this.finishContentsRequest(false, "No carried box can supply the exact saved item");
      }

      this.startChild(refill);
      return this.runningStatus("Fetching exact saved contents from carried shulker stock");
   }

   private AiOperation.Result finishContentsRequest(boolean available, String message) {
      if (available) {
         this.supply.countRefill();
      }

      this.core.resolveContentsRequest(available);
      this.supply.clearContentsRequest();
      this.requestedContents = ItemStack.EMPTY;
      this.core.resetActivity();
      this.resetActivityAfterRefill = false;
      return this.runningStatus(message);
   }

   private AiOperation.Result fail(AiOperation.Status status, String message) {
      return this.fail(status, message, status == AiOperation.Status.CANCELLED ? AiFailure.CANCELLED : AiFailure.UNKNOWN);
   }

   private AiOperation.Result fail(AiOperation.Status status, String message, AiFailure failure) {
      this.shutdown();
      if (status != AiOperation.Status.SUCCEEDED && (this.core.hasUncertainMachineWork() || this.childNeedsReconciliation || this.machineNeedsReconciliation)) {
         status = AiOperation.Status.UNCERTAIN;
      }

      JsonObject data = this.progressData();
      AiPrintOp.PrintLog.info("[AI] print v2: {} - {} ({})", status, message, data);
      return status == AiOperation.Status.SUCCEEDED
         ? new AiOperation.Result(status, message, data.toString(), List.of())
         : new AiOperation.Result(status, message, data.toString(), List.of(), failure);
   }

   private Set<Long> reservedCells() {
      return unionView(this.core.constructionCells(), this.supply.ownedCells());
   }

   static Set<Long> unionView(final Set<Long> first, final Set<Long> second) {
      return Collections.unmodifiableSet(new AbstractSet<Long>() {
         @Override
         public boolean contains(Object key) {
            return first.contains(key) || second.contains(key);
         }

         @Override
         public boolean isEmpty() {
            return first.isEmpty() && second.isEmpty();
         }

         @Override
         public int size() {
            int count = first.size();

            for (long key : second) {
               if (!first.contains(key)) {
                  count++;
               }
            }

            return count;
         }

         @Override
         public Iterator<Long> iterator() {
            return Stream.concat(first.stream(), second.stream().filter(key -> !first.contains(key))).iterator();
         }
      });
   }

   private static String itemId(Item item) {
      return BuiltInRegistries.ITEM.getKey(item).toString();
   }

   private void startChild(AiOperation operation) {
      this.env.releaseControls();
      this.jobScope = AiScaffold.openJobScope();
      this.child = operation;
   }

   private void closeJobScope() {
      if (this.jobScope != null) {
         if (this.mc.level == this.level) {
            for (BlockPos p : this.jobScope.ownedCells()) {
               BlockState state = this.mc.level.getBlockState(p);
               if (AiScaffold.matches(this.jobScope.recorded(p), state.getBlock())) {
                  this.core.notifyBlockChanged(p, state);
               }
            }
         }

         this.jobScope.close();
         this.jobScope = null;
      }
   }

   private AiOperation.Result tickChild() {
      autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(3000000000L);
      autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousBridgeCompat(3000000000L);
      AiOperation.Result result = this.child.tick();
      this.externalWorkData = AiJson.parseObject(result.dataJson());
      if (!result.terminal()) {
         return this.runningStatus(
            (
                  this.terrainCell != null
                     ? "Clearing terrain: "
                     : (this.soilGatherActive ? "Local soil: " : (this.requestedItem != null ? "Drop recovery: " : "Shulker refill: "))
               )
               + result.message()
         );
      }

      this.externalWorkData.addProperty("status", result.status().name());
      this.externalWorkData.addProperty("message", result.message());
      this.cancelChild();
      this.closeJobScope();
      if (this.childNeedsReconciliation) {
         return this.fail(AiOperation.Status.UNCERTAIN, "External work needs reconciliation: " + result.message(), result.failure());
      }

      if (result.status() != AiOperation.Status.UNCERTAIN && result.status() != AiOperation.Status.CANCELLED && result.status() != AiOperation.Status.TIMED_OUT
         )
       {
         if (this.mc.player.containerMenu != this.mc.player.inventoryMenu) {
            AiMenuClose.closeMenu();
         }

         if (this.terrainCell != null) {
            if (!this.supportedAccessTried && result.status() == AiOperation.Status.FAILED && result.failure() == AiFailure.UNREACHABLE) {
               this.supportedAccessTried = true;
               this.startChild(autismclient.ai.engine.AiBreakBlock.breakBlock(this.terrainCell, 30, false, this.reservedCells(), true));
               return this.runningStatus("Finding supported access to terrain at " + this.terrainCell.toShortString());
            } else {
               BlockPos p = this.terrainCell;
               this.terrainCell = null;
               if (result.status() == AiOperation.Status.SUCCEEDED && !AiSchematicActions.isNaturalTerrain(this.mc.level.getBlockState(p))) {
                  this.supply.terrainCleared++;
                  return this.runningStatus("Terrain removed; checking the next obstruction");
               } else {
                  return this.fail(AiOperation.Status.FAILED, "Could not clear terrain at " + p.toShortString() + ": " + result.message(), result.failure());
               }
            }
         } else if (this.requestedItem != null) {
            int gained = Math.max(0, AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.requestedItem) - this.requestedItemHeld);
            this.core.recordSupplied(this.requestedItem, gained);
            if (result.status() == AiOperation.Status.SUCCEEDED) {
               this.requestedItem = null;
               this.core.resetActivity();
               this.resetActivityAfterRefill = false;
               return this.runningStatus("Temporary drops recovered; resuming construction");
            } else {
               this.core.giveUpItem(this.requestedItem);
               this.requestedItem = null;
               this.core.resetActivity();
               this.resetActivityAfterRefill = false;
               return this.runningStatus("Drop recovery was inconclusive; checking ordinary stock");
            }
         } else if (this.requestedContents.isEmpty()) {
            this.core.resetActivity();
            this.resetActivityAfterRefill = false;
            if (AiPrintSupply.countInInventory(this.mc.player.getInventory(), this.missingItem) > this.missingItemHeld) {
               this.supply.completeRefill();
               this.missingItem = null;
               return this.runningStatus("Shulker stock received; resuming construction");
            } else if (this.env.isCreativeMode()) {
               this.supply.resetRefill();
               this.missingItem = null;
               return this.runningStatus("Creative inventory is available; resuming construction");
            } else {
               return this.runningStatus("Checking the next shulker stock source");
            }
         } else {
            return result.status() == AiOperation.Status.SUCCEEDED
               ? this.finishContentsRequest(true, "Recovered exact stock; checking the saved container again")
               : this.runningStatus("Checking the next carried shulker for optional saved contents");
         }
      } else {
         return this.fail(result.status(), "External work needs reconciliation: " + result.message(), result.failure());
      }
   }

   private void prepareForGuard() {
      this.env.releaseControls();
      if (this.core.hasUncertainMachineJob()) {
         this.core.cancelMachineJob();
         this.machineNeedsReconciliation = true;
      }

      this.cancelChild();
      this.closeJobScope();
      this.core.resetActivity();
      this.resetActivityAfterRefill = false;
      this.jobScope = AiScaffold.openJobScope();
   }

   private void cancelChild() {
      this.bankSoilCollected();
      if (this.child != null) {
         this.childNeedsReconciliation = this.childNeedsReconciliation | this.child.requiresReconciliationAfterEmergency();
         this.child.cancel();
         this.childNeedsReconciliation = this.childNeedsReconciliation | this.child.requiresReconciliationAfterEmergency();
         if (this.childNeedsReconciliation) {
            if (this.externalWorkData == null) {
               this.externalWorkData = new JsonObject();
            }

            this.externalWorkData.addProperty("recoveryRequired", true);
         }

         this.child = null;
      }
   }

   private JsonObject progressData() {
      JsonObject o = new JsonObject();
      o.addProperty("engine", "print-v2");
      o.addProperty("phase", this.core.getPhase().name());
      o.addProperty(
         "stage",
         !this.supply.isPrepared() || this.core.isPreparing()
            ? "prepare"
            : (
               !this.terrainDone
                  ? "clear"
                  : (
                     this.requestedItem != null
                        ? "recovery"
                        : (
                           this.missingItem == null && this.requestedContents.isEmpty()
                              ? (
                                 this.core.getPhase() == autismclient.ai.print.PrintCore.PrintPhase.TEARDOWN
                                    ? "drain"
                                    : this.core.getPhase().name().toLowerCase(Locale.ROOT)
                              )
                              : "stock"
                        )
                  )
            )
      );
      o.addProperty("temporaryClickBlocksRemoved", this.core.temporaryBlocksRemoved);
      o.addProperty("climbSteps", this.core.climbSteps);
      o.addProperty("supportsDrained", this.core.temporaryBlocksRemoved);
      o.addProperty("originalSupportsDrained", this.core.originalSupportsDrained());
      o.addProperty("originalCleanupRise", this.core.originalCleanupRise());
      o.addProperty("cleanupBlocksRecovered", this.core.cleanupBlocksRecovered());
      o.addProperty("cells", this.core.cellCount());
      o.addProperty("placed", this.core.placedCount());
      o.addProperty("railVehiclesVerified", this.core.railVehiclesVerified());
      o.addProperty("railSlopesVerified", this.core.railSlopesVerified());
      o.addProperty("railShapeDifferences", this.core.railShapeDifferences());
      o.addProperty("movingPlacements", this.core.movingPlacements);
      o.addProperty("headFacingProofReuses", this.core.headFacingProofReuses);
      o.addProperty("hoppersFilled", this.core.hoppersFilled());
      o.addProperty("containersFilled", this.core.containersFilled());
      o.addProperty("itemFramesVerified", this.core.itemFramesVerified());
      o.addProperty("clockPhasesVerified", this.core.clockPhasesVerified());
      o.addProperty("clockAwaitingCycle", this.core.isClockAwaitingCycle());
      o.addProperty("clocksVerified", this.core.clocksVerified());
      o.addProperty("clockItemsVerified", this.core.clockItemsVerified());
      String recovery = this.closed ? this.core.machineJobDetail() : null;
      if (recovery != null) {
         o.add("machineRecovery", JsonParser.parseString(recovery));
      }

      o.addProperty("containersSkipped", this.core.containersSkipped());
      o.addProperty("railVehiclesSkipped", this.core.railVehiclesSkipped());
      o.addProperty("savedSlotsSkipped", this.core.savedSlotsSkipped());
      o.addProperty("savedSlotsVerified", this.core.savedSlotsVerified());
      o.addProperty("machineOutcomesUnverified", this.core.machineOutcomesUnverified());
      o.addProperty(
         "machineComplete",
         this.core.getPhase() == autismclient.ai.print.PrintCore.PrintPhase.DONE
            && this.core.containersSkipped() == 0
            && this.core.railVehiclesSkipped() == 0
            && this.core.machineOutcomesUnverified() == 0
            && this.core.railShapeDifferences() == 0
      );
      JsonArray omissions = new JsonArray();
      this.core.omissions().forEach(omissions::add);
      o.add("skippedContents", omissions);
      int open = this.core.openCells();
      o.addProperty("open", open);
      o.addProperty("pending", open - this.core.failedTotal);
      o.addProperty("failedTotal", this.core.failedTotal);
      if (Boolean.getBoolean("autism.ai.schematics")) {
         o.addProperty("pendingPlanning", this.core.pendingPlanning());
      }

      o.addProperty("scaffolds", this.core.scaffoldsPlaced);
      o.addProperty("scaffoldingRemaining", this.core.scaffoldingRemaining());
      o.addProperty("clicks", this.core.clicks);
      o.addProperty("decisions", this.core.decisions);
      o.addProperty("planMs", this.core.planNanos / 1000000L);
      o.addProperty("totalCoreMs", this.core.totalCoreNanos / 1000000L);
      o.addProperty("maxDecisionMs", this.core.maxDecisionNanos / 1000000L);
      o.addProperty("maxOperationTickMs", this.maxTickNanos / 1000000L);
      o.addProperty("initializationMs", this.core.initializationNanos / 1000000L);
      if (!this.supply.isPrepared() || this.core.isPreparing()) {
         o.addProperty("preparationDone", this.supply.preparationDone() + this.core.preparationDone());
         o.addProperty("preparationTotal", this.supply.preparationTotal() + this.core.preparationTotal());
         o.addProperty("supplyPreparationDone", this.supply.preparationDone());
         o.addProperty("corePreparationDone", this.core.corePreparationDone());
      }

      o.addProperty("ticks", this.tickCount);
      o.addProperty("shulkerRefills", this.supply.shulkerRefills);
      o.addProperty("terrainCleared", this.supply.terrainCleared);
      o.addProperty("soilCollected", this.soilCollected());
      o.addProperty("soilBroken", this.soilBroken);
      o.addProperty("interactionAwaitingAck", this.env.interactionReceiptState() == autismclient.ai.print.PrintEnv.ReceiptState.PENDING);
      o.addProperty(
         "recoveryRequired",
         AiMenuClose.load()
            || this.childNeedsReconciliation
            || this.machineNeedsReconciliation
            || this.core.hasUncertainMachineWork()
            || this.env.hasPendingHandSwap()
            || this.env.interactionReceiptState() == autismclient.ai.print.PrintEnv.ReceiptState.UNCERTAIN
      );
      if (this.externalWorkData != null) {
         o.add("externalWork", this.externalWorkData.deepCopy());
      }

      if (this.core.getStopReason() != null) {
         o.addProperty("why", this.core.getStopReason());
      }

      JsonArray materials = new JsonArray();
      if (!this.core.isPreparing()) {
         for (Item item : this.core.neededMaterials(24)) {
            materials.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }
      }

      o.add("nextMaterials", materials);
      return o;
   }

   @Override
   public boolean canInterruptNow() {
      return !AiMenuClose.load()
         && !this.childNeedsReconciliation
         && !this.machineNeedsReconciliation
         && !this.env.hasPendingHandSwap()
         && !this.core.hasUncertainMachineJob()
         && this.guard.canInterruptNow()
         && (
            this.child != null
               ? this.child.canInterruptNow()
               : !this.core.isAwaitingInteractionReceipt() && this.mc.player != null && this.mc.player.onGround()
         );
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.guard.requiresReconciliation()
         || AiMenuClose.load()
         || this.childNeedsReconciliation
         || this.machineNeedsReconciliation
         || this.env.hasPendingHandSwap()
         || this.child != null && this.child.requiresReconciliationAfterEmergency()
         || this.core.hasUncertainMachineJob()
         || this.child == null && this.core.isAwaitingInteractionReceipt();
   }

   @Override
   public void cancel() {
      this.closeRequested = true;
      this.shutdown();
   }

   private void shutdown() {
      if (!this.closed) {
         this.closed = true;
         this.core.cancelMachineJob();
         this.cancelChild();
         this.machineNeedsReconciliation = this.machineNeedsReconciliation | this.guard.requiresReconciliation();
         this.guard.cancel();
         this.machineNeedsReconciliation = this.machineNeedsReconciliation | this.guard.requiresReconciliation();
         this.closeJobScope();
         autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
         autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousBridgeCompat(0L);
         autismclient.ai.engine.AiRoutePolicy.clearBlockedCells();
         AiServerEvidence.removeBlockObserver(this.blockObserver);
         AiServerEvidence.removeChunkObserver(this.chunkObserver);
         this.env.releaseControls();
         this.scaffold.close();
         if (this.mc.player == this.player
            && this.mc.player != null
            && this.mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
            autismclient.util.InventorySlotHelper.selectHotbarSlot(this.mc, this.originalSlot);
         }
      }
   }

   private static final class PrintLog {
      static void info(String format, Object... args) {
         AutismClientAddon.LOG.info(format, args);
      }
   }

   private final class LocalSoilGather implements AiOperation {
      private final List<BlockPos> candidates = new ArrayList<>();
      private final long deadline = System.nanoTime() + 600000000000L;
      private final AiPreparationReceipt receipt = new AiPreparationReceipt();
      private int cursor;
      private int failures;
      private int dirtBefore;
      private int waitTicks;
      private AiOperation breakOp;
      private BlockPos target;
      private boolean finished;
      private final int targetCount;

      LocalSoilGather(int target) {
         BlockPos center = AiPrintOp.this.mc.player.blockPosition();

         for (BlockPos pos : BlockPos.betweenClosed(center.offset(-32, -4, -32), center.offset(32, 4, 32))) {
            if (AiTerrain.isLoaded(AiPrintOp.this.mc.level, pos) && !AiPrintOp.this.reservedCells().contains(pos.asLong()) && !AiPrintOp.this.skippedSoilCells.contains(pos.asLong())) {
               BlockState state = AiPrintOp.this.mc.level.getBlockState(pos);
               if ((
                     state.is(Blocks.DIRT)
                        || state.is(Blocks.GRASS_BLOCK)
                        || state.is(Blocks.COARSE_DIRT)
                        || state.is(Blocks.ROOTED_DIRT)
                        || state.is(Blocks.MYCELIUM)
                        || state.is(Blocks.PODZOL)
                  )
                  && state.getFluidState().isEmpty()) {
                  this.candidates.add(pos.immutable());
               }
            }
         }

         this.candidates.sort(Comparator.comparingDouble(posx -> posx.distToCenterSqr(center.getX(), center.getY(), center.getZ())));
         this.targetCount = target;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.finished) {
            return this.finishWith(new AiOperation.Result(AiOperation.Status.CANCELLED, "Local soil gathering cancelled", "{}", List.of(), AiFailure.CANCELLED));
         }

         if (AiPrintOp.this.mc.player == null || AiPrintOp.this.mc.level == null || System.nanoTime() > this.deadline) {
            return this.finishWith(new AiOperation.Result(AiOperation.Status.TIMED_OUT, "Local soil gathering timed out", "{}", List.of(), AiFailure.TIMEOUT));
         }

         if (AiPrintSupply.countInInventory(AiPrintOp.this.mc.player.getInventory(), Items.DIRT) >= this.targetCount && !this.receipt.requiresReconciliation(this.breakOp)) {
            return this.finishWith(new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Collected the required soil", "{}", List.of()));
         }

         if (this.breakOp == null) {
            if (this.cursor >= this.candidates.size()) {
               return this.finishWith(new AiOperation.Result(AiOperation.Status.FAILED, "No reachable local soil remains", "{}", List.of(), AiFailure.NO_REACHABLE_TARGET));
            }

            if (!autismclient.ai.engine.AiBlockMine.canStore(AiPrintOp.this.mc.player.getInventory(), new ItemStack(Items.DIRT))) {
               return this.finishWith(new AiOperation.Result(AiOperation.Status.FAILED, "Make inventory room for local soil", "{}", List.of(), AiFailure.INVENTORY_FULL));
            }

            this.target = this.candidates.get(this.cursor++);
            this.dirtBefore = AiPrintSupply.countInInventory(AiPrintOp.this.mc.player.getInventory(), Items.DIRT);
            this.waitTicks = 0;
            this.breakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(
               this.target,
               30,
               false,
               AiPrintOp.this.reservedCells(),
               false,
               Set.of(),
               (state, stack) -> !state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.MYCELIUM) && !state.is(Blocks.PODZOL)
                  || !BlockBreakSpeedHelper.hasSilkTouch(AiPrintOp.this.mc, stack)
            );
         }

         AiOperation.Result result = this.receipt.tickChild(this.breakOp);
         if (!result.terminal()) {
            return this.running(result.message());
         }

         AiOperation completed = this.breakOp;
         this.breakOp = null;
         this.receipt.cancelChild(completed);
         if (result.status() != AiOperation.Status.SUCCEEDED) {
            AiPrintOp.this.skippedSoilCells.add(this.target.asLong());
            if (AiPrintOp.this.core.trace) {
               AiPrintOp.PrintLog.info("[AI] print v2: local soil at {} skipped: {}", this.target.toShortString(), result.message());
            }

            return result.status() == AiOperation.Status.FAILED && !this.receipt.requiresReconciliation(completed) && ++this.failures < 24
               ? this.running("Skipped an unreachable soil block; trying another")
               : this.finishWith(result);
         } else {
            AiPrintOp.this.soilBroken++;
            int held = AiPrintSupply.countInInventory(AiPrintOp.this.mc.player.getInventory(), Items.DIRT);
            if (held > this.dirtBefore) {
               return this.running("Soil picked up; approaching the next block");
            } else {
               return ++this.waitTicks >= 12 ? this.running("Approaching the next soil block") : this.running("Waiting briefly for the nearby soil pickup");
            }
         }
      }

      private AiOperation.Result running(String message) {
         int held = AiPrintSupply.countInInventory(AiPrintOp.this.mc.player.getInventory(), Items.DIRT);
         return new AiOperation.Result(AiOperation.Status.RUNNING, "(" + held + "/" + this.targetCount + ") " + message, "{}", List.of());
      }

      private AiOperation.Result finishWith(AiOperation.Result result) {
         result = this.receipt.finalizeResult(result, this.requiresReconciliationAfterEmergency());
         this.finished = true;
         return result;
      }

      @Override
      public void cancel() {
         this.finished = true;
         this.receipt.cancelChild(this.breakOp);
      }

      @Override
      public boolean canInterruptNow() {
         return !this.receipt.requiresReconciliation(this.breakOp) && (this.breakOp == null || this.breakOp.canInterruptNow());
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.receipt.requiresReconciliation(this.breakOp);
      }
   }
}
