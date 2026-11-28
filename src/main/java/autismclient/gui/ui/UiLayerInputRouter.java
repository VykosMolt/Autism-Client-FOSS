package autismclient.gui.ui;

public final class UiLayerInputRouter {
   private final UiLayerHost layers;

   public UiLayerInputRouter(UiLayerHost layers) {
      this.layers = layers;
   }

   public boolean mouseClicked(int mouseX, int mouseY, int button) {
      return this.layers.dispatchClick(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean mouseReleased(int mouseX, int mouseY, int button) {
      return this.layers.mouseReleased(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean mouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.layers.mouseDragged(mouseX, mouseY, button, deltaX, deltaY) == UiInputResult.HANDLED;
   }

   public boolean mouseScrolled(int mouseX, int mouseY, double amount) {
      return this.layers.dispatchScroll(mouseX, mouseY, amount) == UiInputResult.HANDLED;
   }

   public boolean keyPressed(int key, int scanCode, int modifiers) {
      return this.layers.keyPressed(key, scanCode, modifiers) == UiInputResult.HANDLED;
   }

   public boolean charTyped(char chr) {
      return this.layers.dispatchChar(chr) == UiInputResult.HANDLED;
   }
}
