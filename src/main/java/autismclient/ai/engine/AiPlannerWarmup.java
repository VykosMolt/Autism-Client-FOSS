package autismclient.ai.engine;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

public final class AiPlannerWarmup {
   private static final int flowState = 100;
   private static volatile boolean putAt_Z;
   private static int flowTemp;
   static final BlockPos putAt_BlockPos = new BlockPos(0, 64, 0);
   static final BlockPos fill_BlockPos = new BlockPos(24, 64, 24);
   private static final int[][] putAt_IArr2 = new int[][]{{3, 0}, {0, 3}, {3, 3}, {-3, 3}, {-3, 0}, {0, -3}};

   private AiPlannerWarmup() {
   }

   public static void tick(Minecraft var0) {
      if (!putAt_Z) {
         if (++flowTemp >= 100) {
            putAt_Z = true;
            Thread var1 = new Thread(AiPlannerWarmup::putAt_V, "Autism planner warm-up");
            var1.setDaemon(true);
            var1.setPriority(1);
            var1.start();
         }
      }
   }

   private static void putAt_V() {
      try {
         long var0 = System.nanoTime();
         AiPathfinder.GridView var2 = putAt_FileStore_TextStor();
         AiPathfinder.PathOptions var3 = new AiPathfinder.PathOptions(
               false, false, false, true, false, true, AiPathfinder.RouteStrategy.FASTEST, AiPathfinder.ThreatField.NONE
            )
            .withFallPolicy(AiPathfinder.FallPolicy.PARKOUR_NO_DAMAGE);
         BlockPos var4 = putAt_BlockPos;
         BlockPos var5 = fill_BlockPos;

         for (int var6 = 0; var6 < 3 && System.nanoTime() - var0 < 12000000000L; var6++) {
            double var7 = 0.1 * (1.0 + 0.2 * var6);
            AiPathfinder.SearchSession var9 = AiPathfinder.SearchSession.createProofSession(
               new AiPlannerWarmup.SpeedGridView(var2, var7),
               var4,
               AiPathfinder.PathGoal.near(var5, 0.6, true),
               var3,
               Set.of(),
               true
            );

            for (int var10 = 0; var10 < 4000 && !var9.step(256); var10++) {
               if (System.nanoTime() - var0 > 12000000000L) {
                  var9.finish();
                  break;
               }
            }
         }

         int var12 = putAt(var2, var0 + 12000000000L);
         AiNavDebug.log("planner warm-up " + (System.nanoTime() - var0) / 1000000L + " ms, " + var12 + " jumps");
      } catch (Throwable var11) {
         AiNavDebug.log("planner warm-up skipped: " + var11);
      }
   }

   static int putAt(AiPathfinder.GridView var0, long var1) {
      int var3 = 0;

      for (int var4 = 0; var4 < 3; var4++) {
         AiPlannerWorld var5 = new AiPlannerWorld(new AiPlannerWarmup.SpeedGridView(var0, 0.1 * (1.0 + 0.2 * var4)), null);

         for (byte var6 = 0; var6 <= 24; var6 += 3) {
            for (byte var7 = 0; var7 <= 24; var7 += 3) {
               BlockPos var8 = putAt(var0, var6, var7);

               for (int[] var12 : putAt_IArr2) {
                  int var13 = var6 + var12[0];
                  int var14 = var7 + var12[1];
                  if (var13 >= 0 && var13 <= 24 && var14 >= 0 && var14 <= 24) {
                     if (System.nanoTime() > var1) {
                        return var3;
                     }

                     BlockPos var15 = putAt(var0, var13, var14);
                     AiJumpSolver.solve(var5, var8, var8.getY(), var15, var15.getY(), (var3 & 1) == 0 ? 0.0 : 0.2, (var3 & 2) == 0, false, true, false, false);
                     var3++;
                  }
               }
            }
         }
      }

      return var3;
   }

   private static BlockPos putAt(AiPathfinder.GridView var0, int var1, int var2) {
      for (int var3 = 60; var3 <= 66; var3++) {
         if (!var0.probe(var1, var3, var2).passable()) {
            return new BlockPos(var1, var3 + 1, var2);
         }
      }

      return new BlockPos(var1, 64, var2);
   }

   static AiPathfinder.GridView putAt_FileStore_TextStor() {
      Long2ObjectOpenHashMap var0 = new Long2ObjectOpenHashMap();
      AiPathfinder.CellInfo var1 = AiPathfinder.CellInfo.fullBlock(1.0 / 0.0);

      for (byte var2 = 0; var2 <= 24; var2 += 3) {
         for (byte var3 = 0; var3 <= 24; var3 += 3) {
            int var4 = (var2 != 0 || var3 != 0) && (var2 != 24 || var3 != 24) ? 62 + Math.floorMod(var2 * 7 + var3 * 5, 3) : 63;
            var0.put(BlockPos.asLong(var2, var4, var3), var1);
            if (Math.floorMod(var2 + var3, 9) == 3) {
               var0.put(BlockPos.asLong(var2, var4 + 4, var3), var1);
            }
         }
      }

      return (var1x, var2x, var3x) -> var2x >= 50 && var2x <= 90 && var1x >= -16 && var1x <= 48 && var3x >= -16 && var3x <= 24
         ? (AiPathfinder.CellInfo)var0.getOrDefault(BlockPos.asLong(var1x, var2x, var3x), AiPathfinder.CellInfo.air())
         : AiPathfinder.CellInfo.UNLOADED;
   }

   private record SpeedGridView(AiPathfinder.GridView grid, double speed) implements AiPathfinder.GridView {
      @Override
      public AiPathfinder.CellInfo probe(int var1, int var2, int var3) {
         return this.grid.probe(var1, var2, var3);
      }

      @Override
      public double movementSpeed() {
         return this.speed;
      }
   }
}
