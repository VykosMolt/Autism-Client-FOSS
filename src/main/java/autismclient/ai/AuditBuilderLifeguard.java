package autismclient.ai;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drowning safeguard for {@code game:build}: if the player is in water and running low on air or
 * health while an AI schematic build is active, stops the AI operation, takes movement controls,
 * swims up toward open air / dry land, and releases once safe. Completely dormant otherwise.
 */
public final class AuditBuilderLifeguard {
   private static final Logger LOG = LoggerFactory.getLogger("autism-builder");
   public static volatile boolean active;
   private static int quiet;
   private static int activeTicks;
   private static BlockPos land;

   private AuditBuilderLifeguard() {
   }

   public static void tickClient(Minecraft mc) {
      LocalPlayer p = mc == null ? null : mc.player;
      if (p == null || mc.level == null || p.isSpectator() || p.getAbilities().flying) {
         if (active) {
            stop(mc);
         }

         return;
      }

      boolean wet = p.isInWater() || p.isEyeInFluid(FluidTags.WATER);
      boolean danger = wet && (p.getAirSupply() < 200 || p.getHealth() < 10.0F);
      if (danger && !active && AiService.auditIsSchematicBuildRunning()) {
         active = true;
         activeTicks = 0;
         land = null;
         AiService assistant = AiService.getExistingInstance();
         if (assistant != null) {
            assistant.stopTask();
            LOG.info("lifeguard: stopped the AI operation");
         }
         LOG.info("lifeguard: taking over at {} (air {}, health {})", p.blockPosition().toShortString(), p.getAirSupply(), p.getHealth());
         System.setProperty("builderfixes.lifeguard", "on");
      }

      if (!active) {
         return;
      }

      boolean safe = !p.isEyeInFluid(FluidTags.WATER) && p.getAirSupply() >= p.getMaxAirSupply() - 10 && p.onGround() && !p.isInWater();
      if (safe && ++quiet > 10) {
         stop(mc);
         LOG.info("lifeguard: safe at {}", p.blockPosition().toShortString());
         return;
      }

      if (!safe) {
         quiet = 0;
      }

      if (++activeTicks > 600) {
         stop(mc);
         LOG.info("lifeguard: gave up after 30 s without reaching safety at {}", p.blockPosition().toShortString());
         return;
      }

      boolean under = p.isEyeInFluid(FluidTags.WATER);
      if (land == null || p.tickCount % 10 == 0) {
         land = under ? nearestAir(mc.level, p.blockPosition()) : nearestLand(mc.level, p.blockPosition());
      }

      if (land == null && !under) {
         stop(mc);
         LOG.info("lifeguard: no land in reach of {}; released the controls at the surface", p.blockPosition().toShortString());
         return;
      }

      mc.options.keyShift.setDown(false);
      mc.options.keyUse.setDown(false);
      mc.options.keyAttack.setDown(false);
      mc.options.keyJump.setDown(wet || p.horizontalCollision);
      if (land != null) {
         double dx = land.getX() + 0.5 - p.getX();
         double dz = land.getZ() + 0.5 - p.getZ();
         p.setYRot((float)Math.toDegrees(Math.atan2(-dx, dz)));
         p.setXRot(under ? -45.0F : 0.0F);
         mc.options.keyUp.setDown(Math.hypot(dx, dz) > 0.3);
      } else {
         p.setXRot(-90.0F);
         mc.options.keyUp.setDown(false);
      }
   }

   public static BlockPos nearestAir(BlockGetter w, BlockPos from) {
      BlockPos best = null;
      double bestD = Double.MAX_VALUE;

      for (int dx = -10; dx <= 10; dx++) {
         for (int dz = -10; dz <= 10; dz++) {
            double d = Math.hypot(dx, dz);
            if (d >= bestD) {
               continue;
            }

            for (int y = from.getY(); y <= from.getY() + 24; y++) {
               BlockPos c = new BlockPos(from.getX() + dx, y, from.getZ() + dz);
               if (!w.getBlockState(c).getCollisionShape(w, c).isEmpty()) {
                  break;
               }

               if (w.getFluidState(c).isEmpty()) {
                  bestD = d;
                  best = new BlockPos(c.getX(), from.getY(), c.getZ());
                  break;
               }
            }
         }
      }

      return best;
   }

   public static BlockPos nearestLand(BlockGetter w, BlockPos from) {
      BlockPos best = null;
      double bestD = Double.MAX_VALUE;

      for (int dx = -12; dx <= 12; dx++) {
         for (int dz = -12; dz <= 12; dz++) {
            if (Math.hypot(dx, dz) >= bestD) {
               continue;
            }

            for (int y = from.getY() - 2; y <= from.getY() + 16; y++) {
               BlockPos c = new BlockPos(from.getX() + dx, y, from.getZ() + dz);
               BlockState floor = w.getBlockState(c.below());
               if (!floor.getFluidState().isEmpty() || !floor.isFaceSturdy(w, c.below(), Direction.UP)) {
                  continue;
               }

               if (!w.getFluidState(c).isEmpty()
                  || !w.getFluidState(c.above()).isEmpty()
                  || !w.getBlockState(c).getCollisionShape(w, c).isEmpty()
                  || !w.getBlockState(c.above()).getCollisionShape(w, c.above()).isEmpty()) {
                  continue;
               }

               double d = Math.hypot(dx, dz) + Math.abs(y - from.getY()) * 0.3;
               if (d < bestD) {
                  bestD = d;
                  best = c;
               }

               // Higher cells in this column are only farther away.
               if (y >= from.getY()) {
                  break;
               }
            }
         }
      }

      return best;
   }

   private static void stop(Minecraft mc) {
      active = false;
      quiet = 0;
      land = null;
      if (mc != null && mc.options != null) {
         release(mc);
      }

      System.setProperty("builderfixes.lifeguard", "off");
   }

   private static void release(Minecraft mc) {
      for (KeyMapping k : new KeyMapping[]{mc.options.keyUp, mc.options.keyJump, mc.options.keyShift, mc.options.keyUse}) {
         k.setDown(false);
      }
   }
}
