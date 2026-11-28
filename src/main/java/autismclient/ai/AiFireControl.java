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
   private final Minecraft mc = Minecraft.getInstance();
   private final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
   private BlockPos fireTarget;
   private long revisionBefore;
   private int waitTicks;
   private int extinguished;
   private AiOperation approach;
   private final boolean bodyOnly;
   private String status = "";

   AiFireControl() {
      this(false);
   }

   AiFireControl(boolean bodyOnly) {
      this.bodyOnly = bodyOnly;
      this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
   }

   int firesExtinguished() {
      return this.extinguished;
   }

   String status() {
      return this.status;
   }

   boolean hasPendingFire() {
      return this.fireTarget != null;
   }

   boolean canInterrupt() {
      return this.fireTarget == null && (this.approach == null || this.approach.canInterruptNow());
   }

   boolean isBusy() {
      return this.fireTarget != null || this.approach != null && this.approach.requiresReconciliationAfterEmergency();
   }

   boolean init(boolean protectDrops, Vec3 killAt) {
      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         if (this.fireTarget != null) {
            if (!(this.mc.level.getBlockState(this.fireTarget).getBlock() instanceof BaseFireBlock)
               && AiServerEvidence.blockRevision(this.fireTarget) > this.revisionBefore) {
               this.extinguished++;
               this.fireTarget = null;
               this.aim.clear();
            } else {
               if (++this.waitTicks < 20) {
                  this.releaseKeys();
                  this.status = "Confirming extinguished fire before fighting or picking up rods";
                  return true;
               }

               this.fireTarget = null;
               this.aim.clear();
            }
         }

         if (this.approach != null && !this.approach.canInterruptNow()) {
            AiOperation.Result r = this.approach.tick();
            if (r.terminal()) {
               this.approach.cancel();
               this.approach = null;
            }

            this.status = "Finish the committed approach before extinguishing fire";
            return true;
         } else {
            BlockPos feet = this.mc.player.blockPosition();
            BlockPos best = null;
            double score = 1.7976931348623157E308;

            for (BlockPos p : BlockPos.betweenClosed(feet.offset(-6, -1, -6), feet.offset(6, 2, 6))) {
               if (AiTerrain.isLoaded(this.mc.level, p) && this.mc.level.getBlockState(p).getBlock() instanceof BaseFireBlock) {
                  boolean body = new AABB(p).inflate(0.2).intersects(this.mc.player.getBoundingBox());
                  if (!this.bodyOnly || body) {
                     boolean loot = protectDrops
                        && !this.mc
                           .level
                           .getEntitiesOfClass(ItemEntity.class, new AABB(p).inflate(1.0), e -> e.getItem().is(Items.BLAZE_ROD))
                           .isEmpty();
                     boolean killFloor = killAt != null && p.getY() <= killAt.y + 1.0 && p.distToCenterSqr(killAt) < 9.0;
                     boolean nearby = p.distToCenterSqr(this.mc.player.position()) < 7.0;
                     if (body || loot || killFloor || nearby) {
                        double cost = p.distToCenterSqr(this.mc.player.getEyePosition()) + (body ? -1000 : (loot ? -500 : 0));
                        if (cost < score) {
                           score = cost;
                           best = p.immutable();
                        }
                     }
                  }
               }
            }

            if (best == null) {
               if (this.approach != null) {
                  this.approach.cancel();
                  this.approach = null;
               }

               return false;
            } else {
               Vec3 point = Vec3.atCenterOf(best);
               Vec3 eye = this.mc.player.getEyePosition();
               BlockHitResult ray = this.mc.level.clip(new ClipContext(eye, point, Block.COLLIDER, Fluid.NONE, this.mc.player));
               boolean reachable = eye.distanceTo(point) < this.mc.player.blockInteractionRange() - 0.1
                  && (ray.getType() == Type.MISS || ray.getBlockPos().equals(best));
               if (!reachable) {
                  if (!protectDrops) {
                     return false;
                  }

                  if (this.approach == null) {
                     this.approach = AiGoto.travelToCompat(
                        best,
                        3.0,
                        false,
                        false,
                        false,
                        5,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                        false,
                        0.0,
                        null,
                        new AiGoto.GotoOptions(false, false, 1, true, false, false)
                     );
                  }

                  AiOperation.Result r = this.approach.tick();
                  this.status = "Reach the burning rod's fire from safe existing floor";
                  if (r.terminal()) {
                     this.approach.cancel();
                     this.approach = null;
                     return false;
                  } else {
                     return true;
                  }
               } else {
                  if (!this.mc.player.onGround() && !this.mc.player.isInWater() && !this.mc.player.onClimbable()) {
                     return false;
                  }

                  if (this.approach != null) {
                     this.approach.cancel();
                     this.approach = null;
                  }

                  this.releaseKeys();
                  if (this.mc.player.isUsingItem()) {
                     this.mc.gameMode.releaseUsingItem(this.mc.player);
                  }

                  this.mc.options.keyUse.setDown(false);
                  this.aim.aimAt(point);
                  this.aim.tick();
                  this.status = "Extinguish fire before combat and rod collection";
                  Vec3 look = eye.add(
                     Vec3.directionFromRotation(this.mc.player.getXRot(), this.mc.player.getYRot())
                        .scale(this.mc.player.blockInteractionRange() - 0.1)
                  );
                  if (new AABB(best).clip(eye, look).isEmpty()) {
                     return true;
                  }

                  this.revisionBefore = AiServerEvidence.blockRevision(best);
                  this.fireTarget = best;
                  this.waitTicks = 0;
                  this.mc.gameMode.startDestroyBlock(best, Direction.UP);
                  Compat263.swing(this.mc.player, InteractionHand.MAIN_HAND);
                  this.mc.player.resetAttackStrengthTicker();
                  AiRunTrace.emitServiceEvent("blaze_fire_extinguish", AiJson.objectOf("at", best.toShortString(), "priority", "before-combat-and-loot"));
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   private void releaseKeys() {
      for (KeyMapping k : new KeyMapping[]{
         this.mc.options.keyUp,
         this.mc.options.keyDown,
         this.mc.options.keyLeft,
         this.mc.options.keyRight,
         this.mc.options.keyJump,
         this.mc.options.keySprint
      }) {
         k.setDown(false);
      }
   }

   void reset() {
      this.aim.clear();
      if (this.approach != null) {
         this.approach.cancel();
      }

      this.approach = null;
      this.fireTarget = null;
   }
}
