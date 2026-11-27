package autismclient.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

public final class PayloadScriptRunner {
   private PayloadScriptRunner() {
   }

   public static PayloadScriptRunner.Inner_StreamUtil init(String javaSource, PayloadScriptRunner.Inner_DataUtil context) {
      PayloadScriptRunner.Inner_DataUtil safeContext = context == null ? new PayloadScriptRunner.Inner_DataUtil("minecraft:empty", new byte[0], null) : context;
      String trimmed = javaSource == null ? "" : javaSource.trim();
      if (!trimmed.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§eNote: Java scripting is disabled. Sending raw payload bytes.");
      }

      return safeContext.init(safeContext.getChannel(), safeContext.getRawBytes());
   }

   public static final class Inner_DataUtil {
      private final String channel;
      private final byte[] rawBytes;
      private final Integer commandApiValue;

      public Inner_DataUtil(String channel, byte[] rawBytes, Integer commandApiValue) {
         this.channel = channel == null ? "minecraft:empty" : channel;
         this.rawBytes = rawBytes == null ? new byte[0] : (byte[])rawBytes.clone();
         this.commandApiValue = commandApiValue;
      }

      public String getChannel() {
         return this.channel;
      }

      public Identifier channelId() {
         return AutismPayloadSupport.parseChannelId(this.channel);
      }

      public byte[] getRawBytes() {
         return (byte[])this.rawBytes.clone();
      }

      public Integer getCommandApiValue() {
         return this.commandApiValue;
      }

      public FriendlyByteBuf newBuffer() {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         if (this.rawBytes.length > 0) {
            buf.writeBytes(this.rawBytes);
         }

         return buf;
      }

      public FriendlyByteBuf load() {
         return new FriendlyByteBuf(Unpooled.buffer());
      }

      public byte[] init(FriendlyByteBuf buf) {
         return AutismPayloadSupport.copyBufferBytes(buf);
      }

      public byte[] init(String text) {
         return AutismPayloadSupport.parseHexOrBase64(text);
      }

      public String init(byte[] bytes) {
         return AutismPayloadSupport.toHexString(bytes);
      }

      public byte[] init(int value) {
         return AutismPayloadSupport.withLeadingBigEndianInt(this.rawBytes, value);
      }

      public PayloadScriptRunner.Inner_StreamUtil init(String nextChannel, byte[] bytes) {
         return new PayloadScriptRunner.Inner_StreamUtil(nextChannel != null && !nextChannel.isBlank() ? nextChannel : this.channel, bytes);
      }
   }

   public record Inner_StreamUtil(String channel, byte[] bytes) {
      public Inner_StreamUtil {
         bytes = bytes == null ? new byte[0] : (byte[])bytes.clone();
      }

      public byte[] bytes() {
         return (byte[])this.bytes.clone();
      }
   }
}
