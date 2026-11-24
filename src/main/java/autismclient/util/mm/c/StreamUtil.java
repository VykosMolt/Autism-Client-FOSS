package autismclient.util.mm.c;

import java.util.function.Consumer;

public interface StreamUtil {
   String init_Str();

   BufferHelper init_BufferHelper();

   void init(String var1, byte[] var2);

   default void init(String topic, byte[] frame, boolean durable) {
      this.init(topic, frame);
   }

   void init(String var1, Consumer<byte[]> var2);

   void init_V_Str(String var1);

   default void init_V() {
   }

   void load();
}
