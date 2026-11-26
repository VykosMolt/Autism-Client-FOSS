package autismclient.commands.impl;

import autismclient.commands.AutismCommandSource;
import autismclient.commands.Command;
import autismclient.compat263.Compat263;
import autismclient.util.AutismClientMessaging;
import autismclient.util.InventoryDropHelper;
import autismclient.util.InventorySlotHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DropCommand extends Command {
   private static final Set<String> init = Set.of("all", "everything", "inventory", "inv", "full", "fullinventory", "full_inventory", "all_items", "bag");
   private static final Set<String> load = Set.of("hand", "held", "hand_item", "held_item", "mainhand", "main_hand");

   public DropCommand() {
      super("drop", "Drop hand, full inventory, or an amount of a held/specific item.");
   }

   @Override
   public void build(LiteralArgumentBuilder<AutismCommandSource> root) {
      root.executes(ctx -> {
         init_V_3();
         return 1;
      });
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("hand").executes(ctx -> init_I_3()));
      root.then(LiteralArgumentBuilder.<AutismCommandSource>literal("fullinventory").executes(ctx -> load()));
      root.then(
         ((RequiredArgumentBuilder)RequiredArgumentBuilder.<AutismCommandSource, Integer>argument("amount", IntegerArgumentType.integer(1))
               .suggests(autismclient.commands.CommandSuggestions::sync)
               .executes(ctx -> init(IntegerArgumentType.getInteger(ctx, "amount"))))
            .then(
               RequiredArgumentBuilder.<AutismCommandSource, String>argument("item", autismclient.commands.arguments.ItemOrBlockIdArgumentType.init())
                  .suggests(autismclient.commands.RegistrySuggestions::init)
                  .executes(ctx -> init(IntegerArgumentType.getInteger(ctx, "amount"), autismclient.commands.arguments.ItemOrBlockIdArgumentType.init(ctx, "item")))
            )
      );
      root.then(
         RequiredArgumentBuilder.<AutismCommandSource, String>argument("item [amount]", StringArgumentType.greedyString())
            .suggests(autismclient.commands.RegistrySuggestions::init)
            .executes(ctx -> init_I(StringArgumentType.getString(ctx, "item [amount]")))
      );
   }

   static DropCommand.Inner_DataUtil init_TextFormat_DataUti(String line) {
      List<String> words = new ArrayList<>();
      int amount = -1;

      for (String token : (line == null ? "" : line).trim().split("\\s+")) {
         if (!token.isEmpty()) {
            if (token.matches("\\d+")) {
               try {
                  amount = Integer.parseInt(token);
               } catch (NumberFormatException e) {
                  amount = 2147483647;
               }
            } else {
               words.add(token.toLowerCase(Locale.ROOT));
            }
         }
      }

      String spec = String.join("_", words);
      if (words.isEmpty()) {
         return amount > 0
            ? new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.ITEM, amount, "")
            : new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.UNKNOWN, -1, "");
      } else if (init.contains(spec)) {
         return new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.FULL, -1, spec);
      } else if (load.contains(spec)) {
         return amount > 0
            ? new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.ITEM, amount, "")
            : new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.HAND, -1, spec);
      } else {
         return new DropCommand.Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2.ITEM, amount, spec);
      }
   }

   private static int init_I(String line) {
      DropCommand.Inner_DataUtil request = init_TextFormat_DataUti(line);
      int var10000;
      switch (request.init_TextFormat_DataUti()) {
         case FULL:
            var10000 = load();
            break;
         case HAND:
            var10000 = init_I_3();
            break;
         case ITEM:
            var10000 = request.init_Str().isEmpty() ? init(request.init_I()) : init(request.init_Str(), request.init_I());
            break;
         default:
            init_V_3();
            var10000 = 1;
      }

      return var10000;
   }

   private static int init(String spec, int requested) {
      Minecraft mc = Minecraft.getInstance();
      if (!init(mc)) {
         return 1;
      }

      Item item = autismclient.commands.RegistrySuggestions.init(mc, spec);
      if (item == null) {
         item = autismclient.commands.RegistrySuggestions.init_Item(spec);
      }

      if (item == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown item: §f" + spec.replace('_', ' '));
         return 1;
      }

      Identifier id = BuiltInRegistries.ITEM.getKey(item);
      if (requested > 0) {
         return init(mc, requested, id, item);
      }

      int available = 0;

      for (int slot : init(mc, item)) {
         available += mc.player.getInventory().getItem(slot).getCount();
      }

      if (available == 0) {
         AutismClientMessaging.sendPrefixed("§cYou do not have §f" + id + "§c in your inventory.");
         return 1;
      } else {
         return init(mc, available, id, item);
      }
   }

   private static void init_V_3() {
      String prefix = autismclient.commands.CommandManager.init_Str();
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "drop hand");
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "drop fullinventory");
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "drop <amount> [item_id]");
      AutismClientMessaging.sendPrefixed("§eUsage: §f" + prefix + "drop <item> [amount]  §7(e.g. drop acacia log 10, drop all)");
   }

   private static int init_I_3() {
      Minecraft mc = Minecraft.getInstance();
      if (!init(mc)) {
         return 1;
      } else {
         ItemStack held = mc.player.getMainHandItem();
         if (held.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§cYou are not holding an item.");
            return 1;
         } else {
            int amount = held.getCount();
            Identifier id = BuiltInRegistries.ITEM.getKey(held.getItem());
            Compat263.drop(mc.player, true);
            AutismClientMessaging.sendPrefixed("§aDropped §f" + amount + "x " + id + "§a.");
            return 1;
         }
      }
   }

   private static int load() {
      Minecraft mc = Minecraft.getInstance();
      if (!init(mc)) {
         return 1;
      }

      int droppedItems = 0;
      int droppedStacks = 0;

      for (int inventorySlot = 0; inventorySlot < 36; inventorySlot++) {
         ItemStack stack = mc.player.getInventory().getItem(inventorySlot);
         if (!stack.isEmpty()) {
            int handlerSlot = InventorySlotHelper.load_I(mc, inventorySlot);
            if (handlerSlot >= 0) {
               int stackCount = stack.getCount();
               if (InventoryDropHelper.init(mc, handlerSlot, 0) > 0) {
                  droppedItems += stackCount;
                  droppedStacks++;
               }
            }
         }
      }

      if (droppedStacks == 0) {
         AutismClientMessaging.sendPrefixed("§eYour inventory is empty.");
      } else {
         AutismClientMessaging.sendPrefixed("§aDropped §f" + droppedItems + " items §7(" + droppedStacks + " stacks§7)§a.");
      }

      return 1;
   }

   private static int init(int requested) {
      Minecraft mc = Minecraft.getInstance();
      if (!init(mc)) {
         return 1;
      } else {
         int selectedSlot = mc.player.getInventory().getSelectedSlot();
         ItemStack held = mc.player.getInventory().getItem(selectedSlot);
         if (held.isEmpty()) {
            AutismClientMessaging.sendPrefixed("§cYou are not holding an item.");
            return 1;
         } else {
            Identifier id = BuiltInRegistries.ITEM.getKey(held.getItem());
            return init(mc, requested, id, held.getItem());
         }
      }
   }

   private static int init(int requested, String rawItemId) {
      Minecraft mc = Minecraft.getInstance();
      if (!init(mc)) {
         return 1;
      }

      Identifier id = init_Identifier(rawItemId);
      Item item = id == null ? null : BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      if (item == null) {
         item = autismclient.commands.RegistrySuggestions.init(mc, rawItemId);
      }

      if (item == null) {
         item = autismclient.commands.RegistrySuggestions.init_Item(rawItemId);
      }

      if (item == null) {
         AutismClientMessaging.sendPrefixed("§cUnknown item id: §f" + rawItemId);
         return 1;
      } else {
         return init(mc, requested, BuiltInRegistries.ITEM.getKey(item), item);
      }
   }

   private static int init(Minecraft mc, int requested, Identifier id, Item item) {
      List<Integer> matchingSlots = init(mc, item);
      int available = 0;

      for (int slot : matchingSlots) {
         available += mc.player.getInventory().getItem(slot).getCount();
      }

      if (available == 0) {
         AutismClientMessaging.sendPrefixed("§cYou do not have §f" + id + "§c in your inventory.");
         return 1;
      }

      int remaining = Math.min(requested, available);
      int dropped = 0;

      for (int inventorySlot : matchingSlots) {
         if (remaining <= 0) {
            break;
         }

         ItemStack stack = mc.player.getInventory().getItem(inventorySlot);
         if (!stack.isEmpty() && stack.getItem() == item) {
            int amount = Math.min(remaining, stack.getCount());
            int handlerSlot = InventorySlotHelper.load_I(mc, inventorySlot);
            if (handlerSlot >= 0 && InventoryDropHelper.init(mc, handlerSlot, amount) != 0) {
               dropped += amount;
               remaining -= amount;
            }
         }
      }

      if (dropped == 0) {
         AutismClientMessaging.sendPrefixed("§cCould not drop §f" + id + "§c from the current screen.");
      } else {
         init(id, dropped, requested);
      }

      return 1;
   }

   private static List<Integer> init(Minecraft mc, Item item) {
      List<Integer> slots = new ArrayList<>();
      int selected = mc.player.getInventory().getSelectedSlot();
      init(mc, item, selected, slots);

      for (int slot = 0; slot < 9; slot++) {
         if (slot != selected) {
            init(mc, item, slot, slots);
         }
      }

      for (int slot = 9; slot < 36; slot++) {
         init(mc, item, slot, slots);
      }

      return slots;
   }

   private static void init(Minecraft mc, Item item, int slot, List<Integer> slots) {
      ItemStack stack = mc.player.getInventory().getItem(slot);
      if (!stack.isEmpty() && stack.getItem() == item) {
         slots.add(slot);
      }
   }

   private static Identifier init_Identifier(String raw) {
      if (raw != null && !raw.isBlank()) {
         String value = raw.trim().toLowerCase(Locale.ROOT);
         return Identifier.tryParse(value.indexOf(58) >= 0 ? value : "minecraft:" + value);
      } else {
         return null;
      }
   }

   private static void init(Identifier id, int dropped, int requested) {
      if (dropped < requested) {
         AutismClientMessaging.sendPrefixed("§eDropped §f" + dropped + "/" + requested + "x " + id + "§e (all available).");
      } else {
         AutismClientMessaging.sendPrefixed("§aDropped §f" + dropped + "x " + id + "§a.");
      }
   }

   private static boolean init(Minecraft mc) {
      if (mc.player != null && mc.gameMode != null) {
         return true;
      }

      AutismClientMessaging.sendPrefixed("§cNot in a world.");
      return false;
   }

   record Inner_DataUtil(DropCommand.Inner_DataUtil.Inner_DataUtil_2 init_TextFormat_DataUti, int init_I, String init_Str) {
      enum Inner_DataUtil_2 {
         FULL,
         HAND,
         ITEM,
         UNKNOWN;
      }
   }
}
