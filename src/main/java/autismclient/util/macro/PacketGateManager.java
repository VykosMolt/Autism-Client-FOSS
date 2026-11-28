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
   private static final ConcurrentHashMap<String, PacketGateManager.Gate> GATES = new ConcurrentHashMap<>();
   private static volatile String newestKey = null;

   private PacketGateManager() {
   }

   public static void openGate(PacketGateAction action) {
      open(action, 0L);
   }

   public static void open(PacketGateAction action, long ownerRunId) {
      if (!autismclient.modules.aD.read_Z()) {
         install(action, ownerRunId);
      }
   }

   static void install(PacketGateAction action, long ownerRunId) {
      if (action != null) {
         ownerRunId = Math.max(0L, ownerRunId);
         String id = action.gateId != null && !action.gateId.isBlank() ? action.gateId.trim() : "auto";
         if (action.mode == PacketGateAction.GateMode.DISABLE_GATE) {
            close(id, ownerRunId);
         } else {
            String key = gateKey(ownerRunId, id);
            GATES.put(key, new PacketGateManager.Gate(ownerRunId, id, action.mode, action.packetNamesCopy(), action.flushOnDisable));
            newestKey = key;
         }
      }
   }

   public static void removeGate(String id) {
      if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
         String target = id.trim();
         GATES.entrySet().removeIf(entry -> entry.getValue().id.equalsIgnoreCase(target));
         refreshNewestKey();
      } else {
         clearAll();
      }
   }

   public static void close(String id, long ownerRunId) {
      if (ownerRunId <= 0L) {
         removeGate(id);
      } else {
         if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
            GATES.remove(gateKey(ownerRunId, id));
            GATES.remove(gateKey(0L, id));
         } else {
            GATES.entrySet().removeIf(entry -> entry.getValue().ownerRunId == ownerRunId);
         }

         refreshNewestKey();
      }
   }

   public static void close(String id, ClientPacketListener connection, boolean flushRequested) {
      close(id, 0L, connection, flushRequested);
   }

   public static void close(String id, long ownerRunId, ClientPacketListener connection, boolean flushRequested) {
      if (autismclient.modules.aD.read_Z()) {
         close(id, ownerRunId);
      } else if (ownerRunId > 0L || id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
         ArrayList<PacketGateManager.Gate> removed = new ArrayList<>();
         if (id == null || id.isBlank() || id.equalsIgnoreCase("all")) {
            GATES.entrySet().removeIf(entry -> {
               if (entry.getValue().ownerRunId != ownerRunId) {
                  return false;
               }

               removed.add(entry.getValue());
               return true;
            });
         } else if (ownerRunId > 0L) {
            PacketGateManager.Gate gate = GATES.remove(gateKey(ownerRunId, id));
            if (gate != null) {
               removed.add(gate);
            }
         } else {
            String target = id.trim();
            GATES.entrySet().removeIf(entry -> {
               if (!entry.getValue().id.equalsIgnoreCase(target)) {
                  return false;
               }

               removed.add(entry.getValue());
               return true;
            });
         }

         refreshNewestKey();
         boolean shouldFlush = flushRequested || removed.stream().anyMatch(gate -> gate.flushOnDisable);
         if (shouldFlush && !hasDelayGate()) {
            AutismSessionState.getInstance().flushDelayedPackets(connection);
         }
      } else {
         onConnectionChange(connection);
      }
   }

   public static void init_V() {
      if (autismclient.modules.aD.read_Z()) {
         clearAll();
      } else {
         String key = newestKey;
         if (key != null) {
            PacketGateManager.Gate gate = GATES.get(key);
            if (gate != null) {
               close(gate.id, gate.ownerRunId, Minecraft.getInstance().getConnection(), true);
            }
         }
      }
   }

   public static void clearAll() {
      GATES.clear();
      newestKey = null;
   }

   public static void onConnectionChange(ClientPacketListener connection) {
      if (autismclient.modules.aD.read_Z()) {
         clearAll();
      } else {
         boolean flush = GATES.values().stream().anyMatch(g -> g.mode == PacketGateAction.GateMode.DELAY && g.flushOnDisable);
         clearAll();
         if (flush) {
            AutismSessionState.getInstance().flushDelayedPackets(connection);
         }
      }
   }

   public static void closeRun(long ownerRunId, ClientPacketListener connection) {
      if (ownerRunId > 0L) {
         close("all", ownerRunId, connection, false);
      }
   }

   private static boolean hasDelayGate() {
      return GATES.values().stream().anyMatch(g -> g.mode == PacketGateAction.GateMode.DELAY);
   }

   private static String findNewestKey() {
      String newest = null;

      for (String key : GATES.keySet()) {
         newest = key;
      }

      return newest;
   }

   private static void refreshNewestKey() {
      String current = newestKey;
      if (current == null || !GATES.containsKey(current)) {
         newestKey = findNewestKey();
      }
   }

   private static String gateKey(long ownerRunId, String id) {
      String normalized = id != null && !id.isBlank() ? id.trim().toLowerCase(Locale.ROOT) : "auto";
      return ownerRunId + "\u0000" + normalized;
   }

   static int countForRun(long ownerRunId) {
      int count = 0;

      for (PacketGateManager.Gate gate : GATES.values()) {
         if (gate.ownerRunId == ownerRunId) {
            count++;
         }
      }

      return count;
   }

   public static PacketGateManager.GateVerdict match(Packet<?> packet, String direction) {
      if (autismclient.modules.aD.read_Z()) {
         return PacketGateManager.GateVerdict.PASS;
      }

      if (packet != null && !GATES.isEmpty()) {
         if (isKeepAlivePacket(packet)) {
            return PacketGateManager.GateVerdict.PASS;
         }

         PacketGateManager.GateVerdict result = PacketGateManager.GateVerdict.PASS;
         ClientPacketListener current = Minecraft.getInstance().getConnection();
         Iterator<PacketGateManager.Gate> gates = GATES.values().iterator();

         while (gates.hasNext()) {
            PacketGateManager.Gate gate = gates.next();
            // Gates without a run owner end with the connection they were made on.
            if (gate.ownerRunId == 0L && gate.connection != current) {
               gates.remove();
               refreshNewestKey();
               continue;
            }

            boolean packetMatches = gate.matchAll || matchesAny(gate.normalized, packet, direction);
            switch (gate.mode) {
               case CANCEL:
                  if (packetMatches) {
                     return PacketGateManager.GateVerdict.CANCEL;
                  }
                  break;
               case DELAY:
                  if (packetMatches) {
                     result = PacketGateManager.GateVerdict.DELAY;
                  }
                  break;
               case ALLOW_ONLY:
                  if (!packetMatches) {
                     return PacketGateManager.GateVerdict.CANCEL;
                  }
               case DISABLE_GATE:
            }
         }

         return result;
      } else {
         return PacketGateManager.GateVerdict.PASS;
      }
   }

   private static boolean isKeepAlivePacket(Packet<?> packet) {
      return packet instanceof ServerboundPongPacket
         || packet instanceof ClientboundPingPacket
         || packet instanceof ServerboundKeepAlivePacket
         || packet instanceof ClientboundKeepAlivePacket;
   }

   private static boolean matchesAny(String[] normalizedPatterns, Packet<?> packet, String direction) {
      for (int i = 0; i < normalizedPatterns.length; i++) {
         if (matchesPattern(normalizedPatterns[i], packet, direction)) {
            return true;
         }
      }

      return false;
   }

   public static boolean hasActiveGates() {
      return autismclient.modules.aD.read_Z() ? false : !GATES.isEmpty();
   }

   public static boolean matches(String expected, Packet<?> packet, String direction) {
      return expected != null && !expected.isBlank() ? matchesPattern(resolvePacketName(expected), packet, direction) : true;
   }

   private static boolean matchesPattern(String want, Packet<?> packet, String direction) {
      if (want.isEmpty()) {
         return false;
      }

      if (namesMatch(want, resolvePacketName(PacketNameResolver.directionalName(packet, direction)))) {
         return true;
      }

      if (namesMatch(want, resolvePacketName(PacketNameResolver.nameOf(packet)))) {
         return true;
      }

      if (namesMatch(want, resolvePacketName(packet.getClass().getSimpleName()))) {
         return true;
      }

      String registry = resolvePacketName(PacketTypeRegistry.getPacketName((Class<? extends Packet<?>>)packet.getClass()));
      return namesMatch(want, registry);
   }

   private static boolean namesMatch(String a, String b) {
      return !a.isEmpty() && !b.isEmpty() && (a.equals(b) || a.endsWith(b) || b.endsWith(a));
   }

   private static String resolvePacketName(String value) {
      return PacketNameResolver.normalizeCached(value);
   }

   public static final class Gate {
      public final long ownerRunId;
      public final String id;
      public final PacketGateAction.GateMode mode;
      public final ArrayList<String> packets;
      public final boolean flushOnDisable;
      final String[] normalized;
      final boolean matchAll;
      final ClientPacketListener connection;

      Gate(long ownerRunId, String id, PacketGateAction.GateMode mode, ArrayList<String> packets, boolean flushOnDisable) {
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

         this.matchAll = all;
         this.normalized = normalized;
      }
   }

   public enum GateVerdict {
      PASS,
      CANCEL,
      DELAY;
   }
}
