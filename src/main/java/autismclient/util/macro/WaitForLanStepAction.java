package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class WaitForLanStepAction implements MacroAction {
   public boolean filterByUser = false;
   public int defaultStep = 1;
   public List<WaitForLanStepAction.Inner_DataUtil> entries = new ArrayList<>();
   public boolean listenDuringPreviousAction = false;
   private boolean enabled = true;

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_LAN_STEP;
   }

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public String getDisplayName() {
      if (!this.filterByUser) {
         return "Wait LAN: any @ step " + this.defaultStep;
      } else if (this.entries.isEmpty()) {
         return "Wait LAN: any @ step " + this.defaultStep;
      } else if (this.entries.size() == 1) {
         WaitForLanStepAction.Inner_DataUtil e = this.entries.get(0);
         return "Wait LAN: " + (e.username.isEmpty() ? "any" : e.username) + " @ " + e.step;
      } else {
         return "Wait LAN: " + this.entries.size() + " peers";
      }
   }

   @Override
   public String getIcon() {
      return "LAN";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("filterByUser", this.filterByUser);
      tag.putInt("defaultStep", this.defaultStep);
      tag.putBoolean("enabled", this.enabled);
      ListTag list = new ListTag();

      for (WaitForLanStepAction.Inner_DataUtil entry : this.entries) {
         list.add(entry.toTag());
      }

      tag.put("entries", list);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filterByUser = tag.getBooleanOr("filterByUser", false);
      this.defaultStep = tag.getIntOr("defaultStep", 1);
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
      this.entries.clear();
      if (tag.contains("entries")) {
         for (Tag el : tag.getList("entries").orElse(new ListTag())) {
            if (el instanceof CompoundTag c) {
               this.entries.add(WaitForLanStepAction.Inner_DataUtil.fromTag(c));
            }
         }
      }
   }

   public static class Inner_DataUtil {
      public String username = "";
      public int step = 1;

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String username, int step) {
         this.username = username != null ? username : "";
         this.step = Math.max(1, step);
      }

      public CompoundTag toTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("username", this.username);
         tag.putInt("step", this.step);
         return tag;
      }

      public static WaitForLanStepAction.Inner_DataUtil fromTag(CompoundTag tag) {
         WaitForLanStepAction.Inner_DataUtil e = new WaitForLanStepAction.Inner_DataUtil();
         e.username = tag.getStringOr("username", "");
         e.step = tag.getIntOr("step", 1);
         return e;
      }

      public String init_Str() {
         return (this.username.isEmpty() ? "Any" : this.username) + " @ step " + this.step;
      }
   }
}
