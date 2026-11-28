package autismclient.mixin;

import autismclient.util.CustomMainMenuGuard;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.client.renderer.Panorama;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Panorama.class)
public abstract class AutismPanoramaOverlayMixin {
   @Unique
   private static final Identifier AUTISM_PANORAMA_OVERLAY = Identifier.fromNamespaceAndPath(
      "autismclient", "textures/gui/title/background/panorama_overlay.png"
   );

   @ModifyArg(
      method = "extractRenderState",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIIII)V"
      ),
      index = 1
   )
   private Identifier autism$swapOverlay(Identifier original) {
      return CustomMainMenuGuard.isSuppressed() ? original : ThemeTextureRecolorer.recoloredBackdrop(AUTISM_PANORAMA_OVERLAY);
   }
}
