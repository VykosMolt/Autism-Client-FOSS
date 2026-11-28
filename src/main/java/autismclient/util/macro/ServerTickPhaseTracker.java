package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Pos;

public class ServerTickPhaseTracker {
   private static final long TICK_NANOS = 50000000L;
   private static final Minecraft MC = Minecraft.getInstance();
   private static final long MIN_WARMUP_MS = 2000L;
   private static final int MIN_SAMPLES = 40;
   private static final int SAMPLE_WINDOW = 60;
   private static final long[] sendTimes = new long[60];
   private static int sampleCursor = 0;
   private static int sampleCount = 0;
   private static volatile long firstPacketNanos = 0L;
   private static volatile long lastPacketNanos = 0L;
   private static volatile long lastGameTime = -9223372036854775808L;
   private static volatile double estimatedTps = 20.0;
   private static volatile long phaseNanos = 0L;
   private static volatile long nextTickNanos = 0L;
   private static volatile long lastSampleNanos = 0L;
   private static volatile int burstPacketCount = 0;
   private static final long MIN_SAMPLE_GAP_NANOS = 5000000L;

   public static void onTimePacket(ClientboundSetTimePacket packet) {
      long now = System.nanoTime();
      if (firstPacketNanos == 0L) {
         firstPacketNanos = now;
      }

      long previousPacketTime = lastPacketNanos;
      long previousGameTime = lastGameTime;
      lastPacketNanos = now;
      lastGameTime = packet.gameTime();
      if (previousPacketTime > 0L && previousGameTime != -9223372036854775808L) {
         long gameTicks = packet.gameTime() - previousGameTime;
         long elapsedNanos = now - previousPacketTime;
         if (gameTicks > 0L && gameTicks <= 200L && elapsedNanos > 0L) {
            double sample = Math.max(0.0, Math.min(20.0, gameTicks * 1.0E9 / elapsedNanos));
            estimatedTps = estimatedTps <= 0.0 ? sample : estimatedTps * 0.75 + sample * 0.25;
         }
      }

      int pingMs = getLatencyMs();
      long halfPingNanos = pingMs * 1000000L / 2L;
      long serverSendTime = now - halfPingNanos;
      sendTimes[sampleCursor] = serverSendTime;
      sampleCursor = (sampleCursor + 1) % 60;
      if (sampleCount < 60) {
         sampleCount++;
      }

      recomputePhase();
   }

   public static void noteBurstPacket() {
      long now = System.nanoTime();
      if (now - lastSampleNanos < 5000000L) {
         burstPacketCount++;
      } else {
         burstPacketCount = 1;
      }

      lastSampleNanos = now;
   }

   public static void init(Packet<?> packet) {
      if (packet instanceof ClientboundSetTimePacket worldTimePacket) {
         onTimePacket(worldTimePacket);
      } else if (packet instanceof Pos || packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundPlayerPositionPacket) {
         noteBurstPacket();
      }
   }

   private static void recomputePhase() {
      if (sampleCount >= 3) {
         long sumPhase = 0L;
         int validSamples = Math.min(sampleCount, 60);

         for (int i = 0; i < validSamples; i++) {
            long phase = sendTimes[i] % 50000000L;
            sumPhase += phase;
         }

         phaseNanos = sumPhase / validSamples;
         long mostRecent = sendTimes[(sampleCursor - 1 + 60) % 60];
         long tickNumber = mostRecent / 50000000L;
         nextTickNanos = tickNumber * 50000000L + phaseNanos;
         long now = System.nanoTime();
         if (nextTickNanos > now) {
            nextTickNanos -= 50000000L;
         }
      }
   }

   public static int getLatencyMs() {
      if (MC.getConnection() != null && MC.player != null) {
         PlayerInfo entry = MC.getConnection().getPlayerInfo(MC.player.getUUID());
         return entry == null ? 0 : entry.getLatency();
      } else {
         return 0;
      }
   }

   public static long nextTickNanos() {
      if (nextTickNanos == 0L) {
         return System.nanoTime() + 50000000L;
      }

      long now = System.nanoTime();
      long elapsed = now - nextTickNanos;
      long ticksElapsed = elapsed / 50000000L;
      return nextTickNanos + (ticksElapsed + 1L) * 50000000L;
   }

   public static long init(int bufferMs, boolean ignorePing) {
      long nextTick = nextTickNanos();
      int pingMs = ignorePing ? 0 : getLatencyMs();
      long halfPingNanos = pingMs * 1000000L / 2L;
      long bufferNanos = bufferMs * 1000000L;
      long optimalTime = nextTick - bufferNanos - halfPingNanos;
      long now = System.nanoTime();

      while (optimalTime <= now) {
         optimalTime += 50000000L;
      }

      return optimalTime;
   }

   public static boolean isWarmedUp() {
      if (firstPacketNanos != 0L && sampleCount >= 40) {
         long elapsedMs = (System.nanoTime() - firstPacketNanos) / 1000000L;
         return elapsedMs >= 2000L && nextTickNanos > 0L;
      } else {
         return false;
      }
   }

   public static float getWarmupProgress() {
      if (firstPacketNanos == 0L) {
         return 0.0F;
      }

      long elapsedMs = (System.nanoTime() - firstPacketNanos) / 1000000L;
      float timeProgress = Math.min(1.0F, (float)elapsedMs / 2000.0F);
      float sampleProgress = Math.min(1.0F, sampleCount / 40.0F);
      return Math.min(timeProgress, sampleProgress);
   }

   public static float confidence() {
      if (!isWarmedUp()) {
         return 0.0F;
      }

      float sampleConfidence = Math.min(1.0F, sampleCount / 60.0F);
      long timeSinceLast = System.nanoTime() - lastPacketNanos;
      float freshnessConfidence = timeSinceLast < 100000000L ? 1.0F : Math.max(0.0F, 1.0F - (float)timeSinceLast / 5.0E8F);
      return sampleConfidence * freshnessConfidence;
   }

   public static long load(int bufferMs, boolean ignorePing) {
      long optimalTime = init(bufferMs, ignorePing);
      return Math.max(0L, (optimalTime - System.nanoTime()) / 1000000L);
   }

   public static long msUntilNextTick() {
      long nextTick = nextTickNanos();
      return Math.max(0L, (nextTick - System.nanoTime()) / 1000000L);
   }

   public static long msSinceLastPacket() {
      return lastPacketNanos == 0L ? 0L : Math.max(0L, (System.nanoTime() - lastPacketNanos) / 1000000L);
   }

   public static double getTps() {
      return Math.max(0.0, Math.min(20.0, estimatedTps));
   }

   public static int getSampleCount() {
      return sampleCount;
   }

   public static long write() {
      return firstPacketNanos == 0L ? 0L : (System.nanoTime() - firstPacketNanos) / 1000000L;
   }

   public static long phaseMs() {
      return phaseNanos / 1000000L;
   }

   public static void reset() {
      sampleCount = 0;
      sampleCursor = 0;
      firstPacketNanos = 0L;
      lastPacketNanos = 0L;
      lastGameTime = -9223372036854775808L;
      estimatedTps = 20.0;
      phaseNanos = 0L;
      nextTickNanos = 0L;
      lastSampleNanos = 0L;
      burstPacketCount = 0;
   }

   public static String debugSummary() {
      return String.format("Ping: %dms | Samples: %d/%d | Phase: %dms | Confidence: %.0f%%", getLatencyMs(), sampleCount, 40, phaseMs(), confidence() * 100.0F);
   }
}
