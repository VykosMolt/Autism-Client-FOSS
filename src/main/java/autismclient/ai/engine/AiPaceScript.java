package autismclient.ai.engine;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class AiPaceScript {
   static final double putAt_D = 3.0;
   static final double fill_D = 1.2;
   static final double readEx_D = 0.3;
   static final double value_D = 2.5;
   static final double prevEx_D = 0.357;
   static final double prev_D = 0.12;
   private static final ConcurrentHashMap<Long, Double> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
   static final double chunkAt_D = 4.0;
   static final Set<AiPathfinder.MoveType> putAt_Set = EnumSet.of(
      AiPathfinder.MoveType.JUMP,
      AiPathfinder.MoveType.SPRINT_JUMP,
      AiPathfinder.MoveType.JUMP_GAP_2,
      AiPathfinder.MoveType.JUMP_GAP_3,
      AiPathfinder.MoveType.PARKOUR_ASCEND,
      AiPathfinder.MoveType.LADDER_JUMP
   );
   static final Set<AiPathfinder.MoveType> fill_Set = EnumSet.of(
      AiPathfinder.MoveType.WALK,
      AiPathfinder.MoveType.WALK_DIAGONAL,
      AiPathfinder.MoveType.SPRINT,
      AiPathfinder.MoveType.SPRINT_DIAGONAL,
      AiPathfinder.MoveType.SPRINT_JUMP
   );
   private static final int value_I = 0;
   private static final int prevEx_I = 1;
   private static final int prev_I = 2;
   private static final int[] putAt_IArr = new int[]{2, 4, 6, 8};
   private static final int[] fill_IArr = new int[]{2, 5};
   private static final int[] readEx_IArr = new int[]{3, 6};
   private static final int[] value_IArr = new int[]{1, -1};
   private static final int[] prevEx_IArr = new int[]{1, 2, 4, 7};
   private static final int[] prev_IArr = new int[]{2, 5, 9};
   private static final int[] chunkAt_IArr = new int[]{2, 3, 5, 9};
   private static final int chunkAt_I = 120;
   private static final int state_I = 20;
   private static final int[] state_IArr = new int[]{2, 5, 10, 20};
   private static final float[] putAt_FArr = new float[]{0.0F, -8.0F, 8.0F, -20.0F, 20.0F, -40.0F, 40.0F};
   private static final float[] fill_FArr = new float[]{0.0F, -15.0F, 15.0F, -35.0F, 35.0F};
   static final double state_D = 0.75;
   static final int flowState = 4;
   static final double closeOf_D = 2.0;
   private static final int closeOf_I = 3;
   private static final int pollAt_I = 4;
   static final double pollAt_D = 8.0;
   static final double pollOf_D = 4.0;
   static final int flowTemp = 30000;
   private static final int pollOf_I = 2000;
   private static final int tick_I = 300;
   private static final int flushOf_I = 48;
   private static final int stateOf_I = 0;
   private static final int store_I = 1;
   private static final int nextBy_I = 2;
   private static final int dataEx_I = 3;
   static final int readEx_I = 3;
   private static final float putAt_F = 20.0F;
   private static final int[] closeOf_IArr = new int[]{3, 8, 16};
   private static final int[] pollAt_IArr = new int[]{8, 16, 32};
   private static final float fill_F = 20.0F;
   private static final int frameOf_I = 0;
   private static final int syncAt_I = 1;
   private static final int stopBy_I = 2;
   private static final int dataTo_I = 3;
   private static final int[] pollOf_IArr = new int[]{1, 2, 4};
   private static final float[] readEx_FArr = new float[]{-4.0F, 4.0F, -14.0F, 14.0F, -30.0F, 30.0F, -60.0F, 60.0F, -90.0F, 90.0F};
   private static final float readEx_F = 40.0F;
   private static final float value_F = 8.0F;
   private static final float[] value_FArr = putAt(putAt_FArr, readEx_FArr);
   private static final int[] tick_IArr = new int[]{2, 4, 6};
   private static final ThreadLocal<AiPaceScript.Inner_TableCache> putAt_ThreadLocal = ThreadLocal.withInitial(AiPaceScript.Inner_TableCache::new);
   private static final int[] flushOf_IArr = new int[]{1, 2, 3};
   private static final int initAt_I = 0;
   private static final int setOf = 1;
   private static final int count = 2;
   private static final double syncAt_D = 1.5;
   private static final int getTo = Math.clamp(Runtime.getRuntime().availableProcessors() - 4, 1, 8);
   private static final ExecutorService putAt_ExecutorService = Executors.newFixedThreadPool(getTo, var0 -> {
      Thread var1 = new Thread(var0, "Autism parkourwr search");
      var1.setDaemon(true);
      var1.setPriority(4);
      return var1;
   });
   static final double tick_D = 0.125;
   static final double flushOf_D = 0.14;
   static final double stateOf_D = 0.05;
   static final double store_D = 1.5;
   static final double nextBy_D = 0.002;
   static final double dataEx_D = 0.002;
   static final double frameOf_D = 0.01;
   private static final double stopBy_D = 4.0;
   private static final double dataTo_D = 10.0;
   private static final double initAt_D = 30.0;

   private AiPaceScript() {
   }

   public static double putAt_D_D(double var0) {
      return putAt_D_D(var0, true, true);
   }

   public static double putAt_D_D(double var0, boolean var2, boolean var3) {
      if (Double.isFinite(var0) && !(var0 <= 0.0)) {
         long var4 = Double.doubleToRawLongBits(var0) ^ (var2 ? 0L : 1L) ^ (var3 ? 0L : 2L);
         return putAt_ConcurrentHashMap.computeIfAbsent(
            var4,
            var4x -> {
               AiPhysics.PhysicsWorld var5 = new AiPhysics.PhysicsWorld() {
                  @Override
                  public double movementSpeed() {
                     return var0;
                  }

                  @Override
                  public List<AABB> collisions(AABB var1) {
                     return var1.minY < 64.0
                        ? List.of(
                           new AABB(
                              Math.floor(var1.minX) - 1.0, 63.0, Math.floor(var1.minZ) - 1.0, Math.ceil(var1.maxX) + 1.0, 64.0, Math.ceil(var1.maxZ) + 1.0
                           )
                        )
                        : List.of();
                  }
               };
               AiPhysics.MotionState var6 = AiPhysics.MotionState.standing(0.5, 64.0, 0.5);
               AiPhysics.MotionState var7 = var6;

               for (int var8 = 0; var8 < 80; var8++) {
                  var7 = AiPhysics.step(var7, AiPhysics.MoveInput.running(0.0F, var3), var5);
               }

               double var17 = var7.z();

               for (int var10 = 0; var10 < 80; var10++) {
                  var7 = AiPhysics.step(var7, AiPhysics.MoveInput.running(0.0F, var3), var5);
               }

               double var18 = (var7.z() - var17) / 80.0;
               if (!var2) {
                  return Math.max(0.1, var18 + 5.0E-4);
               }

               AiPhysics.MotionState var12 = var6;

               for (int var13 = 0; var13 < 120; var13++) {
                  var12 = AiPhysics.step(
                     var12, var12.onGround() ? AiPhysics.MoveInput.jump(0.0F, var3) : AiPhysics.MoveInput.running(0.0F, var3), var5
                  );
               }

               double var19 = var12.z();

               for (int var15 = 0; var15 < 120; var15++) {
                  var12 = AiPhysics.step(
                     var12, var12.onGround() ? AiPhysics.MoveInput.jump(0.0F, var3) : AiPhysics.MoveInput.running(0.0F, var3), var5
                  );
               }

               double var20 = (var12.z() - var19) / 120.0;
               return Math.max(0.1, Math.max(var18, var20) + 5.0E-4);
            }
         );
      } else {
         return 0.357;
      }
   }

   static AiPhysics.PhysicsWorld putAt(AiPhysics.PhysicsWorld var0, double var1) {
      if (!Double.isFinite(var1) || var1 <= 0.0 || Math.abs(var0.movementSpeed() - var1) <= 1.0E-9) {
         return var0;
      } else if (var0 instanceof AiPlannerWorld var3) {
         var3.setSpeed(var1);
         return var3;
      } else {
         return new AiPaceScript.Inner_EventBus(var0, var1);
      }
   }

   static AiPhysics.PhysicsWorld withMovementSpeed(AiPhysics.PhysicsWorld var0, double var1) {
      return Double.isFinite(var1) && !(var1 <= 0.0) && !(Math.abs(var0.movementSpeed() - var1) <= 1.0E-9) ? new AiPaceScript.Inner_EventBus(var0, var1) : var0;
   }

   private static AiPhysics.PhysicsWorld putAt_TokenCore_TaskStat_TokenCore_TaskStat(AiPhysics.PhysicsWorld var0, int var1, AiPaceScript.Inner_BitCache var2) {
      return var2 != null && var2.hasTransition() ? putAt(var0, var2.speedFor(var1)) : var0;
   }

   public static AiPaceScript.Inner_IoPool putAt_TextPool_IoPool_TokenCore_TableCac(
      AiPhysics.MotionState var0, float var1, AiPaceScript.Inner_JsonCache var2, int var3, int var4
   ) {
      int var5 = Math.clamp(var4, 0, var2.segments() - 1);
      double var6 = var2.cum()[Math.clamp(var4, 0, var2.x().length - 1)];
      if (var3 < var2.checkpoints().length) {
         var6 = Math.min(var6, var2.cum()[var2.checkpointPoint()[var3]]);
      }

      return new AiPaceScript.Inner_IoPool(var0, var1, var5, var3, var6);
   }

   public static AiPaceScript.Inner_IoPool fill(AiPhysics.MotionState var0, float var1, AiPaceScript.Inner_JsonCache var2, int var3, int var4) {
      AiPaceScript.Inner_BitCore var5 = putAt_TextPool_BitCore_TokenCore_TableCac(var0, Math.max(0, var4), var2, 1.7976931348623157E308);
      if (var5.seg() >= 0 && !(var5.dist() > 3.0)) {
         double var6 = var5.progress();
         if (var3 < var2.checkpoints().length) {
            var6 = Math.min(var6, var2.cum()[var2.checkpointPoint()[var3]]);
         }

         return new AiPaceScript.Inner_IoPool(var0, var1, var5.seg(), var3, var6);
      } else {
         return null;
      }
   }

   public static AiPaceScript.Inner_CacheHelper putAt_TextPool_CacheHelp_TextPool_JsonCache(AiPaceScript.Inner_JsonCache var0, double var1, int var3) {
      double var4 = var1 + 4.0;
      int var6 = var3 < var0.checkpoints().length ? var0.checkpointPoint()[var3] : 2147483647;
      int var7 = 0;

      while (var7 < var0.cells().length) {
         int var8 = var0.cellPoint()[var7];
         if (var8 < var6) {
            if (!(var0.cum()[var8] >= var4) || var7 < var0.plainCell().length && !var0.plainCell()[var7]) {
               var7++;
               continue;
            }

            return new AiPaceScript.Inner_CacheHelper(var0.cells()[var7], var8, false);
         }
         break;
      }

      return var3 >= var0.checkpoints().length ? null : new AiPaceScript.Inner_CacheHelper(var0.checkpoints()[var3], var6, true);
   }

   public static AiPaceScript.Inner_PacketHelper putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      float var2,
      AiPaceScript.Inner_JsonCache var3,
      AiPaceScript.Inner_TextState var4,
      BooleanSupplier var5
   ) {
      return putAt_TextPool_PacketHel_TextPool_HashEntry(
         AiPaceScript.Inner_HashEntry.putAt_TextPool_HashEntry_TokenCore_TaskStat(var0),
         AiPaceScript.Inner_IoPool.of(var1, var2),
         var3,
         var4,
         null,
         var5,
         new AiPaceScript.Inner_IoHelper()
      );
   }

   public static AiPaceScript.Inner_PacketHelper putAt_TextPool_PacketHel_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPaceScript.Inner_BitCache var4,
      BooleanSupplier var5,
      AiPaceScript.Inner_IoHelper var6
   ) {
      return putAt_TextPool_PacketHel_TextPool_HashEntry(
         AiPaceScript.Inner_HashEntry.putAt_TextPool_HashEntry_TokenCore_TaskStat(var0), var1, var2, var3, var4, var5, var6
      );
   }

   public static AiPaceScript.Inner_PacketHelper putAt(
      Supplier<AiPhysics.PhysicsWorld> var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      BooleanSupplier var4,
      AiPaceScript.Inner_IoHelper var5
   ) {
      return putAt_TextPool_PacketHel_TextPool_HashEntry(new AiPaceScript.Inner_HashEntry(var0), var1, var2, var3, null, var4, var5);
   }

   public static AiPaceScript.Inner_PacketHelper putAt_TextPool_PacketHel_Supplier(
      Supplier<AiPhysics.PhysicsWorld> var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPaceScript.Inner_BitCache var4,
      BooleanSupplier var5,
      AiPaceScript.Inner_IoHelper var6
   ) {
      return putAt_TextPool_PacketHel_TextPool_HashEntry(new AiPaceScript.Inner_HashEntry(var0), var1, var2, var3, var4, var5, var6);
   }

   public static Supplier<AiPhysics.PhysicsWorld> putAt_Supplier_FileStore_TextStor_2(AiPathfinder.GridView var0, double var1, boolean var3) {
      return putAt_Supplier_FileStore_TextStor(var0, var1, var3 ? AiPathfinder.FallPolicy.PARKOUR_NO_DAMAGE : AiPathfinder.FallPolicy.PARKOUR_SAFE);
   }

   public static Supplier<AiPhysics.PhysicsWorld> putAt_Supplier_FileStore_TextStor(
      AiPathfinder.GridView var0, double var1, AiPathfinder.FallPolicy var3
   ) {
      return () -> {
         AiPathfinder.MoveGenerator.CachedGrid var4 = new AiPathfinder.MoveGenerator.CachedGrid(var0, 16384, var1);
         var4.fallPolicy = var3;
         return var4.plannerWorld();
      };
   }

   static AiPaceScript.Inner_PacketHelper putAt(
      AiPaceScript.Inner_HashEntry var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      BooleanSupplier var4,
      AiPaceScript.Inner_IoHelper var5
   ) {
      return putAt_TextPool_PacketHel_TextPool_HashEntry(var0, var1, var2, var3, null, var4, var5);
   }

   static AiPaceScript.Inner_PacketHelper putAt_TextPool_PacketHel_TextPool_HashEntry(
      AiPaceScript.Inner_HashEntry var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPaceScript.Inner_BitCache var4,
      BooleanSupplier var5,
      AiPaceScript.Inner_IoHelper var6
   ) {
      LongAdder var7 = new LongAdder();
      int var8 = var2.checkpoints().length;
      int var9 = 0;
      int var10 = 0;
      int var11 = var3.maxNodes();
      boolean var12 = false;
      double var13 = -1.0;
      int var15 = var6.size();
      if (var2.plateGoal() && var1.touched() < var8) {
         var6.putAt_V_BlockPosArr(Arrays.copyOfRange(var2.checkpoints(), var1.touched(), var8));
      } else if (!var2.plateGoal() && var1.touched() < var8) {
         AiPhysics.PhysicsWorld var16 = var0.putAt_TokenCore_TaskStat();

         try {
            BlockPos var17 = var2.checkpoints()[var8 - 1];
            if (var16.pressurePlateAt(var17.getX(), var17.getY(), var17.getZ())) {
               var6.putAt_V_BlockPosArr(new BlockPos[]{var17});
            }
         } finally {
            var0.putAt_V_TokenCore_TaskStat(var16);
         }
      }

      boolean var80 = var3.weight() > 1.05;
      boolean var81 = var4 != null && var4.hasTransition();
      if (var80 && !var81 && var15 == 0 && !var2.plateGoal() && var8 == 1 && var2.length() <= 120.0 && (var5 == null || !var5.getAsBoolean())) {
         AiPhysics.PhysicsWorld var18 = var0.putAt_TokenCore_TaskStat();

         boolean var19;
         try {
            var19 = putAt(var18, var1, var2, var3, var6, var7);
         } finally {
            var0.putAt_V_TokenCore_TaskStat(var18);
         }

         if (var19) {
            var var84 = var6.fill_List();
            int[] var85 = new int[]{var84.size()};
            return new AiPaceScript.Inner_PacketHelper(List.copyOf(var6.putAt_List()), List.copyOf(var84), var85, true, 0, var7.sum(), null);
         }
      }

      AiPhysics.PhysicsWorld var82 = var0.putAt_TokenCore_TaskStat();

      boolean var83;
      try {
         var83 = !var1.state().onGround() || onClimbable(var82, var1.state()) || putAt_Z_TokenCore_TaskStat(var82, var1.state(), 0.002);
      } finally {
         var0.putAt_V_TokenCore_TaskStat(var82);
      }

      AiPaceScript.Inner_TaskState var20 = new AiPaceScript.Inner_TaskState(
         var1.state(),
         var1.yaw(),
         var1.seg(),
         var1.touched(),
         var15,
         var1.progress(),
         0.0,
         0.0,
         null,
         new AiPhysics.MoveInput[0],
         new AiPhysics.MotionState[0],
         null
      );
      var20.putAt_Z = var83;
      var20.readEx_Z = var83;
      ArrayDeque<AiPaceScript.Inner_TaskState> var21 = new ArrayDeque<>();
      ArrayList<AiPaceScript.Inner_TaskState> var22 = new ArrayList<>();
      AiPaceScript.Inner_TaskState var23 = null;
      AiPaceScript.Inner_TaskState var24 = null;
      double var25 = 0.0;
      int var27 = var0.putAt_Z() ? 4 * getTo : 1;
      boolean var28 = false;
      double var29 = -1.0;

      while (var5 == null || !var5.getAsBoolean()) {
         double var31 = Math.max(var20.putAt_D + (var28 ? Math.min(8.0, var3.horizon()) : var3.horizon()), var13);
         if (!var28 && var31 < var2.length() && var2.length() - var31 <= 6.0) {
            var31 = var2.length() + 1.0;
         }

         PriorityQueue<AiPaceScript.Inner_TaskState> var33 = new PriorityQueue<>((var0x, var1x) -> Double.compare(var0x.fill_D, var1x.fill_D));
         Long2IntOpenHashMap var34 = new Long2IntOpenHashMap(256);
         var34.defaultReturnValue(2147483647);
         AiPaceScript.Inner_RingUtil var35 = new AiPaceScript.Inner_RingUtil(var2.planned().length);
         var33.add(var20);
         AiPaceScript.Inner_TaskState var36 = null;
         AiPaceScript.Inner_TaskState var37 = null;
         int var38 = 0;
         int var39 = 0;
         int var40 = var80 ? 48 : 2147483647;
         int var41 = var80 ? 48 : (var2.length() <= 30.0 ? 300 : 2147483647);
         double var42 = var20.putAt_D;
         AiPaceScript.Inner_TaskState var44 = var20;
         int var45 = var28 ? Math.min(var11, 30000) : var11;

         while (!var33.isEmpty() && var38 < var45 && var36 == null && (var5 == null || !var5.getAsBoolean())) {
            if (var37 != null && var39 >= (var37.flowTemp == var8 ? var41 : var40)) {
               var36 = var37;
               break;
            }

            ArrayList var46 = new ArrayList(var27);

            while (var46.size() < var27 && !var33.isEmpty()) {
               AiPaceScript.Inner_TaskState var47 = (AiPaceScript.Inner_TaskState)var33.peek();
               if (var37 != null && var47.fill_D >= var37.fill_D) {
                  if (var46.isEmpty()) {
                     var36 = var37;
                  }
                  break;
               }

               if (putAt_Z_TextPool_TaskState(var47, var20, var8, var31)) {
                  var33.poll();
                  if (var37 == null || var47.fill_D < var37.fill_D) {
                     var37 = var47;
                  }

                  if (var46.isEmpty()) {
                     var36 = var37;
                  }
                  break;
               }

               var46.add((AiPaceScript.Inner_TaskState)var33.poll());
            }

            if (var36 != null || var46.isEmpty()) {
               break;
            }

            AiPaceScript.Inner_HashPool var97 = new AiPaceScript.Inner_HashPool(var34, var31, var37 != null ? var37.fill_D : 1.0 / 0.0, var80, var4);
            var var48 = putAt_List_List(var46, var2, var3, var0, var7, var28, var35, var97);

            for (int var49 = 0; var49 < var46.size(); var49++) {
               var38++;
               if (var37 != null) {
                  var39++;
               }

               if (((AiPaceScript.Inner_TaskState)var46.get(var49)).putAt_D > var42) {
                  var44 = (AiPaceScript.Inner_TaskState)var46.get(var49);
               }

               var42 = Math.max(var42, ((AiPaceScript.Inner_TaskState)var46.get(var49)).putAt_D);
               var6.putAt_V_D(var42);

               for (AiPaceScript.Inner_TaskState var51 : (Iterable<AiPaceScript.Inner_TaskState>)(Iterable<?>)var48.get(var49)) {
                  if (var51.readEx_I <= var3.maxTicks()) {
                     if (putAt_Z_TextPool_TaskState(var51, var20, var8, var31) && (var37 == null || var51.fill_D < var37.fill_D)) {
                        if (var37 == null || var51.fill_D < var37.fill_D - 0.05) {
                           var39 = 0;
                        }

                        var37 = var51;
                     }

                     if (var37 == null || var51 == var37 || !(var51.fill_D >= var37.fill_D + 3.0)) {
                        long var52 = var51.putAt_J;
                        int var54 = var51.putAt();
                        if (var34.get(var52) > var54) {
                           var34.put(var52, var54);
                           var33.add(var51);
                        }
                     }
                  }
               }
            }
         }

         if (var36 == null && var37 != null) {
            var36 = var37;
         }

         var9 += var38;
         if (AiNavDebug.isEnabled()) {
            AiPhysics.MotionState var95 = var44.putAt_TokenCore_TableCac;
            String var98 = String.format(
               Locale.ROOT,
               "parkourwr window%s from %.1f (tick %d, %.2f %.2f %.2f) to %.1f: %s after %d nodes%s",
               var28 ? " (fine)" : "",
               var20.putAt_D,
               var20.readEx_I,
               var20.putAt_TokenCore_TableCac.x(),
               var20.putAt_TokenCore_TableCac.y(),
               var20.putAt_TokenCore_TableCac.z(),
               var31,
               var36 != null ? "reached" : "no way on",
               var38,
               var36 != null
                  ? ""
                  : String.format(
                     Locale.ROOT,
                     " (furthest %.1f at %.3f %.3f %.3f v %.3f %.3f %.3f)",
                     var42,
                     var95.x(),
                     var95.y(),
                     var95.z(),
                     var95.vx(),
                     var95.vy(),
                     var95.vz()
                  )
            );
            AiNavDebug.log(var98);
         }

         if (var36 == null) {
            if (var23 == null) {
               var23 = var20;
               var24 = (AiPaceScript.Inner_TaskState)var21.peek();
            }

            var25 = Math.max(var25, var42);
            if (var5 != null && var5.getAsBoolean()) {
               break;
            }

            if (!var28) {
               var28 = true;
               var29 = var31;
            } else {
               if (var10 >= 3 || var21.isEmpty()) {
                  break;
               }

               var10++;
               var13 = var31;
               var11 = Math.min(2 * var11, 2 * var3.maxNodes());
               var20 = (AiPaceScript.Inner_TaskState)var21.pop();
            }
         } else {
            if (var23 != null) {
               if (!var6.putAt_Z_I(var20.readEx_I)) {
                  break;
               }

               var23 = null;
            }

            var12 = var36.flowTemp == var8;
            AiPaceScript.Inner_TaskState var96 = var12
               ? var36
               : putAt_TextPool_TaskState_TextPool_TaskState(var36, var20, var20.putAt_D + (var28 ? Math.min(4.0, var3.commit()) : var3.commit()));
            ArrayList var99 = new ArrayList();
            ArrayList var100 = new ArrayList();
            ArrayList<Integer> var101 = new ArrayList<>();
            ArrayList var102 = var81 ? new ArrayList() : null;
            ArrayList var103 = new ArrayList();

            for (AiPaceScript.Inner_TaskState var104 = var96; var104 != var20; var104 = var104.putAt_TextPool_TaskState) {
               var103.add(var104);
            }

            for (int var105 = var103.size() - 1; var105 >= 0; var105--) {
               AiPaceScript.Inner_TaskState var53 = (AiPaceScript.Inner_TaskState)var103.get(var105);
               var99.addAll(Arrays.asList(var53.putAt_TokenCore_CacheHelArr));
               var100.addAll(Arrays.asList(var53.putAt_TokenCore_TableCacArr));

               for (int var106 = 0; var106 < var53.putAt_TokenCore_TableCacArr.length; var106++) {
                  var101.add(var53.flowState);
               }

               if (var102 != null) {
                  if (var53.putAt_DArr != null) {
                     for (double var57 : var53.putAt_DArr) {
                        var102.add(var57);
                     }
                  } else {
                     for (int var107 = 0; var107 < var53.putAt_TokenCore_TableCacArr.length; var107++) {
                        var102.add(var4.speedFor(var53.flowTemp));
                     }
                  }
               }
            }

            var6.putAt(var99, var100, var101, var102);
            var21.push(var20);
            var22.add(var20);
            var20 = new AiPaceScript.Inner_TaskState(
               var96.putAt_TokenCore_TableCac,
               var96.putAt_F,
               var96.flowState,
               var96.flowTemp,
               var96.readEx_I,
               var96.putAt_D,
               var96.fill_D,
               var96.readEx_D,
               null,
               new AiPhysics.MoveInput[0],
               new AiPhysics.MotionState[0],
               null
            );
            var20.putAt_Z = var96.putAt_Z;
            var20.value_I = var96.value_I;
            var20.fill_Z = var96.fill_Z;
            var20.readEx_Z = var96.readEx_Z;
            var20.prevEx = var96.prevEx;
            if (var12) {
               break;
            }

            if (var20.putAt_D >= var13) {
               var13 = -1.0;
               var10 = 0;
               var11 = var3.maxNodes();
            }

            var28 = var13 >= 0.0 || var20.putAt_D < var29;
         }
      }

      AiPaceScript.Inner_TaskState var86 = var23 != null ? var23 : var20;
      AiPaceScript.Inner_TaskState var32 = var23 != null ? var24 : (AiPaceScript.Inner_TaskState)var21.peek();
      if (!var12 && var32 != null) {
         int var87 = var22.indexOf(var32);

         for (int var89 = var87; var89 >= 0 && var32.prevEx > 0.75 && var22.size() - var89 <= 4; var89--) {
            if (((AiPaceScript.Inner_TaskState)var22.get(var89)).prevEx <= 0.75) {
               var32 = (AiPaceScript.Inner_TaskState)var22.get(var89);
            }
         }
      }

      if (!var12 && var32 != null && var32.readEx_I < var86.readEx_I && var6.putAt_Z_I(var32.readEx_I)) {
         var86 = var32;
      }

      AiPaceScript.Inner_StreamState var88 = var12
         ? null
         : new AiPaceScript.Inner_StreamState(var86.putAt_D, Math.max(var25, var86.putAt_D), var86.flowState, var86.flowTemp);
      var6.finish(var12, var88);
      var var90 = var6.fill_List();
      int[] var91 = new int[var8];
      Arrays.fill(var91, -1);
      int var92 = var1.touched();
      AiPhysics.PhysicsWorld var93 = var0.putAt_TokenCore_TaskStat();

      try {
         for (int var94 = 0; var94 < var90.size() && var92 < var91.length; var94++) {
            if (putAt_Z_TokenCore_TaskStat_4(
               var93, (AiPhysics.MotionState)var90.get(var94), var2.checkpoints()[var92], var2.plateGoal(), var2.centerGoal()
            )) {
               var91[var92++] = var94 + 1;
            }
         }
      } finally {
         var0.putAt_V_TokenCore_TaskStat(var93);
      }

      return new AiPaceScript.Inner_PacketHelper(List.copyOf(var6.putAt_List()), List.copyOf(var90), var91, var12, var9, var7.sum(), var88);
   }

   private static AiPaceScript.Inner_TaskState putAt_TextPool_TaskState_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_TaskState var1, double var2
   ) {
      ArrayList var4 = new ArrayList();

      for (AiPaceScript.Inner_TaskState var5 = var0; var5 != var1; var5 = var5.putAt_TextPool_TaskState) {
         var4.add(var5);
      }

      AiPaceScript.Inner_TaskState var8 = null;

      for (int var6 = var4.size() - 1; var6 >= 0; var6--) {
         AiPaceScript.Inner_TaskState var7 = (AiPaceScript.Inner_TaskState)var4.get(var6);
         if (var7.readEx_Z) {
            if (var7.putAt_D >= var2) {
               return var7;
            }

            var8 = var7;
         }
      }

      return var8 != null ? var8 : var0;
   }

   public static AiPaceScript.Inner_JsonCache putAt(
      AiPathfinder.GridView var0, AiPhysics.MotionState var1, List<BlockPos> var2, double var3, boolean var5, BooleanSupplier var6
   ) {
      return putAt_TextPool_JsonCache_FileStore_TextStor(var0, var1, var2, var3, var3, var5, var6);
   }

   public static AiPaceScript.Inner_JsonCache putAt_TextPool_JsonCache_FileStore_TextStor(
      AiPathfinder.GridView var0, AiPhysics.MotionState var1, List<BlockPos> var2, double var3, double var5, boolean var7, BooleanSupplier var8
   ) {
      AiPaceScript.Inner_TokenCodec var9 = putAt_TextPool_TokenCode_FileStore_TextStor(var0, var1, var2, var3, var5, var7, var8);
      return var9 == null ? null : var9.guide();
   }

   public static AiPaceScript.Inner_TokenCodec putAt_TextPool_TokenCode_FileStore_TextStor(
      AiPathfinder.GridView var0, AiPhysics.MotionState var1, List<BlockPos> var2, double var3, double var5, boolean var7, BooleanSupplier var8
   ) {
      AiPathfinder.FallPolicy var9 = var7 ? AiPathfinder.FallPolicy.PARKOUR_NO_DAMAGE : AiPathfinder.FallPolicy.PARKOUR_SAFE;
      AiPathfinder.PathOptions var10 = new AiPathfinder.PathOptions(
            false, false, false, true, false, true, AiPathfinder.RouteStrategy.FASTEST, AiPathfinder.ThreatField.NONE
         )
         .withFallPolicy(var9);
      AiPathfinder.MoveGenerator.CachedGrid var11 = new AiPathfinder.MoveGenerator.CachedGrid(var0, 50000, var5);
      var11.fallPolicy = var9;
      ArrayList<double[]> var12 = new ArrayList<>();
      ArrayList var13 = new ArrayList();
      ArrayList var14 = new ArrayList();
      ArrayList<BlockPos> var15 = new ArrayList<>();
      HashSet<Integer> var16 = new HashSet<>();
      ArrayList<Boolean> var17 = new ArrayList<>();
      ArrayList<AiPaceScript.Inner_JsonEntry> var18 = new ArrayList<>();
      var12.add(new double[]{var1.x(), var1.y(), var1.z()});
      BlockPos var19 = BlockPos.containing(var1.x(), var1.y() + 1.0E-4, var1.z());
      BlockPos var20 = putAt_BlockPos_FileStore_IoHelper(var11, var1, var19);
      if (var20.getX() != var19.getX() || var20.getZ() != var19.getZ()) {
         AiPaceScript.Inner_JsonCache.add(var12, var20.getX() + 0.5, var1.y(), var20.getZ() + 0.5, false);
      }

      int var21 = var2.size();
      AiPathfinder.PathResult[] var22 = new AiPathfinder.PathResult[var21];
      AtomicBoolean var23 = new AtomicBoolean(false);
      BooleanSupplier var24 = () -> var23.get() || var8.getAsBoolean();
      if (var21 == 1) {
         var22[0] = putAt(var0, var20, (BlockPos)var2.get(0), var5, var10, var24);
         if (var22[0] == null) {
            if (!var24.getAsBoolean()) {
               String var25 = String.format(
                  Locale.ROOT,
                  "parkourwr guide leg 1/1 failed: %s -> %s (speed=%.3f, noDamage=%b, start=%.2f,%.2f,%.2f)",
                  var20.toShortString(),
                  ((BlockPos)var2.get(0)).toShortString(),
                  var5,
                  var7,
                  var1.x(),
                  var1.y(),
                  var1.z()
               );
               AiNavDebug.log(var25);
               LogUtils.getLogger().info("[AI] " + var25);
            }

            return null;
         }
      } else {
         ArrayList var40 = new ArrayList(var21);

         for (int var26 = 0; var26 < var21; var26++) {
            int var27 = var26;
            BlockPos var28 = var27 == 0 ? var20 : putAt(var11, (BlockPos)var2.get(var27 - 1));
            BlockPos var29 = (BlockPos)var2.get(var27);
            double var30 = var27 == 0 ? var3 : var5;
            var40.add(
               putAt_ExecutorService.submit(
                  () -> {
                     AiPathfinder.PathResult var13x = putAt(var0, var28, var29, var30, var10, var24);
                     var22[var27] = var13x;
                     if (var13x == null) {
                        if (!var24.getAsBoolean()) {
                           String var14x = String.format(
                              Locale.ROOT,
                              "parkourwr guide leg %d/%d failed: %s -> %s (speed=%.3f, noDamage=%b, start=%.2f,%.2f,%.2f)",
                              var27 + 1,
                              var21,
                              var28.toShortString(),
                              var29.toShortString(),
                              var30,
                              var7,
                              var1.x(),
                              var1.y(),
                              var1.z()
                           );
                           AiNavDebug.log(var14x);
                           LogUtils.getLogger().info("[AI] " + var14x);
                        }

                        var23.set(true);
                     }
                  }
               )
            );
         }

         try {
            for (Future var47 : (Iterable<Future>)(Iterable<?>)(var40)) {
               var47.get();
            }
         } catch (InterruptedException var38) {
            var23.set(true);

            for (Future var52 : (Iterable<Future>)(Iterable<?>)(var40)) {
               var52.cancel(true);
            }

            Thread.currentThread().interrupt();
            return null;
         } catch (ExecutionException var39) {
            var23.set(true);
            throw new IllegalStateException("parkourwr leg plan failed", var39.getCause());
         }

         for (AiPathfinder.PathResult var58 : var22) {
            if (var58 == null) {
               return null;
            }
         }
      }

      ArrayList var41 = new ArrayList();

      for (int var44 = 0; var44 < var21; var44++) {
         AiPathfinder.PathResult var49 = var22[var44];
         var41.addAll(var49.movements());

         for (AiPathfinder.PathStep var59 : var49.movements()) {
            BlockPos var62 = var59.to();
            int var31 = var12.size();
            AiJumpSolver.JumpPlan var32 = var59.jump();
            double var33 = var11.probePos(var62).climbable()
               ? var62.getY()
               : AiPathfinder.MoveGenerator.surfaceY(var62.getY(), var11.probePos(var62.below()));
            double var35 = var32 != null && !var11.probePos(var62).climbable() && Math.abs(var32.landY() - var33) > 0.25
               ? var32.landY()
               : var33;
            AiPaceScript.Inner_JsonCache.add(var12, var62.getX() + 0.5, var35, var62.getZ() + 0.5, putAt_Set.contains(var59.type()));
            if (var59.type() == AiPathfinder.MoveType.SLIME_BOUNCE) {
               for (int var37 = var31; var37 < var12.size(); var37++) {
                  var16.add(var37);
               }
            }

            var17.add(
               fill_Set.contains(var59.type())
                  && !var11.probePos(var62).climbable()
                  && !var11.probePos(var62.below()).passable()
            );
            boolean var65 = var59.type() == AiPathfinder.MoveType.SLIME_BOUNCE;
            var18.add(
               var32 == null
                  ? null
                  : (
                     var65
                        ? new AiPaceScript.Inner_JsonEntry(true, var59.from(), var32.yaw(), var35, var32.sprint(), null)
                        : (
                           var32.maneuver() == null
                                 || var11.probePos(var62).climbable()
                                    && (var59.type() != AiPathfinder.MoveType.LADDER_JUMP || var11.putAt_S_I(var62.getX(), var62.getY(), var62.getZ()) != 0)
                              ? null
                              : new AiPaceScript.Inner_JsonEntry(
                                 false,
                                 var59.from(),
                                 var32.yaw(),
                                 var35,
                                 var32.sprint(),
                                 var32.maneuver(),
                                 putAt_List_FileStore_IoHelper(var11, var59.from(), var62)
                              )
                        )
                  )
            );
            var15.add(var62);
            var14.add(var12.size() - 1);
         }

         var13.add(var12.size() - 1);
      }

      boolean[] var45 = new boolean[var12.size()];

      for (int var55 : var16) {
         var45[var55] = true;
      }

      boolean[] var51 = new boolean[var17.size()];

      for (int var56 = 0; var56 < var51.length; var56++) {
         var51[var56] = (Boolean)var17.get(var56);
      }

      boolean[] var57 = new boolean[var12.size()];

      for (int var60 = 1; var60 < var57.length; var60++) {
         double[] var63 = (double[])var12.get(var60 - 1);
         double[] var64 = (double[])var12.get(var60);
         var57[var60] = !var45[var60]
            && var11.probePos(
                  BlockPos.containing((var63[0] + var64[0]) / 2.0, (var63[1] + var64[1]) / 2.0 + 0.01, (var63[2] + var64[2]) / 2.0)
               )
               .climbable();
      }

      AiPaceScript.Inner_JsonCache var61 = AiPaceScript.Inner_JsonCache.ofWithSplitSpeed(
         var12,
         var13,
         var2,
         var15,
         var14,
         var45,
         var51,
         var18.toArray(AiPaceScript.Inner_JsonEntry[]::new),
         var57,
         putAt_D_D(var21 >= 2 ? var3 : var5),
         putAt_D_D(var5),
         true,
         false
      );
      return new AiPaceScript.Inner_TokenCodec(var61, List.copyOf(var41));
   }

   static BlockPos putAt_BlockPos_FileStore_IoHelper(AiPathfinder.MoveGenerator.CachedGrid var0, AiPhysics.MotionState var1, BlockPos var2) {
      if (!var1.onGround()) {
         return var2;
      }

      int var3 = (int)Math.floor(var1.x());
      int var4 = (int)Math.floor(var1.z());
      int var5 = (int)Math.ceil(var1.y() - 0.001);
      BlockPos var6 = new BlockPos(var3, var5 - 2, var4);
      AiPathfinder.CellInfo var7 = var0.probePos(var6);
      if (!var7.passable() && var7.top16() > 16 && Math.abs(AiPathfinder.MoveGenerator.surfaceY(var5 - 1, var7) - var1.y()) <= 0.05) {
         BlockPos var8 = var6.above();
         AiPathfinder.CellInfo var9 = var0.probePos(var8);
         AiPathfinder.CellInfo var10 = var0.probePos(var8.above());
         if ((var9.passable() || var9.climbable()) && !var9.feetTrigger() && var10.passable()) {
            return var8;
         }
      }

      BlockPos var28 = new BlockPos(var3, var5, var4);
      AiPathfinder.CellInfo var29 = var0.probePos(var28);
      AiPathfinder.CellInfo var30 = var0.probePos(var28.above());
      AiPathfinder.CellInfo var11 = var0.probePos(var28.below());
      if (!var29.climbable()
         && (!var29.passable() || !var30.passable() && (var11.passable() || var11.top16() <= 0 || var11.top16() > 3) || var11.passable() || var29.feetTrigger())
         )
       {
         double var12 = 0.299999;
         BlockPos var14 = null;
         double var15 = 1.7976931348623157E308;

         for (int var17 = (int)Math.floor(var1.x() - var12); var17 <= (int)Math.floor(var1.x() + var12); var17++) {
            for (int var18 = (int)Math.floor(var1.z() - var12); var18 <= (int)Math.floor(var1.z() + var12); var18++) {
               BlockPos var19 = new BlockPos(var17, var5, var18);
               AiPathfinder.CellInfo var20 = var0.probePos(var19.below());
               if (!var20.passable()) {
                  boolean var21 = Math.abs(AiPathfinder.MoveGenerator.surfaceY(var5, var20) - var1.y()) <= 0.05
                     || var20.slabStair() && var1.y() >= var19.getY() - 1.0 && var1.y() <= var19.getY() + 0.05;
                  if (var21) {
                     AiPathfinder.CellInfo var22 = var0.probePos(var19);
                     AiPathfinder.CellInfo var23 = var0.probePos(var19.above());
                     if (var22.passable() && (var23.passable() || var20.top16() > 0 && var20.top16() <= 3) && !var22.feetTrigger()) {
                        double var24 = fill_D_D(var17 + 0.5 - var1.x()) + fill_D_D(var18 + 0.5 - var1.z());
                        if (var24 < var15) {
                           var15 = var24;
                           var14 = var19;
                        }
                     }
                  }
               }

               BlockPos var35 = new BlockPos(var17, var5 - 2, var18);
               AiPathfinder.CellInfo var36 = var0.probePos(var35);
               if (!var36.passable() && var36.top16() > 16 && Math.abs(AiPathfinder.MoveGenerator.surfaceY(var5 - 1, var36) - var1.y()) <= 0.05) {
                  BlockPos var37 = var35.above();
                  AiPathfinder.CellInfo var38 = var0.probePos(var37);
                  AiPathfinder.CellInfo var25 = var0.probePos(var37.above());
                  if (var38.passable() && var25.passable() && !var38.feetTrigger()) {
                     double var26 = fill_D_D(var17 + 0.5 - var1.x()) + fill_D_D(var18 + 0.5 - var1.z());
                     if (var26 < var15) {
                        var15 = var26;
                        var14 = var37;
                     }
                  }
               }
            }
         }

         if (var14 != null) {
            return var14;
         }

         if (var5 > (int)Math.floor(var1.y())) {
            BlockPos var31 = var28.below();
            AiPathfinder.CellInfo var32 = var0.probePos(var31);
            AiPathfinder.CellInfo var33 = var0.probePos(var31.above());
            AiPathfinder.CellInfo var34 = var0.probePos(var31.below());
            if (var32.passable() && var33.passable() && !var34.passable() && !var32.feetTrigger()) {
               return var31;
            }
         }

         return var28;
      } else {
         return var28;
      }
   }

   private static BlockPos putAt(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1) {
      AiPathfinder.CellInfo var2 = var0.probePos(var1);
      if (var2.loaded() && !var2.passable() && var2.top16() > 0 && var2.top16() < 16 && !var2.door() && !var2.hazardFloor() && !var2.lava()) {
         AiPathfinder.CellInfo var3 = var0.probePos(var1.above());
         if (var3.loaded() && var3.passable() && !var3.feetTrigger()) {
            return var1.above();
         }
      }

      if (var2.loaded() && var2.passable() && !var2.water() && !var2.climbable()) {
         AiPathfinder.CellInfo var5 = var0.probePos(var1.below());
         AiPathfinder.CellInfo var4 = var0.probePos(var1.below(2));
         if (var5.loaded()
            && var5.passable()
            && !var5.feetTrigger()
            && var4.loaded()
            && !var4.passable()
            && !var4.hazardFloor()
            && !var4.lava()
            && AiPathfinder.MoveGenerator.isStandable(var5, var2, var4)) {
            return var1.below();
         }
      }

      return var1;
   }

   private static AiPathfinder.PathResult putAt(
      AiPathfinder.GridView var0, final BlockPos var1, BlockPos var2, double var3, AiPathfinder.PathOptions var5, BooleanSupplier var6
   ) {
      if (var6.getAsBoolean()) {
         return null;
      }

      AiPathfinder.RouteBlacklist var7 = new AiPathfinder.RouteBlacklist(var0, true, var3, false);

      try {
         var7.startSearch(var1, AiPathfinder.PathGoal.near(var2, 0.6, true), var5);

         while (!var7.awaitResultBlocking(50000000L)) {
            if (var6.getAsBoolean()) {
               return null;
            }
         }

         if (!var6.getAsBoolean() && var7.lastError() == null) {
            var7.tickDriver(new AiPathfinder.SearchTask() {
               @Override
               public boolean isFollowing() {
                  return false;
               }

               @Override
               public int remainingTicks() {
                  return 0;
               }

               @Override
               public BlockPos currentPos() {
                  return var1;
               }
            });
            AiPathfinder.PathResult var8 = var7.pollReplacementPath();
            return var8 != null && var8.reachesGoal() ? var8 : null;
         } else {
            return null;
         }
      } finally {
         var7.close();
      }
   }

   private static List<BlockPos> putAt_List_FileStore_IoHelper(AiPathfinder.MoveGenerator.CachedGrid var0, BlockPos var1, BlockPos var2) {
      if (var1.getY() != var2.getY()) {
         return null;
      }

      int var3 = var2.getX() - var1.getX();
      int var4 = var2.getZ() - var1.getZ();
      if (var3 != 0 && var4 != 0) {
         return null;
      }

      int var5 = Math.abs(var3 + var4);
      if (var5 < 6) {
         return null;
      }

      int var6 = Integer.signum(var3);
      int var7 = Integer.signum(var4);

      for (int var8 = 2; var8 <= 4; var8++) {
         int var9 = var5 - var8;
         if (var9 > 0 && var9 % 4 == 0) {
            ArrayList<BlockPos> var10 = new ArrayList<>();
            BlockPos var11 = var1.offset(var6 * var8, 0, var7 * var8);
            if (var0.probePos(var11).passable() && !var0.probePos(var11.below()).passable()) {
               var10.add(var11);
               boolean var12 = true;
               int var13 = 0;

               while (var13 < var9 / 4) {
                  if (!var0.probePos(var11.offset(var6, 0, var7)).passable()
                     && !var0.probePos(var11.offset(2 * var6, 0, 2 * var7)).passable()
                     && !var0.probePos(var11.offset(3 * var6, 0, 3 * var7)).passable()) {
                     var11 = var11.offset(4 * var6, 0, 4 * var7);
                     if (var0.probePos(var11).passable() && !var0.probePos(var11.below()).passable()) {
                        var10.add(var11);
                        var13++;
                        continue;
                     }

                     var12 = false;
                     break;
                  }

                  var12 = false;
                  break;
               }

               if (var12 && var11.equals(var2)) {
                  return List.copyOf(var10);
               }
            }
         }
      }

      return null;
   }

   public static AiPaceScript.Inner_JsonCache putAt(
      AiPathfinder.GridView var0, AiPhysics.MotionState var1, List<AiPathfinder.PathStep> var2, double var3, boolean var5, boolean var6
   ) {
      return putAt_TextPool_JsonCache_FileStore_TextStor_2(var0, var1, var2, var3, var5, var6, false);
   }

   public static AiPaceScript.Inner_JsonCache putAt_TextPool_JsonCache_FileStore_TextStor_2(
      AiPathfinder.GridView var0,
      AiPhysics.MotionState var1,
      List<AiPathfinder.PathStep> var2,
      double var3,
      boolean var5,
      boolean var6,
      boolean var7
   ) {
      if (var2 != null && !var2.isEmpty()) {
         AiPathfinder.MoveGenerator.CachedGrid var8 = new AiPathfinder.MoveGenerator.CachedGrid(var0, 8192, var3);
         ArrayList var9 = new ArrayList(var2.size() + 2);
         ArrayList var10 = new ArrayList(var2.size());
         ArrayList<BlockPos> var11 = new ArrayList<>(var2.size());
         HashSet<Integer> var12 = new HashSet<>();
         ArrayList<Boolean> var13 = new ArrayList<>(var2.size());
         ArrayList<AiPaceScript.Inner_JsonEntry> var14 = new ArrayList<>(var2.size());
         var9.add(new double[]{var1.x(), var1.y(), var1.z()});
         BlockPos var15 = ((AiPathfinder.PathStep)var2.getFirst()).from();
         if (var1.onGround()
            && !BlockPos.containing(var1.x(), var1.y() + 1.0E-4, var1.z()).equals(var15)
            && Math.hypot(var1.x() - var15.getX() - 0.5, var1.z() - var15.getZ() - 0.5) <= 1.25
            && Math.abs(var1.y() - var15.getY()) <= 0.6) {
            AiPaceScript.Inner_JsonCache.add(var9, var15.getX() + 0.5, var1.y(), var15.getZ() + 0.5, false);
         }

         for (AiPathfinder.PathStep var17 : var2) {
            BlockPos var18 = var17.to();
            int var19 = var9.size();
            AiJumpSolver.JumpPlan var20 = var17.jump();
            double var21 = var8.probePos(var18).climbable()
               ? var18.getY()
               : AiPathfinder.MoveGenerator.surfaceY(var18.getY(), var8.probePos(var18.below()));
            double var23 = var20 != null && !var8.probePos(var18).climbable() && Math.abs(var20.landY() - var21) > 0.25
               ? var20.landY()
               : var21;
            AiPaceScript.Inner_JsonCache.add(var9, var18.getX() + 0.5, var23, var18.getZ() + 0.5, putAt_Set.contains(var17.type()));
            if (var17.type() == AiPathfinder.MoveType.SLIME_BOUNCE) {
               for (int var25 = var19; var25 < var9.size(); var25++) {
                  var12.add(var25);
               }
            }

            var13.add(
               fill_Set.contains(var17.type())
                  && !var8.probePos(var18).climbable()
                  && !var8.probePos(var18.below()).passable()
            );
            boolean var36 = var17.type() == AiPathfinder.MoveType.SLIME_BOUNCE;
            var14.add(
               var20 == null
                  ? null
                  : (
                     var36
                        ? new AiPaceScript.Inner_JsonEntry(true, var17.from(), var20.yaw(), var23, var20.sprint(), null)
                        : (
                           var20.maneuver() == null
                                 || var8.probePos(var18).climbable()
                                    && (var17.type() != AiPathfinder.MoveType.LADDER_JUMP || var8.putAt_S_I(var18.getX(), var18.getY(), var18.getZ()) != 0)
                              ? null
                              : new AiPaceScript.Inner_JsonEntry(
                                 false,
                                 var17.from(),
                                 var20.yaw(),
                                 var23,
                                 var20.sprint(),
                                 var20.maneuver(),
                                 putAt_List_FileStore_IoHelper(var8, var17.from(), var18)
                              )
                        )
                  )
            );
            var11.add(var18);
            var10.add(var9.size() - 1);
         }

         boolean[] var26 = new boolean[var9.size()];

         for (int var29 : var12) {
            var26[var29] = true;
         }

         boolean[] var28 = new boolean[var13.size()];

         for (int var30 = 0; var30 < var28.length; var30++) {
            var28[var30] = (Boolean)var13.get(var30);
         }

         boolean[] var31 = new boolean[var9.size()];

         for (int var32 = 1; var32 < var31.length; var32++) {
            double[] var34 = (double[])var9.get(var32 - 1);
            double[] var35 = (double[])var9.get(var32);
            var31[var32] = !var26[var32]
               && var8.probePos(
                     BlockPos.containing((var34[0] + var35[0]) / 2.0, (var34[1] + var35[1]) / 2.0 + 0.01, (var34[2] + var35[2]) / 2.0)
                  )
                  .climbable();
         }

         BlockPos var33 = ((AiPathfinder.PathStep)var2.getLast()).to();
         return AiPaceScript.Inner_JsonCache.of(
            var9,
            List.of(var9.size() - 1),
            List.of(var33),
            var11,
            var10,
            var26,
            var28,
            var14.toArray(AiPaceScript.Inner_JsonEntry[]::new),
            var31,
            putAt_D_D(var3, var5, var6),
            false,
            var7
         );
      } else {
         return null;
      }
   }

   static boolean putAt_Z_TokenCore_TaskStat_2(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      double var3 = 0.2999999;
      return !var0.boxCollides(var2.x() - var3, var2.y() + 1.0E-7, var2.z() - var3, var2.x() + var3, var2.y() + 1.8 - 1.0E-7, var2.z() + var3)
         ? false
         : !var0.boxCollides(var1.x() - var3, var1.y() + 1.0E-7, var1.z() - var3, var1.x() + var3, var1.y() + 1.8 - 1.0E-7, var1.z() + var3);
   }

   private static float[] putAt(float[] var0, float[] var1) {
      float[] var2 = Arrays.copyOf(var0, var0.length + var1.length);
      System.arraycopy(var1, 0, var2, var0.length, var1.length);
      return var2;
   }

   static AiPhysics.MoveInput putAt(float var0, AiPhysics.MotionState var1) {
      double var2 = Math.hypot(var1.vx(), var1.vz());
      if (var2 < 0.001) {
         return AiPhysics.MoveInput.NONE.withYaw(var0);
      }

      double var4 = Math.toRadians(var0);
      double var6 = Math.sin(var4);
      double var8 = Math.cos(var4);
      int var10 = 0;
      int var11 = 0;
      double var12 = 0.0;

      for (int var14 = -1; var14 <= 1; var14++) {
         for (int var15 = -1; var15 <= 1; var15++) {
            if (var14 != 0 || var15 != 0) {
               double var16 = -var6 * var14 + var8 * var15;
               double var18 = var8 * var14 + var6 * var15;
               double var20 = Math.hypot(var16, var18);
               double var22 = -(var16 * var1.vx() + var18 * var1.vz()) / (var20 * var2);
               if (var22 > var12) {
                  var12 = var22;
                  var10 = var14;
                  var11 = var15;
               }
            }
         }
      }

      return new AiPhysics.MoveInput(var10, var11, false, false, false, var0);
   }

   private static boolean putAt_Z_TextPool_TaskState(AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_TaskState var1, int var2, double var3) {
      return var0.flowTemp == var2 || var0 != var1 && var0.putAt_D >= var3 && var0.prevEx <= 2.0;
   }

   private static List<AiPaceScript.Inner_TaskState> putAt(
      AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_JsonCache var1, AiPaceScript.Inner_TextState var2, AiPhysics.PhysicsWorld var3, LongAdder var4, boolean var5
   ) {
      return putAt_List_TextPool_TaskState(var0, var1, var2, var3, var4, var5, null, AiPaceScript.Inner_HashPool.UNBOUNDED);
   }

   private static List<AiPaceScript.Inner_TaskState> putAt_List_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_JsonCache var1,
      AiPaceScript.Inner_TextState var2,
      AiPhysics.PhysicsWorld var3,
      LongAdder var4,
      boolean var5,
      AiPaceScript.Inner_RingUtil var6,
      AiPaceScript.Inner_HashPool var7
   ) {
      ArrayList var8 = new ArrayList();
      AiPaceScript.Inner_TableCache var9 = putAt_ThreadLocal.get();
      boolean var10 = !var5 && var7.fastTravel();
      float var11 = fill(var0.putAt_TokenCore_TableCac, var0.putAt_D, var1, var0.putAt_F);
      float[] var12 = var5 ? value_FArr : (var10 ? fill_FArr : putAt_FArr);
      boolean var13 = onClimbable(var3, var0.putAt_TokenCore_TableCac);
      boolean var14 = putAt_Z_TextPool_TaskState(var0, var1, var3);
      int[] var15 = var10 ? (var14 ? chunkAt_IArr : prev_IArr) : prevEx_IArr;
      boolean var16 = var0.putAt_TokenCore_TableCac.onGround() && Math.hypot(var0.putAt_TokenCore_TableCac.vx(), var0.putAt_TokenCore_TableCac.vz()) > 0.02;
      boolean var17 = var13
         && AiLadderJump.canClimbAt(var3, BlockPos.containing(var0.putAt_TokenCore_TableCac.x(), var0.putAt_TokenCore_TableCac.y(), var0.putAt_TokenCore_TableCac.z()));
      boolean var18 = var13 && Math.hypot(var0.putAt_TokenCore_TableCac.vx(), var0.putAt_TokenCore_TableCac.vz()) > 0.02;
      boolean var19 = false;

      for (int var20 = Math.max(0, var0.flowState); var20 <= var0.flowState + 4 && !var19; var20++) {
         var19 = var1.freeSegment(var20);
      }

      int var36 = var1.checkpoints().length;
      boolean var21 = fill(var0, var1, var3);
      boolean var22 = !var0.putAt_TokenCore_TableCac.onGround() || var0.putAt_Z;
      boolean var23 = var22 && (var2.allowHops() || var14);
      AiPhysics.PhysicsWorld var24 = putAt_TokenCore_TaskStat_TokenCore_TaskStat(var3, var0.flowTemp, var7.speedProfile());
      if (var0.putAt_TokenCore_TableCac.onGround()
         && !var13
         && (!var0.putAt_Z || var0.putAt_TextPool_TaskState == null && prevEx(var24, var0.putAt_TokenCore_TableCac))) {
         putAt(var0, var1, var2, var3, var24, var4, var9, var8, var7);
      }

      for (AiPaceScript.Inner_TextStore var26 : putAt(var0, var1, var24, var6, var5, var2.maxTurn())) {
         var var27 = putAt_List_TextPool_TaskState_2(var0, var26.keys(), var2.maxTurn());
         AiPaceScript.Inner_TaskState var28 = putAt_TextPool_TaskState_TextPool_TaskState(
            var0, AiPaceScript.Inner_RingBuffer.keyed(var27), var1, var2, var3, var4, var9, var7
         );
         if (var28 != null && (var28.flowTemp == var36 || putAt_Z_TokenCore_TaskStat_3(var3, var28.putAt_TokenCore_TableCac, var26.target()))) {
            var8.add(var28);
         }
      }

      for (float var40 : var12) {
         float var29 = var11 + var40;
         putAt(var0, var29, 0, false, 0, var15, var1, var2, var3, var4, var9, var8, var19, var7);
         if (var40 == 0.0F) {
            for (int var33 : value_IArr) {
               putAt(var0, var29, var33, false, 0, var15, var1, var2, var3, var4, var9, var8, var19, var7);
            }
         }

         double var41 = Math.abs(Mth.wrapDegrees(var29 - var0.putAt_F));
         if (var23 && var41 <= var2.maxTurn()) {
            putAt(var0, var29, var40, var5, var10, var1, var2, var3, var4, var9, var8, var19, var7);
         } else if ((var5 || var40 == 0.0F && var0.putAt_TokenCore_TableCac.speed() < 0.02)
            && var0.putAt_TokenCore_TableCac.onGround()
            && var41 > var2.maxTurn()) {
            putAt(
               var0,
               new AiPaceScript.Inner_RingBuffer(var29, (int)Math.ceil(var41 / var2.maxTurn()), false, 2, 0, 0, 0, false, 1, 0, 0),
               var1,
               var2,
               var3,
               var4,
               var9,
               var8,
               var19,
               var7
            );
         }

         if (var13) {
            putAt(var0, var29, 0, true, 0, state_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
            if (Math.abs(var40) <= 20.0F) {
               for (int var35 : value_IArr) {
                  putAt(var0, var29, var35, true, 0, state_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
               }
            }

            if (var40 == 0.0F) {
               if (var17 || var5) {
                  putAt(var0, var29, 0, true, 1, state_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
                  if (var18) {
                     putAt(var0, var29, 0, true, 2, pollOf_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
                  }
               }

               putAt(var0, var29, 0, false, 1, state_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
            }
         }

         if ((var5 || var13) && Math.abs(var40) <= (var5 ? 20.0F : 20.0F) && (var0.putAt_TokenCore_TableCac.onGround() || var13)) {
            putAt(var0, var29, 0, false, 3, closeOf_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
            if ((var17 || var5) && var13 && var40 == 0.0F) {
               for (int var48 : value_IArr) {
                  putAt(var0, var29, var48, false, 3, closeOf_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
               }
            }
         }

         boolean var44 = var1.centerGoal() && var1.length() - var0.putAt_D <= 3.0;
         if ((var5 || var21 || var44) && var40 == 0.0F && var16) {
            putAt(var0, var29, 0, false, 2, pollOf_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
            if ((var21 || var44) && !var5) {
               putAt(var0, var29, 0, false, 1, pollOf_IArr, var1, var2, var3, var4, var9, var8, var19, var7);
            }
         }
      }

      return var8;
   }

   private static List<AiPhysics.MoveInput> putAt_List_TextPool_TaskState_2(
      AiPaceScript.Inner_TaskState var0, List<AiPhysics.MoveInput> var1, float var2
   ) {
      if (var1 != null && !var1.isEmpty() && var0.putAt_TokenCore_TableCac.onGround() && !(var0.putAt_TokenCore_TableCac.speed() >= 0.02) && !(var2 <= 0.0F)) {
         float var3 = ((AiPhysics.MoveInput)var1.getFirst()).yaw();
         float var4 = Math.abs(Mth.wrapDegrees(var3 - var0.putAt_F));
         if (var4 <= var2 + 0.001F) {
            return var1;
         }

         int var5 = Math.min(8, (int)Math.ceil((var4 - var2) / var2));
         ArrayList<AiPhysics.MoveInput> var6 = new ArrayList<>(var5 + var1.size());
         float var7 = var0.putAt_F;

         for (int var8 = 0; var8 < var5; var8++) {
            var7 = putAt_F_F(var7, var3, var2);
            var6.add(AiPhysics.MoveInput.NONE.withYaw(var7));
         }

         var6.addAll(var1);
         return var6;
      } else {
         return var1;
      }
   }

   private static boolean prevEx(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      if (!var1.onGround()) {
         return false;
      } else {
         return !putAt_Z_TokenCore_TaskStat(var0, var1, 0.14)
            ? true
            : !var0.boxCollides(var1.x() - 1.0E-4, var1.y() - 0.02, var1.z() - 1.0E-4, var1.x() + 1.0E-4, var1.y() - 1.0E-4, var1.z() + 1.0E-4);
      }
   }

   private static void putAt(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_JsonCache var1,
      AiPaceScript.Inner_TextState var2,
      AiPhysics.PhysicsWorld var3,
      AiPhysics.PhysicsWorld var4,
      LongAdder var5,
      AiPaceScript.Inner_TableCache var6,
      List<AiPaceScript.Inner_TaskState> var7,
      AiPaceScript.Inner_HashPool var8
   ) {
      AiPhysics.MotionState var9 = var0.putAt_TokenCore_TableCac;
      double var10 = 0.299999;
      double var12 = 0.0 / 0.0;
      double var14 = 0.0 / 0.0;
      double var16 = 1.7976931348623157E308;

      for (int var18 = (int)Math.floor(var9.x() - var10); var18 <= (int)Math.floor(var9.x() + var10); var18++) {
         for (int var19 = (int)Math.floor(var9.z() - var10); var19 <= (int)Math.floor(var9.z() + var10); var19++) {
            if (var4.boxCollides(var18 + 0.01, var9.y() - 0.02, var19 + 0.01, var18 + 0.99, var9.y() - 1.0E-4, var19 + 0.99)) {
               double var20 = var18 + 0.5;
               double var22 = var19 + 0.5;
               if (AiJumpSolver.canStandAt(var4, var20, var9.y(), var22)) {
                  double var24 = fill_D_D(var20 - var9.x()) + fill_D_D(var22 - var9.z());
                  if (var24 < var16) {
                     var16 = var24;
                     var12 = var20;
                     var14 = var22;
                  }
               }
            }
         }
      }

      if (!Double.isNaN(var12) && !(var16 < 0.0016)) {
         float var33 = var2.maxTurn();
         float var34 = var0.putAt_F;
         AiPhysics.MotionState var35 = var9;
         ArrayList<AiPhysics.MoveInput> var21 = new ArrayList<>(24);
         float var36 = AiPhysics.yawBetween(var35.x(), var35.z(), var12, var14);

         for (int var23 = 0; var23 < 12 && Math.abs(Mth.wrapDegrees(var36 - var34)) > var33 + 0.001F; var23++) {
            var34 = putAt_F_F(var34, var36, var33);
            AiPhysics.MoveInput var40 = var35.speed() > 0.01 ? putAt(var34, var35) : AiPhysics.MoveInput.NONE.withYaw(var34);
            AiPhysics.MotionState var41 = AiPhysics.stepWithClimbable(var35, var40, var4);
            if (!var41.onGround() || Math.abs(var41.y() - var9.y()) > 1.0E-4) {
               return;
            }

            var21.add(var40);
            var35 = var41;
            var36 = AiPhysics.yawBetween(var35.x(), var35.z(), var12, var14);
         }

         boolean var38 = false;
         boolean var39 = false;
         double var25 = Math.hypot(var35.x() - var12, var35.z() - var14);

         for (int var27 = 0; var27 < 16 && var25 > 0.2; var27++) {
            var36 = AiPhysics.yawBetween(var35.x(), var35.z(), var12, var14);
            var34 = putAt_F_F(var34, var36, var33);
            AiPhysics.MoveInput var28 = new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34);
            AiPhysics.MotionState var29 = AiPhysics.stepWithClimbable(var35, var28, var4);
            if (!var29.onGround() || Math.abs(var29.y() - var9.y()) > 1.0E-4) {
               var28 = new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var34);
               var29 = AiPhysics.stepWithClimbable(var35, var28, var4);
               if (!var29.onGround() || Math.abs(var29.y() - var9.y()) > 1.0E-4) {
                  break;
               }
            }

            var21.add(var28);
            var35 = var29;
            double var30 = Math.hypot(var35.x() - var12, var35.z() - var14);
            if (!var38 && putAt_Z_TokenCore_TaskStat(var4, var35, 0.04)) {
               var38 = true;
               AiPaceScript.Inner_TaskState var32 = putAt_TextPool_TaskState_TextPool_TaskState(
                  var0, AiPaceScript.Inner_RingBuffer.keyed(List.copyOf(var21)), var1, var2, var3, var5, var6, var8
               );
               if (var32 != null) {
                  var7.add(var32);
               }
            }

            if (!var39 && putAt_Z_TokenCore_TaskStat(var4, var35, 0.12) && var30 <= 0.45) {
               var39 = true;
               AiPaceScript.Inner_TaskState var46 = putAt_TextPool_TaskState_TextPool_TaskState(
                  var0, AiPaceScript.Inner_RingBuffer.keyed(List.copyOf(var21)), var1, var2, var3, var5, var6, var8
               );
               if (var46 != null) {
                  var7.add(var46);
               }
            }

            if (var30 > var25 + 0.02) {
               break;
            }

            var25 = var30;
         }

         if (!var21.isEmpty()) {
            for (int var42 = 0; var42 < 8 && var35.speed() >= 0.015; var42++) {
               AiPhysics.MoveInput var44 = var35.speed() > 0.03 ? putAt(var34, var35) : AiPhysics.MoveInput.NONE.withYaw(var34);
               AiPhysics.MotionState var45 = AiPhysics.stepWithClimbable(var35, var44, var4);
               if (!var45.onGround() || Math.abs(var45.y() - var9.y()) > 1.0E-4) {
                  break;
               }

               var21.add(var44);
               var35 = var45;
            }

            AiPaceScript.Inner_TaskState var43 = putAt_TextPool_TaskState_TextPool_TaskState(
               var0, AiPaceScript.Inner_RingBuffer.keyed(List.copyOf(var21)), var1, var2, var3, var5, var6, var8
            );
            if (var43 != null) {
               var7.add(var43);
            }
         }
      }
   }

   private static void putAt(
      AiPaceScript.Inner_TaskState var0,
      float var1,
      float var2,
      boolean var3,
      boolean var4,
      AiPaceScript.Inner_JsonCache var5,
      AiPaceScript.Inner_TextState var6,
      AiPhysics.PhysicsWorld var7,
      LongAdder var8,
      AiPaceScript.Inner_TableCache var9,
      List<AiPaceScript.Inner_TaskState> var10,
      boolean var11,
      AiPaceScript.Inner_HashPool var12
   ) {
      AiPaceScript.Inner_SyncPool var13 = var9.putAt_TextPool_SyncPool;
      AiPaceScript.Inner_RingBuffer var14 = new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, 0, 0, 0);
      AiPaceScript.Inner_TaskState var15 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var14, null, 0, var13, 1, var5, var6, var7, var8, var9, var12);
      if (var15 != null) {
         var10.add(var15);
      }

      if (var11 && var9.fill_Z) {
         AiPaceScript.Inner_TaskState var16 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var14.bouncing(), var5, var6, var7, var8, var9, var12);
         if (var16 != null) {
            var10.add(var16);
         }
      }

      if (var13.putAt_Z && var13.flowTemp >= 0) {
         int var33 = var13.readEx_I;
         int var17 = var13.flowTemp;
         boolean var18 = var11 && var13.readEx_ZArr[var13.flowState];
         long var19 = var13.putAt_J;
         int var21 = -1;
         if (var19 != 0L || var18) {
            int var22 = var19 != 0L ? Long.numberOfTrailingZeros(var19) + 1 : 1;
            int var23 = var18 ? var17 : var17 + var22 - 1;
            if (!var18) {
               var21 = var22;
            }

            putAt_V_TextPool_TaskState(
               var0, new AiPaceScript.Inner_RingBuffer(var1, 0, true, 1, 0, 0, 0), var13, var23, var5, var6, var7, var8, var9, var10, var11, var12
            );
         }

         if (var4) {
            boolean var40 = var15 != null && var15.prevEx <= 0.85 && var15.putAt_D > var0.putAt_D + 1.0;
            if (!var40) {
               putAt_V_TextPool_TaskState(
                  var0, new AiPaceScript.Inner_RingBuffer(var1, 0, true, 2, 0, 0, 0), var13, var17, var5, var6, var7, var8, var9, var10, var11, var12
               );

               for (int var65 : fill_IArr) {
                  if (var65 < var33) {
                     putAt_V_TextPool_TaskState(
                        var0,
                        new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, var65, 0, 0),
                        var13,
                        var17 + var65,
                        var5,
                        var6,
                        var7,
                        var8,
                        var9,
                        var10,
                        var11,
                        var12
                     );
                  }
               }

               if (3 < var33) {
                  long var48 = var19 & -8L;
                  if (var48 != 0L || var18) {
                     int var61 = var48 != 0L ? Long.numberOfTrailingZeros(var48) + 1 : 4;
                     if (var18 || var61 != var21) {
                        int var66 = var18 ? var17 + 3 : var17 + var61 - 1;
                        putAt_V_TextPool_TaskState(
                           var0, new AiPaceScript.Inner_RingBuffer(var1, 0, true, 1, 0, 0, 3), var13, var66, var5, var6, var7, var8, var9, var10, var11, var12
                        );
                     }
                  }
               }
            }
         } else {
            putAt_V_TextPool_TaskState(
               var0, new AiPaceScript.Inner_RingBuffer(var1, 0, true, 2, 0, 0, 0), var13, var17, var5, var6, var7, var8, var9, var10, var11, var12
            );

            for (int var25 : putAt_IArr) {
               if (var25 < var33) {
                  putAt_V_TextPool_TaskState(
                     var0,
                     new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, var25, 0, 0),
                     var13,
                     var17 + var25,
                     var5,
                     var6,
                     var7,
                     var8,
                     var9,
                     var10,
                     var11,
                     var12
                  );
               }
            }

            for (int var55 : readEx_IArr) {
               if (var55 < var33) {
                  long var26 = var19 & -1L << var55;
                  if (var26 != 0L || var18) {
                     int var28 = var26 != 0L ? Long.numberOfTrailingZeros(var26) + 1 : var55 + 1;
                     if (var18 || var28 != var21) {
                        if (!var18) {
                           var21 = var28;
                        }

                        int var29 = var18 ? var17 + var55 : var17 + var28 - 1;
                        putAt_V_TextPool_TaskState(
                           var0, new AiPaceScript.Inner_RingBuffer(var1, 0, true, 1, 0, 0, var55), var13, var29, var5, var6, var7, var8, var9, var10, var11, var12
                        );
                     }
                  }
               }
            }

            if (!var3 && Math.abs(var2) <= 8.0F) {
               for (int var56 : value_IArr) {
                  putAt_V_TextPool_TaskState(
                     var0,
                     new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, 0, 0, 0, false, 0, var56, 0),
                     var13,
                     var17,
                     var5,
                     var6,
                     var7,
                     var8,
                     var9,
                     var10,
                     var11,
                     var12
                  );
               }
            }

            if (!var3 && Math.abs(var2) > 8.0F) {
               int var37 = var2 > 0.0F ? 1 : -1;
               boolean var44 = var33 > 3;
               AiPaceScript.Inner_SyncPool var51 = var44 ? var9.fill_TextPool_SyncPool : null;
               AiPaceScript.Inner_RingBuffer var57 = new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, 0, 0, 0, false, 0, var37, 0);
               AiPaceScript.Inner_TaskState var62 = putAt_TextPool_TaskState_TextPool_TaskState(
                  var0, var57, var13, var17, var51, var44 ? 2 : 0, var5, var6, var7, var8, var9, var12
               );
               if (var62 != null) {
                  var10.add(var62);
               }

               if (var11 && var9.fill_Z) {
                  AiPaceScript.Inner_TaskState var27 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var57.bouncing(), var5, var6, var7, var8, var9, var12);
                  if (var27 != null) {
                     var10.add(var27);
                  }
               }

               if (var44 && var51.putAt_Z && var51.readEx_I > 3) {
                  long var67 = var51.putAt_J & -8L;
                  boolean var72 = var11 && var51.readEx_ZArr[var51.flowState];
                  if (var67 != 0L || var72) {
                     int var30 = var67 != 0L ? Long.numberOfTrailingZeros(var67) + 1 : 4;
                     int var31 = var72 ? var51.flowTemp + 3 : var51.flowTemp + var30 - 1;
                     putAt_V_TextPool_TaskState(
                        var0,
                        new AiPaceScript.Inner_RingBuffer(var1, 0, true, 1, 0, 0, 3, false, 0, var37, 0),
                        var51,
                        var31,
                        var5,
                        var6,
                        var7,
                        var8,
                        var9,
                        var10,
                        var11,
                        var12
                     );
                  }
               }
            }

            if (var3 && Math.abs(var2) <= 40.0F) {
               for (int var58 : value_IArr) {
                  AiPaceScript.Inner_SyncPool var63 = var58 < 0 ? var9.fill_TextPool_SyncPool : var9.readEx_TextPool_SyncPool;
                  AiPaceScript.Inner_RingBuffer var68 = new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, 0, 0, 0, false, 0, var58, 0);
                  AiPaceScript.Inner_TaskState var70 = putAt_TextPool_TaskState_TextPool_TaskState(
                     var0, var68, var13, var17, var63, 0, var5, var6, var7, var8, var9, var12
                  );
                  if (var70 != null) {
                     var10.add(var70);
                  }

                  if (var11 && var9.fill_Z) {
                     AiPaceScript.Inner_TaskState var73 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var68.bouncing(), var5, var6, var7, var8, var9, var12);
                     if (var73 != null) {
                        var10.add(var73);
                     }
                  }
               }

               for (int var39 = -1; var39 <= 1; var39++) {
                  AiPaceScript.Inner_SyncPool var46 = var39 == 0 ? var13 : (var39 < 0 ? var9.fill_TextPool_SyncPool : var9.readEx_TextPool_SyncPool);
                  if (var46.putAt_Z && var46.readEx_I > 0 && var46.flowTemp >= 0) {
                     for (int var69 : tick_IArr) {
                        if (var69 < var46.readEx_I) {
                           int var71 = var46.flowTemp + var69;

                           for (int var32 : flushOf_IArr) {
                              putAt_V_TextPool_TaskState(
                                 var0,
                                 new AiPaceScript.Inner_RingBuffer(var1, 0, true, 0, var69, 0, 0, false, 0, var39, var32),
                                 var46,
                                 var71,
                                 var5,
                                 var6,
                                 var7,
                                 var8,
                                 var9,
                                 var10,
                                 var11,
                                 var12
                              );
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static boolean putAt_Z_TextPool_TaskState(AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_JsonCache var1, AiPhysics.PhysicsWorld var2) {
      if (onClimbable(var2, var0.putAt_TokenCore_TableCac)) {
         return true;
      }

      double var3 = var0.putAt_TokenCore_TableCac.y();
      int var5 = Math.max(0, var0.flowState);
      int var6 = Math.min(var1.segments(), var5 + 4);

      for (int var7 = var5; var7 < var6; var7++) {
         if (var1.freeSegment(var7)) {
            return true;
         }

         if (var1.y()[var7 + 1] - var3 > 0.65) {
            return true;
         }

         double var8 = var1.x()[var7 + 1] - var1.x()[var7];
         double var10 = var1.z()[var7 + 1] - var1.z()[var7];
         if (var8 * var8 + var10 * var10 > 2.25) {
            return true;
         }
      }

      double[] var18 = var1.at(var0.putAt_D + 1.2);
      double var19 = var18[0] - var0.putAt_TokenCore_TableCac.x();
      double var20 = var18[2] - var0.putAt_TokenCore_TableCac.z();
      double var12 = Math.hypot(var19, var20);
      if (var12 > 0.3) {
         double var14 = var0.putAt_TokenCore_TableCac.x() + var19 / var12 * 0.75;
         double var16 = var0.putAt_TokenCore_TableCac.z() + var20 / var12 * 0.75;
         if (!putAt_Z_TokenCore_TaskStat(var2, var14, var3, var16, 0.0)) {
            return true;
         }
      }

      return false;
   }

   private static boolean fill(AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_JsonCache var1, AiPhysics.PhysicsWorld var2) {
      AiPhysics.MotionState var3 = var0.putAt_TokenCore_TableCac;
      if (var3.onGround() && !(var3.vy() > 0.1) && !onClimbable(var2, var3)) {
         int[] var4 = var1.cellPoint();
         int var5 = 0;
         int var6 = var4.length;

         while (var5 < var6) {
            int var7 = var5 + var6 >>> 1;
            if (var4[var7] < var0.flowState - 1) {
               var5 = var7 + 1;
            } else {
               var6 = var7;
            }
         }

         for (int var11 = var5; var11 < var4.length && var4[var11] <= var0.flowState + 4 + 2; var11++) {
            AiPaceScript.Inner_JsonEntry var8 = var1.planned()[var11];
            if (var8 != null) {
               BlockPos var9 = var8.from();
               if (!var2.climbableAt(var9.getX(), var9.getY(), var9.getZ())) {
                  BlockPos var10 = var1.cells()[var11];
                  if (!var2.climbableAt(var10.getX(), var10.getY(), var10.getZ())
                     && Math.hypot(var3.x() - var9.getX() - 0.5, var3.z() - var9.getZ() - 0.5) <= 1.5
                     && Math.abs(var3.y() - var9.getY()) <= 0.6) {
                     return true;
                  }
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static void putAt(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_RingBuffer var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPhysics.PhysicsWorld var4,
      LongAdder var5,
      AiPaceScript.Inner_TableCache var6,
      List<AiPaceScript.Inner_TaskState> var7,
      boolean var8,
      AiPaceScript.Inner_HashPool var9
   ) {
      AiPaceScript.Inner_TaskState var10 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var1, var2, var3, var4, var5, var6, var9);
      if (var10 != null) {
         var7.add(var10);
      }

      if (var8 && var6.fill_Z && var1.air() != 2) {
         AiPaceScript.Inner_TaskState var11 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var1.bouncing(), var2, var3, var4, var5, var6, var9);
         if (var11 != null) {
            var7.add(var11);
         }
      }
   }

   private static void putAt_V_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_RingBuffer var1,
      AiPaceScript.Inner_SyncPool var2,
      int var3,
      AiPaceScript.Inner_JsonCache var4,
      AiPaceScript.Inner_TextState var5,
      AiPhysics.PhysicsWorld var6,
      LongAdder var7,
      AiPaceScript.Inner_TableCache var8,
      List<AiPaceScript.Inner_TaskState> var9,
      boolean var10,
      AiPaceScript.Inner_HashPool var11
   ) {
      AiPaceScript.Inner_TaskState var12 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var1, var2, var3, null, 0, var4, var5, var6, var7, var8, var11);
      if (var12 != null) {
         var9.add(var12);
      }

      if (var10 && var8.fill_Z && var1.air() != 2) {
         AiPaceScript.Inner_TaskState var13 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var1.bouncing(), var4, var5, var6, var7, var8, var11);
         if (var13 != null) {
            var9.add(var13);
         }
      }
   }

   private static List<AiPaceScript.Inner_TextStore> putAt_List_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_JsonCache var1, AiPhysics.PhysicsWorld var2
   ) {
      return putAt(var0, var1, var2, null, false, 180.0F);
   }

   private static List<AiPaceScript.Inner_TextStore> putAt(
      AiPaceScript.Inner_TaskState var0, AiPaceScript.Inner_JsonCache var1, AiPhysics.PhysicsWorld var2, AiPaceScript.Inner_RingUtil var3, boolean var4, float var5
   ) {
      AiPhysics.MotionState var6 = var0.putAt_TokenCore_TableCac;
      boolean var7 = onClimbable(var2, var6);
      if (var7 || var6.onGround() && !(var6.vy() > 0.1)) {
         ArrayList var8 = null;
         int[] var9 = var1.cellPoint();
         int var10 = 0;
         int var11 = var9.length;

         while (var10 < var11) {
            int var12 = var10 + var11 >>> 1;
            if (var9[var12] < var0.flowState - 1) {
               var10 = var12 + 1;
            } else {
               var11 = var12;
            }
         }

         for (int var28 = var10; var28 < var9.length && var9[var28] <= var0.flowState + 4 + 2; var28++) {
            AiPaceScript.Inner_JsonEntry var13 = var1.planned()[var28];
            if (var13 != null) {
               BlockPos var14 = var13.from();
               boolean var15 = var2.climbableAt(var14.getX(), var14.getY(), var14.getZ());
               if (var7 == var15) {
                  double var16 = var13.bounce() ? 1.05 : 1.5;
                  if (!(Math.hypot(var6.x() - var14.getX() - 0.5, var6.z() - var14.getZ() - 0.5) > var16)
                     && (var7 ? !(var6.y() < var14.getY() - 0.2) && !(var6.y() > var14.getY() + 1.1) : !(Math.abs(var6.y() - var14.getY()) > 0.6))) {
                     BlockPos var18 = var1.cells()[var28];
                     List var19 = null;
                     if (var13.bounce()) {
                        long var20 = var3 != null ? AiPaceScript.Inner_RingUtil.putAt(var28, var6, var2.movementSpeed()) : 0L;
                        List var22 = var3 != null ? var3.putAt_ConcurrentHashMap.get(var20) : null;
                        if (var22 != null) {
                           var19 = var22;
                        } else if (var3 == null || var3.readEx_AtomicIntegerArr[var28].getAndIncrement() < 128) {
                           for (AiSlimeBounce.Inner_JsonCache var24 : AiSlimeBounce.findBounces(var2, var6, var13.yaw(), var18, true)) {
                              if (var24.plan().maneuver() != null) {
                                 var19 = var24.plan().maneuver().inputs();
                              }
                           }

                           if (var3 != null) {
                              var3.putAt_ConcurrentHashMap.putIfAbsent(var20, var19 != null ? var19 : AiPaceScript.Inner_RingUtil.putAt_List);
                           }
                        }
                     } else if (var7) {
                        if (!var4) {
                           continue;
                        }

                        boolean var29 = var2.climbableAt(var18.getX(), var18.getY(), var18.getZ());
                        AiJumpManeuver var21 = var13.maneuver();
                        if (var21 == null
                           || (
                              var29
                                 ? !AiLadderJump.putAt_Z_TokenCore_TaskStat(var2, var6, var21, 0, var18)
                                 : !AiLadderJump.putAt_Z_TokenCore_TaskStat(var2, var6, var21, 0, var18, var13.surface())
                           )) {
                           long var34 = var3 != null ? AiPaceScript.Inner_RingUtil.putAt(var28, var6, var2.movementSpeed()) : 0L;
                           List var38 = var3 != null ? var3.putAt_ConcurrentHashMap.get(var34) : null;
                           if (var38 != null) {
                              var19 = var38;
                           } else if (var3 == null || var3.putAt_AtomicIntegerArr[var28].getAndIncrement() < 16) {
                              AiJumpSolver.JumpPlan var25 = var29
                                 ? AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat_2(var2, var6, var14, var18, var13.sprint())
                                 : AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var2, var6, var13.yaw(), var18, var13.surface());
                              if (var25 != null && var25.maneuver() != null) {
                                 var19 = var25.maneuver().inputs();
                              }

                              if (var3 != null) {
                                 var3.putAt_ConcurrentHashMap.putIfAbsent(var34, var19 != null ? var19 : AiPaceScript.Inner_RingUtil.putAt_List);
                              }
                           }
                        } else {
                           var19 = var21.inputs();
                        }
                     } else if (var2.climbableAt(var18.getX(), var18.getY(), var18.getZ())) {
                        if (!var4 && !AiLadderJump.canClimbAt(var2, var18)) {
                           continue;
                        }

                        AiJumpManeuver var30 = var13.maneuver();
                        if (var30 != null && AiLadderJump.putAt_Z_TokenCore_TaskStat(var2, var6, var30, 0, var18)) {
                           var19 = var30.inputs();
                        } else {
                           long var32 = var3 != null ? AiPaceScript.Inner_RingUtil.putAt(var28, var6, var2.movementSpeed()) : 0L;
                           List var36 = var3 != null ? var3.putAt_ConcurrentHashMap.get(var32) : null;
                           if (var36 != null) {
                              var19 = var36;
                           } else if (var3 == null || var3.putAt_AtomicIntegerArr[var28].getAndIncrement() < 16) {
                              AiJumpSolver.JumpPlan var39 = AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var2, var6, var18, var13.sprint());
                              if (var39 != null && var39.maneuver() != null) {
                                 var19 = var39.maneuver().inputs();
                              }

                              if (var3 != null) {
                                 var3.putAt_ConcurrentHashMap.putIfAbsent(var32, var19 != null ? var19 : AiPaceScript.Inner_RingUtil.putAt_List);
                              }
                           }
                        }
                     } else {
                        AiJumpManeuver var31 = var13.maneuver();
                        AiJumpManeuver var33 = null;
                        if (var31 != null
                           && Math.abs(var6.y() - var31.start().y()) <= 0.05
                           && Math.hypot(var6.x() - var31.start().x(), var6.z() - var31.start().z()) <= 0.45
                           && var31.replay(var2, var6, 0, var18, var13.surface()) >= -0.04) {
                           var33 = var31;
                        } else {
                           float var35 = var13.wallTargets() != null && var6.speed() >= 0.02 ? var0.putAt_F : 0.0F / 0.0F;
                           long var37 = var3 != null
                              ? AiPaceScript.Inner_RingUtil.putAt(var28, var6, var2.movementSpeed())
                                 ^ (Float.isNaN(var35) ? 0L : Math.round(var35 / 15.0F) * -7046029254386353131L)
                              : 0L;
                           List var40 = var3 != null ? var3.putAt_ConcurrentHashMap.get(var37) : null;
                           if (var40 != null) {
                              var19 = var40;
                           } else {
                              if (var13.wallTargets() != null) {
                                 boolean var26 = var6.speed() < 0.02;
                                 if (var6.pose() == 0
                                    && Math.abs(var6.y() - var13.surface()) <= 0.02
                                    && Math.hypot(var6.x() - var14.getX() - 0.5, var6.z() - var14.getZ() - 0.5) <= 0.8
                                    && (
                                       var3 == null
                                          || (
                                             var26
                                                ? var3.putAt_AtomicIntegerArr[var28].getAndIncrement() < 16
                                                : var3.fill_AtomicIntegerArr[var28].getAndIncrement() < 64
                                          )
                                    )) {
                                    AiJumpSolver.JumpPlan var27 = AiWallJump.planWallJump(var2, var6, var14, var13.wallTargets(), var35, var5);
                                    if (var27 != null) {
                                       var33 = var27.maneuver();
                                    }
                                 }

                                 if (var33 == null
                                    && var31 != null
                                    && var6.speed() >= 0.075
                                    && var6.pose() == 0
                                    && Math.abs(var6.y() - var31.start().y()) <= 0.02
                                    && Math.hypot(var6.x() - var31.start().x(), var6.z() - var31.start().z()) <= 1.2
                                    && (var3 == null || var3.fill_AtomicIntegerArr[var28].getAndIncrement() < 32)) {
                                    var33 = var31.joinRunUp(var2, var6, var18, var13.surface());
                                 }
                              } else {
                                 if (var31 == null
                                    || !(var6.speed() >= 0.075)
                                    || var6.pose() != 0
                                    || !(Math.abs(var6.y() - var31.start().y()) <= 0.02)
                                    || !(Math.hypot(var6.x() - var31.start().x(), var6.z() - var31.start().z()) <= 1.2)
                                    || var3 != null && var3.fill_AtomicIntegerArr[var28].getAndIncrement() >= 32) {
                                    if (var31 != null
                                       && var6.speed() < 0.075
                                       && Math.abs(var6.y() - var31.start().y()) <= 0.02
                                       && Math.hypot(var6.x() - var31.start().x(), var6.z() - var31.start().z()) <= 0.65
                                       && (var3 == null || var3.putAt_AtomicIntegerArr[var28].getAndIncrement() < 16)) {
                                       var33 = var31.fitApproach(var2, var6, var18, var13.surface(), var31.inputs().getFirst().yaw());
                                    }
                                 } else {
                                    var33 = var31.joinRunUp(var2, var6, var18, var13.surface());
                                 }

                                 if (var33 == null && var6.speed() < 0.01 && (var3 == null || var3.putAt_AtomicIntegerArr[var28].getAndIncrement() < 16)) {
                                    var33 = AiJumpManeuver.live(var2, var6, var14, var18, var13.surface(), var13.yaw(), var13.sprint());
                                 }
                              }

                              if (var33 != null) {
                                 var19 = var33.inputs();
                              }

                              if (var3 != null) {
                                 var3.putAt_ConcurrentHashMap.putIfAbsent(var37, var19 != null ? var19 : AiPaceScript.Inner_RingUtil.putAt_List);
                              }
                           }
                        }

                        if (var33 != null) {
                           var19 = var33.inputs();
                        }
                     }

                     if (var19 != null && !var19.isEmpty()) {
                        if (var8 == null) {
                           var8 = new ArrayList(2);
                        }

                        var8.add(new AiPaceScript.Inner_TextStore(var19, var18));
                     }
                  }
               }
            }
         }

         return var8 == null ? List.of() : var8;
      } else {
         return List.of();
      }
   }

   private static boolean putAt_Z_TokenCore_TaskStat_3(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2) {
      boolean var3 = onClimbable(var0, var1);
      BlockPos var4 = BlockPos.containing(var1.x(), var3 ? var1.y() : Math.ceil(var1.y() - 1.0E-6), var1.z());
      return var4.equals(var2) || var3 && AiSlimeBounce.columnReachable(var0, var4, var2);
   }

   private static List<List<AiPaceScript.Inner_TaskState>> putAt_List_List(
      List<AiPaceScript.Inner_TaskState> var0,
      AiPaceScript.Inner_JsonCache var1,
      AiPaceScript.Inner_TextState var2,
      AiPaceScript.Inner_HashEntry var3,
      LongAdder var4,
      boolean var5,
      AiPaceScript.Inner_RingUtil var6,
      AiPaceScript.Inner_HashPool var7
   ) {
      List[] var8 = new List[var0.size()];
      if (var0.size() != 1 && var3.putAt_Z()) {
         ArrayList<Future> var19 = new ArrayList<>();

         for (int var20 = 0; var20 < var8.length; var20++) {
            int var11 = var20;
            var19.add(putAt_ExecutorService.submit(() -> {
               AiPhysics.PhysicsWorld var10x = var3.putAt_TokenCore_TaskStat();

               try {
                  var8[var11] = putAt_List_TextPool_TaskState((AiPaceScript.Inner_TaskState)var0.get(var11), var1, var2, var10x, var4, var5, var6, var7);
               } finally {
                  var3.putAt_V_TokenCore_TaskStat(var10x);
               }
            }));
         }

         try {
            for (Future var23 : var19) {
               var23.get();
            }
         } catch (InterruptedException var16) {
            for (Future var12 : var19) {
               var12.cancel(true);
            }

            Thread.currentThread().interrupt();
            throw new CancellationException("parkourwr search interrupted");
         } catch (ExecutionException var17) {
            throw new IllegalStateException("parkourwr step failed", var17.getCause());
         }

         return Arrays.asList(var8);
      } else {
         AiPhysics.PhysicsWorld var9 = var3.putAt_TokenCore_TaskStat();

         try {
            for (int var10 = 0; var10 < var8.length; var10++) {
               var8[var10] = putAt_List_TextPool_TaskState((AiPaceScript.Inner_TaskState)var0.get(var10), var1, var2, var9, var4, var5, var6, var7);
            }
         } finally {
            var3.putAt_V_TokenCore_TaskStat(var9);
         }

         return Arrays.asList(var8);
      }
   }

   private static void putAt(
      AiPaceScript.Inner_TaskState var0,
      float var1,
      int var2,
      boolean var3,
      int var4,
      int[] var5,
      AiPaceScript.Inner_JsonCache var6,
      AiPaceScript.Inner_TextState var7,
      AiPhysics.PhysicsWorld var8,
      LongAdder var9,
      AiPaceScript.Inner_TableCache var10,
      List<AiPaceScript.Inner_TaskState> var11,
      boolean var12,
      AiPaceScript.Inner_HashPool var13
   ) {
      int var14 = var4 == 0 ? 0 : 2;
      AiPhysics.MotionState var15 = var0.putAt_TokenCore_TableCac;
      float var16 = var0.putAt_F;
      float var17 = var16;
      int var18 = var0.flowState;
      int var19 = var0.flowTemp;
      int var20 = 0;
      int var21 = 0;
      int var22 = var6.checkpoints().length;
      double var23 = var0.putAt_D;
      double var25 = var0.readEx_D;
      double var27 = 0.0;
      double var29 = var0.prevEx;
      double var31 = 0.0;
      double var33 = var13.bestGoalF();
      boolean var35 = false;
      boolean var36 = onClimbable(var8, var15);
      boolean var37 = !var0.putAt_Z;
      boolean var38 = false;
      int var39 = 0;
      int var40 = var0.value_I;
      AiPhysics.MotionState var41 = var0.putAt_TokenCore_TableCac;
      var10.putAt_Z = false;
      var10.fill_Z = false;
      var10.flowState = 0;

      for (int var42 = 0; var42 < 120; var42++) {
         boolean var43 = (!var15.onGround() || !(var15.vy() <= 0.1)) && !var36;
         if (var43 || !var35) {
            if (!var43) {
               for (; var21 < var5.length && var42 >= var5[var21]; var21++) {
                  if (var20 > 0) {
                     AiPaceScript.Inner_TaskState var44 = putAt(
                        var0,
                        var15,
                        var36,
                        var16,
                        var18,
                        var19,
                        var23,
                        var25,
                        var29,
                        var31,
                        false,
                        var41,
                        var40,
                        var38,
                        var39,
                        null,
                        0,
                        var10,
                        var20,
                        var6,
                        var7,
                        var8,
                        var13
                     );
                     if (var44 != null) {
                        var11.add(var44);
                     }
                  }
               }

               if (var21 >= var5.length) {
                  return;
               }
            }

            if (var0.readEx_I + var20 >= var7.maxTicks()) {
               return;
            }

            if (var33 < 1.0 / 0.0 && var0.readEx_I + var20 + 1 + var7.turnCost() * var25 >= var33 + 6.0) {
               return;
            }

            boolean var45 = var7.allowSprint();
            AiPhysics.MoveInput var58;
            if (!var43) {
               var16 = putAt_F_F(var16, var1, var7.maxTurn());
               if (var4 == 0) {
                  var58 = new AiPhysics.MoveInput(1.0, var2, var3, var45, false, var16);
               } else {
                  AiPhysics.MoveInput var10000;
                  switch (var4) {
                     case 1:
                        var10000 = var3
                           ? new AiPhysics.MoveInput(0.0, 0.0, true, false, putAt_Z_TokenCore_TaskStat_4(var8, var15, var36), var16)
                           : AiPhysics.MoveInput.NONE.withYaw(var16);
                        break;
                     case 3:
                        var10000 = new AiPhysics.MoveInput(1.0, var2, false, false, true, var16);
                        break;
                     default:
                        AiPhysics.MoveInput var46 = putAt(var16, var15);
                        var10000 = var3
                           ? new AiPhysics.MoveInput(
                              var46.forward(), var46.strafe(), true, false, putAt_Z_TokenCore_TaskStat_4(var8, var15, var36), var16
                           )
                           : var46;
                  }

                  var58 = var10000;
               }
            } else {
               var35 = true;
               var10.putAt_Z = true;
               var10.flowState++;
               if (var15.onGround() && var15.vy() > 0.1) {
                  var10.fill_Z = true;
               }

               var58 = var14 != 2 ? new AiPhysics.MoveInput(1.0, 0.0, false, var45, false, var16) : AiPhysics.MoveInput.NONE.withYaw(var16);
            }

            var25 += Math.abs(Mth.wrapDegrees(var16 - var17));
            var17 = var16;
            AiPhysics.PhysicsWorld var60 = putAt_TokenCore_TaskStat_TokenCore_TaskStat(var8, var19, var13.speedProfile());
            AiPhysics.MotionState var47 = AiPhysics.stepWithClimbable(var15, var58, var60);
            var9.increment();
            var40 = var58.sneak() ? var40 + 1 : 0;
            if (var40 < 3 && var47.pose() != 0 && putAt_Z_TokenCore_TaskStat_2(var8, var15, var47)) {
               return;
            }

            double var48 = var27 + Math.sqrt(fill_D_D(var47.x() - var15.x()) + fill_D_D(var47.y() - var15.y()) + fill_D_D(var47.z() - var15.z()));
            putAt_V_TokenCore_TableCac(var47, var18, var6, var0.putAt_D + 1.5 * var48 + 1.0, var10);
            if (var10.flowTemp < 0 || var10.putAt_D > 3.0) {
               return;
            }

            boolean var50 = onClimbable(var8, var47);
            boolean var51 = var47.onGround() && var47.vy() <= 0.1 || var50;
            double var52 = var10.readEx_D - var47.y();
            if (!var6.freeSegment(var10.flowTemp) && var52 > (var51 ? 0.3 : 1.2)) {
               return;
            }

            var var54 = var7.allowedCell();
            if (var54 != null && (var47.onGround() || var50) && !var54.test(BlockPos.containing(var47.x(), var47.y() + 1.0E-4, var47.z()))) {
               return;
            }

            if (AiPhysics.hitsHazard(var8, var15, var47)
               || putAt_Z_TokenCore_TaskStat_3(var8, var15, var47, var36, var50) && !putAt(var8, var37, var15, var47, var36, var50)
               || putAt(var8, var15, var47, var36, var50, var10)
               || !var36 && fill(var60, var15, var58, var47)) {
               return;
            }

            if (var37 && var47.onGround() && putAt_Z_TokenCore_TaskStat(var8, var47, 0.002)) {
               var37 = false;
            }

            if (var6.freeSegment(var10.flowTemp)) {
               if (var47.verticalCollision() && !var47.onGround()) {
                  var38 = true;
               }

               if (var10.fill_Z && !var50 && var47.horizontalCollision()) {
                  var39++;
               }
            }

            var27 = var48;
            var41 = var15;
            var15 = var47;
            var36 = var50;
            var10.putAt_TokenCore_CacheHelArr[var20] = var58;
            var10.putAt_TokenCore_TableCacArr[var20] = var15;
            if (var13.speedProfile() != null && var13.speedProfile().hasTransition()) {
               var10.putAt_DArr[var20] = var60.movementSpeed();
            }

            var20++;
            var29 = var10.putAt_D;
            var31 = var52;
            var18 = var10.flowTemp;
            if (var19 < var22) {
               int var55 = var6.checkpointPoint()[var19];
               if (putAt(var8, var15, var51, var6.checkpoints()[var19], var6.plateGoal(), var6.centerGoal())) {
                  var19++;
               } else if (var18 >= var55 + 1 && var10.fill_D > var6.cum()[var55] + 1.5) {
                  return;
               }
            }

            var23 = var10.fill_D;
            if (var19 < var22) {
               var23 = Math.min(var23, var6.cum()[var6.checkpointPoint()[var19]]);
            }

            if (var19 != var22) {
               continue;
            }
         }
         break;
      }

      if (var20 != 0) {
         AiPaceScript.Inner_TaskState var56 = putAt(
            var0,
            var15,
            var36,
            var16,
            var18,
            var19,
            var23,
            var25,
            var29,
            var31,
            var35,
            var41,
            var40,
            var38,
            var39,
            null,
            0,
            var10,
            var20,
            var6,
            var7,
            var8,
            var13
         );
         if (var56 != null) {
            var11.add(var56);
         }

         if (var12 && var10.fill_Z && var14 != 2 && var21 < var5.length) {
            AiPaceScript.Inner_RingBuffer var57 = new AiPaceScript.Inner_RingBuffer(var1, var5[var21], false, var14, 0, var2, 0, var3, var4, 0, 0).bouncing();
            AiPaceScript.Inner_TaskState var59 = putAt_TextPool_TaskState_TextPool_TaskState(var0, var57, var6, var7, var8, var9, var10, var13);
            if (var59 != null) {
               var11.add(var59);
            }
         }
      }
   }

   private static AiPaceScript.Inner_TaskState putAt_TextPool_TaskState_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_RingBuffer var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPhysics.PhysicsWorld var4,
      LongAdder var5,
      AiPaceScript.Inner_TableCache var6,
      AiPaceScript.Inner_HashPool var7
   ) {
      return putAt_TextPool_TaskState_TextPool_TaskState(var0, var1, null, 0, null, 0, var2, var3, var4, var5, var6, var7);
   }

   private static AiPaceScript.Inner_TaskState putAt_TextPool_TaskState_TextPool_TaskState(
      AiPaceScript.Inner_TaskState var0,
      AiPaceScript.Inner_RingBuffer var1,
      AiPaceScript.Inner_SyncPool var2,
      int var3,
      AiPaceScript.Inner_SyncPool var4,
      int var5,
      AiPaceScript.Inner_JsonCache var6,
      AiPaceScript.Inner_TextState var7,
      AiPhysics.PhysicsWorld var8,
      LongAdder var9,
      AiPaceScript.Inner_TableCache var10,
      AiPaceScript.Inner_HashPool var11
   ) {
      float var12 = var1.heading();
      int var13 = var1.run();
      int var14 = var1.air();
      int var15 = var1.release();
      int var16 = var1.strafe();
      int var17 = var1.steerAfter();
      boolean var18 = var1.jump();
      boolean var19 = var1.holdJump();
      int var20 = var6.checkpoints().length;
      double var21 = var11.bestGoalF();
      boolean var44 = !var0.putAt_Z && var3 == 0;
      AiPhysics.MotionState var23;
      AiPhysics.MotionState var24;
      float var25;
      float var26;
      int var27;
      int var28;
      int var29;
      int var30;
      int var31;
      double var32;
      double var34;
      double var36;
      double var38;
      double var40;
      boolean var42;
      boolean var43;
      boolean var45;
      int var46;
      if (var2 != null && var3 > 0) {
         if (var3 > var2.flowState || var3 == var2.flowState && var2.fill_Z) {
            var10.putAt_Z = false;
            var10.fill_Z = false;
            var10.flowState = 0;
            return null;
         }

         var23 = var2.putAt_TokenCore_TableCacArr[var3 - 1];
         var24 = var3 >= 2 ? var2.putAt_TokenCore_TableCacArr[var3 - 2] : var0.putAt_TokenCore_TableCac;
         var25 = var2.putAt_FArr[var3];
         var26 = var25;
         var27 = var2.putAt_IArr[var3];
         var28 = var2.fill_IArr[var3];
         var32 = var2.fill_DArr[var3];
         var34 = var2.readEx_DArr[var3];
         var36 = var2.value_DArr[var3];
         var38 = var2.prevEx_DArr[var3];
         var40 = var2.prev[var3];
         var31 = var2.readEx_IArr[var3];
         var43 = var2.putAt_ZArr[var3];
         var42 = var2.fill_ZArr[var3];
         var30 = var2.value_IArr[var3];
         var45 = var2.value_ZArr[var3];
         var46 = var2.prevEx_IArr[var3];
         var29 = var3;
         var10.putAt_Z = var42;
         var10.fill_Z = var2.readEx_ZArr[var3];
         var10.flowState = var30;
         if (var4 != null) {
            var4.putAt(var2, var3);
         }
      } else {
         var3 = 0;
         var2 = null;
         var23 = var0.putAt_TokenCore_TableCac;
         var24 = var0.putAt_TokenCore_TableCac;
         var25 = var0.putAt_F;
         var26 = var25;
         var27 = var0.flowState;
         var28 = var0.flowTemp;
         var32 = var0.putAt_D;
         var34 = var0.readEx_D;
         var36 = 0.0;
         var38 = var0.prevEx;
         var40 = 0.0;
         var31 = var0.value_I;
         var43 = onClimbable(var8, var23);
         var42 = false;
         var30 = 0;
         var45 = false;
         var46 = 0;
         var29 = 0;
         var10.putAt_Z = false;
         var10.fill_Z = false;
         var10.flowState = 0;
         if (var4 != null) {
            var4.putAt(var25, var27, var28, var32, var34, var38, var31, var43);
         }
      }

      for (int var47 = var3; var47 < 120; var47++) {
         boolean var48 = (!var23.onGround() || !(var23.vy() <= 0.1)) && !var43;
         label323:
         if (var48 || !var42 || var1.keys() != null && var47 < var1.keys().size()) {
            if (var0.readEx_I + var29 >= var7.maxTicks()) {
               return null;
            }

            if (var21 < 1.0 / 0.0 && var0.readEx_I + var29 + 1 + var7.turnCost() * var34 >= var21 + 6.0) {
               return null;
            }

            boolean var50 = var7.allowSprint();
            AiPhysics.MoveInput var49;
            if (var1.keys() != null) {
               var var51 = var1.keys();
               if (!var48 && var47 >= var51.size()) {
                  break label323;
               }

               AiPhysics.MoveInput var52 = var47 < var51.size()
                  ? (AiPhysics.MoveInput)var51.get(var47)
                  : ((AiPhysics.MoveInput)var51.getLast()).withJump(false);
               var25 = putAt_F_F(var25, var52.yaw(), var7.maxTurn());
               var49 = var52.withYaw(var25);
               if (var48) {
                  var42 = true;
                  var10.putAt_Z = true;
                  var10.flowState = ++var30;
                  if (var23.onGround() && var23.vy() > 0.1) {
                     var10.fill_Z = true;
                  }
               }
            } else if (!var48) {
               if (!var18 && var47 >= var13) {
                  break label323;
               }

               if (var18 && var47 >= 20) {
                  return null;
               }

               var25 = putAt_F_F(var25, var12, var7.maxTurn());
               if (var18 && var47 == 0 && var23.onGround() && !var0.putAt_Z) {
                  return null;
               }

               if ((!var18 || var47 != 0) && var1.ground() != 0) {
                  AiPhysics.MoveInput var10000;
                  switch (var1.ground()) {
                     case 1:
                        var10000 = var19
                           ? new AiPhysics.MoveInput(0.0, 0.0, true, false, putAt_Z_TokenCore_TaskStat_4(var8, var23, var43), var25)
                           : AiPhysics.MoveInput.NONE.withYaw(var25);
                        break;
                     case 3:
                        var10000 = new AiPhysics.MoveInput(1.0, var16, false, false, true, var25);
                        break;
                     default:
                        AiPhysics.MoveInput var63 = putAt(var25, var23);
                        var10000 = var19
                           ? new AiPhysics.MoveInput(
                              var63.forward(), var63.strafe(), true, false, putAt_Z_TokenCore_TaskStat_4(var8, var23, var43), var25
                           )
                           : var63;
                  }

                  var49 = var10000;
               } else {
                  var49 = new AiPhysics.MoveInput(1.0, var16, var18 && var47 == 0 || var19, var50, false, var25);
               }
            } else {
               if (!var42 && var4 != null && var4.flowTemp < 0) {
                  var4.flowTemp = var47;
               }

               var42 = true;
               var10.putAt_Z = true;
               var10.flowState = ++var30;
               if (var4 != null) {
                  var4.putAt_Z = true;
                  var4.readEx_I = var30;
                  if (var5 != 0) {
                     var4.putAt(var5, var30, var23, var32, var6, var25, var7.maxTurn());
                  }
               }

               if (var14 == 1 && var30 > var17) {
                  var25 = putAt_F_F(var25, fill(var23, var32, var6, var25), var7.maxTurn());
               }

               boolean var64 = var14 != 2 && (var15 == 0 || var30 <= var15);
               AiPhysics.MoveInput var67;
               if (var64) {
                  var67 = new AiPhysics.MoveInput(1.0, var1.airStrafe(), false, var50, false, var25);
               } else {
                  switch (var1.after()) {
                     case 1:
                        var67 = putAt(var25, var23);
                        break;
                     case 2:
                        var67 = new AiPhysics.MoveInput(0.0, 1.0, false, false, false, var25);
                        break;
                     case 3:
                        var67 = new AiPhysics.MoveInput(0.0, -1.0, false, false, false, var25);
                        break;
                     default:
                        var67 = AiPhysics.MoveInput.NONE.withYaw(var25);
                  }
               }

               var49 = var67;
               if (var23.onGround() && var23.vy() > 0.1) {
                  var10.fill_Z = true;
                  if (var1.bounceJump()) {
                     var49 = new AiPhysics.MoveInput(1.0, var49.strafe(), true, var50, false, var25);
                  }
               }
            }

            var34 += Math.abs(Mth.wrapDegrees(var25 - var26));
            var26 = var25;
            AiPhysics.PhysicsWorld var65 = putAt_TokenCore_TaskStat_TokenCore_TaskStat(var8, var28, var11.speedProfile());
            double var66 = var65.movementSpeed();
            AiPhysics.MotionState var54 = AiPhysics.stepWithClimbable(var23, var49, var65);
            var9.increment();
            var31 = var49.sneak() ? var31 + 1 : 0;
            if (var31 < 3 && var54.pose() != 0 && putAt_Z_TokenCore_TaskStat_2(var8, var23, var54)) {
               return null;
            }

            double var55 = var36 + Math.sqrt(fill_D_D(var54.x() - var23.x()) + fill_D_D(var54.y() - var23.y()) + fill_D_D(var54.z() - var23.z()));
            putAt_V_TokenCore_TableCac(var54, var27, var6, var0.putAt_D + 1.5 * var55 + 1.0, var10);
            if (var10.flowTemp < 0 || var10.putAt_D > 3.0) {
               return null;
            }

            boolean var57 = onClimbable(var8, var54);
            boolean var58 = var54.onGround() && var54.vy() <= 0.1 || var57;
            double var59 = var10.readEx_D - var54.y();
            if (!var6.freeSegment(var10.flowTemp) && var59 > (var58 ? 0.3 : 1.2)) {
               return null;
            }

            var var61 = var7.allowedCell();
            if (var61 != null && (var54.onGround() || var57) && !var61.test(BlockPos.containing(var54.x(), var54.y() + 1.0E-4, var54.z()))) {
               return null;
            }

            if (AiPhysics.hitsHazard(var8, var23, var54)
               || putAt_Z_TokenCore_TaskStat_3(var8, var23, var54, var43, var57) && !putAt(var8, var44, var23, var54, var43, var57)
               || putAt(var8, var23, var54, var43, var57, var10)
               || !var43 && fill(var65, var23, var49, var54)) {
               return null;
            }

            if (var44 && var54.onGround() && putAt_Z_TokenCore_TaskStat(var8, var54, 0.002)) {
               var44 = false;
            }

            if (var6.freeSegment(var10.flowTemp)) {
               if (var54.verticalCollision() && !var54.onGround()) {
                  var45 = true;
               }

               if (var10.fill_Z && !var57 && var54.horizontalCollision()) {
                  var46++;
               }
            }

            var36 = var55;
            var24 = var23;
            var23 = var54;
            var43 = var57;
            var10.putAt_TokenCore_CacheHelArr[var29] = var49;
            var10.putAt_TokenCore_TableCacArr[var29] = var23;
            if (var11.speedProfile() != null && var11.speedProfile().hasTransition()) {
               var10.putAt_DArr[var29] = var66;
            }

            var29++;
            var38 = var10.putAt_D;
            var40 = var59;
            var27 = var10.flowTemp;
            if (var28 < var20) {
               int var62 = var6.checkpointPoint()[var28];
               if (putAt(var8, var23, var58, var6.checkpoints()[var28], var6.plateGoal(), var6.centerGoal())) {
                  var28++;
               } else if (var27 >= var62 + 1 && var10.fill_D > var6.cum()[var62] + 1.5) {
                  return null;
               }
            }

            var32 = var10.fill_D;
            if (var28 < var20) {
               var32 = Math.min(var32, var6.cum()[var6.checkpointPoint()[var28]]);
            }

            if (var4 != null) {
               var4.putAt(
                  var29, var49, var23, var66, var25, var27, var28, var32, var34, var36, var38, var40, var31, var43, var42, var30, var10.fill_Z, var45, var46
               );
            }

            if (var28 != var20) {
               continue;
            }
         }
         break;
      }

      if (var4 != null) {
         var4.fill_Z = true;
      }

      return putAt(
         var0,
         var23,
         var43,
         var25,
         var27,
         var28,
         var32,
         var34,
         var38,
         var40,
         var42,
         var24,
         var31,
         var45,
         var46,
         var2,
         var3,
         var10,
         var29,
         var6,
         var7,
         var8,
         var11
      );
   }

   private static AiPaceScript.Inner_TaskState putAt(
      AiPaceScript.Inner_TaskState var0,
      AiPhysics.MotionState var1,
      boolean var2,
      float var3,
      int var4,
      int var5,
      double var6,
      double var8,
      double var10,
      double var12,
      boolean var14,
      AiPhysics.MotionState var15,
      int var16,
      boolean var17,
      int var18,
      AiPaceScript.Inner_SyncPool var19,
      int var20,
      AiPaceScript.Inner_TableCache var21,
      int var22,
      AiPaceScript.Inner_JsonCache var23,
      AiPaceScript.Inner_TextState var24,
      AiPhysics.PhysicsWorld var25,
      AiPaceScript.Inner_HashPool var26
   ) {
      int var27 = var23.checkpoints().length;
      boolean var28 = var1.onGround() && var1.vy() <= 0.1 || var2;
      if (var22 != 0 && (var28 || var5 >= var27)) {
         if (var5 < var27 && var1.onGround() && !var2 && var12 > 0.3) {
            return null;
         }

         int var29 = var0.readEx_I + var22;
         if (var29 > var24.maxTicks()) {
            return null;
         }

         boolean var30 = !var1.onGround() || var2 || (!var14 && !var0.putAt_Z ? putAt_Z_TokenCore_TaskStat(var25, var1, 0.002) : fill(var25, var15, var1));
         if (var14 && !var30) {
            return null;
         }

         double var31 = var5 == var27 ? 0.0 : var23.timeLeft(var6);
         if (var5 < var27 && var2 && var23.freeSegment(var4)) {
            double var33 = var23.y()[Math.min(var4 + 1, var23.y().length - 1)];
            if (var1.y() < var33) {
               var31 += (var33 - var1.y()) / 0.12;
            }
         }

         double var48 = var23.alongSpeed(var1.x(), var1.z(), var1.vx(), var1.vz(), var23.clampLookAtReversal(var6, var6 + 2.5));
         double var35 = var0.value_D + (var17 ? 45.0 : 0.0) + (var18 > 0 ? 4.0 + var18 : 0.0);
         double var37 = var29 + var35 + var24.turnCost() * var8 + var24.weight() * var31 - 2.0 * var48 / var23.flatSpeed();
         if (var1.onGround() && var14 && !readEx(var25, var1)) {
            var37 += 1.5;
         }

         boolean var39 = var5 == var27 || var6 >= var26.target() && var10 <= 2.0;
         if (var39) {
            if (var37 >= var26.bestGoalF()) {
               return null;
            }
         } else if (var37 >= var26.bestGoalF() + 3.0) {
            return null;
         }

         long var40 = putAt_J_TokenCore_TableCac(var1, var3, var5);
         int var42 = var29 + (int)Math.round(var35);
         if (!var39 && var26.seen() != null && var26.seen().get(var40) <= var42) {
            return null;
         }

         AiPhysics.MoveInput[] var43 = new AiPhysics.MoveInput[var22];
         AiPhysics.MotionState[] var44 = new AiPhysics.MotionState[var22];
         boolean var45 = var26.speedProfile() != null && var26.speedProfile().hasTransition();
         double[] var46 = var45 ? new double[var22] : null;
         if (var19 != null && var20 > 0) {
            System.arraycopy(var19.putAt_TokenCore_CacheHelArr, 0, var43, 0, var20);
            System.arraycopy(var19.putAt_TokenCore_TableCacArr, 0, var44, 0, var20);
            if (var46 != null) {
               System.arraycopy(var19.putAt_DArr, 0, var46, 0, var20);
            }
         }

         System.arraycopy(var21.putAt_TokenCore_CacheHelArr, var20, var43, var20, var22 - var20);
         System.arraycopy(var21.putAt_TokenCore_TableCacArr, var20, var44, var20, var22 - var20);
         if (var46 != null) {
            System.arraycopy(var21.putAt_DArr, var20, var46, var20, var22 - var20);
         }

         AiPaceScript.Inner_TaskState var47 = new AiPaceScript.Inner_TaskState(var1, var3, var4, var5, var29, var6, var37, var8, var0, var43, var44, var46);
         var47.putAt_Z = var30;
         var47.value_I = var16;
         var47.fill_Z = var2;
         var47.readEx_Z = var1.onGround() && !var2 && putAt_Z_TokenCore_TaskStat(var25, var1, 0.002);
         var47.value_D = var35;
         var47.prevEx = var10;
         var47.putAt_J = var40;
         return var47;
      } else {
         return null;
      }
   }

   private static float putAt_F_F(float var0, float var1, float var2) {
      return var0 + Math.clamp(Mth.wrapDegrees(var1 - var0), -var2, var2);
   }

   public static boolean putAt(AiPhysics.MotionState var0, BlockPos var1) {
      return putAt_Z_TokenCore_TableCac(var0, var1, 0.0, 0.0);
   }

   static boolean putAt_Z_TokenCore_TaskStat_4(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, boolean var3) {
      return putAt(var0, var1, putAt(var0, var1), var2, var3, false);
   }

   static boolean putAt_Z_TokenCore_TaskStat_4(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, BlockPos var2, boolean var3, boolean var4) {
      return putAt(var0, var1, putAt(var0, var1), var2, var3, var4);
   }

   private static boolean putAt(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, boolean var2, BlockPos var3, boolean var4, boolean var5) {
      if (var4) {
         return putAt_Z_TokenCore_TableCac(var1, var3, 0.14, 0.05);
      }

      if (var2 && putAt_Z_TokenCore_TaskStat_3(var0, var1, var3)) {
         if (var0.pressurePlateAt(var3.getX(), var3.getY(), var3.getZ()) && !putAt_Z_TokenCore_TableCac(var1, var3, 0.14, 0.05)) {
            return false;
         }

         if (!var5) {
            return true;
         }

         double var6 = var3.getX() + 0.5;
         double var8 = var3.getZ() + 0.5;
         return Math.hypot(var1.x() - var6, var1.z() - var8) <= 0.28 || !AiJumpSolver.canStandAt(var0, var6, var1.y(), var8);
      } else {
         return false;
      }
   }

   static double putAt_D_TokenCore_TableCac(AiPhysics.MotionState var0) {
      return var0.pose() == 2 ? 0.6 : (var0.pose() != 0 ? 1.5 : 1.8);
   }

   static boolean putAt_Z_TokenCore_TableCac(AiPhysics.MotionState var0, BlockPos var1, double var2) {
      return putAt_Z_TokenCore_TableCac(var0, var1, var2, Math.min(var2, 0.05));
   }

   static boolean putAt_Z_TokenCore_TableCac(AiPhysics.MotionState var0, BlockPos var1, double var2, double var4) {
      double var6 = 0.3;
      double var8 = 0.125 + var2;
      return var0.x() + var6 > var1.getX() + var8
         && var0.x() - var6 < var1.getX() + 1 - var8
         && var0.z() + var6 > var1.getZ() + var8
         && var0.z() - var6 < var1.getZ() + 1 - var8
         && var0.y() < var1.getY() + 0.25 - var4
         && var0.y() + putAt_D_TokenCore_TableCac(var0) > var1.getY() + var4;
   }

   static boolean putAt(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      return var1.onGround() && var1.vy() <= 0.1 || onClimbable(var0, var1);
   }

   static boolean onClimbable(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      return var0.climbableAt((int)Math.floor(var1.x()), (int)Math.floor(var1.y()), (int)Math.floor(var1.z()));
   }

   private static boolean putAt_Z_TokenCore_TaskStat_4(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, boolean var2) {
      if (var2 && !(var1.vy() >= 0.0)) {
         int var3 = (int)Math.floor(var1.y());
         return var1.y() - var3 < 0.22 && !var0.climbableAt((int)Math.floor(var1.x()), var3 - 1, (int)Math.floor(var1.z()));
      } else {
         return false;
      }
   }

   static AiPaceScript.Inner_BitCore putAt_TextPool_BitCore_TokenCore_TableCac(
      AiPhysics.MotionState var0, int var1, AiPaceScript.Inner_JsonCache var2, double var3
   ) {
      AiPaceScript.Inner_TableCache var5 = new AiPaceScript.Inner_TableCache();
      putAt_V_TokenCore_TableCac(var0, var1, var2, var3, var5);
      return new AiPaceScript.Inner_BitCore(var5.flowTemp, var5.putAt_D, var5.fill_D, var5.readEx_D);
   }

   private static void putAt_V_TokenCore_TableCac(
      AiPhysics.MotionState var0, int var1, AiPaceScript.Inner_JsonCache var2, double var3, AiPaceScript.Inner_TableCache var5
   ) {
      int var6 = -1;
      double var7 = 1.7976931348623157E308;
      double var9 = 1.7976931348623157E308;
      double var11 = 0.0;
      double var13 = 0.0;
      double[] var15 = var2.x();
      double[] var16 = var2.y();
      double[] var17 = var2.z();
      double[] var18 = var2.cum();
      int var19 = Math.min(var2.segments() - 1, var1 + 6);
      double var20 = var0.x();
      double var22 = var0.y();
      double var24 = var0.z();

      for (int var26 = Math.max(0, var1);
         var26 <= var19
            && (var26 <= var1 || !var2.isReversalVertex(var26) || !(fill_D_D(var20 - var15[var26]) + fill_D_D(var24 - var17[var26]) > 0.7224999999999999));
         var26++
      ) {
         double var27 = var18[var26];
         if (var27 > var3) {
            break;
         }

         double var29 = var15[var26];
         double var31 = var16[var26];
         double var33 = var17[var26];
         double var35 = var15[var26 + 1];
         double var37 = var16[var26 + 1];
         double var39 = var17[var26 + 1];
         double var41 = var35 - var29;
         double var43 = var39 - var33;
         double var45 = var41 * var41 + var43 * var43;
         boolean var47 = var2.freeSegment(var26);
         double var48;
         if (var45 >= 0.25) {
            var48 = ((var20 - var29) * var41 + (var24 - var33) * var43) / var45;
         } else if (var47) {
            var48 = 1.0;
         } else {
            var48 = Math.abs(var37 - var31) < 1.0E-9 ? 1.0 : (var22 - var31) / (var37 - var31);
         }

         var48 = Math.clamp(var48, 0.0, 1.0);
         double var50 = var18[var26 + 1] - var27;
         if (var50 > 1.0E-9) {
            var48 = Math.min(var48, (var3 - var27) / var50);
         }

         double var52 = var29 + var41 * var48;
         double var54 = var31 + (var37 - var31) * var48;
         double var56 = var33 + var43 * var48;
         double var58 = var47 ? 0.0 : var22 - var54;
         double var60 = fill_D_D(var20 - var52) + fill_D_D(var24 - var56) + fill_D_D(var58 * 0.5);
         if (var60 <= var9) {
            double var62 = Math.sqrt(var60);
            if (var62 <= var7 + 1.0E-6) {
               var7 = var62;
               double var64 = var62 + 1.0E-6;
               var9 = var64 * var64;
               var6 = var26;
               var11 = var27 + var50 * var48;
               var13 = var45 >= 0.25 ? Math.min(var31, var37) : var54;
            }
         }
      }

      var5.flowTemp = var6;
      var5.putAt_D = var7;
      var5.fill_D = var11;
      var5.readEx_D = var13;
   }

   public static float putAt_F_TokenCore_TableCac(AiPhysics.MotionState var0, AiPaceScript.Inner_JsonCache var1, float var2) {
      return fill(var0, 0.0, var1, var2);
   }

   public static float putAt_F_TokenCore_TableCac(AiPhysics.MotionState var0, double var1, AiPaceScript.Inner_JsonCache var3, float var4) {
      return fill(var0, var1, var3, var4);
   }

   private static float fill(AiPhysics.MotionState var0, double var1, AiPaceScript.Inner_JsonCache var3, float var4) {
      double var5 = var3.clampLookAtReversal(var1, var1 + 2.5);
      float var7 = var3.yawAt(var0.x(), var0.z(), var5);
      if (!Float.isNaN(var7)) {
         return var7;
      }

      var5 = var3.clampLookAtReversal(var1, var1 + 6.25);
      var7 = var3.yawAt(var0.x(), var0.z(), var5);
      return Float.isNaN(var7) ? var4 : var7;
   }

   static boolean readEx(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      return putAt_Z_TokenCore_TaskStat(var0, var1, 0.06);
   }

   private static boolean putAt(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPhysics.MotionState var2,
      boolean var3,
      boolean var4,
      AiPaceScript.Inner_TableCache var5
   ) {
      double var6 = 0.3;
      double var8 = var1.x();
      double var10 = var1.z();
      double var12 = var2.x();
      double var14 = var2.z();
      double var16 = putAt_D_TokenCore_TableCac(var1);
      double var18 = putAt_D_TokenCore_TableCac(var2);
      double var20 = Math.min(var8, var12) - var6 - 0.002;
      double var22 = Math.max(var8, var12) + var6 + 0.002;
      double var24 = Math.min(var10, var14) - var6 - 0.002;
      double var26 = Math.max(var10, var14) + var6 + 0.002;
      double var28 = Math.min(var1.y(), var2.y()) + 0.001;
      double var30 = Math.max(var1.y() + var16, var2.y() + var18) - 0.001;
      if (var0 instanceof AiPlannerWorld var32 && var5 != null) {
         AABB[] var33 = var32.putAt_AABBArr_D(var20, var28, var24, var22, var30, var26);
         AABB[] var34 = var5.putAt_AABBArr;
         int var35 = 0;

         for (AABB var39 : var33) {
            if (var39.intersects(var20, var28, var24, var22, var30, var26)) {
               if (var35 >= var34.length) {
                  return value(var0, var2) || !var3 && !var4 && putAt_Z_TokenCore_TaskStat_3(var0, var1, var2, var16);
               }

               var34[var35++] = var39;
            }
         }

         return var35 == 0 ? false : putAt_Z_AABBArr(var34, var35, var2, var18) || !var3 && !var4 && putAt_Z_AABBArr(var34, var35, var1, var2, var16);
      } else {
         return !var0.boxCollides(var20, var28, var24, var22, var30, var26)
            ? false
            : value(var0, var2) || !var3 && !var4 && putAt_Z_TokenCore_TaskStat_3(var0, var1, var2, var16);
      }
   }

   private static boolean putAt_Z_AABBArr(AABB[] var0, int var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      for (int var14 = 0; var14 < var1; var14++) {
         if (var0[var14].intersects(var2, var4, var6, var8, var10, var12)) {
            return true;
         }
      }

      return false;
   }

   private static boolean putAt_Z_AABBArr(AABB[] var0, int var1, AiPhysics.MotionState var2, double var3) {
      double var5 = 0.3;
      double var7 = var2.y() + 0.001;
      double var9 = var2.y() + var3 - 0.001;
      if (!putAt_Z_AABBArr(var0, var1, var2.x() - var5 - 0.002, var7, var2.z() - var5 - 0.002, var2.x() + var5 + 0.002, var9, var2.z() + var5 + 0.002)) {
         return false;
      }

      double var11 = 1.0E-5;
      return !putAt_Z_AABBArr(var0, var1, var2.x() - var5 - var11, var7, var2.z() - var5 - var11, var2.x() + var5 + var11, var9, var2.z() + var5 + var11);
   }

   private static boolean putAt_Z_AABBArr(AABB[] var0, int var1, AiPhysics.MotionState var2, AiPhysics.MotionState var3, double var4) {
      if (var2.onGround() && var3.onGround() && var3.y() > var2.y() + 1.0E-6) {
         return false;
      } else {
         double var6 = var2.x();
         double var8 = var2.z();
         double var10 = var3.x();
         double var12 = var3.z();
         double var14 = var3.y();
         if (!putAt(var0, var1, true, var6, var8, var8, Math.min(var2.y(), var14), Math.max(var2.y(), var14), var4)
            && !putAt(var0, var1, false, var8, var6, var6, Math.min(var2.y(), var14), Math.max(var2.y(), var14), var4)) {
            boolean var16 = Math.abs(var10 - var6) < Math.abs(var12 - var8);
            boolean var17 = var3.horizontalCollision() && var3.vx() == 0.0;
            boolean var18 = var3.horizontalCollision() && var3.vz() == 0.0;
            return var16
               ? putAt(var0, var1, true, var6, var8, var12, var14, var14, var4)
                  || !var18 && putAt_Z_AABBArr_2(var0, var1, true, var6, var8, var12, var14, var4)
                  || putAt(var0, var1, false, var12, var6, var10, var14, var14, var4)
                  || !var17 && putAt_Z_AABBArr_2(var0, var1, false, var12, var6, var10, var14, var4)
               : putAt(var0, var1, false, var8, var6, var10, var14, var14, var4)
                  || !var17 && putAt_Z_AABBArr_2(var0, var1, false, var8, var6, var10, var14, var4)
                  || putAt(var0, var1, true, var10, var8, var12, var14, var14, var4)
                  || !var18 && putAt_Z_AABBArr_2(var0, var1, true, var10, var8, var12, var14, var4);
         } else {
            return true;
         }
      }
   }

   private static boolean putAt_Z_AABBArr_2(AABB[] var0, int var1, boolean var2, double var3, double var5, double var7, double var9, double var11) {
      double var13 = var7 - var5;
      if (Math.abs(var13) < 1.0E-9) {
         return false;
      }

      double var15 = 0.3;
      double var17 = Math.signum(var13);
      double var19 = var7 + var17 * var15;
      double var21 = Math.min(var19 + var17 * 1.0E-7, var19 + var17 * 0.002);
      double var23 = Math.max(var19 + var17 * 1.0E-7, var19 + var17 * 0.002);
      double var25 = var9 + 0.001;
      double var27 = var9 + var11 - 0.001;
      double var29 = var3 - var15 + 1.0E-6;
      double var31 = var3 + var15 - 1.0E-6;
      return var2
         ? putAt_Z_AABBArr(var0, var1, var29, var25, var21, var31, var27, var23)
         : putAt_Z_AABBArr(var0, var1, var21, var25, var29, var23, var27, var31);
   }

   private static boolean putAt(AABB[] var0, int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13) {
      double var15 = 0.3;
      double var17 = Math.min(var5, var7) - var15 + 1.0E-6;
      double var19 = Math.max(var5, var7) + var15 - 1.0E-6;
      double var21 = var9 + 0.001;
      double var23 = var11 + var13 - 0.001;

      for (byte var25 = -1; var25 <= 1; var25 += 2) {
         double var26 = var3 + var25 * var15;
         double var28 = Math.min(var26, var26 + var25 * 0.002);
         double var30 = Math.max(var26, var26 + var25 * 0.002);
         boolean var32 = var2
            ? putAt_Z_AABBArr(var0, var1, var28, var21, var17, var30, var23, var19)
            : putAt_Z_AABBArr(var0, var1, var17, var21, var28, var19, var23, var30);
         if (var32) {
            double var33 = Math.min(var26, var26 + var25 * 1.0E-5);
            double var35 = Math.max(var26, var26 + var25 * 1.0E-5);
            boolean var37 = var2
               ? putAt_Z_AABBArr(var0, var1, var33, var21, var17, var35, var23, var19)
               : putAt_Z_AABBArr(var0, var1, var17, var21, var33, var19, var23, var35);
            if (!var37) {
               return true;
            }
         }
      }

      return false;
   }

   static boolean value(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1) {
      double var2 = 0.3;
      double var4 = var1.y() + 0.001;
      double var6 = var1.y() + putAt_D_TokenCore_TableCac(var1) - 0.001;
      if (!var0.boxCollides(var1.x() - var2 - 0.002, var4, var1.z() - var2 - 0.002, var1.x() + var2 + 0.002, var6, var1.z() + var2 + 0.002)) {
         return false;
      }

      double var8 = 1.0E-5;
      return !var0.boxCollides(var1.x() - var2 - var8, var4, var1.z() - var2 - var8, var1.x() + var2 + var8, var6, var1.z() + var2 + var8);
   }

   static boolean fill(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      return putAt_Z_TokenCore_TaskStat(var0, var1.x(), var2.y(), var1.z(), 0.002);
   }

   private static boolean putAt(
      AiPhysics.PhysicsWorld var0, boolean var1, AiPhysics.MotionState var2, AiPhysics.MotionState var3, boolean var4, boolean var5
   ) {
      if (var1 && !var4 && !var5) {
         if (!var2.onGround() || !var3.onGround() || Math.abs(var3.y() - var2.y()) > 1.0E-6) {
            return false;
         } else {
            return putAt_Z_TokenCore_TaskStat(var0, var3, 0.002) ? true : Math.abs(var3.x() - var2.x()) <= 1.0E-7 && Math.abs(var3.z() - var2.z()) <= 1.0E-7;
         }
      } else {
         return false;
      }
   }

   static boolean readEx(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      return putAt_Z_TokenCore_TaskStat_3(var0, var1, var2, onClimbable(var0, var1), onClimbable(var0, var2));
   }

   private static boolean putAt_Z_TokenCore_TaskStat_3(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, boolean var3, boolean var4
   ) {
      if (!var3 && !var4) {
         if (!var2.onGround()) {
            if (!(var2.y() < var1.y() - 1.0E-9)) {
               return false;
            }

            double var29 = 0.3;
            double var7 = var1.x();
            double var9 = var1.z();
            double var11 = var2.y();
            double var13 = var1.y();
            double var15 = putAt_D_TokenCore_TableCac(var1);
            double var17 = 0.002;
            double var19 = 1.0E-5;
            if (var0 instanceof AiPlannerWorld var30) {
               AABB[] var31 = var30.putAt_AABBArr_D(
                  var7 - var29 - var17, var11, var9 - var29 - var17, var7 + var29 + var17, var13 + var15 - 1.0E-4, var9 + var29 + var17
               );
               int var32 = var31.length;
               if (var32 == 0) {
                  return false;
               }

               if (!putAt_Z_AABBArr(var31, var32, var7 - var29 - var17, var11, var9 - var29 - var17, var7 + var29 + var17, var13, var9 + var29 + var17)) {
                  return false;
               }

               for (int var33 = 0; var33 < 4; var33++) {
                  int var25 = var33 % 2 == 0 ? 1 : -1;
                  boolean var26 = var33 < 2;
                  double var27 = (var26 ? var7 : var9) + var25 * var29;
                  if (fill(var31, var32, var26, var27, var27 + var25 * var17, var26 ? var9 : var7, var29 - 1.0E-6, var11, var13)
                     && !fill(var31, var32, var26, var27, var27 + var25 * var19, var26 ? var9 : var7, var29, var13 + 1.0E-4, var13 + var15 - 1.0E-4)) {
                     return true;
                  }
               }

               return false;
            } else {
               for (int var21 = 0; var21 < 4; var21++) {
                  int var22 = var21 % 2 == 0 ? 1 : -1;
                  boolean var23 = var21 < 2;
                  double var24 = (var23 ? var7 : var9) + var22 * var29;
                  if (fill(var0, var23, var24, var24 + var22 * var17, var23 ? var9 : var7, var29 - 1.0E-6, var11, var13)
                     && !fill(var0, var23, var24, var24 + var22 * var19, var23 ? var9 : var7, var29, var13 + 1.0E-4, var13 + var15 - 1.0E-4)) {
                     return true;
                  }
               }

               return false;
            }
         } else {
            boolean var5 = var1.onGround() && var2.y() > var1.y() + 1.0E-6;
            return var5 ? !putAt_Z_TokenCore_TaskStat(var0, var2, 0.002) : !putAt_Z_TokenCore_TaskStat(var0, var1.x(), var2.y(), var1.z(), 0.002);
         }
      } else {
         return false;
      }
   }

   private static boolean fill(AABB[] var0, int var1, boolean var2, double var3, double var5, double var7, double var9, double var11, double var13) {
      double var15 = Math.min(var3, var5);
      double var17 = Math.max(var3, var5);
      return var2
         ? putAt_Z_AABBArr(var0, var1, var15, var11, var7 - var9, var17, var13, var7 + var9)
         : putAt_Z_AABBArr(var0, var1, var7 - var9, var11, var15, var7 + var9, var13, var17);
   }

   static boolean putAt_Z_TokenCore_TaskStat_2(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, AiPhysics.MotionState var3
   ) {
      return var3.horizontalCollision() && !onClimbable(var0, var1) ? fill(var0, var1, var2, var3) : false;
   }

   private static boolean fill(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MoveInput var2, AiPhysics.MotionState var3
   ) {
      if (!var3.horizontalCollision()) {
         return false;
      } else {
         double var4 = var3.x() - var1.x();
         double var6 = var3.z() - var1.z();
         double var8 = Math.toRadians(var2.yaw());
         double var10 = Math.sin(var8);
         double var12 = Math.cos(var8);
         double var14 = var1.vx() + 0.1 * (-var10 * var2.forward() + var12 * var2.strafe());
         double var16 = var1.vz() + 0.1 * (var12 * var2.forward() + var10 * var2.strafe());
         boolean var18 = var3.vx() == 0.0 && Math.abs(var14) > 1.0E-6;
         boolean var19 = var3.vz() == 0.0 && Math.abs(var16) > 1.0E-6;
         if (var19 && !var18) {
            AiPhysics.MotionState var21 = AiPhysics.stepWithClimbable(
               putAt_TokenCore_TableCac_TokenCore_TableCac(var1, var1.x(), var1.z() + Math.copySign(0.002, var16)), var2, var0
            );
            return Math.abs(var21.x() - var1.x() - var4) > 0.01;
         } else if (var18 && !var19) {
            AiPhysics.MotionState var20 = AiPhysics.stepWithClimbable(
               putAt_TokenCore_TableCac_TokenCore_TableCac(var1, var1.x() + Math.copySign(0.002, var14), var1.z()), var2, var0
            );
            return Math.abs(var20.z() - var1.z() - var6) > 0.01;
         } else {
            return false;
         }
      }
   }

   static boolean value(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2) {
      if (!onClimbable(var0, var1) && !onClimbable(var0, var2)) {
         if (var1.onGround() && var2.onGround() && var2.y() > var1.y() + 1.0E-6) {
            return false;
         }

         double var3 = 0.3;
         double var5 = var1.x();
         double var7 = var1.z();
         double var9 = var2.x();
         double var11 = var2.z();
         double var13 = putAt_D_TokenCore_TableCac(var1);
         double var15 = Math.min(var1.y(), var2.y()) + 0.001;
         double var17 = Math.max(var1.y(), var2.y()) + var13 - 0.001;
         return !var0.boxCollides(
               Math.min(var5, var9) - var3 - 0.002,
               var15,
               Math.min(var7, var11) - var3 - 0.002,
               Math.max(var5, var9) + var3 + 0.002,
               var17,
               Math.max(var7, var11) + var3 + 0.002
            )
            ? false
            : putAt_Z_TokenCore_TaskStat_3(var0, var1, var2, var13);
      } else {
         return false;
      }
   }

   private static boolean putAt_Z_TokenCore_TaskStat_3(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, AiPhysics.MotionState var2, double var3
   ) {
      if (var1.onGround() && var2.onGround() && var2.y() > var1.y() + 1.0E-6) {
         return false;
      } else {
         double var5 = var1.x();
         double var7 = var1.z();
         double var9 = var2.x();
         double var11 = var2.z();
         double var13 = var2.y();
         if (!putAt_Z_TokenCore_TaskStat(var0, true, var5, var7, var7, Math.min(var1.y(), var13), Math.max(var1.y(), var13), var3)
            && !putAt_Z_TokenCore_TaskStat(var0, false, var7, var5, var5, Math.min(var1.y(), var13), Math.max(var1.y(), var13), var3)) {
            boolean var15 = Math.abs(var9 - var5) < Math.abs(var11 - var7);
            boolean var16 = var2.horizontalCollision() && var2.vx() == 0.0;
            boolean var17 = var2.horizontalCollision() && var2.vz() == 0.0;
            return var15
               ? putAt_Z_TokenCore_TaskStat(var0, true, var5, var7, var11, var13, var13, var3)
                  || !var17 && putAt_Z_TokenCore_TaskStat_2(var0, true, var5, var7, var11, var13, var3)
                  || putAt_Z_TokenCore_TaskStat(var0, false, var11, var5, var9, var13, var13, var3)
                  || !var16 && putAt_Z_TokenCore_TaskStat_2(var0, false, var11, var5, var9, var13, var3)
               : putAt_Z_TokenCore_TaskStat(var0, false, var7, var5, var9, var13, var13, var3)
                  || !var16 && putAt_Z_TokenCore_TaskStat_2(var0, false, var7, var5, var9, var13, var3)
                  || putAt_Z_TokenCore_TaskStat(var0, true, var9, var7, var11, var13, var13, var3)
                  || !var17 && putAt_Z_TokenCore_TaskStat_2(var0, true, var9, var7, var11, var13, var3);
         } else {
            return true;
         }
      }
   }

   private static boolean putAt_Z_TokenCore_TaskStat_2(
      AiPhysics.PhysicsWorld var0, boolean var1, double var2, double var4, double var6, double var8, double var10
   ) {
      double var12 = var6 - var4;
      if (Math.abs(var12) < 1.0E-9) {
         return false;
      }

      double var14 = 0.3;
      double var16 = Math.signum(var12);
      double var18 = var6 + var16 * var14;
      double var20 = Math.min(var18 + var16 * 1.0E-7, var18 + var16 * 0.002);
      double var22 = Math.max(var18 + var16 * 1.0E-7, var18 + var16 * 0.002);
      double var24 = var8 + 0.001;
      double var26 = var8 + var10 - 0.001;
      double var28 = var2 - var14 + 1.0E-6;
      double var30 = var2 + var14 - 1.0E-6;
      return var1 ? var0.boxCollides(var28, var24, var20, var30, var26, var22) : var0.boxCollides(var20, var24, var28, var22, var26, var30);
   }

   private static boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, boolean var1, double var2, double var4, double var6, double var8, double var10, double var12
   ) {
      double var14 = 0.3;
      double var16 = Math.min(var4, var6) - var14 + 1.0E-6;
      double var18 = Math.max(var4, var6) + var14 - 1.0E-6;
      double var20 = var8 + 0.001;
      double var22 = var10 + var12 - 0.001;

      for (byte var24 = -1; var24 <= 1; var24 += 2) {
         double var25 = var2 + var24 * var14;
         double var27 = Math.min(var25, var25 + var24 * 0.002);
         double var29 = Math.max(var25, var25 + var24 * 0.002);
         boolean var31 = var1 ? var0.boxCollides(var27, var20, var16, var29, var22, var18) : var0.boxCollides(var16, var20, var27, var18, var22, var29);
         if (var31) {
            double var32 = Math.min(var25, var25 + var24 * 1.0E-5);
            double var34 = Math.max(var25, var25 + var24 * 1.0E-5);
            boolean var36 = var1 ? var0.boxCollides(var32, var20, var16, var34, var22, var18) : var0.boxCollides(var16, var20, var32, var18, var22, var34);
            if (!var36) {
               return true;
            }
         }
      }

      return false;
   }

   private static AiPhysics.MotionState putAt_TokenCore_TableCac_TokenCore_TableCac(AiPhysics.MotionState var0, double var1, double var3) {
      return new AiPhysics.MotionState(
         var1,
         var0.y(),
         var3,
         var0.vx(),
         var0.vy(),
         var0.vz(),
         var0.onGround(),
         var0.jumpDelay(),
         var0.horizontalCollision(),
         var0.verticalCollision(),
         var0.support(),
         var0.sprintCut(),
         var0.pose(),
         var0.stuck()
      );
   }

   private static boolean fill(AiPhysics.PhysicsWorld var0, boolean var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      double var14 = Math.min(var2, var4);
      double var16 = Math.max(var2, var4);
      return var1 ? var0.boxCollides(var14, var10, var6 - var8, var16, var12, var6 + var8) : var0.boxCollides(var6 - var8, var10, var14, var6 + var8, var12, var16);
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, double var2) {
      return putAt_Z_TokenCore_TaskStat(var0, var1.x(), var1.y(), var1.z(), var2);
   }

   static boolean putAt_Z_TokenCore_TaskStat(AiPhysics.PhysicsWorld var0, double var1, double var3, double var5, double var7) {
      double var9 = 0.3 - var7;
      return var0.boxCollides(var1 - var9, var3 - 0.02, var5 - var9, var1 + var9, var3 - 1.0E-4, var5 + var9);
   }

   private static long putAt_J_TextPool_TaskState(AiPaceScript.Inner_TaskState var0) {
      return putAt_J_TokenCore_TableCac(var0.putAt_TokenCore_TableCac, var0.putAt_F, var0.flowTemp);
   }

   private static long putAt_J_TokenCore_TableCac(AiPhysics.MotionState var0, float var1, int var2) {
      long var3 = Math.round(var0.x() * 4.0);
      var3 = var3 * 31L + Math.round(var0.y() * 4.0);
      var3 = var3 * 31L + Math.round(var0.z() * 4.0);
      var3 = var3 * 31L + Math.round(var0.vx() * 10.0);
      var3 = var3 * 31L + Math.round(var0.vz() * 10.0);
      var3 = var3 * 31L + (var0.onGround() ? 1 : 0);
      var3 = var3 * 31L + var2;
      return var3 * 31L + Math.round(var1 / 30.0);
   }

   public static boolean putAt_Z_TokenCore_TaskStat_2(
      AiPhysics.PhysicsWorld var0, AiPaceScript.Inner_IoPool var1, AiPaceScript.Inner_JsonCache var2, AiPaceScript.Inner_TextState var3, AiPaceScript.Inner_IoHelper var4
   ) {
      return putAt(var0, var1, var2, var3, var4, null);
   }

   private static boolean putAt(
      AiPhysics.PhysicsWorld var0,
      AiPaceScript.Inner_IoPool var1,
      AiPaceScript.Inner_JsonCache var2,
      AiPaceScript.Inner_TextState var3,
      AiPaceScript.Inner_IoHelper var4,
      LongAdder var5
   ) {
      if (!var2.plateGoal() && var2.checkpoints().length == 1 && !(var3.weight() <= 1.05)) {
         for (AiPaceScript.Inner_JsonEntry var9 : var2.planned()) {
            if (var9 != null) {
               return false;
            }
         }

         for (boolean var64 : var2.free()) {
            if (var64) {
               return false;
            }
         }

         BlockPos var60 = var2.checkpoints()[0];
         double var62 = var60.getX() + 0.5;
         double var65 = var60.getZ() + 0.5;
         int var11 = Math.min(var3.maxTicks(), Math.max(48, (int)Math.ceil(var2.timeLeft(var1.progress()) * 2.2) + 36));
         AiPaceScript.Inner_TableCache var12 = putAt_ThreadLocal.get();
         ArrayList var13 = new ArrayList(Math.min(var11, 64));
         ArrayList var14 = new ArrayList(Math.min(var11, 64));
         ArrayList var15 = new ArrayList(Math.min(var11, 64));
         AiPhysics.MotionState var16 = var1.state();
         float var17 = var1.yaw();
         int var18 = var1.seg();
         double var19 = var1.progress();
         double var21 = var19;
         boolean var23 = onClimbable(var0, var16);
         if (var16.onGround() && !var23 && prevEx(var0, var16)) {
            return false;
         }

         boolean var24 = !var16.onGround() || var23 || putAt_Z_TokenCore_TaskStat(var0, var16, 0.002);
         int var25 = var16.pose() != 0 ? 3 : 0;
         double var26 = var16.y();
         int var28 = 0;

         for (int var29 = 0; var29 < var11; var29++) {
            boolean var30 = (!var16.onGround() || !(var16.vy() <= 0.1)) && !var23;
            double var31 = var2.length() - var19;
            float var33 = putAt_F_TokenCore_TableCac(var16, var19, var31, var23, var62, var65, var2, var17);
            float var34 = putAt_F_F(var17, var33, var3.maxTurn());
            AiPhysics.MoveInput var35 = null;
            AiPhysics.MotionState var36 = null;
            boolean var37 = false;
            int var38 = var18;
            double var39 = var19;
            double var41 = 0.0;
            if (var23) {
               double[] var67 = var2.at(Math.min(var2.length(), var19 + 1.2));
               AiPhysics.MoveInput[] var69;
               if (var67[1] > var16.y() + 0.12) {
                  boolean var72 = AiLadderJump.canClimbAt(var0, BlockPos.containing(var16.x(), var16.y(), var16.z()));
                  AiPhysics.MoveInput var75 = new AiPhysics.MoveInput(
                     0.0, 0.0, true, false, putAt_Z_TokenCore_TaskStat_4(var0, var16, true), var34
                  );
                  AiPhysics.MoveInput var77 = new AiPhysics.MoveInput(1.0, 0.0, true, false, false, var34);
                  var69 = var72 ? new AiPhysics.MoveInput[]{var75, var77} : new AiPhysics.MoveInput[]{var77, var75};
               } else if (var67[1] < var16.y() - 0.25) {
                  var69 = new AiPhysics.MoveInput[]{
                     AiPhysics.MoveInput.NONE.withYaw(var34), new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                  };
               } else {
                  var69 = new AiPhysics.MoveInput[]{
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                  };
               }

               for (AiPhysics.MoveInput var80 : var69) {
                  AiPhysics.MotionState var82 = putAt_TokenCore_TableCac_TokenCore_TaskStat(
                     var0, var16, var80, var23, var25, var18, var19, var26, var2, var3, var12, var5
                  );
                  if (var82 != null) {
                     var35 = var80;
                     var36 = var82;
                     var37 = onClimbable(var0, var36);
                     var38 = var12.flowTemp;
                     var39 = var12.fill_D;
                     var41 = var12.readEx_D - var36.y();
                     break;
                  }
               }
            } else if (!var30 && var2.centerGoal() && putAt_Z_TokenCore_TaskStat_3(var0, var16, var60)) {
               double var66 = Math.hypot(var16.x() - var62, var16.z() - var65);
               boolean var71 = false;
               double var74 = 1.0 / 0.0;
               AiPhysics.MoveInput[] var79 = var3.allowSprint() && var66 > 0.45
                  ? new AiPhysics.MoveInput[]{
                     new AiPhysics.MoveInput(1.0, 0.0, false, true, false, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var34),
                     putAt(var34, var16),
                     AiPhysics.MoveInput.NONE.withYaw(var34)
                  }
                  : new AiPhysics.MoveInput[]{
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, true, var34),
                     putAt(var34, var16),
                     AiPhysics.MoveInput.NONE.withYaw(var34)
                  };

               for (AiPhysics.MoveInput var88 : var79) {
                  AiPhysics.MotionState var90 = putAt_TokenCore_TableCac_TokenCore_TaskStat(
                     var0, var16, var88, false, var25, var18, var19, var26, var2, var3, var12, var5
                  );
                  if (var90 != null) {
                     boolean var92 = onClimbable(var0, var90);
                     if (putAt_Z_TokenCore_TaskStat_3(var0, var90, var60)) {
                        boolean var94 = var90.onGround() && var90.vy() <= 0.1 || var92;
                        boolean var96 = putAt(var0, var90, var94, var60, false, true);
                        double var57 = var96 ? Math.hypot(var90.vx(), var90.vz()) : Math.hypot(var90.x() - var62, var90.z() - var65);
                        if (var96 && !var71 || var96 == var71 && var57 < var74) {
                           var71 = var96;
                           var74 = var57;
                           var35 = var88;
                           var36 = var90;
                           var37 = var92;
                           var38 = var12.flowTemp;
                           var39 = var12.fill_D;
                           var41 = var12.readEx_D - var36.y();
                        }
                     }
                  }
               }
            } else {
               float var43 = Math.abs(Mth.wrapDegrees(var33 - var34));
               boolean var44 = var2.centerGoal() && var31 <= 0.85;
               boolean var45 = var3.allowSprint() && var43 <= 42.0F && !var44;
               boolean var46 = !var30 && var24;
               boolean var47 = var46 && putAt_Z_TokenCore_TaskStat(var0, var16, var34, var45, var18, var19, var2);
               boolean var48 = var46
                  && !var47
                  && var3.allowHops()
                  && var45
                  && var31 > 4.0
                  && var43 <= 14.0F
                  && putAt_Z_TokenCore_TaskStat(var0, var16, var19, var2);
               AiPhysics.MoveInput[] var49;
               if (var47) {
                  var49 = var45
                     ? new AiPhysics.MoveInput[]{
                        new AiPhysics.MoveInput(1.0, 0.0, true, true, false, var34),
                        new AiPhysics.MoveInput(1.0, 0.0, true, false, false, var34),
                        new AiPhysics.MoveInput(1.0, 0.0, false, true, false, var34),
                        new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                     }
                     : new AiPhysics.MoveInput[]{
                        new AiPhysics.MoveInput(1.0, 0.0, true, false, false, var34),
                        new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                     };
               } else if (var48) {
                  var49 = new AiPhysics.MoveInput[]{
                     new AiPhysics.MoveInput(1.0, 0.0, true, true, false, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, true, false, var34),
                     new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                  };
               } else {
                  var49 = var45
                     ? new AiPhysics.MoveInput[]{
                        new AiPhysics.MoveInput(1.0, 0.0, false, true, false, var34),
                        new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)
                     }
                     : new AiPhysics.MoveInput[]{new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var34)};
               }

               AiPhysics.MoveInput[] var50 = var49;
               int var51 = var50.length;
               int var52 = 0;

               while (var52 < var51) {
                  AiPhysics.MoveInput var53;
                  AiPhysics.MotionState var54;
                  label290: {
                     var53 = var50[var52];
                     var54 = putAt_TokenCore_TableCac_TokenCore_TaskStat(var0, var16, var53, false, var25, var18, var19, var26, var2, var3, var12, var5);
                     if (var54 != null) {
                        if (var53.jump() || !var46 || !var54.horizontalCollision() || var47) {
                           break label290;
                        }

                        AiPhysics.MoveInput var55 = new AiPhysics.MoveInput(1.0, 0.0, true, var53.sprint(), false, var34);
                        AiPhysics.MotionState var56 = putAt_TokenCore_TableCac_TokenCore_TaskStat(
                           var0, var16, var55, false, var25, var18, var19, var26, var2, var3, var12, var5
                        );
                        if (var56 != null && !var56.horizontalCollision()) {
                           var35 = var55;
                           var36 = var56;
                           var37 = onClimbable(var0, var36);
                           var38 = var12.flowTemp;
                           var39 = var12.fill_D;
                           var41 = var12.readEx_D - var36.y();
                           break;
                        }

                        var54 = putAt_TokenCore_TableCac_TokenCore_TaskStat(var0, var16, var53, false, var25, var18, var19, var26, var2, var3, var12, var5);
                        if (var54 != null) {
                           break label290;
                        }
                     }

                     var52++;
                     continue;
                  }

                  var35 = var53;
                  var36 = var54;
                  var37 = onClimbable(var0, var36);
                  var38 = var12.flowTemp;
                  var39 = var12.fill_D;
                  var41 = var12.readEx_D - var36.y();
                  break;
               }

               if (var35 == null) {
                  for (float var89 : new float[]{-18.0F, 18.0F}) {
                     float var91 = putAt_F_F(var17, var33 + var89, var3.maxTurn());
                     AiPhysics.MoveInput var93 = new AiPhysics.MoveInput(1.0, 0.0, false, false, false, var91);
                     AiPhysics.MotionState var95 = putAt_TokenCore_TableCac_TokenCore_TaskStat(
                        var0, var16, var93, false, var25, var18, var19, var26, var2, var3, var12, var5
                     );
                     if (var95 != null) {
                        var35 = var93;
                        var36 = var95;
                        var37 = onClimbable(var0, var36);
                        var38 = var12.flowTemp;
                        var39 = var12.fill_D;
                        var41 = var12.readEx_D - var36.y();
                        break;
                     }
                  }
               }
            }

            if (var35 == null || var36 == null) {
               return false;
            }

            boolean var68 = var36.onGround() && var36.vy() <= 0.1 || var37;
            boolean var70 = !var36.onGround() || var37 || fill(var0, var16, var36);
            if (var30 && var68 && !var70) {
               return false;
            }

            if (var68) {
               var26 = var36.y();
            } else {
               var26 = Math.max(var26, var36.y());
            }

            var25 = var35.sneak() ? var25 + 1 : 0;
            var13.add(var35);
            var14.add(var36);
            var15.add(var38);
            var16 = var36;
            var23 = var37;
            var24 = var70;
            var17 = var35.yaw();
            var18 = var38;
            var19 = Math.min(var39, var2.cum()[var2.checkpointPoint()[0]]);
            if (putAt(var0, var16, var68, var60, false, var2.centerGoal())) {
               if (var16.onGround() && !var23 && var41 > 0.3) {
                  return false;
               }

               var4.putAt(var13, var14, var15);
               var4.finish(true, null);
               return true;
            }

            if (var19 > var21 + 0.15) {
               var21 = var19;
               var28 = 0;
            } else if (++var28 > 22) {
               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static AiPhysics.MotionState putAt_TokenCore_TableCac_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0,
      AiPhysics.MotionState var1,
      AiPhysics.MoveInput var2,
      boolean var3,
      int var4,
      int var5,
      double var6,
      double var8,
      AiPaceScript.Inner_JsonCache var10,
      AiPaceScript.Inner_TextState var11,
      AiPaceScript.Inner_TableCache var12,
      LongAdder var13
   ) {
      AiPhysics.MotionState var14 = AiPhysics.stepWithClimbable(var1, var2, var0);
      if (var13 != null) {
         var13.increment();
      }

      int var15 = var2.sneak() ? var4 + 1 : 0;
      if (var15 < 3 && var14.pose() != 0 && putAt_Z_TokenCore_TaskStat_2(var0, var1, var14)) {
         return null;
      }

      double var16 = Math.sqrt(fill_D_D(var14.x() - var1.x()) + fill_D_D(var14.y() - var1.y()) + fill_D_D(var14.z() - var1.z()));
      putAt_V_TokenCore_TableCac(var14, var5, var10, var6 + 1.5 * var16 + 1.0, var12);
      if (var12.flowTemp >= 0 && !(var12.putAt_D > 3.0)) {
         boolean var18 = onClimbable(var0, var14);
         boolean var19 = var14.onGround() && var14.vy() <= 0.1 || var18;
         if (var19 && !var18 && var8 - var14.y() > var0.dropLimit() + 1.0E-5) {
            return null;
         } else {
            double var20 = var12.readEx_D - var14.y();
            if (!var10.freeSegment(var12.flowTemp) && var20 > (var19 ? 0.3 : 1.2)) {
               return null;
            } else {
               var var22 = var11.allowedCell();
               if (var22 != null && (var14.onGround() || var18) && !var22.test(BlockPos.containing(var14.x(), var14.y() + 1.0E-4, var14.z()))) {
                  return null;
               } else {
                  return !AiPhysics.hitsHazard(var0, var1, var14)
                        && !putAt_Z_TokenCore_TaskStat_3(var0, var1, var14, var3, var18)
                        && !putAt(var0, var1, var14, var3, var18, var12)
                        && (var3 || !fill(var0, var1, var2, var14))
                     ? var14
                     : null;
               }
            }
         }
      } else {
         return null;
      }
   }

   private static float putAt_F_TokenCore_TableCac(
      AiPhysics.MotionState var0, double var1, double var3, boolean var5, double var6, double var8, AiPaceScript.Inner_JsonCache var10, float var11
   ) {
      if (!var5 && var3 <= 2.2) {
         double var16 = var6 - var0.x();
         double var14 = var8 - var0.z();
         return var16 * var16 + var14 * var14 >= 0.0064 ? AiPhysics.yawBetween(var0.x(), var0.z(), var6, var8) : var11;
      } else {
         float var12 = var10.yawAt(var0.x(), var0.z(), var1 + 1.2);
         float var13 = var10.yawAt(var0.x(), var0.z(), var1 + 2.5);
         if (Float.isNaN(var13)) {
            return Float.isNaN(var12) ? var11 : var12;
         } else {
            return !Float.isNaN(var12) && Math.abs(Mth.wrapDegrees(var12 - var13)) > 32.0F ? var12 : var13;
         }
      }
   }

   private static boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, float var2, boolean var3, int var4, double var5, AiPaceScript.Inner_JsonCache var7
   ) {
      double var8 = var1.y();
      double var10 = Math.hypot(var1.vx(), var1.vz());
      double var12 = var3 ? (var10 > 0.15 ? 1.55 : 1.32) : 1.12;
      int var14 = Math.min(var7.segments(), var4 + 3);

      for (int var15 = Math.max(0, var4); var15 < var14; var15++) {
         double var16 = var7.y()[var15 + 1];
         if (var16 - var8 > 0.65) {
            double var18 = Math.hypot(var7.x()[var15 + 1] - var1.x(), var7.z()[var15 + 1] - var1.z());
            if (var18 <= var12) {
               return true;
            }
         }
      }

      double var26 = Math.toRadians(var2);
      double var17 = -Math.sin(var26);
      double var19 = Math.cos(var26);
      double var21 = var1.x() + var17 * 0.5;
      double var23 = var1.z() + var19 * 0.5;
      if (!putAt_Z_TokenCore_TaskStat(var0, var21, var8, var23, 0.0)) {
         double[] var25 = var7.at(Math.min(var7.length(), var5 + 1.8));
         if (var25[1] >= var8 - 0.35) {
            return true;
         }
      }

      return false;
   }

   private static boolean putAt_Z_TokenCore_TaskStat(
      AiPhysics.PhysicsWorld var0, AiPhysics.MotionState var1, double var2, AiPaceScript.Inner_JsonCache var4
   ) {
      double[] var5 = var4.at(var2 + 1.8);
      double[] var6 = var4.at(var2 + 4.2);
      if (!(Math.abs(var5[1] - var1.y()) > 0.15) && !(Math.abs(var6[1] - var1.y()) > 0.15)) {
         float var7 = var4.yawAt(var1.x(), var1.z(), var2 + 1.8);
         float var8 = var4.yawAt(var1.x(), var1.z(), var2 + 4.2);
         if (!Float.isNaN(var7) && !Float.isNaN(var8) && !(Math.abs(Mth.wrapDegrees(var7 - var8)) > 18.0F)) {
            double var9 = 0.3;
            return !var0.boxCollides(var1.x() - var9, var1.y() + 1.8 + 0.05, var1.z() - var9, var1.x() + var9, var1.y() + 1.8 + 0.9, var1.z() + var9);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static double fill_D_D(double var0) {
      return var0 * var0;
   }

   public record Inner_BitCache(double beforeFirstCheckpoint, double afterFirstCheckpoint, int switchCheckpoint) {
      public static AiPaceScript.Inner_BitCache uniform(double var0) {
         return new AiPaceScript.Inner_BitCache(var0, var0, -1);
      }

      public static AiPaceScript.Inner_BitCache switchAtFirstCheckpoint(double var0, double var2) {
         double var4 = Double.isFinite(var2) && var2 > 0.0 ? var2 : 0.1;
         double var6 = Double.isFinite(var0) && var0 > 0.0 ? var0 : var4;
         return Math.abs(var6 - var4) <= 1.0E-6 ? uniform(var4) : new AiPaceScript.Inner_BitCache(var6, var4, 0);
      }

      public double speedFor(int var1) {
         return this.switchCheckpoint >= 0 && var1 <= this.switchCheckpoint ? this.beforeFirstCheckpoint : this.afterFirstCheckpoint;
      }

      public boolean hasTransition() {
         return this.switchCheckpoint >= 0 && Math.abs(this.beforeFirstCheckpoint - this.afterFirstCheckpoint) > 1.0E-6;
      }
   }

   record Inner_BitCore(int seg, double dist, double progress, double height) {
   }

   public record Inner_CacheHelper(BlockPos target, int point, boolean checkpoint) {
   }

   private static final class Inner_EventBus implements AiPhysics.PhysicsWorld {
      private final AiPhysics.PhysicsWorld putAt_TokenCore_TaskStat;
      private final double putAt_D;

      Inner_EventBus(AiPhysics.PhysicsWorld var1, double var2) {
         this.putAt_TokenCore_TaskStat = var1;
         this.putAt_D = var2;
      }

      @Override
      public double dropLimit() {
         return this.putAt_TokenCore_TaskStat.dropLimit();
      }

      @Override
      public double movementSpeed() {
         return this.putAt_D;
      }

      @Override
      public List<AABB> collisions(AABB var1) {
         return this.putAt_TokenCore_TaskStat.collisions(var1);
      }

      @Override
      public Vec3 collideMotion(Vec3 var1, AABB var2, AABB var3) {
         return this.putAt_TokenCore_TaskStat.collideMotion(var1, var2, var3);
      }

      @Override
      public Vec3 collideMotionRaw(
         double var1,
         double var3,
         double var5,
         double var7,
         double var9,
         double var11,
         double var13,
         double var15,
         double var17,
         double var19,
         double var21,
         double var23,
         double var25,
         double var27,
         double var29
      ) {
         return this.putAt_TokenCore_TaskStat.collideMotionRaw(var1, var3, var5, var7, var9, var11, var13, var15, var17, var19, var21, var23, var25, var27, var29);
      }

      @Override
      public boolean putAt_Z_AABB(AABB var1) {
         return this.putAt_TokenCore_TaskStat.putAt_Z_AABB(var1);
      }

      @Override
      public boolean boxCollides(double var1, double var3, double var5, double var7, double var9, double var11) {
         return this.putAt_TokenCore_TaskStat.boxCollides(var1, var3, var5, var7, var9, var11);
      }

      @Override
      public long findSupport(AABB var1, double var2, double var4, double var6) {
         return this.putAt_TokenCore_TaskStat.findSupport(var1, var2, var4, var6);
      }

      @Override
      public long findSupportRaw(double var1, double var3, double var5, double var7, double var9, double var11, double var13, double var15, double var17) {
         return this.putAt_TokenCore_TaskStat.findSupportRaw(var1, var3, var5, var7, var9, var11, var13, var15, var17);
      }

      @Override
      public double frictionAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.frictionAt(var1, var2, var3);
      }

      @Override
      public double speedFactorAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.speedFactorAt(var1, var2, var3);
      }

      @Override
      public boolean slimeAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.slimeAt(var1, var2, var3);
      }

      @Override
      public double bounceAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.bounceAt(var1, var2, var3);
      }

      @Override
      public double jumpFactorAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.jumpFactorAt(var1, var2, var3);
      }

      @Override
      public boolean climbableAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.climbableAt(var1, var2, var3);
      }

      @Override
      public int putAt_I_AABB(AABB var1) {
         return this.putAt_TokenCore_TaskStat.putAt_I_AABB(var1);
      }

      @Override
      public boolean stuckBlocksTracked() {
         return true;
      }

      @Override
      public boolean putAt_Z_I_2(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.putAt_Z_I_2(var1, var2, var3);
      }

      @Override
      public boolean waterAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.waterAt(var1, var2, var3);
      }

      @Override
      public boolean hazardAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.hazardAt(var1, var2, var3);
      }

      @Override
      public boolean contactHazardAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.contactHazardAt(var1, var2, var3);
      }

      @Override
      public boolean floorHazardAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.floorHazardAt(var1, var2, var3);
      }

      @Override
      public boolean invisibleLightAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.invisibleLightAt(var1, var2, var3);
      }

      @Override
      public boolean pressurePlateAt(int var1, int var2, int var3) {
         return this.putAt_TokenCore_TaskStat.pressurePlateAt(var1, var2, var3);
      }

      @Override
      public boolean isRegionPassable(int var1, int var2, int var3, int var4, int var5, int var6, int var7) {
         return this.putAt_TokenCore_TaskStat.isRegionPassable(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   static final class Inner_HashEntry {
      private final Supplier<AiPhysics.PhysicsWorld> putAt_Supplier;
      private final ConcurrentLinkedQueue<AiPhysics.PhysicsWorld> putAt_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();

      Inner_HashEntry(Supplier<AiPhysics.PhysicsWorld> var1) {
         this.putAt_Supplier = var1;
      }

      static AiPaceScript.Inner_HashEntry putAt_TextPool_HashEntry_TokenCore_TaskStat(AiPhysics.PhysicsWorld var0) {
         AiPaceScript.Inner_HashEntry var1 = new AiPaceScript.Inner_HashEntry(null);
         var1.putAt_ConcurrentLinkedQu.add(var0);
         return var1;
      }

      boolean putAt_Z() {
         return this.putAt_Supplier != null && AiPaceScript.getTo > 1;
      }

      AiPhysics.PhysicsWorld putAt_TokenCore_TaskStat() {
         AiPhysics.PhysicsWorld var1 = this.putAt_ConcurrentLinkedQu.poll();
         return var1 != null ? var1 : this.putAt_Supplier.get();
      }

      void putAt_V_TokenCore_TaskStat(AiPhysics.PhysicsWorld var1) {
         this.putAt_ConcurrentLinkedQu.add(var1);
      }
   }

   private record Inner_HashPool(Long2IntOpenHashMap seen, double target, double bestGoalF, boolean fastTravel, AiPaceScript.Inner_BitCache speedProfile) {
      static final AiPaceScript.Inner_HashPool UNBOUNDED = new AiPaceScript.Inner_HashPool(null, 1.0 / 0.0, 1.0 / 0.0, false, null);
   }

   public static final class Inner_IoHelper {
      private final List<AiPhysics.MoveInput> putAt_List = new CopyOnWriteArrayList<>();
      private final List<AiPhysics.MotionState> fill_List = new CopyOnWriteArrayList<>();
      private final List<Integer> readEx = new CopyOnWriteArrayList<>();
      private final List<Double> value = new CopyOnWriteArrayList<>();
      private volatile int flowState;
      private volatile int flowTemp;
      private volatile double putAt_D;
      private volatile boolean putAt_Z;
      private volatile boolean fill_Z;
      private volatile AiPaceScript.Inner_StreamState putAt_TextPool_StreamSta;
      private volatile BlockPos[] putAt_BlockPosArr;

      synchronized void putAt(List<AiPhysics.MoveInput> var1, List<AiPhysics.MotionState> var2, List<Integer> var3) {
         this.putAt(var1, var2, var3, null);
      }

      synchronized void putAt(List<AiPhysics.MoveInput> var1, List<AiPhysics.MotionState> var2, List<Integer> var3, List<Double> var4) {
         this.putAt_List.addAll(var1);
         this.fill_List.addAll(var2);
         this.readEx.addAll(var3);
         if (var4 != null) {
            this.value.addAll(var4);
         }

         this.flowState = this.putAt_List.size();
      }

      synchronized boolean putAt_Z_I(int var1) {
         if (var1 < this.flowTemp) {
            return false;
         }

         while (this.putAt_List.size() > var1) {
            this.putAt_List.removeLast();
            this.fill_List.removeLast();
            this.readEx.removeLast();
         }

         while (this.value.size() > var1) {
            this.value.removeLast();
         }

         this.flowState = Math.min(this.flowState, var1);
         return true;
      }

      public synchronized double sampleAt(int var1) {
         return var1 >= 0 && var1 < this.value.size() ? this.value.get(var1) : 0.0 / 0.0;
      }

      public synchronized int flowState() {
         int var1 = Math.min(this.flowState, this.value.size());
         if (var1 <= 1) {
            return -1;
         }

         double var2 = this.value.get(0);

         for (int var4 = 1; var4 < var1; var4++) {
            if (Math.abs(this.value.get(var4) - var2) > 1.0E-6) {
               return var4;
            }
         }

         return -1;
      }

      public synchronized AiPhysics.MoveInput putAt_TokenCore_CacheHel_I(int var1) {
         if (var1 >= this.flowState) {
            return null;
         }

         this.flowTemp = Math.max(this.flowTemp, var1 + 1);
         return this.putAt_List.get(var1);
      }

      public synchronized AiPhysics.MotionState putAt_TokenCore_TableCac_I(int var1) {
         return var1 >= 0 && var1 < this.flowState ? this.fill_List.get(var1) : null;
      }

      public synchronized int putAt_I_I(int var1) {
         return var1 >= 0 && var1 < this.flowState ? this.readEx.get(var1) : 0;
      }

      public void finish(boolean var1, AiPaceScript.Inner_StreamState var2) {
         this.fill_Z = var1;
         this.putAt_TextPool_StreamSta = var2;
         this.putAt_Z = true;
      }

      public AiPaceScript.Inner_StreamState putAt_TextPool_StreamSta() {
         return this.putAt_TextPool_StreamSta;
      }

      void putAt_V_BlockPosArr(BlockPos[] var1) {
         this.putAt_BlockPosArr = var1;
      }

      BlockPos[] putAt_BlockPosArr() {
         return this.putAt_BlockPosArr;
      }

      void putAt_V_D(double var1) {
         if (var1 > this.putAt_D) {
            this.putAt_D = var1;
         }
      }

      public double putAt_D() {
         return this.putAt_D;
      }

      public int size() {
         return this.flowState;
      }

      public AiPhysics.MoveInput fill_TokenCore_CacheHel(int var1) {
         return this.putAt_List.get(var1);
      }

      public AiPhysics.MotionState fill_TokenCore_TableCac(int var1) {
         return this.fill_List.get(var1);
      }

      public boolean isFinished() {
         return this.putAt_Z;
      }

      public boolean finishedOk() {
         return this.fill_Z;
      }

      List<AiPhysics.MoveInput> putAt_List() {
         return this.putAt_List;
      }

      List<AiPhysics.MotionState> fill_List() {
         return this.fill_List;
      }
   }

   public record Inner_IoPool(AiPhysics.MotionState state, float yaw, int seg, int touched, double progress) {
      public static AiPaceScript.Inner_IoPool of(AiPhysics.MotionState var0, float var1) {
         return new AiPaceScript.Inner_IoPool(var0, var1, 0, 0, 0.0);
      }
   }

   public record Inner_JsonCache(
      double[] x,
      double[] y,
      double[] z,
      double[] cum,
      int[] checkpointPoint,
      BlockPos[] checkpoints,
      BlockPos[] cells,
      int[] cellPoint,
      boolean[] free,
      boolean[] plainCell,
      AiPaceScript.Inner_JsonEntry[] planned,
      boolean[] climb,
      double[] time,
      double flatSpeed,
      boolean plateGoal,
      boolean centerGoal
   ) {
      public Inner_JsonCache(
         double[] var1,
         double[] var2,
         double[] var3,
         double[] var4,
         int[] var5,
         BlockPos[] var6,
         BlockPos[] var7,
         int[] var8,
         boolean[] var9,
         boolean[] var10,
         AiPaceScript.Inner_JsonEntry[] var11,
         boolean[] var12,
         double[] var13,
         double var14
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, true, false);
      }

      public Inner_JsonCache(
         double[] var1,
         double[] var2,
         double[] var3,
         double[] var4,
         int[] var5,
         BlockPos[] var6,
         BlockPos[] var7,
         int[] var8,
         boolean[] var9,
         boolean[] var10,
         AiPaceScript.Inner_JsonEntry[] var11,
         boolean[] var12,
         double[] var13,
         double var14,
         boolean var16
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var13, var14, var16, false);
      }

      public static AiPaceScript.Inner_JsonCache of(List<double[]> var0, List<Integer> var1, List<BlockPos> var2) {
         return of(var0, var1, var2, List.of(), List.of(), new boolean[var0.size()], new boolean[0]);
      }

      public static AiPaceScript.Inner_JsonCache of(
         List<double[]> var0, List<Integer> var1, List<BlockPos> var2, List<BlockPos> var3, List<Integer> var4, boolean[] var5, boolean[] var6
      ) {
         return of(var0, var1, var2, var3, var4, var5, var6, new AiPaceScript.Inner_JsonEntry[var3.size()], new boolean[var0.size()], 0.357, true);
      }

      public static AiPaceScript.Inner_JsonCache of(
         List<double[]> var0,
         List<Integer> var1,
         List<BlockPos> var2,
         List<BlockPos> var3,
         List<Integer> var4,
         boolean[] var5,
         boolean[] var6,
         AiPaceScript.Inner_JsonEntry[] var7,
         boolean[] var8
      ) {
         return of(var0, var1, var2, var3, var4, var5, var6, var7, var8, 0.357, true);
      }

      public static AiPaceScript.Inner_JsonCache of(
         List<double[]> var0,
         List<Integer> var1,
         List<BlockPos> var2,
         List<BlockPos> var3,
         List<Integer> var4,
         boolean[] var5,
         boolean[] var6,
         AiPaceScript.Inner_JsonEntry[] var7,
         boolean[] var8,
         double var9
      ) {
         return of(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, true);
      }

      public static AiPaceScript.Inner_JsonCache of(
         List<double[]> var0,
         List<Integer> var1,
         List<BlockPos> var2,
         List<BlockPos> var3,
         List<Integer> var4,
         boolean[] var5,
         boolean[] var6,
         AiPaceScript.Inner_JsonEntry[] var7,
         boolean[] var8,
         double var9,
         boolean var11
      ) {
         return of(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, var11, false);
      }

      public static AiPaceScript.Inner_JsonCache of(
         List<double[]> var0,
         List<Integer> var1,
         List<BlockPos> var2,
         List<BlockPos> var3,
         List<Integer> var4,
         boolean[] var5,
         boolean[] var6,
         AiPaceScript.Inner_JsonEntry[] var7,
         boolean[] var8,
         double var9,
         boolean var11,
         boolean var12
      ) {
         return ofWithSplitSpeed(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9, var9, var11, var12);
      }

      static AiPaceScript.Inner_JsonCache ofWithSplitSpeed(
         List<double[]> var0,
         List<Integer> var1,
         List<BlockPos> var2,
         List<BlockPos> var3,
         List<Integer> var4,
         boolean[] var5,
         boolean[] var6,
         AiPaceScript.Inner_JsonEntry[] var7,
         boolean[] var8,
         double var9,
         double var11,
         boolean var13,
         boolean var14
      ) {
         int var15 = var0.size();
         double var16 = Double.isFinite(var11) && var11 > 0.05 ? var11 : 0.357;
         double var18 = Double.isFinite(var9) && var9 > 0.05 ? var9 : var16;
         double var20 = Math.max(var18, var16);
         int var22 = var1.size() >= 2 ? (Integer)var1.get(0) : -1;
         double[] var23 = new double[var15];
         double[] var24 = new double[var15];
         double[] var25 = new double[var15];
         double[] var26 = new double[var15];
         double[] var27 = new double[var15];
         boolean[] var28 = Arrays.copyOf(var8, var15);
         boolean[] var29 = Arrays.copyOf(var5, var15);
         int[] var30 = var4.stream().mapToInt(Integer::intValue).toArray();
         AiPaceScript.Inner_JsonEntry[] var31 = Arrays.copyOf(var7, var3.size());

         for (int var32 = 0; var32 < var15; var32++) {
            var23[var32] = ((double[])var0.get(var32))[0];
            var24[var32] = ((double[])var0.get(var32))[1];
            var25[var32] = ((double[])var0.get(var32))[2];
            if (var32 != 0) {
               double var33 = Math.sqrt(
                  AiPaceScript.fill_D_D(var23[var32] - var23[var32 - 1])
                     + AiPaceScript.fill_D_D(var24[var32] - var24[var32 - 1])
                     + AiPaceScript.fill_D_D(var25[var32] - var25[var32 - 1])
               );
               var26[var32] = var26[var32 - 1] + var33;
               double var35 = var22 >= 0 && var32 <= var22 ? var18 : var16;
               double var37 = var28[var32] ? 0.12 : (var29[var32] ? var35 * 0.65 : var35);
               var27[var32] = var33 / var37;
            }
         }

         for (int var43 = 0; var43 < var31.length; var43++) {
            AiPaceScript.Inner_JsonEntry var45 = var31[var43];
            if (var45 != null && var45.maneuver() != null) {
               int var34 = var30[var43];
               int var47 = var43 == 0 ? 0 : var30[var43 - 1];
               if (var34 > var47 && var34 < var15 && !var28[var34] && var45.maneuver().landing().onGround()) {
                  double var36 = 0.0;

                  for (int var38 = var47 + 1; var38 <= var34; var38++) {
                     var36 += var27[var38];
                  }

                  double var48 = Math.max(0.0, var45.maneuver().inputs().size() - var45.maneuver().takeoffTick() - 2.0);
                  if (var48 > var36) {
                     if (var36 > 1.0E-6) {
                        double var49 = var48 / var36;

                        for (int var50 = var47 + 1; var50 <= var34; var50++) {
                           var27[var50] *= var49;
                        }
                     } else {
                        double var40 = var48 / (var34 - var47);

                        for (int var42 = var47 + 1; var42 <= var34; var42++) {
                           var27[var42] = var40;
                        }
                     }
                  }
               }
            }
         }

         double[] var44 = new double[var15];

         for (int var46 = 1; var46 < var15; var46++) {
            var44[var46] = var44[var46 - 1] + var27[var46];
         }

         return new AiPaceScript.Inner_JsonCache(
            var23,
            var24,
            var25,
            var26,
            var1.stream().mapToInt(Integer::intValue).toArray(),
            var2.toArray(BlockPos[]::new),
            var3.toArray(BlockPos[]::new),
            var30,
            var29,
            Arrays.copyOf(var6, var3.size()),
            var31,
            var28,
            var44,
            var20,
            var13,
            var14
         );
      }

      double timeLeft(double var1) {
         int var3 = 0;
         int var4 = this.cum.length - 1;
         if (var1 >= this.cum[var4]) {
            return 0.0;
         }

         while (var4 - var3 > 1) {
            int var5 = var3 + var4 >>> 1;
            if (this.cum[var5] <= var1) {
               var3 = var5;
            } else {
               var4 = var5;
            }
         }

         double var9 = this.cum[var4] - this.cum[var3];
         double var7 = this.time[var3] + (var9 < 1.0E-9 ? 0.0 : (this.time[var4] - this.time[var3]) * (Math.max(0.0, var1 - this.cum[var3]) / var9));
         return this.time[this.time.length - 1] - var7;
      }

      public static void add(List<double[]> var0, double var1, double var3, double var5) {
         add(var0, var1, var3, var5, false);
      }

      public static void add(List<double[]> var0, double var1, double var3, double var5, boolean var7) {
         double[] var8 = (double[])var0.getLast();
         boolean var9 = Math.abs(var1 - var8[0]) + Math.abs(var5 - var8[2]) > 0.5;
         if (var9 && var3 - var8[1] >= 1.5) {
            var0.add(new double[]{var8[0], var3, var8[2]});
         } else if (var9 && var8[1] - var3 >= 1.5 && !var7) {
            var0.add(new double[]{var1, var8[1], var5});
         }

         var0.add(new double[]{var1, var3, var5});
      }

      int segments() {
         return this.x.length - 1;
      }

      boolean isReversalVertex(int var1) {
         if (var1 > 0 && var1 < this.x.length - 1) {
            double var2 = this.x[var1] - this.x[var1 - 1];
            double var4 = this.z[var1] - this.z[var1 - 1];
            double var6 = this.x[var1 + 1] - this.x[var1];
            double var8 = this.z[var1 + 1] - this.z[var1];
            double var10 = var2 * var2 + var4 * var4;
            double var12 = var6 * var6 + var8 * var8;
            return var10 >= 0.0625 && var12 >= 0.0625 && var2 * var6 + var4 * var8 < -0.5 * Math.sqrt(var10 * var12);
         } else {
            return false;
         }
      }

      double clampLookAtReversal(double var1, double var3) {
         for (int var5 = 1; var5 < this.x.length - 1; var5++) {
            if (!(this.cum[var5] <= var1 + 0.35)) {
               if (this.cum[var5] >= var3) {
                  break;
               }

               if (this.isReversalVertex(var5)) {
                  return this.cum[var5];
               }
            }
         }

         return var3;
      }

      boolean freeSegment(int var1) {
         return var1 >= 0 && var1 + 1 < this.free.length && this.free[var1 + 1];
      }

      public double length() {
         return this.cum[this.cum.length - 1];
      }

      double[] at(double var1) {
         if (var1 <= 0.0) {
            return new double[]{this.x[0], this.y[0], this.z[0]};
         }

         int var3 = this.x.length - 1;
         if (var1 >= this.cum[var3]) {
            return new double[]{this.x[var3], this.y[var3], this.z[var3]};
         }

         int var4 = 0;
         int var5 = var3;

         while (var5 - var4 > 1) {
            int var6 = var4 + var5 >>> 1;
            if (this.cum[var6] < var1) {
               var4 = var6;
            } else {
               var5 = var6;
            }
         }

         double var10 = this.cum[var4 + 1] - this.cum[var4];
         double var8 = var10 < 1.0E-9 ? 1.0 : (var1 - this.cum[var4]) / var10;
         return new double[]{
            this.x[var4] + (this.x[var4 + 1] - this.x[var4]) * var8,
            this.y[var4] + (this.y[var4 + 1] - this.y[var4]) * var8,
            this.z[var4] + (this.z[var4 + 1] - this.z[var4]) * var8
         };
      }

      float yawAt(double var1, double var3, double var5) {
         int var11 = this.x.length - 1;
         double var7;
         double var9;
         if (var5 <= 0.0) {
            var7 = this.x[0];
            var9 = this.z[0];
         } else if (var5 >= this.cum[var11]) {
            var7 = this.x[var11];
            var9 = this.z[var11];
         } else {
            int var12 = 0;
            int var13 = var11;

            while (var13 - var12 > 1) {
               int var14 = var12 + var13 >>> 1;
               if (this.cum[var14] < var5) {
                  var12 = var14;
               } else {
                  var13 = var14;
               }
            }

            double var19 = this.cum[var12 + 1] - this.cum[var12];
            double var16 = var19 < 1.0E-9 ? 1.0 : (var5 - this.cum[var12]) / var19;
            var7 = this.x[var12] + (this.x[var12 + 1] - this.x[var12]) * var16;
            var9 = this.z[var12] + (this.z[var12 + 1] - this.z[var12]) * var16;
         }

         double var18 = var7 - var1;
         double var20 = var9 - var3;
         return var18 * var18 + var20 * var20 >= 0.09 ? AiPhysics.yawBetween(var1, var3, var7, var9) : 0.0F / 0.0F;
      }

      double alongSpeed(double var1, double var3, double var5, double var7, double var9) {
         int var15 = this.x.length - 1;
         double var11;
         double var13;
         if (var9 <= 0.0) {
            var11 = this.x[0];
            var13 = this.z[0];
         } else if (var9 >= this.cum[var15]) {
            var11 = this.x[var15];
            var13 = this.z[var15];
         } else {
            int var16 = 0;
            int var17 = var15;

            while (var17 - var16 > 1) {
               int var18 = var16 + var17 >>> 1;
               if (this.cum[var18] < var9) {
                  var16 = var18;
               } else {
                  var17 = var18;
               }
            }

            double var23 = this.cum[var16 + 1] - this.cum[var16];
            double var20 = var23 < 1.0E-9 ? 1.0 : (var9 - this.cum[var16]) / var23;
            var11 = this.x[var16] + (this.x[var16 + 1] - this.x[var16]) * var20;
            var13 = this.z[var16] + (this.z[var16 + 1] - this.z[var16]) * var20;
         }

         double var22 = var11 - var1;
         double var24 = var13 - var3;
         double var25 = Math.sqrt(var22 * var22 + var24 * var24);
         return var25 > 0.3 ? (var5 * var22 + var7 * var24) / var25 : 0.0;
      }
   }

   public record Inner_JsonEntry(boolean bounce, BlockPos from, float yaw, double surface, boolean sprint, AiJumpManeuver maneuver, List<BlockPos> wallTargets) {
      public Inner_JsonEntry(boolean var1, BlockPos var2, float var3, double var4, boolean var6, AiJumpManeuver var7) {
         this(var1, var2, var3, var4, var6, var7, null);
      }
   }

   public record Inner_PacketHelper(
      List<AiPhysics.MoveInput> inputs,
      List<AiPhysics.MotionState> states,
      int[] checkpointTicks,
      boolean complete,
      int expanded,
      long steps,
      AiPaceScript.Inner_StreamState stop
   ) {
      public int ticks() {
         return this.inputs.size();
      }
   }

   private record Inner_RingBuffer(
      float heading,
      int run,
      boolean jump,
      int air,
      int release,
      int strafe,
      int steerAfter,
      boolean holdJump,
      int ground,
      int airStrafe,
      int after,
      boolean bounceJump,
      List<AiPhysics.MoveInput> keys
   ) {
      Inner_RingBuffer(float var1, int var2, boolean var3, int var4, int var5, int var6, int var7) {
         this(var1, var2, var3, var4, var5, var6, var7, false, 0, 0, 0, false, null);
      }

      Inner_RingBuffer(float var1, int var2, boolean var3, int var4, int var5, int var6, int var7, boolean var8) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, 0, 0, 0, false, null);
      }

      Inner_RingBuffer(float var1, int var2, boolean var3, int var4, int var5, int var6, int var7, boolean var8, int var9, int var10, int var11) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, false, null);
      }

      AiPaceScript.Inner_RingBuffer bouncing() {
         return new AiPaceScript.Inner_RingBuffer(
            this.heading,
            this.run,
            this.jump,
            this.air,
            this.release,
            this.strafe,
            this.steerAfter,
            this.holdJump,
            this.ground,
            this.airStrafe,
            this.after,
            true,
            this.keys
         );
      }

      static AiPaceScript.Inner_RingBuffer keyed(List<AiPhysics.MoveInput> var0) {
         return new AiPaceScript.Inner_RingBuffer(((AiPhysics.MoveInput)var0.getFirst()).yaw(), 0, false, 0, 0, 0, 0, false, 0, 0, 0, false, var0);
      }
   }

   private static final class Inner_RingUtil {
      private static final List<AiPhysics.MoveInput> putAt_List = List.of();
      private static final int flowState = 16;
      private static final int flowTemp = 32;
      private static final int readEx_I = 128;
      private final ConcurrentHashMap<Long, List<AiPhysics.MoveInput>> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
      private final AtomicInteger[] putAt_AtomicIntegerArr;
      private final AtomicInteger[] fill_AtomicIntegerArr;
      private final AtomicInteger[] readEx_AtomicIntegerArr;

      Inner_RingUtil(int var1) {
         this.putAt_AtomicIntegerArr = new AtomicInteger[var1];
         this.fill_AtomicIntegerArr = new AtomicInteger[var1];
         this.readEx_AtomicIntegerArr = new AtomicInteger[var1];

         for (int var2 = 0; var2 < var1; var2++) {
            this.putAt_AtomicIntegerArr[var2] = new AtomicInteger();
            this.fill_AtomicIntegerArr[var2] = new AtomicInteger();
            this.readEx_AtomicIntegerArr[var2] = new AtomicInteger();
         }
      }

      static long putAt(int var0, AiPhysics.MotionState var1, double var2) {
         long var4 = var0;
         var4 = var4 * 31L + Math.round(var1.x() * 8.0);
         var4 = var4 * 31L + Math.round(var1.y() * 4.0);
         var4 = var4 * 31L + Math.round(var1.z() * 8.0);
         var4 = var4 * 31L + Math.round(var1.vx() * 16.0);
         var4 = var4 * 31L + Math.round(var1.vz() * 16.0);
         var4 = var4 * 31L + var1.pose();
         return var4 * 31L + Math.round(var2 * 1000.0);
      }
   }

   public record Inner_StreamState(double progress, double reach, int seg, int touched) {
   }

   private static final class Inner_SyncPool {
      final AiPhysics.MoveInput[] putAt_TokenCore_CacheHelArr = new AiPhysics.MoveInput[120];
      final AiPhysics.MotionState[] putAt_TokenCore_TableCacArr = new AiPhysics.MotionState[120];
      final double[] putAt_DArr = new double[120];
      final float[] putAt_FArr = new float[121];
      final int[] putAt_IArr = new int[121];
      final int[] fill_IArr = new int[121];
      final double[] fill_DArr = new double[121];
      final double[] readEx_DArr = new double[121];
      final double[] value_DArr = new double[121];
      final double[] prevEx_DArr = new double[121];
      final double[] prev = new double[121];
      final int[] readEx_IArr = new int[121];
      final boolean[] putAt_ZArr = new boolean[121];
      final boolean[] fill_ZArr = new boolean[121];
      final int[] value_IArr = new int[121];
      final boolean[] readEx_ZArr = new boolean[121];
      final boolean[] value_ZArr = new boolean[121];
      final int[] prevEx_IArr = new int[121];
      int flowState;
      int flowTemp;
      int readEx_I;
      long putAt_J;
      boolean putAt_Z;
      boolean fill_Z;

      void putAt(float var1, int var2, int var3, double var4, double var6, double var8, int var10, boolean var11) {
         this.putAt_FArr[0] = var1;
         this.putAt_IArr[0] = var2;
         this.fill_IArr[0] = var3;
         this.fill_DArr[0] = var4;
         this.readEx_DArr[0] = var6;
         this.value_DArr[0] = 0.0;
         this.prevEx_DArr[0] = var8;
         this.prev[0] = 0.0;
         this.readEx_IArr[0] = var10;
         this.putAt_ZArr[0] = var11;
         this.fill_ZArr[0] = false;
         this.value_IArr[0] = 0;
         this.readEx_ZArr[0] = false;
         this.value_ZArr[0] = false;
         this.prevEx_IArr[0] = 0;
         this.flowState = 0;
         this.flowTemp = -1;
         this.readEx_I = 0;
         this.putAt_J = 0L;
         this.putAt_Z = false;
         this.fill_Z = false;
      }

      void putAt(AiPaceScript.Inner_SyncPool var1, int var2) {
         System.arraycopy(var1.putAt_TokenCore_CacheHelArr, 0, this.putAt_TokenCore_CacheHelArr, 0, var2);
         System.arraycopy(var1.putAt_TokenCore_TableCacArr, 0, this.putAt_TokenCore_TableCacArr, 0, var2);
         System.arraycopy(var1.putAt_DArr, 0, this.putAt_DArr, 0, var2);
         System.arraycopy(var1.putAt_FArr, 0, this.putAt_FArr, 0, var2 + 1);
         System.arraycopy(var1.putAt_IArr, 0, this.putAt_IArr, 0, var2 + 1);
         System.arraycopy(var1.fill_IArr, 0, this.fill_IArr, 0, var2 + 1);
         System.arraycopy(var1.fill_DArr, 0, this.fill_DArr, 0, var2 + 1);
         System.arraycopy(var1.readEx_DArr, 0, this.readEx_DArr, 0, var2 + 1);
         System.arraycopy(var1.value_DArr, 0, this.value_DArr, 0, var2 + 1);
         System.arraycopy(var1.prevEx_DArr, 0, this.prevEx_DArr, 0, var2 + 1);
         System.arraycopy(var1.prev, 0, this.prev, 0, var2 + 1);
         System.arraycopy(var1.readEx_IArr, 0, this.readEx_IArr, 0, var2 + 1);
         System.arraycopy(var1.putAt_ZArr, 0, this.putAt_ZArr, 0, var2 + 1);
         System.arraycopy(var1.fill_ZArr, 0, this.fill_ZArr, 0, var2 + 1);
         System.arraycopy(var1.value_IArr, 0, this.value_IArr, 0, var2 + 1);
         System.arraycopy(var1.readEx_ZArr, 0, this.readEx_ZArr, 0, var2 + 1);
         System.arraycopy(var1.value_ZArr, 0, this.value_ZArr, 0, var2 + 1);
         System.arraycopy(var1.prevEx_IArr, 0, this.prevEx_IArr, 0, var2 + 1);
         this.flowState = var2;
         this.flowTemp = var1.flowTemp;
         this.readEx_I = var1.value_IArr[var2];
         this.putAt_J = 0L;
         this.putAt_Z = var1.fill_ZArr[var2];
         this.fill_Z = false;
      }

      void putAt(
         int var1,
         AiPhysics.MoveInput var2,
         AiPhysics.MotionState var3,
         double var4,
         float var6,
         int var7,
         int var8,
         double var9,
         double var11,
         double var13,
         double var15,
         double var17,
         int var19,
         boolean var20,
         boolean var21,
         int var22,
         boolean var23,
         boolean var24,
         int var25
      ) {
         this.putAt_TokenCore_CacheHelArr[var1 - 1] = var2;
         this.putAt_TokenCore_TableCacArr[var1 - 1] = var3;
         this.putAt_DArr[var1 - 1] = var4;
         this.putAt_FArr[var1] = var6;
         this.putAt_IArr[var1] = var7;
         this.fill_IArr[var1] = var8;
         this.fill_DArr[var1] = var9;
         this.readEx_DArr[var1] = var11;
         this.value_DArr[var1] = var13;
         this.prevEx_DArr[var1] = var15;
         this.prev[var1] = var17;
         this.readEx_IArr[var1] = var19;
         this.putAt_ZArr[var1] = var20;
         this.fill_ZArr[var1] = var21;
         this.value_IArr[var1] = var22;
         this.readEx_ZArr[var1] = var23;
         this.value_ZArr[var1] = var24;
         this.prevEx_IArr[var1] = var25;
         this.flowState = var1;
      }

      void putAt(int var1, int var2, AiPhysics.MotionState var3, double var4, AiPaceScript.Inner_JsonCache var6, float var7, float var8) {
         if (var1 == 1) {
            boolean var9 = var2 <= 3 ? (this.putAt_J & 7L) == 0L : (var2 <= 6 ? (this.putAt_J & 56L) == 0L : (this.putAt_J & -64L) == 0L);
            if (var9 && Float.compare(AiPaceScript.putAt_F_F(var7, AiPaceScript.fill(var3, var4, var6, var7), var8), var7) != 0) {
               this.putAt_J = this.putAt_J | 1L << Math.min(var2 - 1, 63);
            }
         } else if (var1 == 2
            && var2 >= 4
            && (this.putAt_J & -8L) == 0L
            && Float.compare(AiPaceScript.putAt_F_F(var7, AiPaceScript.fill(var3, var4, var6, var7), var8), var7) != 0) {
            this.putAt_J = this.putAt_J | 1L << Math.min(var2 - 1, 63);
         }
      }
   }

   private static final class Inner_TableCache {
      final AiPhysics.MoveInput[] putAt_TokenCore_CacheHelArr = new AiPhysics.MoveInput[120];
      final AiPhysics.MotionState[] putAt_TokenCore_TableCacArr = new AiPhysics.MotionState[120];
      final double[] putAt_DArr = new double[120];
      final AiPaceScript.Inner_SyncPool putAt_TextPool_SyncPool = new AiPaceScript.Inner_SyncPool();
      final AiPaceScript.Inner_SyncPool fill_TextPool_SyncPool = new AiPaceScript.Inner_SyncPool();
      final AiPaceScript.Inner_SyncPool readEx_TextPool_SyncPool = new AiPaceScript.Inner_SyncPool();
      final AABB[] putAt_AABBArr = new AABB[32];
      boolean putAt_Z;
      boolean fill_Z;
      int flowState;
      int flowTemp;
      double putAt_D;
      double fill_D;
      double readEx_D;
   }

   private static final class Inner_TaskState {
      final AiPhysics.MotionState putAt_TokenCore_TableCac;
      final float putAt_F;
      final int flowState;
      final int flowTemp;
      final int readEx_I;
      final double putAt_D;
      final double fill_D;
      final double readEx_D;
      boolean putAt_Z = true;
      int value_I;
      boolean fill_Z;
      boolean readEx_Z = true;
      double value_D;
      double prevEx;
      long putAt_J;
      final AiPaceScript.Inner_TaskState putAt_TextPool_TaskState;
      final AiPhysics.MoveInput[] putAt_TokenCore_CacheHelArr;
      final AiPhysics.MotionState[] putAt_TokenCore_TableCacArr;
      final double[] putAt_DArr;

      int putAt() {
         return this.readEx_I + (int)Math.round(this.value_D);
      }

      Inner_TaskState(
         AiPhysics.MotionState var1,
         float var2,
         int var3,
         int var4,
         int var5,
         double var6,
         double var8,
         double var10,
         AiPaceScript.Inner_TaskState var12,
         AiPhysics.MoveInput[] var13,
         AiPhysics.MotionState[] var14,
         double[] var15
      ) {
         this.putAt_TokenCore_TableCac = var1;
         this.putAt_F = var2;
         this.flowState = var3;
         this.flowTemp = var4;
         this.readEx_I = var5;
         this.putAt_D = var6;
         this.fill_D = var8;
         this.readEx_D = var10;
         this.putAt_TextPool_TaskState = var12;
         this.putAt_TokenCore_CacheHelArr = var13;
         this.putAt_TokenCore_TableCacArr = var14;
         this.putAt_DArr = var15;
      }
   }

   public record Inner_TextState(
      int maxNodes,
      int maxTicks,
      float maxTurn,
      double weight,
      double turnCost,
      double horizon,
      double commit,
      boolean allowHops,
      boolean allowSprint,
      Predicate<BlockPos> allowedCell
   ) {
      public static final AiPaceScript.Inner_TextState DEFAULT = new AiPaceScript.Inner_TextState(60000, 36000, 30.0F, 1.0, 0.01, 22.0, 10.0, true, true, null);
      public static final AiPaceScript.Inner_TextState TRAVEL = new AiPaceScript.Inner_TextState(10000, 12000, 45.0F, 1.35, 0.005, 10.0, 5.0, true, true, null);

      public Inner_TextState(int var1, int var2, float var3, double var4, double var6, double var8, double var10) {
         this(var1, var2, var3, var4, var6, var8, var10, true, true, null);
      }

      public Inner_TextState(int var1, int var2, float var3, double var4, double var6, double var8, double var10, boolean var12, boolean var13) {
         this(var1, var2, var3, var4, var6, var8, var10, var12, var13, null);
      }

      public AiPaceScript.Inner_TextState withControls(boolean var1, boolean var2) {
         return new AiPaceScript.Inner_TextState(
            this.maxNodes, this.maxTicks, this.maxTurn, this.weight, this.turnCost, this.horizon, this.commit, var1, var2, this.allowedCell
         );
      }

      public AiPaceScript.Inner_TextState withAllowedCells(Predicate<BlockPos> var1) {
         return new AiPaceScript.Inner_TextState(
            this.maxNodes, this.maxTicks, this.maxTurn, this.weight, this.turnCost, this.horizon, this.commit, this.allowHops, this.allowSprint, var1
         );
      }
   }

   private record Inner_TextStore(List<AiPhysics.MoveInput> keys, BlockPos target) {
   }

   public record Inner_TokenCodec(AiPaceScript.Inner_JsonCache guide, List<AiPathfinder.PathStep> movements) {
   }
}
