package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiFireControl {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
   private BlockPos init_BlockPos;
   private long init_J;
   private int init_I;
   private int load;
   private AiOperation init_AiOperation;
   private final boolean init_Z;
   private String init_Str = "";

   AiFireControl() {
      this(false);
   }

   AiFireControl(boolean bodyOnly) {
      this.init_Z = bodyOnly;
      this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.Inner_DataUtil.write);
   }

   int init_I() {
      return this.load;
   }

   String init_Str() {
      return this.init_Str;
   }

   boolean init_Z() {
      return this.init_BlockPos != null;
   }

   boolean load_Z() {
      return this.init_BlockPos == null && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
   }

   boolean read() {
      return this.init_BlockPos != null || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
   }

   boolean init(boolean protectDrops, Vec3 killAt) {
      if (this.init_Minecraft.player != null && this.init_Minecraft.level != null && this.init_Minecraft.gameMode != null) {
         if (this.init_BlockPos != null) {
            if (!(this.init_Minecraft.level.getBlockState(this.init_BlockPos).getBlock() instanceof BaseFireBlock)
               && AiServerEvidence.init_J_BlockPos(this.init_BlockPos) > this.init_J) {
               this.load++;
               this.init_BlockPos = null;
               this.init_DataUtil.init_V();
            } else {
               if (++this.init_I < 20) {
                  this.load_V();
                  this.init_Str = "Confirming extinguished fire before fighting or picking up rods";
                  return true;
               }

               this.init_BlockPos = null;
               this.init_DataUtil.init_V();
            }
         }

         if (this.init_AiOperation != null && !this.init_AiOperation.canInterruptNow()) {
            AiOperation.Result r = this.init_AiOperation.tick();
            if (r.terminal()) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            this.init_Str = "Finish the committed approach before extinguishing fire";
            return true;
         } else {
            BlockPos feet = this.init_Minecraft.player.blockPosition();
            BlockPos best = null;
            double score = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-6, -1, -6), feet.offset(6, 2, 6))) {
               if (AiTerrain.init(this.init_Minecraft.level, p) && this.init_Minecraft.level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                  boolean body = new AABB(p).inflate(0.2).intersects(this.init_Minecraft.player.getBoundingBox());
                  if (!this.init_Z || body) {
                     boolean loot = protectDrops
                        && !this.init_Minecraft
                           .level
                           .getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(1.0), e -> e.getItem().is(Items.BLAZE_ROD))
                           .isEmpty();
                     boolean killFloor = killAt != null && p.getY() <= killAt.y + 1.0 && p.distToCenterSqr(killAt) < 9.0;
                     boolean nearby = p.distToCenterSqr(this.init_Minecraft.player.position()) < 7.0;
                     if (body || loot || killFloor || nearby) {
                        double cost = p.distToCenterSqr(this.init_Minecraft.player.getEyePosition()) + (body ? -1000 : (loot ? -500 : 0));
                        if (cost < score) {
                           score = cost;
                           best = p.immutable();
                        }
                     }
                  }
               }
            }

            if (best == null) {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               return false;
            } else {
               Vec3 point = Vec3.atCenterOf(best);
               Vec3 eye = this.init_Minecraft.player.getEyePosition();
               BlockHitResult ray = this.init_Minecraft.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, this.init_Minecraft.player));
               boolean reachable = eye.distanceTo(point) < this.init_Minecraft.player.blockInteractionRange() - 0.1
                  && (ray.getType() == Type.MISS || ray.getBlockPos().equals(best));
               if (!reachable) {
                  if (!protectDrops) {
                     return false;
                  }

                  if (this.init_AiOperation == null) {
                     this.init_AiOperation = AiGoto.init(
                        best,
                        3.0,
                        false,
                        false,
                        false,
                        5,
                        autismclient.ai.engine.AiPathfinder.Inner_IoWorker.load,
                        false,
                        0.0,
                        null,
                        new AiGoto.Inner_BufferHelper(false, false, 1, true, false, false)
                     );
                  }

                  AiOperation.Result r = this.init_AiOperation.tick();
                  this.init_Str = "Reach the burning rod's fire from safe existing floor";
                  if (r.terminal()) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                     return false;
                  } else {
                     return true;
                  }
               } else {
                  if (!this.init_Minecraft.player.onGround() && !this.init_Minecraft.player.isInWater() && !this.init_Minecraft.player.onClimbable()) {
                     return false;
                  }

                  if (this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  this.load_V();
                  if (this.init_Minecraft.player.isUsingItem()) {
                     this.init_Minecraft.gameMode.releaseUsingItem(this.init_Minecraft.player);
                  }

                  this.init_Minecraft.options.keyUse.setDown(false);
                  this.init_DataUtil.init_V_Vec3(point);
                  this.init_DataUtil.load_V();
                  this.init_Str = "Extinguish fire before combat and rod collection";
                  Vec3 look = eye.add(
                     Vec3.directionFromRotation(this.init_Minecraft.player.getXRot(), this.init_Minecraft.player.getYRot())
                        .scale(this.init_Minecraft.player.blockInteractionRange() - 0.1)
                  );
                  if (new AABB(best).clip(eye, look).isEmpty()) {
                     return true;
                  }

                  this.init_J = AiServerEvidence.init_J_BlockPos(best);
                  this.init_BlockPos = best;
                  this.init_I = 0;
                  this.init_Minecraft.gameMode.startDestroyBlock(best, Direction.UP);
                  Compat263.swing(this.init_Minecraft.player, InteractionHand.MAIN_HAND);
                  this.init_Minecraft.player.resetAttackStrengthTicker();
                  AiRunTrace.init_V_Str_JsonObject("blaze_fire_extinguish", AiJson.init_JsonObject_ObjArr("at", best.toShortString(), "priority", "before-combat-and-loot"));
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void load_V() {
      for (KeyMapping k : new KeyMapping[]{
         this.init_Minecraft.options.keyUp,
         this.init_Minecraft.options.keyDown,
         this.init_Minecraft.options.keyLeft,
         this.init_Minecraft.options.keyRight,
         this.init_Minecraft.options.keyJump,
         this.init_Minecraft.options.keySprint
      }) {
         k.setDown(false);
      }
   }

   void init_V() {
      this.init_DataUtil.init_V();
      if (this.init_AiOperation != null) {
         this.init_AiOperation.cancel();
      }

      this.init_AiOperation = null;
      this.init_BlockPos = null;
   }
}
