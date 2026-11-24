package autismclient.util.g.a;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.ClickEvent.RunCommand;

public final class DataUtil {
   private static final Pattern init_Pattern = Pattern.compile("(\\d{1,3})\\s*([+\\-])\\s*(\\d{1,3})");
   private static final Pattern load = Pattern.compile("(?<![A-Za-z0-9])([A-Za-z0-9]{3,8})(?![A-Za-z0-9])");
   private static final Pattern read = Pattern.compile("/([A-Za-z0-9_]*captcha[A-Za-z0-9_]*)[\\s:]+([A-Za-z0-9]{2,16})(?![A-Za-z0-9])", 2);
   private static final Pattern write = Pattern.compile("captcha|verif|\\bcode\\b|type the|enter the|solve|try again|➤", 2);
   private static final String[][] init_StrArr2 = new String[][]{
      {"pink", "light_purple"},
      {"purple", "dark_purple"},
      {"blue", "blue"},
      {"aqua", "aqua"},
      {"cyan", "aqua"},
      {"green", "green"},
      {"lime", "green"},
      {"yellow", "yellow"},
      {"gold", "gold"},
      {"orange", "gold"},
      {"red", "red"},
      {"white", "white"},
      {"gray", "gray"},
      {"grey", "gray"}
   };
   private final ArrayDeque<String> init_ArrayDeque = new ArrayDeque<>();

   public DataUtil.Inner_DataUtil init_DataUtil_DataUtil_Component(Component component) {
      if (component == null) {
         return null;
      }

      String plain = component.getString();
      DataUtil.Inner_DataUtil slash = init_DataUtil_DataUtil_Str(plain);
      if (slash != null) {
         this.init_V_Str(plain);
         return slash;
      }

      List<DataUtil.Inner_StreamUtil> buttons = new ArrayList<>();
      this.init(component, buttons);
      if (!buttons.isEmpty()) {
         DataUtil.Inner_DataUtil a = this.init(buttons, plain);
         if (a != null) {
            this.init_V_Str(plain);
            return a;
         }
      }

      DataUtil.Inner_DataUtil code = this.read(plain);
      this.init_V_Str(plain);
      return code;
   }

   private static DataUtil.Inner_DataUtil init_DataUtil_DataUtil_Str(String line) {
      if (line == null) {
         return null;
      }

      Matcher m = read.matcher(line);

      while (m.find()) {
         if (!m.group(1).toLowerCase(Locale.ROOT).contains("click")) {
            return new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.load, m.group(1) + " " + m.group(2));
         }
      }

      return null;
   }

   private void init(Component node, List<DataUtil.Inner_StreamUtil> out) {
      Style style = node.getStyle();
      if (style != null && style.getClickEvent() instanceof RunCommand rc) {
         String cmd = rc.command();
         if (cmd != null && cmd.toLowerCase(Locale.ROOT).contains("captcha_click")) {
            out.add(new DataUtil.Inner_StreamUtil(cmd, node.getString().trim(), style.getColor()));
         }
      }

      for (Component sibling : node.getSiblings()) {
         this.init(sibling, out);
      }
   }

   private DataUtil.Inner_DataUtil init(List<DataUtil.Inner_StreamUtil> buttons, String currentLine) {
      String context = String.join(" ", this.init_ArrayDeque).toLowerCase(Locale.ROOT);
      Matcher m = init_Pattern.matcher(context);

      while (m.find()) {
         int a = Integer.parseInt(m.group(1));
         int b = Integer.parseInt(m.group(3));
         int result = m.group(2).equals("-") ? a - b : a + b;

         for (DataUtil.Inner_StreamUtil btn : buttons) {
            String label = btn.load().replaceAll("[^0-9-]", "");
            if (!label.isEmpty() && init_I_Str(label) == result) {
               return load(btn.init_Str());
            }
         }
      }

      List<DataUtil.Inner_StreamUtil> labelHits = new ArrayList<>();

      for (DataUtil.Inner_StreamUtil btn : buttons) {
         String label = btn.load().toLowerCase(Locale.ROOT).trim();
         if (label.length() >= 2 && context.contains(label)) {
            labelHits.add(btn);
         }
      }

      if (labelHits.size() == 1) {
         return load(labelHits.get(0).init_Str());
      }

      if (labelHits.size() > 1) {
         for (DataUtil.Inner_StreamUtil btn : labelHits) {
            if (this.init(btn.init_TextColor(), context)) {
               return load(btn.init_Str());
            }
         }

         return load(labelHits.get(0).init_Str());
      } else {
         for (DataUtil.Inner_StreamUtil btn : buttons) {
            if (this.init(btn.init_TextColor(), context)) {
               return load(btn.init_Str());
            }
         }

         return null;
      }
   }

   private boolean init(TextColor color, String context) {
      if (color == null) {
         return false;
      }

      String name = color.serialize().toLowerCase(Locale.ROOT);

      for (String[] pair : init_StrArr2) {
         if (pair[1].equals(name) && context.contains(pair[0])) {
            return true;
         }
      }

      return false;
   }

   private static DataUtil.Inner_DataUtil load(String clickCommand) {
      String cmd = clickCommand.startsWith("/") ? clickCommand.substring(1) : clickCommand;
      return new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.load, cmd);
   }

   private DataUtil.Inner_DataUtil read(String line) {
      if (line != null && !line.isBlank()) {
         if (line.toLowerCase(Locale.ROOT).contains("captcha_click")) {
            return null;
         }

         boolean hint = write.matcher(line).find();
         int arrow = line.indexOf(10148);
         if (!hint && arrow < 0) {
            return null;
         }

         String search = arrow >= 0 ? line.substring(arrow + 1) : line;
         Matcher m = load.matcher(search);
         String candidate = null;

         while (m.find()) {
            String tok = m.group(1);
            if (tok.chars().anyMatch(Character::isDigit) || tok.equals(tok.toUpperCase(Locale.ROOT))) {
               candidate = tok;
            }
         }

         return candidate == null ? null : new DataUtil.Inner_DataUtil(DataUtil.Inner_CachePool.init, candidate);
      } else {
         return null;
      }
   }

   private void init_V_Str(String line) {
      if (line != null && !line.isBlank()) {
         this.init_ArrayDeque.addLast(line);

         while (this.init_ArrayDeque.size() > 6) {
            this.init_ArrayDeque.removeFirst();
         }
      }
   }

   private static int init_I_Str(String s) {
      try {
         return Integer.parseInt(s);
      } catch (NumberFormatException e) {
         return -2147483648;
      }
   }

   public enum Inner_CachePool {
      init,
      load;
   }

   public record Inner_DataUtil(DataUtil.Inner_CachePool init_DataUtil_CachePool, String init_Str) {
   }

   private record Inner_StreamUtil(String init_Str, String load, TextColor init_TextColor) {
   }
}
