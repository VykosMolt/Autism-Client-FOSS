package autismclient.ai;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class cU extends aR {
   private final Object init_Obj;
   BlockPos init_BlockPos;
   BlockPos load;
   BlockPos read;
   private double init_D = 2.0;

   void init_V_D(double scroll) {
      if (Double.isFinite(scroll)) {
         this.init_D = Math.clamp(this.init_D + scroll * Math.max(0.5, this.init_D * 0.15), 0.5, 250.0);
      }

      this.init_V_Z(true);
   }

   String init_Str_3() {
      return String.format(Locale.ROOT, "%.1f blocks", this.init_D);
   }

   cU(Minecraft mc) {
      this.init_Obj = mc.level;
   }

   void load_V_Z(boolean left) {
      this.init_V_Z(true);
      if (this.read != null) {
         if (left) {
            this.init_BlockPos = this.read;
         } else {
            this.load = this.read;
         }
      }
   }

   cP.Inner_DataUtil init_cP_DataUtil() {
      return this.load_Z() ? cP.Inner_DataUtil.between(this.init_BlockPos, this.load) : null;
   }

   @Override
   boolean load_Z() {
      return this.init_BlockPos != null && this.load != null;
   }

   @Override
   boolean read_Z() {
      Minecraft mc = Minecraft.getInstance();
      return this.init_Obj != null && mc.level == this.init_Obj && mc.player != null;
   }

   @Override
   void init_V_Z(boolean surface) {
      Minecraft mc = Minecraft.getInstance();
      if (!this.read_Z()) {
         this.read = null;
      } else {
         Vec3 eye = init_Vec3_Minecraft(mc);
         Vec3 direction = load_Vec3_Minecraft(mc);
         BlockHitResult hit = mc.level.clip(new ClipContext(eye, eye.add(direction.scale(this.init_D)), Block.OUTLINE, Fluid.NONE, mc.player));
         if (hit.getType() == Type.BLOCK) {
            this.read = hit.getBlockPos().immutable();
         } else {
            this.read = BlockPos.containing(eye.add(direction.scale(this.init_D)));
         }
      }
   }

   static Vec3 init_Vec3_Minecraft(Minecraft mc) {
      return autismclient.modules.FreecamController.init_Z() ? autismclient.modules.FreecamController.init_Vec3_F(1.0F) : mc.player.getEyePosition();
   }

   static Vec3 load_Vec3_Minecraft(Minecraft mc) {
      return autismclient.modules.FreecamController.init_Z()
         ? Vec3.directionFromRotation(autismclient.modules.FreecamController.load_F_F(1.0F), autismclient.modules.FreecamController.init_F_F(1.0F))
         : mc.player.getViewVector(1.0F);
   }

   static BlockPos init(Vec3 eye, Vec3 direction) {
      return BlockPos.containing(eye.add(direction.scale(2.0)));
   }

   @Override
   BlockPos init_BlockPos() {
      return this.init_BlockPos == null ? BlockPos.ZERO : this.init_BlockPos;
   }

   @Override
   int init_I() {
      return 0;
   }

   @Override
   String read_Str() {
      return "none";
   }

   @Override
   void init(double scroll, double x, double y, double w, double h) {
   }

   @Override
   void load_V() {
   }

   @Override
   void init(int x, int y, int z) {
   }

   @Override
   void init_V_I(int steps) {
   }

   @Override
   void init_V() {
   }

   @Override
   void init(BlockPos p, int rotation, String mirror) {
   }

   @Override
   void read_V() {
   }
}
