package autismclient.gui.f;

public final class FileCache {
   private final ByteReader init;

   public FileCache(ByteReader layers) {
      this.init = layers;
   }

   public boolean init_Z_I_2(int mouseX, int mouseY, int button) {
      return this.init.init_EventBus_I_2(mouseX, mouseY, button) == EventBus.HANDLED;
   }

   public boolean load(int mouseX, int mouseY, int button) {
      return this.init.load(mouseX, mouseY, button) == EventBus.HANDLED;
   }

   public boolean init(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return this.init.init(mouseX, mouseY, button, deltaX, deltaY) == EventBus.HANDLED;
   }

   public boolean init_Z_I(int mouseX, int mouseY, double amount) {
      return this.init.init_EventBus_I(mouseX, mouseY, amount) == EventBus.HANDLED;
   }

   public boolean read(int key, int scanCode, int modifiers) {
      return this.init.read(key, scanCode, modifiers) == EventBus.HANDLED;
   }

   public boolean init(char chr) {
      return this.init.init_EventBus_C(chr) == EventBus.HANDLED;
   }
}
