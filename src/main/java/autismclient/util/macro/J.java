package autismclient.util.macro;

import autismclient.AutismClientAddon;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchFinishedPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBatchStartPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundChunkBatchReceivedPacket;
import net.minecraft.server.RunningOnDifferentThreadException;

public final class J {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static volatile boolean init_Z;
   private static volatile int init_I;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile boolean write_Z;
   private static final long init_J = 0L;
   private static final ConcurrentHashMap<Long, J.Inner_StreamUtil> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private static final Queue<J.Inner_DataUtil> init_Queue = new ConcurrentLinkedQueue<>();
   private static final Queue<J.Inner_DataUtil> load_Queue = new ConcurrentLinkedQueue<>();
   // ConcurrentLinkedQueue.size() walks the whole queue, so the tick reads these counters instead.
   private static final AtomicInteger incomingQueued = new AtomicInteger();
   private static final AtomicInteger outgoingQueued = new AtomicInteger();
   private static final Set<Packet<?>> init_Set = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static volatile boolean update_Z;
   private static final long load_J = 8000L;
   private static final int load_I = 8192;
   private static final int read_I = 1024;
   private static final int write_I = 256;
   private static final AtomicInteger load_AtomicInteger = new AtomicInteger();
   private static volatile int update_I;
   private static volatile ScheduledExecutorService init_ScheduledExecutorS;
   private static volatile long read_J;
   private static final long write_J = 5000L;

   private static ScheduledExecutorService init_ScheduledExecutorS() {
      ScheduledExecutorService local = init_ScheduledExecutorS;
      if (local == null) {
         synchronized (J.class) {
            local = init_ScheduledExecutorS;
            if (local == null) {
               local = Executors.newSingleThreadScheduledExecutor(r -> {
                  Thread t = new Thread(r, "Autism-PingSpoof-Scheduler");
                  t.setDaemon(true);
                  return t;
               });
               init_ScheduledExecutorS = local;
            }
         }
      }

      return local;
   }

   private J() {
   }

   public static void init_V_I(int delayMs, boolean realIncoming, boolean realOutgoing, long durationNanos) {
      init(0L, delayMs, realIncoming, realOutgoing, durationNanos);
   }

   public static void init(long ownerRunId, int delayMs, boolean realIncoming, boolean realOutgoing, long durationNanos) {
      if (durationNanos <= 0L) {
         init(ownerRunId);
      } else {
         init_ConcurrentHashMap.put(
            ownerRunId, new J.Inner_StreamUtil(Math.max(0, delayMs), realIncoming, realOutgoing, System.nanoTime() + durationNanos, false)
         );
         write_Z = true;
      }
   }

   public static void init(int delayMs, boolean realIncoming, boolean realOutgoing) {
      init_V_J(0L, delayMs, realIncoming, realOutgoing);
   }

   public static void init_V_J(long ownerRunId, int delayMs, boolean realIncoming, boolean realOutgoing) {
      init_ConcurrentHashMap.put(ownerRunId, new J.Inner_StreamUtil(Math.max(0, delayMs), realIncoming, realOutgoing, 0L, true));
      write_Z = true;
   }

   public static void init_V() {
      init(0L);
   }

   public static void init(long ownerRunId) {
      init_ConcurrentHashMap.remove(ownerRunId);
      flush();
   }

   public static void load_V() {
      init_ConcurrentHashMap.clear();
      flush();
   }

   public static void load(int delayMs, boolean realIncoming, boolean realOutgoing) {
      init_I = Math.max(0, delayMs);
      load_Z = realIncoming;
      read_Z = realOutgoing;
      init_Z = true;
      write_Z = true;
   }

   public static void read_V() {
      init_Z = false;
      flush();
   }

   public static int init_I() {
      if (!write_Z) {
         return -1;
      }

      sync();
      if (init_ConcurrentHashMap.isEmpty()) {
         return init_Z ? init_I : -1;
      }

      int effective = 0;

      for (J.Inner_StreamUtil override : init_ConcurrentHashMap.values()) {
         effective = Math.max(effective, override.init_I);
      }

      return effective;
   }

   public static boolean init_Z() {
      if (!write_Z) {
         return false;
      }

      sync();
      if (!init_ConcurrentHashMap.isEmpty()) {
         for (J.Inner_StreamUtil override : init_ConcurrentHashMap.values()) {
            if (override.init_Z) {
               return true;
            }
         }

         return false;
      } else {
         return init_Z && load_Z;
      }
   }

   public static boolean load_Z() {
      if (!write_Z) {
         return false;
      }

      sync();
      if (!init_ConcurrentHashMap.isEmpty()) {
         for (J.Inner_StreamUtil override : init_ConcurrentHashMap.values()) {
            if (override.load) {
               return true;
            }
         }

         return false;
      } else {
         return init_Z && read_Z;
      }
   }

