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
import autismclient.util.cd;
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
   public static final String init_Str = "backtrack";
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final long init_J = 350000000L;
   private static final long load_J = 100000000L;
   private static final double init_D = 0.65;
   private static volatile BacktrackModule init_TaskTimer;
   private final AtomicLong init_AtomicLong = new AtomicLong();
   private long read_J;
   private final Map<Packet<?>, BacktrackModule.Inner_DataUtil> init_Map = new IdentityHashMap<>();
   private final BacktrackPacketQueue<Packet<?>> init_cN = new BacktrackPacketQueue<>(128);
   private final BacktrackPositionTracker init_cO = new BacktrackPositionTracker();
   private final BacktrackTrailHistory init_cP = new BacktrackTrailHistory();
   private ClientLevel init_ClientLevel;
   private LocalPlayer init_LocalPlayer;
   private ClientPacketListener init_ClientPacketListen;
   private LivingEntity init_LivingEntity;
   private LivingEntity load_LivingEntity;
   private LivingEntity read_LivingEntity;
   private long write;
   private long update;
   private Vec3 init_Vec3;
   private long sync;
   private long flush;
   private long close;
   private long open;
   private final ArrayDeque<Long> init_ArrayDeque = new ArrayDeque<>();
   private long poll;
   private boolean init_Z;
   private long push;
   private long pop;
   private long tick;
   private long run;
   private long get;
   private long set;
   private double load_D;
   private String load_Str = "idle";
   private String read_Str = "idle";

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
      long ticket = this.init_AtomicLong.incrementAndGet();
      Runnable enable = () -> {
         if (this.init_AtomicLong.get() == ticket) {
            this.load(true);
            this.push = this.pop = this.tick = this.run = this.get = this.set = 0L;
            this.load_D = 0.0;
            this.init_ClientLevel = init_Minecraft.level;
            this.init_LocalPlayer = init_Minecraft.player;
            this.init_ClientPacketListen = init_Minecraft.getConnection();
            init_TaskTimer = this;
         }
      };
      if (init_Minecraft.isSameThread()) {
         enable.run();
      } else {
         init_Minecraft.execute(enable);
      }
   }

   @Override
   public void onDisable() {
      long ticket = this.init_AtomicLong.incrementAndGet();
      init_TaskTimer = null;
      Runnable disable = () -> {
         if (this.init_AtomicLong.get() == ticket) {
            this.load(true);
         }
      };
      if (init_Minecraft.isSameThread()) {
         disable.run();
      } else {
         init_Minecraft.execute(disable);
      }
   }

   @Override
   public void onGameLeft() {
      this.load(false);
   }

   @Override
   protected void onOptionValueChanged(String id) {
      if (init_Minecraft.isSameThread()) {
         switch (id) {
            case "server-position":
            case "render-style":
            case "delayed-position":
            case "position-trail":
            case "render-smoothing":
            case "position-color":
            case "delayed-color":
               this.init_cP.init();
               break;
            default:
               this.load(true);
         }
      }
   }

   @Override
   public String info() {
      return this.integer("hold-time") + "ms" + (this.init_cN.init_I() > 0 ? " · " + this.init_cN.init_I() : "");
   }

   private boolean init_Z_21() {
      return this.init_ClientLevel != null
         && init_Minecraft.level == this.init_ClientLevel
         && init_Minecraft.player == this.init_LocalPlayer
         && init_Minecraft.getConnection() == this.init_ClientPacketListen;
   }

   private boolean init_Z_LivingEntity(LivingEntity candidate) {
      return candidate instanceof Player
         && candidate != init_Minecraft.player
         && candidate.isAlive()
         && !candidate.isSpectator()
         && init_Minecraft.level.getEntity(candidate.getId()) == candidate
         && !candidate.isPassenger()
         && !AntiBotTracker.load_Z(candidate)
         && !TeamsModule.init(candidate, "killaura");
   }

   private boolean load_Z_10() {
      return init_Minecraft.player != null
         && init_Minecraft.level != null
         && init_Minecraft.getConnection() != null
         && !init_Minecraft.player.isDeadOrDying()
         && !init_Minecraft.player.isSpectator()
         && init_Minecraft.gui.screen() == null
         && init_Minecraft.gui.overlay() == null
         && !init_Minecraft.player.isUsingItem()
         && !aD.init_Z()
         && !FreecamController.init_Z()
         && !cd.init_Z()
         && BlinkPacketManager.init_I() == 0;
   }

   @Override
   public boolean onPacketSend(Packet<?> packet) {
      if (init_Minecraft.isSameThread()
         && packet instanceof ServerboundAttackPacket attack
         && this.load_Z_10()
         && init_Minecraft.level.getEntity(attack.entityId()) instanceof LivingEntity victim
         && this.init_Z_LivingEntity(victim)) {
         synchronized (this.init_Map) {
            if (this.init_Map.size() >= 16) {
               this.init_Map.clear();
            }

            this.init_Map
               .put(
                  packet,
                  new BacktrackModule.Inner_DataUtil(
                     init_Minecraft.level, init_Minecraft.player, init_Minecraft.getConnection(), victim, System.nanoTime(), this.read_J
                  )
               );
         }
      }

      return false;
   }

   public static void init_V_Connection_Packet(Connection connection, Packet<?> packet) {
      BacktrackModule module = init_TaskTimer;
      if (module != null && packet instanceof ServerboundAttackPacket) {
         BacktrackModule.Inner_DataUtil arm;
         synchronized (module.init_Map) {
            arm = module.init_Map.remove(packet);
         }

         if (arm != null) {
            long at = System.nanoTime();
            init_Minecraft.execute(
               () -> {
                  if (init_TaskTimer == module
                     && module.isEnabled()
                     && init_Minecraft.level == arm.init_ClientLevel()
                     && init_Minecraft.player == arm.init_LocalPlayer()
                     && module.read_J == arm.load()
                     && init_Minecraft.getConnection() == arm.init_ClientPacketListen()
                     && arm.init_ClientPacketListen().getConnection() == connection
                     && at - arm.init_J() <= 500000000L
                     && module.init_Z_LivingEntity(arm.init_LivingEntity())) {
                     if (module.init_ClientLevel != null && !module.init_Z_21()) {
                        module.load(false);
                     }

                     module.close = module.load_LivingEntity == arm.init_LivingEntity() && module.write != 0L ? at - module.write : 0L;
                     if (module.load_LivingEntity != arm.init_LivingEntity()) {
                        module.init_ArrayDeque.clear();
                     }

                     if (module.init_ArrayDeque.size() == 12) {
                        module.init_ArrayDeque.removeFirst();
                     }

                     module.init_ArrayDeque.addLast(at);
                     module.load_LivingEntity = arm.init_LivingEntity();
                     module.write = at;
                     module.get++;
                     if (module.load_Z_10()) {
                        module.init_V_19();
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
         if (!this.init_Z_21() && this.init_ClientLevel != null) {
            this.load(false);
         }

         if (!this.load_Z_10()) {
            this.load(true);
         } else {
            this.init_V_19();
            if (this.init_LivingEntity != null) {
               long now = System.nanoTime();
               if (this.init_Z_J_2(now)
                  && this.init_Z_AABB_3(this.init_LivingEntity.getBoundingBox(), this.init_cO.init(), this.init_LocalPlayer.getEyePosition(), now)) {
                  this.init_V_Z_4(false);
               } else {
                  this.init_V_Z_4(true);
               }
            }
         }
      }
   }

   private void init_V_19() {
      if (this.init_ClientLevel == null) {
         this.init_ClientLevel = init_Minecraft.level;
         this.init_LocalPlayer = init_Minecraft.player;
         this.init_ClientPacketListen = init_Minecraft.getConnection();
      }

      LivingEntity selected = this.init_LivingEntity_2();
      if (selected != this.init_LivingEntity) {
         this.init_V_Z_4(true);
         this.init_cP.init();
         this.init_Vec3 = null;
         this.init_LivingEntity = selected;
         if (this.init_LivingEntity != null) {
            InterpolationHandler interpolation = this.init_LivingEntity.getInterpolation();
            Vec3 received = interpolation != null && interpolation.hasActiveInterpolation()
               ? Compat263.interpPos(interpolation)
               : this.init_LivingEntity.position();
            this.init_cO.init(this.init_LivingEntity.getPositionCodec().getBase(), received);
            this.init_cN.init_V(this.init_ClientLevel, this.init_LocalPlayer, this.init_ClientPacketListen, this.init_LivingEntity);
            this.init_V_AABB(this.init_LivingEntity.getBoundingBox(), received, this.init_LocalPlayer.getEyePosition(), System.nanoTime());
         }
      }
   }

   private LivingEntity init_LivingEntity_2() {
      this.load_Str = this.read_Str = "idle";
      if ("Hit".equals(this.choice("target"))) {
         return this.init_LivingEntity_LivingEntity_Str(this.load_LivingEntity_2(), "hit");
      }

      if ("Combat".equals(this.choice("target"))) {
         String owner = autismclient.util.SilentRotationManager.init_Str();

         for (String id : new String[]{owner == null ? "" : owner, "anchor-aura", "crystal-aura", "kill-aura"}) {
            Module module = al.init_Module_Str(id);
            if (module != null && module.isEnabled()) {
               LivingEntity enemy = module instanceof KillAuraModule aura
                  ? aura.load_LivingEntity_3()
                  : (module instanceof CrystalAuraModule aura ? aura.load_LivingEntity_4() : (module instanceof AnchorAuraModule aura ? aura.load_LivingEntity() : null));
               if (enemy != null && this.init_Z_LivingEntity(enemy)) {
                  return this.init_LivingEntity_LivingEntity_Str(enemy, id);
               }
            }
         }

         return this.init_LivingEntity_LivingEntity_Str(this.load_LivingEntity_2(), "hit");
      } else {
         LivingEntity best = null;
         double distance = this.init_D_5();
         double bestSquared = distance * distance;

         for (Player enemy : init_Minecraft.level.players()) {
            if (this.init_Z_LivingEntity(enemy) && (!this.bool("smart-timing") || this.load(enemy))) {
               double squared = init_D_AABB_Vec3_2(enemy.getBoundingBox(), init_Minecraft.player.getEyePosition());
               if (squared < bestSquared) {
                  best = enemy;
                  bestSquared = squared;
               }
            }
         }

         return this.init_LivingEntity_LivingEntity_Str(best, "nearest");
      }
   }

   private LivingEntity init_LivingEntity_LivingEntity_Str(LivingEntity candidate, String reason) {
      if (candidate != null) {
         this.load_Str = this.read_Str = reason;
      }

      return candidate;
   }

   private boolean init_Z_J_2(long now) {
      this.read_Str = this.load_Str;
      if (this.init_Z_LivingEntity(this.init_LivingEntity) && this.init_LivingEntity.hurtTime <= this.integer("hurt-time") && this.integer("hold-time") > 0) {
         if ((this.open == 0L || now - this.open >= 50000000L) && (!this.bool("pause-on-hurt") || this.init_LocalPlayer.hurtTime <= 0)) {
            if (this.bool("smart-timing") && !"crystal-aura".equals(this.load_Str) && !"anchor-aura".equals(this.load_Str)) {
               boolean auraIntent = "kill-aura".equals(this.load_Str);
               if (!auraIntent && !this.load(this.init_LivingEntity)) {
                  this.read_Str = "off-aim";
                  return false;
               }

               int ping = this.init_I_13();
               int hold = this.integer("hold-time");
               long sinceHit = now - this.write;
               if (this.init_LivingEntity != this.load_LivingEntity || !init_Z_J(sinceHit, hold, ping) && !init_Z_J_2(sinceHit, this.close, hold)) {
                  int lookahead = hold + Math.min(100, ping / 2);
                  if (auraIntent && al.init_Module_Str("kill-aura") instanceof KillAuraModule aura) {
                     if (aura.load_LivingEntity_3() == this.init_LivingEntity && aura.init_Z_I_7(lookahead)) {
                        return true;
                     }
                  } else if (autismclient.util.LegacyCombatDetector.init(this.init_LocalPlayer)
                     || init_Z_D_I(
                        (1.0F - this.init_LocalPlayer.getAttackStrengthScale(0.0F)) * this.init_LocalPlayer.getCurrentItemAttackStrengthDelay(), lookahead
                     )) {
                     return true;
                  }

                  this.read_Str = "waiting-charge";
                  return false;
               } else {
                  return true;
               }
            } else {
               return true;
            }
         } else {
            this.read_Str = "hurt-pause";
            return false;
         }
      } else {
         return false;
      }
   }

   private double init_D_5() {
      return Math.min(this.decimal("max-distance"), this.init_LocalPlayer.entityInteractionRange());
   }

   public static boolean init_Z_Player_Entity(Player attacker, Entity victim) {
      BacktrackModule module = init_TaskTimer;
      if (module != null
         && attacker == init_Minecraft.player
         && module.init_LivingEntity == victim
         && module.init_Z_21()
         && module.load_Z_10()
         && module.integer("hold-time") > 0
         && autismclient.util.LegacyCombatDetector.init(attacker)) {
         if (module.init_Z_LivingEntity_J(module.init_LivingEntity, System.nanoTime())) {
            return false;
         }

         module.set++;
         return true;
      } else {
         return false;
      }
   }

   private boolean init_Z_LivingEntity_J(LivingEntity victim, long now) {
      ServerRotationTracker.Inner_CachePool wire = ServerRotationTracker.init_ch_CachePool();
      if (wire.init_Z() && wire.init_Vec3() != null) {
         Vec3 eye = wire.init_Vec3().add(0.0, this.init_LocalPlayer.getEyeHeight(), 0.0);
         Vec3 look = Vec3.directionFromRotation(wire.write(), wire.read());
         double reach = this.init_LocalPlayer.entityInteractionRange();
         if (reach <= 0.0) {
            return false;
         } else {
            AABB drawn = victim.getBoundingBox();
            Vec3 current = victim.position();
            AABB latest = drawn.move(this.init_cO.init().subtract(current));
            if (init_Z_AABB_2(latest, eye, look, reach - 0.035)) {
               return true;
            } else if (this.init_cN.init_I() != 0 && this.read(now)) {
               AABB reachable = drawn.move(this.init_Vec3.subtract(current));
               return init_Z_AABB_2(drawn, eye, look, reach - 0.035)
                  && init_Z_AABB_2(reachable, eye, look, reach - 0.06)
                  && init_Z_AABB_2(latest, eye, look, reach + 0.65);
            } else {
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private static boolean init_Z_AABB_2(AABB box, Vec3 from, Vec3 look, double reach) {
      return reach > 0.0 && (box.contains(from) || box.clip(from, from.add(look.scale(reach))).isPresent());
   }

   private int init_I_13() {
      PlayerInfo entry = this.init_ClientPacketListen.getPlayerInfo(this.init_LocalPlayer.getUUID());
      return entry == null ? 0 : Math.clamp(entry.getLatency(), 0, 300);
   }

   public static boolean init_Z_J(long age, int holdMillis, int pingMillis) {
      int horizon = Math.clamp(pingMillis, 200, 300) + 100;
      return holdMillis > 0 && age >= 0L && age <= horizon * 1000000L;
   }

   private boolean load(LivingEntity candidate) {
      Vec3 eye = this.init_LocalPlayer.getEyePosition();
      double reach = this.init_D_5();
      return candidate.getBoundingBox().inflate(0.1).clip(eye, eye.add(this.init_LocalPlayer.getViewVector(1.0F).scale(reach))).isPresent()
         && this.init_LocalPlayer.hasLineOfSight(candidate);
   }

   public static boolean init_Z_D_I(double ticksUntilAttack, int holdMillis) {
      return Double.isFinite(ticksUntilAttack) && ticksUntilAttack <= Math.max(0, Math.min(300, holdMillis)) / 50.0 + 1.0;
   }

   public static boolean init_Z_J_2(long age, long interval, int holdMillis) {
      long window = (Math.max(0, Math.min(300, holdMillis)) + 50L) * 1000000L;
      return age >= 0L && interval > 0L && interval <= window && age <= Math.min(window, interval + 50000000L);
   }

   private LivingEntity load_LivingEntity_2() {
      if (this.load_LivingEntity != null && this.init_Z_LivingEntity(this.load_LivingEntity)) {
         long age = System.nanoTime() - this.write;
         return age > this.integer("attack-window") * 1000000L && !init_Z_J(age, this.integer("hold-time"), this.init_I_13()) ? null : this.load_LivingEntity;
      } else {
         return null;
      }
   }

   public static boolean init_Z_ClientPacketListen_Packet(ClientPacketListener source, Packet<?> packet) {
      BacktrackModule module = init_TaskTimer;
      if (module == null || module.init_Z || !init_Minecraft.isSameThread()) {
         return false;
      } else if (!module.isEnabled()) {
         module.load(true);
         return false;
      } else {
         return module.load(source, packet);
      }
   }

   private boolean load(ClientPacketListener source, Packet<?> packet) {
      if (this.init_ClientLevel == null && init_Minecraft.getConnection() == source && init_Minecraft.level != null && init_Minecraft.player != null) {
         this.init_ClientLevel = init_Minecraft.level;
         this.init_LocalPlayer = init_Minecraft.player;
         this.init_ClientPacketListen = source;
      }

      if (!this.init_Z_21() || source != this.init_ClientPacketListen) {
         this.load(false);
         return false;
      }

      if (packet instanceof ClientboundRespawnPacket || packet instanceof ClientboundLoginPacket) {
         this.load(false);
         return false;
      }

      if (packet instanceof ClientboundRemoveEntitiesPacket removed) {
         if (this.init_LivingEntity != null && removed.entityIds().contains(this.init_LivingEntity.getId())) {
            this.load(false);
         }

         return false;
      } else if (!(packet instanceof ClientboundPlayerPositionPacket) && !(packet instanceof ClientboundSetPassengersPacket)) {
         if (packet instanceof ClientboundDamageEventPacket damage
            && damage.entityId() != this.init_LocalPlayer.getId()
            && this.init_ClientLevel.getEntity(damage.entityId()) instanceof LivingEntity victim
            && (victim == this.init_LivingEntity || victim == this.load_LivingEntity)) {
            DamageSource cause = damage.getSource(this.init_ClientLevel);
            if (cause.getEntity() != null) {
               this.poll = System.nanoTime();
            }

            if (cause.getDirectEntity() == this.init_LocalPlayer && cause.is(DamageTypes.PLAYER_ATTACK)) {
               this.init_V_LivingEntity_J(victim, this.poll);
            } else if (cause.getEntity() == null) {
               this.load(victim, System.nanoTime());
            }
         }

         if (packet instanceof ClientboundEntityEventPacket event
            && event.getEventId() == 2
            && event.getEntity(this.init_ClientLevel) instanceof LivingEntity victim) {
            this.load(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundAnimatePacket animation
            && animation.getAction() == 1
            && this.init_ClientLevel.getEntity(animation.getId()) instanceof LivingEntity victim) {
            this.load(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundHurtAnimationPacket animation && this.init_ClientLevel.getEntity(animation.id()) instanceof LivingEntity victim) {
            this.load(victim, System.nanoTime());
         }

         if (packet instanceof ClientboundExplodePacket explosion && explosion.playerKnockback().isPresent()
            || packet instanceof ClientboundDamageEventPacket damage && damage.entityId() == this.init_LocalPlayer.getId()
            || packet instanceof ClientboundSetHealthPacket health && health.getHealth() < this.init_LocalPlayer.getHealth()
            || packet instanceof ClientboundSetEntityMotionPacket motion && motion.id() == this.init_LocalPlayer.getId()) {
            this.open = System.nanoTime();
            this.init_Vec3 = null;
            this.init_V_Z_4(true);
            this.init_cP.init();
            return false;
         } else {
            if (!this.load_Z_10()) {
               this.load(true);
               return false;
            }

            if (this.init_LivingEntity == null) {
               return false;
            }

            if (!this.init_Z_LivingEntity(this.init_LivingEntity)) {
               this.load(true);
               return false;
            }

            if (packet instanceof ClientboundTeleportEntityPacket teleport) {
               if (teleport.id() == this.init_LivingEntity.getId()) {
                  this.load(true);
               }

               return false;
            } else {
               Vec3 before = this.init_cO.init();
               Vec3 next = before;
               boolean matches = false;
               boolean moved = false;
               if (packet instanceof ClientboundMoveEntityPacket move && move.getEntity(this.init_ClientLevel) == this.init_LivingEntity) {
                  matches = true;
                  if (move.hasPosition()) {
                     next = this.init_cO.init(Compat263.moveEntityXa(move), Compat263.moveEntityYa(move), Compat263.moveEntityZa(move));
                     moved = true;
                  }
               } else if (packet instanceof ClientboundEntityPositionSyncPacket sync && sync.id() == this.init_LivingEntity.getId()) {
                  moved = true;
                  matches = true;
                  next = Compat263.entityPosSyncValues(sync).position();
                  this.init_cO.init(next);
               } else if (packet instanceof ClientboundRotateHeadPacket head && head.getEntity(this.init_ClientLevel) == this.init_LivingEntity) {
                  matches = true;
               } else if (packet instanceof ClientboundSetEntityMotionPacket motion && motion.id() == this.init_LivingEntity.getId()) {
                  matches = true;
               }

               if (!matches) {
                  return false;
               }

               Vec3 eye = this.init_LocalPlayer.getEyePosition();
               AABB box = this.init_LivingEntity.getBoundingBox();
               long now = System.nanoTime();
               if (moved && !next.equals(before)) {
                  this.init_V_AABB(box, next, eye, now);
               }

               double nextRangeSq = init_D_AABB_Vec3_2(box.move(next.subtract(this.init_LivingEntity.position())), eye);
               double beforeRangeSq = init_D_AABB_Vec3_2(box.move(before.subtract(this.init_LivingEntity.position())), eye);
               boolean approaching = moved
                  && (this.load(now) ? Math.sqrt(nextRangeSq) + 0.08 < Math.sqrt(beforeRangeSq) : nextRangeSq + 0.0025 < beforeRangeSq);
               if (next.distanceToSqr(before) > 16.0 || approaching || !this.init_Z_J_2(now) || !this.init_Z_AABB_3(box, next, eye, now)) {
                  this.init_V_Z_4(true);
                  return false;
               }

               if (!moved && this.init_cN.init_I() == 0) {
                  return false;
               }

               if (!this.init_cN.init(packet, now, this.integer("hold-time") * 1000000L)) {
                  this.init_V_Z_4(true);
                  return false;
               }

               if (moved && this.bool("server-position")) {
                  this.init_cP.init(this.init_LivingEntity, box, box.move(next.subtract(this.init_LivingEntity.position())), next, now);
               }

               this.push++;
               return true;
            }
         }
      } else {
         this.load(true);
         return false;
      }
   }

   private boolean load(long now) {
      return this.init_LivingEntity != null && this.init_LivingEntity == this.read_LivingEntity && now - this.update <= 350000000L;
   }

   private void init_V_LivingEntity_J(LivingEntity victim, long at) {
      this.read_LivingEntity = victim;
      this.update = at;
      if (victim == this.init_LivingEntity) {
         this.init_V_AABB(victim.getBoundingBox(), this.init_cO.init(), this.init_LocalPlayer.getEyePosition(), at);
      }
   }

   private void load(LivingEntity victim, long now) {
      if (victim == this.load_LivingEntity && autismclient.util.LegacyCombatDetector.init(this.init_LocalPlayer) && (this.poll == 0L || now - this.poll > 75000000L)) {
         int ping = this.init_I_13();

         for (long attack : this.init_ArrayDeque) {
            if (init_Z_J_I(now - attack, ping)) {
               this.init_V_LivingEntity_J(victim, now);
               return;
            }
         }
      }
   }

   public static boolean init_Z_J_I(long ageNanos, int pingMillis) {
      int ping = Math.clamp(pingMillis, 0, 300);
      return ageNanos >= Math.max(0, ping - 100) * 1000000L && ageNanos <= Math.max(300, ping + 125) * 1000000L;
   }

   private boolean read(long now) {
      return this.load(now) && this.init_Vec3 != null && this.flush != 0L && now - this.flush <= 100000000L && now - this.sync <= 150000000L;
   }

   private void init_V_AABB(AABB box, Vec3 next, Vec3 eye, long now) {
      double reach = this.init_D_5();
      if (box.move(next.subtract(this.init_LivingEntity.position())).distanceToSqr(eye) <= reach * reach) {
         this.init_Vec3 = next;
         this.sync = now;
         this.flush = 0L;
      } else if (this.init_Vec3 != null && this.flush == 0L) {
         this.flush = now;
      }
   }

   private boolean init_Z_AABB_3(AABB box, Vec3 received, Vec3 eye, long now) {
      Vec3 current = this.init_LivingEntity.position();
      double min = this.decimal("min-distance");
      double reach = this.init_D_5();
      if (init(box, current, received, eye, min, reach)) {
         return true;
      }

      if (!this.load(now)) {
         return false;
      }

      AABB latest = box.move(received.subtract(current));
      return load(box, current, received, eye, min, reach)
         ? init(eye, box, latest, latest, reach, reach)
         : this.read(now) && init(box, current, this.init_Vec3, received, eye, min, reach);
   }

   private void init_V_Z_4(boolean all) {
      if (this.init_cN.init_I() != 0) {
         if (this.init_Z_21()
            && this.init_LivingEntity != null
            && this.init_ClientLevel.getEntity(this.init_LivingEntity.getId()) == this.init_LivingEntity
            && this.init_cN.init_Z(this.init_ClientLevel, this.init_LocalPlayer, this.init_ClientPacketListen, this.init_LivingEntity)) {
            long now = System.nanoTime();
            this.load_D = Math.max(this.load_D, this.init_cN.init(now) / 1000000.0);
            if (all) {
               this.run++;
            }

            this.init_Z = true;

            try {
               this.pop = this.pop + this.init_cN.init(now, all, packet -> init_V_Packet_ClientPacketListen(packet, this.init_ClientPacketListen));
            } finally {
               this.init_Z = false;
            }
         } else {
            this.tick = this.tick + this.init_cN.init_I();
            this.init_cN.init_V();
         }
      }
   }

   private static void init_V_Packet_ClientPacketListen(Packet<?> packet, ClientPacketListener listener) {
      ((Packet<ClientPacketListener>)packet).handle(listener);
   }

   private void load(boolean replay) {
      this.init_cP.init();
      if (replay && this.init_Z_21()) {
         this.init_V_Z_4(true);
      }

      this.tick = this.tick + this.init_cN.init_I();
      this.init_cN.init_V();
      synchronized (this.init_Map) {
         this.init_Map.clear();
      }

      this.init_ArrayDeque.clear();
      this.poll = 0L;
      this.init_LivingEntity = this.load_LivingEntity = this.read_LivingEntity = null;
      this.init_ClientLevel = null;
      this.init_LocalPlayer = null;
      this.init_ClientPacketListen = null;
      this.write = 0L;
      this.init_Vec3 = null;
      this.close = this.open = 0L;
      this.load_Str = this.read_Str = "idle";
      this.read_J++;
   }

   public static Vec3 init_Vec3_LivingEntity(LivingEntity entity) {
      BacktrackModule module = init_TaskTimer;
      return module != null && module.init_LivingEntity == entity && module.init_Z_21() && module.init_ClientLevel.getEntity(entity.getId()) == entity
         ? module.init_cO.init()
         : null;
   }

   static Vec3 init_Vec3_LivingEntity_Vec3(LivingEntity entity, Vec3 eyes) {
      BacktrackModule module = init_TaskTimer;
      if (module != null && module.init_LivingEntity == entity && module.init_cN.init_I() != 0 && module.init_Z_21()) {
         Vec3 received = module.init_cO.init();
         AABB drawn = entity.getBoundingBox();
         long now = System.nanoTime();
         if (!module.init_Z_AABB_3(drawn, received, eyes, now)) {
            return null;
         }

         double reach = module.init_D_5();
         return drawn.move(received.subtract(entity.position())).distanceToSqr(eyes) <= reach * reach ? received : module.init_Vec3;
      } else {
         return null;
      }
   }

   public static boolean init_Z_AABB_2(AABB space) {
      BacktrackModule module = init_TaskTimer;
      return module != null
            && module.init_LivingEntity != null
            && module.init_Z_21()
            && module.init_LivingEntity.isAlive()
            && module.init_ClientLevel.getEntity(module.init_LivingEntity.getId()) == module.init_LivingEntity
         ? space.intersects(module.init_LivingEntity.getBoundingBox().move(module.init_cO.init().subtract(module.init_LivingEntity.position())))
         : false;
   }

   public BacktrackModule.Inner_StreamUtil init_TaskTimer_StreamUt() {
      boolean live = this.init_LivingEntity != null && this.init_Z_21();
      long now = System.nanoTime();
      return new BacktrackModule.Inner_StreamUtil(
         this.init_LivingEntity == null ? -1 : this.init_LivingEntity.getId(),
         this.init_cN.init_I(),
         this.push,
         this.pop,
         this.tick,
         this.run,
         this.get,
         this.load_D,
         live ? this.init_cO.init() : null,
         this.read_J,
         this.read_Str,
         this.init_Z_21() ? this.init_I_13() : 0,
         live && this.bool("server-position") && (this.init_cN.init_I() > 0 || this.init_cP.init_Z_Obj_J(this.init_LivingEntity, now)),
         live && this.load(now),
         this.set
      );
   }

   public static List<EspHighlightBox> init_List_9() {
      BacktrackModule module = init_TaskTimer;
      if (module == null) {
         return List.of();
      } else if (module.init_LivingEntity != null && module.init_Z_21() && module.bool("server-position")) {
         float partial = init_Minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
         Vec3 received = module.init_cO.init();
         AABB body = module.init_LivingEntity.getBoundingBox();
         AABB drawn = body.move(module.init_LivingEntity.getPosition(partial).subtract(module.init_LivingEntity.position()));
         return module.init_cP
            .init(
               module.init_LivingEntity,
               drawn,
               body.move(received.subtract(module.init_LivingEntity.position())),
               received,
               System.nanoTime(),
               module.init_cN.init_I() > 0,
               module.choice("render-style"),
               module.bool("delayed-position"),
               module.bool("position-trail"),
               module.integer("render-smoothing"),
               am.init_I_Module(module, "position-color", -1070216718),
               am.init_I_Module(module, "delayed-color", -1191201708)
            );
      } else {
         module.init_cP.init();
         return List.of();
      }
   }

   public static boolean init(AABB drawn, Vec3 rendered, Vec3 server, Vec3 eye, double minimum, double maximum) {
      return load(drawn, rendered, server, eye, minimum, maximum) && init_Z_AABB(drawn, drawn.move(server.subtract(rendered)), eye, maximum);
   }

   private static boolean load(AABB drawn, Vec3 rendered, Vec3 server, Vec3 eye, double minimum, double maximum) {
      if (Double.isFinite(minimum)
         && Double.isFinite(maximum)
         && !(maximum <= 0.0)
         && !(minimum > maximum)
         && Double.isFinite(server.x)
         && Double.isFinite(server.y)
         && Double.isFinite(server.z)
         && !(server.distanceToSqr(rendered) > 4.0)) {
         double oldDistance = init_D_AABB_Vec3_2(drawn, eye);
         AABB received = drawn.move(server.subtract(rendered));
         double latestDistance = init_D_AABB_Vec3_2(received, eye);
         return oldDistance <= maximum * maximum
            && latestDistance <= maximum * maximum
            && oldDistance >= minimum * minimum
            && latestDistance >= minimum * minimum
            && latestDistance > oldDistance + 0.0025;
      } else {
         return false;
      }
   }

   public static boolean init(AABB drawn, Vec3 rendered, Vec3 reachable, Vec3 server, Vec3 eye, double minimum, double maximum) {
      double extended = maximum + 0.65;
      if (!load(drawn, rendered, server, eye, minimum, extended)) {
         return false;
      }

      AABB oldReceived = drawn.move(reachable.subtract(rendered));
      AABB received = drawn.move(server.subtract(rendered));
      double limit = maximum * maximum;
      return drawn.distanceToSqr(eye) <= limit
         && oldReceived.distanceToSqr(eye) <= limit
         && received.distanceToSqr(eye) > limit
         && Math.abs(server.y - reachable.y) <= 0.5
         && init(eye, drawn, oldReceived, received, maximum, extended);
   }

   private static boolean init(Vec3 eye, AABB drawn, AABB reachable, AABB latest, double reach, double latestReach) {
      AABB first = drawn.deflate(0.04);
      AABB second = reachable.deflate(0.04);
      AABB third = latest.deflate(0.04);
      Vec3 from = first.getCenter();
      Vec3 to = second.getCenter();

      for (int i = 0; i <= 4; i++) {
         Vec3 middle = from.lerp(to, i * 0.25);
         Vec3 upper = new Vec3(middle.x, Math.clamp(eye.y, first.minY, first.maxY), middle.z);
         if (init(first, second, third, eye, upper, reach, latestReach) || init(first, second, third, eye, middle, reach, latestReach)) {
            return true;
         }
      }

      return false;
   }

   private static boolean init(AABB a, AABB b, AABB c, Vec3 eye, Vec3 point, double reach, double latestReach) {
      return load(a, eye, point, reach) && load(b, eye, point, reach) && load(c, eye, point, latestReach);
   }

   private static boolean init_Z_AABB(AABB drawn, AABB received, Vec3 eye, double range) {
      Vec3 middle = drawn.getCenter();
      double inset = Math.min(0.1, Math.min(received.getXsize(), Math.min(received.getYsize(), received.getZsize())) * 0.25);
      AABB interior = received.deflate(inset);
      Vec3 upper = new Vec3(middle.x, Math.max(drawn.minY + inset, Math.min(drawn.maxY - inset, eye.y)), middle.z);
      return load(interior, eye, middle, range) && load(interior, eye, upper, range);
   }

   private static boolean load(AABB box, Vec3 eye, Vec3 point, double range) {
      if (box.contains(eye)) {
         return true;
      }

      Vec3 direction = point.subtract(eye).normalize();
      return box.clip(eye, eye.add(direction.scale(range))).isPresent();
   }

   private static double init_D_AABB_Vec3_2(AABB box, Vec3 eye) {
      double x = Math.max(Math.max(box.minX - eye.x, eye.x - box.maxX), 0.0);
      double y = Math.max(Math.max(box.minY - eye.y, eye.y - box.maxY), 0.0);
      double z = Math.max(Math.max(box.minZ - eye.z, eye.z - box.maxZ), 0.0);
      return x * x + y * y + z * z;
   }

   private record Inner_DataUtil(
      ClientLevel init_ClientLevel,
      LocalPlayer init_LocalPlayer,
      ClientPacketListener init_ClientPacketListen,
      LivingEntity init_LivingEntity,
      long init_J,
      long load
   ) {
   }

   public record Inner_StreamUtil(
      int init_I,
      int load_I,
      long init_J,
      long load_J,
      long read_J,
      long write,
      long update,
      double init_D,
      Vec3 init_Vec3,
      long sync,
      String init_Str,
      int read_I,
      boolean init_Z,
      boolean load_Z,
      long flush
   ) {
   }
}
