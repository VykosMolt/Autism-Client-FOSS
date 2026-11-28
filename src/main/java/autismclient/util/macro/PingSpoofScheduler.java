package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.util.AutismLiteVariant;
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

public final class PingSpoofScheduler {
   private static final Minecraft MINECRAFT = Minecraft.getInstance();
   private static volatile boolean globalEnabled;
   private static volatile int globalDelayMs;
   private static volatile boolean globalRealIncoming;
   private static volatile boolean globalRealOutgoing;
   private static volatile boolean write_Z;
   private static final long init_J = 0L;
   private static final ConcurrentHashMap<Long, PingSpoofScheduler.RunOverride> runOverrides = new ConcurrentHashMap<>();
   private static final Queue<PingSpoofScheduler.Inner_DataUtil> init_Queue = new ConcurrentLinkedQueue<>();
   private static final Queue<PingSpoofScheduler.Inner_DataUtil> load_Queue = new ConcurrentLinkedQueue<>();
   // ConcurrentLinkedQueue.size() walks the whole queue, so the tick reads these counters instead.
   private static final AtomicInteger incomingQueued = new AtomicInteger();
   private static final AtomicInteger outgoingQueued = new AtomicInteger();
   private static final Set<Packet<?>> ownPackets = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
   private static final AtomicInteger ownPacketCount = new AtomicInteger();
   private static volatile boolean update_Z;
   private static final long load_J = 8000L;
   private static final int load_I = 8192;
   private static final int read_I = 1024;
   private static final int write_I = 256;
   private static final AtomicInteger load_AtomicInteger = new AtomicInteger();
   private static volatile int scheduleEpoch;
   private static volatile ScheduledExecutorService schedulerInstance;
   private static volatile long read_J;
   private static final long write_J = 5000L;

   private static ScheduledExecutorService scheduler() {
      ScheduledExecutorService local = schedulerInstance;
      if (local == null) {
         synchronized (PingSpoofScheduler.class) {
            local = schedulerInstance;
            if (local == null) {
               local = Executors.newSingleThreadScheduledExecutor(r -> {
                  Thread t = new Thread(r, "Autism-PingSpoof-Scheduler");
                  t.setDaemon(true);
                  return t;
               });
               schedulerInstance = local;
            }
         }
      }

      return local;
   }

   private PingSpoofScheduler() {
   }

   public static void enable(int delayMs, boolean realIncoming, boolean realOutgoing, long durationNanos) {
      start(0L, delayMs, realIncoming, realOutgoing, durationNanos);
   }

   public static void start(long ownerRunId, int delayMs, boolean realIncoming, boolean realOutgoing, long durationNanos) {
      if (durationNanos <= 0L) {
         stop(ownerRunId);
      } else {
         runOverrides.put(
            ownerRunId, new PingSpoofScheduler.RunOverride(Math.max(0, delayMs), realIncoming, realOutgoing, System.nanoTime() + durationNanos, false)
         );
         write_Z = true;
      }
   }

   public static void setDefaultRunOverride(int delayMs, boolean realIncoming, boolean realOutgoing) {
      setRunOverride(0L, delayMs, realIncoming, realOutgoing);
   }

   public static void setRunOverride(long ownerRunId, int delayMs, boolean realIncoming, boolean realOutgoing) {
      runOverrides.put(ownerRunId, new PingSpoofScheduler.RunOverride(Math.max(0, delayMs), realIncoming, realOutgoing, 0L, true));
      write_Z = true;
   }

   public static void init_V() {
      stop(0L);
   }

   public static void stop(long ownerRunId) {
      runOverrides.remove(ownerRunId);
      updateSchedulerState();
   }

   public static void clearRunOverrides() {
      runOverrides.clear();
      updateSchedulerState();
   }

   public static void setGlobal(int delayMs, boolean realIncoming, boolean realOutgoing) {
      globalDelayMs = Math.max(0, delayMs);
      globalRealIncoming = realIncoming;
      globalRealOutgoing = realOutgoing;
      globalEnabled = true;
      write_Z = true;
   }

   public static void disable() {
      globalEnabled = false;
      updateSchedulerState();
   }

   public static int effectiveDelayMs() {
      if (!write_Z) {
         return -1;
      }

      pruneExpiredOverrides();
      if (runOverrides.isEmpty()) {
         return globalEnabled ? globalDelayMs : -1;
      }

      int effective = 0;

      for (PingSpoofScheduler.RunOverride override : runOverrides.values()) {
         effective = Math.max(effective, override.delayMs);
      }

      return effective;
   }

