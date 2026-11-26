package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class W {
   static final int init_I = 64;
   static final double init_D = 2.5;
   static final int load_I = 8;
   static final long init_J = 4000000L;
   static volatile boolean init_Z;
   static volatile String init_Str = "";
   static final double load_D = 0.25;
   static final double read = 10.0;
   static final double write = 30.0;

   static W.Inner_SyncManager init(W.Inner_ConfigStore input, Vec3 impulse, W.Inner_BufferHelper hypotheses) {
      if (hypotheses != null && hypotheses.init_D() > 0.0 && hypotheses.init_W_SyncManager() != null) {
         double lx = input.init_AABB.getCenter().x - input.init_Vec3.x;
         double lz = input.init_AABB.getCenter().z - input.init_Vec3.z;
         double length = Math.hypot(lx, lz);
         if (length < 1.0E-4) {
            return null;
         }

         lx /= length;
         lz /= length;
         double along = impulse.x * lx + impulse.z * lz;
         double px = impulse.x - lx * along;
         double pz = impulse.z - lz * along;
         double side = Math.hypot(px, pz);
         return side < 0.02
            ? null
            : new W.Inner_SyncManager(-px / side, -pz / side, Math.min(1.0, hypotheses.init_D()) * 0.98, hypotheses.init_W_SyncManager().write(), true);
      } else {
         return null;
      }
   }

   private W() {
   }

   static double init_D_D(double attribute, int knockbackLevel, boolean serverSprint, double charge, boolean legacy) {
      return Double.isFinite(attribute) && Double.isFinite(charge) && !(attribute < 0.0) && knockbackLevel >= 0
         ? attribute / 2.0 + knockbackLevel * 0.5 + (serverSprint && (legacy || charge > 0.9) ? 0.5 : 0.0)
         : 0.0;
   }

   static Vec3 init(W.Inner_ConfigStore input, float yaw) {
      double retained = 1.0 - Math.max(0.0, Math.min(1.0, input.init_D));
      Vec3 motion = input.load_Vec3;
      double dx = input.init_AABB.getCenter().x - input.init_Vec3.x;
      double dz = input.init_AABB.getCenter().z - input.init_Vec3.z;
      double length = Math.hypot(dx, dz);
      if (!(length < 1.0E-4) && !(retained <= 0.0)) {
         double a = Math.toRadians(yaw);
         if (input.load_Z) {
            Vec3 pushed = new Vec3(motion.x / 2.0 + dx / length * 0.4, Math.min(0.4, motion.y / 2.0 + 0.4), motion.z / 2.0 + dz / length * 0.4);
            return input.load_D <= 0.0 ? pushed : new Vec3(pushed.x - Math.sin(a) * input.load_D, pushed.y + 0.1, pushed.z + Math.cos(a) * input.load_D);
         } else {
            double base = 0.4 * retained;
            motion = new Vec3(
               motion.x / 2.0 + dx / length * base, input.init_Z ? Math.min(0.4, motion.y / 2.0 + base) : motion.y, motion.z / 2.0 + dz / length * base
            );
            double extra = input.load_D * retained;
            return extra <= 0.0
               ? motion
               : new Vec3(
                  motion.x / 2.0 - Math.sin(a) * extra, input.init_Z ? Math.min(0.4, motion.y / 2.0 + extra) : motion.y, motion.z / 2.0 + Math.cos(a) * extra
               );
         }
      } else {
         return motion;
      }
   }

   static W.Inner_NetHelper init(
      W.Inner_ConfigStore input, float yaw, double quantum, double maximumTurn, double minimum, boolean voidOnly, int side, W.Inner_UiHelper world
   ) {
      return init(input, yaw, quantum, new W.Inner_StateCache(maximumTurn, minimum, voidOnly, side, 10.0), W.Inner_BufferHelper.init_W_BufferHelper, world);
   }

   static W.Inner_NetHelper init(
      W.Inner_ConfigStore input, float yaw, double quantum, W.Inner_StateCache settings, W.Inner_BufferHelper hypotheses, W.Inner_UiHelper world
   ) {
      init_Str = input != null && Double.isFinite(input.load_D) && input.load_D <= 0.0 ? "no-extra-push" : "invalid";
      if (init(input) && world != null && settings != null && Float.isFinite(yaw)) {
         double maximumTurn = settings.init_D();
         double minimum = settings.load();
         double radius = settings.read();
         boolean voidOnly = settings.init_Z();
         if (Double.isFinite(maximumTurn)
            && Double.isFinite(minimum)
            && !(minimum < 1.0)
            && !(minimum > 40.0)
            && Double.isFinite(radius)
            && !(radius < 0.5)
            && !(radius > 16.0)) {
            if (hypotheses == null) {
               hypotheses = W.Inner_BufferHelper.init_W_BufferHelper;
            }

            maximumTurn = Math.max(0.0, Math.min(180.0, maximumTurn));
            if (maximumTurn < 0.01) {
               return null;
            }

            long deadline = System.nanoTime() + 4000000L;
            V grid = new V(world, input.init_AABB, input.read, radius, minimum, voidOnly);
            init_Str = "no-edge";
            if (!grid.init()) {
               return null;
            }

            W.Inner_ConfigStore unturned = world.init_W_ConfigStore_W_ConfigStore_F(input, yaw);
            W.Inner_JsonUtil original = init(unturned, yaw, world);
            init_Str = original.init_W_TaskQueue == W.Inner_TaskQueue.UNKNOWN ? "unknown-terrain" : "already-falls";
            if (original.init_W_TaskQueue != W.Inner_TaskQueue.UNKNOWN && !original.init(minimum, voidOnly)) {
               double[] turns = init(maximumTurn);
               ArrayList<W.Inner_StreamUtil> candidates = new ArrayList<>(turns.length * 2);
               HashSet<Float> seen = new HashSet<>();

               for (double sampled : turns) {
                  for (int sign : new int[]{-1, 1}) {
                     if (settings.init_I() == 0 || sign == settings.init_I()) {
                        double turn = sampled;
                        if (Double.isFinite(quantum) && quantum > 0.0) {
                           turn = Math.floor(turn / quantum + 1.0E-8) * quantum;
                        }

                        if (!(turn < 0.01)) {
                           float candidate = yaw + (float)(sign * turn);
                           if (seen.add(candidate) && world.init_Z_F(candidate)) {
                              V.Inner_CachePool flight = grid.init_V_CachePool_AABB(
                                 input.init_AABB, init(unturned, candidate), input.init_Z, W.Inner_SyncManager.init_W_SyncManager
                              );
                              if (flight.init_W_TaskQueue() != W.Inner_TaskQueue.UNKNOWN) {
                                 candidates.add(new W.Inner_StreamUtil(candidate, turn, flight));
                              }
                           }
                        }
                     }
                  }
               }

               init_Str = "no-hittable-turn";
               if (candidates.isEmpty()) {
                  return null;
               }

               ArrayList<W.Inner_StreamUtil> throwsFound = new ArrayList<>();

               for (W.Inner_StreamUtil c : candidates) {
                  if (c.init_V_CachePool.init_Z() && c.init_V_CachePool.init(minimum, voidOnly)) {
                     throwsFound.add(c);
                  }
               }

               boolean modelCounter = hypotheses.init_W_SyncManager() != null && hypotheses.init_W_SyncManager() != W.Inner_SyncManager.init_W_SyncManager;
               Map<W.Inner_StreamUtil, Boolean> robust = new IdentityHashMap<>();

               for (W.Inner_StreamUtil c : throwsFound) {
                  boolean holds = !modelCounter;
                  if (modelCounter) {
                     V.Inner_CachePool countered = grid.init_V_CachePool_AABB(
                        input.init_AABB, init(unturned, c.init_F), input.init_Z, hypotheses.init_W_SyncManager()
                     );
                     holds = countered.init_Z() && countered.init(minimum, voidOnly);
                  }

                  robust.put(c, holds);
               }

               Map<W.Inner_StreamUtil, Boolean> strafeHolds = new IdentityHashMap<>();

               for (W.Inner_StreamUtil c : throwsFound) {
                  Vec3 pushed = init(unturned, c.init_F);
                  W.Inner_SyncManager strafe = init(unturned, pushed, hypotheses);
                  boolean holds = strafe == null ? robust.get(c) : false;
                  if (strafe != null) {
                     V.Inner_CachePool strafed = grid.init_V_CachePool_AABB(input.init_AABB, pushed, input.init_Z, strafe);
                     holds = strafed.init_Z() && strafed.init(minimum, voidOnly);
                  }

                  strafeHolds.put(c, holds);
               }

               throwsFound.sort(
                  Comparator.<W.Inner_StreamUtil, Boolean>comparing(c -> !strafeHolds.get(c))
                     .thenComparing(c -> !robust.get(c))
                     .thenComparing(c -> c.init_V_CachePool.read_Z())
                     .thenComparing(c -> c.init_V_CachePool.init_W_TaskQueue() != W.Inner_TaskQueue.VOID)
                     .thenComparingDouble(c -> c.init_D * 0.01 + c.init_V_CachePool.load_I() * 0.03)
               );
               init_Str = throwsFound.isEmpty() ? "no-throw" : "unconfirmed";
               int confirmations = 0;

               label183:
               for (W.Inner_StreamUtil c : throwsFound) {
                  int required = 1;

                  for (Vec3 velocity : hypotheses.init_Vec3Arr()) {
                     if (velocity != null) {
                        required++;
                     }
                  }

                  if (modelCounter && robust.get(c)) {
                     required++;
                  }

                  if (confirmations + required > 8 || System.nanoTime() > deadline && confirmations >= 3) {
                     break;
                  }

                  if (world.load_Z_F(c.init_F)) {
                     W.Inner_ConfigStore at = world.init_W_ConfigStore_W_ConfigStore_F(input, c.init_F);
                     if (init(at)) {
                        V.Inner_CachePool actualFlight = grid.init_V_CachePool_AABB(
                           at.init_AABB, init(at, c.init_F), at.init_Z, W.Inner_SyncManager.init_W_SyncManager
                        );
                        if (actualFlight.init_Z() && actualFlight.init(minimum, voidOnly)) {
                           W.Inner_JsonUtil result = init(at, c.init_F, world, W.Inner_SyncManager.init_W_SyncManager);
                           confirmations++;
                           if (result.init(minimum, voidOnly)) {
                              boolean every = true;

                              for (Vec3 velocity : hypotheses.init_Vec3Arr()) {
                                 if (velocity != null) {
                                    confirmations++;
                                    if (!init(at.load(velocity), c.init_F, world, W.Inner_SyncManager.init_W_SyncManager).init(minimum, voidOnly)) {
                                       every = false;
                                       if (every) {
                                          boolean holds = !modelCounter;
                                          if (modelCounter && robust.get(c)) {
                                             confirmations++;
                                             holds = init(at, c.init_F, world, hypotheses.init_W_SyncManager()).init(minimum, voidOnly);
                                          }

                                          init_Str = "planned";
                                          init_Z = strafeHolds.get(c);
                                          return new W.Inner_NetHelper(c.init_F, result, original, false, holds, 0.0);
                                       }
                                       continue label183;
                                    }
                                 }
                              }
                              break;
                           }
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   static double[] init(double maximumTurn) {
      ArrayList<Double> list = new ArrayList<>();

      for (double turn = 0.25; turn < 9.999999999 && turn < maximumTurn; turn += 0.25) {
         list.add(turn);
      }

      for (double turn = 10.0; turn < 29.999999999 && turn < maximumTurn; turn++) {
         list.add(turn);
      }

      for (double turn = 30.0; turn < maximumTurn - 1.0E-9; turn += 2.5) {
         list.add(turn);
      }

      list.add(maximumTurn);
      double[] turns = new double[list.size()];

      for (int i = 0; i < turns.length; i++) {
         turns[i] = list.get(i);
      }

      return turns;
   }

   static W.Inner_DataUtil init_W_DataUtil_AABB(AABB body, Vec3 motion, boolean grounded, int ticks, W.Inner_UiHelper world) {
      if (body != null && init(motion) && world != null) {
         double vx = motion.x;
         double vy = grounded ? 0.0 : motion.y;
         double vz = motion.z;
         int air = 0;

         for (int t = 0; t < ticks; t++) {
            Vec3 want = new Vec3(vx, grounded ? -0.0784 : vy, vz);
            if (!world.init_Z_AABB(body.expandTowards(want).inflate(1.0, 0.6, 1.0))) {
               break;
            }

            if (grounded && (vx != 0.0 || vz != 0.0)) {
               Vec3 down = world.init_Vec3_AABB_Vec3(body.move(vx, 0.0, vz), new Vec3(0.0, -0.6, 0.0));
               if (!init(down) || down.y <= -0.5999999) {
                  vx = 0.0;
                  vz = 0.0;
                  want = new Vec3(0.0, want.y, 0.0);
               }
            }

            Vec3 actual = world.init_Vec3_AABB_Vec3(body, want);
            if (!init(actual)) {
               break;
            }

            if (Math.abs(actual.x - want.x) > 1.0E-7) {
               vx = 0.0;
            }

            if (Math.abs(actual.z - want.z) > 1.0E-7) {
               vz = 0.0;
            }

            boolean landed = want.y < 0.0 && actual.y > want.y + 1.0E-7;
            body = body.move(actual);
            if (landed) {
               grounded = true;
               vy = 0.0;
               air = 0;
            } else {
               grounded = false;
               vy = (want.y - 0.08) * 0.98;
               air++;
            }
         }

         return new W.Inner_DataUtil(body, grounded, new Vec3(vx, grounded ? 0.0 : vy, vz), air);
      } else {
         return new W.Inner_DataUtil(body, grounded, Vec3.ZERO, 0);
      }
   }

   static W.Inner_JsonUtil init(W.Inner_ConfigStore input, float yaw, W.Inner_UiHelper world) {
      return init(input, yaw, world, W.Inner_SyncManager.init_W_SyncManager);
   }

   static W.Inner_JsonUtil init(W.Inner_ConfigStore input, float yaw, W.Inner_UiHelper world, W.Inner_SyncManager steer) {
      if (init(input) && Float.isFinite(yaw) && world != null) {
         if (steer == null) {
            steer = W.Inner_SyncManager.init_W_SyncManager;
         }

         AABB body = input.init_AABB;
         Vec3 motion = init(input, yaw);
         boolean ground = input.init_Z;
         boolean countering = steer != W.Inner_SyncManager.init_W_SyncManager;
         int tick = 1;

         while (tick <= 64) {
            if (!world.init_Z_AABB(body.expandTowards(motion).inflate(0.001))) {
               return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, tick);
            }

            if (world.load_Z_AABB(body)) {
               return new W.Inner_JsonUtil(W.Inner_TaskQueue.SAFE, 0.0, tick);
            }

            double block = ground ? world.init_D_AABB(body) : 0.6;
            double friction = ground ? block * 0.91 : 0.91;
            if (Double.isFinite(friction) && !(friction <= 0.0) && !(friction > 1.0)) {
               if (countering && steer.init(motion.x, motion.z)) {
                  double accel = ground ? steer.read() * (0.21600002 / (block * block * block)) : steer.write();
                  motion = motion.add(steer.init_D() * accel, 0.0, steer.load() * accel);
               }

               Vec3 wanted = Math.abs(motion.y) < 1.0E-8 ? new Vec3(motion.x, -0.01, motion.z) : motion;
               Vec3 actual = world.init_Vec3_AABB_Vec3(body, wanted);
               if (!init(actual)) {
                  return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, tick);
               }

               if (world.load_Z_AABB(body.expandTowards(actual))) {
                  return new W.Inner_JsonUtil(W.Inner_TaskQueue.SAFE, 0.0, tick);
               }

               if (world.read(body.expandTowards(actual))) {
                  return new W.Inner_JsonUtil(W.Inner_TaskQueue.LAVA, 1.0 / 0.0, tick);
               }

               ground = wanted.y < 0.0 && actual.y > wanted.y + 1.0E-7;
               body = body.move(actual);
               if (world.load_Z_AABB(body)) {
                  return new W.Inner_JsonUtil(W.Inner_TaskQueue.SAFE, 0.0, tick);
               }

               motion = new Vec3(
                  Math.abs(actual.x - wanted.x) > 1.0E-7 ? 0.0 : wanted.x,
                  Math.abs(actual.y - wanted.y) > 1.0E-7 ? 0.0 : wanted.y,
                  Math.abs(actual.z - wanted.z) > 1.0E-7 ? 0.0 : wanted.z
               );
               motion = new Vec3(motion.x * friction, (motion.y - 0.08) * 0.98, motion.z * friction);
               if (ground && tick > 1) {
                  countering = false;
               }

               if (!ground && motion.y < 0.0 && body.minY < input.read - 0.5) {
                  double speed = motion.horizontalDistance();
                  double drift = speed / 0.09 + 0.05 + (steer == W.Inner_SyncManager.init_W_SyncManager ? 0 : 1);
                  Vec3 reach = speed > 1.0E-9 ? new Vec3(motion.x / speed * drift, 0.0, motion.z / speed * drift) : Vec3.ZERO;
                  AABB region = body.expandTowards(reach);
                  if (steer != W.Inner_SyncManager.init_W_SyncManager) {
                     region = region.inflate(1.0, 0.0, 1.0);
                  }

                  W.Inner_CachePool below = world.init_W_CachePool_AABB(region);
                  if (below != null && below.read && below.init_Z && !below.load) {
                     return new W.Inner_JsonUtil(W.Inner_TaskQueue.VOID, 1.0 / 0.0, tick);
                  }
               }

               if ((!ground || !(motion.horizontalDistance() < 0.015)) && tick != 64) {
                  tick++;
                  continue;
               }

               if (motion.horizontalDistance() > 0.03) {
                  return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, tick);
               }

               W.Inner_CachePool floor = world.init_W_CachePool_AABB(body);
               if (floor != null && floor.read) {
                  if (floor.load) {
                     return new W.Inner_JsonUtil(W.Inner_TaskQueue.SAFE, 0.0, tick);
                  }

                  if (floor.write) {
                     return new W.Inner_JsonUtil(W.Inner_TaskQueue.LAVA, 1.0 / 0.0, tick);
                  }

                  if (floor.init_Z) {
                     return new W.Inner_JsonUtil(W.Inner_TaskQueue.VOID, 1.0 / 0.0, tick);
                  }

                  double drop = Math.max(0.0, input.read - floor.init_D);
                  return new W.Inner_JsonUtil(drop > 1.0E-6 ? W.Inner_TaskQueue.DROP : W.Inner_TaskQueue.SAFE, drop, tick);
               }

               return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, tick);
            }

            return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, tick);
         }

         return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, 64);
      } else {
         return new W.Inner_JsonUtil(W.Inner_TaskQueue.UNKNOWN, 0.0, 0);
      }
   }

   static Vec3 init(Vec3 velocity, Input keys, float yaw, double acceleration) {
      double forward = (keys.forward() ? 1 : 0) - (keys.backward() ? 1 : 0);
      double side = (keys.left() ? 1 : 0) - (keys.right() ? 1 : 0);
      double length = Math.hypot(side, forward);
      if (length > 0.0) {
         side = side / length * 0.98;
         forward = forward / length * 0.98;
      }

      double angle = Math.toRadians(yaw);
      double s = Math.sin(angle);
      double c = Math.cos(angle);
      return velocity.add((side * c - forward * s) * acceleration, 0.0, (forward * c + side * s) * acceleration);
   }

   static boolean init(W.Inner_ConfigStore i) {
      return i != null
         && i.init_AABB != null
         && init(i.init_AABB.getCenter())
         && init(i.init_Vec3)
         && init(i.load_Vec3)
         && i.init_AABB.getXsize() > 0.0
         && i.init_AABB.getXsize() <= 4.0
         && i.init_AABB.getYsize() > 0.0
         && i.init_AABB.getYsize() <= 4.0
         && i.init_AABB.getZsize() > 0.0
         && i.init_AABB.getZsize() <= 4.0
         && i.load_Vec3.lengthSqr() <= 4.0
         && Double.isFinite(i.init_D)
         && i.init_D >= 0.0
         && i.init_D < 1.0
         && Double.isFinite(i.load_D)
         && i.load_D > 0.0
         && i.load_D <= 4.0
         && Double.isFinite(i.read)
         && Math.abs(i.read - i.init_AABB.minY) <= 8.0
         && Math.hypot(i.init_AABB.getCenter().x - i.init_Vec3.x, i.init_AABB.getCenter().z - i.init_Vec3.z) > 1.0E-4;
   }

   private static boolean init(Vec3 v) {
      return v != null
         && Double.isFinite(v.x)
         && Double.isFinite(v.y)
         && Double.isFinite(v.z)
         && Math.abs(v.x) <= 3.0E7
         && Math.abs(v.y) <= 3.0E7
         && Math.abs(v.z) <= 3.0E7;
   }

   record Inner_BufferHelper(W.Inner_SyncManager init_W_SyncManager, Vec3[] init_Vec3Arr, double init_D) {
      static final W.Inner_BufferHelper init_W_BufferHelper = new W.Inner_BufferHelper(W.Inner_SyncManager.init_W_SyncManager, new Vec3[0]);

      Inner_BufferHelper(W.Inner_SyncManager counter, Vec3[] velocities) {
         this(counter, velocities, counter != null && counter != W.Inner_SyncManager.init_W_SyncManager ? counter.read() / 0.98 : 0.0);
      }
   }

   record Inner_CachePool(double init_D, boolean init_Z, boolean load, boolean read, boolean write) {
      Inner_CachePool(double y, boolean voidColumn, boolean caught, boolean known) {
         this(y, voidColumn, caught, known, false);
      }
   }

   record Inner_ConfigStore(AABB init_AABB, Vec3 init_Vec3, Vec3 load_Vec3, boolean init_Z, double init_D, double load_D, double read, boolean load_Z) {
      Inner_ConfigStore(AABB body, Vec3 attacker, Vec3 velocity, boolean grounded, double resistance, double extra) {
         this(body, attacker, velocity, grounded, resistance, extra, body == null ? 0.0 / 0.0 : body.minY, false);
      }

      W.Inner_ConfigStore init(Vec3 origin) {
         return new W.Inner_ConfigStore(this.init_AABB, origin, this.load_Vec3, this.init_Z, this.init_D, this.load_D, this.read, this.load_Z);
      }

      W.Inner_ConfigStore load(Vec3 motion) {
         return new W.Inner_ConfigStore(this.init_AABB, this.init_Vec3, motion, this.init_Z, this.init_D, this.load_D, this.read, this.load_Z);
      }
   }

   record Inner_DataUtil(AABB init_AABB, boolean init_Z, Vec3 init_Vec3, int init_I) {
   }

   record Inner_JsonUtil(W.Inner_TaskQueue init_W_TaskQueue, double init_D, int init_I) {
      boolean init(double minimum, boolean voidOnly) {
         return this.init_W_TaskQueue == W.Inner_TaskQueue.VOID
            || !voidOnly
               && (this.init_W_TaskQueue == W.Inner_TaskQueue.LAVA || this.init_W_TaskQueue == W.Inner_TaskQueue.DROP && this.init_D + 1.0E-6 >= minimum);
      }
   }

   record Inner_NetHelper(float init_F, W.Inner_JsonUtil init_W_JsonUtil, W.Inner_JsonUtil load_W_JsonUtil, boolean init_Z, boolean load_Z, double init_D) {
      Inner_NetHelper(float yaw, W.Inner_JsonUtil result, W.Inner_JsonUtil original) {
         this(yaw, result, original, false, false, 0.0);
      }
   }

   record Inner_StateCache(double init_D, double load, boolean init_Z, int init_I, double read) {
   }

   private record Inner_StreamUtil(float init_F, double init_D, V.Inner_CachePool init_V_CachePool) {
   }

   record Inner_SyncManager(double init_D, double load, double read, double write, boolean init_Z) {
      static final W.Inner_SyncManager init_W_SyncManager = new W.Inner_SyncManager(0.0, 0.0, 0.0, 0.0);

      Inner_SyncManager(double x, double z, double speed, double air) {
         this(x, z, speed, air, false);
      }

      boolean init(double vx, double vz) {
         return !this.init_Z || vx * this.init_D + vz * this.load < 0.0;
      }

      static W.Inner_SyncManager init(float yaw, double speed, double air) {
         if (Float.isFinite(yaw) && Double.isFinite(speed) && Double.isFinite(air)) {
            double a = Math.toRadians(yaw);
            return new W.Inner_SyncManager(-Math.sin(a), Math.cos(a), Math.max(0.0, Math.min(1.0, speed)) * 0.98, Math.max(0.0, Math.min(0.2, air)) * 0.98);
         } else {
            return init_W_SyncManager;
         }
      }
   }

   enum Inner_TaskQueue {
      SAFE,
      DROP,
      VOID,
      LAVA,
      UNKNOWN;
   }

   interface Inner_UiHelper {
      boolean init_Z_AABB(AABB var1);

      Vec3 init_Vec3_AABB_Vec3(AABB var1, Vec3 var2);

      boolean load_Z_AABB(AABB var1);

      W.Inner_CachePool init_W_CachePool_AABB(AABB var1);

      double init();

      V.Inner_StreamUtil init_V_StreamUtil_I(int var1, int var2, int var3, double var4);

      default boolean init_Z_F(float yaw) {
         return true;
      }

      default boolean load_Z_F(float yaw) {
         return true;
      }

      default W.Inner_ConfigStore init_W_ConfigStore_W_ConfigStore_F(W.Inner_ConfigStore input, float yaw) {
         return input;
      }

      default boolean read(AABB body) {
         return false;
      }

      default double init_D_AABB(AABB body) {
         return 0.6;
      }
   }
}
