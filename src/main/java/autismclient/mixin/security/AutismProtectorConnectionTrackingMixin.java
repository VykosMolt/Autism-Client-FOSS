package autismclient.mixin.security;

import autismclient.g.ConnectionPrivacyRegistry;
import autismclient.g.ProtectorFeatureFlags;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.net.SocketAddress;
import net.minecraft.network.ClientboundPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.handshake.ClientIntent;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class AutismProtectorConnectionTrackingMixin {
   @Unique
   private volatile ConnectionPrivacyRegistry.Inner_StreamUtil autism$privacyContext;

   @Inject(method = "channelActive", at = @At("HEAD"))
   private void autism$onChannelActive(ChannelHandlerContext context, CallbackInfo ci) {
      SocketAddress peerAddress = context != null && context.channel() != null ? context.channel().remoteAddress() : null;
      this.autism$context(peerAddress, false);
   }

   @Inject(method = "initiateServerboundConnection", at = @At("HEAD"))
   private void autism$trackServerboundConnection(
      String host, int port, ProtocolInfo<?> serverbound, ProtocolInfo<?> clientbound, ClientboundPacketListener listener, ClientIntent intent, CallbackInfo ci
   ) {
      this.autism$bindHandshake(host, port, listener);
   }

   @Inject(method = "initiateServerboundStatusConnection", at = @At("HEAD"))
   private void autism$trackStatusConnection(String host, int port, ClientStatusPacketListener listener, CallbackInfo ci) {
      this.autism$bindHandshake(host, port, listener);
   }

   @Inject(method = "setupInboundProtocol", at = @At("TAIL"))
   private void autism$trackInboundListener(ProtocolInfo<?> protocol, PacketListener listener, CallbackInfo ci) {
      Connection connection = (Connection)(Object)this;
      this.autism$context(connection.getRemoteAddress(), false);
      this.autism$privacyContext = ConnectionPrivacyRegistry.init(connection, listener);
   }

   @Inject(method = "doSendPacket", at = @At("HEAD"))
   private void autism$trackAccount(Packet<?> packet, ChannelFutureListener listener, boolean flush, CallbackInfo ci) {
      if (packet instanceof ServerboundHelloPacket hello) {
         ConnectionPrivacyRegistry.init((Connection)(Object)this, hello.profileId(), hello.name());
      }
   }

   @Inject(method = "channelInactive", at = @At("HEAD"))
   private void autism$onChannelInactive(ChannelHandlerContext context, CallbackInfo ci) {
      this.autism$disconnectExactContext();
   }

   @Inject(method = "handleDisconnection", at = @At("HEAD"))
   private void autism$onDisconnection(CallbackInfo ci) {
      Connection connection = (Connection)(Object)this;
      ConnectionPrivacyRegistry.Inner_StreamUtil context = this.autism$privacyContext;
      if (context == null) {
         context = ConnectionPrivacyRegistry.init_BufferHelper_Strea(connection);
      }

      autism$disconnectClosedConnection(connection, context);
   }

   @Unique
   private static void autism$disconnectClosedConnection(Connection connection, ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      if (!connection.isConnecting() && !connection.isConnected()) {
         autism$disconnectExactContext(context);
      }
   }

   private void autism$disconnectExactContext() {
      ConnectionPrivacyRegistry.Inner_StreamUtil context = this.autism$privacyContext;
      if (context == null) {
         context = ConnectionPrivacyRegistry.init_BufferHelper_Strea((Connection)(Object)this);
      }

      autism$disconnectExactContext(context);
   }

   @Unique
   private static void autism$disconnectExactContext(ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      autismclient.g.OutboundChannelPolicy.init_V(context);
   }

   private ConnectionPrivacyRegistry.Inner_StreamUtil autism$context(SocketAddress peerAddress, boolean multiChannel) {
      Connection connection = (Connection)(Object)this;
      ConnectionPrivacyRegistry.Inner_CachePool mode = ProtectorFeatureFlags.sync() ? ConnectionPrivacyRegistry.Inner_CachePool.VANILLA : ConnectionPrivacyRegistry.Inner_CachePool.MODDED;
      this.autism$privacyContext = ConnectionPrivacyRegistry.init(connection, mode, false, peerAddress);
      return this.autism$privacyContext;
   }

   private void autism$bindHandshake(String host, int port, PacketListener listener) {
      Connection connection = (Connection)(Object)this;
      ConnectionPrivacyRegistry.Inner_StreamUtil context = this.autism$context(connection.getRemoteAddress(), false);
      if (context.init_BufferHelper_Buffe() == null) {
         ConnectionPrivacyRegistry.init(connection, host, port);
      }

      this.autism$privacyContext = ConnectionPrivacyRegistry.init(connection, listener);
   }
}
