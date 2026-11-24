package net.minecraft.client.renderer.rendertype;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline.Builder;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class BufferHelper {
   private static final RenderType init = RenderType.create("autism_trajectory_path", RenderSetup.builder(init_RenderPipeline(false)).createRenderSetup());
   private static final RenderType load = RenderType.create(
      "autism_trajectory_path_occluded", RenderSetup.builder(init_RenderPipeline(true)).createRenderSetup()
   );
   private static final RenderType read = RenderType.create("autism_trajectory_impact", RenderSetup.builder(load(false)).createRenderSetup());
   private static final RenderType write = RenderType.create("autism_trajectory_impact_occluded", RenderSetup.builder(load(true)).createRenderSetup());

   private static RenderPipeline init_RenderPipeline(boolean hidden) {
      return init(RenderPipeline.builder(), RenderPipelines.LINES)
         .withLocation(init("pipeline/trajectory_path" + (hidden ? "_occluded" : "")))
         .withVertexShader(init("core/trajectory_path"))
         .withFragmentShader(init("core/trajectory_path"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
         .withPrimitiveTopology(PrimitiveTopology.LINES)
         .withDepthStencilState(init_DepthStencilState(hidden))
         .build();
   }

   private static RenderPipeline load(boolean hidden) {
      return init(RenderPipeline.builder(), RenderPipelines.DEBUG_FILLED_BOX)
         .withLocation(init("pipeline/trajectory_impact" + (hidden ? "_occluded" : "")))
         .withVertexShader(init("core/trajectory_impact"))
         .withFragmentShader(init("core/trajectory_impact"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
         .withPrimitiveTopology(PrimitiveTopology.QUADS)
         .withDepthStencilState(init_DepthStencilState(hidden))
         .build();
   }

   private static DepthStencilState init_DepthStencilState(boolean hidden) {
      return new DepthStencilState(hidden ? CompareOp.LESS_THAN : CompareOp.GREATER_THAN_OR_EQUAL, false);
   }

   private BufferHelper() {
   }

   public static RenderType init() {
      return init;
   }

   public static RenderType load() {
      return read;
   }

   public static RenderType read() {
      return load;
   }

   public static RenderType write() {
      return write;
   }

   private static Identifier init(String path) {
      return Identifier.fromNamespaceAndPath("autismclient", path);
   }

   private static Builder init(Builder builder, RenderPipeline template) {
      for (BindGroupLayout layout : template.getBindGroupLayouts()) {
         builder.withBindGroupLayout(layout);
      }

      return builder;
   }
}
