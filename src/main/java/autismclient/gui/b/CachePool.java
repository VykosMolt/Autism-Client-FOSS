package autismclient.gui.b;

import autismclient.compat263.Compat263;
import autismclient.util.AutismClientMessaging;
import autismclient.util.g.PacketUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.RunCommand;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

public final class CachePool {
   private CachePool() {
   }

   public static List<CachePool.Inner_DataUtil> init(
      Font font, List<PacketUtil.Inner_BufferHelper> lines, int width, int totalAccounts, Function<String, String> accountLabel, int mutedColor
   ) {
      List<CachePool.Inner_DataUtil> out = new ArrayList<>();
      if (font != null && lines != null) {
         int available = Math.max(1, width);

         for (PacketUtil.Inner_BufferHelper line : lines) {
            Component full = init(line, totalAccounts, accountLabel, mutedColor);
            List<FormattedCharSequence> render = font.split(full, available);
            List<FormattedText> hit = font.getSplitter().splitLines(full, available, Style.EMPTY);

            for (int index = 0; index < render.size(); index++) {
               FormattedText hitLine = index < hit.size() ? hit.get(index) : FormattedText.EMPTY;
               out.add(new CachePool.Inner_DataUtil(line, index, render.get(index), hitLine));
            }
         }

         return out;
      } else {
         return out;
      }
   }

   public static Component init(PacketUtil.Inner_BufferHelper line, int totalAccounts, Function<String, String> accountLabel, int mutedColor) {
      Component message = line != null && line.init_Component() != null ? line.init_Component() : Component.empty();
      return init(line, message, totalAccounts, accountLabel, mutedColor);
   }

   public static Component load(PacketUtil.Inner_BufferHelper line, int totalAccounts, Function<String, String> accountLabel, int mutedColor) {
      if (line != null && !line.init_Z()) {
         Map<String, Component> targets = line.init_Map();
         int count = targets == null ? 0 : targets.size();
         if (count == 1 && totalAccounts > 1) {
            String id = targets.keySet().iterator().next();
            String label = accountLabel == null ? null : accountLabel.apply(id);
            String name = label != null && !label.isBlank() ? PacketUtil.update(label, 16) : id;
            return Component.literal("[" + name + "] ").withColor(mutedColor);
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static void init(GuiGraphicsExtractor graphics, Font font, FormattedText line, int x, int y) {
      if (graphics != null && font != null && line != null) {
         int[] offset = new int[]{0};
         line.visit((style, text) -> {
            int width = font.width(FormattedText.of(text, style));
            if (style.getClickEvent() != null && width > 0) {
               autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(x + offset[0], y + 9, width, 1), -8738817);
            }

            offset[0] += width;
            return Optional.empty();
         }, Style.EMPTY);
      }
   }

   public static void init(URI uri) {
      if (uri != null && uri.getScheme() != null) {
         String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
         if (scheme.equals("http") || scheme.equals("https")) {
            Compat263.openUri(Util.getPlatform(), uri);
         }
      }
   }

   public static ClickEvent init(Font font, FormattedText line, int relativeX) {
      if (font != null && line != null && relativeX >= 0) {
         ClickEvent[] found = new ClickEvent[]{null};
         int[] offset = new int[]{0};
         line.visit((style, text) -> {
            int width = font.width(FormattedText.of(text, style));
            if (relativeX < offset[0] + width) {
               found[0] = style.getClickEvent();
               return Optional.of(Boolean.TRUE);
            } else {
               offset[0] += width;
               return Optional.empty();
            }
         }, Style.EMPTY);
         return found[0];
      } else {
         return null;
      }
   }

   public static int init(
      Font font,
      PacketUtil.Inner_BufferHelper line,
      int lineIndex,
      int relativeX,
      int wrapWidth,
      int totalAccounts,
      Function<String, String> accountLabel,
      int mutedColor,
      RunCommand clicked
   ) {
      if (font != null && line != null && line.init_Map() != null) {
         int ran = 0;

         for (Entry<String, Component> target : line.init_Map().entrySet()) {
            Component full = init(line, target.getValue(), totalAccounts, accountLabel, mutedColor);
            List<FormattedText> wrapped = font.getSplitter().splitLines(full, Math.max(1, wrapWidth), Style.EMPTY);
            String command = (lineIndex < wrapped.size() ? init(font, wrapped.get(lineIndex), relativeX) : null) instanceof RunCommand own
               ? own.command()
               : (clicked == null ? null : clicked.command());
            if (command != null && !command.isBlank()) {
               PacketUtil.init_PacketUtil().init_Str_Str_Str(target.getKey(), command);
               ran++;
            }
         }

         return ran;
      } else {
         return 0;
      }
   }

   private static Component init(
      PacketUtil.Inner_BufferHelper line, Component message, int totalAccounts, Function<String, String> accountLabel, int mutedColor
   ) {
      MutableComponent out = Component.empty();
      if (line != null && line.init_Str() != null && !line.init_Str().isEmpty()) {
         out.append(AutismClientMessaging.themedTag(PacketUtil.update(line.init_Str(), 24)));
      }

      Component recipients = load(line, totalAccounts, accountLabel, mutedColor);
      if (recipients != null) {
         out.append(recipients);
      }

      out.append(message == null ? Component.empty() : StreamUtil.init(message));
      if (line != null && line.init_I() > 1) {
         out.append(Component.literal(" (x" + line.init_I() + ")").withColor(mutedColor));
      }

      return out;
   }

   public record Inner_DataUtil(
      PacketUtil.Inner_BufferHelper init_PacketUtil_BufferH, int init_I, FormattedCharSequence init_FormattedCharSeque, FormattedText init_FormattedText
   ) {
   }
}
