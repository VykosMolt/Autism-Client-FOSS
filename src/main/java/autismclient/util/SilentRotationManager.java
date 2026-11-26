package autismclient.util;

import java.util.Random;
import net.minecraft.client.player.LocalPlayer;

public final class SilentRotationManager {
   public static final float init_F = 72.0F;
   public static final float load_F = 37.0F;
   public static final float read_F = 37.0F;
   public static final float write_F = 2.0F;
   public static final int init_I = 5;
   public static final int load_I = 50;
   public static final int read_I = 40;
   public static final int write_I = 30;
   public static final int update_I = 25;
   public static final int sync_I = 20;
   public static final int flush_I = 18;
   public static final int close_I = 15;
   public static final int open_I = 10;
   public static final int poll_I = 11;
   public static final int push_I = 12;
   public static final int pop_I = 5;
   public static final int tick_I = 1;
   public static final String init_Str = "danger";
   public static final String load_Str = "bed-defender";
   public static final String read_Str = "surround";
   public static final String write_Str = "crystal-aura";
   public static final String update_Str = "anchor-aura";
   public static final String sync_Str = "auto-trap";
   public static final String flush_Str = "kill-aura";
   public static final String close_Str = "kb-displacement";
   public static final String open_Str = "auto-farm";
   public static final String poll_Str = "ai-assistant";
   private static final RotationStreamPlanner.Inner_TaskQueue init_ap_TaskQueue = new RotationStreamPlanner.Inner_TaskQueue();
   private static RotationMath.Rotation init_ce_DataUtil = null;
   private static RotationMath.Rotation load_ce_DataUtil = null;
   private static String push_Str = null;
   private static String pop_Str = null;
   private static int run_I = -2147483648;
   private static int get_I = -2147483648;
   private static int set = -2147483648;
   private static final float update_F = 0.05F;
   private static int put = 0;
   private static final int add = 27;
   private static int clear = 0;
   private static boolean init_Z = false;
   private static float sync_F = 37.0F;
   private static int reset = 27;
   private static float flush_F = 0.0F / 0.0F;
   private static float close_F = 0.0F / 0.0F;
   private static final Random init_Random = new Random();
   private static HumanizedAimStepper init_an;
   private static String tick_Str;
   private static int start = -2147483648;
   private static double init_D = 4.0;
   private static boolean load_Z;
   private static int stop = -2147483648;
   private static final double load_D = 18.0;
   private static final int next = 10;
   private static final double read_D = 2.5;
   private static final float open_F = 4.0F;
   private static final float poll_F = 38.0F;
   private static SilentRotationManager.Inner_DataUtil init_aD_DataUtil;
   private static String run_Str;
   private static String get_Str;
   private static int prev = -2147483648;
   private static double write_D = 18.0;

   private SilentRotationManager() {
   }

   public static void init_V(RotationMath.Rotation rotation) {
      init_V_Str("kill-aura", 10, rotation);
   }

   public static void init_V_Str(String ownerId, int priority, RotationMath.Rotation rotation) {
      if (ownerId != null && rotation != null) {
         int tick = AutismSessionState.init_ck().state_I();
         if (tick != get_I) {
            get_I = tick;
            pop_Str = null;
            run_I = -2147483648;
         }

         if (pop_Str == null || priority >= run_I && (priority != run_I || ownerId.equals(pop_Str))) {
            pop_Str = ownerId;
            run_I = priority;
            push_Str = ownerId;
            load_ce_DataUtil = rotation;
            put = 5;
            init_Z = false;
         }
      }
   }

