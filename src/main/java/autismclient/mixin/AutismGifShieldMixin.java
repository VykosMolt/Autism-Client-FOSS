package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.L;
import autismclient.util.c.GifTextureManager;
import autismclient.util.c.GifRenderContext;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.equipment.ShieldModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ShieldSpecialRenderer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShieldSpecialRenderer.class)
public abstract class AutismGifShieldMixin {
   @Shadow
   @Final
   private ShieldModel model;

   @Inject(
      method = "submit(Lnet/minecraft/core/component/DataComponentMap;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V",
      at = @At("TAIL"),
      require = 0
   )
   private void autism$gifShield(
      @Nullable DataComponentMap components,
      PoseStack poseStack,
      SubmitNodeCollector submitNodeCollector,
      int lightCoords,
      int overlayCoords,
      boolean hasFoil,
      int outlineColor,
      CallbackInfo ci
   ) {
      if (L.load_Z()) {
         String name = GifRenderContext.init_Z() ? L.init_Str() : GifRenderContext.init_Str();
         if (name != null && !name.isEmpty()) {
            String path = L.load(name);
            if (path != null) {
               BannerPatternLayers patterns = components != null
                  ? components.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY)
                  : BannerPatternLayers.EMPTY;
               DyeColor baseColor = components != null ? components.get(DataComponents.BASE_COLOR) : null;
               if (patterns.layers().isEmpty() && baseColor == null) {
                  Identifier texture = GifTextureManager.load(path, L.load_D(), L.write_Z(), L.write_D());
                  if (texture != null) {
                     Compat263.submitModel(
                        submitNodeCollector,
                        this.model,
                        Unit.INSTANCE,
                        poseStack,
                        RenderTypes.entityTranslucent(texture),
                        lightCoords,
                        overlayCoords,
                        -1,
                        null,
                        outlineColor,
                        null
                     );
                  }
               }
            }
         }
      }
   }
}
