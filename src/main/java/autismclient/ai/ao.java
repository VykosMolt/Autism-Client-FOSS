package autismclient.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ao {
   static final int init_I = 1;
   static final int load_I = 2;
   static final int read_I = 4;
   static final int write_I = 8;
   static final int update_I = 16;
   private static final Pattern init_Pattern = Pattern.compile("\u001b\\[[0-9;?]*[ -/]*[@-~]");
   private static final Pattern load_Pattern = Pattern.compile("^ {0,3}(#{1,6})[ \\t]+(.*?)[ \\t#]*$");
   private static final Pattern read_Pattern = Pattern.compile("^( *)[-*+][ \\t]+(.*)$");
   private static final Pattern write_Pattern = Pattern.compile("^( *)(\\d{1,3})[.)][ \\t]+(.*)$");
   private static final Pattern update_Pattern = Pattern.compile("^ {0,3}([-*_])( *\\1){2,} *$");
   private static final Pattern sync = Pattern.compile("^ {0,3}(```+|~~~+).*$");
   private static final Pattern flush = Pattern.compile("\\[([^\\]]+)]\\(([^)\\s]+)\\)");

   private ao() {
   }

   static String init_Str(String text) {
      return text == null ? "" : init_Pattern.matcher(text).replaceAll("");
   }

   static List<ao.Inner_DataUtil> init_List(String markdown) {
      String text = init_Str(markdown).replace("\r", "");
      List<ao.Inner_DataUtil> blocks = new ArrayList<>();
      StringBuilder pending = new StringBuilder();
      ao.Inner_StreamUtil pendingType = ao.Inner_StreamUtil.init;
      StringBuilder code = null;

      for (String line : text.split("\n", -1)) {
         if (code != null) {
            if (sync.matcher(line).matches()) {
               blocks.add(init(ao.Inner_StreamUtil.update, code.toString()));
               code = null;
            } else {
               if (!code.isEmpty()) {
                  code.append('\n');
               }

               code.append(line);
            }
         } else if (sync.matcher(line).matches()) {
            init(pending, pendingType, blocks);
            code = new StringBuilder();
         } else if (line.isBlank()) {
            init(pending, pendingType, blocks);
         } else {
            Matcher m;
            if ((m = load_Pattern.matcher(line)).matches()) {
               init(pending, pendingType, blocks);
               blocks.add(init(ao.Inner_StreamUtil.load, m.group(2), "", m.group(1).length(), 0));
            } else if (update_Pattern.matcher(line).matches()) {
               init(pending, pendingType, blocks);
               blocks.add(init(ao.Inner_StreamUtil.flush, ""));
            } else if ((m = read_Pattern.matcher(line)).matches()) {
               init(pending, pendingType, blocks);
               blocks.add(init(ao.Inner_StreamUtil.read, m.group(2), "", 0, m.group(1).length() / 2));
            } else if ((m = write_Pattern.matcher(line)).matches()) {
               init(pending, pendingType, blocks);
               blocks.add(init(ao.Inner_StreamUtil.write, m.group(3), m.group(2) + ".", 0, m.group(1).length() / 2));
            } else {
               ao.Inner_StreamUtil type = line.startsWith(">") ? ao.Inner_StreamUtil.sync : ao.Inner_StreamUtil.init;
               String content = type == ao.Inner_StreamUtil.sync ? line.substring(1).stripLeading() : line;
               if (type != pendingType) {
                  init(pending, pendingType, blocks);
                  pendingType = type;
               }

               if (!pending.isEmpty()) {
                  pending.append('\n');
               }

               pending.append(content);
            }
         }
      }

      if (code != null) {
         String rest = code.toString();

         while (rest.endsWith("\n")) {
            rest = rest.substring(0, rest.length() - 1);
         }

         blocks.add(init(ao.Inner_StreamUtil.update, rest));
      }

      init(pending, pendingType, blocks);
      return blocks;
   }

   private static void init(StringBuilder pending, ao.Inner_StreamUtil type, List<ao.Inner_DataUtil> blocks) {
      if (!pending.isEmpty()) {
         blocks.add(init(type, pending.toString(), "", 0, 0));
         pending.setLength(0);
      }
   }

   private static ao.Inner_DataUtil init(ao.Inner_StreamUtil type, String text) {
      return new ao.Inner_DataUtil(type, text, new byte[text.length()], "", 0, 0);
   }

   static ao.Inner_DataUtil init(ao.Inner_StreamUtil type, String raw, String marker, int level, int indent) {
      StringBuilder out = new StringBuilder(raw.length());
      StringBuilder styles = new StringBuilder(raw.length());
      int base = type == ao.Inner_StreamUtil.load ? 17 : 0;
      int bold = 0;
      int italic = 0;
      int n = raw.length();
      int i = 0;

      while (i < n) {
         char c = raw.charAt(i);
         int current = base | bold | italic;
         if (c == '\\' && i + 1 < n && "*_`[]\\#".indexOf(raw.charAt(i + 1)) >= 0) {
            init(out, styles, raw.charAt(i + 1), current);
            i += 2;
         } else {
            if (c == '`') {
               int close = raw.indexOf(96, i + 1);
               if (close > i + 1) {
                  for (int k = i + 1; k < close; k++) {
                     init(out, styles, raw.charAt(k), base | 4);
                  }

                  i = close + 1;
                  continue;
               }
            }

            if (c == '[') {
               Matcher m = flush.matcher(raw).region(i, n);
               if (m.lookingAt()) {
                  for (char ch : m.group(1).toCharArray()) {
                     init(out, styles, ch, current | 8);
                  }

                  i = m.end();
                  continue;
               }
            }

            if (c == '*' || c == '_') {
               boolean twin = i + 1 < n && raw.charAt(i + 1) == c;
               int width = twin ? 2 : 1;
               boolean underscore = c == '_';
               if (twin ? bold != 0 : italic != 0) {
                  if (twin) {
                     bold = 0;
                  } else {
                     italic = 0;
                  }

                  i += width;
                  continue;
               }

               boolean wordStart = !underscore || i == 0 || !Character.isLetterOrDigit(raw.charAt(i - 1));
               boolean content = i + width < n && !Character.isWhitespace(raw.charAt(i + width));
               if (wordStart && content && init(raw, i + width, raw.substring(i, i + width), underscore)) {
                  if (twin) {
                     bold = 1;
                  } else {
                     italic = 2;
                  }

                  i += width;
                  continue;
               }
            }

            init(out, styles, c, current);
            i++;
         }
      }

      byte[] bytes = new byte[styles.length()];

      for (int ix = 0; ix < bytes.length; ix++) {
         bytes[ix] = (byte)styles.charAt(ix);
      }

      return new ao.Inner_DataUtil(type, out.toString(), bytes, marker, level, indent);
   }

   private static void init(StringBuilder out, StringBuilder styles, char c, int style) {
      out.append(c);
      styles.append((char)style);
   }

   private static boolean init(String s, int from, String delimiter, boolean underscore) {
      for (int at = s.indexOf(delimiter, from); at >= 0; at = s.indexOf(delimiter, at + delimiter.length())) {
         boolean boundary = !underscore || at + delimiter.length() >= s.length() || !Character.isLetterOrDigit(s.charAt(at + delimiter.length()));
         boolean afterContent = at > 0 && !Character.isWhitespace(s.charAt(at - 1));
         if (boundary && afterContent) {
            return true;
         }
      }

      return false;
   }

   record Inner_DataUtil(ao.Inner_StreamUtil type, String text, byte[] styles, String marker, int level, int indent) {
   }

   enum Inner_StreamUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }
}
