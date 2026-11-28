package autismclient.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class BlockPreviewRenderer {
   private static volatile BlockPos previewPos;
   private static volatile String previewShape = "Lines";
   private static volatile int lineColor = -50373;
   private static volatile int sideColor = 0;
   private static final float init_F = 2.0F;

   private BlockPreviewRenderer() {
   }

   public static void init() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (!autismclient.modules.aD.read_Z() && (AutismLiteVariant.enabled() || autismclient.e.ListPool.init())) {
                  BlockPos pos = previewPos;
                  String shape = previewShape;
                  int renderColor = lineColor;
                  int renderSideColor = sideColor;
                  AutismSessionState state = AutismSessionState.getInstance();
                  if (state.isPlacePreviewActive()) {
                     Minecraft mc = Minecraft.getInstance();
                     if (mc != null && mc.level != null && mc.player != null && mc.hitResult instanceof BlockHitResult bhr) {
                        BlockPos support = bhr.getBlockPos();
                        Direction face = bhr.getDirection() == null ? Direction.UP : bhr.getDirection();
                        BlockPos preview = support.relative(face);
                        if (!mc.level.isOutsideBuildHeight(preview)) {
                           pos = preview;
                           shape = "Lines";
                           renderColor = -50373;
                           renderSideColor = 0;
                        }
                     }
                  }

                  if (pos != null) {
                     Vec3 camera = context.levelState().cameraRenderState.pos;
                     AABB box = new AABB(pos).move(-camera.x, -camera.y, -camera.z);
                     PoseStack poseStack = context.poseStack();
                     String finalShape = shape;
                     int finalColor = renderColor;
                     int finalSideColor = renderSideColor;
                     if (drawsSides(finalShape) && (finalSideColor >>> 24 & 0xFF) > 0) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(), (pose, buffer) -> drawBoxFaces(pose, buffer, box, finalSideColor)
                           );
                     }

                     if (drawsLines(finalShape)) {
                        context.submitNodeCollector()
                           .submitCustomGeometry(
                              poseStack, autismclient.render.AutismRenderTypes.storageEspLinesSeeThrough(), (pose, buffer) -> drawBoxEdges(pose, buffer, box, finalColor)
                           );
                     }
                  }
               }
            }
         );
   }

   public static void init(BlockPos pos) {
      previewPos = pos == null ? null : pos.immutable();
      previewShape = "Lines";
      lineColor = -50373;
      sideColor = 0;
   }

   public static void setPreview(BlockPos pos, String shape, int line, int side) {
      previewPos = pos == null ? null : pos.immutable();
      previewShape = shape == null ? "Lines" : shape;
      lineColor = line;
      sideColor = side;
   }

   public static void init(BlockPos pos, int color) {
      previewPos = pos == null ? null : pos.immutable();
      previewShape = "Lines";
      lineColor = color;
      sideColor = 0;
   }

   public static void load(BlockPos pos) {
      BlockPos current = previewPos;
      if (current == null || pos == null || current.equals(pos)) {
         previewPos = null;
      }
   }

   public static void load() {
      previewPos = null;
   }

   public static void read() {
   }

   private static void drawBoxEdges(Pose pose, VertexConsumer buffer, AABB box, int color) {
      double x1 = box.minX;
      double y1 = box.minY;
      double z1 = box.minZ;
      double x2 = box.maxX;
      double y2 = box.maxY;
      double z2 = box.maxZ;
      drawLine(pose, buffer, x1, y1, z1, x2, y1, z1, color);
      drawLine(pose, buffer, x2, y1, z1, x2, y1, z2, color);
      drawLine(pose, buffer, x2, y1, z2, x1, y1, z2, color);
      drawLine(pose, buffer, x1, y1, z2, x1, y1, z1, color);
      drawLine(pose, buffer, x1, y2, z1, x2, y2, z1, color);
      drawLine(pose, buffer, x2, y2, z1, x2, y2, z2, color);
      drawLine(pose, buffer, x2, y2, z2, x1, y2, z2, color);
      drawLine(pose, buffer, x1, y2, z2, x1, y2, z1, color);
      drawLine(pose, buffer, x1, y1, z1, x1, y2, z1, color);
      drawLine(pose, buffer, x2, y1, z1, x2, y2, z1, color);
      drawLine(pose, buffer, x2, y1, z2, x2, y2, z2, color);
      drawLine(pose, buffer, x1, y1, z2, x1, y2, z2, color);
   }

   private static void drawLine(Pose pose, VertexConsumer buffer, double x1, double y1, double z1, double x2, double y2, double z2, int color) {
      WorldLineRenderer.init(pose, buffer, x1, y1, z1, x2, y2, z2, color, 2.0F);
   }

   private static void drawBoxFaces(Pose pose, VertexConsumer buffer, AABB box, int color) {
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

   private static boolean drawsLines(String shape) {
      return !"Sides".equalsIgnoreCase(shape);
   }

   private static boolean drawsSides(String shape) {
      return "Both".equalsIgnoreCase(shape) || "Sides".equalsIgnoreCase(shape);
   }
}
