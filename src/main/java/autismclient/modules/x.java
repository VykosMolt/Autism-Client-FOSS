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

public final class x {
   private static final int init_I = 1;
   private static final double[][] init_DArr2 = init_DArr2();
   private final Map<UUID, x.Inner_StreamUtil> init_Map = new HashMap<>();
   private x.Inner_StreamUtil init_x_StreamUtil;
   private Level init_Level;
   private long init_J = -9223372036854775808L;

   void init_V() {
      this.init_Map.clear();
      this.init_x_StreamUtil = null;
      this.init_Level = null;
      this.init_J = -9223372036854775808L;
   }

   List<x.Inner_DataUtil> init_List_Level_Vec3(Level level, Vec3 center) {
      if (level != null && center != null) {
         long tick = level.getGameTime();
         boolean same = this.init_Level == level && this.init_x_StreamUtil != null && this.init_x_StreamUtil.init_Vec3.equals(center);
         if (!same || tick != this.init_J && !init(this.init_x_StreamUtil, level, center)) {
            this.init_x_StreamUtil = init_x_StreamUtil_Level_Vec3(level, center);
         }

         this.init_Level = level;
         this.init_J = tick;
         return this.init_x_StreamUtil.init_List;
      } else {
         this.init_V();
         return List.of();
      }
   }

   List<x.Inner_DataUtil> init_List_Level_List(Level level, List<A.Inner_BufferHelper> warnings) {
      HashSet<UUID> live = new HashSet<>();
      Map<BlockPos, Double> combined = new HashMap<>();
      int calculations = 0;
      Iterator var6 = warnings.iterator();

      while (true) {
         x.Inner_StreamUtil saved;
         while (true) {
            if (!var6.hasNext()) {
               this.init_Map.keySet().retainAll(live);
               return init(combined);
            }

            A.Inner_BufferHelper warning = (A.Inner_BufferHelper)var6.next();
            if (!warning.init_Z()
               && warning.init_Entity() instanceof LargeFireball
               && !warning.init_Entity().isRemoved()
               && warning.init_A_DataUtil().init_Vec3() != null) {
               UUID id = warning.init_Entity().getUUID();
               live.add(id);
               Vec3 center = warning.init_A_DataUtil().init_Vec3();
               saved = this.init_Map.get(id);
               if (init(saved, level, center)) {
                  break;
               }

               this.init_Map.remove(id);
               if (calculations++ < 2) {
                  saved = init_x_StreamUtil_Level_Vec3(level, center);
                  this.init_Map.put(id, saved);
                  break;
               }
            }
         }

         for (x.Inner_DataUtil block : saved.init_List) {
            combined.merge(block.init_BlockPos, block.init_D, Math::max);
         }
      }
   }

   private static boolean init(x.Inner_StreamUtil saved, Level level, Vec3 center) {
      return saved != null
         && saved.init_Vec3.equals(center)
         && saved.init_Set.stream().noneMatch(pos -> init_Z_Level_BlockPos(level, pos))
         && saved.init_Map
            .entrySet()
            .stream()
            .allMatch(entry -> init_Z_Level_BlockPos(level, entry.getKey()) && level.getBlockState(entry.getKey()) == entry.getValue());
   }

   private static x.Inner_StreamUtil init_x_StreamUtil_Level_Vec3(Level level, Vec3 center) {
      Map<BlockPos, BlockState> states = new HashMap<>();
      HashSet<BlockPos> unavailable = new HashSet<>();
      List<x.Inner_DataUtil> blocks = init(pos -> {
         if (!init_Z_Level_BlockPos(level, pos)) {
            unavailable.add(pos);
            return null;
         } else {
            BlockState state = level.getBlockState(pos);
            states.put(pos, state);
            FluidState fluid = state.getFluidState();
            float resistance = state.isAir() && fluid.isEmpty() ? -1.0F : Math.max(state.getBlock().getExplosionResistance(), fluid.getExplosionResistance());
            return new x.Inner_CachePool(resistance, !state.isAir() && !(state.getBlock() instanceof LiquidBlock));
         }
      }, center, 1.0F);
      return new x.Inner_StreamUtil(center, states, unavailable, blocks);
   }

   private static boolean init_Z_Level_BlockPos(Level level, BlockPos pos) {
      return level.isInWorldBounds(pos) && level.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
   }

   static List<x.Inner_DataUtil> init(x.Inner_BufferHelper world, Vec3 center, float power) {
      if (center != null
         && Double.isFinite(center.x)
         && Double.isFinite(center.y)
         && Double.isFinite(center.z)
         && Float.isFinite(power)
         && !(power <= 0.0F)
         && !(power > 6.0F)) {
         Map<Long, x.Inner_CachePool> cells = new HashMap<>();
         Map<BlockPos, Double> risks = new HashMap<>();
         MutableBlockPos pos = new MutableBlockPos();

         for (double[] ray : init_DArr2) {
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

               x.Inner_CachePool cell = cells.get(key);
               if (cell == null || !Float.isFinite(cell.init_F)) {
                  break;
               }

               if (cell.init_F >= 0.0F) {
                  float loss = (cell.init_F + 0.3F) * 0.3F;
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

   private static List<x.Inner_DataUtil> init(Map<BlockPos, Double> risks) {
      List<x.Inner_DataUtil> blocks = new ArrayList<>(risks.size());
      risks.forEach((pos, risk) -> blocks.add(new x.Inner_DataUtil(pos, risk)));
      blocks.sort(Comparator.comparingLong(block -> block.init_BlockPos.asLong()));
      return List.copyOf(blocks);
   }

   private static double[][] init_DArr2() {
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

   interface Inner_BufferHelper {
      x.Inner_CachePool cell(BlockPos var1);
   }

   record Inner_CachePool(float init_F, boolean init_Z) {
   }

   public record Inner_DataUtil(BlockPos init_BlockPos, double init_D) {
   }

   private record Inner_StreamUtil(Vec3 init_Vec3, Map<BlockPos, BlockState> init_Map, Set<BlockPos> init_Set, List<x.Inner_DataUtil> init_List) {
   }
}
