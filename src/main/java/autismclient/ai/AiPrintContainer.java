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
   private final Minecraft mc = Minecraft.getInstance();
   private final Object level = this.mc.level;
   private final Object player = this.mc.player;
   private final autismclient.ai.print.LivePrintEnv env;
   private final BlockPos pos;
   private final BlockState state;
   private final Map<Integer, ItemStack> savedSlots;
   private final boolean optional;
   private final boolean lockedHopper;
   private final boolean hasSource;
   private final boolean inspectEmpty;
   private final boolean requireInitiallyEmpty;
   private ItemStack supplyRequest = ItemStack.EMPTY;
   private final ArrayList<ItemStack> unavailableSupplies = new ArrayList<>();
   private final HashSet<List<Integer>> creativeStockSnapshots = new HashSet<>();
   private autismclient.ai.print.PrintMachineJob.JobStatus status = autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING;
   private String message = "Waiting for saved container contents";
   private long dispatchedAtNanos;
   private long inventoryRevisionAtDispatch;
   private long hopperUnlockSinceTick = -9223372036854775808L;
   private boolean creativeAtDispatch;
   private boolean flush;
   private AbstractContainerMenu menu;
   private AiSavedContents.Inner_DataUtil slotLayout;
   private AiInventoryController inventory;
   private Map<Item, Integer> removed = Map.of();
   private boolean isChest;
   private autismclient.ai.print.PrintContainerOrder order;
   private boolean contentsVerified;
   private boolean poll;
   private int recoverySlot = -1;
   private ItemStack recoveryStack = ItemStack.EMPTY;
   private String receiptJson = "{}";
   private final ArrayDeque<BlockPos> collectorFrontier = new ArrayDeque<>();
   private final Map<BlockPos, Integer> collectorDepth = new HashMap<>();
   private long collectorRevision = -9223372036854775808L;
   private long collectorQuietSince;
   private long collectorWaitStart = -9223372036854775808L;
   private int collectorMaxDepth;
   private Iterator<BlockPos> collectorScan;
   private int fillIndex;
   private int verifiedSlotCount;
   private int skippedSlotCount;
   private List<String> skipReasons = List.of();
   private List<ItemStack> initialContents;
   private List<Entry<Integer, ItemStack>> fillTargets;
   private CompletableFuture<Boolean> serverAttachCheck;
   private Supplier<String> guard = () -> null;

   AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan saved) {
      this(env, saved, true);
   }

   AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan saved, boolean optional) {
      this(env, saved, optional, false, false);
   }

   static AiPrintContainer forClock(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan source) {
      return new AiPrintContainer(env, source, true, true, false);
   }

   static AiPrintContainer load(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan contents) {
      return new AiPrintContainer(env, contents, false, false, true);
   }

   private AiPrintContainer(autismclient.ai.print.LivePrintEnv env, AiSchematicMachinePlan.ContainerPlan saved, boolean optional, boolean inspectEmpty, boolean requireInitiallyEmpty) {
      this.env = env;
      this.pos = saved.position();
      this.hasSource = saved.source() != null;
      this.state = saved.source() == null ? Blocks.HOPPER.defaultBlockState().setValue(BlockStateProperties.ENABLED, false) : saved.source();
      this.savedSlots = saved.slots();
      this.optional = optional;
      this.inspectEmpty = inspectEmpty;
      this.requireInitiallyEmpty = requireInitiallyEmpty;
      this.lockedHopper = this.state.is(Blocks.HOPPER) && !this.state.getValue(BlockStateProperties.ENABLED);
      int size = AiSavedContents.slotCount(this.state);
      if (!inspectEmpty && this.savedSlots.isEmpty() || size == 0 || this.savedSlots.keySet().stream().anyMatch(index -> index < 0 || index >= size)) {
         throw new IllegalArgumentException("Invalid saved container slots at " + this.pos.toShortString());
      }
   }

   @Override
   public BlockPos position() {
      return this.pos;
   }

   @Override
   public Item item() {
      return Items.AIR;
   }

   @Override
   public String description() {
      return this.state.is(Blocks.HOPPER) ? "hopper contents" : "container contents";
   }

   @Override
   public String detail() {
      return this.message;
   }

   @Override
   public int verifiedSlots() {
      return this.verifiedSlotCount;
   }

   @Override
   public int skippedSlots() {
      return this.skippedSlotCount;
   }

   @Override
   public List<String> skippedReasons() {
      return this.skipReasons;
   }

   @Override
   public void init(Supplier<String> guard) {
      this.guard = Objects.requireNonNull(guard);
   }

   @Override
   public boolean matchesSource(BlockState state) {
      return AiSavedContents.init(this.state, state, this.hasSource) && (!this.lockedHopper || !state.getValue(BlockStateProperties.ENABLED));
   }

   @Override
   public boolean b_() {
      return this.optional && !this.inspectEmpty && !this.requireInitiallyEmpty;
   }

   @Override
   public void init(autismclient.ai.print.PrintContainerOrder order) {
      this.order = order;
   }

   @Override
   public boolean load_Z() {
      return this.status == autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED && this.contentsVerified && this.sameWorld();
   }

   @Override
   public boolean isProvenFull() {
      if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED && this.b_() && this.sameWorld()) {
         this.resetTransfer();
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Rechecking native container capacity after a machine update"
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void invalidateCapacityProof() {
      this.menu = null;
      this.slotLayout = null;
      this.serverAttachCheck = null;
      this.initialContents = null;
      this.fillTargets = null;
      this.inventory = null;
   }

   @Override
   public void init(Map<Item, Integer> removed) {
      if (this.b_()) {
         this.removed = removed;
         this.poll = true;
         this.isChest = this.state.is(Blocks.CHEST);
      }
   }

   @Override
   public boolean write_Z() {
      return this.b_() && this.state.is(Blocks.CHEST) && this.open();
   }

   @Override
   public boolean needsRevisit() {
      if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED
         && this.isChest
         && this.state.is(Blocks.CHEST)
         && !this.removed.values().stream().noneMatch(count -> count > 0)) {
         this.resetTransfer();
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Rechecking the collector after removing construction access"
         );
         return true;
      } else {
         return false;
      }
   }

   private boolean open() {
      boolean fed = false;

      for (BlockPos half : this.chestHalves()) {
         for (Direction direction : Direction.values()) {
            for (int distance = 1; distance <= 2; distance++) {
               BlockPos at = half.relative(direction, distance);
               if (!this.env.hasChunkAt(at)) {
                  return false;
               }

               BlockState state = this.env.getBlockState(at);
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

   private List<BlockPos> chestHalves() {
      return this.state.getValue(ChestBlock.TYPE) == ChestType.SINGLE
         ? List.of(this.pos)
         : List.of(this.pos, this.pos.relative(ChestBlock.getConnectedDirection(this.state)));
   }

   private boolean slotMatches(int menuSlot) {
      ItemStack stack = this.menu.getSlot(menuSlot).getItem();
      ItemStack wanted = this.savedSlots.get(menuSlot - this.slotLayout.offset());
      return menuSlot >= this.slotLayout.offset()
         && menuSlot < this.slotLayout.offset() + this.slotLayout.savedSlots()
         && !stack.isEmpty()
         && stack.getCount() <= this.removed.getOrDefault(stack.getItem(), 0)
         && ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))
         && (wanted == null || !ItemStack.isSameItemSameComponents(stack, wanted))
         && this.menu.getSlot(menuSlot).mayPickup(this.mc.player);
   }

   private boolean init(ItemStack stack) {
      int remaining = stack.getCount();
      Inventory inventory = this.mc.player.getInventory();

      for (Slot slot : this.menu.slots) {
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

   private boolean passiveCollectorSettled() {
      if (this.state.is(Blocks.CHEST) && !this.creativeAtDispatch && !this.removed.isEmpty() && this.open()) {
         long tick = this.env.currentTick();
         if (this.collectorWaitStart == -9223372036854775808L) {
            this.collectorWaitStart = this.collectorQuietSince = tick;

            for (BlockPos half : this.chestHalves()) {
               this.collectorFrontier.add(half);
               this.collectorDepth.put(half, 0);
            }
         }

         for (int checked = 0; checked < 32 && !this.collectorFrontier.isEmpty(); checked++) {
            BlockPos destination = this.collectorFrontier.removeFirst();
            int distance = this.collectorDepth.get(destination) + 1;

            for (Direction side : Direction.values()) {
               BlockPos at = destination.relative(side);
               if (this.env.hasChunkAt(at)) {
                  BlockState state = this.env.getBlockState(at);
                  if (state.is(Blocks.HOPPER)
                     && state.getValue(BlockStateProperties.FACING_HOPPER) == side.getOpposite()
                     && this.collectorDepth.putIfAbsent(at, distance) == null) {
                     this.collectorMaxDepth = Math.max(this.collectorMaxDepth, distance);
                     this.collectorFrontier.addLast(at);
                  }
               }
            }
         }

         long revision = AiServerEvidence.containerRevision(this.menu.containerId);
         if (!this.collectorFrontier.isEmpty() || revision != this.collectorRevision) {
            this.collectorRevision = revision;
            this.collectorQuietSince = tick;
            this.collectorScan = null;
         }

         int quietTicks = 8 * (this.collectorMaxDepth + 1);
         if (this.collectorFrontier.isEmpty() && tick - this.collectorQuietSince >= quietTicks && this.push()) {
            return true;
         } else {
            long allowance = 200L + quietTicks + 8L * this.removed.values().stream().mapToLong(Integer::longValue).sum();
            if (tick - this.collectorWaitStart > allowance) {
               this.verifiedSlotCount = 0;
               this.skippedSlotCount = this.savedSlots.size();
               this.skipReasons = List.of("The passive collector kept changing after construction; its final contents remain unverified");
               this.closeOwnMenu();
               this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.SKIPPED, this.skipReasons.getFirst());
               return false;
            } else {
               this.message = "Waiting for the passive hopper collector to finish delivering construction drops";
               return false;
            }
         }
      } else {
         return true;
      }
   }

   private boolean push() {
      if (this.collectorScan == null) {
         this.collectorScan = this.collectorDepth.keySet().iterator();
      }

      for (int checked = 0; checked < 32 && this.collectorScan.hasNext(); checked++) {
         BlockPos hopper = this.collectorScan.next();
         if (this.env.hasChunkAt(hopper) && this.env.getBlockState(hopper).is(Blocks.HOPPER)) {
            AABB column = new AABB(
               hopper.getX() - 0.25, hopper.getY(), hopper.getZ() - 0.25, hopper.getX() + 1.25, this.mc.level.getMaxY(), hopper.getZ() + 1.25
            );

            for (ItemEntity entity : this.mc.level.getEntitiesOfClass(ItemEntity.class, column)) {
               ItemStack stack = entity.getItem();
               if (this.removed.getOrDefault(stack.getItem(), 0) > 0
                  && ItemStack.isSameItemSameComponents(stack, new ItemStack(stack.getItem()))
                  && (entity.getDeltaMovement().y < -0.001 || entity.getY() < hopper.getY() + 2)) {
                  this.collectorQuietSince = this.env.currentTick();
                  this.collectorScan = null;
                  return false;
               }
            }
         }
      }

      return !this.collectorScan.hasNext();
   }

   @Override
   public boolean sync() {
      if ((
            this.status == autismclient.ai.print.PrintMachineJob.JobStatus.READY
               || this.status == autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING
         )
         && this.sameWorld()
         && this.stateStillMatches()) {
         if (!autismclient.ai.print.PrintContainerAccess.init(this.mc.level, this.pos)) {
            return false;
         } else {
            BlockState actual = this.env.getBlockState(this.pos);
            if (this.lockedHopper && actual.getValue(BlockStateProperties.ENABLED)) {
               return false;
            } else {
               return this.order == null
                  ? !actual.is(Blocks.HOPPER) || !actual.getValue(BlockStateProperties.ENABLED)
                  : this.order.load(this.pos);
            }
         }
      } else {
         return false;
      }
   }

   private boolean sameWorld() {
      return this.mc.level == this.level && this.mc.player == this.player && this.mc.player != null;
   }

   private boolean stateStillMatches() {
      if (!this.env.hasChunkAt(this.pos)) {
         return false;
      }

      BlockState actual = this.env.getBlockState(this.pos);
      return AiSavedContents.init(this.state, actual, this.hasSource)
         && (!this.lockedHopper || this.optional && !this.flush || !actual.getValue(BlockStateProperties.ENABLED));
   }

   @Override
   public autismclient.ai.print.PrintMachineJob.JobStatus prepare() {
      if (this.status != autismclient.ai.print.PrintMachineJob.JobStatus.READY
         && this.status != autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING) {
         return this.status;
      }

      if (AiMenuClose.load()) {
         return this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Waiting for the previous storage menu to close on the server"
         );
      }

      if (!this.sameWorld()) {
         return this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "World or player changed before filling saved contents"
         );
      }

      if (!this.env.hasChunkAt(this.pos)) {
         return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Waiting for the saved container chunk");
      }

      if (!this.stateStillMatches()) {
         if (this.lockedHopper && AiSavedContents.init(this.state, this.env.getBlockState(this.pos), this.hasSource)) {
            if (this.hopperUnlockSinceTick == -9223372036854775808L) {
               this.hopperUnlockSinceTick = this.env.currentTick();
            }

            return this.env.currentTick() - this.hopperUnlockSinceTick >= 100L
               ? this.setStatus(
                  autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "The captured hopper is still unlocked at " + this.pos.toShortString()
               )
               : this.setStatus(
                  autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Waiting for the captured hopper to lock at " + this.pos.toShortString()
               );
         } else {
            return this.setStatus(
               autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "The saved container changed at " + this.pos.toShortString()
            );
         }
      } else {
         this.hopperUnlockSinceTick = -9223372036854775808L;
         if (!autismclient.ai.print.PrintContainerAccess.init(this.mc.level, this.pos)) {
            return this.setStatus(
               autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "The saved shulker lid is obstructed at " + this.pos.toShortString()
            );
         }

         if (!this.inspectEmpty && this.env.preloadContainerSlots(this.pos, this.savedSlots)) {
            return this.setStatus(
               autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Waiting for the qualification inventory supply receipt"
            );
         }

         if (this.mc.gui.screen() == null
            && this.mc.player.containerMenu == this.mc.player.inventoryMenu
            && this.mc.player.inventoryMenu.getCarried().isEmpty()) {
            if (this.env.isCreativeMode()) {
               ArrayList<ItemStack> deficits = new ArrayList<>();

               for (ItemStack wanted : this.savedSlots.values()) {
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
                     ItemStack held = this.env.getInventoryItem(index);
                     if (ItemStack.isSameItemSameComponents(wanted, held)) {
                        need -= held.getCount();
                     }
                  }

                  if (need > 0) {
                     List<Integer> snapshot = deficits.stream()
                        .map(
                           required -> IntStream.range(0, 36)
                              .filter(index -> ItemStack.isSameItemSameComponents(required, this.env.getInventoryItem(index)))
                              .map(index -> this.env.getInventoryItem(index).getCount())
                              .sum()
                        )
                        .toList();
                     if (this.creativeStockSnapshots.add(snapshot)
                        && this.creativeStockSnapshots.size() <= 128
                        && this.env
                           .giveCreativeStackReserved(
                              wanted.copyWithCount(Math.min(need, wanted.getMaxStackSize())),
                              heldx -> deficits.stream().anyMatch(required -> ItemStack.isSameItemSameComponents(required, heldx))
                           )) {
                        return this.setStatus(
                           autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Preparing exact creative stock for saved contents"
                        );
                     }

                     return this.setStatus(
                        autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "Free inventory space for the saved container materials"
                     );
                  }
               }
            }

            return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.READY, "The saved container is ready");
         } else {
            return this.setStatus(
               autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Waiting for the player inventory and empty cursor"
            );
         }
      }
   }

   @Override
   public boolean beginDispatch() {
      if (this.prepare() == autismclient.ai.print.PrintMachineJob.JobStatus.READY && !this.env.isServerSneaking()) {
         this.dispatchedAtNanos = System.nanoTime();
         this.inventoryRevisionAtDispatch = AiServerEvidence.inventoryRevision();
         this.creativeAtDispatch = this.env.isCreativeMode();
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.SENT, "Waiting for the container menu and its server contents"
         );
         return true;
      } else {
         return false;
      }
   }

   @Override
   public autismclient.ai.print.PrintMachineJob.JobStatus pollStatus() {
      if (this.status != autismclient.ai.print.PrintMachineJob.JobStatus.SENT) {
         return this.status;
      }

      if (this.sameWorld() && this.env.isCreativeMode() == this.creativeAtDispatch && this.stateStillMatches()) {
         if (this.menu == null) {
            if (this.mc.gui.screen() == null && this.mc.player.containerMenu == this.mc.player.inventoryMenu) {
               if (System.nanoTime() - this.dispatchedAtNanos < 5000000000L) {
                  return this.status;
               }

               return this.setStatus(
                  autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "The saved container menu did not open"
               );
            }

            if (!(this.mc.gui.screen() instanceof AbstractContainerScreen<?> screen)
               || screen.getMenu() != this.mc.player.containerMenu
               || (this.slotLayout = AiSavedContents.init(this.state, screen.getMenu())) == null) {
               return this.setStatus(
                  autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "A different screen opened during the saved contents action"
               );
            }

            this.menu = screen.getMenu();
            IntegratedServer server = this.mc.getSingleplayerServer();
            if (server != null) {
               UUID uuid = this.mc.player.getUUID();
               ResourceKey<Level> dimension = this.mc.level.dimension();
               int id = this.menu.containerId;
               this.serverAttachCheck = server.submit(
                  () -> {
                     ServerPlayer actor = server.getPlayerList().getPlayer(uuid);
                     if (actor != null
                        && actor.level().dimension().equals(dimension)
                        && actor.containerMenu.containerId == id
                        && AiSavedContents.init(this.state, actor.containerMenu) != null) {
                        BlockEntity entity = actor.level().getBlockEntity(this.pos);
                        Container attached = actor.containerMenu.getSlot(this.slotLayout.offset()).container;
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

         if (this.mc.player.containerMenu == this.menu
            && this.mc.gui.screen() instanceof AbstractContainerScreen<?> screen
            && screen.getMenu() == this.menu) {
            if (this.recoverySlot >= 0) {
               AiOperation.Result progress = this.inventory.tick();
               this.message = progress.message();
               if (!progress.terminal()) {
                  return this.status;
               }

               this.receiptJson = this.inventory.receiptJson();
               if (progress.status() != AiOperation.Status.SUCCEEDED
                  || !this.menu.getSlot(this.recoverySlot).getItem().isEmpty()
                  || !this.menu.getCarried().isEmpty()) {
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "The recovered scaffold material needs inventory reconciliation"
                  );
               }

               this.removed.computeIfPresent(this.recoveryStack.getItem(), (item, countx) -> countx - this.recoveryStack.getCount());
               this.recoverySlot = -1;
               this.recoveryStack = ItemStack.EMPTY;
               this.inventory = null;
               this.initialContents = null;
            }

            if (this.initialContents == null) {
               if (AiServerEvidence.containerRevision(this.menu.containerId) <= this.inventoryRevisionAtDispatch
                  || this.serverAttachCheck != null && !this.serverAttachCheck.isDone()) {
                  if (System.nanoTime() - this.dispatchedAtNanos < 5000000000L) {
                     return this.status;
                  }

                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "The container menu did not provide its initial server snapshot"
                  );
               }

               if (this.serverAttachCheck != null
                  && (this.serverAttachCheck.isCompletedExceptionally() || !Boolean.TRUE.equals(this.serverAttachCheck.getNow(false)))) {
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "The native menu is attached to a different container"
                  );
               }

               this.initialContents = IntStream.range(0, this.slotLayout.menuSlots())
                  .mapToObj(i -> this.menu.getSlot(i).getItem().copy())
                  .toList();
               if (this.inspectEmpty) {
                  boolean empty = this.initialContents.stream().allMatch(ItemStack::isEmpty) && this.menu.getCarried().isEmpty();
                  this.closeOwnMenu();
                  return this.setStatus(
                     empty ? autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED : autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT,
                     empty ? "Fresh native hopper is empty" : "Existing clock contents require observation; no items changed"
                  );
               }

               if (this.requireInitiallyEmpty && (this.initialContents.stream().anyMatch(stack -> !stack.isEmpty()) || !this.menu.getCarried().isEmpty())) {
                  this.closeOwnMenu();
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "Clock preload no longer empty; no saved stock transferred"
                  );
               }

               AiSavedContents.Inner_CachePool plan = AiSavedContents.init(
                  this.menu, this.mc.player.getInventory(), this.mc.player, this.savedSlots, this.slotLayout
               );
               if (this.poll && !this.removed.isEmpty() && this.menu.getCarried().isEmpty()) {
                  AiSavedContents.Inner_CachePool recovery = AiSavedContents.init(
                     this.menu,
                     this.mc.player.getInventory(),
                     this.mc.player,
                     this.savedSlots,
                     this.slotLayout,
                     this::slotMatches
                  );
                  if (recovery.ready()) {
                     for (int index = this.slotLayout.offset(); index < this.slotLayout.offset() + this.slotLayout.savedSlots(); index++) {
                        if (this.slotMatches(index)) {
                           ItemStack stack = this.menu.getSlot(index).getItem();
                           if (this.init(stack)) {
                              String blocked = this.guard.get();
                              if (blocked != null && !blocked.isBlank()) {
                                 this.closeOwnMenu();
                                 return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, blocked);
                              }

                              this.recoverySlot = index;
                              this.recoveryStack = stack.copy();
                              int count = AiInventoryController.countMatching(this.mc.player.getInventory(), this.recoveryStack);
                              this.inventory = new AiInventoryController(
                                 this.menu.containerId, this.recoveryStack, count + this.recoveryStack.getCount(), Set.of(index), null
                              );
                              this.flush = true;
                              return this.setStatus(
                                 autismclient.ai.print.PrintMachineJob.JobStatus.SENT, "Returning recovered scaffold material to the player inventory"
                              );
                           }
                        }
                     }
                  }
               }

               if (plan.conflict() != null) {
                  this.closeOwnMenu();
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, plan.conflict() + " at " + this.pos.toShortString()
                  );
               }

               this.verifiedSlotCount = plan.satisfiedSlots();
               if (!plan.missing().isEmpty()) {
                  if (this.optional && !this.creativeAtDispatch) {
                     for (AiSavedContents.Inner_StreamUtil missing : plan.missing()) {
                        ItemStack wanted = missing.wanted();
                        if (!this.unavailableSupplies.stream().anyMatch(value -> ItemStack.isSameItemSameComponents(value, wanted))
                           && !AiCarriedShulker.init(this.mc.player.getInventory(), wanted, List.of()).isEmpty()) {
                           this.supplyRequest = wanted.copyWithCount(Math.min(missing.count(), wanted.getMaxStackSize()));
                           this.closeOwnMenu();
                           this.resetTransfer();
                           return this.setStatus(
                              autismclient.ai.print.PrintMachineJob.JobStatus.NEEDS_SUPPLY, "Fetching exact saved contents from a carried shulker"
                           );
                        }
                     }
                  }

                  this.skipReasons = plan.missing().stream().map(AiSavedContents.Inner_StreamUtil::message).toList();
                  if (!this.optional) {
                     this.closeOwnMenu();
                     return this.setStatus(
                        autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "Player inventory lacks saved contents. " + String.join("; ", this.skipReasons)
                     );
                  }

                  this.skippedSlotCount = plan.unfilledSlots();
                  this.closeOwnMenu();
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.SKIPPED,
                     "Skipped " + this.skippedSlotCount + " unfilled saved slots at " + this.pos.toShortString() + ". " + String.join("; ", this.skipReasons)
                  );
               }

               this.fillTargets = plan.targets().entrySet().stream().sorted(Entry.comparingByKey()).toList();
            }

            if (this.fillIndex < this.fillTargets.size()) {
               Entry<Integer, ItemStack> target = this.fillTargets.get(this.fillIndex);
               if (this.inventory == null && !ItemStack.matches(this.menu.getSlot(target.getKey()).getItem(), target.getValue())) {
                  if (this.order != null && !this.order.load(this.pos)) {
                     this.closeOwnMenu();
                     return this.setStatus(
                        autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, "The saved inventory cannot hold its stock until its hopper outlets are settled"
                     );
                  }

                  if (this.lockedHopper && this.env.getBlockState(this.pos).getValue(BlockStateProperties.ENABLED)) {
                     this.closeOwnMenu();
                     return this.setStatus(
                        autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT,
                        "The captured hopper is unlocked before filling at " + this.pos.toShortString()
                     );
                  }

                  String blocked = this.guard.get();
                  if (blocked != null && !blocked.isBlank()) {
                     this.closeOwnMenu();
                     return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.CONFLICT, blocked);
                  }

                  this.flush = true;
               }

               if (this.inventory == null) {
                  this.inventory = new AiInventoryController(this.menu.containerId, target.getKey(), target.getValue());
               }

               AiOperation.Result progress = this.inventory.tick();
               this.message = progress.message();
               if (!progress.terminal()) {
                  return this.status;
               }

               this.receiptJson = this.inventory.receiptJson();
               if (progress.status() != AiOperation.Status.SUCCEEDED) {
                  return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, this.message);
               }

               this.inventory = null;
               this.fillIndex++;
               return this.status;
            } else {
               for (int i = 0; i < this.initialContents.size(); i++) {
                  ItemStack wanted = this.initialContents.get(i);

                  for (Entry<Integer, ItemStack> entry : this.fillTargets) {
                     if (entry.getKey() == i) {
                        wanted = entry.getValue();
                     }
                  }

                  if (!ItemStack.matches(this.menu.getSlot(i).getItem(), wanted)) {
                     if (this.slotMatches(i)) {
                        this.initialContents = null;
                        this.fillTargets = null;
                        this.fillIndex = 0;
                        this.collectorQuietSince = this.env.currentTick();
                        return this.status;
                     }

                     return this.setStatus(
                        autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "Container contents changed before final slot verification"
                     );
                  }
               }

               if (!this.menu.getCarried().isEmpty()) {
                  return this.setStatus(
                     autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "Saved contents transfer left an item on the cursor"
                  );
               }

               if (!this.passiveCollectorSettled()) {
                  return this.status;
               }

               if (this.order != null && !this.order.read()) {
                  return this.status;
               }

               this.verifiedSlotCount = this.fillTargets.size();
               if (this.isChest) {
                  for (int raw = 0; raw < this.slotLayout.savedSlots(); raw++) {
                     if (!this.savedSlots.containsKey(raw) && !this.menu.getSlot(this.slotLayout.offset() + raw).getItem().isEmpty()) {
                        this.skipReasons = List.of("Additional items remain in fresh collector slot " + raw + ". Saved slots are verified");
                        this.closeOwnMenu();
                        return this.setStatus(autismclient.ai.print.PrintMachineJob.JobStatus.SKIPPED, this.skipReasons.getFirst());
                     }
                  }
               }

               this.contentsVerified = IntStream.range(this.slotLayout.offset(), this.slotLayout.offset() + this.slotLayout.savedSlots())
                  .allMatch(index -> {
                     Slot cell = this.menu.getSlot(index);
                     ItemStack stack = cell.getItem();
                     return !stack.isEmpty() && stack.getCount() >= cell.getMaxStackSize(stack);
                  });
               this.closeOwnMenu();
               return this.setStatus(
                  autismclient.ai.print.PrintMachineJob.JobStatus.SUCCEEDED, "Saved container slots and empty cursor verified"
               );
            }
         } else {
            return this.setStatus(
               autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "The claimed container menu changed during transfer"
            );
         }
      } else {
         return this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN,
            "Container, world, or game mode changed during saved contents at " + this.pos.toShortString()
         );
      }
   }

   @Override
   public ItemStack extraMaterial() {
      return this.supplyRequest.copy();
   }

   @Override
   public void onContentsAvailable() {
      if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.NEEDS_SUPPLY) {
         this.supplyRequest = ItemStack.EMPTY;
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Rechecking saved contents after recovering the stock box"
         );
      }
   }

   @Override
   public void onContentsUnavailable() {
      if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.NEEDS_SUPPLY) {
         this.unavailableSupplies.add(this.supplyRequest.copyWithCount(1));
         this.supplyRequest = ItemStack.EMPTY;
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.PREPARING, "Rechecking saved contents before skipping unavailable stock"
         );
      }
   }

   private void resetTransfer() {
      this.menu = null;
      this.slotLayout = null;
      this.serverAttachCheck = null;
      this.initialContents = null;
      this.fillTargets = null;
      this.inventory = null;
      this.fillIndex = this.verifiedSlotCount = this.skippedSlotCount = 0;
      this.flush = false;
      this.contentsVerified = false;
      this.skipReasons = List.of();
      this.creativeStockSnapshots.clear();
      this.collectorFrontier.clear();
      this.collectorDepth.clear();
      this.collectorWaitStart = this.collectorRevision = -9223372036854775808L;
      this.collectorMaxDepth = 0;
      this.collectorScan = null;
   }

   private void closeOwnMenu() {
      if (this.mc.player.containerMenu == this.menu && this.menu.getCarried().isEmpty()) {
         AiMenuClose.closeMenu();
         this.env.clearRecentClick();
      }
   }

   @Override
   public void cancel() {
      if (this.inventory != null) {
         this.inventory.cancel();
      }

      if (this.status == autismclient.ai.print.PrintMachineJob.JobStatus.SENT) {
         this.setStatus(
            autismclient.ai.print.PrintMachineJob.JobStatus.UNCERTAIN, "Saved contents action interrupted at " + this.pos.toShortString()
         );
      }
   }

   @Override
   public String receiptJson() {
      JsonObject receipt = AiJson.objectOf(
         "position",
         this.pos.toShortString(),
         "state",
         this.status.name(),
         "detail",
         this.message,
         "contentsChanging",
         this.flush,
         "nativeMenuClaimed",
         this.menu != null,
         "nativeSnapshotObserved",
         this.initialContents != null,
         "initiallySatisfiedSlots",
         this.verifiedSlotCount,
         "confirmedTargetsProcessed",
         this.fillIndex,
         "savedTargetSlots",
         this.savedSlots.size(),
         "menuId",
         this.menu == null ? -1 : this.menu.containerId,
         "currentTargetSlot",
         this.fillTargets != null && this.fillIndex < this.fillTargets.size() ? this.fillTargets.get(this.fillIndex).getKey() : -1
      );
      receipt.add("transfer", JsonParser.parseString(this.inventory == null ? this.receiptJson : this.inventory.receiptJson()));
      return receipt.toString();
   }

   private autismclient.ai.print.PrintMachineJob.JobStatus setStatus(
      autismclient.ai.print.PrintMachineJob.JobStatus next, String message
   ) {
      this.status = next;
      this.message = message;
      return this.status;
   }
}
