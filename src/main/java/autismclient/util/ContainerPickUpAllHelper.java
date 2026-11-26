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
   private static int init_I = -1;
   private static int load = -1;
   private static ItemStack init_ItemStack = ItemStack.EMPTY;

   private ContainerPickUpAllHelper() {
   }

   public static boolean init(Minecraft mc) {
      return init(mc, null, 1);
   }

   public static boolean init(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard) {
      return init(mc, carriedGuard, 1);
   }

   public static boolean init(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard, int times) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null) {
         return false;
      }

      int clickCount = Math.max(1, times);
      if (mc.isSameThread()) {
         return load(mc, carriedGuard, clickCount);
      }

      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean clicked = new AtomicBoolean(false);
      mc.execute(() -> {
         try {
            clicked.set(load(mc, carriedGuard, clickCount));
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

   private static boolean load(Minecraft mc, autismclient.util.macro.MacroItemTarget carriedGuard, int times) {
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
            if (carried == null || carried.isEmpty() || !init(carriedGuard, carried)) {
               break;
            }

            int triggerSlot = init(mc, handler, carried);
            if (triggerSlot < 0) {
               break;
            }

            try {
               mc.gameMode.handleContainerInput(handler.containerId, triggerSlot, 0, ContainerInput.PICKUP_ALL, mc.player);
               init(handler);
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
               init_I = handler.containerId;
               load = slotId;
               init_ItemStack = afterCarried.copy();
            } else if (init_I == handler.containerId && load >= 0 && ItemStack.isSameItemSameComponents(init_ItemStack, afterCarried)) {
               init_ItemStack = afterCarried.copy();
            } else {
               init();
            }
         } else {
            init();
         }
      }
   }

   private static int init(Minecraft mc, AbstractContainerMenu handler, ItemStack carried) {
      if (init_I != handler.containerId || load < 0 || load >= handler.slots.size()) {
         return -1;
      } else if (!init_ItemStack.isEmpty() && ItemStack.isSameItemSameComponents(init_ItemStack, carried)) {
         Slot slot = handler.slots.get(load);
         return init(mc, slot, carried) ? load : -1;
      } else {
         return -1;
      }
   }

   private static boolean init(Minecraft mc, Slot slot, ItemStack carried) {
      if (mc != null && mc.player != null && slot != null && slot.isActive()) {
         return !slot.hasItem() ? true : !slot.mayPickup(mc.player) && !ItemStack.isSameItemSameComponents(slot.getItem(), carried);
      } else {
         return false;
      }
   }

   private static void init(AbstractContainerMenu handler) {
      if (handler != null && init_I == handler.containerId) {
         ItemStack carried = handler.getCarried();
         if (carried != null && !carried.isEmpty()) {
            init_ItemStack = carried.copy();
         } else {
            init();
         }
      }
   }

   private static void init() {
      init_I = -1;
      load = -1;
      init_ItemStack = ItemStack.EMPTY;
   }

   private static boolean init(autismclient.util.macro.MacroItemTarget target, ItemStack carried) {
      if (target != null && target.update()) {
         autismclient.util.macro.MacroItemTarget identityOnly = target.init_ArrayUtil();
         identityOnly.init_I = -1;
         return identityOnly.init_Z_ItemStack_I(carried, -1);
      } else {
         return true;
      }
   }
}
