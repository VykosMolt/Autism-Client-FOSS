package autismclient.ai.a;

import autismclient.ai.af;
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
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class RingBuffer implements ClassCache.Inner_StateCache {
   static final int init_I = 6;
   private final Minecraft init_Minecraft;
   private final boolean init_Z;
   private final ClientLevel init_ClientLevel;
   private final StateCache init_StateCache;
   private final int load;
   private final int read;
   private volatile RingBuffer.Inner_DataUtil init_RingBuffer_DataUti;
   private static final AtomicReferenceArray<RingBuffer.Inner_StreamUtil> init_AtomicReferenceArr = new AtomicReferenceArray<>(
      Math.max(1, Block.BLOCK_STATE_REGISTRY.size())
   );
   private static final long init_J = 30000000000L;
   private static final int write = 4;
   private static final int update = 40;
   private static final Map<Long, Long> init_Map = new HashMap<>();
   private static Object init_Obj;

   public RingBuffer(Minecraft mc, ItemStack[] tools, StateCache memory) {
      this.init_Minecraft = mc;
      this.init_ClientLevel = mc.level;
      this.init_StateCache = memory;
      this.load = this.init_ClientLevel.getMinY();
      this.read = this.init_ClientLevel.getMaxY() - 1;
      this.init_Z = this.init_ClientLevel.dimension() == Level.NETHER;
      this.init_RingBuffer_DataUti = new RingBuffer.Inner_DataUtil(init_ItemStackArr_ItemStackArr(tools), r.Inner_DataUtil.of(mc), 0L);
   }

   public ClientLevel init_ClientLevel() {
      return this.init_ClientLevel;
   }

   public void init_V_ItemStackArr_2(ItemStack[] snapshot) {
      RingBuffer.Inner_DataUtil old = this.init_RingBuffer_DataUti;
      r.Inner_DataUtil factors = r.Inner_DataUtil.of(this.init_Minecraft);
      if (!init(old.init_ItemStackArr, snapshot) || !factors.equals(old.init_r_DataUtil)) {
         this.init_RingBuffer_DataUti = new RingBuffer.Inner_DataUtil(init_ItemStackArr_ItemStackArr(snapshot), factors, old.init_J + 1L, old.init_BiPredicate);
      }
   }

   public void init_V_BiPredicate_2(BiPredicate<BlockState, ItemStack> accepted) {
      RingBuffer.Inner_DataUtil old = this.init_RingBuffer_DataUti;
      if (old.init_BiPredicate != accepted) {
         this.init_RingBuffer_DataUti = new RingBuffer.Inner_DataUtil(old.init_ItemStackArr, old.init_r_DataUtil, old.init_J + 1L, accepted);
      }
   }

   @Override
   public long init_J() {
      return this.init_RingBuffer_DataUti.init_J;
   }

   @Override
   public ClassCache.Inner_FileCache init_ClassCache_FileCac_I(int x, int y, int z) {
      if (y < this.load) {
         return ClassCache.Inner_FileCache.UNLOADED;
      }

      if (y > this.read) {
         return ClassCache.Inner_FileCache.air();
      }

      BlockState state = this.init_BlockState(x, y, z);
      if (state == null) {
         return this.load_ClassCache_FileCac(x, y, z);
      }

      RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
      if (info == null) {
         return ClassCache.Inner_FileCache.UNLOADED;
      }

      ClassCache.Inner_FileCache probe = !info.diggableKind() ? info.probe() : init(info.probe(), this.init(state, info, x, y, z));
      if (info.falling() && y - 1 >= this.load) {
         BlockState below = this.init_BlockState(x, y - 1, z);
         if (below != null && FallingBlock.isFree(below)) {
            probe = init_ClassCache_FileCac_ClassCache_FileCac(probe);
         }
      }

      return init(probe, TextFormat.init(this.init_ClientLevel, new BlockPos(x, y, z)));
   }

   static ClassCache.Inner_FileCache init(ClassCache.Inner_FileCache probe, boolean sulfur) {
      return sulfur && !probe.hazardFeet() ? probe.withHazards(true, probe.hazardFloor()) : probe;
   }

   static ClassCache.Inner_FileCache init_ClassCache_FileCac_ClassCache_FileCac(ClassCache.Inner_FileCache p) {
      return new ClassCache.Inner_FileCache(
         p.loaded(),
         p.passable(),
         p.top16(),
         p.water(),
         p.lava(),
         p.climbable(),
         p.hazardFeet(),
         true,
         p.falling(),
         p.fallMult10(),
         p.breakTicks(),
         p.door(),
         p.slabStair(),
         p.farmland(),
         p.ice(),
         p.cached(),
         p.destructionPenalty(),
         p.soulSand(),
         p.thorny(),
         p.portal()
      );
   }

   @Override
   public short init_S_I(int x, int y, int z) {
      if (y < this.load) {
         return 1;
      } else if (y > this.read) {
         return 0;
      } else {
         BlockState state = this.init_BlockState(x, y, z);
         if (state == null) {
            return e.init_S_ClassCache_FileCac(this.load_ClassCache_FileCac(x, y, z));
         } else {
            RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
            if (info == null) {
               return 1;
            } else {
               return !info.dynamicShape() ? info.shape() : init(state, new BlockPos(x, y, z));
            }
         }
      }
   }

   @Override
   public byte init_B_I(int x, int y, int z) {
      if (y >= this.load && y <= this.read) {
         BlockState state = this.init_BlockState(x, y, z);
         if (state == null) {
            return m.init_m.init();
         }

         RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
         return info == null ? m.init_m.init() : info.surface();
      } else {
         return m.init_m.init();
      }
   }

   @Override
   public short load_S(int x, int y, int z) {
      if (y < this.load) {
         return 1;
      }

      if (y > this.read) {
         return 0;
      }

      BlockState state = this.init_BlockState(x, y, z);
      if (state == null) {
         return e.init_S_ClassCache_FileCac(this.load_ClassCache_FileCac(x, y, z));
      }

      RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
      if (info == null) {
         return 1;
      }

      if (!info.dynamicShape()) {
         return info.outlineShape();
      }

      try {
         return e.init_S_List(state.getShape(EmptyBlockGetter.INSTANCE, new BlockPos(x, y, z)).toAabbs());
      } catch (RuntimeException unexpected) {
         return 1;
      }
   }

   BlockState init_BlockState(int x, int y, int z) {
      if (y >= this.load && y <= this.read) {
         if (this.init_ClientLevel.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
            try {
               LevelChunkSection[] sections = chunk.getSections();
               int index = chunk.getSectionIndex(y);
               if (index >= 0 && index < sections.length) {
                  LevelChunkSection section = sections[index];
                  return section != null && !section.hasOnlyAir() ? section.getBlockState(x & 15, y & 15, z & 15) : Blocks.AIR.defaultBlockState();
               } else {
                  return null;
               }
            } catch (RuntimeException raced) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private ClassCache.Inner_FileCache load_ClassCache_FileCac(int x, int y, int z) {
      return this.init_StateCache == null ? ClassCache.Inner_FileCache.UNLOADED : this.init_StateCache.init_ClassCache_FileCac_I(x, y, z);
   }

   private double init(BlockState state, RingBuffer.Inner_StreamUtil info, int x, int y, int z) {
      RingBuffer.Inner_DataUtil p = this.init_RingBuffer_DataUti;
      if (info.requiresTool()
         && !p.init_r_DataUtil.creative()
         && !p.load.computeIfAbsent(state, s -> ClassCache.Inner_StreamUtil.init_Z_BlockState_ItemStackArr(s, p.init_ItemStackArr))) {
         return 1.0 / 0.0;
      }

      if (!this.write(x, y, z) && !this.update(x, y, z) && !this.load_Z(x, y, z)) {
         if (!this.init_Z || !state.is(Blocks.NETHER_GOLD_ORE) && !state.is(Blocks.GILDED_BLACKSTONE)) {
            if ((state.is(Blocks.ICE) || state.is(Blocks.FROSTED_ICE)) && !this.init_Z) {
               BlockState under = this.init_BlockState(x, y - 1, z);
               if (under == null || ClassCache.Inner_StreamUtil.init_Z_Z(true, under.isSolid(), under.liquid(), false)) {
                  return 1.0 / 0.0;
               }
            }

            if (!this.init_Z
               || !this.flush(x, y + 2, z)
                  && !this.flush(x + 1, y + 1, z)
                  && !this.flush(x - 1, y + 1, z)
                  && !this.flush(x, y + 1, z + 1)
                  && !this.flush(x, y + 1, z - 1)) {
               double ticks = this.init(p, state, info);
               int stack = 0;
               int yy = y + 1;

               while (yy <= this.read) {
                  BlockState above = this.init_BlockState(x, yy, z);
                  if (above == null) {
                     return 1.0 / 0.0;
                  }

                  RingBuffer.Inner_StreamUtil aboveInfo = init_RingBuffer_StreamU_BlockState(above);
                  if (aboveInfo != null && aboveInfo.falling()) {
                     if (++stack <= 6 && aboveInfo.diggableKind()) {
                        if (this.update(x, yy, z)) {
                           return 1.0 / 0.0;
                        }

                        ticks += this.init(p, above, aboveInfo);
                        yy++;
                        continue;
                     }

                     return 1.0 / 0.0;
                  }
                  break;
               }

               return ticks;
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

   private double init(RingBuffer.Inner_DataUtil p, BlockState state, RingBuffer.Inner_StreamUtil info) {
      return p.init_ConcurrentHashMap
         .computeIfAbsent(
            state,
            s -> p.init_BiPredicate == null
               ? ClassCache.Inner_StreamUtil.init(this.init_Minecraft, s, info.hardness(), p.init_ItemStackArr, p.init_r_DataUtil)
               : ClassCache.Inner_StreamUtil.init(this.init_Minecraft, s, info.hardness(), p.init_ItemStackArr, p.init_r_DataUtil, p.init_BiPredicate)
         );
   }

   private boolean load_Z(int x, int y, int z) {
      return this.read(x + 1, y, z)
         || this.read(x - 1, y, z)
         || this.read(x, y + 1, z)
         || this.read(x, y - 1, z)
         || this.read(x, y, z + 1)
         || this.read(x, y, z - 1);
   }

   private boolean read(int x, int y, int z) {
      if (y >= this.load && y <= this.read) {
         BlockState state = this.init_BlockState(x, y, z);
         return state != null && state.is(Blocks.NETHER_PORTAL);
      } else {
         return false;
      }
   }

   private boolean write(int x, int y, int z) {
      return this.flush(x + 1, y, z)
         || this.flush(x - 1, y, z)
         || this.flush(x, y + 1, z)
         || this.flush(x, y - 1, z)
         || this.flush(x, y, z + 1)
         || this.flush(x, y, z - 1);
   }

   private boolean update(int x, int y, int z) {
      return this.sync(x + 1, y, z) || this.sync(x - 1, y, z) || this.sync(x, y + 1, z) || this.sync(x, y, z + 1) || this.sync(x, y, z - 1);
   }

   private boolean sync(int x, int y, int z) {
      if (y >= this.load && y <= this.read) {
         BlockState state = this.init_BlockState(x, y, z);
         return state != null && state.getFluidState().is(FluidTags.WATER);
      } else {
         return false;
      }
   }

   private boolean flush(int x, int y, int z) {
      if (y >= this.load && y <= this.read) {
         BlockState state = this.init_BlockState(x, y, z);
         return state == null ? true : state.getFluidState().is(FluidTags.LAVA);
      } else {
         return false;
      }
   }

   public boolean init_Z(int x, int y, int z) {
      BlockState state = this.init_BlockState(x, y, z);
      if (state == null) {
         return false;
      }

      RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
      return info != null && info.falling();
   }

   static ClassCache.Inner_FileCache init(ClassCache.Inner_FileCache p, double ticks) {
      return new ClassCache.Inner_FileCache(
         p.loaded(),
         p.passable(),
         p.top16(),
         p.water(),
         p.lava(),
         p.climbable(),
         p.hazardFeet(),
         p.hazardFloor(),
         p.falling(),
         p.fallMult10(),
         ticks,
         p.door(),
         p.slabStair(),
         p.farmland(),
         p.ice(),
         p.cached(),
         p.destructionPenalty(),
         p.soulSand(),
         p.thorny(),
         p.portal(),
         p.narrowFooting()
      );
   }

   static RingBuffer.Inner_StreamUtil init_RingBuffer_StreamU_BlockState(BlockState state) {
      int id = Block.getId(state);
      if (id >= 0 && id < init_AtomicReferenceArr.length()) {
         RingBuffer.Inner_StreamUtil known = init_AtomicReferenceArr.get(id);
         if (known != null) {
            return known;
         }

         RingBuffer.Inner_StreamUtil made = load(state);
         if (made != null) {
            init_AtomicReferenceArr.compareAndSet(id, null, made);
         }

         return made;
      } else {
         return load(state);
      }
   }

   private static RingBuffer.Inner_StreamUtil load(BlockState state) {
      try {
         if (state.isAir()) {
            return new RingBuffer.Inner_StreamUtil(
               ClassCache.Inner_FileCache.air(), (short)0, (short)0, m.init_m.init(), 0.0F, false, false, false, false, false, false
            );
         }

         FluidState fluid = state.getFluidState();
         boolean water = fluid.is(FluidTags.WATER);
         boolean lava = fluid.is(FluidTags.LAVA);
         Block block = state.getBlock();
         boolean dynamic = block.hasDynamicShape() || state.hasOffsetFunction();
         VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         boolean passable = shape.isEmpty();
         boolean door = false;
         if (block instanceof DoorBlock) {
            door = DoorBlock.isWoodenDoor(state);
            if (door && state.getValue(BlockStateProperties.OPEN)) {
               passable = true;
            }
         } else if (block instanceof FenceGateBlock) {
            door = true;
            if (state.getValue(BlockStateProperties.OPEN)) {
               passable = true;
            }
         }

         boolean climb = ClassCache.Inner_StreamUtil.init_Z_BlockState(state);
         if (climb && !(block instanceof ScaffoldingBlock)) {
            passable = true;
         }

         int top16 = passable ? 0 : (int)Math.round(shape.max(Axis.Y) * 16.0);
         boolean hazardFeet = ClassCache.Inner_StreamUtil.load(state);
         boolean thorny = state.is(Blocks.SWEET_BERRY_BUSH);
         boolean contactHazard = ClassCache.Inner_StreamUtil.read(state);
         boolean narrowFooting = !contactHazard && !passable && !ClassCache.Inner_StreamUtil.init_Z_VoxelShape(shape);
         boolean portal = ClassCache.Inner_StreamUtil.write(state);
         String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
         int mult10 = Math.round(ClassCache.Inner_StreamUtil.init_F_Str_Z(path, water) * 10.0F);
         boolean falling = block instanceof FallingBlock;
         boolean slabStair = block instanceof SlabBlock || block instanceof StairBlock;
         boolean farmland = block instanceof FarmlandBlock;
         boolean ice = state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) || state.is(Blocks.BLUE_ICE) || state.is(Blocks.FROSTED_ICE);
         boolean soulSand = state.is(Blocks.SOUL_SAND);
         float hardness = state.getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
         boolean diggableKind = (!passable || block instanceof WebBlock) && fluid.isEmpty() && hardness >= 0.0F && !state.hasBlockEntity();
         ClassCache.Inner_FileCache probe = new ClassCache.Inner_FileCache(
            true,
            passable,
            top16,
            water,
            lava,
            climb,
            hazardFeet,
            contactHazard || narrowFooting,
            falling,
            mult10,
            1.0 / 0.0,
            door,
            slabStair,
            farmland,
            ice,
            false,
            ClassCache.Inner_CachePool.init_I_Str(path),
            soulSand,
            thorny,
            portal,
            narrowFooting
         );
         if (passable && !water && !lava && !climb && !hazardFeet && !thorny && !portal && top16 == 0 && mult10 == 10) {
            probe = ClassCache.Inner_FileCache.air();
         }

         short shapeId = !shape.isEmpty() && (!climb || block instanceof ScaffoldingBlock) ? (shape == Shapes.block() ? 1 : e.init_S_List(shape.toAabbs())) : 0;
         double bounce = block instanceof SlimeBlock ? 1.0 : (block instanceof BedBlock ? 0.75 : 0.0);
         byte surface = m.init(block.getFriction(), block.getSpeedFactor(), block.getJumpFactor(), bounce).init();
         short outlineShape = e.init_S_List(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).toAabbs());
         return new RingBuffer.Inner_StreamUtil(
            probe,
            shapeId,
            outlineShape,
            surface,
            hardness,
            dynamic,
            falling,
            diggableKind,
            state.requiresCorrectToolForDrops(),
            ClassCache.Inner_StreamUtil.update(state),
            StateCache.init(path)
         );
      } catch (RuntimeException unexpected) {
         return null;
      }
   }

   private static short init(BlockState state, BlockPos pos) {
      try {
         VoxelShape shape = state.getCollisionShape(EmptyBlockGetter.INSTANCE, pos);
         if (shape.isEmpty()) {
            return 0;
         } else if (ClassCache.Inner_StreamUtil.init_Z_BlockState(state) && !(state.getBlock() instanceof ScaffoldingBlock)) {
            return 0;
         } else {
            return shape == Shapes.block() ? 1 : e.init_S_List(shape.toAabbs());
         }
      } catch (RuntimeException unexpected) {
         return 1;
      }
   }

   private static ItemStack[] init_ItemStackArr_ItemStackArr(ItemStack[] tools) {
      ItemStack[] out = new ItemStack[tools.length];

      for (int i = 0; i < tools.length; i++) {
         out[i] = tools[i] == null ? ItemStack.EMPTY : tools[i].copy();
      }

      return out;
   }

   static boolean init(ItemStack[] a, ItemStack[] b) {
      if (a != null && b != null && a.length == b.length) {
         for (int i = 0; i < a.length; i++) {
            ItemStack x = a[i] == null ? ItemStack.EMPTY : a[i];
            ItemStack y = b[i] == null ? ItemStack.EMPTY : b[i];
            if (!init_Z_ItemStack(x)) {
               x = ItemStack.EMPTY;
            }

            if (!init_Z_ItemStack(y)) {
               y = ItemStack.EMPTY;
            }

            if (!x.isEmpty() && !y.isEmpty()) {
               if (x.getItem() != y.getItem()) {
                  return false;
               }

               if (!Objects.equals(x.getEnchantments(), y.getEnchantments())) {
                  return false;
               }

               if (load(x) != load(y)) {
                  return false;
               }
            } else if (x.isEmpty() != y.isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean init_Z_ItemStack(ItemStack s) {
      return !s.isEmpty() && s.has(DataComponents.TOOL);
   }

   private static boolean load(ItemStack s) {
      return s.isDamageableItem() && !af.init(s, 2);
   }

   public static void init(Minecraft mc, StateCache memory) {
      if (memory != null && mc.level != null && mc.player != null) {
         if (init_Obj != mc.level) {
            init_Map.clear();
            init_Obj = mc.level;
         }

         long now = System.nanoTime();
         int pcx = mc.player.getBlockX() >> 4;
         int pcz = mc.player.getBlockZ() >> 4;

         for (int r = 0; r <= 4; r++) {
            for (int dx = -r; dx <= r; dx++) {
               for (int dz = -r; dz <= r; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                     int cx = pcx + dx;
                     int cz = pcz + dz;
                     long key = IoWorker.init_J(cx, cz);
                     Long last = init_Map.get(key);
                     if ((last == null || now - last >= 30000000000L)
                        && mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
                        init_Map.put(key, now);
                        if (init_Map.size() > 4096) {
                           init_Map.clear();
                        }

                        init(mc.level, memory, chunk, cx, cz, mc.player.getBlockY());
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   private static void init(ClientLevel level, StateCache memory, LevelChunk chunk, int cx, int cz, int centreY) {
      int yLo = Math.max(level.getMinY(), centreY - 40);
      int yHi = Math.min(level.getMaxY() - 1, centreY + 40);
      int height = yHi - yLo + 1;
      if (height > 0 && height <= 128) {
         short[] flags = new short[256 * height];
         Map<Integer, String> notables = null;
         MutableBlockPos p = new MutableBlockPos();

         for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
               for (int y = yLo; y <= yHi; y++) {
                  p.set((cx << 4) + x, y, (cz << 4) + z);
                  BlockState state = chunk.getBlockState(p);
                  RingBuffer.Inner_StreamUtil info = init_RingBuffer_StreamU_BlockState(state);
                  if (info != null) {
                     ClassCache.Inner_FileCache probe = info.probe();
                     int f = 1;
                     if (probe.passable()) {
                        f |= 2;
                     }

                     if (probe.water()) {
                        f |= 4;
                     }

                     if (probe.lava()) {
                        f |= 8;
                     }

                     if (probe.hazardFeet()) {
                        f |= 32;
                     }

                     if (probe.hazardFloor()) {
                        f |= 64;
                     }

                     flags[(x * 16 + z) * height + (y - yLo)] = (short)f;
                     if (info.notable() && StateCache.init_I(f) == 1) {
                        if (notables == null) {
                           notables = new HashMap<>();
                        }

                        notables.put((y - yLo) * 256 + z * 16 + x, "minecraft:" + BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
                     }
                  }
               }
            }
         }

         memory.init(cx, cz, yLo, height, flags, notables);
      }
   }

   static final class Inner_DataUtil {
      final ItemStack[] init_ItemStackArr;
      final r.Inner_DataUtil init_r_DataUtil;
      final long init_J;
      final BiPredicate<BlockState, ItemStack> init_BiPredicate;
      final ConcurrentHashMap<BlockState, Double> init_ConcurrentHashMap = new ConcurrentHashMap<>();
      final ConcurrentHashMap<BlockState, Boolean> load = new ConcurrentHashMap<>();

      Inner_DataUtil(ItemStack[] tools, r.Inner_DataUtil factors, long revision) {
         this(tools, factors, revision, null);
      }

      Inner_DataUtil(ItemStack[] tools, r.Inner_DataUtil factors, long revision, BiPredicate<BlockState, ItemStack> accepted) {
         this.init_ItemStackArr = tools;
         this.init_r_DataUtil = factors;
         this.init_J = revision;
         this.init_BiPredicate = accepted;
      }
   }

   record Inner_StreamUtil(
      ClassCache.Inner_FileCache probe,
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
