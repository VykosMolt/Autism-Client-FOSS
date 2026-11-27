package autismclient.modules;

import autismclient.api.module.ActionSetting;
import autismclient.api.module.BoolSetting;
import autismclient.api.module.ChoiceSetting;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.EnumSetting;
import autismclient.api.module.IntSetting;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismDangerArrowAccessor;
import autismclient.util.AutismConfig;
import autismclient.util.RemoteViewController;
import autismclient.util.RotationMath;
import autismclient.util.ServerRotationTracker;
import autismclient.util.SprintStateTracker;
import autismclient.util.AutismSessionState;
import autismclient.util.SilentMovementCoordinator;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder.Reference;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

public final class DangerModule extends Module implements SilentMovementCoordinator.Participant {
   private static final int init_I = 40;
   private static final int load_I = 12;
   private static final double init_D = 64.0;
   private static final double load_D = 3.5;
   private static final int read_I = 8;
   private static final int write_I = 14;
   private static final int update = 2;
   private final Map<UUID, DangerModule.Inner_DataUtil> init_Map = new HashMap<>();
   private final ConcurrentHashMap<Integer, Vec3> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private final ConcurrentLinkedQueue<Integer> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
   private List<DangerModule.Inner_CachePool> init_List = List.of();
   private List<IncomingProjectileScanner.Inner_BufferHelper> load_List = List.of();
   private List<IncomingProjectileScanner.Inner_BufferHelper> read_List = List.of();
   private final FireballBlastPredictor init_x = new FireballBlastPredictor();
   private List<FireballBlastPredictor.Inner_DataUtil> write_List = List.of();
   private Object init_Obj;
   private final BedProximityTracker init_v = new BedProximityTracker();
   private Entity init_Entity;
   private boolean init_Z;
   private boolean load_Z;
   private int sync;
   private int flush = -2147483648;
   private int close;
   private int open = -2147483648;
   private int poll = -2147483648;
   private double read_D = 0.0 / 0.0;
   private String init_Str = "";
   private String load_Str = "Idle";
   private RotationMath.Rotation init_ce_DataUtil;
   private RotationMath.Rotation load_ce_DataUtil;
   private int push = -2147483648;
   private RotationMath.Rotation read_ce_DataUtil;

   public DangerModule() {
      super("danger", "Danger", ae.init_ae, "Predict nearby projectiles and pearl landings; optionally return fireballs.");
      AutismConfig.ModuleState saved = AutismConfig.getGlobal().modules.get(this.id());
      if (saved != null) {
         saved.settings.putAll(DangerOptionKeys.init(saved.settings));
      }

      for (IncomingProjectileScanner.Inner_StreamUtil kind : IncomingProjectileScanner.Inner_StreamUtil.values()) {
         this.init_V_A_StreamUtil(kind);
         if (kind == IncomingProjectileScanner.Inner_StreamUtil.FIREBALL) {
            this.add(
               new BoolSetting("auto-return", "Auto Return", true)
                  .group(kind.load())
                  .description("Rotate smoothly and hit incoming redirectable fireballs")
                  .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
            );
            this.add(
               new EnumSetting<>("rotation", "Rotation", DangerModule.Inner_StreamUtil.SILENT, DangerModule.Inner_StreamUtil.values())
                  .group(kind.load())
                  .visibleWhen(() -> this.init_Z_A_StreamUtil(kind) && this.bool("auto-return"))
            );
            this.add(
               new BoolSetting("fireball-blast-blocks", "Blast Block Highlights", false)
                  .group(kind.load())
                  .description("Red blocks at risk from incoming fireballs and your own shots; dimmer edges are less likely")
                  .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
            );
         } else if (kind == IncomingProjectileScanner.Inner_StreamUtil.PEARL) {
            this.add(
               new BoolSetting("pearl-ghost", "Landing Ghost", true)
                  .group(kind.load())
                  .description("Textureless ghost at every known teleport landing, white or name-colored; independent of Warning Radius")
                  .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
            );
            this.add(
               new DoubleSetting("ghost-opacity", "Ghost Opacity", 0.35, 0.1, 0.8, 0.05)
                  .group(kind.load())
                  .visibleWhen(() -> this.init_Z_A_StreamUtil(kind) && this.bool("pearl-ghost"))
            );
         }
      }

      this.add(
         new DoubleSetting("warning-scale", "Warning Scale", 1.0, 0.5, 4.0, 0.5).group("Display").description("Size of warning icons").visibleWhen(this::pop)
      );
      this.add(
         new BoolSetting("bed-warning", "Enabled", true)
            .group("Bed Danger")
            .description("Top-center warning for enemies near your team's bed; detects ownership from your name/team color.")
      );
      this.add(
         new DoubleSetting("bed-warning-radius", "Warning Distance", 30.0, 1.0, 128.0, 1.0)
            .unit("m")
            .group("Bed Danger")
            .description("Show when an opponent is within this distance of your bed.")
            .visibleWhen(() -> this.bool("bed-warning"))
      );
      this.add(
         new IntSetting("bed-scan-range", "Bed Scan Range", 128, 16, 256, 16)
            .unit("m")
            .group("Bed Danger")
            .description("Find your own bed in loaded chunks, then remember it while away.")
            .visibleWhen(() -> this.bool("bed-warning"))
      );
      this.add(new BoolSetting("bed-include-team", "Include Teammates", false).group("Bed Danger").visibleWhen(() -> this.bool("bed-warning")));
      this.add(new BoolSetting("bed-include-friends", "Include Friends", false).group("Bed Danger").visibleWhen(() -> this.bool("bed-warning")));
      this.add(new BoolSetting("bed-distances", "Show Distances", true).group("Bed Danger").visibleWhen(() -> this.bool("bed-warning")));
      this.add(new DoubleSetting("bed-warning-scale", "Warning Size", 1.0, 0.5, 2.0, 0.1).group("Bed Danger").visibleWhen(() -> this.bool("bed-warning")));
      this.add(
         new BoolSetting("bed-warning-sound", "Play Sound", false)
            .group("Bed Danger")
            .description("Play once when another enemy team enters the warning distance.")
            .visibleWhen(() -> this.bool("bed-warning"))
      );
      this.add(
         new ChoiceSetting("bed-sound", "Alert Sound", "Ping", "Ping", "Bell", "Chime")
            .group("Bed Danger")
            .visibleWhen(() -> this.bool("bed-warning") && this.bool("bed-warning-sound"))
      );
      this.add(
         new IntSetting("bed-sound-volume", "Sound Volume", 70, 0, 100, 1)
            .unit("%")
            .group("Bed Danger")
            .visibleWhen(() -> this.bool("bed-warning") && this.bool("bed-warning-sound"))
      );
      this.add(
         new ActionSetting("bed-sound-preview", "Preview Sound", this::init_V_52)
            .buttonLabel("Play")
            .availableOffline()
            .group("Bed Danger")
            .visibleWhen(() -> this.bool("bed-warning") && this.bool("bed-warning-sound"))
      );
   }

