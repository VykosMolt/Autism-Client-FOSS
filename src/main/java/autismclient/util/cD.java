package autismclient.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class cD {
   static final double init = 0.2;

   private cD() {
   }

   public static bb.Inner_DataUtil init(LocalPlayer player) {
      if (player != null
         && player.onGround()
         && !player.isShiftKeyDown()
         && !player.isInWater()
         && !player.isInLava()
         && !player.onClimbable()
         && !player.getAbilities().flying
         && !player.isPassenger()
         && !player.noPhysics) {
         Level level = player.level();
         float block = level.getBlockState(player.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction();
         if (block > 0.0F && !(block > 1.0F)) {
            double accel = player.getSpeed() * (0.21600002F / (block * block * block)) * 0.98F;
            double friction = block * 0.91F;
            Vec3 motion = player.getDeltaMovement();
            AABB box = player.getBoundingBox();
            double drop = player.maxUpStep();
            double inset = init(level, player, box.deflate(0.2, 0.0, 0.2), drop) ? 0.2 : 0.0;
            return worldYaw -> {
               double r = Math.toRadians(worldYaw);
               double dx = -Math.sin(r);
               double dz = Math.cos(r);
               double vx = motion.x;
               double vz = motion.z;
               double x = 0.0;
               double z = 0.0;

               for (int tick = 0; tick < 3; tick++) {
                  vx += dx * accel;
                  vz += dz * accel;
                  AABB moved = box.move(x + vx, 0.0, z + vz);
                  if (!level.noCollision(player, moved)) {
                     return 3;
                  }

                  x += vx;
                  z += vz;
                  if (!init(level, player, moved.deflate(inset, 0.0, inset), drop)) {
                     return tick;
                  }

                  vx *= friction;
                  vz *= friction;
               }

               return 3;
            };
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private static boolean init(Level level, LocalPlayer player, AABB box, double drop) {
      return !level.noCollision(player, new AABB(box.minX, box.minY - drop - 1.0E-5, box.minZ, box.maxX, box.minY, box.maxZ));
   }
}
