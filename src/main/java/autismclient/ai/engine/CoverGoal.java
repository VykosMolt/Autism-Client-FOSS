package autismclient.ai.engine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;

public final class CoverGoal implements AiPathfinder.PathGoal {
   private final List<BlockPos> stances;
   private final Set<Long> stanceKeys = new HashSet<>();

   public CoverGoal(List<BlockPos> stances) {
      this.stances = List.copyOf(stances);

      for (BlockPos p : this.stances) {
         this.stanceKeys.add(p.asLong());
      }
   }

   public List<BlockPos> init_List() {
      return this.stances;
   }

   @Override
   public boolean isInGoal(int x, int y, int z) {
      return this.stanceKeys.contains(BlockPos.asLong(x, y, z));
   }

   @Override
   public double heuristic(int x, int y, int z) {
      double best = 1.7976931348623157E308;

      for (BlockPos p : this.stances) {
         best = Math.min(best, AiPathfinder.MoveCosts.estimateDirectionalHeuristicCompat(p.getX() - x, p.getY() - y, p.getZ() - z));
      }

      return best == 1.7976931348623157E308 ? 0.0 : best;
   }

   @Override
   public String toString() {
      return "cover(" + this.stances.size() + " stances)";
   }
}
