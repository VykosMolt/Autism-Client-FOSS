package autismclient.d;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class DataUtil implements StreamUtil {
   private static final int init_I = 30;
   private static final ChatFormatting init_ChatFormatting = ChatFormatting.GREEN;
   private static final ChatFormatting load = ChatFormatting.LIGHT_PURPLE;
   private static final ChatFormatting read = ChatFormatting.GRAY;
   private static final ChatFormatting write = ChatFormatting.GOLD;

   @Override
   public void init(List<Component> tooltip, @Nullable Tag tag, boolean splitLines) {
      if (tag == null) {
         tooltip.add(Component.literal("No NBT tag").withStyle(ChatFormatting.DARK_GRAY));
      } else {
         this.init(tooltip, tag, "", "", "  ", splitLines);
      }
   }

   private void init(List<Component> tooltip, Tag base, String pad, String tagName, String padIncrement, boolean splitLongStrings) {
      if (base instanceof CompoundTag compound) {
         this.init(tooltip, compound, pad, padIncrement, splitLongStrings);
      } else if (base instanceof ListTag list) {
         this.init(tooltip, list, pad, padIncrement, splitLongStrings);
      } else {
         this.init(tooltip, base, Component.literal(tagName).withStyle(write), pad, splitLongStrings);
      }
   }

   private void init(List<Component> tooltip, CompoundTag tag, String pad, String padIncrement, boolean splitLongStrings) {
      for (String key : tag.keySet()) {
         Tag value = tag.get(key);
         if (value != null) {
            boolean nested = value instanceof ListTag || value instanceof CompoundTag;
            if (nested) {
               Component subtreeName = Component.literal(key).withStyle(write);
               Component intro = Component.literal(pad).append(subtreeName).append(Component.literal(": {").withStyle(read));
               tooltip.add(intro);
               this.init(tooltip, value, pad + padIncrement, key, padIncrement, splitLongStrings);
               tooltip.add(Component.literal(pad + "}").withStyle(read));
            } else {
               this.init(tooltip, value, Component.literal(key).withStyle(write), pad, splitLongStrings);
            }
         }
      }
   }

   private void init(List<Component> tooltip, ListTag list, String pad, String padIncrement, boolean splitLongStrings) {
      for (int index = 0; index < list.size(); index++) {
         Tag element = list.get(index);
         Component bracketedIndex = Component.literal("[")
            .withStyle(read)
            .append(Component.literal(String.valueOf(index)).withStyle(init_ChatFormatting))
            .append(Component.literal("]").withStyle(read));
         if (!(element instanceof ListTag) && !(element instanceof CompoundTag)) {
            this.init(tooltip, element, bracketedIndex, pad, splitLongStrings);
         } else {
            tooltip.add(Component.literal(pad + " ").append(bracketedIndex).append(Component.literal(": {").withStyle(read)));
            this.init(tooltip, element, pad + padIncrement, "", padIncrement, splitLongStrings);
            tooltip.add(Component.literal(pad + "}").withStyle(read));
         }
      }
   }

   private void init(List<Component> tooltip, Tag nbt, Component name, String pad, boolean splitLongStrings) {
      String content = nbt.toString();
      if (splitLongStrings && content.length() >= 30) {
         tooltip.add(Component.literal(pad).append(name).append(Component.literal(":")));
         int index = 0;

         while (index < content.length()) {
            int nextLength = Math.min(30, content.length() - index);
            String chunk = content.substring(index, index + nextLength);
            tooltip.add(Component.literal(pad + "   |").withStyle(ChatFormatting.AQUA).append(Component.literal(" " + chunk).withStyle(load)));
            index += nextLength;
         }
      } else {
         tooltip.add(Component.literal(pad).append(name).append(Component.literal(": ")).append(Component.literal(content).withStyle(load)));
      }
   }
}
