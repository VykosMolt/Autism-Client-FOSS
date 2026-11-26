package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiService;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.LongPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public final class AiFallGuard {
   private static final int init_I = 3;
   private static final double init_D = 3.0;
   private static final Set<AiPathfinder.Inner_SyncManager> init_Set = EnumSet.of(
      AiPathfinder.Inner_SyncManager.start,
      AiPathfinder.Inner_SyncManager.put,
      AiPathfinder.Inner_SyncManager.add,
      AiPathfinder.Inner_SyncManager.clear,
      AiPathfinder.Inner_SyncManager.reset,
      AiPathfinder.Inner_SyncManager.flush,
      AiPathfinder.Inner_SyncManager.close,
      AiPathfinder.Inner_SyncManager.open,
      AiPathfinder.Inner_SyncManager.poll,
      AiPathfinder.Inner_SyncManager.next
   );
   private static volatile long init_J;
   private static volatile AiPathfinder.Inner_SyncManager init_ClassCache_SyncMan;
   private static volatile boolean init_Z;
   private static volatile int load_I = -1073741824;
   private static volatile long load_J;
   private static volatile int read_I = -1073741824;
   private static volatile int write = -1073741824;
   private static volatile LongPredicate init_LongPredicate;
   private static volatile long read_J;

   private AiFallGuard() {
   }

   public static void init_V() {
      init_J = System.nanoTime();
   }

   static void init(AiPathfinder.Inner_SyncManager type, boolean tellyBridge) {
      init(type, tellyBridge, false);
   }

   static void init(AiPathfinder.Inner_SyncManager type, boolean tellyBridge, boolean provedRunUp) {
      init_ClassCache_SyncMan = type;
      init_Z = provedRunUp || init_Set.contains(type) || type == AiPathfinder.Inner_SyncManager.run && tellyBridge;
      LocalPlayer player = Minecraft.getInstance().player;
      load_I = player == null ? -1073741824 : player.tickCount;
      init_J = System.nanoTime();
   }

   public static void load() {
      LocalPlayer player = Minecraft.getInstance().player;
      write = player == null ? -1073741824 : player.tickCount;
   }

   public static void init(LongPredicate traps) {
      init_LongPredicate = traps;
      read_J = System.nanoTime();
   }

   private static boolean init(AiPhysics.Inner_StreamUtil s) {
      LongPredicate traps = init_LongPredicate;
      return traps != null && System.nanoTime() - read_J <= 250000000L
         ? traps.test(BlockPos.asLong((int)Math.floor(s.x()), (int)Math.floor(s.y() + 0.1), (int)Math.floor(s.z())))
         : false;
   }

   public static boolean init_Z() {
      LocalPlayer player = Minecraft.getInstance().player;
      return player != null && player.tickCount - read_I <= 1;
   }

   public static long init_J() {
      return load_J;
   }

   public static Input init(ClientInput source, Input original) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (original != null && player != null && mc.level != null && player.input == source) {
         if (!original.shift()
            && player.onGround()
            && !player.isInWater()
            && !player.onClimbable()
            && !player.getAbilities().flying
            && mc.level.dimension() == Level.END) {
            boolean drivenNow = System.nanoTime() - init_J < 250000000L;
            if (!drivenNow) {
               AiService service = AiService.load_cc();
               drivenNow = service != null && service.open_Z() || autismclient.ai.AiDirectOps.load_Z();
            }

            if (drivenNow) {
               double f = (original.forward() ? 1 : 0) - (original.backward() ? 1 : 0);
               double st = (original.left() ? 1 : 0) - (original.right() ? 1 : 0);
               AiPhysics.Inner_DataUtil voidInput = new AiPhysics.Inner_DataUtil(f, st, original.jump(), original.sprint(), false, player.getYRot());
               if (read(new AiLiveWorld(mc.level, player), AiPhysics.Inner_StreamUtil.of(player), voidInput)) {
                  load_J++;
                  if (player.tickCount - read_I > 1) {
                     AutismClientAddon.LOG.info("[AI] fall guard: held at the void's edge at {}", player.blockPosition().toShortString());
                  }

                  read_I = player.tickCount;
                  return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, false);
               }
            }
         }

         if (!original.shift() && !original.jump()) {
            long now = System.nanoTime();
            boolean driven = now - init_J < 250000000L;
            if (!driven) {
               AiService service = AiService.load_cc();
               driven = service != null && service.open_Z() || autismclient.ai.AiDirectOps.load_Z();
            }

            if (!driven) {
               return original;
            }

            if (player.onGround()
               && !player.isInWater()
               && !player.isInLava()
               && !player.onClimbable()
               && !player.isPassenger()
               && !player.getAbilities().flying
               && !player.isFallFlying()) {
               AiPathfinder.Inner_SyncManager current = init_ClassCache_SyncMan;
               if (current != null && init_Z && player.tickCount - load_I <= 1) {
                  return original;
               }

               if (player.tickCount - write <= 1) {
                  return original;
               }

               if (player.getBlockStateOn().is(Blocks.MAGMA_BLOCK) && !player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                  return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, false);
               }

               double forward = (original.forward() ? 1 : 0) - (original.backward() ? 1 : 0);
               double strafe = (original.left() ? 1 : 0) - (original.right() ? 1 : 0);
               AiPhysics.Inner_DataUtil input = new AiPhysics.Inner_DataUtil(forward, strafe, false, original.sprint(), false, player.getYRot());
               AiLiveWorld world = new AiLiveWorld(mc.level, player);
               if (!load(world, AiPhysics.Inner_StreamUtil.of(player), input)) {
                  return original;
               }

               load_J++;
               if (player.tickCount - read_I > 1) {
                  AutismClientAddon.LOG
                     .info("[AI] fall guard: held at the edge at {} (move {})", player.blockPosition().toShortString(), current == null ? "-" : current);
               }

               read_I = player.tickCount;
               return new Input(original.forward(), original.backward(), original.left(), original.right(), false, true, original.sprint());
            } else {
               return original;
            }
         } else {
            return original;
         }
      } else {
         return original;
      }
   }

   public static boolean init(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil state, AiPhysics.Inner_DataUtil input) {
      if (!input.sneak() && !input.jump() && state.onGround()) {
         int x = (int)Math.floor(state.x());
         int y = (int)Math.floor(state.y() + 0.001);
         int z = (int)Math.floor(state.z());
         return !world.load_Z(x, y, z) && !world.init_Z_I(x, y, z) && !world.sync(x, y, z) && !world.sync(x, y + 1, z)
            ? load(world, state, new AiPhysics.Inner_DataUtil(input.forward(), input.strafe(), false, input.sprint(), false, input.yaw()))
            : false;
      } else {
         return false;
      }
   }

   static boolean load(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil state, AiPhysics.Inner_DataUtil input) {
      double top = state.y();
      AiPhysics.Inner_StreamUtil s = state;

      for (int tick = 0; tick < 3; tick++) {
         AiPhysics.Inner_StreamUtil next = AiPhysics.init(s, input, world);
         if (AiPhysics.init_Z(world, s, next)) {
            return true;
         }

         s = next;
         if (!s.onGround() && !init(world, s)) {
            return init(world, s, top);
         }
      }

      return false;
   }

   static boolean read(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil state, AiPhysics.Inner_DataUtil input) {
      AiPhysics.Inner_StreamUtil s = state;
      double top = state.y();
      AiPhysics.Inner_DataUtil held = new AiPhysics.Inner_DataUtil(input.forward(), input.strafe(), false, input.sprint(), false, input.yaw());

      for (int tick = 0; tick < 90; tick++) {
         s = AiPhysics.init(s, tick == 0 ? input : (tick < 3 ? held : AiPhysics.Inner_DataUtil.NONE), world);
         if (load(world, s) || init(world, s)) {
            return false;
         }

         if (s.onGround() && tick >= 3) {
            return false;
         }

         if (top - s.y() > 64.0) {
            return true;
         }
      }

      return false;
   }

   private static boolean init(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil s, double top) {
      for (int tick = 0; tick < 80; tick++) {
         AiPhysics.Inner_StreamUtil next = AiPhysics.init(s, AiPhysics.Inner_DataUtil.NONE, world);
         if (AiPhysics.init_Z(world, s, next)) {
            return true;
         }

         if (load(world, next)) {
            return init(next);
         }

         if (init(world, next)) {
            return false;
         }

         s = next;
         if (s.onGround()) {
            return top - s.y() > 3.000001;
         }
      }

      return true;
   }

   private static boolean init(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil s) {
      int x = (int)Math.floor(s.x());
      int y = (int)Math.floor(s.y() + 0.001);
      int z = (int)Math.floor(s.z());
      return world.sync(x, y, z) || world.sync(x, y + 1, z);
   }

   private static boolean load(AiPhysics.Inner_CachePool world, AiPhysics.Inner_StreamUtil s) {
      return world.load_Z((int)Math.floor(s.x()), (int)Math.floor(s.y() + 0.1), (int)Math.floor(s.z()));
   }
}
