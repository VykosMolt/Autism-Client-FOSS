package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;

class AiPrintContainer implements autismclient.ai.print.PrintMachineJob {
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final Object init_Obj = this.init_Minecraft.level;
   private final Object load_Obj = this.init_Minecraft.player;
   private final autismclient.ai.print.LivePrintEnv init_DataUtil;
   private final BlockPos init_BlockPos;
   private final BlockState init_BlockState;
   private final Map<Integer, ItemStack> init_Map;
   private final boolean init_Z;
   private final boolean load_Z;
   private final boolean read_Z;
   private final boolean write_Z;
   private final boolean update_Z;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private final ArrayList<ItemStack> init_ArrayList = new ArrayList<>();
   private final HashSet<List<Integer>> init_HashSet = new HashSet<>();
   private autismclient.ai.print.PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti = autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init;
   private String init_Str = "Waiting for saved container contents";
   private long init_J;
   private long load_J;
   private long read_J = -9223372036854775808L;
   private boolean sync_Z;
   private boolean flush;
   private AbstractContainerMenu init_AbstractContainerM;
   private AiSavedContents.Inner_DataUtil init_bM_DataUtil;
   private AiInventoryController init_ae;
   private Map<Item, Integer> load_Map = Map.of();
   private boolean close;
   private autismclient.ai.print.PrintContainerOrder init_StateCache;
   private boolean open;
   private boolean poll;
   private int init_I = -1;
   private ItemStack load_ItemStack = ItemStack.EMPTY;
   private String load_Str = "{}";
   private final ArrayDeque<BlockPos> init_ArrayDeque = new ArrayDeque<>();
   private final Map<BlockPos, Integer> read_Map = new HashMap<>();
   private long write_J = -9223372036854775808L;
   private long update_J;
   private long sync_J = -9223372036854775808L;
   private int load_I;
   private Iterator<BlockPos> init_Iterator;
   private int read_I;
   private int write_I;
   private int update_I;
   private List<String> init_List = List.of();
   private List<ItemStack> load_List;
   private List<Entry<Integer, ItemStack>> read_List;
   private CompletableFuture<Boolean> init_CompletableFuture;
   private Supplier<String> init_Supplier = () -> null;

   AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil saved) {
      this(env, saved, true);
   }

   AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil saved, boolean optional) {
      this(env, saved, optional, false, false);
   }

   static AiPrintContainer init_bd_DataUtil_bV_DataUtil(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil source) {
      return new AiPrintContainer(env, source, true, true, false);
   }

   static AiPrintContainer load(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil contents) {
      return new AiPrintContainer(env, contents, false, false, true);
   }

   private AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.Inner_DataUtil saved, boolean optional, boolean inspectEmpty, boolean requireInitiallyEmpty) {
      this.init_DataUtil = env;
      this.init_BlockPos = saved.position();
      this.read_Z = saved.source() != null;
      this.init_BlockState = saved.source() == null ? Blocks.HOPPER.defaultBlockState().setValue(BlockStateProperties.ENABLED, false) : saved.source();
      this.init_Map = saved.slots();
      this.init_Z = optional;
      this.write_Z = inspectEmpty;
      this.update_Z = requireInitiallyEmpty;
      this.load_Z = this.init_BlockState.is(Blocks.HOPPER) && !this.init_BlockState.getValue(BlockStateProperties.ENABLED);
      int size = AiSavedContents.init_I(this.init_BlockState);
      if (!inspectEmpty && this.init_Map.isEmpty() || size == 0 || this.init_Map.keySet().stream().anyMatch(index -> index < 0 || index >= size)) {
         throw new IllegalArgumentException("Invalid saved container slots at " + this.init_BlockPos.toShortString());
      }
   }

   @Override
   public BlockPos position() {
      return this.init_BlockPos;
   }

   @Override
   public Item item() {
      return Items.AIR;
   }

   @Override
   public String description() {
      return this.init_BlockState.is(Blocks.HOPPER) ? "hopper contents" : "container contents";
   }

   @Override
   public String detail() {
      return this.init_Str;
   }

   @Override
   public int init_I() {
      return this.write_I;
   }

   @Override
   public int skippedSlots() {
      return this.update_I;
   }

   @Override
   public List<String> skippedReasons() {
      return this.init_List;
   }

   @Override
   public void init(Supplier<String> guard) {
      this.init_Supplier = Objects.requireNonNull(guard);
   }

   @Override
   public boolean matchesSource(BlockState state) {
      return AiSavedContents.init(this.init_BlockState, state, this.read_Z) && (!this.load_Z || !state.getValue(BlockStateProperties.ENABLED));
   }

   @Override
   public boolean b_() {
      return this.init_Z && !this.write_Z && !this.update_Z;
   }

   @Override
   public void init(autismclient.ai.print.PrintContainerOrder order) {
      this.init_StateCache = order;
   }

   @Override
   public boolean load_Z() {
      return this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update && this.open && this.pop();
   }

   @Override
   public boolean read_Z() {
      if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update && this.b_() && this.pop()) {
         this.write_V_2();
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Rechecking native container capacity after a machine update"
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void init_V() {
      this.init_AbstractContainerM = null;
      this.init_bM_DataUtil = null;
      this.init_CompletableFuture = null;
      this.load_List = null;
      this.read_List = null;
      this.init_ae = null;
   }

   @Override
   public void init(Map<Item, Integer> removed) {
      if (this.b_()) {
         this.load_Map = removed;
         this.poll = true;
         this.close = this.init_BlockState.is(Blocks.CHEST);
      }
   }

   @Override
   public boolean write_Z() {
      return this.b_() && this.init_BlockState.is(Blocks.CHEST) && this.open();
   }

   @Override
   public boolean update_Z() {
      if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update
         && this.close
         && this.init_BlockState.is(Blocks.CHEST)
         && !this.load_Map.values().stream().noneMatch(count -> count > 0)) {
         this.write_V_2();
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Rechecking the collector after removing construction access"
         );
         return true;
      } else {
         return false;
      }
   }

   private boolean open() {
      boolean fed = false;

      for (BlockPos half : this.init_List()) {
         for (Direction direction : Direction.values()) {
            for (int distance = 1; distance <= 2; distance++) {
               BlockPos at = half.relative(direction, distance);
               if (!this.init_DataUtil.load_Z(at)) {
                  return false;
               }

               BlockState state = this.init_DataUtil.init_BlockState_BlockPos(at);
               if (state.is(Blocks.COMPARATOR) && state.getValue(ComparatorBlock.FACING) == direction.getOpposite()) {
                  return false;
               }

               if (distance == 1 && state.is(Blocks.HOPPER) && state.getValue(BlockStateProperties.FACING_HOPPER) == direction.getOpposite()) {
                  fed = true;
               }
            }
         }
      }

      return fed;
   }

   private List<BlockPos> init_List() {
      return this.init_BlockState.getValue(ChestBlock.TYPE) == ChestType.SINGLE
         ? List.of(this.init_BlockPos)
         : List.of(this.init_BlockPos, this.init_BlockPos.relative(ChestBlock.getConnectedDirection(this.init_BlockState)));
   }

   private boolean init(int menuSlot) {
      ItemStack stack = this.init_AbstractContainerM.getSlot(menuSlot).getItem();
      ItemStack wanted = this.init_Map.get(menuSlot - this.init_bM_DataUtil.offset());
      return menuSlot >= this.init_bM_DataUtil.offset()
         && menuSlot < this.init_bM_DataUtil.offset() + this.init_bM_DataUtil.savedSlots()
         && !stack.isEmpty()
         && stack.getCount() <= this.load_Map.getOrDefault(stack.getItem(), 0)
         && ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))
         && (wanted == null || !ItemStack.isSameItemSameComponents(stack, wanted))
         && this.init_AbstractContainerM.getSlot(menuSlot).mayPickup(this.init_Minecraft.player);
   }

   private boolean init(ItemStack stack) {
      int remaining = stack.getCount();
      Inventory inventory = this.init_Minecraft.player.getInventory();

      for (Slot slot : this.init_AbstractContainerM.slots) {
         if (slot.container == inventory && slot.mayPlace(stack)) {
            ItemStack current = slot.getItem();
            if (current.isEmpty() || ItemStack.isSameItemSameComponents(current, stack)) {
               remaining -= Math.max(0, slot.getMaxStackSize(stack) - current.getCount());
               if (remaining <= 0) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private boolean poll_Z_2() {
      if (this.init_BlockState.is(Blocks.CHEST) && !this.sync_Z && !this.load_Map.isEmpty() && this.open()) {
         long tick = this.init_DataUtil.init_J();
         if (this.sync_J == -9223372036854775808L) {
            this.sync_J = this.update_J = tick;

            for (BlockPos half : this.init_List()) {
               this.init_ArrayDeque.add(half);
               this.read_Map.put(half, 0);
            }
         }

         for (int checked = 0; checked < 32 && !this.init_ArrayDeque.isEmpty(); checked++) {
            BlockPos destination = this.init_ArrayDeque.removeFirst();
            int distance = this.read_Map.get(destination) + 1;

            for (Direction side : Direction.values()) {
               BlockPos at = destination.relative(side);
               if (this.init_DataUtil.load_Z(at)) {
                  BlockState state = this.init_DataUtil.init_BlockState_BlockPos(at);
                  if (state.is(Blocks.HOPPER)
                     && state.getValue(BlockStateProperties.FACING_HOPPER) == side.getOpposite()
                     && this.read_Map.putIfAbsent(at, distance) == null) {
                     this.load_I = Math.max(this.load_I, distance);
                     this.init_ArrayDeque.addLast(at);
                  }
               }
            }
         }

         long revision = AiServerEvidence.init_J_I(this.init_AbstractContainerM.containerId);
         if (!this.init_ArrayDeque.isEmpty() || revision != this.write_J) {
            this.write_J = revision;
            this.update_J = tick;
            this.init_Iterator = null;
         }

         int quietTicks = 8 * (this.load_I + 1);
         if (this.init_ArrayDeque.isEmpty() && tick - this.update_J >= quietTicks && this.push()) {
            return true;
         } else {
            long allowance = 200L + quietTicks + 8L * this.load_Map.values().stream().mapToLong(Integer::longValue).sum();
            if (tick - this.sync_J > allowance) {
               this.write_I = 0;
               this.update_I = this.init_Map.size();
               this.init_List = List.of("The passive collector kept changing after construction; its final contents remain unverified");
               this.update_V();
               this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.sync, this.init_List.getFirst());
               return false;
            } else {
               this.init_Str = "Waiting for the passive hopper collector to finish delivering construction drops";
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private boolean push() {
      if (this.init_Iterator == null) {
         this.init_Iterator = this.read_Map.keySet().iterator();
      }

      for (int checked = 0; checked < 32 && this.init_Iterator.hasNext(); checked++) {
         BlockPos hopper = this.init_Iterator.next();
         if (this.init_DataUtil.load_Z(hopper) && this.init_DataUtil.init_BlockState_BlockPos(hopper).is(Blocks.HOPPER)) {
            AABB column = new AABB(
               hopper.getX() - 0.25, hopper.getY(), hopper.getZ() - 0.25, hopper.getX() + 1.25, this.init_Minecraft.level.getMaxY(), hopper.getZ() + 1.25
            );

            for (ItemEntity entity : this.init_Minecraft.level.getEntitiesOfClass(ItemEntity.class, column)) {
               ItemStack stack = entity.getItem();
               if (this.load_Map.getOrDefault(stack.getItem(), 0) > 0
                  && ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))
                  && (entity.getDeltaMovement().y < -0.001 || entity.getY() < hopper.getY() + 2)) {
                  this.update_J = this.init_DataUtil.init_J();
                  this.init_Iterator = null;
                  return false;
               }
            }
         }
      }

      return !this.init_Iterator.hasNext();
   }

   @Override
   public boolean sync() {
      if ((
            this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load
               || this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init
         )
         && this.pop()
         && this.tick()) {
         if (!autismclient.ai.print.PrintContainerAccess.init(this.init_Minecraft.level, this.init_BlockPos)) {
            return false;
         } else {
            BlockState actual = this.init_DataUtil.init_BlockState_BlockPos(this.init_BlockPos);
            if (this.load_Z && actual.getValue(BlockStateProperties.ENABLED)) {
               return false;
            } else {
               return this.init_StateCache == null
                  ? !actual.is(Blocks.HOPPER) || !actual.getValue(BlockStateProperties.ENABLED)
                  : this.init_StateCache.load(this.init_BlockPos);
            }
         }
      } else {
         return false;
      }
   }

   private boolean pop() {
      return this.init_Minecraft.level == this.init_Obj && this.init_Minecraft.player == this.load_Obj && this.init_Minecraft.player != null;
   }

   private boolean tick() {
      if (!this.init_DataUtil.load_Z(this.init_BlockPos)) {
         return false;
      }

      BlockState actual = this.init_DataUtil.init_BlockState_BlockPos(this.init_BlockPos);
      return AiSavedContents.init(this.init_BlockState, actual, this.read_Z)
         && (!this.load_Z || this.init_Z && !this.flush || !actual.getValue(BlockStateProperties.ENABLED));
   }

   @Override
   public autismclient.ai.print.PrintMachineJob.Inner_DataUtil prepare() {
      if (this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load
         && this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init) {
         return this.init_BitSetUtil_DataUti;
      }

      if (AiMenuClose.load()) {
         return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Waiting for the previous storage menu to close on the server"
         );
      }

      if (!this.pop()) {
         return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "World or player changed before filling saved contents"
         );
      }

      if (!this.init_DataUtil.load_Z(this.init_BlockPos)) {
         return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Waiting for the saved container chunk");
      }

      if (!this.tick()) {
         if (this.load_Z && AiSavedContents.init(this.init_BlockState, this.init_DataUtil.init_BlockState_BlockPos(this.init_BlockPos), this.read_Z)) {
            if (this.read_J == -9223372036854775808L) {
               this.read_J = this.init_DataUtil.init_J();
            }

            return this.init_DataUtil.init_J() - this.read_J >= 100L
               ? this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                  autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "The captured hopper is still unlocked at " + this.init_BlockPos.toShortString()
               )
               : this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                  autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Waiting for the captured hopper to lock at " + this.init_BlockPos.toShortString()
               );
         } else {
            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
               autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "The saved container changed at " + this.init_BlockPos.toShortString()
            );
         }
      } else {
         this.read_J = -9223372036854775808L;
         if (!autismclient.ai.print.PrintContainerAccess.init(this.init_Minecraft.level, this.init_BlockPos)) {
            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
               autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "The saved shulker lid is obstructed at " + this.init_BlockPos.toShortString()
            );
         }

         if (!this.write_Z && this.init_DataUtil.init_Z_BlockPos_Map(this.init_BlockPos, this.init_Map)) {
            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
               autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Waiting for the qualification inventory supply receipt"
            );
         }

         if (this.init_Minecraft.gui.screen() == null
            && this.init_Minecraft.player.containerMenu == this.init_Minecraft.player.inventoryMenu
            && this.init_Minecraft.player.inventoryMenu.getCarried().isEmpty()) {
            if (this.init_DataUtil.update_Z()) {
               ArrayList<ItemStack> deficits = new ArrayList<>();

               for (ItemStack wanted : this.init_Map.values()) {
                  Optional<ItemStack> same = deficits.stream().filter(value -> ItemStack.isSameItemSameComponents(value, wanted)).findFirst();
                  if (same.isPresent()) {
                     same.get().grow(wanted.getCount());
                  } else {
                     deficits.add(wanted.copy());
                  }
               }

               for (ItemStack wanted : deficits) {
                  int need = wanted.getCount();

                  for (int index = 0; index < 36 && need > 0; index++) {
                     ItemStack held = this.init_DataUtil.init_ItemStack_I(index);
                     if (ItemStack.isSameItemSameComponents(wanted, held)) {
                        need -= held.getCount();
                     }
                  }

                  if (need > 0) {
                     List<Integer> snapshot = deficits.stream()
                        .map(
                           required -> IntStream.range(0, 36)
                              .filter(index -> ItemStack.isSameItemSameComponents(required, this.init_DataUtil.init_ItemStack_I(index)))
                              .map(index -> this.init_DataUtil.init_ItemStack_I(index).getCount())
                              .sum()
                        )
                        .toList();
                     if (this.init_HashSet.add(snapshot)
                        && this.init_HashSet.size() <= 128
                        && this.init_DataUtil
                           .init_Z_ItemStack_Predicate(
                              wanted.copyWithCount(Math.min(need, wanted.getMaxStackSize())),
                              heldx -> deficits.stream().anyMatch(required -> ItemStack.isSameItemSameComponents(required, heldx))
                           )) {
                        return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                           autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Preparing exact creative stock for saved contents"
                        );
                     }

                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                        autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "Free inventory space for the saved container materials"
                     );
                  }
               }
            }

            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load, "The saved container is ready");
         } else {
            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
               autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Waiting for the player inventory and empty cursor"
            );
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.load && !this.init_DataUtil.load_Z()) {
         this.init_J = System.nanoTime();
         this.load_J = AiServerEvidence.init_J();
         this.sync_Z = this.init_DataUtil.update_Z();
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read, "Waiting for the container menu and its server contents"
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public autismclient.ai.print.PrintMachineJob.Inner_DataUtil poll_BitSetUtil_DataUti() {
      if (this.init_BitSetUtil_DataUti != autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read) {
         return this.init_BitSetUtil_DataUti;
      }

      if (this.pop() && this.init_DataUtil.update_Z() == this.sync_Z && this.tick()) {
         if (this.init_AbstractContainerM == null) {
            if (this.init_Minecraft.gui.screen() == null && this.init_Minecraft.player.containerMenu == this.init_Minecraft.player.inventoryMenu) {
               if (System.nanoTime() - this.init_J < 5000000000L) {
                  return this.init_BitSetUtil_DataUti;
               }

               return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                  autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "The saved container menu did not open"
               );
            }

            if (!(this.init_Minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen)
               || screen.getMenu() != this.init_Minecraft.player.containerMenu
               || (this.init_bM_DataUtil = AiSavedContents.init(this.init_BlockState, screen.getMenu())) == null) {
               return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                  autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "A different screen opened during the saved contents action"
               );
            }

            this.init_AbstractContainerM = screen.getMenu();
            IntegratedServer server = this.init_Minecraft.getSingleplayerServer();
            if (server != null) {
               UUID uuid = this.init_Minecraft.player.getUUID();
               ResourceKey<Level> dimension = this.init_Minecraft.level.dimension();
               int id = this.init_AbstractContainerM.containerId;
               this.init_CompletableFuture = server.submit(
                  () -> {
                     ServerPlayer actor = server.getPlayerList().getPlayer(uuid);
                     if (actor != null
                        && actor.level().dimension().equals(dimension)
                        && actor.containerMenu.containerId == id
                        && AiSavedContents.init(this.init_BlockState, actor.containerMenu) != null) {
                        BlockEntity entity = actor.level().getBlockEntity(this.init_BlockPos);
                        Container attached = actor.containerMenu.getSlot(this.init_bM_DataUtil.offset()).container;
                        return entity != null
                           && (
                              attached == entity
                                 || entity instanceof Container storage && attached instanceof CompoundContainer combined && combined.contains(storage)
                           );
                     } else {
                        return false;
                     }
                  }
               );
            }
         }

         if (this.init_Minecraft.player.containerMenu == this.init_AbstractContainerM
            && this.init_Minecraft.gui.screen() instanceof AbstractContainerScreen<?> screen
            && screen.getMenu() == this.init_AbstractContainerM) {
            if (this.init_I >= 0) {
               AiOperation.Result progress = this.init_ae.tick();
               this.init_Str = progress.message();
               if (!progress.terminal()) {
                  return this.init_BitSetUtil_DataUti;
               }

               this.load_Str = this.init_ae.init_Str_13();
               if (progress.status() != AiOperation.Status.SUCCEEDED
                  || !this.init_AbstractContainerM.getSlot(this.init_I).getItem().isEmpty()
                  || !this.init_AbstractContainerM.getCarried().isEmpty()) {
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "The recovered scaffold material needs inventory reconciliation"
                  );
               }

               this.load_Map.computeIfPresent(this.load_ItemStack.getItem(), (item, countx) -> countx - this.load_ItemStack.getCount());
               this.init_I = -1;
               this.load_ItemStack = ItemStack.EMPTY;
               this.init_ae = null;
               this.load_List = null;
            }

            if (this.load_List == null) {
               if (AiServerEvidence.init_J_I(this.init_AbstractContainerM.containerId) <= this.load_J
                  || this.init_CompletableFuture != null && !this.init_CompletableFuture.isDone()) {
                  if (System.nanoTime() - this.init_J < 5000000000L) {
                     return this.init_BitSetUtil_DataUti;
                  }

                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "The container menu did not provide its initial server snapshot"
                  );
               }

               if (this.init_CompletableFuture != null
                  && (this.init_CompletableFuture.isCompletedExceptionally() || !Boolean.TRUE.equals(this.init_CompletableFuture.getNow(false)))) {
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "The native menu is attached to a different container"
                  );
               }

               this.load_List = IntStream.range(0, this.init_bM_DataUtil.menuSlots())
                  .mapToObj(i -> this.init_AbstractContainerM.getSlot(i).getItem().copy())
                  .toList();
               if (this.write_Z) {
                  boolean empty = this.load_List.stream().allMatch(ItemStack::isEmpty) && this.init_AbstractContainerM.getCarried().isEmpty();
                  this.update_V();
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     empty ? autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update : autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush,
                     empty ? "Fresh native hopper is empty" : "Existing clock contents require observation; no items changed"
                  );
               }

               if (this.update_Z && (this.load_List.stream().anyMatch(stack -> !stack.isEmpty()) || !this.init_AbstractContainerM.getCarried().isEmpty())) {
                  this.update_V();
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "Clock preload no longer empty; no saved stock transferred"
                  );
               }

               AiSavedContents.Inner_CachePool plan = AiSavedContents.init(
                  this.init_AbstractContainerM, this.init_Minecraft.player.getInventory(), this.init_Minecraft.player, this.init_Map, this.init_bM_DataUtil
               );
               if (this.poll && !this.load_Map.isEmpty() && this.init_AbstractContainerM.getCarried().isEmpty()) {
                  AiSavedContents.Inner_CachePool recovery = AiSavedContents.init(
                     this.init_AbstractContainerM,
                     this.init_Minecraft.player.getInventory(),
                     this.init_Minecraft.player,
                     this.init_Map,
                     this.init_bM_DataUtil,
                     this::init
                  );
                  if (recovery.ready()) {
                     for (int index = this.init_bM_DataUtil.offset(); index < this.init_bM_DataUtil.offset() + this.init_bM_DataUtil.savedSlots(); index++) {
                        if (this.init(index)) {
                           ItemStack stack = this.init_AbstractContainerM.getSlot(index).getItem();
                           if (this.init(stack)) {
                              String blocked = this.init_Supplier.get();
                              if (blocked != null && !blocked.isBlank()) {
                                 this.update_V();
                                 return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, blocked);
                              }

                              this.init_I = index;
                              this.load_ItemStack = stack.copy();
                              int count = AiInventoryController.init_I_Container_ItemStack_2(this.init_Minecraft.player.getInventory(), this.load_ItemStack);
                              this.init_ae = new AiInventoryController(
                                 this.init_AbstractContainerM.containerId, this.load_ItemStack, count + this.load_ItemStack.getCount(), Set.of(index), null
                              );
                              this.flush = true;
                              return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                                 autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read, "Returning recovered scaffold material to the player inventory"
                              );
                           }
                        }
                     }
                  }
               }

               if (plan.conflict() != null) {
                  this.update_V();
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, plan.conflict() + " at " + this.init_BlockPos.toShortString()
                  );
               }

               this.write_I = plan.satisfiedSlots();
               if (!plan.missing().isEmpty()) {
                  if (this.init_Z && !this.sync_Z) {
                     for (AiSavedContents.Inner_StreamUtil missing : plan.missing()) {
                        ItemStack wanted = missing.wanted();
                        if (!this.init_ArrayList.stream().anyMatch(value -> ItemStack.isSameItemSameComponents(value, wanted))
                           && !AiCarriedShulker.init(this.init_Minecraft.player.getInventory(), wanted, List.of()).isEmpty()) {
                           this.init_ItemStack = wanted.copyWithCount(Math.min(missing.count(), wanted.getMaxStackSize()));
                           this.update_V();
                           this.write_V_2();
                           return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                              autismclient.ai.print.PrintMachineJob.Inner_DataUtil.write, "Fetching exact saved contents from a carried shulker"
                           );
                        }
                     }
                  }

                  this.init_List = plan.missing().stream().map(AiSavedContents.Inner_StreamUtil::message).toList();
                  if (!this.init_Z) {
                     this.update_V();
                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                        autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "Player inventory lacks saved contents. " + String.join("; ", this.init_List)
                     );
                  }

                  this.update_I = plan.unfilledSlots();
                  this.update_V();
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.sync,
                     "Skipped " + this.update_I + " unfilled saved slots at " + this.init_BlockPos.toShortString() + ". " + String.join("; ", this.init_List)
                  );
               }

               this.read_List = plan.targets().entrySet().stream().sorted(Entry.comparingByKey()).toList();
            }

            if (this.read_I < this.read_List.size()) {
               Entry<Integer, ItemStack> target = this.read_List.get(this.read_I);
               if (this.init_ae == null && !ItemStack.matches(this.init_AbstractContainerM.getSlot(target.getKey()).getItem(), target.getValue())) {
                  if (this.init_StateCache != null && !this.init_StateCache.load(this.init_BlockPos)) {
                     this.update_V();
                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                        autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, "The saved inventory cannot hold its stock until its hopper outlets are settled"
                     );
                  }

                  if (this.load_Z && this.init_DataUtil.init_BlockState_BlockPos(this.init_BlockPos).getValue(BlockStateProperties.ENABLED)) {
                     this.update_V();
                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                        autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush,
                        "The captured hopper is unlocked before filling at " + this.init_BlockPos.toShortString()
                     );
                  }

                  String blocked = this.init_Supplier.get();
                  if (blocked != null && !blocked.isBlank()) {
                     this.update_V();
                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.flush, blocked);
                  }

                  this.flush = true;
               }

               if (this.init_ae == null) {
                  this.init_ae = new AiInventoryController(this.init_AbstractContainerM.containerId, target.getKey(), target.getValue());
               }

               AiOperation.Result progress = this.init_ae.tick();
               this.init_Str = progress.message();
               if (!progress.terminal()) {
                  return this.init_BitSetUtil_DataUti;
               }

               this.load_Str = this.init_ae.init_Str_13();
               if (progress.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, this.init_Str);
               }

               this.init_ae = null;
               this.read_I++;
               return this.init_BitSetUtil_DataUti;
            } else {
               for (int i = 0; i < this.load_List.size(); i++) {
                  ItemStack wanted = this.load_List.get(i);

                  for (Entry<Integer, ItemStack> entry : this.read_List) {
                     if (entry.getKey() == i) {
                        wanted = entry.getValue();
                     }
                  }

                  if (!ItemStack.matches(this.init_AbstractContainerM.getSlot(i).getItem(), wanted)) {
                     if (this.init(i)) {
                        this.load_List = null;
                        this.read_List = null;
                        this.read_I = 0;
                        this.update_J = this.init_DataUtil.init_J();
                        return this.init_BitSetUtil_DataUti;
                     }

                     return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                        autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "Container contents changed before final slot verification"
                     );
                  }
               }

               if (!this.init_AbstractContainerM.getCarried().isEmpty()) {
                  return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                     autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "Saved contents transfer left an item on the cursor"
                  );
               }

               if (!this.poll_Z_2()) {
                  return this.init_BitSetUtil_DataUti;
               }

               if (this.init_StateCache != null && !this.init_StateCache.read()) {
                  return this.init_BitSetUtil_DataUti;
               }

               this.write_I = this.read_List.size();
               if (this.close) {
                  for (int raw = 0; raw < this.init_bM_DataUtil.savedSlots(); raw++) {
                     if (!this.init_Map.containsKey(raw) && !this.init_AbstractContainerM.getSlot(this.init_bM_DataUtil.offset() + raw).getItem().isEmpty()) {
                        this.init_List = List.of("Additional items remain in fresh collector slot " + raw + ". Saved slots are verified");
                        this.update_V();
                        return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(autismclient.ai.print.PrintMachineJob.Inner_DataUtil.sync, this.init_List.getFirst());
                     }
                  }
               }

               this.open = IntStream.range(this.init_bM_DataUtil.offset(), this.init_bM_DataUtil.offset() + this.init_bM_DataUtil.savedSlots())
                  .allMatch(index -> {
                     Slot cell = this.init_AbstractContainerM.getSlot(index);
                     ItemStack stack = cell.getItem();
                     return !stack.isEmpty() && stack.getCount() >= cell.getMaxStackSize(stack);
                  });
               this.update_V();
               return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
                  autismclient.ai.print.PrintMachineJob.Inner_DataUtil.update, "Saved container slots and empty cursor verified"
               );
            }
         } else {
            return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
               autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "The claimed container menu changed during transfer"
            );
         }
      } else {
         return this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close,
            "Container, world, or game mode changed during saved contents at " + this.init_BlockPos.toShortString()
         );
      }
   }

   @Override
   public ItemStack init_ItemStack() {
      return this.init_ItemStack.copy();
   }

   @Override
   public void load_V() {
      if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.write) {
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Rechecking saved contents after recovering the stock box"
         );
      }
   }

   @Override
   public void read_V() {
      if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.write) {
         this.init_ArrayList.add(this.init_ItemStack.copyWithCount(1));
         this.init_ItemStack = ItemStack.EMPTY;
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.init, "Rechecking saved contents before skipping unavailable stock"
         );
      }
   }

   private void write_V_2() {
      this.init_AbstractContainerM = null;
      this.init_bM_DataUtil = null;
      this.init_CompletableFuture = null;
      this.load_List = null;
      this.read_List = null;
      this.init_ae = null;
      this.read_I = this.write_I = this.update_I = 0;
      this.flush = false;
      this.open = false;
      this.init_List = List.of();
      this.init_HashSet.clear();
      this.init_ArrayDeque.clear();
      this.read_Map.clear();
      this.sync_J = this.write_J = -9223372036854775808L;
      this.load_I = 0;
      this.init_Iterator = null;
   }

   private void update_V() {
      if (this.init_Minecraft.player.containerMenu == this.init_AbstractContainerM && this.init_AbstractContainerM.getCarried().isEmpty()) {
         AiMenuClose.init_Z();
         this.init_DataUtil.load_V();
      }
   }

   @Override
   public void cancel() {
      if (this.init_ae != null) {
         this.init_ae.cancel();
      }

      if (this.init_BitSetUtil_DataUti == autismclient.ai.print.PrintMachineJob.Inner_DataUtil.read) {
         this.init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
            autismclient.ai.print.PrintMachineJob.Inner_DataUtil.close, "Saved contents action interrupted at " + this.init_BlockPos.toShortString()
         );
      }
   }

   @Override
   public String init_Str() {
      JsonObject receipt = AiJson.init_JsonObject_ObjArr(
         "position",
         this.init_BlockPos.toShortString(),
         "state",
         this.init_BitSetUtil_DataUti.name(),
         "detail",
         this.init_Str,
         "contentsChanging",
         this.flush,
         "nativeMenuClaimed",
         this.init_AbstractContainerM != null,
         "nativeSnapshotObserved",
         this.load_List != null,
         "initiallySatisfiedSlots",
         this.write_I,
         "confirmedTargetsProcessed",
         this.read_I,
         "savedTargetSlots",
         this.init_Map.size(),
         "menuId",
         this.init_AbstractContainerM == null ? -1 : this.init_AbstractContainerM.containerId,
         "currentTargetSlot",
         this.read_List != null && this.read_I < this.read_List.size() ? this.read_List.get(this.read_I).getKey() : -1
      );
      receipt.add("transfer", JsonParser.parseString(this.init_ae == null ? this.load_Str : this.init_ae.init_Str_13()));
      return receipt.toString();
   }

   private autismclient.ai.print.PrintMachineJob.Inner_DataUtil init_BitSetUtil_DataUti_BitSetUtil_DataUti_Str(
      autismclient.ai.print.PrintMachineJob.Inner_DataUtil next, String message
   ) {
      this.init_BitSetUtil_DataUti = next;
      this.init_Str = message;
      return this.init_BitSetUtil_DataUti;
   }
}
