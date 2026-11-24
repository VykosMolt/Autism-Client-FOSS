package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DataUtil extends Module {
   private boolean init_Z = true;
   private static volatile long init_J;

   public DataUtil() {
      super("air-jump", "AirJump", ae.load_ae, "Jump in mid air.");
      this.add(new ChoiceSetting("mode", "Mode", "JumpFreely", "JumpFreely", "DoubleJump", "GhostBlock").build());
   }

   @Override
   public void preMovementTick() {
      if (MC.player != null && MC.player.onGround()) {
         this.init_Z = true;
      }
   }

   private boolean read() {
      return "JumpFreely".equals(this.choice("mode")) || "DoubleJump".equals(this.choice("mode")) && this.init_Z;
   }

   public static boolean init_Z_4() {
      return al.init_Module_Str("air-jump") instanceof DataUtil airJump && airJump.isEnabled() && airJump.read();
   }

   public static void init(LivingEntity entity) {
      if (MC != null
         && MC.player != null
         && entity == MC.player
         && al.init_Module_Str("air-jump") instanceof DataUtil airJump
         && airJump.isEnabled()
         && airJump.init_Z
         && !MC.player.onGround()) {
         airJump.init_Z = false;
      }
   }

   public static VoxelShape init(VoxelShape original, BlockPos pos) {
      if (MC != null && MC.isSameThread() && MC.player != null && MC.options != null) {
         return al.init_Module_Str("air-jump") instanceof DataUtil airJump
               && airJump.isEnabled()
               && "GhostBlock".equals(airJump.choice("mode"))
               && pos.getY() < MC.player.blockPosition().getY()
               && load()
            ? Shapes.block()
            : original;
      } else {
         return original;
      }
   }

   public static boolean load() {
      if (MC != null && MC.options != null) {
         long now = System.currentTimeMillis();
         if (MC.options.keyJump.isDown()) {
            init_J = now;
            return true;
         } else {
            return now - init_J <= init_J();
         }
      } else {
         return false;
      }
   }

   private static long init_J() {
      if (MC.player == null) {
         return 300L;
      } else {
         Vec3 v = MC.player.getDeltaMovement();
         double speed = Math.max(Math.hypot(v.x, v.z), Math.abs(v.y));
         if (speed <= 0.22) {
            return 300L;
         } else {
            return speed >= 0.8 ? 900L : 300L + (long)((speed - 0.22) / 0.58 * 600.0);
         }
      }
   }
}
