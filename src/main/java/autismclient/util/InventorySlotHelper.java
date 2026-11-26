package autismclient.util;

import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class InventorySlotHelper {
   public static final int PLAYER_SLOT_COUNT = 41;
   public static final int CONTAINER_SLOT_BASE = 100;

   private InventorySlotHelper() {
   }

   public static void selectHotbarSlot(Minecraft mc, int slot) {
      if (!autismclient.modules.aD.read_Z()) {
         selectHotbarSlotInternal(mc, slot);
      }
   }

   public static void selectHotbarSlotForce(Minecraft mc, int slot) {
      selectHotbarSlotInternal(mc, slot);
   }

   private static void selectHotbarSlotInternal(Minecraft mc, int slot) {
      if (mc != null && mc.player != null && mc.gameMode != null && mc.getConnection() != null) {
         int clampedSlot = Math.max(0, Math.min(8, slot));
         if (mc.player.getInventory().getSelectedSlot() != clampedSlot) {
            mc.player.getInventory().setSelectedSlot(clampedSlot);
            ((AutismMultiPlayerGameModeAccessor)mc.gameMode).autism$ensureHasSentCarriedItem();
         }
      }
   }

   public static boolean swapToHotbar(Minecraft mc, int inventorySlot, int hotbarSlot) {
      return autismclient.modules.aD.read_Z() ? false : swapToHotbarInternal(mc, inventorySlot, hotbarSlot);
   }

   public static boolean swapToHotbarForce(Minecraft mc, int inventorySlot, int hotbarSlot) {
      return swapToHotbarInternal(mc, inventorySlot, hotbarSlot);
   }

   private static boolean swapToHotbarInternal(Minecraft mc, int inventorySlot, int hotbarSlot) {
      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (inventorySlot < 9 || inventorySlot >= 36 || hotbarSlot < 0 || hotbarSlot >= 9) {
         return false;
      }

      if (!mc.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      int sourceHandlerSlot = toScreenSlot(mc, inventorySlot);
      if (sourceHandlerSlot < 0) {
         return false;
      }

      mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, sourceHandlerSlot, hotbarSlot, ContainerInput.SWAP, mc.player);
      return true;
   }

   public static boolean swapToOffhand(Minecraft mc, int inventorySlot) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (inventorySlot < 0 || inventorySlot >= 36) {
         return false;
      }

      if (!mc.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      int handlerSlot = toScreenSlot(mc, inventorySlot);
      if (handlerSlot < 0) {
         return false;
      }

      mc.gameMode.handleContainerInput(mc.player.containerMenu.containerId, handlerSlot, 40, ContainerInput.SWAP, mc.player);
      return true;
   }

   public static boolean swapPlayerSlots(Minecraft mc, int fromSlot, int toSlot) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (fromSlot == toSlot) {
         return true;
      }

      if (fromSlot < 0 || fromSlot >= 41 || toSlot < 0 || toSlot >= 41) {
         return false;
      }

      if (!mc.player.containerMenu.getCarried().isEmpty()) {
         return false;
      }

      int fromScreenSlot = toScreenSlot(mc, fromSlot);
      int toScreenSlot = toScreenSlot(mc, toSlot);
      return fromScreenSlot >= 0 && toScreenSlot >= 0 ? swapScreenSlots(mc, fromScreenSlot, toScreenSlot) : false;
   }

   public static int findInventorySlot(Minecraft mc, String itemName) {
      return findInventorySlot(mc, autismclient.util.macro.MacroItemTarget.parse(itemName));
   }

   public static int findInventorySlot(Minecraft mc, autismclient.util.macro.MacroItemTarget target) {
      if (mc != null && mc.player != null && target != null && target.hasItemFilter()) {
         int bestSlot = -1;
         int bestScore = -1;

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            int score = target.matchScore(stack, slot);
            if (score > bestScore) {
               bestScore = score;
               bestSlot = slot;
            }
         }

         return bestScore >= 0 ? bestSlot : -1;
      } else {
         return -1;
      }
   }

   public static int findVisibleSlot(Minecraft mc, String itemName) {
      return findVisibleSlot(mc, autismclient.util.macro.MacroItemTarget.parse(itemName));
   }

   public static int findVisibleSlot(Minecraft mc, autismclient.util.macro.MacroItemTarget target) {
      if (mc != null && mc.player != null && target != null && target.hasItemFilter()) {
         int bestSlot = -1;
         int bestScore = -1;
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler != null) {
            for (int i = 0; i < handler.slots.size(); i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null) {
                  int visibleSlot = toVisibleSlot(mc, i);
                  int score = target.matchScore(slot.getItem(), visibleSlot);
                  if (score > bestScore) {
                     bestScore = score;
                     bestSlot = visibleSlot;
                  }
               }
            }
         }

         return bestScore >= 0 ? bestSlot : findInventorySlot(mc, target);
      } else {
         return -1;
      }
   }

   public static int ensureItemInHotbar(Minecraft mc, String itemName, int preferredHotbarSlot) {
      return ensureItemInHotbar(mc, autismclient.util.macro.MacroItemTarget.parse(itemName), preferredHotbarSlot);
   }

   public static int ensureItemInHotbar(Minecraft mc, autismclient.util.macro.MacroItemTarget target, int preferredHotbarSlot) {
      if (mc != null && mc.player != null && target != null && target.hasItemFilter()) {
         int bestHotbarSlot = -1;
         int bestHotbarScore = -1;

         for (int slot = 0; slot < 9; slot++) {
            int score = target.matchScore(mc.player.getInventory().getItem(slot), slot);
            if (score > bestHotbarScore) {
               bestHotbarScore = score;
               bestHotbarSlot = slot;
            }
         }

         if (bestHotbarScore >= 0) {
            selectHotbarSlot(mc, bestHotbarSlot);
            return bestHotbarSlot;
         }

         int bestInventorySlot = -1;
         int bestInventoryScore = -1;

         for (int slot = 9; slot < 36; slot++) {
            int score = target.matchScore(mc.player.getInventory().getItem(slot), slot);
            if (score > bestInventoryScore) {
               bestInventoryScore = score;
               bestInventorySlot = slot;
            }
         }

         if (bestInventoryScore < 0) {
            return -1;
         }

         int targetHotbarSlot = Math.max(0, Math.min(8, preferredHotbarSlot));
         if (!swapToHotbar(mc, bestInventorySlot, targetHotbarSlot)) {
            return -1;
         }

         selectHotbarSlot(mc, targetHotbarSlot);
         return targetHotbarSlot;
      } else {
         return -1;
      }
   }

   public static int toVisibleSlot(Minecraft mc, int handlerSlotId) {
      if (mc != null && mc.player != null && mc.player.containerMenu != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handlerSlotId >= 0 && handlerSlotId < handler.slots.size()) {
            Slot slot = handler.slots.get(handlerSlotId);
            int playerSlot = playerInventoryIndex(mc, slot);
            if (playerSlot >= 0) {
               return playerSlot;
            }

            int extraOrdinal = 0;

            for (int i = 0; i < handler.slots.size(); i++) {
               Slot currentSlot = handler.slots.get(i);
               if (currentSlot != null && !isPlayerInventorySlot(mc, currentSlot)) {
                  if (i == handlerSlotId) {
                     return 100 + extraOrdinal;
                  }

                  extraOrdinal++;
               }
            }

            return handlerSlotId;
         } else {
            return handlerSlotId;
         }
      } else {
         return handlerSlotId;
      }
   }

   public static int toVisibleSlot(Minecraft mc, Slot slot) {
      int handlerSlotId = findSlotIndex(mc, slot);
      return handlerSlotId >= 0 ? toVisibleSlot(mc, handlerSlotId) : -1;
   }

   public static int toScreenSlot(Minecraft mc, int userVisibleSlot) {
      if (mc != null && mc.player != null && mc.player.containerMenu != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (userVisibleSlot >= 0 && userVisibleSlot < mc.player.getInventory().getContainerSize()) {
            for (int i = 0; i < handler.slots.size(); i++) {
               if (playerInventoryIndex(mc, handler.slots.get(i)) == userVisibleSlot) {
                  return i;
               }
            }
         }

         if (userVisibleSlot >= 100) {
            int extraOrdinal = userVisibleSlot - 100;

            for (int i = 0; i < handler.slots.size(); i++) {
               Slot slot = handler.slots.get(i);
               if (slot != null && !isPlayerInventorySlot(mc, slot)) {
                  if (extraOrdinal == 0) {
                     return i;
                  }

                  extraOrdinal--;
               }
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static int resolveConfiguredSlot(Minecraft mc, int configuredSlot) {
      return toScreenSlot(mc, configuredSlot);
   }

   public static boolean isPlayerInventorySlot(Minecraft mc, Slot slot) {
      return playerInventoryIndex(mc, slot) >= 0;
   }

   public static int findSlotIndex(Minecraft mc, Slot targetSlot) {
      if (mc != null && mc.player != null && mc.player.containerMenu != null && targetSlot != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;

         for (int i = 0; i < handler.slots.size(); i++) {
            if (handler.slots.get(i) == targetSlot) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   public static boolean swapScreenSlots(Minecraft mc, int fromScreenSlot, int toScreenSlot) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null || mc.player == null || mc.gameMode == null) {
         return false;
      }

      if (fromScreenSlot == toScreenSlot) {
         return true;
      }

      AbstractContainerMenu handler = mc.player.containerMenu;
      if (handler == null) {
         return false;
      }

      if (fromScreenSlot < 0 || fromScreenSlot >= handler.slots.size()) {
         return false;
      }

      if (toScreenSlot < 0 || toScreenSlot >= handler.slots.size()) {
         return false;
      }

      if (!handler.getCarried().isEmpty()) {
         return false;
      }

      int syncId = handler.containerId;
      mc.gameMode.handleContainerInput(syncId, fromScreenSlot, 0, ContainerInput.PICKUP, mc.player);
      mc.gameMode.handleContainerInput(syncId, toScreenSlot, 0, ContainerInput.PICKUP, mc.player);
      mc.gameMode.handleContainerInput(syncId, fromScreenSlot, 0, ContainerInput.PICKUP, mc.player);
      return true;
   }

   private static int playerInventoryIndex(Minecraft mc, Slot slot) {
      if (mc != null && mc.player != null && slot != null && slot.container == mc.player.getInventory()) {
         int index = slot.getContainerSlot();
         return index >= 0 && index < mc.player.getInventory().getContainerSize() ? index : -1;
      } else {
         return -1;
      }
   }
}
