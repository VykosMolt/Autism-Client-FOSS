package autismclient.commands.b;

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
      root.executes(ctx -> init());
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("stop").executes(ctx -> {
         if (!RemoteViewController.init_Z()) {
            AutismClientMessaging.sendPrefixed("§7Remote view is not active.");
         } else {
            RemoteViewController.init(false);
            AutismClientMessaging.sendPrefixed("§aRemote view stopped.");
         }

         return 1;
      }));
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("player", StringArgumentType.greedyString())
            .suggests(RemoteViewCommand::init)
            .executes(ctx -> init(StringArgumentType.getString(ctx, "player")))
      );
   }

   private static int init() {
      Minecraft mc = Minecraft.getInstance();
      Entity entity = mc == null ? null : mc.crosshairPickEntity;
      if (!(entity instanceof Player) && mc != null && mc.hitResult instanceof EntityHitResult hit) {
         entity = hit.getEntity();
      }

      if (entity instanceof Player player) {
         init(RemoteViewController.init_cd_DataUtil(player));
         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cLook at a loaded player first.");
         return 1;
      }
   }

   private static int init(String requested) {
      Player player = init(Minecraft.getInstance(), requested);
      if (player == null) {
         AutismClientMessaging.sendPrefixed("§cPlayer is not loaded: §f" + requested.trim());
         return 1;
      } else {
         init(RemoteViewController.init_cd_DataUtil(player));
         return 1;
      }
   }

   static Player init(Minecraft mc, String requested) {
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

   private static void init(RemoteViewController.Inner_DataUtil result) {
      AutismClientMessaging.sendPrefixed((result.init_Z() ? "§a" : "§c") + result.init_Str());
   }

   private static CompletableFuture<Suggestions> init(CommandContext<AutismCommandSource> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (TabPlayerListParser.Inner_StreamUtil player : TabPlayerListParser.init(Minecraft.getInstance())) {
         if (player.init_Str().toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(player.init_Str());
         }
      }

      return builder.buildFuture();
   }
}
