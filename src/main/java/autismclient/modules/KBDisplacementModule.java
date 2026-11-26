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
   private int init_I = -2147483648;
   private boolean init_Z;
   private long init_J;
   private final QueuedAttackBuffer init_T = new QueuedAttackBuffer();
   private final StrafeInputCorrector init_U = new StrafeInputCorrector();
   private long load_J;
   private ClientLevel init_ClientLevel;
   private ClientLevel load_ClientLevel;
   private ClientPacketListener init_ClientPacketListen;
   private Player init_Player;
   private int load_I;
   private int read_I;
   private int write_I = -2147483648;
   private Item init_Item;
   private float init_F;
   private float load_F;
   private float read_F;
   private LivingEntity init_LivingEntity;
   private boolean load_Z;
   private long read_J;
   private long write_J;
   private long update_J;
   private long sync_J;
   private long flush;
   private long close;
   private final TreeMap<String, Integer> init_TreeMap = new TreeMap<>();
   private String init_Str = "none";
   private String load_Str = "";
   private float write_F;
   private float update_F;
   private float sync_F;
   private boolean read_Z;
   private int update_I = -2147483648;
   private Vec3 init_Vec3 = Vec3.ZERO;
   private String read_Str = "";
   private final TargetHurtWindowTracker init_Y = new TargetHurtWindowTracker();
   private final ArrayDeque<Long> init_ArrayDeque = new ArrayDeque<>();
   private static final long open = 10000000000L;
   private static final int sync_I = 12;
   private static final double init_D = 0.06;
   private static final double load_D = 62.0;
   private final WTapController init_bf = new WTapController(new WTapController.Inner_DataUtil() {
      @Override
      public int init_I() {
         return 100;
      }

      @Override
      public ValueRange init_ValueRange() {
         return WTapController.init_ValueRange;
      }

      @Override
      public ValueRange load_ValueRange() {
         return WTapController.load_ValueRange;
      }

      @Override
      public boolean init_Z() {
         return KBDisplacementModule.this.bool("players-only");
      }

      @Override
      public boolean load_Z() {
         return false;
      }
   });
   private final Map<Integer, Integer> init_Map = new HashMap<>();
   private final Map<Integer, KBDisplacementModule.Inner_BufferHelper> load_Map = new HashMap<>();
   private ClientLevel read_ClientLevel;

   private void init_V_Str_2(String why) {
      this.init_TreeMap.merge(why, 1, Integer::sum);
   }

   private void init_V_J_W_NetHelper(long nanos, KnockbackThrowPlanner.Inner_NetHelper plan) {
      this.update_J++;
      this.sync_J += nanos;
      this.flush = Math.max(this.flush, nanos);
      this.init_Str = plan == null ? "none" : (plan.init_Z() ? "steer" : (plan.load_Z() ? "throw-robust" : "throw"));
   }

   public static String init_Str_7() {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m ? m.load_Str : "";
   }

   public static String load_Str_2() {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m) {
         StringBuilder var4 = new StringBuilder();

         for (Entry<String, Integer> entry : m.init_TreeMap.entrySet()) {
            var4.append(var4.isEmpty() ? "" : ",").append(entry.getKey()).append(':').append(entry.getValue());
         }

         return String.format(
            Locale.ROOT,
            "plans=%d meanUs=%d maxUs=%d redirects=%d throws=%d steers=%d dropped=%d last=%s rtt=%d sprint=%s why=%s",
            m.update_J,
            m.update_J == 0L ? 0L : m.sync_J / m.update_J / 1000L,
            m.flush / 1000L,
            m.init_J,
            m.read_J,
            m.write_J,
            m.close,
            m.init_Str,
            m.init_Y.init_I(m.init_I_15()),
            SprintStateTracker.load(),
            var4
         );
      } else {
         return "";
      }
   }

   static WTapController init_bf() {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.isEnabled() && m.bool("w-tap") ? m.init_bf : null;
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
         new EnumSetting<>("side", "Side", KBDisplacementModule.Inner_DataUtil.Auto, KBDisplacementModule.Inner_DataUtil.values(), Enum::name)
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

   public static void init_V_Player(Player player, Entity entity, Runnable attack) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.isEnabled() && !m.init_Z && player == MC.player && entity instanceof LivingEntity target) {
         Runnable plain = () -> {
            attack.run();
            m.init_V_LivingEntity_2(target);
         };
         ClientLevel level = MC.level;
         if (level != null && m.init_Z_LivingEntity_2(target)) {
            int tick = AutismSessionState.init_ck().state_I();
            if (m.init_T.init_Z()) {
               Runnable receipt = autismclient.util.CombatAttackDispatcher.init_Runnable(target);
               if (!m.init_T.init(target, attack, receipt)) {
                  m.init_V_Z_5(false);
                  plain.run();
                  if (receipt != null) {
                     receipt.run();
                  }
               }

               m.init_V_Str_2("carried-with-pending");
            } else {
               int fallback = m.init_I_15();
               if (m.init_I != tick && m.write_I != tick && !m.init_Y.init_Z(target.getId(), tick, fallback) && !WTappingModule.load()) {
                  m.init_I = tick;
                  ServerRotationTracker.RotationSnapshot before = ServerRotationTracker.init_ch_CachePool();
                  float yaw = ServerRotationTracker.init_F();
                  float pitch = before.write();
                  if (before.init_Z() && Float.isFinite(yaw) && Float.isFinite(pitch)) {
                     Packet<?> packet = new ServerboundAttackPacket(target.getId());
                     if (init_Z_Packet_Packet(new Rot(yaw, pitch, player.onGround(), player.horizontalCollision), packet)
                        && init_Z_Packet_Packet(new PosRot(player.position(), yaw, pitch, player.onGround(), player.horizontalCollision), packet)) {
                        KnockbackThrowPlanner.Inner_NetHelper plan;
                        KBDisplacementModule.Inner_CachePool terrain;
                        try {
                           double extra = m.init_D_7();
                           terrain = new KBDisplacementModule.Inner_CachePool(level, target, pitch);
                           Vec3 nextEye = terrain.init_Vec3_F(yaw).add(0.0, MC.player.getEyeHeight(), 0.0);
                           AABB nextBody = target.getBoundingBox().move(init_Vec3_LivingEntity_2(target));
                           terrain.init_F = AimHitboxHelper.init(pitch, nextEye, nextBody, nextEye, nextBody);
                           terrain.init_Z = AimHitboxHelper.init(nextBody, nextEye, yaw, pitch, autismclient.util.AttackReachHelper.init_D(MC.player));
                           terrain.load_Z = SprintStateTracker.init_Z(MC.player.isSprinting()) && MC.player.getAttackStrengthScale(0.5F) > 0.9F
                              || autismclient.util.LegacyCombatDetector.init(MC.player);
                           KBDisplacementModule.Inner_StreamUtil struck = m.init_X_StreamUtil_LivingEntity(target, terrain, tick, m.init_Y.init_I(fallback));
                           terrain.init_Vec3 = struck.init_Vec3();
                           boolean grounded = struck.init_Z();
                           AABB body = struck.init_AABB();
                           double reference = grounded ? body.minY : terrain.load_D_AABB(body);
                           Vec3 server = m.init(target, grounded, body, reference, struck.init_I());
                           KnockbackThrowPlanner.Inner_ConfigStore input = new KnockbackThrowPlanner.Inner_ConfigStore(
                              body,
                              player.position().add(struck.init_Vec3()),
                              server,
                              grounded,
                              target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),
                              extra,
                              reference,
                              autismclient.util.LegacyCombatDetector.init()
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
                           KnockbackThrowPlanner.Inner_StateCache settings = new KnockbackThrowPlanner.Inner_StateCache(
                              m.integer("max-turn"), m.integer("fall-height"), m.bool("void-only"), side, m.integer("radius")
                           );
                           long started = System.nanoTime();
                           plan = KnockbackThrowPlanner.init(input, yaw, RotationMath.gcdStep(), settings, m.init_W_BufferHelper_LivingEntity(target, grounded, server), terrain);
                           m.init_V_J_W_NetHelper(System.nanoTime() - started, plan);
                        } catch (RuntimeException ignored) {
                           m.init_V_Str_2("error");
                           plain.run();
                           return;
                        }

                        if (plan == null) {
                           m.init_V_Str_2("plan:" + KnockbackThrowPlanner.init_Str);
                           plain.run();
                        } else {
                           double reach = autismclient.util.AttackReachHelper.init_D(MC.player) - 0.06;
                           Vec3 nextEye = terrain.init_Vec3_F(plan.init_F()).add(0.0, MC.player.getEyeHeight(), 0.0);
                           float inputYaw = SilentMovementCoordinator.getMovementYaw(MC.player);
                           if (!(target.getBoundingBox().move(init_Vec3_LivingEntity_2(target)).distanceToSqr(nextEye) > reach * reach)
                              && autismclient.util.SilentRotationManager.load(new RotationMath.Rotation(plan.init_F(), terrain.init_F))) {
                              m.init_V_Str_2("redirect");
                              m.load_ClientLevel = level;
                              m.init_ClientPacketListen = MC.getConnection();
                              m.init_Player = player;
                              m.load_I = MC.player.getInventory().getSelectedSlot();
                              m.read_I = tick;
                              m.init_Item = MC.player.getMainHandItem().getItem();
                              m.init_F = inputYaw;
                              m.load_F = plan.init_F();
                              m.read_F = terrain.init_F;
                              m.init_LivingEntity = target;
                              m.load_Z = plan.init_Z();
                              m.read_Z = !terrain.read(plan.init_F());
                              m.update_F = yaw;
                              m.sync_F = pitch;
                              m.write_F = Mth.wrapDegrees(plan.init_F() - yaw);
                              m.read_Str = plan.init_W_JsonUtil().init_W_TaskQueue()
                                 + (plan.load_Z() ? " robust" : "")
                                 + (KnockbackThrowPlanner.init_Z ? " strafe" : "")
                                 + " ticks="
                                 + plan.init_W_JsonUtil().init_I()
                                 + " lead="
                                 + String.format(Locale.ROOT, "%.2f,%.2f", m.init_Vec3.x, m.init_Vec3.z);
                              m.init_T.init(target, attack, autismclient.util.CombatAttackDispatcher.init_Runnable(target));
                           } else {
                              m.init_V_Str_2(
                                 target.getBoundingBox().move(init_Vec3_LivingEntity_2(target)).distanceToSqr(nextEye) > reach * reach
                                    ? "reach"
                                    : "rotation-busy"
                              );
                              plain.run();
                           }
                        }
                     } else {
                        m.init_V_Str_2("transport");
                        plain.run();
                     }
                  } else {
                     m.init_V_Str_2("no-wire-look");
                     plain.run();
                  }
               } else {
                  m.init_V_Str_2(m.init_I == tick ? "same-tick" : (m.write_I == tick ? "after-movement" : (WTappingModule.load() ? "w-tap" : "immune")));
                  plain.run();
               }
            }
         } else {
            m.init_V_Str_2("ineligible");
            plain.run();
         }
      } else {
         attack.run();
      }
   }

   private void init_V_LivingEntity_2(LivingEntity target) {
      this.init_Y.init_V(target.getId(), AutismSessionState.init_ck().state_I(), this.init_I_15());
   }

   private int init_I_15() {
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection != null && MC.player != null ? connection.getPlayerInfo(MC.player.getUUID()) : null;
      return Math.round(Math.max(0, info == null ? 0 : info.getLatency()) / 50.0F) + 1;
   }

   private KBDisplacementModule.Inner_StreamUtil init_X_StreamUtil_LivingEntity(LivingEntity target, KBDisplacementModule.Inner_CachePool terrain, int tick, int roundTrip) {
      Vec3 latest = EntityMotionTracker.init_Vec3_LivingEntity(target);
      AABB body = target.getBoundingBox().move(latest.subtract(target.position()));
      boolean grounded = target.onGround();
      if (!(target instanceof Player)) {
         return new KBDisplacementModule.Inner_StreamUtil(body, grounded, 0, Vec3.ZERO);
      }

      KBDisplacementModule.Inner_BufferHelper track = this.load_Map.get(target.getId());
      Vec3 motion = track == null ? Vec3.ZERO : track.load();
      this.init_Vec3 = Vec3.ZERO;
      if (motion.lengthSqr() < 1.0E-8) {
         return new KBDisplacementModule.Inner_StreamUtil(body, grounded, 0, Vec3.ZERO);
      }

      int age = Math.max(0, Math.min(4, tick - track.init_I()));
      int toServer = Math.max(0, Math.min(12, roundTrip + 1 + age));
      int toClient = Math.max(0, Math.min(12 - toServer, Math.round(init_I_LivingEntity(target) / 50.0F)));
      KnockbackThrowPlanner.Inner_DataUtil atServer = KnockbackThrowPlanner.init_W_DataUtil_AABB(body, motion, grounded, toServer, terrain);
      KnockbackThrowPlanner.Inner_DataUtil atClient = KnockbackThrowPlanner.init_W_DataUtil_AABB(atServer.init_AABB(), atServer.init_Vec3(), atServer.init_Z(), toClient, terrain);
      Vec3 shift = atClient.init_AABB().getCenter().subtract(atServer.init_AABB().getCenter());
      this.init_Vec3 = atClient.init_AABB().getCenter().subtract(body.getCenter());
      return new KBDisplacementModule.Inner_StreamUtil(atClient.init_AABB(), atServer.init_Z(), grounded ? atServer.init_I() : toServer, new Vec3(shift.x, 0.0, shift.z));
   }

   private static int init_I_LivingEntity(LivingEntity target) {
      ClientPacketListener connection = MC.getConnection();
      PlayerInfo info = connection == null ? null : connection.getPlayerInfo(target.getUUID());
      return info == null ? 0 : Math.max(0, Math.min(300, info.getLatency()));
   }

   private Vec3 init(LivingEntity target, boolean grounded, AABB body, double reference, int extraAir) {
      if (!(target instanceof Player)) {
         return init_Vec3_LivingEntity_2(target);
      }

      if (grounded) {
         return new Vec3(0.0, -0.0784, 0.0);
      }

      int air = Math.max(1, Math.min(60, AutismSessionState.init_ck().state_I() - this.load(target) + Math.max(0, extraAir)));
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

   private KnockbackThrowPlanner.Inner_BufferHelper init_W_BufferHelper_LivingEntity(LivingEntity target, boolean grounded, Vec3 server) {
      Vec3[] alternatives = new Vec3[0];
      if (!grounded && target instanceof Player) {
         Vec3 seen = init_Vec3_LivingEntity_2(target);
         alternatives = new Vec3[]{new Vec3(server.x, Math.max(-3.0, Math.min(3.0, seen.y)), server.z), new Vec3(server.x, -0.0784, server.z)};
      }

      KnockbackThrowPlanner.Inner_SyncManager counter = KnockbackThrowPlanner.Inner_SyncManager.init_W_SyncManager;
      double strafe = 0.0;
      if (target instanceof Player) {
         double speed = target.getAttributeValue(Attributes.MOVEMENT_SPEED);
         counter = KnockbackThrowPlanner.Inner_SyncManager.init(target.getYRot(), speed, target.isSprinting() ? 0.026 : 0.02);
         strafe = target.isSprinting() ? speed * 1.3 : speed;
      }

      return new KnockbackThrowPlanner.Inner_BufferHelper(counter, alternatives, strafe);
   }

   private int load(LivingEntity target) {
      Integer seen = this.init_Map.get(target.getId());
      return seen == null ? AutismSessionState.init_ck().state_I() - 1 : seen;
   }

   private void read_V_11() {
      if (MC.level != null && MC.player != null) {
         int tick = AutismSessionState.init_ck().state_I();
         this.init_Map.keySet().removeIf(id -> MC.level.getEntity(id) == null);
         this.load_Map.keySet().removeIf(id -> MC.level.getEntity(id) == null);

         for (Player other : MC.level.players()) {
            if (other != MC.player && !(other.distanceToSqr(MC.player) > 1024.0)) {
               if (other.onGround() || !this.init_Map.containsKey(other.getId())) {
                  this.init_Map.put(other.getId(), tick);
               }

               Vec3 latest = EntityMotionTracker.init_Vec3_LivingEntity(other);
               KBDisplacementModule.Inner_BufferHelper old = this.load_Map.get(other.getId());
               if (old == null) {
                  this.load_Map.put(other.getId(), new KBDisplacementModule.Inner_BufferHelper(latest, tick, Vec3.ZERO));
               } else if (latest.equals(old.init_Vec3())) {
                  if (tick - old.init_I() > 3 && old.load().lengthSqr() > 0.0) {
                     this.load_Map.put(other.getId(), new KBDisplacementModule.Inner_BufferHelper(old.init_Vec3(), old.init_I(), Vec3.ZERO));
                  }
               } else {
                  Vec3 motion = latest.subtract(old.init_Vec3()).scale(1.0 / Math.max(1, Math.min(4, tick - old.init_I())));
                  if (motion.horizontalDistanceSqr() > 2.25 || Math.abs(motion.y) > 4.0) {
                     motion = Vec3.ZERO;
                  }

                  this.load_Map.put(other.getId(), new KBDisplacementModule.Inner_BufferHelper(latest, tick, motion));
               }
            }
         }
      } else {
         this.init_Map.clear();
         this.load_Map.clear();
      }
   }

   private static boolean read_Z_11() {
      return "kb-displacement".equals(autismclient.util.SilentRotationManager.init_Str());
   }

   public static boolean init_Z_24() {
      return read_Z_11() && al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.init_T.init_Z();
   }

   public static boolean init_Z_Player(Player player, InteractionHand hand, Runnable swing) {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && !m.init_Z
         && player == m.init_Player
         && MC.level == m.load_ClientLevel
         && hand == InteractionHand.MAIN_HAND
         && m.init_T.init_Z_Runnable(swing);
   }

   public static float init_F_F_2(float camera) {
      return al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && m.init_T.init_Z() ? m.init_F : camera;
   }

   public static void init_V_21() {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m) {
         m.write_I = AutismSessionState.init_ck().state_I();
      }
   }

   @Override
   public void preMovementTick() {
      if (WTappingModule.init_bf_2() == this.init_bf) {
         this.init_bf.init_V();
      } else {
         this.init_bf.load_V();
      }

      if (read_Z_11() && MC.options.keyUse.isDown()) {
         this.sync_V_5();
         autismclient.util.SilentRotationManager.load_V();
      } else {
         int tick = AutismSessionState.init_ck().state_I();
         if (this.init_T.init_Z() && tick != this.read_I) {
            this.init_V_Z_5(tick == this.read_I + 1 && this.write_I == this.read_I);
         }

         if (!this.init_T.init_Z() && this.update_I != tick) {
            if (read_Z_11() && !autismclient.util.SilentRotationManager.init_Z()) {
               autismclient.util.SilentRotationManager.init(this.id());
            }

            this.write_V_11();
         }
      }
   }

   public static void load_V_17() {
      if (read_Z_11() && al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m && !m.init_T.init_Z()) {
         autismclient.util.SilentRotationManager.init_V_Str_LocalPlayer(m.id(), MC.player);
      }
   }

   private void init_V_Z_5(boolean displaced) {
      boolean context = this.sync_Z_5();
      float wireYaw = SilentMovementCoordinator.getCameraYaw();
      float wirePitch = SilentMovementCoordinator.getCameraPitch();
      if (!Float.isFinite(wireYaw) || !Float.isFinite(wirePitch)) {
         wireYaw = this.load_F;
         wirePitch = this.read_F;
      }

      displaced &= Math.abs(Mth.wrapDegrees(wireYaw - this.load_F)) < 1.0F && Math.abs(wirePitch - this.read_F) < 1.0F;
      float yaw = wireYaw;
      float pitch = wirePitch;
      this.init_Z = true;
      int queued = this.init_T.init_I();

      try {
         boolean flank = this.read_Z;
         int count = this.init_T
            .init(
               value -> context
                  && value instanceof LivingEntity target
                  && target == this.load_ClientLevel.getEntity(target.getId())
                  && this.init_Z_LivingEntity_2(target)
                  && (
                     AimHitboxHelper.init(target.getBoundingBox(), MC.player.getEyePosition(), yaw, pitch, autismclient.util.AttackReachHelper.init_D(MC.player), 0.002)
                        || flank
                           && AimHitboxHelper.init(
                              target.getBoundingBox(), MC.player.getEyePosition(), this.update_F, this.sync_F, autismclient.util.AttackReachHelper.init_D(MC.player), 0.002
                           )
                  ),
               value -> {
                  if (value instanceof LivingEntity target) {
                     this.init_V_LivingEntity_2(target);
                  }
               }
            );
         this.close += queued - count;
         if (flank && count > 0 && autismclient.util.SilentRotationManager.load(new RotationMath.Rotation(this.update_F, this.sync_F))) {
            this.update_I = AutismSessionState.init_ck().state_I();
         }

         if (displaced && count > 0) {
            long now = System.nanoTime();
            this.init_J += count;

            for (int i = 0; i < count; i++) {
               this.init_ArrayDeque.addLast(now);
            }

            this.load_Str = String.format(
               Locale.ROOT, "tick=%d turn=%.1f pitch=%.1f %s%s", AutismSessionState.init_ck().state_I(), this.write_F, this.read_F, this.read_Str, flank ? " flanked" : ""
            );
            if (this.load_Z) {
               this.write_J += count;
            } else {
               this.read_J += count;
            }

            this.load_J = System.nanoTime() + 2000000000L;
            this.init_ClientLevel = MC.level;
            this.init_U.init();
         }
      } finally {
         this.init_Z = false;
         this.write_V_11();
         this.read_Z = false;
      }
   }

   @Override
   public boolean load_Z_23() {
      return !SilentMovementCoordinator.isSafeWalkActive() && read_Z_11() && (this.init_T.init_Z() || this.update_I == AutismSessionState.init_ck().state_I() || autismclient.util.SilentRotationManager.init_Z());
   }

   private boolean sync_Z_5() {
      return MC.player != null
         && MC.player == this.init_Player
         && MC.level == this.load_ClientLevel
         && MC.getConnection() == this.init_ClientPacketListen
         && MC.player.getInventory().getSelectedSlot() == this.load_I
         && MC.player.getMainHandItem().is(this.init_Item);
   }

   private void write_V_11() {
      this.load_ClientLevel = null;
      this.init_ClientPacketListen = null;
      this.init_Player = null;
      this.init_Item = null;
      this.init_LivingEntity = null;
      this.load_Z = false;
   }

   public static void init_V_ClientboundDamageE_2(ClientboundDamageEventPacket packet) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && m.isEnabled()
         && MC.level != null
         && MC.player != null
         && packet.entityId() != MC.player.getId()) {
         m.init_Y.init(packet.entityId(), AutismSessionState.init_ck().state_I(), packet.getSource(MC.level).getEntity() == MC.player, m.init_I_15());
      }
   }

   private static boolean init_Z_AABB_4(AABB box, Vec3 eye, float yaw, float pitch) {
      return AimHitboxHelper.init(box, eye, yaw, pitch, autismclient.util.AttackReachHelper.init_D(MC.player));
   }

   public static Input init_Input_Input(Input input, float camera, float yaw) {
      if (al.init_Module_Str("kb-displacement") instanceof KBDisplacementModule m
         && m.isEnabled()
         && MC.player != null
         && MC.level == m.init_ClientLevel
         && System.nanoTime() < m.load_J
         && !MC.player.isInWater()
         && !MC.player.isInLava()
         && !MC.player.onClimbable()
         && !MC.player.isFallFlying()
         && !MC.player.isCrouching()
         && !input.shift()) {
         float friction = MC.level.getBlockState(BlockPos.containing(MC.player.getX(), MC.player.getY() - 0.500001, MC.player.getZ())).getBlock().getFriction();
         double drag = MC.player.onGround() ? friction * 0.91 : 0.91;
         return m.init_U.init(input, MC.player.position(), camera, yaw, drag, AutismSessionState.init_ck().state_I(), keys -> {
            boolean sprint = keys.forward() && !keys.backward() && (MC.player.isSprinting() || MovementModuleHooks.init(input.sprint(), false));
            double speed = MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            if (sprint != MC.player.isSprinting()) {
               speed *= sprint ? 1.3 : 0.7692307692307692;
            }

            double acceleration = MC.player.onGround() ? speed * (0.21600002F / (friction * friction * friction)) : (sprint ? 0.026 : 0.02);
            return KnockbackThrowPlanner.init(MC.player.getDeltaMovement(), keys, yaw, acceleration);
         });
      } else {
         return null;
      }
   }

   @Override
   public void tick() {
      if (!this.isEnabled() && read_Z_11()) {
         autismclient.util.SilentRotationManager.init_V_Str_LocalPlayer(this.id(), MC.player);
      }

      if (this.isEnabled()) {
         this.read_V_11();
         this.update_V_9();
      } else if (!this.init_Map.isEmpty()) {
         this.init_Map.clear();
      }
   }

   private void update_V_9() {
      if (MC.player != null
         && MC.level != null
         && this.read_ClientLevel != MC.level
         && MC.gui.screen() == null
         && MC.player.tickCount >= 100
         && MC.player.onGround()) {
         for (Player other : MC.level.players()) {
            if (other != MC.player && other.distanceToSqr(MC.player) < 12.25) {
               return;
            }
         }

         this.read_ClientLevel = MC.level;

         try {
            AABB body = MC.player.getBoundingBox();
            KnockbackThrowPlanner.Inner_ConfigStore input = new KnockbackThrowPlanner.Inner_ConfigStore(
               body, MC.player.position().add(0.0, 0.0, -1.0), new Vec3(0.0, -0.0784, 0.0), true, 0.0, 0.5, body.minY, false
            );
            KnockbackThrowPlanner.Inner_SyncManager counter = KnockbackThrowPlanner.Inner_SyncManager.init(180.0F, 0.13, 0.026);

            for (int radius : new int[]{3, 10}) {
               KnockbackThrowPlanner.init(
                  input,
                  MC.player.getYRot(),
                  RotationMath.gcdStep(),
                  new KnockbackThrowPlanner.Inner_StateCache(100.0, 3.0, false, 0, radius),
                  new KnockbackThrowPlanner.Inner_BufferHelper(counter, new Vec3[0]),
                  new KBDisplacementModule.Inner_CachePool(MC.level, MC.player, MC.player.getXRot())
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
      return read_Z_11();
   }

   private boolean init_Z_LivingEntity_2(LivingEntity target) {
      if (!MC.isSameThread()
         || MC.player == null
         || MC.level == null
         || MC.getConnection() == null
         || MC.gameMode == null
         || MC.gui.screen() != null
         || MC.gui.overlay() != null
         || MC.options.keyUse.isDown()
         || MC.options.keyInventory.isDown()
         || autismclient.util.ManualInteractionYield.init()
         || MC.gameMode instanceof AutismMultiPlayerGameModeAccessor digging && digging.autism$isDestroying() && !BedNukerModule.read_Z_10()
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
         || TeamsModule.init_Z_Entity_5(target)
         || AntiBotTracker.init_Z(target)) {
         return false;
      }

      if (ClutchModule.init_Z_49()
         || DangerModule.sync_Z_12()
         || SilentMovementCoordinator.isSafeWalkActive()
         || BedNukerModule.init_Z_23()
         || AutoTotemModule.init_Z_6()
         || AutoArmorModule.init_Z_20()
         || autismclient.util.macro.MacroAimController.init_Z()) {
         return false;
      }

      if (SilentMovementCoordinator.activeRotation() != null && !QueuedAttackBuffer.init_Z_Str(autismclient.util.SilentRotationManager.init_Str())) {
         return false;
      }

      double reach = autismclient.util.AttackReachHelper.init_D(MC.player);
      return target.getBoundingBox().distanceToSqr(MC.player.getEyePosition()) <= reach * reach && MC.player.hasLineOfSight(target);
   }

   private double init_D_7() {
      int level = EnchantmentHelper.getItemEnchantmentLevel(
         MC.level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.KNOCKBACK), MC.player.getMainHandItem()
      );
      return KnockbackThrowPlanner.init_D_D(
         MC.player.getAttributeValue(Attributes.ATTACK_KNOCKBACK),
         level,
         SprintStateTracker.init_Z(MC.player.isSprinting()),
         MC.player.getAttackStrengthScale(0.5F),
         autismclient.util.LegacyCombatDetector.init(MC.player)
      );
   }

   private static Vec3 init_Vec3_LivingEntity_2(LivingEntity target) {
      Vec3 motion = target.position().subtract(new Vec3(target.xOld, target.yOld, target.zOld));
      return target.tickCount > 1 ? motion : target.getDeltaMovement();
   }

   private static boolean init_Z_Packet_Packet(Packet<?> look, Packet<?> attack) {
      Module canceller = al.init_Module_Str("packet-canceller");
      if (!BlinkPacketManager.init_Z() && (!autismclient.util.macro.PingSpoofScheduler.read_Z() || !autismclient.util.macro.PingSpoofScheduler.load_Z()) && !AutismSessionState.init_ck().reset_Z()) {
         return canceller == null || !canceller.isEnabled() || !canceller.onPacketSend(look) && !canceller.onPacketSend(attack)
            ? autismclient.util.macro.PacketGateManager.init(look, "C2S") == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.PASS
               && autismclient.util.macro.PacketGateManager.init(attack, "C2S") == autismclient.util.macro.PacketGateManager.Inner_StreamUtil.PASS
            : false;
      } else {
         return false;
      }
   }

   private void sync_V_5() {
      this.init_T.init_V();
      this.write_V_11();
      this.init_ArrayDeque.clear();
      this.init_U.init();
      this.init_ClientLevel = null;
      this.load_J = 0L;
      this.init_I = -2147483648;
      this.update_I = -2147483648;
      this.read_Z = false;
   }

   @Override
   public void onEnable() {
      KBDisplacementWarmup.init();
      this.init_bf.read_V();
      this.sync_V_5();
      this.init_J = this.read_J = this.write_J = this.update_J = this.sync_J = this.flush = this.close = 0L;
      this.init_TreeMap.clear();
      this.init_Str = "none";
      this.init_Map.clear();
      this.init_Y.init();
      this.load_Map.clear();
   }

   @Override
   public void onDisable() {
      this.sync_V_5();
      this.init_bf.read_V();
      if (read_Z_11()) {
         autismclient.util.SilentRotationManager.init(this.id());
      }
   }

   @Override
   public void onGameLeft() {
      this.sync_V_5();
      this.init_bf.read_V();
      this.init_Map.clear();
      this.read_ClientLevel = null;
      this.init_Y.init();
      this.load_Map.clear();
      if (read_Z_11()) {
         autismclient.util.SilentRotationManager.load_V();
      }
   }

   @Override
   public void onGameJoin() {
      this.onGameLeft();
      this.write_I = -2147483648;
      this.init_Map.clear();
      if (this.isEnabled()) {
         KBDisplacementWarmup.init();
      }
   }

   @Override
   public String info() {
      if (!this.bool("array-info")) {
         return "";
      }

      long now = System.nanoTime();

      while (!this.init_ArrayDeque.isEmpty() && now - this.init_ArrayDeque.peekFirst() >= 10000000000L) {
         this.init_ArrayDeque.pollFirst();
      }

      int recent = this.init_ArrayDeque.size();
      return recent == 0 ? "" : (recent == 1 ? "1 redirect" : recent + " redirects");
   }

   private record Inner_BufferHelper(Vec3 init_Vec3, int init_I, Vec3 load) {
   }

   private static final class Inner_CachePool implements KnockbackThrowPlanner.Inner_UiHelper {
      private static final byte init_B = 1;
      private static final byte load_B = 2;
      private static final byte read_B = 3;
      private static final byte write_B = 4;
      final ClientLevel init_ClientLevel;
      final LivingEntity init_LivingEntity;
      final Map<Long, Boolean> init_Map = new HashMap<>();
      final Map<Long, ChunkAccess> load_Map = new HashMap<>();
      final Map<Long, BlockState> read_Map = new HashMap<>();
      final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
      final CollisionContext init_CollisionContext;
      float init_F;
      Vec3 init_Vec3 = Vec3.ZERO;
      boolean init_Z;
      boolean load_Z;
      final float load_F;
      final Map<Float, Vec3> write_Map = new HashMap<>();
      final int init_I;
      final int load_I;

      Inner_CachePool(ClientLevel level, LivingEntity target, float pitch) {
         this.init_ClientLevel = level;
         this.init_LivingEntity = target;
         this.init_F = pitch;
         this.init_CollisionContext = CollisionContext.of(target);
         this.load_F = SilentMovementCoordinator.getMovementYaw(Module.MC.player);
         this.init_I = level.getMinY();
         this.load_I = level.getMaxY();
      }

      private Vec3 init_Vec3_F(float yaw) {
         return this.write_Map
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
                  Input keys = SilentMovementCoordinator.correctInputForYaw(raw, this.load_F, yaw);
                  boolean sprint = keys.forward() && !keys.backward() && (Module.MC.player.isSprinting() || MovementModuleHooks.init(raw.sprint(), false));
                  double speed = Module.MC.player.getAttributeValue(Attributes.MOVEMENT_SPEED);
                  if (sprint != Module.MC.player.isSprinting()) {
                     speed *= sprint ? 1.3 : 0.7692307692307692;
                  }

                  BlockPos below = BlockPos.containing(Module.MC.player.getX(), Module.MC.player.getY() - 0.500001, Module.MC.player.getZ());
                  float friction = this.init_BlockState_BlockPos(below).getBlock().getFriction();
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

                  Vec3 movement = KnockbackThrowPlanner.init(velocity, keys, yaw, acceleration);
                  return !this.init_Z_AABB(Module.MC.player.getBoundingBox().expandTowards(movement))
                     ? Module.MC.player.position()
                     : Module.MC
                        .player
                        .position()
                        .add(Entity.collideBoundingBox(Module.MC.player, movement, Module.MC.player.getBoundingBox(), this.init_ClientLevel, List.of()));
               }
            );
      }

      @Override
      public KnockbackThrowPlanner.Inner_ConfigStore init_W_ConfigStore_W_ConfigStore_F(KnockbackThrowPlanner.Inner_ConfigStore input, float yaw) {
         return input.init(this.init_Vec3_F(yaw).add(this.init_Vec3));
      }

      @Override
      public boolean init_Z_F(float yaw) {
         return !this.init_Z ? this.read(yaw) : !this.load_Z || this.write(yaw);
      }

      boolean read(float yaw) {
         Vec3 next = this.init_Vec3_F(yaw).add(0.0, Module.MC.player.getEyeHeight(), 0.0);
         return KBDisplacementModule.init_Z_AABB_4(this.init_LivingEntity.getBoundingBox().move(KBDisplacementModule.init_Vec3_LivingEntity_2(this.init_LivingEntity)), next, yaw, this.init_F);
      }

      boolean write(float yaw) {
         Options o = Module.MC.options;
         Input raw = new Input(
            o.keyUp.isDown(), o.keyDown.isDown(), o.keyLeft.isDown(), o.keyRight.isDown(), o.keyJump.isDown(), o.keyShift.isDown(), o.keySprint.isDown()
         );
         if (raw.forward() && !raw.backward()) {
            Input keys = SilentMovementCoordinator.correctInputForYaw(raw, this.load_F, yaw);
            return keys.forward() && !keys.backward() && Math.abs(Mth.wrapDegrees(yaw - this.load_F)) <= 62.0;
         } else {
            return Math.abs(Mth.wrapDegrees(yaw - this.load_F)) <= 62.0;
         }
      }

      @Override
      public boolean load_Z_F(float yaw) {
         return this.init_Z_F(yaw);
      }

      @Override
      public double init() {
         return this.init_ClientLevel.getMinY();
      }

      @Override
      public boolean init_Z_AABB(AABB area) {
         area = area.inflate(1.0, 0.0, 1.0);
         if (!(area.minX < this.init_ClientLevel.getWorldBorder().getMinX())
            && !(area.maxX > this.init_ClientLevel.getWorldBorder().getMaxX())
            && !(area.minZ < this.init_ClientLevel.getWorldBorder().getMinZ())
            && !(area.maxZ > this.init_ClientLevel.getWorldBorder().getMaxZ())) {
            for (int x = Mth.floor(area.minX) >> 4; x <= Mth.floor(area.maxX) >> 4; x++) {
               for (int z = Mth.floor(area.minZ) >> 4; z <= Mth.floor(area.maxZ) >> 4; z++) {
                  int cx = x;
                  int cz = z;
                  if (!this.init_Map
                     .computeIfAbsent(
                        (long)x << 32 ^ z & 4294967295L, key -> this.init_ClientLevel.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null
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

      private ChunkAccess init_ChunkAccess_I_I(int cx, int cz) {
         long key = (long)cx << 32 ^ cz & 4294967295L;
         if (this.load_Map.containsKey(key)) {
            return this.load_Map.get(key);
         }

         ChunkAccess chunk = this.init_ClientLevel.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
         this.load_Map.put(key, chunk);
         return chunk;
      }

      private BlockState init_BlockState_ChunkAccess(ChunkAccess chunk, int x, int y, int z) {
         if (y >= this.init_I && y <= this.load_I) {
            int index = this.init_ClientLevel.getSectionIndex(y);
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

      BlockState init_BlockState_BlockPos(BlockPos at) {
         return this.read_Map.computeIfAbsent(at.asLong(), key -> this.init_ClientLevel.getBlockState(BlockPos.of(key)));
      }

      @Override
      public Vec3 init_Vec3_AABB_Vec3(AABB body, Vec3 motion) {
         return Entity.collideBoundingBox(this.init_LivingEntity, motion, body, this.init_ClientLevel, List.of());
      }

      @Override
      public double init_D_AABB(AABB body) {
         this.init_BlockPos_MutableBl.set(body.getCenter().x, body.minY - 0.500001, body.getCenter().z);
         return this.init_BlockState_BlockPos(this.init_BlockPos_MutableBl).getBlock().getFriction();
      }

      private static boolean init_Z_BlockState(BlockState state) {
         FluidState fluid = state.getFluidState();
         return !fluid.isEmpty() && !fluid.is(FluidTags.LAVA)
            || state.is(Blocks.COBWEB)
            || state.is(Blocks.POWDER_SNOW)
            || state.is(Blocks.SLIME_BLOCK)
            || state.is(Blocks.HONEY_BLOCK)
            || state.is(BlockTags.CLIMBABLE);
      }

      private static boolean load_Z_BlockState(BlockState state) {
         return state.getFluidState().is(FluidTags.LAVA);
      }

      private boolean init_Z_AABB_Predicate(AABB body, Predicate<BlockState> test) {
         AABB box = body.deflate(0.001);

         for (int x = Mth.floor(box.minX); x <= Mth.floor(box.maxX); x++) {
            for (int z = Mth.floor(box.minZ); z <= Mth.floor(box.maxZ); z++) {
               for (int y = Mth.floor(box.minY - 0.02); y <= Mth.floor(box.maxY); y++) {
                  this.init_BlockPos_MutableBl.set(x, y, z);
                  if (test.test(this.init_BlockState_BlockPos(this.init_BlockPos_MutableBl))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }

      @Override
      public boolean load_Z_AABB(AABB body) {
         return this.init_Z_AABB_Predicate(body, KBDisplacementModule.Inner_CachePool::init_Z_BlockState);
      }

      @Override
      public boolean read(AABB body) {
         return this.init_Z_AABB_Predicate(body, KBDisplacementModule.Inner_CachePool::load_Z_BlockState);
      }

      private static long init(int base, double lo, double hi) {
         return TerrainDropGrid.init_J_I(base, lo, hi);
      }

      @Override
      public TerrainDropGrid.Inner_StreamUtil init_V_StreamUtil_I(int x, int z, int base, double stepTop) {
         WorldBorder border = this.init_ClientLevel.getWorldBorder();
         if (!(x < border.getMinX()) && !(x + 1 > border.getMaxX()) && !(z < border.getMinZ()) && !(z + 1 > border.getMaxZ())) {
            ChunkAccess chunk = this.init_ChunkAccess_I_I(x >> 4, z >> 4);
            if (chunk == null) {
               return null;
            }

            long walls = 0L;
            long caught = 0L;

            for (int y = base; y < base + 8; y++) {
               BlockState state = this.init_BlockState_ChunkAccess(chunk, x, y, z);
               if (!state.isAir()) {
                  if (init_Z_BlockState(state)) {
                     caught |= init(base, y, y + 1);
                  }

                  this.init_BlockPos_MutableBl.set(x, y, z);
                  VoxelShape shape = state.getCollisionShape(this.init_ClientLevel, this.init_BlockPos_MutableBl, this.init_CollisionContext);
                  if (!shape.isEmpty()) {
                     if (shape == Shapes.block()) {
                        walls |= init(base, y, y + 1);
                     } else {
                        for (AABB box : shape.toAabbs()) {
                           walls |= init(base, y + box.minY, y + box.maxY);
                        }
                     }
                  }
               }
            }

            KBDisplacementModule.Inner_CachePool.Inner_DataUtil hit = this.init(chunk, x, z, stepTop, 0.0, 1.0, 0.0, 1.0);
            return new TerrainDropGrid.Inner_StreamUtil(walls, caught, hit.init_B == 1 ? hit.init_D : -1.0 / 0.0, hit.init_B, hit.init_F);
         } else {
            return null;
         }
      }

      private KBDisplacementModule.Inner_CachePool.Inner_DataUtil init(ChunkAccess chunk, int x, int z, double startTop, double fx0, double fx1, double fz0, double fz1) {
         int y = Math.min(this.load_I, Mth.floor(startTop - 0.001));
         LevelChunkSection[] sections = chunk.getSections();

         while (y >= this.init_I) {
            int index = this.init_ClientLevel.getSectionIndex(y);
            if (index < 0) {
               break;
            }

            if (index >= sections.length) {
               y = this.init_I + sections.length * 16 - 1;
            } else {
               LevelChunkSection section = sections[index];
               if (section != null && !section.hasOnlyAir()) {
                  BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                  if (!state.isAir()) {
                     if (init_Z_BlockState(state)) {
                        return new KBDisplacementModule.Inner_CachePool.Inner_DataUtil(y, y + 1, (byte)3, 0.6F);
                     }

                     double top = this.init(state, x, y, z, startTop, fx0, fx1, fz0, fz1);
                     if (top > -1.0 / 0.0) {
                        float friction = state.getBlock().getFriction();
                        if (y - 1 >= this.init_I) {
                           BlockState under = this.init_BlockState_ChunkAccess(chunk, x, y - 1, z);
                           double higher = under.isAir() ? -1.0 / 0.0 : this.init(under, x, y - 1, z, startTop, fx0, fx1, fz0, fz1);
                           if (higher > top) {
                              top = higher;
                              friction = under.getBlock().getFriction();
                           }
                        }

                        return new KBDisplacementModule.Inner_CachePool.Inner_DataUtil(y, top, (byte)1, friction);
                     }

                     if (load_Z_BlockState(state)) {
                        return new KBDisplacementModule.Inner_CachePool.Inner_DataUtil(y, y + 1, (byte)4, 0.6F);
                     }
                  }

                  y--;
               } else {
                  y = (y & -16) - 1;
               }
            }
         }

         return new KBDisplacementModule.Inner_CachePool.Inner_DataUtil(this.init_I - 1, -1.0 / 0.0, (byte)2, 0.6F);
      }

      private double init(BlockState state, int x, int y, int z, double startTop, double fx0, double fx1, double fz0, double fz1) {
         this.init_BlockPos_MutableBl.set(x, y, z);
         VoxelShape shape = state.getCollisionShape(this.init_ClientLevel, this.init_BlockPos_MutableBl, this.init_CollisionContext);
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
      public KnockbackThrowPlanner.Inner_CachePool init_W_CachePool_AABB(AABB body) {
         if (!this.init_Z_AABB(body)) {
            return new KnockbackThrowPlanner.Inner_CachePool(0.0, false, false, false);
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
               ChunkAccess chunk = this.init_ChunkAccess_I_I(x >> 4, z >> 4);
               if (chunk == null) {
                  return new KnockbackThrowPlanner.Inner_CachePool(0.0, false, false, false);
               }

               KBDisplacementModule.Inner_CachePool.Inner_DataUtil hit = this.init(
                  chunk,
                  x,
                  z,
                  body.minY,
                  Math.max(0.0, body.minX + 0.001 - x),
                  Math.min(1.0, body.maxX - 0.001 - x),
                  Math.max(0.0, body.minZ + 0.001 - z),
                  Math.min(1.0, body.maxZ - 0.001 - z)
               );
               if (hit.init_B == 1) {
                  floorTop = Math.max(floorTop, hit.init_D);
               } else if (hit.init_B == 3) {
                  catchTop = Math.max(catchTop, hit.init_D);
               } else if (hit.init_D > -1.0 / 0.0) {
                  lavaTop = Math.max(lavaTop, hit.init_D);
               }
            }
         }

         if (catchTop > -1.0 / 0.0 && catchTop >= floorTop - 1.0E-9 && catchTop >= lavaTop - 1.0E-9) {
            return new KnockbackThrowPlanner.Inner_CachePool(catchTop - 1.0, false, true, true);
         } else if (floorTop > -1.0 / 0.0 && floorTop >= lavaTop - 1.0E-9) {
            return new KnockbackThrowPlanner.Inner_CachePool(floorTop, false, false, true);
         } else {
            return lavaTop > -1.0 / 0.0
               ? new KnockbackThrowPlanner.Inner_CachePool(lavaTop, false, false, true, true)
               : new KnockbackThrowPlanner.Inner_CachePool(this.init_ClientLevel.getMinY(), true, false, true);
         }
      }

      double load_D_AABB(AABB body) {
         KnockbackThrowPlanner.Inner_CachePool below = this.init_W_CachePool_AABB(body);
         return below.read() && !below.init_Z() && !below.load() && !below.write() && below.init_D() >= body.minY - 3.0 && below.init_D() <= body.minY + 1.0E-6
            ? below.init_D()
            : body.minY;
      }

      private record Inner_DataUtil(int init_I, double init_D, byte init_B, float init_F) {
      }
   }

   private enum Inner_DataUtil {
      Auto,
      Left,
      Right;
   }

   private record Inner_StreamUtil(AABB init_AABB, boolean init_Z, int init_I, Vec3 init_Vec3) {
   }
}
