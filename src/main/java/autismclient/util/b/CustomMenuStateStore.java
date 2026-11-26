package autismclient.util.b;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuEvent;
import autismclient.api.custommenu.CustomMenuSnapshot;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuStateStore {
   private volatile long init_J;
   private volatile CustomMenuSnapshot init_CustomMenuSnapshot;

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
            this.init_CustomMenuSnapshot = null;
            this.init_J++;
         } else if (event.snapshot() != null) {
            this.init_CustomMenuSnapshot = event.snapshot().withConnectionState(phase, ++this.init_J);
         }

         return true;
      }
   }

   public CustomMenuSnapshot init_CustomMenuSnapshot() {
      return this.init_CustomMenuSnapshot;
   }

   public long init_J() {
      return this.init_J;
   }

   public synchronized void init(CustomMenuSnapshot expected, CustomMenuSnapshot replacement) {
      if (expected == null || this.init_CustomMenuSnapshot == null || this.init_CustomMenuSnapshot.generation() == expected.generation()) {
         this.init_CustomMenuSnapshot = replacement == null ? null : replacement.withConnectionState(expected == null ? "" : expected.phase(), ++this.init_J);
      }
   }

   public synchronized void init(long seenGeneration, CustomMenuSnapshot replacement, String phase) {
      if (this.init_J == seenGeneration) {
         this.init_J++;
         this.init_CustomMenuSnapshot = replacement == null ? null : replacement.withConnectionState(phase == null ? "" : phase, this.init_J);
      }
   }

   public void init_V() {
      if (this.init_CustomMenuSnapshot != null) {
         synchronized (this) {
            if (this.init_CustomMenuSnapshot != null) {
               this.init_CustomMenuSnapshot = null;
               this.init_J++;
            }
         }
      }
   }
}
