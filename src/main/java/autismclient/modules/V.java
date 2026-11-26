package autismclient.modules;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class V {
   static final byte init_B = 0;
   static final byte load_B = 1;
   static final byte read_B = 2;
   static final byte write_B = 3;
   static final byte update_B = 4;
   static final int init_I = 8;
   static final double init_D = 0.6;
   static final int load_I = 48;
   private static final double close_D = 1.0E-6;
   final W.Inner_UiHelper init_W_UiHelper;
   final double load_D;
   final double read_D;
   final double write_D;
   final double update_D;
   final double sync_D;
   final double flush_D;
   final boolean init_Z;
   final int read_I;
   final int write_I;
   final int update_I;
   final int sync_I;
   final int flush_I;
   private final V.Inner_StreamUtil[] init_V_StreamUtilArr;
   private byte[] init_BArr;
   private byte[] load_BArr;
   private int[] init_IArr;
   private int open;
   int close_I;

   V(W.Inner_UiHelper world, AABB body, double ref, double radius, double minimum, boolean voidOnly) {
      this.init_W_UiHelper = world;
      this.load_D = ref;
      this.write_D = Math.max(0.5, Math.min(16.0, radius));
      this.read_D = minimum;
      this.init_Z = voidOnly;
      this.update_D = body.getCenter().x;
      this.sync_D = body.getCenter().z;
      this.flush_D = body.getYsize();
      this.read_I = (int)Math.floor(ref) - 2;
      this.write_I = (int)Math.floor(this.update_D);
      this.update_I = (int)Math.floor(this.sync_D);
      this.sync_I = (int)Math.ceil(this.write_D) + 8;
      this.flush_I = 2 * this.sync_I + 1;
      this.init_V_StreamUtilArr = new V.Inner_StreamUtil[this.flush_I * this.flush_I];
   }

   private int init_I_I_I(int x, int z) {
      int i = x - this.write_I + this.sync_I;
      int k = z - this.update_I + this.sync_I;
      return i >= 0 && k >= 0 && i < this.flush_I && k < this.flush_I ? i * this.flush_I + k : -1;
   }

   V.Inner_StreamUtil init_V_StreamUtil_I_I(int x, int z) {
      int index = this.init_I_I_I(x, z);
      if (index < 0) {
         return null;
      }

      V.Inner_StreamUtil column = this.init_V_StreamUtilArr[index];
      if (column == null) {
         column = this.init_W_UiHelper.init_V_StreamUtil_I(x, z, this.read_I, this.load_D + 0.6);
         if (column == null) {
            column = V.Inner_StreamUtil.init_V_StreamUtil;
         }

         this.init_V_StreamUtilArr[index] = column;
         this.close_I++;
      }

      return column;
   }

   long init_J_D_D(double lo, double hi) {
      return init_J_I(this.read_I, lo, hi);
   }

   static long init_J_I(int base, double lo, double hi) {
      if (!(hi > lo)) {
         return 0L;
      }

      int from = (int)Math.floor((lo - base) * 8.0 + 1.0E-6);
      int to = (int)Math.ceil((hi - base) * 8.0 - 1.0E-6) - 1;
      if (from < 0) {
         from = 0;
      }

      if (to > 63) {
         to = 63;
      }

      if (to < from) {
         return 0L;
      }

      long upper = to == 63 ? -1L : (1L << to + 1) - 1L;
      return upper & -1L << from;
   }

   double init_D_V_StreamUtil_D(V.Inner_StreamUtil column, double y) {
      int limit = (int)Math.floor((y - this.read_I) * 8.0 + 1.0E-6);
      if (limit > 0) {
         long tops = column.init_J & ~(column.init_J >>> 1);
         long below = limit >= 64 ? tops : tops & (1L << limit) - 1L;
         if (below != 0L) {
            return this.read_I + (64 - Long.numberOfLeadingZeros(below)) / 8.0;
         }
      }

      return column.init_D <= y + 1.0E-6 ? column.init_D : -1.0 / 0.0;
   }

   boolean init(V.Inner_StreamUtil column) {
      return column.init_B != 0
         && (column.init_J & this.init_J_D_D(this.load_D + 0.6, this.load_D + this.flush_D)) == 0L
         && (column.load & this.init_J_D_D(this.load_D, this.load_D + this.flush_D)) == 0L;
   }

   boolean load(V.Inner_StreamUtil column) {
      if (!this.init(column)) {
         return false;
      } else if (column.init_B == 2) {
         return true;
      } else {
         return column.init_B == 4
            ? !this.init_Z
            : !this.init_Z && column.init_B == 1 && this.init_D_V_StreamUtil_D(column, this.load_D + 0.6) <= this.load_D - this.read_D + 1.0E-6;
      }
   }

   double init_D_I_I(int x, int z) {
      double dx = Math.max(Math.max(x - this.update_D, 0.0), this.update_D - (x + 1));
      double dz = Math.max(Math.max(z - this.sync_D, 0.0), this.sync_D - (z + 1));
      return Math.hypot(dx, dz);
   }

   private byte init_B_I_I(int x, int z) {
      int index = this.init_I_I_I(x, z);
      if (index < 0) {
         return 3;
      }

      if (this.init_BArr == null) {
         this.init_BArr = new byte[this.init_V_StreamUtilArr.length];
      }

      byte state = this.init_BArr[index];
      if (state == 0) {
         V.Inner_StreamUtil column = this.init_V_StreamUtil_I_I(x, z);
         state = (byte)(!this.load(column) ? 3 : (this.init_D_I_I(x, z) <= this.write_D + 1.0E-6 ? 1 : 2));
         this.init_BArr[index] = state;
      }

      return state;
   }

   V.Inner_CachePool init_V_CachePool_AABB(AABB body, Vec3 motion, boolean grounded, W.Inner_SyncManager steer) {
      double hw = body.getXsize() / 2.0;
      double hd = body.getZsize() / 2.0;
      double h = body.getYsize();
      double x = body.getCenter().x;
      double z = body.getCenter().z;
      double y = body.minY;
      double vx = motion.x;
      double vy = motion.y;
      double vz = motion.z;
      boolean ground = grounded;
      boolean blocked = false;
      boolean grazed = false;
      long obstacle = this.init_J_D_D(this.load_D + 0.6, this.load_D + h);
      int crossTick = -1;
      double crossSpeed = 0.0;
      if (steer == null) {
         steer = W.Inner_SyncManager.init_W_SyncManager;
      }

      boolean countering = steer != W.Inner_SyncManager.init_W_SyncManager;

      for (int tick = 1; tick <= 48; tick++) {
         double block = this.init_D_D(x, z, hw, hd);
         if (Double.isNaN(block)) {
            return this.init_V_CachePool_I(tick, x, z);
         }

         double friction = ground ? block * 0.91 : 0.91;
         if (countering && steer.init(vx, vz)) {
            double accel = ground ? steer.read() * (0.21600002 / (block * block * block)) : steer.write();
            vx += steer.init_D() * accel;
            vz += steer.load() * accel;
         }

         double wantY = Math.abs(vy) < 1.0E-8 ? -0.01 : vy;
         double before = y;
         boolean landed = false;
         if (wantY < 0.0) {
            double top = this.init(x, z, hw, hd, y);
            if (Double.isNaN(top)) {
               return this.init_V_CachePool_I(tick, x, z);
            }

            double next = y + wantY;
            if (top >= next - 1.0E-9) {
               next = top;
               landed = true;
            }

            y = next;
         } else {
            long above = this.init_J_D(x, z, hw, hd);
            if (above == -9223372036854775808L) {
               return this.init_V_CachePool_I(tick, x, z);
            }

            long hit = above & this.init_J_D_D(y + h, y + h + wantY);
            if (hit != 0L) {
               y = Math.max(y, this.read_I + Long.numberOfTrailingZeros(hit) / 8.0 - h);
               wantY = 0.0;
            } else {
               y += wantY;
            }
         }

         boolean stepping = ground || landed;
         double stepFrom = landed ? y : before;
         long bodyBand = this.init_J_D_D(stepping ? Math.max(y, stepFrom + 0.6) : y, y + h);
         boolean zFirst = Math.abs(vx) < Math.abs(vz);

         for (int pass = 0; pass < 2; pass++) {
            boolean alongX = pass == 0 != zFirst;
            double d = alongX ? vx : vz;
            if (!(Math.abs(d) < 1.0E-9)) {
               double moved = this.init(alongX, d, x, z, hw, hd, bodyBand);
               if (Double.isNaN(moved)) {
                  return this.init_V_CachePool_I(tick, x, z);
               }

               if (moved == -1.0 / 0.0) {
                  return new V.Inner_CachePool(W.Inner_TaskQueue.SAFE, 0.0, tick, x, z, false, crossTick, crossSpeed, blocked, grazed);
               }

               if (Math.abs(moved - d) > 1.0E-9) {
                  blocked = true;
                  if (alongX) {
                     vx = 0.0;
                  } else {
                     vz = 0.0;
                  }
               }

               if (alongX) {
                  x += moved;
               } else {
                  z += moved;
               }
            }
         }

         if (stepping) {
            double raised = this.init(x, z, hw, hd, stepFrom + 0.6);
            if (!Double.isNaN(raised) && raised > y + 1.0E-7) {
               y = raised;
            }
         }

         long walls = this.init_J_D(x, z, hw, hd);
         if (walls == -9223372036854775808L) {
            return this.init_V_CachePool_I(tick, x, z);
         }

         grazed |= (walls & obstacle) != 0L;
         long caught = this.load(x, z, hw, hd);
         if (caught == -9223372036854775808L) {
            return this.init_V_CachePool_I(tick, x, z);
         }

         if ((caught & this.init_J_D_D(y, y + h)) != 0L) {
            return new V.Inner_CachePool(W.Inner_TaskQueue.SAFE, 0.0, tick, x, z, false, crossTick, crossSpeed, blocked, grazed);
         }

         if (crossTick < 0 && !landed && this.init_Z_D(x, z, hw, hd)) {
            crossTick = tick;
            crossSpeed = Math.hypot(vx, vz);
         }

         if (!landed && wantY < 0.0 && y < this.load_D - 0.5) {
            V.Inner_CachePool settled = this.init(x, z, hw, hd, vx, vz, y, tick, crossTick, crossSpeed, blocked, grazed);
            if (settled != null) {
               return settled;
            }
         }

         vy = landed ? 0.0 : wantY;
         vx *= friction;
         vz *= friction;
         vy = (vy - 0.08) * 0.98;
         ground = landed;
         if (ground && tick > 1) {
            countering = false;
         }

         if (ground && Math.hypot(vx, vz) < 0.015) {
            return this.init(x, z, y, tick, crossTick, crossSpeed, blocked, grazed);
         }
      }

      return this.init(x, z, y, 48, crossTick, crossSpeed, blocked, grazed);
   }

   private V.Inner_CachePool init_V_CachePool_I(int tick, double x, double z) {
      return new V.Inner_CachePool(W.Inner_TaskQueue.UNKNOWN, 0.0, tick, x, z, false, -1, 0.0, false, false);
   }

   private V.Inner_CachePool init(double x, double z, double y, int tick, int crossTick, double crossSpeed, boolean blocked, boolean grazed) {
      double drop = Math.max(0.0, this.load_D - y);
      W.Inner_TaskQueue kind = drop > 1.0E-6 ? W.Inner_TaskQueue.DROP : W.Inner_TaskQueue.SAFE;
      boolean edge = crossTick >= 0 && kind == W.Inner_TaskQueue.DROP && !this.init_Z && drop + 1.0E-6 >= this.read_D;
      return new V.Inner_CachePool(kind, drop, tick, x, z, edge, crossTick, crossSpeed, blocked, grazed);
   }

   private V.Inner_CachePool init(
      double x, double z, double hw, double hd, double vx, double vz, double y, int tick, int crossTick, double crossSpeed, boolean blocked, boolean grazed
   ) {
      double speed = Math.hypot(vx, vz);
      double drift = speed * 0.91 / 0.09 + 0.05;
      double ex = speed > 1.0E-9 ? vx / speed * drift : 0.0;
      double ez = speed > 1.0E-9 ? vz / speed * drift : 0.0;
      int x0 = (int)Math.floor(Math.min(x - hw, x - hw + ex) + 0.001);
      int x1 = (int)Math.floor(Math.max(x + hw, x + hw + ex) - 0.001);
      int z0 = (int)Math.floor(Math.min(z - hd, z - hd + ez) + 0.001);
      int z1 = (int)Math.floor(Math.max(z + hd, z + hd + ez) - 0.001);
      double top = -1.0 / 0.0;
      boolean caught = false;
      boolean lava = false;

      for (int cx = x0; cx <= x1; cx++) {
         for (int cz = z0; cz <= z1; cz++) {
            V.Inner_StreamUtil column = this.init_V_StreamUtil_I_I(cx, cz);
            if (column == null) {
               return null;
            }

            if (column.init_B == 0) {
               return this.init_V_CachePool_I(tick, x, z);
            }

            if (column.init_B == 3 || (column.load & this.init_J_D_D(-1.0E9, y)) != 0L) {
               caught = true;
            }

            lava |= column.init_B == 4;
            top = Math.max(top, this.init_D_V_StreamUtil_D(column, y));
         }
      }

      boolean edge = crossTick >= 0;
      if (caught) {
         return new V.Inner_CachePool(W.Inner_TaskQueue.SAFE, 0.0, tick, x, z, false, crossTick, crossSpeed, blocked, grazed);
      }

      if (top == -1.0 / 0.0) {
         return new V.Inner_CachePool(
            lava ? W.Inner_TaskQueue.LAVA : W.Inner_TaskQueue.VOID, 1.0 / 0.0, tick, x, z, edge, crossTick, crossSpeed, blocked, grazed
         );
      }

      double drop = this.load_D - top;
      return !this.init_Z && drop + 1.0E-6 >= this.read_D
         ? new V.Inner_CachePool(W.Inner_TaskQueue.DROP, drop, tick, x, z, edge, crossTick, crossSpeed, blocked, grazed)
         : null;
   }

   private double init(boolean alongX, double d, double x, double z, double hw, double hd, long bodyBand) {
      double lead = alongX ? (d > 0.0 ? x + hw : x - hw) : (d > 0.0 ? z + hd : z - hd);
      double crossMin = alongX ? z - hd : x - hw;
      double crossMax = alongX ? z + hd : x + hw;
      int c0 = (int)Math.floor(crossMin + 0.001);
      int c1 = (int)Math.floor(crossMax - 0.001);
      int from = d > 0.0 ? (int)Math.floor(lead - 1.0E-7) + 1 : (int)Math.floor(lead + 1.0E-7) - 1;
      int to = d > 0.0 ? (int)Math.floor(lead + d - 1.0E-7) : (int)Math.floor(lead + d + 1.0E-7);
      int step = d > 0.0 ? 1 : -1;

      for (int cell = from; d > 0.0 ? cell <= to : cell >= to; cell += step) {
         for (int c = c0; c <= c1; c++) {
            V.Inner_StreamUtil column = alongX ? this.init_V_StreamUtil_I_I(cell, c) : this.init_V_StreamUtil_I_I(c, cell);
            if (column == null) {
               return -1.0 / 0.0;
            }

            if (column.init_B == 0) {
               return 0.0 / 0.0;
            }

            if ((column.init_J & bodyBand) != 0L) {
               double face = d > 0.0 ? cell : cell + 1;
               double moved = face - lead - Math.signum(d) * 1.0E-7;
               return d > 0.0 ? Math.max(0.0, Math.min(d, moved)) : Math.min(0.0, Math.max(d, moved));
            }
         }
      }

      return d;
   }

   private boolean init(double x, double z, double hw, double hd, V.Inner_DataUtil visitor) {
      int x0 = (int)Math.floor(x - hw + 0.001);
      int x1 = (int)Math.floor(x + hw - 0.001);
      int z0 = (int)Math.floor(z - hd + 0.001);
      int z1 = (int)Math.floor(z + hd - 0.001);

      for (int cx = x0; cx <= x1; cx++) {
         for (int cz = z0; cz <= z1; cz++) {
            V.Inner_StreamUtil column = this.init_V_StreamUtil_I_I(cx, cz);
            if (column == null || column.init_B == 0 || !visitor.visit(column)) {
               return false;
            }
         }
      }

      return true;
   }

   private double init(double x, double z, double hw, double hd, double y) {
      double[] top = new double[]{-1.0 / 0.0};
      return this.init(x, z, hw, hd, c -> {
         top[0] = Math.max(top[0], this.init_D_V_StreamUtil_D(c, y));
         return true;
      }) ? top[0] : 0.0 / 0.0;
   }

   private long init_J_D(double x, double z, double hw, double hd) {
      long[] walls = new long[]{0L};
      return this.init(x, z, hw, hd, c -> {
         walls[0] |= c.init_J;
         return true;
      }) ? walls[0] : -9223372036854775808L;
   }

   private long load(double x, double z, double hw, double hd) {
      long[] catches = new long[]{0L};
      return this.init(x, z, hw, hd, c -> {
         catches[0] |= c.load;
         return true;
      }) ? catches[0] : -9223372036854775808L;
   }

   private double init_D_D(double x, double z, double hw, double hd) {
      V.Inner_StreamUtil column = this.init_V_StreamUtil_I_I((int)Math.floor(x), (int)Math.floor(z));
      return column != null && column.init_B != 0 ? column.init_F : 0.0 / 0.0;
   }

   private boolean init_Z_D(double x, double z, double hw, double hd) {
      int x0 = (int)Math.floor(x - hw + 0.001);
      int x1 = (int)Math.floor(x + hw - 0.001);
      int z0 = (int)Math.floor(z - hd + 0.001);
      int z1 = (int)Math.floor(z + hd - 0.001);

      for (int cx = x0; cx <= x1; cx++) {
         for (int cz = z0; cz <= z1; cz++) {
            if (this.init_B_I_I(cx, cz) == 3) {
               return false;
            }
         }
      }

      for (int cx = x0; cx <= x1; cx++) {
         for (int cz = z0; cz <= z1; cz++) {
            if (this.init_Z_I_I(cx, cz)) {
               return true;
            }
         }
      }

      return false;
   }

   boolean init_Z_I_I(int x, int z) {
      byte state = this.init_B_I_I(x, z);
      if (state == 1) {
         return true;
      }

      if (state != 2) {
         return false;
      }

      int index = this.init_I_I_I(x, z);
      if (this.load_BArr == null) {
         this.load_BArr = new byte[this.init_V_StreamUtilArr.length];
      }

      if (this.load_BArr[index] != 0) {
         return this.load_BArr[index] == 1;
      }

      if (this.init_IArr == null) {
         this.init_IArr = new int[this.init_V_StreamUtilArr.length];
         int r = (int)Math.ceil(this.write_D) + 1;

         for (int cx = this.write_I - r; cx <= this.write_I + r; cx++) {
            for (int cz = this.update_I - r; cz <= this.update_I + r; cz++) {
               if (this.init_D_I_I(cx, cz) <= this.write_D + 1.0E-6 && this.init_B_I_I(cx, cz) == 1) {
                  this.init_IArr[this.open++] = this.init_I_I_I(cx, cz);
               }
            }
         }
      }

      for (int i = 0; i < this.open; i++) {
         int anchor = this.init_IArr[i];
         int ax = anchor / this.flush_I - this.sync_I + this.write_I;
         int az = anchor % this.flush_I - this.sync_I + this.update_I;
         if (this.init_Z_I(x, z, ax, az)) {
            this.load_BArr[index] = 1;
            return true;
         }
      }

      this.load_BArr[index] = 2;
      return false;
   }

   private boolean init_Z_I(int x, int z, int endX, int endZ) {
      int dx = Math.abs(endX - x);
      int dz = Math.abs(endZ - z);
      int sx = Integer.compare(endX, x);
      int sz = Integer.compare(endZ, z);
      int crossedX = 0;
      int crossedZ = 0;

      while (crossedX < dx || crossedZ < dz) {
         int nextX = (2 * crossedX + 1) * dz;
         int nextZ = (2 * crossedZ + 1) * dx;
         if (nextX == nextZ) {
            if (this.init_B_I_I(x + sx, z) == 3 || this.init_B_I_I(x, z + sz) == 3) {
               return false;
            }

            x += sx;
            z += sz;
            crossedX++;
            crossedZ++;
         } else if (nextX < nextZ) {
            x += sx;
            crossedX++;
         } else {
            z += sz;
            crossedZ++;
         }

         if (this.init_B_I_I(x, z) == 3) {
            return false;
         }
      }

      return true;
   }

   boolean init() {
      int r = (int)Math.ceil(this.write_D) + 1;

      for (int ring = 0; ring <= r; ring++) {
         for (int x = this.write_I - ring; x <= this.write_I + ring; x++) {
            for (int z = this.update_I - ring; z <= this.update_I + ring; z++) {
               if (Math.max(Math.abs(x - this.write_I), Math.abs(z - this.update_I)) == ring
                  && !(this.init_D_I_I(x, z) > this.write_D + 1.0E-6)
                  && this.init_B_I_I(x, z) == 1) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   record Inner_CachePool(
      W.Inner_TaskQueue init_W_TaskQueue,
      double init_D,
      int init_I,
      double load_D,
      double read_D,
      boolean init_Z,
      int load_I,
      double write,
      boolean load_Z,
      boolean read_Z
   ) {
      boolean init(double minimum, boolean voidOnly) {
         return this.init_W_TaskQueue == W.Inner_TaskQueue.VOID
            || !voidOnly
               && (this.init_W_TaskQueue == W.Inner_TaskQueue.LAVA || this.init_W_TaskQueue == W.Inner_TaskQueue.DROP && this.init_D + 1.0E-6 >= minimum);
      }
   }

   private interface Inner_DataUtil {
      boolean visit(V.Inner_StreamUtil var1);
   }

   record Inner_StreamUtil(long init_J, long load, double init_D, byte init_B, float init_F) {
      static final V.Inner_StreamUtil init_V_StreamUtil = new V.Inner_StreamUtil(-1L, -1L, -1.0 / 0.0, (byte)0, 0.6F);
   }
}
