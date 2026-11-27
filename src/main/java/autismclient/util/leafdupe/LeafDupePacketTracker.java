package autismclient.util.leafdupe;

import autismclient.compat263.CompatSwingPacket;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.game.ClientboundBlockChangedAckPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class LeafDupePacketTracker {
   private static final long init_J = 2000L;
   private static BlockPos init_BlockPos;
   private static BlockPos load_BlockPos;
   private static boolean init_Z;
   private static BlockPos read_BlockPos;
   private static BlockPos write;
   private static double init_D;
   private static long load_J;
   private static int init_I = -1;
   private static long read_J;
   private static final Map<Packet<?>, LeafDupePacketTracker.Inner_StreamUtil> load_Map = new WeakHashMap<>();
   static final Map<Long, LeafDupePacketTracker.Inner_DataUtil> init_Map = new HashMap<>();

   private LeafDupePacketTracker() {
   }

   public static synchronized BlockPos init_BlockPos() {
      return read_BlockPos;
   }

   public static synchronized int trackedSlot() {
      return init_I;
   }

   public static synchronized void init(int slot) {
      if (slot >= 0 && slot < 9) {
         init_I = slot;
      }
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil init(ServerboundPlayerActionPacket packet, Object scope, int slot, double damage) {
      if (scope != null
         && slot >= 0
         && slot <= 8
         && packet.getAction() == Action.START_DESTROY_BLOCK
         && Double.isFinite(damage)
         && !(damage <= 0.0)
         && !(damage >= 1.0)) {
         LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, damage, null);
         load_Map.put(packet, receipt);
         return receipt;
      } else {
         throw new IllegalArgumentException("Unsafe automatic start");
      }
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil init(ServerboundSetCarriedItemPacket packet, Object scope) {
      return init(packet, scope, null);
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil init(ServerboundSetCarriedItemPacket packet, Object scope, LeafDupePacketTracker.Inner_StreamUtil previous) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, packet.getSlot(), 0.0 / 0.0, previous);
      load_Map.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil init(ServerboundUseItemOnPacket packet, Object scope, int slot, LeafDupePacketTracker.Inner_StreamUtil previous) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, 0.0 / 0.0, previous);
      load_Map.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil init(ServerboundPlayerActionPacket packet, Object scope, int slot, LeafDupePacketTracker.Inner_StreamUtil previous) {
      if (packet.getAction() != Action.STOP_DESTROY_BLOCK) {
         throw new IllegalArgumentException("Not a STOP");
      }

      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, 0.0 / 0.0, previous);
      load_Map.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil receiptFor(Packet<?> packet) {
      return load_Map.get(packet);
   }

   public static synchronized boolean init(Packet<?> packet, boolean scopeValid) {
      return init(packet, scopeValid, System.nanoTime());
   }

   static synchronized boolean init(Packet<?> packet, boolean scopeValid, long now) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = load_Map.get(packet);
      if (receipt == null) {
         return true;
      }

      if (receipt.init_Z) {
         return false;
      }

      if (scopeValid
         && !receipt.load_Z
         && !receipt.init(now)
         && (receipt.init_DataUtil_StreamUti == null || receipt.init_DataUtil_StreamUti.init_Z && !receipt.init_DataUtil_StreamUti.load_Z)
         && (packet instanceof ServerboundSetCarriedItemPacket || init_I == receipt.init_I)) {
         if (packet instanceof ServerboundSetCarriedItemPacket && init_I == receipt.init_I) {
            receipt.load_Z = true;
            return false;
         } else {
            return true;
         }
      } else {
         receipt.load_Z = true;
         return false;
      }
   }

   public static synchronized boolean init(LeafDupePacketTracker.Inner_StreamUtil receipt, BlockPos target) {
      return receipt != null && receipt.init_Z && !receipt.load_Z && receipt.load_J == read_J && target.equals(read_BlockPos);
   }

   public static synchronized boolean init(BlockPos pos) {
      return !pos.equals(load_BlockPos) && !init_Z && !pos.equals(init_BlockPos);
   }

   public static synchronized long waitMs() {
      if (write == null) {
         return 0L;
      } else {
         return init_D <= 0.0 ? 9223372036854775807L : init(System.currentTimeMillis(), init_D, load_J);
      }
   }

   static long init(long now, double damage, long lastTime) {
      double delay = Double.isInfinite(damage) ? 0.0 : Math.ceil(1.0 / damage) * 50.0;
      return Math.max(0L, (long)Math.ceil(delay) - (now - lastTime));
   }

   public static synchronized boolean load(BlockPos pos) {
      LeafDupePacketTracker.Inner_DataUtil prediction = init_Map.get(pos.asLong());
      if (prediction == null) {
         return false;
      } else if (System.currentTimeMillis() - prediction.init_J > 2000L) {
         init_Map.remove(pos.asLong());
         return false;
      } else {
         return true;
      }
   }

   public static void init(Connection connection, Packet<?> packet) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         ClientPacketListener listener = mc.getConnection();
         if (listener != null && listener.getConnection() == connection) {
            init_V(packet);
         }
      }
   }

   static synchronized void init_V(Packet<?> packet) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = load_Map.get(packet);
      if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         init(carried.getSlot());
      } else if (packet instanceof ServerboundPlayerActionPacket action) {
         init(action, receipt == null ? null : receipt.init_D);
      } else if (packet instanceof ServerboundMovePlayerPacket || packet instanceof CompatSwingPacket) {
         write();
      } else if (packet instanceof ServerboundPongPacket pong) {
         load(pong.getId());
      }

      if (receipt != null) {
         receipt.load_J = read_J;
         receipt.init_Z = true;
      }
   }

   static synchronized void init(ServerboundPlayerActionPacket packet) {
      init(packet, null);
   }

   private static void init(ServerboundPlayerActionPacket packet, Double preparedDamage) {
      BlockPos pos = packet.getPos().immutable();
      long now = System.currentTimeMillis();
      switch (packet.getAction()) {
         case START_DESTROY_BLOCK:
            read_J++;
            double damage = preparedDamage == null ? init(pos, false) : preparedDamage;
            init_Z = damage >= 1.0;
            load_BlockPos = null;
            init_BlockPos = pos;
            read_BlockPos = damage < 1.0 ? pos : null;
            load_J = now - (write == null ? 50L : 0L);
            write = pos;
            init_D = damage;
            break;
         case ABORT_DESTROY_BLOCK:
            load_BlockPos = pos;
            init_BlockPos = null;
            break;
         case STOP_DESTROY_BLOCK:
            if (write != null) {
               load_J = now;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || init_Map.containsKey(pos.asLong())) {
               return;
            }

            BlockState state = mc.level.getBlockState(pos);
            boolean predictable = !state.isAir() && state.getDestroySpeed(mc.level, pos) != -1.0F && state.getFluidState().isEmpty();
            if (predictable) {
               init_Map.put(pos.asLong(), new LeafDupePacketTracker.Inner_DataUtil(packet.getSequence(), now));
            }
      }
   }

   private static synchronized void write() {
      if (write != null) {
         init_D = Math.max(init_D, init(write, true));
      }
   }

   static synchronized void load(int id) {
      init_Map.values().removeIf(prediction -> prediction.init_Integer != null && prediction.init_Integer == id);
   }

   public static void load(Packet<?> packet) {
      if (packet instanceof ClientboundBlockChangedAckPacket ack) {
         read(ack.sequence());
      } else if (packet instanceof ClientboundPingPacket ping) {
         write(ping.getId());
      }
   }

   static synchronized void read(int sequence) {
      for (LeafDupePacketTracker.Inner_DataUtil prediction : init_Map.values()) {
         if (prediction.init_I <= sequence) {
            prediction.init_Z = true;
         }
      }
   }

   static synchronized void write(int id) {
      for (LeafDupePacketTracker.Inner_DataUtil prediction : init_Map.values()) {
         if (prediction.init_Z && prediction.init_Integer == null) {
            prediction.init_Integer = id;
         }
      }
   }

   public static synchronized void reset() {
      load();
      init_I = -1;
   }

   public static synchronized void load() {
      for (LeafDupePacketTracker.Inner_StreamUtil receipt : load_Map.values()) {
         receipt.markConsumed();
      }

      read_J++;
      init_BlockPos = null;
      load_BlockPos = null;
      init_Z = false;
      read_BlockPos = null;
      write = null;
      init_D = 0.0;
      load_J = 0L;
      init_Map.clear();
   }

   private static double init(BlockPos pos, boolean predictionAware) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null) {
         if (predictionAware && init_Map.containsKey(pos.asLong())) {
            return 1.0 / 0.0;
         }

         BlockState state = mc.level.getBlockState(pos);
         return state.isAir() ? 1.0 / 0.0 : state.getDestroyProgress(mc.player, mc.level, pos);
      } else {
         return 0.0;
      }
   }

   public static synchronized void read() {
      long now = System.currentTimeMillis();
      init_Map.values().removeIf(prediction -> now - prediction.init_J > 2000L);
   }

   public static final class Inner_CachePool {
      private final WeakReference<Object> init_WeakReference;
      private final Vec3 init_Vec3;
      private final Vec3 load;

      public Inner_CachePool(Object owner, Vec3 eye, Vec3 serverEye) {
         this.init_WeakReference = new WeakReference<>(owner);
         this.init_Vec3 = eye;
         this.load = serverEye;
      }

      public Object init() {
         return this.init_WeakReference.get();
      }

      public boolean init(Object currentOwner, Vec3 currentEye, Vec3 currentServerEye) {
         return currentOwner != null
            && currentOwner == this.init_WeakReference.get()
            && this.init_Vec3.distanceToSqr(currentEye) < 1.0E-8
            && this.load.distanceToSqr(currentServerEye) < 1.0E-8;
      }
   }

   static final class Inner_DataUtil {
      final int init_I;
      final long init_J;
      boolean init_Z;
      Integer init_Integer;

      Inner_DataUtil(int sequence, long sentAt) {
         this.init_I = sequence;
         this.init_J = sentAt;
      }
   }

   public static final class Inner_StreamUtil {
      private final Object init_Obj;
      private final int init_I;
      private final double init_D;
      private final LeafDupePacketTracker.Inner_StreamUtil init_DataUtil_StreamUti;
      private final long init_J = System.nanoTime();
      private volatile boolean init_Z;
      private volatile boolean load_Z;
      private long load_J;

      private Inner_StreamUtil(Object scope, int slot, double damage, LeafDupePacketTracker.Inner_StreamUtil previous) {
         this.init_Obj = scope;
         this.init_I = slot;
         this.init_D = damage;
         this.init_DataUtil_StreamUti = previous;
      }

      public Object proof() {
         return this.init_Obj;
      }

      public int slot() {
         return this.init_I;
      }

      public long init_J() {
         return this.init_J;
      }

      public boolean init(long now) {
         return now - this.init_J > 250000000L;
      }

      public boolean isSent() {
         return this.init_Z;
      }

      public boolean load() {
         return this.load_Z;
      }

      public void markConsumed() {
         this.load_Z = true;
      }
   }
}
