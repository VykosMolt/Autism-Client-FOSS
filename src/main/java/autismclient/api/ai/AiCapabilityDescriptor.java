package autismclient.api.ai;

import java.util.List;
import java.util.Objects;

public record AiCapabilityDescriptor(
   String id,
   String owner,
   int version,
   String family,
   String purpose,
   List<String> aliases,
   String inputSchema,
   String prerequisites,
   String effects,
   List<String> resources,
   String successCriteria,
   String failureRecovery,
   String examples,
   AiCapabilityDescriptor.Classification classification
) {
   public AiCapabilityDescriptor {
      for (String value : List.of(id, owner, family, purpose, inputSchema, prerequisites, effects, successCriteria, failureRecovery, examples)) {
         if (value.isBlank()) {
            throw new IllegalArgumentException("Capability contracts must be complete");
         }
      }

      if (!id.matches("[a-zA-Z0-9_.:/-]+") || !id.contains(":")) {
         throw new IllegalArgumentException("Capability id must be namespaced: " + id);
      }

      if (version < 1) {
         throw new IllegalArgumentException("Contract version must be positive");
      }

      aliases = List.copyOf(aliases);
      resources = List.copyOf(resources);
      Objects.requireNonNull(classification);
   }

   public enum Classification {
      OBSERVE,
      CONFIGURE,
      ACTION,
      DISCOVER_ONLY;
   }
}
