package autismclient.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class CachePool {
   private static volatile Map<String, String> init;

   private CachePool() {
   }

   public static String init_Str(String spec) {
      return spec == null ? "" : spec.trim().toLowerCase(Locale.ROOT).replace('-', '_').replaceAll("\\s+", "_");
   }

   public static String init(Identifier id) {
      return id.getNamespace().equals("minecraft") ? id.getPath() : id.toString();
   }

   public static List<String> init_List(String path) {
      List<String> forms = new ArrayList<>(2);
      if (path.endsWith("es") && path.length() > 3) {
         forms.add(path.substring(0, path.length() - 2));
      }

      if (path.endsWith("s") && path.length() > 2) {
         forms.add(path.substring(0, path.length() - 1));
      }

      return forms;
   }

   public static Identifier init_Identifier(String spec) {
      String clean = init_Str(spec);
      if (!clean.isEmpty() && !clean.startsWith("#")) {
         int colon = clean.indexOf(58);
         String namespace = colon < 0 ? "minecraft" : clean.substring(0, colon);
         String path = colon < 0 ? clean : clean.substring(colon + 1);
         Identifier exact = init(namespace, path);
         if (exact != null) {
            return exact;
         }

         for (String form : init_List(path)) {
            Identifier id = init(namespace, form);
            if (id != null) {
               return id;
            }
         }

         List<String> forms = new ArrayList<>();
         forms.add(path);
         forms.addAll(init_List(path));

         for (String form : forms) {
            String hit = init().get(namespace + ":" + form.replace("_", ""));
            if (hit != null) {
               return Identifier.tryParse(hit);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static Item init_Item(String spec) {
      Identifier id = init_Identifier(spec);
      if (id == null) {
         return null;
      }

      Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
      if (item != null) {
         return item;
      }

      Block block = BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
      return block != null && block.asItem() != Items.AIR ? block.asItem() : null;
   }

   public static Block init_Block(String spec) {
      Identifier id = init_Identifier(spec);
      return id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
   }

   public static Item init(Minecraft mc, String spec) {
      if (mc != null && mc.player != null) {
         String clean = init_Str(spec);
         if (clean.isEmpty()) {
            return null;
         }

         List<ItemStack> stacks = new ArrayList<>();

         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               stacks.add(stack);
            }
         }

         Identifier id = init_Identifier(clean);
         if (id != null) {
            for (ItemStack stack : stacks) {
               if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id)) {
                  return stack.getItem();
               }
            }
         }

         String squashed = clean.replace("_", "");
         Item prefix = null;

         for (ItemStack stack : stacks) {
            String name = init_Str(stack.getHoverName().getString());
            if (name.equals(clean) || name.replace("_", "").equals(squashed)) {
               return stack.getItem();
            }

            if (prefix == null && (name.startsWith(clean) || name.replace("_", "").startsWith(squashed))) {
               prefix = stack.getItem();
            }
         }

         return prefix;
      } else {
         return null;
      }
   }

   public static List<String> init(Minecraft mc) {
      TreeSet<String> ids = new TreeSet<>();
      if (mc != null && mc.player != null) {
         for (int slot = 0; slot < mc.player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = mc.player.getInventory().getItem(slot);
            if (!stack.isEmpty()) {
               ids.add(init(BuiltInRegistries.ITEM.getKey(stack.getItem())));
            }
         }

         return new ArrayList<>(ids);
      } else {
         return List.of();
      }
   }

   public static <S> CompletableFuture<Suggestions> init(CommandContext<S> ignored, SuggestionsBuilder builder) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String id : init(Minecraft.getInstance())) {
         if (id.startsWith(remaining)) {
            builder.suggest(id);
         }
      }

      return builder.buildFuture();
   }

   private static Identifier init(String namespace, String path) {
      Identifier id = Identifier.tryParse(namespace + ":" + path);
      if (id == null) {
         return null;
      } else {
         return !BuiltInRegistries.ITEM.containsKey(id) && !BuiltInRegistries.BLOCK.containsKey(id) ? null : id;
      }
   }

   private static Map<String, String> init() {
      Map<String, String> map = init;
      if (map != null) {
         return map;
      }

      map = new HashMap<>();

      for (Identifier key : BuiltInRegistries.BLOCK.keySet()) {
         map.putIfAbsent(key.getNamespace() + ":" + key.getPath().replace("_", ""), key.toString());
      }

      for (Identifier key : BuiltInRegistries.ITEM.keySet()) {
         map.putIfAbsent(key.getNamespace() + ":" + key.getPath().replace("_", ""), key.toString());
      }

      init = map;
      return map;
   }
}
