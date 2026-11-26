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

public final class U {
   public static final Direction[] init_DirectionArr = new Direction[]{
      Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.DOWN
   };
   public static final double init_D = 1.0E-4;
   public static final double load_D = 0.2;
   public static final int init_I = 4;
   public static final int load_I = 3;
   public static final int read_I = 48;
   public static final float init_F = 89.5F;
   private static final double read_D = 0.12;
   private static final double write_D = 0.01;
   private static final int write_I = 16;
   private static final double update_D = 0.06;
   private static final AABB[] init_AABBArr = new AABB[0];
   private static final int update_I = 48;
   private static final U.Inner_SyncManager[] init_U_SyncManagerArr = new U.Inner_SyncManager[48];
   private static int sync;
   private static final int flush = 8;
   private static final BlockPos[] init_BlockPosArr = new BlockPos[8];
   private static final BlockState[] init_BlockStateArr = new BlockState[8];
   private static final AABB[][] init_AABBArr2 = new AABB[8][];
   private static int close;
   private static int open = -2147483648;
   private static boolean init_Z;

   private U() {
   }

   public static U.Inner_NetHelper init(ItemStack stack, Player player, InteractionHand hand) {
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

   public static U.Inner_NetHelper init(BlockPos support, EnumSet<Direction> allowedFaces) {
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

   public static double init_D() {
      return 89.5 - RotationStreamPlanner.init_D_D(RotationMath.init()) - 0.01;
   }

   public static void init(U.Inner_StateCache request, List<U.Inner_TaskQueue> out) {
      if (request != null && out != null && request.init_BlockPos() != null) {
         Level level = init_Level();
         if (level != null) {
            BlockPos cell = request.init_BlockPos();
            int start = out.size();
            boolean inPlace = request.read() && init(cell, level);
            MutableBlockPos scan = new MutableBlockPos();

            for (Direction face : request.init_DirectionArr()) {
               if (request.init_EnumSet() == null || request.init_EnumSet().contains(face)) {
                  for (int form = 0; form < 2; form++) {
                     boolean own = form == 0;
                     if (!own || inPlace) {
                        BlockPos support = own ? cell : scan.setWithOffset(cell, face.getOpposite()).immutable();
                        BlockState state = level.getBlockState(support);
                        if (!state.isAir()) {
                           boolean requiresSneak = init_Z(state, support);
                           boolean sneaking = request.init_Z() || requiresSneak && request.load_Z();
                           if ((own || load(state, support, sneaking)) && (!own || !init(state, support, sneaking))) {
                              U.Inner_BufferHelper intent = new U.Inner_BufferHelper(support, face);
                              if (request.init_Predicate() == null || !request.init_Predicate().test(intent)) {
                                 for (AABB rect : init_AABBArr_BlockState(state, support, face, request.init_I())) {
                                    out.add(
                                       new U.Inner_TaskQueue(
                                          cell,
                                          support,
                                          face,
                                          form,
                                          state,
                                          rect,
                                          load(rect, face),
                                          init(rect, face, request.init_Vec3()),
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

            init(out, start, request);
         }
      }
   }

   public static void init(List<BlockPos> cells, U.Inner_StateCache template, List<U.Inner_TaskQueue> out) {
      if (cells != null && template != null && out != null) {
         BlockPos held = template.init_BlockPos();

         for (BlockPos cell : cells) {
            init(template.init_U_StateCache_BlockPos(cell), out);
         }

         template.init_U_StateCache_BlockPos(held);
      }
   }

   public static long init(List<BlockPos> cells, Level level) {
      if (cells != null && level != null) {
         long key = 0L;
         MutableBlockPos scan = new MutableBlockPos();

         for (BlockPos cell : cells) {
            key += init(cell, level, scan);
         }

         return key;
      } else {
         return 0L;
      }
   }

   private static long init(BlockPos cell, Level level, MutableBlockPos scan) {
      long key = cell.asLong() * 31L + System.identityHashCode(level.getBlockState(cell));

      for (Direction direction : Direction.values()) {
         key = key * 31L + System.identityHashCode(level.getBlockState(scan.setWithOffset(cell, direction)));
      }

      key ^= key >>> 33;
      key *= -49064778989728563L;
      return key ^ key >>> 33;
   }

   private static void init(List<U.Inner_TaskQueue> out, int start, U.Inner_StateCache request) {
      int size = out.size();
      if (size - start >= 2) {
         List<U.Inner_TaskQueue> segment = new ArrayList<>(out.subList(start, size));
         Vec3 eye = request.init_Vec3();
         RotationMath.Inner_DataUtil from = request.init_ce_DataUtil();
         Direction[] order = request.init_DirectionArr();
         segment.sort((left, right) -> {
            boolean leftUp = left.init_Direction() == Direction.UP;
            boolean rightUp = right.init_Direction() == Direction.UP;
            if (leftUp != rightUp) {
               return leftUp ? -1 : 1;
            }

            int area = Double.compare(right.init_D(), left.init_D());
            if (area != 0) {
               return area;
            }

            int edge = Double.compare(right.load_D(), left.load_D());
            if (edge != 0) {
               return edge;
            }

            if (from != null) {
               int turn = Float.compare(init(left, eye, from), init(right, eye, from));
               if (turn != 0) {
                  return turn;
               }
            }

            return Integer.compare(init(order, left.init_Direction()), init(order, right.init_Direction()));
         });

         for (int i = 0; i < segment.size(); i++) {
            out.set(start + i, segment.get(i));
         }
      }
   }

   private static float init(U.Inner_TaskQueue option, Vec3 eye, RotationMath.Inner_DataUtil from) {
      Vec3 centre = init_Vec3(option.init_AABB(), option.init_Direction());
      return RotationMath.init_F(from, RotationMath.init(centre, eye));
   }

   private static int init(Direction[] order, Direction face) {
      for (int i = 0; i < order.length; i++) {
         if (order[i] == face) {
            return i;
         }
      }

      return order.length;
   }

   public static U.Inner_DataUtil init(U.Inner_TaskQueue option, U.Inner_StateCache request, U.Inner_JsonUtil[] refusal) {
      init(refusal, U.Inner_JsonUtil.NONE);
      if (option != null && request != null) {
         Level level = init_Level();
         Direction face = option.init_Direction();
         if (level == null) {
            return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NOT_A_SUPPORT, request, face);
         }

         BlockPos support = option.load_BlockPos();
         BlockState state = level.getBlockState(support);
         if (state != option.init_BlockState()) {
            return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NOT_A_SUPPORT, request, face);
         }

         if (request.init_Predicate() != null && request.init_Predicate().test(option.init_U_BufferHelper())) {
            return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.BANNED, request, face);
         }

         boolean sneaking = request.init_Z() || option.init_Z() && request.load_Z();
         if (request.init_U_NetHelper() != null && request.init_U_NetHelper().init(state, support, sneaking)) {
            Vec3 eye = request.init_Vec3();
            if (init_Z_Vec3(eye, support, face, level)) {
               return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.SELF_OCCLUDED, request, face);
            }

            AABB rect = option.init_AABB();
            Vec3 point = init_Vec3_AABB_2(rect, face, eye, init_ce_DataUtil(request), request.read_D);
            if (point == null) {
               return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.BEHIND_PLANE, request, face);
            }

            U.Inner_UiHelper tier = U.Inner_UiHelper.CENTRE;
            double reachSq = request.init_D() * request.init_D();
            if (eye.distanceToSqr(point) > reachSq) {
               point = init_Vec3_AABB(rect, face, eye, U.Inner_UiHelper.NEAREST, request.read_D);
               if (point == null || eye.distanceToSqr(point) > reachSq) {
                  return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.OUT_OF_REACH, request, face);
               }

               tier = U.Inner_UiHelper.NEAREST;
            }

            RotationMath.Inner_DataUtil goal = RotationMath.init(point, eye);
            if (Math.abs(goal.load()) > request.load_D()) {
               Vec3 flat = init_Vec3_AABB(rect, face, eye, U.Inner_UiHelper.FLATTEST, request.read_D);
               if (flat == null || eye.distanceToSqr(flat) > reachSq) {
                  return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.PAST_PITCH_CAP, request, face);
               }

               goal = RotationMath.init(flat, eye);
               if (Math.abs(goal.load()) > request.load_D()) {
                  return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.PAST_PITCH_CAP, request, face);
               }

               point = flat;
               tier = U.Inner_UiHelper.FLATTEST;
            }

            if (face.getAxis().isHorizontal() && request.load_Vec3() != null) {
               double[] window = init(eye, request.load_Vec3(), rect, face, goal.init(), request.read_D);
               if (window != null) {
                  float pitch = (float)Mth.clamp(goal.load(), window[0], window[1]);
                  if (Math.abs(pitch) > request.load_D()) {
                     return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.PAST_PITCH_CAP, request, face);
                  }

                  goal = new RotationMath.Inner_DataUtil(goal.init(), pitch);
               }
            }

            if (!request.init_U_NetHelper().lands(new BlockHitResult(point, face, support, false), request.init_BlockPos())) {
               return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NOT_A_SUPPORT, request, face);
            }

            RotationMath.Inner_DataUtil from = init_ce_DataUtil(request);
            RotationMath.Inner_DataUtil emitted = request.write() ? RotationMath.init_ce_DataUtil(goal, from == null ? goal : from) : goal;
            float turn = from == null ? 0.0F : RotationMath.init_F(from, goal);
            return new U.Inner_DataUtil(point, goal, emitted, eye.distanceTo(point), turn, tier);
         } else {
            return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NOT_A_SUPPORT, request, face);
         }
      } else {
         return init_U_DataUtil_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NOT_A_SUPPORT, request, null);
      }
   }

