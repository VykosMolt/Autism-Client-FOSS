package autismclient.util.mm;

import autismclient.util.AutismClientMessaging;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.network.chat.HoverEvent.ShowText;

public final class NetHelper {
   private static final int init = 8161736;
   private static final int load = 10132122;
   private static final int read = 15132390;
   private static final int write = 16762967;
   private static final int update = 7332768;
   private static final int sync = 9347327;
   private static final int flush = 11579568;
   private static final int close = 16762967;

   private NetHelper() {
   }

   public static void init_V(JsonUtil line) {
      if (line != null) {
         try {
            AutismClientMessaging.send(init_Component(line));
         } catch (Throwable var2) {
         }
      }
   }

   public static Component init_Component(JsonUtil line) {
      MutableComponent root = Component.empty().append(init());
      if (line.load_Z) {
         return root.append(init(init(line.read, 96), 10132122, true));
      }

      if (!line.init_Z()) {
         return root.append(init_MutableComponent(line)).append(init(": ", 10132122, false)).append(init(init(line.read, 220), 15132390, false));
      }

      List<NetHelper.Inner_DataUtil> btns = new ArrayList<>();
      init(root, line, btns);
      if (!btns.isEmpty()) {
         int token = TaskQueue.init_I(line);

         for (NetHelper.Inner_DataUtil b : btns) {
            root.append(init("  ", 10132122, false)).append(init(b, token));
         }
      }

      return root;
   }

   private static MutableComponent init(NetHelper.Inner_DataUtil b, int token) {
      Style style = Style.EMPTY
         .withColor(b.init_I())
         .withClickEvent(new RunCommand("/autismcard " + b.load() + " " + token))
         .withHoverEvent(new ShowText(Component.literal(b.read())));
      return Component.literal("[" + b.init_Str() + "]").setStyle(style);
   }

   private static void init(MutableComponent root, JsonUtil line, List<NetHelper.Inner_DataUtil> btns) {
      boolean self = line.init_Z;
      switch (line.init_JsonUtil_DataUtil) {
         case read:
            int steps = line.init_DataUtil_NetHelper.init_I;
            root.append(init_MutableComponent(line)).append(init(" shared a macro", 10132122, false));
            if (line.init_DataUtil_NetHelper.init_Str != null && !line.init_DataUtil_NetHelper.init_Str.isBlank()) {
               root.append(init(" \"" + init(line.init_DataUtil_NetHelper.init_Str, 26) + "\"", 16762967, false));
            }

            root.append(init(" (" + steps + (steps == 1 ? " step)" : " steps)"), 10132122, false));
            if (!self) {
               btns.add(new NetHelper.Inner_DataUtil("Inspect", "inspect", "Review and save this macro to your library", 9347327));
            }

            btns.add(init_NetHelper_DataUtil("Copy the macro"));
            break;
         case update:
            boolean tpa = TaskQueue.init_Z(line);
            if (tpa) {
               if (self) {
                  root.append(init_MutableComponent(line)).append(init(" sent a TPA invite", 10132122, false));
               } else if (BufferHelper.init_Z_RenderState(BufferHelper.init_BufferHelper().init_RenderState_Str(line.init_Str))) {
                  root.append(init_MutableComponent(line)).append(init(" invited you to TPA", 15132390, false));
                  btns.add(new NetHelper.Inner_DataUtil("Accept", "run", "Send /tpa to accept", 7332768));
               } else {
                  root.append(init_MutableComponent(line))
                     .append(init(" invited you to TPA ", 10132122, false))
                     .append(init("(not on their server)", 10132122, true));
               }
            } else {
               root.append(init_MutableComponent(line))
                  .append(init(" shared a command ", 10132122, false))
                  .append(init(init(line.init_DataUtil_CachePool.init_Str, 44), 16762967, false));
               if (!self) {
                  btns.add(new NetHelper.Inner_DataUtil("Execute", "run", "Run this command", 7332768));
               }
            }

            btns.add(init_NetHelper_DataUtil("Copy the command"));
            break;
         case write:
            root.append(init_MutableComponent(line))
               .append(init(" shared a packet queue ", 10132122, false))
               .append(init("(" + init(line.init_Str(), 26) + ")", 16762967, false));
            if (!self) {
               btns.add(new NetHelper.Inner_DataUtil("Add to queue", "addqueue", "Add these packets to your queue", 7332768));
            }

            btns.add(new NetHelper.Inner_DataUtil("Inspect", "inspect", "Inspect the packet", 9347327));
            btns.add(init_NetHelper_DataUtil("Copy the packet data"));
            break;
         case sync:
            init(root, line, btns, self);
            break;
         default:
            root.append(init_MutableComponent(line)).append(init(": ", 10132122, false)).append(init(init(line.read, 200), 15132390, false));
      }
   }

