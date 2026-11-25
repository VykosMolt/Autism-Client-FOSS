package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.macro.ap;
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
         String prefix = autismclient.commands.DataUtil.init_Str();
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "vclip <blocks>");
         AutismClientMessaging.sendPrefixed("§7Modes: §fdefault, top, bottom, paper, single, custom");
         AutismClientMessaging.sendPrefixed("§7Examples: §f" + prefix + "vclip top §7or §f" + prefix + "vclip custom -25 10 20 true true");
         return 1;
      });
      root.then(init("blocks", options -> options));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("top").executes(ctx -> {
         ap.Inner_CachePool options = ap.Inner_CachePool.init(0.0);
         options.init_ap_StreamUtil = ap.Inner_StreamUtil.load;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("bottom").executes(ctx -> {
         ap.Inner_CachePool options = ap.Inner_CachePool.init(0.0);
         options.init_ap_StreamUtil = ap.Inner_StreamUtil.read;
         init(options);
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("default").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("paper").then(init("blocks", options -> options)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("single").then(init("blocks", ap.Inner_CachePool::init)));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("normal").then(init("blocks", ap.Inner_CachePool::init)));
      root.then(
         LiteralArgumentBuilder.<AutismCommandSource>literal("custom")
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Double>argument("blocks", DoubleArgumentType.doubleArg())
                  .suggests(autismclient.commands.StreamUtil::load)
                  .then(
                     ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("segment", IntegerArgumentType.integer(1, 50))
                           .suggests(autismclient.commands.StreamUtil::read)
                           .executes(ctx -> {
                              ap.Inner_CachePool options = ap.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
                              options.init_I = IntegerArgumentType.getInteger(ctx, "segment");
                              init(options);
                              return 1;
                           }))
                        .then(
                           ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("maxPackets", IntegerArgumentType.integer(1, 100))
                                 .suggests(autismclient.commands.StreamUtil::write)
                                 .executes(ctx -> {
                                    ap.Inner_CachePool options = ap.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
                                    options.init_I = IntegerArgumentType.getInteger(ctx, "segment");
                                    options.load_I = IntegerArgumentType.getInteger(ctx, "maxPackets");
                                    init(options);
                                    return 1;
                                 }))
                              .then(
                                 ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("updateLocal", BoolArgumentType.bool())
                                       .suggests(autismclient.commands.StreamUtil::flush)
                                       .executes(ctx -> {
                                          ap.Inner_CachePool options = init(ctx);
                                          options.load_Z = BoolArgumentType.getBool(ctx, "updateLocal");
                                          init(options);
                                          return 1;
                                       }))
                                    .then(
                                       ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("vehicle", BoolArgumentType.bool())
                                             .suggests(autismclient.commands.StreamUtil::flush)
                                             .executes(ctx -> {
                                                ap.Inner_CachePool options = init(ctx);
                                                options.load_Z = BoolArgumentType.getBool(ctx, "updateLocal");
                                                options.read = BoolArgumentType.getBool(ctx, "vehicle");
                                                init(options);
                                                return 1;
                                             }))
                                          .then(
                                             RequiredArgumentBuilder.<AutismCommandSource, Boolean>argument("forceGround", BoolArgumentType.bool())
                                                .suggests(autismclient.commands.StreamUtil::flush)
                                                .executes(ctx -> {
                                                   ap.Inner_CachePool options = init(ctx);
                                                   options.load_Z = BoolArgumentType.getBool(ctx, "updateLocal");
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
         .suggests(autismclient.commands.StreamUtil::load)
         .executes(ctx -> {
            ap.Inner_CachePool options = ap.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, name));
            init(customizer.apply(options));
            return 1;
         });
   }

   private static ap.Inner_CachePool init(CommandContext<AutismCommandSource> ctx) {
      ap.Inner_CachePool options = ap.Inner_CachePool.init(DoubleArgumentType.getDouble(ctx, "blocks"));
      options.init_I = IntegerArgumentType.getInteger(ctx, "segment");
      options.load_I = IntegerArgumentType.getInteger(ctx, "maxPackets");
      return options;
   }

   public static void init(double blocks) {
      init(ap.Inner_CachePool.init(blocks));
   }

   private static void init(ap.Inner_CachePool options) {
      options.update = true;
      ap.init(Minecraft.getInstance(), options);
   }

   private interface Inner_DataUtil {
      ap.Inner_CachePool apply(ap.Inner_CachePool var1);
   }
}
