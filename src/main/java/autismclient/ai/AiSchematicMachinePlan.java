package autismclient.ai;

import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public final class AiSchematicMachinePlan {
   static final AiSchematicMachinePlan EMPTY = new AiSchematicMachinePlan(Map.of(), List.of());
   private final Map<Long, ListTag> containerContents;
   private final List<AiSchematic.Inner_TaskQueue> vehicles;
   private final List<AiSchematic.Inner_StreamUtil> frames;
   private final Set<Long> offRailCells;
   private final Set<Long> hopperCells;
   private final Map<Long, BlockState> containerStates;
   private final Map<Long, BlockState> outcomes;
   private final Map<Long, BlockState> write;
   private final Set<Long> clockSections;

   private AiSchematicMachinePlan(Map<Long, ListTag> contents, List<AiSchematic.Inner_TaskQueue> vehicles) {
      this(contents, vehicles, List.of());
   }

   private AiSchematicMachinePlan(Map<Long, ListTag> contents, List<AiSchematic.Inner_TaskQueue> vehicles, List<AiSchematic.Inner_StreamUtil> frames) {
      this(contents, vehicles, Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), Set.of(), frames);
   }

   private AiSchematicMachinePlan(
      Map<Long, ListTag> contents,
      List<AiSchematic.Inner_TaskQueue> vehicles,
      Set<Long> offRailCells,
      Set<Long> hopperCells,
      Map<Long, BlockState> containerStates,
      Map<Long, BlockState> outcomes,
      Map<Long, BlockState> clockContext,
      Set<Long> clockSections,
      List<AiSchematic.Inner_StreamUtil> frames
   ) {
      HashMap<Long, ListTag> copy = new HashMap<>();
      contents.forEach((key, value) -> copy.put(key, value.copy()));
      this.containerContents = Map.copyOf(copy);
      this.vehicles = List.copyOf(vehicles);
      this.frames = List.copyOf(frames);
      this.offRailCells = Set.copyOf(offRailCells);
      this.hopperCells = Set.copyOf(hopperCells);
      this.containerStates = Map.copyOf(containerStates);
      this.outcomes = Map.copyOf(outcomes);
      this.write = Map.copyOf(clockContext);
      this.clockSections = Set.copyOf(clockSections);
   }

   static AiSchematicMachinePlan fromSchematic(AiSchematic.Inner_BufferHelper source, AiBuilder.Blueprint selected) {
      AiSchematicMachinePlan plan = new AiSchematicMachinePlan(source.containerContents(), source.railVehicles(), source.itemFrames()).init(selected);
      HashMap<Long, BlockState> outcomes = new HashMap<>();
      HashSet<Long> vehicleCells = new HashSet<>();

      for (AiSchematic.Inner_TaskQueue vehicle : plan.vehicles) {
         long key = vehicle.rail().asLong();
         if (!plan.offRailCells.contains(key)) {
            vehicleCells.add(key);
         }
      }

      for (AiBuilder.CellSpec cell : selected.cells()) {
         AiBuilder.checkNotCancelled();
         long key = BlockPos.asLong(cell.dx(), cell.dy(), cell.dz());
         if (cell.block().equals("minecraft:piston_head")
            || (cell.block().equals("minecraft:piston") || cell.block().equals("minecraft:sticky_piston")) && cell.properties().containsKey("extended")
            || cell.block().equals("minecraft:iron_trapdoor")
            || cell.block().equals("minecraft:iron_door")
            || cell.block().equals("minecraft:note_block") && "true".equals(cell.properties().get("powered"))
            || vehicleCells.contains(key)
            || plan.hopperCells.contains(key)) {
            outcomes.put(key, AiBuilder.toBlockState(cell));
         }
      }

      return new AiSchematicMachinePlan(plan.containerContents, plan.vehicles, plan.offRailCells, plan.hopperCells, plan.containerStates, outcomes, plan.write, plan.clockSections, plan.frames);
   }

   AiSchematicMachinePlan init(AiBuilder.Blueprint selected) {
      if (this.isEmpty()) {
         return EMPTY;
      }

      HashSet<Long> vehicleCells = new HashSet<>();

      for (AiSchematic.Inner_TaskQueue vehicle : this.vehicles) {
         vehicleCells.add(vehicle.rail().asLong());
      }

      HashSet<Long> cells = new HashSet<>();
      HashSet<Long> offRail = new HashSet<>();
      HashMap<Long, BlockState> selectedStates = new HashMap<>();
      HashSet<Long> outcomeInputs = new HashSet<>(this.outcomes.keySet());
      this.outcomes.forEach((key, state) -> {
         if (state.is(Blocks.PISTON_HEAD)) {
            outcomeInputs.add(BlockPos.of(key).relative(state.getValue(PistonHeadBlock.FACING).getOpposite()).asLong());
         }
      });
      HashSet<Long> hoppers = new HashSet<>();
      HashMap<Long, BlockState> storage = new HashMap<>();
      HashMap<Long, ListTag> kept = new HashMap<>();
      HashSet<Long> frameSupports = new HashSet<>();

      for (AiSchematic.Inner_StreamUtil frame : this.frames) {
         frameSupports.add(frame.support().asLong());
      }

      HashSet<Long> supported = new HashSet<>();

      for (AiBuilder.CellSpec cell : selected.cells()) {
         AiBuilder.checkNotCancelled();
         long key = BlockPos.asLong(cell.dx(), cell.dy(), cell.dz());
         if (frameSupports.contains(key)) {
            supported.add(key);
         }

         if (outcomeInputs.contains(key)) {
            selectedStates.put(key, AiBuilder.toBlockState(cell));
         }

         if (vehicleCells.contains(key)) {
            cells.add(key);
            if (!(AiBuilder.toBlockState(cell).getBlock() instanceof BaseRailBlock)) {
               offRail.add(key);
            }
         }

         ListTag saved = this.containerContents.get(key);
         if (saved != null) {
            kept.put(key, saved);
            BlockState state = AiBuilder.toBlockState(cell);
            if (AiSavedContents.isContainer(state)) {
               storage.put(key, state);
            }

            if (cell.block().equals("minecraft:hopper") && "false".equals(cell.properties().get("enabled"))) {
               hoppers.add(key);
            }
         }
      }

      List<AiSchematic.Inner_TaskQueue> carts = this.vehicles.stream().filter(vehicle -> cells.contains(vehicle.rail().asLong())).toList();
      HashMap<Long, BlockState> selectedOutcomes = new HashMap<>();
      this.outcomes.forEach((key, state) -> {
         boolean keep = selectedStates.containsKey(key);
         if (!keep && state.is(Blocks.PISTON_HEAD)) {
            Direction facing = state.getValue(PistonHeadBlock.FACING);
            BlockState base = selectedStates.get(BlockPos.of(key).relative(facing.getOpposite()).asLong());
            keep = base != null && base.getBlock() instanceof PistonBaseBlock && base.getValue(PistonBaseBlock.FACING) == facing;
         }

         if (keep) {
            selectedOutcomes.put(key, state);
         }
      });
      HashSet<Long> sections = new HashSet<>();
      storage.forEach((key, state) -> {
         if (state.is(Blocks.HOPPER) && state.getValue(BlockStateProperties.FACING_HOPPER).getAxis().isHorizontal()) {
            BlockPos at = BlockPos.of(key);

            for (int x = -1; x <= 1; x++) {
               for (int y = -1; y <= 1; y++) {
                  for (int z = -1; z <= 1; z++) {
                     sections.add(SectionPos.asLong((at.getX() >> 4) + x, (at.getY() >> 4) + y, (at.getZ() >> 4) + z));
                  }
               }
            }
         }
      });
      HashMap<Long, BlockState> context = new HashMap<>();
      if (!sections.isEmpty()) {
         for (AiBuilder.CellSpec cell : selected.cells()) {
            AiBuilder.checkNotCancelled();
            if (sections.contains(SectionPos.asLong(cell.dx() >> 4, cell.dy() >> 4, cell.dz() >> 4))) {
               context.put(BlockPos.asLong(cell.dx(), cell.dy(), cell.dz()), AiBuilder.toBlockState(cell));
            }
         }

         selectedOutcomes.forEach((key, state) -> {
            BlockPos at = BlockPos.of(key);
            if (sections.contains(SectionPos.asLong(at.getX() >> 4, at.getY() >> 4, at.getZ() >> 4))) {
               context.put(key, state);
            }
         });
      }

      List<AiSchematic.Inner_StreamUtil> hung = this.frames.stream().filter(frame -> supported.contains(frame.support().asLong())).toList();
      return kept.isEmpty() && carts.isEmpty() && selectedOutcomes.isEmpty() && hung.isEmpty()
         ? EMPTY
         : new AiSchematicMachinePlan(kept, carts, offRail, hoppers, storage, selectedOutcomes, context, sections, hung);
   }

   boolean isEmpty() {
      return this.containerContents.isEmpty() && this.vehicles.isEmpty() && this.outcomes.isEmpty() && this.frames.isEmpty();
   }

   boolean hasActions() {
      return !this.vehicles.isEmpty() || !this.containerStates.isEmpty() || !this.outcomes.isEmpty() || !this.frames.isEmpty();
   }

   Set<Long> hopperCells() {
      return this.hopperCells;
   }

   Set<Long> containerCells() {
      return this.containerStates.keySet();
   }

   int vehicleCount() {
      return this.vehicles.size();
   }

   int containerCount() {
      return this.containerContents.size();
   }

   List<AiSchematicMachinePlan.ContainerPlan> containerPlans(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.containerContents.keySet());
   }

   List<AiSchematicMachinePlan.ContainerPlan> load(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.hopperCells);
   }

   List<AiSchematicMachinePlan.ContainerPlan> read(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.containerStates.keySet());
   }

   AiSchematicMachinePlan.MachineSpec machineSpec(BlockPos origin, Provider registries) {
      if (!this.hasActions()) {
         return AiSchematicMachinePlan.MachineSpec.EMPTY;
      }

      ArrayList<autismclient.ai.print.PrintSkippedContents> unavailable = new ArrayList<>();
      List<AiSchematicMachinePlan.ContainerPlan> decoded = this.init(origin, registries, this.containerStates.keySet(), unavailable);
      HashMap<BlockPos, BlockState> source = new HashMap<>();
      this.write.forEach((key, state) -> source.put(origin.offset(BlockPos.of(key)), state));
      List<autismclient.ai.print.PrintClockSpec> clocks = AiHopperClockPlan.init(
         source,
         decoded,
         at -> this.clockSections.contains(SectionPos.asLong(at.getX() - origin.getX() >> 4, at.getY() - origin.getY() >> 4, at.getZ() - origin.getZ() >> 4))
      );
      HashSet<BlockPos> grouped = new HashSet<>();
      clocks.forEach(clock -> grouped.addAll(clock.hoppers()));
      return new AiSchematicMachinePlan.MachineSpec(
         this.railVehicles(origin),
         decoded.stream().filter(container -> !grouped.contains(container.position())).toList(),
         this.outcomeStates(origin),
         unavailable,
         clocks,
         this.write(origin, registries)
      );
   }

   List<autismclient.ai.print.PrintItemFrame.Inner_StreamUtil> write(BlockPos origin, Provider registries) {
      if (this.frames.isEmpty()) {
         return List.of();
      }

      RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
      ArrayList<autismclient.ai.print.PrintItemFrame.Inner_StreamUtil> out = new ArrayList<>(this.frames.size());

      for (AiSchematic.Inner_StreamUtil frame : this.frames) {
         ItemStack item = ItemStack.EMPTY;
         boolean undecodable = false;
         if (frame.item() != null) {
            DataResult<ItemStack> decoded = ItemStack.CODEC.parse(ops, frame.item());
            if (decoded.error().isPresent()) {
               undecodable = true;
            } else {
               item = decoded.getOrThrow();
            }
         }

         out.add(
            new autismclient.ai.print.PrintItemFrame.Inner_StreamUtil(
               frame.id(), origin.offset(frame.cell()), frame.facing(), item, frame.rotation(), frame.invisible(), frame.fixed(), undecodable
            )
         );
      }

      return List.copyOf(out);
   }

   private List<AiSchematicMachinePlan.ContainerPlan> init(BlockPos origin, Provider registries, Set<Long> keys) {
      return this.init(origin, registries, keys, null);
   }

   private List<AiSchematicMachinePlan.ContainerPlan> init(BlockPos origin, Provider registries, Set<Long> keys, List<autismclient.ai.print.PrintSkippedContents> unavailable) {
      if (keys.isEmpty()) {
         return List.of();
      }

      ArrayList<AiSchematicMachinePlan.ContainerPlan> result = new ArrayList<>(keys.size());
      RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

      for (long key : keys) {
         AiBuilder.checkNotCancelled();
         HashMap<Integer, ItemStack> slots = new HashMap<>();
         Iterator var11 = this.containerContents.get(key).iterator();

         while (true) {
            if (var11.hasNext()) {
               Tag tag = (Tag)var11.next();
               AiBuilder.checkNotCancelled();
               if (!(tag instanceof CompoundTag item)) {
                  throw new IllegalArgumentException("Saved storage item is not a compound");
               }

               int slot = item.getIntOr("Slot", -1);
               DataResult decoded = ItemStack.CODEC.parse(ops, item);
               if (unavailable == null || !decoded.error().isPresent()) {
                  ItemStack stack = (ItemStack)decoded.getOrThrow();
                  if (slot < 0 || slot > 255 || stack.isEmpty() || stack.getCount() > stack.getMaxStackSize() || slots.putIfAbsent(slot, stack.copy()) != null) {
                     throw new IllegalArgumentException("Invalid or duplicate saved storage slot at " + BlockPos.of(key).toShortString());
                  }
                  continue;
               }

               BlockPos position = origin.offset(BlockPos.of(key));
               unavailable.add(
                  new autismclient.ai.print.PrintSkippedContents(
                     position,
                     this.containerStates.get(key),
                     this.containerContents.get(key).size(),
                     "Saved item in Slot " + slot + " cannot be decoded with this world's registry"
                  )
               );
               slots = null;
            }

            if (slots != null) {
               result.add(new AiSchematicMachinePlan.ContainerPlan(origin.offset(BlockPos.of(key)), slots, this.containerStates.get(key)));
            }
            break;
         }
      }

      result.sort(Comparator.comparingLong(value -> value.position().asLong()));
      if (unavailable != null) {
         unavailable.sort(Comparator.comparingLong(value -> value.position().asLong()));
      }

      return List.copyOf(result);
   }

   List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> railVehicles(BlockPos origin) {
      Vec3 offset = Vec3.atLowerCornerOf(origin);
      return this.vehicles
         .stream()
         .map(
            value -> new autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil(value.id(), value.position().add(offset), !this.offRailCells.contains(value.rail().asLong()))
         )
         .toList();
   }

   Map<BlockPos, BlockState> outcomeStates(BlockPos origin) {
      if (this.outcomes.isEmpty()) {
         return Map.of();
      }

      HashMap<BlockPos, BlockState> placed = new HashMap<>();
      this.outcomes.forEach((key, state) -> placed.put(origin.offset(BlockPos.of(key)), state));
      return Map.copyOf(placed);
   }

   public record ContainerPlan(BlockPos position, Map<Integer, ItemStack> slots, BlockState source) {
      public ContainerPlan(BlockPos position, Map<Integer, ItemStack> slots) {
         this(position, slots, null);
      }

      public ContainerPlan {
         position = position.immutable();
         slots = snapshot(slots);
      }

      public Map<Integer, ItemStack> slots() {
         return snapshot(this.slots);
      }

      private static Map<Integer, ItemStack> snapshot(Map<Integer, ItemStack> slots) {
         HashMap<Integer, ItemStack> copy = new HashMap<>();
         slots.forEach((key, value) -> copy.put(key, value.copy()));
         return Map.copyOf(copy);
      }
   }

   public record MachineSpec(
      List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles,
      List<AiSchematicMachinePlan.ContainerPlan> containers,
      Map<BlockPos, BlockState> outcomes,
      List<autismclient.ai.print.PrintSkippedContents> unavailable,
      List<autismclient.ai.print.PrintClockSpec> clocks,
      List<autismclient.ai.print.PrintItemFrame.Inner_StreamUtil> frames
   ) {
      static final AiSchematicMachinePlan.MachineSpec EMPTY = new AiSchematicMachinePlan.MachineSpec(List.of(), List.of(), Map.of());

      MachineSpec(
         List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles,
         List<AiSchematicMachinePlan.ContainerPlan> containers,
         Map<BlockPos, BlockState> outcomes,
         List<autismclient.ai.print.PrintSkippedContents> unavailable,
         List<autismclient.ai.print.PrintClockSpec> clocks
      ) {
         this(vehicles, containers, outcomes, unavailable, clocks, List.of());
      }

      MachineSpec(List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles, List<AiSchematicMachinePlan.ContainerPlan> containers, Map<BlockPos, BlockState> outcomes) {
         this(vehicles, containers, outcomes, List.of(), List.of());
      }

      MachineSpec(
         List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> vehicles,
         List<AiSchematicMachinePlan.ContainerPlan> containers,
         Map<BlockPos, BlockState> outcomes,
         List<autismclient.ai.print.PrintSkippedContents> unavailable
      ) {
         this(vehicles, containers, outcomes, unavailable, List.of());
      }

      public MachineSpec {
         vehicles = List.copyOf(vehicles);
         containers = List.copyOf(containers);
         outcomes = Map.copyOf(outcomes);
         unavailable = List.copyOf(unavailable);
         clocks = List.copyOf(clocks);
         frames = List.copyOf(frames);
      }
   }
}
