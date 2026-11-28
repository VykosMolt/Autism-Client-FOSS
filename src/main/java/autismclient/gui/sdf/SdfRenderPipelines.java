package autismclient.gui.sdf;

import autismclient.compat263.Compat263;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class SdfRenderPipelines {
   private static final Identifier TEXT_FRAGMENT = Identifier.fromNamespaceAndPath("autismclient", "ui/text_msdf");
   private static final Identifier SHAPE_FRAGMENT = Identifier.fromNamespaceAndPath("autismclient", "ui/sdf_shape");
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline load_RenderPipeline;
   private static boolean initialized;

   private SdfRenderPipelines() {
   }

   public static boolean isAvailable() {
      ensurePipelines();
      return init_RenderPipeline != null && load_RenderPipeline != null;
   }

   public static RenderPipeline init_RenderPipeline() {
      ensurePipelines();
      return init_RenderPipeline != null ? init_RenderPipeline : RenderPipelines.GUI_TEXTURED;
   }

   public static RenderPipeline pipeline() {
      ensurePipelines();
      return load_RenderPipeline != null ? load_RenderPipeline : RenderPipelines.GUI_TEXTURED;
   }

   private static void ensurePipelines() {
      if (!initialized) {
         initialized = true;

         try {
            init_RenderPipeline = createPipeline("ui_text_msdf", TEXT_FRAGMENT);
            load_RenderPipeline = createPipeline("ui_sdf_shape", SHAPE_FRAGMENT);
         } catch (Throwable t) {
            init_RenderPipeline = null;
            load_RenderPipeline = null;
         }
      }
   }

   private static RenderPipeline createPipeline(String name, Identifier fragment) {
      RenderPipeline base = RenderPipelines.GUI_TEXTURED;
      return Compat263.createRenderPipeline(
         Identifier.fromNamespaceAndPath("autismclient", "pipeline/" + name),
         Compat263.getVertexShader(base),
         fragment,
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
}
