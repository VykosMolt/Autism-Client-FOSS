package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import autismclient.util.bK;
import autismclient.util.cA;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class EntityEsp2DRenderer {
   private static final Minecraft MC = Minecraft.getInstance();
   private static final Matrix4f PROJECTION = new Matrix4f();
   private static final EntityEsp2DRenderer.ScreenBounds BOUNDS = new EntityEsp2DRenderer.ScreenBounds();
   private static final Int2ObjectOpenHashMap<EntityEsp2DRenderer.SmoothedHealth> healthByEntity = new Int2ObjectOpenHashMap<>();
   private static final float init_F = 0.12F;
   private static final double init_D = 0.06;
   private static final double load_D = 0.08;
   private static final double read = 0.02;
   private static Object trackedLevel;
   private static long frameCounter;
   private static long lastFrameNanos;

   private EntityEsp2DRenderer() {
   }

   public static void render(GuiGraphicsExtractor context) {
      if (!aD.init_Z()
         && (AutismLiteVariant.enabled() || autismclient.e.ListPool.init())
         && MC != null
         && MC.level != null
         && MC.player != null
         && !MC.gui.hud.isHidden()
         && am.add()
         && !am.clear()) {
         long perf = bK.init_J();

         try {
            renderAll(context);
         } finally {
            bK.init("modules.esp2d", perf);
         }
      }
   }

   private static void renderAll(GuiGraphicsExtractor context) {
      if (MC.getWindow() != null && WorldProjectionHelper.projection(PROJECTION)) {
         if (trackedLevel != MC.level) {
            healthByEntity.clear();
            trackedLevel = MC.level;
         }

         long now = System.nanoTime();
         float dt = lastFrameNanos == 0L ? 0.0F : Math.min(0.1F, (float)(now - lastFrameNanos) / 1.0E9F);
         lastFrameNanos = now;
         long frame = ++frameCounter;
         float screenW = cA.init_I();
         float screenH = cA.load_I();
         float px = screenW / Math.max(1, MC.getWindow().getWidth());
         Vec3 camera = WorldProjectionHelper.cameraPosition();
         float tickDelta = MC.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float healthLerp = dt <= 0.0F ? 1.0F : 1.0F - (float)Math.exp(-dt / 0.12F);
         Module espModule = al.init_Module_Str("esp");
         boolean fill = espModule != null && espModule.bool("fill");

         for (Entity entity : MC.level.entitiesForRendering()) {
            try {
               float fade = am.init_F(entity);
               if (!(fade <= 0.0F)
                  && project(entity, tickDelta, camera, screenW, screenH)
                  && !(BOUNDS.maxX < 0.0F)
                  && !(BOUNDS.maxY < 0.0F)
                  && !(BOUNDS.sx > screenW)
                  && !(BOUNDS.minY > screenH)) {
                  drawBox(context, am.init_I(entity), fade, fill, px);
                  if (entity instanceof LivingEntity living) {
                     drawHealthBar(context, living, fade, px, healthLerp, frame);
                  }
               }
            } catch (Throwable var18) {
            }
         }

         pruneShown(frame);
      }
   }

   private static boolean project(Entity entity, float tickDelta, Vec3 camera, float screenW, float screenH) {
      double x = Mth.lerp(tickDelta, entity.xOld, entity.getX());
      double y = Mth.lerp(tickDelta, entity.yOld, entity.getY());
      double z = Mth.lerp(tickDelta, entity.zOld, entity.getZ());
      EntityDimensions dimensions = entity.getDimensions(entity.getPose());
      double radius = dimensions.width() * 0.5 + 0.06;
      double bottom = y - 0.02 - camera.y;
      double top = y + dimensions.height() + 0.08 - camera.y;
      double cx = x - camera.x;
      double cz = z - camera.z;
      float midY = (float)((bottom + top) * 0.5);
      float centerW = PROJECTION.m03() * (float)cx + PROJECTION.m13() * midY + PROJECTION.m23() * (float)cz + PROJECTION.m33();
      if (centerW <= -((float)(radius + dimensions.height() + 0.5))) {
         return false;
      }

      double distance = Math.sqrt(cx * cx + cz * cz);
      double ux = distance < 1.0E-4 ? 0.0 : cx / distance;
      double uz = distance < 1.0E-4 ? 1.0 : cz / distance;
      double sideX = -uz * radius;
      double sideZ = ux * radius;
      double alongX = ux * radius;
      double alongZ = uz * radius;
      EntityEsp2DRenderer.ScreenBounds box = BOUNDS;
      box.reset(screenW, screenH);
      return box.include(cx + sideX, bottom, cz + sideZ)
         && box.include(cx - sideX, bottom, cz - sideZ)
         && box.include(cx + sideX, top, cz + sideZ)
         && box.include(cx - sideX, top, cz - sideZ)
         && box.include(cx + alongX, bottom, cz + alongZ)
         && box.include(cx - alongX, bottom, cz - alongZ)
         && box.include(cx + alongX, top, cz + alongZ)
         && box.include(cx - alongX, top, cz - alongZ)
         && box.maxX - box.sx > 0.5F
         && box.maxY - box.minY > 0.5F;
   }

   private static void drawBox(GuiGraphicsExtractor context, int rgb, float fade, boolean fill, float px) {
      float x = BOUNDS.sx;
      float y = BOUNDS.minY;
      float w = BOUNDS.maxX - x;
      float h = BOUNDS.maxY - y;
      float radius = Math.max(0.75F, 2.0F * px);
      int shadow = alphaBits(0.62F * fade);
      if (fill) {
         autismclient.gui.sdf.SdfShapeRenderer.drawRoundedRect(context, x, y, w, h, radius, alphaBits(0.2F * fade) | rgb, alphaBits(0.05F * fade) | rgb, 0, 0);
      }

      autismclient.gui.sdf.SdfShapeRenderer.fillRoundRect(context, x - px, y - px, w + 2.0F * px, h + 2.0F * px, radius + px, 0, shadow);
      autismclient.gui.sdf.SdfShapeRenderer.fillRoundRect(context, x, y, w, h, radius, 0, alphaBits(fade) | rgb);
      if (w > 4.0F * px && h > 4.0F * px) {
         autismclient.gui.sdf.SdfShapeRenderer.fillRoundRect(context, x + px, y + px, w - 2.0F * px, h - 2.0F * px, Math.max(0.75F, radius - px), 0, shadow);
      }
   }

   private static void drawHealthBar(GuiGraphicsExtractor context, LivingEntity entity, float fade, float px, float healthLerp, long frame) {
      float health = Mth.clamp(entity.getHealth() / Math.max(1.0F, entity.getMaxHealth()), 0.0F, 1.0F);
      EntityEsp2DRenderer.SmoothedHealth shown = healthByEntity.get(entity.getId());
      if (shown == null) {
         shown = new EntityEsp2DRenderer.SmoothedHealth(health);
         healthByEntity.put(entity.getId(), shown);
      } else {
         shown.init_F = shown.init_F + (health - shown.init_F) * healthLerp;
      }

      shown.frame = frame;
      float y = BOUNDS.minY;
      float h = BOUNDS.maxY - BOUNDS.minY;
      float barW = 5.0F * px;
      float x = BOUNDS.sx - 3.0F * px - barW;
      fillRect(context, x, y - px, barW, h + 2.0F * px, alphaBits(0.62F * fade));
      float inner = barW - 2.0F * px;
      float filled = h * shown.init_F;
      if (!(filled <= 0.25F * px)) {
         int color = alphaBits(fade) | Mth.hsvToRgb(shown.init_F / 3.0F, 0.78F, 1.0F);
         fillRect(context, x + px, y + h - filled, inner, filled, color);
      }
   }

   private static void fillRect(GuiGraphicsExtractor context, float x, float y, float w, float h, int color) {
      float radius = Math.min(w, h) * 0.5F;
      if (radius >= 0.75F && autismclient.gui.sdf.SdfShapeRenderer.isAvailable()) {
         autismclient.gui.sdf.SdfShapeRenderer.fillRoundRect(context, x, y, w, h, radius, color);
      } else {
         context.pose().pushMatrix();
         context.pose().translate(x, y);
         context.pose().scale(w, h);
         context.fill(0, 0, 1, 1, color);
         context.pose().popMatrix();
      }
   }

   private static int alphaBits(float opacity) {
      return Math.round(Mth.clamp(opacity, 0.0F, 1.0F) * 255.0F) << 24;
   }

   private static void pruneShown(long frame) {
      if ((frame & 63L) == 0L && !healthByEntity.isEmpty()) {
         healthByEntity.values().removeIf(shown -> frame - shown.frame > 20L);
      }
   }

   private static final class ScreenBounds {
      private final Vector4f clip = new Vector4f();
      float sx;
      float minY;
      float maxX;
      float maxY;
      private float width;
      private float height;

      void reset(float screenW, float screenH) {
         this.width = screenW;
         this.height = screenH;
         this.sx = this.minY = 3.4028235E38F;
         this.maxX = this.maxY = -3.4028235E38F;
      }

      boolean include(double x, double y, double z) {
         this.clip.set((float)x, (float)y, (float)z, 1.0F);
         EntityEsp2DRenderer.PROJECTION.transform(this.clip);
         if (this.clip.w <= 0.05F) {
            return false;
         }

         float sx = (this.clip.x / this.clip.w * 0.5F + 0.5F) * this.width;
         float sy = (0.5F - this.clip.y / this.clip.w * 0.5F) * this.height;
         if (Float.isFinite(sx) && Float.isFinite(sy)) {
            if (sx < this.sx) {
               this.sx = sx;
            }

            if (sx > this.maxX) {
               this.maxX = sx;
            }

            if (sy < this.minY) {
               this.minY = sy;
            }

            if (sy > this.maxY) {
               this.maxY = sy;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private static final class SmoothedHealth {
      float init_F;
      long frame;

      SmoothedHealth(float value) {
         this.init_F = value;
      }
   }
}
