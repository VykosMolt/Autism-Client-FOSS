package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.NameScrapeController;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

public final class NameScrapeCommand extends Command {
   public NameScrapeCommand() {
      super("namescrape", "Scrape player names to clipboard. [count] limits it; deep = exhaustive; pause/resume/stop.", "scrapenames");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> init(0, false));
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("count", IntegerArgumentType.integer(1))
            .suggests(autismclient.commands.CommandSuggestions::sync)
            .executes(ctx -> init(IntegerArgumentType.getInteger(ctx, "count"), false))
      );
      root.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.<AutismCommandSource>literal("deep").executes(ctx -> init(0, true)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("count", IntegerArgumentType.integer(1))
                  .suggests(autismclient.commands.CommandSuggestions::sync)
                  .executes(ctx -> init(IntegerArgumentType.getInteger(ctx, "count"), true))
            )
      );
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("pause").executes(ctx -> {
         NameScrapeController.pause();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("resume").executes(ctx -> {
         NameScrapeController.resume();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         NameScrapeController.stop();
         return 1;
      }));
   }

   private static int init(int count, boolean deep) {
      NameScrapeController.start(count, deep);
      return 1;
   }
}
