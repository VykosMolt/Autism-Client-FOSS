package autismclient.modules;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ScopedToggleOverride {
   private final BooleanSupplier configured;
   private final Consumer<Boolean> transition;
   private final ArrayList<ScopedToggleOverride.OverrideEntry> init_ArrayList = new ArrayList<>();

   public ScopedToggleOverride(BooleanSupplier configured, Consumer<Boolean> transition) {
      this.configured = configured;
      this.transition = transition;
   }

   public boolean isEnabled() {
      return this.init_ArrayList.isEmpty() ? this.configured.getAsBoolean() : this.init_ArrayList.getLast().init;
   }

   public ScopedToggleOverride.OverrideHandle push(boolean enabled) {
      boolean before = this.isEnabled();
      ScopedToggleOverride.OverrideEntry entry = new ScopedToggleOverride.OverrideEntry(enabled);
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

   public void clear() {
      this.init_ArrayList.clear();
   }

   private void init_V(boolean before) {
      boolean after = this.isEnabled();
      if (before != after) {
         this.transition.accept(after);
      }
   }

   private final class OverrideEntry implements ScopedToggleOverride.OverrideHandle {
      final boolean init;

      OverrideEntry(boolean enabled) {
         this.init = enabled;
      }

      @Override
      public void close() {
         boolean before = ScopedToggleOverride.this.isEnabled();
         if (ScopedToggleOverride.this.init_ArrayList.remove(this)) {
            ScopedToggleOverride.this.init_V(before);
         }
      }
   }

   public interface OverrideHandle extends AutoCloseable {
      @Override
      void close();
   }
}
