package autismclient.ai.b;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap.Entry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

final class BufferHelper {
   private static final int init_I = 128;
   private static final int load_I = 8192;
   final UiHelper init_UiHelper;
   final Long2ObjectOpenHashMap<BufferHelper.Inner_StreamUtil> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   final Int2ObjectOpenHashMap<LongOpenHashSet> init_Int2ObjectOpenHash = new Int2ObjectOpenHashMap<>();
   final Long2ObjectOpenHashMap<LongOpenHashSet> load_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   final LongOpenHashSet load_LongOpenHashSet = new LongOpenHashSet();
   final LongOpenHashSet read_LongOpenHashSet = new LongOpenHashSet();
   long init_J;
   long load_J;
   long read_J;
   private long write;
   private long update;
   private long sync;
   private boolean init_Z;
   private final LinkedHashMap<Long, BufferHelper.Inner_CachePool> init_LinkedHashMap = new LinkedHashMap<>(16, 0.75F, true);

   BufferHelper(UiHelper core) {
      this.init_UiHelper = core;
   }

   void init(long key, int owner) {
      BufferHelper.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.get(key);
      if (entry != null && entry.init_I == owner) {
         if (this.init_UiHelper.init_EventBus.update_Z()) {
            entry.init_Z = true;
         }
      } else {
         if (entry != null) {
            this.load(key);
         }

         this.init_Long2ObjectOpenHas.put(key, new BufferHelper.Inner_StreamUtil(key, owner));
         this.init_Long2ObjectOpenHas.get(key).init_Z = this.init_UiHelper.init_EventBus.update_Z();
         this.init_Long2ObjectOpenHas.get(key).read = this.write;
         this.init_Int2ObjectOpenHash.computeIfAbsent(owner, ignored -> new LongOpenHashSet()).add(key);
         this.init_V_J(key);
         this.init_J++;
      }
   }

   void init_V_J(long key) {
      BufferHelper.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.get(key);
      if (entry != null && this.init_UiHelper.init_EventBus.update_Z()) {
         entry.init_Z = true;
      }

      if (entry != null && this.init_Z_BufferHelper_Strea(entry)) {
         this.init_LongOpenHashSet.add(key);
         this.init_J++;
      }
   }

   void init_V_I(int owner) {
      LongOpenHashSet keys = this.init_Int2ObjectOpenHash.get(owner);
      if (keys != null) {
         for (long key : keys) {
            this.init_LongOpenHashSet.add(key);
         }

         this.init_J++;
      }
   }

   void load(long key) {
      BufferHelper.Inner_StreamUtil entry = this.init_Long2ObjectOpenHas.remove(key);
      if (entry != null) {
         this.init_V_BufferHelper_Strea(entry);
         LongOpenHashSet keys = this.init_Int2ObjectOpenHash.get(entry.init_I);
         if (keys != null && keys.remove(key) && keys.isEmpty()) {
            this.init_Int2ObjectOpenHash.remove(entry.init_I);
         }

         this.init_LongOpenHashSet.remove(key);
         this.load_LongOpenHashSet.remove(key);
         this.read_LongOpenHashSet.remove(key);
         this.init_J++;
      }
   }

   void read(long key) {
      LongOpenHashSet keys = this.load_Long2ObjectOpenHas.get(key);
      if (keys != null) {
         this.init_LongOpenHashSet.addAll(keys);
         this.init_J++;
      }
   }

   void init() {
      this.update++;
      this.init_J++;
   }

   private boolean init_Z_BufferHelper_Strea(BufferHelper.Inner_StreamUtil entry) {
      return entry.init_I >= 0
         && entry.init_I < this.init_UiHelper.init_BArr.length
         && this.init_UiHelper.init_BArr[entry.init_I] == 3
         && this.init_UiHelper.init_Long2ObjectOpenHas.containsKey(entry.init_J);
   }

   private void init_V_BufferHelper_Strea(BufferHelper.Inner_StreamUtil entry) {
      if (entry.init_BufferHelper_DataU != null) {
         for (long key : entry.init_BufferHelper_DataU.reads.keySet()) {
            LongOpenHashSet keys = this.load_Long2ObjectOpenHas.get(key);
            if (keys != null && keys.remove(entry.init_J) && keys.isEmpty()) {
               this.load_Long2ObjectOpenHas.remove(key);
            }
         }
      }
   }

