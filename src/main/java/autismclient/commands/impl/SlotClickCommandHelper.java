package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.util.AutismClientMessaging;
import autismclient.util.InventoryDropHelper;
import autismclient.util.ContainerClickExecutor;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

final class SlotClickCommandHelper {
   static final int init = 100000;

   private SlotClickCommandHelper() {
   }

   static void init(ArgumentBuilder<AutismCommandSource, ?> target, SlotClickCommandHelper.ClickExecutor executor) {
      target.then(init("left", SlotClickCommandHelper.ClickSpec.init("Left", ContainerInput.PICKUP, 0), executor));
      target.then(init("right", SlotClickCommandHelper.ClickSpec.init("Right", ContainerInput.PICKUP, 1), executor));
      target.then(init("middle", SlotClickCommandHelper.ClickSpec.init("Middle", ContainerInput.PICKUP, 2), executor));
      target.then(init("shift-left", SlotClickCommandHelper.ClickSpec.init("Shift Left", ContainerInput.QUICK_MOVE, 0), executor));
      target.then(init("shift-right", SlotClickCommandHelper.ClickSpec.init("Shift Right", ContainerInput.QUICK_MOVE, 1), executor));
      target.then(init("clone", SlotClickCommandHelper.ClickSpec.init("Clone", ContainerInput.CLONE, 2), executor));
      target.then(init("pickup-all", SlotClickCommandHelper.ClickSpec.init("Pick Up All", ContainerInput.PICKUP_ALL, 0), executor));
      target.then(init("drop-item", SlotClickCommandHelper.ClickSpec.dropSpec("Drop Item", SlotClickCommandHelper.DropMode.ITEM), executor));
      target.then(load("drop-stack", SlotClickCommandHelper.ClickSpec.dropSpec("Drop Stack", SlotClickCommandHelper.DropMode.STACK), executor));
      LiteralArgumentBuilder<AutismCommandSource> swap = LiteralArgumentBuilder.<AutismCommandSource>literal("swap");
      RequiredArgumentBuilder<AutismCommandSource, Integer> hotbar = (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument(
            "swap-hotbar", IntegerArgumentType.integer(1, 9)
         )
         .suggests(
            (context, builder) -> autismclient.commands.CommandSuggestions.suggest(
               builder, "1", "2", "3", "4", "5", "6", "7", "8", "9"
            )
         )
         .executes(
            context -> executor.execute(
               context,
               SlotClickCommandHelper.ClickSpec.init(
                  "Swap " + IntegerArgumentType.getInteger(context, "swap-hotbar"),
                  ContainerInput.SWAP,
                  IntegerArgumentType.getInteger(context, "swap-hotbar") - 1
               ),
               1
            )
         );
      hotbar.then(
         timesArgument(
            (context, times) -> executor.execute(
               context,
               SlotClickCommandHelper.ClickSpec.init(
                  "Swap " + IntegerArgumentType.getInteger(context, "swap-hotbar"),
                  ContainerInput.SWAP,
                  IntegerArgumentType.getInteger(context, "swap-hotbar") - 1
               ),
               times
            )
         )
      );
      swap.then(hotbar);
      target.then(swap);
   }

   static int init(int handlerSlot, SlotClickCommandHelper.ClickSpec spec, int times, String targetLabel) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.gameMode == null || mc.getConnection() == null || mc.player.containerMenu == null) {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }

      if (handlerSlot >= 0 && handlerSlot < mc.player.containerMenu.slots.size()) {
         int requested = Math.max(1, times);
         int completed = 0;
         if (spec.kind() == SlotClickCommandHelper.DropMode.ITEM) {
            ItemStack stack = mc.player.containerMenu.slots.get(handlerSlot).getItem();
            if (!stack.isEmpty()) {
               int actual = Math.min(requested, stack.getCount());
               if (InventoryDropHelper.dropFromHandler(mc, handlerSlot, actual) > 0) {
                  completed = actual;
               }
            }
         } else if (spec.kind() == SlotClickCommandHelper.DropMode.STACK) {
            completed = InventoryDropHelper.dropFromHandler(mc, handlerSlot, 0) > 0 ? 1 : 0;
         } else {
            for (int i = 0; i < requested && ContainerClickExecutor.init(mc, handlerSlot, spec.init_I(), spec.input()); i++) {
               completed++;
            }
         }

         if (completed == 0) {
            AutismClientMessaging.sendPrefixed("§cCould not click §f" + targetLabel + "§c.");
         } else {
            String repeat = completed > 1 && spec.kind() != SlotClickCommandHelper.DropMode.STACK ? " x" + completed : "";
            String partial = completed < requested && spec.kind() != SlotClickCommandHelper.DropMode.STACK
               ? " §e(" + completed + "/" + requested + ")"
               : "";
            AutismClientMessaging.sendPrefixed("§a" + spec.label() + " clicked §f" + targetLabel + "§a" + repeat + "." + partial);
         }

         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cThat slot does not exist in the current GUI.");
         return 1;
      }
   }

   private static LiteralArgumentBuilder<AutismCommandSource> init(String literal, SlotClickCommandHelper.ClickSpec spec, SlotClickCommandHelper.ClickExecutor executor) {
      LiteralArgumentBuilder<AutismCommandSource> node = load(literal, spec, executor);
      node.then(timesArgument((context, times) -> executor.execute(context, spec, times)));
      return node;
   }

   private static LiteralArgumentBuilder<AutismCommandSource> load(String literal, SlotClickCommandHelper.ClickSpec spec, SlotClickCommandHelper.ClickExecutor executor) {
      return (LiteralArgumentBuilder<AutismCommandSource>)LiteralArgumentBuilder.<AutismCommandSource>literal(literal).executes(context -> executor.execute(context, spec, 1));
   }

   private static RequiredArgumentBuilder<AutismCommandSource, Integer> timesArgument(SlotClickCommandHelper.TimesExecutor executor) {
      return (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("times", IntegerArgumentType.integer(1, 100000))
         .suggests(autismclient.commands.CommandSuggestions::suggestCount)
         .executes(context -> executor.execute(context, IntegerArgumentType.getInteger(context, "times")));
   }

   @FunctionalInterface
   private interface TimesExecutor {
      int execute(CommandContext<AutismCommandSource> var1, int var2);
   }

   enum DropMode {
      NONE,
      ITEM,
      STACK;
   }

   @FunctionalInterface
   interface ClickExecutor {
      int execute(CommandContext<AutismCommandSource> var1, SlotClickCommandHelper.ClickSpec var2, int var3);
   }

   record ClickSpec(String label, ContainerInput input, int init_I, SlotClickCommandHelper.DropMode kind) {
      static SlotClickCommandHelper.ClickSpec init(String label, ContainerInput input, int button) {
         return new SlotClickCommandHelper.ClickSpec(label, input, button, SlotClickCommandHelper.DropMode.NONE);
      }

      static SlotClickCommandHelper.ClickSpec dropSpec(String label, SlotClickCommandHelper.DropMode dropMode) {
         return new SlotClickCommandHelper.ClickSpec(label, ContainerInput.THROW, dropMode == SlotClickCommandHelper.DropMode.STACK ? 1 : 0, dropMode);
      }
   }
}
