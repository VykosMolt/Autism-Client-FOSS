package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;

public final class ItemSnbtCodec {
   private static final Minecraft CLIENT = Minecraft.getInstance();

   private ItemSnbtCodec() {
   }

   public static String init(ItemStack stack) {
      if (stack != null && !stack.isEmpty()) {
         String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
         String components = write(stack);
         return "/give @p " + id + components + " " + Math.max(1, Math.min(99, stack.getCount()));
      } else {
         return "";
      }
   }

   public static String load(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && CLIENT.player != null) {
         try {
            Tag tag = ItemStack.CODEC.encodeStart(CLIENT.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
            return init(tag);
         } catch (Throwable t) {
            return "{error:\"" + escape(t.getClass().getSimpleName()) + "\"}";
         }
      } else {
         return "{}";
      }
   }

   public static ItemStack parseStack(String snbt) {
      if (snbt != null && !snbt.isBlank() && CLIENT.player != null) {
         try {
            CompoundTag tag = TagParser.parseCompoundFully(snbt.trim());
            return ItemStack.CODEC.parse(CLIENT.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
         } catch (Throwable ignored) {
            return ItemStack.EMPTY;
         }
      } else {
         return ItemStack.EMPTY;
      }
   }

   public static String read(ItemStack stack) {
      return stack != null && !stack.isEmpty() ? ItemStack.validateStrict(stack).error().map(Object::toString).orElse("") : "item stack is empty";
   }

   public static String write(ItemStack stack) {
      if (stack != null && !stack.isEmpty() && CLIENT.player != null) {
         try {
            Tag encoded = DataComponentPatch.CODEC
               .encodeStart(CLIENT.player.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch())
               .getOrThrow();
            if (encoded instanceof CompoundTag compound && !compound.isEmpty()) {
               List<String> parts = new ArrayList<>();

               for (Entry<String, Tag> entry : compound.entrySet()) {
                  String key = normalizeComponentKey(entry.getKey());
                  Tag value = entry.getValue();
                  if (!key.isBlank() && value != null) {
                     if (key.charAt(0) == '!') {
                        parts.add(key);
                     } else {
                        parts.add(key + "=" + init(value));
                     }
                  }
               }

               return parts.isEmpty() ? "" : "[" + String.join(",", parts) + "]";
            } else {
               return "";
            }
         } catch (Throwable t) {
            return "";
         }
      } else {
         return "";
      }
   }

   public static String init(Tag tag) {
      return tag == null ? "" : tag.toString();
   }

   public static String escape(String text) {
      return text == null ? "" : text.replace("\\", "\\\\").replace("\"", "\\\"");
   }

   private static String normalizeComponentKey(String raw) {
      if (raw != null && !raw.isBlank()) {
         String key = raw.trim();
         boolean removed = key.startsWith("!");
         if (removed) {
            key = key.substring(1);
         }

         if (key.startsWith("minecraft:")) {
            key = key.substring("minecraft:".length());
         }

         return removed ? "!" + key : key;
      } else {
         return "";
      }
   }
}
