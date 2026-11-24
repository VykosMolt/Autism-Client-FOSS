package autismclient.mixin;

import autismclient.modules.am;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Lightmap.class)
public abstract class AutismLightmapMixin {
   @Unique
   private static final Vector4fc AUTISM_WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);
   @Shadow
   @Final
   private GpuTexture texture;
   @Unique
   private boolean autism$wasBright;

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   private void autism$fullbrightLightmap(LightmapRenderState renderState, CallbackInfo ci) {
      if (am.poll()) {
         if (!this.autism$wasBright || renderState.needsUpdate) {
            ProfilerFiller profiler = Profiler.get();
            profiler.push("autism_lightmap");
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.texture, AUTISM_WHITE);
            profiler.pop();
            this.autism$wasBright = true;
         }

         ci.cancel();
      } else if (this.autism$wasBright) {
         this.autism$wasBright = false;
         renderState.needsUpdate = true;
      }
   }
}