   public static Vec3 init_Vec3_AABB_2(AABB rect, Direction face, Vec3 eye, RotationMath.Inner_DataUtil from) {
      return init_Vec3_AABB_2(rect, face, eye, from, 1.0 / 0.0);
   }

   private static Vec3 init_Vec3_AABB_2(AABB rect, Direction face, Vec3 eye, RotationMath.Inner_DataUtil from, double marginCap) {
      if (rect != null && face != null && eye != null) {
         Axis normal = face.getAxis();
         double plane = init_D(rect, face);
         if (init(eye, rect, face) <= 1.0E-4) {
            return null;
         }

         Axis first = init(normal, true);
         Axis second = init(normal, false);
         double margin = Math.min(init(eye.distanceTo(init_Vec3(rect, face))), Math.max(0.03, marginCap));
         double[] windowFirst = init_DArr_AABB(rect, first, margin, true);
         double[] windowSecond = init_DArr_AABB(rect, second, margin, true);
         double travel = -1.0;
         Vec3 look = from == null ? null : init(from);
         if (look != null) {
            double along = init(look, normal);
            travel = Math.abs(along) > 1.0E-4 ? (plane - init(eye, normal)) / along : -1.0;
         }

         double b;
         double a;
         if (travel > 0.0) {
            Vec3 crossing = eye.add(look.scale(travel));
            a = init(crossing, first);
            b = init(crossing, second);
         } else {
            a = init(eye, first);
            b = init(eye, second);
         }

         return init_Vec3_Direction_Axis(normal, plane, Mth.clamp(a, windowFirst[0], windowFirst[1]), Mth.clamp(b, windowSecond[0], windowSecond[1]));
      } else {
         return null;
      }
   }

