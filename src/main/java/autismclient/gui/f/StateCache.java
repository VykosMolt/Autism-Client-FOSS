package autismclient.gui.f;

public interface StateCache {
   NetHelper init_NetHelper();

   void init_V_NetHelper(NetHelper var1);

   default NetHelper load_NetHelper() {
      return this.init_NetHelper();
   }

   default void init_V_SyncManager(SyncManager context) {
   }

   default EventBus init_EventBus_I_2(int mouseX, int mouseY, int button) {
      return EventBus.init;
   }

   default EventBus load(int mouseX, int mouseY, int button) {
      return EventBus.init;
   }

   default EventBus init_EventBus_I(int mouseX, int mouseY, int button, double deltaX, double deltaY) {
      return EventBus.init;
   }

   default EventBus init_EventBus_I(int mouseX, int mouseY, double amount) {
      return EventBus.init;
   }

   default EventBus read(int key, int scanCode, int modifiers) {
      return EventBus.init;
   }

   default EventBus init_EventBus_C(char chr) {
      return EventBus.init;
   }
}
