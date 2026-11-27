package autismclient.gui.screens;

import autismclient.compat263.Compat263;
import autismclient.gui.ui.widgets.TooltipRenderer;
import autismclient.gui.ui.widgets.UiLayoutMath;
import autismclient.gui.f.b.k;
import autismclient.gui.f.b.l;
import autismclient.modules.Module;
import autismclient.modules.aD;
import autismclient.modules.ae;
import autismclient.modules.al;
import autismclient.util.AutismLiteVariant;
import autismclient.util.AutismProfilesOverlay;
import autismclient.util.L;
import autismclient.util.aJ;
import autismclient.util.ScreenMacroStopWidget;
import autismclient.util.CustomMainMenuGuard;
import autismclient.util.bK;
import autismclient.util.OverlayManager;
import autismclient.util.cA;
import autismclient.util.AutismOverlay;
import autismclient.util.ThemePaletteManager;
import autismclient.util.ThemeTextureRecolorer;
import autismclient.util.UiColorPalette;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;

public class AutismTitleScreen extends Screen implements ScreenButtonBoundsProvider {
   private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/autism_client_logo.png");
   private static final Identifier CLICK_SOUND_ID = Identifier.fromNamespaceAndPath("autismclient", "gui.main_menu_click");
   private static final SoundEvent CLICK_SOUND = SoundEvent.createVariableRangeEvent(CLICK_SOUND_ID);
   private static final int LOGO_WIDTH = 516;
   private static final int LOGO_HEIGHT = 144;
   private static final Identifier SINGLEPLAYER_TEXTURE = titleTexture("singleplayer");
   private static final Identifier MULTIPLAYER_TEXTURE = titleTexture("multiplayer");
   private static final Identifier REALMS_TEXTURE = titleTexture("minecraft_realms");
   private static final Identifier OPTIONS_TEXTURE = titleTexture("options");
   private static final Identifier QUIT_TEXTURE = titleTexture("quit_game");
   private static final Identifier MODS_ICON = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/icons/modmenu.png");
   private static final Identifier pop_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/icons/discord.png");
   private static final int read_I = 32;
   private static final int write_I = 32;
   private static final Identifier LANGUAGE_ICON = Identifier.fromNamespaceAndPath("minecraft", "icon/language");
   private static final Identifier ACCESSIBILITY_ICON = Identifier.fromNamespaceAndPath("minecraft", "icon/accessibility");
   private static final int update_I = 15;
   private static final int sync_I = 32;
   private static final Component TITLE = Component.translatable("narrator.screen.title");
   private static final int open_I = 5;
   private static final int STATUS_ROW_HEIGHT = 11;
   private static final int push_I = 15;
   private static final int pop_I = 12;
   private static final int tick_I = 16;
   private final autismclient.gui.f.b.CharEncoder theme = new autismclient.gui.f.b.CharEncoder();
   private final List<AutismTitleScreen.Inner_StreamUtil> buttons = new ArrayList<>();
   private final String modCountText = modCountLabel();
   private final boolean modMenuLoaded = FabricLoader.getInstance().isModLoaded("modmenu");
   private int cachedBadgeCount = -1;
   private long badgeCountAt;
   private boolean layoutDirty = true;
   private int layoutWidth = -1;
   private int set = -1;
   private final long openedAt = System.nanoTime();
   private int put;
   private int add;
   private int clear;
   private int reset;
   private int start;
   private int stop;
   private int next;
   private int prev;
   private int valid;
   private int size;
   private int data;
   private int value;
   private int cache;
   private int state;
   private int queue;
   private int buffer;
   private int entry;
   private boolean flush_Z;
   private int node;
   private final List<AutismTitleScreen.Inner_ConfigStore> statusRows = new ArrayList<>();
   private final List<AutismTitleScreen.Inner_DataUtil> moduleRows = new ArrayList<>();
   private int index;
   private int count;
   private static final Component COPYRIGHT = Component.translatable("title.credits");
   private static final Random RANDOM = new Random();
   private static List<String> splashPool;
   private final LogoRenderer logoRenderer = new LogoRenderer(false);
   private SplashRenderer splash;
   private boolean close_Z;
   private static final int flags = 16;

   public AutismTitleScreen() {
      super(TITLE);
   }

   @Override
   protected void init() {
      if (!CustomMainMenuGuard.init() && this.minecraft != null) {
         this.minecraft.gui.setScreen(new TitleScreen());
      } else {
         this.layoutDirty = true;
         this.cachedBadgeCount = -1;
      }
   }

