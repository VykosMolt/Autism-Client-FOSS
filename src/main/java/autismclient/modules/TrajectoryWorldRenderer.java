package autismclient.modules;

import autismclient.util.AutismBufferSource;
import autismclient.util.WorldLineRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import java.util.LinkedHashSet;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Vector3f;

public final class TrajectoryWorldRenderer {
   private static final int init_I = -44976;
   private static final long[] frameNanos = new long[64];
   private static int frameCursor;
   private static int frameSamples;
   private static int segmentCount;
   private static int markerCount;
   private static int ghostCount;
   private static int blastBlockCount;
   private static int hitTargetCount;
   private static int rememberedGhostShown;
   private static int rememberedPearlId = -1;
   private static int heldGhostTint;

   public static int init_I() {
      return heldGhostTint;
   }

   private TrajectoryWorldRenderer() {
   }

   public static void resetStats() {
      rememberedGhostShown = 0;
      hitTargetCount = 0;
      blastBlockCount = 0;
      ghostCount = 0;
      markerCount = 0;
      segmentCount = 0;
      frameSamples = 0;
      frameCursor = 0;
   }

   public static double averageFrameMs() {
      long sum = 0L;

      for (int i = 0; i < frameSamples; i++) {
         sum += frameNanos[i];
      }

      return frameSamples == 0 ? 0.0 : sum / (frameSamples * 1000000.0);
   }

   public static int load_I() {
      return segmentCount;
   }

   public static int lastMarkerCount() {
      return markerCount;
   }

   public static int lastGhostCount() {
      return ghostCount;
   }

   public static int lastBlastBlockCount() {
      return blastBlockCount;
   }

   public static int lastHitTargetCount() {
      return hitTargetCount;
   }

   public static int lastRememberedGhostShown() {
      return rememberedGhostShown;
   }

   public static int lastRememberedPearlId() {
      return rememberedPearlId;
   }

   public static double load_D() {
      return TrajectoryStateCache.init_D();
   }

   public static void resetCounters() {
      heldGhostTint = 0;
      rememberedGhostShown = 0;
      hitTargetCount = 0;
      blastBlockCount = 0;
      ghostCount = 0;
      markerCount = 0;
      segmentCount = 0;
      rememberedPearlId = -1;
   }