   public static boolean isRealIncomingActive() {
      if (!write_Z) {
         return false;
      }

      pruneExpiredOverrides();
      if (!runOverrides.isEmpty()) {
         for (PingSpoofScheduler.RunOverride override : runOverrides.values()) {
            if (override.realIncoming) {
               return true;
            }
         }

         return false;
      } else {
         return globalEnabled && globalRealIncoming;
      }
   }

   public static boolean isRealOutgoing() {
      if (!write_Z) {
         return false;
      }

      pruneExpiredOverrides();
      if (!runOverrides.isEmpty()) {
         for (PingSpoofScheduler.RunOverride override : runOverrides.values()) {
            if (override.load) {
               return true;
            }
         }

         return false;
      } else {
         return globalEnabled && globalRealOutgoing;
      }
   }

   public static boolean isActive() {
      return effectiveDelayMs() >= 0;
   }

   private static void pruneExpiredOverrides() {
      if (runOverrides.isEmpty()) {
         updateSchedulerState();
      } else {
         long now = System.nanoTime();

         for (Entry<Long, PingSpoofScheduler.RunOverride> entry : runOverrides.entrySet()) {
            if (!entry.getValue().isLive(now)) {
               runOverrides.remove(entry.getKey(), entry.getValue());
            }
         }

         updateSchedulerState();
      }
   }

   private static void updateSchedulerState() {
      write_Z = globalEnabled || !runOverrides.isEmpty();
      if (!write_Z && schedulerInstance != null) {
         ScheduledExecutorService local;
         synchronized (PingSpoofScheduler.class) {
            local = schedulerInstance;
            schedulerInstance = null;
         }

         // Already scheduled keep-alives still run; the next delay recreates the scheduler.
         if (local != null) {
            local.shutdown();
         }
      }
   }

   public static void clearQueues() {
      init_Queue.clear();
      load_Queue.clear();
      incomingQueued.set(0);
      outgoingQueued.set(0);
      ownPackets.clear();
      ownPacketCount.set(0);
      update_Z = false;
      scheduleEpoch++;
      load_AtomicInteger.set(0);
   }

   public static boolean interceptIncoming(Packet<?> packet) {
      if (auditIsDisconnect(packet)) {
         clearQueues();
         return false;
      } else if (!write_Z) {
         return false;
      } else if (MINECRAFT == null || MINECRAFT.player == null) {
         return false;
      } else {
         int delay = effectiveDelayMs();
         if (delay <= 0) {
            return false;
         } else if (!isRealIncomingActive()) {
            return false;
         } else if (write(packet)) {
            return false;
         } else if (update(packet)) {
            init_Queue.add(new PingSpoofScheduler.Inner_DataUtil(packet, System.currentTimeMillis()));
            incomingQueued.incrementAndGet();
            update_Z = true;
            return true;
         } else {
            init_Queue.add(new PingSpoofScheduler.Inner_DataUtil(packet, System.currentTimeMillis()));
            incomingQueued.incrementAndGet();
            return true;
         }
      }
   }

   public static boolean interceptOutgoing(Packet<?> packet) {
      if (consumeOwnPacket(packet)) {
         return false;
      }

      if (!AutismLiteVariant.enabled() && !autismclient.e.MethodUtil.init()) {
         return false;
      }

      if (!write_Z) {
         return false;
      }

      if (MINECRAFT == null || MINECRAFT.player == null) {
         return false;
      }

      int delay = effectiveDelayMs();
      if (delay > 0 && packet instanceof ServerboundKeepAlivePacket) {
         schedule(packet, delay);
         return true;
      }

      if (delay <= 0 || !isRealOutgoing()) {
         return false;
      }

      if (sync(packet)) {
         return false;
      }

      load_Queue.add(new PingSpoofScheduler.Inner_DataUtil(packet, System.currentTimeMillis()));
      outgoingQueued.incrementAndGet();
      return true;
   }

