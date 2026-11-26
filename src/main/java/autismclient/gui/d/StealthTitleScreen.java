package autismclient.gui.d;

import autismclient.compat263.Compat263;
import com.mojang.authlib.minecraft.BanDetails;
import com.mojang.realmsclient.RealmsMainScreen;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.FriendsButton;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.PlainTextButton;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.CreditsAndAttributionScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.SafetyScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.ARGB;

public final class StealthTitleScreen extends Screen {
   private static final Component init_Component = Component.translatable("narrator.screen.title");
   private static final Component load = Component.translatable("title.credits");
   private static final Random init_Random = new Random();
   private static List<String> init_List;
   private final LogoRenderer init_LogoRenderer = new LogoRenderer(false);
   private SplashRenderer init_SplashRenderer;
   private boolean init_Z;
   private FriendsButton init_FriendsButton;

   public StealthTitleScreen() {
      super(init_Component);
   }

   @Override
   protected void init() {
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
      int iconRowY = realmsY + spacing;
      boolean friendsAvailable = !this.minecraft.isDemo();
      this.init_FriendsButton = this.addRenderableWidget(
         CommonButtons.friends(
            20,
            b -> OnlineOptionsScreen.confirmFriendsListEnabled(this.minecraft, () -> this.minecraft.gui.setScreen(new FriendsOverlayScreen(this)), this),
            friendsAvailable
         )
      );
      this.init_FriendsButton.setPosition(this.init(1, 3, 20), iconRowY);
      SpriteIconButton language = this.addRenderableWidget(
         CommonButtons.language(
            20, b -> this.minecraft.gui.setScreen(new LanguageSelectScreen(this, this.minecraft.options, this.minecraft.getLanguageManager())), true
         )
      );
      language.setPosition(this.init(2, 3, 20), iconRowY);
      SpriteIconButton accessibility = this.addRenderableWidget(
         CommonButtons.accessibility(20, b -> this.minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, this.minecraft.options)), true)
      );
      accessibility.setPosition(this.init(3, 3, 20), iconRowY);
      int optionsRowY = iconRowY + spacing;
      this.addRenderableWidget(
         Button.builder(
               Component.translatable("menu.options"), b -> this.minecraft.gui.setScreen(Compat263.newOptionsScreen(this, this.minecraft.options, false))
            )
            .bounds(this.width / 2 - 100, optionsRowY, 98, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.translatable("menu.quit"), b -> this.minecraft.stop()).bounds(this.width / 2 + 2, optionsRowY, 98, 20).build()
      );
      int copyrightWidth = this.font.width(load);
      this.addRenderableWidget(
         new PlainTextButton(
            this.width - copyrightWidth - 2,
            this.height - 10,
            copyrightWidth,
            10,
            load,
            b -> this.minecraft.gui.setScreen(new CreditsAndAttributionScreen(this)),
            this.font
         )
      );
   }

   private int init(int currentButton, int numberOfButtons, int buttonWidth) {
      int totalWidth = numberOfButtons * buttonWidth + (numberOfButtons - 1) * 4;
      return this.width / 2 - totalWidth / 2 + (currentButton - 1) * (buttonWidth + 4);
   }

   @Override
   public void tick() {
      if (this.init_FriendsButton != null) {
         this.init_FriendsButton.refreshIncomingRequestCount();
      }
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
      this.minecraft.gameRenderer.panorama().extractRenderState(graphics, this.width, this.height);
      super.extractRenderState(graphics, mouseX, mouseY, delta);
      this.init_LogoRenderer.extractRenderState(graphics, this.width, 1.0F);
      if (!this.init_Z) {
         this.init_SplashRenderer = this.init_SplashRenderer();
         this.init_Z = true;
      }

      if (this.init_SplashRenderer != null && !this.minecraft.options.hideSplashTexts().get()) {
         this.init_SplashRenderer.extractRenderState(graphics, this.width, this.font, 1.0F);
      }

      String version = "Minecraft " + SharedConstants.getCurrentVersion().name();
      graphics.text(this.font, version, 2, this.height - 10, ARGB.white(1.0F));
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
      if (init_List != null) {
         return init_List;
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

      init_List = List.copyOf(pool);
      return init_List;
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
}
