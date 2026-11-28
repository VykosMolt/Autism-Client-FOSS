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
   private static final Matrix4f PROJECTION = new Matrix4f();
   private static final Vector4f clipPos = new Vector4f();
   private static final float[] drawnX = new float[3];
   private static final float[] drawnY = new float[3];
   private static int drawnCount;

   private TrajectoryOffscreenIndicator() {
   }

   public static int drawnCount() {
      return drawnCount;
   }

   static void reset() {
      drawnCount = 0;
   }

   public static void render(GuiGraphicsExtractor graphics) {
      drawnCount = 0;
      Minecraft mc = Minecraft.getInstance();
      Module module = al.init_Module_Str("trajectories");
      if (module instanceof TrajectoriesModule trajectories && module.isEnabled() && mc.player != null && mc.gui.screen() == null && !aD.init_Z() && WorldProjectionHelper.projection(PROJECTION)) {
         Vec3 camera = WorldProjectionHelper.cameraPosition();
         ThrownPearlTracker.Inner_DataUtil remembered = TrajectoryStateCache.rememberedPearl();
         if (remembered != null) {
            drawIndicator(graphics, mc, remembered.position(), camera, -8329771);
         }

         for (TrajectoriesModule.Inner_CachePool path : trajectories.paths()) {
            if (drawnCount >= 3) {
               break;
            }

            if (path.hit() != null) {
               Vec3 at = path.load_Vec3() != null ? path.load_Vec3() : path.hit().getLocation();
               if (remembered == null || !(remembered.position().distanceToSqr(at) < 1.0)) {
                  int color = path.hit().getType() == Type.ENTITY ? -44976 : path.color();
                  drawIndicator(graphics, mc, at, camera, color);
               }
            }
         }
      }
   }

   private static void drawIndicator(GuiGraphicsExtractor graphics, Minecraft mc, Vec3 at, Vec3 camera, int color) {
      clipPos.set((float)(at.x - camera.x), (float)(at.y - camera.y), (float)(at.z - camera.z), 1.0F);
      PROJECTION.transform(clipPos);
      if (Float.isFinite(clipPos.x) && Float.isFinite(clipPos.y) && Float.isFinite(clipPos.w)) {
         float dx = clipPos.x / Math.max(0.001F, Math.abs(clipPos.w));
         float dy = -clipPos.y / Math.max(0.001F, Math.abs(clipPos.w));
         if (!(clipPos.w > 0.0F) || !(Math.abs(dx) < 0.9F) || !(Math.abs(dy) < 0.88F)) {
            int width = cA.init_I();
            int height = cA.load_I();
            if (width >= 64 && height >= 64) {
               if (clipPos.w <= 0.0F) {
                  float yaw = mc.gameRenderer.gameRenderState().levelRenderState.cameraRenderState.yRot;
                  float bearing = Mth.wrapDegrees((float)(Math.toDegrees(Math.atan2(at.z - camera.z, at.x - camera.x)) - 90.0) - yaw) * 0.017453292F;
                  dx = Mth.sin(bearing);
                  dy = -Mth.cos(bearing);
               }

               float scale = Math.max(Math.abs(dx) / Math.max(12.0F, width * 0.5F - 24.0F), Math.abs(dy) / Math.max(12.0F, height * 0.5F - 48.0F));
               if (!(scale < 1.0E-4F) && Float.isFinite(scale)) {
                  float x = width * 0.5F + dx / scale;
                  float y = height * 0.5F + dy / scale;

                  for (int i = 0; i < drawnCount; i++) {
                     if (Math.abs(x - drawnX[i]) < 19.0F && Math.abs(y - drawnY[i]) < 19.0F) {
                        return;
                     }
                  }

                  drawnX[drawnCount] = x;
                  drawnY[drawnCount++] = y;
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
