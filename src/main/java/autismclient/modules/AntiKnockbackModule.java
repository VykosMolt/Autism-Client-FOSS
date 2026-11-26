package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.util.ck;
import autismclient.util.cn;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec3;

public final class AntiKnockbackModule extends Module {
   private final KnockbackCounterStrafe init_ab = new KnockbackCounterStrafe();
   private int init_I = -2147483648;
   private int load_I = -2147483648;
   private Vec3 init_Vec3 = Vec3.ZERO;
   private int read_I = -2147483648;
   private boolean init_Z;
   private boolean load_Z;
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

   private static AntiKnockbackModule init_StateCache() {
      return al.init_Module_Str("anti-knockback") instanceof AntiKnockbackModule m && m.isEnabled() ? m : null;
   }

   private int init_I_10() {
      return ck.init_ck().state_I();
   }

   private boolean init_Z_17() {
      return MC.player != null
         && MC.level != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !aD.init_Z()
         && !FreecamController.init_Z()
         && !FreecamController.write()
         && MC.gui.screen() == null
         && !MC.player.isPassenger()
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && !MC.player.getAbilities().flying
         && !MC.player.isFallFlying();
   }

   public static void init_V_ClientboundDamageE(ClientboundDamageEventPacket packet) {
      AntiKnockbackModule m = init_StateCache();
      if (m != null && m.init_Z_17() && packet.entityId() == MC.player.getId()) {
         DamageSource source = packet.getSource(MC.level);
         Entity attacker = source.getEntity();
         boolean projectile = source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT);
         if (attacker != MC.player && (attacker instanceof LivingEntity || projectile)) {
            m.init_I = m.init_I_10();
            boolean reset = "JumpReset".equals(m.choice("mode"));
            m.init_Z = !reset || ThreadLocalRandom.current().nextInt(100) < m.integer("chance");
            m.load_Z = !reset || m.bool("counter-movement");
            boolean motionReady = m.init_Z_I_5(m.load_I);
            m.read_Z = reset && projectile && !motionReady;
            m.init_ab.init_V();
            if (m.init_Z) {
               if (!m.read_Z) {
                  m.init_ab.init(motionReady ? m.init_Vec3 : Vec3.ZERO, reset, motionReady && m.load_Z, m.integer("counter-ticks"));
                  m.read_I = m.init_I_10();
               }

               m.init_J++;
            }

            if (m.init_Z_I_5(m.load_I)) {
               m.init_I = m.load_I = -2147483648;
            }
         }
      }
   }

   public static void init_V_I_7(int entityId) {
      AntiKnockbackModule m = init_StateCache();
      if (m != null && m.init_Z_17() && entityId == MC.player.getId()) {
         m.load_I = m.init_I_10();
         m.init_Vec3 = MC.player.getDeltaMovement();
         if (m.init_Z_I_5(m.init_I)) {
            if (m.init_Z) {
               if (m.read_Z) {
                  m.init_ab.init(m.init_Vec3, true, m.load_Z, m.integer("counter-ticks"));
               } else {
                  m.init_ab.init(m.init_Vec3, m.load_Z, m.integer("counter-ticks"));
               }

               m.read_I = m.init_I_10();
            }

            m.read_Z = false;
            m.init_I = m.load_I = -2147483648;
         }
      }
   }

   private boolean init_Z_I_5(int tick) {
      int age = this.init_I_10() - tick;
      return age >= 0 && age <= 2;
   }

   public static Input init_Input_Input(Input original) {
      AntiKnockbackModule m = init_StateCache();
      if (m == null || original == null) {
         return original;
      }

      if (m.init_Z_17() && !ClutchModule.read_Z_20() && !DangerModule.sync_Z_12() && !ScaffoldModule.write_Z_5() && !ScaffoldModule.update_Z_5() && !AutoTotemModule.init_Z_6() && !AutoArmorModule.init_Z_20()) {
         int age = m.init_I_10() - m.read_I;
         if (m.read_I != -2147483648 && age >= 0 && age <= 6) {
            boolean ground = MC.player.onGround() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() == 0;
            boolean braking = m.init_ab.init_I() > 0;
            Input result = m.init_ab.init(original, MC.player.getDeltaMovement(), cn.init_F(MC.player), ground);
            if (result.jump() && !original.jump()) {
               m.load_J++;
            }

            if (braking && result != original) {
               m.read_J++;
            }

            return result;
         } else {
            m.init_ab.init_V();
            m.read_I = -2147483648;
            if (!m.init_Z_I_5(m.init_I)) {
               m.init_I = -2147483648;
            }

            if (!m.init_Z_I_5(m.load_I)) {
               m.load_I = -2147483648;
            }

            return original;
         }
      } else {
         m.load_V_13();
         return original;
      }
   }

   private void load_V_13() {
      this.init_ab.init_V();
      this.init_I = this.load_I = this.read_I = -2147483648;
      this.init_Z = this.load_Z = this.read_Z = false;
   }

   public static void init_V_16() {
      AntiKnockbackModule m = init_StateCache();
      if (m != null) {
         m.load_V_13();
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
      this.load_V_13();
      this.init_J = this.load_J = this.read_J = 0L;
   }

   @Override
   public void onDisable() {
      this.load_V_13();
   }

   @Override
   public void onGameLeft() {
      this.load_V_13();
   }

   @Override
   public void onOptionValueChanged(String id) {
      this.load_V_13();
   }

   private enum Inner_DataUtil {
      JumpReset,
      CounterMovement;
   }
}
