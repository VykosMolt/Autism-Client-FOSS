package autismclient.ai.print;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.phys.AABB;

public record PrintClockSpec(
   BlockPos preload,
   BlockPos other,
   BlockPos firstPiston,
   BlockPos secondPiston,
   BlockPos firstPayload,
   BlockPos secondPayload,
   BlockPos lever,
   Direction direction,
   ItemStack material,
   int quantity,
   int savedSlots,
   Map<BlockPos, BlockState> source,
   Set<BlockPos> outputs
) {
   public PrintClockSpec(
      BlockPos preload,
      BlockPos other,
      BlockPos firstPiston,
      BlockPos secondPiston,
      BlockPos firstPayload,
      BlockPos secondPayload,
      BlockPos lever,
      Direction direction,
      ItemStack material,
      int quantity,
      int savedSlots,
      Map<BlockPos, BlockState> source,
      Set<BlockPos> outputs
   ) {
      preload = preload.immutable();
      other = other.immutable();
      firstPiston = firstPiston.immutable();
      secondPiston = secondPiston.immutable();
      firstPayload = firstPayload.immutable();
      secondPayload = secondPayload.immutable();
      lever = lever.immutable();
      material = material.copyWithCount(1);
      HashMap<BlockPos, BlockState> copied = new HashMap<>();
      source.forEach((at, state) -> copied.put(at.immutable(), state));
      source = Map.copyOf(copied);
      outputs = outputs.stream().map(BlockPos::immutable).collect(Collectors.toUnmodifiableSet());
      if (!material.isEmpty() && quantity > 0 && quantity <= 5 * material.getMaxStackSize() && savedSlots >= 1 && direction.getAxis().isHorizontal()) {
         this.preload = preload;
         this.other = other;
         this.firstPiston = firstPiston;
         this.secondPiston = secondPiston;
         this.firstPayload = firstPayload;
         this.secondPayload = secondPayload;
         this.lever = lever;
         this.direction = direction;
         this.material = material;
         this.quantity = quantity;
         this.savedSlots = savedSlots;
         this.source = source;
         this.outputs = outputs;
      } else {
         throw new IllegalArgumentException("Invalid closed hopper-clock stock");
      }
   }

   public ItemStack material() {
      return this.material.copy();
   }

   public Set<BlockPos> hoppers() {
      return Set.of(this.preload, this.other);
   }

   public Set<BlockPos> dynamicCells() {
      HashSet<BlockPos> cells = new HashSet<>(this.outputs);
      cells.addAll(Set.of(this.preload, this.other, this.firstPiston, this.secondPiston, this.firstPayload, this.secondPayload, this.lever));
      return Set.copyOf(cells);
   }

   public AABB sweptVolume() {
      return new AABB(this.firstPayload).minmax(new AABB(this.secondPayload)).inflate(0.05);
   }

   public Map<Integer, ItemStack> preloadSlots() {
      LinkedHashMap<Integer, ItemStack> result = new LinkedHashMap<>();
      int remaining = this.quantity;

      for (int slot = 0; remaining > 0; slot++) {
         int n = Math.min(remaining, this.material.getMaxStackSize());
         result.put(slot, this.material.copyWithCount(n));
         remaining -= n;
      }

      return Map.copyOf(result);
   }

   public int phase(Function<BlockPos, BlockState> world) {
      BlockState a = world.apply(this.firstPayload);
      BlockState b = world.apply(this.secondPayload);
      BlockState pa = world.apply(this.firstPiston);
      BlockState pb = world.apply(this.secondPiston);
      BlockState ha = world.apply(this.preload);
      BlockState hb = world.apply(this.other);
      if (!piston(pa, this.direction) || !piston(pb, this.direction.getOpposite()) || !ha.is(Blocks.HOPPER) || !hb.is(Blocks.HOPPER)) {
         return -1;
      } else if (a.is(Blocks.REDSTONE_BLOCK)
         && head(b, this.direction.getOpposite())
         && !pa.getValue(PistonBaseBlock.EXTENDED)
         && pb.getValue(PistonBaseBlock.EXTENDED)
         && !ha.getValue(BlockStateProperties.ENABLED)
         && hb.getValue(BlockStateProperties.ENABLED)) {
         return 0;
      } else {
         return b.is(Blocks.REDSTONE_BLOCK)
               && head(a, this.direction)
               && pa.getValue(PistonBaseBlock.EXTENDED)
               && !pb.getValue(PistonBaseBlock.EXTENDED)
               && ha.getValue(BlockStateProperties.ENABLED)
               && !hb.getValue(BlockStateProperties.ENABLED)
            ? 1
            : -1;
      }
   }

   public boolean holding(Function<BlockPos, BlockState> world) {
      return world.apply(this.lever) == this.source.get(this.lever).setValue(BlockStateProperties.POWERED, true)
         && this.phase(world) == 0
         && !world.apply(this.preload).getValue(BlockStateProperties.ENABLED);
   }

   public boolean mechanical(BlockPos at) {
      return at.equals(this.firstPayload)
         || at.equals(this.secondPayload)
         || at.equals(this.firstPiston)
         || at.equals(this.secondPiston)
         || this.hoppers().contains(at);
   }

   public boolean movingCell(BlockPos at) {
      return at.equals(this.firstPayload) || at.equals(this.secondPayload) || at.equals(this.firstPiston) || at.equals(this.secondPiston);
   }

   public boolean allowsMovingPayload(BlockPos at, BlockState moved) {
      return !at.equals(this.firstPiston) && !at.equals(this.secondPiston)
         ? (at.equals(this.firstPayload) || at.equals(this.secondPayload))
            && (moved.is(Blocks.REDSTONE_BLOCK) || head(moved, at.equals(this.firstPayload) ? this.direction : this.direction.getOpposite()))
         : piston(moved, at.equals(this.firstPiston) ? this.direction : this.direction.getOpposite());
   }

   public boolean allows(BlockPos at, BlockState actual, boolean leverOn, boolean running) {
      BlockState wanted = this.source.get(at);
      if (wanted == null) {
         return false;
      }

      if (at.equals(this.lever)) {
         return actual == wanted.setValue(BlockStateProperties.POWERED, leverOn);
      }

      if (running && this.movingCell(at) && actual.is(Blocks.MOVING_PISTON)) {
         Direction facing = !at.equals(this.firstPayload) && !at.equals(this.firstPiston) ? this.direction.getOpposite() : this.direction;
         return actual.getValue(PistonHeadBlock.FACING) == facing;
      }

      if (!at.equals(this.firstPayload) && !at.equals(this.secondPayload)) {
         if (!this.outputs.contains(at) || !(wanted.getBlock() instanceof TrapDoorBlock) && !(wanted.getBlock() instanceof DoorBlock)) {
            return !this.mechanical(at) && !this.outputs.contains(at) ? actual == wanted : PrintRules.write_Z_BlockState_BlockState(wanted, actual);
         }

         boolean poweredOnly = wanted.is(Blocks.IRON_TRAPDOOR) || wanted.is(Blocks.IRON_DOOR);
         return actual.is(wanted.getBlock())
            && (
               actual.getValue(BlockStateProperties.OPEN) == actual.getValue(BlockStateProperties.POWERED)
                  || !poweredOnly && actual.getValue(BlockStateProperties.OPEN) == wanted.getValue(BlockStateProperties.OPEN)
            )
            && PrintRules.write_Z_BlockState_BlockState(wanted, actual.setValue(BlockStateProperties.OPEN, wanted.getValue(BlockStateProperties.OPEN)));
      } else {
         Direction facing = at.equals(this.firstPayload) ? this.direction : this.direction.getOpposite();
         return actual.is(Blocks.REDSTONE_BLOCK) || head(actual, facing);
      }
   }

   private static boolean piston(BlockState state, Direction direction) {
      return state.is(Blocks.STICKY_PISTON) && state.getValue(PistonBaseBlock.FACING) == direction;
   }

   private static boolean head(BlockState state, Direction direction) {
      return state.is(Blocks.PISTON_HEAD)
         && state.getValue(PistonHeadBlock.FACING) == direction
         && state.getValue(PistonHeadBlock.TYPE) == PistonType.STICKY
         && !state.getValue(PistonHeadBlock.SHORT);
   }
}
