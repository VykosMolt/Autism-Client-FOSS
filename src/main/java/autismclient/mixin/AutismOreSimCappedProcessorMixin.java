package autismclient.mixin;

import autismclient.util.j.a.SyntheticWorldGenLevel;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CappedProcessor.class)
public abstract class AutismOreSimCappedProcessorMixin {
   @Shadow
   @Final
   private StructureProcessor delegate;
   @Shadow
   @Final
   private IntProvider limit;

   @WrapMethod(method = "finalizeProcessing")
   private List<StructureBlockInfo> autism$useSyntheticSeed(
      ServerLevelAccessor level,
      BlockPos position,
      BlockPos referencePos,
      List<StructureBlockInfo> originalBlocks,
      List<StructureBlockInfo> processedBlocks,
      StructurePlaceSettings settings,
      Operation<List<StructureBlockInfo>> original
   ) {
      if (!(level instanceof SyntheticWorldGenLevel synthetic)) {
         return original.call(level, position, referencePos, originalBlocks, processedBlocks, settings);
      } else if (this.limit.maxInclusive() != 0 && !processedBlocks.isEmpty()) {
         if (originalBlocks.size() != processedBlocks.size()) {
            Util.logAndPauseIfInIde(
               "Original block info list not in sync with processed list, skipping processing. Original size: "
                  + originalBlocks.size()
                  + ", Processed size: "
                  + processedBlocks.size()
            );
            return processedBlocks;
         }

         RandomSource random = RandomSource.createThreadLocalInstance(synthetic.getSeed()).forkPositional().at(position);
         int maxToReplace = Math.min(this.limit.sample(random), processedBlocks.size());
         if (maxToReplace < 1) {
            return processedBlocks;
         }

         IntArrayList indices = Util.toShuffledList(IntStream.range(0, processedBlocks.size()), random);
         IntIterator iterator = indices.intIterator();
         int replaced = 0;

         while (iterator.hasNext() && replaced < maxToReplace) {
            int index = iterator.nextInt();
            StructureBlockInfo originalInfo = originalBlocks.get(index);
            StructureBlockInfo processedInfo = processedBlocks.get(index);
            StructureBlockInfo altered = this.delegate.processBlock(level, position, referencePos, originalInfo.pos(), processedInfo, settings);
            if (altered != null && !processedInfo.equals(altered)) {
               replaced++;
               processedBlocks.set(index, altered);
            }
         }

         return processedBlocks;
      } else {
         return processedBlocks;
      }
   }
}
