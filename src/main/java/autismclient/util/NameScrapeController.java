package autismclient.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ServerData;

public final class NameScrapeController {
   private static final int init_I = 120000;
   private static final int load_I = 150000;
   private static final char[] SPINNER = new char[]{'|', '/', '-', '\\'};
   private static volatile boolean scraping;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile int namesFound;
   private static volatile int queriesSent;
   private static volatile int targetLimit;
   private static volatile boolean hasLimit;
   private static volatile boolean deepScan;
   private static volatile int onlineCount;
   private static volatile String statusText = "";
   private static volatile ClientPacketListener activeConnection;
   private static final CommandTabNameProber.ProbeControl init_be_DataUtil = new CommandTabNameProber.ProbeControl() {
      @Override
      public boolean isCancelled() {
         return NameScrapeController.read_Z();
      }

      @Override
      public boolean isPaused() {
         return NameScrapeController.load_Z && NameScrapeController.scraping;
      }

      @Override
      public int maxNames() {
         return NameScrapeController.targetLimit;
      }

      @Override
      public int maxQueries() {
         return 150000;
      }

      @Override
      public boolean write() {
         return NameScrapeController.deepScan;
      }

      @Override
      public void init(int names, int queries, int maxQueries, String status) {
         NameScrapeController.namesFound = names;
         NameScrapeController.queriesSent = queries;
         NameScrapeController.statusText = status == null ? "" : status;
      }
   };

   private NameScrapeController() {
   }

   public static boolean isRunning() {
      return scraping;
   }

   public static boolean load_Z() {
      return scraping && load_Z;
   }

   public static void start(int requestedLimit) {
      start(requestedLimit, false);
   }