   private void init(BufferHelper.Inner_StreamUtil entry, BufferHelper.Inner_DataUtil certificate) {
      this.init_V_BufferHelper_Strea(entry);
      entry.init_BufferHelper_DataU = certificate;

      for (long key : certificate.reads.keySet()) {
         this.load_Long2ObjectOpenHas.computeIfAbsent(key, ignored -> new LongOpenHashSet()).add(entry.init_J);
      }

      this.init_LongOpenHashSet.remove(entry.init_J);
      this.read_LongOpenHashSet.remove(entry.init_J);
      if (certificate.stable) {
         this.load_LongOpenHashSet.remove(entry.init_J);
      } else {
         this.load_LongOpenHashSet.add(entry.init_J);
      }
   }

   BufferHelper.Inner_CachePool init_BufferHelper_Cache(BlockPos position, BlockState state) {
      if (this.init_Z != this.init_UiHelper.init_EventBus.update_Z()) {
         this.init_Z = this.init_UiHelper.init_EventBus.update_Z();
         if (this.init_Z) {
            this.update++;
            this.write++;
         }

         this.init_J++;
      }

      return new BufferHelper.Inner_CachePool(position, state);
   }

   boolean init_Z(BlockPos position, BlockState state) {
      if (this.init_Long2ObjectOpenHas.isEmpty()) {
         return false;
      }

      BufferHelper.Inner_CachePool guard = this.init_LinkedHashMap.get(position.asLong());
      if (guard == null || guard.init_BlockState != state || !guard.init()) {
         guard = this.init_BufferHelper_Cache(position, state);
         if (this.init_LinkedHashMap.size() >= 64) {
            this.init_LinkedHashMap.remove(this.init_LinkedHashMap.keySet().iterator().next());
         }

         this.init_LinkedHashMap.put(position.asLong(), guard);
      }

      if (!guard.update && !this.init_UiHelper.poll_Z()) {
         guard.init(this.init_UiHelper.write_J());
      }

      return guard.flush || !guard.update || guard.sync;
   }

   private static boolean init_Z_BlockState(BlockState state) {
      Block block = state.getBlock();
      return block instanceof FenceBlock
         || block instanceof WallBlock
         || block instanceof StairBlock
         || block instanceof IronBarsBlock
         || block instanceof FenceGateBlock
         || block instanceof ChestBlock;
   }

   final class Inner_BufferHelper {
      final long init_J;
      final BufferHelper.Inner_DataUtil init_BufferHelper_DataU;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
      final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
      LongIterator init_LongIterator;
      final ArrayUtil.Inner_StreamUtil init_ArrayUtil_StreamUt = BufferHelper.this.init_UiHelper.init_ArrayUtil.new Inner_StreamUtil();
      int init_I;
      int load_I;
      Vec3 init_Vec3;
      boolean init_Z;
      boolean load_Z;
      boolean read;
      boolean write = true;

      Inner_BufferHelper(long key, BufferHelper.Inner_DataUtil certificate) {
         this.init_J = key;
         this.init_BufferHelper_DataU = certificate;
         BufferHelper.this.read_J++;
      }

      BufferHelper.Inner_DataUtil init() {
         return new BufferHelper.Inner_DataUtil(
            this.init_BufferHelper_DataU.point,
            this.init_BufferHelper_DataU.before,
            this.init_BufferHelper_DataU.after,
            this.init_Long2ObjectOpenHas,
            this.init_LongOpenHashSet,
            this.write
         );
      }

      void init(long allowance) {
         if (!this.init_Z) {
            if (!this.init_ArrayUtil_StreamUt.init_Z()) {
               this.read = this.init_Z = true;
            } else {
               try {
                  this.load(allowance);
               } finally {
                  if (!this.init_ArrayUtil_StreamUt.init_Z()) {
                     this.load_Z = false;
                     this.read = this.init_Z = true;
                  }
               }
            }
         }
      }