   public static Vec3 init_Vec3_AABB(AABB rect, Direction face, Vec3 eye, U.Inner_UiHelper tier) {
      return init_Vec3_AABB(rect, face, eye, tier, 1.0 / 0.0);
   }

   public static Vec3 init_Vec3_AABB(AABB rect, Direction face, Vec3 eye, U.Inner_UiHelper tier, double marginCap) {
      if (rect != null && face != null && eye != null) {
         Axis normal = face.getAxis();
         double plane = init_D(rect, face);
         if (init(eye, rect, face) <= 1.0E-4) {
            return null;
         }

         Axis first = init(normal, true);
         Axis second = init(normal, false);
         double margin = Math.min(init(eye.distanceTo(init_Vec3(rect, face))), Math.max(0.03, marginCap));
         boolean centred = tier == U.Inner_UiHelper.CENTRE;
         boolean away = tier == U.Inner_UiHelper.FLATTEST || tier == U.Inner_UiHelper.ESCAPE;
         return init_Vec3_Direction_Axis(
            normal,
            plane,
            init_D_DArr(init_DArr_AABB(rect, first, margin, centred), init(eye, first), first, away),
            init_D_DArr(init_DArr_AABB(rect, second, margin, centred), init(eye, second), second, away)
         );
      } else {
         return null;
      }
   }

   private static double init_D_DArr(double[] window, double at, Axis axis, boolean away) {
      if (away && axis != Axis.Y) {
         return Math.abs(window[0] - at) >= Math.abs(window[1] - at) ? window[0] : window[1];
      } else {
         return Mth.clamp(at, window[0], window[1]);
      }
   }

   public static double init(AABB rect, Axis axis, double margin) {
      double extent = rect.max(axis) - rect.min(axis);
      return Math.min(Math.max(0.0, margin), Math.max(0.0, extent * 0.5 - 1.0E-4));
   }

