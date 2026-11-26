package autismclient.gui.ui.menu;

import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.Utf16TextCursor;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;

final class InlineTextEditState {
   final Object init_Obj;
   private final String load_Str;
   private final InlineTextEditState.Inner_DataUtil init_StreamUtil_DataUti;
   String init_Str;
   int init_I;
   private int load_I;
   private int read;

   InlineTextEditState(Object owner, String initial, InlineTextEditState.Inner_DataUtil sink) {
      this.init_Obj = owner;
      this.init_StreamUtil_DataUti = sink;
      this.load_Str = initial == null ? "" : initial;
      this.init_Str = this.load_Str;
      this.init_I = this.init_Str.length();
      this.load_I = this.init_I;
   }

   int init_I() {
      return Math.min(this.init_I, this.load_I);
   }

   int load_I() {
      return Math.max(this.init_I, this.load_I);
   }

   boolean init_Z() {
      return this.init_I != this.load_I;
   }

   int read_I() {
      return this.read;
   }

   int init(int availableWidth, ToIntFunction<String> widths) {
      this.read = TextFieldRenderer.init(this.init_Str, this.init_I, availableWidth, widths, this.read);
      return this.read;
   }

   void init_V() {
      this.init_StreamUtil_DataUti.accept(this.init_Str);
   }

   void load_V() {
      this.init_StreamUtil_DataUti.accept(this.load_Str);
   }

   int init_I_F(float x, float fieldX, InlineTextEditState.Inner_StreamUtil widths) {
      float best = 3.4028235E38F;
      int bestIndex = 0;
      int i = 0;

      while (i <= this.init_Str.length()) {
         float cx = fieldX + widths.width(this.init_Str.substring(0, i));
         float distance = Math.abs(cx - x);
         if (distance < best) {
            best = distance;
            bestIndex = i;
         }

         if (i == this.init_Str.length()) {
            break;
         }

         i = Utf16TextCursor.write(this.init_Str, i);
      }

      return bestIndex;
   }

   void init(int position, boolean extendSelection) {
      this.init_I = Utf16TextCursor.init_I(this.init_Str, position);
      if (!extendSelection) {
         this.load_I = this.init_I;
      }
   }

   boolean init(int key, int modifiers) {
      boolean ctrl = (modifiers & 2) != 0;
      boolean shift = (modifiers & 1) != 0;
      if (ctrl) {
         switch (key) {
            case 65:
               this.load_I = 0;
               this.init_I = this.init_Str.length();
               return true;
            case 67:
               this.read_V();
               return true;
            case 86:
               this.write_V();
               return true;
            case 88:
               this.read_V();
               this.load_Z();
               return true;
            case 262:
               this.init(this.update(), shift);
               return true;
            case 263:
               this.init(this.write_I(), shift);
               return true;
         }
      }

      switch (key) {
         case 257:
         case 335:
            this.init_V();
            return true;
         case 259:
            if (!this.load_Z() && this.init_I > 0) {
               this.init_V_I(ctrl ? this.write_I() : Utf16TextCursor.read(this.init_Str, this.init_I), this.init_I, "");
            }

            return true;
         case 261:
            if (!this.load_Z() && this.init_I < this.init_Str.length()) {
               this.init_V_I(this.init_I, ctrl ? this.update() : Utf16TextCursor.write(this.init_Str, this.init_I), "");
            }

            return true;
         case 262:
            if (this.init_Z() && !shift) {
               this.init(this.load_I(), false);
            } else {
               this.init(Utf16TextCursor.write(this.init_Str, this.init_I), shift);
            }

            return true;
         case 263:
            if (this.init_Z() && !shift) {
               this.init(this.init_I(), false);
            } else {
               this.init(Utf16TextCursor.read(this.init_Str, this.init_I), shift);
            }

            return true;
         case 268:
            this.init(0, shift);
            return true;
         case 269:
            this.init(this.init_Str.length(), shift);
            return true;
         default:
            return false;
      }
   }

   void init_V_C(char chr) {
      if (chr >= ' ' && chr != 127) {
         this.init_V_I(this.init_I(), this.load_I(), Character.toString(chr));
      }
   }

   private boolean load_Z() {
      if (!this.init_Z()) {
         return false;
      }

      this.init_V_I(this.init_I(), this.load_I(), "");
      return true;
   }

   private void init_V_I(int start, int end, String insert) {
      start = Utf16TextCursor.init_I(this.init_Str, start);
      end = Utf16TextCursor.load(this.init_Str, end);
      this.init_Str = this.init_Str.substring(0, start) + insert + this.init_Str.substring(end);
      this.init_I = start + insert.length();
      this.load_I = this.init_I;
   }

   private void read_V() {
      if (this.init_Z()) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.init_Str.substring(this.init_I(), this.load_I()));
      }
   }

   private void write_V() {
      String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
      if (clip != null && !clip.isEmpty()) {
         this.init_V_I(this.init_I(), this.load_I(), init_Str_Str(clip));
      }
   }

   private static String init_Str_Str(String raw) {
      StringBuilder out = new StringBuilder(raw.length());

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (c >= ' ' && c != 127) {
            out.append(c);
         }
      }

      return out.toString();
   }

   private int write_I() {
      int i = this.init_I;

      while (i > 0 && !init_Z_C(this.init_Str.charAt(i - 1))) {
         i--;
      }

      while (i > 0 && init_Z_C(this.init_Str.charAt(i - 1))) {
         i--;
      }

      return i;
   }

   private int update() {
      int i = this.init_I;

      while (i < this.init_Str.length() && init_Z_C(this.init_Str.charAt(i))) {
         i++;
      }

      while (i < this.init_Str.length() && !init_Z_C(this.init_Str.charAt(i))) {
         i++;
      }

      return i;
   }

   private static boolean init_Z_C(char c) {
      return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-';
   }

   interface Inner_DataUtil {
      void accept(String var1);
   }

   interface Inner_StreamUtil {
      float width(String var1);
   }
}
