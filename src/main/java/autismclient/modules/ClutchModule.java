package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismEntityAccessor;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismLocalPlayerAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.PerTickPlacementLock;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.PlacedBlockTrailRenderer;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ToDoubleFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClutchModule extends Module implements SilentMovementCoordinator.Participant {
   private static final int init_I = 8;
   private static final int load_I = 8;
   private static final int read_I = 20;
   private static final int write_I = 4;
   private static final int update_I = 3;
   private static final int sync_I = 20;
   private static final double LAUNCH_MIN_VELOCITY_Y = 0.55;
   private static final int flush_I = 20;
   private static final int close_I = 10;
   private static final int OWN_STEER_WINDOW_TICKS = 160;
   private static final double load_D = 4.0;
   private final Map<Integer, ClutchModule.ProjectileSample> projectileSamples = new HashMap<>();
   private final Map<BlockPos, Integer> recentPlacementCells = new HashMap<>();
   private Object lastLevel;
   private ClutchFallPhysics.HitEvent launchCause;
   private int launchDamageTick = -2147483648;
   private int lastImpulseTick = -2147483648;
   private int engageTick;
   private int safeLandingTicks;
   private int lastPlaceTick = -2147483648;
   private boolean engaged;
   private boolean disengaging;
   private boolean landingSafe;
   private boolean dangerReturning;
   private boolean slotsReleased;
   private int disengageTicks;
   private int ticksToPlacement = 1;
   private int ownSteerHoldTicks;
   private int safeCatchTick = -2147483648;
   private boolean caughtSafely;
   private final ClutchFallPhysics.TickWindowLatch ownSteerWindow = new ClutchFallPhysics.TickWindowLatch(160);
   private Vec3 preImpulseMotion;
   private Vec3 postImpulseMotion = Vec3.ZERO;
   private int serverImpulseTick = -2147483648;
   private int unattributedHurtTick = -2147483648;
   private int disallowedCauseTick = -2147483648;
   private String causeDebug = "";
   private int causeDebugTick = -2147483648;
   private int originalHotbarSlot = -1;
   private int clutchSlot = -1;
   private int restoreToSlot = -1;
   private int restoreFromSlot = -1;
   private int slotRestoreTicksLeft;
   private ClutchModule.PlacementStep currentPlan;
   private BlockPos placedCatchCell;
   private BlockPos catchTarget;
   private BlockPos lastGroundPos;
   private BlockPos launchGroundPos;
   private int lastGroundTick = -2147483648;
   private int catchRowY;
   private boolean steerAxisX;
   private int catchMode;
   private boolean edgeRescue;
   private boolean lastShift;
   private boolean lastShiftOwned;
   private int crouchStartTick = -2147483648;
   private Vec3 nextEyePosition;
   private Boolean hitAxisZ;
   private boolean enemyLaunchedUp;
   private volatile ClutchModule.WireState wireState;
   private int chargeThrowTick = -2147483648;
   private int ownBoostDroppedTick = -2147483648;
   private boolean ignoredLaunch;
   private int ignoredLaunchTick = -2147483648;
   private int lastRouteTick = -2147483648;
   private int groundedLaunchTick = -2147483648;
   private Vec3 chargeThrowEye;
   private int ownBlastTick = -2147483648;
   private Vec3 ownBlastCenter;
   private static final int OWN_BLAST_WINDOW_TICKS = 100;
   private int rescues;
   private int placements;
   private String status = "Idle";
   private BlockHitResult sentPlacementHit;
   private BlockPos lastPlacedPos;
   private int placedSlot = -1;
   private int placementSequence = -1;
   private int placeHitTick = -2147483648;
   private BlockPos rowReturnPos;
   private static final double read_D = 0.05;
   private static final double write_D = 0.6;
   private static final double update_D = 0.15;
   private static final int table = 3;
   private static final int store = 8;
   private final ClutchModule.WorldCache worldCache = new ClutchModule.WorldCache();

   public ClutchModule() {
      super("clutch", "Clutch", ae.init_ae, "Saves you from knockback falls");
      this.migrateLegacySettings();
      this.add(new BoolSetting("melee", "Melee Hits", true).group("Triggers"));
      this.add(new BoolSetting("projectiles", "Projectiles", true).group("Triggers").description("Arrows, snowballs and eggs"));
      this.add(new BoolSetting("explosions", "Explosions", true).group("Triggers").description("Fireballs and wind charges"));
      this.add(new BoolSetting("own-knockback", "Own Falls", true).group("Triggers").description("Catch falls after your own jumps"));
      this.add(
         new BoolSetting("own-void-only", "Void Only", true)
            .group("Triggers")
            .description("Own falls into void only")
            .visibleWhen(() -> this.bool("own-knockback"))
      );
      this.add(
         new IntSetting("own-fall-height", "Fall Height", 10, 3, 40, 1)
            .group("Triggers")
            .description("Own drop that gets caught")
            .visibleWhen(() -> this.bool("own-knockback") && !this.bool("own-void-only"))
      );
      this.add(new IntSetting("safe-fall", "Safe Fall", 3, 1, 10, 1).group("Rescue").description("Harmless drop in blocks"));
      this.add(new DoubleSetting("guard-time", "Guard Time", 0.0, 0.0, 10.0, 0.5).group("Rescue").description("Protect jumps after rescue"));
      this.add(new BoolSetting("steer", "Enemy Movement", true).group("Rescue").description("Brake, jump and sneak after enemy attacks"));
      this.add(new BoolSetting("own-steer", "Own Fall Movement", false).group("Rescue").description("Brake, jump and sneak for your own falls"));
      this.add(new BoolSetting("restore-slot", "Restore Hotbar", true).group("Rescue"));
      this.add(new BoolSetting("place-animation", "Place Animation", true).group("Animation").description("Placement ripple effect"));
      this.add(new BoolSetting("animation-custom", "Custom Color", false).group("Animation").visibleWhen(() -> this.bool("place-animation")));
      this.add(
         new ColorSetting("animation-color", "Color", -50373)
            .group("Animation")
            .visibleWhen(() -> this.bool("place-animation") && this.bool("animation-custom"))
      );
   }

   @Override
   public void onEnable() {
      this.ownSteerWindow.clear();
      this.syncTrailRenderer();
   }

   @Override
   protected void onOptionValueChanged(String settingId) {
      this.syncTrailRenderer();
   }

   private void syncTrailRenderer() {
      PlacedBlockTrailRenderer.init(
         PlacedBlockTrailRenderer.Inner_DataUtil.CLUTCH,
         this.isEnabled() && this.bool("place-animation"),
         this.bool("animation-custom"),
         am.init_I_Module(this, "animation-color", -50373)
      );
   }

   private void migrateLegacySettings() {
      AutismConfig.ModuleState saved = AutismConfig.getGlobal().modules.get(this.id());
      if (saved != null && saved.settings != null) {
         Map<String, String> settings = saved.settings;
         if (!settings.containsKey("projectiles") && (settings.containsKey("arrows") || settings.containsKey("snowballs"))) {
            settings.put("projectiles", Boolean.toString(!"false".equals(settings.get("arrows")) || !"false".equals(settings.get("snowballs"))));
         }

         if (!settings.containsKey("explosions") && settings.containsKey("fireballs")) {
            settings.put("explosions", settings.get("fireballs"));
         }
      }
   }

   public static ClutchModule instance() {
      return al.init_Module_Str("clutch") instanceof ClutchModule clutch ? clutch : null;
   }

   public static boolean init_Z_49() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && !clutch.slotsReleased && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean read_Z_20() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && !clutch.disengaging && clutch.canSteer() && clutch.catchMode != 3 && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean sync_Z_10() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && !clutch.disengaging && clutch.canSteer() && clutch.catchMode != 3 && !ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 4);
   }

   public static boolean flush_Z_9() {
      ClutchModule clutch = instance();
      return clutch != null
         && clutch.isEnabled()
         && clutch.canAct()
         && (clutch.engaged && !clutch.disengaging ? clutch.canSteer() && clutch.catchMode != 3 : clutch.ownSteerHoldTicks > 0 && clutch.ownSteerArmed());
   }

   public static boolean close_Z_8() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean open_Z_5() {
      ClutchModule clutch = instance();
      return init_Z_49() && (clutch.landingSafe || clutch.disengaging || clutch.dangerReturning);
   }

   @Override
   public boolean isSilentMovementActive() {
      return (close_Z_8() || this.rotationReturning()) && !aD.init_Z() && !FreecamController.isActive();
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.engaged || this.rotationReturning() || this.restoreToSlot >= 0;
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC.player != null) {
         if (this.engaged) {
            this.disengage();
         }

         this.tickRotationReturn();
         this.tickSlotRestore();
      }
   }

   public String getStatus() {
      return this.status;
   }

   public boolean isOwnSteerActive() {
      return this.ownSteerWindow.isActive(this.clientTick());
   }

   public int rescueCount() {
      return this.rescues;
   }

   public int placementCount() {
      return this.placements;
   }

   private int clientTick() {
      return AutismSessionState.getInstance().getClientTick();
   }

   private boolean isAllowedCause(ClutchFallPhysics.HitEvent value) {
      return ClutchFallPhysics.isAllowedCause(value, this.bool("own-knockback"), this.bool("melee"), this.bool("projectiles"), this.bool("explosions"));
   }

   private boolean canSteer() {
      return ClutchFallPhysics.isSourceEnabled(this.launchCause, this.bool("steer"), this.bool("own-steer"))
         && (!ClutchFallPhysics.isSelfOrGuardCause(this.launchCause) || this.ownSteerWindow.isActive(this.clientTick()));
   }

   private boolean ownSteerArmed() {
      return this.ownSteerWindow.isActive(this.clientTick()) && this.bool("own-steer") && (this.bool("own-knockback") || this.inGuardTime() || this.recentlyCaught());
   }

   public static void init_V_LivingEntity_5(LivingEntity entity) {
      ClutchModule clutch = instance();
      if (clutch != null
         && clutch.isEnabled()
         && entity == MC.player
         && MC.level != null
         && !(entity.getDeltaMovement().y <= 0.0)
         && !(((AutismLivingEntityAccessor)entity).autism$getJumpPower() <= 1.0E-5F)) {
         clutch.resetOnLevelChange();
         if (!clutch.freshEnemyLaunch() && (!clutch.engaged || ClutchFallPhysics.isSelfOrGuardCause(clutch.launchCause))) {
            clutch.ownSteerWindow.trigger(clutch.clientTick());
         }
      }
   }

   private boolean rotationReturning() {
      return "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.isReturningToPlayer();
   }

   public static void onDamageEvent(ClientboundDamageEventPacket packet) {
      ClutchModule clutch = instance();
      if (clutch != null && clutch.isEnabled() && MC.player != null && MC.level != null && packet.entityId() == MC.player.getId()) {
         clutch.resetOnLevelChange();
         DamageSource damage = packet.getSource(MC.level);
         Entity direct = damage.getDirectEntity();
         Entity owner = damage.getEntity();
         ClutchFallPhysics.HitCause cause = causeOf(direct);
         if (cause == ClutchFallPhysics.HitCause.OTHER && damage.is(DamageTypes.ARROW)) {
            cause = ClutchFallPhysics.HitCause.ARROW;
         }

         if (cause == ClutchFallPhysics.HitCause.OTHER && (damage.is(DamageTypes.PLAYER_ATTACK) || damage.is(DamageTypes.MOB_ATTACK))) {
            cause = ClutchFallPhysics.HitCause.MELEE;
         }

         ClutchModule.ProjectileSample seen = direct == null ? null : clutch.projectileSamples.get(direct.getId());
         boolean own = owner == MC.player || owner == null && seen != null && seen.own;
         ClutchFallPhysics.HitEvent hit = new ClutchFallPhysics.HitEvent(
            cause, own, cause != ClutchFallPhysics.HitCause.OTHER || owner instanceof LivingEntity || direct instanceof Projectile && owner != null, clutch.clientTick()
         );
         if (cause != ClutchFallPhysics.HitCause.OTHER || direct instanceof Projectile) {
            clutch.disallowedCauseTick = clutch.isAllowedCause(hit) ? -2147483648 : clutch.clientTick();
            if (!clutch.isAllowedCause(hit)) {
               clutch.markIgnoredLaunch();
            }
         }

         if (clutch.isAllowedCause(hit)) {
            clutch.launchCause = hit;
            clutch.launchDamageTick = clutch.clientTick();
            clutch.launchGroundPos = clutch.lastGroundPos;
            clutch.lastImpulseTick = clutch.clientTick();
            clutch.rearmForEnemyHit();
            if (!hit.own()) {
               clutch.ownBoostDroppedTick = -2147483648;
            }
         } else if (hit.own() && cause != ClutchFallPhysics.HitCause.OTHER) {
            clutch.dropOwnBoost();
         }

         clutch.causeDebug = "damage "
            + cause
            + " owner="
            + (owner == null ? "-" : owner.getType().toShortString())
            + " direct="
            + (direct == null ? "-" : direct.getType().toShortString())
            + " eligible="
            + clutch.isAllowedCause(hit);
         clutch.causeDebugTick = clutch.clientTick();
         if (!clutch.isAllowedCause(hit) && cause == ClutchFallPhysics.HitCause.OTHER && owner == null && direct == null) {
            clutch.unattributedHurtTick = clutch.clientTick();
            if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.serverImpulseTick, 2)) {
               clutch.onServerImpulse(true);
            }
         }
      }
   }

   public static void init_V_I_11(int entityId) {
      ClutchModule clutch = instance();
      if (clutch != null && clutch.isEnabled() && MC.player != null && entityId == MC.player.getId()) {
         clutch.resetOnLevelChange();
         clutch.preImpulseMotion = MC.player.getDeltaMovement();
      }
   }

   public static void load_V_I_3(int entityId) {
      ClutchModule clutch = instance();
      if (clutch != null && clutch.isEnabled() && MC.player != null && entityId == MC.player.getId()) {
         clutch.resetOnLevelChange();
         clutch.lastImpulseTick = clutch.clientTick();
         Vec3 before = clutch.preImpulseMotion == null ? Vec3.ZERO : clutch.preImpulseMotion;
         clutch.preImpulseMotion = null;
         Vec3 push = MC.player.getDeltaMovement().subtract(before);
         boolean impulse = ClutchFallPhysics.hasImpulseChange(before, MC.player.getDeltaMovement(), ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.unattributedHurtTick, 3));
         if (impulse) {
            clutch.serverImpulseTick = clutch.clientTick();
            clutch.postImpulseMotion = MC.player.getDeltaMovement();
         }

         if (impulse || clutch.causeDebug.isEmpty()) {
            String line = String.format(Locale.ROOT, "push %.2f/%.2f before=%s", push.horizontalDistance(), push.y, before == Vec3.ZERO ? "none" : "seen");
            clutch.causeDebug = clutch.causeDebugTick == clutch.clientTick() ? clutch.causeDebug + " | " + line : line;
            clutch.causeDebugTick = clutch.clientTick();
         }

         if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 3)) {
            clutch.dropOwnBoost();
         } else {
            ClutchModule.ProjectileSample nearest = null;
            double score = 1.0 / 0.0;

            for (ClutchModule.ProjectileSample sample : clutch.projectileSamples.values()) {
               if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), sample.tick, 4) && !(sample.velocity.lengthSqr() < 0.001)) {
                  double distance = segmentTouchCost(MC.player.getBoundingBox().inflate(0.45), sample.position, sample.position.add(sample.velocity.scale(4.0)));
                  if (Double.isFinite(distance)) {
                     distance = sample.position.distanceToSqr(MC.player.getEyePosition()) * 0.001;
                  }

                  if (distance < score) {
                     score = distance;
                     nearest = sample;
                  }
               }
            }

            if (nearest != null && score <= 0.025) {
               ClutchFallPhysics.HitEvent hit = new ClutchFallPhysics.HitEvent(
                  nearest.cause, nearest.own || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 3), nearest.attributed || impulse, clutch.clientTick()
               );
               boolean enemyDamage = clutch.launchCause != null && !clutch.launchCause.own() && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchDamageTick, 2);
               if (nearest.own && !enemyDamage || clutch.launchCause == null || !ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 4)) {
                  clutch.causeDebug = clutch.causeDebug + " projectile " + nearest.cause + (nearest.own ? " own" : "");
                  clutch.launchCause = clutch.isAllowedCause(hit) ? hit : null;
               }

               if (clutch.isAllowedCause(hit)) {
                  clutch.launchGroundPos = clutch.lastGroundPos;
               } else if (nearest.own && !enemyDamage) {
                  clutch.dropOwnBoost();
               }
            }

            boolean excluded = ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.disallowedCauseTick, 3)
               || nearest != null
                  && !clutch.isAllowedCause(new ClutchFallPhysics.HitEvent(nearest.cause, nearest.own, nearest.attributed || impulse, clutch.clientTick()));
            if (!impulse
               || excluded
               || clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               if (impulse && excluded) {
                  clutch.markIgnoredLaunch();
               }
            } else {
               clutch.onServerImpulse(ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.unattributedHurtTick, 3));
            }

            if (clutch.engaged && clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               clutch.rearmForEnemyHit();
            }

            if (clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               clutch.markGroundedLaunch();
            }
         }
      }
   }

   public static void onExplosion(ClientboundExplodePacket packet) {
      ClutchModule clutch = instance();
      if (clutch != null && clutch.isEnabled() && MC.player != null) {
         clutch.resetOnLevelChange();
         ClutchModule.ProjectileSample nearest = null;
         double best = 16.0;

         for (ClutchModule.ProjectileSample sample : clutch.projectileSamples.values()) {
            if ((sample.cause == ClutchFallPhysics.HitCause.FIREBALL || sample.cause == ClutchFallPhysics.HitCause.WIND)
               && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), sample.tick, 5)) {
               double distance = sample.position.distanceToSqr(packet.center());
               if (distance < best) {
                  nearest = sample;
                  best = distance;
               }
            }
         }

         if (!packet.playerKnockback().isEmpty() && !(packet.playerKnockback().get().lengthSqr() < 0.0025)) {
            boolean ownBlast = clutch.ownBlastCenter != null
                  && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBlastTick, 100)
                  && clutch.ownBlastCenter.distanceToSqr(packet.center()) <= 64.0
               || clutch.nearOwnChargeThrow(packet.center());
            boolean own = nearest != null
               ? nearest.own || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 3)
               : ownBlast || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 3);
            ClutchFallPhysics.HitEvent hit = new ClutchFallPhysics.HitEvent(
               nearest != null ? nearest.cause : ClutchFallPhysics.HitCause.BLAST, own, nearest == null || nearest.attributed, clutch.clientTick()
            );
            if (nearest == null && !clutch.isAllowedCause(hit)) {
               boolean enemyHit = clutch.launchCause != null
                  && !clutch.launchCause.own()
                  && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 4);
               if (own && !enemyHit) {
                  clutch.dropOwnBoost();
               } else {
                  clutch.markIgnoredLaunch();
               }
            } else {
               clutch.disallowedCauseTick = clutch.isAllowedCause(hit) ? -2147483648 : clutch.clientTick();
               if (!clutch.isAllowedCause(hit)) {
                  clutch.markIgnoredLaunch();
               }

               clutch.launchCause = clutch.isAllowedCause(hit) ? hit : null;
               clutch.lastImpulseTick = clutch.clientTick();
               if (clutch.isAllowedCause(hit)) {
                  clutch.launchGroundPos = clutch.lastGroundPos;
               } else if (own) {
                  clutch.dropOwnBoost();
               }

               if (clutch.engaged && clutch.isAllowedCause(hit)) {
                  clutch.rearmForEnemyHit();
               }

               if (clutch.isAllowedCause(hit)) {
                  clutch.markGroundedLaunch();
               }
            }
         } else {
            boolean ours = nearest != null ? nearest.own : clutch.nearOwnChargeThrow(packet.center());
            if (ours && !clutch.engaged && !clutch.freshEnemyLaunch()) {
               clutch.dropOwnBoost();
            }
         }
      }
   }

   private void dropOwnBoost() {
      if (this.launchCause != null) {
         this.causeDebug = this.causeDebug + " own boost dropped " + this.launchCause.cause();
      }

      this.launchCause = null;
      this.launchDamageTick = -2147483648;
      this.ownBoostDroppedTick = this.clientTick();
      this.groundedLaunchTick = -2147483648;
      this.markIgnoredLaunch();
      if (this.engaged) {
         autismclient.util.ClientInteractionOverride.clearAll();
         this.disengage();
      }
   }

   private void markIgnoredLaunch() {
      this.ignoredLaunch = true;
      this.ignoredLaunchTick = this.clientTick();
   }

   private boolean nearOwnChargeThrow(Vec3 near) {
      return this.chargeThrowEye != null && near != null && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.chargeThrowTick, 20) && this.chargeThrowEye.distanceToSqr(near) <= 64.0;
   }

   private boolean freshEnemyLaunch() {
      return ClutchFallPhysics.isEnemyCause(this.launchCause) && this.isAllowedCause(this.launchCause) && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.launchCause.tick(), 4);
   }

   private void onServerImpulse(boolean hurt) {
      if (this.bool("melee") && MC.level != null && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.ownBoostDroppedTick, 3) && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.disallowedCauseTick, 3)) {
         if (!this.engaged && this.postImpulseMotion.y >= 0.55 && this.nearOwnChargeThrow(MC.player.position())) {
            this.causeDebug = this.causeDebug + " own boost";
            this.dropOwnBoost();
         } else {
            this.causeDebug = this.causeDebug + " server impulse hurt=" + hurt;
            this.launchCause = new ClutchFallPhysics.HitEvent(ClutchFallPhysics.HitCause.SERVER_PUSH, false, true, this.clientTick());
            this.launchDamageTick = this.lastImpulseTick = this.clientTick();
            this.launchGroundPos = this.lastGroundPos;
            this.rearmForEnemyHit();
            this.markGroundedLaunch();
         }
      }
   }

   private void rearmForEnemyHit() {
      if (this.engaged && ClutchFallPhysics.isEnemyCause(this.launchCause)) {
         if (this.catchMode == 0 || this.catchMode == 3) {
            this.steerAxisX = this.pickSteerAxis();
         }

         this.catchMode = ClutchFallPhysics.normalizeMode(this.catchMode);
         this.disengaging = false;
         this.disengageTicks = 0;
         this.safeLandingTicks = 0;
         this.engageTick = this.clientTick();
      }
   }

   private boolean inKnockbackLaunch(int tick) {
      return ClutchFallPhysics.isEnemyCause(this.launchCause) && !MC.player.onGround() && ClutchFallPhysics.isRecentKnockbackLaunch(tick, this.launchCause.tick(), this.serverImpulseTick, this.lastGroundTick, 160);
   }

   private boolean inGuardTime() {
      int ticks = (int)Math.round(this.decimal("guard-time") * 20.0);
      return ticks > 0 && this.safeCatchTick != -2147483648 && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.safeCatchTick, ticks);
   }

   private void markGroundedLaunch() {
      float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
      if (MC.level != null
         && MC.player.onGround()
         && MC.player.getDeltaMovement().y < jump
         && !MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(0.0, -0.08, 0.0))) {
         this.groundedLaunchTick = this.clientTick();
      }
   }

   private boolean shouldJumpOnLaunch(boolean stocked, int mode) {
      if (stocked && mode != 2 && !this.disengaging && MC.player.onGround() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() <= 1) {
         return ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.groundedLaunchTick, 2) ? true : this.engaged && !this.landingSafe && ClutchFallPhysics.isEnemyCause(this.launchCause) && !this.isGroundedSafe();
      } else {
         return false;
      }
   }

   private static double segmentTouchCost(AABB box, Vec3 from, Vec3 to) {
      return !box.contains(from) && !box.clip(from, to).isPresent() ? 1.0 / 0.0 : 0.0;
   }

   private static ClutchFallPhysics.HitCause causeOf(Entity entity) {
      if (entity instanceof Fireball) {
         return ClutchFallPhysics.HitCause.FIREBALL;
      } else if (entity instanceof AbstractWindCharge) {
         return ClutchFallPhysics.HitCause.WIND;
      } else if (entity instanceof AbstractArrow) {
         return ClutchFallPhysics.HitCause.ARROW;
      } else {
         return !(entity instanceof Snowball) && !(entity instanceof ThrownEgg) ? ClutchFallPhysics.HitCause.OTHER : ClutchFallPhysics.HitCause.SNOWBALL;
      }
   }

   private void resetOnLevelChange() {
      if (this.lastLevel != MC.level) {
         this.resetAll();
         this.lastLevel = MC.level;
      }
   }

   public static void init_V_Vec3(Vec3 before) {
      ClutchModule clutch = instance();
      if (clutch != null && MC.player != null && before != null) {
         if (MC.player.position().distanceToSqr(before) > 16.0) {
            if (clutch.engaged) {
               autismclient.util.ClientInteractionOverride.clearAll();
            }

            int slot = clutch.restoreToSlot;
            int from = clutch.restoreFromSlot;
            if (slot < 0 && clutch.engaged && clutch.bool("restore-slot") && clutch.originalHotbarSlot >= 0 && clutch.clutchSlot >= 0 && clutch.originalHotbarSlot != clutch.clutchSlot) {
               slot = clutch.originalHotbarSlot;
               from = clutch.clutchSlot;
            }

            clutch.resetAll();
            clutch.lastLevel = MC.level;
            if (slot >= 0) {
               clutch.restoreToSlot = slot;
               clutch.restoreFromSlot = from;
               clutch.slotRestoreTicksLeft = 20;
            }
         } else {
            clutch.wireState = null;
            clutch.currentPlan = null;
         }
      }
   }

   private void pruneSamples() {
      int tick = this.clientTick();
      this.projectileSamples.values().removeIf(sample -> !ClutchFallPhysics.isWithinTickWindow(tick, sample.tick, 5));

      for (Entity entity : MC.level
         .getEntities(MC.player, MC.player.getBoundingBox().inflate(10.0), candidate -> causeOf(candidate) != ClutchFallPhysics.HitCause.OTHER)) {
         Projectile projectile = (Projectile)entity;
         Entity owner = projectile.getOwner();
         ClutchModule.ProjectileSample old = this.projectileSamples.get(entity.getId());
         boolean own = owner != null ? owner == MC.player : old != null && old.own;
         boolean launched = causeOf(entity) == ClutchFallPhysics.HitCause.FIREBALL || causeOf(entity) == ClutchFallPhysics.HitCause.WIND;
         if (old == null
            && owner == null
            && launched
            && this.chargeThrowEye != null
            && ClutchFallPhysics.isWithinTickWindow(tick, this.chargeThrowTick, 8)
            && entity.position().distanceToSqr(this.chargeThrowEye) < 9.0) {
            own = true;
         }

         boolean incoming = entity.position().distanceToSqr(MC.player.getEyePosition()) > 9.0
            && entity.getDeltaMovement().dot(MC.player.getEyePosition().subtract(entity.position())) > 0.05;
         boolean attributed = own || owner instanceof LivingEntity || old != null && old.attributed || incoming;
         this.projectileSamples
            .put(
               entity.getId(),
               new ClutchModule.ProjectileSample(entity.getId(), causeOf(entity), own, attributed, entity.position(), entity.getDeltaMovement(), tick)
            );
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket use
         && use.hand() == InteractionHand.MAIN_HAND
         && use.hitResult() == this.sentPlacementHit
         && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.placeHitTick, 1)) {
         this.placementSequence = use.sequence();
      }

      InteractionHand hand = packet instanceof ServerboundUseItemPacket use
         ? use.hand()
         : (packet instanceof ServerboundUseItemOnPacket use ? use.hand() : null);
      if (MC.player != null && hand != null && (MC.player.getItemInHand(hand).is(Items.FIRE_CHARGE) || MC.player.getItemInHand(hand).is(Items.WIND_CHARGE))) {
         this.chargeThrowTick = this.clientTick();
         this.chargeThrowEye = MC.player.getEyePosition();
      }

      if (MC.player != null && MC.level != null && packet instanceof ServerboundUseItemOnPacket use) {
         BlockPos clicked = use.hitResult().getBlockPos();
         BlockState block = MC.level.getBlockState(clicked);
         ItemStack held = MC.player.getItemInHand(use.hand());
         if (held.is(Items.TNT)
            || held.is(Items.END_CRYSTAL)
            || held.is(Items.RESPAWN_ANCHOR)
            || held.is(Items.FLINT_AND_STEEL)
            || held.is(Items.FIRE_CHARGE)
            || held.is(ItemTags.BEDS)
            || block.is(Blocks.TNT)
            || block.is(Blocks.RESPAWN_ANCHOR)
            || block.is(BlockTags.BEDS)) {
            this.ownBlastTick = this.clientTick();
            this.ownBlastCenter = Vec3.atCenterOf(clicked);
         }
      }

      if (MC.level != null && packet instanceof ServerboundAttackPacket attack && MC.level.getEntity(attack.entityId()) instanceof EndCrystal crystal) {
         this.ownBlastTick = this.clientTick();
         this.ownBlastCenter = crystal.position();
      }

      return false;
   }

   public static void onUseItemOnResult(InteractionHand hand, BlockHitResult hit, InteractionResult result) {
      ClutchModule clutch = instance();
      if (clutch != null
         && hit != null
         && hand == InteractionHand.MAIN_HAND
         && hit == clutch.sentPlacementHit
         && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.placeHitTick, 1)) {
         if (result != null && result.consumesAction()) {
            ScaffoldModule.init_V_I_I(clutch.placedSlot, clutch.placementSequence);
            if (clutch.lastPlacedPos != null && MC.level != null && !MC.level.getBlockState(clutch.lastPlacedPos).isAir()) {
               PlacedBlockTrailRenderer.init(PlacedBlockTrailRenderer.Inner_DataUtil.CLUTCH, clutch.lastPlacedPos);
            }
         }

         clutch.sentPlacementHit = null;
         clutch.lastPlacedPos = null;
         clutch.placedSlot = clutch.placementSequence = -1;
      }
   }

   private boolean canAct() {
      return MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && MC.player.isAlive()
         && !MC.player.isSpectator()
         && !MC.player.isPassenger()
         && !MC.player.getAbilities().flying
         && !MC.player.isFallFlying()
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && MC.gui.screen() == null
         && MC.gui.overlay() == null
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && !BlinkPacketManager.isBlinking()
         && !PkgCls_a.init_Z()
         && !MacroExecutor.isRunning()
         && !AutoTotemModule.init_Z_6();
   }

   private static boolean scaffoldEnabled() {
      Module module = al.init_Module_Str("scaffold");
      return module != null && module.isEnabled();
   }

   @Override
   public void preMovementTick() {
      this.worldCache.begin();

      try {
         this.runPreMovementTick();
      } finally {
         this.worldCache.deactivate();
      }
   }

   private void runPreMovementTick() {
      if (MC.player != null && MC.level != null) {
         this.resetOnLevelChange();
         this.dangerReturning = false;
         this.tickRotationReturn();
         this.tickSlotRestore();
         if (this.ignoredLaunch && (MC.player.onGround() || MC.player.isInWater() || MC.player.isInLava()) && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.ignoredLaunchTick, 2)) {
            this.ignoredLaunch = false;
         }

         if (!MC.player.isInWater() && !MC.player.isInLava()) {
            this.ownSteerWindow.update(this.clientTick(), MC.player.onGround(), MC.player.getDeltaMovement().y);
         } else {
            this.ownSteerWindow.clear();
         }

         if (this.isEnabled() && this.canAct()) {
            if (this.edgeRescue && !this.canSteer()) {
               this.edgeRescue = false;
            }

            this.pruneSamples();
            int tick = this.clientTick();
            if (MC.player.onGround()) {
               this.updateLastGround(tick);
            }

            if (!this.engaged
               && ClutchFallPhysics.isEnemyCause(this.launchCause)
               && !ClutchFallPhysics.isWithinTickWindow(tick, this.launchCause.tick(), 2)
               && !ClutchFallPhysics.isWithinTickWindow(tick, this.lastImpulseTick, 2)
               && this.isGroundedSafe()) {
               this.launchCause = null;
               this.launchDamageTick = -2147483648;
               this.enemyLaunchedUp = false;
            }

            if (this.edgeRescue && this.placedCatchCell != null && !this.placedThisTick(tick)) {
               this.restoreSlot();
               this.disengage();
            } else {
               if (this.edgeRescue && !MC.player.onGround()) {
                  this.edgeRescue = false;
               }

               this.recentPlacementCells.entrySet().removeIf(entry -> tick - entry.getValue() > 1);
               boolean freshHit = this.isAllowedCause(this.launchCause)
                  && (ClutchFallPhysics.isWithinTickWindow(tick, this.launchCause.tick(), 24) && ClutchFallPhysics.isWithinTickWindow(tick, this.lastImpulseTick, 24) || this.inKnockbackLaunch(tick));
               boolean walkingEdge = !this.engaged && !freshHit && this.walkingOffEdge();
               if (this.engaged || freshHit || walkingEdge || this.ownFallNeedsRescue(tick)) {
                  ClutchFallPhysics.FallTrajectory forecast = walkingEdge ? this.forecastWalking() : this.forecastFall();
                  if (!this.engaged && !freshHit) {
                     if (walkingEdge) {
                        if (!forecast.unsafe() || forecast.steps().isEmpty()) {
                           return;
                        }

                        AABB last = forecast.steps().getLast().body();
                        if (!this.isDangerousDrop(Math.max(0.0, MC.player.getY() - last.minY), this.depthBelow(last))) {
                           return;
                        }
                     } else if (!this.armOwnFallRescue(tick, forecast)) {
                        return;
                     }

                     freshHit = true;
                  }

                  this.landingSafe = !forecast.unsafe();
                  ClutchModule.CatchRoute acquiredRoute = null;
                  boolean acquireCrouch = false;
                  boolean triedCrouch = false;
                  if (!this.engaged) {
                     if (!forecast.unsafe()) {
                        return;
                     }

                     int slot = this.bestBlockSlot();
                     boolean chosenAxis = Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
                     boolean ownFlight = ClutchFallPhysics.isSelfOrGuardCause(this.launchCause);
                     int chosenMode = ownFlight ? 3 : 0;
                     ClutchModule.CatchRoute first = ownFlight && forecast.unsafe() ? this.planCatch(forecast, slot) : null;
                     if (this.canSteer() && !walkingEdge && first == null) {
                        forecast = this.forecastSteered(chosenAxis);
                        chosenMode = 0;
                     }

                     if (first == null && forecast.unsafe()) {
                        first = this.planCatch(forecast, slot);
                     }

                     if (this.canSteer() && !walkingEdge && forecast.unsafe() && this.routeHurts(first) && chosenMode != 3) {
                        ClutchFallPhysics.FallTrajectory alternateForecast = this.forecastSteered(!chosenAxis);
                        ClutchModule.CatchRoute alternate = alternateForecast.unsafe() ? this.planCatch(alternateForecast, slot) : null;
                        if (!alternateForecast.unsafe() || this.routeClearlyBetter(alternate, first)) {
                           chosenAxis = !chosenAxis;
                           forecast = alternateForecast;
                           first = alternate;
                        }

                        for (int mode = 1; mode <= 2 && forecast.unsafe() && this.routeHurts(first); mode++) {
                           ClutchFallPhysics.FallTrajectory candidate = this.forecastSteered(chosenAxis, mode);
                           ClutchModule.CatchRoute catchRoute = candidate.unsafe() ? this.planCatch(candidate, slot) : null;
                           if (!candidate.unsafe()
                              || this.routeClearlyBetter(catchRoute, first)
                              || mode == 2 && this.lastGroundPos != null && first == null) {
                              forecast = candidate;
                              first = catchRoute;
                              chosenMode = mode;
                           }
                        }
                     }

                     if (this.canSteer() && !walkingEdge && forecast.unsafe() && first != null && chosenMode == 0) {
                        ClutchFallPhysics.FallTrajectory diagonalForecast = this.forecastSteered(chosenAxis, 1);
                        ClutchModule.CatchRoute diagonal = diagonalForecast.unsafe() ? this.planCatch(diagonalForecast, slot) : null;
                        if (!diagonalForecast.unsafe() || this.routeSimpler(diagonal, first)) {
                           forecast = diagonalForecast;
                           first = diagonal;
                           chosenMode = 1;
                        }
                     }

                     if (first == null && slot >= 0 && !walkingEdge && forecast.unsafe() && this.canCrouchCatch()) {
                        triedCrouch = true;
                        ClutchFallPhysics.FallTrajectory crouched = this.forecastSteeredFrom(chosenAxis, 1, true);
                        ClutchModule.CatchRoute catchRoute = this.planCatch(crouched, slot);
                        if (catchRoute != null) {
                           forecast = crouched;
                           first = catchRoute;
                           chosenMode = 1;
                           acquireCrouch = true;
                        }
                     }

                     if (first == null && this.canSteer() && !walkingEdge && forecast.unsafe() && this.rowReturnTarget() != null) {
                        ClutchFallPhysics.FallTrajectory rowReturn = this.forecastSteered(chosenAxis, 4);
                        ClutchModule.CatchRoute catchRoute = this.prepareRoute(rowReturn, slot, null, false, Set.of(), true);
                        if (catchRoute != null) {
                           forecast = rowReturn;
                           first = catchRoute;
                           chosenMode = 4;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        ClutchModule.CatchRoute urgent = this.planUrgentCatch(forecast, slot);
                        if (urgent != null) {
                           first = urgent;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        first = this.planSingleBlockSave(forecast, chosenAxis, chosenMode, slot);
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        first = this.planCatch(forecast, slot, null, true);
                     }

                     if (walkingEdge && first == null) {
                        return;
                     }

                     if (forecast.unsafe() && first == null && !this.canSteer()) {
                        this.status = "Out of reach";
                        return;
                     }

                     if (forecast.unsafe() && forecast.edgeTick() > 8 && (first == null || first.plan().init_k_NetHelper().load_I() > 8)) {
                        return;
                     }

                     if (walkingEdge) {
                        this.launchCause = new ClutchFallPhysics.HitEvent(ClutchFallPhysics.HitCause.FALL, true, true, tick);
                        this.launchGroundPos = this.lastGroundPos;
                        this.lastImpulseTick = tick;
                     }

                     this.engage(tick, first != null);
                     if (acquireCrouch) {
                        this.crouchStartTick = tick;
                     }

                     acquiredRoute = first;
                     this.steerAxisX = chosenAxis;
                     this.catchMode = chosenMode;
                     this.edgeRescue = walkingEdge;
                     if (first != null && !first.plan().init_Z_2() && !first.tower()) {
                        this.catchTarget = first.plan().init_BlockPos();
                        this.catchRowY = this.catchTarget.getY();
                     }

                     this.landingSafe = !forecast.unsafe();
                  }

                  if (MC.player.onGround() && MC.player.getDeltaMovement().y < 0.6) {
                     this.enemyLaunchedUp = false;
                  }

                  if (freshHit
                     && ClutchFallPhysics.isEnemyCause(this.launchCause)
                     && MC.player.getDeltaMovement().y > 0.15
                     && (MC.player.getDeltaMovement().y >= 0.6 || this.inKnockbackLaunch(tick))) {
                     this.enemyLaunchedUp = true;
                  }

                  if (this.disengaging) {
                     if (!freshHit || !forecast.unsafe()) {
                        this.disengage();
                        return;
                     }

                     this.disengaging = false;
                     this.disengageTicks = 0;
                     this.safeLandingTicks = 0;
                  }

                  if (this.edgeRescue ? !forecast.unsafe() : this.isGroundedSafe()) {
                     this.safeLandingTicks++;
                  } else {
                     this.safeLandingTicks = 0;
                  }

                  if (this.safeLandingTicks < 1 && tick - this.engageTick <= (MC.player.onGround() ? 80 : 160)) {
                     int slot = this.catchBlockSlot();
                     ClutchModule.CatchRoute tower = null;
                     if (this.landingSafe) {
                        this.currentPlan = null;
                        if (!this.placedThisTick(tick)) {
                           this.restoreSlot();
                        }

                        this.status = "Landing";
                     } else {
                        ClutchModule.CatchRoute route = this.landingSafe
                           ? null
                           : (acquiredRoute != null ? acquiredRoute : this.planCatch(forecast, slot));
                        if (!this.landingSafe && this.canSteer() && !this.edgeRescue && this.routeHurts(route)) {
                           ClutchFallPhysics.FallTrajectory alternateForecast = this.forecastSteered(!this.steerAxisX);
                           ClutchModule.CatchRoute alternate = alternateForecast.unsafe()
                              ? this.planCatch(alternateForecast, slot)
                              : null;
                           if (!alternateForecast.unsafe()) {
                              this.steerAxisX = !this.steerAxisX;
                              this.catchMode = 0;
                              this.landingSafe = true;
                              this.currentPlan = null;
                              this.status = "Landing";
                              return;
                           }

                           if (this.routeClearlyBetter(alternate, route)) {
                              this.steerAxisX = !this.steerAxisX;
                              this.catchMode = 0;
                              forecast = alternateForecast;
                              route = alternate;
                           }

                           for (int mode = 1; mode <= 2 && this.routeHurts(route); mode++) {
                              ClutchFallPhysics.FallTrajectory candidate = this.forecastSteered(this.steerAxisX, mode);
                              ClutchModule.CatchRoute catchRoute = candidate.unsafe() ? this.planCatch(candidate, slot) : null;
                              if (!candidate.unsafe()) {
                                 this.catchMode = mode;
                                 this.landingSafe = true;
                                 this.currentPlan = null;
                                 this.restoreSlot();
                                 this.status = "Landing";
                                 return;
                              }

                              if (this.routeClearlyBetter(catchRoute, route)) {
                                 this.catchMode = mode;
                                 forecast = candidate;
                                 route = catchRoute;
                              }
                           }
                        }

                        if (!this.landingSafe && route == null && slot >= 0 && !this.edgeRescue && !triedCrouch && this.canCrouchCatch()) {
                           ClutchFallPhysics.FallTrajectory crouched = this.forecastSteeredFrom(this.steerAxisX, 1, true);
                           ClutchModule.CatchRoute catchRoute = this.planCatch(crouched, slot);
                           if (catchRoute != null) {
                              this.crouchStartTick = tick;
                              this.catchMode = 1;
                              forecast = crouched;
                              route = catchRoute;
                           }
                        }

                        if (!this.landingSafe && route == null && this.canSteer() && !this.edgeRescue && this.rowReturnTarget() != null) {
                           ClutchFallPhysics.FallTrajectory rowReturn = this.forecastSteered(this.steerAxisX, 4);
                           ClutchModule.CatchRoute catchRoute = this.prepareRoute(rowReturn, slot, null, false, Set.of(), true);
                           if (catchRoute != null) {
                              forecast = rowReturn;
                              route = catchRoute;
                              this.catchMode = 4;
                           }
                        }

                        if (!this.landingSafe && route == null && !this.edgeRescue) {
                           route = this.planUrgentCatch(forecast, slot);
                        }

                        if (!this.landingSafe && route == null && !this.edgeRescue) {
                           route = this.planSingleBlockSave(forecast, this.steerAxisX, this.catchMode, slot);
                        }

                        if (!this.landingSafe && route == null && !this.edgeRescue) {
                           route = this.planCatch(forecast, slot, null, true);
                        }

                        if (!this.landingSafe && !this.edgeRescue) {
                           int reserve = route != null && !route.plan().init_Z_2() ? route.plan().init_List().size() : 0;
                           tower = this.planTower(forecast, slot, reserve);
                        }

                        if (tower != null && (route == null || route.plan().init_k_NetHelper().load_I() >= 3)) {
                           route = tower;
                        }

                        this.dangerReturning = DangerModule.init_Z_52() && (route == null || route.tower() || route.plan().init_k_NetHelper().load_I() >= 4);
                        if (this.dangerReturning) {
                           this.currentPlan = null;
                           this.status = "Danger returning";
                        } else if (route == null) {
                           this.currentPlan = null;
                           this.status = slot < 0 ? "Braking without blocks" : "No reachable catch";
                           if (!this.placedThisTick(tick)) {
                              this.restoreSlot();
                           }

                           if (this.edgeRescue) {
                              this.disengage();
                           } else {
                              if (!this.scaffoldActive() && !ClutchFallPhysics.isWithinTickWindow(tick, this.lastRouteTick, 3) && !this.placedThisTick(tick)) {
                                 this.restoreSlot();
                              }

                              if (!this.slotsReleased) {
                                 RotationMath.Rotation aim = autismclient.util.SilentRotationManager.getCurrentRotation();
                                 if (aim == null) {
                                    aim = RotationMath.ofPlayer(MC.player);
                                 }

                                 autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, aim);
                              }
                           }
                        } else {
                           this.lastRouteTick = tick;
                           this.nextEyePosition = forecast.eyePositions().size() > 1 ? forecast.eyePositions().get(1) : null;
                           boolean reacquiringHand = this.slotsReleased;
                           if (this.slotsReleased) {
                              this.slotsReleased = false;
                              this.originalHotbarSlot = this.originalSlot();
                              this.restoreToSlot = -1;
                              autismclient.util.ClientInteractionOverride.clearAll();
                              autismclient.util.SilentRotationManager.syncFromServerRotation();
                           }

                           this.clutchSlot = slot;
                           boolean hand = autismclient.util.InventorySlotArbiter.claimSlot(this.id(), this.clutchSlot);
                           if (hand && (MC.player.getInventory().getSelectedSlot() != this.clutchSlot || reacquiringHand)) {
                              autismclient.util.VirtualInputDispatcher.setSlot(this.clutchSlot);
                           }

                           if (!route.plan().init_Z_2() && !route.tower()) {
                              this.catchTarget = route.plan().init_BlockPos();
                              this.catchRowY = this.catchTarget.getY();
                           }

                           ClutchBlockPlanner.PlacementLink link = route.plan().init_k_NetHelper();
                           this.ticksToPlacement = this.placementTick(link);
                           if (hand
                              && this.currentPlan != null
                              && this.currentPlan.slot == this.clutchSlot
                              && !this.recentPlacementCells.containsKey(this.currentPlan.pos)
                              && (!route.tower() || route.plan().init_k_NetHelper().init_BlockPos().equals(this.currentPlan.pos))) {
                              for (int i = 0; i < route.plan().init_List().size(); i++) {
                                 ClutchBlockPlanner.PlacementLink preparedLink = route.plan().init_List().get(i);
                                 if (preparedLink.init_BlockPos().equals(this.currentPlan.pos)) {
                                    Direction nextFace = i + 1 < route.plan().init_List().size()
                                          && route.plan().init_List().get(i + 1).load_BlockPos().equals(this.currentPlan.pos)
                                       ? route.plan().init_List().get(i + 1).init_Direction()
                                       : null;
                                    this.currentPlan = new ClutchModule.PlacementStep(
                                       this.currentPlan.pos,
                                       this.currentPlan.placement,
                                       this.currentPlan.latestTick,
                                       this.currentPlan.slot,
                                       this.currentPlan.bestEffort,
                                       this.currentPlan.tower,
                                       nextFace,
                                       route.plan().read()
                                    );
                                    break;
                                 }
                              }

                              if (this.tryPlaceCurrent()) {
                                 return;
                              }
                           }

                           ClutchModule.PlacementStep next = this.preparePlacement(route, link, this.clutchSlot);
                           if (next == null && !route.tower() && !this.edgeRescue) {
                              ClutchModule.CatchRoute alternate = this.planCatch(
                                 forecast, this.clutchSlot, null, route.plan().load(), Set.of(link.init_BlockPos())
                              );
                              if (alternate != null) {
                                 ClutchModule.PlacementStep candidate = this.preparePlacement(
                                    alternate, alternate.plan().init_k_NetHelper(), this.clutchSlot
                                 );
                                 if (candidate != null) {
                                    route = alternate;
                                    link = route.plan().init_k_NetHelper();
                                    next = candidate;
                                    this.ticksToPlacement = this.placementTick(link);
                                    if (!route.plan().init_Z_2()) {
                                       this.catchTarget = route.plan().init_BlockPos();
                                       this.catchRowY = this.catchTarget.getY();
                                    }
                                 }
                              }
                           }

                           if (next == null) {
                              this.currentPlan = null;
                              this.status = "Turning to the catch";
                              autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, route.aimAt(link, this.earliestPlacementTick(link)));
                              this.stepRotation(this.ticksToPlacement);
                           } else {
                              this.currentPlan = next;
                              this.status = !hand
                                 ? "Hand busy"
                                 : (
                                    this.edgeRescue
                                       ? "Securing the edge"
                                       : (
                                          route.tower()
                                             ? "Building under the lift"
                                             : (route.plan().load() ? "Reaching toward the fall" : "Catching knockback")
                                       )
                                 );
                              if (this.wireState != null
                                 && this.wireState.tick < this.clientTick()
                                 && MC.player.getInventory().getSelectedSlot() == this.currentPlan.slot) {
                                 Vec3 previousEye = this.wireState.position.add(0.0, this.wireState.eyeHeight, 0.0);
                                 double reach = MC.player.blockInteractionRange() - 0.03;
                                 if (hand && link.init_I() == 0) {
                                    ClutchModule.PlacementStep current = this.earliestPlacementTick(link) == 0
                                       ? this.currentPlan
                                       : this.preparePlacement(
                                          route, new ClutchBlockPlanner.PlacementLink(link.init_BlockPos(), link.load_BlockPos(), link.init_Direction(), 0, 0), this.clutchSlot
                                       );
                                    if (current != null
                                       && ScaffoldModule.init_Z_Vec3(
                                          previousEye,
                                          this.wireState.rotation,
                                          current.placement.support().supportPos(),
                                          reach
                                       )
                                       && ScaffoldModule.init_Z_Vec3(
                                          previousEye,
                                          current.placement.aim().goalRotation(),
                                          current.placement.support().supportPos(),
                                          reach
                                       )) {
                                       this.currentPlan = current;
                                       this.ticksToPlacement = 1;
                                    }
                                 }

                                 if (!this.currentPlan.read
                                    && ScaffoldModule.init_Z_Vec3(
                                       previousEye,
                                       this.wireState.rotation,
                                       this.currentPlan.placement.support().supportPos(),
                                       reach
                                    )) {
                                    autismclient.util.BlockPlacementPlanner.PlacementPlan carry = this.carryPlacementPlan(
                                       this.currentPlan, this.currentPlan.placement.aim().goalRotation(), previousEye, reach
                                    );
                                    if (carry != null) {
                                       this.currentPlan = new ClutchModule.PlacementStep(
                                          this.currentPlan.pos,
                                          carry,
                                          this.currentPlan.latestTick,
                                          this.currentPlan.slot,
                                          this.currentPlan.bestEffort,
                                          this.currentPlan.tower,
                                          this.currentPlan.face,
                                          this.currentPlan.read
                                       );
                                    }
                                 }
                              }

                              autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, this.currentPlan.placement.aim().goalRotation());
                              this.stepRotation(this.ticksToPlacement);
                              if (hand) {
                                 this.tryPlaceCurrent();
                              }
                           }
                        }
                     }
                  } else {
                     int caughtRow = this.launchGroundPos == null ? this.catchRowY : Math.min(this.catchRowY, this.launchGroundPos.getY());
                     if (this.safeLandingTicks >= 1 && MC.player.getY() >= caughtRow + 1 - this.safeFallDistance()) {
                        this.rescues++;
                        this.safeCatchTick = tick;
                        this.caughtSafely = true;
                     }

                     if (this.safeLandingTicks >= 1 && !this.placedThisTick(tick)) {
                        this.restoreSlot();
                     }

                     this.launchCause = null;
                     this.launchDamageTick = -2147483648;
                     this.disengage();
                  }
               }
            }
         } else if (this.engaged) {
            this.disengage();
         }
      } else {
         this.resetAll();
      }
   }

   private void stepRotation(int ticksLeft) {
      if ((this.catchMode == 4 || this.currentPlan != null && this.currentPlan.read) && ticksLeft <= 1) {
         autismclient.util.SilentRotationManager.stepUrgentFixed(this.id(), MC.player, 180.0F);
      } else {
         autismclient.util.SilentRotationManager.stepUrgentTimed(this.id(), MC.player, ticksLeft);
      }
   }

   private void engage(int tick, boolean placement) {
      this.engaged = true;
      this.slotsReleased = !placement;
      this.disengaging = false;
      this.disengageTicks = 0;
      this.engageTick = tick;
      this.safeLandingTicks = 0;
      this.catchMode = 0;
      this.edgeRescue = false;
      this.enemyLaunchedUp = false;
      this.caughtSafely = false;
      this.hitAxisZ = null;
      this.steerAxisX = Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
      this.catchRowY = this.launchGroundPos != null ? this.launchGroundPos.getY() : (int)Math.floor(MC.player.getY()) - 1;
      this.placedCatchCell = null;
      this.catchTarget = null;
      this.lastRouteTick = tick;
      this.originalHotbarSlot = placement ? this.originalSlot() : -1;
      if (placement) {
         this.restoreToSlot = -1;
         autismclient.util.SilentRotationManager.syncFromServerRotation();
         autismclient.util.ClientInteractionOverride.clearAll();
         autismclient.util.VirtualInputDispatcher.update_V();
         if (MC.player.isUsingItem()) {
            MC.gameMode.releaseUsingItem(MC.player);
         }
      }

      this.status = "Catching knockback";
   }

   private int originalSlot() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.restoreToSlot >= 0 && selected == this.restoreFromSlot ? this.restoreToSlot : selected;
   }

   private boolean placedThisTick(int tick) {
      return this.lastPlaceTick == tick || this.sentPlacementHit != null && ClutchFallPhysics.isWithinTickWindow(tick, this.placeHitTick, 1);
   }

   private void updateLastGround(int tick) {
      AABB body = MC.player.getBoundingBox().expandTowards(MC.player.xo - MC.player.getX(), 0.0, MC.player.zo - MC.player.getZ());
      double best = 1.0 / 0.0;

      for (int x = (int)Math.floor(body.minX); x <= (int)Math.floor(body.maxX); x++) {
         for (int z = (int)Math.floor(body.minZ); z <= (int)Math.floor(body.maxZ); z++) {
            BlockPos pos = new BlockPos(x, (int)Math.floor(body.minY - 0.05), z);
            VoxelShape shape = MC.level.getBlockState(pos).getCollisionShape(MC.level, pos);
            if (!shape.isEmpty()) {
               double distance = Vec3.atCenterOf(pos).distanceToSqr(MC.player.position());
               if (distance < best) {
                  best = distance;
                  this.lastGroundPos = pos;
                  this.lastGroundTick = tick;
               }
            }
         }
      }
   }

   private boolean ownFallNeedsRescue(int tick) {
      if (!MC.player.onGround() && this.lastGroundPos != null && this.ownSteerWindow.isActive(tick)) {
         boolean underLaunchRow = MC.player.getDeltaMovement().y < 0.0 && MC.player.getY() < this.lastGroundPos.getY();
         if (this.ignoredLaunch && !underLaunchRow) {
            return false;
         } else if (!this.inGuardTime()) {
            return !this.bool("own-knockback")
               ? false
               : ClutchFallPhysics.shouldDeployRescue(tick, this.lastGroundTick, MC.player.getY(), this.lastGroundPos.getY(), MC.player.getDeltaMovement().y, this.ignoredLaunch, scaffoldEnabled());
         } else {
            return !scaffoldEnabled() || MC.player.getY() < this.lastGroundPos.getY() + 0.4;
         }
      } else {
         return false;
      }
   }

   private boolean armOwnFallRescue(int tick, ClutchFallPhysics.FallTrajectory forecast) {
      if (!this.ownFallNeedsRescue(tick) || !forecast.unsafe() || forecast.steps().isEmpty()) {
         return false;
      }

      if (this.inGuardTime()) {
         this.launchCause = new ClutchFallPhysics.HitEvent(ClutchFallPhysics.HitCause.GUARD, false, true, tick);
         this.launchGroundPos = this.lastGroundPos;
         this.lastImpulseTick = tick;
         this.launchDamageTick = -2147483648;
         return true;
      }

      AABB last = forecast.steps().getLast().body();
      if (!this.isDangerousDrop(this.lastGroundPos.getY() + 1 - last.minY, this.depthBelow(last))) {
         return false;
      }

      this.launchCause = new ClutchFallPhysics.HitEvent(ClutchFallPhysics.HitCause.FALL, true, true, tick);
      this.launchGroundPos = this.lastGroundPos;
      this.lastImpulseTick = tick;
      this.launchDamageTick = -2147483648;
      return true;
   }

   private boolean isDangerousDrop(double fallen, double below) {
      return ClutchFallPhysics.isDangerousDrop(fallen, below, this.bool("own-void-only"), this.integer("own-fall-height"));
   }

   private boolean fallWouldKill(double fallen, double below) {
      if (Double.isNaN(below)) {
         return false;
      }

      if (Double.isInfinite(below)) {
         return true;
      }

      double health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      return ClutchFallPhysics.estimateFallDamage(fallen + below) >= health;
   }

   private double depthBelow(AABB body) {
      int x0 = Mth.floor(body.minX + 0.001);
      int x1 = Mth.floor(body.maxX - 0.001);
      int z0 = Mth.floor(body.minZ + 0.001);
      int z1 = Mth.floor(body.maxZ - 0.001);
      MutableBlockPos pos = new MutableBlockPos();

      for (int y = Mth.floor(body.minY - 0.001); y >= MC.level.getMinY(); y--) {
         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               pos.set(x, y, z);
               if (!MC.level.hasChunkAt(pos)) {
                  return 0.0 / 0.0;
               }

               BlockState state = MC.level.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               if (fluid.is(FluidTags.WATER)) {
                  return 0.0 / 0.0;
               }

               if (fluid.is(FluidTags.LAVA)) {
                  return 1.0 / 0.0;
               }

               VoxelShape shape = state.getCollisionShape(MC.level, pos);
               if (!shape.isEmpty()) {
                  return Math.max(0.0, body.minY - (y + shape.max(Axis.Y)));
               }
            }
         }
      }

      return 1.0 / 0.0;
   }

   private ClutchFallPhysics.MediumContact mediumAt(AABB body) {
      AABB box = body.deflate(0.001);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
         for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               pos.set(x, y, z);
               BlockState state = MC.level.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               if (fluid.is(FluidTags.LAVA)) {
                  return ClutchFallPhysics.MediumContact.HAZARD;
               }

               if (fluid.is(FluidTags.WATER) && y + fluid.getHeight(MC.level, pos) > box.minY) {
                  return ClutchFallPhysics.MediumContact.CUSHION;
               }

               if (state.is(Blocks.COBWEB) || state.is(Blocks.POWDER_SNOW)) {
                  return ClutchFallPhysics.MediumContact.CUSHION;
               }
            }
         }
      }

      Vec3 centre = body.getCenter();
      pos.set(Mth.floor(centre.x), Mth.floor(body.minY - 0.05), Mth.floor(centre.z));
      return MC.level.getBlockState(pos).is(Blocks.SLIME_BLOCK) ? ClutchFallPhysics.MediumContact.CUSHION : ClutchFallPhysics.MediumContact.NONE;
   }

   private double safeFallDistance() {
      double health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      return Math.max(1.0, Math.min(this.integer("safe-fall"), 2.0 + health));
   }

   private ClutchFallPhysics.FallTrajectory forecastFall() {
      if (this.edgeRescue) {
         return this.forecastWalking();
      }

      ClutchFallPhysics.CollisionWorld collision = (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of());
      ClutchFallPhysics.FallMedium medium = this::mediumAt;
      if (this.engaged && !this.disengaging && this.canSteer() && this.catchMode != 3) {
         return this.forecastSteered(this.steerAxisX, this.catchMode);
      }

      Vec3 acceleration = Vec3.ZERO;
      if (MC.player.input != null) {
         acceleration = this.inputAcceleration();
         if (acceleration.lengthSqr() > 0.0) {
            acceleration = acceleration.scale(MC.player.isSprinting() ? 0.026 : 0.02);
         }
      }

      return ClutchFallPhysics.simulateBoxFallWithAccel(
         MC.player.getBoundingBox(), MC.player.getDeltaMovement(), acceleration, this.safeFallDistance(), collision, medium, MC.player.fallDistance
      );
   }

   private ClutchFallPhysics.FallTrajectory forecastSteered(boolean axis) {
      return this.forecastSteered(axis, 0);
   }

   private ClutchFallPhysics.FallTrajectory forecastSteered(boolean axis, int mode) {
      return this.forecastSteered(axis, mode, List.of());
   }

   private ClutchFallPhysics.FallTrajectory forecastSteered(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (this.canSteer() && !this.disengaging && mode != 3) {
         return this.forecastMode(axis, mode, extra, placementTick);
      }

      Vec3 acceleration = Vec3.ZERO;
      if (MC.player.input != null) {
         acceleration = this.inputAcceleration();
         if (acceleration.lengthSqr() > 0.0) {
            acceleration = acceleration.scale(MC.player.isSprinting() ? 0.026 : 0.02);
         }
      }

      Vec3 input = acceleration;
      return ClutchFallPhysics.simulateBoxFall(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         this.safeFallDistance(),
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
         (body, motion, supported) -> input,
         this::mediumAt,
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, extra),
         placementTick,
         MC.player.fallDistance
      );
   }

   private ClutchFallPhysics.FallTrajectory forecastSteered(boolean axis, int mode, List<VoxelShape> extra) {
      return this.forecastMode(axis, mode, extra, 0);
   }

   private ClutchFallPhysics.FallTrajectory forecastMode(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (mode == 3) {
         return this.forecastSteered(axis, mode, extra, placementTick);
      }

      if (MC.player.onGround()
         || !this.crouchRecent()
            && (!(Math.abs(MC.player.getBoundingBox().getXsize() - 0.6) < 0.001) || !(Math.abs(MC.player.getBoundingBox().getYsize() - 1.5) < 0.001))) {
         Vec3 initialMotion = MC.player.getDeltaMovement();
         if (this.canSteer() && this.shouldJumpOnLaunch(this.bestBlockSlot() >= 0, mode)) {
            float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
            initialMotion = new Vec3(initialMotion.x, Math.max(initialMotion.y, jump), initialMotion.z);
         }

         return ClutchFallPhysics.simulateBoxFall(
            MC.player.getBoundingBox(),
            initialMotion,
            this.safeFallDistance(),
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
            (body, motion, supported) -> this.steerDirection(body, motion, axis, mode, supported).scale(supported ? 0.1 : 0.02),
            this::mediumAt,
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, extra),
            placementTick,
            MC.player.fallDistance
         );
      } else {
         return this.forecastSteeredFrom(axis, mode, false, extra, placementTick);
      }
   }

   private Vec3 steerDirection(AABB body, Vec3 motion, boolean axis, int mode, boolean supported) {
      if (supported || this.disengaging) {
         return ClutchFallPhysics.brakingDirection(motion);
      }

      if (mode == 4 && this.rowReturnPos != null) {
         Vec3 centre = body.getCenter();
         double error = axis ? this.rowReturnPos.getZ() + 0.5 - centre.z - motion.z * 5.0 : this.rowReturnPos.getX() + 0.5 - centre.x - motion.x * 5.0;
         return axis ? new Vec3(0.0, 0.0, Math.signum(error)) : new Vec3(Math.signum(error), 0.0, 0.0);
      }

      if (mode == 2 && this.lastGroundPos != null && !MC.level.getBlockState(this.lastGroundPos).getCollisionShape(MC.level, this.lastGroundPos).isEmpty()) {
         Vec3 home = Vec3.atCenterOf(this.lastGroundPos);
         Vec3 centre = body.getCenter();
         Vec3 wanted = new Vec3(home.x - centre.x - motion.x * 5.0, 0.0, home.z - centre.z - motion.z * 5.0);
         if (wanted.horizontalDistanceSqr() > 0.0025) {
            return wanted.normalize();
         }
      }

      return mode == 0 ? ClutchFallPhysics.brakingAxisDirection(motion, axis) : ClutchFallPhysics.brakingDirection(motion);
   }

   private BlockPos rowReturnTarget() {
      if (this.lastGroundPos != null && !MC.level.getBlockState(this.lastGroundPos).getCollisionShape(MC.level, this.lastGroundPos).isEmpty()) {
         return this.rowReturnPos = this.lastGroundPos;
      }

      if (this.rowReturnPos != null && !MC.level.getBlockState(this.rowReturnPos).getCollisionShape(MC.level, this.rowReturnPos).isEmpty()) {
         return this.rowReturnPos;
      }

      int x = Mth.floor(MC.player.getX());
      int z = Mth.floor(MC.player.getZ());
      int y = Mth.floor(MC.player.getY()) - 1;
      double best = 1.0 / 0.0;
      BlockPos found = null;

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            for (int dy = 0; dy < 3; dy++) {
               BlockPos pos = new BlockPos(x + dx, y - dy, z + dz);
               if (!MC.level.getBlockState(pos).getCollisionShape(MC.level, pos).isEmpty()) {
                  double cost = Vec3.atCenterOf(pos).distanceToSqr(MC.player.position());
                  if (cost < best) {
                     best = cost;
                     found = pos;
                  }
               }
            }
         }
      }

      return this.rowReturnPos = found;
   }

   private boolean crouchRecent() {
      return ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.crouchStartTick, 2) && !MC.player.onGround();
   }

   private boolean canCrouchCatch() {
      AABB body = MC.player.getBoundingBox();
      return this.canSteer()
         && !MC.player.onGround()
         && !this.crouchRecent()
         && !MC.options.keyShift.isDown()
         && Math.abs(body.getXsize() - 0.6) < 0.001
         && Math.abs(body.getYsize() - 1.8) < 0.001
         && Math.abs(MC.player.getEyeHeight() - 1.62) < 0.001;
   }

   private ClutchFallPhysics.FallTrajectory forecastSteeredFrom(boolean axis, int mode, boolean start) {
      return this.forecastSteeredFrom(axis, mode, start, List.of(), 0);
   }

   private ClutchFallPhysics.FallTrajectory forecastSteeredFrom(boolean axis, int mode, boolean start, List<VoxelShape> extra, int placementTick) {
      return ClutchFallPhysics.simulateMotionFall(
         autismclient.ai.engine.AiPhysics.MotionState.of(MC.player),
         MC.options.keyShift.isDown() ? 64 : (!start && this.clientTick() != this.crouchStartTick ? 0 : 1),
         MC.player.getYRot(),
         this.safeFallDistance(),
         autismclient.ai.engine.AiPhysics.createLiveWorld(MC.level, MC.player),
         (body, motion, supported) -> this.steerDirection(body, motion, axis, mode, supported),
         this::mediumAt,
         extra.stream().flatMap(shape -> shape.toAabbs().stream()).toList(),
         placementTick,
         MC.player.fallDistance
      );
   }

   private Input keyboardInput() {
      return new Input(
         MC.options.keyUp.isDown(),
         MC.options.keyDown.isDown(),
         MC.options.keyLeft.isDown(),
         MC.options.keyRight.isDown(),
         MC.options.keyJump.isDown(),
         MC.options.keyShift.isDown(),
         MC.options.keySprint.isDown()
      );
   }

   private boolean walkingOffEdge() {
      return this.ownSteerWindow.isActive(this.clientTick())
         && this.bool("own-knockback")
         && this.bool("own-steer")
         && this.ownSteerHoldTicks > 0
         && MC.player.onGround()
         && this.lastGroundPos != null
         && !scaffoldEnabled()
         && !this.ignoredLaunch
         && inputVector(this.keyboardInput(), MC.player.getYRot()).horizontalDistanceSqr() > 0.0;
   }

   private ClutchFallPhysics.FallTrajectory forecastWalking() {
      Input input = this.keyboardInput();
      autismclient.ai.engine.AiPhysics.MoveInput pressed = new autismclient.ai.engine.AiPhysics.MoveInput(
         (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0), (input.left() ? 1 : 0) - (input.right() ? 1 : 0), false, false, true, SilentMovementCoordinator.getMovementYaw(MC.player)
      );
      return ClutchFallPhysics.simulateWalkTrajectory(
         autismclient.ai.engine.AiPhysics.MotionState.of(MC.player),
         pressed,
         autismclient.ai.engine.AiPhysics.createLiveWorld(MC.level, MC.player),
         this.safeFallDistance(),
         this::mediumAt
      );
   }

   private Vec3 inputAcceleration() {
      return this.engaged
         ? inputVector(this.keyboardInput(), MC.player.getYRot())
         : inputVector(MC.player.input.keyPresses, SilentMovementCoordinator.getEntityYaw(MC.player, MC.player.getYRot()));
   }

   private static Vec3 inputVector(Input input, float keyYaw) {
      double forward = (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0);
      double side = (input.left() ? 1 : 0) - (input.right() ? 1 : 0);
      double yaw = Math.toRadians(keyYaw);
      Vec3 direction = new Vec3(side * Math.cos(yaw) - forward * Math.sin(yaw), 0.0, forward * Math.cos(yaw) + side * Math.sin(yaw));
      return direction.lengthSqr() > 0.0 ? direction.normalize() : Vec3.ZERO;
   }

   private int catchBlockSlot() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.clutchSlot >= 0
            && this.clutchSlot == selected
            && this.isClutchBlock(MC.player.getInventory().getItem(selected))
            && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(selected, this.id())
            && ScaffoldModule.load_I_I(selected) > 0
         ? selected
         : this.bestBlockSlot();
   }

   private int bestBlockSlot() {
      int selected = MC.player.getInventory().getSelectedSlot();
      int best = -1;
      int most = 0;

      for (int i = 0; i < 9; i++) {
         if (this.isClutchBlock(MC.player.getInventory().getItem(i)) && !autismclient.util.InventorySlotArbiter.isSlotClaimedByOther(i, this.id())) {
            int available = ScaffoldModule.load_I_I(i);
            if (i == selected && available >= 8) {
               return selected;
            }

            if (available > most) {
               best = i;
               most = available;
            }
         }
      }

      return best;
   }

   private boolean isClutchBlock(ItemStack stack) {
      if (!stack.isEmpty() && stack.getItem() instanceof BlockItem block) {
         BlockState state = block.getBlock().defaultBlockState();
         return state.isCollisionShapeFullBlock(MC.level, MC.player.blockPosition())
            && !(block.getBlock() instanceof FallingBlock)
            && !(block.getBlock() instanceof TntBlock);
      } else {
         return false;
      }
   }

   private ClutchModule.CatchRoute planCatch(ClutchFallPhysics.FallTrajectory forecast, int slot) {
      return this.planCatch(forecast, slot, null, false);
   }

   private ClutchModule.CatchRoute planCatch(ClutchFallPhysics.FallTrajectory forecast, int slot, BlockPos assumed) {
      return this.planCatch(forecast, slot, assumed, false);
   }

   private ClutchModule.CatchRoute planCatch(ClutchFallPhysics.FallTrajectory forecast, int slot, BlockPos assumed, boolean bestEffort) {
      return this.planCatch(forecast, slot, assumed, bestEffort, Set.of());
   }

   private ClutchModule.CatchRoute planCatch(
      ClutchFallPhysics.FallTrajectory forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts
   ) {
      ClutchModule.CatchRoute prepared = this.prepareRoute(forecast, slot, assumed, bestEffort, rejectedStarts, this.catchMode == 4);
      return prepared == null && !bestEffort && assumed == null
         ? (this.currentPlan != null && this.currentPlan.read ? this.planUrgentCatch(forecast, slot) : null)
         : prepared;
   }

   private ClutchModule.CatchRoute prepareRoute(
      ClutchFallPhysics.FallTrajectory forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts, boolean marginal
   ) {
      if (!forecast.steps().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.TrajectoryStep step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         RotationMath.Rotation from = this.currentLook();
         ToDoubleFunction<ClutchBlockPlanner.PlacementLink> turnCost = link -> {
            Vec3 eye = forecast.eyePositions().isEmpty()
               ? ClutchModule.CatchRoute.eyeFromBodies(bodies, eyeHeight, this.earliestPlacementTick(link))
               : forecast.eyePositions().get(Mth.clamp(this.earliestPlacementTick(link), 0, forecast.eyePositions().size() - 1));
            float angle = RotationMath.vectorAngleDegrees(from, ClutchModule.CatchRoute.aimFrom(eye, link));
            return Math.max(0.0F, angle / this.placementTick(link) - 25.0F) * 0.04;
         };
         ClutchBlockPlanner.ClutchPlan plan = null;
         boolean own = ClutchFallPhysics.isSelfOrGuardCause(this.launchCause);

         for (int pass = own ? 0 : 2; pass < 3 && plan == null; pass++) {
            ClutchBlockPlanner.PlacementWorld world = this.placementWorld(assumed, range, rejectedStarts, pass < 2 ? this.supportRowY() : 2147483647);
            plan = ClutchBlockPlanner.init(
               bodies,
               eyeHeight,
               range - 0.05,
               world,
               assumed == null ? 0 : 1,
               MC.player.fallDistance,
               this.effectiveHealth(),
               this.catchTarget,
               this.supportRowY(),
               turnCost,
               Math.max(0, ScaffoldModule.load_I_I(slot) - (assumed == null ? 0 : 1)),
               this.fallDamageCurve(),
               bestEffort ? range : 0.0,
               this.unaidedLandingDamage(forecast),
               forecast.eyePositions(),
               marginal || pass == 1
            );
         }

         return plan == null ? null : new ClutchModule.CatchRoute(plan, bodies, eyeHeight, false, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.CatchRoute planUrgentCatch(ClutchFallPhysics.FallTrajectory forecast, int slot) {
      if (slot >= 0 && !forecast.steps().isEmpty()) {
         List<AABB> bodies = new ArrayList<>();
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.TrajectoryStep step : forecast.steps()) {
            bodies.add(step.body());
         }

         double reach = MC.player.blockInteractionRange() - 0.03;
         ClutchBlockPlanner.ClutchPlan urgent = ClutchBlockPlanner.init(
            bodies,
            MC.player.getEyeHeight(),
            reach - 0.05,
            this.placementWorld(null, reach),
            0,
            MC.player.fallDistance,
            this.effectiveHealth(),
            this.catchTarget,
            this.supportRowY(),
            null,
            ScaffoldModule.load_I_I(slot),
            this.fallDamageCurve(),
            this.unaidedLandingDamage(forecast),
            forecast.eyePositions(),
            true
         );
         return urgent == null ? null : new ClutchModule.CatchRoute(urgent, bodies, MC.player.getEyeHeight(), false, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.CatchRoute planTower(ClutchFallPhysics.FallTrajectory forecast, int slot, int reserve) {
      if (forecast.unsafe() && this.enemyLaunchedUp && ClutchFallPhysics.isEnemyCause(this.launchCause) && slot >= 0 && !forecast.steps().isEmpty() && ScaffoldModule.load_I_I(slot) > reserve) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.TrajectoryStep step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         ClutchBlockPlanner.ClutchPlan plan = ClutchBlockPlanner.load(
            bodies, eyeHeight, range - 0.05, this.placementWorld(null, range, Set.of(), 2147483647, this.landingSafe), 0, forecast.eyePositions()
         );
         return plan == null ? null : new ClutchModule.CatchRoute(plan, bodies, eyeHeight, true, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.CatchRoute planSingleBlockSave(ClutchFallPhysics.FallTrajectory forecast, boolean axis, int mode, int slot) {
      if (!forecast.steps().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.TrajectoryStep step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;

         for (ClutchBlockPlanner.PlacementLink link : ClutchBlockPlanner.init_List_List(bodies, eyeHeight, range - 0.05, this.placementWorld(null, range), 0, 8, forecast.eyePositions())) {
            if (!this.forecastSteered(axis, mode, List.of(Shapes.create(new AABB(link.init_BlockPos()))), link.init_I()).unsafe()) {
               ClutchBlockPlanner.ClutchPlan plan = new ClutchBlockPlanner.ClutchPlan(List.of(link), link.init_BlockPos(), link.init_I() + 1, MC.player.fallDistance, false);
               return new ClutchModule.CatchRoute(plan, bodies, eyeHeight, false, forecast.eyePositions());
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private ClutchBlockPlanner.PlacementWorld placementWorld(BlockPos assumed, double range) {
      return this.placementWorld(assumed, range, Set.of());
   }

   private ClutchBlockPlanner.PlacementWorld placementWorld(BlockPos assumed, double range, Set<BlockPos> rejectedStarts) {
      return this.placementWorld(assumed, range, rejectedStarts, 2147483647);
   }

   private ClutchBlockPlanner.PlacementWorld placementWorld(BlockPos assumed, double range, Set<BlockPos> rejectedStarts, int maxRow) {
      return this.placementWorld(assumed, range, rejectedStarts, maxRow, false);
   }

   private ClutchBlockPlanner.PlacementWorld placementWorld(
      final BlockPos assumed, double range, final Set<BlockPos> rejectedStarts, final int maxRow, boolean optional
   ) {
      final boolean sneaking = MC.player.isSecondaryUseActive();
      final BlockHitResult writtenRay = this.worldCache.wireRay(range);
      final List<AABB> others = this.worldCache.cachedOtherBodies(optional);
      return new ClutchBlockPlanner.PlacementWorld() {
         private boolean isAssumed(int x, int y, int z) {
            return assumed != null && assumed.getX() == x && assumed.getY() == y && assumed.getZ() == z;
         }

         @Override
         public boolean init(int x, int y, int z) {
            return rejectedStarts.isEmpty() || !rejectedStarts.contains(new BlockPos(x, y, z));
         }

         @Override
         public boolean load(int x, int y, int z) {
            return this.isAssumed(x, y, z) || ClutchModule.this.worldCache.isSupport(x, y, z, sneaking);
         }

         @Override
         public boolean read(int x, int y, int z) {
            if (y <= maxRow && !this.isAssumed(x, y, z) && ClutchModule.this.worldCache.isReplaceable(x, y, z)) {
               if (!ClutchModule.this.recentPlacementCells.isEmpty() && ClutchModule.this.recentPlacementCells.containsKey(new BlockPos(x, y, z))) {
                  return false;
               }

               for (AABB other : others) {
                  if (other.minX < x + 1 && other.maxX > x && other.minY < y + 1 && other.maxY > y && other.minZ < z + 1 && other.maxZ > z) {
                     return false;
                  }
               }

               return true;
            } else {
               return false;
            }
         }

         @Override
         public boolean init(Vec3 eye, double distance) {
            return assumed != null && eye.distanceToSqr(Vec3.atCenterOf(assumed)) <= distance * distance
               ? false
               : ClutchModule.this.worldCache.openAirWithin(eye, distance);
         }

         @Override
         public boolean write(int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            return writtenRay != null && !pos.equals(assumed) && writtenRay.getBlockPos().equals(pos);
         }

         @Override
         public boolean init(int x, int y, int z, Direction face) {
            BlockPos pos = new BlockPos(x, y, z);
            return !pos.equals(assumed) && ClutchFallPhysics.matchesPlacementHit(writtenRay, pos, face);
         }
      };
   }

   private List<AABB> otherBodies(boolean optional) {
      int lead = Mth.clamp(Mth.ceil(autismclient.util.macro.ServerTickPhaseTracker.getLatencyMs() / 50.0F) + 4, 4, 10);
      List<AABB> others = new ArrayList<>();

      for (Entity entity : MC.level
         .getEntities(MC.player, MC.player.getBoundingBox().inflate(10.0), candidate -> candidate.blocksBuilding && candidate.isAlive())) {
         Vec3 step = entity.position().subtract(entity.xo, entity.yo, entity.zo);
         InterpolationHandler interpolation = entity.getInterpolation();
         Vec3 latest = interpolation != null && interpolation.hasActiveInterpolation()
            ? Compat263.interpPos(interpolation).subtract(entity.position())
            : Vec3.ZERO;
         AABB swept = optional ? ClutchFallPhysics.predictSweptLeadBox(entity.getBoundingBox(), latest, step, lead) : ClutchFallPhysics.predictLeadBox(entity.getBoundingBox(), latest, step, lead);
         others.add(swept);
      }

      return others;
   }

   private static boolean intersectsBodies(BlockPos cell, List<AABB> bodies) {
      AABB box = new AABB(cell);

      for (AABB body : bodies) {
         if (body.intersects(box)) {
            return true;
         }
      }

      return false;
   }

   private double unaidedLandingDamage(ClutchFallPhysics.FallTrajectory forecast) {
      List<ClutchFallPhysics.TrajectoryStep> steps = forecast.steps();
      if (!steps.isEmpty() && steps.getLast().onGround()) {
         double highest = MC.player.getBoundingBox().minY;

         for (ClutchFallPhysics.TrajectoryStep step : steps) {
            highest = Math.max(highest, step.body().minY);
         }

         return this.fallDamageCurve().applyAsDouble(MC.player.fallDistance + Math.max(0.0, highest - steps.getLast().body().minY));
      } else {
         return 1.0 / 0.0;
      }
   }

   private boolean routeHurts(ClutchModule.CatchRoute route) {
      return this.routeFallDamage(route) >= 4.0;
   }

   private boolean routeClearlyBetter(ClutchModule.CatchRoute candidate, ClutchModule.CatchRoute current) {
      return candidate != null && this.routeFallDamage(candidate) <= this.routeFallDamage(current) - 4.0;
   }

   private boolean routeSimpler(ClutchModule.CatchRoute candidate, ClutchModule.CatchRoute current) {
      if (candidate != null
         && !candidate.plan().load()
         && !current.tower()
         && !(this.routeFallDamage(candidate) > this.routeFallDamage(current))) {
         int links = candidate.plan().init_List().size();
         int previous = current.plan().init_List().size();
         return links < previous || links == previous && candidate.plan().init_I() < current.plan().init_I();
      } else {
         return false;
      }
   }

   private double routeFallDamage(ClutchModule.CatchRoute route) {
      return route != null && !route.plan().load() ? this.fallDamageCurve().applyAsDouble(route.plan().init_D()) : 1.0 / 0.0;
   }

   private double effectiveHealth() {
      return MC.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING) ? 1.0 / 0.0 : MC.player.getHealth() + MC.player.getAbsorptionAmount();
   }

   private DoubleUnaryOperator fallDamageCurve() {
      return this.worldCache.fallDamageCurve();
   }

   private DoubleUnaryOperator computeFallDamageCurve() {
      DamageSource source = MC.player.damageSources().fall();
      Map<Integer, Double> byRaw = new HashMap<>();
      Long2DoubleOpenHashMap byFall = new Long2DoubleOpenHashMap();
      byFall.defaultReturnValue(0.0 / 0.0);
      return fall -> {
         long bits = Double.doubleToLongBits(fall);
         double known = byFall.get(bits);
         if (!Double.isNaN(known)) {
            return known;
         }

         int raw = ((AutismLivingEntityAccessor)MC.player).autism$calculateFallDamage(fall, 1.0F);
         double result = raw <= 0 ? 0.0 : byRaw.computeIfAbsent(raw, amount -> autismclient.util.ExplosionDamageCalculator.applyMitigations(MC.player, source, amount.intValue(), true));
         byFall.put(bits, result);
         return result;
      };
   }

   private int supportRowY() {
      if (this.launchGroundPos != null) {
         return this.launchGroundPos.getY();
      } else {
         return this.lastGroundPos != null && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.lastGroundTick, 40)
            ? this.lastGroundPos.getY()
            : Mth.floor(MC.player.getY() + 1.0E-6) - 1;
      }
   }

   private RotationMath.Rotation currentLook() {
      RotationMath.Rotation look = "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) ? autismclient.util.SilentRotationManager.getCurrentRotation() : null;
      if (look == null) {
         look = SilentMovementCoordinator.getRenderRotation(MC.player);
      }

      return look != null ? look : RotationMath.ofPlayer(MC.player);
   }

   private int placementTick(ClutchBlockPlanner.PlacementLink link) {
      return ClutchFallPhysics.choosePlacementTick(link.init_I(), link.load_I(), ClutchFallPhysics.isEnemyCause(this.launchCause));
   }

   private int earliestPlacementTick(ClutchBlockPlanner.PlacementLink link) {
      return ClutchFallPhysics.clampEarliestTick(link.init_I(), link.load_I());
   }

   private ClutchModule.PlacementStep preparePlacement(ClutchModule.CatchRoute route, ClutchBlockPlanner.PlacementLink link, int slot) {
      ItemStack stack = MC.player.getInventory().getItem(slot);
      int at = route.plan().read() ? link.init_I() : this.earliestPlacementTick(link);
      double reach = MC.player.blockInteractionRange() - 0.03;
      autismclient.util.BlockPlacementPlanner.HitValidator placement = autismclient.util.BlockPlacementPlanner.blockItemValidator(stack, MC.player, InteractionHand.MAIN_HAND);
      RotationMath.Rotation from = this.currentLook();
      autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(24);
      boolean sneaking = MC.player.isSecondaryUseActive();
      Vec3 eye = this.edgeRescue ? MC.player.getEyePosition() : route.eyeAt(at);
      Vec3 leadEye = !this.edgeRescue && !route.plan().init_Z_2() && !route.plan().read() ? route.eyeAt(at + 1) : eye;
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         new autismclient.util.BlockPlacementPlanner.Query(link.init_BlockPos(), eye, reach, placement)
            .withFromRotation(from)
            .withLeadEye(leadEye)
            .withAllowedFaces(EnumSet.of(link.init_Direction()))
            .withAllowInPlace(false)
            .withMaxPitch(89.5)
            .withSneaking(sneaking)
            .withAutoSneak(false)
            .withMarginCap(!route.plan().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
            .withRayBudget(budget)
      );
      if (candidate == null) {
         candidate = autismclient.util.BlockPlacementPlanner.plan(
            new autismclient.util.BlockPlacementPlanner.Query(link.init_BlockPos(), eye, reach, placement)
               .withFromRotation(from)
               .withLeadEye(leadEye)
               .withMaxPitch(89.5)
               .withSneaking(sneaking)
               .withAutoSneak(false)
               .withMarginCap(!route.plan().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
               .withRayBudget(budget)
         );
      }

      Direction nextFace = route.plan().init_List().size() > 1
            && route.plan().init_List().get(1).load_BlockPos().equals(link.init_BlockPos())
         ? route.plan().init_List().get(1).init_Direction()
         : null;
      if (candidate == null) {
         return null;
      }

      ClutchModule.PlacementStep prepared = new ClutchModule.PlacementStep(
         link.init_BlockPos(), candidate, link.load_I(), slot, route.plan().init_Z_2(), route.tower(), nextFace, route.plan().read()
      );
      if (nextFace != null && !prepared.read) {
         autismclient.util.BlockPlacementPlanner.PlacementPlan carried = this.placementFor(
            prepared, candidate.aim().goalRotation(), eye, route.eyeAt(at + 1), null, reach, at
         );
         if (carried != null) {
            prepared = new ClutchModule.PlacementStep(
               prepared.pos, carried, prepared.latestTick, prepared.slot, prepared.bestEffort, prepared.tower, nextFace, prepared.read
            );
         }
      }

      return prepared;
   }

   private BlockHitResult raycastBlock(Vec3 eye, RotationMath.Rotation rotation, double reach) {
      HitResult result = MC.level
         .clip(new ClipContext(eye, eye.add(Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(reach)), Block.OUTLINE, Fluid.NONE, MC.player));
      return result instanceof BlockHitResult hit && result.getType() == Type.BLOCK ? hit : null;
   }

   private boolean tryPlaceCurrent() {
      if (this.currentPlan != null
         && this.lastPlaceTick != this.clientTick()
         && MC.player.getInventory().getSelectedSlot() == this.currentPlan.slot
         && !MC.player.isUsingItem()
         && this.wireState != null
         && this.wireState.tick < this.clientTick()
         && !BlinkPacketManager.isHoldingActionsOnly()) {
         RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
         if (rotation == null) {
            return this.fail("No rotation");
         }

         ItemStack stack = MC.player.getMainHandItem();
         double reach = MC.player.blockInteractionRange() - 0.03;
         BlockPos support = this.currentPlan.placement.support().supportPos();
         Vec3 wireEye = this.wireState.position.add(0.0, this.wireState.eyeHeight, 0.0);
         if (!this.currentPlan.read
            && !ClutchFallPhysics.matchesPlacementHit(
               this.raycastBlock(wireEye, this.wireState.rotation, reach),
               support,
               this.currentPlan.placement.support().face()
            )) {
            return this.fail("Preparing written face");
         }

         List<AABB> otherBodies = this.otherBodies(this.currentPlan.tower && this.landingSafe);
         if (intersectsBodies(this.currentPlan.pos, otherBodies)) {
            return this.fail("Moving body enters placement");
         }

         autismclient.util.BlockPlacementPlanner.PlacementPlan carry = this.currentPlan.read
            ? null
            : this.carryPlacementPlan(this.currentPlan, rotation, wireEye, reach);
         if (carry != null) {
            this.currentPlan = new ClutchModule.PlacementStep(
               this.currentPlan.pos,
               carry,
               this.currentPlan.latestTick,
               this.currentPlan.slot,
               this.currentPlan.bestEffort,
               this.currentPlan.tower,
               this.currentPlan.face,
               this.currentPlan.read
            );
            autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, carry.aim().goalRotation());
            this.stepRotation(1);
            rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
            if (rotation == null) {
               return this.fail("No rotation");
            }
         }

         if (!ScaffoldModule.init_Z_Vec3(wireEye, rotation, support, reach)) {
            return this.fail("Corrected cube look");
         } else {
            Vec3 eye = MC.player.getEyePosition();
            HitResult found = MC.level
               .clip(
                  new ClipContext(eye, eye.add(Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()).scale(reach)), Block.OUTLINE, Fluid.NONE, MC.player)
               );
            if (!(
               found instanceof BlockHitResult hit
                  && found.getType() == Type.BLOCK
                  && hit.getBlockPos().equals(support)
                  && !(wireEye.distanceTo(hit.getLocation()) > reach)
                  && !autismclient.util.BlockPlacementPlanner.wouldInteractWithoutSneak(MC.level.getBlockState(support), support, MC.player.isSecondaryUseActive())
            )) {
               return this.fail("Native face or reach");
            } else {
               if (!ClutchFallPhysics.matchesPlacementHit(
                  this.raycastBlock(wireEye, this.currentPlan.read ? rotation : this.wireState.rotation, reach),
                  support,
                  hit.getDirection()
               )) {
                  return this.fail("Preparing written face");
               }

               BlockPlaceContext context = new BlockPlaceContext(MC.player, InteractionHand.MAIN_HAND, stack, hit);
               BlockPos placedCell = context.getClickedPos();
               if (!placedCell.equals(this.currentPlan.pos)) {
                  double feet = Math.min(MC.player.getY(), this.wireState.position.y);
                  if (hit.getDirection() != Direction.UP || placedCell.getY() + 1 > feet - 1.0E-4 || this.recentPlacementCells.containsKey(placedCell)) {
                     return this.fail("Native cell differs");
                  }

                  if (ClutchFallPhysics.isSelfOrGuardCause(this.launchCause)
                     && !this.currentPlan.bestEffort
                     && placedCell.getY() > this.currentPlan.pos.getY()) {
                     return this.fail("Keeping own catch row");
                  }
               }

               if (intersectsBodies(placedCell, otherBodies)) {
                  return this.fail("Moving body enters placement");
               }

               if (!context.canPlace()) {
                  return this.fail("Native placement unavailable");
               }

               if (new AABB(placedCell).intersects(MC.player.getBoundingBox())
                  || new AABB(placedCell).intersects(MC.player.getBoundingBox().move(this.wireState.position.subtract(MC.player.position())))) {
                  return this.fail("Placement intersects body");
               }

               if (!MC.level.isUnobstructed(((BlockItem)stack.getItem()).getBlock().defaultBlockState(), placedCell, CollisionContext.empty())) {
                  return this.fail("Placement obstructed");
               }

               if (!autismclient.util.InventorySlotArbiter.canBeginActionGroup(this.id()) || !PerTickPlacementLock.init(this.id())) {
                  return this.fail("Placement hand busy");
               }

               if (this.currentPlan.read
                  ? autismclient.util.ClientInteractionOverride.queueUseBlock(hit, InteractionHand.MAIN_HAND, rotation)
                  : autismclient.util.ClientInteractionOverride.queueUseBlock(hit, InteractionHand.MAIN_HAND)) {
                  this.sentPlacementHit = hit;
                  this.lastPlacedPos = placedCell.immutable();
                  this.placedSlot = this.currentPlan.slot;
                  this.placementSequence = -1;
                  this.placeHitTick = this.clientTick();
                  this.lastPlaceTick = this.clientTick();
                  this.placements++;
                  this.recentPlacementCells.put(placedCell, this.clientTick());
                  this.placedCatchCell = placedCell;
                  if (hit.getDirection().getAxis() != Axis.Y) {
                     this.hitAxisZ = hit.getDirection().getAxis() == Axis.Z;
                  }

                  this.status = "Catch placed";
                  autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, rotation);
                  return true;
               } else {
                  return this.fail("Click queue");
               }
            }
         }
      } else {
         return this.fail("Item or look interval");
      }
   }

   private autismclient.util.BlockPlacementPlanner.PlacementPlan carryPlacementPlan(
      ClutchModule.PlacementStep candidatePlan, RotationMath.Rotation rotation, Vec3 wireEye, double reach
   ) {
      Vec3 eye = MC.player.getEyePosition();
      Vec3 next = this.nextEyePosition != null ? this.nextEyePosition : eye.add(MC.player.getDeltaMovement());
      return this.placementFor(candidatePlan, rotation, eye, next, wireEye, reach, 0);
   }

   private ClutchFallPhysics.AirSweep predictAirSweep(int lead, float yaw) {
      return ClutchFallPhysics.predictAirSweep(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.getEyeHeight(),
         MC.player.getYRot(),
         yaw,
         lead,
         (body, motion, supported) -> this.steerDirection(body, motion, this.steerAxisX, 4, supported),
         AutismEntityAccessor::autism$getInputVector
      );
   }

   private autismclient.util.BlockPlacementPlanner.PlacementPlan placementFor(
      ClutchModule.PlacementStep candidatePlan, RotationMath.Rotation rotation, Vec3 eye, Vec3 next, Vec3 wireEye, double reach, int lead
   ) {
      boolean moving = this.catchMode == 4
         && lead <= 1
         && !MC.player.onGround()
         && Math.abs(MC.player.getBoundingBox().getYsize() - 1.8) < 0.001
         && !MC.options.keyShift.isDown()
         && Math.abs(MC.player.getGravity() - 0.08) < 1.0E-9
         && !MC.player.hasEffect(MobEffects.LEVITATION)
         && !MC.player.hasEffect(MobEffects.SLOW_FALLING);
      if (!moving
         && ClutchBlockPlanner.init_Z_Vec3_2(
            next, Vec3.directionFromRotation(rotation.pitch(), rotation.yaw()), candidatePlan.pos, candidatePlan.face, reach
         )) {
         return null;
      }

      autismclient.util.BlockPlacementPlanner.SupportCandidate option = candidatePlan.placement.support();
      AABB rect = option.faceRect();
      double margin = this.catchMode == 4 ? 1.0E-5 : 0.02;
      autismclient.util.BlockPlacementPlanner.HitValidator placement = autismclient.util.BlockPlacementPlanner.blockItemValidator(MC.player.getMainHandItem(), MC.player, InteractionHand.MAIN_HAND);
      autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(8);
      List<Vec3> points = new ArrayList<>(ClutchBlockPlanner.init_List_Vec3(eye, next, rect, candidatePlan.pos, candidatePlan.face, margin));

      for (double a : ClutchBlockPlanner.init_DArr) {
         for (double b : ClutchBlockPlanner.init_DArr) {
            Axis axis = option.face().getAxis();
            points.add(
               axis == Axis.X
                  ? new Vec3(rect.minX, rect.minY + (rect.maxY - rect.minY) * a, rect.minZ + (rect.maxZ - rect.minZ) * b)
                  : (
                     axis == Axis.Z
                        ? new Vec3(rect.minX + (rect.maxX - rect.minX) * b, rect.minY + (rect.maxY - rect.minY) * a, rect.minZ)
                        : new Vec3(rect.minX + (rect.maxX - rect.minX) * a, rect.minY, rect.minZ + (rect.maxZ - rect.minZ) * b)
                  )
            );
         }
      }

      List<RotationMath.Rotation> goals = new ArrayList<>();

      for (Vec3 point : points) {
         goals.add(RotationMath.applyGcd(RotationMath.lookAt(point, eye), rotation));
      }

      if (this.catchMode == 4) {
         List<ClutchBlockPlanner.AimAngles> counts = moving && candidatePlan.face != null && candidatePlan.face.getAxis() != Axis.Y
            ? ClutchBlockPlanner.init_List_Vec3(
               eye,
               rect,
               option.supportPos(),
               option.face(),
               candidatePlan.pos,
               candidatePlan.face,
               reach,
               rotation.yaw(),
               rotation.pitch(),
               RotationMath.gcdStep(),
               margin,
               yaw -> this.predictAirSweep(lead, (float)yaw)
            )
            : ClutchBlockPlanner.init_List_Vec3_2(
               eye,
               next,
               rect,
               option.supportPos(),
               option.face(),
               candidatePlan.pos,
               candidatePlan.face,
               reach,
               rotation.yaw(),
               rotation.pitch(),
               RotationMath.gcdStep(),
               margin
            );

         for (int i = counts.size() - 1; i >= 0; i--) {
            goals.addFirst(new RotationMath.Rotation(counts.get(i).init(), counts.get(i).load()));
         }
      }

      for (RotationMath.Rotation goal : goals) {
         ClutchFallPhysics.AirSweep window = moving ? this.predictAirSweep(lead, goal.yaw()) : null;
         Vec3 clickEye = window == null ? eye : window.startEye();
         Vec3 nextEye = window == null ? next : window.endEye();
         if (window == null || MC.level.noCollision(MC.player, window.sweptBox().deflate(1.0E-5))) {
            Vec3 point = clickEye.add(Vec3.directionFromRotation(goal.pitch(), goal.yaw()));
            if (!(Math.abs(goal.pitch()) > 89.5)
               && (
                  wireEye == null
                     || ClutchFallPhysics.matchesPlacementHit(this.raycastBlock(wireEye, goal, reach), option.supportPos(), option.face())
               )
               && ClutchBlockPlanner.init_Z_Vec3_2(
                  nextEye, Vec3.directionFromRotation(goal.pitch(), goal.yaw()), candidatePlan.pos, candidatePlan.face, reach
               )) {
               autismclient.util.BlockPlacementPlanner.Query request = new autismclient.util.BlockPlacementPlanner.Query(candidatePlan.pos, clickEye, reach, placement)
                  .withFromRotation(rotation)
                  .withMaxPitch(89.5)
                  .withSneaking(MC.player.isSecondaryUseActive())
                  .withAutoSneak(false)
                  .withMarginCap(margin)
                  .withRayBudget(budget);
               autismclient.util.BlockPlacementPlanner.AimSample aim = new autismclient.util.BlockPlacementPlanner.AimSample(
                  point, goal, goal, clickEye.distanceTo(point), RotationMath.vectorAngleDegrees(rotation, goal), autismclient.util.BlockPlacementPlanner.SamplePoint.NEAREST
               );
               autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.verifyPlacement(option, aim, request, null);
               if (candidate != null
                  && (!moving || candidate.aim().goalRotation().equals(goal))
                  && candidate.support().supportPos().equals(option.supportPos())
                  && candidate.support().face() == option.face()
                  && (
                     wireEye == null
                        || ClutchFallPhysics.matchesPlacementHit(
                           this.raycastBlock(wireEye, candidate.aim().goalRotation(), reach),
                           option.supportPos(),
                           option.face()
                        )
                  )
                  && ClutchBlockPlanner.init_Z_Vec3_2(
                     nextEye,
                     Vec3.directionFromRotation(candidate.aim().goalRotation().pitch(), candidate.aim().goalRotation().yaw()),
                     candidatePlan.pos,
                     candidatePlan.face,
                     reach
                  )) {
                  return candidate;
               }

               if (request.rayBudget().remaining() == 0) {
                  return null;
               }
            }
         }
      }

      return null;
   }

   private boolean fail(String reason) {
      return false;
   }

   private boolean pickSteerAxis() {
      return this.hitAxisZ != null ? this.hitAxisZ : Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
   }

   private boolean isGroundedSafe() {
      return ClutchFallPhysics.isGroundedSafe(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.onGround(),
         this.safeFallDistance(),
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
         this::mediumAt
      );
   }

   private boolean recentlyCaught() {
      return this.safeCatchTick != -2147483648 && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.safeCatchTick, 10);
   }

   private boolean shouldSteerInput(Input input) {
      if ("Holding unsafe jump".equals(this.status)) {
         this.status = "Idle";
      }

      if (this.ownSteerArmed() && MC.player.onGround() && (!input.shift() || input.jump()) && !scaffoldEnabled() && this.canAct()) {
         Vec3 wish = inputVector(input, SilentMovementCoordinator.getMovementYaw(MC.player));
         Vec3 motion = MC.player.getDeltaMovement();
         AABB body = MC.player.getBoundingBox();
         Vec3 centre = body.getCenter();
         AABB column = new AABB(centre.x - 0.05, body.minY - 0.6, centre.z - 0.05, centre.x + 0.05, body.minY - 0.01, centre.z + 0.05);
         boolean hanging = MC.level.noCollision(MC.player, column) && this.ownFallDropDangerous(this.depthBelow(column.move(0.0, body.minY - column.minY, 0.0)));
         if (hanging) {
            this.ownSteerHoldTicks = Math.max(this.ownSteerHoldTicks, 2);
         } else if (wish.lengthSqr() == 0.0 && motion.horizontalDistanceSqr() < 0.0025) {
            this.ownSteerHoldTicks = 0;
         }

         if (wish.lengthSqr() > 0.0 || motion.horizontalDistanceSqr() >= 0.0025) {
            double reserve = this.bestBlockSlot() < 0 ? 0.6 : 0.3;
            AABB ahead = body.move(motion.x * 3.0 + wish.x * reserve, 0.0, motion.z * 3.0 + wish.z * reserve);
            Vec3 next = ahead.getCenter();
            AABB feet = new AABB(next.x - 0.05, ahead.minY - 0.6, next.z - 0.05, next.x + 0.05, ahead.minY - 0.01, next.z + 0.05);
            if (MC.level.noCollision(MC.player, feet) && this.ownFallDropDangerous(this.depthBelow(feet.move(0.0, body.minY - feet.minY, 0.0)))) {
               this.ownSteerHoldTicks = 6;
            }
         }

         if (input.jump()) {
            if (((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() > 1) {
               return this.ownSteerHoldTicks-- > 0;
            }

            if (this.ignoredLaunch || this.nearOwnChargeThrow(MC.player.position()) || MC.player.hasEffect(MobEffects.JUMP_BOOST) || !this.jumpWouldBeUnsafe(input)) {
               this.ownSteerHoldTicks = 0;
               return false;
            }

            this.ownSteerHoldTicks = Math.max(this.ownSteerHoldTicks, 6);
         }

         return this.ownSteerHoldTicks-- > 0;
      } else {
         this.ownSteerHoldTicks = 0;
         return false;
      }
   }

   private boolean jumpWouldBeUnsafe(Input input) {
      autismclient.ai.engine.AiPhysics.MotionState start = autismclient.ai.engine.AiPhysics.MotionState.of(MC.player);
      if (start.jumpDelay() > 1) {
         return false;
      }

      boolean sprint = MC.player.isSprinting() || input.sprint();
      if (!sprint && al.init_Module_Str("sprint") instanceof f.Inner_MapStore module && module.isEnabled()) {
         sprint = module.init(false, false);
      }

      autismclient.ai.engine.AiPhysics.MoveInput pressed = new autismclient.ai.engine.AiPhysics.MoveInput(
         (input.forward() ? 1 : 0) - (input.backward() ? 1 : 0),
         (input.left() ? 1 : 0) - (input.right() ? 1 : 0),
         true,
         sprint,
         input.shift(),
         SilentMovementCoordinator.getMovementYaw(MC.player)
      );
      autismclient.ai.engine.AiPhysics.PhysicsWorld world = autismclient.ai.engine.AiPhysics.createLiveWorld(MC.level, MC.player);
      boolean unsafe = this.jumpTrajectoryUnsafe(start, pressed, world);
      if (!unsafe && !input.shift() && start.pose() != 0) {
         unsafe = this.jumpTrajectoryUnsafe(ClutchFallPhysics.withStandingPose(start), pressed, world);
      }

      if (unsafe) {
         this.status = "Holding unsafe jump";
      }

      return unsafe;
   }

   private boolean jumpTrajectoryUnsafe(
      autismclient.ai.engine.AiPhysics.MotionState start,
      autismclient.ai.engine.AiPhysics.MoveInput pressed,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world
   ) {
      ClutchFallPhysics.FallTrajectory intended = ClutchFallPhysics.simulateJumpTrajectory(start, pressed, world, this.safeFallDistance(), null, this::mediumAt);
      if (intended.unsafe() && !intended.steps().isEmpty()) {
         AABB last = intended.steps().getLast().body();
         if (!this.ownFallDropDangerous(this.depthBelow(last) + Math.max(0.0, start.y() - last.minY))) {
            return false;
         }

         int slot = this.bestBlockSlot();
         if (slot >= 0 && this.bool("own-steer") && (this.bool("own-knockback") || this.inGuardTime())) {
            for (boolean axis : new boolean[]{true, false}) {
               ClutchFallPhysics.FallTrajectory braking = ClutchFallPhysics.simulateJumpTrajectory(start, pressed, world, this.safeFallDistance(), axis, this::mediumAt);
               if (!braking.unsafe()) {
                  return false;
               }

               ClutchModule.CatchRoute route = this.planCatch(braking, slot);
               if (route != null) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private boolean ownFallDropDangerous(double below) {
      return this.ownSteerWindow.isActive(this.clientTick())
         && (this.bool("own-knockback") && this.isDangerousDrop(0.0, below) || (this.inGuardTime() || this.recentlyCaught()) && this.fallWouldKill(0.0, below));
   }

   private Input steerInput(Input input) {
      float keyYaw = SilentMovementCoordinator.getMovementYaw(MC.player);
      Vec3 motion = MC.player.getDeltaMovement();
      Vec3 wish = inputVector(input, keyYaw);
      boolean stocked = this.bestBlockSlot() >= 0;
      double ahead = stocked ? 0.15 : 0.6;
      double radius = stocked ? 0.24 : 0.05;
      Vec3 at = MC.player.getBoundingBox().getCenter().add(motion.x * 2.0 + wish.x * ahead, 0.0, motion.z * 2.0 + wish.z * ahead);
      AABB feet = new AABB(at.x - radius, MC.player.getY() - 0.6, at.z - radius, at.x + radius, MC.player.getY() - 0.01, at.z + radius);
      if (input.jump() || MC.level.noCollision(MC.player, feet) && this.ownFallDropDangerous(this.depthBelow(feet.move(0.0, MC.player.getY() - feet.minY, 0.0)))) {
         Vec3 brake = ClutchFallPhysics.brakingDirection(motion);
         double yaw = Math.toRadians(keyYaw);
         double forward = brake.z * Math.cos(yaw) - brake.x * Math.sin(yaw);
         double side = brake.x * Math.cos(yaw) + brake.z * Math.sin(yaw);
         return new Input(forward > 0.08, forward < -0.08, side > 0.08, side < -0.08, false, true, false);
      } else {
         return new Input(input.forward(), input.backward(), input.left(), input.right(), false, true, false);
      }
   }

   public static Input overrideInput(Input input) {
      ClutchModule clutch = instance();
      if (input != null && clutch != null && MC.player != null && MC.level != null) {
         Input result = engagedSteerInput(clutch, input);
         boolean owned = result.shift() && (!input.shift() || clutch.engaged);
         if (result.shift() != clutch.lastShift && (owned || clutch.lastShiftOwned)) {
            ((AutismLocalPlayerAccessor)MC.player).autism$setPositionReminder(20);
         }

         clutch.lastShift = result.shift();
         clutch.lastShiftOwned = owned;
         return result;
      } else {
         return input;
      }
   }

   private static Input engagedSteerInput(ClutchModule clutch, Input input) {
      if (clutch.engaged && !clutch.disengaging) {
         if (!clutch.canSteer() || !clutch.canAct() || clutch.catchMode == 3 || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.ownBoostDroppedTick, 4)) {
            return input;
         }

         if (clutch.edgeRescue) {
            return clutch.steerInput(input);
         }

         Vec3 motion = MC.player.getDeltaMovement();
         Vec3 wanted = clutch.steerDirection(MC.player.getBoundingBox(), motion, clutch.steerAxisX, clutch.catchMode, MC.player.onGround());
         if (MC.player.onGround() && motion.y <= 0.0) {
            wanted = ClutchFallPhysics.brakingDirection(motion);
         }

         double yaw = Math.toRadians(MC.player.getYRot());
         double forward = wanted.z * Math.cos(yaw) - wanted.x * Math.sin(yaw);
         double side = wanted.x * Math.cos(yaw) + wanted.z * Math.sin(yaw);
         boolean edge = MC.player.onGround() && motion.y <= 0.0 && MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(motion.x, -0.2, motion.z));
         boolean freshImpulse = clutch.launchCause != null && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2);
         boolean jump = clutch.shouldJumpOnLaunch(clutch.catchBlockSlot() >= 0, clutch.catchMode);
         return new Input(
            forward > 0.08,
            forward < -0.08,
            side > 0.08,
            side < -0.08,
            jump,
            input.shift() || clutch.clientTick() == clutch.crouchStartTick || edge && (!freshImpulse || clutch.catchBlockSlot() < 0) && !jump,
            false
         );
      } else {
         return clutch.isEnabled() && clutch.shouldSteerInput(input) ? clutch.steerInput(input) : input;
      }
   }

   public static void onMovePacket(Packet<?> packet) {
      if (packet instanceof ServerboundMovePlayerPacket move) {
         ClutchModule clutch = instance();
         if (clutch != null && MC.player != null && (clutch.isEnabled() || clutch.engaged)) {
            ClutchModule.WireState old = clutch.wireState;
            Vec3 fallback = old == null ? MC.player.position() : old.position;
            RotationMath.Rotation oldAim = old == null ? RotationMath.ofPlayer(MC.player) : old.rotation;
            clutch.wireState = new ClutchModule.WireState(
               new Vec3(move.getX(fallback.x), move.getY(fallback.y), move.getZ(fallback.z)),
               new RotationMath.Rotation(move.getYRot(oldAim.yaw()), move.getXRot(oldAim.pitch())),
               MC.player.getEyeHeight(),
               clutch.clientTick()
            );
         }
      }
   }

   private void restoreSlot() {
      if (!this.slotsReleased) {
         this.slotsReleased = true;
         autismclient.util.InventorySlotArbiter.releaseAllSlots(this.id());
         if (this.bool("restore-slot") && this.originalHotbarSlot >= 0 && this.clutchSlot >= 0 && this.originalHotbarSlot != this.clutchSlot) {
            this.restoreToSlot = this.originalHotbarSlot;
            this.restoreFromSlot = this.clutchSlot;
            this.slotRestoreTicksLeft = 20;
            this.tickSlotRestore();
         }

         if ("clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && !this.scaffoldActive()) {
            autismclient.util.SilentRotationManager.releaseOwner(this.id());
         }
      }
   }

   private void disengage() {
      if (this.engaged) {
         this.currentPlan = null;
         autismclient.util.InventorySlotArbiter.releaseAllSlots(this.id());
         if (MC.player != null && MC.level != null && MC.player.isAlive()) {
            if (!this.disengaging && !this.slotsReleased) {
               this.disengaging = true;
               this.disengageTicks = 0;
               if (this.bool("restore-slot") && this.originalHotbarSlot >= 0 && this.clutchSlot >= 0 && this.originalHotbarSlot != this.clutchSlot) {
                  this.restoreToSlot = this.originalHotbarSlot;
                  this.restoreFromSlot = this.clutchSlot;
                  this.slotRestoreTicksLeft = 20;
               }

               this.tickSlotRestore();
            }

            this.disengageTicks++;
            RotationMath.Rotation resume = ScaffoldModule.read_ce_DataUtil();
            boolean bridge = this.scaffoldActive();
            if (this.slotsReleased) {
               this.finishDisengage(bridge);
            } else {
               if (bridge && "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && this.disengageTicks < 20) {
                  RotationMath.Rotation goal = resume != null ? resume : RotationMath.ofPlayer(MC.player);
                  autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, goal);
                  autismclient.util.SilentRotationManager.stepUrgentTimed(this.id(), MC.player, Math.max(1, Math.min(8, 19 - this.disengageTicks)));
                  RotationMath.Rotation end = autismclient.util.SilentRotationManager.getCurrentRotation();
                  if (end != null && RotationMath.vectorAngleDegrees(end, goal) > 1.0F) {
                     this.status = "Handing back";
                     return;
                  }
               }

               this.finishDisengage(bridge);
            }
         } else {
            this.resetAll();
         }
      }
   }

   private boolean scaffoldActive() {
      return ScaffoldModule.read_ce_DataUtil() != null || scaffoldEnabled();
   }

   private void finishDisengage(boolean bridge) {
      RotationMath.Rotation end = autismclient.util.SilentRotationManager.getCurrentRotation();
      boolean ours = "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
      if (bridge && end != null) {
         if (ours) {
            float offset = ScaffoldModule.yawWrapOffset(end.yaw(), MC.player.getYRot());
            if (offset != 0.0F && Float.isFinite(offset)) {
               MC.player.setYRot(MC.player.getYRot() + offset);
               MC.player.yRotO += offset;
               MC.player.setYHeadRot(MC.player.getYHeadRot() + offset);
               MC.player.yHeadRotO += offset;
               MC.player.yBodyRot += offset;
               MC.player.yBodyRotO += offset;
               MC.player.yBob += offset;
               MC.player.yBobO += offset;
            }
         }

         ScaffoldModule.load_V_ce_DataUtil(end);
         if (ours) {
            autismclient.util.SilentRotationManager.resetAll();
         }
      } else if (ours) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
      }

      if (this.caughtSafely) {
         this.safeCatchTick = this.clientTick();
      }

      this.caughtSafely = false;
      this.engaged = this.disengaging = this.dangerReturning = this.slotsReleased = this.edgeRescue = false;
      this.catchMode = 0;
      this.disengageTicks = 0;
      this.status = "Idle";
      this.originalHotbarSlot = this.clutchSlot = -1;
   }

   private void tickRotationReturn() {
      if (!this.engaged && MC.player != null && this.rotationReturning()) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   private void tickSlotRestore() {
      if (this.restoreToSlot >= 0 && MC.player != null) {
         if ((!this.engaged || this.disengaging || this.slotsReleased) && --this.slotRestoreTicksLeft >= 0 && MC.player.getInventory().getSelectedSlot() == this.restoreFromSlot) {
            autismclient.util.VirtualInputDispatcher.requestSlot(this.restoreToSlot, this.restoreFromSlot);
         } else {
            this.restoreToSlot = -1;
         }
      }
   }

   @Override
   public boolean shouldCancelAttack(HitResult hit) {
      return init_Z_49();
   }

   @Override
   public boolean shouldCancelUse(HitResult hit, InteractionHand hand) {
      return init_Z_49();
   }

   @Override
   public void onDisable() {
      PlacedBlockTrailRenderer.recordPlacement(PlacedBlockTrailRenderer.Inner_DataUtil.CLUTCH);
      this.launchCause = null;
      this.launchDamageTick = -2147483648;
      if (this.engaged) {
         autismclient.util.ClientInteractionOverride.clearAll();
      }

      this.disengage();
   }

   @Override
   public void onGameLeft() {
      this.resetAll();
      PlacedBlockTrailRenderer.load(PlacedBlockTrailRenderer.Inner_DataUtil.CLUTCH);
   }

   private void resetAll() {
      if ("clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      autismclient.util.InventorySlotArbiter.releaseAllClaims(this.id());
      this.projectileSamples.clear();
      this.recentPlacementCells.clear();
      this.launchCause = null;
      this.launchDamageTick = -2147483648;
      this.wireState = null;
      this.lastLevel = null;
      this.engaged = this.disengaging = this.landingSafe = this.dangerReturning = this.slotsReleased = false;
      this.disengageTicks = 0;
      this.originalHotbarSlot = this.clutchSlot = -1;
      this.currentPlan = null;
      this.safeCatchTick = -2147483648;
      this.caughtSafely = false;
      this.ownSteerWindow.clear();
      this.preImpulseMotion = null;
      this.postImpulseMotion = Vec3.ZERO;
      this.serverImpulseTick = this.unattributedHurtTick = -2147483648;
      this.disallowedCauseTick = -2147483648;
      this.restoreToSlot = this.restoreFromSlot = -1;
      this.placedCatchCell = this.catchTarget = null;
      this.lastGroundPos = this.launchGroundPos = this.rowReturnPos = null;
      this.lastGroundTick = -2147483648;
      this.status = "Idle";
      this.safeLandingTicks = 0;
      this.lastImpulseTick = -2147483648;
      this.ownSteerHoldTicks = 0;
      this.catchMode = 0;
      this.edgeRescue = false;
      this.enemyLaunchedUp = false;
      this.hitAxisZ = null;
      this.lastShift = this.lastShiftOwned = false;
      this.crouchStartTick = -2147483648;
      this.nextEyePosition = null;
      this.causeDebug = "";
      this.chargeThrowTick = -2147483648;
      this.chargeThrowEye = null;
      this.ownBlastTick = -2147483648;
      this.ownBlastCenter = null;
      this.ownBoostDroppedTick = -2147483648;
      this.ignoredLaunch = false;
      this.ignoredLaunchTick = -2147483648;
      this.lastRouteTick = -2147483648;
      this.groundedLaunchTick = -2147483648;
      this.sentPlacementHit = null;
      this.lastPlacedPos = null;
      this.placedSlot = this.placementSequence = -1;
      this.placeHitTick = -2147483648;
   }

   private record CatchRoute(ClutchBlockPlanner.ClutchPlan plan, List<AABB> bodies, double eyeHeight, boolean tower, List<Vec3> load) {
      CatchRoute(ClutchBlockPlanner.ClutchPlan plan, List<AABB> bodies, double eyeHeight) {
         this(plan, bodies, eyeHeight, false, List.of());
      }

      CatchRoute(ClutchBlockPlanner.ClutchPlan plan, List<AABB> bodies, double eyeHeight, boolean tower) {
         this(plan, bodies, eyeHeight, tower, List.of());
      }

      Vec3 eyeAt(int tick) {
         return this.load.isEmpty() ? eyeFromBodies(this.bodies, this.eyeHeight, tick) : this.load.get(Mth.clamp(tick, 0, this.load.size() - 1));
      }

      RotationMath.Rotation aimAt(ClutchBlockPlanner.PlacementLink link, int tick) {
         return aimFrom(this.eyeAt(tick), link);
      }

      static Vec3 eyeFromBodies(List<AABB> bodies, double eyeHeight, int tick) {
         AABB body = bodies.get(Mth.clamp(tick, 0, bodies.size() - 1));
         return new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
      }

      static RotationMath.Rotation unusedAimFromBodies(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.PlacementLink link, int tick) {
         return aimFrom(eyeFromBodies(bodies, eyeHeight, tick), link);
      }

      static RotationMath.Rotation aimFrom(Vec3 eye, ClutchBlockPlanner.PlacementLink link) {
         BlockPos support = link.load_BlockPos();
         RotationMath.Rotation goal = RotationMath.lookAt(ClutchBlockPlanner.init_Vec3_Vec3(eye, support.getX(), support.getY(), support.getZ(), link.init_Direction()), eye);
         return new RotationMath.Rotation(goal.yaw(), Mth.clamp(goal.pitch(), -89.5F, 89.5F));
      }
   }

   private record ProjectileSample(int entityId, ClutchFallPhysics.HitCause cause, boolean own, boolean attributed, Vec3 position, Vec3 velocity, int tick) {
   }

   private record WireState(Vec3 position, RotationMath.Rotation rotation, double eyeHeight, int tick) {
   }

   private record PlacementStep(
      BlockPos pos,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      int latestTick,
      int slot,
      boolean bestEffort,
      boolean tower,
      Direction face,
      boolean read
   ) {
   }

   private final class WorldCache {
      private boolean active;
      private final Long2ByteOpenHashMap supportCache = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap replaceableCache = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap cellCache = new Long2ByteOpenHashMap();
      private final MutableBlockPos scratchPos = new MutableBlockPos();
      private boolean sneaking;
      private List<AABB> otherBodies;
      private List<AABB> optionalOtherBodies;
      private ClutchModule.WireState rayWireState;
      private double cachedRange = 0.0 / 0.0;
      private BlockHitResult cachedWireRay;
      private DoubleUnaryOperator fallDamageCurve;

      void begin() {
         this.active = true;
         this.supportCache.clear();
         this.replaceableCache.clear();
         this.cellCache.clear();
         this.otherBodies = this.optionalOtherBodies = null;
         this.rayWireState = null;
         this.cachedRange = 0.0 / 0.0;
         this.cachedWireRay = null;
         this.fallDamageCurve = null;
      }

      void deactivate() {
         this.active = false;
         this.otherBodies = this.optionalOtherBodies = null;
         this.rayWireState = null;
         this.cachedWireRay = null;
         this.fallDamageCurve = null;
      }

      boolean isSupport(int x, int y, int z, boolean sneaking) {
         if (!this.active) {
            return this.computeSupport(x, y, z, sneaking);
         }

         if (this.sneaking != sneaking) {
            this.supportCache.clear();
            this.sneaking = sneaking;
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.supportCache.get(key);
         if (known == 0) {
            this.supportCache.put(key, known = (byte)(this.computeSupport(x, y, z, sneaking) ? 1 : 2));
         }

         return known == 1;
      }

      boolean isReplaceable(int x, int y, int z) {
         if (!this.active) {
            return this.computeReplaceable(x, y, z);
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.replaceableCache.get(key);
         if (known == 0) {
            this.replaceableCache.put(key, known = (byte)(this.computeReplaceable(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean openAirWithin(Vec3 eye, double distance) {
         double reach = distance * distance;
         int minX = Mth.floor(eye.x - distance - 0.5) >> 2;
         int maxX = Mth.ceil(eye.x + distance - 0.5) >> 2;
         int minY = Mth.floor(eye.y - distance - 0.5) >> 2;
         int maxY = Mth.ceil(eye.y + distance - 0.5) >> 2;
         int minZ = Mth.floor(eye.z - distance - 0.5) >> 2;
         int maxZ = Mth.ceil(eye.z + distance - 0.5) >> 2;

         for (int qx = minX; qx <= maxX; qx++) {
            double dx = distanceToCube(eye.x, qx);

            for (int qz = minZ; qz <= maxZ; qz++) {
               double dz = distanceToCube(eye.z, qz);
               if (!(dx * dx + dz * dz > reach)) {
                  for (int qy = minY; qy <= maxY; qy++) {
                     double dy = distanceToCube(eye.y, qy);
                     if (!(dx * dx + dy * dy + dz * dz > reach)) {
                        if (!this.active) {
                           if (this.cellHasBlocks(qx, qy, qz)) {
                              return false;
                           }
                        } else {
                           long key = BlockPos.asLong(qx, qy, qz);
                           byte known = this.cellCache.get(key);
                           if (known == 0) {
                              this.cellCache.put(key, known = (byte)(this.cellHasBlocks(qx, qy, qz) ? 2 : 1));
                           }

                           if (known == 2) {
                              return false;
                           }
                        }
                     }
                  }
               }
            }
         }

         return true;
      }

      private static double distanceToCube(double eye, int cube) {
         double low = cube * 4 + 0.5;
         double high = cube * 4 + 3.5;
         return eye < low ? low - eye : (eye > high ? eye - high : 0.0);
      }

      private boolean cellHasBlocks(int qx, int qy, int qz) {
         int sy = qy >> 2;
         if (sy >= Module.MC.level.getMinSectionY() && sy <= Module.MC.level.getMaxSectionY()) {
            if (!(Module.MC.level.getChunkSource().getChunk(qx >> 2, qz >> 2, ChunkStatus.FULL, false) instanceof LevelChunk chunk)) {
               return false;
            } else {
               LevelChunkSection[] var15 = chunk.getSections();
               int index = Module.MC.level.getSectionIndexFromSectionY(sy);
               if (index >= 0 && index < var15.length) {
                  LevelChunkSection section = var15[index];
                  if (section != null && !section.hasOnlyAir()) {
                     int lx = (qx & 3) << 2;
                     int ly = (qy & 3) << 2;
                     int lz = (qz & 3) << 2;

                     for (int x = lx; x < lx + 4; x++) {
                        for (int y = ly; y < ly + 4; y++) {
                           for (int z = lz; z < lz + 4; z++) {
                              if (!section.getBlockState(x, y, z).canBeReplaced()) {
                                 return true;
                              }
                           }
                        }
                     }

                     return false;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      List<AABB> cachedOtherBodies(boolean optional) {
         if (!this.active) {
            return ClutchModule.this.otherBodies(optional);
         } else if (optional) {
            return this.optionalOtherBodies != null ? this.optionalOtherBodies : (this.optionalOtherBodies = ClutchModule.this.otherBodies(true));
         } else {
            return this.otherBodies != null ? this.otherBodies : (this.otherBodies = ClutchModule.this.otherBodies(false));
         }
      }

      BlockHitResult wireRay(double range) {
         ClutchModule.WireState written = ClutchModule.this.wireState;
         if (this.active && written == this.rayWireState && range == this.cachedRange) {
            return this.cachedWireRay;
         }

         BlockHitResult result = written != null && written.tick < ClutchModule.this.clientTick()
            ? ClutchModule.this.raycastBlock(written.position.add(0.0, written.eyeHeight, 0.0), written.rotation, range)
            : null;
         if (this.active) {
            this.rayWireState = written;
            this.cachedRange = range;
            this.cachedWireRay = result;
         }

         return result;
      }

      DoubleUnaryOperator fallDamageCurve() {
         if (!this.active) {
            return ClutchModule.this.computeFallDamageCurve();
         } else {
            return this.fallDamageCurve != null ? this.fallDamageCurve : (this.fallDamageCurve = ClutchModule.this.computeFallDamageCurve());
         }
      }

      private boolean computeSupport(int x, int y, int z, boolean sneaking) {
         this.scratchPos.set(x, y, z);
         if (!Module.MC.level.hasChunkAt(this.scratchPos)) {
            return false;
         }

         BlockState state = Module.MC.level.getBlockState(this.scratchPos);
         return !state.canBeReplaced() && autismclient.util.BlockPlacementPlanner.isValidSupportBlock(state, this.scratchPos.immutable(), sneaking);
      }

      private boolean computeReplaceable(int x, int y, int z) {
         this.scratchPos.set(x, y, z);
         if (!Module.MC.level.isOutsideBuildHeight(this.scratchPos) && Module.MC.level.hasChunkAt(this.scratchPos)) {
            BlockState state = Module.MC.level.getBlockState(this.scratchPos);
            return state.canBeReplaced() && state.getFluidState().isEmpty();
         } else {
            return false;
         }
      }
   }
}
