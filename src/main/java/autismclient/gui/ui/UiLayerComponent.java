package autismclient.gui.ui;
import autismclient.gui.f.NetHelper;
import autismclient.gui.f.SyncManager;

public interface UiLayerComponent {
   NetHelper getBounds();

   void setBounds(NetHelper var1);

   default NetHelper getInteractiveBounds() {
      return this.getBounds();
   }

   default void render(SyncManager context) {
   }

   default UiInputResult onMouseClicked(int mouseX, int mouseY, int button) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult onMouseReleased(int mouseX, int mouseY, int button) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult onMouseDragged(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult onMouseScrolled(int mouseX, int mouseY, double amount) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult onKeyPressed(int key, int scanCode, int modifiers) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult onCharTyped(char chr) {
      return UiInputResult.IGNORED;
   }
}