   public static void render(PoseStack matrices, AutismBufferSource buffers, Vec3 camera, List<TrajectoriesModule.TrajectoryPath> paths) {
      Minecraft mc = Minecraft.getInstance();
      CameraRenderState state = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
      if (state.initialized && mc.level != null && mc.player != null && mc.getWindow().getHeight() >= 1) {
         long start = System.nanoTime();
         resetCounters();
         Pose pose = matrices.last();
         Vec3 forward = new Vec3(state.orientation.transform(new Vector3f(0.0F, 0.0F, -1.0F)));
         float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         double near = mc.options.getCameraType().isFirstPerson() && camera.distanceToSqr(mc.player.getEyePosition(partial)) < 0.25 ? 0.75 : 0.06;
         List<FireballBlastPredictor.DestroyedBlock> blocks = TrajectoryStateCache.blastPredictions(paths);
         blastBlockCount = blocks.size();
         ThrownPearlTracker.PearlForecast remembered = TrajectoryStateCache.rememberedPearl();
         if (remembered != null) {
            rememberedPearlId = remembered.init_I();
         }

         for (int pass = 0; pass < 2; pass++) {
            boolean hidden = pass == 0;
            double opacity = hidden ? 0.78 : 1.0;
            VertexConsumer lines = buffers.buffer(
               hidden ? autismclient.render.AutismRenderTypes.trajectoryPath(true) : autismclient.render.AutismRenderTypes.trajectoryPath(false)
            );

            for (TrajectoriesModule.TrajectoryPath path : paths) {
               renderPath(pose, lines, path, camera, forward, near, opacity);
            }

            VertexConsumer markers = buffers.buffer(
               hidden ? autismclient.render.AutismRenderTypes.trajectoryImpact(true) : autismclient.render.AutismRenderTypes.trajectoryImpact(false)
            );

            for (TrajectoriesModule.TrajectoryPath path : paths) {
               if (path.surface() != null) {
                  drawPath(pose, markers, path, camera, opacity);
               }
            }

            if (remembered != null) {
               drawRing(
                  pose, markers, remembered.position().add(0.0, 0.012, 0.0), new Vec3(0.0, 1.0, 0.0), 0.35, camera, withAlpha(-8329771, (int)(245.0 * opacity)), 17
               );
            }
         }

         LinkedHashSet<Entity> hitTargets = new LinkedHashSet<>();

         for (TrajectoriesModule.TrajectoryPath path : paths) {
            if (path.hit() instanceof EntityHitResult hit
               && isEntityHitItem(path.init_aS_StreamUtil())
               && hit.getEntity().isAlive()
               && !hit.getEntity().isRemoved()) {
               hitTargets.add(hit.getEntity());
            }
         }

         hitTargetCount = hitTargets.size();
         if (!hitTargets.isEmpty()) {
            VertexConsumer fill = buffers.buffer(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());

            for (Entity entity : hitTargets) {
               WorldLineRenderer.box(pose, fill, entityBox(entity, camera, partial, 0.016), withAlpha(-44976, 72));
            }

            VertexConsumer edge = buffers.buffer(autismclient.render.AutismRenderTypes.aiPlanLines());

            for (Entity entity : hitTargets) {
               AABB box = entityBox(entity, camera, partial, 0.018);
               WorldLineRenderer.box(pose, edge, box, withAlpha(-44976, 48), 7.0F);
               WorldLineRenderer.box(pose, edge, box, -653585893, 4.8F);
               WorldLineRenderer.box(pose, edge, box, withAlpha(-44976, 255), 3.0F);
            }
         }

         if (!blocks.isEmpty()) {
            VertexConsumer blastLines = buffers.buffer(autismclient.render.AutismRenderTypes.aiPlanLines());
            PearlGhostRenderer.drawBlastOutlines(pose, blastLines, blocks, camera);
            VertexConsumer fill = buffers.buffer(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());
            PearlGhostRenderer.drawBlastFills(pose, fill, blocks, camera);
         }

         Vec3 rememberedGhost = remembered == null ? null : remembered.position();
         Vec3 heldGhost = null;

         for (TrajectoriesModule.TrajectoryPath path : paths) {
            if (path.load_Vec3() != null) {
               heldGhost = path.load_Vec3();
               break;
            }
         }

         if (rememberedGhost != null) {
            int tint = TrajectoryStateCache.tintFor(rememberedGhost);
            ghostCount++;
            rememberedGhostShown = 1;
            PearlGhostRenderer.drawGhost(mc, matrices, buffers, mc.player, rememberedGhost, camera, partial, tint, 0.58, 2);
            PearlGhostRenderer.drawGhost(mc, matrices, buffers, mc.player, rememberedGhost, camera, partial, tint, 0.68, 1);
         }

         if (heldGhost != null && (rememberedGhost == null || heldGhost.distanceToSqr(rememberedGhost) > 0.25)) {
            int tint = TrajectoryStateCache.tintFor(heldGhost);
            heldGhostTint = tint;
            ghostCount++;
            PearlGhostRenderer.drawGhost(mc, matrices, buffers, mc.player, heldGhost, camera, partial, tint, 0.58, 2);
            PearlGhostRenderer.drawGhost(mc, matrices, buffers, mc.player, heldGhost, camera, partial, tint, 0.68, 1);
         }

         frameNanos[frameCursor] = System.nanoTime() - start;
         frameCursor = (frameCursor + 1) % frameNanos.length;
         frameSamples = Math.min(frameSamples + 1, frameNanos.length);
      }
   }

   private static void drawPath(Pose pose, VertexConsumer buffer, TrajectoriesModule.TrajectoryPath path, Vec3 camera, double opacityScale) {
      TrajectoriesModule.Impact surface = path.surface();
      Vec3 normal = surface.normal();
      int var10000;
      switch (path.init_aS_StreamUtil()) {
         case SPLASH:
            var10000 = surface.load_Z() ? 15 : 14;
            break;
         case LINGERING:
            var10000 = surface.load_Z() ? (surface.init_D() >= 4.0 ? 15 : 16) : 14;
            break;
         case PEARL:
            var10000 = 17;
            break;
         case FISHING:
            var10000 = surface.init_Z() ? 18 : 14;
            break;
         case WIND:
            var10000 = 19;
            break;
         case FIRE:
            var10000 = 20;
            break;
         default:
            var10000 = path.init_Z() ? 20 : 14;
      }

      int style = var10000;
      int opacity = surface.load_Z() ? 240 : (path.init_aS_StreamUtil() == TrajectoriesModule.ProjectileItem.PEARL ? 225 : 230);
      if (!path.load_Z()) {
         opacity = (int)(opacity * 0.86);
      }

      Vec3 center = path.load_Vec3() == null ? surface.point() : path.load_Vec3().add(0.0, 0.012, 0.0);
      if (path.load_Vec3() != null) {
         normal = new Vec3(0.0, 1.0, 0.0);
      }

      drawRing(pose, buffer, center, normal, surface.init_D(), camera, withAlpha(hitColor(path), (int)(opacity * opacityScale)), style);
   }

