package autismclient.ai.b;

import autismclient.AutismClientAddon;
import autismclient.ai.PkgCls_b;
import autismclient.ai.aq;
import autismclient.ai.ca;
import autismclient.ai.a.NodeWalker;
import autismclient.ai.a.ValueSink;
import autismclient.ai.a.a;
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

public final class DataUtil implements EventBus {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int load_I = 65536;
   private static final int read_I = 4096;
   private final LongLinkedOpenHashSet init_LongLinkedOpenHash = new LongLinkedOpenHashSet();
   private final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private boolean init_Z;
   private long read_J;
   private BlockPos init_BlockPos;
   private boolean load_Z = true;
   private final RenderState init_RenderState = new RenderState();
   private final TableIndex init_TableIndex = new TableIndex();
   private boolean read_Z;
   private boolean write_Z;
   private Item init_Item;
   private String init_Str;
   private PkgCls_b.Inner_DataUtil init_b_DataUtil;
   private int write_I;
   private int update_I;
   private int sync;
   private String load_Str;
   private int flush = -2147483648;
   final DataUtil.Inner_DataUtil init_DataUtil_DataUtil = new DataUtil.Inner_DataUtil();
   static final int init_I = 65536;
   private final Long2ObjectLinkedOpenHashMap<BlockState> init_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   private int close;
   private static final boolean update_Z = Boolean.getBoolean("autism.ai.print.trace");

   @Override
   public void init_V_BlockPos_BlockState(BlockPos position, BlockState state) {
      a.init(position, state.getBlock());
   }

   @Override
   public void init_V_BlockPos(BlockPos position) {
      a.init_V(position);
   }

   @Override
   public boolean init_Z_BlockPos_Map(BlockPos position, Map<Integer, ItemStack> saved) {
      return false;
   }

   @Override
   public Object init_Obj() {
      return this.init_Minecraft == null ? null : this.init_Minecraft.level;
   }

   @Override
   public EventBus.Inner_StreamUtil init_EventBus_StreamUti() {
      return aq.load()
         ? EventBus.Inner_StreamUtil.load
         : this.init_TableIndex
            .init(
               this.init_Minecraft.level,
               this.init_Minecraft.player,
               autismclient.util.h.BlockChangeAckTracker.init_Z(this.init_Minecraft.level, this.init_TableIndex.init()),
               this.load_J()
            );
   }

