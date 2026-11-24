package autismclient.ai.a;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

final class BitSetUtil implements NodeWalker.Inner_CachePool {
   private final ClientLevel init_ClientLevel;
   private double init_D = 4.0;
   private final CollisionContext init_CollisionContext;
   private final BlockPos init_BlockPos;
   private final Entity init_Entity;
   private final Long2ObjectOpenHashMap<List<AABB>> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private final Long2ByteOpenHashMap init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap load = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap read = new Long2ByteOpenHashMap();
   private final Set<Long> init_Set = new HashSet<>();
   private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
   private final Long2ByteOpenHashMap write = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap update = new Long2ByteOpenHashMap();

   @Override
   public double init_D() {
      return this.init_D;
   }

   void init(double value) {
      this.init_D = value;
   }

   BitSetUtil(ClientLevel level, Entity body) {
      this(level, body, null);
   }

   BitSetUtil(ClientLevel level, Entity body, BlockPos removed) {
      this.init_ClientLevel = level;
      this.init_CollisionContext = body == null ? CollisionContext.empty() : CollisionContext.of(body);
      this.init_BlockPos = removed;
      this.init_Entity = body;
   }

   @Override
   public double load() {
      if (this.init_Entity instanceof LivingEntity living) {
         double var4 = living.getAttributeValue(Attributes.MOVEMENT_SPEED);
         return living.isSprinting() ? var4 / 1.3 : var4;
      } else {
         return 0.1;
      }
   }

   ClientLevel init_ClientLevel() {
      return this.init_ClientLevel;
   }

   void init_V() {
      this.init_Long2ObjectOpenHas.clear();
      this.init_Long2ByteOpenHashM.clear();
      this.load.clear();
      this.read.clear();
      this.write.clear();
      this.update.clear();
   }

   void init(BlockPos cell, boolean value) {
      if (value) {
         this.init_Set.add(cell.asLong());
      } else {
         this.init_Set.remove(cell.asLong());
      }

      this.init_Long2ObjectOpenHas.remove(cell.asLong());
   }

