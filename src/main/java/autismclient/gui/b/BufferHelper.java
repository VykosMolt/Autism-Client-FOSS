package autismclient.gui.b;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;

public final class BufferHelper {
   private final List<BufferHelper.Inner_DataUtil> init_List = new ArrayList<>();
   private boolean init_Z;
   private boolean load_Z;
   private long init_J;
   private long load_J;
   private int init_I;
   private int load_I;
   private int read;
   private int write;

   public void init_V_List(List<BufferHelper.Inner_DataUtil> current) {
      this.init_List.clear();
      if (current != null) {
         this.init_List.addAll(current);
      }
   }

   public void init_V_J(long seq, int lineIndex, int charIndex) {
      this.init_J = this.load_J = seq;
      this.init_I = this.load_I = lineIndex;
      this.read = this.write = charIndex;
      this.load_Z = true;
      this.init_Z = false;
   }

   public void load_V_J(long seq, int lineIndex, int charIndex) {
      if (this.load_Z) {
         this.load_J = seq;
         this.load_I = lineIndex;
         this.write = charIndex;
         if (seq != this.init_J || lineIndex != this.init_I || charIndex != this.read) {
            this.init_Z = true;
         }
      }
   }

   public void init_V() {
      this.load_Z = false;
   }

   public boolean init_Z() {
      return this.load_Z;
   }

   public boolean load_Z() {
      return this.init_Z;
   }

   public void load_V() {
      this.init_Z = false;
      this.load_Z = false;
   }

   private int init_I_J_I(long seq, int lineIndex) {
      for (int i = 0; i < this.init_List.size(); i++) {
         BufferHelper.Inner_DataUtil row = this.init_List.get(i);
         if (row.init_J() == seq && row.init_I() == lineIndex) {
            return i;
         }
      }

      return -1;
   }

   public int[] init_IArr_J(long seq, int lineIndex, int rowLength) {
      if (!this.init_Z) {
         return null;
      } else {
         long[] bounds = this.init_JArr();
         if (bounds == null) {
            return null;
         } else {
            int loOrd = (int)bounds[0];
            int loChar = (int)bounds[1];
            int hiOrd = (int)bounds[2];
            int hiChar = (int)bounds[3];
            int rOrd = this.init_I_J_I(seq, lineIndex);
            if (rOrd >= 0 && rOrd >= loOrd && rOrd <= hiOrd) {
               int start = rOrd == loOrd ? loChar : 0;
               int end = rOrd == hiOrd ? hiChar : rowLength;
               start = Math.max(0, Math.min(start, rowLength));
               end = Math.max(start, Math.min(end, rowLength));
               return new int[]{start, end};
            } else {
               return null;
            }
         }
      }
   }

   public String init_Str() {
      if (!this.init_Z) {
         return "";
      }

      long[] bounds = this.init_JArr();
      if (bounds == null) {
         return "";
      }

      int loOrd = (int)bounds[0];
      int loChar = (int)bounds[1];
      int hiOrd = (int)bounds[2];
      int hiChar = (int)bounds[3];
      StringBuilder sb = new StringBuilder();

      for (int o = loOrd; o <= hiOrd; o++) {
         String t = this.init_List.get(o).init_Str();
         int start = o == loOrd ? Math.min(loChar, t.length()) : 0;
         int end = o == hiOrd ? Math.min(hiChar, t.length()) : t.length();
         if (o > loOrd) {
            sb.append('\n');
         }

         sb.append(t, Math.max(0, Math.min(start, end)), Math.max(start, end));
      }

      return sb.toString();
   }

   private long[] init_JArr() {
      int aOrd = this.init_I_J_I(this.init_J, this.init_I);
      int fOrd = this.init_I_J_I(this.load_J, this.load_I);
      if (aOrd >= 0 && fOrd >= 0) {
         return aOrd < fOrd || aOrd == fOrd && this.read <= this.write
            ? new long[]{aOrd, this.read, fOrd, this.write}
            : new long[]{fOrd, this.write, aOrd, this.read};
      } else {
         return null;
      }
   }

   public static int init_I_Font(Font font, FormattedText line, int chars) {
      return font != null && line != null && chars > 0 ? font.width(init_FormattedText_FormattedText_I(line, chars)) : 0;
   }

   public static int load_I_Font(Font font, FormattedText line, int relativeX) {
      if (font != null && line != null && relativeX > 0) {
         int[] index = new int[]{0};
         float[] acc = new float[]{0.0F};
         int[] result = new int[]{-1};
         line.visit((style, content) -> {
            for (int i = 0; i < content.length(); i++) {
               float cw = font.width(FormattedText.of(content.substring(i, i + 1), style));
               if (relativeX < acc[0] + cw / 2.0F) {
                  result[0] = index[0];
                  return Optional.of(Boolean.TRUE);
               }

               acc[0] += cw;
               index[0]++;
            }

            return Optional.empty();
         }, Style.EMPTY);
         return result[0] >= 0 ? result[0] : index[0];
      } else {
         return 0;
      }
   }

   private static FormattedText init_FormattedText_FormattedText_I(FormattedText line, int chars) {
      List<FormattedText> parts = new ArrayList<>();
      int[] remaining = new int[]{chars};
      line.visit((style, content) -> {
         int take = Math.min(remaining[0], content.length());
         if (take > 0) {
            parts.add(FormattedText.of(content.substring(0, take), style));
            remaining[0] -= take;
         }

         return remaining[0] <= 0 ? Optional.of(Boolean.TRUE) : Optional.empty();
      }, Style.EMPTY);
      return FormattedText.composite(parts);
   }

   public static String init_Str_FormattedText(FormattedText text) {
      if (text == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder();
      text.visit(part -> {
         sb.append(part);
         return Optional.empty();
      });
      return sb.toString();
   }

   public static String load(FormattedText text) {
      if (text == null) {
         return "";
      }

      StringBuilder sb = new StringBuilder();
      text.visit((style, part) -> {
         sb.append(part);
         return Optional.empty();
      }, Style.EMPTY);
      return sb.toString();
   }

   public record Inner_DataUtil(long init_J, int init_I, String init_Str) {
   }
}
