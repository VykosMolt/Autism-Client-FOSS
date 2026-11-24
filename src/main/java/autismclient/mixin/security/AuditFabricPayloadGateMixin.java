package autismclient.mixin.security;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import org.spongepowered.asm.mixin.Mixin;

// On a connection that presents itself as vanilla, mod payloads arrive undecoded (AutismFabricPayloadCodecMixin).
// A mod's receiver handed one throws, and that disconnects with "Network Protocol Error", so an undecoded payload
// goes to no receiver and vanilla ignores it. Stock had a gate for this on Fabric's own mixin class, where a mixin
// is never applied.
@Mixin(AbstractChanneledNetworkAddon.class)
public abstract class AuditFabricPayloadGateMixin {
   @WrapMethod(method = "handle(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)Z")
   private boolean autism$gatePayload(CustomPacketPayload payload, Operation<Boolean> original) {
      return !(payload instanceof DiscardedPayload) && original.call(payload);
   }
}
