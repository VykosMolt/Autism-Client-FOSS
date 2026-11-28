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
   public static final List<StorageEspCatalog.StorageTarget> TARGETS = List.of(
      blockTarget("minecraft:chest", "Chest", () -> Blocks.CHEST.asItem().getDefaultInstance(), ChestBlockEntity.class),
      blockTarget("minecraft:trapped_chest", "Trapped Chest", () -> Blocks.TRAPPED_CHEST.asItem().getDefaultInstance(), TrappedChestBlockEntity.class),
      blockTarget("minecraft:ender_chest", "Ender Chest", () -> Blocks.ENDER_CHEST.asItem().getDefaultInstance(), EnderChestBlockEntity.class),
      blockTarget("minecraft:barrel", "Barrel", () -> Blocks.BARREL.asItem().getDefaultInstance(), BarrelBlockEntity.class),
      blockTarget("minecraft:shulker_box", "Shulker Box", () -> Blocks.SHULKER_BOX.asItem().getDefaultInstance(), ShulkerBoxBlockEntity.class),
      blockTarget("minecraft:hopper", "Hopper", () -> Blocks.HOPPER.asItem().getDefaultInstance(), HopperBlockEntity.class),
      blockTarget("minecraft:dispenser", "Dispenser", () -> Blocks.DISPENSER.asItem().getDefaultInstance(), DispenserBlockEntity.class),
      blockTarget("minecraft:dropper", "Dropper", () -> Blocks.DROPPER.asItem().getDefaultInstance(), DropperBlockEntity.class),
      blockTarget("minecraft:furnace", "Furnace", () -> Blocks.FURNACE.asItem().getDefaultInstance(), FurnaceBlockEntity.class),
      blockTarget("minecraft:smoker", "Smoker", () -> Blocks.SMOKER.asItem().getDefaultInstance(), SmokerBlockEntity.class),
      blockTarget("minecraft:blast_furnace", "Blast Furnace", () -> Blocks.BLAST_FURNACE.asItem().getDefaultInstance(), BlastFurnaceBlockEntity.class),
      blockTarget("minecraft:brewing_stand", "Brewing Stand", () -> Items.BREWING_STAND.getDefaultInstance(), BrewingStandBlockEntity.class),
      blockTarget("minecraft:crafter", "Crafter", () -> Blocks.CRAFTER.asItem().getDefaultInstance(), CrafterBlockEntity.class),
      blockTarget("minecraft:decorated_pot", "Decorated Pot", () -> Blocks.DECORATED_POT.asItem().getDefaultInstance(), DecoratedPotBlockEntity.class),
      blockTarget(
         "minecraft:chiseled_bookshelf",
         "Chiseled Bookshelf",
         () -> Blocks.CHISELED_BOOKSHELF.asItem().getDefaultInstance(),
         ChiseledBookShelfBlockEntity.class
      ),
      blockTarget("minecraft:campfire", "Campfire", () -> Blocks.CAMPFIRE.asItem().getDefaultInstance(), CampfireBlockEntity.class),
      entityTarget("minecraft:chest_minecart", "Chest Minecart", () -> Items.CHEST_MINECART.getDefaultInstance(), MinecartChest.class),
      entityTarget("minecraft:hopper_minecart", "Hopper Minecart", () -> Items.HOPPER_MINECART.getDefaultInstance(), MinecartHopper.class),
      entityTarget("minecraft:furnace_minecart", "Furnace Minecart", () -> Items.FURNACE_MINECART.getDefaultInstance(), MinecartFurnace.class),
      entityTarget("minecraft:chest_boat", "Chest Boat", () -> Items.OAK_CHEST_BOAT.getDefaultInstance(), AbstractChestBoat.class)
   );
   private static final Map<String, StorageEspCatalog.StorageTarget> TARGETS_BY_ID;
   public static final String DEFAULT_IDS;
   private static volatile StorageEspCatalog.TargetSelection cachedSelection = new StorageEspCatalog.TargetSelection("", List.of(), List.of());

   public static StorageEspCatalog.StorageTarget byId(String id) {
      return id == null ? null : TARGETS_BY_ID.get(id.toLowerCase(Locale.ROOT));
   }

   private static StorageEspCatalog.StorageTarget blockTarget(String id, String label, Supplier<ItemStack> iconSupplier, Class<? extends BlockEntity> beClass) {
      return new StorageEspCatalog.StorageTarget(id, label, "Blocks", iconSupplier, beClass, null);
   }

   private static StorageEspCatalog.StorageTarget entityTarget(String id, String label, Supplier<ItemStack> iconSupplier, Class<? extends Entity> entityClass) {
      return new StorageEspCatalog.StorageTarget(id, label, "Entities", iconSupplier, null, entityClass);
   }

   private StorageEspCatalog() {
   }

   static void collectBoxes(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<AABB, Integer> emit) {
      collectInto(module, level, player, tickDelta, emit == null ? null : (box, color, meshable) -> emit.accept(box, color), null);
   }

   static void collectTraces(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<Vec3, Integer> emit) {
      collectInto(module, level, player, tickDelta, null, emit);
   }

   static void collect(Module module, ClientLevel level, Player player, float tickDelta, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      collectInto(module, level, player, tickDelta, boxEmit == null ? null : (box, color, meshable) -> boxEmit.accept(box, color), traceEmit);
   }

   static void init(Module module, ClientLevel level, Player player, float tickDelta, StorageEspCatalog.BoxSink boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      collectInto(module, level, player, tickDelta, boxEmit, traceEmit);
   }

   private static void collectInto(Module module, ClientLevel level, Player player, float tickDelta, StorageEspCatalog.BoxSink boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (module != null && level != null && player != null && (boxEmit != null || traceEmit != null)) {
         StorageEspCatalog.TargetSelection selection = parseSelection(module.value("storage-list"));
         List<StorageEspCatalog.StorageTarget> blockTargets = selection.blockTargets();
         List<StorageEspCatalog.StorageTarget> entityTargets = selection.load();
         if (!blockTargets.isEmpty() || !entityTargets.isEmpty()) {
            StorageEspCatalog.StorageColors colors = readColors(module);
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
                        for (EspChunkWorker.EspBox entry : EspChunkWorker.storage
                           .scan(chunk, gameTime, stamp, (scanned, out) -> scanChunk(level, scanned, blockTargets, colors, hideMask, out))) {
                           Vec3 trace = entry.tracer();
                           if (!(distanceSq(playerPos, trace.x, trace.y, trace.z) > maxDistSq)) {
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
                     StorageEspCatalog.StorageTarget matched = matchEntity(entity, entityTargets);
                     if (matched != null && !(distanceSq(playerPos, entity.getX(), entity.getY(), entity.getZ()) > maxDistSq)) {
                        AABB box = entityBox(entity, tickDelta).inflate(0.05);
                        int color = entityColor(colors, entity);
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

   private static void scanChunk(
      ClientLevel level, LevelChunk chunk, List<StorageEspCatalog.StorageTarget> blockTargets, StorageEspCatalog.StorageColors colors, long hideMask, List<EspChunkWorker.EspBox> out
   ) {
      Map<Integer, Long> sectionStructures = hideMask == 0L ? Map.of() : new HashMap<>();

      for (Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
         BlockPos pos = entry.getKey();
         BlockEntity be = entry.getValue();
         if (pos != null && be != null && matchBlockEntity(be, blockTargets) != null && !StructureStorageFilter.isInHiddenStructure(chunk, pos, hideMask, sectionStructures)) {
            out.add(new EspChunkWorker.EspBox(blockBox(level, pos), new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), blockEntityColor(colors, be)));
         }
      }
   }

   private static AABB blockBox(ClientLevel level, BlockPos pos) {
      BlockState state = level.getBlockState(pos);
      VoxelShape shape = state.getShape(level, pos);
      return shape != null && !shape.isEmpty() ? shape.bounds().move(pos.getX(), pos.getY(), pos.getZ()) : new AABB(pos);
   }

   private static StorageEspCatalog.StorageTarget matchBlockEntity(BlockEntity be, List<StorageEspCatalog.StorageTarget> targets) {
      StorageEspCatalog.StorageTarget best = null;
      int bestDepth = -1;

      for (StorageEspCatalog.StorageTarget target : targets) {
         if (target.beClass != null && target.beClass.isInstance(be)) {
            int depth = classDepth(be.getClass(), target.beClass);
            if (depth >= 0 && (best == null || depth < bestDepth)) {
               best = target;
               bestDepth = depth;
            }
         }
      }

      return best;
   }

   private static StorageEspCatalog.StorageTarget matchEntity(Entity entity, List<StorageEspCatalog.StorageTarget> targets) {
      StorageEspCatalog.StorageTarget best = null;
      int bestDepth = -1;

      for (StorageEspCatalog.StorageTarget target : targets) {
         if (target.entityClass != null && target.entityClass.isInstance(entity)) {
            int depth = classDepth(entity.getClass(), target.entityClass);
            if (depth >= 0 && (best == null || depth < bestDepth)) {
               best = target;
               bestDepth = depth;
            }
         }
      }

      return best;
   }

   private static int classDepth(Class<?> clazz, Class<?> target) {
      int depth = 0;

      for (Class<?> c = clazz; c != null; depth++) {
         if (c == target) {
            return depth;
         }

         c = c.getSuperclass();
      }

      return -1;
   }

   private static int blockEntityColor(StorageEspCatalog.StorageColors colors, BlockEntity be) {
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

   private static int entityColor(StorageEspCatalog.StorageColors colors, Entity entity) {
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

   private static AABB entityBox(Entity entity, float tickDelta) {
      double dx = Mth.lerp(tickDelta, entity.xOld, entity.getX()) - entity.getX();
      double dy = Mth.lerp(tickDelta, entity.yOld, entity.getY()) - entity.getY();
      double dz = Mth.lerp(tickDelta, entity.zOld, entity.getZ()) - entity.getZ();
      return entity.getBoundingBox().move(dx, dy, dz);
   }

   private static double distanceSq(Vec3 from, double x, double y, double z) {
      double dx = from.x - x;
      double dy = from.y - y;
      double dz = from.z - z;
      return dx * dx + dy * dy + dz * dz;
   }

   private static int colorOption(Module module, String option, int fallback) {
      return am.init_I_Module(module, option, fallback);
   }

   private static StorageEspCatalog.TargetSelection parseSelection(String value) {
      String safe = value == null ? "" : value;
      StorageEspCatalog.TargetSelection cached = cachedSelection;
      if (cached.safe().equals(safe)) {
         return cached;
      }

      List<StorageEspCatalog.StorageTarget> blockTargets = new ArrayList<>();
      List<StorageEspCatalog.StorageTarget> entityTargets = new ArrayList<>();

      for (String raw : safe.split("\\|")) {
         String token = raw.trim();
         if (!token.isEmpty()) {
            StorageEspCatalog.StorageTarget target = byId(token);
            if (target != null) {
               if (target.init_Z()) {
                  blockTargets.add(target);
               } else {
                  entityTargets.add(target);
               }
            }
         }
      }

      StorageEspCatalog.TargetSelection next = new StorageEspCatalog.TargetSelection(safe, List.copyOf(blockTargets), List.copyOf(entityTargets));
      cachedSelection = next;
      return next;
   }

   private static StorageEspCatalog.StorageColors readColors(Module module) {
      return new StorageEspCatalog.StorageColors(
         colorOption(module, "trapped-chest-color", -855695328),
         colorOption(module, "chest-color", -855662592),
         colorOption(module, "ender-chest-color", -864550657),
         colorOption(module, "barrel-color", -855662592),
         colorOption(module, "shulker-color", -860395777),
         colorOption(module, "hopper-color", -864253185),
         colorOption(module, "dispenser-color", -860862392),
         colorOption(module, "furnace-color", -859000218),
         colorOption(module, "crafter-color", -858214805),
         colorOption(module, "other-color", -7566196)
      );
   }

   private static Identifier blockEntityTypeId(BlockEntityType<?> type) {
      return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type);
   }

   static {
      Map<String, StorageEspCatalog.StorageTarget> map = new LinkedHashMap<>();
      StringBuilder def = new StringBuilder();

      for (StorageEspCatalog.StorageTarget target : TARGETS) {
         map.put(target.id, target);
         if (def.length() > 0) {
            def.append('|');
         }

         def.append(target.id);
      }

      TARGETS_BY_ID = Map.copyOf(map);
      DEFAULT_IDS = def.toString();
   }

   public static final class StorageTarget {
      public final String id;
      public final String label;
      public final String group;
      private final Supplier<ItemStack> iconSupplier;
      private final boolean blockEntityBacked;
      private final Class<? extends BlockEntity> beClass;
      private final Class<? extends Entity> entityClass;

      private StorageTarget(
         String id, String label, String group, Supplier<ItemStack> iconSupplier, Class<? extends BlockEntity> beClass, Class<? extends Entity> entityClass
      ) {
         this.id = id;
         this.label = label;
         this.group = group;
         this.iconSupplier = iconSupplier;
         this.blockEntityBacked = beClass != null;
         this.beClass = beClass;
         this.entityClass = entityClass;
      }

      public ItemStack icon() {
         try {
            ItemStack stack = this.iconSupplier.get();
            return stack == null ? ItemStack.EMPTY : stack;
         } catch (Throwable ignored) {
            return ItemStack.EMPTY;
         }
      }

      public boolean init_Z() {
         return this.blockEntityBacked;
      }
   }

   public static final class TargetIds {
      public static final String CHEST = "minecraft:chest";
      public static final String TRAPPED_CHEST = "minecraft:trapped_chest";
      public static final String ENDER_CHEST = "minecraft:ender_chest";
      public static final String BARREL = "minecraft:barrel";
      public static final String SHULKER_BOX = "minecraft:shulker_box";
      public static final String HOPPER = "minecraft:hopper";
      public static final String DISPENSER = "minecraft:dispenser";
      public static final String DROPPER = "minecraft:dropper";
      public static final String FURNACE = "minecraft:furnace";
      public static final String SMOKER = "minecraft:smoker";
      public static final String BLAST_FURNACE = "minecraft:blast_furnace";
      public static final String BREWING_STAND = "minecraft:brewing_stand";
      public static final String CRAFTER = "minecraft:crafter";
      public static final String DECORATED_POT = "minecraft:decorated_pot";
      public static final String CHISELED_BOOKSHELF = "minecraft:chiseled_bookshelf";
      public static final String CAMPFIRE = "minecraft:campfire";
      public static final String CHEST_MINECART = "minecraft:chest_minecart";
      public static final String HOPPER_MINECART = "minecraft:hopper_minecart";
      public static final String FURNACE_MINECART = "minecraft:furnace_minecart";
      public static final String CHEST_BOAT = "minecraft:chest_boat";

      private TargetIds() {
      }
   }

   private record TargetSelection(String safe, List<StorageEspCatalog.StorageTarget> blockTargets, List<StorageEspCatalog.StorageTarget> load) {
   }

   @FunctionalInterface
   interface BoxSink {
      void accept(AABB var1, int var2, boolean var3);
   }

   private record StorageColors(int init, int load, int read, int write, int update, int sync, int flush, int close, int open, int poll) {
   }
}
