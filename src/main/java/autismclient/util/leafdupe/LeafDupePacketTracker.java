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
   private static boolean fullyBroken;
   private static BlockPos currentTarget;
   private static BlockPos breakingPos;
   private static double breakDamage;
   private static long breakStartMs;
   private static int lastCarriedSlot = -1;
   private static long breakGeneration;
   private static final Map<Packet<?>, LeafDupePacketTracker.Inner_StreamUtil> receipts = new WeakHashMap<>();
   static final Map<Long, LeafDupePacketTracker.Inner_DataUtil> predictions = new HashMap<>();

   private LeafDupePacketTracker() {
   }

   public static synchronized BlockPos currentTarget() {
      return currentTarget;
   }

   public static synchronized int trackedSlot() {
      return lastCarriedSlot;
   }

   public static synchronized void setCarriedSlot(int slot) {
      if (slot >= 0 && slot < 9) {
         lastCarriedSlot = slot;
      }
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil trackBreak(ServerboundPlayerActionPacket packet, Object scope, int slot, double damage) {
      if (scope != null
         && slot >= 0
         && slot <= 8
         && packet.getAction() == Action.START_DESTROY_BLOCK
         && Double.isFinite(damage)
         && !(damage <= 0.0)
         && !(damage >= 1.0)) {
         LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, damage, null);
         receipts.put(packet, receipt);
         return receipt;
      } else {
         throw new IllegalArgumentException("Unsafe automatic start");
      }
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil recordSlotChange(ServerboundSetCarriedItemPacket packet, Object scope) {
      return trackSlot(packet, scope, null);
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil trackSlot(ServerboundSetCarriedItemPacket packet, Object scope, LeafDupePacketTracker.Inner_StreamUtil previous) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, packet.getSlot(), 0.0 / 0.0, previous);
      receipts.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil trackUse(ServerboundUseItemOnPacket packet, Object scope, int slot, LeafDupePacketTracker.Inner_StreamUtil previous) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, 0.0 / 0.0, previous);
      receipts.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil trackCycle(ServerboundPlayerActionPacket packet, Object scope, int slot, LeafDupePacketTracker.Inner_StreamUtil previous) {
      if (packet.getAction() != Action.STOP_DESTROY_BLOCK) {
         throw new IllegalArgumentException("Not a STOP");
      }

      LeafDupePacketTracker.Inner_StreamUtil receipt = new LeafDupePacketTracker.Inner_StreamUtil(scope, slot, 0.0 / 0.0, previous);
      receipts.put(packet, receipt);
      return receipt;
   }

   public static synchronized LeafDupePacketTracker.Inner_StreamUtil receiptFor(Packet<?> packet) {
      return receipts.get(packet);
   }

   public static synchronized boolean init(Packet<?> packet, boolean scopeValid) {
      return allowsSend(packet, scopeValid, System.nanoTime());
   }

   static synchronized boolean allowsSend(Packet<?> packet, boolean scopeValid, long now) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = receipts.get(packet);
      if (receipt == null) {
         return true;
      }

      if (receipt.init_Z) {
         return false;
      }

      if (scopeValid
         && !receipt.load_Z
         && !receipt.isStale(now)
         && (receipt.previous == null || receipt.previous.init_Z && !receipt.previous.load_Z)
         && (packet instanceof ServerboundSetCarriedItemPacket || lastCarriedSlot == receipt.slot)) {
         if (packet instanceof ServerboundSetCarriedItemPacket && lastCarriedSlot == receipt.slot) {
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
      return receipt != null && receipt.init_Z && !receipt.load_Z && receipt.generation == breakGeneration && target.equals(currentTarget);
   }

   public static synchronized boolean init(BlockPos pos) {
      return !pos.equals(load_BlockPos) && !fullyBroken && !pos.equals(init_BlockPos);
   }

   public static synchronized long waitMs() {
      if (breakingPos == null) {
         return 0L;
      } else {
         return breakDamage <= 0.0 ? 9223372036854775807L : nextBreakTime(System.currentTimeMillis(), breakDamage, breakStartMs);
      }
   }

   static long nextBreakTime(long now, double damage, long lastTime) {
      double delay = Double.isInfinite(damage) ? 0.0 : Math.ceil(1.0 / damage) * 50.0;
      return Math.max(0L, (long)Math.ceil(delay) - (now - lastTime));
   }

   public static synchronized boolean load(BlockPos pos) {
      LeafDupePacketTracker.Inner_DataUtil prediction = predictions.get(pos.asLong());
      if (prediction == null) {
         return false;
      } else if (System.currentTimeMillis() - prediction.sentAtMs > 2000L) {
         predictions.remove(pos.asLong());
         return false;
      } else {
         return true;
      }
   }

   public static void onSend(Connection connection, Packet<?> packet) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null) {
         ClientPacketListener listener = mc.getConnection();
         if (listener != null && listener.getConnection() == connection) {
            init_V(packet);
         }
      }
   }

   static synchronized void init_V(Packet<?> packet) {
      LeafDupePacketTracker.Inner_StreamUtil receipt = receipts.get(packet);
      if (packet instanceof ServerboundSetCarriedItemPacket carried) {
         setCarriedSlot(carried.getSlot());
      } else if (packet instanceof ServerboundPlayerActionPacket action) {
         recordActionWithDamage(action, receipt == null ? null : receipt.damage);
      } else if (packet instanceof ServerboundMovePlayerPacket || packet instanceof CompatSwingPacket) {
         refreshBreakDamage();
      } else if (packet instanceof ServerboundPongPacket pong) {
         dropPredictionsFor(pong.getId());
      }

      if (receipt != null) {
         receipt.generation = breakGeneration;
         receipt.init_Z = true;
      }
   }

   static synchronized void recordAction(ServerboundPlayerActionPacket packet) {
      recordActionWithDamage(packet, null);
   }

   private static void recordActionWithDamage(ServerboundPlayerActionPacket packet, Double preparedDamage) {
      BlockPos pos = packet.getPos().immutable();
      long now = System.currentTimeMillis();
      switch (packet.getAction()) {
         case START_DESTROY_BLOCK:
            breakGeneration++;
            double damage = preparedDamage == null ? breakDamageAt(pos, false) : preparedDamage;
            fullyBroken = damage >= 1.0;
            load_BlockPos = null;
            init_BlockPos = pos;
            currentTarget = damage < 1.0 ? pos : null;
            breakStartMs = now - (breakingPos == null ? 50L : 0L);
            breakingPos = pos;
            breakDamage = damage;
            break;
         case ABORT_DESTROY_BLOCK:
            load_BlockPos = pos;
            init_BlockPos = null;
            break;
         case STOP_DESTROY_BLOCK:
            if (breakingPos != null) {
               breakStartMs = now;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.level == null || predictions.containsKey(pos.asLong())) {
               return;
            }

            BlockState state = mc.level.getBlockState(pos);
            boolean predictable = !state.isAir() && state.getDestroySpeed(mc.level, pos) != -1.0F && state.getFluidState().isEmpty();
            if (predictable) {
               predictions.put(pos.asLong(), new LeafDupePacketTracker.Inner_DataUtil(packet.getSequence(), now));
            }
      }
   }

   private static synchronized void refreshBreakDamage() {
      if (breakingPos != null) {
         breakDamage = Math.max(breakDamage, breakDamageAt(breakingPos, true));
      }
   }

   static synchronized void dropPredictionsFor(int id) {
      predictions.values().removeIf(prediction -> prediction.entityId != null && prediction.entityId == id);
   }

   public static void onReceive(Packet<?> packet) {
      if (packet instanceof ClientboundBlockChangedAckPacket ack) {
         ackSequence(ack.sequence());
      } else if (packet instanceof ClientboundPingPacket ping) {
         bindAckedEntity(ping.getId());
      }
   }

   static synchronized void ackSequence(int sequence) {
      for (LeafDupePacketTracker.Inner_DataUtil prediction : predictions.values()) {
         if (prediction.sequence <= sequence) {
            prediction.init_Z = true;
         }
      }
   }

   static synchronized void bindAckedEntity(int id) {
      for (LeafDupePacketTracker.Inner_DataUtil prediction : predictions.values()) {
         if (prediction.init_Z && prediction.entityId == null) {
            prediction.entityId = id;
         }
      }
   }

   public static synchronized void reset() {
      resetCycle();
      lastCarriedSlot = -1;
   }

   public static synchronized void resetCycle() {
      for (LeafDupePacketTracker.Inner_StreamUtil receipt : receipts.values()) {
         receipt.markConsumed();
      }

      breakGeneration++;
      init_BlockPos = null;
      load_BlockPos = null;
      fullyBroken = false;
      currentTarget = null;
      breakingPos = null;
      breakDamage = 0.0;
      breakStartMs = 0L;
      predictions.clear();
   }

   private static double breakDamageAt(BlockPos pos, boolean predictionAware) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.player != null && mc.level != null) {
         if (predictionAware && predictions.containsKey(pos.asLong())) {
            return 1.0 / 0.0;
         }

         BlockState state = mc.level.getBlockState(pos);
         return state.isAir() ? 1.0 / 0.0 : state.getDestroyProgress(mc.player, mc.level, pos);
      } else {
         return 0.0;
      }
   }

   public static synchronized void expirePredictions() {
      long now = System.currentTimeMillis();
      predictions.values().removeIf(prediction -> now - prediction.sentAtMs > 2000L);
   }

   public static final class Inner_CachePool {
      private final WeakReference<Object> owner;
      private final Vec3 eye;
      private final Vec3 serverEye;

      public Inner_CachePool(Object owner, Vec3 eye, Vec3 serverEye) {
         this.owner = new WeakReference<>(owner);
         this.eye = eye;
         this.serverEye = serverEye;
      }

      public Object init() {
         return this.owner.get();
      }

      public boolean init(Object currentOwner, Vec3 currentEye, Vec3 currentServerEye) {
         return currentOwner != null
            && currentOwner == this.owner.get()
            && this.eye.distanceToSqr(currentEye) < 1.0E-8
            && this.serverEye.distanceToSqr(currentServerEye) < 1.0E-8;
      }
   }

   static final class Inner_DataUtil {
      final int sequence;
      final long sentAtMs;
      boolean init_Z;
      Integer entityId;

      Inner_DataUtil(int sequence, long sentAt) {
         this.sequence = sequence;
         this.sentAtMs = sentAt;
      }
   }

   public static final class Inner_StreamUtil {
      private final Object scope;
      private final int slot;
      private final double damage;
      private final LeafDupePacketTracker.Inner_StreamUtil previous;
      private final long createdNanos = System.nanoTime();
      private volatile boolean init_Z;
      private volatile boolean load_Z;
      private long generation;

      private Inner_StreamUtil(Object scope, int slot, double damage, LeafDupePacketTracker.Inner_StreamUtil previous) {
         this.scope = scope;
         this.slot = slot;
         this.damage = damage;
         this.previous = previous;
      }

      public Object proof() {
         return this.scope;
      }

      public int slot() {
         return this.slot;
      }

      public long createdNanos() {
         return this.createdNanos;
      }

      public boolean isStale(long now) {
         return now - this.createdNanos > 250000000L;
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
