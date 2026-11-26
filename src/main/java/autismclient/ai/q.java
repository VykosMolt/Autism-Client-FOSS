package autismclient.ai;

import autismclient.ai.a.HashUtil;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class q {
   static final double init_D = 3.0;
   static final double load_D = 7.0;
   static final int[][] init_IArr2 = new int[][]{{4, 0}, {4, 1}, {4, -1}, {3, 1}, {3, -1}, {3, 2}, {3, -2}, {4, 2}, {4, -2}, {3, 0}};
   static final double read_D = 3.0;
   static final double write_D = 11.5;
   static final double update_D = 12.0;
   static final double sync = 3.5;
   static final double flush = 5.5;
   static final double close = 0.75;
   static final int init_I = 15;
   static final int load_I = 300;
   static final double open = 0.8;
   static final int read_I = 3;
   static final float init_F = 10.0F;
   static final int write_I = 4;
   static final int update_I = 3;

   private q() {
   }

   static boolean init_Z_F(float health, boolean bedPending, boolean recoveryActive, boolean childCommitted) {
      return health < 10.0F && !bedPending && !recoveryActive && !childCommitted;
   }

   static boolean init_Z_A_Str(A failure, String message) {
      return failure != A.pop && (message == null || !message.contains("dragon near the towers"));
   }

   static boolean init_Z_I_I(int failedClears, int usableBeds) {
      return failedClears >= 2 && usableBeds >= 4;
   }

   static int init_I_I_I(int endStoneHeld, int shortage) {
      return endStoneHeld + (int)Math.min(96L, Math.max(1L, shortage) + 8L);
   }

   static int init_I(JsonObject evidence) {
      return evidence.has("missing") && evidence.get("missing").isJsonObject()
         ? Math.max(0, ag.init_I_JsonObject(evidence.getAsJsonObject("missing"), "minecraft:dirt", 0))
         : 0;
   }

   static q.Inner_CachePool init_q_CachePool(String phaseName) {
      q.Inner_CachePool var10000;
      switch (phaseName) {
         case "Landing":
         case "SittingScanning":
         case "SittingFlaming":
         case "SittingAttacking":
            var10000 = q.Inner_CachePool.init;
            break;
         case "LandingApproach":
            var10000 = q.Inner_CachePool.load;
            break;
         case "Dying":
            var10000 = q.Inner_CachePool.write;
            break;
         default:
            var10000 = q.Inner_CachePool.read;
      }

      return var10000;
   }

   static q.Inner_CachePool init(double horizontal, double dy, double speed) {
      if (horizontal <= 8.0 && dy >= -2.0 && dy <= 10.0 && speed < 2.0) {
         return q.Inner_CachePool.init;
      } else {
         return horizontal <= 18.0 && dy >= -2.0 && dy <= 25.0 && speed < 8.0 ? q.Inner_CachePool.load : q.Inner_CachePool.read;
      }
   }

   static String init(boolean crystalsRemain, q.Inner_CachePool perch, boolean armed, boolean window, boolean canBed, boolean meleeOk, boolean bowOk) {
      if (crystalsRemain && !canBed) {
         return "crystals";
      }

      if (armed) {
         return window ? "detonate" : "arm_wait";
      }

      if (perch == q.Inner_CachePool.write) {
         return "wait";
      }

      if (!canBed || perch != q.Inner_CachePool.init && perch != q.Inner_CachePool.load) {
         if (perch == q.Inner_CachePool.init && meleeOk) {
            return "melee";
         } else if (crystalsRemain && canBed) {
            return "wait";
         } else {
            return perch == q.Inner_CachePool.read && bowOk ? "bow" : "wait";
         }
      } else {
         return "bed";
      }
   }

   static boolean init_Z_Z_D(boolean perched, double headDistanceToBed) {
      return headDistanceToBed <= 3.5 ? true : perched && headDistanceToBed <= 5.5;
   }

   static int[] init(int fx, int fz, double px, double pz, double radius) {
      double dx = px - (fx + 0.5);
      double dz = pz - (fz + 0.5);
      double len = Math.hypot(dx, dz);
      double ux = len < 1.0E-6 ? 1.0 : dx / len;
      double uz = len < 1.0E-6 ? 0.0 : dz / len;
      return new int[]{(int)Math.round(fx + ux * radius), (int)Math.round(fz + uz * radius)};
   }

   static int[] init_IArr_I(int fx, int fz, double px, double pz) {
      return init(fx, fz, px, pz, 3.0);
   }

   static int[] load(int bx, int bz, double px, double pz, double distance) {
      return init(bx, bz, px, pz, distance);
   }

   static List<double[]> init_List(Minecraft mc) {
      List<double[]> out = new ArrayList<>();
      if (mc.player != null && mc.level != null) {
         for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof AreaEffectCloud cloud
               && !entity.isRemoved()
               && cloud.distanceToSqr(mc.player) < 576.0
               && Math.abs(cloud.getY() - mc.player.getY()) < 2.5) {
               out.add(new double[]{cloud.getX(), cloud.getZ(), cloud.getRadius()});
            }
         }

         return out;
      } else {
         return out;
      }
   }

   static boolean init_Z(Minecraft mc) {
      return mc.player != null && !init_Z_D(mc.player.getX(), mc.player.getZ(), init_List(mc), -0.45);
   }

   static AiOperation init_AiOperation(Minecraft mc) {
      return init_AiOperation_Minecraft_D(mc, 1.0);
   }

   static AiOperation init_AiOperation_Minecraft_D(Minecraft mc, double extra) {
      List<double[]> clouds = init_List(mc);
      r.Inner_DataUtil zones = r.init_Z(mc) ? r.init_r_DataUtil(mc) : r.Inner_DataUtil.NONE;
      if (clouds.isEmpty()) {
         return null;
      }

      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-8, -2, -8), feet.offset(8, 2, 8))) {
         if (init_Z_D(p.getX() + 0.5, p.getZ() + 0.5, clouds, extra) && !zones.hits(r.init_AABB_I(p.getX(), p.getY(), p.getZ()), 1.0)) {
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

      return best == null ? null : X.init(HashUtil.init(best, 0.35, true, true, false, 6, autismclient.ai.a.ClassCache.Inner_IoWorker.init, true));
   }

   static boolean init_Z(double x, double z, List<double[]> clouds) {
      return init_Z_D(x, z, clouds, 0.0);
   }

   static boolean init_Z_D(double x, double z, List<double[]> clouds, double extra) {
      for (double[] cloud : clouds) {
         double dx = x - cloud[0];
         double dz = z - cloud[1];
         double reach = cloud[2] + 0.75 + extra;
         if (dx * dx + dz * dz < reach * reach) {
            return false;
         }
      }

      return true;
   }

   static double[] init_DArr(double x, double z, List<double[]> clouds) {
      if (init_Z(x, z, clouds)) {
         return new double[]{x, z};
      }

      for (double radius : new double[]{2.0, 4.0, 6.0}) {
         for (int step = 0; step < 8; step++) {
            double angle = step * 3.141592653589793 / 4.0;
            double cx = x + Math.cos(angle) * radius;
            double cz = z + Math.sin(angle) * radius;
            if (init_Z(cx, cz, clouds)) {
               return new double[]{cx, cz};
            }
         }
      }

      return new double[]{x, z};
   }

   static boolean init_Z_I(int beds, int bows, int crossbows, int arrows) {
      return beds > 0 || bows + crossbows > 0 && arrows > 0;
   }

   static boolean init_Z(String itemPath) {
      return itemPath.endsWith("_bed");
   }

   static JsonObject init() {
      JsonObject missing = new JsonObject();
      missing.addProperty("minecraft:white_bed", 1);
      missing.addProperty("minecraft:bow", 1);
      missing.addProperty("minecraft:arrow", 8);
      return missing;
   }

   static boolean init_Z_Z_Z(boolean statMet, boolean portalActive) {
      return statMet || portalActive;
   }

   static AiOperation init_AiOperation(final JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (mc.level.dimension() != Level.END) {
            throw new IllegalArgumentException("The dragon fight requires the End; beds must never be used as explosions in the Overworld");
         }

         final int bedsMax = Math.max(0, Math.min(64, ag.init_I_JsonObject(args, "beds", 0)));
         final boolean melee = ag.init_Z_JsonObject(args, "melee", true);
         final boolean bow = ag.init_Z_JsonObject(args, "bow", true);
         final int timeoutSeconds = Math.max(30, Math.min(3600, ag.init_I_JsonObject(args, "timeoutSeconds", 900)));
         cc service = cc.load_cc();
         final boolean silentAim = service != null && service.init_AiSettings().aiSilentAim;
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object init_Obj = mc.level;
            final Object load_Obj = mc.getConnection();
            final UUID init_UUID = mc.player.getUUID();
            final long load_J = ca.load_J();
            final autismclient.ai.a.DataUtil init_DataUtil = new autismclient.ai.a.DataUtil();
            final q.Inner_StreamUtil init_q_StreamUtil = new q.Inner_StreamUtil();
            final X.Inner_CachePool init_X_CachePool = X.Inner_CachePool.standard().withoutFlee();
            boolean init_Z;
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiOperation read_AiOperation;
            AiOperation write_AiOperation;
            AiOperation update_AiOperation;
            AiOperation sync_AiOperation;
            BlockPos init_BlockPos;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            BlockPos write_BlockPos;
            BlockPos update_BlockPos;
            BlockPos sync_BlockPos;
            EnderDragon init_EnderDragon;
            q.Inner_CachePool init_q_CachePool = q.Inner_CachePool.read;
            q.Inner_CachePool load_q_CachePool = q.Inner_CachePool.read;
            Inner_DataUtil init_q_1_DataUtil = Inner_DataUtil.init;
            String init_Str = "";
            String load_Str = "";
            String read_Str;
            JsonObject init_JsonObject;
            long read_J;
            int init_I;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close_I;
            int open_I;
            int poll_I;
            int push_I;
            int pop_I;
            int tick_I;
            int run;
            BlockPos flush_BlockPos;
            int get;
            float init_F = -1.0F;
            int set;
            int put;
            int add;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z = ag.init_Z_JsonObject(args, "crystalsFirst", this.init_I_38() < 4);
            BlockPos close_BlockPos;
            BlockPos open_BlockPos;
            int clear = -1000;
            boolean update_Z;
            boolean sync_Z;
            int reset;
            int start;
            int stop;
            int next;
            float load_F;
            boolean flush_Z;
            BlockPos poll_BlockPos;
            BlockPos push_BlockPos;
            long write_J;
            long update_J;
            double init_D;
            double load_D;
            int prev;
            BlockPos pop_BlockPos;
            BlockPos tick_BlockPos;
            long sync_J;
            final Set<Long> init_Set = new HashSet<>();
            boolean close_Z;
            boolean open_Z;
            boolean poll_Z = true;
            boolean push_Z = true;
            boolean pop_Z;
            boolean tick_Z;
            String write_Str = "";

            {
               this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.a.DataUtil.Inner_DataUtil.init_DataUtil_DataUtil);
               this.init_DataUtil.init_V_Z(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               X.init_V();
               if (this.tick_Z || mc.player == null || mc.level == null) {
                  return this.init(AiOperation.Status.CANCELLED, A.size, "Dragon fight cancelled");
               }

               if (this.init_q_StreamUtil.init()) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, "Interrupted dragon interaction needs observation");
               }

               if (mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || !mc.player.getUUID().equals(this.init_UUID)) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, "Dragon fight world, connection or player changed");
               }

               if (!mc.player.isAlive() || this.load_J != ca.load_J()) {
                  return this.init(AiOperation.Status.FAILED, A.start, "Death ended this dragon fight");
               }

               if (System.nanoTime() > this.init_J) {
                  return this.init(
                     this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                     A.prev,
                     "Dragon fight deadline reached (" + this.init_I + " beds, " + this.load_I + " bow shots, " + this.read_I + " swings)"
                  );
               }

               if (!this.open_Z) {
                  this.open_Z = true;
                  if (!q.init_Z_I(this.init_I_38(), S.init_I_Str("minecraft:bow"), S.init_I_Str("minecraft:crossbow"), bK.write()) && !this.flush_Z_4()) {
                     this.init_JsonObject = q.init();
                     return this.init(
                        A.pop,
                        "No beds and no bow with arrows; the circling dragon cannot be punched - obtain a bed (beds explode in the End) or a bow with arrows"
                     );
                  }
               }

               EnderDragon dragon = this.init_EnderDragon();
               this.init_EnderDragon = dragon;
               if (dragon == null && this.flush_Z) {
                  return this.run();
               }

               if (dragon != null) {
                  this.close_Z = true;
               }

               if (this.init_BlockPos == null) {
                  this.init_BlockPos = this.load_BlockPos_6();
               }

               if (dragon != null) {
                  this.init_EnderDragon = dragon;
                  if (dragon.getHealth() <= 0.0F) {
                     this.init_Z = true;
                     return this.flush_Z ? this.run() : this.init_AiOperation_Result_42();
                  }

                  this.init_Str = this.init_Str(dragon);
                  this.init_q_CachePool = this.init(dragon, this.init_Str);
                  this.init(this.init_q_CachePool);
                  if (q.init_Z_F(
                     mc.player.getHealth(),
                     this.flush_Z,
                     X.close(),
                     this.init_q_1_DataUtil == Inner_DataUtil.load && this.init_AiOperation != null && !this.init_AiOperation.canInterruptNow()
                  )) {
                     return this.init(A.start, "Health " + String.format("%.1f", mc.player.getHealth()) + ": healing before the next perch");
                  }

                  AiOperation.Result charged = this.init_AiOperation_Result(dragon);
                  if (charged != null) {
                     return charged;
                  }

                  AiOperation.Result zoned = this.load_AiOperation_Result_19();
                  if (zoned != null) {
                     return zoned;
                  }

                  AiOperation.Result guarded = this.read_AiOperation_Result_14();
                  if (guarded != null) {
                     return guarded;
                  }

                  AiOperation.Result doused = this.write_AiOperation_Result_8();
                  if (doused != null) {
                     return doused;
                  }

                  if (this.init_q_1_DataUtil != Inner_DataUtil.close
                     && this.init_q_1_DataUtil != Inner_DataUtil.open
                     && this.init_q_1_DataUtil != Inner_DataUtil.update) {
                     AiOperation.Result dodged = this.update_AiOperation_Result_5();
                     if (dodged != null) {
                        return dodged;
                     }
                  }

                  if (this.flush_Z) {
                     return this.run();
                  }

                  AiOperation.Result var10000;
                  switch (this.init_q_1_DataUtil) {
                     case init:
                        var10000 = this.sync_AiOperation_Result_5();
                        break;
                     case load:
                        var10000 = this.flush_AiOperation_Result_3();
                        break;
                     case read:
                        var10000 = this.close();
                        break;
                     case write:
                        var10000 = this.open();
                        break;
                     case update:
                        var10000 = this.poll();
                        break;
                     case sync:
                        var10000 = this.push();
                        break;
                     case flush:
                        var10000 = this.pop();
                        break;
                     case close:
                        var10000 = this.run();
                        break;
                     case open:
                        var10000 = this.get();
                        break;
                     case poll:
                        var10000 = this.set();
                        break;
                     case push:
                        var10000 = this.put();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else if (this.close_Z) {
                  return this.init_AiOperation_Result_42();
               } else {
                  return this.read_Z_12()
                     ? this.load("The dragon is already defeated; its active central exit is observed")
                     : this.init(A.init_A, "No ender dragon is loaded and no kill is recorded; go to the End's central island");
               }
            }

            AiOperation.Result init_AiOperation_Result_42() {
               this.load_V_15();
               boolean portal = this.read_Z_12();
               if (q.init_Z_Z_Z(this.init_Z, portal)) {
                  return this.load("Ender dragon dead (confirmed by " + (this.init_Z ? "observed zero health" : "the active exit portal") + ")");
               }

               if (this.read_J == 0L) {
                  this.read_J = System.nanoTime();
               }

               return System.nanoTime() - this.read_J > 15000000000L
                  ? this.init(AiOperation.Status.UNCERTAIN, null, "Dragon gone without observed zero health or an active central exit; check the fountain")
                  : this.init("Dragon gone; confirming the kill");
            }

            AiOperation.Result init_AiOperation_Result(EnderDragon dragon) {
               if (this.sync_AiOperation != null) {
                  AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.sync, this.sync_AiOperation);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.init("Out of the dragon's charge - " + r.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.sync, this.sync_AiOperation);
                  this.sync_AiOperation = null;
                  return this.init("Clear of the charge");
               } else if (!cv.init_Z() && "ChargingPlayer".equals(this.init_Str)) {
                  Vec3 v = dragon.getDeltaMovement();
                  double speed = Math.hypot(v.x, v.z);
                  if (speed < 0.05) {
                     return null;
                  }

                  double dx = v.x / speed;
                  double dz = v.z / speed;
                  double tx = mc.player.getX() - dragon.getX();
                  double tz = mc.player.getZ() - dragon.getZ();
                  double along = tx * dx + tz * dz;
                  if (!(along < -2.0) && !(along > 48.0)) {
                     double off = tx * -dz + tz * dx;
                     if (Math.abs(off) > 9.0) {
                        return null;
                     }

                     double side = off >= 0.0 ? 1.0 : -1.0;
                     BlockPos feet = mc.player.blockPosition();
                     BlockPos best = null;
                     double bestScore = 1.7976931348623157E308;

                     for (BlockPos p : BlockPos.betweenClosed(feet.offset(-12, -3, -12), feet.offset(12, 3, 12))) {
                        double px = p.getX() + 0.5 - dragon.getX();
                        double pz = p.getZ() + 0.5 - dragon.getZ();
                        double pOff = (px * -dz + pz * dx) * side;
                        if (!(pOff < 10.0)) {
                           BlockPos below = p.below();
                           if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                              && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                              && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                              && ValueSink.init_Z_Minecraft_BlockPos(mc, p)
                              && q.init_Z(p.getX() + 0.5, p.getZ() + 0.5, this.init_List_11())) {
                              double score = p.distSqr(feet);
                              if (score < bestScore) {
                                 bestScore = score;
                                 best = p.immutable();
                              }
                           }
                        }
                     }

                     if (best == null) {
                        return null;
                     }

                     if (this.load_AiOperation != null) {
                        this.init_q_StreamUtil.load(q.Inner_DataUtil.load, this.load_AiOperation);
                        this.load_AiOperation = null;
                     }

                     this.load_V_15();
                     this.sync_AiOperation = HashUtil.init(best, 0.8, false, false, false, 5, autismclient.ai.a.ClassCache.Inner_IoWorker.init, true);
                     cl.init_V_Str_ObjArr(
                        "[AI] dragon: charging at the player ({} off its line); stepping to {}", String.format(Locale.ROOT, "%.1f", off), best.toShortString()
                     );
                     return this.init("The dragon charges; stepping out of its line");
                  } else {
                     return null;
                  }
               } else {
                  return null;
               }
            }

            AiOperation.Result load_AiOperation_Result_19() {
               if (this.update_AiOperation != null) {
                  AiOperation.Result result = this.init_q_StreamUtil
                     .init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.update, this.update_AiOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.init("Out of the wing push - " + result.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.update, this.update_AiOperation);
                  this.update_AiOperation = null;
                  return this.init("Clear of the dragon's body");
               } else {
                  if (cv.init_Z()) {
                     return null;
                  }

                  boolean inZone = r.init_Z(mc) && r.init_r_DataUtil(mc).hits(mc.player.getBoundingBox(), 0.6);
                  boolean clouded = this.init_q_1_DataUtil != Inner_DataUtil.close && !mc.player.isInWater() && q.init_Z(mc);
                  if (!inZone && !clouded) {
                     return null;
                  }

                  BlockPos at = mc.player.blockPosition();
                  this.pop_I = at.equals(this.tick_BlockPos) && mc.player.tickCount - this.tick_I < 60 ? this.pop_I + 1 : 0;
                  double extra = 1.0 + Math.min(4, this.pop_I) * 1.5;
                  AiOperation out = inZone ? r.init(mc, 1.0 + extra) : q.init_AiOperation_Minecraft_D(mc, extra);
                  if (out == null && inZone && clouded) {
                     out = q.init_AiOperation_Minecraft_D(mc, extra);
                  }

                  if (out == null && clouded) {
                     out = r.init_AiOperation(mc);
                  }

                  if (out == null) {
                     return null;
                  }

                  this.tick_BlockPos = at;
                  this.tick_I = mc.player.tickCount;
                  if (this.init_q_1_DataUtil == Inner_DataUtil.write && this.write_BlockPos != null && ++this.run >= 2) {
                     this.init_Set.add(this.write_BlockPos.asLong());
                     this.write_BlockPos = null;
                     this.close_BlockPos = null;
                     this.update_BlockPos = null;
                     this.sync_J = System.nanoTime() + 1000000000L;
                  }

                  if (this.load_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.load, this.load_AiOperation);
                     this.load_AiOperation = null;
                  }

                  if (this.write_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.write, this.write_AiOperation);
                     this.write_AiOperation = null;
                  }

                  this.load_V_15();
                  if (this.init_q_1_DataUtil == Inner_DataUtil.close && !this.flush_Z) {
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                  }

                  this.update_AiOperation = out;
                  cl.init_V_Str_ObjArr(
                     "[AI] dragon: inside the wing push or the breath at {} ({}); stepping out",
                     mc.player.blockPosition().toShortString(),
                     this.init_q_1_DataUtil
                  );
                  return this.init("Inside the dragon's wing push or breath; stepping out");
               }
            }

            AiOperation.Result read_AiOperation_Result_14() {
               if (this.load_AiOperation != null) {
                  AiOperation.Result result = this.init_q_StreamUtil
                     .init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.load, this.load_AiOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.init("Interrupted: " + this.load_Str + " - " + result.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.load, this.load_AiOperation);
                  this.load_AiOperation = null;
                  return this.init("Resuming after " + this.load_Str);
               } else {
                  X.Inner_BufferHelper vitals = X.init_X_BufferHelper();
                  if (!this.canInterruptNow()) {
                     return null;
                  }

                  AiOperation action = X.init_AiOperation_X_CachePool(this.init_X_CachePool, vitals, this.read_I_7());
                  if (action == null) {
                     return null;
                  }

                  this.load_Str = X.init_Str(this.init_X_CachePool, vitals);
                  this.sync_I++;
                  this.load_V_15();
                  this.load_AiOperation = action;
                  return this.init("Interrupted: " + this.load_Str);
               }
            }

            AiOperation.Result write_AiOperation_Result_8() {
               if (this.write_AiOperation != null) {
                  AiOperation.Result result = this.init_q_StreamUtil
                     .init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.write, this.write_AiOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.init("Putting the fire out - " + result.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.write, this.write_AiOperation);
                  this.write_AiOperation = null;
                  return this.init("Fire handled");
               } else if (this.init_q_1_DataUtil != Inner_DataUtil.close && !mc.player.isInWater()) {
                  if (q.init_Z(mc)) {
                     AiOperation out = q.init_AiOperation(mc);
                     if (out != null) {
                        this.load_V_15();
                        this.write_AiOperation = out;
                        return this.init("In the dragon's breath; out of it");
                     }
                  }

                  boolean inFire = u.init_Z(mc);
                  if (!inFire && !mc.player.isOnFire()) {
                     return null;
                  }

                  AiOperation op = u.init_AiOperation(mc);
                  if (op == null && inFire) {
                     op = u.load_AiOperation(mc);
                  }

                  if (op == null) {
                     return null;
                  }

                  this.load_V_15();
                  this.write_AiOperation = op;
                  return this.init("On fire; putting it out");
               } else {
                  return null;
               }
            }

            AiOperation.Result update_AiOperation_Result_5() {
               if (this.read_AiOperation != null) {
                  AiOperation.Result result = this.init_q_StreamUtil
                     .init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.read, this.read_AiOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.init("Dodging a fireball - " + result.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.read, this.read_AiOperation);
                  this.read_AiOperation = null;
                  return this.init("Resuming after the dodge");
               } else {
                  boolean threat = false;

                  for (by.Inner_StreamUtil tracked : by.init(mc)) {
                     if (by.init(tracked)) {
                        threat = true;
                        break;
                     }
                  }

                  if (!threat) {
                     return null;
                  }

                  this.load_V_15();
                  this.update_I++;
                  this.read_AiOperation = p.init(ag.init_JsonObject_ObjArr("deflect", true, "timeoutSeconds", 4));
                  return this.init("Dragon fireball inbound; dodging");
               }
            }

            AiOperation.Result sync_AiOperation_Result_5() {
               if (this.init_BlockPos == null) {
                  this.init_BlockPos = this.load_BlockPos_6();
               }

               if (!this.poll_Z && !this.flush_Z) {
                  this.load_BlockPos = null;
                  this.pop_BlockPos = null;
               }

               boolean finishable = this.init_EnderDragon != null && this.init_EnderDragon.getHealth() <= 90.0F;
               boolean canBed = this.update_Z_7() && (!this.write_Z || !this.init_Z_27() || finishable);
               boolean meleeOk = melee && this.init_BlockPos != null;
               boolean bowReady = bow && this.push_Z && this.sync_Z_6();
               boolean window = this.load_BlockPos != null && this.write_Z_9();
               String action = q.init(this.init_Z_27(), this.init_q_CachePool, this.load_BlockPos != null, window, canBed, meleeOk, bowReady);
               if (this.init_Z_27() && this.init_EnderDragon != null && this.init_EnderDragon.getHealth() > 90.0F && !"detonate".equals(action)) {
                  action = "crystals";
               }

               if (!action.equals(this.write_Str)) {
                  cl.init_V_Str_ObjArr(
                     "[AI] dragon: {} (perch {}, phase {}, health {}, beds {} usable {}, fountain {}, armed {}, bow {})",
                     action,
                     this.init_q_CachePool,
                     this.init_Str,
                     this.init_EnderDragon == null ? "?" : String.format(Locale.ROOT, "%.0f", this.init_EnderDragon.getHealth()),
                     this.init_I_38(),
                     this.poll_Z,
                     this.init_BlockPos == null ? "unknown" : this.init_BlockPos.toShortString(),
                     this.load_BlockPos != null,
                     bowReady
                  );
                  this.write_Str = action;
               }

               AiOperation.Result var10000;
               switch (action) {
                  case "crystals":
                     this.init_q_1_DataUtil = Inner_DataUtil.load;
                     var10000 = this.init("End crystals remain; clearing them first");
                     break;
                  case "detonate":
                     this.init_q_1_DataUtil = Inner_DataUtil.close;
                     var10000 = this.init("The head is over the fountain; detonating the bed");
                     break;
                  case "arm_wait":
                     this.init_q_1_DataUtil = Inner_DataUtil.flush;
                     var10000 = this.init("Bed armed; holding for the head");
                     break;
                  case "bed":
                     this.init_q_1_DataUtil = Inner_DataUtil.write;
                     var10000 = this.init(
                        this.init_q_CachePool == q.Inner_CachePool.load ? "Landing approach; pre-placing a bed" : "Perch window; placing a bed"
                     );
                     break;
                  case "melee":
                     this.init_q_1_DataUtil = Inner_DataUtil.open;
                     this.open_I = 0;
                     var10000 = this.init("Meleeing the perched dragon");
                     break;
                  case "bow":
                     this.init_q_1_DataUtil = Inner_DataUtil.poll;
                     var10000 = this.init("Chipping the circling dragon with the bow");
                     break;
                  default:
                     this.init_q_1_DataUtil = Inner_DataUtil.push;
                     var10000 = this.init("Holding by the fountain for the next perch");
               }

               return var10000;
            }

            AiOperation.Result flush_AiOperation_Result_3() {
               if (!this.write_Z
                  || this.init_q_CachePool != q.Inner_CachePool.init
                  || !this.poll_Z
                  || this.init_I_38() <= 0
                  || this.init_EnderDragon == null
                  || !(this.init_EnderDragon.getHealth() <= 90.0F)
                  || this.init_AiOperation != null && !this.init_AiOperation.canInterruptNow()) {
                  if (this.init_AiOperation == null) {
                     if (this.read_I_7() < 35) {
                        return this.init(A.prev, "Too little time left to clear the remaining crystals");
                     }

                     this.init_AiOperation = h.init(ag.init_JsonObject_ObjArr("timeoutSeconds", this.init(600), "skipCaged", !cv.init_Z()));
                  }

                  AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.init("Clearing crystals: " + r.message());
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                  this.init_AiOperation = null;
                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.init(AiOperation.Status.CANCELLED, A.size, "Dragon fight cancelled");
                  }

                  if (r.failure() == A.pop && !cv.init_Z()) {
                     this.init_V(r);
                     int shortage = q.init_I(bK.init_JsonObject(r.dataJson()));
                     if (shortage > 0 && this.put < 3 && this.read_I_7() >= 45) {
                        this.put++;
                        this.add = q.init_I_I_I(S.init_I_Str("minecraft:end_stone"), shortage);
                        this.init_q_1_DataUtil = Inner_DataUtil.read;
                        return this.init("Gathering nearby End stone for the remaining crystal towers");
                     } else {
                        this.load_Z = true;
                        return this.init(
                           AiOperation.Status.FAILED, A.pop, "Crystal construction remains short; bounded local recovery is unavailable: " + r.message()
                        );
                     }
                  } else if ((r.status() == AiOperation.Status.FAILED || r.status() == AiOperation.Status.TIMED_OUT) && !cv.init_Z() && !this.load_Z_16()) {
                     if (mc.player.getHealth() < 16.0F) {
                        return this.init(A.start, "Health " + String.format("%.1f", mc.player.getHealth()) + ": healing before the caged crystals");
                     }

                     if (q.init_Z_A_Str(r.failure(), r.message())) {
                        this.set++;
                     }

                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Open crystals cleared; the caged wait");
                  } else if (r.status() != AiOperation.Status.FAILED && r.status() != AiOperation.Status.TIMED_OUT) {
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Crystals cleared; engaging the dragon");
                  } else {
                     this.init_V(r);
                     cl.init_V_Str_ObjArr(
                        "[AI] dragon: crystal outcomes {}",
                        String.valueOf(JsonParser.parseString(r.dataJson() == null ? "{}" : r.dataJson()).getAsJsonObject().get("outcomes"))
                     );
                     return this.init(r.status(), r.failure(), "Crystal clearing failed: " + r.message());
                  }
               } else {
                  this.load_V_15();
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("It sits; beds before the remaining crystals");
               }
            }

            AiOperation.Result close() {
               if (this.init_AiOperation == null) {
                  this.init_AiOperation = autismclient.ai.a.BufferHelper.init_AiOperation(
                     ag.init_JsonObject_ObjArr(
                        "block",
                        "minecraft:end_stone",
                        "drop",
                        "minecraft:end_stone",
                        "held",
                        this.add,
                        "radius",
                        16,
                        "explore",
                        false,
                        "toolPrepared",
                        true,
                        "timeoutSeconds",
                        this.init(90)
                     )
                  );
               }

               AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               if (!r.terminal()) {
                  return this.init("Gathering nearby crystal construction blocks: " + r.message());
               } else {
                  this.load_V_15();
                  if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                     return this.init(r.status(), r.failure(), r.message());
                  } else if (r.status() == AiOperation.Status.SUCCEEDED && S.init_I_Str("minecraft:end_stone") >= this.add) {
                     this.init_JsonObject = null;
                     this.read_Str = null;
                     this.load_Z = false;
                     this.init_q_1_DataUtil = Inner_DataUtil.load;
                     return this.init("Crystal construction replenished; retrying the remaining towers");
                  } else {
                     this.load_Z = true;
                     return this.init(AiOperation.Status.FAILED, A.pop, "Nearby crystal construction recovery did not finish: " + r.message());
                  }
               }
            }

            AiOperation.Result open() {
               if (this.init_q_CachePool != q.Inner_CachePool.init && this.init_q_CachePool != q.Inner_CachePool.load) {
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("The perch window closed; re-planning");
               }

               EnderDragon sitter = r.init_EnderDragon(mc);
               double fromFountain = this.init_BlockPos == null
                  ? 0.0
                  : Math.hypot(mc.player.getX() - this.init_BlockPos.getX() - 0.5, mc.player.getZ() - this.init_BlockPos.getZ() - 0.5);
               boolean seated = sitter != null && sitter.getPhaseManager().getCurrentPhase().isSitting();
               if (this.init_BlockPos != null && (fromFountain > 16.0 || fromFountain < 11.0 && !seated)) {
                  if (this.init_AiOperation == null || !this.sync_Z) {
                     BlockPos near = this.sync_BlockPos();
                     if (near != null) {
                        if (this.init_AiOperation != null) {
                           this.load_V_15();
                        }

                        this.init_AiOperation = HashUtil.init(
                           near, 1.5, false, true, true, this.init(20), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                        );
                        this.sync_Z = true;
                     }
                  }

                  if (this.init_AiOperation != null && this.sync_Z) {
                     AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.init("Closing in on the perch");
                     }

                     this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                     this.init_AiOperation = null;
                     this.sync_Z = false;
                  }
               }

               if (sitter != null && sitter.getPhaseManager().getCurrentPhase().isSitting()) {
                  if (this.init_AiOperation == null && this.load_BlockPos == null) {
                     BlockPos standing = this.update_BlockPos();
                     if (standing != null) {
                        this.load_BlockPos = standing;
                        this.update_BlockPos = null;
                        this.write_BlockPos = null;
                        this.close_BlockPos = null;
                        this.init_q_1_DataUtil = Inner_DataUtil.update;
                        cl.init_V_Str_ObjArr("[AI] dragon: a bed already stands at {}; arming it", standing.toShortString());
                        return this.init("A bed already stands by the fountain; backing out of the blast");
                     }
                  }

                  if (this.write_BlockPos == null) {
                     if (System.nanoTime() < this.sync_J) {
                        return this.init("Waiting for the dragon to turn; no bed cell clear of its wings yet");
                     }

                     this.write_BlockPos = r.init_Z(mc) ? this.write_BlockPos_2() : this.read_BlockPos_5();
                     this.run = 0;
                     if (this.write_BlockPos == null && r.init_Z(mc)) {
                        this.sync_J = System.nanoTime() + 500000000L;
                        return this.init("Waiting for the dragon to turn; no bed cell clear of its wings yet");
                     }

                     if (this.write_BlockPos == null) {
                        this.poll_Z = false;
                        cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "No placeable bed cell by the fountain; falling back to melee and bow");
                        this.init_q_1_DataUtil = Inner_DataUtil.init;
                        return this.init("No placeable bed cell by the fountain; falling back to melee and bow");
                     }
                  }

                  if (this.init_AiOperation == null) {
                     String bedId = this.load_Str_5();
                     if (bedId == null || af.read(af.load_af_DataUtil(bedId)) < 0) {
                        this.write_BlockPos = null;
                        this.update_BlockPos = null;
                        this.poll_Z = false;
                        cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "No bed can be brought to hand; falling back to melee and bow");
                        this.init_q_1_DataUtil = Inner_DataUtil.init;
                        return this.init("No bed can be brought to hand; falling back to melee and bow");
                     }

                     if (this.update_BlockPos != null && this.init(this.update_BlockPos)) {
                        if (++this.next > 20) {
                           this.next = 0;
                           if (this.write_BlockPos != null) {
                              this.init_Set.add(this.write_BlockPos.asLong());
                           }

                           this.write_BlockPos = null;
                           this.close_BlockPos = null;
                           this.update_BlockPos = null;
                           this.sync_J = 0L;
                           return this.init("Dragon body blocks that bed cell; choosing another");
                        }

                        return this.init("Waiting for the dragon's body to clear the bed cell");
                     }

                     this.next = 0;
                     if (this.update_BlockPos != null && this.load(this.update_BlockPos)) {
                        this.load_BlockPos = this.update_BlockPos;
                        this.write_BlockPos = null;
                        this.close_BlockPos = null;
                        this.update_BlockPos = null;
                        this.pop_BlockPos = null;
                        this.init_q_1_DataUtil = Inner_DataUtil.update;
                        return this.init("A bed already stands in the planned cell; arming it");
                     }

                     if (this.close_BlockPos != null && mc.player.position().distanceToSqr(Vec3.atBottomCenterOf(this.close_BlockPos)) > 0.48999999999999994) {
                        this.init_AiOperation = HashUtil.init(
                           this.close_BlockPos, 0.3, false, true, true, this.init(15), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                        );
                        this.update_Z = true;
                     } else {
                        this.init_AiOperation = R.init(
                           "place",
                           ag.init_JsonObject_ObjArr("x", this.write_BlockPos.getX(), "y", this.write_BlockPos.getY(), "z", this.write_BlockPos.getZ())
                        );
                        this.update_Z = false;
                     }
                  }

                  AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                  if (!r.terminal()) {
                     return this.init(this.update_Z ? "To the bed stance clear of the dragon" : "Placing the bed beside the fountain");
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                  this.init_AiOperation = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     if (this.update_BlockPos != null && this.load(this.update_BlockPos)) {
                        this.load_BlockPos = this.update_BlockPos;
                        this.update_BlockPos = null;
                        this.init_I++;
                        this.init_q_1_DataUtil = Inner_DataUtil.update;
                        return this.init("Bed appeared unconfirmed; arming it rather than replacing it");
                     }

                     BlockPos missed = this.write_BlockPos;
                     if (missed != null) {
                        this.init_Set.add(missed.asLong());
                     }

                     this.write_BlockPos = null;
                     this.close_BlockPos = null;
                     this.update_BlockPos = null;
                     this.sync_J = 0L;
                     this.push_I++;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     if (this.push_I < 3) {
                        return this.init("Bed click unconfirmed; choosing another cell");
                     }

                     this.poll_Z = false;
                     this.read_Z = true;
                     cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "three unconfirmed bed clicks (until the next perch)");
                     return this.init("Bed click unconfirmed three times; falling back to melee and bow");
                  } else {
                     if (this.update_Z) {
                        this.update_Z = false;
                        this.close_BlockPos = null;
                        return this.init("At the bed stance");
                     }

                     BlockPos triedSupport = this.write_BlockPos;
                     this.write_BlockPos = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && (this.update_BlockPos == null || !this.load(this.update_BlockPos))) {
                        this.update_BlockPos = null;
                        this.push_I++;
                        if (triedSupport != null) {
                           this.init_Set.add(triedSupport.asLong());
                        }

                        cl.init_V_Str_ObjArr(
                           "[AI] dragon: bed placement failed at {} ({}: {}), failure {}",
                           triedSupport == null ? "?" : triedSupport.toShortString(),
                           r.status(),
                           r.message(),
                           this.push_I
                        );
                        this.init_q_1_DataUtil = Inner_DataUtil.init;
                        if (this.push_I < 3) {
                           return this.init("Bed placement failed (" + r.message() + "); trying another cell");
                        }

                        this.poll_Z = false;
                        this.read_Z = true;
                        cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "three bed placements failed (until the next perch)");
                        return this.init("Bed placement failed (" + r.message() + "); falling back to melee and bow");
                     } else {
                        this.load_BlockPos = this.update_BlockPos;
                        this.pop_BlockPos = null;
                        this.update_BlockPos = null;
                        this.init_I++;
                        this.init_q_1_DataUtil = Inner_DataUtil.update;
                        return this.init("Bed armed; backing out of the blast");
                     }
                  }
               } else {
                  if (this.init_AiOperation != null && !this.sync_Z) {
                     this.load_V_15();
                  }

                  this.write_BlockPos = null;
                  this.close_BlockPos = null;
                  return this.init("Waiting for the dragon to sit");
               }
            }

            AiOperation.Result poll() {
               if (this.read_BlockPos == null) {
                  this.sync_BlockPos = null;
                  BlockState bedState = mc.level.getBlockState(this.load_BlockPos);
                  if (bedState.hasProperty(BedBlock.FACING)) {
                     Direction back = bedState.getValue(BedBlock.FACING).getOpposite();
                     Direction side = back.getClockWise();
                     r.Inner_DataUtil zones = r.init_r_DataUtil(mc);
                     double[] breath = r.init_DArr(mc);
                     List<double[]> lying = this.init_List_11();
                     if (this.open_BlockPos != null
                        && r.init(zones, breath, lying, this.open_BlockPos.getX(), this.open_BlockPos.getY(), this.open_BlockPos.getZ())) {
                        this.read_BlockPos = this.open_BlockPos;
                        int dx = this.load_BlockPos.getX() - this.open_BlockPos.getX();
                        int dz = this.load_BlockPos.getZ() - this.open_BlockPos.getZ();
                        this.sync_BlockPos = Math.abs(dx) >= Math.abs(dz)
                           ? new BlockPos(this.open_BlockPos.getX() + Integer.signum(dx), this.open_BlockPos.getY(), this.open_BlockPos.getZ())
                           : new BlockPos(this.open_BlockPos.getX(), this.open_BlockPos.getY(), this.open_BlockPos.getZ() + Integer.signum(dz));
                     }

                     for (int[] o : this.read_BlockPos != null ? new int[0][] : q.init_IArr2) {
                        int x = this.load_BlockPos.getX() + back.getStepX() * o[0] + side.getStepX() * o[1];
                        int z = this.load_BlockPos.getZ() + back.getStepZ() * o[0] + side.getStepZ() * o[1];
                        BlockPos support = this.init(x, z);
                        if (support != null
                           && Math.abs(support.getY() + 1 - this.load_BlockPos.getY()) <= 1
                           && r.init(zones, breath, lying, x, support.getY() + 1, z)) {
                           this.read_BlockPos = support.above();
                           int dx = this.load_BlockPos.getX() - x;
                           int dz = this.load_BlockPos.getZ() - z;
                           this.sync_BlockPos = Math.abs(dx) >= Math.abs(dz)
                              ? new BlockPos(x + Integer.signum(dx), this.read_BlockPos.getY(), z)
                              : new BlockPos(x, this.read_BlockPos.getY(), z + Integer.signum(dz));
                           break;
                        }
                     }
                  }
               }

               if (this.read_BlockPos == null && this.open_BlockPos != null) {
                  this.read_BlockPos = this.open_BlockPos;
                  this.sync_BlockPos = null;
               }

               if (this.read_BlockPos == null) {
                  int[] xz = q.load(
                     this.load_BlockPos.getX(),
                     this.load_BlockPos.getZ(),
                     mc.player.getX(),
                     mc.player.getZ(),
                     Math.max(3.5, mc.player.blockInteractionRange() - 0.4)
                  );
                  BlockPos support = this.init(xz[0], xz[1]);
                  if (support == null) {
                     this.load_BlockPos = null;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("No stand-off ground; abandoning the armed bed");
                  }

                  this.read_BlockPos = support.above();
               }

               if (this.init_AiOperation == null) {
                  this.init_AiOperation = HashUtil.init(
                     this.read_BlockPos, 0.8, false, true, true, this.init(30), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                  );
               }

               AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.init("Backing out of the blast radius");
               }

               this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
               this.init_AiOperation = null;
               this.read_BlockPos = null;
               if (i.init_i_DataUtil_Minecraft(mc).lethal()) {
                  this.load_BlockPos = null;
                  this.poll_Z = false;
                  cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "The blast at click range would be lethal; leaving beds out of the plan");
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("The blast at click range would be lethal; leaving beds out of the plan");
               }

               if (this.sync_BlockPos == null && this.load_BlockPos != null) {
                  BlockPos feet = mc.player.blockPosition();
                  int dx = this.load_BlockPos.getX() - feet.getX();
                  int dz = this.load_BlockPos.getZ() - feet.getZ();
                  BlockPos c = Math.abs(dx) >= Math.abs(dz) ? feet.offset(Integer.signum(dx), 0, 0) : feet.offset(0, 0, Integer.signum(dz));
                  if (!c.equals(this.load_BlockPos)
                     && mc.level.getBlockState(c).canBeReplaced()
                     && !mc.level.getBlockState(c.below()).getCollisionShape(mc.level, c.below()).isEmpty()) {
                     this.sync_BlockPos = c;
                  }
               }

               if (this.sync_BlockPos != null) {
                  this.init_q_1_DataUtil = Inner_DataUtil.sync;
                  return this.init("At stand-off; setting the cover block");
               } else {
                  this.init_q_1_DataUtil = Inner_DataUtil.flush;
                  return this.init("At stand-off; waiting for the head");
               }
            }

            AiOperation.Result push() {
               if (this.init_AiOperation == null) {
                  BlockPos support = this.sync_BlockPos.below();
                  boolean open = mc.level.getBlockState(this.sync_BlockPos).canBeReplaced()
                     && !mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty();
                  String block = this.init_Str_20();
                  if (!open || block == null || af.read(af.load_af_DataUtil(block)) < 0) {
                     if (!mc.level.getBlockState(this.sync_BlockPos).getCollisionShape(mc.level, this.sync_BlockPos).isEmpty()) {
                        this.pop_BlockPos = this.sync_BlockPos.immutable();
                     }

                     this.sync_BlockPos = null;
                     this.init_q_1_DataUtil = Inner_DataUtil.flush;
                     return this.init(open ? "No block to set as cover; waiting for the head" : "Cover cell taken; waiting for the head");
                  }

                  this.init_AiOperation = R.init(
                     "place", ag.init_JsonObject_ObjArr("x", support.getX(), "y", support.getY(), "z", support.getZ(), "face", "UP", "timeoutSeconds", 6)
                  );
               }

               AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.init("Setting a block between the bed and the body");
               }

               this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
               this.init_AiOperation = null;
               cl.init_V_Str_ObjArr("[AI] dragon: cover block in {}: {} ({})", this.sync_BlockPos.toShortString(), r.status(), cl.init_Str_Str(r.message()));
               if (r.status() != AiOperation.Status.SUCCEEDED && ++this.reset < 2 && mc.level.getBlockState(this.sync_BlockPos).canBeReplaced()) {
                  return this.init("Cover not confirmed; setting it again");
               }

               this.reset = 0;
               if (!mc.level.getBlockState(this.sync_BlockPos).getCollisionShape(mc.level, this.sync_BlockPos).isEmpty()) {
                  this.pop_BlockPos = this.sync_BlockPos.immutable();
               }

               this.sync_BlockPos = null;
               this.init_q_1_DataUtil = Inner_DataUtil.flush;
               return this.init(
                  r.status() == AiOperation.Status.SUCCEEDED ? "Cover set; waiting for the head" : "No cover (" + r.message() + "); waiting for the head"
               );
            }

            String init_Str_20() {
               String best = null;
               int bestRank = 2147483647;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty()
                     && bK.init(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), 0) > autismclient.ai.a.UtilCore.init(stack.getItem())) {
                     String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                     int rank = autismclient.ai.a.UtilCore.init_I(id);
                     if (id.equals("minecraft:end_stone") || id.equals("minecraft:obsidian")) {
                        rank = 0;
                     }

                     if (rank >= 0 && rank < bestRank) {
                        bestRank = rank;
                        best = id;
                     }
                  }
               }

               return best;
            }

            AiOperation.Result pop() {
               if (!this.load(this.load_BlockPos)) {
                  this.load_BlockPos = null;
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("The armed bed was destroyed; re-planning");
               }

               if (this.write_Z_9()) {
                  this.init_q_1_DataUtil = Inner_DataUtil.close;
                  return this.init("The head is in range of the bed; detonating");
               }

               double reach = mc.player.blockInteractionRange();
               Vec3 eye = mc.player.getEyePosition();
               double distance = eye.distanceTo(
                  new Vec3(
                     Math.clamp(eye.x, this.load_BlockPos.getX(), this.load_BlockPos.getX() + 1.0),
                     Math.clamp(eye.y, this.load_BlockPos.getY(), this.load_BlockPos.getY() + 0.5625),
                     Math.clamp(eye.z, this.load_BlockPos.getZ(), this.load_BlockPos.getZ() + 1.0)
                  )
               );
               if (!i.init_i_DataUtil_Minecraft(mc).lethal() && q.init_Z(mc.player.getX(), mc.player.getZ(), this.init_List_11()) && !(distance > reach - 0.3)) {
                  return this.init("Bed armed; waiting for the head (" + this.init_Str + ")");
               }

               if (this.init_AiOperation == null) {
                  double[] spot = q.init_DArr(mc.player.getX(), mc.player.getZ(), this.init_List_11());
                  int[] xz = q.load(this.load_BlockPos.getX(), this.load_BlockPos.getZ(), spot[0], spot[1], Math.max(3.5, reach - 0.4));
                  BlockPos support = this.init(xz[0], xz[1]);
                  if (support == null) {
                     return this.init("Holding for the head");
                  }

                  this.init_AiOperation = HashUtil.init(
                     support.above(), 0.8, false, true, true, this.init(20), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                  );
               }

               AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
               } else if (!r.terminal()) {
                  return this.init("Holding at stand-off for the head");
               } else {
                  this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                  this.init_AiOperation = null;
                  if (i.init_i_DataUtil_Minecraft(mc).lethal()) {
                     this.load_BlockPos = null;
                     this.poll_Z = false;
                     cl.init_V_Str_ObjArr("[AI] dragon: beds out of the plan: {}", "Stand-off is no longer survivable; leaving beds out of the plan");
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Stand-off is no longer survivable; leaving beds out of the plan");
                  } else if (r.status() != AiOperation.Status.SUCCEEDED && ++this.poll_I >= 3) {
                     this.poll_I = 0;
                     this.load_BlockPos = null;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Cannot hold the stand-off; leaving that bed");
                  } else {
                     this.poll_I = 0;
                     return this.init("Repositioned at stand-off");
                  }
               }
            }

            AiOperation.Result run() {
               if (this.load_BlockPos == null) {
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("No armed bed; re-planning");
               }

               if (this.flush_Z) {
                  this.start++;
                  boolean damaged = this.init_EnderDragon != null && this.init_EnderDragon.getHealth() < this.load_F;
                  boolean acknowledged = damaged
                     || this.poll_BlockPos != null
                        && this.push_BlockPos != null
                        && ca.init_J_BlockPos(this.poll_BlockPos) > this.write_J
                        && ca.init_J_BlockPos(this.push_BlockPos) > this.update_J
                        && !this.load(this.poll_BlockPos)
                        && !this.load(this.push_BlockPos);
                  if (this.start >= 12 && acknowledged) {
                     this.load_D = this.init_EnderDragon == null ? 0.0 : Math.max(0.0F, this.load_F - this.init_EnderDragon.getHealth());
                     if (this.load_D < 1.0 && ++this.stop >= 2) {
                        this.poll_Z = false;
                        this.write_Z = true;
                     } else if (this.load_D >= 1.0) {
                        this.stop = 0;
                     }

                     this.load_BlockPos = null;
                     this.flush_Z = false;
                     this.close_I = 0;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init(
                        "Bed removed; observed dragon damage "
                           + String.format(Locale.ROOT, "%.1f", this.load_D)
                           + (this.poll_Z ? "" : "; ineffective bed plan retired for crystals and melee")
                     );
                  } else {
                     return this.start >= 60
                        ? this.init(AiOperation.Status.UNCERTAIN, null, "Bed use lacks confirmed server removal of both halves; no repeat click authorized")
                        : this.init("Observing server acknowledgement of both bed halves and dragon health");
                  }
               } else {
                  if (!this.load(this.load_BlockPos)) {
                     this.load_BlockPos = null;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Bed disappeared before use; no detonation credited");
                  }

                  BlockPos head = this.init_BlockPos_19();
                  if (head != null && this.write_Z_9() && this.init_EnderDragon.hurtTime <= 0) {
                     Vec3 blast = Vec3.atCenterOf(head);
                     BlockState state = mc.level.getBlockState(head);
                     BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
                     autismclient.util.ExplosionDamageCalculator.Inner_CachePool options = autismclient.util.ExplosionDamageCalculator.Inner_CachePool.init_S_CachePool
                        .init(List.of(head, foot))
                        .init(autismclient.util.ExplosionDamageCalculator.load(mc.level, blast));
                     this.init_D = autismclient.util.ExplosionDamageCalculator.init_D(mc.player, blast, 5.0F, options);
                     double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                     boolean covered = this.pop_BlockPos != null
                        && mc.level
                           .getBlockState(this.pop_BlockPos)
                           .getCollisionShape(mc.level, this.pop_BlockPos)
                           .toAabbs()
                           .stream()
                           .anyMatch(box -> box.move(this.pop_BlockPos).clip(blast, mc.player.position().add(0.0, 0.4, 0.0)).isPresent());
                     double expectedSelfDamage = Math.max(this.init_D, 8.0);
                     boolean safeBlast = (covered || this.init_D > 0.0)
                        && health - expectedSelfDamage >= 11.5
                        && !i.init_i_DataUtil_Minecraft(mc).lethal()
                        && !q.init_Z(mc)
                        && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse();
                     if (safeBlast) {
                        this.init_DataUtil.init_V_Vec3(Vec3.atCenterOf(this.load_BlockPos));
                        this.init_DataUtil.load_V();
                        BlockHitResult ray = autismclient.ai.a.DataUtil.init_BlockHitResult();
                        if (ray == null || !ray.getBlockPos().equals(head) && !ray.getBlockPos().equals(foot)) {
                           if (++this.start > 40) {
                              this.init_q_1_DataUtil = Inner_DataUtil.flush;
                              this.start = 0;
                           }

                           return this.init("Waiting for an unobstructed bed interaction ray");
                        } else {
                           this.load_F = this.init_EnderDragon.getHealth();
                           this.poll_BlockPos = head.immutable();
                           this.push_BlockPos = foot.immutable();
                           this.write_J = ca.init_J_BlockPos(this.poll_BlockPos);
                           this.update_J = ca.init_J_BlockPos(this.push_BlockPos);
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, ray);
                           this.flush_Z = true;
                           this.start = 0;
                           return this.init("Bed clicked; waiting for observed removal and dragon damage");
                        }
                     } else if (!q.init_Z(mc) && !i.init_i_DataUtil_Minecraft(mc).lethal()) {
                        if (this.init_D > 0.0 && expectedSelfDamage + 11.5 > mc.player.getMaxHealth() + mc.player.getAbsorptionAmount()) {
                           if (++this.prev <= 3) {
                              this.pop_BlockPos = null;
                              this.read_BlockPos = null;
                              this.init_q_1_DataUtil = Inner_DataUtil.flush;
                              return this.init("Blast priced too strong from here; re-planning the stand and cover");
                           } else {
                              this.prev = 0;
                              this.load_BlockPos = null;
                              this.pop_BlockPos = null;
                              this.init_q_1_DataUtil = Inner_DataUtil.init;
                              return this.init("Blast too strong from every stand tried; dropping this bed plan");
                           }
                        } else if (this.init_D > 0.0 && health - expectedSelfDamage < 11.5) {
                           return this.init(
                              A.start,
                              "Health " + String.format(Locale.ROOT, "%.1f", health) + " leaves too little after a bed blast; healing before the next perch"
                           );
                        } else if (!covered && this.init_D <= 0.0) {
                           this.init_q_1_DataUtil = Inner_DataUtil.flush;
                           return this.init("Bed armed; waiting for cover or a priced blast");
                        } else if (!mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse()) {
                           this.poll_Z = false;
                           this.write_Z = true;
                           this.load_BlockPos = null;
                           this.pop_BlockPos = null;
                           this.init_q_1_DataUtil = Inner_DataUtil.init;
                           return this.init("Beds do not explode in this dimension; use the melee/bow fallback");
                        } else {
                           this.poll_Z = false;
                           this.write_Z = true;
                           this.load_BlockPos = null;
                           this.pop_BlockPos = null;
                           this.init_q_1_DataUtil = Inner_DataUtil.init;
                           return this.init("Bed survival/cover gate failed; use the crystal and melee fallback");
                        }
                     } else {
                        this.init_q_1_DataUtil = Inner_DataUtil.flush;
                        return this.init("Bed armed; waiting out the breath/dragon attack before clicking");
                     }
                  } else {
                     this.init_q_1_DataUtil = Inner_DataUtil.flush;
                     return this.init("Head/phase/damage window changed; hold the bed");
                  }
               }
            }

            BlockPos init_BlockPos_19() {
               if (!this.load(this.load_BlockPos)) {
                  return null;
               } else {
                  BlockState state = mc.level.getBlockState(this.load_BlockPos);
                  BlockPos head = state.getValue(BedBlock.PART) == BedPart.HEAD
                     ? this.load_BlockPos
                     : this.load_BlockPos.relative(state.getValue(BedBlock.FACING));
                  BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
                  if (cu.init(mc.level, head) && cu.init(mc.level, foot)) {
                     BlockState headState = mc.level.getBlockState(head);
                     BlockState footState = mc.level.getBlockState(foot);
                     return headState.getBlock() == state.getBlock()
                           && footState.getBlock() == state.getBlock()
                           && headState.getValue(BedBlock.PART) == BedPart.HEAD
                           && footState.getValue(BedBlock.PART) == BedPart.FOOT
                           && headState.getValue(BedBlock.FACING) == footState.getValue(BedBlock.FACING)
                        ? head
                        : null;
                  } else {
                     return null;
                  }
               }
            }

            AiOperation.Result get() {
               if (this.init_q_CachePool != q.Inner_CachePool.init) {
                  this.load_V_15();
                  this.flush_BlockPos = null;
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("The perch ended; re-planning");
               }

               if (++this.open_I > 300) {
                  this.load_V_15();
                  this.flush_BlockPos = null;
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("Long perch; re-evaluating");
               }

               EnderDragonPhase<?> dragonPhase = this.init_EnderDragon.getPhaseManager().getCurrentPhase().getPhase();
               List<double[]> clouds = this.init_List_11();
               r.Inner_DataUtil zones = r.init_r_DataUtil(mc);
               boolean window = dragonPhase == EnderDragonPhase.SITTING_SCANNING || dragonPhase == EnderDragonPhase.SITTING_ATTACKING;
               if (!window) {
                  this.flush_BlockPos = null;
                  if (this.get == 1) {
                     this.load_V_15();
                  }

                  List<double[]> danger = new ArrayList<>(clouds);
                  double[] breath = r.init_DArr(mc);
                  if (breath != null) {
                     danger.add(breath);
                  }

                  if (this.init_AiOperation == null && !q.init_Z_D(mc.player.getX(), mc.player.getZ(), danger, 1.0)) {
                     BlockPos out = this.init(danger, zones);
                     if (out != null) {
                        this.init_AiOperation = X.init(
                           HashUtil.init(out, 0.35, false, false, false, this.init(8), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true)
                        );
                        this.get = 2;
                     }
                  }

                  if (this.init_AiOperation == null && this.get == 0) {
                     BlockPos near = this.sync_BlockPos();
                     if (near != null && Math.hypot(mc.player.getX() - (near.getX() + 0.5), mc.player.getZ() - (near.getZ() + 0.5)) > 2.5) {
                        this.init_AiOperation = HashUtil.init(
                           near, 0.8, false, false, false, this.init(10), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                        );
                        this.get = 3;
                     }
                  }

                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.init(this.get == 3 ? "Closing in while it lands" : "Out of the dragon's flame");
                     }

                     this.load_V_15();
                     this.get = 0;
                  }

                  return this.init("Waiting for the flame to end (" + dragonPhase + ")");
               } else {
                  if (this.get == 2 || this.get == 3) {
                     this.load_V_15();
                     this.get = 0;
                  }

                  if (this.flush_BlockPos == null || this.open_I % 10 == 0) {
                     BlockPos stand = this.init(zones, clouds);
                     if (stand == null || !stand.equals(this.flush_BlockPos)) {
                        this.load_V_15();
                        this.get = 0;
                     }

                     this.flush_BlockPos = stand;
                  }

                  if (this.flush_BlockPos == null) {
                     return this.init("No stand reaches the head clear of the wings");
                  }

                  this.init_DataUtil.init_V_Vec3(autismclient.ai.a.DataUtil.init_Vec3_Entity(this.init_EnderDragon.head));
                  this.init_V_32();
                  double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.1;
                  boolean inReach = this.init_EnderDragon.head.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reach * reach;
                  boolean there = Math.hypot(mc.player.getX() - (this.flush_BlockPos.getX() + 0.5), mc.player.getZ() - (this.flush_BlockPos.getZ() + 0.5))
                        < 0.5
                     && Math.abs(mc.player.getY() - this.flush_BlockPos.getY()) < 0.6;
                  if (!there && (!inReach || !mc.player.onGround())) {
                     if (this.init_AiOperation == null) {
                        this.init_AiOperation = HashUtil.init(
                           this.flush_BlockPos, 0.3, false, false, false, this.init(10), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                        );
                        this.get = 1;
                     }

                     AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.init("Closing on the head");
                     }

                     this.load_V_15();
                     this.get = 0;
                  }

                  this.load_V_15();
                  this.get = 0;
                  this.init_DataUtil.load_V();
                  if (inReach
                     && this.init_DataUtil.load_Z_Entity(this.init_EnderDragon.head)
                     && mc.player.getAttackStrengthScale(0.0F) >= 0.92F
                     && k.init(mc, this.init_EnderDragon.head, this.init_DataUtil)) {
                     mc.gameMode.attack(mc.player, this.init_EnderDragon.head);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     mc.player.resetAttackStrengthTicker();
                     this.read_I++;
                     cl.init_V_Str_ObjArr(
                        "[AI] dragon: struck the head ({}); dragon health {}",
                        this.read_I,
                        String.format(Locale.ROOT, "%.0f", this.init_EnderDragon.getHealth())
                     );
                     return this.init("Struck the head (" + this.read_I + ")");
                  } else {
                     return this.init(inReach ? "Waiting out the attack cooldown" : "Waiting for the head to come into reach");
                  }
               }
            }

            BlockPos init(r.Inner_DataUtil zones, List<double[]> clouds) {
               AABB headBox = this.init_EnderDragon.head.getBoundingBox();
               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.4;
               BlockPos h = this.init_EnderDragon.head.blockPosition();
               BlockPos best = null;
               double bestScore = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(h.offset(-4, -4, -4), h.offset(4, 1, 4))) {
                  BlockPos below = p.below();
                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                     && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                     && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                     && !zones.hits(r.init_AABB_I(p.getX(), p.getY(), p.getZ()), 0.8)
                     && q.init_Z_D(p.getX() + 0.5, p.getZ() + 0.5, clouds, 0.5)) {
                     Vec3 eye = new Vec3(p.getX() + 0.5, p.getY() + mc.player.getEyeHeight(), p.getZ() + 0.5);
                     if (!(headBox.distanceToSqr(eye) > reach * reach) && ValueSink.init_Z_Minecraft_BlockPos(mc, p)) {
                        BlockHitResult ray = mc.level.clip(new ClipContext(eye, headBox.getCenter(), Block.COLLIDER, Fluid.NONE, mc.player));
                        if (ray.getType() != Type.BLOCK) {
                           double score = mc.player.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                           if (score < bestScore) {
                              bestScore = score;
                              best = p.immutable();
                           }
                        }
                     }
                  }
               }

               return best;
            }

            BlockPos init(List<double[]> danger, r.Inner_DataUtil zones) {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(feet.offset(-8, -2, -8), feet.offset(8, 2, 8))) {
                  if (q.init_Z_D(p.getX() + 0.5, p.getZ() + 0.5, danger, 1.5) && !zones.hits(r.init_AABB_I(p.getX(), p.getY(), p.getZ()), 1.0)) {
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

               return best;
            }

            AiOperation.Result set() {
               if (this.init_q_CachePool == q.Inner_CachePool.init) {
                  this.load_V_15();
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("The dragon perched; switching plans");
               }

               if (this.init_AiOperation == null) {
                  if (!this.sync_Z_6()) {
                     this.push_Z = false;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("Out of arrows");
                  }

                  this.init_AiOperation = bC.init(
                     ag.init_JsonObject_ObjArr("entityId", this.init_EnderDragon.getId(), "count", 3, "timeoutSeconds", this.init(45))
                  );
               }

               AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.init("Shooting the circling dragon");
               }

               this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
               this.init_AiOperation = null;
               this.load_I = this.load_I + this.init_I(r);
               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.init(AiOperation.Status.CANCELLED, A.size, "Dragon fight cancelled");
               }

               if (r.status() == AiOperation.Status.FAILED) {
                  if (r.dataJson() != null && r.dataJson().contains("\"missing\"")) {
                     this.init_V(r);
                     this.push_Z = false;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("The bow is out of ammunition");
                  }

                  if (++this.flush_I >= 3) {
                     this.push_Z = false;
                     this.init_q_1_DataUtil = Inner_DataUtil.init;
                     return this.init("The bow cannot connect from here; holding for perches");
                  }
               }

               this.init_q_1_DataUtil = Inner_DataUtil.init;
               return this.init("Re-evaluating");
            }

            AiOperation.Result put() {
               if (this.init_q_CachePool != q.Inner_CachePool.init && this.init_q_CachePool != q.Inner_CachePool.load && this.load_BlockPos == null) {
                  if (this.init_AiOperation == null) {
                     int fx = this.init_BlockPos != null ? this.init_BlockPos.getX() : 0;
                     int fz = this.init_BlockPos != null ? this.init_BlockPos.getZ() : 0;
                     int[] xz = q.init(fx, fz, mc.player.getX(), mc.player.getZ(), 12.0);
                     double[] spot = q.init_DArr(xz[0] + 0.5, xz[1] + 0.5, this.init_List_11());
                     BlockPos support = this.init((int)Math.floor(spot[0]), (int)Math.floor(spot[1]));
                     if (support == null) {
                        return this.init("Holding position");
                     }

                     if (mc.player.distanceToSqr(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5) <= 4.0) {
                        return this.init("Holding by the fountain for the next perch (" + this.init_Str + ")");
                     }

                     this.init_AiOperation = HashUtil.init(
                        support.above(), 1.5, false, true, true, this.init(30), autismclient.ai.a.ClassCache.Inner_IoWorker.init, true
                     );
                  }

                  AiOperation.Result r = this.init_q_StreamUtil.init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.init("Repositioning toward the fountain");
                  }

                  this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                  this.init_AiOperation = null;
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("Re-evaluating");
               } else {
                  this.load_V_15();
                  this.init_q_1_DataUtil = Inner_DataUtil.init;
                  return this.init("Perch incoming; re-planning");
               }
            }

            EnderDragon init_EnderDragon() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EnderDragon dragon && !entity.isRemoved()) {
                     return dragon;
                  }
               }

               return null;
            }

            boolean init_Z_27() {
               if (!cv.init_Z() && !this.load_Z_16()) {
                  return false;
               }

               if (!cv.init_Z() && q.init_Z_I_I(this.set, this.load_I_15())) {
                  return this.load_Z_16();
               }

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved()) {
                     return true;
                  }
               }

               return false;
            }

            boolean load_Z_16() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved() && !h.init(mc, entity.blockPosition())) {
                     return true;
                  }
               }

               return false;
            }

            List<double[]> init_List_11() {
               List<double[]> out = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof AreaEffectCloud cloud && !entity.isRemoved()) {
                     out.add(new double[]{cloud.getX(), cloud.getZ(), cloud.getRadius()});
                  }
               }

               return out;
            }

            String init_Str(EnderDragon dragon) {
               try {
                  EnderDragonPhase synced = dragon.getPhaseManager().getCurrentPhase().getPhase();
                  if (synced == EnderDragonPhase.LANDING) {
                     return "Landing";
                  } else if (synced == EnderDragonPhase.LANDING_APPROACH) {
                     return "LandingApproach";
                  } else if (synced == EnderDragonPhase.SITTING_SCANNING) {
                     return "SittingScanning";
                  } else if (synced == EnderDragonPhase.SITTING_FLAMING) {
                     return "SittingFlaming";
                  } else if (synced == EnderDragonPhase.SITTING_ATTACKING) {
                     return "SittingAttacking";
                  } else if (synced == EnderDragonPhase.DYING) {
                     return "Dying";
                  } else if (synced == EnderDragonPhase.TAKEOFF) {
                     return "Takeoff";
                  } else if (synced == EnderDragonPhase.STRAFE_PLAYER) {
                     return "StrafePlayer";
                  } else if (synced == EnderDragonPhase.CHARGING_PLAYER) {
                     return "ChargingPlayer";
                  } else {
                     return synced == EnderDragonPhase.HOVERING ? "Hover" : "HoldingPattern";
                  }
               } catch (RuntimeException e) {
                  return "";
               }
            }

            q.Inner_CachePool init(EnderDragon dragon, String phaseName) {
               if (!phaseName.isEmpty()) {
                  return q.init_q_CachePool(phaseName);
               }

               double fx = this.init_BlockPos != null ? this.init_BlockPos.getX() + 0.5 : 0.5;
               double fz = this.init_BlockPos != null ? this.init_BlockPos.getZ() + 0.5 : 0.5;
               double fy = this.init_BlockPos != null ? this.init_BlockPos.getY() : 64.0;
               double horizontal = Math.hypot(dragon.getX() - fx, dragon.getZ() - fz);
               return q.init(horizontal, dragon.getY() - fy, dragon.getDeltaMovement().length() * 20.0);
            }

            void init(q.Inner_CachePool perch) {
               if (perch == q.Inner_CachePool.init && this.load_q_CachePool != q.Inner_CachePool.init) {
                  this.write_I++;
                  if (this.read_Z) {
                     this.read_Z = false;
                     this.poll_Z = true;
                     this.push_I = 0;
                     this.init_Set.clear();
                  }

                  if (this.init_EnderDragon != null
                     && this.init_F > 0.0F
                     && this.init_EnderDragon.getHealth() > this.init_F + 10.0F
                     && !this.write_Z
                     && this.init_Z_27()) {
                     this.write_Z = true;
                     cl.init_V_Str_ObjArr(
                        "[AI] dragon: healed from {} to {} between perches; crystals first while it flies",
                        String.format(Locale.ROOT, "%.0f", this.init_F),
                        String.format(Locale.ROOT, "%.0f", this.init_EnderDragon.getHealth())
                     );
                  }
               }

               if (perch != q.Inner_CachePool.init && this.load_q_CachePool == q.Inner_CachePool.init && this.init_EnderDragon != null) {
                  this.init_F = this.init_EnderDragon.getHealth();
               }

               this.load_q_CachePool = perch;
            }

            BlockPos load_BlockPos_6() {
               int bestX = 0;
               int bestZ = 0;
               int bestTop = -2147483648;
               int bestRun = 0;

               for (int x = -8; x <= 8; x++) {
                  for (int z = -8; z <= 8; z++) {
                     int run = 0;
                     int top = -2147483648;
                     int columnBest = 0;

                     for (int y = 110; y >= 40; y--) {
                        if ("bedrock".equals(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(new BlockPos(x, y, z)).getBlock()).getPath())) {
                           run++;
                           top = Math.max(top, y);
                           columnBest = Math.max(columnBest, run);
                        } else {
                           run = 0;
                        }
                     }

                     if (columnBest > bestRun || columnBest == bestRun && columnBest > 0 && top > bestTop) {
                        bestRun = columnBest;
                        bestTop = top;
                        bestX = x;
                        bestZ = z;
                     }
                  }
               }

               return bestRun >= 2 ? new BlockPos(bestX, bestTop, bestZ) : null;
            }

            boolean read_Z_12() {
               if (this.init_BlockPos == null) {
                  return false;
               }

               for (int dx = -3; dx <= 3; dx++) {
                  for (int dz = -3; dz <= 3; dz++) {
                     for (int y = this.init_BlockPos.getY() - 4; y <= this.init_BlockPos.getY() + 1; y++) {
                        BlockPos p = new BlockPos(this.init_BlockPos.getX() + dx, y, this.init_BlockPos.getZ() + dz);
                        if ("end_portal".equals(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock()).getPath())) {
                           return true;
                        }
                     }
                  }
               }

               return false;
            }

            BlockPos read_BlockPos_5() {
               int fx = this.init_BlockPos.getX();
               int fz = this.init_BlockPos.getZ();
               double cx = fx + 0.5;
               double cz = fz + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               double ux = len < 1.0E-6 ? 1.0 : dx / len;
               double uz = len < 1.0E-6 ? 0.0 : dz / len;
               List<double[]> dirs = new ArrayList<>();
               Vec3 facing = r.init_Vec3(mc);
               if (facing != null && facing.x * ux + facing.z * uz > 0.35) {
                  dirs.add(new double[]{facing.x, facing.z});
               }

               dirs.add(new double[]{ux, uz});
               r.Inner_DataUtil zones = r.init_r_DataUtil(mc);
               BlockPos fallback = null;
               List<double[]> clouds = this.init_List_11();

               for (double[] dir : dirs) {
                  for (double radius = 3.0; radius <= 7.0; radius++) {
                     BlockPos support = this.init((int)Math.round(fx + dir[0] * radius), (int)Math.round(fz + dir[1] * radius));
                     if (support != null && !this.init_Set.contains(support.asLong())) {
                        this.close_BlockPos = null;
                        if (zones.isEmpty() || this.init(cx, cz, dir, radius, support.getY() + 1, zones)) {
                           if (q.init_Z(support.getX() + 0.5, support.getZ() + 0.5, clouds)) {
                              this.update_BlockPos = support.above();
                              cl.init_V_Str_ObjArr(
                                 "[AI] dragon: bed cell {} at {} blocks from the fountain{}",
                                 support.above().toShortString(),
                                 radius,
                                 facing != null && dir[0] == facing.x && dir[1] == facing.z ? ", under the head" : ""
                              );
                              return support;
                           }

                           if (fallback == null) {
                              fallback = support;
                           }
                           break;
                        }
                     }
                  }
               }

               if (fallback != null) {
                  this.update_BlockPos = fallback.above();
                  return fallback;
               } else {
                  return null;
               }
            }

            BlockPos write_BlockPos_2() {
               Entity head = r.init_Entity(mc);
               if (head == null) {
                  return null;
               }

               r.Inner_DataUtil zones = r.init_r_DataUtil(mc);
               double[] breath = r.init_DArr(mc);
               List<double[]> clouds = this.init_List_11();
               Vec3 h = head.getBoundingBox().getCenter();
               BlockPos me = mc.player.blockPosition();
               int hx = Mth.floor(h.x);
               int hz = Mth.floor(h.z);
               double best = 1.7976931348623157E308;
               BlockPos bestStance = null;
               BlockPos bestFoot = null;
               double bestBlast = 0.0;
               int safeStances = 0;
               int farBeds = 0;
               int closeBlasts = 0;
               int weakBlasts = 0;

               for (int sx = hx - 11; sx <= hx + 11; sx++) {
                  for (int sz = hz - 11; sz <= hz + 11; sz++) {
                     double d = Math.hypot(sx + 0.5 - h.x, sz + 0.5 - h.z);
                     if (!(d < 3.0) && !(d > 11.0)) {
                        BlockPos ground = this.init(sx, sz);
                        if (ground != null) {
                           BlockPos stance = ground.above();
                           if (r.init(zones, breath, clouds, sx, stance.getY(), sz) && ValueSink.init_Z_Minecraft_BlockPos(mc, stance)) {
                              safeStances++;
                              double walk = Math.hypot(sx - me.getX(), sz - me.getZ());

                              for (int bx = sx - 5; bx <= sx + 5; bx++) {
                                 for (int bz = sz - 5; bz <= sz + 5; bz++) {
                                    double r = Math.hypot(bx - sx, bz - sz);
                                    if (!(r < 2.5) && !(r > 5.0)) {
                                       BlockPos support = this.init(bx, bz);
                                       if (support != null && !this.init_Set.contains(support.asLong())) {
                                          BlockPos foot = support.above();
                                          if (Math.abs(foot.getY() - stance.getY()) <= 1 && mc.level.getBlockState(foot).canBeReplaced()) {
                                             BlockPos bedHead = foot.relative(Direction.getApproximateNearest(bx - sx, 0.0F, bz - sz));
                                             if (mc.level.getBlockState(bedHead).canBeReplaced() && mc.level.getFluidState(bedHead).isEmpty()) {
                                                Vec3 eye = new Vec3(sx + 0.5, stance.getY() + 1.62, sz + 0.5);
                                                if (eye.distanceTo(new Vec3(bx + 0.5, support.getY() + 1.0, bz + 0.5)) > 3.8
                                                   || eye.distanceTo(new Vec3(bx + 0.5, foot.getY() + 0.5, bz + 0.5)) > 3.8) {
                                                   farBeds++;
                                                } else if (new Vec3(sx + 0.5, stance.getY() + 0.9, sz + 0.5).distanceTo(Vec3.atCenterOf(bedHead)) < 3.8) {
                                                   closeBlasts++;
                                                } else {
                                                   double blast = Vec3.atCenterOf(bedHead).distanceTo(h);
                                                   if (blast > 5.0) {
                                                      weakBlasts++;
                                                   } else {
                                                      double score = blast * 3.0 + walk * 0.3;
                                                      if (score < best) {
                                                         best = score;
                                                         bestStance = stance;
                                                         bestFoot = foot;
                                                         bestBlast = blast;
                                                      }
                                                   }
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               if (bestFoot == null) {
                  if (mc.player.tickCount - this.clear > 100) {
                     this.clear = mc.player.tickCount;
                     cl.init_V_Str_ObjArr(
                        "[AI] dragon: no bed plan: {} safe stances, beds out of reach {}, blasts too close {}, too far from the head {}",
                        safeStances,
                        farBeds,
                        closeBlasts,
                        weakBlasts
                     );
                  }

                  return null;
               } else {
                  this.close_BlockPos = bestStance;
                  this.open_BlockPos = bestStance;
                  this.update_BlockPos = bestFoot;
                  cl.init_V_Str_ObjArr(
                     "[AI] dragon: bed plan: stance {}, bed {}, blast {} from the head",
                     bestStance.toShortString(),
                     bestFoot.toShortString(),
                     String.format(Locale.ROOT, "%.1f", bestBlast)
                  );
                  return bestFoot.below();
               }
            }

            boolean init(double cx, double cz, double[] dir, double radius, int feetY, r.Inner_DataUtil zones) {
               for (double k = 2.0; k <= 4.0; k += 0.25) {
                  double x = cx + dir[0] * (radius + k);
                  double z = cz + dir[1] * (radius + k);
                  if (!zones.hits(r.init_AABB_D(x, feetY, z), 1.0)) {
                     BlockPos ground = this.init(Mth.floor(x), Mth.floor(z));
                     if (ground != null
                        && Math.abs(ground.getY() + 1 - feetY) <= 1
                        && !zones.hits(r.init_AABB_I(ground.getX(), ground.getY() + 1, ground.getZ()), 1.0)) {
                        this.close_BlockPos = ground.above();
                        return true;
                     }
                  }
               }

               return false;
            }

            BlockPos update_BlockPos() {
               if (this.init_BlockPos == null) {
                  return null;
               }

               for (BlockPos p : BlockPos.betweenClosed(this.init_BlockPos.offset(-8, -4, -8), this.init_BlockPos.offset(8, 2, 8))) {
                  BlockState state = mc.level.getBlockState(p);
                  if (state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                     return p.immutable();
                  }
               }

               return null;
            }

            BlockPos sync_BlockPos() {
               double cx = this.init_BlockPos.getX() + 0.5;
               double cz = this.init_BlockPos.getZ() + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               double ux = len < 1.0 ? 1.0 : dx / len;
               double uz = len < 1.0 ? 0.0 : dz / len;
               r.Inner_DataUtil zones = r.init_r_DataUtil(mc);
               double[] breath = r.init_DArr(mc);
               List<double[]> lying = this.init_List_11();

               for (double r = 12.0; r <= 15.0; r++) {
                  for (double turn = 0.0; turn <= 1.2; turn += 0.3) {
                     for (int sign : new int[]{1, -1}) {
                        double a = sign * turn;
                        double cos = Math.cos(a);
                        double sin = Math.sin(a);
                        int x = (int)Math.floor(cx + (ux * cos - uz * sin) * r);
                        int z = (int)Math.floor(cz + (ux * sin + uz * cos) * r);
                        BlockPos ground = this.init(x, z);
                        if (ground != null) {
                           BlockPos stand = ground.above();
                           if (autismclient.ai.r.init(zones, breath, lying, x, stand.getY(), z) && ValueSink.init_Z_Minecraft_BlockPos(mc, stand)) {
                              return stand;
                           }
                        }
                     }
                  }
               }

               return null;
            }

            boolean init(BlockPos cell) {
               AABB box = new AABB(cell);

               for (Entity entity : mc.level.getEntities(mc.player, box.inflate(8.0))) {
                  if (entity.blocksBuilding && !entity.isRemoved() && entity.getBoundingBox().intersects(box)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos init(int x, int z) {
               int topY = this.init_BlockPos != null ? this.init_BlockPos.getY() + 1 : mc.player.blockPosition().getY() + 2;

               for (int y = topY; y > topY - 16; y--) {
                  BlockPos p = new BlockPos(x, y, z);
                  if (!cu.init(mc.level, p)) {
                     return null;
                  }

                  if (!mc.level.getBlockState(p).isAir() && mc.level.getBlockState(p.above()).isAir() && mc.level.getBlockState(p.above(2)).isAir()) {
                     return p;
                  }
               }

               return null;
            }

            boolean load(BlockPos pos) {
               return pos != null && q.init_Z(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).getPath());
            }

            boolean write_Z_9() {
               if (this.load_BlockPos != null && this.init_EnderDragon != null && this.init_EnderDragon.isAlive()) {
                  BlockPos head = this.init_BlockPos_19();
                  if (head != null && this.init_q_CachePool != q.Inner_CachePool.write && this.init_q_CachePool != q.Inner_CachePool.read) {
                     double headDistance = Math.sqrt(this.init_EnderDragon.head.getBoundingBox().distanceToSqr(Vec3.atCenterOf(head)));
                     return q.init_Z_Z_D(this.init_q_CachePool == q.Inner_CachePool.init, headDistance);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            int init_I_38() {
               int total = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && q.init_Z(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            boolean update_Z_7() {
               return this.poll_Z && this.init_BlockPos != null && this.init_I_38() > 0 && (bedsMax == 0 || this.init_I < bedsMax);
            }

            int load_I_15() {
               if (!this.update_Z_7()) {
                  return 0;
               } else {
                  return bedsMax == 0 ? this.init_I_38() : Math.min(this.init_I_38(), Math.max(0, bedsMax - this.init_I));
               }
            }

            String load_Str_5() {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && q.init_Z(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())) {
                     return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  }
               }

               return null;
            }

            boolean sync_Z_6() {
               return S.init_I_Str("minecraft:bow") + S.init_I_Str("minecraft:crossbow") > 0 && bK.write() > 0;
            }

            boolean flush_Z_4() {
               Inventory inventory = mc.player.getInventory();

               for (int i = 0; i < 36; i++) {
                  if (inventory.getItem(i).has(DataComponents.WEAPON)) {
                     return true;
                  }
               }

               return false;
            }

            void init_V_32() {
               int slot = af.read();
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  autismclient.util.aw.init_V(mc, slot);
               }
            }

            int init_I(AiOperation.Result r) {
               try {
                  if (r.dataJson() == null) {
                     return 0;
                  }

                  JsonElement parsed = JsonParser.parseString(r.dataJson());
                  return parsed.isJsonObject() && parsed.getAsJsonObject().has("fired") ? parsed.getAsJsonObject().get("fired").getAsInt() : 0;
               } catch (RuntimeException e) {
                  return 0;
               }
            }

            void init_V(AiOperation.Result r) {
               try {
                  if (r.dataJson() == null) {
                     return;
                  }

                  JsonElement parsed = JsonParser.parseString(r.dataJson());
                  if (!parsed.isJsonObject()) {
                     return;
                  }

                  JsonObject data = parsed.getAsJsonObject();
                  if (data.has("missing")) {
                     this.init_JsonObject = data.getAsJsonObject("missing");
                  }

                  if (data.has("code")) {
                     this.read_Str = data.get("code").getAsString();
                  }
               } catch (RuntimeException var4) {
               }
            }

            int init(int cap) {
               return Math.max(10, Math.min(cap, this.read_I_7()));
            }

            int read_I_7() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            void load_V_15() {
               if (this.init_AiOperation != null) {
                  this.init_q_StreamUtil.load(q.Inner_DataUtil.init, this.init_AiOperation);
                  this.init_AiOperation = null;
               }
            }

            void read_V_10() {
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.init, this.init_AiOperation);
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.load, this.load_AiOperation);
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.read, this.read_AiOperation);
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.write, this.write_AiOperation);
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.update, this.update_AiOperation);
               this.init_q_StreamUtil.init_V_q_DataUtil_AiOperation(q.Inner_DataUtil.sync, this.sync_AiOperation);
            }

            JsonObject init_JsonObject_60() {
               JsonObject out = ag.init_JsonObject_ObjArr(
                  "bedsUsed",
                  this.init_I,
                  "bedsLeft",
                  this.init_I_38(),
                  "bowShots",
                  this.load_I,
                  "meleeSwings",
                  this.read_I,
                  "perchCount",
                  this.write_I,
                  "dodges",
                  this.update_I,
                  "dragonDead",
                  this.pop_Z,
                  "ineffectiveBedWindows",
                  this.stop,
                  "lastBedSelfDamageEstimate",
                  this.init_D,
                  "lastBedDragonDamageObserved",
                  this.load_D,
                  "bedsUsable",
                  this.poll_Z,
                  "crystalStockRuns",
                  this.put,
                  "retryableMaterials",
                  this.load_Z,
                  "phase",
                  this.init_q_1_DataUtil.name().toLowerCase(Locale.ROOT),
                  "bedClicked",
                  this.flush_Z
               );
               if (this.flush_Z) {
                  out.addProperty("headRevisionBefore", this.write_J);
                  out.addProperty("footRevisionBefore", this.update_J);
               }

               if (!this.init_Str.isEmpty()) {
                  out.addProperty("dragonPhase", this.init_Str);
               }

               if (this.init_BlockPos != null) {
                  out.add("fountain", ag.init_JsonObject_ObjArr("x", this.init_BlockPos.getX(), "y", this.init_BlockPos.getY(), "z", this.init_BlockPos.getZ()));
               }

               if (this.load_BlockPos != null) {
                  out.add("armedBed", ag.init_JsonObject_ObjArr("x", this.load_BlockPos.getX(), "y", this.load_BlockPos.getY(), "z", this.load_BlockPos.getZ()));
               }

               out.add("survival", X.init_JsonObject_X_BufferHelper(X.init_X_BufferHelper(), this.sync_I, this.load_Str));
               if (this.init_JsonObject != null) {
                  out.add("missing", this.init_JsonObject);
               }

               if (this.read_Str != null) {
                  out.addProperty("code", this.read_Str);
               }

               return out;
            }

            AiOperation.Result init(String message) {
               return this.init_q_StreamUtil.init()
                  ? this.init(AiOperation.Status.UNCERTAIN, null, message)
                  : R.init_AiOperation_Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_60());
            }

            AiOperation.Result load(String message) {
               this.pop_Z = true;
               bL.init_V_Str_Str("dragon_dead", "beds " + this.init_I + ", bow " + this.load_I + ", melee " + this.read_I + ", perches " + this.write_I);
               return this.init(AiOperation.Status.SUCCEEDED, null, message);
            }

            AiOperation.Result init(AiOperation.Status status, A failure, String message) {
               this.read_V_10();
               AiOperation.Result result = this.init_q_StreamUtil.init(status, message, this.init_JsonObject_60(), this.flush_Z);
               this.cancel();
               return new AiOperation.Result(result.status(), result.message(), result.dataJson(), result.evidence(), failure);
            }

            AiOperation.Result init(A failure, String message) {
               return this.init(AiOperation.Status.FAILED, failure, message);
            }

            @Override
            public boolean canInterruptNow() {
               return !this.flush_Z
                  && !this.init_q_StreamUtil.init()
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                  && (this.read_AiOperation == null || this.read_AiOperation.canInterruptNow())
                  && (this.write_AiOperation == null || this.write_AiOperation.canInterruptNow())
                  && (this.update_AiOperation == null || this.update_AiOperation.canInterruptNow())
                  && (this.sync_AiOperation == null || this.sync_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.flush_Z
                  || this.init_q_StreamUtil.init()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                  || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency()
                  || this.write_AiOperation != null && this.write_AiOperation.requiresReconciliationAfterEmergency()
                  || this.update_AiOperation != null && this.update_AiOperation.requiresReconciliationAfterEmergency()
                  || this.sync_AiOperation != null && this.sync_AiOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.tick_Z) {
                  this.tick_Z = true;
                  this.load_V_15();
                  if (this.load_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.load, this.load_AiOperation);
                     this.load_AiOperation = null;
                  }

                  if (this.read_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.read, this.read_AiOperation);
                     this.read_AiOperation = null;
                  }

                  if (this.write_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.write, this.write_AiOperation);
                     this.write_AiOperation = null;
                  }

                  if (this.update_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.update, this.update_AiOperation);
                     this.update_AiOperation = null;
                  }

                  if (this.sync_AiOperation != null) {
                     this.init_q_StreamUtil.load(q.Inner_DataUtil.sync, this.sync_AiOperation);
                     this.sync_AiOperation = null;
                  }

                  this.init_DataUtil.init_V();
               }
            }

            enum Inner_DataUtil {
               init,
               load,
               read,
               write,
               update,
               sync,
               flush,
               close,
               open,
               poll,
               push;
            }
         };
      } else {
         throw new IllegalArgumentException("The dragon fight requires a connected living player");
      }
   }

   enum Inner_CachePool {
      init,
      load,
      read,
      write;
   }

   enum Inner_DataUtil {
      init,
      load,
      read,
      write,
      update,
      sync;
   }

   static final class Inner_StreamUtil {
      private final EnumMap<q.Inner_DataUtil, q.Inner_StreamUtil.Inner_DataUtil> init_EnumMap = new EnumMap<>(q.Inner_DataUtil.class);
      private final JsonObject init_JsonObject = new JsonObject();
      private boolean init_Z;

      AiOperation.Result init_AiOperation_Result_q_DataUtil_AiOperation(q.Inner_DataUtil role, AiOperation child) {
         AiOperation.Result result = child.tick();
         this.init_EnumMap.put(role, new q.Inner_StreamUtil.Inner_DataUtil(child, result));
         if (result.terminal()) {
            result = Y.init_AiOperation_Result_Z_AiOperation_Result(init_Z_AiOperation_AiOperation_Result(child, result), result);
         }

         return result;
      }

      void init_V_q_DataUtil_AiOperation(q.Inner_DataUtil role, AiOperation child) {
         if (child != null) {
            q.Inner_StreamUtil.Inner_DataUtil last = this.init_EnumMap.get(role);
            AiOperation.Result result = last != null && last.child() == child ? last.result() : null;
            if (init_Z_AiOperation_AiOperation_Result(child, result)) {
               this.init_Z = true;
               JsonObject receipt = ag.init_JsonObject_ObjArr("interruptedActionNeedsReconciliation", true);
               if (result != null) {
                  receipt.addProperty("status", result.status().name());
                  receipt.add("receipt", bK.load_JsonObject(result.dataJson()));
               }

               this.init_JsonObject.add(role.name().toLowerCase(Locale.ROOT), receipt);
            }
         }
      }

      void load(q.Inner_DataUtil role, AiOperation child) {
         if (child != null) {
            this.init_V_q_DataUtil_AiOperation(role, child);
            child.cancel();
         }
      }

      boolean init() {
         return this.init_Z;
      }

      AiOperation.Result init(AiOperation.Status status, String message, JsonObject phaseData, boolean bedClicked) {
         JsonObject data = phaseData.deepCopy();
         if (this.init_JsonObject.size() != 0) {
            data.add("pendingChildren", this.init_JsonObject.deepCopy());
         }

         return aB.init_AiOperation_Result_AiOperation_Status(status, message, data, bedClicked || this.init_Z, null);
      }

      private static boolean init_Z_AiOperation_AiOperation_Result(AiOperation child, AiOperation.Result result) {
         JsonObject evidence = result == null ? new JsonObject() : bK.init_JsonObject(result.dataJson());
         return child.requiresReconciliationAfterEmergency()
            || ag.init_Z_JsonObject(evidence, "interruptedActionNeedsReconciliation", false)
            || ag.init_Z_JsonObject(evidence, "reobserveBeforeResuming", false);
      }

      private record Inner_DataUtil(AiOperation child, AiOperation.Result result) {
      }
   }
}
