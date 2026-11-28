package autismclient.modules;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class KBDisplacementWarmup {
   private static final AtomicBoolean init = new AtomicBoolean();

   private KBDisplacementWarmup() {
   }

   static void start() {
      if (init.compareAndSet(false, true)) {
         Thread thread = new Thread(KBDisplacementWarmup::load, "KBDisplacement warm-up");
         thread.setDaemon(true);
         thread.setPriority(1);
         thread.start();
      }
   }

   private static void load() {
      try {
         AABB body = new AABB(0.2, 10.0, 0.2, 0.8, 11.8, 0.8);
         KnockbackThrowPlanner.CounterSteer counter = KnockbackThrowPlanner.CounterSteer.forYaw(180.0F, 0.13, 0.026);
         KnockbackThrowPlanner.CounterHypotheses hypotheses = new KnockbackThrowPlanner.CounterHypotheses(counter, new Vec3[]{new Vec3(0.0, -0.0784, 0.0)});

         for (int run = 0; run < 400; run++) {
            int width = 1 + 2 * (run % 3);
            KBDisplacementWarmup.Inner_DataUtil world = new KBDisplacementWarmup.Inner_DataUtil(width);
            KnockbackThrowPlanner.ThrowInput input = new KnockbackThrowPlanner.ThrowInput(body, new Vec3(0.5, 10.0, -0.8), new Vec3(0.0, -0.0784, 0.0), true, 0.0, 0.5, 10.0, false);
            KnockbackThrowPlanner.plan(input, 0.0F, 0.15, new KnockbackThrowPlanner.PlanSettings(100.0, 3.0, false, 0, 1 + run % 10), hypotheses, world);
            if ((run & 31) == 31) {
               Thread.sleep(5L);
            }
         }
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
      } catch (RuntimeException | LinkageError var8) {
      }
   }

   private static final class Inner_DataUtil implements KnockbackThrowPlanner.ThrowWorld {
      final AABB platform;
      final List<VoxelShape> shapes;

      Inner_DataUtil(int width) {
         this.platform = new AABB(0.5 - width / 2.0, 9.0, -40.0, 0.5 + width / 2.0, 10.0, 40.0);
         this.shapes = List.of(Shapes.create(this.platform));
      }

      @Override
      public boolean regionLoaded(AABB area) {
         return true;
      }

      @Override
      public Vec3 collide(AABB body, Vec3 v) {
         double y = Shapes.collide(Axis.Y, body, this.shapes, v.y);
         body = body.move(0.0, y, 0.0);
         double x;
         double z;
         if (Math.abs(v.x) < Math.abs(v.z)) {
            z = Shapes.collide(Axis.Z, body, this.shapes, v.z);
            body = body.move(0.0, 0.0, z);
            x = Shapes.collide(Axis.X, body, this.shapes, v.x);
         } else {
            x = Shapes.collide(Axis.X, body, this.shapes, v.x);
            body = body.move(x, 0.0, 0.0);
            z = Shapes.collide(Axis.Z, body, this.shapes, v.z);
         }

         return new Vec3(x, y, z);
      }

      @Override
      public boolean load_Z_AABB(AABB body) {
         return false;
      }

      private boolean overlapsPlatform(double minX, double maxX, double minZ, double maxZ) {
         return maxX > this.platform.minX + 0.001
            && minX < this.platform.maxX - 0.001
            && maxZ > this.platform.minZ + 0.001
            && minZ < this.platform.maxZ - 0.001;
      }

      @Override
      public KnockbackThrowPlanner.FloorProbe probeFloor(AABB body) {
         boolean under = this.platform.maxY <= body.minY + 0.01 && this.overlapsPlatform(body.minX, body.maxX, body.minZ, body.maxZ);
         return under ? new KnockbackThrowPlanner.FloorProbe(this.platform.maxY, false, false, true) : new KnockbackThrowPlanner.FloorProbe(0.0, true, false, true);
      }

      @Override
      public double minY() {
         return 0.0;
      }

      @Override
      public TerrainDropGrid.Column column(int x, int z, int base, double stepTop) {
         boolean cell = this.overlapsPlatform(x, x + 1, z, z + 1);
         long walls = cell ? TerrainDropGrid.init_J_I(base, this.platform.minY, this.platform.maxY) : 0L;
         return new TerrainDropGrid.Column(walls, 0L, cell ? this.platform.maxY : -1.0 / 0.0, (byte)(cell ? 1 : 2), 0.6F);
      }
   }
}
