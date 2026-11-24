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
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.PotentSulfurBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.SulfurSpikeBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClassCache {
   private static final ConcurrentHashMap<ClassCache.Inner_ConfigStore, Long> init = new ConcurrentHashMap<>();
   private static final ConcurrentHashMap<ClassCache.Inner_ConfigStore, long[]> load = new ConcurrentHashMap<>();

   private ClassCache() {
   }

   public static List<ClassCache.UiHelper> init_List_List_List(List<ClassCache.UiHelper> current, List<ClassCache.UiHelper> next) {
      if (!current.isEmpty() && !next.isEmpty()) {
         if (!next.get(0).from().equals(current.get(current.size() - 1).to())) {
            return null;
         }

         List<ClassCache.UiHelper> merged = new ArrayList<>(current.size() + next.size());
         merged.addAll(current);
         merged.addAll(next);
         return merged;
      } else {
         return null;
      }
   }

   public static ClassCache.Inner_HashUtil init_ClassCache_HashUti_I_I(int stallsSinceProgress, int replansTotal) {
      if (replansTotal >= 6) {
         return ClassCache.Inner_HashUtil.read;
      } else {
         return stallsSinceProgress >= 3 ? ClassCache.Inner_HashUtil.load : ClassCache.Inner_HashUtil.init;
      }
   }

   public static void init_V_ClassCache_UiHelpe_J(ClassCache.UiHelper move, long nanos) {
      if (move != null && move.type() == ClassCache.Inner_SyncManager.start) {
         load(move, nanos);
      }
   }

   public static void load(ClassCache.UiHelper move, long nanos) {
      if (move != null) {
         long now = System.nanoTime();
         init.values().removeIf(until -> until - now < 0L);
         init.put(new ClassCache.Inner_ConfigStore(move.type(), move.from().asLong(), move.to().asLong()), now + nanos);
      }
   }

   public static void init_V(ClassCache.UiHelper move) {
      if (move != null) {
         long now = System.nanoTime();
         load.values().removeIf(v -> now - v[1] > 120000000000L);
         ClassCache.Inner_ConfigStore key = new ClassCache.Inner_ConfigStore(move.type(), move.from().asLong(), move.to().asLong());
         long[] seen = load.computeIfAbsent(key, k -> new long[]{0L, now});
         seen[0]++;
         seen[1] = now;
         if (seen[0] >= 2L) {
            load(move, 120000000000L);
         }
      }
   }

   static boolean init_Z(ClassCache.UiHelper move) {
      if (init.isEmpty()) {
         return false;
      }

      Long until = init.get(new ClassCache.Inner_ConfigStore(move.type(), move.from().asLong(), move.to().asLong()));
      return until != null && until - System.nanoTime() > 0L;
   }

   public static Set<ClassCache.Inner_SyncManager> init(ClassCache.Inner_SyncManager stalledType) {
      Set var10000;
      switch (stalledType) {
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
         default:
            var10000 = Set.of(stalledType);
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

   static ClassCache.Inner_StateCache init_ClassCache_StateCa_ClassCache_StateCa_List(
      final ClassCache.Inner_StateCache base, List<ClassCache.UiHelper> movements
   ) {
      Map<Long, ClassCache.Inner_FileCache> edits = new HashMap<>();

      for (ClassCache.UiHelper movement : movements) {
         for (BlockPos cell : movement.cellsToBreak()) {
            edits.put(cell.asLong(), ClassCache.Inner_FileCache.air());
         }

         for (BlockPos cell : movement.cellsToPlace()) {
            edits.put(cell.asLong(), ClassCache.Inner_FileCache.fullBlock(1.0 / 0.0));
         }
      }

      if (edits.isEmpty()) {
         return base;
      }

      final Map<Long, ClassCache.Inner_FileCache> frozen = Map.copyOf(edits);
      return new ClassCache.Inner_StateCache() {
         @Override
         public ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int x, int y, int z) {
            ClassCache.Inner_FileCache edited = frozen.get(BlockPos.asLong(x, y, z));
            return edited == null ? base.init_ClassCache_FileCac_I(x, y, z) : edited;
         }

         @Override
         public short init_S_I(int x, int y, int z) {
            ClassCache.Inner_FileCache edited = frozen.get(BlockPos.asLong(x, y, z));
            return edited == null ? base.init_S_I(x, y, z) : e.init_S_ClassCache_FileCac(edited);
         }

         @Override
         public short load_S(int x, int y, int z) {
            ClassCache.Inner_FileCache edited = frozen.get(BlockPos.asLong(x, y, z));
            return edited == null ? base.load_S(x, y, z) : e.init_S_ClassCache_FileCac(edited);
         }

         @Override
         public byte init_B_I(int x, int y, int z) {
            return frozen.containsKey(BlockPos.asLong(x, y, z)) ? m.init_m.init() : base.init_B_I(x, y, z);
         }

         @Override
         public long init_J() {
            return base.init_J();
         }
      };
   }

   public static final class Inner_BufferHelper {
      static final long init_J = 4000000000L;
      static final long load_J = 10000000000L;
      private long read_J;
      private static final long write_J = 30000000000L;
      private static final double init_D = 64.0;
      private final ClassCache.Inner_StateCache init_ClassCache_StateCa;
      private final LongSupplier init_LongSupplier;
      private final Map<Long, ClassCache.Inner_BufferHelper.Inner_DataUtil> init_Map = new ConcurrentHashMap<>();
      private final Set<Long> init_Set = this.init_Map.keySet();
      private final Map<ClassCache.Inner_ConfigStore, ClassCache.Inner_BufferHelper.Inner_DataUtil> load_Map = new HashMap<>();
      private final AtomicInteger init_AtomicInteger = new AtomicInteger();
      private volatile boolean init_Z;
      private volatile ClassCache.Inner_BufferHelper.Inner_StreamUtil init_ClassCache_BufferH;
      private volatile ClassCache.Inner_ByteReader init_ClassCache_ByteRea;
      private volatile ClassCache.Inner_EventBus init_ClassCache_EventBu;
      private final boolean load_Z;
      private ClassCache.Inner_JsonUtil init_ClassCache_JsonUti = ClassCache.Inner_JsonUtil.init_ClassCache_JsonUti_BlockPos_D(BlockPos.ZERO, 1.0);
      private ClassCache.Inner_DataUtil init_ClassCache_DataUti = ClassCache.Inner_DataUtil.WALK_ONLY;
      private Thread init_Thread;
      private BlockPos init_BlockPos = BlockPos.ZERO;
      private ClassCache.Inner_PacketUtil init_ClassCache_PacketU;
      private ClassCache.Inner_PacketUtil load_ClassCache_PacketU;
      private ClassCache.Inner_PacketUtil read_ClassCache_PacketU;
      private boolean read_Z;
      private long update;
      private volatile long sync;
      private volatile boolean write_Z;

      public ClassCache.Inner_EventBus init_ClassCache_EventBu() {
         return this.init_ClassCache_EventBu;
      }

      public ClassCache.Inner_ByteReader init_ClassCache_ByteRea() {
         return this.init_ClassCache_ByteRea;
      }

      public Inner_BufferHelper(ClassCache.Inner_StateCache grid) {
         this(grid, System::nanoTime, false);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache grid, boolean completeRoute) {
         this(grid, System::nanoTime, completeRoute);
      }

      Inner_BufferHelper(ClassCache.Inner_StateCache grid, LongSupplier clock) {
         this(grid, clock, false);
      }

      private Inner_BufferHelper(ClassCache.Inner_StateCache grid, LongSupplier clock, boolean completeRoute) {
         this.load_Z = completeRoute;
         this.init_ClassCache_StateCa = grid;
         this.init_LongSupplier = clock;
      }

      public void init_V_BlockPos_2(BlockPos from, ClassCache.Inner_JsonUtil goal, ClassCache.Inner_DataUtil caps) {
         this.init_Z = false;
         this.init_ClassCache_JsonUti = goal;
         this.init_ClassCache_DataUti = caps;
         this.init_ClassCache_PacketU = null;
         this.load_ClassCache_PacketU = null;
         this.read_ClassCache_PacketU = null;
         this.read_Z = false;
         this.read(from);
      }

      public void init(ClassCache.Inner_DataUtil caps) {
         this.init_ClassCache_DataUti = caps;
      }

      public synchronized void init(ClassCache.Inner_JsonUtil goal) {
         if (this.init_ClassCache_JsonUti != goal) {
            this.init_ClassCache_JsonUti = goal;
            if (this.init_ClassCache_PacketU == null) {
               this.init_AtomicInteger.incrementAndGet();
               this.init_ClassCache_BufferH = null;
               this.read_ClassCache_PacketU = this.load_ClassCache_PacketU = null;
               this.read_Z = false;
               if (this.init_Thread != null) {
                  this.init_Thread.interrupt();
               }
            } else {
               if (this.load_ClassCache_PacketU != null) {
                  BlockPos end = this.load_ClassCache_PacketU.destination();
                  if (end == null || !goal.isInGoal(end.getX(), end.getY(), end.getZ())) {
                     this.load_ClassCache_PacketU = null;
                  }
               }

               if (this.read_Z) {
                  this.init_AtomicInteger.incrementAndGet();
                  this.init_ClassCache_BufferH = null;
                  if (this.init_Thread != null) {
                     this.init_Thread.interrupt();
                  }

                  this.read_Z = false;
               }
            }
         }
      }

      public void init(BlockPos from) {
         this.load_ClassCache_PacketU = null;
         this.init_ClassCache_PacketU = null;
         this.read_ClassCache_PacketU = null;
         this.read_Z = false;
         this.update = System.nanoTime() + 200000000L;
         this.read(from);
      }

      public void init(BlockPos cell, BlockPos feet) {
         this.init_Map
            .put(
               cell.asLong(),
               new ClassCache.Inner_BufferHelper.Inner_DataUtil(this.init_LongSupplier.getAsLong() + 30000000000L, feet.getX(), feet.getY(), feet.getZ())
            );
      }

      void init(ClassCache.UiHelper move, BlockPos feet) {
         this.load_Map
            .put(
               ClassCache.Inner_ConfigStore.of(move),
               new ClassCache.Inner_BufferHelper.Inner_DataUtil(this.init_LongSupplier.getAsLong() + 30000000000L, feet.getX(), feet.getY(), feet.getZ())
            );
      }

      public int init_I() {
         return this.init_Map.size();
      }

      Set<Long> init_Set() {
         return this.init_Set;
      }

      public boolean init_Z() {
         return this.read_ClassCache_PacketU == null && this.init_Thread != null && this.init_Thread.isAlive() || this.init_ClassCache_BufferH != null;
      }

      public boolean load_Z() {
         return this.read_Z && this.load_ClassCache_PacketU == null;
      }

      public boolean init(long nanos) {
         if (this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil) {
            return this.init_ClassCache_BufferH != null;
         }

         if (System.nanoTime() - this.sync > nanos) {
            return this.init_ClassCache_BufferH != null;
         }

         long end = System.nanoTime() + nanos;
         synchronized (this) {
            while (this.init_ClassCache_BufferH == null && !this.init_Z) {
               Thread t = this.init_Thread;
               if (t == null || !t.isAlive()) {
                  break;
               }

               long left = end - System.nanoTime();
               if (left <= 0L) {
                  break;
               }

               try {
                  this.wait(left / 1000000L, (int)(left % 1000000L));
               } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  break;
               }
            }

            return this.init_ClassCache_BufferH != null;
         }
      }

      public ClassCache.Inner_PacketUtil init_ClassCache_PacketU() {
         ClassCache.Inner_PacketUtil s = this.read_ClassCache_PacketU;
         this.read_ClassCache_PacketU = null;
         return s;
      }

      public void init(ClassCache.Inner_PacketUtil s) {
         this.init_ClassCache_PacketU = s;
         if (this.load_ClassCache_PacketU == s) {
            this.load_ClassCache_PacketU = null;
         }
      }

      public ClassCache.Inner_PacketUtil load_ClassCache_PacketU() {
         return this.load_ClassCache_PacketU;
      }

      public ClassCache.Inner_PacketUtil read() {
         ClassCache.Inner_PacketUtil s = this.load_ClassCache_PacketU;
         this.load_ClassCache_PacketU = null;
         return s;
      }

      public void init(ClassCache.Inner_TaskQueue exec) {
         this.load(exec.write_BlockPos());
         ClassCache.Inner_BufferHelper.Inner_StreamUtil done = this.init_ClassCache_BufferH;
         if (done != null) {
            if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
               ListPool.init(
                  "driver result gen="
                     + done.generation()
                     + " currentGen="
                     + this.init_AtomicInteger.get()
                     + " from="
                     + done.from()
                     + " current="
                     + (this.init_ClassCache_PacketU == null ? null : this.init_ClassCache_PacketU.destination())
               );
            }

            if (done.generation() == this.init_AtomicInteger.get()) {
               this.init_ClassCache_BufferH = null;
               if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                  ListPool.init(
                     "driver plan done from="
                        + done.from()
                        + " current="
                        + (this.init_ClassCache_PacketU == null ? null : this.init_ClassCache_PacketU.destination())
                        + " moves="
                        + done.segment().movements().size()
                        + " segment="
                        + done.segment().movements()
                  );
               }

               if (this.init_ClassCache_PacketU == null) {
                  this.read_ClassCache_PacketU = done.segment();
               } else {
                  this.read_Z = false;
                  List<ClassCache.UiHelper> moves = done.segment().movements();
                  if (done.from().equals(this.init_ClassCache_PacketU.destination())
                     && (moves.isEmpty() || moves.get(0).from().equals(this.init_ClassCache_PacketU.destination()))) {
                     this.load_ClassCache_PacketU = done.segment();
                     if (this.load_ClassCache_PacketU.awaitingTerrain()) {
                        this.read_J = System.nanoTime() + 250000000L;
                     }
                  }
               }
            } else {
               this.init_ClassCache_BufferH = null;
            }
         }

         if (this.load_ClassCache_PacketU != null && this.load_ClassCache_PacketU.awaitingTerrain() && System.nanoTime() >= this.read_J) {
            this.load_ClassCache_PacketU = null;
         }

         if (this.init_ClassCache_PacketU != null && this.load_ClassCache_PacketU == null && !this.read_Z && exec.write_Z() && System.nanoTime() >= this.update
            )
          {
            BlockPos dest = this.init_ClassCache_PacketU.destination();
            if (dest != null && !this.init_ClassCache_JsonUti.init_Z_I(dest.getX(), dest.getY(), dest.getZ(), this.init_ClassCache_StateCa)) {
               this.read_Z = true;
               this.init(dest, exec.flush_I(), true, ClassCache.init_ClassCache_StateCa_ClassCache_StateCa_List(this.init_ClassCache_StateCa, exec.init_List()));
            }
         }
      }

      private void load(BlockPos feet) {
         if (!this.init_Map.isEmpty() || !this.load_Map.isEmpty()) {
            long now = this.init_LongSupplier.getAsLong();
            this.init_Map.values().removeIf(entry -> init_Z_ClassCache_BufferH(entry, feet, now));
            this.load_Map.values().removeIf(entry -> init_Z_ClassCache_BufferH(entry, feet, now));
         }
      }

      private static boolean init_Z_ClassCache_BufferH(ClassCache.Inner_BufferHelper.Inner_DataUtil entry, BlockPos feet, long now) {
         double dx = feet.getX() - entry.x();
         double dy = feet.getY() - entry.y();
         double dz = feet.getZ() - entry.z();
         return now >= entry.expiresAt() || dx * dx + dy * dy + dz * dz >= 64.0;
      }

      public BlockPos init_BlockPos() {
         return this.init_BlockPos;
      }

      public synchronized void init_V() {
         this.init_Z = true;
         this.init_ClassCache_EventBu = null;
         this.init_AtomicInteger.incrementAndGet();
         this.init_ClassCache_BufferH = null;
         this.read_ClassCache_PacketU = this.load_ClassCache_PacketU = this.init_ClassCache_PacketU = null;
         Thread t = this.init_Thread;
         if (t != null) {
            t.interrupt();
         }
      }

      private void read(BlockPos from) {
         this.init_V_BlockPos(from, 0, false);
      }

      private synchronized void init_V_BlockPos(BlockPos from, int reservedBlocks, boolean ahead) {
         this.init(from, reservedBlocks, ahead, this.init_ClassCache_StateCa);
      }

      public void load_V() {
         this.write_Z = true;
      }

      private synchronized void init(BlockPos from, int reservedBlocks, boolean ahead, ClassCache.Inner_StateCache plannedGrid) {
         this.write_Z = false;
         if (this.init_Thread != null) {
            this.init_Thread.interrupt();
         }

         this.sync = System.nanoTime();
         int gen = this.init_AtomicInteger.incrementAndGet();
         this.init_ClassCache_BufferH = null;
         this.init_BlockPos = from.immutable();
         ClassCache.Inner_JsonUtil planGoal = this.init_ClassCache_JsonUti;
         ClassCache.Inner_DataUtil planCaps = this.init_ClassCache_DataUti
            .withConstructionBlocks(Math.max(0, this.init_ClassCache_DataUti.constructionBlocks() - reservedBlocks));
         Set<Long> planBlacklist = Set.copyOf(this.init_Set);
         Set<ClassCache.Inner_ConfigStore> failedMoves;
         if (ahead && this.init_ClassCache_PacketU != null && !this.init_ClassCache_PacketU.movements().isEmpty()) {
            failedMoves = new HashSet<>(this.load_Map.keySet());
            ClassCache.UiHelper last = this.init_ClassCache_PacketU.movements().getLast();
            failedMoves.add(new ClassCache.Inner_ConfigStore(last.type(), last.to().asLong(), last.from().asLong()));
         } else {
            failedMoves = Set.copyOf(this.load_Map.keySet());
         }

         Thread t = Thread.ofVirtual().unstarted(() -> this.init(gen, from.immutable(), planGoal, planCaps, planBlacklist, failedMoves, ahead, plannedGrid));
         this.init_Thread = t;
         t.start();
      }

      private void init(
         int gen,
         BlockPos from,
         ClassCache.Inner_JsonUtil planGoal,
         ClassCache.Inner_DataUtil planCaps,
         Set<Long> planBlacklist,
         Set<ClassCache.Inner_ConfigStore> failedMoves,
         boolean ahead,
         ClassCache.Inner_StateCache plannedGrid
      ) {
         long began = System.nanoTime();
         long queries = 0L;
         long generated = 0L;
         long deadline = this.load_Z ? 9223372036854775807L : began + (ahead ? 10000000000L : 4000000000L);
         boolean ordinaryFirst = this.load_Z && planCaps.allowParkour() && planCaps.strategy() == ClassCache.Inner_IoWorker.init;
         boolean limitedAirSearch = ordinaryFirst && !planCaps.allowDig() && !planCaps.allowBridge() && !planCaps.allowPillar();
         BlockPos destination = planGoal.init_BlockPos();
         boolean quickRollout = limitedAirSearch && planCaps.fallPolicy().init() && destination != null && from.distSqr(destination) >= 4096.0;
         long nextProgress = began;
         ClassCache.Inner_PacketUtil result = null;
         int rounds = 0;
         int expanded = 0;
         int peak = 0;

         while (!this.init_Z && this.init_AtomicInteger.get() == gen) {
            long available = this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil clientGrid
               ? clientGrid.load_J()
               : this.init_ClassCache_StateCa.init_J();
            ClassCache.Inner_TextFormat search = ClassCache.Inner_TextFormat.init(
               plannedGrid, from, planGoal, ordinaryFirst ? planCaps.withoutConstruction() : planCaps, planBlacklist, ordinaryFirst, this.load_Z
            );
            search.close_Z = this.load_Z;
            search.init_Set = failedMoves;
            search.init_ClassCache_RenderS.update_Z = quickRollout && !search.flush_Z;
            search.init_ClassCache_RenderS.sync_Z = limitedAirSearch && !ordinaryFirst && !search.flush_Z;
            if (this.load_Z && !ordinaryFirst) {
               search.poll = search.push = 2147483647;
            }

            boolean settled = false;

            while (
               !this.init_Z
                  && this.init_AtomicInteger.get() == gen
                  && System.nanoTime() < (!this.load_Z && ahead && this.write_Z ? Math.min(deadline, began + 4000000000L) : deadline)
                  && !(settled = search.init_Z_I(16))
            ) {
               long now = System.nanoTime();
               if (this.load_Z && !ahead && now >= nextProgress) {
                  synchronized (this) {
                     if (this.init_AtomicInteger.get() == gen) {
                        this.init_ClassCache_EventBu = new ClassCache.Inner_EventBus(expanded + search.init_I(), search.init_List());
                     }
                  }

                  nextProgress = now + 350000000L;
               }
            }

            if (!settled) {
               search.init_V();
            }

            result = search.init_ClassCache_PacketU();
            rounds++;
            queries += search.load_J();
            generated += search.read();
            expanded += search.init_I();
            peak = Math.max(peak, search.write());
            if (this.init_Z || this.init_AtomicInteger.get() != gen) {
               return;
            }

            if ((ordinaryFirst || search.init_ClassCache_RenderS.sync_Z)
               && !result.reachesGoal()
               && !result.awaitingTerrain()
               && result.movements().isEmpty()
               && !result.hitPending()) {
               if (search.init_ClassCache_RenderS.update_Z) {
                  quickRollout = false;
               } else if (ordinaryFirst) {
                  ordinaryFirst = false;
               } else {
                  limitedAirSearch = false;
               }
            } else if (this.load_Z || !result.exhausted() || result.hitPending() || result.reachesGoal() || System.nanoTime() >= deadline) {
               if (result.hitPending() && !result.reachesGoal() && !result.awaitingTerrain() && result.movements().isEmpty() && System.nanoTime() < deadline) {
                  try {
                     if (this.init_ClassCache_StateCa instanceof ClassCache.Inner_StreamUtil clientGrid) {
                        clientGrid.init_V_J_J(available, deadline);
                        continue;
                     }
                  } catch (InterruptedException e) {
                     return;
                  }
               }
               break;
            }
         }

         synchronized (this) {
            if (!this.init_Z && this.init_AtomicInteger.get() == gen && result != null) {
               this.init_ClassCache_ByteRea = new ClassCache.Inner_ByteReader(System.nanoTime() - began, rounds, expanded, generated, queries, peak);
               this.init_ClassCache_EventBu = null;
               this.init_ClassCache_BufferH = new ClassCache.Inner_BufferHelper.Inner_StreamUtil(gen, from, result);
            }

            this.notifyAll();
         }

         long ms = (System.nanoTime() - began) / 1000000L;
         if (result != null && (ms >= 400L && !ahead || ListPool.init_Z())) {
            String line = String.format(
               Locale.ROOT,
               "plan%s from %s: %d ms, %d expanded, %d rounds, %s, %d moves%s",
               ahead ? " ahead" : "",
               from.toShortString(),
               ms,
               expanded,
               rounds,
               result.reachesGoal() ? "reaches the goal" : "partial",
               result.movements().size(),
               result.movements().isEmpty() ? "" : " to " + result.movements().get(result.movements().size() - 1).to().toShortString()
            );
            if (ms >= 400L && !ahead) {
               AutismClientAddon.LOG.info("[AI] slow " + line);
            } else {
               ListPool.init(line);
            }
         }
      }

      private record Inner_DataUtil(long expiresAt, double x, double y, double z) {
      }

      private record Inner_StreamUtil(int generation, BlockPos from, ClassCache.Inner_PacketUtil segment) {
      }
   }

   public record Inner_ByteReader(long elapsedNanos, int rounds, int expanded, long generated, long blockQueries, int peakOpen) {
   }

   public static final class Inner_CachePool {
      public static final double init_D = 4.5;
      public static final double load_D = 6.4;
      public static final double read_D = 3.2;
      public static final double write_D = 4.5;
      public static final double update = 2.9000000000000004;
      public static final double sync = 2.2;
      public static final double flush = 2.5;
      public static final double close = 3.6;
      public static final double open = 25.0;
      public static final double poll = 32.0;
      public static final double push = 41.0;
      public static final double pop = 255.0;
      public static final double tick = 400.0;
      public static final double run = 2.0;
      public static final double get = 14.0;
      public static final double set = 5.0;
      public static final double put = 9.0;
      public static final double add = 3.0;
      public static final double clear = 4.0;
      public static final double reset = 40.0;
      public static final double start = 200.0;
      public static final double stop = 60.0;
      public static final double next = 12.0;
      public static final double prev = 4.5;
      public static final double valid = 10.0;
      public static final double size = 2.0;
      public static final double data = 5.5;
      public static final double value = 3.0;
      public static final double cache = 5.0;
      public static final double state = 0.5625;
      public static final double queue = 1.25;
      public static final int init_I = 3;
      public static final int load_I = 8;
      public static final double buffer = 0.25;
      public static final int read_I = 40;
      public static final int write_I = 2;
      public static final double entry = 60.0;
      public static final double node = 12.0;
      public static final double task = 8.0;
      public static final double pool = 40.0;
      public static final double index = 12.0;
      public static final double count = 10.0;
      public static final double flags = 40.0;
      public static final double level = 60.0;
      public static final double frame = 2.0;
      public static final double block = 1.0;
      public static final double chunk = 8.0;
      public static final double table = 2.0;
      public static final double store = 2.0;
      public static final double drain = 2.0;
      public static final double fill = 2.0;
      public static final double mark = 7.0;
      public static final double scan = 2.0;
      public static final double parse = 20.0;
      private static volatile long init_J;
      public static final double bind = 60.0;
      private static volatile Set<Long> init_Set = Set.of();
      private static volatile long load_J;
      private static volatile long read_J;
      private static volatile long write_J;
      static final double f_a_D = Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0)));
      static final double f_b_D = Math.min(9.0 + f_a_D, 12.0);

      private Inner_CachePool() {
      }

      public static double init_D_I(int gap) {
         return 7.0 + (gap + 1) * 3.2 + 2.0 + (gap - 2);
      }

      public static double load_D_I(int gap) {
         return init_D_I(gap) + 2.0;
      }

      public static void init_V_Set(Set<Long> cells) {
         init_Set = Set.copyOf(cells);
         load_J = cells.isEmpty() ? 0L : System.nanoTime() + 3000000000L;
      }

      static double init_D_List(List<BlockPos> cells) {
         if (!cells.isEmpty() && System.nanoTime() < load_J) {
            Set<Long> set = init_Set;
            double sum = 0.0;

            for (BlockPos cell : cells) {
               if (set.contains(cell.asLong())) {
                  sum += 60.0;
               }
            }

            return sum;
         } else {
            return 0.0;
         }
      }

      public static void init_V_J(long nanos) {
         init_J = nanos <= 0L ? 0L : System.nanoTime() + nanos;
      }

      static boolean init() {
         return System.nanoTime() < init_J;
      }

      public static void load_V_J(long nanos) {
         read_J = nanos <= 0L ? 0L : System.nanoTime() + nanos;
      }

      static boolean load() {
         return System.nanoTime() < read_J;
      }

      public static void read_V_J(long nanos) {
         write_J = nanos <= 0L ? 0L : System.nanoTime() + nanos;
      }

      static boolean read() {
         return System.nanoTime() < write_J;
      }

      public static double init_D_TableIndex_StateCa(TableIndex.Inner_StateCache plan) {
         return 2.0 + plan.ticks() + Math.max(0.0, 0.2 - plan.margin()) * 10.0;
      }

      public static double read_D_I(int blocks) {
         return 5.0 + 3.0 * blocks;
      }

      public static double write(int blocks) {
         return 2.0 + blocks;
      }

      public static double init_D_F(float hardness, double speed, boolean correctTool) {
         if (hardness < 0.0F) {
            return 1.0 / 0.0;
         }

         if (!(speed > 0.0) || !Double.isFinite(speed)) {
            return 1.0 / 0.0;
         }

         if (hardness == 0.0F) {
            return 6.0;
         }

         double progress = speed / hardness / (correctTool ? 30.0 : 100.0);
         return progress >= 1.0 ? 6.0 : Math.ceil(1.0 / progress) + 5.0;
      }

      public static int init_I_Str(String blockPath) {
         short var10000;
         switch (blockPath) {
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

      public static double init_D_D(double breakTicks) {
         return breakTicks + 2.0;
      }

      public static double load_D_D(double sumBreakTicks) {
         return sumBreakTicks + 4.5;
      }

      public static int init_I_D(double costTicks) {
         return (int)Math.ceil(costTicks * 3.0) + 20;
      }

      public static double init_D_I(int dx, int dy, int dz) {
         return Math.hypot(dx, dz) * Math.min(2.9000000000000004, Math.min(3.2, 4.5 / Math.sqrt(2.0))) + Math.abs(dy) * Math.min(5.5, 4.0);
      }

      public static double load(int dx, int rise, int dz) {
         double across = Math.sqrt((double)dx * dx + (double)dz * dz) * f_a_D;
         return rise <= 0 ? across - rise * Math.min(5.5, 4.0) : across + rise * f_b_D;
      }
   }

   record Inner_ConfigStore(ClassCache.Inner_SyncManager type, long from, long to) {
      static ClassCache.Inner_ConfigStore of(ClassCache.UiHelper move) {
         return new ClassCache.Inner_ConfigStore(move.type(), move.from().asLong(), move.to().asLong());
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
         boolean supportedBridges
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            disabledTypes,
            constructionBlocks,
            maxFall,
            avoidWater,
            repairFarmland,
            supportedBridges,
            ClassCache.Inner_NetHelper.init_ClassCache_NetHelp
         );
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
         boolean repairFarmland
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            disabledTypes,
            constructionBlocks,
            maxFall,
            avoidWater,
            repairFarmland,
            false
         );
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
         boolean avoidWater
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            disabledTypes,
            constructionBlocks,
            maxFall,
            avoidWater,
            false
         );
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
         int constructionBlocks
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            disabledTypes,
            constructionBlocks,
            3,
            false
         );
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
         Set<ClassCache.Inner_SyncManager> disabledTypes
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            disabledTypes,
            hasBlocks ? 2147483647 : 0
         );
      }

      public Inner_DataUtil {
         if (constructionBlocks < 0) {
            throw new IllegalArgumentException("Negative construction inventory");
         }

         disabledTypes = Set.copyOf(disabledTypes);
      }

      public Inner_DataUtil(boolean allowDig, boolean allowPillar, boolean allowBridge, boolean canSprint, boolean hasBlocks) {
         this(allowDig, allowPillar, allowBridge, canSprint, hasBlocks, false, ClassCache.Inner_IoWorker.init, ClassCache.Inner_PoolEntry.write);
      }

      public Inner_DataUtil(
         boolean allowDig,
         boolean allowPillar,
         boolean allowBridge,
         boolean canSprint,
         boolean hasBlocks,
         ClassCache.Inner_IoWorker strategy,
         ClassCache.Inner_PoolEntry threats
      ) {
         this(allowDig, allowPillar, allowBridge, canSprint, hasBlocks, false, strategy, threats);
      }

      public Inner_DataUtil(
         boolean allowDig,
         boolean allowPillar,
         boolean allowBridge,
         boolean canSprint,
         boolean hasBlocks,
         boolean allowParkour,
         ClassCache.Inner_IoWorker strategy,
         ClassCache.Inner_PoolEntry threats
      ) {
         this(allowDig, allowPillar, allowBridge, canSprint, hasBlocks, allowParkour, strategy, threats, false, false, false);
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
         boolean levitation
      ) {
         this(allowDig, allowPillar, allowBridge, canSprint, hasBlocks, allowParkour, strategy, threats, jumpBoost, slowFalling, levitation, 2.0);
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
         double breakReluctance
      ) {
         this(
            allowDig,
            allowPillar,
            allowBridge,
            canSprint,
            hasBlocks,
            allowParkour,
            strategy,
            threats,
            jumpBoost,
            slowFalling,
            levitation,
            breakReluctance,
            Set.of()
         );
      }

      ClassCache.Inner_DataUtil withFallPolicy(ClassCache.Inner_NetHelper policy) {
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
            policy
         );
      }

      public ClassCache.Inner_DataUtil withSupportedBridges(boolean allowed) {
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
            allowed,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withFarmlandRepair(boolean allowed) {
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
            allowed,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withConstructionBlocks(int blocks) {
         return new ClassCache.Inner_DataUtil(
            this.allowDig,
            this.allowPillar,
            this.allowBridge,
            this.canSprint,
            blocks > 0,
            this.allowParkour,
            this.strategy,
            this.threats,
            this.jumpBoost,
            this.slowFalling,
            this.levitation,
            this.breakReluctance,
            this.disabledTypes,
            blocks,
            this.maxFall,
            this.avoidWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      public ClassCache.Inner_DataUtil withMovementLimits(int fallCap, boolean noWater) {
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
            Math.max(0, Math.min(3, fallCap)),
            noWater,
            this.repairFarmland,
            this.supportedBridges,
            this.fallPolicy
         );
      }

      ClassCache.Inner_DataUtil withoutConstruction() {
         return new ClassCache.Inner_DataUtil(
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

      public ClassCache.Inner_DataUtil withBreakReluctance(double reluctance) {
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
            reluctance,
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

      public ClassCache.Inner_DataUtil withDisabledTypes(Set<ClassCache.Inner_SyncManager> disabled) {
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
            disabled,
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
      boolean narrowFooting
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
         boolean portal
      ) {
         this(
            loaded,
            passable,
            top16,
            water,
            lava,
            climbable,
            hazardFeet,
            hazardFloor,
            falling,
            fallMult10,
            breakTicks,
            door,
            slabStair,
            farmland,
            ice,
            cached,
            destructionPenalty,
            soulSand,
            thorny,
            portal,
            false
         );
      }

      public Inner_FileCache(
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
         boolean thorny
      ) {
         this(
            loaded,
            passable,
            top16,
            water,
            lava,
            climbable,
            hazardFeet,
            hazardFloor,
            falling,
            fallMult10,
            breakTicks,
            door,
            slabStair,
            farmland,
            ice,
            cached,
            destructionPenalty,
            soulSand,
            thorny,
            false
         );
      }

      public Inner_FileCache(
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
         boolean soulSand
      ) {
         this(
            loaded,
            passable,
            top16,
            water,
            lava,
            climbable,
            hazardFeet,
            hazardFloor,
            falling,
            fallMult10,
            breakTicks,
            door,
            slabStair,
            farmland,
            ice,
            cached,
            destructionPenalty,
            soulSand,
            false
         );
      }

      public Inner_FileCache(
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
         boolean cached
      ) {
         this(
            loaded,
            passable,
            top16,
            water,
            lava,
            climbable,
            hazardFeet,
            hazardFloor,
            falling,
            fallMult10,
            breakTicks,
            door,
            slabStair,
            farmland,
            ice,
            cached,
            0,
            false
         );
      }

      boolean floorContactHazard() {
         return this.hazardFloor && !this.narrowFooting;
      }

      public ClassCache.Inner_FileCache withDestructionPenalty(int ticks) {
         if (ticks < 0) {
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
               ticks,
               this.soulSand,
               this.thorny,
               this.portal,
               this.narrowFooting
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

      public static ClassCache.Inner_FileCache fullBlock(double breakTicks) {
         return new ClassCache.Inner_FileCache(true, false, 16, false, false, false, false, false, false, 10, breakTicks, false, false, false, false, false);
      }

      public static ClassCache.Inner_FileCache doorCell(double breakTicks) {
         return new ClassCache.Inner_FileCache(true, false, 16, false, false, false, false, false, false, 10, breakTicks, true, false, false, false, false);
      }

      public ClassCache.Inner_FileCache withTop16(int top) {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            top,
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
            this.narrowFooting
         );
      }

      public ClassCache.Inner_FileCache withFallMult10(int mult) {
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
            mult,
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
            this.narrowFooting
         );
      }

      public ClassCache.Inner_FileCache withHazards(boolean feet, boolean floor) {
         return new ClassCache.Inner_FileCache(
            this.loaded,
            this.passable,
            this.top16,
            this.water,
            this.lava,
            this.climbable,
            feet,
            floor,
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
            floor && this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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
            this.narrowFooting
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

   public enum Inner_HashUtil {
      init,
      load,
      read;
   }

   public enum Inner_IoWorker {
      init,
      load,
      read;

      public static ClassCache.Inner_IoWorker init(String text) {
         try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("strategy must be fastest, safest or saver");
         }
      }
   }

   public interface Inner_JsonUtil {
      boolean isInGoal(int var1, int var2, int var3);

      default boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
         return this.isInGoal(x, y, z);
      }

      double heuristic(int var1, int var2, int var3);

      default ClassCache.Inner_JsonUtil init(ClassCache.Inner_StateCache grid) {
         return this;
      }

      default boolean read(BlockPos cell) {
         return true;
      }

      default boolean init_Z() {
         return false;
      }

      default boolean load(BlockPos cell) {
         return true;
      }

      default int init(ItemStack stack) {
         return 0;
      }

      default double init_D() {
         return 40.0;
      }

      default boolean write(BlockPos cell) {
         return false;
      }

      default boolean init_Z(BlockPos cell) {
         return true;
      }

      default Set<ClassCache.Inner_SyncManager> init_Set() {
         return Set.of();
      }

      default boolean outsideSearchBox(BlockPos origin, int horizontal, int vertical) {
         return false;
      }

      default BlockPos init_BlockPos() {
         return null;
      }

      static ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_BlockPos_D(BlockPos target, double radius) {
         return init_ClassCache_JsonUti_BlockPos(target, radius, false);
      }

      static ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_BlockPos(final BlockPos target, final double radius, final boolean acceptFloorBelowAirTarget) {
         final double radiusSqr = radius * radius;
         final double radiusAllowance = radius * Math.hypot(ClassCache.Inner_CachePool.f_a_D, ClassCache.Inner_CachePool.f_b_D);
         return new ClassCache.Inner_JsonUtil() {
            @Override
            public BlockPos init_BlockPos() {
               return target;
            }

            @Override
            public boolean isInGoal(int x, int y, int z) {
               double dx = x - target.getX();
               double dy = y - target.getY();
               double dz = z - target.getZ();
               return dx * dx + dy * dy + dz * dz <= radiusSqr;
            }

            @Override
            public boolean init_Z_I(int x, int y, int z, ClassCache.Inner_StateCache planned) {
               if (this.isInGoal(x, y, z)) {
                  return true;
               }

               double dx = x - target.getX();
               double dz = z - target.getZ();
               if (planned == null || dx * dx + dz * dz > radiusSqr) {
                  return false;
               }

               if (acceptFloorBelowAirTarget && dx == 0.0 && dz == 0.0 && y == target.getY() - 1) {
                  ClassCache.Inner_FileCache head = planned.init_ClassCache_FileCac_I(x, y + 1, z);
                  ClassCache.Inner_FileCache extra = planned.init_ClassCache_FileCac_I(x, y + 2, z);
                  ClassCache.Inner_FileCache feet = planned.init_ClassCache_FileCac_I(x, y, z);
                  ClassCache.Inner_FileCache floor = planned.init_ClassCache_FileCac_I(x, y - 1, z);
                  return head != null
                     && extra != null
                     && feet != null
                     && floor != null
                     && head.passable()
                     && extra.passable()
                     && head.loaded()
                     && extra.loaded()
                     && !head.cached()
                     && !extra.cached()
                     && !feet.cached()
                     && !floor.cached()
                     && !head.water()
                     && !head.climbable()
                     && ClassCache.Inner_RenderState.init_Z_ClassCache_FileCac(feet, head, floor);
               }

               if (y != target.getY() + 1) {
                  return false;
               }

               ClassCache.Inner_FileCache support = planned.init_ClassCache_FileCac_I(target.getX(), target.getY(), target.getZ());
               return support != null
                  && support.loaded()
                  && !support.cached()
                  && !support.passable()
                  && support.top16() > 0
                  && support.top16() < 16
                  && !support.door()
                  && !support.hazardFloor()
                  && !support.lava();
            }

            @Override
            public double heuristic(int x, int y, int z) {
               int rise = target.getY() - y;
               if (rise < 0) {
                  rise++;
               } else if (acceptFloorBelowAirTarget && rise > 0) {
                  rise--;
               }

               double remaining = ClassCache.Inner_CachePool.load(x - target.getX(), rise, z - target.getZ());
               if (acceptFloorBelowAirTarget && rise > 0) {
                  double ascent = rise * ClassCache.Inner_CachePool.f_b_D;
                  remaining = Math.max(remaining - ascent, ascent);
               }

               return Math.max(0.0, remaining - radiusAllowance);
            }

            @Override
            public boolean outsideSearchBox(BlockPos origin, int horizontal, int vertical) {
               return Math.abs(target.getX() - origin.getX()) > horizontal + radius
                  || Math.abs(target.getZ() - origin.getZ()) > horizontal + radius
                  || Math.abs(target.getY() - origin.getY()) > vertical + radius;
            }

            @Override
            public String toString() {
               return "near(" + target.toShortString() + ", r=" + radius + ")";
            }
         };
      }

      static ClassCache.Inner_JsonUtil init_ClassCache_JsonUti_I_BlockPos(final int y, final BlockPos bias) {
         return new ClassCache.Inner_JsonUtil() {
            @Override
            public boolean isInGoal(int x, int yy, int z) {
               return yy == y;
            }

            @Override
            public double heuristic(int x, int yy, int z) {
               return Math.abs(yy - y) * 4.0 + Math.hypot(x - bias.getX(), z - bias.getZ()) * 0.5;
            }

            @Override
            public String toString() {
               return "yLevel(" + y + ")";
            }
         };
      }
   }

   enum Inner_NetHelper {
      init_ClassCache_NetHelp(4.0),
      load(3.0),
      read(32.0);

      final double init_D;

      Inner_NetHelper(double maxJumpFall) {
         this.init_D = maxJumpFall;
      }

      boolean init() {
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
      public Inner_PacketUtil(
         List<ClassCache.UiHelper> movements,
         boolean reachesGoal,
         int expanded,
         boolean hitPending,
         boolean exhausted,
         boolean wedgedStart,
         boolean confinedStart
      ) {
         this(movements, reachesGoal, expanded, hitPending, exhausted, wedgedStart, confinedStart, false);
      }

      public Inner_PacketUtil(List<ClassCache.UiHelper> movements, boolean reachesGoal, int expanded, boolean hitPending, boolean exhausted) {
         this(movements, reachesGoal, expanded, hitPending, exhausted, false, false);
      }

      public Inner_PacketUtil(
         List<ClassCache.UiHelper> movements, boolean reachesGoal, int expanded, boolean hitPending, boolean exhausted, boolean wedgedStart
      ) {
         this(movements, reachesGoal, expanded, hitPending, exhausted, wedgedStart, false);
      }

      public Inner_PacketUtil {
         movements = List.copyOf(movements);
      }

      public BlockPos destination() {
         return this.movements.isEmpty() ? null : this.movements.get(this.movements.size() - 1).to();
      }

      public double totalCostTicks() {
         double sum = 0.0;

         for (ClassCache.UiHelper m : this.movements) {
            sum += m.costTicks();
         }

         return sum;
      }
   }

   public interface Inner_PoolEntry {
      ClassCache.Inner_PoolEntry write = (x, y, z) -> 0.0;

      double surcharge(int var1, int var2, int var3);

      default double init(int x, int y, int z) {
         return 0.0;
      }

      default double init(int fx, int fy, int fz, int tx, int ty, int tz) {
         return 0.0;
      }

      static ClassCache.Inner_PoolEntry init(List<BlockPos> mobs) {
         List<BlockPos> copy = List.copyOf(mobs);
         return copy.isEmpty() ? write : (x, y, z) -> {
            double nearestSqr = 1.7976931348623157E308;

            for (BlockPos mob : copy) {
               double dx = x - mob.getX();
               double dy = y - mob.getY();
               double dz = z - mob.getZ();
               nearestSqr = Math.min(nearestSqr, dx * dx + dy * dy + dz * dz);
            }

            double range = 12.0;
            return nearestSqr >= range * range ? 0.0 : 60.0 * (1.0 - Math.sqrt(nearestSqr) / range);
         };
      }
   }

   public static final class Inner_RenderState {
      private static final int[][] init_IArr2 = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
      private static final int[][] load_IArr2 = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
      static final int init_I = 1;
      static final int load_I = 2;
      static final int read_I = 4;
      static final int write = 5;
      private static final int[][] read_IArr2 = new int[][]{{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] init_ClassCache_RenderSArr = init(5);
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] load_ClassCache_RenderSArr = init(11);
      private static final ClassCache.Inner_RenderState.Inner_StreamUtil[] read_ClassCache_RenderSArr = init(8);
      private static final int close = 14;
      static final double init_D = 0.1;
      static final double load_D = 0.1;
      static final int update = 3;
      static final int sync = 4;
      static final int flush = 32;
      static boolean init_Z = true;

      private Inner_RenderState() {
      }

      public static boolean init_Z_ClassCache_FileCac(ClassCache.Inner_FileCache feet, ClassCache.Inner_FileCache head, ClassCache.Inner_FileCache floor) {
         if (!feet.loaded() || !head.loaded() || !floor.loaded()) {
            return false;
         }

         if (feet.hazardFeet() || head.hazardFeet() || floor.hazardFloor() || feet.lava() || head.lava() || floor.lava()) {
            return false;
         }

         if (feet.water() && head.water()) {
            return false;
         }

         if (!floor.passable() && floor.top16() > 16) {
            return false;
         }

         boolean headClear = head.passable() || !floor.passable() && floor.top16() > 0 && floor.top16() <= 3;
         return feet.passable() && headClear && (!floor.passable() && !floor.door() || feet.water() || feet.climbable());
      }

      static boolean init_Z_Function_2(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos to, TableIndex.Inner_StateCache plan) {
         ClassCache.Inner_FileCache floor = at.apply(to.below());
         if (floor.loaded()
            && !floor.passable()
            && floor.top16() <= 24
            && !floor.floorContactHazard()
            && !floor.farmland()
            && !floor.lava()
            && !floor.water()
            && !floor.door()) {
            boolean obstructed = floor.narrowFooting();
            int head = floor.top16() > 16 ? 2 : 1;

            for (int i = 0; i <= head; i++) {
               BlockPos body = to.above(i);
               ClassCache.Inner_FileCache cell = at.apply(body);
               if (!cell.loaded() || cell.hazardFeet() || cell.lava() || cell.water() || cell.climbable() || cell.web() || cell.portal()) {
                  return false;
               }

               if (!cell.passable()) {
                  if (at instanceof ClassCache.Inner_RenderState.Inner_CachePool probes
                     && (!probes.read_Z || probes.init_S_I(body.getX(), body.getY(), body.getZ()) == 1)) {
                     return false;
                  }

                  obstructed = true;
               }
            }

            if (!obstructed) {
               return floor.top16() > 16;
            } else {
               return plan != null
                  ? true
                  : at instanceof ClassCache.Inner_RenderState.Inner_CachePool probes
                     && TableIndex.init_Z(probes.init_StreamGate(), to, init(to.getY(), floor));
            }
         } else {
            return false;
         }
      }

      private static boolean init_Z_ClassCache_RenderS(ClassCache.Inner_RenderState.Inner_CachePool at, BlockPos cell, ClassCache.Inner_FileCache floor) {
         if (floor.loaded() && floor.top16() > 0 && floor.top16() <= 8 && !floor.hazardFloor() && !floor.lava() && !floor.water() && !floor.farmland()) {
            ClassCache.Inner_FileCache head = at.init_ClassCache_FileCac_BlockPos(cell.above());
            if (head.loaded() && !head.passable() && !head.hazardFeet()) {
               double y = init(cell.getY(), floor);
               double x = cell.getX() + 0.5;
               double z = cell.getZ() + 0.5;
               double half = 0.3;
               AABB crouched = new AABB(x - half, y + 1.0E-5, z - half, x + half, y + 1.5 - 1.0E-5, z + half);
               return at.init_StreamGate().init_List(crouched).isEmpty() && !TableIndex.init_Z_NodeWalker_CachePo(at.init_StreamGate(), x, y, z);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }

      private static void init(
         ClassCache.Inner_RenderState.Inner_CachePool at,
         ClassCache.Inner_DataUtil caps,
         Set<Long> blacklist,
         BlockPos from,
         double fromY,
         boolean crouched,
         List<ClassCache.UiHelper> out
      ) {
         int reach = crouched ? 2 : 1;

         for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
               if (dx != 0 || dz != 0) {
                  for (int dy = 1; dy >= -1; dy--) {
                     BlockPos to = from.offset(dx, dy, dz);
                     if (!blacklist.contains(to.asLong())) {
                        ClassCache.Inner_FileCache floor = at.init_ClassCache_FileCac_BlockPos(to.below());
                        if (floor.loaded() && !floor.passable() && !floor.hazardFloor() && !floor.water() && !floor.lava()) {
                           double y = init(to.getY(), floor);
                           if (!(y > fromY + 0.5625) && !(y < fromY - 1.25)) {
                              boolean needsCrouch = init_Z_ClassCache_RenderS(at, to, floor);
                              if (needsCrouch
                                 || crouched
                                    && init_Z_ClassCache_FileCac(
                                       at.init_ClassCache_FileCac_BlockPos(to), at.init_ClassCache_FileCac_BlockPos(to.above()), floor
                                    )) {
                                 TableIndex.Inner_StateCache plan = PoolEntry.lowClearance(
                                    at.init_StreamGate(),
                                    from,
                                    fromY,
                                    to,
                                    y,
                                    crouched,
                                    needsCrouch,
                                    caps.canSprint() && caps.strategy() != ClassCache.Inner_IoWorker.read
                                 );
                                 if (plan != null) {
                                    out.add(ClassCache.UiHelper.jump(from, to, ClassCache.Inner_CachePool.init_D_TableIndex_StateCa(plan), plan));
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

      private static boolean init_Z_ClassCache_RenderS(
         ClassCache.Inner_RenderState.Inner_CachePool at, BlockPos support, int dx, int dz, double fromSurface, double top
      ) {
         short shape = at.init_S_I(support.getX(), support.getY(), support.getZ());
         double entryTop = -1.0 / 0.0;

         for (AABB box : e.init_List_S(shape)) {
            if ((dx <= 0 || !(box.minX > 1.0E-6))
               && (dx >= 0 || !(box.maxX < 0.999999))
               && (dz <= 0 || !(box.minZ > 1.0E-6))
               && (dz >= 0 || !(box.maxZ < 0.999999))) {
               double sideMin = dx != 0 ? box.minZ : box.minX;
               double sideMax = dx != 0 ? box.maxZ : box.maxX;
               if (!(sideMin >= 0.8) && !(sideMax <= 0.2)) {
                  entryTop = Math.max(entryTop, box.maxY);
               }
            }
         }

         return Double.isFinite(entryTop) && top > entryTop && support.getY() + entryTop - fromSurface <= 0.5625 && top - entryTop <= 0.5625;
      }

      static double init(int cellY, ClassCache.Inner_FileCache floorBelow) {
         return cellY - 1 + floorBelow.top16() / 16.0;
      }

      static boolean init(ClassCache.Inner_FileCache fromPlus2, ClassCache.Inner_FileCache fromPlus3) {
         return fromPlus2.passable() && fromPlus3.passable();
      }

      public static int init_I_ClassCache_StateCa(
         ClassCache.Inner_StateCache grid, ClassCache.Inner_DataUtil caps, Set<Long> blacklist, BlockPos from, List<ClassCache.UiHelper> out
      ) {
         return init_I_ClassCache_RenderS(new ClassCache.Inner_RenderState.Inner_CachePool(grid, 256), caps, blacklist, from, out);
      }

      static int init_I_ClassCache_RenderS(
         ClassCache.Inner_RenderState.Inner_CachePool at, ClassCache.Inner_DataUtil caps, Set<Long> blacklist, BlockPos from, List<ClassCache.UiHelper> out
      ) {
         at.init_I = 0;
         at.init_ClassCache_NetHelp = caps.fallPolicy();
         ClassCache.Inner_FileCache f = at.init_ClassCache_FileCac_BlockPos(from);
         if (!f.loaded()) {
            return at.init_I;
         }

         ClassCache.Inner_FileCache fromFloor = at.init_ClassCache_FileCac_BlockPos(from.below());
         ClassCache.Inner_FileCache fromHead = at.init_ClassCache_FileCac_BlockPos(from.above());
         boolean canJump = caps.allowParkour()
            && !caps.jumpBoost()
            && !caps.levitation()
            && !init(caps, ClassCache.Inner_SyncManager.start)
            && load(f, fromFloor)
            && !fromHead.web();
         double surfFrom = fromFloor.passable() ? from.getY() : init(from.getY(), fromFloor);
         boolean centredWalking = !fromFloor.narrowFooting()
            || TableIndex.init_Z_NodeWalker_CachePo(at.init_StreamGate(), from.getX() + 0.5, surfFrom, from.getZ() + 0.5);
         if (at.read_Z && canJump && init_Z_ClassCache_RenderS(at, from, fromFloor)) {
            init(at, caps, blacklist, from, surfFrom, true, out);
            return at.init_I;
         }

         if (!init_Z_ClassCache_FileCac(f, fromHead, fromFloor) && !f.water() && !f.climbable()) {
            boolean shapedStart = canJump && at.read_Z && init_Z_Function_2(at, from, null);
            if (!f.passable() || fromHead.loaded() && !fromHead.passable()) {
               if (shapedStart) {
                  init(at, caps, blacklist, from, fromFloor, surfFrom, out);
                  return at.init_I;
               }

               at.init_I |= 2;
               if (caps.allowDig() && !init(caps, ClassCache.Inner_SyncManager.pop)) {
                  init_V_ClassCache_RenderS(at, caps, blacklist, from, f, out);
               }

               return at.init_I;
            }

            if (!shapedStart && fromHead.loaded() && fromFloor.loaded()) {
               at.init_I |= 4;
            }
         }

         if (f.water() || f.climbable()) {
            BlockPos up = from.above();
            ClassCache.Inner_FileCache upP = at.init_ClassCache_FileCac_BlockPos(up);
            boolean rises = upP.climbable() || upP.water();
            if (!init(caps, ClassCache.Inner_SyncManager.get)
               && rises
               && !blacklist.contains(up.asLong())
               && (upP.passable() || upP.climbable())
               && !upP.lava()
               && (at.init_ClassCache_FileCac_BlockPos(up.above()).passable() || at.init_ClassCache_FileCac_BlockPos(up.above()).climbable())
               && !upP.hazardFeet()) {
               out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.get, from, up, 5.5));
            }

            BlockPos down = from.below();
            if (!init(caps, ClassCache.Inner_SyncManager.get)
               && !blacklist.contains(down.asLong())
               && init_Z_ClassCache_FileCac(
                  at.init_ClassCache_FileCac_BlockPos(down),
                  at.init_ClassCache_FileCac_BlockPos(down.above()),
                  at.init_ClassCache_FileCac_BlockPos(down.below())
               )) {
               out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.get, from, down, 5.5));
            }
         }

         for (int[] d : init_IArr2) {
            BlockPos flat = from.offset(d[0], 0, d[1]);
            if (!at.init(from, d[0], d[1]) && !at.init(flat, d[0], d[1])) {
               ClassCache.Inner_FileCache flatP = at.init_ClassCache_FileCac_BlockPos(flat);
               ClassCache.Inner_FileCache flatHead = at.init_ClassCache_FileCac_BlockPos(flat.above());
               ClassCache.Inner_FileCache flatFloor = at.init_ClassCache_FileCac_BlockPos(flat.below());
               if (caps.allowDig()
                  && !init(caps, ClassCache.Inner_SyncManager.pop)
                  && flatFloor.loaded()
                  && !flatFloor.passable()
                  && !flatFloor.hazardFloor()
                  && !flatFloor.lava()
                  && !blacklist.contains(flat.asLong())
                  && !flatP.hazardFeet()
                  && !flatP.lava()
                  && !flatHead.hazardFeet()
                  && !flatHead.lava()) {
                  boolean feetDig = flatP.diggable();
                  boolean headDig = flatHead.diggable();
                  if ((feetDig || flatP.passable()) && (headDig || flatHead.passable()) && (feetDig || headDig)) {
                     List<BlockPos> breaks = new ArrayList<>(2);
                     double sum = 0.0;
                     double raw = 0.0;
                     if (feetDig) {
                        breaks.add(flat);
                        sum += flatP.breakTicks() + caps.breakReluctance() + flatP.destructionPenalty();
                        raw += flatP.breakTicks();
                     }

                     if (headDig) {
                        breaks.add(flat.above());
                        sum += flatHead.breakTicks() + caps.breakReluctance() + flatHead.destructionPenalty();
                        raw += flatHead.breakTicks();
                     }

                     if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                        sum *= 2.0;
                     }

                     out.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.pop,
                           from,
                           flat,
                           breaks,
                           List.of(),
                           ClassCache.Inner_CachePool.load_D_D(sum) + init_D_Function(at, from, flat, false),
                           raw
                        )
                     );
                  }
               }

               BlockPos stepDown = flat.below();
               if (caps.allowDig() && !init(caps, ClassCache.Inner_SyncManager.next) && !blacklist.contains(stepDown.asLong())) {
                  ClassCache.Inner_FileCache landP = at.init_ClassCache_FileCac_BlockPos(stepDown);
                  ClassCache.Inner_FileCache landFloor = at.init_ClassCache_FileCac_BlockPos(stepDown.below());
                  if (landFloor.loaded()
                     && !landFloor.passable()
                     && !landFloor.hazardFloor()
                     && !landFloor.lava()
                     && !landFloor.farmland()
                     && landFloor.top16() == 16
                     && init(flatHead)
                     && init(flatP)
                     && init(landP)
                     && !flatHead.web()
                     && !flatP.web()
                     && !landP.web()
                     && (flatHead.diggable() || flatP.diggable() || landP.diggable())
                     && init_Z_Function(at, stepDown, d[0], d[1])) {
                     List<BlockPos> breaks = new ArrayList<>(3);
                     double sum = 0.0;
                     double raw = 0.0;

                     for (BlockPos cell : new BlockPos[]{flat.above(), flat, stepDown}) {
                        ClassCache.Inner_FileCache c = cell.equals(stepDown) ? landP : (cell.equals(flat) ? flatP : flatHead);
                        if (c.diggable()) {
                           breaks.add(cell);
                           sum += c.breakTicks() + caps.breakReluctance() + c.destructionPenalty();
                           raw += c.breakTicks();
                        }
                     }

                     if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                        sum *= 2.0;
                     }

                     double safest = caps.strategy() == ClassCache.Inner_IoWorker.load ? 1.0 : 0.0;
                     out.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.next, from, stepDown, breaks, List.of(), ClassCache.Inner_CachePool.read_D_I(1) + safest + sum, raw
                        )
                     );
                  }
               }

               if (caps.allowBridge()
                  && caps.hasBlocks()
                  && !init(caps, ClassCache.Inner_SyncManager.run)
                  && at.init_ClassCache_FileCac_BlockPos(from.below()).top16() == 16
                  && at.init_S_I(from.getX(), from.getY() - 1, from.getZ()) == 1
                  && !blacklist.contains(flat.asLong())
                  && flatP.passable()
                  && flatHead.passable()
                  && !flatP.hazardFeet()
                  && !flatP.lava()
                  && !flatHead.hazardFeet()
                  && !flatHead.lava()
                  && flatFloor.loaded()
                  && flatFloor.passable()
                  && (caps.supportedBridges() || !flatFloor.water() && !flatFloor.lava())
                  && (
                     caps.supportedBridges()
                        || !at.init_ClassCache_FileCac_BlockPos(flat.below(2)).water() && !at.init_ClassCache_FileCac_BlockPos(flat.below(2)).lava()
                  )
                  && init_Z_Function_3(at, from, flat, at.init_ClassCache_UiHelpe)) {
                  int drop = caps.supportedBridges() ? 0 : init_I(at, flat.below());
                  if ((caps.strategy() == ClassCache.Inner_IoWorker.load || ClassCache.Inner_CachePool.load()) && drop > 3) {
                     drop = -1;
                  }

                  if (drop >= 0) {
                     double bridge = caps.strategy() == ClassCache.Inner_IoWorker.read ? 9.0 : 4.5;
                     ClassCache.UiHelper before = at.init_ClassCache_UiHelpe;
                     boolean running = before != null
                        && before.type() == ClassCache.Inner_SyncManager.run
                        && before.to().getX() - before.from().getX() == flat.getX() - from.getX()
                        && before.to().getZ() - before.from().getZ() == flat.getZ() - from.getZ();
                     if (!running) {
                        bridge += 10.0;
                     }

                     if (caps.supportedBridges() || !at.init_ClassCache_FileCac_BlockPos(flat.above(2)).passable()) {
                        bridge += 2.0;
                     }

                     if (drop > 3) {
                        bridge += 1.125 * Math.min(drop - 3, 20);
                     }

                     out.add(new ClassCache.UiHelper(ClassCache.Inner_SyncManager.run, from, flat, List.of(), List.of(flat.below()), bridge));
                  }
               }

               if (ClassCache.Inner_CachePool.read()
                  && caps.allowBridge()
                  && caps.hasBlocks()
                  && !init(caps, ClassCache.Inner_SyncManager.run)
                  && at.init_ClassCache_FileCac_BlockPos(from.below()).top16() == 16
                  && at.init_S_I(from.getX(), from.getY() - 1, from.getZ()) == 1
                  && flatP.passable()
                  && flatHead.passable()
                  && !flatP.hazardFeet()
                  && !flatP.lava()
                  && !flatHead.hazardFeet()
                  && !flatHead.lava()
                  && flatFloor.loaded()
                  && flatFloor.passable()
                  && !flatFloor.water()
                  && !flatFloor.lava()
                  && !flatFloor.hazardFeet()) {
                  BlockPos low = flat.below();
                  BlockPos support = low.below();
                  ClassCache.Inner_FileCache sup = at.init_ClassCache_FileCac_BlockPos(support);
                  if (!blacklist.contains(low.asLong()) && sup.loaded() && sup.passable() && !sup.water() && !sup.lava() && read(at, support)) {
                     int drop = init_I(at, support);
                     if ((caps.strategy() == ClassCache.Inner_IoWorker.load || ClassCache.Inner_CachePool.load()) && drop > 3) {
                        drop = -1;
                     }

                     if (drop >= 0) {
                        out.add(
                           new ClassCache.UiHelper(
                              ClassCache.Inner_SyncManager.run, from, low, List.of(), List.of(support), 14.5 + ClassCache.Inner_CachePool.read_D_I(1)
                           )
                        );
                     }
                  }
               }

               if (centredWalking
                  && !blacklist.contains(flat.asLong())
                  && init_Z_ClassCache_FileCac(flatP, flatHead, flatFloor)
                  && (!caps.avoidWater() || !flatP.water())) {
                  double rise = init(flat.getY(), flatFloor) - surfFrom;
                  if (rise <= 0.5625) {
                     boolean sprint = caps.canSprint()
                        && caps.strategy() != ClassCache.Inner_IoWorker.read
                        && !flatP.water()
                        && !flatP.thorny()
                        && !init(caps, ClassCache.Inner_SyncManager.read);
                     double water = flatP.water() ? 3.0 + (caps.strategy() == ClassCache.Inner_IoWorker.load ? 8.0 : 0.0) : 0.0;
                     double slow = flatFloor.crawl();
                     double thorns = flatP.thorny() ? 25.0 : 0.0;
                     ClassCache.Inner_SyncManager flatType = sprint ? ClassCache.Inner_SyncManager.read : ClassCache.Inner_SyncManager.init;
                     if (!init(caps, flatType)) {
                        out.add(
                           ClassCache.UiHelper.travel(
                              flatType,
                              from,
                              flat,
                              (sprint ? 3.2 : 4.5 + water) * slow + thorns + init_D_Function(at, from, flat, sprint) + at.init_D_BlockPos(flat)
                           )
                        );
                     }

                     BlockPos past = flat.offset(d[0], 0, d[1]);
                     ClassCache.Inner_FileCache beyondFloor = at.init_ClassCache_FileCac_BlockPos(past.below());
                     if (sprint
                        && !beyondFloor.passable()
                        && !init(caps, ClassCache.Inner_SyncManager.reset)
                        && write(at, from, flat)
                        && !at.load(flat)
                        && !at.load(past)) {
                        out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.reset, from, flat, update(at, from, flat) ? 2.2 : 2.9000000000000004));
                     }
                  } else if (rise <= 1.250001
                     && !f.water()
                     && !fromFloor.farmland()
                     && !fromFloor.honey()
                     && !init(caps, ClassCache.Inner_SyncManager.sync)
                     && !f.web()
                     && !fromHead.web()
                     && !flatP.web()
                     && !flatHead.web()
                     && !at.init_ClassCache_FileCac_BlockPos(from.above(2)).web()
                     && at.init_ClassCache_FileCac_BlockPos(from.above(2)).passable()) {
                     out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.sync, from, flat, 9.0));
                  }
               }

               if (caps.allowDig()
                  && !init(caps, ClassCache.Inner_SyncManager.pop)
                  && (flatP.web() || flatHead.web())
                  && !blacklist.contains(flat.asLong())
                  && init_Z_ClassCache_FileCac(flatP, flatHead, flatFloor)
                  && init(flat.getY(), flatFloor) - surfFrom <= 0.5625) {
                  List<BlockPos> cut = new ArrayList<>(2);
                  double sum = 0.0;
                  double raw = 0.0;

                  for (BlockPos cell : new BlockPos[]{flat, flat.above()}) {
                     ClassCache.Inner_FileCache c = cell == flat ? flatP : flatHead;
                     if (c.web()) {
                        if (c.breakTicks() >= 1.0 / 0.0) {
                           cut = null;
                           break;
                        }

                        cut.add(cell);
                        sum += c.breakTicks() + caps.breakReluctance();
                        raw += c.breakTicks();
                     }
                  }

                  if (cut != null && !cut.isEmpty()) {
                     out.add(
                        new ClassCache.UiHelper(
                           ClassCache.Inner_SyncManager.pop,
                           from,
                           flat,
                           cut,
                           List.of(),
                           ClassCache.Inner_CachePool.load_D_D(sum) + at.init_D_BlockPos(flat),
                           raw
                        )
                     );
                  }
               }

               if (!blacklist.contains(flat.asLong())
                  && flatP.door()
                  && !flatP.passable()
                  && !init(caps, ClassCache.Inner_SyncManager.set)
                  && !flatP.hazardFeet()
                  && !flatP.lava()
                  && !flatHead.hazardFeet()
                  && !flatHead.lava()
                  && (flatHead.door() || flatHead.passable())
                  && flatFloor.loaded()
                  && !flatFloor.passable()
                  && !flatFloor.hazardFloor()
                  && !flatFloor.lava()
                  && init(flat.getY(), flatFloor) - surfFrom <= 0.5625) {
                  out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.set, from, flat, 7.5));
               }

               BlockPos up = flat.above();
               ClassCache.Inner_FileCache thinTop = at.init_ClassCache_FileCac_BlockPos(up);
               int riseCells = !flatP.passable() && thinTop.top16() > 0 && thinTop.top16() <= 4 ? 2 : 1;
               BlockPos riseTarget = flat.above(riseCells);
               if (!blacklist.contains(riseTarget.asLong())) {
                  ClassCache.Inner_FileCache upP = at.init_ClassCache_FileCac_BlockPos(riseTarget);
                  ClassCache.Inner_FileCache upHead = at.init_ClassCache_FileCac_BlockPos(riseTarget.above());
                  ClassCache.Inner_FileCache upFloor = at.init_ClassCache_FileCac_BlockPos(riseTarget.below());
                  if ((!upFloor.passable() || f.water() && upP.water()) && init_Z_ClassCache_FileCac(upP, upHead, upFloor)) {
                     double rise = init(riseTarget.getY(), upFloor) - surfFrom;
                     if (!(rise <= 0.5625)
                        && (!upFloor.slabStair() || !init_Z_ClassCache_RenderS(at, riseTarget.below(), d[0], d[1], surfFrom, upFloor.top16() / 16.0))) {
                        if (rise <= 1.25
                           && (!fromFloor.farmland() || f.water())
                           && !fromFloor.honey()
                           && !init(caps, ClassCache.Inner_SyncManager.sync)
                           && !f.web()
                           && !fromHead.web()
                           && !upP.web()
                           && !upHead.web()
                           && !at.init_ClassCache_FileCac_BlockPos(from.above(2)).web()
                           && (!f.water() || load(at, up, d[0], d[1], 1, true))
                           && at.init_ClassCache_FileCac_BlockPos(from.above(2)).passable()) {
                           out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.sync, from, riseTarget, 9.0));
                        }
                     } else if (!init(caps, ClassCache.Inner_SyncManager.update)) {
                        out.add(
                           ClassCache.UiHelper.travel(
                              ClassCache.Inner_SyncManager.update,
                              from,
                              riseTarget,
                              5.0 + init_D_Function(at, from, riseTarget, false) + at.init_D_BlockPos(riseTarget)
                           )
                        );
                     }
                  }
               }

               if (caps.allowDig()
                  && !init(caps, ClassCache.Inner_SyncManager.stop)
                  && !blacklist.contains(up.asLong())
                  && !fromFloor.farmland()
                  && !fromFloor.honey()
                  && !f.water()
                  && !f.lava()
                  && !f.web()
                  && !fromHead.web()
                  && flatP.loaded()
                  && !flatP.passable()
                  && flatP.top16() == 16
                  && !flatP.hazardFloor()
                  && !flatP.lava()
                  && !flatP.door()
                  && init(up.getY(), flatP) - surfFrom <= 1.250001) {
                  BlockPos over = from.above(2);
                  ClassCache.Inner_FileCache overP = at.init_ClassCache_FileCac_BlockPos(over);
                  ClassCache.Inner_FileCache upP = at.init_ClassCache_FileCac_BlockPos(up);
                  ClassCache.Inner_FileCache upHead = at.init_ClassCache_FileCac_BlockPos(up.above());
                  if (init(overP)
                     && init(upP)
                     && init(upHead)
                     && !overP.web()
                     && !upP.web()
                     && !upHead.web()
                     && (overP.diggable() || upP.diggable() || upHead.diggable())
                     && (!overP.diggable() || !at.init_ClassCache_FileCac_BlockPos(over.above()).falling())) {
                     List<BlockPos> breaks = new ArrayList<>(3);
                     double sum = 0.0;
                     double raw = 0.0;

                     for (BlockPos cell : new BlockPos[]{over, up, up.above()}) {
                        ClassCache.Inner_FileCache c = cell == over ? overP : (cell == up ? upP : upHead);
                        if (c.diggable()) {
                           breaks.add(cell);
                           sum += c.breakTicks() + caps.breakReluctance() + c.destructionPenalty();
                           raw += c.breakTicks();
                        }
                     }

                     if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                        sum *= 2.0;
                     }

                     out.add(new ClassCache.UiHelper(ClassCache.Inner_SyncManager.stop, from, up, breaks, List.of(), 9.0 + sum, raw));
                  }
               }

               long softLanding = -9223372036854775808L;
               int columnTop = (int)Math.floor(surfFrom + 1.8 - 1.0E-6);
               int maxDrop = caps.fallPolicy().init() ? (int)caps.fallPolicy().init_D : 8;

               for (int n = 1; n <= maxDrop; n++) {
                  BlockPos t = flat.below(n);
                  ClassCache.Inner_FileCache tP = at.init_ClassCache_FileCac_BlockPos(t);
                  if (!tP.loaded()) {
                     break;
                  }

                  BlockPos above = t.above();
                  if (above.getY() <= columnTop && !at.init_ClassCache_FileCac_BlockPos(above).passable()
                     || n == 1 && flat.above().getY() <= columnTop && !at.init_ClassCache_FileCac_BlockPos(flat.above()).passable()
                     || caps.fallPolicy() != ClassCache.Inner_NetHelper.init_ClassCache_NetHelp && (tP.lava() || tP.hazardFeet())) {
                     break;
                  }

                  if (!blacklist.contains(t.asLong())) {
                     ClassCache.Inner_FileCache tFloor = at.init_ClassCache_FileCac_BlockPos(t.below());
                     double drop = surfFrom - init(t.getY(), tFloor);
                     double hardLimit = caps.fallPolicy().init()
                        ? maxDrop
                        : Math.min(3, caps.maxFall()) + (caps.fallPolicy() == ClassCache.Inner_NetHelper.init_ClassCache_NetHelp ? 0.5625 : 0.0);
                     if (drop <= hardLimit
                        && !tFloor.farmland()
                        && init_Z_ClassCache_FileCac(tP, at.init_ClassCache_FileCac_BlockPos(t.above()), tFloor)
                        && (drop <= 0.562501 || init_Z_Function(at, t, d[0], d[1], (int)Math.ceil(drop - 1.0E-6)))) {
                        ClassCache.Inner_SyncManager type = n == 1
                           ? ClassCache.Inner_SyncManager.flush
                           : (n == 2 ? ClassCache.Inner_SyncManager.close : ClassCache.Inner_SyncManager.open);
                        if (n == 1 && drop > 1.5625) {
                           type = ClassCache.Inner_SyncManager.close;
                        }

                        double safest = caps.strategy() == ClassCache.Inner_IoWorker.load ? n * 1.0 : 0.0;
                        if (!init(caps, type)) {
                           out.add(
                              ClassCache.UiHelper.travel(
                                 type,
                                 from,
                                 t,
                                 (
                                       n <= 3
                                          ? ClassCache.Inner_CachePool.read_D_I(n)
                                          : ClassCache.Inner_CachePool.read_D_I(3) + Math.ceil(Math.sqrt(drop) * 5.0 - 8.0)
                                    )
                                    + safest
                                    + init_D(at, t)
                                    + init_D_Function(at, from, flat, false)
                                    + 255.0 * init_D_Function(at, flat.getX(), flat.getZ(), surfFrom, init(t.getY(), tFloor))
                              )
                           );
                        }
                     }

                     int soft = Math.min(tP.fallMult10(), tFloor.fallMult10());
                     if (tP.passable() && soft <= 2 && !tP.lava() && !tFloor.lava() && !tP.hazardFeet() && (!caps.avoidWater() || !tP.water())) {
                        BlockPos landing = null;
                        if (!tP.water()) {
                           if (tFloor.passable()) {
                              if (!tFloor.water() && !tFloor.hazardFeet()) {
                                 landing = t.below();
                              }
                           } else {
                              landing = t;
                           }
                        } else if (!at.init_ClassCache_FileCac_BlockPos(t.above()).water()) {
                           BlockPos bottom = t;

                           while (at.init_ClassCache_FileCac_BlockPos(bottom.below()).water()) {
                              bottom = bottom.below();
                           }

                           if (from.getY() - bottom.getY() <= 8 && load(at.init_ClassCache_FileCac_BlockPos(bottom.below()))) {
                              landing = t;
                           }
                        }

                        if (landing != null && landing.asLong() != softLanding) {
                           softLanding = landing.asLong();
                           if (!at.init_ClassCache_FileCac_BlockPos(landing.below()).farmland()) {
                              int blocks = Math.max(n, from.getY() - landing.getY());
                              double safest = caps.strategy() == ClassCache.Inner_IoWorker.load ? blocks * 2.0 : 0.0;
                              double sink = 255.0 * init_D_Function(at, flat.getX(), flat.getZ(), surfFrom, landing.getY());
                              if (!init(caps, ClassCache.Inner_SyncManager.poll)) {
                                 out.add(
                                    ClassCache.UiHelper.travel(
                                       ClassCache.Inner_SyncManager.poll,
                                       from,
                                       landing,
                                       ClassCache.Inner_CachePool.write(blocks) + safest + init_D_Function(at, from, flat, false) + sink + init_D(at, landing)
                                    )
                                 );
                              }
                           }
                        }
                     }
                  }
               }

               if (softLanding == -9223372036854775808L
                  && !caps.avoidWater()
                  && !init(caps, ClassCache.Inner_SyncManager.poll)
                  && at.init_ClassCache_FileCac_BlockPos(flat).passable()
                  && at.init_ClassCache_FileCac_BlockPos(flat.above()).passable()
                  && at.init_ClassCache_FileCac_BlockPos(flat.below()).passable()
                  && at.init_ClassCache_FileCac_BlockPos(flat.below(2)).passable()) {
                  BlockPos water = at.init_BlockPos_BlockPos(from, surfFrom, d[0], d[1]);
                  if (water != null && !blacklist.contains(water.asLong()) && !at.init_ClassCache_FileCac_BlockPos(water.below()).farmland()) {
                     int blocks = Math.max(1, from.getY() - water.getY());
                     double safest = caps.strategy() == ClassCache.Inner_IoWorker.load ? blocks * 2.0 : 0.0;
                     out.add(ClassCache.UiHelper.travel(ClassCache.Inner_SyncManager.poll, from, water, ClassCache.Inner_CachePool.write(blocks) + safest));
                  }
               }
            }
         }

         for (int[] d : load_IArr2) {
            if (!centredWalking) {
               break;
            }

            BlockPos t = from.offset(d[0], 0, d[1]);
            if (!blacklist.contains(t.asLong())) {
               ClassCache.Inner_FileCache tP = at.init_ClassCache_FileCac_BlockPos(t);
               ClassCache.Inner_FileCache tFloor = at.init_ClassCache_FileCac_BlockPos(t.below());
               if (init_Z_ClassCache_FileCac(tP, at.init_ClassCache_FileCac_BlockPos(t.above()), tFloor)
                  && (!caps.avoidWater() || !tP.water())
                  && !(init(t.getY(), tFloor) - surfFrom > 0.5625)) {
                  BlockPos c1Pos = from.offset(d[0], 0, 0);
                  BlockPos c2Pos = from.offset(0, 0, d[1]);
                  if (init_Z(at, c1Pos)
                     && init_Z(at, c2Pos)
                     && !at.init_Z_BlockPos(from)
                     && !at.init_Z_BlockPos(t)
                     && !at.init_Z_BlockPos(c1Pos)
                     && !at.init_Z_BlockPos(c2Pos)) {
                     boolean sprint = caps.canSprint()
                        && caps.strategy() != ClassCache.Inner_IoWorker.read
                        && !tP.water()
                        && !tP.thorny()
                        && !init(caps, ClassCache.Inner_SyncManager.write);
                     double water = tP.water() ? 3.0 + (caps.strategy() == ClassCache.Inner_IoWorker.load ? 8.0 : 0.0) : 0.0;
                     double slow = tFloor.crawl();
                     ClassCache.Inner_SyncManager diagType = sprint ? ClassCache.Inner_SyncManager.write : ClassCache.Inner_SyncManager.load;
                     if (!init(caps, diagType)) {
                        out.add(
                           ClassCache.UiHelper.travel(
                              diagType,
                              from,
                              t,
                              (sprint ? 4.5 : 6.4 + water) * slow + (tP.thorny() ? 25.0 : 0.0) + init_D_Function(at, from, t, sprint) + at.init_D_BlockPos(t)
                           )
                        );
                     }
                  }
               }
            }
         }

         if (canJump) {
            init(at, caps, blacklist, from, fromFloor, surfFrom, out);
            if (at.read_Z) {
               init(at, caps, blacklist, from, surfFrom, false, out);
            }
         }

         if (caps.allowPillar()
            && caps.hasBlocks()
            && !init(caps, ClassCache.Inner_SyncManager.tick)
            && !f.lava()
            && !f.water()
            && !fromFloor.honey()
            && (
               !fromFloor.farmland() && fromFloor.top16() >= 16
                  || caps.repairFarmland() && fromFloor.farmland() && fromFloor.top16() == 15
                  || fromFloor.soulSand()
            )
            && !fromFloor.door()
            && !f.web()
            && !fromHead.web()
            && !blacklist.contains(from.above().asLong())
            && !at.init_ClassCache_FileCac_BlockPos(from.above()).hazardFeet()) {
            List<BlockPos> breaks = init_List_Function(at, caps, from);
            if (breaks != null) {
               double pillar = caps.strategy() == ClassCache.Inner_IoWorker.read ? 24.0 : 12.0;
               double sum = 0.0;
               double raw = 0.0;

               for (BlockPos cell : breaks) {
                  ClassCache.Inner_FileCache c = at.init_ClassCache_FileCac_BlockPos(cell);
                  sum += c.breakTicks() + caps.breakReluctance() + c.destructionPenalty();
                  raw += c.breakTicks();
               }

               if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                  sum *= 2.0;
               }

               out.add(
                  new ClassCache.UiHelper(
                     ClassCache.Inner_SyncManager.tick,
                     from,
                     from.above(),
                     breaks,
                     List.of(from),
                     pillar + sum,
                     breaks.isEmpty() ? -1.0 : raw,
                     null,
                     caps.repairFarmland() && fromFloor.farmland()
                  )
               );
            }
         }

         BlockPos below = from.below();
         ClassCache.Inner_FileCache belowP = at.init_ClassCache_FileCac_BlockPos(below);
         boolean openBelow = belowP.passable() && !belowP.water() && !belowP.lava() && !belowP.hazardFeet();
         if (!init(caps, ClassCache.Inner_SyncManager.push)
            && (openBelow || caps.allowDig() && belowP.diggable())
            && !blacklist.contains(below.asLong())
            && !f.lava()
            && (!f.water() || !at.init_ClassCache_FileCac_BlockPos(from.above()).water())
            && !at.init_ClassCache_FileCac_BlockPos(from.above(2)).falling()) {
            ClassCache.Inner_FileCache landing = at.init_ClassCache_FileCac_BlockPos(below.below());
            boolean floorOk = landing.loaded()
               && !landing.passable()
               && !landing.hazardFloor()
               && !landing.lava()
               && !landing.water()
               && !landing.farmland()
               && landing.top16() <= 16;
            if (floorOk) {
               double breakCost = openBelow ? 0.0 : belowP.breakTicks() + caps.breakReluctance() + belowP.destructionPenalty();
               double raw = openBelow ? 0.0 : belowP.breakTicks();
               List<BlockPos> breaks = new ArrayList<>(3);
               breaks.add(below);
               boolean clearable = true;

               for (BlockPos bodyCell : new BlockPos[]{from.above(), from}) {
                  ClassCache.Inner_FileCache body = at.init_ClassCache_FileCac_BlockPos(bodyCell);
                  if (body.passable() && !body.water() && !body.lava()) {
                     short outline = at.load_S(bodyCell.getX(), bodyCell.getY(), bodyCell.getZ());
                     if (outline == -1) {
                        at.init_I |= 1;
                        clearable = false;
                        break;
                     }

                     if (outline != 0) {
                        if (!caps.allowDig() || body.breakTicks() >= 1.0 / 0.0) {
                           clearable = false;
                           break;
                        }

                        breaks.add(bodyCell);
                        breakCost += body.breakTicks() + caps.breakReluctance() + body.destructionPenalty();
                        raw += body.breakTicks();
                     }
                  }
               }

               if (clearable) {
                  if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                     breakCost *= 2.0;
                  }

                  out.add(
                     new ClassCache.UiHelper(
                        ClassCache.Inner_SyncManager.push,
                        from,
                        below,
                        breaks,
                        List.of(),
                        openBelow && breaks.size() == 1 ? ClassCache.Inner_CachePool.read_D_I(1) : ClassCache.Inner_CachePool.init_D_D(breakCost),
                        raw
                     )
                  );
               }
            }
         }

         return at.init_I;
      }

      static List<BlockPos> init_List_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, ClassCache.Inner_DataUtil caps, BlockPos from) {
         BlockPos head = from.above(2);
         BlockPos over = from.above(3);
         ClassCache.Inner_FileCache headP = at.apply(head);
         ClassCache.Inner_FileCache overP = at.apply(over);
         if (headP.loaded() && !headP.lava() && !headP.water() && !headP.hazardFeet() && !headP.web()) {
            List<BlockPos> breaks = new ArrayList<>(2);
            if (!headP.passable()) {
               if (!caps.allowDig() || !headP.diggable() || overP.falling() || overP.lava() || overP.water()) {
                  return null;
               }

               breaks.add(head);
            }

            if (overP.loaded() && !overP.lava() && !overP.hazardFeet()) {
               if (!overP.passable() && caps.allowDig() && overP.diggable()) {
                  ClassCache.Inner_FileCache above = at.apply(over.above());
                  if (!above.falling() && !above.lava() && !above.water()) {
                     breaks.add(over);
                  }
               }

               return breaks;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }

      static boolean init(ClassCache.Inner_FileCache p) {
         if (p.loaded() && !p.lava() && !p.hazardFeet()) {
            return p.passable() ? !p.water() : p.diggable();
         } else {
            return false;
         }
      }

      private static ClassCache.Inner_RenderState.Inner_StreamUtil[] init(int radius) {
         ArrayList<ClassCache.Inner_RenderState.Inner_StreamUtil> directions = new ArrayList<>();

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               int distance = Math.max(Math.abs(x), Math.abs(z));
               if (distance != 0) {
                  double length = Math.hypot(x, z);
                  int stepX = (int)Math.round((double)x / distance);
                  int stepZ = (int)Math.round((double)z / distance);
                  int exits = 1 << init(stepX, stepZ);

                  for (int i = 0; i < read_IArr2.length; i++) {
                     int[] ring = read_IArr2[i];
                     if ((ring[0] * x + ring[1] * z) / (Math.hypot(ring[0], ring[1]) * length) >= 0.5) {
                        exits |= 1 << i;
                     }
                  }

                  double edge = 0.5 * length / distance;
                  double cornerEdge = 0.5 * (Math.abs(x) + Math.abs(z)) / length;
                  directions.add(
                     new ClassCache.Inner_RenderState.Inner_StreamUtil(
                        x, z, distance, stepX, stepZ, length, length - 2.0 * (edge - 0.5), length - 2.0 * (cornerEdge - 0.5), exits
                     )
                  );
               }
            }
         }

         return directions.toArray(ClassCache.Inner_RenderState.Inner_StreamUtil[]::new);
      }

      static boolean load(ClassCache.Inner_FileCache feet, ClassCache.Inner_FileCache floor) {
         return feet.loaded() && !feet.water() && !feet.lava() && !feet.climbable() && !feet.hazardFeet() && !feet.web()
            ? floor.loaded() && !floor.passable() && !floor.floorContactHazard() && !floor.lava() && !floor.water()
            : false;
      }

      private static void init(
         ClassCache.Inner_RenderState.Inner_CachePool at,
         ClassCache.Inner_DataUtil caps,
         Set<Long> blacklist,
         BlockPos from,
         ClassCache.Inner_FileCache footing,
         double surfFrom,
         List<ClassCache.UiHelper> out
      ) {
         boolean limited = at.init_Z();
         if (!limited || !at.write(from)) {
            boolean sprintOk = caps.canSprint() && caps.strategy() != ClassCache.Inner_IoWorker.read;
            boolean shapedTakeoff = at.read_Z && (footing.narrowFooting() || footing.top16() > 16);
            double speed = init_D_ClassCache_RenderS(at, from, surfFrom, sprintOk);
            double inX = 0.0;
            double inZ = 0.0;
            if (speed > 0.0) {
               inX = at.init_ClassCache_UiHelpe.to().getX() - at.init_ClassCache_UiHelpe.from().getX();
               inZ = at.init_ClassCache_UiHelpe.to().getZ() - at.init_ClassCache_UiHelpe.from().getZ();
               double len = Math.hypot(inX, inZ);
               if (len < 1.0E-9) {
                  speed = 0.0;
               } else {
                  inX /= len;
                  inZ /= len;
               }
            }

            int exits = 0;

            for (int i = 0; i < read_IArr2.length; i++) {
               BlockPos next = from.offset(read_IArr2[i][0], 0, read_IArr2[i][1]);
               if (!init_Z_Function(at, next, surfFrom) && init(at, next) && init(at, next.above())) {
                  exits |= 1 << i;
               }
            }

            if (exits != 0 && (limited || !at.write(from))) {
               boolean longDrops = at.init_ClassCache_NetHelp.init();
               ClassCache.Inner_RenderState.Inner_StreamUtil[] directions = longDrops ? load_ClassCache_RenderSArr : init_ClassCache_RenderSArr;
               int minDy = longDrops ? -((int)at.init_ClassCache_NetHelp.init_D) : -4;
               if (longDrops && at.update_Z) {
                  directions = read_ClassCache_RenderSArr;
                  minDy = -14;
               }

               for (ClassCache.Inner_RenderState.Inner_StreamUtil direction : directions) {
                  int dx = direction.x();
                  int dz = direction.z();
                  int cheb = direction.distance();
                  if ((exits & direction.exits()) != 0 || cheb < 2) {
                     double horizontal = direction.length();
                     double reach = at.read_Z ? direction.cornerReach() : direction.reach();
                     int maxDy = cheb <= 5 ? 1 : -Math.max(3, (int)Math.floor((reach - 5.2) / 0.35));

                     for (int dy = maxDy; dy >= minDy; dy--) {
                        if (longDrops && dy < -3) {
                           int bottom = from.getY() - 1;
                           dy = at.init_I_I(from.getX() + dx, bottom + dy, from.getZ() + dz, bottom + minDy) - bottom;
                           if (dy < minDy) {
                              break;
                           }
                        }

                        BlockPos to = from.offset(dx, dy, dz);
                        ClassCache.Inner_FileCache tFloor = at.init_ClassCache_FileCac_BlockPos(to.below());
                        if (tFloor.loaded() && !tFloor.passable()) {
                           boolean shaped = (tFloor.top16() > 16 || at.read_Z) && init_Z_Function_2(at, to, null);
                           if (cheb == 1 && !shaped && !shapedTakeoff && (dx == 0 || dz == 0 || !init_Z_Function_2(at, from, to, surfFrom))) {
                              break;
                           }

                           ClassCache.Inner_FileCache tP = at.init_ClassCache_FileCac_BlockPos(to);
                           if (!blacklist.contains(to.asLong())
                              && !tFloor.farmland()
                              && !tFloor.floorContactHazard()
                              && !tFloor.lava()
                              && !tP.water()
                              && !tP.climbable()
                              && !tP.web()
                              && !tP.portal()
                              && (shaped || init_Z_ClassCache_FileCac(tP, at.init_ClassCache_FileCac_BlockPos(to.above()), tFloor))
                              && !at.init_ClassCache_FileCac_BlockPos(to.above()).web()) {
                              double surfTo = init(to.getY(), tFloor);
                              double rise = surfTo - surfFrom;
                              if (rise > 1.25
                                 || rise < (at.init_ClassCache_NetHelp.init() ? -at.init_ClassCache_NetHelp.init_D : -3.6)
                                 || reach > (rise > 0.6 ? 4.1 : (rise > 0.0 ? 4.6 : 5.2)) + 0.35 * Math.max(0.0, -rise) + 1.0E-6) {
                                 break;
                              }

                              BlockPos before = to.offset(-direction.stepX(), 0, -direction.stepZ());
                              if (Math.max(Math.abs(before.getX() - from.getX()), Math.abs(before.getZ() - from.getZ())) >= 2
                                 && init_Z_Function(at, before, surfTo)) {
                                 break;
                              }

                              double along = speed <= 0.0 ? 0.0 : (inX * dx + inZ * dz) / horizontal;
                              double approach = along >= 0.65 ? speed * along : 0.0;
                              if (approach < 0.003) {
                                 approach = 0.0;
                              }

                              boolean landedAhead = approach > 0.0 && at.init_ClassCache_UiHelpe != null && at.init_ClassCache_UiHelpe.jump() != null;
                              double centre = reach + (approach > 0.0 ? 0.2 : 0.0);
                              double nearest = centre - 0.5 + 0.3 + -0.12;
                              double farthest = centre + 0.5 - 0.3 - -0.12;
                              double flatRun = TableIndex.init(rise, sprintOk, approach > 0.0);
                              double flatRest = approach > 0.0 ? TableIndex.init(rise, sprintOk, false) : flatRun;
                              double maxAllowed = Math.max(
                                 flatRun < 0.0 ? -1.0 : flatRun + (approach > 0.0 ? 0.25 : 1.0), flatRest < 0.0 ? -1.0 : flatRest + 1.0
                              );
                              if (maxAllowed < 0.0 || nearest > maxAllowed || cheb >= 2 && farthest < TableIndex.init(rise) - 0.25) {
                                 break;
                              }

                              ClassCache.Inner_RenderState.Inner_DataUtil candidate = new ClassCache.Inner_RenderState.Inner_DataUtil(
                                 from, to, surfFrom, surfTo, approach, sprintOk, direction.stepX(), direction.stepZ(), landedAhead
                              );
                              if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
                                 ListPool.init("jump candidate from=" + from + " to=" + to + " approach=" + approach);
                              }

                              if (at.init_Consumer != null) {
                                 at.init_Consumer.accept(candidate);
                              } else {
                                 ClassCache.UiHelper proved = init_ClassCache_UiHelpe_ClassCache_RenderS(at, blacklist, candidate);
                                 if (proved != null) {
                                    out.add(proved);
                                 }
                              }
                              break;
                           }

                           if (!at.read_Z || at.init_S_I(to.getX(), to.getY() - 1, to.getZ()) == 1) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      private static boolean init(ClassCache.Inner_RenderState.Inner_CachePool at, BlockPos cell) {
         ClassCache.Inner_FileCache space = at.init_ClassCache_FileCac_BlockPos(cell);
         return space.passable()
            ? true
            : at.read_Z && space.loaded() && !space.hazardFeet() && !space.lava() && !space.water() && at.init_S_I(cell.getX(), cell.getY(), cell.getZ()) != 1;
      }

      private static ClassCache.UiHelper init_ClassCache_UiHelpe_ClassCache_RenderS(
         ClassCache.Inner_RenderState.Inner_CachePool at, Set<Long> blacklist, ClassCache.Inner_RenderState.Inner_DataUtil candidate
      ) {
         BlockPos from = candidate.from();
         BlockPos landing = candidate.to();
         double surface = candidate.targetY();
         TableIndex.Inner_StateCache plan = at.init(from, candidate.sourceY(), landing, surface, candidate.approach(), candidate.sprint());
         boolean hadArc = plan != null;
         double settleCost = 0.0;
         if (candidate.landedAhead()
            && plan != null
            && plan.maneuver() == null
            && at.init_ClassCache_UiHelpe != null
            && at.init_ClassCache_UiHelpe.jump() != null) {
            TableIndex.Inner_StateCache prev = at.init_ClassCache_UiHelpe.jump();
            double r = Math.toRadians(prev.yaw());
            NodeWalker.Inner_StreamUtil landedState = new NodeWalker.Inner_StreamUtil(
               prev.landX(), candidate.sourceY(), prev.landZ(), -Math.sin(r) * prev.landSpeed(), -0.0784, Math.cos(r) * prev.landSpeed(), true, 0, false, false
            );
            if (TableIndex.init_TableIndex_UiHelpe_NodeWalker_CachePo(at.init_StreamGate(), landedState, plan.yaw(), candidate.sprint(), landing, surface, 6)
                  .margin()
               < -0.04) {
               plan = null;
            }
         }

         if (hadArc && candidate.approach() > 0.0 && (plan == null || plan.maneuver() == null && plan.margin() < 0.14)) {
            TableIndex.Inner_StateCache rested = at.init(from, candidate.sourceY(), landing, surface, 0.0, candidate.sprint());
            if (rested != null && rested.margin() >= -0.04) {
               boolean replaces = plan == null
                  || plan.margin() < -0.04
                  || rested.margin() > plan.margin() + 0.04
                  || rested.margin() >= plan.margin() && rested.ticks() + 4 < plan.ticks();
               if (replaces) {
                  plan = rested;
                  settleCost = 4.0;
               }
            }
         }

         if (plan != null && candidate.approach() > 0.0 && !plan.running()) {
            settleCost = Math.max(settleCost, 4.0);
         }

         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            ListPool.init("jump proof from=" + from + " to=" + landing + " plan=" + plan);
         }

         if (plan == null) {
            return null;
         }

         if (plan.margin() < 0.1) {
            BlockPos deeper = candidate.deeper();
            ClassCache.Inner_FileCache floor = at.init_ClassCache_FileCac_BlockPos(deeper.below());
            if (!blacklist.contains(deeper.asLong()) && init_Z_Function(at, deeper, surface) && Math.abs(init(deeper.getY(), floor) - surface) < 1.0E-6) {
               TableIndex.Inner_StateCache inner = at.init(from, candidate.sourceY(), deeper, surface, plan.approach(), candidate.sprint());
               if (inner != null && inner.margin() > plan.margin()) {
                  plan = inner;
                  landing = deeper;
               }
            }
         }

         boolean hard = init_Z_Function_3(at, from, landing);
         boolean lava = load(at, from, landing);
         if (Boolean.getBoolean("autism.ai.parkour.traceCandidates")) {
            ListPool.init(
               "jump decision from="
                  + from
                  + " to="
                  + landing
                  + " margin="
                  + plan.margin()
                  + " hard="
                  + hard
                  + " cautious="
                  + ClassCache.Inner_CachePool.init()
                  + " lava="
                  + lava
            );
         }

         if (!(plan.margin() < -0.04) && (!hard || !ClassCache.Inner_CachePool.init()) && !lava) {
            double risk = hard ? 20.0 : 0.0;
            if (hard && (at.write_Z || at.init_ClassCache_NetHelp != ClassCache.Inner_NetHelper.init_ClassCache_NetHelp)) {
               double comfortableMargin = 0.12;
               risk *= Math.clamp((comfortableMargin - plan.margin()) / (comfortableMargin - -0.04), 0.0, 1.0);
            }

            if (at.init_ClassCache_NetHelp != ClassCache.Inner_NetHelper.init_ClassCache_NetHelp) {
               ClassCache.Inner_FileCache floor = at.init_ClassCache_FileCac_BlockPos(landing.below());
               if (floor.narrowFooting() || floor.top16() > 16) {
                  settleCost += 3.0;
               }

               if (init(plan)) {
                  settleCost += 3.0;
               }
            }

            return ClassCache.UiHelper.jump(
               from,
               landing,
               ClassCache.Inner_CachePool.init_D_TableIndex_StateCa(plan)
                  + settleCost
                  + (at.init_ClassCache_FileCac_BlockPos(landing).thorny() ? 25.0 : 0.0)
                  + risk,
               plan
            );
         } else {
            return null;
         }
      }

      private static boolean init(TableIndex.Inner_StateCache plan) {
         return plan.maneuver() != null && plan.maneuver().inputs().stream().anyMatch(NodeWalker.Inner_DataUtil::sneak);
      }

      static boolean init_Z_Function_3(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to) {
         int dx = to.getX() - from.getX();
         int dz = to.getZ() - from.getZ();
         int cheb = Math.max(Math.abs(dx), Math.abs(dz));
         int top = Math.max(from.getY(), to.getY());

         for (int k = 1; k < cheb; k++) {
            BlockPos mid = new BlockPos(from.getX() + (int)Math.round((double)dx * k / cheb), top, from.getZ() + (int)Math.round((double)dz * k / cheb));
            if (update(at, mid)) {
               return true;
            }
         }

         for (int[] ring : read_IArr2) {
            if (update(at, to.offset(ring[0], 0, ring[1]))) {
               return true;
            }
         }

         return false;
      }

      static boolean load(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to) {
         int dx = to.getX() - from.getX();
         int dz = to.getZ() - from.getZ();
         int cheb = Math.max(Math.abs(dx), Math.abs(dz));
         int top = Math.max(from.getY(), to.getY());

         for (int k = 1; k < cheb; k++) {
            BlockPos mid = new BlockPos(from.getX() + (int)Math.round((double)dx * k / cheb), top, from.getZ() + (int)Math.round((double)dz * k / cheb));
            if (write(at, mid)) {
               return true;
            }
         }

         for (int[] ring : read_IArr2) {
            if (write(at, to.offset(ring[0], 0, ring[1]))) {
               return true;
            }
         }

         return false;
      }

      private static boolean write(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos top) {
         for (int down = 0; down <= 12; down++) {
            ClassCache.Inner_FileCache p = at.apply(top.below(down));
            if (!p.loaded()) {
               return false;
            }

            if (p.lava()) {
               return true;
            }

            if (!p.passable() || p.water()) {
               return false;
            }
         }

         return false;
      }

      private static boolean update(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos top) {
         for (int down = 0; down <= 4; down++) {
            ClassCache.Inner_FileCache p = at.apply(top.below(down));
            if (!p.loaded() || !p.passable() || p.water()) {
               return false;
            }
         }

         return true;
      }

      private static int init(int x, int z) {
         for (int i = 0; i < read_IArr2.length; i++) {
            if (read_IArr2[i][0] == x && read_IArr2[i][1] == z) {
               return i;
            }
         }

         throw new IllegalArgumentException("not a neighbour: " + x + "," + z);
      }

      static boolean init_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos cell, double surfFrom) {
         ClassCache.Inner_FileCache feet = at.apply(cell);
         ClassCache.Inner_FileCache floor = at.apply(cell.below());
         return init_Z_ClassCache_FileCac(feet, at.apply(cell.above()), floor) && !feet.water()
            ? Math.abs(init(cell.getY(), floor) - surfFrom) <= 0.562500001
            : false;
      }

      static boolean init_Z_Function_2(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to, double surfFrom) {
         int dx = to.getX() - from.getX();
         int dz = to.getZ() - from.getZ();
         int cheb = Math.max(Math.abs(dx), Math.abs(dz));
         if (cheb != 1) {
            for (int k = 1; k < cheb; k++) {
               int cx = from.getX() + (int)Math.round((double)dx * k / cheb);
               int cz = from.getZ() + (int)Math.round((double)dz * k / cheb);
               if (!init_Z_Function(at, new BlockPos(cx, from.getY(), cz), surfFrom)) {
                  return true;
               }
            }

            return false;
         } else {
            return dx != 0 && dz != 0 ? !init_Z_Function(at, from.offset(dx, 0, 0), surfFrom) || !init_Z_Function(at, from.offset(0, 0, dz), surfFrom) : false;
         }
      }

      private static double init_D_ClassCache_RenderS(ClassCache.Inner_RenderState.Inner_CachePool at, BlockPos from, double surface, boolean sprintAvailable) {
         if (at.init_ClassCache_UiHelpe == null) {
            return 0.0;
         }

         double var10000;
         switch (at.init_ClassCache_UiHelpe.type()) {
            case init:
            case load:
            case read:
            case write:
               StreamGate world = at.init_StreamGate();
               int y = (int)Math.floor(surface - 0.5000001);
               double friction = world.init_D_I(from.getX(), y, from.getZ());
               double drag = 0.91 * friction * world.load_D(from.getX(), y, from.getZ());
               boolean sprint = at.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.read
                  || at.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.write;
               double acceleration = world.load() * (sprint ? 1.3 : 1.0) * (0.21600002 / (friction * friction * friction)) * 0.98;
               var10000 = Math.min(0.28, acceleration * drag / (1.0 - drag));
               break;
            default:
               var10000 = init(at.init_ClassCache_UiHelpe, sprintAvailable);
         }

         return var10000;
      }

      static double init(ClassCache.UiHelper incoming) {
         return init(incoming, false);
      }

      static double init(ClassCache.UiHelper incoming, boolean sprintAvailable) {
         if (incoming == null) {
            return 0.0;
         }

         double var10000;
         switch (incoming.type()) {
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
               var10000 = sprintAvailable ? 0.28 : 0.2;
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
               var10000 = incoming.jump() == null ? 0.2 : Math.min(0.28, incoming.jump().landSpeed());
         }

         return var10000;
      }

      private static void init_V_ClassCache_RenderS(
         ClassCache.Inner_RenderState.Inner_CachePool at,
         ClassCache.Inner_DataUtil caps,
         Set<Long> blacklist,
         BlockPos from,
         ClassCache.Inner_FileCache f,
         List<ClassCache.UiHelper> out
      ) {
         for (int[] d : init_IArr2) {
            BlockPos flat = from.offset(d[0], 0, d[1]);
            if (!blacklist.contains(flat.asLong())) {
               ClassCache.Inner_FileCache flatP = at.init_ClassCache_FileCac_BlockPos(flat);
               ClassCache.Inner_FileCache flatHead = at.init_ClassCache_FileCac_BlockPos(flat.above());
               ClassCache.Inner_FileCache flatFloor = at.init_ClassCache_FileCac_BlockPos(flat.below());
               if (flatFloor.loaded()
                  && !flatFloor.passable()
                  && !flatFloor.hazardFloor()
                  && !flatFloor.lava()
                  && !flatP.hazardFeet()
                  && !flatP.lava()
                  && !flatHead.hazardFeet()
                  && !flatHead.lava()) {
                  boolean feetDig = flatP.diggable();
                  boolean headDig = flatHead.diggable();
                  if ((feetDig || flatP.passable()) && (headDig || flatHead.passable())) {
                     List<BlockPos> breaks = new ArrayList<>(3);
                     double sum = 0.0;
                     double raw = 0.0;
                     if (!f.passable() && f.diggable()) {
                        breaks.add(from);
                        sum += f.breakTicks() + caps.breakReluctance() + f.destructionPenalty();
                        raw += f.breakTicks();
                     }

                     if (feetDig) {
                        breaks.add(flat);
                        sum += flatP.breakTicks() + caps.breakReluctance() + flatP.destructionPenalty();
                        raw += flatP.breakTicks();
                     }

                     if (headDig) {
                        breaks.add(flat.above());
                        sum += flatHead.breakTicks() + caps.breakReluctance() + flatHead.destructionPenalty();
                        raw += flatHead.breakTicks();
                     }

                     if (caps.strategy() == ClassCache.Inner_IoWorker.read) {
                        sum *= 2.0;
                     }

                     out.add(
                        new ClassCache.UiHelper(ClassCache.Inner_SyncManager.pop, from, flat, breaks, List.of(), ClassCache.Inner_CachePool.load_D_D(sum), raw)
                     );
                  }
               }
            }
         }
      }

      static double init_D_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to, boolean sprint) {
         ClassCache.Inner_FileCache toFeet = at.apply(to);
         ClassCache.Inner_FileCache toHead = at.apply(to.above());
         double portal = !toFeet.portal() && !toHead.portal() ? 0.0 : 400.0;
         boolean far = toFeet.web() || toHead.web();
         if (!far) {
            return portal;
         }

         boolean near = at.apply(from).web() || at.apply(from.above()).web();
         double share = near ? 1.0 : 1.6;
         double length = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());
         return portal + share * length * (sprint ? 32.0 : 41.0);
      }

      static double init_D(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos cell) {
         return !at.apply(cell).portal() && !at.apply(cell.above()).portal() ? 0.0 : 400.0;
      }

      static double init_D_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, int x, int z, double top, double bottom) {
         double held = 0.0;
         double reached = bottom;

         for (int y = (int)Math.floor(bottom); y <= (int)Math.floor(top) + 1; y++) {
            if (at.apply(new BlockPos(x, y, z)).web()) {
               double lo = Math.max(Math.max(y - 1.8, bottom), reached);
               double hi = Math.min(y + 1.0, top);
               if (hi > lo) {
                  held += hi - lo;
                  reached = hi;
               }
            }
         }

         return held;
      }

      static boolean init_Z(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos corner) {
         ClassCache.Inner_FileCache feet = at.apply(corner);
         ClassCache.Inner_FileCache head = at.apply(corner.above());
         ClassCache.Inner_FileCache floor = at.apply(corner.below());
         if (!feet.passable() || !head.passable() || feet.hazardFeet() || feet.lava()) {
            return false;
         }

         if (feet.contactHazard() || head.contactHazard()) {
            return false;
         }

         if (feet.web() || head.web() || feet.portal() || head.portal()) {
            return false;
         }

         if (feet.water()) {
            return true;
         }

         if (!floor.loaded() || floor.lava() || floor.hazardFloor()) {
            return false;
         }

         if (!floor.passable()) {
            return true;
         }

         if (floor.water()) {
            return true;
         }

         ClassCache.Inner_FileCache floor2 = at.apply(corner.below(2));
         return floor2.loaded() && !floor2.passable() && !floor2.lava() && !floor2.hazardFloor();
      }

      static boolean init_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos landing, int dx, int dz) {
         return init_Z_Function(at, landing, dx, dz, 1);
      }

      public static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, double x, double z, BlockPos feet) {
         double r = 0.4;
         AABB room = new AABB(x - r, feet.getY(), z - r, x + r, feet.getY() + 2, z + r);

         for (AABB b : world.init_List(room)) {
            if (b.intersects(room)) {
               return false;
            }
         }

         return true;
      }

      public static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, List<NodeWalker.Inner_StreamUtil> flight, int surfaceY) {
         double r = 0.4;

         for (NodeWalker.Inner_StreamUtil st : flight) {
            if (!(st.y() > surfaceY + 3) && !(st.y() < surfaceY)) {
               AABB room = new AABB(st.x() - r, st.y(), st.z() - r, st.x() + r, st.y() + 1.8, st.z() + r);

               for (AABB b : world.init_List(room)) {
                  if (b.intersects(room)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }

      public static boolean read(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to) {
         ClassCache.Inner_FileCache fromFloor = at.apply(from.below());
         ClassCache.Inner_FileCache toFloor = at.apply(to.below());
         return !fromFloor.passable() && !toFloor.passable() ? init(from.getY(), fromFloor) - init(to.getY(), toFloor) <= 0.562501 : false;
      }

      static boolean init_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos landing, int dx, int dz, int drop) {
         return load(at, landing, dx, dz, drop, false);
      }

      private static boolean load(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos landing, int dx, int dz, int drop, boolean swimmingExit) {
         BlockPos beyond = landing.offset(dx, 0, dz);
         ClassCache.Inner_FileCache feet = at.apply(beyond);
         if (feet.loaded() && !feet.lava() && !feet.hazardFeet()) {
            if (!feet.web() && !at.apply(beyond.above()).web()) {
               if (!feet.passable()) {
                  return drop < 2 || !at.apply(beyond.above()).passable();
               }

               if (feet.water()) {
                  return true;
               }

               int k = 0;

               while (k <= 2) {
                  BlockPos lower = beyond.below(k);
                  ClassCache.Inner_FileCache lowerFeet = at.apply(lower);
                  if (lowerFeet.loaded() && !lowerFeet.lava() && !lowerFeet.hazardFeet() && !lowerFeet.web()) {
                     if (k > 0 && lowerFeet.water()) {
                        return true;
                     }

                     ClassCache.Inner_FileCache floor = at.apply(lower.below());
                     if (floor.loaded() && !floor.lava() && !floor.hazardFloor() && (!floor.farmland() || swimmingExit)) {
                        if (floor.passable() && !floor.water()) {
                           k++;
                           continue;
                        }

                        return init_Z_ClassCache_FileCac(lowerFeet, at.apply(lower.above()), floor);
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

      private static boolean init(ClassCache.Inner_DataUtil caps, ClassCache.Inner_SyncManager type) {
         return caps.disabledTypes().contains(type);
      }

      private static boolean load(ClassCache.Inner_FileCache floorBelowWater) {
         return floorBelowWater.loaded()
            && !floorBelowWater.passable()
            && !floorBelowWater.water()
            && !floorBelowWater.lava()
            && !floorBelowWater.hazardFloor();
      }

      static boolean write(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to) {
         ClassCache.Inner_FileCache feet = at.apply(from);
         ClassCache.Inner_FileCache floor = at.apply(from.below());
         if (feet.water() || feet.lava() || feet.climbable() || feet.hazardFeet() || feet.web() || at.apply(from.above()).web()) {
            return false;
         }

         if (floor.passable() || floor.farmland() || floor.hazardFloor() || floor.lava() || floor.soulSand()) {
            return false;
         }

         if (!floor.honey() && !floor.slime()) {
            ClassCache.Inner_FileCache toFeet = at.apply(to);
            ClassCache.Inner_FileCache toFloor = at.apply(to.below());
            if (!toFloor.honey() && !toFloor.slime()) {
               if (!toFeet.water() && !toFeet.thorny() && !toFeet.web() && !at.apply(to.above()).web() && !toFloor.farmland()) {
                  int dx = Integer.signum(to.getX() - from.getX());
                  int dz = Integer.signum(to.getZ() - from.getZ());
                  int k = 0;

                  while (k <= 4) {
                     BlockPos c = from.offset(dx * k, 0, dz * k);
                     if (!at.apply(c).web() && !at.apply(c.above()).web() && !at.apply(c.above(2)).web()) {
                        if (at.apply(c.below()).farmland()) {
                           return false;
                        }

                        for (int side = -1; side <= 1; side += 2) {
                           if (at.apply(c.offset(dz * side, -1, dx * side)).farmland()) {
                              return false;
                           }
                        }

                        if (!at.apply(c).portal() && !at.apply(c.above()).portal()) {
                           k++;
                           continue;
                        }

                        return false;
                     }

                     return false;
                  }

                  if (init(from.getY(), floor) != init(to.getY(), toFloor)) {
                     return false;
                  } else {
                     return !init_Z_ClassCache_FileCac(toFeet, at.apply(to.above()), toFloor)
                        ? false
                        : init(at.apply(from.above(2)), at.apply(from.above(3))) || update(at, from, to);
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

      static boolean update(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos to) {
         ClassCache.Inner_FileCache roofFrom = at.apply(from.above(2));
         ClassCache.Inner_FileCache roofTo = at.apply(to.above(2));
         return roofFrom.loaded() && roofTo.loaded() && !roofFrom.passable() && !roofTo.passable() && !roofFrom.hazardFeet() && !roofTo.hazardFeet();
      }

      static boolean init_Z_Function(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, int dx, int dz, int gap, boolean ascend) {
         ClassCache.Inner_FileCache feet = at.apply(from);
         ClassCache.Inner_FileCache floor = at.apply(from.below());
         if (!feet.loaded() || feet.water() || feet.lava() || feet.climbable() || feet.hazardFeet() || feet.web()) {
            return false;
         }

         if (floor.ice()) {
            return false;
         }

         if (!floor.passable() && floor.top16() == 16 && !floor.slabStair() && !floor.hazardFloor()) {
            for (int i = 0; i <= gap + 1; i++) {
               int yLo = from.getY() + 1;
               int yHi = from.getY() + 2;
               if (ascend && i == gap + 1) {
                  yLo++;
                  yHi++;
               } else if (ascend && i > 0) {
                  yHi++;
               }

               for (int y = yLo; y <= yHi; y++) {
                  ClassCache.Inner_FileCache c = at.apply(new BlockPos(from.getX() + dx * i, y, from.getZ() + dz * i));
                  if (!c.passable() || c.lava() || c.water() || c.hazardFeet() || c.climbable() || c.web()) {
                     return false;
                  }
               }
            }

            boolean realGap = false;

            for (int i = 1; i <= gap; i++) {
               BlockPos cell = new BlockPos(from.getX() + dx * i, from.getY(), from.getZ() + dz * i);
               ClassCache.Inner_FileCache g = at.apply(cell);
               if (!g.passable() || g.lava() || g.hazardFeet() || g.climbable() || g.web()) {
                  return false;
               }

               if (at.apply(cell.below()).passable()) {
                  realGap = true;
               }
            }

            if (!realGap) {
               return false;
            } else {
               BlockPos landing = new BlockPos(from.getX() + dx * (gap + 1), from.getY() + (ascend ? 1 : 0), from.getZ() + dz * (gap + 1));
               ClassCache.Inner_FileCache landFloor = at.apply(landing.below());
               if (init_Z_ClassCache_FileCac(at.apply(landing), at.apply(landing.above()), landFloor)
                  && !landFloor.farmland()
                  && !landFloor.ice()
                  && !landFloor.slime()) {
                  return at.apply(landing).web() ? false : load(at, landing.offset(dx, 0, dz)) && load(at, landing.offset(2 * dx, 0, 2 * dz));
               } else {
                  return false;
               }
            }
         } else {
            return false;
         }
      }

      static int init_I(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos cell) {
         ClassCache.Inner_FileCache feet = at.apply(cell);
         if (feet.loaded() && !feet.lava() && !feet.hazardFeet()) {
            if (!feet.passable()) {
               return 0;
            }

            int depth = 1;

            while (depth <= 32) {
               ClassCache.Inner_FileCache d = at.apply(cell.below(depth));
               if (!d.loaded()) {
                  return depth;
               }

               if (!d.lava() && !d.hazardFloor() && !d.hazardFeet()) {
                  if (!d.water() && d.passable()) {
                     depth++;
                     continue;
                  }

                  return depth - 1;
               }

               return -1;
            }

            return 32;
         } else {
            return -1;
         }
      }

      static boolean load(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos cell) {
         ClassCache.Inner_FileCache feet = at.apply(cell);
         if (!feet.loaded() || feet.lava() || feet.hazardFeet()) {
            return false;
         }

         if (!feet.passable()) {
            return true;
         }

         ClassCache.Inner_FileCache floor = at.apply(cell.below());
         if (floor.loaded() && !floor.lava() && !floor.hazardFloor()) {
            if (init_Z_ClassCache_FileCac(feet, at.apply(cell.above()), floor)) {
               return true;
            }

            for (int depth = 1; depth <= 4; depth++) {
               ClassCache.Inner_FileCache d = at.apply(cell.below(depth));
               if (!d.loaded() || d.lava() || d.hazardFloor()) {
                  return false;
               }

               if (d.water()) {
                  return true;
               }

               if (!d.passable()) {
                  return true;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      static boolean read(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos support) {
         ClassCache.Inner_FileCache below = at.apply(support.below());
         if (below.loaded() && !below.passable()) {
            return true;
         }

         for (int[] d : init_IArr2) {
            ClassCache.Inner_FileCache n = at.apply(support.offset(d[0], 0, d[1]));
            if (n.loaded() && !n.passable()) {
               return true;
            }
         }

         return false;
      }

      static boolean init_Z_Function_3(Function<BlockPos, ClassCache.Inner_FileCache> at, BlockPos from, BlockPos flat, ClassCache.UiHelper incoming) {
         BlockPos support = flat.below();
         ClassCache.Inner_FileCache below = at.apply(support.below());
         if (below.loaded() && !below.passable()) {
            return true;
         }

         if (incoming != null && incoming.type() == ClassCache.Inner_SyncManager.run) {
            return true;
         }

         if (init_Z) {
            ClassCache.Inner_FileCache under = at.apply(from.below());
            if (under.loaded() && !under.passable() && under.top16() >= 16 && !under.hazardFloor()) {
               return true;
            }
         }

         for (int[] d : init_IArr2) {
            BlockPos n = support.offset(d[0], 0, d[1]);
            if (!n.equals(from.below()) && at.apply(n).loaded() && !at.apply(n).passable()) {
               return true;
            }
         }

         return false;
      }

      static final class Inner_CachePool implements ClassCache.Inner_StateCache, Function<BlockPos, ClassCache.Inner_FileCache> {
         final ClassCache.Inner_StateCache init_ClassCache_StateCa;
         private long[] init_JArr;
         private ClassCache.Inner_FileCache[] init_ClassCache_FileCacArr;
         private final int open;
         int init_I;
         ClassCache.UiHelper init_ClassCache_UiHelpe;
         t.Inner_DataUtil init_t_DataUtil;
         static final int load_I = 6;
         long init_J;
         private StreamGate init_StreamGate;
         private Long2LongOpenHashMap init_Long2LongOpenHashM;
         private final Map<ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil, Object> init_Map = new HashMap<>();
         int read_I;
         int write_I;
         long load_J;
         private long update_J;
         private long sync_J;
         private boolean init_Z;
         private boolean load_Z;
         private boolean read_Z;
         private boolean write_Z;
         private boolean update_Z;
         private boolean sync_Z;
         ClassCache.Inner_NetHelper init_ClassCache_NetHelp;
         private Consumer<ClassCache.Inner_RenderState.Inner_DataUtil> init_Consumer;
         static final long read_J = 65000000L;
         static final long write_J = 200000000L;
         private boolean flush_Z;
         private static final Object init_Obj = new Object();
         private static final Long2ObjectOpenHashMap<Object> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
         private static final int poll = 65536;
         static final int update_I = 0;
         static final int sync_I = 1;
         static final int flush_I = 2;
         private final Long2ByteOpenHashMap init_Long2ByteOpenHashM;
         private final Map<Long, Object> load_Map;
         static final int close_I = 120;
         private static final ClassCache.Inner_FileCache init_ClassCache_FileCac = ClassCache.Inner_FileCache.fullBlock(1.0 / 0.0);
         private long[] load_JArr;
         private ClassCache.Inner_FileCache[] load_ClassCache_FileCacArr;
         private int push;
         private ClassCache.UiHelper load_ClassCache_UiHelpe;
         private t.Inner_DataUtil load_t_DataUtil;
         private boolean close_Z;
         private long flush_J;

         int init_I_I(int x, int y, int z, int minimum) {
            if (this.init_Long2LongOpenHashM == null) {
               this.init_Long2LongOpenHashM = new Long2LongOpenHashMap();
            }

            for (int band = y & -64; band >= (minimum & -64); band -= 64) {
               long key = BlockPos.asLong(x, band, z);
               long bits = this.init_Long2LongOpenHashM.getOrDefault(key, -9223372036854775808L);
               if (bits == -9223372036854775808L && !this.init_Long2LongOpenHashM.containsKey(key)) {
                  boolean pending = false;
                  bits = 0L;

                  for (int bit = 0; bit < 64; bit++) {
                     ClassCache.Inner_FileCache probe = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, band + bit, z);
                     if (probe == null) {
                        pending = true;
                     } else if (probe.loaded() && (!this.load_Z || !probe.cached()) && !probe.passable()) {
                        bits |= 1L << bit;
                     }
                  }

                  if (pending) {
                     this.init_I |= 1;
                  } else {
                     this.init_Long2LongOpenHashM.put(key, bits);
                  }
               }

               if (y - band < 63) {
                  bits &= -1L >>> 63 - (y - band);
               }

               if (bits != 0L) {
                  return Math.max(minimum - 1, band + 63 - Long.numberOfLeadingZeros(bits));
               }
            }

            return minimum - 1;
         }

         Inner_CachePool(ClassCache.Inner_StateCache grid, int capacity) {
            this.init_ClassCache_NetHelp = ClassCache.Inner_NetHelper.init_ClassCache_NetHelp;
            this.init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
            this.load_Map = new HashMap<>();
            this.load_JArr = new long[16];
            this.load_ClassCache_FileCacArr = new ClassCache.Inner_FileCache[16];
            this.init_ClassCache_StateCa = grid;
            this.open = capacity;
            this.init_JArr = new long[256];
            this.init_ClassCache_FileCacArr = new ClassCache.Inner_FileCache[256];
         }

         boolean init_Z() {
            return this.init_Z;
         }

         private boolean read(BlockPos cell) {
            return !this.init_ClassCache_FileCac_BlockPos(cell.above(2)).passable()
               ? true
               : !this.init_ClassCache_FileCac_BlockPos(cell.west().above()).passable()
                     && !this.init_ClassCache_FileCac_BlockPos(cell.east().above()).passable()
                  || !this.init_ClassCache_FileCac_BlockPos(cell.north().above()).passable()
                     && !this.init_ClassCache_FileCac_BlockPos(cell.south().above()).passable();
         }

         private boolean write(BlockPos from) {
            if (this.read_Z) {
               return false;
            }

            boolean skip = this.update_J >= 65000000L && (!this.read(from) || this.sync_J >= 200000000L);
            if (skip) {
               this.init_Z = true;
            }

            return skip;
         }

         int init_I_BlockPos(BlockPos cell) {
            ClassCache.Inner_FileCache p = this.init_ClassCache_FileCac_BlockPos(cell);
            if (p.door() && p.passable()) {
               short shape = this.init_ClassCache_StateCa.init_S_I(cell.getX(), cell.getY(), cell.getZ());
               if (shape != 0 && shape != 1 && shape != -1) {
                  List<AABB> boxes = e.init_List_S(shape);
                  if (boxes.isEmpty()) {
                     return 0;
                  }

                  AABB b = boxes.get(0);
                  return b.maxX - b.minX >= b.maxZ - b.minZ ? 1 : 2;
               } else {
                  return 0;
               }
            } else {
               return 0;
            }
         }

         boolean init_Z_BlockPos(BlockPos cell) {
            return this.init_I_BlockPos(cell) != 0 || this.init_I_BlockPos(cell.above()) != 0;
         }

         boolean init(BlockPos cell, int dx, int dz) {
            int axis = this.init_I_BlockPos(cell);
            if (axis == 0) {
               axis = this.init_I_BlockPos(cell.above());
            }

            if (axis == 0) {
               return false;
            } else if (dx != 0 && dz != 0) {
               return true;
            } else {
               return dx != 0 ? axis != 1 : axis != 2;
            }
         }

         StreamGate init_StreamGate() {
            if (this.init_StreamGate == null) {
               this.init_StreamGate = new StreamGate(this.init_ClassCache_StateCa, this);
            }

            return this.init_StreamGate;
         }

         boolean load(BlockPos cell) {
            long key = cell.asLong();
            byte known = this.init_Long2ByteOpenHashM.get(key);
            if (known != 0) {
               return known == 2;
            }

            boolean hurt = false;

            for (int[] d : ClassCache.Inner_RenderState.read_IArr2) {
               BlockPos c = cell.offset(d[0], 0, d[1]);
               if (this.init_ClassCache_FileCac_BlockPos(c).contactHazard() || this.init_ClassCache_FileCac_BlockPos(c.above()).contactHazard()) {
                  hurt = true;
                  break;
               }
            }

            this.init_Long2ByteOpenHashM.put(key, (byte)(hurt ? 2 : 1));
            return hurt;
         }

         double init_D_BlockPos(BlockPos cell) {
            return this.load(cell) ? 2.0 : 0.0;
         }

         BlockPos init_BlockPos_BlockPos(BlockPos from, double surfFrom, int dx, int dz) {
            long key = from.asLong() * 31L + (dx + 1) * 3 + (dz + 1);
            Object known = this.load_Map.get(key);
            if (known != null) {
               return known == init_Obj ? null : (BlockPos)known;
            }

            if (!this.load(from, dx, dz)) {
               this.load_Map.put(key, init_Obj);
               return null;
            }

            StreamGate w = this.init_StreamGate();
            long pendingBefore = w.init_J;
            BlockPos entry = this.init(w, from, surfFrom, (float)Math.toDegrees(Math.atan2(-dx, dz)), null);
            BlockPos landing = null;
            if (entry != null) {
               BlockPos homed = this.init(w, from, surfFrom, 0.0F / 0.0F, entry);
               if (homed != null) {
                  BlockPos bottom = homed;

                  while (this.init_ClassCache_FileCac_BlockPos(bottom.below()).water() && from.getY() - bottom.getY() < 40) {
                     bottom = bottom.below();
                  }

                  BlockPos top = homed;

                  while (this.init_ClassCache_FileCac_BlockPos(top.above()).water() && top.getY() < from.getY()) {
                     top = top.above();
                  }

                  if (ClassCache.Inner_RenderState.load(this.init_ClassCache_FileCac_BlockPos(bottom.below())) && top.getY() < from.getY()) {
                     landing = top;
                  }
               }
            }

            if (w.init_J == pendingBefore) {
               this.load_Map.put(key, landing == null ? init_Obj : landing);
            }

            return landing;
         }

         private boolean load(BlockPos from, int dx, int dz) {
            for (int k = 1; k <= 3; k++) {
               int x = from.getX() + dx * k;
               int z = from.getZ() + dz * k;

               for (int y = from.getY() - 1; y >= from.getY() - 40; y--) {
                  ClassCache.Inner_FileCache p = this.init_ClassCache_FileCac_BlockPos(new BlockPos(x, y, z));
                  if (!p.loaded()) {
                     break;
                  }

                  if (p.water()) {
                     return true;
                  }

                  if (!p.passable()) {
                     break;
                  }
               }
            }

            return false;
         }

         private BlockPos init(StreamGate w, BlockPos from, double surfFrom, float yaw, BlockPos toward) {
            NodeWalker.Inner_StreamUtil s = NodeWalker.Inner_StreamUtil.standing(from.getX() + 0.5, surfFrom, from.getZ() + 0.5);
            boolean airborne = false;
            ArrayList<NodeWalker.Inner_StreamUtil> flight = new ArrayList<>();

            for (int t = 0; t < 120; t++) {
               float heading = toward == null ? yaw : NodeWalker.init_F_D(s.x(), s.z(), toward.getX() + 0.5, toward.getZ() + 0.5);
               s = NodeWalker.init(s, new NodeWalker.Inner_DataUtil(1.0, 0.0, false, false, false, heading), w);
               if (!s.onGround()) {
                  airborne = true;
               } else if (airborne) {
                  return null;
               }

               if (airborne && s.horizontalCollision()) {
                  return null;
               }

               if (!airborne && t > 12) {
                  return null;
               }

               if (airborne) {
                  flight.add(s);
               }

               BlockPos feet = BlockPos.containing(s.x(), s.y() + 0.01, s.z());
               if (airborne && this.init_ClassCache_FileCac_BlockPos(feet).water()) {
                  return ClassCache.Inner_RenderState.init_Z_NodeWalker_CachePo(w, s.x(), s.z(), feet)
                        && ClassCache.Inner_RenderState.init_Z_NodeWalker_CachePo(w, flight, feet.getY())
                     ? feet
                     : null;
               }

               if (this.init_ClassCache_FileCac_BlockPos(feet).lava() || from.getY() - s.y() > 40.0) {
                  return null;
               }
            }

            return null;
         }

         TableIndex.Inner_StateCache init(BlockPos from, double surfFrom, BlockPos to, double surfTo, double approach, boolean sprint) {
            long approachBits = Double.doubleToLongBits(approach);
            ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil key = new ClassCache.Inner_RenderState.Inner_CachePool.Inner_DataUtil(
               from.asLong(), to.asLong(), approachBits, sprint
            );
            Object known = this.init_Map.get(key);
            if (known != null) {
               return known == init_Obj ? null : (TableIndex.Inner_StateCache)known;
            }

            boolean tight = !this.read_Z && (this.read(from) || this.read(to));
            if (this.read_Z || (tight ? this.sync_J < 200000000L : this.update_J < 65000000L)) {
               long began = System.nanoTime();
               StreamGate w = this.init_StreamGate();
               long pendingBefore = w.init_J;
               boolean staged = false;
               if (!this.write_Z) {
                  staged = !this.init_ClassCache_FileCac_BlockPos(from.above(2)).passable() || !this.init_ClassCache_FileCac_BlockPos(to.above(2)).passable();
                  if (this.read_Z) {
                     staged |= this.init_ClassCache_FileCac_BlockPos(from.below()).narrowFooting()
                        || this.init_ClassCache_FileCac_BlockPos(to.below()).narrowFooting()
                        || !this.init_ClassCache_FileCac_BlockPos(from).passable()
                        || !this.init_ClassCache_FileCac_BlockPos(to).passable();
                  }

                  for (int[] d : ClassCache.Inner_RenderState.init_IArr2) {
                     if (staged) {
                        break;
                     }

                     BlockPos side = from.offset(d[0], 0, d[1]);
                     BlockPos landingSide = to.offset(d[0], 0, d[1]);
                     staged = !this.init_ClassCache_FileCac_BlockPos(side.above()).passable()
                        || !this.init_ClassCache_FileCac_BlockPos(landingSide.above()).passable()
                        || this.read_Z
                           && (!this.init_ClassCache_FileCac_BlockPos(side).passable() || !this.init_ClassCache_FileCac_BlockPos(landingSide).passable());
                  }
               }

               long signature = w.init_J_BlockPos(from, to, staged) * 31L
                  + (to.getX() - from.getX()) * 1000003L
                  + (to.getY() - from.getY()) * 10007L
                  + (to.getZ() - from.getZ()) * 7L
                  + (sprint ? 3 : 0);
               if (this.init_ClassCache_NetHelp != ClassCache.Inner_NetHelper.init_ClassCache_NetHelp) {
                  signature ^= 6384245875588680899L * this.init_ClassCache_NetHelp.ordinal();
               }

               if (staged && this.read_Z) {
                  signature ^= -3263064605168079213L;
               }

               if (staged && this.sync_Z) {
                  signature ^= -7046029254386353131L;
               }

               if (this.update_Z) {
                  signature ^= -6752110988234923001L;
               }

               signature = signature * 31L + HashCommon.mix(approachBits);
               Object shared;
               synchronized (init_Long2ObjectOpenHas) {
                  shared = init_Long2ObjectOpenHas.get(signature);
               }

               TableIndex.Inner_StateCache plan;
               if (shared != null && w.init_J == pendingBefore) {
                  this.write_I++;
                  plan = shared == init_Obj
                     ? null
                     : ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)shared)
                        .plan()
                        .shifted(
                           from.getX() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)shared).x(),
                           from.getY() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)shared).y(),
                           from.getZ() - ((ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil)shared).z()
                        );
                  if (plan != null && plan.maneuver() != null && plan.maneuver().replay(w, plan.maneuver().start(), 0, to, surfTo) < -0.04) {
                     shared = null;
                  }
               } else {
                  plan = null;
               }

               if (shared == null) {
                  this.read_I++;
                  plan = TableIndex.init(w, from, surfFrom, to, surfTo, approach, sprint, staged, this.read_Z, this.sync_Z, this.update_Z);
                  if ((plan != null || !staged) && w.init_J == pendingBefore) {
                     synchronized (init_Long2ObjectOpenHas) {
                        if (init_Long2ObjectOpenHas.size() >= 65536) {
                           init_Long2ObjectOpenHas.clear();
                        }

                        init_Long2ObjectOpenHas.put(
                           signature,
                           plan == null
                              ? init_Obj
                              : new ClassCache.Inner_RenderState.Inner_CachePool.Inner_StreamUtil(plan, from.getX(), from.getY(), from.getZ())
                        );
                     }
                  }
               }

               if (w.init_J == pendingBefore) {
                  this.init_Map.put(key, plan == null ? init_Obj : plan);
               }

               long elapsed = System.nanoTime() - began;
               this.load_J += elapsed;
               if (tight) {
                  this.sync_J += elapsed;
               } else {
                  this.update_J += elapsed;
               }

               return plan;
            } else {
               this.init_Z = true;
               if (!this.flush_Z) {
                  this.flush_Z = true;
                  ListPool.init("jump solver budget spent: solves=" + this.read_I + " hits=" + this.write_I);
               }

               return null;
            }
         }

         private void init_V() {
            long[] oldKeys = this.init_JArr;
            ClassCache.Inner_FileCache[] oldValues = this.init_ClassCache_FileCacArr;
            this.init_JArr = new long[this.init_JArr.length * 2];
            this.init_ClassCache_FileCacArr = new ClassCache.Inner_FileCache[this.init_JArr.length];

            for (int i = 0; i < oldKeys.length; i++) {
               if (oldValues[i] != null) {
                  int slot = (int)HashCommon.mix(oldKeys[i]) & this.init_JArr.length - 1;
                  this.init_JArr[slot] = oldKeys[i];
                  this.init_ClassCache_FileCacArr[slot] = oldValues[i];
               }
            }
         }

         private void load_V() {
            this.close_Z = true;
            this.load_ClassCache_UiHelpe = this.init_ClassCache_UiHelpe;
            this.load_t_DataUtil = this.init_t_DataUtil;
            this.push = 0;
            this.flush_J = 0L;
            if (this.init_ClassCache_UiHelpe != null) {
               for (BlockPos c : this.init_ClassCache_UiHelpe.cellsToBreak()) {
                  this.init(c.asLong(), ClassCache.Inner_FileCache.air());
               }

               for (BlockPos c : this.init_ClassCache_UiHelpe.cellsToPlace()) {
                  this.init(c.asLong(), init_ClassCache_FileCac);
               }

               if (this.init_ClassCache_UiHelpe.type() == ClassCache.Inner_SyncManager.set) {
                  this.init(this.init_ClassCache_UiHelpe.to().asLong(), ClassCache.Inner_FileCache.air());
                  this.init(this.init_ClassCache_UiHelpe.to().above().asLong(), ClassCache.Inner_FileCache.air());
               }
            }

            if (this.init_t_DataUtil != null) {
               int depth = 0;

               for (t.Inner_DataUtil node = this.init_t_DataUtil.init_t_DataUtil; node != null && node.init_ClassCache_UiHelpe != null && depth < 6; depth++) {
                  ClassCache.UiHelper older = node.init_ClassCache_UiHelpe;

                  for (BlockPos c : older.cellsToPlace()) {
                     this.init(c.asLong(), init_ClassCache_FileCac);
                  }

                  for (BlockPos c : older.cellsToBreak()) {
                     this.init(c.asLong(), ClassCache.Inner_FileCache.air());
                  }

                  node = node.init_t_DataUtil;
               }
            }
         }

         private static long init_J_J(long key) {
            return 1L << (int)(key * -7046029254386353131L >>> 58);
         }

         private void init(long key, ClassCache.Inner_FileCache value) {
            this.flush_J = this.flush_J | init_J_J(key);
            if (this.push == this.load_JArr.length) {
               this.load_JArr = Arrays.copyOf(this.load_JArr, this.push * 2);
               this.load_ClassCache_FileCacArr = Arrays.copyOf(this.load_ClassCache_FileCacArr, this.push * 2);
            }

            this.load_JArr[this.push] = key;
            this.load_ClassCache_FileCacArr[this.push++] = value;
         }

         public ClassCache.Inner_FileCache apply(BlockPos p) {
            return this.init_ClassCache_FileCac_BlockPos(p);
         }

         public ClassCache.Inner_FileCache init_ClassCache_FileCac_BlockPos(BlockPos p) {
            return this.init_ClassCache_FileCac_I(p.getX(), p.getY(), p.getZ());
         }

         @Override
         public ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int x, int y, int z) {
            if (!this.close_Z || this.init_ClassCache_UiHelpe != this.load_ClassCache_UiHelpe || this.init_t_DataUtil != this.load_t_DataUtil) {
               this.load_V();
            }

            long key = BlockPos.asLong(x, y, z);
            if ((this.flush_J & init_J_J(key)) != 0L) {
               for (int i = 0; i < this.push; i++) {
                  if (this.load_JArr[i] == key) {
                     return this.load_ClassCache_FileCacArr[i];
                  }
               }
            }

            if (this.init_J >= this.init_JArr.length && this.init_JArr.length < this.open) {
               this.init_V();
            }

            int slot = (int)HashCommon.mix(key) & this.init_JArr.length - 1;
            if (this.init_ClassCache_FileCacArr[slot] != null && this.init_JArr[slot] == key) {
               return this.init_ClassCache_FileCacArr[slot];
            }

            this.init_J++;
            ClassCache.Inner_FileCache value = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z);
            if (value == null) {
               this.init_I |= 1;
               return ClassCache.Inner_FileCache.UNLOADED;
            }

            if (this.load_Z && value.cached()) {
               value = ClassCache.Inner_FileCache.UNLOADED;
            }

            this.init_JArr[slot] = key;
            this.init_ClassCache_FileCacArr[slot] = value;
            return value;
         }

         boolean load_Z() {
            if (!this.close_Z || this.init_ClassCache_UiHelpe != this.load_ClassCache_UiHelpe || this.init_t_DataUtil != this.load_t_DataUtil) {
               this.load_V();
            }

            return this.push != 0;
         }

         @Override
         public short init_S_I(int x, int y, int z) {
            if (!this.close_Z || this.init_ClassCache_UiHelpe != this.load_ClassCache_UiHelpe || this.init_t_DataUtil != this.load_t_DataUtil) {
               this.load_V();
            }

            long key = BlockPos.asLong(x, y, z);
            if ((this.flush_J & init_J_J(key)) != 0L) {
               for (int i = 0; i < this.push; i++) {
                  if (this.load_JArr[i] == key) {
                     return e.init_S_ClassCache_FileCac(this.load_ClassCache_FileCacArr[i]);
                  }
               }
            }

            return this.load_Z && !this.init_ClassCache_FileCac_I(x, y, z).loaded() ? 1 : this.init_ClassCache_StateCa.init_S_I(x, y, z);
         }

         @Override
         public short load_S(int x, int y, int z) {
            if (!this.close_Z || this.init_ClassCache_UiHelpe != this.load_ClassCache_UiHelpe || this.init_t_DataUtil != this.load_t_DataUtil) {
               this.load_V();
            }

            long key = BlockPos.asLong(x, y, z);
            if ((this.flush_J & init_J_J(key)) != 0L) {
               for (int i = 0; i < this.push; i++) {
                  if (this.load_JArr[i] == key) {
                     return e.init_S_ClassCache_FileCac(this.load_ClassCache_FileCacArr[i]);
                  }
               }
            }

            return this.init_ClassCache_StateCa.load_S(x, y, z);
         }

         private record Inner_DataUtil(long from, long to, long approach, boolean sprint) {
         }

         private record Inner_StreamUtil(TableIndex.Inner_StateCache plan, int x, int y, int z) {
         }
      }

      private record Inner_DataUtil(
         BlockPos from, BlockPos to, double sourceY, double targetY, double approach, boolean sprint, int dx, int dz, boolean landedAhead
      ) {
         BlockPos deeper() {
            return this.to.offset(this.dx, 0, this.dz);
         }
      }

      private record Inner_StreamUtil(int x, int z, int distance, int stepX, int stepZ, double length, double reach, double cornerReach, int exits) {
      }
   }

   public interface Inner_StateCache {
      ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int var1, int var2, int var3);

      default long init_J() {
         return 0L;
      }

      default short init_S_I(int x, int y, int z) {
         return e.init_S_ClassCache_FileCac(this.init_ClassCache_FileCac_I(x, y, z));
      }

      default short load_S(int x, int y, int z) {
         return this.init_S_I(x, y, z);
      }

      default byte init_B_I(int x, int y, int z) {
         return m.init(this.init_ClassCache_FileCac_I(x, y, z)).init();
      }
   }

   public static final class Inner_StreamUtil implements ClassCache.Inner_StateCache {
      private static final long init_J = 750000L;
      private final Object init_Obj = new Object();
      private volatile long load_J;
      private volatile long read_J;
      private BiPredicate<BlockState, ItemStack> init_BiPredicate;
      private final Minecraft init_Minecraft;
      private final ClientLevel init_ClientLevel;
      private final int init_I;
      private final int load_I;
      private final LongSupplier init_LongSupplier;
      private volatile ItemStack[] init_ItemStackArr;
      private final ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 init_ClassCache_StreamU_2;
      private final StateCache init_StateCache;
      private final ConcurrentHashMap<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil> init_ConcurrentHashMap = new ConcurrentHashMap<>();
      private final Set<Long> init_Set = ConcurrentHashMap.newKeySet();
      private final Set<Long> load_Set = ConcurrentHashMap.newKeySet();
      private final Map<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil> init_Map = new HashMap<>();
      private volatile BlockPos init_BlockPos = BlockPos.ZERO;
      private long write_J = -9223372036854775808L;
      private ClassCache.Inner_StreamUtil.Inner_DataUtil init_ClassCache_StreamU;
      private int read_I;
      private int[] init_IArr;
      private long update_J = -9223372036854775808L;
      private int write_I;
      private int update_I;
      private int sync_I;
      private int flush_I;
      private int close;
      private short[] init_SArr;
      private Map<Integer, String> load_Map;
      private static final long sync_J = 1000000000L;
      private long flush_J;

      @Override
      public long init_J() {
         return this.read_J;
      }

      public long load_J() {
         return this.load_J + this.init_J();
      }

      void init_V_J_J(long previous, long deadline) throws InterruptedException {
         synchronized (this.init_Obj) {
            while (this.load_J() == previous) {
               long left = deadline - System.nanoTime();
               if (left <= 0L) {
                  return;
               }

               TimeUnit.NANOSECONDS.timedWait(this.init_Obj, left);
            }
         }
      }

      private void load_V_2() {
         synchronized (this.init_Obj) {
            this.load_J++;
            this.init_Obj.notifyAll();
         }
      }

      public Inner_StreamUtil(Minecraft mc, BlockPos a, BlockPos b, ItemStack[] hotbarSnapshot) {
         this(mc, a, b, hotbarSnapshot, null);
      }

      public Inner_StreamUtil(final Minecraft mc, BlockPos a, BlockPos b, ItemStack[] hotbarSnapshot, StateCache memory) {
         this.init_Minecraft = mc;
         this.init_LongSupplier = System::nanoTime;
         this.init_ClientLevel = mc.level;
         int minY = this.init_ClientLevel.getMinY();
         int maxY = this.init_ClientLevel.getMaxY() - 1;
         this.init_I = Math.max(minY, Math.min(a.getY(), b.getY()) - 28);
         this.load_I = Math.min(maxY, Math.max(a.getY(), b.getY()) + 28);
         this.init_ItemStackArr = hotbarSnapshot;
         this.init_StateCache = memory;
         this.init_ClassCache_StreamU_2 = new ClassCache.Inner_StreamUtil.Inner_StreamUtil_2() {
            @Override
            public ClassCache.Inner_FileCache init_ClassCache_FileCac(BlockPos p) {
               return ClassCache.Inner_StreamUtil.init(
                  mc, Inner_StreamUtil.this.init_ClientLevel, p, Inner_StreamUtil.this.init_ItemStackArr, Inner_StreamUtil.this.init_BiPredicate
               );
            }

            @Override
            public boolean init(int cx, int cz) {
               return Inner_StreamUtil.this.init_ClientLevel.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null;
            }

            @Override
            public short init_S(BlockPos p) {
               return ClassCache.Inner_StreamUtil.init_S_ClientLevel_BlockPos(Inner_StreamUtil.this.init_ClientLevel, p);
            }

            @Override
            public byte init_B(BlockPos p) {
               return ClassCache.Inner_StreamUtil.init_B_ClientLevel_BlockPos(Inner_StreamUtil.this.init_ClientLevel, p);
            }
         };
      }

      Inner_StreamUtil(int yLo, int yHi, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 columns) {
         this(yLo, yHi, columns, null);
      }

      Inner_StreamUtil(int yLo, int yHi, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 columns, StateCache memory) {
         this(yLo, yHi, columns, memory, System::nanoTime);
      }

      Inner_StreamUtil(int yLo, int yHi, ClassCache.Inner_StreamUtil.Inner_StreamUtil_2 columns, StateCache memory, LongSupplier copyClock) {
         this.init_LongSupplier = copyClock;
         this.init_Minecraft = null;
         this.init_ClientLevel = null;
         this.init_I = yLo;
         this.load_I = yHi;
         this.init_ItemStackArr = null;
         this.init_ClassCache_StreamU_2 = columns;
         this.init_StateCache = memory;
      }

      public void init_V_BlockPos(BlockPos focus) {
         this.init_BlockPos = focus.immutable();
      }

      public void init_V_BiPredicate(BiPredicate<BlockState, ItemStack> accepted) {
         if (this.init_BiPredicate != accepted) {
            this.init_BiPredicate = accepted;
            this.init_Set.addAll(this.init_ConcurrentHashMap.keySet());
            this.init_ConcurrentHashMap.clear();
            this.init_Map.clear();
            this.read_J++;
            this.init_ClassCache_StreamU = null;
            this.write_J = -9223372036854775808L;
            this.load_V_2();
         }
      }

      public void init_V_ItemStackArr(ItemStack[] snapshot) {
         ItemStack[] previous = this.init_ItemStackArr;
         boolean same = previous != null && previous.length == snapshot.length;
         if (same) {
            for (int i = 0; i < snapshot.length; i++) {
               if (!init_Z_ItemStack_ItemStack(previous[i], snapshot[i])) {
                  same = false;
                  break;
               }
            }
         }

         if (!same && init_Z_ItemStackArr_ItemStackArr(previous, snapshot) && System.nanoTime() - this.flush_J < 1000000000L) {
            this.init_ItemStackArr = snapshot;
         } else {
            this.init_ItemStackArr = snapshot;
            if (!same) {
               if (init_Z_ItemStackArr_ItemStackArr(previous, snapshot)) {
                  this.flush_J = System.nanoTime();
               }

               this.init_Set.addAll(this.init_ConcurrentHashMap.keySet());
               this.init_ConcurrentHashMap.clear();
               this.init_Map.clear();
               this.read_J++;
               this.init_ClassCache_StreamU = null;
               this.write_J = -9223372036854775808L;
               this.load_V_2();
            }
         }
      }

      private static boolean init_Z_ItemStackArr_ItemStackArr(ItemStack[] a, ItemStack[] b) {
         if (a != null && b != null && a.length == b.length) {
            for (int i = 0; i < a.length; i++) {
               ItemStack x = a[i];
               ItemStack y = b[i];
               if (x != y) {
                  if (x == null || y == null) {
                     return false;
                  }

                  boolean xe = x.isEmpty();
                  boolean ye = y.isEmpty();
                  if (!xe && !ye) {
                     if (x.getItem() != y.getItem()) {
                        return false;
                     }

                     if (!Objects.equals(x.getEnchantments(), y.getEnchantments())) {
                        return false;
                     }

                     if (af.init(x, 2) != af.init(y, 2)) {
                        return false;
                     }
                  } else if (xe != ye) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }

      private static boolean init_Z_ItemStack_ItemStack(ItemStack a, ItemStack b) {
         if (a == b) {
            return true;
         } else if (a != null && b != null) {
            boolean aEmpty = a.isEmpty();
            boolean bEmpty = b.isEmpty();
            return !aEmpty && !bEmpty
               ? a.getItem() == b.getItem()
                  && a.getCount() == b.getCount()
                  && Objects.equals(a.getEnchantments(), b.getEnchantments())
                  && af.init(a, 2) == af.init(b, 2)
               : aEmpty == bEmpty;
         } else {
            return false;
         }
      }

      @Override
      public ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int x, int y, int z) {
         if (y >= this.init_I && y <= this.load_I) {
            long key = init_J_I_I(x >> 4, z >> 4);
            ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ConcurrentHashMap.get(key);
            if (copy != null && copy.revision() == IoWorker.init(key)) {
               if (copy.ready().get((x & 15) * 16 + (z & 15)) == 0) {
                  this.init_Set.add(key);
                  this.load_Set.add(init_J_I_I(x, z));
                  return null;
               }

               int height = this.load_I - this.init_I + 1;
               int i = copy.index(x, y, z, this.init_I, height);
               int flags = copy.flags()[i] & '\uffff';
               if ((flags & 1) == 0) {
                  return ClassCache.Inner_FileCache.UNLOADED;
               }

               int dig = copy.digX4()[i] & '\uffff';
               return flags == 3 && copy.top16()[i] == 0 && copy.mult10()[i] == 10 && dig == 65535
                  ? ClassCache.Inner_FileCache.air()
                  : new ClassCache.Inner_FileCache(
                     true,
                     (flags & 2) != 0,
                     copy.top16()[i] & 127,
                     (flags & 4) != 0,
                     (flags & 8) != 0,
                     (flags & 16) != 0,
                     (flags & 32) != 0,
                     (flags & 64) != 0,
                     (flags & 128) != 0,
                     copy.mult10()[i],
                     dig == 65535 ? 1.0 / 0.0 : dig / 4.0,
                     (flags & 256) != 0,
                     (flags & 512) != 0,
                     (flags & 1024) != 0,
                     (flags & 4096) != 0,
                     (flags & 2048) != 0,
                     copy.penalties()[i] & 65535,
                     (flags & 8192) != 0,
                     (flags & 16384) != 0,
                     (flags & 32768) != 0,
                     (copy.top16()[i] & 128) != 0
                  );
            } else {
               if (copy != null) {
                  this.init_ConcurrentHashMap.remove(key, copy);
               }

               this.init_Set.add(key);
               this.load_Set.add(init_J_I_I(x, z));
               return null;
            }
         } else {
            return ClassCache.Inner_FileCache.UNLOADED;
         }
      }

      private ClassCache.Inner_StreamUtil.Inner_DataUtil init_ClassCache_StreamU_I_I(int x, int z) {
         long key = init_J_I_I(x >> 4, z >> 4);
         ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ConcurrentHashMap.get(key);
         if (copy != null && copy.revision() == IoWorker.init(key) && copy.ready().get((x & 15) * 16 + (z & 15)) != 0) {
            return copy;
         }

         if (copy != null && copy.revision() != IoWorker.init(key)) {
            this.init_ConcurrentHashMap.remove(key, copy);
         }

         this.init_Set.add(key);
         this.load_Set.add(init_J_I_I(x, z));
         return null;
      }

      @Override
      public short init_S_I(int x, int y, int z) {
         if (y < this.init_I) {
            return 1;
         } else if (y > this.load_I) {
            return 0;
         } else {
            ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ClassCache_StreamU_I_I(x, z);
            if (copy == null) {
               return -1;
            } else {
               int i = copy.index(x, y, z, this.init_I, this.load_I - this.init_I + 1);
               int flags = copy.flags()[i];
               if ((flags & 1) == 0) {
                  return 1;
               } else {
                  return copy.shape() != null ? copy.shape()[i] : e.init(true, (flags & 2) != 0, copy.top16()[i] & 127);
               }
            }
         }
      }

      @Override
      public byte init_B_I(int x, int y, int z) {
         if (y >= this.init_I && y <= this.load_I) {
            ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ClassCache_StreamU_I_I(x, z);
            if (copy == null) {
               return m.init_m.init();
            } else {
               int i = copy.index(x, y, z, this.init_I, this.load_I - this.init_I + 1);
               if (copy.surface() != null) {
                  return copy.surface()[i];
               } else {
                  int flags = copy.flags()[i];
                  if ((flags & 4096) != 0) {
                     return m.load_m.init();
                  } else {
                     return (flags & 8192) != 0 ? m.sync.init() : m.init_m.init();
                  }
               }
            }
         } else {
            return m.init_m.init();
         }
      }

      public void init_V_2() {
         this.init_V_J(750000L);
      }

      public void init_V_J(long budgetNanos) {
         if (!this.init_Map.isEmpty()) {
            this.read();
         }

         long deadline = this.init_LongSupplier.getAsLong() + budgetNanos;
         int height = this.load_I - this.init_I + 1;

         while (this.init_LongSupplier.getAsLong() < deadline) {
            Long columnKey = this.init_Long();
            if (columnKey != null) {
               this.load_Set.remove(columnKey);
               int x = (int)(columnKey >> 32);
               int z = columnKey.intValue();
               long key = init_J_I_I(x >> 4, z >> 4);
               if (this.init_ClassCache_StreamU_2.init(x >> 4, z >> 4)) {
                  ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ConcurrentHashMap.get(key);
                  if (copy == null || copy.revision() != IoWorker.init(key)) {
                     copy = this.init_ClassCache_StreamU_I_J(height, key);
                     this.init_ConcurrentHashMap.put(key, copy);
                  }

                  int column = (x & 15) * 16 + (z & 15);
                  if (copy.ready().get(column) == 0) {
                     this.init_V_ClassCache_StreamU(copy, x, z, height);
                     copy.ready().set(column, 1);
                     this.load_V_2();
                  }
                  continue;
               }
            }

            if (this.init_ClassCache_StreamU != null && this.init_ConcurrentHashMap.get(this.write_J) != this.init_ClassCache_StreamU) {
               this.init_ClassCache_StreamU = null;
               this.write_J = -9223372036854775808L;
            }

            if (this.init_ClassCache_StreamU == null) {
               Long next = this.load_Long();
               if (next == null) {
                  if (this.update_J != -9223372036854775808L) {
                     this.init_Z_J(deadline);
                  }

                  return;
               }

               this.write_J = next;
               this.read_I = 0;
               this.init_IArr = init_IArr_I_I(
                  Math.max(0, Math.min(15, this.init_BlockPos.getX() - ((int)(next >> 32) << 4))),
                  Math.max(0, Math.min(15, this.init_BlockPos.getZ() - (next.intValue() << 4)))
               );
               this.init_ClassCache_StreamU = this.init_ConcurrentHashMap.get(next);
               if (this.init_ClassCache_StreamU == null || this.init_ClassCache_StreamU.revision() != IoWorker.init(next)) {
                  this.init_ClassCache_StreamU = this.init_ClassCache_StreamU_I_J(height, next);
               }

               int cx = (int)(next >> 32);
               int cz = next.intValue();
               if (!this.init_ClassCache_StreamU_2.init(cx, cz)) {
                  ClassCache.Inner_StreamUtil.Inner_DataUtil remembered = this.init_StateCache == null
                     ? null
                     : this.init_StateCache.init_ClassCache_StreamU_I(cx, cz, this.init_I, height, IoWorker.init(next));
                  if (remembered != null) {
                     this.init_ConcurrentHashMap.put(next, remembered);
                     this.init_Map.put(next, remembered);
                  } else {
                     for (int col = 0; col < 256; col++) {
                        this.init_ClassCache_StreamU.ready().set(col, 1);
                     }

                     this.init_ConcurrentHashMap.put(next, this.init_ClassCache_StreamU);
                     this.init_Map.put(next, this.init_ClassCache_StreamU);
                  }

                  this.init_Set.remove(next);
                  this.load_V_2();
                  this.init_ClassCache_StreamU = null;
                  this.write_J = -9223372036854775808L;
                  continue;
               }

               this.init_ConcurrentHashMap.put(next, this.init_ClassCache_StreamU);
            }

            ClassCache.Inner_StreamUtil.Inner_DataUtil copy = this.init_ClassCache_StreamU;
            int cx = (int)(this.write_J >> 32);
            int cz = (int)this.write_J;

            while (this.read_I < 256 && this.init_LongSupplier.getAsLong() < deadline) {
               int column = this.init_IArr[this.read_I];
               if (copy.ready().get(column) == 0) {
                  this.init_V_ClassCache_StreamU(copy, (cx << 4) + column / 16, (cz << 4) + column % 16, height);
                  copy.ready().set(column, 1);
                  this.load_V_2();
               }

               this.read_I++;
            }

            if (this.read_I < 256) {
               return;
            }

            this.init_ConcurrentHashMap.put(this.write_J, copy);
            this.init_Set.remove(this.write_J);
            this.load_V_2();
            if (this.init_StateCache != null && this.init_ClientLevel != null && height <= 128 && this.update_J == -9223372036854775808L) {
               this.update_J = this.write_J;
               this.write_I = cx;
               this.update_I = cz;
               this.init_SArr = copy.flags();
               this.close = copy.revision();
               this.sync_I = 0;
               this.flush_I = 0;
               this.load_Map = null;
            }

            this.init_ClassCache_StreamU = null;
            this.write_J = -9223372036854775808L;
         }
      }

      private void read() {
         Iterator<Entry<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil>> iterator = this.init_Map.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Long, ClassCache.Inner_StreamUtil.Inner_DataUtil> entry = iterator.next();
            long key = entry.getKey();
            if (this.init_ClassCache_StreamU_2.init((int)(key >> 32), (int)key)) {
               this.init_ConcurrentHashMap.remove(key, entry.getValue());
               this.init_Set.add(key);
               iterator.remove();
            }
         }
      }

      private boolean init_Z_J(long deadline) {
         int height = this.load_I - this.init_I + 1;
         MutableBlockPos p = new MutableBlockPos();

         while (this.sync_I < 256) {
            if (this.init_LongSupplier.getAsLong() >= deadline) {
               return false;
            }

            int x = this.sync_I >> 4;
            int z = this.sync_I & 15;

            while (this.flush_I < height) {
               int i = (x * 16 + z) * height + this.flush_I;
               short flags = this.init_SArr[i];
               if ((flags & 1) != 0 && StateCache.init_I(flags) == 1) {
                  p.set((this.write_I << 4) + x, this.init_I + this.flush_I, (this.update_I << 4) + z);
                  String path = BuiltInRegistries.BLOCK.getKey(this.init_ClientLevel.getBlockState(p).getBlock()).getPath();
                  if (StateCache.init(path)) {
                     if (this.load_Map == null) {
                        this.load_Map = new HashMap<>();
                     }

                     this.load_Map.put(this.flush_I * 256 + z * 16 + x, "minecraft:" + path);
                  }
               }

               this.flush_I++;
            }

            this.flush_I = 0;
            this.sync_I++;
         }

         long key = this.update_J;
         int cx = this.write_I;
         int cz = this.update_I;
         int revision = this.close;
         short[] flags = this.init_SArr;
         Map<Integer, String> found = this.load_Map;
         this.update_J = -9223372036854775808L;
         this.init_SArr = null;
         this.load_Map = null;
         if (revision == IoWorker.init(key)) {
            this.init_StateCache.init(cx, cz, this.init_I, height, flags, found);
         }

         return true;
      }

      private void init_V_ClassCache_StreamU(ClassCache.Inner_StreamUtil.Inner_DataUtil copy, int x, int z, int height) {
         MutableBlockPos p = new MutableBlockPos();

         for (int y = this.init_I; y <= this.load_I; y++) {
            p.set(x, y, z);
            ClassCache.Inner_FileCache probe = this.init_ClassCache_StreamU_2.init_ClassCache_FileCac(p);
            int i = copy.index(x, y, z, this.init_I, height);
            if (!probe.loaded()) {
               copy.shape()[i] = 1;
            } else {
               copy.shape()[i] = this.init_ClassCache_StreamU_2.init_S(p);
               copy.surface()[i] = this.init_ClassCache_StreamU_2.init_B(p);
               int flags = 1;
               if (probe.passable()) {
                  flags |= 2;
               }

               if (probe.water()) {
                  flags |= 4;
               }

               if (probe.lava()) {
                  flags |= 8;
               }

               if (probe.climbable()) {
                  flags |= 16;
               }

               if (probe.hazardFeet()) {
                  flags |= 32;
               }

               if (probe.hazardFloor()) {
                  flags |= 64;
               }

               if (probe.falling()) {
                  flags |= 128;
               }

               if (probe.door()) {
                  flags |= 256;
               }

               if (probe.slabStair()) {
                  flags |= 512;
               }

               if (probe.farmland()) {
                  flags |= 1024;
               }

               if (probe.ice()) {
                  flags |= 4096;
               }

               if (probe.soulSand()) {
                  flags |= 8192;
               }

               if (probe.thorny()) {
                  flags |= 16384;
               }

               if (probe.portal()) {
                  flags |= 32768;
               }

               copy.flags()[i] = (short)flags;
               copy.top16()[i] = (byte)(probe.top16() | (probe.narrowFooting() ? 128 : 0));
               copy.mult10()[i] = (byte)probe.fallMult10();
               copy.penalties()[i] = (short)Math.min(65535, probe.destructionPenalty());
               copy.digX4()[i] = probe.breakTicks() == 1.0 / 0.0 ? -1 : (short)Math.min(65534, (int)Math.round(probe.breakTicks() * 4.0));
            }
         }
      }

      private static int[] init_IArr_I_I(int x, int z) {
         int[] order = new int[256];
         int count = 0;

         for (int radius = 0; radius < 16; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     int xx = x + dx;
                     int zz = z + dz;
                     if (xx >= 0 && xx < 16 && zz >= 0 && zz < 16) {
                        order[count++] = xx * 16 + zz;
                     }
                  }
               }
            }
         }

         return order;
      }

      private ClassCache.Inner_StreamUtil.Inner_DataUtil init_ClassCache_StreamU_I_J(int height, long key) {
         return new ClassCache.Inner_StreamUtil.Inner_DataUtil(
            new short[256 * height],
            new byte[256 * height],
            new short[256 * height],
            new byte[256 * height],
            IoWorker.init(key),
            new short[256 * height],
            new AtomicIntegerArray(256),
            new short[256 * height],
            new byte[256 * height]
         );
      }

      private Long init_Long() {
         Long best = null;
         long distance = 9223372036854775807L;

         for (Long key : this.load_Set) {
            long dx = (int)(key >> 32) - this.init_BlockPos.getX();
            long dz = key.intValue() - this.init_BlockPos.getZ();
            long d = dx * dx + dz * dz;
            if (d < distance || d == distance && (best == null || key < best)) {
               best = key;
               distance = d;
            }
         }

         return best;
      }

      private Long load_Long() {
         Long best = null;
         double bestDist = 1.7976931348623157E308;

         for (Long key : this.init_Set) {
            int cx = (int)(key >> 32);
            int cz = (int)key.longValue();
            double dx = (cx << 4) + 8 - this.init_BlockPos.getX();
            double dz = (cz << 4) + 8 - this.init_BlockPos.getZ();
            double dist = dx * dx + dz * dz;
            if (dist < bestDist) {
               bestDist = dist;
               best = key;
            }
         }

         return best;
      }

      private static long init_J_I_I(int cx, int cz) {
         return IoWorker.init_J(cx, cz);
      }

      public static ClassCache.Inner_FileCache init_ClassCache_FileCac_Minecraft(Minecraft mc, ClientLevel level, BlockPos p, ItemStack[] hotbar) {
         return init(mc, level, p, hotbar, null);
      }

      private static ClassCache.Inner_FileCache init(
         Minecraft mc, ClientLevel level, BlockPos p, ItemStack[] hotbar, BiPredicate<BlockState, ItemStack> accepted
      ) {
         if (level != null && level.isInsideBuildHeight(p) && level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState state = level.getBlockState(p);
            boolean sulfurImpulse = TextFormat.init(level, p);
            if (state.isAir()) {
               return sulfurImpulse ? ClassCache.Inner_FileCache.air().withHazards(true, false) : ClassCache.Inner_FileCache.air();
            }

            FluidState fluid = state.getFluidState();
            boolean water = fluid.is(FluidTags.WATER);
            boolean lava = fluid.is(FluidTags.LAVA);
            VoxelShape shape = state.getCollisionShape(level, p);
            boolean passable = shape.isEmpty();
            Block block = state.getBlock();
            boolean door = false;
            if (block instanceof DoorBlock) {
               door = DoorBlock.isWoodenDoor(state);
               if (door && state.getValue(BlockStateProperties.OPEN)) {
                  passable = true;
               }
            } else if (block instanceof FenceGateBlock) {
               door = true;
               if (state.getValue(BlockStateProperties.OPEN)) {
                  passable = true;
               }
            }

            boolean climb = init_Z_BlockState(state);
            if (climb && !(block instanceof ScaffoldingBlock)) {
               passable = true;
            }

            int top16 = passable ? 0 : (int)Math.round(shape.max(Axis.Y) * 16.0);
            boolean hazardFeet = load(state) || sulfurImpulse;
            boolean thorny = state.is(Blocks.SWEET_BERRY_BUSH);
            boolean contactHazard = read(state);
            boolean narrowFooting = !contactHazard && !passable && !init_Z_VoxelShape(shape);
            boolean portal = write(state);
            String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
            int mult10 = Math.round(init_F_Str_Z(path, water) * 10.0F);
            boolean falling = block instanceof FallingBlock;
            boolean slabStair = block instanceof SlabBlock || block instanceof StairBlock;
            boolean farmland = block instanceof FarmlandBlock;
            boolean ice = state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE);
            boolean soulSand = state.is(Blocks.SOUL_SAND);
            double dig = 1.0 / 0.0;
            if ((!passable || block instanceof WebBlock) && fluid.isEmpty()) {
               float hardness = state.getDestroySpeed(level, p);
               BlockState under = level.getBlockState(p.below());
               boolean melts = init_Z_Z(
                  state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE),
                  under.isSolid(),
                  under.liquid(),
                  level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
               );
               if (hardness >= 0.0F
                  && !state.hasBlockEntity()
                  && !melts
                  && init_Z_Function_BlockPos(pos -> init_ClassCache_StreamU_ClientLevel_BlockPos(level, pos), p)
                  && (mc.player.hasInfiniteMaterials() || !init_Z_Z_Z(state.requiresCorrectToolForDrops(), init_Z_BlockState_ItemStackArr(state, hotbar)))) {
                  r.Inner_DataUtil factors = r.Inner_DataUtil.of(mc);
                  dig = accepted == null ? init(mc, state, hardness, hotbar, factors) : init(mc, state, hardness, hotbar, factors, accepted);

                  for (BlockPos above = p.above(); level.isInsideBuildHeight(above); above = above.above()) {
                     BlockState resting = level.getBlockState(above);
                     if (!(resting.getBlock() instanceof FallingBlock)) {
                        break;
                     }

                     dig += accepted == null
                        ? init(mc, resting, resting.getDestroySpeed(level, above), hotbar, factors)
                        : init(mc, resting, resting.getDestroySpeed(level, above), hotbar, factors, accepted);
                  }
               }
            }

            return new ClassCache.Inner_FileCache(
               true,
               passable,
               top16,
               water,
               lava,
               climb,
               hazardFeet,
               contactHazard || narrowFooting,
               falling,
               mult10,
               dig,
               door,
               slabStair,
               farmland,
               ice,
               false,
               ClassCache.Inner_CachePool.init_I_Str(path),
               soulSand,
               thorny,
               portal,
               narrowFooting
            );
         } else {
            return ClassCache.Inner_FileCache.UNLOADED;
         }
      }

      public static boolean init_Z_VoxelShape(VoxelShape shape) {
         if (shape.isEmpty()) {
            return false;
         }

         double top = shape.max(Axis.Y);

         for (AABB box : shape.toAabbs()) {
            if (!(box.maxY < top - 1.0E-6) && box.maxX > 0.3 && box.minX < 0.7 && box.maxZ > 0.3 && box.minZ < 0.7) {
               return true;
            }
         }

         return false;
      }

      static boolean init_Z_BlockState(BlockState state) {
         return state.getBlock() instanceof LadderBlock || state.is(BlockTags.CLIMBABLE);
      }

      public static boolean load(BlockState state) {
         Block block = state.getBlock();
         return block instanceof BaseFireBlock
            || state.is(Blocks.POWDER_SNOW)
            || state.is(Blocks.WITHER_ROSE)
            || state.is(Blocks.LAVA_CAULDRON)
            || block instanceof BubbleColumnBlock && state.getValue(BubbleColumnBlock.DRAG_DOWN);
      }

      public static boolean read(BlockState state) {
         Block block = state.getBlock();
         return state.is(Blocks.MAGMA_BLOCK)
            || state.is(Blocks.LAVA_CAULDRON)
            || state.is(Blocks.CACTUS)
            || block instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)
            || block instanceof PointedDripstoneBlock
            || block instanceof SulfurSpikeBlock
            || block instanceof PotentSulfurBlock
            || block instanceof SculkShriekerBlock && state.getValue(SculkShriekerBlock.CAN_SUMMON);
      }

      static boolean write(BlockState state) {
         Block block = state.getBlock();
         return block instanceof NetherPortalBlock || block instanceof EndPortalBlock || block instanceof EndGatewayBlock;
      }

      public static short init_S_ClientLevel_BlockPos(ClientLevel level, BlockPos p) {
         if (level != null && level.isInsideBuildHeight(p) && level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) != null) {
            BlockState state = level.getBlockState(p);
            if (state.isAir()) {
               return 0;
            } else {
               VoxelShape shape = state.getCollisionShape(level, p);
               if (shape.isEmpty()) {
                  return 0;
               } else if (init_Z_BlockState(state) && !(state.getBlock() instanceof ScaffoldingBlock)) {
                  return 0;
               } else {
                  return shape == Shapes.block() ? 1 : e.init_S_List(shape.toAabbs());
               }
            }
         } else {
            return 1;
         }
      }

      public static byte init_B_ClientLevel_BlockPos(ClientLevel level, BlockPos p) {
         if (level != null && level.isInsideBuildHeight(p)) {
            Block block = level.getBlockState(p).getBlock();
            double bounce = block instanceof SlimeBlock ? 1.0 : (block instanceof BedBlock ? 0.75 : 0.0);
            return m.init(block.getFriction(), block.getSpeedFactor(), block.getJumpFactor(), bounce).init();
         } else {
            return m.init_m.init();
         }
      }

      static boolean init_Z_Function_BlockPos(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> at, BlockPos target) {
         return !write(at, target) && !read(at, target) && init_I_Function_BlockPos(at, target) <= 6 && !load(at, target);
      }

      static boolean load(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> at, BlockPos target) {
         BlockPos cursor = target.above();

         for (int guard = 0; guard < 384; guard++) {
            ClassCache.Inner_StreamUtil.Inner_CachePool cell = at.apply(cursor);
            if (cell == null || !cell.falling()) {
               return false;
            }

            if (read(at, cursor)) {
               return true;
            }

            cursor = cursor.above();
         }

         return false;
      }

      static boolean read(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> at, BlockPos target) {
         for (int[] d : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            ClassCache.Inner_StreamUtil.Inner_CachePool cell = at.apply(target.offset(d[0], d[1], d[2]));
            if (cell != null && cell.water()) {
               return true;
            }
         }

         return false;
      }

      static boolean write(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> at, BlockPos target) {
         for (int[] d : new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}}) {
            ClassCache.Inner_StreamUtil.Inner_CachePool cell = at.apply(target.offset(d[0], d[1], d[2]));
            if (cell != null && cell.lava()) {
               return true;
            }
         }

         return false;
      }

      static int init_I_Function_BlockPos(Function<BlockPos, ClassCache.Inner_StreamUtil.Inner_CachePool> at, BlockPos target) {
         int stack = 0;
         BlockPos cursor = target.above();

         for (int guard = 0; guard < 384; guard++) {
            ClassCache.Inner_StreamUtil.Inner_CachePool cell = at.apply(cursor);
            if (cell == null || !cell.falling()) {
               return stack;
            }

            stack++;
            cursor = cursor.above();
         }

         return stack;
      }

      static boolean init_Z_ClientLevel_BlockPos(ClientLevel level, BlockPos target) {
         return init_Z_Function_BlockPos(p -> init_ClassCache_StreamU_ClientLevel_BlockPos(level, p), target);
      }

      public static boolean init_Z_Z(boolean ice, boolean belowBlocksMotion, boolean belowLiquid, boolean waterEvaporates) {
         return ice && !waterEvaporates && (belowBlocksMotion || belowLiquid);
      }

      public static boolean init_Z_BlockGetter_BlockPos(BlockGetter level, BlockPos target) {
         try {
            for (Direction d : Direction.values()) {
               if (level.getBlockState(target.relative(d)).is(Blocks.NETHER_PORTAL)) {
                  return true;
               }
            }

            return false;
         } catch (RuntimeException raced) {
            return false;
         }
      }

      static boolean init_Z_ClientLevel(ClientLevel level, BlockPos target, Set<BlockPos> sealedWater) {
         return sealedWater.isEmpty()
            ? init_Z_ClientLevel_BlockPos(level, target)
            : init_Z_Function_BlockPos(
               p -> {
                  ClassCache.Inner_StreamUtil.Inner_CachePool cell = init_ClassCache_StreamU_ClientLevel_BlockPos(level, p);
                  return cell != null && cell.water() && sealedWater.contains(p)
                     ? new ClassCache.Inner_StreamUtil.Inner_CachePool(cell.falling(), cell.open(), cell.lava(), false)
                     : cell;
               },
               target
            );
      }

      private static ClassCache.Inner_StreamUtil.Inner_CachePool init_ClassCache_StreamU_ClientLevel_BlockPos(ClientLevel level, BlockPos p) {
         if (!level.isInsideBuildHeight(p)) {
            return null;
         }

         if (level.getChunkSource().getChunk(p.getX() >> 4, p.getZ() >> 4, ChunkStatus.FULL, false) == null) {
            return new ClassCache.Inner_StreamUtil.Inner_CachePool(false, false, true);
         }

         BlockState state = level.getBlockState(p);
         return new ClassCache.Inner_StreamUtil.Inner_CachePool(
            state.getBlock() instanceof FallingBlock,
            state.getCollisionShape(level, p).isEmpty(),
            state.getFluidState().is(FluidTags.LAVA),
            state.getFluidState().is(FluidTags.WATER)
         );
      }

      public static boolean update(BlockState state) {
         return state.is(BlockTags.IRON_ORES)
            || state.is(BlockTags.GOLD_ORES)
            || state.is(BlockTags.COPPER_ORES)
            || BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath().endsWith("_ore");
      }

      static boolean init_Z_Z_Z(boolean requiresCorrectTool, boolean correctToolAvailable) {
         return requiresCorrectTool && !correctToolAvailable;
      }

      private static boolean init_Z_ItemStack_BlockState(ItemStack stack, BlockState state) {
         return stack != null && !stack.isEmpty() && stack.isCorrectToolForDrops(state) && af.init(stack, 2);
      }

      static boolean init_Z_BlockState_ItemStackArr(BlockState state, ItemStack[] snapshot) {
         for (ItemStack stack : snapshot) {
            if (init_Z_ItemStack_BlockState(stack, state)) {
               return true;
            }
         }

         return false;
      }

      public static boolean init_Z_Minecraft(Minecraft mc, BlockState state, int slots) {
         for (int i = 0; i < slots; i++) {
            if (init_Z_ItemStack_BlockState(mc.player.getInventory().getItem(i), state)) {
               return true;
            }
         }

         return false;
      }

      static float init_F_Str_Z(String blockPath, boolean water) {
         if (water) {
            return 0.0F;
         }

         float var10000;
         switch (blockPath) {
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
               var10000 = blockPath.endsWith("_bed") ? 0.5F : 1.0F;
         }

         return var10000;
      }

      private static double init_D_Minecraft(Minecraft mc, BlockState state, float hardness, ItemStack[] hotbar) {
         return init(mc, state, hardness, hotbar, r.Inner_DataUtil.of(mc));
      }

      static double init(Minecraft mc, BlockState state, float hardness, ItemStack[] hotbar, r.Inner_DataUtil factors) {
         double best = r.init_D_Minecraft(mc, ItemStack.EMPTY, state, hardness, factors);

         for (ItemStack stack : hotbar) {
            if (stack != null && !stack.isEmpty() && af.init(stack, 2)) {
               best = Math.min(best, r.init_D_Minecraft(mc, stack, state, hardness, factors));
            }
         }

         return best;
      }

      static double init(
         Minecraft mc, BlockState state, float hardness, ItemStack[] tools, r.Inner_DataUtil factors, BiPredicate<BlockState, ItemStack> accepted
      ) {
         factors = init_r_DataUtil_r_DataUtil(factors, tools, state, accepted);
         boolean needsTool = state.requiresCorrectToolForDrops() && !factors.creative();
         double best = !needsTool && accepted.test(state, ItemStack.EMPTY) ? r.init_D_Minecraft(mc, ItemStack.EMPTY, state, hardness, factors) : 1.0 / 0.0;

         for (ItemStack stack : tools) {
            if (stack != null && !stack.isEmpty() && af.init(stack, 2) && (!needsTool || stack.isCorrectToolForDrops(state)) && accepted.test(state, stack)) {
               best = Math.min(best, r.init_D_Minecraft(mc, stack, state, hardness, factors));
            }
         }

         return best;
      }

      static r.Inner_DataUtil init_r_DataUtil_r_DataUtil(
         r.Inner_DataUtil factors, ItemStack[] tools, BlockState state, BiPredicate<BlockState, ItemStack> accepted
      ) {
         if (!factors.stonePick()) {
            return factors;
         }

         for (ItemStack stack : tools) {
            if (stack != null && stack.is(Items.STONE_PICKAXE) && af.init(stack, 2) && accepted.test(state, stack)) {
               return factors;
            }
         }

         return new r.Inner_DataUtil(factors.haste(), factors.fatigue(), factors.blockSpeed(), factors.creative(), factors.ironReserve(), false);
      }

      record Inner_CachePool(boolean falling, boolean open, boolean lava, boolean water) {
         Inner_CachePool(boolean falling, boolean open, boolean lava) {
            this(falling, open, lava, false);
         }
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
         static final int HEIGHT_MASK = 127;

         Inner_DataUtil(short[] flags, byte[] top16, short[] digX4, byte[] mult10, int revision) {
            this(flags, top16, digX4, mult10, revision, new short[flags.length], allReady(), null, null);
         }

         private static AtomicIntegerArray allReady() {
            int[] ready = new int[256];
            Arrays.fill(ready, 1);
            return new AtomicIntegerArray(ready);
         }

         int index(int x, int y, int z, int yLo, int height) {
            return ((x & 15) * 16 + (z & 15)) * height + (y - yLo);
         }
      }

      interface Inner_StreamUtil_2 {
         ClassCache.Inner_FileCache init_ClassCache_FileCac(BlockPos var1);

         boolean init(int var1, int var2);

         default short init_S(BlockPos p) {
            return e.init_S_ClassCache_FileCac(this.init_ClassCache_FileCac(p));
         }

         default byte init_B(BlockPos p) {
            return m.init(this.init_ClassCache_FileCac(p)).init();
         }
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
      stop,
      next;
   }

   public interface Inner_TaskQueue {
      boolean write_Z();

      int close_I();

      default int flush_I() {
         return 0;
      }

      default List<ClassCache.UiHelper> init_List() {
         return List.of();
      }

      BlockPos write_BlockPos();
   }

   public static final class Inner_TextFormat {
      public static final int init_I = 64;
      public static final int load_I = 40;
      public static final int read_I = 2000000;
      private static final int update_I = 3000000;
      static final double[] init_DArr = new double[]{1.5, 2.0, 2.5, 3.0, 4.0, 5.0, 10.0};
      static final int write_I = 5;
      private final t.Inner_DataUtil[] init_t_DataUtilArr = new t.Inner_DataUtil[init_DArr.length];
      private final double[] load_DArr = new double[init_DArr.length];
      private final ClassCache.Inner_StateCache init_ClassCache_StateCa;
      private final BlockPos init_BlockPos;
      private final ClassCache.Inner_JsonUtil init_ClassCache_JsonUti;
      private final ClassCache.Inner_DataUtil init_ClassCache_DataUti;
      private final Set<Long> load_Set;
      private final t init_t = new t();
      private final Long2ObjectOpenHashMap<t.Inner_DataUtil> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>(256);
      private final ClassCache.Inner_RenderState.Inner_CachePool init_ClassCache_RenderS;
      private final List<ClassCache.UiHelper> init_List = new ArrayList<>(16);
      private t.Inner_DataUtil init_t_DataUtil;
      private final long init_J;
      private int sync_I;
      private int flush_I;
      private int close_I;
      private int open_I = 1;
      private boolean init_Z;
      private boolean load_Z;
      private boolean read_Z;
      private boolean write_Z;
      private boolean update_Z;
      private int poll = 64;
      private int push = 40;
      private boolean sync_Z;
      private boolean flush_Z;
      private t.Inner_DataUtil load_t_DataUtil;
      private PriorityQueue<ClassCache.Inner_TextFormat.Inner_StreamUtil> init_PriorityQueue;
      private final Map<ClassCache.Inner_TextFormat.Inner_StreamUtil, Future<ClassCache.Inner_TextFormat.Inner_DataUtil>> init_Map = new HashMap<>();
      private final ConcurrentLinkedQueue<ClassCache.Inner_RenderState.Inner_CachePool> init_ConcurrentLinkedQu = new ConcurrentLinkedQueue<>();
      private static final int pop = Math.clamp(Runtime.getRuntime().availableProcessors() / 2, 1, 8);
      private static final ExecutorService init_ExecutorService = Executors.newFixedThreadPool(pop, task -> {
         Thread thread = new Thread(task, "Autism jump proof");
         thread.setDaemon(true);
         thread.setPriority(4);
         return thread;
      });
      private int tick;
      Set<ClassCache.Inner_ConfigStore> init_Set = Set.of();
      private boolean close_Z;
      private long load_J;
      private static final double init_D = 18.0;
      private boolean open_Z;

      static ClassCache.Inner_TextFormat init(
         ClassCache.Inner_StateCache grid, BlockPos start, ClassCache.Inner_JsonUtil goal, ClassCache.Inner_DataUtil caps, Set<Long> blacklist
      ) {
         return init(grid, start, goal, caps, blacklist, false);
      }

      static ClassCache.Inner_TextFormat init(
         ClassCache.Inner_StateCache grid,
         BlockPos start,
         ClassCache.Inner_JsonUtil goal,
         ClassCache.Inner_DataUtil caps,
         Set<Long> blacklist,
         boolean ordinaryJumps
      ) {
         return init(grid, start, goal, caps, blacklist, ordinaryJumps, true);
      }

      private static ClassCache.Inner_TextFormat init(
         ClassCache.Inner_StateCache grid,
         BlockPos start,
         ClassCache.Inner_JsonUtil goal,
         ClassCache.Inner_DataUtil caps,
         Set<Long> blacklist,
         boolean ordinaryJumps,
         boolean completeJumps
      ) {
         ClassCache.Inner_TextFormat search = new ClassCache.Inner_TextFormat(grid, start, goal, caps, blacklist);
         search.sync_Z = true;
         search.init_ClassCache_RenderS.load_Z = true;
         search.init_ClassCache_RenderS.read_Z = completeJumps;
         search.init_ClassCache_RenderS.write_Z = ordinaryJumps;
         if (completeJumps
            && !ordinaryJumps
            && !caps.allowDig()
            && !caps.allowBridge()
            && !caps.allowPillar()
            && (grid instanceof RingBuffer || grid instanceof ClassCache.Inner_StreamUtil)) {
            search.tick = pop;
         }

         if (ordinaryJumps) {
            search.init_t.init(2.0);
         }

         BlockPos target = goal.init_BlockPos();
         ClassCache.Inner_FileCache destination = target == null ? null : grid.init_ClassCache_FileCac_I(target.getX(), target.getY(), target.getZ());
         search.flush_Z = target != null && destination != null && (!destination.loaded() || destination.cached());
         if (target != null && destination == null) {
            search.read_Z = true;
         }

         if (ListPool.init_Z()) {
            ListPool.init(
               "route proof target=" + target + " unknown=" + search.flush_Z + " grid=" + grid.getClass().getSimpleName() + " destination=" + destination
            );
         }

         search.poll = target == null
            ? 512
            : (
               search.flush_Z
                  ? 2048
                  : (int)Math.min(
                     2048L, Math.max(128L, Math.max(Math.abs((long)target.getX() - start.getX()), Math.abs((long)target.getZ() - start.getZ())) + 64L)
                  )
            );
         search.push = target == null ? 512 : (int)Math.min(512L, Math.max(96L, Math.abs((long)target.getY() - start.getY()) + 32L));
         return search;
      }

      private boolean init_Z_BlockPos(BlockPos cell) {
         ClassCache.Inner_FileCache feet = this.init_ClassCache_RenderS.init_ClassCache_FileCac_BlockPos(cell);
         ClassCache.Inner_FileCache floor = this.init_ClassCache_RenderS.init_ClassCache_FileCac_BlockPos(cell.below());
         if (ClassCache.Inner_RenderState.init_Z_ClassCache_FileCac(feet, this.init_ClassCache_RenderS.init_ClassCache_FileCac_BlockPos(cell.above()), floor)
            && !feet.water()
            && !feet.climbable()
            && floor.top16() <= 16) {
            for (int[] direction : ClassCache.Inner_RenderState.init_IArr2) {
               ClassCache.Inner_FileCache neighbor = this.init_ClassCache_StateCa
                  .init_ClassCache_FileCac_I(cell.getX() + direction[0], cell.getY(), cell.getZ() + direction[1]);
               if (neighbor == null) {
                  this.read_Z = true;
               } else if (this.init_ClassCache_JsonUti.heuristic(cell.getX() + direction[0], cell.getY(), cell.getZ() + direction[1])
                  < this.init_ClassCache_JsonUti.heuristic(cell.getX(), cell.getY(), cell.getZ())) {
                  if (!neighbor.loaded() || neighbor.cached()) {
                     return true;
                  }

                  if (neighbor.passable() && neighbor.hazardFeet()) {
                     ClassCache.Inner_FileCache beyond = this.init_ClassCache_StateCa
                        .init_ClassCache_FileCac_I(cell.getX() + 2 * direction[0], cell.getY(), cell.getZ() + 2 * direction[1]);
                     if (beyond == null) {
                        this.read_Z = true;
                     } else if (!beyond.loaded() || beyond.cached()) {
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

      public Inner_TextFormat(
         ClassCache.Inner_StateCache grid, BlockPos start, ClassCache.Inner_JsonUtil goal, ClassCache.Inner_DataUtil caps, Set<Long> blacklist
      ) {
         this.init_ClassCache_StateCa = grid;
         this.init_BlockPos = start.immutable();
         this.init_ClassCache_JsonUti = goal.init(grid);
         this.init_ClassCache_DataUti = caps;
         this.load_Set = blacklist;
         this.init_J = grid.init_J();
         this.init_ClassCache_RenderS = new ClassCache.Inner_RenderState.Inner_CachePool(grid, 16384);
         this.init_ClassCache_RenderS.init_ClassCache_NetHelp = caps.fallPolicy();
         BlockPos target = goal.init_BlockPos();
         if (target != null && caps.allowPillar() && caps.strategy() == ClassCache.Inner_IoWorker.init) {
            int rise = target.getY() - start.getY();
            double across = Math.hypot(target.getX() - start.getX(), target.getZ() - start.getZ());
            if (rise >= 8 && across * 2.0 < rise && caps.constructionBlocks() >= rise) {
               this.init_t.init(1.3);
            }
         }

         if (caps.allowParkour()) {
            this.init_PriorityQueue = new PriorityQueue<>(
               Comparator.comparingDouble(ClassCache.Inner_TextFormat.Inner_StreamUtil::priority)
                  .thenComparingLong(ClassCache.Inner_TextFormat.Inner_StreamUtil::order)
            );
            this.init_ClassCache_RenderS.init_Consumer = this::init_V_ClassCache_RenderS;
         }

         this.init_t_DataUtil = new t.Inner_DataUtil(this.init_BlockPos, goal.heuristic(start.getX(), start.getY(), start.getZ()));
         this.init_t_DataUtil.load_D = 0.0;
         this.init_Long2ObjectOpenHas.put(this.init_BlockPos.asLong(), this.init_t_DataUtil);
         this.init_t.init(this.init_t_DataUtil);
         this.close_I = 1;
         Arrays.fill(this.load_DArr, 1.7976931348623157E308);
      }

      public boolean init_Z() {
         return this.init_t_DataUtil() != null;
      }

      private t.Inner_DataUtil init_t_DataUtil() {
         for (t.Inner_DataUtil n : this.init_t_DataUtilArr) {
            if (n != null && n.init_BlockPos.distSqr(this.init_BlockPos) > 25.0) {
               return n;
            }
         }

         return this.init_t_DataUtil != null
               && this.init_t_DataUtil.init_t_DataUtil != null
               && this.init_t_DataUtil.init_BlockPos.distSqr(this.init_BlockPos) > 25.0
            ? this.init_t_DataUtil
            : null;
      }

      private static double init_D_ClassCache_RenderS(ClassCache.Inner_RenderState.Inner_DataUtil candidate) {
         double dist = Math.hypot(candidate.to().getX() - candidate.from().getX(), candidate.to().getZ() - candidate.from().getZ());
         double rise = Math.max(0.0, candidate.targetY() - candidate.sourceY());
         return Math.min(18.0, 2.0 + Math.max(8.0, dist * 2.7 + rise * 2.0));
      }

      private void init_V_ClassCache_RenderS(ClassCache.Inner_RenderState.Inner_DataUtil candidate) {
         t.Inner_DataUtil source = this.init_ClassCache_RenderS.init_t_DataUtil;
         BlockPos to = candidate.to();
         BlockPos deeper = candidate.deeper();
         if (this.load_Set.contains(deeper.asLong())
            || !ClassCache.Inner_RenderState.init_Z_Function(this.init_ClassCache_RenderS, deeper, candidate.targetY())
            || Math.abs(
                  ClassCache.Inner_RenderState.init(deeper.getY(), this.init_ClassCache_RenderS.init_ClassCache_FileCac_BlockPos(deeper.below()))
                     - candidate.targetY()
               )
               >= 1.0E-6) {
            deeper = null;
         }

         double remaining = this.init_ClassCache_JsonUti.heuristic(to.getX(), to.getY(), to.getZ());
         if (deeper != null) {
            remaining = Math.min(remaining, this.init_ClassCache_JsonUti.heuristic(deeper.getX(), deeper.getY(), deeper.getZ()));
         }

         double priority = this.init_t.init(source.load_D + this.load(candidate), remaining);
         this.init_PriorityQueue.add(new ClassCache.Inner_TextFormat.Inner_StreamUtil(candidate, deeper, source, source.load_D, priority, this.load_J++));
      }

      private double load(ClassCache.Inner_RenderState.Inner_DataUtil candidate) {
         double flight = init_D_ClassCache_RenderS(candidate);
         if (this.init_ClassCache_RenderS.update_Z) {
            double rise = candidate.targetY() - candidate.sourceY();
            flight = Math.max(flight, 7.0 + Math.sqrt(Math.max(0.0, 1.25 - rise) * 22.0));
         }

         return flight;
      }

      private boolean init_Z_BlockPos(BlockPos destination, double maxCost, int blocksUsed) {
         for (t.Inner_DataUtil known = this.init_Long2ObjectOpenHas.get(destination.asLong()); known != null; known = known.load_t_DataUtil) {
            if (!known.init_Z && known.load_D <= maxCost && known.load_I <= blocksUsed) {
               return true;
            }
         }

         return false;
      }

      private boolean init_Z_t_DataUtil(t.Inner_DataUtil source, BlockPos destination, double lowerBound) {
         return this.init_Z_BlockPos(destination, lowerBound, source.load_I)
            ? true
            : this.init_ClassCache_RenderS.init_ClassCache_NetHelp == ClassCache.Inner_NetHelper.init_ClassCache_NetHelp
               && !this.init_ClassCache_RenderS.write_Z
               && this.init_Z_BlockPos(destination, lowerBound + 20.0, source.load_I)
               && ClassCache.Inner_RenderState.init_Z_Function_3(this.init_ClassCache_RenderS, source.init_BlockPos, destination);
      }

      private boolean load_Z() {
         if (this.init_PriorityQueue != null && !this.init_PriorityQueue.isEmpty() && !(this.init_PriorityQueue.peek().priority() > this.init_t.init_D())) {
            ClassCache.Inner_TextFormat.Inner_StreamUtil next = this.init_PriorityQueue.remove();
            t.Inner_DataUtil node = next.source();
            if (!node.init_Z && node.load_D == next.sourceCost()) {
               double comparableCost = node.load_D + (this.init_ClassCache_RenderS.write_Z ? this.load(next.candidate()) : 3.0);
               this.init_ClassCache_RenderS.init_I = 0;
               this.init_ClassCache_RenderS.init_ClassCache_UiHelpe = node.init_ClassCache_UiHelpe;
               this.init_ClassCache_RenderS.init_t_DataUtil = node;
               if (!this.init_Z_t_DataUtil(node, next.candidate().to(), comparableCost)
                  || next.continuation() != null && !this.init_Z_t_DataUtil(node, next.continuation(), comparableCost)) {
                  ClassCache.UiHelper edge = this.init_ClassCache_UiHelpe_ClassCache_TextFor(next);
                  if ((this.init_ClassCache_RenderS.init_I & 1) != 0) {
                     this.read_Z = true;
                  }

                  if (edge != null) {
                     this.flush_I++;
                     this.init(node, edge);
                  }

                  return true;
               } else {
                  this.init_V_ClassCache_TextFor(next);
                  if ((this.init_ClassCache_RenderS.init_I & 1) != 0) {
                     this.read_Z = true;
                  }

                  return true;
               }
            } else {
               this.init_V_ClassCache_TextFor(next);
               return true;
            }
         } else {
            return false;
         }
      }

      private ClassCache.UiHelper init_ClassCache_UiHelpe_ClassCache_TextFor(ClassCache.Inner_TextFormat.Inner_StreamUtil next) {
         if (this.tick >= 2 && !this.flush_Z && !this.init_ClassCache_RenderS.load_Z()) {
            Future<ClassCache.Inner_TextFormat.Inner_DataUtil> required = this.init_Map.computeIfAbsent(next, this::init_Future_ClassCache_TextFor);
            if (this.init_Map.size() < this.tick * 2) {
               PriorityQueue<ClassCache.Inner_TextFormat.Inner_StreamUtil> queued = new PriorityQueue<>(this.init_PriorityQueue);

               while (!queued.isEmpty() && this.init_Map.size() < this.tick * 2) {
                  ClassCache.Inner_TextFormat.Inner_StreamUtil candidate = queued.remove();
                  t.Inner_DataUtil source = candidate.source();
                  if (!source.init_Z
                     && source.load_D == candidate.sourceCost()
                     && (source.init_ClassCache_UiHelpe == null || source.init_ClassCache_UiHelpe.type() != ClassCache.Inner_SyncManager.set)) {
                     this.init_Map.computeIfAbsent(candidate, this::init_Future_ClassCache_TextFor);
                  }
               }
            }

            try {
               ClassCache.Inner_TextFormat.Inner_DataUtil proof = required.get();
               this.init_ClassCache_RenderS.init_I = this.init_ClassCache_RenderS.init_I | proof.flags();
               this.init_ClassCache_RenderS.read_I = this.init_ClassCache_RenderS.read_I + proof.solves();
               this.init_ClassCache_RenderS.write_I = this.init_ClassCache_RenderS.write_I + proof.hits();
               this.init_ClassCache_RenderS.load_J = this.init_ClassCache_RenderS.load_J + proof.nanos();
               this.init_ClassCache_RenderS.init_J = this.init_ClassCache_RenderS.init_J + proof.queries();
               return proof.movement();
            } catch (InterruptedException e) {
               required.cancel(true);
               Thread.currentThread().interrupt();
               this.init_Z = true;
               return null;
            } catch (ExecutionException e) {
               return ClassCache.Inner_RenderState.init_ClassCache_UiHelpe_ClassCache_RenderS(this.init_ClassCache_RenderS, this.load_Set, next.candidate());
            } finally {
               this.init_Map.remove(next);
            }
         } else {
            return ClassCache.Inner_RenderState.init_ClassCache_UiHelpe_ClassCache_RenderS(this.init_ClassCache_RenderS, this.load_Set, next.candidate());
         }
      }

      private Future<ClassCache.Inner_TextFormat.Inner_DataUtil> init_Future_ClassCache_TextFor(ClassCache.Inner_TextFormat.Inner_StreamUtil jump) {
         ClassCache.UiHelper incoming = jump.source().init_ClassCache_UiHelpe;
         return init_ExecutorService.submit(() -> {
            ClassCache.Inner_RenderState.Inner_CachePool worker = this.init_ConcurrentLinkedQu.poll();
            if (worker == null) {
               worker = new ClassCache.Inner_RenderState.Inner_CachePool(this.init_ClassCache_StateCa, 16384);
               worker.load_Z = true;
               worker.read_Z = true;
               worker.sync_Z = this.init_ClassCache_RenderS.sync_Z;
               worker.init_ClassCache_NetHelp = this.init_ClassCache_RenderS.init_ClassCache_NetHelp;
            }

            worker.init_ClassCache_UiHelpe = incoming;
            worker.init_I = worker.read_I = worker.write_I = 0;
            worker.load_J = worker.init_J = 0L;

            try {
               ClassCache.UiHelper movement = ClassCache.Inner_RenderState.init_ClassCache_UiHelpe_ClassCache_RenderS(worker, this.load_Set, jump.candidate());
               return new ClassCache.Inner_TextFormat.Inner_DataUtil(movement, worker.init_I, worker.read_I, worker.write_I, worker.load_J, worker.init_J);
            } finally {
               if (!Thread.currentThread().isInterrupted()) {
                  this.init_ConcurrentLinkedQu.offer(worker);
               }
            }
         });
      }

      private void init_V_ClassCache_TextFor(ClassCache.Inner_TextFormat.Inner_StreamUtil jump) {
         Future<ClassCache.Inner_TextFormat.Inner_DataUtil> proof = this.init_Map.remove(jump);
         if (proof != null) {
            proof.cancel(true);
         }
      }

      private void load_V() {
         for (Future<ClassCache.Inner_TextFormat.Inner_DataUtil> proof : this.init_Map.values()) {
            proof.cancel(true);
         }

         this.init_Map.clear();
      }

      public boolean init_Z_I(int budget) {
         if (budget <= 0) {
            throw new IllegalArgumentException("Expansion budget must be positive");
         }

         for (int i = 0; i < budget && !this.init_Z; i++) {
            if ((i & 63) == 0) {
               if (Thread.currentThread().isInterrupted()) {
                  this.init_Z = true;
                  break;
               }

               if (this.init_ClassCache_StateCa.init_J() != this.init_J) {
                  this.read_Z = true;
                  this.init_Z = true;
                  break;
               }
            }

            boolean hasUnprovedJumps = this.init_PriorityQueue != null && !this.init_PriorityQueue.isEmpty();
            if (!hasUnprovedJumps && this.init_t.init_Z()
               || this.sync_I >= 2000000
               || this.open_I + (hasUnprovedJumps ? this.init_PriorityQueue.size() : 0) >= 3000000) {
               this.open_Z = this.open_Z | (hasUnprovedJumps || !this.init_t.init_Z());
               this.init_Z = true;
               break;
            }

            if (!this.load_Z()) {
               t.Inner_DataUtil node = this.init_t.init_t_DataUtil();
               if (!node.init_Z) {
                  this.sync_I++;
                  BlockPos cell = node.init_BlockPos;
                  if (node.init_D < this.init_t_DataUtil.init_D) {
                     this.init_t_DataUtil = node;
                  }

                  this.init_ClassCache_RenderS.init_ClassCache_UiHelpe = node.init_ClassCache_UiHelpe;
                  this.init_ClassCache_RenderS.init_t_DataUtil = node;
                  if (this.init_ClassCache_JsonUti.init_Z_I(cell.getX(), cell.getY(), cell.getZ(), this.init_ClassCache_RenderS)) {
                     this.init_t_DataUtil = node;
                     this.load_Z = true;
                     this.init_Z = true;
                     break;
                  }

                  if (this.sync_Z
                     && this.flush_Z
                     && (
                        cell.equals(this.init_BlockPos)
                           || node.init_D
                              < this.init_ClassCache_JsonUti.heuristic(this.init_BlockPos.getX(), this.init_BlockPos.getY(), this.init_BlockPos.getZ())
                     )
                     && this.init_Z_BlockPos(cell)) {
                     this.load_t_DataUtil = node;
                     this.init_Z = true;
                     break;
                  }

                  if (!this.sync_Z
                     && this.init_ClassCache_JsonUti.outsideSearchBox(this.init_BlockPos, 64, 40)
                     && node.init_D < this.init_ClassCache_JsonUti.heuristic(this.init_BlockPos.getX(), this.init_BlockPos.getY(), this.init_BlockPos.getZ())
                     && (
                        Math.abs(cell.getX() - this.init_BlockPos.getX()) == 64
                           || Math.abs(cell.getZ() - this.init_BlockPos.getZ()) == 64
                           || Math.abs(cell.getY() - this.init_BlockPos.getY()) == 40
                     )) {
                     this.init_t_DataUtil = node;
                     this.init_Z = true;
                     break;
                  }

                  this.init_List.clear();
                  int moveFlags = ClassCache.Inner_RenderState.init_I_ClassCache_RenderS(
                     this.init_ClassCache_RenderS, this.init_ClassCache_DataUti, this.load_Set, cell, this.init_List
                  );
                  if ((moveFlags & 1) != 0) {
                     this.read_Z = true;
                  }

                  if ((moveFlags & 2) != 0 && cell.equals(this.init_BlockPos)) {
                     this.write_Z = true;
                  }

                  if ((moveFlags & 4) != 0 && cell.equals(this.init_BlockPos)) {
                     this.update_Z = true;
                  }

                  this.flush_I = this.flush_I + this.init_List.size();

                  for (ClassCache.UiHelper edge : this.init_List) {
                     this.init(node, edge);
                  }
               }
            }
         }

         if (this.init_Z) {
            this.load_V();
         }

         return this.init_Z;
      }

      private void init(t.Inner_DataUtil node, ClassCache.UiHelper edge) {
         if ((this.init_Set.isEmpty() || !this.init_Set.contains(ClassCache.Inner_ConfigStore.of(edge))) && (this.close_Z || !ClassCache.init_Z(edge))) {
            boolean protectedCell = false;

            for (BlockPos broken : edge.cellsToBreak()) {
               if (!this.init_ClassCache_JsonUti.load(broken)) {
                  protectedCell = true;
                  break;
               }
            }

            for (BlockPos placed : edge.cellsToPlace()) {
               if (!this.init_ClassCache_JsonUti.read(placed)) {
                  protectedCell = true;
                  break;
               }
            }

            if (!protectedCell) {
               BlockPos t = edge.to();
               if (this.init_ClassCache_JsonUti.init_Z(t)) {
                  if (Math.abs((long)t.getX() - this.init_BlockPos.getX()) <= this.poll
                     && Math.abs((long)t.getZ() - this.init_BlockPos.getZ()) <= this.poll
                     && Math.abs((long)t.getY() - this.init_BlockPos.getY()) <= this.push) {
                     double extra = (
                           this.init_ClassCache_DataUti.strategy() == ClassCache.Inner_IoWorker.load
                              ? this.init_ClassCache_DataUti.threats().surcharge(t.getX(), t.getY(), t.getZ())
                                 + this.init_ClassCache_DataUti
                                    .threats()
                                    .init(edge.from().getX(), edge.from().getY(), edge.from().getZ(), t.getX(), t.getY(), t.getZ())
                              : 0.0
                        )
                        + this.init_ClassCache_DataUti.threats().init(t.getX(), t.getY(), t.getZ());
                     double cost = node.load_D
                        + edge.costTicks()
                        + extra
                        + edge.cellsToPlace().size() * this.init_ClassCache_JsonUti.init_D()
                        + ClassCache.Inner_CachePool.init_D_List(edge.cellsToPlace());

                     for (BlockPos placed : edge.cellsToPlace()) {
                        if (FileCache.read_Z(placed)) {
                           cost += 200.0;
                        }
                     }

                     for (BlockPos dug : edge.cellsToBreak()) {
                        if (FileCache.load_Z(dug)) {
                           cost += 60.0;
                        }

                        if (this.init_ClassCache_JsonUti.write(dug)) {
                           double ticks = this.init_ClassCache_RenderS.init_ClassCache_FileCac_BlockPos(dug).breakTicks();
                           if (Double.isFinite(ticks)) {
                              cost -= ticks;
                           }
                        }
                     }

                     if (cost < node.load_D + 1.0) {
                        cost = node.load_D + 1.0;
                     }

                     if (Double.isFinite(cost) && !(edge.costTicks() <= 0.0) && !(extra < 0.0)) {
                        int used = node.load_I + edge.cellsToPlace().size();
                        if (used <= this.init_ClassCache_DataUti.constructionBlocks()) {
                           t.Inner_DataUtil head = this.init_Long2ObjectOpenHas.get(t.asLong());
                           t.Inner_DataUtil next = null;
                           boolean dominated = false;

                           for (t.Inner_DataUtil label = head; label != null; label = label.load_t_DataUtil) {
                              if (!label.init_Z) {
                                 if (label.load_I <= used && label.load_D <= cost) {
                                    dominated = true;
                                    break;
                                 }

                                 if (label.load_I == used) {
                                    next = label;
                                 }
                              }
                           }

                           if (!dominated) {
                              if (next == null) {
                                 next = new t.Inner_DataUtil(t, this.init_ClassCache_JsonUti.heuristic(t.getX(), t.getY(), t.getZ()));
                                 next.load_I = used;
                                 next.load_t_DataUtil = head;
                                 this.init_Long2ObjectOpenHas.put(t.asLong(), next);
                                 this.open_I++;
                              }

                              for (t.Inner_DataUtil label = head; label != null; label = label.load_t_DataUtil) {
                                 if (label != next && label.load_I >= used && label.load_D >= cost) {
                                    label.init_Z = true;
                                 }
                              }

                              if (cost < next.load_D) {
                                 next.load_D = cost;
                                 next.init_t_DataUtil = node;
                                 next.init_ClassCache_UiHelpe = edge;
                                 this.init_t.init(next);
                                 this.close_I = Math.max(this.close_I, this.init_t.init_I());

                                 for (int c = 0; c < init_DArr.length; c++) {
                                    double v = next.init_D + cost / init_DArr[c];
                                    if (v < this.load_DArr[c]) {
                                       this.load_DArr[c] = v;
                                       this.init_t_DataUtilArr[c] = next;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  } else if (this.sync_Z) {
                     this.open_Z = true;
                  }
               }
            }
         }
      }

      public ClassCache.Inner_PacketUtil init_ClassCache_PacketU() {
         if (!this.init_Z) {
            throw new IllegalStateException("Search still running");
         }

         if (this.sync_Z && !this.load_Z && this.load_t_DataUtil == null) {
            if (ListPool.init_Z()) {
               ListPool.init("unproved route nearest=" + this.init_t_DataUtil.init_BlockPos + " unknown=" + this.flush_Z + " pending=" + this.read_Z);
            }

            return new ClassCache.Inner_PacketUtil(
               List.of(), false, this.sync_I, this.read_Z, this.open_Z || this.init_ClassCache_RenderS.init_Z(), this.write_Z, this.update_Z
            );
         } else {
            ArrayDeque<ClassCache.UiHelper> reversed = new ArrayDeque<>();
            t.Inner_DataUtil cursor = this.sync_Z && !this.load_Z ? this.load_t_DataUtil : this.init_t_DataUtil;
            if (!this.sync_Z && !this.load_Z && cursor.init_BlockPos.distSqr(this.init_BlockPos) <= 25.0) {
               t.Inner_DataUtil partial = this.init_t_DataUtil();
               if (partial != null) {
                  cursor = partial;
               }
            }

            while (cursor.init_t_DataUtil != null) {
               if (reversed.size() >= this.open_I) {
                  throw new IllegalStateException("Cyclic path parent chain");
               }

               reversed.addFirst(cursor.init_ClassCache_UiHelpe);
               cursor = cursor.init_t_DataUtil;
            }

            return new ClassCache.Inner_PacketUtil(
               new ArrayList<>(reversed),
               this.load_Z,
               this.sync_I,
               this.read_Z,
               this.open_Z || !this.load_Z && this.init_ClassCache_RenderS.init_Z(),
               this.write_Z,
               this.update_Z,
               this.sync_Z && !this.load_Z && this.load_t_DataUtil != null && this.load_t_DataUtil.init_BlockPos.equals(this.init_BlockPos)
            );
         }
      }

      List<ClassCache.UiHelper> init_List() {
         ArrayDeque<ClassCache.UiHelper> trail = new ArrayDeque<>();

         for (t.Inner_DataUtil node = this.init_t_DataUtil; node.init_t_DataUtil != null && trail.size() < 64; node = node.init_t_DataUtil) {
            trail.addFirst(node.init_ClassCache_UiHelpe);
         }

         return List.copyOf(trail);
      }

      public void init_V() {
         this.open_Z = !this.init_Z;
         this.init_Z = true;
         this.load_V();
      }

      public int init_I() {
         return this.sync_I;
      }

      public long init_J() {
         return this.init_ClassCache_RenderS.load_J;
      }

      public int load_I() {
         return this.init_ClassCache_RenderS.read_I;
      }

      public int read() {
         return this.flush_I;
      }

      public int write() {
         return this.close_I;
      }

      public long load_J() {
         return this.init_ClassCache_RenderS.init_J;
      }

      private record Inner_DataUtil(ClassCache.UiHelper movement, int flags, int solves, int hits, long nanos, long queries) {
      }

      private record Inner_StreamUtil(
         ClassCache.Inner_RenderState.Inner_DataUtil candidate, BlockPos continuation, t.Inner_DataUtil source, double sourceCost, double priority, long order
      ) {
      }
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
      boolean repairFarmland
   ) {
      public UiHelper(
         ClassCache.Inner_SyncManager type,
         BlockPos from,
         BlockPos to,
         List<BlockPos> cellsToBreak,
         List<BlockPos> cellsToPlace,
         double costTicks,
         double plannedBreakTicks,
         TableIndex.Inner_StateCache jump
      ) {
         this(type, from, to, cellsToBreak, cellsToPlace, costTicks, plannedBreakTicks, jump, false);
      }

      public UiHelper {
         cellsToBreak = List.copyOf(cellsToBreak);
         cellsToPlace = List.copyOf(cellsToPlace);
      }

      public UiHelper(
         ClassCache.Inner_SyncManager type,
         BlockPos from,
         BlockPos to,
         List<BlockPos> cellsToBreak,
         List<BlockPos> cellsToPlace,
         double costTicks,
         double plannedBreakTicks
      ) {
         this(type, from, to, cellsToBreak, cellsToPlace, costTicks, plannedBreakTicks, null);
      }

      UiHelper(ClassCache.Inner_SyncManager type, BlockPos from, BlockPos to, List<BlockPos> cellsToBreak, List<BlockPos> cellsToPlace, double costTicks) {
         this(type, from, to, cellsToBreak, cellsToPlace, costTicks, -1.0);
      }

      static ClassCache.UiHelper travel(ClassCache.Inner_SyncManager type, BlockPos from, BlockPos to, double cost) {
         return new ClassCache.UiHelper(type, from, to, List.of(), List.of(), cost);
      }

      static ClassCache.UiHelper jump(BlockPos from, BlockPos to, double cost, TableIndex.Inner_StateCache plan) {
         return new ClassCache.UiHelper(ClassCache.Inner_SyncManager.start, from, to, List.of(), List.of(), cost, -1.0, plan);
      }
   }
}
