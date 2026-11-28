package autismclient.modules;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3fc;

public final class CrosshairRenderer {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static final int init_I = 0;
   private static final int load_I = 1;
   private static final int read = 2;
   private static final int write = 3;
   private static final int BYTES_PER_PIXEL = 4;
   private static final CrosshairRenderer.TextureCache HUD_CACHE = new CrosshairRenderer.TextureCache("hud", 0L);
   private static final CrosshairRenderer.TextureCache PREVIEW_CACHE = new CrosshairRenderer.TextureCache("preview", 80L);
   private static int textureCounter;

   private CrosshairRenderer() {
   }

   public static boolean render(GuiGraphicsExtractor graphics) {
      CrosshairRenderState.Inner_DataUtil look = CrosshairRenderState.current();
      if (look == null) {
         return false;
      }

      int guiScale = Math.max(1, MINECRAFT.getWindow().getGuiScale());
      if (look.style().isPreset()) {
         graphics.pose().pushMatrix();

         try {
            graphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);
            if (CrosshairVectorPipeline.drawPreset(graphics, look, MINECRAFT.getWindow().getWidth() / 2.0F, MINECRAFT.getWindow().getHeight() / 2.0F, guiScale, 0, aimedTargetType(look))) {
               return true;
            }
         } finally {
            graphics.pose().popMatrix();
         }
      }

      CrosshairRenderer.TextureEntry entry = HUD_CACHE.entryFor(look, guiScale, 0);
      if (entry == null) {
         return false;
      }

      int x = (MINECRAFT.getWindow().getWidth() - entry.width()) / 2;
      int y = (MINECRAFT.getWindow().getHeight() - entry.width()) / 2;
      graphics.pose().pushMatrix();

