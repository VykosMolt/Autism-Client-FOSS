package autismclient.util.autologin;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;

public interface AutoLoginBackend {
   String password();

   boolean isConnected();

   boolean load();

   CustomMenuSnapshot currentMenu();

   boolean init(CustomMenuSnapshot var1, CustomMenuSubmission var2);

   boolean sendCommand(String var1);

   default boolean read() {
      return false;
   }

   default void report(String message) {
   }

   default void load(String context) {
   }
}
