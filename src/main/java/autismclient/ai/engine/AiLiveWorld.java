package autismclient.ai.engine;

import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiLiveWorld implements AiPhysics.PhysicsWorld {
   private final ClientLevel clientLevel;
   private double putAt_D = 4.0;
   private final CollisionContext putAt_CollisionContext;
   private final BlockPos ignoredCollisionPos;
   private final Entity putAt_Entity;
   private final Long2ObjectOpenHashMap<List<AABB>> collisionCache = new Long2ObjectOpenHashMap<>();
   private final Long2ByteOpenHashMap voidOrLavaCache = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap fill_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap hazardCache = new Long2ByteOpenHashMap();
   private final Set<Long> passableCells = new HashSet<>();
   private final MutableBlockPos putAt_BlockPos_MutableBl = new MutableBlockPos();
   private static final Identifier putAt_Identifier = Identifier.withDefaultNamespace("sprinting");
   private static final String putAt_Str = "662a6b8d-da3e-4c1c-8813-96ea6097278d";
   private static volatile double lastMovementSpeed = 0.1;
   private static volatile Object lastSpeedEntity;
   private final Long2ByteOpenHashMap contactHazardCache = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap floorHazardCache = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap lightBlockCache = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap pressurePlateCache = new Long2ByteOpenHashMap();

   @Override
   public double putAt_D() {
      return this.putAt_D;
   }

   void putAt_V_D(double var1) {
      this.putAt_D = var1;
   }

   AiLiveWorld(ClientLevel var1, Entity var2) {
      this(var1, var2, null);
   }

   AiLiveWorld(ClientLevel var1, Entity var2, BlockPos var3) {
      this.clientLevel = var1;
      this.putAt_CollisionContext = var2 == null ? CollisionContext.empty() : CollisionContext.of(var2);
      this.ignoredCollisionPos = var3;
      this.putAt_Entity = var2;
   }

   @Override
   public double movementSpeed() {
      return this.putAt_Entity instanceof LivingEntity var1 ? putAt_D_LivingEntity(var1) : 0.1;
   }

   static boolean putAt_Z_AttributeModifier(AttributeModifier var0) {
      if (var0 == null) {
         return false;
      }

      Identifier var1 = var0.id();
      return putAt_Identifier.equals(var1) || "662a6b8d-da3e-4c1c-8813-96ea6097278d".equalsIgnoreCase(var1.getPath());
   }

   static double putAt_D_D_Collection(double var0, Collection<AttributeModifier> var2) {
      double var3 = var0;

      for (AttributeModifier var6 : var2) {
         if (!putAt_Z_AttributeModifier(var6) && var6.operation() == Operation.ADD_VALUE) {
            var3 += var6.amount();
         }
      }

      double var9 = var3;

      for (AttributeModifier var8 : var2) {
         if (!putAt_Z_AttributeModifier(var8) && var8.operation() == Operation.ADD_MULTIPLIED_BASE) {
            var9 += var3 * var8.amount();
         }
      }

      for (AttributeModifier var11 : var2) {
         if (!putAt_Z_AttributeModifier(var11) && var11.operation() == Operation.ADD_MULTIPLIED_TOTAL) {
            var9 *= 1.0 + var11.amount();
         }
      }

      return var9;
   }

   static double putAt_D_LivingEntity(LivingEntity var0) {
      if (!Minecraft.getInstance().isSameThread()) {
         Object var4 = lastSpeedEntity;
         return var4 == var0 ? lastMovementSpeed : 0.1;
      }

      AttributeInstance var1 = var0.getAttribute(Attributes.MOVEMENT_SPEED);
      double var2;
      if (var1 != null) {
         var2 = var1.getAttribute().value().sanitizeValue(putAt_D_D_Collection(var1.getBaseValue(), var1.getModifiers()));
      } else {
         var2 = var0.getAttributeValue(Attributes.MOVEMENT_SPEED);
         var2 = var0.isSprinting() ? var2 / 1.3 : var2;
      }

      lastMovementSpeed = var2;
      lastSpeedEntity = var0;
      return var2;
   }

   ClientLevel clientLevel() {
      return this.clientLevel;
   }

   void resetCaches() {
      this.collisionCache.clear();
      this.voidOrLavaCache.clear();
      this.fill_Long2ByteOpenHashM.clear();
      this.hazardCache.clear();
      this.contactHazardCache.clear();
      this.floorHazardCache.clear();
      this.lightBlockCache.clear();
      this.pressurePlateCache.clear();
   }

   void putAt_V_BlockPos_Z(BlockPos var1, boolean var2) {
      if (var2) {
         this.passableCells.add(var1.asLong());
      } else {
         this.passableCells.remove(var1.asLong());
      }

      this.collisionCache.remove(var1.asLong());
   }

   @Override
   public boolean prevEx(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.hazardCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      BlockState var7 = this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3));
      boolean var8 = AiEnvironmentHazards.init(this.clientLevel, this.putAt_BlockPos_MutableBl)
         || AiPathfinder.LiveGridView.isFeetHazard(var7)
         || AiPathfinder.LiveGridView.isFloorHazard(var7)
         || var7.getBlock() instanceof SweetBerryBushBlock;
      this.hazardCache.put(var4, (byte)(var8 ? 2 : 1));
      return var8;
   }

   @Override
   public boolean prev(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.contactHazardCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      BlockState var7 = this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3));
      boolean var8 = AiEnvironmentHazards.init(this.clientLevel, this.putAt_BlockPos_MutableBl)
         || AiPathfinder.LiveGridView.isFeetHazard(var7)
         || var7.getBlock() instanceof SweetBerryBushBlock
         || var7.is(Blocks.CACTUS);
      this.contactHazardCache.put(var4, (byte)(var8 ? 2 : 1));
      return var8;
   }

   @Override
   public boolean chunkAt(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.floorHazardCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      boolean var7 = AiPathfinder.LiveGridView.isFloorHazard(this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3)));
      this.floorHazardCache.put(var4, (byte)(var7 ? 2 : 1));
      return var7;
   }

   @Override
   public boolean readEx_Z(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.lightBlockCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      boolean var7 = AiPathfinder.LiveGridView.isInvisibleLightBlock(this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3)));
      this.lightBlockCache.put(var4, (byte)(var7 ? 2 : 1));
      return var7;
   }

   @Override
   public boolean value_Z(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.pressurePlateCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      boolean var7 = AiPathfinder.LiveGridView.isPressurePlate(this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3)));
      this.pressurePlateCache.put(var4, (byte)(var7 ? 2 : 1));
      return var7;
   }

   @Override
   public boolean fill_Z(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.fill_Long2ByteOpenHashM.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      this.putAt_BlockPos_MutableBl.set(var1, var2, var3);
      boolean var7 = this.clientLevel.getFluidState(this.putAt_BlockPos_MutableBl).is(FluidTags.WATER);
      this.fill_Long2ByteOpenHashM.put(var4, (byte)(var7 ? 2 : 1));
      return var7;
   }

   @Override
   public boolean putAt_Z_I_2(int var1, int var2, int var3) {
      long var4 = BlockPos.asLong(var1, var2, var3);
      byte var6 = this.voidOrLavaCache.get(var4);
      if (var6 != 0) {
         return var6 == 2;
      }

      this.putAt_BlockPos_MutableBl.set(var1, var2, var3);
      boolean var7 = var2 < this.clientLevel.getMinY()
         || this.clientLevel.getChunkSource().getChunk(var1 >> 4, var3 >> 4, ChunkStatus.FULL, false) == null
         || this.clientLevel.getFluidState(this.putAt_BlockPos_MutableBl).is(FluidTags.LAVA);
      this.voidOrLavaCache.put(var4, (byte)(var7 ? 2 : 1));
      return var7;
   }

   @Override
   public List<AABB> collisions(AABB var1) {
      int var2 = (int)Math.floor(var1.minX);
      int var3 = (int)Math.floor(var1.maxX);
      int var4 = (int)Math.floor(var1.minY) - 1;
      int var5 = (int)Math.floor(var1.maxY);
      int var6 = (int)Math.floor(var1.minZ);
      int var7 = (int)Math.floor(var1.maxZ);
      ArrayList<AABB> var8 = new ArrayList<>();

      for (int var9 = var2; var9 <= var3; var9++) {
         for (int var10 = var4; var10 <= var5; var10++) {
            for (int var11 = var6; var11 <= var7; var11++) {
               for (AABB var13 : this.collisionBoxes(var9, var10, var11)) {
                  if (var13.intersects(var1)) {
                     var8.add(var13);
                  }
               }
            }
         }
      }

      return var8;
   }

   private List<AABB> collisionBoxes(int var1, int var2, int var3) {
      if (this.ignoredCollisionPos != null && this.ignoredCollisionPos.getX() == var1 && this.ignoredCollisionPos.getY() == var2 && this.ignoredCollisionPos.getZ() == var3) {
         return List.of();
      }

      long var4 = BlockPos.asLong(var1, var2, var3);
      if (this.passableCells.contains(var4)) {
         return List.of();
      }

      List var6 = this.collisionCache.get(var4);
      if (var6 != null) {
         return var6;
      }

      this.putAt_BlockPos_MutableBl.set(var1, var2, var3);
      if (!this.clientLevel.isInsideBuildHeight(this.putAt_BlockPos_MutableBl)) {
         var6 = var2 < this.clientLevel.getMinY() ? List.of(new AABB(var1, var2, var3, var1 + 1, var2 + 1, var3 + 1)) : List.of();
      } else {
         BlockState var7 = this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl);
         VoxelShape var8 = var7.getCollisionShape(this.clientLevel, this.putAt_BlockPos_MutableBl, this.putAt_CollisionContext);
         if (var8.isEmpty()) {
            var6 = List.of();
         } else {
            var6 = new ArrayList(2);

            for (AABB var10 : var8.toAabbs()) {
               var6.add(var10.move(var1, var2, var3));
            }
         }
      }

      this.collisionCache.put(var4, var6);
      return var6;
   }

   private Block putAt_Block(int var1, int var2, int var3) {
      if (this.ignoredCollisionPos != null && this.ignoredCollisionPos.getX() == var1 && this.ignoredCollisionPos.getY() == var2 && this.ignoredCollisionPos.getZ() == var3) {
         return Blocks.AIR;
      }

      this.putAt_BlockPos_MutableBl.set(var1, var2, var3);
      return this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl).getBlock();
   }

   @Override
   public double putAt_D_I(int var1, int var2, int var3) {
      return this.putAt_Block(var1, var2, var3).getFriction();
   }

   @Override
   public double fill_D(int var1, int var2, int var3) {
      return this.putAt_Block(var1, var2, var3).getSpeedFactor();
   }

   @Override
   public double value_D(int var1, int var2, int var3) {
      return this.putAt_Block(var1, var2, var3).getJumpFactor();
   }

   @Override
   public boolean state(int var1, int var2, int var3) {
      BlockState var4 = this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2, var3));
      return AiPathfinder.LiveGridView.isClimbableBlock(var4)
         ? true
         : var4.getBlock() instanceof TrapDoorBlock
            && var2 > this.clientLevel.getMinY()
            && AiPathfinder.LiveGridView.isClimbableTrapdoor(
               var4, this.clientLevel.getBlockState(this.putAt_BlockPos_MutableBl.set(var1, var2 - 1, var3))
            );
   }

   @Override
   public boolean closeOf(int var1, int var2, int var3) {
      return this.putAt_Block(var1, var2, var3) instanceof SlimeBlock;
   }

   @Override
   public double readEx_D(int var1, int var2, int var3) {
      Block var4 = this.putAt_Block(var1, var2, var3);
      return var4 instanceof SlimeBlock ? 1.0 : (var4 instanceof BedBlock ? 0.75 : 0.0);
   }

   @Override
   public boolean putAt_Z() {
      return true;
   }

   @Override
   public int putAt_I_AABB(AABB var1) {
      byte var2 = 0;

      for (int var3 = (int)Math.floor(var1.minX); var3 <= (int)Math.floor(var1.maxX); var3++) {
         for (int var4 = (int)Math.floor(var1.minY); var4 <= (int)Math.floor(var1.maxY); var4++) {
            for (int var5 = (int)Math.floor(var1.minZ); var5 <= (int)Math.floor(var1.maxZ); var5++) {
               Block var6 = this.putAt_Block(var3, var4, var5);
               if (var6 instanceof WebBlock) {
                  return 1;
               }

               if (var6 instanceof SweetBerryBushBlock) {
                  var2 = 2;
               }
            }
         }
      }

      return var2;
   }

   // 5.3 external ABI bridges
   ClientLevel getLevel() { return this.clientLevel(); }
   void clearCaches() { this.resetCaches(); }
   @Override public double maxDropBlocks() { return this.putAt_D(); }
   @Override public double baseMovementSpeed() { return this.movementSpeed(); }
   @Override public double blockFriction(int x, int y, int z) { return this.putAt_D_I(x, y, z); }
   @Override public double speedFactor(int x, int y, int z) { return this.fill_D(x, y, z); }
   @Override public boolean isSlimeBlock(int x, int y, int z) { return this.closeOf(x, y, z); }
   @Override public double bounceFactor(int x, int y, int z) { return this.readEx_D(x, y, z); }
   @Override public double jumpFactor(int x, int y, int z) { return this.value_D(x, y, z); }
   @Override public boolean isClimbable(int x, int y, int z) { return this.state(x, y, z); }
   @Override public int stuckBlockKindIn(AABB box) { return this.putAt_I_AABB(box); }
   @Override public boolean tracksStuckBlocks() { return this.putAt_Z(); }
   @Override public boolean isLavaOrUnloaded(int x, int y, int z) { return this.putAt_Z_I_2(x, y, z); }
   @Override public boolean isWater(int x, int y, int z) { return this.fill_Z(x, y, z); }
   @Override public boolean isInvisibleLightBlock(int x, int y, int z) { return this.readEx_Z(x, y, z); }
   @Override public boolean isPressurePlate(int x, int y, int z) { return this.value_Z(x, y, z); }
   @Override public boolean isHazard(int x, int y, int z) { return this.prevEx(x, y, z); }
}
