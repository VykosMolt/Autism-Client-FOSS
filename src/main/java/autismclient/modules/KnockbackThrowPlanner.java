package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class KnockbackThrowPlanner {
   static final int init_I = 64;
   static final double init_D = 2.5;
   static final int load_I = 8;
   static final long init_J = 4000000L;
   static volatile boolean lastStrafe;
   static volatile String lastStatus = "";
   static final double load_D = 0.25;
   static final double read = 10.0;
   static final double write = 30.0;

   static KnockbackThrowPlanner.Inner_SyncManager counterSteer(KnockbackThrowPlanner.Inner_ConfigStore input, Vec3 impulse, KnockbackThrowPlanner.Inner_BufferHelper hypotheses) {
      if (hypotheses != null && hypotheses.counterStrength() > 0.0 && hypotheses.counter() != null) {
         double lx = input.body.getCenter().x - input.attacker.x;
         double lz = input.body.getCenter().z - input.attacker.z;
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
            : new KnockbackThrowPlanner.Inner_SyncManager(-px / side, -pz / side, Math.min(1.0, hypotheses.counterStrength()) * 0.98, hypotheses.counter().write(), true);
      } else {
         return null;
      }
   }

   private KnockbackThrowPlanner() {
   }

   static double knockbackStrength(double attribute, int knockbackLevel, boolean serverSprint, double charge, boolean legacy) {
      return Double.isFinite(attribute) && Double.isFinite(charge) && !(attribute < 0.0) && knockbackLevel >= 0
         ? attribute / 2.0 + knockbackLevel * 0.5 + (serverSprint && (legacy || charge > 0.9) ? 0.5 : 0.0)
         : 0.0;
   }

   static Vec3 pushedVelocity(KnockbackThrowPlanner.Inner_ConfigStore input, float yaw) {
      double retained = 1.0 - Math.max(0.0, Math.min(1.0, input.resistance));
      Vec3 motion = input.velocity;
      double dx = input.body.getCenter().x - input.attacker.x;
      double dz = input.body.getCenter().z - input.attacker.z;
      double length = Math.hypot(dx, dz);
      if (!(length < 1.0E-4) && !(retained <= 0.0)) {
         double a = Math.toRadians(yaw);
         if (input.load_Z) {
            Vec3 pushed = new Vec3(motion.x / 2.0 + dx / length * 0.4, Math.min(0.4, motion.y / 2.0 + 0.4), motion.z / 2.0 + dz / length * 0.4);
            return input.extra <= 0.0 ? pushed : new Vec3(pushed.x - Math.sin(a) * input.extra, pushed.y + 0.1, pushed.z + Math.cos(a) * input.extra);
         } else {
            double base = 0.4 * retained;
            motion = new Vec3(
               motion.x / 2.0 + dx / length * base, input.grounded ? Math.min(0.4, motion.y / 2.0 + base) : motion.y, motion.z / 2.0 + dz / length * base
            );
            double extra = input.extra * retained;
            return extra <= 0.0
               ? motion
               : new Vec3(
                  motion.x / 2.0 - Math.sin(a) * extra, input.grounded ? Math.min(0.4, motion.y / 2.0 + extra) : motion.y, motion.z / 2.0 + Math.cos(a) * extra
               );
         }
      } else {
         return motion;
      }
   }

   static KnockbackThrowPlanner.Inner_NetHelper plan(
      KnockbackThrowPlanner.Inner_ConfigStore input, float yaw, double quantum, double maximumTurn, double minimum, boolean voidOnly, int side, KnockbackThrowPlanner.Inner_UiHelper world
   ) {
      return init(input, yaw, quantum, new KnockbackThrowPlanner.Inner_StateCache(maximumTurn, minimum, voidOnly, side, 10.0), KnockbackThrowPlanner.Inner_BufferHelper.NO_HYPOTHESES, world);
   }

   static KnockbackThrowPlanner.Inner_NetHelper init(
      KnockbackThrowPlanner.Inner_ConfigStore input, float yaw, double quantum, KnockbackThrowPlanner.Inner_StateCache settings, KnockbackThrowPlanner.Inner_BufferHelper hypotheses, KnockbackThrowPlanner.Inner_UiHelper world
   ) {
      lastStatus = input != null && Double.isFinite(input.extra) && input.extra <= 0.0 ? "no-extra-push" : "invalid";
      if (isValidInput(input) && world != null && settings != null && Float.isFinite(yaw)) {
         double maximumTurn = settings.maximumTurn();
         double minimum = settings.load();
         double radius = settings.read();
         boolean voidOnly = settings.voidOnly();
         if (Double.isFinite(maximumTurn)
            && Double.isFinite(minimum)
            && !(minimum < 1.0)
            && !(minimum > 40.0)
            && Double.isFinite(radius)
            && !(radius < 0.5)
            && !(radius > 16.0)) {
            if (hypotheses == null) {
               hypotheses = KnockbackThrowPlanner.Inner_BufferHelper.NO_HYPOTHESES;
            }

            maximumTurn = Math.max(0.0, Math.min(180.0, maximumTurn));
            if (maximumTurn < 0.01) {
               return null;
            }

            long deadline = System.nanoTime() + 4000000L;
            TerrainDropGrid grid = new TerrainDropGrid(world, input.body, input.read, radius, minimum, voidOnly);
            lastStatus = "no-edge";
            if (!grid.init()) {
               return null;
            }

            KnockbackThrowPlanner.Inner_ConfigStore unturned = world.init_W_ConfigStore_W_ConfigStore_F(input, yaw);
            KnockbackThrowPlanner.Inner_JsonUtil original = evaluate(unturned, yaw, world);
            lastStatus = original.outcome == KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN ? "unknown-terrain" : "already-falls";
            if (original.outcome != KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN && !original.meetsGoal(minimum, voidOnly)) {
               double[] turns = turnCandidates(maximumTurn);
               ArrayList<KnockbackThrowPlanner.Inner_StreamUtil> candidates = new ArrayList<>(turns.length * 2);
               HashSet<Float> seen = new HashSet<>();

               for (double sampled : turns) {
                  for (int sign : new int[]{-1, 1}) {
                     if (settings.side() == 0 || sign == settings.side()) {
                        double turn = sampled;
                        if (Double.isFinite(quantum) && quantum > 0.0) {
                           turn = Math.floor(turn / quantum + 1.0E-8) * quantum;
                        }

                        if (!(turn < 0.01)) {
                           float candidate = yaw + (float)(sign * turn);
                           if (seen.add(candidate) && world.init_Z_F(candidate)) {
                              TerrainDropGrid.Inner_CachePool flight = grid.simulateFlight(
                                 input.body, pushedVelocity(unturned, candidate), input.grounded, KnockbackThrowPlanner.Inner_SyncManager.NONE
                              );
                              if (flight.outcome() != KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN) {
                                 candidates.add(new KnockbackThrowPlanner.Inner_StreamUtil(candidate, turn, flight));
                              }
                           }
                        }
                     }
                  }
               }

               lastStatus = "no-hittable-turn";
               if (candidates.isEmpty()) {
                  return null;
               }

               ArrayList<KnockbackThrowPlanner.Inner_StreamUtil> throwsFound = new ArrayList<>();

               for (KnockbackThrowPlanner.Inner_StreamUtil c : candidates) {
                  if (c.landing.init_Z() && c.landing.init(minimum, voidOnly)) {
                     throwsFound.add(c);
                  }
               }

               boolean modelCounter = hypotheses.counter() != null && hypotheses.counter() != KnockbackThrowPlanner.Inner_SyncManager.NONE;
               Map<KnockbackThrowPlanner.Inner_StreamUtil, Boolean> robust = new IdentityHashMap<>();

               for (KnockbackThrowPlanner.Inner_StreamUtil c : throwsFound) {
                  boolean holds = !modelCounter;
                  if (modelCounter) {
                     TerrainDropGrid.Inner_CachePool countered = grid.simulateFlight(
                        input.body, pushedVelocity(unturned, c.yaw), input.grounded, hypotheses.counter()
                     );
                     holds = countered.init_Z() && countered.init(minimum, voidOnly);
                  }

                  robust.put(c, holds);
               }

               Map<KnockbackThrowPlanner.Inner_StreamUtil, Boolean> strafeHolds = new IdentityHashMap<>();

               for (KnockbackThrowPlanner.Inner_StreamUtil c : throwsFound) {
                  Vec3 pushed = pushedVelocity(unturned, c.yaw);
                  KnockbackThrowPlanner.Inner_SyncManager strafe = counterSteer(unturned, pushed, hypotheses);
                  boolean holds = strafe == null ? robust.get(c) : false;
                  if (strafe != null) {
                     TerrainDropGrid.Inner_CachePool strafed = grid.simulateFlight(input.body, pushed, input.grounded, strafe);
                     holds = strafed.init_Z() && strafed.init(minimum, voidOnly);
                  }

                  strafeHolds.put(c, holds);
               }

               throwsFound.sort(
                  Comparator.<KnockbackThrowPlanner.Inner_StreamUtil, Boolean>comparing(c -> !strafeHolds.get(c))
                     .thenComparing(c -> !robust.get(c))
                     .thenComparing(c -> c.landing.read_Z())
                     .thenComparing(c -> c.landing.outcome() != KnockbackThrowPlanner.Inner_TaskQueue.VOID)
                     .thenComparingDouble(c -> c.init_D * 0.01 + c.landing.load_I() * 0.03)
               );
               lastStatus = throwsFound.isEmpty() ? "no-throw" : "unconfirmed";
               int confirmations = 0;

               label183:
               for (KnockbackThrowPlanner.Inner_StreamUtil c : throwsFound) {
                  int required = 1;

                  for (Vec3 velocity : hypotheses.velocities()) {
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

                  if (world.load_Z_F(c.yaw)) {
                     KnockbackThrowPlanner.Inner_ConfigStore at = world.init_W_ConfigStore_W_ConfigStore_F(input, c.yaw);
                     if (isValidInput(at)) {
                        TerrainDropGrid.Inner_CachePool actualFlight = grid.simulateFlight(
                           at.body, pushedVelocity(at, c.yaw), at.grounded, KnockbackThrowPlanner.Inner_SyncManager.NONE
                        );
                        if (actualFlight.init_Z() && actualFlight.init(minimum, voidOnly)) {
                           KnockbackThrowPlanner.Inner_JsonUtil result = evaluate(at, c.yaw, world, KnockbackThrowPlanner.Inner_SyncManager.NONE);
                           confirmations++;
                           if (result.meetsGoal(minimum, voidOnly)) {
                              boolean every = true;

                              for (Vec3 velocity : hypotheses.velocities()) {
                                 if (velocity != null) {
                                    confirmations++;
                                    if (!evaluate(at.withVelocity(velocity), c.yaw, world, KnockbackThrowPlanner.Inner_SyncManager.NONE).meetsGoal(minimum, voidOnly)) {
                                       every = false;
                                       if (every) {
                                          boolean holds = !modelCounter;
                                          if (modelCounter && robust.get(c)) {
                                             confirmations++;
                                             holds = evaluate(at, c.yaw, world, hypotheses.counter()).meetsGoal(minimum, voidOnly);
                                          }

                                          lastStatus = "planned";
                                          lastStrafe = strafeHolds.get(c);
                                          return new KnockbackThrowPlanner.Inner_NetHelper(c.yaw, result, original, false, holds, 0.0);
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

   static double[] turnCandidates(double maximumTurn) {
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

   static KnockbackThrowPlanner.Inner_DataUtil simulate(AABB body, Vec3 motion, boolean grounded, int ticks, KnockbackThrowPlanner.Inner_UiHelper world) {
      if (body != null && isFiniteVec(motion) && world != null) {
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
               if (!isFiniteVec(down) || down.y <= -0.5999999) {
                  vx = 0.0;
                  vz = 0.0;
                  want = new Vec3(0.0, want.y, 0.0);
               }
            }

            Vec3 actual = world.init_Vec3_AABB_Vec3(body, want);
            if (!isFiniteVec(actual)) {
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

         return new KnockbackThrowPlanner.Inner_DataUtil(body, grounded, new Vec3(vx, grounded ? 0.0 : vy, vz), air);
      } else {
         return new KnockbackThrowPlanner.Inner_DataUtil(body, grounded, Vec3.ZERO, 0);
      }
   }

   static KnockbackThrowPlanner.Inner_JsonUtil evaluate(KnockbackThrowPlanner.Inner_ConfigStore input, float yaw, KnockbackThrowPlanner.Inner_UiHelper world) {
      return evaluate(input, yaw, world, KnockbackThrowPlanner.Inner_SyncManager.NONE);
   }

   static KnockbackThrowPlanner.Inner_JsonUtil evaluate(KnockbackThrowPlanner.Inner_ConfigStore input, float yaw, KnockbackThrowPlanner.Inner_UiHelper world, KnockbackThrowPlanner.Inner_SyncManager steer) {
      if (isValidInput(input) && Float.isFinite(yaw) && world != null) {
         if (steer == null) {
            steer = KnockbackThrowPlanner.Inner_SyncManager.NONE;
         }

         AABB body = input.body;
         Vec3 motion = pushedVelocity(input, yaw);
         boolean ground = input.grounded;
         boolean countering = steer != KnockbackThrowPlanner.Inner_SyncManager.NONE;
         int tick = 1;

         while (tick <= 64) {
            if (!world.init_Z_AABB(body.expandTowards(motion).inflate(0.001))) {
               return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, tick);
            }

            if (world.load_Z_AABB(body)) {
               return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.SAFE, 0.0, tick);
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
               if (!isFiniteVec(actual)) {
                  return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, tick);
               }

               if (world.load_Z_AABB(body.expandTowards(actual))) {
                  return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.SAFE, 0.0, tick);
               }

               if (world.read(body.expandTowards(actual))) {
                  return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.LAVA, 1.0 / 0.0, tick);
               }

               ground = wanted.y < 0.0 && actual.y > wanted.y + 1.0E-7;
               body = body.move(actual);
               if (world.load_Z_AABB(body)) {
                  return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.SAFE, 0.0, tick);
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
                  double drift = speed / 0.09 + 0.05 + (steer == KnockbackThrowPlanner.Inner_SyncManager.NONE ? 0 : 1);
                  Vec3 reach = speed > 1.0E-9 ? new Vec3(motion.x / speed * drift, 0.0, motion.z / speed * drift) : Vec3.ZERO;
                  AABB region = body.expandTowards(reach);
                  if (steer != KnockbackThrowPlanner.Inner_SyncManager.NONE) {
                     region = region.inflate(1.0, 0.0, 1.0);
                  }

                  KnockbackThrowPlanner.Inner_CachePool below = world.init_W_CachePool_AABB(region);
                  if (below != null && below.read && below.init_Z && !below.load) {
                     return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.VOID, 1.0 / 0.0, tick);
                  }
               }

               if ((!ground || !(motion.horizontalDistance() < 0.015)) && tick != 64) {
                  tick++;
                  continue;
               }

               if (motion.horizontalDistance() > 0.03) {
                  return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, tick);
               }

               KnockbackThrowPlanner.Inner_CachePool floor = world.init_W_CachePool_AABB(body);
               if (floor != null && floor.read) {
                  if (floor.load) {
                     return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.SAFE, 0.0, tick);
                  }

                  if (floor.write) {
                     return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.LAVA, 1.0 / 0.0, tick);
                  }

                  if (floor.init_Z) {
                     return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.VOID, 1.0 / 0.0, tick);
                  }

                  double drop = Math.max(0.0, input.read - floor.floorY);
                  return new KnockbackThrowPlanner.Inner_JsonUtil(drop > 1.0E-6 ? KnockbackThrowPlanner.Inner_TaskQueue.DROP : KnockbackThrowPlanner.Inner_TaskQueue.SAFE, drop, tick);
               }

               return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, tick);
            }

            return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, tick);
         }

         return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, 64);
      } else {
         return new KnockbackThrowPlanner.Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue.UNKNOWN, 0.0, 0);
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

   static boolean isValidInput(KnockbackThrowPlanner.Inner_ConfigStore i) {
      return i != null
         && i.body != null
         && isFiniteVec(i.body.getCenter())
         && isFiniteVec(i.attacker)
         && isFiniteVec(i.velocity)
         && i.body.getXsize() > 0.0
         && i.body.getXsize() <= 4.0
         && i.body.getYsize() > 0.0
         && i.body.getYsize() <= 4.0
         && i.body.getZsize() > 0.0
         && i.body.getZsize() <= 4.0
         && i.velocity.lengthSqr() <= 4.0
         && Double.isFinite(i.resistance)
         && i.resistance >= 0.0
         && i.resistance < 1.0
         && Double.isFinite(i.extra)
         && i.extra > 0.0
         && i.extra <= 4.0
         && Double.isFinite(i.read)
         && Math.abs(i.read - i.body.minY) <= 8.0
         && Math.hypot(i.body.getCenter().x - i.attacker.x, i.body.getCenter().z - i.attacker.z) > 1.0E-4;
   }

   private static boolean isFiniteVec(Vec3 v) {
      return v != null
         && Double.isFinite(v.x)
         && Double.isFinite(v.y)
         && Double.isFinite(v.z)
         && Math.abs(v.x) <= 3.0E7
         && Math.abs(v.y) <= 3.0E7
         && Math.abs(v.z) <= 3.0E7;
   }

   record Inner_BufferHelper(KnockbackThrowPlanner.Inner_SyncManager counter, Vec3[] velocities, double counterStrength) {
      static final KnockbackThrowPlanner.Inner_BufferHelper NO_HYPOTHESES = new KnockbackThrowPlanner.Inner_BufferHelper(KnockbackThrowPlanner.Inner_SyncManager.NONE, new Vec3[0]);

      Inner_BufferHelper(KnockbackThrowPlanner.Inner_SyncManager counter, Vec3[] velocities) {
         this(counter, velocities, counter != null && counter != KnockbackThrowPlanner.Inner_SyncManager.NONE ? counter.read() / 0.98 : 0.0);
      }
   }

   record Inner_CachePool(double floorY, boolean init_Z, boolean load, boolean read, boolean write) {
      Inner_CachePool(double y, boolean voidColumn, boolean caught, boolean known) {
         this(y, voidColumn, caught, known, false);
      }
   }

   record Inner_ConfigStore(AABB body, Vec3 attacker, Vec3 velocity, boolean grounded, double resistance, double extra, double read, boolean load_Z) {
      Inner_ConfigStore(AABB body, Vec3 attacker, Vec3 velocity, boolean grounded, double resistance, double extra) {
         this(body, attacker, velocity, grounded, resistance, extra, body == null ? 0.0 / 0.0 : body.minY, false);
      }

      KnockbackThrowPlanner.Inner_ConfigStore init(Vec3 origin) {
         return new KnockbackThrowPlanner.Inner_ConfigStore(this.body, origin, this.velocity, this.grounded, this.resistance, this.extra, this.read, this.load_Z);
      }

      KnockbackThrowPlanner.Inner_ConfigStore withVelocity(Vec3 motion) {
         return new KnockbackThrowPlanner.Inner_ConfigStore(this.body, this.attacker, motion, this.grounded, this.resistance, this.extra, this.read, this.load_Z);
      }
   }

   record Inner_DataUtil(AABB body, boolean init_Z, Vec3 motion, int ticks) {
   }

   record Inner_JsonUtil(KnockbackThrowPlanner.Inner_TaskQueue outcome, double dropDistance, int init_I) {
      boolean meetsGoal(double minimum, boolean voidOnly) {
         return this.outcome == KnockbackThrowPlanner.Inner_TaskQueue.VOID
            || !voidOnly
               && (this.outcome == KnockbackThrowPlanner.Inner_TaskQueue.LAVA || this.outcome == KnockbackThrowPlanner.Inner_TaskQueue.DROP && this.dropDistance + 1.0E-6 >= minimum);
      }
   }

   record Inner_NetHelper(float yaw, KnockbackThrowPlanner.Inner_JsonUtil init_W_JsonUtil, KnockbackThrowPlanner.Inner_JsonUtil original, boolean steer, boolean robust, double init_D) {
      Inner_NetHelper(float yaw, KnockbackThrowPlanner.Inner_JsonUtil result, KnockbackThrowPlanner.Inner_JsonUtil original) {
         this(yaw, result, original, false, false, 0.0);
      }
   }

   record Inner_StateCache(double maximumTurn, double load, boolean voidOnly, int side, double read) {
   }

   private record Inner_StreamUtil(float yaw, double init_D, TerrainDropGrid.Inner_CachePool landing) {
   }

   record Inner_SyncManager(double init_D, double load, double read, double write, boolean active) {
      static final KnockbackThrowPlanner.Inner_SyncManager NONE = new KnockbackThrowPlanner.Inner_SyncManager(0.0, 0.0, 0.0, 0.0);

      Inner_SyncManager(double x, double z, double speed, double air) {
         this(x, z, speed, air, false);
      }

      boolean init(double vx, double vz) {
         return !this.active || vx * this.init_D + vz * this.load < 0.0;
      }

      static KnockbackThrowPlanner.Inner_SyncManager init(float yaw, double speed, double air) {
         if (Float.isFinite(yaw) && Double.isFinite(speed) && Double.isFinite(air)) {
            double a = Math.toRadians(yaw);
            return new KnockbackThrowPlanner.Inner_SyncManager(-Math.sin(a), Math.cos(a), Math.max(0.0, Math.min(1.0, speed)) * 0.98, Math.max(0.0, Math.min(0.2, air)) * 0.98);
         } else {
            return NONE;
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

      KnockbackThrowPlanner.Inner_CachePool init_W_CachePool_AABB(AABB var1);

      double init();

      TerrainDropGrid.Inner_StreamUtil init_V_StreamUtil_I(int var1, int var2, int var3, double var4);

      default boolean init_Z_F(float yaw) {
         return true;
      }

      default boolean load_Z_F(float yaw) {
         return true;
      }

      default KnockbackThrowPlanner.Inner_ConfigStore init_W_ConfigStore_W_ConfigStore_F(KnockbackThrowPlanner.Inner_ConfigStore input, float yaw) {
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
