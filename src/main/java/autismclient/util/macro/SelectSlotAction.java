package autismclient.util.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class SelectSlotAction implements PacketOrderAware, MacroAction {
   public int slot = 0;
   public String itemName = "";
   public MacroItemTarget itemTarget = new MacroItemTarget();
   public SelectSlotAction.Inner_DataUtil strategy = SelectSlotAction.Inner_DataUtil.FIRST_MATCH;
   public String outputVariable = "";
   public volatile PacketOrderMode packetOrder = PacketOrderMode.INSTANT;

   @Override
   public void execute(Minecraft mc) {
      if (mc.player != null) {
         List<MacroItemTarget> targets = new ArrayList<>();

         for (MacroItemTarget candidate : this.init_List_7()) {
            MacroItemTarget resolved = candidate.init_ArrayUtil_Minecraft(mc);
            if (resolved != null) {
               targets.add(resolved);
            }
         }

         if (this.strategy == SelectSlotAction.Inner_DataUtil.EMPTY_SLOT) {
            int empty = this.init_I_Minecraft(mc);
            if (empty >= 0) {
               autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, empty);
               MacroVariableContext.init_V_Str_Obj(this.outputVariable, empty);
               return;
            }
         }

         if (!targets.isEmpty()) {
            if (this.strategy != SelectSlotAction.Inner_DataUtil.FIRST_MATCH) {
               int best = this.init_I_Minecraft_List(mc, targets);
               if (best >= 0) {
                  autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, best);
                  MacroVariableContext.init_V_Str_Obj(this.outputVariable, best);
                  return;
               }
            }

            for (MacroItemTarget target : targets) {
               int selectedSlot = autismclient.util.InventorySlotHelper.ensureItemInHotbar(mc, target, this.slot);
               if (selectedSlot >= 0) {
                  MacroVariableContext.init_V_Str_Obj(this.outputVariable, selectedSlot);
                  return;
               }
            }
         }

         int actualSlot = Math.max(0, Math.min(8, this.slot));
         autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, actualSlot);
         MacroVariableContext.init_V_Str_Obj(this.outputVariable, actualSlot);
      }
   }

   @Override
   public PacketOrderMode init_D() {
      return this.packetOrder;
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.SELECT_SLOT;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", "SELECT_SLOT");
      tag.putInt("slot", this.slot);
      tag.putString("strategy", this.strategy.name());
      tag.putString("outputVariable", this.outputVariable);
      MacroItemTarget target = this.init_ArrayUtil_2();
      if (target.init_Z() || target.update()) {
         tag.put("itemName", target.init_CompoundTag());
      }

      PacketOrderAware.init_V_CompoundTag_D(tag, this.packetOrder);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.slot = tag.getIntOr("slot", 0);
      this.strategy = MacroNbtCodecHelper.init(SelectSlotAction.Inner_DataUtil.class, tag.getStringOr("strategy", "FIRST_MATCH"), SelectSlotAction.Inner_DataUtil.FIRST_MATCH);
      this.outputVariable = tag.getStringOr("outputVariable", "");
      this.itemTarget = tag.getCompound("itemName")
         .map(MacroItemTarget::init_ArrayUtil_CompoundTag)
         .orElseGet(() -> MacroItemTarget.update(tag.getStringOr("itemName", "")));
      this.itemName = this.itemTarget.write_Str();
      this.packetOrder = PacketOrderAware.init_D_CompoundTag(tag);
   }

   @Override
   public String getDisplayName() {
      MacroItemTarget target = this.init_ArrayUtil_2();
      return !target.init_Z() && !target.update() ? "Select Slot " + (this.slot + 1) : "Select " + this.strategy + " \"" + target.load_Str() + "\"";
   }

   @Override
   public String getIcon() {
      return "S";
   }

   private MacroItemTarget init_ArrayUtil_2() {
      if (this.itemTarget != null && (this.itemTarget.init_Z() || this.itemTarget.update())) {
         return this.itemTarget;
      }

      this.itemTarget = MacroItemTarget.update(this.init_Str_Str_6(this.itemName));
      return this.itemTarget;
   }

   private String init_Str_Str_6(String raw) {
      String key = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
      String var10000;
      switch (key) {
         case "bundle":
            var10000 = "minecraft:bundle";
            break;
         case "writable book":
         case "book":
            var10000 = "minecraft:writable_book";
            break;
         case "pickaxe":
            var10000 = "#minecraft:pickaxes";
            break;
         case "trident or bow":
            var10000 = "minecraft:trident|minecraft:bow";
            break;
         case "chest boat/minecart":
         case "chest boat":
            var10000 = "minecraft:chest_minecart";
            break;
         default:
            var10000 = raw;
      }

      return var10000;
   }

   private int init_I_Minecraft(Minecraft mc) {
      for (int i = 0; i < 9; i++) {
         if (mc.player.getInventory().getItem(i).isEmpty()) {
            return i;
         }
      }

      return -1;
   }

   private List<MacroItemTarget> init_List_7() {
      String raw = this.init_Str_Str_6(this.itemName);
      if (raw != null && !raw.isBlank()) {
         if (!raw.contains("|")) {
            MacroItemTarget target = this.init_ArrayUtil_2();
            return !target.init_Z() && !target.update() ? List.of() : List.of(target);
         }

         ArrayList<MacroItemTarget> out = new ArrayList<>();

         for (String part : raw.split("\\|")) {
            MacroItemTarget target = MacroItemTarget.update(part.trim());
            if (target.init_Z() || target.update()) {
               out.add(target);
            }
         }

         return out;
      } else {
         MacroItemTarget target = this.init_ArrayUtil_2();
         return !target.init_Z() && !target.update() ? List.of() : List.of(target);
      }
   }

   private int init_I_Minecraft_List(Minecraft mc, List<MacroItemTarget> targets) {
      int bestSlot = -1;
      int bestMetric = this.strategy == SelectSlotAction.Inner_DataUtil.WORST_DURABILITY ? 2147483647 : -2147483648;

      for (int i = 0; i < 9; i++) {
         ItemStack stack = mc.player.getInventory().getItem(i);
         boolean matches = false;

         for (MacroItemTarget target : targets) {
            if (target.init_I_ItemStack_I(stack, i) >= 0) {
               matches = true;
               break;
            }
         }

         if (matches) {
            int var10000;
            switch (this.strategy) {
               case BEST_DURABILITY:
                  var10000 = stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : 2147483647;
                  break;
               case WORST_DURABILITY:
                  var10000 = stack.isDamageableItem() ? stack.getMaxDamage() - stack.getDamageValue() : 2147483647;
                  break;
               case LARGEST_STACK:
                  var10000 = stack.getCount();
                  break;
               default:
                  var10000 = 0;
            }

            int metric = var10000;
            if (this.strategy == SelectSlotAction.Inner_DataUtil.WORST_DURABILITY && metric < bestMetric || this.strategy != SelectSlotAction.Inner_DataUtil.WORST_DURABILITY && metric > bestMetric) {
               bestMetric = metric;
               bestSlot = i;
            }
         }
      }

      return bestSlot;
   }

   public enum Inner_DataUtil {
      FIRST_MATCH,
      BEST_DURABILITY,
      WORST_DURABILITY,
      LARGEST_STACK,
      EMPTY_SLOT;
   }
}
