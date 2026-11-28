package autismclient.util;

public final class SessionContextWatcher {
   private Object lastWorld;
   private Object lastPlayer;
   private Object lastConnection;

   public void check(Object world, Object player, Object connection, Runnable reset) {
      if (this.lastWorld != world || this.lastPlayer != player || this.lastConnection != connection) {
         reset.run();
         this.lastWorld = world;
         this.lastPlayer = player;
         this.lastConnection = connection;
      }
   }

   public void clear() {
      this.lastWorld = this.lastPlayer = this.lastConnection = null;
   }
}
