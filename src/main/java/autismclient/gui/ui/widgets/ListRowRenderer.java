package autismclient.gui.ui.widgets;

import autismclient.util.ThemePaletteManager;
import autismclient.util.GuiIconAtlas;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import autismclient.gui.f.b.CharEncoder;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public final class ListRowRenderer {
   private static final CharEncoder init_CharEncoder = new CharEncoder();
   private static final int init_I = 2;

   private ListRowRenderer() {
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean focused) {
      int bg = focused ? init_CharEncoder.start() : init_CharEncoder.reset();
      int border = focused ? init_CharEncoder.run() : init_CharEncoder.set();
      autismclient.gui.f.NetHelper bounds = autismclient.gui.f.NetHelper.init(x, y, width, height);
      autismclient.gui.f.PacketUtil.load(context, bounds, 5, bg, border);
   }

   public static void init(GuiGraphicsExtractor context, Font textRenderer, String title, int x, int y) {
      k.init(context, textRenderer, title, init_CharEncoder.init_Identifier_l(l.load), init_CharEncoder.load(l.write), x, y, false);
   }

   public static void init(
      GuiGraphicsExtractor context, Font textRenderer, String label, boolean enabled, int listX, int listWidth, int headerY, int mouseX, int mouseY
   ) {
      int buttonHeight = 13;
      int buttonWidth = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(
         k.init(textRenderer, label, init_CharEncoder.init_Identifier_l(l.read), init_CharEncoder.load(l.read))
      );
      int buttonX = listX + listWidth - buttonWidth - 2;
      int buttonY = headerY + Math.max(0, (init_CharEncoder.init_I_l_I(l.load, 1) - buttonHeight) / 2) - 1;
      OverlayActionButton button = OverlayActionButton.init(buttonX, buttonY, buttonWidth, buttonHeight, Component.literal(label), b -> {});
      button.init_Z = enabled;
      button.init_ByteReader_ByteReader_StreamU(enabled ? OverlayActionButton.ButtonTone.SECONDARY : OverlayActionButton.ButtonTone.GHOST);
      OverlayActionButton.init(context, textRenderer, button, mouseX, mouseY);
   }

   public static void init(GuiGraphicsExtractor context, Font textRenderer, String text, int x, int y, int width) {
      String trimmed = k.init(textRenderer, text, Math.max(1, width - 10), init_CharEncoder.init_Identifier_l(l.write), init_CharEncoder.load(l.write));
      int textY = UiLayoutMath.centerTextY(y, 14, init_CharEncoder.init_I_l(l.write), init_CharEncoder.flush());
      k.init(context, textRenderer, trimmed, init_CharEncoder.init_Identifier_l(l.write), init_CharEncoder.load(l.write), x + 6, textY, false);
   }

   public static void init(
      GuiGraphicsExtractor context,
      Font textRenderer,
      String label,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      boolean selected,
      ListRowRenderer.RowTone tone
   ) {
      init(context, textRenderer, Component.literal(label == null ? "" : label), x, y, width, height, hovered, selected, tone, false);
   }

   public static void init(
      GuiGraphicsExtractor context,
      Font textRenderer,
      Component label,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      boolean selected,
      ListRowRenderer.RowTone tone,
      boolean useMinecraftText
   ) {
      if (selected) {
         init_CharEncoder.prev();
      } else if (hovered) {
         init_CharEncoder.next();
      } else {
         init_CharEncoder.stop();
      }

      int var10000;
      switch (tone) {
         case NORMAL:
            var10000 = selected ? ThemePaletteManager.recolor(-2828, ThemePaletteManager.ColorRole.TEXT) : init_CharEncoder.load(l.read);
            break;
         case READY:
            var10000 = selected ? -1507344 : -6426438;
            break;
         case WARNING:
            var10000 = selected ? -3104 : -14726;
            break;
         case MISSING:
            var10000 = ThemePaletteManager.recolor(selected ? -4884 : -20561, ThemePaletteManager.ColorRole.DANGER);
            break;
         case DANGER:
            var10000 = ThemePaletteManager.recolor(selected ? -5398 : -29556, ThemePaletteManager.ColorRole.DANGER);
            break;
         default:
            throw new MatchException(null, null);
      }

      int fg = var10000;
      init(context, textRenderer, label, x, y, width, height, hovered, selected, fg, useMinecraftText);
   }

   public static void init(
      GuiGraphicsExtractor context,
      Font textRenderer,
      Component label,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      boolean selected,
      int textColor,
      boolean useMinecraftText
   ) {
      init(context, textRenderer, label, ItemStack.EMPTY, x, y, width, height, hovered, selected, textColor, useMinecraftText);
   }

   public static void init(
      GuiGraphicsExtractor context,
      Font textRenderer,
      Component label,
      ItemStack icon,
      int x,
      int y,
      int width,
      int height,
      boolean hovered,
      boolean selected,
      int textColor,
      boolean useMinecraftText
   ) {
      int bg = selected ? init_CharEncoder.prev() : (hovered ? init_CharEncoder.next() : init_CharEncoder.stop());
      int rightInset = 6;
      boolean hasIcon = icon != null && !icon.isEmpty();
      int textX = x + 6 + (hasIcon ? 15 : 0);
      autismclient.gui.f.PacketUtil.init(context, autismclient.gui.f.NetHelper.init(x, y, width, height), bg);
      if (hasIcon) {
         init(context, icon, x + 3, y + Math.max(0, (height - 12) / 2));
      }

      int textWidth = Math.max(1, x + width - rightInset - textX);
      int textY = UiLayoutMath.centerTextY(y, height, init_CharEncoder.init_I_l(l.read), init_CharEncoder.flush());
      if (useMinecraftText) {
         List<FormattedCharSequence> wrapped = textRenderer.split(label == null ? Component.empty() : label, textWidth);
         FormattedCharSequence line = wrapped.isEmpty() ? Component.empty().getVisualOrderText() : wrapped.get(0);
         context.text(textRenderer, line, textX, textY, textColor, false);
      } else {
         String trimmed = k.init(textRenderer, label == null ? "" : label.getString(), textWidth, init_CharEncoder.init_Identifier_l(l.read), textColor);
         k.init(context, textRenderer, trimmed, init_CharEncoder.init_Identifier_l(l.read), textColor, textX, textY, false);
      }
   }

   private static void init(GuiGraphicsExtractor context, ItemStack stack, int x, int y) {
      context.pose().pushMatrix();
      context.pose().scale(0.75F, 0.75F);
      context.item(stack, Math.round(x / 0.75F), Math.round(y / 0.75F));
      context.pose().popMatrix();
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int width) {
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(context, x + 5, y, Math.max(0, width - 10), 536870911);
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int size, boolean hovered) {
      load(context, x, y, size, size, hovered);
   }

   public static void load(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean hovered) {
      IconButtonRenderer.init(context, IconButtonRenderer.init(), autismclient.gui.f.NetHelper.init(x, y, width, height), GuiIconAtlas.open, hovered, true, true);
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int size, String symbol, boolean hovered, boolean danger) {
      init(context, x, y, size, size, symbol, hovered, danger);
   }

   public static void init(GuiGraphicsExtractor context, int x, int y, int width, int height, String symbol, boolean hovered, boolean danger) {
      IconButtonRenderer.init(context, IconButtonRenderer.init(), autismclient.gui.f.NetHelper.init(x, y, width, height), symbol, hovered, true, danger);
   }

   public enum RowTone {
      NORMAL,
      READY,
      WARNING,
      MISSING,
      DANGER;
   }
}
