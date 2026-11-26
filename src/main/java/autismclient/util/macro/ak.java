package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class ak implements MacroAction {
   public String moduleName = "";
   public ak.Inner_StreamUtil toggleMode = ak.Inner_StreamUtil.init;
   public List<ak.Inner_DataUtil> entries = new ArrayList<>();
   private boolean init = true;

   public ak() {
   }

   public ak(String moduleName) {
      this.moduleName = moduleName == null ? "" : moduleName.trim();
      if (!this.moduleName.isBlank()) {
         this.entries.add(new ak.Inner_DataUtil(this.moduleName, this.toggleMode));
      }
   }

   @Override
   public void execute(Minecraft mc) {
      if (!this.entries.isEmpty()) {
         for (ak.Inner_DataUtil entry : this.entries) {
            if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
               autismclient.util.CommandPrefixHelper.init(entry.init_Str, entry.init_ak_StreamUtil);
            }
         }
      } else {
         autismclient.util.CommandPrefixHelper.init(this.moduleName, this.toggleMode);
      }
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("moduleName", this.moduleName);
      tag.putString("toggleMode", this.toggleMode.name());
      ListTag list = new ListTag();

      for (ak.Inner_DataUtil entry : this.entries) {
         if (entry != null && entry.init_Str != null && !entry.init_Str.isBlank()) {
            list.add(entry.init());
         }
      }

      tag.put("entries", list);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      if (tag.contains("moduleName")) {
         this.moduleName = tag.getStringOr("moduleName", "");
      }

      if (tag.contains("toggleMode")) {
         try {
            this.toggleMode = ak.Inner_StreamUtil.valueOf(tag.getStringOr("toggleMode", "TOGGLE"));
         } catch (IllegalArgumentException ignored) {
            this.toggleMode = ak.Inner_StreamUtil.init;
         }
      }

      this.entries.clear();
      if (tag.contains("entries")) {
         for (Tag element : tag.getList("entries").orElse(new ListTag())) {
            if (element instanceof CompoundTag compound) {
               ak.Inner_DataUtil entry = ak.Inner_DataUtil.init(compound);
               if (entry.init_Str != null && !entry.init_Str.isBlank()) {
                  this.entries.add(entry);
               }
            }
         }
      }

      if (this.entries.isEmpty() && this.moduleName != null && !this.moduleName.isBlank()) {
         this.entries.add(new ak.Inner_DataUtil(this.moduleName, this.toggleMode));
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.TOGGLE_MODULE;
   }

   @Override
   public String getDisplayName() {
      if (!this.entries.isEmpty()) {
         return this.entries.size() == 1
            ? init(this.entries.get(0).init_ak_StreamUtil) + " Module (" + this.entries.get(0).init_Str + ")"
            : "Module Batch (" + this.entries.size() + ")";
      }

      String var10000;
      switch (this.toggleMode) {
         case load:
            var10000 = "Enable";
            break;
         case read:
            var10000 = "Disable";
            break;
         default:
            var10000 = "Toggle";
      }

      String modeStr = var10000;
      return modeStr + " Module (" + (this.moduleName.isEmpty() ? "None" : this.moduleName) + ")";
   }

   @Override
   public String getIcon() {
      return "M";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public void sanitizeForSharing() {
      this.entries.removeIf(e -> e != null && autismclient.modules.aD.init(e.init_Str));
      if (autismclient.modules.aD.init(this.moduleName)) {
         this.moduleName = "";
         this.toggleMode = ak.Inner_StreamUtil.init;
      }
   }

   private static String init(ak.Inner_StreamUtil mode) {
      String var10000;
      switch (mode) {
         case load:
            var10000 = "Enable";
            break;
         case read:
            var10000 = "Disable";
            break;
         default:
            var10000 = "Toggle";
      }

      return var10000;
   }

   public static class Inner_DataUtil {
      public String init_Str = "";
      public ak.Inner_StreamUtil init_ak_StreamUtil = ak.Inner_StreamUtil.init;

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String moduleName, ak.Inner_StreamUtil toggleMode) {
         this.init_Str = moduleName == null ? "" : moduleName.trim();
         this.init_ak_StreamUtil = toggleMode == null ? ak.Inner_StreamUtil.init : toggleMode;
      }

      public CompoundTag init() {
         CompoundTag tag = new CompoundTag();
         tag.putString("moduleName", this.init_Str);
         tag.putString("toggleMode", this.init_ak_StreamUtil.name());
         return tag;
      }

      public static ak.Inner_DataUtil init(CompoundTag tag) {
         ak.Inner_DataUtil entry = new ak.Inner_DataUtil();
         if (tag.contains("moduleName")) {
            entry.init_Str = tag.getStringOr("moduleName", "");
         }

         if (tag.contains("toggleMode")) {
            try {
               entry.init_ak_StreamUtil = ak.Inner_StreamUtil.valueOf(tag.getStringOr("toggleMode", "TOGGLE"));
            } catch (IllegalArgumentException ignored) {
               entry.init_ak_StreamUtil = ak.Inner_StreamUtil.init;
            }
         }

         return entry;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read;
   }
}
