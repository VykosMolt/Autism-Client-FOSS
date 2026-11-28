package autismclient.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class PlacedBlockTrailRenderer {
   private static final int init_I = 32;
   private static final long init_J = 320000000L;
   private static final double init_D = 0.62;
   private static final double load_D = 0.0035;
   private static final float init_F = 0.55F;
   private static final int[] XS = new int[32];
   private static final int[] YS = new int[32];
   private static final int[] trailZ = new int[32];
   private static final long[] PLACED_AT = new long[32];
   private static final PlacedBlockTrailRenderer.Inner_DataUtil[] SOURCES = new PlacedBlockTrailRenderer.Inner_DataUtil[32];
   private static final int[] trailColor = new int[32];
   private static final boolean[] CUSTOM_COLOR = new boolean[32];
   private static final PlacedBlockTrailRenderer.Inner_StreamUtil[] STYLES = new PlacedBlockTrailRenderer.Inner_StreamUtil[]{
      new PlacedBlockTrailRenderer.Inner_StreamUtil(false, false, -50373), new PlacedBlockTrailRenderer.Inner_StreamUtil(false, false, -50373)
   };
   private static int nextIndex;

   private PlacedBlockTrailRenderer() {
   }

   public static void configure(boolean enabled, boolean custom, int customArgb) {
      configure(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD, enabled, custom, customArgb);
   }

   public static void configure(PlacedBlockTrailRenderer.Inner_DataUtil source, boolean enabled, boolean custom, int customArgb) {
      STYLES[source.ordinal()] = new PlacedBlockTrailRenderer.Inner_StreamUtil(enabled, custom, customArgb);
      if (!enabled) {
         clearSource(source);
      }
   }

   public static void recordScaffoldPlacement() {
      recordPlacement(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD);
   }

   public static void recordPlacement(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      PlacedBlockTrailRenderer.Inner_StreamUtil style = STYLES[source.ordinal()];
      configure(source, false, style.load(), style.argb());
   }

   public static void clearSource(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      for (int i = 0; i < 32; i++) {
         if (SOURCES[i] == source) {
            PLACED_AT[i] = 0L;
         }
      }
   }

   public static void mark(BlockPos pos) {
      mark(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD, pos);
   }

   public static void mark(PlacedBlockTrailRenderer.Inner_DataUtil source, BlockPos pos) {
      PlacedBlockTrailRenderer.Inner_StreamUtil style = STYLES[source.ordinal()];
      if (style.enabled() && pos != null) {
         XS[nextIndex] = pos.getX();
         YS[nextIndex] = pos.getY();
         trailZ[nextIndex] = pos.getZ();
         PLACED_AT[nextIndex] = System.nanoTime();
         SOURCES[nextIndex] = source;
         trailColor[nextIndex] = style.argb() | 0xFF000000;
         CUSTOM_COLOR[nextIndex] = style.load();
         nextIndex = (nextIndex + 1) % 32;
      }
   }

   public static boolean init_Z() {
      if (!AutismLiteVariant.enabled() && !autismclient.e.ListPool.init()) {
         return false;
      }

      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      long now = System.nanoTime();

      for (long at : PLACED_AT) {
         if (at != 0L && now >= at && now - at < 320000000L) {
            return true;
         }
      }

      return false;
   }

   static long init_J(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      long latest = 0L;

      for (int i = 0; i < 32; i++) {
         if (SOURCES[i] == source) {
            latest = Math.max(latest, PLACED_AT[i]);
         }
      }

      return latest;
   }

   public static void register() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (init_Z()) {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc != null && mc.level != null) {
                     long now = System.nanoTime();
                     int accent = ThemePaletteManager.recolor(-50373, ThemePaletteManager.ColorRole.ACCENT);
                     Vec3 origin = context.levelState().cameraRenderState.pos;
                     context.submitNodeCollector()
                        .submitCustomGeometry(
                           context.poseStack(),
                           autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                           (pose, buffer) -> {
                              for (int i = 0; i < 32; i++) {
                                 long t = PLACED_AT[i];
                                 if (t != 0L) {
                                    long age = now - t;
                                    if (age >= 0L && age < 320000000L) {
                                       drawBlock(
                                          pose,
                                          buffer,
                                          XS[i] - origin.x,
                                          YS[i] - origin.y,
                                          trailZ[i] - origin.z,
                                          age / 3.2E8,
                                          CUSTOM_COLOR[i] ? trailColor[i] : accent
                                       );
                                    }
                                 }
                              }
                           }
                        );
                  }
               }
            }
         );
   }

   private static void drawBlock(Pose pose, VertexConsumer buffer, double bx, double by, double bz, double t, int argb) {
      double inv = 1.0 - t;
      double scale = 1.0 - inv * inv * inv;
      double fade = t < 0.62 ? 1.0 : 1.0 - (t - 0.62) / 0.38;
      int alpha = (int)(0.550000011920929 * fade * 255.0);
      if (alpha > 0) {
         int c = argb & 16777215 | alpha << 24;
         double half = 0.5 * scale + 0.0035;
         double x1 = bx + 0.5 - half;
         double y1 = by + 0.5 - half;
         double z1 = bz + 0.5 - half;
         double x2 = bx + 0.5 + half;
         double y2 = by + 0.5 + half;
         double z2 = bz + 0.5 + half;
         drawQuad(pose, buffer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, c);
         drawQuad(pose, buffer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, c);
         drawQuad(pose, buffer, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, c);
         drawQuad(pose, buffer, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, c);
         drawQuad(pose, buffer, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, c);
         drawQuad(pose, buffer, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, c);
      }
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

   public enum Inner_DataUtil {
      SCAFFOLD,
      CLUTCH;
   }

   private record Inner_StreamUtil(boolean enabled, boolean load, int argb) {
   }
}
