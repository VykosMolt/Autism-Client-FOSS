package autismclient.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractChestBoat;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StorageEspCatalog {
   public static final List<StorageEspCatalog.Inner_BufferHelper> TARGETS = List.of(
      init("minecraft:chest", "Chest", () -> Blocks.CHEST.asItem().getDefaultInstance(), ChestBlockEntity.class),
      init("minecraft:trapped_chest", "Trapped Chest", () -> Blocks.TRAPPED_CHEST.asItem().getDefaultInstance(), TrappedChestBlockEntity.class),
      init("minecraft:ender_chest", "Ender Chest", () -> Blocks.ENDER_CHEST.asItem().getDefaultInstance(), EnderChestBlockEntity.class),
      init("minecraft:barrel", "Barrel", () -> Blocks.BARREL.asItem().getDefaultInstance(), BarrelBlockEntity.class),
      init("minecraft:shulker_box", "Shulker Box", () -> Blocks.SHULKER_BOX.asItem().getDefaultInstance(), ShulkerBoxBlockEntity.class),
      init("minecraft:hopper", "Hopper", () -> Blocks.HOPPER.asItem().getDefaultInstance(), HopperBlockEntity.class),
      init("minecraft:dispenser", "Dispenser", () -> Blocks.DISPENSER.asItem().getDefaultInstance(), DispenserBlockEntity.class),
      init("minecraft:dropper", "Dropper", () -> Blocks.DROPPER.asItem().getDefaultInstance(), DropperBlockEntity.class),
      init("minecraft:furnace", "Furnace", () -> Blocks.FURNACE.asItem().getDefaultInstance(), FurnaceBlockEntity.class),
      init("minecraft:smoker", "Smoker", () -> Blocks.SMOKER.asItem().getDefaultInstance(), SmokerBlockEntity.class),
      init("minecraft:blast_furnace", "Blast Furnace", () -> Blocks.BLAST_FURNACE.asItem().getDefaultInstance(), BlastFurnaceBlockEntity.class),
      init("minecraft:brewing_stand", "Brewing Stand", () -> Items.BREWING_STAND.getDefaultInstance(), BrewingStandBlockEntity.class),
      init("minecraft:crafter", "Crafter", () -> Blocks.CRAFTER.asItem().getDefaultInstance(), CrafterBlockEntity.class),
      init("minecraft:decorated_pot", "Decorated Pot", () -> Blocks.DECORATED_POT.asItem().getDefaultInstance(), DecoratedPotBlockEntity.class),
      init(
         "minecraft:chiseled_bookshelf",
         "Chiseled Bookshelf",
         () -> Blocks.CHISELED_BOOKSHELF.asItem().getDefaultInstance(),
         ChiseledBookShelfBlockEntity.class
      ),
      init("minecraft:campfire", "Campfire", () -> Blocks.CAMPFIRE.asItem().getDefaultInstance(), CampfireBlockEntity.class),
      load("minecraft:chest_minecart", "Chest Minecart", () -> Items.CHEST_MINECART.getDefaultInstance(), MinecartChest.class),
      load("minecraft:hopper_minecart", "Hopper Minecart", () -> Items.HOPPER_MINECART.getDefaultInstance(), MinecartHopper.class),
      load("minecraft:furnace_minecart", "Furnace Minecart", () -> Items.FURNACE_MINECART.getDefaultInstance(), MinecartFurnace.class),
      load("minecraft:chest_boat", "Chest Boat", () -> Items.OAK_CHEST_BOAT.getDefaultInstance(), AbstractChestBoat.class)
   );
   private static final Map<String, StorageEspCatalog.Inner_BufferHelper> init_Map;
   public static final String DEFAULT_IDS;
   private static volatile StorageEspCatalog.Inner_ConfigStore init_ap_ConfigStore = new StorageEspCatalog.Inner_ConfigStore("", List.of(), List.of());

   public static StorageEspCatalog.Inner_BufferHelper byId(String id) {
      return id == null ? null : init_Map.get(id.toLowerCase(Locale.ROOT));
   }

   private static StorageEspCatalog.Inner_BufferHelper init(String id, String label, Supplier<ItemStack> iconSupplier, Class<? extends BlockEntity> beClass) {
      return new StorageEspCatalog.Inner_BufferHelper(id, label, "Blocks", iconSupplier, beClass, null);
   }

   private static StorageEspCatalog.Inner_BufferHelper load(String id, String label, Supplier<ItemStack> iconSupplier, Class<? extends Entity> entityClass) {
      return new StorageEspCatalog.Inner_BufferHelper(id, label, "Entities", iconSupplier, null, entityClass);
   }

   private StorageEspCatalog() {
   }

   static void init(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<AABB, Integer> emit) {
      load(module, level, player, tickDelta, emit == null ? null : (box, color, meshable) -> emit.accept(box, color), null);
   }

   static void load(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<Vec3, Integer> emit) {
      load(module, level, player, tickDelta, null, emit);
   }

   static void init(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      load(module, level, player, tickDelta, boxEmit == null ? null : (box, color, meshable) -> boxEmit.accept(box, color), traceEmit);
   }

   static void init(Module module, ClientLevel level, Player player, float tickDelta, StorageEspCatalog.Inner_DataUtil boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      load(module, level, player, tickDelta, boxEmit, traceEmit);
   }

   private static void load(Module module, ClientLevel level, Player player, float tickDelta, StorageEspCatalog.Inner_DataUtil boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (module != null && level != null && player != null && (boxEmit != null || traceEmit != null)) {
         StorageEspCatalog.Inner_ConfigStore selection = init_ap_ConfigStore(module.value("storage-list"));
         List<StorageEspCatalog.Inner_BufferHelper> blockTargets = selection.init_List();
         List<StorageEspCatalog.Inner_BufferHelper> entityTargets = selection.load();
         if (!blockTargets.isEmpty() || !entityTargets.isEmpty()) {
            StorageEspCatalog.Inner_StreamUtil colors = init(module);
            int chunkRadius = am.init_I();
            double maxDistSq = 2.0 * (chunkRadius * 16.0 + 16.0) * (chunkRadius * 16.0 + 16.0);
            Vec3 playerPos = player.position();
            int playerChunkX = player.chunkPosition().x();
            int playerChunkZ = player.chunkPosition().z();
            if (!blockTargets.isEmpty()) {
               EspChunkWorker.setLevel(level);
               long hideMask = StructureStorageFilter.hideMask(module);
               String stamp = module.value("storage-list") + "|" + colors + "|" + hideMask;
               long gameTime = level.getGameTime();
               ClientChunkCache chunks = level.getChunkSource();

               for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
                  for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                     LevelChunk chunk = chunks.getChunk(playerChunkX + dx, playerChunkZ + dz, ChunkStatus.FULL, false);
                     if (chunk != null) {
                        for (EspChunkWorker.EspBox entry : EspChunkWorker.write
                           .init(chunk, gameTime, stamp, (scanned, out) -> init(level, scanned, blockTargets, colors, hideMask, out))) {
                           Vec3 trace = entry.tracer();
                           if (!(init(playerPos, trace.x, trace.y, trace.z) > maxDistSq)) {
                              if (boxEmit != null) {
                                 boxEmit.accept(entry.box(), entry.color(), true);
                              }

                              if (traceEmit != null) {
                                 traceEmit.accept(trace, entry.color());
                              }
                           }
                        }
                     }
                  }
               }
            }

            if (!entityTargets.isEmpty()) {
               for (Entity entity : level.entitiesForRendering()) {
                  if (entity != null && entity.isAlive()) {
                     StorageEspCatalog.Inner_BufferHelper matched = init(entity, entityTargets);
                     if (matched != null && !(init(playerPos, entity.getX(), entity.getY(), entity.getZ()) > maxDistSq)) {
                        AABB box = init(entity, tickDelta).inflate(0.05);
                        int color = init(colors, entity);
                        if (boxEmit != null) {
                           boxEmit.accept(box, color, false);
                        }

                        if (traceEmit != null) {
                           traceEmit.accept(box.getCenter(), color);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static void init(
      ClientLevel level, LevelChunk chunk, List<StorageEspCatalog.Inner_BufferHelper> blockTargets, StorageEspCatalog.Inner_StreamUtil colors, long hideMask, List<EspChunkWorker.EspBox> out
   ) {
      Map<Integer, Long> sectionStructures = hideMask == 0L ? Map.of() : new HashMap<>();

      for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
         BlockPos pos = entry.getKey();
         BlockEntity be = entry.getValue();
         if (pos != null && be != null && init(be, blockTargets) != null && !StructureStorageFilter.isInHiddenStructure(chunk, pos, hideMask, sectionStructures)) {
            out.add(new EspChunkWorker.EspBox(init(level, pos), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), init(colors, be)));
         }
      }
   }

   private static AABB init(ClientLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      VoxelShape shape = state.getShape(level, pos);
      return shape != null && !shape.isEmpty() ? shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()) : new AABB(pos);
   }

   private static StorageEspCatalog.Inner_BufferHelper init(BlockEntity be, List<StorageEspCatalog.Inner_BufferHelper> targets) {
      StorageEspCatalog.Inner_BufferHelper best = null;
      int bestDepth = -1;

      for (StorageEspCatalog.Inner_BufferHelper target : targets) {
         if (target.init_Class != null && target.init_Class.isInstance(be)) {
            int depth = init(be.getClass(), target.init_Class);
            if (depth >= 0 && (best == null || depth < bestDepth)) {
               best = target;
               bestDepth = depth;
            }
         }
      }

      return best;
   }

   private static StorageEspCatalog.Inner_BufferHelper init(Entity entity, List<StorageEspCatalog.Inner_BufferHelper> targets) {
      StorageEspCatalog.Inner_BufferHelper best = null;
      int bestDepth = -1;

      for (StorageEspCatalog.Inner_BufferHelper target : targets) {
         if (target.load_Class != null && target.load_Class.isInstance(entity)) {
            int depth = init(entity.getClass(), target.load_Class);
            if (depth >= 0 && (best == null || depth < bestDepth)) {
               best = target;
               bestDepth = depth;
            }
         }
      }

      return best;
   }

   private static int init(Class<?> clazz, Class<?> target) {
      int depth = 0;

      for (Class<?> c = clazz; c != null; depth++) {
         if (c == target) {
            return depth;
         }

         c = c.getSuperclass();
      }

      return -1;
   }

   private static int init(StorageEspCatalog.Inner_StreamUtil colors, BlockEntity be) {
      if (be instanceof TrappedChestBlockEntity) {
         return colors.init();
      } else if (be instanceof ChestBlockEntity) {
         return colors.load();
      } else if (be instanceof EnderChestBlockEntity) {
         return colors.read();
      } else if (be instanceof BarrelBlockEntity) {
         return colors.write();
      } else if (be instanceof ShulkerBoxBlockEntity) {
         return colors.update();
      } else if (be instanceof HopperBlockEntity) {
         return colors.sync();
      } else if (be instanceof DispenserBlockEntity) {
         return colors.flush();
      } else if (be instanceof AbstractFurnaceBlockEntity) {
         return colors.close();
      } else if (be instanceof BrewingStandBlockEntity) {
         return colors.close();
      } else if (be instanceof CrafterBlockEntity) {
         return colors.open();
      } else if (be instanceof DecoratedPotBlockEntity) {
         return colors.open();
      } else if (be instanceof ChiseledBookShelfBlockEntity) {
         return colors.open();
      } else {
         return be instanceof CampfireBlockEntity ? colors.open() : colors.poll();
      }
   }

   private static int init(StorageEspCatalog.Inner_StreamUtil colors, Entity entity) {
      if (entity instanceof MinecartChest) {
         return colors.load();
      } else if (entity instanceof AbstractChestBoat) {
         return colors.load();
      } else if (entity instanceof MinecartHopper) {
         return colors.sync();
      } else {
         return entity instanceof MinecartFurnace ? colors.close() : colors.poll();
      }
   }

   private static AABB init(Entity entity, float tickDelta) {
      double dx = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - entity.getX();
      double dy = Mth.lerp(tickDelta, entity.yOld, entity.getY()) - entity.getY();
      double dz = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - entity.getZ();
      return entity.getBoundingBox().move(dx, dy, dz);
   }

   private static double init(Vec3 from, double x, double y, double z) {
      double dx = from.x - x;
      double dy = from.y - y;
      double dz = from.z - z;
      return dx * dx + dy * dy + dz * dz;
   }

   private static int init(Module module, String option, int fallback) {
      return am.init_I_Module(module, option, fallback);
   }

   private static StorageEspCatalog.Inner_ConfigStore init_ap_ConfigStore(String value) {
      String safe = value == null ? "" : value;
      StorageEspCatalog.Inner_ConfigStore cached = init_ap_ConfigStore;
      if (cached.init_Str().equals(safe)) {
         return cached;
      }

      List<StorageEspCatalog.Inner_BufferHelper> blockTargets = new ArrayList<>();
      List<StorageEspCatalog.Inner_BufferHelper> entityTargets = new ArrayList<>();

      for (String raw : safe.split("\\|")) {
         String token = raw.trim();
         if (!token.isEmpty()) {
            StorageEspCatalog.Inner_BufferHelper target = byId(token);
            if (target != null) {
               if (target.init_Z()) {
                  blockTargets.add(target);
               } else {
                  entityTargets.add(target);
               }
            }
         }
      }

      StorageEspCatalog.Inner_ConfigStore next = new StorageEspCatalog.Inner_ConfigStore(safe, List.copyOf(blockTargets), List.copyOf(entityTargets));
      init_ap_ConfigStore = next;
      return next;
   }

   private static StorageEspCatalog.Inner_StreamUtil init(Module module) {
      return new StorageEspCatalog.Inner_StreamUtil(
         init(module, "trapped-chest-color", -855695328),
         init(module, "chest-color", -855662592),
         init(module, "ender-chest-color", -864550657),
         init(module, "barrel-color", -855662592),
         init(module, "shulker-color", -860395777),
         init(module, "hopper-color", -864253185),
         init(module, "dispenser-color", -860862392),
         init(module, "furnace-color", -859000218),
         init(module, "crafter-color", -858214805),
         init(module, "other-color", -7566196)
      );
   }

   private static Identifier init(BlockEntityType<?> type) {
      return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
   }

   static {
      Map<String, StorageEspCatalog.Inner_BufferHelper> map = new LinkedHashMap<>();
      StringBuilder def = new StringBuilder();

      for (StorageEspCatalog.Inner_BufferHelper target : TARGETS) {
         map.put(target.id, target);
         if (def.length() > 0) {
            def.append('|');
         }

         def.append(target.id);
      }

      init_Map = Map.copyOf(map);
      DEFAULT_IDS = def.toString();
   }

   public static final class Inner_BufferHelper {
      public final String id;
      public final String label;
      public final String read;
      private final Supplier<ItemStack> init_Supplier;
      private final boolean init_Z;
      private final Class<? extends BlockEntity> init_Class;
      private final Class<? extends Entity> load_Class;

      private Inner_BufferHelper(
         String id, String label, String group, Supplier<ItemStack> iconSupplier, Class<? extends BlockEntity> beClass, Class<? extends Entity> entityClass
      ) {
         this.id = id;
         this.label = label;
         this.read = group;
         this.init_Supplier = iconSupplier;
         this.init_Z = beClass != null;
         this.init_Class = beClass;
         this.load_Class = entityClass;
      }

      public ItemStack icon() {
         try {
            ItemStack stack = this.init_Supplier.get();
            return stack == null ? ItemStack.EMPTY : stack;
         } catch (Throwable ignored) {
            return ItemStack.EMPTY;
         }
      }

      public boolean init_Z() {
         return this.init_Z;
      }
   }

   public static final class Inner_CachePool {
      public static final String init = "minecraft:chest";
      public static final String load = "minecraft:trapped_chest";
      public static final String read = "minecraft:ender_chest";
      public static final String write = "minecraft:barrel";
      public static final String update = "minecraft:shulker_box";
      public static final String sync = "minecraft:hopper";
      public static final String flush = "minecraft:dispenser";
      public static final String close = "minecraft:dropper";
      public static final String open = "minecraft:furnace";
      public static final String poll = "minecraft:smoker";
      public static final String push = "minecraft:blast_furnace";
      public static final String pop = "minecraft:brewing_stand";
      public static final String tick = "minecraft:crafter";
      public static final String run = "minecraft:decorated_pot";
      public static final String get = "minecraft:chiseled_bookshelf";
      public static final String set = "minecraft:campfire";
      public static final String put = "minecraft:chest_minecart";
      public static final String add = "minecraft:hopper_minecart";
      public static final String clear = "minecraft:furnace_minecart";
      public static final String reset = "minecraft:chest_boat";

      private Inner_CachePool() {
      }
   }

   private record Inner_ConfigStore(String init_Str, List<StorageEspCatalog.Inner_BufferHelper> init_List, List<StorageEspCatalog.Inner_BufferHelper> load) {
   }

   @FunctionalInterface
   interface Inner_DataUtil {
      void accept(AABB var1, int var2, boolean var3);
   }

   private record Inner_StreamUtil(int init, int load, int read, int write, int update, int sync, int flush, int close, int open, int poll) {
   }
}
