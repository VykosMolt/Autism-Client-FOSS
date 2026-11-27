package autismclient.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

final class AiWorldReaders {
   private static final int init_I = 15;
   private static final int load = 64;
   private static final int read = 8;
   private static final int write = 32;
   private static final int update = 100;
   private static final int sync = 640;
   private static final int flush = 64;
   private static final int close = 32;
   private static final int open = 32;
   private static final int poll = 27;
   private static final int push = 512;
   private static final int pop = 6000;
   private static final int tick = 5700;
   private static final String init_Str = "bookPages";

   private AiWorldReaders() {
   }

   static JsonArray scoreboard(Minecraft mc) {
      JsonArray rows = new JsonArray();

      try {
         for (autismclient.util.macro.CaptureValueAction.Inner_JsonUtil line : autismclient.util.macro.CaptureValueAction.init_List_Minecraft_2(mc)) {
            if (rows.size() >= 15) {
               break;
            }

            if (line != null) {
               rows.add(
                  AiJson.objectOf(
                     "row",
                     line.init_I(),
                     "objective",
                     init(line.load(), 128),
                     "objectiveTitle",
                     init(line.read(), 256),
                     "name",
                     init(line.update(), 256),
                     "score",
                     init(line.sync(), 64),
                     "text",
                     init(line.flush(), 512)
                  )
               );
            }
         }
      } catch (Throwable var4) {
      }

      return rows;
   }

   static JsonArray tabList(Minecraft mc) {
      JsonArray players = new JsonArray();

      try {
         for (autismclient.util.TabPlayerListParser.ParsedPlayer player : autismclient.util.TabPlayerListParser.init(mc)) {
            if (players.size() >= 64) {
               break;
            }

            if (player != null && player.init_Str() != null) {
               players.add(AiJson.objectOf("name", init(player.init_Str(), 64), "prefix", init(player.load(), 128)));
            }
         }
      } catch (Throwable var4) {
      }

      return players;
   }

   static JsonObject tabDecoration(Minecraft mc) {
      JsonObject decoration = new JsonObject();

      try {
         if (mc == null || mc.gui == null || mc.gui.hud == null) {
            return decoration;
         }

         PlayerTabOverlay overlay = mc.gui.hud.getTabList();
         decoration.addProperty("header", init(init(AiWorldReaders.Inner_DataUtil.init == null ? null : AiWorldReaders.Inner_DataUtil.init.get(overlay)), 1024));
         decoration.addProperty("footer", init(init(AiWorldReaders.Inner_DataUtil.load == null ? null : AiWorldReaders.Inner_DataUtil.load.get(overlay)), 1024));
      } catch (Throwable var3) {
      }

      return decoration;
   }

   static JsonArray read(Minecraft mc) {
      JsonArray bars = new JsonArray();

      try {
         if (mc == null || mc.gui == null || mc.gui.hud == null) {
            return bars;
         }

         if ((AiWorldReaders.Inner_DataUtil.read == null ? null : AiWorldReaders.Inner_DataUtil.read.get(mc.gui.hud.getBossOverlay())) instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
               if (bars.size() >= 8) {
                  break;
               }

               if (value instanceof BossEvent event) {
                  bars.add(AiJson.objectOf("title", init(init(event.getName()), 256), "progress", event.getProgress()));
               }
            }
         }
      } catch (Throwable var6) {
      }

