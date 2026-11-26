package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public final class CacheEntry {
   private static final int init = 40000;

   private CacheEntry() {
   }

   public static CacheEntry.Inner_DataUtil init(CompoundTag source, List<ItemStack> containerItems, String note) {
      CompoundTag root = source == null ? new CompoundTag() : source.copy();
      List<ItemStack> items = init_List(containerItems);
      CompoundTag state = root.getCompound("state").orElse(new CompoundTag());
      String blockId = state.getStringOr("Name", "minecraft:air");
      String raw = root.toString();
      List<az.Inner_BufferHelper> nice = new ArrayList<>();
      init(nice, "Identity", UiColorPalette.index());
      load(nice, "Block: " + blockId, UiColorPalette.task());
      load(nice, "State: " + root.getStringOr("block_state", blockId), UiColorPalette.parse());
      load(nice, "Dimension: " + root.getStringOr("dimension", "<unknown>"), UiColorPalette.parse());
      int[] pos = root.getIntArray("position").orElse(new int[0]);
      load(nice, "Position: " + init(pos), UiColorPalette.parse());
      load(nice, "Source: " + root.getStringOr("source", "client"), UiColorPalette.a());
      CompoundTag entity = root.getCompound("block_entity").orElse(null);
      if (entity != null) {
         init_V(nice);
         init(nice, "Block Entity", UiColorPalette.node());
         load(nice, "Type: " + entity.getStringOr("id", "<unknown>"), UiColorPalette.task());
         load(nice, "Fields: " + entity.size(), UiColorPalette.parse());
      }

      if (!items.isEmpty() || root.getBooleanOr("contents_available", false)) {
         init_V(nice);
         init(nice, "Contents", UiColorPalette.cache());
         load(nice, "Slots: " + root.getIntOr("container_slots", items.size()), UiColorPalette.parse());
         int nonEmpty = 0;

         for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            if (stack != null && !stack.isEmpty()) {
               nonEmpty++;
               String id = String.valueOf(BuiltInRegistries.ITEM.getKey(stack.getItem()));
               load(nice, "Slot " + slot + ": " + stack.getCount() + "x " + id + " (" + stack.getHoverName().getString() + ")", UiColorPalette.task());
            }
         }

         if (nonEmpty == 0) {
            load(nice, "Container is empty.", UiColorPalette.a());
         }
      }

      if (note != null && !note.isBlank()) {
         init_V(nice);
         init(nice, "Access", UiColorPalette.queue());
         load(nice, note, UiColorPalette.parse());
      }

      List<az.Inner_BufferHelper> rawLines = new ArrayList<>();
      init(rawLines, "Raw Block SNBT", UiColorPalette.index());
      boolean truncated = raw.length() > 40000;
      String shown = truncated ? raw.substring(0, 40000) : raw;

      for (String rawLine : az.init_List_Str(shown)) {
         rawLines.add(new az.Inner_BufferHelper(rawLine, UiColorPalette.task(), az.init_List_Str_I(rawLine, UiColorPalette.task())));
      }

      if (truncated) {
         load(rawLines, "... (display truncated; Copy Raw keeps everything)", UiColorPalette.a());
      }

      StringBuilder pretty = new StringBuilder("Block NBT - ").append(blockId);

      for (az.Inner_BufferHelper inspectionLine : nice) {
         pretty.append('\n').append(inspectionLine.init_Str());
      }

      return new CacheEntry.Inner_DataUtil(blockId, List.copyOf(nice), List.copyOf(rawLines), pretty.toString(), raw);
   }

   private static List<ItemStack> init_List(List<ItemStack> source) {
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

   private static void init(List<az.Inner_BufferHelper> lines, String title, int color) {
      load(lines, "[" + title + "]", color);
   }

   private static void load(List<az.Inner_BufferHelper> lines, String text, int color) {
      lines.add(new az.Inner_BufferHelper(text == null ? "" : text, color));
   }

   private static void init_V(List<az.Inner_BufferHelper> lines) {
      load(lines, "", UiColorPalette.a());
   }

   public record Inner_DataUtil(String init_Str, List<az.Inner_BufferHelper> init_List, List<az.Inner_BufferHelper> load_List, String load_Str, String read)
      implements az.Inner_StreamUtil {
      @Override
      public String init_Str() {
         return "Block NBT - " + this.init_Str;
      }

      @Override
      public String load_Str() {
         return "block";
      }

      @Override
      public ItemStack init_ItemStack() {
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
