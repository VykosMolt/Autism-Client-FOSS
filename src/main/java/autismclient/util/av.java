package autismclient.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class av {
   private av() {
   }

   public static boolean init(Minecraft mc, int handlerSlotId, int button, ContainerInput input) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc == null) {
         return false;
      }

      if (mc.isSameThread()) {
         return load(mc, handlerSlotId, button, input);
      }

      CountDownLatch latch = new CountDownLatch(1);
      AtomicBoolean clicked = new AtomicBoolean(false);
      mc.execute(() -> {
         try {
            clicked.set(load(mc, handlerSlotId, button, input));
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

   private static boolean load(Minecraft mc, int handlerSlotId, int button, ContainerInput input) {
      if (autismclient.modules.aD.read_Z()) {
         return false;
      }

      if (mc != null && mc.player != null && mc.gameMode != null && mc.getConnection() != null && input != null) {
         AbstractContainerMenu handler = mc.player.containerMenu;
         if (handler != null && handlerSlotId >= 0 && handlerSlotId < handler.slots.size()) {
            ItemStack beforeCarried = handler.getCarried().copy();
            ItemStack beforeSlot = handler.slots.get(handlerSlotId).getItem().copy();

            try {
               mc.gameMode.handleContainerInput(handler.containerId, handlerSlotId, button, input, mc.player);
               K.init(mc, handler, handlerSlotId, button, input, beforeCarried, beforeSlot);
               return true;
            } catch (RuntimeException ignored) {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}