      private void load(long allowance) {
         long started = BufferHelper.this.init_UiHelper.init_ArrayUtil.sync();
         boolean dependencyOnly = this.init_I == 3;

         try (
            ArrayUtil.Inner_TaskQueue capture = BufferHelper.this.init_UiHelper.init_ArrayUtil.init_ArrayUtil_TaskQueu();
            ArrayUtil.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.init_ArrayUtil_StreamUt();
         ) {
            try {
               for (int steps = 0; steps < 128 && BufferHelper.this.init_UiHelper.init_ArrayUtil.sync() - started < Math.max(1L, allowance); steps++) {
                  if (this.init_I == 3) {
                     if (this.init_LongIterator == null) {
                        this.init_LongIterator = this.init_LongOpenHashSet.iterator();
                     }

                     if (!this.init_LongIterator.hasNext()) {
                        this.load_Z = BufferHelper.this.init_UiHelper.load_Z_BlockPos(BlockPos.of(this.init_J));
                        this.init_Z = true;
                        return;
                     }

                     BufferHelper.this.init_UiHelper.init_ArrayUtil.getBlockState(BlockPos.of(this.init_LongIterator.nextLong()));
                  } else if (this.init_I == 0) {
                     BlockPos pos = BlockPos.of(this.init_J);
                     if (!BufferHelper.this.init_UiHelper.init_EventBus.load_Z(pos)
                        || !BufferHelper.this.init_UiHelper.init_Long2ObjectOpenHas.containsKey(this.init_J)
                        || !TextFormat.load(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_BufferHelper_DataU.point)
                        || !BufferHelper.this.init_UiHelper.load_Z_BlockPos(pos)) {
                        this.init_Z = true;
                        return;
                     }

                     ListPool target = new ListPool(
                        -1, pos, BufferHelper.this.init_UiHelper.init_ArrayUtil.getBlockState(pos), ListPool.Inner_DataUtil.init, Items.AIR, null
                     );
                     if (ConfigStore.init(
                           BufferHelper.this.init_UiHelper.init_ArrayUtil,
                           BufferHelper.this.init_UiHelper.init_EventBus,
                           BufferHelper.this.init_UiHelper.init_PathHelper,
                           target,
                           Items.DIAMOND_PICKAXE,
                           ConfigStore.Inner_CachePool.update,
                           this.init_BufferHelper_DataU.point,
                           false,
                           false,
                           new ConfigStore.Inner_DataUtil(2000),
                           4.1000000000000005
                        )
                        == null) {
                        this.init_Z = true;
                        return;
                     }

                     this.init_I = 1;
                     this.init_Vec3 = this.init_BufferHelper_DataU.point;
                  } else {
                     TextFormat.Inner_BufferHelper route = this.init_I == 1 ? this.init_BufferHelper_DataU.before : this.init_BufferHelper_DataU.after;
                     boolean removed = this.init_I == 2;
                     if (removed) {
                        BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos_BlockState(BlockPos.of(this.init_J), Blocks.AIR.defaultBlockState());
                     }

                     try {
                        if (this.load_I < route.points().size()) {
                           Vec3 next = route.points().get(this.load_I++);
                           if (!TextFormat.load(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_Vec3)
                              || !TextFormat.init_Z(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_Vec3, next)) {
                              this.init_Z = true;
                              return;
                           }

                           this.init_Vec3 = next;
                        } else {
                           if (this.init_I != 1) {
                              this.init_I = 3;
                              return;
                           }

                           this.init_I = 2;
                           this.load_I = 0;
                           this.init_Vec3 = this.init_BufferHelper_DataU.point;
                        }
                     } finally {
                        if (removed) {
                           BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos(BlockPos.of(this.init_J));
                        }
                     }
                  }
               }
            } finally {
               this.write = this.write & capture.init_Z();

               for (Entry<BlockState> read : capture.init_Long2ObjectOpenHas.long2ObjectEntrySet()) {
                  if (this.init_Long2ObjectOpenHas.size() >= 8192 && !this.init_Long2ObjectOpenHas.containsKey(read.getLongKey())) {
                     this.write = false;
                     break;
                  }

                  BlockState prior = this.init_Long2ObjectOpenHas.putIfAbsent(read.getLongKey(), read.getValue());
                  if (read.getValue() == ArrayUtil.init_BlockState || prior != null && prior != read.getValue()) {
                     this.load_Z = false;
                     this.read = this.init_Z = true;
                  }

                  if (!dependencyOnly && BufferHelper.init_Z_BlockState(read.getValue())) {
                     for (Direction direction : Direction.values()) {
                        if (this.init_LongOpenHashSet.size() >= 8192) {
                           this.write = false;
                           break;
                        }

                        this.init_LongOpenHashSet.add(BlockPos.offset(read.getLongKey(), direction));
                     }
                  }
               }
            }
         }
      }
   }

