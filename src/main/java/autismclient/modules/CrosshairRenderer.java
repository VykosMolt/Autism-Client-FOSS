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
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final int init_I = 0;
   private static final int load_I = 1;
   private static final int read = 2;
   private static final int write = 3;
   private static final int update = 4;
   private static final CrosshairRenderer.Inner_BufferHelper init_o_BufferHelper = new CrosshairRenderer.Inner_BufferHelper("hud", 0L);
   private static final CrosshairRenderer.Inner_BufferHelper load_o_BufferHelper = new CrosshairRenderer.Inner_BufferHelper("preview", 80L);
   private static int sync;

   private CrosshairRenderer() {
   }

   public static boolean init(GuiGraphicsExtractor graphics) {
      CrosshairRenderState.Inner_DataUtil look = CrosshairRenderState.current();
      if (look == null) {
         return false;
      }

      int guiScale = Math.max(1, init_Minecraft.getWindow().getGuiScale());
      if (look.style().isPreset()) {
         graphics.pose().pushMatrix();

         try {
            graphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);
            if (CrosshairVectorPipeline.init(graphics, look, init_Minecraft.getWindow().getWidth() / 2.0F, init_Minecraft.getWindow().getHeight() / 2.0F, guiScale, 0, init(look))) {
               return true;
            }
         } finally {
            graphics.pose().popMatrix();
         }
      }

      CrosshairRenderer.Inner_DataUtil entry = init_o_BufferHelper.init(look, guiScale, 0);
      if (entry == null) {
         return false;
      }

      int x = (init_Minecraft.getWindow().getWidth() - entry.init_I()) / 2;
      int y = (init_Minecraft.getWindow().getHeight() - entry.init_I()) / 2;
      graphics.pose().pushMatrix();

      try {
         graphics.pose().scale(1.0F / guiScale, 1.0F / guiScale);
         init_V_GuiGraphicsExtract(graphics, entry, look, x, y, init(look));
      } finally {
         graphics.pose().popMatrix();
      }

      return true;
   }

   public static void init(GuiGraphicsExtractor graphics, CrosshairRenderState.Inner_DataUtil look, double centerX, double centerY, float pixelsPerUnit) {
      render(graphics, look, centerX, centerY, pixelsPerUnit, 0);
   }

   public static void render(
      GuiGraphicsExtractor graphics, CrosshairRenderState.Inner_DataUtil look, double centerX, double centerY, float pixelsPerUnit, int maxPixels
   ) {
      if (look != null && !(pixelsPerUnit <= 0.0F)) {
         int guiScale = Math.max(1, init_Minecraft.getWindow().getGuiScale());
         if (look.style().isPreset()) {
            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(1.0F / pixelsPerUnit, 1.0F / pixelsPerUnit);
               if (CrosshairVectorPipeline.init(graphics, look, (float)(centerX * pixelsPerUnit), (float)(centerY * pixelsPerUnit), guiScale, maxPixels, 0)) {
                  return;
               }
            } finally {
               graphics.pose().popMatrix();
            }
         }

         CrosshairRenderer.Inner_DataUtil entry = load_o_BufferHelper.init(look, guiScale, maxPixels);
         if (entry != null) {
            int x = (int)Math.round(centerX * pixelsPerUnit - entry.init_I() / 2.0);
            int y = (int)Math.round(centerY * pixelsPerUnit - entry.init_I() / 2.0);
            graphics.pose().pushMatrix();

            try {
               graphics.pose().scale(1.0F / pixelsPerUnit, 1.0F / pixelsPerUnit);
               Matrix3x2fStack pose = graphics.pose();
               pose.translate(CrosshairVectorPipeline.snapToPixel(x, pose.m00(), pose.m20(), guiScale, 0) - x, CrosshairVectorPipeline.snapToPixel(y, pose.m11(), pose.m21(), guiScale, 0) - y);
               init_V_GuiGraphicsExtract(graphics, entry, look, x, y, 0);
            } finally {
               graphics.pose().popMatrix();
            }
         }
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, CrosshairRenderer.Inner_DataUtil entry, CrosshairRenderState.Inner_DataUtil look, int x, int y, int target) {
      float opacity = look.init_F();
      boolean invert = target == 0 && look.init_Z();
      if (invert) {
         graphics.nextStratum();
      }

      if (look.load_Z()) {
         init(graphics, RenderPipelines.GUI_TEXTURED, entry, 3, x, y, init(look.load_I(), opacity));
      }

      if (target != 0) {
         init(graphics, RenderPipelines.GUI_TEXTURED, entry, 1, x, y, init(target, opacity));
      } else if (look.init_Z()) {
         if (look.load_Z()) {
            graphics.nextStratum();
         }

         int level = Math.round(255.0F * opacity);
         init(graphics, RenderPipelines.CROSSHAIR, entry, 2, x, y, 0xFF000000 | level << 16 | level << 8 | level);
      } else if (look.style() == CrosshairPresetGeometry.Inner_NetHelper.CUSTOM) {
         init(graphics, RenderPipelines.GUI_TEXTURED, entry, 0, x, y, init(-1, opacity));
      } else {
         init(graphics, RenderPipelines.GUI_TEXTURED, entry, 1, x, y, init(look.init_I(), opacity));
      }

      if (invert) {
         graphics.nextStratum();
      }
   }

   private static void init(GuiGraphicsExtractor graphics, RenderPipeline pipeline, CrosshairRenderer.Inner_DataUtil entry, int cell, int x, int y, int color) {
      int size = entry.init_I();
      graphics.blit(pipeline, entry.init_Identifier(), x, y, cell * (size + 2) + 1, 1.0F, size, size, size, size, entry.load(), entry.read(), color);
   }

   private static int init(int argb, float opacity) {
      int alpha = Math.round((argb >>> 24 & 0xFF) * Math.max(0.0F, Math.min(1.0F, opacity)));
      return alpha << 24 | argb & 16777215;
   }

   private static int init(CrosshairRenderState.Inner_DataUtil look) {
      if (look.read_Z() && init_Minecraft.player != null && init_Minecraft.level != null) {
         Camera camera = init_Minecraft.gameRenderer.mainCamera();
         if (camera != null && camera.isInitialized()) {
            Vec3 from = camera.position();
            Vector3fc forward = camera.forwardVector();
            Vec3 direction = new Vec3(forward.x(), forward.y(), forward.z());
            double reach = init_Minecraft.player.entityInteractionRange();
            double range = look.write_Z() ? Math.max(reach, look.update()) : reach;
            HitResult block = init_Minecraft.level
               .clip(new ClipContext(from, from.add(direction.scale(range)), Block.OUTLINE, Fluid.NONE, init_Minecraft.player));
            double limit = block.getType() == Type.MISS ? range : from.distanceTo(block.getLocation());
            Vec3 end = from.add(direction.scale(limit));
            Entity viewer = init_Minecraft.getCameraEntity() == null ? init_Minecraft.player : init_Minecraft.getCameraEntity();
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(
               viewer,
               from,
               end,
               new AABB(from, end).inflate(1.0),
               entityx -> entityx != init_Minecraft.player && !entityx.isSpectator() && entityx.isPickable(),
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

   private static final class Inner_BufferHelper {
      private final String init_Str;
      private final long init_J;
      private CrosshairRenderer.Inner_DataUtil init_o_DataUtil;
      private long load;

      Inner_BufferHelper(String name, long throttleMillis) {
         this.init_Str = name;
         this.init_J = throttleMillis;
      }

      CrosshairRenderer.Inner_DataUtil init(CrosshairRenderState.Inner_DataUtil look, int guiScale, int maxPixels) {
         boolean custom = look.style() == CrosshairPresetGeometry.Inner_NetHelper.CUSTOM;
         int outlinePx = look.load_Z() ? Math.max(1, (int)Math.round(look.write_D() * guiScale)) : 0;
         CrosshairRenderer.Inner_StreamUtil key = new CrosshairRenderer.Inner_StreamUtil(
            look.style(),
            look.init_D(),
            custom ? 0.0 : look.load_D(),
            look.style() == CrosshairPresetGeometry.Inner_NetHelper.TACTICAL ? look.read_D() : 0.0,
            outlinePx,
            guiScale,
            maxPixels,
            custom ? look.art() : null
         );
         if (this.init_o_DataUtil != null && this.init_o_DataUtil.init_o_StreamUtil().equals(key)) {
            return this.init_o_DataUtil;
         }

         long now = System.currentTimeMillis();
         if (this.init_o_DataUtil != null && now - this.load < this.init_J) {
            return this.init_o_DataUtil;
         }

         if (custom && look.art() == null) {
            return this.init_o_DataUtil;
         }

         double density = guiScale;
         int attempt = 0;

         while (true) {
            int border = look.load_Z() ? Math.max(1, (int)Math.round(look.write_D() * density)) : 0;
            double unit = density * look.init_D();
            CrosshairPresetGeometry.Inner_CachePool raster = custom
               ? CrosshairPresetGeometry.rasterize(look.art(), unit, border, border + 1)
               : CrosshairPresetGeometry.init(look.style(), unit, look.load_D(), look.read_D(), border, border + 1);
            if (maxPixels <= 0 || raster.pixelCount() <= maxPixels || attempt == 3) {
               CrosshairRenderer.Inner_DataUtil built = this.init(key, raster);
               if (built == null) {
                  return this.init_o_DataUtil;
               }

               this.init();
               this.init_o_DataUtil = built;
               this.load = now;
               return this.init_o_DataUtil;
            }

            density *= Math.max(0.05, (maxPixels - 4.0) / raster.pixelCount());
            attempt++;
         }
      }

      private CrosshairRenderer.Inner_DataUtil init(CrosshairRenderer.Inner_StreamUtil key, CrosshairPresetGeometry.Inner_CachePool raster) {
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
                     image.setPixel(0 * stride + 1 + x, 1 + y, fill << 24 | raster.init(i) & 16777215);
                     image.setPixel(1 * stride + 1 + x, 1 + y, fill << 24 | 16777215);
                     image.setPixel(2 * stride + 1 + x, 1 + y, fill << 24 | fill << 16 | fill << 8 | fill);
                  }

                  if (outline > 0) {
                     image.setPixel(3 * stride + 1 + x, 1 + y, outline << 24 | 16777215);
                  }
               }
            }

            Identifier id = Identifier.fromNamespaceAndPath("autismclient", "dynamic/crosshair/" + this.init_Str + "_" + ++CrosshairRenderer.sync);
            CrosshairRenderer.init_Minecraft.getTextureManager().register(id, new CrosshairRenderer.Inner_CachePool(this.init_Str, image));
            return new CrosshairRenderer.Inner_DataUtil(key, id, size, width, height);
         } catch (Throwable t) {
            image.close();
            return null;
         }
      }

      private void init() {
         if (this.init_o_DataUtil != null) {
            Identifier id = this.init_o_DataUtil.init_Identifier();
            this.init_o_DataUtil = null;

            try {
               CrosshairRenderer.init_Minecraft.getTextureManager().release(id);
            } catch (Throwable var3) {
            }
         }
      }
   }

   private static final class Inner_CachePool extends DynamicTexture {
      Inner_CachePool(String name, NativeImage image) {
         super(() -> "AUTISM crosshair " + name, image);
         this.sampler = RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);
      }
   }

   private record Inner_DataUtil(CrosshairRenderer.Inner_StreamUtil init_o_StreamUtil, Identifier init_Identifier, int init_I, int load, int read) {
   }

   private record Inner_StreamUtil(
      CrosshairPresetGeometry.Inner_NetHelper init_p_NetHelper, double init_D, double load_D, double read_D, int init_I, int load_I, int read_I, CrosshairPixelArt init_m
   ) {
   }
}
