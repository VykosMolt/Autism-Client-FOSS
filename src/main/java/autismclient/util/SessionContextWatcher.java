package autismclient.util;

public final class SessionContextWatcher {
   private Object init;
   private Object load;
   private Object read;

   public void init(Object world, Object player, Object connection, Runnable reset) {
      if (this.init != world || this.load != player || this.read != connection) {
         reset.run();
         this.init = world;
         this.load = player;
         this.read = connection;
      }
   }

   public void init() {
      this.init = this.load = this.read = null;
   }
}
