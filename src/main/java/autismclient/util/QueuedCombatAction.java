package autismclient.util;

import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.modules.Module;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public final class QueuedCombatAction {
   private static QueuedCombatAction.PendingAction init;
   private static QueuedCombatAction.PendingAction load;

   private QueuedCombatAction() {
   }

   public static boolean init(String owner, BlockHitResult hit, InteractionHand hand, BooleanSupplier legal, Runnable dispatched) {
      if (init == null && load == null) {
         QueuedCombatAction.PendingAction action = new QueuedCombatAction.PendingAction(owner, hit, hand, null, legal, dispatched);
         if (!ClientInteractionOverride.queueUseBlock(hit, hand)) {
            return false;
         }

         init = action;
         return true;
      } else {
         return false;
      }
   }

   public static boolean init(String owner, EntityHitResult hit, BooleanSupplier legal, Runnable dispatched) {
      if (init == null && load == null) {
         QueuedCombatAction.PendingAction action = new QueuedCombatAction.PendingAction(owner, null, null, hit.getEntity(), legal, dispatched);
         if (!ClientInteractionOverride.queueAttackEntity(owner, hit)) {
            return false;
         }

         init = action;
         return true;
      } else {
         return false;
      }
   }

   static void init_V() {
      load = init;
      init = null;
   }

   static boolean init_Z() {
      return load == null || load.load();
   }

   public static boolean init(String owner, BlockPos support) {
      return load != null
         && load.owner.equals(owner)
         && load.hit != null
         && load.hit.getBlockPos().equals(support)
         && load.gate.ready
         && load.init();
   }

   static void load() {
      if (init != null) {
         init.gate.load();
      }

      if (load != null) {
         load.gate.load();
      }

      load = null;
      init = null;
   }

   public static void init(String owner) {
      if (init != null && init.owner.equals(owner) || load != null && load.owner.equals(owner)) {
         ClientInteractionOverride.clearAll();
      }
   }

   public static void init(Packet<?> packet) {
      QueuedCombatAction.PendingAction action = load;
      if (action != null) {
         if (packet instanceof ServerboundUseItemOnPacket use
            && action.hit != null
            && action.hand == use.hand()
            && action.hit.getBlockPos().equals(use.hitResult().getBlockPos())
            && action.hit.getDirection() == use.hitResult().getDirection()
            && action.hit.getLocation().distanceToSqr(use.hitResult().getLocation()) < 1.0E-10) {
            boolean wasCommitted = action.gate.committed;
            action.gate.dispatchIfReady();
            if (!wasCommitted && action.gate.committed) {
               ((AutismLocalPlayerAccessor)action.client.player).autism$setPositionReminder(20);
            }
         } else if (packet instanceof ServerboundAttackPacket attack
            && action.entity != null
            && action.entity.getId() == attack.entityId()
            && action.client.level.getEntity(attack.entityId()) == action.entity) {
            action.gate.dispatchIfReady();
         }
      }
   }

   private static final class PendingAction {
      final String owner;
      final Minecraft client = Minecraft.getInstance();
      final Object playerRef = this.client.player;
      final Object levelRef = this.client.level;
      final Object read = this.client.getConnection();
      final int queuedTick = AutismSessionState.getInstance().getClientTick();
      final int selectedSlot = this.client.player.getInventory().getSelectedSlot();
      final ItemStack mainHand = this.client.player.getMainHandItem().copy();
      final ItemStack offHand = this.client.player.getOffhandItem().copy();
      final BlockHitResult hit;
      final InteractionHand hand;
      final Entity entity;
      final QueuedCombatAction.DispatchGate gate;

      PendingAction(String owner, BlockHitResult hit, InteractionHand hand, Entity entity, BooleanSupplier legal, Runnable dispatched) {
         this.owner = owner;
         this.hit = hit;
         this.hand = hand;
         this.entity = entity;
         this.gate = new QueuedCombatAction.DispatchGate(this::init, () -> this.read() && legal.getAsBoolean(), dispatched);
      }

      boolean init() {
         Module module = autismclient.modules.al.init_Module_Str(this.owner);
         return QueuedCombatAction.load == this
            && module != null
            && module.isEnabled()
            && this.client.player == this.playerRef
            && this.client.level == this.levelRef
            && this.client.getConnection() == this.read
            && this.queuedTick == AutismSessionState.getInstance().getClientTick()
            && this.client.gui.screen() == null
            && this.client.gui.overlay() == null;
      }

      boolean load() {
         return this.gate.checkReady();
      }

      boolean read() {
         return !this.client.player.isDeadOrDying()
            && !this.client.player.isUsingItem()
            && this.selectedSlot == this.client.player.getInventory().getSelectedSlot()
            && ItemStack.isSameItemSameComponents(this.mainHand, this.client.player.getMainHandItem())
            && ItemStack.isSameItemSameComponents(this.offHand, this.client.player.getOffhandItem());
      }
   }

   static final class DispatchGate {
      final BooleanSupplier sessionOk;
      final BooleanSupplier inputOk;
      final Runnable onDispatch;
      boolean ready;
      boolean committed;
      boolean read;

      DispatchGate(BooleanSupplier session, BooleanSupplier input, Runnable dispatched) {
         this.sessionOk = session;
         this.inputOk = input;
         this.onDispatch = dispatched;
      }

      boolean checkReady() {
         return this.ready = !this.read && this.sessionOk.getAsBoolean() && this.inputOk.getAsBoolean();
      }

      void dispatchIfReady() {
         if (!this.read && !this.committed && this.ready && this.sessionOk.getAsBoolean()) {
            this.committed = true;
            this.onDispatch.run();
         }
      }

      void load() {
         this.read = true;
      }
   }
}
