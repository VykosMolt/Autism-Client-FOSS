package autismclient.ai.print;

import autismclient.AutismClientAddon;
import autismclient.ai.AiContainerTransactions;
import autismclient.ai.AiMenuClose;
import autismclient.ai.AiServerEvidence;
import autismclient.ai.engine.AiPhysics;
import autismclient.ai.engine.AiPlacementContext;
import autismclient.ai.engine.AiScaffold;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismBlockItemAccessor;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.mixin.accessor.AutismPlayerAccessor;
import autismclient.util.BlockAttackGraceWindow;
import autismclient.util.ServerRotationTracker;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class LivePrintEnv implements PrintEnv {
   private final Minecraft mc = Minecraft.getInstance();
   private static final int MAX_BLOCK_CHANGE_QUEUE = 65536;
   private static final int MAX_CHUNK_CHANGE_QUEUE = 4096;
   private final LongLinkedOpenHashSet pendingBlockChanges = new LongLinkedOpenHashSet();
   private final LongOpenHashSet pendingChunkChanges = new LongOpenHashSet();
   private boolean chunkChangeOverflow;
   private long tickCounter;
   private BlockPos activeDigPos;
   private boolean controlsReleased = true;
   private final PrintCreativeStock creativeStock = new PrintCreativeStock();
   private final PrintInteractionReceipt interactionReceipt = new PrintInteractionReceipt();
   private boolean flightSuppressed;
   private boolean flightAllowedByConfig;
   private Item lastRequestedCreativeItem;
   private String creativeStockError;
   private AiContainerTransactions.TransactionReceipt pendingHandSwap;
   private int pendingSwapSourceSlot;
   private int pendingSwapHotbarSlot;
   private int mismatchedSwapCount;
   private String handSwapError;
   private int recentClickUntilTick = -2147483648;
   final LivePrintEnv.RecentSigns recentPlacedSigns = new LivePrintEnv.RecentSigns();
   static final int MAX_KNOWN_STATES = 65536;
   private final Long2ObjectLinkedOpenHashMap<BlockState> knownBlockStates = new Long2ObjectLinkedOpenHashMap<>();
   private int traceRefusalLogCount;
   private static final boolean TRACE_ENABLED = Boolean.getBoolean("autism.ai.print.trace");

   @Override
   public void recordPlacedBlock(BlockPos position, BlockState state) {
      AiScaffold.init(position, state.getBlock());
   }

   @Override
   public void clearPlacedBlock(BlockPos position) {
      AiScaffold.forget(position);
   }

   @Override
   public boolean preloadContainerSlots(BlockPos position, Map<Integer, ItemStack> saved) {
      return false;
   }

   @Override
   public Object worldToken() {
      return this.mc == null ? null : this.mc.level;
   }

   @Override
   public PrintEnv.ReceiptState interactionReceiptState() {
      return AiMenuClose.load()
         ? PrintEnv.ReceiptState.PENDING
         : this.interactionReceipt
            .init(
               this.mc.level,
               this.mc.player,
               autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(this.mc.level, this.interactionReceipt.init()),
               this.nanoTime()
            );
   }

   @Override
   public String blockingEntityName(BlockPos cell) {
      if (this.mc != null && this.mc.level != null) {
         for (Entity entity : this.mc.level.getEntities(this.player(), new AABB(cell).deflate(0.001))) {
            if (entity.blocksBuilding && !entity.isRemoved()) {
               return entity.getType().getDescription().getString();
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public List<PrintItemFrame.ObservedFrame> itemFramesAt(BlockPos cell) {
      return this.mc != null
            && this.mc.level != null
            && this.mc.level.getChunkSource().hasChunk(cell.getX() >> 4, cell.getZ() >> 4)
         ? this.mc
            .level
            .getEntitiesOfClass(ItemFrame.class, new AABB(cell).inflate(0.5))
            .stream()
            .filter(frame -> !frame.isRemoved() && frame.getPos().equals(cell))
            .map(
               frame -> new PrintItemFrame.ObservedFrame(
                  frame.getUUID(),
                  BuiltInRegistries.ENTITY_TYPE.getKey(frame.getType()).toString(),
                  frame.getDirection(),
                  frame.getItem().copy(),
                  frame.getRotation()
               )
            )
            .toList()
         : null;
   }

   @Override
   public List<PrintRailVehicle.Inner_DataUtil> nearbyMinecarts(BlockPos rail) {
      if (this.mc != null && this.mc.level != null) {
         for (int x = rail.getX() - 2 >> 4; x <= rail.getX() + 2 >> 4; x++) {
            for (int z = rail.getZ() - 2 >> 4; z <= rail.getZ() + 2 >> 4; z++) {
               if (!this.mc.level.getChunkSource().hasChunk(x, z)) {
                  return null;
               }
            }
         }

         return this.mc
            .level
            .getEntitiesOfClass(AbstractMinecart.class, new AABB(rail).inflate(2.0))
            .stream()
            .filter(entity -> !entity.isRemoved())
            .map(
               entity -> new PrintRailVehicle.Inner_DataUtil(
                  entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), entity.position(), entity.getBoundingBox()
               )
            )
            .toList();
      } else {
         return null;
      }
   }

   public void notifyBlockChanged(BlockPos pos) {
      synchronized (this.pendingBlockChanges) {
         if (this.pendingBlockChanges.size() < 65536) {
            this.pendingBlockChanges.add(pos.asLong());
         } else {
            this.notifyChunkChanged((long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L);
         }
      }
   }

   public void notifyChunkChanged(long key) {
      synchronized (this.pendingBlockChanges) {
         if (!this.chunkChangeOverflow) {
            if (this.pendingChunkChanges.size() < 4096) {
               this.pendingChunkChanges.add(key);
            } else {
               this.chunkChangeOverflow = true;
               this.pendingChunkChanges.clear();
            }
         }
      }
   }

   @Override
   public void drainChunkChanges(LongConsumer changed) {
      long[] drained;
      synchronized (this.pendingBlockChanges) {
         drained = this.chunkChangeOverflow ? new long[]{-9223372036854775808L} : this.pendingChunkChanges.toLongArray();
         this.chunkChangeOverflow = false;
         this.pendingChunkChanges.clear();
      }

      for (long key : drained) {
         changed.accept(key);
      }
   }

   public void advanceTick() {
      this.tickCounter++;
      this.lastRequestedCreativeItem = null;
      this.creativeStockError = null;
   }

   public Item lastRequestedCreativeItem() {
      return this.lastRequestedCreativeItem;
   }

   public boolean hasRecentClick() {
      return this.player() != null && this.player().tickCount <= this.recentClickUntilTick;
   }

   public void clearRecentClick() {
      this.recentClickUntilTick = -2147483648;
   }

   public boolean consumePlacedSign(BlockPos pos) {
      return this.recentPlacedSigns.consume(pos);
   }

   private LocalPlayer player() {
      return this.mc.player;
   }

   @Override
   public BlockState getBlockState(BlockPos pos) {
      return this.mc.level.getBlockState(pos);
   }

   @Override
   public BlockEntity getBlockEntity(BlockPos pos) {
      return this.mc.level.getBlockEntity(pos);
   }

   @Override
   public boolean hasChunkAt(BlockPos pos) {
      return this.mc.level.hasChunkAt(pos);
   }

   @Override
   public int getMinY() {
      return this.mc.level.getMinY();
   }

   @Override
   public int getMaxY() {
      return this.mc.level.getMinY() + this.mc.level.getHeight();
   }

   @Override
   public void drainBlockChanges(LongConsumer changed) {
      this.drainBlockChangesWithPrior((key, before) -> changed.accept(key));
   }

   @Override
   public void drainBlockChangesWithPrior(PrintEnv.BlockChangeSink changed) {
      this.drainBlockChangesBudgeted(changed, 2147483647);
   }

   @Override
   public boolean drainBlockChangesBudgeted(PrintEnv.BlockChangeSink changed, int budget) {
      long[] drained;
      synchronized (this.pendingBlockChanges) {
         drained = new long[Math.min(this.pendingBlockChanges.size(), Math.max(0, budget))];

         for (int i = 0; i < drained.length; i++) {
            drained[i] = this.pendingBlockChanges.removeFirstLong();
         }
      }

      if (this.mc.level == null) {
         return true;
      }

      for (long key : drained) {
         BlockPos pos = BlockPos.of(key);
         if (this.mc.level.hasChunkAt(pos)) {
            BlockState now = this.mc.level.getBlockState(pos);
            BlockState before = this.knownBlockStates.putAndMoveToLast(key, now);
            BlockState prior = AiServerEvidence.consumePreChangeState(key);
            if (before == null && prior != null && prior != now) {
               before = prior;
            }

            if (this.knownBlockStates.size() > 65536) {
               this.knownBlockStates.removeFirst();
            }

            if (before == null || before != now || now.hasBlockEntity()) {
               changed.changed(key, before);
            }
         }
      }

      synchronized (this.pendingBlockChanges) {
         return this.pendingBlockChanges.isEmpty();
      }
   }

   @Override
   public AiPhysics.MotionState playerMotionState() {
      return AiPhysics.MotionState.of(this.player());
   }

   @Override
   public AiPhysics.PhysicsWorld livePhysicsWorld() {
      return AiPhysics.createLiveWorld(this.mc.level, this.player());
   }

   @Override
   public float playerYaw() {
      return this.player().getYRot();
   }

   @Override
   public float playerPitch() {
      return this.player().getXRot();
   }

   @Override
   public double playerEyeHeight() {
      return this.player().getEyeHeight();
   }

   @Override
   public float playerHealth() {
      return this.player().getHealth();
   }

   @Override
   public Vec3 serverFeetPos() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      return wire.position() != null ? wire.position() : this.player().position();
   }

   @Override
   public float serverYaw() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      return wire.initialized() && Float.isFinite(wire.currentYaw()) ? wire.currentYaw() : this.player().getYRot();
   }

   @Override
   public float serverPitch() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      return wire.initialized() && Float.isFinite(wire.currentPitch()) ? wire.currentPitch() : this.player().getXRot();
   }

   @Override
   public boolean isServerSneaking() {
      return this.player().getLastSentInput().shift();
   }

   @Override
   public Vec3 serverEyePos() {
      return this.serverFeetPos().add(0.0, this.player().getEyeHeight(), 0.0);
   }

   @Override
   public AABB serverPlayerBox() {
      return this.player().getDimensions(this.player().getPose()).makeBoundingBox(this.serverFeetPos());
   }

   @Override
   public ItemStack getInventoryItem(int index) {
      return this.player().getInventory().getItem(index);
   }

   @Override
   public int getSelectedHotbarSlot() {
      return this.player().getInventory().getSelectedSlot();
   }

   @Override
   public boolean isHandReady() {
      if (AiMenuClose.load()) {
         return false;
      }

      if (this.handSwapError != null) {
         return false;
      }

      if (this.pendingHandSwap == null) {
         return true;
      }

      LocalPlayer p = this.player();
      AiContainerTransactions.ReceiptState state = this.pendingHandSwap.pollAndResync(p == null ? null : p.containerMenu, this.nanoTime());
      switch (state) {
         case WAITING:
            return false;
         case UNCERTAIN:
            this.handSwapError = "The server did not confirm the builder's hand swap. Check the inventory and cursor before resuming.";
            return false;
         case CONFIRMED:
         case SWAPPED_DIFFERENT_STACK:
            this.creativeStock.init(this.pendingSwapSourceSlot, this.pendingSwapHotbarSlot);
            p.getInventory().setSelectedSlot(this.pendingSwapHotbarSlot);
            this.pendingHandSwap = null;
            this.mismatchedSwapCount = state == AiContainerTransactions.ReceiptState.CONFIRMED ? 0 : this.mismatchedSwapCount + 1;
            if (this.mismatchedSwapCount >= 3) {
               this.handSwapError = "The server repeatedly changed the requested hand stack. Check the inventory before resuming.";
               return false;
            }

            return true;
         default:
            throw new IllegalStateException("Unknown hand swap state");
      }
   }

   public String handSwapError() {
      return this.handSwapError;
   }

   public boolean hasPendingHandSwap() {
      return this.pendingHandSwap != null;
   }

   @Override
   public boolean isCreativeMode() {
      return this.player() != null
         && this.mc.gameMode != null
         && this.player().getAbilities().instabuild
         && this.mc.gameMode.getPlayerMode() == GameType.CREATIVE;
   }

   @Override
   public boolean isInstantBreakTool(ItemStack stack) {
      return autismclient.util.BlockBreakSpeedHelper.hasSilkTouch(this.mc, stack);
   }

   public void setFlightAllowed(boolean value) {
      this.flightAllowedByConfig = value;
   }

   @Override
   public boolean isFlightPermitted() {
      boolean allowed = this.player() != null
         && this.mc.gameMode != null
         && PrintFlightPermission.init(
            this.flightAllowedByConfig,
            this.mc.gameMode.getPlayerMode(),
            this.player().getAbilities().mayfly,
            this.player().getAbilities().instabuild
         );
      if (!allowed) {
         this.flightSuppressed = false;
      }

      return allowed;
   }

   @Override
   public boolean canUseFlight() {
      return this.isFlightPermitted() && !this.flightSuppressed;
   }

   @Override
   public boolean suppressFlight() {
      if (!this.canUseFlight()) {
         return false;
      }

      this.flightSuppressed = true;
      LocalPlayer p = this.player();
      if (p.getAbilities().flying) {
         p.getAbilities().flying = false;
         p.onUpdateAbilities();
      }

      return true;
   }

   @Override
   public boolean unsuppressFlight() {
      if (!this.flightSuppressed) {
         return false;
      }

      this.flightSuppressed = false;
      return this.canUseFlight();
   }

   @Override
   public boolean isFlying() {
      return this.player() != null && this.player().getAbilities().flying;
   }

   @Override
   public float flyingSpeed() {
      return this.player() == null ? 0.05F : this.player().getAbilities().getFlyingSpeed();
   }

   @Override
   public boolean canSprint() {
      LocalPlayer p = this.player();
      if (p != null && !p.isPassenger() && !p.isFallFlying() && !p.isMovingSlowly()) {
         AutismLocalPlayerAccessor nativePlayer = (AutismLocalPlayerAccessor)p;
         return nativePlayer.autism$isSprintingPossible(true) && !nativePlayer.autism$isSlowDueToUsingItem();
      } else {
         return false;
      }
   }

   @Override
   public String creativeStockError() {
      return this.creativeStockError;
   }

   @Override
   public void requestCreativeItem(Item item) {
      this.lastRequestedCreativeItem = item;
      LocalPlayer p = this.player();
      if (p != null
         && this.mc.gameMode != null
         && p.getAbilities().instabuild
         && this.mc.gameMode.getPlayerMode() == GameType.CREATIVE
         && this.mc.gui.screen() == null
         && p.containerMenu == p.inventoryMenu) {
         for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(item)) {
               return;
            }
         }

         int slot = this.creativeStock.load(i -> p.getInventory().getItem(i));
         if (slot < 0) {
            this.creativeStockError = "Free one inventory or offhand slot for automatic Creative materials";
         } else {
            ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, Math.min(64, item.getDefaultMaxStackSize()));
            this.setCreativeSlot(p, slot, stack);
         }
      }
   }

   public boolean giveCreativeStack(ItemStack stack) {
      return this.giveCreativeStackReserved(stack, held -> ItemStack.isSameItemSameComponents(stack, held));
   }

   public boolean giveCreativeStackReserved(ItemStack stack, Predicate<ItemStack> reserved) {
      LocalPlayer p = this.player();
      if (p != null && this.isCreativeMode() && this.mc.gui.screen() == null && p.containerMenu == p.inventoryMenu) {
         int slot = this.creativeStock.init(i -> p.getInventory().getItem(i), reserved);
         if (slot < 0) {
            return false;
         }

         this.setCreativeSlot(p, slot, stack.copy());
         return true;
      } else {
         return false;
      }
   }

   private void setCreativeSlot(LocalPlayer p, int slot, ItemStack stack) {
      p.getInventory().setItem(slot, stack);
      this.mc.gameMode.handleCreativeModeItemAdd(stack.copy(), toInventoryMenuSlot(slot));
      this.creativeStock.init(slot, stack);
   }

   @Override
   public boolean isFallGuardActive() {
      return autismclient.ai.engine.AiFallGuard.isHolding();
   }

   @Override
   public BlockState predictPlacedState(Item item, BlockHitResult hit, float yaw, float pitch, boolean sneak, BlockPos at) {
      if (!(item instanceof BlockItem blockItem)) {
         return null;
      } else {
         AiPlacementContext context = new AiPlacementContext(this.player(), new ItemStack(item), hit, yaw, pitch, sneak);
         if (context.canPlace() && context.getClickedPos().equals(at)) {
            LocalPlayer p = this.player();
            AABB box = p == null ? null : p.getBoundingBox();
            if (p != null) {
               p.setBoundingBox(
                  new AABB(at.getX() + 10000.0, -10000.0, at.getZ() + 10000.0, at.getX() + 10000.0 + 0.001, -9999.999, at.getZ() + 10000.0 + 0.001)
               );
            }

            BlockState state;
            try {
               state = ((AutismBlockItemAccessor)blockItem).autism$placementState(context);
            } finally {
               if (p != null) {
                  p.setBoundingBox(box);
               }
            }

            if (TRACE_ENABLED && this.traceRefusalLogCount++ < 400 && (state == null || !state.canSurvive(this.mc.level, at))) {
               StringBuilder blockers = new StringBuilder();

               for (Entity entity : this.mc.level.getEntities(null, new AABB(at))) {
                  blockers.append(' ')
                     .append(entity.getType().toShortString())
                     .append(entity.blocksBuilding ? "!" : "")
                     .append('@')
                     .append(entity.getBoundingBox());
               }

               this.logTrace(
                  "placement refused: state "
                     + state
                     + " for "
                     + item
                     + " at "
                     + at.toShortString()
                     + " on "
                     + this.mc.level.getBlockState(hit.getBlockPos())
                     + " entities"
                     + blockers
               );
            }

            return state != null && state.canSurvive(this.mc.level, at) ? state : null;
         } else {
            if (TRACE_ENABLED && this.traceRefusalLogCount++ < 400) {
               this.logTrace(
                  "placement refused: canPlace "
                     + context.canPlace()
                     + " clicked "
                     + context.getClickedPos().toShortString()
                     + " wanted "
                     + at.toShortString()
                     + " on "
                     + this.mc.level.getBlockState(hit.getBlockPos())
                     + " in "
                     + this.mc.level.getBlockState(at)
               );
            }

            return null;
         }
      }
   }

   @Override
   public boolean canBlockSurvive(BlockState state, BlockPos pos) {
      return state.canSurvive(this.mc.level, pos);
   }

   @Override
   public void applyControls(PrintControls c) {
      LocalPlayer p = this.player();
      if (p != null && this.mc.gameMode != null) {
         if (AiMenuClose.load()) {
            this.releaseControls();
         } else if (!PrintFlightPermission.init(c.fly, this.canUseFlight(), p.getAbilities().flying)) {
            this.releaseControls();
         } else {
            ((AutismPlayerAccessor)p).autism$setJumpTriggerTime(0);
            if (c.fly) {
               if (!p.getAbilities().flying) {
                  p.getAbilities().flying = true;
                  p.onUpdateAbilities();
               }

               if (!c.sprint) {
                  p.setSprinting(false);
               }
            }

            Options o = this.mc.options;
            o.keyUp.setDown(c.forward);
            o.keyDown.setDown(c.back);
            o.keyLeft.setDown(c.left);
            o.keyRight.setDown(c.right);
            o.keyJump.setDown(c.jump);
            o.keyShift.setDown(c.sneak);
            o.keySprint.setDown(c.sprint);
            this.controlsReleased = false;
            if (c.swapSourceSlot >= 0 && c.swapHotbarSlot >= 0 && this.mc.gui.screen() == null) {
               if (this.isHandReady()) {
                  this.pendingSwapSourceSlot = c.swapSourceSlot;
                  this.pendingSwapHotbarSlot = c.swapHotbarSlot;
                  this.pendingHandSwap = AiContainerTransactions.sendVerifiedSwap(p.inventoryMenu, toInventoryMenuSlot(this.pendingSwapSourceSlot), toInventoryMenuSlot(this.pendingSwapHotbarSlot), this.pendingSwapHotbarSlot);
                  if (this.pendingHandSwap == null) {
                     this.handSwapError = "The builder could not start a hand swap. Check the inventory and clear the cursor.";
                  }
               }
            } else {
               if (c.selectHotbarSlot >= 0 && c.selectHotbarSlot < 9) {
                  p.getInventory().setSelectedSlot(c.selectHotbarSlot);
               }

               if (c.clickHit != null && this.mc.gui.screen() == null) {
                  this.mismatchedSwapCount = 0;
                  this.recentClickUntilTick = p.tickCount + 12;
                  BlockPos signPos = p.getMainHandItem().getItem() instanceof BlockItem item && item.getBlock() instanceof SignBlock
                     ? new BlockPlaceContext(p, InteractionHand.MAIN_HAND, p.getMainHandItem(), c.clickHit).getClickedPos().immutable()
                     : null;
                  BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)this.mc.level).autism$getBlockPredictionHandler();
                  int beforeSequence = prediction.currentSequence();
                  InteractionResult result = this.mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, c.clickHit);
                  if (c.trackInteractionReceipt || !c.fly) {
                     this.interactionReceipt.init(this.mc.level, p, beforeSequence, prediction.currentSequence(), this.nanoTime());
                  }

                  if (signPos != null && result.consumesAction()) {
                     this.recentPlacedSigns.record(signPos);
                  }

                  if (result.consumesAction()) {
                     Compat263.swing(p, InteractionHand.MAIN_HAND);
                  }

                  if (result.consumesAction()
                     && c.postClickHotbarSlot >= 0
                     && c.postClickHotbarSlot < 9
                     && this.mc.gui.screen() == null
                     && p.containerMenu == p.inventoryMenu
                     && !AiMenuClose.load()) {
                     p.getInventory().setSelectedSlot(c.postClickHotbarSlot);
                  }

                  BlockPos placed = c.clickHit.getBlockPos().relative(c.clickHit.getDirection());
                  this.notifyBlockChanged(placed);
                  this.notifyBlockChanged(c.clickHit.getBlockPos());
               }

               if (c.interactEntityUuid != null && this.mc.gui.screen() == null) {
                  Entity target = null;

                  for (Entity entity : this.mc.level.entitiesForRendering()) {
                     if (entity.getUUID().equals(c.interactEntityUuid)) {
                        target = entity;
                        break;
                     }
                  }

                  if (target != null && !target.isRemoved()) {
                     Vec3 at = target.getBoundingBox().getCenter();
                     InteractionResult result = this.mc.gameMode.interact(p, target, new EntityHitResult(target, at), InteractionHand.MAIN_HAND);
                     if (result.consumesAction()) {
                        Compat263.swing(p, InteractionHand.MAIN_HAND);
                     }
                  }
               }

               if (c.useHeldItem && this.mc.gui.screen() == null) {
                  InteractionResult result = this.mc.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                  if (result.consumesAction()) {
                     Compat263.swing(p, InteractionHand.MAIN_HAND);
                  }
               }

               if (!Float.isNaN(c.yaw)) {
                  p.setYRot(c.yaw);
                  p.setYHeadRot(c.yaw);
               }

               if (!Float.isNaN(c.pitch)) {
                  p.setXRot(Math.max(-90.0F, Math.min(90.0F, c.pitch)));
               }

               if (c.digPos != null && this.mc.gui.screen() == null) {
                  BlockAttackGraceWindow.init(p);
                  if (!c.digPos.equals(this.activeDigPos)) {
                     this.mc.gameMode.startDestroyBlock(c.digPos, c.digFace);
                     this.activeDigPos = c.digPos;
                  } else {
                     this.mc.gameMode.continueDestroyBlock(c.digPos, c.digFace);
                  }

                  Compat263.swing(p, InteractionHand.MAIN_HAND);
                  if (Boolean.getBoolean("autism.ai.print.trace")
                     && this.mc.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
                     && p.tickCount % 20 == 0) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] print: live dig {} progress={} delay={} destroying={} held={} block={}",
                           c.digPos.toShortString(),
                           dig.autism$getDestroyProgress(),
                           dig.autism$getDestroyDelay(),
                           dig.autism$isDestroying(),
                           p.getMainHandItem(),
                           this.mc.level.getBlockState(c.digPos)
                        );
                  }

                  this.notifyBlockChanged(c.digPos);
               } else if (this.activeDigPos != null) {
                  this.mc.gameMode.stopDestroyBlock();
                  this.activeDigPos = null;
               }
            }
         }
      }
   }

   static int toInventoryMenuSlot(int inventorySlot) {
      return inventorySlot == 40 ? 45 : (inventorySlot < 9 ? inventorySlot + 36 : inventorySlot);
   }

   public void releaseControls() {
      if (!this.controlsReleased && this.mc.options != null) {
         Options o = this.mc.options;
         o.keyUp.setDown(false);
         o.keyDown.setDown(false);
         o.keyLeft.setDown(false);
         o.keyRight.setDown(false);
         o.keyJump.setDown(false);
         o.keyShift.setDown(false);
         o.keySprint.setDown(false);
         if (this.activeDigPos != null && this.mc.gameMode != null) {
            this.mc.gameMode.stopDestroyBlock();
         }

         this.activeDigPos = null;
         this.controlsReleased = true;
      }
   }

   @Override
   public long currentTick() {
      return this.tickCounter;
   }

   @Override
   public long nanoTime() {
      return System.nanoTime();
   }

   @Override
   public void logTrace(String message) {
      AutismClientAddon.LOG.info("[AI] print: {}", message);
   }

   static final class RecentSigns {
      private final LinkedHashSet<Long> signPositions = new LinkedHashSet<>();

      void record(BlockPos pos) {
         this.signPositions.add(pos.asLong());

         while (this.signPositions.size() > 64) {
            this.signPositions.removeFirst();
         }
      }

      boolean consume(BlockPos pos) {
         return this.signPositions.remove(pos.asLong());
      }
   }
}
