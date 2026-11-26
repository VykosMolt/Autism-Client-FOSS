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
   private static final Direction[] init_DirectionArr = Direction.values();
   private final Minecraft init_Minecraft = Minecraft.getInstance();
   private final AiPrintSupply.Inner_CachePool init_bj_CachePool;
   private final Map<Long, AiBuilder.Inner_StreamUtil> init_Map;
   private final AiPrintSupply.Inner_TaskQueue init_bj_TaskQueue = new AiPrintSupply.Inner_TaskQueue();
   private List<BlockPos> init_List;
   private int read;
   private AiPrintSupply.Inner_BufferHelper init_bj_BufferHelper;
   private ItemStack init_ItemStack = ItemStack.EMPTY;
   private ItemStack load_ItemStack = ItemStack.EMPTY;
   private final List<ItemStack> load_List = new ArrayList<>();
   int init_I;
   int load_I;
   private int write = 1;

   int init_I() {
      return this.write;
   }

   static void init_V_TableIndex_ConfigS(AiBuilder.Inner_ConfigStore power) {
      if (!power.substitutes().isEmpty()) {
         throw new IllegalArgumentException("The schematic printer requires exact materials; remove substitutes or use the legacy builder");
      }
   }

   AiPrintSupply(AiBuilder.Inner_DataUtil blueprint) {
      this(blueprint, false);
   }

   AiPrintSupply(AiBuilder.Inner_DataUtil blueprint, boolean clearTerrain) {
      ClientLevel world = this.init_Minecraft.level;
      this.init_bj_CachePool = new AiPrintSupply.Inner_CachePool(
         blueprint,
         world,
         p -> AiTerrain.init(world, p),
         this.init_Minecraft.player == null ? BlockPos.ZERO : this.init_Minecraft.player.blockPosition(),
         clearTerrain
      );
      this.init_Map = this.init_bj_CachePool.init_Map;
      this.init_List = this.init_bj_CachePool.init_List;
   }

   boolean init_Z_I(int workBudget) {
      this.init_bj_CachePool.init_V_I(workBudget);
      return this.init_Z();
   }

   void init_V_BlockPos(BlockPos position) {
      this.init_bj_CachePool.init_V_BlockPos(position);
   }

   boolean init_Z() {
      return this.init_bj_CachePool.init_Z();
   }

   boolean load_Z() {
      return this.init_bj_CachePool.load_Z;
   }

   long init_J() {
      return this.init_bj_CachePool.load_I
         + 6L * this.init_bj_CachePool.read_I
         + this.init_bj_CachePool.write_I
         + this.init_bj_CachePool.init_bj_DataUtil.load
         + this.init_bj_CachePool.init_J;
   }

   long load_J() {
      return 7L * this.init_bj_CachePool.init_TableIndex_DataUti.cells().size()
         + this.init_bj_CachePool.init_bj_DataUtil.total
         + (
            this.init_bj_CachePool.init_Z && this.init_bj_CachePool.load_I == this.init_bj_CachePool.init_TableIndex_DataUti.cells().size()
               ? this.init_bj_CachePool.init_J()
               : 0L
         );
   }

   String init_Str() {
      return this.init_bj_CachePool.init_Str();
   }

   void load(BlockPos pos) {
      this.init_bj_CachePool.load(pos);
   }

   void init_V_J(long key) {
      this.init_bj_CachePool.init_V_J(key);
   }

   static int init(Inventory inventory, Item item) {
      return load(inventory, item);
   }

   static int load(Inventory inventory, Item item) {
      int count = 0;

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = inventory.getItem(slot);
         if (autismclient.ai.print.PrintCore.init_Z_ItemStack_Item(stack, item)) {
            count += stack.getCount();
         }
      }

      ItemStack offhand = inventory.getItem(40);
      return autismclient.ai.print.PrintCore.init_Z_ItemStack_Item(offhand, item) ? count + offhand.getCount() : count;
   }

   boolean init_Z_Item(Item item) {
      return this.init_bj_TaskQueue.init(item, item == null ? 0 : load(this.init_Minecraft.player.getInventory(), item));
   }

   Item init_Item() {
      return this.init_bj_TaskQueue.init_Item;
   }

   boolean read_Z() {
      return this.init_bj_TaskQueue.init_Item != null
         && !AiCarriedShulker.init(this.init_Minecraft.player.getInventory(), this.init_bj_TaskQueue.init_Item, this.init_bj_TaskQueue.init_List, true).isEmpty();
   }

   void init_V() {
      this.init_bj_TaskQueue.init();
      this.init_bj_BufferHelper = null;
      this.init_ItemStack = ItemStack.EMPTY;
      this.init_I++;
   }

   void load_V() {
      this.init_bj_TaskQueue.init();
      this.init_bj_BufferHelper = null;
      this.init_ItemStack = ItemStack.EMPTY;
   }

   AiOperation init_AiOperation_ItemStack(ItemStack wanted, int additional, Set<Long> owned) {
      if (!wanted.isEmpty() && additional > 0 && this.init_Minecraft.player != null && !this.init_Minecraft.player.hasInfiniteMaterials()) {
         if (!ItemStack.isSameItemSameComponents(this.load_ItemStack, wanted)) {
            this.write_V();
            this.load_ItemStack = wanted.copyWithCount(1);
         }

         if (this.load_List.size() >= 37) {
            return null;
         }

         ItemStack box = AiCarriedShulker.init(this.init_Minecraft.player.getInventory(), wanted, this.load_List);
         if (box.isEmpty()) {
            return null;
         }

         this.load_List.add(box.copy());
         return new AiCarriedShulker(box, wanted, additional, init(this.init_Set(), owned));
      } else {
         return null;
      }
   }

   void read_V() {
      this.init_I++;
   }

   void write_V() {
      this.load_ItemStack = ItemStack.EMPTY;
      this.load_List.clear();
   }

   Set<Long> init_Set() {
      if (!this.load_Z()) {
         throw new IllegalStateException("Schematic supply preparation is still running");
      } else {
         return this.init_bj_CachePool.update_Set;
      }
   }

   boolean load(int workBudget) {
      if (!this.load_Z()) {
         return false;
      }

      if (this.init_bj_TaskQueue.init_Item == null) {
         this.init_bj_BufferHelper = null;
         this.init_ItemStack = ItemStack.EMPTY;
         return true;
      }

      if (this.init_bj_BufferHelper == null || this.init_bj_BufferHelper.init_Item != this.init_bj_TaskQueue.init_Item) {
         this.init_ItemStack = AiCarriedShulker.init(
            this.init_Minecraft.player.getInventory(), this.init_bj_TaskQueue.init_Item, this.init_bj_TaskQueue.init_List, true
         );
         if (this.init_ItemStack.isEmpty() && this.init_bj_TaskQueue.init_Set.size() >= 3) {
            return true;
         }

         this.init_bj_BufferHelper = new AiPrintSupply.Inner_BufferHelper(
            this.init_bj_CachePool,
            this.init_bj_TaskQueue.init_Item,
            this.init_bj_TaskQueue.init_Set,
            this.init_Minecraft.player.blockPosition(),
            this.init_Minecraft.player.position()
         );
         if (!this.init_ItemStack.isEmpty()) {
            this.init_bj_BufferHelper.init_V();
         }
      }

      this.init_bj_BufferHelper.init(workBudget);
      if (this.init_bj_BufferHelper.load_Z
         && !this.init_ItemStack.isEmpty()
         && AiCarriedShulker.init(this.init_Minecraft.player.getInventory(), this.init_ItemStack, this.init_bj_TaskQueue.init_Item) == null) {
         this.init_bj_BufferHelper = null;
         this.init_ItemStack = ItemStack.EMPTY;
         return false;
      } else if (this.init_bj_BufferHelper.load_Z
         && this.init_bj_BufferHelper.init_BlockPos != null
         && AiTerrain.init(this.init_Minecraft.level, this.init_bj_BufferHelper.init_BlockPos)
         && !(this.init_Minecraft.level.getBlockState(this.init_bj_BufferHelper.init_BlockPos).getBlock() instanceof ShulkerBoxBlock)) {
         this.init_bj_TaskQueue.init(this.init_bj_BufferHelper.init_BlockPos.asLong());
         this.init_bj_BufferHelper = null;
         return false;
      } else {
         return this.init_bj_BufferHelper.load_Z;
      }
   }

   AiOperation init_AiOperation_Set(Set<Long> owned) {
      if (this.init_bj_TaskQueue.init_Item != null && (!this.init_ItemStack.isEmpty() || this.init_bj_TaskQueue.init_Set.size() < 3)) {
         if (this.init_bj_BufferHelper != null && this.init_bj_BufferHelper.load_Z && this.init_bj_BufferHelper.init_Item == this.init_bj_TaskQueue.init_Item) {
            BlockPos source = this.init_bj_BufferHelper.init_BlockPos;
            int need = this.init_bj_BufferHelper.load();
            this.write = this.init_bj_BufferHelper.init_I();
            this.init_bj_BufferHelper = null;
            Set<Long> keep = init(this.init_Set(), owned);
            if (!this.init_ItemStack.isEmpty()) {
               ItemStack selected = this.init_ItemStack;
               this.init_ItemStack = ItemStack.EMPTY;
               Item supplied = AiCarriedShulker.init(this.init_Minecraft.player.getInventory(), selected, this.init_bj_TaskQueue.init_Item);
               if (supplied == null) {
                  return null;
               }

               this.init_bj_TaskQueue.init_List.add(selected.copy());
               return new AiCarriedShulker(selected, supplied, need, keep);
            } else {
               if (source == null) {
                  return null;
               }

               if (!this.init_bj_TaskQueue.init(source.asLong())) {
                  return null;
               }

               JsonArray items = new JsonArray();
               items.add(AiJson.init_JsonObject_ObjArr("item", BuiltInRegistries.ITEM.getKey(this.init_bj_TaskQueue.init_Item).toString(), "count", need));
               AiOperation approach = AiTerrain.init(this.init_Minecraft.level, source) && autismclient.ai.engine.AiBlockInteraction.init(this.init_Minecraft, source) != null
                  ? null
                  : AiContainer.init(AiGoto.load(source, 60, keep, false));
               return new AiPrintSupply.Inner_NetHelper(
                  approach,
                  () -> !AiTerrain.init(this.init_Minecraft.level, source)
                     ? AiPrintSupply.Inner_StreamUtil.UNKNOWN
                     : (
                        this.init_Minecraft.level.getBlockState(source).getBlock() instanceof ShulkerBoxBlock
                           ? AiPrintSupply.Inner_StreamUtil.SHULKER
                           : AiPrintSupply.Inner_StreamUtil.REPLACED
                     ),
                  () -> AiContainer.init(
                     AiJson.init_JsonObject_ObjArr(
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

   BlockPos init_BlockPos() {
      if (!this.load_Z()) {
         throw new IllegalStateException("Schematic terrain preparation is still running");
      }

      for (int checked = 0; this.read < this.init_List.size() && checked < 64; checked++) {
         BlockPos p = this.init_List.get(this.read++);
         if (!AiTerrain.init(this.init_Minecraft.level, p)) {
            throw new IllegalStateException("Terrain is not loaded at " + p.toShortString());
         }

         if (AiSchematicActions.init(this.init_Minecraft.level.getBlockState(p))
            && !this.init_bj_CachePool.update_Set.contains(p.asLong())
            && !this.init_bj_CachePool.init_Z_BlockPos(p)) {
            if (!AiSchematicActions.init(this.init_Minecraft.level, p)) {
               throw new IllegalStateException("Clearing terrain would release fluid at " + p.toShortString());
            }

            return p;
         }
      }

      return null;
   }

   boolean write_Z() {
      return this.load_Z() && this.read >= this.init_List.size();
   }

   private static Set<Long> load_Set() {
      return Collections.newSetFromMap(new AiPrintSupply.Inner_ConfigStore<>());
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

   static void init_V_AbstractSignEditSc(AbstractSignEditScreen editor, BlockPos origin, Map<Long, String> text) throws IllegalAccessException, NoSuchFieldException {
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

   static BlockPos init_BlockPos_AbstractSignEditSc(AbstractSignEditScreen editor) throws IllegalAccessException, NoSuchFieldException {
      Field field = AbstractSignEditScreen.class.getDeclaredField("sign");
      field.setAccessible(true);
      return ((SignBlockEntity)field.get(editor)).getBlockPos();
   }

   static final class Inner_BufferHelper {
      final AiPrintSupply.Inner_CachePool init_bj_CachePool;
      final Item init_Item;
      final Set<Long> init_Set;
      final Vec3 init_Vec3;
      final AiPrintSupply.Inner_DataUtil init_bj_DataUtil;
      Iterator<Long> init_Iterator;
      BlockPos init_BlockPos;
      double init_D = 1.0 / 0.0;
      int init_I;
      int load_I;
      int read;
      int write;
      boolean init_Z;
      boolean load_Z;

      Inner_BufferHelper(AiPrintSupply.Inner_CachePool prep, Item item, Set<Long> tried, BlockPos center, Vec3 from) {
         this.init_bj_CachePool = prep;
         this.init_Item = item;
         this.init_Set = tried;
         this.init_Vec3 = from;
         this.init_bj_DataUtil = new AiPrintSupply.Inner_DataUtil(center, prep.init_LevelReader);
      }

      void init_V() {
         this.init_bj_DataUtil.load = this.init_bj_DataUtil.total;
         this.init_Z = true;
      }

      void init(int budget) {
         this.write = 0;
         this.init_bj_CachePool.pop = 0;
         long deadline = System.nanoTime() + 2000000L;

         while (
            this.write < Math.max(0, budget)
               && !this.load_Z
               && (this.write <= 0 || this.init_bj_CachePool.pop < 4096 && ((this.write & 31) != 0 || System.nanoTime() < deadline))
         ) {
            this.write++;
            if (!this.init_bj_DataUtil.init()) {
               this.init_bj_DataUtil.init(this.init_bj_CachePool);
            } else if (!this.init_Z) {
               if (this.init_Iterator == null) {
                  this.init_Iterator = this.init_bj_CachePool.init_Set.iterator();
               }

               if (this.init_Iterator.hasNext()) {
                  long key = this.init_Iterator.next();
                  BlockPos p = BlockPos.of(key);
                  if (!this.init_Set.contains(key)
                     && (
                        !this.init_bj_CachePool.init_Predicate.test(p)
                           || this.init_bj_CachePool.init_BlockState_BlockPos(p).getBlock() instanceof ShulkerBoxBlock
                     )) {
                     double d = p.distToCenterSqr(this.init_Vec3);
                     if (d < this.init_D || d == this.init_D && (this.init_BlockPos == null || key < this.init_BlockPos.asLong())) {
                        this.init_BlockPos = p;
                        this.init_D = d;
                     }
                  }
               } else {
                  this.init_Z = true;
               }
            } else if (this.init_I < this.init_bj_CachePool.init_TableIndex_DataUti.cells().size() && this.load_I + this.read < 576) {
               AiBuilder.Inner_StreamUtil cell = this.init_bj_CachePool.init_TableIndex_DataUti.cells().get(this.init_I++);
               BlockPos p = this.init_bj_CachePool.init_BlockPos_TableIndex_StreamU(cell);
               BlockState live = this.init_bj_CachePool.init_Predicate.test(p) ? this.init_bj_CachePool.init_BlockState_BlockPos(p) : null;
               if (live == null || !AiBuilder.update(cell, live)) {
                  for (AiCellMaterials.Inner_CachePool requirement : AiCellMaterials.init(cell, live, false)) {
                     if (requirement.item() == this.init_Item) {
                        if (requirement.use() == AiCellMaterials.Inner_BufferHelper.REUSE) {
                           this.read = Math.max(this.read, requirement.count());
                        } else {
                           this.load_I = Math.min(576, this.load_I + requirement.count());
                        }
                     }
                  }
               }
            } else {
               this.load_Z = true;
            }
         }
      }

      int init_I() {
         return Math.max(1, Math.min(Math.max(1, this.load_I + this.read), 576));
      }

      int load() {
         int total = this.load_I + this.read;
         return Math.max(1, Math.min(total == 0 ? this.init_Item.getDefaultMaxStackSize() : total, 576));
      }
   }

   static final class Inner_CachePool {
      static final int init_I = 32;
      final AiBuilder.Inner_DataUtil init_TableIndex_DataUti;
      final LevelReader init_LevelReader;
      final Predicate<BlockPos> init_Predicate;
      final Map<Long, AiBuilder.Inner_StreamUtil> init_Map = new AiPrintSupply.Inner_ConfigStore<>();
      final Set<Long> init_Set = AiPrintSupply.load_Set();
      final Set<Long> load_Set = AiPrintSupply.load_Set();
      final Set<Long> read_Set = AiPrintSupply.load_Set();
      final Set<Long> write_Set = ConcurrentHashMap.newKeySet();
      final Map<Long, Set<Long>> load_Map = new AiPrintSupply.Inner_ConfigStore<>();
      final Set<Long> update_Set = Collections.unmodifiableSet(
         new AbstractSet<Long>() {
            @Override
            public boolean isEmpty() {
               return Inner_CachePool.this.load_Set.isEmpty() && !this.iterator().hasNext();
            }

            @Override
            public boolean contains(Object key) {
               if (Inner_CachePool.this.load_Set.contains(key)) {
                  return true;
               }

               Set<Long> chunks = Inner_CachePool.this.load_Map.get(key);
               if (chunks != null) {
                  for (long chunk : chunks) {
                     if (Inner_CachePool.this.write_Set.contains(chunk)) {
                        return true;
                     }
                  }
               }

               return Inner_CachePool.this.load_Set.contains(key);
            }

            @Override
            public Iterator<Long> iterator() {
               return Stream.concat(
                     Inner_CachePool.this.load_Set.stream(),
                     Inner_CachePool.this.read_Set.stream().filter(key -> !Inner_CachePool.this.load_Set.contains(key) && this.contains(key))
                  )
                  .iterator();
            }

            @Override
            public int size() {
               if (Inner_CachePool.this.write_Set.isEmpty()) {
                  return Inner_CachePool.this.load_Set.size();
               }

               int n = Inner_CachePool.this.load_Set.size();

               for (long key : Inner_CachePool.this.read_Set) {
                  if (!Inner_CachePool.this.load_Set.contains(key) && this.contains(key)) {
                     n++;
                  }
               }

               return n;
            }
         }
      );
      final List<BlockPos> init_List = new AiPrintSupply.Inner_JsonUtil();
      final Map<Long, Set<Long>> read_Map = new AiPrintSupply.Inner_ConfigStore<>();
      final Map<Long, Set<Long>> write_Map = new AiPrintSupply.Inner_ConfigStore<>();
      private final LinkedHashMap<AiPrintSupply.Inner_CachePool.Inner_DataUtil, Iterator<Long>> init_LinkedHashMap = new LinkedHashMap<>();
      private Iterator<Long> init_Iterator;
      final AiPrintSupply.Inner_DataUtil init_bj_DataUtil;
      final boolean init_Z;
      int load_I;
      int read_I;
      int write_I;
      int update_I;
      int sync;
      int flush;
      int close = -2147483648;
      int open = -2147483648;
      int poll = -2147483648;
      long init_J;
      int push;
      int pop;
      boolean load_Z;
      private BlockPos init_BlockPos;
      private AuditBuilderHull auditHull;

      Inner_CachePool(AiBuilder.Inner_DataUtil blueprint, LevelReader world, Predicate<BlockPos> loaded, BlockPos initialCenter, boolean clearTerrain) {
         this.init_TableIndex_DataUti = blueprint;
         this.init_LevelReader = world;
         this.init_Predicate = loaded;
         this.init_Z = clearTerrain;
         this.init_bj_DataUtil = new AiPrintSupply.Inner_DataUtil(initialCenter, world);
      }

      boolean init_Z() {
         return this.load_Z && this.init_BlockPos == null && this.init_LinkedHashMap.isEmpty() && this.init_Iterator == null;
      }

      void init_V_BlockPos(BlockPos position) {
         this.init_BlockPos = position.immutable();
      }

      String init_Str() {
         if (this.init_LinkedHashMap.isEmpty() && this.init_Iterator == null) {
            if (this.load_I < this.init_TableIndex_DataUti.cells().size()) {
               return "Indexing schematic stock (" + this.load_I + "/" + this.init_TableIndex_DataUti.cells().size() + ")";
            } else if (this.read_I < this.init_TableIndex_DataUti.cells().size()) {
               return "Checking source supports (" + this.read_I + "/" + this.init_TableIndex_DataUti.cells().size() + ")";
            } else if (!this.init_bj_DataUtil.init()) {
               return "Scanning original stock depot (" + this.init_bj_DataUtil.load + "/" + this.init_bj_DataUtil.total + ")";
            } else {
               return this.load_Z ? "Schematic stock prepared" : "Checking terrain (" + this.init_J + " cells)";
            }
         } else {
            return "Protecting changed source supports";
         }
      }

      void init_V_I(int budget) {
         this.push = this.pop = 0;
         long deadline = System.nanoTime() + 2000000L;

         while (
            this.push < Math.max(0, budget) && !this.init_Z() && (this.push <= 0 || this.pop < 4096 && ((this.push & 31) != 0 || System.nanoTime() < deadline))
         ) {
            this.push++;
            if (!this.load()) {
               if (this.load_I < this.init_TableIndex_DataUti.cells().size()) {
                  AiBuilder.Inner_StreamUtil cell = this.init_TableIndex_DataUti.cells().get(this.load_I++);
                  BlockPos p = this.init_BlockPos_TableIndex_StreamU(cell);
                  this.init_Map.put(p.asLong(), cell);
                  this.load_Set.add(p.asLong());
                  this.update_I = Math.min(this.update_I, cell.dx());
                  this.close = Math.max(this.close, cell.dx());
                  this.sync = Math.min(this.sync, cell.dy());
                  this.open = Math.max(this.open, cell.dy());
                  this.flush = Math.min(this.flush, cell.dz());
                  this.poll = Math.max(this.poll, cell.dz());
               } else if (this.init_BlockPos != null) {
                  BlockPos stock = this.init_BlockPos;
                  this.init_BlockPos = null;
                  if (!this.init_Map.containsKey(stock.asLong())
                     && this.init_Predicate.test(stock)
                     && this.init_BlockState_BlockPos(stock).getBlock() instanceof ShulkerBoxBlock) {
                     this.init_Set.add(stock.asLong());
                  }
               } else if (this.read_I < this.init_TableIndex_DataUti.cells().size()) {
                  BlockPos p = this.init_BlockPos_TableIndex_StreamU(this.init_TableIndex_DataUti.cells().get(this.read_I))
                     .relative(AiPrintSupply.init_DirectionArr[this.write_I++]);
                  if (this.write_I == 6) {
                     this.write_I = 0;
                     this.read_I++;
                  }

                  if (!this.init_Map.containsKey(p.asLong()) && this.read_Set.add(p.asLong()) && this.init_Z_BlockPos(p)) {
                     this.load_Set.add(p.asLong());
                  }
               } else if (!this.init_bj_DataUtil.init()) {
                  this.init_bj_DataUtil.init(this);
               } else if (this.init_Z && this.init_J < this.init_J()) {
                  this.init_V();
               } else {
                  this.load_Z = true;
               }
            }
         }
      }

      BlockPos init_BlockPos_TableIndex_StreamU(AiBuilder.Inner_StreamUtil cell) {
         return this.init_TableIndex_DataUti.origin().offset(cell.dx(), cell.dy(), cell.dz());
      }

      BlockState init_BlockState_BlockPos(BlockPos p) {
         this.pop++;
         return this.init_LevelReader.getBlockState(p);
      }

      private long init_J() {
         return this.init_TableIndex_DataUti.cells().isEmpty()
            ? 0L
            : ((long)this.close - this.update_I + 1L) * ((long)this.open - this.sync + 1L) * ((long)this.poll - this.flush + 1L);
      }

      private void init_V() {
         long width = (long)this.close - this.update_I + 1L;
         long depth = (long)this.poll - this.flush + 1L;
         long i = this.init_J++;
         int y = this.open - (int)(i / (width * depth));
         long row = i % (width * depth);
         BlockPos p = this.init_TableIndex_DataUti.origin().offset(this.update_I + (int)(row / depth), y, this.flush + (int)(row % depth));
         if (!this.init_Map.containsKey(p.asLong())) {
            if (!this.init_Predicate.test(p)) {
               throw new IllegalStateException("Terrain must be loaded before clearing at " + p.toShortString());
            }

            BlockState state = this.init_BlockState_BlockPos(p);
            if (AiSchematicActions.init(state)
               && (
                  !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                     || state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) != DoubleBlockHalf.UPPER
                     || !this.init_Map.containsKey(p.below().asLong())
               )) {
               if (this.auditHull == null) {
                  this.auditHull = new AuditBuilderHull(this.init_Map.keySet());
               }

               if (this.auditHull.encloses(p.getX(), p.getY(), p.getZ())) {
                  this.init_List.add(p);
               }
            }
         }
      }

      void load(BlockPos pos) {
         boolean relevant = this.init_Map.containsKey(pos.asLong()) || this.read_Map.containsKey(pos.asLong());

         for (Direction d : AiPrintSupply.init_DirectionArr) {
            relevant |= this.init_Map.containsKey(pos.relative(d).asLong());
         }

         if (relevant) {
            this.load_Set.add(pos.asLong());

            for (Direction d : AiPrintSupply.init_DirectionArr) {
               this.load_Set.add(pos.relative(d).asLong());
            }

            this.init_V_J((long)(pos.getX() >> 4) << 32 | pos.getZ() >> 4 & 4294967295L);
         }
      }

      void init_V_J(long key) {
         Set<Long> owners = this.write_Map.get(key);
         if (owners != null && !owners.isEmpty()) {
            this.write_Set.add(key);
            this.init(new AiPrintSupply.Inner_CachePool.Inner_DataUtil(true, key), owners);
         }
      }

      private void init(AiPrintSupply.Inner_CachePool.Inner_DataUtil change, Set<Long> owners) {
         if (owners != null && !owners.isEmpty() && this.init_Iterator == null && !this.init_LinkedHashMap.containsKey(change)) {
            if (this.init_LinkedHashMap.size() >= 128) {
               this.init_LinkedHashMap.clear();
               this.init_Iterator = this.read_Set.iterator();
            } else {
               this.init_LinkedHashMap.put(change, owners.iterator());
            }
         }
      }

      private boolean load() {
         if (this.init_Iterator != null) {
            if (this.init_Iterator.hasNext()) {
               this.load_Set.add(this.init_Iterator.next());
            } else {
               this.init_Iterator = null;
               this.write_Set.clear();
            }

            return true;
         } else {
            if (this.init_LinkedHashMap.isEmpty()) {
               return false;
            }

            Entry<AiPrintSupply.Inner_CachePool.Inner_DataUtil, Iterator<Long>> entry = this.init_LinkedHashMap.entrySet().iterator().next();
            if (entry.getValue().hasNext()) {
               this.load_Set.add(entry.getValue().next());
            } else {
               this.init_LinkedHashMap.remove(entry.getKey());
               if (entry.getKey().chunk()) {
                  this.write_Set.remove(entry.getKey().key());
               }
            }

            return true;
         }
      }

      boolean init_Z_BlockPos(BlockPos candidate) {
         if (!this.init_Predicate.test(candidate)) {
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
                     return this.init_LevelReader.getMinY();
                  } else if (name.equals("getHeight")) {
                     return this.init_LevelReader.getHeight();
                  } else if (method.isDefault()) {
                     return InvocationHandler.invokeDefault(proxy, method, args);
                  } else {
                     throw AiPrintSupply.Inner_CachePool.Inner_StreamUtil.init;
                  }
               } else if (++queries[0] <= 32 && this.init_Predicate.test(p)) {
                  this.pop++;
                  reads.add(p.asLong());
                  Object var10000;
                  switch (name) {
                     case "getBlockState":
                        var10000 = this.init_LevelReader.getBlockState(p);
                        break;
                     case "getFluidState":
                        var10000 = this.init_LevelReader.getFluidState(p);
                        break;
                     default:
                        var10000 = this.init_LevelReader.getBlockState(p).isAir();
                  }

                  return var10000;
               } else {
                  throw AiPrintSupply.Inner_CachePool.Inner_StreamUtil.init;
               }
            }
         );

         try {
            return AiSchematicActions.init(candidate, this.init_Map, metered);
         } catch (AiPrintSupply.Inner_CachePool.Inner_StreamUtil incomplete) {
            return true;
         } finally {
            Iterator var15 = reads.iterator();

            while (true) {
               if (!var15.hasNext()) {
                  break;
               } else {
                  long key = (Long)var15.next();
                  this.read_Map.computeIfAbsent(key, ignored -> AiPrintSupply.load_Set()).add(candidate.asLong());
                  long chunk = (long)(BlockPos.getX(key) >> 4) << 32 | BlockPos.getZ(key) >> 4 & 4294967295L;
                  Set<Long> owners = this.write_Map.computeIfAbsent(chunk, ignored -> AiPrintSupply.load_Set());
                  boolean added = owners.add(candidate.asLong());
                  this.load_Map.computeIfAbsent(candidate.asLong(), ignored -> ConcurrentHashMap.newKeySet()).add(chunk);
                  if (added && this.write_Set.contains(chunk) && this.init_Iterator == null) {
                     this.init_LinkedHashMap.put(new AiPrintSupply.Inner_CachePool.Inner_DataUtil(true, chunk), owners.iterator());
                  }
               }
            }
         }
      }

      private record Inner_DataUtil(boolean chunk, long key) {
      }

      private static final class Inner_StreamUtil extends RuntimeException {
         static final AiPrintSupply.Inner_CachePool.Inner_StreamUtil init = new AiPrintSupply.Inner_CachePool.Inner_StreamUtil();

         private Inner_StreamUtil() {
            super(null, null, false, false);
         }
      }
   }

   static final class Inner_ConfigStore<V> extends AbstractMap<Long, V> {
      private final ConcurrentSkipListMap<Long, Map<Long, V>> init_ConcurrentSkipList = new ConcurrentSkipListMap<>();
      private final AtomicInteger init_AtomicInteger = new AtomicInteger();

      private static long init(long key) {
         return BlockPos.asLong(BlockPos.getX(key) >> 4, BlockPos.getY(key) >> 4, BlockPos.getZ(key) >> 4);
      }

      public V init(Long key, V value) {
         Map<Long, V> map = this.init_ConcurrentSkipList.computeIfAbsent(init(key), ignored -> new ConcurrentHashMap<>());
         V before = map.put(key, value);
         if (before == null) {
            this.init_AtomicInteger.incrementAndGet();
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
            Map<Long, V> map = this.init_ConcurrentSkipList.get(init(value));
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
         return this.init_AtomicInteger.get();
      }

      @Override
      public Set<Entry<Long, V>> entrySet() {
         return new AbstractSet<Entry<Long, V>>() {
            @Override
            public int size() {
               return Inner_ConfigStore.this.init_AtomicInteger.get();
            }

            @Override
            public Iterator<Entry<Long, V>> iterator() {
               final Iterator<Map<Long, V>> outer = Inner_ConfigStore.this.init_ConcurrentSkipList.values().iterator();
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

   static final class Inner_DataUtil {
      static final int init_I = 40817;
      private static final int RADIUS = 24;
      final BlockPos init_BlockPos;
      final ClientLevel level;
      final int minChunkX;
      final int minChunkZ;
      final int chunkWidth;
      final int total;
      Iterator<BlockPos> pending = Collections.emptyIterator();
      int load;

      Inner_DataUtil(BlockPos center, LevelReader world) {
         this.init_BlockPos = center.immutable();
         this.level = world instanceof ClientLevel clientLevel ? clientLevel : null;
         this.minChunkX = center.getX() - RADIUS >> 4;
         this.minChunkZ = center.getZ() - RADIUS >> 4;
         this.chunkWidth = (center.getX() + RADIUS >> 4) - this.minChunkX + 1;
         int chunkDepth = (center.getZ() + RADIUS >> 4) - this.minChunkZ + 1;
         this.total = this.level == null ? init_I : this.chunkWidth * chunkDepth;
      }

      boolean init() {
         return this.load >= this.total && !this.pending.hasNext();
      }

      void init(AiPrintSupply.Inner_CachePool prep) {
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
            if (Math.abs(p.getX() - this.init_BlockPos.getX()) > RADIUS || Math.abs(p.getZ() - this.init_BlockPos.getZ()) > RADIUS) {
               return;
            }
         } else {
            int i = this.load++;
            p = this.init_BlockPos.offset(i % 49 - 24, i / 49 % 17 - 8, i / 833 - 24);
         }

         if (!prep.init_Map.containsKey(p.asLong()) && prep.init_Predicate.test(p) && prep.init_BlockState_BlockPos(p).getBlock() instanceof ShulkerBoxBlock) {
            prep.init_Set.add(p.asLong());
         }
      }
   }

   static final class Inner_JsonUtil extends AbstractList<BlockPos> {
      private final TreeMap<Integer, BlockPos[]> init_TreeMap = new TreeMap<>();
      private int init_I;

      @Override
      public int size() {
         return this.init_I;
      }

      public BlockPos get(int index) {
         return this.init(index);
      }

      public boolean add(BlockPos pos) {
         return this.init(pos);
      }

      public BlockPos init(int index) {
         Objects.checkIndex(index, this.init_I);
         return this.init_TreeMap.get(index >>> 10)[index & 1023];
      }

      public boolean init(BlockPos pos) {
         this.init_TreeMap.computeIfAbsent(this.init_I >>> 10, ignored -> new BlockPos[1024])[this.init_I & 1023] = pos;
         this.init_I++;
         return true;
      }
   }

   static final class Inner_NetHelper implements AiOperation {
      private AiOperation init_AiOperation;
      private AiOperation load_AiOperation;
      private final Supplier<AiPrintSupply.Inner_StreamUtil> init_Supplier;
      private final Supplier<AiOperation> load_Supplier;
      private final long init_J;
      private final AiPreparationReceipt init_bb = new AiPreparationReceipt();
      private AiOperation.Result init_AiOperation_Result;

      Inner_NetHelper(AiOperation navigation, Supplier<AiPrintSupply.Inner_StreamUtil> state, Supplier<AiOperation> open, long deadline) {
         this.init_AiOperation = navigation;
         this.init_Supplier = state;
         this.load_Supplier = open;
         this.init_J = deadline;
      }

      @Override
      public AiOperation.Result tick() {
         if (this.init_AiOperation_Result != null) {
            return this.init_AiOperation_Result;
         }

         if (System.nanoTime() >= this.init_J) {
            return this.init(new AiOperation.Result(AiOperation.Status.TIMED_OUT, "Stock navigation/withdrawal deadline reached", "{}", List.of(), AiFailure.TIMEOUT));
         }

         if (this.init_Supplier.get() == AiPrintSupply.Inner_StreamUtil.REPLACED) {
            return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "The remembered stock is no longer a shulker box", "{}", List.of(), AiFailure.MISSING_MATERIALS));
         }

         if (this.init_AiOperation != null) {
            AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.init_AiOperation);
            if (!result.terminal()) {
               return result;
            }

            if (result.status() != AiOperation.Status.SUCCEEDED) {
               return this.init(AiContainer.init(result, AiJson.init_JsonObject_ObjArr("action", "withdraw")));
            }

            if (this.init_AiOperation.requiresReconciliationAfterEmergency() || !this.init_AiOperation.canInterruptNow()) {
               return this.init(
                  new AiOperation.Result(AiOperation.Status.UNCERTAIN, "Stock approach needs reconciliation", result.dataJson(), result.evidence())
               );
            }

            this.init_AiOperation.cancel();
            this.init_AiOperation = null;
         }

         AiPrintSupply.Inner_StreamUtil observed = this.init_Supplier.get();
         if (observed == AiPrintSupply.Inner_StreamUtil.UNKNOWN) {
            return new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for the remembered stock chunk", "{}", List.of());
         }

         if (observed == AiPrintSupply.Inner_StreamUtil.REPLACED) {
            return this.init(new AiOperation.Result(AiOperation.Status.FAILED, "The remembered stock is no longer a shulker box", "{}", List.of(), AiFailure.MISSING_MATERIALS));
         }

         if (this.load_AiOperation == null) {
            this.load_AiOperation = this.load_Supplier.get();
         }

         AiOperation.Result result = this.init_bb.init_AiOperation_Result(this.load_AiOperation);
         return result.terminal() ? this.init(result) : result;
      }

      private AiOperation.Result init(AiOperation.Result result) {
         boolean pending = this.requiresReconciliationAfterEmergency();
         this.init();
         this.init_AiOperation_Result = this.init_bb.init(result, pending);
         return this.init_AiOperation_Result;
      }

      private void init() {
         if (this.init_AiOperation != null) {
            this.init_bb.init_V(this.init_AiOperation);
            this.init_AiOperation = null;
         }

         if (this.load_AiOperation != null) {
            this.init_bb.init_V(this.load_AiOperation);
            this.load_AiOperation = null;
         }
      }

      @Override
      public void cancel() {
         if (this.init_AiOperation_Result == null) {
            this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Stock operation cancelled", "{}", List.of()));
         } else {
            this.init();
         }
      }

      @Override
      public boolean canInterruptNow() {
         return !this.init_bb.init_Z(null)
            && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
            && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow());
      }

      @Override
      public boolean requiresReconciliationAfterEmergency() {
         return this.init_bb.init_Z(this.init_AiOperation) || this.init_bb.init_Z(this.load_AiOperation);
      }
   }

   enum Inner_StreamUtil {
      UNKNOWN,
      SHULKER,
      REPLACED;
   }

   static final class Inner_TaskQueue {
      Item init_Item;
      int init_I;
      final Set<Long> init_Set = new HashSet<>();
      final List<ItemStack> init_List = new ArrayList<>();

      boolean init(Item wanted, int available) {
         if (wanted != null && wanted != Items.AIR && available <= 0) {
            if (this.init_Item != wanted) {
               this.init();
               this.init_Item = wanted;
            }

            return ++this.init_I >= 10;
         } else {
            this.init();
            return false;
         }
      }

      boolean init(long key) {
         return this.init_Set.size() < 3 && this.init_Set.add(key);
      }

      void init() {
         this.init_Item = null;
         this.init_I = 0;
         this.init_Set.clear();
         this.init_List.clear();
      }
   }
}
