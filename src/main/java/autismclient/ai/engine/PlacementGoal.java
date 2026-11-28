package autismclient.ai.engine;

import autismclient.util.AutismLiteVariant;
import autismclient.util.BlockPlacementPlanner;
import autismclient.util.RotationMath;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

public final class PlacementGoal implements AiPathfinder.PathGoal {
   private final BlockPos targetBlock;
   private final Item item;
   private final double reach;
   private final double eyeHeight;
   private final AiPathfinder.GridView grid;
   private final AABB workVolume;
   private final Set<Long> rejected;
   private final AiPathfinder.PathGoal interactionGoal;
   private final Set<Long> protectedCells;
   private final PlacementGoal.PlaceConstraint constraint;
   private Set<Long> allowedStances;
   private Map<Long, PlacementGoal.ClickGeometry> clickGeometry;
   private boolean allowsEdits = true;
   private final List<PlacementGoal> alternatives;

   public PlacementGoal(BlockPos target, Item item, double reach, double eyeHeight) {
      this(target, item, reach, eyeHeight, null);
   }

   public PlacementGoal(BlockPos target, Item item, double reach, double eyeHeight, AABB workVolume) {
      this(target, item, reach, eyeHeight, workVolume, null);
   }

   public PlacementGoal(BlockPos target, Item item, double reach, double eyeHeight, AABB workVolume, Set<Long> protectedCells) {
      this(target, item, reach, eyeHeight, workVolume, protectedCells, null);
   }

   public PlacementGoal(BlockPos target, Item item, double reach, double eyeHeight, AABB workVolume, Set<Long> protectedCells, PlacementGoal.PlaceConstraint constraint) {
      this(target, item, reach, eyeHeight, null, ConcurrentHashMap.newKeySet(), workVolume, protectedCells, constraint, List.of());
   }

   private PlacementGoal(
      BlockPos target,
      Item item,
      double reach,
      double eyeHeight,
      AiPathfinder.GridView grid,
      Set<Long> rejected,
      AABB workVolume,
      Set<Long> protectedCells,
      PlacementGoal.PlaceConstraint constraint,
      List<PlacementGoal> alternatives
   ) {
      this.targetBlock = target.immutable();
      this.item = item;
      this.reach = reach;
      this.eyeHeight = eyeHeight;
      this.protectedCells = protectedCells;
      this.constraint = constraint;
      this.alternatives = alternatives;
      this.grid = grid;
      this.rejected = rejected;
      this.workVolume = workVolume;
      this.interactionGoal = new InteractionGoal(target, reach, eyeHeight);
   }

   @Override
   public AiPathfinder.PathGoal init(AiPathfinder.GridView grid) {
      ArrayList<PlacementGoal> bound = new ArrayList<>(this.alternatives.size());

      for (PlacementGoal alternative : this.alternatives) {
         bound.add((PlacementGoal)alternative.init(grid));
      }

      PlacementGoal result = new PlacementGoal(
         this.targetBlock,
         this.item,
         this.reach,
         this.eyeHeight,
         grid,
         this.rejected,
         this.workVolume,
         this.protectedCells,
         this.constraint,
         List.copyOf(bound)
      );
      result.allowedStances = this.allowedStances;
      result.clickGeometry = this.clickGeometry;
      result.allowsEdits = this.allowsEdits;
      return result;
   }

   PlacementGoal withStances(Set<Long> stances) {
      PlacementGoal result = new PlacementGoal(
         this.targetBlock,
         this.item,
         this.reach,
         this.eyeHeight,
         this.grid,
         this.rejected,
         this.workVolume,
         this.protectedCells,
         this.constraint,
         this.alternatives
      );
      result.allowedStances = Set.copyOf(stances);
      result.clickGeometry = this.clickGeometry;
      result.allowsEdits = this.allowsEdits;
      return result;
   }

   PlacementGoal withProofs(Map<Long, PlacementGoal.ClickGeometry> placements) {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException();
      }

