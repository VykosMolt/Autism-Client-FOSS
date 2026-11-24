package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class az implements MacroAction {
   public boolean filterByUser = false;
   public int defaultStep = 1;
   public List<az.Inner_DataUtil> entries = new ArrayList<>();
   public boolean listenDuringPreviousAction = false;
   private boolean init = true;

   @Override
   public StreamGate getType() {
      return StreamGate.f_j_StreamGate;
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
         az.Inner_DataUtil e = this.entries.get(0);
         return "Wait LAN: " + (e.init_Str.isEmpty() ? "any" : e.init_Str) + " @ " + e.init_I;
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
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putBoolean("filterByUser", this.filterByUser);
      tag.putInt("defaultStep", this.defaultStep);
      tag.putBoolean("enabled", this.init);
      ListTag list = new ListTag();

      for (az.Inner_DataUtil entry : this.entries) {
         list.add(entry.init_CompoundTag());
      }

      tag.put("entries", list);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.filterByUser = tag.getBooleanOr("filterByUser", false);
      this.defaultStep = tag.getIntOr("defaultStep", 1);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.entries.clear();
      if (tag.contains("entries")) {
         for (Tag el : tag.getList("entries").orElse(new ListTag())) {
            if (el instanceof CompoundTag c) {
               this.entries.add(az.Inner_DataUtil.init(c));
            }
         }
      }
   }

   public static class Inner_DataUtil {
      public String init_Str = "";
      public int init_I = 1;

      public Inner_DataUtil() {
      }

      public Inner_DataUtil(String username, int step) {
         this.init_Str = username != null ? username : "";
         this.init_I = Math.max(1, step);
      }

      public CompoundTag init_CompoundTag() {
         CompoundTag tag = new CompoundTag();
         tag.putString("username", this.init_Str);
         tag.putInt("step", this.init_I);
         return tag;
      }

      public static az.Inner_DataUtil init(CompoundTag tag) {
         az.Inner_DataUtil e = new az.Inner_DataUtil();
         e.init_Str = tag.getStringOr("username", "");
         e.init_I = tag.getIntOr("step", 1);
         return e;
      }

      public String init_Str() {
         return (this.init_Str.isEmpty() ? "Any" : this.init_Str) + " @ step " + this.init_I;
      }
   }
}
