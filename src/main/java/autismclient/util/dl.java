package autismclient.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class dl {
   private dl() {
   }

   public static String init_Str(String raw) {
      if (raw != null && !raw.isBlank()) {
         try {
            return init_Str(init_List(raw));
         } catch (Throwable t) {
            return raw.trim();
         }
      } else {
         return "{}";
      }
   }

   private static String init_Str(List<dl.Inner_StreamUtil> in) {
      int deficit = 0;

      for (dl.Inner_StreamUtil t : in) {
         deficit += t.init_dl_DataUtil == dl.Inner_DataUtil.init ? 1 : (t.init_dl_DataUtil == dl.Inner_DataUtil.load ? -1 : 0);
      }

      List<dl.Inner_StreamUtil> out = new ArrayList<>(in.size() + 8);
      ArrayDeque<dl.Inner_StreamUtil> open = new ArrayDeque<>();
      dl.Inner_StreamUtil prev = null;

      for (dl.Inner_StreamUtil t : in) {
         if (deficit > 0 && t.init_dl_DataUtil != dl.Inner_DataUtil.load && prev != null && t.init_I > prev.init_I) {
            while (deficit > 0 && !open.isEmpty() && open.peek().init_I < t.init_I && t.load <= open.peek().load) {
               init(out, open.pop());
               deficit--;
            }

            prev = init_dl_StreamUtil(out);
         }

         switch (t.init_dl_DataUtil) {
            case init:
               init(out, prev, t);
               open.push(t);
               out.add(t);
               break;
            case load:
               if (open.isEmpty()) {
                  continue;
               }

               init_V(out);
               dl.Inner_StreamUtil opener = open.pop();
               out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.load, opener.init_Str.equals("{") ? "}" : "]", t.init_I, t.load));
               break;
            case read:
               dl.Inner_StreamUtil lastTok = init_dl_StreamUtil(out);
               if (lastTok == null
                  || lastTok.init_dl_DataUtil == dl.Inner_DataUtil.init
                  || lastTok.init_dl_DataUtil == dl.Inner_DataUtil.read
                  || lastTok.init_dl_DataUtil == dl.Inner_DataUtil.write
                  || lastTok.init_dl_DataUtil == dl.Inner_DataUtil.update) {
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

      for (dl.Inner_StreamUtil t : out) {
         sb.append(t.init_Str);
      }

      return sb.isEmpty() ? "{}" : sb.toString();
   }

   private static void init(List<dl.Inner_StreamUtil> out, dl.Inner_StreamUtil prev, dl.Inner_StreamUtil t) {
      if (prev != null) {
         boolean prevEnds = prev.init_dl_DataUtil == dl.Inner_DataUtil.sync
            || prev.init_dl_DataUtil == dl.Inner_DataUtil.flush
            || prev.init_dl_DataUtil == dl.Inner_DataUtil.load;
         boolean starts = t.init_dl_DataUtil == dl.Inner_DataUtil.sync
            || t.init_dl_DataUtil == dl.Inner_DataUtil.flush
            || t.init_dl_DataUtil == dl.Inner_DataUtil.init;
         if (prevEnds && starts) {
            out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.read, ",", t.init_I, t.load));
         }
      }
   }

   private static void init(List<dl.Inner_StreamUtil> out, dl.Inner_StreamUtil opener) {
      dl.Inner_StreamUtil trailing = null;
      if (!out.isEmpty() && out.get(out.size() - 1).init_dl_DataUtil == dl.Inner_DataUtil.read) {
         trailing = out.remove(out.size() - 1);
      }

      out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.load, opener.init_Str.equals("{") ? "}" : "]", opener.init_I, opener.load));
      if (trailing != null) {
         out.add(trailing);
      }
   }

   private static void init_V(List<dl.Inner_StreamUtil> out) {
      if (!out.isEmpty() && out.get(out.size() - 1).init_dl_DataUtil == dl.Inner_DataUtil.read) {
         out.remove(out.size() - 1);
      }
   }

   private static dl.Inner_StreamUtil init_dl_StreamUtil(List<dl.Inner_StreamUtil> out) {
      return out.isEmpty() ? null : out.get(out.size() - 1);
   }

   private static List<dl.Inner_StreamUtil> init_List(String s) {
      List<dl.Inner_StreamUtil> out = new ArrayList<>();
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
                        out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.read, ",", line, lineIndent));
                        continue;
                     case ':':
                        out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.write, ":", line, lineIndent));
                        continue;
                     case ';':
                        out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.update, ";", line, lineIndent));
                        continue;
                     case '[':
                     case '{':
                        out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.init, String.valueOf(c), line, lineIndent));
                        continue;
                     case ']':
                     case '}':
                        out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.load, String.valueOf(c), line, lineIndent));
                        continue;
                     default:
                        j = i;
                  }

                  while (j < s.length() && !Character.isWhitespace(s.charAt(j)) && "{}[],:;\"'".indexOf(s.charAt(j)) < 0) {
                     j++;
                  }

                  out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.sync, s.substring(i, j), line, lineIndent));
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
                  out.add(new dl.Inner_StreamUtil(dl.Inner_DataUtil.flush, body, line, lineIndent));
                  i = Math.min(j, s.length() - 1);
               }
            }
         }
      }

      return out;
   }

   private enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush;
   }

   private record Inner_StreamUtil(dl.Inner_DataUtil init_dl_DataUtil, String init_Str, int init_I, int load) {
   }
}
