package autismclient.util.autologin;

import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;

public interface AutoLoginBackend {
   String init_Str();

   boolean init_Z();

   boolean load();

   CustomMenuSnapshot init_CustomMenuSnapshot();

   boolean init(CustomMenuSnapshot var1, CustomMenuSubmission var2);

   boolean init_Z(String var1);

   default boolean read() {
      return false;
   }

   default void init_V(String message) {
   }

   default void load(String context) {
   }
}
