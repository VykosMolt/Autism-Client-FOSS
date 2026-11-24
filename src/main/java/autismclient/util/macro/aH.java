package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class aH implements MacroAction {
   public aH.Inner_StreamUtil countMode = aH.Inner_StreamUtil.init;
   public aH.Inner_DataUtil comparison = aH.Inner_DataUtil.load;
   public int slots = 0;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_F_StreamGate;
   }

   public boolean init(Minecraft mc) {
      if (mc != null && mc.player != null) {
         int free = 0;
         int filled = 0;
         int size = Math.min(36, mc.player.getInventory().getContainerSize());

         for (int i = 0; i < size; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
               free++;
            } else {
               filled++;
            }
         }

         int actual = this.countMode == aH.Inner_StreamUtil.load ? filled : free;
         return this.init(actual, Math.max(0, Math.min(36, this.slots)));
      } else {
         return false;
      }
   }

   private boolean init(int actual, int target) {
      boolean var10000;
      switch (this.comparison) {
         case init:
            var10000 = actual < target;
            break;
         case load:
            var10000 = actual <= target;
            break;
         case read:
            var10000 = actual == target;
            break;
         case write:
            var10000 = actual >= target;
            break;
         case update:
            var10000 = actual > target;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("countMode", this.countMode.name());
      tag.putString("comparison", this.comparison.name());
      tag.putInt("slots", Math.max(0, Math.min(36, this.slots)));
      tag.putInt("timeoutMs", Math.max(0, this.timeoutMs));
      tag.putBoolean("enabled", this.init);
      p.init(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.countMode = i.init(aH.Inner_StreamUtil.class, tag.getStringOr("countMode", aH.Inner_StreamUtil.init.name()), aH.Inner_StreamUtil.init);
      this.comparison = i.init(aH.Inner_DataUtil.class, tag.getStringOr("comparison", aH.Inner_DataUtil.load.name()), aH.Inner_DataUtil.load);
      this.slots = Math.max(0, Math.min(36, tag.getIntOr("slots", 0)));
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      p.load(tag, this);
   }

   @Override
   public String getDisplayName() {
      return "Wait " + (this.countMode == aH.Inner_StreamUtil.load ? "Filled" : "Free") + " Slots " + this.init() + " " + Math.max(0, Math.min(36, this.slots));
   }

   private String init() {
      String var10000;
      switch (this.comparison) {
         case init:
            var10000 = "<";
            break;
         case load:
            var10000 = "<=";
            break;
         case read:
            var10000 = "=";
            break;
         case write:
            var10000 = ">=";
            break;
         case update:
            var10000 = ">";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   @Override
   public String getIcon() {
      return "FS";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update;
   }

   public enum Inner_StreamUtil {
      init,
      load;
   }
}
