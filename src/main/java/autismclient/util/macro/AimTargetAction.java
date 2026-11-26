package autismclient.util.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface AimTargetAction {
   boolean init_Z_3();

   void init_V_Z(boolean var1);

   AimTargetAction.AimTarget init_M_DataUtil_Minecraft(Minecraft var1);

   record AimTarget(BlockPos init_BlockPos, Entity init_Entity, Vec3 init_Vec3) {
      public static AimTargetAction.AimTarget init(BlockPos pos) {
         return pos == null ? null : new AimTargetAction.AimTarget(pos, null, null);
      }

      public static AimTargetAction.AimTarget init(Entity entity) {
         return entity == null ? null : new AimTargetAction.AimTarget(null, entity, null);
      }

      public static AimTargetAction.AimTarget init(Vec3 point) {
         return point == null ? null : new AimTargetAction.AimTarget(null, null, point);
      }
   }
}