   public static boolean read_Z() {
      return init_I() >= 0;
   }

   private static void sync() {
      if (init_ConcurrentHashMap.isEmpty()) {
         flush();
      } else {
         long now = System.nanoTime();

         for (Entry<Long, J.Inner_StreamUtil> entry : init_ConcurrentHashMap.entrySet()) {
            if (!entry.getValue().init(now)) {
               init_ConcurrentHashMap.remove(entry.getKey(), entry.getValue());
            }
         }

         flush();
      }
   }

   private static void flush() {
      write_Z = init_Z || !init_ConcurrentHashMap.isEmpty();
      if (!write_Z && init_ScheduledExecutorS != null) {
         ScheduledExecutorService local;
         synchronized (J.class) {
            local = init_ScheduledExecutorS;
            init_ScheduledExecutorS = null;
         }

         // Already scheduled keep-alives still run; the next delay recreates the scheduler.
         if (local != null) {
            local.shutdown();
         }
      }
   }

   public static void write() {
      init_Queue.clear();
      load_Queue.clear();
      incomingQueued.set(0);
      outgoingQueued.set(0);
      init_Set.clear();
      init_AtomicInteger.set(0);
      update_Z = false;
      update_I++;
      load_AtomicInteger.set(0);
   }

   public static boolean init_Z(Packet<?> packet) {
      if (auditIsDisconnect(packet)) {
         write();
         return false;
      } else if (!write_Z) {
         return false;
      } else if (init_Minecraft == null || init_Minecraft.player == null) {
         return false;
      } else {
         int delay = init_I();
         if (delay <= 0) {
            return false;
         } else if (!init_Z()) {
            return false;
         } else if (write(packet)) {
            return false;
         } else if (update(packet)) {
            init_Queue.add(new J.Inner_DataUtil(packet, System.currentTimeMillis()));
            incomingQueued.incrementAndGet();
            update_Z = true;
            return true;
         } else {
            init_Queue.add(new J.Inner_DataUtil(packet, System.currentTimeMillis()));
            incomingQueued.incrementAndGet();
            return true;
         }
      }
   }

   public static boolean load_Z(Packet<?> packet) {
      if (read_Z(packet)) {
         return false;
      }

      if (!write_Z) {
         return false;
      }

      if (init_Minecraft == null || init_Minecraft.player == null) {
         return false;
      }

      int delay = init_I();
      if (delay > 0 && packet instanceof ServerboundKeepAlivePacket) {
         init(packet, delay);
         return true;
      }

      if (delay <= 0 || !load_Z()) {
         return false;
      }

      if (sync(packet)) {
         return false;
      }

      load_Queue.add(new J.Inner_DataUtil(packet, System.currentTimeMillis()));
      outgoingQueued.incrementAndGet();
      return true;
   }

   public static void update() {
      if (init_Minecraft == null || init_Minecraft.getConnection() == null) {
         if (!init_Queue.isEmpty() || !load_Queue.isEmpty() || load_AtomicInteger.get() > 0) {
            write();
         }
         update_Z = false;
      } else if (init_Queue.isEmpty() && load_Queue.isEmpty()) {
         update_Z = false;
      } else {
         long now = System.currentTimeMillis();
         if (!init_Queue.isEmpty()) {
            int delay = init_I();
            boolean flushAll = update_Z || delay < 0 || !init_Z() || incomingQueued.get() >= 8192;
            update_Z = false;
            long threshold = flushAll ? 0L : delay;
            int budget = flushAll ? 2147483647 : 1024;

            J.Inner_DataUtil head;
            while (budget-- > 0 && (head = init_Queue.peek()) != null && (flushAll || now - head.init_J() >= threshold || now - head.init_J() >= 8000L)) {
               if (init_Queue.poll() != null) {
                  incomingQueued.updateAndGet(count -> Math.max(0, count - 1));
               }

               init_V(head.init_Packet());
            }
         }

         if (!load_Queue.isEmpty()) {
            int delay = init_I();
            boolean flushAll = delay < 0 || !load_Z() || outgoingQueued.get() >= 8192;
            long threshold = flushAll ? 0L : delay;
            int budget = flushAll ? 2147483647 : 1024;

            J.Inner_DataUtil head;
            while (budget-- > 0 && (head = load_Queue.peek()) != null && (flushAll || now - head.init_J() >= threshold || now - head.init_J() >= 8000L)) {
               if (load_Queue.poll() != null) {
                  outgoingQueued.updateAndGet(count -> Math.max(0, count - 1));
               }

               load_V(head.init_Packet());
            }
         }
      }
   }

