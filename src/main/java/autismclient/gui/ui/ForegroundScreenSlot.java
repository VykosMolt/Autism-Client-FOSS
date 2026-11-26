package autismclient.gui.ui;

public final class ForegroundScreenSlot<T> {
   private T init;

   public T init_Obj(T background) {
      return this.init == null ? background : this.init;
   }

   public T init_Obj() {
      return this.init;
   }

   public boolean init_Z() {
      return this.init != null;
   }

   public void init_V(T screen) {
      if (this.init == null) {
         this.init = screen;
      }
   }

   public T load(T screen) {
      T previous = this.init;
      this.init = screen;
      return previous;
   }

   public T load() {
      return this.load(null);
   }
}
