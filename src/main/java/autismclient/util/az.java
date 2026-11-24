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

public final class az {
   private static final int init = 40000;

   private az() {
   }

   public static az.Inner_ConfigStore init_az_ConfigStore_ItemStack(ItemStack source) {
      ItemStack stack = source == null ? ItemStack.EMPTY : source.copy();
      String title = stack.isEmpty() ? "Item NBT" : stack.getHoverName().getString();
      az.Inner_CachePool nice = new az.Inner_CachePool(title);
      az.Inner_CachePool raw = new az.Inner_CachePool(title + " Raw");
      String rawSnbt = ay.load(stack);
      String giveCommand = ay.init(stack);
      init_V_ItemStack_az_CachePool(stack, nice);
      init_V_Str_az_CachePool(rawSnbt, raw);
      return new az.Inner_ConfigStore(title, stack, nice.init_List(), raw.init_List(), nice.init_Str(), rawSnbt, giveCommand);
   }

   private static void init_V_ItemStack_az_CachePool(ItemStack stack, az.Inner_CachePool builder) {
      if (stack != null && !stack.isEmpty()) {
         builder.init("Identity", p.index());
         builder.load("Item: " + BuiltInRegistries.ITEM.getKey(stack.getItem()), p.task());
         builder.load("Count: " + stack.getCount(), p.parse());
         builder.load("Hover Name: " + stack.getHoverName().getString(), p.parse());
         builder.load("Components: " + stack.getComponentsPatch().size() + " non-default", p.a());
         init_V_az_CachePool_ItemStack(builder, stack);
         Component customName = stack.get(DataComponents.CUSTOM_NAME);
         Component itemName = stack.get(DataComponents.ITEM_NAME);
         ItemLore lore = stack.get(DataComponents.LORE);
         if (customName != null || itemName != null || lore != null && !lore.lines().isEmpty()) {
            builder.init_V();
            builder.init("Display", p.node());
            if (customName != null) {
               builder.load("Custom Name: " + customName.getString(), p.task());
            }

            if (itemName != null) {
               builder.load("Item Name: " + itemName.getString(), p.parse());
            }

            if (lore != null && !lore.lines().isEmpty()) {
               int i = 1;

               for (Component line : lore.lines()) {
                  builder.load("Lore " + i++ + ": " + line.getString(), p.parse());
               }
            }
         }

         init_V_az_CachePool(
            builder,
            stack,
            "Model / Visuals",
            p.node(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.ITEM_MODEL, "Item Model"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CUSTOM_MODEL_DATA, "Custom Model Data"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.TOOLTIP_STYLE, "Tooltip Style"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.DYED_COLOR, "Dyed Color"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.DYE, "Dye"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.RARITY, "Rarity"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, "Glint Override"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.TOOLTIP_DISPLAY, "Tooltip Display")
         );
         init_V_az_CachePool(builder, "Enchantments", stack.get(DataComponents.ENCHANTMENTS));
         init_V_az_CachePool(builder, "Stored Enchantments", stack.get(DataComponents.STORED_ENCHANTMENTS));
         ItemAttributeModifiers attributes = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
         if (attributes != null && !attributes.modifiers().isEmpty()) {
            builder.init_V();
            builder.init("Attributes", p.queue());

            for (net.minecraft.world.item.component.ItemAttributeModifiers.Entry entry : attributes.modifiers()) {
               String attribute = init_Str_Holder(entry.attribute());
               builder.load(
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
                  p.parse()
               );
            }
         }

         if (stack.has(DataComponents.MAX_DAMAGE)
            || stack.has(DataComponents.DAMAGE)
            || stack.has(DataComponents.UNBREAKABLE)
            || stack.has(DataComponents.MAX_STACK_SIZE)
            || stack.has(DataComponents.ENCHANTMENT_GLINT_OVERRIDE)
            || stack.has(DataComponents.RARITY)) {
            builder.init_V();
            builder.init("Item Rules", p.entry());
            if (stack.has(DataComponents.MAX_DAMAGE)) {
               builder.load("Max Damage: " + stack.getMaxDamage(), p.parse());
            }

            if (stack.has(DataComponents.DAMAGE)) {
               builder.load("Damage: " + stack.getDamageValue(), p.parse());
            }

            if (stack.has(DataComponents.UNBREAKABLE)) {
               builder.load("Unbreakable: true", p.parse());
            }

            if (stack.has(DataComponents.MAX_STACK_SIZE)) {
               builder.load("Max Stack: " + stack.getMaxStackSize(), p.parse());
            }
         }

         init_V_az_CachePool(
            builder,
            stack,
            "Usage / Combat",
            p.queue(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.USE_EFFECTS, "Use Effects"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.MINIMUM_ATTACK_CHARGE, "Minimum Attack Charge"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.DAMAGE_TYPE, "Damage Type"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.FOOD, "Food"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CONSUMABLE, "Consumable"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.USE_REMAINDER, "Use Remainder"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.USE_COOLDOWN, "Use Cooldown"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.DAMAGE_RESISTANT, "Damage Resistant"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.TOOL, "Tool"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.WEAPON, "Weapon"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.ATTACK_RANGE, "Attack Range"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BLOCKS_ATTACKS, "Blocks Attacks"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.PIERCING_WEAPON, "Piercing Weapon"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.KINETIC_WEAPON, "Kinetic Weapon"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.ATTACK_ANIMATION, "Swing Animation")
         );
         init_V_az_CachePool(
            builder,
            stack,
            "Equipment / Enchanting",
            p.buffer(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.ENCHANTABLE, "Enchantable"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.EQUIPPABLE, "Equippable"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.REPAIRABLE, "Repairable"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.GLIDER, "Glider"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.REPAIR_COST, "Repair Cost"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CREATIVE_SLOT_LOCK, "Creative Slot Lock"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.INTANGIBLE_PROJECTILE, "Intangible Projectile"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.DEATH_PROTECTION, "Death Protection"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.TRIM, "Armor Trim"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.PROVIDES_TRIM_MATERIAL, "Provides Trim Material"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BREAK_SOUND, "Break Sound")
         );
         init_V_az_CachePool(
            builder,
            stack,
            "Placement / Mining",
            p.entry(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CAN_PLACE_ON, "Can Place On"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CAN_BREAK, "Can Break"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BLOCK_STATE, "Block State"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.LOCK, "Lock")
         );
         ItemContainerContents container = stack.get(DataComponents.CONTAINER);
         BundleContents bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
         if (container != null || bundle != null) {
            builder.init_V();
            builder.init("Contents", p.cache());
            if (container != null) {
               long count = container.nonEmptyItemCopyStream().count();
               builder.load("Container Items: " + count, p.parse());
            }

            if (bundle != null) {
               builder.load("Bundle Items: " + bundle.size(), p.parse());
            }
         }

         init_V_az_CachePool(
            builder,
            stack,
            "Containers / Projectiles",
            p.cache(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CONTAINER, "Container"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BUNDLE_CONTENTS, "Bundle Contents"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CHARGED_PROJECTILES, "Charged Projectiles"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.CONTAINER_LOOT, "Container Loot"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BEES, "Bees"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.RECIPES, "Recipes")
         );
         if (stack.has(DataComponents.BLOCK_ENTITY_DATA)
            || stack.has(DataComponents.BUCKET_ENTITY_DATA)
            || stack.has(DataComponents.ENTITY_DATA)
            || stack.has(DataComponents.MAP_ID)
            || stack.has(DataComponents.BANNER_PATTERNS)) {
            builder.init_V();
            builder.init("Special Data", p.state());
            init_V_az_CachePool(builder, stack, DataComponents.BLOCK_ENTITY_DATA, "Block Entity Data");
            init_V_az_CachePool(builder, stack, DataComponents.BUCKET_ENTITY_DATA, "Bucket Entity Data");
            init_V_az_CachePool(builder, stack, DataComponents.ENTITY_DATA, "Entity Data");
            init_V_az_CachePool(builder, stack, DataComponents.MAP_ID, "Map ID");
            init_V_az_CachePool(builder, stack, DataComponents.BANNER_PATTERNS, "Banner Patterns");
         }

         init_V_az_CachePool(
            builder,
            stack,
            "Potions / Maps",
            p.state(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.POTION_CONTENTS, "Potion Contents"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.POTION_DURATION_SCALE, "Potion Duration Scale"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.SUSPICIOUS_STEW_EFFECTS, "Suspicious Stew Effects"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.MAP_ID, "Map Color"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.MAP_ID, "Map ID"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.MAP_DECORATIONS, "Map Decorations"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.MAP_POST_PROCESSING, "Map Post Processing")
         );
         init_V_az_CachePool(
            builder,
            stack,
            "Fireworks / Music / Profiles",
            p.index(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.FIREWORK_EXPLOSION, "Firework Explosion"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.FIREWORKS, "Fireworks"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.PROFILE, "Profile"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.INSTRUMENT, "Instrument"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, "Ominous Bottle Amplifier"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.JUKEBOX_PLAYABLE, "Jukebox Playable"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.NOTE_BLOCK_SOUND, "Note Block Sound")
         );
         init_V_az_CachePool(
            builder,
            stack,
            "Banners / Decorations",
            p.node(),
            init_az_DataUtil_DataComponentType_Str(DataComponents.PROVIDES_BANNER_PATTERNS, "Provides Banner Patterns"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BANNER_PATTERNS, "Banner Patterns"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.BASE_COLOR, "Base Color"),
            init_az_DataUtil_DataComponentType_Str(DataComponents.POT_DECORATIONS, "Pot Decorations")
         );
         WrittenBookContent written = stack.get(DataComponents.WRITTEN_BOOK_CONTENT);
         WritableBookContent writable = stack.get(DataComponents.WRITABLE_BOOK_CONTENT);
         if (written != null || writable != null) {
            builder.init_V();
            builder.init("Book", p.buffer());
            if (written != null) {
               builder.load("Title: " + written.title().raw(), p.parse());
               builder.load("Author: " + written.author(), p.parse());
               builder.load("Pages: " + written.pages().size(), p.parse());
            }

            if (writable != null) {
               builder.load("Pages: " + writable.pages().size(), p.parse());
            }
         }

         CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
         if (customData != null) {
            builder.init_V();
            builder.init("Custom Data", p.value());
            CompoundTag tag = customData.copyTag();
            if (tag.isEmpty()) {
               builder.load("{}", p.a());
            } else {
               for (Entry<String, Tag> entry : tag.entrySet()) {
                  builder.load(entry.getKey() + ": " + ay.init(entry.getValue()), p.parse());
               }
            }
         }

         boolean wroteOtherComponents = false;

         for (Entry<DataComponentType<?>, Optional<?>> entry : Compat263.dataComponentPatchEntrySet(stack.getComponentsPatch())) {
            String id = init_Str_DataComponentType(entry.getKey());
            if (!load(id)) {
               if (!wroteOtherComponents) {
                  builder.init_V();
                  builder.init("Other Components", p.pool());
                  wroteOtherComponents = true;
               }

               Optional<?> value = entry.getValue();
               if (value.isPresent()) {
                  builder.init(id + ": " + init_Str_Obj(value.get()));
               } else {
                  builder.init("!" + id);
               }
            }
         }
      } else {
         builder.init("Empty", p.valid());
         builder.load("No item stack was selected.", p.valid());
      }
   }