   private static void init(Packet<?> packet, int delayMs) {
      long wait = load_AtomicInteger.get() >= 256 ? 0L : Math.max(0L, Math.min(delayMs, 8000L));
      int epoch = update_I;
      if (wait <= 0L) {
         load(packet, epoch);
      } else {
         load_AtomicInteger.incrementAndGet();

         try {
            init_ScheduledExecutorS().schedule(() -> {
               load_AtomicInteger.decrementAndGet();
               load(packet, epoch);
            }, wait, TimeUnit.MILLISECONDS);
         } catch (Throwable rejected) {
            load_AtomicInteger.decrementAndGet();
            load(packet, epoch);
         }
      }
   }

   private static void load(Packet<?> packet, int epoch) {
      if (epoch == update_I) {
         ClientPacketListener connection = init_Minecraft == null ? null : init_Minecraft.getConnection();
         if (connection != null) {
            read_V(packet);

            try {
               connection.send(packet);
            } catch (Throwable t) {
               init("keepalive", packet, t);
            } finally {
               read_Z(packet);
            }
         }
      }
   }

   private static void init_V(Packet<?> packet) {
      ClientPacketListener connection = init_Minecraft == null ? null : init_Minecraft.getConnection();
      if (connection != null) {
         try {
            Packet<ClientGamePacketListener> gamePacket = (Packet<ClientGamePacketListener>)packet;
            gamePacket.handle(connection);
         } catch (RunningOnDifferentThreadException var3) {
         } catch (Throwable t) {
            init("incoming", packet, t);
         }
      }
   }

   private static void load_V(Packet<?> packet) {
      ClientPacketListener connection = init_Minecraft == null ? null : init_Minecraft.getConnection();
      if (connection != null) {
         read_V(packet);

         try {
            connection.send(packet);
         } catch (Throwable t) {
            init("outgoing", packet, t);
         } finally {
            read_Z(packet);
         }
      }
   }

   private static void read_V(Packet<?> packet) {
      if (packet != null && init_Set.add(packet)) {
         init_AtomicInteger.incrementAndGet();
      }
   }

   private static boolean read_Z(Packet<?> packet) {
      if (init_AtomicInteger.get() == 0 || packet == null) {
         return false;
      }

      if (!init_Set.remove(packet)) {
         return false;
      }

      init_AtomicInteger.updateAndGet(count -> Math.max(0, count - 1));
      return true;
   }

   private static void init(String direction, Packet<?> packet, Throwable t) {
      if (!(t instanceof RunningOnDifferentThreadException) && !(t.getCause() instanceof RunningOnDifferentThreadException)) {
         long now = System.currentTimeMillis();
         if (now - read_J >= 5000L) {
            read_J = now;
            AutismClientAddon.LOG.warn("[Autism] PingSpoof re-dispatch ({}) failed for {}", direction, packet.getClass().getSimpleName(), t);
         }
      }
   }

   private static boolean write(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (write(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundSystemChatPacket
         || packet instanceof ClientboundDisguisedChatPacket
         || packet instanceof ClientboundKeepAlivePacket
         || packet instanceof ClientboundPingPacket
         || packet instanceof ClientboundStartConfigurationPacket;
   }

   private static boolean auditIsDisconnect(Packet<?> packet) {
      if (packet instanceof ClientboundDisconnectPacket || packet instanceof ClientboundStartConfigurationPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundDisconnectPacket || inner instanceof ClientboundStartConfigurationPacket) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean update(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (update(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundPlayerPositionPacket
         || packet instanceof ClientboundRespawnPacket
         || packet instanceof ClientboundLoginPacket
         || packet instanceof ClientboundDisconnectPacket
         || packet instanceof ClientboundSetHealthPacket health && health.getHealth() <= 0.0F
         || packet instanceof ClientboundLevelChunkWithLightPacket
         || packet instanceof ClientboundLightUpdatePacket
         || packet instanceof ClientboundForgetLevelChunkPacket
         || packet instanceof ClientboundChunkBatchStartPacket
         || packet instanceof ClientboundChunkBatchFinishedPacket
         || packet instanceof ClientboundSetChunkCacheCenterPacket
         || packet instanceof ClientboundSetChunkCacheRadiusPacket
         || packet instanceof ClientboundSectionBlocksUpdatePacket
         || packet instanceof ClientboundCustomPayloadPacket;
   }

   private static boolean sync(Packet<?> packet) {
      return packet instanceof ServerboundKeepAlivePacket
         || packet instanceof ServerboundPongPacket
         || packet instanceof ServerboundChatPacket
         || packet instanceof ServerboundChatCommandPacket
         || packet instanceof ServerboundChatCommandSignedPacket
         || packet instanceof ServerboundResourcePackPacket
         || packet instanceof ServerboundChunkBatchReceivedPacket;
   }

   private record Inner_DataUtil(Packet<?> init_Packet, long init_J) {
   }

   private record Inner_StreamUtil(int init_I, boolean init_Z, boolean load, long init_J, boolean read) {
      boolean init(long now) {
         return this.read || now - this.init_J < 0L;
      }
   }
}
