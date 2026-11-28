package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public final class FireballBlastPredictor {
   private static final int init_I = 1;
   private static final double[][] RAYS = buildRays();
   private final Map<UUID, FireballBlastPredictor.SavedBlast> savedById = new HashMap<>();
   private FireballBlastPredictor.SavedBlast savedBlast;
   private Level level;
   private long tick = -9223372036854775808L;

   void reset() {
      this.savedById.clear();
      this.savedBlast = null;
      this.level = null;
      this.tick = -9223372036854775808L;
   }

   List<FireballBlastPredictor.DestroyedBlock> predictAt(Level level, Vec3 center) {
      if (level != null && center != null) {
         long tick = level.getGameTime();
         boolean same = this.level == level && this.savedBlast != null && this.savedBlast.center.equals(center);
         if (!same || tick != this.tick && !init(this.savedBlast, level, center)) {
            this.savedBlast = computeBlast(level, center);
         }

         this.level = level;
         this.tick = tick;
         return this.savedBlast.blocks;
      } else {
         this.reset();
         return List.of();
      }
   }

   List<FireballBlastPredictor.DestroyedBlock> predict(Level level, List<IncomingProjectileScanner.ProjectileWarning> warnings) {
      HashSet<UUID> live = new HashSet<>();
      Map<BlockPos, Double> combined = new HashMap<>();
      int calculations = 0;
      Iterator var6 = warnings.iterator();

      while (true) {
         FireballBlastPredictor.SavedBlast saved;
         while (true) {
            if (!var6.hasNext()) {
               this.savedById.keySet().retainAll(live);
               return init(combined);
            }

            IncomingProjectileScanner.ProjectileWarning warning = (IncomingProjectileScanner.ProjectileWarning)var6.next();
            if (!warning.init_Z()
               && warning.entity() instanceof LargeFireball
               && !warning.entity().isRemoved()
               && warning.init_A_DataUtil().init_Vec3() != null) {
               UUID id = warning.entity().getUUID();
               live.add(id);
               Vec3 center = warning.init_A_DataUtil().init_Vec3();
               saved = this.savedById.get(id);
               if (init(saved, level, center)) {
                  break;
               }

               this.savedById.remove(id);
               if (calculations++ < 2) {
                  saved = computeBlast(level, center);
                  this.savedById.put(id, saved);
                  break;
               }
            }
         }

         for (FireballBlastPredictor.DestroyedBlock block : saved.blocks) {
            combined.merge(block.pos, block.init_D, Math::max);
         }
      }
   }

   private static boolean init(FireballBlastPredictor.SavedBlast saved, Level level, Vec3 center) {
      return saved != null
         && saved.center.equals(center)
         && saved.positions.stream().noneMatch(pos -> isLoadedPos(level, pos))
         && saved.states
            .entrySet()
            .stream()
            .allMatch(entry -> isLoadedPos(level, entry.getKey()) && level.getBlockState(entry.getKey()) == entry.getValue());
   }

   private static FireballBlastPredictor.SavedBlast computeBlast(Level level, Vec3 center) {
      Map<BlockPos, BlockState> states = new HashMap<>();
      HashSet<BlockPos> unavailable = new HashSet<>();
      List<FireballBlastPredictor.DestroyedBlock> blocks = init(pos -> {
         if (!isLoadedPos(level, pos)) {
            unavailable.add(pos);
            return null;
         } else {
            BlockState state = level.getBlockState(pos);
            states.put(pos, state);
            FluidState fluid = state.getFluidState();
            float resistance = state.isAir() && fluid.isEmpty() ? -1.0F : Math.max(state.getBlock().getExplosionResistance(), fluid.getExplosionResistance());
            return new FireballBlastPredictor.CellResistance(resistance, !state.isAir() && !(state.getBlock() instanceof LiquidBlock));
         }
      }, center, 1.0F);
      return new FireballBlastPredictor.SavedBlast(center, states, unavailable, blocks);
   }

   private static boolean isLoadedPos(Level level, BlockPos pos) {
      return level.isInWorldBounds(pos) && level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   static List<FireballBlastPredictor.DestroyedBlock> init(FireballBlastPredictor.BlastWorld world, Vec3 center, float power) {
      if (center != null
         && Double.isFinite(center.x)
         && Double.isFinite(center.y)
         && Double.isFinite(center.z)
         && Float.isFinite(power)
         && !(power <= 0.0F)
         && !(power > 6.0F)) {
         Map<Long, FireballBlastPredictor.CellResistance> cells = new HashMap<>();
         Map<BlockPos, Double> risks = new HashMap<>();
         MutableBlockPos pos = new MutableBlockPos();

         for (double[] ray : RAYS) {
            double x = center.x;
            double y = center.y;
            double z = center.z;
            float remaining = power * 1.3F;
            float cost = 0.0F;
            long recorded = -9223372036854775808L;

            while (remaining > 0.0F) {
               pos.set(Mth.floor(x), Mth.floor(y), Mth.floor(z));
               long key = pos.asLong();
               if (!cells.containsKey(key)) {
                  cells.put(key, world.cell(pos.immutable()));
               }

               FireballBlastPredictor.CellResistance cell = cells.get(key);
               if (cell == null || !Float.isFinite(cell.resistance)) {
                  break;
               }

               if (cell.resistance >= 0.0F) {
                  float loss = (cell.resistance + 0.3F) * 0.3F;
                  remaining -= loss;
                  cost += loss;
               }

               if (remaining > 0.0F && cell.init_Z && recorded != key) {
                  recorded = key;
                  double probability = Math.clamp((power * 1.3F - cost) / (power * 0.6F), 0.0F, 1.0F);
                  risks.merge(pos.immutable(), probability, (a, b) -> 1.0 - (1.0 - a) * (1.0 - b));
               }

               x += ray[0] * 0.30000001192092896;
               y += ray[1] * 0.30000001192092896;
               z += ray[2] * 0.30000001192092896;
               remaining -= 0.22500001F;
               cost += 0.22500001F;
            }
         }

         return init(risks);
      } else {
         return List.of();
      }
   }

   private static List<FireballBlastPredictor.DestroyedBlock> init(Map<BlockPos, Double> risks) {
      List<FireballBlastPredictor.DestroyedBlock> blocks = new ArrayList<>(risks.size());
      risks.forEach((pos, risk) -> blocks.add(new FireballBlastPredictor.DestroyedBlock(pos, risk)));
      blocks.sort(Comparator.comparingLong(block -> block.pos.asLong()));
      return List.copyOf(blocks);
   }

   private static double[][] buildRays() {
      List<double[]> rays = new ArrayList<>(1352);

      for (int x = 0; x < 16; x++) {
         for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
               if (x == 0 || x == 15 || y == 0 || y == 15 || z == 0 || z == 15) {
                  double dx = x / 15.0F * 2.0F - 1.0F;
                  double dy = y / 15.0F * 2.0F - 1.0F;
                  double dz = z / 15.0F * 2.0F - 1.0F;
                  double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                  rays.add(new double[]{dx / length, dy / length, dz / length});
               }
            }
         }
      }

      return rays.toArray(double[][]::new);
   }

   interface BlastWorld {
      FireballBlastPredictor.CellResistance cell(BlockPos var1);
   }

   record CellResistance(float resistance, boolean init_Z) {
   }

   public record DestroyedBlock(BlockPos pos, double init_D) {
   }

   private record SavedBlast(Vec3 center, Map<BlockPos, BlockState> states, Set<BlockPos> positions, List<FireballBlastPredictor.DestroyedBlock> blocks) {
   }
}
