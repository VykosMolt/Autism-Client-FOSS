package autismclient.mixin;

import autismclient.modules.PoolEntry;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bg;
import autismclient.util.bn;
import autismclient.util.g.ConfigStore;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PacketDecoder.class)
public abstract class AutismPacketDecoderMixin<T extends PacketListener> {
   @Shadow
   @Final
   private ProtocolInfo<T> protocolInfo;

   @WrapMethod(method = "decode")
   private void autism$captureIncomingPlaintext(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, Operation<Void> original) {
      long captureState = bg.init_J();
      if (bg.init_I_J(captureState) == 0) {
         original.call(ctx, input, out);
      } else {
         int outSizeBeforeDecode = out == null ? 0 : out.size();
         boolean multi = !AutismLiteVariant.enabled() && ctx != null && ConfigStore.init_Z(ctx.channel());
         boolean suppressPayloadCodec = multi && bg.load(captureState);
         if (suppressPayloadCodec) {
            bg.init_V();
         }

         byte[] incomingPlaintext = !multi && bg.init_Z_J(captureState) ? bn.init(input) : bg.init_BArr;

         try {
            original.call(ctx, input, out);
         } finally {
            if (suppressPayloadCodec) {
               bg.load_V();
            }
         }

         if (!multi && out != null && !out.isEmpty()) {
            int start = Math.max(0, Math.min(outSizeBeforeDecode, out.size()));
            boolean capturePlaintext = bg.init_Z_J(captureState) && incomingPlaintext.length > 0;
            boolean capturePayloads = bg.load(captureState);
            PoolEntry module = PoolEntry.init_PoolEntry();
            if (module != null) {
               String protocol = this.protocolInfo.id().id();

               for (int i = start; i < out.size(); i++) {
                  if (out.get(i) instanceof Packet<?> packet) {
                     if (capturePlaintext) {
                        bn.init(packet, "S2C", protocol, packet.type(), incomingPlaintext);
                     }

                     if (capturePayloads && autism$isPayloadCarrier(packet)) {
                        module.init(packet, "S2C", protocol, "decoder");
                     }
                  }
               }
            }
         }
      }
   }

   @Unique
   private static boolean autism$isPayloadCarrier(Packet<?> packet) {
      return packet instanceof ClientboundCustomPayloadPacket || packet instanceof BundlePacket;
   }
}
