package autismclient.gui.e;

import autismclient.compat263.Compat263;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class JsonUtil {
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "ui/text_msdf");
   private static final Identifier load = Identifier.fromNamespaceAndPath("autismclient", "ui/sdf_shape");
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline load_RenderPipeline;
   private static boolean init_Z;

   private JsonUtil() {
   }

   public static boolean init_Z() {
      init_V();
      return init_RenderPipeline != null && load_RenderPipeline != null;
   }

   public static RenderPipeline init_RenderPipeline() {
      init_V();
      return init_RenderPipeline != null ? init_RenderPipeline : RenderPipelines.GUI_TEXTURED;
   }

   public static RenderPipeline load() {
      init_V();
      return load_RenderPipeline != null ? load_RenderPipeline : RenderPipelines.GUI_TEXTURED;
   }

   private static void init_V() {
      if (!init_Z) {
         init_Z = true;

         try {
            init_RenderPipeline = init("ui_text_msdf", init_Identifier);
            load_RenderPipeline = init("ui_sdf_shape", load);
         } catch (Throwable t) {
            init_RenderPipeline = null;
            load_RenderPipeline = null;
         }
      }
   }

   private static RenderPipeline init(String name, Identifier fragment) {
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
