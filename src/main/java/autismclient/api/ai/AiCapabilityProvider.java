package autismclient.api.ai;

import java.util.List;

public interface AiCapabilityProvider {
   List<AiCapabilityDescriptor> capabilities();

   default String unavailableReason(String id) {
      return "";
   }

   AiOperation start(String var1, String var2, AiObservation var3);
}
