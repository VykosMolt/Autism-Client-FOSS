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
   private static final String MODERN_COMBAT = "Modern Combat";
   private static final String LEGACY_COMBAT = "1.8 Combat";
   static final ValueRange REACTION_DEFAULT = new ValueRange(0, 0);
   static final ValueRange SWING_OFFSET_DEFAULT = new ValueRange(-75.0, 50.0);
   static final ValueRange HESITATION_DEFAULT = new ValueRange(0, 2);
   static final ValueRange MODERN_HOLD_DEFAULT = new ValueRange(60, 110);
   static final ValueRange update_ValueRange = new ValueRange(0.2, 0.6);
   static final ValueRange EDGE_TOLERANCE_DEFAULT = new ValueRange(0.08, 0.22);
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
   private final ClickerSettingsGroup combatClicker;
   private final HeldItemFilter heldItemFilter;
   private final autismclient.util.clicker.ClickPatternEngine patternEngine = new autismclient.util.clicker.ClickPatternEngine();
   private final autismclient.util.clicker.KeyClickOutput keyOutput = autismclient.util.clicker.KeyClickOutput.attack();
   private final autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper init_DataUtil_NetHelper = new autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper() {
      @Override
      public boolean init_Z_J(long now) {
         return Module.MC.hitResult != null && Module.MC.hitResult.getType() != Type.MISS || TriggerBotModule.this.keyOutput.init_Z_J(now);
      }

      @Override
      public void init_V_J(long now) {
         TriggerBotModule.this.keyOutput.init_V_J(now);
      }

      @Override
      public void onClick(long now) {
         TriggerBotModule.this.keyOutput.onClick(now);
      }

      @Override
      public long earliestClickNanos(long now) {
         return TriggerBotModule.this.keyOutput.earliestClickNanos(now);
      }
   };
   private final Random random = new Random();
   private final autismclient.util.clicker.SwingTimingModel swingTiming = new autismclient.util.clicker.SwingTimingModel(this.random);
   private final autismclient.util.clicker.DoubleClickSampler doubleClickSampler = new autismclient.util.clicker.DoubleClickSampler(this.random);
   private autismclient.util.HumanHandProfile handProfile;
   private int lastTargetId = -1;
   private long reactAt;
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
   private double edgeTolerance;
   private double open = 0.0 / 0.0;
   private long edgeRerollAt = -9223372036854775808L;
   private boolean load_Z;
   private float lastHealth = -1.0F;
   private String entityFilterSource = "";
   private Set<String> entityFilterIds = Set.of();

   TriggerBotModule() {
      super("trigger-bot", "TriggerBot", ae.init_ae, "Attacks configured entities when your crosshair is on them.");
      this.add(RegistryListSetting.entityTypes("entities", "Entities", "minecraft:player").description("Trigger entities").build());
      this.heldItemFilter = new HeldItemFilter(this);
      this.add(new RangeSetting("edge-tolerance", "Edge Tolerance", EDGE_TOLERANCE_DEFAULT, 0.0, 0.5, 0.01).description("Click just past the edge").build());
      this.add(new BoolSetting("respect-cooldown", "Respect Cooldown", true).group("Modern Combat").description("Wait for full charge").build());
      this.add(
         new RangeSetting("reaction", "Reaction", REACTION_DEFAULT, 0.0, 400.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay on a new target")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("swing-offset", "Swing Offset", SWING_OFFSET_DEFAULT, -150.0, 400.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Delay after full charge")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("hesitation", "Hesitation", HESITATION_DEFAULT, 0.0, 20.0, 0.5)
            .unit("%")
            .group("Modern Combat")
            .description("Chance of a late swing")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.add(
         new RangeSetting("modern-hold", "Hold", MODERN_HOLD_DEFAULT, 20.0, 200.0, 5.0)
            .unit("ms")
            .group("Modern Combat")
            .description("Button hold time")
            .visibleWhen(() -> this.bool("respect-cooldown"))
            .build()
      );
      this.combatClicker = new ClickerSettingsGroup(this, "1.8 Combat", ClickerSettingsGroup.DEFAULT_TECHNIQUE, () -> true, ClickerSettingsGroup.DEFAULT_CPS);
      this.add(
         new RangeSetting("legacy-reaction", "Reaction", REACTION_DEFAULT, 0.0, 400.0, 5.0)
            .unit("ms")
            .group("1.8 Combat")
            .description("Delay on a new target")
            .visibleWhen(() -> this.combatClicker.technique().init_Z())
            .build()
      );
      this.add(
         new RangeSetting("pre-reach", "Pre-Reach", update_ValueRange, 0.0, 1.5, 0.05).group("1.8 Combat").description("Swing before reach, blocks").build()
      );
      this.combatClicker.registerSections();
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
      return this.combatClicker == null ? null : this.combatClicker.init_Str_Str(settingId);
   }

   @Override
   protected boolean setExternalSettingValue(String settingId, String value) {
      return this.combatClicker != null && this.combatClicker.init(settingId, value);
   }

   @Override
   public void onInputFrame() {
      long now = System.nanoTime();
      this.keyOutput.init_V();
      if (this.init_Z_35()) {
         this.init_V(now);
      } else {
         boolean noCooldown = autismclient.util.LegacyCombatDetector.init(MC.player);
         boolean timed = !noCooldown && this.bool("respect-cooldown");
         boolean rage = !timed && this.combatClicker.technique() == autismclient.util.clicker.ClickTechnique.RAGE;
         boolean flinched = this.tookDamage();
         boolean physical = this.keyOutput.isPhysicallyDown();
         if (physical && !this.load_Z && !timed) {
            this.patternEngine.init_V_J(now);
         }

         this.load_Z = physical;
         autismclient.util.clicker.ClickProfile profile = this.combatClicker.profile();
         this.patternEngine.setPersonality(this.combatClicker.init_DataUtil_ConfigSto());
         LivingEntity target = this.init(now, noCooldown, rage);
         boolean engaged = false;
         if (target == null && !timed && !rage) {
            target = this.resolveTarget(now);
            engaged = target != null;
         }

         if (target == null) {
            if (this.sync_J == -9223372036854775808L) {
               this.sync_J = now;
            }

            this.patternEngine.init(now, false, profile, this.keyOutput);
            this.keyOutput.read(now);
         } else {
            boolean sameTarget = target.getId() == this.lastTargetId && (this.sync_J == -9223372036854775808L || now - this.sync_J <= 300000000L);
            if (!sameTarget) {
               this.lastTargetId = target.getId();
               this.rollPreReach();
               this.reactAt = rage ? now : now + (long)(this.reactionDelayMs(target, timed) * 1000000.0);
               this.rollSwingOffset();
            }

            this.sync_J = -9223372036854775808L;
            boolean reacted = now >= this.reactAt;
            if (timed) {
               this.patternEngine.init(now, false, profile, this.keyOutput);
               if (flinched && this.random.nextBoolean()) {
                  this.flush_D = this.flush_D + (40.0 + 80.0 * this.random.nextDouble());
               }

               this.init(now, reacted, target);
               this.keyOutput.read(now);
            } else {
               this.keyOutput.read(now);
               this.patternEngine.init(now, reacted, profile, engaged ? this.init_DataUtil_NetHelper : this.keyOutput);
            }
         }
      }
   }

   private LivingEntity resolveTarget(long now) {
      if (this.lastTargetId >= 0 && MC.level != null) {
         if (!(MC.level.getEntity(this.lastTargetId) instanceof LivingEntity living && this.init(living))) {
            return null;
         } else if (autismclient.util.RecentCombatTracker.init(living, now)) {
            return null;
         } else {
            Vec3 eyes = MC.player.getEyePosition();
            AABB box = living.getBoundingBox();
            double near = autismclient.util.AttackReachHelper.maxReach(MC.player) + (Double.isNaN(this.open) ? 0.0 : this.open) + 1.5;
            if (box.distanceToSqr(eyes) > near * near) {
               return null;
            } else {
               return angleToCenter(eyes, box) <= 30.0 ? living : null;
            }
         }
      } else {
         return null;
      }
   }

   private static double angleToCenter(Vec3 eyes, AABB box) {
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
         || !this.heldItemFilter.accepts(MC.player.getMainHandItem());
   }

   private LivingEntity init(long now, boolean noCooldown, boolean rage) {
      if (now >= this.edgeRerollAt) {
         this.edgeTolerance = ValueRange.parse(this.value("edge-tolerance"), EDGE_TOLERANCE_DEFAULT).random(this.random);
         this.edgeRerollAt = now + 80000000L + (long)(this.random.nextDouble() * 8.0E7);
      }

      if (Double.isNaN(this.open)) {
         this.rollPreReach();
      }

      HitResult live = MC.player.raycastHitResult(1.0F, MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity());
      MC.hitResult = live;
      if (live instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && this.init(living)) {
         return !MC.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)
               && !autismclient.util.AttackReachHelper.attackRange(MC.player).isInRange(MC.player, entityHit.getLocation())
            ? null
            : living;
      } else {
         if (MC.hitResult != null && MC.hitResult.getType() != Type.MISS) {
            return null;
         }

         double reach = autismclient.util.AttackReachHelper.maxReach(MC.player);
         if (!rage && this.edgeTolerance > 0.0) {
            LivingEntity graze = this.findGrazeTarget(reach + this.edgeTolerance, this.edgeTolerance);
            if (graze != null && !autismclient.util.RecentCombatTracker.init(graze, now)) {
               return graze;
            }
         }

         LivingEntity poke = noCooldown ? this.findGrazeTarget(reach + this.open, 0.0) : null;
         return poke != null && !autismclient.util.RecentCombatTracker.init(poke, now) ? poke : null;
      }
   }

   private LivingEntity findGrazeTarget(double distance, double grow) {
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
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.load_I >= 0 && ticker < this.load_I + (tick - this.read_I)) {
         this.rollSwingOffset();
      }

      this.load_I = ticker;
      this.read_I = tick;
      if (tick != this.write_I) {
         if (this.update_I != -2147483648 && tick >= this.update_I) {
            boolean onTarget = target.getId() == this.sync_I && MC.hitResult instanceof EntityHitResult hit && hit.getEntity() == target;
            if (onTarget && tick <= this.update_I + 1) {
               if (this.keyOutput.init(now, this.followUpDelayNanos())) {
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
                  autismclient.util.AttackReachHelper.maxReach(MC.player)
               );
            if (lastChance || autismclient.util.clicker.SwingTimingModel.init(shown, ticker, MC.player.getCurrentItemAttackStrengthDelay(), tickMillis(), offset)) {
               ValueRange hold = ValueRange.parse(this.value("modern-hold"), MODERN_HOLD_DEFAULT);
               int followUp = lastChance ? 0 : this.doubleClickSampler.init();
               long holdNanos = followUp > 0 ? this.followUpDelayNanos() : this.swingTiming.init(hold.min(), hold.max());
               if (this.keyOutput.init(now, holdNanos)) {
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

   private long followUpDelayNanos() {
      return autismclient.util.clicker.ClickTimingMath.load(15.0 + 20.0 * this.random.nextDouble());
   }

   private void rollPreReach() {
      this.open = ValueRange.parse(this.value("pre-reach"), update_ValueRange).random(this.random);
   }

   private void rollSwingOffset() {
      this.init_Z = false;
      ValueRange offset = ValueRange.parse(this.value("swing-offset"), SWING_OFFSET_DEFAULT);
      double hesitation = ValueRange.parse(this.value("hesitation"), HESITATION_DEFAULT).random(this.random) * 0.01;
      this.flush_D = this.swingTiming.init(offset.min(), offset.max(), hesitation);
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.flush_I != -2147483648 && tick - this.flush_I <= 2) {
         this.flush_D = -1.0 / 0.0;
      }

      this.flush_I = -2147483648;
   }

   private static double tickMillis() {
      double ms = MC.level == null ? 50.0 : Math.max(50.0, MC.level.tickRateManager().millisecondsPerTick());
      return ms / Math.max(0.05, MovementModuleHooks.speedTimerMultiplier());
   }

   private boolean tookDamage() {
      float health = MC.player.getHealth();
      boolean hit = this.lastHealth >= 0.0F && health < this.lastHealth;
      this.lastHealth = health;
      return hit;
   }

   private double reactionDelayMs(LivingEntity target, boolean timed) {
      if (this.handProfile == null) {
         this.handProfile = autismclient.util.HumanHandProfile.shared();
      }

      ValueRange reaction = ValueRange.parse(this.value(timed ? "reaction" : "legacy-reaction"), REACTION_DEFAULT);
      double trait = Math.abs(this.handProfile.fittsIntercept() * 1000.0);
      double bias = 0.92 + 0.16 * (trait - Math.floor(trait));
      RotationMath.Rotation toTarget = RotationMath.lookAt(target.getBoundingBox().getCenter(), MC.player.getEyePosition());
      float angle = RotationMath.angularDistance(RotationMath.ofPlayer(MC.player), toTarget);
      return autismclient.util.clicker.ReactionDelayModel.init(this.random, reaction.min(), reaction.max(), bias, angle);
   }

   private boolean init(Entity entity) {
      if (entity == null || entity == MC.player || !entity.isAlive() || entity.isSpectator()) {
         return false;
      }

      if (AntiBotTracker.shouldIgnoreEntity(entity)) {
         return false;
      }

      if (TeamsModule.init(entity, "triggerbot")) {
         return false;
      }

      Set<String> ids = this.parsedEntityFilter();
      if (ids.isEmpty()) {
         return false;
      }

      String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().toLowerCase(Locale.ROOT);
      return ids.contains(id) || ids.contains(id.substring(id.indexOf(58) + 1));
   }

   private Set<String> parsedEntityFilter() {
      List<String> entries = this.list("entities");
      String source = String.join("|", entries);
      if (source.equals(this.entityFilterSource)) {
         return this.entityFilterIds;
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

      this.entityFilterSource = source;
      this.entityFilterIds = Set.copyOf(normalized);
      return this.entityFilterIds;
   }

   private void init_V(long now) {
      this.patternEngine.init_V_J_DataUtil_NetHelper(now, this.keyOutput);
      this.keyOutput.release();
      this.read();
   }

   private void read() {
      this.lastTargetId = -1;
      this.reactAt = 0L;
      this.sync_J = -9223372036854775808L;
      this.load_I = -1;
      this.init_Z = false;
      this.write_I = -2147483648;
      this.update_I = -2147483648;
      this.open = 0.0 / 0.0;
      this.load_Z = false;
   }
}
