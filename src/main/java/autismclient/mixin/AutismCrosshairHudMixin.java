package autismclient.mixin;

import autismclient.modules.KillAuraModule;
import autismclient.modules.CrosshairRenderer;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Hud.class)
public abstract class AutismCrosshairHudMixin {
   @WrapOperation(
      method = "extractCrosshair",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
         ordinal = 0
      ),
      require = 0
   )
   private void autism$customCrosshair(
      GuiGraphicsExtractor graphics, RenderPipeline pipeline, Identifier sprite, int x, int y, int width, int height, Operation<Void> original
   ) {
      if ("hud/crosshair".equals(sprite.getPath())) {
         KillAuraModule.init_V_GuiGraphicsExtract(graphics);
         if (CrosshairRenderer.init(graphics)) {
            return;
         }
      }

      original.call(graphics, pipeline, sprite, x, y, width, height);
   }
}
