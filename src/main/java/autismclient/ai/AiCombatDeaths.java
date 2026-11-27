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

final class AiCombatDeaths {
   private final Set<UUID> counted = new HashSet<>();
   private final Set<UUID> load = new HashSet<>();
   private final Map<UUID, LivingEntity> tracked = new HashMap<>();
   private final JsonArray init_JsonArray = new JsonArray();

   void init(UUID identity) {
      if (!this.load.contains(identity)) {
         this.counted.add(identity);
      }
   }

   void init(LivingEntity entity) {
      this.init(entity.getUUID());
      if (this.counted.contains(entity.getUUID())) {
         this.tracked.put(entity.getUUID(), entity);
      }
   }

   boolean init(UUID identity, boolean confirmedDeath, boolean stillPresent) {
      if (!this.counted.contains(identity)) {
         return false;
      }

      if (confirmedDeath) {
         this.counted.remove(identity);
         return this.load.add(identity);
      }

      if (!stillPresent) {
         this.counted.remove(identity);
      }

      return false;
   }

   void init(Minecraft mc) {
      Iterator<Entry<UUID, LivingEntity>> iterator = this.tracked.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<UUID, LivingEntity> entry = iterator.next();
         LivingEntity entity = entry.getValue();
         if (this.init(entry.getKey(), entity.isDeadOrDying(), !entity.isRemoved() && mc.level.getEntity(entity.getId()) == entity)) {
            this.init_JsonArray.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString());
         }

         if (!this.counted.contains(entry.getKey())) {
            iterator.remove();
         }
      }
   }

   int killCount() {
      return this.load.size();
   }

   JsonArray killedTypes() {
      return this.init_JsonArray.deepCopy();
   }
}
