package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aM;
import autismclient.util.aN;
import autismclient.util.macro.MacroExecutor;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class MacroCommand extends Command {
   private static final ConcurrentHashMap<String, AtomicBoolean> init = new ConcurrentHashMap<>();

   public MacroCommand() {
      super("macro", "Run a macro, optionally N times with a delay between starts.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.DataUtil.init_Str();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "macro <name> [times] [delayTicks]");
         AutismClientMessaging.sendPrefixed("§7Also: §f" + prefix + "macro stop [name] §7or §f" + prefix + "macro clear");
         return 1;
      });
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("stop")
               .executes(
                  ctx -> {
                     String pov = AutismLiteVariant.enabled() ? null : autismclient.util.g.ValueSink.init_Str();
                     if (!AutismLiteVariant.enabled() && pov != null) {
                        init(pov);
                        autismclient.util.g.PacketUtil mgr = autismclient.util.g.PacketUtil.load_PacketUtil();
                        autismclient.util.g.PacketUtil.Inner_CachePool result = mgr == null
                           ? new autismclient.util.g.PacketUtil.Inner_CachePool(0, 1, 0, List.of("Multi unavailable"))
                           : mgr.load_PacketUtil_CachePo_Set(Set.of(pov));
                        AutismClientMessaging.sendPrefixed("§ePOV macro stop: §f" + result.init_Str());
                        return 1;
                     } else {
                        init();
                        aN.init_aN().init_V();
                        AutismClientMessaging.sendPrefixed("§eStopped all macros.");
                        return 1;
                     }
                  }
               ))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", autismclient.commands.a.CachePool.init_CachePool())
                  .executes(
                     ctx -> {
                        String name = autismclient.commands.a.CachePool.init(ctx, "name");
                        String pov = AutismLiteVariant.enabled() ? null : autismclient.util.g.ValueSink.init_Str();
                        if (!AutismLiteVariant.enabled() && pov != null) {
                           AtomicBoolean flag = init.remove(init(pov, name));
                           if (flag != null) {
                              flag.set(false);
                           }

                           autismclient.util.g.PacketUtil mgr = autismclient.util.g.PacketUtil.load_PacketUtil();
                           autismclient.util.g.PacketUtil.Inner_CachePool result = mgr == null
                              ? new autismclient.util.g.PacketUtil.Inner_CachePool(0, 1, 0, List.of("Multi unavailable"))
                              : mgr.load_PacketUtil_CachePo_Set(Set.of(pov));
                           AutismClientMessaging.sendPrefixed("§eStopped POV macro §f" + name + "§e: " + result.init_Str());
                           return 1;
                        } else {
                           AtomicBoolean flag = init.remove(name.toLowerCase());
                           if (flag != null) {
                              flag.set(false);
                           }

                           MacroExecutor.init_V_Str(name);
                           AutismClientMessaging.sendPrefixed("§eStopped: §f" + name);
                           return 1;
                        }
                     }
                  )
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("clear").executes(ctx -> {
         String pov = AutismLiteVariant.enabled() ? null : autismclient.util.g.ValueSink.init_Str();
         if (!AutismLiteVariant.enabled() && pov != null) {
            init(pov);
            AutismClientMessaging.sendPrefixed("§eCleared POV macro loops for §f" + pov + "§e.");
         } else {
            init();
            AutismClientMessaging.sendPrefixed("§eCleared macro loops.");
         }

         return 1;
      }));
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", autismclient.commands.a.CachePool.init_CachePool())
               .executes(ctx -> init(autismclient.commands.a.CachePool.init(ctx, "name"), 1, 0)))
            .then(
               ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("times", IntegerArgumentType.integer(1, 100000))
                     .suggests(autismclient.commands.StreamUtil::close)
                     .executes(ctx -> init(autismclient.commands.a.CachePool.init(ctx, "name"), IntegerArgumentType.getInteger(ctx, "times"), 0)))
                  .then(
                     RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("delayTicks", IntegerArgumentType.integer(0, 72000))
                        .suggests(autismclient.commands.StreamUtil::open)
                        .executes(
                           ctx -> init(
                              autismclient.commands.a.CachePool.init(ctx, "name"),
                              IntegerArgumentType.getInteger(ctx, "times"),
                              IntegerArgumentType.getInteger(ctx, "delayTicks")
                           )
                        )
                  )
            )
      );
   }

   private static int init(String name, int times, int delayTicks) {
      aM macro = aN.init_aN().init_aM_Str(name);
      if (macro == null) {
         AutismClientMessaging.sendPrefixed("§cMacro not found: §f" + name);
         return 1;
      } else {
         String pov = AutismLiteVariant.enabled() ? null : autismclient.util.g.ValueSink.init_Str();
         if (!AutismLiteVariant.enabled() && pov != null) {
            return init(name, macro, times, delayTicks, pov);
         } else if (times <= 1 && delayTicks <= 0) {
            aN.init_aN().load_V(name);
            return 1;
         } else {
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            if (connection == null) {
               AutismClientMessaging.sendPrefixed("§cNot connected.");
               return 1;
            }

            AtomicBoolean flag = new AtomicBoolean(true);
            AtomicBoolean previous = init.put(name.toLowerCase(), flag);
            if (previous != null) {
               previous.set(false);
            }

            long sleepMs = Math.max(0L, delayTicks * 50L);
            int totalTimes = Math.max(1, times);
            Thread t = new Thread(() -> {
               try {
                  for (int i = 0; i < totalTimes && flag.get() && Minecraft.getInstance().getConnection() == connection; i++) {
                     aN.init_aN().load_V(name);
                     long start = System.nanoTime();
                     long waitTimeoutNanos = 86400000000000L;

                     while (MacroExecutor.init_Z_Str(name) && flag.get() && System.nanoTime() - start < waitTimeoutNanos) {
                        Thread.sleep(50L);
                     }

                     if (!flag.get() || Minecraft.getInstance().getConnection() != connection) {
                        break;
                     }

                     if (i + 1 < totalTimes && sleepMs > 0L) {
                        Thread.sleep(sleepMs);
                     }
                  }
               } catch (InterruptedException ignored) {
                  Thread.currentThread().interrupt();
               } finally {
                  init.remove(name.toLowerCase(), flag);
               }
            }, "MacroLoop-" + name);
            t.setDaemon(true);
            t.start();
            AutismClientMessaging.sendPrefixed("§aQueued " + totalTimes + "x §f" + name + "§a (delay " + delayTicks + "t)");
            return 1;
         }
      }
   }

   private static int init(String name, aM macro, int times, int delayTicks, String accountId) {
      if (AutismLiteVariant.enabled()) {
         return 1;
      }

      autismclient.util.g.PacketUtil mgr = autismclient.util.g.PacketUtil.load_PacketUtil();
      if (mgr == null) {
         AutismClientMessaging.sendPrefixed("§cMulti is unavailable.");
         return 1;
      }

      Set<String> scope = Set.of(accountId);
      if (times <= 1 && delayTicks <= 0) {
         autismclient.util.g.PacketUtil.Inner_CachePool result = mgr.init_PacketUtil_CachePo_aM_Set(macro, scope);
         AutismClientMessaging.sendPrefixed((result.init_I() > 0 ? "§a" : "§c") + "POV macro §f" + name + "§a: " + result.init_Str());
         return 1;
      }

      String key = init(accountId, name);
      AtomicBoolean flag = new AtomicBoolean(true);
      AtomicBoolean previous = init.put(key, flag);
      if (previous != null) {
         previous.set(false);
      }

      long sleepMs = Math.max(0L, delayTicks * 50L);
      int totalTimes = Math.max(1, times);
      Thread thread = new Thread(() -> {
         try {
            for (int i = 0; i < totalTimes && flag.get(); i++) {
               mgr.init_PacketUtil_CachePo_aM_Set(macro, scope);
               long start = System.nanoTime();
               long timeout = 86400000000000L;

               while (mgr.load_Z(name, scope) && flag.get() && System.nanoTime() - start < timeout) {
                  Thread.sleep(50L);
               }

               if (!flag.get()) {
                  break;
               }

               if (i + 1 < totalTimes && sleepMs > 0L) {
                  Thread.sleep(sleepMs);
               }
            }
         } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
         } finally {
            init.remove(key, flag);
         }
      }, "PovMacroLoop-" + name);
      thread.setDaemon(true);
      thread.start();
      AutismClientMessaging.sendPrefixed("§aQueued POV §f" + totalTimes + "x " + name + "§a for §f" + accountId + "§a.");
      return 1;
   }

   private static String init(String accountId, String name) {
      return "pov:" + accountId.toLowerCase(Locale.ROOT) + ":" + name.toLowerCase(Locale.ROOT);
   }

   private static void init(String accountId) {
      String prefix = "pov:" + accountId.toLowerCase(Locale.ROOT) + ":";
      init.forEach((key, flag) -> {
         if (key.startsWith(prefix) && init.remove(key, flag)) {
            flag.set(false);
         }
      });
   }

   private static void init() {
      for (AtomicBoolean flag : init.values()) {
         flag.set(false);
      }

      init.clear();
   }
}
