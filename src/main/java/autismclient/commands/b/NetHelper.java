package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.aw;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;

public final class NetHelper extends Command {
   public NetHelper() {
      super("click-slot", "Click a GUI/player slot with Item Click actions.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(context -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.DataUtil.init_Str() + "click-slot <slot> [click] [times]");
         return 1;
      });
      RequiredArgumentBuilder<AutismCommandSource, Integer> slot = (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument(
            "slot", autismclient.commands.a.BufferHelper.init()
         )
         .executes(context -> init(context, CharEncoder.Inner_StreamUtil.init("Left", ContainerInput.PICKUP, 0), 1));
      CharEncoder.init(slot, NetHelper::init);
      root.then(slot);
   }

   private static int init(CommandContext<AutismCommandSource> context, CharEncoder.Inner_StreamUtil spec, int times) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.containerMenu != null) {
         int visibleSlot = autismclient.commands.a.BufferHelper.init(context, "slot");
         int handlerSlot = aw.load_I(mc, visibleSlot);
         return CharEncoder.init(handlerSlot, spec, times, autismclient.commands.a.BufferHelper.init_Str_I(visibleSlot));
      } else {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }
   }
}
