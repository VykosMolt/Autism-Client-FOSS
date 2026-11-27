package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class SnbtBraceRepairer {
   private SnbtBraceRepairer() {
   }

   public static String repair(String raw) {
      if (raw != null && !raw.isBlank()) {
         try {
            return init_Str(tokenize(raw));
         } catch (Throwable t) {
            return raw.trim();
         }
      } else {
         return "{}";
      }
   }

   private static String init_Str(List<SnbtBraceRepairer.Inner_StreamUtil> in) {
      int deficit = 0;

      for (SnbtBraceRepairer.Inner_StreamUtil t : in) {
         deficit += t.kind == SnbtBraceRepairer.Inner_DataUtil.OPEN ? 1 : (t.kind == SnbtBraceRepairer.Inner_DataUtil.CLOSE ? -1 : 0);
      }

      List<SnbtBraceRepairer.Inner_StreamUtil> out = new ArrayList<>(in.size() + 8);
      ArrayDeque<SnbtBraceRepairer.Inner_StreamUtil> open = new ArrayDeque<>();
      SnbtBraceRepairer.Inner_StreamUtil prev = null;

      for (SnbtBraceRepairer.Inner_StreamUtil t : in) {
         if (deficit > 0 && t.kind != SnbtBraceRepairer.Inner_DataUtil.CLOSE && prev != null && t.init_I > prev.init_I) {
            while (deficit > 0 && !open.isEmpty() && open.peek().init_I < t.init_I && t.load <= open.peek().load) {
               init(out, open.pop());
               deficit--;
            }

            prev = init_dl_StreamUtil(out);
         }

         switch (t.kind) {
            case OPEN:
               init(out, prev, t);
               open.push(t);
               out.add(t);
               break;
            case CLOSE:
               if (open.isEmpty()) {
                  continue;
               }

               init_V(out);
               SnbtBraceRepairer.Inner_StreamUtil opener = open.pop();
               out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.CLOSE, opener.text.equals("{") ? "}" : "]", t.init_I, t.load));
               break;
            case COMMA:
               SnbtBraceRepairer.Inner_StreamUtil lastTok = init_dl_StreamUtil(out);
               if (lastTok == null
                  || lastTok.kind == SnbtBraceRepairer.Inner_DataUtil.OPEN
                  || lastTok.kind == SnbtBraceRepairer.Inner_DataUtil.COMMA
                  || lastTok.kind == SnbtBraceRepairer.Inner_DataUtil.COLON
                  || lastTok.kind == SnbtBraceRepairer.Inner_DataUtil.SEMI) {
                  continue;
               }

               out.add(t);
               break;
            default:
               init(out, prev, t);
               out.add(t);
         }

         prev = init_dl_StreamUtil(out);
      }

      init_V(out);

      while (!open.isEmpty()) {
         init_V(out);
         init(out, open.pop());
      }

      StringBuilder sb = new StringBuilder();

      for (SnbtBraceRepairer.Inner_StreamUtil t : out) {
         sb.append(t.text);
      }

      return sb.isEmpty() ? "{}" : sb.toString();
   }

   private static void init(List<SnbtBraceRepairer.Inner_StreamUtil> out, SnbtBraceRepairer.Inner_StreamUtil prev, SnbtBraceRepairer.Inner_StreamUtil t) {
      if (prev != null) {
         boolean prevEnds = prev.kind == SnbtBraceRepairer.Inner_DataUtil.ATOM
            || prev.kind == SnbtBraceRepairer.Inner_DataUtil.STRING
            || prev.kind == SnbtBraceRepairer.Inner_DataUtil.CLOSE;
         boolean starts = t.kind == SnbtBraceRepairer.Inner_DataUtil.ATOM
            || t.kind == SnbtBraceRepairer.Inner_DataUtil.STRING
            || t.kind == SnbtBraceRepairer.Inner_DataUtil.OPEN;
         if (prevEnds && starts) {
            out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.COMMA, ",", t.init_I, t.load));
         }
      }
   }

   private static void init(List<SnbtBraceRepairer.Inner_StreamUtil> out, SnbtBraceRepairer.Inner_StreamUtil opener) {
      SnbtBraceRepairer.Inner_StreamUtil trailing = null;
      if (!out.isEmpty() && out.get(out.size() - 1).kind == SnbtBraceRepairer.Inner_DataUtil.COMMA) {
         trailing = out.remove(out.size() - 1);
      }

      out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.CLOSE, opener.text.equals("{") ? "}" : "]", opener.init_I, opener.load));
      if (trailing != null) {
         out.add(trailing);
      }
   }

   private static void init_V(List<SnbtBraceRepairer.Inner_StreamUtil> out) {
      if (!out.isEmpty() && out.get(out.size() - 1).kind == SnbtBraceRepairer.Inner_DataUtil.COMMA) {
         out.remove(out.size() - 1);
      }
   }

   private static SnbtBraceRepairer.Inner_StreamUtil init_dl_StreamUtil(List<SnbtBraceRepairer.Inner_StreamUtil> out) {
      return out.isEmpty() ? null : out.get(out.size() - 1);
   }

   private static List<SnbtBraceRepairer.Inner_StreamUtil> tokenize(String s) {
      List<SnbtBraceRepairer.Inner_StreamUtil> out = new ArrayList<>();
      int line = 0;
      int lineIndent = 0;
      boolean measuring = true;

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '\n') {
            line++;
            lineIndent = 0;
            measuring = true;
         } else {
            if (measuring) {
               if (c == ' ') {
                  lineIndent++;
                  continue;
               }

               if (c == '\t') {
                  lineIndent += 4;
                  continue;
               }

               measuring = false;
            }

            if (!Character.isWhitespace(c)) {
               if (c != '"' && c != '\'') {
                  int j;
                  switch (c) {
                     case ',':
                        out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.COMMA, ",", line, lineIndent));
                        continue;
                     case ':':
                        out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.COLON, ":", line, lineIndent));
                        continue;
                     case ';':
                        out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.SEMI, ";", line, lineIndent));
                        continue;
                     case '[':
                     case '{':
                        out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.OPEN, String.valueOf(c), line, lineIndent));
                        continue;
                     case ']':
                     case '}':
                        out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.CLOSE, String.valueOf(c), line, lineIndent));
                        continue;
                     default:
                        j = i;
                  }

                  while (j < s.length() && !Character.isWhitespace(s.charAt(j)) && "{}[],:;\"'".indexOf(s.charAt(j)) < 0) {
                     j++;
                  }

                  out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.ATOM, s.substring(i, j), line, lineIndent));
                  i = j - 1;
               } else {
                  int j = i + 1;
                  boolean esc = false;

                  while (j < s.length()) {
                     char d = s.charAt(j);
                     if (esc) {
                        esc = false;
                     } else if (d == '\\') {
                        esc = true;
                     } else if (d == c) {
                        break;
                     }

                     if (d == '\n') {
                        line++;
                     }

                     j++;
                  }

                  String body = j < s.length() ? s.substring(i, j + 1) : s.substring(i) + c;
                  out.add(new SnbtBraceRepairer.Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil.STRING, body, line, lineIndent));
                  i = Math.min(j, s.length() - 1);
               }
            }
         }
      }

      return out;
   }

   private enum Inner_DataUtil {
      OPEN,
      CLOSE,
      COMMA,
      COLON,
      SEMI,
      ATOM,
      STRING;
   }

   private record Inner_StreamUtil(SnbtBraceRepairer.Inner_DataUtil kind, String text, int init_I, int load) {
   }
}
