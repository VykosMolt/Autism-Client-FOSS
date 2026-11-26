package autismclient.ai.engine;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;

public final class AiCombatMotion {
   private AiCombatMotion() {
   }

   public static boolean init(Minecraft mc) {
      if (mc.player != null && mc.level != null && mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
         AiLiveWorld world = new AiLiveWorld(mc.level, mc.player);
         AiPhysics.Inner_StreamUtil state = AiPhysics.Inner_StreamUtil.of(mc.player);
         double floorY = state.y();

         for (int tick = 0; tick < 24; tick++) {
            state = AiPhysics.init(state, new AiPhysics.Inner_DataUtil(0.0, 0.0, tick == 0, false, false, mc.player.getYRot()), world);
            BlockPos feet = BlockPos.containing(state.x(), state.y(), state.z());
            if (!mc.level.isInsideBuildHeight(feet)
               || mc.level.getChunkSource().getChunk(feet.getX() >> 4, feet.getZ() >> 4, ChunkStatus.FULL, false) == null
               || state.y() < floorY - 0.1
               || world.init_Z_I(feet.getX(), feet.getY(), feet.getZ())
               || world.write_Z(feet.getX(), feet.getY(), feet.getZ())) {
               return false;
            }

            if (tick > 0 && state.onGround()) {
               return !world.update(feet.getX(), feet.getY() - 1, feet.getZ());
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static AABB init(Minecraft mc, double forward, double strafe, int ticks) {
      List<AABB> trace = init(mc, mc.player == null ? 0.0F : mc.player.getYRot(), forward, strafe, ticks);
      return trace == null ? null : (trace.isEmpty() ? mc.player.getBoundingBox() : trace.getLast());
   }

   public static List<AABB> init(Minecraft mc, float yaw, double forward, double strafe, int ticks) {
      return init(mc, yaw, forward, strafe, ticks, 0);
   }

   public static List<AABB> init(Minecraft mc, float yaw, double forward, double strafe, int ticks, int coastTicks) {
      return init(mc, yaw, forward, strafe, ticks, coastTicks, false);
   }

   public static List<AABB> init(Minecraft mc, float yaw, double forward, double strafe, int ticks, int coastTicks, boolean sprint) {
      if (mc.player != null && mc.level != null && mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
         AiLiveWorld world = new AiLiveWorld(mc.level, mc.player);
         AiPhysics.Inner_StreamUtil state = AiPhysics.Inner_StreamUtil.of(mc.player);
         double slowdown = mc.player.isUsingItem()
            ? mc.player.getUseItem().getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT).speedMultiplier()
            : 1.0;
         AiPhysics.Inner_DataUtil input = new AiPhysics.Inner_DataUtil(forward * slowdown, strafe * slowdown, false, sprint, mc.player.isShiftKeyDown(), yaw);
         ArrayList<AABB> trace = new ArrayList<>();
         int movingTicks = Math.min(12, Math.max(0, ticks));

         for (int tick = 0; tick < movingTicks + Math.min(4, Math.max(0, coastTicks)); tick++) {
            AiPhysics.Inner_DataUtil active = tick < movingTicks
               ? input
               : new AiPhysics.Inner_DataUtil(0.0, 0.0, false, false, mc.player.isShiftKeyDown(), yaw);
            AiPhysics.Inner_StreamUtil next = AiPhysics.init(state, active, world);
            BlockPos feet = BlockPos.containing(next.x(), next.y(), next.z());
            if (!mc.level.isInsideBuildHeight(feet)
               || mc.level.getChunkSource().getChunk(feet.getX() >> 4, feet.getZ() >> 4, ChunkStatus.FULL, false) == null
               || !next.onGround()
               || next.y() < mc.player.getY() - 0.1
               || world.init_Z_I(feet.getX(), feet.getY(), feet.getZ())
               || world.load_Z(feet.getX(), feet.getY(), feet.getZ())
               || world.write_Z(feet.getX(), feet.getY(), feet.getZ())
               || world.update(feet.getX(), feet.getY() - 1, feet.getZ())) {
               return null;
            }

            state = next;
            trace.add(mc.player.getBoundingBox().move(state.x() - mc.player.getX(), state.y() - mc.player.getY(), state.z() - mc.player.getZ()));
         }

         return List.copyOf(trace);
      } else {
         return null;
      }
   }
}
