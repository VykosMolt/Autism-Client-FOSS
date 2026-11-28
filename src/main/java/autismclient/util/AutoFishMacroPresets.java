package autismclient.util;

import autismclient.util.macro.MacroAction;
import autismclient.util.macro.RaceEligibilityChecker;
import java.util.Locale;

public final class AutoFishMacroPresets {
   public static final String FREE_SLOTS_STOP_NAME = "AutoFish - Free Slots Stop";
   public static final String DURABILITY_STOP_NAME = "AutoFish - Durability Stop";
   public static final String read = "AutoFish - Custom Stop";

   private AutoFishMacroPresets() {
   }

   public static AutismMacro init(AutoFishMacroPresets.Preset preset) {
      AutismMacro var10000;
      switch (preset == null ? AutoFishMacroPresets.Preset.CUSTOM : preset) {
         case FREE_SLOTS:
            var10000 = ensurePreset("AutoFish - Free Slots Stop", freeSlotsWait());
            break;
         case DURABILITY:
            var10000 = ensurePreset("AutoFish - Durability Stop", durabilityWait());
            break;
         case CUSTOM:
            var10000 = ensurePreset("AutoFish - Custom Stop", freeSlotsWait());
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static boolean isRaceEligible(AutismMacro macro) {
      return RaceEligibilityChecker.hasEligible(macro);
   }

   public static boolean isStopPreset(String name) {
      return name == null
         ? false
         : matchesPresetName(name, "AutoFish - Free Slots Stop") || matchesPresetName(name, "AutoFish - Durability Stop") || matchesPresetName(name, "AutoFish - Custom Stop");
   }

   public static boolean isStopMacro(AutismMacro macro) {
      return macro != null && isStopPreset(macro.name);
   }

   public static AutoFishMacroPresets.Preset presetFor(String name) {
      if (name == null) {
         return null;
      } else if (matchesPresetName(name, "AutoFish - Free Slots Stop")) {
         return AutoFishMacroPresets.Preset.FREE_SLOTS;
      } else if (matchesPresetName(name, "AutoFish - Durability Stop")) {
         return AutoFishMacroPresets.Preset.DURABILITY;
      } else {
         return matchesPresetName(name, "AutoFish - Custom Stop") ? AutoFishMacroPresets.Preset.CUSTOM : null;
      }
   }

   public static boolean init(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && init(toggle);
   }

   public static boolean isPresetToggle(MacroAction action) {
      return action instanceof autismclient.util.macro.ak toggle && load(toggle);
   }

   private static AutismMacro ensurePreset(String preferredName, MacroAction firstCondition) {
      MacroLibraryManager manager = MacroLibraryManager.getInstance();
      AutismMacro existing = manager.findByName(preferredName);
      if (existing != null) {
         boolean changed = false;
         if (!isRaceEligible(existing)) {
            existing.actions.add(0, firstCondition);
            changed = true;
         }

         changed |= read(existing);
         if (changed) {
            manager.save();
         }

         return existing;
      } else {
         AutismMacro macro = new AutismMacro(manager.generateUniqueName(preferredName));
         macro.description = "AutoFish stop macro";
         macro.actions.add(firstCondition);
         manager.saveMacro(macro);
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
      if (macro != null && macro.actions != null) {
         for (int i = 0; i < macro.actions.size(); i++) {
            MacroAction action = macro.actions.get(i);
            if (action != null && action.isEnabled() && RaceEligibilityChecker.isEligible(action)) {
               int next = i + 1;
               if (next < macro.actions.size() && init(macro.actions.get(next))) {
                  macro.actions.remove(next);
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

   private static boolean matchesPresetName(String name, String base) {
      if (name != null && base != null) {
         String trimmed = name.trim();
         return trimmed.equalsIgnoreCase(base) || trimmed.toLowerCase(Locale.ROOT).startsWith(base.toLowerCase(Locale.ROOT) + " (");
      } else {
         return false;
      }
   }

   private static autismclient.util.macro.WaitFreeSlotsAction freeSlotsWait() {
      autismclient.util.macro.WaitFreeSlotsAction action = new autismclient.util.macro.WaitFreeSlotsAction();
      action.countMode = autismclient.util.macro.WaitFreeSlotsAction.Inner_StreamUtil.FREE_SLOTS;
      action.comparison = autismclient.util.macro.WaitFreeSlotsAction.Inner_DataUtil.AT_MOST;
      action.slots = 0;
      action.timeoutMs = 0;
      return action;
   }

   private static autismclient.util.macro.WaitDurabilityAction durabilityWait() {
      autismclient.util.macro.WaitDurabilityAction action = new autismclient.util.macro.WaitDurabilityAction();
      action.targetMode = autismclient.util.macro.WaitDurabilityAction.TargetMode.ITEM;
      action.itemName = "minecraft:fishing_rod";
      action.measurement = autismclient.util.macro.WaitDurabilityAction.Measurement.REMAINING;
      action.comparison = autismclient.util.macro.WaitDurabilityAction.Comparison.AT_MOST;
      action.value = 2;
      action.useNext = true;
      action.timeoutMs = 0;
      return action;
   }

   public enum Preset {
      FREE_SLOTS,
      DURABILITY,
      CUSTOM;
   }
}
