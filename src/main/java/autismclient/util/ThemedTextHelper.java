package autismclient.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.FontDescription.Resource;
import net.minecraft.resources.Identifier;

public final class ThemedTextHelper {
   private static final Map<Identifier, FontDescription> init = new ConcurrentHashMap<>();

   private ThemedTextHelper() {
   }

   public static int init_I(ThemedTextHelper.TextTone tone) {
      int var10000;
      switch (tone) {
         case TITLE:
         case LABEL:
         case BODY:
            var10000 = UiColorPalette.textBright();
            break;
         case MUTED:
            var10000 = UiColorPalette.textMuted();
            break;
         case ACCENT:
            var10000 = UiColorPalette.accent();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static MutableComponent init(String value, ThemedTextHelper.TextTone tone) {
      return Component.literal(value == null ? "" : value).setStyle(init_Style(tone));
   }

   public static MutableComponent init(String value, int color) {
      Identifier fontId = init_Identifier(ThemedTextHelper.TextTone.BODY);
      return Component.literal(value == null ? "" : value).setStyle(Style.EMPTY.withFont(init(fontId)).withColor(color));
   }

   public static String init(String value) {
      return value != null && !value.isEmpty()
         ? value.replace("✓", "X")
            .replace("✔", "X")
            .replace("✕", "X")
            .replace("✖", "X")
            .replace("→", "->")
            .replace("←", "<-")
            .replace("—", "-")
            .replace("–", "-")
            .replace("·", "-")
            .replace("▼", "v")
            .replace("▾", "v")
            .replace("▲", "^")
            .replace("▴", "^")
            .replace("●", "X")
            .replace("○", "O")
            .replace("≡", "=")
            .replace("★", "*")
            .replace("∞", "INF")
            .replace("⚠", "WARN")
            .replace("⚡", "BURST")
            .trim()
         : "";
   }

   public static Style init_Style(ThemedTextHelper.TextTone tone) {
      int color = init_I(tone);
      return Style.EMPTY.withFont(init(init_Identifier(tone))).withColor(color);
   }

   public static int width(Font renderer, String value, ThemedTextHelper.TextTone tone) {
      return autismclient.gui.f.b.k.init(renderer, value, init_Identifier(tone), init_I(tone));
   }

   public static String ellipsize(Font renderer, String value, int maxWidth, ThemedTextHelper.TextTone tone) {
      return autismclient.gui.f.b.k.init(renderer, value, maxWidth, init_Identifier(tone), init_I(tone));
   }

   public static void drawTone(GuiGraphicsExtractor context, Font renderer, String value, ThemedTextHelper.TextTone tone, int x, int y, boolean shadow) {
      autismclient.gui.f.b.k.init(context, renderer, value, init_Identifier(tone), init_I(tone), x, y, shadow);
   }

   public static void drawColor(GuiGraphicsExtractor context, Font renderer, String value, int color, int x, int y, boolean shadow) {
      autismclient.gui.f.b.k.init(context, renderer, value, autismclient.gui.f.a.DataUtil.init, color, x, y, shadow);
   }

   private static Identifier init_Identifier(ThemedTextHelper.TextTone tone) {
      Identifier var10000;
      switch (tone) {
         case TITLE:
            var10000 = autismclient.gui.f.a.DataUtil.read;
            break;
         case LABEL:
            var10000 = autismclient.gui.f.a.DataUtil.load;
            break;
         case BODY:
         case MUTED:
         case ACCENT:
            var10000 = autismclient.gui.f.a.DataUtil.init;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static FontDescription init(Identifier fontId) {
      return init.computeIfAbsent(fontId, Resource::new);
   }

   public enum TextTone {
      TITLE,
      LABEL,
      BODY,
      MUTED,
      ACCENT;
   }
}
