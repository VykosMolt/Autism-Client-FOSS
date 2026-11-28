package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

final class AiPrintSupply {
   private static final Direction[] DIRECTIONS = Direction.values();
   private final Minecraft mc = Minecraft.getInstance();
   private final AiPrintSupply.SupplyPreparation prep;
   private final Map<Long, AiBuilder.CellSpec> cells;
   private final AiPrintSupply.ShortageTracker shortage = new AiPrintSupply.ShortageTracker();
   private List<BlockPos> terrainCells;
   private int read;
   private AiPrintSupply.StockSourceScan stockScan;
   private ItemStack carriedBox = ItemStack.EMPTY;
   private ItemStack contentsWanted = ItemStack.EMPTY;
   private final List<ItemStack> triedBoxes = new ArrayList<>();
   int shulkerRefills;
   int terrainCleared;
   private int write = 1;

   int init_I() {
      return this.write;
   }

   static void validateOptions(AiBuilder.BuildOptions power) {
      if (!power.substitutes().isEmpty()) {
         throw new IllegalArgumentException("The schematic printer requires exact materials; remove substitutes or use the legacy builder");
      }
   }

   AiPrintSupply(AiBuilder.Blueprint blueprint) {
      this(blueprint, false);
   }

   AiPrintSupply(AiBuilder.Blueprint blueprint, boolean clearTerrain) {
      ClientLevel world = this.mc.level;
      this.prep = new AiPrintSupply.SupplyPreparation(
         blueprint,
         world,
         p -> AiTerrain.isLoaded(world, p),
         this.mc.player == null ? BlockPos.ZERO : this.mc.player.blockPosition(),
         clearTerrain
      );
      this.cells = this.prep.cells;
      this.terrainCells = this.prep.terrainCells;
   }

   boolean prepare(int workBudget) {
      this.prep.step(workBudget);
      return this.preparationSettled();
   }

   void notifyBlockChanged(BlockPos position) {
      this.prep.noteChanged(position);
   }

   boolean preparationSettled() {
      return this.prep.settled();
   }

   boolean isPrepared() {
      return this.prep.prepared;
   }

   long preparationDone() {
      return this.prep.indexed
         + 6L * this.prep.supportCursor
         + this.prep.supportSide
         + this.prep.depotScan.load
         + this.prep.terrainCursor;
   }

   long preparationTotal() {
      return 7L * this.prep.blueprint.cells().size()
         + this.prep.depotScan.total
         + (
            this.prep.clearTerrain && this.prep.indexed == this.prep.blueprint.cells().size()
               ? this.prep.boxVolume()
               : 0L
         );
   }

   String getStatus() {
      return this.prep.status();
   }

   void load(BlockPos pos) {
      this.prep.load(pos);
   }

   void notifyChunkChanged(long key) {
      this.prep.noteChunkChanged(key);
   }

   static int init(Inventory inventory, Item item) {
      return load(inventory, item);
   }

   static int load(Inventory inventory, Item item) {
      int count = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (autismclient.ai.print.PrintCore.matchesItem(stack, item)) {
            count += stack.getCount();
         }
      }

