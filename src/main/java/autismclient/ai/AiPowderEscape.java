package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class AiPowderEscape {
   private AiPowderEscape() {
   }

   static boolean isInPowderSnow(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         BlockPos feet = mc.player.blockPosition();
         return mc.level.getBlockState(feet).is(Blocks.POWDER_SNOW) || mc.level.getBlockState(feet.above()).is(Blocks.POWDER_SNOW);
      } else {
         return false;
      }
   }

   static BlockPos init_BlockPos(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-3, -1, -3), feet.offset(3, 3, 3))) {
         BlockState floor = mc.level.getBlockState(p.below());
         if (!floor.is(Blocks.POWDER_SNOW) && !floor.getCollisionShape(mc.level, p.below()).isEmpty()) {
            boolean clear = true;

            for (BlockPos body : new BlockPos[]{p, p.above()}) {
               BlockState s = mc.level.getBlockState(body);
               if (s.is(Blocks.POWDER_SNOW) || !s.getCollisionShape(mc.level, body).isEmpty() || !s.getFluidState().isEmpty()) {
                  clear = false;
                  break;
               }
            }

            if (clear) {
               double d = p.distSqr(feet) + Math.max(0, p.getY() - feet.getY()) * 2;
               if (d < bestD) {
                  bestD = d;
                  best = p.immutable();
               }
            }
         }
      }

      return best;
   }

   static AiOperation init() {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         final long init_J = System.nanoTime() + 10000000000L;
         BlockPos init_BlockPos;
         int init_I;

         @Override
         public AiOperation.Result tick() {
            if (mc.player == null || mc.level == null) {
               return this.init(AiOperation.Status.CANCELLED, "Left the world");
            }

            if (!AiPowderEscape.isInPowderSnow(mc) && mc.player.onGround()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Out of the powder snow");
            }

            if (System.nanoTime() > this.init_J) {
               return this.init(AiOperation.Status.FAILED, "Still in powder snow");
            }

            if (--this.init_I <= 0) {
               this.init_BlockPos = AiPowderEscape.init_BlockPos(mc);
               this.init_I = 10;
            }

            if (this.init_BlockPos != null) {
               this.init_DataUtil.aimAt(new Vec3(this.init_BlockPos.getX() + 0.5, this.init_BlockPos.getY() + 0.5, this.init_BlockPos.getZ() + 0.5));
               this.init_DataUtil.tick();
            }

            mc.options.keyUp.setDown(this.init_BlockPos != null);
            mc.options.keyJump.setDown(true);
            mc.options.keyShift.setDown(false);
            return AiGameOperations.result(
               AiOperation.Status.RUNNING,
               "Climbing out of powder snow" + (this.init_BlockPos == null ? "" : " to " + this.init_BlockPos.toShortString()),
               new JsonObject()
            );
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.init();
            return AiGameOperations.result(status, message, new JsonObject());
         }

         void init() {
            mc.options.keyUp.setDown(false);
            mc.options.keyJump.setDown(false);
            this.init_DataUtil.clear();
         }

         @Override
         public void cancel() {
            this.init();
         }
      };
   }
}
