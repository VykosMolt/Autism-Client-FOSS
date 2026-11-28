package autismclient.gui.screens;

import autismclient.AutismClientAddon;
import autismclient.compat263.Compat263;
import autismclient.util.AutismItemNbtInspectOverlay;
import autismclient.util.OverlaySuiteController;
import autismclient.util.OverlayManager;
import autismclient.util.AutismOverlay;
import autismclient.util.OverlayScreenController;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class AutismOverlaysScreen extends Screen {
   private static final int init_I = 120;
   private static final int load_I = 16;
   private final AutismOverlay tiedOverlay;
   private final Screen returnScreen;
   private final boolean showBackButton;
   private final boolean dismissOnClose;
   private boolean dimBackground;
   private OverlaySuiteController suiteController;
   private boolean firstInit = true;

   public AutismOverlaysScreen() {
      this(null, null, false);
   }

   public AutismOverlaysScreen(AutismOverlay tiedOverlay) {
      this(tiedOverlay, null, false);
   }

   public AutismOverlaysScreen(AutismOverlay tiedOverlay, Screen returnScreen) {
      this(tiedOverlay, returnScreen, false);
   }

   public AutismOverlaysScreen(AutismOverlay tiedOverlay, Screen returnScreen, boolean showBackButton) {
      this(tiedOverlay, returnScreen, showBackButton, false);
   }

   public AutismOverlaysScreen(AutismOverlay tiedOverlay, Screen returnScreen, boolean showBackButton, boolean dismissOnClose) {
      super(Component.literal("Autism Overlays"));
      this.tiedOverlay = tiedOverlay;
      this.returnScreen = returnScreen;
      this.showBackButton = showBackButton;
      this.dismissOnClose = dismissOnClose;
   }

   public AutismOverlaysScreen withDimmedBackground() {
      this.dimBackground = true;
      return this;
   }

   public boolean isTiedTo(AutismOverlay overlay) {
      return overlay != null && this.tiedOverlay == overlay;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      super.init();
      if (!OverlayScreenController.shouldInterceptBackgroundScreen() && this.suiteController == null && !this.showBackButton && this.minecraft != null && this.minecraft.level != null) {
         autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
         if (module != null && module.isActive()) {
            try {
               this.suiteController = OverlaySuiteController.create(this.font, this.tiedOverlay);
            } catch (Throwable error) {
               AutismClientAddon.LOG.warn("Host screen window set failed to build", error);
            }

            if (this.tiedOverlay != null) {
               OverlayManager.getInstance().register(this.tiedOverlay);
               OverlayManager.getInstance().bringToFront(this.tiedOverlay);
            }
         }
      }

      if (this.firstInit) {
         this.firstInit = false;
         if (this.tiedOverlay != null && this.tiedOverlay.isVisible()) {
            OverlayManager.getInstance().markClosing(this.tiedOverlay);
         }
      }
   }

   @Override
   public void removed() {
      if (this.suiteController != null && !OverlayScreenController.shouldInterceptBackgroundScreen()) {
         try {
            this.suiteController.saveStates();
         } catch (Throwable error) {
            AutismClientAddon.LOG.warn("Host screen window set failed to save", error);
         }

         this.suiteController = null;
      }

      super.removed();
   }

   @Override
   public void tick() {
      super.tick();
      if (this.tiedOverlay != null && !this.tiedOverlay.isVisible() && !this.tiedOverlay.isClosing() && this.minecraft != null && this.minecraft.gui.screen() == this) {
         OverlayScreenController.setScreenExplicit(this.minecraft, this.returnScreen);
      }
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
      if (this.dimBackground) {
         autismclient.gui.f.PacketUtil.init(graphics, autismclient.gui.f.NetHelper.init(0, 0, this.width, this.height), -1072689136);
      }

      OverlayManager.getInstance().render(graphics, mouseX, mouseY, delta);
      if (this.showBackButton && this.font != null) {
         autismclient.gui.f.NetHelper back = this.backButtonRect();
         boolean hovered = this.init((double)mouseX, (double)mouseY);
         autismclient.gui.f.b.CachePool.init(
            autismclient.gui.f.UiHelper.init(graphics, this.font, mouseX, mouseY),
            back,
            "Back",
            autismclient.gui.f.b.CachePool.Inner_DataUtil.init,
            hovered,
            false
         );
      }
   }

   private autismclient.gui.f.NetHelper backButtonRect() {
      int bw = Math.min(120, Math.max(80, this.width - 40));
      return autismclient.gui.f.NetHelper.init((this.width - bw) / 2, this.height - 16 - 6, bw, 16);
   }

   private boolean init(double x, double y) {
      autismclient.gui.f.NetHelper b = this.backButtonRect();
      return x >= b.read() && x < b.init() && y >= b.write() && y < b.load();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
      if (this.showBackButton && this.init(event.x(), event.y())) {
         this.closeAndReturn();
         return true;
      } else {
         return OverlayManager.getInstance().mouseClicked(event.x(), event.y(), Compat263.mouseEventGlfwButton(event));
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent event) {
      return OverlayManager.getInstance().mouseReleased(event.x(), event.y(), Compat263.mouseEventGlfwButton(event));
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      return OverlayManager.getInstance().mouseDragged(event.x(), event.y(), Compat263.mouseEventGlfwButton(event), dx, dy);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
      return OverlayManager.getInstance().mouseScrolled(mouseX, mouseY, scrollY);
   }

   @Override
   public boolean keyPressed(KeyEvent input) {
      boolean inventoryKey = this.minecraft != null
         && this.minecraft.options != null
         && this.minecraft.options.keyInventory != null
         && this.minecraft.options.keyInventory.matches(input);
      boolean closeKey = Compat263.keyEventGlfwKey(input) == 256 || inventoryKey;
      if (closeKey && this.minecraft != null && !OverlayManager.getInstance().hasFocusedTextInput()) {
         this.closeAndReturn();
         return true;
      } else if (OverlayManager.getInstance().keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input))) {
         return true;
      } else if (closeKey && this.minecraft != null) {
         this.closeAndReturn();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return OverlayManager.getInstance().charTyped((char)input.codepoint(), 0);
   }

   private void closeAndReturn() {
      if (this.tiedOverlay != null && (this.showBackButton || this.dismissOnClose)) {
         this.tiedOverlay.setVisible(false);
      }

      if (this.dismissOnClose) {
         if (this.tiedOverlay != null) {
            OverlayManager.getInstance().unregister(this.tiedOverlay);
         }

         AutismItemNbtInspectOverlay.hide();
      }

      if (this.minecraft != null) {
         OverlayScreenController.setScreenExplicit(this.minecraft, this.returnScreen);
      }
   }
}
