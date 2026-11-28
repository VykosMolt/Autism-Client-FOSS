package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class WaitInventoryPredicateAction implements CaptureOutputAction, MacroAction {
   public WaitInventoryPredicateAction.InventoryCondition condition = WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS;
   public String itemName = "";
   public int count = 1;
   public int slot = 0;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private transient boolean baselineTaken = false;
   private transient int baselineCount = 0;
   private transient boolean baselineEmpty = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.WAIT_INVENTORY_PREDICATE;
   }

   public boolean isSatisfied(Minecraft mc) {
      if (mc != null && mc.player != null) {
         MacroTemplateResolver.ResolveResult resolution = MacroVariableContext.resolveTemplate(this.itemName, mc);
         if (!resolution.success()) {
            return false;
         }

         MacroItemTarget target = MacroItemTarget.parse(resolution.value());
         this.captureBaseline(mc, target);
         boolean var10000;
         switch (this.condition) {
            case ITEM_EXISTS:
               var10000 = hasItem(mc, target);
               break;
            case COUNT_AT_LEAST:
               var10000 = countItem(mc, target) >= Math.max(1, this.count);
               break;
            case COUNT_CHANGED:
               var10000 = countItem(mc, target) != this.baselineCount;
               break;
            case COUNT_INCREASED:
               var10000 = countItem(mc, target) > this.baselineCount;
               break;
            case COUNT_DECREASED:
               var10000 = countItem(mc, target) < this.baselineCount;
               break;
            case SLOT_EMPTY:
               var10000 = stackInSlot(mc, this.slot).isEmpty();
               break;
            case SLOT_FILLED:
               var10000 = !stackInSlot(mc, this.slot).isEmpty();
               break;
            case SLOT_CHANGED:
               var10000 = stackInSlot(mc, this.slot).isEmpty() != this.baselineEmpty;
               break;
            case INVENTORY_FULL:
               var10000 = load(mc);
               break;
            case INVENTORY_EMPTY:
               var10000 = read(mc);
               break;
            case CURSOR_MATCHES:
               var10000 = target.matchScore(mc.player.containerMenu.getCarried(), -1) >= 0;
               break;
            case CURSOR_EMPTY:
               var10000 = mc.player.containerMenu.getCarried().isEmpty();
               break;
            case CURSOR_FILLED:
               var10000 = !mc.player.containerMenu.getCarried().isEmpty();
               break;
            case SELECTED_SLOT:
               var10000 = mc.player.getInventory().getSelectedSlot() == Math.max(0, Math.min(8, this.slot));
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return false;
      }
   }

   public void init() {
      this.baselineTaken = false;
   }

   private void captureBaseline(Minecraft mc, MacroItemTarget target) {
      if (!this.baselineTaken) {
         this.baselineCount = countItem(mc, target);
         this.baselineEmpty = stackInSlot(mc, this.slot).isEmpty();
         this.baselineTaken = true;
      }
   }

   static boolean hasItem(Minecraft mc, MacroItemTarget target) {
      return countItem(mc, target) > 0;
   }

   static int countItem(Minecraft mc, MacroItemTarget target) {
      if (mc != null && mc.player != null && target != null && target.hasItemFilter()) {
         int total = 0;

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (target.matchScore(stack, i) >= 0) {
               total += stack.getCount();
            }
         }

         return total;
      } else {
         return 0;
      }
   }

   private static ItemStack stackInSlot(Minecraft mc, int slot) {
      if (mc != null && mc.player != null) {
         int idx = Math.max(0, Math.min(mc.player.getInventory().getContainerSize() - 1, slot));
         return mc.player.getInventory().getItem(idx);
      } else {
         return ItemStack.EMPTY;
      }
   }

   private static boolean load(Minecraft mc) {
      for (int i = 0; i < 36; i++) {
         if (mc.player.getInventory().getItem(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   private static boolean read(Minecraft mc) {
      for (int i = 0; i < 36; i++) {
         if (!mc.player.getInventory().getItem(i).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "WAIT_INVENTORY_PREDICATE");
      tag.putString("condition", this.condition.name());
      tag.putString("itemName", this.itemName);
      tag.putInt("count", this.count);
      tag.putInt("slot", this.slot);
      tag.putInt("timeoutMs", this.timeoutMs);
      ListenDuringPreviousSerializer.writeToTag(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.condition = MacroNbtCodecHelper.parseEnum(WaitInventoryPredicateAction.InventoryCondition.class, tag.getStringOr("condition", "ITEM_EXISTS"), WaitInventoryPredicateAction.InventoryCondition.ITEM_EXISTS);
      this.itemName = tag.getStringOr("itemName", "");
      this.count = tag.getIntOr("count", 1);
      this.slot = tag.getIntOr("slot", 0);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      ListenDuringPreviousSerializer.readFromTag(tag, this);
      this.saveAs = tag.getStringOr("saveAs", "");
   }

   @Override
   public String getDisplayName() {
      return "Wait inventory " + this.condition;
   }

   @Override
   public String getIcon() {
      return "I";
   }

   @Override
   public String getSaveAs() {
      return this.saveAs;
   }

   @Override
   public void setSaveAs(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public enum InventoryCondition {
      ITEM_EXISTS,
      COUNT_AT_LEAST,
      COUNT_CHANGED,
      COUNT_INCREASED,
      COUNT_DECREASED,
      SLOT_EMPTY,
      SLOT_FILLED,
      SLOT_CHANGED,
      INVENTORY_FULL,
      INVENTORY_EMPTY,
      CURSOR_MATCHES,
      CURSOR_EMPTY,
      CURSOR_FILLED,
      SELECTED_SLOT;
   }
}
