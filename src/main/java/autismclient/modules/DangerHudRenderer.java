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
   private static final Matrix4f PROJECTION = new Matrix4f();
   private static final autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgeSize BADGE_SIZE = autismclient.gui.ui.widgets.WorldIndicatorBadge.size(0, 1, false);
   private static final Vector4f PROJECTED = new Vector4f();
   private static final float[] placedMarkerX = new float[6];
   private static final float[] placedMarkerY = new float[6];
   private static final int[] placedEntityIds = new int[6];

   private DangerHudRenderer() {
   }

   public static void render(GuiGraphicsExtractor graphics) {
      DangerModule danger = DangerModule.instance();
      Minecraft mc = Minecraft.getInstance();
      if (danger != null) {
         BedDangerHudRenderer.render(graphics, danger);
      }

      if (danger != null && danger.showsHud() && mc.player != null && mc.gui.screen() == null && !aD.init_Z()) {
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, mc.font, 0, 0);
         if (WorldProjectionHelper.projection(PROJECTION)) {
            Vec3 camera = WorldProjectionHelper.cameraPosition();
            float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float scale = danger.warningScale();
            Arrays.fill(placedMarkerX, 0.0F / 0.0F);
            Arrays.fill(placedMarkerY, 0.0F / 0.0F);
            int drawn = 0;

            for (IncomingProjectileScanner.ProjectileWarning warning : danger.incoming()) {
               if (drawn >= 6) {
                  break;
               }

               Entity entity = warning.entity();
               if (!entity.isRemoved() && danger.isKindOptionEnabled(warning.kind(), "hud")) {
                  boolean duplicate = false;

                  for (int j = 0; j < drawn; j++) {
                     if (placedEntityIds[j] == entity.getId()) {
                        duplicate = true;
                     }
                  }

                  if (!duplicate) {
                     int i = drawn++;
                     placedEntityIds[i] = entity.getId();
                     double x = Mth.lerp(partial, entity.xOld, entity.getX());
                     double y = Mth.lerp(partial, entity.yOld, entity.getY()) + (warning.init_Z() ? entity.getEyeHeight() : entity.getBbHeight() * 0.5);
                     double z = Mth.lerp(partial, entity.zOld, entity.getZ());
                     int color = danger.warningColor(warning);
                     PROJECTED.set((float)(x - camera.x), (float)(y - camera.y), (float)(z - camera.z), 1.0F);
                     PROJECTION.transform(PROJECTED);
                     int screenW = cA.init_I();
                     int screenH = cA.load_I();
                     float nx = PROJECTED.x / Math.max(0.001F, Math.abs(PROJECTED.w));
                     float ny = -PROJECTED.y / Math.max(0.001F, Math.abs(PROJECTED.w));
                     float markerX;
                     float markerY;
                     float angle;
                     if (PROJECTED.w > 0.0F && Math.abs(nx) < 0.9 && Math.abs(ny) < 0.85) {
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
                        if (Math.abs(markerX - placedMarkerX[j]) < 38.0F * scale && Math.abs(markerY - placedMarkerY[j]) < 22.0F * scale) {
                           markerY = placedMarkerY[j] + 23.0F * scale;
                           j = -1;
                        }
                     }

                     markerX = Mth.clamp(markerX, 19.0F * scale, Math.max(19.0F * scale, screenW - 19.0F * scale));
                     markerY = Mth.clamp(markerY, 11.0F * scale, Math.max(11.0F * scale, screenH - 11.0F * scale));
                     placedMarkerX[i] = markerX;
                     placedMarkerY[i] = markerY;
                     autismclient.gui.ui.widgets.WorldIndicatorBadge.render(
                        context,
                        new autismclient.gui.ui.widgets.WorldIndicatorBadge.BadgePlacement(markerX, markerY, angle),
                        BADGE_SIZE,
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
