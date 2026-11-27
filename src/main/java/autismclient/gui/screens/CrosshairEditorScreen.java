package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.modules.CrosshairPixelArt;
import autismclient.modules.CrosshairModule;
import autismclient.modules.CrosshairPresetGeometry;
import autismclient.modules.CrosshairRenderState;
import autismclient.util.cA;
import com.mojang.blaze3d.platform.NativeImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class CrosshairEditorScreen extends VirtualScaledScreen {
   private static final int init_I = 540;
   private static final int load_I = 344;
   private static final int read_I = 6;
   private static final int write_I = 84;
   private static final int update_I = 128;
   private static final int sync_I = 100;
   private static final int flush = -14276304;
   private static final int close = -13552323;
   private static final int[] init_IArr = new int[]{
      16777215, 13158600, 8026746, 0, 16726843, 16747039, 16769333, 9240379, 2873707, 2876384, 3909631, 3890175, 10178047, 16731608, 16748468, 9132587
   };
   private final Screen parent;
   private final CrosshairModule module;
   private final CrosshairPixelArt init_m;
   private final ArrayDeque<CrosshairEditorScreen.Inner_BufferHelper> init_ArrayDeque = new ArrayDeque<>();
   private final ArrayDeque<CrosshairEditorScreen.Inner_BufferHelper> load_ArrayDeque = new ArrayDeque<>();
   private final List<CrosshairEditorScreen.Inner_DataUtil> init_List = new ArrayList<>();
   private final List<autismclient.gui.f.NetHelper> load_List = new ArrayList<>();
   private final List<String> read_List = new ArrayList<>();
   private int open;
   private int[] load_IArr;
   private CrosshairEditorScreen.Inner_ConfigStore option;
   private CrosshairEditorScreen.Inner_ConfigStore load_NetHelper_ConfigSt;
   private int poll;
   private boolean init_Z;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z;
   private int push;
   private int pop;
   private int tick;
   private int[] read_IArr;
   private int run;
   private int get;
   private int set;
   private int put;
   private boolean update_Z;
   private autismclient.gui.ui.widgets.ColorPickerPopup init_NetHelper_2;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private int add;
   private int clear;
   private CrosshairPixelArt load_m;
   private DynamicTexture init_DynamicTexture;
   private Identifier init_Identifier;
   private int reset;
   private boolean sync_Z;
   private int start;
   private int stop;
   private int next;

   public CrosshairEditorScreen(Screen parent, CrosshairModule module) {
      this(parent, module, module.art());
   }

   public CrosshairEditorScreen(Screen parent, CrosshairModule module, CrosshairPixelArt start) {
      super(Component.literal("Crosshair Editor"));
      this.option = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      this.load_NetHelper_ConfigSt = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      this.poll = 1;
      this.write_Z = true;
      this.push = 16777215;
      this.pop = 100;
      this.tick = -1;
      this.sync_Z = true;
      this.stop = -1;
      this.next = -1;
      this.parent = parent;
      this.module = module;
      this.init_m = module.art();
      this.open = start.size();
      this.load_IArr = start.pixels();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   public double[] init_DArr(int cellX, int cellY) {
      CrosshairEditorScreen.Inner_CachePool layout = this.init_NetHelper_CachePoo();
      double scale = cA.init_F();
      return new double[]{
         (layout.canvas().read() + (cellX + 0.5) * layout.load_I()) * scale,
         (layout.canvas().write() + (cellY + 0.5) * layout.load_I()) * scale
      };
   }

   private CrosshairEditorScreen.Inner_CachePool init_NetHelper_CachePoo() {
      this.init_List.clear();
      this.load_List.clear();
      this.read_List.clear();
      int sw = this.virtualWidth();
      int sh = this.virtualHeight();
      int w = Math.max(1, Math.min(540, sw - 12));
      int h = Math.max(1, Math.min(344, sh - 12));
      int x = (sw - w) / 2;
      int y = (sh - h) / 2;
      int header = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int ctl = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
      int gap = 2;
      int rowY = y + header + 6;
      int cx = x + 6 + this.init_I_Str_13("Resolution") + 4;

      for (int option : CrosshairPixelArt.SIZES) {
         int chipW = this.init_I_Str_13(Integer.toString(option)) + 12;
         this.init_List
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(cx, rowY, chipW, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  Integer.toString(option),
                  this.open == option,
                  null,
                  0,
                  option + " by " + option + " pixels",
                  () -> this.load(option)
               )
            );
         cx += chipW + gap;
      }

      cx += 6;
      cx = this.init(cx, rowY, ctl, "Mirror X", this.init_Z, "Mirror left and right (X)", () -> this.init_Z = !this.init_Z);
      cx = this.init(cx, rowY, ctl, "Mirror Y", this.load_Z, "Mirror top and bottom (Y)", () -> this.load_Z = !this.load_Z);
      this.init(cx, rowY, ctl, "Grid", this.write_Z, "Show the grid (G)", () -> this.write_Z = !this.write_Z);
      int rx = x + w - 6;
      rx = this.init(rx, rowY, ctl, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, "Erase everything", this::close);
      rx = this.init(rx, rowY, ctl, "Redo", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, "Redo (Ctrl+Y)", this::poll);
      this.init(rx, rowY, ctl, "Undo", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, "Undo (Ctrl+Z)", this::open);
      int bottomY = y + h - 6 - ctl;
      int bodyTop = rowY + ctl + 6;
      int bodyBottom = bottomY - 6;
      int bodyH = Math.max(1, bodyBottom - bodyTop);
      int tx = x + 6;
      int ty = bodyTop;
      int half = (84 - gap) / 2;
      CrosshairEditorScreen.Inner_ConfigStore[] tools = CrosshairEditorScreen.Inner_ConfigStore.values();

      for (int i = 0; i < tools.length; i++) {
         CrosshairEditorScreen.Inner_ConfigStore option = tools[i];
         this.init_List
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + i % 2 * (half + gap), ty + i / 2 * (ctl + gap), half, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  option.init_Str,
                  this.option == option,
                  null,
                  0,
                  option.load_Str,
                  () -> this.init_V_NetHelper_ConfigSt(option)
               )
            );
      }

      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(tx + half + gap, ty + tools.length / 2 * (ctl + gap), half, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.CHIP,
               "Filled",
               this.read_Z,
               null,
               0,
               "Solid boxes and circles",
               () -> this.read_Z = !this.read_Z
            )
         );
      ty += (tools.length + 2) / 2 * (ctl + gap) - gap + 6;
      int rowText = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      this.add = ty;
      ty += rowText;
      int brushW = (84 - gap * 3) / 4;

      for (int b = 1; b <= 4; b++) {
         int value = b;
         this.init_List
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + (b - 1) * (brushW + gap), ty, brushW, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  Integer.toString(b),
                  this.poll == b,
                  null,
                  0,
                  "Brush size (scroll)",
                  () -> this.poll = value
               )
            );
      }

      ty += ctl + 6;
      this.clear = ty;
      ty += rowText;
      CrosshairPresetGeometry.Inner_NetHelper[] presets = new CrosshairPresetGeometry.Inner_NetHelper[]{
         CrosshairPresetGeometry.Inner_NetHelper.CROSS, CrosshairPresetGeometry.Inner_NetHelper.TACTICAL, CrosshairPresetGeometry.Inner_NetHelper.CIRCLE, CrosshairPresetGeometry.Inner_NetHelper.CHEVRON
      };

      for (int i = 0; i < presets.length; i++) {
         CrosshairPresetGeometry.Inner_NetHelper preset = presets[i];
         this.init_List
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + i % 2 * (half + gap), ty + i / 2 * (ctl + gap), half, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
                  preset.init_Str,
                  false,
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
                  0,
                  "Replace with this shape",
                  () -> this.init_V_p_NetHelper(preset)
               )
            );
      }

      int sx = x + w - 6 - 128;
      int inner = 120;
      int titleH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
      int swatch = (inner - gap * 7) / 8;
      int colorH = titleH + 2 + 16 + 4 + swatch * 2 + gap + 4 + autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 11 + 4;
      autismclient.gui.f.NetHelper colorCard = autismclient.gui.f.NetHelper.init(sx, bodyTop, 128, colorH);
      this.init(colorCard, "Color");
      int iy = bodyTop + titleH + 2;
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(sx + 4, iy, 40, 16);
      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(this.init_NetHelper, CrosshairEditorScreen.Inner_StreamUtil.SWATCH, "", false, null, this.read_I_9(), "Pick any color", this::update)
         );
      iy += 20;

      for (int i = 0; i < init_IArr.length; i++) {
         int color = init_IArr[i];
         int col = i % 8;
         int row = i / 8;
         this.init_List
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(sx + 4 + col * (swatch + gap), iy + row * (swatch + gap), swatch, swatch),
                  CrosshairEditorScreen.Inner_StreamUtil.SWATCH,
                  "",
                  this.push == color,
                  null,
                  0xFF000000 | color,
                  null,
                  () -> this.init_V_I_7(color)
               )
            );
      }

      iy += swatch * 2 + gap + 4 + autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      autismclient.gui.f.NetHelper opacityBounds = autismclient.gui.f.NetHelper.init(sx + 4, iy, inner, 11);
      autismclient.gui.f.NetHelper preview = null;
      int previewTop = colorCard.load() + 6;
      int previewH = bodyBottom - previewTop;
      if (previewH >= titleH + 30) {
         autismclient.gui.f.NetHelper previewCard = autismclient.gui.f.NetHelper.init(sx, previewTop, 128, previewH);
         this.init(previewCard, "Preview");
         preview = autismclient.gui.f.NetHelper.init(sx + 4, previewTop + titleH + 2, inner, previewH - titleH - 6);
      }

      int areaX = tx + 84 + 6;
      int areaW = Math.max(1, sx - 6 - areaX);
      int avail = Math.min(areaW, bodyH);
      int cell = Math.max(1, avail / this.open);
      int canvasSize = cell * this.open;
      autismclient.gui.f.NetHelper canvas = autismclient.gui.f.NetHelper.init(
         areaX + (areaW - canvasSize) / 2, bodyTop + (bodyH - canvasSize) / 2, canvasSize, canvasSize
      );
      int doneW = this.init_I_Str_13("Done") + 16;
      int cancelW = this.init_I_Str_13("Cancel") + 16;
      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(x + w - 6 - doneW, bottomY, doneW, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
               "Done",
               false,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
               0,
               "Save and close (Esc)",
               this::pop
            )
         );
      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(x + w - 6 - doneW - gap - cancelW, bottomY, cancelW, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
               "Cancel",
               false,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               0,
               "Close without saving",
               this::run
            )
         );
      autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(x + 6, bottomY, Math.max(1, w - 18 - doneW - cancelW - gap), ctl);
      return new CrosshairEditorScreen.Inner_CachePool(autismclient.gui.f.NetHelper.init(x, y, w, h), header, canvas, cell, opacityBounds, preview, status);
   }

   private int init(int x, int y, int h, String label, boolean selected, String tip, Runnable action) {
      int w = this.init_I_Str_13(label) + 12;
      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(autismclient.gui.f.NetHelper.init(x, y, w, h), CrosshairEditorScreen.Inner_StreamUtil.CHIP, label, selected, null, 0, tip, action)
         );
      return x + w + 2;
   }

   private int init(int right, int y, int h, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, String tip, Runnable action) {
      int w = this.init_I_Str_13(label) + 14;
      this.init_List
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(right - w, y, w, h), CrosshairEditorScreen.Inner_StreamUtil.BUTTON, label, false, tone, 0, tip, action
            )
         );
      return right - w - 2;
   }

   private void init(autismclient.gui.f.NetHelper bounds, String title) {
      this.load_List.add(bounds);
      this.read_List.add(title);
   }

   private int init_I_Str_13(String text) {
      return autismclient.gui.f.UiHelper.init(this.font).measureWidth(text);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         CrosshairEditorScreen.Inner_CachePool layout = this.init_NetHelper_CachePoo();
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(
            graphics, this.font, this.init_NetHelper_2 == null ? mx : -10000, this.init_NetHelper_2 == null ? my : -10000
         );
         autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
         autismclient.gui.f.NetHelper window = layout.window();
         autismclient.gui.ui.widgets.DialogFrameRenderer.init(
            context,
            window,
            layout.header(),
            "Crosshair Editor",
            this.init_NetHelper_2 == null && my >= window.write() && my < window.write() + layout.header() && window.init_Z(mx, my)
         );

         for (int i = 0; i < this.load_List.size(); i++) {
            autismclient.gui.f.b.BufferHelper.init_V(context, this.load_List.get(i), this.read_List.get(i));
         }

         int rowY = window.write() + layout.header() + 6;
         int ctl = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         context.init_PoolEntry()
            .draw(
               graphics, "Resolution", window.read() + 6, context.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(0, rowY, 1, ctl)), colors.textSecondary
            );
         context.init_PoolEntry().draw(graphics, "Brush", window.read() + 6, this.add, colors.textSecondary);
         context.init_PoolEntry().draw(graphics, "Start From", window.read() + 6, this.clear, colors.textSecondary);
         this.init(context, layout, mx, my);
         CrosshairEditorScreen.Inner_DataUtil hovered = null;

         for (CrosshairEditorScreen.Inner_DataUtil control : this.init_List) {
            boolean over = this.init_NetHelper_2 == null && control.init_NetHelper().init_Z(mx, my);
            if (over) {
               hovered = control;
            }

            switch (control.init_NetHelper_StreamUt()) {
               case CHIP:
                  autismclient.gui.ui.widgets.PillBadgeRenderer.init(context, control.init_NetHelper(), control.init_Str(), control.init_Z(), over);
                  break;
               case BUTTON:
                  autismclient.gui.f.b.CachePool.init(context, control.init_NetHelper(), control.init_Str(), control.init_CachePool_DataUtil(), over, false);
                  break;
               case SWATCH:
                  this.init(context, control.init_NetHelper(), control.argb(), control.init_Z(), over);
            }
         }

         autismclient.gui.f.NetHelper opacityBounds = layout.read();
         context.init_PoolEntry()
            .draw(
               graphics, "Opacity " + this.pop + "%", opacityBounds.read(), opacityBounds.write() - autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 1, colors.textSecondary
            );
         autismclient.gui.ui.widgets.SliderRenderer.init(
            context,
            opacityBounds,
            autismclient.gui.ui.widgets.SliderRenderer.ratio(this.pop, 5.0, 100.0),
            this.init_NetHelper_2 == null && (this.update_Z || opacityBounds.init_Z(mx, my))
         );
         String status = this.stop >= 0 ? this.option.read_Str + "   " + this.stop + ", " + this.next : this.option.read_Str;
         context.init_PoolEntry()
            .drawFitted(graphics, status, layout.update().read(), context.init_PoolEntry().centerTextY(layout.update()), layout.update().update(), colors.textSecondary);
         if (layout.write() != null) {
            this.init(context, layout.write());
         }

         if (hovered != null && hovered.load() != null && this.tick < 0) {
            graphics.nextStratum();
            TooltipRenderer.init(context, hovered.load(), mx, my);
         }

         if (this.init_NetHelper_2 != null) {
            graphics.nextStratum();
            this.init_NetHelper_2.render(autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my));
            if (!this.init_NetHelper_2.isOpen()) {
               this.init_NetHelper_2 = null;
            }
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void init(autismclient.gui.f.SyncManager context, CrosshairEditorScreen.Inner_CachePool layout, int mx, int my) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.NetHelper canvas = layout.canvas();
      int cell = layout.load_I();
      autismclient.gui.f.PacketUtil.load(graphics, canvas.init(-3), 5, colors.inputSurface, colors.borderSubtle);
      int tile = Math.max(cell, canvas.update() / 8);

      for (int ty = 0; ty * tile < canvas.sync(); ty++) {
         for (int tx = 0; tx * tile < canvas.update(); tx++) {
            int w = Math.min(tile, canvas.update() - tx * tile);
            int h = Math.min(tile, canvas.sync() - ty * tile);
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(canvas.read() + tx * tile, canvas.write() + ty * tile, w, h),
               (tx + ty & 1) == 0 ? -14276304 : -13552323
            );
         }
      }

      this.load_V_15();
      if (this.init_Identifier != null) {
         graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            this.init_Identifier,
            canvas.read(),
            canvas.write(),
            0.0F,
            0.0F,
            canvas.update(),
            canvas.sync(),
            this.open,
            this.open,
            this.open,
            this.open
         );
      }

      if (this.write_Z && cell >= 4) {
         for (int i = 1; i < this.open; i++) {
            autismclient.gui.f.PacketUtil.load(graphics, canvas.read() + i * cell, canvas.write(), canvas.sync(), 486539263);
            autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, canvas.read(), canvas.write() + i * cell, canvas.update(), 486539263);
         }
      }

      if (this.write_Z) {
         int guide = colors.accent & 16777215 | -2147483648;
         autismclient.gui.f.PacketUtil.load(graphics, canvas.read() + canvas.update() / 2, canvas.write(), canvas.sync(), guide);
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, canvas.read(), canvas.write() + canvas.sync() / 2, canvas.update(), guide);
      }

      this.stop = -1;
      this.next = -1;
      if (this.init_NetHelper_2 == null && canvas.init_Z(mx, my)) {
         this.stop = Math.min(this.open - 1, (mx - canvas.read()) / cell);
         this.next = Math.min(this.open - 1, (my - canvas.write()) / cell);
         if (this.tick < 0) {
            int mark = this.option == CrosshairEditorScreen.Inner_ConfigStore.ERASER
               ? 1728007501
               : (
                  this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER
                        && this.option != CrosshairEditorScreen.Inner_ConfigStore.FILL
                     ? this.read_I_9() & 16777215 | -1728053248
                     : 1728053247
               );
            int span = this.option != CrosshairEditorScreen.Inner_ConfigStore.PEN
                  && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER
                  && !this.option.init()
               ? 1
               : this.poll;

            for (int[] point : this.init_List(this.stop, this.next, span)) {
               autismclient.gui.f.PacketUtil.init(
                  graphics, autismclient.gui.f.NetHelper.init(canvas.read() + point[0] * cell, canvas.write() + point[1] * cell, cell, cell), mark
               );
            }
         }
      }
   }

   private void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int argb, boolean selected, boolean hovered) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      int half = Math.max(1, bounds.sync() / 2);

      for (int ty = 0; ty * half < bounds.sync(); ty++) {
         for (int tx = 0; tx * half < bounds.update(); tx++) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(
                  bounds.read() + tx * half, bounds.write() + ty * half, Math.min(half, bounds.update() - tx * half), Math.min(half, bounds.sync() - ty * half)
               ),
               (tx + ty & 1) == 0 ? -4342339 : -1
            );
         }
      }

      int border = selected ? colors.accent : (hovered ? colors.textPrimary : colors.borderControl);
      autismclient.gui.f.PacketUtil.load(graphics, bounds, 3, argb, border);
   }

   private void init(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper box) {
      CrosshairRenderState.Inner_DataUtil look = this.module.init_q_DataUtil_m(this.init_m());
      CrosshairRenderState.Inner_DataUtil shown = new CrosshairRenderState.Inner_DataUtil(
         CrosshairPresetGeometry.Inner_NetHelper.CUSTOM,
         look.init_D(),
         look.load_D(),
         look.read_D(),
         look.init_Z(),
         look.init_I(),
         look.init_F(),
         look.load_Z(),
         look.load_I(),
         look.write_D(),
         false,
         0,
         0,
         false,
         0.0,
         look.art()
      );
      autismclient.gui.ui.widgets.CrosshairPreviewRenderer.load(context, box, shown);
   }

   private void load_V_15() {
      if (this.sync_Z || this.init_DynamicTexture == null) {
         if (this.init_DynamicTexture == null || this.reset != this.open) {
            this.read_V_9();
            NativeImage image = new NativeImage(this.open, this.open, true);
            this.init_DynamicTexture = new DynamicTexture(() -> "AUTISM crosshair canvas", image);
            this.init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "dynamic/crosshair/canvas_" + ++this.start);
            this.minecraft.getTextureManager().register(this.init_Identifier, this.init_DynamicTexture);
            this.reset = this.open;
         }

         NativeImage image = this.init_DynamicTexture.getPixels();
         if (image != null) {
            for (int y = 0; y < this.open; y++) {
               for (int x = 0; x < this.open; x++) {
                  image.setPixel(x, y, this.load_IArr[y * this.open + x]);
               }
            }

            this.init_DynamicTexture.upload();
            this.sync_Z = false;
         }
      }
   }

   private void read_V_9() {
      if (this.init_Identifier != null) {
         try {
            this.minecraft.getTextureManager().release(this.init_Identifier);
         } catch (Throwable var2) {
         }
      }

      this.init_DynamicTexture = null;
      this.init_Identifier = null;
   }

   @Override
   public void removed() {
      this.read_V_9();
      super.removed();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      int button = Compat263.mouseEventGlfwButton(event);
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.mouseClicked(mx, my, button);
         if (!this.init_NetHelper_2.isOpen()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else {
         CrosshairEditorScreen.Inner_CachePool layout = this.init_NetHelper_CachePoo();
         if (button == 0 && autismclient.gui.ui.widgets.DialogFrameRenderer.init(layout.window(), layout.header(), mx, my)) {
            this.pop();
            return true;
         }

         autismclient.gui.f.NetHelper canvas = layout.canvas();
         if (!canvas.init_Z(mx, my)) {
            if (button != 0) {
               return true;
            }

            if (layout.read().load(0, -3, 0, -3).init_Z(mx, my)) {
               this.update_Z = true;
               this.init(mx, layout.read());
               return true;
            }

            for (CrosshairEditorScreen.Inner_DataUtil control : this.init_List) {
               if (control.init_NetHelper().init_Z(mx, my)) {
                  control.init_Runnable().run();
                  return true;
               }
            }

            return true;
         } else {
            int cx = Math.min(this.open - 1, (mx - canvas.read()) / layout.load_I());
            int cy = Math.min(this.open - 1, (my - canvas.write()) / layout.load_I());
            if (button != 2 && (button != 0 || !event.hasAltDown() && this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER)) {
               if (button == 0 || button == 1) {
                  this.init(button, cx, cy, event.hasShiftDown());
               }
            } else {
               this.load(cx, cy);
            }

            return true;
         }
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), cA.init_D_D(dragX), cA.init_D_D(dragY));
         return true;
      }

      CrosshairEditorScreen.Inner_CachePool layout = this.init_NetHelper_CachePoo();
      if (this.update_Z) {
         this.init(mx, layout.read());
         return true;
      }

      if (this.tick >= 0 && Compat263.mouseEventGlfwButton(event) == this.tick) {
         autismclient.gui.f.NetHelper canvas = layout.canvas();
         int cx = Math.max(0, Math.min(this.open - 1, Math.floorDiv(mx - canvas.read(), layout.load_I())));
         int cy = Math.max(0, Math.min(this.open - 1, Math.floorDiv(my - canvas.write(), layout.load_I())));
         this.init(cx, cy, event.hasShiftDown());
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.load(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.init_NetHelper_2.isOpen()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(event) == 0) {
            this.update_Z = false;
         }

         if (this.tick >= 0 && Compat263.mouseEventGlfwButton(event) == this.tick) {
            this.sync();
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.init_NetHelper_2 != null) {
         return true;
      }

      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      if (this.init_NetHelper_CachePoo().canvas().init_Z(mx, my) && vertical != 0.0) {
         this.poll = Math.max(1, Math.min(4, this.poll + (vertical > 0.0 ? 1 : -1)));
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.init_NetHelper_2 != null) {
         this.init_NetHelper_2.read(Compat263.keyEventGlfwKey(event), event.keycode(), Compat263.keyEventGlfwModifiers(event));
         if (!this.init_NetHelper_2.isOpen()) {
            this.init_NetHelper_2 = null;
         }

         return true;
      } else {
         int key = Compat263.keyEventGlfwKey(event);
         if (key == 256) {
            if (this.tick >= 0) {
               this.flush();
            } else {
               this.pop();
            }

            return true;
         } else {
            if (this.tick >= 0) {
               return true;
            }

            if (!event.hasControlDown()) {
               for (CrosshairEditorScreen.Inner_ConfigStore option : CrosshairEditorScreen.Inner_ConfigStore.values()) {
                  if (option.init_I == key) {
                     this.init_V_NetHelper_ConfigSt(option);
                     return true;
                  }
               }

               switch (key) {
                  case 71:
                     this.write_Z = !this.write_Z;
                     break;
                  case 88:
                     this.init_Z = !this.init_Z;
                     break;
                  case 89:
                     this.load_Z = !this.load_Z;
                     break;
                  case 91:
                     this.poll = Math.max(1, this.poll - 1);
                     break;
                  case 93:
                     this.poll = Math.min(4, this.poll + 1);
                     break;
                  default:
                     return super.keyPressed(event);
               }

               return true;
            } else {
               if ((key != 90 || !event.hasShiftDown()) && key != 89) {
                  if (key == 90) {
                     this.open();
                  }
               } else {
                  this.poll();
               }

               return true;
            }
         }
      }
   }

   @Override
   public void onClose() {
      this.pop();
   }

   private void init_V_NetHelper_ConfigSt(CrosshairEditorScreen.Inner_ConfigStore option) {
      if (option == CrosshairEditorScreen.Inner_ConfigStore.PICKER && this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER) {
         this.load_NetHelper_ConfigSt = this.option;
      }

      this.option = option;
   }

   private int read_I_9() {
      return Math.round(this.pop * 2.55F) << 24 | this.push & 16777215;
   }

   private void init_V_I_7(int color) {
      this.push = color & 16777215;
      this.write_V_8();
   }

   private void write_V_8() {
      if (this.option == CrosshairEditorScreen.Inner_ConfigStore.PICKER) {
         this.option = this.load_NetHelper_ConfigSt == CrosshairEditorScreen.Inner_ConfigStore.ERASER
            ? CrosshairEditorScreen.Inner_ConfigStore.PEN
            : this.load_NetHelper_ConfigSt;
      } else if (this.option == CrosshairEditorScreen.Inner_ConfigStore.ERASER) {
         this.option = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      }
   }

   private void init(int mouseX, autismclient.gui.f.NetHelper bounds) {
      this.pop = (int)Math.round(autismclient.gui.ui.widgets.SliderRenderer.init(mouseX, bounds.read(), bounds.update(), 5.0, 100.0, 5.0));
   }

   private void update() {
      this.init_NetHelper_2 = new autismclient.gui.ui.widgets.ColorPickerPopup(
         this.init_NetHelper, 0xFF000000 | this.push, -1, this.virtualWidth(), this.virtualHeight(), this::init_V_I_7
      );
   }

   private void load(int x, int y) {
      int p = this.load_IArr[y * this.open + x];
      if (p >>> 24 != 0) {
         this.push = p & 16777215;
         this.pop = Math.max(5, Math.min(100, Math.round((p >>> 24) / 2.55F / 5.0F) * 5));
         this.write_V_8();
      }
   }

   private void init(int button, int x, int y, boolean shift) {
      this.tick = button;
      this.read_IArr = (int[])this.load_IArr.clone();
      this.run = this.set = x;
      this.get = this.put = y;
      int paint = this.write_I_8();
      switch (this.option) {
         case FILL:
            for (int[] seed : this.init_List(x, y)) {
               this.read(seed[0], seed[1], paint);
            }
            break;
         case LINE:
         case BOX:
         case CIRCLE:
            this.load(x, y, shift);
            break;
         default:
            this.init_V(x, y, paint);
      }

      this.push();
   }

   private void init(int x, int y, boolean shift) {
      if (x != this.set || y != this.put || this.option.init()) {
         switch (this.option) {
            case PEN:
            case ERASER:
               this.read(this.set, this.put, x, y, this.write_I_8());
               break;
            case FILL:
            default:
               return;
            case LINE:
            case BOX:
            case CIRCLE:
               this.load_IArr = (int[])this.read_IArr.clone();
               this.load(x, y, shift);
         }

         this.set = x;
         this.put = y;
         this.push();
      }
   }

   private void sync() {
      if (this.read_IArr != null && !Arrays.equals(this.read_IArr, this.load_IArr)) {
         this.init_V_NetHelper_BufferHe(new CrosshairEditorScreen.Inner_BufferHelper(this.open, this.read_IArr));
      }

      this.tick = -1;
      this.read_IArr = null;
   }

   private void flush() {
      if (this.read_IArr != null) {
         this.load_IArr = this.read_IArr;
      }

      this.tick = -1;
      this.read_IArr = null;
      this.push();
   }

   private int write_I_8() {
      return this.tick != 1 && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER ? this.read_I_9() : 0;
   }

   private void load(int x, int y, boolean shift) {
      int paint = this.write_I_8();
      int dx = x - this.run;
      int dy = y - this.get;
      switch (this.option) {
         case LINE:
            if (shift) {
               if (Math.abs(dx) > 2 * Math.abs(dy)) {
                  dy = 0;
               } else if (Math.abs(dy) > 2 * Math.abs(dx)) {
                  dx = 0;
               } else {
                  int d = Math.max(Math.abs(dx), Math.abs(dy));
                  dx = Integer.signum(dx) * d;
                  dy = Integer.signum(dy) * d;
               }
            }

            this.read(this.run, this.get, this.init_I_I_9(this.run + dx), this.init_I_I_9(this.get + dy), paint);
            break;
         case BOX:
         case CIRCLE:
            if (shift) {
               int d = Math.max(Math.abs(dx), Math.abs(dy));
               dx = (dx < 0 ? -1 : 1) * d;
               dy = (dy < 0 ? -1 : 1) * d;
            }

            int x0 = Math.min(this.run, this.init_I_I_9(this.run + dx));
            int x1 = Math.max(this.run, this.init_I_I_9(this.run + dx));
            int y0 = Math.min(this.get, this.init_I_I_9(this.get + dy));
            int y1 = Math.max(this.get, this.init_I_I_9(this.get + dy));
            if (this.option == CrosshairEditorScreen.Inner_ConfigStore.BOX) {
               this.init(x0, y0, x1, y1, paint);
            } else {
               this.load(x0, y0, x1, y1, paint);
            }
      }
   }

   private void init(int x0, int y0, int x1, int y1, int paint) {
      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            boolean edge = x == x0 || x == x1 || y == y0 || y == y1;
            if (this.read_Z) {
               this.load(x, y, paint);
            } else if (edge) {
               this.init_V(x, y, paint);
            }
         }
      }
   }

   private void load(int x0, int y0, int x1, int y1, int paint) {
      double cx = (x0 + x1 + 1) / 2.0;
      double cy = (y0 + y1 + 1) / 2.0;
      double rx = (x1 - x0 + 1) / 2.0;
      double ry = (y1 - y0 + 1) / 2.0;

      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            if (init_Z_I(x, y, cx, cy, rx, ry)) {
               boolean edge = !init_Z_I(x - 1, y, cx, cy, rx, ry)
                  || !init_Z_I(x + 1, y, cx, cy, rx, ry)
                  || !init_Z_I(x, y - 1, cx, cy, rx, ry)
                  || !init_Z_I(x, y + 1, cx, cy, rx, ry);
               if (this.read_Z) {
                  this.load(x, y, paint);
               } else if (edge) {
                  this.init_V(x, y, paint);
               }
            }
         }
      }
   }

   private static boolean init_Z_I(int x, int y, double cx, double cy, double rx, double ry) {
      double nx = (x + 0.5 - cx) / rx;
      double ny = (y + 0.5 - cy) / ry;
      return nx * nx + ny * ny <= 1.0;
   }

   private void read(int x0, int y0, int x1, int y1, int paint) {
      int dx = Math.abs(x1 - x0);
      int dy = -Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx + dy;
      int x = x0;
      int y = y0;

      while (true) {
         this.init_V(x, y, paint);
         if (x == x1 && y == y1) {
            return;
         }

         int e2 = 2 * err;
         if (e2 >= dy) {
            err += dy;
            x += sx;
         }

         if (e2 <= dx) {
            err += dx;
            y += sy;
         }
      }
   }

   private void init_V(int x, int y, int paint) {
      int span = !this.option.init()
            && this.option != CrosshairEditorScreen.Inner_ConfigStore.PEN
            && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER
         ? 1
         : this.poll;
      int from = -(span - 1) / 2;

      for (int oy = from; oy < from + span; oy++) {
         for (int ox = from; ox < from + span; ox++) {
            this.load(x + ox, y + oy, paint);
         }
      }
   }

   private void load(int x, int y, int paint) {
      for (int[] point : this.init_List(x, y)) {
         if (point[0] >= 0 && point[1] >= 0 && point[0] < this.open && point[1] < this.open) {
            this.load_IArr[point[1] * this.open + point[0]] = paint;
         }
      }
   }

   private List<int[]> init_List(int x, int y) {
      List<int[]> points = new ArrayList<>(4);
      points.add(new int[]{x, y});
      if (this.init_Z) {
         points.add(new int[]{this.open - 1 - x, y});
      }

      if (this.load_Z) {
         points.add(new int[]{x, this.open - 1 - y});
      }

      if (this.init_Z && this.load_Z) {
         points.add(new int[]{this.open - 1 - x, this.open - 1 - y});
      }

      return points;
   }

   private List<int[]> init_List(int x, int y, int span) {
      List<int[]> points = new ArrayList<>();
      int from = -(span - 1) / 2;

      for (int oy = from; oy < from + span; oy++) {
         for (int ox = from; ox < from + span; ox++) {
            for (int[] point : this.init_List(x + ox, y + oy)) {
               if (point[0] >= 0 && point[1] >= 0 && point[0] < this.open && point[1] < this.open) {
                  points.add(point);
               }
            }
         }
      }

      return points;
   }

   private void read(int x, int y, int paint) {
      int target = this.load_IArr[y * this.open + x];
      if (target != paint) {
         ArrayDeque<Integer> queue = new ArrayDeque<>();
         queue.add(y * this.open + x);
         this.load_IArr[y * this.open + x] = paint;

         while (!queue.isEmpty()) {
            int i = queue.poll();
            int px = i % this.open;
            int py = i / this.open;
            int[][] steps = new int[][]{{px - 1, py}, {px + 1, py}, {px, py - 1}, {px, py + 1}};

            for (int[] step : steps) {
               if (step[0] >= 0 && step[1] >= 0 && step[0] < this.open && step[1] < this.open) {
                  int n = step[1] * this.open + step[0];
                  if (this.load_IArr[n] == target) {
                     this.load_IArr[n] = paint;
                     queue.add(n);
                  }
               }
            }
         }
      }
   }

   private int init_I_I_9(int value) {
      return Math.max(0, Math.min(this.open - 1, value));
   }

   private void load(int newSize) {
      if (newSize != this.open && CrosshairPixelArt.isValidSize(newSize)) {
         this.init_V_NetHelper_BufferHe(new CrosshairEditorScreen.Inner_BufferHelper(this.open, (int[])this.load_IArr.clone()));
         this.load_IArr = new CrosshairPixelArt(this.open, this.load_IArr).load(newSize).pixels();
         this.open = newSize;
         this.push();
      }
   }

   private void init_V_p_NetHelper(CrosshairPresetGeometry.Inner_NetHelper style) {
      this.init_V_NetHelper_BufferHe(new CrosshairEditorScreen.Inner_BufferHelper(this.open, (int[])this.load_IArr.clone()));
      this.load_IArr = CrosshairPresetGeometry.init(style, this.open, this.module.thickness(), this.module.gap(), this.read_I_9()).pixels();
      this.push();
   }

   private void close() {
      boolean empty = true;

      for (int p : this.load_IArr) {
         if (p != 0) {
            empty = false;
         }
      }

      if (!empty) {
         this.init_V_NetHelper_BufferHe(new CrosshairEditorScreen.Inner_BufferHelper(this.open, (int[])this.load_IArr.clone()));
         this.load_IArr = new int[this.open * this.open];
         this.push();
      }
   }

   private void init_V_NetHelper_BufferHe(CrosshairEditorScreen.Inner_BufferHelper snapshot) {
      this.init_ArrayDeque.push(snapshot);

      while (this.init_ArrayDeque.size() > 100) {
         this.init_ArrayDeque.removeLast();
      }

      this.load_ArrayDeque.clear();
   }

   private void open() {
      if (!this.init_ArrayDeque.isEmpty()) {
         this.load_ArrayDeque.push(new CrosshairEditorScreen.Inner_BufferHelper(this.open, (int[])this.load_IArr.clone()));
         this.load(this.init_ArrayDeque.pop());
      }
   }

   private void poll() {
      if (!this.load_ArrayDeque.isEmpty()) {
         this.init_ArrayDeque.push(new CrosshairEditorScreen.Inner_BufferHelper(this.open, (int[])this.load_IArr.clone()));
         this.load(this.load_ArrayDeque.pop());
      }
   }

   private void load(CrosshairEditorScreen.Inner_BufferHelper snapshot) {
      this.open = snapshot.init_I();
      this.load_IArr = (int[])snapshot.init_IArr().clone();
      this.push();
   }

   private void push() {
      this.sync_Z = true;
      this.load_m = null;
   }

   private CrosshairPixelArt init_m() {
      if (this.load_m == null) {
         this.load_m = new CrosshairPixelArt(this.open, this.load_IArr);
      }

      return this.load_m;
   }

   private void pop() {
      if (this.tick >= 0) {
         this.sync();
      }

      CrosshairPixelArt drawn = this.init_m();
      if (!drawn.equals(this.init_m)) {
         this.module.setArt(drawn);
      }

      this.minecraft.gui.setScreen(this.parent);
   }

   private void run() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private record Inner_BufferHelper(int init_I, int[] init_IArr) {
   }

   private record Inner_CachePool(
      autismclient.gui.f.NetHelper window,
      int header,
      autismclient.gui.f.NetHelper canvas,
      int load_I,
      autismclient.gui.f.NetHelper read,
      autismclient.gui.f.NetHelper write,
      autismclient.gui.f.NetHelper update
   ) {
   }

   private enum Inner_ConfigStore {
      PEN("Pen", "Draw pixels (B)", "Left draws, right erases", 66),
      ERASER("Eraser", "Erase pixels (E)", "Click or drag to erase", 69),
      FILL("Fill", "Fill an area (F)", "Fills touching same color", 70),
      LINE("Line", "Straight line (L)", "Drag, Shift snaps angles", 76),
      BOX("Box", "Rectangle (R)", "Drag, Shift makes squares", 82),
      CIRCLE("Circle", "Circle (C)", "Drag, Shift makes circles", 67),
      PICKER("Picker", "Copy a color (I)", "Click a pixel to copy", 73);

      final String init_Str;
      final String load_Str;
      final String read_Str;
      final int init_I;

      Inner_ConfigStore(String label, String tip, String hint, int key) {
         this.init_Str = label;
         this.load_Str = tip;
         this.read_Str = hint;
         this.init_I = key;
      }

      boolean init() {
         return this == LINE || this == BOX || this == CIRCLE;
      }
   }

   private record Inner_DataUtil(
      autismclient.gui.f.NetHelper init_NetHelper,
      CrosshairEditorScreen.Inner_StreamUtil init_NetHelper_StreamUt,
      String init_Str,
      boolean init_Z,
      autismclient.gui.f.b.CachePool.Inner_DataUtil init_CachePool_DataUtil,
      int argb,
      String load,
      Runnable init_Runnable
   ) {
   }

   private enum Inner_StreamUtil {
      CHIP,
      BUTTON,
      SWATCH;
   }
}
