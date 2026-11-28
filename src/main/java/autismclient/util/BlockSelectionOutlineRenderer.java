package autismclient.util;

import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockSelectionOutlineRenderer {
   private static final double init_D = 0.0025;
   private static final double load_D = 0.25;
   private static volatile boolean outlineOn;
   private static volatile int outlineArgb = -1;
   private static volatile int fillArgbColor;
   private static volatile float outlineWidth = 2.0F;
   private static volatile boolean cornerMode;
   private static volatile boolean fillEnabled;
   private static volatile boolean dynamicColor;

   private BlockSelectionOutlineRenderer() {
   }

   public static void configure(boolean enabled, int line, float width, boolean cornerStyle, boolean filled, int fillArgb) {
      outlineArgb = line;
      outlineWidth = Math.max(0.1F, width);
      cornerMode = cornerStyle;
      fillEnabled = filled;
      fillArgbColor = fillArgb;
      outlineOn = enabled;
   }

   public static void setDynamic(boolean dynamic) {
      dynamicColor = dynamic;
   }

   public static void disable() {
      outlineOn = false;
   }

   public static boolean isActive() {
      return !AutismLiteVariant.enabled() && !autismclient.e.TokenStore.init() ? false : outlineOn && !autismclient.modules.aD.read_Z();
   }

   public static void register() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (isActive() && !FreecamTargetOutlineRenderer.isActive()) {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc != null && mc.level != null && mc.player != null) {
                     CameraRenderState cameraState = context.levelState().cameraRenderState;
                     Vec3 origin = cameraState.pos;
                     Vec3 direction = Vec3.directionFromRotation(cameraState.xRot, cameraState.yRot);
                     double range = Math.max(4.5, mc.player.blockInteractionRange());
                     BlockHitResult hit = mc.level.clip(new ClipContext(origin, origin.add(direction.scale(range)), Block.OUTLINE, Fluid.NONE, mc.player));
                     if (hit != null && hit.getType() == Type.BLOCK) {
                        AABB box = selectionBox(mc, hit.getBlockPos()).inflate(0.0025).move(-origin.x, -origin.y, -origin.z);
                        PoseStack poseStack = context.poseStack();
                        float progress = dynamicColor && mc.gameMode instanceof AutismMultiPlayerGameModeAccessor accessor
                           ? Math.max(0.0F, Math.min(1.0F, accessor.autism$getDestroyProgress()))
                           : 0.0F;
                        boolean dynamic = progress > 0.0F;
                        int line = dynamic ? lerpColor(-13248397, -50373, progress) : outlineArgb;
                        if (fillEnabled) {
                           int fc = dynamic ? fillArgbColor & 0xFF000000 | lerpColor(-13248397, -50373, progress) & 16777215 : fillArgbColor;
                           context.submitNodeCollector()
                              .submitCustomGeometry(
                                 poseStack, autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), (pose, buffer) -> drawFill(pose, buffer, box, fc)
                              );
                        }

                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack,
                              autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(),
                              (pose, buffer) -> drawOutline(pose, buffer, box, line, outlineWidth, cornerMode)
                           );
                     }
                  }
               }
            }
         );
   }

   private static AABB selectionBox(Minecraft mc, BlockPos pos) {
      try {
         VoxelShape shape = mc.level.getBlockState(pos).getShape(mc.level, pos);
         if (!shape.isEmpty()) {
            return shape.bounds().move(pos.getX(), pos.getY(), pos.getZ());
         }
      } catch (Throwable var3) {
      }

      return new AABB(pos);
   }

   private static void drawOutline(Pose pose, VertexConsumer buffer, AABB box, int color, float width, boolean corners) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      drawLine(pose, buffer, x1, y1, z1, x2, y1, z1, color, width, corners);
      drawLine(pose, buffer, x2, y1, z1, x2, y1, z2, color, width, corners);
      drawLine(pose, buffer, x2, y1, z2, x1, y1, z2, color, width, corners);
      drawLine(pose, buffer, x1, y1, z2, x1, y1, z1, color, width, corners);
      drawLine(pose, buffer, x1, y2, z1, x2, y2, z1, color, width, corners);
      drawLine(pose, buffer, x2, y2, z1, x2, y2, z2, color, width, corners);
      drawLine(pose, buffer, x2, y2, z2, x1, y2, z2, color, width, corners);
      drawLine(pose, buffer, x1, y2, z2, x1, y2, z1, color, width, corners);
      drawLine(pose, buffer, x1, y1, z1, x1, y2, z1, color, width, corners);
      drawLine(pose, buffer, x2, y1, z1, x2, y2, z1, color, width, corners);
      drawLine(pose, buffer, x2, y1, z2, x2, y2, z2, color, width, corners);
      drawLine(pose, buffer, x1, y1, z2, x1, y2, z2, color, width, corners);
   }

   private static void drawLine(
      Pose pose, VertexConsumer buffer, double ax, double ay, double az, double bx, double by, double bz, int color, float width, boolean corners
   ) {
      if (!corners) {
         WorldLineRenderer.init(pose, buffer, ax, ay, az, bx, by, bz, color, width);
      } else {
         double dx = (bx - ax) * 0.25;
         double dy = (by - ay) * 0.25;
         double dz = (bz - az) * 0.25;
         WorldLineRenderer.init(pose, buffer, ax, ay, az, ax + dx, ay + dy, az + dz, color, width);
         WorldLineRenderer.init(pose, buffer, bx, by, bz, bx - dx, by - dy, bz - dz, color, width);
      }
   }

   private static int lerpColor(int from, int to, float t) {
      float clamped = Math.max(0.0F, Math.min(1.0F, t));
      int ar = from >> 16 & 0xFF;
      int ag = from >> 8 & 0xFF;
      int ab = from & 0xFF;
      int aa = from >>> 24 & 0xFF;
      int br = to >> 16 & 0xFF;
      int bg = to >> 8 & 0xFF;
      int bb = to & 0xFF;
      int ba = to >>> 24 & 0xFF;
      int r = Math.round(ar + (br - ar) * clamped);
      int g = Math.round(ag + (bg - ag) * clamped);
      int b = Math.round(ab + (bb - ab) * clamped);
      int a = Math.round(aa + (ba - aa) * clamped);
      return a << 24 | r << 16 | g << 8 | b;
   }

   private static void drawFill(Pose pose, VertexConsumer buffer, AABB box, int color) {
      drawQuad(pose, buffer, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, box.minX, box.minY, box.maxZ, color);
      drawQuad(pose, buffer, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.maxX, box.maxY, box.minZ, box.minX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, box.minX, box.maxY, box.maxZ, color);
      drawQuad(pose, buffer, box.maxX, box.minY, box.minZ, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, box.minX, box.maxY, box.minZ, color);
      drawQuad(pose, buffer, box.maxX, box.minY, box.maxZ, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color);
   }

   private static void drawQuad(
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
