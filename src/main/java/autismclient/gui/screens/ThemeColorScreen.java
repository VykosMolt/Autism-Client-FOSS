package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.TextFieldRenderer;
import autismclient.gui.ui.widgets.ToggleSwitchRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.modules.Module;
import autismclient.modules.al;
import autismclient.util.AutismConfig;
import autismclient.util.bi;
import autismclient.util.cA;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ThemeColorScreen extends Screen {
   private static final int SELECTED_LABEL_COLOR = -791321;
   private static final int LABEL_COLOR = -4743522;
   private final Screen parent;
   private final AutismConfig config;
   private final AutismConfig.ThemeColors draftColors = new AutismConfig.ThemeColors();
   private ThemePaletteManager.CompiledPalette draftPalette;
   private final autismclient.gui.ui.UiTheme previewTheme = new autismclient.gui.ui.UiTheme();
   private final autismclient.gui.ui.UiTheme load_TableIndex = new autismclient.gui.ui.UiTheme();
   private final ThemeColorScreen.ColorPicker picker = new ThemeColorScreen.ColorPicker();
   private int selectedRow;
   private ThemeTextureRecolorer.RecolorPreview logoTexture;
   private ThemeTextureRecolorer.RecolorPreview panoramaTexture;
   private ThemeTextureRecolorer.RecolorPreview tickTexture;
   private autismclient.gui.f.NetHelper simpleTab;
   private autismclient.gui.f.NetHelper advancedTab;
   private autismclient.gui.f.NetHelper resetButton;
   private autismclient.gui.f.NetHelper cancelButton;
   private autismclient.gui.f.NetHelper applyButton;
   private final List<autismclient.gui.f.NetHelper> init_List = new ArrayList<>();
   private autismclient.gui.f.NetHelper pickerArea;
   private int scrollRow;
   private int firstVisibleRow;
   private int rowHeight = 1;
   private autismclient.gui.f.NetHelper listArea;
   private autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds scrollbar;
   private boolean draggingScrollbar;
   private int scrollbarGrabOffset;
   private List<ThemeColorScreen.ColorRow> cachedRows;
   private boolean cachedRowsAdvanced;
   private List<Module> read_List;
   private int cachedListRevision = -2147483648;
   private static final int open = 240;
   private static final int poll = 224;

   public ThemeColorScreen(Screen parent) {
      super(Component.literal("Theme Color"));
      this.parent = parent;
      this.config = AutismConfig.getGlobal();
      copyColors(this.config.themeColors, this.draftColors);
      this.recompilePalette();
      this.loadSelectedRow();
   }

   @Override
   protected void init() {
      if (this.logoTexture == null) {
         this.logoTexture = ThemeTextureRecolorer.init(
            Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/autism_client_logo.png"), ThemePaletteManager.ColorRole.ACCENT, 2048
         );
      }

      if (this.panoramaTexture == null) {
         this.panoramaTexture = ThemeTextureRecolorer.init(
            Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama_0.png"), ThemePaletteManager.ColorRole.BACKDROP, 640
         );
      }

      if (this.tickTexture == null) {
         this.tickTexture = ThemeTextureRecolorer.init(autismclient.gui.f.a.DataUtil.tick, ThemePaletteManager.ColorRole.ACCENT, 128);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private List<ThemeColorScreen.ColorRow> colorRows() {
      if (this.cachedRows != null && this.cachedRowsAdvanced == this.draftColors.advanced) {
         return this.cachedRows;
      }

      List<ThemeColorScreen.ColorRow> rows = new ArrayList<>();
      if (!this.draftColors.advanced) {
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Theme Color", () -> this.draftColors.master, v -> this.draftColors.master = v, ThemePaletteManager.ColorRole.ACCENT
            )
         );
      } else {
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Accent", () -> this.draftColors.accent, v -> this.draftColors.accent = v, ThemePaletteManager.ColorRole.ACCENT
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Button Fill", () -> this.draftColors.button, v -> this.draftColors.button = v, ThemePaletteManager.ColorRole.BUTTON
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Outline / Border", () -> this.draftColors.outline, v -> this.draftColors.outline = v, ThemePaletteManager.ColorRole.OUTLINE
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Header", () -> this.draftColors.header, v -> this.draftColors.header = v, ThemePaletteManager.ColorRole.HEADER
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Toggle / Highlight", () -> this.draftColors.toggle, v -> this.draftColors.toggle = v, ThemePaletteManager.ColorRole.TOGGLE
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Hover / Glow", () -> this.draftColors.hover, v -> this.draftColors.hover = v, ThemePaletteManager.ColorRole.HOVER
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow("Text", () -> this.draftColors.text, v -> this.draftColors.text = v, ThemePaletteManager.ColorRole.TEXT)
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Background / Panorama", () -> this.draftColors.backdrop, v -> this.draftColors.backdrop = v, ThemePaletteManager.ColorRole.BACKDROP
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Danger", () -> this.draftColors.danger, v -> this.draftColors.danger = v, ThemePaletteManager.ColorRole.DANGER
            )
         );
         rows.add(
            new ThemeColorScreen.ColorRow(
               "Success", () -> this.draftColors.success, v -> this.draftColors.success = v, ThemePaletteManager.ColorRole.SUCCESS
            )
         );
      }

      this.cachedRows = rows;
      this.cachedRowsAdvanced = this.draftColors.advanced;
      return rows;
   }

   private void recompilePalette() {
      this.draftPalette = ThemePaletteManager.CompiledPalette.compile(this.draftColors);
      this.previewTheme.colors().applyPalette(this.draftPalette);
      this.load_TableIndex.colors().applyPalette(this.draftPalette);
      this.load_TableIndex.colors().accent = this.recolor(-50373, ThemePaletteManager.ColorRole.TOGGLE);
      autismclient.gui.ui.UiColorScheme colors = this.previewTheme.colors();
      colors.headerSurface = UiLayoutMath.lerpColor(colors.headerSurface, this.recolor(1493121851, ThemePaletteManager.ColorRole.HEADER), 0.08F);
      colors.buttonSurface = UiLayoutMath.lerpColor(colors.buttonSurface, this.recolor(-930144476, ThemePaletteManager.ColorRole.BUTTON), 0.12F);
      colors.buttonSurfaceHover = UiLayoutMath.lerpColor(colors.buttonSurfaceHover, this.recolor(-721460124, ThemePaletteManager.ColorRole.HOVER), 0.12F);
      colors.borderSubtle = UiLayoutMath.lerpColor(colors.borderSubtle, this.recolor(1023370340, ThemePaletteManager.ColorRole.OUTLINE), 0.15F);
      colors.borderControl = UiLayoutMath.lerpColor(colors.borderControl, this.recolor(1291805796, ThemePaletteManager.ColorRole.OUTLINE), 0.15F);
   }

   private autismclient.gui.f.SyncManager uiContext(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      return this.uiContext(graphics, mouseX, mouseY, this.previewTheme);
   }

   private autismclient.gui.f.SyncManager uiContext(GuiGraphicsExtractor graphics, int mouseX, int mouseY, autismclient.gui.ui.UiTheme theme) {
      autismclient.gui.f.SyncManager base = autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY);
      return new autismclient.gui.f.SyncManager(graphics, theme, base.init_PoolEntry(), base.init_I(), base.load_I(), mouseX, mouseY, base.init_F());
   }

   private void loadSelectedRow() {
      List<ThemeColorScreen.ColorRow> rows = this.colorRows();
      if (this.selectedRow >= rows.size()) {
         this.selectedRow = 0;
      }

      this.picker.setArgb(rows.get(this.selectedRow).getter().getAsInt());
      this.picker.onChange = argb -> {
         this.colorRows().get(this.selectedRow).setter().accept(argb);
         this.recompilePalette();
      };
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      int mx = cA.init_I_D(mouseX);
      int my = cA.init_I_D(mouseY);
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         int sw = cA.init_I();
         int sh = cA.load_I();
         autismclient.gui.f.SyncManager ctx = this.uiContext(graphics, mx, my);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), ctx.init_TableIndex().colors().backdropScrim);
         int margin = 6;
         int contentW = sw - margin * 2;
         int contentH = sh - margin * 2;
         int gap = 4;
         int leftW = Math.max(224, Math.min(240, contentW - gap - 120));
         int rightW = Math.max(1, contentW - leftW - gap);
         int leftH = Math.min(contentH, this.leftColumnHeight());
         ThemeColorScreen.PreviewLayout preview = this.layoutFor(rightW, contentH);
         int rightH = preview.write();
         if (Math.abs(leftH - rightH) <= 8) {
            leftH = rightH = Math.max(leftH, rightH);
         }

         autismclient.gui.f.NetHelper left = autismclient.gui.f.NetHelper.init(margin, margin, leftW, leftH);
         autismclient.gui.f.NetHelper right = autismclient.gui.f.NetHelper.init(margin + leftW + gap, margin, rightW, rightH);
         this.renderColorList(ctx, left, mx, my);
         this.renderPreview(ctx, right, preview);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int init_I() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int controlHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int lineHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int defaultRowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int pickerHeight() {
      return ThemeColorScreen.ColorPicker.panelWidth(this.draftColors.advanced);
   }

   private int leftColumnHeight() {
      return 4 + this.init_I() + this.controlHeight() + 2 + this.lineHeight() + this.colorRows().size() * this.defaultRowHeight() + 4 + this.pickerHeight() + 4 + this.controlHeight() + 4;
   }

   private void renderColorList(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area, int mx, int my) {
      GuiGraphicsExtractor g = ctx.init_GuiGraphicsExtract();
      autismclient.gui.f.PacketUtil.read(g, area, 8, ctx.init_TableIndex().colors().windowSurface, ctx.init_TableIndex().colors().borderSubtle);
      int x = area.read() + 4;
      int y = area.write() + 4;
      int innerW = area.update() - 8;
      int ctrlH = this.controlHeight();
      this.drawLabel(g, "THEME COLOR", autismclient.gui.f.a.DataUtil.read, this.recolor(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
      y += this.init_I();
      int tabW = (innerW - 2) / 2;
      this.simpleTab = autismclient.gui.f.NetHelper.init(x, y, tabW, ctrlH);
      this.advancedTab = autismclient.gui.f.NetHelper.init(x + tabW + 2, y, innerW - tabW - 2, ctrlH);
      this.renderTab(g, this.simpleTab, "Simple", !this.draftColors.advanced, this.simpleTab.init_Z(mx, my));
      this.renderTab(g, this.advancedTab, "Advanced", this.draftColors.advanced, this.advancedTab.init_Z(mx, my));
      y += ctrlH + 2;
      this.drawLabel(
         g,
         this.draftColors.advanced ? "Channels" : "One color recolors the whole theme",
         autismclient.gui.f.a.DataUtil.load,
         this.textColor(-4743522),
         x,
         UiLayoutMath.centerTextY(y, this.lineHeight(), k.init(autismclient.gui.f.a.DataUtil.load), 1)
      );
      y += this.lineHeight();
      this.init_List.clear();
      List<ThemeColorScreen.ColorRow> rows = this.colorRows();
      int rowH = this.defaultRowHeight();
      int footerY = area.load() - 4 - ctrlH;
      int rowSpace = footerY - 4 - ThemeColorScreen.ColorPicker.panelHeight(this.draftColors.advanced) - 4 - y;
      int visible = Math.min(rows.size(), Math.max(Math.min(3, rows.size()), rowSpace / rowH));
      int maxScroll = rows.size() - visible;
      this.scrollRow = Math.max(0, Math.min(this.scrollRow, maxScroll));
      this.firstVisibleRow = this.scrollRow;
      this.rowHeight = rowH;
      this.listArea = autismclient.gui.f.NetHelper.init(x, y, innerW, visible * rowH - 2);
      int rowW = innerW;
      if (maxScroll > 0) {
         rowW = innerW - 6 - 2;
         autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(x + innerW - 6, y, 6, this.listArea.sync());
         this.scrollbar = autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, rows.size() * rowH, visible * rowH, this.scrollRow * rowH);
         autismclient.gui.ui.widgets.ScrollbarRenderer.init(ctx, this.scrollbar, this.scrollbar.init(mx, my), this.draggingScrollbar);
      } else {
         this.scrollbar = null;
         this.draggingScrollbar = false;
      }

      for (int i = this.scrollRow; i < this.scrollRow + visible; i++) {
         ThemeColorScreen.ColorRow c = rows.get(i);
         autismclient.gui.f.NetHelper rb = autismclient.gui.f.NetHelper.init(x, y, rowW, rowH - 2);
         this.init_List.add(rb);
         boolean sel = i == this.selectedRow;
         boolean over = rb.init_Z(mx, my);
         autismclient.gui.f.PacketUtil.load(
            g,
            rb,
            3,
            sel
               ? UiLayoutMath.lerpColor(ctx.init_TableIndex().colors().cardSurface, ctx.init_TableIndex().colors().accent, 0.12F)
               : (over ? ctx.init_TableIndex().colors().cardSurfaceElevated : ctx.init_TableIndex().colors().cardSurface),
            sel ? ctx.init_TableIndex().colors().accent : ctx.init_TableIndex().colors().borderSubtle
         );
         autismclient.gui.f.NetHelper swatch = autismclient.gui.f.NetHelper.init(rb.read() + 3, rb.write() + 2, 20, rb.sync() - 4);
         autismclient.gui.f.PacketUtil.load(g, swatch, 2, 0xFF000000 | c.getter().getAsInt() & 16777215, ctx.init_TableIndex().colors().borderSubtle);
         int textY = UiLayoutMath.centerTextY(rb.write(), rb.sync(), k.init(autismclient.gui.f.a.DataUtil.init), 1);
         this.drawLabel(g, c.label(), autismclient.gui.f.a.DataUtil.init, sel ? this.textColor(-791321) : this.textColor(-4743522), swatch.init() + 4 + 1, textY);
         String hex = hexString(c.getter().getAsInt());
         int hexW = k.init(this.font, hex, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.drawLabel(g, hex, autismclient.gui.f.a.DataUtil.init, this.textColor(-4743522), rb.init() - hexW - 4, textY);
         y += rowH;
      }

      y += 4;
      this.picker.showAlpha = this.draftColors.advanced;
      this.picker.palette = this.draftPalette;
      this.pickerArea = autismclient.gui.f.NetHelper.init(
         x, y, innerW, Math.max(ThemeColorScreen.ColorPicker.panelHeight(this.draftColors.advanced), Math.min(this.pickerHeight(), footerY - 4 - y))
      );
      this.picker.renderPicker(ctx, this.pickerArea);
      int bw = (innerW - 4) / 3;
      this.resetButton = autismclient.gui.f.NetHelper.init(x, footerY, bw, ctrlH);
      this.cancelButton = autismclient.gui.f.NetHelper.init(x + bw + 2, footerY, bw, ctrlH);
      this.applyButton = autismclient.gui.f.NetHelper.init(x + (bw + 2) * 2, footerY, innerW - (bw + 2) * 2, ctrlH);
      this.renderOutlinedButton(g, this.resetButton, "Reset", this.recolor(1717972012, ThemePaletteManager.ColorRole.OUTLINE), this.resetButton.init_Z(mx, my));
      this.renderOutlinedButton(g, this.cancelButton, "Cancel", this.recolor(1717972012, ThemePaletteManager.ColorRole.OUTLINE), this.cancelButton.init_Z(mx, my));
      this.renderOutlinedButton(g, this.applyButton, "Apply", this.recolor(-39836, ThemePaletteManager.ColorRole.ACCENT), this.applyButton.init_Z(mx, my));
   }

   private ThemeColorScreen.PreviewLayout layoutFor(int panelW, int budget) {
      int innerW = panelW - 8;
      int logoW = this.logoTexture != null ? Math.min(innerW, 340) : 0;
      int logoH = this.logoTexture != null && this.logoTexture.width() > 0
         ? Math.max(1, Math.round(logoW * ((float)this.logoTexture.height() / this.logoTexture.width())))
         : 0;
      int h = this.previewHeaderHeight() + this.previewBodyHeight() + 4;
      boolean showLogo = logoH > 0 && h + logoH + 2 <= budget;
      if (showLogo) {
         h += logoH + 2;
      }

      int bannerH = 0;
      if (this.panoramaTexture != null) {
         int bannerGap = 8;
         int bh = Math.min(Math.round(innerW * 0.56F), budget - h - bannerGap);
         if (bh >= 40) {
            bannerH = bh;
            h += bh + bannerGap;
         }
      }

      return new ThemeColorScreen.PreviewLayout(logoW, logoH, showLogo, bannerH, Math.min(h, budget));
   }

   private int previewBarHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(18);
   }

   private int previewHeaderHeight() {
      return 4 + this.init_I() + this.previewBarHeight() + 2 + 8;
   }

   private void renderPreview(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area, ThemeColorScreen.PreviewLayout layout) {
      GuiGraphicsExtractor g = ctx.init_GuiGraphicsExtract();
      autismclient.gui.f.PacketUtil.read(g, area, 8, ctx.init_TableIndex().colors().windowSurface, ctx.init_TableIndex().colors().borderSubtle);
      int x = area.read() + 4;
      int y = area.write() + 4;
      int innerW = area.update() - 8;
      this.drawLabel(g, "LIVE PREVIEW", autismclient.gui.f.a.DataUtil.load, this.recolor(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
      y += this.init_I();
      int barH = this.previewBarHeight();
      autismclient.gui.f.PacketUtil.read(
         g, autismclient.gui.f.NetHelper.init(x, y, innerW, barH), 5, ctx.init_TableIndex().colors().headerSurface, ctx.init_TableIndex().colors().borderSubtle
      );
      if (this.tickTexture != null) {
         this.tickTexture.applyPalette(this.draftPalette);
         g.blit(
            RenderPipelines.GUI_TEXTURED,
            this.tickTexture.id(),
            x + 4,
            y + (barH - 14) / 2,
            0.0F,
            0.0F,
            14,
            14,
            this.tickTexture.width(),
            this.tickTexture.height(),
            this.tickTexture.width(),
            this.tickTexture.height(),
            -1
         );
      }

      this.drawLabel(
         g,
         "Autism Client",
         autismclient.gui.f.a.DataUtil.load,
         this.recolor(-2828, ThemePaletteManager.ColorRole.TEXT),
         x + 8 + 14,
         this.textBaselineY(y, barH, autismclient.gui.f.a.DataUtil.load)
      );
      y += barH + 2 + 8;
      g.enableScissor(area.read() + 1, area.write() + 1, area.init() - 1, area.load() - 1);

      try {
         this.renderPreviewContents(g, area, x, y, innerW, layout);
      } finally {
         g.disableScissor();
      }
   }

   private void renderPreviewContents(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper area, int x, int y, int innerW, ThemeColorScreen.PreviewLayout layout) {
      int contentLimit = area.load() - 4;
      if (this.panoramaTexture != null) {
         this.panoramaTexture.applyPalette(this.draftPalette);
         int bw = innerW;
         int bh = layout.read();
         if (bh > 0) {
            int regionH = Math.max(1, Math.min(this.panoramaTexture.height(), Math.round(this.panoramaTexture.height() * ((float)bh / bw))));
            float vOff = Math.max(0.0F, (this.panoramaTexture.height() - regionH) / 2.0F);
            g.blit(
               RenderPipelines.GUI_TEXTURED,
               this.panoramaTexture.id(),
               x,
               y,
               0.0F,
               vOff,
               bw,
               bh,
               this.panoramaTexture.width(),
               regionH,
               this.panoramaTexture.width(),
               this.panoramaTexture.height(),
               -1
            );
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, bw, bh), this.recolor(-7392975, ThemePaletteManager.ColorRole.OUTLINE));
            y += bh + 8;
         }
      }

      this.drawLabel(g, "MODULES", autismclient.gui.f.a.DataUtil.load, this.textColor(-4743522), x, y);
      y += this.lineHeight();
      List<Module> preview = this.load_List();

      for (int i = 0; i < preview.size() && y + this.previewRowHeight() <= contentLimit; i++) {
         Module m = preview.get(i);
         String key = m.keybind() >= 0 ? autismclient.commands.arguments.KeyBindArgumentType.keyName(m.keybind()) : "";
         y = this.renderPreviewModuleRow(g, x, y, innerW, m.name(), key, m.isEnabled(), i == 0, i == 1);
      }

      y += 8;
      int ctrlH = this.controlHeight();
      if (y + ctrlH <= contentLimit) {
         this.renderPreviewToggle(g, x, y, ctrlH, true);
         this.renderPreviewToggle(g, x + 30 + 2, y, ctrlH, false);
         int chipX = x + 64 + 4;
         int successW = this.buttonWidth("Success");
         if (chipX + successW <= x + innerW) {
            this.renderPreviewChip(g, chipX, y, successW, ctrlH, "Success", this.recolor(-9448565, ThemePaletteManager.ColorRole.SUCCESS));
         }

         chipX += successW + 2;
         int dangerW = this.buttonWidth("Danger");
         if (chipX + dangerW <= x + innerW) {
            this.renderPreviewChip(g, chipX, y, dangerW, ctrlH, "Danger", this.recolor(-1938838, ThemePaletteManager.ColorRole.DANGER));
         }

         y += ctrlH + 4;
      }

      if (y + ctrlH <= contentLimit) {
         int btnTextW = k.init(this.font, "Button", autismclient.gui.f.a.DataUtil.init, -791321);
         int btnW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(btnTextW);
         autismclient.gui.f.NetHelper btn = autismclient.gui.f.NetHelper.init(x, y, btnW, ctrlH);
         autismclient.gui.f.SyncManager sample = this.uiContext(g, -1, -1);
         autismclient.gui.f.b.CachePool.init(sample, btn, "Button", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, false, false);
         int fieldX = btn.init() + 2;
         autismclient.gui.f.NetHelper field = autismclient.gui.f.NetHelper.init(fieldX, y, Math.max(0, x + innerW - fieldX), ctrlH);
         if (field.update() >= 12) {
            TextFieldRenderer.init(sample, field, "", "Search", false, 0);
         }

         y += ctrlH + 4;
      }

      int lineH = this.lineHeight();
      if (y + lineH <= contentLimit) {
         this.drawLabel(g, "Primary text sample", autismclient.gui.f.a.DataUtil.init, this.recolor(-791321, ThemePaletteManager.ColorRole.TEXT), x, y);
         y += lineH;
      }

      if (y + lineH <= contentLimit) {
         this.drawLabel(g, "Secondary / muted text", autismclient.gui.f.a.DataUtil.init, this.recolor(-4743522, ThemePaletteManager.ColorRole.TEXT), x, y);
         y += lineH;
      }

      if (y + lineH <= contentLimit) {
         this.drawLabel(g, "Accent text", autismclient.gui.f.a.DataUtil.init, this.recolor(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
         y += lineH;
      }

      if (this.logoTexture != null && layout.showLogo()) {
         int logoW = layout.logoWidth();
         int logoH = layout.load();
         int ly = area.load() - 4 - logoH;
         if (ly >= y + 2) {
            this.logoTexture.applyPalette(this.draftPalette);
            int lx = x + (innerW - logoW) / 2;
            g.blit(
               RenderPipelines.GUI_TEXTURED,
               this.logoTexture.id(),
               lx,
               ly,
               0.0F,
               0.0F,
               logoW,
               logoH,
               this.logoTexture.width(),
               this.logoTexture.height(),
               this.logoTexture.width(),
               this.logoTexture.height(),
               -1
            );
         }
      }
   }

   private int previewRowHeight() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int previewBodyHeight() {
      return this.lineHeight() + this.load_List().size() * (this.previewRowHeight() + 2) + 8 + (this.controlHeight() + 4) * 2 + this.lineHeight() * 3;
   }

   private int textBaselineY(int boxY, int boxH, Identifier fontId) {
      return UiLayoutMath.centerTextY(boxY, boxH, k.init(fontId), 1);
   }

   private int renderPreviewModuleRow(GuiGraphicsExtractor g, int x, int y, int w, String name, String keybind, boolean on, boolean selected, boolean hovered) {
      int h = this.previewRowHeight();
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, h);
      autismclient.gui.ui.UiColorScheme colors = this.previewTheme.colors();
      int fill = selected ? UiLayoutMath.lerpColor(colors.cardSurface, this.recolor(-50373, ThemePaletteManager.ColorRole.TOGGLE), 0.12F) : (hovered ? colors.cardSurfaceElevated : colors.cardSurface);
      autismclient.gui.f.PacketUtil.load(g, row, 3, fill);
      if (selected) {
         autismclient.gui.f.PacketUtil.load(g, row, 3, 0, this.recolor(-50373, ThemePaletteManager.ColorRole.TOGGLE));
      }

      int ty = this.textBaselineY(y, h, autismclient.gui.f.a.DataUtil.init);
      String label = name + (keybind != null && !keybind.isEmpty() ? " [" + keybind + "]" : "");
      this.uiContext(g, -1, -1).init_PoolEntry().drawEllipsized(g, label, x + 8, ty, Math.max(1, w - 30 - 16), on ? colors.textPrimary : colors.textSecondary);
      this.renderPreviewToggle(g, x + w - 30 - 4, y, h, on);
      return y + h + 2;
   }

   private static String hexString(int rgb) {
      rgb &= 16777215;
      char[] out = new char[7];
      out[0] = '#';

      for (int i = 6; i >= 1; i--) {
         int nibble = rgb & 15;
         out[i] = (char)(nibble < 10 ? 48 + nibble : 65 + (nibble - 10));
         rgb >>= 4;
      }

      return new String(out);
   }

   private List<Module> load_List() {
      int rev = al.init_I();
      if (this.read_List != null && this.cachedListRevision == rev) {
         return this.read_List;
      }

      List<Module> out = new ArrayList<>();

      for (Module m : al.init_Collection()) {
         if (m.showInModuleMenu()) {
            out.add(m);
            if (out.size() == 4) {
               break;
            }
         }
      }

      this.read_List = out;
      this.cachedListRevision = rev;
      return out;
   }

   private void renderPreviewToggle(GuiGraphicsExtractor g, int x, int y, int h, boolean on) {
      autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(x, y, 30, h);
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(this.uiContext(g, -1, -1, this.load_TableIndex), b, null, on, false, "themePreviewToggle:" + x + ":" + y);
   }

   private int buttonWidth(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
   }

   private void renderPreviewChip(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int color) {
      autismclient.gui.ui.widgets.PillBadgeRenderer.init(this.uiContext(g, -1, -1), autismclient.gui.f.NetHelper.init(x, y, w, h), label, color, true, false);
   }

   private int recolor(int color, ThemePaletteManager.ColorRole ch) {
      return ThemePaletteManager.recolorWithPalette(color, ch, this.draftPalette);
   }

   private int textColor(int color) {
      return this.recolor(color, ThemePaletteManager.ColorRole.TEXT);
   }

   private void renderTab(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper b, String label, boolean active, boolean hover) {
      autismclient.gui.f.b.CachePool.init(
         this.uiContext(g, -1, -1),
         b,
         label,
         active ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         active
      );
   }

   private void renderOutlinedButton(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper b, String label, int border, boolean hover) {
      autismclient.gui.f.b.CachePool.init(
         this.uiContext(g, -1, -1),
         b,
         label,
         border == this.recolor(-39836, ThemePaletteManager.ColorRole.ACCENT)
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read
            : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         false
      );
   }

   private void drawLabel(GuiGraphicsExtractor g, String text, Identifier font, int color, int x, int y) {
      k.init(g, this.font, text, font, color, x, y, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.picker.mouseClicked(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (this.simpleTab != null && this.simpleTab.init_Z(mx, my)) {
         this.setAdvanced(false);
         return true;
      }

      if (this.advancedTab != null && this.advancedTab.init_Z(mx, my)) {
         this.setAdvanced(true);
         return true;
      }

      if (this.scrollbar != null && this.scrollbar.init(mx, my)) {
         this.scrollbarGrabOffset = this.scrollbar.load(mx, my)
            ? my - this.scrollbar.load().write()
            : this.scrollbar.load().sync() / 2;
         this.draggingScrollbar = true;
         this.dragScrollbar(my);
         return true;
      }

      for (int i = 0; i < this.init_List.size(); i++) {
         if (this.init_List.get(i).init_Z(mx, my)) {
            this.selectedRow = this.firstVisibleRow + i;
            this.loadSelectedRow();
            return true;
         }
      }

      if (this.resetButton != null && this.resetButton.init_Z(mx, my)) {
         this.resetDraft();
         return true;
      } else if (this.cancelButton != null && this.cancelButton.init_Z(mx, my)) {
         this.onClose();
         return true;
      } else if (this.applyButton != null && this.applyButton.init_Z(mx, my)) {
         this.applyDraft();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.draggingScrollbar) {
         this.dragScrollbar(cA.init_I_D(event.y()));
         return true;
      } else {
         this.picker.mouseDragged(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy);
         return true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.draggingScrollbar = false;
      this.picker.mouseReleased(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      return true;
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      int mx = cA.init_I_D(x);
      int my = cA.init_I_D(y);
      if (this.scrollbar != null && scrollY != 0.0 && (this.listArea.init_Z(mx, my) || this.scrollbar.init(mx, my))) {
         this.scrollRow = this.scrollRow - (int)Math.signum(scrollY);
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   private void dragScrollbar(int my) {
      if (this.scrollbar != null) {
         this.scrollRow = Math.round((float)autismclient.gui.ui.widgets.ScrollbarRenderer.scrollForDrag(this.scrollbar, my, this.scrollbarGrabOffset) / this.rowHeight);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.picker.keyPressed(input)) {
         return true;
      } else if (Compat263.keyEventGlfwKey(input) == 256) {
         this.onClose();
         return true;
      } else {
         return super.keyPressed(input);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.picker.charTyped(input) ? true : super.charTyped(input);
   }

   private void setAdvanced(boolean advanced) {
      if (this.draftColors.advanced != advanced) {
         if (advanced) {
            int defaultMaster = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
            boolean masterChanged = (this.draftColors.master & 16777215) != (defaultMaster & 16777215)
               || (this.draftColors.master >>> 24 & 0xFF) != 255;
            if (masterChanged) {
               this.draftColors.accent = this.draftColors.master;
               this.draftColors.button = this.draftColors.master;
               this.draftColors.outline = this.draftColors.master;
               this.draftColors.header = this.draftColors.master;
               this.draftColors.toggle = this.draftColors.master;
               this.draftColors.hover = this.draftColors.master;
               this.draftColors.backdrop = this.draftColors.master;
               this.draftColors.danger = this.draftColors.master;
            } else {
               this.draftColors.accent = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
               this.draftColors.button = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.BUTTON.ordinal()];
               this.draftColors.outline = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.OUTLINE.ordinal()];
               this.draftColors.header = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.HEADER.ordinal()];
               this.draftColors.toggle = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.TOGGLE.ordinal()];
               this.draftColors.hover = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.HOVER.ordinal()];
               this.draftColors.backdrop = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.BACKDROP.ordinal()];
               this.draftColors.danger = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.DANGER.ordinal()];
            }
         }

         this.draftColors.advanced = advanced;
         this.selectedRow = 0;
         this.scrollRow = 0;
         this.recompilePalette();
         this.loadSelectedRow();
      }
   }

   private void resetDraft() {
      AutismConfig.ThemeColors def = new AutismConfig.ThemeColors();
      def.advanced = this.draftColors.advanced;
      copyColors(def, this.draftColors);
      this.recompilePalette();
      this.loadSelectedRow();
      bi.load("Theme reset to defaults");
   }

   private void applyDraft() {
      copyColors(this.draftColors, this.config.themeColors);
      ThemePaletteManager.reload();
      Runnable saved = autismclient.gui.ThemeApplyProgressOverlay.init("Saving config");
      autismclient.util.BackgroundTaskExecutor.submit("theme-config-save", () -> {
         try {
            this.config.save();
         } finally {
            saved.run();
         }
      });
      bi.load("Theme applied");
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   @Override
   public void removed() {
      if (this.logoTexture != null) {
         this.logoTexture.close();
         this.logoTexture = null;
      }

      if (this.panoramaTexture != null) {
         this.panoramaTexture.close();
         this.panoramaTexture = null;
      }

      if (this.tickTexture != null) {
         this.tickTexture.close();
         this.tickTexture = null;
      }
   }

   private static void copyColors(AutismConfig.ThemeColors from, AutismConfig.ThemeColors to) {
      to.advanced = from.advanced;
      to.master = from.master;
      to.accent = from.accent;
      to.outline = from.outline;
      to.text = from.text;
      to.toggle = from.toggle;
      to.backdrop = from.backdrop;
      to.success = from.success;
      to.danger = from.danger;
      to.button = from.button;
      to.header = from.header;
      to.hover = from.hover;
   }

   private record PreviewLayout(int logoWidth, int load, boolean showLogo, int read, int write) {
   }

   private static final class ColorPicker {
      private static final int init_I = 130;
      private static final int load_I = 90;
      private static final int read_I = 14;
      private static final int write_I = 6;
      private static final int update_I = 9;
      private static final int sync_I = 12;
      private IntConsumer onChange;
      private boolean showAlpha;
      private ThemePaletteManager.CompiledPalette palette;
      private float hue;
      private float saturation;
      private float brightness;
      private int red;
      private int green;
      private int blue;
      private int alpha = 255;
      private autismclient.gui.f.NetHelper svArea;
      private autismclient.gui.f.NetHelper hueArea;
      private autismclient.gui.f.NetHelper redSlider;
      private autismclient.gui.f.NetHelper greenSlider;
      private autismclient.gui.f.NetHelper blueSlider;
      private autismclient.gui.f.NetHelper alphaSlider;
      private autismclient.gui.f.NetHelper hexInputArea;
      private int dragTarget;
      private autismclient.util.ThemedTextInputBox hexInput;

      static int panelWidth(boolean alpha) {
         return 94 + sliderStackHeight(alpha);
      }

      static int panelHeight(boolean alpha) {
         return controlsHeight() + 4 + sliderStackHeight(alpha);
      }

      private static int controlsHeight() {
         return Math.max(36, autismclient.gui.ui.UiDensityMetrics.scaledHeight(13) * 2 + 2);
      }

      private static int sliderStackHeight(boolean alpha) {
         return ((alpha ? 4 : 3) - 1) * 12 + 9;
      }

      private int pickerRecolor(int color, ThemePaletteManager.ColorRole ch) {
         return ThemePaletteManager.recolorWithPalette(color, ch, this.palette);
      }

      void setArgb(int argb) {
         this.alpha = argb >>> 24 & 0xFF;
         this.red = argb >>> 16 & 0xFF;
         this.green = argb >>> 8 & 0xFF;
         this.blue = argb & 0xFF;
         float[] hsb = Color.RGBtoHSB(this.red, this.green, this.blue, null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
         if (this.hexInput != null) {
            this.hexInput.setFocused(false);
         }

         this.syncHexInput();
      }

      private int argb() {
         return (this.showAlpha ? this.alpha : 255) << 24 | this.red << 16 | this.green << 8 | this.blue;
      }

      private void ensureHexInput() {
         if (this.hexInput == null) {
            this.hexInput = new autismclient.util.ThemedTextInputBox(mc(), mc().font, 0, 0, 20, autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), false);
            this.hexInput.setMaxLength(6);
            this.hexInput.setFilter(next -> next.chars().allMatch(ch -> isHexDigit((char)ch)));
            this.hexInput.setChangedListener(value -> this.applyHexInput());
            this.hexInput.setValue(String.format(Locale.ROOT, "%06X", this.red << 16 | this.green << 8 | this.blue));
         }
      }

      private void syncHexInput() {
         if (this.hexInput != null && !this.hexInput.isFocused()) {
            this.hexInput.setValue(String.format(Locale.ROOT, "%06X", this.red << 16 | this.green << 8 | this.blue));
         }
      }

      private void syncRgbFromHsb() {
         int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness) & 16777215;
         this.red = rgb >> 16 & 0xFF;
         this.green = rgb >> 8 & 0xFF;
         this.blue = rgb & 0xFF;
         this.syncHexInput();
         this.notifyChange();
      }

      private void syncHsbFromRgb() {
         float[] hsb = Color.RGBtoHSB(this.red, this.green, this.blue, null);
         this.hue = hsb[0];
         this.saturation = hsb[1];
         this.brightness = hsb[2];
         this.syncHexInput();
         this.notifyChange();
      }

      private void notifyChange() {
         if (this.onChange != null) {
            this.onChange.accept(this.argb());
         }
      }

      void renderPicker(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area) {
         GuiGraphicsExtractor g2 = ctx.init_GuiGraphicsExtract();
         int slidersH = sliderStackHeight(this.showAlpha);
         int svH = Math.max(controlsHeight(), Math.min(90, area.sync() - 4 - slidersH));
         this.svArea = autismclient.gui.f.NetHelper.init(area.read(), area.write(), 130, svH);
         this.hueArea = autismclient.gui.f.NetHelper.init(this.svArea.init() + 6, area.write(), 14, svH);
         ThemeColorScreen.GradientTexture.forHue(this.hue).blitTo(g2, this.svArea);
         autismclient.gui.f.PacketUtil.load(g2, this.svArea, ctx.init_TableIndex().colors().borderSubtle);
         int cx = this.svArea.read() + Math.round(this.saturation * (this.svArea.update() - 1));
         int cy = this.svArea.write() + Math.round((1.0F - this.brightness) * (this.svArea.sync() - 1));
         autismclient.gui.f.PacketUtil.load(g2, autismclient.gui.f.NetHelper.init(cx - 4, cy - 4, 8, 8), 4, 0, -1342177280);
         autismclient.gui.f.PacketUtil.load(g2, autismclient.gui.f.NetHelper.init(cx - 3, cy - 3, 6, 6), 3, 0, -1);
         ThemeColorScreen.GradientTexture.hueTexture().blitTo(g2, this.hueArea);
         autismclient.gui.f.PacketUtil.load(g2, this.hueArea, ctx.init_TableIndex().colors().borderSubtle);
         int hy = this.hueArea.write() + Math.round((1.0F - this.hue) * (this.hueArea.sync() - 1));
         autismclient.gui.f.PacketUtil.load(
            g2, autismclient.gui.f.NetHelper.init(this.hueArea.read() - 1, hy - 1, this.hueArea.update() + 2, 3), 1, 0, -1
         );
         int rx = this.hueArea.init() + 6;
         int rw = Math.max(58, area.init() - rx);
         int ctrlH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         autismclient.gui.f.NetHelper swatch = autismclient.gui.f.NetHelper.init(rx, area.write(), rw, ctrlH);
         autismclient.gui.f.PacketUtil.load(g2, swatch, 3, -12961222);
         autismclient.gui.f.PacketUtil.load(g2, swatch, 3, this.argb(), ctx.init_TableIndex().colors().borderSubtle);
         this.hexInputArea = autismclient.gui.f.NetHelper.init(rx, swatch.load() + 2, rw, ctrlH);
         this.ensureHexInput();
         this.hexInput.setX(this.hexInputArea.read());
         this.hexInput.setY(this.hexInputArea.write());
         this.hexInput.setWidth(this.hexInputArea.update());
         this.hexInput.setHeight(this.hexInputArea.sync());
         this.hexInput.render(g2, ctx.read(), ctx.write(), 0.0F);
         int sy = this.svArea.load() + 4;
         this.redSlider = autismclient.gui.f.NetHelper.init(area.read() + 14, sy, area.update() - 52, 9);
         this.greenSlider = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 12, area.update() - 52, 9);
         this.blueSlider = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 24, area.update() - 52, 9);
         this.renderChannelSlider(ctx, this.redSlider, "R", this.red, -43691);
         this.renderChannelSlider(ctx, this.greenSlider, "G", this.green, -11149961);
         this.renderChannelSlider(ctx, this.blueSlider, "B", this.blue, -10053121);
         if (this.showAlpha) {
            this.alphaSlider = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 36, area.update() - 52, 9);
            this.renderChannelSlider(ctx, this.alphaSlider, "A", this.alpha, -4210753);
         } else {
            this.alphaSlider = null;
         }
      }

      private void renderChannelSlider(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper b2, String label, int value, int accent) {
         GuiGraphicsExtractor g2 = context.init_GuiGraphicsExtract();
         k.init(
            g2,
            mc().font,
            label,
            autismclient.gui.f.a.DataUtil.init,
            this.pickerRecolor(-4743522, ThemePaletteManager.ColorRole.TEXT),
            b2.read() - 12,
            b2.write() + 1,
            false
         );
         int originalAccent = context.init_TableIndex().colors().accent;
         context.init_TableIndex().colors().accent = accent;

         try {
            autismclient.gui.ui.widgets.SliderRenderer.init(context, b2, value / 255.0, b2.init_Z(context.read(), context.write()));
         } finally {
            context.init_TableIndex().colors().accent = originalAccent;
         }

         k.init(
            g2,
            mc().font,
            Integer.toString(value),
            autismclient.gui.f.a.DataUtil.init,
            this.pickerRecolor(-791321, ThemePaletteManager.ColorRole.TEXT),
            b2.init() + 5,
            b2.write() + 1,
            false
         );
      }

      boolean mouseClicked(int mx, int my, int button) {
         if (this.svArea == null) {
            return false;
         } else {
            this.ensureHexInput();
            if (this.hexInput.mouseClicked(mx, my, button)) {
               return true;
            } else if (this.svArea.init_Z(mx, my)) {
               this.dragTarget = 1;
               this.dragSaturationValue(mx, my);
               return true;
            } else if (this.hueArea.init_Z(mx, my)) {
               this.dragTarget = 2;
               this.dragHue(my);
               return true;
            } else if (this.redSlider.init_Z(mx, my)) {
               this.dragTarget = 3;
               this.dragChannelSlider(mx, this.redSlider, 0);
               return true;
            } else if (this.greenSlider.init_Z(mx, my)) {
               this.dragTarget = 4;
               this.dragChannelSlider(mx, this.greenSlider, 1);
               return true;
            } else if (this.blueSlider.init_Z(mx, my)) {
               this.dragTarget = 5;
               this.dragChannelSlider(mx, this.blueSlider, 2);
               return true;
            } else if (this.alphaSlider != null && this.alphaSlider.init_Z(mx, my)) {
               this.dragTarget = 6;
               this.dragChannelSlider(mx, this.alphaSlider, 3);
               return true;
            } else {
               return false;
            }
         }
      }

      void mouseDragged(int mx, int my, int button, double dx, double dy) {
         if (this.hexInput == null || !this.hexInput.mouseDragged(mx, my, button, dx, dy)) {
            switch (this.dragTarget) {
               case 1:
                  this.dragSaturationValue(mx, my);
                  break;
               case 2:
                  this.dragHue(my);
                  break;
               case 3:
                  this.dragChannelSlider(mx, this.redSlider, 0);
                  break;
               case 4:
                  this.dragChannelSlider(mx, this.greenSlider, 1);
                  break;
               case 5:
                  this.dragChannelSlider(mx, this.blueSlider, 2);
                  break;
               case 6:
                  if (this.alphaSlider != null) {
                     this.dragChannelSlider(mx, this.alphaSlider, 3);
                  }
            }
         }
      }

      void mouseReleased(int mx, int my, int button) {
         this.dragTarget = 0;
         if (this.hexInput != null) {
            this.hexInput.mouseReleased(mx, my, button);
         }
      }

      boolean keyPressed(KeyEvent input) {
         if (this.hexInput != null && this.hexInput.isFocused()) {
            int key = Compat263.keyEventGlfwKey(input);
            if (key != 257 && key != 335 && key != 256) {
               this.hexInput.keyPressed(input);
               return true;
            } else {
               this.hexInput.setFocused(false);
               this.syncHexInput();
               return true;
            }
         } else {
            return false;
         }
      }

      boolean charTyped(CharacterEvent input) {
         if (this.hexInput != null && this.hexInput.isFocused()) {
            this.hexInput.charTyped(input);
            return true;
         } else {
            return false;
         }
      }

      private void applyHexInput() {
         String hexText = this.hexInput == null ? "" : this.hexInput.getValue();
         if (hexText.length() == 6) {
            try {
               int rgb = Integer.parseInt(hexText, 16) & 16777215;
               this.red = rgb >> 16 & 0xFF;
               this.green = rgb >> 8 & 0xFF;
               this.blue = rgb & 0xFF;
               float[] hsb = Color.RGBtoHSB(this.red, this.green, this.blue, null);
               this.hue = hsb[0];
               this.saturation = hsb[1];
               this.brightness = hsb[2];
               this.notifyChange();
            } catch (NumberFormatException var4) {
            }
         }
      }

      private void dragSaturationValue(int mx, int my) {
         this.saturation = clamp01((float)(mx - this.svArea.read()) / Math.max(1, this.svArea.update() - 1));
         this.brightness = clamp01(1.0F - (float)(my - this.svArea.write()) / Math.max(1, this.svArea.sync() - 1));
         this.syncRgbFromHsb();
      }

      private void dragHue(int my) {
         this.hue = clamp01(1.0F - (float)(my - this.hueArea.write()) / Math.max(1, this.hueArea.sync() - 1));
         this.syncRgbFromHsb();
      }

      private void dragChannelSlider(int mx, autismclient.gui.f.NetHelper bounds, int channel) {
         int value = Math.round(clamp01((float)(mx - bounds.read()) / Math.max(1, bounds.update())) * 255.0F);
         if (channel == 0) {
            this.red = value;
         } else if (channel == 1) {
            this.green = value;
         } else {
            if (channel != 2) {
               this.alpha = value;
               this.notifyChange();
               return;
            }

            this.blue = value;
         }

         this.syncHsbFromRgb();
      }

      private static boolean isHexDigit(char c) {
         return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
      }

      private static float clamp01(float v) {
         return Math.max(0.0F, Math.min(1.0F, v));
      }

      private static Minecraft mc() {
         return Minecraft.getInstance();
      }
   }

   private record ColorRow(String label, IntSupplier getter, IntConsumer setter, ThemePaletteManager.ColorRole role) {
   }

   private static final class GradientTexture extends AbstractTexture {
      private static final int init_I = 360;
      private static ThemeColorScreen.GradientTexture hueTexture;
      private static ThemeColorScreen.GradientTexture svTexture;
      private static int svTextureHueBucket = -2147483648;
      private final Identifier textureId;
      private final NativeImage image;
      private final int width;
      private final int height;

      private GradientTexture(Identifier id, String label, int w, int h) {
         this.textureId = id;
         this.width = w;
         this.height = h;
         this.image = new NativeImage(w, h, true);
         this.texture = RenderSystem.getDevice().createTexture("AUTISM " + label, 5, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         Minecraft.getInstance().getTextureManager().register(id, this);
      }

      static ThemeColorScreen.GradientTexture hueTexture() {
         if (hueTexture == null) {
            hueTexture = new ThemeColorScreen.GradientTexture(
               Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/picker_hue"), "theme picker hue", 14, 96
            );

            for (int y = 0; y < hueTexture.height; y++) {
               int color = 0xFF000000 | Color.HSBtoRGB(1.0F - (float)y / (hueTexture.height - 1), 1.0F, 1.0F) & 16777215;

               for (int x = 0; x < hueTexture.width; x++) {
                  hueTexture.image.setPixel(x, y, color);
               }
            }

            hueTexture.upload();
         }

         return hueTexture;
      }

      static ThemeColorScreen.GradientTexture forHue(float hue) {
         int bucket = Math.round(ThemeColorScreen.ColorPicker.clamp01(hue) * 360.0F);
         if (svTexture == null) {
            svTexture = new ThemeColorScreen.GradientTexture(
               Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/picker_sv"), "theme picker sv", 130, 96
            );
         }

         if (svTextureHueBucket != bucket) {
            float hh = bucket / 360.0F;

            for (int x = 0; x < svTexture.width; x++) {
               float s = (float)x / (svTexture.width - 1);

               for (int y = 0; y < svTexture.height; y++) {
                  float v = 1.0F - (float)y / (svTexture.height - 1);
                  svTexture.image.setPixel(x, y, 0xFF000000 | Color.HSBtoRGB(hh, s, v) & 16777215);
               }
            }

            svTexture.upload();
            svTextureHueBucket = bucket;
         }

         return svTexture;
      }

      private void upload() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.image);
      }

      private void blitTo(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper area) {
         g.blit(
            RenderPipelines.GUI_TEXTURED,
            this.textureId,
            area.read(),
            area.write(),
            0.0F,
            0.0F,
            area.update(),
            area.sync(),
            this.width,
            this.height,
            this.width,
            this.height,
            -1
         );
      }
   }
}
