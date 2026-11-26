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
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final autismclient.ai.print.LivePrintEnv init_DataUtil = new autismclient.ai.print.LivePrintEnv();
   private final ClientLevel init_ClientLevel = this.init_Minecraft.level;
   private final LocalPlayer init_LocalPlayer = this.init_Minecraft.player;
   private final autismclient.ai.print.PrintCore init_UiHelper;
   private final List<autismclient.ai.print.PrintTarget> init_List;
   private final AiBuilder.Blueprint init_TableIndex_DataUti;
   private final AiBuilder.BuildOptions init_TableIndex_ConfigS;
   private final AiPrintSupply init_bj;
   private AiOperation init_AiOperation;
   private AiScaffold.Inner_StreamUtil init_a_StreamUtil;
   private BlockPos init_BlockPos;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private Item init_Item;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private int init_I;
   private Item load_Item;
   private int load_I;
   private final Set<Long> init_Set = new HashSet<>();
   private boolean write_Z;
   private int read_I;
   private int write_I;
   private int update_I;
   private JsonObject init_JsonObject;
   private boolean update_Z;
   private boolean sync_Z;
   private final AiGuardLayer init_Y = new AiGuardLayer(new AiGuard.GuardPolicy(true, true, true, 16.0F, 7.0F, 14), "schematic printer");
   private boolean flush_Z;
   private final long init_J = System.nanoTime();
   private final long load_J;
   private final Consumer<BlockPos> init_Consumer;
   private final LongConsumer init_LongConsumer;
   private final int sync_I;
   private boolean close;
   private boolean open;
   private long read_J;
   private long write_J;
   private static final int flush_I = 16384;
   private final AiScaffold.Inner_StreamUtil load_a_StreamUtil;
   private final ArrayDeque<BlockPos> init_ArrayDeque = new ArrayDeque<>();
   private long update_J;

   void init_V_Map(Map<BlockPos, BlockState> blocks) {
      blocks.forEach(this.init_UiHelper::init_V_BlockPos_BlockState);
   }

   void init_V_BlockPos_2(BlockPos position) {
      this.init_bj.init_V_BlockPos(position);
   }

   AiPrintOp(AiBuilder.Blueprint blueprint, int timeoutSeconds, AiBuilder.BuildOptions power) {
      this(blueprint, timeoutSeconds, power, AiPrintRules.init(blueprint));
   }

   AiPrintOp(AiBuilder.Blueprint blueprint, int timeoutSeconds, AiBuilder.BuildOptions power, List<autismclient.ai.print.PrintTarget> preparedTargets) {
      this(
         blueprint,
         timeoutSeconds,
         power,
         preparedTargets,
         power.machine().init_bV_StreamUtil_BlockPos_HolderLookup_Provi(blueprint.origin(), Minecraft.getInstance().level.registryAccess())
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
      autismclient.ai.print.PrintCore.ao geometry
   ) {
      AiPrintSupply.init_V_TableIndex_ConfigS(power);
      this.init_TableIndex_DataUti = blueprint;
      this.init_TableIndex_ConfigS = power;
      this.init_bj = new AiPrintSupply(blueprint, power.clearTerrain());
      this.init_Z = !power.clearTerrain();
      this.init_List = preparedTargets;
      this.init_DataUtil.init_V_Z(power.assumeFly());
      this.init_UiHelper = autismclient.ai.print.PrintCore.init_UiHelper_EventBus(this.init_DataUtil, AiPrintRules.init_bi, this.init_List, geometry);
      this.load_a_StreamUtil = AiScaffold.init(AiBuilder.init_Path(blueprint), List.of());
      this.init_ArrayDeque.addAll(this.load_a_StreamUtil.init_List());
      this.init_UiHelper.init_V_List(actions.vehicles());
      this.init_UiHelper.load_V_List(actions.frames());
      this.init_UiHelper.init_V_Map(actions.outcomes());

      for (AiSchematicMachinePlan.ContainerPlan saved : actions.containers()) {
         this.init_UiHelper.init_V_BitSetUtil(new AiPrintContainer(this.init_DataUtil, saved));
      }

      for (autismclient.ai.print.PrintSkippedContents unavailable : actions.unavailable()) {
         this.init_UiHelper.init_V_BitSetUtil(unavailable);
      }

      for (autismclient.ai.print.PrintClockSpec clock : actions.clocks()) {
         this.init_UiHelper.init_V_BitSetUtil(AiPrintClock.init(this.init_DataUtil, clock));
      }

      this.init_UiHelper.start_Z = Boolean.getBoolean("autism.ai.print.trace");
      this.init_Consumer = pos -> {
         this.init_DataUtil.load_V(pos);
         this.init_bj.load(pos);
      };
      this.init_LongConsumer = key -> {
         this.init_DataUtil.init_V_J(key);
         this.init_bj.init_V_J(key);
      };
      this.load_J = Math.max(0, timeoutSeconds) * 1000000000L;
      this.sync_I = this.init_Minecraft.player == null ? 0 : this.init_Minecraft.player.getInventory().getSelectedSlot();
      AiServerEvidence.init_V_Consumer(this.init_Consumer);
      AiServerEvidence.init_V_LongConsumer(this.init_LongConsumer);
      AiPrintOp.Inner_DataUtil.init("[AI] print v2: {} cells", this.init_List.size());
   }

   @Override
   public AiOperation.Result tick() {
      long started = System.nanoTime();

      AiOperation.Result result;
      try {
         result = this.init_AiOperation_Result_25();
      } finally {
         if (!this.open && System.nanoTime() >= this.update_J) {
            this.load_a_StreamUtil.init_V();
            this.update_J = System.nanoTime() + 1000000000L;
         }

         this.write_J = Math.max(this.write_J, System.nanoTime() - started);
      }

      if (result.terminal()) {
         JsonObject data = AiJson.init_JsonObject_Str(result.dataJson());
         data.addProperty("maxOperationTickMs", this.write_J / 1000000L);
         return new AiOperation.Result(result.status(), result.message(), data.toString(), result.evidence(), result.failure());
      } else {
         return result;
      }
   }

   private AiOperation.Result init_AiOperation_Result_25() {
      if (this.close) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status.CANCELLED, "Build cancelled");
      }

      if (this.init_Minecraft.player == null || this.init_Minecraft.level == null) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status.CANCELLED, "World unavailable");
      }

      if (this.init_Minecraft.level != this.init_ClientLevel || this.init_Minecraft.player != this.init_LocalPlayer) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status.CANCELLED, "World or player changed");
      }

      if (!this.init_Minecraft.player.isAlive()) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status.FAILED, "The player died");
      }

      if (this.load_J > 0L && System.nanoTime() - this.init_J > this.load_J) {
         return this.init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status.TIMED_OUT, "Requested build deadline reached");
      }

      AiMenuClose.Inner_StreamUtil menuClose = AiMenuClose.init_aq_StreamUtil();
      if (menuClose != AiMenuClose.Inner_StreamUtil.READY) {
         this.init_DataUtil.read_V();
         return menuClose == AiMenuClose.Inner_StreamUtil.UNCERTAIN
            ? this.init(AiOperation.Status.UNCERTAIN, "The server did not reconcile the closed storage menu. Inspect the inventory before resuming.", AiFailure.UNKNOWN)
            : this.init_AiOperation_Result_Str_9("Waiting for the server to close the storage menu");
      }

      if (!this.init_DataUtil.read_Z()) {
         this.init_DataUtil.read_V();
         return this.init_DataUtil.init_Str() != null
            ? this.init(this.init_DataUtil.write_Z() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED, this.init_DataUtil.init_Str(), AiFailure.UNKNOWN)
            : this.init_AiOperation_Result_Str_9("Waiting for the server to confirm the hand swap");
      }

      this.init_V_23();

      try {
         this.init_bj.init_Z_I(16384);
      } catch (IllegalStateException failure) {
         return this.init(AiOperation.Status.FAILED, failure.getMessage(), AiFailure.WORLD_UNKNOWN);
      }

      if (!this.init_bj.load_Z()) {
         this.init_DataUtil.read_V();
         return this.init_AiOperation_Result_Str_9(this.init_bj.init_Str());
      }

      if (this.init_UiHelper.init_Z()) {
         this.read_J++;
         this.init_DataUtil.init_V();
         this.init_DataUtil.init_V_SyncManager(this.init_UiHelper.read_SyncManager());
         return this.init_AiOperation_Result_Str_9(this.init_UiHelper.load_Str());
      }

      if (this.init_Minecraft.gui.screen() instanceof AbstractSignEditScreen editor) {
         try {
            if (this.init_DataUtil.init_Z_BlockPos(AiPrintSupply.init_BlockPos_AbstractSignEditSc(editor))) {
               AiPrintSupply.init_V_AbstractSignEditSc(editor, this.init_TableIndex_DataUti.origin(), this.init_TableIndex_ConfigS.signText());
               this.init_Minecraft.gui.setScreen(null);
               this.init_DataUtil.load_V();
            }
         } catch (ReflectiveOperationException | RuntimeException failure) {
            return this.init(AiOperation.Status.FAILED, "Could not apply the schematic's sign text", AiFailure.EXECUTION_FAILED);
         }
      }

      autismclient.ai.engine.AiRoutePolicy.putAt(this.init_bj.init_Set()::contains);
      if (!this.init_DataUtil.update_Z()) {
         AiOperation.Result guarded = this.init_Y
            .init_AiOperation_Result_AiOperation(
               this,
               this::write_V_6,
               () -> this.load_J == 0L ? 120 : (int)Math.max(1L, Math.min(2147483647L, (this.load_J - (System.nanoTime() - this.init_J)) / 1000000000L))
            );
         if (guarded != null) {
            this.flush_Z = true;
            if (guarded.terminal()) {
               return this.init(guarded.status(), guarded.message(), guarded.failure());
            }

            return this.init_AiOperation_Result_Str_9("Survival: " + guarded.message());
         }
      } else if (this.init_Y.init_Z()) {
         boolean pending = this.init_Y.read();
         this.init_Y.init_V();
         if (pending) {
            return this.init(AiOperation.Status.UNCERTAIN, "Game mode changed during an unconfirmed survival action", AiFailure.UNKNOWN);
         }
      }

      if (this.flush_Z) {
         this.flush_Z = false;
         this.read_V_8();
         this.init_UiHelper.load_V();
      }

      if (this.update_Z || this.sync_Z) {
         return this.init(AiOperation.Status.UNCERTAIN, "Build maintenance was interrupted before recovery was confirmed", AiFailure.UNKNOWN);
      }

      if (this.init_AiOperation != null
         && this.init_Item != null
         && this.init_DataUtil.update_Z()
         && this.init_AiOperation.canInterruptNow()
         && !this.init_AiOperation.requiresReconciliationAfterEmergency()) {
         this.init_AiOperation.cancel();
         this.init_AiOperation = null;
         if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
            AiMenuClose.init_Z();
         }

         this.read_V_8();
         this.init_bj.load_V();
         this.init_Item = null;
         this.init_UiHelper.load_V();
         return this.init_AiOperation_Result_Str_9("Creative inventory is available; resuming construction");
      } else {
         if (this.init_AiOperation != null) {
            return this.sync_AiOperation_Result_3();
         }

         if (!this.init_UiHelper.write_Z()
            && this.init_Minecraft.gui.screen() instanceof AbstractContainerScreen<?> opened
            && !(opened instanceof InventoryScreen)
            && this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu
            && this.init_DataUtil.init_Z()) {
            AiPrintOp.Inner_DataUtil.init("[AI] print v2: closing {} the build opened", opened.getClass().getSimpleName());
            AiMenuClose.init_Z();
            this.init_DataUtil.load_V();
            this.init_DataUtil.read_V();
            return this.init_AiOperation_Result_Str_9("Waiting for the server to close the storage menu");
         }

         if (this.init_Minecraft.gui.screen() != null && !this.init_UiHelper.write_Z()) {
            this.init_DataUtil.read_V();
            return this.init_AiOperation_Result_Str_9("Paused while a screen is open");
         }

         if (this.init_DataUtil.poll() && !this.init_DataUtil.flush() && !this.init_UiHelper.write_Z()) {
            this.init_DataUtil.read_V();
            return this.init_AiOperation_Result_Str_9("Paused: land or disable native flight before continuing ground construction");
         }

         if (!this.init_DataUtil.update_Z()
            && !this.init_UiHelper.read_Z()
            && !this.init_UiHelper.flush_Z()
            && this.init_Minecraft.player.getFoodData().getFoodLevel() <= 14
            && !this.init_UiHelper.close_Z()) {
            AiGuard.StatusSnapshot vitals = AiGuard.init_X_BufferHelper();
            if (vitals.hasFood() && vitals.hostilesNear() == 0 && AiSurvival.init_Z(vitals.food())) {
               return this.load("Settling before a meal");
            }
         }

         if (!this.init_Z) {
            try {
               if (this.init_BlockPos == null) {
                  this.init_BlockPos = this.init_bj.init_BlockPos();
               }
            } catch (IllegalStateException failure) {
               return this.init(AiOperation.Status.FAILED, failure.getMessage(), AiFailure.UNREACHABLE);
            }

            if (this.init_BlockPos != null) {
               if (!this.init_UiHelper.close_Z()) {
                  return this.load("Settling before clearing the site");
               }

               this.load_Z = false;
               this.init_V_AiOperation(autismclient.ai.engine.AiBreakBlock.init(this.init_BlockPos, 30, false, this.init_Set_7(), false));
               return this.init_AiOperation_Result_Str_9("Clearing terrain from the schematic's air at " + this.init_BlockPos.toShortString());
            }

            if (!this.init_bj.write_Z()) {
               return this.init_AiOperation_Result_Str_9("Checking remaining terrain");
            }

            this.init_Z = true;
            this.init_UiHelper.load_V();
            this.read_Z = false;
         }

         if (this.read_Z) {
            this.init_UiHelper.load_V();
            this.read_Z = false;
         }

         if (this.load_Item != null) {
            return this.write_AiOperation_Result_5();
         }

         if (!this.init_ItemStack.isEmpty()) {
            return this.update_AiOperation_Result_3();
         }

         if (this.init_Item != null) {
            return this.load_AiOperation_Result_14();
         }

         this.read_J++;
         this.init_DataUtil.init_V();
         autismclient.ai.print.PrintControls controls = this.init_UiHelper.read_SyncManager();
         ItemStack requestedContents = this.init_UiHelper.init_ItemStack();
         if (!requestedContents.isEmpty()) {
            this.init_ItemStack = requestedContents;
            this.init_DataUtil.init_V_SyncManager(controls);
            return this.init_AiOperation_Result_Str_9("Preparing carried stock for saved container contents");
         }

         if (this.init_UiHelper.init_Item() == Items.AIR && this.init_UiHelper.close_Z()) {
            return this.init(AiOperation.Status.FAILED, "Free one inventory or offhand slot so the builder can use an empty hand", AiFailure.INVENTORY_FULL);
         }

         if (!this.init_DataUtil.update_Z() && this.init_UiHelper.load_Item() != null) {
            this.load_Item = this.init_UiHelper.load_Item();
            this.load_I = AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.load_Item);
            this.init_DataUtil.init_V_SyncManager(controls);
            return this.init_AiOperation_Result_Str_9("Preparing to recover " + init_Str_Item(this.load_Item));
         }

         if (!this.init_DataUtil.update_Z() && this.init_UiHelper.init_Z_Item(this.init_UiHelper.init_Item())) {
            this.init_DataUtil.init_V_SyncManager(controls);
            return this.init_AiOperation_Result_Str_9("Recovering temporary " + init_Str_Item(this.init_UiHelper.init_Item()));
         }

         if (this.init_DataUtil.update_Z() || !this.init_bj.init_Z_Item(this.init_UiHelper.init_Item())) {
            this.init_DataUtil.init_V_SyncManager(controls);
            if (this.read_J % 4L == 0L) {
               AiVisualization.init(this, this.init_TableIndex_DataUti.origin(), this.init_TableIndex_DataUti.cells(), this.init_UiHelper.update_I(), false, true);
            }

            AiOperation.Result var10000;
            switch (this.init_UiHelper.init_UiHelper_J()) {
               case DONE:
                  if (this.init_ArrayDeque.isEmpty()) {
                     var10000 = this.init_AiOperation_Result_AiOperation_Status_Str_8(
                        AiOperation.Status.SUCCEEDED,
                        "Built "
                           + this.init_UiHelper.update_I()
                           + " of "
                           + this.init_UiHelper.write_I()
                           + " cells"
                           + (this.init_UiHelper.set_I() == 0 ? "" : ". Skipped unavailable contents in " + this.init_UiHelper.set_I() + " containers")
                           + (this.init_UiHelper.put_I() == 0 ? "" : ". Off-rail minecarts still require assembly: " + this.init_UiHelper.put_I())
                           + (this.init_UiHelper.start_I() == 0 ? "" : ". Saved rail shapes still differ: " + this.init_UiHelper.start_I())
                     );
                  } else {
                     this.init_UiHelper.init_V_BlockPos(this.init_ArrayDeque.peekFirst());
                     var10000 = this.init_AiOperation_Result_Str_9("Loading saved construction blocks before final cleanup");
                  }
                  break;
               case FAILED:
                  var10000 = this.init_AiOperation_Result_AiOperation_Status_Str_8(
                     this.init_UiHelper.update_Z() ? AiOperation.Status.UNCERTAIN : AiOperation.Status.FAILED,
                     "Build stopped: " + this.init_UiHelper.write_Str()
                  );
                  break;
               default:
                  var10000 = this.init_AiOperation_Result_Str_9(this.init_UiHelper.load_Str());
            }

            return var10000;
         } else {
            if (!this.init_TableIndex_ConfigS.shulkerStock()) {
               return this.init(AiOperation.Status.FAILED, "Missing material: " + init_Str_Item(this.init_bj.init_Item()), AiFailure.MISSING_MATERIALS);
            }

            this.init_Item = this.init_bj.init_Item();
            this.init_DataUtil.init_V_SyncManager(controls);
            return this.init_AiOperation_Result_Str_9("Preparing to fetch " + init_Str_Item(this.init_Item));
         }
      }
   }

   private void init_V_23() {
      int count = Math.min(128, this.init_ArrayDeque.size());

      for (int checked = 0; checked < count; checked++) {
         BlockPos position = this.init_ArrayDeque.removeFirst();
         if (!this.init_DataUtil.load_Z(position)) {
            this.init_ArrayDeque.addLast(position);
         } else {
            BlockState state = this.init_DataUtil.init_BlockState_BlockPos(position);
            if (AiScaffold.init(this.load_a_StreamUtil.init(position), state.getBlock())) {
               this.init_UiHelper.init_V_BlockPos_BlockState(position, state);
            } else {
               AiScaffold.init_V(position);
            }
         }
      }
   }

   private AiOperation.Result init_AiOperation_Result_Str_9(String message) {
      String phase;
      if (!this.init_bj.load_Z() || this.init_UiHelper.init_Z()) {
         phase = "Preparing site";
      } else if (!this.init_Z) {
         phase = "Clearing terrain";
      } else if (this.init_Item != null || !this.init_ItemStack.isEmpty()) {
         phase = "Restocking";
      } else if (this.load_Item != null) {
         phase = "Recovering drops";
      } else {
         String var10000;
         switch (this.init_UiHelper.init_UiHelper_J()) {
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
               var10000 = AiTaskStatus.init(message, "Placing blocks");
         }

         phase = var10000;
      }

      AiVisualization.init(this, "Building schematic", phase, this.init_UiHelper.update_I(), this.init_UiHelper.write_I());
      return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_42().toString(), List.of());
   }

   private AiOperation.Result load(String message) {
      this.read_J++;
      this.init_DataUtil.init_V();
      this.read_Z = true;
      autismclient.ai.print.PrintControls controls = this.init_UiHelper.init_SyncManager();
      if (controls == null) {
         return this.init(AiOperation.Status.FAILED, "No safe landing before the next build maintenance action", AiFailure.UNREACHABLE);
      }

      this.init_DataUtil.init_V_SyncManager(controls);
      return this.init_AiOperation_Result_Str_9(message);
   }

   private AiOperation.Result load_AiOperation_Result_14() {
      if ((this.init_DataUtil.update_Z() || AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.init_Item) > 0) && this.init_UiHelper.close_Z()) {
         this.init_bj.load_V();
         this.init_Item = null;
         this.init_UiHelper.load_V();
         return this.init_AiOperation_Result_Str_9("Material is available; resuming construction");
      }

      this.read_J++;
      this.init_DataUtil.init_V();
      boolean portableHere = this.init_UiHelper.close_Z()
         && this.init_bj.read_Z()
         && AiCarriedShulker.init_BlockPos_Minecraft_Set(this.init_Minecraft, this.init_Set_7()) != null;
      autismclient.ai.print.PrintControls controls = portableHere ? this.init_UiHelper.init_SyncManager() : this.init_UiHelper.load_SyncManager();
      if (controls == null) {
         return this.init(AiOperation.Status.FAILED, "No safe route from the work station to shulker stock", AiFailure.UNREACHABLE);
      }

      this.init_DataUtil.init_V_SyncManager(controls);
      if (!portableHere && !this.init_UiHelper.open_Z()) {
         return this.init_AiOperation_Result_Str_9("Returning to ground for " + init_Str_Item(this.init_Item));
      }

      if (!this.init_DataUtil.update_Z() && AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.init_Item) <= 0) {
         this.init_I = AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.init_Item);
         if (!this.init_bj.load(16384)) {
            return this.init_AiOperation_Result_Str_9("Checking shulker stock for " + init_Str_Item(this.init_Item));
         }

         AiOperation refill = this.init_bj.init_AiOperation_Set(this.init_UiHelper.init_Set());
         if (refill == null && this.init_Item == Items.DIRT) {
            return this.read_AiOperation_Result_9();
         }

         if (refill == null) {
            return this.init(
               AiOperation.Status.FAILED,
               "No usable carried or nearby shulker could supply "
                  + init_Str_Item(this.init_Item)
                  + ". Keep an inventory slot free for the withdrawn material and leave room to recover the box.",
               AiFailure.MISSING_MATERIALS
            );
         }

         this.init_V_AiOperation(refill);
         return this.init_AiOperation_Result_Str_9("Fetching " + init_Str_Item(this.init_Item) + " from shulker stock");
      } else {
         this.init_bj.load_V();
         this.init_Item = null;
         this.init_UiHelper.load_V();
         return this.init_AiOperation_Result_Str_9("Material is available; resuming construction");
      }
   }

   private AiOperation.Result read_AiOperation_Result_9() {
      this.write_Z = true;
      this.write_I = AiPrintSupply.load(this.init_Minecraft.player.getInventory(), Items.DIRT);
      this.init_V_AiOperation(new AiPrintOp.Inner_StreamUtil(Math.max(this.init_I, this.init_bj.init_I())));
      return this.init_AiOperation_Result_Str_9("Gathering soil nearby");
   }

   private int init_I_28() {
      return this.read_I + (this.write_Z ? Math.max(0, AiPrintSupply.load(this.init_Minecraft.player.getInventory(), Items.DIRT) - this.write_I) : 0);
   }

   private void load_V_12() {
      this.read_I = this.init_I_28();
      this.write_Z = false;
   }

   private AiOperation.Result write_AiOperation_Result_5() {
      int held = AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.load_Item);
      if (held > this.load_I) {
         this.init_UiHelper.init_V_Item_I(this.load_Item, held - this.load_I);
         this.load_Item = null;
         this.init_UiHelper.load_V();
         return this.init_AiOperation_Result_Str_9("Recovered temporary material; resuming construction");
      } else {
         this.read_J++;
         this.init_DataUtil.init_V();
         autismclient.ai.print.PrintControls controls = this.init_UiHelper.init_SyncManager();
         if (controls == null) {
            return this.init(AiOperation.Status.FAILED, "No safe landing before recovering temporary material", AiFailure.UNREACHABLE);
         } else {
            this.init_DataUtil.init_V_SyncManager(controls);
            if (!this.init_UiHelper.close_Z()) {
               return this.init_AiOperation_Result_Str_9("Settling before recovering " + init_Str_Item(this.load_Item));
            } else {
               int remaining = this.init_UiHelper.init_I_Item(this.load_Item);
               if (remaining <= 0) {
                  this.init_UiHelper.init_V_Item_I(this.load_Item, 0);
                  this.load_Item = null;
                  this.init_UiHelper.load_V();
                  return this.init_AiOperation_Result_Str_9("Temporary material reconciled; resuming construction");
               } else {
                  this.init_V_AiOperation(
                     AiGameplay.write(
                        AiJson.init_JsonObject_ObjArr(
                           "item", init_Str_Item(this.load_Item), "count", Math.min(remaining, 2304), "additional", true, "radius", 16, "timeoutSeconds", 20
                        )
                     )
                  );
                  return this.init_AiOperation_Result_Str_9("Collecting dropped " + init_Str_Item(this.load_Item));
               }
            }
         }
      }
   }

   private AiOperation.Result update_AiOperation_Result_3() {
      if (!this.init_TableIndex_ConfigS.shulkerStock()) {
         return this.init_AiOperation_Result_Z_Str(false, "Carried shulker stock is disabled");
      }

      this.read_J++;
      this.init_DataUtil.init_V();
      autismclient.ai.print.PrintControls controls = this.init_UiHelper.load_SyncManager();
      if (controls == null) {
         return this.init_AiOperation_Result_Z_Str(false, "No safe stock route for the optional saved contents");
      }

      this.init_DataUtil.init_V_SyncManager(controls);
      if (!this.init_UiHelper.open_Z()) {
         return this.init_AiOperation_Result_Str_9("Returning to a safe stock workstation for saved contents");
      }

      AiOperation refill = this.init_bj.init_AiOperation_ItemStack(this.init_ItemStack, this.init_ItemStack.getCount(), this.init_UiHelper.init_Set());
      if (refill == null) {
         return this.init_AiOperation_Result_Z_Str(false, "No carried box can supply the exact saved item");
      }

      this.init_V_AiOperation(refill);
      return this.init_AiOperation_Result_Str_9("Fetching exact saved contents from carried shulker stock");
   }

   private AiOperation.Result init_AiOperation_Result_Z_Str(boolean available, String message) {
      if (available) {
         this.init_bj.read_V();
      }

      this.init_UiHelper.init_V_Z(available);
      this.init_bj.write_V();
      this.init_ItemStack = ItemStack.EMPTY;
      this.init_UiHelper.load_V();
      this.read_Z = false;
      return this.init_AiOperation_Result_Str_9(message);
   }

   private AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_8(AiOperation.Status status, String message) {
      return this.init(status, message, status == AiOperation.Status.CANCELLED ? AiFailure.CANCELLED : AiFailure.UNKNOWN);
   }

   private AiOperation.Result init(AiOperation.Status status, String message, AiFailure failure) {
      this.sync_V_3();
      if (status != AiOperation.Status.SUCCEEDED && (this.init_UiHelper.update_Z() || this.update_Z || this.sync_Z)) {
         status = AiOperation.Status.UNCERTAIN;
      }

      JsonObject data = this.init_JsonObject_42();
      AiPrintOp.Inner_DataUtil.init("[AI] print v2: {} - {} ({})", status, message, data);
      return status == AiOperation.Status.SUCCEEDED
         ? new AiOperation.Result(status, message, data.toString(), List.of())
         : new AiOperation.Result(status, message, data.toString(), List.of(), failure);
   }

   private Set<Long> init_Set_7() {
      return init_Set_Set_Set(this.init_UiHelper.init_Set(), this.init_bj.init_Set());
   }

   static Set<Long> init_Set_Set_Set(final Set<Long> first, final Set<Long> second) {
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

   private static String init_Str_Item(Item item) {
      return BuiltInRegistries.ITEM.getKey(item).toString();
   }

   private void init_V_AiOperation(AiOperation operation) {
      this.init_DataUtil.read_V();
      this.init_a_StreamUtil = AiScaffold.init_a_StreamUtil();
      this.init_AiOperation = operation;
   }

   private void read_V_8() {
      if (this.init_a_StreamUtil != null) {
         if (this.init_Minecraft.level == this.init_ClientLevel) {
            for (BlockPos p : this.init_a_StreamUtil.init_List()) {
               BlockState state = this.init_Minecraft.level.getBlockState(p);
               if (AiScaffold.init(this.init_a_StreamUtil.init(p), state.getBlock())) {
                  this.init_UiHelper.init_V_BlockPos_BlockState(p, state);
               }
            }
         }

         this.init_a_StreamUtil.close();
         this.init_a_StreamUtil = null;
      }
   }

   private AiOperation.Result sync_AiOperation_Result_3() {
      autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_J(3000000000L);
      autismclient.ai.engine.AiPathfinder.MoveCosts.load_V_J(3000000000L);
      AiOperation.Result result = this.init_AiOperation.tick();
      this.init_JsonObject = AiJson.init_JsonObject_Str(result.dataJson());
      if (!result.terminal()) {
         return this.init_AiOperation_Result_Str_9(
            (
                  this.init_BlockPos != null
                     ? "Clearing terrain: "
                     : (this.write_Z ? "Local soil: " : (this.load_Item != null ? "Drop recovery: " : "Shulker refill: "))
               )
               + result.message()
         );
      }

      this.init_JsonObject.addProperty("status", result.status().name());
      this.init_JsonObject.addProperty("message", result.message());
      this.update_V_4();
      this.read_V_8();
      if (this.update_Z) {
         return this.init(AiOperation.Status.UNCERTAIN, "External work needs reconciliation: " + result.message(), result.failure());
      }

      if (result.status() != AiOperation.Status.UNCERTAIN && result.status() != AiOperation.Status.CANCELLED && result.status() != AiOperation.Status.TIMED_OUT
         )
       {
         if (this.init_Minecraft.player.containerMenu != this.init_Minecraft.player.inventoryMenu) {
            AiMenuClose.init_Z();
         }

         if (this.init_BlockPos != null) {
            if (!this.load_Z && result.status() == AiOperation.Status.FAILED && result.failure() == AiFailure.UNREACHABLE) {
               this.load_Z = true;
               this.init_V_AiOperation(autismclient.ai.engine.AiBreakBlock.init(this.init_BlockPos, 30, false, this.init_Set_7(), true));
               return this.init_AiOperation_Result_Str_9("Finding supported access to terrain at " + this.init_BlockPos.toShortString());
            } else {
               BlockPos p = this.init_BlockPos;
               this.init_BlockPos = null;
               if (result.status() == AiOperation.Status.SUCCEEDED && !AiSchematicActions.init(this.init_Minecraft.level.getBlockState(p))) {
                  this.init_bj.load_I++;
                  return this.init_AiOperation_Result_Str_9("Terrain removed; checking the next obstruction");
               } else {
                  return this.init(AiOperation.Status.FAILED, "Could not clear terrain at " + p.toShortString() + ": " + result.message(), result.failure());
               }
            }
         } else if (this.load_Item != null) {
            int gained = Math.max(0, AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.load_Item) - this.load_I);
            this.init_UiHelper.init_V_Item_I(this.load_Item, gained);
            if (result.status() == AiOperation.Status.SUCCEEDED) {
               this.load_Item = null;
               this.init_UiHelper.load_V();
               this.read_Z = false;
               return this.init_AiOperation_Result_Str_9("Temporary drops recovered; resuming construction");
            } else {
               this.init_UiHelper.init_V_Item(this.load_Item);
               this.load_Item = null;
               this.init_UiHelper.load_V();
               this.read_Z = false;
               return this.init_AiOperation_Result_Str_9("Drop recovery was inconclusive; checking ordinary stock");
            }
         } else if (this.init_ItemStack.isEmpty()) {
            this.init_UiHelper.load_V();
            this.read_Z = false;
            if (AiPrintSupply.load(this.init_Minecraft.player.getInventory(), this.init_Item) > this.init_I) {
               this.init_bj.init_V();
               this.init_Item = null;
               return this.init_AiOperation_Result_Str_9("Shulker stock received; resuming construction");
            } else if (this.init_DataUtil.update_Z()) {
               this.init_bj.load_V();
               this.init_Item = null;
               return this.init_AiOperation_Result_Str_9("Creative inventory is available; resuming construction");
            } else {
               return this.init_AiOperation_Result_Str_9("Checking the next shulker stock source");
            }
         } else {
            return result.status() == AiOperation.Status.SUCCEEDED
               ? this.init_AiOperation_Result_Z_Str(true, "Recovered exact stock; checking the saved container again")
               : this.init_AiOperation_Result_Str_9("Checking the next carried shulker for optional saved contents");
         }
      } else {
         return this.init(result.status(), "External work needs reconciliation: " + result.message(), result.failure());
      }
   }

   private void write_V_6() {
      this.init_DataUtil.read_V();
      if (this.init_UiHelper.read_Z()) {
         this.init_UiHelper.init_V();
         this.sync_Z = true;
      }

      this.update_V_4();
      this.read_V_8();
      this.init_UiHelper.load_V();
      this.read_Z = false;
      this.init_a_StreamUtil = AiScaffold.init_a_StreamUtil();
   }

   private void update_V_4() {
      this.load_V_12();
      if (this.init_AiOperation != null) {
         this.update_Z = this.update_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
         this.init_AiOperation.cancel();
         this.update_Z = this.update_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
         if (this.update_Z) {
            if (this.init_JsonObject == null) {
               this.init_JsonObject = new JsonObject();
            }

            this.init_JsonObject.addProperty("recoveryRequired", true);
         }

         this.init_AiOperation = null;
      }
   }

   private JsonObject init_JsonObject_42() {
      JsonObject o = new JsonObject();
      o.addProperty("engine", "print-v2");
      o.addProperty("phase", this.init_UiHelper.init_UiHelper_J().name());
      o.addProperty(
         "stage",
         !this.init_bj.load_Z() || this.init_UiHelper.init_Z()
            ? "prepare"
            : (
               !this.init_Z
                  ? "clear"
                  : (
                     this.load_Item != null
                        ? "recovery"
                        : (
                           this.init_Item == null && this.init_ItemStack.isEmpty()
                              ? (
                                 this.init_UiHelper.init_UiHelper_J() == autismclient.ai.print.PrintCore.J.TEARDOWN
                                    ? "drain"
                                    : this.init_UiHelper.init_UiHelper_J().name().toLowerCase(Locale.ROOT)
                              )
                              : "stock"
                        )
                  )
            )
      );
      o.addProperty("temporaryClickBlocksRemoved", this.init_UiHelper.pop_I);
      o.addProperty("climbSteps", this.init_UiHelper.f_j_I);
      o.addProperty("supportsDrained", this.init_UiHelper.pop_I);
      o.addProperty("originalSupportsDrained", this.init_UiHelper.init_I());
      o.addProperty("originalCleanupRise", this.init_UiHelper.init_J());
      o.addProperty("cleanupBlocksRecovered", this.init_UiHelper.load_J());
      o.addProperty("cells", this.init_UiHelper.write_I());
      o.addProperty("placed", this.init_UiHelper.update_I());
      o.addProperty("railVehiclesVerified", this.init_UiHelper.pop_I());
      o.addProperty("railSlopesVerified", this.init_UiHelper.close_I());
      o.addProperty("railShapeDifferences", this.init_UiHelper.start_I());
      o.addProperty("movingPlacements", this.init_UiHelper.put_I);
      o.addProperty("headFacingProofReuses", this.init_UiHelper.f_q_I);
      o.addProperty("hoppersFilled", this.init_UiHelper.tick_I());
      o.addProperty("containersFilled", this.init_UiHelper.get_I());
      o.addProperty("itemFramesVerified", this.init_UiHelper.run_I());
      o.addProperty("clockPhasesVerified", this.init_UiHelper.push_I());
      o.addProperty("clockAwaitingCycle", this.init_UiHelper.load_Z());
      o.addProperty("clocksVerified", this.init_UiHelper.open_I());
      o.addProperty("clockItemsVerified", this.init_UiHelper.poll_I());
      String recovery = this.open ? this.init_UiHelper.update_Str() : null;
      if (recovery != null) {
         o.add("machineRecovery", JsonParser.parseString(recovery));
      }

      o.addProperty("containersSkipped", this.init_UiHelper.set_I());
      o.addProperty("railVehiclesSkipped", this.init_UiHelper.put_I());
      o.addProperty("savedSlotsSkipped", this.init_UiHelper.add_I());
      o.addProperty("savedSlotsVerified", this.init_UiHelper.clear_I());
      o.addProperty("machineOutcomesUnverified", this.init_UiHelper.reset_I());
      o.addProperty(
         "machineComplete",
         this.init_UiHelper.init_UiHelper_J() == autismclient.ai.print.PrintCore.J.DONE
            && this.init_UiHelper.set_I() == 0
            && this.init_UiHelper.put_I() == 0
            && this.init_UiHelper.reset_I() == 0
            && this.init_UiHelper.start_I() == 0
      );
      JsonArray omissions = new JsonArray();
      this.init_UiHelper.init_List().forEach(omissions::add);
      o.add("skippedContents", omissions);
      int open = this.init_UiHelper.sync_I();
      o.addProperty("open", open);
      o.addProperty("pending", open - this.init_UiHelper.queue_I);
      o.addProperty("failedTotal", this.init_UiHelper.queue_I);
      if (Boolean.getBoolean("autism.ai.schematics")) {
         o.addProperty("pendingPlanning", this.init_UiHelper.read_Str());
      }

      o.addProperty("scaffolds", this.init_UiHelper.push_I);
      o.addProperty("scaffoldingRemaining", this.init_UiHelper.flush_I());
      o.addProperty("clicks", this.init_UiHelper.open_I);
      o.addProperty("decisions", this.init_UiHelper.tick_I);
      o.addProperty("planMs", this.init_UiHelper.init_J / 1000000L);
      o.addProperty("totalCoreMs", this.init_UiHelper.load_J / 1000000L);
      o.addProperty("maxDecisionMs", this.init_UiHelper.read_J / 1000000L);
      o.addProperty("maxOperationTickMs", this.write_J / 1000000L);
      o.addProperty("initializationMs", this.init_UiHelper.write_J / 1000000L);
      if (!this.init_bj.load_Z() || this.init_UiHelper.init_Z()) {
         o.addProperty("preparationDone", this.init_bj.init_J() + this.init_UiHelper.load_I());
         o.addProperty("preparationTotal", this.init_bj.load_J() + this.init_UiHelper.read_I());
         o.addProperty("supplyPreparationDone", this.init_bj.init_J());
         o.addProperty("corePreparationDone", this.init_UiHelper.read_J());
      }

      o.addProperty("ticks", this.read_J);
      o.addProperty("shulkerRefills", this.init_bj.init_I);
      o.addProperty("terrainCleared", this.init_bj.load_I);
      o.addProperty("soilCollected", this.init_I_28());
      o.addProperty("soilBroken", this.update_I);
      o.addProperty("interactionAwaitingAck", this.init_DataUtil.init_EventBus_StreamUti() == autismclient.ai.print.PrintEnv.Inner_StreamUtil.PENDING);
      o.addProperty(
         "recoveryRequired",
         AiMenuClose.load()
            || this.update_Z
            || this.sync_Z
            || this.init_UiHelper.update_Z()
            || this.init_DataUtil.write_Z()
            || this.init_DataUtil.init_EventBus_StreamUti() == autismclient.ai.print.PrintEnv.Inner_StreamUtil.UNCERTAIN
      );
      if (this.init_JsonObject != null) {
         o.add("externalWork", this.init_JsonObject.deepCopy());
      }

      if (this.init_UiHelper.write_Str() != null) {
         o.addProperty("why", this.init_UiHelper.write_Str());
      }

      JsonArray materials = new JsonArray();
      if (!this.init_UiHelper.init_Z()) {
         for (Item item : this.init_UiHelper.init_List_I(24)) {
            materials.add(BuiltInRegistries.ITEM.getKey(item).toString());
         }
      }

      o.add("nextMaterials", materials);
      return o;
   }

   @Override
   public boolean canInterruptNow() {
      return !AiMenuClose.load()
         && !this.update_Z
         && !this.sync_Z
         && !this.init_DataUtil.write_Z()
         && !this.init_UiHelper.read_Z()
         && this.init_Y.load()
         && (
            this.init_AiOperation != null
               ? this.init_AiOperation.canInterruptNow()
               : !this.init_UiHelper.sync_Z() && this.init_Minecraft.player != null && this.init_Minecraft.player.onGround()
         );
   }

   @Override
   public boolean requiresReconciliationAfterEmergency() {
      return this.init_Y.read()
         || AiMenuClose.load()
         || this.update_Z
         || this.sync_Z
         || this.init_DataUtil.write_Z()
         || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
         || this.init_UiHelper.read_Z()
         || this.init_AiOperation == null && this.init_UiHelper.sync_Z();
   }

   @Override
   public void cancel() {
      this.close = true;
      this.sync_V_3();
   }

   private void sync_V_3() {
      if (!this.open) {
         this.open = true;
         this.init_UiHelper.init_V();
         this.update_V_4();
         this.sync_Z = this.sync_Z | this.init_Y.read();
         this.init_Y.init_V();
         this.sync_Z = this.sync_Z | this.init_Y.read();
         this.read_V_8();
         autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_J(0L);
         autismclient.ai.engine.AiPathfinder.MoveCosts.load_V_J(0L);
         autismclient.ai.engine.AiRoutePolicy.putAt_V();
         AiServerEvidence.load_V_Consumer(this.init_Consumer);
         AiServerEvidence.load_V_LongConsumer(this.init_LongConsumer);
         this.init_DataUtil.read_V();
         this.load_a_StreamUtil.close();
         if (this.init_Minecraft.player == this.init_LocalPlayer
            && this.init_Minecraft.player != null
            && this.init_Minecraft.player.getInventory().getSelectedSlot() != this.sync_I) {
            autismclient.util.InventorySlotHelper.selectHotbarSlot(this.init_Minecraft, this.sync_I);
         }
      }
   }

   private static final class Inner_DataUtil {
      static void init(String format, Object... args) {
         AutismClientAddon.LOG.info(format, args);
      }
   }

   private final class Inner_StreamUtil implements AiOperation {
      private final List<BlockPos> init_List = new ArrayList<>();
      private final long init_J = System.nanoTime() + 600000000000L;
      private final AiPreparationReceipt init_bb = new AiPreparationReceipt();
      private int init_I;
      private int load;
      private int read;
      private int write;
      private AiOperation init_AiOperation;
      private BlockPos init_BlockPos;
      private boolean init_Z;
      private final int update;

      Inner_StreamUtil(int target) {
         BlockPos center = AiPrintOp.this.init_Minecraft.player.blockPosition();

         for (BlockPos pos : BlockPos.betweenClosed(center.offset(-32, -4, -32), center.offset(32, 4, 32))) {
            if (AiTerrain.init(AiPrintOp.this.init_Minecraft.level, pos) && !AiPrintOp.this.init_Set_7().contains(pos.asLong()) && !AiPrintOp.this.init_Set.contains(pos.asLong())) {
               BlockState state = AiPrintOp.this.init_Minecraft.level.getBlockState(pos);
               if ((
                     state.is(Blocks.DIRT)
                        || state.is(Blocks.GRASS_BLOCK)
                        || state.is(Blocks.COARSE_DIRT)
                        || state.is(Blocks.ROOTED_DIRT)
                        || state.is(Blocks.MYCELIUM)
                        || state.is(Blocks.PODZOL)
                  )
                  && state.getFluidState().isEmpty()) {
                  this.init_List.add(pos.immutable());
               }
            }
         }

         this.init_List.sort(Comparator.comparingDouble(posx -> posx.distToCenterSqr(center.getX(), center.getY(), center.getZ())));
         this.update = target;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.init_Z) {
            return this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Local soil gathering cancelled", "{}", List.of(), AiFailure.CANCELLED));
         }

         if (AiPrintOp.this.init_Minecraft.player == null || AiPrintOp.this.init_Minecraft.level == null || System.nanoTime() > this.init_J) {
            return this.init(new AiOperation.Result(AiOperation.Status.TIMED_OUT, "Local soil gathering timed out", "{}", List.of(), AiFailure.TIMEOUT));
         }

         if (AiPrintSupply.load(AiPrintOp.this.init_Minecraft.player.getInventory(), Items.DIRT) >= this.update && !this.init_bb.init_Z(this.init_AiOperation)) {
            return this.init(new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Collected the required soil", "{}", List.of()));
         }

         if (this.init_AiOperation == null) {
            if (this.init_I >= this.init_List.size()) {
               return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "No reachable local soil remains", "{}", List.of(), AiFailure.NO_REACHABLE_TARGET));
            }

            if (!autismclient.ai.engine.AiBlockMine.init(AiPrintOp.this.init_Minecraft.player.getInventory(), new ItemStack(Items.DIRT))) {
               return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "Make inventory room for local soil", "{}", List.of(), AiFailure.INVENTORY_FULL));
            }

            this.init_BlockPos = this.init_List.get(this.init_I++);
            this.read = AiPrintSupply.load(AiPrintOp.this.init_Minecraft.player.getInventory(), Items.DIRT);
            this.write = 0;
            this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(
               this.init_BlockPos,
               30,
               false,
               AiPrintOp.this.init_Set_7(),
               false,
               Set.of(),
               (state, stack) -> !state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.MYCELIUM) && !state.is(Blocks.PODZOL)
                  || !BlockBreakSpeedHelper.init_Z(AiPrintOp.this.init_Minecraft, stack)
            );
         }

         AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
         if (!result.terminal()) {
            return this.init(result.message());
         }

         AiOperation completed = this.init_AiOperation;
         this.init_AiOperation = null;
         this.init_bb.init_V(completed);
         if (result.status() != AiOperation.Status.SUCCEEDED) {
            AiPrintOp.this.init_Set.add(this.init_BlockPos.asLong());
            if (AiPrintOp.this.init_UiHelper.start_Z) {
               AiPrintOp.Inner_DataUtil.init("[AI] print v2: local soil at {} skipped: {}", this.init_BlockPos.toShortString(), result.message());
            }

            return result.status() == AiOperation.Status.FAILED && !this.init_bb.init_Z(completed) && ++this.load < 24
               ? this.init("Skipped an unreachable soil block; trying another")
               : this.init(result);
         } else {
            AiPrintOp.this.update_I++;
            int held = AiPrintSupply.load(AiPrintOp.this.init_Minecraft.player.getInventory(), Items.DIRT);
            if (held > this.read) {
               return this.init("Soil picked up; approaching the next block");
            } else {
               return ++this.write >= 12 ? this.init("Approaching the next soil block") : this.init("Waiting briefly for the nearby soil pickup");
            }
         }
      }

      private AiOperation.Result init(String message) {
         int held = AiPrintSupply.load(AiPrintOp.this.init_Minecraft.player.getInventory(), Items.DIRT);
         return new AiOperation.Result(AiOperation.Status.RUNNING, "(" + held + "/" + this.update + ") " + message, "{}", List.of());
      }

      private AiOperation.Result init(AiOperation.Result result) {
         result = this.init_bb.init(result, this.requiresReconciliationAfterEmergency());
         this.init_Z = true;
         return result;
      }

      @Override
      public void cancel() {
         this.init_Z = true;
         this.init_bb.init_V(this.init_AiOperation);
      }

      @Override
      public boolean canInterruptNow() {
         return !this.init_bb.init_Z(this.init_AiOperation) && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.init_bb.init_Z(this.init_AiOperation);
      }
   }
}
