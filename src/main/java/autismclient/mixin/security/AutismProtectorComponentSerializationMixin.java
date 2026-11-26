package autismclient.mixin.security;

import autismclient.protector.PacketOriginComponentCodec;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.serialization.Codec;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ComponentSerialization.class)
public class AutismProtectorComponentSerializationMixin {
   @WrapOperation(
      method = "<clinit>",
      at = @At(
         value = "INVOKE",
         target = "Lcom/mojang/serialization/Codec;recursive(Ljava/lang/String;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"
      )
   )
   private static Codec<Component> autism$wrapRecursive(String name, Function<Codec<Component>, Codec<Component>> body, Operation<Codec<Component>> original) {
      return new PacketOriginComponentCodec(original.call(name, body));
   }
}
