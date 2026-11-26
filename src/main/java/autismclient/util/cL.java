package autismclient.util;

import autismclient.util.macro.MacroAction;
import autismclient.util.macro.c;
import java.util.Locale;

public final class cL {
   public static final String init = "AutoFish - Free Slots Stop";
   public static final String load = "AutoFish - Durability Stop";
   public static final String read = "AutoFish - Custom Stop";

   private cL() {
   }

   public static aM init(cL.Inner_DataUtil preset) {
      aM var10000;
      switch (preset == null ? cL.Inner_DataUtil.CUSTOM : preset) {
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

   public static boolean init(aM macro) {
      return c.init(macro);
   }

   public static boolean init_Z(String name) {
      return name == null
         ? false
         : init(name, "AutoFish - Free Slots Stop") || init(name, "AutoFish - Durability Stop") || init(name, "AutoFish - Custom Stop");
   }

   public static boolean load(aM macro) {
      return macro != null && init_Z(macro.init_Str);
   }

   public static cL.Inner_DataUtil init_cL_DataUtil(String name) {
      if (name == null) {
         return null;
      } else if (init(name, "AutoFish - Free Slots Stop")) {
         return cL.Inner_DataUtil.FREE_SLOTS;
      } else if (init(name, "AutoFish - Durability Stop")) {
         return cL.Inner_DataUtil.DURABILITY;
      } else {
         return init(name, "AutoFish - Custom Stop") ? cL.Inner_DataUtil.CUSTOM : null;
      }
   }

   public static boolean init(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && init(toggle);
   }

   public static boolean load(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && load(toggle);
   }

   private static aM init(String preferredName, MacroAction firstCondition) {
      aN manager = aN.init_aN();
      aM existing = manager.init_aM_Str(preferredName);
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
         aM macro = new aM(manager.init_Str_Str(preferredName));
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

   private static boolean read(aM macro) {
      if (macro != null && macro.init_List != null) {
         for (int i = 0; i < macro.init_List.size(); i++) {
            MacroAction action = macro.init_List.get(i);
            if (action != null && action.isEnabled() && c.init(action)) {
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

   private static autismclient.util.macro.aH init_aH() {
      autismclient.util.macro.aH action = new autismclient.util.macro.aH();
      action.countMode = autismclient.util.macro.aH.Inner_StreamUtil.FREE_SLOTS;
      action.comparison = autismclient.util.macro.aH.Inner_DataUtil.AT_MOST;
      action.slots = 0;
      action.timeoutMs = 0;
      return action;
   }

   private static autismclient.util.macro.ar init_ar() {
      autismclient.util.macro.ar action = new autismclient.util.macro.ar();
      action.targetMode = autismclient.util.macro.ar.Inner_CachePool.ITEM;
      action.itemName = "minecraft:fishing_rod";
      action.measurement = autismclient.util.macro.ar.Inner_StreamUtil.REMAINING;
      action.comparison = autismclient.util.macro.ar.Inner_DataUtil.AT_MOST;
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
