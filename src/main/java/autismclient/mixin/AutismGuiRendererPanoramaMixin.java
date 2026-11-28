package autismclient.mixin;

import autismclient.util.CustomMainMenuGuard;
import autismclient.util.ThemeTextureRecolorer;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiRenderer.class)
public abstract class AutismGuiRendererPanoramaMixin {
   @Unique
   private final CubeMap autism$customCubeMap = new CubeMap(Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama"));

   @Inject(method = "registerPanoramaTextures", at = @At("TAIL"))
   private void autism$registerCustomPanorama(TextureManager textureManager, CallbackInfo ci) {
      ThemeTextureRecolorer.register(textureManager);
   }

   @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/CubeMap;render(FF)V"))
   private void autism$renderPanorama(CubeMap vanillaCubeMap, float rotX, float rotY) {
      boolean custom = !CustomMainMenuGuard.isSuppressed() && ThemeTextureRecolorer.isPanoramaLoaded();
      CubeMap target = custom ? this.autism$customCubeMap : vanillaCubeMap;
      target.render(rotX, rotY);
   }

   @Inject(method = "close", at = @At("TAIL"))
   private void autism$closeCustomPanorama(CallbackInfo ci) {
      this.autism$customCubeMap.close();
   }
}
