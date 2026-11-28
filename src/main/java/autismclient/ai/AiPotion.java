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

   static boolean isFireResistance(ItemStack stack) {
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

   static int findFireResistance(Inventory inventory) {
      for (int slot = 0; slot < 36; slot++) {
         if (isFireResistance(inventory.getItem(slot))) {
            return slot;
         }
      }

      return -1;
   }

   static boolean shouldDrinkFireResistance(Minecraft mc) {
      return mc.player != null && !mc.player.hasEffect(MobEffects.FIRE_RESISTANCE) && findFireResistance(mc.player.getInventory()) >= 0;
   }

   static AiOperation drinkFireResistance() {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         int init_I;
         int load_I;
         int previousSlot = -1;
         boolean init_Z;
         boolean load_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.load_Z || mc.player == null) {
               return this.finish(AiOperation.Status.CANCELLED, "Drink cancelled");
            }

            if (mc.player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
               return this.finish(AiOperation.Status.SUCCEEDED, "Fire resistance active");
            }

            if (++this.init_I > 120) {
               return this.finish(AiOperation.Status.TIMED_OUT, "The potion was not drunk in time");
            }

            Inventory inventory = mc.player.getInventory();
            if (mc.player.isUsingItem()) {
               mc.options.keyUse.setDown(true);
               return this.running("Drinking fire resistance");
            }

            if (!this.init_Z) {
               int slot = AiPotion.findFireResistance(inventory);
               if (slot < 0) {
                  return this.finish(AiOperation.Status.FAILED, "No fire resistance potion in the bag");
               }

               if (!Inventory.isHotbarSlot(slot)) {
                  AiContainerTransactions.sendPredictedClick(mc.player.containerMenu.containerId, slot, inventory.getSelectedSlot(), ContainerInput.SWAP);
                  this.init_Z = true;
                  return this.running("Moving the potion to the hand");
               }

               if (inventory.getSelectedSlot() != slot) {
                  if (this.previousSlot < 0) {
                     this.previousSlot = inventory.getSelectedSlot();
                  }

                  inventory.setSelectedSlot(slot);
                  return this.running("Potion in hand");
               } else {
                  mc.options.keyUse.setDown(true);
                  return this.running("Drinking fire resistance");
               }
            } else if (AiPotion.isFireResistance(inventory.getItem(inventory.getSelectedSlot()))) {
               this.init_Z = false;
               return this.running("Potion in hand");
            } else {
               return ++this.load_I > 40
                  ? this.finish(AiOperation.Status.UNCERTAIN, "The potion did not reach the hand")
                  : this.running("Moving the potion to the hand");
            }
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            mc.options.keyUse.setDown(false);
            if (this.previousSlot >= 0 && mc.player != null) {
               mc.player.getInventory().setSelectedSlot(this.previousSlot);
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
