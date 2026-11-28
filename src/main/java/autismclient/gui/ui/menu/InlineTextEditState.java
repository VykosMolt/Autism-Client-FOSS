package autismclient.gui.ui.menu;

import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.Utf16TextCursor;
import java.util.function.ToIntFunction;
import net.minecraft.client.Minecraft;

final class InlineTextEditState {
   final Object owner;
   private final String initialText;
   private final InlineTextEditState.TextSink sink;
   String text;
   int cursor;
   private int anchor;
   private int scrollX;

   InlineTextEditState(Object owner, String initial, InlineTextEditState.TextSink sink) {
      this.owner = owner;
      this.sink = sink;
      this.initialText = initial == null ? "" : initial;
      this.text = this.initialText;
      this.cursor = this.text.length();
      this.anchor = this.cursor;
   }

   int selectionStart() {
      return Math.min(this.cursor, this.anchor);
   }

   int selectionEnd() {
      return Math.max(this.cursor, this.anchor);
   }

   boolean hasSelection() {
      return this.cursor != this.anchor;
   }

   int scroll() {
      return this.scrollX;
   }

   int updateScroll(int availableWidth, ToIntFunction<String> widths) {
      this.scrollX = TextFieldRenderer.scrollFor(this.text, this.cursor, availableWidth, widths, this.scrollX);
      return this.scrollX;
   }

   void commit() {
      this.sink.accept(this.text);
   }

   void revert() {
      this.sink.accept(this.initialText);
   }

   int indexAt(float x, float fieldX, InlineTextEditState.TextWidth widths) {
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

         i = Utf16TextCursor.next(this.text, i);
      }

      return bestIndex;
   }

   void moveCursor(int position, boolean extendSelection) {
      this.cursor = Utf16TextCursor.clampIndex(this.text, position);
      if (!extendSelection) {
         this.anchor = this.cursor;
      }
   }

   boolean keyPressed(int key, int modifiers) {
      boolean ctrl = (modifiers & 2) != 0;
      boolean shift = (modifiers & 1) != 0;
      if (ctrl) {
         switch (key) {
            case 65:
               this.anchor = 0;
               this.cursor = this.text.length();
               return true;
            case 67:
               this.copySelection();
               return true;
            case 86:
               this.paste();
               return true;
            case 88:
               this.copySelection();
               this.deleteSelection();
               return true;
            case 262:
               this.moveCursor(this.nextWordEnd(), shift);
               return true;
            case 263:
               this.moveCursor(this.previousWordStart(), shift);
               return true;
         }
      }

      switch (key) {
         case 257:
         case 335:
            this.commit();
            return true;
         case 259:
            if (!this.deleteSelection() && this.cursor > 0) {
               this.replaceRange(ctrl ? this.previousWordStart() : Utf16TextCursor.previous(this.text, this.cursor), this.cursor, "");
            }

            return true;
         case 261:
            if (!this.deleteSelection() && this.cursor < this.text.length()) {
               this.replaceRange(this.cursor, ctrl ? this.nextWordEnd() : Utf16TextCursor.next(this.text, this.cursor), "");
            }

            return true;
         case 262:
            if (this.hasSelection() && !shift) {
               this.moveCursor(this.selectionEnd(), false);
            } else {
               this.moveCursor(Utf16TextCursor.next(this.text, this.cursor), shift);
            }

            return true;
         case 263:
            if (this.hasSelection() && !shift) {
               this.moveCursor(this.selectionStart(), false);
            } else {
               this.moveCursor(Utf16TextCursor.previous(this.text, this.cursor), shift);
            }

            return true;
         case 268:
            this.moveCursor(0, shift);
            return true;
         case 269:
            this.moveCursor(this.text.length(), shift);
            return true;
         default:
            return false;
      }
   }

   void typeChar(char chr) {
      if (chr >= ' ' && chr != 127) {
         this.replaceRange(this.selectionStart(), this.selectionEnd(), Character.toString(chr));
      }
   }

   private boolean deleteSelection() {
      if (!this.hasSelection()) {
         return false;
      }

      this.replaceRange(this.selectionStart(), this.selectionEnd(), "");
      return true;
   }

   private void replaceRange(int start, int end, String insert) {
      start = Utf16TextCursor.clampIndex(this.text, start);
      end = Utf16TextCursor.nextBoundary(this.text, end);
      this.text = this.text.substring(0, start) + insert + this.text.substring(end);
      this.cursor = start + insert.length();
      this.anchor = this.cursor;
   }

   private void copySelection() {
      if (this.hasSelection()) {
         Minecraft.getInstance().keyboardHandler.setClipboard(this.text.substring(this.selectionStart(), this.selectionEnd()));
      }
   }

   private void paste() {
      String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
      if (clip != null && !clip.isEmpty()) {
         this.replaceRange(this.selectionStart(), this.selectionEnd(), stripControlChars(clip));
      }
   }

   private static String stripControlChars(String raw) {
      StringBuilder out = new StringBuilder(raw.length());

      for (int i = 0; i < raw.length(); i++) {
         char c = raw.charAt(i);
         if (c >= ' ' && c != 127) {
            out.append(c);
         }
      }

      return out.toString();
   }

   private int previousWordStart() {
      int i = this.cursor;

      while (i > 0 && !isWordChar(this.text.charAt(i - 1))) {
         i--;
      }

      while (i > 0 && isWordChar(this.text.charAt(i - 1))) {
         i--;
      }

      return i;
   }

   private int nextWordEnd() {
      int i = this.cursor;

      while (i < this.text.length() && isWordChar(this.text.charAt(i))) {
         i++;
      }

      while (i < this.text.length() && !isWordChar(this.text.charAt(i))) {
         i++;
      }

      return i;
   }

   private static boolean isWordChar(char c) {
      return Character.isLetterOrDigit(c) || c == '_' || c == '.' || c == '-';
   }

   interface TextSink {
      void accept(String var1);
   }

   interface TextWidth {
      float width(String var1);
   }
}
