package autismclient.ai.a;

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public final class n {
   private n() {
   }

   public static String init(ClientLevel level, BlockPos center, BlockPos mark, int rxz, int ry) {
      Map<String, Character> legend = new LinkedHashMap<>();
      StringBuilder out = new StringBuilder(
         "terrain around "
            + center.toShortString()
            + " (x "
            + (center.getX() - rxz)
            + ".."
            + (center.getX() + rxz)
            + " left to right, z top to bottom; @ player, X target):"
      );

      for (int y = center.getY() + ry; y >= center.getY() - ry; y--) {
         out.append("\n y=").append(y);

         for (int z = center.getZ() - rxz; z <= center.getZ() + rxz; z++) {
            out.append("\n   ");

            for (int x = center.getX() - rxz; x <= center.getX() + rxz; x++) {
               BlockPos p = new BlockPos(x, y, z);
               if (p.equals(center)) {
                  out.append('@');
               } else if (p.equals(mark)) {
                  out.append('X');
               } else {
                  out.append(init(level.getBlockState(p), legend));
               }
            }
         }
      }

      if (!legend.isEmpty()) {
         out.append("\n legend ").append(legend);
      }

      return out.toString();
   }

   private static char init(BlockState state, Map<String, Character> legend) {
      if (state.isAir()) {
         return '.';
      } else {
         FluidState fluid = state.getFluidState();
         if (fluid.is(FluidTags.WATER)) {
            return '~';
         } else if (fluid.is(FluidTags.LAVA)) {
            return 'L';
         } else if (state.is(BlockTags.LEAVES)) {
            return '%';
         } else if (state.is(BlockTags.LOGS)) {
            return 'T';
         } else {
            String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
            if (!id.equals("stone")
               && !id.equals("deepslate")
               && !id.equals("dirt")
               && !id.equals("grass_block")
               && !id.equals("andesite")
               && !id.equals("diorite")
               && !id.equals("granite")
               && !id.equals("tuff")) {
               return state.canBeReplaced() ? ',' : legend.computeIfAbsent(id, k -> (char)(97 + legend.size() % 26));
            } else {
               return '#';
            }
         }
      }
   }
}
