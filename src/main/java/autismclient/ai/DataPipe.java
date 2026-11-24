package autismclient.ai;

import com.google.gson.JsonArray;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;

final class DataPipe {
   private final Set<UUID> init_Set = new HashSet<>();
   private final Set<UUID> load = new HashSet<>();
   private final Map<UUID, LivingEntity> init_Map = new HashMap<>();
   private final JsonArray init_JsonArray = new JsonArray();

   void init(UUID identity) {
      if (!this.load.contains(identity)) {
         this.init_Set.add(identity);
      }
   }

   void init(LivingEntity entity) {
      this.init(entity.getUUID());
      if (this.init_Set.contains(entity.getUUID())) {
         this.init_Map.put(entity.getUUID(), entity);
      }
   }

   boolean init(UUID identity, boolean confirmedDeath, boolean stillPresent) {
      if (!this.init_Set.contains(identity)) {
         return false;
      }

      if (confirmedDeath) {
         this.init_Set.remove(identity);
         return this.load.add(identity);
      }

      if (!stillPresent) {
         this.init_Set.remove(identity);
      }

      return false;
   }

   void init(Minecraft mc) {
      Iterator<Entry<UUID, LivingEntity>> iterator = this.init_Map.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, LivingEntity> entry = iterator.next();
         LivingEntity entity = entry.getValue();
         if (this.init(entry.getKey(), entity.isDeadOrDying(), !entity.isRemoved() && mc.level.getEntity(entity.getId()) == entity)) {
            this.init_JsonArray.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
         }

         if (!this.init_Set.contains(entry.getKey())) {
            iterator.remove();
         }
      }
   }

   int init_I() {
      return this.load.size();
   }

   JsonArray init_JsonArray() {
      return this.init_JsonArray.deepCopy();
   }
}
