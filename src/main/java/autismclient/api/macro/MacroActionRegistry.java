package autismclient.api.macro;

import autismclient.AutismClientAddon;
import autismclient.api.AddonRegistrationResult;
import autismclient.b.DataUtil;
import autismclient.gui.macro.editor.ActionFieldSchema;
import autismclient.gui.macro.editor.FieldType;
import autismclient.gui.macro.editor.FieldDef;
import autismclient.util.macro.MacroAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;

public final class MacroActionRegistry {
   private static final Map<String, MacroActionEntry> ENTRIES = new LinkedHashMap<>();
   private static final Map<String, MacroActionRegistry.ActionCategory> CATEGORIES = new LinkedHashMap<>();
   private static final Map<String, String> ENTRY_OWNERS = new LinkedHashMap<>();
   private static final Map<String, String> CATEGORY_OWNERS = new LinkedHashMap<>();
   private static final Map<String, ActionFieldSchema> RESOLVED_SCHEMAS = new LinkedHashMap<>();
   public static final int ADDON_AUTO_CATEGORY_COLOR = -5609729;
   private static final String ADDON_CAT_PREFIX = "addon:";

   private MacroActionRegistry() {
   }

   public static String scopeCategoryId(String rawId) {
      if (DataUtil.init_Z()) {
         String addonId = DataUtil.init_Str();
         return "addon:" + (addonId != null && !addonId.isBlank() ? addonId : "shared");
      }

      if (rawId != null && rawId.startsWith("addon:")) {
         return rawId;
      }

      String addonId = DataUtil.init_Str();
      String base = addonId != null && !addonId.isBlank() ? addonId : "shared";
      return rawId != null && !rawId.isBlank() ? "addon:" + base + "/" + rawId : "addon:" + base;
   }

   public static synchronized String ensureScopedCategory(String rawId) {
      String scopedId = scopeCategoryId(rawId);
      if (!CATEGORIES.containsKey(scopedId)) {
         registerCategoryInternal(scopedId, DataUtil.load_Str(null), -5609729, DataUtil.init_Str());
      }

      return scopedId;
   }

   public static synchronized void registerScopedCategory(String rawId, String label, int color) {
      String visible = DataUtil.init_Z() ? DataUtil.load_Str(null) : label;
      registerCategoryInternal(scopeCategoryId(rawId), visible, color, DataUtil.init_Str());
   }

   public static synchronized void registerCategory(String id, String label, int color) {
      registerCategoryInternal(id, label, color, null);
   }

   private static synchronized void registerCategoryInternal(String id, String label, int color, String ownerAddonId) {
      if (id != null && !id.isBlank()) {
         CATEGORIES.put(id, new MacroActionRegistry.ActionCategory(id, label != null && !label.isBlank() ? label : id, color));
         if (ownerAddonId != null && !ownerAddonId.isBlank()) {
            CATEGORY_OWNERS.put(id, ownerAddonId);
         }
      }
   }

   public static synchronized MacroActionRegistry.ActionCategory category(String id) {
      return id == null ? null : CATEGORIES.get(id);
   }

   public static boolean register(MacroActionEntry entry) {
      return registerDetailed(entry).accepted();
   }

   // Not synchronized: the factory and the probe are addon code and must not run under the registry lock.
   public static AddonRegistrationResult registerDetailed(MacroActionEntry entry) {
      if (entry == null) {
         return AddonRegistrationResult.rejected("macro action", "", "entry was null");
      }

      String ownerAddonId = DataUtil.init_Str();
      if (ownerAddonId != null && !ownerAddonId.isBlank()) {
         String typeId = entry.typeId();
         if (typeId == null || !typeId.contains(":")) {
            return reject(ownerAddonId, "macro action", typeId, "non-namespaced type id - scope it with AutismAddons.id(...) so it becomes addonId:localId");
         }

         if (!typeId.startsWith(ownerAddonId + ":")) {
            return reject(ownerAddonId, "macro action", typeId, "foreign namespace - the id must start with your addon id; scope it with AutismAddons.id(...)");
         }

         if (entry.factory() == null) {
            return reject(ownerAddonId, "macro action", typeId, "missing factory");
         }

         ActionFieldSchema effectiveSchema = entry.schema();

         MacroAction probe;
         try {
            probe = entry.factory().get();
         } catch (Throwable t) {
            AutismClientAddon.LOG.warn("[MacroActions] Rejecting action '{}' because its factory threw during validation", typeId, t);
            return reject(ownerAddonId, "macro action", typeId, "factory threw: " + errorName(t));
         }

         if (probe == null) {
            return reject(ownerAddonId, "macro action", typeId, "factory returned null");
         }

         if (effectiveSchema == null) {
            ActionSchema declared = AddonAction.schemaOf(probe);
            if (declared != null) {
               effectiveSchema = declared.internal();
            }
         }

         String validationError = validateProbe(typeId, entry, probe, effectiveSchema);
         if (validationError != null) {
            return reject(ownerAddonId, "macro action", typeId, validationError);
         }

         synchronized (MacroActionRegistry.class) {
            if (ENTRIES.containsKey(typeId)) {
               return reject(ownerAddonId, "macro action", typeId, "duplicate type id");
            }

            if (entry.wantsPicker()) {
               entry.setPickerCategory(ensureScopedCategory(entry.pickerCategory()));
            }

            ENTRIES.put(typeId, entry);
            ENTRY_OWNERS.put(typeId, ownerAddonId);
            if (effectiveSchema != null) {
               RESOLVED_SCHEMAS.put(typeId, effectiveSchema);
            }
         }

         DataUtil.init_V("macro action", typeId);
         return AddonRegistrationResult.accepted("macro action", typeId);
      } else {
         return reject(ownerAddonId, "macro action", "", "registration outside an addon lifecycle - register from onInitialize() or onRegisterCategories()");
      }
   }

