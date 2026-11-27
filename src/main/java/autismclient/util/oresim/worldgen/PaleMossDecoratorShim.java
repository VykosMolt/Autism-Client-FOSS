package autismclient.util.oresim.worldgen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HangingMossBlock;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator.Context;

public final class PaleMossDecoratorShim {
   private PaleMossDecoratorShim() {
   }

   public static boolean init(Object context, SyntheticWorldGenLevel synthetic) {
      return false;
   }

   public static boolean load(Object context, SyntheticWorldGenLevel synthetic) {
      return false;
   }

   public static void init(Context context, SyntheticWorldGenLevel synthetic, float leavesProbability, float trunkProbability, float groundProbability) {
      RandomSource random = context.random();
      WorldGenLevel level = context.level();
      List<BlockPos> logs = Util.shuffledCopy(context.logs(), random);
      if (!logs.isEmpty()) {
         BlockPos origin = Collections.min(logs, Comparator.comparingInt(Vec3i::getY));
         if (random.nextFloat() < groundProbability) {
            level.registryAccess()
               .lookup(Registries.FEATURE)
               .flatMap(registry -> registry.get(VegetationFeatures.PALE_MOSS_PATCH))
               .ifPresent(mossPatch -> mossPatch.value().place(level, synthetic.chunkGenerator(), random, origin.above()));
         }

         context.logs().forEach(pos -> {
            if (random.nextFloat() < trunkProbability) {
               BlockPos down = pos.below();
               if (context.isAir(down)) {
                  init(down, context);
               }
            }
         });
         context.leaves().forEach(pos -> {
            if (random.nextFloat() < leavesProbability) {
               BlockPos down = pos.below();
               if (context.isAir(down)) {
                  init(down, context);
               }
            }
         });
      }
   }

   private static void init(BlockPos pos, Context context) {
      while (context.isAir(pos.below()) && !(context.random().nextFloat() < 0.5F)) {
         context.setBlock(pos, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, false));
         pos = pos.below();
      }

      context.setBlock(pos, Blocks.PALE_HANGING_MOSS.defaultBlockState().setValue(HangingMossBlock.TIP, true));
   }
}
