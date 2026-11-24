package autismclient.api.macro;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.b.DataUtil;
import autismclient.util.macro.MacroAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class MacroPresetRegistry {
   private static final List<MacroPresetRegistry.PresetEntry> ENTRIES = new ArrayList<>();
   private static final Map<MacroPresetRegistry.PresetEntry, String> ENTRY_OWNERS = new IdentityHashMap<>();
   private static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();
   private static final Map<String, String> CATEGORY_OWNERS = new LinkedHashMap<>();

   private MacroPresetRegistry() {
   }

   public static synchronized void registerCategory(String id, String label) {
      registerCategory(id, label, DataUtil.init_Str());
   }

   public static synchronized String ensureScopedCategory(String rawId) {
      String scopedId = MacroActionRegistry.scopeCategoryId(rawId);
      if (!CATEGORY_LABELS.containsKey(scopedId)) {
         registerCategory(scopedId, DataUtil.load_Str(null), DataUtil.init_Str());
      }

      return scopedId;
   }

   public static synchronized void registerScopedCategory(String rawId, String label) {
      String visible = DataUtil.init_Z() ? DataUtil.load_Str(null) : label;
      registerCategory(MacroActionRegistry.scopeCategoryId(rawId), visible, DataUtil.init_Str());
   }

   private static synchronized void registerCategory(String id, String label, String ownerAddonId) {
      if (id != null && !id.isBlank()) {
         CATEGORY_LABELS.put(id, label != null && !label.isBlank() ? label : id);
         if (ownerAddonId != null && !ownerAddonId.isBlank()) {
            CATEGORY_OWNERS.put(id, ownerAddonId);
         }
      }
   }

   public static void register(String categoryId, String label, String tip, Supplier<List<MacroAction>> builder) {
      registerDetailed(categoryId, label, tip, builder);
   }

   // Not synchronized: the builder is addon code and must not run under the registry lock.
   public static AddonRegistrationResult registerDetailed(String categoryId, String label, String tip, Supplier<List<MacroAction>> builder) {
      String ownerAddonId = DataUtil.init_Str();
      if (ownerAddonId == null || ownerAddonId.isBlank()) {
         return reject(ownerAddonId, label, "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }

      if (categoryId == null || categoryId.isBlank()) {
         return reject(ownerAddonId, label, "missing category");
      }

      if (label == null || label.isBlank()) {
         return reject(ownerAddonId, label, "missing label");
      }

      if (builder == null) {
         return reject(ownerAddonId, label, "missing builder");
      }

      AddonRegistrationResult validation = validateBuilder(ownerAddonId, label, builder);
      if (!validation.accepted()) {
         return validation;
      }

      MacroPresetRegistry.PresetEntry entry = new MacroPresetRegistry.PresetEntry(categoryId, label, tip == null ? "" : tip, builder);
      synchronized (MacroPresetRegistry.class) {
         ENTRIES.add(entry);
         ENTRY_OWNERS.put(entry, ownerAddonId);
      }

      DataUtil.init_V("preset", label);
      return AddonRegistrationResult.accepted("preset", label);
   }

   private static AddonRegistrationResult validateBuilder(String ownerAddonId, String label, Supplier<List<MacroAction>> builder) {
      List<MacroAction> actions;
      try {
         actions = builder.get();
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[MacroPresets] Rejecting preset '{}': builder threw", label, t);
         DataUtil.init(ownerAddonId, "preset", label, "builder threw " + t.getClass().getSimpleName());
         return AddonRegistrationResult.rejected("preset", label, "builder threw " + t.getClass().getSimpleName());
      }

      if (actions == null) {
         return reject(ownerAddonId, label, "builder returned null");
      }

      for (int i = 0; i < actions.size(); i++) {
         if (actions.get(i) == null) {
            return reject(ownerAddonId, label, "builder returned null action at index " + i);
         }
      }

      return AddonRegistrationResult.accepted("preset", label);
   }

   private static AddonRegistrationResult reject(String ownerAddonId, String label, String reason) {
      AutismClientAddon.LOG.warn("[MacroPresets] Rejecting preset '{}': {}", label, reason);
      DataUtil.init(ownerAddonId, "preset", label, reason);
      return AddonRegistrationResult.rejected("preset", label, reason);
   }

   public static synchronized void unregisterAddon(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         List<MacroPresetRegistry.PresetEntry> remove = new ArrayList<>();

         for (MacroPresetRegistry.PresetEntry entry : ENTRIES) {
            if (addonId.equals(ENTRY_OWNERS.get(entry))) {
               remove.add(entry);
            }
         }

         ENTRIES.removeAll(remove);

         for (MacroPresetRegistry.PresetEntry entry : remove) {
            ENTRY_OWNERS.remove(entry);
         }

         CATEGORY_LABELS.entrySet().removeIf(entryx -> addonId.equals(CATEGORY_OWNERS.get(entryx.getKey())));
         CATEGORY_OWNERS.entrySet().removeIf(entryx -> addonId.equals(entryx.getValue()));
      }
   }

   public static synchronized List<MacroPresetRegistry.PresetEntry> entries() {
      return Collections.unmodifiableList(new ArrayList<>(ENTRIES));
   }

   public static synchronized String categoryLabel(String id) {
      return CATEGORY_LABELS.get(id);
   }

   public record PresetEntry(String categoryId, String label, String tip, Supplier<List<MacroAction>> builder) {
   }
}
