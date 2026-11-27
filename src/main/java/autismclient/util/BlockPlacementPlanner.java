package autismclient.util;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CakeBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.ConduitBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockPlacementPlanner {
   public static final Direction[] DEFAULT_FACE_ORDER = new Direction[]{
      Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
   };
   public static final double PLANE_EPSILON = 1.0E-4;
   public static final double CENTRE_WINDOW_HALF_SPAN = 0.2;
   public static final int MAX_VERIFY_RUNGS = 4;
   public static final int DEFAULT_MAX_FACE_RECTS = 3;
   public static final int CANDIDATE_CACHE_SIZE = 48;
   public static final float MAX_SAFE_PITCH = 89.5F;
   private static final double MIN_EDGE_MARGIN = 0.12;
   private static final double PITCH_CAP_SAFETY_BUFFER = 0.01;
   private static final int MAX_SHAPE_BOXES = 16;
   private static final double PITCH_WINDOW_MARGIN_RATIO = 0.06;
   private static final AABB[] EMPTY_BOXES = new AABB[0];
   private static final int CANDIDATE_CACHE_SLOTS = 48;
   private static final BlockPlacementPlanner.CandidateCache[] CANDIDATE_CACHE = new BlockPlacementPlanner.CandidateCache[48];
   private static int nextCandidateCacheSlot;
   private static final int SHAPE_CACHE_SIZE = 8;
   private static final BlockPos[] SHAPE_CACHE_POSITIONS = new BlockPos[8];
   private static final BlockState[] SHAPE_CACHE_STATES = new BlockState[8];
   private static final AABB[][] SHAPE_CACHE_BOXES = new AABB[8][];
   private static int nextShapeCacheSlot;
   private static int lastLeashCheckTick = -2147483648;
   private static boolean cachedHoldingLeashedEntity;

   private BlockPlacementPlanner() {
   }

   public static BlockPlacementPlanner.HitValidator blockItemValidator(ItemStack stack, Player player, InteractionHand hand) {
      return (hit, cell) -> {
         if (hit == null || cell == null || player == null) {
            return false;
         } else if (stack != null && stack.getItem() instanceof BlockItem) {
            BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hit);
            return context.canPlace() && context.getClickedPos().equals(cell);
         } else {
            return false;
         }
      };
   }

   public static BlockPlacementPlanner.HitValidator anchorFaceValidator(BlockPos support, EnumSet<Direction> allowedFaces) {
      BlockPos anchor = support == null ? null : support.immutable();
      return (hit, cell) -> {
         if (hit != null && anchor != null && cell != null) {
            if (!hit.getBlockPos().equals(anchor)) {
               return false;
            } else {
               return allowedFaces != null && !allowedFaces.contains(hit.getDirection()) ? false : cell.equals(anchor) || cell.equals(anchor.above());
            }
         } else {
            return false;
         }
      };
   }

   public static double defaultMaxPitchDegrees() {
      return 89.5 - RotationStreamPlanner.pitchMargin(RotationMath.gcdStep()) - 0.01;
   }

   public static void collectCandidates(BlockPlacementPlanner.Query request, List<BlockPlacementPlanner.SupportCandidate> out) {
      if (request != null && out != null && request.targetCell() != null) {
         Level level = currentLevel();
         if (level != null) {
            BlockPos cell = request.targetCell();
            int start = out.size();
            boolean inPlace = request.allowInPlace() && canPlaceInReplaceableCell(cell, level);
            MutableBlockPos scan = new MutableBlockPos();

            for (Direction face : request.faceOrder()) {
               if (request.allowedFaces() == null || request.allowedFaces().contains(face)) {
                  for (int form = 0; form < 2; form++) {
                     boolean own = form == 0;
                     if (!own || inPlace) {
                        BlockPos support = own ? cell : scan.setWithOffset(cell, face.getOpposite()).immutable();
                        BlockState state = level.getBlockState(support);
                        if (!state.isAir()) {
                           boolean requiresSneak = requiresSneakToPlace(state, support);
                           boolean sneaking = request.isSneaking() || requiresSneak && request.allowAutoSneak();
                           if ((own || isValidSupportBlock(state, support, sneaking)) && (!own || !wouldInteractWithoutSneak(state, support, sneaking))) {
                              BlockPlacementPlanner.SupportFace intent = new BlockPlacementPlanner.SupportFace(support, face);
                              if (request.bannedSupports() == null || !request.bannedSupports().test(intent)) {
                                 for (AABB rect : extractFaceRects(state, support, face, request.maxFaceRects())) {
                                    out.add(
                                       new BlockPlacementPlanner.SupportCandidate(
                                          cell,
                                          support,
                                          face,
                                          form,
                                          state,
                                          rect,
                                          faceArea(rect, face),
                                          edgeMarginRatio(rect, face, request.eyePos()),
                                          requiresSneak,
                                          intent
                                       )
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            sortCandidates(out, start, request);
         }
      }
   }

   public static void collectCandidatesForCells(List<BlockPos> cells, BlockPlacementPlanner.Query template, List<BlockPlacementPlanner.SupportCandidate> out) {
      if (cells != null && template != null && out != null) {
         BlockPos held = template.targetCell();

         for (BlockPos cell : cells) {
            collectCandidates(template.withTargetCell(cell), out);
         }

         template.withTargetCell(held);
      }
   }

   public static long hashCellsNeighborhood(List<BlockPos> cells, Level level) {
      if (cells != null && level != null) {
         long key = 0L;
         MutableBlockPos scan = new MutableBlockPos();

         for (BlockPos cell : cells) {
            key += hashCellNeighborhood(cell, level, scan);
         }

         return key;
      } else {
         return 0L;
      }
   }

   private static long hashCellNeighborhood(BlockPos cell, Level level, MutableBlockPos scan) {
      long key = cell.asLong() * 31L + System.identityHashCode(level.getBlockState(cell));

      for (Direction direction : Direction.values()) {
         key = key * 31L + System.identityHashCode(level.getBlockState(scan.setWithOffset(cell, direction)));
      }

      key ^= key >>> 33;
      key *= -49064778989728563L;
      return key ^ key >>> 33;
   }

   private static void sortCandidates(List<BlockPlacementPlanner.SupportCandidate> out, int start, BlockPlacementPlanner.Query request) {
      int size = out.size();
      if (size - start >= 2) {
         List<BlockPlacementPlanner.SupportCandidate> segment = new ArrayList<>(out.subList(start, size));
         Vec3 eye = request.eyePos();
         RotationMath.Rotation from = request.fromRotation();
         Direction[] order = request.faceOrder();
         segment.sort((left, right) -> {
            boolean leftUp = left.face() == Direction.UP;
            boolean rightUp = right.face() == Direction.UP;
            if (leftUp != rightUp) {
               return leftUp ? -1 : 1;
            }

            int area = Double.compare(right.faceArea(), left.faceArea());
            if (area != 0) {
               return area;
            }

            int edge = Double.compare(right.edgeMarginRatio(), left.edgeMarginRatio());
            if (edge != 0) {
               return edge;
            }

            if (from != null) {
               int turn = Float.compare(estimateTurnAngle(left, eye, from), estimateTurnAngle(right, eye, from));
               if (turn != 0) {
                  return turn;
               }
            }

            return Integer.compare(faceOrderIndex(order, left.face()), faceOrderIndex(order, right.face()));
         });

         for (int i = 0; i < segment.size(); i++) {
            out.set(start + i, segment.get(i));
         }
      }
   }

   private static float estimateTurnAngle(BlockPlacementPlanner.SupportCandidate option, Vec3 eye, RotationMath.Rotation from) {
      Vec3 centre = faceCenter(option.faceRect(), option.face());
      return RotationMath.angularDistance(from, RotationMath.lookAt(centre, eye));
   }

   private static int faceOrderIndex(Direction[] order, Direction face) {
      for (int i = 0; i < order.length; i++) {
         if (order[i] == face) {
            return i;
         }
      }

      return order.length;
   }

   public static BlockPlacementPlanner.AimSample computeAimSample(BlockPlacementPlanner.SupportCandidate option, BlockPlacementPlanner.Query request, BlockPlacementPlanner.RejectReason[] refusal) {
      setRefusal(refusal, BlockPlacementPlanner.RejectReason.NONE);
      if (option != null && request != null) {
         Level level = currentLevel();
         Direction face = option.face();
         if (level == null) {
            return rejectAim(refusal, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT, request, face);
         }

         BlockPos support = option.supportPos();
         BlockState state = level.getBlockState(support);
         if (state != option.supportState()) {
            return rejectAim(refusal, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT, request, face);
         }

         if (request.bannedSupports() != null && request.bannedSupports().test(option.supportFace())) {
            return rejectAim(refusal, BlockPlacementPlanner.RejectReason.BANNED, request, face);
         }

         boolean sneaking = request.isSneaking() || option.requiresSneak() && request.allowAutoSneak();
         if (request.hitValidator() != null && request.hitValidator().init(state, support, sneaking)) {
            Vec3 eye = request.eyePos();
            if (isSelfOccludedByLid(eye, support, face, level)) {
               return rejectAim(refusal, BlockPlacementPlanner.RejectReason.SELF_OCCLUDED, request, face);
            }

            AABB rect = option.faceRect();
            Vec3 point = projectPreferredPoint(rect, face, eye, resolveFromRotation(request), request.marginCap);
            if (point == null) {
               return rejectAim(refusal, BlockPlacementPlanner.RejectReason.BEHIND_PLANE, request, face);
            }

            BlockPlacementPlanner.SamplePoint tier = BlockPlacementPlanner.SamplePoint.CENTRE;
            double reachSq = request.reach() * request.reach();
            if (eye.distanceToSqr(point) > reachSq) {
               point = sampleFacePoint(rect, face, eye, BlockPlacementPlanner.SamplePoint.NEAREST, request.marginCap);
               if (point == null || eye.distanceToSqr(point) > reachSq) {
                  return rejectAim(refusal, BlockPlacementPlanner.RejectReason.OUT_OF_REACH, request, face);
               }

               tier = BlockPlacementPlanner.SamplePoint.NEAREST;
            }

            RotationMath.Rotation goal = RotationMath.lookAt(point, eye);
            if (Math.abs(goal.pitch()) > request.maxPitchDegrees()) {
               Vec3 flat = sampleFacePoint(rect, face, eye, BlockPlacementPlanner.SamplePoint.FLATTEST, request.marginCap);
               if (flat == null || eye.distanceToSqr(flat) > reachSq) {
                  return rejectAim(refusal, BlockPlacementPlanner.RejectReason.PAST_PITCH_CAP, request, face);
               }

               goal = RotationMath.lookAt(flat, eye);
               if (Math.abs(goal.pitch()) > request.maxPitchDegrees()) {
                  return rejectAim(refusal, BlockPlacementPlanner.RejectReason.PAST_PITCH_CAP, request, face);
               }

               point = flat;
               tier = BlockPlacementPlanner.SamplePoint.FLATTEST;
            }

            if (face.getAxis().isHorizontal() && request.leadEyePos() != null) {
               double[] window = computeLeadPitchWindow(eye, request.leadEyePos(), rect, face, goal.yaw(), request.marginCap);
               if (window != null) {
                  float pitch = (float)Mth.clamp(goal.pitch(), window[0], window[1]);
                  if (Math.abs(pitch) > request.maxPitchDegrees()) {
                     return rejectAim(refusal, BlockPlacementPlanner.RejectReason.PAST_PITCH_CAP, request, face);
                  }

                  goal = new RotationMath.Rotation(goal.yaw(), pitch);
               }
            }

            if (!request.hitValidator().lands(new BlockHitResult(point, face, support, false), request.targetCell())) {
               return rejectAim(refusal, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT, request, face);
            }

            RotationMath.Rotation from = resolveFromRotation(request);
            RotationMath.Rotation emitted = request.quantizeGcd() ? RotationMath.applyGcd(goal, from == null ? goal : from) : goal;
            float turn = from == null ? 0.0F : RotationMath.angularDistance(from, goal);
            return new BlockPlacementPlanner.AimSample(point, goal, emitted, eye.distanceTo(point), turn, tier);
         } else {
            return rejectAim(refusal, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT, request, face);
         }
      } else {
         return rejectAim(refusal, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT, request, null);
      }
   }

   public static Vec3 projectPreferredPoint(AABB rect, Direction face, Vec3 eye, RotationMath.Rotation from) {
      return projectPreferredPoint(rect, face, eye, from, 1.0 / 0.0);
   }

   private static Vec3 projectPreferredPoint(AABB rect, Direction face, Vec3 eye, RotationMath.Rotation from, double marginCap) {
      if (rect != null && face != null && eye != null) {
         Axis normal = face.getAxis();
         double plane = facePlaneCoord(rect, face);
         if (signedDistancePastFace(eye, rect, face) <= 1.0E-4) {
            return null;
         }

         Axis first = tangentAxis(normal, true);
         Axis second = tangentAxis(normal, false);
         double margin = Math.min(marginForDistance(eye.distanceTo(faceCenter(rect, face))), Math.max(0.03, marginCap));
         double[] windowFirst = axisSampleRange(rect, first, margin, true);
         double[] windowSecond = axisSampleRange(rect, second, margin, true);
         double travel = -1.0;
         Vec3 look = from == null ? null : directionVector(from);
         if (look != null) {
            double along = axisComponent(look, normal);
            travel = Math.abs(along) > 1.0E-4 ? (plane - axisComponent(eye, normal)) / along : -1.0;
         }

         double b;
         double a;
         if (travel > 0.0) {
            Vec3 crossing = eye.add(look.scale(travel));
            a = axisComponent(crossing, first);
            b = axisComponent(crossing, second);
         } else {
            a = axisComponent(eye, first);
            b = axisComponent(eye, second);
         }

         return composeFacePoint(normal, plane, Mth.clamp(a, windowFirst[0], windowFirst[1]), Mth.clamp(b, windowSecond[0], windowSecond[1]));
      } else {
         return null;
      }
   }

   public static Vec3 sampleFacePoint(AABB rect, Direction face, Vec3 eye, BlockPlacementPlanner.SamplePoint tier) {
      return sampleFacePoint(rect, face, eye, tier, 1.0 / 0.0);
   }

   public static Vec3 sampleFacePoint(AABB rect, Direction face, Vec3 eye, BlockPlacementPlanner.SamplePoint tier, double marginCap) {
      if (rect != null && face != null && eye != null) {
         Axis normal = face.getAxis();
         double plane = facePlaneCoord(rect, face);
         if (signedDistancePastFace(eye, rect, face) <= 1.0E-4) {
            return null;
         }

         Axis first = tangentAxis(normal, true);
         Axis second = tangentAxis(normal, false);
         double margin = Math.min(marginForDistance(eye.distanceTo(faceCenter(rect, face))), Math.max(0.03, marginCap));
         boolean centred = tier == BlockPlacementPlanner.SamplePoint.CENTRE;
         boolean away = tier == BlockPlacementPlanner.SamplePoint.FLATTEST || tier == BlockPlacementPlanner.SamplePoint.ESCAPE;
         return composeFacePoint(
            normal,
            plane,
            pickAxisCoordinate(axisSampleRange(rect, first, margin, centred), axisComponent(eye, first), first, away),
            pickAxisCoordinate(axisSampleRange(rect, second, margin, centred), axisComponent(eye, second), second, away)
         );
      } else {
         return null;
      }
   }

   private static double pickAxisCoordinate(double[] window, double at, Axis axis, boolean away) {
      if (away && axis != Axis.Y) {
         return Math.abs(window[0] - at) >= Math.abs(window[1] - at) ? window[0] : window[1];
      } else {
         return Mth.clamp(at, window[0], window[1]);
      }
   }

   public static double clampMarginForAxis(AABB rect, Axis axis, double margin) {
      double extent = rect.max(axis) - rect.min(axis);
      return Math.min(Math.max(0.0, margin), Math.max(0.0, extent * 0.5 - 1.0E-4));
   }

   public static double[] axisSampleRange(AABB rect, Axis axis, double margin, boolean centred) {
      double inset = clampMarginForAxis(rect, axis, margin);
      double low = rect.min(axis) + inset;
      double high = rect.max(axis) - inset;
      if (high < low) {
         double mid = (low + high) * 0.5;
         low = mid;
         high = mid;
      }

      if (!centred) {
         return new double[]{low, high};
      }

      double centre = (rect.min(axis) + rect.max(axis)) * 0.5;
      double half = Math.min(0.2, (high - low) * 0.5);
      return new double[]{centre - half, centre + half};
   }

   public static double marginForDistance(double distance) {
      double band = RotationStreamPlanner.pitchMargin(RotationMath.gcdStep());
      return Math.max(0.12, Math.abs(distance) * Math.tan(Math.toRadians(band)));
   }

   public static double edgeMarginRatio(AABB rect, Direction face, Vec3 eye) {
      if (rect != null && face != null && eye != null) {
         double margin = marginForDistance(eye.distanceTo(faceCenter(rect, face))) * 2.0;
         if (margin <= 0.0) {
            return 1.0;
         }

         Axis normal = face.getAxis();
         double first = rect.max(tangentAxis(normal, true)) - rect.min(tangentAxis(normal, true));
         double second = rect.max(tangentAxis(normal, false)) - rect.min(tangentAxis(normal, false));
         return Mth.clamp(Math.min(first, second) / margin, 0.0, 1.0);
      } else {
         return 0.0;
      }
   }

   public static double[] computePitchWindow(Vec3 eye, AABB rect, Direction face, float yaw) {
      return computePitchWindow(eye, rect, face, yaw, 1.0 / 0.0);
   }

   public static double[] computePitchWindow(Vec3 eye, AABB rect, Direction face, float yaw, double marginCap) {
      if (eye != null && rect != null && face != null) {
         double past = signedDistancePastFace(eye, rect, face);
         if (past <= 0.0) {
            return null;
         }

         double yawRad = Math.toRadians(yaw);
         double lookX = -Math.sin(yawRad);
         double lookZ = Math.cos(yawRad);
         double toward = -(lookX * face.getStepX() + lookZ * face.getStepZ());
         if (toward <= 0.1) {
            return null;
         }

         double run = past / toward;
         double margin = Math.min(0.06 * rect.getYsize(), Math.max(0.03, marginCap));
         double shallowest = Math.toDegrees(Math.atan2(eye.y - (rect.maxY - margin), run));
         double steepest = Math.toDegrees(Math.atan2(eye.y - (rect.minY + margin), run));
         return new double[]{shallowest, steepest, run};
      } else {
         return null;
      }
   }

   public static double[] computeLeadPitchWindow(Vec3 eye, Vec3 leadEye, AABB rect, Direction face, float yaw) {
      return computeLeadPitchWindow(eye, leadEye, rect, face, yaw, 1.0 / 0.0);
   }

   private static double[] computeLeadPitchWindow(Vec3 eye, Vec3 leadEye, AABB rect, Direction face, float yaw, double marginCap) {
      double[] primary = computePitchWindow(eye, rect, face, yaw, marginCap);
      if (primary == null) {
         return null;
      }

      double low = primary[0];
      double high = primary[1];
      double[] secondary = computePitchWindow(leadEye, rect, face, yaw, marginCap);
      if (secondary != null) {
         double bothLow = Math.max(low, secondary[0]);
         double bothHigh = Math.min(high, secondary[1]);
         if (bothLow <= bothHigh) {
            low = bothLow;
            high = bothHigh;
         }
      }

      return new double[]{low, high, primary[2]};
   }

   public static BlockPlacementPlanner.PlacementPlan verifyPlacement(
      BlockPlacementPlanner.SupportCandidate option, BlockPlacementPlanner.AimSample aim, BlockPlacementPlanner.Query request, BlockPlacementPlanner.RejectReason[] refusal
   ) {
      setRefusal(refusal, BlockPlacementPlanner.RejectReason.NONE);
      if (option != null && aim != null && request != null) {
         Level level = currentLevel();
         Player player = currentPlayer();
         Direction face = option.face();
         if (level != null && player != null) {
            Vec3 eye = request.eyePos();
            double reach = request.reach();
            double reachSq = reach * reach;
            boolean sneaking = request.isSneaking() || option.requiresSneak() && request.allowAutoSneak();
            RotationMath.Rotation cast = request.quantizeGcd() ? aim.emittedRotation() : aim.goalRotation();
            boolean escaped = false;

            for (int rung = 0; rung < 4; rung++) {
               if (request.rayBudget() != null && !request.rayBudget().consume()) {
                  return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.NO_BUDGET, request, face);
               }

               BlockHitResult hit = raycastBlock(eye, cast, reach, level, player);
               BlockPos hitPos = hit == null ? null : hit.getBlockPos();
               BlockState hitState = hit == null ? null : level.getBlockState(hitPos);
               boolean builds = hit != null
                  && request.hitValidator().init(hitState, hitPos, sneaking)
                  && request.hitValidator().lands(hit, request.targetCell());
               if (!builds) {
                  StringBuilder trace = request.trace();
                  if (trace != null && trace.length() < 160) {
                     trace.append(" [hit=")
                        .append(hitPos)
                        .append(" type=")
                        .append(hit == null ? "MISS" : hit.getType())
                        .append(" face=")
                        .append(hit == null ? null : hit.getDirection())
                        .append(" cell=")
                        .append(request.targetCell())
                        .append(']');
                  }

                  if (escaped) {
                     return rejectPlan(refusal, hit == null ? BlockPlacementPlanner.RejectReason.RAY_MISSED : BlockPlacementPlanner.RejectReason.RAY_WRONG_CELL, request, face);
                  }

                  escaped = true;
                  cast = computeRetryRotation(
                     sampleFacePoint(option.faceRect(), face, eye, BlockPlacementPlanner.SamplePoint.ESCAPE, request.marginCap), cast, eye, reachSq, request
                  );
                  if (cast == null) {
                     return rejectPlan(refusal, hit == null ? BlockPlacementPlanner.RejectReason.RAY_MISSED : BlockPlacementPlanner.RejectReason.RAY_WRONG_CELL, request, face);
                  }
               } else {
                  BlockPlacementPlanner.FaceBounds landing = findHitFaceBounds(hitState, hitPos, hit);
                  if (landing == null) {
                     return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.RAY_MISSED, request, face);
                  }

                  AABB hitRect = landing.box();
                  Direction hitFace = landing.face();
                  if (isPointInsideFace(hitRect, hitFace, hit.getLocation(), request.effectiveMargin(eye.distanceTo(hit.getLocation())))) {
                     boolean substituted = !hitPos.equals(option.supportPos()) || hitFace != face;
                     BlockPlacementPlanner.SupportCandidate found = substituted ? buildSubstitutedCandidate(option, hitPos, hitFace, hitState, hitRect, eye, request) : option;
                     BlockPlacementPlanner.AimSample landed = new BlockPlacementPlanner.AimSample(
                        hit.getLocation(),
                        cast,
                        cast,
                        eye.distanceTo(hit.getLocation()),
                        resolveFromRotation(request) == null ? 0.0F : RotationMath.angularDistance(resolveFromRotation(request), cast),
                        aim.samplePoint()
                     );
                     return new BlockPlacementPlanner.PlacementPlan(found, landed, hit, substituted, scorePlacement(found, landed));
                  }

                  cast = computeRetryRotation(projectPreferredPoint(hitRect, hitFace, eye, cast, request.marginCap), cast, eye, reachSq, request);
                  if (cast == null) {
                     return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.RAY_MISSED, request, face);
                  }
               }
            }

            return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.RAY_MISSED, request, face);
         } else {
            return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.RAY_MISSED, request, face);
         }
      } else {
         return rejectPlan(refusal, BlockPlacementPlanner.RejectReason.RAY_MISSED, request, null);
      }
   }

   private static RotationMath.Rotation computeRetryRotation(Vec3 point, RotationMath.Rotation cast, Vec3 eye, double reachSq, BlockPlacementPlanner.Query request) {
      if (point != null && !(eye.distanceToSqr(point) > reachSq)) {
         RotationMath.Rotation next = RotationMath.lookAt(point, eye);
         if (Math.abs(next.pitch()) > request.maxPitchDegrees()) {
            return null;
         }

         if (request.quantizeGcd()) {
            RotationMath.Rotation from = resolveFromRotation(request);
            next = RotationMath.applyGcd(next, from == null ? next : from);
            if (Math.abs(next.pitch()) > request.maxPitchDegrees()) {
               return null;
            }
         }

         return Float.compare(next.yaw(), cast.yaw()) == 0 && Float.compare(next.pitch(), cast.pitch()) == 0 ? null : next;
      } else {
         return null;
      }
   }

   private static BlockPlacementPlanner.SupportCandidate buildSubstitutedCandidate(
      BlockPlacementPlanner.SupportCandidate option, BlockPos support, Direction face, BlockState state, AABB rect, Vec3 eye, BlockPlacementPlanner.Query request
   ) {
      AABB flat = projectBoxToFace(rect, face);
      return new BlockPlacementPlanner.SupportCandidate(
         option.targetCell(),
         support.immutable(),
         face,
         support.equals(option.targetCell()) ? 0 : 1,
         state,
         flat,
         faceArea(flat, face),
         edgeMarginRatio(flat, face, eye),
         requiresSneakToPlace(state, support),
         new BlockPlacementPlanner.SupportFace(support, face)
      );
   }

   private static double scorePlacement(BlockPlacementPlanner.SupportCandidate option, BlockPlacementPlanner.AimSample aim) {
      return (option.face() == Direction.UP ? 4.0 : 0.0) + option.faceArea() + option.edgeMarginRatio() - aim.turnDegrees() / 180.0;
   }

   public static BlockPlacementPlanner.PlacementPlan plan(BlockPlacementPlanner.Query request) {
      return plan(request, (BlockPlacementPlanner.RejectReason[])null);
   }

   public static BlockPlacementPlanner.PlacementPlan plan(BlockPlacementPlanner.Query request, BlockPlacementPlanner.RejectReason[] outcome) {
      setRefusal(outcome, BlockPlacementPlanner.RejectReason.NONE);
      if (request != null && request.targetCell() != null) {
         Level level = currentLevel();
         if (level == null) {
            return null;
         }

         BlockPlacementPlanner.CandidateCache slot = getOrBuildCandidateCache(request, level);
         List<BlockPlacementPlanner.SupportCandidate> found = slot.candidates;
         int size = found.size();
         if (size == 0) {
            setRefusal(outcome, BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT);
            return null;
         }

         if (slot.nextStartIndex >= size) {
            slot.nextStartIndex = 0;
         }

         BlockPlacementPlanner.RejectReason[] refusal = new BlockPlacementPlanner.RejectReason[1];
         BlockPlacementPlanner.RejectReason last = BlockPlacementPlanner.RejectReason.NOT_A_SUPPORT;
         int start = slot.nextStartIndex;

         for (int step = 0; step < size; step++) {
            int index = start + step;
            if (index >= size) {
               index -= size;
            }

            BlockPlacementPlanner.SupportCandidate option = found.get(index);
            BlockPlacementPlanner.AimSample aim = computeAimSample(option, request, refusal);
            if (aim == null) {
               last = refusal[0];
               slot.nextStartIndex = index + 1 == size ? 0 : index + 1;
            } else {
               BlockPlacementPlanner.PlacementPlan candidate = verifyPlacement(option, aim, request, refusal);
               if (refusal[0] == BlockPlacementPlanner.RejectReason.NO_BUDGET) {
                  slot.nextStartIndex = index;
                  setRefusal(outcome, BlockPlacementPlanner.RejectReason.NO_BUDGET);
                  return null;
               }

               if (candidate != null) {
                  slot.nextStartIndex = index;
                  return candidate;
               }

               slot.nextStartIndex = index + 1 == size ? 0 : index + 1;
               last = refusal[0];
            }
         }

         setRefusal(outcome, last);
         return null;
      } else {
         return null;
      }
   }

   public static BlockHitResult verifyWithRotation(
      BlockPlacementPlanner.PlacementPlan candidate, RotationMath.Rotation wire, Vec3 eye, double reach, BlockPlacementPlanner.Query request
   ) {
      if (candidate != null && wire != null && eye != null && request != null) {
         if (Math.abs(wire.pitch()) > 89.5F) {
            return null;
         }

         Level level = currentLevel();
         Player player = currentPlayer();
         if (level != null && player != null) {
            BlockHitResult hit = raycastBlock(eye, wire, reach, level, player);
            if (hit == null) {
               return null;
            } else {
               BlockPos hitPos = hit.getBlockPos();
               BlockState hitState = level.getBlockState(hitPos);
               if (!request.hitValidator().init(hitState, hitPos, request.isSneaking())) {
                  return null;
               } else if (!request.hitValidator().lands(hit, candidate.support().targetCell())) {
                  return null;
               } else {
                  BlockPlacementPlanner.SupportCandidate plan = candidate.support();
                  if (hitPos.equals(plan.supportPos())
                     && hit.getDirection() == plan.face()
                     && isPointInsideFace(plan.faceRect(), plan.face(), hit.getLocation(), 0.0)) {
                     return hit;
                  } else {
                     return findHitBox(hitState, hitPos, hit) != null ? hit : null;
                  }
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static AABB[] extractFaceRects(BlockState state, BlockPos support, Direction face, int max) {
      AABB[] boxes = getShapeBoxes(state, support);
      if (boxes.length == 0) {
         return EMPTY_BOXES;
      }

      int count = Math.min(boxes.length, 16);
      AABB[] rects = new AABB[count];

      for (int i = 0; i < count; i++) {
         rects[i] = projectBoxToFace(boxes[i], face);
      }

      count = mergeCoplanarRects(rects, count, face);

      for (int i = 1; i < count; i++) {
         AABB key = rects[i];
         double area = faceArea(key, face);

         int j;
         for (j = i - 1; j >= 0 && faceArea(rects[j], face) < area; j--) {
            rects[j + 1] = rects[j];
         }

         rects[j + 1] = key;
      }

      int kept = Math.min(count, Math.max(1, max));
      if (kept == rects.length) {
         return rects;
      }

      AABB[] out = new AABB[kept];
      System.arraycopy(rects, 0, out, 0, kept);
      return out;
   }

   public static AABB findHitBox(BlockState state, BlockPos pos, BlockHitResult hit) {
      BlockPlacementPlanner.FaceBounds landing = findHitFaceBounds(state, pos, hit);
      return landing == null ? null : landing.box();
   }

   public static BlockPlacementPlanner.FaceBounds findHitFaceBounds(BlockState state, BlockPos pos, BlockHitResult hit) {
      if (state != null && pos != null && hit != null) {
         Vec3 point = hit.getLocation();
         Direction reported = hit.getDirection();
         AABB[] boxes = getShapeBoxes(state, pos);

         for (AABB box : boxes) {
            if (isPointInsideFace(box, reported, point, 0.0)) {
               return new BlockPlacementPlanner.FaceBounds(box, reported);
            }
         }

         for (AABB box : boxes) {
            for (Direction face : DEFAULT_FACE_ORDER) {
               if (face != reported && isPointInsideFace(box, face, point, 0.0)) {
                  return new BlockPlacementPlanner.FaceBounds(box, face);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean isPointInsideFace(AABB rect, Direction face, Vec3 point, double margin) {
      if (rect != null && face != null && point != null) {
         Axis normal = face.getAxis();
         return Math.abs(axisComponent(point, normal) - facePlaneCoord(rect, face)) > 1.0E-4
            ? false
            : isCoordInsideInsetAxis(rect, tangentAxis(normal, true), point, margin) && isCoordInsideInsetAxis(rect, tangentAxis(normal, false), point, margin);
      } else {
         return false;
      }
   }

   private static boolean isCoordInsideInsetAxis(AABB rect, Axis axis, Vec3 point, double margin) {
      double inset = safeVerifyInset(rect, axis, margin);
      double at = axisComponent(point, axis);
      return at >= rect.min(axis) + inset - 1.0E-4 && at <= rect.max(axis) - inset + 1.0E-4;
   }

   private static double safeVerifyInset(AABB rect, Axis axis, double margin) {
      double extent = rect.max(axis) - rect.min(axis);
      return Math.max(0.0, Math.min(margin, extent * 0.5 - margin));
   }

   public static double signedDistancePastFace(Vec3 eye, AABB rect, Direction face) {
      if (eye != null && rect != null && face != null) {
         double plane = facePlaneCoord(rect, face);
         double along = axisComponent(eye, face.getAxis());
         return (along - plane) * (face.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 : -1.0);
      } else {
         return 0.0;
      }
   }

   public static Vec3 faceCenter(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = tangentAxis(normal, true);
      Axis second = tangentAxis(normal, false);
      return composeFacePoint(normal, facePlaneCoord(rect, face), (rect.min(first) + rect.max(first)) * 0.5, (rect.min(second) + rect.max(second)) * 0.5);
   }

   public static boolean isSelfOccludedByLid(Vec3 eye, BlockPos support, Direction face, Level level) {
      if (eye == null || support == null || face == null || level == null) {
         return false;
      } else if (!face.getAxis().isHorizontal()) {
         return false;
      } else if (Mth.floor(eye.x) == support.getX() && Mth.floor(eye.z) == support.getZ()) {
         BlockPos lid = support.above();
         return eye.y >= lid.getY() + 1.0 && level.getBlockState(lid).isCollisionShapeFullBlock(level, lid);
      } else {
         return false;
      }
   }

   public static boolean wouldInteractWithoutSneak(BlockState state, BlockPos pos, boolean sneaking) {
      return state != null && pos != null && isInteractiveBlock(state, pos) ? !sneaking : false;
   }

   public static boolean requiresSneakToPlace(BlockState state, BlockPos pos) {
      return wouldInteractWithoutSneak(state, pos, false) && !wouldInteractWithoutSneak(state, pos, true);
   }

   public static boolean isValidSupportBlock(BlockState state, BlockPos support, boolean sneaking) {
      if (state != null && support != null && !state.isAir()) {
         return getShapeBoxes(state, support).length == 0 ? false : !wouldInteractWithoutSneak(state, support, sneaking);
      } else {
         return false;
      }
   }

   public static boolean canPlaceInReplaceableCell(BlockPos cell, Level level) {
      if (cell != null && level != null) {
         BlockState state = level.getBlockState(cell);
         if (!state.isAir() && state.canBeReplaced()) {
            return isInteractiveBlock(state, cell) ? false : getShapeBoxes(state, cell).length > 0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static BlockHitResult raycastBlock(Vec3 eye, RotationMath.Rotation rotation, double reach, Level level, Entity entity) {
      if (eye != null && rotation != null && level != null) {
         Vec3 end = eye.add(directionVector(rotation).scale(reach));
         HitResult result = level.clip(
            new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, entity == null ? CollisionContext.empty() : CollisionContext.of(entity))
         );
         return result instanceof BlockHitResult blockHit && result.getType() == Type.BLOCK ? blockHit : null;
      } else {
         return null;
      }
   }

   public static Axis tangentAxis(Axis normal, boolean first) {
      Axis var10000;
      switch (normal) {
         case X:
            var10000 = first ? Axis.Y : Axis.Z;
            break;
         case Y:
            var10000 = first ? Axis.X : Axis.Z;
            break;
         case Z:
            var10000 = first ? Axis.X : Axis.Y;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static Vec3 composeFacePoint(Axis normal, double plane, double a, double b) {
      Vec3 var10000;
      switch (normal) {
         case X:
            var10000 = new Vec3(plane, a, b);
            break;
         case Y:
            var10000 = new Vec3(a, plane, b);
            break;
         case Z:
            var10000 = new Vec3(a, b, plane);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static double axisComponent(Vec3 point, Axis axis) {
      double var10000;
      switch (axis) {
         case X:
            var10000 = point.x;
            break;
         case Y:
            var10000 = point.y;
            break;
         case Z:
            var10000 = point.z;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static void clearCaches() {
      for (int i = 0; i < 8; i++) {
         SHAPE_CACHE_POSITIONS[i] = null;
         SHAPE_CACHE_STATES[i] = null;
         SHAPE_CACHE_BOXES[i] = null;
      }

      for (int i = 0; i < 48; i++) {
         if (CANDIDATE_CACHE[i] != null) {
            CANDIDATE_CACHE[i].valid = false;
         }
      }
   }

   private static BlockPlacementPlanner.CandidateCache getOrBuildCandidateCache(BlockPlacementPlanner.Query request, Level level) {
      long key = computeCacheKey(request, level);

      for (int i = 0; i < 48; i++) {
         BlockPlacementPlanner.CandidateCache slot = CANDIDATE_CACHE[i];
         if (slot != null && slot.valid && slot.cacheKey == key) {
            return slot;
         }
      }

      BlockPlacementPlanner.CandidateCache slot = CANDIDATE_CACHE[nextCandidateCacheSlot];
      if (slot == null) {
         slot = CANDIDATE_CACHE[nextCandidateCacheSlot] = new BlockPlacementPlanner.CandidateCache();
      }

      nextCandidateCacheSlot = (nextCandidateCacheSlot + 1) % 48;
      slot.candidates.clear();
      slot.nextStartIndex = 0;
      Predicate<BlockPlacementPlanner.SupportFace> held = request.bannedSupports();
      request.withBannedSupports(null);
      collectCandidates(request, slot.candidates);
      request.withBannedSupports(held);
      slot.cacheKey = key;
      slot.valid = true;
      return slot;
   }

   private static long computeCacheKey(BlockPlacementPlanner.Query request, Level level) {
      long key = hashCellNeighborhood(request.targetCell(), level, new MutableBlockPos());
      key = key * 31L + System.identityHashCode(level);

      for (Direction face : request.faceOrder()) {
         key = key * 31L + face.ordinal();
      }

      EnumSet<Direction> allowed = request.allowedFaces();
      key = key * 31L + (allowed == null ? -1L : allowed.hashCode());
      key = key * 31L + (request.allowInPlace() ? 1L : 0L);
      key = key * 31L + request.maxFaceRects();
      key = key * 31L + (request.isSneaking() ? 2L : 0L);
      return key * 31L + (request.allowAutoSneak() ? 4L : 0L);
   }

   private static AABB[] getShapeBoxes(BlockState state, BlockPos pos) {
      if (state != null && pos != null) {
         for (int i = 0; i < 8; i++) {
            if (SHAPE_CACHE_STATES[i] == state && pos.equals(SHAPE_CACHE_POSITIONS[i])) {
               return SHAPE_CACHE_BOXES[i];
            }
         }

         Level level = currentLevel();
         if (level == null) {
            return EMPTY_BOXES;
         }

         Player player = currentPlayer();
         VoxelShape shape = state.getShape(level, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player));
         List<AABB> local = shape.isEmpty() ? List.of() : shape.toAabbs();
         AABB[] boxes = local.isEmpty() ? EMPTY_BOXES : new AABB[local.size()];

         for (int i = 0; i < local.size(); i++) {
            boxes[i] = local.get(i).move(pos);
         }

         SHAPE_CACHE_POSITIONS[nextShapeCacheSlot] = pos.immutable();
         SHAPE_CACHE_STATES[nextShapeCacheSlot] = state;
         SHAPE_CACHE_BOXES[nextShapeCacheSlot] = boxes;
         nextShapeCacheSlot = (nextShapeCacheSlot + 1) % 8;
         return boxes;
      } else {
         return EMPTY_BOXES;
      }
   }

   private static int mergeCoplanarRects(AABB[] rects, int count, Direction face) {
      Axis normal = face.getAxis();
      Axis first = tangentAxis(normal, true);
      Axis second = tangentAxis(normal, false);
      boolean merged = true;

      while (merged && count > 1) {
         merged = false;

         for (int i = 0; i < count && !merged; i++) {
            for (int j = i + 1; j < count; j++) {
               if (!(Math.abs(facePlaneCoord(rects[i], face) - facePlaneCoord(rects[j], face)) > 1.0E-4)) {
                  AABB union = tryUnionRects(rects[i], rects[j], first, second);
                  if (union != null) {
                     rects[i] = union;
                     rects[j] = rects[count - 1];
                     count--;
                     merged = true;
                     break;
                  }
               }
            }
         }
      }

      return count;
   }

   private static AABB tryUnionRects(AABB left, AABB right, Axis first, Axis second) {
      if (rectContains(left, right, first, second)) {
         return left;
      } else if (rectContains(right, left, first, second)) {
         return right;
      } else if (spansMatchOnAxis(left, right, first) && spansTouchOrOverlapOnAxis(left, right, second)) {
         return left.minmax(right);
      } else {
         return spansMatchOnAxis(left, right, second) && spansTouchOrOverlapOnAxis(left, right, first) ? left.minmax(right) : null;
      }
   }

   private static boolean rectContains(AABB outer, AABB inner, Axis first, Axis second) {
      return outer.min(first) - 1.0E-4 <= inner.min(first)
         && outer.max(first) + 1.0E-4 >= inner.max(first)
         && outer.min(second) - 1.0E-4 <= inner.min(second)
         && outer.max(second) + 1.0E-4 >= inner.max(second);
   }

   private static boolean spansMatchOnAxis(AABB left, AABB right, Axis axis) {
      return Math.abs(left.min(axis) - right.min(axis)) <= 1.0E-4 && Math.abs(left.max(axis) - right.max(axis)) <= 1.0E-4;
   }

   private static boolean spansTouchOrOverlapOnAxis(AABB left, AABB right, Axis axis) {
      return left.min(axis) <= right.max(axis) + 1.0E-4 && right.min(axis) <= left.max(axis) + 1.0E-4;
   }

   private static AABB projectBoxToFace(AABB box, Direction face) {
      double plane = facePlaneCoord(box, face);
      AABB var10000;
      switch (face.getAxis()) {
         case X:
            var10000 = new AABB(plane, box.minY, box.minZ, plane, box.maxY, box.maxZ);
            break;
         case Y:
            var10000 = new AABB(box.minX, plane, box.minZ, box.maxX, plane, box.maxZ);
            break;
         case Z:
            var10000 = new AABB(box.minX, box.minY, plane, box.maxX, box.maxY, plane);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static double facePlaneCoord(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      return face.getAxisDirection() == AxisDirection.POSITIVE ? rect.max(normal) : rect.min(normal);
   }

   private static double faceArea(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = tangentAxis(normal, true);
      Axis second = tangentAxis(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private static boolean isInteractiveBlock(BlockState state, BlockPos pos) {
      Level level = currentLevel();
      if (level != null && state.getMenuProvider(level, pos) != null) {
         return true;
      }

      net.minecraft.world.level.block.Block block = state.getBlock();
      if (block instanceof FenceBlock) {
         return isPlayerHoldingLeashedEntity();
      }

      boolean safeBlockEntity = block instanceof AbstractSkullBlock
         || block instanceof ConduitBlock
         || block instanceof SculkSensorBlock
         || block instanceof SculkShriekerBlock
         || block instanceof SculkCatalystBlock
         || block instanceof SpawnerBlock
         || block instanceof BrushableBlock;
      return !safeBlockEntity && block instanceof BaseEntityBlock
         ? true
         : block instanceof BedBlock
            || block instanceof DoorBlock
            || block instanceof TrapDoorBlock
            || block instanceof FenceGateBlock
            || block instanceof ButtonBlock
            || block instanceof LeverBlock
            || block instanceof NoteBlock
            || block instanceof CakeBlock
            || block instanceof CandleCakeBlock
            || block instanceof RespawnAnchorBlock
            || block instanceof DiodeBlock
            || block instanceof DragonEggBlock
            || block instanceof RedstoneWireBlock
            || block instanceof RedStoneOreBlock
            || block instanceof FlowerPotBlock
            || block instanceof AbstractCauldronBlock
            || block instanceof ComposterBlock
            || block instanceof CaveVines
            || block instanceof SweetBerryBushBlock;
   }

   private static boolean isPlayerHoldingLeashedEntity() {
      Player player = currentPlayer();
      if (player == null) {
         return false;
      }

      int tick = AutismSessionState.getInstance().getClientTick();
      if (tick != lastLeashCheckTick) {
         lastLeashCheckTick = tick;
         cachedHoldingLeashedEntity = !Leashable.leashableInArea(player, led -> led.getLeashHolder() == player).isEmpty();
      }

      return cachedHoldingLeashedEntity;
   }

   private static Vec3 directionVector(RotationMath.Rotation rotation) {
      float yaw = -rotation.yaw() * 0.017453292F;
      float pitch = -rotation.pitch() * 0.017453292F;
      float cosPitch = Mth.cos(pitch);
      return new Vec3(Mth.sin(yaw) * cosPitch, Mth.sin(pitch), Mth.cos(yaw) * cosPitch);
   }

   private static RotationMath.Rotation resolveFromRotation(BlockPlacementPlanner.Query request) {
      if (request.fromRotation() != null) {
         return request.fromRotation();
      }

      Player player = currentPlayer();
      return player == null ? null : new RotationMath.Rotation(player.getYRot(), player.getXRot());
   }

   private static Level currentLevel() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? null : mc.level;
   }

   private static Player currentPlayer() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? null : mc.player;
   }

   private static void setRefusal(BlockPlacementPlanner.RejectReason[] refusal, BlockPlacementPlanner.RejectReason value) {
      if (refusal != null && refusal.length > 0) {
         refusal[0] = value;
      }
   }

   private static BlockPlacementPlanner.AimSample rejectAim(
      BlockPlacementPlanner.RejectReason[] refusal, BlockPlacementPlanner.RejectReason reason, BlockPlacementPlanner.Query request, Direction face
   ) {
      setRefusal(refusal, reason);
      appendTrace(request, face, reason);
      return null;
   }

   private static BlockPlacementPlanner.PlacementPlan rejectPlan(
      BlockPlacementPlanner.RejectReason[] refusal, BlockPlacementPlanner.RejectReason reason, BlockPlacementPlanner.Query request, Direction face
   ) {
      setRefusal(refusal, reason);
      appendTrace(request, face, reason);
      return null;
   }

   private static void appendTrace(BlockPlacementPlanner.Query request, Direction face, BlockPlacementPlanner.RejectReason reason) {
      if (request != null && face != null) {
         StringBuilder trace = request.trace();
         if (trace != null && trace.length() < 96) {
            if (trace.length() > 0) {
               trace.append(' ');
            }

            trace.append(face.getSerializedName().charAt(0)).append(':').append(reason.shortCode());
         }
      }
   }

   public record SupportFace(BlockPos supportPos, Direction face) {
      public SupportFace {
         supportPos = supportPos == null ? null : supportPos.immutable();
      }
   }

   public record PlacementPlan(
      BlockPlacementPlanner.SupportCandidate support, BlockPlacementPlanner.AimSample aim, BlockHitResult hit, boolean substituted, double score
   ) {
   }

   public record FaceBounds(AABB box, Direction face) {
   }

   public record AimSample(
      Vec3 point, RotationMath.Rotation goalRotation, RotationMath.Rotation emittedRotation, double distance, float turnDegrees, BlockPlacementPlanner.SamplePoint samplePoint
   ) {
   }

   public enum RejectReason {
      NONE("ok"),
      NOT_A_SUPPORT("ns"),
      SELF_OCCLUDED("so"),
      BEHIND_PLANE("sd"),
      OUT_OF_REACH("rc"),
      PAST_PITCH_CAP("pc"),
      BANNED("st"),
      OUTRANKED("pr"),
      RAY_MISSED("rm"),
      RAY_WRONG_CELL("rw"),
      NO_BUDGET("bg");

      private final String shortCode;

      RejectReason(String code) {
         this.shortCode = code;
      }

      public String shortCode() {
         return this.shortCode;
      }
   }

   public interface HitValidator {
      boolean lands(BlockHitResult var1, BlockPos var2);

      default boolean init(BlockState state, BlockPos pos, boolean sneaking) {
         return !BlockPlacementPlanner.wouldInteractWithoutSneak(state, pos, sneaking);
      }
   }

   public static final class Query {
      private BlockPos targetCell;
      private final Vec3 eyePos;
      private final double reach;
      private BlockPlacementPlanner.HitValidator hitValidator;
      private RotationMath.Rotation fromRotation;
      private double maxPitchDegrees = BlockPlacementPlanner.defaultMaxPitchDegrees();
      private boolean sneaking;
      private boolean allowAutoSneak;
      private Direction[] faceOrder;
      private EnumSet<Direction> allowedFaces;
      private boolean allowInPlace;
      private Vec3 leadEyePos;
      private Predicate<BlockPlacementPlanner.SupportFace> bannedSupports;
      private BlockPlacementPlanner.RayBudget rayBudget;
      private int maxFaceRects;
      private boolean quantizeGcd;
      private double marginCap;
      private StringBuilder trace;

      public Query(BlockPos cell, Vec3 eye, double reach, BlockPlacementPlanner.HitValidator placement) {
         this.faceOrder = BlockPlacementPlanner.DEFAULT_FACE_ORDER;
         this.allowInPlace = true;
         this.maxFaceRects = 3;
         this.quantizeGcd = true;
         this.marginCap = 1.0 / 0.0;
         this.targetCell = cell == null ? null : cell.immutable();
         this.eyePos = eye;
         this.reach = reach;
         this.hitValidator = placement;
      }

      public BlockPlacementPlanner.Query withTargetCell(BlockPos cell) {
         this.targetCell = cell == null ? null : cell.immutable();
         return this;
      }

      public BlockPlacementPlanner.Query withHitValidator(BlockPlacementPlanner.HitValidator placement) {
         this.hitValidator = placement;
         return this;
      }

      public BlockPlacementPlanner.Query withFromRotation(RotationMath.Rotation from) {
         this.fromRotation = from;
         return this;
      }

      public BlockPlacementPlanner.Query withMaxPitch(double degrees) {
         this.maxPitchDegrees = degrees;
         return this;
      }

      public BlockPlacementPlanner.Query withSneaking(boolean sneaking) {
         this.sneaking = sneaking;
         return this;
      }

      public BlockPlacementPlanner.Query withAutoSneak(boolean allowed) {
         this.allowAutoSneak = allowed;
         return this;
      }

      public BlockPlacementPlanner.Query withFaceOrder(Direction[] order) {
         this.faceOrder = order != null && order.length != 0 ? order : BlockPlacementPlanner.DEFAULT_FACE_ORDER;
         return this;
      }

      public BlockPlacementPlanner.Query withAllowedFaces(EnumSet<Direction> faces) {
         this.allowedFaces = faces;
         return this;
      }

      public BlockPlacementPlanner.Query withAllowInPlace(boolean allow) {
         this.allowInPlace = allow;
         return this;
      }

      public BlockPlacementPlanner.Query withLeadEye(Vec3 leadEye) {
         this.leadEyePos = leadEye;
         return this;
      }

      public BlockPlacementPlanner.Query withBannedSupports(Predicate<BlockPlacementPlanner.SupportFace> banned) {
         this.bannedSupports = banned;
         return this;
      }

      public BlockPlacementPlanner.Query withRayBudget(BlockPlacementPlanner.RayBudget budget) {
         this.rayBudget = budget;
         return this;
      }

      public BlockPlacementPlanner.Query withMaxFaceRects(int max) {
         this.maxFaceRects = Math.max(1, max);
         return this;
      }

      public BlockPlacementPlanner.Query withQuantizeGcd(boolean quantize) {
         this.quantizeGcd = quantize;
         return this;
      }

      public BlockPlacementPlanner.Query withMarginCap(double cap) {
         this.marginCap = Double.isNaN(cap) ? 1.0 / 0.0 : Math.max(0.0, cap);
         return this;
      }

      public double effectiveMargin(double distance) {
         return Math.min(BlockPlacementPlanner.marginForDistance(distance), this.marginCap);
      }

      public BlockPlacementPlanner.Query withTrace(StringBuilder trace) {
         this.trace = trace;
         return this;
      }

      public BlockPos targetCell() {
         return this.targetCell;
      }

      public Vec3 eyePos() {
         return this.eyePos;
      }

      public double reach() {
         return this.reach;
      }

      public BlockPlacementPlanner.HitValidator hitValidator() {
         return this.hitValidator;
      }

      public RotationMath.Rotation fromRotation() {
         return this.fromRotation;
      }

      public double maxPitchDegrees() {
         return this.maxPitchDegrees;
      }

      public boolean isSneaking() {
         return this.sneaking;
      }

      public boolean allowAutoSneak() {
         return this.allowAutoSneak;
      }

      public Direction[] faceOrder() {
         return this.faceOrder;
      }

      public EnumSet<Direction> allowedFaces() {
         return this.allowedFaces;
      }

      public boolean allowInPlace() {
         return this.allowInPlace;
      }

      public Vec3 leadEyePos() {
         return this.leadEyePos;
      }

      public Predicate<BlockPlacementPlanner.SupportFace> bannedSupports() {
         return this.bannedSupports;
      }

      public BlockPlacementPlanner.RayBudget rayBudget() {
         return this.rayBudget;
      }

      public int maxFaceRects() {
         return this.maxFaceRects;
      }

      public boolean quantizeGcd() {
         return this.quantizeGcd;
      }

      public StringBuilder trace() {
         return this.trace;
      }
   }

   public static final class RayBudget {
      private int remainingRays;

      public RayBudget(int rays) {
         this.remainingRays = Math.max(0, rays);
      }

      public int remaining() {
         return this.remainingRays;
      }

      public boolean consume() {
         if (this.remainingRays <= 0) {
            return false;
         }

         this.remainingRays--;
         return true;
      }

      public void reset(int rays) {
         this.remainingRays = Math.max(0, rays);
      }
   }

   private static final class CandidateCache {
      private final List<BlockPlacementPlanner.SupportCandidate> candidates = new ArrayList<>();
      private long cacheKey;
      private boolean valid;
      private int nextStartIndex;
   }

   public record SupportCandidate(
      BlockPos targetCell,
      BlockPos supportPos,
      Direction face,
      int placementForm,
      BlockState supportState,
      AABB faceRect,
      double faceArea,
      double edgeMarginRatio,
      boolean requiresSneak,
      BlockPlacementPlanner.SupportFace supportFace
   ) {
   }

   public enum SamplePoint {
      CENTRE,
      NEAREST,
      FLATTEST,
      ESCAPE;
   }
}
