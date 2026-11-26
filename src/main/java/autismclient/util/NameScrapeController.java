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
   private static final char[] init_CArr = new char[]{'|', '/', '-', '\\'};
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile int read_I;
   private static volatile int write_I;
   private static volatile int update_I;
   private static volatile boolean write_Z;
   private static volatile boolean update_Z;
   private static volatile int sync;
   private static volatile String init_Str = "";
   private static volatile ClientPacketListener init_ClientPacketListen;
   private static final CommandTabNameProber.Inner_DataUtil init_be_DataUtil = new CommandTabNameProber.Inner_DataUtil() {
      @Override
      public boolean init_Z() {
         return NameScrapeController.read_Z();
      }

      @Override
      public boolean load_Z() {
         return NameScrapeController.load_Z && NameScrapeController.init_Z;
      }

      @Override
      public int init_I() {
         return NameScrapeController.update_I;
      }

      @Override
      public int load_I() {
         return 150000;
      }

      @Override
      public boolean write() {
         return NameScrapeController.update_Z;
      }

      @Override
      public void init(int names, int queries, int maxQueries, String status) {
         NameScrapeController.read_I = names;
         NameScrapeController.write_I = queries;
         NameScrapeController.init_Str = status == null ? "" : status;
      }
   };

   private NameScrapeController() {
   }

   public static boolean init_Z() {
      return init_Z;
   }

   public static boolean load_Z() {
      return init_Z && load_Z;
   }

   public static void init_V(int requestedLimit) {
      init(requestedLimit, false);
   }

   public static synchronized void init(int requestedLimit, boolean deep) {
      Minecraft mc = Minecraft.getInstance();
      if (mc == null || mc.getConnection() == null || mc.player == null) {
         AutismClientMessaging.sendPrefixed("Namescrape: join a server first.");
      } else if (init_Z) {
         AutismClientMessaging.sendPrefixed("Namescrape: already running. Use stop / pause / resume.");
      } else {
         init_Z = true;
         load_Z = false;
         read_Z = false;
         update_Z = deep;
         read_I = 0;
         write_I = 0;
         init_Str = "Starting...";
         write_Z = requestedLimit > 0;
         update_I = write_Z ? Math.min(requestedLimit, 120000) : 120000;
         sync = init(mc);
         ClientPacketListener connection = mc.getConnection();
         init_ClientPacketListen = connection;
         String self = mc.player.getName().getString();
         LinkedHashMap<String, String> names = CommandTabNameProber.init(mc, self);
         read_I = names.size();
         List<CommandTabNameProber.Inner_StreamUtil> vectors = CommandTabNameProber.init_List_ClientPacketListen(connection);
         BackgroundTaskExecutor.init_Future_Str_Runnable("namescrape", () -> init(names, connection, self, vectors));
      }
   }

   public static void init_V() {
      if (!init_Z) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to pause.");
      } else {
         load_Z = true;
         AutismClientMessaging.sendPrefixed("Namescrape: paused (" + read_I + " so far). Use resume.");
      }
   }

   public static void load_V() {
      if (!init_Z) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to resume.");
      } else {
         load_Z = false;
         AutismClientMessaging.sendPrefixed("Namescrape: resumed.");
      }
   }

   public static void read_V() {
      if (!init_Z) {
         AutismClientMessaging.sendPrefixed("Namescrape: nothing to stop.");
      } else {
         read_Z = true;
         load_Z = false;
         init_Z = false;
         AutismClientMessaging.sendPrefixed("Namescrape: stopping, copying " + read_I + " so far...");
      }
   }

   private static void init(LinkedHashMap<String, String> names, ClientPacketListener connection, String self, List<CommandTabNameProber.Inner_StreamUtil> vectors) {
      try {
         CommandTabNameProber.init(connection, names, self, vectors, init_be_DataUtil);
         List<String> result = new ArrayList<>(names.values());
         if (write_Z && result.size() > update_I) {
            result = new ArrayList<>(result.subList(0, update_I));
         }

         init(result);
      } catch (Throwable t) {
         init("Namescrape failed.", "", 0);
      }
   }

   private static boolean read_Z() {
      if (init_Z && !autismclient.modules.aD.read_Z()) {
         Minecraft mc = Minecraft.getInstance();
         return mc == null || mc.getConnection() != init_ClientPacketListen;
      } else {
         return true;
      }
   }

   private static void init(List<String> result) {
      String joined = String.join("\n", result);
      int bytes = joined.getBytes(StandardCharsets.UTF_8).length;
      String kb = String.format(Locale.ROOT, "%.1f", bytes / 1024.0);
      if (result.isEmpty()) {
         init("Namescrape: no names found.", "", 0);
      } else {
         init(
            (read_Z ? "Namescrape stopped: " : "Namescrape: ") + String.format(Locale.US, "%,d", result.size()) + " names (" + kb + " KB) copied.",
            joined,
            result.size()
         );
      }
   }

   private static void init(String message, String clipboard, int count) {
      Minecraft mc = Minecraft.getInstance();
      Runnable done = () -> {
         init_Z = false;
         load_Z = false;
         init_ClientPacketListen = null;
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

   public static void init(GuiGraphicsExtractor graphics) {
      if (init_Z) {
         Minecraft mc = Minecraft.getInstance();
         if (graphics != null && mc != null && mc.font != null) {
            autismclient.gui.f.SyncManager ctx = autismclient.gui.f.UiHelper.init(graphics, mc.font, -1, -1);
            autismclient.gui.f.JsonUtil colors = ctx.init_TableIndex().init_JsonUtil();
            int names = read_I;
            char spin = init_CArr[(int)(System.currentTimeMillis() / 120L % init_CArr.length)];
            int width = 244;
            int height = 46;
            int pad = 9;
            int x = Math.max(4, (cA.init_I() - width) / 2);
            int y = 6;
            autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x, y, width, height), colors.sync, colors.run);
            String title = (load_Z ? "|| " : spin + " ") + (load_Z ? "Namescrape paused" : "Scraping names");
            int target = init_I();
            String count = init_Str(names) + (target > 0 ? " / " + init_Str(target) : "");
            ctx.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, title, x + pad, y + 6, colors.prev);
            int cw = ctx.init_PoolEntry().init(count);
            ctx.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, count, x + width - pad - cw, y + 6, colors.valid);
            autismclient.gui.f.b.NodeWalker.init(ctx, autismclient.gui.f.NetHelper.init(x + pad, y + 20, width - pad * 2, 8), init_D());
            String status = init_Str != null && !init_Str.isBlank() ? init_Str : "Starting...";
            ctx.init_PoolEntry().init_V_GuiGraphicsExtract_2(graphics, init(ctx, status, width - pad * 2), x + pad, y + 33, colors.valid);
         }
      }
   }

   private static int init_I() {
      return write_Z ? update_I : sync;
   }

   private static double init_D() {
      int target = init_I();
      return target > 0 ? Math.min(1.0, (double)read_I / target) : read_I / (read_I + 1500.0);
   }

   private static int init(Minecraft mc) {
      try {
         ServerData data = mc.getCurrentServer();
         if (data != null && data.players != null) {
            return Math.max(0, data.players.online());
         }
      } catch (Throwable var2) {
      }

      return 0;
   }

   private static String init_Str(int n) {
      return String.format(Locale.US, "%,d", n);
   }

   private static String init(autismclient.gui.f.SyncManager ctx, String text, int maxWidth) {
      if (ctx.init_PoolEntry().init(text) <= maxWidth) {
         return text;
      }

      String ellipsis = "...";
      int limit = maxWidth - ctx.init_PoolEntry().init(ellipsis);
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < text.length() && ctx.init_PoolEntry().init(sb.toString() + text.charAt(i)) <= limit; i++) {
         sb.append(text.charAt(i));
      }

      return sb + ellipsis;
   }
}
