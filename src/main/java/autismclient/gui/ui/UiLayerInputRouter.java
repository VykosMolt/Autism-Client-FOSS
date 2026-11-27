package autismclient.gui.ui;

public final class UiLayerInputRouter {
   private final UiLayerHost init;

   public UiLayerInputRouter(UiLayerHost layers) {
      this.init = layers;
   }

   public boolean mouseClicked(int mouseX, int mouseY, int button) {
      return this.init.dispatchClick(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean load(int mouseX, int mouseY, int button) {
      return this.init.load(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean init(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.init.init(mouseX, mouseY, button, deltaX, deltaY) == UiInputResult.HANDLED;
   }

   public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
      return this.init.dispatchScroll(mouseX, mouseY, amount) == UiInputResult.HANDLED;
   }

   public boolean read(int key, int scanCode, int modifiers) {
      return this.init.read(key, scanCode, modifiers) == UiInputResult.HANDLED;
   }

   public boolean init(char chr) {
      return this.init.dispatchChar(chr) == UiInputResult.HANDLED;
   }
}
