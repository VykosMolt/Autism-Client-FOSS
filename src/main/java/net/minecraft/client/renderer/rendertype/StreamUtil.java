package net.minecraft.client.renderer.rendertype;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.HashMap;
import java.util.Map;

public final class StreamUtil {
   private static final Map<RenderPipeline, RenderType> init = new HashMap<>();

   private StreamUtil() {
   }

   public static RenderType init(RenderPipeline pipeline) {
      return init.computeIfAbsent(pipeline, key -> RenderType.create("autism_pearl_ghost", RenderSetup.builder(key).createRenderSetup()));
   }
}