   public float init_F_10() {
      return (float)Math.max(0.5, Math.min(4.0, this.decimal("warning-scale")));
   }

   List<BedProximityTracker.Inner_DataUtil> init_List_19() {
      return this.init_v.init_List();
   }

   void init_V_52() {
      if (MC != null && !(this.decimal("bed-sound-volume") <= 0.0)) {
         Reference var10000;
         switch (this.choice("bed-sound")) {
            case "Bell":
               var10000 = SoundEvents.NOTE_BLOCK_BELL;
               break;
            case "Chime":
               var10000 = SoundEvents.NOTE_BLOCK_CHIME;
               break;
            default:
               var10000 = SoundEvents.NOTE_BLOCK_PLING;
         }

         Reference<SoundEvent> sound = var10000;
         MC.getSoundManager().play(SimpleSoundInstance.forUI(sound.value(), 1.0F, (float)(this.integer("bed-sound-volume") / 100.0)));
      }
   }

   public static boolean init_Z_52() {
      DangerModule danger = init_z();
      return danger != null && danger.isEnabled() && danger.poll != -2147483648 && ClutchFallPhysics.isWithinTickWindow(AutismSessionState.getInstance().getClientTick(), danger.poll, 1);
   }

   private boolean pop() {
      for (IncomingProjectileScanner.Inner_StreamUtil kind : IncomingProjectileScanner.Inner_StreamUtil.values()) {
         if (this.init_Z_A_StreamUtil_Str(kind, "hud")) {
            return true;
         }
      }

      return false;
   }

