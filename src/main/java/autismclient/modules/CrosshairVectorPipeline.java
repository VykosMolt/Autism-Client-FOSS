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
   private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath("autismclient", "ui/crosshair_vector");
   private static final int init_I = 128;
   private static final LinkedHashMap<CrosshairVectorPipeline.StyleKey, CrosshairVectorPipeline.ParameterTexture> parameterCache = new LinkedHashMap<>(16, 0.75F, true);
   private static final ArrayDeque<CrosshairVectorPipeline.ParameterTexture> retired = new ArrayDeque<>();
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline invertPipeline;
   private static boolean initialized;

   private CrosshairVectorPipeline() {
   }

   static boolean drawPreset(GuiGraphicsExtractor graphics, CrosshairRenderState.Snapshot look, float centerX, float centerY, float pixelsPerGuiUnit, int maxPixels, int target) {
      if (!look.style().isPreset() || !ensurePipelines()) {
         return false;
      }

      if (Float.isFinite(centerX) && Float.isFinite(centerY) && Float.isFinite(pixelsPerGuiUnit) && !(pixelsPerGuiUnit <= 0.0F)) {
         CrosshairVectorSpec shape = new CrosshairVectorSpec(look.style(), look.load_D(), look.read_D());
         double border = look.load_Z() ? look.write_D() : 0.0;
         double span = shape.span(pixelsPerGuiUnit, look.init_D(), border);
         if (Double.isFinite(span) && !(span <= 4.0)) {
            double fit = maxPixels > 0 ? Math.clamp((maxPixels - 4.0) / (span - 4.0), 0.0, 1.0) : 1.0;
            if (fit <= 0.0) {
               return true;
            }

            float unit = (float)(pixelsPerGuiUnit * look.init_D() * fit);
            int stroke = CrosshairPresetGeometry.strokeWidth(look.load_D(), unit);
            float gap = (float)Math.round(look.read_D() * unit);
            float outline = border > 0.0 ? (float)Math.max(1L, Math.round(border * pixelsPerGuiUnit * fit)) : 0.0F;
            float extent = Math.max((float)shape.extent() * unit + 1.0F, 7.0F * unit + stroke * 1.25F + 2.0F) + outline + 2.0F;
            Matrix3x2fStack pose = graphics.pose();
            centerX = snapToPixel(centerX, pose.m00(), pose.m20(), pixelsPerGuiUnit, stroke);
            centerY = snapToPixel(centerY, pose.m11(), pose.m21(), pixelsPerGuiUnit, stroke);
            boolean inverted = look.init_Z() && target == 0;
            int fill = inverted ? -1 : (target != 0 ? target : look.init_I());
            CrosshairVectorPipeline.StyleKey key = new CrosshairVectorPipeline.StyleKey(
               shape.style().ordinal(), stroke, gap, outline, unit, fill, look.load_Z() ? look.load_I() : 0, Math.clamp(look.init_F(), 0.0F, 1.0F)
            );
            CrosshairVectorPipeline.ParameterTexture parameters = parametersFor(key);
            float[] quad = square(centerX, centerY, extent);
            if (inverted) {
               graphics.nextStratum();
               init_V_GuiGraphicsExtract_2(graphics, invertPipeline, parameters, quad, 1);
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

   static void drawEllipse(GuiGraphicsExtractor graphics, float x, float y, float radiusX, float radiusY) {
      if (ensurePipelines()) {
         float w = radiusX + 2.0F;
         float h = radiusY + 2.0F;
         float[] quad = new float[]{x - w, y - h, x + w, y + h, -w / radiusX, -h / radiusY, w / radiusX, h / radiusY};
         CrosshairVectorPipeline.ParameterTexture parameters = parametersFor(new CrosshairVectorPipeline.StyleKey(4, 0.0F, 0.0F, 0.0F, 0.0F, -1275068417, 0, 1.0F));
         init_V_GuiGraphicsExtract_2(graphics, init_RenderPipeline, parameters, quad, 0);
      }
   }

   private static float[] square(float x, float y, float extent) {
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

   private static CrosshairVectorPipeline.ParameterTexture parametersFor(CrosshairVectorPipeline.StyleKey key) {
      CrosshairVectorPipeline.ParameterTexture parameters = parameterCache.get(key);
      if (parameters == null) {
         parameters = new CrosshairVectorPipeline.ParameterTexture(key);
         parameterCache.put(key, parameters);
         if (parameterCache.size() > 128) {
            Iterator<CrosshairVectorPipeline.ParameterTexture> iterator = parameterCache.values().iterator();
            retired.add(iterator.next());
            iterator.remove();
         }
      }

      return parameters;
   }

   private static void init_V_GuiGraphicsExtract_2(
      GuiGraphicsExtractor graphics, RenderPipeline pipeline, CrosshairVectorPipeline.ParameterTexture parameters, float[] quad, int mode
   ) {
      autismclient.gui.sdf.TexturedQuadRenderState element = autismclient.gui.sdf.TexturedQuadRenderState.create(
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

   private static boolean ensurePipelines() {
      if (!initialized) {
         initialized = true;

         try {
            init_RenderPipeline = createPipeline("crosshair_vector", RenderPipelines.GUI_TEXTURED);
            invertPipeline = createPipeline("crosshair_vector_invert", RenderPipelines.CROSSHAIR);
            ClientTickEvents.END_CLIENT_TICK.register(mc -> {
               while (!retired.isEmpty()) {
                  retired.removeFirst().close();
               }
            });
            ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
               parameterCache.values().forEach(AbstractTexture::close);
               parameterCache.clear();

               while (!retired.isEmpty()) {
                  retired.removeFirst().close();
               }
            });
         } catch (RuntimeException | LinkageError error) {
            invertPipeline = null;
            init_RenderPipeline = null;
         }
      }

      return init_RenderPipeline != null && invertPipeline != null;
   }

   private static RenderPipeline createPipeline(String name, RenderPipeline base) {
      return Compat263.createRenderPipeline(
         Identifier.fromNamespaceAndPath("autismclient", "pipeline/" + name),
         Compat263.getVertexShader(base),
         PIPELINE_ID,
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

   private record StyleKey(int init_I, float stroke, float gap, float outline, float unit, int fill, int read_I, float update) {
   }

   private static final class ParameterTexture extends AbstractTexture {
      ParameterTexture(CrosshairVectorPipeline.StyleKey key) {
         ByteBuffer data = MemoryUtil.memAlloc(64).order(ByteOrder.nativeOrder());

         try {
            data.putFloat(key.stroke()).putFloat(key.gap()).putFloat(key.outline()).putFloat(key.init_I());
            putColor(data, key.fill(), key.update());
            putColor(data, key.read_I(), key.update());
            data.putFloat(key.unit()).putFloat(0.0F).putFloat(0.0F).putFloat(0.0F);
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

      private static void putColor(ByteBuffer data, int color, float opacity) {
         data.putFloat((color >>> 16 & 0xFF) / 255.0F)
            .putFloat((color >>> 8 & 0xFF) / 255.0F)
            .putFloat((color & 0xFF) / 255.0F)
            .putFloat((color >>> 24) / 255.0F * opacity);
      }
   }
}
