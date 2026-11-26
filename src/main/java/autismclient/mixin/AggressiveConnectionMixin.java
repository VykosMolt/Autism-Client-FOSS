package autismclient.mixin;

import autismclient.aggressivepvp.Versions;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records what goes out and comes in on the player's own connection for Aggressive PvP. Sends nothing, cancels nothing. */
@Mixin(Connection.class)
public abstract class AggressiveConnectionMixin {
    // TAIL on 3-arg send: catches direct 3-arg sends too, and packets AUTISM cancels at 2-arg or 3-arg HEAD never reach here
    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lio/netty/channel/ChannelFutureListener;Z)V", at = @At("TAIL"))
    private void ap$sent(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
        Versions.onSend(packet);
    }

    @Inject(
        method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Connection;genericsFtw(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;)V")
    )
    private void ap$received(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientboundBundlePacket bundle) {
            for (Packet<?> inner : bundle.subPackets()) Versions.onReceive(inner);
        } else {
            Versions.onReceive(packet);
        }
    }
}
