package autismclient.util.g;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket.Entry;
import net.minecraft.world.level.ChunkPos;

final class CacheEntry {
   private static final int init_I = 2048;
   private static final int load_I = 4096;
   private static final int read_I = 4096;
   private ClientboundLoginPacket init_ClientboundLoginPa;
   private ClientboundRespawnPacket init_ClientboundRespawn;
   private ClientboundSetChunkCacheCenterPacket init_ClientboundSetChun;
   private ClientboundSetChunkCacheRadiusPacket init_ClientboundSetChun_2;
   private ClientboundOpenScreenPacket init_ClientboundOpenScr;
   private ClientboundContainerSetContentPacket init_ClientboundContain;
   private final Map<Long, ClientboundLevelChunkWithLightPacket> init_Map = new LinkedHashMap<>();
   private final Map<Integer, ClientboundAddEntityPacket> load_Map = new LinkedHashMap<>();
   private final Map<Integer, ClientboundSetEntityDataPacket> read_Map = new LinkedHashMap<>();
   private final Map<UUID, ClientboundPlayerInfoUpdatePacket> write = new LinkedHashMap<>();

   synchronized void init_V_Packet(Packet<?> packet) {
      if (packet instanceof ClientboundLoginPacket loginPacket) {
         this.init_ClientboundLoginPa = loginPacket;
         this.init_ClientboundRespawn = null;
         this.load();
      } else if (packet instanceof ClientboundRespawnPacket respawnPacket) {
         this.init_ClientboundRespawn = respawnPacket;
         this.load();
      } else if (packet instanceof ClientboundLevelChunkWithLightPacket chunk) {
         long key = ChunkPos.pack(chunk.x(), chunk.z());
         this.init_Map.remove(key);
         this.init_Map.put(key, chunk);
         if (this.init_Map.size() > 2048) {
            init_V_Map(this.init_Map);
         }
      } else if (packet instanceof ClientboundForgetLevelChunkPacket forget) {
         this.init_Map.remove(forget.pos().pack());
      } else if (packet instanceof ClientboundSetChunkCacheCenterPacket center) {
         this.init_ClientboundSetChun = center;
      } else if (packet instanceof ClientboundSetChunkCacheRadiusPacket radius) {
         this.init_ClientboundSetChun_2 = radius;
      } else if (packet instanceof ClientboundAddEntityPacket add) {
         this.load_Map.remove(add.getId());
         this.load_Map.put(add.getId(), add);
         if (this.load_Map.size() > 4096) {
            var it = this.load_Map.keySet().iterator();
            if (it.hasNext()) {
               Integer evicted = it.next();
               it.remove();
               this.read_Map.remove(evicted);
            }
         }
      } else if (packet instanceof ClientboundSetEntityDataPacket data) {
         if (this.load_Map.containsKey(data.id())) {
            this.read_Map.put(data.id(), data);
         }
      } else if (packet instanceof ClientboundRemoveEntitiesPacket remove) {
         for (int i = 0; i < remove.entityIds().size(); i++) {
            int id = remove.entityIds().getInt(i);
            this.load_Map.remove(id);
            this.read_Map.remove(id);
         }
      } else if (packet instanceof ClientboundPlayerInfoUpdatePacket info) {
         for (Entry entry : info.entries()) {
            this.write.put(entry.profileId(), info);
         }

         while (this.write.size() > 4096) {
            init_V_Map(this.write);
         }
      } else if (packet instanceof ClientboundPlayerInfoRemovePacket remove) {
         for (UUID id : remove.profileIds()) {
            this.write.remove(id);
         }
      } else if (packet instanceof ClientboundOpenScreenPacket open) {
         this.init_ClientboundOpenScr = open;
         this.init_ClientboundContain = null;
      } else if (packet instanceof ClientboundContainerSetContentPacket content) {
         this.init_ClientboundContain = content;
      } else if (packet instanceof ClientboundContainerClosePacket) {
         this.init_ClientboundOpenScr = null;
         this.init_ClientboundContain = null;
      }
   }

   private void load() {
      this.init_Map.clear();
      this.load_Map.clear();
      this.read_Map.clear();
      this.write.clear();
      this.init_ClientboundSetChun = null;
      this.init_ClientboundSetChun_2 = null;
      this.init_ClientboundOpenScr = null;
      this.init_ClientboundContain = null;
   }

   private static void init_V_Map(Map<?, ?> map) {
      Iterator<?> it = map.keySet().iterator();
      if (it.hasNext()) {
         it.next();
         it.remove();
      }
   }

   synchronized boolean init_Z() {
      return this.init_ClientboundLoginPa != null;
   }

   synchronized CacheEntry.Inner_DataUtil init_CacheEntry_DataUti() {
      List<ClientboundPlayerInfoUpdatePacket> distinctInfo = new ArrayList<>(new LinkedHashSet<>(this.write.values()));
      return new CacheEntry.Inner_DataUtil(
         this.init_ClientboundLoginPa,
         this.init_ClientboundRespawn,
         this.init_ClientboundSetChun,
         this.init_ClientboundSetChun_2,
         new ArrayList<>(this.init_Map.values()),
         distinctInfo,
         new ArrayList<>(this.load_Map.values()),
         new ArrayList<>(this.read_Map.values()),
         this.init_ClientboundOpenScr,
         this.init_ClientboundContain
      );
   }

   synchronized void init_V() {
      this.init_ClientboundLoginPa = null;
      this.init_ClientboundRespawn = null;
      this.load();
   }

   record Inner_DataUtil(
      ClientboundLoginPacket init_ClientboundLoginPa,
      ClientboundRespawnPacket init_ClientboundRespawn,
      ClientboundSetChunkCacheCenterPacket init_ClientboundSetChun,
      ClientboundSetChunkCacheRadiusPacket init_ClientboundSetChun_2,
      List<ClientboundLevelChunkWithLightPacket> init_List,
      List<ClientboundPlayerInfoUpdatePacket> load,
      List<ClientboundAddEntityPacket> read,
      List<ClientboundSetEntityDataPacket> write,
      ClientboundOpenScreenPacket init_ClientboundOpenScr,
      ClientboundContainerSetContentPacket init_ClientboundContain
   ) {
   }
}
