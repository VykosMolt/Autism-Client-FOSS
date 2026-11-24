package autismclient.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.phys.Vec3;

final class RefQueue {
   static final int init_I = 0;
   static final int load_I = 1;
   static final int read = 2;
   static final int write = 3;
   static final int update = 5;
   private static final Direction[] init_DirectionArr = Direction.values();
   private static final BitSet init_BitSet = new BitSet();
   private static final RefQueue.Inner_StreamUtil init_RefQueue_StreamUti = new RefQueue.Inner_StreamUtil(1, 1.0 / 0.0, -1);
   private static final RefQueue.Inner_StreamUtil load_RefQueue_StreamUti = new RefQueue.Inner_StreamUtil(0, 0.0, -1);
   private static final double[] init_DArr = new double[]{1.62, 1.27};
   private static final int[] init_IArr = new int[]{0, 1, -1};

   static RefQueue.Inner_NetHelper init_RefQueue_NetHelper_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, Vec3 observer, double reach) {
      return init_RefQueue_NetHelper_RefQueue_ConfigSto(g, observer, reach, init_RefQueue_CachePool(g));
   }

   static RefQueue.Inner_NetHelper init_RefQueue_NetHelper_RefQueue_ConfigSto(
      RefQueue.Inner_ConfigStore g, Vec3 observer, double reach, RefQueue.Inner_CachePool cover
   ) {
      double[] distance = new double[g.write];
      Arrays.fill(distance, 1.0 / 0.0);
      int[] parent = new int[g.write];
      Arrays.fill(parent, -1);

      record Inner_DataUtil(int init_I, double init_D) {
      }

      PriorityQueue<Inner_DataUtil> queue = new PriorityQueue<>(Comparator.comparingDouble(Inner_DataUtil::init_D).thenComparingInt(Inner_DataUtil::init_I));
      int own = g.init(BlockPos.containing(observer));
      int seed = -1;
      double nearest = 1.0 / 0.0;
      if (g.init(own, false)) {
         seed = own;
      } else {
         for (int i = 0; i < g.write; i++) {
            if (g.init(i, false) && (g.init_I(i) == 0 || g.init_I(i) == g.init_I - 1 || g.read_I(i) == 0 || g.read_I(i) == g.read - 1)) {
               double d = g.init_BlockPos(i).distToCenterSqr(observer);
               if (d < nearest) {
                  nearest = d;
                  seed = i;
               }
            }
         }
      }

      if (seed >= 0) {
         distance[seed] = 0.0;
         queue.add(new Inner_DataUtil(seed, 0.0));
      }

      List<Vec3> targets = new ArrayList<>();

      for (int bed : g.init_List) {
         targets.addAll(init(g.init_BlockPos(bed)));
      }

      double best = 1.0 / 0.0;
      RefQueue.Inner_TaskQueue winner = null;
      Vec3 winnerEye = null;
      Vec3 winnerAim = null;

      while (!queue.isEmpty()) {
         Inner_DataUtil visit = queue.remove();
         int i = visit.init_I;
         if (visit.init_D == distance[i] && !(visit.init_D >= best)) {
            BlockPos feet = g.init_BlockPos(i);

            for (double height : init_DArr) {
               Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + height, feet.getZ() + 0.5);

               for (Vec3 target : targets) {
                  if (!(eye.distanceToSqr(target) > reach * reach)) {
                     List<Integer> ray = init_List_RefQueue_ConfigSto(g, eye, target);
                     if (ray != null) {
                        double cost = visit.init_D;

                        for (int block : ray) {
                           if (block != i && block != g.init(i, Direction.UP)) {
                              cost += g.init_RefQueue_StreamUti(block).init_D + 5.0;
                           }
                        }

                        if (!(cost >= best)) {
                           RefQueue.Inner_TaskQueue proof = init_RefQueue_TaskQueue_RefQueue_ConfigSto(g, parent, i, reach);
                           if (proof != null) {
                              List<Integer> remaining = init_List_RefQueue_ConfigSto(g, eye, target, -1, proof.init_BitSet);
                              if (remaining != null) {
                                 for (int block : remaining) {
                                    if (!proof.init_BitSet.get(block)) {
                                       proof.init_BitSet.set(block);
                                       proof.init_LinkedHashSet.add(g.init_BlockPos(block));
                                    }
                                 }

                                 if (load(g, i, proof.init_BitSet)) {
                                    double actual = proof.init_LinkedHashSet.stream().mapToDouble(p -> g.init_RefQueue_StreamUti(g.init(p)).init_D).sum()
                                       + Math.max(0, proof.init_LinkedHashSet.size() - 1) * 5;
                                    double score = actual + (proof.init_LinkedHashSet.isEmpty() ? 0 : 5) + proof.init_List.size() * 0.001;
                                    if (score < best) {
                                       best = score;
                                       winner = proof;
                                       winnerEye = eye;
                                       winnerAim = target;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }

            for (Direction d : Plane.HORIZONTAL) {
               for (int dy : init_IArr) {
                  int n = g.init(g.init_I(i) + d.getStepX(), g.load_I(i) + dy, g.read_I(i) + d.getStepZ());
                  if (g.init(n, true) && g.init_RefQueue_StreamUti(n).init_I != 3 && g.init_RefQueue_StreamUti(g.init(n, Direction.UP)).init_I != 3) {
                     int extra = init_I_RefQueue_ConfigSto(g, i, n);
                     if (extra < 0 || g.read_Z(extra)) {
                        double cost = visit.init_D + 0.001;

                        for (int block : new int[]{n, g.init(n, Direction.UP), extra}) {
                           if (block >= 0 && block != i && block != g.init(i, Direction.UP) && g.init_RefQueue_StreamUti(block).init_I == 2) {
                              cost += g.init_RefQueue_StreamUti(block).init_D + 5.0;
                           }
                        }

                        if (cost < distance[n]) {
                           distance[n] = cost;
                           parent[n] = i;
                           queue.add(new Inner_DataUtil(n, cost));
                        }
                     }
                  }
               }
            }
         }
      }

      if (winner == null) {
         return new RefQueue.Inner_NetHelper(cover.init_Set, List.of(), List.of(), 1.0 / 0.0, false, cover.init_I, null, null);
      }

      double ticks = winner.init_LinkedHashSet.stream().mapToDouble(p -> g.init_RefQueue_StreamUti(g.init(p)).init_D).sum()
         + Math.max(0, winner.init_LinkedHashSet.size() - 1) * 5;
      return new RefQueue.Inner_NetHelper(
         cover.init_Set, List.copyOf(winner.init_LinkedHashSet), List.copyOf(winner.init_List), ticks, true, cover.init_I, winnerEye, winnerAim
      );
   }

   private static int init_I_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, int from, int to) {
      return g.load_I(to) > g.load_I(from)
         ? g.init(g.init_I(from), g.load_I(from) + 2, g.read_I(from))
         : (g.load_I(to) < g.load_I(from) ? g.init(g.init_I(to), g.load_I(from) + 1, g.read_I(to)) : -1);
   }

   private static boolean init_Z_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, int i, BitSet removed) {
      return i >= 0 && (g.init_RefQueue_StreamUti(i).init_I == 0 || removed.get(i));
   }

   private static boolean load(RefQueue.Inner_ConfigStore g, int i, BitSet removed) {
      int floor = g.init(i, Direction.DOWN);
      return init_Z_RefQueue_ConfigSto(g, i, removed)
         && init_Z_RefQueue_ConfigSto(g, g.init(i, Direction.UP), removed)
         && floor >= 0
         && !removed.get(floor)
         && g.init_RefQueue_StreamUti(floor).init_I != 0
         && g.init_RefQueue_StreamUti(floor).init_I != 3;
   }

   private static RefQueue.Inner_TaskQueue init_RefQueue_TaskQueue_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, int[] parent, int finish, double reach) {
      ArrayList<Integer> route = new ArrayList<>();
      int i = finish;

      while (i >= 0) {
         route.add(i);
         i = parent[i];
      }

      Collections.reverse(route);
      RefQueue.Inner_TaskQueue proof = new RefQueue.Inner_TaskQueue(new BitSet(g.write), new LinkedHashSet<>(), new ArrayList<>());

      for (int at = 0; at < route.size(); at++) {
         int ix = route.get(at);
         if (at > 0) {
            int previous = route.get(at - 1);
            ArrayList<Integer> pending = new ArrayList<>();

            for (int block : new int[]{init_I_RefQueue_ConfigSto(g, previous, ix), g.init(ix, Direction.UP), ix}) {
               if (block >= 0 && g.init_RefQueue_StreamUti(block).init_I == 2 && !proof.init_BitSet.get(block) && !pending.contains(block)) {
                  pending.add(block);
               }
            }

            while (!pending.isEmpty()) {
               int selected = -1;

               for (int block : pending) {
                  if (init_Z_RefQueue_ConfigSto(g, previous, block, reach, proof.init_BitSet)) {
                     selected = block;
                     break;
                  }
               }

               if (selected < 0) {
                  return null;
               }

               proof.init_BitSet.set(selected);
               proof.init_LinkedHashSet.add(g.init_BlockPos(selected));
               pending.remove(Integer.valueOf(selected));
            }
         }

         if (!load(g, ix, proof.init_BitSet)) {
            return null;
         }

         proof.init_List.add(g.init_BlockPos(ix));
      }

      return proof;
   }

   private static boolean init_Z_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, int stance, int block, double reach, BitSet removed) {
      BlockPos feet = g.init_BlockPos(stance);
      BlockPos target = g.init_BlockPos(block);

      for (double height : init_DArr) {
         Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + height, feet.getZ() + 0.5);

         for (Vec3 point : init(target)) {
            if (!(eye.distanceToSqr(point) > reach * reach)) {
               List<Integer> hit = init_List_RefQueue_ConfigSto(g, eye, point, block, removed);
               if (hit != null && hit.size() == 1 && hit.getFirst() == block) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static List<Vec3> init(BlockPos p) {
      return List.of(
         new Vec3(p.getX() + 0.5, p.getY() + 0.52, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.08, p.getY() + 0.4, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.92, p.getY() + 0.4, p.getZ() + 0.5),
         new Vec3(p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.08),
         new Vec3(p.getX() + 0.5, p.getY() + 0.4, p.getZ() + 0.92)
      );
   }

   static List<Integer> init_List_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, Vec3 start, Vec3 end) {
      return init_List_RefQueue_ConfigSto(g, start, end, -1, init_BitSet);
   }

   private static List<Integer> init_List_RefQueue_ConfigSto(RefQueue.Inner_ConfigStore g, Vec3 start, Vec3 end, int goal, BitSet removed) {
      Vec3 delta = end.subtract(start);
      int x = (int)Math.floor(start.x);
      int y = (int)Math.floor(start.y);
      int z = (int)Math.floor(start.z);
      int dx = Double.compare(delta.x, 0.0);
      int dy = Double.compare(delta.y, 0.0);
      int dz = Double.compare(delta.z, 0.0);
      double tx = init_D_D(start.x, delta.x, x, dx);
      double ty = init_D_D(start.y, delta.y, y, dy);
      double tz = init_D_D(start.z, delta.z, z, dz);
      double ix = dx == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.x);
      double iy = dy == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.y);
      double iz = dz == 0 ? 1.0 / 0.0 : Math.abs(1.0 / delta.z);
      ArrayList<Integer> result = new ArrayList<>();

      for (int step = 0; step < 80; step++) {
         int i = g.init(x - g.init_BlockPos.getX(), y - g.init_BlockPos.getY(), z - g.init_BlockPos.getZ());
         RefQueue.Inner_StreamUtil cell = g.init_RefQueue_StreamUti(i);
         if (cell.init_I == 1) {
            return null;
         }

         if (cell.init_I == 3) {
            return goal < 0 ? result : null;
         }

         if (cell.init_I == 2 && !removed.get(i) && !result.contains(i)) {
            result.add(i);
         }

         if (i == goal) {
            return result;
         }

         double t = Math.min(tx, Math.min(ty, tz));
         if (t > 1.0) {
            return null;
         }

         boolean crossX = tx <= t + 1.0E-10;
         boolean crossY = ty <= t + 1.0E-10;
         boolean crossZ = tz <= t + 1.0E-10;
         int mask = (crossX ? 1 : 0) | (crossY ? 2 : 0) | (crossZ ? 4 : 0);
         if (Integer.bitCount(mask) > 1) {
            for (int subset = 1; subset < 8; subset++) {
               if ((subset & mask) == subset && subset != mask) {
                  int side = g.init(
                     x + ((subset & 1) != 0 ? dx : 0) - g.init_BlockPos.getX(),
                     y + ((subset & 2) != 0 ? dy : 0) - g.init_BlockPos.getY(),
                     z + ((subset & 4) != 0 ? dz : 0) - g.init_BlockPos.getZ()
                  );
                  RefQueue.Inner_StreamUtil edge = g.init_RefQueue_StreamUti(side);
                  if (edge.init_I == 1) {
                     return null;
                  }

                  if (edge.init_I == 2 && !removed.get(side) && !result.contains(side)) {
                     result.add(side);
                  }
               }
            }
         }

         if (tx <= t + 1.0E-10) {
            x += dx;
            tx += ix;
         }

         if (ty <= t + 1.0E-10) {
            y += dy;
            ty += iy;
         }

         if (tz <= t + 1.0E-10) {
            z += dz;
            tz += iz;
         }
      }

      return null;
   }

   private static double init_D_D(double start, double delta, int cell, int sign) {
      return sign == 0 ? 1.0 / 0.0 : ((sign > 0 ? cell + 1 : cell) - start) / delta;
   }

   static Set<BlockPos> init_Set(RefQueue.Inner_ConfigStore g) {
      return init_RefQueue_CachePool(g).init_Set;
   }

   static RefQueue.Inner_CachePool init_RefQueue_CachePool(RefQueue.Inner_ConfigStore g) {
      BitSet removed = new BitSet(g.write);
      Set<BlockPos> result = new LinkedHashSet<>();
      int layers = 0;
      int infinity = 10000;

      for (int layer = 0; layer < 16; layer++) {
         RefQueue.Inner_BufferHelper flow = new RefQueue.Inner_BufferHelper(g.write * 2 + 2);
         int source = g.write * 2;
         int sink = source + 1;

         for (int i = 0; i < g.write; i++) {
            if (g.init_RefQueue_StreamUti(i).init_I != 1) {
               flow.init_V(i * 2, i * 2 + 1, g.init_RefQueue_StreamUti(i).init_I == 2 && !removed.get(i) ? 1 : 10000);
               if (g.init_RefQueue_StreamUti(i).init_I == 3) {
                  flow.init_V(source, i * 2, 10000);
               }

               if (g.init_Z(i)) {
                  flow.init_V(i * 2 + 1, sink, 10000);
               }

               for (Direction d : init_DirectionArr) {
                  int n = g.init(i, d);
                  if (n >= 0 && g.init_RefQueue_StreamUti(n).init_I != 1) {
                     flow.init_V(i * 2 + 1, n * 2, 10000);
                  }
               }
            }
         }

         int cut = flow.init_I(source, sink, 10000);
         if (cut == 0 || cut >= 10000) {
            break;
         }

         boolean[] reachable = flow.init(source);
         int added = 0;

         for (int i = 0; i < g.write; i++) {
            if (g.init_RefQueue_StreamUti(i).init_I == 2 && reachable[i * 2] && !reachable[i * 2 + 1] && !removed.get(i)) {
               removed.set(i);
               result.add(g.init_BlockPos(i));
               added++;
            }
         }

         if (added == 0) {
            break;
         }

         layers++;
      }

      return new RefQueue.Inner_CachePool(Collections.unmodifiableSet(result), layers);
   }

   private static final class Inner_BufferHelper {
      final int[] init_IArr;
      final int[] load;
      final int[] read;
      final int[] write;
      int[] update;
      int[] sync;
      int[] flush;
      int init_I;

      Inner_BufferHelper(int size) {
         this.init_IArr = new int[size];
         Arrays.fill(this.init_IArr, -1);
         this.load = new int[size];
         this.read = new int[size];
         this.write = new int[size];
         this.update = new int[size * 8];
         this.sync = new int[this.update.length];
         this.flush = new int[this.update.length];
      }

      void init_V(int a, int b, int cap) {
         if (this.init_I + 2 > this.update.length) {
            int size = this.update.length * 2;
            this.update = Arrays.copyOf(this.update, size);
            this.sync = Arrays.copyOf(this.sync, size);
            this.flush = Arrays.copyOf(this.flush, size);
         }

         this.load_V(a, b, cap);
         this.load_V(b, a, 0);
      }

      void load_V(int a, int b, int cap) {
         this.update[this.init_I] = b;
         this.flush[this.init_I] = cap;
         this.sync[this.init_I] = this.init_IArr[a];
         this.init_IArr[a] = this.init_I++;
      }

      boolean[] init(int source) {
         boolean[] seen = new boolean[this.init_IArr.length];
         int read = 0;
         int write = 0;
         this.write[write++] = source;
         seen[source] = true;

         while (read < write) {
            for (int e = this.init_IArr[this.write[read++]]; e >= 0; e = this.sync[e]) {
               if (this.flush[e] > 0 && !seen[this.update[e]]) {
                  seen[this.update[e]] = true;
                  this.write[write++] = this.update[e];
               }
            }
         }

         return seen;
      }

      int init_I(int source, int sink, int limit) {
         int total = 0;

         while (total < limit) {
            Arrays.fill(this.load, -1);
            int read = 0;
            int write = 0;
            this.write[write++] = source;
            this.load[source] = 0;

            while (read < write) {
               int at = this.write[read++];

               for (int e = this.init_IArr[at]; e >= 0; e = this.sync[e]) {
                  if (this.flush[e] > 0 && this.load[this.update[e]] < 0) {
                     this.load[this.update[e]] = this.load[at] + 1;
                     this.write[write++] = this.update[e];
                  }
               }
            }

            if (this.load[sink] < 0) {
               break;
            }

            System.arraycopy(this.init_IArr, 0, this.read, 0, this.init_IArr.length);

            int sent;
            while (total < limit && (sent = this.load_I(source, sink, limit - total)) > 0) {
               total += sent;
            }
         }

         return total;
      }

      int load_I(int at, int sink, int amount) {
         if (at == sink) {
            return amount;
         }

         for (int e = this.read[at]; e >= 0; this.read[at] = e = this.sync[e]) {
            if (this.flush[e] > 0 && this.load[this.update[e]] == this.load[at] + 1) {
               int sent = this.load_I(this.update[e], sink, Math.min(amount, this.flush[e]));
               if (sent > 0) {
                  this.flush[e] = this.flush[e] - sent;
                  this.flush[e ^ 1] = this.flush[e ^ 1] + sent;
                  return sent;
               }
            }
         }

         return 0;
      }
   }

   record Inner_CachePool(Set<BlockPos> init_Set, int init_I) {
   }

   static final class Inner_ConfigStore {
      final BlockPos init_BlockPos;
      final int init_I;
      final int load;
      final int read;
      final int write;
      final RefQueue.Inner_StreamUtil[] init_RefQueue_StreamUtiArr;
      final List<Integer> init_List = new ArrayList<>();

      Inner_ConfigStore(BlockPos origin, int sx, int sy, int sz) {
         this.init_BlockPos = origin;
         this.init_I = sx;
         this.load = sy;
         this.read = sz;
         this.write = sx * sy * sz;
         this.init_RefQueue_StreamUtiArr = new RefQueue.Inner_StreamUtil[this.write];
         Arrays.fill(this.init_RefQueue_StreamUtiArr, RefQueue.init_RefQueue_StreamUti);
      }

      int init(int x, int y, int z) {
         return x >= 0 && y >= 0 && z >= 0 && x < this.init_I && y < this.load && z < this.read ? (y * this.read + z) * this.init_I + x : -1;
      }

      int init(BlockPos p) {
         return this.init(p.getX() - this.init_BlockPos.getX(), p.getY() - this.init_BlockPos.getY(), p.getZ() - this.init_BlockPos.getZ());
      }

      int init_I(int i) {
         return i % this.init_I;
      }

      int load_I(int i) {
         return i / (this.init_I * this.read);
      }

      int read_I(int i) {
         return i / this.init_I % this.read;
      }

      BlockPos init_BlockPos(int i) {
         return this.init_BlockPos.offset(this.init_I(i), this.load_I(i), this.read_I(i));
      }

      int init(int i, Direction d) {
         return this.init(this.init_I(i) + d.getStepX(), this.load_I(i) + d.getStepY(), this.read_I(i) + d.getStepZ());
      }

      boolean init_Z(int i) {
         return this.init_I(i) == 0
            || this.init_I(i) == this.init_I - 1
            || this.read_I(i) == 0
            || this.read_I(i) == this.read - 1
            || this.load_I(i) == 0
            || this.load_I(i) == this.load - 1;
      }

      RefQueue.Inner_StreamUtil init_RefQueue_StreamUti(int i) {
         return i < 0 ? RefQueue.init_RefQueue_StreamUti : this.init_RefQueue_StreamUtiArr[i];
      }

      boolean load_Z(int i) {
         return i >= 0 && (this.init_RefQueue_StreamUtiArr[i].init_I == 0 || this.init_RefQueue_StreamUtiArr[i].init_I == 3);
      }

      boolean read_Z(int i) {
         return i >= 0 && this.init_RefQueue_StreamUtiArr[i].init_I != 1 && Double.isFinite(this.init_RefQueue_StreamUtiArr[i].init_D);
      }

      boolean init(int i, boolean dig) {
         if (i >= 0 && this.load_I(i) != 0 && this.load_I(i) < this.load - 1) {
            int head = this.init(i, Direction.UP);
            int floor = this.init(i, Direction.DOWN);
            return (dig ? this.read_Z(i) && this.read_Z(head) : this.init_RefQueue_StreamUti(i).init_I == 0 && this.init_RefQueue_StreamUti(head).init_I == 0)
               && this.init_RefQueue_StreamUti(floor).init_I != 0
               && this.init_RefQueue_StreamUti(floor).init_I != 3;
         } else {
            return false;
         }
      }
   }

   record Inner_NetHelper(
      Set<BlockPos> init_Set, List<BlockPos> init_List, List<BlockPos> load_List, double init_D, boolean init_Z, int init_I, Vec3 init_Vec3, Vec3 load_Vec3
   ) {
      boolean init_Z_2() {
         return this.init_Z && this.init_I > 1 && this.init_List.size() > 1;
      }

      public boolean load_Z() {
         return this.init_Z;
      }
   }

   record Inner_StreamUtil(int init_I, double init_D, int load) {
   }

   private record Inner_TaskQueue(BitSet init_BitSet, LinkedHashSet<BlockPos> init_LinkedHashSet, List<BlockPos> init_List) {
   }
}
