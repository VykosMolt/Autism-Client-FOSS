package autismclient.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class PvpHoleScanner {
   private static final float init_F = 600.0F;
   private static final float load_F = 3600000.0F;
   private static final Direction[] init_DirectionArr = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
   private static final byte init_B = 0;
   private static final byte load_B = 1;
   private static final byte read_B = 2;
   private static final byte write_B = 3;
   private static final byte update_B = 4;
   private static final int init_I = 4;
   private static final int load_I = 20;
   private static final int read_I = 16384;
   private static final int write_I = 128;
   private static final int update_I = 64;
   private static final int sync = 64000;
   private static final Set<PvpHoleScanner.Inner_ConfigStore> init_Set = new CopyOnWriteArraySet<>();
   private static volatile PvpHoleScanner.Inner_BufferHelper init_aj_BufferHelper = PvpHoleScanner.Inner_BufferHelper.init_aj_BufferHelper;
   private static volatile boolean init_Z;
   private static PvpHoleScanner.Inner_CachePool init_aj_CachePool;
   private static BlockPos init_BlockPos;
   private static int flush;
   private static int close;
   private static long init_J = -9223372036854775808L;
   private static long load_J = -9223372036854775808L;
   private static WeakReference<Level> init_WeakReference;

   private PvpHoleScanner() {
   }

   public static void init(PvpHoleScanner.Inner_ConfigStore subscriber) {
      if (subscriber != null) {
         init_Set.add(subscriber);
      }
   }

   public static void load(PvpHoleScanner.Inner_ConfigStore subscriber) {
      if (subscriber != null && init_Set.remove(subscriber) && init_Set.isEmpty()) {
         init_V();
      }
   }

   public static boolean init_Z() {
      return !init_Set.isEmpty();
   }

   public static void init_V() {
      init_aj_BufferHelper = PvpHoleScanner.Inner_BufferHelper.init_aj_BufferHelper;
      init_Z = true;
      Minecraft mc = Minecraft.getInstance();
      if (mc.isSameThread()) {
         write();
      } else {
         mc.execute(PvpHoleScanner::write);
      }
   }

   public static void load() {
      read();
   }

   public static List<PvpHoleScanner.Inner_DataUtil> init_List() {
      read();
      return init_aj_BufferHelper.init_List();
   }

   public static PvpHoleScanner.Inner_DataUtil init_aj_DataUtil(BlockPos pos) {
      read();
      return pos == null ? null : init_aj_BufferHelper.init_Map().get(pos.asLong());
   }

   public static boolean init_Z(BlockPos pos) {
      return init_aj_DataUtil(pos) != null;
   }

   public static List<PvpHoleScanner.Inner_DataUtil> init(BlockPos centre, int horizontalRadius, int verticalRadius) {
      Level level = Minecraft.getInstance().level;
      if (level != null && centre != null) {
         PvpHoleScanner.Inner_CachePool scan = new PvpHoleScanner.Inner_CachePool(level, centre, horizontalRadius, verticalRadius, 64000L);
         scan.init_Z_I(2147483647);
         return List.copyOf(scan.init_List);
      } else {
         return List.of();
      }
   }

   public static PvpHoleScanner.Inner_DataUtil load(BlockPos pos) {
      Level level = Minecraft.getInstance().level;
      if (level != null && pos != null) {
         PvpHoleScanner.Inner_CachePool scan = new PvpHoleScanner.Inner_CachePool(level, pos, 0, 0, 64000L);
         scan.init_Z_I(2147483647);
         return scan.init_List.isEmpty() ? null : scan.init_List.get(0);
      } else {
         return null;
      }
   }

   private static void read() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.isSameThread()) {
         if (init_Z) {
            init_Z = false;
            write();
         }

         Level level = mc.level;
         if (level != null && mc.player != null && !init_Set.isEmpty()) {
            if (init_Level() != level) {
               write();
               init(level);
               init_aj_BufferHelper = PvpHoleScanner.Inner_BufferHelper.init_aj_BufferHelper;
            }

            long gameTime = level.getGameTime();
            if (gameTime != init_J) {
               init_J = gameTime;
               if (init_aj_CachePool == null) {
                  int horizontal = 0;
                  int vertical = 0;

                  for (PvpHoleScanner.Inner_ConfigStore subscriber : init_Set) {
                     horizontal = Math.max(horizontal, subscriber.init_I_32());
                     vertical = Math.max(vertical, subscriber.load_I_17());
                  }

                  BlockPos centre = mc.player.blockPosition();
                  boolean moved = init_BlockPos == null || centre.distManhattan(init_BlockPos) >= 4;
                  boolean resized = horizontal != flush || vertical != close;
                  boolean stale = gameTime - load_J >= 20L;
                  if (!moved && !resized && !stale) {
                     return;
                  }

                  flush = horizontal;
                  close = vertical;
                  init_aj_CachePool = new PvpHoleScanner.Inner_CachePool(level, centre, horizontal, vertical, 2147483647L);
               }

               if (init_aj_CachePool.init_Z_I(16384)) {
                  init(init_aj_CachePool.init_List);
                  init_BlockPos = init_aj_CachePool.init_BlockPos;
                  load_J = gameTime;
                  init_aj_CachePool = null;
               }
            }
         } else {
            if (init_aj_CachePool != null || init_BlockPos != null) {
               write();
            }

            if (init_aj_BufferHelper != PvpHoleScanner.Inner_BufferHelper.init_aj_BufferHelper) {
               init_aj_BufferHelper = PvpHoleScanner.Inner_BufferHelper.init_aj_BufferHelper;
            }

            init(level);
         }
      }
   }

   private static Level init_Level() {
      WeakReference<Level> ref = init_WeakReference;
      return ref == null ? null : ref.get();
   }

   private static void init(Level level) {
      if (init_Level() != level) {
         init_WeakReference = level == null ? null : new WeakReference<>(level);
      }
   }

   private static void write() {
      init_aj_CachePool = null;
      init_BlockPos = null;
      init_WeakReference = null;
      flush = 0;
      close = 0;
      init_J = -9223372036854775808L;
      load_J = -9223372036854775808L;
   }

   private static void init(List<PvpHoleScanner.Inner_DataUtil> found) {
      List<PvpHoleScanner.Inner_DataUtil> frozen = List.copyOf(found);
      Map<Long, PvpHoleScanner.Inner_DataUtil> byCell = new HashMap<>(Math.max(16, frozen.size() * 3));

      for (PvpHoleScanner.Inner_DataUtil hole : frozen) {
         for (int dx = 0; dx < hole.init_I; dx++) {
            for (int dz = 0; dz < hole.load; dz++) {
               byCell.putIfAbsent(BlockPos.asLong(hole.init_BlockPos.getX() + dx, hole.init_BlockPos.getY(), hole.init_BlockPos.getZ() + dz), hole);
            }
         }
      }

      init_aj_BufferHelper = new PvpHoleScanner.Inner_BufferHelper(frozen, byCell);
   }

   private static long init(int horizontal, int vertical) {
      long side = 2L * horizontal + 1L;
      return side * side * (2L * vertical + 1L);
   }

   private record Inner_BufferHelper(List<PvpHoleScanner.Inner_DataUtil> init_List, Map<Long, PvpHoleScanner.Inner_DataUtil> init_Map) {
      static final PvpHoleScanner.Inner_BufferHelper init_aj_BufferHelper = new PvpHoleScanner.Inner_BufferHelper(List.of(), Map.of());
   }

   private static final class Inner_CachePool {
      private final WeakReference<Level> init_WeakReference;
      private final BlockPos init_BlockPos;
      private final int init_I;
      private final int load;
      private final int read;
      private final int write;
      private final int update;
      private final int sync;
      private final int flush;
      private final int close;
      private final int open;
      private final int poll;
      private final int push;
      private final byte[] init_BArr;
      private final BitSet init_BitSet;
      private final MutableBlockPos init_BlockPos_MutableBl = new MutableBlockPos();
      private final List<PvpHoleScanner.Inner_DataUtil> init_List = new ArrayList<>();
      private int pop;
      private int tick;
      private int run;
      private int get = -2147483648;
      private boolean init_Z;

      Inner_CachePool(Level level, BlockPos centre, int horizontal, int vertical, long maxCells) {
         this.init_WeakReference = new WeakReference<>(level);
         this.init_BlockPos = centre;
         int h = Mth.clamp(horizontal, 0, 128);
         int v = Mth.clamp(vertical, 0, 64);

         while (h > 0 && PvpHoleScanner.init(h, v) > maxCells) {
            h--;
         }

         while (v > 0 && PvpHoleScanner.init(h, v) > maxCells) {
            v--;
         }

         this.init_I = centre.getX() - h;
         this.write = centre.getX() + h;
         this.read = centre.getZ() - h;
         this.sync = centre.getZ() + h;
         this.load = Math.max(centre.getY() - v, level.getMinY() + 1);
         this.update = Math.min(centre.getY() + v, level.getMaxY() - 3);
         this.flush = this.init_I - 2;
         this.close = this.read - 2;
         this.open = this.write - this.init_I + 1 + 4;
         this.poll = this.sync - this.read + 1 + 4;
         this.push = this.open * this.poll;
         this.init_BArr = new byte[this.push * 4];
         this.init_BitSet = new BitSet(this.push);
         this.pop = this.init_I;
         this.tick = this.load;
         this.run = this.read;
      }

      boolean init_Z_I(int cellBudget) {
         if (this.init_Z) {
            return true;
         }

         int budget = Math.max(1, cellBudget);

         while (budget > 0) {
            if (this.tick > this.update) {
               this.init_Z = true;
               return true;
            }

            if (this.get != this.tick) {
               this.init_V_I(this.tick);
            }

            this.init_V(this.pop, this.tick, this.run);
            budget--;
            if (++this.run > this.sync) {
               this.run = this.read;
               if (++this.pop > this.write) {
                  this.pop = this.init_I;
                  this.tick++;
               }
            }
         }

         if (this.tick > this.update) {
            this.init_Z = true;
         }

         return this.init_Z;
      }

      private void init_V_I(int row) {
         if (this.get != -2147483648 && row - this.get == 1) {
            int slot = row + 2 & 3;
            Arrays.fill(this.init_BArr, slot * this.push, (slot + 1) * this.push, (byte)0);
         } else {
            Arrays.fill(this.init_BArr, (byte)0);
         }

         this.init_BitSet.clear();
         this.get = row;
      }

      private void init_V(int cellX, int cellY, int cellZ) {
         if (!this.init_Z(cellX, cellZ) && this.init_Z(cellX, cellY, cellZ)) {
            int resistant = 0;
            int open1 = -1;
            int open2 = -1;

            for (int i = 0; i < PvpHoleScanner.init_DirectionArr.length; i++) {
               Direction direction = PvpHoleScanner.init_DirectionArr[i];
               if (init_Z_B(this.init_B(cellX + direction.getStepX(), cellY, cellZ + direction.getStepZ()))) {
                  resistant++;
               } else if (open1 < 0) {
                  open1 = i;
               } else if (open2 < 0) {
                  open2 = i;
               }
            }

            switch (resistant) {
               case 2:
                  this.init_V(cellX, cellY, cellZ, PvpHoleScanner.init_DirectionArr[open1], PvpHoleScanner.init_DirectionArr[open2]);
                  break;
               case 3:
                  this.init(cellX, cellY, cellZ, PvpHoleScanner.init_DirectionArr[open1]);
                  break;
               case 4:
                  this.load_V(cellX, cellY, cellZ);
            }
         }
      }

      private void load_V(int cellX, int cellY, int cellZ) {
         boolean bedrock = this.init_B(cellX, cellY - 1, cellZ) == 4;

         for (int i = 0; bedrock && i < PvpHoleScanner.init_DirectionArr.length; i++) {
            Direction direction = PvpHoleScanner.init_DirectionArr[i];
            bedrock = this.init_B(cellX + direction.getStepX(), cellY, cellZ + direction.getStepZ()) == 4;
         }

         this.init_V_aj_DataUtil(new PvpHoleScanner.Inner_DataUtil(bedrock ? PvpHoleScanner.Inner_StreamUtil.ONE_BY_ONE_BEDROCK : PvpHoleScanner.Inner_StreamUtil.ONE_BY_ONE, new BlockPos(cellX, cellY, cellZ), 1, 1));
      }

      private void init(int cellX, int cellY, int cellZ, Direction open) {
         int otherX = cellX + open.getStepX();
         int otherZ = cellZ + open.getStepZ();
         if (this.init_Z(otherX, cellY, otherZ)) {
            Direction back = open.getOpposite();

            for (Direction direction : PvpHoleScanner.init_DirectionArr) {
               if (direction != back && !init_Z_B(this.init_B(otherX + direction.getStepX(), cellY, otherZ + direction.getStepZ()))) {
                  return;
               }
            }

            boolean alongX = open.getAxis() == Axis.X;
            this.init_V_aj_DataUtil(
               new PvpHoleScanner.Inner_DataUtil(
                  PvpHoleScanner.Inner_StreamUtil.ONE_BY_TWO, new BlockPos(Math.min(cellX, otherX), cellY, Math.min(cellZ, otherZ)), alongX ? 2 : 1, alongX ? 1 : 2
               )
            );
         }
      }

      private void init_V(int cellX, int cellY, int cellZ, Direction d1, Direction d2) {
         int ax = cellX + d1.getStepX();
         int az = cellZ + d1.getStepZ();
         if (this.init_Z(ax, cellY, az, d1, d2.getOpposite())) {
            int bx = cellX + d2.getStepX();
            int bz = cellZ + d2.getStepZ();
            if (this.init_Z(bx, cellY, bz, d2, d1.getOpposite())) {
               int cx = bx + d1.getStepX();
               int cz = bz + d1.getStepZ();
               if (this.init_Z(cx, cellY, cz, d1, d2)) {
                  this.init_V_aj_DataUtil(new PvpHoleScanner.Inner_DataUtil(PvpHoleScanner.Inner_StreamUtil.TWO_BY_TWO, new BlockPos(Math.min(cellX, cx), cellY, Math.min(cellZ, cz)), 2, 2));
               }
            }
         }
      }

      private boolean init_Z(int cellX, int cellY, int cellZ, Direction a, Direction b) {
         return this.init_Z(cellX, cellY, cellZ)
            && init_Z_B(this.init_B(cellX + a.getStepX(), cellY, cellZ + a.getStepZ()))
            && init_Z_B(this.init_B(cellX + b.getStepX(), cellY, cellZ + b.getStepZ()));
      }

      private boolean init_Z(int cellX, int cellY, int cellZ) {
         return init_Z_B(this.init_B(cellX, cellY - 1, cellZ))
            && this.init_B(cellX, cellY, cellZ) == 1
            && this.init_B(cellX, cellY + 1, cellZ) == 1
            && this.init_B(cellX, cellY + 2, cellZ) == 1;
      }

      private void init_V_aj_DataUtil(PvpHoleScanner.Inner_DataUtil hole) {
         this.init_List.add(hole);

         for (int dx = 0; dx < hole.init_I; dx++) {
            for (int dz = 0; dz < hole.load; dz++) {
               this.init_V(hole.init_BlockPos.getX() + dx, hole.init_BlockPos.getZ() + dz);
            }
         }
      }

      private int init_I(int cellX, int cellZ) {
         int dx = cellX - this.flush;
         int dz = cellZ - this.close;
         return dx >= 0 && dz >= 0 && dx < this.open && dz < this.poll ? dx * this.poll + dz : -1;
      }

      private void init_V(int cellX, int cellZ) {
         int index = this.init_I(cellX, cellZ);
         if (index >= 0) {
            this.init_BitSet.set(index);
         }
      }

      private boolean init_Z(int cellX, int cellZ) {
         int index = this.init_I(cellX, cellZ);
         return index >= 0 && this.init_BitSet.get(index);
      }

      private byte init_B(int cellX, int cellY, int cellZ) {
         int index = this.init_I(cellX, cellZ);
         if (index >= 0 && cellY >= this.get - 1 && cellY <= this.get + 2) {
            index += (cellY & 3) * this.push;
            byte cached = this.init_BArr[index];
            if (cached != 0) {
               return cached;
            }

            byte value = this.load_B(cellX, cellY, cellZ);
            this.init_BArr[index] = value;
            return value;
         } else {
            return this.load_B(cellX, cellY, cellZ);
         }
      }

      private byte load_B(int cellX, int cellY, int cellZ) {
         Level world = this.init_WeakReference.get();
         if (world == null) {
            return 2;
         }

         BlockState blockState = world.getBlockState(this.init_BlockPos_MutableBl.set(cellX, cellY, cellZ));
         if (blockState.isAir()) {
            return 1;
         }

         float resistance = blockState.getBlock().getExplosionResistance();
         return resistance >= 3600000.0F ? 4 : (byte)(resistance >= 600.0F ? 3 : 2);
      }

      private static boolean init_Z_B(byte value) {
         return value == 3 || value == 4;
      }
   }

   public interface Inner_ConfigStore {
      int init_I_32();

      int load_I_17();
   }

   public static final class Inner_DataUtil {
      private final PvpHoleScanner.Inner_StreamUtil init_aj_StreamUtil;
      private final BlockPos init_BlockPos;
      private final int init_I;
      private final int load;
      private final AABB init_AABB;

      private Inner_DataUtil(PvpHoleScanner.Inner_StreamUtil type, BlockPos pos, int sizeX, int sizeZ) {
         this.init_aj_StreamUtil = type;
         this.init_BlockPos = pos;
         this.init_I = sizeX;
         this.load = sizeZ;
         this.init_AABB = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + sizeX, pos.getY() + 1, pos.getZ() + sizeZ);
      }

      public PvpHoleScanner.Inner_StreamUtil init_aj_StreamUtil() {
         return this.init_aj_StreamUtil;
      }

      public BlockPos init_BlockPos() {
         return this.init_BlockPos;
      }

      public int init_I() {
         return this.init_I;
      }

      public int load() {
         return this.load;
      }

      public AABB init_AABB() {
         return this.init_AABB;
      }

      public boolean init_Z() {
         return this.init_aj_StreamUtil == PvpHoleScanner.Inner_StreamUtil.ONE_BY_ONE_BEDROCK;
      }

      public int read() {
         return this.init_I * this.load;
      }

      public boolean init(int x, int y, int z) {
         return y == this.init_BlockPos.getY()
            && x >= this.init_BlockPos.getX()
            && x < this.init_BlockPos.getX() + this.init_I
            && z >= this.init_BlockPos.getZ()
            && z < this.init_BlockPos.getZ() + this.load;
      }

      public boolean init(BlockPos other) {
         return other != null && this.init(other.getX(), other.getY(), other.getZ());
      }

      public boolean load(BlockPos placed) {
         return placed == null
            ? false
            : placed.getX() >= this.init_BlockPos.getX()
               && placed.getX() < this.init_BlockPos.getX() + this.init_I
               && placed.getZ() >= this.init_BlockPos.getZ()
               && placed.getZ() < this.init_BlockPos.getZ() + this.load
               && placed.getY() >= this.init_BlockPos.getY()
               && placed.getY() <= this.init_BlockPos.getY() + 2;
      }

      public List<BlockPos> init_List() {
         List<BlockPos> list = new ArrayList<>(this.init_I * this.load);

         for (int dx = 0; dx < this.init_I; dx++) {
            for (int dz = 0; dz < this.load; dz++) {
               list.add(new BlockPos(this.init_BlockPos.getX() + dx, this.init_BlockPos.getY(), this.init_BlockPos.getZ() + dz));
            }
         }

         return list;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return obj instanceof PvpHoleScanner.Inner_DataUtil other
               ? this.init_aj_StreamUtil == other.init_aj_StreamUtil
                  && this.init_I == other.init_I
                  && this.load == other.load
                  && this.init_BlockPos.equals(other.init_BlockPos)
               : false;
         }
      }

      @Override
      public int hashCode() {
         return this.init_BlockPos.hashCode() * 31 + this.init_aj_StreamUtil.hashCode();
      }

      @Override
      public String toString() {
         return this.init_aj_StreamUtil + "@" + this.init_BlockPos.getX() + "," + this.init_BlockPos.getY() + "," + this.init_BlockPos.getZ();
      }
   }

   public enum Inner_StreamUtil {
      ONE_BY_ONE_BEDROCK,
      ONE_BY_ONE,
      ONE_BY_TWO,
      TWO_BY_TWO;
   }
}
