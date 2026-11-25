package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.aM;
import autismclient.util.aN;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class PathHelper extends Command {
   public PathHelper() {
      super("multi", "Open the Multi menu, or launch/stop a profile.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         init_V();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("open").executes(ctx -> {
         init_V();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         load();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("status").executes(ctx -> {
         read();
         return 1;
      }));
      root.then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("launch").executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "multi launch <profile>");
         return 1;
      })).then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("profile", autismclient.commands.a.NetHelper.init_NetHelper()).executes(ctx -> {
         init(autismclient.commands.a.NetHelper.init(ctx, "profile"));
         return 1;
      })));
      root.then(((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("macro").executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "multi macro <macro> [bot] §7- no bot = all bots");
         return 1;
      })).then(((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("macro", autismclient.commands.a.CachePool.init_CachePool()).executes(ctx -> {
         init(autismclient.commands.a.CachePool.init(ctx, "macro"), "");
         return 1;
      })).then(RequiredArgumentBuilder.<AutismCommandSource, String>argument("bot", autismclient.commands.a.TaskQueue.init()).executes(ctx -> {
         init(autismclient.commands.a.CachePool.init(ctx, "macro"), autismclient.commands.a.TaskQueue.init(ctx, "bot"));
         return 1;
      }))));
   }

   private static void init(String macroName, String botName) {
      autismclient.util.g.PacketUtil manager = autismclient.util.g.PacketUtil.init_PacketUtil();
      if (!manager.load_Z()) {
         AutismClientMessaging.sendPrefixed("§7No Multi batch is active.");
      } else {
         aM macro = aN.init_aN().init_aM_Str(macroName);
         if (macro == null) {
            AutismClientMessaging.sendPrefixed("§cNo macro named: §f" + macroName);
         } else {
            Set<String> scope = manager.init_Set_Str(botName);
            if (scope == null) {
               AutismClientMessaging.sendPrefixed("§cNo bot named: §f" + botName);
               String live = init(manager);
               if (!live.isEmpty()) {
                  AutismClientMessaging.sendPrefixed("§7Bots: §f" + live);
               }
            } else {
               autismclient.util.g.PacketUtil.Inner_CachePool result = manager.init_PacketUtil_CachePo_aM_Set(macro, scope);
               String target = botName != null && !botName.isBlank() ? botName : "all bots";
               AutismClientMessaging.sendPrefixed("§aRunning §f" + macro.init_Str + " §aon §f" + target + " §7- " + result.init_Str());
            }
         }
      }
   }

   private static String init(autismclient.util.g.PacketUtil manager) {
      StringBuilder sb = new StringBuilder();

      for (autismclient.util.g.PacketUtil.Inner_StreamUtil bot : manager.read_List()) {
         if (sb.length() > 0) {
            sb.append("§7, §f");
         }

         sb.append(bot.load());
      }

      return sb.toString();
   }

   private static void init_V() {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         mc.execute(() -> {
            if (mc.gui.screen() == null) {
               Runnable proceed = () -> {
                  if (mc.level != null) {
                     autismclient.modules.PoolEntry module = autismclient.modules.PoolEntry.init_PoolEntry();
                     if (module != null) {
                        module.entry();
                     }
                  } else if (autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
                     mc.gui.setScreen(new autismclient.gui.d.TableIndex(null));
                  } else {
                     mc.gui.setScreen(new autismclient.gui.d.PathHelper(null, init(mc)));
                  }
               };
               autismclient.gui.d.RingBuffer.init(mc, null, proceed);
            }
         });
      }
   }

   private static void init(String name) {
      autismclient.util.g.FieldCache profile = init(autismclient.util.g.TaskTimer.init_TaskTimer().init_List(), name);
      if (profile == null) {
         AutismClientMessaging.sendPrefixed("§cNo profile named: §f" + name);
         String available = init_Str_3();
         if (!available.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§7Profiles: §f" + available);
         }
      } else {
         autismclient.util.g.PacketUtil.Inner_TextFormat result = autismclient.util.g.PacketUtil.init_PacketUtil().init_PacketUtil_TextFor_FieldCache(profile);
         if (result.init_Z()) {
            AutismClientMessaging.sendPrefixed(
               "§aLaunching Multi profile: §f"
                  + profile.write_Str
                  + " §7("
                  + profile.init_List.size()
                  + " account(s)) - run "
                  + autismclient.commands.DataUtil.init_Str()
                  + "multi to open the console."
            );
         } else {
            AutismClientMessaging.sendPrefixed("§cCould not launch: §f" + result.init_Str());
         }
      }
   }

   private static void load() {
      if (!autismclient.util.g.PacketUtil.init_PacketUtil().load_Z()) {
         AutismClientMessaging.sendPrefixed("§7No Multi batch is active.");
      } else {
         autismclient.util.g.PacketUtil.init_PacketUtil().load_V_Str("Stopped via command");
         AutismClientMessaging.sendPrefixed("§eStopped the Multi batch.");
      }
   }

   private static void read() {
      autismclient.util.g.PacketUtil mm = autismclient.util.g.PacketUtil.init_PacketUtil();
      if (!mm.load_Z()) {
         AutismClientMessaging.sendPrefixed("§7Multi: no batch active.");
      } else {
         autismclient.util.g.FieldCache active = mm.init_FieldCache();
         String name = active != null && active.write_Str != null ? active.write_Str : "(unknown)";
         AutismClientMessaging.sendPrefixed("§aMulti: §f" + name + " §7- §f" + mm.load_I() + "§7 ready, §f" + mm.init_I() + "§7 connected.");
      }
   }

   private static String init(Minecraft mc) {
      ServerData sd = mc.getCurrentServer();
      return sd != null && sd.ip != null ? sd.ip.trim() : "";
   }

   private static String init_Str_3() {
      StringBuilder sb = new StringBuilder();

      for (autismclient.util.g.FieldCache p : autismclient.util.g.TaskTimer.init_TaskTimer().init_List()) {
         if (p != null && p.write_Str != null) {
            if (sb.length() > 0) {
               sb.append("§7, §f");
            }

            sb.append(p.write_Str);
         }
      }

      return sb.toString();
   }

   public static autismclient.util.g.FieldCache init(Collection<autismclient.util.g.FieldCache> profiles, String name) {
      if (profiles != null && name != null) {
         String want = name.trim().toLowerCase(Locale.ROOT);
         if (want.isEmpty()) {
            return null;
         }

         for (autismclient.util.g.FieldCache p : profiles) {
            if (p != null && p.write_Str != null && p.write_Str.trim().toLowerCase(Locale.ROOT).equals(want)) {
               return p;
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
