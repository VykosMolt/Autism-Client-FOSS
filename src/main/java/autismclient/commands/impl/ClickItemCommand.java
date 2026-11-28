package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

public final class ClickItemCommand extends Command {
   public ClickItemCommand() {
      super("click-item", "Click an item by its visible in-game name.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(context -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.CommandManager.getPrefix() + "click-item \"<item name>\" [click] [times]");
         return 1;
      });
      RequiredArgumentBuilder<AutismCommandSource, String> item = (RequiredArgumentBuilder<AutismCommandSource, String>)RequiredArgumentBuilder.<AutismCommandSource, String>argument(
            "item-name", autismclient.commands.arguments.ContainerItemNameArgumentType.init()
         )
         .executes(context -> init(context, SlotClickCommandHelper.ClickSpec.init("Left", ContainerInput.PICKUP, 0), 1));
      SlotClickCommandHelper.init(item, ClickItemCommand::init);
      root.then(item);
   }

   private static int init(CommandContext<AutismCommandSource> context, SlotClickCommandHelper.ClickSpec spec, int times) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.player.containerMenu != null) {
         String requestedName = autismclient.commands.arguments.ContainerItemNameArgumentType.init(context, "item-name");
         int handlerSlot = init(mc.player.containerMenu, requestedName);
         if (handlerSlot < 0) {
            AutismClientMessaging.sendPrefixed("§cNo visible item named §f" + requestedName + "§c exists in the current GUI.");
            return 1;
         } else {
            return SlotClickCommandHelper.init(handlerSlot, spec, times, "\"" + requestedName + "\"");
         }
      } else {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }
   }

   private static int init(AbstractContainerMenu menu, String requestedName) {
      String normalizedTarget = autismclient.util.macro.MacroItemTarget.normalizeForMatch(requestedName);
      if (normalizedTarget.isEmpty()) {
         return -1;
      }

      for (int handlerSlot = 0; handlerSlot < menu.slots.size(); handlerSlot++) {
         ItemStack stack = menu.slots.get(handlerSlot).getItem();
         if (!stack.isEmpty() && normalizedTarget.equals(autismclient.util.macro.MacroItemTarget.normalizeForMatch(stack.getHoverName().getString()))) {
            return handlerSlot;
         }
      }

      return -1;
   }
}
