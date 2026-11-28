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
   static final ValueRange PRE_REACH_DEFAULT = new ValueRange(0.2, 0.6);
   static final ValueRange EDGE_TOLERANCE_DEFAULT = new ValueRange(0.08, 0.22);
   private static final long TARGET_MEMORY_NANOS = 300000000L;
   private static final long CLICK_LOCKOUT_NANOS = 250000000L;
   private static final double FLINCH_DELAY_MIN_MS = 40.0;
   private static final double FLINCH_DELAY_SPAN_MS = 80.0;
   private static final long read_J = 80000000L;
   private static final long write_J = 80000000L;
   private static final double ENGAGE_CONE_DEG = 30.0;
   private static final double write_D = 1.5;
   private static final double FOLLOW_UP_DELAY_MIN_MS = 15.0;
   private static final double sync_D = 35.0;
   private final ClickerSettingsGroup combatClicker;
   private final HeldItemFilter heldItemFilter;
   private final autismclient.util.clicker.ClickPatternEngine patternEngine = new autismclient.util.clicker.ClickPatternEngine();
   private final autismclient.util.clicker.KeyClickOutput keyOutput = autismclient.util.clicker.KeyClickOutput.attack();
   private final autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper crosshairGatedSink = new autismclient.util.clicker.ClickPatternEngine.Inner_NetHelper() {
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
   private long targetLostAt = -9223372036854775808L;
   private int lastAttackTicker = -1;
   private int lastTickerTick;
   private boolean awaitingSwing;
   private long lastClickNanos;
   private int lastClickTick = -2147483648;
   private int followUpTick = -2147483648;
   private int followUpTargetId = -1;
   private int followUpClickTick = -2147483648;
   private double swingOffset;
   private double edgeTolerance;
   private double extraReach = 0.0 / 0.0;
   private long edgeRerollAt = -9223372036854775808L;
   private boolean physicalPressed;
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
            .visibleWhen(() -> this.combatClicker.technique().isHumanized())
            .build()
      );
      this.add(
         new RangeSetting("pre-reach", "Pre-Reach", PRE_REACH_DEFAULT, 0.0, 1.5, 0.05).group("1.8 Combat").description("Swing before reach, blocks").build()
      );
      this.combatClicker.registerSections();
   }

   @Override
   public void onEnable() {
      this.resetState();
   }

   @Override
   public void onDisable() {
      this.resetState(System.nanoTime());
   }

   @Override
   public void onGameLeft() {
      this.resetState(System.nanoTime());
   }

   @Override
   protected String externalSettingValue(String settingId) {
      return this.combatClicker == null ? null : this.combatClicker.settingValue(settingId);
   }

   @Override
   protected boolean setExternalSettingValue(String settingId, String value) {
      return this.combatClicker != null && this.combatClicker.setSettingValue(settingId, value);
   }

   @Override
   public void onInputFrame() {
      long now = System.nanoTime();
      this.keyOutput.activate();
      if (this.shouldPause()) {
         this.resetState(now);
      } else {
         boolean noCooldown = autismclient.util.LegacyCombatDetector.isLegacyCombat(MC.player);
         boolean timed = !noCooldown && this.bool("respect-cooldown");
         boolean rage = !timed && this.combatClicker.technique() == autismclient.util.clicker.ClickTechnique.RAGE;
         boolean flinched = this.tookDamage();
         boolean physical = this.keyOutput.isPhysicallyDown();
         if (physical && !this.physicalPressed && !timed) {
            this.patternEngine.init_V_J(now);
         }

         this.physicalPressed = physical;
         autismclient.util.clicker.ClickProfile profile = this.combatClicker.profile();
         this.patternEngine.setPersonality(this.combatClicker.personality());
         LivingEntity target = this.pickTarget(now, noCooldown, rage);
         boolean engaged = false;
         if (target == null && !timed && !rage) {
            target = this.resolveTarget(now);
            engaged = target != null;
         }

         if (target == null) {
            if (this.targetLostAt == -9223372036854775808L) {
               this.targetLostAt = now;
            }

            this.patternEngine.init(now, false, profile, this.keyOutput);
            this.keyOutput.releaseIfDue(now);
         } else {
            boolean sameTarget = target.getId() == this.lastTargetId && (this.targetLostAt == -9223372036854775808L || now - this.targetLostAt <= 300000000L);
            if (!sameTarget) {
               this.lastTargetId = target.getId();
               this.rollPreReach();
               this.reactAt = rage ? now : now + (long)(this.reactionDelayMs(target, timed) * 1000000.0);
               this.rollSwingOffset();
            }

            this.targetLostAt = -9223372036854775808L;
            boolean reacted = now >= this.reactAt;
            if (timed) {
               this.patternEngine.init(now, false, profile, this.keyOutput);
               if (flinched && this.random.nextBoolean()) {
                  this.swingOffset = this.swingOffset + (40.0 + 80.0 * this.random.nextDouble());
               }

               this.tryAttack(now, reacted, target);
               this.keyOutput.releaseIfDue(now);
            } else {
               this.keyOutput.releaseIfDue(now);
               this.patternEngine.init(now, reacted, profile, engaged ? this.crosshairGatedSink : this.keyOutput);
            }
         }
      }
   }

   private LivingEntity resolveTarget(long now) {
      if (this.lastTargetId >= 0 && MC.level != null) {
         if (!(MC.level.getEntity(this.lastTargetId) instanceof LivingEntity living && this.isValidTarget(living))) {
            return null;
         } else if (autismclient.util.RecentCombatTracker.isFreshTarget(living, now)) {
            return null;
         } else {
            Vec3 eyes = MC.player.getEyePosition();
            AABB box = living.getBoundingBox();
            double near = autismclient.util.AttackReachHelper.maxReach(MC.player) + (Double.isNaN(this.extraReach) ? 0.0 : this.extraReach) + 1.5;
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

   private boolean shouldPause() {
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

   private LivingEntity pickTarget(long now, boolean noCooldown, boolean rage) {
      if (now >= this.edgeRerollAt) {
         this.edgeTolerance = ValueRange.parse(this.value("edge-tolerance"), EDGE_TOLERANCE_DEFAULT).random(this.random);
         this.edgeRerollAt = now + 80000000L + (long)(this.random.nextDouble() * 8.0E7);
      }

      if (Double.isNaN(this.extraReach)) {
         this.rollPreReach();
      }

      HitResult live = MC.player.raycastHitResult(1.0F, MC.getCameraEntity() == null ? MC.player : MC.getCameraEntity());
      MC.hitResult = live;
      if (live instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living && this.isValidTarget(living)) {
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
            if (graze != null && !autismclient.util.RecentCombatTracker.isFreshTarget(graze, now)) {
               return graze;
            }
         }

         LivingEntity poke = noCooldown ? this.findGrazeTarget(reach + this.extraReach, 0.0) : null;
         return poke != null && !autismclient.util.RecentCombatTracker.isFreshTarget(poke, now) ? poke : null;
      }
   }

   private LivingEntity findGrazeTarget(double distance, double grow) {
      RotationMath.Rotation look = RotationMath.ofPlayer(MC.player);
      Vec3 eyes = MC.player.getEyePosition();
      Vec3 end = eyes.add(Vec3.directionFromRotation(look.pitch(), look.yaw()).scale(distance));
      LivingEntity best = null;
      double bestDistance = 1.7976931348623157E308;

      for (Entity entity : MC.level.entitiesForRendering()) {
         if (entity instanceof LivingEntity living && living.isPickable() && this.isValidTarget(living)) {
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

   private void tryAttack(long now, boolean reacted, LivingEntity target) {
      int ticker = ((AutismLivingEntityAccessor)MC.player).autism$getAttackStrengthTicker();
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.lastAttackTicker >= 0 && ticker < this.lastAttackTicker + (tick - this.lastTickerTick)) {
         this.rollSwingOffset();
      }

      this.lastAttackTicker = ticker;
      this.lastTickerTick = tick;
      if (tick != this.lastClickTick) {
         if (this.followUpTick != -2147483648 && tick >= this.followUpTick) {
            boolean onTarget = target.getId() == this.followUpTargetId && MC.hitResult instanceof EntityHitResult hit && hit.getEntity() == target;
            if (onTarget && tick <= this.followUpTick + 1) {
               if (this.keyOutput.press(now, this.followUpDelayNanos())) {
                  this.followUpTick = -2147483648;
                  this.lastClickNanos = now;
                  this.lastClickTick = tick;
                  this.followUpClickTick = tick;
               }

               return;
            }

            this.followUpTick = -2147483648;
         }

         if (this.awaitingSwing) {
            if (now - this.lastClickNanos < 250000000L) {
               return;
            }

            this.awaitingSwing = false;
         }

         if (reacted) {
            boolean opening = autismclient.util.RecentCombatTracker.isFreshTarget(target, now);
            double scored = ticker + 0.5;
            double shown = opening ? scored : Math.max(scored, ticker + MC.getDeltaTracker().getGameTimeDeltaPartialTick(false));
            double offset = opening ? 0.0 : this.swingOffset;
            Vec3 step = new Vec3(target.getX() - target.xo, target.getY() - target.yo, target.getZ() - target.zo);
            boolean lastChance = MC.player.getAttackStrengthScale(0.5F) > 0.9F
               && MC.hitResult instanceof EntityHitResult hit
               && hit.getEntity() == target
               && autismclient.util.AttackReachHelper.leavesReachNextTick(
                  MC.player.getEyePosition(),
                  target.getBoundingBox(),
                  ShieldHitWindowHelper.sanitizeMotion(MC.player.getDeltaMovement()),
                  ShieldHitWindowHelper.sanitizeMotion(step),
                  autismclient.util.AttackReachHelper.maxReach(MC.player)
               );
            if (lastChance || autismclient.util.clicker.SwingTimingModel.isReady(shown, ticker, MC.player.getCurrentItemAttackStrengthDelay(), tickMillis(), offset)) {
               ValueRange hold = ValueRange.parse(this.value("modern-hold"), MODERN_HOLD_DEFAULT);
               int followUp = lastChance ? 0 : this.doubleClickSampler.sampleFollowUps();
               long holdNanos = followUp > 0 ? this.followUpDelayNanos() : this.swingTiming.sampleHoldNanos(hold.min(), hold.max());
               if (this.keyOutput.press(now, holdNanos)) {
                  this.awaitingSwing = true;
                  this.lastClickNanos = now;
                  this.lastClickTick = tick;
                  this.followUpTick = followUp > 0 ? tick + followUp : -2147483648;
                  this.followUpTargetId = target.getId();
               }
            }
         }
      }
   }

   private long followUpDelayNanos() {
      return autismclient.util.clicker.ClickTimingMath.msToNanos(15.0 + 20.0 * this.random.nextDouble());
   }

   private void rollPreReach() {
      this.extraReach = ValueRange.parse(this.value("pre-reach"), PRE_REACH_DEFAULT).random(this.random);
   }

   private void rollSwingOffset() {
      this.awaitingSwing = false;
      ValueRange offset = ValueRange.parse(this.value("swing-offset"), SWING_OFFSET_DEFAULT);
      double hesitation = ValueRange.parse(this.value("hesitation"), HESITATION_DEFAULT).random(this.random) * 0.01;
      this.swingOffset = this.swingTiming.sampleOffset(offset.min(), offset.max(), hesitation);
      int tick = AutismSessionState.getInstance().getClientTick();
      if (this.followUpClickTick != -2147483648 && tick - this.followUpClickTick <= 2) {
         this.swingOffset = -1.0 / 0.0;
      }

      this.followUpClickTick = -2147483648;
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
      return autismclient.util.clicker.ReactionDelayModel.sample(this.random, reaction.min(), reaction.max(), bias, angle);
   }

   private boolean isValidTarget(Entity entity) {
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

   private void resetState(long now) {
      this.patternEngine.init_V_J_DataUtil_NetHelper(now, this.keyOutput);
      this.keyOutput.release();
      this.resetState();
   }

   private void resetState() {
      this.lastTargetId = -1;
      this.reactAt = 0L;
      this.targetLostAt = -9223372036854775808L;
      this.lastAttackTicker = -1;
      this.awaitingSwing = false;
      this.lastClickTick = -2147483648;
      this.followUpTick = -2147483648;
      this.extraReach = 0.0 / 0.0;
      this.physicalPressed = false;
   }
}
