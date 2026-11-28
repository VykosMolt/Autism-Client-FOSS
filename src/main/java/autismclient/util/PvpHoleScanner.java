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
   private static final float BLAST_PROOF_RESISTANCE = 600.0F;
   private static final float UNBREAKABLE_RESISTANCE = 3600000.0F;
   private static final Direction[] HORIZONTALS = new Direction[]{Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
   private static final byte init_B = 0;
   private static final byte load_B = 1;
   private static final byte read_B = 2;
   private static final byte write_B = 3;
   private static final byte update_B = 4;
   private static final int init_I = 4;
   private static final int load_I = 20;
   private static final int SCAN_CELL_BUDGET = 16384;
   private static final int MAX_HORIZONTAL_RANGE = 128;
   private static final int MAX_VERTICAL_RANGE = 64;
   private static final int sync = 64000;
   private static final Set<PvpHoleScanner.ScanSubscriber> subscribers = new CopyOnWriteArraySet<>();
   private static volatile PvpHoleScanner.HoleSnapshot snapshot = PvpHoleScanner.HoleSnapshot.EMPTY;
   private static volatile boolean rescanRequested;
   private static PvpHoleScanner.HoleScan activeScan;
   private static BlockPos lastCentre;
   private static int lastHorizontalRadius;
   private static int lastVerticalRadius;
   private static long lastTickTime = -9223372036854775808L;
   private static long lastScanTime = -9223372036854775808L;
   private static WeakReference<Level> levelRef;

   private PvpHoleScanner() {
   }

   public static void subscribe(PvpHoleScanner.ScanSubscriber subscriber) {
      if (subscriber != null) {
         subscribers.add(subscriber);
      }
   }

   public static void unsubscribe(PvpHoleScanner.ScanSubscriber subscriber) {
      if (subscriber != null && subscribers.remove(subscriber) && subscribers.isEmpty()) {
         reset();
      }
   }

   public static boolean hasSubscribers() {
      return !subscribers.isEmpty();
   }

   public static void reset() {
      snapshot = PvpHoleScanner.HoleSnapshot.EMPTY;
      rescanRequested = true;
      Minecraft mc = Minecraft.getInstance();
      if (mc.isSameThread()) {
         resetState();
      } else {
         mc.execute(PvpHoleScanner::resetState);
      }
   }

   public static void tick() {
      tickScan();
   }

   public static List<PvpHoleScanner.Hole> holes() {
      tickScan();
      return snapshot.holes();
   }

   public static PvpHoleScanner.Hole holeAt(BlockPos pos) {
      tickScan();
      return pos == null ? null : snapshot.byCell().get(pos.asLong());
   }

   public static boolean isHole(BlockPos pos) {
      return holeAt(pos) != null;
   }

   public static List<PvpHoleScanner.Hole> scanHolesNow(BlockPos centre, int horizontalRadius, int verticalRadius) {
      Level level = Minecraft.getInstance().level;
      if (level != null && centre != null) {
         PvpHoleScanner.HoleScan scan = new PvpHoleScanner.HoleScan(level, centre, horizontalRadius, verticalRadius, 64000L);
         scan.step(2147483647);
         return List.copyOf(scan.holes);
      } else {
         return List.of();
      }
   }

   public static PvpHoleScanner.Hole scanHoleAtNow(BlockPos pos) {
      Level level = Minecraft.getInstance().level;
      if (level != null && pos != null) {
         PvpHoleScanner.HoleScan scan = new PvpHoleScanner.HoleScan(level, pos, 0, 0, 64000L);
         scan.step(2147483647);
         return scan.holes.isEmpty() ? null : scan.holes.get(0);
      } else {
         return null;
      }
   }

   private static void tickScan() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.isSameThread()) {
         if (rescanRequested) {
            rescanRequested = false;
            resetState();
         }

         Level level = mc.level;
         if (level != null && mc.player != null && !subscribers.isEmpty()) {
            if (trackedLevel() != level) {
               resetState();
               trackLevel(level);
               snapshot = PvpHoleScanner.HoleSnapshot.EMPTY;
            }

            long gameTime = level.getGameTime();
            if (gameTime != lastTickTime) {
               lastTickTime = gameTime;
               if (activeScan == null) {
                  int horizontal = 0;
                  int vertical = 0;

                  for (PvpHoleScanner.ScanSubscriber subscriber : subscribers) {
                     horizontal = Math.max(horizontal, subscriber.horizontalRange());
                     vertical = Math.max(vertical, subscriber.verticalRange());
                  }

                  BlockPos centre = mc.player.blockPosition();
                  boolean moved = lastCentre == null || centre.distManhattan(lastCentre) >= 4;
                  boolean resized = horizontal != lastHorizontalRadius || vertical != lastVerticalRadius;
                  boolean stale = gameTime - lastScanTime >= 20L;
                  if (!moved && !resized && !stale) {
                     return;
                  }

                  lastHorizontalRadius = horizontal;
                  lastVerticalRadius = vertical;
                  activeScan = new PvpHoleScanner.HoleScan(level, centre, horizontal, vertical, 2147483647L);
               }

               if (activeScan.step(16384)) {
                  publishHoles(activeScan.holes);
                  lastCentre = activeScan.centre;
                  lastScanTime = gameTime;
                  activeScan = null;
               }
            }
         } else {
            if (activeScan != null || lastCentre != null) {
               resetState();
            }

            if (snapshot != PvpHoleScanner.HoleSnapshot.EMPTY) {
               snapshot = PvpHoleScanner.HoleSnapshot.EMPTY;
            }

            trackLevel(level);
         }
      }
   }

   private static Level trackedLevel() {
      WeakReference<Level> ref = levelRef;
      return ref == null ? null : ref.get();
   }

   private static void trackLevel(Level level) {
      if (trackedLevel() != level) {
         levelRef = level == null ? null : new WeakReference<>(level);
      }
   }

   private static void resetState() {
      activeScan = null;
      lastCentre = null;
      levelRef = null;
      lastHorizontalRadius = 0;
      lastVerticalRadius = 0;
      lastTickTime = -9223372036854775808L;
      lastScanTime = -9223372036854775808L;
   }

   private static void publishHoles(List<PvpHoleScanner.Hole> found) {
      List<PvpHoleScanner.Hole> frozen = List.copyOf(found);
      Map<Long, PvpHoleScanner.Hole> byCell = new HashMap<>(Math.max(16, frozen.size() * 3));

      for (PvpHoleScanner.Hole hole : frozen) {
         for (int dx = 0; dx < hole.sizeX; dx++) {
            for (int dz = 0; dz < hole.sizeZ; dz++) {
               byCell.putIfAbsent(BlockPos.asLong(hole.pos.getX() + dx, hole.pos.getY(), hole.pos.getZ() + dz), hole);
            }
         }
      }

      snapshot = new PvpHoleScanner.HoleSnapshot(frozen, byCell);
   }

   private static long cellCount(int horizontal, int vertical) {
      long side = 2L * horizontal + 1L;
      return side * side * (2L * vertical + 1L);
   }

   private record HoleSnapshot(List<PvpHoleScanner.Hole> holes, Map<Long, PvpHoleScanner.Hole> byCell) {
      static final PvpHoleScanner.HoleSnapshot EMPTY = new PvpHoleScanner.HoleSnapshot(List.of(), Map.of());
   }

   private static final class HoleScan {
      private final WeakReference<Level> levelRef;
      private final BlockPos centre;
      private final int minX;
      private final int minY;
      private final int minZ;
      private final int maxX;
      private final int maxY;
      private final int maxZ;
      private final int gridOriginX;
      private final int gridOriginZ;
      private final int gridSizeX;
      private final int gridSizeZ;
      private final int gridCells;
      private final byte[] cells;
      private final BitSet claimed;
      private final MutableBlockPos scratchPos = new MutableBlockPos();
      private final List<PvpHoleScanner.Hole> holes = new ArrayList<>();
      private int cursorX;
      private int cursorY;
      private int cursorZ;
      private int lastRowY = -2147483648;
      private boolean done;

      HoleScan(Level level, BlockPos centre, int horizontal, int vertical, long maxCells) {
         this.levelRef = new WeakReference<>(level);
         this.centre = centre;
         int h = Mth.clamp(horizontal, 0, 128);
         int v = Mth.clamp(vertical, 0, 64);

         while (h > 0 && PvpHoleScanner.cellCount(h, v) > maxCells) {
            h--;
         }

         while (v > 0 && PvpHoleScanner.cellCount(h, v) > maxCells) {
            v--;
         }

         this.minX = centre.getX() - h;
         this.maxX = centre.getX() + h;
         this.minZ = centre.getZ() - h;
         this.maxZ = centre.getZ() + h;
         this.minY = Math.max(centre.getY() - v, level.getMinY() + 1);
         this.maxY = Math.min(centre.getY() + v, level.getMaxY() - 3);
         this.gridOriginX = this.minX - 2;
         this.gridOriginZ = this.minZ - 2;
         this.gridSizeX = this.maxX - this.minX + 1 + 4;
         this.gridSizeZ = this.maxZ - this.minZ + 1 + 4;
         this.gridCells = this.gridSizeX * this.gridSizeZ;
         this.cells = new byte[this.gridCells * 4];
         this.claimed = new BitSet(this.gridCells);
         this.cursorX = this.minX;
         this.cursorY = this.minY;
         this.cursorZ = this.minZ;
      }

      boolean step(int cellBudget) {
         if (this.done) {
            return true;
         }

         int budget = Math.max(1, cellBudget);

         while (budget > 0) {
            if (this.cursorY > this.maxY) {
               this.done = true;
               return true;
            }

            if (this.lastRowY != this.cursorY) {
               this.resetRowWindow(this.cursorY);
            }

            this.scanCell(this.cursorX, this.cursorY, this.cursorZ);
            budget--;
            if (++this.cursorZ > this.maxZ) {
               this.cursorZ = this.minZ;
               if (++this.cursorX > this.maxX) {
                  this.cursorX = this.minX;
                  this.cursorY++;
               }
            }
         }

         if (this.cursorY > this.maxY) {
            this.done = true;
         }

         return this.done;
      }

      private void resetRowWindow(int row) {
         if (this.lastRowY != -2147483648 && row - this.lastRowY == 1) {
            int slot = row + 2 & 3;
            Arrays.fill(this.cells, slot * this.gridCells, (slot + 1) * this.gridCells, (byte)0);
         } else {
            Arrays.fill(this.cells, (byte)0);
         }

         this.claimed.clear();
         this.lastRowY = row;
      }

      private void scanCell(int cellX, int cellY, int cellZ) {
         if (!this.isClaimed(cellX, cellZ) && this.isHoleCell(cellX, cellY, cellZ)) {
            int resistant = 0;
            int open1 = -1;
            int open2 = -1;

            for (int i = 0; i < PvpHoleScanner.HORIZONTALS.length; i++) {
               Direction direction = PvpHoleScanner.HORIZONTALS[i];
               if (isBlastProof(this.cellAt(cellX + direction.getStepX(), cellY, cellZ + direction.getStepZ()))) {
                  resistant++;
               } else if (open1 < 0) {
                  open1 = i;
               } else if (open2 < 0) {
                  open2 = i;
               }
            }

            switch (resistant) {
               case 2:
                  this.checkTwoByTwo(cellX, cellY, cellZ, PvpHoleScanner.HORIZONTALS[open1], PvpHoleScanner.HORIZONTALS[open2]);
                  break;
               case 3:
                  this.extendThroughOpenSide(cellX, cellY, cellZ, PvpHoleScanner.HORIZONTALS[open1]);
                  break;
               case 4:
                  this.addSingleHole(cellX, cellY, cellZ);
            }
         }
      }

      private void addSingleHole(int cellX, int cellY, int cellZ) {
         boolean bedrock = this.cellAt(cellX, cellY - 1, cellZ) == 4;

         for (int i = 0; bedrock && i < PvpHoleScanner.HORIZONTALS.length; i++) {
            Direction direction = PvpHoleScanner.HORIZONTALS[i];
            bedrock = this.cellAt(cellX + direction.getStepX(), cellY, cellZ + direction.getStepZ()) == 4;
         }

         this.addHole(new PvpHoleScanner.Hole(bedrock ? PvpHoleScanner.HoleType.ONE_BY_ONE_BEDROCK : PvpHoleScanner.HoleType.ONE_BY_ONE, new BlockPos(cellX, cellY, cellZ), 1, 1));
      }

      private void extendThroughOpenSide(int cellX, int cellY, int cellZ, Direction open) {
         int otherX = cellX + open.getStepX();
         int otherZ = cellZ + open.getStepZ();
         if (this.isHoleCell(otherX, cellY, otherZ)) {
            Direction back = open.getOpposite();

            for (Direction direction : PvpHoleScanner.HORIZONTALS) {
               if (direction != back && !isBlastProof(this.cellAt(otherX + direction.getStepX(), cellY, otherZ + direction.getStepZ()))) {
                  return;
               }
            }

            boolean alongX = open.getAxis() == Axis.X;
            this.addHole(
               new PvpHoleScanner.Hole(
                  PvpHoleScanner.HoleType.ONE_BY_TWO, new BlockPos(Math.min(cellX, otherX), cellY, Math.min(cellZ, otherZ)), alongX ? 2 : 1, alongX ? 1 : 2
               )
            );
         }
      }

      private void checkTwoByTwo(int cellX, int cellY, int cellZ, Direction d1, Direction d2) {
         int ax = cellX + d1.getStepX();
         int az = cellZ + d1.getStepZ();
         if (this.isWalledCell(ax, cellY, az, d1, d2.getOpposite())) {
            int bx = cellX + d2.getStepX();
            int bz = cellZ + d2.getStepZ();
            if (this.isWalledCell(bx, cellY, bz, d2, d1.getOpposite())) {
               int cx = bx + d1.getStepX();
               int cz = bz + d1.getStepZ();
               if (this.isWalledCell(cx, cellY, cz, d1, d2)) {
                  this.addHole(new PvpHoleScanner.Hole(PvpHoleScanner.HoleType.TWO_BY_TWO, new BlockPos(Math.min(cellX, cx), cellY, Math.min(cellZ, cz)), 2, 2));
               }
            }
         }
      }

      private boolean isWalledCell(int cellX, int cellY, int cellZ, Direction a, Direction b) {
         return this.isHoleCell(cellX, cellY, cellZ)
            && isBlastProof(this.cellAt(cellX + a.getStepX(), cellY, cellZ + a.getStepZ()))
            && isBlastProof(this.cellAt(cellX + b.getStepX(), cellY, cellZ + b.getStepZ()));
      }

      private boolean isHoleCell(int cellX, int cellY, int cellZ) {
         return isBlastProof(this.cellAt(cellX, cellY - 1, cellZ))
            && this.cellAt(cellX, cellY, cellZ) == 1
            && this.cellAt(cellX, cellY + 1, cellZ) == 1
            && this.cellAt(cellX, cellY + 2, cellZ) == 1;
      }

      private void addHole(PvpHoleScanner.Hole hole) {
         this.holes.add(hole);

         for (int dx = 0; dx < hole.sizeX; dx++) {
            for (int dz = 0; dz < hole.sizeZ; dz++) {
               this.claim(hole.pos.getX() + dx, hole.pos.getZ() + dz);
            }
         }
      }

      private int columnIndex(int cellX, int cellZ) {
         int dx = cellX - this.gridOriginX;
         int dz = cellZ - this.gridOriginZ;
         return dx >= 0 && dz >= 0 && dx < this.gridSizeX && dz < this.gridSizeZ ? dx * this.gridSizeZ + dz : -1;
      }

      private void claim(int cellX, int cellZ) {
         int index = this.columnIndex(cellX, cellZ);
         if (index >= 0) {
            this.claimed.set(index);
         }
      }

      private boolean isClaimed(int cellX, int cellZ) {
         int index = this.columnIndex(cellX, cellZ);
         return index >= 0 && this.claimed.get(index);
      }

      private byte cellAt(int cellX, int cellY, int cellZ) {
         int index = this.columnIndex(cellX, cellZ);
         if (index >= 0 && cellY >= this.lastRowY - 1 && cellY <= this.lastRowY + 2) {
            index += (cellY & 3) * this.gridCells;
            byte cached = this.cells[index];
            if (cached != 0) {
               return cached;
            }

            byte value = this.classify(cellX, cellY, cellZ);
            this.cells[index] = value;
            return value;
         } else {
            return this.classify(cellX, cellY, cellZ);
         }
      }

      private byte classify(int cellX, int cellY, int cellZ) {
         Level world = this.levelRef.get();
         if (world == null) {
            return 2;
         }

         BlockState blockState = world.getBlockState(this.scratchPos.set(cellX, cellY, cellZ));
         if (blockState.isAir()) {
            return 1;
         }

         float resistance = blockState.getBlock().getExplosionResistance();
         return resistance >= 3600000.0F ? 4 : (byte)(resistance >= 600.0F ? 3 : 2);
      }

      private static boolean isBlastProof(byte value) {
         return value == 3 || value == 4;
      }
   }

   public interface ScanSubscriber {
      int horizontalRange();

      int verticalRange();
   }

   public static final class Hole {
      private final PvpHoleScanner.HoleType type;
      private final BlockPos pos;
      private final int sizeX;
      private final int sizeZ;
      private final AABB box;

      private Hole(PvpHoleScanner.HoleType type, BlockPos pos, int sizeX, int sizeZ) {
         this.type = type;
         this.pos = pos;
         this.sizeX = sizeX;
         this.sizeZ = sizeZ;
         this.box = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + sizeX, pos.getY() + 1, pos.getZ() + sizeZ);
      }

      public PvpHoleScanner.HoleType type() {
         return this.type;
      }

      public BlockPos pos() {
         return this.pos;
      }

      public int getSizeX() {
         return this.sizeX;
      }

      public int sizeZ() {
         return this.sizeZ;
      }

      public AABB box() {
         return this.box;
      }

      public boolean init_Z() {
         return this.type == PvpHoleScanner.HoleType.ONE_BY_ONE_BEDROCK;
      }

      public int area() {
         return this.sizeX * this.sizeZ;
      }

      public boolean contains(int x, int y, int z) {
         return y == this.pos.getY()
            && x >= this.pos.getX()
            && x < this.pos.getX() + this.sizeX
            && z >= this.pos.getZ()
            && z < this.pos.getZ() + this.sizeZ;
      }

      public boolean contains(BlockPos other) {
         return other != null && this.contains(other.getX(), other.getY(), other.getZ());
      }

      public boolean coversColumn(BlockPos placed) {
         return placed == null
            ? false
            : placed.getX() >= this.pos.getX()
               && placed.getX() < this.pos.getX() + this.sizeX
               && placed.getZ() >= this.pos.getZ()
               && placed.getZ() < this.pos.getZ() + this.sizeZ
               && placed.getY() >= this.pos.getY()
               && placed.getY() <= this.pos.getY() + 2;
      }

      public List<BlockPos> cells() {
         List<BlockPos> list = new ArrayList<>(this.sizeX * this.sizeZ);

         for (int dx = 0; dx < this.sizeX; dx++) {
            for (int dz = 0; dz < this.sizeZ; dz++) {
               list.add(new BlockPos(this.pos.getX() + dx, this.pos.getY(), this.pos.getZ() + dz));
            }
         }

         return list;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj) {
            return true;
         } else {
            return obj instanceof PvpHoleScanner.Hole other
               ? this.type == other.type
                  && this.sizeX == other.sizeX
                  && this.sizeZ == other.sizeZ
                  && this.pos.equals(other.pos)
               : false;
         }
      }

      @Override
      public int hashCode() {
         return this.pos.hashCode() * 31 + this.type.hashCode();
      }

      @Override
      public String toString() {
         return this.type + "@" + this.pos.getX() + "," + this.pos.getY() + "," + this.pos.getZ();
      }
   }

   public enum HoleType {
      ONE_BY_ONE_BEDROCK,
      ONE_BY_ONE,
      ONE_BY_TWO,
      TWO_BY_TWO;
   }
}
