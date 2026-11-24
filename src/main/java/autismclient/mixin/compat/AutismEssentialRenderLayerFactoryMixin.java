package autismclient.mixin.compat;

import autismclient.mixin.AutismRenderTypeStateAccessor;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "gg.essential.model.backend.minecraft.RenderLayerFactory$Companion", remap = false)
public abstract class AutismEssentialRenderLayerFactoryMixin {
   @WrapMethod(method = "createRenderLayer")
   private RenderType autism$createRenderLayerDirectly(String name, RenderSetup setup, Operation<RenderType> original) {
      return AutismRenderTypeStateAccessor.autism$create(name, setup);
   }
}
