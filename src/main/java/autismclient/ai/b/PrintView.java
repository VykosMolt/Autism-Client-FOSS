package autismclient.ai.b;

import autismclient.ai.a.AiPhysics;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.WitherRoseBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class PrintView implements SignalGetter {
   static final BlockState init_BlockState = Blocks.BARRIER.defaultBlockState();
   private final PrintEnv init_EventBus;
   private final Long2ObjectOpenHashMap<BlockState> read_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private final Long2LongOpenHashMap write_Long2LongOpenHashM = new Long2LongOpenHashMap();
   private final Long2ObjectOpenHashMap<ArrayDeque<PrintView.Inner_DataUtil>> write_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private final LongOpenHashSet load_LongOpenHashSet = new LongOpenHashSet();
   private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
   private final Long2ObjectOpenHashMap<BlockState> update_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private boolean write_Z;
   static final int init_I = 32768;
   static final int load_I = 65536;
   static final int read_I = 8192;
   private final Long2ObjectLinkedOpenHashMap<PrintView.Inner_NetHelper> init_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   static final int write_I = 512;
   static final int update_I = 1048576;
   private static final int close_I = 256;
   private final int open_I;
   private int poll_I;
   long init_J;
   long load_J;
   private long flush_J;
   private long close_J;
   private final ArrayList<PrintView.Inner_TaskQueue> init_ArrayList = new ArrayList<>();
   private final ArrayList<PrintView.Inner_ConfigStore> load_ArrayList = new ArrayList<>();
   private final ArrayList<PrintView.Inner_StreamUtil> read_ArrayList = new ArrayList<>();
   private boolean update_Z;
   private final IdentityHashMap<BlockState, List<AABB>> init_IdentityHashMap = new IdentityHashMap<>();
   private long open_J;
   final Map<PrintNav.Inner_CachePool, List<PrintNav.Inner_BufferHelper>> init_Map = new LinkedHashMap<>();
   final Long2ObjectOpenHashMap<Direction> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   long read_J;
   long write_J;
   final Long2LongOpenHashMap init_Long2LongOpenHashM = new Long2LongOpenHashMap();
   final Long2LongOpenHashMap load_Long2LongOpenHashM = new Long2LongOpenHashMap();
   final Long2ObjectOpenHashMap<LongArrayList> load_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   final Long2LongOpenHashMap read_Long2LongOpenHashM = new Long2LongOpenHashMap();
   static final int sync_I = 64;
   private long poll_J;
   private long push_J;
   private final long[] init_JArr = new long[4096];
   private int push_I;
   private int pop_I;
   private long pop_J;
   static final boolean init_Z = !"false".equals(System.getProperty("autism.ai.print.feetCache", "true"));
   private static final int tick_I = 1;
   private static final int run_I = 3;
   private final Long2ObjectLinkedOpenHashMap<PrintView.Inner_CachePool> load_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   private final Long2ObjectLinkedOpenHashMap<PrintView.Inner_CachePool> read_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   long update_J;
   long sync_J;
   private long tick_J;
   private int get;
   private long run_J;
   static final int flush_I = 24;
   final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private LevelReader init_LevelReader;
   public boolean load_Z;
   static final double init_D = 0.03;
   static final boolean read_Z = Boolean.getBoolean("autism.ai.print.vanillaRays");
   private final IdentityHashMap<BlockState, List<AABB>> load_IdentityHashMap = new IdentityHashMap<>();
   private final IdentityHashMap<BlockState, List<AABB>> read_IdentityHashMap = new IdentityHashMap<>();

   PrintView.Inner_JsonUtil init_ArrayUtil_JsonUtil() {
      return new PrintView.Inner_JsonUtil();
   }

   long init_J() {
      return this.open_J;
   }

   Direction init_Direction_BlockPos(BlockPos pos) {
      return this.init_Long2ObjectOpenHas.get(pos.asLong());
   }

   public long load_J() {
      return this.flush_J;
   }

   long read_J() {
      return this.close_J;
   }

   boolean init_Z_Long2ObjectMap(Long2ObjectMap<BlockState> expected) {
      return init_Z_Long2ObjectMap_Long2ObjectMap(this.read_Long2ObjectOpenHas, expected);
   }

   static boolean init_Z_Long2ObjectMap_Long2ObjectMap(Long2ObjectMap<BlockState> actual, Long2ObjectMap<BlockState> expected) {
      if (actual == expected) {
         return true;
      }

      if (actual.size() != expected.size()) {
         return false;
      }

      ObjectIterator<Entry<BlockState>> entries = Long2ObjectMaps.fastIterator(expected);

      while (entries.hasNext()) {
         Entry<BlockState> entry = entries.next();
         long key = entry.getLongKey();
         BlockState wanted = entry.getValue();
         if (!Objects.equals(actual.get(key), wanted) || wanted == null && !actual.containsKey(key)) {
            return false;
         }
      }

      return true;
   }

   boolean init_Z() {
      return !this.read_Long2ObjectOpenHas.isEmpty();
   }

   boolean init_Z_Long2ObjectMap(Long2ObjectMap<BlockState> expected, BlockPos c, int radius) {
      if (this.read_Long2ObjectOpenHas.isEmpty() && expected.isEmpty()) {
         return true;
      }

      for (Entry<BlockState> e : this.read_Long2ObjectOpenHas.long2ObjectEntrySet()) {
         if (init_Z_J(e.getLongKey(), c, radius) && expected.get(e.getLongKey()) != e.getValue()) {
            return false;
         }
      }

      for (Entry<BlockState> e : expected.long2ObjectEntrySet()) {
         if (init_Z_J(e.getLongKey(), c, radius) && this.read_Long2ObjectOpenHas.get(e.getLongKey()) != e.getValue()) {
            return false;
         }
      }

      return true;
   }

   private static boolean init_Z_J(long key, BlockPos c, int radius) {
      return Math.abs(BlockPos.getX(key) - c.getX()) <= radius
         && Math.abs(BlockPos.getY(key) - c.getY()) <= radius
         && Math.abs(BlockPos.getZ(key) - c.getZ()) <= radius;
   }

   public void init_V_Long2ObjectMap(Long2ObjectMap<BlockState> into) {
      into.putAll(this.read_Long2ObjectOpenHas);
   }

   long init_J_BlockPos_I(BlockPos c, int radius) {
      if (this.read_Long2ObjectOpenHas.isEmpty()) {
         return 0L;
      }

      long hash = 0L;

      for (Entry<BlockState> e : this.read_Long2ObjectOpenHas.long2ObjectEntrySet()) {
         if (init_Z_J(e.getLongKey(), c, radius)) {
            long h = e.getLongKey() * -4658895280553007687L + Block.getId(e.getValue());
            hash += (h ^ h >>> 31) * -7723592293110705685L;
         }
      }

      return hash;
   }

   void init_V_LongSet(LongSet into) {
      into.addAll(this.load_LongOpenHashSet);
   }

   boolean init_Z_LongSet(LongSet expected) {
      return this.load_LongOpenHashSet.equals(expected);
   }

   public PrintView(PrintEnv env) {
      this(env, 1048576);
   }

   PrintView(PrintEnv env, int geometryInputBudget) {
      if (geometryInputBudget >= 0 && geometryInputBudget <= 1048576) {
         this.init_EventBus = env;
         this.open_I = geometryInputBudget;
      } else {
         throw new IllegalArgumentException("Invalid native geometry receipt budget");
      }
   }

   public void init_V_J(long key) {
      this.flush_J++;
      this.close_J++;
      this.sync(key);
      this.update(key);
      this.init_V_J_Z(key, false);
   }

   public void load_V_J(long key) {
      this.flush_J++;
      this.close_J++;
      this.sync(key);
      this.update(key);
      this.init_V_J_Z(key, true);
   }

   private void init_V_J_Z(long key, boolean addition) {
      long bucket = BlockPos.asLong(BlockPos.getX(key) >> 2, BlockPos.getY(key) >> 2, BlockPos.getZ(key) >> 2);
      (addition ? this.load_Long2LongOpenHashM : this.init_Long2LongOpenHashM).put(bucket, this.close_J);
      if (addition) {
         LongArrayList log = this.load_Long2ObjectOpenHas.computeIfAbsent(bucket, k -> new LongArrayList());
         log.add(this.close_J);
         log.add(key);
         if (log.size() > 128) {
            this.read_Long2LongOpenHashM.put(bucket, Math.max(this.read_Long2LongOpenHashM.get(bucket), log.getLong(0)));
            log.removeElements(0, 2);
         }
      }
   }

   public void read(long key) {
      this.sync(key);
      this.update_Long2ObjectOpenHas.remove(key);
   }

   private void sync(long key) {
      this.poll_J++;
      this.write(key);
   }

   void write(long key) {
      this.push_J++;
      this.init_JArr[this.push_I] = key;
      this.push_I = (this.push_I + 1) % this.init_JArr.length;
      this.pop_I = Math.min(this.pop_I + 1, this.init_JArr.length);
   }

   long write_J() {
      return this.poll_J;
   }

   long update_J() {
      return this.push_J;
   }

   boolean init_Z_J_Long2ObjectMap(long since, Long2ObjectMap<BlockState> inputs) {
      long changes = this.push_J - since;
      if (since >= 0L && changes >= 0L) {
         if (since >= this.pop_J && changes <= this.pop_I) {
            int cursor = this.push_I;

            for (long changed = 0L; changed < changes; changed++) {
               cursor = (cursor + this.init_JArr.length - 1) % this.init_JArr.length;
               long key = this.init_JArr[cursor];
               BlockState wanted = inputs.get(key);
               if (wanted != null && this.getBlockState(BlockPos.of(key)) != wanted) {
                  return false;
               }
            }

            return true;
         } else {
            for (Entry<BlockState> entry : inputs.long2ObjectEntrySet()) {
               if (this.getBlockState(BlockPos.of(entry.getLongKey())) != entry.getValue()) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public void update(long key) {
      this.update_Long2ObjectOpenHas.remove(key);
      int x = BlockPos.getX(key);
      int y = BlockPos.getY(key);
      int z = BlockPos.getZ(key);

      for (int dy = -3; dy <= 2; dy++) {
         this.flush(BlockPos.asLong(x, y + dy, z));
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -4; dy <= 1; dy++) {
               this.flush(BlockPos.asLong(x + dx, y + dy, z + dz));
            }
         }
      }

      if (!this.load_Long2ObjectLinkedO.isEmpty() || !this.read_Long2ObjectLinkedO.isEmpty()) {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               for (int dy = -3; dy <= 3; dy++) {
                  long s = BlockPos.asLong(x + dx, y + dy, z + dz);
                  this.init_V_Long2ObjectLinkedO_J(this.load_Long2ObjectLinkedO, s);
                  this.init_V_Long2ObjectLinkedO_J(this.read_Long2ObjectLinkedO, s);
               }
            }
         }
      }
   }

   public void init_V() {
      this.init_Long2ObjectLinkedO.clear();
      this.load_Long2ObjectLinkedO.clear();
      this.read_Long2ObjectLinkedO.clear();
      this.poll_I = 0;
   }

   void load_V() {
      this.update_Long2ObjectOpenHas.clear();
      this.flush_J++;
      this.close_J++;
      this.poll_J++;
      this.pop_J = ++this.push_J;
      this.push_I = this.pop_I = 0;
      this.init_V();
      this.init_Map.clear();
      this.init_Long2LongOpenHashM.clear();
      this.load_Long2LongOpenHashM.clear();
      this.load_Long2ObjectOpenHas.clear();
      this.read_Long2LongOpenHashM.clear();
      this.init_Long2LongOpenHashM.defaultReturnValue(this.close_J);
   }

   @Nullable List<Vec3> init_List_J(long key, double feetY, boolean detailed) {
      int x = BlockPos.getX(key);
      int y = BlockPos.getY(key);
      int z = BlockPos.getZ(key);
      if (!this.read_Long2ObjectOpenHas.isEmpty() && this.load_Z(x, y, z)) {
         return null;
      }

      Long2ObjectLinkedOpenHashMap<PrintView.Inner_CachePool> map = detailed ? this.read_Long2ObjectLinkedO : this.load_Long2ObjectLinkedO;
      PrintView.Inner_CachePool entry = map.getAndMoveToLast(key);
      boolean needsInputs = this.read_Z();
      if (entry != null && entry.feetY() == feetY && entry.points() != null) {
         if (entry.inputs() == null && !needsInputs || entry.inputs() != null && this.load_Z_ArrayUtil_BufferHe(entry.inputs())) {
            this.update_J++;
            return entry.points();
         }

         if (entry.inputs() != null) {
            return null;
         }
      }

      if (entry != null && entry.feetY() == feetY) {
         if (!needsInputs) {
            long before = this.open_J;
            List<Vec3> all = PrintClicks.load(this, key, feetY, detailed);
            this.sync_J++;
            if (this.open_J == before) {
               this.init(map, key, feetY, all, null);
            } else {
               this.init_V_Long2ObjectLinkedO_J(map, key);
            }

            return all;
         } else {
            try (PrintView.Inner_ConfigStore reads = new PrintView.Inner_ConfigStore()) {
               List<Vec3> all = PrintClicks.load(this, key, feetY, detailed);
               this.sync_J++;
               if (reads.init()) {
                  this.init(map, key, feetY, all, this.init_ArrayUtil_BufferHe_ArrayUtil_ConfigSt(reads));
               } else if (reads.init_J != this.open_J) {
                  this.init_V_Long2ObjectLinkedO_J(map, key);
               }

               return all;
            }
         }
      } else {
         if (entry == null && map.size() >= 8192) {
            this.init_V_Long2ObjectLinkedO_J(map, map.firstLongKey());
         }

         this.init_V_Long2ObjectLinkedO_J(map, key);
         map.putAndMoveToLast(key, new PrintView.Inner_CachePool(feetY, null, null));
         return null;
      }
   }

   private boolean read_Z() {
      return !this.read_Long2ObjectOpenHas.isEmpty() || !this.init_ArrayList.isEmpty() || !this.load_ArrayList.isEmpty();
   }

   private void init(
      Long2ObjectLinkedOpenHashMap<PrintView.Inner_CachePool> map, long key, double feetY, List<Vec3> points, PrintView.@Nullable Inner_BufferHelper inputs
   ) {
      this.init_V_Long2ObjectLinkedO_J(map, key);
      if (inputs != null && !this.init_Z_I(inputs.cells().length)) {
         inputs = null;
      }

      map.putAndMoveToLast(key, new PrintView.Inner_CachePool(feetY, points, inputs));
      if (inputs != null) {
         this.poll_I = this.poll_I + inputs.cells().length;
      }
   }

   private void init_V_J(long key, double height, PrintView.@Nullable Inner_BufferHelper inputs) {
      this.flush(key);
      if (this.init_Long2ObjectLinkedO.size() >= 65536) {
         this.flush(this.init_Long2ObjectLinkedO.firstLongKey());
      }

      if (inputs != null && !this.init_Z_I(inputs.cells().length)) {
         inputs = null;
      }

      this.init_Long2ObjectLinkedO.putAndMoveToLast(key, new PrintView.Inner_NetHelper(height, inputs));
      if (inputs != null) {
         this.poll_I = this.poll_I + inputs.cells().length;
      }
   }

   private void flush(long key) {
      PrintView.Inner_NetHelper removed = this.init_Long2ObjectLinkedO.remove(key);
      if (removed != null && removed.inputs() != null) {
         this.poll_I = this.poll_I - removed.inputs().cells().length;
      }
   }

   private void init_V_Long2ObjectLinkedO_J(Long2ObjectLinkedOpenHashMap<PrintView.Inner_CachePool> map, long key) {
      PrintView.Inner_CachePool removed = map.remove(key);
      if (removed != null && removed.inputs() != null) {
         this.poll_I = this.poll_I - removed.inputs().cells().length;
      }
   }

   private boolean init_Z_I(int count) {
      if (count > this.open_I) {
         return false;
      }

      for (int evictions = 0; this.poll_I + count > this.open_I && evictions < 32; evictions++) {
         if (!this.init_Long2ObjectLinkedO.isEmpty()) {
            this.flush(this.init_Long2ObjectLinkedO.firstLongKey());
         } else if (this.read_Long2ObjectLinkedO.isEmpty() || !this.load_Long2ObjectLinkedO.isEmpty() && (evictions & 1) != 0) {
            if (this.load_Long2ObjectLinkedO.isEmpty()) {
               return false;
            }

            this.init_V_Long2ObjectLinkedO_J(this.load_Long2ObjectLinkedO, this.load_Long2ObjectLinkedO.firstLongKey());
         } else {
            this.init_V_Long2ObjectLinkedO_J(this.read_Long2ObjectLinkedO, this.read_Long2ObjectLinkedO.firstLongKey());
         }
      }

      return this.poll_I + count <= this.open_I;
   }

   private PrintView.@Nullable Inner_BufferHelper init_ArrayUtil_BufferHe_ArrayUtil_ConfigSt(PrintView.Inner_ConfigStore reads) {
      if (!reads.load()) {
         return null;
      }

      long[] cells = reads.init_LongOpenHashSet.toLongArray();
      Arrays.sort(cells);
      return new PrintView.Inner_BufferHelper(cells);
   }

   private boolean init_Z_ArrayUtil_BufferHe(PrintView.Inner_BufferHelper inputs) {
      if (this.read_Long2ObjectOpenHas.isEmpty()) {
         return false;
      }

      if (this.read_Long2ObjectOpenHas.size() < inputs.cells().length) {
         for (long key : this.read_Long2ObjectOpenHas.keySet()) {
            if (Arrays.binarySearch(inputs.cells(), key) >= 0) {
               return true;
            }
         }
      } else {
         for (long key : inputs.cells()) {
            if (this.read_Long2ObjectOpenHas.containsKey(key)) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean load_Z_ArrayUtil_BufferHe(PrintView.@Nullable Inner_BufferHelper inputs) {
      if (inputs == null) {
         return false;
      }

      if (this.init_Z_ArrayUtil_BufferHe(inputs)) {
         return false;
      }

      if (this.init_ArrayList.isEmpty() && this.load_ArrayList.isEmpty()) {
         return true;
      }

      boolean forwarding = false;

      for (int i = 0; i < this.init_ArrayList.size(); i++) {
         if (!this.init_ArrayList.get(i).init_Z(inputs)) {
            forwarding = true;
            break;
         }
      }

      if (!forwarding) {
         for (int i = 0; i < this.load_ArrayList.size(); i++) {
            if (!this.load_ArrayList.get(i).init_Z(inputs)) {
               forwarding = true;
               break;
            }
         }
      }

      if (!forwarding) {
         return true;
      }

      for (long key : inputs.cells()) {
         boolean needed = false;

         for (int i = 0; i < this.init_ArrayList.size(); i++) {
            PrintView.Inner_TaskQueue reads = this.init_ArrayList.get(i);
            if (!reads.init_Z(inputs) && !reads.init_Long2ObjectOpenHas.containsKey(key)) {
               needed = true;
               break;
            }
         }

         if (!needed) {
            for (int i = 0; i < this.load_ArrayList.size(); i++) {
               PrintView.Inner_ConfigStore reads = this.load_ArrayList.get(i);
               if (!reads.init_Z(inputs) && !reads.init_LongOpenHashSet.contains(key)) {
                  needed = true;
                  break;
               }
            }
         }

         if (needed) {
            BlockPos pos = BlockPos.of(key);
            if (this.get == 0) {
               this.tick_J++;
            }

            BlockState live = this.init_BlockState_BlockPos(pos);
            if (live == init_BlockState) {
               return false;
            }

            for (int i = 0; i < this.init_ArrayList.size(); i++) {
               PrintView.Inner_TaskQueue reads = this.init_ArrayList.get(i);
               if (!reads.init_Z(inputs)) {
                  reads.init(pos, live);
               }
            }

            for (int i = 0; i < this.load_ArrayList.size(); i++) {
               PrintView.Inner_ConfigStore reads = this.load_ArrayList.get(i);
               if (!reads.init_Z(inputs)) {
                  reads.init(key);
               }
            }
         }
      }

      for (int i = 0; i < this.init_ArrayList.size(); i++) {
         this.init_ArrayList.get(i).init_V(inputs);
      }

      for (int i = 0; i < this.load_ArrayList.size(); i++) {
         this.load_ArrayList.get(i).init_V(inputs);
      }

      return true;
   }

   private boolean load_Z(int x, int y, int z) {
      if (this.read_Long2ObjectOpenHas.size() <= 32) {
         for (long k : this.read_Long2ObjectOpenHas.keySet()) {
            int dx = BlockPos.getX(k) - x;
            int dy = BlockPos.getY(k) - y;
            int dz = BlockPos.getZ(k) - z;
            if (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && Math.abs(dy) <= 3) {
               return true;
            }
         }

         return false;
      } else {
         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               for (int dy = -3; dy <= 3; dy++) {
                  if (this.read_Long2ObjectOpenHas.containsKey(BlockPos.asLong(x + dx, y + dy, z + dz))) {
                     return true;
                  }
               }
            }
         }

         return false;
      }
   }

   public long sync() {
      return this.tick_J;
   }

   public long flush() {
      return this.run_J;
   }

   public void read_V() {
      this.get++;
   }

   public void write_V() {
      this.get--;
   }

   @Override
   public BlockState getBlockState(BlockPos pos) {
      if (this.get == 0) {
         this.tick_J++;
      }

      for (int i = 0; i < this.init_ArrayList.size(); i++) {
         this.init_ArrayList.get(i).init(pos);
      }

      for (int i = 0; i < this.load_ArrayList.size(); i++) {
         this.load_ArrayList.get(i).init(pos.asLong());
      }

      if (!this.read_Long2ObjectOpenHas.isEmpty()) {
         BlockState assumed = this.read_Long2ObjectOpenHas.get(pos.asLong());
         if (assumed != null) {
            return assumed;
         }
      }

      return this.init_BlockState_BlockPos(pos);
   }

   private BlockState init_BlockState_BlockPos(BlockPos pos) {
      if (pos.getY() < this.init_EventBus.init_I() || pos.getY() >= this.init_EventBus.load_I()) {
         return pos.getY() < this.init_EventBus.init_I() ? init_BlockState : Blocks.AIR.defaultBlockState();
      }

      if (!this.init_EventBus.load_Z(pos)) {
         this.update_Long2ObjectOpenHas.remove(pos.asLong());
         this.open_J++;
         return init_BlockState;
      }

      if (!this.write_Z) {
         return this.init_BlockState_BlockPos_BlockState(pos, this.init_EventBus.init_BlockState_BlockPos(pos));
      }

      long key = pos.asLong();
      BlockState state = this.update_Long2ObjectOpenHas.get(key);
      if (state == null) {
         state = this.init_BlockState_BlockPos_BlockState(pos, this.init_EventBus.init_BlockState_BlockPos(pos));
         if (this.update_Long2ObjectOpenHas.size() >= 32768) {
            this.update_Long2ObjectOpenHas.clear();
         }

         this.update_Long2ObjectOpenHas.put(key, state);
      }

      return state;
   }

   private BlockState init_BlockState_BlockPos_BlockState(BlockPos pos, BlockState state) {
      if (state.getFluidState().getType().isSame(Fluids.LAVA)) {
         this.init_LongOpenHashSet.add(pos.asLong());
      } else if (!this.init_LongOpenHashSet.isEmpty()) {
         this.init_LongOpenHashSet.remove(pos.asLong());
      }

      return state;
   }

   PrintView.Inner_TaskQueue init_ArrayUtil_TaskQueu() {
      PrintView.Inner_TaskQueue reads = new PrintView.Inner_TaskQueue(false);
      this.init_ArrayList.add(reads);
      return reads;
   }

   PrintView.Inner_TaskQueue load_ArrayUtil_TaskQueu() {
      return new PrintView.Inner_TaskQueue(true);
   }

   void init_V_ArrayUtil_TaskQueu(PrintView.Inner_TaskQueue inputs) {
      if (!this.init_ArrayList.isEmpty() || !this.load_ArrayList.isEmpty()) {
         for (long key : inputs.init_Long2ObjectOpenHas.keySet()) {
            this.getBlockState(BlockPos.of(key));
         }
      }
   }

   private void init_V_BlockState_BlockPos(BlockState state, BlockPos pos) {
      if (!this.update_Z && !this.read_ArrayList.isEmpty() && state != init_BlockState) {
         Block block = state.getBlock();
         if ((block instanceof MovingPistonBlock || block instanceof ShulkerBoxBlock)
            && (!this.init_Z_BlockPos(pos) || state == this.init_EventBus.init_BlockState_BlockPos(pos))) {
            for (int i = 0; i < this.read_ArrayList.size(); i++) {
               this.read_ArrayList.get(i).init_LongOpenHashSet.add(pos.asLong());
            }
         }
      }
   }

   public BlockState init_BlockState_I(int x, int y, int z) {
      return this.getBlockState(this.init_BlockPos_MutableBl.set(x, y, z));
   }

   @Override
   public FluidState getFluidState(BlockPos pos) {
      return this.getBlockState(pos).getFluidState();
   }

   @Override
   public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
      this.open_J++;
      if (!this.init_EventBus.load_Z(pos)) {
         return null;
      }

      BlockState assumed = this.read_Long2ObjectOpenHas.get(pos.asLong());
      if (assumed != null && assumed != this.init_EventBus.init_BlockState_BlockPos(pos)) {
         return null;
      }

      this.init_V_BlockState_BlockPos(assumed == null ? this.init_EventBus.init_BlockState_BlockPos(pos) : assumed, pos);
      return this.init_EventBus.init_BlockEntity_BlockPos(pos);
   }

   @Override
   public int getHeight() {
      return this.init_EventBus.load_I() - this.init_EventBus.init_I();
   }

   @Override
   public int getMinY() {
      return this.init_EventBus.init_I();
   }

   private BlockState load_BlockState_BlockPos(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      if (state == init_BlockState) {
         throw new UnsupportedOperationException("Unknown hypothetical support");
      } else {
         return state;
      }
   }

   boolean init_Z_BlockState_BlockPos(BlockState state, BlockPos pos) {
      if (this.init_LevelReader == null) {
         this.init_LevelReader = (LevelReader)Proxy.newProxyInstance(
            LevelReader.class.getClassLoader(),
            new Class[]{LevelReader.class},
            (proxy, method, args) -> {
               Object var10000;
               switch (method.getName()) {
                  case "getBlockState":
                     var10000 = this.load_BlockState_BlockPos((BlockPos)args[0]);
                     break;
                  case "getFluidState":
                     var10000 = this.load_BlockState_BlockPos((BlockPos)args[0]).getFluidState();
                     break;
                  case "getBlockEntity":
                     if (method.getParameterCount() != 1) {
                        var10000 = InvocationHandler.invokeDefault(proxy, method, args);
                     } else {
                        this.load_BlockState_BlockPos((BlockPos)args[0]);
                        var10000 = this.getBlockEntity((BlockPos)args[0]);
                     }
                     break;
                  case "isEmptyBlock":
                     var10000 = this.load_BlockState_BlockPos((BlockPos)args[0]).isAir();
                     break;
                  case "hasChunkAt":
                     var10000 = args.length == 1
                        ? this.init_EventBus.load_Z((BlockPos)args[0])
                        : this.init_EventBus.load_Z(new BlockPos((Integer)args[0], this.init_EventBus.init_I(), (Integer)args[1]));
                     break;
                  case "isUnobstructed":
                     throw new UnsupportedOperationException("No hypothetical entity clearance");
                  case "getMinY":
                     var10000 = this.getMinY();
                     break;
                  case "getHeight":
                     if (args != null && args.length != 0) {
                        throw new UnsupportedOperationException("No hypothetical heightmap");
                     }

                     var10000 = this.getHeight();
                     break;
                  case "toString":
                     var10000 = "PrintView.survivalReader";
                     break;
                  case "hashCode":
                     var10000 = System.identityHashCode(proxy);
                     break;
                  case "equals":
                     var10000 = proxy == args[0];
                     break;
                  default:
                     if (!method.isDefault()) {
                        throw new UnsupportedOperationException("No hypothetical " + method.getName());
                     }

                     var10000 = InvocationHandler.invokeDefault(proxy, method, args);
               }

               return var10000;
            }
         );
      }

      try {
         return state.canSurvive(this.init_LevelReader, pos);
      } catch (UnsupportedOperationException unavailable) {
         return false;
      }
   }

   public void init_V_BlockPos_BlockState(BlockPos pos, BlockState state) {
      this.init_V_BlockPos(pos, state, false);
   }

   public void load(BlockPos pos, BlockState state) {
      this.init_V_BlockPos(pos, state, true);
   }

   public boolean init_Z_BlockPos(BlockPos pos) {
      return this.read_Long2ObjectOpenHas.containsKey(pos.asLong());
   }

   public boolean load_Z_BlockPos(BlockPos pos) {
      return this.load_LongOpenHashSet.contains(pos.asLong());
   }

   private void init_V_BlockPos(BlockPos pos, BlockState state, boolean ownedTemporary) {
      this.flush_J++;
      long key = pos.asLong();
      BlockState old = this.read_Long2ObjectOpenHas.put(key, state);
      if (old == null) {
         this.load(key, true);
      }

      if (old != null) {
         this.write_Long2ObjectOpenHas
            .computeIfAbsent(key, ignored -> new ArrayDeque<>())
            .push(new PrintView.Inner_DataUtil(old, this.load_LongOpenHashSet.contains(key)));
      }

      if (ownedTemporary) {
         this.load_LongOpenHashSet.add(key);
      } else {
         this.load_LongOpenHashSet.remove(key);
      }
   }

   public boolean read(BlockPos pos) {
      if (this.read_Long2ObjectOpenHas.isEmpty()) {
         return true;
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
               if (this.read_Long2ObjectOpenHas.containsKey(BlockPos.asLong(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz))) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public void init_V_BlockPos(BlockPos pos) {
      this.flush_J++;
      long key = pos.asLong();
      ArrayDeque<PrintView.Inner_DataUtil> stack = this.write_Long2ObjectOpenHas.get(key);
      if (stack == null) {
         if (this.read_Long2ObjectOpenHas.remove(key) != null) {
            this.load(key, false);
         }

         this.load_LongOpenHashSet.remove(key);
      } else {
         PrintView.Inner_DataUtil old = stack.pop();
         this.read_Long2ObjectOpenHas.put(key, old.state());
         if (old.temporary()) {
            this.load_LongOpenHashSet.add(key);
         } else {
            this.load_LongOpenHashSet.remove(key);
         }

         if (stack.isEmpty()) {
            this.write_Long2ObjectOpenHas.remove(key);
         }
      }
   }

   public void update_V() {
      if (!this.read_Long2ObjectOpenHas.isEmpty()) {
         this.flush_J++;
      }

      this.read_Long2ObjectOpenHas.clear();
      this.write_Long2LongOpenHashM.clear();
      this.write_Long2ObjectOpenHas.clear();
      this.load_LongOpenHashSet.clear();
   }

   private void load(long key, boolean present) {
      int y = BlockPos.getY(key);
      long column = BlockPos.asLong(BlockPos.getX(key), y >> 6, BlockPos.getZ(key));
      long bit = 1L << y;
      long before = this.write_Long2LongOpenHashM.get(column);
      long after = present ? before | bit : before & ~bit;
      if (after == 0L) {
         this.write_Long2LongOpenHashM.remove(column);
      } else {
         this.write_Long2LongOpenHashM.put(column, after);
      }
   }

   public int init_I() {
      return this.read_Long2ObjectOpenHas.size();
   }

   public boolean load_Z() {
      return !this.read_Long2ObjectOpenHas.isEmpty();
   }

   public VoxelShape init_VoxelShape_BlockPos(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      this.init_V_BlockState_BlockPos(state, pos);
      return state.getCollisionShape(this, pos);
   }

   List<AABB> init_List_BlockState_BlockPos(BlockState state, BlockPos pos) {
      if (state.getBlock().hasDynamicShape()) {
         this.open_J++;
         this.init_V_BlockState_BlockPos(state, pos);
         return state.getCollisionShape(this, pos).toAabbs();
      } else {
         return this.init_IdentityHashMap.computeIfAbsent(state, key -> List.copyOf(key.getCollisionShape(this, pos).toAabbs()));
      }
   }

   List<AABB> init_List_BlockPos(BlockPos pos) {
      return this.init_List_BlockState_BlockPos(this.getBlockState(pos), pos);
   }

   public VoxelShape load_VoxelShape_BlockPos(BlockPos pos) {
      return this.getBlockState(pos).getShape(this, pos);
   }

   public boolean init_Z_AABB(AABB box) {
      AABB inset = box.deflate(1.0E-6);
      int x0 = init_I_D(box.minX);
      int x1 = init_I_D(box.maxX - 1.0E-7);
      int y0 = init_I_D(box.minY);
      int y1 = init_I_D(box.maxY - 1.0E-7);
      int z0 = init_I_D(box.minZ);
      int z1 = init_I_D(box.maxZ - 1.0E-7);
      MutableBlockPos p = new MutableBlockPos();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0 - 1; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               p.set(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir()) {
                  for (AABB b : this.init_List_BlockState_BlockPos(state, p)) {
                     if (b.move(x, y, z).intersects(inset)) {
                        return false;
                     }
                  }
               }
            }
         }
      }

      return true;
   }

   public List<AABB> init_List_AABB(AABB query) {
      List<AABB> out = new ArrayList<>();
      int x0 = init_I_D(query.minX);
      int x1 = init_I_D(query.maxX);
      int y0 = init_I_D(query.minY) - 1;
      int y1 = init_I_D(query.maxY);
      int z0 = init_I_D(query.minZ);
      int z1 = init_I_D(query.maxZ);
      MutableBlockPos p = new MutableBlockPos();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               p.set(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir()) {
                  for (AABB b : this.init_List_BlockState_BlockPos(state, p)) {
                     AABB moved = b.move(x, y, z);
                     if (moved.intersects(query)) {
                        out.add(moved);
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   public boolean load_Z_AABB(AABB box) {
      AABB b = box.deflate(0.001);

      for (BlockPos q : BlockPos.betweenClosed(BlockPos.containing(b.minX, b.minY, b.minZ), BlockPos.containing(b.maxX, b.maxY, b.maxZ))) {
         BlockState s = this.getBlockState(q);
         if (!s.isAir()) {
            FluidState fluid = s.getFluidState();
            if (fluid.getType().isSame(Fluids.LAVA)) {
               if (b.minY < q.getY() + fluid.getHeight(this, q)) {
                  return true;
               }
            } else if (init_Z_BlockState(s)) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean init_Z_BlockState(BlockState state) {
      if (state.isAir()) {
         return false;
      }

      if (state.getFluidState().getType().isSame(Fluids.LAVA)) {
         return true;
      }

      Block b = state.getBlock();
      return b instanceof BaseFireBlock
         || b instanceof CactusBlock
         || b instanceof SweetBerryBushBlock
         || b instanceof WebBlock
         || b instanceof PowderSnowBlock
         || b instanceof MagmaBlock
         || b instanceof CampfireBlock
         || b instanceof WitherRoseBlock
         || b instanceof PointedDripstoneBlock;
   }

   public double init_D_I_2(int x, int y, int z) {
      if (!this.read_Long2ObjectOpenHas.isEmpty() && this.read(x, y, z)) {
         return this.load_D(x, y, z);
      }

      long key = BlockPos.asLong(x, y, z);
      PrintView.Inner_NetHelper cached = this.init_Long2ObjectLinkedO.getAndMoveToLast(key);
      boolean needsInputs = this.read_Z();
      if (cached != null) {
         if (cached.inputs() == null && !needsInputs || cached.inputs() != null && this.load_Z_ArrayUtil_BufferHe(cached.inputs())) {
            this.init_J++;
            return cached.height();
         }

         if (cached.inputs() != null) {
            return this.load_D(x, y, z);
         }
      }

      if (!needsInputs) {
         long before = this.open_J;
         double height = this.load_D(x, y, z);
         this.load_J++;
         if (this.open_J == before) {
            this.init_V_J(key, height, null);
         }

         return height;
      } else {
         try (PrintView.Inner_ConfigStore reads = new PrintView.Inner_ConfigStore()) {
            double h = this.load_D(x, y, z);
            this.load_J++;
            if (reads.init()) {
               this.init_V_J(key, h, this.init_ArrayUtil_BufferHe_ArrayUtil_ConfigSt(reads));
            } else if (reads.init_J != this.open_J) {
               this.flush(key);
            }

            return h;
         }
      }
   }

   private boolean read(int x, int y, int z) {
      if (this.read_Long2ObjectOpenHas.size() <= 2) {
         for (long k : this.read_Long2ObjectOpenHas.keySet()) {
            int dx = BlockPos.getX(k) - x;
            int dy = BlockPos.getY(k) - y;
            int dz = BlockPos.getZ(k) - z;
            if (dx >= -1 && dx <= 1 && dz >= -1 && dz <= 1 && dy >= -2 && dy <= 4) {
               return true;
            }
         }

         return false;
      } else {
         int low = y - 2;
         int bit = low & 63;
         int count = Math.min(7, 64 - bit);
         long lowMask = -1L >>> 64 - count << bit;
         long highMask = count == 7 ? 0L : (1L << 7 - count) - 1L;
         int lowSection = BlockPos.getY(BlockPos.asLong(0, low, 0)) >> 6;
         int highSection = BlockPos.getY(BlockPos.asLong(0, low + count, 0)) >> 6;

         for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
               if ((this.write_Long2LongOpenHashM.get(BlockPos.asLong(x + dx, lowSection, z + dz)) & lowMask) != 0L) {
                  return true;
               }

               if (highMask != 0L && (this.write_Long2LongOpenHashM.get(BlockPos.asLong(x + dx, highSection, z + dz)) & highMask) != 0L) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private double load_D(int x, int y, int z) {
      MutableBlockPos p = new MutableBlockPos();
      double cx = x + 0.5;
      double cz = z + 0.5;
      double top = -1.0 / 0.0;
      double area = 0.0;

      for (int dy = -1; dy <= 0; dy++) {
         p.set(x, y + dy, z);
         BlockState state = this.getBlockState(p);
         if (!state.isAir()) {
            if (state == init_BlockState) {
               return 0.0 / 0.0;
            }

            if (init_Z_BlockState(state)) {
               return 0.0 / 0.0;
            }

            for (AABB b : this.init_List_BlockState_BlockPos(state, p)) {
               if (!(b.maxX + x <= cx - 0.3) && !(b.minX + x >= cx + 0.3) && !(b.maxZ + z <= cz - 0.3) && !(b.minZ + z >= cz + 0.3)) {
                  double bt = b.maxY + y + dy;
                  double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                  double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                  if (bt > top + 1.0E-6) {
                     top = bt;
                     area = ox * oz;
                  } else if (Math.abs(bt - top) <= 1.0E-6) {
                     area += ox * oz;
                  }
               }
            }
         }
      }

      if (top < y || top >= y + 1) {
         return 0.0 / 0.0;
      }

      if (area < 0.03) {
         return 0.0 / 0.0;
      }

      if (!this.init_Z_AABB(PrintGeometry.init(cx, top, cz, false)) && !this.init_Z_AABB(PrintGeometry.init(cx, top, cz, true))) {
         return 0.0 / 0.0;
      }

      for (int dy = 0; dy <= 2; dy++) {
         p.set(x, y + dy, z);
         BlockState state = this.getBlockState(p);
         if (init_Z_BlockState(state) || !state.getFluidState().isEmpty() || state == init_BlockState) {
            return 0.0 / 0.0;
         }
      }

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = 0; dy <= 3; dy++) {
               p.set(x + dx, y + dy, z + dz);
               if (this.getBlockState(p).getFluidState().getType().isSame(Fluids.LAVA)) {
                  return 0.0 / 0.0;
               }
            }
         }
      }

      return top;
   }

   public boolean init_Z_I(int x, int y, int z) {
      return !Double.isNaN(this.init_D_I_2(x, y, z));
   }

   public double init_D_I(int x, int z, double feet) {
      MutableBlockPos p = new MutableBlockPos();
      double cx = x + 0.5;
      double cz = z + 0.5;
      double area = 0.0;
      int cy = (int)Math.floor(feet - 1.0E-6);

      for (int y = cy - 1; y <= cy; y++) {
         p.set(x, y, z);
         BlockState state = this.getBlockState(p);
         if (!state.isAir() && state != init_BlockState) {
            for (AABB b : this.init_List_BlockState_BlockPos(state, p)) {
               double top = b.maxY + y;
               if (!(top > feet + 1.0E-6) && !(top < feet - 0.6)) {
                  double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                  double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                  if (ox > 0.0 && oz > 0.0) {
                     area += ox * oz;
                  }
               }
            }
         }
      }

      return area;
   }

   double init_D_D(double cx, double cz, double feet) {
      double area = 0.0;
      int cy = init_I_D(feet - 1.0E-6);

      for (int x = init_I_D(cx - 0.3); x <= init_I_D(cx + 0.3); x++) {
         for (int z = init_I_D(cz - 0.3); z <= init_I_D(cz + 0.3); z++) {
            for (int y = cy - 1; y <= cy; y++) {
               BlockPos p = new BlockPos(x, y, z);
               BlockState state = this.getBlockState(p);
               if (!state.isAir() && state != init_BlockState) {
                  for (AABB b : this.init_List_BlockState_BlockPos(state, p)) {
                     double top = b.maxY + y;
                     if (!(Math.abs(top - feet) > 1.0E-6)) {
                        double ox = Math.min(b.maxX + x, cx + 0.3) - Math.max(b.minX + x, cx - 0.3);
                        double oz = Math.min(b.maxZ + z, cz + 0.3) - Math.max(b.minZ + z, cz - 0.3);
                        if (ox > 0.0 && oz > 0.0) {
                           area += ox * oz;
                        }
                     }
                  }
               }
            }
         }
      }

      return area;
   }

   public boolean write(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      return state != init_BlockState && state.isCollisionShapeFullBlock(this, pos);
   }

   public boolean update(BlockPos pos) {
      BlockState state = this.getBlockState(pos);
      if (state == init_BlockState) {
         return false;
      } else {
         return state.isAir() ? true : state.getCollisionShape(this, pos).isEmpty() && state.getFluidState().isEmpty() && !init_Z_BlockState(state);
      }
   }

   public boolean sync(BlockPos pos) {
      if (!this.update(pos)) {
         return false;
      }

      BlockState state = this.getBlockState(pos);
      return state.isAir() || state.getShape(this, pos).isEmpty();
   }

   public @Nullable BlockHitResult init_BlockHitResult_Vec3_Vec3(Vec3 eye, Vec3 to) {
      if (this.get == 0) {
         this.run_J++;
         this.tick_J += 24L;
      }

      return read_Z ? this.load(eye, to) : this.read(eye, to);
   }

   @Nullable BlockHitResult load(Vec3 eye, Vec3 to) {
      BlockHitResult hit = this.clip(new ClipContext(eye, to, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, CollisionContext.empty()));
      return hit.getType() == Type.MISS ? null : hit;
   }

   @Nullable BlockHitResult read(Vec3 from, Vec3 to) {
      if (from.equals(to)) {
         return null;
      }

      double toX = Mth.lerp(-1.0E-7, to.x, from.x);
      double toY = Mth.lerp(-1.0E-7, to.y, from.y);
      double toZ = Mth.lerp(-1.0E-7, to.z, from.z);
      double fromX = Mth.lerp(-1.0E-7, from.x, to.x);
      double fromY = Mth.lerp(-1.0E-7, from.y, to.y);
      double fromZ = Mth.lerp(-1.0E-7, from.z, to.z);
      int x = Mth.floor(fromX);
      int y = Mth.floor(fromY);
      int z = Mth.floor(fromZ);
      MutableBlockPos pos = new MutableBlockPos(x, y, z);
      BlockHitResult first = this.init_BlockHitResult_Vec3(from, to, pos);
      if (first != null) {
         return first;
      }

      double dx = toX - fromX;
      double dy = toY - fromY;
      double dz = toZ - fromZ;
      int signX = Mth.sign(dx);
      int signY = Mth.sign(dy);
      int signZ = Mth.sign(dz);
      double tDeltaX = signX == 0 ? 1.7976931348623157E308 : signX / dx;
      double tDeltaY = signY == 0 ? 1.7976931348623157E308 : signY / dy;
      double tDeltaZ = signZ == 0 ? 1.7976931348623157E308 : signZ / dz;
      double tX = tDeltaX * (signX > 0 ? 1.0 - Mth.frac(fromX) : Mth.frac(fromX));
      double tY = tDeltaY * (signY > 0 ? 1.0 - Mth.frac(fromY) : Mth.frac(fromY));
      double tZ = tDeltaZ * (signZ > 0 ? 1.0 - Mth.frac(fromZ) : Mth.frac(fromZ));

      while (tX <= 1.0 || tY <= 1.0 || tZ <= 1.0) {
         if (tX < tY) {
            if (tX < tZ) {
               x += signX;
               tX += tDeltaX;
            } else {
               z += signZ;
               tZ += tDeltaZ;
            }
         } else if (tY < tZ) {
            y += signY;
            tY += tDeltaY;
         } else {
            z += signZ;
            tZ += tDeltaZ;
         }

         BlockHitResult hit = this.init_BlockHitResult_Vec3(from, to, pos.set(x, y, z));
         if (hit != null) {
            return hit;
         }
      }

      return null;
   }

   public List<AABB> load(BlockState state, BlockPos pos) {
      if (load_Z_BlockState(state)) {
         return state.getShape(this, pos).toAabbs();
      }

      List<AABB> boxes = this.load_IdentityHashMap.get(state);
      if (boxes == null) {
         boxes = state.getShape(this, pos).toAabbs();
         this.load_IdentityHashMap.put(state, boxes);
      }

      return boxes;
   }

   private static boolean load_Z_BlockState(BlockState state) {
      Block block = state.getBlock();
      return state.hasOffsetFunction() || block instanceof MovingPistonBlock || block instanceof ShulkerBoxBlock;
   }

   private @Nullable BlockHitResult init_BlockHitResult_Vec3(Vec3 from, Vec3 to, MutableBlockPos cursor) {
      BlockState state = this.getBlockState(cursor);
      if (state.isAir()) {
         return null;
      }

      BlockPos pos = cursor.immutable();
      if (load_Z_BlockState(state)) {
         BlockHitResult result = state.getShape(this, pos).clip(from, to, pos);
         if (result != null) {
            BlockHitResult hitOverride = state.getInteractionShape(this, pos).clip(from, to, pos);
            if (hitOverride != null && hitOverride.getLocation().subtract(from).lengthSqr() < result.getLocation().subtract(from).lengthSqr()) {
               return result.withDirection(hitOverride.getDirection());
            }
         }

         return result;
      } else {
         BlockHitResult result = init(this.load(state, pos), from, to, pos);
         if (result != null) {
            List<AABB> interaction = this.read_IdentityHashMap.get(state);
            if (interaction == null) {
               interaction = state.getInteractionShape(this, pos).toAabbs();
               this.read_IdentityHashMap.put(state, interaction);
            }

            BlockHitResult hitOverride = init(interaction, from, to, pos);
            if (hitOverride != null && hitOverride.getLocation().subtract(from).lengthSqr() < result.getLocation().subtract(from).lengthSqr()) {
               return result.withDirection(hitOverride.getDirection());
            }
         }

         return result;
      }
   }

   private static @Nullable BlockHitResult init(List<AABB> boxes, Vec3 from, Vec3 to, BlockPos pos) {
      if (boxes.isEmpty()) {
         return null;
      }

      Vec3 diff = to.subtract(from);
      if (diff.lengthSqr() < 1.0E-7) {
         return null;
      }

      Vec3 test = from.add(diff.scale(0.001));
      double tx = test.x - pos.getX();
      double ty = test.y - pos.getY();
      double tz = test.z - pos.getZ();

      for (AABB b : boxes) {
         if (b.minX <= tx && tx < b.maxX && b.minY <= ty && ty < b.maxY && b.minZ <= tz && tz < b.maxZ) {
            return new BlockHitResult(test, Direction.getApproximateNearest(diff.x, diff.y, diff.z).getOpposite(), pos, true);
         }
      }

      return AABB.clip(boxes, from, to, pos);
   }

   public @Nullable BlockHitResult write(Vec3 eye, Vec3 to) {
      return this.init_BlockHitResult_Vec3_Vec3(eye, to);
   }

   public AiPhysics.Inner_CachePool init_NodeWalker_CachePo() {
      final PrintView view = this;
      return new AiPhysics.Inner_CachePool() {
         private final Long2ObjectOpenHashMap<List<AABB>> init = new Long2ObjectOpenHashMap<>();

         private List<AABB> init_List_2(int x, int y, int z) {
            return this.init.computeIfAbsent(BlockPos.asLong(x, y, z), key -> {
               BlockPos at = BlockPos.of(key);
               BlockState state = view.getBlockState(at);
               return state.isAir() ? List.of() : view.init_List_BlockState_BlockPos(state, at).stream().map(box -> box.move(at)).toList();
            });
         }

         @Override
         public List<AABB> init_List(AABB query) {
            List<AABB> out = new ArrayList<>();

            for (int x = PrintView.init_I_D(query.minX); x <= PrintView.init_I_D(query.maxX); x++) {
               for (int y = PrintView.init_I_D(query.minY) - 1; y <= PrintView.init_I_D(query.maxY); y++) {
                  for (int z = PrintView.init_I_D(query.minZ); z <= PrintView.init_I_D(query.maxZ); z++) {
                     for (AABB box : this.init_List_2(x, y, z)) {
                        if (box.intersects(query)) {
                           out.add(box);
                        }
                     }
                  }
               }
            }

            return out;
         }

         @Override
         public double init_D_I(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).getBlock().getFriction();
         }

         @Override
         public double load_D(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).getBlock().getSpeedFactor();
         }

         @Override
         public double write_D(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).getBlock().getJumpFactor();
         }

         @Override
         public boolean sync(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).is(BlockTags.CLIMBABLE);
         }

         @Override
         public double read_D(int x, int y, int z) {
            Block block = view.init_BlockState_I(x, y, z).getBlock();
            return block instanceof SlimeBlock ? 1.0 : (block instanceof BedBlock ? 0.75 : 0.0);
         }

         @Override
         public boolean init_Z() {
            return true;
         }

         @Override
         public int init_I(AABB box) {
            int hold = 0;

            for (int x = PrintView.init_I_D(box.minX); x <= PrintView.init_I_D(box.maxX); x++) {
               for (int y = PrintView.init_I_D(box.minY); y <= PrintView.init_I_D(box.maxY); y++) {
                  for (int z = PrintView.init_I_D(box.minZ); z <= PrintView.init_I_D(box.maxZ); z++) {
                     Block block = view.init_BlockState_I(x, y, z).getBlock();
                     if (block instanceof WebBlock) {
                        return 1;
                     }

                     if (block instanceof SweetBerryBushBlock) {
                        hold = 2;
                     }
                  }
               }
            }

            return hold;
         }

         @Override
         public boolean init_Z_I(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).getFluidState().getType().isSame(Fluids.LAVA);
         }

         @Override
         public boolean load_Z(int x, int y, int z) {
            return view.init_BlockState_I(x, y, z).getFluidState().getType().isSame(Fluids.WATER);
         }

         @Override
         public boolean read_Z(int x, int y, int z) {
            return PrintView.init_Z_BlockState(view.init_BlockState_I(x, y, z));
         }
      };
   }

   public static int init_I_D(double v) {
      return (int)Math.floor(v);
   }

   public static Vec3 init_Vec3_BlockPos(BlockPos p) {
      return new Vec3(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
   }

   private record Inner_BufferHelper(long[] cells) {
   }

   private record Inner_CachePool(double feetY, @Nullable List<Vec3> points, PrintView.@Nullable Inner_BufferHelper inputs) {
   }

   private final class Inner_ConfigStore implements AutoCloseable {
      private final long init_J = PrintView.this.open_J;
      final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      private @Nullable IdentityHashMap<PrintView.Inner_BufferHelper, Boolean> init_IdentityHashMap;
      boolean init_Z;
      boolean load;

      Inner_ConfigStore() {
         PrintView.this.load_ArrayList.add(this);
      }

      void init(long key) {
         if (PrintView.this.read_Long2ObjectOpenHas.containsKey(key)) {
            this.load = true;
         }

         if (!this.init_Z && !this.init_LongOpenHashSet.contains(key)) {
            if (this.init_LongOpenHashSet.size() >= 512) {
               this.init_Z = true;
            } else {
               this.init_LongOpenHashSet.add(key);
            }
         }
      }

      boolean init() {
         return !this.load && PrintView.this.open_J == this.init_J;
      }

      boolean load() {
         return !this.init_Z && this.init();
      }

      boolean init_Z(PrintView.Inner_BufferHelper inputs) {
         return this.init_Z || this.init_IdentityHashMap != null && this.init_IdentityHashMap.containsKey(inputs);
      }

      void init_V(PrintView.Inner_BufferHelper inputs) {
         if (!this.init_Z) {
            if (this.init_IdentityHashMap == null) {
               this.init_IdentityHashMap = new IdentityHashMap<>();
            }

            if (this.init_IdentityHashMap.size() < 256) {
               this.init_IdentityHashMap.put(inputs, Boolean.TRUE);
            }
         }
      }

      @Override
      public void close() {
         PrintView.this.load_ArrayList.remove(this);
      }
   }

   private record Inner_DataUtil(BlockState state, boolean temporary) {
   }

   final class Inner_JsonUtil implements AutoCloseable {
      private Inner_JsonUtil() {
         if (PrintView.this.write_Z) {
            throw new IllegalStateException("Nested planner snapshot");
         }

         PrintView.this.update_Long2ObjectOpenHas.clear();
         PrintView.this.write_Z = true;
      }

      @Override
      public void close() {
         PrintView.this.write_Z = false;
         PrintView.this.update_Long2ObjectOpenHas.clear();
      }
   }

   private record Inner_NetHelper(double height, PrintView.@Nullable Inner_BufferHelper inputs) {
   }

   final class Inner_StreamUtil {
      private final Long2ObjectOpenHashMap<PrintView.Inner_StreamUtil.Inner_DataUtil> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
      private final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      private boolean init_Z;

      PrintView.Inner_StreamUtil.Inner_StreamUtil_2 init_ArrayUtil_StreamUt() {
         return new PrintView.Inner_StreamUtil.Inner_StreamUtil_2();
      }

      boolean init_Z() {
         if (this.init_Z) {
            return false;
         }

         boolean previous = PrintView.this.update_Z;
         PrintView.this.update_Z = true;

         try {
            for (Entry<PrintView.Inner_StreamUtil.Inner_DataUtil> entry : this.init_Long2ObjectOpenHas.long2ObjectEntrySet()) {
               if (!entry.getValue().equals(this.init(entry.getLongKey()))) {
                  return !(this.init_Z = true);
               }
            }

            for (long key : this.init_LongOpenHashSet) {
               PrintView.Inner_StreamUtil.Inner_DataUtil now = this.init(key);
               if (now == null) {
                  return !(this.init_Z = true);
               }

               this.init_Long2ObjectOpenHas.putIfAbsent(key, now);
            }

            this.init_LongOpenHashSet.clear();
            return true;
         } finally {
            PrintView.this.update_Z = previous;
         }
      }

      private PrintView.Inner_StreamUtil.Inner_DataUtil init(long key) {
         BlockPos pos = BlockPos.of(key);
         BlockState state = PrintView.this.getBlockState(pos);
         return state == PrintView.init_BlockState
            ? null
            : new PrintView.Inner_StreamUtil.Inner_DataUtil(state, List.copyOf(state.getCollisionShape(PrintView.this, pos).toAabbs()));
      }

      private record Inner_DataUtil(BlockState state, List<AABB> boxes) {
      }

      final class Inner_StreamUtil_2 implements AutoCloseable {
         final boolean init = !PrintView.this.read_ArrayList.contains(Inner_StreamUtil.this);

         Inner_StreamUtil_2() {
            if (this.init) {
               PrintView.this.read_ArrayList.add(Inner_StreamUtil.this);
            }
         }

         @Override
         public void close() {
            if (this.init) {
               PrintView.this.read_ArrayList.remove(Inner_StreamUtil.this);
            }
         }
      }
   }

   final class Inner_TaskQueue implements AutoCloseable {
      private final long init_J = PrintView.this.open_J;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
      final Long2ObjectOpenHashMap<BlockState> load = new Long2ObjectOpenHashMap<>(PrintView.this.read_Long2ObjectOpenHas);
      private final @Nullable LongOpenHashSet init_LongOpenHashSet;
      private @Nullable IdentityHashMap<PrintView.Inner_BufferHelper, Boolean> init_IdentityHashMap;
      boolean init_Z;

      Inner_TaskQueue(boolean retained) {
         this.init_LongOpenHashSet = retained ? new LongOpenHashSet(PrintView.this.load_LongOpenHashSet) : null;
      }

      PrintView.Inner_TaskQueue.Inner_DataUtil init_ArrayUtil_TaskQueu() {
         return new PrintView.Inner_TaskQueue.Inner_DataUtil();
      }

      boolean init_Z() {
         return !this.init_Z && PrintView.this.open_J == this.init_J;
      }

      private void init(BlockPos pos) {
         this.init(pos, null);
      }

      private void init(BlockPos pos, @Nullable BlockState live) {
         long key = pos.asLong();
         if (!this.init_Z && !this.init_Long2ObjectOpenHas.containsKey(key)) {
            if (this.init_Long2ObjectOpenHas.size() >= 8192) {
               this.init_Z = true;
            } else {
               BlockState state = this.load.get(key);
               this.init_Long2ObjectOpenHas.put(key, state != null ? state : (live != null ? live : PrintView.this.init_BlockState_BlockPos(pos)));
            }
         }
      }

      boolean init_Z(PrintView.Inner_BufferHelper inputs) {
         return this.init_Z || this.init_IdentityHashMap != null && this.init_IdentityHashMap.containsKey(inputs);
      }

      void init_V(PrintView.Inner_BufferHelper inputs) {
         if (!this.init_Z) {
            if (this.init_IdentityHashMap == null) {
               this.init_IdentityHashMap = new IdentityHashMap<>();
            }

            if (this.init_IdentityHashMap.size() < 256) {
               this.init_IdentityHashMap.put(inputs, Boolean.TRUE);
            }
         }
      }

      @Override
      public void close() {
         PrintView.this.init_ArrayList.remove(this);
      }

      final class Inner_DataUtil implements AutoCloseable {
         private final boolean init;
         private boolean load;

         private Inner_DataUtil() {
            if (Inner_TaskQueue.this.init_LongOpenHashSet != null
               && PrintView.this.init_Z_Long2ObjectMap(Inner_TaskQueue.this.load)
               && PrintView.this.init_Z_LongSet(Inner_TaskQueue.this.init_LongOpenHashSet)) {
               this.init = !PrintView.this.init_ArrayList.contains(Inner_TaskQueue.this);
               if (this.init) {
                  PrintView.this.init_ArrayList.add(Inner_TaskQueue.this);
               }
            } else {
               throw new IllegalStateException("A retained native receipt requires its original outer hypotheses");
            }
         }

         @Override
         public void close() {
            if (!this.load && this.init) {
               PrintView.this.init_ArrayList.remove(Inner_TaskQueue.this);
            }

            this.load = true;
         }
      }
   }
}