   private static void drawRing(Pose pose, VertexConsumer buffer, Vec3 center, Vec3 normal, double radius, Vec3 camera, int color, int style) {
      Vec3 tangent = Math.abs(normal.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : normal.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      Vec3 bitangent = normal.cross(tangent).normalize();
      double extent = radius / 0.86;
      quad(pose, buffer, center.subtract(camera), tangent.scale(extent), bitangent.scale(extent), color, style);
   }

   private static int hitColor(TrajectoriesModule.TrajectoryPath path) {
      return path.hit() != null && path.hit().getType() == Type.ENTITY ? -44976 : path.color();
   }

   private static boolean isEntityHitItem(TrajectoriesModule.ProjectileItem item) {
      return item == TrajectoriesModule.ProjectileItem.BOW
         || item == TrajectoriesModule.ProjectileItem.CROSSBOW
         || item == TrajectoriesModule.ProjectileItem.TRIDENT
         || item == TrajectoriesModule.ProjectileItem.SNOWBALL
         || item == TrajectoriesModule.ProjectileItem.EGG;
   }

   private static AABB entityBox(Entity entity, Vec3 camera, float partial, double margin) {
      return entity.getBoundingBox().inflate(margin).move(entity.getPosition(partial).subtract(entity.position()).subtract(camera));
   }

   private static Vec3 easedPoint(TrajectoriesModule.TrajectoryPath path, int index) {
      int ease = Math.min(2, path.points().size() - 1);
      return index < ease ? path.points().get(index).add(path.init_Vec3().scale(1.0 - (double)index / ease)) : path.points().get(index);
   }

   private static void renderPath(Pose pose, VertexConsumer buffer, TrajectoriesModule.TrajectoryPath path, Vec3 camera, Vec3 forward, double near, double opacity) {
      if (path.points().size() >= 2) {
         Vec3 previous = easedPoint(path, 0).subtract(camera);
         int count = path.points().size() - 1;

         for (int i = 1; i <= count; i++) {
            Vec3 next = easedPoint(path, i).subtract(camera);
            double fade = path.hit() == null ? Math.min(1.0, (count + 1 - i) / 12.0) : 1.0;
            fade *= opacity * (path.load_Z() ? 1.0 : 0.86);
            renderSegment(pose, buffer, previous, next, forward, near, hitColor(path), fade, (double)i / count, path.load_Z());
            previous = next;
         }
      }
   }

   private static void renderSegment(
      Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vec3 forward, double near, int tint, double fade, double progress, boolean primary
   ) {
      double da = a.dot(forward);
      double db = b.dot(forward);
      if (!(da <= near) || !(db <= near)) {
         if (da < near) {
            a = a.lerp(b, (near - da) / (db - da));
         } else if (db < near) {
            b = b.lerp(a, (near - db) / (da - db));
         }

         Vec3 delta = b.subtract(a);
         if (!(delta.lengthSqr() < 1.0E-10)) {
            Vector3f normal = delta.normalize().toVector3f();
            segmentCount++;
            float width = primary ? 2.35F : 1.95F;
            double fadeA = Math.clamp((a.dot(forward) - near) / 0.45, 0.0, 1.0);
            double fadeB = Math.clamp((b.dot(forward) - near) / 0.45, 0.0, 1.0);
            ribbonLayer(pose, buffer, a, b, normal, tint, 28.0 * fade, fadeA, fadeB, width + 3.5F);
            ribbonLayer(pose, buffer, a, b, normal, -16182498, 115.0 * fade, fadeA, fadeB, width + 1.6F);
            ribbonLayer(pose, buffer, a, b, normal, tint, (200.0 + progress * 40.0) * fade, fadeA, fadeB, width);
         }
      }
   }

   private static void ribbonLayer(
      Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vector3f normal, int tint, double opacity, double fadeA, double fadeB, float width
   ) {
      buffer.addVertex(pose, (float)a.x, (float)a.y, (float)a.z).setColor(withAlpha(tint, (int)(opacity * fadeA))).setNormal(pose, normal).setLineWidth(width);
      buffer.addVertex(pose, (float)b.x, (float)b.y, (float)b.z).setColor(withAlpha(tint, (int)(opacity * fadeB))).setNormal(pose, normal).setLineWidth(width);
   }

   private static int withAlpha(int color, int alpha) {
      return color & 16777215 | Math.clamp(alpha, 0, 255) << 24;
   }

   private static void quad(Pose pose, VertexConsumer buffer, Vec3 at, Vec3 right, Vec3 up, int color, int style) {
      markerCount++;
      vertex(pose, buffer, at.subtract(right).subtract(up), 0.0F, 0.0F, color, style);
      vertex(pose, buffer, at.add(right).subtract(up), 1.0F, 0.0F, color, style);
      vertex(pose, buffer, at.add(right).add(up), 1.0F, 1.0F, color, style);
      vertex(pose, buffer, at.subtract(right).add(up), 0.0F, 1.0F, color, style);
   }

   private static void vertex(Pose pose, VertexConsumer buffer, Vec3 at, float u, float v, int color, int style) {
      buffer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setUv(u + style * 2, v).setColor(color);
   }
}
