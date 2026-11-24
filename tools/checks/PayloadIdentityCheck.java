package autismclient.util;

import java.lang.reflect.Field;
import java.util.Map;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.resources.Identifier;

public class PayloadIdentityCheck {
   @SuppressWarnings("unchecked")
   public static void main(String[] args) throws Exception {
      Identifier id = Identifier.fromNamespaceAndPath("check", "channel");
      DiscardedPayload first = new DiscardedPayload(id);
      DiscardedPayload equalTwin = new DiscardedPayload(id);
      if (!first.equals(equalTwin)) {
         throw new IllegalStateException("premise: record payloads should be value-equal");
      }
      Field field = AutismPayloadSupport.class.getDeclaredField("init_Map");
      field.setAccessible(true);
      ((Map<CustomPacketPayload, byte[]>) field.get(null)).put(first, new byte[]{1, 2, 3});
      if (AutismPayloadSupport.init_BArr(first) == null) {
         System.out.println("FAIL: stored payload lost its bytes");
         System.exit(1);
      }
      if (AutismPayloadSupport.init_BArr(equalTwin) != null) {
         System.out.println("FAIL: an equal but different payload got the other payload's bytes");
         System.exit(1);
      }
      System.out.println("PASS PayloadIdentityCheck");
   }
}
