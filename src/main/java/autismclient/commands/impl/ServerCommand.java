package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismServerInfoOverlay;
import autismclient.util.OverlayManager;
import autismclient.util.macro.ServerTickPhaseTracker;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;

public class ServerCommand extends Command {
   public ServerCommand() {
      super("server", "Open the server info panel, or the plugin scanner.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         openInfo();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("info").executes(ctx -> {
         openInfo();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("plugins").executes(ctx -> {
         load();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("tps").executes(ctx -> {
         showTps();
         return 1;
      }));
   }

   static void openInfo() {
      openInfo(false);
   }

   static void load() {
      openInfo(true);
   }

   private static void openInfo(boolean pluginsTab) {
      AutismServerInfoOverlay overlay = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
      if (overlay == null) {
         AutismClientMessaging.sendPrefixed("§cServer overlay unavailable.");
      } else {
         OverlayManager.getInstance().register(overlay);
         if (pluginsTab) {
            overlay.showPluginsTab();
         } else {
            overlay.showOverviewTab();
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            mc.execute(() -> {
               if (mc.gui.screen() == null) {
                  mc.gui.setScreen(new autismclient.gui.screens.AutismOverlaysScreen(overlay));
               }
            });
         }
      }
   }

   private static void showTps() {
      double tps = ServerTickPhaseTracker.getTps();
      AutismClientMessaging.sendPrefixed(String.format("§eTPS: §f%.2f §7(estimated)", tps));
   }
}
