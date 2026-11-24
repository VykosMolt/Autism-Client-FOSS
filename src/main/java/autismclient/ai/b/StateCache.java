package autismclient.ai.b;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public final class StateCache {
   private final EventBus init_EventBus;
   private final Function<BlockPos, BlockState> init_Function;
   private final Object init_Obj;
   private final LongOpenHashSet init_LongOpenHashSet = new LongOpenHashSet();
   private final Long2ObjectOpenHashMap<StateCache.Inner_DataUtil> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectOpenHashMap<LongOpenHashSet> load = new Long2ObjectOpenHashMap<>();
   private final LongLinkedOpenHashSet init_LongLinkedOpenHash = new LongLinkedOpenHashSet();
   private final Long2ObjectLinkedOpenHashMap<BitSetUtil> init_Long2ObjectLinkedO = new Long2ObjectLinkedOpenHashMap<>();
   private long init_J = -9223372036854775808L;

   public StateCache(EventBus env, Function<BlockPos, BlockState> planned) {
      this.init_EventBus = env;
      this.init_Function = planned;
      this.init_Obj = env.init_Obj();
   }

   public void init(BitSetUtil job) {
      this.init_LongOpenHashSet.add(job.position().asLong());
      job.init(this);
   }

   public boolean init_Z(BlockPos at) {
      return this.init_LongOpenHashSet.contains(at.asLong());
   }

   public boolean init_Z() {
      return this.init_LongOpenHashSet.isEmpty();
   }

   public void load(BitSetUtil job) {
      long key = job.position().asLong();
      this.init_Long2ObjectLinkedO.remove(key);
      if (!job.load_Z() || !this.init_EventBus.load_Z(job.position()) || !job.matchesSource(this.init_EventBus.init_BlockState_BlockPos(job.position()))) {
         this.init_V(job.position());
      } else if (this.read()) {
         StateCache.Inner_DataUtil previous = this.init_Long2ObjectOpenHas.get(key);
         if (previous == null || !this.init(previous, this.init_EventBus.init_BlockState_BlockPos(job.position()))) {
            this.init_V(job.position());
            if (this.read()) {
               LongOpenHashSet dependencies = new LongOpenHashSet();
               if (this.init(job.position(), dependencies)) {
                  dependencies.remove(key);
                  this.init_Long2ObjectOpenHas
                     .put(key, new StateCache.Inner_DataUtil(this.init_EventBus.init_BlockState_BlockPos(job.position()), dependencies.toLongArray(), job));

                  for (long dependency : dependencies) {
                     this.load.computeIfAbsent(dependency, ignored -> new LongOpenHashSet()).add(key);
                  }
               }
            }
         }
      }
   }

   public void init(long key) {
      this.init(
         key, null, this.init_EventBus.load_Z(BlockPos.of(key)) ? this.init_EventBus.init_BlockState_BlockPos(BlockPos.of(key)) : ArrayUtil.init_BlockState
      );
   }

   public void init(long key, BlockState before, BlockState after) {
      StateCache.Inner_DataUtil receipt = this.init_Long2ObjectOpenHas.get(key);
      if ((
            receipt == null
               || !this.init(receipt, after)
               || after.is(Blocks.HOPPER) && after.getValue(HopperBlock.ENABLED) && !receipt.source().getValue(HopperBlock.ENABLED)
         )
         && (
            receipt != null
               || before == null
               || before.hasBlockEntity()
               || after.hasBlockEntity()
               || this.init_Function.apply(BlockPos.of(key)).hasBlockEntity()
         )
         && (this.init(key, true) || this.load.containsKey(key))) {
         this.init_LongLinkedOpenHash.add(key);
      }
   }

   public boolean load() {
      return !this.init_Long2ObjectLinkedO.isEmpty();
   }

   public BitSetUtil init_BitSetUtil() {
      return this.init_Long2ObjectLinkedO.isEmpty() ? null : this.init_Long2ObjectLinkedO.removeFirst();
   }

   public void init_V(BlockPos at) {
      long key = at.asLong();
      this.init_Long2ObjectLinkedO.remove(key);
      if (this.init(key, false) || this.load.containsKey(key)) {
         this.init_LongLinkedOpenHash.add(key);
      }
   }

   private boolean init(long key, boolean recheck) {
      StateCache.Inner_DataUtil receipt = this.init_Long2ObjectOpenHas.remove(key);
      if (receipt == null) {
         return false;
      }

      if (recheck) {
         this.init_Long2ObjectLinkedO.put(key, receipt.job());
      }

      for (long dependency : receipt.dependencies()) {
         LongOpenHashSet parents = this.load.get(dependency);
         if (parents != null && parents.remove(key) && parents.isEmpty()) {
            this.load.remove(dependency);
         }
      }

      return true;
   }

   public boolean read() {
      if (this.init_EventBus.init_Obj() != this.init_Obj) {
         return false;
      }

      if (this.init_J != this.init_EventBus.init_J()) {
         this.init_J = this.init_EventBus.init_J();

         for (int i = 0; i < 128 && !this.init_LongLinkedOpenHash.isEmpty(); i++) {
            long changed = this.init_LongLinkedOpenHash.removeFirstLong();
            LongOpenHashSet parents = this.load.remove(changed);
            if (parents != null) {
               for (long parent : parents) {
                  if (this.init(parent, true)) {
                     this.init_LongLinkedOpenHash.add(parent);
                  }
               }
            }
         }
      }

      return this.init_LongLinkedOpenHash.isEmpty();
   }

   public List<BlockPos> init_List(BlockPos at) {
      LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
      BlockState state = this.init_Function.apply(at);
      if (state.is(Blocks.HOPPER)) {
         result.add(at.relative(state.getValue(HopperBlock.FACING)));
      }

      if (this.init_Function.apply(at.below()).is(Blocks.HOPPER)) {
         result.add(at.below());
      }

      if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
         result.add(at.relative(ChestBlock.getConnectedDirection(state)));
      }

      return List.copyOf(result);
   }

   public boolean load(BlockPos at) {
      return this.read() && this.init(at, null);
   }

   private boolean init(BlockPos at, LongOpenHashSet dependencies) {
      if (!this.init_EventBus.load_Z(at)) {
         return false;
      } else {
         BlockState state = this.init_EventBus.init_BlockState_BlockPos(at);
         BlockState wanted = this.init_Function.apply(at);
         if (state.is(Blocks.HOPPER)
            && (state.getValue(HopperBlock.ENABLED) || wanted.is(Blocks.HOPPER) && wanted.getValue(HopperBlock.ENABLED))
            && !this.read(at.relative(state.getValue(HopperBlock.FACING)), dependencies)) {
            return false;
         } else {
            return !this.load(at.below(), dependencies)
               ? false
               : !(state.getBlock() instanceof ChestBlock)
                  || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE
                  || this.load(at.relative(ChestBlock.getConnectedDirection(state)).below(), dependencies);
         }
      }
   }

   private boolean load(BlockPos at, LongOpenHashSet dependencies) {
      if (dependencies != null) {
         dependencies.add(at.asLong());
      }

      if (!this.init_EventBus.load_Z(at)) {
         return this.read(at, dependencies);
      }

      BlockState state = this.init_EventBus.init_BlockState_BlockPos(at);
      BlockState wanted = this.init_Function.apply(at);
      boolean active = state.is(Blocks.HOPPER) && state.getValue(HopperBlock.ENABLED) || wanted.is(Blocks.HOPPER) && wanted.getValue(HopperBlock.ENABLED);
      return !active || this.read(at, dependencies);
   }

   private boolean read(BlockPos at, LongOpenHashSet dependencies) {
      if (dependencies != null) {
         dependencies.add(at.asLong());
      }

      StateCache.Inner_DataUtil receipt = this.init_Long2ObjectOpenHas.get(at.asLong());
      BlockState state;
      if (this.init_EventBus.load_Z(at)) {
         state = this.init_EventBus.init_BlockState_BlockPos(at);
         BlockState wanted = this.init_Function.apply(at);
         if (!state.hasBlockEntity() && !wanted.hasBlockEntity()) {
            return true;
         }
      } else {
         if (receipt == null) {
            return false;
         }

         state = receipt.source();
      }

      if (!this.init(receipt, state)) {
         return false;
      }

      if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
         BlockPos partner = at.relative(ChestBlock.getConnectedDirection(state));
         if (dependencies != null) {
            dependencies.add(partner.asLong());
         }

         StateCache.Inner_DataUtil other = this.init_Long2ObjectOpenHas.get(partner.asLong());
         if (other == null || !this.init(other, this.init_EventBus.load_Z(partner) ? this.init_EventBus.init_BlockState_BlockPos(partner) : other.source())) {
            return false;
         }
      }

      return true;
   }

   private boolean init(StateCache.Inner_DataUtil receipt, BlockState state) {
      return receipt != null
         && PathHelper.write_Z_BlockState_BlockState(receipt.source(), state)
         && (!(state.getBlock() instanceof CrafterBlock) || !state.getValue(CrafterBlock.TRIGGERED) && !state.getValue(CrafterBlock.CRAFTING))
         && (!(state.getBlock() instanceof DispenserBlock) || !state.getValue(DispenserBlock.TRIGGERED));
   }

   private record Inner_DataUtil(BlockState source, long[] dependencies, BitSetUtil job) {
   }
}
