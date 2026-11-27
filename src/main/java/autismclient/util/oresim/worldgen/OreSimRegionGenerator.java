package autismclient.util.oresim.worldgen;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.blending.Blender;

public final class OreSimRegionGenerator {
   public static final int init_I = 2;
   private static final int load = 8;
   private static final EnumSet<Types> init_EnumSet = EnumSet.of(Types.MOTION_BLOCKING, Types.MOTION_BLOCKING_NO_LEAVES, Types.OCEAN_FLOOR, Types.WORLD_SURFACE);
   private final VanillaWorldGenBootstrap init_ConfigStore;
   private final PalettedContainerFactory init_PalettedContainerF;

   public OreSimRegionGenerator(VanillaWorldGenBootstrap context) {
      this.init_ConfigStore = context;
      this.init_PalettedContainerF = context.paletteFactory();
   }

   public Map<Long, ChunkAccess> init_Map_I(int minChunkX, int minChunkZ, int size) {
      return this.generateRegion(minChunkX, minChunkZ, size, () -> false);
   }

   public Map<Long, ChunkAccess> generateRegion(int minChunkX, int minChunkZ, int size, BooleanSupplier cancelled) {
      if (size < 5) {
         throw new IllegalArgumentException("Region size must leave at least one reportable chunk");
      } else if (cancelled == null) {
         throw new IllegalArgumentException("Cancellation supplier must not be null");
      } else {
         return WorldGenThreadScope.init(() -> this.load(minChunkX, minChunkZ, size, cancelled));
      }
   }

   private Map<Long, ChunkAccess> load(int minChunkX, int minChunkZ, int size, BooleanSupplier cancelled) {
      Map<Long, ChunkAccess> chunks = new HashMap<>();

      for (int dx = -8; dx < size + 8; dx++) {
         for (int dz = -8; dz < size + 8; dz++) {
            init(cancelled);
            ChunkPos pos = new ChunkPos(minChunkX + dx, minChunkZ + dz);
            chunks.put(pos.pack(), new ProtoChunk(pos, UpgradeData.EMPTY, this.init_ConfigStore.heightAccessor(), this.init_PalettedContainerF, null));
         }
      }

      SyntheticWorldGenLevel level = new SyntheticWorldGenLevel(this.init_ConfigStore, chunks);
      StructureManager structures = new StructureManager(level, new WorldOptions(this.init_ConfigStore.seed(), true, false), null);

      for (int dx = -8; dx < size + 8; dx++) {
         for (int dz = -8; dz < size + 8; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            this.init_ConfigStore
               .chunkGenerator()
               .createStructures(
                  this.init_ConfigStore.registryAccess(),
                  this.init_ConfigStore.structureState(),
                  structures,
                  chunk,
                  this.init_ConfigStore.structureTemplates(),
                  this.init_ConfigStore.dimension()
               );
            init(chunk, ChunkStatus.STRUCTURE_STARTS);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            this.init_ConfigStore.chunkGenerator().createReferences(level, structures, chunk);
            init(chunk, ChunkStatus.STRUCTURE_REFERENCES);
         }
      }

      for (int dx = -1; dx <= size; dx++) {
         for (int dz = -1; dz <= size; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            this.init_ConfigStore.chunkGenerator().createBiomes(this.init_ConfigStore.randomState(), Blender.empty(), structures, chunk).join();
            init(chunk, ChunkStatus.BIOMES);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            init(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            this.init_V_WorldGenLevel(level, chunks, structures, chunk);
            init(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.init(chunk.getPos(), 0);
            this.init(level, chunks, structures, chunk, cancelled);
            init(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 1; dx < size - 1; dx++) {
         for (int dz = 1; dz < size - 1; dz++) {
            init(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            Heightmap.primeHeightmaps(chunk, init_EnumSet);
            level.init(chunk.getPos(), 1);
            this.init_ConfigStore.chunkGenerator().applyBiomeDecoration(level, chunk, structures);
            init(chunk, ChunkStatus.FEATURES);
         }
      }

      Map<Long, ChunkAccess> result = new HashMap<>(size * size);

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            init(cancelled);
            long key = ChunkPos.pack(minChunkX + dx, minChunkZ + dz);
            result.put(key, chunks.get(key));
         }
      }

      return result;
   }

   private static void init(BooleanSupplier cancelled) {
      if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
         throw new CancellationException("OreSim region generation superseded");
      }
   }

   private static void init(ChunkAccess chunk, ChunkStatus status) {
      if (chunk instanceof ProtoChunk proto) {
         proto.setPersistedStatus(status);
      }
   }

   private void init_V_WorldGenLevel(WorldGenLevel level, Map<Long, ChunkAccess> chunks, StructureManager structures, ChunkAccess chunk) {
      new WorldGenerationContext(this.init_ConfigStore.chunkGenerator(), this.init_ConfigStore.heightAccessor());
      Set<Holder<Biome>> biomes = new HashSet<>();
      ChunkPos pos = chunk.getPos();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            ChunkAccess neighbour = chunks.get(ChunkPos.pack(pos.x() + dx, pos.z() + dz));
            if (neighbour != null) {
               for (LevelChunkSection section : neighbour.getSections()) {
                  if (section != null) {
                     section.getBiomes().getAll(biomes::add);
                  }
               }
            }
         }
      }
   }

   private void init(SyntheticWorldGenLevel level, Map<Long, ChunkAccess> chunks, StructureManager structures, ChunkAccess chunk, BooleanSupplier cancelled) {
      NoiseBasedChunkGenerator generator = this.init_ConfigStore.noiseGenerator();
      BiomeManager biomeManager = level.getBiomeManager()
         .withDifferentSource(
            (quartX, quartY, quartZ) -> this.init_ConfigStore
               .biomeSource()
               .createCachingResolver(this.init_ConfigStore.randomState())
               .getNoiseBiome(quartX, quartY, quartZ)
         );
      new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      ChunkPos targetPos = chunk.getPos();
   }

   public static void init_V_ChunkAccess(ChunkAccess chunk, Predicate<BlockState> wanted, OreSimRegionGenerator.Inner_DataUtil sink) {
      scanChunk(chunk, wanted, sink, () -> false);
   }

   public static void scanChunk(ChunkAccess chunk, Predicate<BlockState> wanted, OreSimRegionGenerator.Inner_DataUtil sink, BooleanSupplier cancelled) {
      if (cancelled == null) {
         throw new IllegalArgumentException("Cancellation supplier must not be null");
      }

      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();

      for (int index = 0; index < sections.length; index++) {
         init(cancelled);
         LevelChunkSection section = sections[index];
         if (section != null && !section.hasOnlyAir() && section.maybeHas(wanted)) {
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;

            for (int y = 0; y < 16; y++) {
               init(cancelled);

               for (int x = 0; x < 16; x++) {
                  for (int z = 0; z < 16; z++) {
                     BlockState state = section.getBlockState(x, y, z);
                     if (wanted.test(state)) {
                        sink.accept(minX + x, baseY + y, minZ + z, state);
                     }
                  }
               }
            }
         }
      }
   }

   public static long init_J_I(int x, int y, int z) {
      return BlockPos.asLong(x, y, z);
   }

   @FunctionalInterface
   public interface Inner_DataUtil {
      void accept(int var1, int var2, int var3, BlockState var4);
   }
}
