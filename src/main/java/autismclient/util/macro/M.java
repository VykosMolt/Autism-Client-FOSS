package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface M {
   boolean init_Z_3();

   void init_V_Z(boolean var1);

   M.Inner_DataUtil init_M_DataUtil_Minecraft(Minecraft var1);

   record Inner_DataUtil(BlockPos init_BlockPos, Entity init_Entity, Vec3 init_Vec3) {
      public static M.Inner_DataUtil init(BlockPos pos) {
         return pos == null ? null : new M.Inner_DataUtil(pos, null, null);
      }

      public static M.Inner_DataUtil init(Entity entity) {
         return entity == null ? null : new M.Inner_DataUtil(null, entity, null);
      }

      public static M.Inner_DataUtil init(Vec3 point) {
         return point == null ? null : new M.Inner_DataUtil(null, null, point);
      }
   }
}
