package autismclient.modules;

import autismclient.AutismClientAddon;
import autismclient.compat263.CompatSwingPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStartConfigurationPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundChatCommandSignedPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPunchPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

public final class HashUtil {
   private static final Minecraft init_Minecraft = Minecraft.getInstance();
   private static volatile boolean init_Z;
   private static volatile boolean load_Z;
   private static volatile boolean read_Z;
   private static volatile int init_I = 100;
   private static volatile boolean write_Z = true;
   private static volatile boolean update = true;
   private static volatile boolean sync;
   private static volatile boolean flush = true;
   private static volatile double init_D;
   private static volatile double load_D;
   private static volatile double read_D;
   private static volatile float init_F;
   private static volatile float load_F;
   private static volatile float read_F;
   private static volatile float write_F;
   private static PacketUtil init_PacketUtil;
   private static boolean close;
   private static final Queue<Packet<?>> init_Queue = new ConcurrentLinkedQueue<>();
   private static final Queue<Packet<?>> load_Queue = new ConcurrentLinkedQueue<>();
   private static final Set<Packet<?>> init_Set = Collections.synchronizedSet(Collections.newSetFromMap(new IdentityHashMap<>()));
   private static final AtomicInteger init_AtomicInteger = new AtomicInteger();
   private static volatile boolean open;
   private static int load_I;
   private static final List<HashUtil.Inner_StreamUtil> init_List = new CopyOnWriteArrayList<>();
   private static volatile boolean poll;
   private static volatile boolean push;
   private static volatile long init_J;
   private static final long load_J = 5000L;

   private HashUtil() {
   }

   public static void init(HashUtil.Inner_StreamUtil policy) {
      if (policy != null && !init_List.contains(policy)) {
         init_List.add(policy);
      }
   }

   public static void load(HashUtil.Inner_StreamUtil policy) {
      if (policy != null) {
         init_List.remove(policy);
      }
   }

   public static void init(boolean incoming, boolean outgoing) {
      if (incoming) {
         poll = true;
      }

      if (outgoing) {
         push = true;
      }
   }

   public static List<Vec3> init_List() {
      List<Vec3> positions = new ArrayList<>();

      for (Packet<?> packet : load_Queue) {
         if (packet instanceof ServerboundMovePlayerPacket move && move.hasPosition()) {
            positions.add(new Vec3(move.getX(0.0), move.getY(0.0), move.getZ(0.0)));
         }
      }

      return positions;
   }

   private static HashUtil.Inner_DataUtil init(Packet<?> packet, boolean incoming) {
      HashUtil.Inner_DataUtil merged = HashUtil.Inner_DataUtil.init_HashUtil_DataUtil;

      for (HashUtil.Inner_StreamUtil policy : init_List) {
         HashUtil.Inner_DataUtil hold;
         try {
            hold = policy.classify(packet, incoming);
         } catch (Throwable t) {
            continue;
         }

         if (hold != null && hold.init_I > merged.init_I) {
            merged = hold;
         }
      }

      return merged;
   }

   public static void load(boolean incoming, boolean outgoing) {
      boolean wasIncoming = init_Z;
      boolean wasOutgoing = load_Z;
      init_Z = incoming;
      load_Z = outgoing;
      if (wasIncoming && !incoming && !init_Queue.isEmpty() && (init_List.isEmpty() || init(null, true) != HashUtil.Inner_DataUtil.read)) {
         flush();
      }

      if (wasOutgoing && !outgoing && !load_Queue.isEmpty() && (init_List.isEmpty() || init(null, false) != HashUtil.Inner_DataUtil.read)) {
         close();
      }
   }

   public static boolean auditHoldingMovement() {
      return load_Z && write_Z;
   }

   public static void init(boolean enabled, int ticks) {
      read_Z = enabled;
      init_I = Math.max(1, ticks);
   }

   public static void read(boolean movement, boolean actions) {
      boolean wasMovement = write_Z;
      boolean wasActions = update;
      write_Z = movement;
      update = actions;
      if (((wasMovement && !movement) || (wasActions && !actions))
         && !load_Queue.isEmpty()
         && (init_List.isEmpty() || init(null, false) != HashUtil.Inner_DataUtil.read)) {
         close();
      }
   }

   public static void init(boolean show) {
      flush = show;
   }

   public static boolean init_Z() {
      return init_Z || load_Z;
   }

   public static boolean load_Z() {
      return init(load_Z, write_Z, update);
   }

   static boolean init(boolean outgoing, boolean movement, boolean actions) {
      return outgoing && actions && !movement;
   }

   public static int init_I() {
      return init_Queue.size() + load_Queue.size();
   }

   public static int load_I() {
      return read_Z && init_I() > 0 ? Math.max(0, init_I - load_I) : -1;
   }

   public static void init_V() {
      LocalPlayer player = init_Minecraft == null ? null : init_Minecraft.player;
      if (player == null) {
         sync = false;
      } else {
         init_D = player.getX();
         load_D = player.getY();
         read_D = player.getZ();
         init_F = player.getYRot();
         load_F = player.getXRot();
         read_F = player.yHeadRot;
         write_F = player.yBodyRot;
         sync = true;
         close = true;
      }
   }

