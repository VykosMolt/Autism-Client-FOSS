package autismclient.gui.d;

import autismclient.compat263.Compat263;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommonButtons;
import net.minecraft.client.gui.components.FriendsButton;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.friends.FriendsOverlayScreen;
import net.minecraft.client.gui.screens.options.OnlineOptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonLinks;

public class ListPool extends Screen {
   private static final Component init_Component = Component.translatable("menu.returnToGame");
   private static final Component load = Component.translatable("gui.advancements");
   private static final Component read = Component.translatable("gui.stats");
   private static final Component write = Component.translatable("menu.sendFeedback");
   private static final Component update = Component.translatable("menu.reportBugs");
   private static final Component sync = Component.translatable("menu.options");
   private static final Component flush = Component.translatable("menu.multiplayerOptions.button");
   private static final Component close = Component.translatable("menu.playerReporting");
   private static final Component open = Component.translatable("menu.game");
   private FriendsButton init_FriendsButton;

   public ListPool() {
      super(open);
   }

   @Override
   protected void init() {
      this.load();
      int textWidth = this.font.width(this.title);
      this.addRenderableWidget(new StringWidget(this.width / 2 - textWidth / 2, 40, textWidth, 9, this.title, this.font));
   }

   private void load() {
      GridLayout gridLayout = new GridLayout();
      gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
      RowHelper helper = gridLayout.createRowHelper(2);
      helper.addChild(Button.builder(init_Component, b -> {
         this.minecraft.gui.setScreen(null);
         this.minecraft.mouseHandler.grabMouse();
      }).width(204).build(), 2, gridLayout.newCellSettings().paddingTop(50));
      helper.addChild(this.init(load, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), this)));
      helper.addChild(this.init(read, () -> new StatsScreen(this, this.minecraft.player.getStats())));
      LinearLayout iconButtonRow = LinearLayout.horizontal().spacing(4);
      SpriteIconButton reportBugsButton = SpriteIconButton.builder(update, ConfirmLinkScreen.confirmLink(this, CommonLinks.SNAPSHOT_BUGS_FEEDBACK), true)
         .width(20)
         .sprite(Identifier.withDefaultNamespace("pause_menu/bug"), 15, 15)
         .withTootip()
         .build();
      reportBugsButton.active = !SharedConstants.getCurrentVersion().dataVersion().isSideSeries();
      iconButtonRow.addChild(reportBugsButton);
      SpriteIconButton feedbackButton = SpriteIconButton.builder(
            write,
            ConfirmLinkScreen.confirmLink(this, SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK),
            true
         )
         .width(20)
         .sprite(Identifier.withDefaultNamespace("pause_menu/social_interactions"), 15, 15)
         .withTootip()
         .build();
      iconButtonRow.addChild(feedbackButton);
      this.init_FriendsButton = CommonButtons.friends(
         20,
         b -> OnlineOptionsScreen.confirmFriendsListEnabled(this.minecraft, () -> this.minecraft.gui.setScreen(new FriendsOverlayScreen(this)), this),
         !this.minecraft.isDemo()
      );
      iconButtonRow.addChild(this.init_FriendsButton);
      SpriteIconButton playerReportingButton = SpriteIconButton.builder(close, b -> this.minecraft.gui.setScreen(new SocialInteractionsScreen(this)), true)
         .width(20)
         .sprite(Identifier.withDefaultNamespace("pause_menu/player_reporting"), 15, 15)
         .withTootip()
         .build();
      iconButtonRow.addChild(playerReportingButton);
      helper.addChild(iconButtonRow, 2, gridLayout.newCellSettings().alignHorizontallyCenter());
      if (this.minecraft.hasSingleplayerServer()) {
         helper.addChild(this.init(sync, () -> Compat263.newOptionsScreen(this, this.minecraft.options, true)));
         helper.addChild(this.init(flush, () -> Compat263.newOptionsScreen(this, this.minecraft.options, true)));
      } else {
         helper.addChild(
            Button.builder(sync, b -> this.minecraft.gui.setScreen(Compat263.newOptionsScreen(this, this.minecraft.options, true))).width(204).build(), 2
         );
      }

      helper.addChild(Button.builder(CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()), b -> {
         b.active = false;
         this.minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
      }).width(204).build(), 2);
      gridLayout.arrangeElements();
      FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5F, 0.25F);
      gridLayout.visitWidgets(x$0 -> this.addRenderableWidget(x$0));
   }

   private Button init(Component message, Supplier<Screen> newScreen) {
      return Button.builder(message, b -> this.minecraft.gui.setScreen(newScreen.get())).width(98).build();
   }

   @Override
   public void tick() {
      if (this.init_FriendsButton != null) {
         this.init_FriendsButton.refreshIncomingRequestCount();
      }
   }

   @Override
   public void onClose() {
      this.minecraft.gui.setScreen(null);
      this.minecraft.mouseHandler.grabMouse();
   }
}