   @Override
   public String init_Str_BlockPos(BlockPos cell) {
      if (this.init_Minecraft != null && this.init_Minecraft.level != null) {
         for (Entity entity : this.init_Minecraft.level.getEntities(this.init_LocalPlayer(), new AABB(cell).deflate(0.001))) {
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
   public List<RingBuffer.Inner_DataUtil> init_List_BlockPos(BlockPos cell) {
      return this.init_Minecraft != null
            && this.init_Minecraft.level != null
            && this.init_Minecraft.level.getChunkSource().hasChunk(cell.getX() >> 4, cell.getZ() >> 4)
         ? this.init_Minecraft
            .level
            .getEntitiesOfClass(ItemFrame.class, new AABB(cell).inflate(0.5))
            .stream()
            .filter(frame -> !frame.isRemoved() && frame.getPos().equals(cell))
            .map(
               frame -> new RingBuffer.Inner_DataUtil(
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
   public List<MethodUtil.Inner_DataUtil> load_List(BlockPos rail) {
      if (this.init_Minecraft != null && this.init_Minecraft.level != null) {
         for (int x = rail.getX() - 2 >> 4; x <= rail.getX() + 2 >> 4; x++) {
            for (int z = rail.getZ() - 2 >> 4; z <= rail.getZ() + 2 >> 4; z++) {
               if (!this.init_Minecraft.level.getChunkSource().hasChunk(x, z)) {
                  return null;
               }
            }
         }

         return this.init_Minecraft
            .level
            .getEntitiesOfClass(AbstractMinecart.class, new AABB(rail).inflate(2.0))
            .stream()
            .filter(entity -> !entity.isRemoved())
            .map(
               entity -> new MethodUtil.Inner_DataUtil(
                  entity.getUUID(), BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), entity.position(), entity.getBoundingBox()
               )
            )
            .toList();
      } else {
         return null;
      }
   }

   public void load_V(BlockPos pos) {
      synchronized (this.init_LongLinkedOpenHash) {
         if (this.init_LongLinkedOpenHash.size() < 65536) {
            this.init_LongLinkedOpenHash.add(pos.asLong());
         } else {
            this.init_V_J((long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L);
         }
      }
   }

   public void init_V_J(long key) {
      synchronized (this.init_LongLinkedOpenHash) {
         if (!this.init_Z) {
            if (this.init_LongOpenHashSet.size() < 4096) {
               this.init_LongOpenHashSet.add(key);
            } else {
               this.init_Z = true;
               this.init_LongOpenHashSet.clear();
            }
         }
      }
   }

   @Override
   public void init_V_LongConsumer(LongConsumer changed) {
      long[] drained;
      synchronized (this.init_LongLinkedOpenHash) {
         drained = this.init_Z ? new long[]{-9223372036854775808L} : this.init_LongOpenHashSet.toLongArray();
         this.init_Z = false;
         this.init_LongOpenHashSet.clear();
      }

      for (long key : drained) {
         changed.accept(key);
      }
   }

   public void init_V() {
      this.read_J++;
      this.init_Item = null;
      this.init_Str = null;
   }

   public Item init_Item() {
      return this.init_Item;
   }

   public boolean init_Z() {
      return this.init_LocalPlayer() != null && this.init_LocalPlayer().tickCount <= this.flush;
   }

   public void load_V() {
      this.flush = -2147483648;
   }

   public boolean init_Z_BlockPos(BlockPos pos) {
      return this.init_DataUtil_DataUtil.init_Z(pos);
   }

   private LocalPlayer init_LocalPlayer() {
      return this.init_Minecraft.player;
   }

   @Override
   public BlockState init_BlockState_BlockPos(BlockPos pos) {
      return this.init_Minecraft.level.getBlockState(pos);
   }

   @Override
   public BlockEntity init_BlockEntity_BlockPos(BlockPos pos) {
      return this.init_Minecraft.level.getBlockEntity(pos);
   }

   @Override
   public boolean load_Z(BlockPos pos) {
      return this.init_Minecraft.level.hasChunkAt(pos);
   }

   @Override
   public int init_I() {
      return this.init_Minecraft.level.getMinY();
   }

   @Override
   public int load_I() {
      return this.init_Minecraft.level.getMinY() + this.init_Minecraft.level.getHeight();
   }

   @Override
   public void load(LongConsumer changed) {
      this.init_V_EventBus_DataUtil((key, before) -> changed.accept(key));
   }

   @Override
   public void init_V_EventBus_DataUtil(EventBus.Inner_DataUtil changed) {
      this.init_Z_EventBus_DataUtil_I(changed, 2147483647);
   }

   @Override
   public boolean init_Z_EventBus_DataUtil_I(EventBus.Inner_DataUtil changed, int budget) {
      long[] drained;
      synchronized (this.init_LongLinkedOpenHash) {
         drained = new long[Math.min(this.init_LongLinkedOpenHash.size(), Math.max(0, budget))];

         for (int i = 0; i < drained.length; i++) {
            drained[i] = this.init_LongLinkedOpenHash.removeFirstLong();
         }
      }

      if (this.init_Minecraft.level == null) {
         return true;
      }

      for (long key : drained) {
         BlockPos pos = BlockPos.of(key);
         if (this.init_Minecraft.level.hasChunkAt(pos)) {
            BlockState now = this.init_Minecraft.level.getBlockState(pos);
            BlockState before = this.init_Long2ObjectLinkedO.putAndMoveToLast(key, now);
            BlockState prior = ca.init_BlockState_J(key);
            if (before == null && prior != null && prior != now) {
               before = prior;
            }

            if (this.init_Long2ObjectLinkedO.size() > 65536) {
               this.init_Long2ObjectLinkedO.removeFirst();
            }

            if (before == null || before != now || now.hasBlockEntity()) {
               changed.changed(key, before);
            }
         }
      }

      synchronized (this.init_LongLinkedOpenHash) {
         return this.init_LongLinkedOpenHash.isEmpty();
      }
   }

   @Override
   public NodeWalker.Inner_StreamUtil init_NodeWalker_StreamU() {
      return NodeWalker.Inner_StreamUtil.of(this.init_LocalPlayer());
   }

   @Override
   public NodeWalker.Inner_CachePool init_NodeWalker_CachePo() {
      return NodeWalker.init_NodeWalker_CachePo_ClientLevel_Entity(this.init_Minecraft.level, this.init_LocalPlayer());
   }

   @Override
   public float init_F() {
      return this.init_LocalPlayer().getYRot();
   }

   @Override
   public float load_F() {
      return this.init_LocalPlayer().getXRot();
   }

   @Override
   public double init_D() {
      return this.init_LocalPlayer().getEyeHeight();
   }

   @Override
   public float read_F() {
      return this.init_LocalPlayer().getHealth();
   }

   @Override
   public Vec3 init_Vec3() {
      ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
      return wire.init_Vec3() != null ? wire.init_Vec3() : this.init_LocalPlayer().position();
   }

   @Override
   public float write_F() {
      ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
      return wire.init_Z() && Float.isFinite(wire.read()) ? wire.read() : this.init_LocalPlayer().getYRot();
   }

   @Override
   public float update_F() {
      ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
      return wire.init_Z() && Float.isFinite(wire.write()) ? wire.write() : this.init_LocalPlayer().getXRot();
   }

   @Override
   public boolean load_Z() {
      return this.init_LocalPlayer().getLastSentInput().shift();
   }

   @Override
   public Vec3 load_Vec3() {
      return this.init_Vec3().add(0.0, this.init_LocalPlayer().getEyeHeight(), 0.0);
   }

   @Override
   public AABB init_AABB() {
      return this.init_LocalPlayer().getDimensions(this.init_LocalPlayer().getPose()).makeBoundingBox(this.init_Vec3());
   }

   @Override
   public ItemStack init_ItemStack_I(int index) {
      return this.init_LocalPlayer().getInventory().getItem(index);
   }

   @Override
   public int read_I() {
      return this.init_LocalPlayer().getInventory().getSelectedSlot();
   }

   @Override
   public boolean read_Z() {
      if (aq.load()) {
         return false;
      }

      if (this.load_Str != null) {
         return false;
      }

      if (this.init_b_DataUtil == null) {
         return true;
      }

      LocalPlayer p = this.init_LocalPlayer();
      PkgCls_b.Inner_StreamUtil state = this.init_b_DataUtil.load(p == null ? null : p.containerMenu, this.load_J());
      switch (state) {
         case init:
            return false;
         case write:
            this.load_Str = "The server did not confirm the builder's hand swap. Check the inventory and cursor before resuming.";
            return false;
         case load:
         case read:
            this.init_RenderState.init(this.write_I, this.update_I);
            p.getInventory().setSelectedSlot(this.update_I);
            this.init_b_DataUtil = null;
            this.sync = state == PkgCls_b.Inner_StreamUtil.load ? 0 : this.sync + 1;
            if (this.sync >= 3) {
               this.load_Str = "The server repeatedly changed the requested hand stack. Check the inventory before resuming.";
               return false;
            }

            return true;
         default:
            throw new IllegalStateException("Unknown hand swap state");
      }
   }

   public String init_Str() {
      return this.load_Str;
   }

   public boolean write_Z() {
      return this.init_b_DataUtil != null;
   }

   @Override
   public boolean update_Z() {
      return this.init_LocalPlayer() != null
         && this.init_Minecraft.gameMode != null
         && this.init_LocalPlayer().getAbilities().instabuild
         && this.init_Minecraft.gameMode.getPlayerMode() == GameType.CREATIVE;
   }

   @Override
   public boolean init_Z_ItemStack(ItemStack stack) {
      return autismclient.util.BlockBreakSpeedHelper.init_Z(this.init_Minecraft, stack);
   }

   public void init_V_Z(boolean value) {
      this.write_Z = value;
   }

   @Override
   public boolean sync_Z() {
      boolean allowed = this.init_LocalPlayer() != null
         && this.init_Minecraft.gameMode != null
         && ByteReader.init(
            this.write_Z,
            this.init_Minecraft.gameMode.getPlayerMode(),
            this.init_LocalPlayer().getAbilities().mayfly,
            this.init_LocalPlayer().getAbilities().instabuild
         );
      if (!allowed) {
         this.read_Z = false;
      }

      return allowed;
   }

   @Override
   public boolean flush() {
      return this.sync_Z() && !this.read_Z;
   }

   @Override
   public boolean close() {
      if (!this.flush()) {
         return false;
      }

      this.read_Z = true;
      LocalPlayer p = this.init_LocalPlayer();
      if (p.getAbilities().flying) {
         p.getAbilities().flying = false;
         p.onUpdateAbilities();
      }

      return true;
   }

   @Override
   public boolean open() {
      if (!this.read_Z) {
         return false;
      }

      this.read_Z = false;
      return this.flush();
   }

   @Override
   public boolean poll() {
      return this.init_LocalPlayer() != null && this.init_LocalPlayer().getAbilities().flying;
   }

   @Override
   public float sync_F() {
      return this.init_LocalPlayer() == null ? 0.05F : this.init_LocalPlayer().getAbilities().getFlyingSpeed();
   }

   @Override
   public boolean push() {
      LocalPlayer p = this.init_LocalPlayer();
      if (p != null && !p.isPassenger() && !p.isFallFlying() && !p.isMovingSlowly()) {
         AutismLocalPlayerAccessor nativePlayer = (AutismLocalPlayerAccessor)p;
         return nativePlayer.autism$isSprintingPossible(true) && !nativePlayer.autism$isSlowDueToUsingItem();
      } else {
         return false;
      }
   }

   @Override
   public String load_Str() {
      return this.init_Str;
   }

   @Override
   public void init_V_Item(Item item) {
      this.init_Item = item;
      LocalPlayer p = this.init_LocalPlayer();
      if (p != null
         && this.init_Minecraft.gameMode != null
         && p.getAbilities().instabuild
         && this.init_Minecraft.gameMode.getPlayerMode() == GameType.CREATIVE
         && this.init_Minecraft.gui.screen() == null
         && p.containerMenu == p.inventoryMenu) {
         for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).is(item)) {
               return;
            }
         }

         int slot = this.init_RenderState.load(i -> p.getInventory().getItem(i));
         if (slot < 0) {
            this.init_Str = "Free one inventory or offhand slot for automatic Creative materials";
         } else {
            ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, Math.min(64, item.getDefaultMaxStackSize()));
            this.init(p, slot, stack);
         }
      }
   }

   public boolean load(ItemStack stack) {
      return this.init_Z_ItemStack_Predicate(stack, held -> ItemStack.isSameItemSameComponents(stack, held));
   }

   public boolean init_Z_ItemStack_Predicate(ItemStack stack, Predicate<ItemStack> reserved) {
      LocalPlayer p = this.init_LocalPlayer();
      if (p != null && this.update_Z() && this.init_Minecraft.gui.screen() == null && p.containerMenu == p.inventoryMenu) {
         int slot = this.init_RenderState.init(i -> p.getInventory().getItem(i), reserved);
         if (slot < 0) {
            return false;
         }

         this.init(p, slot, stack.copy());
         return true;
      } else {
         return false;
      }
   }

   private void init(LocalPlayer p, int slot, ItemStack stack) {
      p.getInventory().setItem(slot, stack);
      this.init_Minecraft.gameMode.handleCreativeModeItemAdd(stack.copy(), init_I_I(slot));
      this.init_RenderState.init(slot, stack);
   }

   @Override
   public boolean pop() {
      return autismclient.ai.a.PacketUtil.init_Z();
   }

   @Override
   public BlockState init(Item item, BlockHitResult hit, float yaw, float pitch, boolean sneak, BlockPos at) {
      if (!(item instanceof BlockItem blockItem)) {
         return null;
      } else {
         ValueSink context = new ValueSink(this.init_LocalPlayer(), new ItemStack(item), hit, yaw, pitch, sneak);
         if (context.canPlace() && context.getClickedPos().equals(at)) {
            LocalPlayer p = this.init_LocalPlayer();
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

            if (update_Z && this.close++ < 400 && (state == null || !state.canSurvive(this.init_Minecraft.level, at))) {
               StringBuilder blockers = new StringBuilder();

               for (Entity entity : this.init_Minecraft.level.getEntities(null, new AABB(at))) {
                  blockers.append(' ')
                     .append(entity.getType().toShortString())
                     .append(entity.blocksBuilding ? "!" : "")
                     .append('@')
                     .append(entity.getBoundingBox());
               }

               this.init_V_Str(
                  "placement refused: state "
                     + state
                     + " for "
                     + item
                     + " at "
                     + at.toShortString()
                     + " on "
                     + this.init_Minecraft.level.getBlockState(hit.getBlockPos())
                     + " entities"
                     + blockers
               );
            }

            return state != null && state.canSurvive(this.init_Minecraft.level, at) ? state : null;
         } else {
            if (update_Z && this.close++ < 400) {
               this.init_V_Str(
                  "placement refused: canPlace "
                     + context.canPlace()
                     + " clicked "
                     + context.getClickedPos().toShortString()
                     + " wanted "
                     + at.toShortString()
                     + " on "
                     + this.init_Minecraft.level.getBlockState(hit.getBlockPos())
                     + " in "
                     + this.init_Minecraft.level.getBlockState(at)
               );
            }

            return null;
         }
      }
   }

   @Override
   public boolean init_Z_BlockState_BlockPos(BlockState state, BlockPos pos) {
      return state.canSurvive(this.init_Minecraft.level, pos);
   }

   @Override
   public void init_V_SyncManager(SyncManager c) {
      LocalPlayer p = this.init_LocalPlayer();
      if (p != null && this.init_Minecraft.gameMode != null) {
         if (aq.load()) {
            this.read_V();
         } else if (!ByteReader.init(c.close, this.flush(), p.getAbilities().flying)) {
            this.read_V();
         } else {
            ((AutismPlayerAccessor)p).autism$setJumpTriggerTime(0);
            if (c.close) {
               if (!p.getAbilities().flying) {
                  p.getAbilities().flying = true;
                  p.onUpdateAbilities();
               }

               if (!c.flush) {
                  p.setSprinting(false);
               }
            }

            Options o = this.init_Minecraft.options;
            o.keyUp.setDown(c.init_Z);
            o.keyDown.setDown(c.load_Z);
            o.keyLeft.setDown(c.read_Z);
            o.keyRight.setDown(c.write_Z);
            o.keyJump.setDown(c.update);
            o.keyShift.setDown(c.sync);
            o.keySprint.setDown(c.flush);
            this.load_Z = false;
            if (c.read_I >= 0 && c.write_I >= 0 && this.init_Minecraft.gui.screen() == null) {
               if (this.read_Z()) {
                  this.write_I = c.read_I;
                  this.update_I = c.write_I;
                  this.init_b_DataUtil = PkgCls_b.init(p.inventoryMenu, init_I_I(this.write_I), init_I_I(this.update_I), this.update_I);
                  if (this.init_b_DataUtil == null) {
                     this.load_Str = "The builder could not start a hand swap. Check the inventory and clear the cursor.";
                  }
               }
            } else {
               if (c.init_I >= 0 && c.init_I < 9) {
                  p.getInventory().setSelectedSlot(c.init_I);
               }

               if (c.init_BlockHitResult != null && this.init_Minecraft.gui.screen() == null) {
                  this.sync = 0;
                  this.flush = p.tickCount + 12;
                  BlockPos signPos = p.getMainHandItem().getItem() instanceof BlockItem item && item.getBlock() instanceof SignBlock
                     ? new BlockPlaceContext(p, InteractionHand.MAIN_HAND, p.getMainHandItem(), c.init_BlockHitResult).getClickedPos().immutable()
                     : null;
                  BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)this.init_Minecraft.level).autism$getBlockPredictionHandler();
                  int beforeSequence = prediction.currentSequence();
                  InteractionResult result = this.init_Minecraft.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, c.init_BlockHitResult);
                  if (c.open || !c.close) {
                     this.init_TableIndex.init(this.init_Minecraft.level, p, beforeSequence, prediction.currentSequence(), this.load_J());
                  }

                  if (signPos != null && result.consumesAction()) {
                     this.init_DataUtil_DataUtil.init_V(signPos);
                  }

                  if (result.consumesAction()) {
                     Compat263.swing(p, InteractionHand.MAIN_HAND);
                  }

                  if (result.consumesAction()
                     && c.load_I >= 0
                     && c.load_I < 9
                     && this.init_Minecraft.gui.screen() == null
                     && p.containerMenu == p.inventoryMenu
                     && !aq.load()) {
                     p.getInventory().setSelectedSlot(c.load_I);
                  }

                  BlockPos placed = c.init_BlockHitResult.getBlockPos().relative(c.init_BlockHitResult.getDirection());
                  this.load_V(placed);
                  this.load_V(c.init_BlockHitResult.getBlockPos());
               }

               if (c.init_UUID != null && this.init_Minecraft.gui.screen() == null) {
                  Entity target = null;

                  for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
                     if (entity.getUUID().equals(c.init_UUID)) {
                        target = entity;
                        break;
                     }
                  }

                  if (target != null && !target.isRemoved()) {
                     Vec3 at = target.getBoundingBox().getCenter();
                     InteractionResult result = this.init_Minecraft.gameMode.interact(p, target, new EntityHitResult(target, at), InteractionHand.MAIN_HAND);
                     if (result.consumesAction()) {
                        Compat263.swing(p, InteractionHand.MAIN_HAND);
                     }
                  }
               }

               if (c.poll && this.init_Minecraft.gui.screen() == null) {
                  InteractionResult result = this.init_Minecraft.gameMode.useItem(p, InteractionHand.MAIN_HAND);
                  if (result.consumesAction()) {
                     Compat263.swing(p, InteractionHand.MAIN_HAND);
                  }
               }

               if (!Float.isNaN(c.init_F)) {
                  p.setYRot(c.init_F);
                  p.setYHeadRot(c.init_F);
               }

               if (!Float.isNaN(c.load_F)) {
                  p.setXRot(Math.max(-90.0F, Math.min(90.0F, c.load_F)));
               }

               if (c.init_BlockPos != null && this.init_Minecraft.gui.screen() == null) {
                  BlockAttackGraceWindow.init(p);
                  if (!c.init_BlockPos.equals(this.init_BlockPos)) {
                     this.init_Minecraft.gameMode.startDestroyBlock(c.init_BlockPos, c.init_Direction);
                     this.init_BlockPos = c.init_BlockPos;
                  } else {
                     this.init_Minecraft.gameMode.continueDestroyBlock(c.init_BlockPos, c.init_Direction);
                  }

                  Compat263.swing(p, InteractionHand.MAIN_HAND);
                  if (Boolean.getBoolean("autism.ai.print.trace")
                     && this.init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor dig
                     && p.tickCount % 20 == 0) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] print: live dig {} progress={} delay={} destroying={} held={} block={}",
                           c.init_BlockPos.toShortString(),
                           dig.autism$getDestroyProgress(),
                           dig.autism$getDestroyDelay(),
                           dig.autism$isDestroying(),
                           p.getMainHandItem(),
                           this.init_Minecraft.level.getBlockState(c.init_BlockPos)
                        );
                  }

