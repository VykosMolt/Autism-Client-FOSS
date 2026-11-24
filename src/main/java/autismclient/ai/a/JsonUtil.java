package autismclient.ai.a;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class JsonUtil {
   public static final float init_F = 100.0F;
   static final double init_D = 1.62;
   static final double load_D = 1.27;
   static final float load_F = 179.5F;
   static final float read_F = 25.0F;
   static final double read_D = 0.14;
   static final double write_D = 0.04;
   static final float write_F = 12.0F;
   static final double update = 0.3;
   static final int init_I = 30;
   static final int load_I = 8;
   static final int read_I = 6;
   private final JsonUtil.Inner_CachePool init_JsonUtil_CachePool;
   private final boolean init_Z;
   private JsonUtil.Inner_ConfigStore init_JsonUtil_ConfigSto;
   private boolean load_Z;
   private boolean read_Z;
   private boolean write_Z = true;
   public static final double sync = 4.5;

   public JsonUtil(JsonUtil.Inner_CachePool lane, JsonUtil.Inner_ConfigStore style) {
      this(lane, style, false);
   }

   public JsonUtil(JsonUtil.Inner_CachePool lane, JsonUtil.Inner_ConfigStore style, boolean stopAtEnd) {
      this.init_JsonUtil_CachePool = lane;
      this.init_JsonUtil_ConfigSto = style;
      this.init_Z = stopAtEnd;
   }

   public JsonUtil.Inner_CachePool init_JsonUtil_CachePool() {
      return this.init_JsonUtil_CachePool;
   }

   public JsonUtil.Inner_ConfigStore init_JsonUtil_ConfigSto() {
      return this.init_JsonUtil_ConfigSto;
   }

   public void init_V_JsonUtil_ConfigSto(JsonUtil.Inner_ConfigStore value) {
      this.init_JsonUtil_ConfigSto = value;
   }

   public int init_I_NodeWalker_CachePo(NodeWalker.Inner_CachePool world) {
      for (int i = 1; i <= this.init_JsonUtil_CachePool.length(); i++) {
         if (!init_Z_NodeWalker_CachePo(
            world, this.init_JsonUtil_CachePool.cellX(i), this.init_JsonUtil_CachePool.floorY(), this.init_JsonUtil_CachePool.cellZ(i)
         )) {
            return i;
         }
      }

      return this.init_JsonUtil_CachePool.length() + 1;
   }

   static boolean init_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, int x, int y, int z) {
      AABB cell = new AABB(x, y, z, x + 1, y + 1, z + 1).deflate(1.0E-4);

      for (AABB b : world.init_List(cell)) {
         if (b.intersects(cell) && b.maxY >= y + 1 - 0.001) {
            return true;
         }
      }

      return false;
   }

   public JsonUtil.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU_2(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world
   ) {
      return this.init_JsonUtil_StreamUti_NodeWalker_StreamU_2(s, cam, sneaking, world, true);
   }

   public JsonUtil.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU_2(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world, boolean canPlace
   ) {
      this.write_Z = canPlace;
      double w = this.init_JsonUtil_CachePool.lateral(s.x(), s.z());
      if (Math.abs(w) > 0.14) {
         this.read_Z = true;
      } else if (Math.abs(w) < 0.04) {
         this.read_Z = false;
      }

      return this.load_JsonUtil_StreamUti_NodeWalker_StreamU(s, cam, sneaking, world);
   }

   private JsonUtil.Inner_StreamUtil load_JsonUtil_StreamUti_NodeWalker_StreamU(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world
   ) {
      int next = this.init_I_NodeWalker_CachePo(world);
      double u = this.init_JsonUtil_CachePool.along(s.x(), s.z());
      float fwd = this.init_JsonUtil_CachePool.yaw();
      if (s.onGround()) {
         this.load_Z = false;
      }

      if (next <= this.init_JsonUtil_CachePool.length()
         && (
            !s.onGround() || !(u >= this.init_JsonUtil_CachePool.length() + 1) || !init_Z_NodeWalker_StreamU_2(s, world, this.init_JsonUtil_CachePool.floorY())
         )) {
         if (!s.onGround()) {
            return this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, cam, sneaking, world, next);
         }

         if (this.init_JsonUtil_ConfigSto == JsonUtil.Inner_ConfigStore.init) {
            JsonUtil.Inner_DataUtil c = cam.stepToward(fwd, 25.0F);
            if (init_Z_F_F(cam.yaw(), fwd)) {
               if (this.init_Z_NodeWalker_StreamU(s, c, world)) {
                  return this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, c, true);
               }

               JsonUtil.Inner_StreamUtil running = this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, c, false);
               NodeWalker.Inner_StreamUtil ahead = NodeWalker.init(s, init_NodeWalker_DataUti_JsonUtil_StreamUti_JsonUtil_DataUtil(running, c), world);
               if (init_Z_NodeWalker_StreamU_2(ahead, world, this.init_JsonUtil_CachePool.floorY())
                  && (this.init_Z_NodeWalker_StreamU(ahead, c, world) || this.write(ahead, c, world))) {
                  return running;
               }
            } else if (this.read(s, cam, world)) {
               int[] keys = this.init_IArr_F(c.yaw(), s, fwd);
               JsonUtil.Inner_StreamUtil running = new JsonUtil.Inner_StreamUtil(fwd, 25.0F, keys[0], keys[1], false, keys[0] > 0, false, -1, false);
               if (init_Z_F_F(c.yaw(), fwd)
                  && !init_Z_NodeWalker_StreamU_2(
                     NodeWalker.init(s, init_NodeWalker_DataUti_JsonUtil_StreamUti_JsonUtil_DataUtil(running, c), world),
                     world,
                     this.init_JsonUtil_CachePool.floorY()
                  )) {
                  return this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, c, true);
               }

               return running;
            }
         }

         return this.load(s, cam, sneaking, world, next);
      } else {
         return this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, cam, world, u);
      }
   }

   private JsonUtil.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, NodeWalker.Inner_CachePool world, double u
   ) {
      int end = this.init_JsonUtil_CachePool.length();
      boolean beyond = !this.init_Z
         && load_Z_NodeWalker_CachePo(
            world, this.init_JsonUtil_CachePool.cellX(end + 1), this.init_JsonUtil_CachePool.floorY(), this.init_JsonUtil_CachePool.cellZ(end + 1)
         );
      double along = s.vx() * this.init_JsonUtil_CachePool.dx() + s.vz() * this.init_JsonUtil_CachePool.dz();
      boolean stopped = this.init_Z ? s.vx() * s.vx() + s.vz() * s.vz() < 9.0E-4 : Math.abs(along) < 0.03;
      if (!s.onGround() || !(u >= end) || !beyond && (this.init_Z || !(u >= end + 1)) && !stopped) {
         if (!s.onGround()) {
            return JsonUtil.Inner_StreamUtil.idle(cam.yaw(), cam.pitch());
         }

         if (u < end + (beyond ? 0.0 : 0.2)) {
            int[] keys = this.init_IArr_F(cam.yaw(), s, this.init_JsonUtil_CachePool.yaw());
            return new JsonUtil.Inner_StreamUtil(cam.yaw(), cam.pitch(), keys[0], keys[1], false, false, !beyond, -1, false);
         }

         int forward = 0;
         int left = 0;
         double best = s.vx() * s.vx() + s.vz() * s.vz();

         for (int f = -1; f <= 1; f++) {
            for (int l = -1; l <= 1; l++) {
               NodeWalker.Inner_StreamUtil next = NodeWalker.init(s, new NodeWalker.Inner_DataUtil(f, l, false, false, true, cam.yaw()), world);
               double speed = next.vx() * next.vx() + next.vz() * next.vz();
               if (speed < best
                  && this.init_JsonUtil_CachePool.along(next.x(), next.z()) >= end
                  && init_Z_NodeWalker_StreamU_2(next, world, this.init_JsonUtil_CachePool.floorY())
                  && this.write(next, cam, world)) {
                  best = speed;
                  forward = f;
                  left = l;
               }
            }
         }

         return new JsonUtil.Inner_StreamUtil(cam.yaw(), cam.pitch(), forward, left, false, false, true, -1, false);
      } else {
         return new JsonUtil.Inner_StreamUtil(cam.yaw(), cam.pitch(), 0, 0, false, false, false, -1, true);
      }
   }

   private JsonUtil.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil c, boolean jump) {
      int[] keys = this.init_IArr_F(c.yaw(), s, this.init_JsonUtil_CachePool.yaw());
      return new JsonUtil.Inner_StreamUtil(this.init_JsonUtil_CachePool.yaw(), 25.0F, keys[0], keys[1], jump, keys[0] > 0, false, -1, false);
   }

   private JsonUtil.Inner_StreamUtil init_JsonUtil_StreamUti_NodeWalker_StreamU(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world, int next
   ) {
      float fwd = this.init_JsonUtil_CachePool.yaw();
      if (!this.load_Z) {
         float[] goal = this.init_FArr_NodeWalker_StreamU(s, sneaking, next);
         JsonUtil.Inner_DataUtil c = cam.stepToward(goal[0], goal[1]);
         int place = this.init_Z_NodeWalker_StreamU(s, c, sneaking, world, next) ? next : -1;
         if (place >= 0 || this.init_JsonUtil_ConfigSto != JsonUtil.Inner_ConfigStore.init || !this.load(s, cam, world)) {
            int[] keys = this.init_IArr_F(c.yaw(), s, fwd);
            return new JsonUtil.Inner_StreamUtil(goal[0], goal[1], keys[0], keys[1], false, keys[0] > 0, false, place, false);
         }

         this.load_Z = true;
      }

      JsonUtil.Inner_DataUtil c = cam.stepToward(fwd, 25.0F);
      int[] keys = this.init_IArr_F(c.yaw(), s, fwd);
      return new JsonUtil.Inner_StreamUtil(fwd, 25.0F, keys[0], keys[1], false, keys[0] > 0, false, -1, false);
   }

   private JsonUtil.Inner_StreamUtil load(
      NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world, int next
   ) {
      double dip = next <= this.init_JsonUtil_CachePool.length() ? this.init_D_NodeWalker_CachePo_I(world, next) : 0.0 / 0.0;
      float[] goal = Double.isNaN(dip) ? this.init_FArr_NodeWalker_StreamU(s, sneaking, next) : this.init_FArr_NodeWalker_StreamU(s, sneaking, next, dip);
      JsonUtil.Inner_DataUtil c = cam.stepToward(goal[0], goal[1]);
      int place = this.init_Z_NodeWalker_StreamU(s, c, sneaking, world, next) ? next : -1;
      int[] keys = this.init_IArr_F(c.yaw(), s, this.init_JsonUtil_CachePool.yaw());
      if (!Double.isNaN(dip)
         && place < 0
         && !init_Z_NodeWalker_StreamU_2(
            NodeWalker.init(s, new NodeWalker.Inner_DataUtil(keys[0], keys[1], false, false, true, c.yaw()), world),
            world,
            this.init_JsonUtil_CachePool.floorY()
         )) {
         return new JsonUtil.Inner_StreamUtil(goal[0], goal[1], 0, 0, false, false, true, -1, false);
      }

      boolean sneak = false;
      if (place < 0 || ClassCache.Inner_CachePool.init()) {
         NodeWalker.Inner_StreamUtil plain = NodeWalker.init(s, new NodeWalker.Inner_DataUtil(keys[0], keys[1], false, false, false, c.yaw()), world);
         sneak = !init_Z_NodeWalker_StreamU_2(plain, world, this.init_JsonUtil_CachePool.floorY());
      }

      return new JsonUtil.Inner_StreamUtil(goal[0], goal[1], keys[0], keys[1], false, false, sneak, place, false);
   }

   float[] init_FArr_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil s, boolean sneaking, int next) {
      double eyeY = s.y() + (sneaking ? 1.27 : 1.62);
      double w = this.init_JsonUtil_CachePool.lateral(s.x(), s.z());
      double target = Math.max(-0.35, Math.min(0.35, w));
      double[] face = this.init_JsonUtil_CachePool.point(next, target);
      double[] eye = this.init_JsonUtil_CachePool.along(s.x(), s.z()) - next < 0.12
         ? this.init_JsonUtil_CachePool.point(next + 0.12, w)
         : new double[]{s.x(), s.z()};
      double across = Math.hypot(face[0] - eye[0], face[1] - eye[1]);
      float yaw = !(Math.abs(w - target) < 1.0E-6) && !(across < 1.0E-6)
         ? NodeWalker.init_F_D(eye[0], eye[1], face[0], face[1])
         : init_F_F(this.init_JsonUtil_CachePool.yaw() + 179.5F);
      float pitch = (float)Math.toDegrees(Math.atan2(eyeY - (this.init_JsonUtil_CachePool.floorY() + 0.5), Math.max(0.12, across)));
      return new float[]{yaw, Math.min(89.5F, pitch)};
   }

   float[] init_FArr_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil s, boolean sneaking, int next, double top) {
      double eyeY = s.y() + (sneaking ? 1.27 : 1.62);
      double tx = this.init_JsonUtil_CachePool.cellX(next) + 0.5;
      double tz = this.init_JsonUtil_CachePool.cellZ(next) + 0.5;
      float yaw = NodeWalker.init_F_D(s.x(), s.z(), tx, tz);
      float pitch = (float)Math.toDegrees(Math.atan2(eyeY - top, Math.max(0.12, Math.hypot(tx - s.x(), tz - s.z()))));
      return new float[]{yaw, Math.min(89.5F, pitch)};
   }

   boolean init_Z_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil c, boolean sneaking, NodeWalker.Inner_CachePool world, int next) {
      if (!this.write_Z) {
         return false;
      } else {
         Vec3 eye = new Vec3(s.x(), s.y() + (sneaking ? 1.27 : 1.62), s.z());
         int[] hit = init_IArr_NodeWalker_CachePo(world, eye, c.yaw(), c.pitch(), 4.5);
         if (hit == null) {
            return false;
         } else {
            int px = hit[0] + hit[3];
            int py = hit[1] + hit[4];
            int pz = hit[2] + hit[5];
            if (px == this.init_JsonUtil_CachePool.cellX(next) && py == this.init_JsonUtil_CachePool.floorY() && pz == this.init_JsonUtil_CachePool.cellZ(next)
               )
             {
               AABB cell = new AABB(px, py, pz, px + 1, py + 1, pz + 1);
               return !cell.intersects(s.box(sneaking).deflate(1.0E-7));
            } else {
               return false;
            }
         }
      }
   }

   boolean init_Z_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil c, NodeWalker.Inner_CachePool world) {
      JsonUtil.Inner_StreamUtil jump = this.init_JsonUtil_StreamUti_NodeWalker_StreamU(s, c, true);
      NodeWalker.Inner_StreamUtil st = NodeWalker.init(s, init_NodeWalker_DataUti_JsonUtil_StreamUti_JsonUtil_DataUtil(jump, c), world);
      return this.init_Z_NodeWalker_StreamU(st, c, false, world);
   }

   boolean load(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, NodeWalker.Inner_CachePool world) {
      float fwd = this.init_JsonUtil_CachePool.yaw();
      JsonUtil.Inner_DataUtil c = cam;
      NodeWalker.Inner_StreamUtil st = s;

      for (int t = 0; t < 30; t++) {
         c = c.stepToward(fwd, 25.0F);
         int[] keys = this.init_IArr_F(c.yaw(), st, fwd);
         st = NodeWalker.init(st, new NodeWalker.Inner_DataUtil(keys[0], keys[1], false, keys[0] > 0, false, c.yaw()), world);
         if (st.onGround()) {
            return this.init_Z_NodeWalker_StreamU_NodeWalker_CachePo(st, world) && this.write(st, c, world);
         }

         if (st.y() < this.init_JsonUtil_CachePool.floorY()) {
            return false;
         }
      }

      return false;
   }

   boolean read(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil cam, NodeWalker.Inner_CachePool world) {
      float fwd = this.init_JsonUtil_CachePool.yaw();
      JsonUtil.Inner_DataUtil c = cam;
      NodeWalker.Inner_StreamUtil st = s;

      for (int t = 0; t < 6; t++) {
         c = c.stepToward(fwd, 25.0F);
         if (st.onGround() && init_Z_F_F(c.yaw(), fwd) && this.init_Z_NodeWalker_StreamU(st, c, world)) {
            return true;
         }

         int[] keys = this.init_IArr_F(c.yaw(), st, fwd);
         st = NodeWalker.init(st, new NodeWalker.Inner_DataUtil(keys[0], keys[1], false, keys[0] > 0, false, c.yaw()), world);
         if (!init_Z_NodeWalker_StreamU_2(st, world, this.init_JsonUtil_CachePool.floorY())) {
            return false;
         }
      }

      return false;
   }

   boolean write(NodeWalker.Inner_StreamUtil s, JsonUtil.Inner_DataUtil c, NodeWalker.Inner_CachePool world) {
      NodeWalker.Inner_StreamUtil st = s;

      for (int t = 0; t < 8; t++) {
         st = NodeWalker.init(st, new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, true, c.yaw()), world);
         if (!init_Z_NodeWalker_StreamU_2(st, world, this.init_JsonUtil_CachePool.floorY())) {
            return false;
         }
      }

      return true;
   }

   private boolean init_Z_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil st, JsonUtil.Inner_DataUtil cam, boolean sneaking, NodeWalker.Inner_CachePool world) {
      JsonUtil.Inner_BufferHelper placed = new JsonUtil.Inner_BufferHelper(world);
      JsonUtil shadow = new JsonUtil(this.init_JsonUtil_CachePool, this.init_JsonUtil_ConfigSto);
      shadow.read_Z = this.read_Z;
      JsonUtil.Inner_DataUtil c = cam;

      for (int t = 0; t < 30; t++) {
         if (st.onGround()) {
            return this.init_Z_NodeWalker_StreamU_NodeWalker_CachePo(st, placed) && this.write(st, c, placed);
         }

         if (st.y() < this.init_JsonUtil_CachePool.floorY()) {
            return false;
         }

         int next = shadow.init_I_NodeWalker_CachePo(placed);
         JsonUtil.Inner_StreamUtil cmd = next > this.init_JsonUtil_CachePool.length()
            ? shadow.load_JsonUtil_StreamUti_NodeWalker_StreamU(st, c, sneaking, placed)
            : shadow.init_JsonUtil_StreamUti_NodeWalker_StreamU(st, c, sneaking, placed, next);
         c = c.stepToward(cmd.yaw(), cmd.pitch());
         if (cmd.place() > 0) {
            placed.init_V(
               this.init_JsonUtil_CachePool.cellX(cmd.place()), this.init_JsonUtil_CachePool.floorY(), this.init_JsonUtil_CachePool.cellZ(cmd.place())
            );
         }

         st = NodeWalker.init(st, init_NodeWalker_DataUti_JsonUtil_StreamUti_JsonUtil_DataUtil(cmd, c), placed);
      }

      return false;
   }

   private boolean init_Z_NodeWalker_StreamU_NodeWalker_CachePo(NodeWalker.Inner_StreamUtil st, NodeWalker.Inner_CachePool world) {
      if (st.onGround() && !(st.y() < this.init_JsonUtil_CachePool.floorY() + 1 - 0.01)) {
         int next = this.init_I_NodeWalker_CachePo(world);
         double u = this.init_JsonUtil_CachePool.along(st.x(), st.z());
         if (next <= this.init_JsonUtil_CachePool.length() && !(u < next + 1)) {
            int under = (int)Math.floor(u - 0.3);
            return under > next
               && load_Z_NodeWalker_CachePo(
                  world, this.init_JsonUtil_CachePool.cellX(under), this.init_JsonUtil_CachePool.floorY(), this.init_JsonUtil_CachePool.cellZ(under)
               );
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean init_Z_NodeWalker_StreamU_2(NodeWalker.Inner_StreamUtil st, NodeWalker.Inner_CachePool world, int floorY) {
      if (!(st.y() < floorY + 1 - 0.01) && st.onGround()) {
         AABB box = st.box(false);
         AABB under = new AABB(box.minX, box.minY - 0.6, box.minZ, box.maxX, box.minY, box.maxZ);

         for (AABB b : world.init_List(under)) {
            if (b.intersects(under) && b.maxY >= box.minY - 0.001) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   double init_D_NodeWalker_CachePo_I(NodeWalker.Inner_CachePool world, int i) {
      int x = this.init_JsonUtil_CachePool.cellX(i);
      int y = this.init_JsonUtil_CachePool.floorY();
      int z = this.init_JsonUtil_CachePool.cellZ(i);
      AABB cell = new AABB(x, y, z, x + 1, y + 1, z + 1).deflate(1.0E-4);
      double top = 0.0 / 0.0;

      for (AABB b : world.init_List(cell)) {
         if (b.intersects(cell) && b.maxY < y + 1 - 0.001 && !(top >= b.maxY)) {
            top = b.maxY;
         }
      }

      return top;
   }

   int[] init_IArr_F(float yaw, NodeWalker.Inner_StreamUtil s, float heading) {
      double w = this.init_JsonUtil_CachePool.lateral(s.x(), s.z());
      double hr = Math.toRadians(heading);
      double vx = -Math.sin(hr);
      double vz = Math.cos(hr);
      if (this.read_Z) {
         double lx = Math.cos(Math.toRadians(this.init_JsonUtil_CachePool.yaw()));
         double lz = Math.sin(Math.toRadians(this.init_JsonUtil_CachePool.yaw()));
         vx -= Math.signum(w) * lx * 0.75;
         vz -= Math.signum(w) * lz * 0.75;
      }

      double r = Math.toRadians(yaw);
      double fx = -Math.sin(r);
      double fz = Math.cos(r);
      double lx = Math.cos(r);
      double lz = Math.sin(r);
      double forward = vx * fx + vz * fz;
      double left = vx * lx + vz * lz;
      double length = Math.hypot(vx, vz);
      forward /= length;
      left /= length;
      double band = Math.cos(Math.toRadians(67.5));
      return new int[]{forward > band ? 1 : (forward < -band ? -1 : 0), left > band ? 1 : (left < -band ? -1 : 0)};
   }

   static NodeWalker.Inner_DataUtil init_NodeWalker_DataUti_JsonUtil_StreamUti_JsonUtil_DataUtil(JsonUtil.Inner_StreamUtil cmd, JsonUtil.Inner_DataUtil c) {
      return new NodeWalker.Inner_DataUtil(cmd.forward(), cmd.left(), cmd.jump(), cmd.sprint(), cmd.sneak(), c.yaw());
   }

   static boolean init_Z_F_F(float yaw, float heading) {
      return Math.abs(init_F_F(yaw - heading)) <= 12.0F;
   }

   static float init_F_F(float degrees) {
      float d = degrees % 360.0F;
      if (d >= 180.0F) {
         d -= 360.0F;
      }

      if (d < -180.0F) {
         d += 360.0F;
      }

      return d;
   }

   static float init_F_F_F(float v, float cap) {
      return Math.max(-cap, Math.min(cap, v));
   }

   static boolean load_Z_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, int x, int y, int z) {
      AABB cell = new AABB(x, y, z, x + 1, y + 1, z + 1).deflate(1.0E-4);

      for (AABB b : world.init_List(cell)) {
         if (b.intersects(cell)) {
            return true;
         }
      }

      return false;
   }

   static int[] init_IArr_NodeWalker_CachePo(NodeWalker.Inner_CachePool world, Vec3 eye, float yaw, float pitch, double reach) {
      double yr = Math.toRadians(yaw);
      double pr = Math.toRadians(pitch);
      double dx = -Math.sin(yr) * Math.cos(pr);
      double dy = -Math.sin(pr);
      double dz = Math.cos(yr) * Math.cos(pr);
      Vec3 end = eye.add(dx * reach, dy * reach, dz * reach);
      AABB sweep = new AABB(eye, end).inflate(0.001);
      double best = 1.7976931348623157E308;
      int[] out = null;

      for (AABB b : world.init_List(sweep)) {
         double[] tFace = init_DArr_AABB(b, eye, dx, dy, dz);
         if (tFace != null && !(tFace[0] < 0.0) && !(tFace[0] > reach) && !(tFace[0] >= best)) {
            best = tFace[0];
            int axis = (int)tFace[1];
            int sign = (int)tFace[2];
            Vec3 at = eye.add(dx * best, dy * best, dz * best);
            double ix = at.x - (axis == 0 ? sign * 1.0E-4 : 0.0);
            double iy = at.y - (axis == 1 ? sign * 1.0E-4 : 0.0);
            double iz = at.z - (axis == 2 ? sign * 1.0E-4 : 0.0);
            out = new int[]{(int)Math.floor(ix), (int)Math.floor(iy), (int)Math.floor(iz), axis == 0 ? sign : 0, axis == 1 ? sign : 0, axis == 2 ? sign : 0};
         }
      }

      return out;
   }

   private static double[] init_DArr_AABB(AABB b, Vec3 o, double dx, double dy, double dz) {
      double tMin = -1.7976931348623157E308;
      double tMax = 1.7976931348623157E308;
      int axis = -1;
      int sign = 0;
      double[] origin = new double[]{o.x, o.y, o.z};
      double[] dir = new double[]{dx, dy, dz};
      double[] lo = new double[]{b.minX, b.minY, b.minZ};
      double[] hi = new double[]{b.maxX, b.maxY, b.maxZ};

      for (int a = 0; a < 3; a++) {
         if (Math.abs(dir[a]) < 1.0E-12) {
            if (origin[a] < lo[a] || origin[a] > hi[a]) {
               return null;
            }
         } else {
            double t1 = (lo[a] - origin[a]) / dir[a];
            double t2 = (hi[a] - origin[a]) / dir[a];
            int s1 = -1;
            if (t1 > t2) {
               double tmp = t1;
               t1 = t2;
               t2 = tmp;
               s1 = 1;
            }

            if (t1 > tMin) {
               tMin = t1;
               axis = a;
               sign = s1;
            }

            tMax = Math.min(tMax, t2);
            if (tMin > tMax) {
               return null;
            }
         }
      }

      return axis >= 0 && !(tMax < 0.0) ? new double[]{tMin, axis, sign} : null;
   }

   static final class Inner_BufferHelper implements NodeWalker.Inner_CachePool {
      private final NodeWalker.Inner_CachePool init_NodeWalker_CachePo;
      private final List<AABB> init_List = new ArrayList<>();

      Inner_BufferHelper(NodeWalker.Inner_CachePool base) {
         this.init_NodeWalker_CachePo = base;
      }

      void init_V(int x, int y, int z) {
         this.init_List.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
      }

      @Override
      public List<AABB> init_List(AABB query) {
         List<AABB> out = this.init_NodeWalker_CachePo.init_List(query);
         if (this.init_List.isEmpty()) {
            return out;
         }

         List<AABB> all = new ArrayList<>(out);

         for (AABB b : this.init_List) {
            if (b.intersects(query)) {
               all.add(b);
            }
         }

         return all;
      }

      @Override
      public double init_D_I(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.init_D_I(x, y, z);
      }

      @Override
      public double load_D(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.load_D(x, y, z);
      }

      @Override
      public double read_D(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.read_D(x, y, z);
      }

      @Override
      public double write_D(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.write_D(x, y, z);
      }

      @Override
      public boolean init_Z_I(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.init_Z_I(x, y, z);
      }

      @Override
      public boolean load_Z(int x, int y, int z) {
         return this.init_NodeWalker_CachePo.load_Z(x, y, z);
      }
   }

   public record Inner_CachePool(int startX, int startZ, int floorY, int dx, int dz, int length) {
      public Inner_CachePool {
         if (Math.abs(dx) + Math.abs(dz) != 1) {
            throw new IllegalArgumentException("a lane is cardinal");
         }

         if (length < 1) {
            throw new IllegalArgumentException("a lane places at least one support");
         }
      }

      public int cellX(int i) {
         return this.startX + this.dx * i;
      }

      public int cellZ(int i) {
         return this.startZ + this.dz * i;
      }

      public float yaw() {
         return NodeWalker.init_F_D(0.0, 0.0, this.dx, this.dz);
      }

      public double along(double x, double z) {
         if (this.dx == 1) {
            return x - this.startX;
         } else if (this.dx == -1) {
            return this.startX + 1 - x;
         } else {
            return this.dz == 1 ? z - this.startZ : this.startZ + 1 - z;
         }
      }

      public double[] point(double u, double lateral) {
         double lx = Math.cos(Math.toRadians(this.yaw()));
         double lz = Math.sin(Math.toRadians(this.yaw()));
         return new double[]{this.startX + 0.5 + this.dx * (u - 0.5) + lx * lateral, this.startZ + 0.5 + this.dz * (u - 0.5) + lz * lateral};
      }

      public double lateral(double x, double z) {
         double lx = Math.cos(Math.toRadians(this.yaw()));
         double lz = Math.sin(Math.toRadians(this.yaw()));
         return (x - (this.startX + 0.5)) * lx + (z - (this.startZ + 0.5)) * lz;
      }
   }

   public enum Inner_ConfigStore {
      init,
      load;
   }

   public record Inner_DataUtil(float yaw, float pitch) {
      JsonUtil.Inner_DataUtil stepToward(float goalYaw, float goalPitch) {
         float dy = JsonUtil.init_F_F(goalYaw - this.yaw);
         float dp = goalPitch - this.pitch;
         return new JsonUtil.Inner_DataUtil(this.yaw + JsonUtil.init_F_F_F(dy, 100.0F), this.pitch + JsonUtil.init_F_F_F(dp, 100.0F));
      }
   }

   public record Inner_StreamUtil(float yaw, float pitch, int forward, int left, boolean jump, boolean sprint, boolean sneak, int place, boolean done) {
      static JsonUtil.Inner_StreamUtil idle(float yaw, float pitch) {
         return new JsonUtil.Inner_StreamUtil(yaw, pitch, 0, 0, false, false, false, -1, false);
      }
   }
}
