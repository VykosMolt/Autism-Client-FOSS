package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class PoolEntry implements MacroAction {
   public int bodyCount = 1;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_J_StreamGate;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "FINALLY");
      tag.putInt("bodyCount", this.bodyCount);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.bodyCount = tag.getIntOr("bodyCount", 1);
   }

   @Override
   public String getDisplayName() {
      return "Finally cleanup next " + this.bodyCount;
   }

   @Override
   public String getIcon() {
      return "F";
   }
}
