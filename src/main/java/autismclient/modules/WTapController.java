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
   static final ValueRange init_ValueRange = new ValueRange(20, 60);
   static final ValueRange load_ValueRange = new ValueRange(45, 85);
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private final WTapTimingMachine init_be = new WTapTimingMachine();
   private final Random init_Random = new Random();
   private final WTapController.Inner_DataUtil init_bf_DataUtil;
   private ClientLevel init_ClientLevel;
   private ClientPacketListener init_ClientPacketListen;
   private Player init_Player;
   private UUID init_UUID;
   private int init_I;
   private int load_I;
   private Item init_Item;
   private double init_D;

   WTapController(WTapController.Inner_DataUtil settings) {
      this.init_bf_DataUtil = settings;
   }

   private static int init_I() {
      return AutismSessionState.getInstance().getClientTick();
   }

   void init(Player attacker, Entity entity) {
      if (attacker == init_Minecraft.player
         && entity instanceof LivingEntity target
         && SprintStateTracker.sync()
         && this.update()
         && init_Minecraft.player.input.keyPresses.forward()
         && !init_Minecraft.player.input.keyPresses.backward()
         && target.isAlive()
         && target.level() == init_Minecraft.level
         && !target.isSpectator()
         && (!this.init_bf_DataUtil.init_Z() || target instanceof Player)
         && !TeamsModule.isFriend(target)
         && !AntiBotTracker.isBot(target)) {
         long now = System.nanoTime();
         int chance = this.init_bf_DataUtil.init_I();
         boolean selected = chance > 0 && this.init_Random.nextInt(100) < chance;
         if (this.init_be
            .init(now, init_I(), this.init(this.init_bf_DataUtil.init_ValueRange(), 0), this.init(this.init_bf_DataUtil.load_ValueRange(), 1), selected)) {
            this.init_ClientLevel = init_Minecraft.level;
            this.init_ClientPacketListen = init_Minecraft.getConnection();
            this.init_Player = attacker;
            this.init_UUID = target.getUUID();
            this.init_I = target.getId();
            this.load_I = init_Minecraft.player.getInventory().getSelectedSlot();
            this.init_Item = init_Minecraft.player.getMainHandItem().getItem();
         }
      }
   }

   void onDamageEvent(ClientboundDamageEventPacket packet) {
      if (this.init_be.isActive() && packet.entityId() == this.init_I && this.read_Z()) {
         Entity target = init_Minecraft.level.getEntity(this.init_I);
         if (target != null && target.getUUID().equals(this.init_UUID) && packet.getSource(init_Minecraft.level).getEntity() == init_Minecraft.player) {
            this.init_be.init_Z(System.nanoTime());
         }
      }
   }

   Input filterInput(Input original) {
      if (!this.init_be.isActive()) {
         return original;
      }

      boolean valid = this.read_Z()
         && this.update()
         && this.init_bf_DataUtil.init_I() > 0
         && original.forward()
         && !original.backward()
         && !original.shift()
         && this.write_Z();
      boolean release = this.init_be.init(System.nanoTime(), init_I(), valid);
      if (!this.init_be.isActive()) {
         this.write_V();
      }

      if (!release) {
         return original;
      }

      boolean strafe = original.left() != original.right() && !init_Z_Input(original);
      return new Input(false, original.backward(), strafe && original.left(), strafe && original.right(), original.jump(), original.shift(), false);
   }

   private static boolean init_Z_Input(Input original) {
      autismclient.util.MovementInputFixer.Inner_DataUtil footing = LedgeFootingPredictor.init(init_Minecraft.player);
      if (footing == null) {
         return false;
      }

      float camera = init_Minecraft.player.getYRot();
      float side = original.left() ? 90.0F : -90.0F;
      float whole = (float)Math.toDegrees(Math.atan2(original.left() ? 1.0 : -1.0, original.forward() ? 1.0 : 0.0));
      int full = 3;
      return footing.ticks(camera - side) < full && footing.ticks(camera - whole) >= full;
   }

   boolean isTapping() {
      return this.init_be.isReleasing() && this.read_Z() && this.update();
   }

   boolean load_Z() {
      this.tick();
      return this.init_be.isActive();
   }

   void tick() {
      if (!this.init_be.load(System.nanoTime())) {
         this.write_V();
      } else if (this.read_Z()
         && this.update()
         && this.init_bf_DataUtil.init_I() > 0
         && this.write_Z()
         && init_Minecraft.options.keyUp.isDown()
         && !init_Minecraft.options.keyDown.isDown()
         && !init_Minecraft.options.keyShift.isDown()) {
         Entity target = init_Minecraft.level.getEntity(this.init_I);
         if (!(
            target instanceof LivingEntity living
               && living.isAlive()
               && target.getUUID().equals(this.init_UUID)
               && !TeamsModule.isFriend(living)
               && !AntiBotTracker.isBot(living)
         )) {
            this.init_be.reset();
            this.write_V();
         }
      } else {
         this.init_be.reset();
         this.write_V();
      }
   }

   void load_V() {
      if (this.init_be.isActive()) {
         this.init_be.reset();
         this.write_V();
      }
   }

   long tapCount() {
      return this.init_be.init_J();
   }

   void read_V() {
      this.init_be.resetAll();
      this.write_V();
   }

   private boolean read_Z() {
      return init_Minecraft.level != null
         && init_Minecraft.level == this.init_ClientLevel
         && init_Minecraft.player == this.init_Player
         && init_Minecraft.getConnection() == this.init_ClientPacketListen;
   }

   private boolean write_Z() {
      return init_Minecraft.player.getInventory().getSelectedSlot() == this.load_I && init_Minecraft.player.getMainHandItem().is(this.init_Item);
   }

   private boolean update() {
      if (init_Minecraft.isSameThread()
         && init_Minecraft.player != null
         && init_Minecraft.level != null
         && init_Minecraft.getConnection() != null
         && init_Minecraft.gameMode != null
         && init_Minecraft.gui.screen() == null
         && init_Minecraft.gui.overlay() == null
         && init_Minecraft.player.isAlive()
         && !init_Minecraft.player.isSpectator()
         && !init_Minecraft.player.isUsingItem()
         && !autismclient.util.ManualInteractionYield.init()
         && !(init_Minecraft.gameMode instanceof AutismMultiPlayerGameModeAccessor digging && digging.autism$isDestroying() && !BedNukerModule.read_Z_10())
         && !init_Minecraft.player.isPassenger()
         && !init_Minecraft.player.getAbilities().flying
         && !init_Minecraft.player.isFallFlying()
         && !init_Minecraft.player.isInWater()
         && !init_Minecraft.player.isInLava()
         && !init_Minecraft.player.onClimbable()
         && (!this.init_bf_DataUtil.load_Z() || init_Minecraft.player.onGround())
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !FreecamController.isAiControllingBody()) {
         Module scaffold = al.init_Module_Str("scaffold");
         return (scaffold == null || !scaffold.isEnabled())
            && !ClutchModule.init_Z_49()
            && !DangerModule.sync_Z_12()
            && !BedNukerModule.init_Z_23()
            && !AutoTotemModule.init_Z_6()
            && !AutoArmorModule.init_Z_20()
            && !autismclient.util.macro.MacroAimController.init_Z()
            && !SilentMovementCoordinator.isSafeWalkActive();
      } else {
         return false;
      }
   }

   private long init(ValueRange range, int minimum) {
      ValueRange band = (range == null ? (minimum > 0 ? load_ValueRange : init_ValueRange) : range).clamp(minimum, 200.0);
      this.init_D = this.init_D * 0.92 + this.init_Random.nextGaussian() * 0.035;
      double style = (autismclient.util.HumanHandProfile.init_am().close() - 0.9) * 0.5;
      double sample = Math.max(0.0, Math.min(1.0, (this.init_Random.nextDouble() + this.init_Random.nextDouble()) * 0.5 + style + this.init_D));
      return Math.round((band.min() + (band.max() - band.min()) * sample) * 1000000.0);
   }

   private void write_V() {
      this.init_ClientLevel = null;
      this.init_ClientPacketListen = null;
      this.init_Player = null;
      this.init_UUID = null;
      this.init_Item = null;
   }

   interface Inner_DataUtil {
      int init_I();

      ValueRange init_ValueRange();

      ValueRange load_ValueRange();

      boolean init_Z();

      boolean load_Z();
   }
}
