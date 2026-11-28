package autismclient.util.autologin;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;

public interface AutoLoginBackend {
   String password();

   boolean isConnected();

   boolean hasConnection();

   CustomMenuSnapshot currentMenu();

   boolean submit(CustomMenuSnapshot var1, CustomMenuSubmission var2);

   boolean sendCommand(String var1);

   default boolean isBusy() {
      return false;
   }

   default void report(String message) {
   }

   default void onMissingPassword(String context) {
   }
}
