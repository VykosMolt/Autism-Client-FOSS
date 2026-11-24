package autismclient.modules;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class Z {
   private static final AtomicBoolean init = new AtomicBoolean();

   private Z() {
   }

   static void init() {
      if (init.compareAndSet(false, true)) {
         Thread thread = new Thread(Z::load, "KBDisplacement warm-up");
         thread.setDaemon(true);
         thread.setPriority(1);
         thread.start();
      }
   }

   private static void load() {
      try {
         AABB body = new AABB(0.2, 10.0, 0.2, 0.8, 11.8, 0.8);
         W.Inner_SyncManager counter = W.Inner_SyncManager.init(180.0F, 0.13, 0.026);
         W.Inner_BufferHelper hypotheses = new W.Inner_BufferHelper(counter, new Vec3[]{new Vec3(0.0, -0.0784, 0.0)});

         for (int run = 0; run < 400; run++) {
            int width = 1 + 2 * (run % 3);
            Z.Inner_DataUtil world = new Z.Inner_DataUtil(width);
            W.Inner_ConfigStore input = new W.Inner_ConfigStore(body, new Vec3(0.5, 10.0, -0.8), new Vec3(0.0, -0.0784, 0.0), true, 0.0, 0.5, 10.0, false);
            W.init(input, 0.0F, 0.15, new W.Inner_StateCache(100.0, 3.0, false, 0, 1 + run % 10), hypotheses, world);
            if ((run & 31) == 31) {
               Thread.sleep(5L);
            }
         }
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
      } catch (RuntimeException | LinkageError var8) {
      }
   }

   private static final class Inner_DataUtil implements W.Inner_UiHelper {
      final AABB init_AABB;
      final List<VoxelShape> init_List;

      Inner_DataUtil(int width) {
         this.init_AABB = new AABB(0.5 - width / 2.0, 9.0, -40.0, 0.5 + width / 2.0, 10.0, 40.0);
         this.init_List = List.of(Shapes.create(this.init_AABB));
      }

      @Override
      public boolean init_Z_AABB(AABB area) {
         return true;
      }

      @Override
      public Vec3 init_Vec3_AABB_Vec3(AABB body, Vec3 v) {
         double y = Shapes.collide(Axis.Y, body, this.init_List, v.y);
         body = body.move(0.0, y, 0.0);
         double x;
         double z;
         if (Math.abs(v.x) < Math.abs(v.z)) {
            z = Shapes.collide(Axis.Z, body, this.init_List, v.z);
            body = body.move(0.0, 0.0, z);
            x = Shapes.collide(Axis.X, body, this.init_List, v.x);
         } else {
            x = Shapes.collide(Axis.X, body, this.init_List, v.x);
            body = body.move(x, 0.0, 0.0);
            z = Shapes.collide(Axis.Z, body, this.init_List, v.z);
         }

         return new Vec3(x, y, z);
      }

      @Override
      public boolean load_Z_AABB(AABB body) {
         return false;
      }

      private boolean init_Z_D(double minX, double maxX, double minZ, double maxZ) {
         return maxX > this.init_AABB.minX + 0.001
            && minX < this.init_AABB.maxX - 0.001
            && maxZ > this.init_AABB.minZ + 0.001
            && minZ < this.init_AABB.maxZ - 0.001;
      }

      @Override
      public W.Inner_CachePool init_W_CachePool_AABB(AABB body) {
         boolean under = this.init_AABB.maxY <= body.minY + 0.01 && this.init_Z_D(body.minX, body.maxX, body.minZ, body.maxZ);
         return under ? new W.Inner_CachePool(this.init_AABB.maxY, false, false, true) : new W.Inner_CachePool(0.0, true, false, true);
      }

      @Override
      public double init() {
         return 0.0;
      }

      @Override
      public V.Inner_StreamUtil init_V_StreamUtil_I(int x, int z, int base, double stepTop) {
         boolean cell = this.init_Z_D(x, x + 1, z, z + 1);
         long walls = cell ? V.init_J_I(base, this.init_AABB.minY, this.init_AABB.maxY) : 0L;
         return new V.Inner_StreamUtil(walls, 0L, cell ? this.init_AABB.maxY : -1.0 / 0.0, (byte)(cell ? 1 : 2), 0.6F);
      }
   }
}
