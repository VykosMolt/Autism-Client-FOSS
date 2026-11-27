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
   private static final Identifier init_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/autism_client_logo.png");
   private static final Identifier load_Identifier = Identifier.fromNamespaceAndPath("autismclient", "gui.main_menu_click");
   private static final SoundEvent init_SoundEvent = SoundEvent.createVariableRangeEvent(load_Identifier);
   private static final int init_I = 516;
   private static final int load_I = 144;
   private static final Identifier read_Identifier = init_Identifier_Str("singleplayer");
   private static final Identifier write_Identifier = init_Identifier_Str("multiplayer");
   private static final Identifier update_Identifier = init_Identifier_Str("minecraft_realms");
   private static final Identifier sync_Identifier = init_Identifier_Str("options");
   private static final Identifier flush_Identifier = init_Identifier_Str("quit_game");
   private static final Identifier open_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/icons/modmenu.png");
   private static final Identifier pop_Identifier = Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/icons/discord.png");
   private static final int read_I = 32;
   private static final int write_I = 32;
   private static final Identifier run_Identifier = Identifier.fromNamespaceAndPath("minecraft", "icon/language");
   private static final Identifier get_Identifier = Identifier.fromNamespaceAndPath("minecraft", "icon/accessibility");
   private static final int update_I = 15;
   private static final int sync_I = 32;
   private static final Component init_Component = Component.translatable("narrator.screen.title");
   private static final int open_I = 5;
   private static final int poll_I = 11;
   private static final int push_I = 15;
   private static final int pop_I = 12;
   private static final int tick_I = 16;
   private final autismclient.gui.f.b.CharEncoder init_CharEncoder = new autismclient.gui.f.b.CharEncoder();
   private final List<AutismTitleScreen.Inner_StreamUtil> init_List = new ArrayList<>();
   private final String init_Str = load_Str();
   private final boolean init_Z = FabricLoader.getInstance().isModLoaded("modmenu");
   private int run_I = -1;
   private long init_J;
   private boolean sync_Z = true;
   private int get_I = -1;
   private int set = -1;
   private final long load_J = System.nanoTime();
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
   private final List<AutismTitleScreen.Inner_ConfigStore> read_List = new ArrayList<>();
   private final List<AutismTitleScreen.Inner_DataUtil> write_List = new ArrayList<>();
   private int index;
   private int count;
   private static final Component load_Component = Component.translatable("title.credits");
   private static final Random init_Random = new Random();
   private static List<String> update_List;
   private final LogoRenderer init_LogoRenderer = new LogoRenderer(false);
   private SplashRenderer init_SplashRenderer;
   private boolean close_Z;
   private static final int flags = 16;

   public AutismTitleScreen() {
      super(init_Component);
   }

   @Override
   protected void init() {
      if (!CustomMainMenuGuard.init() && this.minecraft != null) {
         this.minecraft.gui.setScreen(new TitleScreen());
      } else {
         this.sync_Z = true;
         this.run_I = -1;
      }
   }

   private void load_V() {
      int spacing = 24;
      int topPos = this.height / 4 + 48;
      this.addRenderableWidget(
         Button.builder(Component.translatable("menu.singleplayer"), b -> this.minecraft.gui.setScreen(new SelectWorldScreen(this)))
            .bounds(this.width / 2 - 100, topPos, 200, 20)
            .build()
      );
      Component disabledReason = this.init_Component();
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
      int copyrightWidth = this.font.width(load_Component);
      this.addRenderableWidget(
         new PlainTextButton(
            this.width - copyrightWidth - 2,
            this.height - 10,
            copyrightWidth,
            10,
            load_Component,
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
      this.read_V();
      cA.init_V_GuiGraphicsExtract(graphics);

      try {
         if (this.flush_Z) {
            this.load(graphics);
            this.read(graphics);
            this.write(graphics);
         }

         this.init_V_GuiGraphicsExtract(graphics);
         Component hoveredTooltip = null;

         for (AutismTitleScreen.Inner_StreamUtil button : this.init_List) {
            button.init_V_GuiGraphicsExtract(graphics, uiMouseX, uiMouseY, delta);
            if (button.init_Z_F_F(uiMouseX, uiMouseY)) {
               graphics.requestCursor(button.init_Z ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
               if (button.load_Component != null) {
                  hoveredTooltip = button.load_Component;
               }
            }
         }

         if (hoveredTooltip != null) {
            this.init_V_GuiGraphicsExtract_2(graphics, hoveredTooltip, uiMouseX, uiMouseY);
         }

         this.update(graphics);
      } finally {
         cA.load_V_GuiGraphicsExtract(graphics);
         bK.init("title.render", perf);
      }

      this.init_V_GuiGraphicsExtract(graphics, mouseX, mouseY);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      if (ScreenMacroStopWidget.init_Z_Minecraft(this.minecraft) && (AutismLiteVariant.enabled() || autismclient.e.HashUtil.init())) {
         List<autismclient.gui.f.NetHelper> avoid = new ArrayList<>();
         avoid.add(autismclient.gui.f.NetHelper.init(this.put, this.add, this.clear, this.reset));
         if (this.flush_Z) {
            avoid.add(autismclient.gui.f.NetHelper.init(this.valid, this.size, this.data, this.value));
            avoid.add(autismclient.gui.f.NetHelper.init(this.state, this.queue, this.buffer, this.entry));
         }

         for (AutismTitleScreen.Inner_StreamUtil button : this.init_List) {
            avoid.add(autismclient.gui.f.NetHelper.init(button.init_I, button.load_I, button.read_I, button.write_I));
         }

         graphics.nextStratum();
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            ScreenMacroStopWidget.init_V_GuiGraphicsExtract(graphics, this, mouseX, mouseY, avoid.toArray(new autismclient.gui.f.NetHelper[0]));
         } catch (Throwable var10) {
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }
      }
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      super.extractRenderState(graphics, mouseX, mouseY, delta);
      this.init_LogoRenderer.extractRenderState(graphics, this.width, 1.0F);
      if (!this.close_Z) {
         this.init_SplashRenderer = this.init_SplashRenderer();
         this.close_Z = true;
      }

      if (this.init_SplashRenderer != null && !this.minecraft.options.hideSplashTexts().get()) {
         this.init_SplashRenderer.extractRenderState(graphics, this.width, this.font, 1.0F);
      }

      String version = "Minecraft " + SharedConstants.getCurrentVersion().name();
      graphics.text(this.font, version, 2, this.height - 10, UiColorPalette.accent());
   }

   private SplashRenderer init_SplashRenderer() {
      List<String> pool = this.init_List();
      if (pool.isEmpty()) {
         return null;
      }

      String text = pool.get(init_Random.nextInt(pool.size()));
      return new SplashRenderer(Component.literal(text).setStyle(Style.EMPTY.withColor(-256)));
   }

   private List<String> init_List() {
      if (update_List != null) {
         return update_List;
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

      update_List = List.copyOf(pool);
      return update_List;
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (Compat263.mouseEventGlfwButton(event) != 0) {
         return false;
      }

      float uiMouseX = (float)cA.init_D_D(event.x());
      float uiMouseY = (float)cA.init_D_D(event.y());
      this.read_V();

      for (AutismTitleScreen.Inner_StreamUtil button : this.init_List) {
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
   public Map<String, autismclient.gui.f.NetHelper> init_Map() {
      this.read_V();
      double sx = cA.init_I() == 0 ? 1.0 : (double)this.width / cA.init_I();
      double sy = cA.load_I() == 0 ? 1.0 : (double)this.height / cA.load_I();
      Map<String, autismclient.gui.f.NetHelper> out = new LinkedHashMap<>();

      for (AutismTitleScreen.Inner_StreamUtil button : this.init_List) {
         if (button.init_Z) {
            String key = button.init_Component.getString();
            if (key.isBlank()) {
               key = "button-" + out.size();
            }

            out.put(
               key,
               autismclient.gui.f.NetHelper.init(
                  (int)Math.round(button.init_I * sx),
                  (int)Math.round(button.load_I * sy),
                  (int)Math.round(button.read_I * sx),
                  (int)Math.round(button.write_I * sy)
               )
            );
         }
      }

      return out;
   }

   private void read_V() {
      int screenW = cA.init_I();
      int screenH = cA.load_I();
      if (this.sync_Z || screenW != this.get_I || screenH != this.set) {
         this.sync_Z = false;
         this.get_I = screenW;
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
         Component disabledReason = this.init_Component();
         boolean multiplayerAllowed = disabledReason == null;
         this.init_List.clear();
         AutismTitleScreen.Inner_StreamUtil singleplayer = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop, rowW, rowH, Component.translatable("menu.singleplayer"), true, () -> this.minecraft.gui.setScreen(new SelectWorldScreen(this))
            )
            .init(1);
         singleplayer.init(read_Identifier, 248, 24, 124, 12);
         this.init_List.add(singleplayer);
         AutismTitleScreen.Inner_StreamUtil multiplayer = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop + rowH + rowGap, rowW, rowH, Component.translatable("menu.multiplayer"), multiplayerAllowed, () -> {
                  Screen next = this.minecraft.options.skipMultiplayerWarning ? new JoinMultiplayerScreen(this) : new SafetyScreen(this);
                  this.minecraft.gui.setScreen(next);
               }
            )
            .init(disabledReason)
            .init(this.read_Str())
            .init(2);
         multiplayer.init(write_Identifier, 222, 24, 111, 12);
         this.init_List.add(multiplayer);
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
         realms.init(update_Identifier, 136, 24, 68, 12);
         this.init_List.add(realms);
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
         options.init(sync_Identifier, 138, 24, 69, 12);
         this.init_List.add(options);
         AutismTitleScreen.Inner_StreamUtil quit = new AutismTitleScreen.Inner_StreamUtil(
               rowX, rowTop + (rowH + rowGap) * 4, rowW, rowH, Component.translatable("menu.quit"), true, () -> this.minecraft.stop()
            )
            .init(5);
         quit.init(flush_Identifier, 74, 24, 37, 12);
         this.init_List.add(quit);
         this.init_V_I(screenW, screenH, margin, utilitySize);
         if (!this.flush_Z) {
            this.read_List.clear();
            this.write_List.clear();
         } else {
            int maxModulesPanelH = screenH - bottomBarH - this.add;
            int fixedModulesH = 44;
            int maxCategoryRows = Math.max(1, (maxModulesPanelH - fixedModulesH - 3) / 12);
            this.init_V_I(maxCategoryRows);
            this.write();
            this.cache = 0;
            int statusValueW = 0;

            for (AutismTitleScreen.Inner_ConfigStore row : this.read_List) {
               this.cache = Math.max(this.cache, k.init(this.font, row.init_Str(), autismclient.gui.f.a.DataUtil.load, 0));
               statusValueW = Math.max(statusValueW, k.init(this.font, row.init_Supplier().get(), autismclient.gui.f.a.DataUtil.init, 0));
            }

            this.data = this.cache + statusValueW + 24;
            this.value = 5 + this.read_List.size() * 11 + 5 - 1;
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

            for (AutismTitleScreen.Inner_DataUtil row : this.write_List) {
               if (row.load() < 0) {
                  modulesContentW = Math.max(modulesContentW, 12 + k.init(this.font, row.init_Str(), autismclient.gui.f.a.DataUtil.init, 0));
               } else {
                  String count = row.init_I() + "/" + row.load();
                  modulesContentW = Math.max(
                     modulesContentW,
                     6
                        + k.init(this.font, row.init_Str(), autismclient.gui.f.a.DataUtil.init, 0)
                        + 8
                        + k.init(this.font, count, autismclient.gui.f.a.DataUtil.init, 0)
                        + 6
                  );
               }
            }

            modulesContentW = Math.max(modulesContentW, 10 + k.init(this.font, "OPEN MANAGER", autismclient.gui.f.a.DataUtil.load, 0) + 20);
            this.buffer = Math.min(sideW, modulesContentW);
            this.entry = fixedModulesH + 3 + this.write_List.size() * 12;
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
               .init_UtilCore_StreamUti_I_Z(260, true);
            openManager.write_Z = false;
            this.init_List.add(openManager);
         }
      }
   }

   private void init(int x, int y, int w, int h, String text, Identifier icon, Runnable action, String url) {
      this.init_List
         .add(
            new AutismTitleScreen.Inner_StreamUtil(x, y, w, h, Component.literal(text), true, action)
               .init_UtilCore_StreamUti_Identifier(icon, 32, 32, 14)
               .init()
               .init(Component.literal(url))
         );
   }

   private void init_V_I(int screenW, int screenH, int margin, int utilitySize) {
      List<AutismTitleScreen.Inner_TaskQueue> all = new ArrayList<>();
      if (this.init_Z) {
         all.add(new AutismTitleScreen.Inner_TaskQueue("MODS", "", open_Identifier, null, 32, () -> this.update(), Component.literal("Mod Menu")));
      }

      all.add(
         new AutismTitleScreen.Inner_TaskQueue(
            "LANGUAGE",
            "",
            null,
            run_Identifier,
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
            get_Identifier,
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
         this.init_V_UtilCore_TaskQueue(all.get(i), x + i * (utilitySize + gap), y, utilitySize);
      }
   }

   private void init_V_UtilCore_TaskQueue(AutismTitleScreen.Inner_TaskQueue spec, int x, int y, int size) {
      AutismTitleScreen.Inner_StreamUtil button = new AutismTitleScreen.Inner_StreamUtil(
            x, y, size, size, Component.literal(spec.init_Str()), spec.init_Runnable() != null, spec.init_Runnable()
         )
         .load(spec.load_Str())
         .init(spec.init_Component());
      if (spec.load_Identifier() != null) {
         button.load(spec.load_Identifier());
      } else if (spec.init_Identifier() != null) {
         button.init_UtilCore_StreamUti_Identifier_I(spec.init_Identifier(), spec.init_I());
      }

      this.init_List.add(button);
   }

   private Component init_Component() {
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
      this.read_List.clear();
      this.read_List.add(new AutismTitleScreen.Inner_ConfigStore("USER:", () -> this.minecraft.getUser().getName()));
      this.read_List.add(new AutismTitleScreen.Inner_ConfigStore("BUILD:", AutismTitleScreen::init_Str));
   }

   private void init_V_I(int maxCategoryRows) {
      this.write_List.clear();
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
         this.write_List.addAll(all);
      } else {
         int keep = Math.max(0, maxCategoryRows - 1);

         for (int i = 0; i < keep; i++) {
            this.write_List.add(all.get(i));
         }

         this.write_List.add(new AutismTitleScreen.Inner_DataUtil("+" + (all.size() - keep) + " MORE", -1, -1));
      }
   }

   private static String init_Str() {
      String version = L.read_Str();
      return version != null && !version.isBlank() ? version : "unknown";
   }

   private float init_F_I(int delayMs) {
      float elapsedMs = (float)(System.nanoTime() - this.load_J) / 1000000.0F - delayMs;
      if (elapsedMs <= 0.0F) {
         return 0.0F;
      }

      float p = Math.min(1.0F, elapsedMs / 340.0F);
      return 1.0F - (1.0F - p) * (1.0F - p) * (1.0F - p);
   }

   private static int init_I_F(float progress) {
      return Math.round((1.0F - progress) * 5.0F);
   }

   private static int init(int color, float progress) {
      return autismclient.gui.f.PacketUtil.init(color, progress);
   }

   private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics) {
      float p = this.init_F_I(0);
      if (!(p <= 0.0F)) {
         float scale = Math.min(1.0F, (this.clear - 16) / 516.0F);
         int drawW = Math.max(1, Math.round(516.0F * scale));
         int drawH = Math.max(1, Math.round(144.0F * scale));
         int logoX = UiLayoutMath.centerOffset(this.put, this.clear, drawW);
         int logoY = this.add + init_I_F(p) + Math.max(0, (this.reset - drawH) / 2);
         graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            ThemeTextureRecolorer.init(init_Identifier, ThemePaletteManager.ColorRole.ACCENT),
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
      float p = this.init_F_I(140);
      if (!(p <= 0.0F)) {
         this.init(graphics, this.start, this.stop + init_I_F(p), this.next, this.prev, null, p);
      }
   }

   private void read(GuiGraphicsExtractor graphics) {
      float p = this.init_F_I(200);
      if (!(p <= 0.0F)) {
         int x = this.valid;
         int y = this.size + init_I_F(p);
         this.init(graphics, x, y, this.data, this.value, null, p);
         int rowY = y + 5;
         int labelColor = init(this.init_CharEncoder.load(l.update), p * 0.85F);
         int valueColor = init(this.init_CharEncoder.load(l.read), p);

         for (AutismTitleScreen.Inner_ConfigStore row : this.read_List) {
            k.init(graphics, this.font, row.init_Str(), autismclient.gui.f.a.DataUtil.load, labelColor, x + 6, rowY, false);
            int valueX = x + 12 + this.cache;
            String value = this.init_Str_Str(row.init_Supplier().get(), x + this.data - 6 - valueX, autismclient.gui.f.a.DataUtil.init, valueColor);
            k.init(graphics, this.font, value, autismclient.gui.f.a.DataUtil.init, valueColor, valueX, rowY + 1, false);
            rowY += 11;
         }
      }
   }

   private void write(GuiGraphicsExtractor graphics) {
      float p = this.init_F_I(260);
      if (!(p <= 0.0F)) {
         int x = this.state;
         int y = this.queue + init_I_F(p);
         this.init(graphics, x, y, this.buffer, this.entry, null, p);
         int accent = init(this.init_CharEncoder.load(l.update), p);
         int muted = init(this.init_CharEncoder.load(l.write), p);
         int body = init(this.init_CharEncoder.load(l.read), p);
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
         autismclient.gui.f.PacketUtil.init_V_GuiGraphicsExtract_2(graphics, x + 5, bigY + 15, this.buffer - 10, init(this.init_CharEncoder.set(), p));
         int rowY = bigY + 15 + 3;

         for (AutismTitleScreen.Inner_DataUtil row : this.write_List) {
            if (row.load() < 0) {
               k.init(graphics, this.font, row.init_Str(), autismclient.gui.f.a.DataUtil.init, muted, x + 6, rowY + 2, false);
            } else {
               String count = row.init_I() + "/" + row.load();
               int countColor = row.init_I() > 0 ? accent : muted;
               int countW = k.init(this.font, count, autismclient.gui.f.a.DataUtil.init, countColor);
               String name = this.init_Str_Str(row.init_Str(), this.buffer - countW - 20, autismclient.gui.f.a.DataUtil.init, body);
               k.init(graphics, this.font, name, autismclient.gui.f.a.DataUtil.init, body, x + 6, rowY + 2, false);
               k.init(graphics, this.font, count, autismclient.gui.f.a.DataUtil.init, countColor, x + this.buffer - 6 - countW, rowY + 2, false);
            }

            rowY += 12;
         }
      }
   }

   private void init(GuiGraphicsExtractor graphics, int x, int y, int w, int h, String header, float p) {
      autismclient.gui.f.PacketUtil.init(
         graphics, autismclient.gui.f.NetHelper.init(x, y, w, h), init(this.init_CharEncoder.poll(), p), init(this.init_CharEncoder.set(), p)
      );
      int accent = init(this.init_CharEncoder.load(l.update), p);
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
      float p = this.init_F_I(320);
      if (!(p <= 0.0F)) {
         Identifier fontId = autismclient.gui.f.a.DataUtil.init;
         int muted = init(this.init_CharEncoder.load(l.write), p);
         int y = cA.load_I() - k.init(fontId) - 3;
         k.init(graphics, this.font, this.init_Str, fontId, muted, 4, y, false);
      }
   }

   private String init_Str_Str(String text, int maxWidth, Identifier fontId, int color) {
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

   private static Identifier init_Identifier_Str(String name) {
      return Identifier.fromNamespaceAndPath("autismclient", "textures/gui/title/button_text/" + name + ".png");
   }

   private static String load_Str() {
      int modCount = FabricLoader.getInstance().getAllMods().size();
      return modCount + (modCount == 1 ? " Mod" : " Mods");
   }

   private void init_V_GuiGraphicsExtract_2(GuiGraphicsExtractor graphics, Component tooltip, float uiMouseX, float uiMouseY) {
      TooltipRenderer.init(
         autismclient.gui.f.UiHelper.init(graphics, this.font, Math.round(uiMouseX), Math.round(uiMouseY)),
         tooltip.getString(),
         Math.round(uiMouseX),
         Math.round(uiMouseY),
         220
      );
   }

   private String read_Str() {
      int count = this.init_I();
      return count < 0 ? null : Integer.toString(count);
   }

   private int init_I() {
      long now = System.currentTimeMillis();
      if (this.run_I >= 0 && now - this.init_J < 2000L) {
         return this.run_I;
      }

      this.init_J = now;

      try {
         ServerList serverList = new ServerList(this.minecraft);
         serverList.load();
         this.run_I = serverList.size();
      } catch (RuntimeException ignored) {
         this.run_I = -1;
      }

      return this.run_I;
   }

   private record Inner_ConfigStore(String init_Str, Supplier<String> init_Supplier) {
   }

   private record Inner_DataUtil(String init_Str, int init_I, int load) {
   }

   private final class Inner_StreamUtil {
      private final int init_I;
      private final int load_I;
      private final int read_I;
      private final int write_I;
      private final Component init_Component;
      private final boolean init_Z;
      private final Runnable init_Runnable;
      private Identifier init_Identifier;
      private int update_I = 32;
      private Identifier load_Identifier;
      private Identifier read_Identifier;
      private int sync = 32;
      private int flush = 32;
      private int close = 14;
      private Identifier write_Identifier;
      private int open;
      private int poll;
      private int push;
      private int pop;
      private Component load_Component;
      private String init_Str;
      private int tick = -1;
      private boolean load_Z;
      private String load_Str = "";
      private boolean read_Z;
      private boolean write_Z = true;
      private int run = 320;
      private boolean update_Z;

      private Inner_StreamUtil(int x, int y, int width, int height, Component label, boolean enabled, Runnable onPress) {
         this.init_I = x;
         this.load_I = y;
         this.read_I = width;
         this.write_I = height;
         this.init_Component = label;
         this.init_Z = enabled;
         this.init_Runnable = onPress;
      }

      private AutismTitleScreen.Inner_StreamUtil init(Component tooltip) {
         this.load_Component = tooltip;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(String rightBadge) {
         this.init_Str = rightBadge;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil init(Identifier icon) {
         return this.init_UtilCore_StreamUti_Identifier_I(icon, 32);
      }

      private AutismTitleScreen.Inner_StreamUtil init_UtilCore_StreamUti_Identifier_I(Identifier icon, int iconTextureSize) {
         this.init_Identifier = icon;
         this.update_I = Math.max(1, iconTextureSize);
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil load(Identifier sprite) {
         this.load_Identifier = sprite;
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
         this.write_Identifier = labelTexture;
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

      private AutismTitleScreen.Inner_StreamUtil init_UtilCore_StreamUti_I_Z(int delayMs, boolean slide) {
         this.run = delayMs;
         this.update_Z = slide;
         return this;
      }

      private AutismTitleScreen.Inner_StreamUtil load(String subtitle) {
         this.load_Z = true;
         this.load_Str = subtitle == null ? "" : subtitle;
         return this;
      }

      private boolean init_Z_F_F(float mouseX, float mouseY) {
         return mouseX >= this.init_I && mouseY >= this.load_I && mouseX < this.init_I + this.read_I && mouseY < this.load_I + this.write_I;
      }

      private boolean load(float mouseX, float mouseY) {
         if (!this.init_Z_F_F(mouseX, mouseY)) {
            return false;
         }

         if (AutismTitleScreen.this.init_F_I(this.run) < 1.0F) {
            return false;
         }

         if (!this.init_Z) {
            return true;
         }

         AutismTitleScreen.this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(AutismTitleScreen.init_SoundEvent, 1.0F, 0.7F));
         if (this.init_Runnable != null) {
            this.init_Runnable.run();
         }

         return true;
      }

      private void init_V_GuiGraphicsExtract(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float delta) {
         float p = AutismTitleScreen.this.init_F_I(this.run);
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
         boolean hovered = this.init_Z && this.init_Z_F_F(mouseX, mouseY);
         int yDraw = this.load_I + (this.update_Z ? AutismTitleScreen.init_I_F(p) : 0);
         int border = ThemePaletteManager.recolor(this.init_Z ? -1721357268 : 1430596138, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.init_I, yDraw, this.read_I, this.write_I),
            AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -1207302650 : -1873801200, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.init_I, this.load_I, this.read_I, this.write_I)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.init_I + 1, yDraw + 1, this.read_I - 2, this.write_I - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
            int accent = AutismTitleScreen.this.init_CharEncoder.load(l.update);
            autismclient.gui.f.PacketUtil.load(
               graphics, autismclient.gui.f.NetHelper.init(this.init_I, yDraw, this.read_I, this.write_I), AutismTitleScreen.init(accent, hoverT * 0.8F * p)
            );
            autismclient.gui.f.PacketUtil.init(
               graphics, autismclient.gui.f.NetHelper.init(this.init_I, yDraw, 2, this.write_I), AutismTitleScreen.init(accent, hoverT * p)
            );
            if (this.write_Z) {
               int slide = Math.round(hoverT * 3.0F);
               autismclient.gui.f.PacketUtil.init(
                  graphics,
                  autismclient.gui.f.NetHelper.init(this.init_I + 4 + slide, yDraw + (this.write_I - 8) / 2, 7, 8),
                  false,
                  AutismTitleScreen.init(accent, hoverT * p)
               );
            }
         }

         String marker = this.init_Str != null && !this.init_Str.isBlank() ? this.init_Str : null;
         if (this.write_Identifier != null && this.open > 0 && this.poll > 0 && this.push > 0 && this.pop > 0) {
            int maxW = Math.max(1, this.read_I - 8);
            int maxH = Math.max(1, this.write_I <= 18 ? this.write_I - 4 : this.write_I - 8);
            float scale = Math.min(1.0F, Math.min((float)maxW / this.push, (float)maxH / this.pop));
            int drawW = Math.max(1, Math.round(this.push * scale));
            int drawH = Math.max(1, Math.round(this.pop * scale));
            int textX = this.init_I + (this.read_I - drawW) / 2;
            int textY = yDraw + (this.write_I - drawH) / 2;
            int textColor = AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               this.write_Identifier,
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
         } else if (this.init_Component != null) {
            String text = this.init_Component.getString();
            int textColor = AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            Identifier fontId = autismclient.gui.f.a.DataUtil.load;
            int textY = UiLayoutMath.centerTextY(yDraw, this.write_I, k.init(fontId), 1);
            String drawText = text;
            int textX;
            if (this.read_Identifier != null) {
               int iconSize = Math.min(this.close, Math.max(1, Math.min(this.write_I - 2, this.read_I - 12)));
               int iconX = this.init_I + 3;
               int iconY = yDraw + (this.write_I - iconSize) / 2;
               graphics.blit(
                  RenderPipelines.GUI_TEXTURED,
                  ThemeTextureRecolorer.init_Identifier(this.read_Identifier),
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
                  AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p)
               );
               textX = iconX + iconSize + 4;
               int maxTextW = Math.max(1, this.init_I + this.read_I - 4 - textX);
               drawText = k.init(AutismTitleScreen.this.font, text, maxTextW, fontId, textColor);
            } else {
               int textW = k.init(AutismTitleScreen.this.font, text, fontId, textColor);
               textX = this.init_I + (this.read_I - textW) / 2;
            }

            k.init(graphics, AutismTitleScreen.this.font, drawText, fontId, textColor, textX, textY, false);
         }

         if (marker != null && !marker.isBlank()) {
            Identifier fontId = autismclient.gui.f.a.DataUtil.load;
            int markerColor = AutismTitleScreen.init(this.init_Z ? ThemePaletteManager.recolor(-11052, ThemePaletteManager.ColorRole.ACCENT) : -8362651, p);
            int markerW = k.init(AutismTitleScreen.this.font, marker, fontId, markerColor);
            int markerX = this.init_I + this.read_I - markerW - 6;
            int markerY = UiLayoutMath.centerTextY(yDraw, this.write_I, k.init(fontId), 1);
            AutismTitleScreen.this.init(graphics, marker, markerX, markerY, markerColor, fontId);
         }
      }

      private void read(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float p) {
         boolean hovered = this.init_Z && this.init_Z_F_F(mouseX, mouseY);
         int yDraw = this.load_I + (this.update_Z ? AutismTitleScreen.init_I_F(p) : 0);
         int border = ThemePaletteManager.recolor(-1721357268, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.init_I, yDraw, this.read_I, this.write_I),
            AutismTitleScreen.init(ThemePaletteManager.recolor(-1878391290, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.init_I, this.load_I, this.read_I, this.write_I)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.init_I + 1, yDraw + 1, this.read_I - 2, this.write_I - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
            autismclient.gui.f.PacketUtil.load(
               graphics,
               autismclient.gui.f.NetHelper.init(this.init_I, yDraw, this.read_I, this.write_I),
               AutismTitleScreen.init(AutismTitleScreen.this.init_CharEncoder.load(l.update), hoverT * 0.8F * p)
            );
         }

         if (this.read_Identifier != null) {
            int iconSize = Math.min(this.close, Math.max(1, this.write_I - 6));
            int iconX = this.init_I + 5;
            int iconY = yDraw + (this.write_I - iconSize) / 2;
            int tint = AutismTitleScreen.init(ThemePaletteManager.recolor(-528658, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               ThemeTextureRecolorer.init_Identifier(this.read_Identifier),
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
                  ThemeTextureRecolorer.init_Identifier(this.read_Identifier),
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
                  AutismTitleScreen.init(AutismTitleScreen.this.init_CharEncoder.load(l.update), hoverT * p)
               );
            }
         }

         int textX = this.init_I + 5 + Math.min(this.close, Math.max(1, this.write_I - 6)) + 5;
         int maxTextW = Math.max(1, this.init_I + this.read_I - 4 - textX);
         int lineColor = AutismTitleScreen.init(AutismTitleScreen.this.init_CharEncoder.load(l.read), p);
         String line = k.init(AutismTitleScreen.this.font, this.init_Component.getString(), maxTextW, autismclient.gui.f.a.DataUtil.load, lineColor);
         int lineY = UiLayoutMath.centerTextY(yDraw, this.write_I, k.init(autismclient.gui.f.a.DataUtil.load), 1);
         k.init(graphics, AutismTitleScreen.this.font, line, autismclient.gui.f.a.DataUtil.load, lineColor, textX, lineY, false);
      }

      private void write(GuiGraphicsExtractor graphics, float mouseX, float mouseY, float p) {
         boolean hovered = this.init_Z && this.init_Z_F_F(mouseX, mouseY);
         int border = ThemePaletteManager.recolor(this.init_Z ? -1721357268 : 1430596138, ThemePaletteManager.ColorRole.OUTLINE);
         autismclient.gui.f.PacketUtil.init(
            graphics,
            autismclient.gui.f.NetHelper.init(this.init_I, this.load_I, this.read_I, this.write_I),
            AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -1727527675 : 1716523024, ThemePaletteManager.ColorRole.BUTTON), p),
            AutismTitleScreen.init(border, p)
         );
         float hoverT = autismclient.gui.ui.HoverTransitionCache.sample(
            autismclient.gui.ui.HoverTransitionCache.boundsKey(autismclient.gui.f.NetHelper.init(this.init_I, this.load_I, this.read_I, this.write_I)), hovered
         );
         if (hoverT > 0.001F) {
            autismclient.gui.f.PacketUtil.init(
               graphics,
               autismclient.gui.f.NetHelper.init(this.init_I + 1, this.load_I + 1, this.read_I - 2, this.write_I - 2),
               Math.round(20.0F * hoverT * p) << 24 | 16777215
            );
         }

         if (this.load_Identifier != null) {
            int iconSize = Math.min(15, Math.max(12, this.write_I - 6));
            int iconX = this.init_I + (this.read_I - iconSize) / 2;
            int iconY = this.load_I + (this.write_I - iconSize) / 2;
            float alpha = (this.init_Z ? 1.0F : 0.45F) * p;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.load_Identifier, iconX, iconY, iconSize, iconSize, alpha);
         } else if (this.init_Identifier != null) {
            int iconSize = Math.min(16, Math.max(12, this.write_I - 7));
            int iconX = this.init_I + (this.read_I - iconSize) / 2;
            int iconY = this.load_I + (this.write_I - iconSize) / 2;
            int iconTint = AutismTitleScreen.init(ThemePaletteManager.recolor(this.init_Z ? -528658 : -8362651, ThemePaletteManager.ColorRole.TEXT), p);
            graphics.blit(
               RenderPipelines.GUI_TEXTURED,
               ThemeTextureRecolorer.init_Identifier(this.init_Identifier),
               iconX,
               iconY,
               0.0F,
               0.0F,
               iconSize,
               iconSize,
               this.update_I,
               this.update_I,
               this.update_I,
               this.update_I,
               iconTint
            );
         }
      }
   }

   private record Inner_TaskQueue(
      String init_Str, String load_Str, Identifier init_Identifier, Identifier load_Identifier, int init_I, Runnable init_Runnable, Component init_Component
   ) {
   }
}
