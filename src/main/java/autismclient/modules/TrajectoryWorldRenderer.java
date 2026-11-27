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
   private static final long[] init_JArr = new long[64];
   private static int load;
   private static int read;
   private static int write;
   private static int update;
   private static int sync;
   private static int flush;
   private static int close;
   private static int open;
   private static int poll = -1;
   private static int push;

   public static int init_I() {
      return push;
   }

   private TrajectoryWorldRenderer() {
   }

   public static void init_V() {
      open = 0;
      close = 0;
      flush = 0;
      sync = 0;
      update = 0;
      write = 0;
      read = 0;
      load = 0;
   }

   public static double init_D() {
      long sum = 0L;

      for (int i = 0; i < read; i++) {
         sum += init_JArr[i];
      }

      return read == 0 ? 0.0 : sum / (read * 1000000.0);
   }

   public static int load_I() {
      return write;
   }

   public static int read() {
      return update;
   }

   public static int write() {
      return sync;
   }

   public static int update() {
      return flush;
   }

   public static int sync() {
      return close;
   }

   public static int flush() {
      return open;
   }

   public static int close() {
      return poll;
   }

   public static double load_D() {
      return TrajectoryStateCache.init_D();
   }

   public static void resetCounters() {
      push = 0;
      open = 0;
      close = 0;
      flush = 0;
      sync = 0;
      update = 0;
      write = 0;
      poll = -1;
   }

   public static void init(PoseStack matrices, AutismBufferSource buffers, Vec3 camera, List<TrajectoriesModule.Inner_CachePool> paths) {
      Minecraft mc = Minecraft.getInstance();
      CameraRenderState state = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
      if (state.initialized && mc.level != null && mc.player != null && mc.getWindow().getHeight() >= 1) {
         long start = System.nanoTime();
         resetCounters();
         Pose pose = matrices.last();
         Vec3 forward = new Vec3(state.orientation.transform(new Vector3f(0.0F, 0.0F, -1.0F)));
         float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         double near = mc.options.getCameraType().isFirstPerson() && camera.distanceToSqr(mc.player.getEyePosition(partial)) < 0.25 ? 0.75 : 0.06;
         List<FireballBlastPredictor.Inner_DataUtil> blocks = TrajectoryStateCache.init(paths);
         flush = blocks.size();
         ThrownPearlTracker.Inner_DataUtil remembered = TrajectoryStateCache.rememberedPearl();
         if (remembered != null) {
            poll = remembered.init_I();
         }

         for (int pass = 0; pass < 2; pass++) {
            boolean hidden = pass == 0;
            double opacity = hidden ? 0.78 : 1.0;
            VertexConsumer lines = buffers.init(
               hidden ? autismclient.render.AutismRenderTypes.trajectoryPath(true) : autismclient.render.AutismRenderTypes.trajectoryPath(false)
            );

            for (TrajectoriesModule.Inner_CachePool path : paths) {
               init_V_PoseStack_Pose(pose, lines, path, camera, forward, near, opacity);
            }

            VertexConsumer markers = buffers.init(
               hidden ? autismclient.render.AutismRenderTypes.trajectoryImpact(true) : autismclient.render.AutismRenderTypes.trajectoryImpact(false)
            );

            for (TrajectoriesModule.Inner_CachePool path : paths) {
               if (path.surface() != null) {
                  init(pose, markers, path, camera, opacity);
               }
            }

            if (remembered != null) {
               init(
                  pose, markers, remembered.position().add(0.0, 0.012, 0.0), new Vec3(0.0, 1.0, 0.0), 0.35, camera, init(-8329771, (int)(245.0 * opacity)), 17
               );
            }
         }

         LinkedHashSet<Entity> hitTargets = new LinkedHashSet<>();

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.hit() instanceof EntityHitResult hit
               && init(path.init_aS_StreamUtil())
               && hit.getEntity().isAlive()
               && !hit.getEntity().isRemoved()) {
               hitTargets.add(hit.getEntity());
            }
         }

         close = hitTargets.size();
         if (!hitTargets.isEmpty()) {
            VertexConsumer fill = buffers.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());

            for (Entity entity : hitTargets) {
               WorldLineRenderer.init(pose, fill, init(entity, camera, partial, 0.016), init(-44976, 72));
            }

            VertexConsumer edge = buffers.init(autismclient.render.AutismRenderTypes.aiPlanLines());

            for (Entity entity : hitTargets) {
               AABB box = init(entity, camera, partial, 0.018);
               WorldLineRenderer.init(pose, edge, box, init(-44976, 48), 7.0F);
               WorldLineRenderer.init(pose, edge, box, -653585893, 4.8F);
               WorldLineRenderer.init(pose, edge, box, init(-44976, 255), 3.0F);
            }
         }

         if (!blocks.isEmpty()) {
            VertexConsumer blastLines = buffers.init(autismclient.render.AutismRenderTypes.aiPlanLines());
            PearlGhostRenderer.init(pose, blastLines, blocks, camera);
            VertexConsumer fill = buffers.init(autismclient.render.AutismRenderTypes.storageEspFillSeeThrough());
            PearlGhostRenderer.load(pose, fill, blocks, camera);
         }

         Vec3 rememberedGhost = remembered == null ? null : remembered.position();
         Vec3 heldGhost = null;

         for (TrajectoriesModule.Inner_CachePool path : paths) {
            if (path.load_Vec3() != null) {
               heldGhost = path.load_Vec3();
               break;
            }
         }

         if (rememberedGhost != null) {
            int tint = TrajectoryStateCache.init(rememberedGhost);
            sync++;
            open = 1;
            PearlGhostRenderer.init(mc, matrices, buffers, mc.player, rememberedGhost, camera, partial, tint, 0.58, 2);
            PearlGhostRenderer.init(mc, matrices, buffers, mc.player, rememberedGhost, camera, partial, tint, 0.68, 1);
         }

         if (heldGhost != null && (rememberedGhost == null || heldGhost.distanceToSqr(rememberedGhost) > 0.25)) {
            int tint = TrajectoryStateCache.init(heldGhost);
            push = tint;
            sync++;
            PearlGhostRenderer.init(mc, matrices, buffers, mc.player, heldGhost, camera, partial, tint, 0.58, 2);
            PearlGhostRenderer.init(mc, matrices, buffers, mc.player, heldGhost, camera, partial, tint, 0.68, 1);
         }

         init_JArr[load] = System.nanoTime() - start;
         load = (load + 1) % init_JArr.length;
         read = Math.min(read + 1, init_JArr.length);
      }
   }

   private static void init(Pose pose, VertexConsumer buffer, TrajectoriesModule.Inner_CachePool path, Vec3 camera, double opacityScale) {
      TrajectoriesModule.Inner_BufferHelper surface = path.surface();
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
      int opacity = surface.load_Z() ? 240 : (path.init_aS_StreamUtil() == TrajectoriesModule.Inner_StreamUtil.PEARL ? 225 : 230);
      if (!path.load_Z()) {
         opacity = (int)(opacity * 0.86);
      }

      Vec3 center = path.load_Vec3() == null ? surface.point() : path.load_Vec3().add(0.0, 0.012, 0.0);
      if (path.load_Vec3() != null) {
         normal = new Vec3(0.0, 1.0, 0.0);
      }

      init(pose, buffer, center, normal, surface.init_D(), camera, init(init(path), (int)(opacity * opacityScale)), style);
   }

   private static void init(Pose pose, VertexConsumer buffer, Vec3 center, Vec3 normal, double radius, Vec3 camera, int color, int style) {
      Vec3 tangent = Math.abs(normal.y) > 0.9 ? new Vec3(1.0, 0.0, 0.0) : normal.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      Vec3 bitangent = normal.cross(tangent).normalize();
      double extent = radius / 0.86;
      init_V_PoseStack_Pose_3(pose, buffer, center.subtract(camera), tangent.scale(extent), bitangent.scale(extent), color, style);
   }

   private static int init(TrajectoriesModule.Inner_CachePool path) {
      return path.hit() != null && path.hit().getType() == Type.ENTITY ? -44976 : path.color();
   }

   private static boolean init(TrajectoriesModule.Inner_StreamUtil item) {
      return item == TrajectoriesModule.Inner_StreamUtil.BOW
         || item == TrajectoriesModule.Inner_StreamUtil.CROSSBOW
         || item == TrajectoriesModule.Inner_StreamUtil.TRIDENT
         || item == TrajectoriesModule.Inner_StreamUtil.SNOWBALL
         || item == TrajectoriesModule.Inner_StreamUtil.EGG;
   }

   private static AABB init(Entity entity, Vec3 camera, float partial, double margin) {
      return entity.getBoundingBox().inflate(margin).move(entity.getPosition(partial).subtract(entity.position()).subtract(camera));
   }

   private static Vec3 init(TrajectoriesModule.Inner_CachePool path, int index) {
      int ease = Math.min(2, path.points().size() - 1);
      return index < ease ? path.points().get(index).add(path.init_Vec3().scale(1.0 - (double)index / ease)) : path.points().get(index);
   }

   private static void init_V_PoseStack_Pose(Pose pose, VertexConsumer buffer, TrajectoriesModule.Inner_CachePool path, Vec3 camera, Vec3 forward, double near, double opacity) {
      if (path.points().size() >= 2) {
         Vec3 previous = init(path, 0).subtract(camera);
         int count = path.points().size() - 1;

         for (int i = 1; i <= count; i++) {
            Vec3 next = init(path, i).subtract(camera);
            double fade = path.hit() == null ? Math.min(1.0, (count + 1 - i) / 12.0) : 1.0;
            fade *= opacity * (path.load_Z() ? 1.0 : 0.86);
            init_V_PoseStack_Pose(pose, buffer, previous, next, forward, near, init(path), fade, (double)i / count, path.load_Z());
            previous = next;
         }
      }
   }

   private static void init_V_PoseStack_Pose(
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
            write++;
            float width = primary ? 2.35F : 1.95F;
            double fadeA = Math.clamp((a.dot(forward) - near) / 0.45, 0.0, 1.0);
            double fadeB = Math.clamp((b.dot(forward) - near) / 0.45, 0.0, 1.0);
            init_V_PoseStack_Pose_2(pose, buffer, a, b, normal, tint, 28.0 * fade, fadeA, fadeB, width + 3.5F);
            init_V_PoseStack_Pose_2(pose, buffer, a, b, normal, -16182498, 115.0 * fade, fadeA, fadeB, width + 1.6F);
            init_V_PoseStack_Pose_2(pose, buffer, a, b, normal, tint, (200.0 + progress * 40.0) * fade, fadeA, fadeB, width);
         }
      }
   }

   private static void init_V_PoseStack_Pose_2(
      Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, Vector3f normal, int tint, double opacity, double fadeA, double fadeB, float width
   ) {
      buffer.addVertex(pose, (float)a.x, (float)a.y, (float)a.z).setColor(init(tint, (int)(opacity * fadeA))).setNormal(pose, normal).setLineWidth(width);
      buffer.addVertex(pose, (float)b.x, (float)b.y, (float)b.z).setColor(init(tint, (int)(opacity * fadeB))).setNormal(pose, normal).setLineWidth(width);
   }

   private static int init(int color, int alpha) {
      return color & 16777215 | Math.clamp(alpha, 0, 255) << 24;
   }

   private static void init_V_PoseStack_Pose_3(Pose pose, VertexConsumer buffer, Vec3 at, Vec3 right, Vec3 up, int color, int style) {
      update++;
      init_V_PoseStack_Pose_2(pose, buffer, at.subtract(right).subtract(up), 0.0F, 0.0F, color, style);
      init_V_PoseStack_Pose_2(pose, buffer, at.add(right).subtract(up), 1.0F, 0.0F, color, style);
      init_V_PoseStack_Pose_2(pose, buffer, at.add(right).add(up), 1.0F, 1.0F, color, style);
      init_V_PoseStack_Pose_2(pose, buffer, at.subtract(right).add(up), 0.0F, 1.0F, color, style);
   }

   private static void init_V_PoseStack_Pose_2(Pose pose, VertexConsumer buffer, Vec3 at, float u, float v, int color, int style) {
      buffer.addVertex(pose, (float)at.x, (float)at.y, (float)at.z).setUv(u + style * 2, v).setColor(color);
   }
}
