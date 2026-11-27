package autismclient.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FreecamTargetOutlineRenderer {
   private static final int init_I = -13248397;
   private static final int load_I = 775280755;
   private static final int read_I = -1946548;
   private static final int write_I = 786582604;
   private static final float init_F = 2.0F;
   private static final double init_D = 0.0025;
   private static final double load_D = 0.04;
   private static final double read_D = 1.0;
   private static final double write_D = 3.0;

   private FreecamTargetOutlineRenderer() {
   }

   public static boolean isActive() {
      return autismclient.modules.FreecamController.isActive() && (autismclient.modules.FreecamController.isInteractionAllowed() || autismclient.modules.FreecamController.isAiControllingBody()) && !autismclient.modules.aD.read_Z();
   }

   public static void register() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (isActive()) {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc != null && mc.level != null && mc.player != null) {
                     CameraRenderState cameraState = context.levelState().cameraRenderState;
                     Vec3 camera = cameraState.pos;
                     Vec3 origin = camera;
                     Vec3 direction = Vec3.directionFromRotation(cameraState.xRot, cameraState.yRot);
                     if (autismclient.modules.FreecamController.isAiControllingBody()) {
                        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                        origin = mc.player.getEyePosition(partialTick);
                        direction = mc.player.getViewVector(partialTick);
                     }

                     double blockRange = Math.max(4.5, mc.player.blockInteractionRange());
                     BlockHitResult blockHit = mc.level
                        .clip(new ClipContext(origin, origin.add(direction.scale(blockRange)), Block.OUTLINE, Fluid.NONE, mc.player));
                     boolean hasBlock = blockHit != null && blockHit.getType() == Type.BLOCK;
                     double blockDistSq = hasBlock ? origin.distanceToSqr(blockHit.getLocation()) : 1.7976931348623157E308;
                     double entityRange = Math.max(3.0, mc.player.entityInteractionRange());
                     Vec3 entityEnd = origin.add(direction.scale(entityRange));
                     AABB entitySearch = new AABB(origin, entityEnd).inflate(1.0);
                     EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                        mc.player, origin, entityEnd, entitySearch, EntitySelector.CAN_BE_PICKED, entityRange * entityRange
                     );
                     double entityDistSq = entityHit != null ? origin.distanceToSqr(entityHit.getLocation()) : 1.7976931348623157E308;
                     PoseStack poseStack = context.poseStack();
                     if (entityHit != null && entityDistSq <= blockDistSq) {
                        Entity target = entityHit.getEntity();
                        boolean reachable = mc.player.isWithinEntityInteractionRange(target, 3.0);
                        int lineColor = reachable ? -13248397 : -1946548;
                        int fillColor = reachable ? 775280755 : 786582604;
                        AABB box = target.getBoundingBox().inflate(0.04).move(-camera.x, -camera.y, -camera.z);
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), (pose, buffer) -> load(pose, buffer, box, fillColor)
                           );
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(), (pose, buffer) -> init(pose, buffer, box, lineColor)
                           );
                     } else if (hasBlock) {
                        BlockPos pos = blockHit.getBlockPos();
                        boolean reachable = mc.player.isWithinBlockInteractionRange(pos, 1.0);
                        int lineColor = reachable ? -13248397 : -1946548;
                        int fillColor = reachable ? 775280755 : 786582604;
                        AABB box = init(mc, pos).inflate(0.0025).move(-camera.x, -camera.y, -camera.z);
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), (pose, buffer) -> load(pose, buffer, box, fillColor)
                           );
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(), (pose, buffer) -> init(pose, buffer, box, lineColor)
                           );
                     }
                  }
               }
            }
         );
   }

   private static AABB init(Minecraft mc, BlockPos pos) {
      try {
         VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
         if (!shape.isEmpty()) {
            return shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());
         }
      } catch (Throwable var3) {
      }

      return new AABB(pos);
   }

   private static void init(Pose pose, VertexConsumer buffer, AABB box, int color) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      init(pose, buffer, x1, y1, z1, x2, y1, z1, color);
      init(pose, buffer, x2, y1, z1, x2, y1, z2, color);
      init(pose, buffer, x2, y1, z2, x1, y1, z2, color);
      init(pose, buffer, x1, y1, z2, x1, y1, z1, color);
      init(pose, buffer, x1, y2, z1, x2, y2, z1, color);
      init(pose, buffer, x2, y2, z1, x2, y2, z2, color);
      init(pose, buffer, x2, y2, z2, x1, y2, z2, color);
      init(pose, buffer, x1, y2, z2, x1, y2, z1, color);
      init(pose, buffer, x1, y1, z1, x1, y2, z1, color);
      init(pose, buffer, x2, y1, z1, x2, y2, z1, color);
      init(pose, buffer, x2, y1, z2, x2, y2, z2, color);
      init(pose, buffer, x1, y1, z2, x1, y2, z2, color);
   }

   private static void init(Pose pose, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      WorldLineRenderer.init(pose, buffer, x1, y1, z1, x2, y2, z2, color, 2.0F);
   }

   private static void load(Pose pose, VertexConsumer buffer, AABB box, int color) {
      init(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
      init(pose, buffer, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);
      init(pose, buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
      init(pose, buffer, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
      init(pose, buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);
      init(pose, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   private static void init(
      Pose pose,
      VertexConsumer buffer,
      double x1,
      double y1,
      double z1,
      double x2,
      double y2,
      double z2,
      double x3,
      double y3,
      double z3,
      double x4,
      double y4,
      double z4,
      int color
   ) {
      buffer.addVertex(pose, (float)x1, (float)y1, (float)z1).setColor(color);
      buffer.addVertex(pose, (float)x2, (float)y2, (float)z2).setColor(color);
      buffer.addVertex(pose, (float)x3, (float)y3, (float)z3).setColor(color);
      buffer.addVertex(pose, (float)x4, (float)y4, (float)z4).setColor(color);
   }
}