   final class Inner_CachePool {
      final BlockPos init_BlockPos;
      final BlockState init_BlockState;
      final long init_J = BufferHelper.this.init_UiHelper.init_ArrayUtil.read_J();
      final long load_J = BufferHelper.this.init_J;
      final long read_J = BufferHelper.this.update;
      final boolean init_Z = BufferHelper.this.init_UiHelper.init_EventBus.update_Z();
      final boolean load_Z;
      final ArrayUtil.Inner_StreamUtil init_ArrayUtil_StreamUt;
      final Long2ObjectOpenHashMap<BlockState> init_Long2ObjectOpenHas;
      final LongOpenHashSet init_LongOpenHashSet;
      final LongOpenHashSet load_LongOpenHashSet;
      final LongArrayList init_LongArrayList;
      final boolean read_Z;
      LongIterator init_LongIterator;
      LongIterator load_LongIterator;
      int init_I;
      int load_I;
      int read_I;
      BufferHelper.Inner_StreamUtil init_BufferHelper_Strea;
      BufferHelper.Inner_ConfigStore init_BufferHelper_Confi;
      BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe;
      BufferHelper.Inner_DataUtil init_BufferHelper_DataU;
      boolean write;
      boolean update;
      boolean sync;
      boolean flush;

      Inner_CachePool(BlockPos position, BlockState state) {
         this.load_Z = BufferHelper.this.init_Long2ObjectOpenHas.isEmpty();
         this.init_ArrayUtil_StreamUt = BufferHelper.this.init_UiHelper.init_ArrayUtil.new Inner_StreamUtil();
         this.init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
         this.init_LongOpenHashSet = new LongOpenHashSet();
         this.load_LongOpenHashSet = new LongOpenHashSet();
         this.init_LongArrayList = new LongArrayList();
         this.init_BlockPos = position;
         this.init_BlockState = state;
         BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_Long2ObjectMap(this.init_Long2ObjectOpenHas);
         BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_LongSet(this.init_LongOpenHashSet);
         this.read_Z = this.init_Long2ObjectOpenHas.isEmpty() && this.init_LongOpenHashSet.isEmpty();
         this.update = this.load_Z;
      }

      boolean init() {
         return !this.flush
            && this.load_J == BufferHelper.this.init_J
            && (
               this.load_Z
                  || this.init_J == BufferHelper.this.init_UiHelper.init_ArrayUtil.read_J()
                     && this.read_J == BufferHelper.this.update
                     && this.init_Z == BufferHelper.this.init_UiHelper.init_EventBus.update_Z()
                     && BufferHelper.this.init_UiHelper.init_ArrayUtil.init_Z_Long2ObjectMap(this.init_Long2ObjectOpenHas)
                     && BufferHelper.this.init_UiHelper.init_ArrayUtil.init_Z_LongSet(this.init_LongOpenHashSet)
                     && this.init_ArrayUtil_StreamUt.init_Z()
            );
      }

      private boolean init(BufferHelper.Inner_DataUtil certificate) {
         for (long key : this.init_Long2ObjectOpenHas.keySet()) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         for (long key : this.init_LongOpenHashSet) {
            if (certificate.reads.containsKey(key)) {
               return true;
            }
         }

         return false;
      }

      void init(long allowance) {
         if (!this.init()) {
            this.flush = true;
         } else if (!this.update) {
            try (ArrayUtil.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.init_ArrayUtil_StreamUt()) {
               this.load(allowance);
            } finally {
               if (!this.init_ArrayUtil_StreamUt.init_Z()) {
                  this.flush = true;
               }
            }
         }
      }

