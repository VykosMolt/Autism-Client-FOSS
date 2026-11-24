package autismclient.modules;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record FileCache(AABB init_AABB, int init_I, int load_I, Vec3 init_Vec3, Vec3 load_Vec3, float init_F) {
   public FileCache(AABB box, int color) {
      this(box, color, color, null, null, 1.5F);
   }

   public FileCache(AABB box, int fill, int outline) {
      this(box, fill, outline, null, null, 1.5F);
   }

   public static FileCache init(Vec3 from, Vec3 to, int color, float width) {
      return new FileCache(null, 0, color, from, to, width);
   }
}
