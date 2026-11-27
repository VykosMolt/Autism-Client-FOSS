package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.InventorySlotHelper;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;

public final class ClickSlotCommand extends Command {
   public ClickSlotCommand() {
      super("click-slot", "Click a GUI/player slot with Item Click actions.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(context -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.CommandManager.getPrefix() + "click-slot <slot> [click] [times]");
         return 1;
      });
      RequiredArgumentBuilder<AutismCommandSource, Integer> slot = (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument(
            "slot", autismclient.commands.arguments.SlotArgumentType.init()
         )
         .executes(context -> init(context, SlotClickCommandHelper.Inner_StreamUtil.init("Left", ContainerInput.PICKUP, 0), 1));
      SlotClickCommandHelper.init(slot, ClickSlotCommand::init);
      root.then(slot);
   }

   private static int init(CommandContext<AutismCommandSource> context, SlotClickCommandHelper.Inner_StreamUtil spec, int times) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.containerMenu != null) {
         int visibleSlot = autismclient.commands.arguments.SlotArgumentType.init(context, "slot");
         int handlerSlot = InventorySlotHelper.toScreenSlot(mc, visibleSlot);
         return SlotClickCommandHelper.init(handlerSlot, spec, times, autismclient.commands.arguments.SlotArgumentType.slotName(visibleSlot));
      } else {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }
   }
}
