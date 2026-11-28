package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class AiMotionAudit {
   private static final boolean ENABLED = Boolean.getBoolean("autism.ai.livetask")
      || Boolean.getBoolean("autism.ai.motionAudit")
      || Boolean.getBoolean("autism.ai.parkour")
      || Boolean.getBoolean("autism.ai.gather")
      || Boolean.getBoolean("autism.ai.combat");
   static final int init_I = 8;
   static final float init_F = 60.0F;
   private static final int read_I = 400;
   private static String operationId;
   private static Vec3 lastPos;
   private static float lastYaw;
   private static int operationTicks;
   private static int stillTicks;
   private static int totalStillTicks;
   private static int pauseCount;
   private static int longestPauseTicks;
   private static int snapCount;
   private static int reversalCount;
   private static int loggedLines;
   private static int lastTurnTick;
   private static float lastTurn;
   private static float worstSnap;
   private static String pauseActivity;
   private static String longestPauseActivity;
   private static BlockPos pausePos;
   static final float load_F = 25.0F;
   static final float read_F = 10.0F;
   static final int load_I = 12;
   private static final float[] yawHistory = new float[13];
   private static final float[] pitchHistory = new float[13];
   private static final String[] noteHistory = new String[13];
   private static int historyCount;
   private static int historyCursor;
   private static int thereAndBackCount;
   private static String pendingNote = "";
   private static final boolean NAV_TRACE = Boolean.getBoolean("autism.navigation.trace");
   private static int lastBusyTick = -2147483648;
   private static int lastSampleTick = -2147483648;
   private static Object lastSamplePlayer;

   public static String lastContext() {
      return pendingNote == null ? "" : pendingNote;
   }

   public static void note(String what) {
      if (ENABLED) {
         pendingNote = what;
      }
   }

   private AiMotionAudit() {
   }

   public static boolean isEnabled() {
      return ENABLED;
   }

   public static void markBusy(Minecraft mc) {
      if (ENABLED && mc.player != null) {
         lastBusyTick = mc.player.tickCount;
      }
   }

   public static void sample(Minecraft mc, String id, String activity) {
      if (ENABLED && mc.player != null && (mc.player.tickCount != lastSampleTick || mc.player != lastSamplePlayer)) {
         lastSampleTick = mc.player.tickCount;
         lastSamplePlayer = mc.player;
         if (!id.equals(operationId)) {
            finishOperation();
            operationId = id;
         }

         LocalPlayer player = mc.player;
         Vec3 pos = player.position();
         float yaw = player.getYRot();
         if (lastPos == null) {
            lastPos = pos;
            lastYaw = yaw;
         } else {
            operationTicks++;
            float turn = Mth.wrapDegrees(yaw - lastYaw);
            if (Math.abs(turn) > 60.0F) {
               snapCount++;
               worstSnap = Math.max(worstSnap, Math.abs(turn));
               logLine(String.format(Locale.ROOT, "snap %.0f deg at %s during '%s'", turn, player.blockPosition().toShortString(), clip(activity)));
            }

            recordLook(yaw, player.getXRot());
            if (Math.abs(turn) > 15.0F) {
               if (lastTurn != 0.0F && Math.signum(turn) != Math.signum(lastTurn) && operationTicks - lastTurnTick <= 6) {
                  reversalCount++;
                  logLine(
                     String.format(
                        Locale.ROOT,
                        "turn reversal %.0f then %.0f deg at %s during '%s'",
                        lastTurn,
                        turn,
                        player.blockPosition().toShortString(),
                        clip(activity)
                     )
                  );
               }

               lastTurn = turn;
               lastTurnTick = operationTicks;
            }

            boolean busy = mc.gui.screen() != null
               || player.isUsingItem()
               || mc.gameMode != null && mc.gameMode.isDestroying()
               || player.isSleeping()
               || player.tickCount - lastBusyTick <= 1;
            boolean still = pos.distanceToSqr(lastPos) < 1.0E-4;
            if (still && !busy) {
               if (stillTicks == 0) {
                  pauseActivity = activity;
                  pausePos = player.blockPosition();
               }

               stillTicks++;
               totalStillTicks++;
            } else {
               endPause();
            }

            if (NAV_TRACE) {
               AutismClientAddon.LOG
                  .info(
                     String.format(
                        Locale.ROOT,
                        "[AI] cam %d yaw %.1f pitch %.1f at %.2f %.2f %.2f keys %s%s%s%s%s body %s%s%s%s%s dig %s | %s%s",
                        operationTicks,
                        yaw,
                        player.getXRot(),
                        pos.x,
                        pos.y,
                        pos.z,
                        mc.options.keyUp.isDown() ? "W" : "",
                        mc.options.keyLeft.isDown() ? "A" : "",
                        mc.options.keyDown.isDown() ? "S" : "",
                        mc.options.keyRight.isDown() ? "D" : "",
                        mc.options.keyJump.isDown() ? "J" : "",
                        player.onGround() ? "g" : "-",
                        player.isSprinting() ? "s" : "-",
                        player.horizontalCollision ? "h" : "-",
                        player.verticalCollision && !player.verticalCollisionBelow ? "c" : "-",
                        player.onClimbable() ? "l" : "-",
                        AiDecisionAudit.isDigging(mc) ? "on" : "-",
                        clip(activity, 90),
                        pendingNote.isEmpty() ? "" : " | " + pendingNote
                     )
                  );
            }

            lastPos = pos;
            lastYaw = yaw;
            AiDecisionAudit.auditTick(mc, clip(activity, 90) + (pendingNote.isEmpty() ? "" : " | " + pendingNote));
            pendingNote = "";
         }
      }
   }

   public static void finishOperation() {
      if (ENABLED && operationId != null) {
         endPause();
         AiDecisionAudit.logSummary(operationId);
         if (operationTicks > 0) {
            AutismClientAddon.LOG
               .info(
                  String.format(
                     Locale.ROOT,
                     "[AI] motion %s: %.1f s, still %.1f s in %d pauses (longest %.1f s during '%s'), snaps %d (worst %.0f deg), reversals %d, there-and-backs %d",
                     operationId,
                     operationTicks / 20.0,
                     totalStillTicks / 20.0,
                     pauseCount,
                     longestPauseTicks / 20.0,
                     longestPauseActivity == null ? "-" : clip(longestPauseActivity),
                     snapCount,
                     worstSnap,
                     reversalCount,
                     thereAndBackCount
                  )
               );
         }

         operationId = null;
         lastPos = null;
         lastTurnTick = 0;
         loggedLines = 0;
         reversalCount = 0;
         snapCount = 0;
         longestPauseTicks = 0;
         pauseCount = 0;
         totalStillTicks = 0;
         stillTicks = 0;
         operationTicks = 0;
         thereAndBackCount = 0;
         historyCursor = 0;
         historyCount = 0;
         worstSnap = 0.0F;
         lastTurn = 0.0F;
         longestPauseActivity = null;
         pauseActivity = null;
      }
   }

   private static void recordLook(float yaw, float pitch) {
      int n = yawHistory.length;
      int[] flip = AiDecisionAudit.auditFlip(yawHistory, pitchHistory, historyCursor, historyCount, 12, 10.0F, 25.0F, yaw, pitch);
      if (flip != null) {
         int k = flip[0];
         int then = flip[1];
         int peak = flip[2];
         if (noteHistory[peak] == null || !noteHistory[peak].startsWith("cap ")) {
            thereAndBackCount++;
            logLine(
               String.format(
                  Locale.ROOT,
                  "there-and-back %.0f deg over %d ticks (yaw %.0f pitch %.0f -> yaw %.0f pitch %.0f -> back) out during '%s', back during '%s'",
                  angleBetween(yawHistory[peak], pitchHistory[peak], yawHistory[then], pitchHistory[then]),
                  k,
                  yawHistory[then],
                  pitchHistory[then],
                  yawHistory[peak],
                  pitchHistory[peak],
                  noteHistory[peak],
                  pendingNote
               )
            );
         }

         historyCount = 0;
      }

      yawHistory[historyCursor] = yaw;
      pitchHistory[historyCursor] = pitch;
      noteHistory[historyCursor] = pendingNote;
      historyCursor = (historyCursor + 1) % n;
      historyCount = Math.min(historyCount + 1, n);
   }

   private static float angleBetween(float yawA, float pitchA, float yawB, float pitchB) {
      float dy = Mth.wrapDegrees(yawA - yawB);
      float dp = pitchA - pitchB;
      return (float)Math.sqrt(dy * dy + dp * dp);
   }

   private static void endPause() {
      if (stillTicks >= 8) {
         pauseCount++;
         if (stillTicks > longestPauseTicks) {
            longestPauseTicks = stillTicks;
            longestPauseActivity = pauseActivity;
         }

         logLine(String.format(Locale.ROOT, "pause %.2f s at %s during '%s'", stillTicks / 20.0, pausePos.toShortString(), clip(pauseActivity)));
      }

      stillTicks = 0;
   }

   private static void logLine(String line) {
      if (++loggedLines <= 400) {
         AutismClientAddon.LOG.info("[AI] motion " + line);
      }
   }

   private static String clip(String s) {
      return clip(s, 160);
   }

   private static String clip(String s, int max) {
      if (s == null) {
         return "";
      } else {
         return s.length() > max ? s.substring(0, max) + "..." : s;
      }
   }
}
