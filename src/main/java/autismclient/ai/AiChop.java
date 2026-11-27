package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

final class AiChop {
   static final int init_I = 64;
   private static final int[][] COLUMN_OFFSETS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}, {0, 0}};
   private static final int[][] load = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

   private AiChop() {
   }

   static boolean init(String arg, String blockId) {
      return autismclient.ai.engine.AiMine.init(arg, blockId);
   }

   static List<BlockPos> init(Predicate<BlockPos> matches, BlockPos start, int cap) {
      if (cap >= 1 && matches.test(start)) {
         List<BlockPos> result = new ArrayList<>();
         Set<Long> visited = new HashSet<>();
         ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
         visited.add(start.asLong());
         frontier.add(start);

         while (!frontier.isEmpty() && result.size() < cap) {
            BlockPos next = frontier.poll();
            result.add(next);

            for (int dx = -1; dx <= 1; dx++) {
               for (int dy = -1; dy <= 1; dy++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     if (dx != 0 || dy != 0 || dz != 0) {
                        BlockPos neighbor = next.offset(dx, dy, dz);
                        if (visited.add(neighbor.asLong()) && matches.test(neighbor)) {
                           frontier.add(neighbor);
                        }
                     }
                  }
               }
            }
         }

         return result;
      } else {
         return List.of();
      }
   }

   static List<BlockPos> init(Collection<BlockPos> veinBlocks, BlockPos reference) {
      List<BlockPos> ordered = new ArrayList<>(veinBlocks);
      ordered.sort(Comparator.comparingInt(Vec3i::getY).thenComparingLong(p -> {
         long dx = p.getX() - reference.getX();
         long dz = p.getZ() - reference.getZ();
         return dx * dx + dz * dz;
      }).thenComparingInt(Vec3i::getX).thenComparingInt(Vec3i::getZ));
      return ordered;
   }

   static BlockPos init(List<BlockPos> remainingBottomUp, Predicate<BlockPos> directlyBreakable) {
      for (BlockPos pos : remainingBottomUp) {
         if (directlyBreakable.test(pos)) {
            return pos;
         }
      }

      return null;
   }

   static List<BlockPos> init(BlockPos target, List<BlockPos> veinBottomUp, Predicate<BlockPos> support, Predicate<BlockPos> clear, BlockPos reference) {
      List<BlockPos> candidates = new ArrayList<>();
      Set<Long> seen = new HashSet<>();

      for (int[] column : COLUMN_OFFSETS) {
         init(candidates, seen, new BlockPos(target.getX() + column[0], target.getY() - 1, target.getZ() + column[1]));
      }

      for (BlockPos v : veinBottomUp) {
         init(candidates, seen, v, support, clear);

         for (int[] d : load) {
            init(candidates, seen, v.offset(d[0], 0, d[1]), support, clear);
         }
      }

      candidates.sort(Comparator.comparingDouble(p -> p.distSqr(reference)));
      return candidates;
   }

   private static void init(List<BlockPos> candidates, Set<Long> seen, BlockPos pos) {
      if (seen.add(pos.asLong())) {
         candidates.add(pos);
      }
   }

   private static void init(List<BlockPos> candidates, Set<Long> seen, BlockPos floor, Predicate<BlockPos> support, Predicate<BlockPos> clear) {
      if (support.test(floor) && clear.test(floor.above()) && clear.test(floor.above(2))) {
         init(candidates, seen, floor.above());
      }
   }

   static List<AiChop.Inner_DataUtil.Inner_StreamUtil> init(List<AiChop.Inner_DataUtil.Inner_StreamUtil> drops) {
      List<AiChop.Inner_DataUtil.Inner_StreamUtil> ordered = new ArrayList<>(drops);
      ordered.sort(
         Comparator.comparingDouble(AiChop.Inner_DataUtil.Inner_StreamUtil::distanceSqr).thenComparingInt(AiChop.Inner_DataUtil.Inner_StreamUtil::entityId)
      );
      return ordered;
   }

   static AiOperation.Status init(boolean countReached, boolean queueEmpty, int chopped, int skipped) {
      if (countReached) {
         return AiOperation.Status.SUCCEEDED;
      } else if (!queueEmpty) {
         return null;
      } else {
         return skipped == 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED;
      }
   }

   static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         String blockArg = AiJson.getString(args, "block", "log").trim();
         if (blockArg.isEmpty()) {
            throw new IllegalArgumentException("block must be a registry id or a suffix family like \"log\"");
         }

         boolean counted = args.has("count");
         JsonObject gather = AiJson.objectOf(
            "block",
            blockArg,
            "trees",
            true,
            "radius",
            Math.max(16, Math.min(64, AiJson.getInt(args, "radius", 48))),
            "timeoutSeconds",
            Math.max(20, Math.min(600, AiJson.getInt(args, "timeoutSeconds", 180)))
         );
         if (args.has("held")) {
            gather.addProperty("held", Math.max(1, AiJson.getInt(args, "held", 1)));
         } else if (counted) {
            gather.addProperty("count", Math.max(1, Math.min(64, AiJson.getInt(args, "count", 64))));
         } else {
            gather.addProperty("treeLimit", 1);
         }

         return autismclient.ai.engine.AiBlockMine.create(gather);
      } else {
         throw new IllegalArgumentException("Chopping requires a connected living player");
      }
   }

   static final class Inner_DataUtil {
      static final int init_I = 3;
      private final LinkedList<AiChop.Inner_DataUtil.Inner_CachePool> queue = new LinkedList<>();

      void init(List<BlockPos> ordered) {
         for (BlockPos pos : ordered) {
            this.queue.addLast(new AiChop.Inner_DataUtil.Inner_DataUtil_2(pos, 0));
         }
      }

      void load(List<AiChop.Inner_DataUtil.Inner_StreamUtil> nearestFirst) {
         for (int i = nearestFirst.size() - 1; i >= 0; i--) {
            this.queue.addFirst(nearestFirst.get(i));
         }
      }

      AiChop.Inner_DataUtil.Inner_CachePool peek() {
         return this.queue.peek();
      }

      AiChop.Inner_DataUtil.Inner_CachePool poll() {
         return this.queue.poll();
      }

      boolean isEmpty() {
         return this.queue.isEmpty();
      }

      int size() {
         return this.queue.size();
      }

      List<BlockPos> init_List() {
         List<BlockPos> out = new ArrayList<>();

         for (AiChop.Inner_DataUtil.Inner_CachePool target : this.queue) {
            if (target instanceof AiChop.Inner_DataUtil.Inner_DataUtil_2 b) {
               out.add(b.pos());
            }
         }

         return out;
      }

      boolean init_Z(BlockPos pos) {
         for (int i = 0; i < this.queue.size(); i++) {
            if (this.queue.get(i) instanceof AiChop.Inner_DataUtil.Inner_DataUtil_2 b && b.pos().equals(pos)) {
               this.queue.remove(i);
               return true;
            }
         }

         return false;
      }

      int load_I() {
         AiChop.Inner_DataUtil.Inner_CachePool head = this.queue.peek();
         return head instanceof AiChop.Inner_DataUtil.Inner_DataUtil_2 b ? this.init_I(b.pos()) : -1;
      }

      int init_I(BlockPos pos) {
         for (int i = 0; i < this.queue.size(); i++) {
            if (this.queue.get(i) instanceof AiChop.Inner_DataUtil.Inner_DataUtil_2 b && b.pos().equals(pos)) {
               this.queue.set(i, new AiChop.Inner_DataUtil.Inner_DataUtil_2(pos, b.failures() + 1));
               return b.failures() + 1;
            }
         }

         return -1;
      }

      int load(BlockPos pos) {
         for (AiChop.Inner_DataUtil.Inner_CachePool target : this.queue) {
            if (target instanceof AiChop.Inner_DataUtil.Inner_DataUtil_2 b && b.pos().equals(pos)) {
               return b.failures();
            }
         }

         return 0;
      }

      sealed interface Inner_CachePool permits AiChop.Inner_DataUtil.Inner_DataUtil_2, AiChop.Inner_DataUtil.Inner_StreamUtil {
      }

      record Inner_DataUtil_2(BlockPos pos, int failures) implements AiChop.Inner_DataUtil.Inner_CachePool {
      }

      record Inner_StreamUtil(int entityId, String item, BlockPos pos, double distanceSqr) implements AiChop.Inner_DataUtil.Inner_CachePool {
      }
   }
}
