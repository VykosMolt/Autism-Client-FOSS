package autismclient.util;

import autismclient.compat263.Compat263;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ItemNbtInspector {
   private static final int init = 40000;

   private ItemNbtInspector() {
   }

   public static ItemNbtInspector.ItemInspection inspect(ItemStack source) {
      ItemStack stack = source == null ? ItemStack.EMPTY : source.copy();
      String title = stack.isEmpty() ? "Item NBT" : stack.getHoverName().getString();
      ItemNbtInspector.InspectLineBuilder nice = new ItemNbtInspector.InspectLineBuilder(title);
      ItemNbtInspector.InspectLineBuilder raw = new ItemNbtInspector.InspectLineBuilder(title + " Raw");
      String rawSnbt = ItemSnbtCodec.snbt(stack);
      String giveCommand = ItemSnbtCodec.giveCommand(stack);
      appendFriendly(stack, nice);
      appendRaw(rawSnbt, raw);
      return new ItemNbtInspector.ItemInspection(title, stack, nice.build(), raw.build(), nice.toText(), rawSnbt, giveCommand);
   }

   private static void appendFriendly(ItemStack stack, ItemNbtInspector.InspectLineBuilder builder) {
      if (stack != null && !stack.isEmpty()) {
         builder.section("Identity", UiColorPalette.warmWhite());
         builder.line("Item: " + BuiltInRegistries.ITEM.getKey(stack.getItem()), UiColorPalette.softWhite());
         builder.line("Count: " + stack.getCount(), UiColorPalette.textBody());
         builder.line("Hover Name: " + stack.getHoverName().getString(), UiColorPalette.textBody());
         builder.line("Components: " + stack.getComponentsPatch().size() + " non-default", UiColorPalette.textMuted());
         appendIds(builder, stack);
         Component customName = stack.get(DataComponents.CUSTOM_NAME);
         Component itemName = stack.get(DataComponents.ITEM_NAME);
         ItemLore lore = stack.get(DataComponents.LORE);
         if (customName != null || itemName != null || lore != null && !lore.lines().isEmpty()) {
            builder.blank();
            builder.section("Display", UiColorPalette.cyan());
            if (customName != null) {
               builder.line("Custom Name: " + customName.getString(), UiColorPalette.softWhite());
            }

            if (itemName != null) {
               builder.line("Item Name: " + itemName.getString(), UiColorPalette.textBody());
            }

            if (lore != null && !lore.lines().isEmpty()) {
               int i = 1;

               for (Component line : lore.lines()) {
                  builder.line("Lore " + i++ + ": " + line.getString(), UiColorPalette.textBody());
               }
            }
         }

         appendSection(
            builder,
            stack,
            "Model / Visuals",
            UiColorPalette.cyan(),
            summary(DataComponents.ITEM_MODEL, "Item Model"),
            summary(DataComponents.CUSTOM_MODEL_DATA, "Custom Model Data"),
            summary(DataComponents.TOOLTIP_STYLE, "Tooltip Style"),
            summary(DataComponents.DYED_COLOR, "Dyed Color"),
            summary(DataComponents.DYE, "Dye"),
            summary(DataComponents.RARITY, "Rarity"),
            summary(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, "Glint Override"),
            summary(DataComponents.TOOLTIP_DISPLAY, "Tooltip Display")
         );
         appendEnchantments(builder, "Enchantments", stack.get(DataComponents.ENCHANTMENTS));
         appendEnchantments(builder, "Stored Enchantments", stack.get(DataComponents.STORED_ENCHANTMENTS));
         ItemAttributeModifiers attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (attributes != null && !attributes.modifiers().isEmpty()) {
            builder.blank();
            builder.section("Attributes", UiColorPalette.amber());

            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : attributes.modifiers()) {
               String attribute = holderName(entry.attribute());
               builder.line(
                  attribute
                     + ": "
                     + entry.modifier().amount()
                     + " "
                     + entry.modifier().operation().getSerializedName()
                     + " ("
                     + entry.slot()
                     + ", "
                     + entry.modifier().id()
                     + ")",
                  UiColorPalette.textBody()
               );
            }
         }

         if (stack.has(DataComponents.MAX_DAMAGE)
            || stack.has(DataComponents.DAMAGE)
            || stack.has(DataComponents.UNBREAKABLE)
            || stack.has(DataComponents.MAX_STACK_SIZE)
            || stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
            || stack.has(DataComponents.RARITY)) {
            builder.blank();
            builder.section("Item Rules", UiColorPalette.warningYellow());
            if (stack.has(DataComponents.MAX_DAMAGE)) {
               builder.line("Max Damage: " + stack.getMaxDamage(), UiColorPalette.textBody());
            }

            if (stack.has(DataComponents.DAMAGE)) {
               builder.line("Damage: " + stack.getDamageValue(), UiColorPalette.textBody());
            }

            if (stack.has(DataComponents.UNBREAKABLE)) {
               builder.line("Unbreakable: true", UiColorPalette.textBody());
            }

            if (stack.has(DataComponents.MAX_STACK_SIZE)) {
               builder.line("Max Stack: " + stack.getMaxStackSize(), UiColorPalette.textBody());
            }
         }

         appendSection(
            builder,
            stack,
            "Usage / Combat",
            UiColorPalette.amber(),
            summary(DataComponents.USE_EFFECTS, "Use Effects"),
            summary(DataComponents.MINIMUM_ATTACK_CHARGE, "Minimum Attack Charge"),
            summary(DataComponents.DAMAGE_TYPE, "Damage Type"),
            summary(DataComponents.FOOD, "Food"),
            summary(DataComponents.CONSUMABLE, "Consumable"),
            summary(DataComponents.USE_REMAINDER, "Use Remainder"),
            summary(DataComponents.USE_COOLDOWN, "Use Cooldown"),
            summary(DataComponents.DAMAGE_RESISTANT, "Damage Resistant"),
            summary(DataComponents.TOOL, "Tool"),
            summary(DataComponents.WEAPON, "Weapon"),
            summary(DataComponents.ATTACK_RANGE, "Attack Range"),
            summary(DataComponents.BLOCKS_ATTACKS, "Blocks Attacks"),
            summary(DataComponents.PIERCING_WEAPON, "Piercing Weapon"),
            summary(DataComponents.KINETIC_WEAPON, "Kinetic Weapon"),
            summary(DataComponents.ATTACK_ANIMATION, "Swing Animation")
         );
         appendSection(
            builder,
            stack,
            "Equipment / Enchanting",
            UiColorPalette.blue(),
            summary(DataComponents.ENCHANTABLE, "Enchantable"),
            summary(DataComponents.EQUIPPABLE, "Equippable"),
            summary(DataComponents.REPAIRABLE, "Repairable"),
            summary(DataComponents.GLIDER, "Glider"),
            summary(DataComponents.REPAIR_COST, "Repair Cost"),
            summary(DataComponents.CREATIVE_SLOT_LOCK, "Creative Slot Lock"),
            summary(DataComponents.INTANGIBLE_PROJECTILE, "Intangible Projectile"),
            summary(DataComponents.DEATH_PROTECTION, "Death Protection"),
            summary(DataComponents.TRIM, "Armor Trim"),
            summary(DataComponents.PROVIDES_TRIM_MATERIAL, "Provides Trim Material"),
            summary(DataComponents.BREAK_SOUND, "Break Sound")
         );
         appendSection(
            builder,
            stack,
            "Placement / Mining",
            UiColorPalette.warningYellow(),
            summary(DataComponents.CAN_PLACE_ON, "Can Place On"),
            summary(DataComponents.CAN_BREAK, "Can Break"),
            summary(DataComponents.BLOCK_STATE, "Block State"),
            summary(DataComponents.LOCK, "Lock")
         );
         ItemContainerContents container = stack.get(DataComponents.CONTAINER);
         BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
         if (container != null || bundle != null) {
            builder.blank();
            builder.section("Contents", UiColorPalette.mint());
            if (container != null) {
               long count = container.nonEmptyItemCopyStream().count();
               builder.line("Container Items: " + count, UiColorPalette.textBody());
            }

            if (bundle != null) {
               builder.line("Bundle Items: " + bundle.size(), UiColorPalette.textBody());
            }
         }

         appendSection(
            builder,
            stack,
            "Containers / Projectiles",
            UiColorPalette.mint(),
            summary(DataComponents.CONTAINER, "Container"),
            summary(DataComponents.BUNDLE_CONTENTS, "Bundle Contents"),
            summary(DataComponents.CHARGED_PROJECTILES, "Charged Projectiles"),
            summary(DataComponents.CONTAINER_LOOT, "Container Loot"),
            summary(DataComponents.BEES, "Bees"),
            summary(DataComponents.RECIPES, "Recipes")
         );
         if (stack.has(DataComponents.BLOCK_ENTITY_DATA)
            || stack.has(DataComponents.BUCKET_ENTITY_DATA)
            || stack.has(DataComponents.ENTITY_DATA)
            || stack.has(DataComponents.MAP_ID)
            || stack.has(DataComponents.BANNER_PATTERNS)) {
            builder.blank();
            builder.section("Special Data", UiColorPalette.magenta());
            appendComponent(builder, stack, DataComponents.BLOCK_ENTITY_DATA, "Block Entity Data");
            appendComponent(builder, stack, DataComponents.BUCKET_ENTITY_DATA, "Bucket Entity Data");
            appendComponent(builder, stack, DataComponents.ENTITY_DATA, "Entity Data");
            appendComponent(builder, stack, DataComponents.MAP_ID, "Map ID");
            appendComponent(builder, stack, DataComponents.BANNER_PATTERNS, "Banner Patterns");
         }

         appendSection(
            builder,
            stack,
            "Potions / Maps",
            UiColorPalette.magenta(),
            summary(DataComponents.POTION_CONTENTS, "Potion Contents"),
            summary(DataComponents.POTION_DURATION_SCALE, "Potion Duration Scale"),
            summary(DataComponents.SUSPICIOUS_STEW_EFFECTS, "Suspicious Stew Effects"),
            summary(DataComponents.MAP_ID, "Map Color"),
            summary(DataComponents.MAP_ID, "Map ID"),
            summary(DataComponents.MAP_DECORATIONS, "Map Decorations"),
            summary(DataComponents.MAP_POST_PROCESSING, "Map Post Processing")
         );
         appendSection(
            builder,
            stack,
            "Fireworks / Music / Profiles",
            UiColorPalette.warmWhite(),
            summary(DataComponents.FIREWORK_EXPLOSION, "Firework Explosion"),
            summary(DataComponents.FIREWORKS, "Fireworks"),
            summary(DataComponents.PROFILE, "Profile"),
            summary(DataComponents.INSTRUMENT, "Instrument"),
            summary(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, "Ominous Bottle Amplifier"),
            summary(DataComponents.JUKEBOX_PLAYABLE, "Jukebox Playable"),
            summary(DataComponents.NOTE_BLOCK_SOUND, "Note Block Sound")
         );
         appendSection(
            builder,
            stack,
            "Banners / Decorations",
            UiColorPalette.cyan(),
            summary(DataComponents.PROVIDES_BANNER_PATTERNS, "Provides Banner Patterns"),
            summary(DataComponents.BANNER_PATTERNS, "Banner Patterns"),
            summary(DataComponents.BASE_COLOR, "Base Color"),
            summary(DataComponents.POT_DECORATIONS, "Pot Decorations")
         );
         WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
         WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
         if (written != null || writable != null) {
            builder.blank();
            builder.section("Book", UiColorPalette.blue());
            if (written != null) {
               builder.line("Title: " + written.title().raw(), UiColorPalette.textBody());
               builder.line("Author: " + written.author(), UiColorPalette.textBody());
               builder.line("Pages: " + written.pages().size(), UiColorPalette.textBody());
            }

            if (writable != null) {
               builder.line("Pages: " + writable.pages().size(), UiColorPalette.textBody());
            }
         }

         CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            builder.blank();
            builder.section("Custom Data", UiColorPalette.successText());
            CompoundTag tag = customData.copyTag();
            if (tag.isEmpty()) {
               builder.line("{}", UiColorPalette.textMuted());
            } else {
               for (Entry<String, Tag> entry : tag.entrySet()) {
                  builder.line(entry.getKey() + ": " + ItemSnbtCodec.tagText(entry.getValue()), UiColorPalette.textBody());
               }
            }
         }

         boolean wroteOtherComponents = false;

         for (Entry<DataComponentType<?>, Optional<?>> entry : Compat263.dataComponentPatchEntrySet(stack.getComponentsPatch())) {
            String id = componentTypeName(entry.getKey());
            if (!isKnownComponent(id)) {
               if (!wroteOtherComponents) {
                  builder.blank();
                  builder.section("Other Components", UiColorPalette.silver());
                  wroteOtherComponents = true;
               }

               Optional<?> value = entry.getValue();
               if (value.isPresent()) {
                  builder.snbtLine(id + ": " + describe(value.get()));
               } else {
                  builder.snbtLine("!" + id);
               }
            }
         }
      } else {
         builder.section("Empty", UiColorPalette.errorText());
         builder.line("No item stack was selected.", UiColorPalette.errorText());
      }
   }

   private static void appendRaw(String rawSnbt, ItemNbtInspector.InspectLineBuilder builder) {
      builder.section("Raw ItemStack SNBT", UiColorPalette.warmWhite());
      boolean truncated = rawSnbt != null && rawSnbt.length() > 40000;
      String shown = truncated ? rawSnbt.substring(0, 40000) : rawSnbt;

      for (String line : prettyLines(shown)) {
         builder.snbtLine(line);
      }

      if (truncated) {
         builder.snbtLine("... (truncated for display; use Copy for the full NBT)");
      }
   }

   private static void appendEnchantments(ItemNbtInspector.InspectLineBuilder builder, String title, ItemEnchantments enchantments) {
      if (enchantments != null && !enchantments.isEmpty()) {
         builder.blank();
         builder.section(title, UiColorPalette.blue());

         for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            builder.line(holderName(entry.getKey()) + ": " + entry.getIntValue(), UiColorPalette.textBody());
         }
      }
   }

   private static <T> void appendComponent(ItemNbtInspector.InspectLineBuilder builder, ItemStack stack, DataComponentType<T> type, String label) {
      T value = stack.get(type);
      if (value != null) {
         builder.snbtLine(label + ": " + describe(value));
      }
   }

   @SafeVarargs
   private static void appendSection(ItemNbtInspector.InspectLineBuilder builder, ItemStack stack, String title, int sectionColor, ItemNbtInspector.ComponentSummary<?>... summaries) {
      boolean wroteTitle = false;

      for (ItemNbtInspector.ComponentSummary<?> summary : summaries) {
         Object value = componentValue(stack, summary.type());
         if (value != null) {
            if (!wroteTitle) {
               builder.blank();
               builder.section(title, sectionColor);
               wroteTitle = true;
            }

            builder.snbtLine(summary.label() + ": " + describe(value));
         }
      }
   }

   private static Object componentValue(ItemStack stack, DataComponentType<?> type) {
      return stack.get(type);
   }

   private static <T> ItemNbtInspector.ComponentSummary<T> summary(DataComponentType<T> type, String label) {
      return new ItemNbtInspector.ComponentSummary<>(type, label);
   }

   private static void appendIds(ItemNbtInspector.InspectLineBuilder builder, ItemStack stack) {
      Map<String, String> ids = new LinkedHashMap<>();
      CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         collectIds("", customData.copyTag(), ids);
      }

      collectId(ids, stack, DataComponents.BLOCK_ENTITY_DATA, "block_entity_data");
      collectId(ids, stack, DataComponents.ENTITY_DATA, "entity_data");
      collectId(ids, stack, DataComponents.BUCKET_ENTITY_DATA, "bucket_entity_data");

      for (Entry<DataComponentType<?>, Optional<?>> entry : Compat263.dataComponentPatchEntrySet(stack.getComponentsPatch())) {
         String id = componentTypeName(entry.getKey());
         if (isIdKey(id) && entry.getValue().isPresent()) {
            ids.put(id, describe(entry.getValue().get()));
         }
      }

      if (!ids.isEmpty()) {
         builder.blank();
         builder.section("Identity / IDs", UiColorPalette.mint());
         ids.forEach((key, value) -> builder.snbtLine(key + ": " + value));
      }
   }

   private static <T> void collectId(Map<String, String> ids, ItemStack stack, DataComponentType<T> type, String label) {
      T value = stack.get(type);
      if (value != null) {
         String text = describe(value);
         if (text.contains("id") || text.contains("uuid") || text.contains("owner") || text.contains("profile")) {
            ids.put(label, text);
         }
      }
   }

   private static void collectIds(String path, Tag tag, Map<String, String> ids) {
      if (tag instanceof CompoundTag compound) {
         for (Entry<String, Tag> entry : compound.entrySet()) {
            String key = entry.getKey();
            String childPath = path.isBlank() ? key : path + "." + key;
            if (isIdKey(key)) {
               ids.put(childPath, ItemSnbtCodec.tagText(entry.getValue()));
            }

            collectIds(childPath, entry.getValue(), ids);
         }
      }
   }

   private static boolean isIdKey(String key) {
      if (key == null) {
         return false;
      }

      String normalized = key.replace("-", "_").replace(".", "_").replace("/", "_").toLowerCase(Locale.ROOT);
      return normalized.equals("id")
         || normalized.endsWith("_id")
         || normalized.contains("original_id")
         || normalized.contains("unique_id")
         || normalized.contains("uuid")
         || normalized.contains("owner")
         || normalized.contains("creator")
         || normalized.contains("profile");
   }

   private static String describe(Object value) {
      if (value == null) {
         return "";
      } else if (value instanceof Component component) {
         return component.getString();
      } else {
         return value instanceof CustomData customData ? ItemSnbtCodec.tagText(customData.copyTag()) : String.valueOf(value);
      }
   }

   private static String holderName(Holder<?> holder) {
      return holder == null ? "<unknown>" : holder.unwrapKey().map(ResourceKey::identifier).map(Object::toString).orElse(String.valueOf(holder));
   }

   private static String componentTypeName(DataComponentType<?> type) {
      if (type == null) {
         return "<unknown>";
      }

      Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
      if (id == null) {
         return String.valueOf(type);
      }

      String text = id.toString();
      return text.startsWith("minecraft:") ? text.substring("minecraft:".length()) : text;
   }

   private static boolean isKnownComponent(String id) {
      boolean var10000;
      switch (id) {
         case "custom_name":
         case "item_name":
         case "lore":
         case "enchantments":
         case "stored_enchantments":
         case "attribute_modifiers":
         case "max_damage":
         case "damage":
         case "unbreakable":
         case "max_stack_size":
         case "use_effects":
         case "minimum_attack_charge":
         case "damage_type":
         case "item_model":
         case "custom_model_data":
         case "tooltip_display":
         case "repair_cost":
         case "creative_slot_lock":
         case "enchantment_glint_override":
         case "intangible_projectile":
         case "food":
         case "consumable":
         case "use_remainder":
         case "use_cooldown":
         case "damage_resistant":
         case "tool":
         case "weapon":
         case "attack_range":
         case "enchantable":
         case "equippable":
         case "repairable":
         case "glider":
         case "tooltip_style":
         case "death_protection":
         case "blocks_attacks":
         case "piercing_weapon":
         case "kinetic_weapon":
         case "swing_animation":
         case "rarity":
         case "dye":
         case "dyed_color":
         case "map_color":
         case "container":
         case "bundle_contents":
         case "charged_projectiles":
         case "potion_contents":
         case "potion_duration_scale":
         case "suspicious_stew_effects":
         case "trim":
         case "debug_stick_state":
         case "instrument":
         case "provides_trim_material":
         case "ominous_bottle_amplifier":
         case "jukebox_playable":
         case "provides_banner_patterns":
         case "recipes":
         case "lodestone_tracker":
         case "firework_explosion":
         case "fireworks":
         case "profile":
         case "note_block_sound":
         case "base_color":
         case "pot_decorations":
         case "block_state":
         case "bees":
         case "lock":
         case "container_loot":
         case "block_entity_data":
         case "bucket_entity_data":
         case "entity_data":
         case "map_id":
         case "map_decorations":
         case "map_post_processing":
         case "banner_patterns":
         case "written_book_content":
         case "writable_book_content":
         case "custom_data":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   public static String prettyPrint(String text) {
      return String.join("\n", prettyLines(text));
   }

   public static List<String> prettyLines(String text) {
      if (text != null && !text.isBlank()) {
         List<String> out = new ArrayList<>();
         StringBuilder current = new StringBuilder();
         int indent = 0;
         char quote = 0;
         boolean escape = false;

         for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (quote != 0) {
               current.append(ch);
               if (escape) {
                  escape = false;
               } else if (ch == '\\') {
                  escape = true;
               } else if (ch == quote) {
                  quote = 0;
               }
            } else if (ch != '"' && ch != '\'') {
               switch (ch) {
                  case ',':
                     current.append(ch);
                     flushLine(out, current, indent);
                     break;
                  case '[':
                  case '{':
                     current.append(ch);
                     flushLine(out, current, indent);
                     indent++;
                     passIndex(text, i);
                     break;
                  case ']':
                  case '}':
                     flushLine(out, current, indent);
                     indent = Math.max(0, indent - 1);
                     current.append(ch);
                     if (i + 1 < text.length() && text.charAt(i + 1) == ',') {
                        current.append(',');
                        i++;
                     }

                     flushLine(out, current, indent);
                     break;
                  default:
                     if (!Character.isWhitespace(ch)) {
                        current.append(ch);
                     }
               }
            } else {
               quote = ch;
               current.append(ch);
            }
         }

         flushLine(out, current, indent);
         return out.isEmpty() ? List.of(text) : out;
      } else {
         return List.of("{}");
      }
   }

   private static int passIndex(String text, int index) {
      return index;
   }

   private static void flushLine(List<String> out, StringBuilder current, int indent) {
      if (!current.isEmpty()) {
         String line = current.toString().trim();
         current.setLength(0);
         if (!line.isEmpty()) {
            out.add("  ".repeat(Math.max(0, indent)) + line);
         }
      }
   }

   public static List<ItemNbtInspector.HighlightToken> highlight(String text, int fallbackColor) {
      if (text != null && !text.isEmpty()) {
         List<ItemNbtInspector.HighlightToken> tokens = new ArrayList<>();
         int i = 0;

         while (i < text.length()) {
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch)) {
               int start = i++;

               while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
                  i++;
               }

               tokens.add(new ItemNbtInspector.HighlightToken(text.substring(start, i), fallbackColor));
            } else if (ch != '"' && ch != '\'') {
               if ("{}[](),:=".indexOf(ch) >= 0) {
                  tokens.add(new ItemNbtInspector.HighlightToken(String.valueOf(ch), UiColorPalette.silver()));
                  i++;
               } else if (ch == '!') {
                  tokens.add(new ItemNbtInspector.HighlightToken("!", UiColorPalette.errorText()));
                  i++;
               } else {
                  int start = i++;

                  while (i < text.length() && !Character.isWhitespace(text.charAt(i)) && "{}[](),:=\"'".indexOf(text.charAt(i)) < 0) {
                     i++;
                  }

                  String word = text.substring(start, i);
                  int color = wordColor(text, i, word, fallbackColor);
                  tokens.add(new ItemNbtInspector.HighlightToken(word, color));
               }
            } else {
               int start = i++;
               boolean escape = false;

               while (true) {
                  if (i < text.length()) {
                     char c = text.charAt(i++);
                     if (escape) {
                        escape = false;
                        continue;
                     }

                     if (c == '\\') {
                        escape = true;
                        continue;
                     }

                     if (c != ch) {
                        continue;
                     }
                  }

                  tokens.add(new ItemNbtInspector.HighlightToken(text.substring(start, i), UiColorPalette.mint()));
                  break;
               }
            }
         }

         return List.copyOf(tokens);
      } else {
         return List.of(new ItemNbtInspector.HighlightToken("", fallbackColor));
      }
   }

   private static int wordColor(String text, int tokenEnd, String word, int fallbackColor) {
      String lower = word.toLowerCase(Locale.ROOT);
      int next = tokenEnd;

      while (next < text.length() && Character.isWhitespace(text.charAt(next))) {
         next++;
      }

      if (next >= text.length() || text.charAt(next) != ':' && text.charAt(next) != '=') {
         if (lower.equals("true") || lower.equals("false")) {
            return UiColorPalette.magenta();
         } else if (lower.equals("null")) {
            return UiColorPalette.textMuted();
         } else if (word.matches("[-+]?\\d+(\\.\\d+)?[bBsSlLfFdD]?")) {
            return UiColorPalette.warningYellow();
         } else {
            return word.contains(":") ? UiColorPalette.blue() : fallbackColor;
         }
      } else {
         return UiColorPalette.cyan();
      }
   }

   public record InspectLine(String text, int color, List<ItemNbtInspector.HighlightToken> tokens) {
      public InspectLine(String text, int color) {
         this(text, color, List.of());
      }
   }

   private static final class InspectLineBuilder {
      private final String title;
      private final List<ItemNbtInspector.InspectLine> lines = new ArrayList<>();

      private InspectLineBuilder(String title) {
         this.title = title != null && !title.isBlank() ? title : "Item NBT";
      }

      private void section(String text, int color) {
         this.line("[" + text + "]", color);
      }

      private void line(String text, int color) {
         this.lines.add(new ItemNbtInspector.InspectLine(text == null ? "" : text, color));
      }

      private void snbtLine(String text) {
         String safe = text == null ? "" : text;
         this.lines.add(new ItemNbtInspector.InspectLine(safe, UiColorPalette.softWhite(), ItemNbtInspector.highlight(safe, UiColorPalette.softWhite())));
      }

      private void blank() {
         this.lines.add(new ItemNbtInspector.InspectLine("", UiColorPalette.textMuted()));
      }

      private List<ItemNbtInspector.InspectLine> build() {
         return List.copyOf(this.lines);
      }

      private String toText() {
         StringBuilder out = new StringBuilder(this.title);

         for (ItemNbtInspector.InspectLine line : this.lines) {
            out.append('\n').append(line.text());
         }

         return out.toString();
      }
   }

   public record ItemInspection(
      String init_Str,
      ItemStack stack,
      List<ItemNbtInspector.InspectLine> lines,
      List<ItemNbtInspector.InspectLine> rawLines,
      String load_Str,
      String read,
      String write
   ) implements ItemNbtInspector.NbtInspection {
      @Override
      public String init_Str() {
         return "Item NBT - " + this.init_Str;
      }

      @Override
      public String load_Str() {
         return "item";
      }

      @Override
      public String read() {
         return this.init_Str;
      }

      @Override
      public String write() {
         return this.load_Str;
      }

      @Override
      public String update() {
         return this.read;
      }

      public String sync() {
         return this.write;
      }
   }

   private record ComponentSummary<T>(DataComponentType<T> type, String label) {
   }

   public interface NbtInspection {
      String read();

      String init_Str();

      String load_Str();

      ItemStack stack();

      List<ItemNbtInspector.InspectLine> lines();

      List<ItemNbtInspector.InspectLine> rawLines();

      String write();

      String update();
   }

   public record HighlightToken(String text, int color) {
   }
}
