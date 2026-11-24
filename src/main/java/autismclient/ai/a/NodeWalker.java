package autismclient.ai.a;

import autismclient.mixin.accessor.AutismLivingEntityAccessor;
import it.unimi.dsi.fastutil.floats.FloatArraySet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class NodeWalker {
   public static final double init_D = 0.6;
   public static final double load_D = 1.8;
   public static final double read_D = 1.5;
   public static final double write_D = 0.6;
   public static final double update_D = 0.6;
   public static final int init_I = 0;
   public static final int load_I = 1;
   public static final int read_I = 2;
   public static final int write_I = 3;
   public static final int update_I = 4;
   public static final double sync_D = 0.08;
   public static final double flush_D = 0.42;
   public static final double close = 0.2;
   public static final double open = 0.1;
   public static final double poll = 1.3;
   public static final double push = 0.02;
   public static final double pop = 0.026;
   public static final double tick = 0.6;
   public static final double run = 0.98;
   public static final double get = 0.8;
   private static final double set = 1.0E-7;
   public static final int sync_I = 1;
   public static final int flush_I = 2;
   private static final double put = Math.cos(0.13962633907794952);

   private NodeWalker() {
   }

   public static NodeWalker.Inner_CachePool init_NodeWalker_CachePo_ClientLevel_Entity(ClientLevel level, Entity body) {
      return new BitSetUtil(level, body);
   }

   static boolean init_Z(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil from, NodeWalker.Inner_StreamUtil to) {
      return init_Z_NodeWalker_CachePo(world, from, to, false);
   }

   static boolean load(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil from, NodeWalker.Inner_StreamUtil to) {
      return init_Z_NodeWalker_CachePo(world, from, to, true);
   }

   static int[] init_IArr(NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil from, NodeWalker.Inner_StreamUtil to) {
      double half = 0.33999999999999997;
      double x0 = Math.min(from.x(), to.x()) - half;
      double x1 = Math.max(from.x(), to.x()) + half;
      double feet = Math.min(from.y(), to.y());
      double y0 = feet - 1.0E-5;
      double y1 = Math.max(from.y(), to.y()) + 1.8;
      int bodyBottom = (int)Math.floor(feet + 1.0E-5);
      double z0 = Math.min(from.z(), to.z()) - half;
      double z1 = Math.max(from.z(), to.z()) + half;

      for (int x = (int)Math.floor(x0); x <= (int)Math.floor(x1 - 1.0E-5); x++) {
         for (int y = (int)Math.floor(y0); y <= (int)Math.floor(y1 - 1.0E-5); y++) {
            for (int z = (int)Math.floor(z0); z <= (int)Math.floor(z1 - 1.0E-5); z++) {
               boolean touched = load(from, x, y, z);
               if (!touched && (y < bodyBottom ? world.update(x, y, z) : world.write_Z(x, y, z))) {
                  return new int[]{x, y, z, 1};
               }

               if (y >= bodyBottom && world.init_Z_I(x, y, z) && (!touched || init_Z_NodeWalker_StreamU(to, x, y, z))) {
                  return new int[]{x, y, z, 0};
               }
            }
         }
      }

      return null;
   }

   private static boolean init_Z_NodeWalker_StreamU(NodeWalker.Inner_StreamUtil to, int x, int y, int z) {
      double half = 0.33999999999999997;
      return x >= (int)Math.floor(to.x() - half)
         && x <= (int)Math.floor(to.x() + half - 1.0E-5)
         && z >= (int)Math.floor(to.z() - half)
         && z <= (int)Math.floor(to.z() + half - 1.0E-5)
         && y >= (int)Math.floor(to.y() - 0.001)
         && y <= (int)Math.floor(to.y() + 1.8 - 1.0E-5);
   }

   private static boolean load(NodeWalker.Inner_StreamUtil from, int x, int y, int z) {
      double half = 0.33999999999999997;
      return x >= (int)Math.floor(from.x() - half)
         && x <= (int)Math.floor(from.x() + half - 1.0E-5)
         && z >= (int)Math.floor(from.z() - half)
         && z <= (int)Math.floor(from.z() + half - 1.0E-5)
         && y >= (int)Math.floor(from.y() - 0.001)
         && y <= (int)Math.floor(from.y() + 1.8 - 1.0E-5);
   }

   private static boolean init_Z_NodeWalker_CachePo(
      NodeWalker.Inner_CachePool world, NodeWalker.Inner_StreamUtil from, NodeWalker.Inner_StreamUtil to, boolean includeWater
   ) {
      double half = 0.33999999999999997;
      double x0 = Math.min(from.x(), to.x()) - half;
      double x1 = Math.max(from.x(), to.x()) + half;
      double feet = Math.min(from.y(), to.y());
      double y0 = feet - 1.0E-5;
      double y1 = Math.max(from.y(), to.y()) + 1.8;
      int bodyBottom = (int)Math.floor(feet + 1.0E-5);
      double z0 = Math.min(from.z(), to.z()) - half;
      double z1 = Math.max(from.z(), to.z()) + half;
      int minX = (int)Math.floor(x0);
      int maxX = (int)Math.floor(x1 - 1.0E-5);
      int minY = (int)Math.floor(y0);
      int maxY = (int)Math.floor(y1 - 1.0E-5);
      int minZ = (int)Math.floor(z0);
      int maxZ = (int)Math.floor(z1 - 1.0E-5);
      if (includeWater && world.init(minX, minY, minZ, maxX, maxY, maxZ, bodyBottom)) {
         return false;
      }

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               if ((y < bodyBottom ? world.update(x, y, z) : world.write_Z(x, y, z)) && (includeWater || !load(from, x, y, z))) {
                  return true;
               }

               if (y >= bodyBottom) {
                  if (world.init_Z_I(x, y, z) && (!load(from, x, y, z) || init_Z_NodeWalker_StreamU(to, x, y, z))) {
                     return true;
                  }

                  if (includeWater && world.load_Z(x, y, z)) {
                     return true;
                  }
               }
            }
         }
      }

      return false;
   }

   static double init_D_I(int hold) {
      return hold == 1 ? 0.25 : (hold == 2 ? 0.8 : 1.0);
   }

   static double load(int hold) {
      return hold == 1 ? 0.05 : (hold == 2 ? 0.75 : 1.0);
   }

   public static NodeWalker.Inner_StreamUtil init(NodeWalker.Inner_StreamUtil s, NodeWalker.Inner_DataUtil in, NodeWalker.Inner_CachePool world) {
      return init_NodeWalker_StreamU_NodeWalker_StreamU(s, in, world, true);
   }

   public static NodeWalker.Inner_StreamUtil load(NodeWalker.Inner_StreamUtil s, NodeWalker.Inner_DataUtil in, NodeWalker.Inner_CachePool world) {
      return init_NodeWalker_StreamU_NodeWalker_StreamU(s, in, world, false);
   }

   private static NodeWalker.Inner_StreamUtil init_NodeWalker_StreamU_NodeWalker_StreamU(
      NodeWalker.Inner_StreamUtil s, NodeWalker.Inner_DataUtil in, NodeWalker.Inner_CachePool world, boolean holdEdge
   ) {
      long support = s.onGround()
         ? (s.support() != -9223372036854775808L ? s.support() : init(s.box(false), s.x(), s.y(), s.z(), world))
         : -9223372036854775808L;
      int jumpDelay = Math.max(0, s.jumpDelay() - 1);
      double vx = s.vx();
      double vy = s.vy();
      double vz = s.vz();
      if (vx * vx + vz * vz < 9.0E-6) {
         vx = 0.0;
         vz = 0.0;
      }

      if (Math.abs(vy) < 0.003) {
         vy = 0.0;
      }

      boolean down = s.pose() != 0;
      boolean sprint = in.sprint() && in.forward() > 0.8 && !in.sneak() && !down && !s.sprintCut();
      if (in.jump()) {
         if (s.onGround() && jumpDelay == 0) {
            double jumpFactor = world.write_D((int)Math.floor(s.x()), (int)Math.floor(s.y()), (int)Math.floor(s.z()));
            if (jumpFactor == 1.0) {
               jumpFactor = world.write_D(init_I_J_D(support, s.x()), (int)Math.floor(s.y() - 0.5000001), load(support, s.z()));
            }

            vy = Math.max(0.42 * jumpFactor, vy);
            if (sprint) {
               double rad = Math.toRadians(in.yaw());
               vx += -Math.sin(rad) * 0.2;
               vz += Math.cos(rad) * 0.2;
            }

            jumpDelay = 10;
         }
      } else {
         jumpDelay = 0;
      }

      int bx = init_I_J_D(support, s.x());
      int by = (int)Math.floor(s.y() - 0.5000001);
      int bz = load(support, s.z());
      double friction = s.onGround() ? world.init_D_I(bx, by, bz) : 0.6;
      double drag = s.onGround() ? friction * 0.91 : 0.91;
      double speed = s.onGround() ? world.load() * (sprint ? 1.3 : 1.0) * (0.21600002 / (friction * friction * friction)) : (sprint ? 0.026 : 0.02);
      double forward = in.forward() * 0.98;
      double strafe = in.strafe() * 0.98;
      if (s.pose() != 0) {
         forward *= 0.3;
         strafe *= 0.3;
      }

      double len2 = forward * forward + strafe * strafe;
      if (len2 >= 1.0E-7) {
         double scale = len2 > 1.0 ? speed / Math.sqrt(len2) : speed;
         double fx = strafe * scale;
         double fz = forward * scale;
         double rad = Math.toRadians(in.yaw());
         double sin = Math.sin(rad);
         double cos = Math.cos(rad);
         vx += fx * cos - fz * sin;
         vz += fz * cos + fx * sin;
      }

      AABB box = s.box(in.sneak());
      Vec3 wanted = new Vec3(vx, vy, vz);
      if (s.stuck() != 0) {
         wanted = new Vec3(vx * init_D_I(s.stuck()), vy * load(s.stuck()), vz * init_D_I(s.stuck()));
      }

      if (holdEdge && in.sneak() && s.onGround() && vy <= 0.0) {
         wanted = init(wanted, box, world);
      }

      Vec3 moved = init_Vec3_Vec3(wanted, box, s.onGround(), world);
      double nx = s.x() + moved.x;
      double ny = s.y() + moved.y;
      double nz = s.z() + moved.z;
      boolean hitX = !init_Z_D_D(wanted.x, moved.x);
      boolean hitZ = !init_Z_D_D(wanted.z, moved.z);
      boolean hitY = !init_Z_D_D(wanted.y, moved.y);
      boolean onGround = hitY && wanted.y < 0.0;
      if (hitX) {
         vx = 0.0;
      }

      if (hitZ) {
         vz = 0.0;
      }

      AABB after = new AABB(nx - 0.3, ny, nz - 0.3, nx + 0.3, ny + (box.maxY - box.minY), nz + 0.3);
      long newSupport = onGround ? init(after, nx, ny, nz, world) : -9223372036854775808L;
      if (onGround && newSupport == -9223372036854775808L) {
         newSupport = init(after.move(-moved.x, 0.0, -moved.z), nx, ny, nz, world);
      }

      int fx = init_I_J_D(newSupport, nx);
      int fy = (int)Math.floor(ny - 0.2);
      int fz = load(newSupport, nz);
      if (hitY) {
         double restitution = onGround && !in.sneak() && -wanted.y >= 0.08 ? world.read_D(fx, fy, fz) : 0.0;
         if (restitution > 0.0) {
            double portion = moved.y / wanted.y;
            vy = (portion * 0.08 - wanted.y) * (1.0 + portion * -0.020000000000000018) * restitution;
         } else {
            vy = 0.0;
         }
      }

      if (onGround && !in.sneak() && Math.abs(vy) < 0.1 && world.read_D(fx, fy, fz) >= 0.9) {
         double slow = 0.4 + Math.abs(vy) * 0.2;
         vx *= slow;
         vz *= slow;
      }

      double factor = world.load_D((int)Math.floor(nx), (int)Math.floor(ny), (int)Math.floor(nz));
      if (factor == 1.0) {
         factor = world.load_D(init_I_J_D(newSupport, nx), (int)Math.floor(ny - 0.5000001), load(newSupport, nz));
      }

      vx *= factor;
      vz *= factor;
      if (s.stuck() != 0) {
         vx = 0.0;
         vy = 0.0;
         vz = 0.0;
      }

      vy -= 0.08;
      vx *= drag;
      vy *= 0.98;
      vz *= drag;
      int hold = world.init_Z() ? world.init_I(box.expandTowards(moved).deflate(1.0E-5)) : 0;
      int nextPose = s.pose();
      AABB desired = new AABB(nx - 0.3, ny, nz - 0.3, nx + 0.3, ny + (in.sneak() ? 1.5 : 1.8), nz + 0.3).deflate(1.0E-7);
      if (!world.init_Z(desired)) {
         if (in.sneak()) {
            nextPose = onGround ? 4 : 1;
         } else {
            nextPose = s.pose() == 4 ? 3 : 0;
         }
      } else {
         AABB crouch = new AABB(nx - 0.3, ny, nz - 0.3, nx + 0.3, ny + 1.5, nz + 0.3).deflate(1.0E-7);
         if (!in.sneak() && !world.init_Z(crouch)) {
            nextPose = 1;
         } else {
            AABB crawl = new AABB(nx - 0.3, ny, nz - 0.3, nx + 0.3, ny + 0.6, nz + 0.3).deflate(1.0E-7);
            if (!world.init_Z(crawl)) {
               nextPose = 2;
            }
         }
      }

      return new NodeWalker.Inner_StreamUtil(
         nx,
         ny,
         nz,
         vx,
         vy,
         vz,
         onGround,
         jumpDelay,
         hitX || hitZ,
         hitY,
         newSupport,
         (hitX || hitZ) && !init_Z_NodeWalker_DataUti_Vec3(in, moved),
         nextPose,
         hold
      );
   }

   static boolean init_Z_NodeWalker_DataUti_Vec3(NodeWalker.Inner_DataUtil in, Vec3 movement) {
      float rad = in.yaw() * 0.017453292F;
      double sin = Math.sin(rad);
      double cos = Math.cos(rad);
      double gx = in.strafe() * cos - in.forward() * sin;
      double gz = in.forward() * cos + in.strafe() * sin;
      double a2 = gx * gx + gz * gz;
      double m2 = movement.x * movement.x + movement.z * movement.z;
      if (!(a2 < 9.999999747378752E-6) && !(m2 < 9.999999747378752E-6)) {
         double cosine = (gx * movement.x + gz * movement.z) / Math.sqrt(a2 * m2);
         return cosine > put && cosine <= 1.0;
      } else {
         return false;
      }
   }

   static int init_I_J_D(long support, double x) {
      return support == -9223372036854775808L ? (int)Math.floor(x) : BlockPos.getX(support);
   }

   static int load(long support, double z) {
      return support == -9223372036854775808L ? (int)Math.floor(z) : BlockPos.getZ(support);
   }

   static long init(AABB body, double x, double y, double z, NodeWalker.Inner_CachePool world) {
      AABB under = new AABB(body.minX, body.minY - 1.0E-6, body.minZ, body.maxX, body.minY, body.maxZ);
      long best = -9223372036854775808L;
      double bestDist = 1.7976931348623157E308;
      int bx = 0;
      int by = 0;
      int bz = 0;

      for (AABB b : world.init_List(under)) {
         if (b.intersects(under)) {
            int cx = (int)Math.floor((b.minX + b.maxX) / 2.0);
            int cy = (int)Math.floor(b.minY + 1.0E-7);
            int cz = (int)Math.floor((b.minZ + b.maxZ) / 2.0);
            double dx = cx + 0.5 - x;
            double dy = cy + 0.5 - y;
            double dz = cz + 0.5 - z;
            double d = dx * dx + dy * dy + dz * dz;
            boolean greater = best != -9223372036854775808L && (cy != by ? cy > by : (cz != bz ? cz > bz : cx > bx));
            if (d < bestDist || d == bestDist && greater) {
               best = BlockPos.asLong(cx, cy, cz);
               bestDist = d;
               bx = cx;
               by = cy;
               bz = cz;
            }
         }
      }

      return best;
   }

   private static boolean init_Z_D_D(double a, double b) {
      return Math.abs(b - a) < 1.0E-5;
   }

   static Vec3 init_Vec3_Vec3(Vec3 wanted, AABB box, boolean onGround, NodeWalker.Inner_CachePool world) {
      if (wanted.lengthSqr() == 0.0) {
         return wanted;
      }

      List<AABB> shapes = world.init_List(box.expandTowards(wanted).expandTowards(0.0, -0.51, 0.0).inflate(1.0E-4));
      Vec3 result = init(wanted, box, shapes);
      boolean xHit = !init_Z_D_D(wanted.x, result.x);
      boolean zHit = !init_Z_D_D(wanted.z, result.z);
      boolean yHit = !init_Z_D_D(wanted.y, result.y);
      boolean landed = yHit && wanted.y < 0.0;
      if ((onGround || landed) && (xHit || zHit)) {
         AABB grounded = landed ? box.move(0.0, result.y, 0.0) : box;
         AABB stepArea = grounded.expandTowards(wanted.x, 0.6, wanted.z);
         if (!landed) {
            stepArea = stepArea.expandTowards(0.0, -1.0E-5, 0.0);
         }

         List<AABB> stepShapes = world.init_List(stepArea);
         FloatArraySet candidates = new FloatArraySet(4);

         for (AABB shape : stepShapes) {
            for (double boundary : new double[]{shape.minY, shape.maxY}) {
               float height = (float)(boundary - grounded.minY);
               if (height >= 0.0F && height != (float)result.y && height <= 0.6F) {
                  candidates.add(height);
               }
            }
         }

         float[] heights = candidates.toFloatArray();
         Arrays.sort(heights);

         for (float height : heights) {
            Vec3 stepped = init(new Vec3(wanted.x, height, wanted.z), grounded, stepShapes);
            if (stepped.horizontalDistanceSqr() > result.horizontalDistanceSqr()) {
               return stepped.subtract(0.0, box.minY - grounded.minY, 0.0);
            }
         }
      }

      return result;
   }

   static Vec3 init(Vec3 v, AABB box, List<AABB> shapes) {
      if (shapes.isEmpty()) {
         return v;
      }

      double dx = v.x;
      double dy = v.y;
      double dz = v.z;
      if (dy != 0.0) {
         dy = init_D_I(1, box, shapes, dy);
         if (dy != 0.0) {
            box = box.move(0.0, dy, 0.0);
         }
      }

      boolean zFirst = Math.abs(dx) < Math.abs(dz);
      if (zFirst && dz != 0.0) {
         dz = init_D_I(2, box, shapes, dz);
         if (dz != 0.0) {
            box = box.move(0.0, 0.0, dz);
         }
      }

      if (dx != 0.0) {
         dx = init_D_I(0, box, shapes, dx);
         if (!zFirst && dx != 0.0) {
            box = box.move(dx, 0.0, 0.0);
         }
      }

      if (!zFirst && dz != 0.0) {
         dz = init_D_I(2, box, shapes, dz);
      }

      return new Vec3(dx, dy, dz);
   }

   private static double init_D_I(int a, AABB box, List<AABB> shapes, double delta) {
      for (AABB s : shapes) {
         if (Math.abs(delta) < 1.0E-7) {
            return 0.0;
         }

         boolean var10000;
         switch (a) {
            case 0:
               var10000 = s.maxY > box.minY + 1.0E-7 && s.minY < box.maxY - 1.0E-7 && s.maxZ > box.minZ + 1.0E-7 && s.minZ < box.maxZ - 1.0E-7;
               break;
            case 1:
               var10000 = s.maxX > box.minX + 1.0E-7 && s.minX < box.maxX - 1.0E-7 && s.maxZ > box.minZ + 1.0E-7 && s.minZ < box.maxZ - 1.0E-7;
               break;
            default:
               var10000 = s.maxX > box.minX + 1.0E-7 && s.minX < box.maxX - 1.0E-7 && s.maxY > box.minY + 1.0E-7 && s.minY < box.maxY - 1.0E-7;
         }

         boolean overlap = var10000;
         if (overlap) {
            double sMin = a == 0 ? s.minX : (a == 1 ? s.minY : s.minZ);
            double sMax = a == 0 ? s.maxX : (a == 1 ? s.maxY : s.maxZ);
            double bMin = a == 0 ? box.minX : (a == 1 ? box.minY : box.minZ);
            double bMax = a == 0 ? box.maxX : (a == 1 ? box.maxY : box.maxZ);
            if (delta > 0.0 && sMin >= bMax - 1.0E-7) {
               delta = Math.min(delta, sMin - bMax);
            } else if (delta < 0.0 && sMax <= bMin + 1.0E-7) {
               delta = Math.max(delta, sMax - bMin);
            }
         }
      }

      return delta;
   }

   private static Vec3 init(Vec3 v, AABB box, NodeWalker.Inner_CachePool world) {
      double x = v.x;
      double z = v.z;

      while (x != 0.0 && !init_Z_AABB_NodeWalker_CachePo(box.move(x, 0.0, 0.0), world)) {
         x = init_D_D(x);
      }

      while (z != 0.0 && !init_Z_AABB_NodeWalker_CachePo(box.move(0.0, 0.0, z), world)) {
         z = init_D_D(z);
      }

      while (x != 0.0 && z != 0.0 && !init_Z_AABB_NodeWalker_CachePo(box.move(x, 0.0, z), world)) {
         x = init_D_D(x);
         z = init_D_D(z);
      }

      return new Vec3(x, v.y, z);
   }

   private static double init_D_D(double delta) {
      return Math.abs(delta) <= 0.05 ? 0.0 : delta - Math.copySign(0.05, delta);
   }

   private static boolean init_Z_AABB_NodeWalker_CachePo(AABB box, NodeWalker.Inner_CachePool world) {
      AABB below = new AABB(box.minX + 1.0E-7, box.minY - 0.6 - 1.0E-7, box.minZ + 1.0E-7, box.maxX - 1.0E-7, box.minY, box.maxZ - 1.0E-7);
      return world.init_Z(below);
   }

   public static List<NodeWalker.Inner_StreamUtil> init(
      NodeWalker.Inner_StreamUtil start, List<NodeWalker.Inner_DataUtil> inputs, NodeWalker.Inner_CachePool world
   ) {
      List<NodeWalker.Inner_StreamUtil> out = new ArrayList<>(inputs.size() + 1);
      out.add(start);
      NodeWalker.Inner_StreamUtil s = start;

      for (NodeWalker.Inner_DataUtil in : inputs) {
         s = init(s, in, world);
         out.add(s);
      }

      return out;
   }

   public static float init_F_D(double fromX, double fromZ, double toX, double toZ) {
      return (float)(Math.toDegrees(Math.atan2(toZ - fromZ, toX - fromX)) - 90.0);
   }

   public interface Inner_CachePool {
      default double init_D() {
         return 4.0;
      }

      default double load() {
         return 0.1;
      }

      List<AABB> init_List(AABB var1);

      default boolean init_Z(AABB query) {
         for (AABB box : this.init_List(query)) {
            if (box.intersects(query)) {
               return true;
            }
         }

         return false;
      }

      default double init_D_I(int x, int y, int z) {
         return 0.6;
      }

      default double load_D(int x, int y, int z) {
         return 1.0;
      }

      default boolean flush(int x, int y, int z) {
         return false;
      }

      default double read_D(int x, int y, int z) {
         return this.flush(x, y, z) ? 1.0 : 0.0;
      }

      default double write_D(int x, int y, int z) {
         return 1.0;
      }

      default boolean sync(int x, int y, int z) {
         return false;
      }

      default int init_I(AABB box) {
         return 0;
      }

      default boolean init_Z() {
         return false;
      }

      default boolean init_Z_I(int x, int y, int z) {
         return false;
      }

      default boolean load_Z(int x, int y, int z) {
         return false;
      }

      default boolean read_Z(int x, int y, int z) {
         return false;
      }

      default boolean write_Z(int x, int y, int z) {
         return this.read_Z(x, y, z);
      }

      default boolean update(int x, int y, int z) {
         return this.read_Z(x, y, z);
      }

      default boolean init(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, int floorBelow) {
         return false;
      }
   }

   public record Inner_DataUtil(double forward, double strafe, boolean jump, boolean sprint, boolean sneak, float yaw) {
      public static final NodeWalker.Inner_DataUtil NONE = new NodeWalker.Inner_DataUtil(0.0, 0.0, false, false, false, 0.0F);

      public static NodeWalker.Inner_DataUtil run(float yaw, boolean sprint) {
         return new NodeWalker.Inner_DataUtil(1.0, 0.0, false, sprint, false, yaw);
      }

      public static NodeWalker.Inner_DataUtil jump(float yaw, boolean sprint) {
         return new NodeWalker.Inner_DataUtil(1.0, 0.0, true, sprint, false, yaw);
      }

      public NodeWalker.Inner_DataUtil withStrafe(double s) {
         return new NodeWalker.Inner_DataUtil(this.forward, s, this.jump, this.sprint, this.sneak, this.yaw);
      }

      public NodeWalker.Inner_DataUtil withJump(boolean j) {
         return new NodeWalker.Inner_DataUtil(this.forward, this.strafe, j, this.sprint, this.sneak, this.yaw);
      }

      public NodeWalker.Inner_DataUtil withYaw(float y) {
         return new NodeWalker.Inner_DataUtil(this.forward, this.strafe, this.jump, this.sprint, this.sneak, y);
      }
   }

   public record Inner_StreamUtil(
      double x,
      double y,
      double z,
      double vx,
      double vy,
      double vz,
      boolean onGround,
      int jumpDelay,
      boolean horizontalCollision,
      boolean verticalCollision,
      long support,
      boolean sprintCut,
      int pose,
      int stuck
   ) {
      public static final long NO_SUPPORT = -9223372036854775808L;
      public static final double RESTING_VY = -0.0784;

      public Inner_StreamUtil(
         double x,
         double y,
         double z,
         double vx,
         double vy,
         double vz,
         boolean onGround,
         int jumpDelay,
         boolean horizontalCollision,
         boolean verticalCollision,
         long support,
         boolean sprintCut,
         int pose
      ) {
         this(x, y, z, vx, vy, vz, onGround, jumpDelay, horizontalCollision, verticalCollision, support, sprintCut, pose, 0);
      }

      public Inner_StreamUtil(
         double x, double y, double z, double vx, double vy, double vz, boolean onGround, int jumpDelay, boolean horizontalCollision, boolean verticalCollision
      ) {
         this(x, y, z, vx, vy, vz, onGround, jumpDelay, horizontalCollision, verticalCollision, -9223372036854775808L, false, 0);
      }

      public Inner_StreamUtil(
         double x,
         double y,
         double z,
         double vx,
         double vy,
         double vz,
         boolean onGround,
         int jumpDelay,
         boolean horizontalCollision,
         boolean verticalCollision,
         long support
      ) {
         this(x, y, z, vx, vy, vz, onGround, jumpDelay, horizontalCollision, verticalCollision, support, false, 0);
      }

      public Inner_StreamUtil(
         double x,
         double y,
         double z,
         double vx,
         double vy,
         double vz,
         boolean onGround,
         int jumpDelay,
         boolean horizontalCollision,
         boolean verticalCollision,
         long support,
         boolean sprintCut
      ) {
         this(x, y, z, vx, vy, vz, onGround, jumpDelay, horizontalCollision, verticalCollision, support, sprintCut, 0);
      }

      public static NodeWalker.Inner_StreamUtil of(Entity p) {
         Vec3 v = p.getDeltaMovement();
         int pose = p.getPose() == Pose.SWIMMING && !p.isInWater() ? 2 : (p.getPose() == Pose.CROUCHING ? 3 : 0);
         if (pose == 3 && p.onGround() && p instanceof LocalPlayer player && player.input.keyPresses.shift()) {
            pose = 4;
         }

         int jumpDelay = p instanceof AutismLivingEntityAccessor access ? access.autism$getNoJumpDelay() : 0;
         return new NodeWalker.Inner_StreamUtil(
            p.getX(),
            p.getY(),
            p.getZ(),
            v.x,
            v.y,
            v.z,
            p.onGround(),
            jumpDelay,
            p.horizontalCollision,
            p.verticalCollision,
            -9223372036854775808L,
            p.horizontalCollision && !p.minorHorizontalCollision,
            pose,
            stuckAt(p)
         );
      }

      static int stuckAt(Entity p) {
         AABB b = p.getBoundingBox().deflate(1.0E-5);
         Level level = p.level();
         int hold = 0;
         MutableBlockPos at = new MutableBlockPos();

         for (int x = (int)Math.floor(b.minX); x <= (int)Math.floor(b.maxX); x++) {
            for (int y = (int)Math.floor(b.minY); y <= (int)Math.floor(b.maxY); y++) {
               for (int z = (int)Math.floor(b.minZ); z <= (int)Math.floor(b.maxZ); z++) {
                  Block block = level.getBlockState(at.set(x, y, z)).getBlock();
                  if (block instanceof WebBlock) {
                     return 1;
                  }

                  if (block instanceof SweetBerryBushBlock) {
                     hold = 2;
                  }
               }
            }
         }

         return hold;
      }

      public NodeWalker.Inner_StreamUtil atRest() {
         return new NodeWalker.Inner_StreamUtil(
            this.x, this.y, this.z, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L, false, this.pose, this.stuck
         );
      }

      public boolean crawling() {
         return this.pose == 2;
      }

      public static NodeWalker.Inner_StreamUtil standing(double x, double y, double z) {
         return new NodeWalker.Inner_StreamUtil(x, y, z, 0.0, -0.0784, 0.0, true, 0, false, false, -9223372036854775808L);
      }

      public Vec3 position() {
         return new Vec3(this.x, this.y, this.z);
      }

      public double speed() {
         return Math.hypot(this.vx, this.vz);
      }

      public AABB box(boolean sneaking) {
         double h = this.pose == 2 ? 0.6 : (!sneaking && this.pose == 0 ? 1.8 : 1.5);
         return new AABB(this.x - 0.3, this.y, this.z - 0.3, this.x + 0.3, this.y + h, this.z + 0.3);
      }
   }
}
