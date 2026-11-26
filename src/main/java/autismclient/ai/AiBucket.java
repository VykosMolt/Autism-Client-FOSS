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
   private static final int load = 40;

   private AiBucket() {
   }

   static AiOperation init(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final AiBucket.Inner_DataUtil action = init_TextFormat_DataUti(AiJson.init_Str_JsonObject(args, "action", "fill"));
         final String fluid = AiJson.init_Str_JsonObject(args, "fluid", "water").trim().toLowerCase(Locale.ROOT);
         if (!fluid.equals("water") && !fluid.equals("lava")) {
            throw new IllegalArgumentException("fluid must be water or lava");
         }

         boolean hasTarget = args.has("x") && args.has("y") && args.has("z");
         final BlockPos explicit = hasTarget ? AiGameOperations.init_BlockPos(args) : null;
         final int timeoutSeconds = Math.max(5, Math.min(300, AiJson.init_I_JsonObject(args, "timeoutSeconds", 60)));
         if (action == AiBucket.Inner_DataUtil.EMPTY
            && fluid.equals("water")
            && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
            return AiGameOperations.init_AiOperation(
               AiOperation.Status.FAILED,
               "Water evaporates in this dimension; the water bucket stays full",
               AiJson.init_JsonObject_ObjArr("refused", "water_evaporates")
            );
         }

         final String bucketId = action == AiBucket.Inner_DataUtil.FILL ? "minecraft:bucket" : "minecraft:" + fluid + "_bucket";
         final String resultId = action == AiBucket.Inner_DataUtil.FILL ? "minecraft:" + fluid + "_bucket" : "minecraft:bucket";
         return new AiOperation() {
            final Object init_Obj = mc.level;
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final int init_I = AiGameState.init_I_Str(resultId);
            BlockPos init_BlockPos;
            int load_I;
            int read;
            long load_J;
            boolean init_Z;
            boolean load_Z;

            @Override
            public boolean canInterruptNow() {
               return !this.init_Z || this.load_Z;
            }

            @Override
            public AiOperation.Result tick() {
               if (this.load_Z || mc.player == null || mc.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, "Bucket use cancelled");
               }

               if (mc.level != this.init_Obj) {
                  return this.init(AiOperation.Status.CANCELLED, "Bucket target belongs to the previous dimension");
               }

               if (action == AiBucket.Inner_DataUtil.EMPTY
                  && fluid.equals("water")
                  && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)) {
                  return this.init(AiOperation.Status.FAILED, "Water placement refused by the current dimension rule");
               }

               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Bucket use did not complete before the deadline");
               }

               if (this.init_Z) {
                  if (AiGameState.init_I_Str(resultId) > this.init_I && AiServerEvidence.init_J() > this.load_J) {
                     return this.init(
                        AiOperation.Status.SUCCEEDED,
                        action == AiBucket.Inner_DataUtil.FILL ? "Filled a bucket with " + fluid : "Emptied the " + fluid + " bucket"
                     );
                  } else {
                     return ++this.read > 40
                        ? this.init(AiOperation.Status.UNCERTAIN, "The click went out but the held bucket did not change; reobserve before repeating")
                        : this.init("Waiting for the bucket to change");
                  }
               } else {
                  if (AiGameState.init_I_Str(bucketId) <= 0) {
                     JsonObject missing = new JsonObject();
                     missing.addProperty(bucketId, 1);
                     return this.init(AiOperation.Status.FAILED, "No " + bucketId + " in inventory", AiJson.init_JsonObject_ObjArr("missing", missing));
                  }

                  if (AiItems.read(AiItems.load_af_DataUtil(bucketId)) < 0) {
                     return this.init(AiOperation.Status.FAILED, "Could not hold the " + bucketId);
                  }

                  if (this.init_BlockPos == null) {
                     this.init_BlockPos = action == AiBucket.Inner_DataUtil.FILL ? this.init_BlockPos_4() : this.load();
                     this.load_I = 0;
                  }

                  if (this.init_BlockPos == null) {
                     return this.init(
                        AiOperation.Status.FAILED,
                        action == AiBucket.Inner_DataUtil.FILL
                           ? "No " + fluid + " SOURCE block within reach (flowing " + fluid + " cannot be picked up)"
                           : "No free cell within reach to empty the bucket into"
                     );
                  }

                  Vec3 point = this.init_Vec3_4();
                  if (point == null) {
                     return this.init(AiOperation.Status.FAILED, "No visible source face within reach at " + this.init_BlockPos.toShortString());
                  }

                  this.init_DataUtil.init_V_ce_DataUtil(autismclient.util.RotationMath.lookAt(point, mc.player.getEyePosition()));
                  this.init_DataUtil.load_V();
                  if (++this.load_I > 60) {
                     return this.init(AiOperation.Status.FAILED, "Could not aim at " + this.init_BlockPos.toShortString());
                  }

                  BlockHitResult hit = this.init(action == AiBucket.Inner_DataUtil.FILL ? Fluid.SOURCE_ONLY : Fluid.NONE);
                  boolean waterlog = fluid.equals("water") && AiBucket.init(mc.level.getBlockState(this.init_BlockPos));
                  if (!AiBucket.init(action, hit, this.init_BlockPos, waterlog)) {
                     return this.init("Turning toward " + this.init_BlockPos.toShortString());
                  }

                  if (action == AiBucket.Inner_DataUtil.EMPTY
                     && fluid.equals("water")
                     && !waterlog
                     && AiBucket.init(mc.level.getBlockState(hit.getBlockPos()))) {
                     return this.init("Turning toward " + this.init_BlockPos.toShortString());
                  }

                  if (!this.init_Z_9()) {
                     return this.init("Writing the look before using the bucket");
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
                        return this.init("Settling before the lava pour");
                     }

                     BlockPos feet = mc.player.blockPosition();

                     for (BlockPos body : List.of(feet, feet.above())) {
                        if (body.distManhattan(this.init_BlockPos) <= 1) {
                           return this.init(
                              AiOperation.Status.FAILED, "Refusing a lava pour into or beside the player's own cells at " + this.init_BlockPos.toShortString()
                           );
                        }
                     }
                  }

                  this.load_J = AiServerEvidence.init_J();
                  InteractionResult used = mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                  if (Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] bucket {} {} at {}: result {}, eye {}, pose {}, held {}, fluid {}",
                           action,
                           fluid,
                           this.init_BlockPos.toShortString(),
                           used,
                           mc.player.getEyePosition(),
                           mc.player.getPose(),
                           BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()),
                           mc.level.getFluidState(this.init_BlockPos)
                        );
                  }

                  if (used instanceof Success success && success.swingSource() == SwingSource.PREDICTED) {
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                  }

                  this.init_Z = true;
                  return this.init("Using the bucket");
               }
            }

            boolean init_Z_9() {
               autismclient.util.ServerRotationTracker.RotationSnapshot wire = autismclient.util.ServerRotationTracker.init_ch_CachePool();
               return wire.init_Z() && AiBucket.init(wire.read(), wire.write(), mc.player.getYRot(), mc.player.getXRot());
            }

            Vec3 init_Vec3_4() {
               if (action == AiBucket.Inner_DataUtil.FILL) {
                  if (!this.init(this.init_BlockPos)) {
                     return null;
                  }

                  Vec3 eye = mc.player.getEyePosition();
                  double reach = Math.max(4.5, mc.player.blockInteractionRange());
                  return AiBucket.init(this.init_BlockPos, point -> {
                     if (eye.distanceToSqr(point) > reach * reach) {
                        return false;
                     }

                     BlockHitResult hitx = mc.level.clip(new ClipContext(eye, point, Block.OUTLINE, Fluid.SOURCE_ONLY, mc.player));
                     return hitx.getType() == Type.BLOCK && hitx.getBlockPos().equals(this.init_BlockPos);
                  });
               } else {
                  BlockHitResult hit = AiBucket.init(
                     mc, this.init_BlockPos, fluid.equals("water") && AiBucket.init(mc.level.getBlockState(this.init_BlockPos)), fluid.equals("water")
                  );
                  return hit == null ? null : hit.getLocation();
               }
            }

            BlockHitResult init(Fluid fluidMode) {
               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               Vec3 eye = mc.player.getEyePosition();
               Vec3 end = eye.add(mc.player.getLookAngle().scale(reach));
               BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, Block.OUTLINE, fluidMode, mc.player));
               return hit != null && hit.getType() == Type.BLOCK ? hit : null;
            }

            BlockPos init_BlockPos_4() {
               if (explicit != null) {
                  return this.init(explicit) ? explicit : null;
               }

               double reach = Math.max(4.5, mc.player.blockInteractionRange());
               int radius = (int)Math.ceil(reach);
               BlockPos origin = mc.player.blockPosition();
               BlockPos best = null;
               double bestDistance = reach * reach;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
                  if (this.init(pos)) {
                     double distance = mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos));
                     if (distance < bestDistance) {
                        bestDistance = distance;
                        best = pos.immutable();
                     }
                  }
               }

               return best;
            }

            boolean init(BlockPos pos) {
               if (!AiTerrain.init(mc.level, pos)) {
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

            BlockPos load() {
               if (explicit != null) {
                  return explicit;
               }

               BlockPos feet = mc.player.blockPosition();

               for (Direction side : Plane.HORIZONTAL) {
                  BlockPos candidate = feet.relative(side);
                  if (AiTerrain.init(mc.level, candidate)) {
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

            JsonObject init_JsonObject_20() {
               JsonObject out = AiJson.init_JsonObject_ObjArr(
                  "action",
                  action.name().toLowerCase(Locale.ROOT),
                  "fluid",
                  fluid,
                  "holding",
                  bucketId,
                  "expecting",
                  resultId,
                  "have",
                  AiGameState.init_I_Str(resultId),
                  "startedWith",
                  this.init_I
               );
               if (this.init_BlockPos != null) {
                  out.addProperty("target", this.init_BlockPos.toShortString());
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_20().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, null);
            }

            AiOperation.Result init(AiOperation.Status status, String message, JsonObject extra) {
               JsonObject data = this.init_JsonObject_20();
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
                  this.init_DataUtil.init_V();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Bucket use requires a connected living player");
      }
   }

   static boolean init(float wireYaw, float wirePitch, float playerYaw, float playerPitch) {
      return Float.isFinite(wireYaw) && Float.isFinite(wirePitch) && Float.isFinite(playerYaw) && Float.isFinite(playerPitch)
         ? Math.abs(Mth.wrapDegrees(wireYaw - playerYaw)) <= 0.05F && Math.abs(wirePitch - playerPitch) <= 0.05F
         : false;
   }

   static boolean init(AiBucket.Inner_DataUtil action, BlockHitResult hit, BlockPos target) {
      return init(action, hit, target, false);
   }

   static boolean init(AiBucket.Inner_DataUtil action, BlockHitResult hit, BlockPos target, boolean waterlog) {
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

   static boolean init(BlockState state) {
      BooleanProperty property = BlockStateProperties.WATERLOGGED;
      return state.getBlock() instanceof LiquidBlockContainer && state.hasProperty(property) && !state.getValue(property);
   }

   static BlockHitResult init(Minecraft mc, BlockPos target, boolean waterlog) {
      return init(mc, target, waterlog, false);
   }

   static BlockHitResult init(Minecraft mc, BlockPos target, boolean waterlog, boolean water) {
      if (mc.player != null && mc.level != null) {
         if (waterlog) {
            return autismclient.ai.engine.AiBlockInteraction.init(mc, target);
         }

         BlockHitResult best = null;

         for (Direction face : Direction.values()) {
            if (!water || !init(mc.level.getBlockState(target.relative(face.getOpposite())))) {
               BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.init(mc, target.relative(face.getOpposite()), face);
               if (init(AiBucket.Inner_DataUtil.EMPTY, hit, target)
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

   static Vec3 init(BlockPos source, Predicate<Vec3> visible) {
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

   private static AiBucket.Inner_DataUtil init_TextFormat_DataUti(String text) {
      try {
         return AiBucket.Inner_DataUtil.valueOf(text.trim().toUpperCase(Locale.ROOT));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("action must be fill or empty");
      }
   }

   static String init_Str(String fluid) {
      return BuiltInRegistries.ITEM.getKey(fluid.equals("lava") ? Items.LAVA_BUCKET : Items.WATER_BUCKET).toString();
   }

   enum Inner_DataUtil {
      FILL,
      EMPTY;
   }
}
