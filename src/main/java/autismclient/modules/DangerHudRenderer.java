package autismclient.modules;

import autismclient.util.cA;
import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class DangerHudRenderer {
   private static final Matrix4f init_Matrix4f = new Matrix4f();
   private static final autismclient.gui.ui.widgets.WorldIndicatorBadge.Inner_StreamUtil init_TaskTimer_StreamUt = autismclient.gui.ui.widgets.WorldIndicatorBadge.init(0, 1, false);
   private static final Vector4f init_Vector4f = new Vector4f();
   private static final float[] init_FArr = new float[6];
   private static final float[] load = new float[6];
   private static final int[] init_IArr = new int[6];

   private DangerHudRenderer() {
   }

   public static void init(GuiGraphicsExtractor graphics) {
      DangerModule danger = DangerModule.instance();
      Minecraft mc = Minecraft.getInstance();
      if (danger != null) {
         BedDangerHudRenderer.init(graphics, danger);
      }

      if (danger != null && danger.close_Z_9() && mc.player != null && mc.gui.screen() == null && !aD.init_Z()) {
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, mc.font, 0, 0);
         if (WorldProjectionHelper.init(init_Matrix4f)) {
            Vec3 camera = WorldProjectionHelper.init();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float scale = danger.warningScale();
            Arrays.fill(init_FArr, 0.0F / 0.0F);
            Arrays.fill(load, 0.0F / 0.0F);
            int drawn = 0;

            for (IncomingProjectileScanner.Inner_BufferHelper warning : danger.read_List_2()) {
               if (drawn >= 6) {
                  break;
               }

               Entity entity = warning.entity();
               if (!entity.isRemoved() && danger.isKindOptionEnabled(warning.kind(), "hud")) {
                  boolean duplicate = false;

                  for (int j = 0; j < drawn; j++) {
                     if (init_IArr[j] == entity.getId()) {
                        duplicate = true;
                     }
                  }

                  if (!duplicate) {
                     int i = drawn++;
                     init_IArr[i] = entity.getId();
                     double x = Mth.lerp(partial, entity.xOld, entity.getX());
                     double y = Mth.lerp(partial, entity.yOld, entity.getY()) + (warning.init_Z() ? entity.getEyeHeight() : entity.getBbHeight() * 0.5);
                     double z = Mth.lerp(partial, entity.zOld, entity.getZ());
                     int color = danger.warningColor(warning);
                     init_Vector4f.set((float)(x - camera.x), (float)(y - camera.y), (float)(z - camera.z), 1.0F);
                     init_Matrix4f.transform(init_Vector4f);
                     int screenW = cA.init_I();
                     int screenH = cA.load_I();
                     float nx = init_Vector4f.x / Math.max(0.001F, Math.abs(init_Vector4f.w));
                     float ny = -init_Vector4f.y / Math.max(0.001F, Math.abs(init_Vector4f.w));
                     float markerX;
                     float markerY;
                     float angle;
                     if (init_Vector4f.w > 0.0F && Math.abs(nx) < 0.9 && Math.abs(ny) < 0.85) {
                        markerX = (nx + 1.0F) * screenW * 0.5F;
                        markerY = (ny + 1.0F) * screenH * 0.5F - 12.0F - 10.0F * scale;
                        angle = 3.1415927F;
                     } else {
                        float yaw = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.yRot;
                        float bearing = Mth.wrapDegrees((float)(Math.toDegrees(Math.atan2(z - camera.z, x - camera.x)) - 90.0) - yaw);
                        angle = bearing * 0.017453292F;
                        float ring = 60.0F + 16.0F * scale;
                        markerX = screenW * 0.5F + Mth.sin(angle) * ring;
                        markerY = screenH * 0.5F - Mth.cos(angle) * ring;
                     }

                     for (int j = 0; j < i; j++) {
                        if (Math.abs(markerX - init_FArr[j]) < 38.0F * scale && Math.abs(markerY - load[j]) < 22.0F * scale) {
                           markerY = load[j] + 23.0F * scale;
                           j = -1;
                        }
                     }

                     markerX = Mth.clamp(markerX, 19.0F * scale, Math.max(19.0F * scale, screenW - 19.0F * scale));
                     markerY = Mth.clamp(markerY, 11.0F * scale, Math.max(11.0F * scale, screenH - 11.0F * scale));
                     init_FArr[i] = markerX;
                     load[i] = markerY;
                     autismclient.gui.ui.widgets.WorldIndicatorBadge.init(
                        context,
                        new autismclient.gui.ui.widgets.WorldIndicatorBadge.Inner_DataUtil(markerX, markerY, angle),
                        init_TaskTimer_StreamUt,
                        scale,
                        warning.kind().icon(),
                        null,
                        color,
                        true
                     );
                  }
               }
            }
         }
      }
   }
}
