package autismclient.ai.a;

import autismclient.AutismClientAddon;
import autismclient.ai.af;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SulfurSpikeBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TwistingVinesBlock;
import net.minecraft.world.level.block.TwistingVinesPlantBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.WeepingVinesBlock;
import net.minecraft.world.level.block.WeepingVinesPlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClassCache {
   private static final ConcurrentHashMap<ClassCache.Inner_ConfigStore, Long> putAt = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<ClassCache.Inner_ConfigStore, long[]> fill = new ConcurrentHashMap<>();

   private ClassCache() {
   }

   private static int flowState(ClassCache.UiHelper var0) {
      if (var0 == null) {
         return 4;
      }

      int var1 = Integer.compare(var0.to().getX(), var0.from().getX());
      int var2 = Integer.compare(var0.to().getZ(), var0.from().getZ());
      return 3 * (var1 + 1) + var2 + 1;
   }

   private static double putAt_D_FileStore_PacketHe_FileStore_PacketHe(ClassCache.UiHelper var0, ClassCache.UiHelper var1) {
      if (var0 == null) {
         return 0.0;
      }

      double var2 = var0.to().getX() - var0.from().getX();
      double var4 = var0.to().getZ() - var0.from().getZ();
      double var6 = var1.to().getX() - var1.from().getX();
      double var8 = var1.to().getZ() - var1.from().getZ();
      double var10 = Math.hypot(var2, var4) * Math.hypot(var6, var8);
      if (var10 == 0.0) {
         return var1.jump() == null ? 0.0 : 2.0;
      }

      double var12 = 1.0 - Math.clamp((var2 * var6 + var4 * var8) / var10, -1.0, 1.0);
      return var12 * (var1.jump() != null ? 6 : (var0.jump() != null ? 4 : 2));
   }

   private static double putAt(ClassCache.Inner_PacketUtil var0) {
      double var1 = 0.0;
      ClassCache.UiHelper var3 = null;

      for (ClassCache.UiHelper var5 : var0.movements()) {
         var1 += putAt_D(var5) + putAt_D_FileStore_PacketHe_FileStore_PacketHe(var3, var5);
         var3 = var5;
      }

      return var1;
   }

   private static double putAt_D(ClassCache.UiHelper var0) {
      TableIndex.Inner_StateCache var1 = var0.jump();
      return var0.type() == ClassCache.Inner_SyncManager.start && var1 != null && var1.maneuver() != null
         ? Math.max(var1.ticks() + 4, var0.costTicks() - 12.0)
         : var0.costTicks();
   }

   private static ClassCache.Inner_PacketUtil putAt(
      ClassCache.Inner_PacketUtil var0,
      ClassCache.Inner_StateCache var1,
      ClassCache.Inner_DataUtil var2,
      double var3,
      Set<Long> var5,
      Set<ClassCache.Inner_ConfigStore> var6
   ) {
      var var7 = var0.movements();
      if (var7.size() < 2) {
         return var0;
      }

      ArrayList<List<ClassCache.Inner_TextState>> var8 = new ArrayList<>(Collections.nCopies(var7.size(), List.of()));
      ClassCache.Inner_RenderState.Inner_CachePool var9 = new ClassCache.Inner_RenderState.Inner_CachePool(var1, 50000, var3);
      var9.value_Z = var9.prevEx_Z = true;
      var9.prev_Z = true;
      var9.putAt_FileStore_RingUtil = var2.fallPolicy();

      for (int var10 = var7.size() - 1; var10 >= 0; var10--) {
         if (Thread.currentThread().isInterrupted()) {
            return var0;
         }

         ClassCache.UiHelper var11 = (ClassCache.UiHelper)var7.get(var10);
         ArrayList var12 = new ArrayList();
         var12.add(putAt(var11, var10 + 1, var8, 0.0));
         BlockPos var13 = var11.from();
         ClassCache.Inner_FileCache var14 = var9.putAt_FileStore_BitCache_BlockPos(var13.below());
         double var15 = ClassCache.Inner_RenderState.putAt(var13.getY(), var14);
         if (!var14.farmland()
            && !var14.slime()
            && ClassCache.Inner_RenderState.putAt_Z_FileStore_BitCache(
               var9.putAt_FileStore_BitCache_BlockPos(var13), var9.putAt_FileStore_BitCache_BlockPos(var13.above()), var14
            )
            && TableIndex.putAt_Z_TokenCore_TaskStat(var9.putAt_RefQueue(), var13.getX() + 0.5, var15, var13.getZ() + 0.5)) {
            var9.putAt_FileStore_PacketHe = var10 == 0 ? null : (ClassCache.UiHelper)var7.get(var10 - 1);

            for (int var17 = var10 + 1; var17 < Math.min(var10 + 16, var7.size()); var17++) {
               BlockPos var18 = ((ClassCache.UiHelper)var7.get(var17)).to();
               double var19 = Math.hypot(var18.getX() - var13.getX(), var18.getZ() - var13.getZ());
               ClassCache.Inner_FileCache var21 = var9.putAt_FileStore_BitCache_BlockPos(var18.below());
               double var22 = ClassCache.Inner_RenderState.putAt(var18.getY(), var21);
               double var24 = var15 - var22;
               if (!(var19 < 1.6)
                  && !(var19 > (var24 > 14.0 ? 10.5 : 8.5))
                  && !(-var24 > 1.25)
                  && !(var24 > var2.fallPolicy().putAt_D)
                  && !var5.contains(var18.asLong())
                  && !var21.slime()
                  && ClassCache.Inner_RenderState.putAt_Z_FileStore_BitCache(
                     var9.putAt_FileStore_BitCache_BlockPos(var18), var9.putAt_FileStore_BitCache_BlockPos(var18.above()), var21
                  )) {
                  TableIndex.Inner_StateCache var26 = var9.putAt(var13, var15, var18, var22, 0.0, var2.canSprint());
                  if (var26 != null && !(var26.margin() < 0.08) && !ClassCache.Inner_RenderState.readEx(var9, var13, var18)) {
                     ClassCache.UiHelper var27 = ClassCache.UiHelper.jump(
                        var13, var18, ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var26), var26
                     );
                     if (!var6.contains(ClassCache.Inner_ConfigStore.of(var27))) {
                        var12.add(putAt(var27, var17 + 1, var8, 1.5));
                     }
                  }
               }
            }
         }

         var8.set(var10, var12);
      }

      ClassCache.Inner_TextState var28 = ((Inner_TextState)(((List)var8.getFirst()).stream().min(Comparator.comparingDouble(ClassCache.Inner_TextState::ticks)).orElseThrow()));
      ArrayList var29 = new ArrayList();

      for (ClassCache.Inner_TextState var30 = var28; var30 != null; var30 = var30.next()) {
         var29.add(var30.first());
      }

      return new ClassCache.Inner_PacketUtil(var29, true, var0.expanded(), false, false);
   }

   private static ClassCache.Inner_TextState putAt(ClassCache.UiHelper var0, int var1, List<List<ClassCache.Inner_TextState>> var2, double var3) {
      ClassCache.Inner_TextState var5 = null;
      double var6 = 1.0 / 0.0;
      if (var1 < var2.size()) {
         for (ClassCache.Inner_TextState var9 : (Iterable<ClassCache.Inner_TextState>)(Iterable<?>)var2.get(var1)) {
            double var10 = var9.ticks() + putAt_D_FileStore_PacketHe_FileStore_PacketHe(var0, var9.first());
            if (var10 < var6) {
               var6 = var10;
               var5 = var9;
            }
         }
      }

      return new ClassCache.Inner_TextState(var0, var5, putAt_D(var0) + var3 + (var5 == null ? 0.0 : var6));
   }

   private static ClassCache.Inner_PacketUtil putAt(
      ClassCache.Inner_PacketUtil var0, ClassCache.Inner_StateCache var1, ClassCache.Inner_DataUtil var2, double var3
   ) {
      var var5 = new ArrayList<>(var0.movements());
      ClassCache.Inner_RenderState.Inner_CachePool var6 = new ClassCache.Inner_RenderState.Inner_CachePool(var1, 50000, var3);
      var6.value_Z = true;
      var6.putAt_FileStore_RingUtil = var2.fallPolicy();
      StreamGate var7 = var6.putAt_RefQueue();

      for (int var8 = 0; var8 + 1 < var5.size() && !Thread.currentThread().isInterrupted(); var8++) {
         ClassCache.UiHelper var9 = (ClassCache.UiHelper)var5.get(var8);
         ArrayList<TableIndex.Inner_HashPool> var10 = new ArrayList<>(4);
         int var11 = 0;

         for (int var12 = var8; var12 < Math.min(var8 + 4, var5.size()); var12++) {
            ClassCache.UiHelper var13 = (ClassCache.UiHelper)var5.get(var12);
            if (var13.type() != ClassCache.Inner_SyncManager.start
               || var13.jump() == null
               || !var13.jump().sprint()
               || var6.putAt_FileStore_BitCache_BlockPos(var13.from().below()).ice()
               || var6.putAt_FileStore_BitCache_BlockPos(var13.to().below()).ice()) {
               break;
            }

            ClassCache.Inner_FileCache var14 = var6.putAt_FileStore_BitCache_BlockPos(var13.from().below());
            double var15 = var13.jump().landY() - ClassCache.Inner_RenderState.putAt(var13.from().getY(), var14);
            if (var15 > 1.29 || var15 < -2.25) {
               break;
            }

            var10.add(new TableIndex.Inner_HashPool(var13.to(), var13.jump().landY()));
            var11 += var13.jump().ticks();
         }

         if (var10.size() >= 2) {
            TableIndex.Inner_StateCache var20 = var9.jump();
            double var21 = ClassCache.Inner_RenderState.putAt(var9.from().getY(), var6.putAt_FileStore_BitCache_BlockPos(var9.from().below()));
            double var22 = Math.toRadians(var20.yaw());
            NodeWalker.Inner_StreamUtil var17 = var20.maneuver() == null
               ? new NodeWalker.Inner_StreamUtil(
                  var20.startX(),
                  var21,
                  var20.startZ(),
                  -Math.sin(var22) * var20.approach(),
                  -0.0784,
                  Math.cos(var22) * var20.approach(),
                  true,
                  0,
                  false,
                  false
               )
               : var20.maneuver().start();
            var var18 = TableIndex.putAt_List_TokenCore_TaskStat(var7, var17, var20.yaw(), var10);
            if (var18.size() == var10.size() && var18.stream().mapToInt(TableIndex.Inner_StateCache::ticks).sum() < var11 - 1) {
               for (int var19 = 0; var19 < var18.size(); var19++) {
                  var5.set(var8 + var19, ((ClassCache.UiHelper)var5.get(var8 + var19)).withPaceJump((TableIndex.Inner_StateCache)var18.get(var19)));
               }

               var8 += var18.size() - 1;
            }
         }
      }

      return new ClassCache.Inner_PacketUtil(var5, true, var0.expanded(), false, false);
   }

   public static List<ClassCache.UiHelper> putAt_List_List_List(List<ClassCache.UiHelper> var0, List<ClassCache.UiHelper> var1) {
      if (!var0.isEmpty() && !var1.isEmpty()) {
         if (!((ClassCache.UiHelper)var1.get(0)).from().equals(((ClassCache.UiHelper)var0.get(var0.size() - 1)).to())) {
            return null;
         }

         ArrayList<ClassCache.UiHelper> var2 = new ArrayList<>(var0.size() + var1.size());
         var2.addAll(var0);
         var2.addAll(var1);
         return var2;
      } else {
         return null;
      }
   }

   static List<ClassCache.UiHelper> fill(List<ClassCache.UiHelper> var0, List<ClassCache.UiHelper> var1) {
      if (!var0.isEmpty() && !var1.isEmpty()) {
         HashMap var2 = new HashMap();

         for (int var3 = 0; var3 < var1.size(); var3++) {
            var2.putIfAbsent(((ClassCache.UiHelper)var1.get(var3)).from(), var3);
         }

         for (int var6 = 0; var6 < var0.size(); var6++) {
            Integer var4 = (Integer)var2.get(((ClassCache.UiHelper)var0.get(var6)).to());
            if (var4 != null) {
               ArrayList var5 = new ArrayList(var6 + 1 + var1.size() - var4);
               var5.addAll(var0.subList(0, var6 + 1));
               var5.addAll(var1.subList(var4, var1.size()));
               return List.copyOf(var5);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static ClassCache.Inner_HashUtil putAt_FileStore_HashEntr_I_I(int var0, int var1) {
      if (var1 >= 6) {
         return ClassCache.Inner_HashUtil.read;
      } else {
         return var0 >= 3 ? ClassCache.Inner_HashUtil.load : ClassCache.Inner_HashUtil.init;
      }
   }

   public static void putAt_V_FileStore_PacketHe_J(ClassCache.UiHelper var0, long var1) {
      if (var0 != null && var0.type() == ClassCache.Inner_SyncManager.start) {
         fill(var0, var1);
      }
   }

   public static void fill(ClassCache.UiHelper var0, long var1) {
      if (var0 != null) {
         long var3 = System.nanoTime();
         putAt.values().removeIf(var2 -> var2 - var3 < 0L);
         putAt.put(new ClassCache.Inner_ConfigStore(var0.type(), var0.from().asLong(), var0.to().asLong()), var3 + var1);
      }
   }

   public static void putAt_V(ClassCache.UiHelper var0) {
      if (var0 != null) {
         long var1 = System.nanoTime();
         fill.values().removeIf(var2 -> var1 - var2[1] > 120000000000L);
         ClassCache.Inner_ConfigStore var3 = new ClassCache.Inner_ConfigStore(var0.type(), var0.from().asLong(), var0.to().asLong());
         long[] var4 = fill.computeIfAbsent(var3, var2 -> new long[]{0L, var1});
         var4[0]++;
         var4[1] = var1;
         if (var4[0] >= 2L) {
            fill(var0, 120000000000L);
         }
      }
   }

   static boolean putAt_Z(ClassCache.UiHelper var0) {
      if (putAt.isEmpty()) {
         return false;
      }

      Long var1 = putAt.get(new ClassCache.Inner_ConfigStore(var0.type(), var0.from().asLong(), var0.to().asLong()));
      return var1 != null && var1 - System.nanoTime() > 0L;
   }

   public static Set<ClassCache.Inner_SyncManager> putAt(ClassCache.Inner_SyncManager var0) {
      Set var10000;
      switch (var0) {
         case init:
         case read:
            var10000 = Set.of(ClassCache.Inner_SyncManager.reset, ClassCache.Inner_SyncManager.read);
            break;
         case load:
         case write:
            var10000 = Set.of(ClassCache.Inner_SyncManager.write);
            break;
         case update:
         case flush:
         case close:
         case open:
         case poll:
         case push:
         case pop:
         case tick:
         case run:
         case get:
         case set:
         case put:
         case add:
         case clear:
         case reset:
         case SLIME_BOUNCE:
         case LADDER_JUMP:
         default:
            var10000 = Set.of(var0);
            break;
         case sync:
            var10000 = Set.of(ClassCache.Inner_SyncManager.sync, ClassCache.Inner_SyncManager.stop);
            break;
         case start:
            var10000 = Set.of();
            break;
         case stop:
            var10000 = Set.of(ClassCache.Inner_SyncManager.stop, ClassCache.Inner_SyncManager.sync);
      }

      return var10000;
   }

   static ClassCache.Inner_StateCache putAt_FileStore_TextStor_FileStore_TextStor_List(
      final ClassCache.Inner_StateCache var0, List<ClassCache.UiHelper> var1
   ) {
      HashMap var2 = new HashMap();

      for (ClassCache.UiHelper var4 : var1) {
         for (BlockPos var6 : var4.cellsToBreak()) {
            var2.put(var6.asLong(), ClassCache.Inner_FileCache.air());
         }

         for (BlockPos var9 : var4.cellsToPlace()) {
            var2.put(var9.asLong(), ClassCache.Inner_FileCache.fullBlock(1.0 / 0.0));
         }
      }

      if (var2.isEmpty()) {
         return var0;
      }

      final var var7 = Map.copyOf(var2);
      return new ClassCache.Inner_StateCache() {
         @Override
         public ClassCache.Inner_FileCache probe(int var1, int var2x, int var3) {
            ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.probe(var1, var2x, var3) : var4;
         }

         @Override
         public short putAt_S_I(int var1, int var2x, int var3) {
            ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.putAt_S_I(var1, var2x, var3) : e.init_S_ClassCache_FileCac(var4);
         }

         @Override
         public short fill_S(int var1, int var2x, int var3) {
            ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var7.get(BlockPos.asLong(var1, var2x, var3));
            return var4 == null ? var0.fill_S(var1, var2x, var3) : e.init_S_ClassCache_FileCac(var4);
         }

         @Override
         public byte putAt_B_I(int var1, int var2x, int var3) {
            return var7.containsKey(BlockPos.asLong(var1, var2x, var3)) ? m.init_m.init() : var0.putAt_B_I(var1, var2x, var3);
         }

         @Override
         public long putAt_J() {
            return var0.putAt_J();
         }
      };
   }

   public record Inner_FileCache(
      boolean loaded,
      boolean passable,
      int top16,
      boolean water,
      boolean lava,
      boolean climbable,
      boolean hazardFeet,
      boolean hazardFloor,
      boolean falling,
      int fallMult10,
      double breakTicks,
      boolean door,
      boolean slabStair,
      boolean farmland,
      boolean ice,
      boolean cached,
      int destructionPenalty,
      boolean soulSand,
      boolean thorny,
      boolean portal,
      boolean narrowFooting,
      boolean feetTrigger,
      boolean pressurePlate
   ) {
      public static final double NOT_DIGGABLE = 1.0 / 0.0;
      public static final ClassCache.Inner_FileCache UNLOADED = new ClassCache.Inner_FileCache(
         false, false, 0, false, false, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      private static final ClassCache.Inner_FileCache AIR = new ClassCache.Inner_FileCache(
         true, true, 0, false, false, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      private static final ClassCache.Inner_FileCache WATER = new ClassCache.Inner_FileCache(
         true, true, 0, true, false, false, false, false, false, 0, 1.0 / 0.0, false, false, false, false, false
      );
      private static final ClassCache.Inner_FileCache LAVA = new ClassCache.Inner_FileCache(
         true, true, 0, false, true, false, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false
      );
      public static final ClassCache.Inner_FileCache CACHED_AIR = air().withCached();
      public static final ClassCache.Inner_FileCache CACHED_SOLID = fullBlock(1.0 / 0.0).withCached();
      public static final ClassCache.Inner_FileCache CACHED_WATER = waterCell().withCached();
      public static final ClassCache.Inner_FileCache CACHED_HAZARD = lavaCell().withCached();

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21,
         boolean var22,
         boolean var23
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            var22,
            var23,
            false
         );
      }

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21,
         boolean var22
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            var22,
            false,
            false
         );
      }

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20,
         boolean var21
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var13,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            var20,
            var21,
            false,
            false,
            false
         );
      }

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19,
         boolean var20
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, var18, var19, var20, false);
      }

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17,
         int var18,
         boolean var19
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, var18, var19, false);
      }

      public Inner_FileCache(
         boolean var1,
         boolean var2,
         int var3,
         boolean var4,
         boolean var5,
         boolean var6,
         boolean var7,
         boolean var8,
         boolean var9,
         int var10,
         double var11,
         boolean var13,
         boolean var14,
         boolean var15,
         boolean var16,
         boolean var17
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var13, var14, var15, var16, var17, 0, false);
      }

      boolean floorContactHazard() {
         return this.hazardFloor && !this.narrowFooting;
      }

      public ClassCache.Inner_FileCache withDestructionPenalty(int var1) {
         if (var1 < 0) {
            throw new IllegalArgumentException("Negative destruction penalty");
         } else {
            return new ClassCache.Inner_FileCache(
               this.loaded,
               this.passable,
               this.top16,
               this.water,
               this.lava,
               this.climbable,
               this.hazardFeet,
               this.hazardFloor,
               this.falling,
               this.fallMult10,
               this.breakTicks,
               this.door,
               this.slabStair,
               this.farmland,
               this.ice,
               this.cached,
               var1,
               this.soulSand,
               this.thorny,
               this.portal,
               this.narrowFooting,
               this.feetTrigger,
               this.pressurePlate
            );
         }
      }

      public static ClassCache.Inner_FileCache air() {
         return AIR;
      }

      public static ClassCache.Inner_FileCache waterCell() {
         return WATER;
      }

      public static ClassCache.Inner_FileCache lavaCell() {
         return LAVA;
      }

      public static ClassCache.Inner_FileCache climbableCell() {
         return new ClassCache.Inner_FileCache(true, true, 0, false, false, true, false, false, false, 10, 1.0 / 0.0, false, false, false, false, false);
      }

      public static ClassCache.Inner_FileCache fullBlock(double var0) {
         return new ClassCache.Inner_FileCache(true, false, 16, false, false, false, false, false, false, 10, var0, false, false, false, false, false);
      }

      public static ClassCache.Inner_FileCache doorCell(double var0) {
         return new ClassCache.Inner_FileCache(true, false, 16, false, false, false, false, false, false, 10, var0, true, false, false, false, false);
      }

      public ClassCache.Inner_FileCache withTop16(int var1) {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            var1,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withFallMult10(int var1) {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            var1,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withHazards(boolean var1, boolean var2) {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            var1,
            var2,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            var2 && this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withFalling() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            true,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withSlabStair() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            true,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withFarmland() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            true,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withIce() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            true,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withSoulSand() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            true,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withThorny() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            true,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withPortal() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            this.cached,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            true,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      public ClassCache.Inner_FileCache withCached() {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            this.hazardFeet,
            this.hazardFloor,
            this.falling,
            this.fallMult10,
            this.breakTicks,
            this.door,
            this.slabStair,
            this.farmland,
            this.ice,
            true,
            this.destructionPenalty,
            this.soulSand,
            this.thorny,
            this.portal,
            this.narrowFooting,
            this.feetTrigger,
            this.pressurePlate
         );
      }

      boolean diggable() {
         return !this.passable && this.breakTicks < 1.0 / 0.0 && !this.lava && !this.water;
      }

      public boolean web() {
         return this.loaded && this.passable && this.fallMult10 == 0 && !this.water && !this.lava && !this.climbable && !this.hazardFeet;
      }

      public boolean honey() {
         return this.loaded && !this.passable && this.top16 == 15 && this.fallMult10 == 2;
      }

      public boolean slime() {
         return this.loaded && !this.passable && this.top16 == 16 && this.fallMult10 == 0;
      }

      public double crawl() {
         return !this.soulSand && !this.honey() ? (this.slime() ? 3.6 : 1.0) : 2.5;
      }

      public boolean contactHazard() {
         return this.loaded && (this.hazardFeet || this.lava || this.thorny || this.portal || !this.passable && this.hazardFloor && this.top16 == 15);
      }
   }

   record Inner_ConfigStore(ClassCache.Inner_SyncManager type, long from, long to) {
      static ClassCache.Inner_ConfigStore of(ClassCache.UiHelper var0) {
         return new ClassCache.Inner_ConfigStore(var0.type(), var0.from().asLong(), var0.to().asLong());
      }
   }

   public record Inner_DataUtil(
      boolean allowDig,
      boolean allowPillar,
      boolean allowBridge,
      boolean canSprint,
      boolean hasBlocks,
      boolean allowParkour,
      ClassCache.Inner_IoWorker strategy,
      ClassCache.Inner_PoolEntry threats,
      boolean jumpBoost,
      boolean slowFalling,
      boolean levitation,
      double breakReluctance,
      Set<ClassCache.Inner_SyncManager> disabledTypes,
      int constructionBlocks,
      int maxFall,
      boolean avoidWater,
      boolean repairFarmland,
      boolean supportedBridges,
      ClassCache.Inner_NetHelper fallPolicy
   ) {
      public static final ClassCache.Inner_DataUtil WALK_ONLY = new ClassCache.Inner_DataUtil(false, false, false, true, false);
      public static final ClassCache.Inner_DataUtil FULL = new ClassCache.Inner_DataUtil(true, true, true, true, true);

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<ClassCache.Inner_SyncManager> var14,
         int var15,
         int var16,
         boolean var17,
         boolean var18,
         boolean var19
      ) {
         this(
            var1,
            var2,
            var3,
            var4,
            var5,
            var6,
            var7,
            var8,
            var9,
            var10,
            var11,
            var12,
            var14,
            var15,
            var16,
            var17,
            var18,
            var19,
            ClassCache.Inner_NetHelper.init
         );
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<ClassCache.Inner_SyncManager> var14,
         int var15,
         int var16,
         boolean var17,
         boolean var18
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, var16, var17, var18, false);
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<ClassCache.Inner_SyncManager> var14,
         int var15,
         int var16,
         boolean var17
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, var16, var17, false);
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<ClassCache.Inner_SyncManager> var14,
         int var15
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var15, 3, false);
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12,
         Set<ClassCache.Inner_SyncManager> var14
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, var14, var5 ? 2147483647 : 0);
      }

      public Inner_DataUtil(
         boolean allowDig,
         boolean allowPillar,
         boolean allowBridge,
         boolean canSprint,
         boolean hasBlocks,
         boolean allowParkour,
         ClassCache.Inner_IoWorker strategy,
         ClassCache.Inner_PoolEntry threats,
         boolean jumpBoost,
         boolean slowFalling,
         boolean levitation,
         double breakReluctance,
         Set<ClassCache.Inner_SyncManager> disabledTypes,
         int constructionBlocks,
         int maxFall,
         boolean avoidWater,
         boolean repairFarmland,
         boolean supportedBridges,
         ClassCache.Inner_NetHelper fallPolicy
      ) {
         if (constructionBlocks < 0) {
            throw new IllegalArgumentException("Negative construction inventory");
         }

         disabledTypes = Set.copyOf(disabledTypes);
         this.allowDig = allowDig;
         this.allowPillar = allowPillar;
         this.allowBridge = allowBridge;
         this.canSprint = canSprint;
         this.hasBlocks = hasBlocks;
         this.allowParkour = allowParkour;
         this.strategy = strategy;
         this.threats = threats;
         this.jumpBoost = jumpBoost;
         this.slowFalling = slowFalling;
         this.levitation = levitation;
         this.breakReluctance = breakReluctance;
         this.disabledTypes = disabledTypes;
         this.constructionBlocks = constructionBlocks;
         this.maxFall = maxFall;
         this.avoidWater = avoidWater;
         this.repairFarmland = repairFarmland;
         this.supportedBridges = supportedBridges;
         this.fallPolicy = fallPolicy;
      }

      public Inner_DataUtil(boolean var1, boolean var2, boolean var3, boolean var4, boolean var5) {
         this(var1, var2, var3, var4, var5, false, ClassCache.Inner_IoWorker.init, ClassCache.Inner_PoolEntry.value);
      }

      public Inner_DataUtil(
         boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, ClassCache.Inner_IoWorker var6, ClassCache.Inner_PoolEntry var7
      ) {
         this(var1, var2, var3, var4, var5, false, var6, var7);
      }

      public Inner_DataUtil(
         boolean var1, boolean var2, boolean var3, boolean var4, boolean var5, boolean var6, ClassCache.Inner_IoWorker var7, ClassCache.Inner_PoolEntry var8
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, false, false, false);
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, 2.0);
      }

      public Inner_DataUtil(
         boolean var1,
         boolean var2,
         boolean var3,
         boolean var4,
         boolean var5,
         boolean var6,
         ClassCache.Inner_IoWorker var7,
         ClassCache.Inner_PoolEntry var8,
         boolean var9,
         boolean var10,
         boolean var11,
         double var12
      ) {
         this(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11, var12, Set.of());
      }

      ClassCache.Inner_DataUtil withFallPolicy(ClassCache.Inner_NetHelper var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            var1
         );
      }

      public ClassCache.Inner_DataUtil withSupportedBridges(boolean var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            var1,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withFarmlandRepair(boolean var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            var1,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withConstructionBlocks(int var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            var1 > 0,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            var1,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withMovementLimits(int var1, boolean var2) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            Math.max(0, Math.min(3, var1)),
            var2,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      ClassCache.Inner_DataUtil withoutConstruction() {
         return !this.allowDig && !this.allowPillar && !this.allowBridge && !this.hasBlocks && this.constructionBlocks == 0 && !this.supportedBridges
            ? this
            : new ClassCache.Inner_DataUtil(
               false,
               false,
               false,
               this.canSprint,
               false,
               this.allowParkour,
               this.strategy,
               this.threats,
               this.jumpBoost,
               this.slowFalling,
               this.levitation,
               this.breakReluctance,
               this.disabledTypes,
               0,
               this.maxFall,
               this.avoidWater,
               this.repairFarmland,
               false,
               this.fallPolicy
            );
      }

      public ClassCache.Inner_DataUtil withBreakReluctance(double var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            var1,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withDig() {
         return new ClassCache.Inner_DataUtil(
            true,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withDisabledTypes(Set<ClassCache.Inner_SyncManager> var1) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            this.hasBlocks,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            var1,
            this.constructionBlocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }
   }

   public record Inner_EventBus(int expanded, List<ClassCache.UiHelper> trail) {
   }

   public enum Inner_HashUtil {
      init,
      load,
      read;
   }

   private record Inner_HashPool(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
      static ClassCache.Inner_HashPool around(ClassCache.Inner_PacketUtil var0) {
         BlockPos var1 = var0.movements().getFirst().from();
         int var2 = var1.getX();
         int var3 = var2;
         int var4 = var1.getY();
         int var5 = var4;
         int var6 = var1.getZ();
         int var7 = var6;

         for (ClassCache.UiHelper var9 : var0.movements()) {
            BlockPos var10 = var9.to();
            var2 = Math.min(var2, var10.getX());
            var3 = Math.max(var3, var10.getX());
            var4 = Math.min(var4, var10.getY());
            var5 = Math.max(var5, var10.getY());
            var6 = Math.min(var6, var10.getZ());
            var7 = Math.max(var7, var10.getZ());
         }

         return new ClassCache.Inner_HashPool(var2 - 7, var3 + 7, var4 - 5, var5 + 5, var6 - 7, var7 + 7);
      }

      boolean contains(int var1, int var2, int var3) {
         return var1 >= this.minX && var1 <= this.maxX && var2 >= this.minY && var2 <= this.maxY && var3 >= this.minZ && var3 <= this.maxZ;
      }
   }

   public enum Inner_IoWorker {
      init,
      load,
      read;

      public static ClassCache.Inner_IoWorker init(String var0) {
         return putAt(var0);
      }

      public static ClassCache.Inner_IoWorker putAt(String var0) {
         try {
            return valueOf(var0.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException | NullPointerException var2) {
            throw new IllegalArgumentException("strategy must be fastest, safest or saver");
         }
      }
   }

   public record Inner_IoCodec(long occupied, boolean pending) {
   }

   public static final class Inner_RenderState {
      private static final int[][] putAt_IArr2 = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
      private static final int[][] fill_IArr2 = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
      static final int flowState = 1;
      static final int flowTemp = 2;
      static final int readEx_I = 4;
      static final int value_I = 5;
      private static final int[][] readEx_IArr2 = new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] putAt_FileStore_IoHelperArr = putAt(5);
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] fill_FileStore_IoHelperArr = putAt(7);
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] readEx_FileStore_IoHelperArr = putAt(11);
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] value_FileStore_IoHelperArr = putAt(8);
      private static final int state = 14;
      static final double putAt_D = 0.1;
      static final double fill_D = 0.1;
      static final int prevEx = 3;
      static final int prev = 4;
      static final int chunkAt = 32;
      static boolean putAt_Z = true;

      private Inner_RenderState() {
      }

      public static boolean init_Z_ClassCache_FileCac(ClassCache.Inner_FileCache a, ClassCache.Inner_FileCache b, ClassCache.Inner_FileCache c) {
         return putAt_Z_FileStore_BitCache(a, b, c);
      }

            public static boolean putAt_Z_FileStore_BitCache(ClassCache.Inner_FileCache var0, ClassCache.Inner_FileCache var1, ClassCache.Inner_FileCache var2) {
         if (!var0.loaded() || !var1.loaded() || !var2.loaded()) {
            return false;
         }

         if (var0.hazardFeet() || var0.feetTrigger() || var1.hazardFeet() || var2.hazardFloor() || var0.lava() || var1.lava() || var2.lava()) {
            return false;
         }

         if (var0.water() && var1.water()) {
            return false;
         }

         if (!var2.passable() && var2.top16() > 16) {
            return false;
         }

         boolean var3 = var1.passable() || !var2.passable() && var2.top16() > 0 && var2.top16() <= 3;
         return var0.passable() && var3 && (!var2.passable() && !var2.door() || var0.water() || var0.climbable());
      }

      static boolean putAt_Z_Function_2(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, TableIndex.Inner_StateCache var2) {
         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (var3.loaded()
            && !var3.passable()
            && var3.top16() <= 24
            && !var3.floorContactHazard()
            && !var3.farmland()
            && !var3.lava()
            && !var3.water()
            && !var3.door()) {
            boolean var4 = var3.narrowFooting();
            int var5 = var3.top16() > 16 ? 2 : 1;

            for (int var6 = 0; var6 <= var5; var6++) {
               BlockPos var7 = var1.above(var6);
               ClassCache.Inner_FileCache var8 = (ClassCache.Inner_FileCache)var0.apply(var7);
               if (!var8.loaded()
                  || var8.hazardFeet()
                  || var6 == 0 && var8.feetTrigger()
                  || var8.lava()
                  || var8.water()
                  || var8.climbable()
                  || var8.web()
                  || var8.portal()) {
                  return false;
               }

               if (!var8.passable()) {
                  if (var0 instanceof ClassCache.Inner_RenderState.Inner_CachePool var9
                     && (!var9.prevEx_Z || var9.putAt_S_I(var7.getX(), var7.getY(), var7.getZ()) == 1)) {
                     return false;
                  }

                  var4 = true;
               }
            }

            if (!var4) {
               return var3.top16() > 16;
            } else if (var2 != null) {
               return true;
            } else if (var0 instanceof ClassCache.Inner_RenderState.Inner_CachePool var10) {
               double var11 = putAt_D_FileStore_IoHelper(var10, var1, var3);
               return TableIndex.putAt_Z_TokenCore_TaskStat_2(var10.putAt_RefQueue(), var1, var11);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static double putAt_D_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, ClassCache.Inner_FileCache var2) {
         double var3 = putAt(var1.getY(), var2);
         return var0.prevEx_Z
               && var2.slabStair()
               && var2.top16() == 16
               && !var0.putAt_FileStore_BitCache_BlockPos(var1.above()).passable()
               && !TableIndex.putAt_Z_TokenCore_TaskStat_2(var0.putAt_RefQueue(), var1, var3)
               && TableIndex.putAt_Z_TokenCore_TaskStat_2(var0.putAt_RefQueue(), var1, var3 - 0.5)
            ? var3 - 0.5
            : var3;
      }

      private static boolean putAt_Z_FileStore_IoHelper_3(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, ClassCache.Inner_FileCache var2) {
         if (var2.loaded() && var2.top16() > 0 && var2.top16() <= 8 && !var2.hazardFloor() && !var2.lava() && !var2.water() && !var2.farmland()) {
            ClassCache.Inner_FileCache var3 = var0.putAt_FileStore_BitCache_BlockPos(var1.above());
            if (var3.loaded() && !var3.passable() && !var3.hazardFeet()) {
               double var4 = putAt(var1.getY(), var2);
               double var6 = var1.getX() + 0.5;
               double var8 = var1.getZ() + 0.5;
               double var10 = 0.3;
               AABB var12 = new AABB(var6 - var10, var4 + 1.0E-5, var8 - var10, var6 + var10, var4 + 1.5 - 1.0E-5, var8 + var10);
               return var0.putAt_RefQueue().collisions(var12).isEmpty() && !TableIndex.putAt_Z_TokenCore_TaskStat(var0.putAt_RefQueue(), var6, var4, var8);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static void putAt(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         ClassCache.Inner_DataUtil var1,
         Set<Long> var2,
         BlockPos var3,
         double var4,
         boolean var6,
         List<ClassCache.UiHelper> var7
      ) {
         int var8 = var6 ? 2 : 1;

         for (int var9 = -var8; var9 <= var8; var9++) {
            for (int var10 = -var8; var10 <= var8; var10++) {
               if (var9 != 0 || var10 != 0) {
                  for (int var11 = 1; var11 >= -1; var11--) {
                     BlockPos var12 = var3.offset(var9, var11, var10);
                     if (!var2.contains(var12.asLong())) {
                        ClassCache.Inner_FileCache var13 = var0.putAt_FileStore_BitCache_BlockPos(var12.below());
                        if (var13.loaded() && !var13.passable() && !var13.hazardFloor() && !var13.water() && !var13.lava()) {
                           double var14 = putAt(var12.getY(), var13);
                           if (!(var14 > var4 + 0.5625) && !(var14 < var4 - 1.25)) {
                              boolean var16 = putAt_Z_FileStore_IoHelper_3(var0, var12, var13);
                              if (var16
                                 || var6
                                    && putAt_Z_FileStore_BitCache(
                                       var0.putAt_FileStore_BitCache_BlockPos(var12), var0.putAt_FileStore_BitCache_BlockPos(var12.above()), var13
                                    )) {
                                 TableIndex.Inner_StateCache var17 = PoolEntry.lowClearance(
                                    var0.putAt_RefQueue(),
                                    var3,
                                    var4,
                                    var12,
                                    var14,
                                    var6,
                                    var16,
                                    var1.canSprint() && var1.strategy() != ClassCache.Inner_IoWorker.read
                                 );
                                 if (var17 != null) {
                                    var7.add(ClassCache.UiHelper.jump(var3, var12, ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var17), var17));
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static boolean putAt_Z_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, int var2, int var3, double var4, double var6
      ) {
         short var8 = var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ());
         double var9 = -1.0 / 0.0;

         for (AABB var12 : e.init_List_S(var8)) {
            if ((var2 <= 0 || !(var12.minX > 1.0E-6))
               && (var2 >= 0 || !(var12.maxX < 0.999999))
               && (var3 <= 0 || !(var12.minZ > 1.0E-6))
               && (var3 >= 0 || !(var12.maxZ < 0.999999))) {
               double var13 = var2 != 0 ? var12.minZ : var12.minX;
               double var15 = var2 != 0 ? var12.maxZ : var12.maxX;
               if (!(var13 >= 0.8) && !(var15 <= 0.2)) {
                  var9 = Math.max(var9, var12.maxY);
               }
            }
         }

         return Double.isFinite(var9) && var6 > var9 && var1.getY() + var9 - var4 <= 0.5625 && var6 - var9 <= 0.5625;
      }

      static double putAt(int var0, ClassCache.Inner_FileCache var1) {
         return var0 - 1 + var1.top16() / 16.0;
      }

      static boolean putAt(ClassCache.Inner_FileCache var0, ClassCache.Inner_FileCache var1) {
         return var0.passable() && var1.passable();
      }

      public static int putAt_I_FileStore_TextStor(
         ClassCache.Inner_StateCache var0, ClassCache.Inner_DataUtil var1, Set<Long> var2, BlockPos var3, List<ClassCache.UiHelper> var4
      ) {
         return putAt_I_FileStore_IoHelper(new ClassCache.Inner_RenderState.Inner_CachePool(var0, 256), var1, var2, var3, var4);
      }

      static int putAt_I_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         ClassCache.Inner_DataUtil var1,
         Set<Long> var2,
         BlockPos var3,
         List<ClassCache.UiHelper> var4
      ) {
         var0.flowState = 0;
         var0.putAt_FileStore_RingUtil = var1.fallPolicy();
         ClassCache.Inner_FileCache var5 = var0.putAt_FileStore_BitCache_BlockPos(var3);
         if (!var5.loaded()) {
            return var0.flowState;
         }

         ClassCache.Inner_FileCache var6 = var0.putAt_FileStore_BitCache_BlockPos(var3.below());
         ClassCache.Inner_FileCache var7 = var0.putAt_FileStore_BitCache_BlockPos(var3.above());
         boolean var8 = var1.allowParkour()
            && !var1.jumpBoost()
            && !var1.levitation()
            && !putAt(var1, ClassCache.Inner_SyncManager.start)
            && fill(var5, var6)
            && !var7.web();
         double var9 = var6.passable() ? var3.getY() : putAt_D_FileStore_IoHelper(var0, var3, var6);
         boolean var11 = !var6.narrowFooting() || TableIndex.putAt_Z_TokenCore_TaskStat(var0.putAt_RefQueue(), var3.getX() + 0.5, var9, var3.getZ() + 0.5);
         AABB var12 = null;
         if (var7.loaded()
            && (
               !var5.passable() && var0.putAt_S_I(var3.getX(), var3.getY(), var3.getZ()) > 1
                  || !var7.passable() && var0.putAt_S_I(var3.getX(), var3.getY() + 1, var3.getZ()) > 1
            )
            && TableIndex.putAt_Z_TokenCore_TaskStat(var0.putAt_RefQueue(), var3.getX() + 0.5, var9, var3.getZ() + 0.5)) {
            var12 = NodeWalker.Inner_StreamUtil.standing(var3.getX() + 0.5, var9, var3.getZ() + 0.5).box(false).deflate(1.0E-4);
         }

         if (var0.prevEx_Z && var8 && putAt_Z_FileStore_IoHelper_3(var0, var3, var6)) {
            putAt(var0, var1, var2, var3, var9, true, var4);
            return var0.flowState;
         }

         if (!putAt_Z_FileStore_BitCache(var5, var7, var6) && !var5.water() && !var5.climbable() && var12 == null) {
            boolean var13 = var8 && var0.prevEx_Z && putAt_Z_Function_2(var0, var3, null);
            if (!var5.passable() || var7.loaded() && !var7.passable()) {
               if (var13) {
                  putAt(var0, var1, var2, var3, var6, var9, var4);
                  return var0.flowState;
               }

               var0.flowState |= 2;
               if (var1.allowDig() && !putAt(var1, ClassCache.Inner_SyncManager.pop)) {
                  putAt_V_FileStore_IoHelper_2(var0, var1, var2, var3, var5, var4);
               }

               return var0.flowState;
            }

            if (!var13 && var7.loaded() && var6.loaded()) {
               var0.flowState |= 4;
            }
         }

         if (var5.water() || var5.climbable()) {
            BlockPos var48 = var3.above();
            ClassCache.Inner_FileCache var14 = var0.putAt_FileStore_BitCache_BlockPos(var48);
            boolean var15 = var14.climbable() || var14.water();
            if (!putAt(var1, ClassCache.Inner_SyncManager.get)
               && var15
               && !var2.contains(var48.asLong())
               && (var14.passable() || var14.climbable())
               && !var14.lava()
               && (var0.putAt_FileStore_BitCache_BlockPos(var48.above()).passable() || var0.putAt_FileStore_BitCache_BlockPos(var48.above()).climbable())
               && !var14.hazardFeet()) {
               var4.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.get, var3, var48, 5.5));
            }

            BlockPos var16 = var3.below();
            if (!putAt(var1, ClassCache.Inner_SyncManager.get)
               && !var2.contains(var16.asLong())
               && (
                  var5.climbable() && var0.putAt_FileStore_BitCache_BlockPos(var16).climbable()
                     || putAt_Z_FileStore_BitCache(
                        var0.putAt_FileStore_BitCache_BlockPos(var16),
                        var0.putAt_FileStore_BitCache_BlockPos(var16.above()),
                        var0.putAt_FileStore_BitCache_BlockPos(var16.below())
                     )
               )) {
               var4.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.get, var3, var16, 5.5));
            }

            if (var5.climbable()
               && var0.prevEx_Z
               && var1.allowParkour()
               && !var1.jumpBoost()
               && !var1.levitation()
               && !putAt(var1, ClassCache.Inner_SyncManager.LADDER_JUMP)) {
               boolean var17 = var1.canSprint() && var1.strategy() != ClassCache.Inner_IoWorker.read;
               putAt_V_FileStore_IoHelper_2(var0, var2, var3, var17, var4);
               putAt(var0, var2, var3, var3.getY() + 0.7, 255, true, var17, var4);
            }
         }

         for (int[] var60 : putAt_IArr2) {
            BlockPos var64 = var3.offset(var60[0], 0, var60[1]);
            if ((var12 == null || !var0.putAt_RefQueue().putAt_Z_AABB(var12.expandTowards(var60[0], 0.0, var60[1])))
               && !var0.putAt(var3, var60[0], var60[1])
               && !var0.putAt(var64, var60[0], var60[1])) {
               ClassCache.Inner_FileCache var18 = var0.putAt_FileStore_BitCache_BlockPos(var64);
               ClassCache.Inner_FileCache var19 = var0.putAt_FileStore_BitCache_BlockPos(var64.above());
               ClassCache.Inner_FileCache var20 = var0.putAt_FileStore_BitCache_BlockPos(var64.below());
               if (var1.allowDig()
                  && !putAt(var1, ClassCache.Inner_SyncManager.pop)
                  && var20.loaded()
                  && !var20.passable()
                  && !var20.hazardFloor()
                  && !var20.lava()
                  && !var2.contains(var64.asLong())
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()) {
                  boolean var21 = var18.diggable();
                  boolean var22 = var19.diggable();
                  if ((var21 || var18.passable()) && (var22 || var19.passable()) && (var21 || var22)) {
                     ArrayList var23 = new ArrayList(2);
                     double var24 = 0.0;
                     double var26 = 0.0;
                     if (var21) {
                        var23.add(var64);
                        var24 += var18.breakTicks() + var1.breakReluctance() + var18.destructionPenalty();
                        var26 += var18.breakTicks();
                     }

                     if (var22) {
                        var23.add(var64.above());
                        var24 += var19.breakTicks() + var1.breakReluctance() + var19.destructionPenalty();
                        var26 += var19.breakTicks();
                     }

                     if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                        var24 *= 2.0;
                     }

                     var4.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.pop,
                           var3,
                           var64,
                           var23,
                           List.of(),
                           ClassCache.Inner_CachePool.value_D_D(var24) + putAt_D_Function(var0, var3, var64, false),
                           var26
                        )
                     );
                  }
               }

               BlockPos var74 = var64.below();
               if (var1.allowDig() && !putAt(var1, ClassCache.Inner_SyncManager.next) && !var2.contains(var74.asLong())) {
                  ClassCache.Inner_FileCache var77 = var0.putAt_FileStore_BitCache_BlockPos(var74);
                  ClassCache.Inner_FileCache var86 = var0.putAt_FileStore_BitCache_BlockPos(var74.below());
                  if (var86.loaded()
                     && !var86.passable()
                     && !var86.hazardFloor()
                     && !var86.lava()
                     && !var86.farmland()
                     && var86.top16() == 16
                     && putAt(var19)
                     && putAt(var18)
                     && putAt(var77)
                     && !var19.web()
                     && !var18.web()
                     && !var77.web()
                     && (var19.diggable() || var18.diggable() || var77.diggable())
                     && putAt_Z_Function(var0, var74, var60[0], var60[1])) {
                     ArrayList var93 = new ArrayList(3);
                     double var25 = 0.0;
                     double var27 = 0.0;

                     for (BlockPos var32 : new BlockPos[]{var64.above(), var64, var74}) {
                        ClassCache.Inner_FileCache var33 = var32.equals(var74) ? var77 : (var32.equals(var64) ? var18 : var19);
                        if (var33.diggable()) {
                           var93.add(var32);
                           var25 += var33.breakTicks() + var1.breakReluctance() + var33.destructionPenalty();
                           var27 += var33.breakTicks();
                        }
                     }

                     if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                        var25 *= 2.0;
                     }

                     double var120 = var1.strategy() == ClassCache.Inner_IoWorker.load ? 1.0 : 0.0;
                     var4.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.next, var3, var74, var93, List.of(), ClassCache.Inner_CachePool.readEx_D_I(1) + var120 + var25, var27
                        )
                     );
                  }
               }

               if (var1.allowBridge()
                  && var1.hasBlocks()
                  && !putAt(var1, ClassCache.Inner_SyncManager.run)
                  && var0.putAt_FileStore_BitCache_BlockPos(var3.below()).top16() == 16
                  && var0.putAt_S_I(var3.getX(), var3.getY() - 1, var3.getZ()) == 1
                  && !var2.contains(var64.asLong())
                  && var18.passable()
                  && var19.passable()
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && var20.loaded()
                  && var20.passable()
                  && (var1.supportedBridges() || !var20.water() && !var20.lava())
                  && (
                     var1.supportedBridges()
                        || !var0.putAt_FileStore_BitCache_BlockPos(var64.below(2)).water() && !var0.putAt_FileStore_BitCache_BlockPos(var64.below(2)).lava()
                  )
                  && putAt_Z_Function_3(var0, var3, var64, var0.putAt_FileStore_PacketHe)) {
                  int var78 = var1.supportedBridges() ? 0 : flowState(var0, var64.below());
                  if ((var1.strategy() == ClassCache.Inner_IoWorker.load || ClassCache.Inner_CachePool.fill()) && var78 > 3) {
                     var78 = -1;
                  }

                  if (var78 >= 0) {
                     double var87 = var1.strategy() == ClassCache.Inner_IoWorker.read ? 9.0 : 4.5;
                     ClassCache.UiHelper var98 = var0.putAt_FileStore_PacketHe;
                     boolean var105 = var98 != null
                        && var98.type() == ClassCache.Inner_SyncManager.run
                        && var98.to().getX() - var98.from().getX() == var64.getX() - var3.getX()
                        && var98.to().getZ() - var98.from().getZ() == var64.getZ() - var3.getZ();
                     if (!var105) {
                        var87 += 10.0;
                     }

                     if (var1.supportedBridges() || !var0.putAt_FileStore_BitCache_BlockPos(var64.above(2)).passable()) {
                        var87 += 2.0;
                     }

                     if (var78 > 3) {
                        var87 += 1.125 * Math.min(var78 - 3, 20);
                     }

                     var4.add(new ClassCache.UiHelper(ClassCache.Inner_SyncManager.run, var3, var64, List.of(), List.of(var64.below()), var87));
                  }
               }

               if (ClassCache.Inner_CachePool.readEx()
                  && var1.allowBridge()
                  && var1.hasBlocks()
                  && !putAt(var1, ClassCache.Inner_SyncManager.run)
                  && var0.putAt_FileStore_BitCache_BlockPos(var3.below()).top16() == 16
                  && var0.putAt_S_I(var3.getX(), var3.getY() - 1, var3.getZ()) == 1
                  && var18.passable()
                  && var19.passable()
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && var20.loaded()
                  && var20.passable()
                  && !var20.water()
                  && !var20.lava()
                  && !var20.hazardFeet()) {
                  BlockPos var79 = var64.below();
                  BlockPos var88 = var79.below();
                  ClassCache.Inner_FileCache var94 = var0.putAt_FileStore_BitCache_BlockPos(var88);
                  if (!var2.contains(var79.asLong()) && var94.loaded() && var94.passable() && !var94.water() && !var94.lava() && readEx(var0, var88)) {
                     int var99 = flowState(var0, var88);
                     if ((var1.strategy() == ClassCache.Inner_IoWorker.load || ClassCache.Inner_CachePool.fill()) && var99 > 3) {
                        var99 = -1;
                     }

                     if (var99 >= 0) {
                        var4.add(
                           new ClassCache.UiHelper(
                              ClassCache.Inner_SyncManager.run, var3, var79, List.of(), List.of(var88), 14.5 + ClassCache.Inner_CachePool.readEx_D_I(1)
                           )
                        );
                     }
                  }
               }

               if (var11 && !var2.contains(var64.asLong()) && putAt_Z_FileStore_BitCache(var18, var19, var20) && (!var1.avoidWater() || !var18.water())) {
                  double var80 = putAt(var64.getY(), var20) - var9;
                  if (var80 <= 0.5625) {
                     boolean var95 = var1.canSprint()
                        && var1.strategy() != ClassCache.Inner_IoWorker.read
                        && !var18.water()
                        && !var18.thorny()
                        && !putAt(var1, ClassCache.Inner_SyncManager.read);
                     double var100 = var18.water() ? 3.0 + (var1.strategy() == ClassCache.Inner_IoWorker.load ? 8.0 : 0.0) : 0.0;
                     double var110 = var20.crawl();
                     double var121 = var18.thorny() ? 25.0 : 0.0;
                     ClassCache.Inner_SyncManager var131 = var95 ? ClassCache.Inner_SyncManager.read : ClassCache.Inner_SyncManager.init;
                     if (!putAt(var1, var131)) {
                        var4.add(
                           ClassCache.UiHelper.travel(
                              var131,
                              var3,
                              var64,
                              (var95 ? 3.2 : 4.5 + var100) * var110 + var121 + putAt_D_Function(var0, var3, var64, var95) + var0.putAt_D_BlockPos(var64)
                           )
                        );
                     }

                     BlockPos var137 = var64.offset(var60[0], 0, var60[1]);
                     ClassCache.Inner_FileCache var141 = var0.putAt_FileStore_BitCache_BlockPos(var137.below());
                     if (var95
                        && !var141.passable()
                        && !putAt(var1, ClassCache.Inner_SyncManager.reset)
                        && prevEx(var0, var3, var64)
                        && !var0.fill(var64)
                        && !var0.fill(var137)) {
                        var4.add(
                           ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.reset, var3, var64, prev(var0, var3, var64) ? 2.2 : 2.9000000000000004)
                        );
                     }
                  } else if (var80 <= 1.250001
                     && !var5.water()
                     && !var6.farmland()
                     && !var6.honey()
                     && (!var0.prevEx_Z || !var1.allowParkour() || !var5.climbable() || !var6.passable())
                     && !putAt(var1, ClassCache.Inner_SyncManager.sync)
                     && !var5.web()
                     && !var7.web()
                     && !var18.web()
                     && !var19.web()
                     && !var0.putAt_FileStore_BitCache_BlockPos(var3.above(2)).web()
                     && var0.putAt_FileStore_BitCache_BlockPos(var3.above(2)).passable()) {
                     var4.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.sync, var3, var64, 9.0));
                  }
               }

               if (var1.allowDig()
                  && !putAt(var1, ClassCache.Inner_SyncManager.pop)
                  && (var18.web() || var19.web())
                  && !var2.contains(var64.asLong())
                  && putAt_Z_FileStore_BitCache(var18, var19, var20)
                  && putAt(var64.getY(), var20) - var9 <= 0.5625) {
                  ArrayList var81 = new ArrayList(2);
                  double var89 = 0.0;
                  double var101 = 0.0;

                  for (BlockPos var127 : new BlockPos[]{var64, var64.above()}) {
                     ClassCache.Inner_FileCache var132 = var127 == var64 ? var18 : var19;
                     if (var132.web()) {
                        if (var132.breakTicks() >= 1.0 / 0.0) {
                           var81 = null;
                           break;
                        }

                        var81.add(var127);
                        var89 += var132.breakTicks() + var1.breakReluctance();
                        var101 += var132.breakTicks();
                     }
                  }

                  if (var81 != null && !var81.isEmpty()) {
                     var4.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.pop,
                           var3,
                           var64,
                           var81,
                           List.of(),
                           ClassCache.Inner_CachePool.value_D_D(var89) + var0.putAt_D_BlockPos(var64),
                           var101
                        )
                     );
                  }
               }

               if (!var2.contains(var64.asLong())
                  && var18.door()
                  && !var18.passable()
                  && !putAt(var1, ClassCache.Inner_SyncManager.set)
                  && !var18.hazardFeet()
                  && !var18.lava()
                  && !var19.hazardFeet()
                  && !var19.lava()
                  && (var19.door() || var19.passable())
                  && var20.loaded()
                  && !var20.passable()
                  && !var20.hazardFloor()
                  && !var20.lava()
                  && putAt(var64.getY(), var20) - var9 <= 0.5625) {
                  var4.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.set, var3, var64, 7.5));
               }

               BlockPos var82 = var64.above();
               ClassCache.Inner_FileCache var90 = var0.putAt_FileStore_BitCache_BlockPos(var82);
               int var96 = !var18.passable() && var90.top16() > 0 && var90.top16() <= 4 ? 2 : 1;
               BlockPos var102 = var64.above(var96);
               if (!var2.contains(var102.asLong())) {
                  ClassCache.Inner_FileCache var106 = var0.putAt_FileStore_BitCache_BlockPos(var102);
                  ClassCache.Inner_FileCache var112 = var0.putAt_FileStore_BitCache_BlockPos(var102.above());
                  ClassCache.Inner_FileCache var116 = var0.putAt_FileStore_BitCache_BlockPos(var102.below());
                  if ((!var116.passable() || var5.water() && var106.water()) && putAt_Z_FileStore_BitCache(var106, var112, var116)) {
                     double var123 = putAt(var102.getY(), var116) - var9;
                     if (var123 <= 0.5625
                        || var116.slabStair() && putAt_Z_FileStore_IoHelper(var0, var102.below(), var60[0], var60[1], var9, var116.top16() / 16.0)) {
                        if (!putAt(var1, ClassCache.Inner_SyncManager.update)) {
                           boolean var133 = var1.canSprint()
                              && var1.strategy() != ClassCache.Inner_IoWorker.read
                              && !var106.water()
                              && !putAt(var1, ClassCache.Inner_SyncManager.read);
                           double var138 = var123 > 0.0 && var123 <= 0.062501 ? 4.5 : (var133 ? 3.6 : 5.0);
                           var4.add(
                              ClassCache.UiHelper.travel(
                                 ClassCache.Inner_SyncManager.update,
                                 var3,
                                 var102,
                                 var138 + putAt_D_Function(var0, var3, var102, false) + var0.putAt_D_BlockPos(var102)
                              )
                           );
                        }
                     } else if (var123 <= 1.25
                        && (!var6.farmland() || var5.water())
                        && !var6.honey()
                        && (!var0.prevEx_Z || !var1.allowParkour() || !var5.climbable() || !var6.passable())
                        && !putAt(var1, ClassCache.Inner_SyncManager.sync)
                        && !var5.web()
                        && !var7.web()
                        && !var106.web()
                        && !var112.web()
                        && !var0.putAt_FileStore_BitCache_BlockPos(var3.above(2)).web()
                        && (!var5.water() || fill(var0, var82, var60[0], var60[1], 1, true))
                        && var0.putAt_FileStore_BitCache_BlockPos(var3.above(2)).passable()) {
                        var4.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.sync, var3, var102, 9.0));
                     }
                  }
               }

               if (var1.allowDig()
                  && !putAt(var1, ClassCache.Inner_SyncManager.stop)
                  && !var2.contains(var82.asLong())
                  && !var6.farmland()
                  && !var6.honey()
                  && !var5.water()
                  && !var5.lava()
                  && !var5.web()
                  && !var7.web()
                  && var18.loaded()
                  && !var18.passable()
                  && var18.top16() == 16
                  && !var18.hazardFloor()
                  && !var18.lava()
                  && !var18.door()
                  && putAt(var82.getY(), var18) - var9 <= 1.250001) {
                  BlockPos var107 = var3.above(2);
                  ClassCache.Inner_FileCache var113 = var0.putAt_FileStore_BitCache_BlockPos(var107);
                  ClassCache.Inner_FileCache var117 = var0.putAt_FileStore_BitCache_BlockPos(var82);
                  ClassCache.Inner_FileCache var124 = var0.putAt_FileStore_BitCache_BlockPos(var82.above());
                  if (putAt(var113)
                     && putAt(var117)
                     && putAt(var124)
                     && !var113.web()
                     && !var117.web()
                     && !var124.web()
                     && (var113.diggable() || var117.diggable() || var124.diggable())
                     && (!var113.diggable() || !var0.putAt_FileStore_BitCache_BlockPos(var107.above()).falling())) {
                     ArrayList var128 = new ArrayList(3);
                     double var134 = 0.0;
                     double var142 = 0.0;

                     for (BlockPos var38 : new BlockPos[]{var107, var82, var82.above()}) {
                        ClassCache.Inner_FileCache var39 = var38 == var107 ? var113 : (var38 == var82 ? var117 : var124);
                        if (var39.diggable()) {
                           var128.add(var38);
                           var134 += var39.breakTicks() + var1.breakReluctance() + var39.destructionPenalty();
                           var142 += var39.breakTicks();
                        }
                     }

                     if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                        var134 *= 2.0;
                     }

                     var4.add(new ClassCache.UiHelper(ClassCache.Inner_SyncManager.stop, var3, var82, var128, List.of(), 9.0 + var134, var142));
                  }
               }

               long var108 = -9223372036854775808L;
               int var118 = (int)Math.floor(var9 + 1.8 - 1.0E-6);
               int var125 = var1.fallPolicy().putAt_Z() ? (int)var1.fallPolicy().putAt_D : 8;

               for (int var129 = 1; var129 <= var125; var129++) {
                  BlockPos var135 = var64.below(var129);
                  ClassCache.Inner_FileCache var139 = var0.putAt_FileStore_BitCache_BlockPos(var135);
                  if (!var139.loaded()) {
                     break;
                  }

                  BlockPos var143 = var135.above();
                  if (var143.getY() <= var118 && !var0.putAt_FileStore_BitCache_BlockPos(var143).passable()
                     || var129 == 1 && var64.above().getY() <= var118 && !var0.putAt_FileStore_BitCache_BlockPos(var64.above()).passable()
                     || var139.feetTrigger()
                     || var1.fallPolicy() != ClassCache.Inner_NetHelper.init && (var139.lava() || var139.hazardFeet())) {
                     break;
                  }

                  if (!var2.contains(var135.asLong())) {
                     ClassCache.Inner_FileCache var34 = var0.putAt_FileStore_BitCache_BlockPos(var135.below());
                     double var144 = var139.climbable() ? var135.getY() : putAt(var135.getY(), var34);
                     double var145 = var9 - var144;
                     double var146 = var1.fallPolicy().putAt_Z()
                        ? var125
                        : Math.min(3, var1.maxFall()) + (var1.fallPolicy() == ClassCache.Inner_NetHelper.init ? 0.5625 : 0.0);
                     if (var145 <= var146
                        && !var34.farmland()
                        && putAt_Z_FileStore_BitCache(var139, var0.putAt_FileStore_BitCache_BlockPos(var135.above()), var34)
                        && (
                           var145 <= 0.562501
                              || var129 == 1 && var139.climbable()
                              || putAt_Z_Function(var0, var135, var60[0], var60[1], (int)Math.ceil(var145 - 1.0E-6))
                        )) {
                        ClassCache.Inner_SyncManager var41 = var129 == 1
                           ? ClassCache.Inner_SyncManager.flush
                           : (var129 == 2 ? ClassCache.Inner_SyncManager.close : ClassCache.Inner_SyncManager.open);
                        if (var129 == 1 && var145 > 1.5625) {
                           var41 = ClassCache.Inner_SyncManager.close;
                        }

                        double var42 = var1.strategy() == ClassCache.Inner_IoWorker.load ? var129 * 1.0 : 0.0;
                        if (!putAt(var1, var41)) {
                           double var44 = var145 > 0.0 && var145 <= 0.062501
                              ? 4.5
                              : (
                                 var129 <= 3
                                    ? ClassCache.Inner_CachePool.readEx_D_I(var129)
                                    : ClassCache.Inner_CachePool.readEx_D_I(3) + Math.ceil(Math.sqrt(var145) * 5.0 - 8.0)
                              );
                           var4.add(
                              ClassCache.UiHelper.travel(
                                 var41,
                                 var3,
                                 var135,
                                 var44
                                    + var42
                                    + putAt_D(var0, var135)
                                    + putAt_D_Function(var0, var3, var64, false)
                                    + 255.0 * putAt_D_Function(var0, var64.getX(), var64.getZ(), var9, putAt(var135.getY(), var34))
                              )
                           );
                        }
                     }

                     int var147 = Math.min(var139.fallMult10(), var34.fallMult10());
                     if (var139.passable()
                        && var147 <= 2
                        && !var139.lava()
                        && !var34.lava()
                        && !var139.hazardFeet()
                        && !var139.feetTrigger()
                        && (!var1.avoidWater() || !var139.water())) {
                        BlockPos var148 = null;
                        if (!var139.water()) {
                           if (var34.passable()) {
                              if (!var34.water() && !var34.hazardFeet()) {
                                 var148 = var135.below();
                              }
                           } else {
                              var148 = var135;
                           }
                        } else if (!var0.putAt_FileStore_BitCache_BlockPos(var135.above()).water()) {
                           BlockPos var43 = var135;

                           while (var0.putAt_FileStore_BitCache_BlockPos(var43.below()).water()) {
                              var43 = var43.below();
                           }

                           if (var3.getY() - var43.getY() <= 8 && fill(var0.putAt_FileStore_BitCache_BlockPos(var43.below()))) {
                              var148 = var135;
                           }
                        }

                        if (var148 != null && var148.asLong() != var108) {
                           var108 = var148.asLong();
                           if (!var0.putAt_FileStore_BitCache_BlockPos(var148.below()).farmland()) {
                              int var149 = Math.max(var129, var3.getY() - var148.getY());
                              double var150 = var1.strategy() == ClassCache.Inner_IoWorker.load ? var149 * 2.0 : 0.0;
                              double var46 = 255.0 * putAt_D_Function(var0, var64.getX(), var64.getZ(), var9, var148.getY());
                              if (!putAt(var1, ClassCache.Inner_SyncManager.poll)) {
                                 var4.add(
                                    ClassCache.UiHelper.travel(
                                       ClassCache.Inner_SyncManager.poll,
                                       var3,
                                       var148,
                                       ClassCache.Inner_CachePool.value_D_I(var149)
                                          + var150
                                          + putAt_D_Function(var0, var3, var64, false)
                                          + var46
                                          + putAt_D(var0, var148)
                                    )
                                 );
                              }
                           }
                        }
                     }
                  }
               }

               if (var108 == -9223372036854775808L
                  && !var1.avoidWater()
                  && !putAt(var1, ClassCache.Inner_SyncManager.poll)
                  && var0.putAt_FileStore_BitCache_BlockPos(var64).passable()
                  && var0.putAt_FileStore_BitCache_BlockPos(var64.above()).passable()
                  && var0.putAt_FileStore_BitCache_BlockPos(var64.below()).passable()
                  && var0.putAt_FileStore_BitCache_BlockPos(var64.below(2)).passable()) {
                  BlockPos var130 = var0.putAt_BlockPos_BlockPos(var3, var9, var60[0], var60[1]);
                  if (var130 != null && !var2.contains(var130.asLong()) && !var0.putAt_FileStore_BitCache_BlockPos(var130.below()).farmland()) {
                     int var136 = Math.max(1, var3.getY() - var130.getY());
                     double var140 = var1.strategy() == ClassCache.Inner_IoWorker.load ? var136 * 2.0 : 0.0;
                     var4.add(
                        ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.poll, var3, var130, ClassCache.Inner_CachePool.value_D_I(var136) + var140)
                     );
                  }
               }
            }
         }

         for (int[] var61 : fill_IArr2) {
            if (!var11) {
               break;
            }

            if (var12 == null || !var0.putAt_RefQueue().putAt_Z_AABB(var12.expandTowards(var61[0], 0.0, var61[1]))) {
               BlockPos var65 = var3.offset(var61[0], 0, var61[1]);
               if (!var2.contains(var65.asLong())) {
                  ClassCache.Inner_FileCache var67 = var0.putAt_FileStore_BitCache_BlockPos(var65);
                  ClassCache.Inner_FileCache var70 = var0.putAt_FileStore_BitCache_BlockPos(var65.below());
                  if (putAt_Z_FileStore_BitCache(var67, var0.putAt_FileStore_BitCache_BlockPos(var65.above()), var70)
                     && (!var1.avoidWater() || !var67.water())
                     && !(putAt(var65.getY(), var70) - var9 > 0.5625)) {
                     BlockPos var71 = var3.offset(var61[0], 0, 0);
                     BlockPos var75 = var3.offset(0, 0, var61[1]);
                     if (putAt_Z(var0, var71)
                        && putAt_Z(var0, var75)
                        && !var0.putAt_Z_BlockPos(var3)
                        && !var0.putAt_Z_BlockPos(var65)
                        && !var0.putAt_Z_BlockPos(var71)
                        && !var0.putAt_Z_BlockPos(var75)) {
                        boolean var83 = var1.canSprint()
                           && var1.strategy() != ClassCache.Inner_IoWorker.read
                           && !var67.water()
                           && !var67.thorny()
                           && !putAt(var1, ClassCache.Inner_SyncManager.write);
                        double var91 = var67.water() ? 3.0 + (var1.strategy() == ClassCache.Inner_IoWorker.load ? 8.0 : 0.0) : 0.0;
                        double var103 = var70.crawl();
                        ClassCache.Inner_SyncManager var114 = var83 ? ClassCache.Inner_SyncManager.write : ClassCache.Inner_SyncManager.load;
                        if (!putAt(var1, var114)) {
                           var4.add(
                              ClassCache.UiHelper.travel(
                                 var114,
                                 var3,
                                 var65,
                                 (var83 ? 4.5 : 6.4 + var91) * var103
                                    + (var67.thorny() ? 25.0 : 0.0)
                                    + putAt_D_Function(var0, var3, var65, var83)
                                    + var0.putAt_D_BlockPos(var65)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         if (var8) {
            putAt(var0, var1, var2, var3, var6, var9, var4);
            if (var0.prevEx_Z
               && var1.canSprint()
               && var1.strategy() != ClassCache.Inner_IoWorker.read
               && var0.putAt_FileStore_RingUtil != ClassCache.Inner_NetHelper.init
               && var9 == var3.getY()) {
               putAt_V_FileStore_IoHelper(var0, var2, var3, var9, var4);
            }

            if (var0.prevEx_Z) {
               putAt(var0, var1, var2, var3, var9, false, var4);
            }

            if (var0.prevEx_Z && !putAt(var1, ClassCache.Inner_SyncManager.SLIME_BOUNCE)) {
               putAt_V_FileStore_IoHelper(var0, var1, var2, var3, var9, var4);
            }
         }

         if (var1.allowPillar()
            && var1.hasBlocks()
            && !putAt(var1, ClassCache.Inner_SyncManager.tick)
            && !var5.lava()
            && !var5.water()
            && !var6.honey()
            && (!var6.farmland() && var6.top16() >= 16 || var1.repairFarmland() && var6.farmland() && var6.top16() == 15 || var6.soulSand())
            && !var6.door()
            && !var5.web()
            && !var7.web()
            && !var2.contains(var3.above().asLong())
            && !var0.putAt_FileStore_BitCache_BlockPos(var3.above()).hazardFeet()) {
            var var51 = putAt_List_Function(var0, var1, var3);
            if (var51 != null) {
               double var55 = var1.strategy() == ClassCache.Inner_IoWorker.read ? 24.0 : 12.0;
               double var62 = 0.0;
               double var68 = 0.0;

               for (BlockPos var76 : var51) {
                  ClassCache.Inner_FileCache var84 = var0.putAt_FileStore_BitCache_BlockPos(var76);
                  var62 += var84.breakTicks() + var1.breakReluctance() + var84.destructionPenalty();
                  var68 += var84.breakTicks();
               }

               if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                  var62 *= 2.0;
               }

               var4.add(
                  new ClassCache.UiHelper(
                     ClassCache.Inner_SyncManager.tick,
                     var3,
                     var3.above(),
                     var51,
                     List.of(var3),
                     var55 + var62,
                     var51.isEmpty() ? -1.0 : var68,
                     null,
                     var1.repairFarmland() && var6.farmland()
                  )
               );
            }
         }

         BlockPos var52 = var3.below();
         ClassCache.Inner_FileCache var56 = var0.putAt_FileStore_BitCache_BlockPos(var52);
         boolean var59 = var56.passable() && !var56.water() && !var56.lava() && !var56.hazardFeet();
         if (!putAt(var1, ClassCache.Inner_SyncManager.push)
            && (var59 || var1.allowDig() && var56.diggable())
            && !var2.contains(var52.asLong())
            && !var5.lava()
            && (!var5.water() || !var0.putAt_FileStore_BitCache_BlockPos(var3.above()).water())
            && !var0.putAt_FileStore_BitCache_BlockPos(var3.above(2)).falling()) {
            ClassCache.Inner_FileCache var63 = var0.putAt_FileStore_BitCache_BlockPos(var52.below());
            boolean var66 = var63.loaded()
               && !var63.passable()
               && !var63.hazardFloor()
               && !var63.lava()
               && !var63.water()
               && !var63.farmland()
               && var63.top16() <= 16;
            if (var66) {
               double var69 = var59 ? 0.0 : var56.breakTicks() + var1.breakReluctance() + var56.destructionPenalty();
               double var73 = var59 ? 0.0 : var56.breakTicks();
               ArrayList var85 = new ArrayList(3);
               var85.add(var52);
               boolean var92 = true;

               for (BlockPos var115 : new BlockPos[]{var3.above(), var3}) {
                  ClassCache.Inner_FileCache var119 = var0.putAt_FileStore_BitCache_BlockPos(var115);
                  if (var119.passable() && !var119.water() && !var119.lava()) {
                     short var126 = var0.fill_S(var115.getX(), var115.getY(), var115.getZ());
                     if (var126 == -1) {
                        var0.flowState |= 1;
                        var92 = false;
                        break;
                     }

                     if (var126 != 0) {
                        if (!var1.allowDig() || var119.breakTicks() >= 1.0 / 0.0) {
                           var92 = false;
                           break;
                        }

                        var85.add(var115);
                        var69 += var119.breakTicks() + var1.breakReluctance() + var119.destructionPenalty();
                        var73 += var119.breakTicks();
                     }
                  }
               }

               if (var92) {
                  if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                     var69 *= 2.0;
                  }

                  var4.add(
                     new ClassCache.UiHelper(
                        ClassCache.Inner_SyncManager.push,
                        var3,
                        var52,
                        var85,
                        List.of(),
                        var59 && var85.size() == 1 ? ClassCache.Inner_CachePool.readEx_D_I(1) : ClassCache.Inner_CachePool.readEx_D_D(var69),
                        var73
                     )
                  );
               }
            }
         }

         return var0.flowState;
      }

      static List<BlockPos> putAt_List_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, ClassCache.Inner_DataUtil var1, BlockPos var2) {
         BlockPos var3 = var2.above(2);
         BlockPos var4 = var2.above(3);
         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var3);
         ClassCache.Inner_FileCache var6 = (ClassCache.Inner_FileCache)var0.apply(var4);
         if (var5.loaded() && !var5.lava() && !var5.water() && !var5.hazardFeet() && !var5.web()) {
            ArrayList<BlockPos> var7 = new ArrayList<>(2);
            if (!var5.passable()) {
               if (!var1.allowDig() || !var5.diggable() || var6.falling() || var6.lava() || var6.water()) {
                  return null;
               }

               var7.add(var3);
            }

            if (var6.loaded() && !var6.lava() && !var6.hazardFeet()) {
               if (!var6.passable() && var1.allowDig() && var6.diggable()) {
                  ClassCache.Inner_FileCache var8 = (ClassCache.Inner_FileCache)var0.apply(var4.above());
                  if (!var8.falling() && !var8.lava() && !var8.water()) {
                     var7.add(var4);
                  }
               }

               return var7;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      static boolean putAt(ClassCache.Inner_FileCache var0) {
         if (var0.loaded() && !var0.lava() && !var0.hazardFeet()) {
            return var0.passable() ? !var0.water() : var0.diggable();
         } else {
            return false;
         }
      }

      private static void putAt_V_FileStore_IoHelper_2(
         ClassCache.Inner_RenderState.Inner_CachePool var0, Set<Long> var1, BlockPos var2, boolean var3, List<ClassCache.UiHelper> var4
      ) {
         boolean var5 = var0.putAt_FileStore_BitCache_BlockPos(var2.above()).climbable();

         for (int[] var9 : readEx_IArr2) {
            for (int var10 = 1; var10 <= 2; var10++) {
               int var11 = var9[0] * var10;
               int var12 = var9[1] * var10;
               float var13 = NodeWalker.putAt_F_D(0.0, 0.0, var11, var12);

               for (int var14 = var5 ? 0 : 2; var14 >= -2; var14--) {
                  BlockPos var15 = var2.offset(var11, var14, var12);
                  if (!var1.contains(var15.asLong())) {
                     ClassCache.Inner_FileCache var16 = var0.putAt_FileStore_BitCache_BlockPos(var15.below());
                     if (putAt_Z_FileStore_BitCache(var0.putAt_FileStore_BitCache_BlockPos(var15), var0.putAt_FileStore_BitCache_BlockPos(var15.above()), var16)
                        && !var16.farmland()
                        && !var16.slime()) {
                        double var17 = putAt(var15.getY(), var16);

                        for (double var22 : new double[]{0.35, 0.65, 0.85}) {
                           NodeWalker.Inner_StreamUtil var24 = new NodeWalker.Inner_StreamUtil(
                              var2.getX() + 0.5, var2.getY() + var22, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false
                           );
                           TableIndex.Inner_StateCache var25 = AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var0.putAt_RefQueue(), var24, var13, var15, var17);
                           if (var25 != null) {
                              var4.add(
                                 new ClassCache.UiHelper(
                                    ClassCache.Inner_SyncManager.LADDER_JUMP, var2, var15, List.of(), List.of(), var25.ticks() + 5.5, -1.0, var25
                                 )
                              );
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         if (!var5 && var0.putAt_S_I(var2.getX(), var2.getY(), var2.getZ()) != 0) {
            StreamGate var26 = var0.putAt_RefQueue();
            NodeWalker.Inner_StreamUtil var27 = new NodeWalker.Inner_StreamUtil(
               var2.getX() + 0.5, var2.getY() + 0.7, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false
            );
            AiLadderJump.Inner_CacheHelper var28 = AiLadderJump.putAt_TableCore_CacheHel(var26, var27, var2);
            if (var28 != null) {
               for (ClassCache.Inner_RenderState.Inner_StreamUtil var32 : putAt_FileStore_IoHelperArr) {
                  if (var32.distance() > 2 && !(var32.length() > 5.2)) {
                     for (int var33 = 2; var33 >= -2; var33--) {
                        BlockPos var34 = var2.offset(var32.x(), var33, var32.z());
                        ClassCache.Inner_FileCache var35 = var0.putAt_FileStore_BitCache_BlockPos(var34.below());
                        ClassCache.Inner_FileCache var36 = var0.putAt_FileStore_BitCache_BlockPos(var34);
                        if (!var36.climbable()
                           && !var35.slime()
                           && !var35.farmland()
                           && !var1.contains(var34.asLong())
                           && putAt_Z_FileStore_BitCache(var36, var0.putAt_FileStore_BitCache_BlockPos(var34.above()), var35)) {
                           TableIndex.Inner_StateCache var37 = AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(
                              var26, var27, var28, var34, putAt(var34.getY(), var35), var3
                           );
                           if (var37 != null) {
                              var4.add(
                                 new ClassCache.UiHelper(
                                    ClassCache.Inner_SyncManager.LADDER_JUMP, var2, var34, List.of(), List.of(), var37.ticks() + 5.5, -1.0, var37
                                 )
                              );
                           }
                           break;
                        }
                     }
                  }
               }
            }
         }
      }

      private static void putAt(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         Set<Long> var1,
         BlockPos var2,
         double var3,
         int var5,
         boolean var6,
         boolean var7,
         List<ClassCache.UiHelper> var8
      ) {
         StreamGate var9 = var0.putAt_RefQueue();
         NodeWalker.Inner_StreamUtil var10 = var6
            ? new NodeWalker.Inner_StreamUtil(var2.getX() + 0.5, var3, var2.getZ() + 0.5, 0.0, 0.1176, 0.0, false, 0, false, false)
            : NodeWalker.Inner_StreamUtil.standing(var2.getX() + 0.5, var3, var2.getZ() + 0.5);
         if (var6 || TableIndex.putAt_Z_TokenCore_TaskStat(var9, var10.x(), var3, var10.z())) {
            boolean var11 = !var6 && var0.putAt_FileStore_RingUtil.putAt_Z();
            ClassCache.Inner_RenderState.Inner_StreamUtil[] var12 = var11 ? value_FileStore_IoHelperArr : putAt_FileStore_IoHelperArr;
            double var13 = var6 ? (var0.putAt_FileStore_BitCache_BlockPos(var2.above()).climbable() ? 3.8 : 5.2) : (var11 ? 7.3 : 6.1);

            for (ClassCache.Inner_RenderState.Inner_StreamUtil var18 : var12) {
               if ((var5 & var18.exits()) != 0 && !(var18.length() > var13)) {
                  for (int var19 = 1; var19 >= -4; var19--) {
                     BlockPos var20 = var2.offset(var18.x(), var19, var18.z());
                     ClassCache.Inner_FileCache var21 = var0.putAt_FileStore_BitCache_BlockPos(var20);
                     if (var21.climbable() && !var21.hazardFeet() && !var21.water() && !var1.contains(var20.asLong())) {
                        if (var18.distance() <= 2 && putAt_Z_FileStore_IoHelper_2(var0, var20, var3)) {
                           break;
                        }

                        TableIndex.Inner_StateCache var22 = var6
                           ? AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat_2(var9, var10, var2, var20, var7)
                           : AiLadderJump.putAt_IoUtil_TextState_TokenCore_TaskStat(var9, var10, var20, var7);
                        if (var22 != null) {
                           BlockPos var24 = BlockPos.containing(var22.landX(), var22.landY(), var22.landZ());
                           if (!var1.contains(var24.asLong())) {
                              var8.add(
                                 new ClassCache.UiHelper(
                                    ClassCache.Inner_SyncManager.LADDER_JUMP, var2, var24, List.of(), List.of(), var22.ticks() + 2.0, -1.0, var22
                                 )
                              );
                           }
                           break;
                        }

                        int var23 = Math.max(-4, var19 - 4);

                        while (var19 > var23 && var0.putAt_FileStore_BitCache_BlockPos(var2.offset(var18.x(), var19 - 1, var18.z())).climbable()) {
                           var19--;
                        }
                     }
                  }
               }
            }
         }
      }

      private static boolean putAt_Z_FileStore_IoHelper_2(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, double var2) {
         for (BlockPos var4 = var1; var4.getY() >= Math.floor(var2) - 1.0; var4 = var4.below()) {
            if (!var0.putAt_FileStore_BitCache_BlockPos(var4.below()).climbable()) {
               ClassCache.Inner_FileCache var5 = var0.putAt_FileStore_BitCache_BlockPos(var4.below());
               return var4.getY() - var2 <= 1.25 && var5.loaded() && var5.top16() >= 8 && !var5.passable() && !var5.hazardFloor();
            }
         }

         return false;
      }

      private static void putAt_V_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         ClassCache.Inner_DataUtil var1,
         Set<Long> var2,
         BlockPos var3,
         double var4,
         List<ClassCache.UiHelper> var6
      ) {
         boolean var7 = !var0.readEx();
         List var8 = var7 ? var0.fill_Map.get(var3) : null;
         if (var8 == null) {
            StreamGate var9 = var0.putAt_RefQueue();
            long var10 = var9.putAt_J;
            var8 = putAt_List_FileStore_IoHelper(var0, var1, var3, var4);
            if (var7 && var9.putAt_J == var10 && (var0.flowState & 1) == 0) {
               var0.fill_Map.put(var3, var8);
            }
         }

         for (ClassCache.UiHelper var13 : (Iterable<ClassCache.UiHelper>)(Iterable<?>)(var8)) {
            if (!var2.contains(var13.to().asLong())) {
               var6.add(var13);
            }
         }
      }

      private static List<ClassCache.UiHelper> putAt_List_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0, ClassCache.Inner_DataUtil var1, BlockPos var2, double var3
      ) {
         ArrayList<ClassCache.UiHelper> var5 = new ArrayList<>();
         boolean var6 = var1.canSprint() && var1.strategy() != ClassCache.Inner_IoWorker.read;

         for (int[] var10 : readEx_IArr2) {
            int var11 = var10[0];
            int var12 = var10[1];
            if (var0.putAt_FileStore_BitCache_BlockPos(var2.offset(var11, -1, var12)).passable()
               && var0.putAt_FileStore_BitCache_BlockPos(var2.offset(var11, 0, var12)).passable()
               && putAt_Z_FileStore_IoHelper(var0, var2, var11, var12)) {
               StreamGate var13 = var0.putAt_RefQueue();
               float var14 = NodeWalker.putAt_F_D(0.0, 0.0, var11, var12);
               double[][] var15 = new double[][]{{0.0, 0.0}, {0.0, var12 * 0.35}, {var11 * 0.35, 0.0}, {var11 * 0.35, var12 * 0.35}};

               for (int var16 = 0; var16 < var15.length; var16++) {
                  double[] var17 = var15[var16];
                  if (var16 <= 0 || var17[0] != 0.0 || var17[1] != 0.0) {
                     NodeWalker.Inner_StreamUtil var18 = NodeWalker.Inner_StreamUtil.standing(var2.getX() + 0.5 + var17[0], var3, var2.getZ() + 0.5 + var17[1]);
                     if (TableIndex.putAt_Z_TokenCore_TaskStat(var13, var18.x(), var18.y(), var18.z())) {
                        var var19 = AiSlimeBounce.putAt_List_TokenCore_TaskStat(var13, var18, var14, null, var6);

                        for (AiSlimeBounce.Inner_JsonCache var21 : var19) {
                           if (!var0.putAt_FileStore_BitCache_BlockPos(var21.cell().below()).farmland()) {
                              TableIndex.Inner_StateCache var22 = var21.plan();
                              var5.add(
                                 new ClassCache.UiHelper(
                                    ClassCache.Inner_SyncManager.SLIME_BOUNCE,
                                    var2,
                                    var21.cell(),
                                    List.of(),
                                    List.of(),
                                    var22.ticks() + 2.0 + Math.hypot(var17[0], var17[1]) * 10.0,
                                    -1.0,
                                    var22
                                 )
                              );
                           }
                        }

                        if (!var19.isEmpty()) {
                           break;
                        }
                     }
                  }
               }
            }
         }

         return var5;
      }

      private static boolean putAt_Z_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, int var2, int var3) {
         boolean var4 = var0.putAt_FileStore_TextStor instanceof RingBuffer && !var0.readEx();
         int var5 = var1.getY() - 24 - 1;

         for (int var6 = 2; var6 <= 8; var6++) {
            int var7 = var1.getX() + var2 * var6;
            int var8 = var1.getZ() + var3 * var6;

            for (int var9 = var1.getY() - 4; var9 >= var5; var9--) {
               if (var4) {
                  var9 = var0.putAt_I_I(var7, var9, var8, var5);
                  if (var9 < var5) {
                     break;
                  }
               }

               ClassCache.Inner_FileCache var10 = var0.probe(var7, var9, var8);
               if (!var10.loaded()) {
                  break;
               }

               if (var10.slime()) {
                  return true;
               }

               if (var10.top16() >= 16 && !var10.passable()) {
                  break;
               }
            }
         }

         return false;
      }

      private static ClassCache.Inner_RenderState.Inner_StreamUtil[] putAt(int var0) {
         ArrayList<ClassCache.Inner_RenderState.Inner_StreamUtil> var1 = new ArrayList<>();

         for (int var2 = -var0; var2 <= var0; var2++) {
            for (int var3 = -var0; var3 <= var0; var3++) {
               int var4 = Math.max(Math.abs(var2), Math.abs(var3));
               if (var4 != 0) {
                  double var5 = Math.hypot(var2, var3);
                  int var7 = (int)Math.round((double)var2 / var4);
                  int var8 = (int)Math.round((double)var3 / var4);
                  int var9 = 1 << putAt(var7, var8);

                  for (int var10 = 0; var10 < readEx_IArr2.length; var10++) {
                     int[] var11 = readEx_IArr2[var10];
                     if ((var11[0] * var2 + var11[1] * var3) / (Math.hypot(var11[0], var11[1]) * var5) >= 0.5) {
                        var9 |= 1 << var10;
                     }
                  }

                  double var14 = 0.5 * var5 / var4;
                  double var12 = 0.5 * (Math.abs(var2) + Math.abs(var3)) / var5;
                  var1.add(
                     new ClassCache.Inner_RenderState.Inner_StreamUtil(
                        var2, var3, var4, var7, var8, var5, var5 - 2.0 * (var14 - 0.5), var5 - 2.0 * (var12 - 0.5), var9
                     )
                  );
               }
            }
         }

         return var1.toArray(ClassCache.Inner_RenderState.Inner_StreamUtil[]::new);
      }

      static boolean fill(ClassCache.Inner_FileCache var0, ClassCache.Inner_FileCache var1) {
         return var0.loaded() && !var0.water() && !var0.lava() && !var0.climbable() && !var0.hazardFeet() && !var0.web()
            ? var1.loaded() && !var1.passable() && !var1.floorContactHazard() && !var1.lava() && !var1.water()
            : false;
      }

      private static void putAt(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         ClassCache.Inner_DataUtil var1,
         Set<Long> var2,
         BlockPos var3,
         ClassCache.Inner_FileCache var4,
         double var5,
         List<ClassCache.UiHelper> var7
      ) {
         boolean var8 = var0.fill_Z();
         if (!var8 || !var0.value(var3)) {
            boolean var9 = var1.canSprint() && var1.strategy() != ClassCache.Inner_IoWorker.read;
            boolean var10 = var0.prevEx_Z
               && (
                  var4.narrowFooting()
                     || var4.top16() > 16
                     || var5 != putAt(var3.getY(), var4)
                     || !putAt_Z_FileStore_BitCache(var0.putAt_FileStore_BitCache_BlockPos(var3), var0.putAt_FileStore_BitCache_BlockPos(var3.above()), var4)
               );
            double var11 = var4.ice() ? 0.0 : putAt_D_FileStore_IoHelper(var0, var3, var5, var9);
            double var13 = 0.0;
            double var15 = 0.0;
            if (var11 > 0.0) {
               var13 = var0.putAt_FileStore_PacketHe.to().getX() - var0.putAt_FileStore_PacketHe.from().getX();
               var15 = var0.putAt_FileStore_PacketHe.to().getZ() - var0.putAt_FileStore_PacketHe.from().getZ();
               double var17 = Math.hypot(var13, var15);
               if (var17 < 1.0E-9) {
                  var11 = 0.0;
               } else {
                  var13 /= var17;
                  var15 /= var17;
               }
            }

            int var72 = 0;

            for (int var18 = 0; var18 < readEx_IArr2.length; var18++) {
               BlockPos var19 = var3.offset(readEx_IArr2[var18][0], 0, readEx_IArr2[var18][1]);
               if (!putAt_Z_Function(var0, var19, var5) && fill(var0, var19) && fill(var0, var19.above())) {
                  var72 |= 1 << var18;
               }
            }

            if (var72 != 0 && (var8 || !var0.value(var3))) {
               if (var0.prevEx_Z && !putAt(var1, ClassCache.Inner_SyncManager.LADDER_JUMP)) {
                  putAt(var0, var2, var3, var5, var72, false, var9, var7);
               }

               boolean var73 = var0.putAt_FileStore_RingUtil.putAt_Z();
               boolean var74 = var73 && var0.putAt_FileStore_TextStor instanceof RingBuffer && !var0.readEx();
               double var20 = 2.0 * Math.max(0.0, var0.putAt_D / 0.1 - 1.0);
               int var22 = 5 + Math.min(2, (int)Math.ceil(var20 - 1.0E-5));
               ClassCache.Inner_RenderState.Inner_StreamUtil[] var23 = var73
                  ? readEx_FileStore_IoHelperArr
                  : (var22 > 5 ? fill_FileStore_IoHelperArr : putAt_FileStore_IoHelperArr);
               int var24 = var73 ? -((int)var0.putAt_FileStore_RingUtil.putAt_D) : -4;
               if (var73 && var0.closeOf_Z) {
                  var24 = -8;
               }

               if (var73 && var0.chunkAt_Z) {
                  var23 = value_FileStore_IoHelperArr;
                  var24 = Math.max(var24, -14);
               }

               int var25 = (int)Math.ceil(var5 + 1.25) - var3.getY();

               for (ClassCache.Inner_RenderState.Inner_StreamUtil var29 : var23) {
                  int var30 = var29.x();
                  int var31 = var29.z();
                  int var32 = var29.distance();
                  if ((var73 || var32 <= var22) && ((var72 & var29.exits()) != 0 || var32 < 2)) {
                     double var33 = var29.length();
                     double var35 = var0.prevEx_Z ? var29.cornerReach() : var29.reach();
                     int var37 = var32 <= var22 ? var25 : -Math.max(3, (int)Math.floor((var35 - 5.2 - var20) / 0.35));

                     for (int var38 = var37; var38 >= var24; var38--) {
                        if (var74 || var73 && var38 < -3) {
                           int var39 = var3.getY() - 1;
                           var38 = var0.putAt_I_I(var3.getX() + var30, var39 + var38, var3.getZ() + var31, var39 + var24) - var39;
                           if (var38 < var24) {
                              break;
                           }
                        }

                        BlockPos var75 = var3.offset(var30, var38, var31);
                        ClassCache.Inner_FileCache var40 = var0.putAt_FileStore_BitCache_BlockPos(var75.below());
                        if (var40.loaded() && !var40.passable()) {
                           boolean var41 = (var40.top16() > 16 || var0.prevEx_Z) && putAt_Z_Function_2(var0, var75, null);
                           if (var32 == 1 && !var41 && !var10 && (var30 == 0 || var31 == 0 || !putAt_Z_Function_2(var0, var3, var75, var5))) {
                              break;
                           }

                           ClassCache.Inner_FileCache var42 = var0.putAt_FileStore_BitCache_BlockPos(var75);
                           if (!var2.contains(var75.asLong())
                              && !var40.farmland()
                              && !var40.floorContactHazard()
                              && !var40.lava()
                              && !var42.water()
                              && !var42.climbable()
                              && !var42.web()
                              && !var42.portal()
                              && (var41 || putAt_Z_FileStore_BitCache(var42, var0.putAt_FileStore_BitCache_BlockPos(var75.above()), var40))
                              && !var0.putAt_FileStore_BitCache_BlockPos(var75.above()).web()) {
                              double var43 = putAt_D_FileStore_IoHelper(var0, var75, var40);
                              double var45 = var43 - var5;
                              if (var45 > 1.25) {
                                 if (var0.putAt_S_I(var75.getX(), var75.getY() - 1, var75.getZ()) == 1) {
                                    break;
                                 }
                              } else {
                                 if (var45 < (var0.putAt_FileStore_RingUtil.putAt_Z() ? -var0.putAt_FileStore_RingUtil.putAt_D : -3.6)) {
                                    break;
                                 }

                                 double var47 = var0.prevEx_Z && var9 ? 4.2 : 4.1;
                                 double var49 = var0.prevEx_Z && var45 <= -3.5 ? 0.45 : 0.35;
                                 if (var35 > (var45 > 0.6 ? var47 : (var45 > 0.0 ? 4.6 : 5.2)) + var20 + var49 * Math.max(0.0, -var45) + 1.0E-6) {
                                    break;
                                 }

                                 BlockPos var51 = var75.offset(-var29.stepX(), 0, -var29.stepZ());
                                 if (Math.max(Math.abs(var51.getX() - var3.getX()), Math.abs(var51.getZ() - var3.getZ())) >= 2
                                    && putAt_Z_Function(var0, var51, var43)
                                    && (!var0.putAt_Z || putAt_Z_Function(var0, var51.offset(-var29.stepX(), 0, -var29.stepZ()), var43))) {
                                    break;
                                 }

                                 double var52 = var11 <= 0.0 ? 0.0 : (var13 * var30 + var15 * var31) / var33;
                                 double var54 = var52 >= 0.65 ? var11 * var52 : 0.0;
                                 if (var54 < 0.003) {
                                    var54 = 0.0;
                                 }

                                 boolean var56 = var54 > 0.0 && var0.putAt_FileStore_PacketHe != null && var0.putAt_FileStore_PacketHe.jump() != null;
                                 double var57 = var35 + (var54 > 0.0 ? 0.2 : 0.0);
                                 double var59 = var57 - 0.5 + 0.3 + -0.12;
                                 double var61 = var57 + 0.5 - 0.3 - -0.12;
                                 double var63 = TableIndex.putAt_D_D(var45, var9, var54 > 0.0, var0.putAt_D);
                                 double var65 = var54 > 0.0 ? TableIndex.putAt_D_D(var45, var9, false, var0.putAt_D) : var63;
                                 double var67 = Math.max(var63 < 0.0 ? -1.0 : var63 + (var54 > 0.0 ? 0.25 : 1.0), var65 < 0.0 ? -1.0 : var65 + 1.0);
                                 if (var4.ice() && var0.prevEx_Z && var32 >= 4 && var9) {
                                    var67 = Math.max(var67, var63 + 1.5);
                                 }

                                 if (var67 < 0.0 || var59 > var67 || var32 >= 2 && var61 < TableIndex.putAt(var45) - 0.25) {
                                    break;
                                 }

                                 ClassCache.Inner_RenderState.Inner_DataUtil var69 = new ClassCache.Inner_RenderState.Inner_DataUtil(
                                    var3, var75, var5, var43, var54, var9, var29.stepX(), var29.stepZ(), var56
                                 );
                                 if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                                    ListPool.init("jump candidate from=" + var3 + " to=" + var75 + " approach=" + var54);
                                 }

                                 if (var0.putAt_Consumer != null) {
                                    var0.putAt_Consumer.accept(var69);
                                 } else {
                                    ClassCache.UiHelper var70 = putAt_FileStore_PacketHe_FileStore_IoHelper_2(var0, var2, var69);
                                    if (var70 != null) {
                                       var7.add(var70);
                                       ClassCache.UiHelper var71 = putAt_FileStore_PacketHe_FileStore_IoHelper(var0, var70, var2);
                                       if (var71 != null) {
                                          var7.add(var71);
                                       }
                                    }
                                 }

                                 if (!var0.prevEx_Z
                                    || var40.slabStair()
                                    || var0.putAt_S_I(var75.getX(), var75.getY() - 1, var75.getZ()) == 1
                                    || var0.putAt_FileStore_BitCache_BlockPos(var75.below(2)).passable()) {
                                    break;
                                 }
                              }
                           } else if (!var0.prevEx_Z) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static void putAt_V_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0, Set<Long> var1, BlockPos var2, double var3, List<ClassCache.UiHelper> var5
      ) {
         for (int[] var9 : putAt_IArr2) {
            int var10 = var9[0];
            int var11 = var9[1];

            for (int var12 = 2; var12 <= 4; var12++) {
               BlockPos var13 = var2.offset(var10 * var12, 0, var11 * var12);
               if (!var0.putAt_FileStore_BitCache_BlockPos(var13.offset(var10, 0, var11)).passable() && putAt_Z_FileStore_IoHelper(var0, var1, var13)) {
                  ArrayList<BlockPos> var14 = new ArrayList<>();
                  var14.add(var13);

                  for (int var15 = 0;
                     var15 < 2
                        && putAt(var0, var13.offset(var10, 0, var11))
                        && putAt(var0, var13.offset(2 * var10, 0, 2 * var11))
                        && putAt(var0, var13.offset(3 * var10, 0, 3 * var11));
                     var15++
                  ) {
                     BlockPos var16 = var13.offset(4 * var10, 0, 4 * var11);
                     if (!putAt_Z_FileStore_IoHelper(var0, var1, var16)) {
                        break;
                     }

                     var14.add(var16);
                     var13 = var16;
                  }

                  if (var14.size() != 1) {
                     TableIndex.Inner_StateCache var17 = AiWallJump.putAt(var0.putAt_RefQueue(), var2, var3, var14);
                     if (var17 != null && !(var17.maneuver().replay(var0.putAt_RefQueue(), var17.maneuver().start(), 0, var13, var3) < -0.04)) {
                        var5.add(ClassCache.UiHelper.jump(var2, var13, ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var17), var17));
                     }
                  }
               }
            }
         }
      }

      private static boolean putAt_Z_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_CachePool var0, Set<Long> var1, BlockPos var2) {
         if (!var1.contains(var2.asLong()) && var0.putAt_FileStore_BitCache_BlockPos(var2).passable()) {
            ClassCache.Inner_FileCache var3 = var0.putAt_FileStore_BitCache_BlockPos(var2.below());
            return var3.top16() == 16
               && !var3.farmland()
               && !var3.slime()
               && !var3.ice()
               && putAt_Z_FileStore_BitCache(var0.putAt_FileStore_BitCache_BlockPos(var2), var0.putAt_FileStore_BitCache_BlockPos(var2.above()), var3);
         } else {
            return false;
         }
      }

      private static boolean putAt(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1) {
         return var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ()) == 1 && var0.putAt_S_I(var1.getX(), var1.getY() + 1, var1.getZ()) == 1;
      }

      private static boolean fill(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = var0.putAt_FileStore_BitCache_BlockPos(var1);
         return var2.passable()
            ? true
            : var0.prevEx_Z
               && var2.loaded()
               && !var2.hazardFeet()
               && !var2.lava()
               && !var2.water()
               && var0.putAt_S_I(var1.getX(), var1.getY(), var1.getZ()) != 1;
      }

      private static ClassCache.UiHelper putAt_FileStore_PacketHe_FileStore_IoHelper_2(
         ClassCache.Inner_RenderState.Inner_CachePool var0, Set<Long> var1, ClassCache.Inner_RenderState.Inner_DataUtil var2
      ) {
         BlockPos var3 = var2.from();
         BlockPos var4 = var2.to();
         double var5 = var2.targetY();
         if (var0.prevEx_Z
            && var2.sprint()
            && var0.putAt_FileStore_BitCache_BlockPos(var3.below()).ice()
            && var3.distToCenterSqr(var4.getX() + 0.5, var3.getY() + 0.5, var4.getZ() + 0.5) >= 16.0) {
            TableIndex.Inner_StateCache var7 = TableIndex.putAt_IoUtil_TextState_TokenCore_TaskStat_4(var0.putAt_RefQueue(), var3, var2.sourceY(), var4, var5, true);
            if (var7 != null && !readEx(var0, var3, var4) && (!ClassCache.Inner_CachePool.putAt() || !fill(var0, var3, var4))) {
               return ClassCache.UiHelper.jump(var3, var4, ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var7), var7);
            }
         }

         TableIndex.Inner_StateCache var25 = var0.putAt(var3, var2.sourceY(), var4, var5, var2.approach(), var2.sprint());
         BlockPos var8 = var4;
         if (var25 == null
            && var0.prevEx_Z
            && var2.sprint()
            && var3.distSqr(var8) >= 9.0
            && var3.distSqr(var8) <= 29.0
            && var0.putAt_FileStore_BitCache_BlockPos(var3.below()).top16() <= 3) {
            var25 = var0.putAt_IoUtil_TextState_BlockPos(
               var3,
               var8,
               -9223372036854775807L,
               true,
               () -> TableIndex.putAt_IoUtil_TextState_TokenCore_TaskStat_6(var0.putAt_RefQueue(), var3, var2.sourceY(), var8, var5)
            );
         }

         if (var25 == null
            && var0.prevEx_Z
            && var3.distSqr(var8) <= 18.0
            && (
               !var0.putAt_FileStore_BitCache_BlockPos(var8.above()).passable()
                  || var0.putAt_FileStore_BitCache_BlockPos(var8.below()).slabStair() && !var0.putAt_FileStore_BitCache_BlockPos(var8.above(2)).passable()
            )) {
            var25 = var0.putAt_IoUtil_TextState_BlockPos(
               var3,
               var8,
               -9223372036854775806L,
               var2.sprint(),
               () -> PoolEntry.roofedLanding(
                  var0.putAt_RefQueue(), var3, var2.sourceY(), var8, var5, var2.sprint(), var0.putAt_FileStore_BitCache_BlockPos(var3.below()).slabStair()
               )
            );
         }

         if (var25 == null
            && var0.prevEx_Z
            && var3.distSqr(var8) <= 18.0
            && var0.putAt_FileStore_BitCache_BlockPos(var3.below()).slabStair()
            && (
               !var0.putAt_FileStore_BitCache_BlockPos(var3.offset(var2.dx(), 0, var2.dz())).passable()
                     && (!var0.putAt_FileStore_BitCache_BlockPos(var8).passable() || !var0.putAt_FileStore_BitCache_BlockPos(var8.above()).passable())
                  || !var0.putAt_FileStore_BitCache_BlockPos(var3.above()).passable() && var5 > var2.sourceY() + 0.4
            )) {
            var25 = var0.putAt_IoUtil_TextState_BlockPos(
               var3,
               var8,
               -9223372036854775805L,
               var2.sprint(),
               () -> PoolEntry.cornerDeparture(var0.putAt_RefQueue(), var3, var2.sourceY(), var8, var5, var2.sprint())
            );
         }

         boolean var9 = var25 != null;
         boolean var10 = false;
         if (var0.prevEx_Z && !var0.prev_Z && var2.sprint() && Math.abs(var2.sourceY() - var5) <= 1.0E-6 && putAt_Z_Function_3(var0, var3, var4)) {
            TableIndex.Inner_StateCache var11 = AiWallJump.putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0.putAt_RefQueue(), var3, var5, var4, var2.approach());
            if (var11 != null
               && (var25 == null || var11.ticks() <= var25.ticks() + 4)
               && var11.maneuver().replay(var0.putAt_RefQueue(), var11.maneuver().start(), 0, var4, var5) >= -0.04) {
               var25 = var11;
               var9 = false;
               var10 = true;
               if (ListPool.init_Z()) {
                  ListPool.init("proved sprint shoulder from=" + var3.toShortString() + " to=" + var4.toShortString() + " ticks=" + var11.ticks());
               }
            }
         }

         double var26 = 0.0;
         if (var2.landedAhead()
            && var25 != null
            && var25.maneuver() == null
            && var0.putAt_FileStore_PacketHe != null
            && var0.putAt_FileStore_PacketHe.jump() != null) {
            TableIndex.Inner_StateCache var13 = var0.putAt_FileStore_PacketHe.jump();
            double var14 = Math.toRadians(var13.yaw());
            NodeWalker.Inner_StreamUtil var16 = new NodeWalker.Inner_StreamUtil(
               var13.landX(),
               var2.sourceY(),
               var13.landZ(),
               -Math.sin(var14) * var13.landSpeed(),
               -0.0784,
               Math.cos(var14) * var13.landSpeed(),
               true,
               0,
               false,
               false
            );
            if (TableIndex.putAt_IoUtil_IoPool_TokenCore_TaskStat(var0.putAt_RefQueue(), var16, var25.yaw(), var2.sprint(), var4, var5, 6).margin() < -0.04) {
               var25 = null;
            }
         }

         if (var9 && var2.approach() > 0.0 && (var25 == null || var25.maneuver() == null && var25.margin() < 0.14)) {
            TableIndex.Inner_StateCache var27 = var0.putAt(var3, var2.sourceY(), var4, var5, 0.0, var2.sprint());
            if (var27 != null && var27.margin() >= -0.04) {
               boolean var30 = var25 == null
                  || var25.margin() < -0.04
                  || var27.margin() > var25.margin() + 0.04
                  || var27.margin() >= var25.margin() && var27.ticks() + 4 < var25.ticks();
               if (var30) {
                  var25 = var27;
                  var26 = 4.0;
               }
            }
         }

         if (var25 != null && var2.approach() > 0.0 && !var25.running()) {
            var26 = Math.max(var26, 4.0);
         }

         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            ListPool.init("jump proof from=" + var3 + " to=" + var4 + " plan=" + var25);
         }

         if (var25 == null) {
            return null;
         }

         if (var25.margin() < 0.1) {
            BlockPos var28 = var2.deeper();
            ClassCache.Inner_FileCache var31 = var0.putAt_FileStore_BitCache_BlockPos(var28.below());
            boolean var15 = var25.margin() < -0.04
               || var2.dx() == 0
               || var2.dz() == 0
               || putAt_Z_Function(var0, var4.offset(var2.dx(), 0, 0), var5)
                  && Math.abs(putAt(var4.getY(), var0.putAt_FileStore_BitCache_BlockPos(var4.offset(var2.dx(), -1, 0))) - var5) < 1.0E-6
               || putAt_Z_Function(var0, var4.offset(0, 0, var2.dz()), var5)
                  && Math.abs(putAt(var4.getY(), var0.putAt_FileStore_BitCache_BlockPos(var4.offset(0, -1, var2.dz()))) - var5) < 1.0E-6;
            if (var15 && !var1.contains(var28.asLong()) && putAt_Z_Function(var0, var28, var5) && Math.abs(putAt(var28.getY(), var31) - var5) < 1.0E-6) {
               TableIndex.Inner_StateCache var34 = var10
                  ? AiWallJump.putAt_IoUtil_TextState_TokenCore_TaskStat_3(var0.putAt_RefQueue(), var3, var5, var28, var2.approach())
                  : var0.putAt(var3, var2.sourceY(), var28, var5, var25.approach(), var2.sprint());
               if (var34 != null && var34.margin() > var25.margin()) {
                  var25 = var34;
                  var4 = var28;
               }
            }
         }

         boolean var29 = fill(var0, var3, var4);
         boolean var32 = readEx(var0, var3, var4);
         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            ListPool.init(
               "jump decision from="
                  + var3
                  + " to="
                  + var4
                  + " margin="
                  + var25.margin()
                  + " hard="
                  + var29
                  + " cautious="
                  + ClassCache.Inner_CachePool.putAt()
                  + " lava="
                  + var32
            );
         }

         if (!(var25.margin() < -0.04) && (!var29 || !ClassCache.Inner_CachePool.putAt()) && !var32) {
            double var33 = var3.distToCenterSqr(var4.getX() + 0.5, var3.getY() + 0.5, var4.getZ() + 0.5);
            boolean var17 = var0.putAt_D > 0.125;
            if (var17 && var0.prev_Z && var25.maneuver() == null && var25.running() && var5 - var2.sourceY() > 0.6 && var33 >= 23.04 && var25.margin() < 0.16) {
               TableIndex.Inner_StateCache var18 = var0.putAt(var3, var2.sourceY(), var4, var5, 0.0, var2.sprint());
               if (var18 == null || var18.margin() < -0.04) {
                  return null;
               }
            }

            boolean var35 = var17 ? var33 > 29.160000000000004 : var33 >= 20.25 && var5 - var2.sourceY() > 0.6;
            ClassCache.Inner_FileCache var19 = var0.putAt_FileStore_BitCache_BlockPos(var3.below());
            boolean var20 = var25.runTicks() >= 3 && var33 >= 16.0 && var5 - var2.sourceY() >= 0.75 && (var19.narrowFooting() || var19.top16() > 16);
            if (var0.putAt_D > 0.100001
               && var25.maneuver() == null
               && (var20 || var25.runTicks() >= 6 || var0.prev_Z && var35)
               && !TableIndex.putAt_Z_TokenCore_TaskStat(var0.putAt_RefQueue(), var25, var2.sourceY())) {
               return null;
            }

            double var21 = var29 ? 20.0 : 0.0;
            if (var29 && (var0.prev_Z || var0.putAt_FileStore_RingUtil != ClassCache.Inner_NetHelper.init)) {
               double var23 = 0.12;
               var21 *= Math.clamp((var23 - var25.margin()) / (var23 - -0.04), 0.0, 1.0);
            }

            if (var0.putAt_FileStore_RingUtil != ClassCache.Inner_NetHelper.init) {
               ClassCache.Inner_FileCache var36 = var0.putAt_FileStore_BitCache_BlockPos(var4.below());
               if (var36.narrowFooting() || var36.top16() > 16) {
                  var26 += 3.0;
               }

               if (putAt(var25)) {
                  var26 += 3.0;
               }
            }

            double var37 = ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var25);
            if (var25.maneuver() != null && var25.backup() == 0.0 && var33 <= 2.5 && !putAt(var25)) {
               var37 -= 10.0;
            }

            return ClassCache.UiHelper.jump(
               var3, var4, var37 + var26 + (var0.putAt_FileStore_BitCache_BlockPos(var4).thorny() ? 25.0 : 0.0) + var21, var25
            );
         } else {
            return null;
         }
      }

      static boolean putAt_Z_Function_3(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.abs(var3) + Math.abs(var4);
         if ((var3 == 0 || var4 == 0) && var5 >= 2 && var5 <= 4) {
            int var6 = Integer.signum(var3);
            int var7 = Integer.signum(var4);
            BlockPos var8 = var1.offset(var6, 0, var7);
            if (!((ClassCache.Inner_FileCache)var0.apply(var8)).passable() && !((ClassCache.Inner_FileCache)var0.apply(var8.above())).passable()) {
               BlockPos var9 = var1.offset(var6 * 3, 0, var7 * 3);
               return var5 < 4 || ((ClassCache.Inner_FileCache)var0.apply(var9)).passable() && ((ClassCache.Inner_FileCache)var0.apply(var9.above())).passable();
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static ClassCache.UiHelper putAt_FileStore_PacketHe_FileStore_IoHelper(
         ClassCache.Inner_RenderState.Inner_CachePool var0, ClassCache.UiHelper var1, Set<Long> var2
      ) {
         TableIndex.Inner_StateCache var3 = var1.jump();
         if (var0.prevEx_Z && !var3.running() && var3.maneuver() == null && var3.sprint() && var3.coastAfter() != 2147483647) {
            BlockPos var4 = var1.from();
            BlockPos var5 = var1.to();
            ClassCache.Inner_FileCache var6 = var0.putAt_FileStore_BitCache_BlockPos(var5.below());
            if (var6.top16() == 16 && var0.putAt_S_I(var5.getX(), var5.getY() - 1, var5.getZ()) != 1) {
               int var7 = var5.getX() - var4.getX();
               int var8 = var5.getZ() - var4.getZ();
               if (Math.max(Math.abs(var7), Math.abs(var8)) < 3) {
                  return null;
               }

               int var9 = Math.abs(var7) > Math.abs(var8) ? Integer.signum(var7) : 0;
               int var10 = var9 == 0 ? Integer.signum(var8) : 0;
               if (putAt_Z_Function(var0, var5.offset(var9, 0, var10), var3.landY())) {
                  return null;
               }

               double var11 = putAt(var4.getY(), var0.putAt_FileStore_BitCache_BlockPos(var4.below()));
               StreamGate var13 = var0.putAt_RefQueue();
               TableIndex.Inner_StateCache var14 = TableIndex.putAt_IoUtil_TextState_TokenCore_TaskStat(var13, var3, var5, var11, var3.landY());
               if (var14 == null) {
                  return null;
               }

               for (int var15 = 4; var15 <= 7; var15++) {
                  for (int var16 = -1; var16 <= 1; var16++) {
                     int var17 = var15 <= 5 ? -3 : -5;
                     int var18 = var15 <= 5 ? 0 : -3;

                     for (int var19 = var17; var19 <= var18; var19++) {
                        BlockPos var20 = var5.offset(var9 * var15 + var10 * var16, var19, var10 * var15 + var9 * var16);
                        if (!var2.contains(var20.asLong())) {
                           ClassCache.Inner_FileCache var21 = var0.putAt_FileStore_BitCache_BlockPos(var20.below());
                           if ((
                                 putAt_Z_FileStore_BitCache(
                                       var0.putAt_FileStore_BitCache_BlockPos(var20), var0.putAt_FileStore_BitCache_BlockPos(var20.above()), var21
                                    )
                                    || putAt_Z_Function_2(var0, var20, null)
                              )
                              && !var21.farmland()
                              && !var21.slime()
                              && var0.putAt_S_I(var20.getX(), var20.getY() - 1, var20.getZ()) != 1) {
                              double var22 = putAt(var20.getY(), var21);
                              TableIndex.Inner_StateCache var24 = TableIndex.putAt_IoUtil_TextState_TokenCore_TaskStat_2(var13, var14, var20, var22, var14.sprint());
                              if (var24 != null
                                 && !(var24.margin() < 0.03)
                                 && !readEx(var0, var5, var20)
                                 && (!ClassCache.Inner_CachePool.putAt() || !fill(var0, var5, var20))) {
                                 var var25 = new ArrayList<>(var14.maneuver().inputs());
                                 var25.addAll(var24.maneuver().inputs());
                                 PoolEntry var26 = new PoolEntry(
                                    var25, var14.maneuver().start(), var24.maneuver().landing(), var24.margin(), var14.maneuver().takeoffTick()
                                 );
                                 if (!(var26.replay(var13, var26.start(), 0, var20, var22) < -0.04)) {
                                    TableIndex.Inner_StateCache var27 = new TableIndex.Inner_StateCache(
                                       var14.yaw(),
                                       var14.runTicks(),
                                       var14.sprint(),
                                       2147483647,
                                       var25.size() - var14.runTicks(),
                                       var3.startX(),
                                       var3.startZ(),
                                       var24.landX(),
                                       var22,
                                       var24.landZ(),
                                       var24.landSpeed(),
                                       var24.margin(),
                                       var3.backup(),
                                       0.0,
                                       var26
                                    );
                                    return ClassCache.UiHelper.jump(
                                       var4, var20, var1.costTicks() + ClassCache.Inner_CachePool.putAt_D_IoUtil_TextState(var24), var27
                                    );
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      private static boolean putAt(TableIndex.Inner_StateCache var0) {
         return var0.maneuver() != null && var0.maneuver().inputs().stream().anyMatch(NodeWalker.Inner_DataUtil::sneak);
      }

      static boolean fill(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.max(Math.abs(var3), Math.abs(var4));
         int var6 = Math.max(var1.getY(), var2.getY());

         for (int var7 = 1; var7 < var5; var7++) {
            BlockPos var8 = new BlockPos(
               var1.getX() + (int)Math.round((double)var3 * var7 / var5), var6, var1.getZ() + (int)Math.round((double)var4 * var7 / var5)
            );
            if (prevEx(var0, var8)) {
               return true;
            }
         }

         for (int[] var10 : readEx_IArr2) {
            if (prevEx(var0, var2.offset(var10[0], 0, var10[1]))) {
               return true;
            }
         }

         return false;
      }

      static boolean readEx(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         int var3 = var2.getX() - var1.getX();
         int var4 = var2.getZ() - var1.getZ();
         int var5 = Math.max(Math.abs(var3), Math.abs(var4));
         int var6 = Math.max(var1.getY(), var2.getY());

         for (int var7 = 1; var7 < var5; var7++) {
            BlockPos var8 = new BlockPos(
               var1.getX() + (int)Math.round((double)var3 * var7 / var5), var6, var1.getZ() + (int)Math.round((double)var4 * var7 / var5)
            );
            if (value(var0, var8)) {
               return true;
            }
         }

         for (int[] var10 : readEx_IArr2) {
            if (value(var0, var2.offset(var10[0], 0, var10[1]))) {
               return true;
            }
         }

         return false;
      }

      private static boolean value(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         for (int var2 = 0; var2 <= 12; var2++) {
            ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.below(var2));
            if (!var3.loaded()) {
               return false;
            }

            if (var3.lava()) {
               return true;
            }

            if (!var3.passable() || var3.water()) {
               return false;
            }
         }

         return false;
      }

      private static boolean prevEx(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         for (int var2 = 0; var2 <= 4; var2++) {
            ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.below(var2));
            if (!var3.loaded() || !var3.passable() || var3.water()) {
               return false;
            }
         }

         return true;
      }

      private static int putAt(int var0, int var1) {
         for (int var2 = 0; var2 < readEx_IArr2.length; var2++) {
            if (readEx_IArr2[var2][0] == var0 && readEx_IArr2[var2][1] == var1) {
               return var2;
            }
         }

         throw new IllegalArgumentException("not a neighbour: " + var0 + "," + var1);
      }

      static boolean putAt_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, double var2) {
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var1);
         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         return putAt_Z_FileStore_BitCache(var4, (ClassCache.Inner_FileCache)var0.apply(var1.above()), var5) && !var4.water()
            ? Math.abs(putAt(var1.getY(), var5) - var2) <= 0.562500001
            : false;
      }

      static boolean putAt_Z_Function_2(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2, double var3) {
         int var5 = var2.getX() - var1.getX();
         int var6 = var2.getZ() - var1.getZ();
         int var7 = Math.max(Math.abs(var5), Math.abs(var6));
         if (var7 != 1) {
            for (int var8 = 1; var8 < var7; var8++) {
               int var9 = var1.getX() + (int)Math.round((double)var5 * var8 / var7);
               int var10 = var1.getZ() + (int)Math.round((double)var6 * var8 / var7);
               if (!putAt_Z_Function(var0, new BlockPos(var9, var1.getY(), var10), var3)) {
                  return true;
               }
            }

            return false;
         } else {
            return var5 != 0 && var6 != 0
               ? !putAt_Z_Function(var0, var1.offset(var5, 0, 0), var3) || !putAt_Z_Function(var0, var1.offset(0, 0, var6), var3)
               : false;
         }
      }

      private static double putAt_D_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_CachePool var0, BlockPos var1, double var2, boolean var4) {
         if (var0.putAt_FileStore_PacketHe == null) {
            return 0.0;
         }

         double var10000;
         switch (var0.putAt_FileStore_PacketHe.type()) {
            case init:
            case load:
            case read:
            case write:
               StreamGate var5 = var0.putAt_RefQueue();
               int var6 = (int)Math.floor(var2 - 0.5000001);
               double var7 = var5.putAt_D_I(var1.getX(), var6, var1.getZ());
               double var9 = 0.91 * var7 * var5.fill_D(var1.getX(), var6, var1.getZ());
               boolean var11 = var0.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.read
                  || var0.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.write;
               double var12 = var5.movementSpeed() * (var11 ? 1.3 : 1.0) * (0.21600002 / (var7 * var7 * var7)) * 0.98;
               double var14 = var12 * var9 / (1.0 - var9);
               var10000 = var7 >= 0.9 ? var14 * (1.0 - Math.pow(var9, Math.max(1.0, var0.putAt_FileStore_PacketHe.costTicks()))) : var14;
               break;
            default:
               var10000 = putAt(var0.putAt_FileStore_PacketHe, var4);
         }

         return var10000;
      }

      static double putAt(ClassCache.UiHelper var0) {
         return putAt(var0, false);
      }

      static double putAt(ClassCache.UiHelper var0, boolean var1) {
         if (var0 == null) {
            return 0.0;
         }

         double var10000;
         switch (var0.type()) {
            case init:
            case load:
            case update:
            case flush:
               var10000 = 0.2;
               break;
            case read:
            case write:
            case put:
            case add:
            case clear:
            case reset:
               var10000 = 0.28;
               break;
            case sync:
               var10000 = var1 ? 0.28 : 0.2;
               break;
            case close:
            case open:
            case poll:
            case push:
            case pop:
            case tick:
            case run:
            case get:
            case set:
            default:
               var10000 = 0.0;
               break;
            case start:
               var10000 = var0.jump() == null ? 0.2 : Math.min(0.28, var0.jump().landSpeed());
         }

         return var10000;
      }

      private static void putAt_V_FileStore_IoHelper_2(
         ClassCache.Inner_RenderState.Inner_CachePool var0,
         ClassCache.Inner_DataUtil var1,
         Set<Long> var2,
         BlockPos var3,
         ClassCache.Inner_FileCache var4,
         List<ClassCache.UiHelper> var5
      ) {
         for (int[] var9 : putAt_IArr2) {
            BlockPos var10 = var3.offset(var9[0], 0, var9[1]);
            if (!var2.contains(var10.asLong())) {
               ClassCache.Inner_FileCache var11 = var0.putAt_FileStore_BitCache_BlockPos(var10);
               ClassCache.Inner_FileCache var12 = var0.putAt_FileStore_BitCache_BlockPos(var10.above());
               ClassCache.Inner_FileCache var13 = var0.putAt_FileStore_BitCache_BlockPos(var10.below());
               if (var13.loaded()
                  && !var13.passable()
                  && !var13.hazardFloor()
                  && !var13.lava()
                  && !var11.hazardFeet()
                  && !var11.lava()
                  && !var12.hazardFeet()
                  && !var12.lava()) {
                  boolean var14 = var11.diggable();
                  boolean var15 = var12.diggable();
                  if ((var14 || var11.passable()) && (var15 || var12.passable())) {
                     ArrayList var16 = new ArrayList(3);
                     double var17 = 0.0;
                     double var19 = 0.0;
                     if (!var4.passable() && var4.diggable()) {
                        var16.add(var3);
                        var17 += var4.breakTicks() + var1.breakReluctance() + var4.destructionPenalty();
                        var19 += var4.breakTicks();
                     }

                     if (var14) {
                        var16.add(var10);
                        var17 += var11.breakTicks() + var1.breakReluctance() + var11.destructionPenalty();
                        var19 += var11.breakTicks();
                     }

                     if (var15) {
                        var16.add(var10.above());
                        var17 += var12.breakTicks() + var1.breakReluctance() + var12.destructionPenalty();
                        var19 += var12.breakTicks();
                     }

                     if (var1.strategy() == ClassCache.Inner_IoWorker.read) {
                        var17 *= 2.0;
                     }

                     var5.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.pop, var3, var10, var16, List.of(), ClassCache.Inner_CachePool.value_D_D(var17), var19
                        )
                     );
                  }
               }
            }
         }
      }

      static double putAt_D_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2, boolean var3) {
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var2);
         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var2.above());
         double var6 = !var4.portal() && !var5.portal() ? 0.0 : 400.0;
         boolean var8 = var4.web() || var5.web();
         if (!var8) {
            return var6;
         }

         boolean var9 = ((ClassCache.Inner_FileCache)var0.apply(var1)).web() || ((ClassCache.Inner_FileCache)var0.apply(var1.above())).web();
         double var10 = var9 ? 1.0 : 1.6;
         double var12 = Math.hypot(var2.getX() - var1.getX(), var2.getZ() - var1.getZ());
         return var6 + var10 * var12 * (var3 ? 32.0 : 41.0);
      }

      static double putAt_D(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         return !((ClassCache.Inner_FileCache)var0.apply(var1)).portal() && !((ClassCache.Inner_FileCache)var0.apply(var1.above())).portal() ? 0.0 : 400.0;
      }

      static double putAt_D_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, int var1, int var2, double var3, double var5) {
         double var7 = 0.0;
         double var9 = var5;

         for (int var11 = (int)Math.floor(var5); var11 <= (int)Math.floor(var3) + 1; var11++) {
            if (((ClassCache.Inner_FileCache)var0.apply(new BlockPos(var1, var11, var2))).web()) {
               double var12 = Math.max(Math.max(var11 - 1.8, var5), var9);
               double var14 = Math.min(var11 + 1.0, var3);
               if (var14 > var12) {
                  var7 += var14 - var12;
                  var9 = var14;
               }
            }
         }

         return var7;
      }

      static boolean putAt_Z(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = (ClassCache.Inner_FileCache)var0.apply(var1);
         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.above());
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (!var2.passable() || !var3.passable() || var2.hazardFeet() || var2.lava()) {
            return false;
         }

         if (var2.contactHazard() || var3.contactHazard()) {
            return false;
         }

         if (var2.web() || var3.web() || var2.portal() || var3.portal()) {
            return false;
         }

         if (var2.water()) {
            return true;
         }

         if (!var4.loaded() || var4.lava() || var4.hazardFloor()) {
            return false;
         }

         if (!var4.passable()) {
            return true;
         }

         if (var4.water()) {
            return true;
         }

         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var1.below(2));
         return var5.loaded() && !var5.passable() && !var5.lava() && !var5.hazardFloor();
      }

      static boolean putAt_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, int var2, int var3) {
         return putAt_Z_Function(var0, var1, var2, var3, 1);
      }

      public static boolean putAt_Z_TokenCore_TaskStat(NodeWalker.Inner_CachePool var0, double var1, double var3, BlockPos var5) {
         double var6 = 0.4;
         AABB var8 = new AABB(var1 - var6, var5.getY(), var3 - var6, var1 + var6, var5.getY() + 2, var3 + var6);

         for (AABB var10 : var0.collisions(var8)) {
            if (var10.intersects(var8)) {
               return false;
            }
         }

         return true;
      }

      public static boolean putAt_Z_TokenCore_TaskStat(NodeWalker.Inner_CachePool var0, List<NodeWalker.Inner_StreamUtil> var1, int var2) {
         double var3 = 0.4;

         for (NodeWalker.Inner_StreamUtil var6 : var1) {
            if (!(var6.y() > var2 + 3) && !(var6.y() < var2)) {
               AABB var7 = new AABB(var6.x() - var3, var6.y(), var6.z() - var3, var6.x() + var3, var6.y() + 1.8, var6.z() + var3);

               for (AABB var9 : var0.collisions(var7)) {
                  if (var9.intersects(var7)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      public static boolean value(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var2.below());
         return !var3.passable() && !var4.passable() ? putAt(var1.getY(), var3) - putAt(var2.getY(), var4) <= 0.562501 : false;
      }

      static boolean putAt_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, int var2, int var3, int var4) {
         return fill(var0, var1, var2, var3, var4, false);
      }

      private static boolean fill(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, int var2, int var3, int var4, boolean var5) {
         BlockPos var6 = var1.offset(var2, 0, var3);
         ClassCache.Inner_FileCache var7 = (ClassCache.Inner_FileCache)var0.apply(var6);
         if (var7.loaded() && !var7.lava() && !var7.hazardFeet()) {
            if (!var7.web() && !((ClassCache.Inner_FileCache)var0.apply(var6.above())).web()) {
               if (!var7.passable()) {
                  return var4 < 2 || !((ClassCache.Inner_FileCache)var0.apply(var6.above())).passable();
               }

               if (var7.water()) {
                  return true;
               }

               int var8 = 0;

               while (var8 <= 2) {
                  BlockPos var9 = var6.below(var8);
                  ClassCache.Inner_FileCache var10 = (ClassCache.Inner_FileCache)var0.apply(var9);
                  if (var10.loaded() && !var10.lava() && !var10.hazardFeet() && !var10.web()) {
                     if (var8 > 0 && var10.water()) {
                        return true;
                     }

                     ClassCache.Inner_FileCache var11 = (ClassCache.Inner_FileCache)var0.apply(var9.below());
                     if (var11.loaded() && !var11.lava() && !var11.hazardFloor() && (!var11.farmland() || var5)) {
                        if (var11.passable() && !var11.water()) {
                           var8++;
                           continue;
                        }

                        return putAt_Z_FileStore_BitCache(var10, (ClassCache.Inner_FileCache)var0.apply(var9.above()), var11);
                     }

                     return false;
                  }

                  return false;
               }

               return false;
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static boolean putAt(ClassCache.Inner_DataUtil var0, ClassCache.Inner_SyncManager var1) {
         return var0.disabledTypes().contains(var1);
      }

      private static boolean fill(ClassCache.Inner_FileCache var0) {
         return var0.loaded() && !var0.passable() && !var0.water() && !var0.lava() && !var0.hazardFloor();
      }

      static boolean prevEx(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1);
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (var3.water() || var3.lava() || var3.climbable() || var3.hazardFeet() || var3.web() || ((ClassCache.Inner_FileCache)var0.apply(var1.above())).web()) {
            return false;
         }

         if (var4.passable() || var4.farmland() || var4.hazardFloor() || var4.lava() || var4.soulSand()) {
            return false;
         }

         if (!var4.honey() && !var4.slime()) {
            ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var2);
            ClassCache.Inner_FileCache var6 = (ClassCache.Inner_FileCache)var0.apply(var2.below());
            if (!var6.honey() && !var6.slime()) {
               if (!var5.water() && !var5.thorny() && !var5.web() && !((ClassCache.Inner_FileCache)var0.apply(var2.above())).web() && !var6.farmland()) {
                  int var7 = Integer.signum(var2.getX() - var1.getX());
                  int var8 = Integer.signum(var2.getZ() - var1.getZ());
                  int var9 = 0;

                  while (var9 <= 4) {
                     BlockPos var10 = var1.offset(var7 * var9, 0, var8 * var9);
                     if (!((ClassCache.Inner_FileCache)var0.apply(var10)).web()
                        && !((ClassCache.Inner_FileCache)var0.apply(var10.above())).web()
                        && !((ClassCache.Inner_FileCache)var0.apply(var10.above(2))).web()) {
                        if (((ClassCache.Inner_FileCache)var0.apply(var10.below())).farmland()) {
                           return false;
                        }

                        for (byte var11 = -1; var11 <= 1; var11 += 2) {
                           if (((ClassCache.Inner_FileCache)var0.apply(var10.offset(var8 * var11, -1, var7 * var11))).farmland()) {
                              return false;
                           }
                        }

                        if (!((ClassCache.Inner_FileCache)var0.apply(var10)).portal() && !((ClassCache.Inner_FileCache)var0.apply(var10.above())).portal()) {
                           var9++;
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }

                  if (putAt(var1.getY(), var4) != putAt(var2.getY(), var6)) {
                     return false;
                  } else {
                     return !putAt_Z_FileStore_BitCache(var5, (ClassCache.Inner_FileCache)var0.apply(var2.above()), var6)
                        ? false
                        : putAt((ClassCache.Inner_FileCache)var0.apply(var1.above(2)), (ClassCache.Inner_FileCache)var0.apply(var1.above(3)))
                           || prev(var0, var1, var2);
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      static boolean prev(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2) {
         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.above(2));
         ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var2.above(2));
         return var3.loaded() && var4.loaded() && !var3.passable() && !var4.passable() && !var3.hazardFeet() && !var4.hazardFeet();
      }

      static boolean putAt_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, int var2, int var3, int var4, boolean var5) {
         ClassCache.Inner_FileCache var6 = (ClassCache.Inner_FileCache)var0.apply(var1);
         ClassCache.Inner_FileCache var7 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (!var6.loaded() || var6.water() || var6.lava() || var6.climbable() || var6.hazardFeet() || var6.feetTrigger() || var6.web()) {
            return false;
         }

         if (var7.ice()) {
            return false;
         }

         if (!var7.passable() && var7.top16() == 16 && !var7.slabStair() && !var7.hazardFloor()) {
            for (int var8 = 0; var8 <= var4 + 1; var8++) {
               int var9 = var1.getY() + 1;
               int var10 = var1.getY() + 2;
               if (var5 && var8 == var4 + 1) {
                  var9++;
                  var10++;
               } else if (var5 && var8 > 0) {
                  var10++;
               }

               for (int var11 = var9; var11 <= var10; var11++) {
                  ClassCache.Inner_FileCache var12 = (ClassCache.Inner_FileCache)var0.apply(
                     new BlockPos(var1.getX() + var2 * var8, var11, var1.getZ() + var3 * var8)
                  );
                  if (!var12.passable() || var12.lava() || var12.water() || var12.hazardFeet() || var12.climbable() || var12.web()) {
                     return false;
                  }
               }
            }

            boolean var13 = false;

            for (int var14 = 1; var14 <= var4; var14++) {
               BlockPos var16 = new BlockPos(var1.getX() + var2 * var14, var1.getY(), var1.getZ() + var3 * var14);
               ClassCache.Inner_FileCache var18 = (ClassCache.Inner_FileCache)var0.apply(var16);
               if (!var18.passable() || var18.lava() || var18.hazardFeet() || var18.feetTrigger() || var18.climbable() || var18.web()) {
                  return false;
               }

               if (((ClassCache.Inner_FileCache)var0.apply(var16.below())).passable()) {
                  var13 = true;
               }
            }

            if (!var13) {
               return false;
            } else {
               BlockPos var15 = new BlockPos(var1.getX() + var2 * (var4 + 1), var1.getY() + (var5 ? 1 : 0), var1.getZ() + var3 * (var4 + 1));
               ClassCache.Inner_FileCache var17 = (ClassCache.Inner_FileCache)var0.apply(var15.below());
               if (!putAt_Z_FileStore_BitCache((ClassCache.Inner_FileCache)var0.apply(var15), (ClassCache.Inner_FileCache)var0.apply(var15.above()), var17)
                  || var17.farmland()
                  || var17.ice()
                  || var17.slime()) {
                  return false;
               } else {
                  return ((ClassCache.Inner_FileCache)var0.apply(var15)).web()
                     ? false
                     : fill(var0, var15.offset(var2, 0, var3)) && fill(var0, var15.offset(2 * var2, 0, 2 * var3));
               }
            }
         } else {
            return false;
         }
      }

      static int flowState(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = (ClassCache.Inner_FileCache)var0.apply(var1);
         if (var2.loaded() && !var2.lava() && !var2.hazardFeet()) {
            if (!var2.passable()) {
               return 0;
            }

            int var3 = 1;

            while (var3 <= 32) {
               ClassCache.Inner_FileCache var4 = (ClassCache.Inner_FileCache)var0.apply(var1.below(var3));
               if (!var4.loaded()) {
                  return var3;
               }

               if (!var4.lava() && !var4.hazardFloor() && !var4.hazardFeet()) {
                  if (!var4.water() && var4.passable()) {
                     var3++;
                     continue;
                  }

                  return var3 - 1;
               }

               return -1;
            }

            return 32;
         } else {
            return -1;
         }
      }

      static boolean fill(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = (ClassCache.Inner_FileCache)var0.apply(var1);
         if (!var2.loaded() || var2.lava() || var2.hazardFeet()) {
            return false;
         }

         if (!var2.passable()) {
            return true;
         }

         ClassCache.Inner_FileCache var3 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (var3.loaded() && !var3.lava() && !var3.hazardFloor()) {
            if (putAt_Z_FileStore_BitCache(var2, (ClassCache.Inner_FileCache)var0.apply(var1.above()), var3)) {
               return true;
            }

            for (int var4 = 1; var4 <= 4; var4++) {
               ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var1.below(var4));
               if (!var5.loaded() || var5.lava() || var5.hazardFloor()) {
                  return false;
               }

               if (var5.water()) {
                  return true;
               }

               if (!var5.passable()) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      static boolean readEx(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
         if (var2.loaded() && !var2.passable()) {
            return true;
         }

         for (int[] var6 : putAt_IArr2) {
            ClassCache.Inner_FileCache var7 = (ClassCache.Inner_FileCache)var0.apply(var1.offset(var6[0], 0, var6[1]));
            if (var7.loaded() && !var7.passable()) {
               return true;
            }
         }

         return false;
      }

      static boolean putAt_Z_Function_3(Function<BlockPos, ClassCache.Inner_FileCache> var0, BlockPos var1, BlockPos var2, ClassCache.UiHelper var3) {
         BlockPos var4 = var2.below();
         ClassCache.Inner_FileCache var5 = (ClassCache.Inner_FileCache)var0.apply(var4.below());
         if (var5.loaded() && !var5.passable()) {
            return true;
         }

         if (var3 != null && var3.type() == ClassCache.Inner_SyncManager.run) {
            return true;
         }

         if (putAt_Z) {
            ClassCache.Inner_FileCache var6 = (ClassCache.Inner_FileCache)var0.apply(var1.below());
            if (var6.loaded() && !var6.passable() && var6.top16() >= 16 && !var6.hazardFloor()) {
               return true;
            }
         }

         for (int[] var9 : putAt_IArr2) {
            BlockPos var10 = var4.offset(var9[0], 0, var9[1]);
            if (!var10.equals(var1.below())
               && ((ClassCache.Inner_FileCache)var0.apply(var10)).loaded()
               && !((ClassCache.Inner_FileCache)var0.apply(var10)).passable()) {
               return true;
            }
         }

         return false;
      }

      private record Inner_DataUtil(
         BlockPos from, BlockPos to, double sourceY, double targetY, double approach, boolean sprint, int dx, int dz, boolean landedAhead
      ) {
         BlockPos deeper() {
            return this.to.offset(this.dx, 0, this.dz);
         }
      }

      static final class Inner_CachePool implements ClassCache.Inner_StateCache, Function<BlockPos, ClassCache.Inner_FileCache> {
         @Override
         public ClassCache.Inner_FileCache apply(BlockPos var1) {
            return this.putAt_FileStore_BitCache_BlockPos(var1);
         }
         final ClassCache.Inner_StateCache putAt_FileStore_TextStor;
         private long[] putAt_JArr;
         private ClassCache.Inner_FileCache[] putAt_FileStore_BitCacheArr;
         private final int closeOf_I;
         int flowState;
         ClassCache.UiHelper putAt_FileStore_PacketHe;
         boolean putAt_Z;
         t.Inner_DataUtil putAt_DataCodec_CacheHel;
         static final int flowTemp = 6;
         long putAt_J;
         private StreamGate putAt_RefQueue;
         private Long2LongOpenHashMap putAt_Long2LongOpenHashM;
         Map<ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil, Object> putAt_Map = new HashMap<>();
         private final Map<BlockPos, List<ClassCache.UiHelper>> fill_Map = new HashMap<>();
         int readEx_I;
         int value_I;
         long fill_J;
         private long prevEx_J;
         private long prev_J;
         private boolean readEx_Z;
         private boolean value_Z;
         private boolean prevEx_Z;
         private boolean prev_Z;
         private boolean chunkAt_Z;
         private boolean state_Z;
         boolean fill_Z;
         private boolean closeOf_Z;
         ClassCache.Inner_NetHelper putAt_FileStore_RingUtil;
         private Consumer<ClassCache.Inner_RenderState.Inner_DataUtil> putAt_Consumer;
         static final long readEx_J = 65000000L;
         static final long value_J = 200000000L;
         private boolean pollAt_Z;
         private static final Object putAt_Obj = new Object();
         private static final Long2ObjectOpenHashMap<Object> putAt_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
         private static final int pollAt_I = 65536;
         final double putAt_D;
         static final int prevEx_I = 0;
         static final int prev_I = 1;
         static final int chunkAt_I = 2;
         private final Long2ByteOpenHashMap putAt_Long2ByteOpenHashM;
         private final Map<Long, Object> readEx_Map;
         static final int state_I = 120;
         private static final ClassCache.Inner_FileCache putAt_FileStore_BitCache = ClassCache.Inner_FileCache.fullBlock(1.0 / 0.0);
         private long[] fill_JArr;
         private ClassCache.Inner_FileCache[] fill_FileStore_BitCacheArr;
         private int pollOf_I;
         private ClassCache.UiHelper fill_FileStore_PacketHe;
         private t.Inner_DataUtil fill_DataCodec_CacheHel;
         private boolean pollOf_Z;
         private long chunkAt_J;

         int putAt_I_I(int var1, int var2, int var3, int var4) {
            if (this.putAt_Long2LongOpenHashM == null) {
               this.putAt_Long2LongOpenHashM = new Long2LongOpenHashMap();
            }

            for (int var5 = var2 & -64; var5 >= (var4 & -64); var5 -= 64) {
               long var6 = BlockPos.asLong(var1, var5, var3);
               long var8 = this.putAt_Long2LongOpenHashM.getOrDefault(var6, -9223372036854775808L);
               if (var8 == -9223372036854775808L && !this.putAt_Long2LongOpenHashM.containsKey(var6)) {
                  ClassCache.Inner_IoCodec var10 = this.putAt_FileStore_TextStor.putAt_FileStore_IoCodec_I(var1, var5, var3, this.value_Z);
                  var8 = var10.occupied();
                  if (var10.pending()) {
                     this.flowState |= 1;
                  } else {
                     this.putAt_Long2LongOpenHashM.put(var6, var8);
                  }
               }

               if (var2 - var5 < 63) {
                  var8 &= -1L >>> 63 - (var2 - var5);
               }

               if (var8 != 0L) {
                  return Math.max(var4 - 1, var5 + 63 - Long.numberOfLeadingZeros(var8));
               }
            }

            return var4 - 1;
         }

         Inner_CachePool(ClassCache.Inner_StateCache var1, int var2) {
            this(var1, var2, var1.movementSpeed());
         }

         Inner_CachePool(ClassCache.Inner_StateCache var1, int var2, double var3) {
            this.putAt_FileStore_RingUtil = ClassCache.Inner_NetHelper.init;
            this.putAt_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
            this.readEx_Map = new HashMap<>();
            this.fill_JArr = new long[16];
            this.fill_FileStore_BitCacheArr = new ClassCache.Inner_FileCache[16];
            this.putAt_FileStore_TextStor = var1;
            this.closeOf_I = var2;
            this.putAt_D = var3;
            this.putAt_JArr = new long[256];
            this.putAt_FileStore_BitCacheArr = new ClassCache.Inner_FileCache[256];
         }

         boolean fill_Z() {
            return this.readEx_Z;
         }

         private boolean readEx(BlockPos var1) {
            return !this.putAt_FileStore_BitCache_BlockPos(var1.above(2)).passable()
               ? true
               : !this.putAt_FileStore_BitCache_BlockPos(var1.west().above()).passable()
                     && !this.putAt_FileStore_BitCache_BlockPos(var1.east().above()).passable()
                  || !this.putAt_FileStore_BitCache_BlockPos(var1.north().above()).passable()
                     && !this.putAt_FileStore_BitCache_BlockPos(var1.south().above()).passable();
         }

         private boolean value(BlockPos var1) {
            if (this.prevEx_Z) {
               return false;
            }

            boolean var2 = this.prevEx_J >= 65000000L && (!this.readEx(var1) || this.prev_J >= 200000000L);
            if (var2) {
               this.readEx_Z = true;
            }

            return var2;
         }

         int putAt_I_BlockPos(BlockPos var1) {
            ClassCache.Inner_FileCache var2 = this.putAt_FileStore_BitCache_BlockPos(var1);
            if (var2.door() && var2.passable()) {
               short var3 = this.putAt_FileStore_TextStor.putAt_S_I(var1.getX(), var1.getY(), var1.getZ());
               if (var3 != 0 && var3 != 1 && var3 != -1) {
                  List var4 = e.init_List_S(var3);
                  if (var4.isEmpty()) {
                     return 0;
                  }

                  AABB var5 = (AABB)var4.get(0);
                  return var5.maxX - var5.minX >= var5.maxZ - var5.minZ ? 1 : 2;
               } else {
                  return 0;
               }
            } else {
               return 0;
            }
         }

         boolean putAt_Z_BlockPos(BlockPos var1) {
            return this.putAt_I_BlockPos(var1) != 0 || this.putAt_I_BlockPos(var1.above()) != 0;
         }

         boolean putAt(BlockPos var1, int var2, int var3) {
            int var4 = this.putAt_I_BlockPos(var1);
            if (var4 == 0) {
               var4 = this.putAt_I_BlockPos(var1.above());
            }

            if (var4 == 0) {
               return false;
            } else if (var2 != 0 && var3 != 0) {
               return true;
            } else {
               return var2 != 0 ? var4 != 1 : var4 != 2;
            }
         }

         StreamGate putAt_RefQueue() {
            if (this.putAt_RefQueue == null) {
               this.putAt_RefQueue = new StreamGate(this.putAt_FileStore_TextStor, this);
            }

            return this.putAt_RefQueue;
         }

         boolean fill(BlockPos var1) {
            long var2 = var1.asLong();
            byte var4 = this.putAt_Long2ByteOpenHashM.get(var2);
            if (var4 != 0) {
               return var4 == 2;
            }

            boolean var5 = false;

            for (int[] var9 : ClassCache.Inner_RenderState.readEx_IArr2) {
               BlockPos var10 = var1.offset(var9[0], 0, var9[1]);
               if (this.putAt_FileStore_BitCache_BlockPos(var10).contactHazard() || this.putAt_FileStore_BitCache_BlockPos(var10.above()).contactHazard()) {
                  var5 = true;
                  break;
               }
            }

            this.putAt_Long2ByteOpenHashM.put(var2, (byte)(var5 ? 2 : 1));
            return var5;
         }

         double putAt_D_BlockPos(BlockPos var1) {
            return this.fill(var1) ? 2.0 : 0.0;
         }

         BlockPos putAt_BlockPos_BlockPos(BlockPos var1, double var2, int var4, int var5) {
            long var6 = var1.asLong() * 31L + (var4 + 1) * 3 + (var5 + 1);
            Object var8 = this.readEx_Map.get(var6);
            if (var8 != null) {
               return var8 == putAt_Obj ? null : (BlockPos)var8;
            }

            if (!this.fill(var1, var4, var5)) {
               this.readEx_Map.put(var6, putAt_Obj);
               return null;
            }

            StreamGate var9 = this.putAt_RefQueue();
            long var10 = var9.putAt_J;
            BlockPos var12 = this.putAt_BlockPos_RefQueue(var9, var1, var2, (float)Math.toDegrees(Math.atan2(-var4, var5)), null);
            BlockPos var13 = null;
            if (var12 != null) {
               BlockPos var14 = this.putAt_BlockPos_RefQueue(var9, var1, var2, 0.0F / 0.0F, var12);
               if (var14 != null) {
                  BlockPos var15 = var14;

                  while (this.putAt_FileStore_BitCache_BlockPos(var15.below()).water() && var1.getY() - var15.getY() < 40) {
                     var15 = var15.below();
                  }

                  BlockPos var16 = var14;

                  while (this.putAt_FileStore_BitCache_BlockPos(var16.above()).water() && var16.getY() < var1.getY()) {
                     var16 = var16.above();
                  }

                  if (ClassCache.Inner_RenderState.fill(this.putAt_FileStore_BitCache_BlockPos(var15.below())) && var16.getY() < var1.getY()) {
                     var13 = var16;
                  }
               }
            }

            if (var9.putAt_J == var10) {
               this.readEx_Map.put(var6, var13 == null ? putAt_Obj : var13);
            }

            return var13;
         }

         private boolean fill(BlockPos var1, int var2, int var3) {
            for (int var4 = 1; var4 <= 3; var4++) {
               int var5 = var1.getX() + var2 * var4;
               int var6 = var1.getZ() + var3 * var4;

               for (int var7 = var1.getY() - 1; var7 >= var1.getY() - 40; var7--) {
                  ClassCache.Inner_FileCache var8 = this.putAt_FileStore_BitCache_BlockPos(new BlockPos(var5, var7, var6));
                  if (!var8.loaded()) {
                     break;
                  }

                  if (var8.water()) {
                     return true;
                  }

                  if (!var8.passable()) {
                     break;
                  }
               }
            }

            return false;
         }

         private BlockPos putAt_BlockPos_RefQueue(StreamGate var1, BlockPos var2, double var3, float var5, BlockPos var6) {
            NodeWalker.Inner_StreamUtil var7 = NodeWalker.Inner_StreamUtil.standing(var2.getX() + 0.5, var3, var2.getZ() + 0.5);
            boolean var8 = false;
            ArrayList<NodeWalker.Inner_StreamUtil> var9 = new ArrayList<>();

            for (int var10 = 0; var10 < 120; var10++) {
               float var11 = var6 == null ? var5 : NodeWalker.putAt_F_D(var7.x(), var7.z(), var6.getX() + 0.5, var6.getZ() + 0.5);
               var7 = NodeWalker.putAt(var7, new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, var11), var1);
               if (!var7.onGround()) {
                  var8 = true;
               } else if (var8) {
                  return null;
               }

               if (var8 && var7.horizontalCollision()) {
                  return null;
               }

               if (!var8 && var10 > 12) {
                  return null;
               }

               if (var8) {
                  var9.add(var7);
               }

               BlockPos var12 = BlockPos.containing(var7.x(), var7.y() + 0.01, var7.z());
               if (var8 && this.putAt_FileStore_BitCache_BlockPos(var12).water()) {
                  return ClassCache.Inner_RenderState.putAt_Z_TokenCore_TaskStat(var1, var7.x(), var7.z(), var12)
                        && ClassCache.Inner_RenderState.putAt_Z_TokenCore_TaskStat(var1, var9, var12.getY())
                     ? var12
                     : null;
               }

               if (this.putAt_FileStore_BitCache_BlockPos(var12).lava() || var2.getY() - var7.y() > 40.0) {
                  return null;
               }
            }

            return null;
         }

         TableIndex.Inner_StateCache putAt_IoUtil_TextState_BlockPos(BlockPos var1, BlockPos var2, long var3, boolean var5, Supplier<TableIndex.Inner_StateCache> var6) {
            ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil var7 = new ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil(
               var1.asLong(), var2.asLong(), var3, var5
            );
            Object var8 = this.putAt_Map.get(var7);
            if (var8 != null) {
               return var8 == putAt_Obj ? null : (TableIndex.Inner_StateCache)var8;
            }

            StreamGate var9 = this.putAt_RefQueue();
            long var10 = var9.putAt_J;
            TableIndex.Inner_StateCache var12 = (TableIndex.Inner_StateCache)var6.get();
            if (var9.putAt_J == var10 && !this.readEx()) {
               this.putAt_Map.put(var7, var12 == null ? putAt_Obj : var12);
            }

            return var12;
         }

         TableIndex.Inner_StateCache putAt(BlockPos var1, double var2, BlockPos var4, double var5, double var7, boolean var9) {
            long var10 = Double.doubleToLongBits(var7);
            ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil var12 = new ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil(
               var1.asLong(), var4.asLong(), var10, var9
            );
            Object var13 = this.putAt_Map.get(var12);
            if (var13 != null) {
               return var13 == putAt_Obj ? null : (TableIndex.Inner_StateCache)var13;
            }

            boolean var14 = !this.prevEx_Z && (this.readEx(var1) || this.readEx(var4));
            if (this.prevEx_Z || (var14 ? this.prev_J < 200000000L : this.prevEx_J < 65000000L)) {
               long var15 = System.nanoTime();
               StreamGate var17 = this.putAt_RefQueue();
               long var18 = var17.putAt_J;
               boolean var20 = this.prevEx_Z && this.putAt_FileStore_RingUtil == ClassCache.Inner_NetHelper.load && this.putAt_Z_BlockPos(var1, var2, var4, var5);
               boolean var21 = this.prevEx_Z
                  && Math.abs(var4.getX() - var1.getX()) == 1
                  && Math.abs(var4.getZ() - var1.getZ()) == 1
                  && Math.abs(var5 - var2) <= 0.25
                  && (
                     !this.putAt_FileStore_BitCache_BlockPos(new BlockPos(var4.getX(), var1.getY(), var1.getZ())).passable()
                        || !this.putAt_FileStore_BitCache_BlockPos(new BlockPos(var1.getX(), var1.getY(), var4.getZ())).passable()
                  );
               boolean var22 = var20 || var21;
               if (!this.prev_Z) {
                  var22 |= !this.putAt_FileStore_BitCache_BlockPos(var1.above(2)).passable()
                     || !this.putAt_FileStore_BitCache_BlockPos(var4.above(2)).passable();
                  if (this.prevEx_Z) {
                     var22 |= this.putAt_FileStore_BitCache_BlockPos(var1.below()).narrowFooting()
                        || this.putAt_FileStore_BitCache_BlockPos(var4.below()).narrowFooting()
                        || !this.putAt_FileStore_BitCache_BlockPos(var1).passable()
                        || !this.putAt_FileStore_BitCache_BlockPos(var4).passable();
                  }

                  for (int[] var26 : ClassCache.Inner_RenderState.putAt_IArr2) {
                     if (var22) {
                        break;
                     }

                     BlockPos var27 = var1.offset(var26[0], 0, var26[1]);
                     BlockPos var28 = var4.offset(var26[0], 0, var26[1]);
                     var22 = !this.putAt_FileStore_BitCache_BlockPos(var27.above()).passable()
                        || !this.putAt_FileStore_BitCache_BlockPos(var28.above()).passable()
                        || this.prevEx_Z
                           && (!this.putAt_FileStore_BitCache_BlockPos(var27).passable() || !this.putAt_FileStore_BitCache_BlockPos(var28).passable());
                  }
               }

               if (var7 > 0.0 && var22) {
                  TableIndex.Inner_StateCache var36 = this.putAt(var17, var1, var2, var4, var5, var7, var10, var9, var18);
                  TableIndex.Inner_StateCache var37 = var36;
                  if (var37 == null || var37.margin() < -0.04) {
                     TableIndex.Inner_StateCache var39 = this.putAt(var1, var2, var4, var5, 0.0, var9);
                     if (var39 != null && (var37 == null || var39.margin() > var37.margin())) {
                        var37 = var39;
                     }
                  }

                  if (var17.putAt_J == var18) {
                     this.putAt_Map.put(var12, var37 == null ? putAt_Obj : var37);
                  }

                  long var40 = System.nanoTime() - var15;
                  this.fill_J += var40;
                  if (var14) {
                     this.prev_J += var40;
                  } else {
                     this.prevEx_J += var40;
                  }

                  return var37;
               } else {
                  long var33 = var17.putAt_J_BlockPos(var1, var4, var22) * 31L
                     + (var4.getX() - var1.getX()) * 1000003L
                     + (var4.getY() - var1.getY()) * 10007L
                     + (var4.getZ() - var1.getZ()) * 7L
                     + (var9 ? 3 : 0);
                  if (this.putAt_FileStore_RingUtil != ClassCache.Inner_NetHelper.init) {
                     var33 ^= 6384245875588680899L * this.putAt_FileStore_RingUtil.ordinal();
                  }

                  if (var22 && this.prevEx_Z) {
                     var33 ^= -3263064605168079213L;
                  }

                  if (this.prevEx_Z && !var22 && var5 - var2 > 0.6 && Math.abs((var4.getX() - var1.getX()) * (var4.getZ() - var1.getZ())) == 8) {
                     var33 ^= -3263064605168079213L;
                  }

                  boolean var38 = this.state_Z && !var20;
                  if (var22 && var38) {
                     var33 ^= -7046029254386353131L;
                  }

                  if (var22 && this.fill_Z) {
                     var33 ^= -1065810590584100411L;
                  }

                  if (this.chunkAt_Z) {
                     var33 ^= -6752110988234923001L;
                  }

                  var33 = var33 * 31L + HashCommon.mix(var10);
                  var33 = var33 * 31L + HashCommon.mix(Double.doubleToLongBits(this.putAt_D));
                  Object var42;
                  synchronized (putAt_Long2ObjectOpenHas) {
                     var42 = putAt_Long2ObjectOpenHas.get(var33);
                  }

                  TableIndex.Inner_StateCache var41;
                  if (var42 != null && var17.putAt_J == var18) {
                     this.value_I++;
                     var41 = var42 == putAt_Obj
                        ? null
                        : ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var42)
                           .plan()
                           .shifted(
                              var1.getX() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var42).x(),
                              var1.getY() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var42).y(),
                              var1.getZ() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var42).z()
                           );
                     if (var41 != null && var41.maneuver() != null && var41.maneuver().replay(var17, var41.maneuver().start(), 0, var4, var5) < -0.04) {
                        var42 = null;
                     }
                  } else {
                     var41 = null;
                  }

                  if (var42 == null) {
                     this.readEx_I++;
                     var41 = TableIndex.putAt(var17, var1, var2, var4, var5, var7, var9, var22, this.prevEx_Z, var38, this.chunkAt_Z);
                     if ((var41 != null || !var22) && var17.putAt_J == var18) {
                        synchronized (putAt_Long2ObjectOpenHas) {
                           if (putAt_Long2ObjectOpenHas.size() >= 65536) {
                              putAt_Long2ObjectOpenHas.clear();
                           }

                           putAt_Long2ObjectOpenHas.put(
                              var33,
                              var41 == null
                                 ? putAt_Obj
                                 : new ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil(var41, var1.getX(), var1.getY(), var1.getZ())
                           );
                        }
                     }
                  }

                  if (var17.putAt_J == var18) {
                     this.putAt_Map.put(var12, var41 == null ? putAt_Obj : var41);
                  }

                  long var45 = System.nanoTime() - var15;
                  this.fill_J += var45;
                  if (var14) {
                     this.prev_J += var45;
                  } else {
                     this.prevEx_J += var45;
                  }

                  return var41;
               }
            } else {
               this.readEx_Z = true;
               if (!this.pollAt_Z) {
                  this.pollAt_Z = true;
                  ListPool.init("jump solver budget spent: solves=" + this.readEx_I + " hits=" + this.value_I);
               }

               return null;
            }
         }

         private TableIndex.Inner_StateCache putAt(
            StreamGate var1, BlockPos var2, double var3, BlockPos var5, double var6, double var8, long var10, boolean var12, long var13
         ) {
            long var15 = var1.putAt_J_BlockPos(var2, var5, false) * 31L
               + (var5.getX() - var2.getX()) * 1000003L
               + (var5.getY() - var2.getY()) * 10007L
               + (var5.getZ() - var2.getZ()) * 7L
               + (var12 ? 3 : 0);
            if (this.putAt_FileStore_RingUtil != ClassCache.Inner_NetHelper.init) {
               var15 ^= 6384245875588680899L * this.putAt_FileStore_RingUtil.ordinal();
            }

            if (this.chunkAt_Z) {
               var15 ^= -6752110988234923001L;
            }

            var15 = var15 * 31L + HashCommon.mix(var10);
            var15 = var15 * 31L + HashCommon.mix(Double.doubleToLongBits(this.putAt_D));
            Object var17;
            synchronized (putAt_Long2ObjectOpenHas) {
               var17 = putAt_Long2ObjectOpenHas.get(var15);
            }

            if (var17 != null && var1.putAt_J == var13) {
               this.value_I++;
               return var17 == putAt_Obj
                  ? null
                  : ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var17)
                     .plan()
                     .shifted(
                        var2.getX() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var17).x(),
                        var2.getY() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var17).y(),
                        var2.getZ() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)var17).z()
                     );
            }

            this.readEx_I++;
            TableIndex.Inner_StateCache var25 = TableIndex.putAt(var1, var2, var3, var5, var6, var8, var12, false, false, false, this.chunkAt_Z);
            if (var1.putAt_J == var13) {
               synchronized (putAt_Long2ObjectOpenHas) {
                  if (putAt_Long2ObjectOpenHas.size() >= 65536) {
                     putAt_Long2ObjectOpenHas.clear();
                  }

                  putAt_Long2ObjectOpenHas.put(
                     var15,
                     var25 == null ? putAt_Obj : new ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil(var25, var2.getX(), var2.getY(), var2.getZ())
                  );
               }
            }

            return var25;
         }

         private boolean putAt_Z_BlockPos(BlockPos var1, double var2, BlockPos var4, double var5) {
            int var7 = var4.getX() - var1.getX();
            int var8 = var4.getZ() - var1.getZ();
            int var9 = Math.max(Math.abs(var7), Math.abs(var8));
            if (var9 >= 4 && !(var2 - var5 < 1.5)) {
               int var10 = (int)Math.floor(var2);

               for (int var11 = 2; var11 < var9; var11++) {
                  int var12 = var1.getX() + (int)Math.round((double)var11 * var7 / var9);
                  int var13 = var1.getZ() + (int)Math.round((double)var11 * var8 / var9);
                  if (this.putAt_S_I(var12, var10, var13) == 1 && this.putAt_S_I(var12, var10 + 1, var13) == 1) {
                     return true;
                  }
               }

               return false;
            } else {
               return false;
            }
         }

         private void putAt_V() {
            long[] var1 = this.putAt_JArr;
            ClassCache.Inner_FileCache[] var2 = this.putAt_FileStore_BitCacheArr;
            this.putAt_JArr = new long[this.putAt_JArr.length * 2];
            this.putAt_FileStore_BitCacheArr = new ClassCache.Inner_FileCache[this.putAt_JArr.length];

            for (int var3 = 0; var3 < var1.length; var3++) {
               if (var2[var3] != null) {
                  int var4 = (int)HashCommon.mix(var1[var3]) & this.putAt_JArr.length - 1;
                  this.putAt_JArr[var4] = var1[var3];
                  this.putAt_FileStore_BitCacheArr[var4] = var2[var3];
               }
            }
         }

         private void fill_V() {
            this.pollOf_Z = true;
            this.fill_FileStore_PacketHe = this.putAt_FileStore_PacketHe;
            this.fill_DataCodec_CacheHel = this.putAt_DataCodec_CacheHel;
            this.pollOf_I = 0;
            this.chunkAt_J = 0L;
            if (this.putAt_FileStore_PacketHe != null) {
               for (BlockPos var2 : this.putAt_FileStore_PacketHe.cellsToBreak()) {
                  this.putAt(var2.asLong(), ClassCache.Inner_FileCache.air());
               }

               for (BlockPos var8 : this.putAt_FileStore_PacketHe.cellsToPlace()) {
                  this.putAt(var8.asLong(), putAt_FileStore_BitCache);
               }

               if (this.putAt_FileStore_PacketHe.type() == ClassCache.Inner_SyncManager.set) {
                  this.putAt(this.putAt_FileStore_PacketHe.to().asLong(), ClassCache.Inner_FileCache.air());
                  this.putAt(this.putAt_FileStore_PacketHe.to().above().asLong(), ClassCache.Inner_FileCache.air());
               }
            }

            if (this.putAt_DataCodec_CacheHel != null) {
               int var7 = 0;

               for (t.Inner_DataUtil var9 = this.putAt_DataCodec_CacheHel.init_t_DataUtil;
                  var9 != null && var9.init_ClassCache_UiHelpe != null && var7 < 6;
                  var7++
               ) {
                  ClassCache.UiHelper var3 = var9.init_ClassCache_UiHelpe;

                  for (BlockPos var5 : var3.cellsToPlace()) {
                     this.putAt(var5.asLong(), putAt_FileStore_BitCache);
                  }

                  for (BlockPos var11 : var3.cellsToBreak()) {
                     this.putAt(var11.asLong(), ClassCache.Inner_FileCache.air());
                  }

                  var9 = var9.init_t_DataUtil;
               }
            }
         }

         private static long putAt_J_J(long var0) {
            return 1L << (int)(var0 * -7046029254386353131L >>> 58);
         }

         private void putAt(long var1, ClassCache.Inner_FileCache var3) {
            this.chunkAt_J = this.chunkAt_J | putAt_J_J(var1);
            if (this.pollOf_I == this.fill_JArr.length) {
               this.fill_JArr = Arrays.copyOf(this.fill_JArr, this.pollOf_I * 2);
               this.fill_FileStore_BitCacheArr = Arrays.copyOf(this.fill_FileStore_BitCacheArr, this.pollOf_I * 2);
            }

            this.fill_JArr[this.pollOf_I] = var1;
            this.fill_FileStore_BitCacheArr[this.pollOf_I++] = var3;
         }

         public ClassCache.Inner_FileCache putAt_FileStore_BitCache_BlockPos(BlockPos var1) {
            return this.probe(var1.getX(), var1.getY(), var1.getZ());
         }

         @Override
         public ClassCache.Inner_FileCache probe(int var1, int var2, int var3) {
            if (!this.pollOf_Z
               || this.putAt_FileStore_PacketHe != this.fill_FileStore_PacketHe
               || this.putAt_DataCodec_CacheHel != this.fill_DataCodec_CacheHel) {
               this.fill_V();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.chunkAt_J & putAt_J_J(var4)) != 0L) {
               for (int var6 = 0; var6 < this.pollOf_I; var6++) {
                  if (this.fill_JArr[var6] == var4) {
                     return this.fill_FileStore_BitCacheArr[var6];
                  }
               }
            }

            if (this.putAt_J >= this.putAt_JArr.length && this.putAt_JArr.length < this.closeOf_I) {
               this.putAt_V();
            }

            int var8 = (int)HashCommon.mix(var4) & this.putAt_JArr.length - 1;
            if (this.putAt_FileStore_BitCacheArr[var8] != null && this.putAt_JArr[var8] == var4) {
               return this.putAt_FileStore_BitCacheArr[var8];
            }

            this.putAt_J++;
            ClassCache.Inner_FileCache var7 = this.putAt_FileStore_TextStor.probe(var1, var2, var3);
            if (var7 == null) {
               this.flowState |= 1;
               return ClassCache.Inner_FileCache.UNLOADED;
            }

            if (this.value_Z && var7.cached()) {
               var7 = ClassCache.Inner_FileCache.UNLOADED;
            }

            this.putAt_JArr[var8] = var4;
            this.putAt_FileStore_BitCacheArr[var8] = var7;
            return var7;
         }

         boolean readEx() {
            if (!this.pollOf_Z
               || this.putAt_FileStore_PacketHe != this.fill_FileStore_PacketHe
               || this.putAt_DataCodec_CacheHel != this.fill_DataCodec_CacheHel) {
               this.fill_V();
            }

            return this.pollOf_I != 0;
         }

         @Override
         public short putAt_S_I(int var1, int var2, int var3) {
            if (!this.pollOf_Z
               || this.putAt_FileStore_PacketHe != this.fill_FileStore_PacketHe
               || this.putAt_DataCodec_CacheHel != this.fill_DataCodec_CacheHel) {
               this.fill_V();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.chunkAt_J & putAt_J_J(var4)) != 0L) {
               for (int var6 = 0; var6 < this.pollOf_I; var6++) {
                  if (this.fill_JArr[var6] == var4) {
                     return e.init_S_ClassCache_FileCac(this.fill_FileStore_BitCacheArr[var6]);
                  }
               }
            }

            return this.value_Z && !this.probe(var1, var2, var3).loaded() ? 1 : this.putAt_FileStore_TextStor.putAt_S_I(var1, var2, var3);
         }

         @Override
         public short fill_S(int var1, int var2, int var3) {
            if (!this.pollOf_Z
               || this.putAt_FileStore_PacketHe != this.fill_FileStore_PacketHe
               || this.putAt_DataCodec_CacheHel != this.fill_DataCodec_CacheHel) {
               this.fill_V();
            }

            long var4 = BlockPos.asLong(var1, var2, var3);
            if ((this.chunkAt_J & putAt_J_J(var4)) != 0L) {
               for (int var6 = 0; var6 < this.pollOf_I; var6++) {
                  if (this.fill_JArr[var6] == var4) {
                     return e.init_S_ClassCache_FileCac(this.fill_FileStore_BitCacheArr[var6]);
                  }
               }
            }

            return this.putAt_FileStore_TextStor.fill_S(var1, var2, var3);
         }

         private record Inner_DataUtil(long from, long to, long approach, boolean sprint) {
         }

         private record Inner_StreamUtil(TableIndex.Inner_StateCache plan, int x, int y, int z) {
         }
      }

      private record Inner_StreamUtil(int x, int z, int distance, int stepX, int stepZ, double length, double reach, double cornerReach, int exits) {
      }
   }

   public static final class Inner_TextFormat {
      public static final int flowState = 64;
      public static final int flowTemp = 40;
      public static final int readEx_I = 2000000;
      private static final int prevEx_I = 3000000;
      static final double[] putAt_DArr = new double[]{1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0};
      static final int value_I = 5;
      private final t.Inner_DataUtil[] putAt_DataCodec_CacheHelArr = new t.Inner_DataUtil[putAt_DArr.length];
      private final double[] fill_DArr = new double[putAt_DArr.length];
      private final ClassCache.Inner_StateCache putAt_FileStore_TextStor;
      private final BlockPos putAt_BlockPos;
      private final ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod;
      private final ClassCache.Inner_DataUtil putAt_FileStore_CacheHel;
      private final Set<Long> fill_Set;
      private final t putAt_DataCodec = new t();
      private final Long2ObjectOpenHashMap<t.Inner_DataUtil> putAt_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>(256);
      private final ClassCache.Inner_RenderState.Inner_CachePool putAt_FileStore_IoHelper;
      private final List<ClassCache.UiHelper> putAt_List = new ArrayList<>(16);
      private t.Inner_DataUtil putAt_DataCodec_CacheHel;
      private final long putAt_J;
      private int prev_I;
      private int chunkAt_I;
      private int state_I;
      private int closeOf_I = 1;
      private boolean putAt_Z;
      private boolean fill_Z;
      private boolean readEx_Z;
      private boolean value_Z;
      private boolean prevEx_Z;
      private int pollAt_I = 64;
      private int pollOf_I = 40;
      private BlockPos fill_BlockPos;
      private boolean prev_Z;
      private static final int tick_I = 24;
      private static final int flushOf_I = 14;
      private boolean chunkAt_Z;
      private boolean state_Z;
      private boolean closeOf_Z;
      private boolean pollAt_Z;
      private boolean pollOf_Z;
      private ClassCache.Inner_HashPool putAt_FileStore_HashPool;
      private t.Inner_DataUtil fill_DataCodec_CacheHel;
      private PriorityQueue<ClassCache.Inner_TextFormat.Inner_StreamUtil> putAt_PriorityQueue;
      private final Map<ClassCache.Inner_TextFormat.Inner_StreamUtil, Future<ClassCache.Inner_TextFormat.Inner_DataUtil>> putAt_Map = new HashMap<>();
      private final ConcurrentLinkedQueue<ClassCache.Inner_RenderState.Inner_CachePool> putAt_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
      private static final int stateOf = Runtime.getRuntime().availableProcessors();
      private static final int store = Math.clamp(stateOf >= 10 ? stateOf - 2 : stateOf / 2, 1, 10);
      private static final ExecutorService putAt_ExecutorService = Executors.newFixedThreadPool(store, var0 -> {
         Thread var1 = new Thread(var0, "Autism jump proof");
         var1.setDaemon(true);
         var1.setPriority(4);
         return var1;
      });
      private int nextBy;
      Set<ClassCache.Inner_ConfigStore> putAt_Set = Set.of();
      private boolean tick_Z;
      private long fill_J;
      private static final double putAt_D = 18.0;
      private boolean flushOf_Z;

      static ClassCache.Inner_TextFormat putAt(
         ClassCache.Inner_StateCache var0, BlockPos var1, ClassCache.Inner_JsonUtil var2, ClassCache.Inner_DataUtil var3, Set<Long> var4
      ) {
         return putAt(var0, var1, var2, var3, var4, false);
      }

      static ClassCache.Inner_TextFormat putAt(
         ClassCache.Inner_StateCache var0, BlockPos var1, ClassCache.Inner_JsonUtil var2, ClassCache.Inner_DataUtil var3, Set<Long> var4, boolean var5
      ) {
         return putAt(var0, var1, var2, var3, var4, var5, true, var0.movementSpeed());
      }

      private static ClassCache.Inner_TextFormat putAt(
         ClassCache.Inner_StateCache var0,
         BlockPos var1,
         ClassCache.Inner_JsonUtil var2,
         ClassCache.Inner_DataUtil var3,
         Set<Long> var4,
         boolean var5,
         boolean var6,
         double var7
      ) {
         ClassCache.Inner_TextFormat var9 = new ClassCache.Inner_TextFormat(var0, var1, var2, var3, var4, var7);
         var9.chunkAt_Z = true;
         var9.putAt_FileStore_IoHelper.value_Z = true;
         var9.putAt_FileStore_IoHelper.prevEx_Z = var6;
         var9.putAt_FileStore_IoHelper.prev_Z = var5;
         if (var6
            && !var5
            && !var3.allowDig()
            && !var3.allowBridge()
            && !var3.allowPillar()
            && (var0 instanceof RingBuffer || var0 instanceof ClassCache.Inner_StreamUtil || var0.putAt_Z())) {
            var9.nextBy = store;
            var9.putAt_FileStore_IoHelper.putAt_Map = new ConcurrentHashMap<>();
         }

         if (var5) {
            var9.putAt_DataCodec.init(2.0);
         }

         BlockPos var10 = var2.putAt_BlockPos();
         ClassCache.Inner_FileCache var11 = var10 == null ? null : var0.probe(var10.getX(), var10.getY(), var10.getZ());
         var9.state_Z = var10 != null && var11 != null && (!var11.loaded() || var11.cached());
         if (var10 != null && var11 == null) {
            var9.readEx_Z = true;
         }

         if (ListPool.init_Z()) {
            ListPool.init("route proof target=" + var10 + " unknown=" + var9.state_Z + " grid=" + var0.getClass().getSimpleName() + " destination=" + var11);
         }

         var9.pollAt_I = var10 == null
            ? 512
            : (
               var9.state_Z
                  ? 2048
                  : (int)Math.min(2048L, Math.max(128L, Math.max(Math.abs((long)var10.getX() - var1.getX()), Math.abs((long)var10.getZ() - var1.getZ())) + 64L))
            );
         var9.pollOf_I = var10 == null ? 512 : (int)Math.min(512L, Math.max(96L, Math.abs((long)var10.getY() - var1.getY()) + 32L));
         return var9;
      }

      private boolean putAt_Z_BlockPos(BlockPos var1) {
         ClassCache.Inner_FileCache var2 = this.putAt_FileStore_IoHelper.putAt_FileStore_BitCache_BlockPos(var1);
         ClassCache.Inner_FileCache var3 = this.putAt_FileStore_IoHelper.putAt_FileStore_BitCache_BlockPos(var1.below());
         if (ClassCache.Inner_RenderState.putAt_Z_FileStore_BitCache(var2, this.putAt_FileStore_IoHelper.putAt_FileStore_BitCache_BlockPos(var1.above()), var3)
            && !var2.water()
            && !var2.climbable()
            && var3.top16() <= 16) {
            BlockPos var4 = this.putAt_FileStore_TokenCod.putAt_BlockPos();
            int var5 = Integer.compare(var4.getX(), var1.getX());
            int var6 = Integer.compare(var4.getZ(), var1.getZ());

            for (int[] var10 : ClassCache.Inner_RenderState.putAt_IArr2) {
               if (var10[0] * var5 + var10[1] * var6 > 0) {
                  ClassCache.Inner_FileCache var11 = this.putAt_FileStore_TextStor.probe(var1.getX() + var10[0], var1.getY(), var1.getZ() + var10[1]);
                  if (var11 == null) {
                     this.readEx_Z = true;
                  } else {
                     if (!var11.loaded() || var11.cached()) {
                        return true;
                     }

                     if (var11.passable() && var11.hazardFeet()) {
                        ClassCache.Inner_FileCache var12 = this.putAt_FileStore_TextStor
                           .probe(var1.getX() + 2 * var10[0], var1.getY(), var1.getZ() + 2 * var10[1]);
                        if (var12 == null) {
                           this.readEx_Z = true;
                        } else if (!var12.loaded() || var12.cached()) {
                           return true;
                        }
                     }
                  }
               }
            }

            return false;
         } else {
            return false;
         }
      }

      public Inner_TextFormat(ClassCache.Inner_StateCache var1, BlockPos var2, ClassCache.Inner_JsonUtil var3, ClassCache.Inner_DataUtil var4, Set<Long> var5) {
         this(var1, var2, var3, var4, var5, var1.movementSpeed());
      }

      private Inner_TextFormat(
         ClassCache.Inner_StateCache var1, BlockPos var2, ClassCache.Inner_JsonUtil var3, ClassCache.Inner_DataUtil var4, Set<Long> var5, double var6
      ) {
         this.putAt_FileStore_TextStor = var1;
         this.putAt_BlockPos = var2.immutable();
         this.putAt_FileStore_TokenCod = var3.putAt_FileStore_TokenCod_FileStore_TextStor(var1);
         this.putAt_FileStore_CacheHel = var4;
         this.fill_Set = var5;
         this.putAt_J = var1.putAt_J();
         this.putAt_FileStore_IoHelper = new ClassCache.Inner_RenderState.Inner_CachePool(var1, 16384, var6);
         this.putAt_FileStore_IoHelper.putAt_FileStore_RingUtil = var4.fallPolicy();
         BlockPos var8 = var3.putAt_BlockPos();
         if (var8 != null && var4.allowPillar() && var4.strategy() == ClassCache.Inner_IoWorker.init) {
            int var9 = var8.getY() - var2.getY();
            double var10 = Math.hypot(var8.getX() - var2.getX(), var8.getZ() - var2.getZ());
            if (var9 >= 8 && var10 * 2.0 < var9 && var4.constructionBlocks() >= var9) {
               this.putAt_DataCodec.init(1.3);
            }
         }

         if (var4.allowParkour()) {
            this.putAt_PriorityQueue = new PriorityQueue<>(
               Comparator.comparingDouble(ClassCache.Inner_TextFormat.Inner_StreamUtil::priority).thenComparingLong(ClassCache.Inner_TextFormat.Inner_StreamUtil::order)
            );
            this.putAt_FileStore_IoHelper.putAt_Consumer = this::putAt_V_FileStore_IoHelper;
         }

         this.putAt_DataCodec_CacheHel = new t.Inner_DataUtil(this.putAt_BlockPos, var3.heuristic(var2.getX(), var2.getY(), var2.getZ()));
         this.putAt_DataCodec_CacheHel.load_D = 0.0;
         this.putAt_Long2ObjectOpenHas.put(this.putAt_BlockPos.asLong(), this.putAt_DataCodec_CacheHel);
         this.putAt_DataCodec.init(this.putAt_DataCodec_CacheHel);
         this.state_I = 1;
         Arrays.fill(this.fill_DArr, 1.7976931348623157E308);
      }

      public boolean putAt_Z() {
         return this.putAt_DataCodec_CacheHel() != null;
      }

      private t.Inner_DataUtil putAt_DataCodec_CacheHel() {
         for (t.Inner_DataUtil var4 : this.putAt_DataCodec_CacheHelArr) {
            if (var4 != null && var4.init_BlockPos.distSqr(this.putAt_BlockPos) > 25.0) {
               return var4;
            }
         }

         return this.putAt_DataCodec_CacheHel != null
               && this.putAt_DataCodec_CacheHel.init_t_DataUtil != null
               && this.putAt_DataCodec_CacheHel.init_BlockPos.distSqr(this.putAt_BlockPos) > 25.0
            ? this.putAt_DataCodec_CacheHel
            : null;
      }

      private static double putAt_D_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_DataUtil var0) {
         double var1 = Math.hypot(var0.to().getX() - var0.from().getX(), var0.to().getZ() - var0.from().getZ());
         double var3 = Math.max(0.0, var0.targetY() - var0.sourceY());
         return Math.min(18.0, 2.0 + Math.max(8.0, var1 * 2.7 + var3 * 2.0));
      }

      private void putAt_V_FileStore_IoHelper(ClassCache.Inner_RenderState.Inner_DataUtil var1) {
         t.Inner_DataUtil var2 = this.putAt_FileStore_IoHelper.putAt_DataCodec_CacheHel;
         BlockPos var3 = var1.to();
         BlockPos var4 = var1.deeper();
         if (this.fill_BlockPos == null && this.putAt_FileStore_HashPool == null || this.fill(var3) || this.fill(var4)) {
            if (this.fill_Set.contains(var4.asLong())
               || !ClassCache.Inner_RenderState.putAt_Z_Function(this.putAt_FileStore_IoHelper, var4, var1.targetY())
               || Math.abs(
                     ClassCache.Inner_RenderState.putAt(var4.getY(), this.putAt_FileStore_IoHelper.putAt_FileStore_BitCache_BlockPos(var4.below()))
                        - var1.targetY()
                  )
                  >= 1.0E-6) {
               var4 = null;
            }

            double var5 = this.fill(var1);
            if (!this.putAt_FileStore_IoHelper.chunkAt_Z
               || !this.putAt_Z_DataCodec_CacheHel(var2, var3, var2.load_D + var5)
               || var4 != null && !this.putAt_Z_DataCodec_CacheHel(var2, var4, var2.load_D + var5)) {
               double var7 = this.putAt_D_I(var3.getX(), var3.getY(), var3.getZ());
               if (var4 != null) {
                  var7 = Math.min(var7, this.putAt_D_I(var4.getX(), var4.getY(), var4.getZ()));
               }

               double var9 = this.putAt_DataCodec.init(var2.load_D + var5, var7);
               this.putAt_PriorityQueue.add(new ClassCache.Inner_TextFormat.Inner_StreamUtil(var1, var4, var2, var2.load_D, var9, this.fill_J++));
            }
         }
      }

      private double fill(ClassCache.Inner_RenderState.Inner_DataUtil var1) {
         double var2 = putAt_D_FileStore_IoHelper(var1);
         if (this.putAt_FileStore_IoHelper.chunkAt_Z) {
            double var4 = var1.targetY() - var1.sourceY();
            var2 = Math.max(var2, 7.0 + Math.sqrt(Math.max(0.0, 1.25 - var4) * 22.0));
         }

         return var2;
      }

      private double putAt_D_I(int var1, int var2, int var3) {
         double var4 = this.putAt_FileStore_TokenCod.heuristic(var1, var2, var3);
         BlockPos var6 = this.putAt_FileStore_TokenCod.putAt_BlockPos();
         if ((this.putAt_FileStore_IoHelper.chunkAt_Z || this.fill_BlockPos != null || this.putAt_FileStore_HashPool != null)
            && var6 != null
            && var2 > var6.getY() + 1) {
            double var7 = var2 - var6.getY() - 1.0;
            double var9 = Math.min(var7 * 4.0, Math.sqrt(var7 * 26.0));
            return Math.max(0.0, var4 - var7 * 4.0 + var9);
         } else {
            return var4;
         }
      }

      private boolean fill(BlockPos var1) {
         int var2 = var1.getX();
         int var3 = var1.getY();
         int var4 = var1.getZ();
         return Math.abs((long)var2 - this.putAt_BlockPos.getX()) <= this.pollAt_I
            && Math.abs((long)var4 - this.putAt_BlockPos.getZ()) <= this.pollAt_I
            && Math.abs((long)var3 - this.putAt_BlockPos.getY()) <= this.pollOf_I
            && (this.putAt_FileStore_HashPool == null || this.putAt_FileStore_HashPool.contains(var2, var3, var4))
            && (
               this.fill_BlockPos == null
                  || putAt(var2, this.putAt_BlockPos.getX(), this.fill_BlockPos.getX(), 24)
                     && putAt(var4, this.putAt_BlockPos.getZ(), this.fill_BlockPos.getZ(), 24)
                     && var3 <= Math.max(this.putAt_BlockPos.getY(), this.fill_BlockPos.getY()) + 14
                     && var3 >= this.putAt_D_I_I(var2, var4)
            );
      }

      private double putAt_D_I_I(int var1, int var2) {
         if (!this.prev_Z) {
            return Math.min(this.putAt_BlockPos.getY(), this.fill_BlockPos.getY()) - 14;
         }

         double var3 = (double)this.fill_BlockPos.getX() - this.putAt_BlockPos.getX();
         double var5 = (double)this.fill_BlockPos.getZ() - this.putAt_BlockPos.getZ();
         double var7 = Math.clamp(
            ((var1 - this.putAt_BlockPos.getX()) * var3 + (var2 - this.putAt_BlockPos.getZ()) * var5) / (var3 * var3 + var5 * var5), 0.0, 1.0
         );
         return this.putAt_BlockPos.getY() + var7 * (this.fill_BlockPos.getY() - this.putAt_BlockPos.getY()) - 8.0;
      }

      private static boolean putAt(int var0, int var1, int var2, int var3) {
         return var0 >= Math.min(var1, var2) - var3 && var0 <= Math.max(var1, var2) + var3;
      }

      private boolean putAt_Z_BlockPos(BlockPos var1, double var2, int var4) {
         for (t.Inner_DataUtil var5 = this.putAt_Long2ObjectOpenHas.get(var1.asLong()); var5 != null; var5 = var5.load_t_DataUtil) {
            if (!var5.init_Z && var5.load_D <= var2 && var5.load_I <= var4) {
               return true;
            }
         }

         return false;
      }

      private boolean putAt_Z_DataCodec_CacheHel(t.Inner_DataUtil var1, BlockPos var2, double var3) {
         return this.putAt_Z_BlockPos(var2, var3, var1.load_I)
            ? true
            : this.putAt_FileStore_IoHelper.putAt_FileStore_RingUtil == ClassCache.Inner_NetHelper.init
               && !this.putAt_FileStore_IoHelper.prev_Z
               && this.putAt_Z_BlockPos(var2, var3 + 20.0, var1.load_I)
               && ClassCache.Inner_RenderState.fill(this.putAt_FileStore_IoHelper, var1.init_BlockPos, var2);
      }

      private boolean fill_Z() {
         if (this.putAt_PriorityQueue != null
            && !this.putAt_PriorityQueue.isEmpty()
            && !(this.putAt_PriorityQueue.peek().priority() > this.putAt_DataCodec.init_D())) {
            ClassCache.Inner_TextFormat.Inner_StreamUtil var1 = this.putAt_PriorityQueue.remove();
            t.Inner_DataUtil var2 = var1.source();
            if (!var2.init_Z && var2.load_D == var1.sourceCost()) {
               double var3 = this.putAt_D_FileStore_IoPool_T(var1);
               this.putAt_FileStore_IoHelper.flowState = 0;
               this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe = var2.init_ClassCache_UiHelpe;
               this.putAt_FileStore_IoHelper.putAt_DataCodec_CacheHel = var2;
               if (!this.putAt_Z_DataCodec_CacheHel(var2, var1.candidate().to(), var3)
                  || var1.continuation() != null && !this.putAt_Z_DataCodec_CacheHel(var2, var1.continuation(), var3)) {
                  ClassCache.UiHelper var5 = this.putAt_FileStore_PacketHe_FileStore_IoPool_T(var1);
                  if (var5 != null) {
                     this.chunkAt_I++;
                     this.putAt_V_DataCodec_CacheHel_FileStore_PacketHe(var2, var5);
                     ClassCache.UiHelper var6 = ClassCache.Inner_RenderState.putAt_FileStore_PacketHe_FileStore_IoHelper(
                        this.putAt_FileStore_IoHelper, var5, this.fill_Set
                     );
                     if (var6 != null) {
                        this.chunkAt_I++;
                        this.putAt_V_DataCodec_CacheHel_FileStore_PacketHe(var2, var6);
                     }
                  }

                  if ((this.putAt_FileStore_IoHelper.flowState & 1) != 0) {
                     this.readEx_Z = true;
                  }

                  return true;
               } else {
                  this.putAt_V_FileStore_IoPool_T(var1);
                  if ((this.putAt_FileStore_IoHelper.flowState & 1) != 0) {
                     this.readEx_Z = true;
                  }

                  return true;
               }
            } else {
               this.putAt_V_FileStore_IoPool_T(var1);
               return true;
            }
         } else {
            return false;
         }
      }

      private double putAt_D_FileStore_IoPool_T(ClassCache.Inner_TextFormat.Inner_StreamUtil var1) {
         return var1.sourceCost()
            + (this.pollAt_Z || !this.putAt_FileStore_IoHelper.prev_Z && !this.putAt_FileStore_IoHelper.state_Z ? 3.0 : this.fill(var1.candidate()));
      }

      private ClassCache.UiHelper putAt_FileStore_PacketHe_FileStore_IoPool_T(ClassCache.Inner_TextFormat.Inner_StreamUtil var1) {
         if (this.nextBy >= 2 && !this.state_Z && !this.putAt_FileStore_IoHelper.readEx()) {
            var var2 = this.putAt_Map.computeIfAbsent(var1, this::putAt_Future_FileStore_IoPool_T);
            if (this.putAt_Map.size() < this.nextBy * 2) {
               int var3 = 0;

               for (ClassCache.Inner_TextFormat.Inner_StreamUtil var5 : this.putAt_PriorityQueue) {
                  if (++var3 > this.nextBy * 8 || this.putAt_Map.size() >= this.nextBy * 2) {
                     break;
                  }

                  t.Inner_DataUtil var6 = var5.source();
                  if (!var6.init_Z && var6.load_D == var5.sourceCost() && !this.putAt_Map.containsKey(var5)) {
                     double var7 = this.putAt_D_FileStore_IoPool_T(var5);
                     if ((
                           !this.putAt_Z_BlockPos(var5.candidate().to(), var7, var6.load_I)
                              || var5.continuation() != null && !this.putAt_Z_BlockPos(var5.continuation(), var7, var6.load_I)
                        )
                        && (var6.init_ClassCache_UiHelpe == null || var6.init_ClassCache_UiHelpe.type() != ClassCache.Inner_SyncManager.set)) {
                        this.putAt_Map.computeIfAbsent(var5, this::putAt_Future_FileStore_IoPool_T);
                     }
                  }
               }
            }

            try {
               ClassCache.Inner_TextFormat.Inner_DataUtil var16 = (ClassCache.Inner_TextFormat.Inner_DataUtil)var2.get();
               this.putAt_FileStore_IoHelper.flowState = this.putAt_FileStore_IoHelper.flowState | var16.flags();
               this.putAt_FileStore_IoHelper.readEx_I = this.putAt_FileStore_IoHelper.readEx_I + var16.solves();
               this.putAt_FileStore_IoHelper.value_I = this.putAt_FileStore_IoHelper.value_I + var16.hits();
               this.putAt_FileStore_IoHelper.fill_J = this.putAt_FileStore_IoHelper.fill_J + var16.nanos();
               this.putAt_FileStore_IoHelper.putAt_J = this.putAt_FileStore_IoHelper.putAt_J + var16.queries();
               return var16.movement();
            } catch (InterruptedException var13) {
               var2.cancel(true);
               Thread.currentThread().interrupt();
               this.putAt_Z = true;
               return null;
            } catch (ExecutionException var14) {
               return ClassCache.Inner_RenderState.putAt_FileStore_PacketHe_FileStore_IoHelper_2(this.putAt_FileStore_IoHelper, this.fill_Set, var1.candidate());
            } finally {
               this.putAt_Map.remove(var1);
            }
         } else {
            return ClassCache.Inner_RenderState.putAt_FileStore_PacketHe_FileStore_IoHelper_2(this.putAt_FileStore_IoHelper, this.fill_Set, var1.candidate());
         }
      }

      private Future<ClassCache.Inner_TextFormat.Inner_DataUtil> putAt_Future_FileStore_IoPool_T(ClassCache.Inner_TextFormat.Inner_StreamUtil var1) {
         ClassCache.UiHelper var2 = var1.source().init_ClassCache_UiHelpe;
         return putAt_ExecutorService.submit(
            () -> {
               ClassCache.Inner_RenderState.Inner_CachePool var3 = this.putAt_ConcurrentLinkedQu.poll();
               if (var3 == null) {
                  var3 = new ClassCache.Inner_RenderState.Inner_CachePool(this.putAt_FileStore_TextStor, 16384, this.putAt_FileStore_IoHelper.putAt_D);
                  var3.value_Z = true;
                  var3.prevEx_Z = true;
                  var3.state_Z = this.putAt_FileStore_IoHelper.state_Z;
                  var3.fill_Z = this.putAt_FileStore_IoHelper.fill_Z;
                  var3.chunkAt_Z = this.putAt_FileStore_IoHelper.chunkAt_Z;
                  var3.putAt_FileStore_RingUtil = this.putAt_FileStore_IoHelper.putAt_FileStore_RingUtil;
                  var3.prev_Z = this.putAt_FileStore_IoHelper.prev_Z;
                  var3.closeOf_Z = this.putAt_FileStore_IoHelper.closeOf_Z;
                  var3.putAt_Map = this.putAt_FileStore_IoHelper.putAt_Map;
               }

               var3.putAt_FileStore_PacketHe = var2;
               var3.flowState = var3.readEx_I = var3.value_I = 0;
               var3.fill_J = var3.putAt_J = 0L;

               try {
                  ClassCache.UiHelper var4 = ClassCache.Inner_RenderState.putAt_FileStore_PacketHe_FileStore_IoHelper_2(
                     var3, this.fill_Set, var1.candidate()
                  );
                  return new ClassCache.Inner_TextFormat.Inner_DataUtil(var4, var3.flowState, var3.readEx_I, var3.value_I, var3.fill_J, var3.putAt_J);
               } finally {
                  if (!Thread.currentThread().isInterrupted()) {
                     this.putAt_ConcurrentLinkedQu.offer(var3);
                  }
               }
            }
         );
      }

      private void putAt_V_FileStore_IoPool_T(ClassCache.Inner_TextFormat.Inner_StreamUtil var1) {
         Future var2 = this.putAt_Map.remove(var1);
         if (var2 != null) {
            var2.cancel(true);
         }
      }

      private void fill_V() {
         for (Future var2 : this.putAt_Map.values()) {
            var2.cancel(true);
         }

         this.putAt_Map.clear();
      }

      public boolean putAt_Z_I(int var1) {
         if (var1 <= 0) {
            throw new IllegalArgumentException("Expansion budget must be positive");
         }

         for (int var2 = 0; var2 < var1 && !this.putAt_Z; var2++) {
            if ((var2 & 63) == 0) {
               if (Thread.currentThread().isInterrupted()) {
                  this.putAt_Z = true;
                  break;
               }

               if (this.putAt_FileStore_TextStor.putAt_J() != this.putAt_J) {
                  this.readEx_Z = true;
                  this.putAt_Z = true;
                  break;
               }
            }

            boolean var3 = this.putAt_PriorityQueue != null && !this.putAt_PriorityQueue.isEmpty();
            if (!var3 && this.putAt_DataCodec.init_Z() || this.prev_I >= 2000000 || this.closeOf_I + (var3 ? this.putAt_PriorityQueue.size() : 0) >= 3000000) {
               this.flushOf_Z = this.flushOf_Z | (var3 || !this.putAt_DataCodec.init_Z());
               this.putAt_Z = true;
               break;
            }

            if (!this.fill_Z()) {
               t.Inner_DataUtil var4 = this.putAt_DataCodec.init_t_DataUtil();
               if (!var4.init_Z) {
                  this.prev_I++;
                  BlockPos var5 = var4.init_BlockPos;
                  if (var4.init_D < this.putAt_DataCodec_CacheHel.init_D) {
                     this.putAt_DataCodec_CacheHel = var4;
                  }

                  this.putAt_FileStore_IoHelper.putAt_FileStore_PacketHe = var4.init_ClassCache_UiHelpe;
                  this.putAt_FileStore_IoHelper.putAt_DataCodec_CacheHel = var4;
                  if (this.putAt_FileStore_TokenCod.putAt_Z_I(var5.getX(), var5.getY(), var5.getZ(), this.putAt_FileStore_IoHelper)) {
                     this.putAt_DataCodec_CacheHel = var4;
                     this.fill_Z = true;
                     this.putAt_Z = true;
                     break;
                  }

                  if (this.chunkAt_Z && this.state_Z && this.putAt_Z_BlockPos_D(var5, var4.init_D) && this.putAt_Z_BlockPos(var5)) {
                     this.fill_DataCodec_CacheHel = var4;
                     this.putAt_Z = true;
                     break;
                  }

                  if (!this.chunkAt_Z
                     && this.putAt_FileStore_TokenCod.outsideSearchBox(this.putAt_BlockPos, 64, 40)
                     && var4.init_D
                        < this.putAt_FileStore_TokenCod.heuristic(this.putAt_BlockPos.getX(), this.putAt_BlockPos.getY(), this.putAt_BlockPos.getZ())
                     && (
                        Math.abs(var5.getX() - this.putAt_BlockPos.getX()) == 64
                           || Math.abs(var5.getZ() - this.putAt_BlockPos.getZ()) == 64
                           || Math.abs(var5.getY() - this.putAt_BlockPos.getY()) == 40
                     )) {
                     this.putAt_DataCodec_CacheHel = var4;
                     this.putAt_Z = true;
                     break;
                  }

                  this.putAt_List.clear();
                  int var6 = ClassCache.Inner_RenderState.putAt_I_FileStore_IoHelper(
                     this.putAt_FileStore_IoHelper, this.putAt_FileStore_CacheHel, this.fill_Set, var5, this.putAt_List
                  );
                  if ((var6 & 1) != 0) {
                     this.readEx_Z = true;
                  }

                  if ((var6 & 2) != 0 && var5.equals(this.putAt_BlockPos)) {
                     this.value_Z = true;
                  }

                  if ((var6 & 4) != 0 && var5.equals(this.putAt_BlockPos)) {
                     this.prevEx_Z = true;
                  }

                  this.chunkAt_I = this.chunkAt_I + this.putAt_List.size();

                  for (ClassCache.UiHelper var8 : this.putAt_List) {
                     this.putAt_V_DataCodec_CacheHel_FileStore_PacketHe(var4, var8);
                  }
               }
            }
         }

         if (this.putAt_Z) {
            this.fill_V();
         }

         return this.putAt_Z;
      }

      private boolean putAt_Z_BlockPos_D(BlockPos var1, double var2) {
         if (var1.equals(this.putAt_BlockPos)) {
            return true;
         }

         double var4 = this.putAt_FileStore_TokenCod.heuristic(this.putAt_BlockPos.getX(), this.putAt_BlockPos.getY(), this.putAt_BlockPos.getZ());
         if (var2 < var4) {
            return true;
         }

         BlockPos var6 = this.putAt_FileStore_TokenCod.putAt_BlockPos();
         return var2 == var4
            && var1.distToCenterSqr(var6.getX() + 0.5, var6.getY() + 0.5, var6.getZ() + 0.5)
               < this.putAt_BlockPos.distToCenterSqr(var6.getX() + 0.5, var6.getY() + 0.5, var6.getZ() + 0.5);
      }

      private void putAt_V_DataCodec_CacheHel_FileStore_PacketHe(t.Inner_DataUtil var1, ClassCache.UiHelper var2) {
         if ((this.putAt_Set.isEmpty() || !this.putAt_Set.contains(ClassCache.Inner_ConfigStore.of(var2))) && (this.tick_Z || !ClassCache.putAt_Z(var2))) {
            boolean var3 = false;

            for (BlockPos var5 : var2.cellsToBreak()) {
               if (!this.putAt_FileStore_TokenCod.fill_Z_BlockPos(var5)) {
                  var3 = true;
                  break;
               }
            }

            for (BlockPos var21 : var2.cellsToPlace()) {
               if (!this.putAt_FileStore_TokenCod.readEx_Z_BlockPos(var21)) {
                  var3 = true;
                  break;
               }
            }

            if (!var3) {
               BlockPos var20 = var2.to();
               if (this.putAt_FileStore_TokenCod.putAt_Z_BlockPos(var20)) {
                  if (!this.fill(var20)) {
                     if (this.chunkAt_Z) {
                        this.closeOf_Z = true;
                        this.flushOf_Z = true;
                     }
                  } else {
                     double var22 = (
                           this.putAt_FileStore_CacheHel.strategy() == ClassCache.Inner_IoWorker.load
                              ? this.putAt_FileStore_CacheHel.threats().surcharge(var20.getX(), var20.getY(), var20.getZ())
                                 + this.putAt_FileStore_CacheHel
                                    .threats()
                                    .putAt(var2.from().getX(), var2.from().getY(), var2.from().getZ(), var20.getX(), var20.getY(), var20.getZ())
                              : 0.0
                        )
                        + this.putAt_FileStore_CacheHel.threats().putAt(var20.getX(), var20.getY(), var20.getZ());
                     double var10000;
                     if (this.chunkAt_Z && this.putAt_FileStore_CacheHel.allowParkour()) {
                        switch (var2.type()) {
                           case init:
                           case load:
                           case read:
                           case write:
                           case update:
                              var10000 = var2.costTicks() * Math.min(1.0, 0.1 / this.putAt_FileStore_IoHelper.putAt_D);
                              break;
                           case sync:
                           case flush:
                           case close:
                           case open:
                           case poll:
                           case push:
                           case pop:
                           case tick:
                           case run:
                           case get:
                           case set:
                           case put:
                           case add:
                           case clear:
                           default:
                              var10000 = var2.costTicks();
                              break;
                           case reset:
                              var10000 = var2.costTicks() * ClassCache.Inner_CachePool.putAt_D_D(this.putAt_FileStore_IoHelper.putAt_D);
                        }
                     } else {
                        var10000 = var2.costTicks();
                     }

                     double var7 = var10000;
                     if (this.pollAt_Z) {
                        var7 += ClassCache.putAt_D(var2) - var2.costTicks();
                     }

                     double var9 = var1.load_D
                        + var7
                        + var22
                        + var2.cellsToPlace().size() * this.putAt_FileStore_TokenCod.putAt_D()
                        + ClassCache.Inner_CachePool.putAt_D_List(var2.cellsToPlace());
                     if (this.pollAt_Z) {
                        var9 += ClassCache.putAt_D_FileStore_PacketHe_FileStore_PacketHe(var1.init_ClassCache_UiHelpe, var2);
                     }

                     for (BlockPos var12 : var2.cellsToPlace()) {
                        if (FileCache.read_Z(var12)) {
                           var9 += 200.0;
                        }
                     }

                     for (BlockPos var25 : var2.cellsToBreak()) {
                        if (FileCache.load_Z(var25)) {
                           var9 += 60.0;
                        }

                        if (this.putAt_FileStore_TokenCod.value(var25)) {
                           double var13 = this.putAt_FileStore_IoHelper.putAt_FileStore_BitCache_BlockPos(var25).breakTicks();
                           if (Double.isFinite(var13)) {
                              var9 -= var13;
                           }
                        }
                     }

                     if (var9 < var1.load_D + 1.0) {
                        var9 = var1.load_D + 1.0;
                     }

                     if (Double.isFinite(var9) && !(var2.costTicks() <= 0.0) && !(var22 < 0.0)) {
                        int var24 = var1.load_I + var2.cellsToPlace().size();
                        if (var24 <= this.putAt_FileStore_CacheHel.constructionBlocks()) {
                           t.Inner_DataUtil var26 = this.putAt_Long2ObjectOpenHas.get(var20.asLong());
                           t.Inner_DataUtil var27 = null;
                           boolean var14 = false;
                           int var15 = this.pollOf_Z ? ClassCache.flowState(var2) : 0;

                           for (t.Inner_DataUtil var16 = var26; var16 != null; var16 = var16.load_t_DataUtil) {
                              if (!var16.init_Z && (!this.pollOf_Z || ClassCache.flowState(var16.init_ClassCache_UiHelpe) == var15)) {
                                 if (var16.load_I <= var24 && var16.load_D <= var9) {
                                    var14 = true;
                                    break;
                                 }

                                 if (var16.load_I == var24) {
                                    var27 = var16;
                                 }
                              }
                           }

                           if (!var14) {
                              if (var27 == null) {
                                 var27 = new t.Inner_DataUtil(var20, this.putAt_D_I(var20.getX(), var20.getY(), var20.getZ()));
                                 var27.load_I = var24;
                                 var27.load_t_DataUtil = var26;
                                 this.putAt_Long2ObjectOpenHas.put(var20.asLong(), var27);
                                 this.closeOf_I++;
                              }

                              for (t.Inner_DataUtil var28 = var26; var28 != null; var28 = var28.load_t_DataUtil) {
                                 if (var28 != var27
                                    && var28.load_I >= var24
                                    && var28.load_D >= var9
                                    && (!this.pollOf_Z || ClassCache.flowState(var28.init_ClassCache_UiHelpe) == var15)) {
                                    var28.init_Z = true;
                                 }
                              }

                              if (var9 < var27.load_D) {
                                 var27.load_D = var9;
                                 var27.init_t_DataUtil = var1;
                                 var27.init_ClassCache_UiHelpe = var2;
                                 this.putAt_DataCodec.init(var27);
                                 this.state_I = Math.max(this.state_I, this.putAt_DataCodec.init_I());

                                 for (int var29 = 0; var29 < putAt_DArr.length; var29++) {
                                    double var17 = var27.init_D + var9 / putAt_DArr[var29];
                                    if (var17 < this.fill_DArr[var29]) {
                                       this.fill_DArr[var29] = var17;
                                       this.putAt_DataCodec_CacheHelArr[var29] = var27;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      public ClassCache.Inner_PacketUtil init_ClassCache_PacketU() { return this.putAt_FileStore_StreamSt(); }
      public boolean init_Z() { return this.putAt_Z(); }
      public boolean init_Z_I(int budget) { return this.putAt_Z_I(budget); }
      public void init_V() { this.putAt_V(); }

            public ClassCache.Inner_PacketUtil putAt_FileStore_StreamSt() {
         if (!this.putAt_Z) {
            throw new IllegalStateException("Search still running");
         }

         if (this.chunkAt_Z && !this.fill_Z && this.fill_DataCodec_CacheHel == null) {
            if (ListPool.init_Z()) {
               ListPool.init(
                  "unproved route nearest=" + this.putAt_DataCodec_CacheHel.init_BlockPos + " unknown=" + this.state_Z + " pending=" + this.readEx_Z
               );
            }

            return new ClassCache.Inner_PacketUtil(
               List.of(), false, this.prev_I, this.readEx_Z, this.flushOf_Z || this.putAt_FileStore_IoHelper.fill_Z(), this.value_Z, this.prevEx_Z
            );
         } else {
            ArrayDeque var1 = new ArrayDeque();
            t.Inner_DataUtil var2 = this.chunkAt_Z && !this.fill_Z ? this.fill_DataCodec_CacheHel : this.putAt_DataCodec_CacheHel;
            if (!this.chunkAt_Z && !this.fill_Z && var2.init_BlockPos.distSqr(this.putAt_BlockPos) <= 25.0) {
               t.Inner_DataUtil var3 = this.putAt_DataCodec_CacheHel();
               if (var3 != null) {
                  var2 = var3;
               }
            }

            while (var2.init_t_DataUtil != null) {
               if (var1.size() >= this.closeOf_I) {
                  throw new IllegalStateException("Cyclic path parent chain");
               }

               var1.addFirst(var2.init_ClassCache_UiHelpe);
               var2 = var2.init_t_DataUtil;
            }

            return new ClassCache.Inner_PacketUtil(
               new ArrayList<>(var1),
               this.fill_Z,
               this.prev_I,
               this.readEx_Z,
               this.flushOf_Z || !this.fill_Z && this.putAt_FileStore_IoHelper.fill_Z(),
               this.value_Z,
               this.prevEx_Z,
               this.chunkAt_Z
                  && !this.fill_Z
                  && this.fill_DataCodec_CacheHel != null
                  && this.fill_DataCodec_CacheHel.init_BlockPos.equals(this.putAt_BlockPos)
            );
         }
      }

      List<ClassCache.UiHelper> putAt_List() {
         ArrayDeque var1 = new ArrayDeque();

         for (t.Inner_DataUtil var2 = this.putAt_DataCodec_CacheHel;
            var2.init_t_DataUtil != null && var1.size() < 64;
            var2 = var2.init_t_DataUtil
         ) {
            var1.addFirst(var2.init_ClassCache_UiHelpe);
         }

         return List.copyOf(var1);
      }

      public void putAt_V() {
         this.flushOf_Z = !this.putAt_Z;
         this.putAt_Z = true;
         this.fill_V();
      }

      public int flowState() {
         return this.prev_I;
      }

      public long putAt_J() {
         return this.putAt_FileStore_IoHelper.fill_J;
      }

      public int flowTemp() {
         return this.putAt_FileStore_IoHelper.readEx_I;
      }

      public int readEx() {
         return this.chunkAt_I;
      }

      public int value() {
         return this.state_I;
      }

      public long fill_J() {
         return this.putAt_FileStore_IoHelper.putAt_J;
      }

      private record Inner_DataUtil(ClassCache.UiHelper movement, int flags, int solves, int hits, long nanos, long queries) {
      }

      private record Inner_StreamUtil(
         ClassCache.Inner_RenderState.Inner_DataUtil candidate,
         BlockPos continuation,
         t.Inner_DataUtil source,
         double sourceCost,
         double priority,
         long order
      ) {
      }
   }

   public static final class Inner_CachePool {
      public static final double putAt_D = 4.5;
      public static final double fill_D = 6.4;
      public static final double readEx_D = 3.2;
      public static final double value_D = 4.5;
      public static final double prevEx = 2.9000000000000004;
      public static final double prev = 2.2;
      private static final ConcurrentHashMap<Long, Double> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
      public static final double chunkAt = 2.5;
      public static final double state = 3.6;
      public static final double closeOf = 25.0;
      public static final double pollAt = 32.0;
      public static final double pollOf = 41.0;
      public static final double tick = 255.0;
      public static final double flushOf = 400.0;
      public static final double stateOf = 2.0;
      public static final double store = 14.0;
      public static final double nextBy = 5.0;
      public static final double dataEx = 3.6;
      public static final double frameOf = 9.0;
      public static final double syncAt = 3.0;
      public static final double stopBy = 4.0;
      public static final double dataTo = 40.0;
      public static final double initAt = 200.0;
      public static final double setOf = 60.0;
      public static final double count = 12.0;
      public static final double getTo = 4.5;
      public static final double tableEx = 10.0;
      public static final double levelOf = 2.0;
      public static final double addTo = 5.5;
      public static final double task = 3.0;
      public static final double initEx = 5.0;
      public static final double poolEx = 0.5625;
      public static final double writeAt = 0.0625;
      public static final double reset = 1.0E-6;
      public static final double readBy = 1.25;
      public static final int flowState = 3;
      public static final int flowTemp = 8;
      public static final double entryAt = 0.25;
      public static final int readEx_I = 40;
      public static final int value_I = 2;
      public static final double prevTo = 60.0;
      public static final double init = 12.0;
      public static final double putBy = 8.0;
      public static final double table = 40.0;
      public static final double poolBy = 12.0;
      public static final double stateEx = 10.0;
      public static final double syncEx = 40.0;
      public static final double entryEx = 60.0;
      public static final double queue = 2.0;
      public static final double flagsEx = 1.0;
      public static final double flushBy = 8.0;
      public static final double cacheEx = 2.0;
      public static final double sync = 2.0;
      public static final double levelAt = 2.0;
      public static final double queueOf = 2.0;
      public static final double flushTo = 7.0;
      public static final double poolAt = 2.0;
      public static final double poolOf = 20.0;
      private static volatile long putAt_J;
      public static final double storeBy = 60.0;
      private static volatile Set<Long> putAt_Set = Set.of();
      private static volatile long fill_J;
      private static volatile long readEx_J;
      private static volatile long value_J;
      static final double writeOf = Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0)));
      static final double blockBy = Math.min(9.0 + writeOf, 12.0);

      private Inner_CachePool() {
      }

      static double putAt_D_D(double var0) {
         if (!(var0 > 0.100001)) {
            return 1.0;
         }

         long var2 = Math.round(var0 * 100000.0);
         Double var4 = putAt_ConcurrentHashMap.get(var2);
         if (var4 != null) {
            return var4;
         }

         double var5 = fill_D_D(var0) / fill_D_D(0.1);
         if (putAt_ConcurrentHashMap.size() < 64) {
            putAt_ConcurrentHashMap.put(var2, var5);
         }

         return var5;
      }

      static double fill_D_D(final double var0) {
         NodeWalker.Inner_CachePool var2 = new NodeWalker.Inner_CachePool() {
            @Override
            public double movementSpeed() {
               return var0;
            }

            @Override
            public List<AABB> collisions(AABB var1) {
               ArrayList<AABB> var2x = new ArrayList<>(4);

               for (int var3 = (int)Math.floor(var1.minX); var3 <= (int)Math.floor(var1.maxX); var3++) {
                  for (int var4 = (int)Math.floor(var1.minZ); var4 <= (int)Math.floor(var1.maxZ); var4++) {
                     AABB var5 = new AABB(var3, 63.0, var4, var3 + 1, 64.0, var4 + 1);
                     if (var5.intersects(var1)) {
                        var2x.add(var5);
                     }
                  }
               }

               return var2x;
            }
         };
         NodeWalker.Inner_StreamUtil var3 = new NodeWalker.Inner_StreamUtil(0.5, 64.0, 0.5, 0.0, -0.0784, 0.0, true, 0, false, false);
         double var4 = 0.0;

         for (int var6 = 0; var6 < 400; var6++) {
            if (var6 == 200) {
               var4 = var3.z();
            }

            var3 = NodeWalker.putAt(var3, var3.onGround() ? NodeWalker.Inner_DataUtil.jump(0.0F, true) : NodeWalker.Inner_DataUtil.run(0.0F, true), var2);
         }

         return 200.0 / Math.max(1.0E-6, var3.z() - var4);
      }

      // 5.3 external ABI bridges
      public static double init_D_I(int gap) { return putAt_D_I(gap); }
      public static double load_D_I(int gap) { return fill_D_I(gap); }
      public static void init_V_Set(Set<Long> cells) { putAt_V_Set(cells); }
      public static void init_V_J(long nanos) { putAt_V_J(nanos); }
      static boolean init() { return putAt(); }
      public static void load_V_J(long nanos) { fill_V_J(nanos); }
      static boolean load() { return fill(); }
      public static void read_V_J(long nanos) { readEx_V_J(nanos); }
      static boolean read() { return readEx(); }
      public static double init_D_F(float hardness, double speed, boolean correctTool) { return putAt_D_F(hardness, speed, correctTool); }
      public static double init_D_I(int dx, int dy, int dz) { return putAt_D_I(dx, dy, dz); }
      public static double load(int dx, int rise, int dz) { return fill(dx, rise, dz); }

      public static double putAt_D_I(int var0) {
         return 7.0 + (var0 + 1) * 3.2 + 2.0 + (var0 - 2);
      }

      public static double fill_D_I(int var0) {
         return putAt_D_I(var0) + 2.0;
      }

      public static void putAt_V_Set(Set<Long> var0) {
         putAt_Set = Set.copyOf(var0);
         fill_J = var0.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
      }

      static double putAt_D_List(List<BlockPos> var0) {
         if (!var0.isEmpty() && System.nanoTime() < fill_J) {
            var var1 = putAt_Set;
            double var2 = 0.0;

            for (BlockPos var5 : var0) {
               if (var1.contains(var5.asLong())) {
                  var2 += 60.0;
               }
            }

            return var2;
         } else {
            return 0.0;
         }
      }

      public static void putAt_V_J(long var0) {
         putAt_J = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean putAt() {
         return System.nanoTime() < putAt_J;
      }

      public static void fill_V_J(long var0) {
         readEx_J = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean fill() {
         return System.nanoTime() < readEx_J;
      }

      public static void readEx_V_J(long var0) {
         value_J = var0 <= 0L ? 0L : System.nanoTime() + var0;
      }

      static boolean readEx() {
         return System.nanoTime() < value_J;
      }

      public static double putAt_D_IoUtil_TextState(TableIndex.Inner_StateCache var0) {
         return 2.0 + var0.ticks() + Math.max(0.0, 0.2 - var0.margin()) * 10.0 + (var0.maneuver() == null ? 0 : 16);
      }

      public static double readEx_D_I(int var0) {
         return 5.0 + 3.0 * var0;
      }

      public static double value_D_I(int var0) {
         return 2.0 + var0;
      }

      public static double putAt_D_F(float var0, double var1, boolean var3) {
         if (var0 < 0.0F) {
            return 1.0 / 0.0;
         }

         if (!(var1 > 0.0) || !Double.isFinite(var1)) {
            return 1.0 / 0.0;
         }

         if (var0 == 0.0F) {
            return 6.0;
         }

         double var4 = var1 / var0 / (var3 ? 30.0 : 100.0);
         return var4 >= 1.0 ? 6.0 : Math.ceil(1.0 / var4) + 5.0;
      }

      public static int putAt_I_Str(String var0) {
         short var10000;
         switch (var0) {
            case "diamond_block":
            case "emerald_block":
            case "netherite_block":
            case "ancient_debris":
               var10000 = 4000;
               break;
            case "gold_block":
            case "iron_block":
            case "raw_iron_block":
            case "raw_gold_block":
            case "lapis_block":
               var10000 = 400;
               break;
            case "piston":
            case "sticky_piston":
            case "observer":
            case "redstone_block":
            case "powered_rail":
            case "repeater":
            case "comparator":
            case "crafting_table":
               var10000 = 200;
               break;
            case "infested_stone":
            case "infested_cobblestone":
            case "infested_stone_bricks":
            case "infested_mossy_stone_bricks":
            case "infested_cracked_stone_bricks":
            case "infested_chiseled_stone_bricks":
            case "infested_deepslate":
               var10000 = 2000;
               break;
            default:
               var10000 = 0;
         }

         return var10000;
      }

      public static double readEx_D_D(double var0) {
         return var0 + 2.0;
      }

      public static double value_D_D(double var0) {
         return var0 + 4.5;
      }

      public static int putAt_I_D(double var0) {
         return (int)Math.ceil(var0 * 3.0) + 20;
      }

      public static double putAt_D_I(int var0, int var1, int var2) {
         return Math.hypot(var0, var2) * Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0))) + Math.abs(var1) * Math.min(5.5, 4.0);
      }

      public static double fill(int var0, int var1, int var2) {
         double var3 = Math.sqrt((double)var0 * var0 + (double)var2 * var2) * writeOf;
         return var1 <= 0 ? var3 - var1 * Math.min(5.5, 4.0) : var3 + var1 * blockBy;
      }
   }

   public interface Inner_TaskQueue {
      boolean putAt_Z();

      int flowState();

      default int flowTemp() {
         return 0;
      }

      default List<ClassCache.UiHelper> putAt_List() {
         return List.of();
      }

      BlockPos putAt_BlockPos();
   }

   public record UiHelper(
      ClassCache.Inner_SyncManager type,
      BlockPos from,
      BlockPos to,
      List<BlockPos> cellsToBreak,
      List<BlockPos> cellsToPlace,
      double costTicks,
      double plannedBreakTicks,
      TableIndex.Inner_StateCache jump,
      boolean repairFarmland,
      TableIndex.Inner_StateCache paceJump
   ) {
      public UiHelper(
         ClassCache.Inner_SyncManager var1,
         BlockPos var2,
         BlockPos var3,
         List<BlockPos> var4,
         List<BlockPos> var5,
         double var6,
         double var8,
         TableIndex.Inner_StateCache var10,
         boolean var11
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, var11, null);
      }

      public UiHelper(
         ClassCache.Inner_SyncManager var1,
         BlockPos var2,
         BlockPos var3,
         List<BlockPos> var4,
         List<BlockPos> var5,
         double var6,
         double var8,
         TableIndex.Inner_StateCache var10
      ) {
         this(var1, var2, var3, var4, var5, var6, var8, var10, false);
      }

      public UiHelper(
         ClassCache.Inner_SyncManager type,
         BlockPos from,
         BlockPos to,
         List<BlockPos> cellsToBreak,
         List<BlockPos> cellsToPlace,
         double costTicks,
         double plannedBreakTicks,
         TableIndex.Inner_StateCache jump,
         boolean repairFarmland,
         TableIndex.Inner_StateCache paceJump
      ) {
         cellsToBreak = List.copyOf(cellsToBreak);
         cellsToPlace = List.copyOf(cellsToPlace);
         this.type = type;
         this.from = from;
         this.to = to;
         this.cellsToBreak = cellsToBreak;
         this.cellsToPlace = cellsToPlace;
         this.costTicks = costTicks;
         this.plannedBreakTicks = plannedBreakTicks;
         this.jump = jump;
         this.repairFarmland = repairFarmland;
         this.paceJump = paceJump;
      }

      public UiHelper(ClassCache.Inner_SyncManager var1, BlockPos var2, BlockPos var3, List<BlockPos> var4, List<BlockPos> var5, double var6, double var8) {
         this(var1, var2, var3, var4, var5, var6, var8, null);
      }

      UiHelper(ClassCache.Inner_SyncManager var1, BlockPos var2, BlockPos var3, List<BlockPos> var4, List<BlockPos> var5, double var6) {
         this(var1, var2, var3, var4, var5, var6, -1.0);
      }

      static ClassCache.UiHelper travel(ClassCache.Inner_SyncManager var0, BlockPos var1, BlockPos var2, double var3) {
         return new ClassCache.UiHelper(var0, var1, var2, List.of(), List.of(), var3);
      }

      static ClassCache.UiHelper jump(BlockPos var0, BlockPos var1, double var2, TableIndex.Inner_StateCache var4) {
         return new ClassCache.UiHelper(ClassCache.Inner_SyncManager.start, var0, var1, List.of(), List.of(), var2, -1.0, var4);
      }

      ClassCache.UiHelper withPaceJump(TableIndex.Inner_StateCache var1) {
         return new ClassCache.UiHelper(
            this.type, this.from, this.to, this.cellsToBreak, this.cellsToPlace, this.costTicks, this.plannedBreakTicks, this.jump, this.repairFarmland, var1
         );
      }
   }

   public record Inner_ByteReader(long elapsedNanos, int rounds, int expanded, long generated, long blockQueries, int peakOpen) {
   }

   enum Inner_NetHelper {
      init(4.0),
      load(3.999),
      read(32.0);

      final double putAt_D;

      Inner_NetHelper(double var3) {
         this.putAt_D = var3;
      }

      boolean putAt_Z() {
         return this == read;
      }
   }

   public record Inner_PacketUtil(
      List<ClassCache.UiHelper> movements,
      boolean reachesGoal,
      int expanded,
      boolean hitPending,
      boolean exhausted,
      boolean wedgedStart,
      boolean confinedStart,
      boolean awaitingTerrain
   ) {
      public Inner_PacketUtil(List<ClassCache.UiHelper> var1, boolean var2, int var3, boolean var4, boolean var5, boolean var6, boolean var7) {
         this(var1, var2, var3, var4, var5, var6, var7, false);
      }

      public Inner_PacketUtil(List<ClassCache.UiHelper> var1, boolean var2, int var3, boolean var4, boolean var5) {
         this(var1, var2, var3, var4, var5, false, false);
      }

      public Inner_PacketUtil(List<ClassCache.UiHelper> var1, boolean var2, int var3, boolean var4, boolean var5, boolean var6) {
         this(var1, var2, var3, var4, var5, var6, false);
      }

      public Inner_PacketUtil(
         List<ClassCache.UiHelper> movements,
         boolean reachesGoal,
         int expanded,
         boolean hitPending,
         boolean exhausted,
         boolean wedgedStart,
         boolean confinedStart,
         boolean awaitingTerrain
      ) {
         movements = List.copyOf(movements);
         this.movements = movements;
         this.reachesGoal = reachesGoal;
         this.expanded = expanded;
         this.hitPending = hitPending;
         this.exhausted = exhausted;
         this.wedgedStart = wedgedStart;
         this.confinedStart = confinedStart;
         this.awaitingTerrain = awaitingTerrain;
      }

      public BlockPos destination() {
         return this.movements.isEmpty() ? null : this.movements.get(this.movements.size() - 1).to();
      }

      public double totalCostTicks() {
         double var1 = 0.0;

         for (ClassCache.UiHelper var4 : this.movements) {
            var1 += var4.costTicks();
         }

         return var1;
      }
   }

   public enum Inner_SyncManager {
      init,
      load,
      read,
      write,
      update,
      sync,
      flush,
      close,
      open,
      poll,
      push,
      pop,
      tick,
      run,
      get,
      set,
      put,
      add,
      clear,
      reset,
      start,
      SLIME_BOUNCE,
      LADDER_JUMP,
      stop,
      next;
   }

   public static final class Inner_StreamUtil implements ClassCache.Inner_StateCache {
      // 5.3 external ABI bridges
      public long load_J() { return this.fill_J(); }
      public void init_V_2() { this.putAt_V_2(); }
      public void init_V_BlockPos(BlockPos focus) { this.putAt_V_BlockPos(focus); }
      public void init_V_ItemStackArr(ItemStack[] snapshot) { this.putAt_V_ItemStackArr(snapshot); }
      public void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) { this.putAt_V_BiPredicate(accepted); }
      public void init_V_J(long budgetNanos) { this.putAt_V_J(budgetNanos); }
      public static ClassCache.Inner_FileCache init_ClassCache_FileCac_Minecraft(Minecraft mc, ClientLevel level, BlockPos p, ItemStack[] hotbar) {
         return putAt_FileStore_BitCache_Minecraft(mc, level, p, hotbar);
      }
      public static boolean load(BlockState state) { return fill(state); }
      public static boolean read(BlockState state) { return prevEx(state); }
      public static boolean update(BlockState state) { return chunkAt(state); }
      public static boolean init_Z_BlockGetter_BlockPos(BlockGetter level, BlockPos target) { return putAt_Z_BlockGetter_BlockPos(level, target); }
      static boolean init_Z_ClientLevel_BlockPos(ClientLevel level, BlockPos target) { return putAt_Z_ClientLevel_BlockPos(level, target); }
      static boolean init_Z_Z_Z(boolean requiresCorrectTool, boolean correctToolAvailable) { return putAt_Z_Z_Z(requiresCorrectTool, correctToolAvailable); }
      public static boolean init_Z_Minecraft(Minecraft mc, BlockState state, int slots) { return putAt_Z_Minecraft(mc, state, slots); }
      private static final long putAt_J = 750000L;
      private final Object putAt_Obj = new Object();
      private volatile long fill_J;
      private volatile long readEx_J;
      private BiPredicate<BlockState, ItemStack> putAt_BiPredicate;
      private final Minecraft putAt_Minecraft;
      private final ClientLevel putAt_ClientLevel;
      private final int flowState;
      private final int flowTemp;
      private final LongSupplier putAt_LongSupplier;
      private volatile ItemStack[] putAt_ItemStackArr;
      private final ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 putAt_FileStore_TableCac_2;
      private final StateCache putAt_PacketHelper;
      private final ConcurrentHashMap<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
      private final Set<Long> putAt_Set = ConcurrentHashMap.newKeySet();
      private final Set<Long> fill_Set = ConcurrentHashMap.newKeySet();
      private final Map<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil> putAt_Map = new HashMap<>();
      private volatile BlockPos putAt_BlockPos = BlockPos.ZERO;
      private long value_J = -9223372036854775808L;
      private ClassCache.Inner_StreamUtil.Inner_DataUtil putAt_FileStore_TableCac;
      private int readEx_I;
      private int[] putAt_IArr;
      private long prevEx_J = -9223372036854775808L;
      private int value_I;
      private int prevEx_I;
      private int prev_I;
      private int chunkAt_I;
      private int state;
      private short[] putAt_SArr;
      private Map<Integer, String> fill_Map;
      private static final long prev_J = 1000000000L;
      private long chunkAt_J;

      @Override
      public long putAt_J() {
         return this.readEx_J;
      }

      public long fill_J() {
         return this.fill_J + this.putAt_J();
      }

      void putAt_V_J_J(long var1, long var3) throws java.lang.InterruptedException {
         synchronized (this.putAt_Obj) {
            while (this.fill_J() == var1) {
               long var6 = var3 - System.nanoTime();
               if (var6 <= 0L) {
                  return;
               }

               TimeUnit.NANOSECONDS.timedWait(this.putAt_Obj, var6);
            }
         }
      }

      private void fill_V_2() {
         synchronized (this.putAt_Obj) {
            this.fill_J++;
            this.putAt_Obj.notifyAll();
         }
      }

      public Inner_StreamUtil(Minecraft var1, BlockPos var2, BlockPos var3, ItemStack[] var4) {
         this(var1, var2, var3, var4, null);
      }

      public Inner_StreamUtil(final Minecraft var1, BlockPos var2, BlockPos var3, ItemStack[] var4, StateCache var5) {
         this.putAt_Minecraft = var1;
         this.putAt_LongSupplier = System::nanoTime;
         this.putAt_ClientLevel = var1.level;
         int var6 = this.putAt_ClientLevel.getMinY();
         int var7 = this.putAt_ClientLevel.getMaxY() - 1;
         this.flowState = Math.max(var6, Math.min(var2.getY(), var3.getY()) - 28);
         this.flowTemp = Math.min(var7, Math.max(var2.getY(), var3.getY()) + 28);
         this.putAt_ItemStackArr = var4;
         this.putAt_PacketHelper = var5;
         this.putAt_FileStore_TableCac_2 = new ClassCache.Inner_StreamUtil.Inner_StreamUtil_2() {
            @Override
            public ClassCache.Inner_FileCache putAt_FileStore_BitCache(BlockPos var1x) {
               return ClassCache.Inner_StreamUtil.putAt(
                  var1, Inner_StreamUtil.this.putAt_ClientLevel, var1x, Inner_StreamUtil.this.putAt_ItemStackArr, Inner_StreamUtil.this.putAt_BiPredicate
               );
            }

            @Override
            public boolean putAt(int var1x, int var2x) {
               return Inner_StreamUtil.this.putAt_ClientLevel.getChunkSource().getChunk(var1x, var2x, ChunkStatus.FULL, false) != null;
            }

            @Override
            public short putAt_S(BlockPos var1x) {
               return ClassCache.Inner_StreamUtil.putAt_S_ClientLevel_BlockPos(Inner_StreamUtil.this.putAt_ClientLevel, var1x);
            }

            @Override
            public byte putAt_B(BlockPos var1x) {
               return ClassCache.Inner_StreamUtil.putAt_B_ClientLevel_BlockPos(Inner_StreamUtil.this.putAt_ClientLevel, var1x);
            }
         };
      }

      Inner_StreamUtil(int var1, int var2, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 var3) {
         this(var1, var2, var3, null);
      }

      Inner_StreamUtil(int var1, int var2, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 var3, StateCache var4) {
         this(var1, var2, var3, var4, System::nanoTime);
      }

      @Override
      public double movementSpeed() {
         return this.putAt_Minecraft != null && this.putAt_Minecraft.player != null ? BitSetUtil.putAt_D_LivingEntity(this.putAt_Minecraft.player) : 0.1;
      }

      Inner_StreamUtil(int var1, int var2, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 var3, StateCache var4, LongSupplier var5) {
         this.putAt_LongSupplier = var5;
         this.putAt_Minecraft = null;
         this.putAt_ClientLevel = null;
         this.flowState = var1;
         this.flowTemp = var2;
         this.putAt_ItemStackArr = null;
         this.putAt_FileStore_TableCac_2 = var3;
         this.putAt_PacketHelper = var4;
      }

      public void putAt_V_BlockPos(BlockPos var1) {
         this.putAt_BlockPos = var1.immutable();
      }

      public void putAt_V_BiPredicate(BiPredicate<BlockState, ItemStack> var1) {
         if (this.putAt_BiPredicate != var1) {
            this.putAt_BiPredicate = var1;
            this.putAt_Set.addAll(this.putAt_ConcurrentHashMap.keySet());
            this.putAt_ConcurrentHashMap.clear();
            this.putAt_Map.clear();
            this.readEx_J++;
            this.putAt_FileStore_TableCac = null;
            this.value_J = -9223372036854775808L;
            this.fill_V_2();
         }
      }

      public void putAt_V_ItemStackArr(ItemStack[] var1) {
         ItemStack[] var2 = this.putAt_ItemStackArr;
         boolean var3 = var2 != null && var2.length == var1.length;
         if (var3) {
            for (int var4 = 0; var4 < var1.length; var4++) {
               if (!putAt_Z_ItemStack_ItemStack(var2[var4], var1[var4])) {
                  var3 = false;
                  break;
               }
            }
         }

         if (!var3 && putAt_Z_ItemStackArr_ItemStackArr(var2, var1) && System.nanoTime() - this.chunkAt_J < 1000000000L) {
            this.putAt_ItemStackArr = var1;
         } else {
            this.putAt_ItemStackArr = var1;
            if (!var3) {
               if (putAt_Z_ItemStackArr_ItemStackArr(var2, var1)) {
                  this.chunkAt_J = System.nanoTime();
               }

               this.putAt_Set.addAll(this.putAt_ConcurrentHashMap.keySet());
               this.putAt_ConcurrentHashMap.clear();
               this.putAt_Map.clear();
               this.readEx_J++;
               this.putAt_FileStore_TableCac = null;
               this.value_J = -9223372036854775808L;
               this.fill_V_2();
            }
         }
      }

      private static boolean putAt_Z_ItemStackArr_ItemStackArr(ItemStack[] var0, ItemStack[] var1) {
         if (var0 != null && var1 != null && var0.length == var1.length) {
            for (int var2 = 0; var2 < var0.length; var2++) {
               ItemStack var3 = var0[var2];
               ItemStack var4 = var1[var2];
               if (var3 != var4) {
                  if (var3 == null || var4 == null) {
                     return false;
                  }

                  boolean var5 = var3.isEmpty();
                  boolean var6 = var4.isEmpty();
                  if (!var5 && !var6) {
                     if (var3.getItem() != var4.getItem()) {
                        return false;
                     }

                     if (!Objects.equals(var3.getEnchantments(), var4.getEnchantments())) {
                        return false;
                     }

                     if (af.init(var3, 2) != af.init(var4, 2)) {
                        return false;
                     }
                  } else if (var5 != var6) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private static boolean putAt_Z_ItemStack_ItemStack(ItemStack var0, ItemStack var1) {
         if (var0 == var1) {
            return true;
         } else if (var0 != null && var1 != null) {
            boolean var2 = var0.isEmpty();
            boolean var3 = var1.isEmpty();
            return !var2 && !var3
               ? var0.getItem() == var1.getItem()
                  && var0.getCount() == var1.getCount()
                  && Objects.equals(var0.getEnchantments(), var1.getEnchantments())
                  && af.init(var0, 2) == af.init(var1, 2)
               : var2 == var3;
         } else {
            return false;
         }
      }

      @Override
      public ClassCache.Inner_FileCache probe(int var1, int var2, int var3) {
         if (var2 >= this.flowState && var2 <= this.flowTemp) {
            long var4 = putAt_J_I_I(var1 >> 4, var3 >> 4);
            ClassCache.Inner_StreamUtil.Inner_DataUtil var6 = this.putAt_ConcurrentHashMap.get(var4);
            if (var6 != null && var6.revision() == IoWorker.init(var4)) {
               if (var6.ready().get((var1 & 15) * 16 + (var3 & 15)) == 0) {
                  this.putAt_Set.add(var4);
                  this.fill_Set.add(putAt_J_I_I(var1, var3));
                  return null;
               }

               int var7 = this.flowTemp - this.flowState + 1;
               int var8 = var6.index(var1, var2, var3, this.flowState, var7);
               int var9 = var6.flags()[var8] & '\uffff';
               if ((var9 & 1) == 0) {
                  return ClassCache.Inner_FileCache.UNLOADED;
               }

               int var10 = var6.digX4()[var8] & '\uffff';
               return var9 == 3 && var6.top16()[var8] == 0 && var6.mult10()[var8] == 10 && var10 == 65535
                  ? ClassCache.Inner_FileCache.air()
                  : new ClassCache.Inner_FileCache(
                     true,
                     (var9 & 2) != 0,
                     var6.top16()[var8] & 31,
                     (var9 & 4) != 0,
                     (var9 & 8) != 0,
                     (var9 & 16) != 0,
                     (var9 & 32) != 0,
                     (var9 & 64) != 0,
                     (var9 & 128) != 0,
                     var6.mult10()[var8],
                     var10 == 65535 ? 1.0 / 0.0 : var10 / 4.0,
                     (var9 & 256) != 0,
                     (var9 & 512) != 0,
                     (var9 & 1024) != 0,
                     (var9 & 4096) != 0,
                     (var9 & 2048) != 0,
                     var6.penalties()[var8] & 65535,
                     (var9 & 8192) != 0,
                     (var9 & 16384) != 0,
                     (var9 & 32768) != 0,
                     (var6.top16()[var8] & 128) != 0,
                     (var6.top16()[var8] & 64) != 0,
                     (var6.top16()[var8] & 32) != 0
                  );
            } else {
               if (var6 != null) {
                  this.putAt_ConcurrentHashMap.remove(var4, var6);
               }

               this.putAt_Set.add(var4);
               this.fill_Set.add(putAt_J_I_I(var1, var3));
               return null;
            }
         } else {
            return ClassCache.Inner_FileCache.UNLOADED;
         }
      }

      private ClassCache.Inner_StreamUtil.Inner_DataUtil putAt_FileStore_TableCac_I_I(int var1, int var2) {
         long var3 = putAt_J_I_I(var1 >> 4, var2 >> 4);
         ClassCache.Inner_StreamUtil.Inner_DataUtil var5 = this.putAt_ConcurrentHashMap.get(var3);
         if (var5 != null && var5.revision() == IoWorker.init(var3) && var5.ready().get((var1 & 15) * 16 + (var2 & 15)) != 0) {
            return var5;
         }

         if (var5 != null && var5.revision() != IoWorker.init(var3)) {
            this.putAt_ConcurrentHashMap.remove(var3, var5);
         }

         this.putAt_Set.add(var3);
         this.fill_Set.add(putAt_J_I_I(var1, var2));
         return null;
      }

      @Override
      public short putAt_S_I(int var1, int var2, int var3) {
         if (var2 < this.flowState) {
            return 1;
         } else if (var2 > this.flowTemp) {
            return 0;
         } else {
            ClassCache.Inner_StreamUtil.Inner_DataUtil var4 = this.putAt_FileStore_TableCac_I_I(var1, var3);
            if (var4 == null) {
               return -1;
            } else {
               int var5 = var4.index(var1, var2, var3, this.flowState, this.flowTemp - this.flowState + 1);
               short var6 = var4.flags()[var5];
               if ((var6 & 1) == 0) {
                  return 1;
               } else {
                  return var4.shape() != null ? var4.shape()[var5] : e.init(true, (var6 & 2) != 0, var4.top16()[var5] & 31);
               }
            }
         }
      }

      @Override
      public byte putAt_B_I(int var1, int var2, int var3) {
         if (var2 >= this.flowState && var2 <= this.flowTemp) {
            ClassCache.Inner_StreamUtil.Inner_DataUtil var4 = this.putAt_FileStore_TableCac_I_I(var1, var3);
            if (var4 == null) {
               return m.init_m.init();
            } else {
               int var5 = var4.index(var1, var2, var3, this.flowState, this.flowTemp - this.flowState + 1);
               if (var4.surface() != null) {
                  return var4.surface()[var5];
               } else {
                  short var6 = var4.flags()[var5];
                  if ((var6 & 4096) != 0) {
                     return m.load_m.init();
                  } else {
                     return (var6 & 8192) != 0 ? m.sync.init() : m.init_m.init();
                  }
               }
            }
         } else {
            return m.init_m.init();
         }
      }

      public void putAt_V_2() {
         this.putAt_V_J(750000L);
      }

      public void putAt_V_J(long var1) {
         if (!this.putAt_Map.isEmpty()) {
            this.readEx();
         }

         long var3 = this.putAt_LongSupplier.getAsLong() + var1;
         int var5 = this.flowTemp - this.flowState + 1;

         while (this.putAt_LongSupplier.getAsLong() < var3) {
            Long var6 = this.putAt_Long();
            if (var6 != null) {
               this.fill_Set.remove(var6);
               int var7 = (int)(var6 >> 32);
               int var8 = var6.intValue();
               long var9 = putAt_J_I_I(var7 >> 4, var8 >> 4);
               if (this.putAt_FileStore_TableCac_2.putAt(var7 >> 4, var8 >> 4)) {
                  ClassCache.Inner_StreamUtil.Inner_DataUtil var20 = this.putAt_ConcurrentHashMap.get(var9);
                  if (var20 == null || var20.revision() != IoWorker.init(var9)) {
                     var20 = this.putAt_FileStore_TableCac_I_J(var5, var9);
                     this.putAt_ConcurrentHashMap.put(var9, var20);
                  }

                  int var12 = (var7 & 15) * 16 + (var8 & 15);
                  if (var20.ready().get(var12) == 0) {
                     this.putAt_V_FileStore_TableCac(var20, var7, var8, var5);
                     var20.ready().set(var12, 1);
                     this.fill_V_2();
                  }
                  continue;
               }
            }

            if (this.putAt_FileStore_TableCac != null && this.putAt_ConcurrentHashMap.get(this.value_J) != this.putAt_FileStore_TableCac) {
               this.putAt_FileStore_TableCac = null;
               this.value_J = -9223372036854775808L;
            }

            if (this.putAt_FileStore_TableCac == null) {
               Long var13 = this.fill_Long();
               if (var13 == null) {
                  if (this.prevEx_J != -9223372036854775808L) {
                     this.putAt_Z_J(var3);
                  }

                  return;
               }

               this.value_J = var13;
               this.readEx_I = 0;
               this.putAt_IArr = putAt_IArr_I_I(
                  Math.max(0, Math.min(15, this.putAt_BlockPos.getX() - ((int)(var13 >> 32) << 4))),
                  Math.max(0, Math.min(15, this.putAt_BlockPos.getZ() - (var13.intValue() << 4)))
               );
               this.putAt_FileStore_TableCac = this.putAt_ConcurrentHashMap.get(var13);
               if (this.putAt_FileStore_TableCac == null || this.putAt_FileStore_TableCac.revision() != IoWorker.init(var13)) {
                  this.putAt_FileStore_TableCac = this.putAt_FileStore_TableCac_I_J(var5, var13);
               }

               int var15 = (int)(var13 >> 32);
               int var17 = var13.intValue();
               if (!this.putAt_FileStore_TableCac_2.putAt(var15, var17)) {
                  ClassCache.Inner_StreamUtil.Inner_DataUtil var19 = this.putAt_PacketHelper == null
                     ? null
                     : this.putAt_PacketHelper.init_ClassCache_StreamU_I(var15, var17, this.flowState, var5, IoWorker.init(var13));
                  if (var19 != null) {
                     this.putAt_ConcurrentHashMap.put(var13, var19);
                     this.putAt_Map.put(var13, var19);
                  } else {
                     for (int var11 = 0; var11 < 256; var11++) {
                        this.putAt_FileStore_TableCac.ready().set(var11, 1);
                     }

                     this.putAt_ConcurrentHashMap.put(var13, this.putAt_FileStore_TableCac);
                     this.putAt_Map.put(var13, this.putAt_FileStore_TableCac);
                  }

                  this.putAt_Set.remove(var13);
                  this.fill_V_2();
                  this.putAt_FileStore_TableCac = null;
                  this.value_J = -9223372036854775808L;
                  continue;
               }

               this.putAt_ConcurrentHashMap.put(var13, this.putAt_FileStore_TableCac);
            }

            ClassCache.Inner_StreamUtil.Inner_DataUtil var14 = this.putAt_FileStore_TableCac;
            int var16 = (int)(this.value_J >> 32);
            int var18 = (int)this.value_J;

            while (this.readEx_I < 256 && this.putAt_LongSupplier.getAsLong() < var3) {
               int var10 = this.putAt_IArr[this.readEx_I];
               if (var14.ready().get(var10) == 0) {
                  this.putAt_V_FileStore_TableCac(var14, (var16 << 4) + var10 / 16, (var18 << 4) + var10 % 16, var5);
                  var14.ready().set(var10, 1);
                  this.fill_V_2();
               }

               this.readEx_I++;
            }

            if (this.readEx_I < 256) {
               return;
            }

            this.putAt_ConcurrentHashMap.put(this.value_J, var14);
            this.putAt_Set.remove(this.value_J);
            this.fill_V_2();
            if (this.putAt_PacketHelper != null && this.putAt_ClientLevel != null && var5 <= 128 && this.prevEx_J == -9223372036854775808L) {
               this.prevEx_J = this.value_J;
               this.value_I = var16;
               this.prevEx_I = var18;
               this.putAt_SArr = var14.flags();
               this.state = var14.revision();
               this.prev_I = 0;
               this.chunkAt_I = 0;
               this.fill_Map = null;
            }

            this.putAt_FileStore_TableCac = null;
            this.value_J = -9223372036854775808L;
         }
      }

      private void readEx() {
         Iterator var1 = this.putAt_Map.entrySet().iterator();

         while (var1.hasNext()) {
            Entry var2 = (Entry)var1.next();
            long var3 = (Long)var2.getKey();
            if (this.putAt_FileStore_TableCac_2.putAt((int)(var3 >> 32), (int)var3)) {
               this.putAt_ConcurrentHashMap.remove(var3, var2.getValue());
               this.putAt_Set.add(var3);
               var1.remove();
            }
         }
      }

      private boolean putAt_Z_J(long var1) {
         int var3 = this.flowTemp - this.flowState + 1;
         MutableBlockPos var4 = new MutableBlockPos();

         while (this.prev_I < 256) {
            if (this.putAt_LongSupplier.getAsLong() >= var1) {
               return false;
            }

            int var5 = this.prev_I >> 4;
            int var6 = this.prev_I & 15;

            while (this.chunkAt_I < var3) {
               int var7 = (var5 * 16 + var6) * var3 + this.chunkAt_I;
               short var8 = this.putAt_SArr[var7];
               if ((var8 & 1) != 0 && StateCache.init_I(var8) == 1) {
                  var4.set((this.value_I << 4) + var5, this.flowState + this.chunkAt_I, (this.prevEx_I << 4) + var6);
                  String var9 = BuiltInRegistries.BLOCK.getKey(this.putAt_ClientLevel.getBlockState(var4).getBlock()).getPath();
                  if (StateCache.init(var9)) {
                     if (this.fill_Map == null) {
                        this.fill_Map = new HashMap<>();
                     }

                     this.fill_Map.put(this.chunkAt_I * 256 + var6 * 16 + var5, "minecraft:" + var9);
                  }
               }

               this.chunkAt_I++;
            }

            this.chunkAt_I = 0;
            this.prev_I++;
         }

         long var12 = this.prevEx_J;
         int var13 = this.value_I;
         int var14 = this.prevEx_I;
         int var15 = this.state;
         short[] var10 = this.putAt_SArr;
         Map var11 = this.fill_Map;
         this.prevEx_J = -9223372036854775808L;
         this.putAt_SArr = null;
         this.fill_Map = null;
         if (var15 == IoWorker.init(var12)) {
            this.putAt_PacketHelper.init(var13, var14, this.flowState, var3, var10, var11);
         }

         return true;
      }

      private void putAt_V_FileStore_TableCac(ClassCache.Inner_StreamUtil.Inner_DataUtil var1, int var2, int var3, int var4) {
         MutableBlockPos var5 = new MutableBlockPos();

         for (int var6 = this.flowState; var6 <= this.flowTemp; var6++) {
            var5.set(var2, var6, var3);
            ClassCache.Inner_FileCache var7 = this.putAt_FileStore_TableCac_2.putAt_FileStore_BitCache(var5);
            int var8 = var1.index(var2, var6, var3, this.flowState, var4);
            if (!var7.loaded()) {
               var1.shape()[var8] = 1;
            } else {
               var1.shape()[var8] = this.putAt_FileStore_TableCac_2.putAt_S(var5);
               var1.surface()[var8] = this.putAt_FileStore_TableCac_2.putAt_B(var5);
               char var9 = 1;
               if (var7.passable()) {
                  var9 |= 2;
               }

               if (var7.water()) {
                  var9 |= 4;
               }

               if (var7.lava()) {
                  var9 |= '\b';
               }

               if (var7.climbable()) {
                  var9 |= 16;
               }

               if (var7.hazardFeet()) {
                  var9 |= ' ';
               }

               if (var7.hazardFloor()) {
                  var9 |= '@';
               }

               if (var7.falling()) {
                  var9 |= 128;
               }

               if (var7.door()) {
                  var9 |= 256;
               }

               if (var7.slabStair()) {
                  var9 |= 512;
               }

               if (var7.farmland()) {
                  var9 |= 1024;
               }

               if (var7.ice()) {
                  var9 |= 4096;
               }

               if (var7.soulSand()) {
                  var9 |= 8192;
               }

               if (var7.thorny()) {
                  var9 |= 16384;
               }

               if (var7.portal()) {
                  var9 |= '耀';
               }

               var1.flags()[var8] = (short)var9;
               var1.top16()[var8] = (byte)(var7.top16() | (var7.narrowFooting() ? 128 : 0) | (var7.feetTrigger() ? 64 : 0) | (var7.pressurePlate() ? 32 : 0));
               var1.mult10()[var8] = (byte)var7.fallMult10();
               var1.penalties()[var8] = (short)Math.min(65535, var7.destructionPenalty());
               var1.digX4()[var8] = var7.breakTicks() == 1.0 / 0.0 ? -1 : (short)Math.min(65534, (int)Math.round(var7.breakTicks() * 4.0));
            }
         }
      }

      private static int[] putAt_IArr_I_I(int var0, int var1) {
         int[] var2 = new int[256];
         int var3 = 0;

         for (int var4 = 0; var4 < 16; var4++) {
            for (int var5 = -var4; var5 <= var4; var5++) {
               for (int var6 = -var4; var6 <= var4; var6++) {
                  if (Math.max(Math.abs(var5), Math.abs(var6)) == var4) {
                     int var7 = var0 + var5;
                     int var8 = var1 + var6;
                     if (var7 >= 0 && var7 < 16 && var8 >= 0 && var8 < 16) {
                        var2[var3++] = var7 * 16 + var8;
                     }
                  }
               }
            }
         }

         return var2;
      }

      private ClassCache.Inner_StreamUtil.Inner_DataUtil putAt_FileStore_TableCac_I_J(int var1, long var2) {
         return new ClassCache.Inner_StreamUtil.Inner_DataUtil(
            new short[256 * var1],
            new byte[256 * var1],
            new short[256 * var1],
            new byte[256 * var1],
            IoWorker.init(var2),
            new short[256 * var1],
            new AtomicIntegerArray(256),
            new short[256 * var1],
            new byte[256 * var1]
         );
      }

      private Long putAt_Long() {
         Long var1 = null;
         long var2 = 9223372036854775807L;

         for (Long var5 : this.fill_Set) {
            long var6 = (int)(var5 >> 32) - this.putAt_BlockPos.getX();
            long var8 = var5.intValue() - this.putAt_BlockPos.getZ();
            long var10 = var6 * var6 + var8 * var8;
            if (var10 < var2 || var10 == var2 && (var1 == null || var5 < var1)) {
               var1 = var5;
               var2 = var10;
            }
         }

         return var1;
      }

      private Long fill_Long() {
         Long var1 = null;
         double var2 = 1.7976931348623157E308;

         for (Long var5 : this.putAt_Set) {
            int var6 = (int)(var5 >> 32);
            int var7 = (int)var5.longValue();
            double var8 = (var6 << 4) + 8 - this.putAt_BlockPos.getX();
            double var10 = (var7 << 4) + 8 - this.putAt_BlockPos.getZ();
            double var12 = var8 * var8 + var10 * var10;
            if (var12 < var2) {
               var2 = var12;
               var1 = var5;
            }
         }

         return var1;
      }

      private static long putAt_J_I_I(int var0, int var1) {
         return IoWorker.init_J(var0, var1);
      }

      public static ClassCache.Inner_FileCache putAt_FileStore_BitCache_Minecraft(Minecraft var0, ClientLevel var1, BlockPos var2, ItemStack[] var3) {
         return putAt(var0, var1, var2, var3, null);
      }

      private static ClassCache.Inner_FileCache putAt(Minecraft var0, ClientLevel var1, BlockPos var2, ItemStack[] var3, BiPredicate<BlockState, ItemStack> var4) {
         if (var1 != null
            && var1.isInsideBuildHeight(var2)
            && var1.getChunkSource().getChunk(var2.getX() >> 4, var2.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState var5 = var1.getBlockState(var2);
            boolean var6 = TextFormat.init(var1, var2);
            if (var5.isAir()) {
               return var6 ? ClassCache.Inner_FileCache.air().withHazards(true, false) : ClassCache.Inner_FileCache.air();
            }

            FluidState var7 = var5.getFluidState();
            boolean var8 = var7.is(FluidTags.WATER);
            boolean var9 = var7.is(FluidTags.LAVA);
            VoxelShape var10 = var5.getCollisionShape(var1, var2);
            boolean var11 = var10.isEmpty();
            Block var12 = var5.getBlock();
            boolean var13 = false;
            if (var12 instanceof DoorBlock) {
               var13 = DoorBlock.isWoodenDoor(var5);
               if (var13 && var5.getValue(BlockStateProperties.OPEN)) {
                  var11 = true;
               }
            } else if (var12 instanceof FenceGateBlock) {
               var13 = true;
               if (var5.getValue(BlockStateProperties.OPEN)) {
                  var11 = true;
               }
            }

            boolean var14 = putAt_Z_BlockState(var5) || var2.getY() > var1.getMinY() && putAt_Z_BlockState_BlockState(var5, var1.getBlockState(var2.below()));
            if (var14 && !(var12 instanceof ScaffoldingBlock)) {
               var11 = true;
            }

            int var15 = var11 ? 0 : (int)Math.round(var10.max(Axis.Y) * 16.0);
            boolean var16 = fill(var5) || var6;
            boolean var17 = readEx(var5);
            boolean var18 = value(var5);
            boolean var19 = var5.is(Blocks.SWEET_BERRY_BUSH);
            boolean var20 = prevEx(var5);
            boolean var21 = !var20 && !var11 && !putAt_Z_VoxelShape(var10);
            boolean var22 = prev(var5);
            String var23 = BuiltInRegistries.BLOCK.getKey(var12).getPath();
            int var24 = Math.round(putAt_F_Str_Z(var23, var8) * 10.0F);
            boolean var25 = var12 instanceof FallingBlock;
            boolean var26 = var12 instanceof SlabBlock || var12 instanceof StairBlock;
            boolean var27 = var12 instanceof FarmlandBlock;
            boolean var28 = var5.is(Blocks.ICE) || var5.is(Blocks.PACKED_ICE) || var5.is(Blocks.BLUE_ICE) || var5.is(Blocks.FROSTED_ICE);
            boolean var29 = var5.is(Blocks.SOUL_SAND);
            double var30 = 1.0 / 0.0;
            if ((!var11 || var12 instanceof WebBlock) && var7.isEmpty()) {
               float var32 = var5.getDestroySpeed(var1, var2);
               BlockState var33 = var1.getBlockState(var2.below());
               boolean var34 = putAt_Z_Z(
                  var5.is(Blocks.ICE) || var5.is(Blocks.FROSTED_ICE),
                  var33.isSolid(),
                  var33.liquid(),
                  var1.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
               );
               if (var32 >= 0.0F
                  && !var5.hasBlockEntity()
                  && !var34
                  && putAt_Z_Function_BlockPos(var1x -> putAt_FileStore_TableCac_ClientLevel_BlockPos(var1, var1x), var2)
                  && (var0.player.hasInfiniteMaterials() || !putAt_Z_Z_Z(var5.requiresCorrectToolForDrops(), putAt_Z_BlockState_ItemStackArr(var5, var3)))) {
                  r.Inner_DataUtil var35 = r.Inner_DataUtil.of(var0);
                  var30 = var4 == null ? putAt(var0, var5, var32, var3, var35) : putAt(var0, var5, var32, var3, var35, var4);

                  for (BlockPos var36 = var2.above(); var1.isInsideBuildHeight(var36); var36 = var36.above()) {
                     BlockState var37 = var1.getBlockState(var36);
                     if (!(var37.getBlock() instanceof FallingBlock)) {
                        break;
                     }

                     var30 += var4 == null
                        ? putAt(var0, var37, var37.getDestroySpeed(var1, var36), var3, var35)
                        : putAt(var0, var37, var37.getDestroySpeed(var1, var36), var3, var35, var4);
                  }
               }
            }

            return new ClassCache.Inner_FileCache(
               true,
               var11,
               var15,
               var8,
               var9,
               var14,
               var16,
               var20 || var21,
               var25,
               var24,
               var30,
               var13,
               var26,
               var27,
               var28,
               false,
               ClassCache.Inner_CachePool.putAt_I_Str(var23),
               var29,
               var19,
               var22,
               var21,
               var17,
               var18
            );
         } else {
            return ClassCache.Inner_FileCache.UNLOADED;
         }
      }

      public static boolean putAt_Z_VoxelShape(VoxelShape var0) {
         if (var0.isEmpty()) {
            return false;
         }

         double var1 = var0.max(Axis.Y);

         for (AABB var4 : var0.toAabbs()) {
            if (!(var4.maxY < var1 - 1.0E-6) && var4.maxX > 0.3 && var4.minX < 0.7 && var4.maxZ > 0.3 && var4.minZ < 0.7) {
               return true;
            }
         }

         return false;
      }

      static boolean putAt_Z_BlockState(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof LadderBlock
            || var1 instanceof VineBlock
            || var1 instanceof WeepingVinesBlock
            || var1 instanceof WeepingVinesPlantBlock
            || var1 instanceof TwistingVinesBlock
            || var1 instanceof TwistingVinesPlantBlock
            || var1 instanceof CaveVinesBlock
            || var1 instanceof CaveVinesPlantBlock
            || var1 instanceof ScaffoldingBlock
            || var0.is(BlockTags.CLIMBABLE);
      }

      static boolean putAt_Z_BlockState_BlockState(BlockState var0, BlockState var1) {
         return var0.getBlock() instanceof TrapDoorBlock
            && var0.getValue(TrapDoorBlock.OPEN)
            && var1 != null
            && var1.getBlock() instanceof LadderBlock
            && var1.getValue(LadderBlock.FACING) == var0.getValue(TrapDoorBlock.FACING);
      }

      public static boolean fill(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof BaseFireBlock
            || var0.is(Blocks.POWDER_SNOW)
            || var0.is(Blocks.WITHER_ROSE)
            || var0.is(Blocks.LAVA_CAULDRON)
            || var1 instanceof BubbleColumnBlock && var0.getValue(BubbleColumnBlock.DRAG_DOWN);
      }

      public static boolean readEx(BlockState var0) {
         return var0.is(Blocks.LIGHT) && var0.getValue(LightBlock.LEVEL) == 0;
      }

      public static boolean value(BlockState var0) {
         return var0.getBlock() instanceof BasePressurePlateBlock;
      }

      public static boolean prevEx(BlockState var0) {
         Block var1 = var0.getBlock();
         return var0.is(Blocks.MAGMA_BLOCK)
            || var0.is(Blocks.LAVA_CAULDRON)
            || var0.is(Blocks.CACTUS)
            || var1 instanceof CampfireBlock && var0.getValue(CampfireBlock.LIT)
            || var1 instanceof PointedDripstoneBlock
            || var1 instanceof SulfurSpikeBlock
            || var1 instanceof PotentSulfurBlock
            || var1 instanceof SculkShriekerBlock && var0.getValue(SculkShriekerBlock.CAN_SUMMON);
      }

      static boolean prev(BlockState var0) {
         Block var1 = var0.getBlock();
         return var1 instanceof NetherPortalBlock || var1 instanceof EndPortalBlock || var1 instanceof EndGatewayBlock;
      }

      public static short putAt_S_ClientLevel_BlockPos(ClientLevel var0, BlockPos var1) {
         if (var0 != null
            && var0.isInsideBuildHeight(var1)
            && var0.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState var2 = var0.getBlockState(var1);
            if (var2.isAir()) {
               return 0;
            } else {
               VoxelShape var3 = var2.getCollisionShape(var0, var1);
               if (var3.isEmpty()) {
                  return 0;
               } else {
                  return var3 == Shapes.block() ? 1 : e.init_S_List(var3.toAabbs());
               }
            }
         } else {
            return 1;
         }
      }

      public static byte putAt_B_ClientLevel_BlockPos(ClientLevel var0, BlockPos var1) {
         if (var0 != null && var0.isInsideBuildHeight(var1)) {
            Block var2 = var0.getBlockState(var1).getBlock();
            double var3 = var2 instanceof SlimeBlock ? 1.0 : (var2 instanceof BedBlock ? 0.75 : 0.0);
            return m.init(var2.getFriction(), var2.getSpeedFactor(), var2.getJumpFactor(), var3).init();
         } else {
            return m.init_m.init();
         }
      }

      static boolean putAt_Z_Function_BlockPos(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> var0, BlockPos var1) {
         return !value(var0, var1) && !readEx(var0, var1) && putAt_I_Function_BlockPos(var0, var1) <= 6 && !fill(var0, var1);
      }

      static boolean fill(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> var0, BlockPos var1) {
         BlockPos var2 = var1.above();

         for (int var3 = 0; var3 < 384; var3++) {
            ClassCache.Inner_StreamUtil.Inner_CachePool var4 = (ClassCache.Inner_StreamUtil.Inner_CachePool)var0.apply(var2);
            if (var4 == null || !var4.falling()) {
               return false;
            }

            if (readEx(var0, var2)) {
               return true;
            }

            var2 = var2.above();
         }

         return false;
      }

      static boolean readEx(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> var0, BlockPos var1) {
         for (int[] var5 : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            ClassCache.Inner_StreamUtil.Inner_CachePool var6 = (ClassCache.Inner_StreamUtil.Inner_CachePool)var0.apply(var1.offset(var5[0], var5[1], var5[2]));
            if (var6 != null && var6.water()) {
               return true;
            }
         }

         return false;
      }

      static boolean value(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> var0, BlockPos var1) {
         for (int[] var5 : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            ClassCache.Inner_StreamUtil.Inner_CachePool var6 = (ClassCache.Inner_StreamUtil.Inner_CachePool)var0.apply(var1.offset(var5[0], var5[1], var5[2]));
            if (var6 != null && var6.lava()) {
               return true;
            }
         }

         return false;
      }

      static int putAt_I_Function_BlockPos(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> var0, BlockPos var1) {
         int var2 = 0;
         BlockPos var3 = var1.above();

         for (int var4 = 0; var4 < 384; var4++) {
            ClassCache.Inner_StreamUtil.Inner_CachePool var5 = (ClassCache.Inner_StreamUtil.Inner_CachePool)var0.apply(var3);
            if (var5 == null || !var5.falling()) {
               return var2;
            }

            var2++;
            var3 = var3.above();
         }

         return var2;
      }

      static boolean putAt_Z_ClientLevel_BlockPos(ClientLevel var0, BlockPos var1) {
         return putAt_Z_Function_BlockPos(var1x -> putAt_FileStore_TableCac_ClientLevel_BlockPos(var0, var1x), var1);
      }

      public static boolean putAt_Z_Z(boolean var0, boolean var1, boolean var2, boolean var3) {
         return var0 && !var3 && (var1 || var2);
      }

      public static boolean putAt_Z_BlockGetter_BlockPos(BlockGetter var0, BlockPos var1) {
         try {
            for (Direction var5 : Direction.values()) {
               if (var0.getBlockState(var1.relative(var5)).is(Blocks.NETHER_PORTAL)) {
                  return true;
               }
            }

            return false;
         } catch (RuntimeException var6) {
            return false;
         }
      }

      static boolean putAt_Z_ClientLevel(ClientLevel var0, BlockPos var1, Set<BlockPos> var2) {
         return var2.isEmpty()
            ? putAt_Z_ClientLevel_BlockPos(var0, var1)
            : putAt_Z_Function_BlockPos(
               var2x -> {
                  ClassCache.Inner_StreamUtil.Inner_CachePool var3 = putAt_FileStore_TableCac_ClientLevel_BlockPos(var0, var2x);
                  return var3 != null && var3.water() && var2.contains(var2x)
                     ? new ClassCache.Inner_StreamUtil.Inner_CachePool(var3.falling(), var3.open(), var3.lava(), false)
                     : var3;
               },
               var1
            );
      }

      private static ClassCache.Inner_StreamUtil.Inner_CachePool putAt_FileStore_TableCac_ClientLevel_BlockPos(ClientLevel var0, BlockPos var1) {
         if (!var0.isInsideBuildHeight(var1)) {
            return null;
         }

         if (var0.getChunkSource().getChunk(var1.getX() >> 4, var1.getZ() >> 4, ChunkStatus.FULL, false) == null) {
            return new ClassCache.Inner_StreamUtil.Inner_CachePool(false, false, true);
         }

         BlockState var2 = var0.getBlockState(var1);
         return new ClassCache.Inner_StreamUtil.Inner_CachePool(
            var2.getBlock() instanceof FallingBlock,
            var2.getCollisionShape(var0, var1).isEmpty(),
            var2.getFluidState().is(FluidTags.LAVA),
            var2.getFluidState().is(FluidTags.WATER)
         );
      }

      public static boolean chunkAt(BlockState var0) {
         return var0.is(BlockTags.IRON_ORES)
            || var0.is(BlockTags.GOLD_ORES)
            || var0.is(BlockTags.COPPER_ORES)
            || BuiltInRegistries.BLOCK.getKey(var0.getBlock()).getPath().endsWith("_ore");
      }

      static boolean putAt_Z_Z_Z(boolean var0, boolean var1) {
         return var0 && !var1;
      }

      private static boolean putAt_Z_ItemStack_BlockState(ItemStack var0, BlockState var1) {
         return var0 != null && !var0.isEmpty() && var0.isCorrectToolForDrops(var1) && af.init(var0, 2);
      }

      static boolean putAt_Z_BlockState_ItemStackArr(BlockState var0, ItemStack[] var1) {
         for (ItemStack var5 : var1) {
            if (putAt_Z_ItemStack_BlockState(var5, var0)) {
               return true;
            }
         }

         return false;
      }

      public static boolean putAt_Z_Minecraft(Minecraft var0, BlockState var1, int var2) {
         for (int var3 = 0; var3 < var2; var3++) {
            if (putAt_Z_ItemStack_BlockState(var0.player.getInventory().getItem(var3), var1)) {
               return true;
            }
         }

         return false;
      }

      static float putAt_F_Str_Z(String var0, boolean var1) {
         if (var1) {
            return 0.0F;
         }

         float var10000;
         switch (var0) {
            case "cobweb":
            case "powder_snow":
               var10000 = 0.0F;
               break;
            case "hay_block":
            case "honey_block":
               var10000 = 0.2F;
               break;
            case "slime_block":
               var10000 = 0.0F;
               break;
            default:
               var10000 = var0.endsWith("_bed") ? 0.5F : 1.0F;
         }

         return var10000;
      }

      private static double putAt_D_Minecraft(Minecraft var0, BlockState var1, float var2, ItemStack[] var3) {
         return putAt(var0, var1, var2, var3, r.Inner_DataUtil.of(var0));
      }

      static double putAt(Minecraft var0, BlockState var1, float var2, ItemStack[] var3, r.Inner_DataUtil var4) {
         double var5 = r.init_D_Minecraft(var0, ItemStack.EMPTY, var1, var2, var4);

         for (ItemStack var10 : var3) {
            if (var10 != null && !var10.isEmpty() && af.init(var10, 2)) {
               var5 = Math.min(var5, r.init_D_Minecraft(var0, var10, var1, var2, var4));
            }
         }

         return var5;
      }

      static double putAt(
         Minecraft var0, BlockState var1, float var2, ItemStack[] var3, r.Inner_DataUtil var4, BiPredicate<BlockState, ItemStack> var5
      ) {
         var4 = putAt_ByteWorker_CacheHe_ByteWorker_CacheHe(var4, var3, var1, var5);
         boolean var6 = var1.requiresCorrectToolForDrops() && !var4.creative();
         double var7 = !var6 && var5.test(var1, ItemStack.EMPTY) ? r.init_D_Minecraft(var0, ItemStack.EMPTY, var1, var2, var4) : 1.0 / 0.0;

         for (ItemStack var12 : var3) {
            if (var12 != null && !var12.isEmpty() && af.init(var12, 2) && (!var6 || var12.isCorrectToolForDrops(var1)) && var5.test(var1, var12)) {
               var7 = Math.min(var7, r.init_D_Minecraft(var0, var12, var1, var2, var4));
            }
         }

         return var7;
      }

      static r.Inner_DataUtil putAt_ByteWorker_CacheHe_ByteWorker_CacheHe(
         r.Inner_DataUtil var0, ItemStack[] var1, BlockState var2, BiPredicate<BlockState, ItemStack> var3
      ) {
         if (!var0.stonePick()) {
            return var0;
         }

         for (ItemStack var7 : var1) {
            if (var7 != null && var7.is(Items.STONE_PICKAXE) && af.init(var7, 2) && var3.test(var2, var7)) {
               return var0;
            }
         }

         return new r.Inner_DataUtil(var0.haste(), var0.fatigue(), var0.blockSpeed(), var0.creative(), var0.ironReserve(), false);
      }

      record Inner_DataUtil(
         short[] flags, byte[] top16, short[] digX4, byte[] mult10, int revision, short[] penalties, AtomicIntegerArray ready, short[] shape, byte[] surface
      ) {
         static final int LOADED = 1;
         static final int PASSABLE = 2;
         static final int WATER = 4;
         static final int LAVA = 8;
         static final int CLIMB = 16;
         static final int HAZ_FEET = 32;
         static final int HAZ_FLOOR = 64;
         static final int FALLING = 128;
         static final int DOOR = 256;
         static final int SLAB_STAIR = 512;
         static final int FARMLAND = 1024;
         static final int CACHED = 2048;
         static final int ICE = 4096;
         static final int SOUL_SAND = 8192;
         static final int THORNY = 16384;
         static final int PORTAL = 32768;
         static final int NARROW_FOOTING = 128;
         static final int FEET_TRIGGER = 64;
         static final int PRESSURE_PLATE = 32;
         static final int HEIGHT_MASK = 31;

         Inner_DataUtil(short[] var1, byte[] var2, short[] var3, byte[] var4, int var5) {
            this(var1, var2, var3, var4, var5, new short[var1.length], allReady(), null, null);
         }

         private static AtomicIntegerArray allReady() {
            int[] var0 = new int[256];
            Arrays.fill(var0, 1);
            return new AtomicIntegerArray(var0);
         }

         int index(int var1, int var2, int var3, int var4, int var5) {
            return ((var1 & 15) * 16 + (var3 & 15)) * var5 + (var2 - var4);
         }
      }

      record Inner_CachePool(boolean falling, boolean open, boolean lava, boolean water) {
         Inner_CachePool(boolean var1, boolean var2, boolean var3) {
            this(var1, var2, var3, false);
         }
      }

      interface Inner_StreamUtil_2 {
         ClassCache.Inner_FileCache putAt_FileStore_BitCache(BlockPos var1);

         boolean putAt(int var1, int var2);

         default short putAt_S(BlockPos var1) {
            return e.init_S_ClassCache_FileCac(this.putAt_FileStore_BitCache(var1));
         }

         default byte putAt_B(BlockPos var1) {
            return m.init(this.putAt_FileStore_BitCache(var1)).init();
         }
      }
   }

   public static final class Inner_BufferHelper {
      static final long putAt_J = 4000000000L;
      static final long fill_J = 10000000000L;
      private static final int flowState = 32;
      private long readEx_J;
      private static final long value_J = 30000000000L;
      private static final double putAt_D = 64.0;
      private final ClassCache.Inner_StateCache putAt_FileStore_TextStor;
      private final LongSupplier putAt_LongSupplier;
      private final Map<Long, ClassCache.Inner_BufferHelper.Inner_DataUtil> putAt_Map = new ConcurrentHashMap<>();
      private final Set<Long> putAt_Set = this.putAt_Map.keySet();
      private final Map<ClassCache.Inner_ConfigStore, ClassCache.Inner_BufferHelper.Inner_DataUtil> fill_Map = new HashMap<>();
      private final AtomicInteger putAt_AtomicInteger = new AtomicInteger();
      private volatile boolean putAt_Z;
      private volatile ClassCache.Inner_BufferHelper.Inner_StreamUtil putAt_FileStore_TaskStat;
      private volatile String putAt_Str;
      private volatile ClassCache.Inner_ByteReader putAt_FileStore_RingBuff;
      private volatile ClassCache.Inner_EventBus putAt_FileStore_EventBus;
      private final boolean fill_Z;
      private final double fill_D;
      private final boolean readEx_Z;
      private ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod = ClassCache.Inner_JsonUtil.putAt_FileStore_TokenCod_BlockPos_D(BlockPos.ZERO, 1.0);
      private ClassCache.Inner_DataUtil putAt_FileStore_CacheHel = ClassCache.Inner_DataUtil.WALK_ONLY;
      private Thread putAt_Thread;
      private BlockPos putAt_BlockPos = BlockPos.ZERO;
      private ClassCache.Inner_PacketUtil putAt_FileStore_StreamSt;
      private ClassCache.Inner_PacketUtil fill_FileStore_StreamSt;
      private ClassCache.Inner_PacketUtil readEx_FileStore_StreamSt;
      private boolean value_Z;
      private long prevEx_J;
      private volatile long prev;
      private volatile boolean prevEx_Z;

      public ClassCache.Inner_EventBus putAt_FileStore_EventBus() {
         return this.putAt_FileStore_EventBus;
      }

      public ClassCache.Inner_ByteReader putAt_FileStore_RingBuff() {
         return this.putAt_FileStore_RingBuff;
      }

      String putAt_Str() {
         return this.putAt_Str;
      }

      public Inner_BufferHelper(ClassCache.Inner_StateCache var1) {
         this(var1, System::nanoTime, false);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache var1, boolean var2) {
         this(var1, System::nanoTime, var2);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache var1, boolean var2, double var3) {
         this(var1, var2, var3, false);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache var1, boolean var2, double var3, boolean var5) {
         this(var1, System::nanoTime, var2, var3, var5);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache var1, LongSupplier var2) {
         this(var1, var2, false);
      }

      private Inner_BufferHelper(ClassCache.Inner_StateCache var1, LongSupplier var2, boolean var3) {
         this(var1, var2, var3, 0.0 / 0.0, false);
      }

      private Inner_BufferHelper(ClassCache.Inner_StateCache var1, LongSupplier var2, boolean var3, double var4, boolean var6) {
         this.fill_Z = var3;
         this.putAt_FileStore_TextStor = var1;
         this.putAt_LongSupplier = var2;
         this.fill_D = var4;
         this.readEx_Z = var6;
      }

      double putAt_D() {
         return Double.isNaN(this.fill_D) ? this.putAt_FileStore_TextStor.movementSpeed() : this.fill_D;
      }

      public void putAt_V_BlockPos_2(BlockPos var1, ClassCache.Inner_JsonUtil var2, ClassCache.Inner_DataUtil var3) {
         this.putAt_Z = false;
         this.putAt_FileStore_TokenCod = var2;
         this.putAt_FileStore_CacheHel = var3;
         this.putAt_FileStore_StreamSt = null;
         this.fill_FileStore_StreamSt = null;
         this.readEx_FileStore_StreamSt = null;
         this.value_Z = false;
         this.readEx(var1);
      }

      public void putAt(ClassCache.Inner_DataUtil var1) {
         this.putAt_FileStore_CacheHel = var1;
      }

      public synchronized void putAt(ClassCache.Inner_JsonUtil var1) {
         if (this.putAt_FileStore_TokenCod != var1) {
            this.putAt_FileStore_TokenCod = var1;
            if (this.putAt_FileStore_StreamSt == null) {
               this.putAt_AtomicInteger.incrementAndGet();
               this.putAt_FileStore_TaskStat = null;
               this.readEx_FileStore_StreamSt = this.fill_FileStore_StreamSt = null;
               this.value_Z = false;
               if (this.putAt_Thread != null) {
                  this.putAt_Thread.interrupt();
               }
            } else {
               if (this.fill_FileStore_StreamSt != null) {
                  BlockPos var2 = this.fill_FileStore_StreamSt.destination();
                  if (var2 == null || !var1.isInGoal(var2.getX(), var2.getY(), var2.getZ())) {
                     this.fill_FileStore_StreamSt = null;
                  }
               }

               if (this.value_Z) {
                  this.putAt_AtomicInteger.incrementAndGet();
                  this.putAt_FileStore_TaskStat = null;
                  if (this.putAt_Thread != null) {
                     this.putAt_Thread.interrupt();
                  }

                  this.value_Z = false;
               }
            }
         }
      }

      public void putAt(BlockPos var1) {
         this.fill_FileStore_StreamSt = null;
         this.putAt_FileStore_StreamSt = null;
         this.readEx_FileStore_StreamSt = null;
         this.value_Z = false;
         this.prevEx_J = System.nanoTime() + 200000000L;
         this.readEx(var1);
      }

      public void putAt_V_BlockPos_BlockPos(BlockPos var1, BlockPos var2) {
         this.putAt_Map
            .put(
               var1.asLong(),
               new ClassCache.Inner_BufferHelper.Inner_DataUtil(this.putAt_LongSupplier.getAsLong() + 30000000000L, var2.getX(), var2.getY(), var2.getZ())
            );
      }

      void putAt_V_FileStore_PacketHe_BlockPos(ClassCache.UiHelper var1, BlockPos var2) {
         this.fill_Map
            .put(
               ClassCache.Inner_ConfigStore.of(var1),
               new ClassCache.Inner_BufferHelper.Inner_DataUtil(this.putAt_LongSupplier.getAsLong() + 30000000000L, var2.getX(), var2.getY(), var2.getZ())
            );
      }

      public int flowState() {
         return this.putAt_Map.size();
      }

      Set<Long> putAt_Set() {
         return this.putAt_Set;
      }

      public boolean putAt_Z() {
         return this.readEx_FileStore_StreamSt == null && this.putAt_Thread != null && this.putAt_Thread.isAlive() || this.putAt_FileStore_TaskStat != null;
      }

      public boolean fill_Z() {
         return this.value_Z && this.fill_FileStore_StreamSt == null;
      }

      public boolean putAt(long var1) {
         if (this.putAt_FileStore_TextStor instanceof ClassCache.Inner_StreamUtil) {
            return this.putAt_FileStore_TaskStat != null;
         }

         if (System.nanoTime() - this.prev > var1) {
            return this.putAt_FileStore_TaskStat != null;
         }

         long var3 = System.nanoTime() + var1;
         synchronized (this) {
            while (this.putAt_FileStore_TaskStat == null && this.putAt_Str == null && !this.putAt_Z) {
               Thread var6 = this.putAt_Thread;
               if (var6 == null || !var6.isAlive()) {
                  break;
               }

               long var7 = var3 - System.nanoTime();
               if (var7 <= 0L) {
                  break;
               }

               try {
                  this.wait(var7 / 1000000L, (int)(var7 % 1000000L));
               } catch (InterruptedException var11) {
                  Thread.currentThread().interrupt();
                  break;
               }
            }

            return this.putAt_FileStore_TaskStat != null;
         }
      }

      public boolean fill(long var1) {
         long var3 = System.nanoTime() + var1;
         synchronized (this) {
            while (this.putAt_FileStore_TaskStat == null && this.putAt_Str == null && !this.putAt_Z) {
               Thread var6 = this.putAt_Thread;
               if (var6 == null || !var6.isAlive()) {
                  break;
               }

               long var7 = var3 - System.nanoTime();
               if (var7 <= 0L) {
                  break;
               }

               try {
                  this.wait(var7 / 1000000L, (int)(var7 % 1000000L));
               } catch (InterruptedException var11) {
                  Thread.currentThread().interrupt();
                  break;
               }
            }

            return this.putAt_FileStore_TaskStat != null || this.putAt_Str != null || this.putAt_Z || this.putAt_Thread == null || !this.putAt_Thread.isAlive();
         }
      }

      public ClassCache.Inner_PacketUtil putAt_FileStore_StreamSt() {
         ClassCache.Inner_PacketUtil var1 = this.readEx_FileStore_StreamSt;
         this.readEx_FileStore_StreamSt = null;
         return var1;
      }

      public void putAt(ClassCache.Inner_PacketUtil var1) {
         this.putAt_FileStore_StreamSt = var1;
         if (this.fill_FileStore_StreamSt == var1) {
            this.fill_FileStore_StreamSt = null;
         }
      }

      void fill(ClassCache.Inner_PacketUtil var1) {
         this.putAt_V();
         this.putAt_Z = false;
         this.value_Z = false;
         this.putAt(var1);
      }

      public ClassCache.Inner_PacketUtil fill_FileStore_StreamSt() {
         return this.fill_FileStore_StreamSt;
      }

      public ClassCache.Inner_PacketUtil readEx() {
         ClassCache.Inner_PacketUtil var1 = this.fill_FileStore_StreamSt;
         this.fill_FileStore_StreamSt = null;
         return var1;
      }

      public void putAt(ClassCache.Inner_TaskQueue var1) {
         this.fill(var1.putAt_BlockPos());
         ClassCache.Inner_BufferHelper.Inner_StreamUtil var2 = this.putAt_FileStore_TaskStat;
         if (var2 != null) {
            if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
               ListPool.init(
                  "driver result gen="
                     + var2.generation()
                     + " currentGen="
                     + this.putAt_AtomicInteger.get()
                     + " from="
                     + var2.from()
                     + " current="
                     + (this.putAt_FileStore_StreamSt == null ? null : this.putAt_FileStore_StreamSt.destination())
               );
            }

            if (var2.generation() == this.putAt_AtomicInteger.get()) {
               this.putAt_FileStore_TaskStat = null;
               if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                  ListPool.init(
                     "driver plan done from="
                        + var2.from()
                        + " current="
                        + (this.putAt_FileStore_StreamSt == null ? null : this.putAt_FileStore_StreamSt.destination())
                        + " moves="
                        + var2.segment().movements().size()
                        + " segment="
                        + var2.segment().movements()
                  );
               }

               if (this.putAt_FileStore_StreamSt == null) {
                  this.readEx_FileStore_StreamSt = var2.segment();
               } else {
                  this.value_Z = false;
                  var var3 = var2.segment().movements();
                  if (var2.from().equals(this.putAt_FileStore_StreamSt.destination())
                     && (var3.isEmpty() || ((ClassCache.UiHelper)var3.get(0)).from().equals(this.putAt_FileStore_StreamSt.destination()))) {
                     this.fill_FileStore_StreamSt = var2.segment();
                     if (this.fill_FileStore_StreamSt.awaitingTerrain()) {
                        this.readEx_J = System.nanoTime() + 250000000L;
                     }
                  }
               }
            } else {
               this.putAt_FileStore_TaskStat = null;
            }
         }

         if (this.fill_FileStore_StreamSt != null && this.fill_FileStore_StreamSt.awaitingTerrain() && System.nanoTime() >= this.readEx_J) {
            this.fill_FileStore_StreamSt = null;
         }

         if (this.putAt_FileStore_StreamSt != null && this.fill_FileStore_StreamSt == null && !this.value_Z && var1.putAt_Z()) {
            if (System.nanoTime() >= this.prevEx_J) {
               BlockPos var4 = this.putAt_FileStore_StreamSt.destination();
               if (var4 != null && !this.putAt_FileStore_TokenCod.putAt_Z_I(var4.getX(), var4.getY(), var4.getZ(), this.putAt_FileStore_TextStor)) {
                  this.value_Z = true;
                  this.putAt(
                     var4, var1.flowTemp(), true, ClassCache.putAt_FileStore_TextStor_FileStore_TextStor_List(this.putAt_FileStore_TextStor, var1.putAt_List())
                  );
               }
            }
         }
      }

      private void fill(BlockPos var1) {
         if (!this.putAt_Map.isEmpty() || !this.fill_Map.isEmpty()) {
            long var2 = this.putAt_LongSupplier.getAsLong();
            this.putAt_Map.values().removeIf(var3 -> putAt_Z_FileStore_TaskStat(var3, var1, var2));
            this.fill_Map.values().removeIf(var3 -> putAt_Z_FileStore_TaskStat(var3, var1, var2));
         }
      }

      private static boolean putAt_Z_FileStore_TaskStat(ClassCache.Inner_BufferHelper.Inner_DataUtil var0, BlockPos var1, long var2) {
         double var4 = var1.getX() - var0.x();
         double var6 = var1.getY() - var0.y();
         double var8 = var1.getZ() - var0.z();
         return var2 >= var0.expiresAt() || var4 * var4 + var6 * var6 + var8 * var8 >= 64.0;
      }

      public BlockPos putAt_BlockPos() {
         return this.putAt_BlockPos;
      }

      public synchronized void putAt_V() {
         this.putAt_Z = true;
         this.putAt_FileStore_EventBus = null;
         this.putAt_AtomicInteger.incrementAndGet();
         this.putAt_FileStore_TaskStat = null;
         this.putAt_Str = null;
         this.readEx_FileStore_StreamSt = this.fill_FileStore_StreamSt = this.putAt_FileStore_StreamSt = null;
         Thread var1 = this.putAt_Thread;
         if (var1 != null) {
            var1.interrupt();
         }
      }

      private void readEx(BlockPos var1) {
         this.putAt_V_BlockPos(var1, 0, false);
      }

      private synchronized void putAt_V_BlockPos(BlockPos var1, int var2, boolean var3) {
         this.putAt(var1, var2, var3, this.putAt_FileStore_TextStor);
      }

      public void fill_V() {
         this.prevEx_Z = true;
      }

      private synchronized void putAt(BlockPos var1, int var2, boolean var3, ClassCache.Inner_StateCache var4) {
         this.prevEx_Z = false;
         if (this.putAt_Thread != null) {
            this.putAt_Thread.interrupt();
         }

         this.prev = System.nanoTime();
         int var5 = this.putAt_AtomicInteger.incrementAndGet();
         this.putAt_FileStore_TaskStat = null;
         this.putAt_Str = null;
         this.putAt_BlockPos = var1.immutable();
         ClassCache.Inner_JsonUtil var6 = this.putAt_FileStore_TokenCod;
         ClassCache.Inner_DataUtil var7 = this.putAt_FileStore_CacheHel
            .withConstructionBlocks(Math.max(0, this.putAt_FileStore_CacheHel.constructionBlocks() - var2));
         var var8 = Set.copyOf(this.putAt_Set);
         Set var9;
         if (var3 && this.putAt_FileStore_StreamSt != null && !this.putAt_FileStore_StreamSt.movements().isEmpty()) {
            var9 = new HashSet<>(this.fill_Map.keySet());
            ClassCache.UiHelper var10 = this.putAt_FileStore_StreamSt.movements().getLast();
            var9.add(new ClassCache.Inner_ConfigStore(var10.type(), var10.to().asLong(), var10.from().asLong()));
         } else {
            var9 = Set.copyOf(this.fill_Map.keySet());
         }

         double var13 = this.putAt_D();
         Thread var12 = Thread.ofVirtual().unstarted(() -> {
            try {
               this.putAt(var5, var1.immutable(), var6, var7, var8, var9, var3, var4, var13);
            } catch (RuntimeException var15) {
               RuntimeException var11 = var15;
               synchronized (this) {
                  if (this.putAt_Z || this.putAt_AtomicInteger.get() != var5) {
                     return;
                  }

                  this.putAt_Str = "Route planner failed (" + var11.getClass().getSimpleName() + ")";
                  this.putAt_FileStore_EventBus = null;
                  this.notifyAll();
               }

               AutismClientAddon.LOG.error("[AI] Route planner failed from {}", var1.toShortString(), var15);
            }
         });
         this.putAt_Thread = var12;
         var12.start();
      }

      private void putAt(
         int var1,
         BlockPos var2,
         ClassCache.Inner_JsonUtil var3,
         ClassCache.Inner_DataUtil var4,
         Set<Long> var5,
         Set<ClassCache.Inner_ConfigStore> var6,
         boolean var7,
         ClassCache.Inner_StateCache var8,
         double var9
      ) {
         long var11 = System.nanoTime();
         long var13 = 0L;
         long var15 = 0L;
         long var17 = this.fill_Z ? 9223372036854775807L : var11 + (var7 ? 10000000000L : 4000000000L);
         boolean var19 = this.fill_Z && var4.allowParkour() && var4.strategy() == ClassCache.Inner_IoWorker.init;
         boolean var20 = var19 && !var4.allowDig() && !var4.allowBridge() && !var4.allowPillar();
         BlockPos var21 = var3.putAt_BlockPos();
         int var22 = var21 == null ? 0 : var21.getY() - var2.getY();
         boolean var23 = var20
            && var21 != null
            && (var22 >= 16 || var22 <= -16 && !var4.fallPolicy().putAt_Z())
            && Math.hypot(var2.getX() - var21.getX(), var2.getZ() - var21.getZ()) <= 32.0;
         if (var23) {
            var20 = false;
         }

         boolean var24 = var23;
         boolean var25 = var20 && var4.fallPolicy().putAt_Z() && var21 != null && var2.distSqr(var21) >= 1024.0;
         boolean var26 = var25 && var9 <= 0.100001;
         boolean var27 = var26;
         boolean var28 = var20 && var22 <= -16 && var4.fallPolicy().putAt_Z() && Math.hypot(var2.getX() - var21.getX(), var2.getZ() - var21.getZ()) <= 32.0;
         boolean var29 = var25 && putAt_Z_BlockPos_BlockPos(var2, var21);
         boolean var30 = var29;
         long var31 = var11;
         ClassCache.Inner_PacketUtil var33 = null;
         ClassCache.Inner_PacketUtil var34 = null;
         int var35 = 0;
         int var36 = 0;
         int var37 = 0;
         int var38 = 0;
         boolean var39 = false;
         boolean var40 = false;

         while (!this.putAt_Z && this.putAt_AtomicInteger.get() == var1) {
            long var41 = System.nanoTime();
            long var43 = this.putAt_FileStore_TextStor instanceof ClassCache.Inner_StreamUtil var45 ? var45.fill_J() : this.putAt_FileStore_TextStor.putAt_J();
            ClassCache.Inner_TextFormat var60 = ClassCache.Inner_TextFormat.putAt(
               var8, var2, var3, var19 ? var4.withoutConstruction() : var4, var5, var19, this.fill_Z, var9
            );
            var60.tick_Z = this.fill_Z;
            var60.putAt_Set = var6;
            if ((var29 || var28) && !var60.state_Z) {
               var60.fill_BlockPos = var21;
               var60.prev_Z = var30;
            }

            var60.putAt_FileStore_IoHelper.chunkAt_Z = var27 && !var60.state_Z;
            var60.putAt_FileStore_IoHelper.state_Z = var20 && !var19 && !var60.state_Z;
            var60.putAt_FileStore_IoHelper.fill_Z = var24 && !var19 && !var60.state_Z;
            var60.putAt_FileStore_IoHelper.closeOf_Z = var28 || var60.prev_Z || var25 && var60.putAt_FileStore_IoHelper.state_Z;
            if (var19 && var23 && var22 >= 16) {
               var60.putAt_DataCodec.init(1.45);
            }

            if (var19
               && !var60.state_Z
               && !var23
               && !var28
               && var21 != null
               && var4.fallPolicy().putAt_Z()
               && Math.abs(var22) <= 12
               && var2.distSqr(var21) <= 4096.0
               && var9 > 0.100001) {
               var60.putAt_DataCodec.init(1.0);
            }

            if (var60.putAt_FileStore_IoHelper.fill_Z) {
               var60.putAt_DataCodec.init(1.55);
            }

            if (var35 > 0) {
               var60.pollAt_Z = true;
               var60.pollOf_Z = var35 == 1;
               if (var35 == 1) {
                  var60.putAt_FileStore_HashPool = ClassCache.Inner_HashPool.around(var34);
               }

               var60.putAt_FileStore_IoHelper.putAt_Z = var35 == 2;
               var60.putAt_DataCodec.init(var35 == 1 ? 1.55 : 2.05);
               var60.putAt_FileStore_IoHelper.closeOf_Z = false;
               var60.putAt_FileStore_IoHelper.chunkAt_Z = false;
               var60.putAt_FileStore_IoHelper.fill_Z = false;
            }

            if (this.fill_Z && (!var19 || var40) && var60.fill_BlockPos == null) {
               var60.pollAt_I = var60.pollOf_I = 2147483647;
            }

            boolean var61 = false;
            boolean var47 = false;
            long var48 = var11;

            while (
               !this.putAt_Z
                  && this.putAt_AtomicInteger.get() == var1
                  && (var35 == 0 || System.nanoTime() - var41 < 8000000000L)
                  && System.nanoTime() < (!this.fill_Z && var7 && this.prevEx_Z ? Math.min(var17, var11 + 4000000000L) : var17)
                  && !(var61 = var60.putAt_Z_I(16))
            ) {
               long var50 = System.nanoTime();
               if (this.fill_Z && var60.state_Z && !var39 && var50 >= var48) {
                  var47 = putAt_Z_FileStore_TextStor_BlockPos(var8, var21);
                  if (var47) {
                     break;
                  }

                  var48 = var50 + 250000000L;
               }

               if (this.fill_Z && !var7 && var50 >= var31) {
                  synchronized (this) {
                     if (this.putAt_AtomicInteger.get() == var1) {
                        this.putAt_FileStore_EventBus = new ClassCache.Inner_EventBus(var37 + var60.flowState(), var60.putAt_List());
                     }
                  }

                  var31 = var50 + 350000000L;
               }
            }

            if (!var61) {
               var60.putAt_V();
            }

            var33 = var60.putAt_FileStore_StreamSt();
            if (ListPool.init_Z() && this.fill_Z) {
               ListPool.init(
                  "route round "
                     + (var36 + 1)
                     + ": "
                     + (System.nanoTime() - var41) / 1000000L
                     + " ms, ordinary="
                     + var19
                     + ", quick="
                     + var60.putAt_FileStore_IoHelper.chunkAt_Z
                     + ", reduced="
                     + var60.putAt_FileStore_IoHelper.state_Z
                     + ", expanded="
                     + var60.flowState()
                     + ", jump solves="
                     + var60.flowTemp()
                     + ", cache hits="
                     + var60.putAt_FileStore_IoHelper.value_I
                     + ", reached="
                     + var33.reachesGoal()
               );
            }

            var36++;
            var13 += var60.fill_J();
            var15 += var60.readEx();
            var37 += var60.flowState();
            var38 = Math.max(var38, var60.value());
            if (this.putAt_Z || this.putAt_AtomicInteger.get() != var1) {
               return;
            }

            if (this.fill_Z && var60.state_Z && !var39 && (var47 || putAt_Z_FileStore_TextStor_BlockPos(var8, var21))) {
               var39 = true;
               continue;
            }

            if (!this.readEx_Z || var7 || var60.state_Z || !var33.reachesGoal() && var34 == null) {
               if ((var60.fill_BlockPos != null || var19 || var60.putAt_FileStore_IoHelper.state_Z || var60.putAt_FileStore_IoHelper.fill_Z)
                  && !var33.reachesGoal()
                  && !var33.awaitingTerrain()
                  && var33.movements().isEmpty()
                  && !var33.hitPending()) {
                  if (var60.fill_BlockPos != null) {
                     if (var28) {
                        var28 = false;
                     } else if (var60.prev_Z) {
                        var30 = false;
                     } else if (var60.putAt_FileStore_IoHelper.chunkAt_Z) {
                        var27 = false;
                     } else if (var19) {
                        var19 = false;
                     } else {
                        var29 = false;
                        var19 = true;
                        var27 = var26;
                     }
                     continue;
                  }

                  if (var19 && var60.closeOf_Z && !var40) {
                     var40 = true;
                     continue;
                  }

                  if (var60.putAt_FileStore_IoHelper.chunkAt_Z) {
                     var27 = false;
                  } else if (var19) {
                     var19 = false;
                  } else if (var60.putAt_FileStore_IoHelper.closeOf_Z) {
                     var25 = false;
                  } else if (var60.putAt_FileStore_IoHelper.state_Z) {
                     var20 = false;
                  } else {
                     var24 = false;
                  }
                  continue;
               }

               if (!this.fill_Z && var33.exhausted() && !var33.hitPending() && !var33.reachesGoal() && System.nanoTime() < var17) {
                  continue;
               }

               if (var33.hitPending() && !var33.reachesGoal() && !var33.awaitingTerrain() && var33.movements().isEmpty() && System.nanoTime() < var17) {
                  try {
                     if (this.putAt_FileStore_TextStor instanceof ClassCache.Inner_StreamUtil var62) {
                        var62.putAt_V_J_J(var43, var17);
                        continue;
                     }
                  } catch (InterruptedException var56) {
                     return;
                  }
               }
               break;
            }

            if (var33.reachesGoal()) {
               var33 = ClassCache.putAt(var33, var8, var4, var9, var5, var6);
            }

            if (var33.reachesGoal() && (var34 == null || ClassCache.putAt(var33) < ClassCache.putAt(var34) - 0.5)) {
               var34 = var33;
            }

            ListPool.init(
               "Parkour WR pass "
                  + (var35 + 1)
                  + " from "
                  + var2.toShortString()
                  + ": "
                  + (var33.reachesGoal() ? String.format(Locale.ROOT, "%.1f ticks", ClassCache.putAt(var33)) : "no faster proof")
            );
            if (var35 < 2 && var34.movements().size() > 2) {
               var35++;
               var19 = var35 == 1;
               var27 = false;
               var29 = false;
               var28 = false;
               var40 = false;
               continue;
            }

            var33 = ClassCache.putAt(var34, var8, var4, var9);
            break;
         }

         synchronized (this) {
            if (!this.putAt_Z && this.putAt_AtomicInteger.get() == var1 && var33 != null) {
               this.putAt_FileStore_RingBuff = new ClassCache.Inner_ByteReader(System.nanoTime() - var11, var36, var37, var15, var13, var38);
               this.putAt_FileStore_EventBus = null;
               this.putAt_FileStore_TaskStat = new ClassCache.Inner_BufferHelper.Inner_StreamUtil(var1, var2, var33);
            }

            this.notifyAll();
         }

         long var58 = (System.nanoTime() - var11) / 1000000L;
         if (var33 != null && (var58 >= 400L && !var7 || ListPool.init_Z())) {
            String var59 = String.format(
               Locale.ROOT,
               "plan%s from %s: %d ms, %d expanded, %d rounds, %s, %d moves%s",
               var7 ? " ahead" : "",
               var2.toShortString(),
               var58,
               var37,
               var36,
               var33.reachesGoal() ? "reaches the goal" : "partial",
               var33.movements().size(),
               var33.movements().isEmpty() ? "" : " to " + var33.movements().get(var33.movements().size() - 1).to().toShortString()
            );
            if (var58 >= 400L && !var7) {
               AutismClientAddon.LOG.info("[AI] slow " + var59);
            } else {
               ListPool.init(var59);
            }
         }
      }

      private static boolean putAt_Z_BlockPos_BlockPos(BlockPos var0, BlockPos var1) {
         return var0.getY() - var1.getY() >= 12 && Math.hypot(var0.getX() - var1.getX(), var0.getZ() - var1.getZ()) >= 32.0;
      }

      private static boolean putAt_Z_FileStore_TextStor_BlockPos(ClassCache.Inner_StateCache var0, BlockPos var1) {
         ClassCache.Inner_FileCache var2 = var0.probe(var1.getX(), var1.getY(), var1.getZ());
         return var2 != null && var2.loaded() && !var2.cached();
      }

      private record Inner_DataUtil(long expiresAt, double x, double y, double z) {
      }

      private record Inner_StreamUtil(int generation, BlockPos from, ClassCache.Inner_PacketUtil segment) {
      }
   }

   public interface Inner_PoolEntry {
      ClassCache.Inner_PoolEntry value = (var0, var1, var2) -> 0.0;
      ClassCache.Inner_PoolEntry write = value;
      static ClassCache.Inner_PoolEntry init(List<BlockPos> mobs) { return putAt(mobs); }
      default double init(int x, int y, int z) { return this.putAt(x, y, z); }
      default double init(int fx, int fy, int fz, int tx, int ty, int tz) { return this.putAt(fx, fy, fz, tx, ty, tz); }

      double surcharge(int var1, int var2, int var3);

      default double putAt(int var1, int var2, int var3) {
         return 0.0;
      }

      default double putAt(int var1, int var2, int var3, int var4, int var5, int var6) {
         return 0.0;
      }

      static ClassCache.Inner_PoolEntry putAt(List<BlockPos> var0) {
         var var1 = List.copyOf(var0);
         return var1.isEmpty() ? value : (var1x, var2, var3) -> {
            double var4 = 1.7976931348623157E308;

            for (BlockPos var7 : var1) {
               double var8 = var1x - var7.getX();
               double var10 = var2 - var7.getY();
               double var12 = var3 - var7.getZ();
               var4 = Math.min(var4, var8 * var8 + var10 * var10 + var12 * var12);
            }

            double var14 = 12.0;
            return var4 >= var14 * var14 ? 0.0 : 60.0 * (1.0 - Math.sqrt(var4) / var14);
         };
      }
   }

   private record Inner_TextState(ClassCache.UiHelper first, ClassCache.Inner_TextState next, double ticks) {
   }

   public interface Inner_StateCache {
      ClassCache.Inner_FileCache probe(int var1, int var2, int var3);

      default ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int var1, int var2, int var3) {
         return this.probe(var1, var2, var3);
      }

      default long init_J() { return this.putAt_J(); }
      default short init_S_I(int x, int y, int z) { return this.putAt_S_I(x, y, z); }
      default short load_S(int x, int y, int z) { return this.fill_S(x, y, z); }
      default byte init_B_I(int x, int y, int z) { return this.putAt_B_I(x, y, z); }

      default long putAt_J() {
         return 0L;
      }

      default short putAt_S_I(int var1, int var2, int var3) {
         return e.init_S_ClassCache_FileCac(this.probe(var1, var2, var3));
      }

      default short fill_S(int var1, int var2, int var3) {
         return this.putAt_S_I(var1, var2, var3);
      }

      default byte putAt_B_I(int var1, int var2, int var3) {
         return m.init(this.probe(var1, var2, var3)).init();
      }

      default double movementSpeed() {
         return 0.1;
      }

      default boolean putAt_Z() {
         return false;
      }

      default ClassCache.Inner_IoCodec putAt_FileStore_IoCodec_I(int var1, int var2, int var3, boolean var4) {
         long var5 = 0L;
         boolean var7 = false;

         for (int var8 = 0; var8 < 64; var8++) {
            ClassCache.Inner_FileCache var9 = this.probe(var1, var2 + var8, var3);
            if (var9 == null) {
               var7 = true;
            } else if (var9.loaded() && (!var4 || !var9.cached()) && !var9.passable()) {
               var5 |= 1L << var8;
            }
         }

         return new ClassCache.Inner_IoCodec(var5, var7);
      }
   }

   public interface Inner_JsonUtil {
      // 5.3 external ABI bridges for untouched ai/* goals
      default boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) { return this.isInGoal(x, y, z); }
      default ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) { return this; }
      default boolean init_Z(BlockPos cell) { return true; }
      default boolean read(BlockPos cell) { return true; }
      default boolean init_Z() { return false; }
      default boolean load(BlockPos cell) { return true; }
      default int init(ItemStack stack) { return 0; }
      default double init_D() { return 40.0; }
      default boolean write(BlockPos cell) { return false; }
      default Set<ClassCache.Inner_SyncManager> init_Set() { return Set.of(); }
      default BlockPos init_BlockPos() { return null; }
      static ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_BlockPos_D(BlockPos target, double radius) {
         return putAt_FileStore_TokenCod_BlockPos_D(target, radius);
      }
      boolean isInGoal(int var1, int var2, int var3);

      default boolean putAt_Z_I(int var1, int var2, int var3, ClassCache.Inner_StateCache var4) {
         return this.init_Z_I(var1, var2, var3, var4);
      }

      double heuristic(int var1, int var2, int var3);

      default ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod_FileStore_TextStor(ClassCache.Inner_StateCache var1) {
         return this.init(var1);
      }

      default boolean readEx_Z_BlockPos(BlockPos var1) {
         return this.read(var1);
      }

      default boolean putAt_Z() {
         return this.init_Z();
      }

      default boolean fill_Z_BlockPos(BlockPos var1) {
         return this.load(var1);
      }

      default int putAt_I_ItemStack(ItemStack var1) {
         return this.init(var1);
      }

      default double putAt_D() {
         return this.init_D();
      }

      default boolean value(BlockPos var1) {
         return this.write(var1);
      }

      default boolean putAt_Z_BlockPos(BlockPos var1) {
         return this.init_Z(var1);
      }

      default Set<ClassCache.Inner_SyncManager> putAt_Set() {
         return this.init_Set();
      }

      default boolean outsideSearchBox(BlockPos var1, int var2, int var3) {
         return false;
      }

      default BlockPos putAt_BlockPos() {
         return this.init_BlockPos();
      }

      static ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod_BlockPos_D(BlockPos var0, double var1) {
         return putAt_FileStore_TokenCod_BlockPos(var0, var1, false);
      }

      static ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod_BlockPos(final BlockPos var0, final double var1, final boolean var3) {
         final double var4 = var1 * var1;
         final double var6 = var1 * Math.hypot(ClassCache.Inner_CachePool.writeOf, ClassCache.Inner_CachePool.blockBy);
         return new ClassCache.Inner_JsonUtil() {
            @Override
            public BlockPos putAt_BlockPos() {
               return var0;
            }

            @Override
            public BlockPos init_BlockPos() {
               return this.putAt_BlockPos();
            }

            @Override
            public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
               return this.putAt_Z_I(x, y, z, planned);
            }

            @Override
            public boolean isInGoal(int var1x, int var2, int var3x) {
               double var4x = var1x - var0.getX();
               double var6x = var2 - var0.getY();
               double var8 = var3x - var0.getZ();
               return var4x * var4x + var6x * var6x + var8 * var8 <= var4;
            }

            @Override
            public boolean putAt_Z_I(int var1x, int var2, int var3x, ClassCache.Inner_StateCache var4x) {
               if (this.isInGoal(var1x, var2, var3x)) {
                  return true;
               }

               double var5 = var1x - var0.getX();
               double var7 = var3x - var0.getZ();
               if (var4x == null || var5 * var5 + var7 * var7 > var4) {
                  return false;
               }

               if (var3 && var5 == 0.0 && var7 == 0.0 && var2 == var0.getY() - 1) {
                  ClassCache.Inner_FileCache var13 = var4x.probe(var1x, var2 + 1, var3x);
                  ClassCache.Inner_FileCache var10 = var4x.probe(var1x, var2 + 2, var3x);
                  ClassCache.Inner_FileCache var11 = var4x.probe(var1x, var2, var3x);
                  ClassCache.Inner_FileCache var12 = var4x.probe(var1x, var2 - 1, var3x);
                  return var13 != null
                     && var10 != null
                     && var11 != null
                     && var12 != null
                     && var13.passable()
                     && var10.passable()
                     && var13.loaded()
                     && var10.loaded()
                     && !var13.cached()
                     && !var10.cached()
                     && !var11.cached()
                     && !var12.cached()
                     && !var13.water()
                     && !var13.climbable()
                     && ClassCache.Inner_RenderState.putAt_Z_FileStore_BitCache(var11, var13, var12);
               }

               if (var2 != var0.getY() + 1) {
                  return false;
               }

               ClassCache.Inner_FileCache var9 = var4x.probe(var0.getX(), var0.getY(), var0.getZ());
               return var9 != null
                  && var9.loaded()
                  && !var9.cached()
                  && !var9.passable()
                  && var9.top16() > 0
                  && var9.top16() < 16
                  && !var9.door()
                  && !var9.hazardFloor()
                  && !var9.lava();
            }

            @Override
            public double heuristic(int var1x, int var2, int var3x) {
               int var4x = var0.getY() - var2;
               if (var4x < 0) {
                  var4x++;
               } else if (var3 && var4x > 0) {
                  var4x--;
               }

               double var5 = ClassCache.Inner_CachePool.fill(var1x - var0.getX(), var4x, var3x - var0.getZ());
               if (var3 && var4x > 0) {
                  double var7 = var4x * ClassCache.Inner_CachePool.blockBy;
                  var5 = Math.max(var5 - var7, var7);
               }

               return Math.max(0.0, var5 - var6);
            }

            @Override
            public boolean outsideSearchBox(BlockPos var1x, int var2, int var3x) {
               return Math.abs(var0.getX() - var1x.getX()) > var2 + var1
                  || Math.abs(var0.getZ() - var1x.getZ()) > var2 + var1
                  || Math.abs(var0.getY() - var1x.getY()) > var3x + var1;
            }

            @Override
            public String toString() {
               return "near(" + var0.toShortString() + ", r=" + var1 + ")";
            }
         };
      }

      static ClassCache.Inner_JsonUtil putAt_FileStore_TokenCod_I_BlockPos(final int var0, final BlockPos var1) {
         return new ClassCache.Inner_JsonUtil() {
            @Override
            public boolean isInGoal(int var1x, int var2, int var3) {
               return var2 == var0;
            }

            @Override
            public double heuristic(int var1x, int var2, int var3) {
               return Math.abs(var2 - var0) * 4.0 + Math.hypot(var1x - var1.getX(), var3 - var1.getZ()) * 0.5;
            }

            @Override
            public String toString() {
               return "yLevel(" + var0 + ")";
            }
         };
      }
   }
}
