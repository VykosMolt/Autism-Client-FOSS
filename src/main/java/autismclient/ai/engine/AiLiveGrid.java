package autismclient.ai.engine;

import autismclient.ai.AiItems;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AiLiveGrid implements AiPathfinder.Inner_StateCache {
   static final int flowState = 6;
   private final Minecraft putAt_Minecraft;
   private final boolean putAt_Z;
   private final ClientLevel putAt_ClientLevel;
   private final AiChunkMemory putAt_PacketHelper;
   private final int fill;
   private final int readEx;
   private volatile AiLiveGrid.Inner_DataUtil putAt_PoolStore_CacheHel;
   private static final AtomicReferenceArray<AiLiveGrid.Inner_StreamUtil> putAt_AtomicReferenceArr = new AtomicReferenceArray<>(
      Math.max(1, Block.BLOCK_STATE_REGISTRY.size())
   );
   private static final long putAt_J = 30000000000L;
   private static final int value = 4;
   private static final int prevEx = 40;
   private static final Map<Long, Long> putAt_Map = new HashMap<>();
   private static Object putAt_Obj;

   public AiLiveGrid(Minecraft var1, ItemStack[] var2, AiChunkMemory var3) {
      this.putAt_Minecraft = var1;
      this.putAt_ClientLevel = var1.level;
      this.putAt_PacketHelper = var3;
      this.fill = this.putAt_ClientLevel.getMinY();
      this.readEx = this.putAt_ClientLevel.getMaxY() - 1;
      this.putAt_Z = this.putAt_ClientLevel.dimension() == Level.NETHER;
      this.putAt_PoolStore_CacheHel = new AiLiveGrid.Inner_DataUtil(putAt_ItemStackArr_ItemStackArr(var2), MiningCost.Inner_DataUtil.of(var1), 0L);
   }

   public ClientLevel putAt_ClientLevel() {
      return this.putAt_ClientLevel;
   }

   @Override
   public double movementSpeed() {
      return this.putAt_Minecraft.player == null ? 0.1 : AiLiveWorld.putAt_D_LivingEntity(this.putAt_Minecraft.player);
   }

   public void putAt_V_ItemStackArr_2(ItemStack[] var1) {
      AiLiveGrid.Inner_DataUtil var2 = this.putAt_PoolStore_CacheHel;
      MiningCost.Inner_DataUtil var3 = MiningCost.Inner_DataUtil.of(this.putAt_Minecraft);
      if (!putAt(var2.putAt_ItemStackArr, var1) || !var3.equals(var2.putAt_ByteWorker_CacheHe)) {
         this.putAt_PoolStore_CacheHel = new AiLiveGrid.Inner_DataUtil(putAt_ItemStackArr_ItemStackArr(var1), var3, var2.putAt_J + 1L, var2.putAt_BiPredicate);
      }
   }

   public void putAt_V_BiPredicate_2(BiPredicate<BlockState, ItemStack> var1) {
      AiLiveGrid.Inner_DataUtil var2 = this.putAt_PoolStore_CacheHel;
      if (var2.putAt_BiPredicate != var1) {
         this.putAt_PoolStore_CacheHel = new AiLiveGrid.Inner_DataUtil(var2.putAt_ItemStackArr, var2.putAt_ByteWorker_CacheHe, var2.putAt_J + 1L, var1);
      }
   }

   @Override
   public long putAt_J() {
      return this.putAt_PoolStore_CacheHel.putAt_J;
   }

   @Override
   public AiPathfinder.Inner_FileCache probe(int var1, int var2, int var3) {
      if (var2 < this.fill) {
         return AiPathfinder.Inner_FileCache.UNLOADED;
      }

      if (var2 > this.readEx) {
         return AiPathfinder.Inner_FileCache.air();
      }

      BlockState var4 = this.putAt_BlockState(var1, var2, var3);
      if (var4 == null) {
         return this.putAt_FileStore_BitCache(var1, var2, var3);
      }

      AiLiveGrid.Inner_StreamUtil var5 = putAt_PoolStore_TableCac_BlockState(var4);
      if (var5 == null) {
         return AiPathfinder.Inner_FileCache.UNLOADED;
      }

      AiPathfinder.Inner_FileCache var6 = !var5.diggableKind() ? var5.probe() : putAt(var5.probe(), this.putAt(var4, var5, var1, var2, var3));
      if (var5.falling() && var2 - 1 >= this.fill) {
         BlockState var7 = this.putAt_BlockState(var1, var2 - 1, var3);
         if (var7 != null && FallingBlock.isFree(var7)) {
            var6 = putAt_FileStore_BitCache_FileStore_BitCache(var6);
         }
      }

      if (!var6.climbable() && var4.getBlock() instanceof TrapDoorBlock && var2 - 1 >= this.fill) {
         BlockState var8 = this.putAt_BlockState(var1, var2 - 1, var3);
         if (AiPathfinder.Inner_StreamUtil.putAt_Z_BlockState_BlockState(var4, var8)) {
            var6 = fill(var6);
         }
      }

      return putAt(var6, AiEnvironmentHazards.init(this.putAt_ClientLevel, new BlockPos(var1, var2, var3)));
   }

   @Override
   public AiPathfinder.Inner_IoCodec putAt_FileStore_IoCodec_I(int var1, int var2, int var3, boolean var4) {
      if (this.putAt_ClientLevel.getChunkSource().getChunk(var1 >> 4, var3 >> 4, ChunkStatus.FULL, false) instanceof LevelChunk var6) {
         long var7 = 0L;
         int var9 = Math.max(var2, this.fill);
         int var10 = Math.min(var2 + 64 - 1, this.readEx);

         try {
            LevelChunkSection[] var11 = var6.getSections();
            int var12 = var1 & 15;
            int var13 = var3 & 15;
            int var14 = var9;

            while (var14 <= var10) {
               LevelChunkSection var15 = var11[var6.getSectionIndex(var14)];
               int var16 = Math.min(var10, var14 | 15);
               if (var15 != null && !var15.hasOnlyAir()) {
                  for (; var14 <= var16; var14++) {
                     BlockState var17 = var15.getBlockState(var12, var14 & 15, var13);
                     if (!var17.isAir()) {
                        AiLiveGrid.Inner_StreamUtil var18 = putAt_PoolStore_TableCac_BlockState(var17);
                        if (var18 != null && !var18.probe().passable()) {
                           var7 |= 1L << var14 - var2;
                        }
                     }
                  }
               }

               var14 = var16 + 1;
            }

            return new AiPathfinder.Inner_IoCodec(var7, false);
         } catch (RuntimeException var19) {
            return AiPathfinder.Inner_StateCache.super.putAt_FileStore_IoCodec_I(var1, var2, var3, var4);
         }
      } else {
         return !var4 && this.putAt_PacketHelper != null
            ? AiPathfinder.Inner_StateCache.super.putAt_FileStore_IoCodec_I(var1, var2, var3, false)
            : new AiPathfinder.Inner_IoCodec(0L, false);
      }
   }

   static AiPathfinder.Inner_FileCache putAt(AiPathfinder.Inner_FileCache var0, boolean var1) {
      return var1 && !var0.hazardFeet() ? var0.withHazards(true, var0.hazardFloor()) : var0;
   }

   static AiPathfinder.Inner_FileCache putAt_FileStore_BitCache_FileStore_BitCache(AiPathfinder.Inner_FileCache var0) {
      return new AiPathfinder.Inner_FileCache(
         var0.loaded(),
         var0.passable(),
         var0.top16(),
         var0.water(),
         var0.lava(),
         var0.climbable(),
         var0.hazardFeet(),
         true,
         var0.falling(),
         var0.fallMult10(),
         var0.breakTicks(),
         var0.door(),
         var0.slabStair(),
         var0.farmland(),
         var0.ice(),
         var0.cached(),
         var0.destructionPenalty(),
         var0.soulSand(),
         var0.thorny(),
         var0.portal(),
         false,
         var0.feetTrigger(),
         var0.pressurePlate()
      );
   }

   static AiPathfinder.Inner_FileCache fill(AiPathfinder.Inner_FileCache var0) {
      return new AiPathfinder.Inner_FileCache(
         var0.loaded(),
         true,
         0,
         var0.water(),
         var0.lava(),
         true,
         var0.hazardFeet(),
         var0.hazardFloor(),
         var0.falling(),
         var0.fallMult10(),
         var0.breakTicks(),
         var0.door(),
         var0.slabStair(),
         var0.farmland(),
         var0.ice(),
         var0.cached(),
         var0.destructionPenalty(),
         var0.soulSand(),
         var0.thorny(),
         var0.portal(),
         var0.narrowFooting(),
         var0.feetTrigger(),
         var0.pressurePlate()
      );
   }

   @Override
   public short putAt_S_I(int var1, int var2, int var3) {
      if (var2 < this.fill) {
         return 1;
      } else if (var2 > this.readEx) {
         return 0;
      } else {
         BlockState var4 = this.putAt_BlockState(var1, var2, var3);
         if (var4 == null) {
            return AiShapes.init_S_ClassCache_FileCac(this.putAt_FileStore_BitCache(var1, var2, var3));
         } else {
            AiLiveGrid.Inner_StreamUtil var5 = putAt_PoolStore_TableCac_BlockState(var4);
            if (var5 == null) {
               return 1;
            } else {
               return !var5.dynamicShape() ? var5.shape() : putAt(var4, new BlockPos(var1, var2, var3));
            }
         }
      }
   }

   @Override
   public byte putAt_B_I(int var1, int var2, int var3) {
      if (var2 >= this.fill && var2 <= this.readEx) {
         BlockState var4 = this.putAt_BlockState(var1, var2, var3);
         if (var4 == null) {
            return AiSurface.NORMAL.init();
         }

         AiLiveGrid.Inner_StreamUtil var5 = putAt_PoolStore_TableCac_BlockState(var4);
         return var5 == null ? AiSurface.NORMAL.init() : var5.surface();
      } else {
         return AiSurface.NORMAL.init();
      }
   }

   @Override
   public short fill_S(int var1, int var2, int var3) {
      if (var2 < this.fill) {
         return 1;
      }

      if (var2 > this.readEx) {
         return 0;
      }

      BlockState var4 = this.putAt_BlockState(var1, var2, var3);
      if (var4 == null) {
         return AiShapes.init_S_ClassCache_FileCac(this.putAt_FileStore_BitCache(var1, var2, var3));
      }

      AiLiveGrid.Inner_StreamUtil var5 = putAt_PoolStore_TableCac_BlockState(var4);
      if (var5 == null) {
         return 1;
      }

      if (!var5.dynamicShape()) {
         return var5.outlineShape();
      }

      try {
         return AiShapes.init_S_List(var4.getShape(EmptyBlockGetter.INSTANCE, new BlockPos(var1, var2, var3)).toAabbs());
      } catch (RuntimeException var7) {
         return 1;
      }
   }

   BlockState putAt_BlockState(int var1, int var2, int var3) {
      if (var2 >= this.fill && var2 <= this.readEx) {
         if (this.putAt_ClientLevel.getChunkSource().getChunk(var1 >> 4, var3 >> 4, ChunkStatus.FULL, false) instanceof LevelChunk var5) {
            try {
               LevelChunkSection[] var6 = var5.getSections();
               int var7 = var5.getSectionIndex(var2);
               if (var7 >= 0 && var7 < var6.length) {
                  LevelChunkSection var8 = var6[var7];
                  return var8 != null && !var8.hasOnlyAir() ? var8.getBlockState(var1 & 15, var2 & 15, var3 & 15) : Blocks.AIR.defaultBlockState();
               } else {
                  return null;
               }
            } catch (RuntimeException var9) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private AiPathfinder.Inner_FileCache putAt_FileStore_BitCache(int var1, int var2, int var3) {
      return this.putAt_PacketHelper == null ? AiPathfinder.Inner_FileCache.UNLOADED : this.putAt_PacketHelper.init_ClassCache_FileCac_I(var1, var2, var3);
   }

   private double putAt(BlockState var1, AiLiveGrid.Inner_StreamUtil var2, int var3, int var4, int var5) {
      AiLiveGrid.Inner_DataUtil var6 = this.putAt_PoolStore_CacheHel;
      if (var2.requiresTool()
         && !var6.putAt_ByteWorker_CacheHe.creative()
         && !var6.fill.computeIfAbsent(var1, var1x -> AiPathfinder.Inner_StreamUtil.putAt_Z_BlockState_ItemStackArr(var1x, var6.putAt_ItemStackArr))) {
         return 1.0 / 0.0;
      }

      if (!this.value(var3, var4, var5) && !this.prevEx(var3, var4, var5) && !this.fill_Z(var3, var4, var5)) {
         if (!this.putAt_Z || !var1.is(Blocks.NETHER_GOLD_ORE) && !var1.is(Blocks.GILDED_BLACKSTONE)) {
            if ((var1.is(Blocks.ICE) || var1.is(Blocks.FROSTED_ICE)) && !this.putAt_Z) {
               BlockState var7 = this.putAt_BlockState(var3, var4 - 1, var5);
               if (var7 == null || AiPathfinder.Inner_StreamUtil.putAt_Z_Z(true, var7.isSolid(), var7.liquid(), false)) {
                  return 1.0 / 0.0;
               }
            }

            if (!this.putAt_Z
               || !this.chunkAt(var3, var4 + 2, var5)
                  && !this.chunkAt(var3 + 1, var4 + 1, var5)
                  && !this.chunkAt(var3 - 1, var4 + 1, var5)
                  && !this.chunkAt(var3, var4 + 1, var5 + 1)
                  && !this.chunkAt(var3, var4 + 1, var5 - 1)) {
               double var13 = this.putAt(var6, var1, var2);
               int var9 = 0;
               int var10 = var4 + 1;

               while (var10 <= this.readEx) {
                  BlockState var11 = this.putAt_BlockState(var3, var10, var5);
                  if (var11 == null) {
                     return 1.0 / 0.0;
                  }

                  AiLiveGrid.Inner_StreamUtil var12 = putAt_PoolStore_TableCac_BlockState(var11);
                  if (var12 != null && var12.falling()) {
                     if (++var9 <= 6 && var12.diggableKind()) {
                        if (this.prevEx(var3, var10, var5)) {
                           return 1.0 / 0.0;
                        }

                        var13 += this.putAt(var6, var11, var12);
                        var10++;
                        continue;
                     }

                     return 1.0 / 0.0;
                  }
                  break;
               }

               return var13;
            } else {
               return 1.0 / 0.0;
            }
         } else {
            return 1.0 / 0.0;
         }
      } else {
         return 1.0 / 0.0;
      }
   }

   private double putAt(AiLiveGrid.Inner_DataUtil var1, BlockState var2, AiLiveGrid.Inner_StreamUtil var3) {
      return var1.putAt_ConcurrentHashMap
         .computeIfAbsent(
            var2,
            var3x -> var1.putAt_BiPredicate == null
               ? AiPathfinder.Inner_StreamUtil.putAt(this.putAt_Minecraft, var3x, var3.hardness(), var1.putAt_ItemStackArr, var1.putAt_ByteWorker_CacheHe)
               : AiPathfinder.Inner_StreamUtil.putAt(
                  this.putAt_Minecraft, var3x, var3.hardness(), var1.putAt_ItemStackArr, var1.putAt_ByteWorker_CacheHe, var1.putAt_BiPredicate
               )
         );
   }

   private boolean fill_Z(int var1, int var2, int var3) {
      return this.readEx(var1 + 1, var2, var3)
         || this.readEx(var1 - 1, var2, var3)
         || this.readEx(var1, var2 + 1, var3)
         || this.readEx(var1, var2 - 1, var3)
         || this.readEx(var1, var2, var3 + 1)
         || this.readEx(var1, var2, var3 - 1);
   }

   private boolean readEx(int var1, int var2, int var3) {
      if (var2 >= this.fill && var2 <= this.readEx) {
         BlockState var4 = this.putAt_BlockState(var1, var2, var3);
         return var4 != null && var4.is(Blocks.NETHER_PORTAL);
      } else {
         return false;
      }
   }

   private boolean value(int var1, int var2, int var3) {
      return this.chunkAt(var1 + 1, var2, var3)
         || this.chunkAt(var1 - 1, var2, var3)
         || this.chunkAt(var1, var2 + 1, var3)
         || this.chunkAt(var1, var2 - 1, var3)
         || this.chunkAt(var1, var2, var3 + 1)
         || this.chunkAt(var1, var2, var3 - 1);
   }

   private boolean prevEx(int var1, int var2, int var3) {
      return this.prev(var1 + 1, var2, var3)
         || this.prev(var1 - 1, var2, var3)
         || this.prev(var1, var2 + 1, var3)
         || this.prev(var1, var2, var3 + 1)
         || this.prev(var1, var2, var3 - 1);
   }

   private boolean prev(int var1, int var2, int var3) {
      if (var2 >= this.fill && var2 <= this.readEx) {
         BlockState var4 = this.putAt_BlockState(var1, var2, var3);
         return var4 != null && var4.getFluidState().is(FluidTags.WATER);
      } else {
         return false;
      }
   }

   private boolean chunkAt(int var1, int var2, int var3) {
      if (var2 >= this.fill && var2 <= this.readEx) {
         BlockState var4 = this.putAt_BlockState(var1, var2, var3);
         return var4 == null ? true : var4.getFluidState().is(FluidTags.LAVA);
      } else {
         return false;
      }
   }

   public boolean putAt_Z_2(int var1, int var2, int var3) {
      BlockState var4 = this.putAt_BlockState(var1, var2, var3);
      if (var4 == null) {
         return false;
      }

      AiLiveGrid.Inner_StreamUtil var5 = putAt_PoolStore_TableCac_BlockState(var4);
      return var5 != null && var5.falling();
   }

   static AiPathfinder.Inner_FileCache putAt(AiPathfinder.Inner_FileCache var0, double var1) {
      return new AiPathfinder.Inner_FileCache(
         var0.loaded(),
         var0.passable(),
         var0.top16(),
         var0.water(),
         var0.lava(),
         var0.climbable(),
         var0.hazardFeet(),
         var0.hazardFloor(),
         var0.falling(),
         var0.fallMult10(),
         var1,
         var0.door(),
         var0.slabStair(),
         var0.farmland(),
         var0.ice(),
         var0.cached(),
         var0.destructionPenalty(),
         var0.soulSand(),
         var0.thorny(),
         var0.portal(),
         var0.narrowFooting(),
         var0.feetTrigger(),
         var0.pressurePlate()
      );
   }

   static AiLiveGrid.Inner_StreamUtil putAt_PoolStore_TableCac_BlockState(BlockState var0) {
      int var1 = Block.getId(var0);
      if (var1 >= 0 && var1 < putAt_AtomicReferenceArr.length()) {
         AiLiveGrid.Inner_StreamUtil var2 = putAt_AtomicReferenceArr.get(var1);
         if (var2 != null) {
            return var2;
         }

         AiLiveGrid.Inner_StreamUtil var3 = fill(var0);
         if (var3 != null) {
            putAt_AtomicReferenceArr.compareAndSet(var1, null, var3);
         }

         return var3;
      } else {
         return fill(var0);
      }
   }

   private static AiLiveGrid.Inner_StreamUtil fill(BlockState var0) {
      try {
         if (var0.isAir()) {
            return new AiLiveGrid.Inner_StreamUtil(
               AiPathfinder.Inner_FileCache.air(), (short)0, (short)0, AiSurface.NORMAL.init(), 0.0F, false, false, false, false, false, false
            );
         }

         FluidState var1 = var0.getFluidState();
         boolean var2 = var1.is(FluidTags.WATER);
         boolean var3 = var1.is(FluidTags.LAVA);
         Block var4 = var0.getBlock();
         boolean var5 = var4.hasDynamicShape() || var0.hasOffsetFunction();
         VoxelShape var6 = var0.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         boolean var7 = var6.isEmpty();
         boolean var8 = false;
         if (var4 instanceof DoorBlock) {
            var8 = DoorBlock.isWoodenDoor(var0);
            if (var8 && var0.getValue(BlockStateProperties.OPEN)) {
               var7 = true;
            }
         } else if (var4 instanceof FenceGateBlock) {
            var8 = true;
            if (var0.getValue(BlockStateProperties.OPEN)) {
               var7 = true;
            }
         }

         boolean var9 = AiPathfinder.Inner_StreamUtil.putAt_Z_BlockState(var0);
         if (var9 && !(var4 instanceof ScaffoldingBlock)) {
            var7 = true;
         }

         int var10 = var7 ? 0 : (int)Math.round(var6.max(Axis.Y) * 16.0);
         boolean var11 = AiPathfinder.Inner_StreamUtil.fill(var0);
         boolean var12 = AiPathfinder.Inner_StreamUtil.readEx(var0);
         boolean var13 = AiPathfinder.Inner_StreamUtil.value(var0);
         boolean var14 = var0.is(Blocks.SWEET_BERRY_BUSH);
         boolean var15 = AiPathfinder.Inner_StreamUtil.prevEx(var0);
         boolean var16 = !var15 && !var7 && !AiPathfinder.Inner_StreamUtil.putAt_Z_VoxelShape(var6);
         boolean var17 = AiPathfinder.Inner_StreamUtil.prev(var0);
         String var18 = BuiltInRegistries.BLOCK.getKey(var4).getPath();
         int var19 = Math.round(AiPathfinder.Inner_StreamUtil.putAt_F_Str_Z(var18, var2) * 10.0F);
         boolean var20 = var4 instanceof FallingBlock;
         boolean var21 = var4 instanceof SlabBlock || var4 instanceof StairBlock;
         boolean var22 = var4 instanceof FarmlandBlock;
         boolean var23 = var0.is(Blocks.ICE) || var0.is(Blocks.PACKED_ICE) || var0.is(Blocks.BLUE_ICE) || var0.is(Blocks.FROSTED_ICE);
         boolean var24 = var0.is(Blocks.SOUL_SAND);
         float var25 = var0.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         boolean var26 = (!var7 || var4 instanceof WebBlock) && var1.isEmpty() && var25 >= 0.0F && !var0.hasBlockEntity();
         AiPathfinder.Inner_FileCache var27 = new AiPathfinder.Inner_FileCache(
            true,
            var7,
            var10,
            var2,
            var3,
            var9,
            var11,
            var15 || var16,
            var20,
            var19,
            1.0 / 0.0,
            var8,
            var21,
            var22,
            var23,
            false,
            AiPathfinder.Inner_CachePool.putAt_I_Str(var18),
            var24,
            var14,
            var17,
            var16,
            var12,
            var13
         );
         if (var7 && !var2 && !var3 && !var9 && !var11 && !var12 && !var13 && !var14 && !var17 && var10 == 0 && var19 == 10) {
            var27 = AiPathfinder.Inner_FileCache.air();
         }

         short var28 = var6.isEmpty() ? 0 : (var6 == Shapes.block() ? 1 : AiShapes.init_S_List(var6.toAabbs()));
         double var29 = var4 instanceof SlimeBlock ? 1.0 : (var4 instanceof BedBlock ? 0.75 : 0.0);
         byte var31 = AiSurface.init(var4.getFriction(), var4.getSpeedFactor(), var4.getJumpFactor(), var29).init();
         short var32 = AiShapes.init_S_List(var0.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs());
         return new AiLiveGrid.Inner_StreamUtil(
            var27,
            var28,
            var32,
            var31,
            var25,
            var5,
            var20,
            var26,
            var0.requiresCorrectToolForDrops(),
            AiPathfinder.Inner_StreamUtil.chunkAt(var0),
            AiChunkMemory.init(var18)
         );
      } catch (RuntimeException var33) {
         return null;
      }
   }

   private static short putAt(BlockState var0, BlockPos var1) {
      try {
         VoxelShape var2 = var0.getCollisionShape(EmptyBlockGetter.INSTANCE, var1);
         if (var2.isEmpty()) {
            return 0;
         } else {
            return var2 == Shapes.block() ? 1 : AiShapes.init_S_List(var2.toAabbs());
         }
      } catch (RuntimeException var3) {
         return 1;
      }
   }

   private static ItemStack[] putAt_ItemStackArr_ItemStackArr(ItemStack[] var0) {
      ItemStack[] var1 = new ItemStack[var0.length];

      for (int var2 = 0; var2 < var0.length; var2++) {
         var1[var2] = var0[var2] == null ? ItemStack.EMPTY : var0[var2].copy();
      }

      return var1;
   }

   static boolean putAt(ItemStack[] var0, ItemStack[] var1) {
      if (var0 != null && var1 != null && var0.length == var1.length) {
         for (int var2 = 0; var2 < var0.length; var2++) {
            ItemStack var3 = var0[var2] == null ? ItemStack.EMPTY : var0[var2];
            ItemStack var4 = var1[var2] == null ? ItemStack.EMPTY : var1[var2];
            if (!putAt_Z_ItemStack(var3)) {
               var3 = ItemStack.EMPTY;
            }

            if (!putAt_Z_ItemStack(var4)) {
               var4 = ItemStack.EMPTY;
            }

            if (!var3.isEmpty() && !var4.isEmpty()) {
               if (var3.getItem() != var4.getItem()) {
                  return false;
               }

               if (!Objects.equals(var3.getEnchantments(), var4.getEnchantments())) {
                  return false;
               }

               if (fill(var3) != fill(var4)) {
                  return false;
               }
            } else if (var3.isEmpty() != var4.isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean putAt_Z_ItemStack(ItemStack var0) {
      return !var0.isEmpty() && var0.has(DataComponents.TOOL);
   }

   private static boolean fill(ItemStack var0) {
      return var0.isDamageableItem() && !AiItems.init(var0, 2);
   }

   public static void putAt(Minecraft var0, AiChunkMemory var1) {
      if (var1 != null && var0.level != null && var0.player != null) {
         if (putAt_Obj != var0.level) {
            putAt_Map.clear();
            putAt_Obj = var0.level;
         }

         long var2 = System.nanoTime();
         int var4 = var0.player.getBlockX() >> 4;
         int var5 = var0.player.getBlockZ() >> 4;

         for (int var6 = 0; var6 <= 4; var6++) {
            for (int var7 = -var6; var7 <= var6; var7++) {
               for (int var8 = -var6; var8 <= var6; var8++) {
                  if (Math.max(Math.abs(var7), Math.abs(var8)) == var6) {
                     int var9 = var4 + var7;
                     int var10 = var5 + var8;
                     long var11 = AiGridRevisions.init_J(var9, var10);
                     Long var13 = putAt_Map.get(var11);
                     if ((var13 == null || var2 - var13 >= 30000000000L)
                        && var0.level.getChunkSource().getChunk(var9, var10, ChunkStatus.FULL, false) instanceof LevelChunk var14) {
                        putAt_Map.put(var11, var2);
                        if (putAt_Map.size() > 4096) {
                           putAt_Map.clear();
                        }

                        putAt(var0.level, var1, var14, var9, var10, var0.player.getBlockY());
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static void putAt(ClientLevel var0, AiChunkMemory var1, LevelChunk var2, int var3, int var4, int var5) {
      int var6 = Math.max(var0.getMinY(), var5 - 40);
      int var7 = Math.min(var0.getMaxY() - 1, var5 + 40);
      int var8 = var7 - var6 + 1;
      if (var8 > 0 && var8 <= 128) {
         short[] var9 = new short[256 * var8];
         HashMap var10 = null;
         MutableBlockPos var11 = new MutableBlockPos();

         for (int var12 = 0; var12 < 16; var12++) {
            for (int var13 = 0; var13 < 16; var13++) {
               for (int var14 = var6; var14 <= var7; var14++) {
                  var11.set((var3 << 4) + var12, var14, (var4 << 4) + var13);
                  BlockState var15 = var2.getBlockState(var11);
                  AiLiveGrid.Inner_StreamUtil var16 = putAt_PoolStore_TableCac_BlockState(var15);
                  if (var16 != null) {
                     AiPathfinder.Inner_FileCache var17 = var16.probe();
                     byte var18 = 1;
                     if (var17.passable()) {
                        var18 |= 2;
                     }

                     if (var17.water()) {
                        var18 |= 4;
                     }

                     if (var17.lava()) {
                        var18 |= 8;
                     }

                     if (var17.hazardFeet()) {
                        var18 |= 32;
                     }

                     if (var17.hazardFloor()) {
                        var18 |= 64;
                     }

                     var9[(var12 * 16 + var13) * var8 + (var14 - var6)] = var18;
                     if (var16.notable() && AiChunkMemory.init_I(var18) == 1) {
                        if (var10 == null) {
                           var10 = new HashMap();
                        }

                        var10.put((var14 - var6) * 256 + var13 * 16 + var12, "minecraft:" + BuiltInRegistries.BLOCK.getKey(var15.getBlock()).getPath());
                     }
                  }
               }
            }
         }

         var1.init(var3, var4, var6, var8, var9, var10);
      }
   }

   static final class Inner_DataUtil {
      final ItemStack[] putAt_ItemStackArr;
      final MiningCost.Inner_DataUtil putAt_ByteWorker_CacheHe;
      final long putAt_J;
      final BiPredicate<BlockState, ItemStack> putAt_BiPredicate;
      final ConcurrentHashMap<BlockState, Double> putAt_ConcurrentHashMap = new ConcurrentHashMap<>();
      final ConcurrentHashMap<BlockState, Boolean> fill = new ConcurrentHashMap<>();

      Inner_DataUtil(ItemStack[] var1, MiningCost.Inner_DataUtil var2, long var3) {
         this(var1, var2, var3, null);
      }

      Inner_DataUtil(ItemStack[] var1, MiningCost.Inner_DataUtil var2, long var3, BiPredicate<BlockState, ItemStack> var5) {
         this.putAt_ItemStackArr = var1;
         this.putAt_ByteWorker_CacheHe = var2;
         this.putAt_J = var3;
         this.putAt_BiPredicate = var5;
      }
   }

   record Inner_StreamUtil(
      AiPathfinder.Inner_FileCache probe,
      short shape,
      short outlineShape,
      byte surface,
      float hardness,
      boolean dynamicShape,
      boolean falling,
      boolean diggableKind,
      boolean requiresTool,
      boolean ore,
      boolean notable
   ) {
   }
}
