package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class AntiKnockbackModule extends Module {
   private final KnockbackCounterStrafe counterStrafe = new KnockbackCounterStrafe();
   private int init_I = -2147483648;
   private int load_I = -2147483648;
   private Vec3 preHitMotion = Vec3.ZERO;
   private int read_I = -2147483648;
   private boolean init_Z;
   private boolean counterMovement;
   private boolean read_Z;
   private long init_J;
   private long load_J;
   private long read_J;

   public AntiKnockbackModule() {
      super("anti-knockback", "AntiKnockback", ae.init_ae, "Jump reset and briefly counter enemy knockback");
      this.add(new EnumSetting<>("mode", "Mode", AntiKnockbackModule.Inner_DataUtil.JumpReset, AntiKnockbackModule.Inner_DataUtil.values(), Enum::name));
      this.add(
         new IntSetting("chance", "Chance", 100, 0, 100, 1)
            .description("Jump reset activation chance")
            .visibleWhen(() -> "JumpReset".equals(this.choice("mode")))
      );
      this.add(
         new BoolSetting("counter-movement", "Counter Movement", true)
            .description("Briefly brake after a jump reset")
            .visibleWhen(() -> "JumpReset".equals(this.choice("mode")))
      );
      this.add(
         new IntSetting("counter-ticks", "Counter Time", 4, 1, 6, 1)
            .description("Maximum ticks of counter movement")
            .visibleWhen(() -> "CounterMovement".equals(this.choice("mode")) || this.bool("counter-movement"))
      );
   }

   private static AntiKnockbackModule activeInstance() {
      return al.init_Module_Str("anti-knockback") instanceof AntiKnockbackModule m && m.isEnabled() ? m : null;
   }

   private int clientTick() {
      return AutismSessionState.getInstance().getClientTick();
   }

   private boolean canAct() {
      return MC.player != null
         && MC.level != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !FreecamController.isAiControllingBody()
         && MC.gui.screen() == null
         && !MC.player.isPassenger()
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && !MC.player.getAbilities().flying
         && !MC.player.isFallFlying();
   }

   public static void onDamageEvent(ClientboundDamageEventPacket packet) {
      AntiKnockbackModule m = activeInstance();
      if (m != null && m.canAct() && packet.entityId() == MC.player.getId()) {
         DamageSource source = packet.getSource(MC.level);
         Entity attacker = source.getEntity();
         boolean projectile = source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT);
         if (attacker != MC.player && (attacker instanceof LivingEntity || projectile)) {
            m.init_I = m.clientTick();
            boolean reset = "JumpReset".equals(m.choice("mode"));
            m.init_Z = !reset || ThreadLocalRandom.current().nextInt(100) < m.integer("chance");
            m.counterMovement = !reset || m.bool("counter-movement");
            boolean motionReady = m.isRecent(m.load_I);
            m.read_Z = reset && projectile && !motionReady;
            m.counterStrafe.reset();
            if (m.init_Z) {
               if (!m.read_Z) {
                  m.counterStrafe.init(motionReady ? m.preHitMotion : Vec3.ZERO, reset, motionReady && m.counterMovement, m.integer("counter-ticks"));
                  m.read_I = m.clientTick();
               }

               m.init_J++;
            }

            if (m.isRecent(m.load_I)) {
               m.init_I = m.load_I = -2147483648;
            }
         }
      }
   }

   public static void init_V_I_7(int entityId) {
      AntiKnockbackModule m = activeInstance();
      if (m != null && m.canAct() && entityId == MC.player.getId()) {
         m.load_I = m.clientTick();
         m.preHitMotion = MC.player.getDeltaMovement();
         if (m.isRecent(m.init_I)) {
            if (m.init_Z) {
               if (m.read_Z) {
                  m.counterStrafe.init(m.preHitMotion, true, m.counterMovement, m.integer("counter-ticks"));
               } else {
                  m.counterStrafe.init(m.preHitMotion, m.counterMovement, m.integer("counter-ticks"));
               }

               m.read_I = m.clientTick();
            }

            m.read_Z = false;
            m.init_I = m.load_I = -2147483648;
         }
      }
   }

   private boolean isRecent(int tick) {
      int age = this.clientTick() - tick;
      return age >= 0 && age <= 2;
   }

   public static Input filterInput(Input original) {
      AntiKnockbackModule m = activeInstance();
      if (m == null || original == null) {
         return original;
      }

      if (m.canAct() && !ClutchModule.read_Z_20() && !DangerModule.sync_Z_12() && !ScaffoldModule.write_Z_5() && !ScaffoldModule.update_Z_5() && !AutoTotemModule.init_Z_6() && !AutoArmorModule.init_Z_20()) {
         int age = m.clientTick() - m.read_I;
         if (m.read_I != -2147483648 && age >= 0 && age <= 6) {
            boolean ground = MC.player.onGround() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() == 0;
            boolean braking = m.counterStrafe.init_I() > 0;
            Input result = m.counterStrafe.init(original, MC.player.getDeltaMovement(), SilentMovementCoordinator.getMovementYaw(MC.player), ground);
            if (result.jump() && !original.jump()) {
               m.load_J++;
            }

            if (braking && result != original) {
               m.read_J++;
            }

            return result;
         } else {
            m.counterStrafe.reset();
            m.read_I = -2147483648;
            if (!m.isRecent(m.init_I)) {
               m.init_I = -2147483648;
            }

            if (!m.isRecent(m.load_I)) {
               m.load_I = -2147483648;
            }

            return original;
         }
      } else {
         m.resetState();
         return original;
      }
   }

   private void resetState() {
      this.counterStrafe.reset();
      this.init_I = this.load_I = this.read_I = -2147483648;
      this.init_Z = this.counterMovement = this.read_Z = false;
   }

   public static void init_V_16() {
      AntiKnockbackModule m = activeInstance();
      if (m != null) {
         m.resetState();
      }
   }

   public long init_J_4() {
      return this.init_J;
   }

   public long load_J() {
      return this.load_J;
   }

   public long read() {
      return this.read_J;
   }

   @Override
   public void onEnable() {
      this.resetState();
      this.init_J = this.load_J = this.read_J = 0L;
   }

   @Override
   public void onDisable() {
      this.resetState();
   }

   @Override
   public void onGameLeft() {
      this.resetState();
   }

   @Override
   public void onOptionValueChanged(String id) {
      this.resetState();
   }

   private enum Inner_DataUtil {
      JumpReset,
      CounterMovement;
   }
}
