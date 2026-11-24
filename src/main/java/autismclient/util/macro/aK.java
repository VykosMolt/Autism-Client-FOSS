package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class aK implements CacheEntry, MacroAction {
   public aK.Inner_DataUtil condition = aK.Inner_DataUtil.init;
   public String itemName = "";
   public int count = 1;
   public int slot = 0;
   public int timeoutMs = 0;
   public boolean listenDuringPreviousAction = false;
   public String saveAs = "";
   private transient boolean init_Z = false;
   private transient int init_I = 0;
   private transient boolean load = false;

   @Override
   public void execute(Minecraft mc) {
   }

   @Override
   public StreamGate getType() {
      return StreamGate.f_D_StreamGate;
   }

   public boolean init(Minecraft mc) {
      if (mc != null && mc.player != null) {
         j.Inner_CachePool resolution = n.init_j_CachePool_Str_Minecraft(this.itemName, mc);
         if (!resolution.init_Z()) {
            return false;
         }

         ArrayUtil target = ArrayUtil.update(resolution.init_Str());
         this.init_V_Minecraft_ArrayUtil(mc, target);
         boolean var10000;
         switch (this.condition) {
            case init:
               var10000 = init_Z_Minecraft_ArrayUtil(mc, target);
               break;
            case load:
               var10000 = init_I_Minecraft_ArrayUtil(mc, target) >= Math.max(1, this.count);
               break;
            case read:
               var10000 = init_I_Minecraft_ArrayUtil(mc, target) != this.init_I;
               break;
            case write:
               var10000 = init_I_Minecraft_ArrayUtil(mc, target) > this.init_I;
               break;
            case update:
               var10000 = init_I_Minecraft_ArrayUtil(mc, target) < this.init_I;
               break;
            case sync:
               var10000 = init_ItemStack_Minecraft_I(mc, this.slot).isEmpty();
               break;
            case flush:
               var10000 = !init_ItemStack_Minecraft_I(mc, this.slot).isEmpty();
               break;
            case close:
               var10000 = init_ItemStack_Minecraft_I(mc, this.slot).isEmpty() != this.load;
               break;
            case open:
               var10000 = load(mc);
               break;
            case poll:
               var10000 = read(mc);
               break;
            case push:
               var10000 = target.init_I_ItemStack_I(mc.player.containerMenu.getCarried(), -1) >= 0;
               break;
            case pop:
               var10000 = mc.player.containerMenu.getCarried().isEmpty();
               break;
            case tick:
               var10000 = !mc.player.containerMenu.getCarried().isEmpty();
               break;
            case run:
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
      this.init_Z = false;
   }

   private void init_V_Minecraft_ArrayUtil(Minecraft mc, ArrayUtil target) {
      if (!this.init_Z) {
         this.init_I = init_I_Minecraft_ArrayUtil(mc, target);
         this.load = init_ItemStack_Minecraft_I(mc, this.slot).isEmpty();
         this.init_Z = true;
      }
   }

   static boolean init_Z_Minecraft_ArrayUtil(Minecraft mc, ArrayUtil target) {
      return init_I_Minecraft_ArrayUtil(mc, target) > 0;
   }

   static int init_I_Minecraft_ArrayUtil(Minecraft mc, ArrayUtil target) {
      if (mc != null && mc.player != null && target != null && target.update()) {
         int total = 0;

         for (int i = 0; i < mc.player.getInventory().getContainerSize(); i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (target.init_I_ItemStack_I(stack, i) >= 0) {
               total += stack.getCount();
            }
         }

         return total;
      } else {
         return 0;
      }
   }

   private static ItemStack init_ItemStack_Minecraft_I(Minecraft mc, int slot) {
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
      p.init(tag, this);
      tag.putString("saveAs", this.saveAs);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.condition = i.init(aK.Inner_DataUtil.class, tag.getStringOr("condition", "ITEM_EXISTS"), aK.Inner_DataUtil.init);
      this.itemName = tag.getStringOr("itemName", "");
      this.count = tag.getIntOr("count", 1);
      this.slot = tag.getIntOr("slot", 0);
      this.timeoutMs = tag.getIntOr("timeoutMs", 0);
      p.load(tag, this);
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
   public String load() {
      return this.saveAs;
   }

   @Override
   public void a_(String name) {
      this.saveAs = name == null ? "" : name;
   }

   public enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run;
   }
}
