import autismclient.ai.AuditBuilderLifeguard;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LifeguardCheck {
   static BlockGetter world(Map<BlockPos, BlockState> blocks) {
      return (BlockGetter) Proxy.newProxyInstance(BlockGetter.class.getClassLoader(), new Class[]{BlockGetter.class}, (proxy, m, a) -> switch (m.getName()) {
         case "getBlockState" -> blocks.getOrDefault((BlockPos) a[0], Blocks.AIR.defaultBlockState());
         case "getFluidState" -> blocks.getOrDefault((BlockPos) a[0], Blocks.AIR.defaultBlockState()).getFluidState();
         case "getBlockEntity" -> null;
         case "getMinY" -> -64;
         case "getHeight" -> 384;
         default -> {
            if (m.isDefault()) {
               yield InvocationHandler.invokeDefault(proxy, m, a);
            }
            throw new UnsupportedOperationException(m.getName());
         }
      });
   }

   // nearestLand as it was before pruning, to prove the pruned one returns the same cell.
   static BlockPos oldNearestLand(BlockGetter w, BlockPos from) {
      BlockPos best = null;
      double bestD = Double.MAX_VALUE;
      for (int dx = -12; dx <= 12; dx++) {
         for (int dz = -12; dz <= 12; dz++) {
            for (int y = from.getY() - 2; y <= from.getY() + 16; y++) {
               BlockPos c = new BlockPos(from.getX() + dx, y, from.getZ() + dz);
               BlockState floor = w.getBlockState(c.below());
               if (!floor.getFluidState().isEmpty() || !floor.isFaceSturdy(w, c.below(), Direction.UP)) {
                  continue;
               }
               if (!w.getFluidState(c).isEmpty()
                  || !w.getFluidState(c.above()).isEmpty()
                  || !w.getBlockState(c).getCollisionShape(w, c).isEmpty()
                  || !w.getBlockState(c.above()).getCollisionShape(w, c.above()).isEmpty()) {
                  continue;
               }
               double d = Math.hypot(dx, dz) + Math.abs(y - from.getY()) * 0.3;
               if (d < bestD) {
                  bestD = d;
                  best = c;
               }
            }
         }
      }
      return best;
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL LifeguardCheck: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      BlockState water = Blocks.WATER.defaultBlockState();
      BlockState stone = Blocks.STONE.defaultBlockState();

      Map<BlockPos, BlockState> sea = new HashMap<>();
      for (int x = -15; x <= 15; x++) {
         for (int z = -15; z <= 15; z++) {
            sea.put(new BlockPos(x, 50, z), stone);
            for (int y = 51; y <= 63; y++) {
               sea.put(new BlockPos(x, y, z), water);
            }
         }
      }
      BlockGetter open = world(sea);
      check(Objects.equals(AuditBuilderLifeguard.nearestAir(open, new BlockPos(0, 58, 0)), new BlockPos(0, 58, 0)), "air straight above in open water");
      check(AuditBuilderLifeguard.nearestLand(open, new BlockPos(0, 63, 0)) == null, "no land in open water");

      for (int y = 51; y <= 64; y++) {
         sea.put(new BlockPos(5, y, 0), stone);
      }
      check(Objects.equals(AuditBuilderLifeguard.nearestLand(world(sea), new BlockPos(0, 63, 0)), new BlockPos(5, 65, 0)), "island top is the nearest land");

      Random random = new Random(42);
      BlockState[] palette = {Blocks.AIR.defaultBlockState(), stone, water};
      for (int round = 0; round < 40; round++) {
         Map<BlockPos, BlockState> blocks = new HashMap<>();
         for (int x = -13; x <= 13; x++) {
            for (int z = -13; z <= 13; z++) {
               for (int y = 57; y <= 80; y++) {
                  blocks.put(new BlockPos(x, y, z), palette[random.nextInt(round % 2 == 0 ? 3 : 2)]);
               }
            }
         }
         BlockGetter w = world(blocks);
         BlockPos from = new BlockPos(0, 62, 0);
         check(Objects.equals(AuditBuilderLifeguard.nearestLand(w, from), oldNearestLand(w, from)), "pruned nearestLand differs in round " + round);
      }

      System.out.println("PASS LifeguardCheck");
   }
}
