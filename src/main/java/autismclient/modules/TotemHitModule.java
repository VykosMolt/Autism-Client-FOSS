package autismclient.modules;

import autismclient.api.module.IntSetting;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RemoteViewController;
import autismclient.util.AutismSessionState;
import autismclient.util.macro.MacroExecutor;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public final class TotemHitModule extends Module {
   public static final String init_Str = "totem-hit";
   private LivingEntity target;
   private Object targetLevel;
   private int originalSlot = -1;
   private int totemSlot = -1;
   private int swapTick;
   private int switchTick;
   private int returnAtTick;
   private long swordReadyAt;
   private boolean swordHitDone;
   private ItemStack swordSnapshot = ItemStack.EMPTY;

   public TotemHitModule() {
      super("totem-hit", "TotemHit", ae.init_ae, "Borrow a knockback sword for an attack made with a totem.");
      this.add(
         new IntSetting("switch-delay", "Switch Delay", 0, 0, 250, 10).description("Settling time before the original click is dispatched.").unit("ms").build()
      );
      this.add(
         new IntSetting("return-delay", "Return Delay", 0, 0, 500, 10)
            .description("Extra time before returning to the totem after the hit.")
            .unit("ms")
            .build()
      );
   }

   @Override
   public boolean shouldCancelAttack(HitResult hit) {
      if (this.target != null && !this.swordHitDone) {
         return true;
      }

      if (this.canAct()
         && MC.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)
         && hit instanceof EntityHitResult entityHit
         && entityHit.getEntity() instanceof LivingEntity enemy
         && enemy.isAlive()
         && !enemy.isSpectator()
         && !AntiBotTracker.shouldIgnoreEntity(enemy)
         && !TeamsModule.init(enemy, "killaura")) {
         Reference<Enchantment> knockback = MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK);
         int best = -1;
         int bestLevel = 0;

         for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = MC.player.getInventory().getItem(slot);
            if (stack.is(ItemTags.SWORDS) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "totem-hit")) {
               int enchantment = EnchantmentHelper.getItemEnchantmentLevel(knockback, stack);
               if (enchantment > bestLevel) {
                  best = slot;
                  bestLevel = enchantment;
               }
            }
         }

         if (best < 0) {
            return false;
         }

         this.target = enemy;
         this.targetLevel = MC.level;
         this.totemSlot = best;
         this.swordSnapshot = MC.player.getInventory().getItem(best).copy();
         this.originalSlot = MC.player.getInventory().getSelectedSlot();
         this.swapTick = AutismSessionState.getInstance().getClientTick();
         this.switchTick = -2147483648;
         this.swordHitDone = false;
         return true;
      } else {
         return false;
      }
   }

   private boolean canAct() {
      return MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !MC.player.isDeadOrDying()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !MC.player.isHandsBusy()
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && !MacroExecutor.isRunning()
         && !AutoTotemModule.init_Z_6()
         && !AutoArmorModule.init_Z_20()
         && !autismclient.util.SilentRotationManager.hasActiveRotation()
         && !ScaffoldModule.update_Z_5();
   }

   @Override
   public void preMovementTick() {
      if (!AutismLiteVariant.enabled() && this.target != null) {
         if (MC.player != null && MC.level == this.targetLevel) {
            int tick = AutismSessionState.getInstance().getClientTick();
            int selected = MC.player.getInventory().getSelectedSlot();
            if (this.switchTick != -2147483648 && selected != this.totemSlot) {
               if (!CrystalAuraModule.hasBorrowedSlot(selected) && !AnchorAuraModule.hasSwitchedToSlot(selected) && !KillAuraModule.load_Z_I_3(selected)) {
                  this.reset();
               }
            } else if (!this.swordHitDone
               && tick - this.swapTick <= 8
               && this.isEnabled()
               && this.target.isAlive()
               && ItemStack.isSameItemSameComponents(this.swordSnapshot, MC.player.getInventory().getItem(this.totemSlot))) {
               if (this.canAct()) {
                  if (this.switchTick == -2147483648) {
                     if (selected != this.originalSlot || !MC.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
                        this.reset();
                     } else if (autismclient.util.InventorySlotArbiter.claimSlot("totem-hit", this.originalSlot)
                        && autismclient.util.InventorySlotArbiter.claimSlot("totem-hit", this.totemSlot)
                        && this.selectSlot(this.totemSlot)) {
                        this.switchTick = tick;
                        this.swordReadyAt = System.nanoTime() + this.integer("switch-delay") * 1000000L;
                     }
                  } else if (tick != this.switchTick
                     && System.nanoTime() >= this.swordReadyAt
                     && autismclient.util.WeaponDamageCalculator.init() instanceof EntityHitResult hit
                     && hit.getEntity() == this.target) {
                     if (!autismclient.util.QueuedCombatAction.queueAttack(
                        "totem-hit",
                        hit,
                        () -> this.canAct()
                           && this.target.isAlive()
                           && MC.player.getMainHandItem().is(ItemTags.SWORDS)
                           && !TeamsModule.init(this.target, "killaura")
                           && autismclient.util.WeaponDamageCalculator.init() instanceof EntityHitResult current
                           && current.getEntity() == this.target,
                        () -> {
                           this.swordHitDone = true;
                           this.returnAtTick = AutismSessionState.getInstance().getClientTick() + 1 + (this.integer("return-delay") + 49) / 50;
                        }
                     )) {
                        // Not queued: stop cancelling clicks and return to the totem next tick.
                        this.swordHitDone = true;
                        this.returnAtTick = tick;
                     }
                  }
               }
            } else if (!this.swordHitDone || tick - this.returnAtTick >= 0) {
               if (this.switchTick == -2147483648 || selected == this.originalSlot) {
                  this.reset();
               } else if (this.canAct() && this.selectSlot(this.originalSlot)) {
                  this.reset();
               }
            }
         } else {
            this.reset();
         }
      }
   }

   private boolean selectSlot(int slot) {
      if (!autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(slot, "totem-hit") && autismclient.util.InventorySlotArbiter.beginActionGroup("totem-hit")) {
         try {
            autismclient.util.InventorySlotHelper.selectHotbarSlot(MC, slot);
         } finally {
            autismclient.util.InventorySlotArbiter.endActionGroup("totem-hit");
         }

         return true;
      } else {
         return false;
      }
   }

   private void reset() {
      autismclient.util.QueuedCombatAction.init("totem-hit");
      this.target = null;
      this.targetLevel = null;
      this.originalSlot = this.totemSlot = -1;
      this.swordHitDone = false;
      this.swordSnapshot = ItemStack.EMPTY;
      autismclient.util.InventorySlotArbiter.releaseAllClaims("totem-hit");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.target != null;
   }

   @Override
   public void tick() {
      if (!this.isEnabled()) {
         this.preMovementTick();
      }
   }

   @Override
   public void onDisable() {
      autismclient.util.QueuedCombatAction.init("totem-hit");
   }

   @Override
   public void onGameLeft() {
      this.reset();
   }
}
