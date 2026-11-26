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

public final class aF {
   private static final int init_I = 20;
   private static final int load_I = 200;
   private static final boolean init_Z = Boolean.getBoolean("autism.lagtest");
   private static final long init_J = init_Z ? 3000L : 15000L;
   private static final long load_J = init_Z ? 30000L : 300000L;
   private static final int read_I = init_Z ? 5 : 30;
   private static int write_I;
   private static double init_D = -1.0;
   private static int update_I;
   private static long read_J;
   private static long write_J;
   private static boolean load_Z;
   private static Map<String, long[]> init_Map;
   private static int sync;
   private static long update_J;

   private aF() {
   }

   public static void init(Minecraft client) {
      try {
         bK.init_V();
         if (load_Z && !bK.init_Z()) {
            load_Z = false;
            read(client);
         }

         if (++write_I % 20 != 0) {
            return;
         }

         load(client);
      } catch (Throwable t) {
         long now = System.currentTimeMillis();
         if (now - update_J >= 5000L) {
            update_J = now;
            AutismClientAddon.LOG.warn("[Autism] lag watchdog failed; isolated to protect the tick", t);
         }
      }
   }

   public static void init() {
      init_D = -1.0;
      update_I = 0;
      read_J = 0L;
      load_Z = false;
      init_Map = null;
   }

   private static void load(Minecraft client) {
      if (client == null || client.level == null) {
         init();
      } else if (client.getWindow() == null || !client.getWindow().isFocused()) {
         read_J = 0L;
      } else if (autismclient.util.macro.MacroFpsOverrideManager.init_Z()) {
         read_J = 0L;
      } else {
         int fps = client.getFps();
         long now = System.currentTimeMillis();
         if (fps > 0 && auditLow(fps, client.gui.screen() != null, now)) {
            if (!load_Z && now - write_J >= load_J) {
               write_J = now;
               sync = fps;
               if (!bK.read_Z()) {
                  AutismClientAddon.LOG
                     .warn("[AutismLagReport] sustained low fps detected (fps={} baseline={})", fps, String.format(Locale.ROOT, "%.1f", init_D));
               } else {
                  init_Map = bK.init_Map();
                  bK.init(200);
                  load_Z = true;
                  AutismClientAddon.LOG
                     .warn(
                        "[AutismLagReport] sustained low fps detected (fps={} baseline={}); sampling frame time for 10s...",
                        fps,
                        String.format(Locale.ROOT, "%.1f", init_D)
                     );
               }
            }
         }
      }
   }

   // True once fps has stayed below half the baseline for init_J ms. Menu frame rates must not enter the baseline.
   static boolean auditLow(int fps, boolean screenOpen, long now) {
      if (screenOpen) {
         read_J = 0L;
         return false;
      }

      if (init_D < 0.0) {
         init_D = fps;
      }

      double alpha = fps > init_D ? 0.2 : 0.02;
      init_D = init_D + alpha * (fps - init_D);
      if (update_I < read_I) {
         update_I++;
         read_J = 0L;
      } else if (fps >= Math.max(15.0, init_D * 0.5)) {
         read_J = 0L;
      } else if (read_J == 0L) {
         read_J = now;
      } else {
         return now - read_J >= init_J;
      }

      return false;
   }

   private static void read(Minecraft client) {
      StringBuilder sb = new StringBuilder(1024);
      sb.append("===== lag census — paste this whole block when reporting =====");
      long heapUsedMb = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() >> 20;
      long heapMaxMb = Runtime.getRuntime().maxMemory() >> 20;
      long lowForMs = read_J == 0L ? 0L : System.currentTimeMillis() - read_J;
      init(
         sb,
         "fps="
            + (client == null ? -1 : client.getFps())
            + " atTrigger="
            + sync
            + " baseline="
            + String.format(Locale.ROOT, "%.1f", init_D)
            + " lowForMs="
            + lowForMs
            + " heapMB="
            + heapUsedMb
            + "/"
            + heapMaxMb
      );
      init(
         sb,
         "state",
         () -> {
            boolean focused = client != null && client.getWindow() != null && client.getWindow().isFocused();
            Screen screen = client == null ? null : client.gui.screen();
            ck shared = ck.init_ck();
            boolean capture = shared != null && (shared.chunk() || shared.level() || shared.count() || shared.pool() || shared.frame() || shared.cache_Z());
            return "focused="
               + focused
               + " screen="
               + (screen == null ? "none" : screen.getClass().getSimpleName())
               + " capture="
               + capture
               + " payloadStudy="
               + bI.init_Z()
               + " packHide="
               + autismclient.modules.aD.init_Z();
         }
      );
      init(sb, "overlays", () -> bk.init_bk().init_Str());
      init(sb, "packetLogger", () -> {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.init_PoolEntry();
         AutismPacketLoggerOverlay logger = module == null ? null : module.load_AutismPacketLogger();
         return logger == null ? "not created" : logger.load_Str_9();
      });
      init(sb, "macro", () -> MacroExecutor.read_Str() + " pendingConditions=" + MacroWaitRegistry.init_I());
      init(sb, "antiVanish", autismclient.modules.AntiVanishModule::load_Str);
      init(sb, "hud", () -> {
         return "enabledElements=" + al.init_I() + " activeModules=" + autismclient.modules.al.load_List().size() + " toasts=" + bi.init_I();
      });
      init(
         sb,
         "frame sections (10s window, window-total desc, avg/max* ms, n)",
         () -> {
            StringBuilder sections = new StringBuilder();
            Map<String, long[]> before = init_Map;
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
      init(sb, "===== end lag census =====");
      init_Map = null;
      AutismClientAddon.LOG.warn("[AutismLagReport] {}", sb);
   }

   private static void init(StringBuilder sb, String text) {
      sb.append("\n[AutismLagReport] ").append(text);
   }

   private static void init(StringBuilder sb, String label, Supplier<String> value) {
      String text;
      try {
         text = value.get();
      } catch (Throwable t) {
         text = "n/a (" + t.getClass().getSimpleName() + ")";
      }

      init(sb, label + ": " + text);
   }
}
