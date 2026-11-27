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

      return safeContext.init(safeContext.init_Str(), safeContext.init_BArr());
   }

   public static final class Inner_DataUtil {
      private final String init_Str;
      private final byte[] init_BArr;
      private final Integer init_Integer;

      public Inner_DataUtil(String channel, byte[] rawBytes, Integer commandApiValue) {
         this.init_Str = channel == null ? "minecraft:empty" : channel;
         this.init_BArr = rawBytes == null ? new byte[0] : (byte[])rawBytes.clone();
         this.init_Integer = commandApiValue;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public Identifier init_Identifier() {
         return AutismPayloadSupport.parseChannelId(this.init_Str);
      }

      public byte[] init_BArr() {
         return (byte[])this.init_BArr.clone();
      }

      public Integer init_Integer() {
         return this.init_Integer;
      }

      public FriendlyByteBuf init_FriendlyByteBuf() {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         if (this.init_BArr.length > 0) {
            buf.writeBytes(this.init_BArr);
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
         return AutismPayloadSupport.withLeadingBigEndianInt(this.init_BArr, value);
      }

      public PayloadScriptRunner.Inner_StreamUtil init(String nextChannel, byte[] bytes) {
         return new PayloadScriptRunner.Inner_StreamUtil(nextChannel != null && !nextChannel.isBlank() ? nextChannel : this.init_Str, bytes);
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
