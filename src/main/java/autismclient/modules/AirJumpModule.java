package autismclient.modules;

import autismclient.api.module.ChoiceSetting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AirJumpModule extends Module {
   private boolean doubleJumpReady = true;
   private static volatile long lastJumpPressMillis;

   public AirJumpModule() {
      super("air-jump", "AirJump", ae.load_ae, "Jump in mid air.");
      this.add(new ChoiceSetting("mode", "Mode", "JumpFreely", "JumpFreely", "DoubleJump", "GhostBlock").build());
   }

   @Override
   public void preMovementTick() {
      if (MC.player != null && MC.player.onGround()) {
         this.doubleJumpReady = true;
      }
   }

   private boolean allowsJump() {
      return "JumpFreely".equals(this.choice("mode")) || "DoubleJump".equals(this.choice("mode")) && this.doubleJumpReady;
   }

   public static boolean canAirJump() {
      return al.init_Module_Str("air-jump") instanceof AirJumpModule airJump && airJump.isEnabled() && airJump.allowsJump();
   }

   public static void init(LivingEntity entity) {
      if (MC != null
         && MC.player != null
         && entity == MC.player
         && al.init_Module_Str("air-jump") instanceof AirJumpModule airJump
         && airJump.isEnabled()
         && airJump.doubleJumpReady
         && !MC.player.onGround()) {
         airJump.doubleJumpReady = false;
      }
   }

   public static VoxelShape init(VoxelShape original, BlockPos pos) {
      if (MC != null && MC.isSameThread() && MC.player != null && MC.options != null) {
         return al.init_Module_Str("air-jump") instanceof AirJumpModule airJump
               && airJump.isEnabled()
               && "GhostBlock".equals(airJump.choice("mode"))
               && pos.getY() < MC.player.blockPosition().getY()
               && jumpRecentlyPressed()
            ? Shapes.block()
            : original;
      } else {
         return original;
      }
   }

   public static boolean jumpRecentlyPressed() {
      if (MC != null && MC.options != null) {
         long now = System.currentTimeMillis();
         if (MC.options.keyJump.isDown()) {
            lastJumpPressMillis = now;
            return true;
         } else {
            return now - lastJumpPressMillis <= jumpGraceMillis();
         }
      } else {
         return false;
      }
   }

   private static long jumpGraceMillis() {
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
