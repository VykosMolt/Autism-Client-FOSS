package autismclient.commands.b;

import autismclient.commands.AutismCommandSource;
import autismclient.util.AutismClientMessaging;
import autismclient.util.O;
import autismclient.util.av;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

final class SlotClickArguments {
   static final int init = 100000;

   private SlotClickArguments() {
   }

   static void init(ArgumentBuilder<AutismCommandSource, ?> target, SlotClickArguments.Inner_DataUtil executor) {
      target.then(init("left", SlotClickArguments.Inner_StreamUtil.init("Left", ContainerInput.PICKUP, 0), executor));
      target.then(init("right", SlotClickArguments.Inner_StreamUtil.init("Right", ContainerInput.PICKUP, 1), executor));
      target.then(init("middle", SlotClickArguments.Inner_StreamUtil.init("Middle", ContainerInput.PICKUP, 2), executor));
      target.then(init("shift-left", SlotClickArguments.Inner_StreamUtil.init("Shift Left", ContainerInput.QUICK_MOVE, 0), executor));
      target.then(init("shift-right", SlotClickArguments.Inner_StreamUtil.init("Shift Right", ContainerInput.QUICK_MOVE, 1), executor));
      target.then(init("clone", SlotClickArguments.Inner_StreamUtil.init("Clone", ContainerInput.CLONE, 2), executor));
      target.then(init("pickup-all", SlotClickArguments.Inner_StreamUtil.init("Pick Up All", ContainerInput.PICKUP_ALL, 0), executor));
      target.then(init("drop-item", SlotClickArguments.Inner_StreamUtil.init("Drop Item", SlotClickArguments.Inner_CachePool.load), executor));
      target.then(load("drop-stack", SlotClickArguments.Inner_StreamUtil.init("Drop Stack", SlotClickArguments.Inner_CachePool.read), executor));
      LiteralArgumentBuilder<AutismCommandSource> swap = LiteralArgumentBuilder.<AutismCommandSource>literal("swap");
      RequiredArgumentBuilder<AutismCommandSource, Integer> hotbar = (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument(
            "swap-hotbar", IntegerArgumentType.integer(1, 9)
         )
         .suggests(
            (context, builder) -> autismclient.commands.StreamUtil.init_CompletableFuture_SuggestionsBuilder_StrArr(
               builder, "1", "2", "3", "4", "5", "6", "7", "8", "9"
            )
         )
         .executes(
            context -> executor.execute(
               context,
               SlotClickArguments.Inner_StreamUtil.init(
                  "Swap " + IntegerArgumentType.getInteger(context, "swap-hotbar"),
                  ContainerInput.SWAP,
                  IntegerArgumentType.getInteger(context, "swap-hotbar") - 1
               ),
               1
            )
         );
      hotbar.then(
         init(
            (context, times) -> executor.execute(
               context,
               SlotClickArguments.Inner_StreamUtil.init(
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

   static int init(int handlerSlot, SlotClickArguments.Inner_StreamUtil spec, int times, String targetLabel) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.gameMode == null || mc.getConnection() == null || mc.player.containerMenu == null) {
         AutismClientMessaging.sendPrefixed("§cNo active inventory or GUI.");
         return 1;
      }

      if (handlerSlot >= 0 && handlerSlot < mc.player.containerMenu.slots.size()) {
         int requested = Math.max(1, times);
         int completed = 0;
         if (spec.init_CharEncoder_CacheP() == SlotClickArguments.Inner_CachePool.load) {
            ItemStack stack = mc.player.containerMenu.slots.get(handlerSlot).getItem();
            if (!stack.isEmpty()) {
               int actual = Math.min(requested, stack.getCount());
               if (O.init(mc, handlerSlot, actual) > 0) {
                  completed = actual;
               }
            }
         } else if (spec.init_CharEncoder_CacheP() == SlotClickArguments.Inner_CachePool.read) {
            completed = O.init(mc, handlerSlot, 0) > 0 ? 1 : 0;
         } else {
            for (int i = 0; i < requested && av.init(mc, handlerSlot, spec.init_I(), spec.init_ContainerInput()); i++) {
               completed++;
            }
         }

         if (completed == 0) {
            AutismClientMessaging.sendPrefixed("§cCould not click §f" + targetLabel + "§c.");
         } else {
            String repeat = completed > 1 && spec.init_CharEncoder_CacheP() != SlotClickArguments.Inner_CachePool.read ? " x" + completed : "";
            String partial = completed < requested && spec.init_CharEncoder_CacheP() != SlotClickArguments.Inner_CachePool.read
               ? " §e(" + completed + "/" + requested + ")"
               : "";
            AutismClientMessaging.sendPrefixed("§a" + spec.init_Str() + " clicked §f" + targetLabel + "§a" + repeat + "." + partial);
         }

         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cThat slot does not exist in the current GUI.");
         return 1;
      }
   }

   private static LiteralArgumentBuilder<AutismCommandSource> init(String literal, SlotClickArguments.Inner_StreamUtil spec, SlotClickArguments.Inner_DataUtil executor) {
      LiteralArgumentBuilder<AutismCommandSource> node = load(literal, spec, executor);
      node.then(init((context, times) -> executor.execute(context, spec, times)));
      return node;
   }

   private static LiteralArgumentBuilder<AutismCommandSource> load(String literal, SlotClickArguments.Inner_StreamUtil spec, SlotClickArguments.Inner_DataUtil executor) {
      return (LiteralArgumentBuilder<AutismCommandSource>)LiteralArgumentBuilder.<AutismCommandSource>literal(literal).executes(context -> executor.execute(context, spec, 1));
   }

   private static RequiredArgumentBuilder<AutismCommandSource, Integer> init(SlotClickArguments.Inner_BufferHelper executor) {
      return (RequiredArgumentBuilder<AutismCommandSource, Integer>)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("times", IntegerArgumentType.integer(1, 100000))
         .suggests(autismclient.commands.StreamUtil::sync)
         .executes(context -> executor.execute(context, IntegerArgumentType.getInteger(context, "times")));
   }

   @FunctionalInterface
   private interface Inner_BufferHelper {
      int execute(CommandContext<AutismCommandSource> var1, int var2);
   }

   enum Inner_CachePool {
      init,
      load,
      read;
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      int execute(CommandContext<AutismCommandSource> var1, SlotClickArguments.Inner_StreamUtil var2, int var3);
   }

   record Inner_StreamUtil(String init_Str, ContainerInput init_ContainerInput, int init_I, SlotClickArguments.Inner_CachePool init_CharEncoder_CacheP) {
      static SlotClickArguments.Inner_StreamUtil init(String label, ContainerInput input, int button) {
         return new SlotClickArguments.Inner_StreamUtil(label, input, button, SlotClickArguments.Inner_CachePool.init);
      }

      static SlotClickArguments.Inner_StreamUtil init(String label, SlotClickArguments.Inner_CachePool dropMode) {
         return new SlotClickArguments.Inner_StreamUtil(label, ContainerInput.THROW, dropMode == SlotClickArguments.Inner_CachePool.read ? 1 : 0, dropMode);
      }
   }
}