   private void addVanillaButtons() {
      int spacing = 24;
      int topPos = this.height / 4 + 48;
      this.addRenderableWidget(
         Button.builder(Component.translatable("menu.singleplayer"), b -> this.minecraft.gui.setScreen(new SelectWorldScreen(this)))
            .bounds(this.width / 2 - 100, topPos, 200, 20)
            .build()
      );
      Component disabledReason = this.multiplayerDisabledReason();
      boolean multiplayerAllowed = disabledReason == null;
      Tooltip tooltip = disabledReason != null ? Tooltip.create(disabledReason) : null;
      int multiplayerY = topPos + spacing;
      this.addRenderableWidget(Button.builder(Component.translatable("menu.multiplayer"), b -> {
         Screen next = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
         this.minecraft.gui.setScreen(next);
      }).bounds(this.width / 2 - 100, multiplayerY, 200, 20).tooltip(tooltip).build()).active = multiplayerAllowed;
      int realmsY = multiplayerY + spacing;
      this.addRenderableWidget(
            Button.builder(Component.translatable("menu.online"), b -> this.minecraft.gui.setScreen(new RealmsMainScreen(this)))
               .bounds(this.width / 2 - 100, realmsY, 200, 20)
               .tooltip(tooltip)
               .build()
         )
         .active = multiplayerAllowed;
      int rowY = realmsY + 36;
      SpriteIconButton language = this.addRenderableWidget(
         CommonButtons.language(
            20, b -> this.minecraft.gui.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true
         )
      );
      language.setPosition(this.width / 2 - 124, rowY);
      this.addRenderableWidget(
         Button.builder(
               Component.translatable("menu.options"), b -> this.minecraft.gui.setScreen(Compat263.newOptionsScreen(this, this.minecraft.options, false))
            )
            .bounds(this.width / 2 - 100, rowY, 98, 20)
            .build()
      );
      this.addRenderableWidget(Button.builder(Component.translatable("menu.quit"), b -> this.minecraft.stop()).bounds(this.width / 2 + 2, rowY, 98, 20).build());
      SpriteIconButton accessibility = this.addRenderableWidget(
         CommonButtons.accessibility(20, b -> this.minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true)
      );
      accessibility.setPosition(this.width / 2 + 104, rowY);
      int copyrightWidth = this.font.width(COPYRIGHT);
      this.addRenderableWidget(
         new PlainTextButton(
            this.width - copyrightWidth - 2,
            this.height - 10,
            copyrightWidth,
            10,
            COPYRIGHT,
            b -> this.minecraft.gui.setScreen(new CreditsAndAttributionScreen(this)),
            this.font
         )
      );
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      long perf = bK.init_J();
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      float uiMouseX = (float)cA.init_D_D(mouseX);
      float uiMouseY = (float)cA.init_D_D(mouseY);
      this.layout();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         if (this.flush_Z) {
            this.load(graphics);
            this.read(graphics);
            this.write(graphics);
         }

         this.renderLogo(graphics);
         Component hoveredTooltip = null;

         for (AutismTitleScreen.Inner_StreamUtil button : this.buttons) {
            button.render(graphics, uiMouseX, uiMouseY, delta);
            if (button.contains(uiMouseX, uiMouseY)) {
               graphics.requestCursor(button.enabled ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
               if (button.tooltip != null) {
                  hoveredTooltip = button.tooltip;
               }
            }
         }

         if (hoveredTooltip != null) {
            this.renderTooltip(graphics, hoveredTooltip, uiMouseX, uiMouseY);
         }

         this.update(graphics);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
         bK.init("title.render", perf);
      }

      this.init_V_GuiGraphicsExtract(graphics, mouseX, mouseY);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      if (ScreenMacroStopWidget.isVisible(this.minecraft) && (AutismLiteVariant.enabled() || autismclient.e.HashUtil.init())) {
         List<autismclient.gui.f.NetHelper> avoid = new ArrayList<>();
         avoid.add(autismclient.gui.f.NetHelper.init(this.put, this.add, this.clear, this.reset));
         if (this.flush_Z) {
            avoid.add(autismclient.gui.f.NetHelper.init(this.valid, this.size, this.data, this.value));
            avoid.add(autismclient.gui.f.NetHelper.init(this.state, this.queue, this.buffer, this.entry));
         }

         for (AutismTitleScreen.Inner_StreamUtil button : this.buttons) {
            avoid.add(autismclient.gui.f.NetHelper.init(button.x, button.y, button.width, button.height));
         }

         graphics.nextStratum();
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            ScreenMacroStopWidget.render(graphics, this, mouseX, mouseY, avoid.toArray(new autismclient.gui.f.NetHelper[0]));
         } catch (Throwable var10) {
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      super.extractRenderState(graphics, mouseX, mouseY, delta);
      this.logoRenderer.extractRenderState(graphics, this.width, 1.0F);
      if (!this.close_Z) {
         this.splash = this.pickSplash();
         this.close_Z = true;
      }

      if (this.splash != null && !this.minecraft.options.hideSplashTexts().get()) {
         this.splash.extractRenderState(graphics, this.width, this.font, 1.0F);
      }

      String version = "Minecraft " + SharedConstants.getCurrentVersion().name();
      graphics.text(this.font, version, 2, this.height - 10, UiColorPalette.accent());
   }

   private SplashRenderer pickSplash() {
      List<String> pool = this.loadSplashPool();
      if (pool.isEmpty()) {
         return null;
      }

      String text = pool.get(RANDOM.nextInt(pool.size()));
      return new SplashRenderer(Component.literal(text).setStyle(Style.EMPTY.withColor(-256)));
   }

   private List<String> loadSplashPool() {
      if (splashPool != null) {
         return splashPool;
      }

      List<String> pool = new ArrayList<>();

      try {
         IoSupplier<InputStream> supplier = Compat263.vanillaGetResource(
            this.minecraft.getVanillaPackResources(), PackType.CLIENT_RESOURCES, Identifier.withDefaultNamespace("texts/splashes.txt")
         );
         if (supplier != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(supplier.get(), StandardCharsets.UTF_8))) {
               reader.lines().map(String::trim).filter(line -> !line.isEmpty() && line.hashCode() != 125780783).forEach(pool::add);
            }
         }
      } catch (Exception var8) {
      }

