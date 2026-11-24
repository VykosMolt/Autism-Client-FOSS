package autismclient.util;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

final class da {
   private Object init_Obj;
   private MapId init_MapId;
   private MapItemSavedData init_MapItemSavedData;
   private int init_I;
   private boolean init_Z;
   private boolean load;

   boolean init(Object level, MapId id, MapItemSavedData data, int tick) {
      if (level == null || id == null || data == null) {
         this.init();
         return false;
      }

      if (this.init_Z && this.init_Obj == level && id.equals(this.init_MapId) && this.init_MapItemSavedData == data && this.init_I == tick) {
         return this.load;
      }

      this.init_Obj = level;
      this.init_MapId = id;
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

   void init(MapId id) {
      if (id != null && id.equals(this.init_MapId)) {
         this.init_Z = false;
      }
   }

   void init() {
      this.init_Obj = null;
      this.init_MapId = null;
      this.init_MapItemSavedData = null;
      this.init_Z = this.load = false;
   }
}
