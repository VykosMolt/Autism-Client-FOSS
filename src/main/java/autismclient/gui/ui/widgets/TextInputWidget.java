package autismclient.gui.ui.widgets;

import autismclient.compat263.CompatGLFW;
import autismclient.util.ThemePaletteManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;

public class TextInputWidget extends autismclient.gui.ui.layout.LayoutNode implements FocusableInput {
   private static final int DEFAULT_MAX_LENGTH = 512;
   private static final long MULTI_CLICK_WINDOW_MS = 320L;
   private static final float MULTI_CLICK_MAX_DISTANCE = 4.0F;
   private static TextInputWidget focusedWidget;
   private String value = "";
   private String placeholder = "";
   private int maxLength = 512;
   private float minWidth = 56.0F;
   private float maxWidth = 3.4028235E38F;
   private float preferredWidth = -1.0F;
   private int horizontalPadding = 4;
   private int fixedHeight = -1;
   private int textYOffset = 1;
   private boolean editable = true;
   private boolean drawBackground = true;
   private int cornerRadius = 3;
   private boolean hoverEffectsEnabled = true;
   private boolean focusEffectsEnabled = true;
   private int backgroundColorOverride = 0;
   private boolean focused = false;
   private int cursorIndex = 0;
   private int selectionAnchor = 0;
   private int horizontalScrollIndex = 0;
   private int verticalScrollLine = 0;
   private boolean draggingScrollbar = false;
   private int scrollbarDragOffsetY = 0;
   private boolean manualScrollPinned = false;
   private boolean draggingSelection = false;
   private boolean multiline = false;
   private long blinkResetMs = System.currentTimeMillis();
   private Identifier fontId = null;
   private l textTone = l.read;
   private l placeholderTone = l.write;
   private Predicate<String> inputFilter = next -> true;
   private Consumer<String> onChange;
   private Consumer<String> onSubmit;
   private boolean submitOnEnter;
   private Function<String, Component> displayTextProvider;
   private LongSupplier displayRevisionProvider;
   private boolean historyNavigationEnabled = false;
   private final List<String> historyEntries = new ArrayList<>();
   private Supplier<Collection<String>> historyProvider;
   private int historyIndex = -1;
   private String historyDraftValue = "";
   private boolean applyingHistoryEntry = false;
   private String cachedRichSource = "";
   private Function<String, Component> cachedRichProvider;
   private TextInputWidget.RichMetrics cachedRichMetrics;
   private boolean richCacheValid;
   private long cachedRichRevision = -9223372036854775808L;
   private long textVersion = 1L;
   private long styleVersion = 1L;
   private long prefixCacheTextVersion = -9223372036854775808L;
   private long prefixCacheStyleVersion = -9223372036854775808L;
   private Font prefixCacheFont;
   private Identifier prefixCacheFontId;
   private int prefixCacheGeneration = -2147483648;
   private int prefixCacheColor;
   private TextInputWidget.RichMetrics prefixCacheRichMetrics;
   private boolean prefixCacheUsesMsdf;
   private int[] prefixWidths = new int[]{0};
   private long wrapCacheTextVersion = -9223372036854775808L;
   private long wrapCacheStyleVersion = -9223372036854775808L;
   private Font wrapCacheFont;
   private Identifier wrapCacheFontId;
   private int wrapCacheGeneration = -2147483648;
   private int wrapCacheWidth = -2147483648;
   private boolean wrapCacheMultiline;
   private TextInputWidget.RichMetrics wrapCacheRichMetrics;
   private List<TextInputWidget.WrappedLine> cachedWrappedLines = List.of();
   private String placeholderWrapText = "";
   private Font placeholderWrapFont;
   private Identifier placeholderWrapFontId;
   private int placeholderWrapGeneration = -2147483648;
   private int placeholderWrapWidth = -2147483648;
   private List<String> cachedPlaceholderLines = List.of();
   private long lastClickMs = 0L;
   private float lastClickX = 0.0F / 0.0F;
   private float lastClickY = 0.0F / 0.0F;
   private int consecutiveClickCount = 0;

   public TextInputWidget() {
      this.nodeHeight = 16.0F;
   }

   public TextInputWidget setValue(String value) {
      String sanitized = this.sanitizeAndFilter(value);
      if (!Objects.equals(this.value, sanitized)) {
         this.value = sanitized;
         this.invalidateTextCaches();
         this.markLayoutDirty();
         if (this.cursorIndex > this.value.length()) {
            this.cursorIndex = this.value.length();
         }

         if (this.selectionAnchor > this.value.length()) {
            this.selectionAnchor = this.value.length();
         }

         this.manualScrollPinned = false;
         if (this.onChange != null) {
            this.onChange.accept(this.value);
         }
      }

      this.clampCursorAndSelection();
      this.horizontalScrollIndex = Math.min(this.horizontalScrollIndex, this.value.length());
      if (this.historyNavigationEnabled && !this.applyingHistoryEntry) {
         this.historyIndex = -1;
         this.historyDraftValue = this.value;
      }

      return this;
   }

   public String getValue() {
      return this.value;
   }

