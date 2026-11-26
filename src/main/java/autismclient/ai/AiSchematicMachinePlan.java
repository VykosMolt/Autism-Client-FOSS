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
   static final AiSchematicMachinePlan init_bV = new AiSchematicMachinePlan(Map.of(), List.of());
   private final Map<Long, ListTag> init_Map;
   private final List<AiSchematic.Inner_TaskQueue> init_List;
   private final List<AiSchematic.Inner_StreamUtil> load_List;
   private final Set<Long> init_Set;
   private final Set<Long> load_Set;
   private final Map<Long, BlockState> load_Map;
   private final Map<Long, BlockState> read_Map;
   private final Map<Long, BlockState> write;
   private final Set<Long> read_Set;

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
      this.init_Map = Map.copyOf(copy);
      this.init_List = List.copyOf(vehicles);
      this.load_List = List.copyOf(frames);
      this.init_Set = Set.copyOf(offRailCells);
      this.load_Set = Set.copyOf(hopperCells);
      this.load_Map = Map.copyOf(containerStates);
      this.read_Map = Map.copyOf(outcomes);
      this.write = Map.copyOf(clockContext);
      this.read_Set = Set.copyOf(clockSections);
   }

   static AiSchematicMachinePlan init_bV_bP_BufferHelper_TableIndex_DataUti(AiSchematic.Inner_BufferHelper source, AiBuilder.Blueprint selected) {
      AiSchematicMachinePlan plan = new AiSchematicMachinePlan(source.containerContents(), source.railVehicles(), source.itemFrames()).init(selected);
      HashMap<Long, BlockState> outcomes = new HashMap<>();
      HashSet<Long> vehicleCells = new HashSet<>();

      for (AiSchematic.Inner_TaskQueue vehicle : plan.init_List) {
         long key = vehicle.rail().asLong();
         if (!plan.init_Set.contains(key)) {
            vehicleCells.add(key);
         }
      }

      for (AiBuilder.CellSpec cell : selected.cells()) {
         AiBuilder.init();
         long key = BlockPos.asLong(cell.dx(), cell.dy(), cell.dz());
         if (cell.block().equals("minecraft:piston_head")
            || (cell.block().equals("minecraft:piston") || cell.block().equals("minecraft:sticky_piston")) && cell.properties().containsKey("extended")
            || cell.block().equals("minecraft:iron_trapdoor")
            || cell.block().equals("minecraft:iron_door")
            || cell.block().equals("minecraft:note_block") && "true".equals(cell.properties().get("powered"))
            || vehicleCells.contains(key)
            || plan.load_Set.contains(key)) {
            outcomes.put(key, AiBuilder.init_BlockState(cell));
         }
      }

      return new AiSchematicMachinePlan(plan.init_Map, plan.init_List, plan.init_Set, plan.load_Set, plan.load_Map, outcomes, plan.write, plan.read_Set, plan.load_List);
   }

   AiSchematicMachinePlan init(AiBuilder.Blueprint selected) {
      if (this.init_Z()) {
         return init_bV;
      }

      HashSet<Long> vehicleCells = new HashSet<>();

      for (AiSchematic.Inner_TaskQueue vehicle : this.init_List) {
         vehicleCells.add(vehicle.rail().asLong());
      }

      HashSet<Long> cells = new HashSet<>();
      HashSet<Long> offRail = new HashSet<>();
      HashMap<Long, BlockState> selectedStates = new HashMap<>();
      HashSet<Long> outcomeInputs = new HashSet<>(this.read_Map.keySet());
      this.read_Map.forEach((key, state) -> {
         if (state.is(Blocks.PISTON_HEAD)) {
            outcomeInputs.add(BlockPos.of(key).relative(state.getValue(PistonHeadBlock.FACING).getOpposite()).asLong());
         }
      });
      HashSet<Long> hoppers = new HashSet<>();
      HashMap<Long, BlockState> storage = new HashMap<>();
      HashMap<Long, ListTag> kept = new HashMap<>();
      HashSet<Long> frameSupports = new HashSet<>();

      for (AiSchematic.Inner_StreamUtil frame : this.load_List) {
         frameSupports.add(frame.support().asLong());
      }

      HashSet<Long> supported = new HashSet<>();

      for (AiBuilder.CellSpec cell : selected.cells()) {
         AiBuilder.init();
         long key = BlockPos.asLong(cell.dx(), cell.dy(), cell.dz());
         if (frameSupports.contains(key)) {
            supported.add(key);
         }

         if (outcomeInputs.contains(key)) {
            selectedStates.put(key, AiBuilder.init_BlockState(cell));
         }

         if (vehicleCells.contains(key)) {
            cells.add(key);
            if (!(AiBuilder.init_BlockState(cell).getBlock() instanceof BaseRailBlock)) {
               offRail.add(key);
            }
         }

         ListTag saved = this.init_Map.get(key);
         if (saved != null) {
            kept.put(key, saved);
            BlockState state = AiBuilder.init_BlockState(cell);
            if (AiSavedContents.init_Z(state)) {
               storage.put(key, state);
            }

            if (cell.block().equals("minecraft:hopper") && "false".equals(cell.properties().get("enabled"))) {
               hoppers.add(key);
            }
         }
      }

      List<AiSchematic.Inner_TaskQueue> carts = this.init_List.stream().filter(vehicle -> cells.contains(vehicle.rail().asLong())).toList();
      HashMap<Long, BlockState> selectedOutcomes = new HashMap<>();
      this.read_Map.forEach((key, state) -> {
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
            AiBuilder.init();
            if (sections.contains(SectionPos.asLong(cell.dx() >> 4, cell.dy() >> 4, cell.dz() >> 4))) {
               context.put(BlockPos.asLong(cell.dx(), cell.dy(), cell.dz()), AiBuilder.init_BlockState(cell));
            }
         }

         selectedOutcomes.forEach((key, state) -> {
            BlockPos at = BlockPos.of(key);
            if (sections.contains(SectionPos.asLong(at.getX() >> 4, at.getY() >> 4, at.getZ() >> 4))) {
               context.put(key, state);
            }
         });
      }

      List<AiSchematic.Inner_StreamUtil> hung = this.load_List.stream().filter(frame -> supported.contains(frame.support().asLong())).toList();
      return kept.isEmpty() && carts.isEmpty() && selectedOutcomes.isEmpty() && hung.isEmpty()
         ? init_bV
         : new AiSchematicMachinePlan(kept, carts, offRail, hoppers, storage, selectedOutcomes, context, sections, hung);
   }

   boolean init_Z() {
      return this.init_Map.isEmpty() && this.init_List.isEmpty() && this.read_Map.isEmpty() && this.load_List.isEmpty();
   }

   boolean load_Z() {
      return !this.init_List.isEmpty() || !this.load_Map.isEmpty() || !this.read_Map.isEmpty() || !this.load_List.isEmpty();
   }

   Set<Long> init_Set() {
      return this.load_Set;
   }

   Set<Long> load_Set() {
      return this.load_Map.keySet();
   }

   int init_I() {
      return this.init_List.size();
   }

   int load_I() {
      return this.init_Map.size();
   }

   List<AiSchematicMachinePlan.ContainerPlan> init_List_BlockPos_HolderLookup_Provi(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.init_Map.keySet());
   }

   List<AiSchematicMachinePlan.ContainerPlan> load(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.load_Set);
   }

   List<AiSchematicMachinePlan.ContainerPlan> read(BlockPos origin, Provider registries) {
      return this.init(origin, registries, this.load_Map.keySet());
   }

   AiSchematicMachinePlan.MachineSpec init_bV_StreamUtil_BlockPos_HolderLookup_Provi(BlockPos origin, Provider registries) {
      if (!this.load_Z()) {
         return AiSchematicMachinePlan.MachineSpec.EMPTY;
      }

      ArrayList<autismclient.ai.print.PrintSkippedContents> unavailable = new ArrayList<>();
      List<AiSchematicMachinePlan.ContainerPlan> decoded = this.init(origin, registries, this.load_Map.keySet(), unavailable);
      HashMap<BlockPos, BlockState> source = new HashMap<>();
      this.write.forEach((key, state) -> source.put(origin.offset(BlockPos.of(key)), state));
      List<autismclient.ai.print.PrintClockSpec> clocks = AiHopperClockPlan.init(
         source,
         decoded,
         at -> this.read_Set.contains(SectionPos.asLong(at.getX() - origin.getX() >> 4, at.getY() - origin.getY() >> 4, at.getZ() - origin.getZ() >> 4))
      );
      HashSet<BlockPos> grouped = new HashSet<>();
      clocks.forEach(clock -> grouped.addAll(clock.hoppers()));
      return new AiSchematicMachinePlan.MachineSpec(
         this.init_List(origin),
         decoded.stream().filter(container -> !grouped.contains(container.position())).toList(),
         this.init_Map(origin),
         unavailable,
         clocks,
         this.write(origin, registries)
      );
   }

   List<autismclient.ai.print.PrintItemFrame.Inner_StreamUtil> write(BlockPos origin, Provider registries) {
      if (this.load_List.isEmpty()) {
         return List.of();
      }

      RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
      ArrayList<autismclient.ai.print.PrintItemFrame.Inner_StreamUtil> out = new ArrayList<>(this.load_List.size());

      for (AiSchematic.Inner_StreamUtil frame : this.load_List) {
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
         AiBuilder.init();
         HashMap<Integer, ItemStack> slots = new HashMap<>();
         Iterator var11 = this.init_Map.get(key).iterator();

         while (true) {
            if (var11.hasNext()) {
               Tag tag = (Tag)var11.next();
               AiBuilder.init();
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
                     this.load_Map.get(key),
                     this.init_Map.get(key).size(),
                     "Saved item in Slot " + slot + " cannot be decoded with this world's registry"
                  )
               );
               slots = null;
            }

            if (slots != null) {
               result.add(new AiSchematicMachinePlan.ContainerPlan(origin.offset(BlockPos.of(key)), slots, this.load_Map.get(key)));
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

   List<autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil> init_List(BlockPos origin) {
      Vec3 offset = Vec3.atLowerCornerOf(origin);
      return this.init_List
         .stream()
         .map(
            value -> new autismclient.ai.print.PrintRailVehicle.Inner_StreamUtil(value.id(), value.position().add(offset), !this.init_Set.contains(value.rail().asLong()))
         )
         .toList();
   }

   Map<BlockPos, BlockState> init_Map(BlockPos origin) {
      if (this.read_Map.isEmpty()) {
         return Map.of();
      }

      HashMap<BlockPos, BlockState> placed = new HashMap<>();
      this.read_Map.forEach((key, state) -> placed.put(origin.offset(BlockPos.of(key)), state));
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
