package autismclient.modules;

import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismGuiGraphicsExtractorAccessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.lwjgl.system.MemoryUtil;

final class CrosshairVectorPipeline {
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "ui/crosshair_vector");
   private static final int init_I = 128;
   private static final LinkedHashMap<CrosshairVectorPipeline.Inner_DataUtil, CrosshairVectorPipeline.Inner_StreamUtil> init_LinkedHashMap = new LinkedHashMap<>(16, 0.75F, true);
   private static final ArrayDeque<CrosshairVectorPipeline.Inner_StreamUtil> init_ArrayDeque = new ArrayDeque<>();
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline load;
   private static boolean init_Z;

   private CrosshairVectorPipeline() {
   }

   static boolean init(GuiGraphicsExtractor graphics, CrosshairRenderState.Inner_DataUtil look, float centerX, float centerY, float pixelsPerGuiUnit, int maxPixels, int target) {
      if (!look.style().isPreset() || !init()) {
         return false;
      }

      if (Float.isFinite(centerX) && Float.isFinite(centerY) && Float.isFinite(pixelsPerGuiUnit) && !(pixelsPerGuiUnit <= 0.0F)) {
         CrosshairVectorSpec shape = new CrosshairVectorSpec(look.style(), look.load_D(), look.read_D());
         double border = look.load_Z() ? look.write_D() : 0.0;
         double span = shape.init(pixelsPerGuiUnit, look.init_D(), border);
         if (Double.isFinite(span) && !(span <= 4.0)) {
            double fit = maxPixels > 0 ? Math.clamp((maxPixels - 4.0) / (span - 4.0), 0.0, 1.0) : 1.0;
            if (fit <= 0.0) {
               return true;
            }

            float unit = (float)(pixelsPerGuiUnit * look.init_D() * fit);
            int stroke = CrosshairPresetGeometry.init(look.load_D(), unit);
            float gap = (float)Math.round(look.read_D() * unit);
            float outline = border > 0.0 ? (float)Math.max(1L, Math.round(border * pixelsPerGuiUnit * fit)) : 0.0F;
            float extent = Math.max((float)shape.read() * unit + 1.0F, 7.0F * unit + stroke * 1.25F + 2.0F) + outline + 2.0F;
            Matrix3x2fStack pose = graphics.pose();
            centerX = snapToPixel(centerX, pose.m00(), pose.m20(), pixelsPerGuiUnit, stroke);
            centerY = snapToPixel(centerY, pose.m11(), pose.m21(), pixelsPerGuiUnit, stroke);
            boolean inverted = look.init_Z() && target == 0;
            int fill = inverted ? -1 : (target != 0 ? target : look.init_I());
            CrosshairVectorPipeline.Inner_DataUtil key = new CrosshairVectorPipeline.Inner_DataUtil(
               shape.style().ordinal(), stroke, gap, outline, unit, fill, look.load_Z() ? look.load_I() : 0, Math.clamp(look.init_F(), 0.0F, 1.0F)
            );
            CrosshairVectorPipeline.Inner_StreamUtil parameters = init(key);
            float[] quad = init(centerX, centerY, extent);
            if (inverted) {
               graphics.nextStratum();
               init_V_GuiGraphicsExtract_2(graphics, load, parameters, quad, 1);
               if (outline > 0.0F && look.load_I() >>> 24 != 0) {
                  graphics.nextStratum();
                  init_V_GuiGraphicsExtract_2(graphics, init_RenderPipeline, parameters, quad, 2);
               }

               graphics.nextStratum();
            } else {
               init_V_GuiGraphicsExtract_2(graphics, init_RenderPipeline, parameters, quad, 0);
            }

            return true;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }

   static void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, float x, float y, float radiusX, float radiusY) {
      if (init()) {
         float w = radiusX + 2.0F;
         float h = radiusY + 2.0F;
         float[] quad = new float[]{x - w, y - h, x + w, y + h, -w / radiusX, -h / radiusY, w / radiusX, h / radiusY};
         CrosshairVectorPipeline.Inner_StreamUtil parameters = init(new CrosshairVectorPipeline.Inner_DataUtil(4, 0.0F, 0.0F, 0.0F, 0.0F, -1275068417, 0, 1.0F));
         init_V_GuiGraphicsExtract_2(graphics, init_RenderPipeline, parameters, quad, 0);
      }
   }

   private static float[] init(float x, float y, float extent) {
      return new float[]{x - extent, y - extent, x + extent, y + extent, -extent, -extent, extent, extent};
   }

   static float snapToPixel(float position, float scale, float offset, float guiScale, int stroke) {
      float pixelSize = scale * guiScale;
      if (!(pixelSize > 0.0F)) {
         return position;
      }

      float screenPosition = position * pixelSize + offset * guiScale;
      float half = (stroke & 1) * 0.5F;
      return position + (Math.round(screenPosition - half) + half - screenPosition) / pixelSize;
   }

   private static CrosshairVectorPipeline.Inner_StreamUtil init(CrosshairVectorPipeline.Inner_DataUtil key) {
      CrosshairVectorPipeline.Inner_StreamUtil parameters = init_LinkedHashMap.get(key);
      if (parameters == null) {
         parameters = new CrosshairVectorPipeline.Inner_StreamUtil(key);
         init_LinkedHashMap.put(key, parameters);
         if (init_LinkedHashMap.size() > 128) {
            Iterator<CrosshairVectorPipeline.Inner_StreamUtil> iterator = init_LinkedHashMap.values().iterator();
            init_ArrayDeque.add(iterator.next());
            iterator.remove();
         }
      }

      return parameters;
   }

   private static void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor graphics, RenderPipeline pipeline, CrosshairVectorPipeline.Inner_StreamUtil parameters, float[] quad, int mode
   ) {
      autismclient.gui.sdf.TexturedQuadRenderState element = autismclient.gui.sdf.TexturedQuadRenderState.init(
         pipeline,
         TextureSetup.singleTexture(parameters.getTextureView(), parameters.getSampler()),
         new Matrix3x2f(graphics.pose()),
         quad,
         1,
         0xFF000000 | mode << 16,
         autismclient.gui.sdf.UiScissorStack.peek()
      );
      if (element != null) {
         ((AutismGuiGraphicsExtractorAccessor)graphics).autism$guiRenderState().addGuiElement(element);
      }
   }

   private static boolean init() {
      if (!init_Z) {
         init_Z = true;

         try {
            init_RenderPipeline = init("crosshair_vector", RenderPipelines.GUI_TEXTURED);
            load = init("crosshair_vector_invert", RenderPipelines.CROSSHAIR);
            ClientTickEvents.END_CLIENT_TICK.register(mc -> {
               while (!init_ArrayDeque.isEmpty()) {
                  init_ArrayDeque.removeFirst().close();
               }
            });
            ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
               init_LinkedHashMap.values().forEach(AbstractTexture::close);
               init_LinkedHashMap.clear();

               while (!init_ArrayDeque.isEmpty()) {
                  init_ArrayDeque.removeFirst().close();
               }
            });
         } catch (RuntimeException | LinkageError error) {
            load = null;
            init_RenderPipeline = null;
         }
      }

      return init_RenderPipeline != null && load != null;
   }

   private static RenderPipeline init(String name, RenderPipeline base) {
      return Compat263.createRenderPipeline(
         Identifier.fromNamespaceAndPath("autismclient", "pipeline/" + name),
         Compat263.getVertexShader(base),
         init_Identifier,
         base.getShaderDefines(),
         base.getBindGroupLayouts(),
         base.getColorTargetStates().toArray(new ColorTargetState[0]),
         base.getDepthStencilState(),
         base.getPolygonMode(),
         base.isCull(),
         base.getVertexFormatBindings().toArray(new VertexFormat[0]),
         base.getPrimitiveTopology(),
         base.getSortKey()
      );
   }

   private record Inner_DataUtil(int init_I, float init_F, float load_F, float read_F, float write, int load_I, int read_I, float update) {
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      Inner_StreamUtil(CrosshairVectorPipeline.Inner_DataUtil key) {
         ByteBuffer data = MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());

         try {
            data.putFloat(key.init_F()).putFloat(key.load_F()).putFloat(key.read_F()).putFloat(key.init_I());
            init(data, key.load_I(), key.update());
            init(data, key.read_I(), key.update());
            data.putFloat(key.write()).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
            data.flip();
            this.texture = RenderSystem.getDevice().createTexture("crosshair vector parameters", 15, GpuFormat.RGBA32_FLOAT, 4, 1, 1, 1);
            this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
            this.sampler = RenderSystem.getSamplerCache()
               .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.NEAREST, FilterMode.NEAREST, false);

            try (GpuBuffer upload = RenderSystem.getDevice().createBuffer(() -> "crosshair parameter upload", 16, data)) {
               RenderSystem.getDevice().createCommandEncoder().copyBufferToTexture(upload.slice(), 0, 0, 4, 1, this.texture, 0, 0, 4, 1, 0, 0);
            }
         } catch (RuntimeException | Error error) {
            this.close();
            throw error;
         } finally {
            MemoryUtil.memFree(data);
         }
      }

      private static void init(ByteBuffer data, int color, float opacity) {
         data.putFloat((color >>> 16 & 0xFF) / 255.0F)
            .putFloat((color >>> 8 & 0xFF) / 255.0F)
            .putFloat((color & 0xFF) / 255.0F)
            .putFloat((color >>> 24) / 255.0F * opacity);
      }
   }
}
