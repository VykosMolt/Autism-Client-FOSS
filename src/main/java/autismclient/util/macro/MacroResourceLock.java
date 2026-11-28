package autismclient.util.macro;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MacroResourceLock {
   private final EnumMap<MacroResourceClassifier.MacroResource, Object> owners = new EnumMap<>(MacroResourceClassifier.MacroResource.class);

   public synchronized AutoCloseable init(EnumSet<MacroResourceClassifier.MacroResource> resources) {
      EnumSet<MacroResourceClassifier.MacroResource> claimed = resources.clone();

      for (MacroResourceClassifier.MacroResource resource : claimed) {
         if (this.owners.containsKey(resource)) {
            return null;
         }
      }

      Object token = new Object();

      for (MacroResourceClassifier.MacroResource resource : claimed) {
         this.owners.put(resource, token);
      }

      AtomicBoolean closed = new AtomicBoolean();
      return () -> {
         if (closed.compareAndSet(false, true)) {
            synchronized (this) {
               for (MacroResourceClassifier.MacroResource resourcex : claimed) {
                  if (this.owners.get(resourcex) == token) {
                     this.owners.remove(resourcex);
                  }
               }
            }
         }
      };
   }

   public synchronized boolean init(MacroResourceClassifier.MacroResource resource) {
      return this.owners.containsKey(resource);
   }
}