   private static void open() {
      boolean want = load_Z && write_Z && flush && sync && init_Minecraft != null && init_Minecraft.player != null && init_Minecraft.level != null;
      if (!want) {
         push();
      } else if (init_PacketUtil == null || init_PacketUtil.isRemoved() || init_PacketUtil.level() != init_Minecraft.level) {
         poll();
      } else if (close) {
         init(init_PacketUtil);
         close = false;
      }
   }

   private static void poll() {
      push();
      LocalPlayer player = init_Minecraft == null ? null : init_Minecraft.player;
      ClientLevel level = init_Minecraft == null ? null : init_Minecraft.level;
      if (player != null && level != null) {
         try {
            PacketUtil fake = new PacketUtil(level, player);
            init(fake);
            level.addEntity(fake);
            init_PacketUtil = fake;
            close = false;
         } catch (Throwable t) {
            init_PacketUtil = null;
            AutismClientAddon.LOG.warn("[Autism] Blink clone spawn failed", t);
         }
      }
   }

   private static void init(PacketUtil fake) {
      fake.snapTo(init_D, load_D, read_D, init_F, load_F);
      fake.init(read_F, write_F);
   }

   private static void push() {
      if (init_PacketUtil != null) {
         try {
            init_PacketUtil.discard();
         } catch (Throwable var1) {
         }

         init_PacketUtil = null;
      }
   }

   private static void auditDiscardClone() {
      if (init_Minecraft == null || init_Minecraft.isSameThread()) {
         push();
      } else {
         init_Minecraft.execute(HashUtil::push);
      }
   }

   public static void load_V() {
      init_Z = false;
      load_Z = false;
      read_Z = false;
      sync();
      load_I = 0;
      sync = false;
      auditDiscardClone();
   }

   public static void auditDisconnect() {
      init_Z = false;
      load_Z = false;
      read_Z = false;
      read();
      auditDiscardClone();
   }

   public static void read() {
      init_Queue.clear();
      load_Queue.clear();
      init_Set.clear();
      init_AtomicInteger.set(0);
      open = false;
      poll = false;
      push = false;
      load_I = 0;
      sync = false;
   }

   public static boolean init_Z(Packet<?> packet) {
      if (auditIsDisconnect(packet)) {
         auditDisconnect();
         return false;
      } else if (auditIsStartConfiguration(packet)) {
         read();
         auditDiscardClone();
         return false;
      } else if (!init_List.isEmpty() && init(packet, true) == HashUtil.Inner_DataUtil.read) {
         if (sync(packet) || flush(packet)) {
            return false;
         } else if (close(packet)) {
            poll = true;
            return false;
         } else {
            init_Queue.add(packet);
            return true;
         }
      } else if (!init_Z || init_Minecraft == null || init_Minecraft.player == null) {
         return false;
      } else if (sync(packet) || flush(packet)) {
         return false;
      } else if (close(packet)) {
         init_Queue.add(packet);
         open = true;
         return true;
      } else {
         init_Queue.add(packet);
         return true;
      }
   }

   public static boolean load(Packet<?> packet) {
      if (update(packet)) {
         return false;
      }

      if (!init_List.isEmpty() && init(packet, false) == HashUtil.Inner_DataUtil.read) {
         if (!open(packet) && !poll(packet)) {
            load_Queue.add(packet);
            return true;
         } else {
            return false;
         }
      } else {
         if (!load_Z || init_Minecraft == null || init_Minecraft.player == null) {
            return false;
         }

         if (open(packet) || poll(packet)) {
            return false;
         }

         if (!read(packet)) {
            return false;
         }

         load_Queue.add(packet);
         return true;
      }
   }

   private static boolean read(Packet<?> packet) {
      return write_Z && packet instanceof ServerboundMovePlayerPacket ? true : update && write(packet);
   }

   private static boolean write(Packet<?> packet) {
      return packet instanceof ServerboundPlayerActionPacket
         || packet instanceof ServerboundUseItemOnPacket
         || packet instanceof ServerboundUseItemPacket
         || packet instanceof ServerboundInteractPacket
         || packet instanceof ServerboundAttackPacket
         || packet instanceof ServerboundPunchPacket
         || packet instanceof CompatSwingPacket;
   }

   public static void write() {
      if ((!init_List.isEmpty() || poll || push) && init_Minecraft != null && init_Minecraft.getConnection() != null) {
         if (!init_List.isEmpty()) {
            if (!init_Z && init(null, true) == HashUtil.Inner_DataUtil.init_HashUtil_DataUtil) {
               poll = true;
            }

            if (!load_Z && init(null, false) == HashUtil.Inner_DataUtil.init_HashUtil_DataUtil) {
               push = true;
            }
         }

         if (poll) {
            poll = false;
            flush();
         }

         if (push) {
            push = false;
            if (!load_Z) {
               close();
            }
         }
      }
   }

