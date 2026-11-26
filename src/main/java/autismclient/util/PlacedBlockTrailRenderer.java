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
   private static final int[] init_IArr = new int[32];
   private static final int[] load_IArr = new int[32];
   private static final int[] read = new int[32];
   private static final long[] init_JArr = new long[32];
   private static final PlacedBlockTrailRenderer.Inner_DataUtil[] init_cg_DataUtilArr = new PlacedBlockTrailRenderer.Inner_DataUtil[32];
   private static final int[] write = new int[32];
   private static final boolean[] init_ZArr = new boolean[32];
   private static final PlacedBlockTrailRenderer.Inner_StreamUtil[] init_cg_StreamUtilArr = new PlacedBlockTrailRenderer.Inner_StreamUtil[]{
      new PlacedBlockTrailRenderer.Inner_StreamUtil(false, false, -50373), new PlacedBlockTrailRenderer.Inner_StreamUtil(false, false, -50373)
   };
   private static int load_I;

   private PlacedBlockTrailRenderer() {
   }

   public static void init(boolean enabled, boolean custom, int customArgb) {
      init(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD, enabled, custom, customArgb);
   }

   public static void init(PlacedBlockTrailRenderer.Inner_DataUtil source, boolean enabled, boolean custom, int customArgb) {
      init_cg_StreamUtilArr[source.ordinal()] = new PlacedBlockTrailRenderer.Inner_StreamUtil(enabled, custom, customArgb);
      if (!enabled) {
         load(source);
      }
   }

   public static void init_V() {
      init_V(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD);
   }

   public static void init_V(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      PlacedBlockTrailRenderer.Inner_StreamUtil style = init_cg_StreamUtilArr[source.ordinal()];
      init(source, false, style.load(), style.init_I());
   }

   public static void load(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      for (int i = 0; i < 32; i++) {
         if (init_cg_DataUtilArr[i] == source) {
            init_JArr[i] = 0L;
         }
      }
   }

   public static void init(BlockPos pos) {
      init(PlacedBlockTrailRenderer.Inner_DataUtil.SCAFFOLD, pos);
   }

   public static void init(PlacedBlockTrailRenderer.Inner_DataUtil source, BlockPos pos) {
      PlacedBlockTrailRenderer.Inner_StreamUtil style = init_cg_StreamUtilArr[source.ordinal()];
      if (style.init_Z() && pos != null) {
         init_IArr[load_I] = pos.getX();
         load_IArr[load_I] = pos.getY();
         read[load_I] = pos.getZ();
         init_JArr[load_I] = System.nanoTime();
         init_cg_DataUtilArr[load_I] = source;
         write[load_I] = style.init_I() | 0xFF000000;
         init_ZArr[load_I] = style.load();
         load_I = (load_I + 1) % 32;
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

      for (long at : init_JArr) {
         if (at != 0L && now >= at && now - at < 320000000L) {
            return true;
         }
      }

      return false;
   }

   static long init_J(PlacedBlockTrailRenderer.Inner_DataUtil source) {
      long latest = 0L;

      for (int i = 0; i < 32; i++) {
         if (init_cg_DataUtilArr[i] == source) {
            latest = Math.max(latest, init_JArr[i]);
         }
      }

      return latest;
   }

   public static void load() {
      LevelRenderEvents.COLLECT_SUBMITS
         .register(
            context -> {
               if (init_Z()) {
                  Minecraft mc = Minecraft.getInstance();
                  if (mc != null && mc.level != null) {
                     long now = System.nanoTime();
                     int accent = ThemePaletteManager.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.ColorRole.ACCENT);
                     Vec3 origin = context.levelState().cameraRenderState.pos;
                     context.submitNodeCollector()
                        .submitCustomGeometry(
                           context.poseStack(),
                           autismclient.render.AutismRenderTypes.storageEspFillSeeThrough(),
                           (pose, buffer) -> {
                              for (int i = 0; i < 32; i++) {
                                 long t = init_JArr[i];
                                 if (t != 0L) {
                                    long age = now - t;
                                    if (age >= 0L && age < 320000000L) {
                                       init(
                                          pose,
                                          buffer,
                                          init_IArr[i] - origin.x,
                                          load_IArr[i] - origin.y,
                                          read[i] - origin.z,
                                          age / 3.2E8,
                                          init_ZArr[i] ? write[i] : accent
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

   private static void init(Pose pose, VertexConsumer buffer, double bx, double by, double bz, double t, int argb) {
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
         init(pose, buffer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, c);
         init(pose, buffer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, c);
         init(pose, buffer, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, c);
         init(pose, buffer, x2, y1, z1, x1, y1, z1, x1, y2, z1, x2, y2, z1, c);
         init(pose, buffer, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, c);
         init(pose, buffer, x2, y1, z2, x2, y1, z1, x2, y2, z1, x2, y2, z2, c);
      }
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

   public enum Inner_DataUtil {
      SCAFFOLD,
      CLUTCH;
   }

   private record Inner_StreamUtil(boolean init_Z, boolean load, int init_I) {
   }
}
