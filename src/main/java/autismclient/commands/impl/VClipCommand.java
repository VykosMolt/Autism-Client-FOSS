package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.macro.VClipAction;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;

public class VClipCommand extends Command {
   public VClipCommand() {
      super("vclip", "Lets you clip through blocks vertically with movement packets.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.CommandManager.getPrefix();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "vclip <blocks>");
         AutismClientMessaging.sendPrefixed("§7Modes: §fdefault, top, bottom, paper, single, custom");
         AutismClientMessaging.sendPrefixed("§7Examples: §f" + prefix + "vclip top §7or §f" + prefix + "vclip custom -25 10 20 true true");
         return 1;
      });
      root.then(init("blocks", options -> options));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("top").executes(ctx -> {
         VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(0.0);
         options.mode = VClipAction.Inner_StreamUtil.TOP;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("bottom").executes(ctx -> {
         VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(0.0);
         options.mode = VClipAction.Inner_StreamUtil.BOTTOM;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("default").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("paper").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("single").then(init("blocks", VClipAction.Inner_CachePool::init)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("normal").then(init("blocks", VClipAction.Inner_CachePool::init)));
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("custom")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Double>argument("blocks", DoubleArgumentType.doubleArg())
                  .suggests(autismclient.commands.CommandSuggestions::load)
                  .then(
                     ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("segment", IntegerArgumentType.integer(1, 50))
                           .suggests(autismclient.commands.CommandSuggestions::read)
                           .executes(ctx -> {
                              VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
                              options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
                              init(options);
                              return 1;
                           }))
                        .then(
                           ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("maxPackets", IntegerArgumentType.integer(1, 100))
                                 .suggests(autismclient.commands.CommandSuggestions::write)
                                 .executes(ctx -> {
                                    VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
                                    options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
                                    options.maxPackets = IntegerArgumentType.getInteger(ctx, "maxPackets");
                                    init(options);
                                    return 1;
                                 }))
                              .then(
                                 ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("updateLocal", BoolArgumentType.bool())
                                       .suggests(autismclient.commands.CommandSuggestions::flush)
                                       .executes(ctx -> {
                                          VClipAction.Inner_CachePool options = init(ctx);
                                          options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                          init(options);
                                          return 1;
                                       }))
                                    .then(
                                       ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("vehicle", BoolArgumentType.bool())
                                             .suggests(autismclient.commands.CommandSuggestions::flush)
                                             .executes(ctx -> {
                                                VClipAction.Inner_CachePool options = init(ctx);
                                                options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                                options.read = BoolArgumentType.getBool(ctx, "vehicle");
                                                init(options);
                                                return 1;
                                             }))
                                          .then(
                                             RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("forceGround", BoolArgumentType.bool())
                                                .suggests(autismclient.commands.CommandSuggestions::flush)
                                                .executes(ctx -> {
                                                   VClipAction.Inner_CachePool options = init(ctx);
                                                   options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                                   options.read = BoolArgumentType.getBool(ctx, "vehicle");
                                                   options.write = BoolArgumentType.getBool(ctx, "forceGround");
                                                   init(options);
                                                   return 1;
                                                })
                                          )
                                    )
                              )
                        )
                  )
            )
      );
   }

   private static RequiredArgumentBuilder<AutismCommandSource, Double> init(String name, VClipCommand.Inner_DataUtil customizer) {
      return (RequiredArgumentBuilder<AutismCommandSource, Double>)RequiredArgumentBuilder.<AutismCommandSource, Double>argument(name, DoubleArgumentType.doubleArg())
         .suggests(autismclient.commands.CommandSuggestions::load)
         .executes(ctx -> {
            VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, name));
            init(customizer.apply(options));
            return 1;
         });
   }

   private static VClipAction.Inner_CachePool init(CommandContext<AutismCommandSource> ctx) {
      VClipAction.Inner_CachePool options = VClipAction.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
      options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
      options.maxPackets = IntegerArgumentType.getInteger(ctx, "maxPackets");
      return options;
   }

   public static void init(double blocks) {
      init(VClipAction.Inner_CachePool.init(blocks));
   }

   private static void init(VClipAction.Inner_CachePool options) {
      options.update = true;
      VClipAction.init(Minecraft.getInstance(), options);
   }

   private interface Inner_DataUtil {
      VClipAction.Inner_CachePool apply(VClipAction.Inner_CachePool var1);
   }
}
