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

   private static String init_Str(List<SnbtBraceRepairer.Token> in) {
      int deficit = 0;

      for (SnbtBraceRepairer.Token t : in) {
         deficit += t.kind == SnbtBraceRepairer.TokenKind.OPEN ? 1 : (t.kind == SnbtBraceRepairer.TokenKind.CLOSE ? -1 : 0);
      }

      List<SnbtBraceRepairer.Token> out = new ArrayList<>(in.size() + 8);
      ArrayDeque<SnbtBraceRepairer.Token> open = new ArrayDeque<>();
      SnbtBraceRepairer.Token prev = null;

      for (SnbtBraceRepairer.Token t : in) {
         if (deficit > 0 && t.kind != SnbtBraceRepairer.TokenKind.CLOSE && prev != null && t.init_I > prev.init_I) {
            while (deficit > 0 && !open.isEmpty() && open.peek().init_I < t.init_I && t.load <= open.peek().load) {
               closeOpener(out, open.pop());
               deficit--;
            }

            prev = init_dl_StreamUtil(out);
         }

         switch (t.kind) {
            case OPEN:
               appendWithSeparator(out, prev, t);
               open.push(t);
               out.add(t);
               break;
            case CLOSE:
               if (open.isEmpty()) {
                  continue;
               }

               init_V(out);
               SnbtBraceRepairer.Token opener = open.pop();
               out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.CLOSE, opener.text.equals("{") ? "}" : "]", t.init_I, t.load));
               break;
            case COMMA:
               SnbtBraceRepairer.Token lastTok = init_dl_StreamUtil(out);
               if (lastTok == null
                  || lastTok.kind == SnbtBraceRepairer.TokenKind.OPEN
                  || lastTok.kind == SnbtBraceRepairer.TokenKind.COMMA
                  || lastTok.kind == SnbtBraceRepairer.TokenKind.COLON
                  || lastTok.kind == SnbtBraceRepairer.TokenKind.SEMI) {
                  continue;
               }

               out.add(t);
               break;
            default:
               appendWithSeparator(out, prev, t);
               out.add(t);
         }

         prev = init_dl_StreamUtil(out);
      }

      init_V(out);

      while (!open.isEmpty()) {
         init_V(out);
         closeOpener(out, open.pop());
      }

      StringBuilder sb = new StringBuilder();

      for (SnbtBraceRepairer.Token t : out) {
         sb.append(t.text);
      }

      return sb.isEmpty() ? "{}" : sb.toString();
   }

   private static void appendWithSeparator(List<SnbtBraceRepairer.Token> out, SnbtBraceRepairer.Token prev, SnbtBraceRepairer.Token t) {
      if (prev != null) {
         boolean prevEnds = prev.kind == SnbtBraceRepairer.TokenKind.ATOM
            || prev.kind == SnbtBraceRepairer.TokenKind.STRING
            || prev.kind == SnbtBraceRepairer.TokenKind.CLOSE;
         boolean starts = t.kind == SnbtBraceRepairer.TokenKind.ATOM
            || t.kind == SnbtBraceRepairer.TokenKind.STRING
            || t.kind == SnbtBraceRepairer.TokenKind.OPEN;
         if (prevEnds && starts) {
            out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.COMMA, ",", t.init_I, t.load));
         }
      }
   }

   private static void closeOpener(List<SnbtBraceRepairer.Token> out, SnbtBraceRepairer.Token opener) {
      SnbtBraceRepairer.Token trailing = null;
      if (!out.isEmpty() && out.get(out.size() - 1).kind == SnbtBraceRepairer.TokenKind.COMMA) {
         trailing = out.remove(out.size() - 1);
      }

      out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.CLOSE, opener.text.equals("{") ? "}" : "]", opener.init_I, opener.load));
      if (trailing != null) {
         out.add(trailing);
      }
   }

   private static void init_V(List<SnbtBraceRepairer.Token> out) {
      if (!out.isEmpty() && out.get(out.size() - 1).kind == SnbtBraceRepairer.TokenKind.COMMA) {
         out.remove(out.size() - 1);
      }
   }

   private static SnbtBraceRepairer.Token init_dl_StreamUtil(List<SnbtBraceRepairer.Token> out) {
      return out.isEmpty() ? null : out.get(out.size() - 1);
   }

   private static List<SnbtBraceRepairer.Token> tokenize(String s) {
      List<SnbtBraceRepairer.Token> out = new ArrayList<>();
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
                        out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.COMMA, ",", line, lineIndent));
                        continue;
                     case ':':
                        out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.COLON, ":", line, lineIndent));
                        continue;
                     case ';':
                        out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.SEMI, ";", line, lineIndent));
                        continue;
                     case '[':
                     case '{':
                        out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.OPEN, String.valueOf(c), line, lineIndent));
                        continue;
                     case ']':
                     case '}':
                        out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.CLOSE, String.valueOf(c), line, lineIndent));
                        continue;
                     default:
                        j = i;
                  }

                  while (j < s.length() && !Character.isWhitespace(s.charAt(j)) && "{}[],:;\"'".indexOf(s.charAt(j)) < 0) {
                     j++;
                  }

                  out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.ATOM, s.substring(i, j), line, lineIndent));
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
                  out.add(new SnbtBraceRepairer.Token(SnbtBraceRepairer.TokenKind.STRING, body, line, lineIndent));
                  i = Math.min(j, s.length() - 1);
               }
            }
         }
      }

      return out;
   }

   private enum TokenKind {
      OPEN,
      CLOSE,
      COMMA,
      COLON,
      SEMI,
      ATOM,
      STRING;
   }

   private record Token(SnbtBraceRepairer.TokenKind kind, String text, int init_I, int load) {
   }
}
