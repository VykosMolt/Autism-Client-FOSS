package autismclient.api.hud;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.b.DataUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class HudElements {
   private static final Map<String, HudElementProvider> PROVIDERS = new LinkedHashMap<>();
   private static final Map<String, String> OWNERS = new LinkedHashMap<>();
   private static int revision;

   private HudElements() {
   }

   public static synchronized int revision() {
      return revision;
   }

   public static boolean register(HudElementProvider provider) {
      return registerDetailed(provider).accepted();
   }

   // Not synchronized: provider methods are addon code and must not run under the registry lock.
   public static AddonRegistrationResult registerDetailed(HudElementProvider provider) {
      if (provider == null) {
         return AddonRegistrationResult.rejected("hud", "", "provider was null");
      }

      String ownerAddonId = DataUtil.init_Str();
      if (ownerAddonId != null && !ownerAddonId.isBlank()) {
         String id = provider.id();
         if (id == null || !id.contains(":")) {
            return reject(ownerAddonId, id, "non-namespaced id - scope it with AutismAddons.id(...) so it becomes addonId:localId");
         }

         if (!id.startsWith(ownerAddonId + ":")) {
            return reject(ownerAddonId, id, "foreign namespace - the id must start with your addon id; scope it with AutismAddons.id(...)");
         }

         AddonRegistrationResult sizeCheck = validateSize(ownerAddonId, id, provider);
         if (!sizeCheck.accepted()) {
            return sizeCheck;
         }

         synchronized (HudElements.class) {
            if (PROVIDERS.containsKey(id)) {
               return reject(ownerAddonId, id, "duplicate id");
            }

            PROVIDERS.put(id, provider);
            OWNERS.put(id, ownerAddonId);
            revision++;
         }

         DataUtil.init_V("hud", id);
         return AddonRegistrationResult.accepted("hud", id);
      } else {
         return reject(ownerAddonId, "", "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }
   }

   private static AddonRegistrationResult validateSize(String ownerAddonId, String id, HudElementProvider provider) {
      int width;
      int height;
      try {
         width = provider.width();
         height = provider.height();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[Hud] Rejecting HUD element '{}': sizing threw", id, t);
         DataUtil.init(ownerAddonId, "hud", id, "sizing threw " + t.getClass().getSimpleName());
         return AddonRegistrationResult.rejected("hud", id, "sizing threw " + t.getClass().getSimpleName());
      }

      if (width <= 0) {
         return reject(ownerAddonId, id, "width must be positive");
      } else {
         return height <= 0 ? reject(ownerAddonId, id, "height must be positive") : AddonRegistrationResult.accepted("hud", id);
      }
   }

   private static AddonRegistrationResult reject(String ownerAddonId, String id, String reason) {
      AutismClientAddon.LOG.warn("[Hud] Rejecting HUD element '{}': {}", id, reason);
      DataUtil.init(ownerAddonId, "hud", id, reason);
      return AddonRegistrationResult.rejected("hud", id, reason);
   }

   public static synchronized void unregisterAddon(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         List<String> remove = new ArrayList<>();

         for (Entry<String, String> owner : OWNERS.entrySet()) {
            if (addonId.equals(owner.getValue())) {
               remove.add(owner.getKey());
            }
         }

         if (!remove.isEmpty()) {
            for (String id : remove) {
               PROVIDERS.remove(id);
               OWNERS.remove(id);
            }

            revision++;
         }
      }
   }

   public static synchronized HudElementProvider get(String id) {
      return id == null ? null : PROVIDERS.get(id);
   }

   public static synchronized boolean isAddon(String id) {
      return id != null && PROVIDERS.containsKey(id);
   }

   public static synchronized List<String> ids() {
      return Collections.unmodifiableList(new ArrayList<>(PROVIDERS.keySet()));
   }

   public static synchronized boolean isEmpty() {
      return PROVIDERS.isEmpty();
   }
}