   @Override
   public boolean read_Z(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.read.get(key);
      if (known != 0) {
         return known == 2;
      }

      BlockState state = this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl.set(x, y, z));
      boolean unsafe = TextFormat.init(this.init_ClientLevel, this.init_BlockPos_MutableBl)
         || ClassCache.Inner_StreamUtil.load(state)
         || ClassCache.Inner_StreamUtil.read(state)
         || state.getBlock() instanceof SweetBerryBushBlock;
      this.read.put(key, (byte)(unsafe ? 2 : 1));
      return unsafe;
   }

   @Override
   public boolean write_Z(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.write.get(key);
      if (known != 0) {
         return known == 2;
      }

      BlockState state = this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl.set(x, y, z));
      boolean unsafe = TextFormat.init(this.init_ClientLevel, this.init_BlockPos_MutableBl)
         || ClassCache.Inner_StreamUtil.load(state)
         || state.getBlock() instanceof SweetBerryBushBlock
         || state.is(Blocks.CACTUS);
      this.write.put(key, (byte)(unsafe ? 2 : 1));
      return unsafe;
   }

   @Override
   public boolean update(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.update.get(key);
      if (known != 0) {
         return known == 2;
      }

      boolean unsafe = ClassCache.Inner_StreamUtil.read(this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl.set(x, y, z)));
      this.update.put(key, (byte)(unsafe ? 2 : 1));
      return unsafe;
   }

   @Override
   public boolean load_Z(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.load.get(key);
      if (known != 0) {
         return known == 2;
      }

      this.init_BlockPos_MutableBl.set(x, y, z);
      boolean present = this.init_ClientLevel.getFluidState(this.init_BlockPos_MutableBl).is(FluidTags.WATER);
      this.load.put(key, (byte)(present ? 2 : 1));
      return present;
   }

   @Override
   public boolean init_Z_I(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      byte known = this.init_Long2ByteOpenHashM.get(key);
      if (known != 0) {
         return known == 2;
      }

      this.init_BlockPos_MutableBl.set(x, y, z);
      boolean unsafe = y < this.init_ClientLevel.getMinY()
         || this.init_ClientLevel.getChunkSource().getChunk(x >> 4, z >> 4, ChunkStatus.FULL, false) == null
         || this.init_ClientLevel.getFluidState(this.init_BlockPos_MutableBl).is(FluidTags.LAVA);
      this.init_Long2ByteOpenHashM.put(key, (byte)(unsafe ? 2 : 1));
      return unsafe;
   }

   @Override
   public List<AABB> init_List(AABB q) {
      int x0 = (int)Math.floor(q.minX);
      int x1 = (int)Math.floor(q.maxX);
      int y0 = (int)Math.floor(q.minY) - 1;
      int y1 = (int)Math.floor(q.maxY);
      int z0 = (int)Math.floor(q.minZ);
      int z1 = (int)Math.floor(q.maxZ);
      List<AABB> out = new ArrayList<>();

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               for (AABB b : this.init_List(x, y, z)) {
                  if (b.intersects(q)) {
                     out.add(b);
                  }
               }
            }
         }
      }

      return out;
   }

   private List<AABB> init_List(int x, int y, int z) {
      if (this.init_BlockPos != null && this.init_BlockPos.getX() == x && this.init_BlockPos.getY() == y && this.init_BlockPos.getZ() == z) {
         return List.of();
      }

      long key = BlockPos.asLong(x, y, z);
      if (this.init_Set.contains(key)) {
         return List.of();
      }

      List<AABB> boxes = this.init_Long2ObjectOpenHas.get(key);
      if (boxes != null) {
         return boxes;
      }

      this.init_BlockPos_MutableBl.set(x, y, z);
      if (!this.init_ClientLevel.isInsideBuildHeight(this.init_BlockPos_MutableBl)) {
         boxes = y < this.init_ClientLevel.getMinY() ? List.of(new AABB(x, y, z, x + 1, y + 1, z + 1)) : List.of();
      } else {
         BlockState state = this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl);
         VoxelShape shape = state.getCollisionShape(this.init_ClientLevel, this.init_BlockPos_MutableBl, this.init_CollisionContext);
         if (shape.isEmpty()) {
            boxes = List.of();
         } else {
            boxes = new ArrayList<>(2);

            for (AABB b : shape.toAabbs()) {
               boxes.add(b.move(x, y, z));
            }
         }
      }

      this.init_Long2ObjectOpenHas.put(key, boxes);
      return boxes;
   }

   private Block init_Block(int x, int y, int z) {
      if (this.init_BlockPos != null && this.init_BlockPos.getX() == x && this.init_BlockPos.getY() == y && this.init_BlockPos.getZ() == z) {
         return Blocks.AIR;
      }

      this.init_BlockPos_MutableBl.set(x, y, z);
      return this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl).getBlock();
   }

   @Override
   public double init_D_I(int x, int y, int z) {
      return this.init_Block(x, y, z).getFriction();
   }

   @Override
   public double load_D(int x, int y, int z) {
      return this.init_Block(x, y, z).getSpeedFactor();
   }

   @Override
   public double write_D(int x, int y, int z) {
      return this.init_Block(x, y, z).getJumpFactor();
   }

   @Override
   public boolean sync(int x, int y, int z) {
      return this.init_ClientLevel.getBlockState(this.init_BlockPos_MutableBl.set(x, y, z)).is(BlockTags.CLIMBABLE);
   }

   @Override
   public double read_D(int x, int y, int z) {
      Block b = this.init_Block(x, y, z);
      return b instanceof SlimeBlock ? 1.0 : (b instanceof BedBlock ? 0.75 : 0.0);
   }

   @Override
   public boolean init_Z() {
      return true;
   }

   @Override
   public int init_I(AABB box) {
      int hold = 0;

      for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); x++) {
         for (int y = (int)Math.floor(box.minY); y <= (int)Math.floor(box.maxY); y++) {
            for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); z++) {
               Block b = this.init_Block(x, y, z);
               if (b instanceof WebBlock) {
                  return 1;
               }

               if (b instanceof SweetBerryBushBlock) {
                  hold = 2;
               }
            }
         }
      }

      return hold;
   }
}