   public static boolean init_Z(RotationMath.Rotation rotation) {
      if (AutismLiteVariant.enabled()) {
         return false;
      }

      if (rotation != null && Float.isFinite(rotation.yaw()) && Float.isFinite(rotation.pitch())) {
         init_V_Str("kill-aura", 10, rotation);
         if ("kill-aura".equals(push_Str) && "kill-aura".equals(pop_Str)) {
            ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
            float yaw = ServerRotationTracker.init_F();
            RotationMath.Rotation from = wire.init_Z() ? new RotationMath.Rotation(Float.isFinite(yaw) ? yaw : wire.read(), wire.write()) : rotation;
            init_ce_DataUtil = RotationMath.applyGcd(rotation, from);
            load_ce_DataUtil = init_ce_DataUtil;
            set = AutismSessionState.init_ck().state_I();
            load_Z = false;
            RotationStreamPlanner.init_V_ap_TaskQueue_3(init_ap_TaskQueue, init_ce_DataUtil, 0.0, 0.0, RotationMath.gcdStep());
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean load(RotationMath.Rotation rotation) {
      if (!AutismLiteVariant.enabled() && rotation != null) {
         ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
         if (!wire.init_Z()) {
            return false;
         }

         RotationMath.Rotation from = new RotationMath.Rotation(ServerRotationTracker.init_F(), wire.write());
         init_V_Str("kb-displacement", 11, rotation);
         if (!"kb-displacement".equals(push_Str)) {
            return false;
         }

         init_ce_DataUtil = RotationMath.applyGcd(rotation, from);
         load_ce_DataUtil = init_ce_DataUtil;
         set = AutismSessionState.init_ck().state_I();
         load_Z = false;
         RotationStreamPlanner.init_V_ap_TaskQueue_3(
            init_ap_TaskQueue, init_ce_DataUtil, RotationMath.angleDelta(init_ce_DataUtil.yaw(), from.yaw()), init_ce_DataUtil.pitch() - from.pitch(), RotationMath.gcdStep()
         );
         return true;
      } else {
         return false;
      }
   }

   public static void init_V_Str_D(String ownerId, double toleranceDegrees) {
      tick_Str = ownerId;
      start = AutismSessionState.init_ck().state_I();
      init_D = Math.max(0.5, toleranceDegrees);
   }

   private static boolean init_Z_Str_I(String ownerId, int tick) {
      return ownerId != null && ownerId.equals(tick_Str) && start == tick;
   }

   public static void init_V_Str_aD_DataUtil(String ownerId, SilentRotationManager.Inner_DataUtil ownerHand) {
      init_V_Str_2(ownerId, ownerHand, 18.0);
   }

   public static void init_V_Str_2(String ownerId, SilentRotationManager.Inner_DataUtil ownerHand, double accelUp) {
      init_aD_DataUtil = ownerHand;
      get_Str = ownerId;
      prev = AutismSessionState.init_ck().state_I();
      write_D = Math.max(18.0, accelUp);
   }

   private static boolean load(String ownerId, int tick) {
      return init_aD_DataUtil != null && ownerId != null && ownerId.equals(get_Str) && prev == tick;
   }

   private static RotationMath.Rotation init_ce_DataUtil_F_F(float maxYawStep, float maxPitchStep) {
      double[] last = RotationStreamPlanner.init_DArr_ap_TaskQueue(init_ap_TaskQueue);
      double count = RotationMath.gcdStep();
      maxYawStep = (float)Math.max(maxYawStep, last[0] - 18.0 + count);
      maxPitchStep = (float)Math.max(maxPitchStep, last[1] - 18.0 + count);
      maxPitchStep = (float)Math.min(maxPitchStep, last[1] + 18.0);
      RotationMath.Rotation at = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(init_ap_TaskQueue);
      double[] want = init_aD_DataUtil.step(at, maxYawStep, maxPitchStep);
      load_Z = true;
      stop = -1073741824;
      return RotationStreamPlanner.init(init_ap_TaskQueue, want[0], want[1], maxYawStep, maxPitchStep, RotationMath.gcdStep(), write_D, 18.0, CombatAimEngine.init_Z_DArr(want), "kill-aura".equals(get_Str));
   }

   public static void init_V_F_F(float exactYaw, float exactClosestYaw) {
      RotationStreamPlanner.init_V_ap_TaskQueue(init_ap_TaskQueue, exactYaw, exactClosestYaw);
   }

   private static RotationMath.Rotation init_ce_DataUtil_ce_DataUtil(
      RotationMath.Rotation goal, float maxYawStep, float maxPitchStep, double tolerance, int tick, boolean handBack
   ) {
      if (init_an == null) {
         init_an = HumanizedAimStepper.init_an();
      }

      if (!load_Z || tick - stop > 10) {
         init_an.init_V();
      }

      init_an.init_V_Z(handBack);
      stop = tick;
      load_Z = true;
      RotationMath.Rotation at = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(init_ap_TaskQueue);
      double[] want = init_an.init(goal.yaw(), goal.pitch(), at.yaw(), at.pitch(), tolerance, maxYawStep, maxPitchStep, true);
      return RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(init_ap_TaskQueue, want[0], want[1], maxYawStep, maxPitchStep, RotationMath.gcdStep(), 18.0);
   }

   public static double[] init_DArr() {
      return RotationStreamPlanner.init_DArr_ap_TaskQueue(init_ap_TaskQueue);
   }

   public static String init_Str() {
      return push_Str;
   }

   public static void init_V() {
      ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
      if (wire.init_Z()) {
         float yaw = ServerRotationTracker.init_F();
         if (!Float.isFinite(yaw)) {
            yaw = wire.read();
         }

         if (init_ce_DataUtil == null || !(Math.abs(RotationMath.angleDelta(init_ce_DataUtil.yaw(), yaw)) < 0.2) || !(Math.abs(init_ce_DataUtil.pitch() - wire.write()) < 0.2)) {
            init_ce_DataUtil = new RotationMath.Rotation(yaw, wire.write());
            RotationStreamPlanner.init_V_ap_TaskQueue_3(init_ap_TaskQueue, init_ce_DataUtil, RotationMath.angleDelta(wire.read(), wire.init_F()), wire.write() - wire.load(), RotationMath.gcdStep());
         }
      }
   }

   public static void load_V() {
      read_V();
      pop_Str = null;
      run_I = -2147483648;
      get_I = -2147483648;
      set = -2147483648;
   }

   private static void read_V() {
      init_ce_DataUtil = null;
      load_ce_DataUtil = null;
      push_Str = null;
      run_Str = null;
      put = 0;
      clear = 0;
      init_Z = false;
      load_Z = false;
      RotationStreamPlanner.init_V_ap_TaskQueue(init_ap_TaskQueue);
   }

   public static void init(String ownerId) {
      if (push_Str == null || push_Str.equals(ownerId)) {
         if (load_Z()) {
            read_V();
         } else {
            load_ce_DataUtil = null;
            put = 0;
            init_Z = init_ce_DataUtil != null;
            sync_F = 34.0F + init_Random.nextFloat() * 3.0F;
            reset = 22 + init_Random.nextInt(13);
            if (load_Z) {
               reset = 45 + init_Random.nextInt(15);
            }

            flush_F = 0.0F / 0.0F;
            close_F = 0.0F / 0.0F;
         }
      }
   }

   static boolean init_Z_F_F(float gapDegrees, float cameraDeltaDegrees) {
      return cameraDeltaDegrees >= 4.0F && gapDegrees + cameraDeltaDegrees <= 38.0F;
   }

   public static boolean init_Z() {
      return init_Z && init_ce_DataUtil != null;
   }

   public static boolean load_Z() {
      return run_Str != null && run_Str.equals(push_Str);
   }

   public static boolean read_Z() {
      return init_ce_DataUtil != null;
   }

   public static RotationMath.Rotation init_ce_DataUtil() {
      return init_ce_DataUtil;
   }

   public static void init_V_LocalPlayer_F(LocalPlayer player, float step) {
      init_V_Str_3("danger", player, step);
   }

   public static void init_V_Str_3(String requester, LocalPlayer player, float step) {
      if (player != null && ("danger".equals(requester) || "clutch".equals(requester)) && requester.equals(push_Str) && load_ce_DataUtil != null && put > 0) {
         int tick = AutismSessionState.init_ck().state_I();
         if (set != tick) {
            init_V();
            if (init_ce_DataUtil == null) {
               init_ce_DataUtil = RotationMath.ofPlayer(player);
            }

            init_ce_DataUtil = RotationMath.stepQuantized(init_ce_DataUtil, load_ce_DataUtil, step, step, RotationMath.gcdStep());
            RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(init_ap_TaskQueue, init_ce_DataUtil);
            load_Z = false;
            set = tick;
            put--;
         }
      }
   }

   public static SilentRotationManager.Inner_StreamUtil init_aD_StreamUtil_D_I(double remaining, int ticksLeft) {
      int n = Math.max(1, ticksLeft);
      double angle = Math.max(0.0, remaining);
      if ((!(angle <= 4.0) || n <= 1 && !(angle <= 1.0)) && !(n >= 2.0 + angle / 16.0)) {
         if (n >= 1.0 + angle / 30.0) {
            return new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.DANGER_RETURN, 37.0F, false);
         } else {
            return n >= 1.0 + angle / 68.0
               ? new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.TELLY_AIR_FLICK, 110.0F, false)
               : new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.STANDARD, (float)Math.min(180.0, Math.max(30.0, angle / n + 2.0)), true);
         }
      } else {
         return new SilentRotationManager.Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil.STANDARD, 40.0F, false);
      }
   }

   public static void init_V_Str_4(String requester, LocalPlayer player, int ticksLeft) {
      if (player != null && ("danger".equals(requester) || "clutch".equals(requester)) && requester.equals(push_Str) && load_ce_DataUtil != null && put > 0) {
         int tick = AutismSessionState.init_ck().state_I();
         if (set != tick) {
            init_V();
            if (init_ce_DataUtil == null) {
               init_ce_DataUtil = RotationMath.ofPlayer(player);
            }

            if (!RotationStreamPlanner.init_Z_ap_TaskQueue(init_ap_TaskQueue)) {
               RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(init_ap_TaskQueue, init_ce_DataUtil);
            }

            double remaining = Math.max(
               Math.abs(RotationMath.angleDelta(load_ce_DataUtil.yaw(), init_ce_DataUtil.yaw())), Math.abs(load_ce_DataUtil.pitch() - init_ce_DataUtil.pitch())
            );
            SilentRotationManager.Inner_StreamUtil urgency = init_aD_StreamUtil_D_I(remaining, ticksLeft);
            double gcd = RotationMath.gcdStep();
            if (urgency.init_Z()) {
               init_ce_DataUtil = RotationMath.stepQuantized(init_ce_DataUtil, load_ce_DataUtil, urgency.init_F(), urgency.init_F(), gcd);
               RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(init_ap_TaskQueue, init_ce_DataUtil);
            } else {
               init_ce_DataUtil = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue_2(
                  init_ap_TaskQueue, load_ce_DataUtil, urgency.init_F(), urgency.init_F(), gcd, false, urgency.init_ap_StreamUtil()
               );
            }

            load_Z = false;
            set = tick;
            put--;
         }
      }
   }

   public static void init_V_Str_LocalPlayer(String ownerId, LocalPlayer player) {
      init(ownerId, player, 72.0F, 72.0F);
   }

   public static void init_V_Str_5(String ownerId, LocalPlayer player, boolean pinQuiet) {
      init_V_Str(ownerId, player, 72.0F, 72.0F, RotationStreamPlanner.Inner_StreamUtil.STANDARD, pinQuiet);
   }

   public static void init(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep) {
      init(ownerId, player, maxYawStep, maxPitchStep, RotationStreamPlanner.Inner_StreamUtil.STANDARD);
   }

   public static void init(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep, RotationStreamPlanner.Inner_StreamUtil profile) {
      init_V_Str(ownerId, player, maxYawStep, maxPitchStep, profile, false);
   }

   private static void init_V_Str(String ownerId, LocalPlayer player, float maxYawStep, float maxPitchStep, RotationStreamPlanner.Inner_StreamUtil profile, boolean pinQuiet) {
      if (player == null) {
         load_V();
      } else if (push_Str == null || push_Str.equals(ownerId)) {
         int tick = AutismSessionState.init_ck().state_I();
         if (tick != set) {
            set = tick;
            RotationMath.Rotation playerRotation = RotationMath.ofPlayer(player);
            if (load_ce_DataUtil != null && put > 0) {
               boolean visible = autismclient.modules.al.init_Module_Str(ownerId) instanceof SilentMovementCoordinator.Participant aimOwner && aimOwner.update_Z_9();
               if (visible
                  && load_Z()
                  && !pinQuiet
                  && init_ce_DataUtil != null
                  && (playerRotation.yaw() != init_ce_DataUtil.yaw() || playerRotation.pitch() != init_ce_DataUtil.pitch())) {
                  init_ce_DataUtil = playerRotation;
                  RotationStreamPlanner.init_V_ap_TaskQueue_3(init_ap_TaskQueue, playerRotation, 0.0, 0.0, RotationMath.gcdStep());
               }

               if (!RotationStreamPlanner.init_Z_ap_TaskQueue(init_ap_TaskQueue)) {
                  RotationMath.Rotation seedFrom = init_ce_DataUtil;
                  if (seedFrom == null) {
                     ServerRotationTracker.RotationSnapshot wire = ServerRotationTracker.init_ch_CachePool();
                     float wireYaw = ServerRotationTracker.init_F();
                     seedFrom = wire.init_Z() ? new RotationMath.Rotation(Float.isFinite(wireYaw) ? wireYaw : wire.read(), wire.write()) : playerRotation;
                  }

                  RotationStreamPlanner.init_V_ap_TaskQueue_ce_DataUtil(init_ap_TaskQueue, seedFrom);
                  load_Z = false;
               }

               if (!pinQuiet
                  || init_ce_DataUtil == null
                  || (AutismLiteVariant.enabled() || !SilentMiningCoordinator.init(ownerId, init_ce_DataUtil, load_ce_DataUtil, init_ap_TaskQueue))
                     && (
                        !(Math.abs(RotationMath.angleDelta(init_ce_DataUtil.yaw(), load_ce_DataUtil.yaw())) <= 0.05F)
                           || !(Math.abs(init_ce_DataUtil.pitch() - load_ce_DataUtil.pitch()) <= 0.05F)
                     )) {
                  if (load(ownerId, tick)) {
                     init_ce_DataUtil = init_ce_DataUtil_F_F(maxYawStep, maxPitchStep);
                  } else if (init_Z_Str_I(ownerId, tick)) {
                     init_ce_DataUtil = init_ce_DataUtil_ce_DataUtil(load_ce_DataUtil, maxYawStep, maxPitchStep, init_D, tick, false);
                  } else {
                     load_Z = false;
                     init_ce_DataUtil = RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue_2(
                        init_ap_TaskQueue, load_ce_DataUtil, maxYawStep, maxPitchStep, RotationMath.gcdStep(), false, profile
                     );
                  }

                  init_V_LocalPlayer_Z(player, visible);
                  put--;
               } else {
                  put--;
                  init_V_LocalPlayer_Z(player, visible);
               }
            } else if (load_Z()) {
               read_V();
            } else {
               load_ce_DataUtil = null;
               if (init_ce_DataUtil == null) {
                  clear = 0;
                  init_Z = false;
               } else {
                  if (clear <= 0) {
                     clear = reset;
                  }

                  float cameraDelta = Float.isNaN(flush_F) ? 0.0F : RotationMath.vectorAngleDegrees(playerRotation, new RotationMath.Rotation(flush_F, close_F));
                  flush_F = playerRotation.yaw();
                  close_F = playerRotation.pitch();
                  boolean releaseNow = init_Z_F_F(RotationMath.vectorAngleDegrees(init_ce_DataUtil, playerRotation), cameraDelta);
                  RotationMath.Rotation next = releaseNow
                     ? playerRotation
                     : (
                        load_Z && RotationStreamPlanner.init_Z_ap_TaskQueue(init_ap_TaskQueue)
                           ? init_ce_DataUtil_ce_DataUtil(playerRotation, sync_F, sync_F, 2.5, tick, true)
                           : RotationStreamPlanner.init_ce_DataUtil_ap_TaskQueue(init_ap_TaskQueue, playerRotation, sync_F, sync_F, RotationMath.gcdStep(), false)
                     );
                  clear--;
                  if (!releaseNow && !(RotationMath.vectorAngleDegrees(next, playerRotation) <= 2.0F) && clear > 0) {
                     init_ce_DataUtil = next;
                  } else {
                     float fixedYaw = init_ce_DataUtil.yaw() + RotationMath.angleDelta(player.getYRot(), init_ce_DataUtil.yaw());
                     player.setYRot(fixedYaw);
                     player.yBob = fixedYaw;
                     player.yBobO = fixedYaw;
                     read_V();
                  }
               }
            }
         }
      }
   }

   private static void init_V_LocalPlayer_Z(LocalPlayer player, boolean visible) {
      run_Str = visible && init_ce_DataUtil != null ? push_Str : null;
      if (run_Str != null) {
         player.setYRot(init_ce_DataUtil.yaw());
         player.setXRot(init_ce_DataUtil.pitch());
      }
   }

   public interface Inner_DataUtil {
      double[] step(RotationMath.Rotation var1, float var2, float var3);
   }

   public record Inner_StreamUtil(RotationStreamPlanner.Inner_StreamUtil init_ap_StreamUtil, float init_F, boolean init_Z) {
   }
}
