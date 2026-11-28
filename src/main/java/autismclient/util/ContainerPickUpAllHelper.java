package autismclient.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class ContainerPickUpAllHelper {
   private static int trackedContainerId = -1;
   private static int trackedSlot = -1;
   private static ItemStack trackedCarried = ItemStack.EMPTY;

   private ContainerPickUpAllHelper() {
   }

   public static boolean pickUpAll(Minecraft mc) {
      return pickUpAll(mc, null, 1);
   }

   public static boolean pickUpAll(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard) {
      return pickUpAll(mc, carriedGuard, 1);
   }

   public static boolean pickUpAll(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard, int times) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null) {
         return false;
      }

      int clickCount = Math.max(1, times);
      if (mc.isSameThread()) {
         return clickPickUpAll(mc, carriedGuard, clickCount);
      }

      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean clicked = new AtomicBoolean(false);
      mc.execute(() -> {
         try {
            clicked.set(clickPickUpAll(mc, carriedGuard, clickCount));
         } finally {
            latch.countDown();
         }
      });

      try {
         if (!latch.await(2L, TimeUnit.SECONDS)) {
            return false;
         }
      } catch (InterruptedException e) {
         Thread.currentThread().interrupt();
         return false;
      }

      return clicked.get();
   }

   private static boolean clickPickUpAll(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard, int times) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc != null && mc.player != null && mc.gameMode != null && mc.getConnection() != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler == null) {
            return false;
         }

         boolean clickedAny = false;

         for (int i = 0; i < times; i++) {
            ItemStack carried = handler.getCarried();
            if (carried == null || carried.isEmpty() || !carriedMatchesGuard(carriedGuard, carried)) {
               break;
            }

            int triggerSlot = resolveTrackedSlot(mc, handler, carried);
            if (triggerSlot < 0) {
               break;
            }

            try {
               mc.gameMode.handleContainerInput(handler.containerId, triggerSlot, 0, ContainerInput.PICKUP_ALL, mc.player);
               refreshTrackedCarried(handler);
               clickedAny = true;
            } catch (RuntimeException ignored) {
               break;
            }
         }

         return clickedAny;
      } else {
         return false;
      }
   }

   public static void init(
      Minecraft mc, AbstractContainerMenu handler, int slotId, int button, ContainerInput input, ItemStack beforeCarried, ItemStack beforeSlot
   ) {
      if (mc != null && mc.player != null && handler != null && input != null) {
         ItemStack afterCarried = handler.getCarried();
         if (afterCarried != null && !afterCarried.isEmpty()) {
            if (input == ContainerInput.PICKUP
               && slotId >= 0
               && slotId < handler.slots.size()
               && (button == 0 || button == 1)
               && (beforeCarried == null || beforeCarried.isEmpty())
               && beforeSlot != null
               && !beforeSlot.isEmpty()
               && ItemStack.isSameItemSameComponents(beforeSlot, afterCarried)) {
               trackedContainerId = handler.containerId;
               trackedSlot = slotId;
               trackedCarried = afterCarried.copy();
            } else if (trackedContainerId == handler.containerId && trackedSlot >= 0 && ItemStack.isSameItemSameComponents(trackedCarried, afterCarried)) {
               trackedCarried = afterCarried.copy();
            } else {
               clearTracking();
            }
         } else {
            clearTracking();
         }
      }
   }

   private static int resolveTrackedSlot(Minecraft mc, AbstractContainerMenu handler, ItemStack carried) {
      if (trackedContainerId != handler.containerId || trackedSlot < 0 || trackedSlot >= handler.slots.size()) {
         return -1;
      } else if (!trackedCarried.isEmpty() && ItemStack.isSameItemSameComponents(trackedCarried, carried)) {
         Slot slot = handler.slots.get(trackedSlot);
         return slotExhausted(mc, slot, carried) ? trackedSlot : -1;
      } else {
         return -1;
      }
   }

   private static boolean slotExhausted(Minecraft mc, Slot slot, ItemStack carried) {
      if (mc != null && mc.player != null && slot != null && slot.isActive()) {
         return !slot.hasItem() ? true : !slot.mayPickup(mc.player) && !ItemStack.isSameItemSameComponents(slot.getItem(), carried);
      } else {
         return false;
      }
   }

   private static void refreshTrackedCarried(AbstractContainerMenu handler) {
      if (handler != null && trackedContainerId == handler.containerId) {
         ItemStack carried = handler.getCarried();
         if (carried != null && !carried.isEmpty()) {
            trackedCarried = carried.copy();
         } else {
            clearTracking();
         }
      }
   }

   private static void clearTracking() {
      trackedContainerId = -1;
      trackedSlot = -1;
      trackedCarried = ItemStack.EMPTY;
   }

   private static boolean carriedMatchesGuard(autismclient.util.macro.MacroItemTarget target, ItemStack carried) {
      if (target != null && target.hasItemFilter()) {
         autismclient.util.macro.MacroItemTarget identityOnly = target.copy();
         identityOnly.slot = -1;
         return identityOnly.matches(carried, -1);
      } else {
         return true;
      }
   }
}
