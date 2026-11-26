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
   private static final int init_I = -791321;
   private static final int load_I = -4743522;
   private final Screen init_Screen;
   private final AutismConfig init_AutismConfig;
   private final AutismConfig.ThemeColors init_AutismConfig_Theme = new AutismConfig.ThemeColors();
   private ThemePaletteManager.CompiledPalette init_ct_StreamUtil;
   private final autismclient.gui.ui.UiTheme init_TableIndex = new autismclient.gui.ui.UiTheme();
   private final autismclient.gui.ui.UiTheme load_TableIndex = new autismclient.gui.ui.UiTheme();
   private final ThemeColorScreen.Inner_CachePool init_CacheEntry_CachePo = new ThemeColorScreen.Inner_CachePool();
   private int read_I;
   private ThemeTextureRecolorer.Inner_CachePool init_cu_CachePool;
   private ThemeTextureRecolorer.Inner_CachePool load_cu_CachePool;
   private ThemeTextureRecolorer.Inner_CachePool read_cu_CachePool;
   private autismclient.gui.f.NetHelper init_NetHelper;
   private autismclient.gui.f.NetHelper load_NetHelper;
   private autismclient.gui.f.NetHelper read_NetHelper;
   private autismclient.gui.f.NetHelper write_NetHelper;
   private autismclient.gui.f.NetHelper update_NetHelper;
   private final List<autismclient.gui.f.NetHelper> init_List = new ArrayList<>();
   private autismclient.gui.f.NetHelper sync_NetHelper;
   private int write_I;
   private int update_I;
   private int sync_I = 1;
   private autismclient.gui.f.NetHelper flush_NetHelper;
   private autismclient.gui.ui.widgets.ScrollbarRenderer.ScrollbarBounds init_UtilCore_DataUtil;
   private boolean init_Z;
   private int flush_I;
   private List<ThemeColorScreen.Inner_DataUtil> load_List;
   private boolean load_Z;
   private List<Module> read_List;
   private int close = -2147483648;
   private static final int open = 240;
   private static final int poll = 224;

   public ThemeColorScreen(Screen parent) {
      super(Component.literal("Theme Color"));
      this.init_Screen = parent;
      this.init_AutismConfig = AutismConfig.getGlobal();
      init_V_AutismConfig_Theme_AutismConfig_Theme(this.init_AutismConfig.themeColors, this.init_AutismConfig_Theme);
      this.load_V();
      this.read_V();
   }

   @Override
   protected void init() {
      if (this.init_cu_CachePool == null) {
         this.init_cu_CachePool = ThemeTextureRecolorer.init(
            Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/autism_client_logo.png"), ThemePaletteManager.ColorRole.ACCENT, 2048
         );
      }

      if (this.load_cu_CachePool == null) {
         this.load_cu_CachePool = ThemeTextureRecolorer.init(
            Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/background/panorama_0.png"), ThemePaletteManager.ColorRole.BACKDROP, 640
         );
      }

      if (this.read_cu_CachePool == null) {
         this.read_cu_CachePool = ThemeTextureRecolorer.init(autismclient.gui.f.a.DataUtil.tick, ThemePaletteManager.ColorRole.ACCENT, 128);
      }
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   private List<ThemeColorScreen.Inner_DataUtil> init_List() {
      if (this.load_List != null && this.load_Z == this.init_AutismConfig_Theme.advanced) {
         return this.load_List;
      }

      List<ThemeColorScreen.Inner_DataUtil> rows = new ArrayList<>();
      if (!this.init_AutismConfig_Theme.advanced) {
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Theme Color", () -> this.init_AutismConfig_Theme.master, v -> this.init_AutismConfig_Theme.master = v, ThemePaletteManager.ColorRole.ACCENT
            )
         );
      } else {
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Accent", () -> this.init_AutismConfig_Theme.accent, v -> this.init_AutismConfig_Theme.accent = v, ThemePaletteManager.ColorRole.ACCENT
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Button Fill", () -> this.init_AutismConfig_Theme.button, v -> this.init_AutismConfig_Theme.button = v, ThemePaletteManager.ColorRole.BUTTON
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Outline / Border", () -> this.init_AutismConfig_Theme.outline, v -> this.init_AutismConfig_Theme.outline = v, ThemePaletteManager.ColorRole.OUTLINE
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Header", () -> this.init_AutismConfig_Theme.header, v -> this.init_AutismConfig_Theme.header = v, ThemePaletteManager.ColorRole.HEADER
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Toggle / Highlight", () -> this.init_AutismConfig_Theme.toggle, v -> this.init_AutismConfig_Theme.toggle = v, ThemePaletteManager.ColorRole.TOGGLE
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Hover / Glow", () -> this.init_AutismConfig_Theme.hover, v -> this.init_AutismConfig_Theme.hover = v, ThemePaletteManager.ColorRole.HOVER
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil("Text", () -> this.init_AutismConfig_Theme.text, v -> this.init_AutismConfig_Theme.text = v, ThemePaletteManager.ColorRole.TEXT)
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Background / Panorama", () -> this.init_AutismConfig_Theme.backdrop, v -> this.init_AutismConfig_Theme.backdrop = v, ThemePaletteManager.ColorRole.BACKDROP
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Danger", () -> this.init_AutismConfig_Theme.danger, v -> this.init_AutismConfig_Theme.danger = v, ThemePaletteManager.ColorRole.DANGER
            )
         );
         rows.add(
            new ThemeColorScreen.Inner_DataUtil(
               "Success", () -> this.init_AutismConfig_Theme.success, v -> this.init_AutismConfig_Theme.success = v, ThemePaletteManager.ColorRole.SUCCESS
            )
         );
      }

      this.load_List = rows;
      this.load_Z = this.init_AutismConfig_Theme.advanced;
      return rows;
   }

   private void load_V() {
      this.init_ct_StreamUtil = ThemePaletteManager.CompiledPalette.compile(this.init_AutismConfig_Theme);
      this.init_TableIndex.colors().applyPalette(this.init_ct_StreamUtil);
      this.load_TableIndex.colors().applyPalette(this.init_ct_StreamUtil);
      this.load_TableIndex.colors().accent = this.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.ColorRole.TOGGLE);
      autismclient.gui.ui.UiColorScheme colors = this.init_TableIndex.colors();
      colors.headerSurface = UiLayoutMath.lerpColor(colors.headerSurface, this.init_I_I_ct_DataUtil(1493121851, ThemePaletteManager.ColorRole.HEADER), 0.08F);
      colors.buttonSurface = UiLayoutMath.lerpColor(colors.buttonSurface, this.init_I_I_ct_DataUtil(-930144476, ThemePaletteManager.ColorRole.BUTTON), 0.12F);
      colors.buttonSurfaceHover = UiLayoutMath.lerpColor(colors.buttonSurfaceHover, this.init_I_I_ct_DataUtil(-721460124, ThemePaletteManager.ColorRole.HOVER), 0.12F);
      colors.borderSubtle = UiLayoutMath.lerpColor(colors.borderSubtle, this.init_I_I_ct_DataUtil(1023370340, ThemePaletteManager.ColorRole.OUTLINE), 0.15F);
      colors.borderControl = UiLayoutMath.lerpColor(colors.borderControl, this.init_I_I_ct_DataUtil(1291805796, ThemePaletteManager.ColorRole.OUTLINE), 0.15F);
   }

   private autismclient.gui.f.SyncManager init(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      return this.init(graphics, mouseX, mouseY, this.init_TableIndex);
   }

   private autismclient.gui.f.SyncManager init(GuiGraphicsExtractor graphics, int mouseX, int mouseY, autismclient.gui.ui.UiTheme theme) {
      autismclient.gui.f.SyncManager base = autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY);
      return new autismclient.gui.f.SyncManager(graphics, theme, base.init_PoolEntry(), base.init_I(), base.load_I(), mouseX, mouseY, base.init_F());
   }

   private void read_V() {
      List<ThemeColorScreen.Inner_DataUtil> rows = this.init_List();
      if (this.read_I >= rows.size()) {
         this.read_I = 0;
      }

      this.init_CacheEntry_CachePo.init_V_I(rows.get(this.read_I).init_IntSupplier().getAsInt());
      this.init_CacheEntry_CachePo.init_IntConsumer = argb -> {
         this.init_List().get(this.read_I).init_IntConsumer().accept(argb);
         this.load_V();
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
         autismclient.gui.f.SyncManager ctx = this.init(graphics, mx, my);
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, sw, sh), ctx.init_TableIndex().colors().backdropScrim);
         int margin = 6;
         int contentW = sw - margin * 2;
         int contentH = sh - margin * 2;
         int gap = 4;
         int leftW = Math.max(224, Math.min(240, contentW - gap - 120));
         int rightW = Math.max(1, contentW - leftW - gap);
         int leftH = Math.min(contentH, this.sync());
         ThemeColorScreen.Inner_BufferHelper preview = this.init_CacheEntry_BufferH_I_I(rightW, contentH);
         int rightH = preview.write();
         if (Math.abs(leftH - rightH) <= 8) {
            leftH = rightH = Math.max(leftH, rightH);
         }

         autismclient.gui.f.NetHelper left = autismclient.gui.f.NetHelper.init(margin, margin, leftW, leftH);
         autismclient.gui.f.NetHelper right = autismclient.gui.f.NetHelper.init(margin + leftW + gap, margin, rightW, rightH);
         this.init(ctx, left, mx, my);
         this.init(ctx, right, preview);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
      }
   }

   private int init_I() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int load_I() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
   }

   private int read_I() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(12);
   }

   private int write_I() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int update_I() {
      return ThemeColorScreen.Inner_CachePool.init_I_Z(this.init_AutismConfig_Theme.advanced);
   }

   private int sync() {
      return 4 + this.init_I() + this.load_I() + 2 + this.read_I() + this.init_List().size() * this.write_I() + 4 + this.update_I() + 4 + this.load_I() + 4;
   }

   private void init(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area, int mx, int my) {
      GuiGraphicsExtractor g = ctx.init_GuiGraphicsExtract();
      autismclient.gui.f.PacketUtil.read(g, area, 8, ctx.init_TableIndex().colors().windowSurface, ctx.init_TableIndex().colors().borderSubtle);
      int x = area.read() + 4;
      int y = area.write() + 4;
      int innerW = area.update() - 8;
      int ctrlH = this.load_I();
      this.init(g, "THEME COLOR", autismclient.gui.f.a.DataUtil.read, this.init_I_I_ct_DataUtil(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
      y += this.init_I();
      int tabW = (innerW - 2) / 2;
      this.init_NetHelper = autismclient.gui.f.NetHelper.init(x, y, tabW, ctrlH);
      this.load_NetHelper = autismclient.gui.f.NetHelper.init(x + tabW + 2, y, innerW - tabW - 2, ctrlH);
      this.init(g, this.init_NetHelper, "Simple", !this.init_AutismConfig_Theme.advanced, this.init_NetHelper.init_Z(mx, my));
      this.init(g, this.load_NetHelper, "Advanced", this.init_AutismConfig_Theme.advanced, this.load_NetHelper.init_Z(mx, my));
      y += ctrlH + 2;
      this.init(
         g,
         this.init_AutismConfig_Theme.advanced ? "Channels" : "One color recolors the whole theme",
         autismclient.gui.f.a.DataUtil.load,
         this.init_I_I(-4743522),
         x,
         UiLayoutMath.centerTextY(y, this.read_I(), k.init(autismclient.gui.f.a.DataUtil.load), 1)
      );
      y += this.read_I();
      this.init_List.clear();
      List<ThemeColorScreen.Inner_DataUtil> rows = this.init_List();
      int rowH = this.write_I();
      int footerY = area.load() - 4 - ctrlH;
      int rowSpace = footerY - 4 - ThemeColorScreen.Inner_CachePool.load_I_Z(this.init_AutismConfig_Theme.advanced) - 4 - y;
      int visible = Math.min(rows.size(), Math.max(Math.min(3, rows.size()), rowSpace / rowH));
      int maxScroll = rows.size() - visible;
      this.write_I = Math.max(0, Math.min(this.write_I, maxScroll));
      this.update_I = this.write_I;
      this.sync_I = rowH;
      this.flush_NetHelper = autismclient.gui.f.NetHelper.init(x, y, innerW, visible * rowH - 2);
      int rowW = innerW;
      if (maxScroll > 0) {
         rowW = innerW - 6 - 2;
         autismclient.gui.f.NetHelper track = autismclient.gui.f.NetHelper.init(x + innerW - 6, y, 6, this.flush_NetHelper.sync());
         this.init_UtilCore_DataUtil = autismclient.gui.ui.widgets.ScrollbarRenderer.init(track, rows.size() * rowH, visible * rowH, this.write_I * rowH);
         autismclient.gui.ui.widgets.ScrollbarRenderer.init(ctx, this.init_UtilCore_DataUtil, this.init_UtilCore_DataUtil.init(mx, my), this.init_Z);
      } else {
         this.init_UtilCore_DataUtil = null;
         this.init_Z = false;
      }

      for (int i = this.write_I; i < this.write_I + visible; i++) {
         ThemeColorScreen.Inner_DataUtil c = rows.get(i);
         autismclient.gui.f.NetHelper rb = autismclient.gui.f.NetHelper.init(x, y, rowW, rowH - 2);
         this.init_List.add(rb);
         boolean sel = i == this.read_I;
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
         autismclient.gui.f.PacketUtil.load(g, swatch, 2, 0xFF000000 | c.init_IntSupplier().getAsInt() & 16777215, ctx.init_TableIndex().colors().borderSubtle);
         int textY = UiLayoutMath.centerTextY(rb.write(), rb.sync(), k.init(autismclient.gui.f.a.DataUtil.init), 1);
         this.init(g, c.init_Str(), autismclient.gui.f.a.DataUtil.init, sel ? this.init_I_I(-791321) : this.init_I_I(-4743522), swatch.init() + 4 + 1, textY);
         String hex = init_Str_I(c.init_IntSupplier().getAsInt());
         int hexW = k.init(this.font, hex, autismclient.gui.f.a.DataUtil.init, -4743522);
         this.init(g, hex, autismclient.gui.f.a.DataUtil.init, this.init_I_I(-4743522), rb.init() - hexW - 4, textY);
         y += rowH;
      }

      y += 4;
      this.init_CacheEntry_CachePo.init_Z = this.init_AutismConfig_Theme.advanced;
      this.init_CacheEntry_CachePo.init_ct_StreamUtil = this.init_ct_StreamUtil;
      this.sync_NetHelper = autismclient.gui.f.NetHelper.init(
         x, y, innerW, Math.max(ThemeColorScreen.Inner_CachePool.load_I_Z(this.init_AutismConfig_Theme.advanced), Math.min(this.update_I(), footerY - 4 - y))
      );
      this.init_CacheEntry_CachePo.init(ctx, this.sync_NetHelper);
      int bw = (innerW - 4) / 3;
      this.read_NetHelper = autismclient.gui.f.NetHelper.init(x, footerY, bw, ctrlH);
      this.write_NetHelper = autismclient.gui.f.NetHelper.init(x + bw + 2, footerY, bw, ctrlH);
      this.update_NetHelper = autismclient.gui.f.NetHelper.init(x + (bw + 2) * 2, footerY, innerW - (bw + 2) * 2, ctrlH);
      this.init(g, this.read_NetHelper, "Reset", this.init_I_I_ct_DataUtil(1717972012, ThemePaletteManager.ColorRole.OUTLINE), this.read_NetHelper.init_Z(mx, my));
      this.init(g, this.write_NetHelper, "Cancel", this.init_I_I_ct_DataUtil(1717972012, ThemePaletteManager.ColorRole.OUTLINE), this.write_NetHelper.init_Z(mx, my));
      this.init(g, this.update_NetHelper, "Apply", this.init_I_I_ct_DataUtil(-39836, ThemePaletteManager.ColorRole.ACCENT), this.update_NetHelper.init_Z(mx, my));
   }

   private ThemeColorScreen.Inner_BufferHelper init_CacheEntry_BufferH_I_I(int panelW, int budget) {
      int innerW = panelW - 8;
      int logoW = this.init_cu_CachePool != null ? Math.min(innerW, 340) : 0;
      int logoH = this.init_cu_CachePool != null && this.init_cu_CachePool.init_I() > 0
         ? Math.max(1, Math.round(logoW * ((float)this.init_cu_CachePool.load_I() / this.init_cu_CachePool.init_I())))
         : 0;
      int h = this.close() + this.poll() + 4;
      boolean showLogo = logoH > 0 && h + logoH + 2 <= budget;
      if (showLogo) {
         h += logoH + 2;
      }

      int bannerH = 0;
      if (this.load_cu_CachePool != null) {
         int bannerGap = 8;
         int bh = Math.min(Math.round(innerW * 0.56F), budget - h - bannerGap);
         if (bh >= 40) {
            bannerH = bh;
            h += bh + bannerGap;
         }
      }

      return new ThemeColorScreen.Inner_BufferHelper(logoW, logoH, showLogo, bannerH, Math.min(h, budget));
   }

   private int flush() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(18);
   }

   private int close() {
      return 4 + this.init_I() + this.flush() + 2 + 8;
   }

   private void init(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area, ThemeColorScreen.Inner_BufferHelper layout) {
      GuiGraphicsExtractor g = ctx.init_GuiGraphicsExtract();
      autismclient.gui.f.PacketUtil.read(g, area, 8, ctx.init_TableIndex().colors().windowSurface, ctx.init_TableIndex().colors().borderSubtle);
      int x = area.read() + 4;
      int y = area.write() + 4;
      int innerW = area.update() - 8;
      this.init(g, "LIVE PREVIEW", autismclient.gui.f.a.DataUtil.load, this.init_I_I_ct_DataUtil(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
      y += this.init_I();
      int barH = this.flush();
      autismclient.gui.f.PacketUtil.read(
         g, autismclient.gui.f.NetHelper.init(x, y, innerW, barH), 5, ctx.init_TableIndex().colors().headerSurface, ctx.init_TableIndex().colors().borderSubtle
      );
      if (this.read_cu_CachePool != null) {
         this.read_cu_CachePool.init_V_ct_StreamUtil(this.init_ct_StreamUtil);
         g.blit(
            RenderPipelines.GUI_TEXTURED,
            this.read_cu_CachePool.init_Identifier(),
            x + 4,
            y + (barH - 14) / 2,
            0.0F,
            0.0F,
            14,
            14,
            this.read_cu_CachePool.init_I(),
            this.read_cu_CachePool.load_I(),
            this.read_cu_CachePool.init_I(),
            this.read_cu_CachePool.load_I(),
            -1
         );
      }

      this.init(
         g,
         "Autism Client",
         autismclient.gui.f.a.DataUtil.load,
         this.init_I_I_ct_DataUtil(-2828, ThemePaletteManager.ColorRole.TEXT),
         x + 8 + 14,
         this.init(y, barH, autismclient.gui.f.a.DataUtil.load)
      );
      y += barH + 2 + 8;
      g.enableScissor(area.read() + 1, area.write() + 1, area.init() - 1, area.load() - 1);

      try {
         this.init(g, area, x, y, innerW, layout);
      } finally {
         g.disableScissor();
      }
   }

   private void init(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper area, int x, int y, int innerW, ThemeColorScreen.Inner_BufferHelper layout) {
      int contentLimit = area.load() - 4;
      if (this.load_cu_CachePool != null) {
         this.load_cu_CachePool.init_V_ct_StreamUtil(this.init_ct_StreamUtil);
         int bw = innerW;
         int bh = layout.read();
         if (bh > 0) {
            int regionH = Math.max(1, Math.min(this.load_cu_CachePool.load_I(), Math.round(this.load_cu_CachePool.load_I() * ((float)bh / bw))));
            float vOff = Math.max(0.0F, (this.load_cu_CachePool.load_I() - regionH) / 2.0F);
            g.blit(
               RenderPipelines.GUI_TEXTURED,
               this.load_cu_CachePool.init_Identifier(),
               x,
               y,
               0.0F,
               vOff,
               bw,
               bh,
               this.load_cu_CachePool.init_I(),
               regionH,
               this.load_cu_CachePool.init_I(),
               this.load_cu_CachePool.load_I(),
               -1
            );
            autismclient.gui.f.PacketUtil.load(g, autismclient.gui.f.NetHelper.init(x, y, bw, bh), this.init_I_I_ct_DataUtil(-7392975, ThemePaletteManager.ColorRole.OUTLINE));
            y += bh + 8;
         }
      }

      this.init(g, "MODULES", autismclient.gui.f.a.DataUtil.load, this.init_I_I(-4743522), x, y);
      y += this.read_I();
      List<Module> preview = this.load_List();

      for (int i = 0; i < preview.size() && y + this.open() <= contentLimit; i++) {
         Module m = preview.get(i);
         String key = m.keybind() >= 0 ? autismclient.commands.arguments.KeyBindArgumentType.init_Str_I(m.keybind()) : "";
         y = this.init(g, x, y, innerW, m.name(), key, m.isEnabled(), i == 0, i == 1);
      }

      y += 8;
      int ctrlH = this.load_I();
      if (y + ctrlH <= contentLimit) {
         this.init(g, x, y, ctrlH, true);
         this.init(g, x + 30 + 2, y, ctrlH, false);
         int chipX = x + 64 + 4;
         int successW = this.init_I_Str("Success");
         if (chipX + successW <= x + innerW) {
            this.init(g, chipX, y, successW, ctrlH, "Success", this.init_I_I_ct_DataUtil(-9448565, ThemePaletteManager.ColorRole.SUCCESS));
         }

         chipX += successW + 2;
         int dangerW = this.init_I_Str("Danger");
         if (chipX + dangerW <= x + innerW) {
            this.init(g, chipX, y, dangerW, ctrlH, "Danger", this.init_I_I_ct_DataUtil(-1938838, ThemePaletteManager.ColorRole.DANGER));
         }

         y += ctrlH + 4;
      }

      if (y + ctrlH <= contentLimit) {
         int btnTextW = k.init(this.font, "Button", autismclient.gui.f.a.DataUtil.init, -791321);
         int btnW = autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(btnTextW);
         autismclient.gui.f.NetHelper btn = autismclient.gui.f.NetHelper.init(x, y, btnW, ctrlH);
         autismclient.gui.f.SyncManager sample = this.init(g, -1, -1);
         autismclient.gui.f.b.CachePool.init(sample, btn, "Button", autismclient.gui.f.b.CachePool.Inner_DataUtil.init, false, false);
         int fieldX = btn.init() + 2;
         autismclient.gui.f.NetHelper field = autismclient.gui.f.NetHelper.init(fieldX, y, Math.max(0, x + innerW - fieldX), ctrlH);
         if (field.update() >= 12) {
            TextFieldRenderer.init(sample, field, "", "Search", false, 0);
         }

         y += ctrlH + 4;
      }

      int lineH = this.read_I();
      if (y + lineH <= contentLimit) {
         this.init(g, "Primary text sample", autismclient.gui.f.a.DataUtil.init, this.init_I_I_ct_DataUtil(-791321, ThemePaletteManager.ColorRole.TEXT), x, y);
         y += lineH;
      }

      if (y + lineH <= contentLimit) {
         this.init(g, "Secondary / muted text", autismclient.gui.f.a.DataUtil.init, this.init_I_I_ct_DataUtil(-4743522, ThemePaletteManager.ColorRole.TEXT), x, y);
         y += lineH;
      }

      if (y + lineH <= contentLimit) {
         this.init(g, "Accent text", autismclient.gui.f.a.DataUtil.init, this.init_I_I_ct_DataUtil(-45747, ThemePaletteManager.ColorRole.ACCENT), x, y);
         y += lineH;
      }

      if (this.init_cu_CachePool != null && layout.init_Z()) {
         int logoW = layout.init_I();
         int logoH = layout.load();
         int ly = area.load() - 4 - logoH;
         if (ly >= y + 2) {
            this.init_cu_CachePool.init_V_ct_StreamUtil(this.init_ct_StreamUtil);
            int lx = x + (innerW - logoW) / 2;
            g.blit(
               RenderPipelines.GUI_TEXTURED,
               this.init_cu_CachePool.init_Identifier(),
               lx,
               ly,
               0.0F,
               0.0F,
               logoW,
               logoH,
               this.init_cu_CachePool.init_I(),
               this.init_cu_CachePool.load_I(),
               this.init_cu_CachePool.init_I(),
               this.init_cu_CachePool.load_I(),
               -1
            );
         }
      }
   }

   private int open() {
      return autismclient.gui.ui.UiDensityMetrics.scaledHeight(14);
   }

   private int poll() {
      return this.read_I() + this.load_List().size() * (this.open() + 2) + 8 + (this.load_I() + 4) * 2 + this.read_I() * 3;
   }

   private int init(int boxY, int boxH, Identifier fontId) {
      return UiLayoutMath.centerTextY(boxY, boxH, k.init(fontId), 1);
   }

   private int init(GuiGraphicsExtractor g, int x, int y, int w, String name, String keybind, boolean on, boolean selected, boolean hovered) {
      int h = this.open();
      autismclient.gui.f.NetHelper row = autismclient.gui.f.NetHelper.init(x, y, w, h);
      autismclient.gui.ui.UiColorScheme colors = this.init_TableIndex.colors();
      int fill = selected ? UiLayoutMath.lerpColor(colors.cardSurface, this.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.ColorRole.TOGGLE), 0.12F) : (hovered ? colors.cardSurfaceElevated : colors.cardSurface);
      autismclient.gui.f.PacketUtil.load(g, row, 3, fill);
      if (selected) {
         autismclient.gui.f.PacketUtil.load(g, row, 3, 0, this.init_I_I_ct_DataUtil(-50373, ThemePaletteManager.ColorRole.TOGGLE));
      }

      int ty = this.init(y, h, autismclient.gui.f.a.DataUtil.init);
      String label = name + (keybind != null && !keybind.isEmpty() ? " [" + keybind + "]" : "");
      this.init(g, -1, -1).init_PoolEntry().drawEllipsized(g, label, x + 8, ty, Math.max(1, w - 30 - 16), on ? colors.textPrimary : colors.textSecondary);
      this.init(g, x + w - 30 - 4, y, h, on);
      return y + h + 2;
   }

   private static String init_Str_I(int rgb) {
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
      if (this.read_List != null && this.close == rev) {
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
      this.close = rev;
      return out;
   }

   private void init(GuiGraphicsExtractor g, int x, int y, int h, boolean on) {
      autismclient.gui.f.NetHelper b = autismclient.gui.f.NetHelper.init(x, y, 30, h);
      autismclient.gui.ui.widgets.ToggleSwitchRenderer.init(this.init(g, -1, -1, this.load_TableIndex), b, null, on, false, "themePreviewToggle:" + x + ":" + y);
   }

   private int init_I_Str(String label) {
      return autismclient.gui.ui.UiDensityMetrics.buttonWidthForText(k.init(this.font, label, autismclient.gui.f.a.DataUtil.init, 0));
   }

   private void init(GuiGraphicsExtractor g, int x, int y, int w, int h, String label, int color) {
      autismclient.gui.ui.widgets.PillBadgeRenderer.init(this.init(g, -1, -1), autismclient.gui.f.NetHelper.init(x, y, w, h), label, color, true, false);
   }

   private int init_I_I_ct_DataUtil(int color, ThemePaletteManager.ColorRole ch) {
      return ThemePaletteManager.recolorWithPalette(color, ch, this.init_ct_StreamUtil);
   }

   private int init_I_I(int color) {
      return this.init_I_I_ct_DataUtil(color, ThemePaletteManager.ColorRole.TEXT);
   }

   private void init(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper b, String label, boolean active, boolean hover) {
      autismclient.gui.f.b.CachePool.init(
         this.init(g, -1, -1),
         b,
         label,
         active ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         active
      );
   }

   private void init(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper b, String label, int border, boolean hover) {
      autismclient.gui.f.b.CachePool.init(
         this.init(g, -1, -1),
         b,
         label,
         border == this.init_I_I_ct_DataUtil(-39836, ThemePaletteManager.ColorRole.ACCENT)
            ? autismclient.gui.f.b.CachePool.Inner_DataUtil.read
            : autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
         hover,
         false
      );
   }

   private void init(GuiGraphicsExtractor g, String text, Identifier font, int color, int x, int y) {
      k.init(g, this.font, text, font, color, x, y, false);
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      int mx = cA.init_I_D(event.x());
      int my = cA.init_I_D(event.y());
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return true;
      }

      if (this.init_CacheEntry_CachePo.init_Z(mx, my, Compat263.mouseEventGlfwButton(event))) {
         return true;
      }

      if (this.init_NetHelper != null && this.init_NetHelper.init_Z(mx, my)) {
         this.init_V_Z(false);
         return true;
      }

      if (this.load_NetHelper != null && this.load_NetHelper.init_Z(mx, my)) {
         this.init_V_Z(true);
         return true;
      }

      if (this.init_UtilCore_DataUtil != null && this.init_UtilCore_DataUtil.init(mx, my)) {
         this.flush_I = this.init_UtilCore_DataUtil.load(mx, my)
            ? my - this.init_UtilCore_DataUtil.load().write()
            : this.init_UtilCore_DataUtil.load().sync() / 2;
         this.init_Z = true;
         this.init_V_I(my);
         return true;
      }

      for (int i = 0; i < this.init_List.size(); i++) {
         if (this.init_List.get(i).init_Z(mx, my)) {
            this.read_I = this.update_I + i;
            this.read_V();
            return true;
         }
      }

      if (this.read_NetHelper != null && this.read_NetHelper.init_Z(mx, my)) {
         this.write_V();
         return true;
      } else if (this.write_NetHelper != null && this.write_NetHelper.init_Z(mx, my)) {
         this.onClose();
         return true;
      } else if (this.update_NetHelper != null && this.update_NetHelper.init_Z(mx, my)) {
         this.update_V();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.init_Z) {
         this.init_V_I(cA.init_I_D(event.y()));
         return true;
      } else {
         this.init_CacheEntry_CachePo.init_V_I(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event), dx, dy);
         return true;
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      this.init_Z = false;
      this.init_CacheEntry_CachePo.init_V(cA.init_I_D(event.x()), cA.init_I_D(event.y()), Compat263.mouseEventGlfwButton(event));
      return true;
   }

   @Override
   public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
      int mx = cA.init_I_D(x);
      int my = cA.init_I_D(y);
      if (this.init_UtilCore_DataUtil != null && scrollY != 0.0 && (this.flush_NetHelper.init_Z(mx, my) || this.init_UtilCore_DataUtil.init(mx, my))) {
         this.write_I = this.write_I - (int)Math.signum(scrollY);
         return true;
      } else {
         return super.mouseScrolled(x, y, scrollX, scrollY);
      }
   }

   private void init_V_I(int my) {
      if (this.init_UtilCore_DataUtil != null) {
         this.write_I = Math.round((float)autismclient.gui.ui.widgets.ScrollbarRenderer.init_I_UtilCore_DataUtil(this.init_UtilCore_DataUtil, my, this.flush_I) / this.sync_I);
      }
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      if (this.init_CacheEntry_CachePo.init_Z_KeyEvent(input)) {
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
      return this.init_CacheEntry_CachePo.init_Z_CharacterEvent(input) ? true : super.charTyped(input);
   }

   private void init_V_Z(boolean advanced) {
      if (this.init_AutismConfig_Theme.advanced != advanced) {
         if (advanced) {
            int defaultMaster = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
            boolean masterChanged = (this.init_AutismConfig_Theme.master & 16777215) != (defaultMaster & 16777215)
               || (this.init_AutismConfig_Theme.master >>> 24 & 0xFF) != 255;
            if (masterChanged) {
               this.init_AutismConfig_Theme.accent = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.button = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.outline = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.header = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.toggle = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.hover = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.backdrop = this.init_AutismConfig_Theme.master;
               this.init_AutismConfig_Theme.danger = this.init_AutismConfig_Theme.master;
            } else {
               this.init_AutismConfig_Theme.accent = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.ACCENT.ordinal()];
               this.init_AutismConfig_Theme.button = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.BUTTON.ordinal()];
               this.init_AutismConfig_Theme.outline = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.OUTLINE.ordinal()];
               this.init_AutismConfig_Theme.header = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.HEADER.ordinal()];
               this.init_AutismConfig_Theme.toggle = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.TOGGLE.ordinal()];
               this.init_AutismConfig_Theme.hover = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.HOVER.ordinal()];
               this.init_AutismConfig_Theme.backdrop = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.BACKDROP.ordinal()];
               this.init_AutismConfig_Theme.danger = ThemePaletteManager.DEFAULT_COLORS[ThemePaletteManager.ColorRole.DANGER.ordinal()];
            }
         }

         this.init_AutismConfig_Theme.advanced = advanced;
         this.read_I = 0;
         this.write_I = 0;
         this.load_V();
         this.read_V();
      }
   }

   private void write_V() {
      AutismConfig.ThemeColors def = new AutismConfig.ThemeColors();
      def.advanced = this.init_AutismConfig_Theme.advanced;
      init_V_AutismConfig_Theme_AutismConfig_Theme(def, this.init_AutismConfig_Theme);
      this.load_V();
      this.read_V();
      bi.load("Theme reset to defaults");
   }

   private void update_V() {
      init_V_AutismConfig_Theme_AutismConfig_Theme(this.init_AutismConfig_Theme, this.init_AutismConfig.themeColors);
      ThemePaletteManager.reload();
      Runnable saved = autismclient.gui.ThemeApplyProgressOverlay.init("Saving config");
      autismclient.util.BackgroundTaskExecutor.init_Future_Str_Runnable("theme-config-save", () -> {
         try {
            this.init_AutismConfig.save();
         } finally {
            saved.run();
         }
      });
      bi.load("Theme applied");
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(this.init_Screen);
   }

   @Override
   public void removed() {
      if (this.init_cu_CachePool != null) {
         this.init_cu_CachePool.close();
         this.init_cu_CachePool = null;
      }

      if (this.load_cu_CachePool != null) {
         this.load_cu_CachePool.close();
         this.load_cu_CachePool = null;
      }

      if (this.read_cu_CachePool != null) {
         this.read_cu_CachePool.close();
         this.read_cu_CachePool = null;
      }
   }

   private static void init_V_AutismConfig_Theme_AutismConfig_Theme(AutismConfig.ThemeColors from, AutismConfig.ThemeColors to) {
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

   private record Inner_BufferHelper(int init_I, int load, boolean init_Z, int read, int write) {
   }

   private static final class Inner_CachePool {
      private static final int init_I = 130;
      private static final int load_I = 90;
      private static final int read_I = 14;
      private static final int write_I = 6;
      private static final int update_I = 9;
      private static final int sync_I = 12;
      private IntConsumer init_IntConsumer;
      private boolean init_Z;
      private ThemePaletteManager.CompiledPalette init_ct_StreamUtil;
      private float init_F;
      private float load_F;
      private float read_F;
      private int flush_I;
      private int close;
      private int open;
      private int poll = 255;
      private autismclient.gui.f.NetHelper init_NetHelper;
      private autismclient.gui.f.NetHelper load_NetHelper;
      private autismclient.gui.f.NetHelper read_NetHelper;
      private autismclient.gui.f.NetHelper write_NetHelper;
      private autismclient.gui.f.NetHelper update_NetHelper;
      private autismclient.gui.f.NetHelper sync_NetHelper;
      private autismclient.gui.f.NetHelper flush_NetHelper;
      private int push;
      private autismclient.util.ThemedTextInputBox init_k;

      static int init_I_Z(boolean alpha) {
         return 94 + read(alpha);
      }

      static int load_I_Z(boolean alpha) {
         return init_I() + 4 + read(alpha);
      }

      private static int init_I() {
         return Math.max(36, autismclient.gui.ui.UiDensityMetrics.scaledHeight(13) * 2 + 2);
      }

      private static int read(boolean alpha) {
         return ((alpha ? 4 : 3) - 1) * 12 + 9;
      }

      private int init(int color, ThemePaletteManager.ColorRole ch) {
         return ThemePaletteManager.recolorWithPalette(color, ch, this.init_ct_StreamUtil);
      }

      void init_V_I(int argb) {
         this.poll = argb >>> 24 & 0xFF;
         this.flush_I = argb >>> 16 & 0xFF;
         this.close = argb >>> 8 & 0xFF;
         this.open = argb & 0xFF;
         float[] hsb = Color.RGBtoHSB(this.flush_I, this.close, this.open, null);
         this.init_F = hsb[0];
         this.load_F = hsb[1];
         this.read_F = hsb[2];
         if (this.init_k != null) {
            this.init_k.setFocused(false);
         }

         this.load_V();
      }

      private int load_I() {
         return (this.init_Z ? this.poll : 255) << 24 | this.flush_I << 16 | this.close << 8 | this.open;
      }

      private void init_V() {
         if (this.init_k == null) {
            this.init_k = new autismclient.util.ThemedTextInputBox(init_Minecraft(), init_Minecraft().font, 0, 0, 20, autismclient.gui.ui.UiDensityMetrics.scaledHeight(13), false);
            this.init_k.setMaxLength(6);
            this.init_k.setFilter(next -> next.chars().allMatch(ch -> init_Z_C((char)ch)));
            this.init_k.setChangedListener(value -> this.sync());
            this.init_k.setValue(String.format(Locale.ROOT, "%06X", this.flush_I << 16 | this.close << 8 | this.open));
         }
      }

      private void load_V() {
         if (this.init_k != null && !this.init_k.isFocused()) {
            this.init_k.setValue(String.format(Locale.ROOT, "%06X", this.flush_I << 16 | this.close << 8 | this.open));
         }
      }

      private void read() {
         int rgb = Color.HSBtoRGB(this.init_F, this.load_F, this.read_F) & 16777215;
         this.flush_I = rgb >> 16 & 0xFF;
         this.close = rgb >> 8 & 0xFF;
         this.open = rgb & 0xFF;
         this.load_V();
         this.update();
      }

      private void write() {
         float[] hsb = Color.RGBtoHSB(this.flush_I, this.close, this.open, null);
         this.init_F = hsb[0];
         this.load_F = hsb[1];
         this.read_F = hsb[2];
         this.load_V();
         this.update();
      }

      private void update() {
         if (this.init_IntConsumer != null) {
            this.init_IntConsumer.accept(this.load_I());
         }
      }

      void init(autismclient.gui.f.SyncManager ctx, autismclient.gui.f.NetHelper area) {
         GuiGraphicsExtractor g2 = ctx.init_GuiGraphicsExtract();
         int slidersH = read(this.init_Z);
         int svH = Math.max(init_I(), Math.min(90, area.sync() - 4 - slidersH));
         this.init_NetHelper = autismclient.gui.f.NetHelper.init(area.read(), area.write(), 130, svH);
         this.load_NetHelper = autismclient.gui.f.NetHelper.init(this.init_NetHelper.init() + 6, area.write(), 14, svH);
         ThemeColorScreen.Inner_StreamUtil.init(this.init_F).init(g2, this.init_NetHelper);
         autismclient.gui.f.PacketUtil.load(g2, this.init_NetHelper, ctx.init_TableIndex().colors().borderSubtle);
         int cx = this.init_NetHelper.read() + Math.round(this.load_F * (this.init_NetHelper.update() - 1));
         int cy = this.init_NetHelper.write() + Math.round((1.0F - this.read_F) * (this.init_NetHelper.sync() - 1));
         autismclient.gui.f.PacketUtil.load(g2, autismclient.gui.f.NetHelper.init(cx - 4, cy - 4, 8, 8), 4, 0, -1342177280);
         autismclient.gui.f.PacketUtil.load(g2, autismclient.gui.f.NetHelper.init(cx - 3, cy - 3, 6, 6), 3, 0, -1);
         ThemeColorScreen.Inner_StreamUtil.init_CacheEntry_StreamU().init(g2, this.load_NetHelper);
         autismclient.gui.f.PacketUtil.load(g2, this.load_NetHelper, ctx.init_TableIndex().colors().borderSubtle);
         int hy = this.load_NetHelper.write() + Math.round((1.0F - this.init_F) * (this.load_NetHelper.sync() - 1));
         autismclient.gui.f.PacketUtil.load(
            g2, autismclient.gui.f.NetHelper.init(this.load_NetHelper.read() - 1, hy - 1, this.load_NetHelper.update() + 2, 3), 1, 0, -1
         );
         int rx = this.load_NetHelper.init() + 6;
         int rw = Math.max(58, area.init() - rx);
         int ctrlH = autismclient.gui.ui.UiDensityMetrics.scaledHeight(13);
         autismclient.gui.f.NetHelper swatch = autismclient.gui.f.NetHelper.init(rx, area.write(), rw, ctrlH);
         autismclient.gui.f.PacketUtil.load(g2, swatch, 3, -12961222);
         autismclient.gui.f.PacketUtil.load(g2, swatch, 3, this.load_I(), ctx.init_TableIndex().colors().borderSubtle);
         this.flush_NetHelper = autismclient.gui.f.NetHelper.init(rx, swatch.load() + 2, rw, ctrlH);
         this.init_V();
         this.init_k.setX(this.flush_NetHelper.read());
         this.init_k.setY(this.flush_NetHelper.write());
         this.init_k.setWidth(this.flush_NetHelper.update());
         this.init_k.setHeight(this.flush_NetHelper.sync());
         this.init_k.render(g2, ctx.read(), ctx.write(), 0.0F);
         int sy = this.init_NetHelper.load() + 4;
         this.read_NetHelper = autismclient.gui.f.NetHelper.init(area.read() + 14, sy, area.update() - 52, 9);
         this.write_NetHelper = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 12, area.update() - 52, 9);
         this.update_NetHelper = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 24, area.update() - 52, 9);
         this.init_V_SyncManager(ctx, this.read_NetHelper, "R", this.flush_I, -43691);
         this.init_V_SyncManager(ctx, this.write_NetHelper, "G", this.close, -11149961);
         this.init_V_SyncManager(ctx, this.update_NetHelper, "B", this.open, -10053121);
         if (this.init_Z) {
            this.sync_NetHelper = autismclient.gui.f.NetHelper.init(area.read() + 14, sy + 36, area.update() - 52, 9);
            this.init_V_SyncManager(ctx, this.sync_NetHelper, "A", this.poll, -4210753);
         } else {
            this.sync_NetHelper = null;
         }
      }

      private void init_V_SyncManager(autismclient.gui.f.SyncManager context, autismclient.gui.f.NetHelper b2, String label, int value, int accent) {
         GuiGraphicsExtractor g2 = context.init_GuiGraphicsExtract();
         k.init(
            g2,
            init_Minecraft().font,
            label,
            autismclient.gui.f.a.DataUtil.init,
            this.init(-4743522, ThemePaletteManager.ColorRole.TEXT),
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
            init_Minecraft().font,
            Integer.toString(value),
            autismclient.gui.f.a.DataUtil.init,
            this.init(-791321, ThemePaletteManager.ColorRole.TEXT),
            b2.init() + 5,
            b2.write() + 1,
            false
         );
      }

      boolean init_Z(int mx, int my, int button) {
         if (this.init_NetHelper == null) {
            return false;
         } else {
            this.init_V();
            if (this.init_k.mouseClicked(mx, my, button)) {
               return true;
            } else if (this.init_NetHelper.init_Z(mx, my)) {
               this.push = 1;
               this.init(mx, my);
               return true;
            } else if (this.load_NetHelper.init_Z(mx, my)) {
               this.push = 2;
               this.load_V_I(my);
               return true;
            } else if (this.read_NetHelper.init_Z(mx, my)) {
               this.push = 3;
               this.init(mx, this.read_NetHelper, 0);
               return true;
            } else if (this.write_NetHelper.init_Z(mx, my)) {
               this.push = 4;
               this.init(mx, this.write_NetHelper, 1);
               return true;
            } else if (this.update_NetHelper.init_Z(mx, my)) {
               this.push = 5;
               this.init(mx, this.update_NetHelper, 2);
               return true;
            } else if (this.sync_NetHelper != null && this.sync_NetHelper.init_Z(mx, my)) {
               this.push = 6;
               this.init(mx, this.sync_NetHelper, 3);
               return true;
            } else {
               return false;
            }
         }
      }

      void init_V_I(int mx, int my, int button, double dx, double dy) {
         if (this.init_k == null || !this.init_k.mouseDragged(mx, my, button, dx, dy)) {
            switch (this.push) {
               case 1:
                  this.init(mx, my);
                  break;
               case 2:
                  this.load_V_I(my);
                  break;
               case 3:
                  this.init(mx, this.read_NetHelper, 0);
                  break;
               case 4:
                  this.init(mx, this.write_NetHelper, 1);
                  break;
               case 5:
                  this.init(mx, this.update_NetHelper, 2);
                  break;
               case 6:
                  if (this.sync_NetHelper != null) {
                     this.init(mx, this.sync_NetHelper, 3);
                  }
            }
         }
      }

      void init_V(int mx, int my, int button) {
         this.push = 0;
         if (this.init_k != null) {
            this.init_k.mouseReleased(mx, my, button);
         }
      }

      boolean init_Z_KeyEvent(KeyEvent input) {
         if (this.init_k != null && this.init_k.isFocused()) {
            int key = Compat263.keyEventGlfwKey(input);
            if (key != 257 && key != 335 && key != 256) {
               this.init_k.keyPressed(input);
               return true;
            } else {
               this.init_k.setFocused(false);
               this.load_V();
               return true;
            }
         } else {
            return false;
         }
      }

      boolean init_Z_CharacterEvent(CharacterEvent input) {
         if (this.init_k != null && this.init_k.isFocused()) {
            this.init_k.charTyped(input);
            return true;
         } else {
            return false;
         }
      }

      private void sync() {
         String hexText = this.init_k == null ? "" : this.init_k.getValue();
         if (hexText.length() == 6) {
            try {
               int rgb = Integer.parseInt(hexText, 16) & 16777215;
               this.flush_I = rgb >> 16 & 0xFF;
               this.close = rgb >> 8 & 0xFF;
               this.open = rgb & 0xFF;
               float[] hsb = Color.RGBtoHSB(this.flush_I, this.close, this.open, null);
               this.init_F = hsb[0];
               this.load_F = hsb[1];
               this.read_F = hsb[2];
               this.update();
            } catch (NumberFormatException var4) {
            }
         }
      }

      private void init(int mx, int my) {
         this.load_F = init_F_F((float)(mx - this.init_NetHelper.read()) / Math.max(1, this.init_NetHelper.update() - 1));
         this.read_F = init_F_F(1.0F - (float)(my - this.init_NetHelper.write()) / Math.max(1, this.init_NetHelper.sync() - 1));
         this.read();
      }

      private void load_V_I(int my) {
         this.init_F = init_F_F(1.0F - (float)(my - this.load_NetHelper.write()) / Math.max(1, this.load_NetHelper.sync() - 1));
         this.read();
      }

      private void init(int mx, autismclient.gui.f.NetHelper bounds, int channel) {
         int value = Math.round(init_F_F((float)(mx - bounds.read()) / Math.max(1, bounds.update())) * 255.0F);
         if (channel == 0) {
            this.flush_I = value;
         } else if (channel == 1) {
            this.close = value;
         } else {
            if (channel != 2) {
               this.poll = value;
               this.update();
               return;
            }

            this.open = value;
         }

         this.write();
      }

      private static boolean init_Z_C(char c) {
         return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
      }

      private static float init_F_F(float v) {
         return Math.max(0.0F, Math.min(1.0F, v));
      }

      private static Minecraft init_Minecraft() {
         return Minecraft.getInstance();
      }
   }

   private record Inner_DataUtil(String init_Str, IntSupplier init_IntSupplier, IntConsumer init_IntConsumer, ThemePaletteManager.ColorRole init_ct_DataUtil) {
   }

   private static final class Inner_StreamUtil extends AbstractTexture {
      private static final int init_I = 360;
      private static ThemeColorScreen.Inner_StreamUtil init_CacheEntry_StreamU;
      private static ThemeColorScreen.Inner_StreamUtil load_CacheEntry_StreamU;
      private static int load_I = -2147483648;
      private final Identifier init_Identifier;
      private final NativeImage init_NativeImage;
      private final int read;
      private final int write;

      private Inner_StreamUtil(Identifier id, String label, int w, int h) {
         this.init_Identifier = id;
         this.read = w;
         this.write = h;
         this.init_NativeImage = new NativeImage(w, h, true);
         this.texture = RenderSystem.getDevice().createTexture("AUTISM " + label, 5, GpuFormat.RGBA8_UNORM, w, h, 1, 1);
         this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
         this.textureView = RenderSystem.getDevice().createTextureView(this.texture);
         Minecraft.getInstance().getTextureManager().register(id, this);
      }

      static ThemeColorScreen.Inner_StreamUtil init_CacheEntry_StreamU() {
         if (init_CacheEntry_StreamU == null) {
            init_CacheEntry_StreamU = new ThemeColorScreen.Inner_StreamUtil(
               Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/picker_hue"), "theme picker hue", 14, 96
            );

            for (int y = 0; y < init_CacheEntry_StreamU.write; y++) {
               int color = 0xFF000000 | Color.HSBtoRGB(1.0F - (float)y / (init_CacheEntry_StreamU.write - 1), 1.0F, 1.0F) & 16777215;

               for (int x = 0; x < init_CacheEntry_StreamU.read; x++) {
                  init_CacheEntry_StreamU.init_NativeImage.setPixel(x, y, color);
               }
            }

            init_CacheEntry_StreamU.init_V();
         }

         return init_CacheEntry_StreamU;
      }

      static ThemeColorScreen.Inner_StreamUtil init(float hue) {
         int bucket = Math.round(ThemeColorScreen.Inner_CachePool.init_F_F(hue) * 360.0F);
         if (load_CacheEntry_StreamU == null) {
            load_CacheEntry_StreamU = new ThemeColorScreen.Inner_StreamUtil(
               Identifier.fromNamespaceAndPath("autismclient", "dynamic/theme/picker_sv"), "theme picker sv", 130, 96
            );
         }

         if (load_I != bucket) {
            float hh = bucket / 360.0F;

            for (int x = 0; x < load_CacheEntry_StreamU.read; x++) {
               float s = (float)x / (load_CacheEntry_StreamU.read - 1);

               for (int y = 0; y < load_CacheEntry_StreamU.write; y++) {
                  float v = 1.0F - (float)y / (load_CacheEntry_StreamU.write - 1);
                  load_CacheEntry_StreamU.init_NativeImage.setPixel(x, y, 0xFF000000 | Color.HSBtoRGB(hh, s, v) & 16777215);
               }
            }

            load_CacheEntry_StreamU.init_V();
            load_I = bucket;
         }

         return load_CacheEntry_StreamU;
      }

      private void init_V() {
         RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.texture, this.init_NativeImage);
      }

      private void init(GuiGraphicsExtractor g, autismclient.gui.f.NetHelper area) {
         g.blit(
            RenderPipelines.GUI_TEXTURED,
            this.init_Identifier,
            area.read(),
            area.write(),
            0.0F,
            0.0F,
            area.update(),
            area.sync(),
            this.read,
            this.write,
            this.read,
            this.write,
            -1
         );
      }
   }
}
