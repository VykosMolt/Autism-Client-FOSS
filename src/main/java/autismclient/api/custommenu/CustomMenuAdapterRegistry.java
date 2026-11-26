package autismclient.api.custommenu;

import autismclient.AutismClientAddon;
import autismclient.b.DataUtil;
import autismclient.util.b.VanillaDialogMenuAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuAdapterRegistry {
   private static final Map<String, CustomMenuAdapterRegistry.Registered> ADAPTERS = new LinkedHashMap<>();
   private static volatile CustomMenuAdapterRegistry.RegistrySnapshot SNAPSHOT;

   private CustomMenuAdapterRegistry() {
   }

   public static synchronized boolean register(CustomMenuAdapter adapter) {
      String var2 = DataUtil.init_Str();
      if (var2 == null) {
         return false;
      }

      if (adapter != null && adapter.id() != null && !adapter.id().isBlank()) {
         String id = adapter.id().trim().toLowerCase(Locale.ROOT);
         if (!id.startsWith(var2.toLowerCase(Locale.ROOT) + ":")) {
            AutismClientAddon.LOG.warn("[CustomMenus] Rejecting adapter '{}': the id must start with your addon id; scope it with AutismAddons.id(...)", id);
            return false;
         }

         if (ADAPTERS.containsKey(id)) {
            return false;
         }

         ADAPTERS.put(id, new CustomMenuAdapterRegistry.Registered(adapter, var2));
         publishSnapshot();
         return true;
      } else {
         return false;
      }
   }

   public static synchronized void unregisterAddon(String addonId) {
      if (addonId != null && !addonId.isBlank() && ADAPTERS.entrySet().removeIf(entry -> addonId.equals(entry.getValue().owner()))) {
         publishSnapshot();
      }
   }

   public static boolean acceptsInbound(Packet<?> packet) {
      if (packet == null) {
         return false;
      }

      for (CustomMenuAdapterRegistry.Registered registered : SNAPSHOT.ordered()) {
         try {
            if (registered.adapter().acceptsInbound(packet)) {
               return true;
            }
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("[CustomMenus] Adapter '{}' failed while checking a packet", registered.adapter().id(), error);
         }
      }

      return false;
   }

   public static CustomMenuEvent inspect(Packet<?> packet, String phase) {
      if (packet == null) {
         return CustomMenuEvent.NONE;
      }

      for (CustomMenuAdapterRegistry.Registered registered : SNAPSHOT.ordered()) {
         try {
            if (registered.adapter().acceptsInbound(packet)) {
               CustomMenuEvent event = registered.adapter().inspectInbound(packet, phase);
               if (event != null && event.type() != CustomMenuEvent.Type.NONE) {
                  return event;
               }
            }
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("[CustomMenus] Adapter '{}' failed while reading a packet", registered.adapter().id(), error);
         }
      }

      return CustomMenuEvent.NONE;
   }

   public static CustomMenuSubmitResult submit(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
      if (snapshot == null) {
         return CustomMenuSubmitResult.failure("No custom menu is open");
      }

      if (snapshot.adapterId() == null) {
         return CustomMenuSubmitResult.failure("Custom menu adapter is unavailable");
      }

      CustomMenuAdapterRegistry.Registered registered = SNAPSHOT.byId().get(snapshot.adapterId().toLowerCase(Locale.ROOT));
      if (registered == null) {
         return CustomMenuSubmitResult.failure("Custom menu adapter is unavailable");
      }

      try {
         CustomMenuSubmitResult result = registered.adapter().submit(snapshot, submission);
         return result == null ? CustomMenuSubmitResult.failure("Custom menu adapter returned no result") : result;
      } catch (Throwable error) {
         AutismClientAddon.LOG
            .warn("[CustomMenus] Adapter '{}' failed while submitting a form ({})", registered.adapter().id(), error.getClass().getSimpleName());
         return CustomMenuSubmitResult.failure("Custom menu adapter failed");
      }
   }

   public static List<String> ids() {
      return SNAPSHOT.ids();
   }

   private static void publishSnapshot() {
      CustomMenuAdapterRegistry.Registered[] ordered = ADAPTERS.values().toArray(CustomMenuAdapterRegistry.Registered[]::new);
      SNAPSHOT = new CustomMenuAdapterRegistry.RegistrySnapshot(ordered, Map.copyOf(ADAPTERS), List.copyOf(ADAPTERS.keySet()));
   }

   static {
      ADAPTERS.put("minecraft:dialog", new CustomMenuAdapterRegistry.Registered(new VanillaDialogMenuAdapter(), "autismclient"));
      publishSnapshot();
   }

   private record Registered(CustomMenuAdapter adapter, String owner) {
   }

   private record RegistrySnapshot(CustomMenuAdapterRegistry.Registered[] ordered, Map<String, CustomMenuAdapterRegistry.Registered> byId, List<String> ids) {
   }
}
