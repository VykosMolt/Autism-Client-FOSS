package net.minecraft.client.renderer.rendertype;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatOutputTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BindGroupLayout;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline.Builder;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class CachePool {
   private static final RenderPipeline init_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.DEBUG_FILLED_BOX)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/storage_esp_fill_see_through"))
      .withVertexShader("core/position_color")
      .withFragmentShader("core/position_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderPipeline load_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/storage_esp_lines_see_through"))
      .withVertexShader("core/rendertype_lines")
      .withFragmentShader("core/rendertype_lines")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType init_RenderType = RenderType.create(
      "autism_storage_esp_fill_see_through", RenderSetup.builder(init_RenderPipeline).sortOnUpload().createRenderSetup()
   );
   private static final RenderType load_RenderType = RenderType.create(
      "autism_storage_esp_lines_see_through", RenderSetup.builder(load_RenderPipeline).createRenderSetup()
   );
   private static final RenderPipeline read_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/tracer_esp_lines"))
      .withVertexShader(Identifier.fromNamespaceAndPath("autismclient", "core/fogless_lines"))
      .withFragmentShader(Identifier.fromNamespaceAndPath("autismclient", "core/fogless_lines"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType read = RenderType.create(
      "autism_tracer_esp_lines",
      Compat263.setOutputTarget(
            RenderSetup.builder(read_RenderPipeline).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING), CompatOutputTarget.ITEM_ENTITY_TARGET
         )
         .createRenderSetup()
   );
   private static final RenderPipeline write_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/waypoint_disc_see_through"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final Function<Identifier, RenderType> init_Function = Util.memoize(
      texture -> RenderType.create(
         "autism_waypoint_disc_see_through", RenderSetup.builder(write_RenderPipeline).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup()
      )
   );
   private static final RenderPipeline update_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/ore_ghost_see_through"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(ColorTargetState.DEFAULT)
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final RenderType write = RenderType.create(
      "autism_ore_ghost_see_through",
      RenderSetup.builder(update_RenderPipeline)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );
   private static final RenderPipeline sync_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/ai_ghost"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final RenderType update = RenderType.create(
      "autism_ai_ghost",
      RenderSetup.builder(sync_RenderPipeline)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );
   private static final RenderPipeline flush_RenderPipeline = init(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/ai_plan_lines"))
      .withVertexShader(Identifier.fromNamespaceAndPath("autismclient", "core/fogless_lines"))
      .withFragmentShader(Identifier.fromNamespaceAndPath("autismclient", "core/fogless_lines"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType sync = RenderType.create("autism_ai_plan_lines", RenderSetup.builder(flush_RenderPipeline).createRenderSetup());
   private static final RenderPipeline close = init(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(Identifier.fromNamespaceAndPath("autismclient", "pipeline/ai_plan_ghost"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
      .build();
   private static final RenderType flush = RenderType.create(
      "autism_ai_plan_ghost",
      RenderSetup.builder(close)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );
   private static final Function<Identifier, RenderType> load_Function = Util.memoize(
      texture -> RenderType.create(
         "autism_female_body_translucent_cull",
         RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .affectsCrumbling()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE)
            .createRenderSetup()
      )
   );

   public static RenderType init() {
      return flush;
   }

   public static RenderType load() {
      return update;
   }

   public static RenderType read() {
      return sync;
   }

   private CachePool() {
   }

   public static RenderType init(Identifier texture) {
      return RenderType.create(
         "autism_skin_preview",
         RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT).withTexture("Sampler0", texture).useLightmap().useOverlay().createRenderSetup()
      );
   }

   public static RenderType write() {
      return init_RenderType;
   }

   public static RenderType update() {
      return load_RenderType;
   }

   public static RenderType sync() {
      return read;
   }

   public static RenderType load(Identifier texture) {
      return init_Function.apply(texture);
   }

   public static RenderType flush() {
      return write;
   }

   public static RenderType read(Identifier texture) {
      return load_Function.apply(texture);
   }

   private static Builder init(Builder builder, RenderPipeline template) {
      for (BindGroupLayout layout : template.getBindGroupLayouts()) {
         builder.withBindGroupLayout(layout);
      }

      return builder;
   }
}
