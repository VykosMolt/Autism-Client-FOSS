package autismclient.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public final class PacketWireCaptureHandler extends ChannelDuplexHandler {
   private final String init;

   public PacketWireCaptureHandler(String direction) {
      this.init = direction;
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (init() && msg instanceof ByteBuf buf) {
         PacketWireCaptureStore.init(ctx.channel(), this.init, buf);
      }

      super.channelRead(ctx, msg);
   }

   @Override
   public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
      if (init() && msg instanceof ByteBuf buf) {
         PacketWireCaptureStore.init(ctx.channel(), this.init, buf);
      }

      super.write(ctx, msg, promise);
   }

   private static boolean init() {
      return PacketCaptureState.isLoggerCaptureActive();
   }
}
