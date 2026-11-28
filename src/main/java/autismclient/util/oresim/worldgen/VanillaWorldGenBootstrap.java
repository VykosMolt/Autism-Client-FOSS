package autismclient.util.oresim.worldgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMap;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderLookup.RegistryLookup;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.core.RegistryAccess.Frozen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.chunk.Strategy;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

public final class VanillaWorldGenBootstrap {
   private static final Object LOCK = new Object();
   private static volatile VanillaWorldGenBootstrap.BootstrapState bootstrapState;
   private final long seed;
   private final ResourceKey<Level> dimension;
   private final Provider registries;
   private final RegistryAccess registryAccess;
   private final ChunkGenerator generator;
   private final RandomState randomState;
   private final ChunkGeneratorStructureState structureState;
   private final StructureTemplateManager templates;
   private final DimensionType dimensionType;
   private final LevelHeightAccessor heightAccessor;

   private VanillaWorldGenBootstrap(
      long seed,
      ResourceKey<Level> dimension,
      VanillaWorldGenBootstrap.BootstrapState bootstrap,
      ChunkGenerator generator,
      RandomState randomState,
      DimensionType dimensionType
   ) {
      this.seed = seed;
      this.dimension = dimension;
      this.registries = bootstrap.registries();
      this.registryAccess = bootstrap.registries();
      this.generator = generator;
      this.randomState = randomState;
      this.structureState = generator.createState(this.registries.lookupOrThrow(Registries.STRUCTURE_SET), randomState, seed);
      this.structureState.ensureStructuresGenerated();
      this.templates = bootstrap.templates();
      this.dimensionType = dimensionType;
      this.heightAccessor = LevelHeightAccessor.create(dimensionType.minY(), dimensionType.height());
   }

   public static VanillaWorldGenBootstrap create(long seed, ResourceKey<Level> dimension) {
      VanillaWorldGenBootstrap.BootstrapState bootstrap = sharedBootstrap();
      Provider lookup = bootstrap.registries();
      ResourceKey<LevelStem> stemKey = levelStemKey(dimension);
      WorldPreset preset = lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL).value();
      LevelStem stem = preset.createWorldDimensions().dimensions().get(stemKey);
      if (stem == null) {
         throw new IllegalStateException("No level stem for " + dimension);
      }

