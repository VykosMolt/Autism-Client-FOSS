package autismclient.util.macro;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class MacroResourceLock {
   private final EnumMap<MacroResourceClassifier.Inner_DataUtil, Object> owners = new EnumMap<>(MacroResourceClassifier.Inner_DataUtil.class);

   public synchronized AutoCloseable init(EnumSet<MacroResourceClassifier.Inner_DataUtil> resources) {
      EnumSet<MacroResourceClassifier.Inner_DataUtil> claimed = resources.clone();

      for (MacroResourceClassifier.Inner_DataUtil resource : claimed) {
         if (this.owners.containsKey(resource)) {
            return null;
         }
      }

      Object token = new Object();

      for (MacroResourceClassifier.Inner_DataUtil resource : claimed) {
         this.owners.put(resource, token);
      }

      AtomicBoolean closed = new AtomicBoolean();
      return () -> {
         if (closed.compareAndSet(false, true)) {
            synchronized (this) {
               for (MacroResourceClassifier.Inner_DataUtil resourcex : claimed) {
                  if (this.owners.get(resourcex) == token) {
                     this.owners.remove(resourcex);
                  }
               }
            }
         }
      };
   }

   public synchronized boolean init(MacroResourceClassifier.Inner_DataUtil resource) {
      return this.owners.containsKey(resource);
   }
}
