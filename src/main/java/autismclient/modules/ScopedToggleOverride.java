package autismclient.modules;

import java.util.ArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class ScopedToggleOverride {
   private final BooleanSupplier configured;
   private final Consumer<Boolean> transition;
   private final ArrayList<ScopedToggleOverride.OverrideEntry> overrides = new ArrayList<>();

   public ScopedToggleOverride(BooleanSupplier configured, Consumer<Boolean> transition) {
      this.configured = configured;
      this.transition = transition;
   }

   public boolean isEnabled() {
      return this.overrides.isEmpty() ? this.configured.getAsBoolean() : this.overrides.getLast().enabled;
   }

   public ScopedToggleOverride.OverrideHandle push(boolean enabled) {
      boolean before = this.isEnabled();
      ScopedToggleOverride.OverrideEntry entry = new ScopedToggleOverride.OverrideEntry(enabled);
      this.overrides.add(entry);

      try {
         this.notifyIfChanged(before);
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
      this.overrides.clear();
   }

   private void notifyIfChanged(boolean before) {
      boolean after = this.isEnabled();
      if (before != after) {
         this.transition.accept(after);
      }
   }

   private final class OverrideEntry implements ScopedToggleOverride.OverrideHandle {
      final boolean enabled;

      OverrideEntry(boolean enabled) {
         this.enabled = enabled;
      }

      @Override
      public void close() {
         boolean before = ScopedToggleOverride.this.isEnabled();
         if (ScopedToggleOverride.this.overrides.remove(this)) {
            ScopedToggleOverride.this.notifyIfChanged(before);
         }
      }
   }

   public interface OverrideHandle extends AutoCloseable {
      @Override
      void close();
   }
}
