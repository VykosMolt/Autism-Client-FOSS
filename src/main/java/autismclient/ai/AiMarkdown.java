package autismclient.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class AiMarkdown {
   static final int init_I = 1;
   static final int load_I = 2;
   static final int read_I = 4;
   static final int write_I = 8;
   static final int update_I = 16;
   private static final Pattern ANSI_ESCAPE = Pattern.compile("\u001b\\[[0-9;?]*[ -/]*[@-~]");
   private static final Pattern HEADING = Pattern.compile("^ {0,3}(#{1,6})[ \\t]+(.*?)[ \\t#]*$");
   private static final Pattern BULLET = Pattern.compile("^( *)[-*+][ \\t]+(.*)$");
   private static final Pattern ORDERED = Pattern.compile("^( *)(\\d{1,3})[.)][ \\t]+(.*)$");
   private static final Pattern RULE = Pattern.compile("^ {0,3}([-*_])( *\\1){2,} *$");
   private static final Pattern FENCE = Pattern.compile("^ {0,3}(```+|~~~+).*$");
   private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)\\s]+)\\)");

   private AiMarkdown() {
   }

   static String stripAnsi(String text) {
      return text == null ? "" : ANSI_ESCAPE.matcher(text).replaceAll("");
   }

   static List<AiMarkdown.MarkdownBlock> parse(String markdown) {
      String text = stripAnsi(markdown).replace("\r", "");
      List<AiMarkdown.MarkdownBlock> blocks = new ArrayList<>();
      StringBuilder pending = new StringBuilder();
      AiMarkdown.BlockType pendingType = AiMarkdown.BlockType.PARAGRAPH;
      StringBuilder code = null;

      for (String line : text.split("\n", -1)) {
         if (code != null) {
            if (FENCE.matcher(line).matches()) {
               blocks.add(block(AiMarkdown.BlockType.CODE, code.toString()));
               code = null;
            } else {
               if (!code.isEmpty()) {
                  code.append('\n');
               }

               code.append(line);
            }
         } else if (FENCE.matcher(line).matches()) {
            flushParagraph(pending, pendingType, blocks);
            code = new StringBuilder();
         } else if (line.isBlank()) {
            flushParagraph(pending, pendingType, blocks);
         } else {
            Matcher m;
            if ((m = HEADING.matcher(line)).matches()) {
               flushParagraph(pending, pendingType, blocks);
               blocks.add(styledBlock(AiMarkdown.BlockType.HEADING, m.group(2), "", m.group(1).length(), 0));
            } else if (RULE.matcher(line).matches()) {
               flushParagraph(pending, pendingType, blocks);
               blocks.add(block(AiMarkdown.BlockType.RULE, ""));
            } else if ((m = BULLET.matcher(line)).matches()) {
               flushParagraph(pending, pendingType, blocks);
               blocks.add(styledBlock(AiMarkdown.BlockType.BULLET, m.group(2), "", 0, m.group(1).length() / 2));
            } else if ((m = ORDERED.matcher(line)).matches()) {
               flushParagraph(pending, pendingType, blocks);
               blocks.add(styledBlock(AiMarkdown.BlockType.NUMBERED, m.group(3), m.group(2) + ".", 0, m.group(1).length() / 2));
            } else {
               AiMarkdown.BlockType type = line.startsWith(">") ? AiMarkdown.BlockType.QUOTE : AiMarkdown.BlockType.PARAGRAPH;
               String content = type == AiMarkdown.BlockType.QUOTE ? line.substring(1).stripLeading() : line;
               if (type != pendingType) {
                  flushParagraph(pending, pendingType, blocks);
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

         blocks.add(block(AiMarkdown.BlockType.CODE, rest));
      }

      flushParagraph(pending, pendingType, blocks);
      return blocks;
   }

   private static void flushParagraph(StringBuilder pending, AiMarkdown.BlockType type, List<AiMarkdown.MarkdownBlock> blocks) {
      if (!pending.isEmpty()) {
         blocks.add(styledBlock(type, pending.toString(), "", 0, 0));
         pending.setLength(0);
      }
   }

   private static AiMarkdown.MarkdownBlock block(AiMarkdown.BlockType type, String text) {
      return new AiMarkdown.MarkdownBlock(type, text, new byte[text.length()], "", 0, 0);
   }

   static AiMarkdown.MarkdownBlock styledBlock(AiMarkdown.BlockType type, String raw, String marker, int level, int indent) {
      StringBuilder out = new StringBuilder(raw.length());
      StringBuilder styles = new StringBuilder(raw.length());
      int base = type == AiMarkdown.BlockType.HEADING ? 17 : 0;
      int bold = 0;
      int italic = 0;
      int n = raw.length();
      int i = 0;

      while (i < n) {
         char c = raw.charAt(i);
         int current = base | bold | italic;
         if (c == '\\' && i + 1 < n && "*_`[]\\#".indexOf(raw.charAt(i + 1)) >= 0) {
            appendStyled(out, styles, raw.charAt(i + 1), current);
            i += 2;
         } else {
            if (c == '`') {
               int close = raw.indexOf(96, i + 1);
               if (close > i + 1) {
                  for (int k = i + 1; k < close; k++) {
                     appendStyled(out, styles, raw.charAt(k), base | 4);
                  }

                  i = close + 1;
                  continue;
               }
            }

            if (c == '[') {
               Matcher m = LINK.matcher(raw).region(i, n);
               if (m.lookingAt()) {
                  for (char ch : m.group(1).toCharArray()) {
                     appendStyled(out, styles, ch, current | 8);
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
               if (wordStart && content && hasClosing(raw, i + width, raw.substring(i, i + width), underscore)) {
                  if (twin) {
                     bold = 1;
                  } else {
                     italic = 2;
                  }

                  i += width;
                  continue;
               }
            }

            appendStyled(out, styles, c, current);
            i++;
         }
      }

      byte[] bytes = new byte[styles.length()];

      for (int ix = 0; ix < bytes.length; ix++) {
         bytes[ix] = (byte)styles.charAt(ix);
      }

      return new AiMarkdown.MarkdownBlock(type, out.toString(), bytes, marker, level, indent);
   }

   private static void appendStyled(StringBuilder out, StringBuilder styles, char c, int style) {
      out.append(c);
      styles.append((char)style);
   }

   private static boolean hasClosing(String s, int from, String delimiter, boolean underscore) {
      for (int at = s.indexOf(delimiter, from); at >= 0; at = s.indexOf(delimiter, at + delimiter.length())) {
         boolean boundary = !underscore || at + delimiter.length() >= s.length() || !Character.isLetterOrDigit(s.charAt(at + delimiter.length()));
         boolean afterContent = at > 0 && !Character.isWhitespace(s.charAt(at - 1));
         if (boundary && afterContent) {
            return true;
         }
      }

      return false;
   }

   record MarkdownBlock(AiMarkdown.BlockType type, String text, byte[] styles, String marker, int level, int indent) {
   }

   enum BlockType {
      PARAGRAPH,
      HEADING,
      BULLET,
      NUMBERED,
      CODE,
      QUOTE,
      RULE;
   }
}
