package autismclient.mixin;

import autismclient.gui.ui.UiAlphaStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class AutismGuiAlphaMixin {
   @ModifyVariable(method = "innerFill", at = @At("HEAD"), argsOnly = true, ordinal = 4)
   private int autism$fadeFill(int color) {
      return UiAlphaStack.apply(color);
   }

   @ModifyVariable(method = "innerFill", at = @At("HEAD"), argsOnly = true)
   private Integer autism$fadeFillGradient(Integer color) {
      return color == null ? null : UiAlphaStack.apply(color);
   }

   @ModifyVariable(
      method = "innerBlit(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;Lcom/mojang/renderpearl/api/textures/GpuTextureView;Lcom/mojang/renderpearl/api/textures/GpuSampler;IIIIFFFFI)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 4
   )
   private int autism$fadeBlit(int color) {
      return UiAlphaStack.apply(color);
   }

   @ModifyVariable(method = "innerTiledBlit", at = @At("HEAD"), argsOnly = true, ordinal = 6)
   private int autism$fadeTiledBlit(int color) {
      return UiAlphaStack.apply(color);
   }

   @ModifyVariable(
      method = "text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;IIIZ)V",
      at = @At("HEAD"),
      argsOnly = true,
      ordinal = 2
   )
   private int autism$fadeText(int color) {
      return UiAlphaStack.apply(color);
   }

   @Inject(
      method = "item(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;III)V",
      at = @At("HEAD"),
      cancellable = true
   )
   private void autism$skipFadedItem(LivingEntity owner, Level level, ItemStack stack, int x, int y, int seed, CallbackInfo ci) {
      if (UiAlphaStack.current() < 0.5F) {
         ci.cancel();
      }
   }

   @Inject(method = {"entity", "skin"}, at = @At("HEAD"), cancellable = true)
   private void autism$skipFadedPreview(CallbackInfo ci) {
      if (UiAlphaStack.current() < 0.5F) {
         ci.cancel();
      }
   }
}
