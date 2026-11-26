package autismclient.mixin.security;

import autismclient.g.BufferHelper;
import autismclient.g.TokenStore;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PayloadTypeRegistryImpl.class)
public class AutismFabricPayloadCodecMixin {
   @Shadow
   @Final
   private PacketFlow flow;

   @Inject(
      method = "get(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload$TypeAndCodec;",
      at = @At("HEAD"),
      cancellable = true
   )
   private void autism$gatePayloadCodec(Identifier id, CallbackInfoReturnable<TypeAndCodec<?, ?>> cir) {
      if (this.flow == PacketFlow.CLIENTBOUND && TokenStore.init_Z()) {
         BufferHelper.Inner_StreamUtil context = TokenStore.init_BufferHelper_Strea();
         if ((context == null || context.init_Connection().getReceiving() == PacketFlow.CLIENTBOUND)
            && (context == null || context.init_BufferHelper_Cache() != BufferHelper.Inner_CachePool.MODDED || !BufferHelper.init_Z(context))) {
            cir.setReturnValue(null);
         }
      }
   }
}