   public static void update() {
      if (!init_Z && !load_Z && init_Queue.isEmpty() && load_Queue.isEmpty() && init_AtomicInteger.get() == 0 && !open && init_PacketUtil == null) {
         load_I = 0;
      } else if (init_Minecraft != null && init_Minecraft.getConnection() != null) {
         if (open) {
            open = false;
            flush();
         }

         if (init_I() > 0) {
            load_I++;
            if (read_Z && load_I >= init_I) {
               sync();
               load_I = 0;
               init_V();
            }
         } else {
            load_I = 0;
         }

         open();
      } else {
         if (init_I() > 0 || init_AtomicInteger.get() > 0) {
            read();
         }

         push();
      }
   }

   public static void sync() {
      flush();
      close();
   }

   public static void flush() {
      ClientPacketListener connection = init_Minecraft == null ? null : init_Minecraft.getConnection();

      Packet<?> packet;
      while ((packet = init_Queue.poll()) != null) {
         if (connection != null) {
            init(connection, packet);
         }
      }
   }

   public static void close() {
      ClientPacketListener connection = init_Minecraft == null ? null : init_Minecraft.getConnection();

      Packet<?> packet;
      while ((packet = load_Queue.poll()) != null) {
         if (connection != null) {
            init_V(packet);

            try {
               connection.send(packet);
            } catch (Throwable t) {
               init("outgoing", packet, t);
            } finally {
               update(packet);
            }
         }
      }
   }

   private static void init_V(Packet<?> packet) {
      if (packet != null && init_Set.add(packet)) {
         init_AtomicInteger.incrementAndGet();
      }
   }

   private static boolean update(Packet<?> packet) {
      if (init_AtomicInteger.get() == 0 || packet == null) {
         return false;
      }

      if (!init_Set.remove(packet)) {
         return false;
      }

      init_AtomicInteger.updateAndGet(count -> Math.max(0, count - 1));
      return true;
   }

   private static void init(ClientPacketListener connection, Packet<?> packet) {
      try {
         Packet<ClientGamePacketListener> gamePacket = (Packet<ClientGamePacketListener>)packet;
         gamePacket.handle(connection);
      } catch (RunningOnDifferentThreadException var3) {
      } catch (Throwable t) {
         init("incoming", packet, t);
      }
   }

   private static void init(String direction, Packet<?> packet, Throwable t) {
      if (!(t instanceof RunningOnDifferentThreadException) && !(t.getCause() instanceof RunningOnDifferentThreadException)) {
         long now = System.currentTimeMillis();
         if (now - init_J >= 5000L) {
            init_J = now;
            AutismClientAddon.LOG.warn("[Autism] Blink re-dispatch ({}) failed for {}", direction, packet.getClass().getSimpleName(), t);
         }
      }
   }

   private static boolean sync(Packet<?> packet) {
      return packet instanceof ClientboundSystemChatPacket
         || packet instanceof ClientboundDisguisedChatPacket
         || packet instanceof ClientboundSoundPacket sound && sound.getSound().value() == SoundEvents.PLAYER_HURT;
   }

   private static boolean flush(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (flush(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundKeepAlivePacket || packet instanceof ClientboundPingPacket;
   }

   private static boolean auditIsStartConfiguration(Packet<?> packet) {
      if (packet instanceof ClientboundStartConfigurationPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundStartConfigurationPacket) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean auditIsDisconnect(Packet<?> packet) {
      if (packet instanceof ClientboundDisconnectPacket) {
         return true;
      }

      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (inner instanceof ClientboundDisconnectPacket) {
               return true;
            }
         }
      }

      return false;
   }

   private static boolean close(Packet<?> packet) {
      if (packet instanceof ClientboundBundlePacket bundle) {
         for (Packet<?> inner : bundle.subPackets()) {
            if (close(inner)) {
               return true;
            }
         }

         return false;
      }

      return packet instanceof ClientboundPlayerPositionPacket
         || packet instanceof ClientboundRespawnPacket
         || packet instanceof ClientboundLoginPacket
         || packet instanceof ClientboundDisconnectPacket
         || packet instanceof ClientboundSetHealthPacket health && health.getHealth() <= 0.0F;
   }

   private static boolean open(Packet<?> packet) {
      return packet instanceof ServerboundChatPacket
         || packet instanceof ServerboundChatCommandPacket
         || packet instanceof ServerboundChatCommandSignedPacket;
   }

   private static boolean poll(Packet<?> packet) {
      return packet instanceof ServerboundKeepAlivePacket || packet instanceof ServerboundPongPacket || packet instanceof ServerboundResourcePackPacket;
   }

   public enum Inner_DataUtil {
      init_HashUtil_DataUtil(0),
      load(1),
      read(2);

      private final int init_I;

      Inner_DataUtil(int priority) {
         this.init_I = priority;
      }
   }

   public interface Inner_StreamUtil {
      HashUtil.Inner_DataUtil classify(Packet<?> var1, boolean var2);
   }
}
