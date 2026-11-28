package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.util.AutismClientMessaging;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GiveCommand extends Command {
   public GiveCommand() {
      super("give", "Give item to self (creative only).");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "give <item> [count]");
         return 1;
      });
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, String>argument("item", autismclient.commands.arguments.ItemOrBlockIdArgumentType.id())
               .suggests(autismclient.commands.CommandSuggestions::suggestItemId)
               .executes(ctx -> give(autismclient.commands.arguments.ItemOrBlockIdArgumentType.getId(ctx, "item"), 1)))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("count", IntegerArgumentType.integer(1, 64))
                  .suggests(autismclient.commands.CommandSuggestions::suggestCount)
                  .executes(ctx -> give(autismclient.commands.arguments.ItemOrBlockIdArgumentType.getId(ctx, "item"), IntegerArgumentType.getInteger(ctx, "count")))
            )
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("item [count]", StringArgumentType.greedyString())
            .suggests(autismclient.commands.CommandSuggestions::suggestItemId)
            .executes(ctx -> giveFromLine(StringArgumentType.getString(ctx, "item [count]")))
      );
   }

   private static int giveFromLine(String line) {
      List<String> words = new ArrayList<>();
      int count = 1;

      for (String token : line.trim().split("\\s+")) {
         if (!token.isEmpty()) {
            if (token.matches("\\d+")) {
               count = Math.max(1, Math.min(64, Integer.parseInt(token.length() > 3 ? "64" : token)));
            } else {
               words.add(token);
            }
         }
      }

      if (words.isEmpty()) {
         AutismClientMessaging.sendPrefixed("§eUsage: " + autismclient.commands.CommandManager.getPrefix() + "give <item> [count]");
         return 1;
      } else {
         return give(String.join("_", words), count);
      }
   }

   private static int give(String itemId, int count) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null) {
         AutismClientMessaging.sendPrefixed("§cNot in a world.");
         return 1;
      }

      if (!mc.player.getAbilities().instabuild) {
         AutismClientMessaging.sendPrefixed("§cCreative mode required.");
         return 1;
      }

      Item item = autismclient.commands.RegistrySuggestions.resolveItem(itemId);
      if (item == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown item: §f" + itemId);
         return 1;
      }

      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      ItemStack stack = new ItemStack(item, count);
      int slot = mc.player.getInventory().getSelectedSlot();
      int containerSlot = 36 + slot;
      mc.getConnection().send(new ServerboundSetCreativeModeSlotPacket((short)containerSlot, stack));
      mc.player.getInventory().setItem(slot, stack);
      AutismClientMessaging.sendPrefixed("§aGave §f" + id + " x" + count);
      return 1;
   }
}
