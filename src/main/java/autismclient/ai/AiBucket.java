package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResult.Success;
import net.minecraft.world.InteractionResult.SwingSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiBucket {
   private static final int init = 60;
   private static final int MAX_WAIT_TICKS = 40;

   private AiBucket() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiBucket.Inner_DataUtil action = parseAction(AiJson.getString(args, "action", "fill"));
         final String fluid = AiJson.getString(args, "fluid", "water").trim().toLowerCase(Locale.ROOT);
         if (!fluid.equals("water") && !fluid.equals("lava")) {
            throw new IllegalArgumentException("fluid must be water or lava");
         }

         boolean hasTarget = args.has("x") && args.has("y") && args.has("z");
         final BlockPos explicit = hasTarget ? AiGameOperations.requireBlockPos(args) : null;
         final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 60)));
         if (action == AiBucket.Inner_DataUtil.EMPTY
            && fluid.equals("water")
            && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
            return AiGameOperations.immediate(
               AiOperation.Status.FAILED,
               "Water evaporates in this dimension; the water bucket stays full",
               AiJson.objectOf("refused", "water_evaporates")
            );
         }

         final String bucketId = action == AiBucket.Inner_DataUtil.FILL ? "minecraft:bucket" : "minecraft:" + fluid + "_bucket";
         final String resultId = action == AiBucket.Inner_DataUtil.FILL ? "minecraft:" + fluid + "_bucket" : "minecraft:bucket";
         return new AiOperation() {
            final Object level = mc.level;
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final int resultCountBefore = AiGameState.countInventoryItem(resultId);
            BlockPos target;
            int load_I;
            int waitTicks;
            long inventoryRevision;
            boolean init_Z;
            boolean load_Z;

            @Override
            public boolean canInterruptNow() {
               return !this.init_Z || this.load_Z;
            }

            @Override
            public AiOperation.Result tick() {
               if (this.load_Z || mc.player == null || mc.level == null) {
                  return this.finish(AiOperation.Status.CANCELLED, "Bucket use cancelled");
               }

               if (mc.level != this.level) {
                  return this.finish(AiOperation.Status.CANCELLED, "Bucket target belongs to the previous dimension");
               }

               if (action == AiBucket.Inner_DataUtil.EMPTY
                  && fluid.equals("water")
                  && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                  return this.finish(AiOperation.Status.FAILED, "Water placement refused by the current dimension rule");
               }

               if (System.nanoTime() > this.deadline) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Bucket use did not complete before the deadline");
               }

               if (this.init_Z) {
                  if (AiGameState.countInventoryItem(resultId) > this.resultCountBefore && AiServerEvidence.inventoryRevision() > this.inventoryRevision) {
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        action == AiBucket.Inner_DataUtil.FILL ? "Filled a bucket with " + fluid : "Emptied the " + fluid + " bucket"
                     );
                  } else {
                     return ++this.waitTicks > 40
                        ? this.finish(AiOperation.Status.UNCERTAIN, "The click went out but the held bucket did not change; reobserve before repeating")
                        : this.running("Waiting for the bucket to change");
                  }
               } else {
                  if (AiGameState.countInventoryItem(bucketId) <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty(bucketId, 1);
                     return this.finishWith(AiOperation.Status.FAILED, "No " + bucketId + " in inventory", AiJson.objectOf("missing", missing));
                  }

                  if (AiItems.equipHotbar(AiItems.resolveItem(bucketId)) < 0) {
                     return this.finish(AiOperation.Status.FAILED, "Could not hold the " + bucketId);
                  }

                  if (this.target == null) {
                     this.target = action == AiBucket.Inner_DataUtil.FILL ? this.findFillTarget() : this.findEmptyTarget();
                     this.load_I = 0;
                  }

                  if (this.target == null) {
                     return this.finish(
                        AiOperation.Status.FAILED,
                        action == AiBucket.Inner_DataUtil.FILL
                           ? "No " + fluid + " SOURCE block within reach (flowing " + fluid + " cannot be picked up)"
                           : "No free cell within reach to empty the bucket into"
                     );
                  }

                  Vec3 point = this.aimPoint();
                  if (point == null) {
                     return this.finish(AiOperation.Status.FAILED, "No visible source face within reach at " + this.target.toShortString());
                  }

                  this.aim.setTargetRotation(autismclient.util.RotationMath.lookAt(point, mc.player.getEyePosition()));
                  this.aim.tick();
                  if (++this.load_I > 60) {
                     return this.finish(AiOperation.Status.FAILED, "Could not aim at " + this.target.toShortString());
                  }

                  BlockHitResult hit = this.raycast(action == AiBucket.Inner_DataUtil.FILL ? Fluid.SOURCE_ONLY : Fluid.NONE);
                  boolean waterlog = fluid.equals("water") && AiBucket.isWaterloggable(mc.level.getBlockState(this.target));
                  if (!AiBucket.hitMatchesWaterlog(action, hit, this.target, waterlog)) {
                     return this.running("Turning toward " + this.target.toShortString());
                  }

                  if (action == AiBucket.Inner_DataUtil.EMPTY
                     && fluid.equals("water")
                     && !waterlog
                     && AiBucket.isWaterloggable(mc.level.getBlockState(hit.getBlockPos()))) {
                     return this.running("Turning toward " + this.target.toShortString());
                  }

                  if (!this.init_Z_9()) {
                     return this.running("Writing the look before using the bucket");
                  }

                  if (action == AiBucket.Inner_DataUtil.EMPTY && fluid.equals("lava")) {
                     mc.options.keyUp.setDown(false);
                     mc.options.keyDown.setDown(false);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     mc.options.keyJump.setDown(false);
                     mc.options.keySprint.setDown(false);
                     Vec3 motion = mc.player.getDeltaMovement();
                     if (!mc.player.onGround() || motion.x * motion.x + motion.z * motion.z > 1.0E-4) {
                        return this.running("Settling before the lava pour");
                     }

                     BlockPos feet = mc.player.blockPosition();

                     for (BlockPos body : List.of(feet, feet.above())) {
                        if (body.distManhattan(this.target) <= 1) {
                           return this.finish(
                              AiOperation.Status.FAILED, "Refusing a lava pour into or beside the player's own cells at " + this.target.toShortString()
                           );
                        }
                     }
                  }

                  this.inventoryRevision = AiServerEvidence.inventoryRevision();
                  InteractionResult used = mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                  if (Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] bucket {} {} at {}: result {}, eye {}, pose {}, held {}, fluid {}",
                           action,
                           fluid,
                           this.target.toShortString(),
                           used,
                           mc.player.getEyePosition(),
                           mc.player.getPose(),
                           BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()),
                           mc.level.getFluidState(this.target)
                        );
                  }

                  if (used instanceof Success success && success.swingSource() == SwingSource.PREDICTED) {
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  }

                  this.init_Z = true;
                  return this.running("Using the bucket");
               }
            }

            boolean init_Z_9() {
               autismclient.util.ServerRotationTracker.RotationSnapshot wire = autismclient.util.ServerRotationTracker.getSnapshot();
               return wire.initialized() && AiBucket.wireMatchesPlayer(wire.currentYaw(), wire.currentPitch(), mc.player.getYRot(), mc.player.getXRot());
            }

            Vec3 aimPoint() {
               if (action == AiBucket.Inner_DataUtil.FILL) {
                  if (!this.isSourceFluid(this.target)) {
                     return null;
                  }

                  Vec3 eye = mc.player.getEyePosition();
                  double reach = Math.max(4.5, mc.player.blockInteractionRange());
                  return AiBucket.visiblePoint(this.target, point -> {
                     if (eye.distanceToSqr(point) > reach * reach) {
                        return false;
                     }

                     BlockHitResult hitx = mc.level.clip(new ClipContext(eye, point, Block.OUTLINE, Fluid.SOURCE_ONLY, mc.player));
                     return hitx.getType() == Type.BLOCK && hitx.getBlockPos().equals(this.target);
                  });
               } else {
                  BlockHitResult hit = AiBucket.init(
                     mc, this.target, fluid.equals("water") && AiBucket.isWaterloggable(mc.level.getBlockState(this.target)), fluid.equals("water")
                  );
                  return hit == null ? null : hit.getLocation();
               }
            }

            BlockHitResult raycast(Fluid fluidMode) {
               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               Vec3 eye = mc.player.getEyePosition();
               Vec3 end = eye.add(mc.player.getLookAngle().scale(reach));
               BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, fluidMode, mc.player));
               return hit != null && hit.getType() == Type.BLOCK ? hit : null;
            }

            BlockPos findFillTarget() {
               if (explicit != null) {
                  return this.isSourceFluid(explicit) ? explicit : null;
               }

               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               int radius = (int)Math.ceil(reach);
               BlockPos origin = mc.player.blockPosition();
               BlockPos best = null;
               double bestDistance = reach * reach;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
                  if (this.isSourceFluid(pos)) {
                     double distance = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            boolean isSourceFluid(BlockPos pos) {
               if (!AiTerrain.isLoaded(mc.level, pos)) {
                  return false;
               } else {
                  FluidState state = mc.level.getFluidState(pos);
                  if (!state.isSource()) {
                     return false;
                  } else {
                     return fluid.equals("water") ? state.is(FluidTags.WATER) : state.is(FluidTags.LAVA);
                  }
               }
            }

            BlockPos findEmptyTarget() {
               if (explicit != null) {
                  return explicit;
               }

               BlockPos feet = mc.player.blockPosition();

               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos candidate = feet.relative(side);
                  if (AiTerrain.isLoaded(mc.level, candidate)) {
                     BlockState state = mc.level.getBlockState(candidate);
                     if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                        BlockPos below = candidate.below();
                        if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                           return candidate;
                        }
                     }
                  }
               }

               return null;
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf(
                  "action",
                  action.name().toLowerCase(Locale.ROOT),
                  "fluid",
                  fluid,
                  "holding",
                  bucketId,
                  "expecting",
                  resultId,
                  "have",
                  AiGameState.countInventoryItem(resultId),
                  "startedWith",
                  this.resultCountBefore
               );
               if (this.target != null) {
                  out.addProperty("target", this.target.toShortString());
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finishWith(status, message, null);
            }

            AiOperation.Result finishWith(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.progressJson();
               if (extra != null) {
                  for (Entry<String, JsonElement> entry : extra.entrySet()) {
                     data.add(entry.getKey(), entry.getValue());
                  }
               }

               this.cancel();
               return new AiOperation.Result(status, message, data.toString(), List.of());
            }

            @Override
            public void cancel() {
               if (!this.load_Z) {
                  this.load_Z = true;
                  this.aim.clear();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Bucket use requires a connected living player");
      }
   }

   static boolean wireMatchesPlayer(float wireYaw, float wirePitch, float playerYaw, float playerPitch) {
      return Float.isFinite(wireYaw) && Float.isFinite(wirePitch) && Float.isFinite(playerYaw) && Float.isFinite(playerPitch)
         ? Math.abs(Mth.wrapDegrees(wireYaw - playerYaw)) <= 0.05F && Math.abs(wirePitch - playerPitch) <= 0.05F
         : false;
   }

   static boolean hitMatches(AiBucket.Inner_DataUtil action, BlockHitResult hit, BlockPos target) {
      return hitMatchesWaterlog(action, hit, target, false);
   }

   static boolean hitMatchesWaterlog(AiBucket.Inner_DataUtil action, BlockHitResult hit, BlockPos target, boolean waterlog) {
      if (hit == null) {
         return false;
      }

      boolean var10000;
      switch (action) {
         case FILL:
            var10000 = hit.getBlockPos().equals(target);
            break;
         case EMPTY:
            var10000 = waterlog ? hit.getBlockPos().equals(target) : hit.getBlockPos().relative(hit.getDirection()).equals(target);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static boolean isWaterloggable(BlockState state) {
      BooleanProperty property = BlockStateProperties.WATERLOGGED;
      return state.getBlock() instanceof LiquidBlockContainer && state.hasProperty(property) && !state.getValue(property);
   }

   static BlockHitResult unusedRaycastTarget(Minecraft mc, BlockPos target, boolean waterlog) {
      return init(mc, target, waterlog, false);
   }

   static BlockHitResult init(Minecraft mc, BlockPos target, boolean waterlog, boolean water) {
      if (mc.player != null && mc.level != null) {
         if (waterlog) {
            return autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, target);
         }

         BlockHitResult best = null;

         for (Direction face : Direction.values()) {
            if (!water || !isWaterloggable(mc.level.getBlockState(target.relative(face.getOpposite())))) {
               BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, target.relative(face.getOpposite()), face);
               if (hitMatches(AiBucket.Inner_DataUtil.EMPTY, hit, target)
                  && (
                     best == null || mc.player.getEyePosition().distanceToSqr(hit.getLocation()) < mc.player.getEyePosition().distanceToSqr(best.getLocation())
                  )) {
                  best = hit;
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static Vec3 visiblePoint(BlockPos source, Predicate<Vec3> visible) {
      for (double x : new double[]{0.5, 0.2, 0.8}) {
         for (double z : new double[]{0.5, 0.2, 0.8}) {
            Vec3 point = new Vec3(source.getX() + x, source.getY() + 0.8, source.getZ() + z);
            if (visible.test(point)) {
               return point;
            }
         }
      }

      return null;
   }

   private static AiBucket.Inner_DataUtil parseAction(String text) {
      try {
         return AiBucket.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be fill or empty");
      }
   }

   static String bucketItemId(String fluid) {
      return BuiltInRegistries.ITEM.getKey(fluid.equals("lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET).toString();
   }

   enum Inner_DataUtil {
      FILL,
      EMPTY;
   }
}