   public static double[] init_DArr_AABB(AABB rect, Axis axis, double margin, boolean centred) {
      double inset = init(rect, axis, margin);
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

   public static double init(double distance) {
      double band = RotationStreamPlanner.init_D_D(RotationMath.init());
      return Math.max(0.12, Math.abs(distance) * Math.tan(Math.toRadians(band)));
   }

   public static double init(AABB rect, Direction face, Vec3 eye) {
      if (rect != null && face != null && eye != null) {
         double margin = init(eye.distanceTo(init_Vec3(rect, face))) * 2.0;
         if (margin <= 0.0) {
            return 1.0;
         }

         Axis normal = face.getAxis();
         double first = rect.max(init(normal, true)) - rect.min(init(normal, true));
         double second = rect.max(init(normal, false)) - rect.min(init(normal, false));
         return Mth.clamp(Math.min(first, second) / margin, 0.0, 1.0);
      } else {
         return 0.0;
      }
   }

   public static double[] init_DArr_Vec3(Vec3 eye, AABB rect, Direction face, float yaw) {
      return init_DArr_Vec3(eye, rect, face, yaw, 1.0 / 0.0);
   }

   public static double[] init_DArr_Vec3(Vec3 eye, AABB rect, Direction face, float yaw, double marginCap) {
      if (eye != null && rect != null && face != null) {
         double past = init(eye, rect, face);
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

   public static double[] init_DArr_Vec3_2(Vec3 eye, Vec3 leadEye, AABB rect, Direction face, float yaw) {
      return init(eye, leadEye, rect, face, yaw, 1.0 / 0.0);
   }

   private static double[] init(Vec3 eye, Vec3 leadEye, AABB rect, Direction face, float yaw, double marginCap) {
      double[] primary = init_DArr_Vec3(eye, rect, face, yaw, marginCap);
      if (primary == null) {
         return null;
      }

      double low = primary[0];
      double high = primary[1];
      double[] secondary = init_DArr_Vec3(leadEye, rect, face, yaw, marginCap);
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

   public static U.Inner_CachePool init_U_CachePool_U_TaskQueue(
      U.Inner_TaskQueue option, U.Inner_DataUtil aim, U.Inner_StateCache request, U.Inner_JsonUtil[] refusal
   ) {
      init(refusal, U.Inner_JsonUtil.NONE);
      if (option != null && aim != null && request != null) {
         Level level = init_Level();
         Player player = init_Player();
         Direction face = option.init_Direction();
         if (level != null && player != null) {
            Vec3 eye = request.init_Vec3();
            double reach = request.init_D();
            double reachSq = reach * reach;
            boolean sneaking = request.init_Z() || option.init_Z() && request.load_Z();
            RotationMath.Inner_DataUtil cast = request.write() ? aim.load() : aim.init_ce_DataUtil();
            boolean escaped = false;

            for (int rung = 0; rung < 4; rung++) {
               if (request.init_U_StreamUtil() != null && !request.init_U_StreamUtil().init_Z()) {
                  return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.NO_BUDGET, request, face);
               }

               BlockHitResult hit = init_BlockHitResult_Vec3(eye, cast, reach, level, player);
               BlockPos hitPos = hit == null ? null : hit.getBlockPos();
               BlockState hitState = hit == null ? null : level.getBlockState(hitPos);
               boolean builds = hit != null
                  && request.init_U_NetHelper().init(hitState, hitPos, sneaking)
                  && request.init_U_NetHelper().lands(hit, request.init_BlockPos());
               if (!builds) {
                  StringBuilder trace = request.init_StringBuilder();
                  if (trace != null && trace.length() < 160) {
                     trace.append(" [hit=")
                        .append(hitPos)
                        .append(" type=")
                        .append(hit == null ? "MISS" : hit.getType())
                        .append(" face=")
                        .append(hit == null ? null : hit.getDirection())
                        .append(" cell=")
                        .append(request.init_BlockPos())
                        .append(']');
                  }

                  if (escaped) {
                     return init_U_CachePool_U_JsonUtilArr(refusal, hit == null ? U.Inner_JsonUtil.RAY_MISSED : U.Inner_JsonUtil.RAY_WRONG_CELL, request, face);
                  }

                  escaped = true;
                  cast = init_ce_DataUtil_Vec3(
                     init_Vec3_AABB(option.init_AABB(), face, eye, U.Inner_UiHelper.ESCAPE, request.read_D), cast, eye, reachSq, request
                  );
                  if (cast == null) {
                     return init_U_CachePool_U_JsonUtilArr(refusal, hit == null ? U.Inner_JsonUtil.RAY_MISSED : U.Inner_JsonUtil.RAY_WRONG_CELL, request, face);
                  }
               } else {
                  U.Inner_ConfigStore landing = init_U_ConfigStore(hitState, hitPos, hit);
                  if (landing == null) {
                     return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.RAY_MISSED, request, face);
                  }

                  AABB hitRect = landing.init_AABB();
                  Direction hitFace = landing.init_Direction();
                  if (init_Z_AABB_2(hitRect, hitFace, hit.getLocation(), request.init_D_D(eye.distanceTo(hit.getLocation())))) {
                     boolean substituted = !hitPos.equals(option.load_BlockPos()) || hitFace != face;
                     U.Inner_TaskQueue found = substituted ? init(option, hitPos, hitFace, hitState, hitRect, eye, request) : option;
                     U.Inner_DataUtil landed = new U.Inner_DataUtil(
                        hit.getLocation(),
                        cast,
                        cast,
                        eye.distanceTo(hit.getLocation()),
                        init_ce_DataUtil(request) == null ? 0.0F : RotationMath.init_F(init_ce_DataUtil(request), cast),
                        aim.init_U_UiHelper()
                     );
                     return new U.Inner_CachePool(found, landed, hit, substituted, init(found, landed));
                  }

                  cast = init_ce_DataUtil_Vec3(init_Vec3_AABB_2(hitRect, hitFace, eye, cast, request.read_D), cast, eye, reachSq, request);
                  if (cast == null) {
                     return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.RAY_MISSED, request, face);
                  }
               }
            }

            return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.RAY_MISSED, request, face);
         } else {
            return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.RAY_MISSED, request, face);
         }
      } else {
         return init_U_CachePool_U_JsonUtilArr(refusal, U.Inner_JsonUtil.RAY_MISSED, request, null);
      }
   }

   private static RotationMath.Inner_DataUtil init_ce_DataUtil_Vec3(Vec3 point, RotationMath.Inner_DataUtil cast, Vec3 eye, double reachSq, U.Inner_StateCache request) {
      if (point != null && !(eye.distanceToSqr(point) > reachSq)) {
         RotationMath.Inner_DataUtil next = RotationMath.init(point, eye);
         if (Math.abs(next.load()) > request.load_D()) {
            return null;
         }

         if (request.write()) {
            RotationMath.Inner_DataUtil from = init_ce_DataUtil(request);
            next = RotationMath.init_ce_DataUtil(next, from == null ? next : from);
            if (Math.abs(next.load()) > request.load_D()) {
               return null;
            }
         }

         return Float.compare(next.init(), cast.init()) == 0 && Float.compare(next.load(), cast.load()) == 0 ? null : next;
      } else {
         return null;
      }
   }

   private static U.Inner_TaskQueue init(
      U.Inner_TaskQueue option, BlockPos support, Direction face, BlockState state, AABB rect, Vec3 eye, U.Inner_StateCache request
   ) {
      AABB flat = init_AABB(rect, face);
      return new U.Inner_TaskQueue(
         option.init_BlockPos(),
         support.immutable(),
         face,
         support.equals(option.init_BlockPos()) ? 0 : 1,
         state,
         flat,
         load(flat, face),
         init(flat, face, eye),
         init_Z(state, support),
         new U.Inner_BufferHelper(support, face)
      );
   }

   private static double init(U.Inner_TaskQueue option, U.Inner_DataUtil aim) {
      return (option.init_Direction() == Direction.UP ? 4.0 : 0.0) + option.init_D() + option.load_D() - aim.init_F() / 180.0;
   }

   public static U.Inner_CachePool init_U_CachePool(U.Inner_StateCache request) {
      return init(request, (U.Inner_JsonUtil[])null);
   }

   public static U.Inner_CachePool init(U.Inner_StateCache request, U.Inner_JsonUtil[] outcome) {
      init(outcome, U.Inner_JsonUtil.NONE);
      if (request != null && request.init_BlockPos() != null) {
         Level level = init_Level();
         if (level == null) {
            return null;
         }

         U.Inner_SyncManager slot = init_U_SyncManager(request, level);
         List<U.Inner_TaskQueue> found = slot.init_List;
         int size = found.size();
         if (size == 0) {
            init(outcome, U.Inner_JsonUtil.NOT_A_SUPPORT);
            return null;
         }

         if (slot.init_I >= size) {
            slot.init_I = 0;
         }

         U.Inner_JsonUtil[] refusal = new U.Inner_JsonUtil[1];
         U.Inner_JsonUtil last = U.Inner_JsonUtil.NOT_A_SUPPORT;
         int start = slot.init_I;

         for (int step = 0; step < size; step++) {
            int index = start + step;
            if (index >= size) {
               index -= size;
            }

            U.Inner_TaskQueue option = found.get(index);
            U.Inner_DataUtil aim = init(option, request, refusal);
            if (aim == null) {
               last = refusal[0];
               slot.init_I = index + 1 == size ? 0 : index + 1;
            } else {
               U.Inner_CachePool candidate = init_U_CachePool_U_TaskQueue(option, aim, request, refusal);
               if (refusal[0] == U.Inner_JsonUtil.NO_BUDGET) {
                  slot.init_I = index;
                  init(outcome, U.Inner_JsonUtil.NO_BUDGET);
                  return null;
               }

               if (candidate != null) {
                  slot.init_I = index;
                  return candidate;
               }

               slot.init_I = index + 1 == size ? 0 : index + 1;
               last = refusal[0];
            }
         }

         init(outcome, last);
         return null;
      } else {
         return null;
      }
   }

   public static BlockHitResult init_BlockHitResult_U_CachePool(
      U.Inner_CachePool candidate, RotationMath.Inner_DataUtil wire, Vec3 eye, double reach, U.Inner_StateCache request
   ) {
      if (candidate != null && wire != null && eye != null && request != null) {
         if (Math.abs(wire.load()) > 89.5F) {
            return null;
         }

         Level level = init_Level();
         Player player = init_Player();
         if (level != null && player != null) {
            BlockHitResult hit = init_BlockHitResult_Vec3(eye, wire, reach, level, player);
            if (hit == null) {
               return null;
            } else {
               BlockPos hitPos = hit.getBlockPos();
               BlockState hitState = level.getBlockState(hitPos);
               if (!request.init_U_NetHelper().init(hitState, hitPos, request.init_Z())) {
                  return null;
               } else if (!request.init_U_NetHelper().lands(hit, candidate.init_U_TaskQueue().init_BlockPos())) {
                  return null;
               } else {
                  U.Inner_TaskQueue plan = candidate.init_U_TaskQueue();
                  if (hitPos.equals(plan.load_BlockPos())
                     && hit.getDirection() == plan.init_Direction()
                     && init_Z_AABB_2(plan.init_AABB(), plan.init_Direction(), hit.getLocation(), 0.0)) {
                     return hit;
                  } else {
                     return init_AABB(hitState, hitPos, hit) != null ? hit : null;
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

   public static AABB[] init_AABBArr_BlockState(BlockState state, BlockPos support, Direction face, int max) {
      AABB[] boxes = init_AABBArr(state, support);
      if (boxes.length == 0) {
         return init_AABBArr;
      }

      int count = Math.min(boxes.length, 16);
      AABB[] rects = new AABB[count];

      for (int i = 0; i < count; i++) {
         rects[i] = init_AABB(boxes[i], face);
      }

      count = init(rects, count, face);

      for (int i = 1; i < count; i++) {
         AABB key = rects[i];
         double area = load(key, face);

         int j;
         for (j = i - 1; j >= 0 && load(rects[j], face) < area; j--) {
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

   public static AABB init_AABB(BlockState state, BlockPos pos, BlockHitResult hit) {
      U.Inner_ConfigStore landing = init_U_ConfigStore(state, pos, hit);
      return landing == null ? null : landing.init_AABB();
   }

   public static U.Inner_ConfigStore init_U_ConfigStore(BlockState state, BlockPos pos, BlockHitResult hit) {
      if (state != null && pos != null && hit != null) {
         Vec3 point = hit.getLocation();
         Direction reported = hit.getDirection();
         AABB[] boxes = init_AABBArr(state, pos);

         for (AABB box : boxes) {
            if (init_Z_AABB_2(box, reported, point, 0.0)) {
               return new U.Inner_ConfigStore(box, reported);
            }
         }

         for (AABB box : boxes) {
            for (Direction face : init_DirectionArr) {
               if (face != reported && init_Z_AABB_2(box, face, point, 0.0)) {
                  return new U.Inner_ConfigStore(box, face);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static boolean init_Z_AABB_2(AABB rect, Direction face, Vec3 point, double margin) {
      if (rect != null && face != null && point != null) {
         Axis normal = face.getAxis();
         return Math.abs(init(point, normal) - init_D(rect, face)) > 1.0E-4
            ? false
            : init_Z_AABB(rect, init(normal, true), point, margin) && init_Z_AABB(rect, init(normal, false), point, margin);
      } else {
         return false;
      }
   }

   private static boolean init_Z_AABB(AABB rect, Axis axis, Vec3 point, double margin) {
      double inset = load(rect, axis, margin);
      double at = init(point, axis);
      return at >= rect.min(axis) + inset - 1.0E-4 && at <= rect.max(axis) - inset + 1.0E-4;
   }

   private static double load(AABB rect, Axis axis, double margin) {
      double extent = rect.max(axis) - rect.min(axis);
      return Math.max(0.0, Math.min(margin, extent * 0.5 - margin));
   }

   public static double init(Vec3 eye, AABB rect, Direction face) {
      if (eye != null && rect != null && face != null) {
         double plane = init_D(rect, face);
         double along = init(eye, face.getAxis());
         return (along - plane) * (face.getAxisDirection() == AxisDirection.POSITIVE ? 1.0 : -1.0);
      } else {
         return 0.0;
      }
   }

   public static Vec3 init_Vec3(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = init(normal, true);
      Axis second = init(normal, false);
      return init_Vec3_Direction_Axis(normal, init_D(rect, face), (rect.min(first) + rect.max(first)) * 0.5, (rect.min(second) + rect.max(second)) * 0.5);
   }

   public static boolean init_Z_Vec3(Vec3 eye, BlockPos support, Direction face, Level level) {
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

   public static boolean init(BlockState state, BlockPos pos, boolean sneaking) {
      return state != null && pos != null && load(state, pos) ? !sneaking : false;
   }

   public static boolean init_Z(BlockState state, BlockPos pos) {
      return init(state, pos, false) && !init(state, pos, true);
   }

   public static boolean load(BlockState state, BlockPos support, boolean sneaking) {
      if (state != null && support != null && !state.isAir()) {
         return init_AABBArr(state, support).length == 0 ? false : !init(state, support, sneaking);
      } else {
         return false;
      }
   }

   public static boolean init(BlockPos cell, Level level) {
      if (cell != null && level != null) {
         BlockState state = level.getBlockState(cell);
         if (!state.isAir() && state.canBeReplaced()) {
            return load(state, cell) ? false : init_AABBArr(state, cell).length > 0;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public static BlockHitResult init_BlockHitResult_Vec3(Vec3 eye, RotationMath.Inner_DataUtil rotation, double reach, Level level, Entity entity) {
      if (eye != null && rotation != null && level != null) {
         Vec3 end = eye.add(init(rotation).scale(reach));
         HitResult result = level.clip(
            new ClipContext(eye, end, Block.OUTLINE, Fluid.NONE, entity == null ? CollisionContext.empty() : CollisionContext.of(entity))
         );
         return result instanceof BlockHitResult blockHit && result.getType() == Type.BLOCK ? blockHit : null;
      } else {
         return null;
      }
   }

   public static Axis init(Axis normal, boolean first) {
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

   public static Vec3 init_Vec3_Direction_Axis(Axis normal, double plane, double a, double b) {
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

   public static double init(Vec3 point, Axis axis) {
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

   public static void init_V() {
      for (int i = 0; i < 8; i++) {
         init_BlockPosArr[i] = null;
         init_BlockStateArr[i] = null;
         init_AABBArr2[i] = null;
      }

      for (int i = 0; i < 48; i++) {
         if (init_U_SyncManagerArr[i] != null) {
            init_U_SyncManagerArr[i].init_Z = false;
         }
      }
   }

   private static U.Inner_SyncManager init_U_SyncManager(U.Inner_StateCache request, Level level) {
      long key = init_J(request, level);

      for (int i = 0; i < 48; i++) {
         U.Inner_SyncManager slot = init_U_SyncManagerArr[i];
         if (slot != null && slot.init_Z && slot.init_J == key) {
            return slot;
         }
      }

      U.Inner_SyncManager slot = init_U_SyncManagerArr[sync];
      if (slot == null) {
         slot = init_U_SyncManagerArr[sync] = new U.Inner_SyncManager();
      }

      sync = (sync + 1) % 48;
      slot.init_List.clear();
      slot.init_I = 0;
      Predicate<U.Inner_BufferHelper> held = request.init_Predicate();
      request.init_U_StateCache_Predicate(null);
      init(request, slot.init_List);
      request.init_U_StateCache_Predicate(held);
      slot.init_J = key;
      slot.init_Z = true;
      return slot;
   }

   private static long init_J(U.Inner_StateCache request, Level level) {
      long key = init(request.init_BlockPos(), level, new MutableBlockPos());
      key = key * 31L + System.identityHashCode(level);

      for (Direction face : request.init_DirectionArr()) {
         key = key * 31L + face.ordinal();
      }

      EnumSet<Direction> allowed = request.init_EnumSet();
      key = key * 31L + (allowed == null ? -1L : allowed.hashCode());
      key = key * 31L + (request.read() ? 1L : 0L);
      key = key * 31L + request.init_I();
      key = key * 31L + (request.init_Z() ? 2L : 0L);
      return key * 31L + (request.load_Z() ? 4L : 0L);
   }

   private static AABB[] init_AABBArr(BlockState state, BlockPos pos) {
      if (state != null && pos != null) {
         for (int i = 0; i < 8; i++) {
            if (init_BlockStateArr[i] == state && pos.equals(init_BlockPosArr[i])) {
               return init_AABBArr2[i];
            }
         }

         Level level = init_Level();
         if (level == null) {
            return init_AABBArr;
         }

         Player player = init_Player();
         VoxelShape shape = state.getShape(level, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player));
         List<AABB> local = shape.isEmpty() ? List.of() : shape.toAabbs();
         AABB[] boxes = local.isEmpty() ? init_AABBArr : new AABB[local.size()];

         for (int i = 0; i < local.size(); i++) {
            boxes[i] = local.get(i).move(pos);
         }

         init_BlockPosArr[close] = pos.immutable();
         init_BlockStateArr[close] = state;
         init_AABBArr2[close] = boxes;
         close = (close + 1) % 8;
         return boxes;
      } else {
         return init_AABBArr;
      }
   }

   private static int init(AABB[] rects, int count, Direction face) {
      Axis normal = face.getAxis();
      Axis first = init(normal, true);
      Axis second = init(normal, false);
      boolean merged = true;

      while (merged && count > 1) {
         merged = false;

         for (int i = 0; i < count && !merged; i++) {
            for (int j = i + 1; j < count; j++) {
               if (!(Math.abs(init_D(rects[i], face) - init_D(rects[j], face)) > 1.0E-4)) {
                  AABB union = init_AABB_AABB(rects[i], rects[j], first, second);
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

   private static AABB init_AABB_AABB(AABB left, AABB right, Axis first, Axis second) {
      if (init_Z_AABB_3(left, right, first, second)) {
         return left;
      } else if (init_Z_AABB_3(right, left, first, second)) {
         return right;
      } else if (init(left, right, first) && load(left, right, second)) {
         return left.minmax(right);
      } else {
         return init(left, right, second) && load(left, right, first) ? left.minmax(right) : null;
      }
   }

   private static boolean init_Z_AABB_3(AABB outer, AABB inner, Axis first, Axis second) {
      return outer.min(first) - 1.0E-4 <= inner.min(first)
         && outer.max(first) + 1.0E-4 >= inner.max(first)
         && outer.min(second) - 1.0E-4 <= inner.min(second)
         && outer.max(second) + 1.0E-4 >= inner.max(second);
   }

   private static boolean init(AABB left, AABB right, Axis axis) {
      return Math.abs(left.min(axis) - right.min(axis)) <= 1.0E-4 && Math.abs(left.max(axis) - right.max(axis)) <= 1.0E-4;
   }

   private static boolean load(AABB left, AABB right, Axis axis) {
      return left.min(axis) <= right.max(axis) + 1.0E-4 && right.min(axis) <= left.max(axis) + 1.0E-4;
   }

   private static AABB init_AABB(AABB box, Direction face) {
      double plane = init_D(box, face);
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

   private static double init_D(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      return face.getAxisDirection() == AxisDirection.POSITIVE ? rect.max(normal) : rect.min(normal);
   }

   private static double load(AABB rect, Direction face) {
      Axis normal = face.getAxis();
      Axis first = init(normal, true);
      Axis second = init(normal, false);
      return (rect.max(first) - rect.min(first)) * (rect.max(second) - rect.min(second));
   }

   private static boolean load(BlockState state, BlockPos pos) {
      Level level = init_Level();
      if (level != null && state.getMenuProvider(level, pos) != null) {
         return true;
      }

      net.minecraft.world.level.block.Block block = state.getBlock();
      if (block instanceof FenceBlock) {
         return init_Z();
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

   private static boolean init_Z() {
      Player player = init_Player();
      if (player == null) {
         return false;
      }

      int tick = ck.init_ck().state_I();
      if (tick != open) {
         open = tick;
         init_Z = !Leashable.leashableInArea(player, led -> led.getLeashHolder() == player).isEmpty();
      }

      return init_Z;
   }

   private static Vec3 init(RotationMath.Inner_DataUtil rotation) {
      float yaw = -rotation.init() * 0.017453292F;
      float pitch = -rotation.load() * 0.017453292F;
      float cosPitch = Mth.cos(pitch);
      return new Vec3(Mth.sin(yaw) * cosPitch, Mth.sin(pitch), Mth.cos(yaw) * cosPitch);
   }

   private static RotationMath.Inner_DataUtil init_ce_DataUtil(U.Inner_StateCache request) {
      if (request.init_ce_DataUtil() != null) {
         return request.init_ce_DataUtil();
      }

      Player player = init_Player();
      return player == null ? null : new RotationMath.Inner_DataUtil(player.getYRot(), player.getXRot());
   }

   private static Level init_Level() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? null : mc.level;
   }

   private static Player init_Player() {
      Minecraft mc = Minecraft.getInstance();
      return mc == null ? null : mc.player;
   }

   private static void init(U.Inner_JsonUtil[] refusal, U.Inner_JsonUtil value) {
      if (refusal != null && refusal.length > 0) {
         refusal[0] = value;
      }
   }

   private static U.Inner_DataUtil init_U_DataUtil_U_JsonUtilArr(
      U.Inner_JsonUtil[] refusal, U.Inner_JsonUtil reason, U.Inner_StateCache request, Direction face
   ) {
      init(refusal, reason);
      init(request, face, reason);
      return null;
   }

   private static U.Inner_CachePool init_U_CachePool_U_JsonUtilArr(
      U.Inner_JsonUtil[] refusal, U.Inner_JsonUtil reason, U.Inner_StateCache request, Direction face
   ) {
      init(refusal, reason);
      init(request, face, reason);
      return null;
   }

   private static void init(U.Inner_StateCache request, Direction face, U.Inner_JsonUtil reason) {
      if (request != null && face != null) {
         StringBuilder trace = request.init_StringBuilder();
         if (trace != null && trace.length() < 96) {
            if (trace.length() > 0) {
               trace.append(' ');
            }

            trace.append(face.getSerializedName().charAt(0)).append(':').append(reason.init());
         }
      }
   }

   public record Inner_BufferHelper(BlockPos init_BlockPos, Direction init_Direction) {
      public Inner_BufferHelper {
         init_BlockPos = init_BlockPos == null ? null : init_BlockPos.immutable();
      }
   }

   public record Inner_CachePool(
      U.Inner_TaskQueue init_U_TaskQueue, U.Inner_DataUtil init_U_DataUtil, BlockHitResult init_BlockHitResult, boolean init_Z, double init_D
   ) {
   }

   public record Inner_ConfigStore(AABB init_AABB, Direction init_Direction) {
   }

   public record Inner_DataUtil(
      Vec3 init_Vec3, RotationMath.Inner_DataUtil init_ce_DataUtil, RotationMath.Inner_DataUtil load, double init_D, float init_F, U.Inner_UiHelper init_U_UiHelper
   ) {
   }

   public enum Inner_JsonUtil {
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

      private final String init_Str;

      Inner_JsonUtil(String code) {
         this.init_Str = code;
      }

      public String init() {
         return this.init_Str;
      }
   }

   public interface Inner_NetHelper {
      boolean lands(BlockHitResult var1, BlockPos var2);

      default boolean init(BlockState state, BlockPos pos, boolean sneaking) {
         return !U.init(state, pos, sneaking);
      }
   }

   public static final class Inner_StateCache {
      private BlockPos init_BlockPos;
      private final Vec3 init_Vec3;
      private final double init_D;
      private U.Inner_NetHelper init_U_NetHelper;
      private RotationMath.Inner_DataUtil init_ce_DataUtil;
      private double load_D = U.init_D();
      private boolean init_Z;
      private boolean load_Z;
      private Direction[] init_DirectionArr;
      private EnumSet<Direction> init_EnumSet;
      private boolean read_Z;
      private Vec3 load_Vec3;
      private Predicate<U.Inner_BufferHelper> init_Predicate;
      private U.Inner_StreamUtil init_U_StreamUtil;
      private int init_I;
      private boolean write;
      private double read_D;
      private StringBuilder init_StringBuilder;

      public Inner_StateCache(BlockPos cell, Vec3 eye, double reach, U.Inner_NetHelper placement) {
         this.init_DirectionArr = U.init_DirectionArr;
         this.read_Z = true;
         this.init_I = 3;
         this.write = true;
         this.read_D = 1.0 / 0.0;
         this.init_BlockPos = cell == null ? null : cell.immutable();
         this.init_Vec3 = eye;
         this.init_D = reach;
         this.init_U_NetHelper = placement;
      }

      public U.Inner_StateCache init_U_StateCache_BlockPos(BlockPos cell) {
         this.init_BlockPos = cell == null ? null : cell.immutable();
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_U_NetHelper(U.Inner_NetHelper placement) {
         this.init_U_NetHelper = placement;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_ce_DataUtil(RotationMath.Inner_DataUtil from) {
         this.init_ce_DataUtil = from;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_D(double degrees) {
         this.load_D = degrees;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_Z(boolean sneaking) {
         this.init_Z = sneaking;
         return this;
      }

      public U.Inner_StateCache load(boolean allowed) {
         this.load_Z = allowed;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_DirectionArr(Direction[] order) {
         this.init_DirectionArr = order != null && order.length != 0 ? order : U.init_DirectionArr;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_EnumSet(EnumSet<Direction> faces) {
         this.init_EnumSet = faces;
         return this;
      }

      public U.Inner_StateCache read(boolean allow) {
         this.read_Z = allow;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_Vec3(Vec3 leadEye) {
         this.load_Vec3 = leadEye;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_Predicate(Predicate<U.Inner_BufferHelper> banned) {
         this.init_Predicate = banned;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_U_StreamUtil(U.Inner_StreamUtil budget) {
         this.init_U_StreamUtil = budget;
         return this;
      }

      public U.Inner_StateCache init_U_StateCache_I(int max) {
         this.init_I = Math.max(1, max);
         return this;
      }

      public U.Inner_StateCache write(boolean quantize) {
         this.write = quantize;
         return this;
      }

      public U.Inner_StateCache load(double cap) {
         this.read_D = Double.isNaN(cap) ? 1.0 / 0.0 : Math.max(0.0, cap);
         return this;
      }

      public double init_D_D(double distance) {
         return Math.min(U.init(distance), this.read_D);
      }

      public U.Inner_StateCache init_U_StateCache_StringBuilder(StringBuilder trace) {
         this.init_StringBuilder = trace;
         return this;
      }

      public BlockPos init_BlockPos() {
         return this.init_BlockPos;
      }

      public Vec3 init_Vec3() {
         return this.init_Vec3;
      }

      public double init_D() {
         return this.init_D;
      }

      public U.Inner_NetHelper init_U_NetHelper() {
         return this.init_U_NetHelper;
      }

      public RotationMath.Inner_DataUtil init_ce_DataUtil() {
         return this.init_ce_DataUtil;
      }

      public double load_D() {
         return this.load_D;
      }

      public boolean init_Z() {
         return this.init_Z;
      }

      public boolean load_Z() {
         return this.load_Z;
      }

      public Direction[] init_DirectionArr() {
         return this.init_DirectionArr;
      }

      public EnumSet<Direction> init_EnumSet() {
         return this.init_EnumSet;
      }

      public boolean read() {
         return this.read_Z;
      }

      public Vec3 load_Vec3() {
         return this.load_Vec3;
      }

      public Predicate<U.Inner_BufferHelper> init_Predicate() {
         return this.init_Predicate;
      }

      public U.Inner_StreamUtil init_U_StreamUtil() {
         return this.init_U_StreamUtil;
      }

      public int init_I() {
         return this.init_I;
      }

      public boolean write() {
         return this.write;
      }

      public StringBuilder init_StringBuilder() {
         return this.init_StringBuilder;
      }
   }

   public static final class Inner_StreamUtil {
      private int init;

      public Inner_StreamUtil(int rays) {
         this.init = Math.max(0, rays);
      }

      public int init_I() {
         return this.init;
      }

      public boolean init_Z() {
         if (this.init <= 0) {
            return false;
         }

         this.init--;
         return true;
      }

      public void init(int rays) {
         this.init = Math.max(0, rays);
      }
   }

   private static final class Inner_SyncManager {
      private final List<U.Inner_TaskQueue> init_List = new ArrayList<>();
      private long init_J;
      private boolean init_Z;
      private int init_I;
   }

   public record Inner_TaskQueue(
      BlockPos init_BlockPos,
      BlockPos load_BlockPos,
      Direction init_Direction,
      int init_I,
      BlockState init_BlockState,
      AABB init_AABB,
      double init_D,
      double load_D,
      boolean init_Z,
      U.Inner_BufferHelper init_U_BufferHelper
   ) {
   }

   public enum Inner_UiHelper {
      CENTRE,
      NEAREST,
      FLATTEST,
      ESCAPE;
   }
}
