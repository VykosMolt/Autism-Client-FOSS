package autismclient.util.j.a;

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
   private static final Object init_Obj = new Object();
   private static volatile VanillaWorldGenBootstrap.Inner_DataUtil init_ConfigStore_DataUt;
   private final long init_J;
   private final ResourceKey<Level> init_ResourceKey;
   private final Provider init_HolderLookup_Provi;
   private final RegistryAccess init_RegistryAccess;
   private final ChunkGenerator init_ChunkGenerator;
   private final RandomState init_RandomState;
   private final ChunkGeneratorStructureState init_ChunkGeneratorStru;
   private final StructureTemplateManager init_StructureTemplateM;
   private final DimensionType init_DimensionType;
   private final LevelHeightAccessor init_LevelHeightAccesso;

   private VanillaWorldGenBootstrap(
      long seed,
      ResourceKey<Level> dimension,
      VanillaWorldGenBootstrap.Inner_DataUtil bootstrap,
      ChunkGenerator generator,
      RandomState randomState,
      DimensionType dimensionType
   ) {
      this.init_J = seed;
      this.init_ResourceKey = dimension;
      this.init_HolderLookup_Provi = bootstrap.init_RegistryAccess_Fro();
      this.init_RegistryAccess = bootstrap.init_RegistryAccess_Fro();
      this.init_ChunkGenerator = generator;
      this.init_RandomState = randomState;
      this.init_ChunkGeneratorStru = generator.createState(this.init_HolderLookup_Provi.lookupOrThrow(Registries.STRUCTURE_SET), randomState, seed);
      this.init_ChunkGeneratorStru.ensureStructuresGenerated();
      this.init_StructureTemplateM = bootstrap.init_StructureTemplateM();
      this.init_DimensionType = dimensionType;
      this.init_LevelHeightAccesso = LevelHeightAccessor.create(dimensionType.minY(), dimensionType.height());
   }

   public static VanillaWorldGenBootstrap init(long seed, ResourceKey<Level> dimension) {
      VanillaWorldGenBootstrap.Inner_DataUtil bootstrap = init_ConfigStore_DataUt();
      Provider lookup = bootstrap.init_RegistryAccess_Fro();
      ResourceKey<LevelStem> stemKey = init(dimension);
      WorldPreset preset = lookup.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.NORMAL).value();
      LevelStem stem = preset.createWorldDimensions().dimensions().get(stemKey);
      if (stem == null) {
         throw new IllegalStateException("No level stem for " + dimension);
      }

      ChunkGenerator generator = stem.generator();
      DimensionType dimensionType = stem.type().value();
      RandomState randomState = init(generator, lookup, seed);
      return new VanillaWorldGenBootstrap(seed, dimension, bootstrap, generator, randomState, dimensionType);
   }

   private static RandomState init(ChunkGenerator generator, Provider lookup, long seed) {
      if (generator instanceof NoiseBasedChunkGenerator noise) {
         NoiseGeneratorSettings settings = noise.generatorSettings().value();
         return RandomState.create(lookup.lookupOrThrow(Registries.NOISE), seed, settings);
      } else {
         throw new IllegalStateException("Unsupported generator: " + generator.getClass().getName());
      }
   }

   private static ResourceKey<LevelStem> init(ResourceKey<Level> dimension) {
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

   private static VanillaWorldGenBootstrap.Inner_DataUtil init_ConfigStore_DataUt() {
      VanillaWorldGenBootstrap.Inner_DataUtil bootstrap = init_ConfigStore_DataUt;
      if (bootstrap != null) {
         return bootstrap;
      }

      synchronized (init_Obj) {
         if (init_ConfigStore_DataUt == null) {
            init_ConfigStore_DataUt = load();
         }

         return init_ConfigStore_DataUt;
      }
   }

   private static VanillaWorldGenBootstrap.Inner_DataUtil load() {
      VanillaPackResources vanilla = ServerPacksSource.createVanillaPackSource();
      CloseableResourceManager resources = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(vanilla.fullResources()));
      Executor direct = Runnable::run;
      LayeredRegistryAccess<RegistryLayer> initial = RegistryLayer.createRegistryAccess();
      List<PendingTags<?>> staticTags = TagLoader.loadTagsForExistingRegistries(resources, initial.getLayer(RegistryLayer.STATIC));
      Map<Identifier, List<Identifier>> vanillaBlockTags = init(staticTags);
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
         return new VanillaWorldGenBootstrap.Inner_DataUtil(registries, resources, storage, templates, vanillaBlockTags);
      } catch (IOException error) {
         resources.close();
         throw new IllegalStateException("Could not create OreSim's temporary structure-template context", error);
      }
   }

   private static Map<Identifier, List<Identifier>> init(List<PendingTags<?>> pendingTags) {
      PendingTags<Block> vanillaBlocks = (PendingTags<Block>)pendingTags.stream()
         .filter(tags -> tags.key().equals(Registries.BLOCK))
         .findFirst()
         .orElseThrow(() -> new IllegalStateException("Vanilla datapack supplied no block tags"));
      Map<Identifier, List<Identifier>> expected = init(vanillaBlocks.lookup());
      Map<Identifier, List<Identifier>> current = init(BuiltInRegistries.BLOCK);
      if (current.isEmpty()) {
         pendingTags.forEach(PendingTags::apply);
         current = init(BuiltInRegistries.BLOCK);
      }

      return Map.copyOf(expected);
   }

   private static Map<Identifier, List<Identifier>> init(RegistryLookup<Block> lookup) {
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

   public long init_J() {
      return this.init_J;
   }

   public ResourceKey<Level> init_ResourceKey() {
      return this.init_ResourceKey;
   }

   public Provider init_HolderLookup_Provi() {
      return this.init_HolderLookup_Provi;
   }

   public RegistryAccess init_RegistryAccess() {
      return this.init_RegistryAccess;
   }

   public ChunkGenerator init_ChunkGenerator() {
      return this.init_ChunkGenerator;
   }

   public NoiseBasedChunkGenerator init_NoiseBasedChunkGen() {
      return (NoiseBasedChunkGenerator)this.init_ChunkGenerator;
   }

   public BiomeSource init_BiomeSource() {
      return this.init_ChunkGenerator.getBiomeSource();
   }

   public RandomState init_RandomState() {
      return this.init_RandomState;
   }

   public ChunkGeneratorStructureState init_ChunkGeneratorStru() {
      return this.init_ChunkGeneratorStru;
   }

   public StructureTemplateManager init_StructureTemplateM() {
      return this.init_StructureTemplateM;
   }

   public boolean init_Z() {
      Map<Identifier, List<Identifier>> current = init(BuiltInRegistries.BLOCK);

      for (Entry<Identifier, List<Identifier>> entry : init_ConfigStore_DataUt.init_Map().entrySet()) {
         if (!entry.getValue().equals(current.get(entry.getKey()))) {
            return false;
         }
      }

      return true;
   }

   public DimensionType init_DimensionType() {
      return this.init_DimensionType;
   }

   public LevelHeightAccessor init_LevelHeightAccesso() {
      return this.init_LevelHeightAccesso;
   }

   public PalettedContainerFactory init_PalettedContainerF() {
      final List<Reference<Biome>> biomes = this.init_HolderLookup_Provi.lookupOrThrow(Registries.BIOME).listElements().toList();
      final Map<Holder<Biome>, Integer> ids = new IdentityHashMap<>();

      for (int i = 0; i < biomes.size(); i++) {
         ids.put(biomes.get(i), i);
      }

      IdMap<Holder<Biome>> biomeIds = new IdMap<Holder<Biome>>() {
         public int getId(Holder<Biome> value) {
            return this.init(value);
         }

         public Holder<Biome> byId(int id) {
            return this.init(id);
         }

         public int init(Holder<Biome> value) {
            Integer id = ids.get(value);
            return id == null ? -1 : id;
         }

         public Holder<Biome> init(int id) {
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
      Holder<Biome> defaultBiome = this.init_HolderLookup_Provi.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
      return new PalettedContainerFactory(blockStrategy, defaultBlock, null, Strategy.createForBiomes(biomeIds), defaultBiome, null);
   }

   private record Inner_DataUtil(
      Frozen init_RegistryAccess_Fro,
      CloseableResourceManager init_CloseableResourceM,
      LevelStorageAccess init_LevelStorageSource,
      StructureTemplateManager init_StructureTemplateM,
      Map<Identifier, List<Identifier>> init_Map
   ) {
   }
}
