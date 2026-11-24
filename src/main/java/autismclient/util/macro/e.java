package autismclient.util.macro;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public final class e {
   private final EnumMap<DataPipe.Inner_DataUtil, Object> init = new EnumMap<>(DataPipe.Inner_DataUtil.class);

   public synchronized AutoCloseable init(EnumSet<DataPipe.Inner_DataUtil> resources) {
      EnumSet<DataPipe.Inner_DataUtil> claimed = resources.clone();

      for (DataPipe.Inner_DataUtil resource : claimed) {
         if (this.init.containsKey(resource)) {
            return null;
         }
      }

      Object token = new Object();

      for (DataPipe.Inner_DataUtil resource : claimed) {
         this.init.put(resource, token);
      }

      AtomicBoolean closed = new AtomicBoolean();
      return () -> {
         if (closed.compareAndSet(false, true)) {
            synchronized (this) {
               for (DataPipe.Inner_DataUtil resourcex : claimed) {
                  if (this.init.get(resourcex) == token) {
                     this.init.remove(resourcex);
                  }
               }
            }
         }
      };
   }

   public synchronized boolean init(DataPipe.Inner_DataUtil resource) {
      return this.init.containsKey(resource);
   }
}
