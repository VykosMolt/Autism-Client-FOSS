package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class aD implements CacheEntry, MacroAction {
   public List<aD.Inner_DataUtil> entries = new ArrayList<>();
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private boolean init = true;

   public aD() {
   }

   public aD(int slotNumber) {
      aD.Inner_DataUtil entry = new aD.Inner_DataUtil();
      entry.init_Str = slotNumber >= 0 ? "#" + slotNumber : "";
      entry.init_ArrayUtil = ArrayUtil.update(entry.init_Str);
      this.entries.add(entry);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      ListTag list = new ListTag();

      for (aD.Inner_DataUtil entry : this.entries) {
         CompoundTag entryTag = new CompoundTag();
         ArrayUtil target = entry.init_ArrayUtil();
         if (target.init_Z() || target.update()) {
            entryTag.put("target", target.init_CompoundTag());
         }

         entryTag.putString("mode", entry.init_aD_StreamUtil.name());
         entryTag.putInt("count", entry.init_I);
         list.add(entryTag);
      }

      tag.put("entries", list);
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.entries.clear();
      if (tag.contains("entries")) {
         for (Tag element : tag.getList("entries").orElse(new ListTag())) {
            if (element instanceof CompoundTag entryTag) {
               aD.Inner_DataUtil entry = new aD.Inner_DataUtil();
               entry.init_ArrayUtil = entryTag.getCompound("target")
                  .map(ArrayUtil::init_ArrayUtil_CompoundTag)
                  .orElseGet(() -> ArrayUtil.update(entryTag.getStringOr("target", "")));
               entry.init_Str = entry.init_ArrayUtil.write_Str();
               entry.init_I = entryTag.getIntOr("count", 1);

               try {
                  entry.init_aD_StreamUtil = aD.Inner_StreamUtil.valueOf(entryTag.getStringOr("mode", "NOT_EMPTY"));
               } catch (IllegalArgumentException ignored) {
                  entry.init_aD_StreamUtil = aD.Inner_StreamUtil.init;
               }

               this.entries.add(entry);
            }
         }
      } else {
         aD.Inner_StreamUtil globalMode = aD.Inner_StreamUtil.init;
         int globalCount = 1;
         if (tag.contains("waitMode")) {
            try {
               String mode = tag.getStringOr("waitMode", "NOT_EMPTY");
               if ("HAS_ITEM".equals(mode)) {
                  mode = "NOT_EMPTY";
               }

               globalMode = aD.Inner_StreamUtil.valueOf(mode);
            } catch (IllegalArgumentException var8) {
            }
         }

         if (tag.contains("targetCount")) {
            globalCount = tag.getIntOr("targetCount", 1);
         }

         if (tag.contains("targetEntries")) {
            for (Tag element : tag.getList("targetEntries").orElse(new ListTag())) {
               String value = element.asString().orElse("").strip();
               if (!value.isBlank()) {
                  this.entries.add(new aD.Inner_DataUtil(value, globalMode, globalCount));
               }
            }
         } else {
            boolean slotSpecific = true;
            if (tag.contains("scope")) {
               String scope = tag.getStringOr("scope", "HANDLER_SLOT");
               slotSpecific = "SLOT".equals(scope) || "HANDLER_SLOT".equals(scope);
            }

            int slotNumber = tag.contains("slotNumber") && slotSpecific ? tag.getIntOr("slotNumber", -1) : -1;
            String itemName = tag.getStringOr("itemName", "");
            String target;
            if (slotNumber >= 0 && !itemName.isEmpty()) {
               target = "#" + slotNumber + "|" + itemName;
            } else if (slotNumber >= 0) {
               target = "#" + slotNumber;
            } else {
               target = itemName;
            }

            this.entries.add(new aD.Inner_DataUtil(target, globalMode, globalCount));
         }
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   public void init(CompoundTag tag) {
      this.entries.clear();
      String itemName = tag.getStringOr("itemName", "");
      boolean useSlot = tag.contains("useSlot") && tag.getBooleanOr("useSlot", false);
      int targetSlot = tag.contains("targetSlot") ? tag.getIntOr("targetSlot", -1) : -1;
      int slotNumber = useSlot && targetSlot >= 0 ? targetSlot : -1;
      String target;
      if (slotNumber >= 0 && !itemName.isEmpty()) {
         target = "#" + slotNumber + "|" + itemName;
      } else if (slotNumber >= 0) {
         target = "#" + slotNumber;
      } else {
         target = itemName;
      }

      this.entries.add(new aD.Inner_DataUtil(target, aD.Inner_StreamUtil.init, 1));
      this.init = true;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.table;
   }

   @Override
   public String getDisplayName() {
      if (this.entries.isEmpty()) {
         return "Wait Slot (empty)";
      }

      if (this.entries.size() == 1) {
         aD.Inner_DataUtil entry = this.entries.get(0);
         String display = ah.load_Str_Str_3(entry.init_Str.isEmpty() ? "" : entry.init_Str);
         if (display.isEmpty()) {
            display = "any slot";
         }

         return "Wait " + display + " " + entry.init_Str();
      } else {
         return "Wait all(" + this.entries.size() + ")";
      }
   }

   @Override
   public String getIcon() {
      return "WSL";
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
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public static class Inner_DataUtil {
      public String init_Str = "";
      public ArrayUtil init_ArrayUtil = new ArrayUtil();
      public aD.Inner_StreamUtil init_aD_StreamUtil = aD.Inner_StreamUtil.init;
      public int init_I = 1;

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String target, aD.Inner_StreamUtil waitMode, int targetCount) {
         this.init_Str = target;
         this.init_ArrayUtil = ArrayUtil.update(target);
         this.init_aD_StreamUtil = waitMode;
         this.init_I = targetCount;
      }

      public aD.Inner_DataUtil init_aD_DataUtil() {
         aD.Inner_DataUtil copy = new aD.Inner_DataUtil(this.init_Str, this.init_aD_StreamUtil, this.init_I);
         copy.init_ArrayUtil = this.init_ArrayUtil == null ? new ArrayUtil() : this.init_ArrayUtil.init_ArrayUtil();
         return copy;
      }

      public String init_Str() {
         String var10000;
         switch (this.init_aD_StreamUtil) {
            case init:
               var10000 = "has";
               break;
            case load:
               var10000 = "empty";
               break;
            case read:
               var10000 = ">=" + this.init_I;
               break;
            case write:
               var10000 = "<" + this.init_I;
               break;
            case update:
               var10000 = "~";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      public void init_V() {
         aD.Inner_StreamUtil[] modes = aD.Inner_StreamUtil.values();
         this.init_aD_StreamUtil = modes[(this.init_aD_StreamUtil.ordinal() + 1) % modes.length];
      }

      public void load() {
         aD.Inner_StreamUtil[] modes = aD.Inner_StreamUtil.values();
         this.init_aD_StreamUtil = modes[(this.init_aD_StreamUtil.ordinal() - 1 + modes.length) % modes.length];
      }

      public ArrayUtil init_ArrayUtil() {
         if (this.init_ArrayUtil != null && (this.init_ArrayUtil.init_Z() || this.init_ArrayUtil.update())) {
            return this.init_ArrayUtil;
         }

         this.init_ArrayUtil = ArrayUtil.update(this.init_Str);
         return this.init_ArrayUtil;
      }
   }

   public enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update;
   }
}
