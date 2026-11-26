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
   private static final int init_I = 512;
   private static final long init_J_2 = 320L;
   private static final float sync_F = 4.0F;
   private static TextInputWidget init_BitSetUtil;
   private String init_Str = "";
   private String load_Str = "";
   private int load_I = 512;
   private float flush_F = 56.0F;
   private float close_F = 3.4028235E38F;
   private float open_F = -1.0F;
   private int read_I = 4;
   private int write_I = -1;
   private int update_I = 1;
   private boolean write_Z = true;
   private boolean update_Z = true;
   private int sync_I = 3;
   private boolean sync_Z = true;
   private boolean flush_Z = true;
   private int flush_I = 0;
   private boolean close_Z = false;
   private int close_I = 0;
   private int open_I = 0;
   private int poll_I = 0;
   private int push_I = 0;
   private boolean open_Z = false;
   private int pop_I = 0;
   private boolean poll_Z = false;
   private boolean push_Z = false;
   private boolean pop_Z = false;
   private long load_J = System.currentTimeMillis();
   private Identifier init_Identifier = null;
   private l init_l = l.read;
   private l load_l = l.write;
   private Predicate<String> init_Predicate = next -> true;
   private Consumer<String> init_Consumer;
   private Consumer<String> load_Consumer;
   private boolean tick_Z;
   private Function<String, Component> init_Function;
   private LongSupplier init_LongSupplier;
   private boolean run_Z = false;
   private final List<String> init_List = new ArrayList<>();
   private Supplier<Collection<String>> init_Supplier;
   private int tick_I = -1;
   private String read_Str = "";
   private boolean get_Z = false;
   private String write_Str = "";
   private Function<String, Component> load_Function;
   private TextInputWidget.Inner_StreamUtil init_BitSetUtil_StreamU;
   private boolean set_Z;
   private long read_J = -9223372036854775808L;
   private long write_J = 1L;
   private long update_J = 1L;
   private long sync_J = -9223372036854775808L;
   private long flush_J = -9223372036854775808L;
   private Font init_Font;
   private Identifier load_Identifier;
   private int run_I = -2147483648;
   private int get_I;
   private TextInputWidget.Inner_StreamUtil load_BitSetUtil_StreamU;
   private boolean put_Z;
   private int[] init_IArr = new int[]{0};
   private long close_J = -9223372036854775808L;
   private long open_J = -9223372036854775808L;
   private Font load_Font;
   private Identifier read_Identifier;
   private int set_I = -2147483648;
   private int put_I = -2147483648;
   private boolean add_Z;
   private TextInputWidget.Inner_StreamUtil read_BitSetUtil_StreamU;
   private List<TextInputWidget.Inner_CachePool> load_List = List.of();
   private String update_Str = "";
   private Font read_Font;
   private Identifier write_Identifier;
   private int add_I = -2147483648;
   private int clear = -2147483648;
   private List<String> read_List = List.of();
   private long poll_J = 0L;
   private float poll_F = 0.0F / 0.0F;
   private float push_F = 0.0F / 0.0F;
   private int reset = 0;

   public TextInputWidget() {
      this.write = 16.0F;
   }

   public TextInputWidget init_BitSetUtil_Str(String value) {
      String sanitized = this.init_Str_Str(value);
      if (!Objects.equals(this.init_Str, sanitized)) {
         this.init_Str = sanitized;
         this.sync_V();
         this.load_V();
         if (this.close_I > this.init_Str.length()) {
            this.close_I = this.init_Str.length();
         }

         if (this.open_I > this.init_Str.length()) {
            this.open_I = this.init_Str.length();
         }

         this.poll_Z = false;
         if (this.init_Consumer != null) {
            this.init_Consumer.accept(this.init_Str);
         }
      }

      this.write_V();
      this.poll_I = Math.min(this.poll_I, this.init_Str.length());
      if (this.run_Z && !this.get_Z) {
         this.tick_I = -1;
         this.read_Str = this.init_Str;
      }

      return this;
   }

   public String init_Str() {
      return this.init_Str;
   }

   public TextInputWidget load_BitSetUtil_Str(String placeholder) {
      String next = placeholder == null ? "" : placeholder;
      if (!this.load_Str.equals(next)) {
         this.load_Str = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget init_BitSetUtil_I(int maxLength) {
      this.load_I = Math.max(1, maxLength);
      this.init_BitSetUtil_Str(this.init_Str);
      return this;
   }

   public TextInputWidget init_BitSetUtil_F(float minWidth) {
      float next = Math.max(0.0F, minWidth);
      if (Float.compare(this.flush_F, next) != 0) {
         this.flush_F = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget load_BitSetUtil_F(float maxWidth) {
      float next = Math.max(this.flush_F, maxWidth);
      if (Float.compare(this.close_F, next) != 0) {
         this.close_F = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget read_BitSetUtil_F(float preferredWidth) {
      if (Float.compare(this.open_F, preferredWidth) != 0) {
         this.open_F = preferredWidth;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget load_BitSetUtil_I(int horizontalPadding) {
      int next = Math.max(0, horizontalPadding);
      if (this.read_I != next) {
         this.read_I = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget read_BitSetUtil_I(int fixedHeight) {
      int next = Math.max(1, fixedHeight);
      if (this.write_I != next) {
         this.write_I = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget write_BitSetUtil(int textYOffset) {
      this.update_I = textYOffset;
      return this;
   }

   public TextInputWidget init_BitSetUtil_Z(boolean editable) {
      this.write_Z = editable;
      if (!editable) {
         this.init_V_Z(false);
      }

      return this;
   }

   public boolean init_Z() {
      return this.write_Z;
   }

   public TextInputWidget load_BitSetUtil_Z(boolean drawBackground) {
      this.update_Z = drawBackground;
      return this;
   }

   public TextInputWidget update(int radius) {
      this.sync_I = Math.max(0, radius);
      return this;
   }

   public TextInputWidget read_BitSetUtil_Z(boolean hoverEffectsEnabled) {
      this.sync_Z = hoverEffectsEnabled;
      return this;
   }

   public TextInputWidget write(boolean focusEffectsEnabled) {
      this.flush_Z = focusEffectsEnabled;
      return this;
   }

   public TextInputWidget sync(int backgroundColorOverride) {
      this.flush_I = backgroundColorOverride;
      return this;
   }

   public TextInputWidget init_BitSetUtil_Identifier(Identifier fontId) {
      if (!Objects.equals(this.init_Identifier, fontId)) {
         this.init_Identifier = fontId;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget init_BitSetUtil_l(l textTone) {
      l next = textTone == null ? l.read : textTone;
      if (this.init_l != next) {
         this.init_l = next;
         this.update_J++;
         this.flush_V();
         this.load_V();
      }

      return this;
   }

   public TextInputWidget load_BitSetUtil_l(l placeholderTone) {
      l next = placeholderTone == null ? l.write : placeholderTone;
      if (this.load_l != next) {
         this.load_l = next;
         this.load_V();
      }

      return this;
   }

   public TextInputWidget init_BitSetUtil_Predicate(Predicate<String> filter) {
      this.init_Predicate = filter == null ? next -> true : filter;
      return this;
   }

   public TextInputWidget init_BitSetUtil_Consumer(Consumer<String> onChange) {
      this.init_Consumer = onChange;
      return this;
   }

   public TextInputWidget load_BitSetUtil_Consumer(Consumer<String> onSubmit) {
      this.load_Consumer = onSubmit;
      return this;
   }

   public TextInputWidget init_BitSetUtil_Function(Function<String, Component> displayTextProvider) {
      if (this.init_Function != displayTextProvider) {
         this.init_Function = displayTextProvider;
         this.update_J++;
         this.update_V();
         this.flush_V();
      }

      return this;
   }

   public TextInputWidget init_BitSetUtil_LongSupplier(LongSupplier revisionProvider) {
      if (this.init_LongSupplier != revisionProvider) {
         this.init_LongSupplier = revisionProvider;
         this.update_J++;
         this.update_V();
         this.flush_V();
      }

      return this;
   }

   public TextInputWidget update(boolean multiline) {
      if (this.pop_Z != multiline) {
         this.pop_Z = multiline;
         if (multiline) {
            this.poll_I = 0;
         } else {
            this.push_I = 0;
         }

         this.load_V();
      }

      return this;
   }

   public TextInputWidget sync(boolean submitOnEnter) {
      this.tick_Z = submitOnEnter;
      return this;
   }

   public int init_I_SyncManager_I(autismclient.gui.ui.layout.LayoutContext context, int fieldWidth) {
      if (this.init_Str.isEmpty()) {
         return 1;
      }

      Identifier font = this.init_Identifier_SyncManager(context);
      int scaledHorizontalPadding = context.init_CharEncoder().load(this.read_I);
      int frameInset = context.init_CharEncoder().load(2);
      int innerW = this.init_I_I(fieldWidth, scaledHorizontalPadding, frameInset);
      int bodyColor = context.init_CharEncoder().load(this.init_l);
      return Math.max(1, this.init_List_Font(context.init_Font(), font, bodyColor, innerW, this.init_BitSetUtil_StreamU_Str(this.init_Str)).size());
   }

   public int load(autismclient.gui.ui.layout.LayoutContext context, int rows) {
      int lineHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(1);
      return Math.max(1, rows) * lineHeight + context.init_CharEncoder().load(6);
   }

   public TextInputWidget flush(boolean historyNavigationEnabled) {
      this.run_Z = historyNavigationEnabled;
      if (!historyNavigationEnabled) {
         this.tick_I = -1;
         this.read_Str = "";
      }

      return this;
   }

   public TextInputWidget init_BitSetUtil_Supplier(Supplier<Collection<String>> historyProvider) {
      this.init_Supplier = historyProvider;
      this.tick_I = -1;
      this.read_Str = "";
      return this;
   }

   public TextInputWidget read_BitSetUtil_Str(String entry) {
      String sanitized = this.init_Str_Str(entry);
      if (sanitized.isEmpty()) {
         return this;
      }

      if (!sanitized.equals(this.init_List.isEmpty() ? null : this.init_List.get(this.init_List.size() - 1))) {
         this.init_List.add(sanitized);

         while (this.init_List.size() > 100) {
            this.init_List.remove(0);
         }
      }

      this.tick_I = -1;
      this.read_Str = "";
      return this;
   }

   public TextInputWidget close(boolean growX) {
      super.init_TableIndex_Z(growX);
      return this;
   }

   @Override
   public float init_F_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
      if (this.open_F > 0.0F) {
         return UiLayoutMath.init_F_F(this.open_F, this.flush_F, this.close_F);
      }

      String sample = this.init_Str.isEmpty() ? this.load_Str : this.init_Str;
      if (sample.isEmpty()) {
         sample = "Component";
      }

      Identifier font = this.init_Identifier_SyncManager(context);
      int color = context.init_CharEncoder().load(this.init_l);
      return UiLayoutMath.init_F_Font(context.init_Font(), sample, font, color, context.init_CharEncoder().load(this.read_I + 2), this.flush_F, this.close_F);
   }

   @Override
   public float init_F_SyncManager_F(autismclient.gui.ui.layout.LayoutContext context, float availableWidth) {
      int resolvedFixedHeight = this.write_I > 0 ? this.write_I : Math.max(context.init_CharEncoder().update(), context.init_CharEncoder().load(16));
      int contentHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(5);
      return Math.max(12, Math.max(resolvedFixedHeight, contentHeight));
   }

   @Override
   public void init_V_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
      if (this.init_Z) {
         int drawX = Math.round(this.init_F);
         int drawY = Math.round(this.load_F);
         int drawW = Math.round(this.read_F);
         int drawH = Math.round(this.write);
         boolean hovered = this.init_Z_F_F(context.init_F(), context.load());
         float hover = this.init_F_Z_F(hovered, context.read());
         Identifier font = this.init_Identifier_SyncManager(context);
         int fontHeight = context.init_CharEncoder().init_I_l(this.init_l);
         int scaledHorizontalPadding = context.init_CharEncoder().load(this.read_I);
         int textYOffsetPx = context.init_CharEncoder().open() + this.update_I - 1;
         int contentInset = context.init_CharEncoder().load(3);
         int frameInset = context.init_CharEncoder().load(2);
         int lineSpacing = context.init_CharEncoder().load(1);
         int bodyColor = context.init_CharEncoder().load(this.init_l);
         int placeholderColor = context.init_CharEncoder().load(this.load_l);
         int bg = this.flush_I != 0 ? this.flush_I : (this.close_Z && this.flush_Z ? context.init_CharEncoder().start() : context.init_CharEncoder().reset());
         int border = this.write_Z && this.close_Z && this.flush_Z ? context.init_CharEncoder().run() : context.init_CharEncoder().set();
         int innerW = this.init_I_I(drawW, scaledHorizontalPadding, frameInset);
         TextInputWidget.Inner_StreamUtil richMetrics = this.init_Str.isEmpty() ? null : this.init_BitSetUtil_StreamU_Str(this.init_Str);
         if (!this.pop_Z) {
            this.init_V_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
         }

         autismclient.gui.f.NetHelper fieldBounds = autismclient.gui.f.NetHelper.init(drawX, drawY, drawW, drawH);
         int radius = this.sync_I;
         if (this.update_Z) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), fieldBounds, radius, context.init_I_I(bg), context.init_I_I(border));
         } else {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), fieldBounds, context.init_I_I(border));
         }

         if (this.write_Z && this.sync_Z && hover > 0.0F && !this.close_Z) {
            int hoverTint = ThemePaletteManager.init_I_I_ct_DataUtil((int)(hover * 14.0F) << 24 | 16723759, ThemePaletteManager.ColorRole.ACCENT);
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), fieldBounds.init(1), Math.max(0, radius - 1), context.init_I_I(hoverTint));
         }

         if (!this.write_Z) {
            autismclient.gui.f.PacketUtil.load(context.init_GuiGraphicsExtract(), fieldBounds.init(1), Math.max(0, radius - 1), context.init_I_I(1712592152));
         } else if (this.close_Z && this.flush_Z) {
            autismclient.gui.f.PacketUtil.load(
               context.init_GuiGraphicsExtract(),
               fieldBounds.init(1),
               Math.max(0, radius - 1),
               context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(318716731, ThemePaletteManager.ColorRole.ACCENT))
            );
         }

         int textX = drawX + scaledHorizontalPadding;
         if (this.init_Str.isEmpty()) {
            if (this.pop_Z) {
               int textY = drawY + contentInset + textYOffsetPx;
               context.init_RingBuffer().init(context.init_GuiGraphicsExtract(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  for (String line : this.load(context.init_Font(), font, placeholderColor, this.load_Str, innerW)) {
                     if (textY + fontHeight > drawY + drawH - frameInset) {
                        break;
                     }

                     k.init(context.init_GuiGraphicsExtract(), context.init_Font(), line, font, context.init_I_I(placeholderColor), textX, textY, false);
                     textY += fontHeight + lineSpacing;
                  }
               } finally {
                  context.init_RingBuffer().read(context.init_GuiGraphicsExtract());
               }

               if (this.close_Z && this.flush_Z()) {
                  context.init_GuiGraphicsExtract()
                     .fill(
                        textX,
                        drawY + contentInset,
                        textX + 1,
                        drawY + contentInset + fontHeight,
                        context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            } else {
               int textY = UiLayoutMath.init(drawY, drawH, fontHeight, textYOffsetPx);
               String displayPlaceholder = k.init(context.init_Font(), this.load_Str, innerW, font, placeholderColor);
               k.init(context.init_GuiGraphicsExtract(), context.init_Font(), displayPlaceholder, font, context.init_I_I(placeholderColor), textX, textY, false);
               if (this.close_Z && this.flush_Z()) {
                  context.init_GuiGraphicsExtract()
                     .fill(
                        textX,
                        drawY + contentInset,
                        textX + 1,
                        drawY + drawH - contentInset,
                        context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            }
         } else if (this.pop_Z) {
            List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
            int lineHeight = fontHeight + lineSpacing;
            int visibleLineCount = Math.max(1, Math.max(1, drawH - context.init_CharEncoder().load(6)) / lineHeight);
            this.init_V_I_List(visibleLineCount, wrappedLines);
            int selectionStart = Math.min(this.close_I, this.open_I);
            int selectionEnd = Math.max(this.close_I, this.open_I);
            int startLine = Math.min(this.push_I, Math.max(0, wrappedLines.size() - 1));
            int endLine = Math.min(wrappedLines.size(), startLine + visibleLineCount);
            context.init_RingBuffer().init(context.init_GuiGraphicsExtract(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

            try {
               int lineY = drawY + contentInset + textYOffsetPx;

               for (int lineIndex = startLine; lineIndex < endLine; lineIndex++) {
                  TextInputWidget.Inner_CachePool line = wrappedLines.get(lineIndex);
                  if (selectionStart != selectionEnd) {
                     int visibleSelectionStart = Math.max(selectionStart, line.init());
                     int visibleSelectionEnd = Math.min(selectionEnd, line.read());
                     if (visibleSelectionStart < visibleSelectionEnd) {
                        int selectionX = textX
                           + this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, line.init(), visibleSelectionStart);
                        int selectionW = this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, visibleSelectionStart, visibleSelectionEnd);
                        context.init_GuiGraphicsExtract()
                           .fill(
                              selectionX,
                              lineY - 1,
                              selectionX + selectionW,
                              lineY + fontHeight + lineSpacing,
                              context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(1728006730, ThemePaletteManager.ColorRole.ACCENT))
                           );
                     }
                  }

                  if (richMetrics != null) {
                     this.init(
                        context.init_GuiGraphicsExtract(),
                        context.init_Font(),
                        font,
                        bodyColor,
                        this.init_Str,
                        richMetrics,
                        line.init(),
                        line.read(),
                        textX,
                        lineY
                     );
                  } else {
                     String lineComponent = this.init_Str.substring(line.init(), line.read());
                     this.init(context.init_GuiGraphicsExtract(), context.init_Font(), lineComponent, font, context.init_I_I(bodyColor), textX, lineY);
                  }

                  lineY += lineHeight;
               }
            } finally {
               context.init_RingBuffer().read(context.init_GuiGraphicsExtract());
            }

            if (this.close_Z && this.flush_Z()) {
               int cursorLine = this.init_I_List_I(wrappedLines, this.close_I);
               if (cursorLine >= startLine && cursorLine < endLine) {
                  TextInputWidget.Inner_CachePool line = wrappedLines.get(cursorLine);
                  int visibleCursor = Math.min(this.close_I, this.init_I_BitSetUtil_CachePo(line));
                  int caretX = textX + this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, line.init(), visibleCursor);
                  int caretY = drawY + contentInset + textYOffsetPx + (cursorLine - startLine) * lineHeight;
                  context.init_GuiGraphicsExtract()
                     .fill(
                        caretX,
                        caretY - 1,
                        caretX + 1,
                        caretY + fontHeight + lineSpacing,
                        context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(-4627, ThemePaletteManager.ColorRole.TEXT))
                     );
               }
            }

            this.init_V_SyncManager(context, drawX, drawY, drawW, drawH, lineHeight, visibleLineCount, wrappedLines.size());
         } else {
            int textY = UiLayoutMath.init(drawY, drawH, fontHeight, textYOffsetPx);
            int visibleEnd = this.init_I_Font_2(context.init_Font(), font, bodyColor, innerW, richMetrics);
            int selectionStart = Math.min(this.close_I, this.open_I);
            int selectionEnd = Math.max(this.close_I, this.open_I);
            if (selectionStart != selectionEnd) {
               int visibleSelectionStart = Math.max(selectionStart, this.poll_I);
               int visibleSelectionEnd = Math.min(selectionEnd, visibleEnd);
               if (visibleSelectionStart < visibleSelectionEnd) {
                  int selectionX = textX + this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, this.poll_I, visibleSelectionStart);
                  int selectionW = this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, visibleSelectionStart, visibleSelectionEnd);
                  context.init_GuiGraphicsExtract()
                     .fill(
                        selectionX,
                        drawY + 2,
                        selectionX + selectionW,
                        drawY + drawH - 2,
                        context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(1728006730, ThemePaletteManager.ColorRole.ACCENT))
                     );
               }
            }

            if (richMetrics != null) {
               context.init_RingBuffer().init(context.init_GuiGraphicsExtract(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  this.init(
                     context.init_GuiGraphicsExtract(), context.init_Font(), font, bodyColor, this.init_Str, richMetrics, this.poll_I, visibleEnd, textX, textY
                  );
               } finally {
                  context.init_RingBuffer().read(context.init_GuiGraphicsExtract());
               }
            } else {
               context.init_RingBuffer().init(context.init_GuiGraphicsExtract(), drawX + 1, drawY + 1, drawX + drawW - 1, drawY + drawH - 1);

               try {
                  this.init(
                     context.init_GuiGraphicsExtract(),
                     context.init_Font(),
                     this.init_Str.substring(this.poll_I, visibleEnd),
                     font,
                     context.init_I_I(bodyColor),
                     textX,
                     textY
                  );
               } finally {
                  context.init_RingBuffer().read(context.init_GuiGraphicsExtract());
               }
            }

            if (this.close_Z && this.flush_Z()) {
               int caretX = textX + this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, this.poll_I, this.close_I);
               context.init_GuiGraphicsExtract()
                  .fill(
                     caretX,
                     drawY + contentInset,
                     caretX + 1,
                     drawY + drawH - contentInset,
                     context.init_I_I(ThemePaletteManager.init_I_I_ct_DataUtil(-4627, ThemePaletteManager.ColorRole.TEXT))
                  );
            }
         }
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
      if (button != 0 || !this.init_Z_F_F(mouseX, mouseY)) {
         return false;
      }

      if (this.pop_Z && this.init_Z_SyncManager(context, mouseX, mouseY)) {
         this.init_V_Z(true);
         return true;
      }

      if (!this.write_Z) {
         return false;
      }

      this.init_V_Z(true);
      this.push_Z = true;
      int innerW = this.init_I_I(Math.round(this.read_F), context.init_CharEncoder().load(this.read_I), context.init_CharEncoder().load(2));
      Identifier font = this.init_Identifier_SyncManager(context);
      int bodyColor = context.init_CharEncoder().load(this.init_l);
      TextInputWidget.Inner_StreamUtil richMetrics = this.init_BitSetUtil_StreamU_Str(this.init_Str);
      int clickCursor;
      if (this.pop_Z) {
         List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
         int lineHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(1);
         int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.write) - context.init_CharEncoder().load(6)) / lineHeight);
         this.init_V_I_List(visibleLineCount, wrappedLines);
         clickCursor = this.init_I_Font(
            context.init_Font(),
            font,
            bodyColor,
            mouseX - (this.init_F + context.init_CharEncoder().load(this.read_I)),
            mouseY - this.load_F_SyncManager(context),
            lineHeight,
            wrappedLines,
            richMetrics
         );
      } else {
         this.init_V_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
         clickCursor = this.init_I_Font(
            context.init_Font(), font, bodyColor, mouseX - (this.init_F + context.init_CharEncoder().load(this.read_I)), richMetrics
         );
      }

      int clickCount = this.init_I_F_F(mouseX, mouseY);
      long handle = Minecraft.getInstance().getWindow().handle();
      boolean extend = CompatGLFW.glfwGetKey(handle, 340) == 1 || CompatGLFW.glfwGetKey(handle, 344) == 1;
      if (extend) {
         this.close_I = clickCursor;
      } else if (clickCount >= 3) {
         this.load_V_I(clickCursor);
      } else if (clickCount == 2) {
         this.init_V_I(clickCursor);
      } else {
         this.close_I = clickCursor;
         this.open_I = clickCursor;
      }

      this.poll_Z = false;
      this.read_V();
      return true;
   }

   @Override
   public boolean load_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button) {
      if (button == 0 && this.open_Z) {
         this.open_Z = false;
         this.pop_I = 0;
         return true;
      } else if (button == 0 && this.push_Z) {
         this.push_Z = false;
         return this.close_Z;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, int button, float deltaX, float deltaY) {
      if (button == 0 && this.open_Z) {
         this.init_V_SyncManager_F(context, mouseY);
         return true;
      }

      if (this.write_Z && this.close_Z && this.push_Z && button == 0) {
         Identifier font = this.init_Identifier_SyncManager(context);
         int bodyColor = context.init_CharEncoder().load(this.init_l);
         int innerW = this.init_I_I(Math.round(this.read_F), context.init_CharEncoder().load(this.read_I), context.init_CharEncoder().load(2));
         TextInputWidget.Inner_StreamUtil richMetrics = this.init_BitSetUtil_StreamU_Str(this.init_Str);
         if (this.pop_Z) {
            List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
            int lineHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(1);
            int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.write) - context.init_CharEncoder().load(6)) / lineHeight);
            this.init_V_I_List(visibleLineCount, wrappedLines);
            this.close_I = this.init_I_Font(
               context.init_Font(),
               font,
               bodyColor,
               mouseX - (this.init_F + context.init_CharEncoder().load(this.read_I)),
               mouseY - this.load_F_SyncManager(context),
               lineHeight,
               wrappedLines,
               richMetrics
            );
         } else {
            this.init_V_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
            this.close_I = this.init_I_Font(
               context.init_Font(), font, bodyColor, mouseX - (this.init_F + context.init_CharEncoder().load(this.read_I)), richMetrics
            );
         }

         this.poll_Z = false;
         this.read_V();
         return true;
      } else {
         return false;
      }
   }

   private void init_V_SyncManager(
      autismclient.gui.ui.layout.LayoutContext context, int drawX, int drawY, int drawW, int drawH, int lineHeight, int visibleLineCount, int wrappedLineCount
   ) {
      int maxStart = Math.max(0, wrappedLineCount - visibleLineCount);
      if (maxStart > 0) {
         ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
            Math.max(lineHeight, wrappedLineCount * lineHeight),
            Math.max(lineHeight, visibleLineCount * lineHeight),
            drawX + drawW - 5,
            drawY + 2,
            3,
            Math.max(1, drawH - 4),
            this.push_I * lineHeight
         );
         ScrollbarMetricsHelper.init(context.init_GuiGraphicsExtract(), metrics, metrics.init(context.init_F(), context.load()), this.open_Z);
      }
   }

   private boolean init_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY) {
      TextInputWidget.Inner_DataUtil info = this.init_BitSetUtil_DataUti_SyncManager(context);
      if (info != null && info.init_PoolEntry_DataUtil().init_Z() && info.init_PoolEntry_DataUtil().init(mouseX, mouseY)) {
         this.open_Z = true;
         this.pop_I = info.init_PoolEntry_DataUtil().load(mouseX, mouseY)
            ? Math.max(0, Math.round(mouseY) - info.init_PoolEntry_DataUtil().update())
            : info.init_PoolEntry_DataUtil().sync() / 2;
         this.init_V_SyncManager_F(context, mouseY);
         return true;
      } else {
         return false;
      }
   }

   private void init_V_SyncManager_F(autismclient.gui.ui.layout.LayoutContext context, float mouseY) {
      TextInputWidget.Inner_DataUtil info = this.init_BitSetUtil_DataUti_SyncManager(context);
      if (info != null && info.init_PoolEntry_DataUtil().init_Z()) {
         int pixels = ScrollbarMetricsHelper.init(info.init_PoolEntry_DataUtil(), mouseY, this.pop_I);
         this.push_I = Math.max(0, Math.min(info.load(), Math.round((float)pixels / Math.max(1, info.init_I()))));
         this.poll_Z = true;
      }
   }

   private TextInputWidget.Inner_DataUtil init_BitSetUtil_DataUti_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
      if (!this.pop_Z) {
         return null;
      }

      Identifier font = this.init_Identifier_SyncManager(context);
      int bodyColor = context.init_CharEncoder().load(this.init_l);
      int innerW = this.init_I_I(Math.round(this.read_F), context.init_CharEncoder().load(this.read_I), context.init_CharEncoder().load(2));
      TextInputWidget.Inner_StreamUtil richMetrics = this.init_BitSetUtil_StreamU_Str(this.init_Str);
      List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_Str.isEmpty()
         ? this.init_List_Font_2(context.init_Font(), font, bodyColor, this.load_Str, innerW)
         : this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
      int lineHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(1);
      int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.write) - context.init_CharEncoder().load(6)) / lineHeight);
      int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
      ScrollbarMetricsHelper.Metrics metrics = ScrollbarMetricsHelper.init_PoolEntry_DataUtil_I_2(
         Math.max(lineHeight, wrappedLines.size() * lineHeight),
         Math.max(lineHeight, visibleLineCount * lineHeight),
         Math.round(this.init_F + this.read_F) - 5,
         Math.round(this.load_F) + 2,
         3,
         Math.max(1, Math.round(this.write) - 4),
         this.push_I * lineHeight
      );
      return new TextInputWidget.Inner_DataUtil(metrics, lineHeight, maxStart);
   }

   private List<TextInputWidget.Inner_CachePool> init_List_Font_2(Font renderer, Identifier font, int color, String text, int maxWidth) {
      String source = text != null && !text.isEmpty() ? text : " ";
      List<TextInputWidget.Inner_CachePool> lines = new ArrayList<>();
      int offset = 0;

      for (String line : this.load(renderer, font, color, source, maxWidth)) {
         int end = Math.min(source.length(), offset + line.length());
         lines.add(new TextInputWidget.Inner_CachePool(offset, end, end));
         offset = Math.min(source.length(), end + 1);
      }

      if (lines.isEmpty()) {
         lines.add(new TextInputWidget.Inner_CachePool(0, 0, 0));
      }

      return lines;
   }

   @Override
   public boolean init_Z_SyncManager(autismclient.gui.ui.layout.LayoutContext context, float mouseX, float mouseY, float amount) {
      if (this.pop_Z && this.init_Z_F_F(mouseX, mouseY)) {
         Identifier font = this.init_Identifier_SyncManager(context);
         int bodyColor = context.init_CharEncoder().load(this.init_l);
         int innerW = this.init_I_I(Math.round(this.read_F), context.init_CharEncoder().load(this.read_I), context.init_CharEncoder().load(2));
         TextInputWidget.Inner_StreamUtil richMetrics = this.init_BitSetUtil_StreamU_Str(this.init_Str);
         List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
         int lineHeight = context.init_CharEncoder().init_I_l(this.init_l) + context.init_CharEncoder().load(1);
         int visibleLineCount = Math.max(1, Math.max(1, Math.round(this.write) - context.init_CharEncoder().load(6)) / lineHeight);
         int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
         if (maxStart <= 0) {
            return false;
         }

         int step = Math.max(1, Math.round(Math.abs(amount) * 3.0F));
         int next = this.push_I + (amount < 0.0F ? step : -step);
         next = Math.max(0, Math.min(maxStart, next));
         if (next == this.push_I) {
            return false;
         }

         this.push_I = next;
         this.poll_Z = true;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_SyncManager_3(autismclient.gui.ui.layout.LayoutContext context, int keyCode, int scanCode, int modifiers) {
      if (this.close_Z && this.write_Z) {
         boolean ctrl = (modifiers & 10) != 0;
         boolean shift = (modifiers & 1) != 0;
         switch (keyCode) {
            case 65:
               if (ctrl) {
                  this.open_I = 0;
                  this.close_I = this.init_Str.length();
                  this.poll_Z = false;
                  this.read_V();
                  return true;
               }
               break;
            case 67:
               if (ctrl) {
                  this.close_V();
                  return true;
               }
               break;
            case 86:
               if (ctrl) {
                  this.open_V();
                  return true;
               }
               break;
            case 88:
               if (ctrl) {
                  this.close_V();
                  this.open_Z();
                  return true;
               }
               break;
            case 256:
               this.init_V_Z(false);
               return true;
            case 257:
            case 335:
               if (!this.pop_Z || this.tick_Z && !shift) {
                  if (this.load_Consumer != null) {
                     this.load_Consumer.accept(this.init_Str);
                  }

                  return true;
               }

               this.load_V_Str("\n");
               return true;
            case 259:
               if (this.open_Z()) {
                  return true;
               }

               if (this.close_I > 0) {
                  int from = ctrl ? this.init_I_I(this.close_I) : Utf16TextCursor.read(this.init_Str, this.close_I);
                  this.init_V_I(from, this.close_I, "");
               }

               return true;
            case 261:
               if (this.open_Z()) {
                  return true;
               }

               if (this.close_I < this.init_Str.length()) {
                  int to = ctrl ? this.load_I_I(this.close_I) : Utf16TextCursor.write(this.init_Str, this.close_I);
                  this.init_V_I(this.close_I, to, "");
               }

               return true;
            case 262:
               if (ctrl) {
                  this.load(this.load_I_I(this.close_I), shift);
               } else {
                  this.init_V_I_Z(1, shift);
               }

               return true;
            case 263:
               if (ctrl) {
                  this.load(this.init_I_I(this.close_I), shift);
               } else {
                  this.init_V_I_Z(-1, shift);
               }

               return true;
            case 264:
               if (!this.pop_Z && this.run_Z) {
                  this.read_V_I(1);
                  return true;
               }

               if (this.pop_Z) {
                  this.init_V_SyncManager(context, 1, shift);
                  return true;
               }
               break;
            case 265:
               if (!this.pop_Z && this.run_Z) {
                  this.read_V_I(-1);
                  return true;
               }

               if (this.pop_Z) {
                  this.init_V_SyncManager(context, -1, shift);
                  return true;
               }
               break;
            case 268:
               if (!ctrl && this.pop_Z) {
                  this.load(this.read_I_I(this.close_I), shift);
               } else {
                  this.load(0, shift);
               }

               return true;
            case 269:
               if (!ctrl && this.pop_Z) {
                  this.load(this.write_I(this.close_I), shift);
               } else {
                  this.load(this.init_Str.length(), shift);
               }

               return true;
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean init_Z_SyncManager_2(autismclient.gui.ui.layout.LayoutContext context, char chr, int modifiers) {
      if (this.close_Z && this.write_Z && this.init_Z_I(chr)) {
         this.load_V_Str(Character.toString(chr));
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean load_Z() {
      return this.close_Z;
   }

   @Override
   public void init_V_Z(boolean focused) {
      boolean nextFocused = focused && this.write_Z;
      if (!nextFocused || this.close_Z != nextFocused) {
         this.push_Z = false;
      }

      this.close_Z = nextFocused;
      if (nextFocused) {
         TextInputWidget previous = init_BitSetUtil;
         if (previous != null && previous != this) {
            previous.init_V_Z(false);
         }

         init_BitSetUtil = this;
      } else if (init_BitSetUtil == this) {
         init_BitSetUtil = null;
      }

      this.read_V();
   }

   public static boolean read_Z() {
      TextInputWidget input = init_BitSetUtil;
      return input != null && input.load_Z();
   }

   public static void init_V() {
      TextInputWidget input = init_BitSetUtil;
      if (input != null) {
         input.init_V_Z(false);
      }

      init_BitSetUtil = null;
   }

   public TextInputWidget flush(int selectionEnd) {
      this.close_I = Utf16TextCursor.init_I(this.init_Str, selectionEnd);
      this.poll_Z = false;
      this.read_V();
      return this;
   }

   public TextInputWidget init_BitSetUtil() {
      this.load(this.init_Str.length(), false);
      return this;
   }

   public int init_I() {
      return Math.min(this.close_I, this.open_I);
   }

   public int load_I() {
      return Math.max(this.close_I, this.open_I);
   }

   public String load_Str() {
      return this.init_Str.substring(this.init_I(), this.load_I());
   }

   public TextInputWidget write(String text) {
      this.load_V_Str(text == null ? "" : text);
      return this;
   }

   private Identifier init_Identifier_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
      return this.init_Identifier != null ? this.init_Identifier : context.init_CharEncoder().init_Identifier_l(l.read);
   }

   private boolean flush_Z() {
      return (System.currentTimeMillis() - this.load_J) / 530L % 2L == 0L;
   }

   private void read_V() {
      this.load_J = System.currentTimeMillis();
   }

   private void write_V() {
      this.close_I = Utf16TextCursor.init_I(this.init_Str, this.close_I);
      this.open_I = Utf16TextCursor.init_I(this.init_Str, this.open_I);
   }

   private int init_I_Font_2(Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.Inner_StreamUtil richMetrics) {
      this.init_V_Font(renderer, font, color, richMetrics);
      int start = Utf16TextCursor.init_I(this.init_Str, this.poll_I);
      int target = this.init_IArr[start] + Math.max(0, availableWidth);
      return Utf16TextCursor.init_I(this.init_Str, load_I_IArr(this.init_IArr, start, this.init_Str.length(), target));
   }

   private void init_V_Font(Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.Inner_StreamUtil richMetrics) {
      this.write_V();
      this.init_V_Font(renderer, font, color, richMetrics);
      int safeCursor = Utf16TextCursor.init_I(this.init_Str, this.close_I);
      this.poll_I = Utf16TextCursor.init_I(this.init_Str, this.poll_I);
      if (safeCursor < this.poll_I) {
         this.poll_I = safeCursor;
      }

      int target = this.init_IArr[safeCursor] - Math.max(0, availableWidth);
      if (this.init_IArr[this.poll_I] < target) {
         this.poll_I = Utf16TextCursor.load(this.init_Str, init_I_IArr(this.init_IArr, 0, safeCursor, target));
      }

      int maxOffsetWidth = Math.max(0, this.init_IArr[this.init_Str.length()] - Math.max(0, availableWidth));
      if (this.init_IArr[this.poll_I] > maxOffsetWidth) {
         this.poll_I = Utf16TextCursor.init_I(this.init_Str, init_I_IArr(this.init_IArr, 0, this.poll_I, maxOffsetWidth));
      }

      if (this.poll_I == safeCursor && safeCursor > 0) {
         this.poll_I = Utf16TextCursor.read(this.init_Str, safeCursor);
      }
   }

   private int init_I_Font(Font renderer, Identifier font, int color, float mouseInnerX, TextInputWidget.Inner_StreamUtil richMetrics) {
      this.init_V_Font(renderer, font, color, richMetrics);
      int start = Utf16TextCursor.init_I(this.init_Str, this.poll_I);
      int target = Math.max(0, this.init_IArr[start] + Math.round(mouseInnerX));
      int candidate = read(this.init_IArr, 0, this.init_Str.length(), target);
      return Utf16TextCursor.init(this.init_Str, this.init_IArr, candidate, target);
   }

   private List<String> load(Font renderer, Identifier font, int color, String text, int availableWidth) {
      if (text != null && !text.isEmpty()) {
         int safeWidth = Math.max(1, availableWidth);
         int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.init_I();
         if (Objects.equals(text, this.load_Str)
            && Objects.equals(this.update_Str, text)
            && this.read_Font == renderer
            && Objects.equals(this.write_Identifier, font)
            && this.add_I == reloadGeneration
            && this.clear == safeWidth) {
            return this.read_List;
         }

         List<String> wrapped = new ArrayList<>();

         for (TextInputWidget.Inner_CachePool line : this.init_List_Font(renderer, font, color, text, safeWidth, null)) {
            wrapped.add(text.substring(line.init(), line.read()));
         }

         List<String> result = wrapped.isEmpty() ? List.of("") : List.copyOf(wrapped);
         if (Objects.equals(text, this.load_Str)) {
            this.update_Str = text;
            this.read_Font = renderer;
            this.write_Identifier = font;
            this.add_I = reloadGeneration;
            this.clear = safeWidth;
            this.read_List = result;
         }

         return result;
      } else {
         return List.of("");
      }
   }

   private TextInputWidget.Inner_StreamUtil init_BitSetUtil_StreamU_Str(String sourceValue) {
      if (this.init_Function != null && sourceValue != null && !sourceValue.isEmpty()) {
         long providerRevision = this.load_J();
         if (this.set_Z && this.load_Function == this.init_Function && this.read_J == providerRevision && Objects.equals(this.write_Str, sourceValue)) {
            return this.init_BitSetUtil_StreamU;
         }

         Component richDisplay = this.init_Function.apply(sourceValue);
         if (richDisplay == null) {
            this.init_V_Str(sourceValue, providerRevision, null);
            return null;
         }

         String richComponent = richDisplay.getString();
         if (!Objects.equals(richComponent, sourceValue)) {
            this.init_V_Str(sourceValue, providerRevision, null);
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
            this.init_V_Str(sourceValue, providerRevision, null);
            return null;
         }

         while (charStyles.size() < richComponent.length()) {
            charStyles.add(Style.EMPTY);
         }

         if (charStyles.size() > richComponent.length()) {
            charStyles = new ArrayList<>(charStyles.subList(0, richComponent.length()));
         }

         TextInputWidget.Inner_StreamUtil metrics = new TextInputWidget.Inner_StreamUtil(richComponent, charStyles);
         this.init_V_Str(sourceValue, providerRevision, metrics);
         return metrics;
      } else {
         return null;
      }
   }

   private long load_J() {
      if (this.init_LongSupplier == null) {
         return 0L;
      }

      try {
         return this.init_LongSupplier.getAsLong();
      } catch (RuntimeException ignored) {
         return 0L;
      }
   }

   private void init_V_Str(String sourceValue, long providerRevision, TextInputWidget.Inner_StreamUtil metrics) {
      this.write_Str = sourceValue == null ? "" : sourceValue;
      this.load_Function = this.init_Function;
      this.read_J = providerRevision;
      this.init_BitSetUtil_StreamU = metrics;
      this.set_Z = true;
   }

   private void update_V() {
      this.write_Str = "";
      this.load_Function = null;
      this.read_J = -9223372036854775808L;
      this.init_BitSetUtil_StreamU = null;
      this.set_Z = false;
   }

   private void sync_V() {
      this.write_J++;
      this.update_V();
      this.sync_J = -9223372036854775808L;
      this.flush_V();
   }

   private void flush_V() {
      this.close_J = -9223372036854775808L;
      this.read_BitSetUtil_StreamU = null;
      this.load_List = List.of();
   }

   private void init_V_Font(Font renderer, Identifier font, int color, TextInputWidget.Inner_StreamUtil richMetrics) {
      int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.init_I();
      if (this.sync_J != this.write_J
         || this.flush_J != this.update_J
         || this.init_Font != renderer
         || !Objects.equals(this.load_Identifier, font)
         || this.run_I != reloadGeneration
         || this.get_I != color
         || this.load_BitSetUtil_StreamU != richMetrics
         || this.init_IArr.length != this.init_Str.length() + 1) {
         autismclient.gui.sdf.MsdfFontFace face = autismclient.gui.sdf.MsdfFontFace.init(font);
         this.put_Z = richMetrics == null && autismclient.gui.sdf.MsdfTextRenderer.init(face, this.init_Str.replace("\n", ""));
         this.init_IArr = Utf16TextCursor.init(
            this.init_Str,
            (text, start, end) -> {
               String glyph = text.substring(start, end);
               if (glyph.equals("\n")) {
                  return 0.0F;
               }

               if (this.put_Z) {
                  return autismclient.gui.sdf.MsdfTextRenderer.init_F(face, glyph, autismclient.gui.sdf.MsdfTextRenderer.init_F_ConfigStore(face));
               }

               if (richMetrics == null) {
                  return this.init_Str.indexOf(167) >= 0
                     ? renderer.getSplitter().stringWidth(text.substring(0, end)) - renderer.getSplitter().stringWidth(text.substring(0, start))
                     : renderer.getSplitter().stringWidth(glyph);
               }

               Style style = richMetrics.init_List().get(Math.min(start, richMetrics.init_List().size() - 1));
               return renderer.getSplitter().stringWidth(Component.literal(glyph).setStyle(style == null ? Style.EMPTY : style));
            }
         );
         this.sync_J = this.write_J;
         this.flush_J = this.update_J;
         this.init_Font = renderer;
         this.load_Identifier = font;
         this.run_I = reloadGeneration;
         this.get_I = color;
         this.load_BitSetUtil_StreamU = richMetrics;
      }
   }

   private void init(GuiGraphicsExtractor graphics, Font renderer, String text, Identifier font, int color, int x, int y) {
      if (this.put_Z) {
         k.init(graphics, renderer, text, font, color, x, y, false);
      } else {
         graphics.text(renderer, text, x, y, color, false);
      }
   }

   private Component init_Component_Str(String sourceText, TextInputWidget.Inner_StreamUtil richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.init_I(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.init_I(sourceText, end));
      if (richMetrics != null && safeStart < safeEnd) {
         MutableComponent out = Component.empty();
         StringBuilder segment = new StringBuilder();
         Style currentStyle = null;

         for (int i = safeStart; i < safeEnd; i++) {
            Style style = richMetrics.init_List().get(Math.min(i, richMetrics.init_List().size() - 1));
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

   private int init(Font renderer, Identifier font, int color, String sourceText, TextInputWidget.Inner_StreamUtil richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.init_I(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.init_I(sourceText, end));
      if (safeStart >= safeEnd) {
         return 0;
      } else if (Objects.equals(sourceText, this.init_Str)) {
         this.init_V_Font(renderer, font, color, richMetrics);
         return this.init_IArr[safeEnd] - this.init_IArr[safeStart];
      } else {
         return richMetrics == null
            ? this.init_I_Font(renderer, font, color, sourceText.substring(safeStart, safeEnd))
            : this.load(renderer, font, color, sourceText, richMetrics, safeStart, safeEnd);
      }
   }

   private int load(Font renderer, Identifier font, int fallbackColor, String sourceText, TextInputWidget.Inner_StreamUtil richMetrics, int start, int end) {
      int safeStart = Utf16TextCursor.init_I(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.init_I(sourceText, end));
      return renderer.width(this.init_Component_Str(sourceText, richMetrics, safeStart, safeEnd));
   }

   private void init(
      GuiGraphicsExtractor context,
      Font renderer,
      Identifier font,
      int fallbackColor,
      String sourceText,
      TextInputWidget.Inner_StreamUtil richMetrics,
      int start,
      int end,
      int x,
      int y
   ) {
      int safeStart = Utf16TextCursor.init_I(sourceText, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.init_I(sourceText, end));
      if (safeStart < safeEnd) {
         context.text(renderer, this.init_Component_Str(sourceText, richMetrics, safeStart, safeEnd), x, y, fallbackColor, false);
      }
   }

   private List<TextInputWidget.Inner_CachePool> init_List_Font(
      Font renderer, Identifier font, int color, int availableWidth, TextInputWidget.Inner_StreamUtil richMetrics
   ) {
      int safeWidth = Math.max(1, availableWidth);
      int reloadGeneration = autismclient.gui.sdf.MsdfTextRenderer.init_I();
      if (this.close_J == this.write_J
         && this.open_J == this.update_J
         && this.load_Font == renderer
         && Objects.equals(this.read_Identifier, font)
         && this.set_I == reloadGeneration
         && this.put_I == safeWidth
         && this.add_Z == this.pop_Z
         && this.read_BitSetUtil_StreamU == richMetrics) {
         return this.load_List;
      }

      this.init_V_Font(renderer, font, color, richMetrics);
      List<TextInputWidget.Inner_CachePool> lines = this.init_List_Font(renderer, font, color, this.init_Str, safeWidth, richMetrics);
      this.load_List = List.copyOf(lines);
      this.close_J = this.write_J;
      this.open_J = this.update_J;
      this.load_Font = renderer;
      this.read_Identifier = font;
      this.set_I = reloadGeneration;
      this.put_I = safeWidth;
      this.add_Z = this.pop_Z;
      this.read_BitSetUtil_StreamU = richMetrics;
      return this.load_List;
   }

   private List<TextInputWidget.Inner_CachePool> init_List_Font(
      Font renderer, Identifier font, int color, String text, int availableWidth, TextInputWidget.Inner_StreamUtil richMetrics
   ) {
      String safeComponent = text == null ? "" : text;
      List<TextInputWidget.Inner_CachePool> lines = new ArrayList<>();

      for (autismclient.gui.ui.TextWrapLayout.Inner_DataUtil line : autismclient.gui.ui.TextWrapLayout.init(
         safeComponent, availableWidth, (start, end) -> this.init(renderer, font, color, safeComponent, richMetrics, start, end)
      )) {
         lines.add(new TextInputWidget.Inner_CachePool(line.init(), line.load(), line.read()));
      }

      return lines;
   }

   private void init_V_I_List(int visibleLineCount, List<TextInputWidget.Inner_CachePool> wrappedLines) {
      if (wrappedLines.isEmpty()) {
         this.push_I = 0;
      } else {
         int cursorLine = this.init_I_List_I(wrappedLines, this.close_I);
         int maxStart = Math.max(0, wrappedLines.size() - visibleLineCount);
         if (this.poll_Z) {
            this.push_I = Math.max(0, Math.min(this.push_I, maxStart));
         } else {
            if (cursorLine < this.push_I) {
               this.push_I = cursorLine;
            }

            if (cursorLine >= this.push_I + visibleLineCount) {
               this.push_I = cursorLine - visibleLineCount + 1;
            }

            this.push_I = Math.max(0, Math.min(this.push_I, maxStart));
         }
      }
   }

   private int init_I_List_I(List<TextInputWidget.Inner_CachePool> wrappedLines, int index) {
      if (wrappedLines.isEmpty()) {
         return 0;
      }

      int clamped = Math.max(0, Math.min(index, this.init_Str.length()));

      for (int i = 0; i < wrappedLines.size(); i++) {
         TextInputWidget.Inner_CachePool line = wrappedLines.get(i);
         if (clamped < line.load()) {
            return i;
         }

         if (clamped == line.load()) {
            if (i == wrappedLines.size() - 1) {
               return i;
            }

            if (clamped == line.init()) {
               return i;
            }
         }
      }

      return wrappedLines.size() - 1;
   }

   private float load_F_SyncManager(autismclient.gui.ui.layout.LayoutContext context) {
      return this.load_F + context.init_CharEncoder().load(3) + context.init_CharEncoder().open() + this.update_I - 1.0F;
   }

   private int init_I_Font(
      Font renderer,
      Identifier font,
      int color,
      float mouseInnerX,
      float mouseInnerY,
      int lineHeight,
      List<TextInputWidget.Inner_CachePool> wrappedLines,
      TextInputWidget.Inner_StreamUtil richMetrics
   ) {
      if (wrappedLines.isEmpty()) {
         return 0;
      }

      int lineIndex = this.push_I + Math.max(0, (int)Math.floor((double)mouseInnerY / Math.max(1, lineHeight)));
      lineIndex = Math.max(0, Math.min(lineIndex, wrappedLines.size() - 1));
      TextInputWidget.Inner_CachePool line = wrappedLines.get(lineIndex);
      return this.init_I_Font(renderer, font, color, mouseInnerX, line, richMetrics);
   }

   private int init_I_Font(
      Font renderer, Identifier font, int color, float mouseInnerX, TextInputWidget.Inner_CachePool line, TextInputWidget.Inner_StreamUtil richMetrics
   ) {
      this.init_V_Font(renderer, font, color, richMetrics);
      int contentEnd = this.init_I_BitSetUtil_CachePo(line);
      int start = Math.max(0, Math.min(line.init(), this.init_Str.length()));
      int visibleEnd = Math.max(start, Math.min(contentEnd, this.init_Str.length()));
      int target = this.init_IArr[start] + Math.max(0, Math.round(mouseInnerX));
      int candidate = read(this.init_IArr, start, visibleEnd, target);
      int best = Utf16TextCursor.init(this.init_Str, this.init_IArr, candidate, target);
      return Math.max(line.init(), Math.min(best, contentEnd));
   }

   private int init_I_BitSetUtil_CachePo(TextInputWidget.Inner_CachePool line) {
      int end = line.load();
      if (end > line.init() && end <= this.init_Str.length() && this.init_Str.charAt(end - 1) == '\n') {
         end--;
      }

      return end;
   }

   private void init_V_SyncManager(autismclient.gui.ui.layout.LayoutContext context, int direction, boolean keepSelection) {
      Identifier font = this.init_Identifier_SyncManager(context);
      int bodyColor = context.init_CharEncoder().load(this.init_l);
      int innerW = this.init_I_I(Math.round(this.read_F), context.init_CharEncoder().load(this.read_I), context.init_CharEncoder().load(2));
      TextInputWidget.Inner_StreamUtil richMetrics = this.init_BitSetUtil_StreamU_Str(this.init_Str);
      List<TextInputWidget.Inner_CachePool> wrappedLines = this.init_List_Font(context.init_Font(), font, bodyColor, innerW, richMetrics);
      if (!wrappedLines.isEmpty()) {
         int currentLineIndex = this.init_I_List_I(wrappedLines, this.close_I);
         int targetLineIndex = Math.max(0, Math.min(wrappedLines.size() - 1, currentLineIndex + direction));
         if (targetLineIndex != currentLineIndex) {
            TextInputWidget.Inner_CachePool currentLine = wrappedLines.get(currentLineIndex);
            int visibleCursor = Math.min(this.close_I, this.init_I_BitSetUtil_CachePo(currentLine));
            int cursorX = this.init(context.init_Font(), font, bodyColor, this.init_Str, richMetrics, currentLine.init(), visibleCursor);
            TextInputWidget.Inner_CachePool targetLine = wrappedLines.get(targetLineIndex);
            int targetCursor = this.init_I_Font(context.init_Font(), font, bodyColor, cursorX, targetLine, richMetrics);
            this.load(targetCursor, keepSelection);
         }
      }
   }

   private void init_V_I_Z(int delta, boolean keepSelection) {
      int next = this.close_I;
      if (delta < 0) {
         for (int i = 0; i < -delta; i++) {
            next = Utf16TextCursor.read(this.init_Str, next);
         }
      } else {
         for (int i = 0; i < delta; i++) {
            next = Utf16TextCursor.write(this.init_Str, next);
         }
      }

      this.load(next, keepSelection);
   }

   private int init_I_I(int totalWidth, int padding, int frameInset) {
      return Math.max(1, totalWidth - padding * 2 - frameInset - (this.pop_Z ? 5 : 0));
   }

   private int init_I_F_F(float mouseX, float mouseY) {
      long now = System.currentTimeMillis();
      boolean closeEnough = !Float.isNaN(this.poll_F) && Math.abs(mouseX - this.poll_F) <= 4.0F && Math.abs(mouseY - this.push_F) <= 4.0F;
      if (now - this.poll_J <= 320L && closeEnough) {
         this.reset++;
      } else {
         this.reset = 1;
      }

      this.poll_J = now;
      this.poll_F = mouseX;
      this.push_F = mouseY;
      return this.reset;
   }

   private void init_V_I(int index) {
      int clamped = Math.max(0, Math.min(index, this.init_Str.length()));
      if (this.init_Str.isEmpty()) {
         this.close_I = this.open_I = 0;
      } else {
         int probe = clamped;
         if (probe == this.init_Str.length() && probe > 0) {
            probe--;
         }

         if (probe < this.init_Str.length() && !this.init_Z_C(this.init_Str.charAt(probe)) && probe > 0 && this.init_Z_C(this.init_Str.charAt(probe - 1))) {
            probe--;
         }

         int start = probe;
         int end;
         if (probe < this.init_Str.length() && this.init_Z_C(this.init_Str.charAt(probe))) {
            while (start > 0 && this.init_Z_C(this.init_Str.charAt(start - 1))) {
               start--;
            }

            end = probe + 1;

            while (end < this.init_Str.length() && this.init_Z_C(this.init_Str.charAt(end))) {
               end++;
            }
         } else {
            while (start > 0 && !this.init_Z_C(this.init_Str.charAt(start - 1)) && this.init_Str.charAt(start - 1) != '\n') {
               start--;
            }

            end = Math.min(this.init_Str.length(), probe + 1);

            while (end < this.init_Str.length() && !this.init_Z_C(this.init_Str.charAt(end)) && this.init_Str.charAt(end) != '\n') {
               end++;
            }
         }

         this.open_I = Utf16TextCursor.init_I(this.init_Str, start);
         this.close_I = Utf16TextCursor.load(this.init_Str, Math.max(start, end));
      }
   }

   private void load_V_I(int index) {
      int clamped = Math.max(0, Math.min(index, this.init_Str.length()));
      this.open_I = Utf16TextCursor.init_I(this.init_Str, this.read_I_I(clamped));
      this.close_I = Utf16TextCursor.init_I(this.init_Str, this.write_I(clamped));
      if (this.close_I < this.init_Str.length() && this.init_Str.charAt(this.close_I) == '\n') {
         this.close_I++;
      }
   }

   private int init_I_I(int index) {
      int i = Math.max(0, Math.min(index, this.init_Str.length()));

      while (i > 0 && Character.isWhitespace(this.init_Str.charAt(i - 1))) {
         i--;
      }

      if (i > 0 && this.init_Z_C(this.init_Str.charAt(i - 1))) {
         while (i > 0 && this.init_Z_C(this.init_Str.charAt(i - 1))) {
            i--;
         }
      } else {
         while (i > 0 && !Character.isWhitespace(this.init_Str.charAt(i - 1)) && !this.init_Z_C(this.init_Str.charAt(i - 1))) {
            i--;
         }
      }

      return i;
   }

   private int load_I_I(int index) {
      int i = Math.max(0, Math.min(index, this.init_Str.length()));

      while (i < this.init_Str.length() && Character.isWhitespace(this.init_Str.charAt(i))) {
         i++;
      }

      if (i < this.init_Str.length() && this.init_Z_C(this.init_Str.charAt(i))) {
         while (i < this.init_Str.length() && this.init_Z_C(this.init_Str.charAt(i))) {
            i++;
         }
      } else {
         while (i < this.init_Str.length() && !Character.isWhitespace(this.init_Str.charAt(i)) && !this.init_Z_C(this.init_Str.charAt(i))) {
            i++;
         }
      }

      return i;
   }

   private int read_I_I(int index) {
      int i = Math.max(0, Math.min(index, this.init_Str.length()));

      while (i > 0 && this.init_Str.charAt(i - 1) != '\n') {
         i--;
      }

      return i;
   }

   private int write_I(int index) {
      int i = Math.max(0, Math.min(index, this.init_Str.length()));

      while (i < this.init_Str.length() && this.init_Str.charAt(i) != '\n') {
         i++;
      }

      return i;
   }

   private boolean init_Z_C(char chr) {
      return Character.isLetterOrDigit(chr) || chr == '_' || chr == '-' || chr == ':' || chr == '.';
   }

   private void read_V_I(int direction) {
      List<String> history = this.init_List();
      if (!history.isEmpty()) {
         if (this.tick_I >= history.size()) {
            this.tick_I = history.size() - 1;
         }

         if (direction < 0) {
            if (this.tick_I == -1) {
               this.read_Str = this.init_Str;
               this.tick_I = history.size() - 1;
            } else if (this.tick_I > 0) {
               this.tick_I--;
            }

            this.init_V_Str(history.get(this.tick_I));
         } else {
            if (this.tick_I == -1) {
               return;
            }

            if (this.tick_I < history.size() - 1) {
               this.tick_I++;
               this.init_V_Str(history.get(this.tick_I));
            } else {
               this.tick_I = -1;
               this.init_V_Str(this.read_Str);
            }
         }
      }
   }

   private List<String> init_List() {
      List<String> out = new ArrayList<>();
      if (this.init_Supplier != null) {
         Collection<String> provided = this.init_Supplier.get();
         if (provided != null) {
            for (String entry : provided) {
               this.init_V_List_Str(out, entry);
            }
         }
      }

      for (String entry : this.init_List) {
         this.init_V_List_Str(out, entry);
      }

      return out;
   }

   private void init_V_List_Str(List<String> out, String entry) {
      String sanitized = this.init_Str_Str(entry);
      if (!sanitized.isEmpty() && (out.isEmpty() || !sanitized.equals(out.get(out.size() - 1)))) {
         out.add(sanitized);

         while (out.size() > 100) {
            out.remove(0);
         }
      }
   }

   private void init_V_Str(String nextValue) {
      this.get_Z = true;

      try {
         this.init_BitSetUtil_Str(nextValue);
      } finally {
         this.get_Z = false;
      }

      this.load(this.init_Str.length(), false);
   }

   private void load(int nextCursor, boolean keepSelection) {
      this.close_I = Utf16TextCursor.init_I(this.init_Str, nextCursor);
      if (!keepSelection) {
         this.open_I = this.close_I;
      }

      this.poll_Z = false;
      this.read_V();
   }

   private boolean close_Z() {
      return this.close_I != this.open_I;
   }

   private void close_V() {
      if (this.close_Z()) {
         int start = Math.min(this.close_I, this.open_I);
         int end = Math.max(this.close_I, this.open_I);
         Minecraft.getInstance().keyboardHandler.setClipboard(this.init_Str.substring(start, end));
      }
   }

   private void open_V() {
      String clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
      if (clipboard != null && !clipboard.isEmpty()) {
         this.load_V_Str(clipboard);
      }
   }

   private boolean open_Z() {
      if (!this.close_Z()) {
         return false;
      }

      int start = Math.min(this.close_I, this.open_I);
      int end = Math.max(this.close_I, this.open_I);
      this.init_V_I(start, end, "");
      return true;
   }

   private void load_V_Str(String replacement) {
      int start = Math.min(this.close_I, this.open_I);
      int end = Math.max(this.close_I, this.open_I);
      this.init_V_I(start, end, replacement);
   }

   private void init_V_I(int start, int end, String replacement) {
      int safeStart = Utf16TextCursor.init_I(this.init_Str, start);
      int safeEnd = Math.max(safeStart, Utf16TextCursor.load(this.init_Str, end));
      int capacity = Math.max(0, this.load_I - (this.init_Str.length() - (safeEnd - safeStart)));
      String sanitizedReplacement = Utf16TextCursor.init_Str(this.load_Str_Str(replacement), capacity);
      String next = this.init_Str.substring(0, safeStart) + sanitizedReplacement + this.init_Str.substring(safeEnd);
      if (this.init_Predicate.test(next)) {
         this.init_Str = next;
         this.sync_V();
         this.close_I = Utf16TextCursor.init_I(next, Math.min(next.length(), safeStart + sanitizedReplacement.length()));
         this.open_I = this.close_I;
         this.poll_Z = false;
         this.read_V();
         if (this.run_Z && !this.get_Z) {
            this.tick_I = -1;
            this.read_Str = this.init_Str;
         }

         if (this.init_Consumer != null) {
            this.init_Consumer.accept(this.init_Str);
         }
      }
   }

   private String init_Str_Str(String input) {
      String next = this.load_Str_Str(input);
      return this.init_Predicate.test(next) ? next : this.init_Str;
   }

   private String load_Str_Str(String input) {
      if (input != null && !input.isEmpty()) {
         StringBuilder sb = new StringBuilder(input.length());
         int i = 0;

         while (i < input.length() && sb.length() < this.load_I) {
            int codePoint = input.codePointAt(i);
            int units = Character.charCount(codePoint);
            i += units;
            if (this.init_Z_I(codePoint) && sb.length() + units <= this.load_I) {
               sb.appendCodePoint(codePoint);
            }
         }

         return sb.toString();
      } else {
         return "";
      }
   }

   private boolean init_Z_I(int codePoint) {
      if (codePoint == 10) {
         return this.pop_Z;
      } else {
         return codePoint == 13 ? false : codePoint >= 32 && codePoint != 127;
      }
   }

   private int init_I_Font(Font renderer, Identifier font, int color, String text) {
      return k.init(renderer, text, font, color);
   }

   private static int init_I_IArr(int[] widths, int from, int to, int target) {
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

   private static int load_I_IArr(int[] widths, int from, int to, int target) {
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

   private static int read(int[] widths, int from, int to, int target) {
      if (target <= widths[from]) {
         return from;
      }

      if (target >= widths[to]) {
         return to;
      }

      int right = init_I_IArr(widths, from, to, target);
      if (right <= from) {
         return from;
      }

      int left = right - 1;
      return target - widths[left] < widths[right] - target ? left : right;
   }

   private record Inner_CachePool(int init, int load, int read) {
   }

   private record Inner_DataUtil(ScrollbarMetricsHelper.Metrics init_PoolEntry_DataUtil, int init_I, int load) {
   }

   private record Inner_StreamUtil(String init_Str, List<Style> init_List) {
   }
}
