package autismclient.mixin;

import autismclient.modules.NoRenderState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class AutismNoRenderHudMixin {
   @Inject(method = "extractPortalOverlay", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noPortalOverlay(GuiGraphicsExtractor ctx, float alpha, CallbackInfo ci) {
      if (NoRenderState.init_Z()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractSpyglassOverlay", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noSpyglassOverlay(GuiGraphicsExtractor ctx, float scale, CallbackInfo ci) {
      if (NoRenderState.load_Z()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractConfusionOverlay", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noNausea(GuiGraphicsExtractor ctx, float amount, CallbackInfo ci) {
      if (NoRenderState.read()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractVignette", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noVignette(GuiGraphicsExtractor ctx, Entity entity, CallbackInfo ci) {
      if (NoRenderState.open()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noCrosshair(GuiGraphicsExtractor ctx, DeltaTracker delta, CallbackInfo ci) {
      if (NoRenderState.put()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractTitle", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noTitle(GuiGraphicsExtractor ctx, DeltaTracker delta, CallbackInfo ci) {
      if (NoRenderState.add()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractSelectedItemName", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noHeldItemName(GuiGraphicsExtractor ctx, CallbackInfo ci) {
      if (NoRenderState.clear()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noPotionIcons(GuiGraphicsExtractor ctx, DeltaTracker delta, CallbackInfo ci) {
      if (NoRenderState.start()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noScoreboardSidebar(GuiGraphicsExtractor ctx, DeltaTracker delta, CallbackInfo ci) {
      if (NoRenderState.set()) {
         ci.cancel();
      }
   }

   @Inject(method = "displayScoreboardSidebar", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noScoreboardDisplay(GuiGraphicsExtractor ctx, Objective objective, CallbackInfo ci) {
      if (NoRenderState.set()) {
         ci.cancel();
      }
   }

   @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true, require = 0)
   private void autism$noTextureOverlay(GuiGraphicsExtractor ctx, Identifier texture, float alpha, CallbackInfo ci) {
      if (texture != null) {
         String path = texture.getPath();
         if (NoRenderState.write() && path.contains("pumpkin")) {
            ci.cancel();
         } else if (NoRenderState.update() && (path.contains("powder") || path.contains("snow"))) {
            ci.cancel();
         }
      }
   }
}