   private static void init_V_Str_az_CachePool(String rawSnbt, az.Inner_CachePool builder) {
      builder.init("Raw ItemStack SNBT", p.index());
      boolean truncated = rawSnbt != null && rawSnbt.length() > 40000;
      String shown = truncated ? rawSnbt.substring(0, 40000) : rawSnbt;

      for (String line : init_List_Str(shown)) {
         builder.init(line);
      }

      if (truncated) {
         builder.init("... (truncated for display; use Copy for the full NBT)");
      }
   }

   private static void init_V_az_CachePool(az.Inner_CachePool builder, String title, ItemEnchantments enchantments) {
      if (enchantments != null && !enchantments.isEmpty()) {
         builder.init_V();
         builder.init(title, p.buffer());

         for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            builder.load(init_Str_Holder(entry.getKey()) + ": " + entry.getIntValue(), p.parse());
         }
      }
   }

   private static <T> void init_V_az_CachePool(az.Inner_CachePool builder, ItemStack stack, DataComponentType<T> type, String label) {
      T value = stack.get(type);
      if (value != null) {
         builder.init(label + ": " + init_Str_Obj(value));
      }
   }

   @SafeVarargs
   private static void init_V_az_CachePool(az.Inner_CachePool builder, ItemStack stack, String title, int sectionColor, az.Inner_DataUtil<?>... summaries) {
      boolean wroteTitle = false;

      for (az.Inner_DataUtil<?> summary : summaries) {
         Object value = init_Obj_ItemStack_DataComponentType(stack, summary.init_DataComponentType());
         if (value != null) {
            if (!wroteTitle) {
               builder.init_V();
               builder.init(title, sectionColor);
               wroteTitle = true;
            }

            builder.init(summary.init_Str() + ": " + init_Str_Obj(value));
         }
      }
   }

   private static Object init_Obj_ItemStack_DataComponentType(ItemStack stack, DataComponentType<?> type) {
      return stack.get(type);
   }

   private static <T> az.Inner_DataUtil<T> init_az_DataUtil_DataComponentType_Str(DataComponentType<T> type, String label) {
      return new az.Inner_DataUtil<>(type, label);
   }

   private static void init_V_az_CachePool_ItemStack(az.Inner_CachePool builder, ItemStack stack) {
      Map<String, String> ids = new LinkedHashMap<>();
      CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
      if (customData != null) {
         init_V_Str("", customData.copyTag(), ids);
      }

      init_V_Map(ids, stack, DataComponents.BLOCK_ENTITY_DATA, "block_entity_data");
      init_V_Map(ids, stack, DataComponents.ENTITY_DATA, "entity_data");
      init_V_Map(ids, stack, DataComponents.BUCKET_ENTITY_DATA, "bucket_entity_data");

      for (Entry<DataComponentType<?>, Optional<?>> entry : Compat263.dataComponentPatchEntrySet(stack.getComponentsPatch())) {
         String id = init_Str_DataComponentType(entry.getKey());
         if (init_Z_Str(id) && entry.getValue().isPresent()) {
            ids.put(id, init_Str_Obj(entry.getValue().get()));
         }
      }

      if (!ids.isEmpty()) {
         builder.init_V();
         builder.init("Identity / IDs", p.cache());
         ids.forEach((key, value) -> builder.init(key + ": " + value));
      }
   }

   private static <T> void init_V_Map(Map<String, String> ids, ItemStack stack, DataComponentType<T> type, String label) {
      T value = stack.get(type);
      if (value != null) {
         String text = init_Str_Obj(value);
         if (text.contains("id") || text.contains("uuid") || text.contains("owner") || text.contains("profile")) {
            ids.put(label, text);
         }
      }
   }

   private static void init_V_Str(String path, Tag tag, Map<String, String> ids) {
      if (tag instanceof CompoundTag compound) {
         for (Entry<String, Tag> entry : compound.entrySet()) {
            String key = entry.getKey();
            String childPath = path.isBlank() ? key : path + "." + key;
            if (init_Z_Str(key)) {
               ids.put(childPath, ay.init(entry.getValue()));
            }

            init_V_Str(childPath, entry.getValue(), ids);
         }
      }
   }

   private static boolean init_Z_Str(String key) {
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

   private static String init_Str_Obj(Object value) {
      if (value == null) {
         return "";
      } else if (value instanceof Component component) {
         return component.getString();
      } else {
         return value instanceof CustomData customData ? ay.init(customData.copyTag()) : String.valueOf(value);
      }
   }

   private static String init_Str_Holder(Holder<?> holder) {
      return holder == null ? "<unknown>" : holder.unwrapKey().map(ResourceKey::identifier).map(Object::toString).orElse(String.valueOf(holder));
   }

   private static String init_Str_DataComponentType(DataComponentType<?> type) {
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

   private static boolean load(String id) {
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

   public static String init_Str_Str(String text) {
      return String.join("\n", init_List_Str(text));
   }

   public static List<String> init_List_Str(String text) {
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
                     init_V_List(out, current, indent);
                     break;
                  case '[':
                  case '{':
                     current.append(ch);
                     init_V_List(out, current, indent);
                     indent++;
                     init_I_Str_I(text, i);
                     break;
                  case ']':
                  case '}':
                     init_V_List(out, current, indent);
                     indent = Math.max(0, indent - 1);
                     current.append(ch);
                     if (i + 1 < text.length() && text.charAt(i + 1) == ',') {
                        current.append(',');
                        i++;
                     }

                     init_V_List(out, current, indent);
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

         init_V_List(out, current, indent);
         return out.isEmpty() ? List.of(text) : out;
      } else {
         return List.of("{}");
      }
   }

   private static int init_I_Str_I(String text, int index) {
      return index;
   }

   private static void init_V_List(List<String> out, StringBuilder current, int indent) {
      if (!current.isEmpty()) {
         String line = current.toString().trim();
         current.setLength(0);
         if (!line.isEmpty()) {
            out.add("  ".repeat(Math.max(0, indent)) + line);
         }
      }
   }

   public static List<az.Inner_TaskQueue> init_List_Str_I(String text, int fallbackColor) {
      if (text != null && !text.isEmpty()) {
         List<az.Inner_TaskQueue> tokens = new ArrayList<>();
         int i = 0;

         while (i < text.length()) {
            char ch = text.charAt(i);
            if (Character.isWhitespace(ch)) {
               int start = i++;

               while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
                  i++;
               }

               tokens.add(new az.Inner_TaskQueue(text.substring(start, i), fallbackColor));
            } else if (ch != '"' && ch != '\'') {
               if ("{}[](),:=".indexOf(ch) >= 0) {
                  tokens.add(new az.Inner_TaskQueue(String.valueOf(ch), p.pool()));
                  i++;
               } else if (ch == '!') {
                  tokens.add(new az.Inner_TaskQueue("!", p.valid()));
                  i++;
               } else {
                  int start = i++;

                  while (i < text.length() && !Character.isWhitespace(text.charAt(i)) && "{}[](),:=\"'".indexOf(text.charAt(i)) < 0) {
                     i++;
                  }

                  String word = text.substring(start, i);
                  int color = init_I_Str(text, i, word, fallbackColor);
                  tokens.add(new az.Inner_TaskQueue(word, color));
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

                  tokens.add(new az.Inner_TaskQueue(text.substring(start, i), p.cache()));
                  break;
               }
            }
         }

         return List.copyOf(tokens);
      } else {
         return List.of(new az.Inner_TaskQueue("", fallbackColor));
      }
   }

   private static int init_I_Str(String text, int tokenEnd, String word, int fallbackColor) {
      String lower = word.toLowerCase(Locale.ROOT);
      int next = tokenEnd;

      while (next < text.length() && Character.isWhitespace(text.charAt(next))) {
         next++;
      }

      if (next >= text.length() || text.charAt(next) != ':' && text.charAt(next) != '=') {
         if (lower.equals("true") || lower.equals("false")) {
            return p.state();
         } else if (lower.equals("null")) {
            return p.a();
         } else if (word.matches("[-+]?\\d+(\\.\\d+)?[bBsSlLfFdD]?")) {
            return p.entry();
         } else {
            return word.contains(":") ? p.buffer() : fallbackColor;
         }
      } else {
         return p.node();
      }
   }

   public record Inner_BufferHelper(String init_Str, int init_I, List<az.Inner_TaskQueue> init_List) {
      public Inner_BufferHelper(String text, int color) {
         this(text, color, List.of());
      }
   }

   private static final class Inner_CachePool {
      private final String init_Str;
      private final List<az.Inner_BufferHelper> init_List = new ArrayList<>();

      private Inner_CachePool(String title) {
         this.init_Str = title != null && !title.isBlank() ? title : "Item NBT";
      }

      private void init(String text, int color) {
         this.load("[" + text + "]", color);
      }

      private void load(String text, int color) {
         this.init_List.add(new az.Inner_BufferHelper(text == null ? "" : text, color));
      }

      private void init(String text) {
         String safe = text == null ? "" : text;
         this.init_List.add(new az.Inner_BufferHelper(safe, p.task(), az.init_List_Str_I(safe, p.task())));
      }

      private void init_V() {
         this.init_List.add(new az.Inner_BufferHelper("", p.a()));
      }

      private List<az.Inner_BufferHelper> init_List() {
         return List.copyOf(this.init_List);
      }

      private String init_Str() {
         StringBuilder out = new StringBuilder(this.init_Str);

         for (az.Inner_BufferHelper line : this.init_List) {
            out.append('\n').append(line.init_Str());
         }

         return out.toString();
      }
   }

   public record Inner_ConfigStore(
      String init_Str,
      ItemStack init_ItemStack,
      List<az.Inner_BufferHelper> init_List,
      List<az.Inner_BufferHelper> load_List,
      String load_Str,
      String read,
      String write
   ) implements az.Inner_StreamUtil {
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

   private record Inner_DataUtil<T>(DataComponentType<T> init_DataComponentType, String init_Str) {
   }

   public interface Inner_StreamUtil {
      String read();

      String init_Str();

      String load_Str();

      ItemStack init_ItemStack();

      List<az.Inner_BufferHelper> init_List();

      List<az.Inner_BufferHelper> load_List();

      String write();

      String update();
   }

   public record Inner_TaskQueue(String init_Str, int init_I) {
   }
}
