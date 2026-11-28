package autismclient.util.macro;

import autismclient.util.PacketNameResolver;
import autismclient.util.PacketTypeRegistry;
import autismclient.util.AutismSessionState;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;

public final class PacketGateManager {
   private static final ConcurrentHashMap<String, PacketGateManager.Inner_DataUtil> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private static volatile String init_Str = null;

   private PacketGateManager() {
   }

   public static void init(PacketGateAction action) {
      init(action, 0L);
   }

   public static void init(PacketGateAction action, long ownerRunId) {
      if (!autismclient.modules.aD.read_Z()) {
         load(action, ownerRunId);
      }
   }

   static void load(PacketGateAction action, long ownerRunId) {
      if (action != null) {
         ownerRunId = Math.max(0L, ownerRunId);
         String id = action.gateId != null && !action.gateId.isBlank() ? action.gateId.trim() : "auto";
         if (action.mode == PacketGateAction.GateMode.DISABLE_GATE) {
            init(id, ownerRunId);
         } else {
            String key = init(ownerRunId, id);
            init_ConcurrentHashMap.put(key, new PacketGateManager.Inner_DataUtil(ownerRunId, id, action.mode, action.packetNamesCopy(), action.flushOnDisable));
            init_Str = key;
         }
      }
   }

   public static void removeGate(String id) {
      if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
         String target = id.trim();
         init_ConcurrentHashMap.entrySet().removeIf(entry -> entry.getValue().id.equalsIgnoreCase(target));
         read();
      } else {
         clearAll();
      }
   }

   public static void init(String id, long ownerRunId) {
      if (ownerRunId <= 0L) {
         removeGate(id);
      } else {
         if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
            init_ConcurrentHashMap.remove(init(ownerRunId, id));
            init_ConcurrentHashMap.remove(init(0L, id));
         } else {
            init_ConcurrentHashMap.entrySet().removeIf(entry -> entry.getValue().ownerRunId == ownerRunId);
         }

         read();
      }
   }

   public static void init(String id, ClientPacketListener connection, boolean flushRequested) {
      init(id, 0L, connection, flushRequested);
   }

   public static void init(String id, long ownerRunId, ClientPacketListener connection, boolean flushRequested) {
      if (autismclient.modules.aD.read_Z()) {
         init(id, ownerRunId);
      } else if (ownerRunId > 0L || id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
         ArrayList<PacketGateManager.Inner_DataUtil> removed = new ArrayList<>();
         if (id == null || id.isBlank() || id.equalsIgnoreCase("all")) {
            init_ConcurrentHashMap.entrySet().removeIf(entry -> {
               if (entry.getValue().ownerRunId != ownerRunId) {
                  return false;
               }

               removed.add(entry.getValue());
               return true;
            });
         } else if (ownerRunId > 0L) {
            PacketGateManager.Inner_DataUtil gate = init_ConcurrentHashMap.remove(init(ownerRunId, id));
            if (gate != null) {
               removed.add(gate);
            }
         } else {
            String target = id.trim();
            init_ConcurrentHashMap.entrySet().removeIf(entry -> {
               if (!entry.getValue().id.equalsIgnoreCase(target)) {
                  return false;
               }

               removed.add(entry.getValue());
               return true;
            });
         }

         read();
         boolean shouldFlush = flushRequested || removed.stream().anyMatch(gate -> gate.flushOnDisable);
         if (shouldFlush && !hasDelayGate()) {
            AutismSessionState.getInstance().flushDelayedPackets(connection);
         }
      } else {
         init(connection);
      }
   }

   public static void init_V() {
      if (autismclient.modules.aD.read_Z()) {
         clearAll();
      } else {
         String key = init_Str;
         if (key != null) {
            PacketGateManager.Inner_DataUtil gate = init_ConcurrentHashMap.get(key);
            if (gate != null) {
               init(gate.id, gate.ownerRunId, Minecraft.getInstance().getConnection(), true);
            }
         }
      }
   }

   public static void clearAll() {
      init_ConcurrentHashMap.clear();
      init_Str = null;
   }

   public static void init(ClientPacketListener connection) {
      if (autismclient.modules.aD.read_Z()) {
         clearAll();
      } else {
         boolean flush = init_ConcurrentHashMap.values().stream().anyMatch(g -> g.mode == PacketGateAction.GateMode.DELAY && g.flushOnDisable);
         clearAll();
         if (flush) {
            AutismSessionState.getInstance().flushDelayedPackets(connection);
         }
      }
   }

   public static void init(long ownerRunId, ClientPacketListener connection) {
      if (ownerRunId > 0L) {
         init("all", ownerRunId, connection, false);
      }
   }

   private static boolean hasDelayGate() {
      return init_ConcurrentHashMap.values().stream().anyMatch(g -> g.mode == PacketGateAction.GateMode.DELAY);
   }

   private static String init_Str() {
      String newest = null;

      for (String key : init_ConcurrentHashMap.keySet()) {
         newest = key;
      }

      return newest;
   }

   private static void read() {
      String current = init_Str;
      if (current == null || !init_ConcurrentHashMap.containsKey(current)) {
         init_Str = init_Str();
      }
   }

   private static String init(long ownerRunId, String id) {
      String normalized = id != null && !id.isBlank() ? id.trim().toLowerCase(Locale.ROOT) : "auto";
      return ownerRunId + "\u0000" + normalized;
   }

   static int init(long ownerRunId) {
      int count = 0;

      for (PacketGateManager.Inner_DataUtil gate : init_ConcurrentHashMap.values()) {
         if (gate.ownerRunId == ownerRunId) {
            count++;
         }
      }

      return count;
   }

   public static PacketGateManager.Inner_StreamUtil init(Packet<?> packet, String direction) {
      if (autismclient.modules.aD.read_Z()) {
         return PacketGateManager.Inner_StreamUtil.PASS;
      }

      if (packet != null && !init_ConcurrentHashMap.isEmpty()) {
         if (init(packet)) {
            return PacketGateManager.Inner_StreamUtil.PASS;
         }

         PacketGateManager.Inner_StreamUtil result = PacketGateManager.Inner_StreamUtil.PASS;
         ClientPacketListener current = Minecraft.getInstance().getConnection();
         Iterator<PacketGateManager.Inner_DataUtil> gates = init_ConcurrentHashMap.values().iterator();

         while (gates.hasNext()) {
            PacketGateManager.Inner_DataUtil gate = gates.next();
            // Gates without a run owner end with the connection they were made on.
            if (gate.ownerRunId == 0L && gate.connection != current) {
               gates.remove();
               read();
               continue;
            }

            boolean packetMatches = gate.load || init(gate.normalized, packet, direction);
            switch (gate.mode) {
               case CANCEL:
                  if (packetMatches) {
                     return PacketGateManager.Inner_StreamUtil.CANCEL;
                  }
                  break;
               case DELAY:
                  if (packetMatches) {
                     result = PacketGateManager.Inner_StreamUtil.DELAY;
                  }
                  break;
               case ALLOW_ONLY:
                  if (!packetMatches) {
                     return PacketGateManager.Inner_StreamUtil.CANCEL;
                  }
               case DISABLE_GATE:
            }
         }

         return result;
      } else {
         return PacketGateManager.Inner_StreamUtil.PASS;
      }
   }

   private static boolean init(Packet<?> packet) {
      return packet instanceof ServerboundPongPacket
         || packet instanceof ClientboundPingPacket
         || packet instanceof ServerboundKeepAlivePacket
         || packet instanceof ClientboundKeepAlivePacket;
   }

   private static boolean init(String[] normalizedPatterns, Packet<?> packet, String direction) {
      for (int i = 0; i < normalizedPatterns.length; i++) {
         if (load(normalizedPatterns[i], packet, direction)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasActiveGates() {
      return autismclient.modules.aD.read_Z() ? false : !init_ConcurrentHashMap.isEmpty();
   }

   public static boolean init(String expected, Packet<?> packet, String direction) {
      return expected != null && !expected.isBlank() ? load(resolvePacketName(expected), packet, direction) : true;
   }

   private static boolean load(String want, Packet<?> packet, String direction) {
      if (want.isEmpty()) {
         return false;
      }

      if (init(want, resolvePacketName(PacketNameResolver.directionalName(packet, direction)))) {
         return true;
      }

      if (init(want, resolvePacketName(PacketNameResolver.nameOf(packet)))) {
         return true;
      }

      if (init(want, resolvePacketName(packet.getClass().getSimpleName()))) {
         return true;
      }

      String registry = resolvePacketName(PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass()));
      return init(want, registry);
   }

   private static boolean init(String a, String b) {
      return !a.isEmpty() && !b.isEmpty() && (a.equals(b) || a.endsWith(b) || b.endsWith(a));
   }

   private static String resolvePacketName(String value) {
      return PacketNameResolver.normalizeCached(value);
   }

   public static final class Inner_DataUtil {
      public final long ownerRunId;
      public final String id;
      public final PacketGateAction.GateMode mode;
      public final ArrayList<String> packets;
      public final boolean flushOnDisable;
      final String[] normalized;
      final boolean load;
      final ClientPacketListener connection;

      Inner_DataUtil(long ownerRunId, String id, PacketGateAction.GateMode mode, ArrayList<String> packets, boolean flushOnDisable) {
         this.connection = Minecraft.getInstance().getConnection();
         this.ownerRunId = ownerRunId;
         this.id = id;
         this.mode = mode;
         this.packets = packets;
         this.flushOnDisable = flushOnDisable;
         boolean all = packets.isEmpty();
         String[] normalized = new String[packets.size()];

         for (int i = 0; i < packets.size(); i++) {
            String pattern = packets.get(i);
            if (pattern != null && !pattern.isBlank()) {
               normalized[i] = PacketGateManager.resolvePacketName(pattern);
            } else {
               all = true;
               normalized[i] = "";
            }
         }

         this.load = all;
         this.normalized = normalized;
      }
   }

   public enum Inner_StreamUtil {
      PASS,
      CANCEL,
      DELAY;
   }
}
