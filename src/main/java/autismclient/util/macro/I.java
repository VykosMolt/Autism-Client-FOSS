package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class I implements MacroAction {
   public int times = 1;

   @Override
   public void execute(Minecraft mc) {
      autismclient.util.K.init(mc, null, this.times);
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "PICK_UP_ALL");
      tag.putInt("times", Math.max(1, this.times));
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.times = Math.max(1, tag.getIntOr("times", 1));
   }

   @Override
   public StreamGate getType() {
      return StreamGate.PICK_UP_ALL;
   }

   @Override
   public String getDisplayName() {
      return "Pick Up All" + (this.times > 1 ? " x" + this.times : "");
   }

   @Override
   public String getIcon() {
      return "All";
   }
}
