package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.ai.a.PacketUtil;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class r {
   static final double init = 1.0;
   static final double load = 0.3;
   static final double read = 400.0;

   private r() {
   }

   static AABB init(AABB wing) {
      return wing.inflate(4.0, 2.0, 4.0).move(0.0, -2.0, 0.0);
   }

   static AABB load(AABB part) {
      return part.inflate(1.0);
   }

   static r.Inner_DataUtil init_r_DataUtil(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         List<AABB> push = new ArrayList<>();
         List<AABB> strike = new ArrayList<>();

         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EnderDragon dragon && dragon.isAlive() && !(dragon.distanceToSqr(mc.player) > 16384.0)) {
               for (EnderDragonPart part : dragon.getSubEntities()) {
                  String name = part.name;
                  if (name.equals("wing")) {
                     push.add(init(part.getBoundingBox()));
                  } else if (name.equals("head") || name.equals("neck")) {
                     strike.add(load(part.getBoundingBox()));
                  }
               }
            }
         }

         return push.isEmpty() && strike.isEmpty() ? r.Inner_DataUtil.NONE : new r.Inner_DataUtil(push, strike);
      } else {
         return r.Inner_DataUtil.NONE;
      }
   }

   static AABB init_AABB_I(int x, int y, int z) {
      return new AABB(x + 0.2, y, z + 0.2, x + 0.8, y + 1.8, z + 0.8);
   }

   static AABB init_AABB_D(double x, double feetY, double z) {
      return new AABB(x - 0.3, feetY, z - 0.3, x + 0.3, feetY + 1.8, z + 0.3);
   }

   static EnderDragon init_EnderDragon(Minecraft mc) {
      if (mc.level == null) {
         return null;
      }

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof EnderDragon dragon && dragon.isAlive()) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getCurrentPhase();
            if (phase.isSitting() || phase.getPhase() == EnderDragonPhase.LANDING) {
               return dragon;
            }
         }
      }

      return null;
   }

   static boolean init_Z(Minecraft mc) {
      return init_EnderDragon(mc) != null;
   }

   static Vec3 init_Vec3(Minecraft mc) {
      EnderDragon dragon = init_EnderDragon(mc);
      if (dragon == null) {
         return null;
      }

      for (EnderDragonPart part : dragon.getSubEntities()) {
         if (part.name.equals("head")) {
            Vec3 d = new Vec3(part.getX() - dragon.getX(), 0.0, part.getZ() - dragon.getZ());
            return d.lengthSqr() < 1.0E-6 ? null : d.normalize();
         }
      }

      return null;
   }

   static Entity init_Entity(Minecraft mc) {
      EnderDragon dragon = init_EnderDragon(mc);
      if (dragon == null) {
         return null;
      }

      for (EnderDragonPart part : dragon.getSubEntities()) {
         if (part.name.equals("head")) {
            return part;
         }
      }

      return null;
   }

   static double[] init_DArr(Minecraft mc) {
      Entity head = init_Entity(mc);
      Vec3 facing = init_Vec3(mc);
      return head != null && facing != null ? new double[]{head.getX() + facing.x * 2.5, head.getZ() + facing.z * 2.5, 5.0} : null;
   }

   static boolean init(r.Inner_DataUtil zones, double[] breath, List<double[]> clouds, int x, int y, int z) {
      if (zones.hits(init_AABB_I(x, y, z), 1.0)) {
         return false;
      }

      double cx = x + 0.5;
      double cz = z + 0.5;
      return breath != null && Math.hypot(cx - breath[0], cz - breath[1]) < breath[2] + 1.0 ? false : q.init_Z_D(cx, cz, clouds, 0.5);
   }

   static AiOperation init_AiOperation(final Minecraft mc) {
      if (mc.player != null && mc.player.onGround() && S.init_I_Str("minecraft:water_bucket") > 0) {
         List<double[]> clouds = q.init_List(mc);
         if (clouds.isEmpty()) {
            return null;
         }

         double[] c = clouds.get(0);
         double best = 1.7976931348623157E308;

         for (double[] cloud : clouds) {
            double d = Math.hypot(cloud[0] - mc.player.getX(), cloud[1] - mc.player.getZ());
            if (d < best) {
               best = d;
               c = cloud;
            }
         }

         double dx = mc.player.getX() - c[0];
         double dz = mc.player.getZ() - c[1];
         double len = Math.hypot(dx, dz);
         final double ux = len < 0.001 ? 1.0 : dx / len;
         final double uz = len < 0.001 ? 0.0 : dz / len;
         return new AiOperation() {
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            AiOperation init_AiOperation = au.init_AiOperation_JsonObject(ag.init_JsonObject_ObjArr("method", "auto", "collect", true, "timeoutSeconds", 15));
            int init_I;
            boolean init_Z;

            @Override
            public AiOperation.Result tick() {
               if (!this.init_Z && mc.player != null) {
                  AiOperation.Result r = this.init_AiOperation.tick();
                  if (!r.terminal()) {
                     if (mc.player.onGround()) {
                        if (++this.init_I > 60) {
                           return this.init(AiOperation.Status.FAILED, "Could not step off the pillar");
                        }

                        this.init_DataUtil.init_V_Vec3(mc.player.getEyePosition().add(ux * 4.0, -1.5, uz * 4.0));
                        this.init_DataUtil.load_V();
                        PacketUtil.load();
                        mc.options.keyUp.setDown(true);
                     } else {
                        mc.options.keyUp.setDown(false);
                     }

                     return R.init_AiOperation_Result(AiOperation.Status.RUNNING, "Off the pillar out of the cloud", new JsonObject());
                  } else {
                     return this.init(r.status(), "Dropped off out of the cloud: " + r.message());
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Cloud drop cancelled");
               }
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               mc.options.keyUp.setDown(false);
               this.init_DataUtil.init_V();
               return R.init_AiOperation_Result(status, message, new JsonObject());
            }

            @Override
            public void cancel() {
               this.init_Z = true;
               this.init_AiOperation.cancel();
               mc.options.keyUp.setDown(false);
            }
         };
      } else {
         return null;
      }
   }

   public static autismclient.ai.a.ClassCache.Inner_PoolEntry init_ClassCache_PoolEnt(Minecraft mc) {
      r.Inner_DataUtil zones = init_r_DataUtil(mc);
      List<double[]> clouds = new ArrayList<>(q.init_List(mc));
      double[] breath = init_DArr(mc);
      if (breath != null) {
         clouds.add(breath);
      }

      return zones.isEmpty() && clouds.isEmpty() ? autismclient.ai.a.ClassCache.Inner_PoolEntry.write : (x, y, z) -> {
         if (zones.hits(init_AABB_I(x, y, z), 1.0)) {
            return 400.0;
         }

         for (double[] c : clouds) {
            if (Math.hypot(x + 0.5 - c[0], z + 0.5 - c[1]) < c[2] + 0.5) {
               return 400.0;
            }
         }

         return 0.0;
      };
   }

   static AiOperation load(Minecraft mc) {
      return init(mc, 2.0);
   }

   static AiOperation init(Minecraft mc, double clearance) {
      r.Inner_DataUtil zones = init_r_DataUtil(mc);
      if (!zones.isEmpty() && zones.hits(mc.player.getBoundingBox(), 0.6)) {
         List<double[]> clouds = q.init_List(mc);
         BlockPos feet = mc.player.blockPosition();
         BlockPos best = null;
         double bestD = 1.7976931348623157E308;

         for (BlockPos p : BlockPos.betweenClosed(feet.offset(-10, -3, -10), feet.offset(10, 3, 10))) {
            if (!zones.hits(init_AABB_I(p.getX(), p.getY(), p.getZ()), clearance) && q.init_Z_D(p.getX() + 0.5, p.getZ() + 0.5, clouds, 0.0)) {
               BlockPos below = p.below();
               if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                  && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                  && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                  && ValueSink.init_Z_Minecraft_BlockPos(mc, p)) {
                  double d = p.distSqr(feet);
                  if (d < bestD) {
                     bestD = d;
                     best = p.immutable();
                  }
               }
            }
         }

         return best == null ? null : X.init(HashUtil.init(best, 0.35, false, false, false, 6, autismclient.ai.a.ClassCache.Inner_IoWorker.init, true));
      } else {
         return null;
      }
   }

   record Inner_DataUtil(List<AABB> push, List<AABB> strike) {
      static final r.Inner_DataUtil NONE = new r.Inner_DataUtil(List.of(), List.of());

      boolean isEmpty() {
         return this.push.isEmpty() && this.strike.isEmpty();
      }

      boolean hits(AABB box, double margin) {
         for (AABB zone : this.push) {
            if (zone.inflate(margin).intersects(box)) {
               return true;
            }
         }

         double strike = Math.min(margin, 0.3);

         for (AABB zone : this.strike) {
            if (zone.inflate(strike).intersects(box)) {
               return true;
            }
         }

         return false;
      }
   }
}
