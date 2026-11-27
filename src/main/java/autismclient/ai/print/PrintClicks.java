package autismclient.ai.print;

import autismclient.ai.engine.AiPistonPower;
import autismclient.ai.engine.AiPlacementContext;
import autismclient.ai.engine.PlacementGoal;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MinecartItem;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CartographyTableBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.LoomBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SmithingTableBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class PrintClicks {
   private static final double[] FACE_FRACTIONS_HORIZ = new double[]{0.5, 0.2, 0.8};
   private static final double[] FACE_FRACTIONS_VERT = new double[]{0.3, 0.7, 0.12, 0.88};
   private static final double[] FACE_FRACTIONS_EDGE = new double[]{0.5, 0.2, 0.8, 0.04, 0.96, 0.0, 1.0};
   static final double DEFAULT_CLICK_REACH = 4.1000000000000005;
   private static final double[][] COARSE_FEET_OFFSETS = new double[][]{{0.0, 0.0}, {0.75, 0.0}, {-0.75, 0.0}, {0.0, 0.75}, {0.0, -0.75}};
   private static final double[][] DETAILED_FEET_OFFSETS = buildDetailedFeetOffsets();

   private PrintClicks() {
   }

   static boolean isServerLookAlignedWithTarget(PrintView view, PrintEnv env, PrintTarget target, PrintClicks.Inner_CachePool mode) {
      Vec3 eye = env.serverEyePos();
      BlockHitResult hit = view.raycastOutline(eye, eye.add(PrintGeometry.init_Vec3(env.serverYaw(), env.serverPitch()).scale(4.5)));
      if (hit != null && !(hit.getLocation().distanceTo(eye) > 4.4)) {
         BlockState state = view.getBlockState(hit.getBlockPos());
         boolean waterloggable = state.getBlock() instanceof LiquidBlockContainer && getFluidBucketItem(target) == Items.WATER_BUCKET;
         BlockPos destination = waterloggable ? hit.getBlockPos() : hit.getBlockPos().relative(hit.getDirection());
         return destination.equals(target.pos()) && (mode != PrintClicks.Inner_CachePool.WATERLOG || waterloggable);
      } else {
         return false;
      }
   }

   static boolean isOrientationSensitive(PrintTarget target, PlacementGoal.PlaceConstraint constraint) {
      BlockState state = target.wanted();
      return PrintCore.sync_Z_BlockState(state)
         || state.getBlock() instanceof HopperBlock
         || state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
         || state.hasProperty(BlockStateProperties.HALF)
         || state.hasProperty(BlockStateProperties.SLAB_TYPE)
         || state.hasProperty(BlockStateProperties.AXIS)
         || state.hasProperty(BlockStateProperties.ROTATION_16)
         || constraint != null
            && (
               constraint.playerFacing() != null
                  || constraint.clickFace() != null
                  || constraint.highHalf() != null
                  || constraint.axis() != null
                  || constraint.horizontalFace() != null
            );
   }

   static boolean isValidHopperClickFace(BlockState wanted, Direction face) {
      if (wanted.getBlock().getClass() != HopperBlock.class) {
         return true;
      }

      Direction outlet = wanted.getValue(BlockStateProperties.FACING_HOPPER);
      return outlet == Direction.DOWN ? face.getAxis() == Axis.Y : outlet == face.getOpposite();
   }

   private static boolean isReplaceableTargetState(BlockState state) {
      return state != PrintView.UNLOADED_SENTINEL && (state.canBeReplaced() || state.isAir());
   }

   private static boolean isValidSupportState(BlockState state) {
      return state != PrintView.UNLOADED_SENTINEL && !state.isAir() && !state.canBeReplaced();
   }

   private static boolean acceptsSupportFace(BlockState wanted, Direction face, PlacementGoal.@Nullable PlaceConstraint constraint) {
      return wanted.getBlock() instanceof MultifaceBlock && !MultifaceBlock.hasFace(wanted, face.getOpposite())
         ? false
         : isValidHopperClickFace(wanted, face) && (constraint == null || constraint.acceptsFace(face));
   }

   static boolean hasPlaceableSupport(PrintView view, PrintTarget target, PlacementGoal.@Nullable PlaceConstraint constraint, @Nullable BlockPos requiredSupport) {
      if (!isReplaceableTargetState(view.getBlockState(target.pos()))) {
         return false;
      }

      for (Direction direction : Direction.values()) {
         BlockPos support = target.pos().relative(direction);
         if ((requiredSupport == null || support.equals(requiredSupport)) && acceptsSupportFace(target.wanted(), direction.getOpposite(), constraint)) {
            BlockState state = view.getBlockState(support);
            if (isValidSupportState(state) && !view.getOutlineBoxes(state, support).isEmpty()) {
               return true;
            }
         }
      }

      return false;
   }

   static @Nullable Direction getRequiredLookDirection(BlockState wanted) {
      Class<?> type = wanted.getBlock().getClass();
      if (type == ObserverBlock.class) {
         return wanted.getValue(BlockStateProperties.FACING);
      } else if (type != PistonBaseBlock.class && type != DispenserBlock.class && type != DropperBlock.class && type != BarrelBlock.class) {
         return type == CrafterBlock.class ? wanted.getValue(BlockStateProperties.ORIENTATION).front().getOpposite() : null;
      } else {
         return wanted.getValue(BlockStateProperties.FACING).getOpposite();
      }
   }

   static PrintClicks.@Nullable Inner_StreamUtil findClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget
   ) {
      return findClick(view, env, rules, target, item, mode, feet, crouch, exact, budget, 4.4);
   }

   static PrintClicks.@Nullable Inner_StreamUtil findClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach
   ) {
      return findClick(view, env, rules, target, item, mode, feet, crouch, exact, budget, reach, null);
   }

   static boolean isClickValidFromFeet(PrintView view, PrintEnv env, PrintRules rules, PrintClicks.Inner_StreamUtil click, Vec3 feet, double reach) {
      if (isOrientationSensitive(click.target(), null)) {
         PrintClicks.Inner_StreamUtil aligned = tryClickWithLook(
            view,
            env,
            rules,
            click.target(),
            click.item(),
            feet,
            click.yaw(),
            click.pitch(),
            click.crouch(),
            true,
            new PrintClicks.Inner_DataUtil(1),
            reach,
            null
         );
         if (aligned != null && aligned.support().equals(click.support()) && aligned.face() == click.face()) {
            return true;
         }
      }

      Vec3 eye = PrintGeometry.init(feet, false);
      Vec3 point = click.result().getLocation();
      if (eye.distanceToSqr(point) > reach * reach) {
         return false;
      }

      float yaw = PrintGeometry.init(eye, point);
      float pitch = PrintGeometry.load(eye, point);
      Direction required = getRequiredLookDirection(click.target().wanted());
      if (required != null && AiPlacementContext.init(yaw, pitch)[0] != required) {
         return false;
      }

      Vec3 direction = point.subtract(eye).normalize();
      BlockHitResult hit = view.raycastBlock(eye, point.add(direction.scale(0.02)));
      if (hit != null && hit.getBlockPos().equals(click.support()) && hit.getDirection() == click.face() && !(hit.getLocation().distanceToSqr(point) >= 4.0E-4)
         )
       {
         RailShape rail = getRequiredUnconnectedRailShape(view, click.target());
         if (rail != null && Direction.fromYRot(yaw).getAxis() != (rail == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X)) {
            return false;
         }

         if (isOrientationSensitive(click.target(), null)) {
            BlockState predicted = env.predictPlacedState(click.item(), hit, yaw, pitch, click.crouch(), click.target().pos());
            if (predicted == null || !matchesPredictedState(view, rules, click.target(), predicted, rail)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static PrintClicks.@Nullable Inner_StreamUtil tryClickWithLook(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      Vec3 feet,
      float yaw,
      float pitch,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.@Nullable PlaceConstraint constraint
   ) {
      if (!budget.isExhausted() && isReplaceableTargetState(view.getBlockState(target.pos()))) {
         boolean compact = crouch && !env.canUseFlight();
         AABB body = PrintGeometry.init(feet.x, feet.y, feet.z, compact);
         if (!budget.isBodyClear(view, body)) {
            return null;
         }

         Vec3 eye = PrintGeometry.init(feet, compact);
         budget.raycastCount++;
         BlockHitResult hit = view.raycastBlock(eye, eye.add(PrintGeometry.init_Vec3(yaw, pitch).scale(reach)));
         if (hit != null
            && hit.getBlockPos().relative(hit.getDirection()).equals(target.pos())
            && !(hit.getLocation().distanceTo(eye) > reach)
            && isValidSupportState(view.getBlockState(hit.getBlockPos()))
            && acceptsSupportFace(target.wanted(), hit.getDirection(), constraint)
            && satisfiesPlaceConstraint(constraint, hit.getDirection(), eye, hit.getLocation(), hit.getBlockPos())) {
            Direction required = getRequiredLookDirection(target.wanted());
            if (required != null && AiPlacementContext.init(yaw, pitch)[0] != required) {
               return null;
            }

            RailShape shape = getRequiredUnconnectedRailShape(view, target);
            if (shape != null && Direction.fromYRot(yaw).getAxis() != (shape == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X)) {
               return null;
            }

            BlockState predicted;
            if (exact) {
               budget.predictCount++;
               predicted = env.predictPlacedState(item, hit, yaw, pitch, crouch, target.pos());
            } else {
               predicted = approximatePlacedState(view, target);
            }

            return predicted != null && matchesPredictedState(view, rules, target, predicted, shape) && !collidesWithPlacedState(view, predicted, target.pos(), body)
               ? new PrintClicks.Inner_StreamUtil(
                  target, PrintClicks.Inner_CachePool.PLACE, item, hit.getBlockPos(), hit.getDirection(), hit, eye, feet, crouch, yaw, pitch, predicted
               )
               : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static boolean satisfiesPlaceConstraint(PlacementGoal.PlaceConstraint c, Direction face, Vec3 eye, Vec3 hit, BlockPos support) {
      if (c == null) {
         return true;
      }

      if (!c.acceptsFace(face)) {
         return false;
      }

      if (!c.acceptsRay(hit.x - eye.x, hit.y - eye.y, hit.z - eye.z)) {
         return false;
      }

      if (c.highHalf() != null && face.getAxis().isHorizontal()) {
         double frac = hit.y - Math.floor(hit.y);
         if (c.highHalf() != frac > 0.5) {
            return false;
         }
      }

      return true;
   }

   static PrintClicks.@Nullable Inner_StreamUtil findClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint
   ) {
      return findClick(view, env, rules, target, item, mode, feet, crouch, exact, budget, reach, constraint, null);
   }

   static PrintClicks.@Nullable Inner_StreamUtil findClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint,
      @Nullable BlockPos requiredSupport
   ) {
      return findClickCore(view, env, rules, target, item, mode, feet, crouch, exact, budget, reach, constraint, requiredSupport, env.canUseFlight());
   }

   static PrintClicks.@Nullable Inner_StreamUtil findGroundedClickAndPredict(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint,
      @Nullable BlockPos requiredSupport
   ) {
      PrintClicks.Inner_StreamUtil click = findClickCore(view, env, rules, target, item, mode, feet, crouch, false, budget, reach, constraint, requiredSupport, false);
      if (click != null && mode == PrintClicks.Inner_CachePool.PLACE && !view.hasHypotheses()) {
         BlockState placed = env.predictPlacedState(item, click.result(), click.yaw(), click.pitch(), click.crouch(), target.pos());
         if (placed != null && matchesPredictedState(view, rules, target, placed) && placed != click.predicted()) {
            boolean compact = click.eye().y - feet.y < 1.61;
            return collidesWithPlacedState(view, placed, target.pos(), PrintGeometry.init(feet.x, feet.y, feet.z, compact))
               ? click
               : new PrintClicks.Inner_StreamUtil(
                  target, mode, item, click.support(), click.face(), click.result(), click.eye(), feet, click.crouch(), click.yaw(), click.pitch(), placed
               );
         } else {
            return click;
         }
      } else {
         return click;
      }
   }

   private static PrintClicks.@Nullable Inner_StreamUtil findClickCore(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint,
      @Nullable BlockPos requiredSupport,
      boolean flightPose
   ) {
      boolean compact = crouch && !flightPose;
      AABB body = PrintGeometry.init(feet.x, feet.y, feet.z, compact);
      if (!budget.isBodyClear(view, body)) {
         if (compact || flightPose) {
            return null;
         }

         compact = true;
         body = PrintGeometry.init(feet.x, feet.y, feet.z, true);
         if (!budget.isBodyClear(view, body)) {
            return null;
         }
      }

      Vec3 eye = PrintGeometry.init(feet, compact);
      BlockPos cell = target.pos();
      if (eye.distanceToSqr(PrintView.blockCenter(cell)) > square(reach + 0.9)) {
         return null;
      }

      if (mode != PrintClicks.Inner_CachePool.INTERACT && mode != PrintClicks.Inner_CachePool.WATERLOG && mode != PrintClicks.Inner_CachePool.DIG) {
         BlockState here = view.getBlockState(cell);
         if (here == PrintView.UNLOADED_SENTINEL) {
            return null;
         }

         if (mode == PrintClicks.Inner_CachePool.PLACE && !isReplaceableTargetState(here)) {
            return null;
         }

         if (mode != PrintClicks.Inner_CachePool.POUR || here.isAir() || here.canBeReplaced() && !here.getFluidState().isSource()) {
            Direction requiredLook = mode == PrintClicks.Inner_CachePool.PLACE ? getRequiredLookDirection(target.wanted()) : null;
            RailShape railShape = mode == PrintClicks.Inner_CachePool.PLACE ? getRequiredUnconnectedRailShape(view, target) : null;
            int passes = mode == PrintClicks.Inner_CachePool.PLACE && isOrientationSensitive(target, constraint) ? 2 : 1;

            for (int pass = 0; pass < passes; pass++) {
               for (Direction d : Direction.values()) {
                  BlockPos support = cell.relative(d);
                  if (requiredSupport == null || support.equals(requiredSupport)) {
                     Direction face = d.getOpposite();
                     if (mode != PrintClicks.Inner_CachePool.PLACE || acceptsSupportFace(target.wanted(), face, exact ? null : constraint)) {
                        BlockState supportState = view.getBlockState(support);
                        if (isValidSupportState(supportState)
                           && (crouch || !isInteractableBlock(supportState))
                           && (
                              mode != PrintClicks.Inner_CachePool.POUR
                                 || !(supportState.getBlock() instanceof LiquidBlockContainer)
                                 || item != Items.WATER_BUCKET
                           )) {
                           List<AABB> outline = view.getOutlineBoxes(supportState, support);
                           if (!outline.isEmpty()) {
                              for (AABB box : outline) {
                                 AABB b = box.move(support);
                                 double plane = face.getAxisDirection() == AxisDirection.POSITIVE ? b.max(face.getAxis()) : b.min(face.getAxis());
                                 double eyeSide = getAxisCoordinate(eye, face.getAxis()) - plane;
                                 if (face.getAxisDirection() == AxisDirection.POSITIVE ? !(eyeSide <= 0.001) : !(eyeSide >= -0.001)) {
                                    for (Vec3 point : sampleFacePoints(b, face, plane, pass > 0)) {
                                       if (!(eye.distanceToSqr(point) > square(reach))
                                          && (mode != PrintClicks.Inner_CachePool.PLACE || exact || satisfiesPlaceConstraint(constraint, face, eye, point, support))) {
                                          float yaw = 0.0F;
                                          float pitch = 0.0F;
                                          if (requiredLook != null || railShape != null) {
                                             yaw = PrintGeometry.init(eye, point);
                                             pitch = PrintGeometry.load(eye, point);
                                             if (requiredLook != null && AiPlacementContext.init(yaw, pitch)[0] != requiredLook
                                                || railShape != null
                                                   && Direction.fromYRot(yaw).getAxis() != (railShape == RailShape.NORTH_SOUTH ? Axis.Z : Axis.X)) {
                                                continue;
                                             }
                                          }

                                          if (budget.isExhausted()) {
                                             return null;
                                          }

                                          budget.raycastCount++;
                                          Vec3 dir = point.subtract(eye).normalize();
                                          BlockHitResult hit = view.raycastBlock(eye, point.add(dir.scale(0.02)));
                                          if (hit != null
                                             && hit.getBlockPos().equals(support)
                                             && hit.getDirection() == face
                                             && !(hit.getLocation().distanceToSqr(point) > 4.0E-4)) {
                                             if (requiredLook == null && railShape == null) {
                                                yaw = PrintGeometry.init(eye, point);
                                                pitch = PrintGeometry.load(eye, point);
                                             }

                                             if (exact) {
                                                BlockHitResult wire = view.raycastBlock(
                                                   eye, eye.add(PrintGeometry.init_Vec3(yaw, pitch).scale(reach))
                                                );
                                                if (wire == null || !wire.getBlockPos().equals(support) || wire.getDirection() != face) {
                                                   continue;
                                                }

                                                hit = wire;
                                             }

                                             BlockState predicted = null;
                                             if (mode != PrintClicks.Inner_CachePool.PLACE) {
                                                return new PrintClicks.Inner_StreamUtil(
                                                   target, mode, item, support, face, hit, eye, feet, crouch, yaw, pitch, predicted
                                                );
                                             }

                                             if (exact) {
                                                budget.predictCount++;
                                                predicted = env.predictPlacedState(item, hit, yaw, pitch, crouch, cell);
                                                if (predicted == null || !matchesPredictedState(view, rules, target, predicted, railShape)) {
                                                   continue;
                                                }
                                             } else {
                                                predicted = approximatePlacedState(view, target);
                                             }

                                             if (!collidesWithPlacedState(view, predicted, cell, body)) {
                                                return new PrintClicks.Inner_StreamUtil(
                                                   target, mode, item, support, face, hit, eye, feet, crouch, yaw, pitch, predicted
                                                );
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return mode == PrintClicks.Inner_CachePool.INTERACT && !isSafeToInteract(view, target, body)
            ? null
            : findDirectBlockClick(view, env, rules, target, item, mode, eye, feet, crouch, exact, budget, reach);
      }
   }

   private static BlockState approximatePlacedState(PrintView view, PrintTarget target) {
      BlockState state = target.wanted();
      Block block = state.getBlock();
      if (block instanceof TrapDoorBlock || block instanceof FenceGateBlock || block instanceof DoorBlock) {
         boolean power = view.hasNeighborSignal(target.pos());
         if (block instanceof DoorBlock) {
            power |= view.hasNeighborSignal(target.pos().above());
         }

         state = state.setValue(BlockStateProperties.OPEN, power).setValue(BlockStateProperties.POWERED, power);
      }

      return state;
   }

   static boolean matchesPredictedState(PrintView view, PrintRules rules, PrintTarget target, BlockState predicted) {
      return matchesPredictedState(view, rules, target, predicted, getRequiredUnconnectedRailShape(view, target));
   }

   private static boolean matchesPredictedState(PrintView view, PrintRules rules, PrintTarget target, BlockState predicted, @Nullable RailShape railShape) {
      if (target.index() < 0 && target.cell() == null) {
         return target.wanted().getBlock() instanceof BaseRailBlock rail && predicted.is(rail)
            ? predicted.setValue(rail.getShapeProperty(), target.wanted().getValue(rail.getShapeProperty())) == target.wanted()
            : predicted == target.wanted();
      }

      if (!rules.load_Z_ListPool_BlockState(target, predicted)) {
         return false;
      }

      BlockState wanted = target.wanted();
      if (railShape != null && predicted.getValue(((BaseRailBlock)wanted.getBlock()).getShapeProperty()) != railShape) {
         return false;
      }

      if (wanted.getBlock() instanceof ChestBlock && predicted.getBlock() instanceof ChestBlock) {
         EnumProperty<ChestType> property = ChestBlock.TYPE;
         ChestType type = wanted.getValue(property);
         ChestType got = predicted.getValue(property);
         if (type == ChestType.SINGLE) {
            return got == type;
         }

         Direction facing = wanted.getValue(ChestBlock.FACING);
         BlockPos partner = target.pos().relative(type == ChestType.LEFT ? facing.getClockWise() : facing.getCounterClockWise());
         return view.getBlockState(partner).getBlock() instanceof ChestBlock ? got == type : got == ChestType.SINGLE;
      } else {
         return true;
      }
   }

   private static @Nullable RailShape getRequiredUnconnectedRailShape(PrintView view, PrintTarget target) {
      if (!(target.wanted().getBlock() instanceof BaseRailBlock rail)) {
         return null;
      } else {
         RailShape shape = target.wanted().getValue(rail.getShapeProperty());
         if (shape != RailShape.NORTH_SOUTH && shape != RailShape.EAST_WEST) {
            return null;
         }

         for (Direction side : Plane.HORIZONTAL) {
            BlockPos neighbor = target.pos().relative(side);

            for (int dy = -1; dy <= 1; dy++) {
               BlockState state = view.getBlockState(neighbor.offset(0, dy, 0));
               if (state == PrintView.UNLOADED_SENTINEL) {
                  return null;
               }

               if (state.getBlock() instanceof BaseRailBlock && neighborRailCanConnectToTarget(view, neighbor.offset(0, dy, 0), state, target.pos())) {
                  return null;
               }
            }
         }

         return shape;
      }
   }

   private static boolean neighborRailCanConnectToTarget(PrintView view, BlockPos at, BlockState state, BlockPos target) {
      BaseRailBlock rail = (BaseRailBlock)state.getBlock();
      Direction[] ends = getRailEndDirections(state.getValue(rail.getShapeProperty()));
      int connected = 0;

      for (Direction end : ends) {
         BlockPos neighbor = at.relative(end);
         if (neighbor.getX() == target.getX() && neighbor.getZ() == target.getZ()) {
            return true;
         }

         for (int dy : new int[]{0, 1, -1}) {
            BlockPos candidate = neighbor.offset(0, dy, 0);
            BlockState other = view.getBlockState(candidate);
            if (other == PrintView.UNLOADED_SENTINEL) {
               return true;
            }

            if (other.getBlock() instanceof BaseRailBlock next) {
               for (Direction toward : getRailEndDirections(other.getValue(next.getShapeProperty()))) {
                  if (toward == end.getOpposite()) {
                     connected++;
                     break;
                  }
               }
               break;
            }
         }
      }

      return connected < 2;
   }

   private static Direction[] getRailEndDirections(RailShape shape) {
      Direction[] var10000;
      switch (shape) {
         case NORTH_SOUTH:
         case ASCENDING_NORTH:
         case ASCENDING_SOUTH:
            var10000 = new Direction[]{Direction.NORTH, Direction.SOUTH};
            break;
         case EAST_WEST:
         case ASCENDING_EAST:
         case ASCENDING_WEST:
            var10000 = new Direction[]{Direction.EAST, Direction.WEST};
            break;
         case NORTH_EAST:
            var10000 = new Direction[]{Direction.NORTH, Direction.EAST};
            break;
         case NORTH_WEST:
            var10000 = new Direction[]{Direction.NORTH, Direction.WEST};
            break;
         case SOUTH_EAST:
            var10000 = new Direction[]{Direction.SOUTH, Direction.EAST};
            break;
         case SOUTH_WEST:
            var10000 = new Direction[]{Direction.SOUTH, Direction.WEST};
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static PrintClicks.@Nullable Inner_StreamUtil findPlacementClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Vec3 feet,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint
   ) {
      return findPlacementClick(view, env, rules, target, feet, exact, budget, reach, constraint, null);
   }

   static PrintClicks.@Nullable Inner_StreamUtil findPlacementClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Vec3 feet,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach,
      PlacementGoal.PlaceConstraint constraint,
      @Nullable BlockPos support
   ) {
      PrintClicks.Inner_StreamUtil click = findClick(
         view, env, rules, target, target.item(), PrintClicks.Inner_CachePool.PLACE, feet, true, exact, budget, reach, constraint, support
      );
      return click != null
         ? click
         : findClick(view, env, rules, target, target.item(), PrintClicks.Inner_CachePool.PLACE, feet, false, exact, budget, reach, constraint, support);
   }

   static @Nullable Direction getDoubleSlabMergeFace(BlockState live, BlockState wanted) {
      if (live.getBlock() instanceof SlabBlock
         && live.is(wanted.getBlock())
         && wanted.hasProperty(SlabBlock.TYPE)
         && wanted.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
         Direction var10000;
         switch ((SlabType)live.getValue(SlabBlock.TYPE)) {
            case BOTTOM:
               var10000 = Direction.UP;
               break;
            case TOP:
               var10000 = Direction.DOWN;
               break;
            case DOUBLE:
               var10000 = null;
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      } else {
         return null;
      }
   }

   private static PrintClicks.@Nullable Inner_StreamUtil findDirectBlockClick(
      PrintView view,
      PrintEnv env,
      PrintRules rules,
      PrintTarget target,
      Item item,
      PrintClicks.Inner_CachePool mode,
      Vec3 eye,
      Vec3 feet,
      boolean crouch,
      boolean exact,
      PrintClicks.Inner_DataUtil budget,
      double reach
   ) {
      BlockPos cell = target.pos();
      BlockState state = view.getBlockState(cell);
      if (mode == PrintClicks.Inner_CachePool.WATERLOG && !(state.getBlock() instanceof LiquidBlockContainer)) {
         return null;
      }

      if (mode == PrintClicks.Inner_CachePool.INTERACT && item instanceof MinecartItem) {
         if (!(state.getBlock() instanceof BaseRailBlock rail)) {
            return null;
         }

         EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(BuiltInRegistries.ITEM.getKey(item)).orElse(null);
         if (type == null || type.getDimensions().makeBoundingBox(PrintRailVehicle.init(cell, state, rail)).intersects(PrintGeometry.init(feet.x, feet.y, feet.z, crouch))
            )
          {
            return null;
         }
      }

      List<AABB> outline = view.getOutlineBoxes(state, cell);
      if (outline.isEmpty()) {
         return null;
      }

      Direction merge = mode == PrintClicks.Inner_CachePool.INTERACT && item == state.getBlock().asItem() ? getDoubleSlabMergeFace(state, target.wanted()) : null;
      Set<Direction> pulse = mode == PrintClicks.Inner_CachePool.INTERACT && PrintCore.init_Z_ListPool_Item(target, item)
         ? PrintCore.init_Set_ArrayUtil_BlockPos(view, cell)
         : null;
      if (pulse != null && pulse.isEmpty()) {
         return null;
      }

      Direction only = mode == PrintClicks.Inner_CachePool.INTERACT ? view.getRequiredInteractFace(cell) : null;

      for (AABB box : outline) {
         AABB b = box.move(cell);

         for (Direction face : Direction.values()) {
            if ((merge == null || face == merge) && (pulse == null || pulse.contains(face)) && (only == null || face == only)) {
               double plane = face.getAxisDirection() == AxisDirection.POSITIVE ? b.max(face.getAxis()) : b.min(face.getAxis());
               double eyeSide = getAxisCoordinate(eye, face.getAxis()) - plane;
               if (face.getAxisDirection() == AxisDirection.POSITIVE ? !(eyeSide <= 0.001) : !(eyeSide >= -0.001)) {
                  for (Vec3 point : sampleFacePoints(b, face, plane)) {
                     if (budget.isExhausted()) {
                        return null;
                     }

                     if (!(eye.distanceToSqr(point) > square(reach))) {
                        budget.raycastCount++;
                        Vec3 dir = point.subtract(eye).normalize();
                        BlockHitResult hit = view.raycastBlock(eye, point.add(dir.scale(0.02)));
                        if (hit != null && hit.getBlockPos().equals(cell) && hit.getDirection() == face) {
                           float yaw = PrintGeometry.init(eye, point);
                           float pitch = PrintGeometry.load(eye, point);
                           if (mode != PrintClicks.Inner_CachePool.INTERACT || !(state.getBlock() instanceof MultifaceBlock)) {
                              return new PrintClicks.Inner_StreamUtil(target, mode, item, cell, hit.getDirection(), hit, eye, feet, crouch, yaw, pitch, null);
                           }

                           BlockHitResult wire = view.raycastBlock(eye, eye.add(PrintGeometry.init_Vec3(yaw, pitch).scale(reach)));
                           if (wire != null
                              && wire.getBlockPos().equals(cell)
                              && canExtendMultifaceBlock(view, env, rules, target, item, wire, yaw, pitch, crouch, exact)) {
                              hit = wire;
                              return new PrintClicks.Inner_StreamUtil(target, mode, item, cell, hit.getDirection(), hit, eye, feet, crouch, yaw, pitch, null);
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   static boolean canExtendMultifaceBlock(
      PrintView view, PrintEnv env, PrintRules rules, PrintTarget target, Item item, BlockHitResult hit, float yaw, float pitch, boolean crouch, boolean exact
   ) {
      BlockState live = view.getBlockState(target.pos());
      if (!(live.getBlock() instanceof MultifaceBlock block)) {
         return false;
      } else {
         BlockState var17 = null;
         if (exact && !view.hasHypotheses()) {
            var17 = env.predictPlacedState(item, hit, yaw, pitch, crouch, target.pos());
         } else {
            for (Direction direction : AiPlacementContext.init(yaw, pitch)) {
               var17 = block.getStateForPlacement(live, view, target.pos(), direction);
               if (var17 != null) {
                  break;
               }
            }
         }

         return var17 != null && var17 != live && rules.load_Z_ListPool_BlockState(target, var17);
      }
   }

   static boolean isCellWithinStanceReach(long stanceKey, double feetY, BlockPos cell, double reach) {
      double dx = Math.max(0.0, Math.abs(BlockPos.getX(stanceKey) - cell.getX()) - 0.85);
      double dz = Math.max(0.0, Math.abs(BlockPos.getZ(stanceKey) - cell.getZ()) - 0.85);
      double cy = cell.getY() + 0.5;
      double lo = feetY + 1.27;
      double hi = feetY + 1.62;
      double dy = cy < lo ? lo - cy : (cy > hi ? cy - hi : 0.0);
      return dx * dx + dy * dy + dz * dz <= square(reach + 0.9) + 1.0E-6;
   }

   static List<Vec3> getCandidateFeetPositions(PrintView view, long stanceKey, double feetY) {
      return getCandidateFeetPositions(view, stanceKey, feetY, true);
   }

   static List<Vec3> getCandidateFeetPositions(PrintView view, long stanceKey, double feetY, boolean detailed) {
      return getCandidateFeetPositions(view, stanceKey, feetY, detailed, null);
   }

   static List<Vec3> getCandidateFeetPositions(PrintView view, long stanceKey, double feetY, boolean detailed, Vec3 current) {
      if (current == null && PrintView.FEET_CACHE_ENABLED) {
         List<Vec3> cached = view.getCachedClickPoints(stanceKey, feetY, detailed);
         if (cached != null) {
            return cached;
         }
      }

      return new PrintClicks.Inner_BufferHelper(view, stanceKey, feetY, detailed ? DETAILED_FEET_OFFSETS : COARSE_FEET_OFFSETS, current);
   }

   static List<Vec3> computeCandidateFeetPositions(PrintView view, long stanceKey, double feetY, boolean detailed) {
      return List.copyOf(new PrintClicks.Inner_BufferHelper(view, stanceKey, feetY, detailed ? DETAILED_FEET_OFFSETS : COARSE_FEET_OFFSETS, null));
   }

   private static double[][] buildDetailedFeetOffsets() {
      ArrayList<double[]> offsets = new ArrayList<>();
      offsets.add(new double[]{0.0, 0.0});
      double clear = 0.199;

      for (double dx : new double[]{0.0, -clear, clear, -0.25, 0.25, -0.75, 0.75}) {
         for (double dz : new double[]{0.0, -clear, clear, -0.25, 0.25, -0.75, 0.75}) {
            if (dx != 0.0 || dz != 0.0) {
               offsets.add(new double[]{dx, dz});
            }
         }
      }

      return offsets.toArray(double[][]::new);
   }

   static boolean isInteractableBlock(BlockState state) {
      Block b = state.getBlock();
      return b instanceof BaseEntityBlock
         || b instanceof DoorBlock
         || b instanceof TrapDoorBlock
         || b instanceof FenceGateBlock
         || b instanceof ButtonBlock
         || b instanceof LeverBlock
         || b instanceof DiodeBlock
         || b instanceof NoteBlock
         || b instanceof BedBlock
         || b instanceof CakeBlock
         || b instanceof CraftingTableBlock
         || b instanceof AnvilBlock
         || b instanceof ComposterBlock
         || b instanceof AbstractCauldronBlock
         || b instanceof RedstoneWireBlock
         || b instanceof RespawnAnchorBlock
         || b instanceof GrindstoneBlock
         || b instanceof StonecutterBlock
         || b instanceof LoomBlock
         || b instanceof CartographyTableBlock
         || b instanceof SmithingTableBlock;
   }

   static boolean isSafeToInteract(PrintView view, PrintTarget target, AABB body) {
      BooleanProperty open = BlockStateProperties.OPEN;
      BlockState live = view.getBlockState(target.pos());
      if (!PrintContainerAccess.init(view, target.pos(), live, body)) {
         return false;
      }

      if (getDoubleSlabMergeFace(live, target.wanted()) != null && collidesWithPlacedState(view, live.setValue(SlabBlock.TYPE, SlabType.DOUBLE), target.pos(), body)) {
         return false;
      }

      if (live.hasProperty(open) && target.wanted().hasProperty(open) && !live.getValue(open).equals(target.wanted().getValue(open))) {
         BlockState next = live.cycle(open);
         if (collidesWithPlacedState(view, next, target.pos(), body)) {
            return false;
         }

         if (live.getBlock() instanceof DoorBlock) {
            BlockPos other = live.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER ? target.pos().above() : target.pos().below();
            BlockState half = view.getBlockState(other);
            if (half.is(live.getBlock()) && collidesWithPlacedState(view, half.setValue(open, next.getValue(open)), other, body)) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   static boolean collidesWithPlacedState(PrintView view, BlockState state, BlockPos cell, AABB body) {
      if (intersectsBlockCollision(view, state, cell, body)) {
         return true;
      }

      if (state.getBlock() instanceof PistonBaseBlock) {
         Direction facing = state.getValue(BlockStateProperties.FACING);
         if (new AABB(cell.relative(facing)).intersects(body.deflate(1.0E-4)) && AiPistonPower.init(view, cell, facing)) {
            return true;
         }
      }

      return false;
   }

   static boolean intersectsBlockCollision(PrintView view, BlockState state, BlockPos cell, AABB body) {
      for (AABB box : view.getCollisionBoxesForState(state, cell)) {
         if (box.move(cell).intersects(body.deflate(1.0E-4))) {
            return true;
         }
      }

      return false;
   }

   static List<Vec3> sampleFacePoints(AABB b, Direction face, double plane) {
      return sampleFacePoints(b, face, plane, false);
   }

   private static List<Vec3> sampleFacePoints(AABB b, Direction face, final double plane, final boolean edges) {
      final Axis axis = face.getAxis();
      final Axis u = axis == Axis.X ? Axis.Y : Axis.X;
      final Axis v = axis == Axis.Z ? Axis.Y : Axis.Z;
      final double u0 = b.min(u);
      final double u1 = b.max(u);
      final double v0 = b.min(v);
      final double v1 = b.max(v);
      final double insetU = edges ? Math.min(0.015, (u1 - u0) * 0.15) : Math.min(0.15, (u1 - u0) * 0.25);
      final double insetV = edges ? Math.min(0.015, (v1 - v0) * 0.15) : Math.min(0.15, (v1 - v0) * 0.25);
      final double[] us = edges ? FACE_FRACTIONS_EDGE : (u == Axis.Y ? FACE_FRACTIONS_VERT : FACE_FRACTIONS_HORIZ);
      final double[] vs = edges ? FACE_FRACTIONS_EDGE : (v == Axis.Y ? FACE_FRACTIONS_VERT : FACE_FRACTIONS_HORIZ);
      return new AbstractList<Vec3>() {
         @Override
         public int size() {
            return us.length * vs.length;
         }

         public Vec3 get(int index) {
            return this.init(index);
         }

         public Vec3 init(int index) {
            Objects.checkIndex(index, this.size());
            double fu = us[index / vs.length];
            double fv = vs[index % vs.length];
            double pu = !edges && u == Axis.Y ? u0 + (u1 - u0) * fu : u0 + insetU + (u1 - u0 - 2.0 * insetU) * fu;
            double pv = !edges && v == Axis.Y ? v0 + (v1 - v0) * fv : v0 + insetV + (v1 - v0 - 2.0 * insetV) * fv;
            return PrintClicks.pointOnFacePlane(axis, plane, u, pu, v, pv);
         }
      };
   }

   private static Vec3 pointOnFacePlane(Axis axis, double a, Axis u, double pu, Axis v, double pv) {
      double x = 0.0;
      double y = 0.0;
      double z = 0.0;
      switch (axis) {
         case X:
            x = a;
            break;
         case Y:
            y = a;
            break;
         case Z:
            z = a;
      }

      switch (u) {
         case X:
            x = pu;
            break;
         case Y:
            y = pu;
            break;
         case Z:
            z = pu;
      }

      switch (v) {
         case X:
            x = pv;
            break;
         case Y:
            y = pv;
            break;
         case Z:
            z = pv;
      }

      return new Vec3(x, y, z);
   }

   static double getAxisCoordinate(Vec3 v, Axis axis) {
      double var10000;
      switch (axis) {
         case X:
            var10000 = v.x;
            break;
         case Y:
            var10000 = v.y;
            break;
         case Z:
            var10000 = v.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static double square(double v) {
      return v * v;
   }

   static Item getFluidBucketItem(PrintTarget target) {
      return target.wanted().getFluidState().getType().isSame(Fluids.LAVA) ? Items.LAVA_BUCKET : Items.WATER_BUCKET;
   }

   private static final class Inner_BufferHelper extends AbstractList<Vec3> {
      private final PrintView view;
      private final int cellX;
      private final int cellZ;
      private final double feetY;
      private final double[][] offsets;
      private final ArrayList<Vec3> resolved = new ArrayList<>();
      private int nextOffsetIndex;
      private List<Vec3> edgeCandidates;
      private int nextEdgeIndex;
      private List<AABB> nearbyCollisions;

      Inner_BufferHelper(PrintView view, long key, double y, double[][] offsets, Vec3 current) {
         this.view = view;
         this.cellX = BlockPos.getX(key);
         this.cellZ = BlockPos.getZ(key);
         this.feetY = y;
         this.offsets = offsets;
         if (current != null) {
            this.resolved.add(current);
         }
      }

      private boolean ensureResolved(int index) {
         while (true) {
            Vec3 feet;
            while (true) {
               if (this.resolved.size() <= index) {
                  if (this.nextOffsetIndex < this.offsets.length) {
                     double[] offset = this.offsets[this.nextOffsetIndex++];
                     feet = new Vec3(this.cellX + 0.5 + offset[0], this.feetY, this.cellZ + 0.5 + offset[1]);
                     if (offset[0] == 0.0 && offset[1] == 0.0) {
                        this.resolved.add(feet);
                        continue;
                     }
                     break;
                  }

                  if (this.edgeCandidates == null) {
                     this.edgeCandidates = this.buildFloorEdgeCandidates();
                  }

                  if (this.nextEdgeIndex < this.edgeCandidates.size()) {
                     feet = this.edgeCandidates.get(this.nextEdgeIndex++);
                     if (this.resolved.contains(feet)) {
                        continue;
                     }
                     break;
                  }
               }

               return index < this.resolved.size();
            }

            AABB body = PrintGeometry.init(feet.x, this.feetY, feet.z, true);
            if (this.view.isBoxClear(body)
               && !this.view.intersectsHazard(body)
               && !PrintView.isHazardBlock(this.view.getBlockState(BlockPos.containing(feet.x, this.feetY - 0.2, feet.z)))) {
               AABB under = new AABB(body.minX, this.feetY - 0.01, body.minZ, body.maxX, this.feetY - 0.001, body.maxZ);
               if (!this.view.isBoxClear(under) && this.hasContinuousFooting(feet)) {
                  this.resolved.add(feet);
               }
            }
         }
      }

      private boolean hasContinuousFooting(Vec3 feet) {
         double cx = this.cellX + 0.5;
         double cz = this.cellZ + 0.5;
         if (this.nearbyCollisions == null) {
            this.nearbyCollisions = this.view.collectCollisions(new AABB(cx - 1.55, this.feetY - 0.01, cz - 1.55, cx + 1.55, this.feetY + 1.5, cz + 1.55));
         }

         ArrayList<PrintClicks.Inner_BufferHelper.Inner_DataUtil> footing = new ArrayList<>();

         for (AABB box : this.nearbyCollisions) {
            PrintClicks.Inner_BufferHelper.Inner_DataUtil interval = intersectFootprintSegment(cx, cz, feet.x - cx, feet.z - cz, box);
            if (interval != null) {
               if (box.maxY > this.feetY + 1.0E-4 && box.minY < this.feetY + 1.5 - 1.0E-4) {
                  return false;
               }

               if (box.maxY > this.feetY - 0.01 && box.maxY <= this.feetY + 1.0E-4) {
                  footing.add(interval);
               }
            }
         }

         footing.sort(Comparator.comparingDouble(PrintClicks.Inner_BufferHelper.Inner_DataUtil::start));
         double covered = 0.0;

         for (PrintClicks.Inner_BufferHelper.Inner_DataUtil interval : footing) {
            if (interval.start > covered + 1.0E-9) {
               return false;
            }

            covered = Math.max(covered, interval.end);
            if (covered >= 1.0) {
               return true;
            }
         }

         return false;
      }

      private static PrintClicks.Inner_BufferHelper.Inner_DataUtil intersectFootprintSegment(double x, double z, double dx, double dz, AABB box) {
         double radius = 0.2999;
         double start = 0.0;
         double end = 1.0;
         if (Math.abs(dx) < 1.0E-12) {
            if (x <= box.minX - radius || x >= box.maxX + radius) {
               return null;
            }
         } else {
            double a = (box.minX - radius - x) / dx;
            double b = (box.maxX + radius - x) / dx;
            start = Math.max(start, Math.min(a, b));
            end = Math.min(end, Math.max(a, b));
         }

         if (Math.abs(dz) < 1.0E-12) {
            if (z <= box.minZ - radius || z >= box.maxZ + radius) {
               return null;
            }
         } else {
            double a = (box.minZ - radius - z) / dz;
            double b = (box.maxZ + radius - z) / dz;
            start = Math.max(start, Math.min(a, b));
            end = Math.min(end, Math.max(a, b));
         }

         return start < end ? new PrintClicks.Inner_BufferHelper.Inner_DataUtil(start, end) : null;
      }

      private List<Vec3> buildFloorEdgeCandidates() {
         LinkedHashSet<Double> xs = new LinkedHashSet<>();
         LinkedHashSet<Double> zs = new LinkedHashSet<>();
         xs.add(this.cellX + 0.5);
         zs.add(this.cellZ + 0.5);
         AABB under = new AABB(
            this.cellX + 0.5 - 0.3, this.feetY - 0.01, this.cellZ + 0.5 - 0.3, this.cellX + 0.5 + 0.3, this.feetY - 0.001, this.cellZ + 0.5 + 0.3
         );

         for (AABB floor : this.view.collectCollisions(under)) {
            if (!(Math.abs(floor.maxY - this.feetY) > 1.0E-6)) {
               xs.add(floor.minX - 0.015);
               xs.add(floor.maxX + 0.015);
               zs.add(floor.minZ - 0.015);
               zs.add(floor.maxZ + 0.015);
            }
         }

         ArrayList<Vec3> out = new ArrayList<>();

         for (double fx : xs) {
            for (double fz : zs) {
               out.add(new Vec3(fx, this.feetY, fz));
            }
         }

         return out;
      }

      public Vec3 get(int index) {
         return this.getAt(index);
      }

      public Vec3 getAt(int index) {
         if (index >= 0 && this.ensureResolved(index)) {
            return this.resolved.get(index);
         } else {
            throw new IndexOutOfBoundsException(index);
         }
      }

      @Override
      public int size() {
         this.ensureResolved(2147483647);
         return this.resolved.size();
      }

      @Override
      public Iterator<Vec3> iterator() {
         return new Iterator<Vec3>() {
            private int init;

            @Override
            public boolean hasNext() {
               return Inner_BufferHelper.this.ensureResolved(this.init);
            }

            public Vec3 next() {
               return this.init();
            }

            public Vec3 init() {
               if (!Inner_BufferHelper.this.ensureResolved(this.init)) {
                  throw new NoSuchElementException();
               } else {
                  return Inner_BufferHelper.this.resolved.get(this.init++);
               }
            }
         };
      }

      private record Inner_DataUtil(double start, double end) {
      }
   }

   enum Inner_CachePool {
      PLACE,
      INTERACT,
      POUR,
      WATERLOG,
      DIG;
   }

   static final class Inner_DataUtil {
      int raycastCount;
      int predictCount;
      final int maxRaycasts;
      private PrintView cachedView;
      private long cachedViewRevision;
      private final Map<AABB, Boolean> bodyClearCache = new HashMap<>();

      Inner_DataUtil() {
         this.maxRaycasts = -1;
      }

      Inner_DataUtil(int maxRays) {
         this.maxRaycasts = maxRays;
      }

      boolean isExhausted() {
         return this.maxRaycasts >= 0 && this.raycastCount >= this.maxRaycasts;
      }

      boolean isBodyClear(PrintView view, AABB body) {
         if (this.cachedView != view || this.cachedViewRevision != view.viewRevision()) {
            this.bodyClearCache.clear();
            this.cachedView = view;
            this.cachedViewRevision = view.viewRevision();
         }

         return this.bodyClearCache.computeIfAbsent(body, box -> view.isBoxClear(box.deflate(0.001)));
      }
   }

   record Inner_StreamUtil(
      PrintTarget target,
      PrintClicks.Inner_CachePool mode,
      Item item,
      BlockPos support,
      Direction face,
      BlockHitResult result,
      Vec3 eye,
      Vec3 feet,
      boolean crouch,
      float yaw,
      float pitch,
      @Nullable BlockState predicted
   ) {
   }
}