   private static AddonRegistrationResult reject(String ownerAddonId, String kind, String id, String reason) {
      AutismClientAddon.LOG.warn("[MacroActions] Rejecting {} '{}': {}", kind, id, reason);
      DataUtil.init(ownerAddonId, kind, id, reason);
      return AddonRegistrationResult.rejected(kind, id, reason);
   }

   private static String validateProbe(String typeId, MacroActionEntry entry, MacroAction probe, ActionFieldSchema schema) {
      String writtenType;
      CompoundTag tag;
      try {
         tag = probe.toTag();
         writtenType = tag.getStringOr("type", "");
      } catch (Throwable t) {
         AutismClientAddon.LOG.warn("[MacroActions] Rejecting '{}' because toTag() threw", typeId, t);
         return "toTag() threw: " + errorName(t);
      }

      if (!typeId.equals(writtenType)) {
         return "writes type=\"" + writtenType + "\" instead of \"" + typeId + "\"";
      }

      if (schema != null) {
         for (FieldDef field : schema.fields()) {
            String key = field.key();
            if (field.type() == FieldType.BLOCK_POS) {
               for (String axisKey : field.xyzKeys()) {
                  if (axisKey != null && !axisKey.isEmpty() && !tag.contains(axisKey)) {
                     return "schema field '" + axisKey + "' is not written by toTag()/save()";
                  }
               }
            } else if (key != null && !key.isEmpty() && !tag.contains(key)) {
               return "schema field '" + key + "' is not written by toTag()/save()";
            }
         }
      }

      return entry.isCondition() && !(probe instanceof ContextMacroAction) ? "conditions must implement ContextMacroAction" : null;
   }

   public static synchronized void unregisterAddon(String addonId) {
      if (addonId != null && !addonId.isBlank()) {
         List<String> removeTypes = new ArrayList<>();

         for (Entry<String, String> owner : ENTRY_OWNERS.entrySet()) {
            if (addonId.equals(owner.getValue())) {
               removeTypes.add(owner.getKey());
            }
         }

         for (String typeId : removeTypes) {
            ENTRIES.remove(typeId);
            RESOLVED_SCHEMAS.remove(typeId);
            ENTRY_OWNERS.remove(typeId);
         }

         List<String> removeCategories = new ArrayList<>();

         for (Entry<String, String> owner : CATEGORY_OWNERS.entrySet()) {
            if (addonId.equals(owner.getValue())) {
               removeCategories.add(owner.getKey());
            }
         }

         for (String categoryId : removeCategories) {
            CATEGORIES.remove(categoryId);
            CATEGORY_OWNERS.remove(categoryId);
         }
      }
   }

   public static synchronized Supplier<MacroAction> factory(String typeId) {
      MacroActionEntry e = typeId == null ? null : ENTRIES.get(typeId);
      return e == null ? null : e.factory();
   }

   public static synchronized ActionFieldSchema schema(String typeId) {
      return typeId == null ? null : RESOLVED_SCHEMAS.get(typeId);
   }

   public static synchronized boolean isCondition(String typeId) {
      MacroActionEntry e = typeId == null ? null : ENTRIES.get(typeId);
      return e != null && e.isCondition();
   }

   public static synchronized String ownerOf(String typeId) {
      return typeId == null ? null : ENTRY_OWNERS.get(typeId);
   }

   public static synchronized List<MacroActionEntry> entries() {
      return Collections.unmodifiableList(new ArrayList<>(ENTRIES.values()));
   }

   private static String errorName(Throwable t) {
      if (t == null) {
         return "unknown";
      }

      String message = t.getMessage();
      return message != null && !message.isBlank() ? t.getClass().getSimpleName() + ": " + message : t.getClass().getSimpleName();
   }

   public record ActionCategory(String id, String label, int color) {
   }
}
