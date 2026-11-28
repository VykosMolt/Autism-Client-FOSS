package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

public class WaitFreeSlotsAction implements MacroAction {
   public WaitFreeSlotsAction.CountMode countMode = WaitFreeSlotsAction.CountMode.FREE_SLOTS;
   public WaitFreeSlotsAction.Comparison comparison = WaitFreeSlotsAction.Comparison.AT_MOST;
   public int slots = 0;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_FREE_SLOTS;
   }

   public boolean test(Minecraft mc) {
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

         int actual = this.countMode == WaitFreeSlotsAction.CountMode.FILLED_SLOTS ? filled : free;
         return this.meetsTarget(actual, Math.max(0, Math.min(36, this.slots)));
      } else {
         return false;
      }
   }

   private boolean meetsTarget(int actual, int target) {
      boolean var10000;
      switch (this.comparison) {
         case BELOW:
            var10000 = actual < target;
            break;
         case AT_MOST:
            var10000 = actual <= target;
            break;
         case EXACT:
            var10000 = actual == target;
            break;
         case AT_LEAST:
            var10000 = actual >= target;
            break;
         case ABOVE:
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
      tag.putBoolean("enabled", this.enabled);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.countMode = MacroNbtCodecHelper.parseEnum(WaitFreeSlotsAction.CountMode.class, tag.getStringOr("countMode", WaitFreeSlotsAction.CountMode.FREE_SLOTS.name()), WaitFreeSlotsAction.CountMode.FREE_SLOTS);
      this.comparison = MacroNbtCodecHelper.parseEnum(WaitFreeSlotsAction.Comparison.class, tag.getStringOr("comparison", WaitFreeSlotsAction.Comparison.AT_MOST.name()), WaitFreeSlotsAction.Comparison.AT_MOST);
      this.slots = Math.max(0, Math.min(36, tag.getIntOr("slots", 0)));
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
   }

   @Override
   public String getDisplayName() {
      return "Wait " + (this.countMode == WaitFreeSlotsAction.CountMode.FILLED_SLOTS ? "Filled" : "Free") + " Slots " + this.comparisonSymbol() + " " + Math.max(0, Math.min(36, this.slots));
   }

   private String comparisonSymbol() {
      String var10000;
      switch (this.comparison) {
         case BELOW:
            var10000 = "<";
            break;
         case AT_MOST:
            var10000 = "<=";
            break;
         case EXACT:
            var10000 = "=";
            break;
         case AT_LEAST:
            var10000 = ">=";
            break;
         case ABOVE:
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
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   public enum Comparison {
      BELOW,
      AT_MOST,
      EXACT,
      AT_LEAST,
      ABOVE;
   }

   public enum CountMode {
      FREE_SLOTS,
      FILLED_SLOTS;
   }
}
