package autismclient.mixin.security;

import autismclient.g.CallerModInspector;
import autismclient.g.ModNamespaceCatalog;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.TypeAndCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PayloadTypeRegistryImpl.class)
public class AutismProtectorPayloadTypeRegistryImplMixin {
   @Inject(method = "register", at = @At("RETURN"))
   private void autism$trackPayloadDefaultMod(Type<?> type, StreamCodec<?, ?> codec, CallbackInfoReturnable<TypeAndCodec<?, ?>> cir) {
      for (String mod : CallerModInspector.init()) {
         ModNamespaceCatalog.read_V(mod);
         ModNamespaceCatalog.init(CallerModInspector.init_Set(mod));
      }
   }
}
