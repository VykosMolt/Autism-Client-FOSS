package autismclient.api.ai;

import autismclient.api.AddonRegistrationResult;
import autismclient.b.DataUtil;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AiCapabilities {
   private static final Map<String, AiCapabilities.Entry> ENTRIES = new LinkedHashMap<>();
   private static long revision;

   private AiCapabilities() {
   }

   public static synchronized AddonRegistrationResult registerDetailed(AiCapabilityProvider provider) {
      String owner = DataUtil.init_Str();
      if (owner != null && !owner.isBlank() && provider != null) {
         try {
            List<AiCapabilityDescriptor> contracts = List.copyOf(provider.capabilities());
            Set<String> ids = new HashSet<>();

            for (AiCapabilityDescriptor d : contracts) {
               if (d.owner().equals(owner) && d.id().startsWith(owner + ":")) {
                  if (ids.add(d.id()) && !ENTRIES.containsKey(d.id())) {
                     continue;
                  }

                  throw new IllegalArgumentException("Duplicate capability " + d.id());
               }

               throw new IllegalArgumentException("Contract must use its add-on namespace");
            }

            for (AiCapabilityDescriptor d : contracts) {
               ENTRIES.put(d.id(), new AiCapabilities.Entry(owner, d, provider));
               DataUtil.init_V("AI capability", d.id());
            }

            revision++;
            return AddonRegistrationResult.accepted("AI capability", owner);
         } catch (Throwable e) {
            return AddonRegistrationResult.rejected("AI capability", owner, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
         }
      } else {
         return AddonRegistrationResult.rejected("AI capability", "", "Registration requires an active add-on");
      }
   }

   public static boolean register(AiCapabilityProvider provider) {
      return registerDetailed(provider).accepted();
   }

   public static synchronized void unregisterAddon(String owner) {
      if (ENTRIES.values().removeIf(e -> e.owner().equals(owner))) {
         revision++;
      }
   }

   public static synchronized List<AiCapabilities.Entry> entries() {
      return List.copyOf(ENTRIES.values());
   }

   public static synchronized long revision() {
      return revision;
   }

   public record Entry(String owner, AiCapabilityDescriptor descriptor, AiCapabilityProvider provider) {
   }
}
