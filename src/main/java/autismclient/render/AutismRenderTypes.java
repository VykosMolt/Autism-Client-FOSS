package autismclient.render;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatOutputTarget;
import autismclient.mixin.AutismRenderTypeStateAccessor;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public final class AutismRenderTypes {
   private static final RenderType TRAJECTORY_PATH = AutismRenderTypeStateAccessor.autism$create(
      "autism_trajectory_path", RenderSetup.builder(trajectoryPathPipeline(false)).createRenderSetup()
   );
   private static final RenderType TRAJECTORY_PATH_OCCLUDED = AutismRenderTypeStateAccessor.autism$create(
      "autism_trajectory_path_occluded", RenderSetup.builder(trajectoryPathPipeline(true)).createRenderSetup()
   );
   private static final RenderType TRAJECTORY_IMPACT = AutismRenderTypeStateAccessor.autism$create(
      "autism_trajectory_impact", RenderSetup.builder(trajectoryImpactPipeline(false)).createRenderSetup()
   );
   private static final RenderType TRAJECTORY_IMPACT_OCCLUDED = AutismRenderTypeStateAccessor.autism$create(
      "autism_trajectory_impact_occluded", RenderSetup.builder(trajectoryImpactPipeline(true)).createRenderSetup()
   );

   private static final RenderPipeline STORAGE_ESP_FILL_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.DEBUG_FILLED_BOX)
      .withLocation(id("pipeline/storage_esp_fill_see_through"))
      .withVertexShader("core/position_color")
      .withFragmentShader("core/position_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderPipeline STORAGE_ESP_LINES_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(id("pipeline/storage_esp_lines_see_through"))
      .withVertexShader("core/rendertype_lines")
      .withFragmentShader("core/rendertype_lines")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType STORAGE_ESP_FILL = AutismRenderTypeStateAccessor.autism$create(
      "autism_storage_esp_fill_see_through", RenderSetup.builder(STORAGE_ESP_FILL_PIPELINE).sortOnUpload().createRenderSetup()
   );
   private static final RenderType STORAGE_ESP_LINES = AutismRenderTypeStateAccessor.autism$create(
      "autism_storage_esp_lines_see_through", RenderSetup.builder(STORAGE_ESP_LINES_PIPELINE).createRenderSetup()
   );

   private static final RenderPipeline TRACER_ESP_LINES_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(id("pipeline/tracer_esp_lines"))
      .withVertexShader(id("core/fogless_lines"))
      .withFragmentShader(id("core/fogless_lines"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType TRACER_ESP_LINES = AutismRenderTypeStateAccessor.autism$create(
      "autism_tracer_esp_lines",
      Compat263.setOutputTarget(
            RenderSetup.builder(TRACER_ESP_LINES_PIPELINE).setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING), CompatOutputTarget.ITEM_ENTITY_TARGET
         )
         .createRenderSetup()
   );

   private static final RenderPipeline WAYPOINT_DISC_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(id("pipeline/waypoint_disc_see_through"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final Function<Identifier, RenderType> WAYPOINT_DISC = Util.memoize(
      texture -> AutismRenderTypeStateAccessor.autism$create(
         "autism_waypoint_disc_see_through", RenderSetup.builder(WAYPOINT_DISC_PIPELINE).withTexture("Sampler0", texture).sortOnUpload().createRenderSetup()
      )
   );

   private static final RenderPipeline ORE_GHOST_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(id("pipeline/ore_ghost_see_through"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(ColorTargetState.DEFAULT)
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final RenderType ORE_GHOST = AutismRenderTypeStateAccessor.autism$create(
      "autism_ore_ghost_see_through",
      RenderSetup.builder(ORE_GHOST_PIPELINE)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );

   private static final RenderPipeline AI_GHOST_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(id("pipeline/ai_ghost"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
      .build();
   private static final RenderType AI_GHOST = AutismRenderTypeStateAccessor.autism$create(
      "autism_ai_ghost",
      RenderSetup.builder(AI_GHOST_PIPELINE)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );

   private static final RenderPipeline AI_PLAN_LINES_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.LINES)
      .withLocation(id("pipeline/ai_plan_lines"))
      .withVertexShader(id("core/fogless_lines"))
      .withFragmentShader(id("core/fogless_lines"))
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
      .withPrimitiveTopology(PrimitiveTopology.LINES)
      .withDepthStencilState(Optional.empty())
      .build();
   private static final RenderType AI_PLAN_LINES = AutismRenderTypeStateAccessor.autism$create(
      "autism_ai_plan_lines", RenderSetup.builder(AI_PLAN_LINES_PIPELINE).createRenderSetup()
   );

   private static final RenderPipeline AI_PLAN_GHOST_PIPELINE = copyLayouts(RenderPipeline.builder(), RenderPipelines.GUI_TEXTURED)
      .withLocation(id("pipeline/ai_plan_ghost"))
      .withVertexShader("core/position_tex_color")
      .withFragmentShader("core/position_tex_color")
      .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
      .withCull(false)
      .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
      .withPrimitiveTopology(PrimitiveTopology.QUADS)
      .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true))
      .build();
   private static final RenderType AI_PLAN_GHOST = AutismRenderTypeStateAccessor.autism$create(
      "autism_ai_plan_ghost",
      RenderSetup.builder(AI_PLAN_GHOST_PIPELINE)
         .withTexture(
            "Sampler0",
            Sheets.BLOCKS_MAPPER.sheet(),
            () -> RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, true)
         )
         .createRenderSetup()
   );

   private static final Map<RenderPipeline, RenderType> PEARL_GHOST = new HashMap<>();

   private AutismRenderTypes() {
   }

   public static RenderType trajectoryPath(boolean occluded) {
      return occluded ? TRAJECTORY_PATH_OCCLUDED : TRAJECTORY_PATH;
   }

   public static RenderType trajectoryImpact(boolean occluded) {
      return occluded ? TRAJECTORY_IMPACT_OCCLUDED : TRAJECTORY_IMPACT;
   }

   public static RenderType aiPlanGhost() {
      return AI_PLAN_GHOST;
   }

   public static RenderType aiGhost() {
      return AI_GHOST;
   }

   public static RenderType aiPlanLines() {
      return AI_PLAN_LINES;
   }

   public static RenderType storageEspFillSeeThrough() {
      return STORAGE_ESP_FILL;
   }

   public static RenderType storageEspLinesSeeThrough() {
      return STORAGE_ESP_LINES;
   }

   public static RenderType tracerEspLines() {
      return TRACER_ESP_LINES;
   }

   public static RenderType waypointDiscSeeThrough(Identifier texture) {
      return WAYPOINT_DISC.apply(texture);
   }

   public static RenderType oreGhostSeeThrough() {
      return ORE_GHOST;
   }

   public static RenderType pearlGhost(RenderPipeline pipeline) {
      return PEARL_GHOST.computeIfAbsent(
         pipeline, key -> AutismRenderTypeStateAccessor.autism$create("autism_pearl_ghost", RenderSetup.builder(key).createRenderSetup())
      );
   }

   private static RenderPipeline trajectoryPathPipeline(boolean hidden) {
      return copyLayouts(RenderPipeline.builder(), RenderPipelines.LINES)
         .withLocation(id("pipeline/trajectory_path" + (hidden ? "_occluded" : "")))
         .withVertexShader(id("core/trajectory_path"))
         .withFragmentShader(id("core/trajectory_path"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH)
         .withPrimitiveTopology(PrimitiveTopology.LINES)
         .withDepthStencilState(trajectoryDepth(hidden))
         .build();
   }

   private static RenderPipeline trajectoryImpactPipeline(boolean hidden) {
      return copyLayouts(RenderPipeline.builder(), RenderPipelines.DEBUG_FILLED_BOX)
         .withLocation(id("pipeline/trajectory_impact" + (hidden ? "_occluded" : "")))
         .withVertexShader(id("core/trajectory_impact"))
         .withFragmentShader(id("core/trajectory_impact"))
         .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
         .withCull(false)
         .withVertexBinding(0, DefaultVertexFormat.POSITION_TEX_COLOR)
         .withPrimitiveTopology(PrimitiveTopology.QUADS)
         .withDepthStencilState(trajectoryDepth(hidden))
         .build();
   }

   private static DepthStencilState trajectoryDepth(boolean hidden) {
      return new DepthStencilState(hidden ? CompareOp.LESS_THAN : CompareOp.GREATER_THAN_OR_EQUAL, false);
   }

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("autismclient", path);
   }

   private static Builder copyLayouts(Builder builder, RenderPipeline template) {
      for (BindGroupLayout layout : template.getBindGroupLayouts()) {
         builder.withBindGroupLayout(layout);
      }

      return builder;
   }
}
