package autismclient.gui.ui;

public final class ForegroundScreenSlot<T> {
   private T init;

   public T orElse(T background) {
      return this.init == null ? background : this.init;
   }

   public T get() {
      return this.init;
   }

   public boolean isSet() {
      return this.init != null;
   }

   public void setIfEmpty(T screen) {
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
