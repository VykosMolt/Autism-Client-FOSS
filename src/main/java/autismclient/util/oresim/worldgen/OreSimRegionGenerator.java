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
   private static final EnumSet<Types> PRIMED_HEIGHTMAPS = EnumSet.of(Types.MOTION_BLOCKING, Types.MOTION_BLOCKING_NO_LEAVES, Types.OCEAN_FLOOR, Types.WORLD_SURFACE);
   private final VanillaWorldGenBootstrap context;
   private final PalettedContainerFactory paletteFactory;

   public OreSimRegionGenerator(VanillaWorldGenBootstrap context) {
      this.context = context;
      this.paletteFactory = context.paletteFactory();
   }

   public Map<Long, ChunkAccess> generateRegion(int minChunkX, int minChunkZ, int size) {
      return this.generateRegion(minChunkX, minChunkZ, size, () -> false);
   }

   public Map<Long, ChunkAccess> generateRegion(int minChunkX, int minChunkZ, int size, BooleanSupplier cancelled) {
      if (size < 5) {
         throw new IllegalArgumentException("Region size must leave at least one reportable chunk");
      } else if (cancelled == null) {
         throw new IllegalArgumentException("Cancellation supplier must not be null");
      } else {
         return WorldGenThreadScope.run(() -> this.generateChunks(minChunkX, minChunkZ, size, cancelled));
      }
   }

   private Map<Long, ChunkAccess> generateChunks(int minChunkX, int minChunkZ, int size, BooleanSupplier cancelled) {
      Map<Long, ChunkAccess> chunks = new HashMap<>();

      for (int dx = -8; dx < size + 8; dx++) {
         for (int dz = -8; dz < size + 8; dz++) {
            checkCancelled(cancelled);
            ChunkPos pos = new ChunkPos(minChunkX + dx, minChunkZ + dz);
            chunks.put(pos.pack(), new ProtoChunk(pos, UpgradeData.EMPTY, this.context.heightAccessor(), this.paletteFactory, null));
         }
      }

      SyntheticWorldGenLevel level = new SyntheticWorldGenLevel(this.context, chunks);
      StructureManager structures = new StructureManager(level, new WorldOptions(this.context.seed(), true, false), null);

      for (int dx = -8; dx < size + 8; dx++) {
         for (int dz = -8; dz < size + 8; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            this.context
               .chunkGenerator()
               .createStructures(
                  this.context.registryAccess(),
                  this.context.structureState(),
                  structures,
                  chunk,
                  this.context.structureTemplates(),
                  this.context.dimension()
               );
            markStatus(chunk, ChunkStatus.STRUCTURE_STARTS);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            this.context.chunkGenerator().createReferences(level, structures, chunk);
            markStatus(chunk, ChunkStatus.STRUCTURE_REFERENCES);
         }
      }

      for (int dx = -1; dx <= size; dx++) {
         for (int dz = -1; dz <= size; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            this.context.chunkGenerator().createBiomes(this.context.randomState(), Blender.empty(), structures, chunk).join();
            markStatus(chunk, ChunkStatus.BIOMES);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            markStatus(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            this.init_V_WorldGenLevel(level, chunks, structures, chunk);
            markStatus(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            level.recenter(chunk.getPos(), 0);
            this.placeFeatures(level, chunks, structures, chunk, cancelled);
            markStatus(chunk, ChunkStatus.TERRAIN);
         }
      }

      for (int dx = 1; dx < size - 1; dx++) {
         for (int dz = 1; dz < size - 1; dz++) {
            checkCancelled(cancelled);
            ChunkAccess chunk = chunks.get(ChunkPos.pack(minChunkX + dx, minChunkZ + dz));
            Heightmap.primeHeightmaps(chunk, PRIMED_HEIGHTMAPS);
            level.recenter(chunk.getPos(), 1);
            this.context.chunkGenerator().applyBiomeDecoration(level, chunk, structures);
            markStatus(chunk, ChunkStatus.FEATURES);
         }
      }

      Map<Long, ChunkAccess> result = new HashMap<>(size * size);

      for (int dx = 0; dx < size; dx++) {
         for (int dz = 0; dz < size; dz++) {
            checkCancelled(cancelled);
            long key = ChunkPos.pack(minChunkX + dx, minChunkZ + dz);
            result.put(key, chunks.get(key));
         }
      }

      return result;
   }

   private static void checkCancelled(BooleanSupplier cancelled) {
      if (cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) {
         throw new CancellationException("OreSim region generation superseded");
      }
   }

   private static void markStatus(ChunkAccess chunk, ChunkStatus status) {
      if (chunk instanceof ProtoChunk proto) {
         proto.setPersistedStatus(status);
      }
   }

   private void init_V_WorldGenLevel(WorldGenLevel level, Map<Long, ChunkAccess> chunks, StructureManager structures, ChunkAccess chunk) {
      new WorldGenerationContext(this.context.chunkGenerator(), this.context.heightAccessor());
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

   private void placeFeatures(SyntheticWorldGenLevel level, Map<Long, ChunkAccess> chunks, StructureManager structures, ChunkAccess chunk, BooleanSupplier cancelled) {
      NoiseBasedChunkGenerator generator = this.context.noiseGenerator();
      BiomeManager biomeManager = level.getBiomeManager()
         .withDifferentSource(
            (quartX, quartY, quartZ) -> this.context
               .biomeSource()
               .createCachingResolver(this.context.randomState())
               .getNoiseBiome(quartX, quartY, quartZ)
         );
      new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
      ChunkPos targetPos = chunk.getPos();
   }

   public static void scanChunk(ChunkAccess chunk, Predicate<BlockState> wanted, OreSimRegionGenerator.BlockSink sink) {
      scanChunk(chunk, wanted, sink, () -> false);
   }

   public static void scanChunk(ChunkAccess chunk, Predicate<BlockState> wanted, OreSimRegionGenerator.BlockSink sink, BooleanSupplier cancelled) {
      if (cancelled == null) {
         throw new IllegalArgumentException("Cancellation supplier must not be null");
      }

      LevelChunkSection[] sections = chunk.getSections();
      int minX = chunk.getPos().getMinBlockX();
      int minZ = chunk.getPos().getMinBlockZ();

      for (int index = 0; index < sections.length; index++) {
         checkCancelled(cancelled);
         LevelChunkSection section = sections[index];
         if (section != null && !section.hasOnlyAir() && section.maybeHas(wanted)) {
            int baseY = chunk.getSectionYFromSectionIndex(index) << 4;

            for (int y = 0; y < 16; y++) {
               checkCancelled(cancelled);

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

   public static long packPos(int x, int y, int z) {
      return BlockPos.asLong(x, y, z);
   }

   @FunctionalInterface
   public interface BlockSink {
      void accept(int var1, int var2, int var3, BlockState var4);
   }
}
