package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.modules.aC;
import autismclient.util.AutismClientMessaging;
import autismclient.util.p;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.HoverEvent.ShowText;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class CopyPosCommand extends Command {
   public CopyPosCommand() {
      super("copypos", "Copies current coordinates.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(
         ctx -> {
            Minecraft mc = Minecraft.getInstance();
            if (aC.init_Z()) {
               String coordinates = init(aC.load_Vec3_F(1.0F));
               if (mc != null && mc.keyboardHandler != null) {
                  mc.keyboardHandler.setClipboard(coordinates);
               }

               AutismClientMessaging.sendPrefixed("§aPosition copied (freecam): ", init(coordinates));
               return 1;
            } else {
               Player controlled = mc == null ? null : mc.player;
               if (mc != null && controlled != null && mc.keyboardHandler != null) {
                  Entity positionOwner = controlled.getVehicle() != null ? controlled.getVehicle() : controlled;
                  String coordinates = init(positionOwner.position());
                  mc.keyboardHandler.setClipboard(coordinates);
                  AutismClientMessaging.sendPrefixed("§aPosition copied: ", init(coordinates));
                  return 1;
               } else {
                  AutismClientMessaging.sendPrefixed("§cNo controlled position available.");
                  return 1;
               }
            }
         }
      );
   }

   static Component init(String coordinates) {
      return Component.literal(coordinates)
         .withStyle(
            style -> style.withColor(p.parse())
               .withUnderlined(true)
               .withHoverEvent(new ShowText(Component.literal("Click to copy coordinates")))
               .withClickEvent(new CopyToClipboard(coordinates))
         );
   }

   static String init(Vec3 position) {
      return position == null ? "0 0 0" : init(position.x) + " " + load(position.y) + " " + init(position.z);
   }

   private static int init(double value) {
      return Double.isFinite(value) ? (int)Math.floor(value) : 0;
   }

   private static int load(double value) {
      return Double.isFinite(value) ? (int)Math.ceil(value - 1.0E-7) : 0;
   }
}