      splashPool = List.copyOf(pool);
      return splashPool;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return false;
      }

      float uiMouseX = (float)cA.init_D_D(event.x());
      float uiMouseY = (float)cA.init_D_D(event.y());
      this.layout();

      for (AutismTitleScreen.Inner_StreamUtil button : this.buttons) {
         if (button.load(uiMouseX, uiMouseY)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public void removed() {
   }

   @Override
   public Map<String, autismclient.gui.f.NetHelper> buttonBounds() {
      this.layout();
      double sx = cA.init_I() == 0 ? 1.0 : (double)this.width / cA.init_I();
      double sy = cA.load_I() == 0 ? 1.0 : (double)this.height / cA.load_I();
      Map<String, autismclient.gui.f.NetHelper> out = new LinkedHashMap<>();

      for (AutismTitleScreen.Inner_StreamUtil button : this.buttons) {
         if (button.enabled) {
            String key = button.label.getString();
            if (key.isBlank()) {
               key = "button-" + out.size();
            }

            out.put(
               key,
               autismclient.gui.f.NetHelper.init(
                  (int)Math.round(button.x * sx),
                  (int)Math.round(button.y * sy),
                  (int)Math.round(button.width * sx),
                  (int)Math.round(button.height * sy)
               )
            );
         }
      }

      return out;
   }

   private void layout() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      if (this.layoutDirty || screenW != this.layoutWidth || screenH != this.set) {
         this.layoutDirty = false;
         this.layoutWidth = screenW;
         this.set = screenH;
         boolean compact = screenH < 320;
         int margin = 8;
         this.clear = Math.min(296, Math.max(180, screenW - margin * 2));
         this.reset = Math.max(20, Math.round(144.0F * Math.min(1.0F, (this.clear - 16) / 516.0F)));
         int rowW = Math.min(this.clear - 28, compact ? 220 : 240);
         int rowH = compact ? 18 : 22;
         int rowGap = compact ? 3 : 5;
         int cardToRows = compact ? 6 : 10;
         int stackH = this.reset + cardToRows + rowH * 5 + rowGap * 4;
         int utilitySize = compact ? 16 : 20;
         int bottomBarH = utilitySize + margin + 14;
         this.put = (screenW - this.clear) / 2;
         this.add = Math.max(margin, (screenH - stackH - bottomBarH) / 2);
         int rowTop = this.add + this.reset + cardToRows;
         int rowX = this.put + (this.clear - rowW) / 2;
         int sideW = Math.min(172, (screenW - this.clear) / 2 - margin * 2);
         this.flush_Z = sideW >= 104 && screenH >= 250;
         Component disabledReason = this.multiplayerDisabledReason();
         boolean multiplayerAllowed = disabledReason == null;
         this.buttons.clear();
         AutismTitleScreen.Inner_StreamUtil singleplayer = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop, rowW, rowH, Component.translatable("menu.singleplayer"), true, () -> this.minecraft.gui.setScreen(new SelectWorldScreen(this))
            )
            .init(1);
         singleplayer.init(SINGLEPLAYER_TEXTURE, 248, 24, 124, 12);
         this.buttons.add(singleplayer);
         AutismTitleScreen.Inner_StreamUtil multiplayer = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop + rowH + rowGap, rowW, rowH, Component.translatable("menu.multiplayer"), multiplayerAllowed, () -> {
                  Screen next = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
                  this.minecraft.gui.setScreen(next);
               }
            )
            .init(disabledReason)
            .init(this.badgeText())
            .init(2);
         multiplayer.init(MULTIPLAYER_TEXTURE, 222, 24, 111, 12);
         this.buttons.add(multiplayer);
         AutismTitleScreen.Inner_StreamUtil realms = new AutismTitleScreen.Inner_StreamUtil(
               rowX,
               rowTop + (rowH + rowGap) * 2,
               rowW,
               rowH,
               Component.translatable("menu.online"),
               multiplayerAllowed,
               () -> this.minecraft.gui.setScreen(new RealmsMainScreen(this))
            )
            .init(disabledReason)
            .init(3);
         realms.init(REALMS_TEXTURE, 136, 24, 68, 12);
         this.buttons.add(realms);
         AutismTitleScreen.Inner_StreamUtil options = new AutismTitleScreen.Inner_StreamUtil(
               rowX,
               rowTop + (rowH + rowGap) * 3,
               rowW,
               rowH,
               Component.translatable("menu.options"),
               true,
               () -> this.minecraft.gui.setScreen(Compat263.newOptionsScreen(this, this.minecraft.options, false))
            )
            .init(4);
         options.init(OPTIONS_TEXTURE, 138, 24, 69, 12);
         this.buttons.add(options);
         AutismTitleScreen.Inner_StreamUtil quit = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop + (rowH + rowGap) * 4, rowW, rowH, Component.translatable("menu.quit"), true, () -> this.minecraft.stop()
            )
            .init(5);
         quit.init(QUIT_TEXTURE, 74, 24, 37, 12);
         this.buttons.add(quit);
         this.layoutUtilityButtons(screenW, screenH, margin, utilitySize);
         if (!this.flush_Z) {
            this.statusRows.clear();
            this.moduleRows.clear();
         } else {
            int maxModulesPanelH = screenH - bottomBarH - this.add;
            int fixedModulesH = 44;
            int maxCategoryRows = Math.max(1, (maxModulesPanelH - fixedModulesH - 3) / 12);
            this.buildModuleRows(maxCategoryRows);
            this.write();
            this.cache = 0;
            int statusValueW = 0;

            for (AutismTitleScreen.Inner_ConfigStore row : this.statusRows) {
               this.cache = Math.max(this.cache, k.init(this.font, row.label(), autismclient.gui.f.a.DataUtil.load, 0));
               statusValueW = Math.max(statusValueW, k.init(this.font, row.value().get(), autismclient.gui.f.a.DataUtil.init, 0));
            }

            this.data = this.cache + statusValueW + 24;
            this.value = 5 + this.statusRows.size() * 11 + 5 - 1;
            this.valid = margin;
            this.size = this.add;
            int supportRowH = Math.max(compact ? 16 : 20, k.init(autismclient.gui.f.a.DataUtil.load) + 8);
            int supportRowGap = 3;
            int supportIcon = Math.min(14, supportRowH - 6);
            String[] supportLabels = new String[]{"AUTISM INC", "AUTISM CLIENT"};
            int supportTextW = 0;

            for (String label : supportLabels) {
               supportTextW = Math.max(supportTextW, k.init(this.font, label, autismclient.gui.f.a.DataUtil.load, 0));
            }

            this.next = supportTextW + supportIcon + 22;
            this.prev = 5 + 2 * supportRowH + supportRowGap + 5;
            this.start = margin;
            this.stop = this.size + this.value + 6;
            int supportRowY = this.stop + 5;
            this.init(
               this.start + 4,
               supportRowY,
               this.next - 8,
               supportRowH,
               supportLabels[0],
               pop_Identifier,
               () -> aJ.init_V("https://discord.gg/V8GsKP6k5u"),
               "https://discord.gg/V8GsKP6k5u"
            );
            this.init(
               this.start + 4,
               supportRowY + supportRowH + supportRowGap,
               this.next - 8,
               supportRowH,
               supportLabels[1],
               pop_Identifier,
               () -> aJ.init_V("https://discord.com/invite/JZ7XgUCtBu"),
               "https://discord.com/invite/JZ7XgUCtBu"
            );
            String activeText = Integer.toString(this.count);
            String activeSuffix = " / " + this.index + " ACTIVE";
            int modulesContentW = 7
               + k.init(this.font, activeText, autismclient.gui.f.a.DataUtil.read, 0)
               + 3
               + k.init(this.font, activeSuffix, autismclient.gui.f.a.DataUtil.init, 0)
               + 7;

            for (AutismTitleScreen.Inner_DataUtil row : this.moduleRows) {
               if (row.load() < 0) {
                  modulesContentW = Math.max(modulesContentW, 12 + k.init(this.font, row.label(), autismclient.gui.f.a.DataUtil.init, 0));
               } else {
                  String count = row.active() + "/" + row.load();
                  modulesContentW = Math.max(
                     modulesContentW,
                     6
                        + k.init(this.font, row.label(), autismclient.gui.f.a.DataUtil.init, 0)
                        + 8
                        + k.init(this.font, count, autismclient.gui.f.a.DataUtil.init, 0)
                        + 6
                  );
               }
            }

            modulesContentW = Math.max(modulesContentW, 10 + k.init(this.font, "OPEN MANAGER", autismclient.gui.f.a.DataUtil.load, 0) + 20);
            this.buffer = Math.min(sideW, modulesContentW);
            this.entry = fixedModulesH + 3 + this.moduleRows.size() * 12;
            this.state = screenW - margin - this.buffer;
            this.queue = this.add;
            AutismTitleScreen.Inner_StreamUtil openManager = new AutismTitleScreen.Inner_StreamUtil(
                  this.state + 5, this.queue + this.entry - 5 - 16, this.buffer - 10, 16, Component.literal("OPEN MANAGER"), true, () -> {
                     if (!aD.read_Z()) {
                        this.minecraft.gui.setScreen(new AutismModulesScreen(this, AutismModulesScreen.Inner_DataUtil.TITLE_SETUP));
                     }
                  }
               )
               .init(0)
               .animated(260, true);
            openManager.write_Z = false;
            this.buttons.add(openManager);
         }
      }
   }

   private void init(int x, int y, int w, int h, String text, Identifier icon, Runnable action, String url) {
      this.buttons
         .add(
            new AutismTitleScreen.Inner_StreamUtil(x, y, w, h, Component.literal(text), true, action)
               .init_UtilCore_StreamUti_Identifier(icon, 32, 32, 14)
               .init()
               .init(Component.literal(url))
         );
   }

   private void layoutUtilityButtons(int screenW, int screenH, int margin, int utilitySize) {
      List<AutismTitleScreen.Inner_TaskQueue> all = new ArrayList<>();
      if (this.modMenuLoaded) {
         all.add(new AutismTitleScreen.Inner_TaskQueue("MODS", "", MODS_ICON, null, 32, () -> this.update(), Component.literal("Mod Menu")));
      }

      all.add(
         new AutismTitleScreen.Inner_TaskQueue(
            "LANGUAGE",
            "",
            null,
            LANGUAGE_ICON,
            32,
            () -> this.minecraft.gui.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())),
            Component.literal("Language")
         )
      );
      all.add(new AutismTitleScreen.Inner_TaskQueue("MODULES", "", autismclient.gui.f.a.DataUtil.pop, null, 32, () -> {
         if (!aD.read_Z()) {
            this.minecraft.gui.setScreen(new AutismModulesScreen(this, AutismModulesScreen.Inner_DataUtil.TITLE_SETUP));
         }
      }, Component.literal("Modules & Macros")));
      all.add(new AutismTitleScreen.Inner_TaskQueue("PROFILES", "", autismclient.gui.f.a.DataUtil.add, null, 32, () -> {
         if (!aD.read_Z()) {
            autismclient.modules.ModuleRuntimeManager mod = autismclient.modules.ModuleRuntimeManager.getInstance();
            AutismOverlay overlay = mod == null ? null : mod.getOrCreateProfilesOverlay();
            if (overlay != null) {
               OverlayManager.getInstance().register(overlay);
               ((AutismProfilesOverlay)overlay).load(true);
               overlay.setVisible(true);
               this.minecraft.gui.setScreen(new AutismOverlaysScreen(overlay, this, true));
            }
         }
      }, Component.literal("Profiles")));
      all.add(
         new AutismTitleScreen.Inner_TaskQueue(
            "ACCESSIBILITY",
            "",
            null,
            ACCESSIBILITY_ICON,
            32,
            () -> this.minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)),
            Component.literal("Accessibility")
         )
      );
      int gap = 4;
      this.node = all.isEmpty() ? 0 : all.size() * utilitySize + (all.size() - 1) * gap;
      int x = screenW - margin - this.node;
      int y = screenH - margin - utilitySize;

      for (int i = 0; i < all.size(); i++) {
         this.addUtilityButton(all.get(i), x + i * (utilitySize + gap), y, utilitySize);
      }
   }

   private void addUtilityButton(AutismTitleScreen.Inner_TaskQueue spec, int x, int y, int size) {
      AutismTitleScreen.Inner_StreamUtil button = new AutismTitleScreen.Inner_StreamUtil(
            x, y, size, size, Component.literal(spec.label()), spec.action() != null, spec.action()
         )
         .load(spec.load_Str())
         .init(spec.tooltip());
      if (spec.load_Identifier() != null) {
         button.load(spec.load_Identifier());
      } else if (spec.icon() != null) {
         button.withIcon(spec.icon(), spec.iconSize());
      }

      this.buttons.add(button);
   }

   private Component multiplayerDisabledReason() {
      if (this.minecraft.allowsMultiplayer()) {
         return null;
      } else if (this.minecraft.isNameBanned()) {
         return Component.translatable("title.multiplayer.disabled.banned.name");
      } else {
         BanDetails multiplayerBan = this.minecraft.multiplayerBan();
         if (multiplayerBan != null) {
            return multiplayerBan.expires() != null
               ? Component.translatable("title.multiplayer.disabled.banned.temporary")
               : Component.translatable("title.multiplayer.disabled.banned.permanent");
         } else {
            return Component.translatable("title.multiplayer.disabled");
         }
      }
   }

   private void write() {
      this.statusRows.clear();
      this.statusRows.add(new AutismTitleScreen.Inner_ConfigStore("USER:", () -> this.minecraft.getUser().getName()));
      this.statusRows.add(new AutismTitleScreen.Inner_ConfigStore("BUILD:", AutismTitleScreen::buildVersion));
   }

   private void buildModuleRows(int maxCategoryRows) {
      this.moduleRows.clear();
      int total = 0;
      int active = 0;
      List<AutismTitleScreen.Inner_DataUtil> all = new ArrayList<>();

      for (ae category : ae.init_List()) {
         List<Module> modules = al.init_List_ae(category);
         if (!modules.isEmpty()) {
            int categoryActive = 0;

            for (Module module : modules) {
               if (module.isEnabled()) {
                  categoryActive++;
               }
            }

            all.add(new AutismTitleScreen.Inner_DataUtil(category.load().toUpperCase(Locale.ROOT), categoryActive, modules.size()));
            total += modules.size();
            active += categoryActive;
         }
      }

      this.index = total;
      this.count = active;
      if (all.size() <= maxCategoryRows) {
         this.moduleRows.addAll(all);
      } else {
         int keep = Math.max(0, maxCategoryRows - 1);

         for (int i = 0; i < keep; i++) {
            this.moduleRows.add(all.get(i));
         }

         this.moduleRows.add(new AutismTitleScreen.Inner_DataUtil("+" + (all.size() - keep) + " MORE", -1, -1));
      }
   }

   private static String buildVersion() {
      String version = L.read_Str();
      return version != null && !version.isBlank() ? version : "unknown";
   }

   private float introProgress(int delayMs) {
      float elapsedMs = (float)(System.nanoTime() - this.openedAt) / 1000000.0F - delayMs;
      if (elapsedMs <= 0.0F) {
         return 0.0F;
      }

      float p = Math.min(1.0F, elapsedMs / 340.0F);
      return 1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p);
   }

   private static int introOffset(float progress) {
      return Math.round((1.0F - progress) * 5.0F);
   }

   private static int init(int color, float progress) {
      return autismclient.gui.f.PacketUtil.init(color, progress);
   }

   private void renderLogo(GuiGraphicsExtractor graphics) {
      float p = this.introProgress(0);
      if (!(p <= 0.0F)) {
         float scale = Math.min(1.0F, (this.clear - 16) / 516.0F);
         int drawW = Math.max(1, Math.round(516.0F * scale));
         int drawH = Math.max(1, Math.round(144.0F * scale));
         int logoX = UiLayoutMath.centerOffset(this.put, this.clear, drawW);
         int logoY = this.add + introOffset(p) + Math.max(0, (this.reset - drawH) / 2);
         graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ThemeTextureRecolorer.init(LOGO_TEXTURE, ThemePaletteManager.ColorRole.ACCENT),
            logoX,
            logoY,
            0.0F,
            0.0F,
            drawW,
            drawH,
            516,
            144,
            516,
            144,
            ARGB.white(p)
         );
      }
   }

   private void load(GuiGraphicsExtractor graphics) {
      float p = this.introProgress(140);
      if (!(p <= 0.0F)) {
         this.init(graphics, this.start, this.stop + introOffset(p), this.next, this.prev, null, p);
      }
   }

   private void read(GuiGraphicsExtractor graphics) {
      float p = this.introProgress(200);
      if (!(p <= 0.0F)) {
         int x = this.valid;
         int y = this.size + introOffset(p);
         this.init(graphics, x, y, this.data, this.value, null, p);
         int rowY = y + 5;
         int labelColor = init(this.theme.load(l.update), p * 0.85F);
         int valueColor = init(this.theme.load(l.read), p);

         for (AutismTitleScreen.Inner_ConfigStore row : this.statusRows) {
            k.init(graphics, this.font, row.label(), autismclient.gui.f.a.DataUtil.load, labelColor, x + 6, rowY, false);
            int valueX = x + 12 + this.cache;
            String value = this.ellipsize(row.value().get(), x + this.data - 6 - valueX, autismclient.gui.f.a.DataUtil.init, valueColor);
            k.init(graphics, this.font, value, autismclient.gui.f.a.DataUtil.init, valueColor, valueX, rowY + 1, false);
            rowY += 11;
         }
      }
   }

   private void write(GuiGraphicsExtractor graphics) {
      float p = this.introProgress(260);
      if (!(p <= 0.0F)) {
         int x = this.state;
         int y = this.queue + introOffset(p);
         this.init(graphics, x, y, this.buffer, this.entry, null, p);
         int accent = init(this.theme.load(l.update), p);
         int muted = init(this.theme.load(l.write), p);
         int body = init(this.theme.load(l.read), p);
         int bigY = y + 5;
         String activeText = Integer.toString(this.count);
         k.init(graphics, this.font, activeText, autismclient.gui.f.a.DataUtil.read, accent, x + 7, bigY, false);
         int activeW = k.init(this.font, activeText, autismclient.gui.f.a.DataUtil.read, accent);
         k.init(
            graphics,
            this.font,
            " / " + this.index + " ACTIVE",
            autismclient.gui.f.a.DataUtil.init,
            muted,
            x + 7 + activeW + 3,
            bigY + (k.init(autismclient.gui.f.a.DataUtil.read) - k.init(autismclient.gui.f.a.DataUtil.init)),
            false
         );
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + 5, bigY + 15, this.buffer - 10, init(this.theme.set(), p));
         int rowY = bigY + 15 + 3;

         for (AutismTitleScreen.Inner_DataUtil row : this.moduleRows) {
            if (row.load() < 0) {
               k.init(graphics, this.font, row.label(), autismclient.gui.f.a.DataUtil.init, muted, x + 6, rowY + 2, false);
            } else {
               String count = row.active() + "/" + row.load();
               int countColor = row.active() > 0 ? accent : muted;
               int countW = k.init(this.font, count, autismclient.gui.f.a.DataUtil.init, countColor);
               String name = this.ellipsize(row.label(), this.buffer - countW - 20, autismclient.gui.f.a.DataUtil.init, body);
               k.init(graphics, this.font, name, autismclient.gui.f.a.DataUtil.init, body, x + 6, rowY + 2, false);
               k.init(graphics, this.font, count, autismclient.gui.f.a.DataUtil.init, countColor, x + this.buffer - 6 - countW, rowY + 2, false);
            }

            rowY += 12;
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, String header, float p) {
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), init(this.theme.poll(), p), init(this.theme.set(), p)
      );
      int accent = init(this.theme.load(l.update), p);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + 1, y + 1, 5, accent);
      autismclient.gui.f.PacketUtil.load(graphics, x + 1, y + 1, 5, accent);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + w - 6, y + 1, 5, accent);
      autismclient.gui.f.PacketUtil.load(graphics, x + w - 2, y + 1, 5, accent);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + 1, y + h - 2, 5, accent);
      autismclient.gui.f.PacketUtil.load(graphics, x + 1, y + h - 6, 5, accent);
      autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + w - 6, y + h - 2, 5, accent);
      autismclient.gui.f.PacketUtil.load(graphics, x + w - 2, y + h - 6, 5, accent);
      if (header != null && !header.isBlank()) {
         String text = "/// " + header + " ///";
         int textW = k.init(this.font, text, autismclient.gui.f.a.DataUtil.load, accent);
         k.init(graphics, this.font, text, autismclient.gui.f.a.DataUtil.load, accent, x + (w - textW) / 2, y + 4, false);
      }
   }

   private void init(GuiGraphicsExtractor graphics, String text, int x, int y, int color, Identifier fontId) {
      k.init(graphics, this.font, text == null ? "" : text, fontId, color, x, y, false);
   }

   private void update(GuiGraphicsExtractor graphics) {
      float p = this.introProgress(320);
      if (!(p <= 0.0F)) {
         Identifier fontId = autismclient.gui.f.a.DataUtil.init;
         int muted = init(this.theme.load(l.write), p);
         int y = cA.load_I() - k.init(fontId) - 3;
         k.init(graphics, this.font, this.modCountText, fontId, muted, 4, y, false);
      }
   }

   private String ellipsize(String text, int maxWidth, Identifier fontId, int color) {
      if (text == null) {
         return "";
      }

      if (k.init(this.font, text, fontId, color) <= maxWidth) {
         return text;
      }

      String trimmed = text;

      while (trimmed.length() > 1 && k.init(this.font, trimmed + ".", fontId, color) > maxWidth) {
         trimmed = trimmed.substring(0, trimmed.length() - 1);
      }

      return trimmed + ".";
   }

   private void update() {
      try {
         Class<?> apiClass = Class.forName("com.terraformersmc.modmenu.api.ModMenuApi");
         Method createMethod = apiClass.getMethod("createModsScreen", Screen.class);
         Screen modsScreen = (Screen)createMethod.invoke(null, this);
         this.minecraft.gui.setScreen(modsScreen);
      } catch (Exception var4) {
      }
   }

   private static Identifier titleTexture(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/button_text/" + name + ".png");
   }

   private static String modCountLabel() {
      int modCount = FabricLoader.getInstance().getAllMods().size();
      return modCount + (modCount == 1 ? " Mod" : " Mods");
   }

   private void renderTooltip(GuiGraphicsExtractor graphics, Component tooltip, float uiMouseX, float uiMouseY) {
      TooltipRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, Math.round(uiMouseX), Math.round(uiMouseY)),
         tooltip.getString(),
         Math.round(uiMouseX),
         Math.round(uiMouseY),
         220
      );
   }

   private String badgeText() {
      int count = this.badgeCount();
      return count < 0 ? null : Integer.toString(count);
   }

   private int badgeCount() {
      long now = System.currentTimeMillis();
      if (this.cachedBadgeCount >= 0 && now - this.badgeCountAt < 2000L) {
         return this.cachedBadgeCount;
      }

      this.badgeCountAt = now;

      try {
         ServerList serverList = new ServerList(this.minecraft);
         serverList.load();
         this.cachedBadgeCount = serverList.size();
      } catch (RuntimeException ignored) {
         this.cachedBadgeCount = -1;
      }

      return this.cachedBadgeCount;
   }

   private record Inner_ConfigStore(String label, Supplier<String> value) {
   }

   private record Inner_DataUtil(String label, int active, int load) {
   }

   private final class Inner_StreamUtil {
      private final int x;
      private final int y;
      private final int width;
      private final int height;
      private final Component label;
      private final boolean enabled;
      private final Runnable onPress;
      private Identifier icon;
      private int iconTextureSize = 32;
      private Identifier sprite;
      private Identifier read_Identifier;
      private int sync = 32;
      private int flush = 32;
      private int close = 14;
      private Identifier labelTexture;
      private int open;
      private int poll;
      private int push;
      private int pop;
      private Component tooltip;
      private String rightBadge;
      private int tick = -1;
      private boolean load_Z;
      private String subtitle = "";
      private boolean read_Z;
      private boolean write_Z = true;
      private int run = 320;
      private boolean update_Z;

      private Inner_StreamUtil(int x, int y, int width, int height, Component label, boolean enabled, Runnable onPress) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.label = label;
         this.enabled = enabled;
         this.onPress = onPress;
      }

      private AutismTitleScreen.Inner_StreamUtil init(Component tooltip) {
         this.tooltip = tooltip;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(String rightBadge) {
         this.rightBadge = rightBadge;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(Identifier icon) {
         return this.withIcon(icon, 32);
      }

      private AutismTitleScreen.Inner_StreamUtil withIcon(Identifier icon, int iconTextureSize) {
         this.icon = icon;
         this.iconTextureSize = Math.max(1, iconTextureSize);
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil load(Identifier sprite) {
         this.sprite = sprite;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init_UtilCore_StreamUti_Identifier(Identifier icon, int textureWidth, int textureHeight, int drawSize) {
         this.read_Identifier = icon;
         this.sync = Math.max(1, textureWidth);
         this.flush = Math.max(1, textureHeight);
         this.close = Math.max(1, drawSize);
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(Identifier labelTexture, int labelTextureWidth, int labelTextureHeight, int labelDrawWidth, int labelDrawHeight) {
         this.labelTexture = labelTexture;
         this.open = labelTextureWidth;
         this.poll = labelTextureHeight;
         this.push = labelDrawWidth;
         this.pop = labelDrawHeight;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(int rowIndex) {
         this.tick = rowIndex;
         if (rowIndex > 0) {
            this.run = 60 + rowIndex * 50;
         }

         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init() {
         this.read_Z = true;
         this.run = 140;
         this.update_Z = true;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil animated(int delayMs, boolean slide) {
         this.run = delayMs;
         this.update_Z = slide;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil load(String subtitle) {
         this.load_Z = true;
         this.subtitle = subtitle == null ? "" : subtitle;
         return this;
      }

      private boolean contains(float mouseX, float mouseY) {
         return mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
      }

      private boolean load(float mouseX, float mouseY) {
         if (!this.contains(mouseX, mouseY)) {
            return false;
         }

         if (AutismTitleScreen.this.introProgress(this.run) < 1.0F) {
            return false;
         }

         if (!this.enabled) {
            return true;
         }

         AutismTitleScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AutismTitleScreen.CLICK_SOUND, 1.0F, 0.7F));
         if (this.onPress != null) {
            this.onPress.run();
         }

         return true;
      }

      private void render(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float delta) {
         float p = AutismTitleScreen.this.introProgress(this.run);
         if (!(p <= 0.0F)) {
            if (this.load_Z) {
               this.write(graphics, mouseX, mouseY, p);
            } else if (this.read_Z) {
               this.read(graphics, mouseX, mouseY, p);
            } else {
               this.load(graphics, mouseX, mouseY, p);
            }
         }
      }

      private void load(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float p) {
         boolean hovered = this.enabled && this.contains(mouseX, mouseY);
         int yDraw = this.y + (this.update_Z ? AutismTitleScreen.introOffset(p) : 0);
         int border = ThemePaletteManager.recolor(this.enabled ? -1721357268 : 1430596138, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.x, yDraw, this.width, this.height),
            AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -1207302650 : -1873801200, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.x + 1, yDraw + 1, this.width - 2, this.height - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
            int accent = AutismTitleScreen.this.theme.load(l.update);
            autismclient.gui.f.PacketUtil.load(
               graphics, autismclient.gui.f.NetHelper.init(this.x, yDraw, this.width, this.height), AutismTitleScreen.init(accent, hoverT * 0.8F * p)
            );
            autismclient.gui.f.PacketUtil.init(
               graphics, autismclient.gui.f.NetHelper.init(this.x, yDraw, 2, this.height), AutismTitleScreen.init(accent, hoverT * p)
            );
            if (this.write_Z) {
               int slide = Math.round(hoverT * 3.0F);
               autismclient.gui.f.PacketUtil.init(
                  graphics,
                  autismclient.gui.f.NetHelper.init(this.x + 4 + slide, yDraw + (this.height - 8) / 2, 7, 8),
                  false,
                  AutismTitleScreen.init(accent, hoverT * p)
               );
            }
         }

         String marker = this.rightBadge != null && !this.rightBadge.isBlank() ? this.rightBadge : null;
         if (this.labelTexture != null && this.open > 0 && this.poll > 0 && this.push > 0 && this.pop > 0) {
            int maxW = Math.max(1, this.width - 8);
            int maxH = Math.max(1, this.height <= 18 ? this.height - 4 : this.height - 8);
            float scale = Math.min(1.0F, Math.min((float)maxW / this.push, (float)maxH / this.pop));
            int drawW = Math.max(1, Math.round(this.push * scale));
            int drawH = Math.max(1, Math.round(this.pop * scale));
            int textX = this.x + (this.width - drawW) / 2;
            int textY = yDraw + (this.height - drawH) / 2;
            int textColor = AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               this.labelTexture,
               textX,
               textY,
               0.0F,
               0.0F,
               drawW,
               drawH,
               this.open,
               this.poll,
               this.open,
               this.poll,
               textColor
            );
         } else if (this.label != null) {
            String text = this.label.getString();
            int textColor = AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            Identifier fontId = autismclient.gui.f.a.DataUtil.load;
            int textY = UiLayoutMath.centerTextY(yDraw, this.height, k.init(fontId), 1);
            String drawText = text;
            int textX;
            if (this.read_Identifier != null) {
               int iconSize = Math.min(this.close, Math.max(1, Math.min(this.height - 2, this.width - 12)));
               int iconX = this.x + 3;
               int iconY = yDraw + (this.height - iconSize) / 2;
               graphics.blit(
                  RenderPipelines.GUI_TEXTURED,
                  ThemeTextureRecolorer.themed(this.read_Identifier),
                  iconX,
                  iconY,
                  0.0F,
                  0.0F,
                  iconSize,
                  iconSize,
                  this.sync,
                  this.flush,
                  this.sync,
                  this.flush,
                  AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p)
               );
               textX = iconX + iconSize + 4;
               int maxTextW = Math.max(1, this.x + this.width - 4 - textX);
               drawText = k.init(AutismTitleScreen.this.font, text, maxTextW, fontId, textColor);
            } else {
               int textW = k.init(AutismTitleScreen.this.font, text, fontId, textColor);
               textX = this.x + (this.width - textW) / 2;
            }

            k.init(graphics, AutismTitleScreen.this.font, drawText, fontId, textColor, textX, textY, false);
         }

         if (marker != null && !marker.isBlank()) {
            Identifier fontId = autismclient.gui.f.a.DataUtil.load;
            int markerColor = AutismTitleScreen.init(this.enabled ? ThemePaletteManager.recolor(-11052, ThemePaletteManager.ColorRole.ACCENT) : -8362651, p);
            int markerW = k.init(AutismTitleScreen.this.font, marker, fontId, markerColor);
            int markerX = this.x + this.width - markerW - 6;
            int markerY = UiLayoutMath.centerTextY(yDraw, this.height, k.init(fontId), 1);
            AutismTitleScreen.this.init(graphics, marker, markerX, markerY, markerColor, fontId);
         }
      }

      private void read(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float p) {
         boolean hovered = this.enabled && this.contains(mouseX, mouseY);
         int yDraw = this.y + (this.update_Z ? AutismTitleScreen.introOffset(p) : 0);
         int border = ThemePaletteManager.recolor(-1721357268, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.x, yDraw, this.width, this.height),
            AutismTitleScreen.init(ThemePaletteManager.recolor(-1878391290, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.x + 1, yDraw + 1, this.width - 2, this.height - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
            autismclient.gui.f.PacketUtil.load(
               graphics,
               autismclient.gui.f.NetHelper.init(this.x, yDraw, this.width, this.height),
               AutismTitleScreen.init(AutismTitleScreen.this.theme.load(l.update), hoverT * 0.8F * p)
            );
         }

         if (this.read_Identifier != null) {
            int iconSize = Math.min(this.close, Math.max(1, this.height - 6));
            int iconX = this.x + 5;
            int iconY = yDraw + (this.height - iconSize) / 2;
            int tint = AutismTitleScreen.init(ThemePaletteManager.recolor(-528658, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               ThemeTextureRecolorer.themed(this.read_Identifier),
               iconX,
               iconY,
               0.0F,
               0.0F,
               iconSize,
               iconSize,
               this.sync,
               this.flush,
               this.sync,
               this.flush,
               tint
            );
            if (hoverT > 0.001F) {
               graphics.blit(
                  RenderPipelines.GUI_TEXTURED,
                  ThemeTextureRecolorer.themed(this.read_Identifier),
                  iconX,
                  iconY,
                  0.0F,
                  0.0F,
                  iconSize,
                  iconSize,
                  this.sync,
                  this.flush,
                  this.sync,
                  this.flush,
                  AutismTitleScreen.init(AutismTitleScreen.this.theme.load(l.update), hoverT * p)
               );
            }
         }

         int textX = this.x + 5 + Math.min(this.close, Math.max(1, this.height - 6)) + 5;
         int maxTextW = Math.max(1, this.x + this.width - 4 - textX);
         int lineColor = AutismTitleScreen.init(AutismTitleScreen.this.theme.load(l.read), p);
         String line = k.init(AutismTitleScreen.this.font, this.label.getString(), maxTextW, autismclient.gui.f.a.DataUtil.load, lineColor);
         int lineY = UiLayoutMath.centerTextY(yDraw, this.height, k.init(autismclient.gui.f.a.DataUtil.load), 1);
         k.init(graphics, AutismTitleScreen.this.font, line, autismclient.gui.f.a.DataUtil.load, lineColor, textX, lineY, false);
      }

      private void write(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float p) {
         boolean hovered = this.enabled && this.contains(mouseX, mouseY);
         int border = ThemePaletteManager.recolor(this.enabled ? -1721357268 : 1430596138, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height),
            AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -1727527675 : 1716523024, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.x, this.y, this.width, this.height)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.x + 1, this.y + 1, this.width - 2, this.height - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
         }

         if (this.sprite != null) {
            int iconSize = Math.min(15, Math.max(12, this.height - 6));
            int iconX = this.x + (this.width - iconSize) / 2;
            int iconY = this.y + (this.height - iconSize) / 2;
            float alpha = (this.enabled ? 1.0F : 0.45F) * p;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.sprite, iconX, iconY, iconSize, iconSize, alpha);
         } else if (this.icon != null) {
            int iconSize = Math.min(16, Math.max(12, this.height - 7));
            int iconX = this.x + (this.width - iconSize) / 2;
            int iconY = this.y + (this.height - iconSize) / 2;
            int iconTint = AutismTitleScreen.init(ThemePaletteManager.recolor(this.enabled ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               ThemeTextureRecolorer.themed(this.icon),
               iconX,
               iconY,
               0.0F,
               0.0F,
               iconSize,
               iconSize,
               this.iconTextureSize,
               this.iconTextureSize,
               this.iconTextureSize,
               this.iconTextureSize,
               iconTint
            );
         }
      }
   }

   private record Inner_TaskQueue(
      String label, String load_Str, Identifier icon, Identifier load_Identifier, int iconSize, Runnable action, Component tooltip
   ) {
   }
}
