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
   private final Map<Integer, ClutchModule.Inner_CachePool> projectileSamples = new HashMap<>();
   private final Map<BlockPos, Integer> recentPlacementCells = new HashMap<>();
   private Object lastLevel;
   private ClutchFallPhysics.Inner_TaskQueue launchCause;
   private int poll_I = -2147483648;
   private int push_I = -2147483648;
   private int pop_I;
   private int tick;
   private int run = -2147483648;
   private boolean engaged;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private boolean slotsReleased;
   private int get;
   private int set = 1;
   private int put;
   private int add = -2147483648;
   private boolean sync_Z;
   private final ClutchFallPhysics.Inner_NetHelper ownSteerWindow = new ClutchFallPhysics.Inner_NetHelper(160);
   private Vec3 preImpulseMotion;
   private Vec3 postImpulseMotion = Vec3.ZERO;
   private int clear = -2147483648;
   private int reset = -2147483648;
   private int start = -2147483648;
   private String causeDebug = "";
   private int stop = -2147483648;
   private int next = -1;
   private int prev = -1;
   private int valid = -1;
   private int size = -1;
   private int data;
   private ClutchModule.Inner_DataUtil currentPlan;
   private BlockPos init_BlockPos;
   private BlockPos load_BlockPos;
   private BlockPos read_BlockPos;
   private BlockPos write_BlockPos;
   private int value = -2147483648;
   private int cache;
   private boolean flush_Z;
   private int state;
   private boolean close_Z;
   private boolean lastShift;
   private boolean lastShiftOwned;
   private int queue = -2147483648;
   private Vec3 nextEyePosition;
   private Boolean hitAxisZ;
   private boolean push_Z;
   private volatile ClutchModule.Inner_ConfigStore wireState;
   private int buffer = -2147483648;
   private int entry = -2147483648;
   private boolean pop_Z;
   private int node = -2147483648;
   private int task = -2147483648;
   private int pool = -2147483648;
   private Vec3 write_Vec3;
   private int index = -2147483648;
   private Vec3 ownBlastCenter;
   private static final int count = 100;
   private int flags;
   private int level;
   private String status = "Idle";
   private BlockHitResult sentPlacementHit;
   private BlockPos lastPlacedPos;
   private int frame = -1;
   private int block = -1;
   private int chunk = -2147483648;
   private BlockPos sync_BlockPos;
   private static final double read_D = 0.05;
   private static final double write_D = 0.6;
   private static final double update_D = 0.15;
   private static final int table = 3;
   private static final int store = 8;
   private final ClutchModule.Inner_StreamUtil worldCache = new ClutchModule.Inner_StreamUtil();

   public ClutchModule() {
      super("clutch", "Clutch", ae.init_ae, "Saves you from knockback falls");
      this.load_V_32();
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

   private void load_V_32() {
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
      return clutch != null && clutch.engaged && !clutch.load_Z && clutch.canSteer() && clutch.state != 3 && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean sync_Z_10() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && !clutch.load_Z && clutch.canSteer() && clutch.state != 3 && !ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 4);
   }

   public static boolean flush_Z_9() {
      ClutchModule clutch = instance();
      return clutch != null
         && clutch.isEnabled()
         && clutch.set()
         && (clutch.engaged && !clutch.load_Z ? clutch.canSteer() && clutch.state != 3 : clutch.put > 0 && clutch.pop_Z_3());
   }

   public static boolean close_Z_8() {
      ClutchModule clutch = instance();
      return clutch != null && clutch.engaged && "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean open_Z_5() {
      ClutchModule clutch = instance();
      return init_Z_49() && (clutch.read_Z || clutch.load_Z || clutch.write_Z);
   }

   @Override
   public boolean isSilentMovementActive() {
      return (close_Z_8() || this.rotationReturning()) && !aD.init_Z() && !FreecamController.isActive();
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.engaged || this.rotationReturning() || this.valid >= 0;
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && MC.player != null) {
         if (this.engaged) {
            this.disengage();
         }

         this.tickRotationReturn();
         this.run_V_3();
      }
   }

   public String getStatus() {
      return this.status;
   }

   public boolean isOwnSteerActive() {
      return this.ownSteerWindow.isActive(this.clientTick());
   }

   public int init_I_29() {
      return this.flags;
   }

   public int load_I_15() {
      return this.level;
   }

   private int clientTick() {
      return AutismSessionState.getInstance().getClientTick();
   }

   private boolean isAllowedCause(ClutchFallPhysics.Inner_TaskQueue value) {
      return ClutchFallPhysics.isAllowedCause(value, this.bool("own-knockback"), this.bool("melee"), this.bool("projectiles"), this.bool("explosions"));
   }

   private boolean canSteer() {
      return ClutchFallPhysics.isSourceEnabled(this.launchCause, this.bool("steer"), this.bool("own-steer"))
         && (!ClutchFallPhysics.isSelfOrGuardCause(this.launchCause) || this.ownSteerWindow.isActive(this.clientTick()));
   }

   private boolean pop_Z_3() {
      return this.ownSteerWindow.isActive(this.clientTick()) && this.bool("own-steer") && (this.bool("own-knockback") || this.inGuardTime() || this.prev());
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
         if (!clutch.run_Z_3() && (!clutch.engaged || ClutchFallPhysics.isSelfOrGuardCause(clutch.launchCause))) {
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
         ClutchFallPhysics.Inner_StreamUtil cause = causeOf(direct);
         if (cause == ClutchFallPhysics.Inner_StreamUtil.OTHER && damage.is(DamageTypes.ARROW)) {
            cause = ClutchFallPhysics.Inner_StreamUtil.ARROW;
         }

         if (cause == ClutchFallPhysics.Inner_StreamUtil.OTHER && (damage.is(DamageTypes.PLAYER_ATTACK) || damage.is(DamageTypes.MOB_ATTACK))) {
            cause = ClutchFallPhysics.Inner_StreamUtil.MELEE;
         }

         ClutchModule.Inner_CachePool seen = direct == null ? null : clutch.projectileSamples.get(direct.getId());
         boolean own = owner == MC.player || owner == null && seen != null && seen.own;
         ClutchFallPhysics.Inner_TaskQueue hit = new ClutchFallPhysics.Inner_TaskQueue(
            cause, own, cause != ClutchFallPhysics.Inner_StreamUtil.OTHER || owner instanceof LivingEntity || direct instanceof Projectile && owner != null, clutch.clientTick()
         );
         if (cause != ClutchFallPhysics.Inner_StreamUtil.OTHER || direct instanceof Projectile) {
            clutch.start = clutch.isAllowedCause(hit) ? -2147483648 : clutch.clientTick();
            if (!clutch.isAllowedCause(hit)) {
               clutch.write_V_20();
            }
         }

         if (clutch.isAllowedCause(hit)) {
            clutch.launchCause = hit;
            clutch.poll_I = clutch.clientTick();
            clutch.write_BlockPos = clutch.read_BlockPos;
            clutch.push_I = clutch.clientTick();
            clutch.update_V_16();
            if (!hit.own()) {
               clutch.entry = -2147483648;
            }
         } else if (hit.own() && cause != ClutchFallPhysics.Inner_StreamUtil.OTHER) {
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
         clutch.stop = clutch.clientTick();
         if (!clutch.isAllowedCause(hit) && cause == ClutchFallPhysics.Inner_StreamUtil.OTHER && owner == null && direct == null) {
            clutch.reset = clutch.clientTick();
            if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.clear, 2)) {
               clutch.init_V_Z_10(true);
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
         clutch.push_I = clutch.clientTick();
         Vec3 before = clutch.preImpulseMotion == null ? Vec3.ZERO : clutch.preImpulseMotion;
         clutch.preImpulseMotion = null;
         Vec3 push = MC.player.getDeltaMovement().subtract(before);
         boolean impulse = ClutchFallPhysics.hasImpulseChange(before, MC.player.getDeltaMovement(), ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.reset, 3));
         if (impulse) {
            clutch.clear = clutch.clientTick();
            clutch.postImpulseMotion = MC.player.getDeltaMovement();
         }

         if (impulse || clutch.causeDebug.isEmpty()) {
            String line = String.format(Locale.ROOT, "push %.2f/%.2f before=%s", push.horizontalDistance(), push.y, before == Vec3.ZERO ? "none" : "seen");
            clutch.causeDebug = clutch.stop == clutch.clientTick() ? clutch.causeDebug + " | " + line : line;
            clutch.stop = clutch.clientTick();
         }

         if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 3)) {
            clutch.dropOwnBoost();
         } else {
            ClutchModule.Inner_CachePool nearest = null;
            double score = 1.0 / 0.0;

            for (ClutchModule.Inner_CachePool sample : clutch.projectileSamples.values()) {
               if (ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), sample.tick, 4) && !(sample.velocity.lengthSqr() < 0.001)) {
                  double distance = init_D_AABB(MC.player.getBoundingBox().inflate(0.45), sample.position, sample.position.add(sample.velocity.scale(4.0)));
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
               ClutchFallPhysics.Inner_TaskQueue hit = new ClutchFallPhysics.Inner_TaskQueue(
                  nearest.cause, nearest.own || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 3), nearest.load_Z || impulse, clutch.clientTick()
               );
               boolean enemyDamage = clutch.launchCause != null && !clutch.launchCause.own() && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.poll_I, 2);
               if (nearest.own && !enemyDamage || clutch.launchCause == null || !ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 4)) {
                  clutch.causeDebug = clutch.causeDebug + " projectile " + nearest.cause + (nearest.own ? " own" : "");
                  clutch.launchCause = clutch.isAllowedCause(hit) ? hit : null;
               }

               if (clutch.isAllowedCause(hit)) {
                  clutch.write_BlockPos = clutch.read_BlockPos;
               } else if (nearest.own && !enemyDamage) {
                  clutch.dropOwnBoost();
               }
            }

            boolean excluded = ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.start, 3)
               || nearest != null
                  && !clutch.isAllowedCause(new ClutchFallPhysics.Inner_TaskQueue(nearest.cause, nearest.own, nearest.load_Z || impulse, clutch.clientTick()));
            if (!impulse
               || excluded
               || clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               if (impulse && excluded) {
                  clutch.write_V_20();
               }
            } else {
               clutch.init_V_Z_10(ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.reset, 3));
            }

            if (clutch.engaged && clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               clutch.update_V_16();
            }

            if (clutch.isAllowedCause(clutch.launchCause) && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2)) {
               clutch.sync_V_11();
            }
         }
      }
   }

   public static void onExplosion(ClientboundExplodePacket packet) {
      ClutchModule clutch = instance();
      if (clutch != null && clutch.isEnabled() && MC.player != null) {
         clutch.resetOnLevelChange();
         ClutchModule.Inner_CachePool nearest = null;
         double best = 16.0;

         for (ClutchModule.Inner_CachePool sample : clutch.projectileSamples.values()) {
            if ((sample.cause == ClutchFallPhysics.Inner_StreamUtil.FIREBALL || sample.cause == ClutchFallPhysics.Inner_StreamUtil.WIND)
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
                  && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.index, 100)
                  && clutch.ownBlastCenter.distanceToSqr(packet.center()) <= 64.0
               || clutch.init_Z_Vec3_3(packet.center());
            boolean own = nearest != null
               ? nearest.own || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 3)
               : ownBlast || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 3);
            ClutchFallPhysics.Inner_TaskQueue hit = new ClutchFallPhysics.Inner_TaskQueue(
               nearest != null ? nearest.cause : ClutchFallPhysics.Inner_StreamUtil.BLAST, own, nearest == null || nearest.load_Z, clutch.clientTick()
            );
            if (nearest == null && !clutch.isAllowedCause(hit)) {
               boolean enemyHit = clutch.launchCause != null
                  && !clutch.launchCause.own()
                  && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 4);
               if (own && !enemyHit) {
                  clutch.dropOwnBoost();
               } else {
                  clutch.write_V_20();
               }
            } else {
               clutch.start = clutch.isAllowedCause(hit) ? -2147483648 : clutch.clientTick();
               if (!clutch.isAllowedCause(hit)) {
                  clutch.write_V_20();
               }

               clutch.launchCause = clutch.isAllowedCause(hit) ? hit : null;
               clutch.push_I = clutch.clientTick();
               if (clutch.isAllowedCause(hit)) {
                  clutch.write_BlockPos = clutch.read_BlockPos;
               } else if (own) {
                  clutch.dropOwnBoost();
               }

               if (clutch.engaged && clutch.isAllowedCause(hit)) {
                  clutch.update_V_16();
               }

               if (clutch.isAllowedCause(hit)) {
                  clutch.sync_V_11();
               }
            }
         } else {
            boolean ours = nearest != null ? nearest.own : clutch.init_Z_Vec3_3(packet.center());
            if (ours && !clutch.engaged && !clutch.run_Z_3()) {
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
      this.poll_I = -2147483648;
      this.entry = this.clientTick();
      this.pool = -2147483648;
      this.write_V_20();
      if (this.engaged) {
         autismclient.util.ClientInteractionOverride.clearAll();
         this.disengage();
      }
   }

   private void write_V_20() {
      this.pop_Z = true;
      this.node = this.clientTick();
   }

   private boolean init_Z_Vec3_3(Vec3 near) {
      return this.write_Vec3 != null && near != null && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.buffer, 20) && this.write_Vec3.distanceToSqr(near) <= 64.0;
   }

   private boolean run_Z_3() {
      return ClutchFallPhysics.isEnemyCause(this.launchCause) && this.isAllowedCause(this.launchCause) && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.launchCause.tick(), 4);
   }

   private void init_V_Z_10(boolean hurt) {
      if (this.bool("melee") && MC.level != null && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.entry, 3) && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.start, 3)) {
         if (!this.engaged && this.postImpulseMotion.y >= 0.55 && this.init_Z_Vec3_3(MC.player.position())) {
            this.causeDebug = this.causeDebug + " own boost";
            this.dropOwnBoost();
         } else {
            this.causeDebug = this.causeDebug + " server impulse hurt=" + hurt;
            this.launchCause = new ClutchFallPhysics.Inner_TaskQueue(ClutchFallPhysics.Inner_StreamUtil.SERVER_PUSH, false, true, this.clientTick());
            this.poll_I = this.push_I = this.clientTick();
            this.write_BlockPos = this.read_BlockPos;
            this.update_V_16();
            this.sync_V_11();
         }
      }
   }

   private void update_V_16() {
      if (this.engaged && ClutchFallPhysics.isEnemyCause(this.launchCause)) {
         if (this.state == 0 || this.state == 3) {
            this.flush_Z = this.stop();
         }

         this.state = ClutchFallPhysics.normalizeMode(this.state);
         this.load_Z = false;
         this.get = 0;
         this.tick = 0;
         this.pop_I = this.clientTick();
      }
   }

   private boolean init_Z_I_9(int tick) {
      return ClutchFallPhysics.isEnemyCause(this.launchCause) && !MC.player.onGround() && ClutchFallPhysics.isRecentKnockbackLaunch(tick, this.launchCause.tick(), this.clear, this.value, 160);
   }

   private boolean inGuardTime() {
      int ticks = (int)Math.round(this.decimal("guard-time") * 20.0);
      return ticks > 0 && this.add != -2147483648 && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.add, ticks);
   }

   private void sync_V_11() {
      float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
      if (MC.level != null
         && MC.player.onGround()
         && MC.player.getDeltaMovement().y < jump
         && !MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(0.0, -0.08, 0.0))) {
         this.pool = this.clientTick();
      }
   }

   private boolean init_Z_Z_I_2(boolean stocked, int mode) {
      if (stocked && mode != 2 && !this.load_Z && MC.player.onGround() && ((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() <= 1) {
         return ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.pool, 2) ? true : this.engaged && !this.read_Z && ClutchFallPhysics.isEnemyCause(this.launchCause) && !this.next();
      } else {
         return false;
      }
   }

   private static double init_D_AABB(AABB box, Vec3 from, Vec3 to) {
      return !box.contains(from) && !box.clip(from, to).isPresent() ? 1.0 / 0.0 : 0.0;
   }

   private static ClutchFallPhysics.Inner_StreamUtil causeOf(Entity entity) {
      if (entity instanceof Fireball) {
         return ClutchFallPhysics.Inner_StreamUtil.FIREBALL;
      } else if (entity instanceof AbstractWindCharge) {
         return ClutchFallPhysics.Inner_StreamUtil.WIND;
      } else if (entity instanceof AbstractArrow) {
         return ClutchFallPhysics.Inner_StreamUtil.ARROW;
      } else {
         return !(entity instanceof Snowball) && !(entity instanceof ThrownEgg) ? ClutchFallPhysics.Inner_StreamUtil.OTHER : ClutchFallPhysics.Inner_StreamUtil.SNOWBALL;
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

            int slot = clutch.valid;
            int from = clutch.size;
            if (slot < 0 && clutch.engaged && clutch.bool("restore-slot") && clutch.next >= 0 && clutch.prev >= 0 && clutch.next != clutch.prev) {
               slot = clutch.next;
               from = clutch.prev;
            }

            clutch.resetAll();
            clutch.lastLevel = MC.level;
            if (slot >= 0) {
               clutch.valid = slot;
               clutch.size = from;
               clutch.data = 20;
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
         .getEntities(MC.player, MC.player.getBoundingBox().inflate(10.0), candidate -> causeOf(candidate) != ClutchFallPhysics.Inner_StreamUtil.OTHER)) {
         Projectile projectile = (Projectile)entity;
         Entity owner = projectile.getOwner();
         ClutchModule.Inner_CachePool old = this.projectileSamples.get(entity.getId());
         boolean own = owner != null ? owner == MC.player : old != null && old.own;
         boolean launched = causeOf(entity) == ClutchFallPhysics.Inner_StreamUtil.FIREBALL || causeOf(entity) == ClutchFallPhysics.Inner_StreamUtil.WIND;
         if (old == null
            && owner == null
            && launched
            && this.write_Vec3 != null
            && ClutchFallPhysics.isWithinTickWindow(tick, this.buffer, 8)
            && entity.position().distanceToSqr(this.write_Vec3) < 9.0) {
            own = true;
         }

         boolean incoming = entity.position().distanceToSqr(MC.player.getEyePosition()) > 9.0
            && entity.getDeltaMovement().dot(MC.player.getEyePosition().subtract(entity.position())) > 0.05;
         boolean attributed = own || owner instanceof LivingEntity || old != null && old.load_Z || incoming;
         this.projectileSamples
            .put(
               entity.getId(),
               new ClutchModule.Inner_CachePool(entity.getId(), causeOf(entity), own, attributed, entity.position(), entity.getDeltaMovement(), tick)
            );
      }
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (packet instanceof ServerboundUseItemOnPacket use
         && use.hand() == InteractionHand.MAIN_HAND
         && use.hitResult() == this.sentPlacementHit
         && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.chunk, 1)) {
         this.block = use.sequence();
      }

      InteractionHand hand = packet instanceof ServerboundUseItemPacket use
         ? use.hand()
         : (packet instanceof ServerboundUseItemOnPacket use ? use.hand() : null);
      if (MC.player != null && hand != null && (MC.player.getItemInHand(hand).is(Items.FIRE_CHARGE) || MC.player.getItemInHand(hand).is(Items.WIND_CHARGE))) {
         this.buffer = this.clientTick();
         this.write_Vec3 = MC.player.getEyePosition();
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
            this.index = this.clientTick();
            this.ownBlastCenter = Vec3.atCenterOf(clicked);
         }
      }

      if (MC.level != null && packet instanceof ServerboundAttackPacket attack && MC.level.getEntity(attack.entityId()) instanceof EndCrystal crystal) {
         this.index = this.clientTick();
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
         && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.chunk, 1)) {
         if (result != null && result.consumesAction()) {
            ScaffoldModule.init_V_I_I(clutch.frame, clutch.block);
            if (clutch.lastPlacedPos != null && MC.level != null && !MC.level.getBlockState(clutch.lastPlacedPos).isAir()) {
               PlacedBlockTrailRenderer.init(PlacedBlockTrailRenderer.Inner_DataUtil.CLUTCH, clutch.lastPlacedPos);
            }
         }

         clutch.sentPlacementHit = null;
         clutch.lastPlacedPos = null;
         clutch.frame = clutch.block = -1;
      }
   }

   private boolean set() {
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

   private static boolean put() {
      Module module = al.init_Module_Str("scaffold");
      return module != null && module.isEnabled();
   }

   @Override
   public void preMovementTick() {
      this.worldCache.begin();

      try {
         this.open_V_5();
      } finally {
         this.worldCache.load();
      }
   }

   private void open_V_5() {
      if (MC.player != null && MC.level != null) {
         this.resetOnLevelChange();
         this.write_Z = false;
         this.tickRotationReturn();
         this.run_V_3();
         if (this.pop_Z && (MC.player.onGround() || MC.player.isInWater() || MC.player.isInLava()) && !ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.node, 2)) {
            this.pop_Z = false;
         }

         if (!MC.player.isInWater() && !MC.player.isInLava()) {
            this.ownSteerWindow.update(this.clientTick(), MC.player.onGround(), MC.player.getDeltaMovement().y);
         } else {
            this.ownSteerWindow.clear();
         }

         if (this.isEnabled() && this.set()) {
            if (this.close_Z && !this.canSteer()) {
               this.close_Z = false;
            }

            this.pruneSamples();
            int tick = this.clientTick();
            if (MC.player.onGround()) {
               this.write(tick);
            }

            if (!this.engaged
               && ClutchFallPhysics.isEnemyCause(this.launchCause)
               && !ClutchFallPhysics.isWithinTickWindow(tick, this.launchCause.tick(), 2)
               && !ClutchFallPhysics.isWithinTickWindow(tick, this.push_I, 2)
               && this.next()) {
               this.launchCause = null;
               this.poll_I = -2147483648;
               this.push_Z = false;
            }

            if (this.close_Z && this.init_BlockPos != null && !this.load_Z_I_5(tick)) {
               this.restoreSlot();
               this.disengage();
            } else {
               if (this.close_Z && !MC.player.onGround()) {
                  this.close_Z = false;
               }

               this.recentPlacementCells.entrySet().removeIf(entry -> tick - entry.getValue() > 1);
               boolean freshHit = this.isAllowedCause(this.launchCause)
                  && (ClutchFallPhysics.isWithinTickWindow(tick, this.launchCause.tick(), 24) && ClutchFallPhysics.isWithinTickWindow(tick, this.push_I, 24) || this.init_Z_I_9(tick));
               boolean walkingEdge = !this.engaged && !freshHit && this.reset();
               if (this.engaged || freshHit || walkingEdge || this.read_Z(tick)) {
                  ClutchFallPhysics.Inner_ConfigStore forecast = walkingEdge ? this.forecastWalking() : this.forecastFall();
                  if (!this.engaged && !freshHit) {
                     if (walkingEdge) {
                        if (!forecast.unsafe() || forecast.steps().isEmpty()) {
                           return;
                        }

                        AABB last = forecast.steps().getLast().body();
                        if (!this.isDangerousDrop(Math.max(0.0, MC.player.getY() - last.minY), this.depthBelow(last))) {
                           return;
                        }
                     } else if (!this.init_Z_I_i_ConfigStore(tick, forecast)) {
                        return;
                     }

                     freshHit = true;
                  }

                  this.read_Z = !forecast.unsafe();
                  ClutchModule.Inner_BufferHelper acquiredRoute = null;
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
                     ClutchModule.Inner_BufferHelper first = ownFlight && forecast.unsafe() ? this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot) : null;
                     if (this.canSteer() && !walkingEdge && first == null) {
                        forecast = this.forecastSteered(chosenAxis);
                        chosenMode = 0;
                     }

                     if (first == null && forecast.unsafe()) {
                        first = this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                     }

                     if (this.canSteer() && !walkingEdge && forecast.unsafe() && this.init_Z_j_BufferHelper(first) && chosenMode != 3) {
                        ClutchFallPhysics.Inner_ConfigStore alternateForecast = this.forecastSteered(!chosenAxis);
                        ClutchModule.Inner_BufferHelper alternate = alternateForecast.unsafe() ? this.init_j_BufferHelper_i_ConfigStore_I(alternateForecast, slot) : null;
                        if (!alternateForecast.unsafe() || this.init_Z_j_BufferHelper_j_BufferHelper(alternate, first)) {
                           chosenAxis = !chosenAxis;
                           forecast = alternateForecast;
                           first = alternate;
                        }

                        for (int mode = 1; mode <= 2 && forecast.unsafe() && this.init_Z_j_BufferHelper(first); mode++) {
                           ClutchFallPhysics.Inner_ConfigStore candidate = this.forecastSteered(chosenAxis, mode);
                           ClutchModule.Inner_BufferHelper catchRoute = candidate.unsafe() ? this.init_j_BufferHelper_i_ConfigStore_I(candidate, slot) : null;
                           if (!candidate.unsafe()
                              || this.init_Z_j_BufferHelper_j_BufferHelper(catchRoute, first)
                              || mode == 2 && this.read_BlockPos != null && first == null) {
                              forecast = candidate;
                              first = catchRoute;
                              chosenMode = mode;
                           }
                        }
                     }

                     if (this.canSteer() && !walkingEdge && forecast.unsafe() && first != null && chosenMode == 0) {
                        ClutchFallPhysics.Inner_ConfigStore diagonalForecast = this.forecastSteered(chosenAxis, 1);
                        ClutchModule.Inner_BufferHelper diagonal = diagonalForecast.unsafe() ? this.init_j_BufferHelper_i_ConfigStore_I(diagonalForecast, slot) : null;
                        if (!diagonalForecast.unsafe() || this.load_Z_j_BufferHelper_j_BufferHelper(diagonal, first)) {
                           forecast = diagonalForecast;
                           first = diagonal;
                           chosenMode = 1;
                        }
                     }

                     if (first == null && slot >= 0 && !walkingEdge && forecast.unsafe() && this.clear()) {
                        triedCrouch = true;
                        ClutchFallPhysics.Inner_ConfigStore crouched = this.forecastSteeredFrom(chosenAxis, 1, true);
                        ClutchModule.Inner_BufferHelper catchRoute = this.init_j_BufferHelper_i_ConfigStore_I(crouched, slot);
                        if (catchRoute != null) {
                           forecast = crouched;
                           first = catchRoute;
                           chosenMode = 1;
                           acquireCrouch = true;
                        }
                     }

                     if (first == null && this.canSteer() && !walkingEdge && forecast.unsafe() && this.init_BlockPos_7() != null) {
                        ClutchFallPhysics.Inner_ConfigStore rowReturn = this.forecastSteered(chosenAxis, 4);
                        ClutchModule.Inner_BufferHelper catchRoute = this.init(rowReturn, slot, null, false, Set.of(), true);
                        if (catchRoute != null) {
                           forecast = rowReturn;
                           first = catchRoute;
                           chosenMode = 4;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        ClutchModule.Inner_BufferHelper urgent = this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                        if (urgent != null) {
                           first = urgent;
                        }
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        first = this.init_j_BufferHelper_i_ConfigStore_2(forecast, chosenAxis, chosenMode, slot);
                     }

                     if (first == null && !walkingEdge && forecast.unsafe()) {
                        first = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, true);
                     }

                     if (walkingEdge && first == null) {
                        return;
                     }

                     if (forecast.unsafe() && first == null && !this.canSteer()) {
                        this.status = "Out of reach";
                        return;
                     }

                     if (forecast.unsafe() && forecast.edgeTick() > 8 && (first == null || first.init_k_JsonUtil().init_k_NetHelper().load_I() > 8)) {
                        return;
                     }

                     if (walkingEdge) {
                        this.launchCause = new ClutchFallPhysics.Inner_TaskQueue(ClutchFallPhysics.Inner_StreamUtil.FALL, true, true, tick);
                        this.write_BlockPos = this.read_BlockPos;
                        this.push_I = tick;
                     }

                     this.engage(tick, first != null);
                     if (acquireCrouch) {
                        this.queue = tick;
                     }

                     acquiredRoute = first;
                     this.flush_Z = chosenAxis;
                     this.state = chosenMode;
                     this.close_Z = walkingEdge;
                     if (first != null && !first.init_k_JsonUtil().init_Z_2() && !first.init_Z()) {
                        this.load_BlockPos = first.init_k_JsonUtil().init_BlockPos();
                        this.cache = this.load_BlockPos.getY();
                     }

                     this.read_Z = !forecast.unsafe();
                  }

                  if (MC.player.onGround() && MC.player.getDeltaMovement().y < 0.6) {
                     this.push_Z = false;
                  }

                  if (freshHit
                     && ClutchFallPhysics.isEnemyCause(this.launchCause)
                     && MC.player.getDeltaMovement().y > 0.15
                     && (MC.player.getDeltaMovement().y >= 0.6 || this.init_Z_I_9(tick))) {
                     this.push_Z = true;
                  }

                  if (this.load_Z) {
                     if (!freshHit || !forecast.unsafe()) {
                        this.disengage();
                        return;
                     }

                     this.load_Z = false;
                     this.get = 0;
                     this.tick = 0;
                  }

                  if (this.close_Z ? !forecast.unsafe() : this.next()) {
                     this.tick++;
                  } else {
                     this.tick = 0;
                  }

                  if (this.tick < 1 && tick - this.pop_I <= (MC.player.onGround() ? 80 : 160)) {
                     int slot = this.update_I_3();
                     ClutchModule.Inner_BufferHelper tower = null;
                     if (this.read_Z) {
                        this.currentPlan = null;
                        if (!this.load_Z_I_5(tick)) {
                           this.restoreSlot();
                        }

                        this.status = "Landing";
                     } else {
                        ClutchModule.Inner_BufferHelper route = this.read_Z
                           ? null
                           : (acquiredRoute != null ? acquiredRoute : this.init_j_BufferHelper_i_ConfigStore_I(forecast, slot));
                        if (!this.read_Z && this.canSteer() && !this.close_Z && this.init_Z_j_BufferHelper(route)) {
                           ClutchFallPhysics.Inner_ConfigStore alternateForecast = this.forecastSteered(!this.flush_Z);
                           ClutchModule.Inner_BufferHelper alternate = alternateForecast.unsafe()
                              ? this.init_j_BufferHelper_i_ConfigStore_I(alternateForecast, slot)
                              : null;
                           if (!alternateForecast.unsafe()) {
                              this.flush_Z = !this.flush_Z;
                              this.state = 0;
                              this.read_Z = true;
                              this.currentPlan = null;
                              this.status = "Landing";
                              return;
                           }

                           if (this.init_Z_j_BufferHelper_j_BufferHelper(alternate, route)) {
                              this.flush_Z = !this.flush_Z;
                              this.state = 0;
                              forecast = alternateForecast;
                              route = alternate;
                           }

                           for (int mode = 1; mode <= 2 && this.init_Z_j_BufferHelper(route); mode++) {
                              ClutchFallPhysics.Inner_ConfigStore candidate = this.forecastSteered(this.flush_Z, mode);
                              ClutchModule.Inner_BufferHelper catchRoute = candidate.unsafe() ? this.init_j_BufferHelper_i_ConfigStore_I(candidate, slot) : null;
                              if (!candidate.unsafe()) {
                                 this.state = mode;
                                 this.read_Z = true;
                                 this.currentPlan = null;
                                 this.restoreSlot();
                                 this.status = "Landing";
                                 return;
                              }

                              if (this.init_Z_j_BufferHelper_j_BufferHelper(catchRoute, route)) {
                                 this.state = mode;
                                 forecast = candidate;
                                 route = catchRoute;
                              }
                           }
                        }

                        if (!this.read_Z && route == null && slot >= 0 && !this.close_Z && !triedCrouch && this.clear()) {
                           ClutchFallPhysics.Inner_ConfigStore crouched = this.forecastSteeredFrom(this.flush_Z, 1, true);
                           ClutchModule.Inner_BufferHelper catchRoute = this.init_j_BufferHelper_i_ConfigStore_I(crouched, slot);
                           if (catchRoute != null) {
                              this.queue = tick;
                              this.state = 1;
                              forecast = crouched;
                              route = catchRoute;
                           }
                        }

                        if (!this.read_Z && route == null && this.canSteer() && !this.close_Z && this.init_BlockPos_7() != null) {
                           ClutchFallPhysics.Inner_ConfigStore rowReturn = this.forecastSteered(this.flush_Z, 4);
                           ClutchModule.Inner_BufferHelper catchRoute = this.init(rowReturn, slot, null, false, Set.of(), true);
                           if (catchRoute != null) {
                              forecast = rowReturn;
                              route = catchRoute;
                              this.state = 4;
                           }
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot);
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.init_j_BufferHelper_i_ConfigStore_2(forecast, this.flush_Z, this.state, slot);
                        }

                        if (!this.read_Z && route == null && !this.close_Z) {
                           route = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, true);
                        }

                        if (!this.read_Z && !this.close_Z) {
                           int reserve = route != null && !route.init_k_JsonUtil().init_Z_2() ? route.init_k_JsonUtil().init_List().size() : 0;
                           tower = this.init_j_BufferHelper_i_ConfigStore(forecast, slot, reserve);
                        }

                        if (tower != null && (route == null || route.init_k_JsonUtil().init_k_NetHelper().load_I() >= 3)) {
                           route = tower;
                        }

                        this.write_Z = DangerModule.init_Z_52() && (route == null || route.init_Z() || route.init_k_JsonUtil().init_k_NetHelper().load_I() >= 4);
                        if (this.write_Z) {
                           this.currentPlan = null;
                           this.status = "Danger returning";
                        } else if (route == null) {
                           this.currentPlan = null;
                           this.status = slot < 0 ? "Braking without blocks" : "No reachable catch";
                           if (!this.load_Z_I_5(tick)) {
                              this.restoreSlot();
                           }

                           if (this.close_Z) {
                              this.disengage();
                           } else {
                              if (!this.valid() && !ClutchFallPhysics.isWithinTickWindow(tick, this.task, 3) && !this.load_Z_I_5(tick)) {
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
                           this.task = tick;
                           this.nextEyePosition = forecast.eyePositions().size() > 1 ? forecast.eyePositions().get(1) : null;
                           boolean reacquiringHand = this.slotsReleased;
                           if (this.slotsReleased) {
                              this.slotsReleased = false;
                              this.next = this.write_I_7();
                              this.valid = -1;
                              autismclient.util.ClientInteractionOverride.clearAll();
                              autismclient.util.SilentRotationManager.syncFromServerRotation();
                           }

                           this.prev = slot;
                           boolean hand = autismclient.util.InventorySlotArbiter.claimSlot(this.id(), this.prev);
                           if (hand && (MC.player.getInventory().getSelectedSlot() != this.prev || reacquiringHand)) {
                              autismclient.util.VirtualInputDispatcher.setSlot(this.prev);
                           }

                           if (!route.init_k_JsonUtil().init_Z_2() && !route.init_Z()) {
                              this.load_BlockPos = route.init_k_JsonUtil().init_BlockPos();
                              this.cache = this.load_BlockPos.getY();
                           }

                           ClutchBlockPlanner.Inner_NetHelper link = route.init_k_JsonUtil().init_k_NetHelper();
                           this.set = this.placementTick(link);
                           if (hand
                              && this.currentPlan != null
                              && this.currentPlan.load_I == this.prev
                              && !this.recentPlacementCells.containsKey(this.currentPlan.pos)
                              && (!route.init_Z() || route.init_k_JsonUtil().init_k_NetHelper().init_BlockPos().equals(this.currentPlan.pos))) {
                              for (int i = 0; i < route.init_k_JsonUtil().init_List().size(); i++) {
                                 ClutchBlockPlanner.Inner_NetHelper preparedLink = route.init_k_JsonUtil().init_List().get(i);
                                 if (preparedLink.init_BlockPos().equals(this.currentPlan.pos)) {
                                    Direction nextFace = i + 1 < route.init_k_JsonUtil().init_List().size()
                                          && route.init_k_JsonUtil().init_List().get(i + 1).load_BlockPos().equals(this.currentPlan.pos)
                                       ? route.init_k_JsonUtil().init_List().get(i + 1).init_Direction()
                                       : null;
                                    this.currentPlan = new ClutchModule.Inner_DataUtil(
                                       this.currentPlan.pos,
                                       this.currentPlan.placement,
                                       this.currentPlan.init_I,
                                       this.currentPlan.load_I,
                                       this.currentPlan.init_Z,
                                       this.currentPlan.load_Z,
                                       nextFace,
                                       route.init_k_JsonUtil().read()
                                    );
                                    break;
                                 }
                              }

                              if (this.start()) {
                                 return;
                              }
                           }

                           ClutchModule.Inner_DataUtil next = this.init_j_DataUtil_j_BufferHelper(route, link, this.prev);
                           if (next == null && !route.init_Z() && !this.close_Z) {
                              ClutchModule.Inner_BufferHelper alternate = this.init_j_BufferHelper_i_ConfigStore(
                                 forecast, this.prev, null, route.init_k_JsonUtil().load(), Set.of(link.init_BlockPos())
                              );
                              if (alternate != null) {
                                 ClutchModule.Inner_DataUtil candidate = this.init_j_DataUtil_j_BufferHelper(
                                    alternate, alternate.init_k_JsonUtil().init_k_NetHelper(), this.prev
                                 );
                                 if (candidate != null) {
                                    route = alternate;
                                    link = route.init_k_JsonUtil().init_k_NetHelper();
                                    next = candidate;
                                    this.set = this.placementTick(link);
                                    if (!route.init_k_JsonUtil().init_Z_2()) {
                                       this.load_BlockPos = route.init_k_JsonUtil().init_BlockPos();
                                       this.cache = this.load_BlockPos.getY();
                                    }
                                 }
                              }
                           }

                           if (next == null) {
                              this.currentPlan = null;
                              this.status = "Turning to the catch";
                              autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, route.init_ce_DataUtil_k_NetHelper_I(link, this.earliestPlacementTick(link)));
                              this.read_V(this.set);
                           } else {
                              this.currentPlan = next;
                              this.status = !hand
                                 ? "Hand busy"
                                 : (
                                    this.close_Z
                                       ? "Securing the edge"
                                       : (
                                          route.init_Z()
                                             ? "Building under the lift"
                                             : (route.init_k_JsonUtil().load() ? "Reaching toward the fall" : "Catching knockback")
                                       )
                                 );
                              if (this.wireState != null
                                 && this.wireState.tick < this.clientTick()
                                 && MC.player.getInventory().getSelectedSlot() == this.currentPlan.load_I) {
                                 Vec3 previousEye = this.wireState.position.add(0.0, this.wireState.eyeHeight, 0.0);
                                 double reach = MC.player.blockInteractionRange() - 0.03;
                                 if (hand && link.init_I() == 0) {
                                    ClutchModule.Inner_DataUtil current = this.earliestPlacementTick(link) == 0
                                       ? this.currentPlan
                                       : this.init_j_DataUtil_j_BufferHelper(
                                          route, new ClutchBlockPlanner.Inner_NetHelper(link.init_BlockPos(), link.load_BlockPos(), link.init_Direction(), 0, 0), this.prev
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
                                       this.set = 1;
                                    }
                                 }

                                 if (!this.currentPlan.read
                                    && ScaffoldModule.init_Z_Vec3(
                                       previousEye,
                                       this.wireState.rotation,
                                       this.currentPlan.placement.support().supportPos(),
                                       reach
                                    )) {
                                    autismclient.util.BlockPlacementPlanner.PlacementPlan carry = this.init_U_CachePool_j_DataUtil(
                                       this.currentPlan, this.currentPlan.placement.aim().goalRotation(), previousEye, reach
                                    );
                                    if (carry != null) {
                                       this.currentPlan = new ClutchModule.Inner_DataUtil(
                                          this.currentPlan.pos,
                                          carry,
                                          this.currentPlan.init_I,
                                          this.currentPlan.load_I,
                                          this.currentPlan.init_Z,
                                          this.currentPlan.load_Z,
                                          this.currentPlan.face,
                                          this.currentPlan.read
                                       );
                                    }
                                 }
                              }

                              autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, this.currentPlan.placement.aim().goalRotation());
                              this.read_V(this.set);
                              if (hand) {
                                 this.start();
                              }
                           }
                        }
                     }
                  } else {
                     int caughtRow = this.write_BlockPos == null ? this.cache : Math.min(this.cache, this.write_BlockPos.getY());
                     if (this.tick >= 1 && MC.player.getY() >= caughtRow + 1 - this.safeFallDistance()) {
                        this.flags++;
                        this.add = tick;
                        this.sync_Z = true;
                     }

                     if (this.tick >= 1 && !this.load_Z_I_5(tick)) {
                        this.restoreSlot();
                     }

                     this.launchCause = null;
                     this.poll_I = -2147483648;
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

   private void read_V(int ticksLeft) {
      if ((this.state == 4 || this.currentPlan != null && this.currentPlan.read) && ticksLeft <= 1) {
         autismclient.util.SilentRotationManager.stepUrgentFixed(this.id(), MC.player, 180.0F);
      } else {
         autismclient.util.SilentRotationManager.stepUrgentTimed(this.id(), MC.player, ticksLeft);
      }
   }

   private void engage(int tick, boolean placement) {
      this.engaged = true;
      this.slotsReleased = !placement;
      this.load_Z = false;
      this.get = 0;
      this.pop_I = tick;
      this.tick = 0;
      this.state = 0;
      this.close_Z = false;
      this.push_Z = false;
      this.sync_Z = false;
      this.hitAxisZ = null;
      this.flush_Z = Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
      this.cache = this.write_BlockPos != null ? this.write_BlockPos.getY() : (int)Math.floor(MC.player.getY()) - 1;
      this.init_BlockPos = null;
      this.load_BlockPos = null;
      this.task = tick;
      this.next = placement ? this.write_I_7() : -1;
      if (placement) {
         this.valid = -1;
         autismclient.util.SilentRotationManager.syncFromServerRotation();
         autismclient.util.ClientInteractionOverride.clearAll();
         autismclient.util.VirtualInputDispatcher.update_V();
         if (MC.player.isUsingItem()) {
            MC.gameMode.releaseUsingItem(MC.player);
         }
      }

      this.status = "Catching knockback";
   }

   private int write_I_7() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.valid >= 0 && selected == this.size ? this.valid : selected;
   }

   private boolean load_Z_I_5(int tick) {
      return this.run == tick || this.sentPlacementHit != null && ClutchFallPhysics.isWithinTickWindow(tick, this.chunk, 1);
   }

   private void write(int tick) {
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
                  this.read_BlockPos = pos;
                  this.value = tick;
               }
            }
         }
      }
   }

   private boolean read_Z(int tick) {
      if (!MC.player.onGround() && this.read_BlockPos != null && this.ownSteerWindow.isActive(tick)) {
         boolean underLaunchRow = MC.player.getDeltaMovement().y < 0.0 && MC.player.getY() < this.read_BlockPos.getY();
         if (this.pop_Z && !underLaunchRow) {
            return false;
         } else if (!this.inGuardTime()) {
            return !this.bool("own-knockback")
               ? false
               : ClutchFallPhysics.shouldDeployRescue(tick, this.value, MC.player.getY(), this.read_BlockPos.getY(), MC.player.getDeltaMovement().y, this.pop_Z, put());
         } else {
            return !put() || MC.player.getY() < this.read_BlockPos.getY() + 0.4;
         }
      } else {
         return false;
      }
   }

   private boolean init_Z_I_i_ConfigStore(int tick, ClutchFallPhysics.Inner_ConfigStore forecast) {
      if (!this.read_Z(tick) || !forecast.unsafe() || forecast.steps().isEmpty()) {
         return false;
      }

      if (this.inGuardTime()) {
         this.launchCause = new ClutchFallPhysics.Inner_TaskQueue(ClutchFallPhysics.Inner_StreamUtil.GUARD, false, true, tick);
         this.write_BlockPos = this.read_BlockPos;
         this.push_I = tick;
         this.poll_I = -2147483648;
         return true;
      }

      AABB last = forecast.steps().getLast().body();
      if (!this.isDangerousDrop(this.read_BlockPos.getY() + 1 - last.minY, this.depthBelow(last))) {
         return false;
      }

      this.launchCause = new ClutchFallPhysics.Inner_TaskQueue(ClutchFallPhysics.Inner_StreamUtil.FALL, true, true, tick);
      this.write_BlockPos = this.read_BlockPos;
      this.push_I = tick;
      this.poll_I = -2147483648;
      return true;
   }

   private boolean isDangerousDrop(double fallen, double below) {
      return ClutchFallPhysics.isDangerousDrop(fallen, below, this.bool("own-void-only"), this.integer("own-fall-height"));
   }

   private boolean load_Z_D_D_2(double fallen, double below) {
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

   private ClutchFallPhysics.Inner_BufferHelper mediumAt(AABB body) {
      AABB box = body.deflate(0.001);
      MutableBlockPos pos = new MutableBlockPos();

      for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
         for (int y = Mth.floor(box.minY); y <= Mth.floor(box.maxY); y++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               pos.set(x, y, z);
               BlockState state = MC.level.getBlockState(pos);
               FluidState fluid = state.getFluidState();
               if (fluid.is(FluidTags.LAVA)) {
                  return ClutchFallPhysics.Inner_BufferHelper.HAZARD;
               }

               if (fluid.is(FluidTags.WATER) && y + fluid.getHeight(MC.level, pos) > box.minY) {
                  return ClutchFallPhysics.Inner_BufferHelper.CUSHION;
               }

               if (state.is(Blocks.COBWEB) || state.is(Blocks.POWDER_SNOW)) {
                  return ClutchFallPhysics.Inner_BufferHelper.CUSHION;
               }
            }
         }
      }

      Vec3 centre = body.getCenter();
      pos.set(Mth.floor(centre.x), Mth.floor(body.minY - 0.05), Mth.floor(centre.z));
      return MC.level.getBlockState(pos).is(Blocks.SLIME_BLOCK) ? ClutchFallPhysics.Inner_BufferHelper.CUSHION : ClutchFallPhysics.Inner_BufferHelper.NONE;
   }

   private double safeFallDistance() {
      double health = MC.player.getHealth() + MC.player.getAbsorptionAmount();
      return Math.max(1.0, Math.min(this.integer("safe-fall"), 2.0 + health));
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastFall() {
      if (this.close_Z) {
         return this.forecastWalking();
      }

      ClutchFallPhysics.Inner_CachePool collision = (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of());
      ClutchFallPhysics.Inner_JsonUtil medium = this::mediumAt;
      if (this.engaged && !this.load_Z && this.canSteer() && this.state != 3) {
         return this.forecastSteered(this.flush_Z, this.state);
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

   private ClutchFallPhysics.Inner_ConfigStore forecastSteered(boolean axis) {
      return this.forecastSteered(axis, 0);
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastSteered(boolean axis, int mode) {
      return this.forecastSteered(axis, mode, List.of());
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastSteered(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (this.canSteer() && !this.load_Z && mode != 3) {
         return this.load(axis, mode, extra, placementTick);
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

   private ClutchFallPhysics.Inner_ConfigStore forecastSteered(boolean axis, int mode, List<VoxelShape> extra) {
      return this.load(axis, mode, extra, 0);
   }

   private ClutchFallPhysics.Inner_ConfigStore load(boolean axis, int mode, List<VoxelShape> extra, int placementTick) {
      if (mode == 3) {
         return this.forecastSteered(axis, mode, extra, placementTick);
      }

      if (MC.player.onGround()
         || !this.add()
            && (!(Math.abs(MC.player.getBoundingBox().getXsize() - 0.6) < 0.001) || !(Math.abs(MC.player.getBoundingBox().getYsize() - 1.5) < 0.001))) {
         Vec3 initialMotion = MC.player.getDeltaMovement();
         if (this.canSteer() && this.init_Z_Z_I_2(this.bestBlockSlot() >= 0, mode)) {
            float jump = ((AutismLivingEntityAccessor)MC.player).autism$getJumpPower();
            initialMotion = new Vec3(initialMotion.x, Math.max(initialMotion.y, jump), initialMotion.z);
         }

         return ClutchFallPhysics.simulateBoxFall(
            MC.player.getBoundingBox(),
            initialMotion,
            this.safeFallDistance(),
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
            (body, motion, supported) -> this.init_Vec3_AABB(body, motion, axis, mode, supported).scale(supported ? 0.1 : 0.02),
            this::mediumAt,
            (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, extra),
            placementTick,
            MC.player.fallDistance
         );
      } else {
         return this.forecastSteeredFrom(axis, mode, false, extra, placementTick);
      }
   }

   private Vec3 init_Vec3_AABB(AABB body, Vec3 motion, boolean axis, int mode, boolean supported) {
      if (supported || this.load_Z) {
         return ClutchFallPhysics.brakingDirection(motion);
      }

      if (mode == 4 && this.sync_BlockPos != null) {
         Vec3 centre = body.getCenter();
         double error = axis ? this.sync_BlockPos.getZ() + 0.5 - centre.z - motion.z * 5.0 : this.sync_BlockPos.getX() + 0.5 - centre.x - motion.x * 5.0;
         return axis ? new Vec3(0.0, 0.0, Math.signum(error)) : new Vec3(Math.signum(error), 0.0, 0.0);
      }

      if (mode == 2 && this.read_BlockPos != null && !MC.level.getBlockState(this.read_BlockPos).getCollisionShape(MC.level, this.read_BlockPos).isEmpty()) {
         Vec3 home = Vec3.atCenterOf(this.read_BlockPos);
         Vec3 centre = body.getCenter();
         Vec3 wanted = new Vec3(home.x - centre.x - motion.x * 5.0, 0.0, home.z - centre.z - motion.z * 5.0);
         if (wanted.horizontalDistanceSqr() > 0.0025) {
            return wanted.normalize();
         }
      }

      return mode == 0 ? ClutchFallPhysics.brakingAxisDirection(motion, axis) : ClutchFallPhysics.brakingDirection(motion);
   }

   private BlockPos init_BlockPos_7() {
      if (this.read_BlockPos != null && !MC.level.getBlockState(this.read_BlockPos).getCollisionShape(MC.level, this.read_BlockPos).isEmpty()) {
         return this.sync_BlockPos = this.read_BlockPos;
      }

      if (this.sync_BlockPos != null && !MC.level.getBlockState(this.sync_BlockPos).getCollisionShape(MC.level, this.sync_BlockPos).isEmpty()) {
         return this.sync_BlockPos;
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

      return this.sync_BlockPos = found;
   }

   private boolean add() {
      return ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.queue, 2) && !MC.player.onGround();
   }

   private boolean clear() {
      AABB body = MC.player.getBoundingBox();
      return this.canSteer()
         && !MC.player.onGround()
         && !this.add()
         && !MC.options.keyShift.isDown()
         && Math.abs(body.getXsize() - 0.6) < 0.001
         && Math.abs(body.getYsize() - 1.8) < 0.001
         && Math.abs(MC.player.getEyeHeight() - 1.62) < 0.001;
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastSteeredFrom(boolean axis, int mode, boolean start) {
      return this.forecastSteeredFrom(axis, mode, start, List.of(), 0);
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastSteeredFrom(boolean axis, int mode, boolean start, List<VoxelShape> extra, int placementTick) {
      return ClutchFallPhysics.simulateMotionFall(
         autismclient.ai.engine.AiPhysics.MotionState.of(MC.player),
         MC.options.keyShift.isDown() ? 64 : (!start && this.clientTick() != this.queue ? 0 : 1),
         MC.player.getYRot(),
         this.safeFallDistance(),
         autismclient.ai.engine.AiPhysics.createLiveWorld(MC.level, MC.player),
         (body, motion, supported) -> this.init_Vec3_AABB(body, motion, axis, mode, supported),
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

   private boolean reset() {
      return this.ownSteerWindow.isActive(this.clientTick())
         && this.bool("own-knockback")
         && this.bool("own-steer")
         && this.put > 0
         && MC.player.onGround()
         && this.read_BlockPos != null
         && !put()
         && !this.pop_Z
         && inputVector(this.keyboardInput(), MC.player.getYRot()).horizontalDistanceSqr() > 0.0;
   }

   private ClutchFallPhysics.Inner_ConfigStore forecastWalking() {
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

   private int update_I_3() {
      int selected = MC.player.getInventory().getSelectedSlot();
      return this.prev >= 0
            && this.prev == selected
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

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_I(ClutchFallPhysics.Inner_ConfigStore forecast, int slot) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, null, false);
   }

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_2(ClutchFallPhysics.Inner_ConfigStore forecast, int slot, BlockPos assumed) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, assumed, false);
   }

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(ClutchFallPhysics.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort) {
      return this.init_j_BufferHelper_i_ConfigStore(forecast, slot, assumed, bestEffort, Set.of());
   }

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(
      ClutchFallPhysics.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts
   ) {
      ClutchModule.Inner_BufferHelper prepared = this.init(forecast, slot, assumed, bestEffort, rejectedStarts, this.state == 4);
      return prepared == null && !bestEffort && assumed == null
         ? (this.currentPlan != null && this.currentPlan.read ? this.load_j_BufferHelper_i_ConfigStore_I(forecast, slot) : null)
         : prepared;
   }

   private ClutchModule.Inner_BufferHelper init(
      ClutchFallPhysics.Inner_ConfigStore forecast, int slot, BlockPos assumed, boolean bestEffort, Set<BlockPos> rejectedStarts, boolean marginal
   ) {
      if (!forecast.steps().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.Inner_UiHelper step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         RotationMath.Rotation from = this.currentLook();
         ToDoubleFunction<ClutchBlockPlanner.Inner_NetHelper> turnCost = link -> {
            Vec3 eye = forecast.eyePositions().isEmpty()
               ? ClutchModule.Inner_BufferHelper.init(bodies, eyeHeight, this.earliestPlacementTick(link))
               : forecast.eyePositions().get(Mth.clamp(this.earliestPlacementTick(link), 0, forecast.eyePositions().size() - 1));
            float angle = RotationMath.vectorAngleDegrees(from, ClutchModule.Inner_BufferHelper.init_ce_DataUtil_Vec3_k_NetHelper(eye, link));
            return Math.max(0.0F, angle / this.placementTick(link) - 25.0F) * 0.04;
         };
         ClutchBlockPlanner.Inner_JsonUtil plan = null;
         boolean own = ClutchFallPhysics.isSelfOrGuardCause(this.launchCause);

         for (int pass = own ? 0 : 2; pass < 3 && plan == null; pass++) {
            ClutchBlockPlanner.Inner_DataUtil world = this.placementWorld(assumed, range, rejectedStarts, pass < 2 ? this.flush_I_2() : 2147483647);
            plan = ClutchBlockPlanner.init(
               bodies,
               eyeHeight,
               range - 0.05,
               world,
               assumed == null ? 0 : 1,
               MC.player.fallDistance,
               this.effectiveHealth(),
               this.load_BlockPos,
               this.flush_I_2(),
               turnCost,
               Math.max(0, ScaffoldModule.load_I_I(slot) - (assumed == null ? 0 : 1)),
               this.fallDamageCurve(),
               bestEffort ? range : 0.0,
               this.init_D_i_ConfigStore(forecast),
               forecast.eyePositions(),
               marginal || pass == 1
            );
         }

         return plan == null ? null : new ClutchModule.Inner_BufferHelper(plan, bodies, eyeHeight, false, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.Inner_BufferHelper load_j_BufferHelper_i_ConfigStore_I(ClutchFallPhysics.Inner_ConfigStore forecast, int slot) {
      if (slot >= 0 && !forecast.steps().isEmpty()) {
         List<AABB> bodies = new ArrayList<>();
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.Inner_UiHelper step : forecast.steps()) {
            bodies.add(step.body());
         }

         double reach = MC.player.blockInteractionRange() - 0.03;
         ClutchBlockPlanner.Inner_JsonUtil urgent = ClutchBlockPlanner.init(
            bodies,
            MC.player.getEyeHeight(),
            reach - 0.05,
            this.placementWorld(null, reach),
            0,
            MC.player.fallDistance,
            this.effectiveHealth(),
            this.load_BlockPos,
            this.flush_I_2(),
            null,
            ScaffoldModule.load_I_I(slot),
            this.fallDamageCurve(),
            this.init_D_i_ConfigStore(forecast),
            forecast.eyePositions(),
            true
         );
         return urgent == null ? null : new ClutchModule.Inner_BufferHelper(urgent, bodies, MC.player.getEyeHeight(), false, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore(ClutchFallPhysics.Inner_ConfigStore forecast, int slot, int reserve) {
      if (forecast.unsafe() && this.push_Z && ClutchFallPhysics.isEnemyCause(this.launchCause) && slot >= 0 && !forecast.steps().isEmpty() && ScaffoldModule.load_I_I(slot) > reserve) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.Inner_UiHelper step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;
         ClutchBlockPlanner.Inner_JsonUtil plan = ClutchBlockPlanner.load(
            bodies, eyeHeight, range - 0.05, this.placementWorld(null, range, Set.of(), 2147483647, this.read_Z), 0, forecast.eyePositions()
         );
         return plan == null ? null : new ClutchModule.Inner_BufferHelper(plan, bodies, eyeHeight, true, forecast.eyePositions());
      } else {
         return null;
      }
   }

   private ClutchModule.Inner_BufferHelper init_j_BufferHelper_i_ConfigStore_2(ClutchFallPhysics.Inner_ConfigStore forecast, boolean axis, int mode, int slot) {
      if (!forecast.steps().isEmpty() && slot >= 0) {
         List<AABB> bodies = new ArrayList<>(forecast.steps().size() + 1);
         bodies.add(MC.player.getBoundingBox());

         for (ClutchFallPhysics.Inner_UiHelper step : forecast.steps()) {
            bodies.add(step.body());
         }

         double eyeHeight = MC.player.getEyeHeight();
         double range = MC.player.blockInteractionRange() - 0.03;

         for (ClutchBlockPlanner.Inner_NetHelper link : ClutchBlockPlanner.init_List_List(bodies, eyeHeight, range - 0.05, this.placementWorld(null, range), 0, 8, forecast.eyePositions())) {
            if (!this.forecastSteered(axis, mode, List.of(Shapes.create(new AABB(link.init_BlockPos()))), link.init_I()).unsafe()) {
               ClutchBlockPlanner.Inner_JsonUtil plan = new ClutchBlockPlanner.Inner_JsonUtil(List.of(link), link.init_BlockPos(), link.init_I() + 1, MC.player.fallDistance, false);
               return new ClutchModule.Inner_BufferHelper(plan, bodies, eyeHeight, false, forecast.eyePositions());
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private ClutchBlockPlanner.Inner_DataUtil placementWorld(BlockPos assumed, double range) {
      return this.placementWorld(assumed, range, Set.of());
   }

   private ClutchBlockPlanner.Inner_DataUtil placementWorld(BlockPos assumed, double range, Set<BlockPos> rejectedStarts) {
      return this.placementWorld(assumed, range, rejectedStarts, 2147483647);
   }

   private ClutchBlockPlanner.Inner_DataUtil placementWorld(BlockPos assumed, double range, Set<BlockPos> rejectedStarts, int maxRow) {
      return this.placementWorld(assumed, range, rejectedStarts, maxRow, false);
   }

   private ClutchBlockPlanner.Inner_DataUtil placementWorld(
      final BlockPos assumed, double range, final Set<BlockPos> rejectedStarts, final int maxRow, boolean optional
   ) {
      final boolean sneaking = MC.player.isSecondaryUseActive();
      final BlockHitResult writtenRay = this.worldCache.init(range);
      final List<AABB> others = this.worldCache.init(optional);
      return new ClutchBlockPlanner.Inner_DataUtil() {
         private boolean update(int x, int y, int z) {
            return assumed != null && assumed.getX() == x && assumed.getY() == y && assumed.getZ() == z;
         }

         @Override
         public boolean init(int x, int y, int z) {
            return rejectedStarts.isEmpty() || !rejectedStarts.contains(new BlockPos(x, y, z));
         }

         @Override
         public boolean load(int x, int y, int z) {
            return this.update(x, y, z) || ClutchModule.this.worldCache.init(x, y, z, sneaking);
         }

         @Override
         public boolean read(int x, int y, int z) {
            if (y <= maxRow && !this.update(x, y, z) && ClutchModule.this.worldCache.init(x, y, z)) {
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
               : ClutchModule.this.worldCache.init_Z_Vec3_D(eye, distance);
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

   private double init_D_i_ConfigStore(ClutchFallPhysics.Inner_ConfigStore forecast) {
      List<ClutchFallPhysics.Inner_UiHelper> steps = forecast.steps();
      if (!steps.isEmpty() && steps.getLast().onGround()) {
         double highest = MC.player.getBoundingBox().minY;

         for (ClutchFallPhysics.Inner_UiHelper step : steps) {
            highest = Math.max(highest, step.body().minY);
         }

         return this.fallDamageCurve().applyAsDouble(MC.player.fallDistance + Math.max(0.0, highest - steps.getLast().body().minY));
      } else {
         return 1.0 / 0.0;
      }
   }

   private boolean init_Z_j_BufferHelper(ClutchModule.Inner_BufferHelper route) {
      return this.init_D_j_BufferHelper(route) >= 4.0;
   }

   private boolean init_Z_j_BufferHelper_j_BufferHelper(ClutchModule.Inner_BufferHelper candidate, ClutchModule.Inner_BufferHelper current) {
      return candidate != null && this.init_D_j_BufferHelper(candidate) <= this.init_D_j_BufferHelper(current) - 4.0;
   }

   private boolean load_Z_j_BufferHelper_j_BufferHelper(ClutchModule.Inner_BufferHelper candidate, ClutchModule.Inner_BufferHelper current) {
      if (candidate != null
         && !candidate.init_k_JsonUtil().load()
         && !current.init_Z()
         && !(this.init_D_j_BufferHelper(candidate) > this.init_D_j_BufferHelper(current))) {
         int links = candidate.init_k_JsonUtil().init_List().size();
         int previous = current.init_k_JsonUtil().init_List().size();
         return links < previous || links == previous && candidate.init_k_JsonUtil().init_I() < current.init_k_JsonUtil().init_I();
      } else {
         return false;
      }
   }

   private double init_D_j_BufferHelper(ClutchModule.Inner_BufferHelper route) {
      return route != null && !route.init_k_JsonUtil().load() ? this.fallDamageCurve().applyAsDouble(route.init_k_JsonUtil().init_D()) : 1.0 / 0.0;
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

   private int flush_I_2() {
      if (this.write_BlockPos != null) {
         return this.write_BlockPos.getY();
      } else {
         return this.read_BlockPos != null && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.value, 40)
            ? this.read_BlockPos.getY()
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

   private int placementTick(ClutchBlockPlanner.Inner_NetHelper link) {
      return ClutchFallPhysics.choosePlacementTick(link.init_I(), link.load_I(), ClutchFallPhysics.isEnemyCause(this.launchCause));
   }

   private int earliestPlacementTick(ClutchBlockPlanner.Inner_NetHelper link) {
      return ClutchFallPhysics.clampEarliestTick(link.init_I(), link.load_I());
   }

   private ClutchModule.Inner_DataUtil init_j_DataUtil_j_BufferHelper(ClutchModule.Inner_BufferHelper route, ClutchBlockPlanner.Inner_NetHelper link, int slot) {
      ItemStack stack = MC.player.getInventory().getItem(slot);
      int at = route.init_k_JsonUtil().read() ? link.init_I() : this.earliestPlacementTick(link);
      double reach = MC.player.blockInteractionRange() - 0.03;
      autismclient.util.BlockPlacementPlanner.HitValidator placement = autismclient.util.BlockPlacementPlanner.blockItemValidator(stack, MC.player, InteractionHand.MAIN_HAND);
      RotationMath.Rotation from = this.currentLook();
      autismclient.util.BlockPlacementPlanner.RayBudget budget = new autismclient.util.BlockPlacementPlanner.RayBudget(24);
      boolean sneaking = MC.player.isSecondaryUseActive();
      Vec3 eye = this.close_Z ? MC.player.getEyePosition() : route.init(at);
      Vec3 leadEye = !this.close_Z && !route.init_k_JsonUtil().init_Z_2() && !route.init_k_JsonUtil().read() ? route.init(at + 1) : eye;
      autismclient.util.BlockPlacementPlanner.PlacementPlan candidate = autismclient.util.BlockPlacementPlanner.plan(
         new autismclient.util.BlockPlacementPlanner.Query(link.init_BlockPos(), eye, reach, placement)
            .withFromRotation(from)
            .withLeadEye(leadEye)
            .withAllowedFaces(EnumSet.of(link.init_Direction()))
            .withAllowInPlace(false)
            .withMaxPitch(89.5)
            .withSneaking(sneaking)
            .withAutoSneak(false)
            .withMarginCap(!route.init_k_JsonUtil().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
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
               .withMarginCap(!route.init_k_JsonUtil().load() && route.load().isEmpty() ? 1.0 / 0.0 : 0.02)
               .withRayBudget(budget)
         );
      }

      Direction nextFace = route.init_k_JsonUtil().init_List().size() > 1
            && route.init_k_JsonUtil().init_List().get(1).load_BlockPos().equals(link.init_BlockPos())
         ? route.init_k_JsonUtil().init_List().get(1).init_Direction()
         : null;
      if (candidate == null) {
         return null;
      }

      ClutchModule.Inner_DataUtil prepared = new ClutchModule.Inner_DataUtil(
         link.init_BlockPos(), candidate, link.load_I(), slot, route.init_k_JsonUtil().init_Z_2(), route.init_Z(), nextFace, route.init_k_JsonUtil().read()
      );
      if (nextFace != null && !prepared.read) {
         autismclient.util.BlockPlacementPlanner.PlacementPlan carried = this.init(
            prepared, candidate.aim().goalRotation(), eye, route.init(at + 1), null, reach, at
         );
         if (carried != null) {
            prepared = new ClutchModule.Inner_DataUtil(
               prepared.pos, carried, prepared.init_I, prepared.load_I, prepared.init_Z, prepared.load_Z, nextFace, prepared.read
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

   private boolean start() {
      if (this.currentPlan != null
         && this.run != this.clientTick()
         && MC.player.getInventory().getSelectedSlot() == this.currentPlan.load_I
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

         List<AABB> otherBodies = this.otherBodies(this.currentPlan.load_Z && this.read_Z);
         if (intersectsBodies(this.currentPlan.pos, otherBodies)) {
            return this.fail("Moving body enters placement");
         }

         autismclient.util.BlockPlacementPlanner.PlacementPlan carry = this.currentPlan.read
            ? null
            : this.init_U_CachePool_j_DataUtil(this.currentPlan, rotation, wireEye, reach);
         if (carry != null) {
            this.currentPlan = new ClutchModule.Inner_DataUtil(
               this.currentPlan.pos,
               carry,
               this.currentPlan.init_I,
               this.currentPlan.load_I,
               this.currentPlan.init_Z,
               this.currentPlan.load_Z,
               this.currentPlan.face,
               this.currentPlan.read
            );
            autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, carry.aim().goalRotation());
            this.read_V(1);
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
                     && !this.currentPlan.init_Z
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
                  this.frame = this.currentPlan.load_I;
                  this.block = -1;
                  this.chunk = this.clientTick();
                  this.run = this.clientTick();
                  this.level++;
                  this.recentPlacementCells.put(placedCell, this.clientTick());
                  this.init_BlockPos = placedCell;
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

   private autismclient.util.BlockPlacementPlanner.PlacementPlan init_U_CachePool_j_DataUtil(
      ClutchModule.Inner_DataUtil candidatePlan, RotationMath.Rotation rotation, Vec3 wireEye, double reach
   ) {
      Vec3 eye = MC.player.getEyePosition();
      Vec3 next = this.nextEyePosition != null ? this.nextEyePosition : eye.add(MC.player.getDeltaMovement());
      return this.init(candidatePlan, rotation, eye, next, wireEye, reach, 0);
   }

   private ClutchFallPhysics.Inner_DataUtil predictAirSweep(int lead, float yaw) {
      return ClutchFallPhysics.predictAirSweep(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.getEyeHeight(),
         MC.player.getYRot(),
         yaw,
         lead,
         (body, motion, supported) -> this.init_Vec3_AABB(body, motion, this.flush_Z, 4, supported),
         AutismEntityAccessor::autism$getInputVector
      );
   }

   private autismclient.util.BlockPlacementPlanner.PlacementPlan init(
      ClutchModule.Inner_DataUtil candidatePlan, RotationMath.Rotation rotation, Vec3 eye, Vec3 next, Vec3 wireEye, double reach, int lead
   ) {
      boolean moving = this.state == 4
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
      double margin = this.state == 4 ? 1.0E-5 : 0.02;
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

      if (this.state == 4) {
         List<ClutchBlockPlanner.Inner_StreamUtil> counts = moving && candidatePlan.face != null && candidatePlan.face.getAxis() != Axis.Y
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
         ClutchFallPhysics.Inner_DataUtil window = moving ? this.predictAirSweep(lead, goal.yaw()) : null;
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

   private boolean stop() {
      return this.hitAxisZ != null ? this.hitAxisZ : Math.abs(MC.player.getDeltaMovement().x) > Math.abs(MC.player.getDeltaMovement().z);
   }

   private boolean next() {
      return ClutchFallPhysics.isGroundedSafe(
         MC.player.getBoundingBox(),
         MC.player.getDeltaMovement(),
         MC.player.onGround(),
         this.safeFallDistance(),
         (body, motion) -> Entity.collideBoundingBox(MC.player, motion, body, MC.level, List.of()),
         this::mediumAt
      );
   }

   private boolean prev() {
      return this.add != -2147483648 && ClutchFallPhysics.isWithinTickWindow(this.clientTick(), this.add, 10);
   }

   private boolean shouldSteerInput(Input input) {
      if ("Holding unsafe jump".equals(this.status)) {
         this.status = "Idle";
      }

      if (this.pop_Z_3() && MC.player.onGround() && (!input.shift() || input.jump()) && !put() && this.set()) {
         Vec3 wish = inputVector(input, SilentMovementCoordinator.getMovementYaw(MC.player));
         Vec3 motion = MC.player.getDeltaMovement();
         AABB body = MC.player.getBoundingBox();
         Vec3 centre = body.getCenter();
         AABB column = new AABB(centre.x - 0.05, body.minY - 0.6, centre.z - 0.05, centre.x + 0.05, body.minY - 0.01, centre.z + 0.05);
         boolean hanging = MC.level.noCollision(MC.player, column) && this.init_Z_D_5(this.depthBelow(column.move(0.0, body.minY - column.minY, 0.0)));
         if (hanging) {
            this.put = Math.max(this.put, 2);
         } else if (wish.lengthSqr() == 0.0 && motion.horizontalDistanceSqr() < 0.0025) {
            this.put = 0;
         }

         if (wish.lengthSqr() > 0.0 || motion.horizontalDistanceSqr() >= 0.0025) {
            double reserve = this.bestBlockSlot() < 0 ? 0.6 : 0.3;
            AABB ahead = body.move(motion.x * 3.0 + wish.x * reserve, 0.0, motion.z * 3.0 + wish.z * reserve);
            Vec3 next = ahead.getCenter();
            AABB feet = new AABB(next.x - 0.05, ahead.minY - 0.6, next.z - 0.05, next.x + 0.05, ahead.minY - 0.01, next.z + 0.05);
            if (MC.level.noCollision(MC.player, feet) && this.init_Z_D_5(this.depthBelow(feet.move(0.0, body.minY - feet.minY, 0.0)))) {
               this.put = 6;
            }
         }

         if (input.jump()) {
            if (((AutismLivingEntityAccessor)MC.player).autism$getNoJumpDelay() > 1) {
               return this.put-- > 0;
            }

            if (this.pop_Z || this.init_Z_Vec3_3(MC.player.position()) || MC.player.hasEffect(MobEffects.JUMP_BOOST) || !this.load_Z_Input_2(input)) {
               this.put = 0;
               return false;
            }

            this.put = Math.max(this.put, 6);
         }

         return this.put-- > 0;
      } else {
         this.put = 0;
         return false;
      }
   }

   private boolean load_Z_Input_2(Input input) {
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
      boolean unsafe = this.init_Z_NodeWalker_StreamU(start, pressed, world);
      if (!unsafe && !input.shift() && start.pose() != 0) {
         unsafe = this.init_Z_NodeWalker_StreamU(ClutchFallPhysics.withStandingPose(start), pressed, world);
      }

      if (unsafe) {
         this.status = "Holding unsafe jump";
      }

      return unsafe;
   }

   private boolean init_Z_NodeWalker_StreamU(
      autismclient.ai.engine.AiPhysics.MotionState start,
      autismclient.ai.engine.AiPhysics.MoveInput pressed,
      autismclient.ai.engine.AiPhysics.PhysicsWorld world
   ) {
      ClutchFallPhysics.Inner_ConfigStore intended = ClutchFallPhysics.simulateJumpTrajectory(start, pressed, world, this.safeFallDistance(), null, this::mediumAt);
      if (intended.unsafe() && !intended.steps().isEmpty()) {
         AABB last = intended.steps().getLast().body();
         if (!this.init_Z_D_5(this.depthBelow(last) + Math.max(0.0, start.y() - last.minY))) {
            return false;
         }

         int slot = this.bestBlockSlot();
         if (slot >= 0 && this.bool("own-steer") && (this.bool("own-knockback") || this.inGuardTime())) {
            for (boolean axis : new boolean[]{true, false}) {
               ClutchFallPhysics.Inner_ConfigStore braking = ClutchFallPhysics.simulateJumpTrajectory(start, pressed, world, this.safeFallDistance(), axis, this::mediumAt);
               if (!braking.unsafe()) {
                  return false;
               }

               ClutchModule.Inner_BufferHelper route = this.init_j_BufferHelper_i_ConfigStore_I(braking, slot);
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

   private boolean init_Z_D_5(double below) {
      return this.ownSteerWindow.isActive(this.clientTick())
         && (this.bool("own-knockback") && this.isDangerousDrop(0.0, below) || (this.inGuardTime() || this.prev()) && this.load_Z_D_D_2(0.0, below));
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
      if (input.jump() || MC.level.noCollision(MC.player, feet) && this.init_Z_D_5(this.depthBelow(feet.move(0.0, MC.player.getY() - feet.minY, 0.0)))) {
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
         Input result = init_Input_j_Input(clutch, input);
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

   private static Input init_Input_j_Input(ClutchModule clutch, Input input) {
      if (clutch.engaged && !clutch.load_Z) {
         if (!clutch.canSteer() || !clutch.set() || clutch.state == 3 || ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.entry, 4)) {
            return input;
         }

         if (clutch.close_Z) {
            return clutch.steerInput(input);
         }

         Vec3 motion = MC.player.getDeltaMovement();
         Vec3 wanted = clutch.init_Vec3_AABB(MC.player.getBoundingBox(), motion, clutch.flush_Z, clutch.state, MC.player.onGround());
         if (MC.player.onGround() && motion.y <= 0.0) {
            wanted = ClutchFallPhysics.brakingDirection(motion);
         }

         double yaw = Math.toRadians(MC.player.getYRot());
         double forward = wanted.z * Math.cos(yaw) - wanted.x * Math.sin(yaw);
         double side = wanted.x * Math.cos(yaw) + wanted.z * Math.sin(yaw);
         boolean edge = MC.player.onGround() && motion.y <= 0.0 && MC.level.noCollision(MC.player, MC.player.getBoundingBox().move(motion.x, -0.2, motion.z));
         boolean freshImpulse = clutch.launchCause != null && ClutchFallPhysics.isWithinTickWindow(clutch.clientTick(), clutch.launchCause.tick(), 2);
         boolean jump = clutch.init_Z_Z_I_2(clutch.update_I_3() >= 0, clutch.state);
         return new Input(
            forward > 0.08,
            forward < -0.08,
            side > 0.08,
            side < -0.08,
            jump,
            input.shift() || clutch.clientTick() == clutch.queue || edge && (!freshImpulse || clutch.update_I_3() < 0) && !jump,
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
            ClutchModule.Inner_ConfigStore old = clutch.wireState;
            Vec3 fallback = old == null ? MC.player.position() : old.position;
            RotationMath.Rotation oldAim = old == null ? RotationMath.ofPlayer(MC.player) : old.rotation;
            clutch.wireState = new ClutchModule.Inner_ConfigStore(
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
         if (this.bool("restore-slot") && this.next >= 0 && this.prev >= 0 && this.next != this.prev) {
            this.valid = this.next;
            this.size = this.prev;
            this.data = 20;
            this.run_V_3();
         }

         if ("clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && !this.valid()) {
            autismclient.util.SilentRotationManager.releaseOwner(this.id());
         }
      }
   }

   private void disengage() {
      if (this.engaged) {
         this.currentPlan = null;
         autismclient.util.InventorySlotArbiter.releaseAllSlots(this.id());
         if (MC.player != null && MC.level != null && MC.player.isAlive()) {
            if (!this.load_Z && !this.slotsReleased) {
               this.load_Z = true;
               this.get = 0;
               if (this.bool("restore-slot") && this.next >= 0 && this.prev >= 0 && this.next != this.prev) {
                  this.valid = this.next;
                  this.size = this.prev;
                  this.data = 20;
               }

               this.run_V_3();
            }

            this.get++;
            RotationMath.Rotation resume = ScaffoldModule.read_ce_DataUtil();
            boolean bridge = this.valid();
            if (this.slotsReleased) {
               this.load_V_Z_3(bridge);
            } else {
               if (bridge && "clutch".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && this.get < 20) {
                  RotationMath.Rotation goal = resume != null ? resume : RotationMath.ofPlayer(MC.player);
                  autismclient.util.SilentRotationManager.requestRotation(this.id(), 50, goal);
                  autismclient.util.SilentRotationManager.stepUrgentTimed(this.id(), MC.player, Math.max(1, Math.min(8, 19 - this.get)));
                  RotationMath.Rotation end = autismclient.util.SilentRotationManager.getCurrentRotation();
                  if (end != null && RotationMath.vectorAngleDegrees(end, goal) > 1.0F) {
                     this.status = "Handing back";
                     return;
                  }
               }

               this.load_V_Z_3(bridge);
            }
         } else {
            this.resetAll();
         }
      }
   }

   private boolean valid() {
      return ScaffoldModule.read_ce_DataUtil() != null || put();
   }

   private void load_V_Z_3(boolean bridge) {
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

      if (this.sync_Z) {
         this.add = this.clientTick();
      }

      this.sync_Z = false;
      this.engaged = this.load_Z = this.write_Z = this.slotsReleased = this.close_Z = false;
      this.state = 0;
      this.get = 0;
      this.status = "Idle";
      this.next = this.prev = -1;
   }

   private void tickRotationReturn() {
      if (!this.engaged && MC.player != null && this.rotationReturning()) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }
   }

   private void run_V_3() {
      if (this.valid >= 0 && MC.player != null) {
         if ((!this.engaged || this.load_Z || this.slotsReleased) && --this.data >= 0 && MC.player.getInventory().getSelectedSlot() == this.size) {
            autismclient.util.VirtualInputDispatcher.init(this.valid, this.size);
         } else {
            this.valid = -1;
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
      this.poll_I = -2147483648;
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
      this.poll_I = -2147483648;
      this.wireState = null;
      this.lastLevel = null;
      this.engaged = this.load_Z = this.read_Z = this.write_Z = this.slotsReleased = false;
      this.get = 0;
      this.next = this.prev = -1;
      this.currentPlan = null;
      this.add = -2147483648;
      this.sync_Z = false;
      this.ownSteerWindow.clear();
      this.preImpulseMotion = null;
      this.postImpulseMotion = Vec3.ZERO;
      this.clear = this.reset = -2147483648;
      this.start = -2147483648;
      this.valid = this.size = -1;
      this.init_BlockPos = this.load_BlockPos = null;
      this.read_BlockPos = this.write_BlockPos = this.sync_BlockPos = null;
      this.value = -2147483648;
      this.status = "Idle";
      this.tick = 0;
      this.push_I = -2147483648;
      this.put = 0;
      this.state = 0;
      this.close_Z = false;
      this.push_Z = false;
      this.hitAxisZ = null;
      this.lastShift = this.lastShiftOwned = false;
      this.queue = -2147483648;
      this.nextEyePosition = null;
      this.causeDebug = "";
      this.buffer = -2147483648;
      this.write_Vec3 = null;
      this.index = -2147483648;
      this.ownBlastCenter = null;
      this.entry = -2147483648;
      this.pop_Z = false;
      this.node = -2147483648;
      this.task = -2147483648;
      this.pool = -2147483648;
      this.sentPlacementHit = null;
      this.lastPlacedPos = null;
      this.frame = this.block = -1;
      this.chunk = -2147483648;
   }

   private record Inner_BufferHelper(ClutchBlockPlanner.Inner_JsonUtil init_k_JsonUtil, List<AABB> init_List, double init_D, boolean init_Z, List<Vec3> load) {
      Inner_BufferHelper(ClutchBlockPlanner.Inner_JsonUtil plan, List<AABB> bodies, double eyeHeight) {
         this(plan, bodies, eyeHeight, false, List.of());
      }

      Inner_BufferHelper(ClutchBlockPlanner.Inner_JsonUtil plan, List<AABB> bodies, double eyeHeight, boolean tower) {
         this(plan, bodies, eyeHeight, tower, List.of());
      }

      Vec3 init(int tick) {
         return this.load.isEmpty() ? init(this.init_List, this.init_D, tick) : this.load.get(Mth.clamp(tick, 0, this.load.size() - 1));
      }

      RotationMath.Rotation init_ce_DataUtil_k_NetHelper_I(ClutchBlockPlanner.Inner_NetHelper link, int tick) {
         return init_ce_DataUtil_Vec3_k_NetHelper(this.init(tick), link);
      }

      static Vec3 init(List<AABB> bodies, double eyeHeight, int tick) {
         AABB body = bodies.get(Mth.clamp(tick, 0, bodies.size() - 1));
         return new Vec3((body.minX + body.maxX) * 0.5, body.minY + eyeHeight, (body.minZ + body.maxZ) * 0.5);
      }

      static RotationMath.Rotation init(List<AABB> bodies, double eyeHeight, ClutchBlockPlanner.Inner_NetHelper link, int tick) {
         return init_ce_DataUtil_Vec3_k_NetHelper(init(bodies, eyeHeight, tick), link);
      }

      static RotationMath.Rotation init_ce_DataUtil_Vec3_k_NetHelper(Vec3 eye, ClutchBlockPlanner.Inner_NetHelper link) {
         BlockPos support = link.load_BlockPos();
         RotationMath.Rotation goal = RotationMath.lookAt(ClutchBlockPlanner.init_Vec3_Vec3(eye, support.getX(), support.getY(), support.getZ(), link.init_Direction()), eye);
         return new RotationMath.Rotation(goal.yaw(), Mth.clamp(goal.pitch(), -89.5F, 89.5F));
      }
   }

   private record Inner_CachePool(int init_I, ClutchFallPhysics.Inner_StreamUtil cause, boolean own, boolean load_Z, Vec3 position, Vec3 velocity, int tick) {
   }

   private record Inner_ConfigStore(Vec3 position, RotationMath.Rotation rotation, double eyeHeight, int tick) {
   }

   private record Inner_DataUtil(
      BlockPos pos,
      autismclient.util.BlockPlacementPlanner.PlacementPlan placement,
      int init_I,
      int load_I,
      boolean init_Z,
      boolean load_Z,
      Direction face,
      boolean read
   ) {
   }

   private final class Inner_StreamUtil {
      private boolean active;
      private final Long2ByteOpenHashMap init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap load_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
      private final Long2ByteOpenHashMap read = new Long2ByteOpenHashMap();
      private final MutableBlockPos scratchPos = new MutableBlockPos();
      private boolean sneaking;
      private List<AABB> init_List;
      private List<AABB> load_List;
      private ClutchModule.Inner_ConfigStore init_j_ConfigStore;
      private double cachedRange = 0.0 / 0.0;
      private BlockHitResult init_BlockHitResult;
      private DoubleUnaryOperator fallDamageCurve;

      void begin() {
         this.active = true;
         this.init_Long2ByteOpenHashM.clear();
         this.load_Long2ByteOpenHashM.clear();
         this.read.clear();
         this.init_List = this.load_List = null;
         this.init_j_ConfigStore = null;
         this.cachedRange = 0.0 / 0.0;
         this.init_BlockHitResult = null;
         this.fallDamageCurve = null;
      }

      void load() {
         this.active = false;
         this.init_List = this.load_List = null;
         this.init_j_ConfigStore = null;
         this.init_BlockHitResult = null;
         this.fallDamageCurve = null;
      }

      boolean init(int x, int y, int z, boolean sneaking) {
         if (!this.active) {
            return this.load(x, y, z, sneaking);
         }

         if (this.sneaking != sneaking) {
            this.init_Long2ByteOpenHashM.clear();
            this.sneaking = sneaking;
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.init_Long2ByteOpenHashM.get(key);
         if (known == 0) {
            this.init_Long2ByteOpenHashM.put(key, known = (byte)(this.load(x, y, z, sneaking) ? 1 : 2));
         }

         return known == 1;
      }

      boolean init(int x, int y, int z) {
         if (!this.active) {
            return this.read(x, y, z);
         }

         long key = BlockPos.asLong(x, y, z);
         byte known = this.load_Long2ByteOpenHashM.get(key);
         if (known == 0) {
            this.load_Long2ByteOpenHashM.put(key, known = (byte)(this.read(x, y, z) ? 1 : 2));
         }

         return known == 1;
      }

      boolean init_Z_Vec3_D(Vec3 eye, double distance) {
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
                           if (this.load(qx, qy, qz)) {
                              return false;
                           }
                        } else {
                           long key = BlockPos.asLong(qx, qy, qz);
                           byte known = this.read.get(key);
                           if (known == 0) {
                              this.read.put(key, known = (byte)(this.load(qx, qy, qz) ? 2 : 1));
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

      private boolean load(int qx, int qy, int qz) {
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

      List<AABB> init(boolean optional) {
         if (!this.active) {
            return ClutchModule.this.otherBodies(optional);
         } else if (optional) {
            return this.load_List != null ? this.load_List : (this.load_List = ClutchModule.this.otherBodies(true));
         } else {
            return this.init_List != null ? this.init_List : (this.init_List = ClutchModule.this.otherBodies(false));
         }
      }

      BlockHitResult init(double range) {
         ClutchModule.Inner_ConfigStore written = ClutchModule.this.wireState;
         if (this.active && written == this.init_j_ConfigStore && range == this.cachedRange) {
            return this.init_BlockHitResult;
         }

         BlockHitResult result = written != null && written.tick < ClutchModule.this.clientTick()
            ? ClutchModule.this.raycastBlock(written.position.add(0.0, written.eyeHeight, 0.0), written.rotation, range)
            : null;
         if (this.active) {
            this.init_j_ConfigStore = written;
            this.cachedRange = range;
            this.init_BlockHitResult = result;
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

      private boolean load(int x, int y, int z, boolean sneaking) {
         this.scratchPos.set(x, y, z);
         if (!Module.MC.level.hasChunkAt(this.scratchPos)) {
            return false;
         }

         BlockState state = Module.MC.level.getBlockState(this.scratchPos);
         return !state.canBeReplaced() && autismclient.util.BlockPlacementPlanner.isValidSupportBlock(state, this.scratchPos.immutable(), sneaking);
      }

      private boolean read(int x, int y, int z) {
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
