package autismclient.ai.a;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

final class MapStore {
   private static final int flowState = 720;
   private static final int flowTemp = 11;
   private static final int[] putAt_IArr = new int[]{0, -1, 1};
   private static final int[] fill_IArr = new int[]{0, -4, 4, -10, 10};
   private static final int[] readEx = new int[]{0, -30, 30};

   private MapStore() {
   }

   static TableIndex.Inner_StateCache putAt(NodeWalker.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5) {
      var var7 = TableIndex.putAt_List_TokenCore_TaskStat(var0, var4, var5);
      if (!var7.isEmpty() && !TableIndex.putAt_Z_TokenCore_TaskStat(var0, var4.getX() + 0.5, var5, var4.getZ() + 0.5)) {
         TableIndex.Inner_SyncManager var8 = (TableIndex.Inner_SyncManager)var7.getFirst();
         AABB var9 = new AABB(var1.getX(), var2 - 0.02, var1.getZ(), var1.getX() + 1, var2 - 0.001, var1.getZ() + 1);

         for (AABB var11 : var0.collisions(var9)) {
            if (!(Math.abs(var11.maxY - var2) > 0.01)) {
               boolean var12 = var11.getXsize() >= 0.75 && var11.getZsize() <= 0.25;
               if (var12 || var11.getZsize() >= 0.75 && var11.getXsize() <= 0.25) {
                  int var13 = var12 ? var4.getX() - var1.getX() : var4.getZ() - var1.getZ();
                  int var14 = var12 ? var4.getZ() - var1.getZ() : var4.getX() - var1.getX();
                  if (Math.abs(var13) >= 2 && var14 != 0) {
                     int var15 = Integer.signum(var13);
                     int var16 = Integer.signum(var14);
                     float var17 = var12 ? -90 * var15 : (var15 > 0 ? 0.0F : 180.0F);
                     double var18 = var12 ? var4.getX() + 0.5 : var4.getZ() + 0.5;
                     double var20 = var12 ? (var15 > 0 ? var11.maxX : var11.minX) : (var15 > 0 ? var11.maxZ : var11.minZ);
                     double var22 = (var12 ? var8.z() : var8.x()) * var16;
                     MapStore.Inner_DataUtil var24 = new MapStore.Inner_DataUtil(
                        var12, var15, var16, var17, var20 * var15 + 0.65, var18 * var15 - 0.78, var22 - 0.06, var18 * var15 - 0.4, var22 - 0.01
                     );
                     TableIndex.Inner_StateCache var25 = putAt(var0, var1, var2, var4, var5, var11, var24);
                     if (var25 != null) {
                        return var25;
                     }
                  }
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static TableIndex.Inner_StateCache putAt(
      NodeWalker.Inner_CachePool var0, BlockPos var1, double var2, BlockPos var4, double var5, AABB var7, MapStore.Inner_DataUtil var8
   ) {
      Long2ObjectOpenHashMap var9 = new Long2ObjectOpenHashMap();
      List var10 = putAt(var0, var1, var7, var2, var8.alongX());
      int var11 = var8.alongX() ? var8.alongSign() * var8.acrossSign() : -var8.alongSign() * var8.acrossSign();

      for (int var12 = 0; var12 <= 11 && !var10.isEmpty(); var12++) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         Long2ObjectOpenHashMap var13 = new Long2ObjectOpenHashMap();

         for (MapStore.Inner_StreamUtil var15 : (Iterable<MapStore.Inner_StreamUtil>)(Iterable<?>)(var10)) {
            NodeWalker.Inner_StreamUtil var16 = var15.state();
            float var17 = var15.input() == null ? var8.yaw() : var15.input().yaw();

            for (byte var18 = -18; var18 <= 102; var18 += 6) {
               float var19 = var8.yaw() + var18 * var11;
               if (!(Math.abs(Mth.wrapDegrees(var19 - var17)) > 56.0F)) {
                  for (int var23 : putAt_IArr) {
                     NodeWalker.Inner_DataUtil var24 = new NodeWalker.Inner_DataUtil(1.0, var23, true, true, false, var19);
                     NodeWalker.Inner_StreamUtil var25 = NodeWalker.putAt(var16, var24, var0);
                     if (!var25.onGround() && !(var25.y() < var2 + 0.12) && !NodeWalker.fill(var0, var16, var25)) {
                        putAt(var9, new MapStore.Inner_StreamUtil(var25, var24, var15, var12, var8.score(var25, var5)));
                     }
                  }
               }
            }

            for (int var57 : fill_IArr) {
               for (int var66 : putAt_IArr) {
                  NodeWalker.Inner_DataUtil var26 = new NodeWalker.Inner_DataUtil(1.0, var66, false, true, false, var8.yaw() + var57);
                  NodeWalker.Inner_StreamUtil var27 = NodeWalker.putAt(var16, var26, var0);
                  if (var27.onGround()
                     && !(Math.abs(var27.y() - var2) > 0.02)
                     && !(var8.along(var27) > var8.launchLimit())
                     && !NodeWalker.fill(var0, var16, var27)) {
                     putAt(var13, new MapStore.Inner_StreamUtil(var27, var26, var15, var12, 0.0));
                  }
               }
            }
         }

         var10 = new ArrayList<>(var13.values());
      }

      List var45 = putAt(var9);
      boolean var46 = var0.putAt_Z_AABB(new AABB(var4.getX() + 0.05, var5 + 1.5, var4.getZ() + 0.05, var4.getX() + 0.95, var5 + 1.8 + 0.35, var4.getZ() + 0.95));
      TableIndex.Inner_StateCache var47 = null;

      for (int var48 = 0; var48 < 14 && !var45.isEmpty(); var48++) {
         if (Thread.currentThread().isInterrupted()) {
            return null;
         }

         Long2ObjectOpenHashMap var49 = new Long2ObjectOpenHashMap();

         for (MapStore.Inner_StreamUtil var52 : (Iterable<MapStore.Inner_StreamUtil>)(Iterable<?>)(var45)) {
            boolean[] var54 = var46 && var52.state().vy() < 0.26 && var52.state().y() <= var5 + 1.25 ? new boolean[]{false, true} : new boolean[]{false};
            int var56 = 0;

            for (MapStore.Inner_StreamUtil var58 = var52; var58 != null && var58.input() != null && var58.input().sneak() && var56 < 3; var58 = var58.previous()) {
               var56++;
            }

            for (int var65 : readEx) {
               for (int var28 : putAt_IArr) {
                  for (int var32 : putAt_IArr) {
                     for (boolean var36 : var54) {
                        if (var28 != 0 || var32 != 0 || var65 == 0 || var36) {
                           float var37 = var52.input().yaw() + var65;
                           NodeWalker.Inner_DataUtil var38 = new NodeWalker.Inner_DataUtil(var28, var32, false, var28 > 0 && !var36, var36, var37);
                           NodeWalker.Inner_StreamUtil var39 = NodeWalker.putAt(var52.state(), var38, var0);
                           if ((!(var39.y() < var5 - 0.02) || !(var39.vy() < 0.0)) && !NodeWalker.fill(var0, var52.state(), var39)) {
                              int var40 = var36 ? var56 + 1 : 0;
                              if (var40 >= 3 || var39.pose() == 0 || !AiPaceScript.putAt_Z_TokenCore_TaskStat_2(var0, var52.state(), var39)) {
                                 MapStore.Inner_StreamUtil var41 = new MapStore.Inner_StreamUtil(
                                    var39, var38, var52, var52.run(), var8.score(var39, var5) + Math.abs(var65) * 4.0E-4
                                 );
                                 if (var39.onGround() && Math.abs(var39.y() - var5) <= 0.02) {
                                    double var42 = Math.min(TableIndex.putAt_D_TokenCore_TaskStat_2(var0, var39, var4, var5, var37), putAt(var0, var39, var5));
                                    if (var42 >= -0.04 && (var47 == null || var42 > var47.margin())) {
                                       TableIndex.Inner_StateCache var44 = putAt(var1, var41, var42, var8.yaw());
                                       if (var44.maneuver().replay(var0, var44.maneuver().start(), 0, var4, var5) >= -0.04) {
                                          if (var42 >= 0.03) {
                                             return var44;
                                          }

                                          var47 = var44;
                                       }
                                    }
                                 }

                                 putAt(var49, var41);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         var45 = putAt(var49);
      }

      return var47;
   }

   private static double putAt(NodeWalker.Inner_CachePool var0, NodeWalker.Inner_StreamUtil var1, double var2) {
      double var4 = 0.3;
      AABB var6 = new AABB(var1.x() - var4, var2 - 0.02, var1.z() - var4, var1.x() + var4, var2 - 0.001, var1.z() + var4);
      double var7 = -1.0;

      for (AABB var10 : var0.collisions(var6)) {
         if (!(Math.abs(var10.maxY - var2) > 0.01)) {
            double var11 = Math.min(var6.maxX, var10.maxX) - Math.max(var6.minX, var10.minX);
            double var13 = Math.min(var6.maxZ, var10.maxZ) - Math.max(var6.minZ, var10.minZ);
            var7 = Math.max(var7, Math.min(var11, var13) - 0.055);
         }
      }

      return var7;
   }

   private static List<MapStore.Inner_StreamUtil> putAt(NodeWalker.Inner_CachePool var0, BlockPos var1, AABB var2, double var3, boolean var5) {
      ArrayList<MapStore.Inner_StreamUtil> var6 = new ArrayList<>();
      int var7 = var5 ? var1.getX() : var1.getZ();
      int var8 = var5 ? var1.getZ() : var1.getX();
      double var9 = var5 ? var2.minZ : var2.minX;
      double var11 = var5 ? var2.maxZ : var2.maxX;

      for (double var16 : new double[]{0.288, 0.2275, 0.1875, 0.1475}) {
         for (double var21 : new double[]{var9 - var16, var11 + var16}) {
            if (!(var21 < var8) && !(var21 > var8 + 1)) {
               for (double var26 : new double[]{-0.265, 0.015, 0.44, 0.72, 1.265}) {
                  double var28 = var5 ? var7 + var26 : var21;
                  double var30 = var5 ? var21 : var7 + var26;
                  if (TableIndex.putAt_Z_TokenCore_TaskStat(var0, var28, var3, var30)) {
                     var6.add(new MapStore.Inner_StreamUtil(NodeWalker.Inner_StreamUtil.standing(var28, var3, var30), null, null, 0, 0.0));
                  }
               }
            }
         }
      }

      return var6;
   }

   private static void putAt(Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> var0, MapStore.Inner_StreamUtil var1) {
      NodeWalker.Inner_StreamUtil var2 = var1.state();
      long var3 = Math.round(var2.x() * 32.0) & 511L
         | (Math.round(var2.z() * 32.0) & 511L) << 9
         | (Math.round(var2.y() * 40.0) & 511L) << 18
         | (Math.round(var2.vx() * 60.0) & 127L) << 27
         | (Math.round(var2.vz() * 60.0) & 127L) << 34
         | (Math.round(var2.vy() * 60.0) & 63L) << 41;
      if (var2.onGround()) {
         var3 |= 140737488355328L;
      }

      MapStore.Inner_StreamUtil var5 = (MapStore.Inner_StreamUtil)var0.get(var3);
      if (var5 == null || var1.score() < var5.score()) {
         var0.put(var3, var1);
      }
   }

   private static List<MapStore.Inner_StreamUtil> putAt(Long2ObjectOpenHashMap<MapStore.Inner_StreamUtil> var0) {
      var var1 = new ArrayList<>(var0.values());
      var1.sort(Comparator.comparingDouble(MapStore.Inner_StreamUtil::score));
      return var1.subList(0, Math.min(720, var1.size()));
   }

   private static TableIndex.Inner_StateCache putAt(BlockPos var0, MapStore.Inner_StreamUtil var1, double var2, float var4) {
      ArrayDeque var5 = new ArrayDeque();

      MapStore.Inner_StreamUtil var6;
      for (var6 = var1; var6.input() != null; var6 = var6.previous()) {
         var5.addFirst(var6.input());
      }

      NodeWalker.Inner_StreamUtil var7 = var6.state();
      NodeWalker.Inner_StreamUtil var8 = var1.state();
      PoolEntry var9 = new PoolEntry(List.copyOf(var5), var7, var8, var2, var1.run());
      double var10 = Math.hypot(var7.x() - var0.getX() - 0.5, var7.z() - var0.getZ() - 0.5);
      return new TableIndex.Inner_StateCache(
         var4, var1.run(), true, 2147483647, var5.size() - var1.run(), var7.x(), var7.z(), var8.x(), var8.y(), var8.z(), var8.speed(), var2, var10, 0.0, var9
      );
   }

   private record Inner_DataUtil(
      boolean alongX,
      int alongSign,
      int acrossSign,
      float yaw,
      double launchLimit,
      double cornerAlong,
      double cornerAcross,
      double landingAlong,
      double landingAcross
   ) {
      double along(NodeWalker.Inner_StreamUtil var1) {
         return (this.alongX ? var1.x() : var1.z()) * this.alongSign;
      }

      double across(NodeWalker.Inner_StreamUtil var1) {
         return (this.alongX ? var1.z() : var1.x()) * this.acrossSign;
      }

      double score(NodeWalker.Inner_StreamUtil var1, double var2) {
         double var4 = this.along(var1);
         double var6 = this.across(var1);
         double var8 = (this.alongX ? var1.vx() : var1.vz()) * this.alongSign;
         double var10 = (this.alongX ? var1.vz() : var1.vx()) * this.acrossSign;
         return var4 < this.cornerAlong && var6 < this.cornerAcross
            ? Math.hypot((this.cornerAcross - var6 - var10 * 0.9) * 1.46, this.cornerAlong + 0.1 - var4 - var8 * 0.9)
               + Math.max(0.0, var2 + 0.1 - var1.y()) * 0.3
            : Math.hypot(this.landingAcross - var6 - var10 * 0.6, this.landingAlong - var4 - var8 * 0.6) + Math.max(0.0, var2 - var1.y()) * 0.3;
      }
   }

   private record Inner_StreamUtil(
      NodeWalker.Inner_StreamUtil state, NodeWalker.Inner_DataUtil input, MapStore.Inner_StreamUtil previous, int run, double score
   ) {
   }
}
