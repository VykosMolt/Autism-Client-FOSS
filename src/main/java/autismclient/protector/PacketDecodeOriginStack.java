package autismclient.protector;

import java.util.Arrays;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

public final class PacketDecodeOriginStack {
   private static final ThreadLocal<PacketDecodeOriginStack.OriginStack> STACK = ThreadLocal.withInitial(PacketDecodeOriginStack.OriginStack::new);

   private PacketDecodeOriginStack() {
   }

   public static boolean tracks(Packet<?> packet) {
      return packet instanceof ClientboundCustomPayloadPacket
         || packet instanceof ClientboundBlockEntityDataPacket
         || packet instanceof ClientboundLevelChunkWithLightPacket;
   }

   public static boolean isDecoding() {
      return STACK.get().depth != 0;
   }

   public static void push(ConnectionPrivacyRegistry.ConnectionSession context) {
      PacketDecodeOriginStack.OriginStack state = STACK.get();
      if (state.depth == state.init_BufferHelper_StreaArr.length) {
         state.init_BufferHelper_StreaArr = Arrays.copyOf(state.init_BufferHelper_StreaArr, state.depth * 2);
      }

      state.init_BufferHelper_StreaArr[state.depth++] = context;
   }

   public static ConnectionPrivacyRegistry.ConnectionSession current() {
      PacketDecodeOriginStack.OriginStack state = STACK.get();
      return state.depth == 0 ? null : state.init_BufferHelper_StreaArr[state.depth - 1];
   }

   public static void init(boolean value) {
      if (value) {
         push(current());
      } else {
         PacketDecodeOriginStack.OriginStack state = STACK.get();
         if (state.depth > 0) {
            state.init_BufferHelper_StreaArr[--state.depth] = null;
         }
      }
   }

   private static final class OriginStack {
      private int depth;
      private ConnectionPrivacyRegistry.ConnectionSession[] init_BufferHelper_StreaArr = new ConnectionPrivacyRegistry.ConnectionSession[4];
   }
}
