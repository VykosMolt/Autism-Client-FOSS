package autismclient.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;

final class PlayerDisplayNameParser {
   private static final int init = 2048;
   private static final int MAX_NODES = 256;
   private static final int read = 32;

   private PlayerDisplayNameParser() {
   }

   static boolean displayContainsName(String accountName, Component display) {
      if (!isPlausibleAccountName(accountName)) {
         return false;
      }

      PlayerDisplayNameParser.FlatText text = flatten(display);
      return text != null && indexOfName(text.text.toString(), accountName) >= 0;
   }

   static boolean isPlausibleAccountName(String name) {
      if (name != null && !name.isEmpty() && name.length() <= 32 && name.equals(name.strip())) {
         boolean alphanumeric = false;

         for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c < 'A' || c > 'Z') && (c < 'a' || c > 'z') && (c < '0' || c > '9')) {
               if (c != '_' && c != '.' && c != ' ') {
                  return false;
               }
            } else {
               alphanumeric = true;
            }
         }

         return alphanumeric;
      } else {
         return false;
      }
   }

   static MutableComponent displayName(String accountName, Component serverDisplay, TextColor fallbackColor) {
      if (!isPlausibleAccountName(accountName)) {
         return Component.empty();
      }

      PlayerDisplayNameParser.FlatText display = flatten(serverDisplay);
      int start = display == null ? -1 : indexOfName(display.text.toString(), accountName);
      MutableComponent result = Component.empty();
      int runStart = 0;
      TextColor current = start < 0 ? fallbackColor : orElse(display.colors.get(start), fallbackColor);

      for (int i = 1; i <= accountName.length(); i++) {
         TextColor next = i != accountName.length() && start >= 0 ? orElse(display.colors.get(start + i), fallbackColor) : fallbackColor;
         if (i == accountName.length() || !Objects.equals(current, next)) {
            result.append(Component.literal(accountName.substring(runStart, i)).setStyle(current == null ? Style.EMPTY : Style.EMPTY.withColor(current)));
            runStart = i;
            current = next;
         }
      }

      return result;
   }

   private static TextColor orElse(TextColor value, TextColor fallback) {
      return value == null ? fallback : value;
   }

   static int displayColor(String accountName, Component display, TextColor fallback) {
      long[] sum = new long[4];
      displayName(accountName, display, fallback).visit((style, text) -> {
         int rgb = style.getColor() == null ? 16777215 : style.getColor().getValue();
         int weight = text.length();
         sum[0] += (rgb >> 16 & 0xFF) * weight;
         sum[1] += (rgb >> 8 & 0xFF) * weight;
         sum[2] += (rgb & 0xFF) * weight;
         sum[3] += weight;
         return Optional.empty();
      }, Style.EMPTY);
      return sum[3] == 0L ? -1 : 0xFF000000 | (int)(sum[0] / sum[3]) << 16 | (int)(sum[1] / sum[3]) << 8 | (int)(sum[2] / sum[3]);
   }

   private static int indexOfName(String display, String accountName) {
      int found = -1;

      for (int i = 0; i <= display.length() - accountName.length(); i++) {
         if (display.regionMatches(true, i, accountName, 0, accountName.length())) {
            int end = i + accountName.length();
            if ((i <= 0 || !isNameChar(display.codePointBefore(i))) && (end >= display.length() || !isNameChar(display.codePointAt(end)))) {
               if (found >= 0) {
                  return -1;
               }

               found = i;
            }
         }
      }

      return found;
   }

   private static boolean isNameChar(int codepoint) {
      return Character.isLetterOrDigit(codepoint) || codepoint == 95 || codepoint == 46;
   }

   private static PlayerDisplayNameParser.FlatText flatten(Component component) {
      if (component == null) {
         return null;
      }

      PlayerDisplayNameParser.FlatText out = new PlayerDisplayNameParser.FlatText();

      try {
         return appendComponent(component, Style.EMPTY, 0, out) ? out : null;
      } catch (RuntimeException exception) {
         return null;
      }
   }

   private static boolean appendComponent(Component component, Style inherited, int depth, PlayerDisplayNameParser.FlatText out) {
      if (depth <= 32 && ++out.nodeCount <= 256) {
         if (component.getContents() instanceof PlainTextContents plain) {
            String var9 = plain.text();
            if ((out.length = out.length + var9.length()) > 2048) {
               return false;
            }

            Style style = component.getStyle().applyTo(inherited);
            appendLegacyText(var9, style.getColor(), out);

            for (Component sibling : component.getSiblings()) {
               if (!appendComponent(sibling, style, depth + 1, out)) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static void appendLegacyText(String text, TextColor inherited, PlayerDisplayNameParser.FlatText out) {
      TextColor color = inherited;
      int i = 0;

      while (i < text.length()) {
         char c = text.charAt(i);
         if (c == 167 && i + 1 < text.length()) {
            char code = Character.toLowerCase(text.charAt(i + 1));
            int rgb = code == 'x' ? parseHexColor(text, i + 2) : -1;
            if (rgb >= 0) {
               color = TextColor.fromRgb(rgb);
               i += 14;
            } else {
               ChatFormatting formatting = ChatFormatting.getByCode(code);
               if (formatting == ChatFormatting.RESET) {
                  color = inherited == null ? TextColor.fromRgb(16777215) : inherited;
               } else if (formatting != null) {
                  TextColor legacyColor = Style.EMPTY.applyLegacyFormat(formatting).getColor();
                  if (legacyColor != null) {
                     color = legacyColor;
                  }
               }

               i += 2;
            }
         } else {
            out.text.append(c);
            out.colors.add(color);
            i++;
         }
      }
   }

   private static int parseHexColor(String text, int start) {
      if (start + 12 > text.length()) {
         return -1;
      }

      int rgb = 0;

      for (int i = start; i < start + 12; i += 2) {
         if (text.charAt(i) != 167) {
            return -1;
         }

         int digit = Character.digit(text.charAt(i + 1), 16);
         if (digit < 0) {
            return -1;
         }

         rgb = rgb << 4 | digit;
      }

      return rgb;
   }

   private static final class FlatText {
      final StringBuilder text = new StringBuilder();
      final List<TextColor> colors = new ArrayList<>();
      int length;
      int nodeCount;
   }
}
