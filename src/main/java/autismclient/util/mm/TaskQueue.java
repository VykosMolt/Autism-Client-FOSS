package autismclient.util.mm;

import autismclient.gui.d.TaskTimer;
import autismclient.util.AutismFilterViewOverlay;
import autismclient.util.AutismGuiViewOverlay;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.AutismMacroEditorOverlay;
import autismclient.util.AutismModuleViewOverlay;
import autismclient.util.AutismPacketInspectOverlay;
import autismclient.util.AutismPacketLoggerOverlay;
import autismclient.util.aM;
import autismclient.util.bi;
import autismclient.util.bk;
import autismclient.util.cX;
import autismclient.util.o;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class TaskQueue {
   private static final Map<Integer, JsonUtil> init_Map = new ConcurrentHashMap<>();
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static final int init_I = 256;
   static final String init_Str = "autismcard";
   private static final int load = -13248397;
   private static final int read = -14249;
   private static final int write = -42149;

   private TaskQueue() {
   }

   public static int init_I(JsonUtil line) {
      int id = init_AtomicInteger.incrementAndGet();
      init_Map.put(id, line);
      if (init_Map.size() > 256) {
         int threshold = id - 256;
         init_Map.keySet().removeIf(k -> k <= threshold);
      }

      return id;
   }

   public static boolean init(String command) {
      if (command == null) {
         return false;
      }

      String c = command.trim();
      if (c.startsWith("/")) {
         c = c.substring(1).trim();
      }

      if (!c.equals("autismcard") && !c.startsWith("autismcard ")) {
         return false;
      }

      String rest = c.length() > "autismcard".length() ? c.substring("autismcard".length()).trim() : "";
      int sp = rest.indexOf(32);
      if (sp > 0) {
         String action = rest.substring(0, sp).trim();

         try {
            init(action, Integer.parseInt(rest.substring(sp + 1).trim()));
         } catch (NumberFormatException ignored) {
            bi.init("This shared item is no longer available.", -14249);
         }
      }

      return true;
   }

   public static void init(String action, int token) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.execute(() -> load(action, token));
      }
   }

   private static void load(String action, int token) {
      JsonUtil line = init_Map.get(token);
      if (line == null) {
         bi.init("This shared item is no longer available.", -14249);
      } else {
         try {
            switch (action) {
               case "import":
                  if (line.init_DataUtil_NetHelper != null) {
                     ByteReader.init(line.init_DataUtil_NetHelper);
                  }
                  break;
               case "run":
                  if (line.init_DataUtil_CachePool != null) {
                     BufferHelper.init_BufferHelper().init_V_DataUtil_CachePool(line.init_DataUtil_CachePool);
                  }
                  break;
               case "addqueue":
                  if (line.init_DataUtil_StateCach != null) {
                     init(line.init_DataUtil_StateCach);
                  }
                  break;
               case "importfilter":
                  if (line.init_DataUtil_DataUtil != null) {
                     int n = ConfigStore.init_I(line.init_DataUtil_DataUtil);
                     bi.init("Imported " + n + " filtered packet(s).", -13248397);
                  }
                  break;
               case "join":
                  if (line.init_DataUtil_DataUtil != null) {
                     update(line.init_DataUtil_DataUtil);
                  }
                  break;
               case "importsteps":
                  if (line.init_DataUtil_DataUtil != null) {
                     init(line.init_DataUtil_DataUtil);
                  }
                  break;
               case "toeditor":
                  if (line.init_DataUtil_DataUtil != null) {
                     load(line.init_DataUtil_DataUtil);
                  }
                  break;
               case "previewmodule":
                  if (line.init_DataUtil_DataUtil != null) {
                     read(line.init_DataUtil_DataUtil);
                  }
                  break;
               case "applymodule":
                  if (line.init_DataUtil_DataUtil != null) {
                     write(line.init_DataUtil_DataUtil);
                  }
                  break;
               case "copy":
                  init_V(line);
                  break;
               case "inspect":
                  load(line);
                  break;
               case "view":
                  read(line);
            }
         } catch (Throwable t) {
            bi.init("Action failed.", -42149);
         }
      }
   }

   private static void init(autismclient.util.mm.b.DataUtil.StateCache offer) {
      int n = ByteReader.init_I(offer);
      if (n < 0) {
         bi.init("Could not read shared queue.", -42149);
      } else {
         bi.init("Added " + n + " packet(s) to your queue.", -13248397);
      }
   }

   private static void init(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      autismclient.util.mm.b.DataUtil.NetHelper offer = new autismclient.util.mm.b.DataUtil.NetHelper();
      offer.read = b.read;
      offer.init_Str = b.load;
      ByteReader.init(offer);
   }

   private static void load(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      aM macro = o.init_aM(b.read);
      if (macro == null) {
         bi.init("Could not read the shared steps.", -42149);
      } else {
         AutismMacroEditorOverlay ed = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
         if (ed != null) {
            ed.load_V_aM(macro);
            init(ed, false);
         }
      }
   }

   private static void read(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         AutismModuleViewOverlay ov = new AutismModuleViewOverlay(mc.font);
         if (ov.init(b)) {
            init(ov, true);
         }
      }
   }

   private static void write(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      int n = ConfigStore.load_I(b);
      if (n < 0) {
         bi.init("Could not apply module settings.", -42149);
      } else {
         bi.init("Applied " + n + " setting(s) to " + ConfigStore.init_Str(b) + ".", -13248397);
      }
   }

   public static String init_Str(JsonUtil line) {
      String var10000;
      switch (line.init_JsonUtil_DataUtil) {
         case read:
            var10000 = line.init_DataUtil_NetHelper.read;
            break;
         case write:
            var10000 = line.init_DataUtil_StateCach.read;
            break;
         case update:
            var10000 = BufferHelper.init_Str_DataUtil_CachePool(line.init_DataUtil_CachePool);
            break;
         case sync:
            switch (line.init_DataUtil_DataUtil.init_Str) {
               case "server":
                  return ConfigStore.read_Str(line.init_DataUtil_DataUtil);
               case "position":
                  return line.init_DataUtil_DataUtil.read;
               case "item":
               case "filter":
               case "module":
                  return ConfigStore.update_Str(line.init_DataUtil_DataUtil);
               default:
                  return line.init_DataUtil_DataUtil.read;
            }
         default:
            var10000 = line.read;
      }

      return var10000;
   }

   private static void init_V(JsonUtil line) {
      String data = init_Str(line);
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.keyboardHandler.setClipboard(data == null ? "" : data);
      }

      bi.init("Copied to clipboard.");
   }

   private static void load(JsonUtil line) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         switch (line.init_JsonUtil_DataUtil) {
            case read:
               aM macro = o.init_aM(line.init_DataUtil_NetHelper.read);
               if (macro == null) {
                  bi.init("Could not read macro.", -42149);
                  return;
               }

               AutismMacroEditorOverlay ed = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
               if (ed == null) {
                  return;
               }

               ed.load_V_aM(macro);
               init(ed, false);
               break;
            case write:
               AutismPacketLoggerOverlay.Inner_JsonUtil entry = ByteReader.init_AutismPacketLogger(line.init_DataUtil_StateCach);
               if (entry == null) {
                  bi.init("Could not rebuild packet.", -42149);
                  return;
               }

               AutismPacketInspectOverlay ov = new AutismPacketInspectOverlay(mc.font);
               ov.init_V_AutismPacketLogger(entry, 60, 60);
               init(ov, true);
            case update:
            default:
               break;
            case sync:
               if ("item".equals(line.init_DataUtil_DataUtil.init_Str)) {
                  ItemStack st = ConfigStore.init_ItemStack(line.init_DataUtil_DataUtil);
                  if (st == null || st.isEmpty()) {
                     bi.init("Could not read item.", -42149);
                     return;
                  }

                  AutismItemNbtInspectOverlay ovNbt = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(mc.font);
                  if (ovNbt == null) {
                     return;
                  }

                  ovNbt.init_V_ItemStack(st, 60, 60);
                  init(ovNbt, true);
               } else if ("filter".equals(line.init_DataUtil_DataUtil.init_Str)) {
                  AutismFilterViewOverlay ovFilter = new AutismFilterViewOverlay(mc.font);
                  if (ovFilter.init(line.init_DataUtil_DataUtil)) {
                     init(ovFilter, true);
                  }
               }
         }
      }
   }

   private static void read(JsonUtil line) {
      if (line.init_DataUtil_DataUtil != null && "gui".equals(line.init_DataUtil_DataUtil.init_Str)) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            AutismGuiViewOverlay ov = new AutismGuiViewOverlay(mc.font);
            if (ov.init(line.init_DataUtil_DataUtil)) {
               init(ov, true);
            }
         }
      }
   }

   private static void update(autismclient.util.mm.b.DataUtil.Inner_DataUtil b) {
      if ("server".equals(b.init_Str)) {
         Minecraft mc = Minecraft.getInstance();
         Screen ret = mc == null ? null : mc.gui.screen();
         init(ConfigStore.read_Str(b), ConfigStore.load_Str(b), ret);
      }
   }

   public static void init(String rawIp, String displayName, Screen returnScreen) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         String ip = HashUtil.init(rawIp, 64);
         if (ip.isBlank()) {
            bi.init("No server address.", -14249);
         } else if (BufferHelper.init_Z_Str(ip)) {
            bi.init("You're already on that server.", -14249);
         } else {
            String shown = displayName != null && !displayName.isBlank() ? HashUtil.init(displayName, 64) : ip;
            mc.gui.setScreen(new ConfirmScreen(ok -> {
               if (ok) {
                  ServerData data = new ServerData(shown, ip, Type.OTHER);
                  ConnectScreen.startConnecting(returnScreen, mc, ServerAddress.parseString(ip), data, false, null);
               } else {
                  mc.gui.setScreen(returnScreen);
               }
            }, Component.literal("Join " + shown + "?"), Component.literal(ip + "\nOnly connect to servers you trust.")));
         }
      }
   }

   private static void init(cX ov, boolean background) {
      if (background) {
         bk.init_bk().init_V_cX_cX_DataUtil(ov, cX.Inner_DataUtil.write);
      } else {
         bk.init_bk().init_V(ov);
      }

      bk.init_bk().update_V(ov);
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.gui.setScreen(new TaskTimer(ov, null, false, true));
      }
   }

   static boolean init_Z(JsonUtil line) {
      return line.init_DataUtil_CachePool != null
         && line.init_DataUtil_CachePool.init_Str != null
         && line.init_DataUtil_CachePool.init_Str.trim().toLowerCase(Locale.ROOT).startsWith("tpa");
   }
}
