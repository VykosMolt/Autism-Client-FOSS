package autismclient.gui.ui.widgets;

import autismclient.util.ThemePaletteManager;
import java.util.function.ToIntFunction;

public final class TextFieldRenderer {
   private TextFieldRenderer() {
   }

   public static void init(
      autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, String text, String placeholder, boolean focused, int cursor
   ) {
      init(context, bounds, text, placeholder, focused, cursor, -1, -1);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String text,
      String placeholder,
      boolean focused,
      int cursor,
      int selectionStart,
      int selectionEnd
   ) {
      String value = text == null ? "" : text;
      int scroll = focused ? init(value, cursor, bounds.update() - 8, fragment -> context.init_PoolEntry().init(value, fragment)) : 0;
      init(context, bounds, value, placeholder, focused, cursor, selectionStart, selectionEnd, scroll);
   }

   public static void init(
      autismclient.gui.f.SyncManager context,
      autismclient.gui.f.NetHelper bounds,
      String text,
      String placeholder,
      boolean focused,
      int cursor,
      int selectionStart,
      int selectionEnd,
      int scrollOffset
   ) {
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().init_JsonUtil();
      autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), bounds, 3, focused ? colors.tick : colors.pop, focused ? colors.clear : colors.get);
      String value = text == null ? "" : text;
      ToIntFunction<String> widths = fragment -> context.init_PoolEntry().init(value, fragment);
      String shown = value.isEmpty() ? placeholder : value;
      int scroll = focused ? Math.max(0, scrollOffset) : 0;
      int textX = bounds.read() + 4 - scroll;
      int textY = context.init_PoolEntry().init_I(bounds);
      context.init_GuiGraphicsExtract().enableScissor(bounds.read() + 3, bounds.write() + 1, bounds.init() - 3, bounds.load() - 1);

      try {
         if (focused && selectionStart >= 0 && selectionEnd > selectionStart) {
            int start = Utf16TextCursor.init_I(value, selectionStart);
            int end = Utf16TextCursor.load(value, selectionEnd);
            int selectX = textX + widths.applyAsInt(value.substring(0, start));
            int selectW = Math.max(1, widths.applyAsInt(value.substring(0, end)) - widths.applyAsInt(value.substring(0, start)));
            autismclient.gui.f.PacketUtil.init(
               context.init_GuiGraphicsExtract(),
               autismclient.gui.f.NetHelper.init(selectX, bounds.write() + 3, selectW, Math.max(1, bounds.sync() - 6)),
               ThemePaletteManager.init_I_I_ct_DataUtil(1728006730, ThemePaletteManager.ColorRole.ACCENT)
            );
         }

         context.init_PoolEntry()
            .init_V_GuiGraphicsExtract_2(context.init_GuiGraphicsExtract(), shown, textX, textY, value.isEmpty() ? colors.valid : colors.prev);
         if (focused) {
            String before = value.substring(0, Utf16TextCursor.init_I(value, cursor));
            int caret = Math.min(bounds.init() - 3, textX + widths.applyAsInt(before));
            autismclient.gui.f.PacketUtil.init(
               context.init_GuiGraphicsExtract(), autismclient.gui.f.NetHelper.init(caret, bounds.write() + 3, 1, Math.max(1, bounds.sync() - 6)), colors.prev
            );
         }
      } finally {
         context.init_GuiGraphicsExtract().disableScissor();
      }
   }

   public static int init(String text, int cursor, int availableWidth, ToIntFunction<String> widths) {
      return init(text, cursor, availableWidth, widths, 0);
   }

   public static int init(String text, int cursor, int availableWidth, ToIntFunction<String> widths, int previousScroll) {
      String value = text == null ? "" : text;
      int boundary = Utf16TextCursor.init_I(value, cursor);
      int width = Math.max(1, availableWidth);
      int caret = widths.applyAsInt(value.substring(0, boundary));
      int maxScroll = Math.max(0, widths.applyAsInt(value) - width);
      int scroll = Math.max(0, Math.min(previousScroll, maxScroll));
      if (caret < scroll) {
         scroll = caret;
      } else if (caret > scroll + width) {
         scroll = caret - width;
      }

      return Math.max(0, Math.min(scroll, maxScroll));
   }
}
