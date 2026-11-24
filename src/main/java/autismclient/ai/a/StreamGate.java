package autismclient.ai.a;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ShortOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

final class StreamGate implements NodeWalker.Inner_CachePool {
   private static final int init_I = 65536;
   private static final int load_I = 4096;
   private final ClassCache.Inner_StateCache init_ClassCache_StateCa;
   private final ClassCache.Inner_RenderState.Inner_CachePool init_ClassCache_RenderS;
   private final StreamGate.Inner_DataUtil[] init_StreamGate_DataUtiArr = new StreamGate.Inner_DataUtil[4096];
   private final Long2ShortOpenHashMap init_Long2ShortOpenHash = new Long2ShortOpenHashMap();
   private final long[] init_JArr = new long[4096];
   private final int[] init_IArr = new int[4096];
   private final Long2ByteOpenHashMap init_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap load_Long2ByteOpenHashM = new Long2ByteOpenHashMap();
   private final Long2ByteOpenHashMap read = new Long2ByteOpenHashMap();
   private final long[] load_JArr = new long[4096];
   private final byte[] init_BArr = new byte[4096];
   private final Long2ByteOpenHashMap write = new Long2ByteOpenHashMap();
   private final Long2IntOpenHashMap init_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private final Long2IntOpenHashMap load_Long2IntOpenHashMa = new Long2IntOpenHashMap();
   private boolean init_Z;
   private ClassCache.UiHelper init_ClassCache_UiHelpe;
   private t.Inner_DataUtil init_t_DataUtil;
   private boolean load_Z;
   long init_J;

   @Override
   public double init_D() {
      return this.init_ClassCache_RenderS == null ? NodeWalker.Inner_CachePool.super.init_D() : this.init_ClassCache_RenderS.init_ClassCache_NetHelp.init_D;
   }

   StreamGate(ClassCache.Inner_StateCache grid, ClassCache.Inner_RenderState.Inner_CachePool probes) {
      this.init_ClassCache_StateCa = grid;
      this.init_ClassCache_RenderS = probes;
      this.init_Long2ShortOpenHash.defaultReturnValue((short)-32768);
      this.init_Long2ByteOpenHashM.defaultReturnValue((byte)-1);
      this.load_Long2ByteOpenHashM.defaultReturnValue((byte)-1);
      this.read.defaultReturnValue((byte)-1);
      Arrays.fill(this.init_BArr, (byte)-1);
      this.write.defaultReturnValue((byte)-1);
      this.init_Long2IntOpenHashMa.defaultReturnValue(-1);
   }

