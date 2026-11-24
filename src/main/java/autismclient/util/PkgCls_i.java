package autismclient.util;

import autismclient.compat263.Compat263;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class PkgCls_i {
   private static RenderPipeline init_RenderPipeline;
   private static RenderPipeline load;
   private static boolean init_Z;
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "core/chams");

   private PkgCls_i() {
   }

   public static RenderPipeline init_RenderPipeline() {
      init();
      return init_RenderPipeline != null ? init_RenderPipeline : RenderPipelines.ENTITY_TRANSLUCENT_CULL;
   }

   public static RenderPipeline load() {
      init();
      return load != null ? load : RenderPipelines.ENTITY_TRANSLUCENT_CULL;
   }

   private static void init() {
      if (!init_Z) {
         init_Z = true;
         RenderPipeline base = RenderPipelines.ENTITY_TRANSLUCENT_CULL;

         try {
            DepthStencilState baseDepth = base.getDepthStencilState();
            CompareOp visibleOp = baseDepth == null ? CompareOp.GREATER_THAN_OR_EQUAL : baseDepth.depthTest();
            CompareOp occludedOp = init(visibleOp);
            init_RenderPipeline = init(base, "chams_visible", new DepthStencilState(visibleOp, true));
            load = init(base, "chams_occluded", new DepthStencilState(occludedOp, false));
         } catch (Throwable t) {
            init_RenderPipeline = base;
            load = base;
         }
      }
   }

   private static RenderPipeline init(RenderPipeline base, String name, DepthStencilState depth) {
      return Compat263.createRenderPipeline(
         Identifier.fromNamespaceAndPath("autismclient", "pipeline/" + name),
         Compat263.getVertexShader(base),
         init_Identifier,
         base.getShaderDefines(),
         base.getBindGroupLayouts(),
         base.getColorTargetStates().toArray(new ColorTargetState[0]),
         depth,
         base.getPolygonMode(),
         true,
         base.getVertexFormatBindings().toArray(new VertexFormat[0]),
         base.getPrimitiveTopology(),
         base.getSortKey()
      );
   }

   private static CompareOp init(CompareOp op) {
      CompareOp var10000;
      switch (op) {
         case GREATER_THAN_OR_EQUAL:
            var10000 = CompareOp.LESS_THAN;
            break;
         case GREATER_THAN:
            var10000 = CompareOp.LESS_THAN_OR_EQUAL;
            break;
         case LESS_THAN_OR_EQUAL:
            var10000 = CompareOp.GREATER_THAN;
            break;
         case LESS_THAN:
            var10000 = CompareOp.GREATER_THAN_OR_EQUAL;
            break;
         default:
            var10000 = CompareOp.LESS_THAN;
      }

      return var10000;
   }
}
