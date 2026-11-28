package autismclient.modules;

import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.ColorSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.IntSetting;
import autismclient.compat263.Compat263;
import autismclient.util.AutismLiteVariant;
import autismclient.util.BacktrackPacketQueue;
import autismclient.util.BacktrackPositionTracker;
import autismclient.util.BacktrackTrailHistory;
import autismclient.util.RemoteViewController;
import autismclient.util.ServerRotationTracker;
import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundDamageEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BacktrackModule extends Module {
   public static final String MODULE_ID = "backtrack";
   private static final Minecraft CLIENT = Minecraft.getInstance();
   private static final long HIT_CONFIRM_WINDOW_NANOS = 350000000L;
   private static final long OUT_OF_REACH_GRACE_NANOS = 100000000L;
   private static final double REACH_EXTENSION = 0.65;
   private static volatile BacktrackModule instance;
   private final AtomicLong resetTicket = new AtomicLong();
   private long epoch;
   private final Map<Packet<?>, BacktrackModule.ArmContext> armContexts = new IdentityHashMap<>();
   private final BacktrackPacketQueue<Packet<?>> packetQueue = new BacktrackPacketQueue<>(128);
   private final BacktrackPositionTracker positionTracker = new BacktrackPositionTracker();
   private final BacktrackTrailHistory trailHistory = new BacktrackTrailHistory();
   private ClientLevel level;
   private LocalPlayer player;
   private ClientPacketListener connection;
   private LivingEntity target;
   private LivingEntity hitTarget;
   private LivingEntity confirmedHitTarget;
   private long lastHitAt;
   private long confirmedHitAt;
   private Vec3 reachablePosition;
   private long reachableAt;
   private long outOfReachSince;
   private long hitInterval;
   private long lastSelfHurtAt;
   private final ArrayDeque<Long> recentHitTimes = new ArrayDeque<>();
   private long lastDamageEventAt;
   private boolean releasing;
   private long heldPackets;
   private long releasedPackets;
   private long discardedPackets;
   private long fullReleases;
   private long hits;
   private long set;
   private double maxHeldMillis;
   private String targetReason = "idle";
   private String holdStatus = "idle";

   public BacktrackModule() {
      super("backtrack", "Backtrack", ae.init_ae, "Briefly hold a retreating target's movement updates.");
      this.add(
         new ChoiceSetting("target", "Target", "Combat", "Combat", "Hit", "Nearest")
            .description("Combat follows the active aura or accepted hit; Hit follows attacks; Nearest selects a nearby enemy.")
            .build()
      );
      this.add(
         new IntSetting("hold-time", "Hold Time", 150, 0, 300, 10)
            .unit("ms")
            .description("Maximum added delay per target update. Smart Timing uses ping to find attack opportunities.")
            .build()
      );
      this.add(
         new BoolSetting("smart-timing", "Smart Timing", true)
            .description("Hold useful retreats during an attack response or when the next attack is near.")
            .build()
      );
      this.add(new IntSetting("attack-window", "After Attack", 1000, 100, 3000, 50).unit("ms").build());
      this.add(new DoubleSetting("min-distance", "Min Distance", 0.5, 0.0, 3.0, 0.1).unit("blocks").build());
      this.add(
         new DoubleSetting("max-distance", "Max Distance", 3.0, 0.5, 6.0, 0.1)
            .description("Also limited by the server-provided attack reach.")
            .unit("blocks")
            .build()
      );
      this.add(new IntSetting("hurt-time", "Max Target Hurt Time", 10, 0, 10, 1).build());
      this.add(
         new BoolSetting("pause-on-hurt", "Pause When Hurt", false)
            .description("Pause for the full hurt animation. Incoming knockback always releases the current hold.")
            .build()
      );
      this.add(new BoolSetting("server-position", "Show Server Position", true).build());
      this.add(
         new ChoiceSetting("render-style", "Render Style", "Ghost", "Ghost", "Box")
            .description("Cyan marks the received position. The last hold fades after the target catches up.")
            .visibleWhen(() -> this.bool("server-position"))
            .build()
      );
      this.add(new BoolSetting("delayed-position", "Show Delayed Body", true).visibleWhen(() -> this.bool("server-position")).build());
      this.add(new BoolSetting("position-trail", "Position Trail", true).visibleWhen(() -> this.bool("server-position")).build());
      this.add(
         new IntSetting("render-smoothing", "Ghost Smoothing", 60, 0, 200, 10)
            .unit("ms")
            .visibleWhen(() -> this.bool("server-position") && !"Box".equals(this.choice("render-style")))
            .build()
      );
      this.add(new ColorSetting("position-color", "Received Color", -1070216718).visibleWhen(() -> this.bool("server-position")).build());
      this.add(
         new ColorSetting("delayed-color", "Delayed Color", -1191201708)
            .visibleWhen(() -> this.bool("server-position") && this.bool("delayed-position"))
            .build()
      );
   }

   @Override
   public void onEnable() {
      long ticket = this.resetTicket.incrementAndGet();
      Runnable enable = () -> {
         if (this.resetTicket.get() == ticket) {
            this.resetQueue(true);
            this.heldPackets = this.releasedPackets = this.discardedPackets = this.fullReleases = this.hits = this.set = 0L;
            this.maxHeldMillis = 0.0;
            this.level = CLIENT.level;
            this.player = CLIENT.player;
            this.connection = CLIENT.getConnection();
            instance = this;
         }
      };
      if (CLIENT.isSameThread()) {
         enable.run();
      } else {
         CLIENT.execute(enable);
      }
   }

   @Override
   public void onDisable() {
      long ticket = this.resetTicket.incrementAndGet();
      instance = null;
      Runnable disable = () -> {
         if (this.resetTicket.get() == ticket) {
            this.resetQueue(true);
         }
      };
      if (CLIENT.isSameThread()) {
         disable.run();
      } else {
         CLIENT.execute(disable);
      }
   }

   @Override
   public void onGameLeft() {
      this.resetQueue(false);
   }

   @Override
   protected void onOptionValueChanged(String id) {
      if (CLIENT.isSameThread()) {
         switch (id) {
            case "server-position":
            case "render-style":
            case "delayed-position":
            case "position-trail":
            case "render-smoothing":
            case "position-color":
            case "delayed-color":
               this.trailHistory.clear();
               break;
            default:
               this.resetQueue(true);
         }
      }
   }

   @Override
   public String info() {
      return this.integer("hold-time") + "ms" + (this.packetQueue.size() > 0 ? " · " + this.packetQueue.size() : "");
   }

   private boolean isSameSession() {
      return this.level != null
         && CLIENT.level == this.level
         && CLIENT.player == this.player
         && CLIENT.getConnection() == this.connection;
   }

   private boolean isValidTarget(LivingEntity candidate) {
      return candidate instanceof Player
         && candidate != CLIENT.player
         && candidate.isAlive()
         && !candidate.isSpectator()
         && CLIENT.level.getEntity(candidate.getId()) == candidate
         && !candidate.isPassenger()
         && !AntiBotTracker.shouldIgnoreEntity(candidate)
         && !TeamsModule.init(candidate, "killaura");
   }

   private boolean canBacktrack() {
      return CLIENT.player != null
         && CLIENT.level != null
         && CLIENT.getConnection() != null
         && !CLIENT.player.isDeadOrDying()
         && !CLIENT.player.isSpectator()
         && CLIENT.gui.screen() == null
         && CLIENT.gui.overlay() == null
         && !CLIENT.player.isUsingItem()
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.isActive()
         && BlinkPacketManager.getQueuedPacketCount() == 0;
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (CLIENT.isSameThread()
         && packet instanceof ServerboundAttackPacket attack
         && this.canBacktrack()
         && CLIENT.level.getEntity(attack.entityId()) instanceof LivingEntity victim
         && this.isValidTarget(victim)) {
         synchronized (this.armContexts) {
            if (this.armContexts.size() >= 16) {
               this.armContexts.clear();
            }

            this.armContexts
               .put(
                  packet,
                  new BacktrackModule.ArmContext(
                     CLIENT.level, CLIENT.player, CLIENT.getConnection(), victim, System.nanoTime(), this.epoch
                  )
               );
         }
      }

      return false;
   }

   public static void init_V_Connection_Packet(Connection connection, Packet<?> packet) {
      BacktrackModule module = instance;
      if (module != null && packet instanceof ServerboundAttackPacket) {
         BacktrackModule.ArmContext arm;
         synchronized (module.armContexts) {
            arm = module.armContexts.remove(packet);
         }

         if (arm != null) {
            long at = System.nanoTime();
            CLIENT.execute(
               () -> {
                  if (instance == module
                     && module.isEnabled()
                     && CLIENT.level == arm.level()
                     && CLIENT.player == arm.player()
                     && module.epoch == arm.load()
                     && CLIENT.getConnection() == arm.connection()
                     && arm.connection().getConnection() == connection
                     && at - arm.armedAt() <= 500000000L
                     && module.isValidTarget(arm.victim())) {
                     if (module.level != null && !module.isSameSession()) {
                        module.resetQueue(false);
                     }

                     module.hitInterval = module.hitTarget == arm.victim() && module.lastHitAt != 0L ? at - module.lastHitAt : 0L;
                     if (module.hitTarget != arm.victim()) {
                        module.recentHitTimes.clear();
                     }

                     if (module.recentHitTimes.size() == 12) {
                        module.recentHitTimes.removeFirst();
                     }

                     module.recentHitTimes.addLast(at);
                     module.hitTarget = arm.victim();
                     module.lastHitAt = at;
                     module.hits++;
                     if (module.canBacktrack()) {
                        module.updateTarget();
                     }
                  }
               }
            );
         }
      }
   }

   @Override
   public void onPacketProcessFrame() {
      if (!AutismLiteVariant.enabled()) {
         if (!this.isSameSession() && this.level != null) {
            this.resetQueue(false);
         }

         if (!this.canBacktrack()) {
            this.resetQueue(true);
         } else {
            this.updateTarget();
            if (this.target != null) {
               long now = System.nanoTime();
               if (this.shouldHold(now)
                  && this.isUsefulHold(this.target.getBoundingBox(), this.positionTracker.position(), this.player.getEyePosition(), now)) {
                  this.releasePackets(false);
               } else {
                  this.releasePackets(true);
               }
            }
         }
      }
   }

   private void updateTarget() {
      if (this.level == null) {
         this.level = CLIENT.level;
         this.player = CLIENT.player;
         this.connection = CLIENT.getConnection();
      }

      LivingEntity selected = this.selectTarget();
      if (selected != this.target) {
         this.releasePackets(true);
         this.trailHistory.clear();
         this.reachablePosition = null;
         this.target = selected;
         if (this.target != null) {
            InterpolationHandler interpolation = this.target.getInterpolation();
            Vec3 received = interpolation != null && interpolation.hasActiveInterpolation()
               ? Compat263.interpPos(interpolation)
               : this.target.position();
            this.positionTracker.reset(this.target.getPositionCodec().getBase(), received);
            this.packetQueue.bind(this.level, this.player, this.connection, this.target);
            this.updateReachablePosition(this.target.getBoundingBox(), received, this.player.getEyePosition(), System.nanoTime());
         }
      }
   }

   private LivingEntity selectTarget() {
      this.targetReason = this.holdStatus = "idle";
      if ("Hit".equals(this.choice("target"))) {
         return this.withReason(this.recentHitTarget(), "hit");
      }

      if ("Combat".equals(this.choice("target"))) {
         String owner = autismclient.util.SilentRotationManager.getActiveOwnerId();

         for (String id : new String[]{owner == null ? "" : owner, "anchor-aura", "crystal-aura", "kill-aura"}) {
            Module module = al.init_Module_Str(id);
            if (module != null && module.isEnabled()) {
               LivingEntity enemy = module instanceof KillAuraModule aura
                  ? aura.activeTarget()
                  : (module instanceof CrystalAuraModule aura ? aura.currentTarget() : (module instanceof AnchorAuraModule aura ? aura.currentTarget() : null));
               if (enemy != null && this.isValidTarget(enemy)) {
                  return this.withReason(enemy, id);
               }
            }
         }

         return this.withReason(this.recentHitTarget(), "hit");
      } else {
         LivingEntity best = null;
         double distance = this.reach();
         double bestSquared = distance * distance;

         for (Player enemy : CLIENT.level.players()) {
            if (this.isValidTarget(enemy) && (!this.bool("smart-timing") || this.isAimedAt(enemy))) {
               double squared = distanceToBox(enemy.getBoundingBox(), CLIENT.player.getEyePosition());
               if (squared < bestSquared) {
                  best = enemy;
                  bestSquared = squared;
               }
            }
         }

         return this.withReason(best, "nearest");
      }
   }

   private LivingEntity withReason(LivingEntity candidate, String reason) {
      if (candidate != null) {
         this.targetReason = this.holdStatus = reason;
      }

      return candidate;
   }

   private boolean shouldHold(long now) {
      this.holdStatus = this.targetReason;
      if (this.isValidTarget(this.target) && this.target.hurtTime <= this.integer("hurt-time") && this.integer("hold-time") > 0) {
         if ((this.lastSelfHurtAt == 0L || now - this.lastSelfHurtAt >= 50000000L) && (!this.bool("pause-on-hurt") || this.player.hurtTime <= 0)) {
            if (this.bool("smart-timing") && !"crystal-aura".equals(this.targetReason) && !"anchor-aura".equals(this.targetReason)) {
               boolean auraIntent = "kill-aura".equals(this.targetReason);
               if (!auraIntent && !this.isAimedAt(this.target)) {
                  this.holdStatus = "off-aim";
                  return false;
               }

               int ping = this.pingMillis();
               int hold = this.integer("hold-time");
               long sinceHit = now - this.lastHitAt;
               if (this.target != this.hitTarget || !withinHoldHorizon(sinceHit, hold, ping) && !withinHitCadence(sinceHit, this.hitInterval, hold)) {
                  int lookahead = hold + Math.min(100, ping / 2);
                  if (auraIntent && al.init_Module_Str("kill-aura") instanceof KillAuraModule aura) {
                     if (aura.activeTarget() == this.target && aura.init_Z_I_7(lookahead)) {
                        return true;
                     }
                  } else if (autismclient.util.LegacyCombatDetector.isLegacyCombat(this.player)
                     || attackWithinHold(
                        (1.0F - this.player.getAttackStrengthScale(0.0F)) * this.player.getCurrentItemAttackStrengthDelay(), lookahead
                     )) {
                     return true;
                  }

                  this.holdStatus = "waiting-charge";
                  return false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            this.holdStatus = "hurt-pause";
            return false;
         }
      } else {
         return false;
      }
   }

   private double reach() {
      return Math.min(this.decimal("max-distance"), this.player.entityInteractionRange());
   }

   public static boolean init_Z_Player_Entity(Player attacker, Entity victim) {
      BacktrackModule module = instance;
      if (module != null
         && attacker == CLIENT.player
         && module.target == victim
         && module.isSameSession()
         && module.canBacktrack()
         && module.integer("hold-time") > 0
         && autismclient.util.LegacyCombatDetector.isLegacyCombat(attacker)) {
         if (module.canHitFromWire(module.target, System.nanoTime())) {
            return false;
         }

         module.set++;
         return true;
      } else {
         return false;
      }
   }

   private boolean canHitFromWire(LivingEntity victim, long now) {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
      if (wire.initialized() && wire.position() != null) {
         Vec3 eye = wire.position().add(0.0, this.player.getEyeHeight(), 0.0);
         Vec3 look = Vec3.directionFromRotation(wire.currentPitch(), wire.currentYaw());
         double reach = this.player.entityInteractionRange();
         if (reach <= 0.0) {
            return false;
         } else {
            AABB drawn = victim.getBoundingBox();
            Vec3 current = victim.position();
            AABB latest = drawn.move(this.positionTracker.position().subtract(current));
            if (rayHits(latest, eye, look, reach - 0.035)) {
               return true;
            } else if (this.packetQueue.size() != 0 && this.hasReachableServerPosition(now)) {
               AABB reachable = drawn.move(this.reachablePosition.subtract(current));
               return rayHits(drawn, eye, look, reach - 0.035)
                  && rayHits(reachable, eye, look, reach - 0.06)
                  && rayHits(latest, eye, look, reach + 0.65);
            } else {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private static boolean rayHits(AABB box, Vec3 from, Vec3 look, double reach) {
      return reach > 0.0 && (box.contains(from) || box.clip(from, from.add(look.scale(reach))).isPresent());
   }

   private int pingMillis() {
      PlayerInfo entry = this.connection.getPlayerInfo(this.player.getUUID());
      return entry == null ? 0 : Math.clamp(entry.getLatency(), 0, 300);
   }

   public static boolean withinHoldHorizon(long age, int holdMillis, int pingMillis) {
      int horizon = Math.clamp(pingMillis, 200, 300) + 100;
      return holdMillis > 0 && age >= 0L && age <= horizon * 1000000L;
   }

   private boolean isAimedAt(LivingEntity candidate) {
      Vec3 eye = this.player.getEyePosition();
      double reach = this.reach();
      return candidate.getBoundingBox().inflate(0.1).clip(eye, eye.add(this.player.getViewVector(1.0F).scale(reach))).isPresent()
         && this.player.hasLineOfSight(candidate);
   }

   public static boolean attackWithinHold(double ticksUntilAttack, int holdMillis) {
      return Double.isFinite(ticksUntilAttack) && ticksUntilAttack <= Math.max(0, Math.min(300, holdMillis)) / 50.0 + 1.0;
   }

   public static boolean withinHitCadence(long age, long interval, int holdMillis) {
      long window = (Math.max(0, Math.min(300, holdMillis)) + 50L) * 1000000L;
      return age >= 0L && interval > 0L && interval <= window && age <= Math.min(window, interval + 50000000L);
   }

   private LivingEntity recentHitTarget() {
      if (this.hitTarget != null && this.isValidTarget(this.hitTarget)) {
         long age = System.nanoTime() - this.lastHitAt;
         return age > this.integer("attack-window") * 1000000L && !withinHoldHorizon(age, this.integer("hold-time"), this.pingMillis()) ? null : this.hitTarget;
      } else {
         return null;
      }
   }

   public static boolean init_Z_ClientPacketListen_Packet(ClientPacketListener source, Packet<?> packet) {
      BacktrackModule module = instance;
      if (module == null || module.releasing || !CLIENT.isSameThread()) {
         return false;
      } else if (!module.isEnabled()) {
         module.resetQueue(true);
         return false;
      } else {
         return module.interceptPacket(source, packet);
      }
   }

   private boolean interceptPacket(ClientPacketListener source, Packet<?> packet) {
      if (this.level == null && CLIENT.getConnection() == source && CLIENT.level != null && CLIENT.player != null) {
         this.level = CLIENT.level;
         this.player = CLIENT.player;
         this.connection = source;
      }

      if (!this.isSameSession() || source != this.connection) {
         this.resetQueue(false);
         return false;
      }

      if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
         this.resetQueue(false);
         return false;
      }

      if (packet instanceof ClientboundRemoveEntitiesPacket removed) {
         if (this.target != null && removed.entityIds().contains(this.target.getId())) {
            this.resetQueue(false);
         }

         return false;
      } else if (!(packet instanceof ClientboundPlayerPositionPacket) && !(packet instanceof ClientboundSetPassengersPacket)) {
         if (packet instanceof ClientboundDamageEventPacket damage
            && damage.entityId() != this.player.getId()
            && this.level.getEntity(damage.entityId()) instanceof LivingEntity victim
            && (victim == this.target || victim == this.hitTarget)) {
            DamageSource cause = damage.getSource(this.level);
            if (cause.getEntity() != null) {
               this.lastDamageEventAt = System.nanoTime();
            }

            if (cause.getDirectEntity() == this.player && cause.is(DamageTypes.PLAYER_ATTACK)) {
               this.confirmHit(victim, this.lastDamageEventAt);
            } else if (cause.getEntity() == null) {
               this.onSourcelessDamage(victim, System.nanoTime());
            }
         }

         if (packet instanceof ClientboundEntityEventPacket event
            && event.getEventId() == 2
            && event.getEntity(this.level) instanceof LivingEntity victim) {
            this.onSourcelessDamage(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundAnimatePacket animation
            && animation.getAction() == 1
            && this.level.getEntity(animation.getId()) instanceof LivingEntity victim) {
            this.onSourcelessDamage(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundHurtAnimationPacket animation && this.level.getEntity(animation.id()) instanceof LivingEntity victim) {
            this.onSourcelessDamage(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundExplodePacket explosion && explosion.playerKnockback().isPresent()
            || packet instanceof ClientboundDamageEventPacket damage && damage.entityId() == this.player.getId()
            || packet instanceof ClientboundSetHealthPacket health && health.getHealth() < this.player.getHealth()
            || packet instanceof ClientboundSetEntityMotionPacket motion && motion.id() == this.player.getId()) {
            this.lastSelfHurtAt = System.nanoTime();
            this.reachablePosition = null;
            this.releasePackets(true);
            this.trailHistory.clear();
            return false;
         } else {
            if (!this.canBacktrack()) {
               this.resetQueue(true);
               return false;
            }

            if (this.target == null) {
               return false;
            }

            if (!this.isValidTarget(this.target)) {
               this.resetQueue(true);
               return false;
            }

            if (packet instanceof ClientboundTeleportEntityPacket teleport) {
               if (teleport.id() == this.target.getId()) {
                  this.resetQueue(true);
               }

               return false;
            } else {
               Vec3 before = this.positionTracker.position();
               Vec3 next = before;
               boolean matches = false;
               boolean moved = false;
               if (packet instanceof ClientboundMoveEntityPacket move && move.getEntity(this.level) == this.target) {
                  matches = true;
                  if (move.hasPosition()) {
                     next = this.positionTracker.decode(Compat263.moveEntityXa(move), Compat263.moveEntityYa(move), Compat263.moveEntityZa(move));
                     moved = true;
                  }
               } else if (packet instanceof ClientboundEntityPositionSyncPacket sync && sync.id() == this.target.getId()) {
                  moved = true;
                  matches = true;
                  next = Compat263.entityPosSyncValues(sync).position();
                  this.positionTracker.set(next);
               } else if (packet instanceof ClientboundRotateHeadPacket head && head.getEntity(this.level) == this.target) {
                  matches = true;
               } else if (packet instanceof ClientboundSetEntityMotionPacket motion && motion.id() == this.target.getId()) {
                  matches = true;
               }

               if (!matches) {
                  return false;
               }

               Vec3 eye = this.player.getEyePosition();
               AABB box = this.target.getBoundingBox();
               long now = System.nanoTime();
               if (moved && !next.equals(before)) {
                  this.updateReachablePosition(box, next, eye, now);
               }

               double nextRangeSq = distanceToBox(box.move(next.subtract(this.target.position())), eye);
               double beforeRangeSq = distanceToBox(box.move(before.subtract(this.target.position())), eye);
               boolean approaching = moved
                  && (this.hasRecentConfirmedHit(now) ? Math.sqrt(nextRangeSq) + 0.08 < Math.sqrt(beforeRangeSq) : nextRangeSq + 0.0025 < beforeRangeSq);
               if (next.distanceToSqr(before) > 16.0 || approaching || !this.shouldHold(now) || !this.isUsefulHold(box, next, eye, now)) {
                  this.releasePackets(true);
                  return false;
               }

               if (!moved && this.packetQueue.size() == 0) {
                  return false;
               }

               if (!this.packetQueue.offer(packet, now, this.integer("hold-time") * 1000000L)) {
                  this.releasePackets(true);
                  return false;
               }

               if (moved && this.bool("server-position")) {
                  this.trailHistory.add(this.target, box, box.move(next.subtract(this.target.position())), next, now);
               }

               this.heldPackets++;
               return true;
            }
         }
      } else {
         this.resetQueue(true);
         return false;
      }
   }

   private boolean hasRecentConfirmedHit(long now) {
      return this.target != null && this.target == this.confirmedHitTarget && now - this.confirmedHitAt <= 350000000L;
   }

   private void confirmHit(LivingEntity victim, long at) {
      this.confirmedHitTarget = victim;
      this.confirmedHitAt = at;
      if (victim == this.target) {
         this.updateReachablePosition(victim.getBoundingBox(), this.positionTracker.position(), this.player.getEyePosition(), at);
      }
   }

   private void onSourcelessDamage(LivingEntity victim, long now) {
      if (victim == this.hitTarget && autismclient.util.LegacyCombatDetector.isLegacyCombat(this.player) && (this.lastDamageEventAt == 0L || now - this.lastDamageEventAt > 75000000L)) {
         int ping = this.pingMillis();

         for (long attack : this.recentHitTimes) {
            if (isPlausibleHitDelay(now - attack, ping)) {
               this.confirmHit(victim, now);
               return;
            }
         }
      }
   }

   public static boolean isPlausibleHitDelay(long ageNanos, int pingMillis) {
      int ping = Math.clamp(pingMillis, 0, 300);
      return ageNanos >= Math.max(0, ping - 100) * 1000000L && ageNanos <= Math.max(300, ping + 125) * 1000000L;
   }

   private boolean hasReachableServerPosition(long now) {
      return this.hasRecentConfirmedHit(now) && this.reachablePosition != null && this.outOfReachSince != 0L && now - this.outOfReachSince <= 100000000L && now - this.reachableAt <= 150000000L;
   }

   private void updateReachablePosition(AABB box, Vec3 next, Vec3 eye, long now) {
      double reach = this.reach();
      if (box.move(next.subtract(this.target.position())).distanceToSqr(eye) <= reach * reach) {
         this.reachablePosition = next;
         this.reachableAt = now;
         this.outOfReachSince = 0L;
      } else if (this.reachablePosition != null && this.outOfReachSince == 0L) {
         this.outOfReachSince = now;
      }
   }

   private boolean isUsefulHold(AABB box, Vec3 received, Vec3 eye, long now) {
      Vec3 current = this.target.position();
      double min = this.decimal("min-distance");
      double reach = this.reach();
      if (isUsefulBacktrack(box, current, received, eye, min, reach)) {
         return true;
      }

      if (!this.hasRecentConfirmedHit(now)) {
         return false;
      }

      AABB latest = box.move(received.subtract(current));
      return serverPositionIsFarther(box, current, received, eye, min, reach)
         ? hasSharedHitPoint(eye, box, latest, latest, reach, reach)
         : this.hasReachableServerPosition(now) && isReachRescue(box, current, this.reachablePosition, received, eye, min, reach);
   }

   private void releasePackets(boolean all) {
      if (this.packetQueue.size() != 0) {
         if (this.isSameSession()
            && this.target != null
            && this.level.getEntity(this.target.getId()) == this.target
            && this.packetQueue.isBoundTo(this.level, this.player, this.connection, this.target)) {
            long now = System.nanoTime();
            this.maxHeldMillis = Math.max(this.maxHeldMillis, this.packetQueue.oldestAgeNanos(now) / 1000000.0);
            if (all) {
               this.fullReleases++;
            }

            this.releasing = true;

            try {
               this.releasedPackets = this.releasedPackets + this.packetQueue.release(now, all, packet -> handlePacket(packet, this.connection));
            } finally {
               this.releasing = false;
            }
         } else {
            this.discardedPackets = this.discardedPackets + this.packetQueue.size();
            this.packetQueue.clear();
         }
      }
   }

   private static void handlePacket(Packet<?> packet, ClientPacketListener listener) {
      ((Packet<ClientPacketListener>)packet).handle(listener);
   }

   private void resetQueue(boolean replay) {
      this.trailHistory.clear();
      if (replay && this.isSameSession()) {
         this.releasePackets(true);
      }

      this.discardedPackets = this.discardedPackets + this.packetQueue.size();
      this.packetQueue.clear();
      synchronized (this.armContexts) {
         this.armContexts.clear();
      }

      this.recentHitTimes.clear();
      this.lastDamageEventAt = 0L;
      this.target = this.hitTarget = this.confirmedHitTarget = null;
      this.level = null;
      this.player = null;
      this.connection = null;
      this.lastHitAt = 0L;
      this.reachablePosition = null;
      this.hitInterval = this.lastSelfHurtAt = 0L;
      this.targetReason = this.holdStatus = "idle";
      this.epoch++;
   }

   public static Vec3 backtrackedPosition(LivingEntity entity) {
      BacktrackModule module = instance;
      return module != null && module.target == entity && module.isSameSession() && module.level.getEntity(entity.getId()) == entity
         ? module.positionTracker.position()
         : null;
   }

   static Vec3 init_Vec3_LivingEntity_Vec3(LivingEntity entity, Vec3 eyes) {
      BacktrackModule module = instance;
      if (module != null && module.target == entity && module.packetQueue.size() != 0 && module.isSameSession()) {
         Vec3 received = module.positionTracker.position();
         AABB drawn = entity.getBoundingBox();
         long now = System.nanoTime();
         if (!module.isUsefulHold(drawn, received, eyes, now)) {
            return null;
         }

         double reach = module.reach();
         return drawn.move(received.subtract(entity.position())).distanceToSqr(eyes) <= reach * reach ? received : module.reachablePosition;
      } else {
         return null;
      }
   }

   public static boolean init_Z_AABB_2(AABB space) {
      BacktrackModule module = instance;
      return module != null
            && module.target != null
            && module.isSameSession()
            && module.target.isAlive()
            && module.level.getEntity(module.target.getId()) == module.target
         ? space.intersects(module.target.getBoundingBox().move(module.positionTracker.position().subtract(module.target.position())))
         : false;
   }

   public BacktrackModule.StatusSnapshot statusSnapshot() {
      boolean live = this.target != null && this.isSameSession();
      long now = System.nanoTime();
      return new BacktrackModule.StatusSnapshot(
         this.target == null ? -1 : this.target.getId(),
         this.packetQueue.size(),
         this.heldPackets,
         this.releasedPackets,
         this.discardedPackets,
         this.fullReleases,
         this.hits,
         this.maxHeldMillis,
         live ? this.positionTracker.position() : null,
         this.epoch,
         this.holdStatus,
         this.isSameSession() ? this.pingMillis() : 0,
         live && this.bool("server-position") && (this.packetQueue.size() > 0 || this.trailHistory.hasRecentTrail(this.target, now)),
         live && this.hasRecentConfirmedHit(now),
         this.set
      );
   }

   public static List<EspHighlightBox> highlightBoxes() {
      BacktrackModule module = instance;
      if (module == null) {
         return List.of();
      } else if (module.target != null && module.isSameSession() && module.bool("server-position")) {
         float partial = CLIENT.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         Vec3 received = module.positionTracker.position();
         AABB body = module.target.getBoundingBox();
         AABB drawn = body.move(module.target.getPosition(partial).subtract(module.target.position()));
         return module.trailHistory
            .boxes(
               module.target,
               drawn,
               body.move(received.subtract(module.target.position())),
               received,
               System.nanoTime(),
               module.packetQueue.size() > 0,
               module.choice("render-style"),
               module.bool("delayed-position"),
               module.bool("position-trail"),
               module.integer("render-smoothing"),
               am.init_I_Module(module, "position-color", -1070216718),
               am.init_I_Module(module, "delayed-color", -1191201708)
            );
      } else {
         module.trailHistory.clear();
         return List.of();
      }
   }

   public static boolean isUsefulBacktrack(AABB drawn, Vec3 rendered, Vec3 server, Vec3 eye, double minimum, double maximum) {
      return serverPositionIsFarther(drawn, rendered, server, eye, minimum, maximum) && rayHitsInset(drawn, drawn.move(server.subtract(rendered)), eye, maximum);
   }

   private static boolean serverPositionIsFarther(AABB drawn, Vec3 rendered, Vec3 server, Vec3 eye, double minimum, double maximum) {
      if (Double.isFinite(minimum)
         && Double.isFinite(maximum)
         && !(maximum <= 0.0)
         && !(minimum > maximum)
         && Double.isFinite(server.x)
         && Double.isFinite(server.y)
         && Double.isFinite(server.z)
         && !(server.distanceToSqr(rendered) > 4.0)) {
         double oldDistance = distanceToBox(drawn, eye);
         AABB received = drawn.move(server.subtract(rendered));
         double latestDistance = distanceToBox(received, eye);
         return oldDistance <= maximum * maximum
            && latestDistance <= maximum * maximum
            && oldDistance >= minimum * minimum
            && latestDistance >= minimum * minimum
            && latestDistance > oldDistance + 0.0025;
      } else {
         return false;
      }
   }

   public static boolean isReachRescue(AABB drawn, Vec3 rendered, Vec3 reachable, Vec3 server, Vec3 eye, double minimum, double maximum) {
      double extended = maximum + 0.65;
      if (!serverPositionIsFarther(drawn, rendered, server, eye, minimum, extended)) {
         return false;
      }

      AABB oldReceived = drawn.move(reachable.subtract(rendered));
      AABB received = drawn.move(server.subtract(rendered));
      double limit = maximum * maximum;
      return drawn.distanceToSqr(eye) <= limit
         && oldReceived.distanceToSqr(eye) <= limit
         && received.distanceToSqr(eye) > limit
         && Math.abs(server.y - reachable.y) <= 0.5
         && hasSharedHitPoint(eye, drawn, oldReceived, received, maximum, extended);
   }

   private static boolean hasSharedHitPoint(Vec3 eye, AABB drawn, AABB reachable, AABB latest, double reach, double latestReach) {
      AABB first = drawn.deflate(0.04);
      AABB second = reachable.deflate(0.04);
      AABB third = latest.deflate(0.04);
      Vec3 from = first.getCenter();
      Vec3 to = second.getCenter();

      for (int i = 0; i <= 4; i++) {
         Vec3 middle = from.lerp(to, i * 0.25);
         Vec3 upper = new Vec3(middle.x, Math.clamp(eye.y, first.minY, first.maxY), middle.z);
         if (pointHitsAll(first, second, third, eye, upper, reach, latestReach) || pointHitsAll(first, second, third, eye, middle, reach, latestReach)) {
            return true;
         }
      }

      return false;
   }

   private static boolean pointHitsAll(AABB a, AABB b, AABB c, Vec3 eye, Vec3 point, double reach, double latestReach) {
      return rayHitsBox(a, eye, point, reach) && rayHitsBox(b, eye, point, reach) && rayHitsBox(c, eye, point, latestReach);
   }

   private static boolean rayHitsInset(AABB drawn, AABB received, Vec3 eye, double range) {
      Vec3 middle = drawn.getCenter();
      double inset = Math.min(0.1, Math.min(received.getXsize(), Math.min(received.getYsize(), received.getZsize())) * 0.25);
      AABB interior = received.deflate(inset);
      Vec3 upper = new Vec3(middle.x, Math.max(drawn.minY + inset, Math.min(drawn.maxY - inset, eye.y)), middle.z);
      return rayHitsBox(interior, eye, middle, range) && rayHitsBox(interior, eye, upper, range);
   }

   private static boolean rayHitsBox(AABB box, Vec3 eye, Vec3 point, double range) {
      if (box.contains(eye)) {
         return true;
      }

      Vec3 direction = point.subtract(eye).normalize();
      return box.clip(eye, eye.add(direction.scale(range))).isPresent();
   }

   private static double distanceToBox(AABB box, Vec3 eye) {
      double x = Math.max(Math.max(box.minX - eye.x, eye.x - box.maxX), 0.0);
      double y = Math.max(Math.max(box.minY - eye.y, eye.y - box.maxY), 0.0);
      double z = Math.max(Math.max(box.minZ - eye.z, eye.z - box.maxZ), 0.0);
      return x * x + y * y + z * z;
   }

   private record ArmContext(
      ClientLevel level,
      LocalPlayer player,
      ClientPacketListener connection,
      LivingEntity victim,
      long armedAt,
      long load
   ) {
   }

   public record StatusSnapshot(
      int targetId,
      int queuedPackets,
      long heldPackets,
      long releasedPackets,
      long discardedPackets,
      long write,
      long update,
      double maxHeldMillis,
      Vec3 serverPosition,
      long sync,
      String status,
      int pingMillis,
      boolean showingTrail,
      boolean recentlyHit,
      long flush
   ) {
   }
}
