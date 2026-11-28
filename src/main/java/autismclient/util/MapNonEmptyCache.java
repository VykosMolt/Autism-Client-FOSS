package autismclient.util;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class MapNonEmptyCache {
   private Object level;
   private MapId id;
   private MapItemSavedData init_MapItemSavedData;
   private int init_I;
   private boolean init_Z;
   private boolean load;

   boolean isNonEmpty(Object level, MapId id, MapItemSavedData data, int tick) {
      if (level == null || id == null || data == null) {
         this.clear();
         return false;
      }

      if (this.init_Z && this.level == level && id.equals(this.id) && this.init_MapItemSavedData == data && this.init_I == tick) {
         return this.load;
      }

      this.level = level;
      this.id = id;
      this.init_MapItemSavedData = data;
      this.init_I = tick;
      this.init_Z = true;
      this.load = false;

      for (byte color : data.colors) {
         if (MapColor.getColorFromPackedId(color) != 0) {
            this.load = true;
            break;
         }
      }

      return this.load;
   }

   void invalidate(MapId id) {
      if (id != null && id.equals(this.id)) {
         this.init_Z = false;
      }
   }

   void clear() {
      this.level = null;
      this.id = null;
      this.init_MapItemSavedData = null;
      this.init_Z = this.load = false;
   }
}
