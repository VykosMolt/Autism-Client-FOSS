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
         if (action.mode == PacketGateAction.Inner_CachePool.DISABLE_GATE) {
            init(id, ownerRunId);
         } else {
            String key = init(ownerRunId, id);
            init_ConcurrentHashMap.put(key, new PacketGateManager.Inner_DataUtil(ownerRunId, id, action.mode, action.init(), action.flushOnDisable));
            init_Str = key;
         }
      }
   }

   public static void init_V(String id) {
      if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
         String target = id.trim();
         init_ConcurrentHashMap.entrySet().removeIf(entry -> entry.getValue().init_Str.equalsIgnoreCase(target));
         read();
      } else {
         load_V();
      }
   }

   public static void init(String id, long ownerRunId) {
      if (ownerRunId <= 0L) {
         init_V(id);
      } else {
         if (id != null && !id.isBlank() && !id.equalsIgnoreCase("all")) {
            init_ConcurrentHashMap.remove(init(ownerRunId, id));
            init_ConcurrentHashMap.remove(init(0L, id));
         } else {
            init_ConcurrentHashMap.entrySet().removeIf(entry -> entry.getValue().init_J == ownerRunId);
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
               if (entry.getValue().init_J != ownerRunId) {
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
               if (!entry.getValue().init_Str.equalsIgnoreCase(target)) {
                  return false;
               }

               removed.add(entry.getValue());
               return true;
            });
         }

         read();
         boolean shouldFlush = flushRequested || removed.stream().anyMatch(gate -> gate.init_Z);
         if (shouldFlush && !load_Z()) {
            AutismSessionState.init_ck().init_I_ClientPacketListen(connection);
         }
      } else {
         init(connection);
      }
   }

   public static void init_V() {
      if (autismclient.modules.aD.read_Z()) {
         load_V();
      } else {
         String key = init_Str;
         if (key != null) {
            PacketGateManager.Inner_DataUtil gate = init_ConcurrentHashMap.get(key);
            if (gate != null) {
               init(gate.init_Str, gate.init_J, Minecraft.getInstance().getConnection(), true);
            }
         }
      }
   }

   public static void load_V() {
      init_ConcurrentHashMap.clear();
      init_Str = null;
   }

   public static void init(ClientPacketListener connection) {
      if (autismclient.modules.aD.read_Z()) {
         load_V();
      } else {
         boolean flush = init_ConcurrentHashMap.values().stream().anyMatch(g -> g.init_B_CachePool == PacketGateAction.Inner_CachePool.DELAY && g.init_Z);
         load_V();
         if (flush) {
            AutismSessionState.init_ck().init_I_ClientPacketListen(connection);
         }
      }
   }

   public static void init(long ownerRunId, ClientPacketListener connection) {
      if (ownerRunId > 0L) {
         init("all", ownerRunId, connection, false);
      }
   }

   private static boolean load_Z() {
      return init_ConcurrentHashMap.values().stream().anyMatch(g -> g.init_B_CachePool == PacketGateAction.Inner_CachePool.DELAY);
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
         if (gate.init_J == ownerRunId) {
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
            if (gate.init_J == 0L && gate.connection != current) {
               gates.remove();
               read();
               continue;
            }

            boolean packetMatches = gate.load || init(gate.init_StrArr, packet, direction);
            switch (gate.init_B_CachePool) {
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

   public static boolean init_Z() {
      return autismclient.modules.aD.read_Z() ? false : !init_ConcurrentHashMap.isEmpty();
   }

   public static boolean init(String expected, Packet<?> packet, String direction) {
      return expected != null && !expected.isBlank() ? load(init_Str(expected), packet, direction) : true;
   }

   private static boolean load(String want, Packet<?> packet, String direction) {
      if (want.isEmpty()) {
         return false;
      }

      if (init(want, init_Str(PacketNameResolver.init_Str_Packet_Str(packet, direction)))) {
         return true;
      }

      if (init(want, init_Str(PacketNameResolver.init_Str_Packet(packet)))) {
         return true;
      }

      if (init(want, init_Str(packet.getClass().getSimpleName()))) {
         return true;
      }

      String registry = init_Str(PacketTypeRegistry.init_Str_Class((Class<? extends Packet<?>>)packet.getClass()));
      return init(want, registry);
   }

   private static boolean init(String a, String b) {
      return !a.isEmpty() && !b.isEmpty() && (a.equals(b) || a.endsWith(b) || b.endsWith(a));
   }

   private static String init_Str(String value) {
      return PacketNameResolver.load(value);
   }

   public static final class Inner_DataUtil {
      public final long init_J;
      public final String init_Str;
      public final PacketGateAction.Inner_CachePool init_B_CachePool;
      public final ArrayList<String> init_ArrayList;
      public final boolean init_Z;
      final String[] init_StrArr;
      final boolean load;
      final ClientPacketListener connection;

      Inner_DataUtil(long ownerRunId, String id, PacketGateAction.Inner_CachePool mode, ArrayList<String> packets, boolean flushOnDisable) {
         this.connection = Minecraft.getInstance().getConnection();
         this.init_J = ownerRunId;
         this.init_Str = id;
         this.init_B_CachePool = mode;
         this.init_ArrayList = packets;
         this.init_Z = flushOnDisable;
         boolean all = packets.isEmpty();
         String[] normalized = new String[packets.size()];

         for (int i = 0; i < packets.size(); i++) {
            String pattern = packets.get(i);
            if (pattern != null && !pattern.isBlank()) {
               normalized[i] = PacketGateManager.init_Str(pattern);
            } else {
               all = true;
               normalized[i] = "";
            }
         }

         this.load = all;
         this.init_StrArr = normalized;
      }
   }

   public enum Inner_StreamUtil {
      PASS,
      CANCEL,
      DELAY;
   }
}
