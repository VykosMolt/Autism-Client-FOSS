package autismclient.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;

public final class ContainerCloseGuard {
   private static final long init_J = 60000L;
   private static final Map<Integer, AtomicInteger> holdCounts = new ConcurrentHashMap<>();
   private static final Map<Integer, ServerboundContainerClosePacket> deferredCloses = new ConcurrentHashMap<>();
   private static final Map<Integer, Long> lastTouched = new ConcurrentHashMap<>();
   private static volatile Consumer<ContainerClickSnapshot.Inner_StreamUtil> snapshotConsumer = null;

   private ContainerCloseGuard() {
   }

   public static void holdOpen(int containerId) {
      if (containerId >= 0) {
         holdCounts.computeIfAbsent(containerId, id -> new AtomicInteger(0)).incrementAndGet();
         lastTouched.put(containerId, System.currentTimeMillis());
      }
   }

   public static boolean isHeldOpen(int containerId) {
      AtomicInteger counter = holdCounts.get(containerId);
      return counter != null && counter.get() > 0;
   }

   public static void defer(int containerId, ServerboundContainerClosePacket pkt) {
      if (pkt != null) {
         deferredCloses.put(containerId, pkt);
         lastTouched.put(containerId, System.currentTimeMillis());
      }
   }

   public static void hold(int containerId, ClientPacketListener conn) {
      AtomicInteger counter = holdCounts.get(containerId);
      if (counter == null) {
         flushDeferredClose(containerId, conn);
      } else if (counter.decrementAndGet() <= 0) {
         holdCounts.remove(containerId);
         flushDeferredClose(containerId, conn);
         lastTouched.remove(containerId);
      } else {
         lastTouched.put(containerId, System.currentTimeMillis());
      }
   }

   private static void flushDeferredClose(int containerId, ClientPacketListener conn) {
      ServerboundContainerClosePacket pending = deferredCloses.remove(containerId);
      if (!autismclient.modules.aD.read_Z() && pending != null && conn != null) {
         try {
            conn.send(pending);
         } catch (Throwable var4) {
         }
      }
   }

   public static void clear() {
      holdCounts.clear();
      deferredCloses.clear();
      lastTouched.clear();
   }

   public static void init(ClientPacketListener conn) {
      holdCounts.clear();
      lastTouched.clear();
      if (conn != null && !autismclient.modules.aD.read_Z()) {
         for (Entry<Integer, ServerboundContainerClosePacket> entry : deferredCloses.entrySet()) {
            try {
               conn.send(entry.getValue());
            } catch (Throwable var4) {
            }
         }

         deferredCloses.clear();
      } else {
         deferredCloses.clear();
      }
   }

   public static void onOpen(int newContainerId) {
      if (holdCounts.remove(newContainerId) != null) {
         deferredCloses.remove(newContainerId);
         lastTouched.remove(newContainerId);
      }
   }

   public static void expireStale() {
      if (!lastTouched.isEmpty()) {
         long now = System.currentTimeMillis();
         List<Integer> expired = new ArrayList<>();

         for (Entry<Integer, Long> e : lastTouched.entrySet()) {
            if (now - e.getValue() >= 60000L) {
               expired.add(e.getKey());
            }
         }

         if (!expired.isEmpty()) {
            ClientPacketListener conn = null;

            try {
               Minecraft mc = Minecraft.getInstance();
               conn = mc == null ? null : mc.getConnection();
            } catch (Throwable var6) {
            }

            for (Integer id : expired) {
               holdCounts.remove(id);
               lastTouched.remove(id);
               flushDeferredClose(id, conn);
            }
         }
      }
   }

   public static boolean hasHolds() {
      return !lastTouched.isEmpty();
   }

   public static void setSnapshotConsumer(Consumer<ContainerClickSnapshot.Inner_StreamUtil> consumer) {
      snapshotConsumer = consumer;
   }

   public static boolean isCapturing() {
      return snapshotConsumer != null;
   }

   public static boolean offerSnapshot(ContainerClickSnapshot.Inner_StreamUtil target) {
      Consumer<ContainerClickSnapshot.Inner_StreamUtil> consumer = snapshotConsumer;
      if (consumer != null && target != null) {
         snapshotConsumer = null;

         try {
            consumer.accept(target);
         } catch (Throwable var3) {
         }

         return true;
      } else {
         return false;
      }
   }

   public static void clearSnapshotConsumer() {
      snapshotConsumer = null;
   }
}