      try {
         graphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);
         drawTexture(graphics, entry, look, x, y, aimedTargetType(look));
      } finally {
         graphics.pose().popMatrix();
      }

      return true;
   }

   public static void renderAt(GuiGraphicsExtractor graphics, CrosshairRenderState.Inner_DataUtil look, double centerX, double centerY, float pixelsPerUnit) {
      render(graphics, look, centerX, centerY, pixelsPerUnit, 0);
   }

   public static void render(
      GuiGraphicsExtractor graphics, CrosshairRenderState.Inner_DataUtil look, double centerX, double centerY, float pixelsPerUnit, int maxPixels
   ) {
      if (look != null && !(pixelsPerUnit <= 0.0F)) {
         int guiScale = Math.max(1, MINECRAFT.getWindow().getGuiScale());
         if (look.style().isPreset()) {
            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(1.0F / pixelsPerUnit, 1.0F / pixelsPerUnit);
               if (CrosshairVectorPipeline.drawPreset(graphics, look, (float)(centerX * pixelsPerUnit), (float)(centerY * pixelsPerUnit), guiScale, maxPixels, 0)) {
                  return;
               }
            } finally {
               graphics.pose().popMatrix();
            }
         }

         CrosshairRenderer.TextureEntry entry = PREVIEW_CACHE.entryFor(look, guiScale, maxPixels);
         if (entry != null) {
            int x = (int)Math.round(centerX * pixelsPerUnit - entry.width() / 2.0);
            int y = (int)Math.round(centerY * pixelsPerUnit - entry.width() / 2.0);
            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(1.0F / pixelsPerUnit, 1.0F / pixelsPerUnit);
               Matrix3x2fStack pose = graphics.pose();
               pose.translate(CrosshairVectorPipeline.snapToPixel(x, pose.m00(), pose.m20(), guiScale, 0) - x, CrosshairVectorPipeline.snapToPixel(y, pose.m11(), pose.m21(), guiScale, 0) - y);
               drawTexture(graphics, entry, look, x, y, 0);
            } finally {
               graphics.pose().popMatrix();
            }
         }
      }
   }

   private static void drawTexture(GuiGraphicsExtractor graphics, CrosshairRenderer.TextureEntry entry, CrosshairRenderState.Inner_DataUtil look, int x, int y, int target) {
      float opacity = look.init_F();
      boolean invert = target == 0 && look.init_Z();
      if (invert) {
         graphics.nextStratum();
      }

      if (look.load_Z()) {
         blitCell(graphics, RenderPipelines.GUI_TEXTURED, entry, 3, x, y, withOpacity(look.load_I(), opacity));
      }

      if (target != 0) {
         blitCell(graphics, RenderPipelines.GUI_TEXTURED, entry, 1, x, y, withOpacity(target, opacity));
      } else if (look.init_Z()) {
         if (look.load_Z()) {
            graphics.nextStratum();
         }

         int level = Math.round(255.0F * opacity);
         blitCell(graphics, RenderPipelines.CROSSHAIR, entry, 2, x, y, 0xFF000000 | level << 16 | level << 8 | level);
      } else if (look.style() == CrosshairPresetGeometry.Inner_NetHelper.CUSTOM) {
         blitCell(graphics, RenderPipelines.GUI_TEXTURED, entry, 0, x, y, withOpacity(-1, opacity));
      } else {
         blitCell(graphics, RenderPipelines.GUI_TEXTURED, entry, 1, x, y, withOpacity(look.init_I(), opacity));
      }

      if (invert) {
         graphics.nextStratum();
      }
   }

   private static void blitCell(GuiGraphicsExtractor graphics, RenderPipeline pipeline, CrosshairRenderer.TextureEntry entry, int cell, int x, int y, int color) {
      int size = entry.width();
      graphics.blit(pipeline, entry.texture(), x, y, cell * (size + 2) + 1, 1.0F, size, size, size, size, entry.textureWidth(), entry.textureHeight(), color);
   }

   private static int withOpacity(int argb, float opacity) {
      int alpha = Math.round((argb >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, opacity)));
      return alpha << 24 | argb & 16777215;
   }

   private static int aimedTargetType(CrosshairRenderState.Inner_DataUtil look) {
      if (look.read_Z() && MINECRAFT.player != null && MINECRAFT.level != null) {
         Camera camera = MINECRAFT.gameRenderer.mainCamera();
         if (camera != null && camera.isInitialized()) {
            Vec3 from = camera.position();
            Vector3fc forward = camera.forwardVector();
            Vec3 direction = new Vec3(forward.x(), forward.y(), forward.z());
            double reach = MINECRAFT.player.entityInteractionRange();
            double range = look.write_Z() ? Math.max(reach, look.update()) : reach;
            HitResult block = MINECRAFT.level
               .clip(new ClipContext(from, from.add(direction.scale(range)), Block.OUTLINE, Fluid.NONE, MINECRAFT.player));
            double limit = block.getType() == Type.MISS ? range : from.distanceTo(block.getLocation());
            Vec3 end = from.add(direction.scale(limit));
            Entity viewer = MINECRAFT.getCameraEntity() == null ? MINECRAFT.player : MINECRAFT.getCameraEntity();
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(
               viewer,
               from,
               end,
               new AABB(from, end).inflate(1.0),
               entityx -> entityx != MINECRAFT.player && !entityx.isSpectator() && entityx.isPickable(),
               limit * limit
            );
            if (hit == null) {
               return 0;
            } else {
               Entity entity = hit.getEntity();
               if (!(entity instanceof LivingEntity living && living.isAlive() && !(entity instanceof ArmorStand))) {
                  return 0;
               } else if (AntiBotTracker.shouldIgnoreEntity(entity)) {
                  return 0;
               } else {
                  boolean inReach = from.distanceTo(hit.getLocation()) <= reach;
                  if (TeamsModule.isFriend(entity)) {
                     return inReach ? look.write_I() : 0;
                  } else if (!(entity instanceof Enemy) && !(entity instanceof Player)) {
                     return inReach ? look.write_I() : 0;
                  } else {
                     return !inReach && !look.write_Z() ? 0 : look.read_I();
                  }
               }
            }
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   private static final class TextureCache {
      private final String name;
      private final long throttleMillis;
      private CrosshairRenderer.TextureEntry cached;
      private long lastBuildMillis;

      TextureCache(String name, long throttleMillis) {
         this.name = name;
         this.throttleMillis = throttleMillis;
      }

      CrosshairRenderer.TextureEntry entryFor(CrosshairRenderState.Inner_DataUtil look, int guiScale, int maxPixels) {
         boolean custom = look.style() == CrosshairPresetGeometry.Inner_NetHelper.CUSTOM;
         int outlinePx = look.load_Z() ? Math.max(1, (int)Math.round(look.write_D() * guiScale)) : 0;
         CrosshairRenderer.TextureKey key = new CrosshairRenderer.TextureKey(
            look.style(),
            look.init_D(),
            custom ? 0.0 : look.load_D(),
            look.style() == CrosshairPresetGeometry.Inner_NetHelper.TACTICAL ? look.read_D() : 0.0,
            outlinePx,
            guiScale,
            maxPixels,
            custom ? look.art() : null
         );
         if (this.cached != null && this.cached.key().equals(key)) {
            return this.cached;
         }

         long now = System.currentTimeMillis();
         if (this.cached != null && now - this.lastBuildMillis < this.throttleMillis) {
            return this.cached;
         }

         if (custom && look.art() == null) {
            return this.cached;
         }

         double density = guiScale;
         int attempt = 0;

         while (true) {
            int border = look.load_Z() ? Math.max(1, (int)Math.round(look.write_D() * density)) : 0;
            double unit = density * look.init_D();
            CrosshairPresetGeometry.Inner_CachePool raster = custom
               ? CrosshairPresetGeometry.rasterize(look.art(), unit, border, border + 1)
               : CrosshairPresetGeometry.raster(look.style(), unit, look.load_D(), look.read_D(), border, border + 1);
            if (maxPixels <= 0 || raster.pixelCount() <= maxPixels || attempt == 3) {
               CrosshairRenderer.TextureEntry built = this.build(key, raster);
               if (built == null) {
                  return this.cached;
               }

               this.releaseCached();
               this.cached = built;
               this.lastBuildMillis = now;
               return this.cached;
            }

            density *= Math.max(0.05, (maxPixels - 4.0) / raster.pixelCount());
            attempt++;
         }
      }

      private CrosshairRenderer.TextureEntry build(CrosshairRenderer.TextureKey key, CrosshairPresetGeometry.Inner_CachePool raster) {
         int size = raster.pixelCount();
         int stride = size + 2;
         int width = stride * 4;
         int height = stride;
         NativeImage image = new NativeImage(width, height, true);

         try {
            for (int y = 0; y < size; y++) {
               for (int x = 0; x < size; x++) {
                  int i = y * size + x;
                  int fill = Math.round(raster.coverage()[i] * 255.0F);
                  int outline = Math.round(raster.load()[i] * 255.0F);
                  if (fill > 0) {
                     image.setPixel(0 * stride + 1 + x, 1 + y, fill << 24 | raster.pixel(i) & 16777215);
                     image.setPixel(1 * stride + 1 + x, 1 + y, fill << 24 | 16777215);
                     image.setPixel(2 * stride + 1 + x, 1 + y, fill << 24 | fill << 16 | fill << 8 | fill);
                  }

                  if (outline > 0) {
                     image.setPixel(3 * stride + 1 + x, 1 + y, outline << 24 | 16777215);
                  }
               }
            }

            Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/crosshair/" + this.name + "_" + ++CrosshairRenderer.textureCounter);
            CrosshairRenderer.MINECRAFT.getTextureManager().register(id, new CrosshairRenderer.CrosshairTexture(this.name, image));
            return new CrosshairRenderer.TextureEntry(key, id, size, width, height);
         } catch (Throwable t) {
            image.close();
            return null;
         }
      }

      private void releaseCached() {
         if (this.cached != null) {
            Identifier id = this.cached.texture();
            this.cached = null;

            try {
               CrosshairRenderer.MINECRAFT.getTextureManager().release(id);
            } catch (Throwable var3) {
            }
         }
      }
   }

   private static final class CrosshairTexture extends DynamicTexture {
      CrosshairTexture(String name, NativeImage image) {
         super(() -> "AUTISM crosshair " + name, image);
         this.sampler = RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);
      }
   }

   private record TextureEntry(CrosshairRenderer.TextureKey key, Identifier texture, int width, int textureWidth, int textureHeight) {
   }

   private record TextureKey(
      CrosshairPresetGeometry.Inner_NetHelper init_p_NetHelper, double init_D, double load_D, double read_D, int init_I, int load_I, int read_I, CrosshairPixelArt init_m
   ) {
   }
}
