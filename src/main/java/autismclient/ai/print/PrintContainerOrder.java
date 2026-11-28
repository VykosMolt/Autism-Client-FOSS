package autismclient.ai.print;

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

public final class PrintContainerOrder {
   private final PrintEnv env;
   private final Function<BlockPos, BlockState> planned;
   private final Object worldToken;
   private final LongOpenHashSet positions = new LongOpenHashSet();
   private final Long2ObjectOpenHashMap<PrintContainerOrder.OrderEntry> entries = new Long2ObjectOpenHashMap<>();
   private final Long2ObjectOpenHashMap<LongOpenHashSet> dependents = new Long2ObjectOpenHashMap<>();
   private final LongLinkedOpenHashSet recheckQueue = new LongLinkedOpenHashSet();
   private final Long2ObjectLinkedOpenHashMap<PrintMachineJob> readyJobs = new Long2ObjectLinkedOpenHashMap<>();
   private long lastTick = -9223372036854775808L;

   public PrintContainerOrder(PrintEnv env, Function<BlockPos, BlockState> planned) {
      this.env = env;
      this.planned = planned;
      this.worldToken = env.worldToken();
   }

   public void track(PrintMachineJob job) {
      this.positions.add(job.position().asLong());
      job.setOrder(this);
   }

   public boolean init_Z(BlockPos at) {
      return this.positions.contains(at.asLong());
   }

   public boolean isIdle() {
      return this.positions.isEmpty();
   }

   public void complete(PrintMachineJob job) {
      long key = job.position().asLong();
      this.readyJobs.remove(key);
      if (!job.isVerifiedSuccess() || !this.env.hasChunkAt(job.position()) || !job.matchesSource(this.env.getBlockState(job.position()))) {
         this.init_V(job.position());
      } else if (this.read()) {
         PrintContainerOrder.OrderEntry previous = this.entries.get(key);
         if (previous == null || !this.init(previous, this.env.getBlockState(job.position()))) {
            this.init_V(job.position());
            if (this.read()) {
               LongOpenHashSet dependencies = new LongOpenHashSet();
               if (this.init(job.position(), dependencies)) {
                  dependencies.remove(key);
                  this.entries
                     .put(key, new PrintContainerOrder.OrderEntry(this.env.getBlockState(job.position()), dependencies.toLongArray(), job));

                  for (long dependency : dependencies) {
                     this.dependents.computeIfAbsent(dependency, ignored -> new LongOpenHashSet()).add(key);
                  }
               }
            }
         }
      }
   }

   public void init(long key) {
      this.onBlockChange(
         key, null, this.env.hasChunkAt(BlockPos.of(key)) ? this.env.getBlockState(BlockPos.of(key)) : PrintView.UNLOADED_SENTINEL
      );
   }

   public void onBlockChange(long key, BlockState before, BlockState after) {
      PrintContainerOrder.OrderEntry receipt = this.entries.get(key);
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
               || this.planned.apply(BlockPos.of(key)).hasBlockEntity()
         )
         && (this.init(key, true) || this.dependents.containsKey(key))) {
         this.recheckQueue.add(key);
      }
   }

   public boolean hasPending() {
      return !this.readyJobs.isEmpty();
   }

   public PrintMachineJob pollNext() {
      return this.readyJobs.isEmpty() ? null : this.readyJobs.removeFirst();
   }

   public void init_V(BlockPos at) {
      long key = at.asLong();
      this.readyJobs.remove(key);
      if (this.init(key, false) || this.dependents.containsKey(key)) {
         this.recheckQueue.add(key);
      }
   }

   private boolean init(long key, boolean recheck) {
      PrintContainerOrder.OrderEntry receipt = this.entries.remove(key);
      if (receipt == null) {
         return false;
      }

      if (recheck) {
         this.readyJobs.put(key, receipt.job());
      }

      for (long dependency : receipt.dependencies()) {
         LongOpenHashSet parents = this.dependents.get(dependency);
         if (parents != null && parents.remove(key) && parents.isEmpty()) {
            this.dependents.remove(dependency);
         }
      }

      return true;
   }

   public boolean read() {
      if (this.env.worldToken() != this.worldToken) {
         return false;
      }

      if (this.lastTick != this.env.currentTick()) {
         this.lastTick = this.env.currentTick();

         for (int i = 0; i < 128 && !this.recheckQueue.isEmpty(); i++) {
            long changed = this.recheckQueue.removeFirstLong();
            LongOpenHashSet parents = this.dependents.remove(changed);
            if (parents != null) {
               for (long parent : parents) {
                  if (this.init(parent, true)) {
                     this.recheckQueue.add(parent);
                  }
               }
            }
         }
      }

      return this.recheckQueue.isEmpty();
   }

   public List<BlockPos> dependenciesOf(BlockPos at) {
      LinkedHashSet<BlockPos> result = new LinkedHashSet<>();
      BlockState state = this.planned.apply(at);
      if (state.is(Blocks.HOPPER)) {
         result.add(at.relative(state.getValue(HopperBlock.FACING)));
      }

      if (this.planned.apply(at.below()).is(Blocks.HOPPER)) {
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
      if (!this.env.hasChunkAt(at)) {
         return false;
      } else {
         BlockState state = this.env.getBlockState(at);
         BlockState wanted = this.planned.apply(at);
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

      if (!this.env.hasChunkAt(at)) {
         return this.read(at, dependencies);
      }

      BlockState state = this.env.getBlockState(at);
      BlockState wanted = this.planned.apply(at);
      boolean active = state.is(Blocks.HOPPER) && state.getValue(HopperBlock.ENABLED) || wanted.is(Blocks.HOPPER) && wanted.getValue(HopperBlock.ENABLED);
      return !active || this.read(at, dependencies);
   }

   private boolean read(BlockPos at, LongOpenHashSet dependencies) {
      if (dependencies != null) {
         dependencies.add(at.asLong());
      }

      PrintContainerOrder.OrderEntry receipt = this.entries.get(at.asLong());
      BlockState state;
      if (this.env.hasChunkAt(at)) {
         state = this.env.getBlockState(at);
         BlockState wanted = this.planned.apply(at);
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

         PrintContainerOrder.OrderEntry other = this.entries.get(partner.asLong());
         if (other == null || !this.init(other, this.env.hasChunkAt(partner) ? this.env.getBlockState(partner) : other.source())) {
            return false;
         }
      }

      return true;
   }

   private boolean init(PrintContainerOrder.OrderEntry receipt, BlockState state) {
      return receipt != null
         && PrintRules.matchesLayout(receipt.source(), state)
         && (!(state.getBlock() instanceof CrafterBlock) || !state.getValue(CrafterBlock.TRIGGERED) && !state.getValue(CrafterBlock.CRAFTING))
         && (!(state.getBlock() instanceof DispenserBlock) || !state.getValue(DispenserBlock.TRIGGERED));
   }

   private record OrderEntry(BlockState source, long[] dependencies, PrintMachineJob job) {
   }
}
