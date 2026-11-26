package autismclient.ai.a;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class CoverGoal implements AiPathfinder.Inner_JsonUtil {
   private final List<BlockPos> init_List;
   private final Set<Long> init_Set = new HashSet<>();

   public CoverGoal(List<BlockPos> stances) {
      this.init_List = List.copyOf(stances);

      for (BlockPos p : this.init_List) {
         this.init_Set.add(p.asLong());
      }
   }

   public List<BlockPos> init_List() {
      return this.init_List;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.init_Set.contains(BlockPos.asLong(x, y, z));
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double best = 1.7976931348623157E308;

      for (BlockPos p : this.init_List) {
         best = Math.min(best, AiPathfinder.Inner_CachePool.load(p.getX() - x, p.getY() - y, p.getZ() - z));
      }

      return best == 1.7976931348623157E308 ? 0.0 : best;
   }

   @Override
   public String toString() {
      return "cover(" + this.init_List.size() + " stances)";
   }
}
