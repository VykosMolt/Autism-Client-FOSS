package autismclient.gui.ui.menu;

import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.Utf16TextCursor;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;

final class InlineTextEditState {
   final Object owner;
   private final String load_Str;
   private final InlineTextEditState.Inner_DataUtil init_StreamUtil_DataUti;
   String text;
   int cursor;
   private int load_I;
   private int read;

   InlineTextEditState(Object owner, String initial, InlineTextEditState.Inner_DataUtil sink) {
      this.owner = owner;
      this.init_StreamUtil_DataUti = sink;
      this.load_Str = initial == null ? "" : initial;
      this.text = this.load_Str;
      this.cursor = this.text.length();
      this.load_I = this.cursor;
   }

   int selectionStart() {
      return Math.min(this.cursor, this.load_I);
   }

   int selectionEnd() {
      return Math.max(this.cursor, this.load_I);
   }

   boolean hasSelection() {
      return this.cursor != this.load_I;
   }

   int scroll() {
      return this.read;
   }

   int init(int availableWidth, ToIntFunction<String> widths) {
      this.read = TextFieldRenderer.init(this.text, this.cursor, availableWidth, widths, this.read);
      return this.read;
   }

   void commit() {
      this.init_StreamUtil_DataUti.accept(this.text);
   }

   void load_V() {
      this.init_StreamUtil_DataUti.accept(this.load_Str);
   }

   int indexAt(float x, float fieldX, InlineTextEditState.Inner_StreamUtil widths) {
      float best = 3.4028235E38F;
      int bestIndex = 0;
      int i = 0;

      while (i <= this.text.length()) {
         float cx = fieldX + widths.width(this.text.substring(0, i));
         float distance = Math.abs(cx - x);
         if (distance < best) {
            best = distance;
            bestIndex = i;
         }

         if (i == this.text.length()) {
            break;
         }

         i = Utf16TextCursor.write(this.text, i);
      }

      return bestIndex;
   }

   void init(int position, boolean extendSelection) {
      this.cursor = Utf16TextCursor.clampIndex(this.text, position);
      if (!extendSelection) {
         this.load_I = this.cursor;
      }
   }

   boolean init(int key, int modifiers) {
      boolean ctrl = (modifiers & 2) != 0;
      boolean shift = (modifiers & 1) != 0;
      if (ctrl) {
         switch (key) {
            case 65:
               this.load_I = 0;
               this.cursor = this.text.length();
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
            this.commit();
            return true;
         case 259:
            if (!this.load_Z() && this.cursor > 0) {
               this.init_V_I(ctrl ? this.write_I() : Utf16TextCursor.read(this.text, this.cursor), this.cursor, "");
            }

            return true;
         case 261:
            if (!this.load_Z() && this.cursor < this.text.length()) {
               this.init_V_I(this.cursor, ctrl ? this.update() : Utf16TextCursor.write(this.text, this.cursor), "");
            }

            return true;
         case 262:
            if (this.hasSelection() && !shift) {
               this.init(this.selectionEnd(), false);
            } else {
               this.init(Utf16TextCursor.write(this.text, this.cursor), shift);
            }

            return true;
         case 263:
            if (this.hasSelection() && !shift) {
               this.init(this.selectionStart(), false);
            } else {
               this.init(Utf16TextCursor.read(this.text, this.cursor), shift);
            }

            return true;
         case 268:
            this.init(0, shift);
            return true;
         case 269:
            this.init(this.text.length(), shift);
            return true;
         default:
            return false;
      }
   }

   void typeChar(char chr) {
      if (chr >= ' ' && chr != 127) {
         this.init_V_I(this.selectionStart(), this.selectionEnd(), Character.toString(chr));
      }
   }

   private boolean load_Z() {
      if (!this.hasSelection()) {
         return false;
      }

      this.init_V_I(this.selectionStart(), this.selectionEnd(), "");
      return true;
   }

   private void init_V_I(int start, int end, String insert) {
      start = Utf16TextCursor.clampIndex(this.text, start);
      end = Utf16TextCursor.load(this.text, end);
      this.text = this.text.substring(0, start) + insert + this.text.substring(end);
      this.cursor = start + insert.length();
      this.load_I = this.cursor;
   }

   private void read_V() {
      if (this.hasSelection()) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.text.substring(this.selectionStart(), this.selectionEnd()));
      }
   }

   private void write_V() {
      String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
      if (clip != null && !clip.isEmpty()) {
         this.init_V_I(this.selectionStart(), this.selectionEnd(), init_Str_Str(clip));
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
      int i = this.cursor;

      while (i > 0 && !init_Z_C(this.text.charAt(i - 1))) {
         i--;
      }

      while (i > 0 && init_Z_C(this.text.charAt(i - 1))) {
         i--;
      }

      return i;
   }

   private int update() {
      int i = this.cursor;

      while (i < this.text.length() && init_Z_C(this.text.charAt(i))) {
         i++;
      }

      while (i < this.text.length() && !init_Z_C(this.text.charAt(i))) {
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