   private void init_V_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil kind) {
      String key = kind.init_Str() + "-";
      String group = kind.load();
      this.add(new BoolSetting(key + "enabled", "Enabled", true).group(group));
      this.add(
         new BoolSetting(key + "hud", "Directional Warning", true)
            .group(group)
            .description("Compact projectile icon and direction arrow, colored green to red by distance")
            .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
      );
      int multiplier = kind == IncomingProjectileScanner.Inner_StreamUtil.PEARL ? 2 : 1;
      this.add(
         new DoubleSetting(key + "radius", "Warning Radius", 15 * multiplier, multiplier, 30 * multiplier, 1.0)
            .group(group)
            .description("Only warn about predicted landings or passing paths within this many blocks")
            .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
      );
      this.add(
         new BoolSetting(key + "trajectory", "Show Trajectory", true)
            .group(group)
            .description("Draw the flight path and its landing marker through walls")
            .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
      );
      this.add(
         new BoolSetting(key + "aim-preview", "Enemy Aim Preview", false)
            .group(group)
            .description("Preview this projectile while an enemy holds it, before firing or throwing")
            .visibleWhen(() -> this.init_Z_A_StreamUtil(kind))
      );
   }

   public static DangerModule init_z() {
      return al.init_Module_Str("danger") instanceof DangerModule danger ? danger : null;
   }

   public static boolean read_Z_22() {
      DangerModule danger = init_z();
      return danger != null
         && danger.init_Z
         && !aD.init_Z()
         && !FreecamController.isActive()
         && "danger".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())
         && autismclient.util.SilentRotationManager.hasActiveRotation();
   }

   public static boolean sync_Z_12() {
      DangerModule danger = init_z();
      return danger != null && danger.init_Z && !danger.load_Z && !aD.init_Z() && !FreecamController.isActive();
   }

   public static boolean flush_Z_10() {
      DangerModule danger = init_z();
      return danger != null && danger.init_Z && !aD.init_Z() && !FreecamController.isActive();
   }

   public List<DangerModule.Inner_CachePool> load_List_4() {
      return this.init_List;
   }

   public String init_Str_17() {
      return this.init_Str;
   }

   public String load_Str_7() {
      return this.load_Str;
   }

   public int init_I_31() {
      return this.close;
   }

   public List<IncomingProjectileScanner.Inner_BufferHelper> read_List_2() {
      return this.load_List;
   }

   public List<IncomingProjectileScanner.Inner_BufferHelper> write_List() {
      return this.read_List;
   }

   public List<FireballBlastPredictor.Inner_DataUtil> update_List() {
      return this.write_List;
   }

   public boolean init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil kind) {
      return this.bool(kind.init_Str() + "-enabled");
   }

   public boolean init_Z_A_StreamUtil_Str(IncomingProjectileScanner.Inner_StreamUtil kind, String option) {
      return this.init_Z_A_StreamUtil(kind) && this.bool(kind.init_Str() + "-" + option);
   }

   public double init_D_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil kind) {
      return this.decimal(kind.init_Str() + "-radius");
   }

   public boolean close_Z_9() {
      return this.isEnabled() && this.load_List.stream().anyMatch(w -> this.init_Z_A_StreamUtil_Str(w.init_A_StreamUtil(), "hud"));
   }

   public boolean open_Z_6() {
      return !this.write_List.isEmpty()
         || this.load_List.stream().anyMatch(w -> this.init_Z_A_StreamUtil_Str(w.init_A_StreamUtil(), "trajectory"))
         || this.init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.PEARL) && this.bool("pearl-ghost") && !this.read_List.isEmpty();
   }

   public int init_I_A_BufferHelper(IncomingProjectileScanner.Inner_BufferHelper warning) {
      if (warning.init_Z()) {
         return -9720065;
      }

      double distance = MC.player.position().distanceTo(warning.init_Entity().position());
      return am.init_I_D_D(Math.max(0.0, distance - 3.0), 17.0);
   }

   @Override
   public boolean load_Z_23() {
      return (this.init_Z || autismclient.util.SilentRotationManager.isReturningToPlayer()) && !aD.init_Z() && !FreecamController.isActive();
   }

   @Override
   public boolean hasDisabledTickWork() {
      return this.init_Z || tick_Z_8();
   }

   @Override
   public void tick() {
      if (this.isEnabled()) {
         this.init_v.init_V_Minecraft_z(MC, this);
      }

      if (!this.isEnabled() && this.init_Z) {
         this.open = AutismSessionState.getInstance().getClientTick();
         this.flush_V_10();
      }

      if (!this.isEnabled()) {
         this.load_V_35();
      }
   }

   private static boolean tick_Z_8() {
      return "danger".equals(autismclient.util.SilentRotationManager.getActiveOwnerId()) && autismclient.util.SilentRotationManager.isReturningToPlayer();
   }

   private void load_V_35() {
      if (!this.init_Z && MC != null && MC.player != null && tick_Z_8()) {
         autismclient.util.SilentRotationManager.tick("danger", MC.player);
      }
   }

   @Override
   public void preMovementTick() {
      if (MC != null && MC.player != null && MC.level != null && MC.player.isAlive()) {
         if (this.init_Obj != MC.level) {
            this.poll_V_6();
            this.init_Obj = MC.level;
         }

         int nextTick = AutismSessionState.getInstance().getClientTick();
         if (this.init_Z && nextTick - this.open > 1) {
            autismclient.util.SilentRotationManager.syncFromServerRotation();
         }

         this.open = nextTick;
         this.load_V_35();
         this.sync_V_13();
         if (MC.player.onGround()) {
            this.read_D = MC.player.getY();
         }

         this.write_V_22();
         this.read_V_23();
         if (this.isEnabled() && this.init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.FIREBALL) && this.bool("auto-return") && this.set()) {
            DangerModule.Inner_CachePool best = null;
            boolean landingPriority = false;

            for (DangerModule.Inner_CachePool threat : this.init_List) {
               DangerModule.Inner_DataUtil attempt = this.init_Map.get(threat.init_Entity.getUUID());
               if (threat.init_Z && (attempt == null || FireballInterceptMath.init(this.open - attempt.init_I))) {
                  Fireball ball = (Fireball)threat.init_Entity;
                  RotationMath.Rotation goal = this.init_ce_DataUtil_Entity(ball);
                  ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
                  double yaw = wire.initialized() ? wire.currentYaw() : MC.player.getYRot();
                  double pitch = wire.initialized() ? wire.currentPitch() : MC.player.getXRot();
                  double strike = FireballInterceptMath.init_D_Vec3(
                     MC.player.getEyePosition(),
                     this.init_Vec3_8(),
                     ball.getBoundingBox(),
                     ball.getDeltaMovement(),
                     ball.accelerationPower,
                     ball.isInWater() ? 0.8 : 0.95,
                     MC.player.entityInteractionRange() - 0.025,
                     40
                  );
                  double yawDifference = RotationMath.angleDelta(goal.yaw(), (float)yaw);
                  double pitchDifference = goal.pitch() - pitch;
                  float step = FireballInterceptMath.init(yawDifference, pitchDifference, strike, threat.init_D, RotationMath.gcdStep());
                  int turn = FireballInterceptMath.init(yawDifference, pitchDifference, step, RotationMath.gcdStep());
                  boolean current = this.init_Z && !this.load_Z && this.init_Entity == ball;
                  if (this.init_Z_I_D_2(turn, strike)) {
                     int start = this.get() ? turn : Math.max(turn, init_I_D_D(yawDifference, pitchDifference));
                     boolean holdBridge = threat.load_Z && strike >= 0.0 && strike <= 14.0 && run();
                     if (current || holdBridge || strike >= 0.0 && strike <= init_I_I_5(start) || threat.load_Z && threat.init_D <= 1.0) {
                        best = threat;
                        break;
                     }
                  } else {
                     landingPriority = true;
                  }
               }
            }

            if (best == null) {
               this.flush_V_10();
               if (landingPriority) {
                  this.init_Str = "Bridge landing priority";
               }
            } else {
               if (this.load_Z) {
                  this.load_Z = false;
                  this.init_Entity = null;
                  this.init_Z = false;
               }

               if (this.init_Entity != best.init_Entity) {
                  this.init_Entity = best.init_Entity;
                  this.sync = this.open;
                  autismclient.util.SilentRotationManager.syncFromServerRotation();
                  this.init_Z = true;
                  this.read_ce_DataUtil = null;
                  this.load_ce_DataUtil = autismclient.util.SilentRotationManager.getCurrentRotation();
               }

               if (this.open - this.sync >= Math.max(12, 14) + 6) {
                  this.init_Map.put(this.init_Entity.getUUID(), new DangerModule.Inner_DataUtil(this.open, this.init_Entity.getDeltaMovement(), this.init_Entity.getId()));
                  this.init_Str = "Return window ended";
                  this.flush_V_10();
               } else {
                  RotationMath.Rotation goal = this.init_ce_DataUtil_Entity(this.init_Entity);
                  autismclient.util.SilentRotationManager.requestRotation(this.id(), 40, goal);
                  this.init_ce_DataUtil = this.load_ce_DataUtil;
                  RotationMath.Rotation from = autismclient.util.SilentRotationManager.getCurrentRotation();
                  if (from == null) {
                     from = RotationMath.ofPlayer(MC.player);
                  }

                  double strike = FireballInterceptMath.init_D_Vec3(
                     MC.player.getEyePosition(),
                     this.init_Vec3_8(),
                     this.init_Entity.getBoundingBox(),
                     this.init_Entity.getDeltaMovement(),
                     ((Fireball)this.init_Entity).accelerationPower,
                     this.init_Entity.isInWater() ? 0.8 : 0.95,
                     MC.player.entityInteractionRange() - 0.025,
                     40
                  );
                  if (this.get()) {
                     autismclient.util.SilentRotationManager.stepDangerRotation(
                        MC.player, FireballInterceptMath.init(RotationMath.angleDelta(goal.yaw(), from.yaw()), goal.pitch() - from.pitch(), strike, best.init_D, RotationMath.gcdStep())
                     );
                  } else {
                     double deadline = (strike >= 0.0 ? Math.min(strike, best.init_D) : best.init_D) - 2.0;
                     autismclient.util.SilentRotationManager.stepUrgentTimed("danger", MC.player, Double.isFinite(deadline) ? Math.max(1, (int)Math.floor(deadline)) : 1);
                  }

                  this.load_ce_DataUtil = autismclient.util.SilentRotationManager.getCurrentRotation();
                  this.init_Str = "Returning fireball";
               }
            }
         } else {
            if (this.init_Z) {
               autismclient.util.SilentRotationManager.syncFromServerRotation();
            }

            this.flush_V_10();
         }
      } else {
         this.poll_V_6();
      }
   }

   static int init_I_I_5(int turnTicks) {
      return Math.min(11, turnTicks + 2);
   }

   private void read_V_23() {
      if (this.isEnabled() && this.init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.FIREBALL) && this.bool("auto-return")) {
         for (DangerModule.Inner_CachePool threat : this.init_List) {
            if (threat.init_Z && threat.load_Z) {
               DangerModule.Inner_DataUtil attempt = this.init_Map.get(threat.init_Entity.getUUID());
               if (attempt == null || FireballInterceptMath.init(this.open - attempt.init_I)) {
                  Fireball ball = (Fireball)threat.init_Entity;
                  double strike = FireballInterceptMath.init_D_Vec3(
                     MC.player.getEyePosition(),
                     this.init_Vec3_8(),
                     ball.getBoundingBox(),
                     ball.getDeltaMovement(),
                     ball.accelerationPower,
                     ball.isInWater() ? 0.8 : 0.95,
                     MC.player.entityInteractionRange() - 0.025,
                     40
                  );
                  if (strike >= 0.0 && strike <= 8.0) {
                     this.poll = this.open;
                     return;
                  }
               }
            }
         }
      }
   }

   private RotationMath.Rotation init_ce_DataUtil_Entity(Entity ball) {
      Vec3 eye = MC.player.getEyePosition();
      AABB box = ball.getBoundingBox();
      Entity shooter = ball instanceof Projectile projectile ? projectile.getOwner() : null;
      Vec3 outgoing = shooter != null && shooter != MC.player && shooter.isAlive()
         ? shooter.getBoundingBox().getCenter().add(shooter.getDeltaMovement().scale(Math.min(10.0, shooter.distanceTo(ball) / 1.5))).subtract(box.getCenter())
         : ball.getDeltaMovement().scale(-1.0);
      FireballInterceptMath.Inner_DataUtil aim = FireballInterceptMath.init(eye, this.init_Vec3_8(), box, ball.getDeltaMovement(), outgoing, MC.player.entityInteractionRange() - 0.025);
      return RotationMath.lookAt(aim.load(), aim.init());
   }

   private Vec3 init_Vec3_8() {
      Vec3 motion = MC.player.getDeltaMovement();
      return MC.player.onGround() ? new Vec3(motion.x, 0.0, motion.z) : motion;
   }

   static int init_I_D_D(double yawDifference, double pitchDifference) {
      return (int)Math.ceil(2.0 + Math.max(Math.abs(yawDifference), Math.abs(pitchDifference)) / 16.0);
   }

   public static boolean poll_Z_6() {
      return flush_Z_10();
   }

   private static boolean run() {
      return MC.player.onGround() && ScaffoldModule.read_Z_12() && ScaffoldModule.read_ce_DataUtil() != null;
   }

   private boolean get() {
      return !MC.player.onGround() && !MC.player.getAbilities().flying && ScaffoldModule.read_ce_DataUtil() != null;
   }

   private boolean init_Z_I_D_2(int turn, double strike) {
      if (!MC.player.onGround() && !MC.player.getAbilities().flying) {
         RotationMath.Rotation resume = ScaffoldModule.read_ce_DataUtil();
         if (resume == null) {
            return true;
         }

         double floor = Double.isFinite(this.read_D) ? this.read_D : Math.floor(MC.player.getY());
         int landing = FireballInterceptMath.init(MC.player.getY() - floor, MC.player.getDeltaMovement().y);
         Vec3 motion = MC.player.getDeltaMovement();
         AABB support = MC.player.getBoundingBox().move(motion.x * landing, floor - MC.player.getY() - 0.2, motion.z * landing).setMaxY(floor - 0.02);
         if (!MC.level.noCollision(MC.player, support)) {
            return true;
         }

         int returnTurn = 1;
         int placing = 2 * init(floor, motion, landing);
         return strike >= 0.0 && Math.max(turn, strike) + returnTurn + placing < landing;
      } else {
         return true;
      }
   }

   private static int init(double floor, Vec3 motion, int landing) {
      Vec3 from = MC.player.position();
      int row = Mth.floor(floor - 0.5);
      Set<Long> seen = new HashSet<>();
      int missing = 0;

      for (int i = 0; i <= 8; i++) {
         double t = i / 8.0 * landing;
         BlockPos cell = BlockPos.containing(from.x + motion.x * t, row, from.z + motion.z * t);
         if (seen.add(cell.asLong()) && MC.level.getBlockState(cell).getCollisionShape(MC.level, cell).isEmpty()) {
            missing++;
         }
      }

      return missing;
   }

   private boolean set() {
      return (!ClutchModule.init_Z_49() || ClutchModule.open_Z_5())
         && MC != null
         && MC.player != null
         && MC.level != null
         && MC.gameMode != null
         && MC.getConnection() != null
         && !MC.player.isSpectator()
         && MC.gui.screen() == null
         && !aD.init_Z()
         && !FreecamController.isActive()
         && !RemoteViewController.init_Z()
         && !MacroExecutor.isRunning()
         && !BlinkPacketManager.isBlinking()
         && !PkgCls_a.init_Z()
         && !autismclient.util.macro.MacroAimController.init_Z()
         && !f.load();
   }

   private void write_V_22() {
      Integer acknowledged;
      while ((acknowledged = this.init_ConcurrentLinkedQu.poll()) != null) {
         if (this.init_ConcurrentHashMap.remove(acknowledged) != null) {
            this.close++;
         }
      }

      List<DangerModule.Inner_CachePool> found = new ArrayList<>();
      double rangeSqr = 4096.0;
      Vec3 playerMotion = this.init_Vec3_8();

      for (Fireball ball : MC.level.getEntitiesOfClass(Fireball.class, MC.player.getBoundingBox().inflate(64.0))) {
         Entity entity = ball;
         if (entity.isAlive() && !entity.isRemoved() && !(entity.distanceToSqr(MC.player) > rangeSqr)) {
            DangerModule.Inner_DataUtil attempt = this.init_Map.get(entity.getUUID());
            if ((attempt == null || ball.getOwner() != MC.player && !(entity.getDeltaMovement().dot(attempt.init_Vec3) < 0.0)) && ball.getOwner() != MC.player) {
               double inertia = ball.isInWater() ? 0.8 : 0.95;
               double impact = FireballInterceptMath.init_D_AABB(
                  MC.player.getBoundingBox(), playerMotion, entity.getBoundingBox(), entity.getDeltaMovement(), 40, 1.5, ball.accelerationPower, inertia
               );
               boolean hits = impact >= 0.0;
               if (!hits) {
                  IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(MC.level, ball, IncomingProjectileScanner.Inner_StreamUtil.FIREBALL);
                  Vec3 landing = flight.init_Vec3();
                  if (landing != null && MC.player.getBoundingBox().distanceToSqr(landing) <= 12.25) {
                     impact = flight.init_D();
                     hits = true;
                  } else {
                     impact = FireballInterceptMath.load(
                        MC.player.getBoundingBox(),
                        playerMotion,
                        entity.getBoundingBox(),
                        entity.getDeltaMovement(),
                        40,
                        MC.player.entityInteractionRange() + 0.5,
                        ball.accelerationPower,
                        inertia
                     );
                  }
               }

               if (!(impact < 0.0)) {
                  Entity shooter = ball.getOwner();
                  String source = shooter instanceof Player player
                     ? player.getGameProfile().name()
                     : (shooter == null ? "Unknown sender" : shooter.getName().getString());
                  found.add(new DangerModule.Inner_CachePool(entity, impact, Math.sqrt(entity.distanceToSqr(MC.player)), source, entity instanceof LargeFireball, hits));
               }
            }
         }
      }

      found.sort(Comparator.comparingDouble(DangerModule.Inner_CachePool::init_D));
      this.init_List = List.copyOf(found);
      this.update_V_18();
      this.init_Map.entrySet().removeIf(entry -> {
         int id = entry.getValue().load;
         Entity live = MC.level.getEntity(id);
         if (this.open - entry.getValue().init_I <= 80 && live != null && live.getUUID().equals(entry.getKey())) {
            return false;
         }

         this.init_ConcurrentHashMap.remove(id);
         return true;
      });
      if (!this.init_Z) {
         this.init_Str = this.init_List.isEmpty() ? "" : (this.init_List.getFirst().load_Z ? "Incoming fireball" : "Fireball passing");
      }
   }

   private void update_V_18() {
      if (this.isEnabled() && !aD.init_Z()) {
         double range = 64.0;
         List<IncomingProjectileScanner.Inner_BufferHelper> result = new ArrayList<>();
         boolean showPearlGhosts = this.init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.PEARL) && this.bool("pearl-ghost");
         HashMap<UUID, IncomingProjectileScanner.Inner_BufferHelper> landingModels = new HashMap<>();
         HashSet<UUID> predictedPearls = new HashSet<>();
         List<Projectile> candidates = MC.level
            .getEntitiesOfClass(
               Projectile.class,
               MC.player.getBoundingBox().inflate(range),
               ballx -> IncomingProjectileScanner.init(ballx) != null
                  && this.init_Z_A_StreamUtil(IncomingProjectileScanner.init(ballx))
                  && ballx.isAlive()
                  && !ballx.isRemoved()
                  && ballx.getOwner() != MC.player
                  && ballx.getDeltaMovement().lengthSqr() > 1.0E-7
                  && (ballx.getOwner() == null || !TeamsModule.init_Z_Entity_5(ballx.getOwner()))
                  && ballx.distanceToSqr(MC.player) <= range * range
                  && (!(ballx instanceof AbstractArrow) || !((AutismDangerArrowAccessor)ballx).autism$inGround())
            );
         candidates.sort(Comparator.comparingDouble(ballx -> ballx.distanceToSqr(MC.player)));

         for (int i = 0; i < Math.min(32, candidates.size()); i++) {
            Projectile ball = candidates.get(i);
            DangerModule.Inner_DataUtil attempt = this.init_Map.get(ball.getUUID());
            if (attempt == null || !(ball.getDeltaMovement().dot(attempt.init_Vec3) < 0.0)) {
               IncomingProjectileScanner.Inner_StreamUtil kind = IncomingProjectileScanner.init(ball);
               IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(MC.level, ball, kind);
               Player owner = ball.getOwner() instanceof Player player ? player : null;
               IncomingProjectileScanner.Inner_BufferHelper warning = new IncomingProjectileScanner.Inner_BufferHelper(ball, owner, kind, false, flight, this.init_I_Player(owner));
               if (kind == IncomingProjectileScanner.Inner_StreamUtil.PEARL) {
                  predictedPearls.add(ball.getUUID());
                  if (showPearlGhosts && IncomingProjectileScanner.init(warning)) {
                     landingModels.put(ball.getUUID(), warning);
                  }
               }

               if (IncomingProjectileScanner.init(flight, kind, MC.player.position(), this.init_D_A_StreamUtil(kind))) {
                  result.add(warning);
               }
            }
         }

         if (showPearlGhosts) {
            ArrayList<ThrownEnderpearl> distantPearls = new ArrayList<>();

            for (Entity entity : MC.level.entitiesForRendering()) {
               if (entity instanceof ThrownEnderpearl pearl
                  && !predictedPearls.contains(pearl.getUUID())
                  && !pearl.isRemoved()
                  && pearl.isAlive()
                  && pearl.getOwner() instanceof AbstractClientPlayer owner
                  && owner != MC.player
                  && owner.isAlive()
                  && !TeamsModule.init_Z_Entity_5(owner)) {
                  distantPearls.add(pearl);
               }
            }

            distantPearls.sort(Comparator.comparingDouble(pearl -> pearl.distanceToSqr(MC.player)));

            for (int i = 0; i < Math.min(16, distantPearls.size()); i++) {
               ThrownEnderpearl pearl = distantPearls.get(i);
               Player owner = (Player)pearl.getOwner();
               IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(MC.level, pearl, IncomingProjectileScanner.Inner_StreamUtil.PEARL);
               IncomingProjectileScanner.Inner_BufferHelper warning = new IncomingProjectileScanner.Inner_BufferHelper(pearl, owner, IncomingProjectileScanner.Inner_StreamUtil.PEARL, false, flight, this.init_I_Player(owner));
               if (IncomingProjectileScanner.init(warning)) {
                  landingModels.put(pearl.getUUID(), warning);
               }

               if (IncomingProjectileScanner.init(flight, IncomingProjectileScanner.Inner_StreamUtil.PEARL, MC.player.position(), this.init_D_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.PEARL))) {
                  result.add(warning);
               }
            }
         }

         if (Arrays.stream(IncomingProjectileScanner.Inner_StreamUtil.values()).anyMatch(kindx -> this.init_Z_A_StreamUtil_Str(kindx, "aim-preview"))) {
            ArrayList<AbstractClientPlayer> players = new ArrayList<>(MC.level.players());
            players.removeIf(
               player -> player == MC.player
                  || !player.isAlive()
                  || player.isSpectator()
                  || player.distanceToSqr(MC.player) > range * range && (!showPearlGhosts || init_A_StreamUtil_Player(player) != IncomingProjectileScanner.Inner_StreamUtil.PEARL)
                  || TeamsModule.init_Z_Entity_5(player)
                  || !this.init_Z_Player_3(player)
                  || AntiBotTracker.isBot(player)
            );
            players.sort(Comparator.comparingDouble(player -> player.distanceToSqr(MC.player)));

            for (int i = 0; i < Math.min(12, players.size()); i++) {
               Player owner = players.get(i);

               for (InteractionHand hand : InteractionHand.values()) {
                  ItemStack stack = owner.getItemInHand(hand);
                  IncomingProjectileScanner.Inner_StreamUtil kind = IncomingProjectileScanner.init(stack);
                  if (kind != null) {
                     List<ProjectileBallistics.Inner_BufferHelper> shots = ProjectileBallistics.init(owner, stack, true, true);

                     for (ProjectileBallistics.Inner_BufferHelper shot : shots) {
                        if (!this.init_Z_A_StreamUtil_Str(kind, "aim-preview")) {
                           break;
                        }

                        if (kind != IncomingProjectileScanner.Inner_StreamUtil.ARROW || shot.init_aX_DataUtil().init_D() != 0.0) {
                           IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(MC.level, owner, kind, shot);
                           IncomingProjectileScanner.Inner_BufferHelper warning = new IncomingProjectileScanner.Inner_BufferHelper(owner, owner, kind, true, flight, this.init_I_Player(owner));
                           if (showPearlGhosts && IncomingProjectileScanner.init(warning)) {
                              landingModels.put(owner.getUUID(), warning);
                           }

                           if (IncomingProjectileScanner.init(flight, kind, MC.player.position(), this.init_D_A_StreamUtil(kind))) {
                              result.add(warning);
                           }
                        }
                     }

                     if (!shots.isEmpty()) {
                        break;
                     }
                  }
               }
            }
         }

         result.sort(Comparator.comparing(IncomingProjectileScanner.Inner_BufferHelper::init_Z).thenComparingDouble(warningx -> warningx.init_A_DataUtil().init_D()));
         this.load_List = List.copyOf(result.subList(0, Math.min(16, result.size())));
         ArrayList<IncomingProjectileScanner.Inner_BufferHelper> models = new ArrayList<>(landingModels.values());
         models.sort(
            Comparator.comparing(IncomingProjectileScanner.Inner_BufferHelper::init_Z)
               .thenComparingDouble(warningx -> warningx.init_A_DataUtil().load().distanceToSqr(MC.player.position()))
         );
         this.read_List = List.copyOf(models);
         if (this.init_Z_A_StreamUtil(IncomingProjectileScanner.Inner_StreamUtil.FIREBALL) && this.bool("fireball-blast-blocks")) {
            ArrayList<IncomingProjectileScanner.Inner_BufferHelper> blastSources = new ArrayList<>(this.load_List);
            List<LargeFireball> own = MC.level
               .getEntitiesOfClass(
                  LargeFireball.class,
                  MC.player.getBoundingBox().inflate(range),
                  ballx -> ballx.getOwner() == MC.player
                     && ballx.isAlive()
                     && !ballx.isRemoved()
                     && ballx.distanceToSqr(MC.player) <= range * range
                     && ballx.getDeltaMovement().lengthSqr() > 1.0E-7
               );
            own.sort(Comparator.comparingDouble(ballx -> ballx.distanceToSqr(MC.player)));

            for (int i = 0; i < Math.min(8, own.size()); i++) {
               LargeFireball ball = own.get(i);
               IncomingProjectileScanner.Inner_DataUtil flight = IncomingProjectileScanner.init(MC.level, ball, IncomingProjectileScanner.Inner_StreamUtil.FIREBALL);
               if (flight.init_Vec3() != null) {
                  blastSources.add(new IncomingProjectileScanner.Inner_BufferHelper(ball, MC.player, IncomingProjectileScanner.Inner_StreamUtil.FIREBALL, false, flight, -1));
               }
            }

            blastSources.sort(Comparator.comparingDouble(source -> source.init_A_DataUtil().init_D()));
            this.write_List = this.init_x.init_List_Level_List(MC.level, blastSources);
         } else {
            this.open_V_6();
         }
      } else {
         this.load_List = this.read_List = List.of();
         this.open_V_6();
      }
   }

   private boolean init_Z_Player_3(Player player) {
      IncomingProjectileScanner.Inner_StreamUtil kind = init_A_StreamUtil_Player(player);
      return kind != null && this.init_Z_A_StreamUtil_Str(kind, "aim-preview");
   }

   private static IncomingProjectileScanner.Inner_StreamUtil init_A_StreamUtil_Player(Player player) {
      IncomingProjectileScanner.Inner_StreamUtil main = IncomingProjectileScanner.init(player.getMainHandItem());
      return main != null ? main : IncomingProjectileScanner.init(player.getOffhandItem());
   }

   private int init_I_Player(Player owner) {
      if (owner == null) {
         return -1;
      }

      PlayerInfo info = MC.getConnection() == null ? null : MC.getConnection().getPlayerInfo(owner.getUUID());
      PlayerTeam team = owner.getTeam();
      TextColor fallback = team == null ? null : team.getColor().map(TeamColor::textColor).orElse(null);
      return PlayerDisplayNameParser.init_I(owner.getGameProfile().name(), info == null ? null : info.getTabListDisplayName(), fallback);
   }

   @Override
   public boolean onPacketReceive(Packet<?> packet) {
      if (packet instanceof ClientboundSetEntityMotionPacket motion) {
         Vec3 incoming = this.init_ConcurrentHashMap.get(motion.id());
         if (incoming != null && motion.movement().dot(incoming) < 0.0) {
            this.init_ConcurrentLinkedQu.offer(motion.id());
         }
      } else if (packet instanceof ClientboundEntityPositionSyncPacket position) {
         Vec3 incoming = this.init_ConcurrentHashMap.get(position.id());
         if (incoming != null && Compat263.entityPosSyncValues(position).deltaMovement().dot(incoming) < 0.0) {
            this.init_ConcurrentLinkedQu.offer(position.id());
         }
      }

      return false;
   }

   public static void init_V_Packet_3(Packet<?> packet) {
      DangerModule danger = init_z();
      if (danger != null) {
         danger.onPacketReceive(packet);
      }
   }

   private void sync_V_13() {
      if (!this.isEnabled() || !this.init_Z || this.load_Z || this.init_Entity == null || !this.init_Entity.isAlive() || !this.set()) {
         this.load_Str = "Inactive";
      } else if (!read_Z_22() || this.flush == this.open) {
         this.load_Str = "Rotation or attack interval";
      } else if (SprintStateTracker.init_Z()) {
         this.load_Str = "Sprint still announced";
      } else {
         DangerModule.Inner_DataUtil previousAttempt = this.init_Map.get(this.init_Entity.getUUID());
         if (previousAttempt != null && !FireballInterceptMath.init(this.open - previousAttempt.init_I)) {
            this.load_Str = "Retry interval";
         } else {
            RotationMath.Rotation rotation = autismclient.util.SilentRotationManager.getCurrentRotation();
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.getSnapshot();
            if (rotation != null
               && wire.initialized()
               && !(Math.abs(RotationMath.angleDelta(rotation.yaw(), wire.currentYaw())) > 0.15)
               && !(Math.abs(rotation.pitch() - wire.currentPitch()) > 0.15)) {
               Vec3 eye = MC.player.getEyePosition();
               double reach = MC.player.entityInteractionRange() - 0.025;
               Vec3 direction = Vec3.directionFromRotation(rotation.pitch(), rotation.yaw());
               Optional<Vec3> hit = this.init_Entity.getBoundingBox().clip(eye, eye.add(direction.scale(reach)));
               if (this.init_Entity.getBoundingBox().contains(eye)) {
                  hit = Optional.of(eye);
               }

               if (hit.isEmpty()) {
                  this.load_Str = "Live ray or reach";
               } else if (MC.level.clip(new ClipContext(eye, hit.get(), Block.COLLIDER, Fluid.NONE, MC.player)).getType() != Type.MISS) {
                  this.load_Str = "Blocked ray";
               } else if (!autismclient.util.InventorySlotArbiter.beginActionGroup(this.id())) {
                  this.load_Str = "Hand busy";
               } else {
                  try {
                     Vec3 incoming = this.init_Entity.getDeltaMovement();
                     this.init_ConcurrentHashMap.put(this.init_Entity.getId(), incoming);
                     if (MC.player.isUsingItem()) {
                        MC.gameMode.releaseUsingItem(MC.player);
                     }

                     float cameraYaw = MC.player.getYRot();
                     float cameraPitch = MC.player.getXRot();

                     try {
                        MC.player.setYRot(rotation.yaw());
                        MC.player.setXRot(rotation.pitch());
                        MC.gameMode.attack(MC.player, this.init_Entity);
                     } finally {
                        MC.player.setYRot(cameraYaw);
                        MC.player.setXRot(cameraPitch);
                     }

                     Compat263.swing(MC.player, InteractionHand.MAIN_HAND);
                     this.init_Map.put(this.init_Entity.getUUID(), new DangerModule.Inner_DataUtil(this.open, incoming, this.init_Entity.getId()));
                     this.flush = this.open;
                     this.init_Str = "Return sent";
                     this.load_Str = "Sent";
                     this.load_Z = true;
                     this.close_V_9();
                  } finally {
                     autismclient.util.InventorySlotArbiter.endActionGroup(this.id());
                  }
               }
            } else {
               this.load_Str = "Unwritten look";
            }
         }
      }
   }

   private void flush_V_10() {
      if (this.init_Z) {
         if (ClutchModule.init_Z_49() && !ClutchModule.open_Z_5()) {
            this.close_V_9();
            this.init_Z = this.load_Z = false;
            this.init_Entity = null;
         } else if (MC != null && MC.player != null && MC.level != null) {
            if (!this.id().equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
               if (!this.load_Z) {
                  this.close_V_9();
               }

               this.init_Z = false;
               this.load_Z = false;
               this.init_Entity = null;
            } else {
               if (!this.load_Z) {
                  this.close_V_9();
               }

               this.load_Z = true;
               RotationMath.Rotation bridgeResume = ScaffoldModule.read_ce_DataUtil();
               Module scaffold = al.init_Module_Str("scaffold");
               if (bridgeResume != null || scaffold != null && scaffold.isEnabled()) {
                  RotationMath.Rotation resume = bridgeResume != null ? bridgeResume : RotationMath.ofPlayer(MC.player);
                  autismclient.util.SilentRotationManager.requestRotation(this.id(), 40, resume);
                  autismclient.util.SilentRotationManager.stepDangerRotation(MC.player, (float)(180.0 + RotationMath.gcdStep()));
                  RotationMath.Rotation endpoint = autismclient.util.SilentRotationManager.getCurrentRotation();
                  if (endpoint != null && RotationMath.vectorAngleDegrees(endpoint, resume) <= 1.0F) {
                     if (bridgeResume != null) {
                        ScaffoldModule.init_V_ce_DataUtil_3(endpoint);
                     }

                     autismclient.util.SilentRotationManager.resetAll();
                     this.init_Z = false;
                     this.load_Z = false;
                     this.init_Entity = null;
                  }
               } else {
                  autismclient.util.SilentRotationManager.releaseOwner("danger");
                  this.init_Z = false;
                  this.load_Z = false;
                  this.init_Entity = null;
                  this.load_V_35();
               }
            }
         } else {
            this.poll_V_6();
         }
      }
   }

   public static RotationMath.Rotation init_ce_DataUtil_F_2(float partialTick) {
      DangerModule danger = init_z();
      if (danger == null
         || !"Visible".equalsIgnoreCase(danger.value("rotation"))
         || MC == null
         || MC.player == null
         || !MC.player.isAlive()
         || aD.init_Z()
         || FreecamController.isActive()) {
         return null;
      }

      if (!danger.init_Z || danger.load_Z) {
         float progress = (AutismSessionState.getInstance().getClientTick() - danger.push + partialTick) / 2.0F;
         if (danger.read_ce_DataUtil != null && !(progress >= 1.0F) && !(progress < 0.0F)) {
            progress = progress * progress * (3.0F - 2.0F * progress);
            return RotationMath.lerp(danger.read_ce_DataUtil, new RotationMath.Rotation(MC.player.getYRot(), MC.player.getXRot()), progress);
         } else {
            return null;
         }
      } else if (danger.load_ce_DataUtil == null) {
         return null;
      } else {
         return danger.init_ce_DataUtil == null
            ? danger.load_ce_DataUtil
            : RotationMath.lerp(danger.init_ce_DataUtil, danger.load_ce_DataUtil, partialTick);
      }
   }

   private void close_V_9() {
      this.push = AutismSessionState.getInstance().getClientTick();
      this.read_ce_DataUtil = this.load_ce_DataUtil;
   }

   public static boolean push() {
      return MC != null && MC.player != null
         ? sync_Z_12() || flush_Z_10() && MC.player.onGround() && MC.player.input != null && MC.player.input.keyPresses.jump()
         : false;
   }

   @Override
   public void onDisable() {
      this.init_v.init_V();
      this.load_List = this.read_List = List.of();
      this.open_V_6();
      this.flush_V_10();
   }

   @Override
   public void onGameLeft() {
      this.poll_V_6();
   }

   @Override
   public boolean shouldCancelUse(HitResult hit, InteractionHand hand) {
      return read_Z_22();
   }

   public static Input init_Input_Input_4(Input input) {
      if (input != null
         && flush_Z_10()
         && MC != null
         && MC.player != null
         && MC.level != null
         && al.init_Module_Str("scaffold") instanceof ScaffoldModule scaffold
         && scaffold.isEnabled()) {
         Vec3 step = MC.player.getDeltaMovement();
         AABB support = MC.player.getBoundingBox().move(step.x * 3.0, -0.25, step.z * 3.0).deflate(0.05).setMaxY(MC.player.getY() - 0.02);
         boolean edge = MC.player.onGround() && !ScaffoldModule.read_Z_12() && MC.level.noCollision(MC.player, support);
         if (MC.player.onGround() && input.jump()) {
            MC.player.setSprinting(false);
         }

         return new Input(
            input.forward(),
            input.backward(),
            input.left(),
            input.right(),
            input.jump() && !edge && !MC.player.onGround(),
            input.shift() || edge,
            input.sprint() && (!MC.player.onGround() || !input.jump())
         );
      } else {
         return input;
      }
   }

   private void open_V_6() {
      this.write_List = List.of();
      this.init_x.init_V();
   }

   private void poll_V_6() {
      this.init_v.init_V();
      this.open_V_6();
      if ("danger".equals(autismclient.util.SilentRotationManager.getActiveOwnerId())) {
         autismclient.util.SilentRotationManager.resetAll();
      }

      this.init_List = List.of();
      this.load_List = this.read_List = List.of();
      this.init_Map.clear();
      this.init_ConcurrentHashMap.clear();
      this.init_ConcurrentLinkedQu.clear();
      this.init_Entity = null;
      this.init_Obj = null;
      this.init_Z = false;
      this.load_Z = false;
      this.init_Str = "";
      this.init_ce_DataUtil = this.load_ce_DataUtil = this.read_ce_DataUtil = null;
      this.read_D = 0.0 / 0.0;
      this.poll = -2147483648;
   }

   public record Inner_CachePool(Entity init_Entity, double init_D, double load_D, String init_Str, boolean init_Z, boolean load_Z) {
   }

   private record Inner_DataUtil(int init_I, Vec3 init_Vec3, int load) {
   }

   public enum Inner_StreamUtil {
      SILENT,
      VISIBLE;
   }
}
