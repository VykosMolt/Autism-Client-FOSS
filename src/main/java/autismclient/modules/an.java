package autismclient.modules;

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

public final class an {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final Matrix4f init_Matrix4f = new Matrix4f();
   private static final an.Inner_DataUtil init_an_DataUtil = new an.Inner_DataUtil();
   private static final Int2ObjectOpenHashMap<an.Inner_StreamUtil> init_Int2ObjectOpenHash = new Int2ObjectOpenHashMap<>();
   private static final float init_F = 0.12F;
   private static final double init_D = 0.06;
   private static final double load_D = 0.08;
   private static final double read = 0.02;
   private static Object init_Obj;
   private static long init_J;
   private static long load_J;

   private an() {
   }

   public static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context) {
      if (!aD.init_Z()
         && init_Minecraft != null
         && init_Minecraft.level != null
         && init_Minecraft.player != null
         && !init_Minecraft.gui.hud.isHidden()
         && am.add()
         && !am.clear()) {
         long perf = bK.init_J();

         try {
            load(context);
         } finally {
            bK.init("modules.esp2d", perf);
         }
      }
   }

   private static void load(GuiGraphicsExtractor context) {
      if (init_Minecraft.getWindow() != null && Q.init(init_Matrix4f)) {
         if (init_Obj != init_Minecraft.level) {
            init_Int2ObjectOpenHash.clear();
            init_Obj = init_Minecraft.level;
         }

         long now = System.nanoTime();
         float dt = load_J == 0L ? 0.0F : Math.min(0.1F, (float)(now - load_J) / 1.0E9F);
         load_J = now;
         long frame = ++init_J;
         float screenW = cA.init_I();
         float screenH = cA.load_I();
         float px = screenW / Math.max(1, init_Minecraft.getWindow().getWidth());
         Vec3 camera = Q.init();
         float tickDelta = init_Minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         float healthLerp = dt <= 0.0F ? 1.0F : 1.0F - (float)Math.exp(-dt / 0.12F);
         Module espModule = al.init_Module_Str("esp");
         boolean fill = espModule != null && espModule.bool("fill");

         for (Entity entity : init_Minecraft.level.entitiesForRendering()) {
            try {
               float fade = am.init_F(entity);
               if (!(fade <= 0.0F)
                  && init_Z_Entity(entity, tickDelta, camera, screenW, screenH)
                  && !(init_an_DataUtil.read < 0.0F)
                  && !(init_an_DataUtil.write < 0.0F)
                  && !(init_an_DataUtil.init_F > screenW)
                  && !(init_an_DataUtil.load > screenH)) {
                  init_V_GuiGraphicsExtract(context, am.init_I(entity), fade, fill, px);
                  if (entity instanceof LivingEntity living) {
                     init_V_GuiGraphicsExtract_2(context, living, fade, px, healthLerp, frame);
                  }
               }
            } catch (Throwable var18) {
            }
         }

         init_V_J(frame);
      }
   }

   private static boolean init_Z_Entity(Entity entity, float tickDelta, Vec3 camera, float screenW, float screenH) {
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
      float centerW = init_Matrix4f.m03() * (float)cx + init_Matrix4f.m13() * midY + init_Matrix4f.m23() * (float)cz + init_Matrix4f.m33();
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
      an.Inner_DataUtil box = init_an_DataUtil;
      box.init(screenW, screenH);
      return box.init(cx + sideX, bottom, cz + sideZ)
         && box.init(cx - sideX, bottom, cz - sideZ)
         && box.init(cx + sideX, top, cz + sideZ)
         && box.init(cx - sideX, top, cz - sideZ)
         && box.init(cx + alongX, bottom, cz + alongZ)
         && box.init(cx - alongX, bottom, cz - alongZ)
         && box.init(cx + alongX, top, cz + alongZ)
         && box.init(cx - alongX, top, cz - alongZ)
         && box.read - box.init_F > 0.5F
         && box.write - box.load > 0.5F;
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, int rgb, float fade, boolean fill, float px) {
      float x = init_an_DataUtil.init_F;
      float y = init_an_DataUtil.load;
      float w = init_an_DataUtil.read - x;
      float h = init_an_DataUtil.write - y;
      float radius = Math.max(0.75F, 2.0F * px);
      int shadow = init_I_F(0.62F * fade);
      if (fill) {
         autismclient.gui.e.BufferHelper.init_V_GuiGraphicsExtract(context, x, y, w, h, radius, init_I_F(0.2F * fade) | rgb, init_I_F(0.05F * fade) | rgb, 0, 0);
      }

      autismclient.gui.e.BufferHelper.init(context, x - px, y - px, w + 2.0F * px, h + 2.0F * px, radius + px, 0, shadow);
      autismclient.gui.e.BufferHelper.init(context, x, y, w, h, radius, 0, init_I_F(fade) | rgb);
      if (w > 4.0F * px && h > 4.0F * px) {
         autismclient.gui.e.BufferHelper.init(context, x + px, y + px, w - 2.0F * px, h - 2.0F * px, Math.max(0.75F, radius - px), 0, shadow);
      }
   }

   private static void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor context, LivingEntity entity, float fade, float px, float healthLerp, long frame) {
      float health = Mth.clamp(entity.getHealth() / Math.max(1.0F, entity.getMaxHealth()), 0.0F, 1.0F);
      an.Inner_StreamUtil shown = init_Int2ObjectOpenHash.get(entity.getId());
      if (shown == null) {
         shown = new an.Inner_StreamUtil(health);
         init_Int2ObjectOpenHash.put(entity.getId(), shown);
      } else {
         shown.init_F = shown.init_F + (health - shown.init_F) * healthLerp;
      }

      shown.init_J = frame;
      float y = init_an_DataUtil.load;
      float h = init_an_DataUtil.write - init_an_DataUtil.load;
      float barW = 5.0F * px;
      float x = init_an_DataUtil.init_F - 3.0F * px - barW;
      init_V_GuiGraphicsExtract(context, x, y - px, barW, h + 2.0F * px, init_I_F(0.62F * fade));
      float inner = barW - 2.0F * px;
      float filled = h * shown.init_F;
      if (!(filled <= 0.25F * px)) {
         int color = init_I_F(fade) | Mth.hsvToRgb(shown.init_F / 3.0F, 0.78F, 1.0F);
         init_V_GuiGraphicsExtract(context, x + px, y + h - filled, inner, filled, color);
      }
   }

   private static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor context, float x, float y, float w, float h, int color) {
      float radius = Math.min(w, h) * 0.5F;
      if (radius >= 0.75F && autismclient.gui.e.BufferHelper.init_Z()) {
         autismclient.gui.e.BufferHelper.init(context, x, y, w, h, radius, color);
      } else {
         context.pose().pushMatrix();
         context.pose().translate(x, y);
         context.pose().scale(w, h);
         context.fill(0, 0, 1, 1, color);
         context.pose().popMatrix();
      }
   }

   private static int init_I_F(float opacity) {
      return Math.round(Mth.clamp(opacity, 0.0F, 1.0F) * 255.0F) << 24;
   }

   private static void init_V_J(long frame) {
      if ((frame & 63L) == 0L && !init_Int2ObjectOpenHash.isEmpty()) {
         init_Int2ObjectOpenHash.values().removeIf(shown -> frame - shown.init_J > 20L);
      }
   }

   private static final class Inner_DataUtil {
      private final Vector4f init_Vector4f = new Vector4f();
      float init_F;
      float load;
      float read;
      float write;
      private float update;
      private float sync;

      void init(float screenW, float screenH) {
         this.update = screenW;
         this.sync = screenH;
         this.init_F = this.load = 3.4028235E38F;
         this.read = this.write = -3.4028235E38F;
      }

      boolean init(double x, double y, double z) {
         this.init_Vector4f.set((float)x, (float)y, (float)z, 1.0F);
         an.init_Matrix4f.transform(this.init_Vector4f);
         if (this.init_Vector4f.w <= 0.05F) {
            return false;
         }

         float sx = (this.init_Vector4f.x / this.init_Vector4f.w * 0.5F + 0.5F) * this.update;
         float sy = (0.5F - this.init_Vector4f.y / this.init_Vector4f.w * 0.5F) * this.sync;
         if (Float.isFinite(sx) && Float.isFinite(sy)) {
            if (sx < this.init_F) {
               this.init_F = sx;
            }

            if (sx > this.read) {
               this.read = sx;
            }

            if (sy < this.load) {
               this.load = sy;
            }

            if (sy > this.write) {
               this.write = sy;
            }

            return true;
         } else {
            return false;
         }
      }
   }

   private static final class Inner_StreamUtil {
      float init_F;
      long init_J;

      Inner_StreamUtil(float value) {
         this.init_F = value;
      }
   }
}