      private void load(long allowance) {
         long started = BufferHelper.this.init_UiHelper.init_ArrayUtil.sync();
         long deadline = System.nanoTime() + 2000000L;

         for (int steps = 0;
            steps < 128
               && BufferHelper.this.init_UiHelper.init_ArrayUtil.sync() - started < Math.max(1L, allowance)
               && (steps == 0 || System.nanoTime() < deadline);
            steps++
         ) {
            long left = Math.min(8192L, Math.max(1L, allowance - (BufferHelper.this.init_UiHelper.init_ArrayUtil.sync() - started)));
            if (this.init_I < 6) {
               if (this.load_LongIterator != null) {
                  if (!this.load_LongIterator.hasNext()) {
                     this.load_LongIterator = null;
                  } else {
                     long key = this.load_LongIterator.nextLong();
                     if (this.load_LongOpenHashSet.add(key)) {
                        this.init_LongArrayList.add(key);
                     }
                  }
               } else {
                  if (this.init_LongIterator == null) {
                     LongIterator var10001;
                     switch (this.init_I) {
                        case 0:
                           var10001 = (BufferHelper.this.sync != this.read_J
                                 ? BufferHelper.this.init_Long2ObjectOpenHas.keySet()
                                 : BufferHelper.this.init_LongOpenHashSet)
                              .iterator();
                           break;
                        case 1:
                           var10001 = BufferHelper.this.load_Long2ObjectOpenHas.getOrDefault(this.init_BlockPos.asLong(), new LongOpenHashSet()).iterator();
                           break;
                        case 2:
                           var10001 = BufferHelper.this.load_LongOpenHashSet.iterator();
                           break;
                        case 3:
                           var10001 = BufferHelper.this.read_LongOpenHashSet.iterator();
                           break;
                        case 4:
                           var10001 = this.init_Long2ObjectOpenHas.keySet().iterator();
                           break;
                        default:
                           var10001 = this.init_LongOpenHashSet.iterator();
                     }

                     this.init_LongIterator = var10001;
                  }

                  if (!this.init_LongIterator.hasNext()) {
                     this.init_LongIterator = null;
                     this.init_I++;
                  } else {
                     long key = this.init_LongIterator.nextLong();
                     if (this.init_I >= 4) {
                        LongOpenHashSet linked = BufferHelper.this.load_Long2ObjectOpenHas.get(key);
                        if (linked != null) {
                           this.load_LongIterator = linked.iterator();
                        }
                     } else if (this.load_LongOpenHashSet.add(key)) {
                        this.init_LongArrayList.add(key);
                     }
                  }
               }
            } else {
               if (this.init_BufferHelper_Strea == null) {
                  if (this.load_I >= this.init_LongArrayList.size()) {
                     if (this.read_Z) {
                        BufferHelper.this.sync = this.read_J;
                     }

                     this.update = true;
                     return;
                  }

                  this.init_BufferHelper_Strea = BufferHelper.this.init_Long2ObjectOpenHas.get(this.init_LongArrayList.getLong(this.load_I++));
                  if (this.init_BufferHelper_Strea == null
                     || !BufferHelper.this.init_Z_BufferHelper_Strea(this.init_BufferHelper_Strea)
                     || this.init_BufferHelper_Strea.init_J == this.init_BlockPos.asLong()) {
                     this.init_BufferHelper_Strea = null;
                     continue;
                  }

                  if (this.init_Z) {
                     this.init_BufferHelper_Strea.init_Z = true;
                  }

                  this.init_BufferHelper_DataU = this.init_BufferHelper_Strea.init_BufferHelper_DataU;
                  this.write = this.init_BufferHelper_DataU != null
                     && (
                        !this.init_BufferHelper_DataU.stable
                           || this.init_BufferHelper_DataU.reads.containsKey(this.init_BlockPos.asLong())
                           || this.init(this.init_BufferHelper_DataU)
                     );
                  if (!this.init_Z
                     && this.init_BufferHelper_DataU == null
                     && !this.init_BufferHelper_Strea.init_Z
                     && this.init_BufferHelper_Strea.read >= BufferHelper.this.write) {
                     this.init_BufferHelper_Strea = null;
                     continue;
                  }

                  if (this.init_BufferHelper_DataU == null
                     && this.init_BufferHelper_Strea.load == this.init_J
                     && !BufferHelper.this.init_LongOpenHashSet.contains(this.init_BufferHelper_Strea.init_J)) {
                     this.sync = this.update = true;
                     BufferHelper.this.init_UiHelper.pop_LongOpenHashSet.add(this.init_BufferHelper_Strea.init_J);
                     return;
                  }

                  this.read_I = 0;
                  if (this.init_BufferHelper_DataU != null) {
                     this.init_BufferHelper_Buffe = BufferHelper.this.new Inner_BufferHelper(this.init_BufferHelper_Strea.init_J, this.init_BufferHelper_DataU);
                  } else {
                     this.init_BufferHelper_Confi = BufferHelper.this.new Inner_ConfigStore(this.init_BufferHelper_Strea.init_J, null);
                  }
               }

               if (this.read_I == 0) {
                  if (this.init_BufferHelper_Buffe != null) {
                     this.init_BufferHelper_Buffe.init(left);
                     if (this.init_BufferHelper_Buffe.read) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Buffe.init_Z) {
                        continue;
                     }

                     if (this.init_BufferHelper_Buffe.load_Z) {
                        this.init_BufferHelper_DataU = this.init_BufferHelper_Buffe.init();
                     } else {
                        this.init_BufferHelper_Confi = BufferHelper.this.new Inner_ConfigStore(
                           this.init_BufferHelper_Strea.init_J, this.init_BufferHelper_DataU.point
                        );
                        this.init_BufferHelper_DataU = null;
                     }

                     this.init_BufferHelper_Buffe = null;
                  }

                  if (this.init_BufferHelper_Confi != null) {
                     this.init_BufferHelper_Confi.init(left);
                     if (this.init_BufferHelper_Confi.read_Z) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Confi.init_Z) {
                        continue;
                     }

                     this.init_BufferHelper_DataU = this.init_BufferHelper_Confi.init_BufferHelper_DataU;
                     if (this.init_BufferHelper_DataU == null) {
                        if (this.read_Z && !this.init_BufferHelper_Confi.load_Z) {
                           this.init_BufferHelper_Strea.load = this.init_J;
                           BufferHelper.this.read_LongOpenHashSet.add(this.init_BufferHelper_Strea.init_J);
                           BufferHelper.this.init_LongOpenHashSet.remove(this.init_BufferHelper_Strea.init_J);
                        }

                        this.sync = this.update = true;
                        BufferHelper.this.init_UiHelper.pop_LongOpenHashSet.add(this.init_BufferHelper_Strea.init_J);
                        return;
                     }

                     this.init_BufferHelper_Confi = null;
                  }

                  if (this.read_Z) {
                     BufferHelper.this.init(this.init_BufferHelper_Strea, this.init_BufferHelper_DataU);
                  }

                  if (BufferHelper.this.init_UiHelper.init_ArrayUtil.getBlockState(this.init_BlockPos) != this.init_BlockState
                     && (
                        this.init_BufferHelper_DataU.neighbors.contains(this.init_BlockPos.asLong())
                           || !this.init_BufferHelper_DataU.stable && this.init_BufferHelper_DataU.reads.size() >= 8192
                     )) {
                     this.sync = this.update = true;
                     BufferHelper.this.init_UiHelper.pop_LongOpenHashSet.add(this.init_BufferHelper_Strea.init_J);
                     return;
                  }

                  if (this.init_BufferHelper_DataU.stable && !this.init_BufferHelper_DataU.reads.containsKey(this.init_BlockPos.asLong())) {
                     this.init_BufferHelper_Strea = null;
                  } else {
                     this.read_I = 1;
                     this.init_BufferHelper_Buffe = BufferHelper.this.new Inner_BufferHelper(this.init_BufferHelper_Strea.init_J, this.init_BufferHelper_DataU);
                  }
               } else {
                  BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos_BlockState(this.init_BlockPos, this.init_BlockState);

                  try {
                     if (this.init_BufferHelper_Buffe != null) {
                        this.init_BufferHelper_Buffe.init(left);
                        if (!this.init_BufferHelper_Buffe.read) {
                           if (this.init_BufferHelper_Buffe.init_Z) {
                              if (this.init_BufferHelper_Buffe.load_Z) {
                                 this.init_BufferHelper_Buffe = null;
                                 this.init_BufferHelper_Strea = null;
                              } else {
                                 this.init_BufferHelper_Buffe = null;
                                 this.init_BufferHelper_Confi = BufferHelper.this.new Inner_ConfigStore(
                                    this.init_BufferHelper_Strea.init_J, this.init_BufferHelper_DataU.point
                                 );
                              }
                           }
                           continue;
                        }

                        this.flush = true;
                        return;
                     }

                     this.init_BufferHelper_Confi.init(left);
                     if (this.init_BufferHelper_Confi.read_Z) {
                        this.flush = true;
                        return;
                     }

                     if (!this.init_BufferHelper_Confi.init_Z) {
                        continue;
                     }

                     if (this.init_BufferHelper_Confi.init_BufferHelper_DataU != null) {
                        this.init_BufferHelper_Confi = null;
                        this.init_BufferHelper_Strea = null;
                        continue;
                     }

                     this.sync = this.update = true;
                     BufferHelper.this.init_UiHelper.pop_LongOpenHashSet.add(this.init_BufferHelper_Strea.init_J);
                  } finally {
                     BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos(this.init_BlockPos);
                  }

                  return;
               }
            }
         }
      }
   }

   final class Inner_ConfigStore {
      final long init_J;
      final ListPool init_ListPool;
      final ArrayList<Long> init_ArrayList = new ArrayList<>();
      final ConfigStore.Inner_DataUtil init_ConfigStore_DataUt = new ConfigStore.Inner_DataUtil(18000);
      List<Vec3> init_List;
      Vec3 init_Vec3;
      UiHelper.h init_UiHelper_h;
      UiHelper.h load_UiHelper_h;
      BufferHelper.Inner_BufferHelper init_BufferHelper_Buffe;
      int init_I;
      int load_I;
      int read_I;
      final ArrayUtil.Inner_StreamUtil init_ArrayUtil_StreamUt = BufferHelper.this.init_UiHelper.init_ArrayUtil.new Inner_StreamUtil();
      boolean init_Z;
      boolean load_Z;
      boolean read_Z;
      boolean write;
      BufferHelper.Inner_DataUtil init_BufferHelper_DataU;

      Inner_ConfigStore(long key, Vec3 preferred) {
         this.init_J = key;
         BlockPos pos = BlockPos.of(key);
         this.init_ListPool = new ListPool(
            -1, pos, BufferHelper.this.init_UiHelper.init_ArrayUtil.getBlockState(pos), ListPool.Inner_DataUtil.init, Items.AIR, null
         );
         this.init_ArrayList.addAll(PacketUtil.init(pos));
         this.init_ArrayList.sort(Comparator.<Long>comparingDouble(k -> BlockPos.of(k).distToCenterSqr(Vec3.atCenterOf(pos))).thenComparingLong(k -> k));
         if (preferred == null) {
            UiHelper.at recovery = BufferHelper.this.init_UiHelper.init_LinkedHashMap.get(key);
            if (recovery != null) {
               preferred = recovery.work();
            }
         }

         if (preferred != null) {
            this.init_List = List.of(preferred);
         }

         BufferHelper.this.load_J++;
      }

      void init(long allowance) {
         if (!this.init_ArrayUtil_StreamUt.init_Z()) {
            this.read_Z = this.init_Z = true;
         } else {
            long transientBefore = BufferHelper.this.init_UiHelper.init_ArrayUtil.init_J();

            try (ArrayUtil.Inner_StreamUtil.Inner_StreamUtil_2 geometry = this.init_ArrayUtil_StreamUt.init_ArrayUtil_StreamUt()) {
               this.load(allowance);
            } finally {
               this.load_Z = this.load_Z | BufferHelper.this.init_UiHelper.init_ArrayUtil.init_J() != transientBefore;
               if (!this.init_ArrayUtil_StreamUt.init_Z()) {
                  this.read_Z = this.init_Z = true;
                  this.init_BufferHelper_DataU = null;
               }
            }
         }
      }

      private void load(long allowance) {
         long started = BufferHelper.this.init_UiHelper.init_ArrayUtil.sync();
         if (!this.write) {
            this.write = true;
            if (!BufferHelper.this.init_UiHelper.load_Z_BlockPos(this.init_ListPool.pos())) {
               this.init_Z = true;
               return;
            }
         }

         for (int steps = 0; !this.init_Z && steps < 128 && BufferHelper.this.init_UiHelper.init_ArrayUtil.sync() - started < Math.max(1L, allowance); steps++) {
            long left = Math.max(1L, allowance - (BufferHelper.this.init_UiHelper.init_ArrayUtil.sync() - started));
            if (this.init_BufferHelper_Buffe != null) {
               this.init_BufferHelper_Buffe.init(left);
               if (this.init_BufferHelper_Buffe.read) {
                  this.read_Z = this.init_Z = true;
                  return;
               }

               if (!this.init_BufferHelper_Buffe.init_Z) {
                  return;
               }

               if (this.init_BufferHelper_Buffe.load_Z) {
                  this.init_BufferHelper_DataU = this.init_BufferHelper_Buffe.init();
                  this.init_Z = true;
                  return;
               }

               this.init_BufferHelper_Buffe = null;
               this.init_Vec3 = null;
               this.init_UiHelper_h = this.load_UiHelper_h = null;
               this.read_I = 0;
            }

            if (this.init_Vec3 == null) {
               if (this.init_List == null || this.load_I >= this.init_List.size()) {
                  if (this.init_I >= this.init_ArrayList.size() || this.init_ConfigStore_DataUt.init()) {
                     this.load_Z = this.load_Z | this.init_ConfigStore_DataUt.init();
                     this.init_Z = true;
                     return;
                  }

                  long at = this.init_ArrayList.get(this.init_I++);
                  if (BufferHelper.this.init_UiHelper.init_ArrayUtil.getBlockState(BlockPos.of(at)) == ArrayUtil.init_BlockState) {
                     this.load_Z = true;
                  }

                  this.init_List = PacketUtil.init(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_ListPool, ConfigStore.Inner_CachePool.update, at);
                  this.load_I = 0;
                  continue;
               }

               this.init_Vec3 = this.init_List.get(this.load_I++);
               if (!TextFormat.load(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_Vec3)) {
                  this.init_Vec3 = null;
                  continue;
               }

               ConfigStore.Inner_StreamUtil click = ConfigStore.init(
                  BufferHelper.this.init_UiHelper.init_ArrayUtil,
                  BufferHelper.this.init_UiHelper.init_EventBus,
                  BufferHelper.this.init_UiHelper.init_PathHelper,
                  this.init_ListPool,
                  Items.DIAMOND_PICKAXE,
                  ConfigStore.Inner_CachePool.update,
                  this.init_Vec3,
                  false,
                  false,
                  this.init_ConfigStore_DataUt,
                  4.1000000000000005
               );
               if (click == null) {
                  this.init_Vec3 = null;
                  continue;
               }

               this.init_UiHelper_h = BufferHelper.this.init_UiHelper.new h(this.init_Vec3);
               this.read_I = 1;
            }

            if (this.read_I == 1) {
               this.init_UiHelper_h.init(left);
               if (!this.init_UiHelper_h.init_Z) {
                  return;
               }

               if (this.init_UiHelper_h.init_TextFormat_BufferH == null) {
                  this.load_Z = this.load_Z | this.init(this.init_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = null;
               } else {
                  this.read_I = 2;
               }
            } else {
               BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos_BlockState(this.init_ListPool.pos(), Blocks.AIR.defaultBlockState());

               try {
                  if (!TextFormat.load(BufferHelper.this.init_UiHelper.init_ArrayUtil, this.init_Vec3)) {
                     this.init_Vec3 = null;
                     this.init_UiHelper_h = this.load_UiHelper_h = null;
                     this.read_I = 0;
                     continue;
                  }

                  if (this.load_UiHelper_h == null) {
                     this.load_UiHelper_h = BufferHelper.this.init_UiHelper.new h(this.init_Vec3);
                  }

                  this.load_UiHelper_h.init(left);
                  if (!this.load_UiHelper_h.init_Z) {
                     return;
                  }
               } finally {
                  BufferHelper.this.init_UiHelper.init_ArrayUtil.init_V_BlockPos(this.init_ListPool.pos());
               }

               if (this.load_UiHelper_h.init_TextFormat_BufferH != null) {
                  this.init_BufferHelper_Buffe = BufferHelper.this.new Inner_BufferHelper(
                     this.init_J,
                     new BufferHelper.Inner_DataUtil(
                        this.init_Vec3,
                        this.init_UiHelper_h.init_TextFormat_BufferH,
                        this.load_UiHelper_h.init_TextFormat_BufferH,
                        new Long2ObjectOpenHashMap<>(),
                        new LongOpenHashSet(),
                        false
                     )
                  );
               } else {
                  this.load_Z = this.load_Z | this.init(this.load_UiHelper_h);
                  this.init_Vec3 = null;
                  this.init_UiHelper_h = this.load_UiHelper_h = null;
                  this.read_I = 0;
               }
            }
         }
      }

      private boolean init(UiHelper.h exit) {
         return exit.init_TextFormat_ConfigS != null
            && exit.init_TextFormat_ConfigS.init_TextFormat_BufferH == null
            && !exit.init_TextFormat_ConfigS.init_PriorityQueue.isEmpty()
            && exit.init_TextFormat_ConfigS.load_I >= exit.init_TextFormat_ConfigS.init_I;
      }
   }

   record Inner_DataUtil(
      Vec3 point,
      TextFormat.Inner_BufferHelper before,
      TextFormat.Inner_BufferHelper after,
      Long2ObjectOpenHashMap<BlockState> reads,
      LongOpenHashSet neighbors,
      boolean stable
   ) {
   }

   static final class Inner_StreamUtil {
      final long init_J;
      int init_I;
      BufferHelper.Inner_DataUtil init_BufferHelper_DataU;
      long load = -9223372036854775808L;
      long read;
      boolean init_Z;

      Inner_StreamUtil(long key, int owner) {
         this.init_J = key;
         this.init_I = owner;
      }
   }
}
