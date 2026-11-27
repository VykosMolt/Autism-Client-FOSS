package autismclient.util.oresim.worldgen;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.attribute.EnvironmentAttributeReader;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.status.ChunkType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;

public final class SyntheticWorldGenLevel implements WorldGenLevel {
   private static final BlockState AIR = Blocks.AIR.defaultBlockState();
   private static final FluidState EMPTY_FLUID = Blocks.AIR.defaultBlockState().getFluidState();
   private final VanillaWorldGenBootstrap bootstrap;
   private final Map<Long, ChunkAccess> chunks;
   private final BiomeManager biomeManager;
   private final LevelLightEngine lightEngine;
   private RandomSource random;
   private long init_J;
   private int init_I;
   private int load_I;
   private int read;
   private final WorldGenTickAccess<Block> blockTicks = new WorldGenTickAccess<>(pos -> this.init(pos).getBlockTicks());
   private final WorldGenTickAccess<Fluid> fluidTicks = new WorldGenTickAccess<>(pos -> this.init(pos).getFluidTicks());

   public SyntheticWorldGenLevel(VanillaWorldGenBootstrap context, Map<Long, ChunkAccess> chunks) {
      this.bootstrap = context;
      this.chunks = chunks;
      this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(context.seed()));
      this.lightEngine = new LevelLightEngine(new LightChunkGetter() {
         @Override
         public LightChunk getChunkForLighting(int chunkX, int chunkZ) {
            return SyntheticWorldGenLevel.this.init(chunkX, chunkZ);
         }

         @Override
         public BlockGetter getLevel() {
            return SyntheticWorldGenLevel.this;
         }
      }, true, context.dimensionType().hasSkyLight());
      this.init(new ChunkPos(0, 0), 0);
   }

   public void init(ChunkPos center, int writeRadius) {
      this.random = this.bootstrap
         .randomState()
         .getOrCreateRandomFactory(Identifier.withDefaultNamespace("worldgen_region_random"))
         .at(center.getWorldPosition());
      this.init_J = 0L;
      this.init_I = center.x();
      this.load_I = center.z();
      this.read = Math.max(0, writeRadius);
   }

   private ChunkAccess init(int chunkX, int chunkZ) {
      return this.chunks.get(ChunkPos.pack(chunkX, chunkZ));
   }

   private ChunkAccess init(BlockPos pos) {
      ChunkAccess chunk = this.init(pos.getX() >> 4, pos.getZ() >> 4);
      if (chunk == null) {
         throw new IllegalStateException("Generation touched unprepared chunk at " + pos);
      } else {
         return chunk;
      }
   }

   StructureTemplateManager structureTemplates() {
      return this.bootstrap.structureTemplates();
   }

   ChunkGenerator chunkGenerator() {
      return this.bootstrap.chunkGenerator();
   }

   @Override
   public LevelTickAccess<Block> getBlockTicks() {
      return this.blockTicks;
   }

   @Override
   public LevelTickAccess<Fluid> getFluidTicks() {
      return this.fluidTicks;
   }

   @Override
   public long getSeed() {
      return this.bootstrap.seed();
   }

   @Override
   public ChunkAccess getChunk(int chunkX, int chunkZ, ChunkStatus status, boolean require) {
      ChunkAccess chunk = this.init(chunkX, chunkZ);
      if (chunk == null && require) {
         throw new IllegalStateException("Generation asked for unprepared chunk " + chunkX + ", " + chunkZ);
      } else {
         return chunk;
      }
   }

   @Override
   public boolean hasChunk(int chunkX, int chunkZ) {
      return this.init(chunkX, chunkZ) != null;
   }

   @Override
   public BlockState getBlockState(BlockPos pos) {
      if (this.isOutsideBuildHeight(pos)) {
         return AIR;
      }

      ChunkAccess chunk = this.init(pos.getX() >> 4, pos.getZ() >> 4);
      return chunk == null ? AIR : chunk.getBlockState(pos);
   }

   @Override
   public FluidState getFluidState(BlockPos pos) {
      if (this.isOutsideBuildHeight(pos)) {
         return EMPTY_FLUID;
      }

      ChunkAccess chunk = this.init(pos.getX() >> 4, pos.getZ() >> 4);
      return chunk == null ? EMPTY_FLUID : chunk.getFluidState(pos);
   }

   @Override
   public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
      if (!this.isOutsideBuildHeight(pos) && this.ensureCanWrite(pos)) {
         ChunkAccess chunk = this.init(pos.getX() >> 4, pos.getZ() >> 4);
         if (chunk == null) {
            return false;
         }

         BlockState oldState = chunk.setBlockState(pos, state, flags);
         if (state.hasBlockEntity()) {
            if (chunk.getPersistedStatus().getChunkType() == ChunkType.LEVELCHUNK) {
               BlockEntity blockEntity = ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);
               if (blockEntity != null) {
                  chunk.setBlockEntity(blockEntity);
               } else {
                  chunk.removeBlockEntity(pos);
               }
            } else {
               CompoundTag tag = new CompoundTag();
               tag.putInt("x", pos.getX());
               tag.putInt("y", pos.getY());
               tag.putInt("z", pos.getZ());
               tag.putString("id", "DUMMY");
               chunk.setBlockEntityNbt(tag);
            }
         } else if (oldState != null && oldState.hasBlockEntity()) {
            chunk.removeBlockEntity(pos);
         }

         if ((flags & 16) == 0) {
            BlockPos postProcess = state.getPostProcessPos(this, pos);
            if (postProcess != null) {
               this.init(postProcess).markPosForPostProcessing(postProcess);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean ensureCanWrite(BlockPos pos) {
      int chunkX = SectionPos.blockToSectionCoord(pos.getX());
      int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
      return Math.abs(this.init_I - chunkX) <= this.read && Math.abs(this.load_I - chunkZ) <= this.read;
   }

   @Override
   public int getHeight(Types type, int x, int z) {
      ChunkAccess chunk = this.init(x >> 4, z >> 4);
      return chunk == null ? this.getMinY() : chunk.getHeight(type, x & 15, z & 15) + 1;
   }

   @Override
   public Holder<Biome> getUncachedNoiseBiome(int quartX, int quartY, int quartZ) {
      return this.bootstrap.biomeSource().createCachingResolver(this.bootstrap.randomState()).getNoiseBiome(quartX, quartY, quartZ);
   }

   @Override
   public BiomeManager getBiomeManager() {
      return this.biomeManager;
   }

   @Override
   public RegistryAccess registryAccess() {
      return this.bootstrap.registryAccess();
   }

   @Override
   public DimensionType dimensionType() {
      return this.bootstrap.dimensionType();
   }

   @Override
   public int getMinY() {
      return this.bootstrap.heightAccessor().getMinY();
   }

   @Override
   public int getHeight() {
      return this.bootstrap.heightAccessor().getHeight();
   }

   @Override
   public int getSeaLevel() {
      return this.bootstrap.chunkGenerator().getSeaLevel();
   }

   @Override
   public void setCurrentlyGenerating(Supplier<String> currentlyGenerating) {
   }

   @Override
   public long getGameTime() {
      return 0L;
   }

   @Override
   public ServerLevel getLevel() {
      throw new UnsupportedOperationException("Synthetic worldgen level has no server level");
   }

   @Override
   public MinecraftServer getServer() {
      return null;
   }

   @Override
   public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
      throw new UnsupportedOperationException("Synthetic worldgen level has no difficulty");
   }

   @Override
   public WorldBorder getWorldBorder() {
      return new WorldBorder();
   }

   @Override
   public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
      return predicate.test(this.getFluidState(pos));
   }

   @Override
   public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
      return predicate.test(this.getBlockState(pos));
   }

   @Override
   public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
      BlockEntity blockEntity = this.getBlockEntity(pos);
      return blockEntity != null && blockEntity.getType() == type ? Optional.of((T)blockEntity) : Optional.empty();
   }

   @Override
   public BlockPos getHeightmapPos(Types type, BlockPos pos) {
      return new BlockPos(pos.getX(), this.getHeight(type, pos.getX(), pos.getZ()), pos.getZ());
   }

   @Override
   public List<Player> players() {
      return List.of();
   }

   @Override
   public List<Entity> getEntities(Entity except, AABB area, Predicate<? super Entity> filter) {
      return List.of();
   }

   @Override
   public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> test, AABB area, Predicate<? super T> filter) {
      return List.of();
   }

   @Override
   public BlockGetter getChunkForCollisions(int chunkX, int chunkZ) {
      return this.init(chunkX, chunkZ);
   }

   @Override
   public List<VoxelShape> getEntityCollisions(Entity entity, AABB area) {
      return List.of();
   }

   @Override
   public ChunkSource getChunkSource() {
      throw new UnsupportedOperationException("Synthetic worldgen level has no chunk source");
   }

   @Override
   public LevelData getLevelData() {
      throw new UnsupportedOperationException("Synthetic worldgen level has no level data");
   }

   @Override
   public LevelLightEngine getLightEngine() {
      return this.lightEngine;
   }

   @Override
   public BlockEntity getBlockEntity(BlockPos pos) {
      ChunkAccess chunk = this.init(pos);
      BlockEntity blockEntity = chunk.getBlockEntity(pos);
      if (blockEntity != null) {
         return blockEntity;
      }

      CompoundTag tag = chunk.getBlockEntityNbt(pos);
      BlockState state = chunk.getBlockState(pos);
      if (tag == null) {
         return null;
      }

      if ("DUMMY".equals(tag.getStringOr("id", ""))) {
         if (!state.hasBlockEntity()) {
            return null;
         }

         blockEntity = ((EntityBlock)state.getBlock()).newBlockEntity(pos, state);
      } else {
         blockEntity = BlockEntity.loadStatic(pos, state, tag, this.registryAccess());
      }

      if (blockEntity != null) {
         chunk.setBlockEntity(blockEntity);
      }

      return blockEntity;
   }

   @Override
   public boolean removeBlock(BlockPos pos, boolean move) {
      return this.setBlock(pos, AIR, 3, 512);
   }

   @Override
   public boolean destroyBlock(BlockPos pos, boolean drop, Entity breaker, int recursionLeft) {
      return this.setBlock(pos, AIR, 3, recursionLeft);
   }

   @Override
   public RandomSource getRandom() {
      return this.random;
   }

   @Override
   public long nextSubTickCount() {
      return this.init_J++;
   }

   @Override
   public int getSkyDarken() {
      return 0;
   }

   @Override
   public boolean isClientSide() {
      return false;
   }

   @Override
   public FeatureFlagSet enabledFeatures() {
      return FeatureFlags.DEFAULT_FLAGS;
   }

   @Override
   public EnvironmentAttributeReader environmentAttributes() {
      throw new UnsupportedOperationException("Synthetic worldgen level has no environment attributes");
   }

   @Override
   public void playSound(Entity source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
   }

   @Override
   public void addParticle(ParticleOptions particle, double x, double y, double z, double dx, double dy, double dz) {
   }

   @Override
   public void levelEvent(Entity source, int type, BlockPos pos, int data) {
   }

   @Override
   public void gameEvent(Holder<GameEvent> event, Vec3 pos, Context context) {
   }
}
