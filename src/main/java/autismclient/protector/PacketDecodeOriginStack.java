package autismclient.protector;

import java.util.Arrays;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

public final class PacketDecodeOriginStack {
   private static final ThreadLocal<PacketDecodeOriginStack.OriginStack> init = ThreadLocal.withInitial(PacketDecodeOriginStack.OriginStack::new);

   private PacketDecodeOriginStack() {
   }

   public static boolean tracks(Packet<?> packet) {
      return packet instanceof ClientboundCustomPayloadPacket
         || packet instanceof ClientboundBlockEntityDataPacket
         || packet instanceof ClientboundLevelChunkWithLightPacket;
   }

   public static boolean isDecoding() {
      return init.get().init_I != 0;
   }

   public static void push(ConnectionPrivacyRegistry.ConnectionSession context) {
      PacketDecodeOriginStack.OriginStack state = init.get();
      if (state.init_I == state.init_BufferHelper_StreaArr.length) {
         state.init_BufferHelper_StreaArr = Arrays.copyOf(state.init_BufferHelper_StreaArr, state.init_I * 2);
      }

      state.init_BufferHelper_StreaArr[state.init_I++] = context;
   }

   public static ConnectionPrivacyRegistry.ConnectionSession current() {
      PacketDecodeOriginStack.OriginStack state = init.get();
      return state.init_I == 0 ? null : state.init_BufferHelper_StreaArr[state.init_I - 1];
   }

   public static void init(boolean value) {
      if (value) {
         push(current());
      } else {
         PacketDecodeOriginStack.OriginStack state = init.get();
         if (state.init_I > 0) {
            state.init_BufferHelper_StreaArr[--state.init_I] = null;
         }
      }
   }

   private static final class OriginStack {
      private int init_I;
      private ConnectionPrivacyRegistry.ConnectionSession[] init_BufferHelper_StreaArr = new ConnectionPrivacyRegistry.ConnectionSession[4];
   }
}