   @Override
   public List<AABB> init_List(AABB q) {
      this.init_V_2();
      int x0 = (int)Math.floor(q.minX);
      int x1 = (int)Math.floor(q.maxX);
      int y0 = (int)Math.floor(q.minY) - 1;
      int y1 = (int)Math.floor(q.maxY);
      int z0 = (int)Math.floor(q.minZ);
      int z1 = (int)Math.floor(q.maxZ);
      List<AABB> out = null;

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               List<AABB> boxes = this.init_List_I(x, y, z);
               if (!boxes.isEmpty()) {
                  for (AABB b : boxes) {
                     if (b.intersects(q)) {
                        if (out == null) {
                           out = new ArrayList<>(4);
                        }

                        out.add(b);
                     }
                  }
               }
            }
         }
      }

      return out == null ? List.of() : out;
   }

   @Override
   public boolean init_Z(AABB q) {
      this.init_V_2();
      int x0 = (int)Math.floor(q.minX);
      int x1 = (int)Math.floor(q.maxX);
      int y0 = (int)Math.floor(q.minY) - 1;
      int y1 = (int)Math.floor(q.maxY);
      int z0 = (int)Math.floor(q.minZ);
      int z1 = (int)Math.floor(q.maxZ);

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               List<AABB> boxes = this.init_List_I(x, y, z);

               for (int i = 0; i < boxes.size(); i++) {
                  if (boxes.get(i).intersects(q)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   private void init_V_2() {
      if (this.init_ClassCache_RenderS != null
         && (
            this.init_ClassCache_RenderS.init_ClassCache_UiHelpe != this.init_ClassCache_UiHelpe
               || this.init_ClassCache_RenderS.init_t_DataUtil != this.init_t_DataUtil
         )) {
         boolean changedTerrain = this.init_ClassCache_RenderS.load_Z();
         if (this.load_Z || changedTerrain) {
            Arrays.fill(this.init_StreamGate_DataUtiArr, null);
            this.init_Long2ShortOpenHash.clear();
            this.load_Long2IntOpenHashMa.clear();
            this.init_Long2ByteOpenHashM.clear();
            this.load_Long2ByteOpenHashM.clear();
            this.read.clear();
            Arrays.fill(this.init_BArr, (byte)-1);
            this.write.clear();
            this.init_Long2IntOpenHashMa.clear();
            this.init_Z = false;
            Arrays.fill(this.init_IArr, 0);
         }

         this.load_Z = changedTerrain;
         this.init_ClassCache_UiHelpe = this.init_ClassCache_RenderS.init_ClassCache_UiHelpe;
         this.init_t_DataUtil = this.init_ClassCache_RenderS.init_t_DataUtil;
      }
   }

   private List<AABB> init_List_I(int x, int y, int z) {
      long key = BlockPos.asLong(x, y, z);
      int index = init_I_I(x, y, z);
      StreamGate.Inner_DataUtil known = this.init_StreamGate_DataUtiArr[index];
      if (known != null && known.key() == key) {
         return known.boxes();
      }

      long pendingBefore = this.init_J;
      short id = this.init(x, y, z, key);
      List<AABB> local = e.init_List_S(id);
      List<AABB> boxes;
      if (local.isEmpty()) {
         boxes = List.of();
      } else {
         boxes = new ArrayList<>(local.size());

         for (AABB b : local) {
            boxes.add(b.move(x, y, z));
         }
      }

      if (this.init_J == pendingBefore) {
         this.init_StreamGate_DataUtiArr[index] = new StreamGate.Inner_DataUtil(key, boxes);
      }

      return boxes;
   }

   private static int init_I_I(int x, int y, int z) {
      return (x & 15) << 8 | (y & 15) << 4 | z & 15;
   }

   short init_S_I(int x, int y, int z) {
      this.init_V_2();
      return this.init(x, y, z, BlockPos.asLong(x, y, z));
   }

   private short init(int x, int y, int z, long key) {
      short known = this.init_Long2ShortOpenHash.get(key);
      if (known != -32768) {
         return known;
      }

      long pendingBefore = this.init_J;
      short id = this.load_S(x, y, z);
      if (this.init_J != pendingBefore) {
         return id;
      }

      if (this.init_Long2ShortOpenHash.size() >= 65536) {
         this.init_Long2ShortOpenHash.clear();
         this.load_Long2IntOpenHashMa.clear();
      }

      this.init_Long2ShortOpenHash.put(key, id);
      if (id == 0) {
         int hold = this.load_I(x, y, z);
         if (hold != 0) {
            this.load_Long2IntOpenHashMa.put(key, hold);
            this.init_Z = true;
         }
      }

      return id;
   }

   private int load_I(int x, int y, int z) {
      if (this.init_ClassCache_RenderS != null
         && this.init_ClassCache_RenderS.init_ClassCache_UiHelpe != null
         && this.init_ClassCache_RenderS.init_ClassCache_UiHelpe.cellsToBreak().contains(new BlockPos(x, y, z))) {
         return 0;
      } else {
         ClassCache.Inner_FileCache p = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z);
         if (p == null) {
            return 0;
         } else {
            return p.web() ? 1 : (p.thorny() ? 2 : 0);
         }
      }
   }

   @Override
   public boolean sync(int x, int y, int z) {
      this.init_V_2();
      if (this.init_ClassCache_RenderS != null
         && this.init_ClassCache_RenderS.init_ClassCache_UiHelpe != null
         && this.init_ClassCache_RenderS.init_ClassCache_UiHelpe.cellsToBreak().contains(new BlockPos(x, y, z))) {
         return false;
      }

      long key = BlockPos.asLong(x, y, z);
      byte known = this.init_ClassCache_RenderS == null ? -1 : this.write.get(key);
      if (known >= 0) {
         return known != 0;
      }

      ClassCache.Inner_FileCache p = this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z);
      if (this.init_ClassCache_RenderS != null && p != null && p.loaded()) {
         if (this.write.size() >= 65536) {
            this.write.clear();
         }

         this.write.put(key, (byte)(p.climbable() ? 1 : 0));
      }

      return p != null && p.climbable();
   }

   @Override
   public boolean load_Z(int x, int y, int z) {
      this.init_V_2();
      long key = BlockPos.asLong(x, y, z);
      byte known = this.init_ClassCache_RenderS == null ? -1 : this.load_Long2ByteOpenHashM.get(key);
      if (known >= 0) {
         return known != 0;
      }

      ClassCache.Inner_FileCache p = this.init_ClassCache_RenderS == null
         ? this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z)
         : this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
      if (this.init_ClassCache_RenderS != null && p != null && p.loaded()) {
         if (this.load_Long2ByteOpenHashM.size() >= 65536) {
            this.load_Long2ByteOpenHashM.clear();
         }

         this.load_Long2ByteOpenHashM.put(key, (byte)(p.water() ? 1 : 0));
      }

      return p != null && p.water();
   }

   @Override
   public boolean init_Z_I(int x, int y, int z) {
      this.init_V_2();
      long key = BlockPos.asLong(x, y, z);
      byte known = this.init_ClassCache_RenderS == null ? -1 : this.init_Long2ByteOpenHashM.get(key);
      if (known >= 0) {
         return known != 0;
      }

      ClassCache.Inner_FileCache p = this.init_ClassCache_RenderS == null
         ? this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z)
         : this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
      if (p != null && p.loaded()) {
         if (this.init_Long2ByteOpenHashM.size() >= 65536) {
            this.init_Long2ByteOpenHashM.clear();
         }

         if (this.init_ClassCache_RenderS != null) {
            this.init_Long2ByteOpenHashM.put(key, (byte)(p.lava() ? 1 : 0));
         }

         return p.lava();
      } else {
         this.init_J++;
         return true;
      }
   }

   @Override
   public boolean read_Z(int x, int y, int z) {
      this.init_V_2();
      long key = BlockPos.asLong(x, y, z);
      byte known = this.init_ClassCache_RenderS == null ? -1 : this.read.get(key);
      if (known >= 0) {
         return known != 0;
      }

      ClassCache.Inner_FileCache p = this.init_ClassCache_RenderS == null
         ? this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z)
         : this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
      if (p != null && p.loaded()) {
         boolean unsafe = p.hazardFeet() || p.floorContactHazard() || p.thorny();
         if (this.init_ClassCache_RenderS != null) {
            if (this.read.size() >= 65536) {
               this.read.clear();
            }

            this.read.put(key, (byte)(unsafe ? 1 : 0));
         }

         return unsafe;
      } else {
         this.init_J++;
         return true;
      }
   }

   @Override
   public boolean write_Z(int x, int y, int z) {
      this.init_V_2();
      ClassCache.Inner_FileCache p = this.init_ClassCache_RenderS == null
         ? this.init_ClassCache_StateCa.init_ClassCache_FileCac_I(x, y, z)
         : this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
      if (p != null && p.loaded()) {
         return p.hazardFeet() || p.thorny() || !p.passable() && p.floorContactHazard() && p.top16() == 15;
      }

      this.init_J++;
      return true;
   }

   @Override
   public boolean init(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int floorBelow) {
      if (this.init_ClassCache_RenderS == null) {
         return false;
      }

      int dx = maxX - minX;
      int dy = maxY - minY;
      int dz = maxZ - minZ;
      int floor = floorBelow - minY;
      if ((dx | dy | dz | floor) >>> 4 != 0) {
         return false;
      }

      this.init_V_2();
      long origin = BlockPos.asLong(minX, minY, minZ);
      int extents = 1 + (dx << 12 | dy << 8 | dz << 4 | floor);
      int index = (int)HashCommon.mix(origin ^ extents * -7046029254386353131L) & 4095;
      if (this.init_IArr[index] == extents && this.init_JArr[index] == origin) {
         return true;
      }

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               ClassCache.Inner_FileCache p = this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
               if (!p.loaded() || p.hazardFeet() || p.thorny()) {
                  return false;
               }

               if (y < floorBelow) {
                  if (p.floorContactHazard()) {
                     return false;
                  }
               } else if (p.lava() || p.water() || !p.passable() && p.floorContactHazard() && p.top16() == 15) {
                  return false;
               }
            }
         }
      }

      this.init_JArr[index] = origin;
      this.init_IArr[index] = extents;
      return true;
   }

   @Override
   public boolean init_Z() {
      return this.init_Z;
   }

   @Override
   public int init_I(AABB box) {
      if (!this.init_Z) {
         return 0;
      }

      int hold = 0;

      for (int x = (int)Math.floor(box.minX); x <= (int)Math.floor(box.maxX); x++) {
         for (int y = (int)Math.floor(box.minY); y <= (int)Math.floor(box.maxY); y++) {
            for (int z = (int)Math.floor(box.minZ); z <= (int)Math.floor(box.maxZ); z++) {
               this.init_S_I(x, y, z);
               int h = this.load_Long2IntOpenHashMa.get(BlockPos.asLong(x, y, z));
               if (h == 1) {
                  return h;
               }

               if (h != 0) {
                  hold = h;
               }
            }
         }
      }

      return hold;
   }

   private short load_S(int x, int y, int z) {
      if (this.init_ClassCache_RenderS != null && this.init_ClassCache_RenderS.init_ClassCache_UiHelpe != null) {
         ClassCache.UiHelper in = this.init_ClassCache_RenderS.init_ClassCache_UiHelpe;
         BlockPos p = new BlockPos(x, y, z);
         if (in.cellsToBreak().contains(p)) {
            return 0;
         }

         if (in.cellsToPlace().contains(p)) {
            return 1;
         }

         if (in.type() == ClassCache.Inner_SyncManager.set && x == in.to().getX() && z == in.to().getZ() && y >= in.to().getY() && y <= in.to().getY() + 1) {
            return 0;
         }
      }

      short id = this.init_ClassCache_RenderS == null ? this.init_ClassCache_StateCa.init_S_I(x, y, z) : this.init_ClassCache_RenderS.init_S_I(x, y, z);
      if (id < 0) {
         this.init_J++;
         if (this.init_ClassCache_RenderS != null) {
            this.init_ClassCache_RenderS.init_I |= 1;
         }

         return 1;
      } else {
         return id;
      }
   }

   long init(BlockPos from, BlockPos to) {
      return this.init_J_BlockPos(from, to, true);
   }

   long init_J_BlockPos(BlockPos from, BlockPos to, boolean staged) {
      int radius = staged ? (int)Math.ceil(4.0) + 1 : 2;
      int x0 = Math.min(from.getX() - radius, to.getX() - 1);
      int x1 = Math.max(from.getX() + radius, to.getX() + 1);
      int z0 = Math.min(from.getZ() - radius, to.getZ() - 1);
      int z1 = Math.max(from.getZ() + radius, to.getZ() + 1);
      int y0 = Math.min(from.getY(), to.getY()) - 1;
      int y1 = Math.max(from.getY(), to.getY()) + 3;
      long h = -7046029254386353131L;

      for (int x = x0; x <= x1; x++) {
         for (int y = y0; y <= y1; y++) {
            for (int z = z0; z <= z1; z++) {
               h = (h ^ HashCommon.mix((long)this.read_I(x, y, z))) * 1099511628211L;
            }
         }
      }

      return h;
   }

   private int read_I(int x, int y, int z) {
      this.init_V_2();
      long key = BlockPos.asLong(x, y, z);
      int known = this.init_ClassCache_RenderS == null ? -1 : this.init_Long2IntOpenHashMa.get(key);
      if (known >= 0) {
         return known;
      }

      long pendingBefore = this.init_J;
      int packed = this.init_S_I(x, y, z) & '\uffff';
      packed |= (this.init_B_I(x, y, z) & 255) << 16;
      if (this.sync(x, y, z)) {
         packed |= 16777216;
      }

      if (this.init_Z_I(x, y, z)) {
         packed |= 33554432;
      }

      if (this.load_Z(x, y, z)) {
         packed |= 67108864;
      }

      packed |= this.load_Long2IntOpenHashMa.get(key) << 27;
      if (this.read_Z(x, y, z)) {
         packed |= 536870912;
      }

      if (this.init_ClassCache_RenderS != null && this.init_J == pendingBefore) {
         if (this.init_Long2IntOpenHashMa.size() >= 65536) {
            this.init_Long2IntOpenHashMa.clear();
         }

         this.init_Long2IntOpenHashMa.put(key, packed);
      }

      return packed;
   }

   private m init_m_I(int x, int y, int z) {
      return m.init(this.init_B_I(x, y, z));
   }

   byte init_B_I(int x, int y, int z) {
      if (this.init_ClassCache_RenderS == null) {
         return this.init_ClassCache_StateCa.init_B_I(x, y, z);
      }

      this.init_V_2();
      long key = BlockPos.asLong(x, y, z);
      int index = init_I_I(x, y, z);
      if (this.load_JArr[index] == key && this.init_BArr[index] >= 0) {
         return this.init_BArr[index];
      }

      byte surface = this.init_ClassCache_StateCa.init_B_I(x, y, z);
      ClassCache.Inner_FileCache cell = this.init_ClassCache_RenderS.init_ClassCache_FileCac_I(x, y, z);
      if (cell != null && cell.loaded()) {
         this.load_JArr[index] = key;
         this.init_BArr[index] = surface;
      }

      return surface;
   }

   @Override
   public double init_D_I(int x, int y, int z) {
      return this.init_m_I(x, y, z).init_D;
   }

   @Override
   public double load_D(int x, int y, int z) {
      return this.init_m_I(x, y, z).load_D;
   }

   @Override
   public double write_D(int x, int y, int z) {
      return this.init_m_I(x, y, z).read_D;
   }

   @Override
   public double read_D(int x, int y, int z) {
      return this.init_m_I(x, y, z).write_D;
   }

   private record Inner_DataUtil(long key, List<AABB> boxes) {
   }
}
