package autismclient.util;

import autismclient.AutismClientAddon;
import autismclient.util.macro.MacroExecutor;
import autismclient.util.macro.MacroWaitRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class LagWatchdogReporter {
   private static final int init_I = 20;
   private static final int load_I = 200;
   private static final boolean LAG_TEST = Boolean.getBoolean("autism.lagtest");
   private static final long LOW_FPS_HOLD_MS = LAG_TEST ? 3000L : 15000L;
   private static final long REPORT_INTERVAL_MS = LAG_TEST ? 30000L : 300000L;
   private static final int LOW_FPS_SAMPLES = LAG_TEST ? 5 : 30;
   private static int tickCounter;
   private static double baselineFps = -1.0;
   private static int lowFpsSamples;
   private static long lowFpsSince;
   private static long lastReportMs;
   private static boolean load_Z;
   private static Map<String, long[]> perfSnapshot;
   private static int lastFps;
   private static long lastPollMs;

   private LagWatchdogReporter() {
   }

   public static void init(Minecraft client) {
      try {
         bK.init_V();
         if (load_Z && !bK.init_Z()) {
            load_Z = false;
            logReport(client);
         }

         if (++tickCounter % 20 != 0) {
            return;
         }

         sample(client);
      } catch (Throwable t) {
         long now = System.currentTimeMillis();
         if (now - lastPollMs >= 5000L) {
            lastPollMs = now;
            AutismClientAddon.LOG.warn("[Autism] lag watchdog failed; isolated to protect the tick", t);
         }
      }
   }

   public static void init() {
      baselineFps = -1.0;
      lowFpsSamples = 0;
      lowFpsSince = 0L;
      load_Z = false;
      perfSnapshot = null;
   }

   private static void sample(Minecraft client) {
      if (client == null || client.level == null) {
         init();
      } else if (client.getWindow() == null || !client.getWindow().isFocused()) {
         lowFpsSince = 0L;
      } else if (autismclient.util.macro.MacroFpsOverrideManager.isActive()) {
         lowFpsSince = 0L;
      } else {
         int fps = client.getFps();
         long now = System.currentTimeMillis();
         if (fps > 0 && auditLow(fps, client.gui.screen() != null, now)) {
            if (!load_Z && now - lastReportMs >= REPORT_INTERVAL_MS) {
               lastReportMs = now;
               lastFps = fps;
               if (!bK.read_Z()) {
                  AutismClientAddon.LOG
                     .warn("[AutismLagReport] sustained low fps detected (fps={} baseline={})", fps, String.format(Locale.ROOT, "%.1f", baselineFps));
               } else {
                  perfSnapshot = bK.init_Map();
                  bK.init(200);
                  load_Z = true;
                  AutismClientAddon.LOG
                     .warn(
                        "[AutismLagReport] sustained low fps detected (fps={} baseline={}); sampling frame time for 10s...",
                        fps,
                        String.format(Locale.ROOT, "%.1f", baselineFps)
                     );
               }
            }
         }
      }
   }

   // True once fps has stayed below half the baseline for init_J ms. Menu frame rates must not enter the baseline.
   static boolean auditLow(int fps, boolean screenOpen, long now) {
      if (screenOpen) {
         lowFpsSince = 0L;
         return false;
      }

      if (baselineFps < 0.0) {
         baselineFps = fps;
      }

      double alpha = fps > baselineFps ? 0.2 : 0.02;
      baselineFps = baselineFps + alpha * (fps - baselineFps);
      if (lowFpsSamples < LOW_FPS_SAMPLES) {
         lowFpsSamples++;
         lowFpsSince = 0L;
      } else if (fps >= Math.max(15.0, baselineFps * 0.5)) {
         lowFpsSince = 0L;
      } else if (lowFpsSince == 0L) {
         lowFpsSince = now;
      } else {
         return now - lowFpsSince >= LOW_FPS_HOLD_MS;
      }

      return false;
   }

   private static void logReport(Minecraft client) {
      StringBuilder sb = new StringBuilder(1024);
      sb.append("===== lag census — paste this whole block when reporting =====");
      long heapUsedMb = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() >> 20;
      long heapMaxMb = Runtime.getRuntime().maxMemory() >> 20;
      long lowForMs = lowFpsSince == 0L ? 0L : System.currentTimeMillis() - lowFpsSince;
      appendLine(
         sb,
         "fps="
            + (client == null ? -1 : client.getFps())
            + " atTrigger="
            + lastFps
            + " baseline="
            + String.format(Locale.ROOT, "%.1f", baselineFps)
            + " lowForMs="
            + lowForMs
            + " heapMB="
            + heapUsedMb
            + "/"
            + heapMaxMb
      );
      appendField(
         sb,
         "state",
         () -> {
            boolean focused = client != null && client.getWindow() != null && client.getWindow().isFocused();
            Screen screen = client == null ? null : client.gui.screen();
            AutismSessionState shared = AutismSessionState.getInstance();
            boolean capture = shared != null && (shared.isTimedCaptureMode() || shared.hasCaptureCancelCallback() || shared.hasAttackCaptureCallback() || shared.hasBlockCaptureCallback() || shared.hasEntityCaptureCallback() || shared.isGbreakCapturing());
            return "focused="
               + focused
               + " screen="
               + (screen == null ? "none" : screen.getClass().getSimpleName())
               + " capture="
               + capture
               + " payloadStudy="
               + PayloadStudyTracker.isActive()
               + " packHide="
               + autismclient.modules.aD.init_Z();
         }
      );
      appendField(sb, "overlays", () -> OverlayManager.getInstance().debugSummary());
      appendField(sb, "packetLogger", () -> {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         AutismPacketLoggerOverlay logger = module == null ? null : module.getPacketLoggerOverlay();
         return logger == null ? "not created" : logger.debugSummary();
      });
      appendField(sb, "macro", () -> MacroExecutor.formatDebugSummary() + " pendingConditions=" + MacroWaitRegistry.pendingCount());
      appendField(sb, "antiVanish", autismclient.modules.AntiVanishModule::debugSummary);
      appendField(sb, "hud", () -> {
         return "enabledElements=" + AutismHudManager.enabledCount() + " activeModules=" + autismclient.modules.al.load_List().size() + " toasts=" + bi.init_I();
      });
      appendField(
         sb,
         "frame sections (10s window, window-total desc, avg/max* ms, n)",
         () -> {
            StringBuilder sections = new StringBuilder();
            Map<String, long[]> before = perfSnapshot;
            List<long[]> rows = new ArrayList<>();
            List<String> names = new ArrayList<>();

            for (Entry<String, long[]> entry : bK.init_Map().entrySet()) {
               long[] nowC = entry.getValue();
               long[] beforeC = before == null ? null : before.get(entry.getKey());
               long dSamples = nowC[0] - (beforeC == null ? 0L : beforeC[0]);
               long dTotal = nowC[1] - (beforeC == null ? 0L : beforeC[1]);
               if (dSamples > 0L) {
                  names.add(entry.getKey());
                  rows.add(new long[]{dTotal, dSamples, nowC[2], names.size() - 1});
               }
            }

            rows.sort((a, b) -> Long.compare(b[0], a[0]));
            int shown = Math.min(12, rows.size());

            for (int i = 0; i < shown; i++) {
               long[] row = rows.get(i);
               sections.append("\n[AutismLagReport]   ")
                  .append(names.get((int)row[3]))
                  .append(" avg=")
                  .append(String.format(Locale.ROOT, "%.3f", (double)row[0] / row[1] / 1000000.0))
                  .append(" max*=")
                  .append(String.format(Locale.ROOT, "%.3f", row[2] / 1000000.0))
                  .append(" n=")
                  .append(row[1]);
            }

            return sections.length() == 0 ? "(no samples)" : sections.toString();
         }
      );
      appendLine(sb, "===== end lag census =====");
      perfSnapshot = null;
      AutismClientAddon.LOG.warn("[AutismLagReport] {}", sb);
   }

   private static void appendLine(StringBuilder sb, String text) {
      sb.append("\n[AutismLagReport] ").append(text);
   }

   private static void appendField(StringBuilder sb, String label, Supplier<String> value) {
      String text;
      try {
         text = value.get();
      } catch (Throwable t) {
         text = "n/a (" + t.getClass().getSimpleName() + ")";
      }

      appendLine(sb, label + ": " + text);
   }
}
