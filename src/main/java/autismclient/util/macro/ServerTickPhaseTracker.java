package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket.Pos;

public class ServerTickPhaseTracker {
   private static final long init_J = 50000000L;
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static final long load_J = 2000L;
   private static final int init_I = 40;
   private static final int load_I = 60;
   private static final long[] init_JArr = new long[60];
   private static int read_I = 0;
   private static int write_I = 0;
   private static volatile long read_J = 0L;
   private static volatile long write_J = 0L;
   private static volatile long update_J = -9223372036854775808L;
   private static volatile double init_D = 20.0;
   private static volatile long sync = 0L;
   private static volatile long flush = 0L;
   private static volatile long close = 0L;
   private static volatile int update_I = 0;
   private static final long open = 5000000L;

   public static void init(ClientboundSetTimePacket packet) {
      long now = System.nanoTime();
      if (read_J == 0L) {
         read_J = now;
      }

      long previousPacketTime = write_J;
      long previousGameTime = update_J;
      write_J = now;
      update_J = packet.gameTime();
      if (previousPacketTime > 0L && previousGameTime != -9223372036854775808L) {
         long gameTicks = packet.gameTime() - previousGameTime;
         long elapsedNanos = now - previousPacketTime;
         if (gameTicks > 0L && gameTicks <= 200L && elapsedNanos > 0L) {
            double sample = Math.max(0.0, Math.min(20.0, gameTicks * 1.0E9 / elapsedNanos));
            init_D = init_D <= 0.0 ? sample : init_D * 0.75 + sample * 0.25;
         }
      }

      int pingMs = init_I();
      long halfPingNanos = pingMs * 1000000L / 2L;
      long serverSendTime = now - halfPingNanos;
      init_JArr[read_I] = serverSendTime;
      read_I = (read_I + 1) % 60;
      if (write_I < 60) {
         write_I++;
      }

      read_V();
   }

   public static void init_V() {
      long now = System.nanoTime();
      if (now - close < 5000000L) {
         update_I++;
      } else {
         update_I = 1;
      }

      close = now;
   }

   public static void init(Packet<?> packet) {
      if (packet instanceof ClientboundSetTimePacket worldTimePacket) {
         init(worldTimePacket);
      } else if (packet instanceof Pos || packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundPlayerPositionPacket) {
         init_V();
      }
   }

   private static void read_V() {
      if (write_I >= 3) {
         long sumPhase = 0L;
         int validSamples = Math.min(write_I, 60);

         for (int i = 0; i < validSamples; i++) {
            long phase = init_JArr[i] % 50000000L;
            sumPhase += phase;
         }

         sync = sumPhase / validSamples;
         long mostRecent = init_JArr[(read_I - 1 + 60) % 60];
         long tickNumber = mostRecent / 50000000L;
         flush = tickNumber * 50000000L + sync;
         long now = System.nanoTime();
         if (flush > now) {
            flush -= 50000000L;
         }
      }
   }

   public static int init_I() {
      if (init_Minecraft.getConnection() != null && init_Minecraft.player != null) {
         PlayerInfo entry = init_Minecraft.getConnection().getPlayerInfo(init_Minecraft.player.getUUID());
         return entry == null ? 0 : entry.getLatency();
      } else {
         return 0;
      }
   }

   public static long init_J() {
      if (flush == 0L) {
         return System.nanoTime() + 50000000L;
      }

      long now = System.nanoTime();
      long elapsed = now - flush;
      long ticksElapsed = elapsed / 50000000L;
      return flush + (ticksElapsed + 1L) * 50000000L;
   }

   public static long init(int bufferMs, boolean ignorePing) {
      long nextTick = init_J();
      int pingMs = ignorePing ? 0 : init_I();
      long halfPingNanos = pingMs * 1000000L / 2L;
      long bufferNanos = bufferMs * 1000000L;
      long optimalTime = nextTick - bufferNanos - halfPingNanos;
      long now = System.nanoTime();

      while (optimalTime <= now) {
         optimalTime += 50000000L;
      }

      return optimalTime;
   }

   public static boolean init_Z() {
      if (read_J != 0L && write_I >= 40) {
         long elapsedMs = (System.nanoTime() - read_J) / 1000000L;
         return elapsedMs >= 2000L && flush > 0L;
      } else {
         return false;
      }
   }

   public static float init_F() {
      if (read_J == 0L) {
         return 0.0F;
      }

      long elapsedMs = (System.nanoTime() - read_J) / 1000000L;
      float timeProgress = Math.min(1.0F, (float)elapsedMs / 2000.0F);
      float sampleProgress = Math.min(1.0F, write_I / 40.0F);
      return Math.min(timeProgress, sampleProgress);
   }

   public static float load_F() {
      if (!init_Z()) {
         return 0.0F;
      }

      float sampleConfidence = Math.min(1.0F, write_I / 60.0F);
      long timeSinceLast = System.nanoTime() - write_J;
      float freshnessConfidence = timeSinceLast < 100000000L ? 1.0F : Math.max(0.0F, 1.0F - (float)timeSinceLast / 5.0E8F);
      return sampleConfidence * freshnessConfidence;
   }

   public static long load(int bufferMs, boolean ignorePing) {
      long optimalTime = init(bufferMs, ignorePing);
      return Math.max(0L, (optimalTime - System.nanoTime()) / 1000000L);
   }

   public static long load_J() {
      long nextTick = init_J();
      return Math.max(0L, (nextTick - System.nanoTime()) / 1000000L);
   }

   public static long read_J() {
      return write_J == 0L ? 0L : Math.max(0L, (System.nanoTime() - write_J) / 1000000L);
   }

   public static double init_D() {
      return Math.max(0.0, Math.min(20.0, init_D));
   }

   public static int load_I() {
      return write_I;
   }

   public static long write() {
      return read_J == 0L ? 0L : (System.nanoTime() - read_J) / 1000000L;
   }

   public static long update() {
      return sync / 1000000L;
   }

   public static void load_V() {
      write_I = 0;
      read_I = 0;
      read_J = 0L;
      write_J = 0L;
      update_J = -9223372036854775808L;
      init_D = 20.0;
      sync = 0L;
      flush = 0L;
      close = 0L;
      update_I = 0;
   }

   public static String init_Str() {
      return String.format("Ping: %dms | Samples: %d/%d | Phase: %dms | Confidence: %.0f%%", init_I(), write_I, 40, update(), load_F() * 100.0F);
   }
}
