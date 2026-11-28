package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;

public class HClipCommand extends Command {
   public HClipCommand() {
      super("hclip", "Lets you clip through blocks horizontally (forward) with movement packets.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         String prefix = autismclient.commands.CommandManager.getPrefix();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "hclip <blocks>");
         AutismClientMessaging.sendPrefixed("§7Modes: §fdefault, forward, back, padding, single, custom (paper alias still works)");
         AutismClientMessaging.sendPrefixed("§7Examples: §f" + prefix + "hclip forward §7or §f" + prefix + "hclip custom -25 10 20 true true true");
         return 1;
      });
      root.then(init("blocks", options -> options));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("forward").executes(ctx -> {
         autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(0.0);
         options.mode = autismclient.util.macro.HClipAction.ClipMode.FORWARD;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("back").executes(ctx -> {
         autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(0.0);
         options.mode = autismclient.util.macro.HClipAction.ClipMode.BACK;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("default").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("paper").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("padding").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("single").then(init("blocks", autismclient.util.macro.HClipAction.ClipOptions::init)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("normal").then(init("blocks", autismclient.util.macro.HClipAction.ClipOptions::init)));
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("custom")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Double>argument("blocks", DoubleArgumentType.doubleArg())
                  .suggests(autismclient.commands.CommandSuggestions::load)
                  .then(
                     ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("segment", IntegerArgumentType.integer(1, 50))
                           .suggests(autismclient.commands.CommandSuggestions::read)
                           .executes(
                              ctx -> {
                                 autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(
                                    DoubleArgumentType.getDouble(ctx, "blocks")
                                 );
                                 options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
                                 init(options);
                                 return 1;
                              }
                           ))
                        .then(
                           ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("maxPackets", IntegerArgumentType.integer(1, 100))
                                 .suggests(autismclient.commands.CommandSuggestions::write)
                                 .executes(
                                    ctx -> {
                                       autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(
                                          DoubleArgumentType.getDouble(ctx, "blocks")
                                       );
                                       options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
                                       options.maxPackets = IntegerArgumentType.getInteger(ctx, "maxPackets");
                                       init(options);
                                       return 1;
                                    }
                                 ))
                              .then(
                                 ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("updateLocal", BoolArgumentType.bool())
                                       .suggests(autismclient.commands.CommandSuggestions::flush)
                                       .executes(ctx -> {
                                          autismclient.util.macro.HClipAction.ClipOptions options = init(ctx);
                                          options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                          init(options);
                                          return 1;
                                       }))
                                    .then(
                                       ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("vehicle", BoolArgumentType.bool())
                                             .suggests(autismclient.commands.CommandSuggestions::flush)
                                             .executes(ctx -> {
                                                autismclient.util.macro.HClipAction.ClipOptions options = init(ctx);
                                                options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                                options.tryVehicleFirst = BoolArgumentType.getBool(ctx, "vehicle");
                                                init(options);
                                                return 1;
                                             }))
                                          .then(
                                             RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("forceGround", BoolArgumentType.bool())
                                                .suggests(autismclient.commands.CommandSuggestions::flush)
                                                .executes(ctx -> {
                                                   autismclient.util.macro.HClipAction.ClipOptions options = init(ctx);
                                                   options.updateLocalPosition = BoolArgumentType.getBool(ctx, "updateLocal");
                                                   options.tryVehicleFirst = BoolArgumentType.getBool(ctx, "vehicle");
                                                   options.forceGrounded = BoolArgumentType.getBool(ctx, "forceGround");
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

   private static RequiredArgumentBuilder<AutismCommandSource, Double> init(String name, HClipCommand.Inner_DataUtil customizer) {
      return (RequiredArgumentBuilder<AutismCommandSource, Double>)RequiredArgumentBuilder.<AutismCommandSource, Double>argument(name, DoubleArgumentType.doubleArg())
         .suggests(autismclient.commands.CommandSuggestions::load)
         .executes(
            ctx -> {
               autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(
                  DoubleArgumentType.getDouble(ctx, name)
               );
               init(customizer.apply(options));
               return 1;
            }
         );
   }

   private static autismclient.util.macro.HClipAction.ClipOptions init(CommandContext<AutismCommandSource> ctx) {
      autismclient.util.macro.HClipAction.ClipOptions options = autismclient.util.macro.HClipAction.ClipOptions.init(
         DoubleArgumentType.getDouble(ctx, "blocks")
      );
      options.segmentBlocks = IntegerArgumentType.getInteger(ctx, "segment");
      options.maxPackets = IntegerArgumentType.getInteger(ctx, "maxPackets");
      options.maxRoutePackets = options.maxPackets;
      return options;
   }

   public static void init(double blocks) {
      init(autismclient.util.macro.HClipAction.ClipOptions.init(blocks));
   }

   private static void init(autismclient.util.macro.HClipAction.ClipOptions options) {
      options.chatFeedback = true;
      autismclient.util.macro.HClipAction.init(Minecraft.getInstance(), options);
   }

   private interface Inner_DataUtil {
      autismclient.util.macro.HClipAction.ClipOptions apply(autismclient.util.macro.HClipAction.ClipOptions var1);
   }
}
