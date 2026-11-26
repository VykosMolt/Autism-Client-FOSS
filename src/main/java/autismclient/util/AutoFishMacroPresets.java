package autismclient.util;

import autismclient.util.macro.MacroAction;
import autismclient.util.macro.RaceEligibilityChecker;
import java.util.Locale;

public final class AutoFishMacroPresets {
   public static final String init = "AutoFish - Free Slots Stop";
   public static final String load = "AutoFish - Durability Stop";
   public static final String read = "AutoFish - Custom Stop";

   private AutoFishMacroPresets() {
   }

   public static AutismMacro init(AutoFishMacroPresets.Inner_DataUtil preset) {
      AutismMacro var10000;
      switch (preset == null ? AutoFishMacroPresets.Inner_DataUtil.CUSTOM : preset) {
         case FREE_SLOTS:
            var10000 = init("AutoFish - Free Slots Stop", init_aH());
            break;
         case DURABILITY:
            var10000 = init("AutoFish - Durability Stop", init_ar());
            break;
         case CUSTOM:
            var10000 = init("AutoFish - Custom Stop", init_aH());
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static boolean init(AutismMacro macro) {
      return RaceEligibilityChecker.init(macro);
   }

   public static boolean init_Z(String name) {
      return name == null
         ? false
         : init(name, "AutoFish - Free Slots Stop") || init(name, "AutoFish - Durability Stop") || init(name, "AutoFish - Custom Stop");
   }

   public static boolean load(AutismMacro macro) {
      return macro != null && init_Z(macro.init_Str);
   }

   public static AutoFishMacroPresets.Inner_DataUtil init_cL_DataUtil(String name) {
      if (name == null) {
         return null;
      } else if (init(name, "AutoFish - Free Slots Stop")) {
         return AutoFishMacroPresets.Inner_DataUtil.FREE_SLOTS;
      } else if (init(name, "AutoFish - Durability Stop")) {
         return AutoFishMacroPresets.Inner_DataUtil.DURABILITY;
      } else {
         return init(name, "AutoFish - Custom Stop") ? AutoFishMacroPresets.Inner_DataUtil.CUSTOM : null;
      }
   }

   public static boolean init(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && init(toggle);
   }

   public static boolean load(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && load(toggle);
   }

   private static AutismMacro init(String preferredName, MacroAction firstCondition) {
      aN manager = aN.init_aN();
      AutismMacro existing = manager.init_aM_Str(preferredName);
      if (existing != null) {
         boolean changed = false;
         if (!init(existing)) {
            existing.init_List.add(0, firstCondition);
            changed = true;
         }

         changed |= read(existing);
         if (changed) {
            manager.load_V();
         }

         return existing;
      } else {
         AutismMacro macro = new AutismMacro(manager.init_Str_Str(preferredName));
         macro.load_Str = "AutoFish stop macro";
         macro.init_List.add(firstCondition);
         manager.init_V_aM(macro);
         return macro;
      }
   }

   private static boolean init(autismclient.util.macro.ak action) {
      if (action == null) {
         return false;
      }

      if (action.entries != null && !action.entries.isEmpty()) {
         for (autismclient.util.macro.ak.Inner_DataUtil entry : action.entries) {
            if (entry != null && "AutoFish".equalsIgnoreCase(entry.init_Str)) {
               return true;
            }
         }

         return false;
      } else {
         return "AutoFish".equalsIgnoreCase(action.moduleName);
      }
   }

   private static boolean load(autismclient.util.macro.ak action) {
      if (action == null) {
         return false;
      }

      if (action.entries != null && !action.entries.isEmpty()) {
         for (autismclient.util.macro.ak.Inner_DataUtil entry : action.entries) {
            if (entry != null && "AutoFish".equalsIgnoreCase(entry.init_Str) && entry.init_ak_StreamUtil == autismclient.util.macro.ak.Inner_StreamUtil.read) {
               return true;
            }
         }

         return false;
      } else {
         return "AutoFish".equalsIgnoreCase(action.moduleName) && action.toggleMode == autismclient.util.macro.ak.Inner_StreamUtil.read;
      }
   }

   private static boolean read(AutismMacro macro) {
      if (macro != null && macro.init_List != null) {
         for (int i = 0; i < macro.init_List.size(); i++) {
            MacroAction action = macro.init_List.get(i);
            if (action != null && action.isEnabled() && RaceEligibilityChecker.init(action)) {
               int next = i + 1;
               if (next < macro.init_List.size() && init(macro.init_List.get(next))) {
                  macro.init_List.remove(next);
                  return true;
               }

               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static boolean init(String name, String base) {
      if (name != null && base != null) {
         String trimmed = name.trim();
         return trimmed.equalsIgnoreCase(base) || trimmed.toLowerCase(Locale.ROOT).startsWith(base.toLowerCase(Locale.ROOT) + " (");
      } else {
         return false;
      }
   }

   private static autismclient.util.macro.WaitFreeSlotsAction init_aH() {
      autismclient.util.macro.WaitFreeSlotsAction action = new autismclient.util.macro.WaitFreeSlotsAction();
      action.countMode = autismclient.util.macro.WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS;
      action.comparison = autismclient.util.macro.WaitFreeSlotsAction.Inner_DataUtil.AT_MOST;
      action.slots = 0;
      action.timeoutMs = 0;
      return action;
   }

   private static autismclient.util.macro.WaitDurabilityAction init_ar() {
      autismclient.util.macro.WaitDurabilityAction action = new autismclient.util.macro.WaitDurabilityAction();
      action.targetMode = autismclient.util.macro.WaitDurabilityAction.Inner_CachePool.ITEM;
      action.itemName = "minecraft:fishing_rod";
      action.measurement = autismclient.util.macro.WaitDurabilityAction.Inner_StreamUtil.REMAINING;
      action.comparison = autismclient.util.macro.WaitDurabilityAction.Inner_DataUtil.AT_MOST;
      action.value = 2;
      action.useNext = true;
      action.timeoutMs = 0;
      return action;
   }

   public enum Inner_DataUtil {
      FREE_SLOTS,
      DURABILITY,
      CUSTOM;
   }
}
