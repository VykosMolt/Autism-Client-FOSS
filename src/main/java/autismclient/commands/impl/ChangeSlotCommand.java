package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import autismclient.util.InventorySlotHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;

public final class ChangeSlotCommand extends Command {
   public ChangeSlotCommand() {
      super("change-slot", "Select hotbar slot 1-9.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(context -> {
         AutismClientMessaging.sendPrefixed("§eUsage: §f" + autismclient.commands.CommandManager.init_Str() + "change-slot <1-9>");
         return 1;
      });
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("hotbar-slot", IntegerArgumentType.integer(1, 9))
            .suggests(
               (context, builder) -> autismclient.commands.CommandSuggestions.init_CompletableFuture_SuggestionsBuilder_StrArr(
                  builder, "1", "2", "3", "4", "5", "6", "7", "8", "9"
               )
            )
            .executes(context -> init(IntegerArgumentType.getInteger(context, "hotbar-slot")))
      );
   }

   private static int init(int oneBasedSlot) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.getConnection() != null) {
         InventorySlotHelper.init_V(mc, oneBasedSlot - 1);
         AutismClientMessaging.sendPrefixed("§aSelected hotbar slot §f" + oneBasedSlot + "§a.");
         return 1;
      } else {
         AutismClientMessaging.sendPrefixed("§cNot in a world.");
         return 1;
      }
   }
}