                  this.load_V(c.init_BlockPos);
               } else if (this.init_BlockPos != null) {
                  this.init_Minecraft.gameMode.stopDestroyBlock();
                  this.init_BlockPos = null;
               }
            }
         }
      }
   }

   static int init_I_I(int inventorySlot) {
      return inventorySlot == 40 ? 45 : (inventorySlot < 9 ? inventorySlot + 36 : inventorySlot);
   }

   public void read_V() {
      if (!this.load_Z && this.init_Minecraft.options != null) {
         Options o = this.init_Minecraft.options;
         o.keyUp.setDown(false);
         o.keyDown.setDown(false);
         o.keyLeft.setDown(false);
         o.keyRight.setDown(false);
         o.keyJump.setDown(false);
         o.keyShift.setDown(false);
         o.keySprint.setDown(false);
         if (this.init_BlockPos != null && this.init_Minecraft.gameMode != null) {
            this.init_Minecraft.gameMode.stopDestroyBlock();
         }

         this.init_BlockPos = null;
         this.load_Z = true;
      }
   }

   @Override
   public long init_J() {
      return this.read_J;
   }

   @Override
   public long load_J() {
      return System.nanoTime();
   }

   @Override
   public void init_V_Str(String message) {
      AutismClientAddon.LOG.info("[AI] print: {}", message);
   }

   static final class Inner_DataUtil {
      private final LinkedHashSet<Long> init = new LinkedHashSet<>();

      void init_V(BlockPos pos) {
         this.init.add(pos.asLong());

         while (this.init.size() > 64) {
            this.init.removeFirst();
         }
      }

      boolean init_Z(BlockPos pos) {
         return this.init.remove(pos.asLong());
      }
   }
}
