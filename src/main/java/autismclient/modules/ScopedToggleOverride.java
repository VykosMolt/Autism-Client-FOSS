package autismclient.modules;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ScopedToggleOverride {
   private final BooleanSupplier init_BooleanSupplier;
   private final Consumer<Boolean> init_Consumer;
   private final ArrayList<ScopedToggleOverride.Inner_DataUtil> init_ArrayList = new ArrayList<>();

   public ScopedToggleOverride(BooleanSupplier configured, Consumer<Boolean> transition) {
      this.init_BooleanSupplier = configured;
      this.init_Consumer = transition;
   }

   public boolean init_Z() {
      return this.init_ArrayList.isEmpty() ? this.init_BooleanSupplier.getAsBoolean() : this.init_ArrayList.getLast().init;
   }

   public ScopedToggleOverride.Inner_StreamUtil init_aP_StreamUtil(boolean enabled) {
      boolean before = this.init_Z();
      ScopedToggleOverride.Inner_DataUtil entry = new ScopedToggleOverride.Inner_DataUtil(enabled);
      this.init_ArrayList.add(entry);

      try {
         this.init_V(before);
         return entry;
      } catch (Throwable error) {
         try {
            entry.close();
         } catch (Throwable cleanup) {
            error.addSuppressed(cleanup);
         }

         throw error;
      }
   }

   public void init_V() {
      this.init_ArrayList.clear();
   }

   private void init_V(boolean before) {
      boolean after = this.init_Z();
      if (before != after) {
         this.init_Consumer.accept(after);
      }
   }

   private final class Inner_DataUtil implements ScopedToggleOverride.Inner_StreamUtil {
      final boolean init;

      Inner_DataUtil(boolean enabled) {
         this.init = enabled;
      }

      @Override
      public void close() {
         boolean before = ScopedToggleOverride.this.init_Z();
         if (ScopedToggleOverride.this.init_ArrayList.remove(this)) {
            ScopedToggleOverride.this.init_V(before);
         }
      }
   }

   public interface Inner_StreamUtil extends AutoCloseable {
      @Override
      void close();
   }
}
