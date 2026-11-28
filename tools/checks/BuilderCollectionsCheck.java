package autismclient.ai;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

public class BuilderCollectionsCheck {
   static void check(boolean ok, String what) {
      if (!ok) {
         System.out.println("FAIL: " + what);
         System.exit(1);
      }
   }

   public static void main(String[] args) throws Exception {
      Map<Long, String> map = new AiPrintSupply.SectionedCellMap<>();
      check(map.put(5L, "a") == null && "a".equals(map.put(5L, "b")) && "b".equals(map.get(5L)) && map.size() == 1, "Map.put");
      check("c".equals(map.computeIfAbsent(7L, k -> "c")) && map.size() == 2, "Map.computeIfAbsent");

      List<BlockPos> list = new AiPrintSupply.PagedPosList();
      for (int i = 0; i < 1500; i++) {
         check(list.add(new BlockPos(i, 0, 0)), "List.add");
      }
      check(list.size() == 1500 && list.get(1234).getX() == 1234, "List.get after add");

      Set<Long> set = Collections.newSetFromMap(new AiPrintSupply.SectionedCellMap<>());
      check(set.add(1L) && !set.add(1L) && set.contains(1L) && set.size() == 1, "newSetFromMap.add");

      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      LevelReader world = (LevelReader) Proxy.newProxyInstance(LevelReader.class.getClassLoader(), new Class[]{LevelReader.class}, (proxy, method, a) -> switch (method.getName()) {
         case "getBlockState" -> Blocks.AIR.defaultBlockState();
         case "getFluidState" -> Fluids.EMPTY.defaultFluidState();
         case "getMinY" -> 0;
         case "getHeight" -> 256;
         default -> {
            if (method.isDefault()) {
               yield java.lang.reflect.InvocationHandler.invokeDefault(proxy, method, a);
            }
            throw new UnsupportedOperationException(method.getName());
         }
      });
      AiPrintSupply.SupplyPreparation prep = new AiPrintSupply.SupplyPreparation(new AiBuilder.Blueprint(BlockPos.ZERO, List.of()), world, p -> true, BlockPos.ZERO, false);
      BlockPos candidate = new BlockPos(3, 64, 3);
      prep.cells.put(candidate.above().asLong(), new AiBuilder.CellSpec(3, 65, 3, "minecraft:torch"));
      boolean[] result = new boolean[]{true};
      Thread t = new Thread(() -> result[0] = prep.init_Z_BlockPos(candidate));
      t.setDaemon(true);
      t.start();
      t.join(10_000);
      check(!t.isAlive(), "init_Z_BlockPos finally loop did not terminate");
      check(!result[0], "torch on air is not a support");
      check(prep.readsByCell.get(candidate.asLong()).contains(candidate.asLong()), "read_Map recorded the read");
      long chunk = (long) (candidate.getX() >> 4) << 32 | candidate.getZ() >> 4 & 4294967295L;
      check(prep.ownersByChunk.get(chunk).contains(candidate.asLong()) && prep.chunksByCell.get(candidate.asLong()).contains(chunk), "chunk owners recorded");
      System.out.println("PASS BuilderCollectionsCheck");
   }
}