      return bars;
   }

   static JsonObject vitals(Minecraft mc) {
      JsonObject vitals = new JsonObject();

      try {
         if (mc == null || mc.player == null) {
            return vitals;
         }

         vitals.addProperty("experienceLevel", mc.player.experienceLevel);
         vitals.addProperty("experienceProgress", mc.player.experienceProgress);
         vitals.addProperty("airSupply", mc.player.getAirSupply());
         vitals.addProperty("maxAirSupply", mc.player.getMaxAirSupply());
         vitals.addProperty("armor", mc.player.getArmorValue());
      } catch (Throwable var3) {
      }

      return vitals;
   }

   static JsonObject init(ItemStack stack) {
      JsonObject summary = new JsonObject();

      try {
         if (stack == null || stack.isEmpty()) {
            return summary;
         }

         Component customName = stack.get(DataComponents.CUSTOM_NAME);
         if (customName != null) {
            summary.addProperty("customName", init(customName.getString(), 64));
         }

         JsonArray enchants = new JsonArray();
         init(enchants, stack.get(DataComponents.ENCHANTMENTS), 8);
         if (enchants.size() > 0) {
            summary.add("enchants", enchants);
         }

         JsonArray stored = new JsonArray();
         init(stored, stack.get(DataComponents.STORED_ENCHANTMENTS), 8);
         if (stored.size() > 0) {
            summary.add("storedEnchants", stored);
         }

         ItemLore lore = stack.get(DataComponents.LORE);
         if (lore != null && !lore.lines().isEmpty()) {
            summary.addProperty("loreLines", lore.lines().size());
         }

         CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
         if (modelData != null) {
            if (!modelData.floats().isEmpty() && modelData.floats().get(0) != null) {
               summary.addProperty("customModelData", modelData.floats().get(0));
            } else if (!modelData.strings().isEmpty() && modelData.strings().get(0) != null) {
               summary.addProperty("customModelData", init(modelData.strings().get(0), 64));
            } else if (!modelData.flags().isEmpty() && modelData.flags().get(0) != null) {
               summary.addProperty("customModelData", modelData.flags().get(0));
            } else if (!modelData.colors().isEmpty() && modelData.colors().get(0) != null) {
               summary.addProperty("customModelData", modelData.colors().get(0));
            }
         }

         PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
         if (potion != null && (potion.potion().isPresent() || !potion.customEffects().isEmpty() || potion.customName().isPresent())) {
            summary.addProperty("potion", potion.customName().map(name -> init(name, 64)).orElseGet(() -> potion.potion().map(AiWorldReaders::init).orElse("")));
            JsonArray effects = new JsonArray();

            for (MobEffectInstance effect : potion.getAllEffects()) {
               if (effect != null) {
                  if (effects.size() >= 4) {
                     break;
                  }

                  effects.add(init(effect.getEffect()) + " " + effect.getAmplifier());
               }
            }

            if (effects.size() > 0) {
               summary.add("potionEffects", effects);
            }
         }

         WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
         if (written != null) {
            summary.add(
               "writtenBook",
               AiJson.objectOf("title", init(written.title() == null ? "" : written.title().raw(), 48), "author", init(written.author(), 48))
            );
         }

         ResolvableProfile profile = stack.get(DataComponents.PROFILE);
         if (profile != null) {
            String name = null;

            try {
               name = profile.name().orElse(null);
            } catch (Throwable var12) {
            }

            if (name != null && !name.isBlank()) {
               summary.addProperty("profile", init(name, 64));
            }
         }

         if (stack.get(DataComponents.TRIM) != null) {
            summary.addProperty("trimmed", true);
         }

         if (stack.get(DataComponents.DYED_COLOR) != null) {
            summary.addProperty("dyed", true);
         }

         ItemContainerContents container = stack.get(DataComponents.CONTAINER);
         if (container != null) {
            summary.addProperty("containerItems", init(container));
         }

         if (stack.get(DataComponents.FOOD) != null) {
            summary.addProperty("food", true);
         }

         if (stack.get(DataComponents.CONSUMABLE) != null) {
            summary.addProperty("consumable", true);
         }

         init_V(summary);
      } catch (Throwable var13) {
      }

      return summary;
   }

   static JsonObject init(ItemStack stack, int page) {
      JsonObject base = new JsonObject();

      try {
         base.addProperty("item", stack != null && !stack.isEmpty() ? BuiltInRegistries.ITEM.getKey(stack.getItem()).toString() : "minecraft:air");
         base.addProperty("name", stack != null && !stack.isEmpty() ? init(stack.getHoverName().getString(), 256) : "");
         base.addProperty("count", stack == null ? 0 : stack.getCount());
      } catch (Throwable t) {
         base.addProperty("item", "minecraft:air");
         base.addProperty("name", "");
         base.addProperty("count", 0);
      }

      base.addProperty("untrusted", true);

      List<JsonObject> pages;
      try {
         List<Entry<String, JsonElement>> sections = new ArrayList<>();
         if (stack != null && !stack.isEmpty()) {
            init(stack, sections);
         }

         pages = init(base, sections);
      } catch (Throwable t) {
         pages = new ArrayList<>();
         pages.add(init_JsonObject(base));
      }

      int total = pages.size();
      int requested = Math.max(0, page);
      JsonObject out = requested < total ? pages.get(requested) : init_JsonObject(base);
      out.addProperty("page", requested);
      out.addProperty("pages", total);
      return out;
   }

   private static void init(ItemStack stack, List<Entry<String, JsonElement>> sections) {
      try {
         Component customName = stack.get(DataComponents.CUSTOM_NAME);
         if (customName != null) {
            sections.add(Map.entry("customName", AiJson.GSON.toJsonTree(init(customName.getString(), 512))));
         }
      } catch (Throwable var19) {
      }

      try {
         JsonArray enchantments = new JsonArray();
         init(enchantments, stack.get(DataComponents.ENCHANTMENTS), false);
         init(enchantments, stack.get(DataComponents.STORED_ENCHANTMENTS), true);
         if (enchantments.size() > 0) {
            sections.add(Map.entry("enchantments", enchantments));
         }
      } catch (Throwable var18) {
      }

      try {
         ItemLore lore = stack.get(DataComponents.LORE);
         if (lore != null && !lore.lines().isEmpty()) {
            JsonArray lines = new JsonArray();
            lore.lines().stream().limit(32L).forEach(line -> lines.add(init(line.getString(), 256)));
            sections.add(Map.entry("lore", lines));
         }
      } catch (Throwable var17) {
      }

      try {
         CustomModelData modelData = stack.get(DataComponents.CUSTOM_MODEL_DATA);
         if (modelData != null
            && (!modelData.floats().isEmpty() || !modelData.flags().isEmpty() || !modelData.strings().isEmpty() || !modelData.colors().isEmpty())) {
            JsonObject object = new JsonObject();
            JsonArray floats = new JsonArray();
            modelData.floats().stream().limit(16L).forEach(value -> floats.add(value == null ? JsonNull.INSTANCE : AiJson.GSON.toJsonTree(value)));
            if (floats.size() > 0) {
               object.add("floats", floats);
            }

            JsonArray flags = new JsonArray();
            modelData.flags().stream().limit(16L).forEach(value -> flags.add(value == null ? JsonNull.INSTANCE : AiJson.GSON.toJsonTree(value)));
            if (flags.size() > 0) {
               object.add("flags", flags);
            }

            JsonArray strings = new JsonArray();
            modelData.strings().stream().limit(8L).forEach(value -> strings.add(init(value, 128)));
            if (strings.size() > 0) {
               object.add("strings", strings);
            }

            JsonArray colors = new JsonArray();
            modelData.colors().stream().limit(16L).forEach(value -> colors.add(value == null ? JsonNull.INSTANCE : AiJson.GSON.toJsonTree(value)));
            if (colors.size() > 0) {
               object.add("colors", colors);
            }

            sections.add(Map.entry("customModelData", object));
         }
      } catch (Throwable var22) {
      }

      try {
         PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
         if (potion != null
            && (potion.potion().isPresent() || !potion.customEffects().isEmpty() || potion.customName().isPresent() || potion.customColor().isPresent())) {
            JsonObject object = new JsonObject();
            potion.potion().ifPresent(holder -> object.addProperty("name", init((Holder<?>)holder)));
            potion.customName().ifPresent(name -> object.addProperty("customName", init(name, 256)));
            potion.customColor().ifPresent(color -> object.addProperty("color", color));
            JsonArray effects = new JsonArray();

            for (MobEffectInstance effect : potion.getAllEffects()) {
               if (effects.size() >= 32) {
                  break;
               }

               if (effect != null) {
                  effects.add(AiJson.objectOf("id", init(effect.getEffect()), "amplifier", effect.getAmplifier(), "duration", effect.getDuration()));
               }
            }

            if (effects.size() > 0) {
               object.add("effects", effects);
            }

            sections.add(Map.entry("potion", object));
         }
      } catch (Throwable var21) {
      }

      try {
         ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (modifiers != null && !modifiers.modifiers().isEmpty()) {
            JsonArray entries = new JsonArray();

            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
               if (entries.size() >= 32) {
                  break;
               }

               if (entry != null && entry.modifier() != null) {
                  entries.add(
                     AiJson.objectOf(
                        "attribute",
                        init(entry.attribute()),
                        "amount",
                        entry.modifier().amount(),
                        "operation",
                        entry.modifier().operation() == null ? "" : entry.modifier().operation().getSerializedName(),
                        "slot",
                        entry.slot() == null ? "" : entry.slot().getSerializedName()
                     )
                  );
               }
            }

            if (entries.size() > 0) {
               sections.add(Map.entry("attributes", entries));
            }
         }
      } catch (Throwable var20) {
      }

      try {
         ResolvableProfile profile = stack.get(DataComponents.PROFILE);
         if (profile != null) {
            String name = null;

            try {
               name = profile.name().orElse(null);
            } catch (Throwable var15) {
            }

            if (name != null && !name.isBlank()) {
               sections.add(Map.entry("profile", AiJson.objectOf("name", init(name, 64))));
            }
         }
      } catch (Throwable var16) {
      }

      try {
         if (stack.get(DataComponents.TRIM) instanceof ArmorTrim armorTrim) {
            sections.add(Map.entry("trim", AiJson.objectOf("material", init(armorTrim.material()), "pattern", init(armorTrim.pattern()))));
         }
      } catch (Throwable var14) {
      }

      try {
         DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
         if (dyed != null) {
            sections.add(Map.entry("dyedColor", AiJson.GSON.toJsonTree(dyed.rgb())));
         }
      } catch (Throwable var13) {
      }

      try {
         if (stack.getMaxDamage() > 0) {
            sections.add(Map.entry("damage", AiJson.objectOf("current", stack.getDamageValue(), "max", stack.getMaxDamage())));
         }
      } catch (Throwable var12) {
      }

      try {
         FoodProperties food = stack.get(DataComponents.FOOD);
         if (food != null) {
            sections.add(
               Map.entry("food", AiJson.objectOf("nutrition", food.nutrition(), "saturation", food.saturation(), "canAlwaysEat", food.canAlwaysEat()))
            );
         }
      } catch (Throwable var11) {
      }

      try {
         if (stack.get(DataComponents.CONSUMABLE) != null) {
            sections.add(Map.entry("consumable", AiJson.GSON.toJsonTree(true)));
         }
      } catch (Throwable var10) {
      }

      try {
         ItemContainerContents container = stack.get(DataComponents.CONTAINER);
         if (container != null) {
            sections.add(Map.entry("container", AiJson.objectOf("count", init(container))));
            JsonArray items = new JsonArray();
            int[] index = new int[]{0};
            container.nonEmptyItemCopyStream().limit(27L).forEach(inner -> {
               JsonObject entry = new JsonObject();
               entry.addProperty("slot", Integer.valueOf((int)(index[0]++)));
               entry.addProperty("item", BuiltInRegistries.ITEM.getKey(inner.getItem()).toString());
               entry.addProperty("count", inner.getCount());
               entry.addProperty("name", init(inner.getHoverName().getString(), 128));
               entry.add("components", init(inner));
               items.add(entry);
            });
            if (items.size() > 0) {
               sections.add(Map.entry("containerItems", items));
            }
         }
      } catch (Throwable var9) {
      }

      try {
         WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
         WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
         if (written != null) {
            JsonObject book = new JsonObject();
            book.addProperty("kind", "written");
            book.addProperty("title", init(written.title() == null ? "" : written.title().raw(), 256));
            book.addProperty("author", init(written.author(), 256));
            book.addProperty("generation", written.generation());
            book.addProperty("totalPages", written.pages().size());
            sections.add(Map.entry("book", book));
            JsonArray bookPages = new JsonArray();
            written.pages()
               .stream()
               .limit(100L)
               .forEach(bookPage -> bookPages.add(init(bookPage != null && bookPage.raw() != null ? bookPage.raw().getString() : "", 640)));
            sections.add(Map.entry("bookPages", bookPages));
         } else if (writable != null) {
            JsonObject book = new JsonObject();
            book.addProperty("kind", "writable");
            book.addProperty("totalPages", writable.pages().size());
            sections.add(Map.entry("book", book));
            JsonArray bookPages = new JsonArray();
            writable.pages()
               .stream()
               .limit(100L)
               .forEach(bookPage -> bookPages.add(init(bookPage != null && bookPage.raw() != null ? String.valueOf(bookPage.raw()) : "", 640)));
            sections.add(Map.entry("bookPages", bookPages));
         }
      } catch (Throwable var8) {
      }
   }

   private static void init(JsonArray out, ItemEnchantments enchantments, int cap) {
      if (enchantments != null) {
         for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (out.size() >= cap) {
               break;
            }

            out.add(init(entry.getKey()) + " " + entry.getIntValue());
         }
      }
   }

   private static void init(JsonArray out, ItemEnchantments enchantments, boolean stored) {
      if (enchantments != null) {
         for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            if (out.size() >= 64) {
               break;
            }

            out.add(AiJson.objectOf("id", init(entry.getKey()), "level", entry.getIntValue(), "stored", stored));
         }
      }
   }

   private static int init(ItemContainerContents container) {
      int count = 0;

      for (Object ignored : container.nonEmptyItems()) {
         if (++count >= 256) {
            break;
         }
      }

      return count;
   }

   private static void init_V(JsonObject summary) {
      boolean halved = true;

      while (init(summary) > 512 && halved) {
         halved = false;

         for (String key : new String[]{"enchants", "storedEnchants", "potionEffects"}) {
            if (summary.get(key) instanceof JsonArray array && array.size() > 1) {
               int kept = Math.max(1, array.size() / 2);
               int dropped = array.size() - kept;

               while (array.size() > kept) {
                  array.remove(array.size() - 1);
               }

               array.add("…(+" + dropped + ")");
               halved = true;
            }
         }
      }

      for (String key : new String[]{
         "customModelData", "writtenBook", "potionEffects", "profile", "storedEnchants", "enchants", "potion", "loreLines", "customName"
      }) {
         if (init(summary) <= 512) {
            return;
         }

         summary.remove(key);
      }
   }

   private static List<JsonObject> init(JsonObject base, List<Entry<String, JsonElement>> sections) {
      List<JsonObject> pages = new ArrayList<>();
      JsonObject current = init_JsonObject(base);

      for (Entry<String, JsonElement> section : sections) {
         String key = section.getKey();
         JsonElement value = section.getValue();
         if (value instanceof JsonArray array && array.size() > 0) {
            JsonArray slice = new JsonArray();
            int sliceStart = 0;
            int sliceSize = 0;

            for (int i = 0; i < array.size(); i++) {
               JsonElement element = array.get(i);
               int elementSize = init(element) + 1;
               boolean pageHasContent = current.size() > base.size() || slice.size() > 0;
               if (init(current) + sliceSize + key.length() + 8 + elementSize > 5700 && pageHasContent) {
                  if (slice.size() > 0) {
                     init(current, key, slice, sliceStart);
                     pages.add(current);
                     current = init_JsonObject(base);
                     slice = new JsonArray();
                     sliceSize = 0;
                     sliceStart = i;
                  } else {
                     pages.add(current);
                     current = init_JsonObject(base);
                  }
               }

               slice.add(element);
               sliceSize += elementSize;
            }

            if (slice.size() > 0) {
               init(current, key, slice, sliceStart);
            }
         } else {
            if (init(current) + key.length() + 4 + init(value) > 5700 && current.size() > base.size()) {
               pages.add(current);
               current = init_JsonObject(base);
            }

            current.add(key, value);
         }
      }

      pages.add(current);
      return pages;
   }

   private static void init(JsonObject page, String key, JsonArray slice, int sliceStart) {
      page.add(key, slice);
      if ("bookPages".equals(key)) {
         page.addProperty("bookPageOffset", sliceStart);
      }
   }

   private static JsonObject init_JsonObject(JsonObject base) {
      JsonObject page = new JsonObject();

      for (Entry<String, JsonElement> entry : base.entrySet()) {
         page.add(entry.getKey(), entry.getValue());
      }

      return page;
   }

   private static int init(JsonElement element) {
      return AiJson.GSON.toJson(element).length();
   }

   private static String init(Holder<?> holder) {
      if (holder == null) {
         return "[unregistered]";
      }

      try {
         return holder.unwrapKey().map(key -> key.identifier().toString()).orElse("[unregistered]");
      } catch (Throwable t) {
         return "[unregistered]";
      }
   }

   private static String init(Object value) {
      return value instanceof Component component ? component.getString() : "";
   }

   private static String init(String text, int max) {
      if (text == null) {
         return "";
      } else {
         return text.length() <= max ? text : text.substring(0, max);
      }
   }

   private static final class Inner_DataUtil {
      private static final Field init = init(PlayerTabOverlay.class, "header");
      private static final Field load = init(PlayerTabOverlay.class, "footer");
      private static final Field read = init(BossHealthOverlay.class, "events");

      private static Field init(Class<?> type, String name) {
         try {
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field;
         } catch (Throwable t) {
            return null;
         }
      }
   }
}
