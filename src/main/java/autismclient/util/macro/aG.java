package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class aG implements CacheEntry, MacroAction {
   public String targetDimension = "";
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("targetDimension", this.targetDimension == null ? "" : this.targetDimension);
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.targetDimension = tag.getStringOr("targetDimension", "");
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public StreamGate getType() {
      return StreamGate.WAIT_WORLD_CHANGE;
   }

   @Override
   public String getDisplayName() {
      return this.targetDimension != null && !this.targetDimension.isBlank() ? "Wait World: " + this.targetDimension : "Wait World Change";
   }

   @Override
   public String getIcon() {
      return "WRL";
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
}
