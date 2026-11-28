package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiScaffold;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class AiSchematicActions {
   private AiSchematicActions() {
   }

   static boolean hasApproach(BlockPos worker, AABB site, int budget, Predicate<BlockPos> open) {
      return !init_Set_BlockPos(worker, site, budget, open).isEmpty();
   }

   static Set<Long> init_Set_BlockPos(BlockPos worker, AABB site, int budget, Predicate<BlockPos> open) {
      HashSet<Long> seen = new HashSet<>();
      ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      queue.add(worker);
      seen.add(worker.asLong());

      while (!queue.isEmpty()) {
         if (seen.size() > budget) {
            return Set.of();
         }

         BlockPos cell = queue.poll();
         if (!site.contains(Vec3.atCenterOf(cell))) {
            return Set.of();
         }

         for (Direction direction : Direction.values()) {
            BlockPos next = cell.relative(direction);
            if (seen.add(next.asLong()) && open.test(next)) {
               queue.add(next);
            }
         }
      }

      return seen;
   }

   static BlockPos init(LevelReader world, BlockPos ceiling, Map<Long, AiBuilder.CellSpec> source) {
      BlockPos below = ceiling.below();
      AiBuilder.CellSpec cell = source.get(below.asLong());
      if (cell != null && AiBuilder.toBlockState(cell).getBlock() instanceof BedBlock && !AiBuilder.matchesIgnoringUnpairedChest(cell, world.getBlockState(below))) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            BlockPos foot = isBedHead(cell) ? below.relative(facing.getOpposite()) : below;
            BlockPos head = foot.relative(facing);
            return !world.getBlockState(foot.below()).getCollisionShape(world, foot.below()).isEmpty()
                  && !world.getBlockState(head.below()).getCollisionShape(world, head.below()).isEmpty()
               ? foot
               : null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static LevelReader withReplacedBlock(LevelReader world, BlockPos cell, BlockState replacement) {
      return (LevelReader)Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, args) -> {
         if (args != null && args.length > 0 && cell.equals(args[0])) {
            if (method.getName().equals("getBlockState")) {
               return replacement;
            }

            if (method.getName().equals("getFluidState")) {
               return replacement.getFluidState();
            }

            if (method.getName().equals("isEmptyBlock")) {
               return replacement.isAir();
            }
         }

         try {
            return method.invoke(world, args);
         } catch (InvocationTargetException e) {
            throw e.getCause();
         }
      });
   }

   static BlockPos foundationFor(AiBuilder.CellSpec cell, BlockPos target, LevelReader world, Set<Long> sourceCells) {
      BlockState requested = AiBuilder.toBlockState(cell);
      if (requested.canSurvive(world, target)) {
         return null;
      } else {
         BlockPos below = target.below();
         BlockState live = world.getBlockState(below);
         if (!sourceCells.contains(below.asLong()) && live.canBeReplaced() && live.getFluidState().isEmpty()) {
            return requested.canSurvive(withReplacedBlock(world, below, Blocks.DIRT.defaultBlockState()), target) ? below : null;
         } else {
            return null;
         }
      }
   }

   static BlockPos dependencyFor(
      AiBuilder.CellSpec cell, BlockPos target, LevelReader world, Map<Long, AiBuilder.CellSpec> source
   ) {
      BlockState requested = AiBuilder.toBlockState(cell);
      if (requested.canSurvive(world, target)) {
         for (Direction direction : Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            AiBuilder.CellSpec wanted = source.get(neighbour.asLong());
            if (wanted != null
               && !AiBuilder.matchesIgnoringUnpairedChest(wanted, world.getBlockState(neighbour))
               && !requested.canSurvive(withReplacedBlock(world, neighbour, Blocks.AIR.defaultBlockState()), target)) {
               return neighbour;
            }
         }

         return null;
      } else {
         for (Direction direction : Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            AiBuilder.CellSpec wanted = source.get(neighbour.asLong());
            if (wanted != null && requested.canSurvive(withReplacedBlock(world, neighbour, AiBuilder.toBlockState(wanted)), target)) {
               return neighbour;
            }
         }

         return null;
      }
   }

   static boolean init(LevelReader world, BlockPos bottom, int topY) {
      for (int y = bottom.getY() - 1; y <= topY; y++) {
         BlockPos pos = new BlockPos(bottom.getX(), y, bottom.getZ());
         BlockState state = world.getBlockState(pos);
         if (!state.getFluidState().isEmpty()
            || autismclient.ai.engine.AiPathfinder.LiveGridView.isFeetHazardCompat(state)
            || autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state)
            || y >= bottom.getY() && (state.hasBlockEntity() || state.getDestroySpeed(world, pos) < 0.0F)) {
            return false;
         }
      }

      return true;
   }

   static boolean init(BlockPos candidate, Map<Long, AiBuilder.CellSpec> source, LevelReader world) {
      return init_Z_BlockPos(candidate, source, world, null);
   }

   static boolean init_Z_BlockPos(BlockPos candidate, Map<Long, AiBuilder.CellSpec> source, LevelReader world, BlockPos ignore) {
      LevelReader removed = null;

      for (Direction direction : Direction.values()) {
         BlockPos dependent = candidate.relative(direction);
         if (!dependent.equals(ignore)) {
            AiBuilder.CellSpec cell = source.get(dependent.asLong());
            if (cell != null) {
               BlockState requested = AiBuilder.toBlockState(cell);
               if (direction == Direction.UP && requested.getBlock() instanceof FallingBlock) {
                  return true;
               }

               if (requested.canSurvive(world, dependent)) {
                  if (removed == null) {
                     removed = withReplacedBlock(world, candidate, Blocks.AIR.defaultBlockState());
                  }

                  if (!requested.canSurvive(removed, dependent)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   static AiOperation descendAccessShaft(final BlockPos top, final BlockPos bottom, final Set<Long> protectedCells) {
      return init_AiOperation_BlockPos_2(top, bottom, protectedCells, protectedCells);
   }

   static AiOperation init_AiOperation_BlockPos_2(
      final BlockPos top, final BlockPos bottom, final Set<Long> protectedCells, final Set<Long> digProtected
   ) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         AiOperation child = AiGoto.travelToCompat(top, 0.25, false, false, false, 20);
         final long deadline = System.nanoTime() + 40000000000L;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadline) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Shaft descent deadline");
               }

               if (this.child != null) {
                  AiOperation.Result r = this.child.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.child.cancel();
                  this.child = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(r.status(), r.message());
                  }
               }

               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
               if (!autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Landing inside the access shaft", "{}", List.of());
               }

               if (mc.player.getY() <= bottom.getY() + 0.25 && mc.player.blockPosition().equals(bottom)) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Buried construction stance reached");
               }

               Vec3 centre = Vec3.atBottomCenterOf(bottom);
               Vec3 vel = mc.player.getDeltaMovement();
               double dx = centre.x - mc.player.getX();
               double dz = centre.z - mc.player.getZ();
               if (!(dx * dx + dz * dz > 0.0012250000000000002) && !(vel.horizontalDistanceSqr() > 4.0E-4)) {
                  mc.options.keyShift.setDown(false);
                  BlockPos floor = BlockPos.containing(mc.player.getX(), mc.player.getY() - 0.001, mc.player.getZ());
                  if (floor.getX() != bottom.getX()
                     || floor.getZ() != bottom.getZ()
                     || floor.getY() < bottom.getY()
                     || floor.getY() >= top.getY()
                     || protectedCells.contains(floor.asLong())) {
                     return this.finish(AiOperation.Status.FAILED, "The shaft descent would leave its temporary access column");
                  } else if (AiSchematicActions.init(mc.level, bottom, floor.getY()) && autismclient.ai.engine.AiMotionSafety.isSafeToBreakSupport(floor)) {
                     this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(floor, 10, true, digProtected, false);
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Opening the next floor of the access shaft", "{}", List.of());
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "The access shaft has no safe landing after its floor is removed");
                  }
               } else {
                  mc.player.setSprinting(false);
                  mc.options.keyShift.setDown(true);
                  if (vel.horizontalDistanceSqr() <= 4.0E-4) {
                     double yawRad = Math.toRadians(mc.player.getYRot());
                     double fwd = -Math.sin(yawRad) * dx + Math.cos(yawRad) * dz;
                     double strafe = Math.cos(yawRad) * dx + Math.sin(yawRad) * dz;
                     mc.options.keyUp.setDown(fwd > 0.024);
                     mc.options.keyDown.setDown(fwd < -0.024);
                     mc.options.keyLeft.setDown(strafe > 0.024);
                     mc.options.keyRight.setDown(strafe < -0.024);
                  }

                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Centering on the access shaft before removing its floor", "{}", List.of());
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Shaft descent cancelled");
            }
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.cancel();
            return new AiOperation.Result(status, message, "{}", List.of());
         }

         @Override
         public boolean canInterruptNow() {
            return this.child == null ? autismclient.ai.engine.AiMotionSafety.isPlayerSupported() : this.child.canInterruptNow();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.child != null) {
               this.child.cancel();
            }

            autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
         }
      };
   }

   static AiOperation init(final AiBuilder.CellSpec cell, final BlockPos target, final autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player.onGround()
         && !(Math.abs(mc.player.getY() - target.getY() - 1.0) > 1.0E-5)
         && Block.isShapeFullBlock(AiBuilder.toBlockState(cell).getCollisionShape(mc.level, target))) {
         BlockPos floor = new BlockPos(mc.player.blockPosition().getX(), target.getY(), mc.player.blockPosition().getZ());
         int dx = target.getX() - floor.getX();
         int dz = target.getZ() - floor.getZ();
         if (Math.abs(dx) + Math.abs(dz) == 1 && Block.isShapeFullBlock(mc.level.getBlockState(floor).getCollisionShape(mc.level, floor))) {
            Direction face = Direction.getNearest(dx, 0, dz, Direction.NORTH);
            if (constraint.acceptsFace(face) && constraint.acceptsRay(-dx * 0.2, -1.5, -dz * 0.2)) {
               final Vec3 edge = Vec3.atBottomCenterOf(floor.above()).add(dx * 0.7, 0.0, dz * 0.7);
               if (!autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(mc.player.position(), edge)) {
                  return null;
               }

               Vec3 displacement = edge.subtract(mc.player.position());

               for (int i = 1; i <= 8; i++) {
                  if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(displacement.scale(i / 8.0)).deflate(0.001))) {
                     return null;
                  }
               }

               return new AiOperation() {
                  final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
                  int ticks;

                  @Override
                  public AiOperation.Result tick() {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     mc.player.setSprinting(false);
                     mc.options.keyShift.setDown(true);
                     if (mc.player.onGround() && ++this.ticks <= 60) {
                        if (autismclient.ai.engine.PlacementGoal.init(mc, target, AiBuilder.toBlockState(cell).getBlock().asItem(), constraint, 72) != null) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Support side exposed");
                        }

                        Vec3 remaining = edge.subtract(mc.player.position());
                        if (!autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(mc.player.position(), edge)) {
                           return this.finish(AiOperation.Status.FAILED, "Support-edge alignment would touch lava");
                        }

                        if (remaining.lengthSqr() < 0.0025) {
                           return this.finish(AiOperation.Status.FAILED, "Support side is occluded");
                        }

                        for (int i = 1; i <= 4; i++) {
                           if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(remaining.scale(i / 4.0)).deflate(0.001))) {
                              return this.finish(AiOperation.Status.FAILED, "Support-edge alignment is obstructed");
                           }
                        }

                        this.aim.aimAt(edge.add(0.0, mc.player.getEyeHeight(), 0.0));
                        this.aim.tick();
                        mc.options.keyUp.setDown(this.aim.isSettled());
                        return new AiOperation.Result(AiOperation.Status.RUNNING, "Exposing the support side", "{}", List.of());
                     } else {
                        return this.finish(AiOperation.Status.FAILED, "Support-edge alignment failed");
                     }
                  }

                  AiOperation.Result finish(AiOperation.Status status, String message) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     return new AiOperation.Result(status, message, "{}", List.of());
                  }

                  @Override
                  public void cancel() {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  }
               };
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static List<BlockPos> init(AiBuilder.Blueprint blueprint) {
      return init(blueprint, blueprint.cells().stream().mapToInt(AiBuilder.CellSpec::dy).max().orElse(0));
   }

   static List<BlockPos> init(AiBuilder.Blueprint blueprint, int highestWorkDy) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return List.of();
      }

      int bottom = cells.stream().mapToInt(AiBuilder.CellSpec::dy).min().orElse(0);
      int height = Math.min(highestWorkDy, cells.stream().mapToInt(AiBuilder.CellSpec::dy).max().orElse(0)) - bottom + 1;
      if (height < 6) {
         return List.of();
      }

      int left = cells.stream().mapToInt(AiBuilder.CellSpec::dx).min().orElse(0) - 3;
      int right = cells.stream().mapToInt(AiBuilder.CellSpec::dx).max().orElse(0) + 3;
      int front = cells.stream().mapToInt(AiBuilder.CellSpec::dz).min().orElse(0) - 3;
      int back = cells.stream().mapToInt(AiBuilder.CellSpec::dz).max().orElse(0) + 3;
      ArrayList<BlockPos> result = new ArrayList<>();
      int x = left;
      int z = front;
      int direction = 0;
      result.add(blueprint.origin().offset(x, bottom - 1, z));

      for (int level = 1; level <= height; level++) {
         int var10000;
         switch (direction) {
            case 0:
               x++;
               var10000 = x == right ? 1 : 0;
               break;
            case 1:
               z++;
               var10000 = z == back ? 2 : 1;
               break;
            case 2:
               x--;
               var10000 = x == left ? 3 : 2;
               break;
            default:
               z--;
               var10000 = z == front ? 0 : direction;
         }

         direction = var10000;
         result.add(blueprint.origin().offset(x, bottom + level - 1, z));
      }

      return List.copyOf(result);
   }

   static AiOperation buildAccessStairs(AiBuilder.Blueprint blueprint, Set<Long> protectedCells, Set<Long> owned) {
      return init_AiOperation_TableIndex_DataUti(
         blueprint, protectedCells, owned, blueprint.cells().stream().mapToInt(AiBuilder.CellSpec::dy).max().orElse(0)
      );
   }

   static AiOperation init_AiOperation_TableIndex_DataUti(
      final AiBuilder.Blueprint blueprint, final Set<Long> protectedCells, final Set<Long> owned, int highestWorkDy
   ) {
      final Minecraft mc = Minecraft.getInstance();
      final List<BlockPos> steps = init(blueprint, highestWorkDy);
      return !steps.isEmpty()
            && steps.stream()
               .skip(1L)
               .allMatch(
                  pos -> Block.isShapeFullBlock(mc.level.getBlockState(pos).getCollisionShape(mc.level, pos))
                     && mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(pos.above()).subtract(mc.player.position())))
               )
         ? AiGameOperations.immediate(AiOperation.Status.SUCCEEDED, "Existing access stairs verified", new JsonObject())
         : new AiOperation() {
            AiOperation child;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            int stepIndex = 1;
            int phase;
            int alignTicks;
            boolean cancelled;
            boolean placingLanding;
            String failure;
            final long deadline = System.nanoTime() + 600000000000L;

            @Override
            public AiOperation.Result tick() {
               if (this.failure != null) {
                  return this.finish(AiOperation.Status.FAILED, this.failure);
               }

               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Access construction deadline");
                  }

                  if (!steps.isEmpty() && this.stepIndex < steps.size()) {
                     if (this.child != null) {
                        AiOperation.Result r = this.child.tick();
                        if (!r.terminal()) {
                           return this.running(r.message());
                        }

                        this.child.cancel();
                        this.child = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED && !this.placingLanding) {
                           return this.finish(r.status(), r.message());
                        }

                        this.placingLanding = false;
                        this.phase++;
                        this.alignTicks = 0;
                     }

                     BlockPos previous = steps.get(this.stepIndex - 1);
                     BlockPos top = steps.get(this.stepIndex);
                     BlockPos under = top.below();
                     HashSet<Long> keep = new HashSet<>(protectedCells);
                     keep.addAll(owned);
                     if (this.phase == 0) {
                        this.child = this.walkTo(previous.above(), keep);
                        return this.running("Approaching access step " + this.stepIndex);
                     }

                     if (this.phase == 1) {
                        if (this.init(top)) {
                           this.phase = 5;
                           this.alignTicks = 0;
                        } else if (this.init(under)) {
                           this.phase = 3;
                           this.alignTicks = 0;
                        } else {
                           Direction direction = Direction.getNearest(top.getX() - previous.getX(), 0, top.getZ() - previous.getZ(), Direction.NORTH);
                           Vec3 edge = Vec3.atBottomCenterOf(previous.above()).add(direction.getStepX() * 0.7, 0.0, direction.getStepZ() * 0.7);
                           Vec3 centre = Vec3.atBottomCenterOf(previous.above());
                           Vec3 delta = mc.player.position().subtract(centre);
                           double along = delta.x * direction.getStepX() + delta.z * direction.getStepZ();
                           double across = delta.x * direction.getStepZ() - delta.z * direction.getStepX();
                           if ((along < 0.67 || Math.abs(across) > 0.1) && !this.alignTo(edge, true)) {
                              return this.running("Exposing the next step's support face");
                           }

                           autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                           mc.options.keyShift.setDown(true);
                           this.phase = 2;
                           this.alignTicks = 0;
                        }
                     }

                     if (this.phase == 2) {
                        mc.options.keyShift.setDown(true);
                        if (!this.init(under)) {
                           this.child = this.placeSupport(under, keep);
                           return this.running("Placing the access step's lower support");
                        }

                        this.phase = 3;
                     }

                     if (this.phase == 3) {
                        if (!this.alignTo(Vec3.atBottomCenterOf(previous.above()), false)) {
                           return this.running("Returning onto the access tread");
                        }

                        this.phase = 4;
                        this.alignTicks = 0;
                     }

                     if (this.phase == 4) {
                        if (!this.init(top)) {
                           this.child = this.placeSupport(top, keep);
                           return this.running("Placing the access tread");
                        }

                        this.phase = 5;
                     }

                     if (this.phase == 5) {
                        this.child = this.walkTo(top.above(), keep);
                        return this.running("Climbing access step " + this.stepIndex);
                     }

                     if (this.phase == 6) {
                        BlockPos landing = this.turnLandingAt(this.stepIndex);
                        if (landing != null && !this.init(landing) && mc.level.getBlockState(landing).canBeReplaced()) {
                           Direction direction = Direction.getNearest(landing.getX() - top.getX(), 0, landing.getZ() - top.getZ(), Direction.NORTH);
                           Vec3 centre = Vec3.atBottomCenterOf(top.above());
                           Vec3 edge = centre.add(direction.getStepX() * 0.7, 0.0, direction.getStepZ() * 0.7);
                           Vec3 delta = mc.player.position().subtract(centre);
                           double along = delta.x * direction.getStepX() + delta.z * direction.getStepZ();
                           double across = delta.x * direction.getStepZ() - delta.z * direction.getStepX();
                           if ((along < 0.67 || Math.abs(across) > 0.1) && !this.alignTo(edge, true)) {
                              return this.running("Leaning out for the stair landing");
                           }

                           autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                           mc.options.keyShift.setDown(true);
                           this.placingLanding = true;
                           this.child = this.placeSupport(landing, keep);
                           return this.running("Placing the access stair landing");
                        }

                        this.phase = 7;
                     }

                     this.stepIndex++;
                     this.phase = 0;
                     return this.running("Access step verified");
                  } else {
                     return this.finish(AiOperation.Status.SUCCEEDED, "Access stairs verified");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Access construction cancelled");
               }
            }

            boolean init(BlockPos pos) {
               return Block.isShapeFullBlock(mc.level.getBlockState(pos).getCollisionShape(mc.level, pos));
            }

            BlockPos turnLandingAt(int at) {
               if (at >= 1 && at + 1 < steps.size()) {
                  BlockPos before = steps.get(at - 1);
                  BlockPos tread = steps.get(at);
                  BlockPos after = steps.get(at + 1);
                  int inX = tread.getX() - before.getX();
                  int inZ = tread.getZ() - before.getZ();
                  int outX = after.getX() - tread.getX();
                  int outZ = after.getZ() - tread.getZ();
                  return inX == outX && inZ == outZ ? null : tread.offset(-outX, 0, -outZ);
               } else {
                  return null;
               }
            }

            AiOperation walkTo(BlockPos feet, Set<Long> keep) {
               return AiGoto.travelToCompat(
                  feet,
                  0.25,
                  true,
                  false,
                  false,
                  30,
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                  true,
                  2.0,
                  AiGoto.protectingCellsBelowYCompat(
                     autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(feet, 0.25), keep, blueprint.origin().getY()
                  )
               );
            }

            AiOperation placeSupport(final BlockPos pos, Set<Long> keep) {
               BlockState state = mc.level.getBlockState(pos);
               if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                  final AiOperation build = AiBuilder.buildSchematicSneaking(
                     new AiBuilder.Blueprint(pos, List.of(new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"))), keep
                  );
                  return new AiOperation() {
                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result r = build.tick();
                        if (r.status() == AiOperation.Status.SUCCEEDED && init(pos)) {
                           AiScaffold.init(pos, mc.level.getBlockState(pos).getBlock());
                           owned.add(pos.asLong());
                        }

                        return r;
                     }

                     @Override
                     public void cancel() {
                        build.cancel();
                     }
                  };
               } else {
                  return AiGameOperations.immediate(AiOperation.Status.FAILED, "Access route is occupied at " + pos.toShortString(), new JsonObject());
               }
            }

            boolean alignTo(Vec3 point, boolean sneak) {
               if (!autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(mc.player.position(), point)) {
                  this.failure = "Access alignment would touch lava";
                  return false;
               } else {
                  double dx = point.x - mc.player.getX();
                  double dz = point.z - mc.player.getZ();
                  if (dx * dx + dz * dz < (sneak ? 1.0E-4 : 0.01)) {
                     autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                     return true;
                  } else if (++this.alignTicks > 90) {
                     this.failure = "Access alignment could not reach " + point + " from " + mc.player.position();
                     return false;
                  } else {
                     mc.player.setSprinting(false);
                     mc.options.keySprint.setDown(false);
                     mc.options.keyShift.setDown(sneak);
                     mc.options.keyJump.setDown(false);
                     mc.options.keyDown.setDown(false);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     this.aim.aimAt(point.add(0.0, mc.player.getEyeHeight(), 0.0));
                     this.aim.tick();
                     mc.options.keyUp.setDown(this.aim.isSettled());
                     return false;
                  }
               }
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Step " + this.stepIndex + "/" + (steps.size() - 1) + ": " + message, new JsonObject());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.cancel();
               return AiGameOperations.result(status, message, new JsonObject());
            }

            @Override
            public void cancel() {
               this.cancelled = true;
               if (this.child != null) {
                  this.child.cancel();
               }

               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
            }
         };
   }

   static String init(BlockPos target, BlockGetter world, Map<Long, AiBuilder.CellSpec> wanted) {
      HashSet<Long> seen = new HashSet<>();
      ArrayDeque<long[]> queue = new ArrayDeque<>();
      seen.add(target.asLong());
      queue.add(new long[]{target.asLong(), 0L});
      Direction[] directions = new Direction[]{Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

      while (!queue.isEmpty() && seen.size() < 4096) {
         long[] node = queue.poll();
         BlockPos at = BlockPos.of(node[0]);

         for (Direction direction : directions) {
            boolean down = direction == Direction.DOWN;
            if (down || node[1] < 7L) {
               BlockPos next = at.relative(direction);
               if (seen.add(next.asLong())) {
                  AiBuilder.CellSpec cell = wanted.get(next.asLong());
                  if (cell != null) {
                     BlockState live = world.getBlockState(next);
                     if (!read(live)) {
                        if (AiBuilder.fluidOfCell(cell) == null && !"true".equals(cell.properties().get("waterlogged"))) {
                           return "waiting for " + next.toShortString() + " to hold the fluid in";
                        }

                        queue.add(new long[]{next.asLong(), down ? 0L : node[1] + 1L});
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   static boolean init(BlockState s) {
      if (!s.isAir() && s.getFluidState().isEmpty() && !s.hasBlockEntity()) {
         return !s.canBeReplaced() && !s.is(BlockTags.FLOWERS) && !s.is(BlockItemTags.SAPLINGS.block())
            ? s.is(BlockTags.DIRT)
               || s.is(BlockTags.SAND)
               || s.is(BlockTags.BASE_STONE_OVERWORLD)
               || s.is(BlockTags.LEAVES)
               || s.is(BlockTags.LOGS)
               || s.is(BlockTags.TERRACOTTA)
               || s.is(BlockItemTags.COAL_ORES.block())
               || s.is(BlockTags.IRON_ORES)
               || s.is(BlockTags.COPPER_ORES)
               || s.is(BlockTags.GOLD_ORES)
               || s.is(BlockItemTags.REDSTONE_ORES.block())
               || s.is(BlockItemTags.LAPIS_ORES.block())
               || s.is(BlockItemTags.DIAMOND_ORES.block())
               || s.is(BlockItemTags.EMERALD_ORES.block())
               || s.is(Blocks.GRAVEL)
               || s.is(Blocks.CLAY)
               || s.is(Blocks.SNOW)
               || s.is(Blocks.SNOW_BLOCK)
               || s.is(Blocks.POWDER_SNOW)
               || s.is(Blocks.ICE)
               || s.is(Blocks.PACKED_ICE)
               || s.is(Blocks.BLUE_ICE)
               || s.is(Blocks.SANDSTONE)
               || s.is(Blocks.RED_SANDSTONE)
               || s.is(Blocks.MUD)
               || s.is(Blocks.CALCITE)
               || s.is(Blocks.DRIPSTONE_BLOCK)
               || s.is(Blocks.POINTED_DRIPSTONE)
               || s.is(Blocks.MOSS_BLOCK)
               || s.is(Blocks.MOSS_CARPET)
               || s.is(Blocks.VINE)
               || s.is(Blocks.SUGAR_CANE)
               || s.is(Blocks.CACTUS)
               || s.is(Blocks.PUMPKIN)
               || s.is(Blocks.MELON)
               || s.is(Blocks.BAMBOO)
               || s.is(Blocks.SWEET_BERRY_BUSH)
               || s.is(Blocks.BROWN_MUSHROOM_BLOCK)
               || s.is(Blocks.RED_MUSHROOM_BLOCK)
               || s.is(Blocks.MUSHROOM_STEM)
               || s.is(Blocks.AMETHYST_BLOCK)
               || s.is(Blocks.SMOOTH_BASALT)
            : true;
      } else {
         return false;
      }
   }

   static boolean init(BlockGetter world, BlockPos p) {
      for (Direction d : Direction.values()) {
         FluidState fluid = world.getFluidState(p.relative(d));
         if (!fluid.isEmpty()) {
            if (!fluid.getType().isSame(Fluids.WATER)) {
               return false;
            }

            if (d != Direction.DOWN) {
               return false;
            }
         }
      }

      return true;
   }

   static List<BlockPos> cellsToClear(AiBuilder.Blueprint blueprint, Map<Long, AiBuilder.CellSpec> wanted, BlockGetter world) {
      return cellsToClear(blueprint, wanted, world, AiSchematicActions::init);
   }

   static List<BlockPos> load(AiBuilder.Blueprint blueprint, Map<Long, AiBuilder.CellSpec> wanted, BlockGetter world) {
      LinkedHashSet<BlockPos> out = new LinkedHashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         Block block = AiBuilder.toBlockState(cell).getBlock();
         if (block instanceof WallBlock || block instanceof FenceBlock || block instanceof IronBarsBlock) {
            BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());

            for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP)) {
               BlockPos next = at.relative(direction);
               if (!wanted.containsKey(next.asLong()) && load(world.getBlockState(next))) {
                  out.add(next);
               }
            }
         }
      }

      ArrayList<BlockPos> sorted = new ArrayList<>(out);
      sorted.sort(Comparator.<BlockPos>comparingInt(p -> p.getY()).reversed());
      return sorted;
   }

   static boolean load(BlockState s) {
      if (init(s)) {
         return true;
      } else {
         return !s.isAir() && s.getFluidState().isEmpty() && !s.hasBlockEntity()
            ? autismclient.ai.engine.AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(s.getBlock().asItem()).toString()) >= 0
            : false;
      }
   }

   static List<BlockPos> cellsToClear(
      AiBuilder.Blueprint blueprint, Map<Long, AiBuilder.CellSpec> wanted, BlockGetter world, Predicate<BlockState> clearable
   ) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return List.of();
      }

      int minX = 0;
      int minY = 0;
      int minZ = 0;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;

      for (AiBuilder.CellSpec c : cells) {
         minX = Math.min(minX, c.dx());
         minY = Math.min(minY, c.dy());
         minZ = Math.min(minZ, c.dz());
         maxX = Math.max(maxX, c.dx());
         maxY = Math.max(maxY, c.dy());
         maxZ = Math.max(maxZ, c.dz());
      }

      ArrayList<BlockPos> out = new ArrayList<>();
      new MutableBlockPos();

      for (int y = maxY; y >= minY; y--) {
         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               BlockPos at = blueprint.origin().offset(x, y, z);
               if (!wanted.containsKey(at.asLong())) {
                  BlockState state = world.getBlockState(at);
                  if (clearable.test(state)
                     && (
                        !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                           || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER
                           || !wanted.containsKey(at.below().asLong())
                     )) {
                     out.add(at);
                  }
               }
            }
         }
      }

      return out;
   }

   static boolean read(BlockState state) {
      if (state.getFluidState().isSource()) {
         return true;
      } else {
         return state.getBlock() instanceof LiquidBlockContainer
            ? true
            : state.isSolid() || state.getBlock() instanceof DoorBlock || state.is(BlockTags.SIGNS) || state.is(Blocks.LADDER) || state.is(Blocks.SUGAR_CANE);
      }
   }

   static boolean needsAction(AiBuilder.CellSpec cell) {
      String id = cell.block();
      return id.equals("minecraft:farmland")
         || id.equals("minecraft:dirt_path")
         || AiBuilder.fluidOfCell(cell) != null
         || id.startsWith("minecraft:potted_")
         || "upper".equals(cell.properties().get("half"))
         || isBedHead(cell)
         || write(cell)
         || isFilledComposter(cell);
   }

   static boolean isFilledComposter(AiBuilder.CellSpec cell) {
      return cell.block().equals("minecraft:composter") && !"0".equals(cell.properties().getOrDefault("level", "0"));
   }

   static boolean isBedHead(AiBuilder.CellSpec cell) {
      return "head".equals(cell.properties().get("part")) && cell.block().endsWith("_bed");
   }

   static boolean write(AiBuilder.CellSpec cell) {
      return cell.block().equals("minecraft:water_cauldron")
         || cell.block().equals("minecraft:lava_cauldron")
         || cell.block().equals("minecraft:powder_snow_cauldron");
   }

   static Item cauldronFillItem(AiBuilder.CellSpec cell) {
      Item var10000;
      switch (cell.block()) {
         case "minecraft:lava_cauldron":
            var10000 = Items.LAVA_BUCKET;
            break;
         case "minecraft:powder_snow_cauldron":
            var10000 = Items.POWDER_SNOW_BUCKET;
            break;
         default:
            var10000 = Items.WATER_BUCKET;
      }

      return var10000;
   }

   static boolean isPartialFill(AiBuilder.CellSpec cell, BlockState live) {
      if (write(cell) && live != null) {
         Block wantedBlock = BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()));
         if (live.getBlock() != wantedBlock) {
            return false;
         }

         boolean water = cell.block().equals("minecraft:water_cauldron");
         boolean powderSnow = cell.block().equals("minecraft:powder_snow_cauldron");
         if (!water && !powderSnow) {
            return false;
         }

         int wanted = Integer.parseInt(cell.properties().getOrDefault("level", "3"));
         return (!powderSnow || wanted == 3) && live.getValue(LayeredCauldronBlock.LEVEL) < wanted;
      } else {
         return false;
      }
   }

   static List<String> actionsFor(AiBuilder.CellSpec cell) {
      String id = cell.block();
      if (AiBuilder.fluidOfCell(cell) != null || id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path") || isBedHead(cell)) {
         return List.of();
      }

      if (isFilledComposter(cell)) {
         return List.of();
      }

      if (!write(cell)) {
         if (id.startsWith("minecraft:potted_")) {
            return init(AiBuilder.toBlockState(cell).getBlock()) != Items.AIR ? List.of() : List.of("no plant item for the pot");
         } else {
            return "upper".equals(cell.properties().get("half")) ? List.of() : List.of("no survival construction strategy");
         }
      } else {
         return id.equals("minecraft:powder_snow_cauldron") && !"3".equals(cell.properties().getOrDefault("level", "3"))
            ? List.of("level=" + cell.properties().get("level"))
            : List.of();
      }
   }

   static Item init(Block block) {
      return block instanceof FlowerPotBlock pot ? pot.getPotted().asItem() : Items.AIR;
   }

   static String postInteractItemSpec(AiBuilder.CellSpec cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return null;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String name = entry.getKey();
         if (AiBuilder.orientationKindForProperty(live.getBlock(), name) == AiBuilder.OrientationKind.POST_INTERACT) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(name);
            if (property != null) {
               String actual = String.valueOf(live.getValue(property));
               String requested = entry.getValue();
               if (!AiBuilder.propertyValuesMatch(live.getBlock(), name, actual, requested)) {
                  Item needed = AiBuilder.postInteractToolItem(live.getBlock(), name);
                  if (needed != null) {
                     if (needed == Items.ENDER_EYE && !Boolean.parseBoolean(requested)) {
                        return null;
                     }

                     if (needed == Items.GLOWSTONE && Integer.parseInt(actual) >= Integer.parseInt(requested)) {
                        return null;
                     }

                     if (needed == Items.FLINT_AND_STEEL && !Boolean.parseBoolean(requested)) {
                        return null;
                     }

                     if (needed == Items.DIAMOND_SHOVEL && Boolean.parseBoolean(requested)) {
                        return null;
                     }

                     return needed == Items.DIAMOND_SHOVEL ? "shovel" : BuiltInRegistries.ITEM.getKey(needed).toString();
                  }

                  if (AiBuilder.isStackableCountProperty(live.getBlock(), name) && Integer.parseInt(actual) < Integer.parseInt(requested)) {
                     return BuiltInRegistries.ITEM.getKey(live.getBlock().asItem()).toString();
                  }

                  return null;
               }
            }
         }
      }

      return null;
   }

   static AiOperation init(final AiBuilder.CellSpec cell, final BlockPos target, final Set<Long> protectedCells) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         AiOperation child;
         int attempts;

         @Override
         public AiOperation.Result tick() {
            if (mc.player != null && mc.level != null) {
               mc.options.keyShift.setDown(false);
               if (this.child != null) {
                  AiOperation.Result r = this.child.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.child.cancel();
                  this.child = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return r;
                  }
               }

               BlockState live = mc.level.getBlockState(target);
               if (AiBuilder.matchesIgnoringUnpairedChest(cell, live)) {
                  return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Interaction state verified", new JsonObject());
               }

               if (++this.attempts > (cell.properties().containsKey("note") ? 30 : (cell.properties().containsKey("delay") ? 6 : 2))) {
                  return AiGameOperations.result(AiOperation.Status.FAILED, "Interaction state did not settle", new JsonObject());
               }

               if (AiBuilder.needsWaterlogToggleOnly(cell, live)) {
                  BlockHitResult hit = autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, target);
                  if (hit != null && !(mc.player.getEyePosition().distanceToSqr(hit.getLocation()) > 16.0)) {
                     this.child = AiBucket.init(
                        AiJson.objectOf(
                           "action",
                           "true".equals(cell.properties().get("waterlogged")) ? "empty" : "fill",
                           "fluid",
                           "water",
                           "x",
                           target.getX(),
                           "y",
                           target.getY(),
                           "z",
                           target.getZ(),
                           "timeoutSeconds",
                           10
                        )
                     );
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Restoring waterlogging with a bucket", new JsonObject());
                  } else {
                     this.child = AiGoto.approachBlockCompat(target, 30, protectedCells);
                     return AiGameOperations.result(AiOperation.Status.RUNNING, "Approaching the waterlogged block", new JsonObject());
                  }
               } else {
                  JsonObject args = AiJson.objectOf("x", target.getX(), "y", target.getY(), "z", target.getZ());
                  String itemSpec = AiSchematicActions.postInteractItemSpec(cell, live);
                  if (itemSpec != null) {
                     int slot = AiItems.equipHotbar(AiItems.resolveItem(itemSpec));
                     if (slot < 0) {
                        return AiGameOperations.result(AiOperation.Status.FAILED, "Missing " + itemSpec, new JsonObject());
                     }

                     args.addProperty("slot", slot);
                  }

                  this.child = AiGameOperations.blockAction("interact", args, protectedCells);
                  return AiGameOperations.result(AiOperation.Status.RUNNING, "Approaching the block's interaction face", new JsonObject());
               }
            } else {
               return AiGameOperations.result(AiOperation.Status.CANCELLED, "World unavailable", new JsonObject());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.child == null || this.child.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.child != null && this.child.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.child != null) {
               this.child.cancel();
            }
         }
      };
   }

   static List<AiCellMaterials.Inner_CachePool> materialPlans(AiBuilder.CellSpec cell) {
      return AiCellMaterials.init(cell);
   }

   static List<AiCellMaterials.Inner_CachePool> materialPlans(AiBuilder.CellSpec cell, BlockPos target) {
      Minecraft mc = Minecraft.getInstance();
      boolean waterAbove = mc.level.getFluidState(target.above()).is(FluidTags.WATER);
      return AiCellMaterials.init(cell, mc.level.getBlockState(target), waterAbove);
   }

   static List<Item> materials(AiBuilder.CellSpec cell) {
      return distinctItems(materialPlans(cell));
   }

   static List<Item> load(AiBuilder.CellSpec cell, BlockPos target) {
      return distinctItems(materialPlans(cell, target));
   }

   private static List<Item> distinctItems(List<AiCellMaterials.Inner_CachePool> requirements) {
      return requirements.stream().map(AiCellMaterials.Inner_CachePool::item).distinct().toList();
   }

   static AiOperation load(final AiBuilder.CellSpec cell, final BlockPos target, Set<Long> shared) {
      final Minecraft mc = Minecraft.getInstance();
      final HashSet<Long> protectedCells = new HashSet<>(shared);
      return new AiOperation() {
         AiOperation child;
         boolean cancelled;
         int attempts;
         int waterState;
         int soilApproaches;
         AiOperation.Result deferredResult;
         final long deadline = System.nanoTime() + 120000000000L;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadline && this.waterState != 3) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Special placement deadline");
               }

               if (this.child != null) {
                  AiOperation.Result r = this.child.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.child.cancel();
                  this.child = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     if (this.waterState == 3) {
                        this.waterState = 0;
                        return this.finish(r.status(), "Water restoration failed: " + r.message());
                     }

                     return this.finish(r.status(), r.message());
                  }

                  if (this.waterState == 1) {
                     this.waterState = 2;
                  } else if (this.waterState == 3) {
                     this.waterState = 0;
                  }

                  if (this.deferredResult != null && this.waterState == 0) {
                     return this.finish(this.deferredResult.status(), this.deferredResult.message());
                  }
               }

               BlockState live = mc.level.getBlockState(target);
               if (this.waterState == 2 && live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()))) {
                  this.waterState = 3;
                  this.child = this.waterBucketAbove("empty");
                  return this.running("Restoring water above the converted soil");
               }

               if (AiBuilder.matchesSchematicCell(cell, live)) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Special cell verified");
               }

               if (++this.attempts > 20) {
                  return this.finish(AiOperation.Status.FAILED, "Special cell state could not be achieved: " + live);
               }

               String id = cell.block();
               if (AiSchematicActions.isBedHead(cell)) {
                  Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
                  if (facing != null && facing.getAxis().isHorizontal()) {
                     BlockPos foot = target.relative(facing.getOpposite());
                     if (!live.canBeReplaced()) {
                        if ((!live.hasBlockEntity() || live.getBlock() instanceof BedBlock) && live.getFluidState().isEmpty()) {
                           this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                           return this.running("Clearing the bed's head cell");
                        } else {
                           return this.finish(AiOperation.Status.FAILED, "The bed's head cell is occupied by protected contents");
                        }
                     } else {
                        BlockState footState = mc.level.getBlockState(foot);
                        if (footState.canBeReplaced()) {
                           LinkedHashMap<String, String> props = new LinkedHashMap<>(cell.properties());
                           props.put("part", "foot");
                           protectedCells.add(foot.asLong());
                           this.child = AiBuilder.buildSchematicSubtask(
                              new AiBuilder.Blueprint(foot, List.of(new AiBuilder.CellSpec(0, 0, 0, id, props))), protectedCells
                           );
                           return this.running("Placing the bed's foot, which makes its head");
                        } else if ((!footState.hasBlockEntity() || footState.getBlock() instanceof BedBlock) && footState.getFluidState().isEmpty()) {
                           this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(foot, 30, true, protectedCells);
                           return this.running("Clearing the bed's foot cell");
                        } else {
                           return this.finish(AiOperation.Status.FAILED, "The bed's foot cell is occupied by protected contents");
                        }
                     }
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "A bed head needs a horizontal facing");
                  }
               } else if (AiSchematicActions.isFilledComposter(cell)) {
                  if (live.is(Blocks.COMPOSTER)) {
                     int level = live.getValue(ComposterBlock.LEVEL);
                     int wanted = Integer.parseInt(cell.properties().get("level"));
                     if (level > wanted) {
                        this.child = level == 8
                           ? this.useItemOn(target, "minecraft:dirt", false)
                           : autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                        return this.running("Emptying the overfilled composter");
                     } else if (level == 7 && wanted == 8) {
                        this.attempts--;
                        return this.running("Waiting for the full composter to turn ready");
                     } else {
                        this.child = this.useItemOn(target, "minecraft:pumpkin_pie", false);
                        return this.running("Composting a pumpkin pie");
                     }
                  } else if (live.canBeReplaced()) {
                     this.child = this.placeBlock(target, "minecraft:composter");
                     return this.running("Placing the composter");
                  } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                     this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                     return this.running("Clearing the composter cell");
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "The composter cell is occupied by protected contents");
                  }
               } else if (!AiSchematicActions.write(cell)) {
                  if ("upper".equals(cell.properties().get("half"))) {
                     if (live.canBeReplaced() || live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                        BlockPos root = target.below();
                        BlockState lower = mc.level.getBlockState(root);
                        if (lower.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                           if (lower.getBlock() instanceof DoorBlock
                              && live.getBlock() == lower.getBlock()
                              && !String.valueOf(lower.getValue(DoorBlock.OPEN)).equals(cell.properties().getOrDefault("open", "false"))) {
                              mc.options.keyShift.setDown(false);
                              this.child = AiGameOperations.blockAction(
                                 "interact", AiJson.objectOf("x", root.getX(), "y", root.getY(), "z", root.getZ()), protectedCells
                              );
                              return this.running("Restoring the paired door's open state");
                           } else {
                              this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(root, 30, true, protectedCells);
                              return this.running("Recovering the conflicting paired block");
                           }
                        } else if (!lower.canBeReplaced()) {
                           this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(root, 30, true, protectedCells);
                           return this.running("Clearing the missing lower half");
                        } else if (mc.level.getBlockState(root.below()).isAir()) {
                           this.child = this.placeBlock(root.below(), "minecraft:dirt");
                           return this.running("Supporting the lower half");
                        } else {
                           LinkedHashMap<String, String> props = new LinkedHashMap<>(cell.properties());
                           props.put("half", "lower");
                           protectedCells.add(root.asLong());
                           this.child = AiBuilder.buildSchematicSubtask(
                              new AiBuilder.Blueprint(root, List.of(new AiBuilder.CellSpec(0, 0, 0, id, props))), protectedCells
                           );
                           return this.running("Placing the lower half to create the requested upper half");
                        }
                     } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                        this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                        return this.running("Clearing the occupied upper half");
                     } else {
                        return this.finish(AiOperation.Status.FAILED, "The upper half is occupied by protected contents");
                     }
                  } else if (AiBuilder.fluidOfCell(cell) != null) {
                     String fluid = id.equals("minecraft:lava") ? "lava" : "water";
                     if (!"0".equals(cell.properties().getOrDefault("level", "0"))) {
                        return this.finish(AiOperation.Status.FAILED, "Flowing " + fluid + " must be produced by surrounding source cells");
                     }

                     if (live.isAir() || live.canBeReplaced() || !live.getFluidState().isEmpty()) {
                        if (AiBucket.init(mc, target, false, fluid.equals("water")) == null) {
                           this.child = AiGoto.approachBlockToInteractCompat(target.below(), 30, protectedCells, true, Direction.UP, 1.0);
                           return this.running("Approaching a visible receiving face for the water cell");
                        } else {
                           this.child = AiBucket.init(
                              AiJson.objectOf("action", "empty", "fluid", fluid, "x", target.getX(), "y", target.getY(), "z", target.getZ())
                           );
                           return this.running("Emptying a " + fluid + " bucket");
                        }
                     } else {
                        if (live.hasBlockEntity()) {
                           return this.finish(AiOperation.Status.FAILED, "The water cell is occupied by protected contents");
                        }

                        this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, false, protectedCells);
                        return this.running("Clearing the block where the water goes");
                     }
                  } else if (id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path")) {
                     if (live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Soil converted; moisture settles from surrounding water");
                     }

                     if (!live.isAir() && !live.canBeReplaced()) {
                        if (live.is(Blocks.DIRT)
                           || live.is(Blocks.GRASS_BLOCK)
                           || live.is(Blocks.DIRT_PATH)
                           || live.is(Blocks.COARSE_DIRT)
                           || live.is(Blocks.ROOTED_DIRT)
                           || id.equals("minecraft:dirt_path") && (live.is(Blocks.MYCELIUM) || live.is(Blocks.PODZOL))) {
                           BlockPos above = target.above();
                           BlockState cover = mc.level.getBlockState(above);
                           if (cover.isAir()) {
                              BlockHitResult top = autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, target, Direction.UP);
                              if ((top == null || mc.player.getEyePosition().distanceTo(top.getLocation()) > mc.player.blockInteractionRange() - 0.3)
                                 && this.soilApproaches++ < 3) {
                                 this.child = AiGoto.approachBlockToInteractCompat(target, 30, protectedCells, true, Direction.UP, 1.25);
                                 return this.running("Approaching the soil's top face");
                              } else {
                                 this.child = this.useItemOn(target, id.equals("minecraft:farmland") ? "hoe" : "shovel", true);
                                 return this.running("Converting the soil");
                              }
                           } else if (cover.getFluidState().is(FluidTags.WATER) && cover.getFluidState().isSource() && this.waterState == 0) {
                              Vec3 eye = mc.player.getEyePosition();
                              Vec3 centre = Vec3.atCenterOf(above);
                              BlockHitResult hit = mc.level
                                 .clip(new ClipContext(eye, centre, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, mc.player));
                              if (!(eye.distanceToSqr(centre) > 16.0) && hit.getBlockPos().equals(above)) {
                                 this.waterState = 1;
                                 this.child = this.waterBucketAbove("fill");
                                 return this.running("Lifting the water to expose the soil");
                              } else {
                                 this.child = AiGoto.travelToCompat(
                                    above,
                                    0.45,
                                    false,
                                    false,
                                    false,
                                    15,
                                    autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                                    true,
                                    2.0,
                                    AiGoto.protectingCellsBelowYCompat(
                                       autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(above, 0.45),
                                       protectedCells,
                                       target.getY()
                                    )
                                 );
                                 return this.running("Approaching the water above the soil");
                              }
                           } else if (!cover.hasBlockEntity() && cover.getFluidState().isEmpty() && !protectedCells.contains(above.asLong())) {
                              this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(above, 10, true, protectedCells, false);
                              return this.running("Clearing the temporary obstruction above the soil");
                           } else {
                              return this.finish(
                                 AiOperation.Status.FAILED,
                                 "The soil's top face is occupied by protected contents ("
                                    + BuiltInRegistries.BLOCK.getKey(cover.getBlock())
                                    + (cover.getFluidState().isEmpty() ? "" : ", fluid")
                                    + ")"
                              );
                           }
                        } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                           this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                           return this.running("Clearing the non-soil block before planting");
                        } else {
                           return this.finish(AiOperation.Status.FAILED, "The soil cell is occupied by protected contents");
                        }
                     } else {
                        this.child = this.placeBlock(target, "minecraft:dirt");
                        return this.running("Placing soil");
                     }
                  } else if (id.startsWith("minecraft:potted_")) {
                     Item plant = AiSchematicActions.init(AiBuilder.toBlockState(cell).getBlock());
                     if (plant == Items.AIR) {
                        return this.finish(AiOperation.Status.FAILED, "No plant item for " + id);
                     } else if (!live.is(Blocks.FLOWER_POT)) {
                        this.child = this.placeBlock(target, "minecraft:flower_pot");
                        return this.running("Placing a flower pot");
                     } else {
                        this.child = this.useItemOn(target, BuiltInRegistries.ITEM.getKey(plant).toString(), false);
                        return this.running("Planting the pot");
                     }
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "No survival construction strategy for " + id);
                  }
               } else if (live.getBlock() instanceof AbstractCauldronBlock) {
                  Block wanted = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
                  if (live.getBlock() != wanted && !live.is(Blocks.CAULDRON)) {
                     this.child = this.useItemOn(target, "minecraft:bucket", false);
                     return this.running("Emptying the cauldron's wrong contents");
                  }

                  if (live.is(Blocks.CAULDRON)) {
                     this.child = this.useItemOn(target, BuiltInRegistries.ITEM.getKey(AiSchematicActions.cauldronFillItem(cell)).toString(), false);
                     return this.running("Filling the cauldron");
                  }

                  if (AiSchematicActions.isPartialFill(cell, live)) {
                     this.child = this.useItemOn(target, BuiltInRegistries.ITEM.getKey(AiSchematicActions.cauldronFillItem(cell)).toString(), false);
                     return this.running("Refilling the cauldron");
                  }

                  if (live.is(Blocks.WATER_CAULDRON)) {
                     int current = live.getValue(LayeredCauldronBlock.LEVEL);
                     int wantedLevel = Integer.parseInt(cell.properties().getOrDefault("level", "3"));
                     if (current > wantedLevel) {
                        this.child = this.useItemOn(target, "minecraft:glass_bottle", false);
                        return this.running("Taking a bottle of water out of the cauldron");
                     }
                  }

                  return this.finish(AiOperation.Status.FAILED, "The cauldron's contents cannot be set to " + cell.properties());
               } else if (live.canBeReplaced()) {
                  this.child = this.placeBlock(target, "minecraft:cauldron");
                  return this.running("Placing the cauldron");
               } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                  this.child = autismclient.ai.engine.AiBreakBlock.breakBlock(target, 30, true, protectedCells);
                  return this.running("Clearing the cauldron cell");
               } else {
                  return this.finish(AiOperation.Status.FAILED, "The cauldron cell is occupied by protected contents");
               }
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Special placement cancelled");
            }
         }

         AiOperation placeBlock(BlockPos pos, String id) {
            return AiBuilder.buildSchematicSubtask(new AiBuilder.Blueprint(pos, List.of(new AiBuilder.CellSpec(0, 0, 0, id))), protectedCells);
         }

         AiOperation waterBucketAbove(String action) {
            BlockPos pos = target.above();
            return AiBucket.init(
               AiJson.objectOf("action", action, "fluid", "water", "x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "timeoutSeconds", 10)
            );
         }

         AiOperation useItemOn(BlockPos pos, String item, boolean top) {
            if (!top && mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > 16.0) {
               return AiGoto.approachBlockCompat(pos, 30, protectedCells);
            }

            int slot = AiItems.equipHotbar(AiItems.resolveItem(item));
            if (slot < 0) {
               return AiGameOperations.immediate(AiOperation.Status.FAILED, "Missing " + item, new JsonObject());
            }

            JsonObject args = AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "slot", slot);
            if (top) {
               args.addProperty("face", "UP");
            }

            return AiGameOperations.blockAction(top ? "use" : "interact", args, protectedCells);
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            if (this.waterState == 2 && mc.player != null && mc.level != null) {
               if (this.child != null) {
                  this.child.cancel();
               }

               this.deferredResult = AiGameOperations.result(status, message, new JsonObject());
               this.waterState = 3;
               this.child = this.waterBucketAbove("empty");
               return this.running("Restoring borrowed water before reporting the failed soil conversion");
            } else {
               this.cancel();
               return AiGameOperations.result(status, message, new JsonObject());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.waterState == 0 && (this.child == null || this.child.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.waterState != 0 || this.child != null && this.child.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            if (this.child != null) {
               this.child.cancel();
            }
         }
      };
   }

   static boolean clickCreatedDirt(boolean clicked, Block live, long beforeRevision, long currentRevision) {
      return clicked && currentRevision > beforeRevision && AiScaffold.init(Blocks.DIRT, live);
   }

   static boolean resultPlacedBlocks(AiOperation.Result result) {
      if (result.status() != AiOperation.Status.SUCCEEDED) {
         return false;
      }

      try {
         return AiJson.getInt(AiJson.parseObject(result.dataJson()), "placed", 0) > 0;
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   static void markScaffoldIfCreated(BlockPos support, Block block, boolean createdByOperation) {
      if (createdByOperation && !AiScaffold.isScaffold(support)) {
         AiScaffold.init(support, block);
      }
   }

   static AiOperation init_AiOperation_BlockPos(final BlockPos support, final BlockPos target, final Set<Long> protectedCells, int minimumDigY) {
      final Minecraft mc = Minecraft.getInstance();
      autismclient.ai.engine.AiPathfinder.PathGoal goal = AiGoto.protectingCellsBelowYCompat(
         autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(support.above(), 0.6), protectedCells, minimumDigY
      );
      final Supplier<AiOperation> climb = () -> AiGoto.travelToCompat(
         support.above(), 0.6, true, true, true, 120, autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true, 2.0, goal
      );
      return new AiOperation() {
         AiOperation child;
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer();
         final long deadline = System.nanoTime() + 120000000000L;
         int leanTicks;
         boolean cancelled;
         boolean directAttempted;
         boolean placingDirectly;
         boolean fallbackBuilding;
         boolean directPlaceClicked;
         long supportRevisionBefore;

         @Override
         public AiOperation.Result tick() {
            if (!this.cancelled && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.deadline) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Scaffold construction deadline");
               }

               if (!this.directAttempted) {
                  this.directAttempted = true;
                  if (!mc.player.getBoundingBox().intersects(new AABB(support))
                     && mc.level.getBlockState(support).canBeReplaced()
                     && mc.level.getBlockState(support).getFluidState().isEmpty()
                     && autismclient.ai.engine.PlacementGoal.init(mc, support, Items.DIRT, null, 72) != null) {
                     this.supportRevisionBefore = AiServerEvidence.blockRevision(support);
                     this.placer.start(support, Items.DIRT);
                     this.placingDirectly = true;
                  } else {
                     this.child = climb.get();
                  }
               }

               if (this.placingDirectly) {
                  switch (this.placer.tick()) {
                     case WORKING:
                        return this.running("Placing the reachable temporary support without climbing onto it");
                     case PLACED:
                        Block live = mc.level.getBlockState(support).getBlock();
                        AiSchematicActions.markScaffoldIfCreated(support, live, AiSchematicActions.clickCreatedDirt(this.placer.hasClicked(), live, this.supportRevisionBefore, AiServerEvidence.blockRevision(support)));
                        return this.finish(AiOperation.Status.SUCCEEDED, "Reachable temporary support verified");
                     case FAILED:
                        this.directPlaceClicked = this.placer.hasClicked();
                        this.placer.reset();
                        this.placingDirectly = false;
                        this.child = climb.get();
                  }
               }

               if (this.child != null) {
                  AiOperation.Result r = this.child.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  boolean createdByFallback = this.fallbackBuilding && AiSchematicActions.resultPlacedBlocks(r);
                  this.fallbackBuilding = false;
                  if (createdByFallback) {
                     Block live = mc.level.getBlockState(support).getBlock();
                     AiSchematicActions.markScaffoldIfCreated(support, live, AiScaffold.init(Blocks.DIRT, live));
                  }

                  this.child.cancel();
                  this.child = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finish(r.status(), r.message());
                  }
               }

               if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                  this.child = AiBuilder.buildSchematicSubtask(
                     new AiBuilder.Blueprint(support, List.of(new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"))), protectedCells
                  );
                  this.fallbackBuilding = true;
                  return this.running("Placing the temporary support");
               }

               Block live = mc.level.getBlockState(support).getBlock();
               AiSchematicActions.markScaffoldIfCreated(support, live, AiSchematicActions.clickCreatedDirt(this.directPlaceClicked, live, this.supportRevisionBefore, AiServerEvidence.blockRevision(support)));
               if (support.getY() == target.getY()) {
                  Direction side = Direction.getNearest(target.getX() - support.getX(), 0, target.getZ() - support.getZ(), Direction.NORTH);
                  Vec3 edge = Vec3.atBottomCenterOf(support.above()).add(side.getStepX() * 0.7, 0.0, side.getStepZ() * 0.7);
                  if (mc.player.position().distanceToSqr(edge) > 1.0E-4) {
                     AABB edgeBody = mc.player.getBoundingBox().move(edge.subtract(mc.player.position()));
                     if (mc.level.noCollision(mc.player, edgeBody) && ++this.leanTicks <= 45) {
                        mc.options.keyShift.setDown(true);
                        mc.options.keyJump.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        this.aim.aimAt(edge.add(0.0, mc.player.getEyeHeight(), 0.0));
                        this.aim.tick();
                        mc.options.keyUp.setDown(this.aim.isSettled());
                        return this.running("Leaning over the scaffold to expose its side face");
                     }

                     return this.finish(AiOperation.Status.SUCCEEDED, "Support ready; the edge needs a different stance");
                  }
               }

               return this.finish(AiOperation.Status.SUCCEEDED, "Temporary support ready");
            } else {
               return this.finish(AiOperation.Status.CANCELLED, "Scaffold construction cancelled");
            }
         }

         AiOperation.Result running(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result finish(AiOperation.Status status, String message) {
            this.cancel();
            return AiGameOperations.result(status, message, new JsonObject());
         }

         @Override
         public void cancel() {
            this.cancelled = true;
            this.placer.reset();
            if (this.child != null) {
               this.child.cancel();
            }

            if (mc.player != null) {
               mc.options.keyUp.setDown(false);
               mc.options.keyShift.setDown(false);
            }
         }
      };
   }
}
