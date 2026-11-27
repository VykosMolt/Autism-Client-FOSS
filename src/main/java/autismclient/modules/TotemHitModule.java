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
   private LivingEntity init_LivingEntity;
   private Object init_Obj;
   private int init_I = -1;
   private int load = -1;
   private int read;
   private int write;
   private int update;
   private long init_J;
   private boolean init_Z;
   private ItemStack init_ItemStack = ItemStack.EMPTY;

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
      if (this.init_LivingEntity != null && !this.init_Z) {
         return true;
      }

      if (this.init_Z_29()
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

         this.init_LivingEntity = enemy;
         this.init_Obj = MC.level;
         this.load = best;
         this.init_ItemStack = MC.player.getInventory().getItem(best).copy();
         this.init_I = MC.player.getInventory().getSelectedSlot();
         this.read = AutismSessionState.getInstance().getClientTick();
         this.write = -2147483648;
         this.init_Z = false;
         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_29() {
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
      if (!AutismLiteVariant.enabled() && this.init_LivingEntity != null) {
         if (MC.player != null && MC.level == this.init_Obj) {
            int tick = AutismSessionState.getInstance().getClientTick();
            int selected = MC.player.getInventory().getSelectedSlot();
            if (this.write != -2147483648 && selected != this.load) {
               if (!CrystalAuraModule.init_Z_I_10(selected) && !AnchorAuraModule.init_Z_I(selected) && !KillAuraModule.load_Z_I_3(selected)) {
                  this.init_V_24();
               }
            } else if (!this.init_Z
               && tick - this.read <= 8
               && this.isEnabled()
               && this.init_LivingEntity.isAlive()
               && ItemStack.isSameItemSameComponents(this.init_ItemStack, MC.player.getInventory().getItem(this.load))) {
               if (this.init_Z_29()) {
                  if (this.write == -2147483648) {
                     if (selected != this.init_I || !MC.player.getMainHandItem().is(Items.TOTEM_OF_UNDYING)) {
                        this.init_V_24();
                     } else if (autismclient.util.InventorySlotArbiter.claimSlot("totem-hit", this.init_I)
                        && autismclient.util.InventorySlotArbiter.claimSlot("totem-hit", this.load)
                        && this.init(this.load)) {
                        this.write = tick;
                        this.init_J = System.nanoTime() + this.integer("switch-delay") * 1000000L;
                     }
                  } else if (tick != this.write
                     && System.nanoTime() >= this.init_J
                     && autismclient.util.WeaponDamageCalculator.init() instanceof EntityHitResult hit
                     && hit.getEntity() == this.init_LivingEntity) {
                     if (!autismclient.util.QueuedCombatAction.init(
                        "totem-hit",
                        hit,
                        () -> this.init_Z_29()
                           && this.init_LivingEntity.isAlive()
                           && MC.player.getMainHandItem().is(ItemTags.SWORDS)
                           && !TeamsModule.init(this.init_LivingEntity, "killaura")
                           && autismclient.util.WeaponDamageCalculator.init() instanceof EntityHitResult current
                           && current.getEntity() == this.init_LivingEntity,
                        () -> {
                           this.init_Z = true;
                           this.update = AutismSessionState.getInstance().getClientTick() + 1 + (this.integer("return-delay") + 49) / 50;
                        }
                     )) {
                        // Not queued: stop cancelling clicks and return to the totem next tick.
                        this.init_Z = true;
                        this.update = tick;
                     }
                  }
               }
            } else if (!this.init_Z || tick - this.update >= 0) {
               if (this.write == -2147483648 || selected == this.init_I) {
                  this.init_V_24();
               } else if (this.init_Z_29() && this.init(this.init_I)) {
                  this.init_V_24();
               }
            }
         } else {
            this.init_V_24();
         }
      }
   }

   private boolean init(int slot) {
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

   private void init_V_24() {
      autismclient.util.QueuedCombatAction.init("totem-hit");
      this.init_LivingEntity = null;
      this.init_Obj = null;
      this.init_I = this.load = -1;
      this.init_Z = false;
      this.init_ItemStack = ItemStack.EMPTY;
      autismclient.util.InventorySlotArbiter.releaseAllClaims("totem-hit");
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.init_LivingEntity != null;
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
      this.init_V_24();
   }
}
