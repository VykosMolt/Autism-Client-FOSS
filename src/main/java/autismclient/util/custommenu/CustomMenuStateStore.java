package autismclient.util.custommenu;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuEvent;
import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuStateStore {
   private volatile long revision;
   private volatile CustomMenuSnapshot snapshot;

   public boolean init(Packet<?> packet, String phase) {
      return !CustomMenuAdapterRegistry.acceptsInbound(packet) ? false : this.load(packet, phase);
   }

   public boolean load(Packet<?> packet, String phase) {
      CustomMenuEvent event = CustomMenuAdapterRegistry.inspect(packet, phase);
      if (event.type() == CustomMenuEvent.Type.NONE) {
         return false;
      }

      synchronized (this) {
         if (event.type() == CustomMenuEvent.Type.CLEAR) {
            this.snapshot = null;
            this.revision++;
         } else if (event.snapshot() != null) {
            this.snapshot = event.snapshot().withConnectionState(phase, ++this.revision);
         }

         return true;
      }
   }

   public CustomMenuSnapshot snapshot() {
      return this.snapshot;
   }

   public long generation() {
      return this.revision;
   }

   public synchronized void init(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      if (expected == null || this.snapshot == null || this.snapshot.generation() == expected.generation()) {
         this.snapshot = replacement == null ? null : replacement.withConnectionState(expected == null ? "" : expected.phase(), ++this.revision);
      }
   }

   public synchronized void init(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      if (this.revision == seenGeneration) {
         this.revision++;
         this.snapshot = replacement == null ? null : replacement.withConnectionState(phase == null ? "" : phase, this.revision);
      }
   }

   public void clear() {
      if (this.snapshot != null) {
         synchronized (this) {
            if (this.snapshot != null) {
               this.snapshot = null;
               this.revision++;
            }
         }
      }
   }
}
