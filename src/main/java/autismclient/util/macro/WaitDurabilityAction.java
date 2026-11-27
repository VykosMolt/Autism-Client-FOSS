package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class WaitDurabilityAction implements MacroAction {
   public WaitDurabilityAction.Inner_CachePool targetMode = WaitDurabilityAction.Inner_CachePool.HELD;
   public String itemName = "";
   public int slot = 0;
   public WaitDurabilityAction.Inner_StreamUtil measurement = WaitDurabilityAction.Inner_StreamUtil.REMAINING;
   public WaitDurabilityAction.Inner_DataUtil comparison = WaitDurabilityAction.Inner_DataUtil.AT_MOST;
   public int value = 2;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   public boolean useNext = false;
   private boolean init = true;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_DURABILITY;
   }

   public boolean init(Minecraft mc) {
      if (mc != null && mc.player != null) {
         boolean var10000;
         switch (this.targetMode) {
            case HELD:
               var10000 = this.matches(mc.player.getMainHandItem()) || this.matches(mc.player.getOffhandItem());
               break;
            case ITEM:
               var10000 = this.load(mc);
               break;
            case SLOT:
               var10000 = this.matches(this.init(mc, this.slot));
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   private boolean load(Minecraft mc) {
      MacroItemTarget target = MacroItemTarget.parse(this.itemName).resolveTemplates(mc);
      if (target == null) {
         return false;
      }

      if (!target.hasItemFilter() && !target.hasSlot()) {
         return this.matches(mc.player.getMainHandItem()) || this.matches(mc.player.getOffhandItem());
      }

      MacroItemTarget itemOnly = target.copy();
      if (itemOnly.hasItemFilter()) {
         itemOnly.slot = -1;
      }

      int size = Math.min(36, mc.player.getInventory().getContainerSize());

      for (int i = 0; i < size; i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         if (itemOnly.matchScore(stack, i) >= 0 && this.matches(stack)) {
            return true;
         }
      }

      return itemOnly.matchScore(mc.player.getOffhandItem(), 40) >= 0 && this.matches(mc.player.getOffhandItem());
   }

   private ItemStack init(Minecraft mc, int visibleSlot) {
      if (mc == null || mc.player == null) {
         return ItemStack.EMPTY;
      }

      if (visibleSlot == 40) {
         return mc.player.getOffhandItem();
      }

      int size = mc.player.getInventory().getContainerSize();
      int index = Math.max(0, Math.min(Math.max(0, size - 1), visibleSlot));
      return mc.player.getInventory().getItem(index);
   }

   public boolean matches(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) {
         int metric = this.init_I(stack);
         int target = this.load(stack);
         return this.init(metric, target);
      } else {
         return false;
      }
   }

   private int init_I(ItemStack stack) {
      int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
      int var10000;
      switch (this.measurement) {
         case REMAINING:
            var10000 = remaining;
            break;
         case DAMAGE_USED:
            var10000 = Math.max(0, stack.getDamageValue());
            break;
         case PERCENT_REMAINING:
            var10000 = stack.getMaxDamage() <= 0 ? 100 : Math.round(remaining * 100.0F / stack.getMaxDamage());
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private int load(ItemStack stack) {
      int max = this.measurement == WaitDurabilityAction.Inner_StreamUtil.PERCENT_REMAINING ? 100 : Math.max(0, stack == null ? 2048 : stack.getMaxDamage());
      return Math.max(0, Math.min(max, this.value));
   }

   private boolean init(int actual, int target) {
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
      tag.putString("targetMode", this.targetMode.name());
      tag.putString("itemName", this.itemName == null ? "" : this.itemName);
      tag.putInt("slot", Math.max(0, Math.min(40, this.slot)));
      tag.putString("measurement", this.measurement.name());
      tag.putString("comparison", this.comparison.name());
      tag.putInt("value", Math.max(0, this.value));
      tag.putInt("timeoutMs", Math.max(0, this.timeoutMs));
      tag.putBoolean("useNext", this.useNext);
      tag.putBoolean("enabled", this.init);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.targetMode = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Inner_CachePool.class, tag.getStringOr("targetMode", WaitDurabilityAction.Inner_CachePool.HELD.name()), WaitDurabilityAction.Inner_CachePool.HELD);
      this.itemName = tag.getStringOr("itemName", "");
      this.slot = Math.max(0, Math.min(40, tag.getIntOr("slot", 0)));
      this.measurement = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Inner_StreamUtil.class, tag.getStringOr("measurement", WaitDurabilityAction.Inner_StreamUtil.REMAINING.name()), WaitDurabilityAction.Inner_StreamUtil.REMAINING);
      this.comparison = MacroNbtCodecHelper.parseEnum(WaitDurabilityAction.Inner_DataUtil.class, tag.getStringOr("comparison", WaitDurabilityAction.Inner_DataUtil.AT_MOST.name()), WaitDurabilityAction.Inner_DataUtil.AT_MOST);
      this.value = Math.max(0, tag.getIntOr("value", 2));
      this.timeoutMs = Math.max(0, tag.getIntOr("timeoutMs", 0));
      this.useNext = tag.getBooleanOr("useNext", false);
      if (tag.contains("enabled")) {
         this.init = tag.getBooleanOr("enabled", true);
      }

      ListenDuringPreviousSerializer.readFromTag(tag, this);
   }

   @Override
   public String getDisplayName() {
      return "Wait Durability " + this.init() + " " + this.load() + " " + this.read() + " " + Math.max(0, this.value) + (this.useNext ? " + next" : "");
   }

   private String init() {
      String var10000;
      switch (this.targetMode) {
         case HELD:
            var10000 = "Held";
            break;
         case ITEM:
            var10000 = this.itemName != null && !this.itemName.isBlank() ? MacroItemTarget.parse(this.itemName).formatSummary() : "Held";
            break;
         case SLOT:
            var10000 = "#" + Math.max(0, Math.min(40, this.slot));
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String load() {
      String var10000;
      switch (this.measurement) {
         case REMAINING:
            var10000 = "remaining";
            break;
         case DAMAGE_USED:
            var10000 = "damage";
            break;
         case PERCENT_REMAINING:
            var10000 = "%";
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private String read() {
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
      return "DUR";
   }

   @Override
   public boolean isEnabled() {
      return this.init;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init = e;
   }

   public enum Inner_CachePool {
      HELD,
      ITEM,
      SLOT;
   }

   public enum Inner_DataUtil {
      BELOW,
      AT_MOST,
      EXACT,
      AT_LEAST,
      ABOVE;
   }

   public enum Inner_StreamUtil {
      REMAINING,
      DAMAGE_USED,
      PERCENT_REMAINING;
   }
}
