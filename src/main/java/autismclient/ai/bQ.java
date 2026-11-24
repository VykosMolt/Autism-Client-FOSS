package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.ai.a.a;
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

final class bQ {
   private bQ() {
   }

   static boolean init_Z_BlockPos(BlockPos worker, AABB site, int budget, Predicate<BlockPos> open) {
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

   static BlockPos init(LevelReader world, BlockPos ceiling, Map<Long, TableIndex.Inner_StreamUtil> source) {
      BlockPos below = ceiling.below();
      TableIndex.Inner_StreamUtil cell = source.get(below.asLong());
      if (cell != null && TableIndex.init_BlockState(cell).getBlock() instanceof BedBlock && !TableIndex.read(cell, world.getBlockState(below))) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            BlockPos foot = read_Z(cell) ? below.relative(facing.getOpposite()) : below;
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

   static LevelReader init(LevelReader world, BlockPos cell, BlockState replacement) {
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

   static BlockPos init_BlockPos_TableIndex_StreamU_2(TableIndex.Inner_StreamUtil cell, BlockPos target, LevelReader world, Set<Long> sourceCells) {
      BlockState requested = TableIndex.init_BlockState(cell);
      if (requested.canSurvive(world, target)) {
         return null;
      } else {
         BlockPos below = target.below();
         BlockState live = world.getBlockState(below);
         if (!sourceCells.contains(below.asLong()) && live.canBeReplaced() && live.getFluidState().isEmpty()) {
            return requested.canSurvive(init(world, below, Blocks.DIRT.defaultBlockState()), target) ? below : null;
         } else {
            return null;
         }
      }
   }

   static BlockPos init_BlockPos_TableIndex_StreamU(
      TableIndex.Inner_StreamUtil cell, BlockPos target, LevelReader world, Map<Long, TableIndex.Inner_StreamUtil> source
   ) {
      BlockState requested = TableIndex.init_BlockState(cell);
      if (requested.canSurvive(world, target)) {
         for (Direction direction : Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            TableIndex.Inner_StreamUtil wanted = source.get(neighbour.asLong());
            if (wanted != null
               && !TableIndex.read(wanted, world.getBlockState(neighbour))
               && !requested.canSurvive(init(world, neighbour, Blocks.AIR.defaultBlockState()), target)) {
               return neighbour;
            }
         }

         return null;
      } else {
         for (Direction direction : Direction.values()) {
            BlockPos neighbour = target.relative(direction);
            TableIndex.Inner_StreamUtil wanted = source.get(neighbour.asLong());
            if (wanted != null && requested.canSurvive(init(world, neighbour, TableIndex.init_BlockState(wanted)), target)) {
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
            || autismclient.ai.a.ClassCache.Inner_StreamUtil.load(state)
            || autismclient.ai.a.ClassCache.Inner_StreamUtil.read(state)
            || y >= bottom.getY() && (state.hasBlockEntity() || state.getDestroySpeed(world, pos) < 0.0F)) {
            return false;
         }
      }

      return true;
   }

   static boolean init(BlockPos candidate, Map<Long, TableIndex.Inner_StreamUtil> source, LevelReader world) {
      return init_Z_BlockPos(candidate, source, world, null);
   }

   static boolean init_Z_BlockPos(BlockPos candidate, Map<Long, TableIndex.Inner_StreamUtil> source, LevelReader world, BlockPos ignore) {
      LevelReader removed = null;

      for (Direction direction : Direction.values()) {
         BlockPos dependent = candidate.relative(direction);
         if (!dependent.equals(ignore)) {
            TableIndex.Inner_StreamUtil cell = source.get(dependent.asLong());
            if (cell != null) {
               BlockState requested = TableIndex.init_BlockState(cell);
               if (direction == Direction.UP && requested.getBlock() instanceof FallingBlock) {
                  return true;
               }

               if (requested.canSurvive(world, dependent)) {
                  if (removed == null) {
                     removed = init(world, candidate, Blocks.AIR.defaultBlockState());
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

   static AiOperation init(final BlockPos top, final BlockPos bottom, final Set<Long> protectedCells) {
      return init_AiOperation_BlockPos_2(top, bottom, protectedCells, protectedCells);
   }

   static AiOperation init_AiOperation_BlockPos_2(
      final BlockPos top, final BlockPos bottom, final Set<Long> protectedCells, final Set<Long> digProtected
   ) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         AiOperation init_AiOperation = HashUtil.init_AiOperation_BlockPos(top, 0.25, false, false, false, 20);
         final long init_J = System.nanoTime() + 40000000000L;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Shaft descent deadline");
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init(r.status(), r.message());
                  }
               }

               autismclient.ai.a.AiMotionSafety.putAt_V();
               if (!autismclient.ai.a.AiMotionSafety.fill_Z()) {
                  return new AiOperation.Result(AiOperation.Status.RUNNING, "Landing inside the access shaft", "{}", List.of());
               }

               if (mc.player.getY() <= bottom.getY() + 0.25 && mc.player.blockPosition().equals(bottom)) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Buried construction stance reached");
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
                     return this.init(AiOperation.Status.FAILED, "The shaft descent would leave its temporary access column");
                  } else if (bQ.init(mc.level, bottom, floor.getY()) && autismclient.ai.a.AiMotionSafety.putAt(floor)) {
                     this.init_AiOperation = autismclient.ai.a.ConfigStore.init(floor, 10, true, digProtected, false);
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Opening the next floor of the access shaft", "{}", List.of());
                  } else {
                     return this.init(AiOperation.Status.FAILED, "The access shaft has no safe landing after its floor is removed");
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
               return this.init(AiOperation.Status.CANCELLED, "Shaft descent cancelled");
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.cancel();
            return new AiOperation.Result(status, message, "{}", List.of());
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation == null ? autismclient.ai.a.AiMotionSafety.fill_Z() : this.init_AiOperation.canInterruptNow();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            autismclient.ai.a.AiMotionSafety.putAt_V();
         }
      };
   }

   static AiOperation init(final TableIndex.Inner_StreamUtil cell, final BlockPos target, final autismclient.ai.a.u.Inner_DataUtil constraint) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player.onGround()
         && !(Math.abs(mc.player.getY() - target.getY() - 1.0) > 1.0E-5)
         && Block.isShapeFullBlock(TableIndex.init_BlockState(cell).getCollisionShape(mc.level, target))) {
         BlockPos floor = new BlockPos(mc.player.blockPosition().getX(), target.getY(), mc.player.blockPosition().getZ());
         int dx = target.getX() - floor.getX();
         int dz = target.getZ() - floor.getZ();
         if (Math.abs(dx) + Math.abs(dz) == 1 && Block.isShapeFullBlock(mc.level.getBlockState(floor).getCollisionShape(mc.level, floor))) {
            Direction face = Direction.getNearest(dx, 0, dz, Direction.NORTH);
            if (constraint.acceptsFace(face) && constraint.acceptsRay(-dx * 0.2, -1.5, -dz * 0.2)) {
               final Vec3 edge = Vec3.atBottomCenterOf(floor.above()).add(dx * 0.7, 0.0, dz * 0.7);
               if (!autismclient.ai.a.AiMotionSafety.putAt_Z_Vec3_Vec3(mc.player.position(), edge)) {
                  return null;
               }

               Vec3 displacement = edge.subtract(mc.player.position());

               for (int i = 1; i <= 8; i++) {
                  if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(displacement.scale(i / 8.0)).deflate(0.001))) {
                     return null;
                  }
               }

               return new AiOperation() {
                  final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
                  int init_I;

                  @Override
                  public AiOperation.Result tick() {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     mc.player.setSprinting(false);
                     mc.options.keyShift.setDown(true);
                     if (mc.player.onGround() && ++this.init_I <= 60) {
                        if (autismclient.ai.a.u.init(mc, target, TableIndex.init_BlockState(cell).getBlock().asItem(), constraint, 72) != null) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Support side exposed");
                        }

                        Vec3 remaining = edge.subtract(mc.player.position());
                        if (!autismclient.ai.a.AiMotionSafety.putAt_Z_Vec3_Vec3(mc.player.position(), edge)) {
                           return this.init(AiOperation.Status.FAILED, "Support-edge alignment would touch lava");
                        }

                        if (remaining.lengthSqr() < 0.0025) {
                           return this.init(AiOperation.Status.FAILED, "Support side is occluded");
                        }

                        for (int i = 1; i <= 4; i++) {
                           if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(remaining.scale(i / 4.0)).deflate(0.001))) {
                              return this.init(AiOperation.Status.FAILED, "Support-edge alignment is obstructed");
                           }
                        }

                        this.init_DataUtil.init_V_Vec3(edge.add(0.0, mc.player.getEyeHeight(), 0.0));
                        this.init_DataUtil.load_V();
                        mc.options.keyUp.setDown(this.init_DataUtil.load_Z());
                        return new AiOperation.Result(AiOperation.Status.RUNNING, "Exposing the support side", "{}", List.of());
                     } else {
                        return this.init(AiOperation.Status.FAILED, "Support-edge alignment failed");
                     }
                  }

                  AiOperation.Result init(AiOperation.Status status, String message) {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     return new AiOperation.Result(status, message, "{}", List.of());
                  }

                  @Override
                  public void cancel() {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
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

   static List<BlockPos> init(TableIndex.Inner_DataUtil blueprint) {
      return init(blueprint, blueprint.cells().stream().mapToInt(TableIndex.Inner_StreamUtil::dy).max().orElse(0));
   }

   static List<BlockPos> init(TableIndex.Inner_DataUtil blueprint, int highestWorkDy) {
      List<TableIndex.Inner_StreamUtil> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return List.of();
      }

      int bottom = cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dy).min().orElse(0);
      int height = Math.min(highestWorkDy, cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dy).max().orElse(0)) - bottom + 1;
      if (height < 6) {
         return List.of();
      }

      int left = cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dx).min().orElse(0) - 3;
      int right = cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dx).max().orElse(0) + 3;
      int front = cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dz).min().orElse(0) - 3;
      int back = cells.stream().mapToInt(TableIndex.Inner_StreamUtil::dz).max().orElse(0) + 3;
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

   static AiOperation init(TableIndex.Inner_DataUtil blueprint, Set<Long> protectedCells, Set<Long> owned) {
      return init_AiOperation_TableIndex_DataUti(
         blueprint, protectedCells, owned, blueprint.cells().stream().mapToInt(TableIndex.Inner_StreamUtil::dy).max().orElse(0)
      );
   }

   static AiOperation init_AiOperation_TableIndex_DataUti(
      final TableIndex.Inner_DataUtil blueprint, final Set<Long> protectedCells, final Set<Long> owned, int highestWorkDy
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
         ? R.init_AiOperation(AiOperation.Status.SUCCEEDED, "Existing access stairs verified", new JsonObject())
         : new AiOperation() {
            AiOperation init_AiOperation;
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            int init_I = 1;
            int load_I;
            int read;
            boolean init_Z;
            boolean load_Z;
            String init_Str;
            final long init_J = System.nanoTime() + 600000000000L;

            @Override
            public AiOperation.Result tick() {
               if (this.init_Str != null) {
                  return this.init(AiOperation.Status.FAILED, this.init_Str);
               }

               if (!this.init_Z && mc.player != null && mc.level != null) {
                  if (System.nanoTime() > this.init_J) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Access construction deadline");
                  }

                  if (!steps.isEmpty() && this.init_I < steps.size()) {
                     if (this.init_AiOperation != null) {
                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return this.init(r.message());
                        }

                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (r.status() != AiOperation.Status.SUCCEEDED && !this.load_Z) {
                           return this.init(r.status(), r.message());
                        }

                        this.load_Z = false;
                        this.load_I++;
                        this.read = 0;
                     }

                     BlockPos previous = steps.get(this.init_I - 1);
                     BlockPos top = steps.get(this.init_I);
                     BlockPos under = top.below();
                     HashSet<Long> keep = new HashSet<>(protectedCells);
                     keep.addAll(owned);
                     if (this.load_I == 0) {
                        this.init_AiOperation = this.init(previous.above(), keep);
                        return this.init("Approaching access step " + this.init_I);
                     }

                     if (this.load_I == 1) {
                        if (this.init(top)) {
                           this.load_I = 5;
                           this.read = 0;
                        } else if (this.init(under)) {
                           this.load_I = 3;
                           this.read = 0;
                        } else {
                           Direction direction = Direction.getNearest(top.getX() - previous.getX(), 0, top.getZ() - previous.getZ(), Direction.NORTH);
                           Vec3 edge = Vec3.atBottomCenterOf(previous.above()).add(direction.getStepX() * 0.7, 0.0, direction.getStepZ() * 0.7);
                           Vec3 centre = Vec3.atBottomCenterOf(previous.above());
                           Vec3 delta = mc.player.position().subtract(centre);
                           double along = delta.x * direction.getStepX() + delta.z * direction.getStepZ();
                           double across = delta.x * direction.getStepZ() - delta.z * direction.getStepX();
                           if ((along < 0.67 || Math.abs(across) > 0.1) && !this.init(edge, true)) {
                              return this.init("Exposing the next step's support face");
                           }

                           autismclient.ai.a.AiMotionSafety.putAt_V();
                           mc.options.keyShift.setDown(true);
                           this.load_I = 2;
                           this.read = 0;
                        }
                     }

                     if (this.load_I == 2) {
                        mc.options.keyShift.setDown(true);
                        if (!this.init(under)) {
                           this.init_AiOperation = this.load(under, keep);
                           return this.init("Placing the access step's lower support");
                        }

                        this.load_I = 3;
                     }

                     if (this.load_I == 3) {
                        if (!this.init(Vec3.atBottomCenterOf(previous.above()), false)) {
                           return this.init("Returning onto the access tread");
                        }

                        this.load_I = 4;
                        this.read = 0;
                     }

                     if (this.load_I == 4) {
                        if (!this.init(top)) {
                           this.init_AiOperation = this.load(top, keep);
                           return this.init("Placing the access tread");
                        }

                        this.load_I = 5;
                     }

                     if (this.load_I == 5) {
                        this.init_AiOperation = this.init(top.above(), keep);
                        return this.init("Climbing access step " + this.init_I);
                     }

                     if (this.load_I == 6) {
                        BlockPos landing = this.init(this.init_I);
                        if (landing != null && !this.init(landing) && mc.level.getBlockState(landing).canBeReplaced()) {
                           Direction direction = Direction.getNearest(landing.getX() - top.getX(), 0, landing.getZ() - top.getZ(), Direction.NORTH);
                           Vec3 centre = Vec3.atBottomCenterOf(top.above());
                           Vec3 edge = centre.add(direction.getStepX() * 0.7, 0.0, direction.getStepZ() * 0.7);
                           Vec3 delta = mc.player.position().subtract(centre);
                           double along = delta.x * direction.getStepX() + delta.z * direction.getStepZ();
                           double across = delta.x * direction.getStepZ() - delta.z * direction.getStepX();
                           if ((along < 0.67 || Math.abs(across) > 0.1) && !this.init(edge, true)) {
                              return this.init("Leaning out for the stair landing");
                           }

                           autismclient.ai.a.AiMotionSafety.putAt_V();
                           mc.options.keyShift.setDown(true);
                           this.load_Z = true;
                           this.init_AiOperation = this.load(landing, keep);
                           return this.init("Placing the access stair landing");
                        }

                        this.load_I = 7;
                     }

                     this.init_I++;
                     this.load_I = 0;
                     return this.init("Access step verified");
                  } else {
                     return this.init(AiOperation.Status.SUCCEEDED, "Access stairs verified");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Access construction cancelled");
               }
            }

            boolean init(BlockPos pos) {
               return Block.isShapeFullBlock(mc.level.getBlockState(pos).getCollisionShape(mc.level, pos));
            }

            BlockPos init(int at) {
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

            AiOperation init(BlockPos feet, Set<Long> keep) {
               return HashUtil.init(
                  feet,
                  0.25,
                  true,
                  false,
                  false,
                  30,
                  autismclient.ai.a.ClassCache.Inner_IoWorker.init,
                  true,
                  2.0,
                  HashUtil.init_ClassCache_JsonUti_ClassCache_JsonUti(
                     autismclient.ai.a.ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(feet, 0.25), keep, blueprint.origin().getY()
                  )
               );
            }

            AiOperation load(final BlockPos pos, Set<Long> keep) {
               BlockState state = mc.level.getBlockState(pos);
               if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                  final AiOperation build = TableIndex.load(
                     new TableIndex.Inner_DataUtil(pos, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:dirt"))), keep
                  );
                  return new AiOperation() {
                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result r = build.tick();
                        if (r.status() == AiOperation.Status.SUCCEEDED && init(pos)) {
                           a.init(pos, mc.level.getBlockState(pos).getBlock());
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
                  return R.init_AiOperation(AiOperation.Status.FAILED, "Access route is occupied at " + pos.toShortString(), new JsonObject());
               }
            }

            boolean init(Vec3 point, boolean sneak) {
               if (!autismclient.ai.a.AiMotionSafety.putAt_Z_Vec3_Vec3(mc.player.position(), point)) {
                  this.init_Str = "Access alignment would touch lava";
                  return false;
               } else {
                  double dx = point.x - mc.player.getX();
                  double dz = point.z - mc.player.getZ();
                  if (dx * dx + dz * dz < (sneak ? 1.0E-4 : 0.01)) {
                     autismclient.ai.a.AiMotionSafety.putAt_V();
                     return true;
                  } else if (++this.read > 90) {
                     this.init_Str = "Access alignment could not reach " + point + " from " + mc.player.position();
                     return false;
                  } else {
                     mc.player.setSprinting(false);
                     mc.options.keySprint.setDown(false);
                     mc.options.keyShift.setDown(sneak);
                     mc.options.keyJump.setDown(false);
                     mc.options.keyDown.setDown(false);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     this.init_DataUtil.init_V_Vec3(point.add(0.0, mc.player.getEyeHeight(), 0.0));
                     this.init_DataUtil.load_V();
                     mc.options.keyUp.setDown(this.init_DataUtil.load_Z());
                     return false;
                  }
               }
            }

            AiOperation.Result init(String message) {
               return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Step " + this.init_I + "/" + (steps.size() - 1) + ": " + message, new JsonObject());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.cancel();
               return R.init_AiOperation_Result(status, message, new JsonObject());
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
               }

               autismclient.ai.a.AiMotionSafety.putAt_V();
            }
         };
   }

   static String init(BlockPos target, BlockGetter world, Map<Long, TableIndex.Inner_StreamUtil> wanted) {
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
                  TableIndex.Inner_StreamUtil cell = wanted.get(next.asLong());
                  if (cell != null) {
                     BlockState live = world.getBlockState(next);
                     if (!read(live)) {
                        if (TableIndex.init_Fluid(cell) == null && !"true".equals(cell.properties().get("waterlogged"))) {
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

   static List<BlockPos> init(TableIndex.Inner_DataUtil blueprint, Map<Long, TableIndex.Inner_StreamUtil> wanted, BlockGetter world) {
      return init_List_TableIndex_DataUti(blueprint, wanted, world, bQ::init);
   }

   static List<BlockPos> load(TableIndex.Inner_DataUtil blueprint, Map<Long, TableIndex.Inner_StreamUtil> wanted, BlockGetter world) {
      LinkedHashSet<BlockPos> out = new LinkedHashSet<>();

      for (TableIndex.Inner_StreamUtil cell : blueprint.cells()) {
         Block block = TableIndex.init_BlockState(cell).getBlock();
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
            ? autismclient.ai.a.UtilCore.init_I(BuiltInRegistries.ITEM.getKey(s.getBlock().asItem()).toString()) >= 0
            : false;
      }
   }

   static List<BlockPos> init_List_TableIndex_DataUti(
      TableIndex.Inner_DataUtil blueprint, Map<Long, TableIndex.Inner_StreamUtil> wanted, BlockGetter world, Predicate<BlockState> clearable
   ) {
      List<TableIndex.Inner_StreamUtil> cells = blueprint.cells();
      if (cells.isEmpty()) {
         return List.of();
      }

      int minX = 0;
      int minY = 0;
      int minZ = 0;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;

      for (TableIndex.Inner_StreamUtil c : cells) {
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

   static boolean init_Z(TableIndex.Inner_StreamUtil cell) {
      String id = cell.block();
      return id.equals("minecraft:farmland")
         || id.equals("minecraft:dirt_path")
         || TableIndex.init_Fluid(cell) != null
         || id.startsWith("minecraft:potted_")
         || "upper".equals(cell.properties().get("half"))
         || read_Z(cell)
         || write(cell)
         || load_Z(cell);
   }

   static boolean load_Z(TableIndex.Inner_StreamUtil cell) {
      return cell.block().equals("minecraft:composter") && !"0".equals(cell.properties().getOrDefault("level", "0"));
   }

   static boolean read_Z(TableIndex.Inner_StreamUtil cell) {
      return "head".equals(cell.properties().get("part")) && cell.block().endsWith("_bed");
   }

   static boolean write(TableIndex.Inner_StreamUtil cell) {
      return cell.block().equals("minecraft:water_cauldron")
         || cell.block().equals("minecraft:lava_cauldron")
         || cell.block().equals("minecraft:powder_snow_cauldron");
   }

   static Item init_Item(TableIndex.Inner_StreamUtil cell) {
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

   static boolean init_Z(TableIndex.Inner_StreamUtil cell, BlockState live) {
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

   static List<String> init_List(TableIndex.Inner_StreamUtil cell) {
      String id = cell.block();
      if (TableIndex.init_Fluid(cell) != null || id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path") || read_Z(cell)) {
         return List.of();
      }

      if (load_Z(cell)) {
         return List.of();
      }

      if (!write(cell)) {
         if (id.startsWith("minecraft:potted_")) {
            return init(TableIndex.init_BlockState(cell).getBlock()) != Items.AIR ? List.of() : List.of("no plant item for the pot");
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

   static String init_Str(TableIndex.Inner_StreamUtil cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return null;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String name = entry.getKey();
         if (TableIndex.init_TableIndex_TaskQue(live.getBlock(), name) == TableIndex.Inner_TaskQueue.poll) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(name);
            if (property != null) {
               String actual = String.valueOf(live.getValue(property));
               String requested = entry.getValue();
               if (!TableIndex.init(live.getBlock(), name, actual, requested)) {
                  Item needed = TableIndex.init_Item(live.getBlock(), name);
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

                  if (TableIndex.init_Z(live.getBlock(), name) && Integer.parseInt(actual) < Integer.parseInt(requested)) {
                     return BuiltInRegistries.ITEM.getKey(live.getBlock().asItem()).toString();
                  }

                  return null;
               }
            }
         }
      }

      return null;
   }

   static AiOperation init(final TableIndex.Inner_StreamUtil cell, final BlockPos target, final Set<Long> protectedCells) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         AiOperation init_AiOperation;
         int init_I;

         @Override
         public AiOperation.Result tick() {
            if (mc.player != null && mc.level != null) {
               mc.options.keyShift.setDown(false);
               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return r;
                  }
               }

               BlockState live = mc.level.getBlockState(target);
               if (TableIndex.read(cell, live)) {
                  return R.init_AiOperation_Result(AiOperation.Status.SUCCEEDED, "Interaction state verified", new JsonObject());
               }

               if (++this.init_I > (cell.properties().containsKey("note") ? 30 : (cell.properties().containsKey("delay") ? 6 : 2))) {
                  return R.init_AiOperation_Result(AiOperation.Status.FAILED, "Interaction state did not settle", new JsonObject());
               }

               if (TableIndex.flush(cell, live)) {
                  BlockHitResult hit = autismclient.ai.a.CachePool.init(mc, target);
                  if (hit != null && !(mc.player.getEyePosition().distanceToSqr(hit.getLocation()) > 16.0)) {
                     this.init_AiOperation = TextFormat.init(
                        ag.init_JsonObject_ObjArr(
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
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Restoring waterlogging with a bucket", new JsonObject());
                  } else {
                     this.init_AiOperation = HashUtil.init_AiOperation_BlockPos(target, 30, protectedCells);
                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Approaching the waterlogged block", new JsonObject());
                  }
               } else {
                  JsonObject args = ag.init_JsonObject_ObjArr("x", target.getX(), "y", target.getY(), "z", target.getZ());
                  String itemSpec = bQ.init_Str(cell, live);
                  if (itemSpec != null) {
                     int slot = af.read(af.load_af_DataUtil(itemSpec));
                     if (slot < 0) {
                        return R.init_AiOperation_Result(AiOperation.Status.FAILED, "Missing " + itemSpec, new JsonObject());
                     }

                     args.addProperty("slot", slot);
                  }

                  this.init_AiOperation = R.init("interact", args, protectedCells);
                  return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Approaching the block's interaction face", new JsonObject());
               }
            } else {
               return R.init_AiOperation_Result(AiOperation.Status.CANCELLED, "World unavailable", new JsonObject());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   static List<CharEncoder.Inner_CachePool> load_List(TableIndex.Inner_StreamUtil cell) {
      return CharEncoder.init(cell);
   }

   static List<CharEncoder.Inner_CachePool> init(TableIndex.Inner_StreamUtil cell, BlockPos target) {
      Minecraft mc = Minecraft.getInstance();
      boolean waterAbove = mc.level.getFluidState(target.above()).is(FluidTags.WATER);
      return CharEncoder.init(cell, mc.level.getBlockState(target), waterAbove);
   }

   static List<Item> read_List(TableIndex.Inner_StreamUtil cell) {
      return init(load_List(cell));
   }

   static List<Item> load(TableIndex.Inner_StreamUtil cell, BlockPos target) {
      return init(init(cell, target));
   }

   private static List<Item> init(List<CharEncoder.Inner_CachePool> requirements) {
      return requirements.stream().map(CharEncoder.Inner_CachePool::item).distinct().toList();
   }

   static AiOperation load(final TableIndex.Inner_StreamUtil cell, final BlockPos target, Set<Long> shared) {
      final Minecraft mc = Minecraft.getInstance();
      final HashSet<Long> protectedCells = new HashSet<>(shared);
      return new AiOperation() {
         AiOperation init_AiOperation;
         boolean init_Z;
         int init_I;
         int load;
         int read;
         AiOperation.Result init_AiOperation_Result;
         final long init_J = System.nanoTime() + 120000000000L;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J && this.load != 3) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Special placement deadline");
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     if (this.load == 3) {
                        this.load = 0;
                        return this.init(r.status(), "Water restoration failed: " + r.message());
                     }

                     return this.init(r.status(), r.message());
                  }

                  if (this.load == 1) {
                     this.load = 2;
                  } else if (this.load == 3) {
                     this.load = 0;
                  }

                  if (this.init_AiOperation_Result != null && this.load == 0) {
                     return this.init(this.init_AiOperation_Result.status(), this.init_AiOperation_Result.message());
                  }
               }

               BlockState live = mc.level.getBlockState(target);
               if (this.load == 2 && live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(cell.block()))) {
                  this.load = 3;
                  this.init_AiOperation = this.init_AiOperation("empty");
                  return this.init_AiOperation_Result_11("Restoring water above the converted soil");
               }

               if (TableIndex.update(cell, live)) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Special cell verified");
               }

               if (++this.init_I > 20) {
                  return this.init(AiOperation.Status.FAILED, "Special cell state could not be achieved: " + live);
               }

               String id = cell.block();
               if (bQ.read_Z(cell)) {
                  Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
                  if (facing != null && facing.getAxis().isHorizontal()) {
                     BlockPos foot = target.relative(facing.getOpposite());
                     if (!live.canBeReplaced()) {
                        if ((!live.hasBlockEntity() || live.getBlock() instanceof BedBlock) && live.getFluidState().isEmpty()) {
                           this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                           return this.init_AiOperation_Result_11("Clearing the bed's head cell");
                        } else {
                           return this.init(AiOperation.Status.FAILED, "The bed's head cell is occupied by protected contents");
                        }
                     } else {
                        BlockState footState = mc.level.getBlockState(foot);
                        if (footState.canBeReplaced()) {
                           LinkedHashMap<String, String> props = new LinkedHashMap<>(cell.properties());
                           props.put("part", "foot");
                           protectedCells.add(foot.asLong());
                           this.init_AiOperation = TableIndex.init(
                              new TableIndex.Inner_DataUtil(foot, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, id, props))), protectedCells
                           );
                           return this.init_AiOperation_Result_11("Placing the bed's foot, which makes its head");
                        } else if ((!footState.hasBlockEntity() || footState.getBlock() instanceof BedBlock) && footState.getFluidState().isEmpty()) {
                           this.init_AiOperation = autismclient.ai.a.ConfigStore.init(foot, 30, true, protectedCells);
                           return this.init_AiOperation_Result_11("Clearing the bed's foot cell");
                        } else {
                           return this.init(AiOperation.Status.FAILED, "The bed's foot cell is occupied by protected contents");
                        }
                     }
                  } else {
                     return this.init(AiOperation.Status.FAILED, "A bed head needs a horizontal facing");
                  }
               } else if (bQ.load_Z(cell)) {
                  if (live.is(Blocks.COMPOSTER)) {
                     int level = live.getValue(ComposterBlock.LEVEL);
                     int wanted = Integer.parseInt(cell.properties().get("level"));
                     if (level > wanted) {
                        this.init_AiOperation = level == 8
                           ? this.init(target, "minecraft:dirt", false)
                           : autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                        return this.init_AiOperation_Result_11("Emptying the overfilled composter");
                     } else if (level == 7 && wanted == 8) {
                        this.init_I--;
                        return this.init_AiOperation_Result_11("Waiting for the full composter to turn ready");
                     } else {
                        this.init_AiOperation = this.init(target, "minecraft:pumpkin_pie", false);
                        return this.init_AiOperation_Result_11("Composting a pumpkin pie");
                     }
                  } else if (live.canBeReplaced()) {
                     this.init_AiOperation = this.init(target, "minecraft:composter");
                     return this.init_AiOperation_Result_11("Placing the composter");
                  } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                     this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                     return this.init_AiOperation_Result_11("Clearing the composter cell");
                  } else {
                     return this.init(AiOperation.Status.FAILED, "The composter cell is occupied by protected contents");
                  }
               } else if (!bQ.write(cell)) {
                  if ("upper".equals(cell.properties().get("half"))) {
                     if (live.canBeReplaced() || live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                        BlockPos root = target.below();
                        BlockState lower = mc.level.getBlockState(root);
                        if (lower.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                           if (lower.getBlock() instanceof DoorBlock
                              && live.getBlock() == lower.getBlock()
                              && !String.valueOf(lower.getValue(DoorBlock.OPEN)).equals(cell.properties().getOrDefault("open", "false"))) {
                              mc.options.keyShift.setDown(false);
                              this.init_AiOperation = R.init(
                                 "interact", ag.init_JsonObject_ObjArr("x", root.getX(), "y", root.getY(), "z", root.getZ()), protectedCells
                              );
                              return this.init_AiOperation_Result_11("Restoring the paired door's open state");
                           } else {
                              this.init_AiOperation = autismclient.ai.a.ConfigStore.init(root, 30, true, protectedCells);
                              return this.init_AiOperation_Result_11("Recovering the conflicting paired block");
                           }
                        } else if (!lower.canBeReplaced()) {
                           this.init_AiOperation = autismclient.ai.a.ConfigStore.init(root, 30, true, protectedCells);
                           return this.init_AiOperation_Result_11("Clearing the missing lower half");
                        } else if (mc.level.getBlockState(root.below()).isAir()) {
                           this.init_AiOperation = this.init(root.below(), "minecraft:dirt");
                           return this.init_AiOperation_Result_11("Supporting the lower half");
                        } else {
                           LinkedHashMap<String, String> props = new LinkedHashMap<>(cell.properties());
                           props.put("half", "lower");
                           protectedCells.add(root.asLong());
                           this.init_AiOperation = TableIndex.init(
                              new TableIndex.Inner_DataUtil(root, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, id, props))), protectedCells
                           );
                           return this.init_AiOperation_Result_11("Placing the lower half to create the requested upper half");
                        }
                     } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                        this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                        return this.init_AiOperation_Result_11("Clearing the occupied upper half");
                     } else {
                        return this.init(AiOperation.Status.FAILED, "The upper half is occupied by protected contents");
                     }
                  } else if (TableIndex.init_Fluid(cell) != null) {
                     String fluid = id.equals("minecraft:lava") ? "lava" : "water";
                     if (!"0".equals(cell.properties().getOrDefault("level", "0"))) {
                        return this.init(AiOperation.Status.FAILED, "Flowing " + fluid + " must be produced by surrounding source cells");
                     }

                     if (live.isAir() || live.canBeReplaced() || !live.getFluidState().isEmpty()) {
                        if (TextFormat.init(mc, target, false, fluid.equals("water")) == null) {
                           this.init_AiOperation = HashUtil.init_AiOperation_BlockPos_3(target.below(), 30, protectedCells, true, Direction.UP, 1.0);
                           return this.init_AiOperation_Result_11("Approaching a visible receiving face for the water cell");
                        } else {
                           this.init_AiOperation = TextFormat.init(
                              ag.init_JsonObject_ObjArr("action", "empty", "fluid", fluid, "x", target.getX(), "y", target.getY(), "z", target.getZ())
                           );
                           return this.init_AiOperation_Result_11("Emptying a " + fluid + " bucket");
                        }
                     } else {
                        if (live.hasBlockEntity()) {
                           return this.init(AiOperation.Status.FAILED, "The water cell is occupied by protected contents");
                        }

                        this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, false, protectedCells);
                        return this.init_AiOperation_Result_11("Clearing the block where the water goes");
                     }
                  } else if (id.equals("minecraft:farmland") || id.equals("minecraft:dirt_path")) {
                     if (live.getBlock() == BuiltInRegistries.BLOCK.getValue(Identifier.parse(id))) {
                        return this.init(AiOperation.Status.SUCCEEDED, "Soil converted; moisture settles from surrounding water");
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
                              BlockHitResult top = autismclient.ai.a.CachePool.init(mc, target, Direction.UP);
                              if ((top == null || mc.player.getEyePosition().distanceTo(top.getLocation()) > mc.player.blockInteractionRange() - 0.3)
                                 && this.read++ < 3) {
                                 this.init_AiOperation = HashUtil.init_AiOperation_BlockPos_3(target, 30, protectedCells, true, Direction.UP, 1.25);
                                 return this.init_AiOperation_Result_11("Approaching the soil's top face");
                              } else {
                                 this.init_AiOperation = this.init(target, id.equals("minecraft:farmland") ? "hoe" : "shovel", true);
                                 return this.init_AiOperation_Result_11("Converting the soil");
                              }
                           } else if (cover.getFluidState().is(FluidTags.WATER) && cover.getFluidState().isSource() && this.load == 0) {
                              Vec3 eye = mc.player.getEyePosition();
                              Vec3 centre = Vec3.atCenterOf(above);
                              BlockHitResult hit = mc.level
                                 .clip(new ClipContext(eye, centre, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.SOURCE_ONLY, mc.player));
                              if (!(eye.distanceToSqr(centre) > 16.0) && hit.getBlockPos().equals(above)) {
                                 this.load = 1;
                                 this.init_AiOperation = this.init_AiOperation("fill");
                                 return this.init_AiOperation_Result_11("Lifting the water to expose the soil");
                              } else {
                                 this.init_AiOperation = HashUtil.init(
                                    above,
                                    0.45,
                                    false,
                                    false,
                                    false,
                                    15,
                                    autismclient.ai.a.ClassCache.Inner_IoWorker.init,
                                    true,
                                    2.0,
                                    HashUtil.init_ClassCache_JsonUti_ClassCache_JsonUti(
                                       autismclient.ai.a.ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(above, 0.45),
                                       protectedCells,
                                       target.getY()
                                    )
                                 );
                                 return this.init_AiOperation_Result_11("Approaching the water above the soil");
                              }
                           } else if (!cover.hasBlockEntity() && cover.getFluidState().isEmpty() && !protectedCells.contains(above.asLong())) {
                              this.init_AiOperation = autismclient.ai.a.ConfigStore.init(above, 10, true, protectedCells, false);
                              return this.init_AiOperation_Result_11("Clearing the temporary obstruction above the soil");
                           } else {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "The soil's top face is occupied by protected contents ("
                                    + BuiltInRegistries.BLOCK.getKey(cover.getBlock())
                                    + (cover.getFluidState().isEmpty() ? "" : ", fluid")
                                    + ")"
                              );
                           }
                        } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                           this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                           return this.init_AiOperation_Result_11("Clearing the non-soil block before planting");
                        } else {
                           return this.init(AiOperation.Status.FAILED, "The soil cell is occupied by protected contents");
                        }
                     } else {
                        this.init_AiOperation = this.init(target, "minecraft:dirt");
                        return this.init_AiOperation_Result_11("Placing soil");
                     }
                  } else if (id.startsWith("minecraft:potted_")) {
                     Item plant = bQ.init(TableIndex.init_BlockState(cell).getBlock());
                     if (plant == Items.AIR) {
                        return this.init(AiOperation.Status.FAILED, "No plant item for " + id);
                     } else if (!live.is(Blocks.FLOWER_POT)) {
                        this.init_AiOperation = this.init(target, "minecraft:flower_pot");
                        return this.init_AiOperation_Result_11("Placing a flower pot");
                     } else {
                        this.init_AiOperation = this.init(target, BuiltInRegistries.ITEM.getKey(plant).toString(), false);
                        return this.init_AiOperation_Result_11("Planting the pot");
                     }
                  } else {
                     return this.init(AiOperation.Status.FAILED, "No survival construction strategy for " + id);
                  }
               } else if (live.getBlock() instanceof AbstractCauldronBlock) {
                  Block wanted = BuiltInRegistries.BLOCK.getValue(Identifier.parse(id));
                  if (live.getBlock() != wanted && !live.is(Blocks.CAULDRON)) {
                     this.init_AiOperation = this.init(target, "minecraft:bucket", false);
                     return this.init_AiOperation_Result_11("Emptying the cauldron's wrong contents");
                  }

                  if (live.is(Blocks.CAULDRON)) {
                     this.init_AiOperation = this.init(target, BuiltInRegistries.ITEM.getKey(bQ.init_Item(cell)).toString(), false);
                     return this.init_AiOperation_Result_11("Filling the cauldron");
                  }

                  if (bQ.init_Z(cell, live)) {
                     this.init_AiOperation = this.init(target, BuiltInRegistries.ITEM.getKey(bQ.init_Item(cell)).toString(), false);
                     return this.init_AiOperation_Result_11("Refilling the cauldron");
                  }

                  if (live.is(Blocks.WATER_CAULDRON)) {
                     int current = live.getValue(LayeredCauldronBlock.LEVEL);
                     int wantedLevel = Integer.parseInt(cell.properties().getOrDefault("level", "3"));
                     if (current > wantedLevel) {
                        this.init_AiOperation = this.init(target, "minecraft:glass_bottle", false);
                        return this.init_AiOperation_Result_11("Taking a bottle of water out of the cauldron");
                     }
                  }

                  return this.init(AiOperation.Status.FAILED, "The cauldron's contents cannot be set to " + cell.properties());
               } else if (live.canBeReplaced()) {
                  this.init_AiOperation = this.init(target, "minecraft:cauldron");
                  return this.init_AiOperation_Result_11("Placing the cauldron");
               } else if (!live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                  this.init_AiOperation = autismclient.ai.a.ConfigStore.init(target, 30, true, protectedCells);
                  return this.init_AiOperation_Result_11("Clearing the cauldron cell");
               } else {
                  return this.init(AiOperation.Status.FAILED, "The cauldron cell is occupied by protected contents");
               }
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Special placement cancelled");
            }
         }

         AiOperation init(BlockPos pos, String id) {
            return TableIndex.init(new TableIndex.Inner_DataUtil(pos, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, id))), protectedCells);
         }

         AiOperation init_AiOperation(String action) {
            BlockPos pos = target.above();
            return TextFormat.init(
               ag.init_JsonObject_ObjArr("action", action, "fluid", "water", "x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "timeoutSeconds", 10)
            );
         }

         AiOperation init(BlockPos pos, String item, boolean top) {
            if (!top && mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > 16.0) {
               return HashUtil.init_AiOperation_BlockPos(pos, 30, protectedCells);
            }

            int slot = af.read(af.load_af_DataUtil(item));
            if (slot < 0) {
               return R.init_AiOperation(AiOperation.Status.FAILED, "Missing " + item, new JsonObject());
            }

            JsonObject args = ag.init_JsonObject_ObjArr("x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "slot", slot);
            if (top) {
               args.addProperty("face", "UP");
            }

            return R.init(top ? "use" : "interact", args, protectedCells);
         }

         AiOperation.Result init_AiOperation_Result_11(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            if (this.load == 2 && mc.player != null && mc.level != null) {
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
               }

               this.init_AiOperation_Result = R.init_AiOperation_Result(status, message, new JsonObject());
               this.load = 3;
               this.init_AiOperation = this.init_AiOperation("empty");
               return this.init_AiOperation_Result_11("Restoring borrowed water before reporting the failed soil conversion");
            } else {
               this.cancel();
               return R.init_AiOperation_Result(status, message, new JsonObject());
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.load == 0 && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.load != 0 || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   static boolean init_Z_Z(boolean clicked, Block live, long beforeRevision, long currentRevision) {
      return clicked && currentRevision > beforeRevision && a.init(Blocks.DIRT, live);
   }

   static boolean init(AiOperation.Result result) {
      if (result.status() != AiOperation.Status.SUCCEEDED) {
         return false;
      }

      try {
         return ag.init_I_JsonObject(ag.init_JsonObject_Str(result.dataJson()), "placed", 0) > 0;
      } catch (RuntimeException ignored) {
         return false;
      }
   }

   static void init(BlockPos support, Block block, boolean createdByOperation) {
      if (createdByOperation && !a.init_Z(support)) {
         a.init(support, block);
      }
   }

   static AiOperation init_AiOperation_BlockPos(final BlockPos support, final BlockPos target, final Set<Long> protectedCells, int minimumDigY) {
      final Minecraft mc = Minecraft.getInstance();
      autismclient.ai.a.ClassCache.Inner_JsonUtil goal = HashUtil.init_ClassCache_JsonUti_ClassCache_JsonUti(
         autismclient.ai.a.ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(support.above(), 0.6), protectedCells, minimumDigY
      );
      final Supplier<AiOperation> climb = () -> HashUtil.init(
         support.above(), 0.6, true, true, true, 120, autismclient.ai.a.ClassCache.Inner_IoWorker.init, true, 2.0, goal
      );
      return new AiOperation() {
         AiOperation init_AiOperation;
         final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
         final autismclient.ai.a.DataPipe init_DataPipe = new autismclient.ai.a.DataPipe();
         final long init_J = System.nanoTime() + 120000000000L;
         int init_I;
         boolean init_Z;
         boolean load_Z;
         boolean read;
         boolean write;
         boolean update;
         long load_J;

         @Override
         public AiOperation.Result tick() {
            if (!this.init_Z && mc.player != null && mc.level != null) {
               if (System.nanoTime() > this.init_J) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Scaffold construction deadline");
               }

               if (!this.load_Z) {
                  this.load_Z = true;
                  if (!mc.player.getBoundingBox().intersects(new AABB(support))
                     && mc.level.getBlockState(support).canBeReplaced()
                     && mc.level.getBlockState(support).getFluidState().isEmpty()
                     && autismclient.ai.a.u.init(mc, support, Items.DIRT, null, 72) != null) {
                     this.load_J = ca.init_J_BlockPos(support);
                     this.init_DataPipe.init_V_BlockPos_Item(support, Items.DIRT);
                     this.read = true;
                  } else {
                     this.init_AiOperation = climb.get();
                  }
               }

               if (this.read) {
                  switch (this.init_DataPipe.init_DataPipe_DataUtil()) {
                     case init:
                        return this.init("Placing the reachable temporary support without climbing onto it");
                     case load:
                        Block live = mc.level.getBlockState(support).getBlock();
                        bQ.init(support, live, bQ.init_Z_Z(this.init_DataPipe.init_Z(), live, this.load_J, ca.init_J_BlockPos(support)));
                        return this.init(AiOperation.Status.SUCCEEDED, "Reachable temporary support verified");
                     case read:
                        this.update = this.init_DataPipe.init_Z();
                        this.init_DataPipe.init_V();
                        this.read = false;
                        this.init_AiOperation = climb.get();
                  }
               }

               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  boolean createdByFallback = this.write && bQ.init(r);
                  this.write = false;
                  if (createdByFallback) {
                     Block live = mc.level.getBlockState(support).getBlock();
                     bQ.init(support, live, a.init(Blocks.DIRT, live));
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init(r.status(), r.message());
                  }
               }

               if (mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                  this.init_AiOperation = TableIndex.init(
                     new TableIndex.Inner_DataUtil(support, List.of(new TableIndex.Inner_StreamUtil(0, 0, 0, "minecraft:dirt"))), protectedCells
                  );
                  this.write = true;
                  return this.init("Placing the temporary support");
               }

               Block live = mc.level.getBlockState(support).getBlock();
               bQ.init(support, live, bQ.init_Z_Z(this.update, live, this.load_J, ca.init_J_BlockPos(support)));
               if (support.getY() == target.getY()) {
                  Direction side = Direction.getNearest(target.getX() - support.getX(), 0, target.getZ() - support.getZ(), Direction.NORTH);
                  Vec3 edge = Vec3.atBottomCenterOf(support.above()).add(side.getStepX() * 0.7, 0.0, side.getStepZ() * 0.7);
                  if (mc.player.position().distanceToSqr(edge) > 1.0E-4) {
                     AABB edgeBody = mc.player.getBoundingBox().move(edge.subtract(mc.player.position()));
                     if (mc.level.noCollision(mc.player, edgeBody) && ++this.init_I <= 45) {
                        mc.options.keyShift.setDown(true);
                        mc.options.keyJump.setDown(false);
                        mc.options.keyDown.setDown(false);
                        mc.options.keyLeft.setDown(false);
                        mc.options.keyRight.setDown(false);
                        this.init_DataUtil.init_V_Vec3(edge.add(0.0, mc.player.getEyeHeight(), 0.0));
                        this.init_DataUtil.load_V();
                        mc.options.keyUp.setDown(this.init_DataUtil.load_Z());
                        return this.init("Leaning over the scaffold to expose its side face");
                     }

                     return this.init(AiOperation.Status.SUCCEEDED, "Support ready; the edge needs a different stance");
                  }
               }

               return this.init(AiOperation.Status.SUCCEEDED, "Temporary support ready");
            } else {
               return this.init(AiOperation.Status.CANCELLED, "Scaffold construction cancelled");
            }
         }

         AiOperation.Result init(String message) {
            return R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, new JsonObject());
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            this.cancel();
            return R.init_AiOperation_Result(status, message, new JsonObject());
         }

         @Override
         public void cancel() {
            this.init_Z = true;
            this.init_DataPipe.init_V();
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }

            if (mc.player != null) {
               mc.options.keyUp.setDown(false);
               mc.options.keyShift.setDown(false);
            }
         }
      };
   }
}
