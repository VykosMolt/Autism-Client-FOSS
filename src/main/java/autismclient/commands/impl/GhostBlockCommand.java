package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.GhostBlockModule;
import autismclient.modules.al;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

public class GhostBlockCommand extends Command {
   public GhostBlockCommand() {
      super("ghostblock", "Client-side ghost blocks: place, remove and manage blocks only you can see.", "gb");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.CommandManager.getPrefix();
         AutismClientMessaging.sendPrefixed("§eUsage: " + prefix + "ghostblock <toggle|reset|creative|block>");
         AutismClientMessaging.sendPrefixed("§7" + prefix + "ghostblock toggle §8- §7toggle the module");
         AutismClientMessaging.sendPrefixed("§7" + prefix + "ghostblock reset §8- §7remove all ghost blocks");
         AutismClientMessaging.sendPrefixed("§7" + prefix + "ghostblock creative [on|off] §8- §7fake creative gamemode");
         AutismClientMessaging.sendPrefixed("§7" + prefix + "ghostblock block [id|off] §8- §7arm a block for placement");
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("toggle").executes(ctx -> {
         GhostBlockModule module = module();
         if (module == null) {
            return 1;
         }

         module.toggle();
         return 1;
      }));
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("reset")
            .executes(
               ctx -> {
                  GhostBlockModule module = module();
                  if (module == null) {
                     return 1;
                  }

                  int removed = module.clearAll();
                  AutismClientMessaging.sendPrefixed(
                     removed == 0
                        ? "§7No ghost blocks to remove."
                        : "§aRemoved §f" + removed + "§a ghost block" + (removed == 1 ? "" : "s") + " and restored the original states."
                  );
                  return 1;
               }
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("creative").executes(ctx -> setEnabled((String)null)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("state", StringArgumentType.word())
                  .suggests(autismclient.commands.CommandSuggestions::suggestDelayState)
                  .executes(ctx -> setEnabled(StringArgumentType.getString(ctx, "state")))
            )
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("block").executes(ctx -> {
               GhostBlockModule module = module();
               if (module == null) {
                  return 1;
               }

               Block armed = module.getArmedBlock();
               AutismClientMessaging.sendPrefixed(armed == null ? "§7No ghost block armed." : "§7Armed ghost block: §f" + BuiltInRegistries.BLOCK.getKey(armed));
               return 1;
            }))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("block", autismclient.commands.arguments.ItemOrBlockIdArgumentType.id())
                  .suggests(autismclient.commands.CommandSuggestions::suggestBlockId)
                  .executes(ctx -> setMode(autismclient.commands.arguments.ItemOrBlockIdArgumentType.getId(ctx, "block")))
            )
      );
   }

   private static int setEnabled(String state) {
      GhostBlockModule module = module();
      if (module == null) {
         return 1;
      }

      boolean target;
      if (state == null || "toggle".equalsIgnoreCase(state)) {
         target = !module.isCreativeMode();
      } else if (!"on".equalsIgnoreCase(state) && !"enable".equalsIgnoreCase(state)) {
         if (!"off".equalsIgnoreCase(state) && !"disable".equalsIgnoreCase(state)) {
            AutismClientMessaging.sendPrefixed("§cUnknown state: §f" + state);
            return 1;
         }

         target = false;
      } else {
         target = true;
      }

      module.setCreativeMode(target);
      if (target && !module.isEnabled()) {
         module.setEnabled(true);
      }

      AutismClientMessaging.sendPrefixed(
         target ? "§aFake creative on. §7Whatever you place becomes a ghost block." : "§7Fake creative off. §7Back to your real gamemode."
      );
      return 1;
   }

   private static int setMode(String input) {
      GhostBlockModule module = module();
      if (module == null) {
         return 1;
      }

      String value = input.toLowerCase(Locale.ROOT);
      if (!"off".equals(value) && !"none".equals(value) && !"reset".equals(value)) {
         Identifier id = Identifier.tryParse(value.contains(":") ? value : "minecraft:" + value);
         Block block = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
         if (block == null) {
            block = autismclient.commands.RegistrySuggestions.resolveBlock(value);
         }

         if (block == null) {
            AutismClientMessaging.sendPrefixed("§cUnknown block: §f" + input);
            return 1;
         }

         module.setCreativeBlock(block, false);
         if (!module.isEnabled()) {
            module.setEnabled(true);
         }

         AutismClientMessaging.sendPrefixed(
            "§aGhost block: §f" + BuiltInRegistries.BLOCK.getKey(block) + (module.isCreativeMode() ? "" : " §7(switch Mode to Creative to place it)")
         );
         return 1;
      } else {
         module.setCreativeBlock(null, false);
         AutismClientMessaging.sendPrefixed("§7Ghost block disarmed; held blocks place ghosts again.");
         return 1;
      }
   }

   private static GhostBlockModule module() {
      if (al.init_Module_Str("ghostblock") instanceof GhostBlockModule ghostBlock) {
         return ghostBlock;
      } else {
         AutismClientMessaging.sendPrefixed("§cGhostBlock module is not available.");
         return null;
      }
   }
}
