package autismclient.modules;

import autismclient.api.module.ValueRange;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.LedgeFootingPredictor;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import java.util.Random;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

final class WTapController {
   static final ValueRange DEFAULT_DELAY_MS = new ValueRange(20, 60);
   static final ValueRange DEFAULT_RELEASE_MS = new ValueRange(45, 85);
   private static final Minecraft MC = Minecraft.getInstance();
   private final WTapTimingMachine timing = new WTapTimingMachine();
   private final Random random = new Random();
   private final WTapController.Settings settings;
   private ClientLevel level;
   private ClientPacketListener connection;
   private Player attacker;
   private UUID targetUuid;
   private int targetId;
   private int slot;
   private Item weapon;
   private double drift;

   WTapController(WTapController.Settings settings) {
      this.settings = settings;
   }

   private static int clientTick() {
      return AutismSessionState.getInstance().getClientTick();
   }

   void onAttack(Player attacker, Entity entity) {
      if (attacker == MC.player
         && entity instanceof LivingEntity target
         && SprintStateTracker.attackStartSprinting()
         && this.canTap()
         && MC.player.input.keyPresses.forward()
         && !MC.player.input.keyPresses.backward()
         && target.isAlive()
         && target.level() == MC.level
         && !target.isSpectator()
         && (!this.settings.playersOnly() || target instanceof Player)
         && !TeamsModule.isFriend(target)
         && !AntiBotTracker.isBot(target)) {
         long now = System.nanoTime();
         int chance = this.settings.chance();
         boolean selected = chance > 0 && this.random.nextInt(100) < chance;
         if (this.timing
            .arm(now, clientTick(), this.sampleDelay(this.settings.delayRange(), 0), this.sampleDelay(this.settings.releaseRange(), 1), selected)) {
            this.level = MC.level;
            this.connection = MC.getConnection();
            this.attacker = attacker;
            this.targetUuid = target.getUUID();
            this.targetId = target.getId();
            this.slot = MC.player.getInventory().getSelectedSlot();
            this.weapon = MC.player.getMainHandItem().getItem();
         }
      }
   }

   void onDamageEvent(ClientboundDamageEventPacket packet) {
      if (this.timing.isActive() && packet.entityId() == this.targetId && this.isSameSession()) {
         Entity target = MC.level.getEntity(this.targetId);
         if (target != null && target.getUUID().equals(this.targetUuid) && packet.getSource(MC.level).getEntity() == MC.player) {
            this.timing.beginDelay(System.nanoTime());
         }
      }
   }

   Input filterInput(Input original) {
      if (!this.timing.isActive()) {
         return original;
      }

      boolean valid = this.isSameSession()
         && this.canTap()
         && this.settings.chance() > 0
         && original.forward()
         && !original.backward()
         && !original.shift()
         && this.isHoldingWeapon();
      boolean release = this.timing.shouldRelease(System.nanoTime(), clientTick(), valid);
      if (!this.timing.isActive()) {
         this.clearTarget();
      }

      if (!release) {
         return original;
      }

      boolean strafe = original.left() != original.right() && !init_Z_Input(original);
      return new Input(false, original.backward(), strafe && original.left(), strafe && original.right(), original.jump(), original.shift(), false);
   }

   private static boolean init_Z_Input(Input original) {
      autismclient.util.MovementInputFixer.FootingModel footing = LedgeFootingPredictor.predict(MC.player);
      if (footing == null) {
         return false;
      }

      float camera = MC.player.getYRot();
      float side = original.left() ? 90.0F : -90.0F;
      float whole = (float)Math.toDegrees(Math.atan2(original.left() ? 1.0 : -1.0, original.forward() ? 1.0 : 0.0));
      int full = 3;
      return footing.ticks(camera - side) < full && footing.ticks(camera - whole) >= full;
   }

   boolean isTapping() {
      return this.timing.isReleasing() && this.isSameSession() && this.canTap();
   }

   boolean pollActive() {
      this.tick();
      return this.timing.isActive();
   }

   void tick() {
      if (!this.timing.activeAt(System.nanoTime())) {
         this.clearTarget();
      } else if (this.isSameSession()
         && this.canTap()
         && this.settings.chance() > 0
         && this.isHoldingWeapon()
         && MC.options.keyUp.isDown()
         && !MC.options.keyDown.isDown()
         && !MC.options.keyShift.isDown()) {
         Entity target = MC.level.getEntity(this.targetId);
         if (!(
            target instanceof LivingEntity living
               && living.isAlive()
               && target.getUUID().equals(this.targetUuid)
               && !TeamsModule.isFriend(living)
               && !AntiBotTracker.isBot(living)
         )) {
            this.timing.reset();
            this.clearTarget();
         }
      } else {
         this.timing.reset();
         this.clearTarget();
      }
   }

   void cancelTap() {
      if (this.timing.isActive()) {
         this.timing.reset();
         this.clearTarget();
      }
   }

   long tapCount() {
      return this.timing.init_J();
   }

   void resetAll() {
      this.timing.resetAll();
      this.clearTarget();
   }

   private boolean isSameSession() {
      return MC.level != null
         && MC.level == this.level
         && MC.player == this.attacker
         && MC.getConnection() == this.connection;
   }

   private boolean isHoldingWeapon() {
      return MC.player.getInventory().getSelectedSlot() == this.slot && MC.player.getMainHandItem().is(this.weapon);
   }

   private boolean canTap() {
      if (MC.isSameThread()
         && MC.player != null
         && MC.level != null
         && MC.getConnection() != null
         && MC.gameMode != null
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !MC.player.isUsingItem()
         && !autismclient.util.ManualInteractionYield.isYielding()
         && !(MC.gameMode instanceof AutismMultiPlayerGameModeAccessor digging && digging.autism$isDestroying() && !BedNukerModule.isMining())
         && !MC.player.isPassenger()
         && !MC.player.getAbilities().flying
         && !MC.player.isFallFlying()
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && !MC.player.onClimbable()
         && (!this.settings.groundOnly() || MC.player.onGround())
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !FreecamController.isAiControllingBody()) {
         Module scaffold = al.init_Module_Str("scaffold");
         return (scaffold == null || !scaffold.isEnabled())
            && !ClutchModule.isClutchEngaged()
            && !DangerModule.sync_Z_12()
            && !BedNukerModule.init_Z_23()
            && !AutoTotemModule.init_Z_6()
            && !AutoArmorModule.isClickPending()
            && !autismclient.util.macro.MacroAimController.isActive()
            && !SilentMovementCoordinator.isSafeWalkActive();
      } else {
         return false;
      }
   }

   private long sampleDelay(ValueRange range, int minimum) {
      ValueRange band = (range == null ? (minimum > 0 ? DEFAULT_RELEASE_MS : DEFAULT_DELAY_MS) : range).clamp(minimum, 200.0);
      this.drift = this.drift * 0.92 + this.random.nextGaussian() * 0.035;
      double style = (autismclient.util.HumanHandProfile.shared().strokeEase() - 0.9) * 0.5;
      double sample = Math.max(0.0, Math.min(1.0, (this.random.nextDouble() + this.random.nextDouble()) * 0.5 + style + this.drift));
      return Math.round((band.min() + (band.max() - band.min()) * sample) * 1000000.0);
   }

   private void clearTarget() {
      this.level = null;
      this.connection = null;
      this.attacker = null;
      this.targetUuid = null;
      this.weapon = null;
   }

   interface Settings {
      int chance();

      ValueRange delayRange();

      ValueRange releaseRange();

      boolean playersOnly();

      boolean groundOnly();
   }
}