   public static void tick() {
      if (MINECRAFT == null || MINECRAFT.getConnection() == null) {
         if (!init_Queue.isEmpty() || !load_Queue.isEmpty() || load_AtomicInteger.get() > 0) {
            clearQueues();
         }
         update_Z = false;
      } else if (init_Queue.isEmpty() && load_Queue.isEmpty()) {
         update_Z = false;
      } else {
         long now = System.currentTimeMillis();
         if (!init_Queue.isEmpty()) {
            int delay = effectiveDelayMs();
            boolean flushAll = update_Z || delay < 0 || !isRealIncomingActive() || incomingQueued.get() >= 8192;
            update_Z = false;
            long threshold = flushAll ? 0L : delay;
            int budget = flushAll ? 2147483647 : 1024;

            PingSpoofScheduler.Inner_DataUtil head;
            while (budget-- > 0 && (head = init_Queue.peek()) != null && (flushAll || now - head.queuedAtMs() >= threshold || now - head.queuedAtMs() >= 8000L)) {
               if (init_Queue.poll() != null) {
                  incomingQueued.updateAndGet(count -> Math.max(0, count - 1));
               }

               deliverIncoming(head.packet());
            }
         }

         if (!load_Queue.isEmpty()) {
            int delay = effectiveDelayMs();
            boolean flushAll = delay < 0 || !isRealOutgoing() || outgoingQueued.get() >= 8192;
            long threshold = flushAll ? 0L : delay;
            int budget = flushAll ? 2147483647 : 1024;

            PingSpoofScheduler.Inner_DataUtil head;
            while (budget-- > 0 && (head = load_Queue.peek()) != null && (flushAll || now - head.queuedAtMs() >= threshold || now - head.queuedAtMs() >= 8000L)) {
               if (load_Queue.poll() != null) {
                  outgoingQueued.updateAndGet(count -> Math.max(0, count - 1));
               }

               sendOutgoing(head.packet());
            }
         }
      }
   }

   private static void schedule(Packet<?> packet, int delayMs) {
      long wait = load_AtomicInteger.get() >= 256 ? 0L : Math.max(0L, Math.min(delayMs, 8000L));
      int epoch = scheduleEpoch;
      if (wait <= 0L) {
         deliver(packet, epoch);
      } else {
         load_AtomicInteger.incrementAndGet();

         try {
            scheduler().schedule(() -> {
               load_AtomicInteger.decrementAndGet();
               deliver(packet, epoch);
            }, wait, TimeUnit.MILLISECONDS);
         } catch (Throwable rejected) {
            load_AtomicInteger.decrementAndGet();
            deliver(packet, epoch);
         }
      }
   }

   private static void deliver(Packet<?> packet, int epoch) {
      if (epoch == scheduleEpoch) {
         ClientPacketListener connection = MINECRAFT == null ? null : MINECRAFT.getConnection();
         if (connection != null) {
            markOwnPacket(packet);

            try {
               connection.send(packet);
            } catch (Throwable t) {
               logFailure("keepalive", packet, t);
            } finally {
               consumeOwnPacket(packet);
            }
         }
      }
   }

   private static void deliverIncoming(Packet<?> packet) {
      ClientPacketListener connection = MINECRAFT == null ? null : MINECRAFT.getConnection();
      if (connection != null) {
         try {
            Packet<ClientGamePacketListener> gamePacket = (Packet<ClientGamePacketListener>)packet;
            gamePacket.handle(connection);
         } catch (RunningOnDifferentThreadException var3) {
         } catch (Throwable t) {
            logFailure("incoming", packet, t);
         }
      }
   }

   private static void sendOutgoing(Packet<?> packet) {
      ClientPacketListener connection = MINECRAFT == null ? null : MINECRAFT.getConnection();
      if (connection != null) {
         markOwnPacket(packet);

         try {
            connection.send(packet);
         } catch (Throwable t) {
            logFailure("outgoing", packet, t);
         } finally {
            consumeOwnPacket(packet);
         }
      }
   }

   private static void markOwnPacket(Packet<?> packet) {
      if (packet != null && ownPackets.add(packet)) {
         ownPacketCount.incrementAndGet();
      }
   }

   private static boolean consumeOwnPacket(Packet<?> packet) {
      if (ownPacketCount.get() == 0 || packet == null) {
         return false;
      }

      if (!ownPackets.remove(packet)) {
         return false;
      }

      ownPacketCount.updateAndGet(count -> Math.max(0, count - 1));
      return true;
   }

   private static void logFailure(String direction, Packet<?> packet, Throwable t) {
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

   private record Inner_DataUtil(Packet<?> packet, long queuedAtMs) {
   }

   private record RunOverride(int delayMs, boolean realIncoming, boolean load, long expiresAtMs, boolean read) {
      boolean isLive(long now) {
         return this.read || now - this.expiresAtMs < 0L;
      }
   }
}
