package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismMacro;
import autismclient.util.MacroLibraryManager;
import autismclient.util.macro.MacroExecutor;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
         String prefix = autismclient.commands.CommandManager.init_Str();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "macro <name> [times] [delayTicks]");
         AutismClientMessaging.sendPrefixed("§7Also: §f" + prefix + "macro stop [name] §7or §f" + prefix + "macro clear");
         return 1;
      });
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("stop")
               .executes(
                  ctx -> {
                     init();
                     MacroLibraryManager.init_aN().init_V();
                     AutismClientMessaging.sendPrefixed("§eStopped all macros.");
                     return 1;
                  }
               ))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", autismclient.commands.arguments.MacroNameArgumentType.init_CachePool())
                  .executes(
                     ctx -> {
                        String name = autismclient.commands.arguments.MacroNameArgumentType.init(ctx, "name");
                        AtomicBoolean flag = init.remove(name.toLowerCase());
                        if (flag != null) {
                           flag.set(false);
                        }

                        MacroExecutor.init_V_Str(name);
                        AutismClientMessaging.sendPrefixed("§eStopped: §f" + name);
                        return 1;
                     }
                  )
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("clear").executes(ctx -> {
         init();
         AutismClientMessaging.sendPrefixed("§eCleared macro loops.");
         return 1;
      }));
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("name", autismclient.commands.arguments.MacroNameArgumentType.init_CachePool())
               .executes(ctx -> init(autismclient.commands.arguments.MacroNameArgumentType.init(ctx, "name"), 1, 0)))
            .then(
               ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("times", IntegerArgumentType.integer(1, 100000))
                     .suggests(autismclient.commands.CommandSuggestions::close)
                     .executes(ctx -> init(autismclient.commands.arguments.MacroNameArgumentType.init(ctx, "name"), IntegerArgumentType.getInteger(ctx, "times"), 0)))
                  .then(
                     RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("delayTicks", IntegerArgumentType.integer(0, 72000))
                        .suggests(autismclient.commands.CommandSuggestions::open)
                        .executes(
                           ctx -> init(
                              autismclient.commands.arguments.MacroNameArgumentType.init(ctx, "name"),
                              IntegerArgumentType.getInteger(ctx, "times"),
                              IntegerArgumentType.getInteger(ctx, "delayTicks")
                           )
                        )
                  )
            )
      );
   }

   private static int init(String name, int times, int delayTicks) {
      AutismMacro macro = MacroLibraryManager.init_aN().init_aM_Str(name);
      if (macro == null) {
         AutismClientMessaging.sendPrefixed("§cMacro not found: §f" + name);
         return 1;
      } else if (times <= 1 && delayTicks <= 0) {
         MacroLibraryManager.init_aN().load_V(name);
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
                     MacroLibraryManager.init_aN().load_V(name);
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

   private static void init() {
      for (AtomicBoolean flag : init.values()) {
         flag.set(false);
      }

      init.clear();
   }
}
