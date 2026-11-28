package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.api.module.ValueRange;
import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.SprintStateTracker;
import autismclient.util.EntityMotionTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class KBDisplacementModule extends Module implements SilentMovementCoordinator.Participant {
   private int lastWrappedAttackTick = -2147483648;
   private boolean sendingRedirect;
   private long redirectCount;
   private final QueuedAttackBuffer attackBuffer = new QueuedAttackBuffer();
   private final StrafeInputCorrector strafeCorrector = new StrafeInputCorrector();
   private long inputFixUntil;
   private ClientLevel inputFixLevel;
   private ClientLevel redirectLevel;
   private ClientPacketListener redirectConnection;
   private Player player;
   private int redirectSlot;
   private int redirectTick;
   private int lastPositionSendTick = -2147483648;
   private Item redirectItem;
   private float inputYaw;
   private float wireYaw;
   private float wirePitch;
   private LivingEntity redirectTarget;
   private boolean redirectSteer;
   private long throwCount;
   private long steerCount;
   private long profileSamples;
   private long profileNanos;
   private long maxPlanNanos;
   private long droppedCount;
   private final TreeMap<String, Integer> reasonCounts = new TreeMap<>();
   private String lastPlanKind = "none";
   private String debugLine = "";
   private float redirectTurn;
   private float redirectYaw;
   private float redirectPitch;
   private boolean flank;
   private int lastFlankSnapTick = -2147483648;
   private Vec3 clientServerOffset = Vec3.ZERO;
   private String redirectOutcome = "";
   private final TargetHurtWindowTracker hurtWindow = new TargetHurtWindowTracker();
   private final ArrayDeque<Long> recentRedirectTimes = new ArrayDeque<>();
   private static final long open = 10000000000L;
   private static final int sync_I = 12;
   private static final double init_D = 0.06;
   private static final double load_D = 62.0;
   private final WTapController wTap = new WTapController(new WTapController.Settings() {
      @Override
      public int chance() {
         return 100;
      }

      @Override
      public ValueRange delayRange() {
         return WTapController.DEFAULT_DELAY_MS;
      }

      @Override
      public ValueRange releaseRange() {
         return WTapController.DEFAULT_RELEASE_MS;
      }

      @Override
      public boolean playersOnly() {
         return KBDisplacementModule.this.bool("players-only");
      }

      @Override
      public boolean groundOnly() {
         return false;
      }
   });
   private final Map<Integer, Integer> lastGroundedTicks = new HashMap<>();
   private final Map<Integer, KBDisplacementModule.TargetTrack> targetTracks = new HashMap<>();
   private ClientLevel plannerWarmLevel;

   private void recordReason(String why) {
      this.reasonCounts.merge(why, 1, Integer::sum);
   }

   private void recordPlanTiming(long nanos, KnockbackThrowPlanner.ThrowPlan plan) {
      this.profileSamples++;
      this.profileNanos += nanos;
      this.maxPlanNanos = Math.max(this.maxPlanNanos, nanos);
      this.lastPlanKind = plan == null ? "none" : (plan.steer() ? "steer" : (plan.robust() ? "throw-robust" : "throw"));
   }

   public static String init_Str_7() {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m ? m.debugLine : "";
   }

   public static String load_Str_2() {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m) {
         StringBuilder var4 = new StringBuilder();

         for (Entry<String, Integer> entry : m.reasonCounts.entrySet()) {
            var4.append(var4.isEmpty() ? "" : ",").append(entry.getKey()).append(':').append(entry.getValue());
         }

         return String.format(
            Locale.ROOT,
            "plans=%d meanUs=%d maxUs=%d redirects=%d throws=%d steers=%d dropped=%d last=%s rtt=%d sprint=%s why=%s",
            m.profileSamples,
            m.profileSamples == 0L ? 0L : m.profileNanos / m.profileSamples / 1000L,
            m.maxPlanNanos / 1000L,
            m.redirectCount,
            m.throwCount,
            m.steerCount,
            m.droppedCount,
            m.lastPlanKind,
            m.hurtWindow.medianHurtDelay(m.pingTicks()),
            SprintStateTracker.predictedServerSprinting(),
            var4
         );
      } else {
         return "";
      }
   }

   static WTapController wTapController() {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.isEnabled() && m.bool("w-tap") ? m.wTap : null;
   }

   public KBDisplacementModule() {
      super("kb-displacement", "KBDisplacement", ae.init_ae, "Direct sprint and Knockback hits toward a qualifying fall.");
      this.add(
         new IntSetting("radius", "Radius", 3, 1, 10, 1)
            .unit("blocks")
            .description("Maximum distance from the target to the fall edge. Turns only when this hit can cause the fall; 1 fits a one-wide bridge.")
      );
      this.add(
         new BoolSetting("void-only", "Void Only", false)
            .description("Only falls into the bottom-of-world void. Floors, lava, catching media and unknown terrain do not qualify.")
      );
      this.add(
         new IntSetting("fall-height", "Fall Height", 3, 1, 40, 1)
            .description("Minimum blocks below the target's current footing.")
            .visibleWhen(() -> !this.bool("void-only"))
      );
      this.add(
         new EnumSetting<>("side", "Side", KBDisplacementModule.PushSide.Auto, KBDisplacementModule.PushSide.values(), Enum::name)
            .description("Choose either side automatically, or restrict the displacement side.")
      );
      this.add(new IntSetting("max-turn", "Max Turn", 100, 15, 180, 5).description("Largest temporary turn from the attack's existing server direction."));
      this.add(new BoolSetting("players-only", "Players Only", true).description("Redirect hits on players only."));
      this.add(new BoolSetting("w-tap", "W-Tap", true).description("Reset sprint after landed hits. The W-tapping module's settings win while it is on."));
      this.add(
         new BoolSetting("array-info", "ArrayList Info", false)
            .description("Show recent redirects next to the name in the array list. Each shows for 10 seconds.")
      );
   }

   public static void wrapAttack(Player player, Entity entity, Runnable attack) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.isEnabled() && !m.sendingRedirect && player == MC.player && entity instanceof LivingEntity target) {
         Runnable plain = () -> {
            attack.run();
            m.recordHurtWindow(target);
         };
         ClientLevel level = MC.level;
         if (level != null && m.canRedirectAttack(target)) {
            int tick = AutismSessionState.getInstance().getClientTick();
            if (m.attackBuffer.hasQueued()) {
               Runnable receipt = autismclient.util.CombatAttackDispatcher.receiptFor(target);
               if (!m.attackBuffer.queue(target, attack, receipt)) {
                  m.flushAttackBuffer(false);
                  plain.run();
                  if (receipt != null) {
                     receipt.run();
                  }
               }

               m.recordReason("carried-with-pending");
            } else {
               int fallback = m.pingTicks();
               if (m.lastWrappedAttackTick != tick && m.lastPositionSendTick != tick && !m.hurtWindow.isInHurtWindow(target.getId(), tick, fallback) && !WTappingModule.tapActive()) {
                  m.lastWrappedAttackTick = tick;
                  ServerRotationTracker.RotationSnapshot before = ServerRotationTracker.getSnapshot();
                  float yaw = ServerRotationTracker.getLastWireYaw();
                  float pitch = before.currentPitch();
                  if (before.initialized() && Float.isFinite(yaw) && Float.isFinite(pitch)) {
                     Packet<?> packet = new ServerboundAttackPacket(target.getId());
                     if (packetsPassFilters(new Rot(yaw, pitch, player.onGround(), player.horizontalCollision), packet)
                        && packetsPassFilters(new PosRot(player.position(), yaw, pitch, player.onGround(), player.horizontalCollision), packet)) {
                        KnockbackThrowPlanner.ThrowPlan plan;
                        KBDisplacementModule.TerrainSampler terrain;
                        try {
                           double extra = m.knockbackStrength();
                           terrain = new KBDisplacementModule.TerrainSampler(level, target, pitch);
                           Vec3 nextEye = terrain.inputVector(yaw).add(0.0, MC.player.getEyeHeight(), 0.0);
                           AABB nextBody = target.getBoundingBox().move(targetMotion(target));
                           terrain.pitch = AimHitboxHelper.init(pitch, nextEye, nextBody, nextEye, nextBody);
                           terrain.aimHits = AimHitboxHelper.rayHits(nextBody, nextEye, yaw, pitch, autismclient.util.AttackReachHelper.maxReach(MC.player));
                           terrain.sprintHit = SprintStateTracker.effectiveSprinting(MC.player.isSprinting()) && MC.player.getAttackStrengthScale(0.5F) > 0.9F
                              || autismclient.util.LegacyCombatDetector.isLegacyCombat(MC.player);
                           KBDisplacementModule.DisplacementResult struck = m.simulateStrike(target, terrain, tick, m.hurtWindow.medianHurtDelay(fallback));
                           terrain.strikeShift = struck.horizontalShift();
                           boolean grounded = struck.grounded();
                           AABB body = struck.body();
                           double reference = grounded ? body.minY : terrain.floorReference(body);
                           Vec3 server = m.serverSideMotion(target, grounded, body, reference, struck.extraAir());
                           KnockbackThrowPlanner.ThrowInput input = new KnockbackThrowPlanner.ThrowInput(
                              body,
                              player.position().add(struck.horizontalShift()),
                              server,
                              grounded,
                              target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),
                              extra,
                              reference,
                              autismclient.util.LegacyCombatDetector.isLegacy()
                           );
                           byte var10000;
                           switch (m.choice("side")) {
                              case "Left":
                                 var10000 = -1;
                                 break;
                              case "Right":
                                 var10000 = 1;
                                 break;
                              default:
                                 var10000 = 0;
                           }

                           int side = var10000;
                           KnockbackThrowPlanner.PlanSettings settings = new KnockbackThrowPlanner.PlanSettings(
                              m.integer("max-turn"), m.integer("fall-height"), m.bool("void-only"), side, m.integer("radius")
                           );
                           long started = System.nanoTime();
                           plan = KnockbackThrowPlanner.plan(input, yaw, RotationMath.gcdStep(), settings, m.counterHypotheses(target, grounded, server), terrain);
                           m.recordPlanTiming(System.nanoTime() - started, plan);
                        } catch (RuntimeException ignored) {
                           m.recordReason("error");
                           plain.run();
                           return;
                        }

                        if (plan == null) {
                           m.recordReason("plan:" + KnockbackThrowPlanner.lastStatus);
                           plain.run();
                        } else {
                           double reach = autismclient.util.AttackReachHelper.maxReach(MC.player) - 0.06;
                           Vec3 nextEye = terrain.inputVector(plan.yaw()).add(0.0, MC.player.getEyeHeight(), 0.0);
                           float inputYaw = SilentMovementCoordinator.getMovementYaw(MC.player);
                           if (!(target.getBoundingBox().move(targetMotion(target)).distanceToSqr(nextEye) > reach * reach)
                              && autismclient.util.SilentRotationManager.snapKbDisplacementRotation(new RotationMath.Rotation(plan.yaw(), terrain.pitch))) {
                              m.recordReason("redirect");
                              m.redirectLevel = level;
                              m.redirectConnection = MC.getConnection();
                              m.player = player;
                              m.redirectSlot = MC.player.getInventory().getSelectedSlot();
                              m.redirectTick = tick;
                              m.redirectItem = MC.player.getMainHandItem().getItem();
                              m.inputYaw = inputYaw;
                              m.wireYaw = plan.yaw();
                              m.wirePitch = terrain.pitch;
                              m.redirectTarget = target;
                              m.redirectSteer = plan.steer();
                              m.flank = !terrain.staysAimed(plan.yaw());
                              m.redirectYaw = yaw;
                              m.redirectPitch = pitch;
                              m.redirectTurn = Mth.wrapDegrees(plan.yaw() - yaw);
                              m.redirectOutcome = plan.redirected().outcome()
                                 + (plan.robust() ? " robust" : "")
                                 + (KnockbackThrowPlanner.lastStrafe ? " strafe" : "")
                                 + " ticks="
                                 + plan.redirected().ticks()
                                 + " lead="
                                 + String.format(Locale.ROOT, "%.2f,%.2f", m.clientServerOffset.x, m.clientServerOffset.z);
                              m.attackBuffer.queue(target, attack, autismclient.util.CombatAttackDispatcher.receiptFor(target));
                           } else {
                              m.recordReason(
                                 target.getBoundingBox().move(targetMotion(target)).distanceToSqr(nextEye) > reach * reach
                                    ? "reach"
                                    : "rotation-busy"
                              );
                              plain.run();
                           }
                        }
                     } else {
                        m.recordReason("transport");
                        plain.run();
                     }
                  } else {
                     m.recordReason("no-wire-look");
                     plain.run();
                  }
               } else {
                  m.recordReason(m.lastWrappedAttackTick == tick ? "same-tick" : (m.lastPositionSendTick == tick ? "after-movement" : (WTappingModule.tapActive() ? "w-tap" : "immune")));
                  plain.run();
               }
            }
         } else {
            m.recordReason("ineligible");
            plain.run();
         }
      } else {
         attack.run();
      }
   }

   private void recordHurtWindow(LivingEntity target) {
      this.hurtWindow.onAttackSent(target.getId(), AutismSessionState.getInstance().getClientTick(), this.pingTicks());
   }

   private int pingTicks() {
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection != null && MC.player != null ? connection.getPlayerInfo(MC.player.getUUID()) : null;
      return Math.round(Math.max(0, info == null ? 0 : info.getLatency()) / 50.0F) + 1;
   }

   private KBDisplacementModule.DisplacementResult simulateStrike(LivingEntity target, KBDisplacementModule.TerrainSampler terrain, int tick, int roundTrip) {
      Vec3 latest = EntityMotionTracker.observedPosition(target);
      AABB body = target.getBoundingBox().move(latest.subtract(target.position()));
      boolean grounded = target.onGround();
      if (!(target instanceof Player)) {
         return new KBDisplacementModule.DisplacementResult(body, grounded, 0, Vec3.ZERO);
      }

      KBDisplacementModule.TargetTrack track = this.targetTracks.get(target.getId());
      Vec3 motion = track == null ? Vec3.ZERO : track.motion();
      this.clientServerOffset = Vec3.ZERO;
      if (motion.lengthSqr() < 1.0E-8) {
         return new KBDisplacementModule.DisplacementResult(body, grounded, 0, Vec3.ZERO);
      }

      int age = Math.max(0, Math.min(4, tick - track.tick()));
      int toServer = Math.max(0, Math.min(12, roundTrip + 1 + age));
      int toClient = Math.max(0, Math.min(12 - toServer, Math.round(latencyMs(target) / 50.0F)));
      KnockbackThrowPlanner.SimulatedMotion atServer = KnockbackThrowPlanner.simulate(body, motion, grounded, toServer, terrain);
      KnockbackThrowPlanner.SimulatedMotion atClient = KnockbackThrowPlanner.simulate(atServer.body(), atServer.motion(), atServer.grounded(), toClient, terrain);
      Vec3 shift = atClient.body().getCenter().subtract(atServer.body().getCenter());
      this.clientServerOffset = atClient.body().getCenter().subtract(body.getCenter());
      return new KBDisplacementModule.DisplacementResult(atClient.body(), atServer.grounded(), grounded ? atServer.ticks() : toServer, new Vec3(shift.x, 0.0, shift.z));
   }

   private static int latencyMs(LivingEntity target) {
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection == null ? null : connection.getPlayerInfo(target.getUUID());
      return info == null ? 0 : Math.max(0, Math.min(300, info.getLatency()));
   }

   private Vec3 serverSideMotion(LivingEntity target, boolean grounded, AABB body, double reference, int extraAir) {
      if (!(target instanceof Player)) {
         return targetMotion(target);
      }

      if (grounded) {
         return new Vec3(0.0, -0.0784, 0.0);
      }

      int air = Math.max(1, Math.min(60, AutismSessionState.getInstance().getClientTick() - this.lastGroundedTickOf(target) + Math.max(0, extraAir)));
      double vy = -0.0784;
      double height = body.minY - reference;

      for (int i = 1; i < air; i++) {
         if (height >= 0.0 && -vy >= height) {
            vy = -0.0784;
         } else {
            vy = (vy - 0.08) * 0.98;
         }
      }

      return new Vec3(0.0, vy, 0.0);
   }

   private KnockbackThrowPlanner.CounterHypotheses counterHypotheses(LivingEntity target, boolean grounded, Vec3 server) {
      Vec3[] alternatives = new Vec3[0];
      if (!grounded && target instanceof Player) {
         Vec3 seen = targetMotion(target);
         alternatives = new Vec3[]{new Vec3(server.x, Math.max(-3.0, Math.min(3.0, seen.y)), server.z), new Vec3(server.x, -0.0784, server.z)};
      }

      KnockbackThrowPlanner.CounterSteer counter = KnockbackThrowPlanner.CounterSteer.NONE;
      double strafe = 0.0;
      if (target instanceof Player) {
         double speed = target.getAttributeValue(Attributes.MOVEMENT_SPEED);
         counter = KnockbackThrowPlanner.CounterSteer.forYaw(target.getYRot(), speed, target.isSprinting() ? 0.026 : 0.02);
         strafe = target.isSprinting() ? speed * 1.3 : speed;
      }

      return new KnockbackThrowPlanner.CounterHypotheses(counter, alternatives, strafe);
   }

   private int lastGroundedTickOf(LivingEntity target) {
      Integer seen = this.lastGroundedTicks.get(target.getId());
      return seen == null ? AutismSessionState.getInstance().getClientTick() - 1 : seen;
   }

   private void trackNearbyPlayers() {
      if (MC.level != null && MC.player != null) {
         int tick = AutismSessionState.getInstance().getClientTick();
         this.lastGroundedTicks.keySet().removeIf(id -> MC.level.getEntity(id) == null);
         this.targetTracks.keySet().removeIf(id -> MC.level.getEntity(id) == null);

         for (Player other : MC.level.players()) {
            if (other != MC.player && !(other.distanceToSqr(MC.player) > 1024.0)) {
               if (other.onGround() || !this.lastGroundedTicks.containsKey(other.getId())) {
                  this.lastGroundedTicks.put(other.getId(), tick);
               }

               Vec3 latest = EntityMotionTracker.observedPosition(other);
               KBDisplacementModule.TargetTrack old = this.targetTracks.get(other.getId());
               if (old == null) {
                  this.targetTracks.put(other.getId(), new KBDisplacementModule.TargetTrack(latest, tick, Vec3.ZERO));
               } else if (latest.equals(old.latest())) {
                  if (tick - old.tick() > 3 && old.motion().lengthSqr() > 0.0) {
                     this.targetTracks.put(other.getId(), new KBDisplacementModule.TargetTrack(old.latest(), old.tick(), Vec3.ZERO));
                  }
               } else {
                  Vec3 motion = latest.subtract(old.latest()).scale(1.0 / Math.max(1, Math.min(4, tick - old.tick())));
                  if (motion.horizontalDistanceSqr() > 2.25 || Math.abs(motion.y) > 4.0) {
                     motion = Vec3.ZERO;
                  }

                  this.targetTracks.put(other.getId(), new KBDisplacementModule.TargetTrack(latest, tick, motion));
               }
            }
         }
      } else {
         this.lastGroundedTicks.clear();
         this.targetTracks.clear();
      }
   }

   private static boolean ownsSilentRotation() {
      return "kb-displacement".equals(autismclient.util.SilentRotationManager.getActiveOwnerId());
   }

   public static boolean hasQueuedAttack() {
      return ownsSilentRotation() && al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.attackBuffer.hasQueued();
   }

   public static boolean init_Z_Player(Player player, InteractionHand hand, Runnable swing) {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && !m.sendingRedirect
         && player == m.player
         && MC.level == m.redirectLevel
         && hand == InteractionHand.MAIN_HAND
         && m.attackBuffer.enqueue(swing);
   }

   public static float movementYaw(float camera) {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.attackBuffer.hasQueued() ? m.inputYaw : camera;
   }

   public static void afterSendPosition() {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m) {
         m.lastPositionSendTick = AutismSessionState.getInstance().getClientTick();
      }
   }

   @Override
   public void preMovementTick() {
      if (WTappingModule.wTapController() == this.wTap) {
         this.wTap.tick();
      } else {
         this.wTap.cancelTap();
      }

      if (ownsSilentRotation() && MC.options.keyUse.isDown()) {
         this.resetState();
         autismclient.util.SilentRotationManager.resetAll();
      } else {
         int tick = AutismSessionState.getInstance().getClientTick();
         if (this.attackBuffer.hasQueued() && tick != this.redirectTick) {
            this.flushAttackBuffer(tick == this.redirectTick + 1 && this.lastPositionSendTick == this.redirectTick);
         }

         if (!this.attackBuffer.hasQueued() && this.lastFlankSnapTick != tick) {
            if (ownsSilentRotation() && !autismclient.util.SilentRotationManager.isReturningToPlayer()) {
               autismclient.util.SilentRotationManager.releaseOwner(this.id());
            }

            this.clearRedirect();
         }
      }
   }

   public static void onTickHead() {
      if (ownsSilentRotation() && al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && !m.attackBuffer.hasQueued()) {
         autismclient.util.SilentRotationManager.tick(m.id(), MC.player);
      }
   }

   private void flushAttackBuffer(boolean displaced) {
      boolean context = this.isRedirectContextValid();
      float wireYaw = SilentMovementCoordinator.getCameraYaw();
      float wirePitch = SilentMovementCoordinator.getCameraPitch();
      if (!Float.isFinite(wireYaw) || !Float.isFinite(wirePitch)) {
         wireYaw = this.wireYaw;
         wirePitch = this.wirePitch;
      }

      displaced &= Math.abs(Mth.wrapDegrees(wireYaw - this.wireYaw)) < 1.0F && Math.abs(wirePitch - this.wirePitch) < 1.0F;
      float yaw = wireYaw;
      float pitch = wirePitch;
      this.sendingRedirect = true;
      int queued = this.attackBuffer.size();

      try {
         boolean flank = this.flank;
         int count = this.attackBuffer
            .dispatch(
               value -> context
                  && value instanceof LivingEntity target
                  && target == this.redirectLevel.getEntity(target.getId())
                  && this.canRedirectAttack(target)
                  && (
                     AimHitboxHelper.rayHits(target.getBoundingBox(), MC.player.getEyePosition(), yaw, pitch, autismclient.util.AttackReachHelper.maxReach(MC.player), 0.002)
                        || flank
                           && AimHitboxHelper.rayHits(
                              target.getBoundingBox(), MC.player.getEyePosition(), this.redirectYaw, this.redirectPitch, autismclient.util.AttackReachHelper.maxReach(MC.player), 0.002
                           )
                  ),
               value -> {
                  if (value instanceof LivingEntity target) {
                     this.recordHurtWindow(target);
                  }
               }
            );
         this.droppedCount += queued - count;
         if (flank && count > 0 && autismclient.util.SilentRotationManager.snapKbDisplacementRotation(new RotationMath.Rotation(this.redirectYaw, this.redirectPitch))) {
            this.lastFlankSnapTick = AutismSessionState.getInstance().getClientTick();
         }

         if (displaced && count > 0) {
            long now = System.nanoTime();
            this.redirectCount += count;

            for (int i = 0; i < count; i++) {
               this.recentRedirectTimes.addLast(now);
            }

            this.debugLine = String.format(
               Locale.ROOT, "tick=%d turn=%.1f pitch=%.1f %s%s", AutismSessionState.getInstance().getClientTick(), this.redirectTurn, this.wirePitch, this.redirectOutcome, flank ? " flanked" : ""
            );
            if (this.redirectSteer) {
               this.steerCount += count;
            } else {
               this.throwCount += count;
            }

            this.inputFixUntil = System.nanoTime() + 2000000000L;
            this.inputFixLevel = MC.level;
            this.strafeCorrector.reset();
         }
      } finally {
         this.sendingRedirect = false;
         this.clearRedirect();
         this.flank = false;
      }
   }

   @Override
   public boolean isSilentMovementActive() {
      return !SilentMovementCoordinator.isSafeWalkActive() && ownsSilentRotation() && (this.attackBuffer.hasQueued() || this.lastFlankSnapTick == AutismSessionState.getInstance().getClientTick() || autismclient.util.SilentRotationManager.isReturningToPlayer());
   }

   private boolean isRedirectContextValid() {
      return MC.player != null
         && MC.player == this.player
         && MC.level == this.redirectLevel
         && MC.getConnection() == this.redirectConnection
         && MC.player.getInventory().getSelectedSlot() == this.redirectSlot
         && MC.player.getMainHandItem().is(this.redirectItem);
   }

   private void clearRedirect() {
      this.redirectLevel = null;
      this.redirectConnection = null;
      this.player = null;
      this.redirectItem = null;
      this.redirectTarget = null;
      this.redirectSteer = false;
   }

   public static void onDamageEvent(ClientboundDamageEventPacket packet) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && m.isEnabled()
         && MC.level != null
         && MC.player != null
         && packet.entityId() != MC.player.getId()) {
         m.hurtWindow.onHurt(packet.entityId(), AutismSessionState.getInstance().getClientTick(), packet.getSource(MC.level).getEntity() == MC.player, m.pingTicks());
      }
   }

   private static boolean isAimedAt(AABB box, Vec3 eye, float yaw, float pitch) {
      return AimHitboxHelper.rayHits(box, eye, yaw, pitch, autismclient.util.AttackReachHelper.maxReach(MC.player));
   }

   public static Input fixInput(Input input, float camera, float yaw) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && m.isEnabled()
         && MC.player != null
         && MC.level == m.inputFixLevel
         && System.nanoTime() < m.inputFixUntil
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && !MC.player.onClimbable()
         && !MC.player.isFallFlying()
         && !MC.player.isCrouching()
         && !input.shift()) {
         float friction = MC.level.getBlockState(BlockPos.containing(MC.player.getX(), MC.player.getY() - 0.500001, MC.player.getZ())).getBlock().getFriction();
         double drag = MC.player.onGround() ? friction * 0.91 : 0.91;
         return m.strafeCorrector.correct(input, MC.player.position(), camera, yaw, drag, AutismSessionState.getInstance().getClientTick(), keys -> {
            boolean sprint = keys.forward() && !keys.backward() && (MC.player.isSprinting() || MovementModuleHooks.wantsSprint(input.sprint(), false));
            double speed = MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (sprint != MC.player.isSprinting()) {
               speed *= sprint ? 1.3 : 0.7692307692307692;
            }

            double acceleration = MC.player.onGround() ? speed * (0.21600002F / (friction * friction * friction)) : (sprint ? 0.026 : 0.02);
            return KnockbackThrowPlanner.applyInput(MC.player.getDeltaMovement(), keys, yaw, acceleration);
         });
      } else {
         return null;
      }
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && ownsSilentRotation()) {
         autismclient.util.SilentRotationManager.tick(this.id(), MC.player);
      }

      if (this.isEnabled()) {
         this.trackNearbyPlayers();
         this.warmUpPlanner();
      } else if (!this.lastGroundedTicks.isEmpty()) {
         this.lastGroundedTicks.clear();
      }
   }

   private void warmUpPlanner() {
      if (MC.player != null
         && MC.level != null
         && this.plannerWarmLevel != MC.level
         && MC.gui.screen() == null
         && MC.player.tickCount >= 100
         && MC.player.onGround()) {
         for (Player other : MC.level.players()) {
            if (other != MC.player && other.distanceToSqr(MC.player) < 12.25) {
               return;
            }
         }

         this.plannerWarmLevel = MC.level;

         try {
            AABB body = MC.player.getBoundingBox();
            KnockbackThrowPlanner.ThrowInput input = new KnockbackThrowPlanner.ThrowInput(
               body, MC.player.position().add(0.0, 0.0, -1.0), new Vec3(0.0, -0.0784, 0.0), true, 0.0, 0.5, body.minY, false
            );
            KnockbackThrowPlanner.CounterSteer counter = KnockbackThrowPlanner.CounterSteer.forYaw(180.0F, 0.13, 0.026);

            for (int radius : new int[]{3, 10}) {
               KnockbackThrowPlanner.plan(
                  input,
                  MC.player.getYRot(),
                  RotationMath.gcdStep(),
                  new KnockbackThrowPlanner.PlanSettings(100.0, 3.0, false, 0, radius),
                  new KnockbackThrowPlanner.CounterHypotheses(counter, new Vec3[0]),
                  new KBDisplacementModule.TerrainSampler(MC.level, MC.player, MC.player.getXRot())
               );
            }
         } catch (RuntimeException var8) {
         }
      }
   }

   @Override
   public boolean ticksWhenDisabled() {
      return true;
   }

   @Override
   public boolean hasDisabledTickWork() {
      return ownsSilentRotation();
   }

   private boolean canRedirectAttack(LivingEntity target) {
      if (!MC.isSameThread()
         || MC.player == null
         || MC.level == null
         || MC.getConnection() == null
         || MC.gameMode == null
         || MC.gui.screen() != null
         || MC.gui.overlay() != null
         || MC.options.keyUse.isDown()
         || MC.options.keyInventory.isDown()
         || autismclient.util.ManualInteractionYield.isYielding()
         || MC.gameMode instanceof AutismMultiPlayerGameModeAccessor digging && digging.autism$isDestroying() && !BedNukerModule.isMining()
         || aD.init_Z()
         || FreecamController.isActive()
         || FreecamController.isAiControllingBody()
         || !MC.player.isAlive()
         || MC.player.isSpectator()
         || MC.gameMode.isSpectator()
         || MC.player.isUsingItem()
         || MC.player.isPassenger()
         || MC.player.isFallFlying()
         || MC.player.getAbilities().flying
         || MC.player.isInWater()
         || MC.player.isInLava()
         || MC.player.onClimbable()
         || MC.player.isCrouching()
         || MC.options.keyShift.isDown()
         || MC.player.getAttributeValue(Attributes.FRICTION_MODIFIER) != 1.0
         || MC.player.getAttributeValue(Attributes.AIR_DRAG_MODIFIER) != 1.0
         || target == MC.player
         || target.level() != MC.level
         || !target.isAlive()
         || !target.isAttackable()
         || target.isSpectator()
         || target.isPassenger()
         || target.isFallFlying()
         || target.isInWater()
         || target.isInLava()
         || target.onClimbable()
         || target.isNoGravity()
         || this.bool("players-only") && !(target instanceof Player)
         || target instanceof Player p && (p.isCreative() || p.getAbilities().flying)
         || target.hasEffect(MobEffects.LEVITATION)
         || target.hasEffect(MobEffects.SLOW_FALLING)
         || MC.player.getMainHandItem().has(DataComponents.PIERCING_WEAPON)
         || TeamsModule.isFriend(target)
         || AntiBotTracker.isBot(target)) {
         return false;
      }

      if (ClutchModule.isClutchEngaged()
         || DangerModule.sync_Z_12()
         || SilentMovementCoordinator.isSafeWalkActive()
         || BedNukerModule.init_Z_23()
         || AutoTotemModule.init_Z_6()
         || AutoArmorModule.isClickPending()
         || autismclient.util.macro.MacroAimController.isActive()) {
         return false;
      }

      if (SilentMovementCoordinator.activeRotation() != null && !QueuedAttackBuffer.isCompatibleOwner(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         return false;
      }

      double reach = autismclient.util.AttackReachHelper.maxReach(MC.player);
      return target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach && MC.player.hasLineOfSight(target);
   }

   private double knockbackStrength() {
      int level = EnchantmentHelper.getItemEnchantmentLevel(
         MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), MC.player.getMainHandItem()
      );
      return KnockbackThrowPlanner.knockbackStrength(
         MC.player.getAttributeValue(Attributes.ATTACK_KNOCKBACK),
         level,
         SprintStateTracker.effectiveSprinting(MC.player.isSprinting()),
         MC.player.getAttackStrengthScale(0.5F),
         autismclient.util.LegacyCombatDetector.isLegacyCombat(MC.player)
      );
   }

   private static Vec3 targetMotion(LivingEntity target) {
      Vec3 motion = target.position().subtract(new Vec3(target.xOld, target.yOld, target.zOld));
      return target.tickCount > 1 ? motion : target.getDeltaMovement();
   }

   private static boolean packetsPassFilters(Packet<?> look, Packet<?> attack) {
      Module canceller = al.init_Module_Str("packet-canceller");
      if (!BlinkPacketManager.isBlinking() && (!autismclient.util.macro.PingSpoofScheduler.isActive() || !autismclient.util.macro.PingSpoofScheduler.isRealOutgoing()) && !AutismSessionState.getInstance().isUseCustomPackets()) {
         return canceller == null || !canceller.isEnabled() || !canceller.onPacketSend(look) && !canceller.onPacketSend(attack)
            ? autismclient.util.macro.PacketGateManager.match(look, "C2S") == autismclient.util.macro.PacketGateManager.GateVerdict.PASS
               && autismclient.util.macro.PacketGateManager.match(attack, "C2S") == autismclient.util.macro.PacketGateManager.GateVerdict.PASS
            : false;
      } else {
         return false;
      }
   }

   private void resetState() {
      this.attackBuffer.clear();
      this.clearRedirect();
      this.recentRedirectTimes.clear();
      this.strafeCorrector.reset();
      this.inputFixLevel = null;
      this.inputFixUntil = 0L;
      this.lastWrappedAttackTick = -2147483648;
      this.lastFlankSnapTick = -2147483648;
      this.flank = false;
   }

   @Override
   public void onEnable() {
      KBDisplacementWarmup.start();
      this.wTap.resetAll();
      this.resetState();
      this.redirectCount = this.throwCount = this.steerCount = this.profileSamples = this.profileNanos = this.maxPlanNanos = this.droppedCount = 0L;
      this.reasonCounts.clear();
      this.lastPlanKind = "none";
      this.lastGroundedTicks.clear();
      this.hurtWindow.clear();
      this.targetTracks.clear();
   }

   @Override
   public void onDisable() {
      this.resetState();
      this.wTap.resetAll();
      if (ownsSilentRotation()) {
         autismclient.util.SilentRotationManager.releaseOwner(this.id());
      }
   }

   @Override
   public void onGameLeft() {
      this.resetState();
      this.wTap.resetAll();
      this.lastGroundedTicks.clear();
      this.plannerWarmLevel = null;
      this.hurtWindow.clear();
      this.targetTracks.clear();
      if (ownsSilentRotation()) {
         autismclient.util.SilentRotationManager.resetAll();
      }
   }

   @Override
   public void onGameJoin() {
      this.onGameLeft();
      this.lastPositionSendTick = -2147483648;
      this.lastGroundedTicks.clear();
      if (this.isEnabled()) {
         KBDisplacementWarmup.start();
      }
   }

   @Override
   public String info() {
      if (!this.bool("array-info")) {
         return "";
      }

      long now = System.nanoTime();

      while (!this.recentRedirectTimes.isEmpty() && now - this.recentRedirectTimes.peekFirst() >= 10000000000L) {
         this.recentRedirectTimes.pollFirst();
      }

      int recent = this.recentRedirectTimes.size();
      return recent == 0 ? "" : (recent == 1 ? "1 redirect" : recent + " redirects");
   }

   private record TargetTrack(Vec3 latest, int tick, Vec3 motion) {
   }

   private static final class TerrainSampler implements KnockbackThrowPlanner.ThrowWorld {
      private static final byte init_B = 1;
      private static final byte load_B = 2;
      private static final byte read_B = 3;
      private static final byte write_B = 4;
      final ClientLevel level;
      final LivingEntity target;
      final Map<Long, Boolean> chunkLoaded = new HashMap<>();
      final Map<Long, ChunkAccess> chunkCache = new HashMap<>();
      final Map<Long, BlockState> blockStateCache = new HashMap<>();
      final MutableBlockPos cursor = new MutableBlockPos();
      final CollisionContext collisionContext;
      float pitch;
      Vec3 strikeShift = Vec3.ZERO;
      boolean aimHits;
      boolean sprintHit;
      final float movementYaw;
      final Map<Float, Vec3> inputVectorCache = new HashMap<>();
      final int minY;
      final int maxY;

      TerrainSampler(ClientLevel level, LivingEntity target, float pitch) {
         this.level = level;
         this.target = target;
         this.pitch = pitch;
         this.collisionContext = CollisionContext.of(target);
         this.movementYaw = SilentMovementCoordinator.getMovementYaw(Module.MC.player);
         this.minY = level.getMinY();
         this.maxY = level.getMaxY();
      }

      private Vec3 inputVector(float yaw) {
         return this.inputVectorCache
            .computeIfAbsent(
               yaw,
               key -> {
                  Options o = Module.MC.options;
                  Input raw = new Input(
                     o.keyUp.isDown(),
                     o.keyDown.isDown(),
                     o.keyLeft.isDown(),
                     o.keyRight.isDown(),
                     o.keyJump.isDown(),
                     o.keyShift.isDown(),
                     o.keySprint.isDown()
                  );
                  Input keys = SilentMovementCoordinator.correctInputForYaw(raw, this.movementYaw, yaw);
                  boolean sprint = keys.forward() && !keys.backward() && (Module.MC.player.isSprinting() || MovementModuleHooks.wantsSprint(raw.sprint(), false));
                  double speed = Module.MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                  if (sprint != Module.MC.player.isSprinting()) {
                     speed *= sprint ? 1.3 : 0.7692307692307692;
                  }

                  BlockPos below = BlockPos.containing(Module.MC.player.getX(), Module.MC.player.getY() - 0.500001, Module.MC.player.getZ());
                  float friction = this.cachedBlockState(below).getBlock().getFriction();
                  double acceleration = Module.MC.player.onGround() ? speed * (0.21600002F / (friction * friction * friction)) : (sprint ? 0.026 : 0.02);
                  Vec3 velocity = Module.MC.player.getDeltaMovement();
                  if (velocity.horizontalDistanceSqr() < 9.0E-6) {
                     velocity = new Vec3(0.0, velocity.y, 0.0);
                  }

                  if (Math.abs(velocity.y) < 0.003) {
                     velocity = new Vec3(velocity.x, 0.0, velocity.z);
                  }

                  AutismLivingEntityAccessor living = (AutismLivingEntityAccessor)Module.MC.player;
                  if (raw.jump() && Module.MC.player.onGround() && living.autism$getNoJumpDelay() <= 1) {
                     velocity = new Vec3(velocity.x, Math.max(living.autism$getJumpPower(), velocity.y), velocity.z);
                     if (sprint) {
                        double a = Math.toRadians(yaw);
                        velocity = velocity.add(-Math.sin(a) * 0.2, 0.0, Math.cos(a) * 0.2);
                     }
                  }

                  Vec3 movement = KnockbackThrowPlanner.applyInput(velocity, keys, yaw, acceleration);
                  return !this.regionLoaded(Module.MC.player.getBoundingBox().expandTowards(movement))
                     ? Module.MC.player.position()
                     : Module.MC
                        .player
                        .position()
                        .add(Entity.collideBoundingBox(Module.MC.player, movement, Module.MC.player.getBoundingBox(), this.level, List.of()));
               }
            );
      }

      @Override
      public KnockbackThrowPlanner.ThrowInput inputForYaw(KnockbackThrowPlanner.ThrowInput input, float yaw) {
         return input.withOrigin(this.inputVector(yaw).add(this.strikeShift));
      }

      @Override
      public boolean acceptsYaw(float yaw) {
         return !this.aimHits ? this.staysAimed(yaw) : !this.sprintHit || this.keepsMovementHeading(yaw);
      }

      boolean staysAimed(float yaw) {
         Vec3 next = this.inputVector(yaw).add(0.0, Module.MC.player.getEyeHeight(), 0.0);
         return KBDisplacementModule.isAimedAt(this.target.getBoundingBox().move(KBDisplacementModule.targetMotion(this.target)), next, yaw, this.pitch);
      }

      boolean keepsMovementHeading(float yaw) {
         Options o = Module.MC.options;
         Input raw = new Input(
            o.keyUp.isDown(), o.keyDown.isDown(), o.keyLeft.isDown(), o.keyRight.isDown(), o.keyJump.isDown(), o.keyShift.isDown(), o.keySprint.isDown()
         );
         if (raw.forward() && !raw.backward()) {
            Input keys = SilentMovementCoordinator.correctInputForYaw(raw, this.movementYaw, yaw);
            return keys.forward() && !keys.backward() && Math.abs(Mth.wrapDegrees(yaw - this.movementYaw)) <= 62.0;
         } else {
            return Math.abs(Mth.wrapDegrees(yaw - this.movementYaw)) <= 62.0;
         }
      }

      @Override
      public boolean confirmsYaw(float yaw) {
         return this.acceptsYaw(yaw);
      }

      @Override
      public double minY() {
         return this.level.getMinY();
      }

      @Override
      public boolean regionLoaded(AABB area) {
         area = area.inflate(1.0, 0.0, 1.0);
         if (!(area.minX < this.level.getWorldBorder().getMinX())
            && !(area.maxX > this.level.getWorldBorder().getMaxX())
            && !(area.minZ < this.level.getWorldBorder().getMinZ())
            && !(area.maxZ > this.level.getWorldBorder().getMaxZ())) {
            for (int x = Mth.floor(area.minX) >> 4; x <= Mth.floor(area.maxX) >> 4; x++) {
               for (int z = Mth.floor(area.minZ) >> 4; z <= Mth.floor(area.maxZ) >> 4; z++) {
                  int cx = x;
                  int cz = z;
                  if (!this.chunkLoaded
                     .computeIfAbsent(
                        (long)x << 32 ^ z & 4294967295L, key -> this.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null
                     )) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private ChunkAccess chunkAt(int cx, int cz) {
         long key = (long)cx << 32 ^ cz & 4294967295L;
         if (this.chunkCache.containsKey(key)) {
            return this.chunkCache.get(key);
         }

         ChunkAccess chunk = this.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
         this.chunkCache.put(key, chunk);
         return chunk;
      }

      private BlockState blockStateIn(ChunkAccess chunk, int x, int y, int z) {
         if (y >= this.minY && y <= this.maxY) {
            int index = this.level.getSectionIndex(y);
            LevelChunkSection[] sections = chunk.getSections();
            if (index >= 0 && index < sections.length) {
               LevelChunkSection section = sections[index];
               return section != null && !section.hasOnlyAir() ? section.getBlockState(x & 15, y & 15, z & 15) : Blocks.AIR.defaultBlockState();
            } else {
               return Blocks.AIR.defaultBlockState();
            }
         } else {
            return Blocks.AIR.defaultBlockState();
         }
      }

      BlockState cachedBlockState(BlockPos at) {
         return this.blockStateCache.computeIfAbsent(at.asLong(), key -> this.level.getBlockState(BlockPos.of(key)));
      }

      @Override
      public Vec3 collide(AABB body, Vec3 motion) {
         return Entity.collideBoundingBox(this.target, motion, body, this.level, List.of());
      }

      @Override
      public double frictionUnder(AABB body) {
         this.cursor.set(body.getCenter().x, body.minY - 0.500001, body.getCenter().z);
         return this.cachedBlockState(this.cursor).getBlock().getFriction();
      }

      private static boolean isFallCatcher(BlockState state) {
         FluidState fluid = state.getFluidState();
         return !fluid.isEmpty() && !fluid.is(FluidTags.LAVA)
            || state.is(Blocks.COBWEB)
            || state.is(Blocks.POWDER_SNOW)
            || state.is(Blocks.SLIME_BLOCK)
            || state.is(Blocks.HONEY_BLOCK)
            || state.is(BlockTags.CLIMBABLE);
      }

      private static boolean isLava(BlockState state) {
         return state.getFluidState().is(FluidTags.LAVA);
      }

      private boolean anyBlockMatches(AABB body, Predicate<BlockState> test) {
         AABB box = body.deflate(0.001);

         for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               for (int y = Mth.floor(box.minY - 0.02); y <= Mth.floor(box.maxY); y++) {
                  this.cursor.set(x, y, z);
                  if (test.test(this.cachedBlockState(this.cursor))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      @Override
      public boolean touchesFallCatcher(AABB body) {
         return this.anyBlockMatches(body, KBDisplacementModule.TerrainSampler::isFallCatcher);
      }

      @Override
      public boolean touchesLava(AABB body) {
         return this.anyBlockMatches(body, KBDisplacementModule.TerrainSampler::isLava);
      }

      private static long heightMask(int base, double lo, double hi) {
         return TerrainDropGrid.sliceMask(base, lo, hi);
      }

      @Override
      public TerrainDropGrid.Column column(int x, int z, int base, double stepTop) {
         WorldBorder border = this.level.getWorldBorder();
         if (!(x < border.getMinX()) && !(x + 1 > border.getMaxX()) && !(z < border.getMinZ()) && !(z + 1 > border.getMaxZ())) {
            ChunkAccess chunk = this.chunkAt(x >> 4, z >> 4);
            if (chunk == null) {
               return null;
            }

            long walls = 0L;
            long caught = 0L;

            for (int y = base; y < base + 8; y++) {
               BlockState state = this.blockStateIn(chunk, x, y, z);
               if (!state.isAir()) {
                  if (isFallCatcher(state)) {
                     caught |= heightMask(base, y, y + 1);
                  }

                  this.cursor.set(x, y, z);
                  VoxelShape shape = state.getCollisionShape(this.level, this.cursor, this.collisionContext);
                  if (!shape.isEmpty()) {
                     if (shape == Shapes.block()) {
                        walls |= heightMask(base, y, y + 1);
                     } else {
                        for (AABB box : shape.toAabbs()) {
                           walls |= heightMask(base, y + box.minY, y + box.maxY);
                        }
                     }
                  }
               }
            }

            KBDisplacementModule.TerrainSampler.SurfaceHit hit = this.surfaceHit(chunk, x, z, stepTop, 0.0, 1.0, 0.0, 1.0);
            return new TerrainDropGrid.Column(walls, caught, hit.surfaceKind == 1 ? hit.top : -1.0 / 0.0, hit.surfaceKind, hit.friction);
         } else {
            return null;
         }
      }

      private KBDisplacementModule.TerrainSampler.SurfaceHit surfaceHit(ChunkAccess chunk, int x, int z, double startTop, double fx0, double fx1, double fz0, double fz1) {
         int y = Math.min(this.maxY, Mth.floor(startTop - 0.001));
         LevelChunkSection[] sections = chunk.getSections();

         while (y >= this.minY) {
            int index = this.level.getSectionIndex(y);
            if (index < 0) {
               break;
            }

            if (index >= sections.length) {
               y = this.minY + sections.length * 16 - 1;
            } else {
               LevelChunkSection section = sections[index];
               if (section != null && !section.hasOnlyAir()) {
                  BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                  if (!state.isAir()) {
                     if (isFallCatcher(state)) {
                        return new KBDisplacementModule.TerrainSampler.SurfaceHit(y, y + 1, (byte)3, 0.6F);
                     }

                     double top = this.shapeTopBelow(state, x, y, z, startTop, fx0, fx1, fz0, fz1);
                     if (top > -1.0 / 0.0) {
                        float friction = state.getBlock().getFriction();
                        if (y - 1 >= this.minY) {
                           BlockState under = this.blockStateIn(chunk, x, y - 1, z);
                           double higher = under.isAir() ? -1.0 / 0.0 : this.shapeTopBelow(under, x, y - 1, z, startTop, fx0, fx1, fz0, fz1);
                           if (higher > top) {
                              top = higher;
                              friction = under.getBlock().getFriction();
                           }
                        }

                        return new KBDisplacementModule.TerrainSampler.SurfaceHit(y, top, (byte)1, friction);
                     }

                     if (isLava(state)) {
                        return new KBDisplacementModule.TerrainSampler.SurfaceHit(y, y + 1, (byte)4, 0.6F);
                     }
                  }

                  y--;
               } else {
                  y = (y & -16) - 1;
               }
            }
         }

         return new KBDisplacementModule.TerrainSampler.SurfaceHit(this.minY - 1, -1.0 / 0.0, (byte)2, 0.6F);
      }

      private double shapeTopBelow(BlockState state, int x, int y, int z, double startTop, double fx0, double fx1, double fz0, double fz1) {
         this.cursor.set(x, y, z);
         VoxelShape shape = state.getCollisionShape(this.level, this.cursor, this.collisionContext);
         if (shape.isEmpty()) {
            return -1.0 / 0.0;
         }

         double best = -1.0 / 0.0;

         for (AABB box : shape == Shapes.block() ? List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)) : shape.toAabbs()) {
            if (box.maxX > fx0 && box.minX < fx1 && box.maxZ > fz0 && box.minZ < fz1 && y + box.maxY <= startTop + 0.01) {
               best = Math.max(best, y + box.maxY);
            }
         }

         return best;
      }

      @Override
      public KnockbackThrowPlanner.FloorProbe probeFloor(AABB body) {
         if (!this.regionLoaded(body)) {
            return new KnockbackThrowPlanner.FloorProbe(0.0, false, false, false);
         }

         int x0 = Mth.floor(body.minX + 0.001);
         int x1 = Mth.floor(body.maxX - 0.001);
         int z0 = Mth.floor(body.minZ + 0.001);
         int z1 = Mth.floor(body.maxZ - 0.001);
         double floorTop = -1.0 / 0.0;
         double catchTop = -1.0 / 0.0;
         double lavaTop = -1.0 / 0.0;

         for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
               ChunkAccess chunk = this.chunkAt(x >> 4, z >> 4);
               if (chunk == null) {
                  return new KnockbackThrowPlanner.FloorProbe(0.0, false, false, false);
               }

               KBDisplacementModule.TerrainSampler.SurfaceHit hit = this.surfaceHit(
                  chunk,
                  x,
                  z,
                  body.minY,
                  Math.max(0.0, body.minX + 0.001 - x),
                  Math.min(1.0, body.maxX - 0.001 - x),
                  Math.max(0.0, body.minZ + 0.001 - z),
                  Math.min(1.0, body.maxZ - 0.001 - z)
               );
               if (hit.surfaceKind == 1) {
                  floorTop = Math.max(floorTop, hit.top);
               } else if (hit.surfaceKind == 3) {
                  catchTop = Math.max(catchTop, hit.top);
               } else if (hit.top > -1.0 / 0.0) {
                  lavaTop = Math.max(lavaTop, hit.top);
               }
            }
         }

         if (catchTop > -1.0 / 0.0 && catchTop >= floorTop - 1.0E-9 && catchTop >= lavaTop - 1.0E-9) {
            return new KnockbackThrowPlanner.FloorProbe(catchTop - 1.0, false, true, true);
         } else if (floorTop > -1.0 / 0.0 && floorTop >= lavaTop - 1.0E-9) {
            return new KnockbackThrowPlanner.FloorProbe(floorTop, false, false, true);
         } else {
            return lavaTop > -1.0 / 0.0
               ? new KnockbackThrowPlanner.FloorProbe(lavaTop, false, false, true, true)
               : new KnockbackThrowPlanner.FloorProbe(this.level.getMinY(), true, false, true);
         }
      }

      double floorReference(AABB body) {
         KnockbackThrowPlanner.FloorProbe below = this.probeFloor(body);
         return below.known() && !below.voidColumn() && !below.caught() && !below.lava() && below.floorY() >= body.minY - 3.0 && below.floorY() <= body.minY + 1.0E-6
            ? below.floorY()
            : body.minY;
      }

      private record SurfaceHit(int y, double top, byte surfaceKind, float friction) {
      }
   }

   private enum PushSide {
      Auto,
      Left,
      Right;
   }

   private record DisplacementResult(AABB body, boolean grounded, int extraAir, Vec3 horizontalShift) {
   }
}
