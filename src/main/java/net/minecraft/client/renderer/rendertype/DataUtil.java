package net.minecraft.client.renderer.rendertype;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.rendertype.RenderSetup.TextureBinding;
import net.minecraft.resources.Identifier;

public final class DataUtil {
   private static final Map<Identifier, RenderType> init = new ConcurrentHashMap<>();
   private static final Map<Identifier, RenderType> load = new ConcurrentHashMap<>();

   private DataUtil() {
   }

   public static Identifier init(RenderSetup setup) {
      try {
         TextureBinding binding = setup.textures.get("Sampler0");
         return binding == null ? null : binding.location();
      } catch (Throwable t) {
         return null;
      }
   }

   public static RenderType init(Identifier texture, RenderPipeline pipeline) {
      return init.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "autism_chams_visible", RenderSetup.builder(pipeline).withTexture("Sampler0", tex).useLightmap().useOverlay().createRenderSetup()
         )
      );
   }

   public static RenderType load(Identifier texture, RenderPipeline pipeline) {
      return load.computeIfAbsent(
         texture,
         tex -> RenderType.create(
            "autism_chams_occluded", RenderSetup.builder(pipeline).withTexture("Sampler0", tex).useLightmap().useOverlay().createRenderSetup()
         )
      );
   }
}
