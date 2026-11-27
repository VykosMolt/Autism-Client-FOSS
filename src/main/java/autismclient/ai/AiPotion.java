package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

final class AiPotion {
   private AiPotion() {
   }

   static boolean init(ItemStack stack) {
      if (!stack.is(Items.POTION)) {
         return false;
      }

      PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
      if (contents == null) {
         return false;
      }

      for (MobEffectInstance effect : contents.getAllEffects()) {
         if (effect.getEffect().is(MobEffects.FIRE_RESISTANCE)) {
            return true;
         }
      }

      return false;
   }

   static int init(Inventory inventory) {
      for (int slot = 0; slot < 36; slot++) {
         if (init(inventory.getItem(slot))) {
            return slot;
         }
      }

      return -1;
   }

   static boolean init(Minecraft mc) {
      return mc.player != null && !mc.player.hasEffect(MobEffects.FIRE_RESISTANCE) && init(mc.player.getInventory()) >= 0;
   }

   static AiOperation init() {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         int init_I;
         int load_I;
         int read = -1;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load_Z || mc.player == null) {
               return this.init(AiOperation.Status.CANCELLED, "Drink cancelled");
            }

            if (mc.player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
               return this.init(AiOperation.Status.SUCCEEDED, "Fire resistance active");
            }

            if (++this.init_I > 120) {
               return this.init(AiOperation.Status.TIMED_OUT, "The potion was not drunk in time");
            }

            Inventory inventory = mc.player.getInventory();
            if (mc.player.isUsingItem()) {
               mc.options.keyUse.setDown(true);
               return this.init("Drinking fire resistance");
            }

            if (!this.init_Z) {
               int slot = AiPotion.init(inventory);
               if (slot < 0) {
                  return this.init(AiOperation.Status.FAILED, "No fire resistance potion in the bag");
               }

               if (!Inventory.isHotbarSlot(slot)) {
                  AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, slot, inventory.getSelectedSlot(), ContainerInput.SWAP);
                  this.init_Z = true;
                  return this.init("Moving the potion to the hand");
               }

               if (inventory.getSelectedSlot() != slot) {
                  if (this.read < 0) {
                     this.read = inventory.getSelectedSlot();
                  }

                  inventory.setSelectedSlot(slot);
                  return this.init("Potion in hand");
               } else {
                  mc.options.keyUse.setDown(true);
                  return this.init("Drinking fire resistance");
               }
            } else if (AiPotion.init(inventory.getItem(inventory.getSelectedSlot()))) {
               this.init_Z = false;
               return this.init("Potion in hand");
            } else {
               return ++this.load_I > 40
                  ? this.init(AiOperation.Status.UNCERTAIN, "The potion did not reach the hand")
                  : this.init("Moving the potion to the hand");
            }
         }

         AiOperation.Result init(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            mc.options.keyUse.setDown(false);
            if (this.read >= 0 && mc.player != null) {
               mc.player.getInventory().setSelectedSlot(this.read);
            }

            return AiGameOperations.result(status, message, new JsonObject());
         }

         @Override
         public void cancel() {
            this.load_Z = true;
            mc.options.keyUse.setDown(false);
         }
      };
   }
}
