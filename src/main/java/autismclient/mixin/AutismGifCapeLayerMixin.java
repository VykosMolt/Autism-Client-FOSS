package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.modules.L;
import autismclient.util.c.CachePool;
import autismclient.util.c.StreamUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CapeLayer.class)
public abstract class AutismGifCapeLayerMixin {
   @Shadow
   @Final
   private HumanoidModel model;

   @Shadow
   protected abstract boolean hasLayer(ItemStack var1, LayerType var2);

   @Inject(
      method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/AvatarRenderState;FF)V",
      at = @At("HEAD"),
      cancellable = true,
      require = 0
   )
   private void autism$gifCape(
      PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, AvatarRenderState state, float yRot, float xRot, CallbackInfo ci
   ) {
      if (L.init_Z()) {
         String name = autism$owner(state);
         if (name != null && !name.isEmpty()) {
            String path = L.init(name);
            if (path != null) {
               Identifier texture = CachePool.init_Identifier_Str_2(path, L.init_D(), L.read_Z(), L.read_D());
               if (texture != null) {
                  ci.cancel();
                  if (!state.isInvisible && !this.hasLayer(state.chestEquipment, LayerType.WINGS)) {
                     poseStack.pushPose();
                     if (this.hasLayer(state.chestEquipment, LayerType.HUMANOID)) {
                        poseStack.translate(0.0F, -0.053125F, 0.06875F);
                     }

                     Compat263.submitModel(
                        collector,
                        this.model,
                        state,
                        poseStack,
                        RenderTypes.entityTranslucent(texture),
                        lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        state.outlineColor,
                        null
                     );
                     poseStack.popPose();
                  }
               }
            }
         }
      }
   }

   @Unique
   private static String autism$owner(AvatarRenderState state) {
      String name = StreamUtil.init_Str();
      if (name != null && !name.isEmpty()) {
         return name;
      }

      Minecraft mc = Minecraft.getInstance();
      return mc != null && mc.level != null && mc.level.getEntity(state.id) instanceof Player player && player.getGameProfile() != null
         ? player.getGameProfile().name()
         : null;
   }
}
