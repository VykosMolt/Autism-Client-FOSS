package autismclient.gui.ui;

public final class ForegroundScreenSlot<T> {
   private T foreground;

   public T orElse(T background) {
      return this.foreground == null ? background : this.foreground;
   }

   public T get() {
      return this.foreground;
   }

   public boolean isSet() {
      return this.foreground != null;
   }

   public void setIfEmpty(T screen) {
      if (this.foreground == null) {
         this.foreground = screen;
      }
   }

   public T load(T screen) {
      T previous = this.foreground;
      this.foreground = screen;
      return previous;
   }

   public T load() {
      return this.load(null);
   }
}
