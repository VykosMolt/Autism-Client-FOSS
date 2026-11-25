package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.cd;
import autismclient.util.ce;
import autismclient.util.ch;
import autismclient.util.ck;
import autismclient.util.cn;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class UtilCore extends Module implements cn.Inner_DataUtil {
   private DataPipe.Inner_BufferHelper init_DataPipe_BufferHel;
   private final ValueSink init_ValueSink = new ValueSink();
   private BlockPos init_BlockPos;
   private boolean init_Z;
   private boolean load_Z;
   private BlockPos load_BlockPos;
   private BlockHitResult init_BlockHitResult;
   private boolean read_Z;
   private boolean write_Z;
   private int init_I = -2147483648;
   private int load_I = -2147483648;
   private int read_I = -1;
   private long init_J;
   private Object init_Obj;
   private String init_Str = "Idle";
   private int write_I = -1;
   private int update = -1;
   private int sync = -2147483648;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private Object load_Obj;

   public UtilCore() {
      super("bed-nuker", "BedNuker", ae.read, "Silently mine the quickest bed opening in reach");
      this.add(new BoolSetting("auto-tool", "Auto Tool", true).description("Use Auto Tool defaults independently; keep the held item when no tool is faster"));
      this.add(new BoolSetting("move-fix", "Move Fix", true).description("Walk where you look"));
      this.add(new BoolSetting("own-bed", "Own Bed", false).description("Allow mining your team's bed"));
   }

   @Override
   public String settingsHint() {
      return "Uses BedESP settings, even when disabled.";
   }

   @Override
   public boolean write_Z_10() {
      return this.bool("move-fix");
   }

   private int init_I_14() {
      return ck.init_ck().state_I();
   }

   public static UtilCore init_UtilCore() {
      return al.init_Module_Str("bed-nuker") instanceof UtilCore m && m.isEnabled() ? m : null;
   }

   static DataPipe.Inner_BufferHelper init_DataPipe_BufferHel_BlockPos(BlockPos pos) {
      UtilCore m = init_UtilCore();
      return m != null && pos.equals(m.init_BlockPos) ? m.init_DataPipe_BufferHel : null;
   }

   static ClassCache.Inner_DataUtil init_ClassCache_DataUti() {
      UtilCore m = init_UtilCore();
      if (m != null && m.init_DataPipe_BufferHel != null && m.init_BlockPos != null && MC.level != null && MC.player != null) {
         ClassCache.Inner_CachePool policy = ClassCache.init_ClassCache_CachePo();
         if (!policy.load() && !policy.read()) {
            return null;
         }

         BlockState state = MC.level.getBlockState(m.init_BlockPos);
         if (state.getBlock() instanceof BedBlock block) {
            BlockPos var7 = m.init_BlockPos.relative(BedBlock.getConnectedDirection(state));
            AABB box = new AABB(m.init_BlockPos);
            if (MC.level.getBlockState(var7).is(block)) {
               box = box.minmax(new AABB(var7));
            }

            box = box.setMaxY(m.init_BlockPos.getY() + 0.5625);
            List<BlockPos> cuts = m.init_DataPipe_BufferHel.init_List().stream().filter(c -> !c.init_Z()).map(DataPipe.Inner_DataUtil::init_BlockPos).toList();
            return new ClassCache.Inner_DataUtil(
               m.init_BlockPos,
               var7,
               block.getColor(),
               box,
               box.getCenter().add(0.0, 1.25, 0.0),
               List.of(),
               cuts,
               List.of(),
               m.init_DataPipe_BufferHel.init_D(),
               "",
               !cuts.isEmpty(),
               List.of(),
               MC.player.getEyePosition(),
               m.init_DataPipe_BufferHel.init_Vec3()
            );
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean init_Z_23() {
      UtilCore m = init_UtilCore();
      return m != null && m.init_I == m.init_I_14();
   }

   public static boolean read_Z_10() {
      UtilCore m = init_UtilCore();
      return m != null && m.init_Z && autismclient.util.aY.init_Z(m.id());
   }

   static ItemStack init_ItemStack() {
      if (MC.player == null) {
         return ItemStack.EMPTY;
      }

      if (al.init_Module_Str("bed-nuker") instanceof UtilCore m) {
         int slot = m.load_I_7();
         if (slot >= 0) {
            return MC.player.getInventory().getItem(slot);
         }
      }

      return MC.player.getMainHandItem();
   }

   static boolean sync() {
      return al.init_Module_Str("bed-nuker") instanceof UtilCore m && m.write_I >= 0 && m.load_I_7() >= 0;
   }

   static boolean flush() {
      if (al.init_Module_Str("bed-nuker") instanceof UtilCore m && !init_Z_23()) {
         int slot = m.load_I_7();
         if (slot >= 0 && !autismclient.util.ah.init(slot, m.id()) && autismclient.util.ah.write_Z(m.id())) {
            try {
               m.read_V_10();
               m.read_I = -1;
               autismclient.util.aw.init_V(MC, slot);
               m.init_V_20();
               return true;
            } finally {
               autismclient.util.ah.read_V(m.id());
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int load_I_7() {
      if (MC.player == null) {
         this.init_V_20();
         return -1;
      }

      int slot = init(
         MC.player.getInventory().getSelectedSlot(),
         this.write_I,
         this.update,
         this.load_Obj == MC.level,
         this.isEnabled(),
         (long)this.init_I_14() - this.sync,
         ItemStack.isSameItem(MC.player.getMainHandItem(), this.init_ItemStack)
      );
      if (slot < 0) {
         this.init_V_20();
      }

      return slot;
   }

   static int init(int selected, int original, int tool, boolean sameWorld, boolean enabled, long age, boolean sameTool) {
      return sameWorld
            && sameTool
            && original >= 0
            && original < 9
            && tool >= 0
            && tool < 9
            && original != tool
            && selected == tool
            && (enabled || age >= 0L && age <= 6L)
         ? original
         : -1;
   }

   private void init_V_20() {
      this.write_I = this.update = -1;
      this.sync = -2147483648;
      this.init_ItemStack = ItemStack.EMPTY;
      this.load_Obj = null;
   }

   public static boolean close() {
      UtilCore m = init_UtilCore();
      return m == null || !read_Z_10() || m.write_Z && m.init_ValueSink.init_Z(System.nanoTime());
   }

   public static void init_V_BlockPos_3(BlockPos pos) {
      UtilCore m = init_UtilCore();
      if (m != null && read_Z_10() && pos.equals(m.load_BlockPos)) {
         m.init_ValueSink.init_V(System.nanoTime());
      }
   }

   public static float init_F_F_BlockPos(float step, BlockPos pos) {
      UtilCore m = init_UtilCore();
      if (m != null && read_Z_10() && pos.equals(m.load_BlockPos)) {
         float damage = ((AutismMultiPlayerGameModeAccessor)MC.gameMode).autism$getDestroyProgress();
         return !m.read_Z || !m.write_Z && damage + step >= 1.0F ? 0.0F : m.init_ValueSink.init(step, damage, System.nanoTime());
      } else {
         return step;
      }
   }

   private boolean open() {
      return this.id().equals(autismclient.util.aD.init_Str());
   }

   @Override
   public boolean load_Z_23() {
      return !cn.init_Z() && (autismclient.util.aD.init_Z() || this.isEnabled() && this.init_I == this.init_I_14());
   }

   private boolean poll() {
      return MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !aD.init_Z()
         && !aC.init_Z()
         && !aC.write()
         && !cd.init_Z()
         && !autismclient.util.g.PoolEntry.init_Z()
         && !PkgCls_a.init_Z()
         && !MacroExecutor.write_Z()
         && !f.load()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !cn.init_Z()
         && !aI.update_Z_5()
         && !j.init_Z_49()
         && !z.sync_Z_12()
         && !ListPool.init_Z_10()
         && !aK.init_Z_27()
         && !BufferHelper.init_Z_2()
         && !t.init_Z_51()
         && !"auto-trap".equals(autismclient.util.aD.init_Str())
         && !FieldCache.init_Z_6()
         && !TableIndex.init_Z_20()
         && !HashUtil.load_Z()
         && !autismclient.util.DataPipe.init();
   }

   @Override
   public void preMovementTick() {
      this.load_Z = true;

      try {
         this.load_V_16();
      } finally {
         this.load_Z = false;
      }
   }

   private void load_V_16() {
      this.init_I = -2147483648;
      this.read_Z = this.write_Z = false;
      if (this.init_Obj != MC.level) {
         this.write_V_10();
         this.init_V_20();
         this.init_ValueSink.load();
         this.init_Obj = MC.level;
      }

      if (!this.poll()) {
         this.init_V_Str_Z("Paused", true);
      } else {
         if (this.load_I_7() >= 0) {
            this.sync = this.init_I_14();
         }

         af.init_V_Obj(MC.level);
         AutismMultiPlayerGameModeAccessor mining = (AutismMultiPlayerGameModeAccessor)MC.gameMode;
         BlockPos current = this.init_Z && mining.autism$isDestroying() ? mining.autism$getDestroyBlockPos() : null;
         float progress = current == null ? 0.0F : mining.autism$getDestroyProgress();
         DataPipe.Inner_BufferHelper best = null;
         BlockPos selected = null;
         DyeColor own = b.init();
         ClassCache.Inner_CachePool policy = ClassCache.init_ClassCache_CachePo();
         Vec3 nextEye = this.init_Vec3_2();
         StreamGate.Inner_StreamUtil tracking = null;
         if (current != null
            && current.equals(this.load_BlockPos)
            && this.init_DataPipe_BufferHel != null
            && this.init_BlockPos != null
            && MC.level.getBlockState(this.init_BlockPos).getBlock() instanceof BedBlock block
            && this.init_Z_BedBlock_DyeColor(block, own)) {
            tracking = StreamGate.init(current, this.init_DataPipe_BufferHel.init_Vec3(), progress, nextEye);
            if (tracking != null) {
               ArrayList<DataPipe.Inner_DataUtil> cells = new ArrayList<>();
               cells.add(tracking.init_DataPipe_DataUtil());

               for (DataPipe.Inner_DataUtil cell : this.init_DataPipe_BufferHel.init_List().subList(1, this.init_DataPipe_BufferHel.init_List().size())) {
                  BlockState state = MC.level.getBlockState(cell.init_BlockPos());
                  if (!state.isAir()) {
                     cells.add(
                        new DataPipe.Inner_DataUtil(
                           cell.init_BlockPos(),
                           cell.init_List(),
                           StreamGate.init(state, cell.init_BlockPos(), MC.player.getInventory().getItem(cell.init_I())),
                           cell.init_I(),
                           cell.init_Z()
                        )
                     );
                  }
               }

               AABB bounds = new AABB(this.init_BlockPos)
                  .minmax(new AABB(this.init_BlockPos.relative(BedBlock.getConnectedDirection(MC.level.getBlockState(this.init_BlockPos)))))
                  .setMaxY(this.init_BlockPos.getY() + 0.5625);
               boolean complete = bounds.distanceToSqr(MC.player.getEyePosition()) <= Math.pow(MC.player.blockInteractionRange(), 2.0);
               best = new DataPipe.Inner_BufferHelper(
                  List.copyOf(cells),
                  tracking.init_Vec3() == null ? this.init_DataPipe_BufferHel.init_Vec3() : tracking.init_Vec3(),
                  cells.stream().mapToDouble(DataPipe.Inner_DataUtil::init_D).sum() + Math.max(0, cells.size() - 1) * 5,
                  complete
               );
               selected = this.init_BlockPos;
            }
         }

         if (current != null && tracking == null) {
            this.read_V_10();
            current = null;
            progress = 0.0F;
         }

         if (best == null && (own != null || this.bool("own-bed"))) {
            int radius = (int)Math.ceil(MC.player.blockInteractionRange()) + 8;
            ArrayList<BlockPos> candidates = new ArrayList<>();
            BlockPos playerPos = MC.player.blockPosition();

            for (int cx = playerPos.getX() - radius >> 4; cx <= playerPos.getX() + radius >> 4; cx++) {
               for (int cz = playerPos.getZ() - radius >> 4; cz <= playerPos.getZ() + radius >> 4; cz++) {
                  LevelChunk chunk = MC.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                  if (chunk != null) {
                     for (af.Inner_CachePool entry : ClassCache.init_List_LevelChunk(chunk)) {
                        BlockPos p = BlockPos.containing(entry.init_Vec3());
                        if (!(new AABB(p).distanceToSqr(MC.player.getEyePosition()) > (double)radius * radius)) {
                           BlockState state = MC.level.getBlockState(p);
                           if (state.getBlock() instanceof BedBlock block
                              && this.init_Z_BedBlock_DyeColor(block, own)
                              && !(new AABB(p).distanceToSqr(MC.player.position()) > (double)policy.init_I() * policy.init_I())
                              && (
                                 state.getValue(BedBlock.PART) != BedPart.HEAD
                                    || !MC.level.getBlockState(p.relative(BedBlock.getConnectedDirection(state))).is(block)
                              )) {
                              candidates.add(p.immutable());
                           }
                        }
                     }
                  }
               }
            }

            candidates.sort(Comparator.comparingDouble(px -> px.distToCenterSqr(MC.player.position())));

            for (BlockPos p : candidates.stream().limit(8L).toList()) {
               DataPipe.Inner_BufferHelper candidate = StreamGate.load(p, this.bool("auto-tool"), current, progress);
               if (candidate != null
                  && (
                     best == null
                        || candidate.init_Z() && !best.init_Z()
                        || candidate.init_Z() == best.init_Z()
                           && (candidate.init_D() < best.init_D() - 0.5 || candidate.init_D() <= best.init_D() + 0.5 && p.equals(this.init_BlockPos))
                  )) {
                  best = candidate;
                  selected = p;
               }
            }
         }

         this.init_DataPipe_BufferHel = best;
         this.init_BlockPos = selected;
         if (this.init_DataPipe_BufferHel == null) {
            this.init_V_Str_Z("No reachable bed", true);
         } else {
            double budget = aa.init_D_9();
            boolean critical = MC.player.getHealth() + MC.player.getAbsorptionAmount() <= 6.0F;
            ce.Inner_DataUtil at = autismclient.util.aD.init_ce_DataUtil();
            StreamGate.Inner_StreamUtil follow = tracking == null
               ? StreamGate.init(this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_BlockPos(), this.init_DataPipe_BufferHel.init_Vec3(), 0.0F, nextEye)
               : tracking;
            Vec3 point = follow != null && follow.init_Vec3() != null ? follow.init_Vec3() : this.init_DataPipe_BufferHel.init_Vec3();
            ce.Inner_DataUtil goal = ce.init(point, nextEye);
            double turn = ce.init_F(at == null ? ce.init(MC.player) : at, goal);
            double finish = this.init_D_6()
               + Math.ceil(Math.max(0.0, turn - 2.0) / 20.0)
               + Math.max(this.init_ValueSink.load(System.nanoTime()), Math.max(0, mining.autism$getDestroyDelay()));
            if (this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_I() != MC.player.getInventory().getSelectedSlot()) {
               finish += 2.0;
            }

            if (!Double.isFinite(budget) || this.init_DataPipe_BufferHel.init_Z() && DataPipe.init(finish, budget, critical)) {
               this.init_I = this.init_I_14();
               this.init_Str = this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_Z() ? "Breaking bed" : "Opening bed";
               autismclient.util.aD.init_V_Str(this.id(), 12, goal);
               if (!this.open()) {
                  this.read_V_10();
               } else {
                  autismclient.util.aD.init(this.id(), MC.player, 37.0F, 37.0F);
                  int slot = this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_I();
                  if (slot != MC.player.getInventory().getSelectedSlot()) {
                     this.read_V_10();
                     if (this.read_I != slot) {
                        this.read_I = slot;
                        this.init_J = System.nanoTime() + 12000000L;
                     }

                     if (System.nanoTime() >= this.init_J && autismclient.util.ah.write_Z(this.id())) {
                        try {
                           if (this.load_I_7() < 0) {
                              this.write_I = MC.player.getInventory().getSelectedSlot();
                           }

                           autismclient.util.aw.init_V(MC, slot);
                           this.load_I = this.init_I_14();
                           this.update = slot;
                           this.init_ItemStack = MC.player.getMainHandItem().copy();
                           this.load_Obj = MC.level;
                           this.sync = this.init_I_14();
                        } finally {
                           autismclient.util.ah.read_V(this.id());
                        }
                     }
                  } else {
                     this.read_I = -1;
                  }

                  ch.Inner_CachePool wire = ch.init_ch_CachePool();
                  BlockHitResult hit = null;
                  if (wire.init_Z() && this.load_I != this.init_I_14() && slot == MC.player.getInventory().getSelectedSlot()) {
                     ce.Inner_DataUtil written = new ce.Inner_DataUtil(wire.read(), wire.write());
                     BlockHitResult before = this.init_BlockHitResult_ce_DataUtil_2(written);
                     this.write_Z = this.init_Z_BlockHitResult_2(before);
                     if (this.write_Z) {
                        hit = before;
                     }
                  }

                  this.read_Z = this.write_Z || tracking == null || tracking.init_Z();
                  double[] lastStep = autismclient.util.aD.init_DArr();
                  boolean continuing = current != null && this.init_Z && current.equals(this.load_BlockPos);
                  if (continuing && slot == MC.player.getInventory().getSelectedSlot()) {
                     BlockHitResult visible = this.init_BlockHitResult_ce_DataUtil_2(
                        ce.init(this.init_DataPipe_BufferHel.init_Vec3(), MC.player.getEyePosition())
                     );
                     if (this.init_Z_BlockHitResult_2(visible)) {
                        this.init_BlockHitResult = visible;
                     }

                     if (!autismclient.util.aY.init(this.id(), this.init_BlockHitResult)) {
                        this.read_V_10();
                     }
                  } else if (hit != null && lastStep[0] <= 18.0 && lastStep[1] <= 18.0 && autismclient.util.aY.init(this.id(), hit)) {
                     this.init_Z = true;
                     this.load_BlockPos = hit.getBlockPos().immutable();
                     this.init_BlockHitResult = hit;
                  } else {
                     this.read_V_10();
                  }
               }
            } else {
               this.init_V_Str_Z("Combat", false);
            }
         }
      }
   }

   private boolean init_Z_BedBlock_DyeColor(BedBlock block, DyeColor own) {
      return this.bool("own-bed") || own != null && block.getColor() != own;
   }

   private Vec3 init_Vec3_2() {
      if (!MC.player.isInWater() && !MC.player.isInLava() && !MC.player.getAbilities().flying && !MC.player.isFallFlying()) {
         autismclient.ai.a.NodeWalker.Inner_StreamUtil state = autismclient.ai.a.NodeWalker.Inner_StreamUtil.of(MC.player);
         autismclient.ai.a.NodeWalker.Inner_DataUtil input = new autismclient.ai.a.NodeWalker.Inner_DataUtil(
            (MC.options.keyUp.isDown() ? 1 : 0) - (MC.options.keyDown.isDown() ? 1 : 0),
            (MC.options.keyLeft.isDown() ? 1 : 0) - (MC.options.keyRight.isDown() ? 1 : 0),
            MC.options.keyJump.isDown(),
            MC.player.isSprinting(),
            MC.options.keyShift.isDown(),
            cn.init_F(MC.player)
         );
         return CacheEntry.init(
            state, input, autismclient.ai.a.NodeWalker.init_NodeWalker_CachePo_ClientLevel_Entity(MC.level, MC.player), MC.player.getEyeHeight()
         );
      } else {
         return MC.player.getEyePosition();
      }
   }

   private BlockHitResult init_BlockHitResult_ce_DataUtil_2(ce.Inner_DataUtil aim) {
      Vec3 eye = MC.player.getEyePosition();
      return MC.level
         .clip(
            new ClipContext(
               eye, eye.add(Vec3.directionFromRotation(aim.load(), aim.init()).scale(MC.player.blockInteractionRange())), Block.OUTLINE, Fluid.NONE, MC.player
            )
         );
   }

   private boolean init_Z_BlockHitResult_2(BlockHitResult hit) {
      return hit != null && hit.getType() == Type.BLOCK && this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_BlockPos().equals(hit.getBlockPos());
   }

   private void read_V_10() {
      if (this.init_Z) {
         this.init_ValueSink.init();
         autismclient.util.aY.init_V(this.id());
         if (this.load_Z) {
            autismclient.util.aY.init(this.load_BlockPos);
         } else {
            autismclient.util.aY.load(this.load_BlockPos);
         }
      }

      this.init_Z = false;
      this.load_BlockPos = null;
      this.init_BlockHitResult = null;
      this.write_Z = this.read_Z = false;
   }

   private void init_V_Str_Z(String reason, boolean pump) {
      this.read_V_10();
      this.read_I = -1;
      this.init_Str = reason;
      if (this.open()) {
         autismclient.util.aD.init(this.id());
         if (pump && MC.player != null) {
            autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
         }
      }
   }

   private void write_V_10() {
      this.read_V_10();
      this.read_I = -1;
      this.init_DataPipe_BufferHel = null;
      this.init_BlockPos = null;
      this.init_I = this.load_I = -2147483648;
   }

   @Override
   public void onDisable() {
      this.sync = this.init_I_14();
      this.write_V_10();
      StreamGate.init();
      this.init_V_Str_Z("Idle", false);
   }

   @Override
   public void onGameLeft() {
      this.write_V_10();
      this.init_V_20();
      StreamGate.init();
      this.init_ValueSink.load();
      if (this.open()) {
         autismclient.util.aD.load_V();
      }

      this.init_Obj = null;
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.open();
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && this.open() && MC.player != null) {
         autismclient.util.aD.init_V_Str_LocalPlayer(this.id(), MC.player);
      }
   }

   @Override
   public String info() {
      return this.init_Str;
   }

   public BlockPos init_BlockPos_4() {
      return this.init_DataPipe_BufferHel == null ? null : this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_BlockPos();
   }

   public String init_Str_6() {
      return this.init_BlockHitResult == null ? "" : this.init_BlockHitResult.getDirection().name();
   }

   public double init_D_6() {
      return this.init_DataPipe_BufferHel == null
         ? -1.0
         : this.init_DataPipe_BufferHel.init_D()
            + Math.max(0.0, this.init_ValueSink.init_D(System.nanoTime()) - this.init_DataPipe_BufferHel.init_DataPipe_DataUtil().init_D());
   }
}
