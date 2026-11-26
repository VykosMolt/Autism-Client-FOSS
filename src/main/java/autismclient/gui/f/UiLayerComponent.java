package autismclient.gui.f;

public interface UiLayerComponent {
   NetHelper init_NetHelper();

   void init_V_NetHelper(NetHelper var1);

   default NetHelper load_NetHelper() {
      return this.init_NetHelper();
   }

   default void init_V_SyncManager(SyncManager context) {
   }

   default UiInputResult init_EventBus_I_2(int mouseX, int mouseY, int button) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult load(int mouseX, int mouseY, int button) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult init_EventBus_I(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult init_EventBus_I(int mouseX, int mouseY, double amount) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult read(int key, int scanCode, int modifiers) {
      return UiInputResult.IGNORED;
   }

   default UiInputResult init_EventBus_C(char chr) {
      return UiInputResult.IGNORED;
   }
}
