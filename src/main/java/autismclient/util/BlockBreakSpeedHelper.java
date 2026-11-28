package autismclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockBreakSpeedHelper {
   private BlockBreakSpeedHelper() {
   }

   public static float breakSpeed(Minecraft mc, ItemStack stack, BlockState state) {
      if (stack != null && !stack.isEmpty() && state != null) {
         float speed;
         try {
            speed = stack.getDestroySpeed(state);
         } catch (Throwable t) {
            return 0.0F;
         }

         if (speed <= 1.0F && stack.getItem() == Items.SHEARS) {
            String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if ("cobweb".equals(path) || path.endsWith("_leaves")) {
               speed = 15.0F;
            } else if (path.endsWith("_wool")) {
               speed = 5.0F;
            }
         }

         if (speed > 1.0F) {
            int efficiency = efficiencyLevel(mc, stack);
            if (efficiency > 0) {
               float addition = (float)efficiency * efficiency + 1.0F;
               speed += Math.max(0.0F, Math.min(1024.0F, addition));
            }
         }

         return speed;
      } else {
         return 0.0F;
      }
   }

   private static int efficiencyLevel(Minecraft mc, ItemStack stack) {
      return enchantLevel(mc, stack, Enchantments.EFFICIENCY);
   }

   public static boolean hasSilkTouch(Minecraft mc, ItemStack stack) {
      return stack != null && !stack.isEmpty() ? enchantLevel(mc, stack, Enchantments.SILK_TOUCH) > 0 : false;
   }

   private static int enchantLevel(Minecraft mc, ItemStack stack, ResourceKey<Enchantment> key) {
      try {
         if (mc != null && mc.level != null) {
            Reference<Enchantment> holder = mc.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
            return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
         } else {
            return 0;
         }
      } catch (Throwable t) {
         return 0;
      }
   }

   public static float mainHandSpeed(Minecraft mc, BlockState state) {
      return mc != null && mc.player != null ? breakSpeed(mc, mc.player.getMainHandItem(), state) : 0.0F;
   }

   public static int bestToolSlot(Minecraft mc, BlockState state, int limit, boolean ignoreDurability) {
      return bestToolSlot(mc, state, limit, ignoreDurability, false);
   }

   public static int bestToolSlot(Minecraft mc, BlockState state, int limit, boolean ignoreDurability, boolean requireSilkTouch) {
      if (mc != null && mc.player != null && state != null) {
         int best = -1;
         float bestSpeed = -1.0F;
         int selected = mc.player.getInventory().getSelectedSlot();

         for (int slot = 0; slot < limit; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty() && (ignoreDurability || !stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 2)) {
               float speed = breakSpeed(mc, stack, state);
               if (!(speed <= 1.0F) && (!requireSilkTouch || hasSilkTouch(mc, stack))) {
                  boolean better = speed > bestSpeed || speed == bestSpeed && best >= 0 && slotCost(slot, selected) < slotCost(best, selected);
                  if (better) {
                     bestSpeed = speed;
                     best = slot;
                  }
               }
            }
         }

         return best;
      } else {
         return -1;
      }
   }

   private static int slotCost(int slot, int selected) {
      return slot < 9 ? Math.abs(slot - selected) : 100 + slot;
   }

   public static boolean switchToBestTool(Minecraft mc, BlockState state, boolean considerInventory, boolean ignoreDurability) {
      return init(mc, state, considerInventory, ignoreDurability, false);
   }

   public static boolean init(Minecraft mc, BlockState state, boolean considerInventory, boolean ignoreDurability, boolean preferSilkTouch) {
      if (mc == null || mc.player == null || state == null) {
         return false;
      }

      if (mc.player.hasInfiniteMaterials()) {
         return true;
      }

      int limit = considerInventory ? 36 : 9;
      int best = -1;
      if (preferSilkTouch) {
         best = bestToolSlot(mc, state, limit, ignoreDurability, true);
      }

      if (best < 0) {
         best = bestToolSlot(mc, state, limit, ignoreDurability, false);
      }

      if (best < 0) {
         return false;
      }

      int selected = mc.player.getInventory().getSelectedSlot();
      ItemStack bestStack = mc.player.getInventory().getItem(best);
      ItemStack heldStack = mc.player.getInventory().getItem(selected);
      float bestSpeed = breakSpeed(mc, bestStack, state);
      float heldSpeed = breakSpeed(mc, heldStack, state);
      boolean silkChosen = preferSilkTouch && bestSpeed > 1.0F && hasSilkTouch(mc, bestStack);
      if (silkChosen) {
         if (heldSpeed > 1.0F && hasSilkTouch(mc, heldStack)) {
            return true;
         }
      } else if (bestSpeed <= heldSpeed) {
         return true;
      }

      if (best < 9) {
         InventorySlotHelper.selectHotbarSlot(mc, best);
         return true;
      }

      if (!InventorySlotHelper.swapPlayerSlots(mc, best, selected)) {
         return false;
      }

      InventorySlotHelper.selectHotbarSlot(mc, selected);
      return true;
   }
}
