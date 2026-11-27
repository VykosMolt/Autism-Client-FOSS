package autismclient.gui.macro.editor.capture;

import autismclient.gui.f.NetHelper;
import autismclient.gui.ui.widgets.ListRowRenderer;
import autismclient.util.ThemePaletteManager;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class EditorListRowHelper {
   private static final int init = 2;
   private static final int load = 34;
   private static final int read = 44;

   private EditorListRowHelper() {
   }

   public static EditorListRowHelper.Inner_DataUtil init(NetHelper bounds, boolean slotField, boolean capture) {
      int captureWidth = capture ? 52 : 0;
      int captureX = bounds.init() - captureWidth;
      int addX = (capture ? captureX - 2 : bounds.init()) - 34;
      int slotX = slotField ? addX - 2 - 44 : 0;
      int textRight = slotField ? slotX : addX;
      return new EditorListRowHelper.Inner_DataUtil(
         NetHelper.init(bounds.read(), bounds.write(), Math.max(1, textRight - bounds.read() - 2), 13),
         slotField ? NetHelper.init(slotX, bounds.write(), 44, 13) : null,
         NetHelper.init(addX, bounds.write(), 34, 13),
         capture ? NetHelper.init(captureX, bounds.write(), captureWidth, 13) : null
      );
   }

   public static <T> List<Integer> init(List<T> values, List<Integer> target, Predicate<T> predicate) {
      target.clear();
      if (values != null && predicate != null) {
         for (int index = 0; index < values.size(); index++) {
            if (predicate.test(values.get(index))) {
               target.add(index);
            }
         }

         return target;
      } else {
         return target;
      }
   }

   public static <T> List<T> load(List<T> values, List<T> target, Predicate<T> predicate) {
      target.clear();
      if (values != null && predicate != null) {
         for (T value : values) {
            if (predicate.test(value)) {
               target.add(value);
            }
         }

         return target;
      } else {
         return target;
      }
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      Component label,
      ItemStack icon,
      NetHelper bounds,
      boolean hovered,
      boolean selected,
      ListRowRenderer.RowTone tone,
      boolean minecraftText
   ) {
      int var10000;
      switch (tone == null ? ListRowRenderer.RowTone.NORMAL : tone) {
         case READY:
            var10000 = selected ? -1507344 : -6426438;
            break;
         case WARNING:
            var10000 = selected ? -3104 : -14726;
            break;
         case MISSING:
            var10000 = selected ? -4884 : -20561;
            break;
         case DANGER:
            var10000 = selected ? -5398 : -29556;
            break;
         case NORMAL:
            var10000 = selected ? ThemePaletteManager.recolor(-2828, ThemePaletteManager.ColorRole.TEXT) : -1;
            break;
         default:
            throw new MatchException(null, null);
      }

      int color = var10000;
      ListRowRenderer.drawIconRow(
         graphics,
         font,
         label,
         icon == null ? ItemStack.EMPTY : icon,
         bounds.read(),
         bounds.write(),
         bounds.update(),
         bounds.sync(),
         hovered,
         selected,
         color,
         minecraftText
      );
   }

   public static void init(
      GuiGraphicsExtractor graphics,
      Font font,
      Component label,
      NetHelper bounds,
      boolean hovered,
      boolean selected,
      ListRowRenderer.RowTone tone,
      boolean minecraftText
   ) {
      init(graphics, font, label, ItemStack.EMPTY, bounds, hovered, selected, tone, minecraftText);
   }

   public static void init(
      GuiGraphicsExtractor graphics, Font font, Component label, NetHelper bounds, boolean hovered, boolean selected, int textColor, boolean minecraftText
   ) {
      ListRowRenderer.drawColoredRow(graphics, font, label, bounds.read(), bounds.write(), bounds.update(), bounds.sync(), hovered, selected, textColor, minecraftText);
   }

   public static void init(GuiGraphicsExtractor graphics, NetHelper bounds, boolean hovered) {
      ListRowRenderer.drawDeleteButton(graphics, bounds.read(), bounds.write() + 1, bounds.update(), Math.max(1, bounds.sync() - 2), hovered);
   }

   public static void init(GuiGraphicsExtractor graphics, NetHelper bounds, String symbol, boolean hovered, boolean danger) {
      ListRowRenderer.drawSymbolButtonSquare(graphics, bounds.read(), bounds.write(), bounds.update(), symbol, hovered, danger);
   }

   public record Inner_DataUtil(NetHelper init, NetHelper load, NetHelper read, NetHelper write) {
   }
}
