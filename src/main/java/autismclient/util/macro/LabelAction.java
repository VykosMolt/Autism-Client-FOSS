package autismclient.util.macro;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class LabelAction implements MacroAction {
   public String name = "";
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.LABEL;
   }

   public static String normalize(String s) {
      return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
   }

   public String normalizedName() {
      return normalize(this.name);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "LABEL");
      tag.putString("name", this.name == null ? "" : this.name);
      tag.putBoolean("enabled", this.init);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.name = tag.getStringOr("name", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public String getDisplayName() {
      return "Label: " + (this.name != null && !this.name.isBlank() ? this.name : "(unnamed)");
   }

   @Override
   public String getIcon() {
      return "::";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }
}
