package autismclient.modules;

import autismclient.util.cA;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class TrajectoryOffscreenIndicator {
   private static final Matrix4f init_Matrix4f = new Matrix4f();
   private static final Vector4f init_Vector4f = new Vector4f();
   private static final float[] init_FArr = new float[3];
   private static final float[] load = new float[3];
   private static int init_I;

   private TrajectoryOffscreenIndicator() {
   }

   public static int init_I() {
      return init_I;
   }

   static void init_V() {
      init_I = 0;
   }

   public static void init(GuiGraphicsExtractor graphics) {
      init_I = 0;
      Minecraft mc = Minecraft.getInstance();
      Module module = al.init_Module_Str("trajectories");
      if (module instanceof TrajectoriesModule trajectories && module.isEnabled() && mc.player != null && mc.gui.screen() == null && !aD.init_Z() && WorldProjectionHelper.init(init_Matrix4f)) {
         Vec3 camera = WorldProjectionHelper.init();
         ThrownPearlTracker.Inner_DataUtil remembered = TrajectoryStateCache.init_aV_DataUtil();
         if (remembered != null) {
            init(graphics, mc, remembered.init_Vec3(), camera, -8329771);
         }

         for (TrajectoriesModule.Inner_CachePool path : trajectories.init_List_12()) {
            if (init_I >= 3) {
               break;
            }

            if (path.init_HitResult() != null) {
               Vec3 at = path.load_Vec3() != null ? path.load_Vec3() : path.init_HitResult().getLocation();
               if (remembered == null || !(remembered.init_Vec3().distanceToSqr(at) < 1.0)) {
                  int color = path.init_HitResult().getType() == Type.ENTITY ? -44976 : path.init_I();
                  init(graphics, mc, at, camera, color);
               }
            }
         }
      }
   }

   private static void init(GuiGraphicsExtractor graphics, Minecraft mc, Vec3 at, Vec3 camera, int color) {
      init_Vector4f.set((float)(at.x - camera.x), (float)(at.y - camera.y), (float)(at.z - camera.z), 1.0F);
      init_Matrix4f.transform(init_Vector4f);
      if (Float.isFinite(init_Vector4f.x) && Float.isFinite(init_Vector4f.y) && Float.isFinite(init_Vector4f.w)) {
         float dx = init_Vector4f.x / Math.max(0.001F, Math.abs(init_Vector4f.w));
         float dy = -init_Vector4f.y / Math.max(0.001F, Math.abs(init_Vector4f.w));
         if (!(init_Vector4f.w > 0.0F) || !(Math.abs(dx) < 0.9F) || !(Math.abs(dy) < 0.88F)) {
            int width = cA.init_I();
            int height = cA.load_I();
            if (width >= 64 && height >= 64) {
               if (init_Vector4f.w <= 0.0F) {
                  float yaw = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.yRot;
                  float bearing = Mth.wrapDegrees((float)(Math.toDegrees(Math.atan2(at.z - camera.z, at.x - camera.x)) - 90.0) - yaw) * 0.017453292F;
                  dx = Mth.sin(bearing);
                  dy = -Mth.cos(bearing);
               }

               float scale = Math.max(Math.abs(dx) / Math.max(12.0F, width * 0.5F - 24.0F), Math.abs(dy) / Math.max(12.0F, height * 0.5F - 48.0F));
               if (!(scale < 1.0E-4F) && Float.isFinite(scale)) {
                  float x = width * 0.5F + dx / scale;
                  float y = height * 0.5F + dy / scale;

                  for (int i = 0; i < init_I; i++) {
                     if (Math.abs(x - init_FArr[i]) < 19.0F && Math.abs(y - load[i]) < 19.0F) {
                        return;
                     }
                  }

                  init_FArr[init_I] = x;
                  load[init_I++] = y;
                  autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract(graphics, x, y, 9.0F, -586147549);
                  graphics.pose().pushMatrix();

                  try {
                     graphics.pose().translate(x, y);
                     graphics.pose().rotate((float)Math.atan2(dx, -dy));
                     autismclient.gui.f.PacketUtil.write(graphics, autismclient.gui.f.NetHelper.init(-4, -5, 8, 10), color);
                  } finally {
                     graphics.pose().popMatrix();
                  }
               }
            }
         }
      }
   }
}
