package autismclient.util;

import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import autismclient.modules.Module;
import autismclient.modules.f;
import autismclient.util.macro.MacroExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class ListPool {
   private static final int init_I = 20;
   private static final long init_J = 1250000000L;
   private static LocalPlayer init_LocalPlayer;
   private static ClientPacketListener init_ClientPacketListen;
   private static ClientLevel init_ClientLevel;
   private static ce.Inner_DataUtil init_ce_DataUtil;
   private static ItemStack init_ItemStack = ItemStack.EMPTY;
   private static ItemStack load_ItemStack = ItemStack.EMPTY;
   private static int load_I;
   private static int read_I;
   private static int write_I = -2147483648;
   private static int update_I = -2147483648;
   private static int sync_I;
   private static boolean init_Z;
   private static boolean load_Z;
   private static boolean read_Z;
   private static boolean write_Z;
   private static boolean update_Z;
   private static boolean sync_Z;
   private static boolean flush;
   private static boolean close;
   private static HitResult init_HitResult;
   private static BlockState init_BlockState;
   private static Vec3 init_Vec3;
   private static ce.Inner_DataUtil load_ce_DataUtil;
   private static ce.Inner_DataUtil read_ce_DataUtil;
   private static long load_J;
   private static final q.Inner_DataUtil init_q_DataUtil = new q.Inner_DataUtil();

   private static boolean close() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && "Legit".equals(aura.value("interactions"));
   }

   public static boolean init_Z() {
      return sync_Z && !read();
   }

   private ListPool() {
   }

   private static boolean open() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && aura.isEnabled() && "kill-aura".equals(aD.init_Str()) && aD.read_Z();
   }

   public static boolean load() {
      Module aura = autismclient.modules.al.init_Module_Str("kill-aura");
      return aura != null && aura.isEnabled() && "Block".equals(aura.value("interactions"));
   }

   private static boolean load_Z(Minecraft mc) {
      return mc.player != null
         && mc.level != null
         && mc.gameMode != null
         && mc.getConnection() != null
         && !mc.gameMode.isDestroying()
         && !ai.init(mc)
         && mc.getCameraEntity() == mc.player
         && !mc.player.isDeadOrDying()
         && !mc.player.isSpectator()
         && mc.gui.screen() == null
         && mc.gui.overlay() == null
         && !autismclient.modules.aD.init_Z()
         && !autismclient.modules.FreecamController.init_Z()
         && !cd.init_Z()
         && !MacroExecutor.write_Z()
         && (AutismLiteVariant.enabled() || !autismclient.util.g.PkgCls_a.init_Z())
         && !f.load()
         && !cn.init_Z()
         && !autismclient.modules.BlinkPacketManager.init_Z()
         && !autismclient.modules.AutoTotemModule.init_Z_6()
         && !autismclient.modules.AutoArmorModule.init_Z_20();
   }

   private static boolean read_Z(Minecraft mc) {
      return mc.options != null && aB.init(mc.options.keyUse).autism$physicalForBody();
   }

   private static AutismKeyMappingAccessor init_AutismKeyMappingAc(Minecraft mc) {
      return (AutismKeyMappingAccessor)mc.options.keyUse;
   }

   public static boolean read() {
      Minecraft mc = Minecraft.getInstance();
      return mc.options != null && init_AutismKeyMappingAc(mc).autism$clickCount() > 0 && !s.flush_Z();
   }

   public static boolean write() {
      Minecraft mc = Minecraft.getInstance();
      int tick = ck.init_ck().state_I();
      if (open() && load_Z(mc) && !load()) {
         if (init_LocalPlayer != null && (init_LocalPlayer != mc.player || init_ClientPacketListen != mc.getConnection() || init_ClientLevel != mc.level)) {
            init_V();
         }

         if (autismclient.modules.KillAuraModule.init_Z_31()) {
            init_V();
            return false;
         }

         boolean pressed = read_Z(mc);
         boolean edge = read();
         if (sync_Z) {
            if (pressed && !edge) {
               return false;
            }

            init_V();
         }

         if (edge && !init_Z) {
            init_Z = true;
            sync_I = tick;
         }

         if (init_Z && tick - sync_I > 20) {
            update_V(mc);
            return false;
         }

         if (pressed || init_Z || load_Z && mc.player.isUsingItem()) {
            if (load_Z && !write_Z(mc) && !pressed) {
               update_V(mc);
               return false;
            }

            init_LocalPlayer = mc.player;
            init_ClientPacketListen = mc.getConnection();
            init_ClientLevel = mc.level;
            load_Z = true;
            write_I = tick;
            autismclient.modules.KillAuraModule.init_V_27();
            if (!init_LocalPlayer.isUsingItem() || pressed) {
               update_Z = false;
            } else if (!update_Z) {
               update_Z = true;
               init_ce_DataUtil = null;
               init_HitResult = null;
            }

            if (init_ce_DataUtil == null || !write_Z(mc) || flush && !init_LocalPlayer.isUsingItem()) {
               if (close()) {
                  read_V(mc);
               } else {
                  read_ce_DataUtil = init_ce_DataUtil(mc);
                  read_I = tick;
               }

               load_I = init_LocalPlayer.getInventory().getSelectedSlot();
               init_ItemStack = init_LocalPlayer.getMainHandItem().copy();
               load_ItemStack = init_LocalPlayer.getOffhandItem().copy();
               flush = false;
            }

            if (close()) {
               if (!flush && !update_Z(mc) || update_Z && init(tick) || !init_LocalPlayer.isUsingItem() && init(tick)) {
                  update_V(mc);
                  return false;
               }

               if (!flush && !update_Z && ce.init_F(load_ce_DataUtil, ce.init(init_LocalPlayer)) > 8.0F) {
                  update_V(mc);
                  return false;
               }

               if (init_LocalPlayer.isUsingItem() && !update_Z) {
                  load_ce_DataUtil = ce.init(init_LocalPlayer);
                  init_HitResult = null;
                  read_ce_DataUtil = load_ce_DataUtil;
               } else if (init_HitResult != null && init_HitResult.getType() != Type.MISS) {
                  read_ce_DataUtil = ce.init(init_Vec3(), init_LocalPlayer.getEyePosition());
               }

               ch.Inner_CachePool wire = ch.init_ch_CachePool();
               float yaw = ch.init_F();
               ce.Inner_DataUtil written = wire.init_Z() ? new ce.Inner_DataUtil(Float.isFinite(yaw) ? yaw : wire.read(), wire.write()) : null;
               boolean ready = tick > read_I && written != null && init_Z(mc, written);
               write_V(mc);
               if (!autismclient.modules.KillAuraModule.init_Z_ce_DataUtil(ready ? written : read_ce_DataUtil, init_q_DataUtil, ready)) {
                  init_V();
                  return false;
               }

               if (!ready) {
                  close = false;
               }
            } else {
               if (!update_Z && init_ce_DataUtil != null && !load(mc, init_ce_DataUtil)) {
                  if (!pressed && !init_LocalPlayer.isUsingItem()) {
                     update_V(mc);
                     return false;
                  }

                  read_ce_DataUtil = init_ce_DataUtil(mc);
                  read_I = tick;
               }

               if (update_Z && tick - read_I > 20) {
                  update_V(mc);
                  return false;
               }

               if (!aD.init_Z(read_ce_DataUtil)) {
                  init_V();
                  return false;
               }
            }

            init_ce_DataUtil = aD.init_ce_DataUtil();
            return true;
         } else {
            init_V();
            return false;
         }
      } else {
         init_V();
         return false;
      }
   }

   public static boolean update() {
      Minecraft mc = Minecraft.getInstance();
      return load_Z
         && init_LocalPlayer == mc.player
         && init_ClientPacketListen == mc.getConnection()
         && init_ClientLevel == mc.level
         && write_I == ck.init_ck().state_I()
         && open();
   }

   public static boolean sync() {
      return open();
   }

   private static boolean poll() {
      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      return init_ce_DataUtil != null
         && wire.init_Z()
         && Math.abs(ce.init(wire.read(), init_ce_DataUtil.init())) < 0.001F
         && Math.abs(wire.write() - init_ce_DataUtil.load()) < 0.001F
         && ck.init_ck().state_I() > read_I;
   }

   public static void init_V(Minecraft mc) {
      if (mc.options != null && open() && load_Z(mc)) {
         if (autismclient.modules.KillAuraModule.init_Z_31()) {
            sync_Z = false;
         } else {
            AutismKeyMappingAccessor key = init_AutismKeyMappingAc(mc);
            if (sync_Z) {
               if (!read()) {
                  key.autism$setClickCount(0);
                  return;
               }

               sync_Z = false;
            }

            if (close() && update() && !flush && !update_Z(mc)) {
               update_V(mc);
            } else if (!update()) {
               if (!load() && (key.autism$clickCount() > 0 || read_Z(mc))) {
                  init_LocalPlayer = mc.player;
                  init_ClientPacketListen = mc.getConnection();
                  init_ClientLevel = mc.level;
                  if (!init_Z) {
                     sync_I = ck.init_ck().state_I();
                  }

                  init_Z = true;
                  key.autism$setClickCount(0);
               }
            } else {
               if (key.autism$clickCount() > 0) {
                  if (!init_Z) {
                     sync_I = ck.init_ck().state_I();
                  }

                  init_Z = true;
                  key.autism$setClickCount(0);
               }

               if (poll() && write_Z(mc) && (update_Z || init_Z(mc, init_ce_DataUtil))) {
                  if (init_Z) {
                     key.autism$setClickCount(1);
                  }
               } else {
                  aB.init(mc.options.keyUse).autism$simulatePress(mc.player.isUsingItem());
                  key.autism$setClickCount(0);
                  write_Z = true;
               }
            }
         }
      }
   }

   public static void load_V(Minecraft mc) {
      if (write_Z && mc.options != null) {
         aB.init(mc.options.keyUse).autism$resetPressedState();
      }

      write_Z = false;
   }

   public static boolean init_Z(Minecraft mc) {
      if (!read_Z
         && open()
         && !s.flush_Z()
         && !autismclient.modules.KillAuraModule.init_Z_31()
         && !autismclient.modules.ScaffoldModule.init_Z_26()
         && !f.load()
         && !autismclient.modules.aD.init_Z()
         && !autismclient.modules.FreecamController.init_Z()
         && !cd.init_Z()) {
         ck state = ck.init_ck();
         return !state.frame() && !state.pool();
      } else {
         return false;
      }
   }

   public static ListPool.Inner_DataUtil init_ListPool_DataUtil(Minecraft mc) {
      int tick = ck.init_ck().state_I();
      if (!load() && !sync_Z && load_Z(mc) && update_I != tick) {
         if (!close() || update_Z(mc) && !init(tick)) {
            if (update() && poll() && write_Z(mc) && init_Z(mc, init_ce_DataUtil)) {
               HitResult hit = init_HitResult(mc, init_ce_DataUtil);
               ListPool.Inner_DataUtil frame = new ListPool.Inner_DataUtil(
                  init_LocalPlayer, mc.hitResult, init_LocalPlayer.getYRot(), init_LocalPlayer.getXRot()
               );
               init_LocalPlayer.setYRot(init_ce_DataUtil.init());
               init_LocalPlayer.setXRot(init_ce_DataUtil.load());
               mc.hitResult = hit;
               read_Z = true;
               init_Z = false;
               update_I = tick;
               flush = true;
               return frame;
            }

            if (!init_Z) {
               sync_I = tick;
            }

            init_Z = true;
            return null;
         } else {
            update_V(mc);
            return null;
         }
      } else {
         return null;
      }
   }

   public static boolean flush() {
      return read_Z;
   }

   private static boolean write_Z(Minecraft mc) {
      return init_LocalPlayer == mc.player
         && load_I == init_LocalPlayer.getInventory().getSelectedSlot()
         && ItemStack.isSameItemSameComponents(init_ItemStack, init_LocalPlayer.getMainHandItem())
         && ItemStack.isSameItemSameComponents(load_ItemStack, init_LocalPlayer.getOffhandItem());
   }

   private static void read_V(Minecraft mc) {
      load_ce_DataUtil = ce.init(init_LocalPlayer);
      init_HitResult = init_LocalPlayer.isUsingItem() ? null : init_HitResult(mc, load_ce_DataUtil);
      init_Vec3 = init_HitResult == null
         ? null
         : (init_HitResult instanceof EntityHitResult entity ? entity.getLocation().subtract(entity.getEntity().position()) : init_HitResult.getLocation());
      init_BlockState = init_HitResult instanceof BlockHitResult block && block.getType() == Type.BLOCK ? mc.level.getBlockState(block.getBlockPos()) : null;
      read_ce_DataUtil = load_ce_DataUtil;
      read_I = ck.init_ck().state_I();
      load_J = System.nanoTime();
      close = true;
   }

   private static boolean init(int tick) {
      return load_J != 0L && (tick - read_I > 20 || System.nanoTime() - load_J > 1250000000L);
   }

   private static Vec3 init_Vec3() {
      return init_HitResult instanceof EntityHitResult hit ? hit.getEntity().position().add(init_Vec3) : init_Vec3;
   }

   private static boolean update_Z(Minecraft mc) {
      if (init_HitResult != null && init_HitResult.getType() != Type.MISS) {
         if (init_HitResult instanceof EntityHitResult entity
            && (entity.getEntity().isRemoved() || entity.getEntity().level() != mc.level || !entity.getEntity().isPickable())) {
            return false;
         } else if (init_HitResult instanceof BlockHitResult block && !mc.level.getBlockState(block.getBlockPos()).equals(init_BlockState)) {
            return false;
         } else {
            Vec3 eye = init_LocalPlayer.getEyePosition();
            Vec3 point = init_Vec3();
            double reach = init_HitResult.getType() == Type.BLOCK ? init_LocalPlayer.blockInteractionRange() : init_LocalPlayer.entityInteractionRange();
            return point != null && !(eye.distanceToSqr(point) > (reach - 0.035) * (reach - 0.035)) ? init(init_HitResult(mc, ce.init(point, eye))) : false;
         }
      } else {
         return true;
      }
   }

   private static boolean init(HitResult hit) {
      if (init_HitResult != null && hit != null && init_HitResult.getType() == hit.getType()) {
         if (init_HitResult instanceof EntityHitResult expected && hit instanceof EntityHitResult actual) {
            return expected.getEntity() == actual.getEntity();
         } else {
            return init_HitResult instanceof BlockHitResult expected && hit instanceof BlockHitResult actual
               ? expected.getBlockPos().equals(actual.getBlockPos()) && expected.getDirection() == actual.getDirection()
               : init_HitResult.getType() == Type.MISS;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z(Minecraft mc, ce.Inner_DataUtil at) {
      if (!close()) {
         return load(mc, at);
      } else {
         return init_HitResult != null && init_HitResult.getType() != Type.MISS
            ? init(init_HitResult(mc, at))
            : ce.init_F(read_ce_DataUtil, at) <= Math.max(0.2, ce.init() * 1.5) && (init_HitResult == null || init_HitResult(mc, at).getType() == Type.MISS);
      }
   }

   private static void write_V(Minecraft mc) {
      Vec3 eye = init_LocalPlayer.getEyePosition();
      Vec3 point = init_HitResult != null && init_HitResult.getType() != Type.MISS
         ? init_Vec3()
         : eye.add(Vec3.directionFromRotation(read_ce_DataUtil.load(), read_ce_DataUtil.init()).scale(3.0));
      AABB box = init_HitResult instanceof EntityHitResult entity
         ? entity.getEntity().getBoundingBox()
         : (
            init_HitResult instanceof BlockHitResult block && block.getType() == Type.BLOCK
               ? new AABB(block.getBlockPos())
               : new AABB(point, point).inflate(0.1)
         );
      init_q_DataUtil.init_q_DataUtil_Vec3_2(eye, box, point);
      init_q_DataUtil.init_D = read_ce_DataUtil.init();
      init_q_DataUtil.load_D = read_ce_DataUtil.load();
      init_q_DataUtil.read_D = Math.max(0.25, Math.min(2.5, init_q_DataUtil.read_D));
      init_q_DataUtil.write_D = init_q_DataUtil.update_D = Math.max(0.25, Math.min(1.5, init_q_DataUtil.read_D));
      init_q_DataUtil.read_Z = false;
      init_q_DataUtil.write_Z = true;
      init_q_DataUtil.update_Z = !close;
      init_q_DataUtil.flush_Z = true;
      init_q_DataUtil.close_Z = true;
      init_q_DataUtil.open_Z = true;
      init_q_DataUtil.sync_D = 0.0;
      init_q_DataUtil.init_I = 0;
      init_q_DataUtil.init_Z = false;
      init_q_DataUtil.poll_D = ce.init();
      init_q_DataUtil.push_D = 18.0;
      init_q_DataUtil.push_Z = false;
      init_q_DataUtil.pop_Z = aI.init(init_LocalPlayer);
      init_q_DataUtil.load_Z = close;
   }

   private static void update_V(Minecraft mc) {
      boolean held = read_Z(mc);
      if (mc.options != null) {
         init_AutismKeyMappingAc(mc).autism$setClickCount(0);
      }

      init_V();
      init_LocalPlayer = mc.player;
      init_ClientPacketListen = mc.getConnection();
      init_ClientLevel = mc.level;
      sync_Z = held;
   }

   private static ce.Inner_DataUtil init_ce_DataUtil(Minecraft mc) {
      ce.Inner_DataUtil camera = ce.init(mc.player);
      ch.Inner_CachePool wire = ch.init_ch_CachePool();
      if (!wire.init_Z()) {
         return camera;
      }

      float yaw = ch.init_F();
      ce.Inner_DataUtil from = new ce.Inner_DataUtil(Float.isFinite(yaw) ? yaw : wire.read(), wire.write());
      ce.Inner_DataUtil rounded = ce.init_ce_DataUtil(camera, from);
      if (load(mc, rounded)) {
         return rounded;
      }

      ce.Inner_DataUtil best = rounded;
      double bestDistance = 1.0 / 0.0;
      float step = (float)ce.init();

      for (int x = -1; x <= 1; x++) {
         for (int y = -1; y <= 1; y++) {
            if (x != 0 || y != 0) {
               ce.Inner_DataUtil candidate = ce.init_ce_DataUtil(new ce.Inner_DataUtil(rounded.init() + x * step, rounded.load() + y * step), from);
               double distance = ce.init_F(camera, candidate);
               if (distance < bestDistance && load(mc, candidate)) {
                  best = candidate;
                  bestDistance = distance;
               }
            }
         }
      }

      return best;
   }

   private static boolean load(Minecraft mc, ce.Inner_DataUtil at) {
      HitResult wanted = init_HitResult(mc, ce.init(mc.player));
      HitResult actual = init_HitResult(mc, at);
      if (wanted.getType() != actual.getType()) {
         return false;
      } else if (wanted instanceof BlockHitResult a && actual instanceof BlockHitResult b && wanted.getType() == Type.BLOCK) {
         return a.getBlockPos().equals(b.getBlockPos()) && a.getDirection() == b.getDirection();
      } else {
         return wanted instanceof EntityHitResult a && actual instanceof EntityHitResult b
            ? a.getEntity() == b.getEntity() && !a.getEntity().isRemoved()
            : ce.init_F(at, ce.init(mc.player)) <= Math.max(0.15, ce.init());
      }
   }

   private static HitResult init_HitResult(Minecraft mc, ce.Inner_DataUtil at) {
      Vec3 eye = mc.player.getEyePosition();
      Vec3 look = Vec3.directionFromRotation(at.load(), at.init());
      double blockReach = mc.player.blockInteractionRange();
      double entityReach = mc.player.entityInteractionRange();
      double reach = Math.max(blockReach, entityReach);
      Vec3 end = eye.add(look.scale(reach));
      BlockHitResult block = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, mc.player));
      double obstruction = block.getType() == Type.BLOCK ? eye.distanceToSqr(block.getLocation()) : reach * reach;
      EntityHitResult entity = ProjectileUtil.getEntityHitResult(
         mc.player,
         eye,
         end,
         mc.player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0),
         target -> !target.isSpectator() && target.isPickable(),
         Math.min(obstruction, entityReach * entityReach)
      );
      if (entity != null && eye.distanceToSqr(entity.getLocation()) <= entityReach * entityReach) {
         return entity;
      } else {
         return block.getType() == Type.BLOCK && obstruction <= blockReach * blockReach
            ? block
            : BlockHitResult.miss(end, Direction.getApproximateNearest(look.x, look.y, look.z), mc.player.blockPosition());
      }
   }

   public static void init_V() {
      load_Z = false;
      init_Z = false;
      init_ce_DataUtil = null;
      init_LocalPlayer = null;
      init_ClientPacketListen = null;
      init_ClientLevel = null;
      update_Z = false;
      sync_Z = false;
      init_HitResult = null;
      init_Vec3 = null;
      read_ce_DataUtil = null;
      load_ce_DataUtil = null;
      init_BlockState = null;
      load_J = 0L;
      flush = false;
      init_ItemStack = load_ItemStack = ItemStack.EMPTY;
      update_I = -2147483648;
      write_I = -2147483648;
   }

   public record Inner_DataUtil(LocalPlayer init_LocalPlayer, HitResult init_HitResult, float init_F, float load) {
      public void init(Minecraft mc) {
         this.init_LocalPlayer.setYRot(this.init_F);
         this.init_LocalPlayer.setXRot(this.load);
         if (mc.player == this.init_LocalPlayer) {
            mc.hitResult = this.init_HitResult;
         }

         ListPool.read_Z = false;
      }
   }
}