      PlacementGoal result = this.withStances(placements.keySet());
      result.clickGeometry = Map.copyOf(placements);
      return result;
   }

   PlacementGoal clientThreadProofsOnly() {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException();
      }

      PlacementGoal result = new PlacementGoal(
         this.targetBlock,
         this.item,
         this.reach,
         this.eyeHeight,
         this.grid,
         this.rejected,
         this.workVolume,
         this.protectedCells,
         this.constraint,
         this.alternatives
      );
      result.allowedStances = this.allowedStances;
      result.clickGeometry = this.clickGeometry;
      result.allowsEdits = false;
      return result;
   }

   public static PlacementGoal ofTargets(List<PlacementGoal.PlacementRequest> targets, double reach, double eyeHeight, AABB workVolume, Set<Long> protectedCells) {
      return create(targets, reach, eyeHeight, workVolume, protectedCells, Set.of());
   }

   public static PlacementGoal create(List<PlacementGoal.PlacementRequest> targets, double reach, double eyeHeight, AABB workVolume, Set<Long> protectedCells, Set<Long> avoid) {
      if (AutismLiteVariant.enabled()) {
         throw new IllegalStateException();
      }

      if (targets.isEmpty()) {
         throw new IllegalArgumentException("A batch approach needs at least one target");
      }

      Set<Long> rejected = ConcurrentHashMap.newKeySet();
      rejected.addAll(avoid);
      ArrayList<PlacementGoal> alternatives = new ArrayList<>(targets.size() - 1);

      for (int i = 1; i < targets.size(); i++) {
         PlacementGoal.PlacementRequest t = targets.get(i);
         alternatives.add(new PlacementGoal(t.pos(), t.item(), reach, eyeHeight, null, rejected, workVolume, protectedCells, t.constraint(), List.of()));
      }

      PlacementGoal.PlacementRequest first = targets.getFirst();
      return new PlacementGoal(first.pos(), first.item(), reach, eyeHeight, null, rejected, workVolume, protectedCells, first.constraint(), List.copyOf(alternatives));
   }

   public String itemId() {
      return BuiltInRegistries.ITEM.getKey(this.item).toString();
   }

   public AABB getWorkVolume() {
      return this.workVolume;
   }

   public boolean hasProtectedCells() {
      return this.protectedCells != null;
   }

   @Override
   public double blockPlaceCost() {
      return 40.0;
   }

   @Override
   public boolean allowsCell(BlockPos cell) {
      return this.protectedCells != null || this.workVolume == null || !this.workVolume.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5);
   }

   @Override
   public boolean load(BlockPos cell) {
      if (!this.allowsEdits) {
         return false;
      } else {
         return this.protectedCells != null
            ? !this.protectedCells.contains(cell.asLong()) && (this.workVolume == null || cell.getY() >= Math.floor(this.workVolume.minY) || this.sync(cell))
            : this.workVolume != null && !this.workVolume.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5);
      }
   }

   private boolean sync(BlockPos cell) {
      if (this.constraint != null
         && this.constraint.crouchForLook()
         && Math.abs(cell.getX() - this.targetBlock.getX()) <= 2
         && Math.abs(cell.getZ() - this.targetBlock.getZ()) <= 2
         && cell.getY() >= this.targetBlock.getY() - 3) {
         return true;
      }

      for (PlacementGoal alternative : this.alternatives) {
         if (alternative.sync(cell)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean read(BlockPos cell) {
      if (!this.allowsEdits) {
         return false;
      }

      if (cell.equals(this.targetBlock)) {
         return false;
      }

      for (PlacementGoal alternative : this.alternatives) {
         if (cell.equals(alternative.targetBlock)) {
            return false;
         }
      }

      return this.protectedCells != null
         ? !this.protectedCells.contains(cell.asLong())
         : this.workVolume == null || !this.workVolume.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5);
   }

   @Override
   public double heuristic(int x, int y, int z) {
      if (this.allowedStances != null) {
         double best = 1.7976931348623157E308;

         for (long stance : this.allowedStances) {
            if (!this.rejected.contains(stance)) {
               best = Math.min(best, AiPathfinder.MoveCosts.estimateDirectionalHeuristicCompat(BlockPos.getX(stance) - x, BlockPos.getY(stance) - y, BlockPos.getZ(stance) - z));
            }
         }

         return best == 1.7976931348623157E308 ? 0.0 : best;
      } else {
         double best = this.ownHeuristic(x, y, z);

         for (PlacementGoal alternative : this.alternatives) {
            best = Math.min(best, alternative.ownHeuristic(x, y, z));
         }

         return best;
      }
   }

   private double ownHeuristic(int x, int y, int z) {
      double rise = Math.max(0.0, this.targetBlock.getY() - y - this.eyeHeight - this.reach - 1.0);
      return Math.max(this.interactionGoal.heuristic(x, y, z), rise * 5.5)
         + (y > this.targetBlock.getY() ? (y - this.targetBlock.getY()) * 0.5 : 0.0);
   }

   @Override
   public BlockPos getTargetBlock() {
      return this.targetBlock;
   }

   @Override
   public boolean outsideSearchBox(BlockPos origin, int horizontal, int vertical) {
      if (!this.interactionGoal.outsideSearchBox(origin, horizontal, vertical)) {
         return false;
      }

      for (PlacementGoal alternative : this.alternatives) {
         if (!alternative.interactionGoal.outsideSearchBox(origin, horizontal, vertical)) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.isInGoalWithGrid(x, y, z, this.grid);
   }

   @Override
   public boolean isInGoalWithGrid(int x, int y, int z, AiPathfinder.GridView grid) {
      long feet = BlockPos.asLong(x, y, z);
      if (grid != null && !this.rejected.contains(feet) && (this.allowedStances == null || this.allowedStances.contains(feet))) {
         if (this.clickGeometry != null) {
            PlacementGoal.ClickGeometry proof = this.clickGeometry.get(feet);
            AiPathfinder.CellInfo floor = grid.probeCell(x, y - 1, z);
            if (proof != null && floor != null && floor.loaded()) {
               double offset = floor.passable() ? 0.0 : floor.top16() / 16.0 - 1.0;
               if (Math.abs(offset - proof.floorOffset()) > 0.001) {
                  return false;
               }

               Vec3 point = proof.point().add(-proof.face().getStepX() * 0.001, -proof.face().getStepY() * 0.001, -proof.face().getStepZ() * 0.001);
               return InteractionGoal.canReach(grid, proof.support(), x, y, z, proof.eyeHeight(), point.x, point.y, point.z, this.reach, proof.face(), null);
            } else {
               return false;
            }
         } else {
            if (this.isStanceCandidate(x, y, z, grid)) {
               return true;
            }

            for (PlacementGoal alternative : this.alternatives) {
               if (alternative.isStanceCandidate(x, y, z, grid)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean isStanceCandidate(int x, int y, int z, AiPathfinder.GridView grid) {
      if (!(Math.abs(x - this.targetBlock.getX()) > this.reach + 1.5) && !(Math.abs(z - this.targetBlock.getZ()) > this.reach + 1.5)) {
         AiPathfinder.CellInfo floor = grid.probeCell(x, y - 1, z);
         double actualEyeHeight = floor != null && !floor.passable() ? this.eyeHeight - 1.0 + floor.top16() / 16.0 : this.eyeHeight;
         double feetOffset = actualEyeHeight - this.eyeHeight;
         if (this.constraint != null && this.constraint.crouchForLook()) {
            actualEyeHeight -= Math.max(0.0, this.eyeHeight - 1.27);
         }

         if (this.protectedCells == null && this.workVolume != null && this.workVolume.intersects(x + 0.2, y, z + 0.2, x + 0.8, y + 1.8, z + 0.8)) {
            return false;
         }

         if (this.constraint != null && this.constraint.placedShape() != null) {
            double feetY = y + feetOffset;
            AABB body = new AABB(x + 0.2, feetY, z + 0.2, x + 0.8, feetY + 1.8, z + 0.8);

            for (AABB box : AiShapes.boxes(this.constraint.placedShape())) {
               if (box.move(this.targetBlock.getX(), this.targetBlock.getY(), this.targetBlock.getZ()).intersects(body)) {
                  return false;
               }
            }
         } else if (x == this.targetBlock.getX()
            && z == this.targetBlock.getZ()
            && this.targetBlock.getY() >= y
            && this.targetBlock.getY() <= y + 1
            && (
               !(this.item instanceof BlockItem blockItem)
                  || blockItem.getBlock().defaultBlockState().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
            )) {
            return false;
         }

         for (Direction face : Direction.values()) {
            if (this.constraint == null || this.constraint.acceptsFace(face.getOpposite())) {
               BlockPos support = this.targetBlock.relative(face);
               AiPathfinder.CellInfo p = grid.probeCell(support.getX(), support.getY(), support.getZ());
               if (p != null && p.loaded() && !p.lava()) {
                  double dot = (x + 0.5 - this.targetBlock.getX() - 0.5) * face.getStepX()
                     + (y + actualEyeHeight - this.targetBlock.getY() - 0.5) * face.getStepY()
                     + (z + 0.5 - this.targetBlock.getZ() - 0.5) * face.getStepZ();
                  if (!(dot > 0.5)) {
                     for (AABB box : AiShapes.boxes(grid.collisionShapeCode(support.getX(), support.getY(), support.getZ()))) {
                        double[] heights = this.constraint != null && this.constraint.highHalf() != null && face.getAxis().isHorizontal()
                           ? (this.constraint.highHalf() ? new double[]{0.75, 0.55, 0.95} : new double[]{0.25, 0.45, 0.05})
                           : new double[]{(box.minY + box.maxY) / 2.0};

                        for (double height : heights) {
                           if (!face.getAxis().isHorizontal() || !(height < box.minY) && !(height > box.maxY)) {
                              double hitX = support.getX()
                                 + (face.getStepX() == 0 ? (box.minX + box.maxX) / 2.0 : (face.getStepX() > 0 ? box.minX + 0.001 : box.maxX - 0.001));
                              double hitY = support.getY() + (face.getStepY() == 0 ? height : (face.getStepY() > 0 ? box.minY + 0.001 : box.maxY - 0.001));
                              double hitZ = support.getZ()
                                 + (face.getStepZ() == 0 ? (box.minZ + box.maxZ) / 2.0 : (face.getStepZ() > 0 ? box.minZ + 0.001 : box.maxZ - 0.001));
                              if (face.getAxis() == Axis.Y && this.constraint != null && this.constraint.crouchForLook()) {
                                 double edgeX = Mth.clamp(x + 0.5, support.getX() + box.minX + 0.035, support.getX() + box.maxX - 0.035);
                                 double edgeZ = Mth.clamp(z + 0.5, support.getZ() + box.minZ + 0.035, support.getZ() + box.maxZ - 0.035);
                                 if (this.constraint.acceptsRay(edgeX - x - 0.5, hitY - y - actualEyeHeight, edgeZ - z - 0.5)
                                    && InteractionGoal.canReach(
                                       grid,
                                       support,
                                       x,
                                       y,
                                       z,
                                       actualEyeHeight,
                                       edgeX,
                                       hitY,
                                       edgeZ,
                                       this.reach,
                                       face.getOpposite(),
                                       this.constraint.highHalf()
                                    )) {
                                    return true;
                                 }
                              }

                              if (face.getAxis() == Axis.Y && this.constraint != null && this.constraint.clickYaw() != null) {
                                 double yawRadians = Math.toRadians(this.constraint.clickYaw().floatValue());
                                 double aimedX = hitX - Math.sin(yawRadians) * (box.maxX - box.minX) * 0.25;
                                 double aimedZ = hitZ + Math.cos(yawRadians) * (box.maxZ - box.minZ) * 0.25;
                                 if (this.constraint.acceptsRay(aimedX - x - 0.5, hitY - y - actualEyeHeight, aimedZ - z - 0.5)
                                    && InteractionGoal.canReach(
                                       grid,
                                       support,
                                       x,
                                       y,
                                       z,
                                       actualEyeHeight,
                                       aimedX,
                                       hitY,
                                       aimedZ,
                                       this.reach,
                                       face.getOpposite(),
                                       this.constraint.highHalf()
                                    )) {
                                    return true;
                                 }
                              }

                              if ((this.constraint == null || this.constraint.acceptsRay(hitX - x - 0.5, hitY - y - actualEyeHeight, hitZ - z - 0.5))
                                 && InteractionGoal.canReach(
                                    grid,
                                    support,
                                    x,
                                    y,
                                    z,
                                    actualEyeHeight,
                                    hitX,
                                    hitY,
                                    hitZ,
                                    this.reach,
                                    face.getOpposite(),
                                    this.constraint == null ? null : this.constraint.highHalf()
                                 )) {
                                 return true;
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public boolean acceptsStance(Minecraft mc) {
      if (mc.player == null || !this.rejected.contains(AiStance.standingPos(mc).asLong()) && (this.allowedStances == null || this.allowedStances.contains(AiStance.standingPos(mc).asLong()))) {
         if (this.load(mc)) {
            return true;
         }

         for (PlacementGoal alternative : this.alternatives) {
            if (alternative.load(mc)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private boolean load(Minecraft mc) {
      if (mc.player != null && mc.level != null && (this.protectedCells != null || this.workVolume == null || !mc.player.getBoundingBox().intersects(this.workVolume))) {
         if (this.constraint != null && this.constraint.placedShape() != null) {
            for (AABB box : AiShapes.boxes(this.constraint.placedShape())) {
               if (box.move(this.targetBlock.getX(), this.targetBlock.getY(), this.targetBlock.getZ()).intersects(mc.player.getBoundingBox())) {
                  return false;
               }
            }
         }

         BlockPlacementPlanner.PlacementPlan hit = planPlacement(mc, this.targetBlock, this.item, this.constraint, 72);
         if (hit != null && this.item instanceof BlockItem blockItem) {
            BlockState state = blockItem.getBlock()
               .getStateForPlacement(
                  placeContext(
                     mc.player,
                     new ItemStack(this.item),
                     hit.hit(),
                     eyePosition(mc, this.targetBlock, this.item, this.constraint, mc.player.position(), false)
                  )
               );
            if (state == null) {
               return false;
            }

            if (Shapes.joinIsNotEmpty(
               state.getCollisionShape(mc.level, this.targetBlock).move(this.targetBlock.getX(), this.targetBlock.getY(), this.targetBlock.getZ()),
               Shapes.create(mc.player.getBoundingBox()),
               BooleanOp.AND
            )) {
               return false;
            }
         }

         return hit != null;
      } else {
         return false;
      }
   }

   public static BlockPlacementPlanner.PlacementPlan planPlacement(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, int rays) {
      return planPlacement(
         mc,
         target,
         item,
         constraint,
         eyePosition(mc, target, item, constraint, mc.player.position(), false),
         mc.player.getBoundingBox(),
         mc.player.blockInteractionRange(),
         rays
      );
   }

   public static boolean canPlace(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint) {
      return !AutismLiteVariant.enabled()
         ? AiPlacementContext.init(item, constraint, target, mc.level::getBlockState)
         : constraint != null && constraint.crouchForLook();
   }

   public static double standingEyeHeight(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, boolean future) {
      double standing = future ? mc.player.getEyeHeight(Pose.STANDING) : mc.player.getEyeHeight();
      return canPlace(mc, target, item, constraint) ? Math.min(standing, mc.player.getEyeHeight(Pose.CROUCHING)) : standing;
   }

   private static Vec3 eyePosition(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, Vec3 feet, boolean future) {
      return feet.add(0.0, standingEyeHeight(mc, target, item, constraint, future), 0.0);
   }

   private static BlockPlaceContext placeContext(Player player, ItemStack stack, BlockHitResult hit, Vec3 eye) {
      return AutismLiteVariant.enabled() ? new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack, hit) : new AiPlacementContext(player, stack, hit, eye);
   }

   public static BlockPlacementPlanner.PlacementPlan planPlacement(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, Vec3 feet, int rays) {
      AABB body = new AABB(feet.x - 0.3, feet.y, feet.z - 0.3, feet.x + 0.3, feet.y + 1.8, feet.z + 0.3);
      return planPlacement(mc, target, item, constraint, eyePosition(mc, target, item, constraint, feet, true), body, mc.player.blockInteractionRange() - 0.25, rays);
   }

   private static BlockPlacementPlanner.PlacementPlan planPlacement(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, Vec3 eye, AABB body, double reach, int rays) {
      if (constraint != null && constraint.placedShape() != null) {
         for (AABB box : AiShapes.boxes(constraint.placedShape())) {
            if (box.move(target.getX(), target.getY(), target.getZ()).intersects(body)) {
               return null;
            }
         }
      }

      BlockPlacementPlanner.HitValidator placement = (hit, cell) -> isValidHit(mc, cell, item, constraint, eye, body, hit);
      BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(target, eye, reach, placement)
         .withSneaking(mc.player.isShiftKeyDown())
         .withAutoSneak(constraint != null)
         .withRayBudget(new BlockPlacementPlanner.RayBudget(rays));
      if (constraint != null) {
         if (constraint.clickYaw() != null) {
            request.withFromRotation(new RotationMath.Rotation(constraint.clickYaw(), mc.player.getXRot()));
         }

         EnumSet<Direction> faces = EnumSet.noneOf(Direction.class);

         for (Direction face : Direction.values()) {
            if (constraint.acceptsFace(face)) {
               faces.add(face);
            }
         }

         request.withAllowedFaces(faces);
      }

      if (constraint != null && (constraint.highHalf() != null || constraint.crouchForLook())) {
         ArrayList<BlockPlacementPlanner.SupportCandidate> options = new ArrayList<>();
         BlockPlacementPlanner.collectCandidates(request, options);
         BlockPlacementPlanner.RejectReason[] refusal = new BlockPlacementPlanner.RejectReason[1];

         for (BlockPlacementPlanner.SupportCandidate option : options) {
            AABB rect = constraint.highHalf() == null
               ? option.faceRect()
               : clipToHalf(option.faceRect(), option.face(), option.supportPos().getY(), constraint.highHalf());
            if (rect != null && constraint.crouchForLook()) {
               rect = clipToEyeCone(rect, option.face(), eye);
            }

            if (rect != null) {
               BlockPlacementPlanner.SupportCandidate clipped = new BlockPlacementPlanner.SupportCandidate(
                  option.targetCell(),
                  option.supportPos(),
                  option.face(),
                  option.placementForm(),
                  option.supportState(),
                  rect,
                  option.faceArea(),
                  option.edgeMarginRatio(),
                  option.requiresSneak(),
                  option.supportFace()
               );
               BlockPlacementPlanner.AimSample aim = BlockPlacementPlanner.computeAimSample(clipped, request, refusal);
               if (aim != null) {
                  BlockPlacementPlanner.PlacementPlan candidate = BlockPlacementPlanner.verifyPlacement(clipped, aim, request, refusal);
                  if (candidate != null) {
                     return candidate;
                  }

                  if (refusal[0] == BlockPlacementPlanner.RejectReason.NO_BUDGET) {
                     break;
                  }
               }
            }
         }

         return null;
      } else {
         return BlockPlacementPlanner.plan(request);
      }
   }

   static boolean isValidHit(Minecraft mc, BlockPos target, Item item, PlacementGoal.PlaceConstraint constraint, Vec3 eye, AABB body, BlockHitResult hit) {
      if (hit == null || target == null) {
         return false;
      } else if (!(item instanceof BlockItem blockItem)) {
         return false;
      } else {
         BlockPlaceContext context = placeContext(mc.player, new ItemStack(item), hit, eye);
         if (context.canPlace() && context.getClickedPos().equals(target)) {
            if (constraint == null
               || constraint.acceptsFace(hit.getDirection())
                  && constraint.acceptsRay(hit.getLocation().x - eye.x, hit.getLocation().y - eye.y, hit.getLocation().z - eye.z)) {
               BlockState state = blockItem.getBlock().getStateForPlacement(context);
               return state != null
                  && (constraint == null || constraint.acceptsHalf(state))
                  && !Shapes.joinIsNotEmpty(
                     state.getCollisionShape(mc.level, target).move(target.getX(), target.getY(), target.getZ()), Shapes.create(body), BooleanOp.AND
                  );
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   static AABB clipToEyeCone(AABB face, Direction direction, Vec3 eye) {
      if (direction.getAxis() != Axis.Y) {
         return face;
      }

      double radius = (face.minY - eye.y) * 0.96;
      if (radius <= 0.0) {
         return null;
      }

      double minX = Math.max(face.minX, eye.x - radius);
      double maxX = Math.min(face.maxX, eye.x + radius);
      double minZ = Math.max(face.minZ, eye.z - radius);
      double maxZ = Math.min(face.maxZ, eye.z + radius);
      return !(maxX - minX < 0.001) && !(maxZ - minZ < 0.001) ? new AABB(minX, face.minY, minZ, maxX, face.maxY, maxZ) : null;
   }

   static AABB clipToHalf(AABB rect, Direction face, int supportY, boolean high) {
      if (!face.getAxis().isHorizontal()) {
         return rect;
      }

      double min = high ? Math.max(rect.minY, supportY + 0.51) : rect.minY;
      double max = high ? rect.maxY : Math.min(rect.maxY, supportY + 0.49);
      return max > min ? new AABB(rect.minX, min, rect.minZ, rect.maxX, max, rect.maxZ) : null;
   }

   public boolean rejectStance(BlockPos feet) {
      if (this.rejected.size() < 64) {
         this.rejected.add(feet.asLong());
         return true;
      } else {
         this.rejected.clear();
         this.rejected.add(feet.asLong());
         return false;
      }
   }

   public record PlacementRequest(BlockPos pos, Item item, PlacementGoal.PlaceConstraint constraint, BlockState requested) {
      public PlacementRequest(BlockPos pos, Item item, PlacementGoal.PlaceConstraint constraint) {
         this(pos, item, constraint, null);
      }
   }

   public record PlaceConstraint(
      Direction clickFace,
      Direction playerFacing,
      Axis axis,
      Boolean highHalf,
      boolean fullDirection,
      Direction horizontalFace,
      Short placedShape,
      Float yaw,
      double yawTolerance
   ) {
      public PlaceConstraint(
         Direction clickFace, Direction playerFacing, Axis axis, Boolean highHalf, boolean fullDirection, Direction horizontalFace, Short placedShape
      ) {
         this(clickFace, playerFacing, axis, highHalf, fullDirection, horizontalFace, placedShape, null, 35.0);
      }

      public PlaceConstraint(Direction clickFace, Direction playerFacing, Axis axis, Boolean highHalf, boolean fullDirection, Direction horizontalFace) {
         this(clickFace, playerFacing, axis, highHalf, fullDirection, horizontalFace, null);
      }

      public PlaceConstraint(Direction clickFace, Direction playerFacing, Axis axis, Boolean highHalf) {
         this(clickFace, playerFacing, axis, highHalf, false, null);
      }

      public PlaceConstraint(Direction clickFace, Direction playerFacing, Axis axis, Boolean highHalf, boolean fullDirection) {
         this(clickFace, playerFacing, axis, highHalf, fullDirection, null);
      }

      public boolean acceptsFace(Direction face) {
         return (this.clickFace == null || face == this.clickFace)
            && (this.axis == null || face.getAxis() == this.axis)
            && (this.highHalf == null || face.getAxis().isHorizontal() || this.highHalf == (face == Direction.DOWN))
            && (this.horizontalFace == null || !face.getAxis().isHorizontal() || face == this.horizontalFace);
      }

      public boolean crouchForLook() {
         return this.fullDirection && this.playerFacing == Direction.UP;
      }

      public boolean acceptsRay(double dx, double dz) {
         return this.yaw != null
            ? Math.abs(Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0 - this.yaw.floatValue())) <= this.yawTolerance
            : this.playerFacing == null
               || !this.playerFacing.getAxis().isHorizontal()
               || Math.abs(Mth.wrapDegrees(Math.toDegrees(Math.atan2(dz, dx)) - 90.0 - this.playerFacing.toYRot())) <= 35.0;
      }

      public boolean acceptsRay(double dx, double dy, double dz) {
         if (this.fullDirection && this.playerFacing != null) {
            double x = Math.abs(dx);
            double y = Math.abs(dy);
            double z = Math.abs(dz);
            Direction nearest = y > Math.max(x, z)
               ? (dy > 0.0 ? Direction.UP : Direction.DOWN)
               : (x > z ? (dx > 0.0 ? Direction.EAST : Direction.WEST) : (dz > 0.0 ? Direction.SOUTH : Direction.NORTH));
            return nearest == this.playerFacing && (!this.playerFacing.getAxis().isHorizontal() && this.yaw == null || this.acceptsRay(dx, dz));
         } else {
            return this.acceptsRay(dx, dz);
         }
      }

      public Float clickYaw() {
         return this.yaw != null ? this.yaw : (this.playerFacing != null && this.playerFacing.getAxis().isHorizontal() ? this.playerFacing.toYRot() : null);
      }

      public boolean acceptsHalf(BlockState state) {
         if (this.highHalf == null) {
            return true;
         } else if (state.hasProperty(SlabBlock.TYPE)) {
            return this.highHalf == (state.getValue(SlabBlock.TYPE) == SlabType.TOP);
         } else {
            return state.hasProperty(BlockStateProperties.HALF) ? this.highHalf == (state.getValue(BlockStateProperties.HALF) == Half.TOP) : true;
         }
      }
   }

   record ClickGeometry(BlockPos support, Vec3 point, Direction face, double floorOffset, double eyeHeight) {
      ClickGeometry {
         support = support.immutable();
      }
   }
}
