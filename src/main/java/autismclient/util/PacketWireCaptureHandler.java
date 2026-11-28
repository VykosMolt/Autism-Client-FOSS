package autismclient.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public final class PacketWireCaptureHandler extends ChannelDuplexHandler {
   private final String direction;

   public PacketWireCaptureHandler(String direction) {
      this.direction = direction;
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (isCaptureActive() && msg instanceof ByteBuf buf) {
         PacketWireCaptureStore.captureRaw(ctx.channel(), this.direction, buf);
      }

      super.channelRead(ctx, msg);
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (isCaptureActive() && msg instanceof ByteBuf buf) {
         PacketWireCaptureStore.captureRaw(ctx.channel(), this.direction, buf);
      }

      super.write(ctx, msg, promise);
   }

   private static boolean isCaptureActive() {
      return PacketCaptureState.isLoggerCaptureActive();
   }
}
