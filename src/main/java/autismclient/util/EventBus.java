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

public final class EventBus {
   private static EventBus.Inner_DataUtil init;
   private static EventBus.Inner_DataUtil load;

   private EventBus() {
   }

   public static boolean init(String owner, BlockHitResult hit, InteractionHand hand, BooleanSupplier legal, Runnable dispatched) {
      if (init == null && load == null) {
         EventBus.Inner_DataUtil action = new EventBus.Inner_DataUtil(owner, hit, hand, null, legal, dispatched);
         if (!s.init(hit, hand)) {
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
         EventBus.Inner_DataUtil action = new EventBus.Inner_DataUtil(owner, null, null, hit.getEntity(), legal, dispatched);
         if (!s.init(owner, hit)) {
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
         && load.init_Str.equals(owner)
         && load.init_BlockHitResult != null
         && load.init_BlockHitResult.getBlockPos().equals(support)
         && load.init_EventBus_StreamUti.init_Z
         && load.init();
   }

   static void load() {
      if (init != null) {
         init.init_EventBus_StreamUti.load();
      }

      if (load != null) {
         load.init_EventBus_StreamUti.load();
      }

      load = null;
      init = null;
   }

   public static void init(String owner) {
      if (init != null && init.init_Str.equals(owner) || load != null && load.init_Str.equals(owner)) {
         s.write_V();
      }
   }

   public static void init(Packet<?> packet) {
      EventBus.Inner_DataUtil action = load;
      if (action != null) {
         if (packet instanceof ServerboundUseItemOnPacket use
            && action.init_BlockHitResult != null
            && action.init_InteractionHand == use.hand()
            && action.init_BlockHitResult.getBlockPos().equals(use.hitResult().getBlockPos())
            && action.init_BlockHitResult.getDirection() == use.hitResult().getDirection()
            && action.init_BlockHitResult.getLocation().distanceToSqr(use.hitResult().getLocation()) < 1.0E-10) {
            boolean wasCommitted = action.init_EventBus_StreamUti.load_Z;
            action.init_EventBus_StreamUti.init_V();
            if (!wasCommitted && action.init_EventBus_StreamUti.load_Z) {
               ((AutismLocalPlayerAccessor)action.init_Minecraft.player).autism$setPositionReminder(20);
            }
         } else if (packet instanceof ServerboundAttackPacket attack
            && action.init_Entity != null
            && action.init_Entity.getId() == attack.entityId()
            && action.init_Minecraft.level.getEntity(attack.entityId()) == action.init_Entity) {
            action.init_EventBus_StreamUti.init_V();
         }
      }
   }

   private static final class Inner_DataUtil {
      final String init_Str;
      final Minecraft init_Minecraft = Minecraft.getInstance();
      final Object init_Obj = this.init_Minecraft.player;
      final Object load_Obj = this.init_Minecraft.level;
      final Object read = this.init_Minecraft.getConnection();
      final int init_I = ck.init_ck().state_I();
      final int load_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
      final ItemStack init_ItemStack = this.init_Minecraft.player.getMainHandItem().copy();
      final ItemStack load_ItemStack = this.init_Minecraft.player.getOffhandItem().copy();
      final BlockHitResult init_BlockHitResult;
      final InteractionHand init_InteractionHand;
      final Entity init_Entity;
      final EventBus.Inner_StreamUtil init_EventBus_StreamUti;

      Inner_DataUtil(String owner, BlockHitResult hit, InteractionHand hand, Entity entity, BooleanSupplier legal, Runnable dispatched) {
         this.init_Str = owner;
         this.init_BlockHitResult = hit;
         this.init_InteractionHand = hand;
         this.init_Entity = entity;
         this.init_EventBus_StreamUti = new EventBus.Inner_StreamUtil(this::init, () -> this.read() && legal.getAsBoolean(), dispatched);
      }

      boolean init() {
         Module module = autismclient.modules.al.init_Module_Str(this.init_Str);
         return EventBus.load == this
            && module != null
            && module.isEnabled()
            && this.init_Minecraft.player == this.init_Obj
            && this.init_Minecraft.level == this.load_Obj
            && this.init_Minecraft.getConnection() == this.read
            && this.init_I == ck.init_ck().state_I()
            && this.init_Minecraft.gui.screen() == null
            && this.init_Minecraft.gui.overlay() == null;
      }

      boolean load() {
         return this.init_EventBus_StreamUti.init_Z();
      }

      boolean read() {
         return !this.init_Minecraft.player.isDeadOrDying()
            && !this.init_Minecraft.player.isUsingItem()
            && this.load_I == this.init_Minecraft.player.getInventory().getSelectedSlot()
            && ItemStack.isSameItemSameComponents(this.init_ItemStack, this.init_Minecraft.player.getMainHandItem())
            && ItemStack.isSameItemSameComponents(this.load_ItemStack, this.init_Minecraft.player.getOffhandItem());
      }
   }

   static final class Inner_StreamUtil {
      final BooleanSupplier init_BooleanSupplier;
      final BooleanSupplier load_BooleanSupplier;
      final Runnable init_Runnable;
      boolean init_Z;
      boolean load_Z;
      boolean read;

      Inner_StreamUtil(BooleanSupplier session, BooleanSupplier input, Runnable dispatched) {
         this.init_BooleanSupplier = session;
         this.load_BooleanSupplier = input;
         this.init_Runnable = dispatched;
      }

      boolean init_Z() {
         return this.init_Z = !this.read && this.init_BooleanSupplier.getAsBoolean() && this.load_BooleanSupplier.getAsBoolean();
      }

      void init_V() {
         if (!this.read && !this.load_Z && this.init_Z && this.init_BooleanSupplier.getAsBoolean()) {
            this.load_Z = true;
            this.init_Runnable.run();
         }
      }

      void load() {
         this.read = true;
      }
   }
}
