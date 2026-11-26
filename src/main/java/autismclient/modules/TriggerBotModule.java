package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.RangeSetting;
import autismclient.api.module.RegistryListSetting;
import autismclient.api.module.ValueRange;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.util.RotationMath;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class TriggerBotModule extends Module {
   private static final String init_Str = "Modern Combat";
   private static final String load_Str = "1.8 Combat";
   static final ValueRange init_ValueRange = new ValueRange(0, 0);
   static final ValueRange load_ValueRange = new ValueRange(-75.0, 50.0);
   static final ValueRange read_ValueRange = new ValueRange(0, 2);
   static final ValueRange write_ValueRange = new ValueRange(60, 110);
   static final ValueRange update_ValueRange = new ValueRange(0.2, 0.6);
   static final ValueRange sync_ValueRange = new ValueRange(0.08, 0.22);
   private static final long init_J = 300000000L;
   private static final long load_J = 250000000L;
   private static final double init_D = 40.0;
   private static final double load_D = 80.0;
   private static final long read_J = 80000000L;
   private static final long write_J = 80000000L;
   private static final double read_D = 30.0;
   private static final double write_D = 1.5;
   private static final double update_D = 15.0;
   private static final double sync_D = 35.0;
   private final ClickerSettingsGroup init_h;
   private final HeldItemFilter init_O;
   private final autismclient.util.clicker.ClickPatternEngine init_DataUtil = new autismclient.util.clicker.ClickPatternEngine();
   private final autismclient.util.clicker.KeyClickOutput init_StreamUtil = autismclient.util.clicker.KeyClickOutput.init_StreamUtil();
   private final autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper init_DataUtil_NetHelper = new autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper() {
      @Override
      public boolean init_Z_J(long now) {
         return Module.MC.hitResult != null && Module.MC.hitResult.getType() != Type.MISS || TriggerBotModule.this.init_StreamUtil.init_Z_J(now);
      }

      @Override
      public void init_V_J(long now) {
         TriggerBotModule.this.init_StreamUtil.init_V_J(now);
      }

      @Override
      public void load_V(long now) {
         TriggerBotModule.this.init_StreamUtil.load_V(now);
      }

      @Override
      public long init_J_J(long now) {
         return TriggerBotModule.this.init_StreamUtil.init_J_J(now);
      }
   };
   private final Random init_Random = new Random();
   private final autismclient.util.clicker.SwingTimingModel init_JsonUtil = new autismclient.util.clicker.SwingTimingModel(this.init_Random);
   private final autismclient.util.clicker.DoubleClickSampler init_TaskQueue = new autismclient.util.clicker.DoubleClickSampler(this.init_Random);
   private autismclient.util.HumanHandProfile init_am;
   private int init_I = -1;
   private long update_J;
   private long sync_J = -9223372036854775808L;
   private int load_I = -1;
   private int read_I;
   private boolean init_Z;
   private long flush_J;
   private int write_I = -2147483648;
   private int update_I = -2147483648;
   private int sync_I = -1;
   private int flush_I = -2147483648;
   private double flush_D;
   private double close_D;
   private double open = 0.0 / 0.0;
   private long close_J = -9223372036854775808L;
   private boolean load_Z;
   private float init_F = -1.0F;
   private String read_Str = "";
   private Set<String> init_Set = Set.of();

   TriggerBotModule() {
      super("trigger-bot", "TriggerBot", ae.init_ae, "Attacks configured entities when your crosshair is on them.");
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").description("Trigger entities").build());
      this.init_O = new HeldItemFilter(this);
      this.add(new RangeSetting("edge-tolerance", "Edge Tolerance", sync_ValueRange, 0.0, 0.5, 0.01).description("Click just past the edge").build());
      this.add(new BoolSetting("respect-cooldown", "Respect Cooldown", true).group("Modern Combat").description("Wait for full charge").build());
      this.add(
         new RangeSetting("reaction", "Reaction", init_ValueRange, 0.0, 400.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay on a new target")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("swing-offset", "Swing Offset", load_ValueRange, -150.0, 400.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay after full charge")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("hesitation", "Hesitation", read_ValueRange, 0.0, 20.0, 0.5)
            .unit("%")
            .group("Modern Combat")
            .description("Chance of a late swing")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("modern-hold", "Hold", write_ValueRange, 20.0, 200.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Button hold time")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.init_h = new ClickerSettingsGroup(this, "1.8 Combat", ClickerSettingsGroup.init_BufferHelper, () -> true, ClickerSettingsGroup.init_ValueRange);
      this.add(
         new RangeSetting("legacy-reaction", "Reaction", init_ValueRange, 0.0, 400.0, 5.0)
            .unit("ms")
            .group("1.8 Combat")
            .description("Delay on a new target")
            .visibleWhen(() -> this.init_h.init_BufferHelper().init_Z())
            .build()
      );
      this.add(
         new RangeSetting("pre-reach", "Pre-Reach", update_ValueRange, 0.0, 1.5, 0.05).group("1.8 Combat").description("Swing before reach, blocks").build()
      );
      this.init_h.init_V();
   }

   @Override
   public void onEnable() {
      this.read();
   }

   @Override
   public void onDisable() {
      this.init_V(System.nanoTime());
   }

   @Override
   public void onGameLeft() {
      this.init_V(System.nanoTime());
   }

   @Override
   protected String externalSettingValue(String settingId) {
      return this.init_h == null ? null : this.init_h.init_Str_Str(settingId);
   }

   @Override
   protected boolean setExternalSettingValue(String settingId, String value) {
      return this.init_h != null && this.init_h.init(settingId, value);
   }

   @Override
   public void onInputFrame() {
      long now = System.nanoTime();
      this.init_StreamUtil.init_V();
      if (this.init_Z_35()) {
         this.init_V(now);
      } else {
         boolean noCooldown = autismclient.util.LegacyCombatDetector.init(MC.player);
         boolean timed = !noCooldown && this.bool("respect-cooldown");
         boolean rage = !timed && this.init_h.init_BufferHelper() == autismclient.util.clicker.ClickTechnique.RAGE;
         boolean flinched = this.load_Z_15();
         boolean physical = this.init_StreamUtil.load_Z();
         if (physical && !this.load_Z && !timed) {
            this.init_DataUtil.init_V_J(now);
         }

         this.load_Z = physical;
         autismclient.util.clicker.ClickProfile profile = this.init_h.init_CachePool();
         this.init_DataUtil.init_V_DataUtil_ConfigSto(this.init_h.init_DataUtil_ConfigSto());
         LivingEntity target = this.init(now, noCooldown, rage);
         boolean engaged = false;
         if (target == null && !timed && !rage) {
            target = this.init_LivingEntity(now);
            engaged = target != null;
         }

         if (target == null) {
            if (this.sync_J == -9223372036854775808L) {
               this.sync_J = now;
            }

            this.init_DataUtil.init(now, false, profile, this.init_StreamUtil);
            this.init_StreamUtil.read(now);
         } else {
            boolean sameTarget = target.getId() == this.init_I && (this.sync_J == -9223372036854775808L || now - this.sync_J <= 300000000L);
            if (!sameTarget) {
               this.init_I = target.getId();
               this.init_V_31();
               this.update_J = rage ? now : now + (long)(this.init_D_LivingEntity_Z(target, timed) * 1000000.0);
               this.load_V_23();
            }

            this.sync_J = -9223372036854775808L;
            boolean reacted = now >= this.update_J;
            if (timed) {
               this.init_DataUtil.init(now, false, profile, this.init_StreamUtil);
               if (flinched && this.init_Random.nextBoolean()) {
                  this.flush_D = this.flush_D + (40.0 + 80.0 * this.init_Random.nextDouble());
               }

               this.init(now, reacted, target);
               this.init_StreamUtil.read(now);
            } else {
               this.init_StreamUtil.read(now);
               this.init_DataUtil.init(now, reacted, profile, engaged ? this.init_DataUtil_NetHelper : this.init_StreamUtil);
            }
         }
      }
   }

   private LivingEntity init_LivingEntity(long now) {
      if (this.init_I >= 0 && MC.level != null) {
         if (!(MC.level.getEntity(this.init_I) instanceof LivingEntity living && this.init(living))) {
            return null;
         } else if (autismclient.util.RecentCombatTracker.init(living, now)) {
            return null;
         } else {
            Vec3 eyes = MC.player.getEyePosition();
            AABB box = living.getBoundingBox();
            double near = autismclient.util.AttackReachHelper.init_D(MC.player) + (Double.isNaN(this.open) ? 0.0 : this.open) + 1.5;
            if (box.distanceToSqr(eyes) > near * near) {
               return null;
            } else {
               return init_D_Vec3_AABB_2(eyes, box) <= 30.0 ? living : null;
            }
         }
      } else {
         return null;
      }
   }

   private static double init_D_Vec3_AABB_2(Vec3 eyes, AABB box) {
      Vec3 to = box.getCenter().subtract(eyes);
      double distance = Math.max(1.0E-6, to.length());
      RotationMath.Rotation look = RotationMath.ofPlayer(MC.player);
      Vec3 view = Vec3.directionFromRotation(look.pitch(), look.yaw());
      double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, view.dot(to) / distance))));
      double radius = Math.toDegrees(Math.atan2(0.5 * Math.hypot(box.getXsize(), box.getYsize()), distance));
      return Math.max(0.0, angle - radius);
   }

   private boolean init_Z_35() {
      return MC == null
         || MC.player == null
         || MC.level == null
         || MC.gui.screen() != null
         || ScaffoldModule.read_Z_12()
         || ScaffoldModule.update_Z_5()
         || SilentMovementCoordinator.activeRotation() != null
         || ScaffoldModule.flush_Z_5()
         || f.load()
         || AutoTotemModule.init_Z_6()
         || !this.init_O.init_Z_ItemStack(MC.player.getMainHandItem());
   }

   private LivingEntity init(long now, boolean noCooldown, boolean rage) {
      if (now >= this.close_J) {
         this.close_D = ValueRange.parse(this.value("edge-tolerance"), sync_ValueRange).random(this.init_Random);
         this.close_J = now + 80000000L + (long)(this.init_Random.nextDouble() * 8.0E7);
      }

      if (Double.isNaN(this.open)) {
         this.init_V_31();
      }

      HitResult live = MC.player.raycastHitResult(1.0F, MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity());
      MC.hitResult = live;
      if (live instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && this.init(living)) {
         return !MC.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)
               && !autismclient.util.AttackReachHelper.init_AttackRange(MC.player).isInRange(MC.player, entityHit.getLocation())
            ? null
            : living;
      } else {
         if (MC.hitResult != null && MC.hitResult.getType() != Type.MISS) {
            return null;
         }

         double reach = autismclient.util.AttackReachHelper.init_D(MC.player);
         if (!rage && this.close_D > 0.0) {
            LivingEntity graze = this.init_LivingEntity_D_D(reach + this.close_D, this.close_D);
            if (graze != null && !autismclient.util.RecentCombatTracker.init(graze, now)) {
               return graze;
            }
         }

         LivingEntity poke = noCooldown ? this.init_LivingEntity_D_D(reach + this.open, 0.0) : null;
         return poke != null && !autismclient.util.RecentCombatTracker.init(poke, now) ? poke : null;
      }
   }

   private LivingEntity init_LivingEntity_D_D(double distance, double grow) {
      RotationMath.Rotation look = RotationMath.ofPlayer(MC.player);
      Vec3 eyes = MC.player.getEyePosition();
      Vec3 end = eyes.add(Vec3.directionFromRotation(look.pitch(), look.yaw()).scale(distance));
      LivingEntity best = null;
      double bestDistance = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && living.isPickable() && this.init(living)) {
            AABB box = living.getBoundingBox().inflate(living.getPickRadius() + grow);
            if (!box.contains(eyes)) {
               Optional<Vec3> hit = box.clip(eyes, end);
               if (!hit.isEmpty()) {
                  double d = hit.get().distanceTo(eyes);
                  if (d < bestDistance) {
                     bestDistance = d;
                     best = living;
                  }
               }
            }
         }
      }

      return best;
   }

   private void init(long now, boolean reacted, LivingEntity target) {
      int ticker = ((AutismLivingEntityAccessor)MC.player).autism$getAttackStrengthTicker();
      int tick = AutismSessionState.init_ck().state_I();
      if (this.load_I >= 0 && ticker < this.load_I + (tick - this.read_I)) {
         this.load_V_23();
      }

      this.load_I = ticker;
      this.read_I = tick;
      if (tick != this.write_I) {
         if (this.update_I != -2147483648 && tick >= this.update_I) {
            boolean onTarget = target.getId() == this.sync_I && MC.hitResult instanceof EntityHitResult hit && hit.getEntity() == target;
            if (onTarget && tick <= this.update_I + 1) {
               if (this.init_StreamUtil.init(now, this.init_J_7())) {
                  this.update_I = -2147483648;
                  this.flush_J = now;
                  this.write_I = tick;
                  this.flush_I = tick;
               }

               return;
            }

            this.update_I = -2147483648;
         }

         if (this.init_Z) {
            if (now - this.flush_J < 250000000L) {
               return;
            }

            this.init_Z = false;
         }

         if (reacted) {
            boolean opening = autismclient.util.RecentCombatTracker.init(target, now);
            double scored = ticker + 0.5;
            double shown = opening ? scored : Math.max(scored, ticker + MC.getDeltaTracker().getGameTimeDeltaPartialTick(false));
            double offset = opening ? 0.0 : this.flush_D;
            Vec3 step = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
            boolean lastChance = MC.player.getAttackStrengthScale(0.5F) > 0.9F
               && MC.hitResult instanceof EntityHitResult hit
               && hit.getEntity() == target
               && autismclient.util.AttackReachHelper.init(
                  MC.player.getEyePosition(),
                  target.getBoundingBox(),
                  ShieldHitWindowHelper.init(MC.player.getDeltaMovement()),
                  ShieldHitWindowHelper.init(step),
                  autismclient.util.AttackReachHelper.init_D(MC.player)
               );
            if (lastChance || autismclient.util.clicker.SwingTimingModel.init(shown, ticker, MC.player.getCurrentItemAttackStrengthDelay(), init_D_10(), offset)) {
               ValueRange hold = ValueRange.parse(this.value("modern-hold"), write_ValueRange);
               int followUp = lastChance ? 0 : this.init_TaskQueue.init();
               long holdNanos = followUp > 0 ? this.init_J_7() : this.init_JsonUtil.init(hold.min(), hold.max());
               if (this.init_StreamUtil.init(now, holdNanos)) {
                  this.init_Z = true;
                  this.flush_J = now;
                  this.write_I = tick;
                  this.update_I = followUp > 0 ? tick + followUp : -2147483648;
                  this.sync_I = target.getId();
               }
            }
         }
      }
   }

   private long init_J_7() {
      return autismclient.util.clicker.ClickTimingMath.load(15.0 + 20.0 * this.init_Random.nextDouble());
   }

   private void init_V_31() {
      this.open = ValueRange.parse(this.value("pre-reach"), update_ValueRange).random(this.init_Random);
   }

   private void load_V_23() {
      this.init_Z = false;
      ValueRange offset = ValueRange.parse(this.value("swing-offset"), load_ValueRange);
      double hesitation = ValueRange.parse(this.value("hesitation"), read_ValueRange).random(this.init_Random) * 0.01;
      this.flush_D = this.init_JsonUtil.init(offset.min(), offset.max(), hesitation);
      int tick = AutismSessionState.init_ck().state_I();
      if (this.flush_I != -2147483648 && tick - this.flush_I <= 2) {
         this.flush_D = -1.0 / 0.0;
      }

      this.flush_I = -2147483648;
   }

   private static double init_D_10() {
      double ms = MC.level == null ? 50.0 : Math.max(50.0, MC.level.tickRateManager().millisecondsPerTick());
      return ms / Math.max(0.05, MovementModuleHooks.init_F());
   }

   private boolean load_Z_15() {
      float health = MC.player.getHealth();
      boolean hit = this.init_F >= 0.0F && health < this.init_F;
      this.init_F = health;
      return hit;
   }

   private double init_D_LivingEntity_Z(LivingEntity target, boolean timed) {
      if (this.init_am == null) {
         this.init_am = autismclient.util.HumanHandProfile.init_am();
      }

      ValueRange reaction = ValueRange.parse(this.value(timed ? "reaction" : "legacy-reaction"), init_ValueRange);
      double trait = Math.abs(this.init_am.init_D() * 1000.0);
      double bias = 0.92 + 0.16 * (trait - Math.floor(trait));
      RotationMath.Rotation toTarget = RotationMath.lookAt(target.getBoundingBox().getCenter(), MC.player.getEyePosition());
      float angle = RotationMath.angularDistance(RotationMath.ofPlayer(MC.player), toTarget);
      return autismclient.util.clicker.ReactionDelayModel.init(this.init_Random, reaction.min(), reaction.max(), bias, angle);
   }

   private boolean init(Entity entity) {
      if (entity == null || entity == MC.player || !entity.isAlive() || entity.isSpectator()) {
         return false;
      }

      if (AntiBotTracker.load_Z(entity)) {
         return false;
      }

      if (TeamsModule.init(entity, "triggerbot")) {
         return false;
      }

      Set<String> ids = this.init_Set_10();
      if (ids.isEmpty()) {
         return false;
      }

      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      return ids.contains(id) || ids.contains(id.substring(id.indexOf(58) + 1));
   }

   private Set<String> init_Set_10() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.read_Str)) {
         return this.init_Set;
      }

      Set<String> normalized = new LinkedHashSet<>();

      for (String entry : entries) {
         if (entry != null) {
            String value = entry.trim().toLowerCase(Locale.ROOT);
            if (!value.isEmpty()) {
               normalized.add(value);
               int split = value.indexOf(58);
               if (split >= 0 && split + 1 < value.length()) {
                  normalized.add(value.substring(split + 1));
               }
            }
         }
      }

      this.read_Str = source;
      this.init_Set = Set.copyOf(normalized);
      return this.init_Set;
   }

   private void init_V(long now) {
      this.init_DataUtil.init_V_J_DataUtil_NetHelper(now, this.init_StreamUtil);
      this.init_StreamUtil.load_V();
      this.read();
   }

   private void read() {
      this.init_I = -1;
      this.update_J = 0L;
      this.sync_J = -9223372036854775808L;
      this.load_I = -1;
      this.init_Z = false;
      this.write_I = -2147483648;
      this.update_I = -2147483648;
      this.open = 0.0 / 0.0;
      this.load_Z = false;
   }
}
