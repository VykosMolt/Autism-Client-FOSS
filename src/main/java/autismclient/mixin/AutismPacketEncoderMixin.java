package autismclient.mixin;

import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.PacketCaptureState;
import autismclient.util.PacketWireCaptureStore;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.PacketEncoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PacketEncoder.class)
public abstract class AutismPacketEncoderMixin<T extends PacketListener> {
   @Shadow
   @Final
   private ProtocolInfo<T> protocolInfo;

   @WrapMethod(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V")
   private void autism$captureEncodedPlaintext(ChannelHandlerContext ctx, Packet<T> packet, ByteBuf output, Operation<Void> original) {
      long captureState = PacketCaptureState.getState();
      if (PacketCaptureState.captureFlags(captureState) == 0) {
         original.call(ctx, packet, output);
      } else {
         original.call(ctx, packet, output);
         ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
         if (module != null) {
            String protocol = this.protocolInfo.id().id();
            if (PacketCaptureState.isLoggerCapture(captureState)) {
               PacketWireCaptureStore.capture(packet, "C2S", protocol, packet.type(), output);
            }

            if (PacketCaptureState.load(captureState) && autism$isPayloadCarrier(packet)) {
               module.captureCustomPayloadFromSource(packet, "C2S", protocol, "encoder");
            }
         }
      }
   }

   @Unique
   private static boolean autism$isPayloadCarrier(Packet<?> packet) {
      return packet instanceof ServerboundCustomPayloadPacket || packet instanceof BundlePacket;
   }
}
