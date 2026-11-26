package autismclient.commands.b;

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
         init();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("info").executes(ctx -> {
         init();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("plugins").executes(ctx -> {
         load();
         return 1;
      }));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("tps").executes(ctx -> {
         read();
         return 1;
      }));
   }

   static void init() {
      init(false);
   }

   static void load() {
      init(true);
   }

   private static void init(boolean pluginsTab) {
      AutismServerInfoOverlay overlay = autismclient.modules.ModuleRuntimeManager.init_PoolEntry().init_AutismServerInfoOv();
      if (overlay == null) {
         AutismClientMessaging.sendPrefixed("§cServer overlay unavailable.");
      } else {
         OverlayManager.init_bk().init_V(overlay);
         if (pluginsTab) {
            overlay.clear_V_8();
         } else {
            overlay.add_V_9();
         }

         Minecraft mc = Minecraft.getInstance();
         if (mc != null) {
            mc.execute(() -> {
               if (mc.gui.screen() == null) {
                  mc.gui.setScreen(new autismclient.gui.d.AutismOverlaysScreen(overlay));
               }
            });
         }
      }
   }

   private static void read() {
      double tps = ServerTickPhaseTracker.init_D();
      AutismClientMessaging.sendPrefixed(String.format("§eTPS: §f%.2f §7(estimated)", tps));
   }
}
