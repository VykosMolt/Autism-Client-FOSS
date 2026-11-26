package autismclient.gui.ui;

public final class UiLayerInputRouter {
   private final UiLayerHost init;

   public UiLayerInputRouter(UiLayerHost layers) {
      this.init = layers;
   }

   public boolean init_Z_I_2(int mouseX, int mouseY, int button) {
      return this.init.init_EventBus_I_2(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean load(int mouseX, int mouseY, int button) {
      return this.init.load(mouseX, mouseY, button) == UiInputResult.HANDLED;
   }

   public boolean init(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.init.init(mouseX, mouseY, button, deltaX, deltaY) == UiInputResult.HANDLED;
   }

   public boolean init_Z_I(int mouseX, int mouseY, double amount) {
      return this.init.init_EventBus_I(mouseX, mouseY, amount) == UiInputResult.HANDLED;
   }

   public boolean read(int key, int scanCode, int modifiers) {
      return this.init.read(key, scanCode, modifiers) == UiInputResult.HANDLED;
   }

   public boolean init(char chr) {
      return this.init.init_EventBus_C(chr) == UiInputResult.HANDLED;
   }
}
