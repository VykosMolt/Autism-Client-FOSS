package autismclient.ai;

import autismclient.AutismClientAddon;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.LongPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiBuildSite {
   private static final int init_I = 21;
   private static final int load_I = 1048576;
   private static final long init_J = 2097151L;
   private static final int read_I = 0;
   private static final int write_I = 21;
   private static final int update_I = 42;
   private static final int sync_I = 4096;
   private static final int flush_I = 4095;
   private static final int close_I = 4;
   private static final int open = 4096;
   private static final int poll = 8192;
   private static final int push = 8;
   private static final AiBuildSite.Inner_CachePool[] init_IoWorker_CachePoolArr = AiBuildSite.Inner_CachePool.values();
   private static final Direction[] init_DirectionArr = Direction.values();
   private static final int[] init_IArr = new int[0];
   private final AiBuilder.Inner_DataUtil init_TableIndex_DataUti;
   private final Map<String, List<String>> init_Map;
   private final LevelReader init_LevelReader;
   private final int pop;
   private final BlockPos init_BlockPos;
   private final int tick;
   private final int run;
   private final int get;
   private final AiBuilder.Inner_StreamUtil[] init_TableIndex_StreamUArr;
   private final long[] init_JArr;
   private final long[] load_JArr;
   private final byte[] init_BArr;
   private final Item[] init_ItemArr;
   private final byte[] load_BArr;
   private final int[] load_IArr;
   private final BitSet init_BitSet;
   private final BitSet load_BitSet;
   private final long[] read_JArr;
   private final int[] read_IArr;
   private final BitSet read_BitSet;
   private int set;
   private final long[] write_JArr;
   private final int[] write_IArr;
   private final int[] update_IArr;
   private final int[] sync_IArr;
   private final Map<Long, int[]> load_Map;
   private final BitSet write_BitSet;
   private final BitSet update_BitSet;
   private final BitSet sync_BitSet;
   private final BitSet flush_BitSet;
   private final int[] flush_IArr;
   private int put;
   private int add;
   private int clear;
   private Map<Integer, String> read_Map;
   private List<Long> init_List;
   private Set<Long> init_Set;
   private Set<Long> load_Set;
   private int[] close_IArr = init_IArr;
   private int reset;
   private int start;
   private int stop = -1;
   private int next = -1;
   private Map<String, Integer> write_Map = Map.of();
   private int prev = 8;
   private static final double init_D = 8.0;
   private static final BlockPos[] init_BlockPosArr = new BlockPos[0];

   AiBuildSite(AiBuilder.Inner_DataUtil blueprint, Map<String, List<String>> substitutes) {
      this(blueprint, substitutes, null);
   }

   AiBuildSite(AiBuilder.Inner_DataUtil blueprint, Map<String, List<String>> substitutes, LevelReader world) {
      if (blueprint == null) {
         throw new IllegalArgumentException("A build site needs a blueprint");
      }

      this.init_TableIndex_DataUti = blueprint;
      this.init_Map = substitutes == null ? Map.of() : substitutes;
      this.init_LevelReader = world;
      List<AiBuilder.Inner_StreamUtil> input = blueprint.cells();
      this.pop = input.size();
      this.init_BlockPos = blueprint.origin();
      this.tick = this.init_BlockPos.getX();
      this.run = this.init_BlockPos.getY();
      this.get = this.init_BlockPos.getZ();
      this.init_TableIndex_StreamUArr = new AiBuilder.Inner_StreamUtil[this.pop];
      this.init_JArr = new long[this.pop];
      this.load_JArr = new long[this.pop];
      this.init_BArr = new byte[this.pop];
      this.init_ItemArr = new Item[this.pop];
      this.load_BArr = new byte[this.pop];
      this.load_IArr = new int[this.pop];
      this.write_BitSet = new BitSet(this.pop);
      this.update_BitSet = new BitSet(this.pop);
      this.sync_BitSet = new BitSet(this.pop);
      this.flush_BitSet = new BitSet(this.pop);
      this.init_BitSet = new BitSet(this.pop);
      this.read_BitSet = new BitSet(this.pop);
      HashMap<String, Map<Map<String, String>, AiBuildSite.Inner_DataUtil>> classified = new HashMap<>();
      AiBuildSite.Inner_StreamUtil observedIndices = new AiBuildSite.Inner_StreamUtil();
      int classificationFailures = 0;

      for (int i = 0; i < this.pop; i++) {
         AiBuilder.Inner_StreamUtil cell = input.get(i);
         this.init_TableIndex_StreamUArr[i] = cell;
         this.init_JArr[i] = load_J_I(cell.dx(), cell.dy(), cell.dz());
         this.load_JArr[i] = BlockPos.asLong(this.tick + cell.dx(), this.run + cell.dy(), this.get + cell.dz());
         AiBuildSite.Inner_DataUtil entry = classified.computeIfAbsent(cell.block(), ignored -> new HashMap<>()).get(cell.properties());
         if (entry == null) {
            boolean standable = false;

            AiBuildSite.Inner_CachePool kind;
            Item item;
            byte dependencies;
            try {
               kind = AiBuildIntent.init_IoWorker_CachePool(cell);
               item = AiBuildIntent.init_Item(cell, kind);
               dependencies = init_B_TableIndex_StreamU_IoWorker_CachePool(cell, kind);
               standable = init_Z_TableIndex_StreamU(cell);
            } catch (RuntimeException unknown) {
               if (classificationFailures++ == 0) {
                  AutismClientAddon.LOG.warn("[AI] build site could not classify {}, treating it as SKIP", cell, unknown);
               }

               kind = AiBuildSite.Inner_CachePool.SKIP;
               item = Items.AIR;
               dependencies = 0;
            }

            entry = new AiBuildSite.Inner_DataUtil((byte)kind.ordinal(), item, dependencies, standable);
            classified.get(cell.block()).put(cell.properties(), entry);
         }

         this.init_BArr[i] = entry.kindOrdinal();
         this.init_ItemArr[i] = entry.item();
         this.load_BArr[i] = entry.dependencies();
         if (entry.standable()) {
            this.init_BitSet.set(i);
         }

         boolean placeable = this.init_BArr[i] != (byte)AiBuildSite.Inner_CachePool.DERIVED.ordinal()
            && this.init_BArr[i] != (byte)AiBuildSite.Inner_CachePool.SKIP.ordinal();
         if (placeable) {
            this.write_BitSet.set(i);
            this.put++;
            if (AiBuilder.load_Z(cell)) {
               this.flush_BitSet.set(i);
               observedIndices.init(i);
            }
         }
      }

      this.flush_IArr = observedIndices.init_IArr();
      int[] byPosition = new int[this.pop];
      int i = 0;

      while (i < this.pop) {
         byPosition[i] = i++;
      }

      this.init_V_IArr(byPosition);
      this.write_IArr = byPosition;
      this.write_JArr = new long[this.pop];

      for (int rank = 0; rank < this.pop; rank++) {
         this.write_JArr[rank] = this.load_JArr[byPosition[rank]];
      }

      this.load_BitSet = new BitSet(this.pop);

      for (int ix = 0; ix < this.pop; ix++) {
         if (this.init_BitSet.get(ix) && this.run(ix)) {
            this.load_BitSet.set(ix);
         }
      }

      i = this.load_BitSet.cardinality();
      this.read_JArr = new long[i];
      int at = 0;

      for (int rank = 0; rank < this.pop && at < i; rank++) {
         int index = this.write_IArr[rank];
         if (this.load_BitSet.get(index)) {
            this.read_JArr[at++] = this.load_JArr[index];
         }
      }

      this.read_V();
      this.load_Map = this.load_Map();
      int[] ordered = this.read_IArr();
      this.update_IArr = this.init_IArr_IArr(ordered);
      this.sync_IArr = new int[this.pop];
      int rank = 0;

      while (rank < this.pop) {
         this.sync_IArr[this.update_IArr[rank]] = rank++;
      }

      this.read_IArr = new int[i];
      rank = 0;

      for (int rankx = 0; rankx < this.pop && rank < i; rankx++) {
         int index = this.update_IArr[rankx];
         if (this.load_BitSet.get(index)) {
            this.read_IArr[rank++] = index;
         }
      }

      this.init_V();
   }

   static boolean init_Z_TableIndex_StreamU(AiBuilder.Inner_StreamUtil cell) {
      if (cell == null) {
         return false;
      }

      BlockState state;
      try {
         state = AiBuilder.init_BlockState(cell);
      } catch (RuntimeException unknown) {
         return false;
      }

      if (!state.getFluidState().isEmpty()) {
         return false;
      }

      if (autismclient.ai.engine.AiPathfinder.Inner_StreamUtil.read(state)) {
         return false;
      }

      VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
      if (shape.isEmpty()) {
         return false;
      }

      for (AABB box : shape.toAabbs()) {
         if (!(Math.abs(box.maxY - 1.0) > 0.001) && box.minX <= 0.501 && box.maxX >= 0.499 && box.minZ <= 0.501 && box.maxZ >= 0.499) {
            return true;
         }
      }

      return false;
   }

   int init_I() {
      return this.pop;
   }

   long init_J_I(int index) {
      return this.load_JArr[index];
   }

   BlockPos init_BlockPos_I(int index) {
      return BlockPos.of(this.load_JArr[index]);
   }

   AiBuilder.Inner_StreamUtil init_TableIndex_StreamU_I(int index) {
      return this.init_TableIndex_StreamUArr[index];
   }

   AiBuildSite.Inner_CachePool init_IoWorker_CachePool_I(int index) {
      return init_IoWorker_CachePoolArr[this.init_BArr[index] & 0xFF];
   }

   Item init_Item_I(int index) {
      return this.init_ItemArr[index];
   }

   int init_I_I(int index) {
      return read_I_J(this.init_JArr[index]);
   }

   int load_I_I(int index) {
      return this.sync_IArr[index];
   }

   BlockPos init_BlockPos() {
      return this.init_BlockPos;
   }

   AiBuilder.Inner_DataUtil init_TableIndex_DataUti() {
      return this.init_TableIndex_DataUti;
   }

   Map<String, List<String>> init_Map() {
      return this.init_Map;
   }

   int init_I_BlockPos(BlockPos position) {
      return position == null ? -1 : this.init_I_J(position.asLong());
   }

   int init_I_J(long positionKey) {
      int low = 0;
      int high = this.pop - 1;

      while (low <= high) {
         int middle = low + high >>> 1;
         long key = this.write_JArr[middle];
         if (key < positionKey) {
            low = middle + 1;
         } else {
            if (key <= positionKey) {
               return this.write_IArr[middle];
            }

            high = middle - 1;
         }
      }

      return -1;
   }

   int read_I_I(int rank) {
      return this.update_IArr[rank];
   }

   int write_I_I(int index) {
      return this.load_IArr[index];
   }

   AiBuilder.Inner_StreamUtil load_TableIndex_StreamU_I(int index) {
      return AiBuildIntent.init_TableIndex_StreamU(this.init_TableIndex_StreamUArr[index]);
   }

   boolean init_Z_I(int index) {
      return AiBuildIntent.read_Z(this.init_TableIndex_StreamUArr[index]);
   }

   boolean load_Z_I(int index) {
      return AiBuilder.init_Z(this.init_TableIndex_StreamUArr[index]);
   }

   int update_I(int index) {
      return this.flush_I(index);
   }

   boolean read_Z_I(int index) {
      return this.flush_I(index) >= 0;
   }

   boolean write_Z_I(int index) {
      AiBuildSite.Inner_CachePool kind = this.init_IoWorker_CachePool_I(index);
      return kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP && !this.flush_BitSet.get(index);
   }

   boolean update_Z(int index) {
      return this.flush_BitSet.get(index);
   }

   int[] init_IArr() {
      return this.flush_IArr;
   }

   int load_I() {
      if (this.flush_IArr.length == 0) {
         return 0;
      }

      LevelReader level = this.init_LevelReader();
      if (level == null) {
         return 0;
      }

      int reconciled = 0;

      for (int index : this.flush_IArr) {
         if (!this.update_BitSet.get(index)) {
            BlockPos at = this.init_BlockPos_I(index);
            if (read(level, at) && this.init_Z_I_BlockState(index, level.getBlockState(at))) {
               this.init_V_I(index);
               reconciled++;
            }
         }
      }

      return reconciled;
   }

   int[] load_IArr() {
      if (this.close_IArr.length == 0) {
         AiBuildSite.Inner_StreamUtil found = new AiBuildSite.Inner_StreamUtil();

         for (int i = 0; i < this.pop; i++) {
            if (AiBuildIntent.read_Z(this.init_TableIndex_StreamUArr[i])) {
               found.init(i);
            }
         }

         this.close_IArr = found.init_IArr();
      }

      return this.close_IArr;
   }

   boolean sync_Z(int index) {
      LevelReader level = this.init_LevelReader();
      if (level == null) {
         return this.update_BitSet.get(index);
      }

      BlockPos at = this.init_BlockPos_I(index);
      return read(level, at) && AiBuildIntent.init(this.init_TableIndex_StreamUArr[index], level.getBlockState(at));
   }

   int read_I() {
      if (this.flush_IArr.length > 0 && --this.prev <= 0) {
         this.prev = 8;
         this.load_I();
      }

      return this.put;
   }

   int write() {
      return this.add;
   }

   int update() {
      return this.clear;
   }

   String init_Str_I(int index) {
      return this.read_Map == null ? null : this.read_Map.get(index);
   }

   private void init_V() {
      while (this.reset < this.pop && !this.write_BitSet.get(this.update_IArr[this.reset])) {
         this.reset++;
      }
   }

   boolean flush_Z(int index) {
      return this.write_BitSet.get(index);
   }

   boolean close_Z(int index) {
      return this.update_BitSet.get(index);
   }

   boolean open_Z_I(int index) {
      return this.sync_BitSet.get(index);
   }

   boolean init_Z_J(long posKey) {
      return this.init_I_J(posKey) >= 0;
   }

   LongPredicate init_LongPredicate() {
      return this::init_Z_J;
   }

   long[] init_JArr() {
      return this.write_JArr;
   }

   void init_V_I(int index) {
      if (index >= 0 && index < this.pop) {
         boolean wasPending = this.write_BitSet.get(index);
         boolean wasFailed = this.sync_BitSet.get(index);
         this.write_BitSet.clear(index);
         this.sync_BitSet.clear(index);
         if (wasPending) {
            this.put--;
         }

         if (wasFailed) {
            this.clear--;
         }

         if (!this.update_BitSet.get(index)) {
            this.update_BitSet.set(index);
            this.add++;
         }

         if (this.read_Map != null) {
            this.read_Map.remove(index);
         }

         this.start++;
         this.write_V_I(index);
         int partner = this.close_I(index);
         if (partner >= 0 && !this.update_BitSet.get(partner)) {
            this.poll_Z_I(partner);
         }
      }
   }

   void load_V_I(int index) {
      if (index >= 0 && index < this.pop) {
         if (this.update_BitSet.get(index)) {
            this.update_BitSet.clear(index);
            this.add--;
         }

         if (this.sync_BitSet.get(index)) {
            this.sync_BitSet.clear(index);
            this.clear--;
         }

         if (!this.write_BitSet.get(index)) {
            this.write_BitSet.set(index);
            this.put++;
            this.reset = Math.min(this.reset, this.sync_IArr[index]);
         }

         this.start++;
      }
   }

   void init_V_I_Str(int index, String reason) {
      if (index >= 0 && index < this.pop) {
         if (this.update_BitSet.get(index)) {
            this.update_BitSet.clear(index);
            this.add--;
         }

         if (this.write_BitSet.get(index)) {
            this.write_BitSet.clear(index);
            this.put--;
         }

         if (!this.sync_BitSet.get(index)) {
            this.sync_BitSet.set(index);
            this.clear++;
         }

         if (this.read_Map == null) {
            this.read_Map = new LinkedHashMap<>();
         }

         this.read_Map.put(index, reason == null ? "failed" : reason);
         this.start++;
         this.init_V();
      }
   }

   void read_V_I(int index) {
      this.load_V_I(index);
   }

   int sync() {
      LevelReader level = this.init_LevelReader();
      if (level == null) {
         return 0;
      }

      int adopted = 0;

      for (int i = 0; i < this.pop; i++) {
         if (!this.update_BitSet.get(i)) {
            BlockPos at = this.init_BlockPos_I(i);
            if (read(level, at) && this.init_Z_I_BlockState(i, level.getBlockState(at))) {
               this.init_V_I(i);
               adopted++;
            }
         }
      }

      return adopted;
   }

   boolean poll_Z_I(int index) {
      LevelReader level = this.init_LevelReader();
      if (level == null) {
         return this.update_BitSet.get(index);
      }

      BlockPos at = this.init_BlockPos_I(index);
      if (read(level, at) && this.init_Z_I_BlockState(index, level.getBlockState(at))) {
         this.init_V_I(index);
         return true;
      }

      if (this.update_BitSet.get(index)) {
         this.read_V_I(index);
      }

      return false;
   }

   boolean init_Z_BlockPos(BlockPos pos) {
      LevelReader level = this.init_LevelReader();
      return level != null && pos != null && read(level, pos) ? !level.getBlockState(pos).isAir() : false;
   }

   boolean push(int index) {
      return this.init_Z_I_LevelReader(index, this.init_LevelReader());
   }

   int[] init_IArr_Vec3(Vec3 eye, double radius, int max) {
      if (eye != null && max > 0 && this.pop != 0) {
         int[] near = this.init_IArr_Vec3_D(eye, radius);
         if (near.length == 0) {
            return init_IArr;
         }

         this.init_V_IArr_I(near, near.length);
         LevelReader level = this.init_LevelReader();
         int[] answer = new int[Math.min(max, near.length)];
         int count = 0;

         for (int index : near) {
            if (this.init_Z_I_LevelReader(index, level)) {
               answer[count++] = index;
               if (count == max) {
                  break;
               }
            }
         }

         return count == answer.length ? answer : (count == 0 ? init_IArr : Arrays.copyOf(answer, count));
      } else {
         return init_IArr;
      }
   }

   int[] load_IArr_Vec3(Vec3 center, double radius, int max) {
      if (center != null && max > 0 && this.pop != 0) {
         int[] near = this.init_IArr_Vec3_D(center, radius);
         if (near.length == 0) {
            return init_IArr;
         }

         this.init_V_IArr_I(near, near.length);
         return near.length <= max ? near : Arrays.copyOf(near, max);
      } else {
         return init_IArr;
      }
   }

   Map<String, Integer> init_Map_I(int max) {
      if (max <= 0 || this.pop == 0) {
         return Map.of();
      }

      if (this.stop == this.start && this.next == max) {
         return this.write_Map;
      }

      Map<String, Integer> out = new LinkedHashMap<>();
      int limit = Math.min(this.pop, this.reset + 4096);

      for (int rank = this.reset; rank < limit && out.size() < max; rank++) {
         int index = this.update_IArr[rank];
         if (this.write_BitSet.get(index) && !this.flush_BitSet.get(index)) {
            Item item = this.init_ItemArr[index];
            if (item != null && item != Items.AIR) {
               int cost;
               try {
                  cost = AiBuilder.init_I(this.init_TableIndex_StreamUArr[index]);
               } catch (RuntimeException unknown) {
                  cost = 1;
               }

               if (cost > 0) {
                  String id = BuiltInRegistries.ITEM.getKey(item).toString();
                  out.merge(id, cost, Integer::sum);
               }
            }
         }
      }

      this.stop = this.start;
      this.next = max;
      this.write_Map = Collections.unmodifiableMap(out);
      return this.write_Map;
   }

   Set<Long> init_Set() {
      if (this.init_Set == null) {
         this.init_Set = new AiBuildSite.Inner_BufferHelper();
      }

      return this.init_Set;
   }

   List<Long> init_List() {
      if (this.init_List != null) {
         return this.init_List;
      }

      LevelReader level = this.init_LevelReader();
      if (level == null) {
         this.init_List = List.of();
         return this.init_List;
      }

      LinkedHashSet<Long> out = new LinkedHashSet<>();
      int limit = Math.min(this.pop, this.reset + 8192);

      for (int rank = this.reset; rank < limit; rank++) {
         int index = this.update_IArr[rank];
         if (this.write_BitSet.get(index) && this.write_Z_I(index)) {
            int mask = this.load_BArr[index] & 255;
            if (mask != 0) {
               boolean fluid = this.init_IoWorker_CachePool_I(index) == AiBuildSite.Inner_CachePool.FLUID;
               BlockPos base = this.init_BlockPos_I(index);

               for (int ordinal = 0; ordinal < init_DirectionArr.length; ordinal++) {
                  if ((mask & 1 << ordinal) != 0) {
                     BlockPos at = base.relative(init_DirectionArr[ordinal]);
                     if (!this.init_Z_J(at.asLong())) {
                        boolean held = fluid ? update(level, at) : write(level, at);
                        if (!held) {
                           out.add(at.asLong());
                        }
                     }
                  }
               }
            }
         }
      }

      this.init_List = List.copyOf(out);
      return this.init_List;
   }

   boolean pop(int index) {
      return index >= 0 && index < this.pop && this.load_BitSet.get(index);
   }

   boolean tick(int index) {
      if (index >= 0 && index < this.pop) {
         BlockPos at = BlockPos.of(this.load_JArr[index]).below();
         int below = this.init_I_J(at.asLong());
         return below >= 0 && this.init_BitSet.get(below);
      } else {
         return false;
      }
   }

   boolean run(int index) {
      if (index >= 0 && index < this.pop) {
         BlockPos at = BlockPos.of(this.load_JArr[index]);
         return this.init_I_J(BlockPos.asLong(at.getX(), at.getY() + 1, at.getZ())) >= 0
            ? false
            : this.init_I_J(BlockPos.asLong(at.getX(), at.getY() + 2, at.getZ())) < 0;
      } else {
         return false;
      }
   }

   private int sync_I(int index) {
      if (this.load_BitSet.get(index)) {
         return 0;
      } else {
         return this.run(index) ? 1 : 2;
      }
   }

   int[] init_IArr_I_I(int fromIndex, int max) {
      if (max > 0 && this.read_IArr.length != 0) {
         int lowerRank = fromIndex >= 0 && fromIndex < this.pop ? this.sync_IArr[fromIndex] : 0;
         int[] found = this.load_IArr_I_I(lowerRank, max);
         if (found.length == 0 && this.set < this.read_IArr.length) {
            this.load_V();
            found = this.load_IArr_I_I(lowerRank, max);
         }

         return found;
      } else {
         return init_IArr;
      }
   }

   private int[] load_IArr_I_I(int lowerRank, int max) {
      int low = 0;
      int high = this.read_IArr.length - 1;
      int start = this.read_IArr.length;

      while (low <= high) {
         int middle = low + high >>> 1;
         if (this.sync_IArr[this.read_IArr[middle]] < lowerRank) {
            low = middle + 1;
         } else {
            start = middle;
            high = middle - 1;
         }
      }

      AiBuildSite.Inner_StreamUtil out = new AiBuildSite.Inner_StreamUtil();
      int budget = Math.max(512, max * 64);

      for (int at = start; at < this.read_IArr.length && out.init_I() < max && budget-- > 0; at++) {
         int index = this.read_IArr[at];
         if (this.write_BitSet.get(index) && !this.update_BitSet.get(index) && this.read_BitSet.get(index)) {
            out.init(index);
         }
      }

      return out.init_IArr();
   }

   private void load_V() {
      int budget = Math.max(4096, this.read_IArr.length >>> 3);

      while (this.set < this.read_IArr.length && budget-- > 0) {
         int index = this.read_IArr[this.set++];
         if (this.write_BitSet.get(index) && !this.update_BitSet.get(index) && this.get(index)) {
            this.read_BitSet.set(index);
         }
      }
   }

   private boolean get(int index) {
      BlockPos base = this.init_BlockPos_I(index);

      for (Direction direction : init_DirectionArr) {
         int other = this.init_I_J(base.relative(direction).asLong());
         if (other >= 0 && this.update_BitSet.get(other)) {
            return true;
         }
      }

      return false;
   }

   private void write_V_I(int index) {
      BlockPos base = this.init_BlockPos_I(index);

      for (Direction direction : init_DirectionArr) {
         int other = this.init_I_J(base.relative(direction).asLong());
         if (other >= 0 && !this.update_BitSet.get(other) && this.write_BitSet.get(other) && this.load_BitSet.get(other)) {
            this.read_BitSet.set(other);
         }
      }
   }

   int[] init_IArr_BlockPos(BlockPos position) {
      if (position == null) {
         return init_IArr;
      }

      int[] found = new int[init_DirectionArr.length];
      int count = 0;

      for (Direction direction : init_DirectionArr) {
         int other = this.init_I_J(position.relative(direction).asLong());
         if (other >= 0 && this.load_BitSet.get(other)) {
            found[count++] = other;
         }
      }

      if (count > 1) {
         this.init_V_IArr_I(found, count);
      }

      return count == 0 ? init_IArr : (count == found.length ? found : Arrays.copyOf(found, count));
   }

   Set<Long> load_Set() {
      if (this.load_Set == null) {
         this.load_Set = new AiBuildSite.Inner_ConfigStore();
      }

      return this.load_Set;
   }

   long[] load_JArr() {
      return this.read_JArr;
   }

   boolean load_Z_BlockPos(BlockPos feet) {
      LevelReader level = this.init_LevelReader();
      if (level == null || feet == null) {
         return false;
      } else if (read(level, feet.below()) && read(level, feet) && read(level, feet.above())) {
         return this.init_I_J(feet.asLong()) < 0 && this.init_I_J(feet.above().asLong()) < 0
            ? this.init_Z_LevelReader_BlockPos(level, feet.below())
               && load_Z_LevelReader_BlockPos(level, feet)
               && load_Z_LevelReader_BlockPos(level, feet.above())
            : false;
      } else {
         return false;
      }
   }

   private boolean init_Z_LevelReader_BlockPos(LevelReader level, BlockPos floorPos) {
      int index = this.init_I_J(floorPos.asLong());
      if (index < 0) {
         BlockState state = level.getBlockState(floorPos);
         if (state.isAir() || state.canBeReplaced()) {
            return false;
         } else if (state.getCollisionShape(level, floorPos).isEmpty()) {
            return false;
         } else {
            return !state.getFluidState().isEmpty() ? false : !autismclient.ai.engine.AiPathfinder.Inner_StreamUtil.read(state);
         }
      } else {
         return this.update_BitSet.get(index) && this.init_BitSet.get(index);
      }
   }

   private static boolean load_Z_LevelReader_BlockPos(LevelReader level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      if (!state.getCollisionShape(level, pos).isEmpty()) {
         return false;
      } else {
         return !state.getFluidState().isEmpty()
            ? false
            : !autismclient.ai.engine.AiPathfinder.Inner_StreamUtil.load(state) && !autismclient.ai.engine.AiPathfinder.Inner_StreamUtil.read(state);
      }
   }

   BlockPos[] init_BlockPosArr_Vec3(Vec3 center, double radius, int max) {
      if (center != null && max > 0 && this.pop != 0) {
         LevelReader level = this.init_LevelReader();
         if (level == null) {
            return init_BlockPosArr;
         }

         double capped = Math.min(radius, 8.0);
         if (capped <= 0.0) {
            return init_BlockPosArr;
         }

         double radiusSq = capped * capped;
         int baseX = (int)Math.floor(center.x);
         int baseY = (int)Math.floor(center.y);
         int baseZ = (int)Math.floor(center.z);
         int reach = (int)Math.ceil(capped);
         BlockPos[] best = new BlockPos[Math.min(max, 64)];
         double[] bestDistance = new double[best.length];
         int count = 0;

         for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
               for (int dz = -reach; dz <= reach; dz++) {
                  BlockPos feet = new BlockPos(baseX + dx, baseY + dy, baseZ + dz);
                  double distance = Vec3.atBottomCenterOf(feet).distanceToSqr(center);
                  if (!(distance > radiusSq)
                     && (count != best.length || init_Z_D(distance, feet, bestDistance[count - 1], best[count - 1]))
                     && this.load_Z_BlockPos(feet)) {
                     int at;
                     for (at = count < best.length ? count : best.length - 1; at > 0 && init_Z_D(distance, feet, bestDistance[at - 1], best[at - 1]); at--) {
                        best[at] = best[at - 1];
                        bestDistance[at] = bestDistance[at - 1];
                     }

                     best[at] = feet;
                     bestDistance[at] = distance;
                     if (count < best.length) {
                        count++;
                     }
                  }
               }
            }
         }

         return count == 0 ? init_BlockPosArr : Arrays.copyOf(best, count);
      } else {
         return init_BlockPosArr;
      }
   }

   private static boolean init_Z_D(double distance, BlockPos feet, double otherDistance, BlockPos other) {
      if (distance < otherDistance) {
         return true;
      } else if (distance > otherDistance) {
         return false;
      } else if (feet.getY() != other.getY()) {
         return feet.getY() < other.getY();
      } else {
         return feet.getX() != other.getX() ? feet.getX() < other.getX() : feet.getZ() < other.getZ();
      }
   }

   private int[] read_IArr() {
      int[] order = new int[this.pop];
      int i = 0;

      while (i < this.pop) {
         order[i] = i++;
      }

      int[] other = new int[this.pop];
      int[] field = new int[this.pop];

      for (int pass = 0; pass < 6; pass++) {
         for (int ix = 0; ix < this.pop; ix++) {
            int index = order[ix];
            long delta = this.init_JArr[index];
            int var12;
            switch (pass) {
               case 0:
                  var12 = init_I_I_I(load_I_J(delta), write_I_J(delta));
                  break;
               case 1:
                  var12 = poll_I_I(write_I_J(delta));
                  break;
               case 2:
                  var12 = open_I_I(this.init_BArr[index] & 255);
                  break;
               case 3:
                  var12 = poll_I_I(this.load_IArr[index]);
                  break;
               case 4:
                  var12 = this.sync_I(index);
                  break;
               default:
                  var12 = poll_I_I(read_I_J(delta));
            }

            field[ix] = var12;
         }

         int[] target = other;
         short var10000;
         switch (pass) {
            case 2:
               var10000 = 4;
               break;
            case 4:
               var10000 = 3;
               break;
            default:
               var10000 = 4096;
         }

         int range = var10000;
         init_V_IArr(order, target, field, range);
         other = order;
         order = target;
      }

      return order;
   }

   private int[] init_IArr_IArr(int[] base) {
      if (this.pop < 2) {
         return base;
      }

      int[] baseRank = new int[this.pop];
      int rank = 0;

      while (rank < this.pop) {
         baseRank[base[rank]] = rank++;
      }

      int[] provider = new int[this.pop];
      Arrays.fill(provider, -1);

      for (int rankx = 0; rankx < this.pop; rankx++) {
         int index = base[rankx];
         AiBuildSite.Inner_CachePool kind = this.init_IoWorker_CachePool_I(index);
         if (kind == AiBuildSite.Inner_CachePool.PARTNER) {
            int found = this.init_I_I(index, this.load_BArr[index] & 255, baseRank, 2147483647);
            if (found >= 0) {
               provider[index] = found;
            }
         } else if (kind == AiBuildSite.Inner_CachePool.SIMPLE || kind == AiBuildSite.Inner_CachePool.INTERACT) {
            int found = this.init_I_I_IArr(index, baseRank);
            if (found >= 0) {
               provider[index] = found;
            }
         }
      }

      for (int rankx = 0; rankx < this.pop; rankx++) {
         int index = base[rankx];
         if (provider[index] < 0) {
            int sibling = this.flush_I(index);
            if (sibling >= 0 && baseRank[sibling] < rankx) {
               provider[index] = sibling;
            }
         }
      }

      int[] dependentCount = new int[this.pop];

      for (int index = 0; index < this.pop; index++) {
         int parent = provider[index];
         if (parent >= 0 && parent != index) {
            dependentCount[parent]++;
         }
      }

      int[] dependentStart = new int[this.pop + 1];
      int total = 0;

      for (int index = 0; index < this.pop; index++) {
         dependentStart[index] = total;
         total += dependentCount[index];
      }

      dependentStart[this.pop] = total;
      int[] dependents = new int[total];
      int[] cursor = Arrays.copyOf(dependentStart, this.pop);

      for (int rankx = 0; rankx < this.pop; rankx++) {
         int index = base[rankx];
         int parent = provider[index];
         if (parent >= 0 && parent != index) {
            dependents[cursor[parent]++] = index;
         }
      }

      int[] out = new int[this.pop];
      BitSet emitted = new BitSet(this.pop);
      BitSet onPath = new BitSet(this.pop);
      int[] stack = new int[Math.max(8, Math.min(this.pop, 64))];
      int emittedCount = 0;

      for (int rankx = 0; rankx < this.pop; rankx++) {
         int start = base[rankx];
         if (!emitted.get(start)) {
            int depth = 0;
            if (depth == stack.length) {
               stack = Arrays.copyOf(stack, stack.length * 2);
            }

            stack[depth++] = start;

            while (depth > 0) {
               int index = stack[depth - 1];
               int parent = provider[index];
               if (parent >= 0 && !emitted.get(parent)) {
                  if (!onPath.get(parent)) {
                     onPath.set(index);
                     if (depth == stack.length) {
                        stack = Arrays.copyOf(stack, stack.length * 2);
                     }

                     stack[depth++] = parent;
                     continue;
                  }

                  provider[index] = -1;
               }

               depth--;
               if (!emitted.get(index)) {
                  onPath.clear(index);
                  emitted.set(index);
                  out[emittedCount++] = index;
                  int from = dependentStart[index];
                  int to = from + dependentCount[index];

                  for (int slot = to - 1; slot >= from; slot--) {
                     int dependent = dependents[slot];
                     if (!emitted.get(dependent) && !onPath.get(dependent)) {
                        if (depth == stack.length) {
                           stack = Arrays.copyOf(stack, stack.length * 2);
                        }

                        stack[depth++] = dependent;
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   private int init_I_I(int index, int mask, int[] baseRank, int ignore) {
      int best = -1;
      int bestRank = 2147483647;
      BlockPos base = this.init_BlockPos_I(index);

      for (int ordinal = 0; ordinal < init_DirectionArr.length; ordinal++) {
         if ((mask & 1 << ordinal) != 0) {
            int found = this.init_I_J(base.relative(init_DirectionArr[ordinal]).asLong());
            if (found >= 0 && found != index) {
               AiBuildSite.Inner_CachePool kind = this.init_IoWorker_CachePool_I(found);
               if (kind != AiBuildSite.Inner_CachePool.FLUID && kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP && found != ignore
                  )
                {
                  int rank = baseRank[found];
                  if (rank < bestRank) {
                     best = found;
                     bestRank = rank;
                  }
               }
            }
         }
      }

      return best;
   }

   private int init_I_I_IArr(int index, int[] baseRank) {
      int mask = this.load_BArr[index] & 255;
      if (mask == 0) {
         return -1;
      }

      int best = -1;
      int bestRank = 2147483647;
      BlockPos base = this.init_BlockPos_I(index);

      for (int ordinal = 0; ordinal < init_DirectionArr.length; ordinal++) {
         if ((mask & 1 << ordinal) != 0) {
            Direction direction = init_DirectionArr[ordinal];
            if (direction.getAxis().isHorizontal()) {
               int found = this.init_I_J(base.relative(direction).asLong());
               if (found >= 0 && found != index) {
                  AiBuildSite.Inner_CachePool kind = this.init_IoWorker_CachePool_I(found);
                  if (kind != AiBuildSite.Inner_CachePool.FLUID && kind != AiBuildSite.Inner_CachePool.DERIVED && kind != AiBuildSite.Inner_CachePool.SKIP) {
                     int rank = baseRank[found];
                     if (rank < bestRank) {
                        best = found;
                        bestRank = rank;
                     }
                  }
               }
            }
         }
      }

      return best;
   }

   private int flush_I(int index) {
      AiBuilder.Inner_StreamUtil cell = this.init_TableIndex_StreamUArr[index];
      String type = cell.properties().get("type");
      if (type != null && !type.equals("single") && cell.block().endsWith("chest")) {
         Direction side = AiBuilder.init_Direction(cell);
         if (side == null) {
            return -1;
         } else {
            int sibling = this.init_I_J(this.init_BlockPos_I(index).relative(side).asLong());
            if (sibling >= 0 && sibling != index) {
               return this.init_TableIndex_StreamUArr[sibling].block().equals(cell.block()) ? sibling : -1;
            } else {
               return -1;
            }
         }
      } else {
         return -1;
      }
   }

   private int close_I(int index) {
      int rank = this.sync_IArr[index];
      if (rank + 1 < this.pop) {
         int next = this.update_IArr[rank + 1];
         if (this.init_Z_I_I(next, index)) {
            return next;
         }
      }

      if (rank > 0) {
         int previous = this.update_IArr[rank - 1];
         if (this.init_Z_I_I(index, previous)) {
            return previous;
         }
      }

      return -1;
   }

   private boolean init_Z_I_I(int dependent, int provider) {
      if (dependent == provider) {
         return false;
      }

      if (this.init_IoWorker_CachePool_I(dependent) == AiBuildSite.Inner_CachePool.PARTNER) {
         int mask = this.load_BArr[dependent] & 255;
         BlockPos base = this.init_BlockPos_I(dependent);

         for (int ordinal = 0; ordinal < init_DirectionArr.length; ordinal++) {
            if ((mask & 1 << ordinal) != 0 && this.init_I_J(base.relative(init_DirectionArr[ordinal]).asLong()) == provider) {
               return true;
            }
         }

         return false;
      } else {
         return this.flush_I(dependent) == provider;
      }
   }

   private static int open_I_I(int kindOrdinal) {
      byte var10000;
      switch (init_IoWorker_CachePoolArr[kindOrdinal]) {
         case SIMPLE:
         case INTERACT:
         case PARTNER:
            var10000 = 0;
            break;
         case SPECIAL:
            var10000 = 1;
            break;
         case FLUID:
            var10000 = 2;
            break;
         case DERIVED:
         case SKIP:
            var10000 = 3;
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   private static int init_I_I_I(int dx, int dz) {
      return poll_I_I((dz & 1) == 0 ? dx : 4095 - dx);
   }

   private static int poll_I_I(int value) {
      return Math.clamp(value, 0, 4095);
   }

   private static void init_V_IArr(int[] src, int[] dst, int[] field, int range) {
      int[] counts = new int[range];

      for (int i = 0; i < src.length; i++) {
         counts[field[i]]++;
      }

      int sum = 0;

      for (int value = 0; value < range; value++) {
         int count = counts[value];
         counts[value] = sum;
         sum += count;
      }

      for (int i = 0; i < src.length; i++) {
         int index = src[i];
         dst[counts[field[i]]++] = index;
      }
   }

   private static byte init_B_TableIndex_StreamU_IoWorker_CachePool(AiBuilder.Inner_StreamUtil cell, AiBuildSite.Inner_CachePool kind) {
      List<BlockPos> offsets = AiBuildIntent.init_List(cell, kind);
      if (offsets.isEmpty()) {
         return 0;
      }

      int mask = 0;

      for (BlockPos offset : offsets) {
         Direction direction = Direction.getNearest(offset.getX(), offset.getY(), offset.getZ(), null);
         if (direction != null) {
            mask |= 1 << direction.ordinal();
         }
      }

      return (byte)mask;
   }

   private void read_V() {
      Arrays.fill(this.load_IArr, -1);
      int[] chain = new int[Math.min(this.pop, 64)];

      for (int i = 0; i < this.pop; i++) {
         if (this.load_IArr[i] < 0) {
            int length = 0;
            int current = i;
            int base = 0;

            while (true) {
               if (length == chain.length) {
                  chain = Arrays.copyOf(chain, chain.length * 2);
               }

               chain[length++] = current;
               int belowY = this.run + read_I_J(this.init_JArr[current]) - 1;
               long belowKey = BlockPos.asLong(this.tick + load_I_J(this.init_JArr[current]), belowY, this.get + write_I_J(this.init_JArr[current]));
               int below = this.init_I_J(belowKey);
               if (below < 0) {
                  base = 0;
                  break;
               }

               if (this.load_IArr[below] >= 0) {
                  base = this.load_IArr[below] + 1;
                  break;
               }

               current = below;
            }

            int depth = base;

            for (int slot = length - 1; slot >= 0; slot--) {
               this.load_IArr[chain[slot]] = poll_I_I(depth++);
            }
         }
      }
   }

   private Map<Long, int[]> load_Map() {
      if (this.pop == 0) {
         return Map.of();
      }

      HashMap<Long, AiBuildSite.Inner_StreamUtil> grouped = new HashMap<>();

      for (int i = 0; i < this.pop; i++) {
         long delta = this.init_JArr[i];
         long key = init_J_I(this.tick + load_I_J(delta), this.run + read_I_J(delta), this.get + write_I_J(delta));
         grouped.computeIfAbsent(key, ignored -> new AiBuildSite.Inner_StreamUtil()).init(i);
      }

      HashMap<Long, int[]> out = new HashMap<>(Math.max(16, grouped.size() * 2));

      for (Entry<Long, AiBuildSite.Inner_StreamUtil> entry : grouped.entrySet()) {
         out.put(entry.getKey(), entry.getValue().init_IArr());
      }

      return out;
   }

   private static long init_J_I(int x, int y, int z) {
      return BlockPos.asLong(x >> 4, y >> 4, z >> 4);
   }

   private int[] init_IArr_Vec3_D(Vec3 center, double radius) {
      AiBuildSite.Inner_StreamUtil found = new AiBuildSite.Inner_StreamUtil();
      double radiusSq = radius * radius;
      int minSectionX = (int)Math.floor(center.x - radius) >> 4;
      int maxSectionX = (int)Math.floor(center.x + radius) >> 4;
      int minSectionY = (int)Math.floor(center.y - radius) >> 4;
      int maxSectionY = (int)Math.floor(center.y + radius) >> 4;
      int minSectionZ = (int)Math.floor(center.z - radius) >> 4;
      int maxSectionZ = (int)Math.floor(center.z + radius) >> 4;

      for (int sectionX = minSectionX; sectionX <= maxSectionX; sectionX++) {
         for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
            for (int sectionZ = minSectionZ; sectionZ <= maxSectionZ; sectionZ++) {
               int[] indices = this.load_Map.get(BlockPos.asLong(sectionX, sectionY, sectionZ));
               if (indices != null) {
                  for (int index : indices) {
                     if (this.write_BitSet.get(index) && !this.flush_BitSet.get(index)) {
                        long delta = this.init_JArr[index];
                        double dx = this.tick + load_I_J(delta) + 0.5 - center.x;
                        double dy = this.run + read_I_J(delta) + 0.5 - center.y;
                        double dz = this.get + write_I_J(delta) + 0.5 - center.z;
                        if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                           found.init(index);
                        }
                     }
                  }
               }
            }
         }
      }

      return found.init_IArr();
   }

   private LevelReader init_LevelReader() {
      if (this.init_LevelReader != null) {
         return this.init_LevelReader;
      }

      try {
         Minecraft mc = Minecraft.getInstance();
         return mc == null ? null : mc.level;
      } catch (RuntimeException | LinkageError unavailable) {
         return null;
      }
   }

   private boolean init_Z_I_LevelReader(int index, LevelReader level) {
      if (index >= 0 && index < this.pop) {
         int mask = this.load_BArr[index] & 255;
         if (mask != 0 && level != null) {
            BlockPos base = this.init_BlockPos_I(index);
            boolean fluid = this.init_IoWorker_CachePool_I(index) == AiBuildSite.Inner_CachePool.FLUID;

            for (int ordinal = 0; ordinal < init_DirectionArr.length; ordinal++) {
               if ((mask & 1 << ordinal) != 0) {
                  BlockPos at = base.relative(init_DirectionArr[ordinal]);
                  int other = this.init_I_J(at.asLong());
                  if (other >= 0) {
                     AiBuildSite.Inner_CachePool otherKind = this.init_IoWorker_CachePool_I(other);
                     if (otherKind != AiBuildSite.Inner_CachePool.FLUID
                        && otherKind != AiBuildSite.Inner_CachePool.DERIVED
                        && otherKind != AiBuildSite.Inner_CachePool.SKIP
                        && !this.update_BitSet.get(other)) {
                        return false;
                     }
                  } else if (fluid && !update(level, at)) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean read(LevelReader level, BlockPos at) {
      return !(level instanceof Level real && !real.isLoaded(at));
   }

   private static boolean write(LevelReader level, BlockPos at) {
      if (!read(level, at)) {
         return false;
      }

      BlockState state = level.getBlockState(at);
      return !state.isAir() && !state.canBeReplaced() && !state.getShape(level, at).isEmpty();
   }

   private static boolean update(LevelReader level, BlockPos at) {
      if (!read(level, at)) {
         return false;
      }

      BlockState state = level.getBlockState(at);
      return !state.getFluidState().isEmpty() ? true : !state.isAir() && !state.canBeReplaced() && !state.getShape(level, at).isEmpty();
   }

   private boolean init_Z_I_BlockState(int index, BlockState live) {
      AiBuilder.Inner_StreamUtil cell = this.init_TableIndex_StreamUArr[index];
      if (AiBuildIntent.init(cell, live)) {
         return true;
      } else {
         return AiBuildIntent.read_Z(cell) && AiBuildIntent.init(AiBuildIntent.init_TableIndex_StreamU(cell), live) ? true : AiBuildIntent.init(cell, live, this.init_Map);
      }
   }

   private void init_V_IArr(int[] values) {
      int size = values.length;
      if (size >= 2) {
         int[] buffer = new int[size];

         for (int width = 1; width < size; width <<= 1) {
            for (int low = 0; low < size; low += width * 2) {
               int middle = Math.min(low + width, size);
               int high = Math.min(low + width * 2, size);
               int left = low;
               int right = middle;
               int write = low;

               while (left < middle && right < high) {
                  if (this.load_JArr[values[left]] <= this.load_JArr[values[right]]) {
                     buffer[write++] = values[left++];
                  } else {
                     buffer[write++] = values[right++];
                  }
               }

               while (left < middle) {
                  buffer[write++] = values[left++];
               }

               while (right < high) {
                  buffer[write++] = values[right++];
               }
            }

            System.arraycopy(buffer, 0, values, 0, size);
         }
      }
   }

   private void init_V_IArr_I(int[] values, int size) {
      if (size >= 2) {
         int[] buffer = new int[size];

         for (int width = 1; width < size; width <<= 1) {
            for (int low = 0; low < size; low += width * 2) {
               int middle = Math.min(low + width, size);
               int high = Math.min(low + width * 2, size);
               int left = low;
               int right = middle;
               int write = low;

               while (left < middle && right < high) {
                  if (this.sync_IArr[values[left]] <= this.sync_IArr[values[right]]) {
                     buffer[write++] = values[left++];
                  } else {
                     buffer[write++] = values[right++];
                  }
               }

               while (left < middle) {
                  buffer[write++] = values[left++];
               }

               while (right < high) {
                  buffer[write++] = values[right++];
               }
            }

            System.arraycopy(buffer, 0, values, 0, size);
         }
      }
   }

   private static long load_J_I(int dx, int dy, int dz) {
      long x = (long)(dx + 1048576) << 42;
      long y = (long)(dy + 1048576) << 21;
      long z = (long)(dz + 1048576) << 0;
      return x | y | z;
   }

   private static int load_I_J(long delta) {
      return (int)(delta >>> 42 & 2097151L) - 1048576;
   }

   private static int read_I_J(long delta) {
      return (int)(delta >>> 21 & 2097151L) - 1048576;
   }

   private static int write_I_J(long delta) {
      return (int)(delta >>> 0 & 2097151L) - 1048576;
   }

   private final class Inner_BufferHelper extends AbstractSet<Long> {
      @Override
      public int size() {
         return AiBuildSite.this.pop;
      }

      @Override
      public boolean contains(Object candidate) {
         return candidate instanceof Long key && AiBuildSite.this.init_I_J(key) >= 0;
      }

      @Override
      public Iterator<Long> iterator() {
         return new Iterator<Long>() {
            private int init;

            @Override
            public boolean hasNext() {
               return this.init < AiBuildSite.this.pop;
            }

            public Long next() {
               return this.init();
            }

            public Long init() {
               if (this.init >= AiBuildSite.this.pop) {
                  throw new NoSuchElementException();
               } else {
                  return AiBuildSite.this.write_JArr[this.init++];
               }
            }
         };
      }
   }

   enum Inner_CachePool {
      SIMPLE,
      INTERACT,
      SPECIAL,
      FLUID,
      PARTNER,
      DERIVED,
      SKIP;
   }

   private final class Inner_ConfigStore extends AbstractSet<Long> {
      @Override
      public int size() {
         return AiBuildSite.this.read_JArr.length;
      }

      @Override
      public boolean contains(Object candidate) {
         if (candidate instanceof Long key) {
            int low = 0;
            int high = AiBuildSite.this.read_JArr.length - 1;
            long wanted = key;

            while (low <= high) {
               int middle = low + high >>> 1;
               long value = AiBuildSite.this.read_JArr[middle];
               if (value < wanted) {
                  low = middle + 1;
               } else {
                  if (value <= wanted) {
                     return true;
                  }

                  high = middle - 1;
               }
            }

            return false;
         } else {
            return false;
         }
      }

      @Override
      public Iterator<Long> iterator() {
         return new Iterator<Long>() {
            private int init;

            @Override
            public boolean hasNext() {
               return this.init < AiBuildSite.this.read_JArr.length;
            }

            public Long next() {
               return this.init();
            }

            public Long init() {
               if (this.init >= AiBuildSite.this.read_JArr.length) {
                  throw new NoSuchElementException();
               } else {
                  return AiBuildSite.this.read_JArr[this.init++];
               }
            }
         };
      }
   }

   private record Inner_DataUtil(byte kindOrdinal, Item item, byte dependencies, boolean standable) {
   }

   private static final class Inner_StreamUtil {
      private int[] init_IArr = new int[8];
      private int init_I;

      void init(int value) {
         if (this.init_I == this.init_IArr.length) {
            this.init_IArr = Arrays.copyOf(this.init_IArr, this.init_I * 2);
         }

         this.init_IArr[this.init_I++] = value;
      }

      int init_I() {
         return this.init_I;
      }

      int[] init_IArr() {
         return this.init_I == 0 ? AiBuildSite.init_IArr : Arrays.copyOf(this.init_IArr, this.init_I);
      }
   }
}
