package autismclient.ai.print;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

public interface PrintRules {
   boolean isFinished(PrintTarget var1, BlockState var2);

   boolean isPlacedCorrectly(PrintTarget var1, BlockState var2);

   default boolean acceptsUnpoweredOutput(PrintTarget target, BlockState live) {
      return false;
   }

   static boolean facesWithinSavedShape(BlockState wanted, BlockState live) {
      if (!live.is(wanted.getBlock())) {
         return false;
      }

      for (Direction side : Direction.values()) {
         if (MultifaceBlock.hasFace(live, side) && !MultifaceBlock.hasFace(wanted, side)) {
            return false;
         }
      }

      return true;
   }

   PrintRules.Inner_CachePool nextInteraction(PrintTarget var1, BlockState var2);

   default boolean isSafeToClick(PrintTarget target, SignalGetter world) {
      return true;
   }

   static boolean matchesIgnoringNotePower(BlockState wanted, BlockState live) {
      return wanted == live
         ? true
         : wanted.is(Blocks.NOTE_BLOCK)
            && live.is(Blocks.NOTE_BLOCK)
            && live.setValue(BlockStateProperties.POWERED, wanted.getValue(BlockStateProperties.POWERED)) == wanted;
   }

   static boolean matchesLayout(BlockState wanted, BlockState live) {
      if (!live.is(wanted.getBlock())) {
         return false;
      }

      for (Property<?> property : wanted.getProperties()) {
         if (!wanted.getValue(property).equals(live.getValue(property))) {
            String name = property.getName();
            if (!Set.of("powered", "power", "lit", "extended", "triggered", "locked", "enabled").contains(name)
               && (!name.equals("open") || !wanted.is(Blocks.IRON_TRAPDOOR) && !wanted.is(Blocks.IRON_DOOR))) {
               return false;
            }
         }
      }

      return true;
   }

   default PrintRules.Inner_StreamUtil traceSignals(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      return new PrintRules.Inner_StreamUtil(this.signalPaths(world, source, targets), PrintRules.Inner_DataUtil.NO_PATH);
   }

   default PrintRules.Inner_StreamUtil traceSignalsWithConfiguration(
      BlockGetter world, BlockPos source, Set<BlockPos> targets, Set<BlockPos> configurationTargets
   ) {
      return this.traceSignals(world, source, targets);
   }

   default Set<BlockPos> sourceInputs(BlockGetter world, BlockPos source) {
      return Set.of();
   }

   default Map<BlockPos, PrintRules.Inner_DataUtil> signalPaths(BlockGetter world, BlockPos source, Set<BlockPos> targets) {
      HashMap<BlockPos, PrintRules.Inner_DataUtil> result = new HashMap<>();

      for (BlockPos at : targets) {
         result.put(at, PrintRules.Inner_DataUtil.UNKNOWN);
      }

      return result;
   }

   boolean isWaterlogMissing(PrintTarget var1, BlockState var2);

   record Inner_CachePool(Item item, String property) {
   }

   enum Inner_DataUtil {
      REACHES,
      NO_PATH,
      UNKNOWN;
   }

   record Inner_StreamUtil(Map<BlockPos, PrintRules.Inner_DataUtil> reached, PrintRules.Inner_DataUtil unseen) {
   }
}