      ChunkGenerator generator = stem.generator();
      DimensionType dimensionType = stem.type().value();
      RandomState randomState = createRandomState(generator, lookup, seed);
      return new VanillaWorldGenBootstrap(seed, dimension, bootstrap, generator, randomState, dimensionType);
   }

   private static RandomState createRandomState(ChunkGenerator generator, Provider lookup, long seed) {
      if (generator instanceof NoiseBasedChunkGenerator noise) {
         NoiseGeneratorSettings settings = noise.generatorSettings().value();
         return RandomState.create(lookup.lookupOrThrow(Registries.NOISE), seed, settings);
      } else {
         throw new IllegalStateException("Unsupported generator: " + generator.getClass().getName());
      }
   }

   private static ResourceKey<LevelStem> levelStemKey(ResourceKey<Level> dimension) {
      if (Level.OVERWORLD.equals(dimension)) {
         return LevelStem.OVERWORLD;
      } else if (Level.NETHER.equals(dimension)) {
         return LevelStem.NETHER;
      } else if (Level.END.equals(dimension)) {
         return LevelStem.END;
      } else {
         throw new IllegalArgumentException("Unsupported vanilla dimension: " + dimension.identifier());
      }
   }

   private static VanillaWorldGenBootstrap.BootstrapState sharedBootstrap() {
      VanillaWorldGenBootstrap.BootstrapState bootstrap = bootstrapState;
      if (bootstrap != null) {
         return bootstrap;
      }

      synchronized (LOCK) {
         if (bootstrapState == null) {
            bootstrapState = createBootstrap();
         }

         return bootstrapState;
      }
   }

   private static VanillaWorldGenBootstrap.BootstrapState createBootstrap() {
      VanillaPackResources vanilla = ServerPacksSource.createVanillaPackSource();
      CloseableResourceManager resources = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(vanilla.fullResources()));
      Executor direct = Runnable::run;
      LayeredRegistryAccess<RegistryLayer> initial = RegistryLayer.createRegistryAccess();
      List<PendingTags<?>> staticTags = TagLoader.loadTagsForExistingRegistries(resources, initial.getLayer(RegistryLayer.STATIC));
      Map<Identifier, List<Identifier>> vanillaBlockTags = blockTagsFromPending(staticTags);
      List<RegistryLookup<?>> worldgenContext = TagLoader.buildUpdatedLookups(initial.getAccessForLoading(RegistryLayer.WORLD), staticTags);
      Frozen worldgen = RegistryDataLoader.load(resources, worldgenContext, RegistryDataLoader.WORLD_REGISTRIES, direct).join();
      List<RegistryLookup<?>> dimensionContext = Stream.concat(worldgenContext.stream(), worldgen.listRegistries()).toList();
      Frozen dimensions = RegistryDataLoader.load(resources, dimensionContext, RegistryDataLoader.DIMENSION_REGISTRIES, direct).join();
      Frozen registries = initial.replaceFrom(RegistryLayer.WORLD, worldgen, dimensions).compositeAccess();

      try {
         Path scratch = Files.createTempDirectory("autismclient-oresim-26_2-");
         scratch.toFile().deleteOnExit();
         LevelStorageSource storageSource = LevelStorageSource.createDefault(scratch);
         LevelStorageAccess storage = storageSource.createAccess("templates");
         storage.getLevelPath(LevelResource.ROOT).toFile().deleteOnExit();
         StructureTemplateManager templates = new StructureTemplateManager(
            resources, storage, DataFixers.getDataFixer(), registries.lookupOrThrow(Registries.BLOCK).filterFeatures(FeatureFlags.DEFAULT_FLAGS)
         );
         return new VanillaWorldGenBootstrap.BootstrapState(registries, resources, storage, templates, vanillaBlockTags);
      } catch (IOException error) {
         resources.close();
         throw new IllegalStateException("Could not create OreSim's temporary structure-template context", error);
      }
   }

   private static Map<Identifier, List<Identifier>> blockTagsFromPending(List<PendingTags<?>> pendingTags) {
      PendingTags<Block> vanillaBlocks = (PendingTags<Block>)pendingTags.stream()
         .filter(tags -> tags.key().equals(Registries.BLOCK))
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Vanilla datapack supplied no block tags"));
      Map<Identifier, List<Identifier>> expected = blockTags(vanillaBlocks.lookup());
      Map<Identifier, List<Identifier>> current = blockTags(BuiltInRegistries.BLOCK);
      if (current.isEmpty()) {
         pendingTags.forEach(PendingTags::apply);
         current = blockTags(BuiltInRegistries.BLOCK);
      }

      return Map.copyOf(expected);
   }

   private static Map<Identifier, List<Identifier>> blockTags(RegistryLookup<Block> lookup) {
      Map<Identifier, List<Identifier>> result = new TreeMap<>();

      try {
         lookup.listTags().forEach(tag -> {
            if ("minecraft".equals(tag.key().location().getNamespace())) {
               List<Identifier> blocks = tag.stream().map(holder -> holder.unwrapKey().orElseThrow().identifier()).sorted().toList();
               result.put(tag.key().location(), blocks);
            }
         });
      } catch (IllegalStateException var3) {
      }

      return result;
   }

   public long seed() {
      return this.seed;
   }

   public ResourceKey<Level> dimension() {
      return this.dimension;
   }

   public Provider registries() {
      return this.registries;
   }

   public RegistryAccess registryAccess() {
      return this.registryAccess;
   }

   public ChunkGenerator chunkGenerator() {
      return this.generator;
   }

   public NoiseBasedChunkGenerator noiseGenerator() {
      return (NoiseBasedChunkGenerator)this.generator;
   }

   public BiomeSource biomeSource() {
      return this.generator.getBiomeSource();
   }

   public RandomState randomState() {
      return this.randomState;
   }

   public ChunkGeneratorStructureState structureState() {
      return this.structureState;
   }

   public StructureTemplateManager structureTemplates() {
      return this.templates;
   }

   public boolean init_Z() {
      Map<Identifier, List<Identifier>> current = blockTags(BuiltInRegistries.BLOCK);

      for (Entry<Identifier, List<Identifier>> entry : bootstrapState.vanillaBlockTags().entrySet()) {
         if (!entry.getValue().equals(current.get(entry.getKey()))) {
            return false;
         }
      }

      return true;
   }

   public DimensionType dimensionType() {
      return this.dimensionType;
   }

   public LevelHeightAccessor heightAccessor() {
      return this.heightAccessor;
   }

   public PalettedContainerFactory paletteFactory() {
      final List<Reference<Biome>> biomes = this.registries.lookupOrThrow(Registries.BIOME).listElements().toList();
      final Map<Holder<Biome>, Integer> ids = new IdentityHashMap<>();

      for (int i = 0; i < biomes.size(); i++) {
         ids.put(biomes.get(i), i);
      }

      IdMap<Holder<Biome>> biomeIds = new IdMap<Holder<Biome>>() {
         public int getId(Holder<Biome> value) {
            return this.idOf(value);
         }

         public Holder<Biome> byId(int id) {
            return this.holderAt(id);
         }

         public int idOf(Holder<Biome> value) {
            Integer id = ids.get(value);
            return id == null ? -1 : id;
         }

         public Holder<Biome> holderAt(int id) {
            return id >= 0 && id < biomes.size() ? biomes.get(id) : null;
         }

         @Override
         public int size() {
            return biomes.size();
         }

         @Override
         public Iterator<Holder<Biome>> iterator() {
            return biomes.stream().map(reference -> (Holder<Biome>)reference).iterator();
         }
      };
      Strategy<BlockState> blockStrategy = Strategy.createForBlockStates(Block.BLOCK_STATE_REGISTRY);
      BlockState defaultBlock = Blocks.AIR.defaultBlockState();
      Holder<Biome> defaultBiome = this.registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
      return new PalettedContainerFactory(blockStrategy, defaultBlock, null, Strategy.createForBiomes(biomeIds), defaultBiome, null);
   }

   private record BootstrapState(
      Frozen registries,
      CloseableResourceManager resources,
      LevelStorageAccess storage,
      StructureTemplateManager templates,
      Map<Identifier, List<Identifier>> vanillaBlockTags
   ) {
   }
}
