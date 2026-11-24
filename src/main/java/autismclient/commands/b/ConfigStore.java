package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;

public class ConfigStore extends Command {
   public ConfigStore() {
      super("clear", "Clear every message from the chat.", "cls");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         Minecraft mc = Minecraft.getInstance();
         if (mc.gui != null && mc.gui.hud != null && mc.gui.hud.getChat() != null) {
            try {
               mc.gui.hud.getChat().clearMessages(false);
               mc.gui.hud.getChat().resetChatScroll();
            } catch (Throwable t) {
               AutismClientMessaging.sendPrefixed("§cClear failed: " + t.getMessage());
            }

            return 1;
         } else {
            AutismClientMessaging.sendPrefixed("§cChat unavailable.");
            return 1;
         }
      });
   }
}
