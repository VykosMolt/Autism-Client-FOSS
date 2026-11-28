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
   private static final int[] PALETTE_COLORS = new int[]{
      16777215, 13158600, 8026746, 0, 16726843, 16747039, 16769333, 9240379, 2873707, 2876384, 3909631, 3890175, 10178047, 16731608, 16748468, 9132587
   };
   private final Screen parent;
   private final CrosshairModule module;
   private final CrosshairPixelArt originalArt;
   private final ArrayDeque<CrosshairEditorScreen.Inner_BufferHelper> undoStack = new ArrayDeque<>();
   private final ArrayDeque<CrosshairEditorScreen.Inner_BufferHelper> redoStack = new ArrayDeque<>();
   private final List<CrosshairEditorScreen.Inner_DataUtil> controls = new ArrayList<>();
   private final List<autismclient.gui.f.NetHelper> sectionBounds = new ArrayList<>();
   private final List<String> sectionTitles = new ArrayList<>();
   private int canvasSize;
   private int[] pixels;
   private CrosshairEditorScreen.Inner_ConfigStore option;
   private CrosshairEditorScreen.Inner_ConfigStore toolBeforePicker;
   private int brushSize;
   private boolean mirrorX;
   private boolean mirrorY;
   private boolean fillShapes;
   private boolean showGrid;
   private int colorRgb;
   private int opacityPercent;
   private int strokeButton;
   private int[] strokeStartPixels;
   private int strokeStartX;
   private int strokeStartY;
   private int strokeLastX;
   private int strokeLastY;
   private boolean draggingOpacitySlider;
   private autismclient.gui.ui.widgets.ColorPickerPopup colorPopup;
   private autismclient.gui.f.NetHelper customColorSwatchBounds;
   private int brushLabelY;
   private int startFromLabelY;
   private CrosshairPixelArt cachedArt;
   private DynamicTexture canvasTexture;
   private Identifier canvasTextureId;
   private int canvasTextureSize;
   private boolean canvasDirty;
   private int canvasTextureSerial;
   private int hoverCellX;
   private int hoverCellY;

   public CrosshairEditorScreen(Screen parent, CrosshairModule module) {
      this(parent, module, module.art());
   }

   public CrosshairEditorScreen(Screen parent, CrosshairModule module, CrosshairPixelArt start) {
      super(Component.literal("Crosshair Editor"));
      this.option = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      this.toolBeforePicker = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      this.brushSize = 1;
      this.showGrid = true;
      this.colorRgb = 16777215;
      this.opacityPercent = 100;
      this.strokeButton = -1;
      this.canvasDirty = true;
      this.hoverCellX = -1;
      this.hoverCellY = -1;
      this.parent = parent;
      this.module = module;
      this.originalArt = module.art();
      this.canvasSize = start.size();
      this.pixels = start.pixels();
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   public double[] cellScreenCenter(int cellX, int cellY) {
      CrosshairEditorScreen.Inner_CachePool layout = this.computeLayout();
      double scale = cA.init_F();
      return new double[]{
         (layout.canvas().read() + (cellX + 0.5) * layout.cellSize()) * scale,
         (layout.canvas().write() + (cellY + 0.5) * layout.cellSize()) * scale
      };
   }

   private CrosshairEditorScreen.Inner_CachePool computeLayout() {
      this.controls.clear();
      this.sectionBounds.clear();
      this.sectionTitles.clear();
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
      int cx = x + 6 + this.textWidth("Resolution") + 4;

      for (int option : CrosshairPixelArt.SIZES) {
         int chipW = this.textWidth(Integer.toString(option)) + 12;
         this.controls
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(cx, rowY, chipW, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  Integer.toString(option),
                  this.canvasSize == option,
                  null,
                  0,
                  option + " by " + option + " pixels",
                  () -> this.resizeCanvas(option)
               )
            );
         cx += chipW + gap;
      }

      cx += 6;
      cx = this.addChip(cx, rowY, ctl, "Mirror X", this.mirrorX, "Mirror left and right (X)", () -> this.mirrorX = !this.mirrorX);
      cx = this.addChip(cx, rowY, ctl, "Mirror Y", this.mirrorY, "Mirror top and bottom (Y)", () -> this.mirrorY = !this.mirrorY);
      this.addChip(cx, rowY, ctl, "Grid", this.showGrid, "Show the grid (G)", () -> this.showGrid = !this.showGrid);
      int rx = x + w - 6;
      rx = this.addRightButton(rx, rowY, ctl, "Clear", autismclient.gui.f.b.CachePool.Inner_DataUtil.update, "Erase everything", this::clearCanvas);
      rx = this.addRightButton(rx, rowY, ctl, "Redo", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, "Redo (Ctrl+Y)", this::redoStroke);
      this.addRightButton(rx, rowY, ctl, "Undo", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, "Undo (Ctrl+Z)", this::undoStroke);
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
         this.controls
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + i % 2 * (half + gap), ty + i / 2 * (ctl + gap), half, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  option.init_Str,
                  this.option == option,
                  null,
                  0,
                  option.load_Str,
                  () -> this.selectTool(option)
               )
            );
      }

      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(tx + half + gap, ty + tools.length / 2 * (ctl + gap), half, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.CHIP,
               "Filled",
               this.fillShapes,
               null,
               0,
               "Solid boxes and circles",
               () -> this.fillShapes = !this.fillShapes
            )
         );
      ty += (tools.length + 2) / 2 * (ctl + gap) - gap + 6;
      int rowText = autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
      this.brushLabelY = ty;
      ty += rowText;
      int brushW = (84 - gap * 3) / 4;

      for (int b = 1; b <= 4; b++) {
         int value = b;
         this.controls
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + (b - 1) * (brushW + gap), ty, brushW, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.CHIP,
                  Integer.toString(b),
                  this.brushSize == b,
                  null,
                  0,
                  "Brush size (scroll)",
                  () -> this.brushSize = value
               )
            );
      }

      ty += ctl + 6;
      this.startFromLabelY = ty;
      ty += rowText;
      CrosshairPresetGeometry.Inner_NetHelper[] presets = new CrosshairPresetGeometry.Inner_NetHelper[]{
         CrosshairPresetGeometry.Inner_NetHelper.CROSS, CrosshairPresetGeometry.Inner_NetHelper.TACTICAL, CrosshairPresetGeometry.Inner_NetHelper.CIRCLE, CrosshairPresetGeometry.Inner_NetHelper.CHEVRON
      };

      for (int i = 0; i < presets.length; i++) {
         CrosshairPresetGeometry.Inner_NetHelper preset = presets[i];
         this.controls
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(tx + i % 2 * (half + gap), ty + i / 2 * (ctl + gap), half, ctl),
                  CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
                  preset.init_Str,
                  false,
                  autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
                  0,
                  "Replace with this shape",
                  () -> this.applyPreset(preset)
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
      this.customColorSwatchBounds = autismclient.gui.f.NetHelper.init(sx + 4, iy, 40, 16);
      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(this.customColorSwatchBounds, CrosshairEditorScreen.Inner_StreamUtil.SWATCH, "", false, null, this.currentArgb(), "Pick any color", this::openColorPicker)
         );
      iy += 20;

      for (int i = 0; i < PALETTE_COLORS.length; i++) {
         int color = PALETTE_COLORS[i];
         int col = i % 8;
         int row = i / 8;
         this.controls
            .add(
               new CrosshairEditorScreen.Inner_DataUtil(
                  autismclient.gui.f.NetHelper.init(sx + 4 + col * (swatch + gap), iy + row * (swatch + gap), swatch, swatch),
                  CrosshairEditorScreen.Inner_StreamUtil.SWATCH,
                  "",
                  this.colorRgb == color,
                  null,
                  0xFF000000 | color,
                  null,
                  () -> this.setColorRgb(color)
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
      int cell = Math.max(1, avail / this.canvasSize);
      int canvasSize = cell * this.canvasSize;
      autismclient.gui.f.NetHelper canvas = autismclient.gui.f.NetHelper.init(
         areaX + (areaW - canvasSize) / 2, bodyTop + (bodyH - canvasSize) / 2, canvasSize, canvasSize
      );
      int doneW = this.textWidth("Done") + 16;
      int cancelW = this.textWidth("Cancel") + 16;
      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(x + w - 6 - doneW, bottomY, doneW, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
               "Done",
               false,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.write,
               0,
               "Save and close (Esc)",
               this::saveAndClose
            )
         );
      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(x + w - 6 - doneW - gap - cancelW, bottomY, cancelW, ctl),
               CrosshairEditorScreen.Inner_StreamUtil.BUTTON,
               "Cancel",
               false,
               autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
               0,
               "Close without saving",
               this::cancelAndClose
            )
         );
      autismclient.gui.f.NetHelper status = autismclient.gui.f.NetHelper.init(x + 6, bottomY, Math.max(1, w - 18 - doneW - cancelW - gap), ctl);
      return new CrosshairEditorScreen.Inner_CachePool(autismclient.gui.f.NetHelper.init(x, y, w, h), header, canvas, cell, opacityBounds, preview, status);
   }

   private int addChip(int x, int y, int h, String label, boolean selected, String tip, Runnable action) {
      int w = this.textWidth(label) + 12;
      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(autismclient.gui.f.NetHelper.init(x, y, w, h), CrosshairEditorScreen.Inner_StreamUtil.CHIP, label, selected, null, 0, tip, action)
         );
      return x + w + 2;
   }

   private int addRightButton(int right, int y, int h, String label, autismclient.gui.f.b.CachePool.Inner_DataUtil tone, String tip, Runnable action) {
      int w = this.textWidth(label) + 14;
      this.controls
         .add(
            new CrosshairEditorScreen.Inner_DataUtil(
               autismclient.gui.f.NetHelper.init(right - w, y, w, h), CrosshairEditorScreen.Inner_StreamUtil.BUTTON, label, false, tone, 0, tip, action
            )
         );
      return right - w - 2;
   }

   private void init(autismclient.gui.f.NetHelper bounds, String title) {
      this.sectionBounds.add(bounds);
      this.sectionTitles.add(title);
   }

   private int textWidth(String text) {
      return autismclient.gui.f.UiHelper.init(this.font).measureWidth(text);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         CrosshairEditorScreen.Inner_CachePool layout = this.computeLayout();
         autismclient.gui.f.SyncManager context = autismclient.gui.f.UiHelper.init(
            graphics, this.font, this.colorPopup == null ? mx : -10000, this.colorPopup == null ? my : -10000
         );
         autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
         autismclient.gui.f.NetHelper window = layout.window();
         autismclient.gui.ui.widgets.DialogFrameRenderer.init(
            context,
            window,
            layout.header(),
            "Crosshair Editor",
            this.colorPopup == null && my >= window.write() && my < window.write() + layout.header() && window.init_Z(mx, my)
         );

         for (int i = 0; i < this.sectionBounds.size(); i++) {
            autismclient.gui.f.b.BufferHelper.init_V(context, this.sectionBounds.get(i), this.sectionTitles.get(i));
         }

         int rowY = window.write() + layout.header() + 6;
         int ctl = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         context.init_PoolEntry()
            .draw(
               graphics, "Resolution", window.read() + 6, context.init_PoolEntry().centerTextY(autismclient.gui.f.NetHelper.init(0, rowY, 1, ctl)), colors.textSecondary
            );
         context.init_PoolEntry().draw(graphics, "Brush", window.read() + 6, this.brushLabelY, colors.textSecondary);
         context.init_PoolEntry().draw(graphics, "Start From", window.read() + 6, this.startFromLabelY, colors.textSecondary);
         this.renderCanvas(context, layout, mx, my);
         CrosshairEditorScreen.Inner_DataUtil hovered = null;

         for (CrosshairEditorScreen.Inner_DataUtil control : this.controls) {
            boolean over = this.colorPopup == null && control.bounds().init_Z(mx, my);
            if (over) {
               hovered = control;
            }

            switch (control.kind()) {
               case CHIP:
                  autismclient.gui.ui.widgets.PillBadgeRenderer.init(context, control.bounds(), control.label(), control.selected(), over);
                  break;
               case BUTTON:
                  autismclient.gui.f.b.CachePool.init(context, control.bounds(), control.label(), control.tone(), over, false);
                  break;
               case SWATCH:
                  this.renderSwatch(context, control.bounds(), control.argb(), control.selected(), over);
            }
         }

         autismclient.gui.f.NetHelper opacityBounds = layout.read();
         context.init_PoolEntry()
            .draw(
               graphics, "Opacity " + this.opacityPercent + "%", opacityBounds.read(), opacityBounds.write() - autismclient.gui.ui.UiDensityMetrics.scaledHeight(12) + 1, colors.textSecondary
            );
         autismclient.gui.ui.widgets.SliderRenderer.init(
            context,
            opacityBounds,
            autismclient.gui.ui.widgets.SliderRenderer.ratio(this.opacityPercent, 5.0, 100.0),
            this.colorPopup == null && (this.draggingOpacitySlider || opacityBounds.init_Z(mx, my))
         );
         String status = this.hoverCellX >= 0 ? this.option.read_Str + "   " + this.hoverCellX + ", " + this.hoverCellY : this.option.read_Str;
         context.init_PoolEntry()
            .drawFitted(graphics, status, layout.update().read(), context.init_PoolEntry().centerTextY(layout.update()), layout.update().update(), colors.textSecondary);
         if (layout.write() != null) {
            this.init(context, layout.write());
         }

         if (hovered != null && hovered.load() != null && this.strokeButton < 0) {
            graphics.nextStratum();
            TooltipRenderer.init(context, hovered.load(), mx, my);
         }

         if (this.colorPopup != null) {
            graphics.nextStratum();
            this.colorPopup.render(autismclient.gui.f.UiHelper.init(graphics, this.font, mx, my));
            if (!this.colorPopup.isOpen()) {
               this.colorPopup = null;
            }
         }
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private void renderCanvas(autismclient.gui.f.SyncManager context, CrosshairEditorScreen.Inner_CachePool layout, int mx, int my) {
      GuiGraphicsExtractor graphics = context.init_GuiGraphicsExtract();
      autismclient.gui.ui.UiColorScheme colors = context.init_TableIndex().colors();
      autismclient.gui.f.NetHelper canvas = layout.canvas();
      int cell = layout.cellSize();
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

      this.uploadCanvasTexture();
      if (this.canvasTextureId != null) {
         graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            this.canvasTextureId,
            canvas.read(),
            canvas.write(),
            0.0F,
            0.0F,
            canvas.update(),
            canvas.sync(),
            this.canvasSize,
            this.canvasSize,
            this.canvasSize,
            this.canvasSize
         );
      }

      if (this.showGrid && cell >= 4) {
         for (int i = 1; i < this.canvasSize; i++) {
            autismclient.gui.f.PacketUtil.load(graphics, canvas.read() + i * cell, canvas.write(), canvas.sync(), 486539263);
            autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, canvas.read(), canvas.write() + i * cell, canvas.update(), 486539263);
         }
      }

      if (this.showGrid) {
         int guide = colors.accent & 16777215 | -2147483648;
         autismclient.gui.f.PacketUtil.load(graphics, canvas.read() + canvas.update() / 2, canvas.write(), canvas.sync(), guide);
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, canvas.read(), canvas.write() + canvas.sync() / 2, canvas.update(), guide);
      }

      this.hoverCellX = -1;
      this.hoverCellY = -1;
      if (this.colorPopup == null && canvas.init_Z(mx, my)) {
         this.hoverCellX = Math.min(this.canvasSize - 1, (mx - canvas.read()) / cell);
         this.hoverCellY = Math.min(this.canvasSize - 1, (my - canvas.write()) / cell);
         if (this.strokeButton < 0) {
            int mark = this.option == CrosshairEditorScreen.Inner_ConfigStore.ERASER
               ? 1728007501
               : (
                  this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER
                        && this.option != CrosshairEditorScreen.Inner_ConfigStore.FILL
                     ? this.currentArgb() & 16777215 | -1728053248
                     : 1728053247
               );
            int span = this.option != CrosshairEditorScreen.Inner_ConfigStore.PEN
                  && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER
                  && !this.option.isShapeTool()
               ? 1
               : this.brushSize;

            for (int[] point : this.brushPoints(this.hoverCellX, this.hoverCellY, span)) {
               autismclient.gui.f.PacketUtil.init(
                  graphics, autismclient.gui.f.NetHelper.init(canvas.read() + point[0] * cell, canvas.write() + point[1] * cell, cell, cell), mark
               );
            }
         }
      }
   }

   private void renderSwatch(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper bounds, int argb, boolean selected, boolean hovered) {
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
      CrosshairRenderState.Inner_DataUtil look = this.module.init_q_DataUtil_m(this.currentArt());
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

   private void uploadCanvasTexture() {
      if (this.canvasDirty || this.canvasTexture == null) {
         if (this.canvasTexture == null || this.canvasTextureSize != this.canvasSize) {
            this.releaseCanvasTexture();
            NativeImage image = new NativeImage(this.canvasSize, this.canvasSize, true);
            this.canvasTexture = new DynamicTexture(() -> "AUTISM crosshair canvas", image);
            this.canvasTextureId = Identifier.fromNamespaceAndPath("autismclient", "dynamic/crosshair/canvas_" + ++this.canvasTextureSerial);
            this.minecraft.getTextureManager().register(this.canvasTextureId, this.canvasTexture);
            this.canvasTextureSize = this.canvasSize;
         }

         NativeImage image = this.canvasTexture.getPixels();
         if (image != null) {
            for (int y = 0; y < this.canvasSize; y++) {
               for (int x = 0; x < this.canvasSize; x++) {
                  image.setPixel(x, y, this.pixels[y * this.canvasSize + x]);
               }
            }

            this.canvasTexture.upload();
            this.canvasDirty = false;
         }
      }
   }

   private void releaseCanvasTexture() {
      if (this.canvasTextureId != null) {
         try {
            this.minecraft.getTextureManager().release(this.canvasTextureId);
         } catch (Throwable var2) {
         }
      }

      this.canvasTexture = null;
      this.canvasTextureId = null;
   }

   @Override
   public void removed() {
      this.releaseCanvasTexture();
      super.removed();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      int button = Compat263.mouseEventGlfwButton(event);
      if (this.colorPopup != null) {
         this.colorPopup.mouseClicked(mx, my, button);
         if (!this.colorPopup.isOpen()) {
            this.colorPopup = null;
         }

         return true;
      } else {
         CrosshairEditorScreen.Inner_CachePool layout = this.computeLayout();
         if (button == 0 && autismclient.gui.ui.widgets.DialogFrameRenderer.init(layout.window(), layout.header(), mx, my)) {
            this.saveAndClose();
            return true;
         }

         autismclient.gui.f.NetHelper canvas = layout.canvas();
         if (!canvas.init_Z(mx, my)) {
            if (button != 0) {
               return true;
            }

            if (layout.read().load(0, -3, 0, -3).init_Z(mx, my)) {
               this.draggingOpacitySlider = true;
               this.init(mx, layout.read());
               return true;
            }

            for (CrosshairEditorScreen.Inner_DataUtil control : this.controls) {
               if (control.bounds().init_Z(mx, my)) {
                  control.action().run();
                  return true;
               }
            }

            return true;
         } else {
            int cx = Math.min(this.canvasSize - 1, (mx - canvas.read()) / layout.cellSize());
            int cy = Math.min(this.canvasSize - 1, (my - canvas.write()) / layout.cellSize());
            if (button != 2 && (button != 0 || !event.hasAltDown() && this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER)) {
               if (button == 0 || button == 1) {
                  this.beginStroke(button, cx, cy, event.hasShiftDown());
               }
            } else {
               this.pickColorAt(cx, cy);
            }

            return true;
         }
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.colorPopup != null) {
         this.colorPopup.mouseDragged(mx, my, Compat263.mouseEventGlfwButton(event), cA.init_D_D(dragX), cA.init_D_D(dragY));
         return true;
      }

      CrosshairEditorScreen.Inner_CachePool layout = this.computeLayout();
      if (this.draggingOpacitySlider) {
         this.init(mx, layout.read());
         return true;
      }

      if (this.strokeButton >= 0 && Compat263.mouseEventGlfwButton(event) == this.strokeButton) {
         autismclient.gui.f.NetHelper canvas = layout.canvas();
         int cx = Math.max(0, Math.min(this.canvasSize - 1, Math.floorDiv(mx - canvas.read(), layout.cellSize())));
         int cy = Math.max(0, Math.min(this.canvasSize - 1, Math.floorDiv(my - canvas.write(), layout.cellSize())));
         this.continueStroke(cx, cy, event.hasShiftDown());
      }

      return true;
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (this.colorPopup != null) {
         this.colorPopup.load(mx, my, Compat263.mouseEventGlfwButton(event));
         if (!this.colorPopup.isOpen()) {
            this.colorPopup = null;
         }

         return true;
      } else {
         if (Compat263.mouseEventGlfwButton(event) == 0) {
            this.draggingOpacitySlider = false;
         }

         if (this.strokeButton >= 0 && Compat263.mouseEventGlfwButton(event) == this.strokeButton) {
            this.endStroke();
         }

         return true;
      }
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      if (this.colorPopup != null) {
         return true;
      }

      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      if (this.computeLayout().canvas().init_Z(mx, my) && vertical != 0.0) {
         this.brushSize = Math.max(1, Math.min(4, this.brushSize + (vertical > 0.0 ? 1 : -1)));
      }

      return true;
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      if (this.colorPopup != null) {
         this.colorPopup.read(Compat263.keyEventGlfwKey(event), event.keycode(), Compat263.keyEventGlfwModifiers(event));
         if (!this.colorPopup.isOpen()) {
            this.colorPopup = null;
         }

         return true;
      } else {
         int key = Compat263.keyEventGlfwKey(event);
         if (key == 256) {
            if (this.strokeButton >= 0) {
               this.cancelStroke();
            } else {
               this.saveAndClose();
            }

            return true;
         } else {
            if (this.strokeButton >= 0) {
               return true;
            }

            if (!event.hasControlDown()) {
               for (CrosshairEditorScreen.Inner_ConfigStore option : CrosshairEditorScreen.Inner_ConfigStore.values()) {
                  if (option.init_I == key) {
                     this.selectTool(option);
                     return true;
                  }
               }

               switch (key) {
                  case 71:
                     this.showGrid = !this.showGrid;
                     break;
                  case 88:
                     this.mirrorX = !this.mirrorX;
                     break;
                  case 89:
                     this.mirrorY = !this.mirrorY;
                     break;
                  case 91:
                     this.brushSize = Math.max(1, this.brushSize - 1);
                     break;
                  case 93:
                     this.brushSize = Math.min(4, this.brushSize + 1);
                     break;
                  default:
                     return super.keyPressed(event);
               }

               return true;
            } else {
               if ((key != 90 || !event.hasShiftDown()) && key != 89) {
                  if (key == 90) {
                     this.undoStroke();
                  }
               } else {
                  this.redoStroke();
               }

               return true;
            }
         }
      }
   }

   @Override
   public void onClose() {
      this.saveAndClose();
   }

   private void selectTool(CrosshairEditorScreen.Inner_ConfigStore option) {
      if (option == CrosshairEditorScreen.Inner_ConfigStore.PICKER && this.option != CrosshairEditorScreen.Inner_ConfigStore.PICKER) {
         this.toolBeforePicker = this.option;
      }

      this.option = option;
   }

   private int currentArgb() {
      return Math.round(this.opacityPercent * 2.55F) << 24 | this.colorRgb & 16777215;
   }

   private void setColorRgb(int color) {
      this.colorRgb = color & 16777215;
      this.leavePickerTool();
   }

   private void leavePickerTool() {
      if (this.option == CrosshairEditorScreen.Inner_ConfigStore.PICKER) {
         this.option = this.toolBeforePicker == CrosshairEditorScreen.Inner_ConfigStore.ERASER
            ? CrosshairEditorScreen.Inner_ConfigStore.PEN
            : this.toolBeforePicker;
      } else if (this.option == CrosshairEditorScreen.Inner_ConfigStore.ERASER) {
         this.option = CrosshairEditorScreen.Inner_ConfigStore.PEN;
      }
   }

   private void init(int mouseX, autismclient.gui.f.NetHelper bounds) {
      this.opacityPercent = (int)Math.round(autismclient.gui.ui.widgets.SliderRenderer.init(mouseX, bounds.read(), bounds.update(), 5.0, 100.0, 5.0));
   }

   private void openColorPicker() {
      this.colorPopup = new autismclient.gui.ui.widgets.ColorPickerPopup(
         this.customColorSwatchBounds, 0xFF000000 | this.colorRgb, -1, this.virtualWidth(), this.virtualHeight(), this::setColorRgb
      );
   }

   private void pickColorAt(int x, int y) {
      int p = this.pixels[y * this.canvasSize + x];
      if (p >>> 24 != 0) {
         this.colorRgb = p & 16777215;
         this.opacityPercent = Math.max(5, Math.min(100, Math.round((p >>> 24) / 2.55F / 5.0F) * 5));
         this.leavePickerTool();
      }
   }

   private void beginStroke(int button, int x, int y, boolean shift) {
      this.strokeButton = button;
      this.strokeStartPixels = (int[])this.pixels.clone();
      this.strokeStartX = this.strokeLastX = x;
      this.strokeStartY = this.strokeLastY = y;
      int paint = this.strokePaint();
      switch (this.option) {
         case FILL:
            for (int[] seed : this.mirroredPoints(x, y)) {
               this.floodFill(seed[0], seed[1], paint);
            }
            break;
         case LINE:
         case BOX:
         case CIRCLE:
            this.previewShape(x, y, shift);
            break;
         default:
            this.paintBrush(x, y, paint);
      }

      this.markCanvasDirty();
   }

   private void continueStroke(int x, int y, boolean shift) {
      if (x != this.strokeLastX || y != this.strokeLastY || this.option.isShapeTool()) {
         switch (this.option) {
            case PEN:
            case ERASER:
               this.drawLine(this.strokeLastX, this.strokeLastY, x, y, this.strokePaint());
               break;
            case FILL:
            default:
               return;
            case LINE:
            case BOX:
            case CIRCLE:
               this.pixels = (int[])this.strokeStartPixels.clone();
               this.previewShape(x, y, shift);
         }

         this.strokeLastX = x;
         this.strokeLastY = y;
         this.markCanvasDirty();
      }
   }

   private void endStroke() {
      if (this.strokeStartPixels != null && !Arrays.equals(this.strokeStartPixels, this.pixels)) {
         this.pushUndo(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, this.strokeStartPixels));
      }

      this.strokeButton = -1;
      this.strokeStartPixels = null;
   }

   private void cancelStroke() {
      if (this.strokeStartPixels != null) {
         this.pixels = this.strokeStartPixels;
      }

      this.strokeButton = -1;
      this.strokeStartPixels = null;
      this.markCanvasDirty();
   }

   private int strokePaint() {
      return this.strokeButton != 1 && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER ? this.currentArgb() : 0;
   }

   private void previewShape(int x, int y, boolean shift) {
      int paint = this.strokePaint();
      int dx = x - this.strokeStartX;
      int dy = y - this.strokeStartY;
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

            this.drawLine(this.strokeStartX, this.strokeStartY, this.clampCell(this.strokeStartX + dx), this.clampCell(this.strokeStartY + dy), paint);
            break;
         case BOX:
         case CIRCLE:
            if (shift) {
               int d = Math.max(Math.abs(dx), Math.abs(dy));
               dx = (dx < 0 ? -1 : 1) * d;
               dy = (dy < 0 ? -1 : 1) * d;
            }

            int x0 = Math.min(this.strokeStartX, this.clampCell(this.strokeStartX + dx));
            int x1 = Math.max(this.strokeStartX, this.clampCell(this.strokeStartX + dx));
            int y0 = Math.min(this.strokeStartY, this.clampCell(this.strokeStartY + dy));
            int y1 = Math.max(this.strokeStartY, this.clampCell(this.strokeStartY + dy));
            if (this.option == CrosshairEditorScreen.Inner_ConfigStore.BOX) {
               this.drawBox(x0, y0, x1, y1, paint);
            } else {
               this.drawEllipse(x0, y0, x1, y1, paint);
            }
      }
   }

   private void drawBox(int x0, int y0, int x1, int y1, int paint) {
      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            boolean edge = x == x0 || x == x1 || y == y0 || y == y1;
            if (this.fillShapes) {
               this.paintMirrored(x, y, paint);
            } else if (edge) {
               this.paintBrush(x, y, paint);
            }
         }
      }
   }

   private void drawEllipse(int x0, int y0, int x1, int y1, int paint) {
      double cx = (x0 + x1 + 1) / 2.0;
      double cy = (y0 + y1 + 1) / 2.0;
      double rx = (x1 - x0 + 1) / 2.0;
      double ry = (y1 - y0 + 1) / 2.0;

      for (int y = y0; y <= y1; y++) {
         for (int x = x0; x <= x1; x++) {
            if (insideEllipse(x, y, cx, cy, rx, ry)) {
               boolean edge = !insideEllipse(x - 1, y, cx, cy, rx, ry)
                  || !insideEllipse(x + 1, y, cx, cy, rx, ry)
                  || !insideEllipse(x, y - 1, cx, cy, rx, ry)
                  || !insideEllipse(x, y + 1, cx, cy, rx, ry);
               if (this.fillShapes) {
                  this.paintMirrored(x, y, paint);
               } else if (edge) {
                  this.paintBrush(x, y, paint);
               }
            }
         }
      }
   }

   private static boolean insideEllipse(int x, int y, double cx, double cy, double rx, double ry) {
      double nx = (x + 0.5 - cx) / rx;
      double ny = (y + 0.5 - cy) / ry;
      return nx * nx + ny * ny <= 1.0;
   }

   private void drawLine(int x0, int y0, int x1, int y1, int paint) {
      int dx = Math.abs(x1 - x0);
      int dy = -Math.abs(y1 - y0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int err = dx + dy;
      int x = x0;
      int y = y0;

      while (true) {
         this.paintBrush(x, y, paint);
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

   private void paintBrush(int x, int y, int paint) {
      int span = !this.option.isShapeTool()
            && this.option != CrosshairEditorScreen.Inner_ConfigStore.PEN
            && this.option != CrosshairEditorScreen.Inner_ConfigStore.ERASER
         ? 1
         : this.brushSize;
      int from = -(span - 1) / 2;

      for (int oy = from; oy < from + span; oy++) {
         for (int ox = from; ox < from + span; ox++) {
            this.paintMirrored(x + ox, y + oy, paint);
         }
      }
   }

   private void paintMirrored(int x, int y, int paint) {
      for (int[] point : this.mirroredPoints(x, y)) {
         if (point[0] >= 0 && point[1] >= 0 && point[0] < this.canvasSize && point[1] < this.canvasSize) {
            this.pixels[point[1] * this.canvasSize + point[0]] = paint;
         }
      }
   }

   private List<int[]> mirroredPoints(int x, int y) {
      List<int[]> points = new ArrayList<>(4);
      points.add(new int[]{x, y});
      if (this.mirrorX) {
         points.add(new int[]{this.canvasSize - 1 - x, y});
      }

      if (this.mirrorY) {
         points.add(new int[]{x, this.canvasSize - 1 - y});
      }

      if (this.mirrorX && this.mirrorY) {
         points.add(new int[]{this.canvasSize - 1 - x, this.canvasSize - 1 - y});
      }

      return points;
   }

   private List<int[]> brushPoints(int x, int y, int span) {
      List<int[]> points = new ArrayList<>();
      int from = -(span - 1) / 2;

      for (int oy = from; oy < from + span; oy++) {
         for (int ox = from; ox < from + span; ox++) {
            for (int[] point : this.mirroredPoints(x + ox, y + oy)) {
               if (point[0] >= 0 && point[1] >= 0 && point[0] < this.canvasSize && point[1] < this.canvasSize) {
                  points.add(point);
               }
            }
         }
      }

      return points;
   }

   private void floodFill(int x, int y, int paint) {
      int target = this.pixels[y * this.canvasSize + x];
      if (target != paint) {
         ArrayDeque<Integer> queue = new ArrayDeque<>();
         queue.add(y * this.canvasSize + x);
         this.pixels[y * this.canvasSize + x] = paint;

         while (!queue.isEmpty()) {
            int i = queue.poll();
            int px = i % this.canvasSize;
            int py = i / this.canvasSize;
            int[][] steps = new int[][]{{px - 1, py}, {px + 1, py}, {px, py - 1}, {px, py + 1}};

            for (int[] step : steps) {
               if (step[0] >= 0 && step[1] >= 0 && step[0] < this.canvasSize && step[1] < this.canvasSize) {
                  int n = step[1] * this.canvasSize + step[0];
                  if (this.pixels[n] == target) {
                     this.pixels[n] = paint;
                     queue.add(n);
                  }
               }
            }
         }
      }
   }

   private int clampCell(int value) {
      return Math.max(0, Math.min(this.canvasSize - 1, value));
   }

   private void resizeCanvas(int newSize) {
      if (newSize != this.canvasSize && CrosshairPixelArt.isValidSize(newSize)) {
         this.pushUndo(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, (int[])this.pixels.clone()));
         this.pixels = new CrosshairPixelArt(this.canvasSize, this.pixels).load(newSize).pixels();
         this.canvasSize = newSize;
         this.markCanvasDirty();
      }
   }

   private void applyPreset(CrosshairPresetGeometry.Inner_NetHelper style) {
      this.pushUndo(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, (int[])this.pixels.clone()));
      this.pixels = CrosshairPresetGeometry.init(style, this.canvasSize, this.module.thickness(), this.module.gap(), this.currentArgb()).pixels();
      this.markCanvasDirty();
   }

   private void clearCanvas() {
      boolean empty = true;

      for (int p : this.pixels) {
         if (p != 0) {
            empty = false;
         }
      }

      if (!empty) {
         this.pushUndo(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, (int[])this.pixels.clone()));
         this.pixels = new int[this.canvasSize * this.canvasSize];
         this.markCanvasDirty();
      }
   }

   private void pushUndo(CrosshairEditorScreen.Inner_BufferHelper snapshot) {
      this.undoStack.push(snapshot);

      while (this.undoStack.size() > 100) {
         this.undoStack.removeLast();
      }

      this.redoStack.clear();
   }

   private void undoStroke() {
      if (!this.undoStack.isEmpty()) {
         this.redoStack.push(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, (int[])this.pixels.clone()));
         this.restoreSnapshot(this.undoStack.pop());
      }
   }

   private void redoStroke() {
      if (!this.redoStack.isEmpty()) {
         this.undoStack.push(new CrosshairEditorScreen.Inner_BufferHelper(this.canvasSize, (int[])this.pixels.clone()));
         this.restoreSnapshot(this.redoStack.pop());
      }
   }

   private void restoreSnapshot(CrosshairEditorScreen.Inner_BufferHelper snapshot) {
      this.canvasSize = snapshot.size();
      this.pixels = (int[])snapshot.pixels().clone();
      this.markCanvasDirty();
   }

   private void markCanvasDirty() {
      this.canvasDirty = true;
      this.cachedArt = null;
   }

   private CrosshairPixelArt currentArt() {
      if (this.cachedArt == null) {
         this.cachedArt = new CrosshairPixelArt(this.canvasSize, this.pixels);
      }

      return this.cachedArt;
   }

   private void saveAndClose() {
      if (this.strokeButton >= 0) {
         this.endStroke();
      }

      CrosshairPixelArt drawn = this.currentArt();
      if (!drawn.equals(this.originalArt)) {
         this.module.setArt(drawn);
      }

      this.minecraft.gui.setScreen(this.parent);
   }

   private void cancelAndClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private record Inner_BufferHelper(int size, int[] pixels) {
   }

   private record Inner_CachePool(
      autismclient.gui.f.NetHelper window,
      int header,
      autismclient.gui.f.NetHelper canvas,
      int cellSize,
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

      boolean isShapeTool() {
         return this == LINE || this == BOX || this == CIRCLE;
      }
   }

   private record Inner_DataUtil(
      autismclient.gui.f.NetHelper bounds,
      CrosshairEditorScreen.Inner_StreamUtil kind,
      String label,
      boolean selected,
      autismclient.gui.f.b.CachePool.Inner_DataUtil tone,
      int argb,
      String load,
      Runnable action
   ) {
   }

   private enum Inner_StreamUtil {
      CHIP,
      BUTTON,
      SWATCH;
   }
}
