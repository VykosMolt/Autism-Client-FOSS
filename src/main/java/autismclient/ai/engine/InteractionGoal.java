package autismclient.ai.engine;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class InteractionGoal implements AiPathfinder.PathGoal {
   private final BlockPos init_BlockPos;
   private final double init_D;
   private final double load;
   private final AiPathfinder.GridView init_ClassCache_StateCa;
   private final Direction init_Direction;
   private final boolean init_Z;
   private final Set<Long> init_Set;

   public InteractionGoal(BlockPos target, double reach, double eyeHeight) {
      this(target, reach, eyeHeight, null);
   }

   public InteractionGoal(BlockPos target, double reach, double eyeHeight, Direction requiredFace) {
      this(target, reach, eyeHeight, null, ConcurrentHashMap.newKeySet(), requiredFace, false);
   }

   private InteractionGoal(BlockPos target, double reach, double eyeHeight, AiPathfinder.GridView grid, Set<Long> rejected, Direction requiredFace, boolean safeRemoval) {
      if (Double.isFinite(reach) && !(reach <= 0.0) && Double.isFinite(eyeHeight)) {
         this.init_BlockPos = target.immutable();
         this.init_D = reach;
         this.load = eyeHeight;
         this.init_ClassCache_StateCa = grid;
         this.init_Set = rejected;
         this.init_Direction = requiredFace;
         this.init_Z = safeRemoval;
      } else {
         throw new IllegalArgumentException("Invalid interaction reach");
      }
   }

   public InteractionGoal forSnapshotGrids() {
      return new InteractionGoal(this.init_BlockPos, this.init_D, this.load, this.init_ClassCache_StateCa, this.init_Set, this.init_Direction, true);
   }

   public boolean breaksTarget() {
      return this.init_Z;
   }

   public BlockPos getTarget() {
      return this.init_BlockPos;
   }

   public double getReach() {
      return this.init_D;
   }

   public Direction getFace() {
      return this.init_Direction;
   }

   public void rejectStance(BlockPos feet) {
      this.init_Set.add(feet.asLong());
   }

   public int rejectedStanceCount() {
      return this.init_Set.size();
   }

   @Override
   public boolean load(BlockPos cell) {
      return !this.init_BlockPos.equals(cell);
   }

   @Override
   public AiPathfinder.PathGoal init(AiPathfinder.GridView grid) {
      return new InteractionGoal(this.init_BlockPos, this.init_D, this.load, grid, this.init_Set, this.init_Direction, this.init_Z);
   }

   @Override
   public BlockPos getTargetBlock() {
      return this.init_BlockPos;
   }

   @Override
   public boolean outsideSearchBox(BlockPos from, int horizontal, int vertical) {
      return Math.abs(from.getX() - this.init_BlockPos.getX()) > horizontal + this.init_D
         || Math.abs(from.getZ() - this.init_BlockPos.getZ()) > horizontal + this.init_D
         || Math.abs(from.getY() - this.init_BlockPos.getY()) > vertical + this.init_D + this.load;
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double horizontal = Math.hypot(x - this.init_BlockPos.getX(), z - this.init_BlockPos.getZ());
      double vertical = Math.abs(y + this.load - this.init_BlockPos.getY() - 0.5);
      return Math.max(0.0, Math.max(horizontal * 2.9000000000000004, vertical) - this.init_D * 2.9000000000000004) + 0.5 * horizontal;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.isInGoalWithGrid(x, y, z, this.init_ClassCache_StateCa);
   }

   @Override
   public boolean isInGoalWithGrid(int x, int y, int z, AiPathfinder.GridView planned) {
      if (this.init_Direction == null
         && x == this.init_BlockPos.getX()
         && z == this.init_BlockPos.getZ()
         && this.init_BlockPos.getY() >= y - 1
         && this.init_BlockPos.getY() <= y + 1) {
         return false;
      }

      if (!this.init_Set.isEmpty() && this.init_Set.contains(BlockPos.asLong(x, y, z))) {
         return false;
      }

      double cx = this.init_BlockPos.getX() + 0.5;
      double cy = this.init_BlockPos.getY() + 0.5;
      double cz = this.init_BlockPos.getZ() + 0.5;
      double ex = cx - x - 0.5;
      double ey = cy - y - this.load;
      double ez = cz - z - 0.5;
      if (ex * ex + ey * ey + ez * ez > (this.init_D + 0.5) * (this.init_D + 0.5)) {
         return false;
      }

      if (planned == null) {
         return false;
      }

      List<AABB> boxes = AiShapes.boxes(planned.collisionShapeCode(this.init_BlockPos.getX(), this.init_BlockPos.getY(), this.init_BlockPos.getZ()));
      if (boxes.isEmpty()) {
         boxes = List.of(new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
      }

      for (AABB box : boxes) {
         Vec3 centre = box.getCenter().add(this.init_BlockPos.getX(), this.init_BlockPos.getY(), this.init_BlockPos.getZ());
         if (this.init_Direction != null) {
            for (Vec3 point : AiBlockInteraction.sampleFacePoints(box, this.init_BlockPos, this.init_Direction)) {
               if (init(planned, this.init_BlockPos, x, y, z, this.load, point.x, point.y, point.z, this.init_D, this.init_Direction, null)) {
                  return true;
               }
            }
         } else {
            if (load(planned, this.init_BlockPos, x, y, z, this.load, centre.x, centre.y, centre.z, this.init_D)) {
               return true;
            }

            for (Direction face : Direction.values()) {
               Vec3 point = centre.add(
                  face.getStepX() * (box.maxX - box.minX) * 0.49,
                  face.getStepY() * (box.maxY - box.minY) * 0.49,
                  face.getStepZ() * (box.maxZ - box.minZ) * 0.49
               );
               if (load(planned, this.init_BlockPos, x, y, z, this.load, point.x, point.y, point.z, this.init_D)) {
                  return true;
               }
            }

            for (Direction face : Direction.values()) {
               for (Vec3 point : AiBlockInteraction.sampleFacePoints(box, this.init_BlockPos, face)) {
                  if (load(planned, this.init_BlockPos, x, y, z, this.load, point.x, point.y, point.z, this.init_D)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   static boolean init(
      AiPathfinder.GridView grid, BlockPos target, int x, int y, int z, double eyeHeight, double targetX, double targetY, double targetZ, double reach
   ) {
      double dx = targetX - x - 0.5;
      double dy = targetY - y - eyeHeight;
      double dz = targetZ - z - 0.5;
      if (!(dx * dx + dy * dy + dz * dz > reach * reach) && grid != null) {
         int steps = (int)Math.ceil(Math.sqrt(dx * dx + dy * dy + dz * dz) * 8.0);
         long previous = -9223372036854775808L;

         for (int i = 1; i < steps; i++) {
            double t = (double)i / steps;
            int bx = (int)Math.floor(x + 0.5 + dx * t);
            int by = (int)Math.floor(y + eyeHeight + dy * t);
            int bz = (int)Math.floor(z + 0.5 + dz * t);
            long key = BlockPos.asLong(bx, by, bz);
            if (key != previous) {
               previous = key;
               if (key == target.asLong()) {
                  return true;
               }

               if (bx != x || bz != z || by != y && by != y + 1) {
                  AiPathfinder.CellInfo p = grid.probeCell(bx, by, bz);
                  if (p == null || !p.loaded() || !p.passable() || p.lava()) {
                     return false;
                  }
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   static boolean load(
      AiPathfinder.GridView grid, BlockPos target, int x, int y, int z, double eyeHeight, double targetX, double targetY, double targetZ, double reach
   ) {
      return init(grid, target, x, y, z, eyeHeight, targetX, targetY, targetZ, reach, null, null);
   }

   static boolean init(
      AiPathfinder.GridView grid,
      BlockPos target,
      int x,
      int y,
      int z,
      double eyeHeight,
      double targetX,
      double targetY,
      double targetZ,
      double reach,
      Direction requiredFace,
      Boolean highHalf
   ) {
      if (grid == null) {
         return false;
      }

      Vec3 from = new Vec3(x + 0.5, y + eyeHeight, z + 0.5);
      Vec3 to = new Vec3(targetX, targetY, targetZ);
      Vec3 delta = to.subtract(from);
      if (delta.lengthSqr() > reach * reach) {
         return false;
      }

      int bx = (int)Math.floor(from.x);
      int by = (int)Math.floor(from.y);
      int bz = (int)Math.floor(from.z);
      int sx = delta.x > 0.0 ? 1 : (delta.x < 0.0 ? -1 : 0);
      int sy = delta.y > 0.0 ? 1 : (delta.y < 0.0 ? -1 : 0);
      int sz = delta.z > 0.0 ? 1 : (delta.z < 0.0 ? -1 : 0);
      double tx = sx == 0 ? 1.0 / 0.0 : ((sx > 0 ? bx + 1 : bx) - from.x) / delta.x;
      double ty = sy == 0 ? 1.0 / 0.0 : ((sy > 0 ? by + 1 : by) - from.y) / delta.y;
      double tz = sz == 0 ? 1.0 / 0.0 : ((sz > 0 ? bz + 1 : bz) - from.z) / delta.z;
      double ix = sx == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.x);
      double iy = sy == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.y);
      double iz = sz == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.z);

      for (int cells = 0; cells < 64; cells++) {
         if (bx != target.getX() || by - 1 != target.getY() || bz != target.getZ()) {
            short below = grid.collisionShapeCode(bx, by - 1, bz);
            if (AiShapes.topOf(below) > 1.0) {
               for (AABB box : AiShapes.boxes(below)) {
                  if (box.move(bx, by - 1, bz).clip(from, to).isPresent()) {
                     return false;
                  }
               }
            }
         }

         if (bx == target.getX() && by == target.getY() && bz == target.getZ()) {
            if (requiredFace == null) {
               return true;
            }

            Vec3 first = null;
            Direction firstFace = null;
            double distance = 1.7976931348623157E308;

            for (AABB local : AiShapes.boxes(grid.collisionShapeCode(bx, by, bz))) {
               AABB box = local.move(bx, by, bz);
               Optional<Vec3> entry = box.clip(from, to);
               if (!entry.isEmpty() && !(from.distanceToSqr(entry.get()) >= distance)) {
                  first = entry.get();
                  distance = from.distanceToSqr(first);
                  firstFace = Math.abs(first.x - box.minX) < 1.0E-6
                     ? Direction.WEST
                     : (
                        Math.abs(first.x - box.maxX) < 1.0E-6
                           ? Direction.EAST
                           : (
                              Math.abs(first.y - box.minY) < 1.0E-6
                                 ? Direction.DOWN
                                 : (
                                    Math.abs(first.y - box.maxY) < 1.0E-6
                                       ? Direction.UP
                                       : (Math.abs(first.z - box.minZ) < 1.0E-6 ? Direction.NORTH : Direction.SOUTH)
                                 )
                           )
                     );
               }
            }

            return first != null && firstFace == requiredFace && (highHalf == null || !requiredFace.getAxis().isHorizontal() || highHalf == first.y - by > 0.5);
         }

         AiPathfinder.CellInfo probe = grid.probeCell(bx, by, bz);
         if (probe == null || !probe.loaded() || probe.lava()) {
            return false;
         }

         for (AABB box : AiShapes.boxes(grid.collisionShapeCode(bx, by, bz))) {
            if (box.move(bx, by, bz).clip(from, to).isPresent()) {
               return false;
            }
         }

         double next = Math.min(tx, Math.min(ty, tz));
         if (next > 1.0) {
            return true;
         }

         if (tx <= next + 1.0E-10) {
            bx += sx;
            tx += ix;
         }

         if (ty <= next + 1.0E-10) {
            by += sy;
            ty += iy;
         }

         if (tz <= next + 1.0E-10) {
            bz += sz;
            tz += iz;
         }
      }

      return false;
   }
}