   private static void init(MutableComponent root, JsonUtil line, List<NetHelper.Inner_DataUtil> btns, boolean self) {
      autismclient.util.mm.b.DataUtil.Inner_DataUtil b = line.init_DataUtil_DataUtil;
      switch (b.init_Str) {
         case "gui":
            root.append(init_MutableComponent(line))
               .append(init(" shared a GUI ", 10132122, false))
               .append(init("\"" + init(b.load, 26) + "\"", 16762967, false));
            btns.add(new NetHelper.Inner_DataUtil("View", "view", "View the shared GUI", 9347327));
            break;
         case "item":
            root.append(init_MutableComponent(line)).append(init(" shared an item ", 10132122, false)).append(init(init(b.load, 28), 16762967, false));
            btns.add(new NetHelper.Inner_DataUtil("Inspect", "inspect", "Inspect the shared item", 9347327));
            btns.add(init_NetHelper_DataUtil("Copy"));
            break;
         case "filter":
            root.append(init_MutableComponent(line))
               .append(init(" shared a packet filter ", 10132122, false))
               .append(init("(" + b.init_I + ")", 16762967, false));
            if (!self) {
               btns.add(new NetHelper.Inner_DataUtil("Import", "importfilter", "Merge this packet filter", 7332768));
            }

            btns.add(new NetHelper.Inner_DataUtil("Inspect", "inspect", "See the packets in this filter", 9347327));
            btns.add(init_NetHelper_DataUtil("Copy"));
            break;
         case "position":
            root.append(init_MutableComponent(line)).append(init(" shared a position ", 10132122, false)).append(init(init(b.load, 36), 16762967, false));
            btns.add(init_NetHelper_DataUtil("Copy the position"));
            break;
         case "server":
            String ip = ConfigStore.read_Str(b);
            int players = ConfigStore.read_I(b);
            int max = ConfigStore.write_I(b);
            int ping = ConfigStore.update_I(b);
            String stats = " · " + players + (max > 0 ? "/" + max : "") + " online" + (ping >= 0 ? " · " + ping + "ms" : "");
            root.append(init_MutableComponent(line))
               .append(init(" shared a server ", 10132122, false))
               .append(init(init(init_Str(ip), 30), 16762967, false))
               .append(init(stats, 10132122, false));
            if (!self && !BufferHelper.init_Z_Str(ip)) {
               btns.add(new NetHelper.Inner_DataUtil("Join", "join", "Join this server", 16762967));
            }

            btns.add(init_NetHelper_DataUtil("Copy the address"));
            break;
         case "steps":
            root.append(init_MutableComponent(line))
               .append(init(" shared macro steps ", 10132122, false))
               .append(init("(" + b.init_I + (b.init_I == 1 ? " step)" : " steps)"), 16762967, false));
            if (!self) {
               btns.add(new NetHelper.Inner_DataUtil("Import", "importsteps", "Import as a new macro", 7332768));
               btns.add(new NetHelper.Inner_DataUtil("To editor", "toeditor", "Open these steps in the macro editor", 9347327));
            }

            btns.add(init_NetHelper_DataUtil("Copy the steps"));
            break;
         case "module":
            root.append(init_MutableComponent(line))
               .append(init(" shared module settings ", 10132122, false))
               .append(init("\"" + init(b.load, 24) + "\"", 16762967, false));
            btns.add(new NetHelper.Inner_DataUtil("Preview", "previewmodule", "Preview the module settings", 9347327));
            if (!self) {
               btns.add(new NetHelper.Inner_DataUtil("Apply", "applymodule", "Apply these settings to your module", 7332768));
            }

            btns.add(init_NetHelper_DataUtil("Copy"));
            break;
         default:
            root.append(init_MutableComponent(line)).append(init(" shared something", 10132122, false));
            btns.add(init_NetHelper_DataUtil("Copy"));
      }
   }

   private static Component init() {
      return Component.literal("Lobby ").setStyle(Style.EMPTY.withColor(8161736)).append(Component.literal("· ").setStyle(Style.EMPTY.withColor(10132122)));
   }

   private static MutableComponent init_MutableComponent(JsonUtil line) {
      String n = init(BufferHelper.init_BufferHelper().init_Str_Str(line.init_Str), 24);
      MutableComponent c;
      if (BufferHelper.init_Z_Str_Z(line.init_Str, line.init_Z)) {
         c = Component.empty();
         int len = n.length();

         for (int i = 0; i < len; i++) {
            c.append(Component.literal(String.valueOf(n.charAt(i))).setStyle(Style.EMPTY.withColor(BufferHelper.init(line.init_Str, line.init_Z, i, len))));
         }
      } else {
         c = init(n, BufferHelper.init_I_Str_Z(line.init_Str, line.init_Z), false);
      }

      if (line.init_Z) {
         c.append(init(" (you)", 10132122, false));
      }

      return c;
   }

   private static MutableComponent init(String s, int color, boolean italic) {
      return Component.literal(s == null ? "" : s).setStyle(Style.EMPTY.withColor(color).withItalic(italic));
   }

   private static NetHelper.Inner_DataUtil init_NetHelper_DataUtil(String hover) {
      return new NetHelper.Inner_DataUtil("Copy", "copy", hover, 11579568);
   }

   private static String init(String s, int max) {
      if (s == null) {
         return "";
      }

      s = s.strip();
      return s.length() <= max ? s : s.substring(0, Math.max(1, max - 1)).strip() + "…";
   }

   private static String init_Str(String ip) {
      return ConfigStore.init_Str(ip);
   }

   private record Inner_DataUtil(String init_Str, String load, String read, int init_I) {
   }
}
