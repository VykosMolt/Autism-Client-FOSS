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
            "slot", autismclient.commands.arguments.SlotArgumentType.create()
         )
         .executes(context -> clickSlot(context, SlotClickCommandHelper.ClickSpec.of("Left", ContainerInput.PICKUP, 0), 1));
      SlotClickCommandHelper.register(slot, ClickSlotCommand::clickSlot);
      root.then(slot);
   }

   private static int clickSlot(CommandContext<AutismCommandSource> context, SlotClickCommandHelper.ClickSpec spec, int times) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.containerMenu != null) {
         int visibleSlot = autismclient.commands.arguments.SlotArgumentType.getValue(context, "slot");
         int handlerSlot = InventorySlotHelper.toScreenSlot(mc, visibleSlot);
         return SlotClickCommandHelper.click(handlerSlot, spec, times, autismclient.commands.arguments.SlotArgumentType.slotName(visibleSlot));
      } else {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }
   }
}
