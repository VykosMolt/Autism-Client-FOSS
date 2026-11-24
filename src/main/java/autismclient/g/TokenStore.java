package autismclient.g;

import java.util.Arrays;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;

public final class TokenStore {
   private static final ThreadLocal<TokenStore.Inner_DataUtil> init = ThreadLocal.withInitial(TokenStore.Inner_DataUtil::new);

   private TokenStore() {
   }

   public static boolean init(Packet<?> packet) {
      return packet instanceof ClientboundCustomPayloadPacket
         || packet instanceof ClientboundBlockEntityDataPacket
         || packet instanceof ClientboundLevelChunkWithLightPacket;
   }

   public static boolean init_Z() {
      return init.get().init_I != 0;
   }

   public static void init(BufferHelper.Inner_StreamUtil context) {
      TokenStore.Inner_DataUtil state = init.get();
      if (state.init_I == state.init_BufferHelper_StreaArr.length) {
         state.init_BufferHelper_StreaArr = Arrays.copyOf(state.init_BufferHelper_StreaArr, state.init_I * 2);
      }

      state.init_BufferHelper_StreaArr[state.init_I++] = context;
   }

   public static BufferHelper.Inner_StreamUtil init_BufferHelper_Strea() {
      TokenStore.Inner_DataUtil state = init.get();
      return state.init_I == 0 ? null : state.init_BufferHelper_StreaArr[state.init_I - 1];
   }

   public static void init(boolean value) {
      if (value) {
         init(init_BufferHelper_Strea());
      } else {
         TokenStore.Inner_DataUtil state = init.get();
         if (state.init_I > 0) {
            state.init_BufferHelper_StreaArr[--state.init_I] = null;
         }
      }
   }

   private static final class Inner_DataUtil {
      private int init_I;
      private BufferHelper.Inner_StreamUtil[] init_BufferHelper_StreaArr = new BufferHelper.Inner_StreamUtil[4];
   }
}