      ItemStack offhand = inventory.getItem(40);
      return autismclient.ai.print.PrintCore.matchesItem(offhand, item) ? count + offhand.getCount() : count;
   }

   boolean canSupply(Item item) {
      return this.shortage.init(item, item == null ? 0 : load(this.mc.player.getInventory(), item));
   }

   Item getMissingItem() {
      return this.shortage.item;
   }

   boolean hasPortableStock() {
      return this.shortage.item != null
         && !AiCarriedShulker.init(this.mc.player.getInventory(), this.shortage.item, this.shortage.triedBoxes, true).isEmpty();
   }

   void completeRefill() {
      this.shortage.init();
      this.stockScan = null;
      this.carriedBox = ItemStack.EMPTY;
      this.shulkerRefills++;
   }

   void resetRefill() {
      this.shortage.init();
      this.stockScan = null;
      this.carriedBox = ItemStack.EMPTY;
   }

   AiOperation refillOperation(ItemStack wanted, int additional, Set<Long> owned) {
      if (!wanted.isEmpty() && additional > 0 && this.mc.player != null && !this.mc.player.hasInfiniteMaterials()) {
         if (!ItemStack.isSameItemSameComponents(this.contentsWanted, wanted)) {
            this.clearContentsRequest();
            this.contentsWanted = wanted.copyWithCount(1);
         }

         if (this.triedBoxes.size() >= 37) {
            return null;
         }

         ItemStack box = AiCarriedShulker.init(this.mc.player.getInventory(), wanted, this.triedBoxes);
         if (box.isEmpty()) {
            return null;
         }

         this.triedBoxes.add(box.copy());
         return new AiCarriedShulker(box, wanted, additional, init(this.ownedCells(), owned));
      } else {
         return null;
      }
   }

   void countRefill() {
      this.shulkerRefills++;
   }

   void clearContentsRequest() {
      this.contentsWanted = ItemStack.EMPTY;
      this.triedBoxes.clear();
   }

   Set<Long> ownedCells() {
      if (!this.isPrepared()) {
         throw new IllegalStateException("Schematic supply preparation is still running");
      } else {
         return this.prep.ownedCells;
      }
   }

   boolean load(int workBudget) {
      if (!this.isPrepared()) {
         return false;
      }

      if (this.shortage.item == null) {
         this.stockScan = null;
         this.carriedBox = ItemStack.EMPTY;
         return true;
      }

      if (this.stockScan == null || this.stockScan.item != this.shortage.item) {
         this.carriedBox = AiCarriedShulker.init(
            this.mc.player.getInventory(), this.shortage.item, this.shortage.triedBoxes, true
         );
         if (this.carriedBox.isEmpty() && this.shortage.triedSources.size() >= 3) {
            return true;
         }

         this.stockScan = new AiPrintSupply.StockSourceScan(
            this.prep,
            this.shortage.item,
            this.shortage.triedSources,
            this.mc.player.blockPosition(),
            this.mc.player.position()
         );
         if (!this.carriedBox.isEmpty()) {
            this.stockScan.skipDepotScan();
         }
      }

      this.stockScan.init(workBudget);
      if (this.stockScan.finished
         && !this.carriedBox.isEmpty()
         && AiCarriedShulker.init(this.mc.player.getInventory(), this.carriedBox, this.shortage.item) == null) {
         this.stockScan = null;
         this.carriedBox = ItemStack.EMPTY;
         return false;
      } else if (this.stockScan.finished
         && this.stockScan.bestSource != null
         && AiTerrain.isLoaded(this.mc.level, this.stockScan.bestSource)
         && !(this.mc.level.getBlockState(this.stockScan.bestSource).getBlock() instanceof ShulkerBoxBlock)) {
         this.shortage.init(this.stockScan.bestSource.asLong());
         this.stockScan = null;
         return false;
      } else {
         return this.stockScan.finished;
      }
   }

   AiOperation stockRefillOperation(Set<Long> owned) {
      if (this.shortage.item != null && (!this.carriedBox.isEmpty() || this.shortage.triedSources.size() < 3)) {
         if (this.stockScan != null && this.stockScan.finished && this.stockScan.item == this.shortage.item) {
            BlockPos source = this.stockScan.bestSource;
            int need = this.stockScan.load();
            this.write = this.stockScan.neededCount();
            this.stockScan = null;
            Set<Long> keep = init(this.ownedCells(), owned);
            if (!this.carriedBox.isEmpty()) {
               ItemStack selected = this.carriedBox;
               this.carriedBox = ItemStack.EMPTY;
               Item supplied = AiCarriedShulker.init(this.mc.player.getInventory(), selected, this.shortage.item);
               if (supplied == null) {
                  return null;
               }

               this.shortage.triedBoxes.add(selected.copy());
               return new AiCarriedShulker(selected, supplied, need, keep);
            } else {
               if (source == null) {
                  return null;
               }

               if (!this.shortage.init(source.asLong())) {
                  return null;
               }

               JsonArray items = new JsonArray();
               items.add(AiJson.objectOf("item", BuiltInRegistries.ITEM.getKey(this.shortage.item).toString(), "count", need));
               AiOperation approach = AiTerrain.isLoaded(this.mc.level, source) && autismclient.ai.engine.AiBlockInteraction.findReachableHit(this.mc, source) != null
                  ? null
                  : AiContainer.init(AiGoto.approachBlockToInteractCompat(source, 60, keep, false));
               return new AiPrintSupply.StockWithdrawOperation(
                  approach,
                  () -> !AiTerrain.isLoaded(this.mc.level, source)
                     ? AiPrintSupply.StockState.UNKNOWN
                     : (
                        this.mc.level.getBlockState(source).getBlock() instanceof ShulkerBoxBlock
                           ? AiPrintSupply.StockState.SHULKER
                           : AiPrintSupply.StockState.REPLACED
                     ),
                  () -> AiContainer.init(
                     AiJson.objectOf(
                        "action",
                        "withdraw",
                        "x",
                        source.getX(),
                        "y",
                        source.getY(),
                        "z",
                        source.getZ(),
                        "items",
                        items,
                        "navigate",
                        false,
                        "timeoutSeconds",
                        60
                     ),
                     keep
                  ),
                  System.nanoTime() + 60000000000L
               );
            }
         } else {
            throw new IllegalStateException("Schematic stock scan is still running");
         }
      } else {
         return null;
      }
   }

   BlockPos nextTerrainCell() {
      if (!this.isPrepared()) {
         throw new IllegalStateException("Schematic terrain preparation is still running");
      }

      for (int checked = 0; this.read < this.terrainCells.size() && checked < 64; checked++) {
         BlockPos p = this.terrainCells.get(this.read++);
         if (!AiTerrain.isLoaded(this.mc.level, p)) {
            throw new IllegalStateException("Terrain is not loaded at " + p.toShortString());
         }

         if (AiSchematicActions.init(this.mc.level.getBlockState(p))
            && !this.prep.ownedCells.contains(p.asLong())
            && !this.prep.init_Z_BlockPos(p)) {
            if (!AiSchematicActions.init(this.mc.level, p)) {
               throw new IllegalStateException("Clearing terrain would release fluid at " + p.toShortString());
            }

            return p;
         }
      }

      return null;
   }

   boolean isTerrainCleared() {
      return this.isPrepared() && this.read >= this.terrainCells.size();
   }

   private static Set<Long> newCellSet() {
      return Collections.newSetFromMap(new AiPrintSupply.SectionedCellMap<>());
   }

   private static Set<Long> init(final Set<Long> first, final Set<Long> second) {
      return Collections.unmodifiableSet(new AbstractSet<Long>() {
         @Override
         public boolean contains(Object key) {
            return first.contains(key) || second.contains(key);
         }

         @Override
         public boolean isEmpty() {
            return first.isEmpty() && second.isEmpty();
         }

         @Override
         public Iterator<Long> iterator() {
            return Stream.concat(first.stream(), second.stream().filter(key -> !first.contains(key))).iterator();
         }

         @Override
         public int size() {
            int n = first.size();

            for (long key : second) {
               if (!first.contains(key)) {
                  n++;
               }
            }

            return n;
         }
      });
   }

   static void init(String[] messages, String text) {
      if (text != null) {
         String[] lines = text.split("\n", -1);

         for (int i = 0; i < messages.length; i++) {
            messages[i] = i < lines.length ? lines[i] : "";
         }
      }
   }

   static void fillSignText(AbstractSignEditScreen editor, BlockPos origin, Map<Long, String> text) throws IllegalAccessException, NoSuchFieldException {
      Class<AbstractSignEditScreen> screen = AbstractSignEditScreen.class;
      Field signField = screen.getDeclaredField("sign");
      Field slotField = screen.getDeclaredField("slot");
      Field messagesField = screen.getDeclaredField("messages");
      signField.setAccessible(true);
      slotField.setAccessible(true);
      messagesField.setAccessible(true);
      SignBlockEntity sign = (SignBlockEntity)signField.get(editor);
      if (slotField.get(editor) == SignTextSlot.FRONT) {
         init((String[])messagesField.get(editor), text.get(sign.getBlockPos().subtract(origin).asLong()));
      }
   }

   static BlockPos signPos(AbstractSignEditScreen editor) throws IllegalAccessException, NoSuchFieldException {
      Field field = AbstractSignEditScreen.class.getDeclaredField("sign");
      field.setAccessible(true);
      return ((SignBlockEntity)field.get(editor)).getBlockPos();
   }

   static final class StockSourceScan {
      final AiPrintSupply.SupplyPreparation prep;
      final Item item;
      final Set<Long> triedSources;
      final Vec3 scanOrigin;
      final AiPrintSupply.DepotScan depotScan;
      Iterator<Long> stockIterator;
      BlockPos bestSource;
      double bestDistSq = 1.0 / 0.0;
      int cellCursor;
      int consumeCount;
      int read;
      int write;
      boolean stockScanned;
      boolean finished;

      StockSourceScan(AiPrintSupply.SupplyPreparation prep, Item item, Set<Long> tried, BlockPos center, Vec3 from) {
         this.prep = prep;
         this.item = item;
         this.triedSources = tried;
         this.scanOrigin = from;
         this.depotScan = new AiPrintSupply.DepotScan(center, prep.world);
      }

      void skipDepotScan() {
         this.depotScan.load = this.depotScan.total;
         this.stockScanned = true;
      }

      void init(int budget) {
         this.write = 0;
         this.prep.pop = 0;
         long deadline = System.nanoTime() + 2000000L;

         while (
            this.write < Math.max(0, budget)
               && !this.finished
               && (this.write <= 0 || this.prep.pop < 4096 && ((this.write & 31) != 0 || System.nanoTime() < deadline))
         ) {
            this.write++;
            if (!this.depotScan.init()) {
               this.depotScan.init(this.prep);
            } else if (!this.stockScanned) {
               if (this.stockIterator == null) {
                  this.stockIterator = this.prep.stockCells.iterator();
               }

               if (this.stockIterator.hasNext()) {
                  long key = this.stockIterator.next();
                  BlockPos p = BlockPos.of(key);
                  if (!this.triedSources.contains(key)
                     && (
                        !this.prep.loaded.test(p)
                           || this.prep.readState(p).getBlock() instanceof ShulkerBoxBlock
                     )) {
                     double d = p.distToCenterSqr(this.scanOrigin);
                     if (d < this.bestDistSq || d == this.bestDistSq && (this.bestSource == null || key < this.bestSource.asLong())) {
                        this.bestSource = p;
                        this.bestDistSq = d;
                     }
                  }
               } else {
                  this.stockScanned = true;
               }
            } else if (this.cellCursor < this.prep.blueprint.cells().size() && this.consumeCount + this.read < 576) {
               AiBuilder.CellSpec cell = this.prep.blueprint.cells().get(this.cellCursor++);
               BlockPos p = this.prep.worldPos(cell);
               BlockState live = this.prep.loaded.test(p) ? this.prep.readState(p) : null;
               if (live == null || !AiBuilder.matchesSchematicCell(cell, live)) {
                  for (AiCellMaterials.MaterialRequirement requirement : AiCellMaterials.requirements(cell, live, false)) {
                     if (requirement.item() == this.item) {
                        if (requirement.use() == AiCellMaterials.MaterialUse.REUSE) {
                           this.read = Math.max(this.read, requirement.count());
                        } else {
                           this.consumeCount = Math.min(576, this.consumeCount + requirement.count());
                        }
                     }
                  }
               }
            } else {
               this.finished = true;
            }
         }
      }

      int neededCount() {
         return Math.max(1, Math.min(Math.max(1, this.consumeCount + this.read), 576));
      }

      int load() {
         int total = this.consumeCount + this.read;
         return Math.max(1, Math.min(total == 0 ? this.item.getDefaultMaxStackSize() : total, 576));
      }
   }

   static final class SupplyPreparation {
      static final int MAX_PROBE_READS = 32;
      final AiBuilder.Blueprint blueprint;
      final LevelReader world;
      final Predicate<BlockPos> loaded;
      final Map<Long, AiBuilder.CellSpec> cells = new AiPrintSupply.SectionedCellMap<>();
      final Set<Long> stockCells = AiPrintSupply.newCellSet();
      final Set<Long> protectedCells = AiPrintSupply.newCellSet();
      final Set<Long> supportCandidates = AiPrintSupply.newCellSet();
      final Set<Long> dirtyChunks = ConcurrentHashMap.newKeySet();
      final Map<Long, Set<Long>> chunksByCell = new AiPrintSupply.SectionedCellMap<>();
      final Set<Long> ownedCells = Collections.unmodifiableSet(
         new AbstractSet<Long>() {
            @Override
            public boolean isEmpty() {
               return SupplyPreparation.this.protectedCells.isEmpty() && !this.iterator().hasNext();
            }

            @Override
            public boolean contains(Object key) {
               if (SupplyPreparation.this.protectedCells.contains(key)) {
                  return true;
               }

               Set<Long> chunks = SupplyPreparation.this.chunksByCell.get(key);
               if (chunks != null) {
                  for (long chunk : chunks) {
                     if (SupplyPreparation.this.dirtyChunks.contains(chunk)) {
                        return true;
                     }
                  }
               }

               return SupplyPreparation.this.protectedCells.contains(key);
            }

            @Override
            public Iterator<Long> iterator() {
               return Stream.concat(
                     SupplyPreparation.this.protectedCells.stream(),
                     SupplyPreparation.this.supportCandidates.stream().filter(key -> !SupplyPreparation.this.protectedCells.contains(key) && this.contains(key))
                  )
                  .iterator();
            }

            @Override
            public int size() {
               if (SupplyPreparation.this.dirtyChunks.isEmpty()) {
                  return SupplyPreparation.this.protectedCells.size();
               }

               int n = SupplyPreparation.this.protectedCells.size();

               for (long key : SupplyPreparation.this.supportCandidates) {
                  if (!SupplyPreparation.this.protectedCells.contains(key) && this.contains(key)) {
                     n++;
                  }
               }

               return n;
            }
         }
      );
      final List<BlockPos> terrainCells = new AiPrintSupply.PagedPosList();
      final Map<Long, Set<Long>> readsByCell = new AiPrintSupply.SectionedCellMap<>();
      final Map<Long, Set<Long>> ownersByChunk = new AiPrintSupply.SectionedCellMap<>();
      private final LinkedHashMap<AiPrintSupply.SupplyPreparation.PendingChange, Iterator<Long>> pendingChanges = new LinkedHashMap<>();
      private Iterator<Long> fullRecheck;
      final AiPrintSupply.DepotScan depotScan;
      final boolean clearTerrain;
      int indexed;
      int supportCursor;
      int supportSide;
      int minDx;
      int sync;
      int flush;
      int close = -2147483648;
      int open = -2147483648;
      int poll = -2147483648;
      long terrainCursor;
      int push;
      int pop;
      boolean prepared;
      private BlockPos changedStock;
      private AuditBuilderHull auditHull;

      SupplyPreparation(AiBuilder.Blueprint blueprint, LevelReader world, Predicate<BlockPos> loaded, BlockPos initialCenter, boolean clearTerrain) {
         this.blueprint = blueprint;
         this.world = world;
         this.loaded = loaded;
         this.clearTerrain = clearTerrain;
         this.depotScan = new AiPrintSupply.DepotScan(initialCenter, world);
      }

      boolean settled() {
         return this.prepared && this.changedStock == null && this.pendingChanges.isEmpty() && this.fullRecheck == null;
      }

      void noteChanged(BlockPos position) {
         this.changedStock = position.immutable();
      }

      String status() {
         if (this.pendingChanges.isEmpty() && this.fullRecheck == null) {
            if (this.indexed < this.blueprint.cells().size()) {
               return "Indexing schematic stock (" + this.indexed + "/" + this.blueprint.cells().size() + ")";
            } else if (this.supportCursor < this.blueprint.cells().size()) {
               return "Checking source supports (" + this.supportCursor + "/" + this.blueprint.cells().size() + ")";
            } else if (!this.depotScan.init()) {
               return "Scanning original stock depot (" + this.depotScan.load + "/" + this.depotScan.total + ")";
            } else {
               return this.prepared ? "Schematic stock prepared" : "Checking terrain (" + this.terrainCursor + " cells)";
            }
         } else {
            return "Protecting changed source supports";
         }
      }

      void step(int budget) {
         this.push = this.pop = 0;
         long deadline = System.nanoTime() + 2000000L;

         while (
            this.push < Math.max(0, budget) && !this.settled() && (this.push <= 0 || this.pop < 4096 && ((this.push & 31) != 0 || System.nanoTime() < deadline))
         ) {
            this.push++;
            if (!this.load()) {
               if (this.indexed < this.blueprint.cells().size()) {
                  AiBuilder.CellSpec cell = this.blueprint.cells().get(this.indexed++);
                  BlockPos p = this.worldPos(cell);
                  this.cells.put(p.asLong(), cell);
                  this.protectedCells.add(p.asLong());
                  this.minDx = Math.min(this.minDx, cell.dx());
                  this.close = Math.max(this.close, cell.dx());
                  this.sync = Math.min(this.sync, cell.dy());
                  this.open = Math.max(this.open, cell.dy());
                  this.flush = Math.min(this.flush, cell.dz());
                  this.poll = Math.max(this.poll, cell.dz());
               } else if (this.changedStock != null) {
                  BlockPos stock = this.changedStock;
                  this.changedStock = null;
                  if (!this.cells.containsKey(stock.asLong())
                     && this.loaded.test(stock)
                     && this.readState(stock).getBlock() instanceof ShulkerBoxBlock) {
                     this.stockCells.add(stock.asLong());
                  }
               } else if (this.supportCursor < this.blueprint.cells().size()) {
                  BlockPos p = this.worldPos(this.blueprint.cells().get(this.supportCursor))
                     .relative(AiPrintSupply.DIRECTIONS[this.supportSide++]);
                  if (this.supportSide == 6) {
                     this.supportSide = 0;
                     this.supportCursor++;
                  }

                  if (!this.cells.containsKey(p.asLong()) && this.supportCandidates.add(p.asLong()) && this.init_Z_BlockPos(p)) {
                     this.protectedCells.add(p.asLong());
                  }
               } else if (!this.depotScan.init()) {
                  this.depotScan.init(this);
               } else if (this.clearTerrain && this.terrainCursor < this.boxVolume()) {
                  this.checkTerrainCell();
               } else {
                  this.prepared = true;
               }
            }
         }
      }

      BlockPos worldPos(AiBuilder.CellSpec cell) {
         return this.blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
      }

      BlockState readState(BlockPos p) {
         this.pop++;
         return this.world.getBlockState(p);
      }

      private long boxVolume() {
         return this.blueprint.cells().isEmpty()
            ? 0L
            : ((long)this.close - this.minDx + 1L) * ((long)this.open - this.sync + 1L) * ((long)this.poll - this.flush + 1L);
      }

      private void checkTerrainCell() {
         long width = (long)this.close - this.minDx + 1L;
         long depth = (long)this.poll - this.flush + 1L;
         long i = this.terrainCursor++;
         int y = this.open - (int)(i / (width * depth));
         long row = i % (width * depth);
         BlockPos p = this.blueprint.origin().offset(this.minDx + (int)(row / depth), y, this.flush + (int)(row % depth));
         if (!this.cells.containsKey(p.asLong())) {
            if (!this.loaded.test(p)) {
               throw new IllegalStateException("Terrain must be loaded before clearing at " + p.toShortString());
            }

            BlockState state = this.readState(p);
            if (AiSchematicActions.init(state)
               && (
                  !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                     || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER
                     || !this.cells.containsKey(p.below().asLong())
               )) {
               if (this.auditHull == null) {
                  this.auditHull = new AuditBuilderHull(this.cells.keySet());
               }

               if (this.auditHull.encloses(p.getX(), p.getY(), p.getZ())) {
                  this.terrainCells.add(p);
               }
            }
         }
      }

      void load(BlockPos pos) {
         boolean relevant = this.cells.containsKey(pos.asLong()) || this.readsByCell.containsKey(pos.asLong());

         for (Direction d : AiPrintSupply.DIRECTIONS) {
            relevant |= this.cells.containsKey(pos.relative(d).asLong());
         }

         if (relevant) {
            this.protectedCells.add(pos.asLong());

            for (Direction d : AiPrintSupply.DIRECTIONS) {
               this.protectedCells.add(pos.relative(d).asLong());
            }

            this.noteChunkChanged((long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L);
         }
      }

      void noteChunkChanged(long key) {
         Set<Long> owners = this.ownersByChunk.get(key);
         if (owners != null && !owners.isEmpty()) {
            this.dirtyChunks.add(key);
            this.init(new AiPrintSupply.SupplyPreparation.PendingChange(true, key), owners);
         }
      }

      private void init(AiPrintSupply.SupplyPreparation.PendingChange change, Set<Long> owners) {
         if (owners != null && !owners.isEmpty() && this.fullRecheck == null && !this.pendingChanges.containsKey(change)) {
            if (this.pendingChanges.size() >= 128) {
               this.pendingChanges.clear();
               this.fullRecheck = this.supportCandidates.iterator();
            } else {
               this.pendingChanges.put(change, owners.iterator());
            }
         }
      }

      private boolean load() {
         if (this.fullRecheck != null) {
            if (this.fullRecheck.hasNext()) {
               this.protectedCells.add(this.fullRecheck.next());
            } else {
               this.fullRecheck = null;
               this.dirtyChunks.clear();
            }

            return true;
         } else {
            if (this.pendingChanges.isEmpty()) {
               return false;
            }

            Entry<AiPrintSupply.SupplyPreparation.PendingChange, Iterator<Long>> entry = this.pendingChanges.entrySet().iterator().next();
            if (entry.getValue().hasNext()) {
               this.protectedCells.add(entry.getValue().next());
            } else {
               this.pendingChanges.remove(entry.getKey());
               if (entry.getKey().chunk()) {
                  this.dirtyChunks.remove(entry.getKey().key());
               }
            }

            return true;
         }
      }

      boolean init_Z_BlockPos(BlockPos candidate) {
         if (!this.loaded.test(candidate)) {
            return true;
         }

         HashSet<Long> reads = new HashSet<>();
         int[] queries = new int[]{0};
         LevelReader metered = (LevelReader)Proxy.newProxyInstance(
            LevelReader.class.getClassLoader(),
            new Class[]{LevelReader.class},
            (proxy, method, args) -> {
               String name = method.getName();
               if (args == null
                  || args.length <= 0
                  || !(args[0] instanceof BlockPos p && (name.equals("getBlockState") || name.equals("getFluidState") || name.equals("isEmptyBlock")))) {
                  if (name.equals("getMinY")) {
                     return this.world.getMinY();
                  } else if (name.equals("getHeight")) {
                     return this.world.getHeight();
                  } else if (method.isDefault()) {
                     return InvocationHandler.invokeDefault(proxy, method, args);
                  } else {
                     throw AiPrintSupply.SupplyPreparation.IncompleteRead.init;
                  }
               } else if (++queries[0] <= 32 && this.loaded.test(p)) {
                  this.pop++;
                  reads.add(p.asLong());
                  Object var10000;
                  switch (name) {
                     case "getBlockState":
                        var10000 = this.world.getBlockState(p);
                        break;
                     case "getFluidState":
                        var10000 = this.world.getFluidState(p);
                        break;
                     default:
                        var10000 = this.world.getBlockState(p).isAir();
                  }

                  return var10000;
               } else {
                  throw AiPrintSupply.SupplyPreparation.IncompleteRead.init;
               }
            }
         );

         try {
            return AiSchematicActions.init(candidate, this.cells, metered);
         } catch (AiPrintSupply.SupplyPreparation.IncompleteRead incomplete) {
            return true;
         } finally {
            Iterator var15 = reads.iterator();

            while (true) {
               if (!var15.hasNext()) {
                  break;
               } else {
                  long key = (Long)var15.next();
                  this.readsByCell.computeIfAbsent(key, ignored -> AiPrintSupply.newCellSet()).add(candidate.asLong());
                  long chunk = (long)(BlockPos.getX(key) >> 4) << 32 | BlockPos.getZ(key) >> 4 & 4294967295L;
                  Set<Long> owners = this.ownersByChunk.computeIfAbsent(chunk, ignored -> AiPrintSupply.newCellSet());
                  boolean added = owners.add(candidate.asLong());
                  this.chunksByCell.computeIfAbsent(candidate.asLong(), ignored -> ConcurrentHashMap.newKeySet()).add(chunk);
                  if (added && this.dirtyChunks.contains(chunk) && this.fullRecheck == null) {
                     this.pendingChanges.put(new AiPrintSupply.SupplyPreparation.PendingChange(true, chunk), owners.iterator());
                  }
               }
            }
         }
      }

      private record PendingChange(boolean chunk, long key) {
      }

      private static final class IncompleteRead extends RuntimeException {
         static final AiPrintSupply.SupplyPreparation.IncompleteRead init = new AiPrintSupply.SupplyPreparation.IncompleteRead();

         private IncompleteRead() {
            super(null, null, false, false);
         }
      }
   }

   static final class SectionedCellMap<V> extends AbstractMap<Long, V> {
      private final ConcurrentSkipListMap<Long, Map<Long, V>> bySection = new ConcurrentSkipListMap<>();
      private final AtomicInteger size = new AtomicInteger();

      private static long init(long key) {
         return BlockPos.asLong(BlockPos.getX(key) >> 4, BlockPos.getY(key) >> 4, BlockPos.getZ(key) >> 4);
      }

      public V init(Long key, V value) {
         Map<Long, V> map = this.bySection.computeIfAbsent(init(key), ignored -> new ConcurrentHashMap<>());
         V before = map.put(key, value);
         if (before == null) {
            this.size.incrementAndGet();
         }

         return before;
      }

      @Override
      public V put(Long key, V value) {
         return this.init(key, value);
      }

      @Override
      public V get(Object key) {
         if (key instanceof Long value) {
            Map<Long, V> map = this.bySection.get(init(value));
            return map == null ? null : map.get(value);
         } else {
            return null;
         }
      }

      @Override
      public boolean containsKey(Object key) {
         return this.get(key) != null;
      }

      @Override
      public int size() {
         return this.size.get();
      }

      @Override
      public Set<Entry<Long, V>> entrySet() {
         return new AbstractSet<Entry<Long, V>>() {
            @Override
            public int size() {
               return SectionedCellMap.this.size.get();
            }

            @Override
            public Iterator<Entry<Long, V>> iterator() {
               final Iterator<Map<Long, V>> outer = SectionedCellMap.this.bySection.values().iterator();
               return new Iterator<Entry<Long, V>>() {
                  Iterator<Entry<Long, V>> init = Collections.emptyIterator();

                  @Override
                  public boolean hasNext() {
                     while (!this.init.hasNext() && outer.hasNext()) {
                        this.init = outer.next().entrySet().iterator();
                     }

                     return this.init.hasNext();
                  }

                  public Entry<Long, V> next() {
                     return this.init();
                  }

                  public Entry<Long, V> init() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        return this.init.next();
                     }
                  }
               };
            }
         };
      }
   }

   static final class DepotScan {
      static final int CUBE_CELLS = 40817;
      private static final int RADIUS = 24;
      final BlockPos center;
      final ClientLevel level;
      final int minChunkX;
      final int minChunkZ;
      final int chunkWidth;
      final int total;
      Iterator<BlockPos> pending = Collections.emptyIterator();
      int load;

      DepotScan(BlockPos center, LevelReader world) {
         this.center = center.immutable();
         this.level = world instanceof ClientLevel clientLevel ? clientLevel : null;
         this.minChunkX = center.getX() - RADIUS >> 4;
         this.minChunkZ = center.getZ() - RADIUS >> 4;
         this.chunkWidth = (center.getX() + RADIUS >> 4) - this.minChunkX + 1;
         int chunkDepth = (center.getZ() + RADIUS >> 4) - this.minChunkZ + 1;
         this.total = this.level == null ? CUBE_CELLS : this.chunkWidth * chunkDepth;
      }

      boolean init() {
         return this.load >= this.total && !this.pending.hasNext();
      }

      void init(AiPrintSupply.SupplyPreparation prep) {
         BlockPos p;
         if (this.level != null) {
            if (!this.pending.hasNext()) {
               int idx = this.load++;
               LevelChunk chunk = this.level
                  .getChunkSource()
                  .getChunk(this.minChunkX + idx % this.chunkWidth, this.minChunkZ + idx / this.chunkWidth, ChunkStatus.FULL, false);
               if (chunk != null) {
                  this.pending = chunk.getBlockEntities()
                     .values()
                     .stream()
                     .filter(ShulkerBoxBlockEntity.class::isInstance)
                     .map(BlockEntity::getBlockPos)
                     .toList()
                     .iterator();
               }

               return;
            }

            p = this.pending.next();
            if (Math.abs(p.getX() - this.center.getX()) > RADIUS || Math.abs(p.getZ() - this.center.getZ()) > RADIUS) {
               return;
            }
         } else {
            int i = this.load++;
            p = this.center.offset(i % 49 - 24, i / 49 % 17 - 8, i / 833 - 24);
         }

         if (!prep.cells.containsKey(p.asLong()) && prep.loaded.test(p) && prep.readState(p).getBlock() instanceof ShulkerBoxBlock) {
            prep.stockCells.add(p.asLong());
         }
      }
   }

   static final class PagedPosList extends AbstractList<BlockPos> {
      private final TreeMap<Integer, BlockPos[]> pages = new TreeMap<>();
      private int size;

      @Override
      public int size() {
         return this.size;
      }

      public BlockPos get(int index) {
         return this.init(index);
      }

      public boolean add(BlockPos pos) {
         return this.init(pos);
      }

      public BlockPos init(int index) {
         Objects.checkIndex(index, this.size);
         return this.pages.get(index >>> 10)[index & 1023];
      }

      public boolean init(BlockPos pos) {
         this.pages.computeIfAbsent(this.size >>> 10, ignored -> new BlockPos[1024])[this.size & 1023] = pos;
         this.size++;
         return true;
      }
   }

   static final class StockWithdrawOperation implements AiOperation {
      private AiOperation navigation;
      private AiOperation opened;
      private final Supplier<AiPrintSupply.StockState> stateSupplier;
      private final Supplier<AiOperation> opener;
      private final long deadline;
      private final AiPreparationReceipt receipt = new AiPreparationReceipt();
      private AiOperation.Result finalResult;

      StockWithdrawOperation(AiOperation navigation, Supplier<AiPrintSupply.StockState> state, Supplier<AiOperation> open, long deadline) {
         this.navigation = navigation;
         this.stateSupplier = state;
         this.opener = open;
         this.deadline = deadline;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.finalResult != null) {
            return this.finalResult;
         }

         if (System.nanoTime() >= this.deadline) {
            return this.init(new AiOperation.Result(AiOperation.Status.TIMED_OUT, "Stock navigation/withdrawal deadline reached", "{}", List.of(), AiFailure.TIMEOUT));
         }

         if (this.stateSupplier.get() == AiPrintSupply.StockState.REPLACED) {
            return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "The remembered stock is no longer a shulker box", "{}", List.of(), AiFailure.MISSING_MATERIALS));
         }

         if (this.navigation != null) {
            AiOperation.Result result = this.receipt.tickChild(this.navigation);
            if (!result.terminal()) {
               return result;
            }

            if (result.status() != AiOperation.Status.SUCCEEDED) {
               return this.init(AiContainer.init(result, AiJson.objectOf("action", "withdraw")));
            }

            if (this.navigation.requiresReconciliationAfterEmergency() || !this.navigation.canInterruptNow()) {
               return this.init(
                  new AiOperation.Result(AiOperation.Status.UNCERTAIN, "Stock approach needs reconciliation", result.dataJson(), result.evidence())
               );
            }

            this.navigation.cancel();
            this.navigation = null;
         }

         AiPrintSupply.StockState observed = this.stateSupplier.get();
         if (observed == AiPrintSupply.StockState.UNKNOWN) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for the remembered stock chunk", "{}", List.of());
         }

         if (observed == AiPrintSupply.StockState.REPLACED) {
            return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "The remembered stock is no longer a shulker box", "{}", List.of(), AiFailure.MISSING_MATERIALS));
         }

         if (this.opened == null) {
            this.opened = this.opener.get();
         }

         AiOperation.Result result = this.receipt.tickChild(this.opened);
         return result.terminal() ? this.init(result) : result;
      }

      private AiOperation.Result init(AiOperation.Result result) {
         boolean pending = this.requiresReconciliationAfterEmergency();
         this.init();
         this.finalResult = this.receipt.finalizeResult(result, pending);
         return this.finalResult;
      }

      private void init() {
         if (this.navigation != null) {
            this.receipt.cancelChild(this.navigation);
            this.navigation = null;
         }

         if (this.opened != null) {
            this.receipt.cancelChild(this.opened);
            this.opened = null;
         }
      }

      @Override
      public void cancel() {
         if (this.finalResult == null) {
            this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Stock operation cancelled", "{}", List.of()));
         } else {
            this.init();
         }
      }

      @Override
      public boolean canInterruptNow() {
         return !this.receipt.requiresReconciliation(null)
            && (this.navigation == null || this.navigation.canInterruptNow())
            && (this.opened == null || this.opened.canInterruptNow());
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.receipt.requiresReconciliation(this.navigation) || this.receipt.requiresReconciliation(this.opened);
      }
   }

   enum StockState {
      UNKNOWN,
      SHULKER,
      REPLACED;
   }

   static final class ShortageTracker {
      Item item;
      int misses;
      final Set<Long> triedSources = new HashSet<>();
      final List<ItemStack> triedBoxes = new ArrayList<>();

      boolean init(Item wanted, int available) {
         if (wanted != null && wanted != Items.AIR && available <= 0) {
            if (this.item != wanted) {
               this.init();
               this.item = wanted;
            }

            return ++this.misses >= 10;
         } else {
            this.init();
            return false;
         }
      }

      boolean init(long key) {
         return this.triedSources.size() < 3 && this.triedSources.add(key);
      }

      void init() {
         this.item = null;
         this.misses = 0;
         this.triedSources.clear();
         this.triedBoxes.clear();
      }
   }
}
