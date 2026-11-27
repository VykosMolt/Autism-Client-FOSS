package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class BlockNbtFormatter {
   private static final int init = 40000;

   private BlockNbtFormatter() {
   }

   public static BlockNbtFormatter.Inner_DataUtil init(CompoundTag source, List<ItemStack> containerItems, String note) {
      CompoundTag root = source == null ? new CompoundTag() : source.copy();
      List<ItemStack> items = copyStacks(containerItems);
      CompoundTag state = root.getCompound("state").orElse(new CompoundTag());
      String blockId = state.getStringOr("Name", "minecraft:air");
      String raw = root.toString();
      List<ItemNbtInspector.Inner_BufferHelper> nice = new ArrayList<>();
      init(nice, "Identity", UiColorPalette.warmWhite());
      load(nice, "Block: " + blockId, UiColorPalette.softWhite());
      load(nice, "State: " + root.getStringOr("block_state", blockId), UiColorPalette.textBody());
      load(nice, "Dimension: " + root.getStringOr("dimension", "<unknown>"), UiColorPalette.textBody());
      int[] pos = root.getIntArray("position").orElse(new int[0]);
      load(nice, "Position: " + init(pos), UiColorPalette.textBody());
      load(nice, "Source: " + root.getStringOr("source", "client"), UiColorPalette.textMuted());
      CompoundTag entity = root.getCompound("block_entity").orElse(null);
      if (entity != null) {
         init_V(nice);
         init(nice, "Block Entity", UiColorPalette.cyan());
         load(nice, "Type: " + entity.getStringOr("id", "<unknown>"), UiColorPalette.softWhite());
         load(nice, "Fields: " + entity.size(), UiColorPalette.textBody());
      }

      if (!items.isEmpty() || root.getBooleanOr("contents_available", false)) {
         init_V(nice);
         init(nice, "Contents", UiColorPalette.mint());
         load(nice, "Slots: " + root.getIntOr("container_slots", items.size()), UiColorPalette.textBody());
         int nonEmpty = 0;

         for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack != null && !stack.isEmpty()) {
               nonEmpty++;
               String id = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
               load(nice, "Slot " + slot + ": " + stack.getCount() + "x " + id + " (" + stack.getHoverName().getString() + ")", UiColorPalette.softWhite());
            }
         }

         if (nonEmpty == 0) {
            load(nice, "Container is empty.", UiColorPalette.textMuted());
         }
      }

      if (note != null && !note.isBlank()) {
         init_V(nice);
         init(nice, "Access", UiColorPalette.amber());
         load(nice, note, UiColorPalette.textBody());
      }

      List<ItemNbtInspector.Inner_BufferHelper> rawLines = new ArrayList<>();
      init(rawLines, "Raw Block SNBT", UiColorPalette.warmWhite());
      boolean truncated = raw.length() > 40000;
      String shown = truncated ? raw.substring(0, 40000) : raw;

      for (String rawLine : ItemNbtInspector.prettyLines(shown)) {
         rawLines.add(new ItemNbtInspector.Inner_BufferHelper(rawLine, UiColorPalette.softWhite(), ItemNbtInspector.highlight(rawLine, UiColorPalette.softWhite())));
      }

      if (truncated) {
         load(rawLines, "... (display truncated; Copy Raw keeps everything)", UiColorPalette.textMuted());
      }

      StringBuilder pretty = new StringBuilder("Block NBT - ").append(blockId);

      for (ItemNbtInspector.Inner_BufferHelper inspectionLine : nice) {
         pretty.append('\n').append(inspectionLine.text());
      }

      return new BlockNbtFormatter.Inner_DataUtil(blockId, List.copyOf(nice), List.copyOf(rawLines), pretty.toString(), raw);
   }

   private static List<ItemStack> copyStacks(List<ItemStack> source) {
      if (source != null && !source.isEmpty()) {
         List<ItemStack> copy = new ArrayList<>(source.size());

         for (ItemStack stack : source) {
            copy.add(stack == null ? ItemStack.EMPTY : stack.copy());
         }

         return List.copyOf(copy);
      } else {
         return List.of();
      }
   }

   private static String init(int[] pos) {
      return pos.length >= 3 ? pos[0] + ", " + pos[1] + ", " + pos[2] : "<unknown>";
   }

   private static void init(List<ItemNbtInspector.Inner_BufferHelper> lines, String title, int color) {
      load(lines, "[" + title + "]", color);
   }

   private static void load(List<ItemNbtInspector.Inner_BufferHelper> lines, String text, int color) {
      lines.add(new ItemNbtInspector.Inner_BufferHelper(text == null ? "" : text, color));
   }

   private static void init_V(List<ItemNbtInspector.Inner_BufferHelper> lines) {
      load(lines, "", UiColorPalette.textMuted());
   }

   public record Inner_DataUtil(String init_Str, List<ItemNbtInspector.Inner_BufferHelper> lines, List<ItemNbtInspector.Inner_BufferHelper> rawLines, String load_Str, String read)
      implements ItemNbtInspector.Inner_StreamUtil {
      @Override
      public String init_Str() {
         return "Block NBT - " + this.init_Str;
      }

      @Override
      public String load_Str() {
         return "block";
      }

      @Override
      public ItemStack stack() {
         return ItemStack.EMPTY;
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
   }
}