   public TextInputWidget setPlaceholder(String placeholder) {
      String next = placeholder == null ? "" : placeholder;
      if (!this.placeholder.equals(next)) {
         this.placeholder = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setMaxLength(int maxLength) {
      this.maxLength = Math.max(1, maxLength);
      this.setValue(this.value);
      return this;
   }

   public TextInputWidget setMinWidth(float minWidth) {
      float next = Math.max(0.0F, minWidth);
      if (Float.compare(this.minWidth, next) != 0) {
         this.minWidth = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setMaxWidth(float maxWidth) {
      float next = Math.max(this.minWidth, maxWidth);
      if (Float.compare(this.maxWidth, next) != 0) {
         this.maxWidth = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setPreferredWidth(float preferredWidth) {
      if (Float.compare(this.preferredWidth, preferredWidth) != 0) {
         this.preferredWidth = preferredWidth;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setHorizontalPadding(int horizontalPadding) {
      int next = Math.max(0, horizontalPadding);
      if (this.horizontalPadding != next) {
         this.horizontalPadding = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setFixedHeight(int fixedHeight) {
      int next = Math.max(1, fixedHeight);
      if (this.fixedHeight != next) {
         this.fixedHeight = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setTextYOffset(int textYOffset) {
      this.textYOffset = textYOffset;
      return this;
   }

   public TextInputWidget setEditable(boolean editable) {
      this.editable = editable;
      if (!editable) {
         this.setFocused(false);
      }

      return this;
   }

   public boolean isEditable() {
      return this.editable;
   }

   public TextInputWidget setDrawBackground(boolean drawBackground) {
      this.drawBackground = drawBackground;
      return this;
   }

   public TextInputWidget setCornerRadius(int radius) {
      this.cornerRadius = Math.max(0, radius);
      return this;
   }

   public TextInputWidget setHoverEffectsEnabled(boolean hoverEffectsEnabled) {
      this.hoverEffectsEnabled = hoverEffectsEnabled;
      return this;
   }

   public TextInputWidget setFocusEffectsEnabled(boolean focusEffectsEnabled) {
      this.focusEffectsEnabled = focusEffectsEnabled;
      return this;
   }

   public TextInputWidget setBackgroundColorOverride(int backgroundColorOverride) {
      this.backgroundColorOverride = backgroundColorOverride;
      return this;
   }

   public TextInputWidget setFontId(Identifier fontId) {
      if (!Objects.equals(this.fontId, fontId)) {
         this.fontId = fontId;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setTextTone(l textTone) {
      l next = textTone == null ? l.read : textTone;
      if (this.textTone != next) {
         this.textTone = next;
         this.styleVersion++;
         this.invalidateWrapCache();
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setPlaceholderTone(l placeholderTone) {
      l next = placeholderTone == null ? l.write : placeholderTone;
      if (this.placeholderTone != next) {
         this.placeholderTone = next;
         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setInputFilter(Predicate<String> filter) {
      this.inputFilter = filter == null ? next -> true : filter;
      return this;
   }

   public TextInputWidget setOnChange(Consumer<String> onChange) {
      this.onChange = onChange;
      return this;
   }

   public TextInputWidget setOnSubmit(Consumer<String> onSubmit) {
      this.onSubmit = onSubmit;
      return this;
   }

   public TextInputWidget setDisplayTextProvider(Function<String, Component> displayTextProvider) {
      if (this.displayTextProvider != displayTextProvider) {
         this.displayTextProvider = displayTextProvider;
         this.styleVersion++;
         this.invalidateRichCache();
         this.invalidateWrapCache();
      }

      return this;
   }

   public TextInputWidget setDisplayRevisionProvider(LongSupplier revisionProvider) {
      if (this.displayRevisionProvider != revisionProvider) {
         this.displayRevisionProvider = revisionProvider;
         this.styleVersion++;
         this.invalidateRichCache();
         this.invalidateWrapCache();
      }

      return this;
   }

   public TextInputWidget setMultiline(boolean multiline) {
      if (this.multiline != multiline) {
         this.multiline = multiline;
         if (multiline) {
            this.horizontalScrollIndex = 0;
         } else {
            this.verticalScrollLine = 0;
         }

         this.markLayoutDirty();
      }

      return this;
   }

   public TextInputWidget setSubmitOnEnter(boolean submitOnEnter) {
      this.submitOnEnter = submitOnEnter;
      return this;
   }

   public int measureWrappedLineCount(autismclient.gui.ui.layout.LayoutContext context, int fieldWidth) {
      if (this.value.isEmpty()) {
         return 1;
      }

      Identifier font = this.resolveFontId(context);
      int scaledHorizontalPadding = context.theme().load(this.horizontalPadding);
      int frameInset = context.theme().load(2);
      int innerW = this.computeInnerTextWidth(fieldWidth, scaledHorizontalPadding, frameInset);
      int bodyColor = context.theme().load(this.textTone);
      return Math.max(1, this.getWrappedLines(context.font(), font, bodyColor, innerW, this.resolveRichMetrics(this.value)).size());
   }

   public int heightForRows(autismclient.gui.ui.layout.LayoutContext context, int rows) {
      int lineHeight = context.theme().init_I_l(this.textTone) + context.theme().load(1);
      return Math.max(1, rows) * lineHeight + context.theme().load(6);
   }

   public TextInputWidget setHistoryNavigationEnabled(boolean historyNavigationEnabled) {
      this.historyNavigationEnabled = historyNavigationEnabled;
      if (!historyNavigationEnabled) {
         this.historyIndex = -1;
         this.historyDraftValue = "";
      }

      return this;
   }

   public TextInputWidget setHistoryProvider(Supplier<Collection<String>> historyProvider) {
      this.historyProvider = historyProvider;
      this.historyIndex = -1;
      this.historyDraftValue = "";
      return this;
   }

   public TextInputWidget pushHistoryEntry(String entry) {
      String sanitized = this.sanitizeAndFilter(entry);
      if (sanitized.isEmpty()) {
         return this;
      }

      if (!sanitized.equals(this.historyEntries.isEmpty() ? null : this.historyEntries.get(this.historyEntries.size() - 1))) {
         this.historyEntries.add(sanitized);

         while (this.historyEntries.size() > 100) {
            this.historyEntries.remove(0);
         }
      }

      this.historyIndex = -1;
      this.historyDraftValue = "";
      return this;
   }

   public TextInputWidget withGrowX(boolean growX) {
      super.setGrowX(growX);
      return this;
   }

   @Override
   public float measurePreferredWidth(autismclient.gui.ui.layout.LayoutContext context) {
      if (this.preferredWidth > 0.0F) {
         return UiLayoutMath.clamp(this.preferredWidth, this.minWidth, this.maxWidth);
      }

      String sample = this.value.isEmpty() ? this.placeholder : this.value;
      if (sample.isEmpty()) {
         sample = "Component";
      }

      Identifier font = this.resolveFontId(context);
      int color = context.theme().load(this.textTone);
      return UiLayoutMath.measurePaddedWidthFloat(context.font(), sample, font, color, context.theme().load(this.horizontalPadding + 2), this.minWidth, this.maxWidth);
   }

   @Override
   public float measureHeight(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
      int resolvedFixedHeight = this.fixedHeight > 0 ? this.fixedHeight : Math.max(context.theme().update(), context.theme().load(16));
      int contentHeight = context.theme().init_I_l(this.textTone) + context.theme().load(5);
      return Math.max(12, Math.max(resolvedFixedHeight, contentHeight));
   }

   @Override
   public void render(autismclient.gui.ui.layout.LayoutContext context) {
      if (this.nodeVisible) {
         int drawX = Math.round(this.nodeX);
         int drawY = Math.round(this.nodeY);
         int drawW = Math.round(this.nodeWidth);
         int drawH = Math.round(this.nodeHeight);
         boolean hovered = this.containsPoint(context.mouseX(), context.mouseY());
         float hover = this.stepHover(hovered, context.delta());
         Identifier font = this.resolveFontId(context);
         int fontHeight = context.theme().init_I_l(this.textTone);
         int scaledHorizontalPadding = context.theme().load(this.horizontalPadding);
         int textYOffsetPx = context.theme().open() + this.textYOffset - 1;
         int contentInset = context.theme().load(3);
         int frameInset = context.theme().load(2);
         int lineSpacing = context.theme().load(1);
         int bodyColor = context.theme().load(this.textTone);
         int placeholderColor = context.theme().load(this.placeholderTone);
         int bg = this.backgroundColorOverride != 0 ? this.backgroundColorOverride : (this.focused && this.focusEffectsEnabled ? context.theme().start() : context.theme().reset());
         int border = this.editable && this.focused && this.focusEffectsEnabled ? context.theme().run() : context.theme().set();
         int innerW = this.computeInnerTextWidth(drawW, scaledHorizontalPadding, frameInset);
         TextInputWidget.RichMetrics richMetrics = this.value.isEmpty() ? null : this.resolveRichMetrics(this.value);
         if (!this.multiline) {
            this.ensureHorizontalScrollForCursor(context.font(), font, bodyColor, innerW, richMetrics);
         }

         autismclient.gui.f.NetHelper fieldBounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
         int radius = this.cornerRadius;
         if (this.drawBackground) {
            autismclient.gui.f.PacketUtil.load(context.graphics(), fieldBounds, radius, context.applyAlpha(bg), context.applyAlpha(border));
         } else {
            autismclient.gui.f.PacketUtil.load(context.graphics(), fieldBounds, context.applyAlpha(border));
         }

         if (this.editable && this.hoverEffectsEnabled && hover > 0.0F && !this.focused) {
            int hoverTint = ThemePaletteManager.recolor((int)(hover * 14.0F) << 24 | 16723759, ThemePaletteManager.ColorRole.ACCENT);
            autismclient.gui.f.PacketUtil.load(context.graphics(), fieldBounds.init(1), Math.max(0, radius - 1), context.applyAlpha(hoverTint));
         }

         if (!this.editable) {
            autismclient.gui.f.PacketUtil.load(context.graphics(), fieldBounds.init(1), Math.max(0, radius - 1), context.applyAlpha(1712592152));
         } else if (this.focused && this.focusEffectsEnabled) {
            autismclient.gui.f.PacketUtil.load(
               context.graphics(),
               fieldBounds.init(1),
               Math.max(0, radius - 1),
               context.applyAlpha(ThemePaletteManager.recolor(318716731, ThemePaletteManager.ColorRole.ACCENT))
            );
         }

         int textX = drawX + scaledHorizontalPadding;
         if (this.value.isEmpty()) {
            if (this.multiline) {
               int textY = drawY + contentInset + textYOffsetPx;
               context.viewport().pushClip(context.graphics(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  for (String line : this.wrapPlaceholderLines(context.font(), font, placeholderColor, this.placeholder, innerW)) {
                     if (textY + fontHeight > drawY + drawH - frameInset) {
                        break;
                     }

                     k.init(context.graphics(), context.font(), line, font, context.applyAlpha(placeholderColor), textX, textY, false);
                     textY += fontHeight + lineSpacing;
                  }
               } finally {
                  context.viewport().popClip(context.graphics());
               }

               if (this.focused && this.isCaretBlinkVisible()) {
                  context.graphics()
                     .fill(
                        textX,
                        drawY + contentInset,
                        textX + 1,
                        drawY + contentInset + fontHeight,
                        context.applyAlpha(ThemePaletteManager.recolor(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            } else {
               int textY = UiLayoutMath.centerTextY(drawY, drawH, fontHeight, textYOffsetPx);
               String displayPlaceholder = k.init(context.font(), this.placeholder, innerW, font, placeholderColor);
               k.init(context.graphics(), context.font(), displayPlaceholder, font, context.applyAlpha(placeholderColor), textX, textY, false);
               if (this.focused && this.isCaretBlinkVisible()) {
                  context.graphics()
                     .fill(
                        textX,
                        drawY + contentInset,
                        textX + 1,
                        drawY + drawH - contentInset,
                        context.applyAlpha(ThemePaletteManager.recolor(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            }
         } else if (this.multiline) {
            List<TextInputWidget.WrappedLine> wrappedLines = this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
            int lineHeight = fontHeight + lineSpacing;
            int visibleLineCount = Math.max(1, Math.max(1, drawH - context.theme().load(6)) / lineHeight);
            this.ensureVerticalScrollForCursor(visibleLineCount, wrappedLines);
            int selectionStart = Math.min(this.cursorIndex, this.selectionAnchor);
            int selectionEnd = Math.max(this.cursorIndex, this.selectionAnchor);
            int startLine = Math.min(this.verticalScrollLine, Math.max(0, wrappedLines.size() - 1));
            int endLine = Math.min(wrappedLines.size(), startLine + visibleLineCount);
            context.viewport().pushClip(context.graphics(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

            try {
               int lineY = drawY + contentInset + textYOffsetPx;

               for (int lineIndex = startLine; lineIndex < endLine; lineIndex++) {
                  TextInputWidget.WrappedLine line = wrappedLines.get(lineIndex);
                  if (selectionStart != selectionEnd) {
                     int visibleSelectionStart = Math.max(selectionStart, line.start());
                     int visibleSelectionEnd = Math.min(selectionEnd, line.renderEnd());
                     if (visibleSelectionStart < visibleSelectionEnd) {
                        int selectionX = textX
                           + this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, line.start(), visibleSelectionStart);
                        int selectionW = this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, visibleSelectionStart, visibleSelectionEnd);
                        context.graphics()
                           .fill(
                              selectionX,
                              lineY - 1,
                              selectionX + selectionW,
                              lineY + fontHeight + lineSpacing,
                              context.applyAlpha(ThemePaletteManager.recolor(1728006730, ThemePaletteManager.ColorRole.ACCENT))
                           );
                     }
                  }

                  if (richMetrics != null) {
                     this.drawStyledSegment(
                        context.graphics(),
                        context.font(),
                        font,
                        bodyColor,
                        this.value,
                        richMetrics,
                        line.start(),
                        line.renderEnd(),
                        textX,
                        lineY
                     );
                  } else {
                     String lineComponent = this.value.substring(line.start(), line.renderEnd());
                     this.drawPlainSegment(context.graphics(), context.font(), lineComponent, font, context.applyAlpha(bodyColor), textX, lineY);
                  }

                  lineY += lineHeight;
               }
            } finally {
               context.viewport().popClip(context.graphics());
            }

            if (this.focused && this.isCaretBlinkVisible()) {
               int cursorLine = this.findLineIndexForCursor(wrappedLines, this.cursorIndex);
               if (cursorLine >= startLine && cursorLine < endLine) {
                  TextInputWidget.WrappedLine line = wrappedLines.get(cursorLine);
                  int visibleCursor = Math.min(this.cursorIndex, this.lineContentEnd(line));
                  int caretX = textX + this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, line.start(), visibleCursor);
                  int caretY = drawY + contentInset + textYOffsetPx + (cursorLine - startLine) * lineHeight;
                  context.graphics()
                     .fill(
                        caretX,
                        caretY - 1,
                        caretX + 1,
                        caretY + fontHeight + lineSpacing,
                        context.applyAlpha(ThemePaletteManager.recolor(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            }

            this.renderMultilineScrollbar(context, drawX, drawY, drawW, drawH, lineHeight, visibleLineCount, wrappedLines.size());
         } else {
            int textY = UiLayoutMath.centerTextY(drawY, drawH, fontHeight, textYOffsetPx);
            int visibleEnd = this.findVisibleEndIndex(context.font(), font, bodyColor, innerW, richMetrics);
            int selectionStart = Math.min(this.cursorIndex, this.selectionAnchor);
            int selectionEnd = Math.max(this.cursorIndex, this.selectionAnchor);
            if (selectionStart != selectionEnd) {
               int visibleSelectionStart = Math.max(selectionStart, this.horizontalScrollIndex);
               int visibleSelectionEnd = Math.min(selectionEnd, visibleEnd);
               if (visibleSelectionStart < visibleSelectionEnd) {
                  int selectionX = textX + this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, this.horizontalScrollIndex, visibleSelectionStart);
                  int selectionW = this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, visibleSelectionStart, visibleSelectionEnd);
                  context.graphics()
                     .fill(
                        selectionX,
                        drawY + 2,
                        selectionX + selectionW,
                        drawY + drawH - 2,
                        context.applyAlpha(ThemePaletteManager.recolor(1728006730, ThemePaletteManager.ColorRole.ACCENT))
                     );
               }
            }

            if (richMetrics != null) {
               context.viewport().pushClip(context.graphics(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  this.drawStyledSegment(
                     context.graphics(), context.font(), font, bodyColor, this.value, richMetrics, this.horizontalScrollIndex, visibleEnd, textX, textY
                  );
               } finally {
                  context.viewport().popClip(context.graphics());
               }
            } else {
               context.viewport().pushClip(context.graphics(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  this.drawPlainSegment(
                     context.graphics(),
                     context.font(),
                     this.value.substring(this.horizontalScrollIndex, visibleEnd),
                     font,
                     context.applyAlpha(bodyColor),
                     textX,
                     textY
                  );
               } finally {
                  context.viewport().popClip(context.graphics());
               }
            }

            if (this.focused && this.isCaretBlinkVisible()) {
               int caretX = textX + this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, this.horizontalScrollIndex, this.cursorIndex);
               context.graphics()
                  .fill(
                     caretX,
                     drawY + contentInset,
                     caretX + 1,
                     drawY + drawH - contentInset,
                     context.applyAlpha(ThemePaletteManager.recolor(-4627, ThemePaletteManager.ColorRole.TEXT))
                  );
            }
         }
      }
   }

   @Override
   public boolean mouseClicked(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
      if (button != 0 || !this.containsPoint(mouseX, mouseY)) {
         return false;
      }

      if (this.multiline && this.tryStartScrollbarDrag(context, mouseX, mouseY)) {
         this.setFocused(true);
         return true;
      }

      if (!this.editable) {
         return false;
      }

      this.setFocused(true);
      this.draggingSelection = true;
      int innerW = this.computeInnerTextWidth(Math.round(this.nodeWidth), context.theme().load(this.horizontalPadding), context.theme().load(2));
      Identifier font = this.resolveFontId(context);
      int bodyColor = context.theme().load(this.textTone);
      TextInputWidget.RichMetrics richMetrics = this.resolveRichMetrics(this.value);
      int clickCursor;
      if (this.multiline) {
         List<TextInputWidget.WrappedLine> wrappedLines = this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
         int lineHeight = context.theme().init_I_l(this.textTone) + context.theme().load(1);
         int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.nodeHeight) - context.theme().load(6)) / lineHeight);
         this.ensureVerticalScrollForCursor(visibleLineCount, wrappedLines);
         clickCursor = this.indexForMultilinePoint(
            context.font(),
            font,
            bodyColor,
            mouseX - (this.nodeX + context.theme().load(this.horizontalPadding)),
            mouseY - this.multilineTextOriginY(context),
            lineHeight,
            wrappedLines,
            richMetrics
         );
      } else {
         this.ensureHorizontalScrollForCursor(context.font(), font, bodyColor, innerW, richMetrics);
         clickCursor = this.indexForSingleLineX(
            context.font(), font, bodyColor, mouseX - (this.nodeX + context.theme().load(this.horizontalPadding)), richMetrics
         );
      }

      int clickCount = this.registerClickCount(mouseX, mouseY);
      long handle = Minecraft.getInstance().getWindow().handle();
      boolean extend = CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
      if (extend) {
         this.cursorIndex = clickCursor;
      } else if (clickCount >= 3) {
         this.selectParagraphAt(clickCursor);
      } else if (clickCount == 2) {
         this.selectWordAt(clickCursor);
      } else {
         this.cursorIndex = clickCursor;
         this.selectionAnchor = clickCursor;
      }

      this.manualScrollPinned = false;
      this.resetCaretBlink();
      return true;
   }

   @Override
   public boolean mouseReleased(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
      if (button == 0 && this.draggingScrollbar) {
         this.draggingScrollbar = false;
         this.scrollbarDragOffsetY = 0;
         return true;
      } else if (button == 0 && this.draggingSelection) {
         this.draggingSelection = false;
         return this.focused;
      } else {
         return false;
      }
   }

   @Override
   public boolean mouseDragged(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      if (button == 0 && this.draggingScrollbar) {
         this.updateScrollbarDrag(context, mouseY);
         return true;
      }

      if (this.editable && this.focused && this.draggingSelection && button == 0) {
         Identifier font = this.resolveFontId(context);
         int bodyColor = context.theme().load(this.textTone);
         int innerW = this.computeInnerTextWidth(Math.round(this.nodeWidth), context.theme().load(this.horizontalPadding), context.theme().load(2));
         TextInputWidget.RichMetrics richMetrics = this.resolveRichMetrics(this.value);
         if (this.multiline) {
            List<TextInputWidget.WrappedLine> wrappedLines = this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
            int lineHeight = context.theme().init_I_l(this.textTone) + context.theme().load(1);
            int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.nodeHeight) - context.theme().load(6)) / lineHeight);
            this.ensureVerticalScrollForCursor(visibleLineCount, wrappedLines);
            this.cursorIndex = this.indexForMultilinePoint(
               context.font(),
               font,
               bodyColor,
               mouseX - (this.nodeX + context.theme().load(this.horizontalPadding)),
               mouseY - this.multilineTextOriginY(context),
               lineHeight,
               wrappedLines,
               richMetrics
            );
         } else {
            this.ensureHorizontalScrollForCursor(context.font(), font, bodyColor, innerW, richMetrics);
            this.cursorIndex = this.indexForSingleLineX(
               context.font(), font, bodyColor, mouseX - (this.nodeX + context.theme().load(this.horizontalPadding)), richMetrics
            );
         }

         this.manualScrollPinned = false;
         this.resetCaretBlink();
         return true;
      } else {
         return false;
      }
   }

   private void renderMultilineScrollbar(
      autismclient.gui.ui.layout.LayoutContext context, int drawX, int drawY, int drawW, int drawH, int lineHeight, int visibleLineCount, int wrappedLineCount
   ) {
      int maxStart = Math.max(0, wrappedLineCount - visibleLineCount);
      if (maxStart > 0) {
         ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.compute(
            Math.max(lineHeight, wrappedLineCount * lineHeight),
            Math.max(lineHeight, visibleLineCount * lineHeight),
            drawX + drawW - 5,
            drawY + 2,
            3,
            Math.max(1, drawH - 4),
            this.verticalScrollLine * lineHeight
         );
         ScrollbarMetricsHelper.render(context.graphics(), metrics, metrics.containsTrack(context.mouseX(), context.mouseY()), this.draggingScrollbar);
      }
   }

   private boolean tryStartScrollbarDrag(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY) {
      TextInputWidget.ScrollbarInfo info = this.computeScrollbarInfo(context);
      if (info != null && info.metrics().isScrollable() && info.metrics().containsTrack(mouseX, mouseY)) {
         this.draggingScrollbar = true;
         this.scrollbarDragOffsetY = info.metrics().containsThumb(mouseX, mouseY)
            ? Math.max(0, Math.round(mouseY) - info.metrics().thumbY())
            : info.metrics().thumbHeight() / 2;
         this.updateScrollbarDrag(context, mouseY);
         return true;
      } else {
         return false;
      }
   }

   private void updateScrollbarDrag(autismclient.gui.ui.layout.LayoutContext context, float mouseY) {
      TextInputWidget.ScrollbarInfo info = this.computeScrollbarInfo(context);
      if (info != null && info.metrics().isScrollable()) {
         int pixels = ScrollbarMetricsHelper.scrollOffsetForMouseY(info.metrics(), mouseY, this.scrollbarDragOffsetY);
         this.verticalScrollLine = Math.max(0, Math.min(info.maxStartLine(), Math.round((float)pixels / Math.max(1, info.lineHeight()))));
         this.manualScrollPinned = true;
      }
   }

   private TextInputWidget.ScrollbarInfo computeScrollbarInfo(autismclient.gui.ui.layout.LayoutContext context) {
      if (!this.multiline) {
         return null;
      }

      Identifier font = this.resolveFontId(context);
      int bodyColor = context.theme().load(this.textTone);
      int innerW = this.computeInnerTextWidth(Math.round(this.nodeWidth), context.theme().load(this.horizontalPadding), context.theme().load(2));
      TextInputWidget.RichMetrics richMetrics = this.resolveRichMetrics(this.value);
      List<TextInputWidget.WrappedLine> wrappedLines = this.value.isEmpty()
         ? this.wrapPlaceholderAsSpans(context.font(), font, bodyColor, this.placeholder, innerW)
         : this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
      int lineHeight = context.theme().init_I_l(this.textTone) + context.theme().load(1);
      int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.nodeHeight) - context.theme().load(6)) / lineHeight);
      int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
      ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.compute(
         Math.max(lineHeight, wrappedLines.size() * lineHeight),
         Math.max(lineHeight, visibleLineCount * lineHeight),
         Math.round(this.nodeX + this.nodeWidth) - 5,
         Math.round(this.nodeY) + 2,
         3,
         Math.max(1, Math.round(this.nodeHeight) - 4),
         this.verticalScrollLine * lineHeight
      );
      return new TextInputWidget.ScrollbarInfo(metrics, lineHeight, maxStart);
   }

   private List<TextInputWidget.WrappedLine> wrapPlaceholderAsSpans(Font renderer, Identifier font, int color, String text, int maxWidth) {
      String source = text != null && !text.isEmpty() ? text : " ";
      List<TextInputWidget.WrappedLine> lines = new ArrayList<>();
      int offset = 0;

      for (String line : this.wrapPlaceholderLines(renderer, font, color, source, maxWidth)) {
         int end = Math.min(source.length(), offset + line.length());
         lines.add(new TextInputWidget.WrappedLine(offset, end, end));
         offset = Math.min(source.length(), end + 1);
      }

      if (lines.isEmpty()) {
         lines.add(new TextInputWidget.WrappedLine(0, 0, 0));
      }

      return lines;
   }

   @Override
   public boolean mouseScrolled(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, float amount) {
      if (this.multiline && this.containsPoint(mouseX, mouseY)) {
         Identifier font = this.resolveFontId(context);
         int bodyColor = context.theme().load(this.textTone);
         int innerW = this.computeInnerTextWidth(Math.round(this.nodeWidth), context.theme().load(this.horizontalPadding), context.theme().load(2));
         TextInputWidget.RichMetrics richMetrics = this.resolveRichMetrics(this.value);
         List<TextInputWidget.WrappedLine> wrappedLines = this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
         int lineHeight = context.theme().init_I_l(this.textTone) + context.theme().load(1);
         int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.nodeHeight) - context.theme().load(6)) / lineHeight);
         int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
         if (maxStart <= 0) {
            return false;
         }

         int step = Math.max(1, Math.round(Math.abs(amount) * 3.0F));
         int next = this.verticalScrollLine + (amount < 0.0F ? step : -step);
         next = Math.max(0, Math.min(maxStart, next));
         if (next == this.verticalScrollLine) {
            return false;
         }

         this.verticalScrollLine = next;
         this.manualScrollPinned = true;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean keyPressed(autismclient.gui.ui.layout.LayoutContext context, int keyCode, int scanCode, int modifiers) {
      if (this.focused && this.editable) {
         boolean ctrl = (modifiers & 10) != 0;
         boolean shift = (modifiers & 1) != 0;
         switch (keyCode) {
            case 65:
               if (ctrl) {
                  this.selectionAnchor = 0;
                  this.cursorIndex = this.value.length();
                  this.manualScrollPinned = false;
                  this.resetCaretBlink();
                  return true;
               }
               break;
            case 67:
               if (ctrl) {
                  this.copySelectionToClipboard();
                  return true;
               }
               break;
            case 86:
               if (ctrl) {
                  this.pasteFromClipboard();
                  return true;
               }
               break;
            case 88:
               if (ctrl) {
                  this.copySelectionToClipboard();
                  this.deleteSelection();
                  return true;
               }
               break;
            case 256:
               this.setFocused(false);
               return true;
            case 257:
            case 335:
               if (!this.multiline || this.submitOnEnter && !shift) {
                  if (this.onSubmit != null) {
                     this.onSubmit.accept(this.value);
                  }

                  return true;
               }

               this.replaceSelection("\n");
               return true;
            case 259:
               if (this.deleteSelection()) {
                  return true;
               }

               if (this.cursorIndex > 0) {
                  int from = ctrl ? this.findPreviousWordBoundary(this.cursorIndex) : Utf16TextCursor.previous(this.value, this.cursorIndex);
                  this.replaceRange(from, this.cursorIndex, "");
               }

               return true;
            case 261:
               if (this.deleteSelection()) {
                  return true;
               }

               if (this.cursorIndex < this.value.length()) {
                  int to = ctrl ? this.findNextWordBoundary(this.cursorIndex) : Utf16TextCursor.next(this.value, this.cursorIndex);
                  this.replaceRange(this.cursorIndex, to, "");
               }

               return true;
            case 262:
               if (ctrl) {
                  this.moveCursorTo(this.findNextWordBoundary(this.cursorIndex), shift);
               } else {
                  this.moveCursorByCodePoints(1, shift);
               }

               return true;
            case 263:
               if (ctrl) {
                  this.moveCursorTo(this.findPreviousWordBoundary(this.cursorIndex), shift);
               } else {
                  this.moveCursorByCodePoints(-1, shift);
               }

               return true;
            case 264:
               if (!this.multiline && this.historyNavigationEnabled) {
                  this.navigateHistory(1);
                  return true;
               }

               if (this.multiline) {
                  this.moveCursorVertically(context, 1, shift);
                  return true;
               }
               break;
            case 265:
               if (!this.multiline && this.historyNavigationEnabled) {
                  this.navigateHistory(-1);
                  return true;
               }

               if (this.multiline) {
                  this.moveCursorVertically(context, -1, shift);
                  return true;
               }
               break;
            case 268:
               if (!ctrl && this.multiline) {
                  this.moveCursorTo(this.findLineStart(this.cursorIndex), shift);
               } else {
                  this.moveCursorTo(0, shift);
               }

               return true;
            case 269:
               if (!ctrl && this.multiline) {
                  this.moveCursorTo(this.findLineEnd(this.cursorIndex), shift);
               } else {
                  this.moveCursorTo(this.value.length(), shift);
               }

               return true;
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(autismclient.gui.ui.layout.LayoutContext context, char chr, int modifiers) {
      if (this.focused && this.editable && this.isAllowedCodePoint(chr)) {
         this.replaceSelection(Character.toString(chr));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isFocused() {
      return this.focused;
   }

   @Override
   public void setFocused(boolean focused) {
      boolean nextFocused = focused && this.editable;
      if (!nextFocused || this.focused != nextFocused) {
         this.draggingSelection = false;
      }

      this.focused = nextFocused;
      if (nextFocused) {
         TextInputWidget previous = focusedWidget;
         if (previous != null && previous != this) {
            previous.setFocused(false);
         }

         focusedWidget = this;
      } else if (focusedWidget == this) {
         focusedWidget = null;
      }

      this.resetCaretBlink();
   }

   public static boolean hasAnyFocused() {
      TextInputWidget input = focusedWidget;
      return input != null && input.isFocused();
   }

   public static void clearGlobalFocus() {
      TextInputWidget input = focusedWidget;
      if (input != null) {
         input.setFocused(false);
      }

      focusedWidget = null;
   }

   public TextInputWidget setSelectionEnd(int selectionEnd) {
      this.cursorIndex = Utf16TextCursor.clampIndex(this.value, selectionEnd);
      this.manualScrollPinned = false;
      this.resetCaretBlink();
      return this;
   }

   public TextInputWidget moveCursorToEnd() {
      this.moveCursorTo(this.value.length(), false);
      return this;
   }

   public int getSelectionStart() {
      return Math.min(this.cursorIndex, this.selectionAnchor);
   }

   public int getSelectionEnd() {
      return Math.max(this.cursorIndex, this.selectionAnchor);
   }

   public String getSelectedText() {
      return this.value.substring(this.getSelectionStart(), this.getSelectionEnd());
   }

   public TextInputWidget insertText(String text) {
      this.replaceSelection(text == null ? "" : text);
      return this;
   }

   private Identifier resolveFontId(autismclient.gui.ui.layout.LayoutContext context) {
      return this.fontId != null ? this.fontId : context.theme().init_Identifier_l(l.read);
   }

   private boolean isCaretBlinkVisible() {
      return (System.currentTimeMillis() - this.blinkResetMs) / 530L % 2L == 0L;
   }

   private void resetCaretBlink() {
      this.blinkResetMs = System.currentTimeMillis();
   }

   private void clampCursorAndSelection() {
      this.cursorIndex = Utf16TextCursor.clampIndex(this.value, this.cursorIndex);
      this.selectionAnchor = Utf16TextCursor.clampIndex(this.value, this.selectionAnchor);
   }

   private int findVisibleEndIndex(Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.RichMetrics richMetrics) {
      this.ensurePrefixWidths(renderer, font, color, richMetrics);
      int start = Utf16TextCursor.clampIndex(this.value, this.horizontalScrollIndex);
      int target = this.prefixWidths[start] + Math.max(0, availableWidth);
      return Utf16TextCursor.clampIndex(this.value, upperBoundInclusive(this.prefixWidths, start, this.value.length(), target));
   }

   private void ensureHorizontalScrollForCursor(Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.RichMetrics richMetrics) {
      this.clampCursorAndSelection();
      this.ensurePrefixWidths(renderer, font, color, richMetrics);
      int safeCursor = Utf16TextCursor.clampIndex(this.value, this.cursorIndex);
      this.horizontalScrollIndex = Utf16TextCursor.clampIndex(this.value, this.horizontalScrollIndex);
      if (safeCursor < this.horizontalScrollIndex) {
         this.horizontalScrollIndex = safeCursor;
      }

      int target = this.prefixWidths[safeCursor] - Math.max(0, availableWidth);
      if (this.prefixWidths[this.horizontalScrollIndex] < target) {
         this.horizontalScrollIndex = Utf16TextCursor.load(this.value, lowerBound(this.prefixWidths, 0, safeCursor, target));
      }

      int maxOffsetWidth = Math.max(0, this.prefixWidths[this.value.length()] - Math.max(0, availableWidth));
      if (this.prefixWidths[this.horizontalScrollIndex] > maxOffsetWidth) {
         this.horizontalScrollIndex = Utf16TextCursor.clampIndex(this.value, lowerBound(this.prefixWidths, 0, this.horizontalScrollIndex, maxOffsetWidth));
      }

      if (this.horizontalScrollIndex == safeCursor && safeCursor > 0) {
         this.horizontalScrollIndex = Utf16TextCursor.previous(this.value, safeCursor);
      }
   }

   private int indexForSingleLineX(Font renderer, Identifier font, int color, float mouseInnerX, TextInputWidget.RichMetrics richMetrics) {
      this.ensurePrefixWidths(renderer, font, color, richMetrics);
      int start = Utf16TextCursor.clampIndex(this.value, this.horizontalScrollIndex);
      int target = Math.max(0, this.prefixWidths[start] + Math.round(mouseInnerX));
      int candidate = nearestWidthIndex(this.prefixWidths, 0, this.value.length(), target);
      return Utf16TextCursor.init(this.value, this.prefixWidths, candidate, target);
   }

   private List<String> wrapPlaceholderLines(Font renderer, Identifier font, int color, String text, int availableWidth) {
      if (text != null && !text.isEmpty()) {
         int safeWidth = Math.max(1, availableWidth);
         int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration();
         if (Objects.equals(text, this.placeholder)
            && Objects.equals(this.placeholderWrapText, text)
            && this.placeholderWrapFont == renderer
            && Objects.equals(this.placeholderWrapFontId, font)
            && this.placeholderWrapGeneration == reloadGeneration
            && this.placeholderWrapWidth == safeWidth) {
            return this.cachedPlaceholderLines;
         }

         List<String> wrapped = new ArrayList<>();

         for (TextInputWidget.WrappedLine line : this.computeWrappedLines(renderer, font, color, text, safeWidth, null)) {
            wrapped.add(text.substring(line.start(), line.renderEnd()));
         }

         List<String> result = wrapped.isEmpty() ? List.of("") : List.copyOf(wrapped);
         if (Objects.equals(text, this.placeholder)) {
            this.placeholderWrapText = text;
            this.placeholderWrapFont = renderer;
            this.placeholderWrapFontId = font;
            this.placeholderWrapGeneration = reloadGeneration;
            this.placeholderWrapWidth = safeWidth;
            this.cachedPlaceholderLines = result;
         }

         return result;
      } else {
         return List.of("");
      }
   }

   private TextInputWidget.RichMetrics resolveRichMetrics(String sourceValue) {
      if (this.displayTextProvider != null && sourceValue != null && !sourceValue.isEmpty()) {
         long providerRevision = this.queryDisplayRevision();
         if (this.richCacheValid && this.cachedRichProvider == this.displayTextProvider && this.cachedRichRevision == providerRevision && Objects.equals(this.cachedRichSource, sourceValue)) {
            return this.cachedRichMetrics;
         }

         Component richDisplay = this.displayTextProvider.apply(sourceValue);
         if (richDisplay == null) {
            this.storeRichCache(sourceValue, providerRevision, null);
            return null;
         }

         String richComponent = richDisplay.getString();
         if (!Objects.equals(richComponent, sourceValue)) {
            this.storeRichCache(sourceValue, providerRevision, null);
            return null;
         }

         List<Style> rawStyles = new ArrayList<>(richComponent.length());
         richDisplay.visit((style, part) -> {
            if (part != null && !part.isEmpty()) {
               Style safeStyle = style == null ? Style.EMPTY : style;

               for (int i = 0; i < part.length(); i++) {
                  rawStyles.add(safeStyle);
               }
            }

            return Optional.empty();
         }, Style.EMPTY);
         List<Style> charStyles = new ArrayList<>(rawStyles);
         if (charStyles.isEmpty()) {
            this.storeRichCache(sourceValue, providerRevision, null);
            return null;
         }

         while (charStyles.size() < richComponent.length()) {
            charStyles.add(Style.EMPTY);
         }

         if (charStyles.size() > richComponent.length()) {
            charStyles = new ArrayList<>(charStyles.subList(0, richComponent.length()));
         }

         TextInputWidget.RichMetrics metrics = new TextInputWidget.RichMetrics(richComponent, charStyles);
         this.storeRichCache(sourceValue, providerRevision, metrics);
         return metrics;
      } else {
         return null;
      }
   }

   private long queryDisplayRevision() {
      if (this.displayRevisionProvider == null) {
         return 0L;
      }

      try {
         return this.displayRevisionProvider.getAsLong();
      } catch (RuntimeException ignored) {
         return 0L;
      }
   }

   private void storeRichCache(String sourceValue, long providerRevision, TextInputWidget.RichMetrics metrics) {
      this.cachedRichSource = sourceValue == null ? "" : sourceValue;
      this.cachedRichProvider = this.displayTextProvider;
      this.cachedRichRevision = providerRevision;
      this.cachedRichMetrics = metrics;
      this.richCacheValid = true;
   }

   private void invalidateRichCache() {
      this.cachedRichSource = "";
      this.cachedRichProvider = null;
      this.cachedRichRevision = -9223372036854775808L;
      this.cachedRichMetrics = null;
      this.richCacheValid = false;
   }

   private void invalidateTextCaches() {
      this.textVersion++;
      this.invalidateRichCache();
      this.prefixCacheTextVersion = -9223372036854775808L;
      this.invalidateWrapCache();
   }

   private void invalidateWrapCache() {
      this.wrapCacheTextVersion = -9223372036854775808L;
      this.wrapCacheRichMetrics = null;
      this.cachedWrappedLines = List.of();
   }

   private void ensurePrefixWidths(Font renderer, Identifier font, int color, TextInputWidget.RichMetrics richMetrics) {
      int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration();
      if (this.prefixCacheTextVersion != this.textVersion
         || this.prefixCacheStyleVersion != this.styleVersion
         || this.prefixCacheFont != renderer
         || !Objects.equals(this.prefixCacheFontId, font)
         || this.prefixCacheGeneration != reloadGeneration
         || this.prefixCacheColor != color
         || this.prefixCacheRichMetrics != richMetrics
         || this.prefixWidths.length != this.value.length() + 1) {
         autismclient.gui.sdf.MsdfFontFace face = autismclient.gui.sdf.MsdfFontFace.forFont(font);
         this.prefixCacheUsesMsdf = richMetrics == null && autismclient.gui.sdf.MsdfTextRenderer.canRender(face, this.value.replace("\n", ""));
         this.prefixWidths = Utf16TextCursor.prefixWidths(
            this.value,
            (text, start, end) -> {
               String glyph = text.substring(start, end);
               if (glyph.equals("\n")) {
                  return 0.0F;
               }

               if (this.prefixCacheUsesMsdf) {
                  return autismclient.gui.sdf.MsdfTextRenderer.measureWidthFloat(face, glyph, autismclient.gui.sdf.MsdfTextRenderer.scaledFontSize(face));
               }

               if (richMetrics == null) {
                  return this.value.indexOf(167) >= 0
                     ? renderer.getSplitter().stringWidth(text.substring(0, end)) - renderer.getSplitter().stringWidth(text.substring(0, start))
                     : renderer.getSplitter().stringWidth(glyph);
               }

               Style style = richMetrics.charStyles().get(Math.min(start, richMetrics.charStyles().size() - 1));
               return renderer.getSplitter().stringWidth(Component.literal(glyph).setStyle(style == null ? Style.EMPTY : style));
            }
         );
         this.prefixCacheTextVersion = this.textVersion;
         this.prefixCacheStyleVersion = this.styleVersion;
         this.prefixCacheFont = renderer;
         this.prefixCacheFontId = font;
         this.prefixCacheGeneration = reloadGeneration;
         this.prefixCacheColor = color;
         this.prefixCacheRichMetrics = richMetrics;
      }
   }

   private void drawPlainSegment(GuiGraphicsExtractor graphics, Font renderer, String text, Identifier font, int color, int x, int y) {
      if (this.prefixCacheUsesMsdf) {
         k.init(graphics, renderer, text, font, color, x, y, false);
      } else {
         graphics.text(renderer, text, x, y, color, false);
      }
   }

   private Component buildStyledComponent(String sourceText, TextInputWidget.RichMetrics richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.clampIndex(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.clampIndex(sourceText, end));
      if (richMetrics != null && safeStart < safeEnd) {
         MutableComponent out = Component.empty();
         StringBuilder segment = new StringBuilder();
         Style currentStyle = null;

         for (int i = safeStart; i < safeEnd; i++) {
            Style style = richMetrics.charStyles().get(Math.min(i, richMetrics.charStyles().size() - 1));
            if (currentStyle == null) {
               currentStyle = style;
            } else if (!currentStyle.equals(style)) {
               out.append(Component.literal(segment.toString()).setStyle(currentStyle));
               segment.setLength(0);
               currentStyle = style;
            }

            segment.append(sourceText.charAt(i));
         }

         if (!segment.isEmpty()) {
            out.append(Component.literal(segment.toString()).setStyle(currentStyle == null ? Style.EMPTY : currentStyle));
         }

         return out;
      } else {
         return Component.literal(sourceText.substring(safeStart, safeEnd));
      }
   }

   private int measureRangeWidth(Font renderer, Identifier font, int color, String sourceText, TextInputWidget.RichMetrics richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.clampIndex(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.clampIndex(sourceText, end));
      if (safeStart >= safeEnd) {
         return 0;
      } else if (Objects.equals(sourceText, this.value)) {
         this.ensurePrefixWidths(renderer, font, color, richMetrics);
         return this.prefixWidths[safeEnd] - this.prefixWidths[safeStart];
      } else {
         return richMetrics == null
            ? this.measurePlainWidth(renderer, font, color, sourceText.substring(safeStart, safeEnd))
            : this.measureStyledRangeWidth(renderer, font, color, sourceText, richMetrics, safeStart, safeEnd);
      }
   }

   private int measureStyledRangeWidth(Font renderer, Identifier font, int fallbackColor, String sourceText, TextInputWidget.RichMetrics richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.clampIndex(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.clampIndex(sourceText, end));
      return renderer.width(this.buildStyledComponent(sourceText, richMetrics, safeStart, safeEnd));
   }

   private void drawStyledSegment(
      GuiGraphicsExtractor context,
      Font renderer,
      Identifier font,
      int fallbackColor,
      String sourceText,
      TextInputWidget.RichMetrics richMetrics,
      int start,
      int end,
      int x,
      int y
   ) {
      int safeStart = Utf16TextCursor.clampIndex(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.clampIndex(sourceText, end));
      if (safeStart < safeEnd) {
         context.text(renderer, this.buildStyledComponent(sourceText, richMetrics, safeStart, safeEnd), x, y, fallbackColor, false);
      }
   }

   private List<TextInputWidget.WrappedLine> getWrappedLines(
      Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.RichMetrics richMetrics
   ) {
      int safeWidth = Math.max(1, availableWidth);
      int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.cacheGeneration();
      if (this.wrapCacheTextVersion == this.textVersion
         && this.wrapCacheStyleVersion == this.styleVersion
         && this.wrapCacheFont == renderer
         && Objects.equals(this.wrapCacheFontId, font)
         && this.wrapCacheGeneration == reloadGeneration
         && this.wrapCacheWidth == safeWidth
         && this.wrapCacheMultiline == this.multiline
         && this.wrapCacheRichMetrics == richMetrics) {
         return this.cachedWrappedLines;
      }

      this.ensurePrefixWidths(renderer, font, color, richMetrics);
      List<TextInputWidget.WrappedLine> lines = this.computeWrappedLines(renderer, font, color, this.value, safeWidth, richMetrics);
      this.cachedWrappedLines = List.copyOf(lines);
      this.wrapCacheTextVersion = this.textVersion;
      this.wrapCacheStyleVersion = this.styleVersion;
      this.wrapCacheFont = renderer;
      this.wrapCacheFontId = font;
      this.wrapCacheGeneration = reloadGeneration;
      this.wrapCacheWidth = safeWidth;
      this.wrapCacheMultiline = this.multiline;
      this.wrapCacheRichMetrics = richMetrics;
      return this.cachedWrappedLines;
   }

   private List<TextInputWidget.WrappedLine> computeWrappedLines(
      Font renderer, Identifier font, int color, String text, int availableWidth, TextInputWidget.RichMetrics richMetrics
   ) {
      String safeComponent = text == null ? "" : text;
      List<TextInputWidget.WrappedLine> lines = new ArrayList<>();

      for (autismclient.gui.ui.TextWrapLayout.Inner_DataUtil line : autismclient.gui.ui.TextWrapLayout.wrap(
         safeComponent, availableWidth, (start, end) -> this.measureRangeWidth(renderer, font, color, safeComponent, richMetrics, start, end)
      )) {
         lines.add(new TextInputWidget.WrappedLine(line.init(), line.load(), line.read()));
      }

      return lines;
   }

   private void ensureVerticalScrollForCursor(int visibleLineCount, List<TextInputWidget.WrappedLine> wrappedLines) {
      if (wrappedLines.isEmpty()) {
         this.verticalScrollLine = 0;
      } else {
         int cursorLine = this.findLineIndexForCursor(wrappedLines, this.cursorIndex);
         int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
         if (this.manualScrollPinned) {
            this.verticalScrollLine = Math.max(0, Math.min(this.verticalScrollLine, maxStart));
         } else {
            if (cursorLine < this.verticalScrollLine) {
               this.verticalScrollLine = cursorLine;
            }

            if (cursorLine >= this.verticalScrollLine + visibleLineCount) {
               this.verticalScrollLine = cursorLine - visibleLineCount + 1;
            }

            this.verticalScrollLine = Math.max(0, Math.min(this.verticalScrollLine, maxStart));
         }
      }
   }

   private int findLineIndexForCursor(List<TextInputWidget.WrappedLine> wrappedLines, int index) {
      if (wrappedLines.isEmpty()) {
         return 0;
      }

      int clamped = Math.max(0, Math.min(index, this.value.length()));

      for (int i = 0; i < wrappedLines.size(); i++) {
         TextInputWidget.WrappedLine line = wrappedLines.get(i);
         if (clamped < line.end()) {
            return i;
         }

         if (clamped == line.end()) {
            if (i == wrappedLines.size() - 1) {
               return i;
            }

            if (clamped == line.start()) {
               return i;
            }
         }
      }

      return wrappedLines.size() - 1;
   }

   private float multilineTextOriginY(autismclient.gui.ui.layout.LayoutContext context) {
      return this.nodeY + context.theme().load(3) + context.theme().open() + this.textYOffset - 1.0F;
   }

   private int indexForMultilinePoint(
      Font renderer,
      Identifier font,
      int color,
      float mouseInnerX,
      float mouseInnerY,
      int lineHeight,
      List<TextInputWidget.WrappedLine> wrappedLines,
      TextInputWidget.RichMetrics richMetrics
   ) {
      if (wrappedLines.isEmpty()) {
         return 0;
      }

      int lineIndex = this.verticalScrollLine + Math.max(0, (int)Math.floor((double)mouseInnerY / Math.max(1, lineHeight)));
      lineIndex = Math.max(0, Math.min(lineIndex, wrappedLines.size() - 1));
      TextInputWidget.WrappedLine line = wrappedLines.get(lineIndex);
      return this.indexForLineX(renderer, font, color, mouseInnerX, line, richMetrics);
   }

   private int indexForLineX(
      Font renderer, Identifier font, int color, float mouseInnerX, TextInputWidget.WrappedLine line, TextInputWidget.RichMetrics richMetrics
   ) {
      this.ensurePrefixWidths(renderer, font, color, richMetrics);
      int contentEnd = this.lineContentEnd(line);
      int start = Math.max(0, Math.min(line.start(), this.value.length()));
      int visibleEnd = Math.max(start, Math.min(contentEnd, this.value.length()));
      int target = this.prefixWidths[start] + Math.max(0, Math.round(mouseInnerX));
      int candidate = nearestWidthIndex(this.prefixWidths, start, visibleEnd, target);
      int best = Utf16TextCursor.init(this.value, this.prefixWidths, candidate, target);
      return Math.max(line.start(), Math.min(best, contentEnd));
   }

   private int lineContentEnd(TextInputWidget.WrappedLine line) {
      int end = line.end();
      if (end > line.start() && end <= this.value.length() && this.value.charAt(end - 1) == '\n') {
         end--;
      }

      return end;
   }

   private void moveCursorVertically(autismclient.gui.ui.layout.LayoutContext context, int direction, boolean keepSelection) {
      Identifier font = this.resolveFontId(context);
      int bodyColor = context.theme().load(this.textTone);
      int innerW = this.computeInnerTextWidth(Math.round(this.nodeWidth), context.theme().load(this.horizontalPadding), context.theme().load(2));
      TextInputWidget.RichMetrics richMetrics = this.resolveRichMetrics(this.value);
      List<TextInputWidget.WrappedLine> wrappedLines = this.getWrappedLines(context.font(), font, bodyColor, innerW, richMetrics);
      if (!wrappedLines.isEmpty()) {
         int currentLineIndex = this.findLineIndexForCursor(wrappedLines, this.cursorIndex);
         int targetLineIndex = Math.max(0, Math.min(wrappedLines.size() - 1, currentLineIndex + direction));
         if (targetLineIndex != currentLineIndex) {
            TextInputWidget.WrappedLine currentLine = wrappedLines.get(currentLineIndex);
            int visibleCursor = Math.min(this.cursorIndex, this.lineContentEnd(currentLine));
            int cursorX = this.measureRangeWidth(context.font(), font, bodyColor, this.value, richMetrics, currentLine.start(), visibleCursor);
            TextInputWidget.WrappedLine targetLine = wrappedLines.get(targetLineIndex);
            int targetCursor = this.indexForLineX(context.font(), font, bodyColor, cursorX, targetLine, richMetrics);
            this.moveCursorTo(targetCursor, keepSelection);
         }
      }
   }

   private void moveCursorByCodePoints(int delta, boolean keepSelection) {
      int next = this.cursorIndex;
      if (delta < 0) {
         for (int i = 0; i < -delta; i++) {
            next = Utf16TextCursor.previous(this.value, next);
         }
      } else {
         for (int i = 0; i < delta; i++) {
            next = Utf16TextCursor.next(this.value, next);
         }
      }

      this.moveCursorTo(next, keepSelection);
   }

   private int computeInnerTextWidth(int totalWidth, int padding, int frameInset) {
      return Math.max(1, totalWidth - padding * 2 - frameInset - (this.multiline ? 5 : 0));
   }

   private int registerClickCount(float mouseX, float mouseY) {
      long now = System.currentTimeMillis();
      boolean closeEnough = !Float.isNaN(this.lastClickX) && Math.abs(mouseX - this.lastClickX) <= 4.0F && Math.abs(mouseY - this.lastClickY) <= 4.0F;
      if (now - this.lastClickMs <= 320L && closeEnough) {
         this.consecutiveClickCount++;
      } else {
         this.consecutiveClickCount = 1;
      }

      this.lastClickMs = now;
      this.lastClickX = mouseX;
      this.lastClickY = mouseY;
      return this.consecutiveClickCount;
   }

   private void selectWordAt(int index) {
      int clamped = Math.max(0, Math.min(index, this.value.length()));
      if (this.value.isEmpty()) {
         this.cursorIndex = this.selectionAnchor = 0;
      } else {
         int probe = clamped;
         if (probe == this.value.length() && probe > 0) {
            probe--;
         }

         if (probe < this.value.length() && !this.isWordChar(this.value.charAt(probe)) && probe > 0 && this.isWordChar(this.value.charAt(probe - 1))) {
            probe--;
         }

         int start = probe;
         int end;
         if (probe < this.value.length() && this.isWordChar(this.value.charAt(probe))) {
            while (start > 0 && this.isWordChar(this.value.charAt(start - 1))) {
               start--;
            }

            end = probe + 1;

            while (end < this.value.length() && this.isWordChar(this.value.charAt(end))) {
               end++;
            }
         } else {
            while (start > 0 && !this.isWordChar(this.value.charAt(start - 1)) && this.value.charAt(start - 1) != '\n') {
               start--;
            }

            end = Math.min(this.value.length(), probe + 1);

            while (end < this.value.length() && !this.isWordChar(this.value.charAt(end)) && this.value.charAt(end) != '\n') {
               end++;
            }
         }

         this.selectionAnchor = Utf16TextCursor.clampIndex(this.value, start);
         this.cursorIndex = Utf16TextCursor.load(this.value, Math.max(start, end));
      }
   }

   private void selectParagraphAt(int index) {
      int clamped = Math.max(0, Math.min(index, this.value.length()));
      this.selectionAnchor = Utf16TextCursor.clampIndex(this.value, this.findLineStart(clamped));
      this.cursorIndex = Utf16TextCursor.clampIndex(this.value, this.findLineEnd(clamped));
      if (this.cursorIndex < this.value.length() && this.value.charAt(this.cursorIndex) == '\n') {
         this.cursorIndex++;
      }
   }

   private int findPreviousWordBoundary(int index) {
      int i = Math.max(0, Math.min(index, this.value.length()));

      while (i > 0 && Character.isWhitespace(this.value.charAt(i - 1))) {
         i--;
      }

      if (i > 0 && this.isWordChar(this.value.charAt(i - 1))) {
         while (i > 0 && this.isWordChar(this.value.charAt(i - 1))) {
            i--;
         }
      } else {
         while (i > 0 && !Character.isWhitespace(this.value.charAt(i - 1)) && !this.isWordChar(this.value.charAt(i - 1))) {
            i--;
         }
      }

      return i;
   }

   private int findNextWordBoundary(int index) {
      int i = Math.max(0, Math.min(index, this.value.length()));

      while (i < this.value.length() && Character.isWhitespace(this.value.charAt(i))) {
         i++;
      }

      if (i < this.value.length() && this.isWordChar(this.value.charAt(i))) {
         while (i < this.value.length() && this.isWordChar(this.value.charAt(i))) {
            i++;
         }
      } else {
         while (i < this.value.length() && !Character.isWhitespace(this.value.charAt(i)) && !this.isWordChar(this.value.charAt(i))) {
            i++;
         }
      }

      return i;
   }

   private int findLineStart(int index) {
      int i = Math.max(0, Math.min(index, this.value.length()));

      while (i > 0 && this.value.charAt(i - 1) != '\n') {
         i--;
      }

      return i;
   }

   private int findLineEnd(int index) {
      int i = Math.max(0, Math.min(index, this.value.length()));

      while (i < this.value.length() && this.value.charAt(i) != '\n') {
         i++;
      }

      return i;
   }

   private boolean isWordChar(char chr) {
      return Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == ':' || chr == '.';
   }

   private void navigateHistory(int direction) {
      List<String> history = this.collectHistory();
      if (!history.isEmpty()) {
         if (this.historyIndex >= history.size()) {
            this.historyIndex = history.size() - 1;
         }

         if (direction < 0) {
            if (this.historyIndex == -1) {
               this.historyDraftValue = this.value;
               this.historyIndex = history.size() - 1;
            } else if (this.historyIndex > 0) {
               this.historyIndex--;
            }

            this.applyHistoryValue(history.get(this.historyIndex));
         } else {
            if (this.historyIndex == -1) {
               return;
            }

            if (this.historyIndex < history.size() - 1) {
               this.historyIndex++;
               this.applyHistoryValue(history.get(this.historyIndex));
            } else {
               this.historyIndex = -1;
               this.applyHistoryValue(this.historyDraftValue);
            }
         }
      }
   }

   private List<String> collectHistory() {
      List<String> out = new ArrayList<>();
      if (this.historyProvider != null) {
         Collection<String> provided = this.historyProvider.get();
         if (provided != null) {
            for (String entry : provided) {
               this.appendUniqueHistory(out, entry);
            }
         }
      }

      for (String entry : this.historyEntries) {
         this.appendUniqueHistory(out, entry);
      }

      return out;
   }

   private void appendUniqueHistory(List<String> out, String entry) {
      String sanitized = this.sanitizeAndFilter(entry);
      if (!sanitized.isEmpty() && (out.isEmpty() || !sanitized.equals(out.get(out.size() - 1)))) {
         out.add(sanitized);

         while (out.size() > 100) {
            out.remove(0);
         }
      }
   }

   private void applyHistoryValue(String nextValue) {
      this.applyingHistoryEntry = true;

      try {
         this.setValue(nextValue);
      } finally {
         this.applyingHistoryEntry = false;
      }

      this.moveCursorTo(this.value.length(), false);
   }

   private void moveCursorTo(int nextCursor, boolean keepSelection) {
      this.cursorIndex = Utf16TextCursor.clampIndex(this.value, nextCursor);
      if (!keepSelection) {
         this.selectionAnchor = this.cursorIndex;
      }

      this.manualScrollPinned = false;
      this.resetCaretBlink();
   }

   private boolean hasSelection() {
      return this.cursorIndex != this.selectionAnchor;
   }

   private void copySelectionToClipboard() {
      if (this.hasSelection()) {
         int start = Math.min(this.cursorIndex, this.selectionAnchor);
         int end = Math.max(this.cursorIndex, this.selectionAnchor);
         Minecraft.getInstance().keyboardHandler.setClipboard(this.value.substring(start, end));
      }
   }

   private void pasteFromClipboard() {
      String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
      if (clipboard != null && !clipboard.isEmpty()) {
         this.replaceSelection(clipboard);
      }
   }

   private boolean deleteSelection() {
      if (!this.hasSelection()) {
         return false;
      }

      int start = Math.min(this.cursorIndex, this.selectionAnchor);
      int end = Math.max(this.cursorIndex, this.selectionAnchor);
      this.replaceRange(start, end, "");
      return true;
   }

   private void replaceSelection(String replacement) {
      int start = Math.min(this.cursorIndex, this.selectionAnchor);
      int end = Math.max(this.cursorIndex, this.selectionAnchor);
      this.replaceRange(start, end, replacement);
   }

   private void replaceRange(int start, int end, String replacement) {
      int safeStart = Utf16TextCursor.clampIndex(this.value, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.load(this.value, end));
      int capacity = Math.max(0, this.maxLength - (this.value.length() - (safeEnd - safeStart)));
      String sanitizedReplacement = Utf16TextCursor.truncate(this.sanitizeInput(replacement), capacity);
      String next = this.value.substring(0, safeStart) + sanitizedReplacement + this.value.substring(safeEnd);
      if (this.inputFilter.test(next)) {
         this.value = next;
         this.invalidateTextCaches();
         this.cursorIndex = Utf16TextCursor.clampIndex(next, Math.min(next.length(), safeStart + sanitizedReplacement.length()));
         this.selectionAnchor = this.cursorIndex;
         this.manualScrollPinned = false;
         this.resetCaretBlink();
         if (this.historyNavigationEnabled && !this.applyingHistoryEntry) {
            this.historyIndex = -1;
            this.historyDraftValue = this.value;
         }

         if (this.onChange != null) {
            this.onChange.accept(this.value);
         }
      }
   }

   private String sanitizeAndFilter(String input) {
      String next = this.sanitizeInput(input);
      return this.inputFilter.test(next) ? next : this.value;
   }

   private String sanitizeInput(String input) {
      if (input != null && !input.isEmpty()) {
         StringBuilder sb = new StringBuilder(input.length());
         int i = 0;

         while (i < input.length() && sb.length() < this.maxLength) {
            int codePoint = input.codePointAt(i);
            int units = Character.charCount(codePoint);
            i += units;
            if (this.isAllowedCodePoint(codePoint) && sb.length() + units <= this.maxLength) {
               sb.appendCodePoint(codePoint);
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private boolean isAllowedCodePoint(int codePoint) {
      if (codePoint == 10) {
         return this.multiline;
      } else {
         return codePoint == 13 ? false : codePoint >= 32 && codePoint != 127;
      }
   }

   private int measurePlainWidth(Font renderer, Identifier font, int color, String text) {
      return k.init(renderer, text, font, color);
   }

   private static int lowerBound(int[] widths, int from, int to, int target) {
      int low = Math.max(0, from);
      int high = Math.max(low, Math.min(to, widths.length - 1));

      while (low < high) {
         int middle = low + (high - low >>> 1);
         if (widths[middle] < target) {
            low = middle + 1;
         } else {
            high = middle;
         }
      }

      return low;
   }

   private static int upperBoundInclusive(int[] widths, int from, int to, int target) {
      int low = Math.max(0, from);
      int high = Math.max(low, Math.min(to, widths.length - 1));
      int answer = low;

      while (low <= high) {
         int middle = low + (high - low >>> 1);
         if (widths[middle] <= target) {
            answer = middle;
            low = middle + 1;
         } else {
            high = middle - 1;
         }
      }

      return answer;
   }

   private static int nearestWidthIndex(int[] widths, int from, int to, int target) {
      if (target <= widths[from]) {
         return from;
      }

      if (target >= widths[to]) {
         return to;
      }

      int right = lowerBound(widths, from, to, target);
      if (right <= from) {
         return from;
      }

      int left = right - 1;
      return target - widths[left] < widths[right] - target ? left : right;
   }

   private record WrappedLine(int start, int end, int renderEnd) {
   }

   private record ScrollbarInfo(ScrollbarMetricsHelper.Metrics metrics, int lineHeight, int maxStartLine) {
   }

   private record RichMetrics(String text, List<Style> charStyles) {
   }
}
