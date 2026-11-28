package autismclient.util;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class MapNonEmptyCache {
   private Object level;
   private MapId id;
   private MapItemSavedData cachedData;
   private int cachedTick;
   private boolean cacheValid;
   private boolean cachedNonEmpty;

   boolean isNonEmpty(Object level, MapId id, MapItemSavedData data, int tick) {
      if (level == null || id == null || data == null) {
         this.clear();
         return false;
      }

      if (this.cacheValid && this.level == level && id.equals(this.id) && this.cachedData == data && this.cachedTick == tick) {
         return this.cachedNonEmpty;
      }

      this.level = level;
      this.id = id;
      this.cachedData = data;
      this.cachedTick = tick;
      this.cacheValid = true;
      this.cachedNonEmpty = false;

      for (byte color : data.colors) {
         if (MapColor.getColorFromPackedId(color) != 0) {
            this.cachedNonEmpty = true;
            break;
         }
      }

      return this.cachedNonEmpty;
   }

   void invalidate(MapId id) {
      if (id != null && id.equals(this.id)) {
         this.cacheValid = false;
      }
   }

   void clear() {
      this.level = null;
      this.id = null;
      this.cachedData = null;
      this.cacheValid = this.cachedNonEmpty = false;
   }
}
