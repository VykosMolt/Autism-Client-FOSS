package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.TabPlayerListParser;
import autismclient.util.RemoteViewController;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

public final class RemoteViewCommand extends Command {
   public RemoteViewCommand() {
      super("rv", "Watch a loaded player's POV.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> viewCrosshairTarget());
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         if (!RemoteViewController.isActive()) {
            AutismClientMessaging.sendPrefixed("§7Remote view is not active.");
         } else {
            RemoteViewController.stop(false);
            AutismClientMessaging.sendPrefixed("§aRemote view stopped.");
         }

         return 1;
      }));
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("player", StringArgumentType.greedyString())
            .suggests(RemoteViewCommand::suggestPlayers)
            .executes(ctx -> viewPlayer(StringArgumentType.getString(ctx, "player")))
      );
   }

   private static int viewCrosshairTarget() {
      Minecraft mc = Minecraft.getInstance();
      Entity entity = mc == null ? null : mc.crosshairPickEntity;
      if (!(entity instanceof Player) && mc != null && mc.hitResult instanceof EntityHitResult hit) {
         entity = hit.getEntity();
      }

      if (entity instanceof Player player) {
         report(RemoteViewController.startViewing(player));
         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cLook at a loaded player first.");
         return 1;
      }
   }

   private static int viewPlayer(String requested) {
      Player player = findPlayer(Minecraft.getInstance(), requested);
      if (player == null) {
         AutismClientMessaging.sendPrefixed("§cPlayer is not loaded: §f" + requested.trim());
         return 1;
      } else {
         report(RemoteViewController.startViewing(player));
         return 1;
      }
   }

   static Player findPlayer(Minecraft mc, String requested) {
      if (mc != null && mc.level != null && requested != null) {
         String wanted = requested.trim();
         if (wanted.isEmpty()) {
            return null;
         }

         for (Player player : mc.level.players()) {
            if (player != null) {
               String profile = player.getGameProfile() == null ? "" : player.getGameProfile().name();
               String shown = player.getName().getString();
               if (wanted.equalsIgnoreCase(profile) || wanted.equalsIgnoreCase(shown)) {
                  return player;
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static void report(RemoteViewController.Inner_DataUtil result) {
      AutismClientMessaging.sendPrefixed((result.success() ? "§a" : "§c") + result.message());
   }

   private static CompletableFuture<Suggestions> suggestPlayers(CommandContext<AutismCommandSource> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (TabPlayerListParser.ParsedPlayer player : TabPlayerListParser.parse(Minecraft.getInstance())) {
         if (player.name().toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(player.name());
         }
      }

      return builder.buildFuture();
   }
}
