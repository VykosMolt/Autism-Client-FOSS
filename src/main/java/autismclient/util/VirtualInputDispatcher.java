package autismclient.util;

import autismclient.mixin.accessor.AutismKeyMappingAccessor;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class VirtualInputDispatcher {
   private static final Minecraft MC = Minecraft.getInstance();
   private static boolean attackQueued;
   private static boolean useQueued;
   private static GuardedInputTicket pendingTicket;
   private static GuardedInputTicket activeTicket;
   private static VirtualInputDispatcher.InputOwner requestedOwner = VirtualInputDispatcher.InputOwner.NONE;
   private static long requestedGeneration;
   private static int pendingSlot = -1;
   private static int pendingSlotFrom = -1;
   private static boolean attackPressed;
   private static boolean usePressed;
   private static VirtualInputDispatcher.InputOwner dispatchedOwner = VirtualInputDispatcher.InputOwner.NONE;
   private static VirtualInputDispatcher.InputOwner consumedOwner = VirtualInputDispatcher.InputOwner.NONE;
   private static long dispatchedGeneration;
   private static long consumedGeneration;
   private static KeyMapping mapping;
   private static boolean attackHeld;
   private static boolean useHeld;

   private VirtualInputDispatcher() {
   }

   public static void queueAttack() {
      if (!autismclient.modules.aD.read_Z()) {
         attackQueued = true;
      }
   }

   public static void queueUse() {
      if (!autismclient.modules.aD.read_Z()) {
         useQueued = true;
      }
   }

   public static boolean init(String owner, BooleanSupplier guard, Consumer<Boolean> receipt) {
      if (owner != null && guard != null && receipt != null && !autismclient.modules.aD.read_Z() && pendingTicket == null && activeTicket == null) {
         pendingTicket = new GuardedInputTicket(owner, guard, receipt);
         return true;
      } else {
         return false;
      }
   }

   public static boolean isTicketOwner(String owner) {
      return activeTicket != null && activeTicket.owner.equals(owner);
   }

   public static void markTicketSent(String owner) {
      if (isTicketOwner(owner)) {
         activeTicket.markSent();
      }
   }

   public static void load(String owner) {
      if (pendingTicket != null && pendingTicket.owner.equals(owner)) {
         GuardedInputTicket old = pendingTicket;
         pendingTicket = null;
         old.fail();
      }
   }

   private static void finishActiveTicket() {
      GuardedInputTicket old = activeTicket;
      activeTicket = null;
      if (old != null) {
         old.resolveDispatched();
      }
   }

   private static void dropPendingTicket() {
      GuardedInputTicket old = pendingTicket;
      pendingTicket = null;
      if (old != null) {
         old.fail();
      }
   }

   public static void requestScaffold(long generation) {
      if (generation > 0L && !autismclient.modules.aD.read_Z()) {
         requestedGeneration = generation;
         requestOwner(VirtualInputDispatcher.InputOwner.SCAFFOLD);
      }
   }

   public static void requestFastExp() {
      requestOwner(VirtualInputDispatcher.InputOwner.FAST_EXP);
   }

   public static void requestFastBlock() {
      requestOwner(VirtualInputDispatcher.InputOwner.FAST_BLOCK);
   }

   public static boolean consumeFastExp() {
      return consumeOwner(VirtualInputDispatcher.InputOwner.FAST_EXP);
   }

   public static boolean consumeFastBlock() {
      return consumeOwner(VirtualInputDispatcher.InputOwner.FAST_BLOCK);
   }

   public static boolean consumeScaffold(long generation) {
      if (generation <= 0L || dispatchedGeneration != generation) {
         return false;
      }

      if (!consumeOwner(VirtualInputDispatcher.InputOwner.SCAFFOLD)) {
         return false;
      }

      consumedGeneration = generation;
      dispatchedGeneration = 0L;
      return true;
   }

   public static boolean isScaffoldConsumed() {
      return consumedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD;
   }

   public static long init_J() {
      return isScaffoldConsumed() ? consumedGeneration : 0L;
   }

   public static void update_V() {
      if (requestedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD
         || dispatchedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD
         || consumedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD
         || requestedGeneration != 0L
         || dispatchedGeneration != 0L
         || consumedGeneration != 0L) {
         releaseOwner(VirtualInputDispatcher.InputOwner.SCAFFOLD);
         requestedGeneration = 0L;
         dispatchedGeneration = 0L;
         consumedGeneration = 0L;
      }
   }

   public static boolean isFastExpConsumed() {
      return consumedOwner == VirtualInputDispatcher.InputOwner.FAST_EXP;
   }

   public static boolean hasFastRequest() {
      return dispatchedOwner == VirtualInputDispatcher.InputOwner.FAST_EXP || dispatchedOwner == VirtualInputDispatcher.InputOwner.FAST_BLOCK;
   }

   public static void releaseFastExp() {
      releaseOwner(VirtualInputDispatcher.InputOwner.FAST_EXP);
   }

   public static void releaseFastBlock() {
      releaseOwner(VirtualInputDispatcher.InputOwner.FAST_BLOCK);
   }

   private static void requestOwner(VirtualInputDispatcher.InputOwner owner) {
      if (!autismclient.modules.aD.read_Z() && owner != null && owner != VirtualInputDispatcher.InputOwner.NONE) {
         requestedOwner = owner;
      }
   }

   static boolean plainWins(boolean plainQueued, boolean scaffoldQueued) {
      return plainQueued && !scaffoldQueued;
   }

   private static boolean consumeOwner(VirtualInputDispatcher.InputOwner owner) {
      if (dispatchedOwner != owner) {
         return false;
      }

      dispatchedOwner = VirtualInputDispatcher.InputOwner.NONE;
      consumedOwner = owner;
      return true;
   }

   private static void releaseOwner(VirtualInputDispatcher.InputOwner owner) {
      if (requestedOwner == owner) {
         requestedOwner = VirtualInputDispatcher.InputOwner.NONE;
      }

      if (dispatchedOwner == owner && MC != null && MC.options != null) {
         simulatePress(MC.options.keyUse, false);
         usePressed = false;
      }

      if (dispatchedOwner == owner) {
         dispatchedOwner = VirtualInputDispatcher.InputOwner.NONE;
      }

      if (consumedOwner == owner) {
         consumedOwner = VirtualInputDispatcher.InputOwner.NONE;
      }

      if (owner == VirtualInputDispatcher.InputOwner.SCAFFOLD) {
         requestedGeneration = 0L;
         dispatchedGeneration = 0L;
         consumedGeneration = 0L;
      }

      if (MC != null && MC.options != null && MC.options.keyUse != null) {
         KeyMappingControl.of(MC.options.keyUse).autism$resetPressedState();
      }
   }

   public static void setAttackHeld(boolean held) {
      attackHeld = applyHold(MC != null && MC.options != null ? MC.options.keyAttack : null, attackHeld, held);
   }

   public static boolean isHeld(boolean use) {
      return use ? useHeld : attackHeld;
   }

   public static void setUseHeld(boolean held) {
      useHeld = applyHold(MC != null && MC.options != null ? MC.options.keyUse : null, useHeld, held);
   }

   private static boolean applyHold(KeyMapping mapping, boolean current, boolean held) {
      if (mapping == null) {
         return false;
      }

      if (held && !mapping.isUnbound() && !autismclient.modules.aD.read_Z() && canDispatch()) {
         if (!mapping.isDown()) {
            simulatePress(mapping, true);
         }

         return true;
      } else {
         if (current) {
            if (canDispatch()) {
               simulatePress(mapping, false);
            }

            KeyMappingControl.of(mapping).autism$resetPressedState();
         }

         return false;
      }
   }

   private static void releaseHolds() {
      setAttackHeld(false);
      setUseHeld(false);
   }

   public static void setSlot(int slot) {
      if (!autismclient.modules.aD.read_Z()) {
         pendingSlot = Math.max(0, Math.min(8, slot));
         pendingSlotFrom = -1;
      }
   }

   public static void requestSlot(int slot, int from) {
      if (!autismclient.modules.aD.read_Z()) {
         pendingSlot = Math.max(0, Math.min(8, slot));
         pendingSlotFrom = from;
      }
   }

   public static void close() {
      if (!canDispatch()) {
         resetForState();
      } else {
         boolean combatOwnsUse = ClientInteractionOverride.ownsUseInput();
         boolean combatOwnsAttack = ClientInteractionOverride.ownsAttackInput();
         if (pendingTicket != null && !pendingTicket.isValid()) {
            dropPendingTicket();
         }

         if (attackQueued && !combatOwnsAttack) {
            simulatePress(MC.options.keyAttack, true);
            attackPressed = true;
         }

         if (!combatOwnsAttack) {
            attackQueued = false;
         }

         if (pendingTicket != null && !combatOwnsUse && !useQueued && requestedOwner == VirtualInputDispatcher.InputOwner.NONE) {
            boolean physicalUse = MC.options.keyUse.isDown() || ((AutismKeyMappingAccessor)MC.options.keyUse).autism$clickCount() > 0;
            if (physicalUse) {
               dropPendingTicket();
            } else {
               activeTicket = pendingTicket;
               pendingTicket = null;
               simulatePress(MC.options.keyUse, true);
               usePressed = true;
            }
         }

         if (!combatOwnsUse && (useQueued || requestedOwner != VirtualInputDispatcher.InputOwner.NONE)) {
            boolean plainWinsTick = requestedOwner != VirtualInputDispatcher.InputOwner.NONE && plainWins(useQueued, requestedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD);
            dispatchedOwner = plainWinsTick ? VirtualInputDispatcher.InputOwner.NONE : requestedOwner;
            dispatchedGeneration = dispatchedOwner == VirtualInputDispatcher.InputOwner.SCAFFOLD ? requestedGeneration : 0L;
            if (dispatchedOwner != VirtualInputDispatcher.InputOwner.NONE) {
               while (MC.options.keyUse.consumeClick()) {
               }
            }

            simulatePress(MC.options.keyUse, true);
            usePressed = true;
         }

         if (!AutismLiteVariant.enabled() && autismclient.modules.BedNukerModule.init_Z_23()) {
            pendingSlot = -1;
            pendingSlotFrom = -1;
         }

         if (pendingSlot >= 0
            && MC.options.keyHotbarSlots != null
            && pendingSlot < MC.options.keyHotbarSlots.length
            && (pendingSlotFrom < 0 || MC.player.getInventory().getSelectedSlot() == pendingSlotFrom)) {
            mapping = MC.options.keyHotbarSlots[pendingSlot];
            simulatePress(mapping, true);
         }

         if (!combatOwnsUse) {
            useQueued = false;
            if (dispatchedOwner != VirtualInputDispatcher.InputOwner.NONE || requestedOwner == VirtualInputDispatcher.InputOwner.NONE) {
               requestedOwner = VirtualInputDispatcher.InputOwner.NONE;
               requestedGeneration = 0L;
            }
         }

         pendingSlot = -1;
         pendingSlotFrom = -1;
      }
   }

   public static void open() {
      if (!canDispatch()) {
         resetForState();
      }
   }

   public static void poll() {
      finishActiveTicket();
      if (MC != null && MC.options != null) {
         if (attackPressed) {
            simulatePress(MC.options.keyAttack, false);
            attackPressed = false;
         }

         if (usePressed) {
            simulatePress(MC.options.keyUse, false);
            usePressed = false;
         }

         dispatchedOwner = VirtualInputDispatcher.InputOwner.NONE;
         consumedOwner = VirtualInputDispatcher.InputOwner.NONE;
         dispatchedGeneration = 0L;
         consumedGeneration = 0L;
         if (mapping != null) {
            simulatePress(mapping, false);
            mapping = null;
         }
      } else {
         attackPressed = false;
         usePressed = false;
         dispatchedOwner = VirtualInputDispatcher.InputOwner.NONE;
         consumedOwner = VirtualInputDispatcher.InputOwner.NONE;
         dispatchedGeneration = 0L;
         consumedGeneration = 0L;
      }
   }

   private static void resetForState() {
      if (isScreenBlocked()) {
         pop();
      } else {
         push();
      }
   }

   private static boolean isScreenBlocked() {
      return MC == null
         || MC.player == null
         || MC.level == null
         || MC.options == null
         || MC.getWindow() == null
         || MC.gui.screen() != null
         || MC.gui.overlay() != null;
   }

   public static void push() {
      dropPendingTicket();
      attackQueued = false;
      useQueued = false;
      requestedOwner = VirtualInputDispatcher.InputOwner.NONE;
      requestedGeneration = 0L;
      dispatchedGeneration = 0L;
      consumedGeneration = 0L;
      pendingSlot = -1;
      pendingSlotFrom = -1;
      poll();
      releaseHolds();
   }

   public static void pop() {
      dropPendingTicket();
      attackQueued = false;
      useQueued = false;
      requestedOwner = VirtualInputDispatcher.InputOwner.NONE;
      requestedGeneration = 0L;
      dispatchedGeneration = 0L;
      consumedGeneration = 0L;
      pendingSlot = -1;
      pendingSlotFrom = -1;
      poll();
      releaseHolds();
      drainClicks();
   }

   private static void drainClicks() {
      if (MC != null && MC.options != null) {
         drainKeyClicks(MC.options.keyUse);
         drainKeyClicks(MC.options.keyAttack);
      }
   }

   private static void drainKeyClicks(KeyMapping mapping) {
      if (mapping != null) {
         while (mapping.consumeClick()) {
         }

         KeyMappingControl.of(mapping).autism$resetPressedState();
      }
   }

   private static boolean canDispatch() {
      return MC != null
         && MC.player != null
         && MC.level != null
         && MC.options != null
         && MC.getWindow() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !autismclient.modules.aD.init_Z();
   }

   private static void simulatePress(KeyMapping mapping, boolean pressed) {
      if (mapping != null) {
         KeyMappingControl.of(mapping).autism$simulatePress(pressed);
      }
   }

   private enum InputOwner {
      NONE,
      FAST_BLOCK,
      FAST_EXP,
      SCAFFOLD;
   }
}