   public static synchronized void start(int requestedLimit, boolean deep) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getConnection() == null || mc.player == null) {
         AutismClientMessaging.sendPrefixed("Namescrape: join a server first.");
      } else if (scraping) {
         AutismClientMessaging.sendPrefixed("Namescrape: already running. Use stop / pause / resume.");
      } else {
         scraping = true;
         load_Z = false;
         read_Z = false;
         deepScan = deep;
         namesFound = 0;
         queriesSent = 0;
         statusText = "Starting...";
         hasLimit = requestedLimit > 0;
         targetLimit = hasLimit ? Math.min(requestedLimit, 120000) : 120000;
         onlineCount = serverOnlineCount(mc);
         ClientPacketListener connection = mc.getConnection();
         activeConnection = connection;
         String self = mc.player.getName().getString();
         LinkedHashMap<String, String> names = CommandTabNameProber.probe(mc, self);
         namesFound = names.size();
         List<CommandTabNameProber.ProbeVector> vectors = CommandTabNameProber.discoverVectors(connection);
         BackgroundTaskExecutor.submit("namescrape", () -> runProbe(names, connection, self, vectors));
      }
   }

   public static void pause() {
      if (!scraping) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to pause.");
      } else {
         load_Z = true;
         AutismClientMessaging.sendPrefixed("Namescrape: paused (" + namesFound + " so far). Use resume.");
      }
   }

   public static void resume() {
      if (!scraping) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to resume.");
      } else {
         load_Z = false;
         AutismClientMessaging.sendPrefixed("Namescrape: resumed.");
      }
   }

   public static void stop() {
      if (!scraping) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to stop.");
      } else {
         read_Z = true;
         load_Z = false;
         scraping = false;
         AutismClientMessaging.sendPrefixed("Namescrape: stopping, copying " + namesFound + " so far...");
      }
   }

   private static void runProbe(LinkedHashMap<String, String> names, ClientPacketListener connection, String self, List<CommandTabNameProber.ProbeVector> vectors) {
      try {
         CommandTabNameProber.init(connection, names, self, vectors, init_be_DataUtil);
         List<String> result = new ArrayList<>(names.values());
         if (hasLimit && result.size() > targetLimit) {
            result = new ArrayList<>(result.subList(0, targetLimit));
         }

         deliverResult(result);
      } catch (Throwable t) {
         finishScrape("Namescrape failed.", "", 0);
      }
   }

   private static boolean read_Z() {
      if (scraping && !autismclient.modules.aD.read_Z()) {
         Minecraft mc = Minecraft.getInstance();
         return mc == null || mc.getConnection() != activeConnection;
      } else {
         return true;
      }
   }

   private static void deliverResult(List<String> result) {
      String joined = String.join("\n", result);
      int bytes = joined.getBytes(StandardCharsets.UTF_8).length;
      String kb = String.format(Locale.ROOT, "%.1f", bytes / 1024.0);
      if (result.isEmpty()) {
         finishScrape("Namescrape: no names found.", "", 0);
      } else {
         finishScrape(
            (read_Z ? "Namescrape stopped: " : "Namescrape: ") + String.format(Locale.US, "%,d", result.size()) + " names (" + kb + " KB) copied.",
            joined,
            result.size()
         );
      }
   }

   private static void finishScrape(String message, String clipboard, int count) {
      Minecraft mc = Minecraft.getInstance();
      Runnable done = () -> {
         scraping = false;
         load_Z = false;
         activeConnection = null;
         if (!autismclient.modules.aD.write_Z()) {
            if (count > 0 && mc != null && mc.keyboardHandler != null && !clipboard.isEmpty()) {
               mc.keyboardHandler.setClipboard(clipboard);
            }

            bi.load(message);
         }
      };
      if (mc != null) {
         mc.execute(done);
      } else {
         done.run();
      }
   }

   public static void render(GuiGraphicsExtractor graphics) {
      if (scraping) {
         Minecraft mc = Minecraft.getInstance();
         if (graphics != null && mc != null && mc.font != null) {
            autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, mc.font, -1, -1);
            autismclient.gui.ui.UiColorScheme colors = ctx.init_TableIndex().colors();
            int names = namesFound;
            char spin = SPINNER[(int)(System.currentTimeMillis() / 120L % SPINNER.length)];
            int width = 244;
            int height = 46;
            int pad = 9;
            int x = Math.max(4, (cA.init_I() - width) / 2);
            int y = 6;
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), colors.popupSurface, colors.borderStrong);
            String title = (load_Z ? "|| " : spin + " ") + (load_Z ? "Namescrape paused" : "Scraping names");
            int target = target();
            String count = formatCount(names) + (target > 0 ? " / " + formatCount(target) : "");
            ctx.init_PoolEntry().draw(graphics, title, x + pad, y + 6, colors.textPrimary);
            int cw = ctx.init_PoolEntry().measureWidth(count);
            ctx.init_PoolEntry().draw(graphics, count, x + width - pad - cw, y + 6, colors.textSecondary);
            autismclient.gui.ui.widgets.ProgressBarRenderer.init(ctx, autismclient.gui.f.NetHelper.init(x + pad, y + 20, width - pad * 2, 8), progress());
            String status = statusText != null && !statusText.isBlank() ? statusText : "Starting...";
            ctx.init_PoolEntry().draw(graphics, ellipsize(ctx, status, width - pad * 2), x + pad, y + 33, colors.textSecondary);
         }
      }
   }

   private static int target() {
      return hasLimit ? targetLimit : onlineCount;
   }

   private static double progress() {
      int target = target();
      return target > 0 ? Math.min(1.0, (double)namesFound / target) : namesFound / (namesFound + 1500.0);
   }

   private static int serverOnlineCount(Minecraft mc) {
      try {
         ServerData data = mc.getCurrentServer();
         if (data != null && data.players != null) {
            return Math.max(0, data.players.online());
         }
      } catch (Throwable var2) {
      }

      return 0;
   }

   private static String formatCount(int n) {
      return String.format(Locale.US, "%,d", n);
   }

   private static String ellipsize(autismclient.gui.f.SyncManager ctx, String text, int maxWidth) {
      if (ctx.init_PoolEntry().measureWidth(text) <= maxWidth) {
         return text;
      }

      String ellipsis = "...";
      int limit = maxWidth - ctx.init_PoolEntry().measureWidth(ellipsis);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < text.length() && ctx.init_PoolEntry().measureWidth(sb.toString() + text.charAt(i)) <= limit; i++) {
         sb.append(text.charAt(i));
      }

      return sb + ellipsis;
   }
}
