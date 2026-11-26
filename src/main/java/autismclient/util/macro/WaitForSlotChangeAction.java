package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class WaitForSlotChangeAction implements CaptureOutputAction, MacroAction {
   public List<WaitForSlotChangeAction.Inner_DataUtil> entries = new ArrayList<>();
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private boolean init = true;

   public WaitForSlotChangeAction() {
   }

   public WaitForSlotChangeAction(int slotNumber) {
      WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil();
      entry.init_Str = slotNumber >= 0 ? "#" + slotNumber : "";
      entry.init_ArrayUtil = MacroItemTarget.parse(entry.init_Str);
      this.entries.add(entry);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      ListTag list = new ListTag();

      for (WaitForSlotChangeAction.Inner_DataUtil entry : this.entries) {
         CompoundTag entryTag = new CompoundTag();
         MacroItemTarget target = entry.init_ArrayUtil();
         if (target.hasSlot() || target.hasItemFilter()) {
            entryTag.put("target", target.toTag());
         }

         entryTag.putString("mode", entry.init_aD_StreamUtil.name());
         entryTag.putInt("count", entry.init_I);
         list.add(entryTag);
      }

      tag.put("entries", list);
      tag.putBoolean("enabled", this.init);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.entries.clear();
      if (tag.contains("entries")) {
         for (Tag element : tag.getList("entries").orElse(new ListTag())) {
            if (element instanceof CompoundTag entryTag) {
               WaitForSlotChangeAction.Inner_DataUtil entry = new WaitForSlotChangeAction.Inner_DataUtil();
               entry.init_ArrayUtil = entryTag.getCompound("target")
                  .map(MacroItemTarget::fromTag)
                  .orElseGet(() -> MacroItemTarget.parse(entryTag.getStringOr("target", "")));
               entry.init_Str = entry.init_ArrayUtil.toSpecString();
               entry.init_I = entryTag.getIntOr("count", 1);

               try {
                  entry.init_aD_StreamUtil = WaitForSlotChangeAction.Inner_StreamUtil.valueOf(entryTag.getStringOr("mode", "NOT_EMPTY"));
               } catch (IllegalArgumentException ignored) {
                  entry.init_aD_StreamUtil = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
               }

               this.entries.add(entry);
            }
         }
      } else {
         WaitForSlotChangeAction.Inner_StreamUtil globalMode = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
         int globalCount = 1;
         if (tag.contains("waitMode")) {
            try {
               String mode = tag.getStringOr("waitMode", "NOT_EMPTY");
               if ("HAS_ITEM".equals(mode)) {
                  mode = "NOT_EMPTY";
               }

               globalMode = WaitForSlotChangeAction.Inner_StreamUtil.valueOf(mode);
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
                  this.entries.add(new WaitForSlotChangeAction.Inner_DataUtil(value, globalMode, globalCount));
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

            this.entries.add(new WaitForSlotChangeAction.Inner_DataUtil(target, globalMode, globalCount));
         }
      }

      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
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

      this.entries.add(new WaitForSlotChangeAction.Inner_DataUtil(target, WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY, 1));
      this.init = true;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_SLOT_CHANGE;
   }

   @Override
   public String getDisplayName() {
      if (this.entries.isEmpty()) {
         return "Wait Slot (empty)";
      }

      if (this.entries.size() == 1) {
         WaitForSlotChangeAction.Inner_DataUtil entry = this.entries.get(0);
         String display = StoreItemAction.load_Str_Str_3(entry.init_Str.isEmpty() ? "" : entry.init_Str);
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
      public MacroItemTarget init_ArrayUtil = new MacroItemTarget();
      public WaitForSlotChangeAction.Inner_StreamUtil init_aD_StreamUtil = WaitForSlotChangeAction.Inner_StreamUtil.NOT_EMPTY;
      public int init_I = 1;

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String target, WaitForSlotChangeAction.Inner_StreamUtil waitMode, int targetCount) {
         this.init_Str = target;
         this.init_ArrayUtil = MacroItemTarget.parse(target);
         this.init_aD_StreamUtil = waitMode;
         this.init_I = targetCount;
      }

      public WaitForSlotChangeAction.Inner_DataUtil init_aD_DataUtil() {
         WaitForSlotChangeAction.Inner_DataUtil copy = new WaitForSlotChangeAction.Inner_DataUtil(this.init_Str, this.init_aD_StreamUtil, this.init_I);
         copy.init_ArrayUtil = this.init_ArrayUtil == null ? new MacroItemTarget() : this.init_ArrayUtil.copy();
         return copy;
      }

      public String init_Str() {
         String var10000;
         switch (this.init_aD_StreamUtil) {
            case NOT_EMPTY:
               var10000 = "has";
               break;
            case IS_EMPTY:
               var10000 = "empty";
               break;
            case COUNT_AT_LEAST:
               var10000 = ">=" + this.init_I;
               break;
            case COUNT_BELOW:
               var10000 = "<" + this.init_I;
               break;
            case ANY_CHANGE:
               var10000 = "~";
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }

      public void init_V() {
         WaitForSlotChangeAction.Inner_StreamUtil[] modes = WaitForSlotChangeAction.Inner_StreamUtil.values();
         this.init_aD_StreamUtil = modes[(this.init_aD_StreamUtil.ordinal() + 1) % modes.length];
      }

      public void load() {
         WaitForSlotChangeAction.Inner_StreamUtil[] modes = WaitForSlotChangeAction.Inner_StreamUtil.values();
         this.init_aD_StreamUtil = modes[(this.init_aD_StreamUtil.ordinal() - 1 + modes.length) % modes.length];
      }

      public MacroItemTarget init_ArrayUtil() {
         if (this.init_ArrayUtil != null && (this.init_ArrayUtil.hasSlot() || this.init_ArrayUtil.hasItemFilter())) {
            return this.init_ArrayUtil;
         }

         this.init_ArrayUtil = MacroItemTarget.parse(this.init_Str);
         return this.init_ArrayUtil;
      }
   }

   public enum Inner_StreamUtil {
      NOT_EMPTY,
      IS_EMPTY,
      COUNT_AT_LEAST,
      COUNT_BELOW,
      ANY_CHANGE;
   }
}
