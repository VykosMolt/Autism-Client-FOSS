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
   private static final Map<Integer, AtomicInteger> init_Map = new ConcurrentHashMap<>();
   private static final Map<Integer, ServerboundContainerClosePacket> load = new ConcurrentHashMap<>();
   private static final Map<Integer, Long> read = new ConcurrentHashMap<>();
   private static volatile Consumer<ContainerClickSnapshot.Inner_StreamUtil> init_Consumer = null;

   private ContainerCloseGuard() {
   }

   public static void init_V(int containerId) {
      if (containerId >= 0) {
         init_Map.computeIfAbsent(containerId, id -> new AtomicInteger(0)).incrementAndGet();
         read.put(containerId, System.currentTimeMillis());
      }
   }

   public static boolean init_Z(int containerId) {
      AtomicInteger counter = init_Map.get(containerId);
      return counter != null && counter.get() > 0;
   }

   public static void init(int containerId, ServerboundContainerClosePacket pkt) {
      if (pkt != null) {
         load.put(containerId, pkt);
         read.put(containerId, System.currentTimeMillis());
      }
   }

   public static void init(int containerId, ClientPacketListener conn) {
      AtomicInteger counter = init_Map.get(containerId);
      if (counter == null) {
         load(containerId, conn);
      } else if (counter.decrementAndGet() <= 0) {
         init_Map.remove(containerId);
         load(containerId, conn);
         read.remove(containerId);
      } else {
         read.put(containerId, System.currentTimeMillis());
      }
   }

   private static void load(int containerId, ClientPacketListener conn) {
      ServerboundContainerClosePacket pending = load.remove(containerId);
      if (!autismclient.modules.aD.read_Z() && pending != null && conn != null) {
         try {
            conn.send(pending);
         } catch (Throwable var4) {
         }
      }
   }

   public static void init_V() {
      init_Map.clear();
      load.clear();
      read.clear();
   }

   public static void init(ClientPacketListener conn) {
      init_Map.clear();
      read.clear();
      if (conn != null && !autismclient.modules.aD.read_Z()) {
         for (Entry<Integer, ServerboundContainerClosePacket> entry : load.entrySet()) {
            try {
               conn.send(entry.getValue());
            } catch (Throwable var4) {
            }
         }

         load.clear();
      } else {
         load.clear();
      }
   }

   public static void load(int newContainerId) {
      if (init_Map.remove(newContainerId) != null) {
         load.remove(newContainerId);
         read.remove(newContainerId);
      }
   }

   public static void load_V() {
      if (!read.isEmpty()) {
         long now = System.currentTimeMillis();
         List<Integer> expired = new ArrayList<>();

         for (Entry<Integer, Long> e : read.entrySet()) {
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
               init_Map.remove(id);
               read.remove(id);
               load(id, conn);
            }
         }
      }
   }

   public static boolean init_Z() {
      return !read.isEmpty();
   }

   public static void init(Consumer<ContainerClickSnapshot.Inner_StreamUtil> consumer) {
      init_Consumer = consumer;
   }

   public static boolean load_Z() {
      return init_Consumer != null;
   }

   public static boolean init(ContainerClickSnapshot.Inner_StreamUtil target) {
      Consumer<ContainerClickSnapshot.Inner_StreamUtil> consumer = init_Consumer;
      if (consumer != null && target != null) {
         init_Consumer = null;

         try {
            consumer.accept(target);
         } catch (Throwable var3) {
         }

         return true;
      } else {
         return false;
      }
   }

   public static void read() {
      init_Consumer = null;
   }
}
