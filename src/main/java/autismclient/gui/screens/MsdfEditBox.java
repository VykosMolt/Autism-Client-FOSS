package autismclient.gui.screens;

import autismclient.gui.ui.widgets.Utf16TextCursor;
import autismclient.mixin.accessor.AutismEditBoxAccessor;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.PreeditEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public class MsdfEditBox extends EditBox {
   private float caret;
   private boolean centered;
   private boolean load;
   private int cursor = -1;
   private String lastValue = "";
   private long blinkResetMs;

   public MsdfEditBox(Font font, int width, int height, Component narration) {
      super(font, width, height, narration);
   }

   public MsdfEditBox(Font font, int x, int y, int width, int height, Component narration) {
      super(font, x, y, width, height, narration);
   }

   public MsdfEditBox(Font font, int x, int y, int width, int height, @Nullable EditBox oldBox, Component narration) {
      super(font, x, y, width, height, oldBox, narration);
   }

   @Override
   public void setCentered(boolean centered) {
      this.centered = centered;
      super.setCentered(centered);
   }

   @Override
   public boolean preeditUpdated(@Nullable PreeditEvent event) {
      this.load = event != null;
      return super.preeditUpdated(event);
   }

   private boolean canUseMsdf() {
      AutismEditBoxAccessor acc = (AutismEditBoxAccessor)this;
      String value = this.getValue();
      return !this.load
         && acc.autism$formatters().isEmpty()
         && (value.isEmpty() || autismclient.gui.sdf.MsdfTextRenderer.canRender(autismclient.gui.sdf.MsdfFontFace.BODY, value));
   }

   private float textWidth(String text) {
      return autismclient.gui.sdf.MsdfTextRenderer.measureWidthFloat(
         autismclient.gui.sdf.MsdfFontFace.BODY,
         text,
         autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(autismclient.gui.sdf.MsdfFontFace.BODY)
      );
   }

   private void updateScroll() {
      String value = this.getValue();
      int cursor = Utf16TextCursor.clampIndex(value, this.getCursorPosition());
      float available = Math.max(1, this.getInnerWidth() - 1);
      float caret = this.textWidth(value.substring(0, cursor));
      float maxScroll = Math.max(0.0F, this.textWidth(value) - available);
      this.caret = Math.max(0.0F, Math.min(this.caret, maxScroll));
      if (caret < this.caret) {
         this.caret = caret;
      } else if (caret > this.caret + available) {
         this.caret = caret - available;
      }
   }

   private float textStartX() {
      float width = this.textWidth(this.getValue());
      return this.centered && width < this.getInnerWidth() ? this.getX() + (this.getWidth() - width) / 2.0F : this.getX() + (this.isBordered() ? 4 : 0);
   }

   private int cursorIndexAt(double mouseX) {
      this.updateScroll();
      String value = this.getValue();
      float target = (float)mouseX - this.textStartX() + this.caret;
      float previousWidth = 0.0F;
      int previous = 0;

      for (int next = Utf16TextCursor.next(value, 0); next > previous; next = Utf16TextCursor.next(value, previous)) {
         float nextWidth = this.textWidth(value.substring(0, next));
         if (target < (previousWidth + nextWidth) / 2.0F) {
            return previous;
         }

         previous = next;
         previousWidth = nextWidth;
      }

      return value.length();
   }

   @Override
   public void onClick(MouseButtonEvent event, boolean doubleClick) {
      if (!this.canUseMsdf()) {
         super.onClick(event, doubleClick);
      } else {
         int clicked = this.cursorIndexAt(event.x());
         if (doubleClick) {
            this.setCursorPosition(clicked);
            int start = this.getWordPosition(-1);
            int end = this.getWordPosition(1);
            this.moveCursorTo(start, false);
            this.moveCursorTo(end, true);
         } else {
            this.moveCursorTo(clicked, event.hasShiftDown());
         }

         this.blinkResetMs = Util.getMillis();
      }
   }

   @Override
   protected void onDrag(MouseButtonEvent event, double dx, double dy) {
      if (!this.canUseMsdf()) {
         super.onDrag(event, dx, dy);
      } else {
         this.moveCursorTo(this.cursorIndexAt(event.x()), true);
         this.blinkResetMs = Util.getMillis();
      }
   }

   @Override
   public int getScreenX(int charIndex) {
      if (!this.canUseMsdf()) {
         return super.getScreenX(charIndex);
      }

      if (charIndex > this.getValue().length()) {
         return this.getX();
      }

      this.updateScroll();
      int end = Utf16TextCursor.clampIndex(this.getValue(), charIndex);
      return Math.round(this.textStartX() - this.caret + this.textWidth(this.getValue().substring(0, end)));
   }

   @Override
   public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.isVisible()) {
         AutismEditBoxAccessor acc = (AutismEditBoxAccessor)this;
         if (!this.canUseMsdf()) {
            super.extractWidgetRenderState(graphics, mouseX, mouseY, delta);
         } else {
            String value = this.getValue();
            this.updateScroll();
            autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(graphics, MsdfEditBox.FontSource.font(), mouseX, mouseY);
            autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
            autismclient.gui.sdf.MsdfFontFace face = autismclient.gui.sdf.MsdfFontFace.BODY;
            float size = autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(face);
            if (this.isBordered()) {
               autismclient.gui.f.PacketUtil.load(
                  graphics,
                  autismclient.gui.f.NetHelper.init(this.getX(), this.getY(), this.getWidth(), this.getHeight()),
                  3,
                  this.isFocused() ? colors.inputSurfaceFocused : colors.inputSurface,
                  this.isActive() ? (this.isFocused() ? colors.accent : colors.borderControl) : colors.borderSubtle
               );
            }

            int cursor = Utf16TextCursor.clampIndex(value, acc.autism$cursorPos());
            int highlight = Utf16TextCursor.clampIndex(value, acc.autism$highlightPos());
            if (cursor != this.cursor || !value.equals(this.lastValue)) {
               this.cursor = cursor;
               this.lastValue = value;
               this.blinkResetMs = Util.getMillis();
            }

            int textColor = acc.autism$isEditable() ? colors.textPrimary : colors.textMuted;
            float drawX = this.textStartX() - this.caret;
            float textY = autismclient.gui.sdf.MsdfTextRenderer.centerVertically(face, this.getY(), this.getHeight(), size);
            float textTop = autismclient.gui.sdf.MsdfTextRenderer.alignToVanillaTop(face, textY, size);
            float cursorX = drawX + this.textWidth(value.substring(0, cursor));
            int padding = this.isBordered() ? 4 : 0;
            int clipLeft = this.getX() + padding;
            int clipRight = Math.max(clipLeft, this.getX() + this.getWidth() - padding);
            int selectionTop = this.getY() + Math.min(3, Math.max(0, this.getHeight() / 4));
            int selectionBottom = this.getY() + this.getHeight() - Math.min(3, Math.max(0, this.getHeight() / 4));
            graphics.enableScissor(clipLeft, this.getY(), clipRight, this.getY() + this.getHeight());

            try {
               if (this.isFocused() && highlight != cursor) {
                  float highlightX = drawX + this.textWidth(value.substring(0, highlight));
                  int left = Math.round(Math.min(cursorX, highlightX));
                  int right = Math.round(Math.max(cursorX, highlightX));
                  graphics.fill(left, selectionTop, Math.max(left + 1, right), selectionBottom, autismclient.gui.f.PacketUtil.init(colors.accent, 0.35F));
               }

               autismclient.gui.sdf.MsdfTextRenderer.draw(graphics, face, value, drawX, textTop, size, textColor, false);
               if (value.isEmpty() && acc.autism$hint() != null && !this.isFocused()) {
                  graphics.text(MsdfEditBox.FontSource.font(), acc.autism$hint(), Math.round(this.textStartX()), Math.round(textY), colors.textMuted);
               }

               if (cursor == value.length() && value.length() < acc.autism$maxLength() && acc.autism$suggestion() != null) {
                  context.init_PoolEntry().draw(graphics, acc.autism$suggestion(), Math.round(cursorX), Math.round(textY), colors.textMuted);
               }

               long blinkStart = Math.max(this.blinkResetMs, acc.autism$focusedTime());
               if (this.isFocused() && (Util.getMillis() - blinkStart) / 500L % 2L == 0L) {
                  int caret = Math.max(clipLeft, Math.min(clipRight - 1, Math.round(cursorX)));
                  graphics.fill(caret, selectionTop, caret + 1, selectionBottom, textColor | 0xFF000000);
               }
            } finally {
               graphics.disableScissor();
            }

            if (this.isHovered()) {
               graphics.requestCursor(acc.autism$isEditable() ? CursorTypes.IBEAM : CursorTypes.NOT_ALLOWED);
            }
         }
      }
   }

   private static final class FontSource {
      private static Font font() {
         return Minecraft.getInstance().font;
      }
   }
}
