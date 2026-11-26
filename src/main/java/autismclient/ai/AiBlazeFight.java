package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiFallGuard;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiBlazeFight {
   static final double init_D = 2.15;
   static final double load_D = 2.55;
   static final double read_D = 7.0;
   static final double write_D = 12.0;
   static final int init_I = 25;
   static final int load_I = 30;
   static final int read_I = 30;
   private static final boolean init_Z = Boolean.getBoolean("autism.navigation.trace");
   static final int write_I = 10;
   static final int update_I = 4;
   static final double update_D = 4.4;
   static final int sync = 40;
   static final Map<UUID, Long> init_Map = new ConcurrentHashMap<>();
   private static String init_Str = "";
   private static String load_Str = "";
   static volatile long init_J;
   static final int flush = 24;

   private AiBlazeFight() {
   }

   private static void init_V(Minecraft mc) {
      String world = AiWorldIdentity.init(mc);
      String dimension = AiWorldIdentity.load(mc);
      if (!world.equals(init_Str) || !dimension.equals(load_Str)) {
         init_Map.clear();
         init_Str = world;
         load_Str = dimension;
      }

      long now = System.nanoTime();
      init_Map.entrySet().removeIf(entry -> entry.getValue() <= now);

      while (init_Map.size() > 256) {
         init_Map.remove(init_Map.keySet().iterator().next());
      }
   }

   static boolean init() {
      return System.nanoTime() < init_J;
   }

   static autismclient.ai.engine.CoverGoal init(
      BlockGetter level,
      AABB target,
      Vec3 targetEye,
      double eyeHeight,
      double reach,
      BlockPos current,
      boolean currentUseful,
      boolean allowPillar,
      Predicate<BlockPos> loaded,
      Predicate<BlockPos> safe
   ) {
      BlockPos under = BlockPos.containing(target.getCenter().x, target.minY - 0.5, target.getCenter().z);
      List<BlockPos> stances = new ArrayList<>();
      int groundDy = current.getY() - under.getY();
      int firstDy = Math.min(-3, Math.max(-12, groundDy));

      for (int dx = -3; dx <= 3; dx++) {
         for (int dy = firstDy; dy <= 3; dy++) {
            for (int dz = -3; dz <= 3; dz++) {
               if (dy == groundDy && groundDy < -3 || !(dx * dx + dy * dy + dz * dz > 12.25)) {
                  BlockPos feet = under.offset(dx, dy, dz);
                  if ((!feet.equals(current) || currentUseful)
                     && loaded.test(feet.below())
                     && loaded.test(feet.above(2))
                     && loaded.test(BlockPos.containing(targetEye))
                     && loaded.test(new BlockPos(feet.getX(), feet.getY(), under.getZ()))
                     && loaded.test(new BlockPos(under.getX(), feet.getY(), feet.getZ()))) {
                     Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + eyeHeight, feet.getZ() + 0.5);
                     boolean jump = level.getBlockState(feet.above(2)).getCollisionShape(level, feet.above(2)).isEmpty();
                     if (init(target, eye, reach, true, jump, allowPillar)
                        && safe.test(feet)
                        && level.getFluidState(feet).isEmpty()
                        && level.getFluidState(feet.above()).isEmpty()
                        && level.getFluidState(feet.below()).isEmpty()
                        && autismclient.ai.engine.AiSight.init_Z_BlockGetter_3(level, eye, targetEye, -9223372036854775808L, -9223372036854775808L)) {
                        boolean vertical = target.minY > eye.y && Math.sqrt(target.distanceToSqr(eye.add(0.0, 1.15, 0.0))) > reach;
                        if (!vertical || jump && init(level, feet, loaded)) {
                           stances.add(feet);
                        }
                     }
                  }
               }
            }
         }
      }

      return new autismclient.ai.engine.CoverGoal(stances);
   }

   static boolean init(AABB target, Vec3 eye, double reach, boolean seen, boolean jump, boolean pillar) {
      double horizontal = Math.hypot(target.getCenter().x - eye.x, target.getCenter().z - eye.z);
      double gap = Math.sqrt(target.distanceToSqr(eye));
      double jumpGap = Math.sqrt(target.distanceToSqr(eye.add(0.0, 1.15, 0.0)));
      return seen && (gap <= reach || jump && horizontal < 3.5 && jumpGap <= reach || pillar && horizontal < 3.5 && target.minY > eye.y && jumpGap > reach);
   }

   static int init_I_I(int tries, AiOperation.Status status, double before, double after, boolean useful) {
      if (status != AiOperation.Status.RUNNING && status != AiOperation.Status.UNCERTAIN) {
         return status == AiOperation.Status.SUCCEEDED && (useful || after < before - 0.3) ? 0 : tries + 1;
      } else {
         return tries;
      }
   }

   private static boolean init(BlockGetter level, BlockPos feet, Predicate<BlockPos> loaded) {
      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-1, -3, -1), feet.offset(1, 3, 1))) {
         if (!loaded.test(p) || level.getFluidState(p).is(FluidTags.LAVA)) {
            return false;
         }
      }

      return true;
   }

   static boolean init(Minecraft mc, Entity blaze) {
      if (mc.level != null && blaze != null) {
         BlockPos at = blaze.blockPosition();

         for (int down = 0; down <= 24; down++) {
            BlockPos cell = at.below(down);
            if (!mc.level.hasChunkAt(cell)) {
               return false;
            }

            BlockState state = mc.level.getBlockState(cell);
            if (mc.level.getFluidState(cell).is(FluidTags.LAVA) && state.getCollisionShape(mc.level, cell).isEmpty()) {
               return true;
            }

            if (!state.getCollisionShape(mc.level, cell).isEmpty()) {
               return false;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static <T> T init(T nearest, T dry) {
      return dry != null ? dry : nearest;
   }

   static double init_D_D(double distance, boolean seen, boolean committed, boolean wounded, boolean groundAccess) {
      return distance + (seen ? 0 : 6) - (committed ? 3 : 0) - (wounded ? 6 : 0) + (groundAccess ? 0 : 5);
   }

   static boolean init(BlockGetter level, AABB box, double reach, Predicate<BlockPos> loaded) {
      BlockPos base = BlockPos.containing(box.getCenter().x, box.minY, box.getCenter().z);
      double reachSq = reach * reach;

      for (int dx = -3; dx <= 3; dx++) {
         for (int dz = -3; dz <= 3; dz++) {
            if (dx * dx + dz * dz <= 10) {
               for (int dy = 1; dy >= -6; dy--) {
                  BlockPos floor = base.offset(dx, dy - 1, dz);
                  if (!loaded.test(floor) || !level.getFluidState(floor).isEmpty()) {
                     break;
                  }

                  if (!level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()) {
                     BlockPos feet = floor.above();
                     if (level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                        && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                        && level.getFluidState(feet).isEmpty()) {
                        Vec3 eye = new Vec3(feet.getX() + 0.5, feet.getY() + 1.62, feet.getZ() + 0.5);
                        if (box.distanceToSqr(eye) <= reachSq || box.distanceToSqr(eye.add(0.0, 1.15, 0.0)) <= reachSq) {
                           return true;
                        }
                     }
                     break;
                  }
               }
            }
         }
      }

      return false;
   }

   static AABB init(AABB box, double dx, double dz, Predicate<AABB> free) {
      AABB at = box;

      for (double moved = 0.0; moved < 4.4; moved += 0.25) {
         AABB next = at.move(dx * 0.25, 0.0, dz * 0.25);
         if (!free.test(next)) {
            break;
         }

         at = next;
      }

      return at;
   }

   static boolean init(BlockGetter level, AABB box, Vec3 from, double reach, Predicate<BlockPos> loaded, Predicate<AABB> free) {
      double dx = box.getCenter().x - from.x;
      double dz = box.getCenter().z - from.z;
      double length = Math.hypot(dx, dz);
      return length < 1.0E-4 ? init(level, box, reach + 0.25, loaded) : init(level, init(box, dx / length, dz / length, free), reach + 0.25, loaded);
   }

   static boolean init(boolean shotBlockable, int ticksToShot, int raiseTicks) {
      return !shotBlockable || ticksToShot >= raiseTicks + 2;
   }

   static boolean init(float health, float floor, float targetHealth, double finishDamage, double gap, double reach, boolean seen, int finishTicks) {
      return seen && targetHealth > 0.0F && targetHealth <= finishDamage && gap <= reach + 1.0 && health > floor && finishTicks < 30;
   }

   static AiOperation init(final JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final int count = Math.max(1, AiJson.init_I_JsonObject(args, "count", 1));
      final double radius = Math.max(6.0, Math.min(32.0, AiJson.init_D_JsonObject(args, "radius", 24.0)));
      final float retreat = (float)AiJson.init_D_JsonObject(args, "retreatHealth", 8.0);
      final int seconds = Math.max(10, Math.min(300, AiJson.init_I_JsonObject(args, "timeoutSeconds", 90)));
      return new AiOperation() {
         final Object init_Obj = mc.level;
         final Object load_Obj = mc.getConnection();
         final Object read_Obj = mc.player;
         final long init_J = AiServerEvidence.load_J();
         final long load_J = System.nanoTime() + seconds * 1000000000L;
         final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
         final AiFireControl init_K = new AiFireControl();
         final Random init_Random = new Random();
         Entity init_Entity;
         final AiCombatDeaths init_DataPipe;
         int init_I;
         String init_Str;
         String load_Str;
         AiOperation init_AiOperation;
         int load_I;
         int read_I;
         int write_I;
         int update_I;
         int sync_I;
         int flush_I;
         int close_I;
         int open_I;
         int poll;
         int push;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
         boolean write_Z;
         boolean update_Z;
         boolean sync_Z;
         boolean flush_Z;
         double init_D;
         int pop;
         int tick;
         int run;
         int get;
         int set;
         int put;
         int add;
         int clear;
         int reset;
         int start;
         int stop;
         int next;
         boolean close_Z;
         Vec3 init_Vec3;
         int prev;
         int valid;
         double load_D;
         double read_D;
         double write_D;
         float init_F;
         float load_F;
         Blaze init_Blaze;
         int size;
         AiOperation load_AiOperation;
         final AiPreparationReceipt init_bb;
         AiBlazeArena init_SyncManager;
         boolean open_Z;
         JsonObject init_JsonObject;
         BlockPos init_BlockPos;
         String read_Str;
         long read_J;

         {
            this.init_DataUtil.init_V_DataUtil_DataUtil(autismclient.ai.engine.AiAim.AimProfile.FAST);
            if (AiJson.init_Z_JsonObject(args, "useShield", false) && !AiDefense.init_Z(mc)) {
               AiItems.init_Z(AiItems.load_af_DataUtil("minecraft:shield"));
            }

            this.init_DataPipe = new AiCombatDeaths();
            this.init_Str = "initial blaze";
            this.load_Str = "";
            this.sync_I = this.init_Random.nextBoolean() ? 1 : -1;
            this.init_D = 1.7976931348623157E308;
            this.pop = -1;
            this.start = -100;
            this.stop = 99;
            this.close_Z = true;
            this.valid = -100;
            this.write_D = 1.7976931348623157E308;
            this.init_bb = new AiPreparationReceipt();
            this.init_JsonObject = new JsonObject();
            this.read_Str = "";
         }

         @Override
         public AiOperation.Result tick() {
            if (this.sync_Z) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Blaze fight cancelled");
            }

            if (mc.player != null && mc.level != null) {
               if (mc.level != this.init_Obj || mc.getConnection() != this.load_Obj || mc.player != this.read_Obj) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, "Blaze fight world, connection or player changed");
               }

               if (!mc.player.isAlive() || this.init_J != AiServerEvidence.load_J()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.FAILED, "Death ended this blaze fight");
               }

               if (this.init_bb.init_Z(null)) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                     AiOperation.Status.UNCERTAIN, "Blaze access changes need observation before another action"
                  );
               }

               AiBlazeFight.init_V(mc);
               AiBlazeFight.init_J = System.nanoTime() + 400000000L;
               AiFallGuard.init_V();
               this.push++;
               this.init_DataPipe.init(mc);
               this.load_I = this.init_DataPipe.init_I();
               if (System.nanoTime() > this.load_J) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                     this.load_I >= count ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT,
                     "Fight time over after " + this.load_I + " of " + count + " blazes"
                  );
               }

               float line = retreat + Math.min(6, 2 * Math.max(0, this.get - 1));
               if (mc.player.getHealth() <= line) {
                  boolean finishing = this.init_Entity instanceof LivingEntity living
                     && this.init_Entity.isAlive()
                     && AiBlazeFight.init(
                        mc.player.getHealth(),
                        Math.max(3.0F, retreat - 4.0F),
                        living.getHealth(),
                        this.init(1.0F) * 0.95,
                        Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition())),
                        AiDefense.init_D(mc) - 0.05,
                        mc.player.hasLineOfSight(this.init_Entity),
                        this.clear
                     );
                  if (!finishing) {
                     this.update_Z = true;
                     return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                        AiOperation.Status.FAILED,
                        "Disengaged at "
                           + (int)mc.player.getHealth()
                           + " health with "
                           + this.get
                           + " other blazes in sight ("
                           + this.put
                           + " charged); "
                           + this.load_I
                           + " blazes down"
                     );
                  }

                  this.clear++;
               }

               this.stop = mc.player.isSprinting() ? 0 : Math.min(99, this.stop + 1);
               if (this.init_Entity != null && (!this.init_Entity.isAlive() || this.init_Entity.isRemoved())) {
                  this.init_Entity = null;
                  this.write_Z = false;
               }

               if (this.load_I >= count) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.SUCCEEDED, "Killed " + this.load_I + " blazes");
               }

               if (this.init_Entity != null && mc.level.getEntity(this.init_Entity.getId()) != this.init_Entity) {
                  this.init_Entity = null;
                  this.write_Z = false;
                  this.update_V();
               }

               if (this.write_Z && (this.init_Entity == null || this.init_Entity.distanceToSqr(mc.player) > 49.0)) {
                  this.write_Z = false;
                  this.init_Entity = null;
               }

               if (this.push % 4 == 0 && !this.write_Z) {
                  Entity striker = this.load_Entity();
                  if (striker != null) {
                     this.init(striker, true, "melee attacker at the body");
                  }
               }

               Entity next = this.write_Z ? this.init_Entity : this.init_Entity_2();
               if (next != this.init_Entity) {
                  this.init(next, this.write_Z, this.write_Z ? "defensive threat" : "requested blaze after defense");
               }

               if (!this.open_Z
                  && (this.init_Entity == null || Math.sqrt(this.init_Entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > AiDefense.init_D(mc))) {
                  this.open_Z = true;
                  BlockPos observed = AiWorldScan.init_List_Str_I("minecraft:blaze", 1)
                     .stream()
                     .filter(p -> p.distSqr(mc.player.blockPosition()) < 36.0)
                     .findFirst()
                     .orElse(null);
                  if (observed != null) {
                     this.init_SyncManager = new AiBlazeArena(observed, 8);
                  }
               }

               if (this.init_SyncManager != null && this.write_Z && this.init_SyncManager.canInterruptNow()) {
                  this.init_SyncManager.cancel();
                  this.init_SyncManager = null;
               }

               if ((
                     this.init_K.init_Z()
                        || (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                           && (this.init_SyncManager == null || this.init_SyncManager.canInterruptNow())
                  )
                  && (this.init_K.init_Z() || AiEndRun.init_Z(mc))
                  && this.init_K.init(false, null)) {
                  if (this.load_AiOperation != null && this.load_AiOperation.canInterruptNow()) {
                     this.update_V();
                  }

                  return this.init(this.init_K.init_Str());
               } else {
                  Creeper creeper = AiCombat.init_Creeper(mc);
                  if (creeper == null && this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (creeper != null
                     && this.init_K.load_Z()
                     && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                     && (this.init_SyncManager == null || this.init_SyncManager.canInterruptNow())
                     && autismclient.ai.engine.AiMotionSafety.fill_Z()) {
                     if (this.init_AiOperation == null) {
                        this.update_V();
                        this.write_V_2();
                        this.read_V_3();
                        if (this.init_SyncManager != null) {
                           this.init_SyncManager.cancel();
                           this.init_SyncManager = null;
                        }

                        this.init_AiOperation = AiSurvival.load(AiJson.init_JsonObject_ObjArr("radius", 10, "distance", 10, "timeoutSeconds", 6));
                        AiRunTrace.init_V_Str_JsonObject(
                           "blaze_combat_priority", AiJson.init_JsonObject_ObjArr("reason", "nearby creeper fuse", "entityId", creeper.getId())
                        );
                     }

                     AiOperation.Result escape = this.init_AiOperation.tick();
                     if (!escape.terminal()) {
                        return this.init("Leaving the creeper blast before killing blazes: " + escape.message());
                     } else {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (escape.status() == AiOperation.Status.UNCERTAIN) {
                           this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Blast escape uncertain");
                           return escape;
                        } else {
                           return this.init("Reassessing the creeper blast before resuming blaze combat");
                        }
                     }
                  } else {
                     if (this.init_SyncManager != null) {
                        AiOperation.Result r = this.init_SyncManager.tick();
                        this.init_JsonObject = AiRunResources.init_JsonObject(r.dataJson());
                        if (!r.terminal()) {
                           return this.init(r.message());
                        }

                        this.init_SyncManager.cancel();
                        this.init_SyncManager = null;
                        if (r.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                        }
                     }

                     if (this.init_Entity == null) {
                        this.write_V_2();
                        this.read_V_3();
                        return ++this.close_I > 20
                           ? this.init_AiOperation_Result_AiOperation_Status_Str_5(
                              AiOperation.Status.FAILED,
                              (
                                    this.init_JsonObject_21().get("loadedBlazes").getAsInt() > 0
                                       ? "Loaded blazes have no usable access: " + this.load_Str
                                       : "No loaded blaze available"
                                 )
                                 + "; defeated "
                                 + this.load_I
                                 + " of "
                                 + count
                           )
                           : this.init("Looking for the next blaze");
                     }

                     this.close_I = 0;
                     if ((this.load_AiOperation == null || this.load_AiOperation.canInterruptNow()) && this.init_K.init(true, this.init_Entity.position())) {
                        if (this.load_AiOperation != null && this.load_AiOperation.canInterruptNow()) {
                           this.update_V();
                        }

                        this.read_V_3();
                        return this.init(this.init_K.init_Str());
                     } else {
                        if (this.size > 0 && --this.size == 0 && this.init_F >= 0.0F && this.init_Blaze != null) {
                           float after = this.init_Blaze.getHealth();
                           boolean confirmed = !this.init_Blaze.isRemoved() || after <= 0.0F;
                           JsonObject observation = AiJson.init_JsonObject_ObjArr(
                              "targetId",
                              this.init_Blaze.getId(),
                              "targetUuid",
                              this.init_Blaze.getUUID().toString(),
                              "healthBefore",
                              this.init_F,
                              "crit",
                              this.load_Z,
                              "charge",
                              this.load_F,
                              "healthObserved",
                              confirmed,
                              "targetRemoved",
                              this.init_Blaze.isRemoved()
                           );
                           if (confirmed) {
                              observation.addProperty("healthAfter", after);
                              observation.addProperty("observedHealthDelta", this.init_F - after);
                           }

                           AiRunTrace.init_V_Str_JsonObject("blaze_attack_observation", observation);
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] blaze fight: target {} observed health {} -> {} ({}, charge {})",
                                 this.init_Blaze.getId(),
                                 String.format("%.1f", this.init_F),
                                 confirmed ? String.format("%.1f", after) : "unavailable",
                                 this.load_Z ? "crit" : "plain",
                                 String.format("%.2f", this.load_F)
                              );
                           this.init_Blaze = null;
                        }

                        Vec3 eye = mc.player.getEyePosition();
                        AABB box = this.init_Entity.getBoundingBox();
                        double horizontal = Math.hypot(this.init_Entity.getX() - mc.player.getX(), this.init_Entity.getZ() - mc.player.getZ());
                        boolean seen = mc.player.hasLineOfSight(this.init_Entity);
                        double reach = AiDefense.init_D(mc) - 0.05;
                        if (this.add > 0) {
                           this.add--;
                        }

                        if (this.push % 4 == 0) {
                           this.close_Z = AiBlazeFight.init(mc.level, box, reach + 0.3, p -> AiTerrain.init(mc.level, p));
                        }

                        this.next = !this.close_Z && this.add <= 0 ? this.next + 1 : 0;
                        this.init_V_11();
                        double gapNow = Math.sqrt(box.distanceToSqr(eye));
                        Vec3 jumpEye = eye.add(0.0, 1.15, 0.0);
                        double jumpGap = Math.sqrt(box.distanceToSqr(jumpEye));
                        boolean reachableJumping = jumpGap <= reach;
                        boolean attackStance = AiBlazeFight.init(box, eye, reach, seen, this.read_Z_2(), false);
                        boolean verticalAccess = seen && horizontal < 3.5 && box.minY > eye.y && !reachableJumping;
                        if (attackStance) {
                           this.run = 0;
                           this.read_Z = false;
                        }

                        if (verticalAccess) {
                           this.read_Z = false;
                        }

                        if (this.add > 0 || this.init_Vec3 != null) {
                           this.tick = 0;
                           this.init_D = gapNow;
                           this.pop = this.init_Entity.getId();
                        } else if (!attackStance && !verticalAccess && gapNow > this.init_D - 0.3) {
                           this.tick++;
                        } else {
                           this.tick = 0;
                        }

                        if (gapNow < this.init_D - 0.3 || this.init_Entity.getId() != this.pop) {
                           this.init_D = gapNow;
                           this.pop = this.init_Entity.getId();
                        }

                        if (this.next < 20
                           || this.write_Z
                           || this.load_AiOperation != null && (!this.load_AiOperation.canInterruptNow() || this.init_bb.init_Z(this.load_AiOperation))) {
                           if (this.tick > 30 && (this.load_AiOperation == null || !this.read_Str.equals("approach"))) {
                              this.tick = 0;
                              this.init_D = gapNow;
                              if (++this.run > 3) {
                                 this.init("no approach progress for three attempts", gapNow, 10);
                                 return this.init("Leaving a blaze no route reaches for now");
                              }

                              this.read_Z = true;
                           }

                           boolean chase = seen && box.maxY >= mc.player.getY() - 1.0 && box.minY <= mc.player.getEyeY() + 3.5;
                           if ((this.push < this.reset || !(horizontal > (chase ? 12.0 : 7.0)) && (seen || !(horizontal > 2.5)) && !this.read_Z)
                              && (
                                 this.load_AiOperation == null
                                    || !this.read_Str.equals("approach")
                                    || this.load_AiOperation.canInterruptNow() && !this.init_bb.init_Z(this.load_AiOperation)
                              )) {
                              if (this.load_AiOperation != null && this.read_Str.equals("approach")) {
                                 this.update_V();
                              }

                              if (this.load_AiOperation != null && this.read_Str.equals("pillar")) {
                                 AiOperation.Result r = this.init_bb.init_AiOperation_Result(this.load_AiOperation);
                                 if (r.status() == AiOperation.Status.UNCERTAIN) {
                                    return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                                 }

                                 if (!r.terminal()) {
                                    return this.init("Building up to the blaze");
                                 }

                                 this.update_V();
                              }

                              this.poll = !reachableJumping && box.minY > eye.y && horizontal < 3.5 ? this.poll + 1 : 0;
                              if (this.poll > 30 && this.open_I < 4 && mc.player.onGround() && this.write_Z()) {
                                 this.poll = 0;
                                 this.open_I++;
                                 this.write_V_2();
                                 this.read_V_3();
                                 this.read_Str = "pillar";
                                 this.load_AiOperation = AiGoto.init_AiOperation_BlockPos(mc.player.blockPosition().above(), 0.5, false, true, false, 4);
                                 return this.init("Building up to the blaze");
                              }

                              AiProjectiles.IncomingProjectile coming = AiCombat.init_by_StreamUtil(mc);
                              boolean hasShield = AiDefense.init_Z(mc);
                              double gap = Math.sqrt(box.distanceToSqr(eye));
                              boolean inReach = gap <= reach;
                              float charge = mc.player.getAttackStrengthScale(0.5F);
                              boolean falling = !mc.player.onGround()
                                 && mc.player.getY() < mc.player.yo - 0.02
                                 && mc.player.fallDistance > 0.0
                                 && !mc.player.isInWater()
                                 && !mc.player.onClimbable()
                                 && !mc.player.isPassenger()
                                 && !mc.player.hasEffect(MobEffects.BLINDNESS);
                              boolean blockNow = hasShield
                                 && coming != null
                                 && AiDefense.init(mc, mc.level.getEntity(coming.entityId()))
                                 && AiDefense.init(mc, AiCombat.init(coming))
                                 && AiCombat.init(coming) <= 10;
                              boolean swingFirst = AiBlazeFight.init(blockNow, coming == null ? 2147483647 : AiCombat.init(coming), AiDefense.load(mc));
                              boolean swingSoon = inReach && charge >= 0.85F && swingFirst;
                              Vec3 look = autismclient.ai.engine.AiAim.load_Vec3_Entity(this.init_Entity);
                              if (blockNow && !swingSoon) {
                                 look = AiCombat.init_Vec3_Minecraft_Vec3(mc, look);
                              }

                              this.init_DataUtil.init_V_Vec3(look);
                              this.init_DataUtil.load_V();
                              boolean onTarget = this.init_DataUtil.load_Z_Entity(this.init_Entity);
                              boolean killsNow = this.init_Entity instanceof LivingEntity dying
                                 && dying.getHealth() <= this.init(charge) * 0.95
                                 && charge >= 0.5F;
                              boolean swingDue = inReach && (charge >= 0.92F || killsNow) && onTarget && AiDefense.init(mc, this.init_Entity, this.init_DataUtil);
                              int forward = horizontal > 2.55 ? 1 : (horizontal < 2.15 ? -1 : 0);
                              this.sync_I = 0;
                              if (!mc.player.onGround() || !this.init_Z_I_I(forward, 0)) {
                                 forward = 0;
                              }

                              boolean knockHold = false;
                              boolean finishes = this.init_Entity instanceof LivingEntity hurt && hurt.getHealth() <= this.init(1.0F) * 0.95;
                              if (!finishes && !(gap > reach + 1.5) && mc.player.onGround() && this.prev < 40) {
                                 if (this.push - this.valid >= 4) {
                                    this.valid = this.push;
                                    this.init_Vec3 = this.init_Vec3_Entity_D(this.init_Entity, reach);
                                 }
                              } else {
                                 this.init_Vec3 = null;
                              }

                              if (this.init_Vec3 != null) {
                                 double sx = this.init_Vec3.x - mc.player.getX();
                                 double sz = this.init_Vec3.z - mc.player.getZ();
                                 if (sx * sx + sz * sz < 0.12249999999999998) {
                                    this.init_Vec3 = null;
                                 } else {
                                    this.prev++;
                                    int[] keys = this.init_IArr_D_D(sx, sz);
                                    if (this.init_Z_I_I(keys[0], keys[1])) {
                                       forward = keys[0];
                                       this.sync_I = keys[1];
                                       knockHold = true;
                                    } else {
                                       this.init_Vec3 = null;
                                    }
                                 }
                              }

                              if (this.push % 4 == 0) {
                                 this.init_V_Vec3_Z(eye, !swingDue && !knockHold);
                              }

                              if (this.set > 0 && !swingDue && !knockHold && mc.player.onGround()) {
                                 this.set--;
                                 int[] keys = this.init_IArr_D_D(this.load_D, this.read_D);
                                 if (this.init_Z_I_I(keys[0], keys[1])) {
                                    forward = keys[0];
                                    this.sync_I = keys[1];
                                    this.flush_I = 0;
                                 } else {
                                    this.set = 0;
                                 }
                              }

                              AiProjectiles.IncomingProjectile dodge = null;

                              for (AiProjectiles.IncomingProjectile t : AiProjectiles.init(mc)) {
                                 if (AiProjectiles.init(t) && AiCombat.init(t) <= 20 && (dodge == null || AiCombat.init(t) < AiCombat.init(dodge))) {
                                    dodge = t;
                                 }
                              }

                              if (dodge != null && mc.player.onGround() && (!blockNow || !AiCombat.init_Z_Minecraft(mc, dodge, mc.player.getYRot()))) {
                                 int away = this.init(dodge);
                                 if (this.init_Z_I_I(forward, away)) {
                                    this.sync_I = away;
                                    this.flush_I = 0;
                                 }
                              }

                              if (!swingDue
                                 && !knockHold
                                 && mc.player.onGround()
                                 && (
                                    dodge != null && AiCombat.init(dodge) <= 20
                                       || this.init_Entity.distanceToSqr(mc.player) < 6.760000000000001
                                       || this.init_Z_10()
                                       || this.put > 0
                                       || this.init_Entity.isOnFire() && seen
                                 )) {
                                 double[] away = this.init_DArr();
                                 if (away != null) {
                                    int[] keys = this.init_IArr_D_D(away[0], away[1]);
                                    if (this.init_Z_I_I(keys[0], keys[1])) {
                                       forward = keys[0];
                                       this.sync_I = keys[1];
                                    }
                                 }
                              }

                              if (AiEndRun.init_Z(mc)) {
                                 if (this.init_Z_I_I(-1, this.sync_I)) {
                                    forward = -1;
                                 } else if (this.init_Z_I_I(0, -this.sync_I)) {
                                    this.sync_I = -this.sync_I;
                                 }
                              }

                              mc.options.keyUp.setDown(forward > 0);
                              mc.options.keyDown.setDown(forward < 0);
                              mc.options.keyLeft.setDown(this.sync_I > 0);
                              mc.options.keyRight.setDown(this.sync_I < 0);
                              boolean sprint = forward > 0
                                 && this.sync_I == 0
                                 && (gap > reach + 1.5 || finishes && gap > reach - 0.3)
                                 && !this.init_Z
                                 && !blockNow
                                 && mc.player.onGround()
                                 && autismclient.ai.engine.AiCombatMotion.init(mc, mc.player.getYRot(), 1.0, 0.0, 8, 4, true) != null;
                              if (!sprint && mc.player.isSprinting()) {
                                 mc.player.setSprinting(false);
                              }

                              mc.options.keySprint.setDown(sprint);
                              boolean canFinish = this.init_Entity instanceof LivingEntity living && living.getHealth() <= this.init(charge) * 0.95;
                              boolean canJump = mc.player.onGround() && this.read_Z_2() && autismclient.ai.engine.AiCombatMotion.init(mc) && this.load_Z_5();
                              boolean jump = canJump
                                 && !canFinish
                                 && !blockNow
                                 && !knockHold
                                 && !sprint
                                 && this.stop >= 2
                                 && charge >= 0.72F
                                 && horizontal <= 3.15
                                 && (inReach || reachableJumping);
                              if (jump || !mc.player.onGround()) {
                                 mc.options.keyUp.setDown(false);
                                 mc.options.keyDown.setDown(false);
                                 mc.options.keyLeft.setDown(false);
                                 mc.options.keyRight.setDown(false);
                              }

                              mc.options.keyJump.setDown(jump);
                              if (swingDue
                                 && AiDefense.init(mc, this.init_Entity, this.init_DataUtil)
                                 && swingFirst
                                 && (this.stop >= 2 || killsNow)
                                 && !knockHold
                                 && (falling && charge >= 0.98F || killsNow || mc.player.onGround() && (canFinish || !canJump))) {
                                 if (falling) {
                                    this.write_I++;
                                 }

                                 this.init_F = this.init_Entity instanceof Blaze b ? b.getHealth() : -1.0F;
                                 this.init_Blaze = this.init_Entity instanceof Blaze b ? b : null;
                                 this.size = 3;
                                 this.load_Z = falling;
                                 this.load_F = charge;
                                 this.read_V_3();
                                 mc.options.keySprint.setDown(false);
                                 mc.player.setSprinting(false);
                                 mc.gameMode.attack(mc.player, this.init_Entity);
                                 Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                                 mc.player.resetAttackStrengthTicker();
                                 this.read_I++;
                                 this.add = 25;
                                 this.init_Vec3 = null;
                                 this.prev = 0;
                              }

                              if (blockNow && !this.init_Z) {
                                 this.update_I++;
                              }

                              if (blockNow) {
                                 this.load_V_5();
                              } else {
                                 this.read_V_3();
                              }

                              if (AiBlazeFight.init_Z && autismclient.ai.engine.AiNavDebug.init_Z()) {
                                 autismclient.ai.engine.AiNavDebug.init(
                                    String.format(
                                       Locale.ROOT,
                                       "blaze ring t=%d target %d hp %.1f gap %.2f hz %.2f dy %.2f seen %b ground %b charge %.2f keys %d/%d sprint %b jump %b due %b first %b sprintTicks %d knock %b grace %d stuck %d",
                                       this.push,
                                       this.init_Entity.getId(),
                                       this.init_Entity instanceof Blaze b ? b.getHealth() : -1.0F,
                                       gap,
                                       horizontal,
                                       box.minY - mc.player.getY(),
                                       seen,
                                       this.close_Z,
                                       charge,
                                       forward,
                                       this.sync_I,
                                       sprint,
                                       jump,
                                       swingDue,
                                       swingFirst,
                                       this.stop,
                                       knockHold,
                                       this.add,
                                       this.tick
                                    )
                                 );
                              }

                              return this.init(
                                 (this.init_Z ? "Shield up, " : "")
                                    + (
                                       this.write_Z
                                          ? "fighting the " + this.init_Entity.getType().getDescription().getString().toLowerCase() + " at the player ("
                                          : "fighting the blaze ("
                                    )
                                    + this.load_I
                                    + " down, "
                                    + String.format("%.1f", gap)
                                    + "m)"
                              );
                           } else {
                              if (autismclient.ai.engine.AiNavDebug.init_Z() && (this.load_AiOperation == null || this.push % 10 == 0)) {
                                 autismclient.ai.engine.AiNavDebug.init(
                                    "blaze travel branch t="
                                       + this.push
                                       + " target "
                                       + this.init_Entity.getId()
                                       + " hz "
                                       + String.format("%.2f", horizontal)
                                       + " seen "
                                       + seen
                                       + " chase "
                                       + chase
                                       + " force "
                                       + this.read_Z
                                       + " leg "
                                       + this.read_Str
                                       + " tries "
                                       + this.run
                                 );
                              }

                              AiProjectiles.IncomingProjectile onTheWay = AiDefense.init_Z(mc) ? AiCombat.load(mc) : null;
                              if (onTheWay == null
                                 || !AiDefense.init(mc, mc.level.getEntity(onTheWay.entityId()))
                                 || !AiDefense.init(mc, AiCombat.init(onTheWay))
                                 || AiCombat.init(onTheWay) > 8
                                 || this.load_AiOperation != null && (!this.load_AiOperation.canInterruptNow() || this.init_bb.init_Z(this.load_AiOperation))) {
                                 this.read_V_3();
                                 double gapApproach = Math.sqrt(box.distanceToSqr(eye));
                                 if (this.load_AiOperation != null
                                    && this.read_Str.equals("approach")
                                    && System.nanoTime() - this.read_J > 6000000000L
                                    && this.load_AiOperation.canInterruptNow()
                                    && !this.init_bb.init_Z(this.load_AiOperation)) {
                                    if (gapApproach > this.write_D - 1.5) {
                                       this.update_V();
                                       this.run++;
                                    } else {
                                       this.write_D = gapApproach;
                                       this.read_J = System.nanoTime();
                                    }
                                 }

                                 if (this.run >= 2) {
                                    this.init("two failed or unproductive approach legs", gapNow, 15);
                                    return this.init("Leaving a blaze no route reaches for now");
                                 }

                                 BlockPos under = BlockPos.containing(this.init_Entity.getX(), this.init_Entity.getY() - 0.5, this.init_Entity.getZ());
                                 if (this.load_AiOperation == null
                                    || !this.read_Str.equals("approach")
                                    || this.init_BlockPos.distSqr(under) > 25.0
                                       && this.load_AiOperation.canInterruptNow()
                                       && !this.init_bb.init_Z(this.load_AiOperation)) {
                                    this.update_V();
                                    autismclient.ai.engine.CoverGoal goal = AiBlazeFight.init(
                                       mc.level,
                                       box,
                                       this.init_Entity.getEyePosition(),
                                       mc.player.getEyeHeight(),
                                       reach,
                                       mc.player.blockPosition(),
                                       this.update_Z(),
                                       this.open_I < 4,
                                       p -> AiTerrain.init(mc.level, p),
                                       AiGoto::init_Z
                                    );
                                    if (goal.init_List().isEmpty()) {
                                       if (this.push - this.start >= 20) {
                                          this.run++;
                                          this.start = this.push;
                                       }

                                       this.read_Z = false;
                                       this.reset = this.push + 20;
                                       return this.init("No observed safe attack stance for this blaze");
                                    }

                                    this.write_V_2();
                                    this.read_Str = "approach";
                                    this.init_BlockPos = under;
                                    this.read_J = System.nanoTime();
                                    this.write_D = gapApproach;
                                    this.load_AiOperation = AiGoto.init(
                                       under,
                                       3.5,
                                       true,
                                       true,
                                       true,
                                       20,
                                       autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                       true,
                                       2.0,
                                       goal,
                                       AiGoto.GotoOptions.DEFAULT
                                    );
                                 }

                                 AiOperation.Result r = this.init_bb.init_AiOperation_Result(this.load_AiOperation);
                                 if (autismclient.ai.engine.AiNavDebug.init_Z() && this.push % 20 == 0) {
                                    autismclient.ai.engine.AiNavDebug.init(
                                       "blaze approach target "
                                          + this.init_Entity.getId()
                                          + " at "
                                          + this.init_Entity.blockPosition().toShortString()
                                          + " player "
                                          + mc.player.blockPosition().toShortString()
                                          + " gap "
                                          + String.format("%.1f", gapApproach)
                                          + " tries "
                                          + this.run
                                          + " status "
                                          + r.message()
                                    );
                                 }

                                 if (r.status() == AiOperation.Status.UNCERTAIN) {
                                    return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                                 }

                                 if (r.terminal()) {
                                    this.run = AiBlazeFight.init_I_I(
                                       this.run, r.status(), this.write_D, Math.sqrt(box.distanceToSqr(mc.player.getEyePosition())), this.update_Z()
                                    );
                                    AiRunTrace.init_V_Str_JsonObject(
                                       "blaze_approach_result",
                                       AiJson.init_JsonObject_ObjArr(
                                          "target",
                                          this.init_Entity.getId(),
                                          "status",
                                          r.status().name(),
                                          "reason",
                                          r.message(),
                                          "gapBefore",
                                          this.write_D,
                                          "gapAfter",
                                          Math.sqrt(box.distanceToSqr(mc.player.getEyePosition())),
                                          "attempts",
                                          this.run
                                       )
                                    );
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] blaze approach: target {} {} gap {} -> {}, attempts {}, {}",
                                          this.init_Entity.getId(),
                                          r.status(),
                                          String.format("%.1f", this.write_D),
                                          String.format("%.1f", Math.sqrt(box.distanceToSqr(mc.player.getEyePosition()))),
                                          this.run,
                                          r.message()
                                       );
                                    this.update_V();
                                    this.read_Z = false;
                                 } else if (this.read_Z
                                    && this.update_Z()
                                    && this.load_AiOperation.canInterruptNow()
                                    && !this.init_bb.init_Z(this.load_AiOperation)) {
                                    this.update_V();
                                    this.read_Z = false;
                                 }

                                 return this.init("Closing on the blaze (" + (int)horizontal + "m)");
                              } else {
                                 if (autismclient.ai.engine.AiNavDebug.init_Z() && this.push % 20 == 0) {
                                    autismclient.ai.engine.AiNavDebug.init(
                                       "blaze approach shield target "
                                          + this.init_Entity.getId()
                                          + " player "
                                          + mc.player.blockPosition().toShortString()
                                          + " gap "
                                          + String.format("%.1f", horizontal)
                                    );
                                 }

                                 this.update_V();
                                 this.write_V_2();
                                 this.init_DataUtil
                                    .init_V_Vec3(AiCombat.init_Vec3_Minecraft_Vec3(mc, autismclient.ai.engine.AiAim.load_Vec3_Entity(this.init_Entity)));
                                 this.init_DataUtil.load_V();
                                 if (!this.init_Z) {
                                    this.update_I++;
                                 }

                                 this.load_V_5();
                                 return this.init("Shield up on the way to the blaze");
                              }
                           }
                        } else {
                           this.init("no floor within reach of it", gapNow, 6);
                           return this.init("Leaving a blaze out over the drop");
                        }
                     }
                  }
               }
            } else {
               return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Left the world");
            }
         }

         void init(Entity next, boolean defensive, String reason) {
            this.init_Entity = next;
            this.write_Z = defensive;
            if (!defensive && next instanceof Blaze blaze) {
               this.init_DataPipe.init(blaze);
            }

            this.update_V();
            this.write_V_2();
            this.run = this.tick = this.poll = this.set = 0;
            this.next = this.add = this.clear = this.reset = this.prev = 0;
            this.close_Z = true;
            this.init_Vec3 = null;
            this.read_Z = false;
            this.init_D = 1.7976931348623157E308;
            this.pop = -1;
            this.init_I++;
            this.init_Str = reason;
            AiRunTrace.init_V_Str_JsonObject(
               "blaze_target_switch", AiJson.init_JsonObject_ObjArr("to", next == null ? -1 : next.getId(), "reason", reason, "defensiveDetour", defensive)
            );
         }

         JsonObject init_JsonObject_21() {
            int loaded = 0;
            int deferred = 0;
            if (mc.level != null && mc.player != null) {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Blaze && entity.isAlive() && entity.distanceToSqr(mc.player) <= radius * radius) {
                     loaded++;
                     if (AiBlazeFight.init_Map.containsKey(entity.getUUID())) {
                        deferred++;
                     }
                  }
               }

               return AiJson.init_JsonObject_ObjArr("loadedBlazes", loaded, "deferredBlazes", deferred, "lastUnreachableReason", this.load_Str);
            } else {
               return AiJson.init_JsonObject_ObjArr("contextAvailable", false);
            }
         }

         Entity init_Entity_2() {
            Entity best = null;
            Entity dryBest = null;
            double bestScore = 1.7976931348623157E308;
            double dryScore = 1.7976931348623157E308;
            if (this.init_Entity != null) {
               Long until = AiBlazeFight.init_Map.get(this.init_Entity.getUUID());
               boolean deferred = until != null && until > System.nanoTime();
               boolean keep = AiCombatPriority.init_Z_Z(this.init_Entity.isAlive(), this.init_Entity.distanceTo(mc.player) <= radius, deferred);
               double reachSq = (AiDefense.init_D(mc) - 0.05) * (AiDefense.init_D(mc) - 0.05);
               Vec3 eye = mc.player.getEyePosition();
               double farGap = AiDefense.init_D(mc) + 2.5;
               if (keep && this.add == 0 && this.init_Entity.getBoundingBox().distanceToSqr(eye) > farGap * farGap) {
                  Entity close = null;

                  for (Entity e : mc.level.entitiesForRendering()) {
                     if (e instanceof Blaze
                        && e != this.init_Entity
                        && e.isAlive()
                        && e.distanceToSqr(mc.player) < 6.25
                        && e.getBoundingBox().distanceToSqr(eye) <= reachSq
                        && mc.player.hasLineOfSight(e)
                        && (!args.has("entityId") || e.getId() == AiJson.init_I_JsonObject(args, "entityId", -1))) {
                        close = e;
                        break;
                     }
                  }

                  if (close != null) {
                     AiBlazeFight.init_Map.remove(close.getUUID());
                     return close;
                  }
               }

               if (keep && (this.next < 8 || this.push % 4 != 0)) {
                  return this.init_Entity;
               }
            }

            double reachNow = AiDefense.init_D(mc) + 0.25;

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze blaze && e.isAlive() && (!args.has("entityId") || e.getId() == AiJson.init_I_JsonObject(args, "entityId", -1))) {
                  Long until = AiBlazeFight.init_Map.get(e.getUUID());
                  if (until == null || until <= System.nanoTime()) {
                     double d = e.distanceTo(mc.player);
                     if (!(d > radius)) {
                        boolean ground = e == this.init_Entity
                           ? this.close_Z
                           : AiBlazeFight.init(mc.level, e.getBoundingBox(), reachNow, p -> AiTerrain.init(mc.level, p));
                        double score = AiBlazeFight.init_D_D(
                           d, mc.player.hasLineOfSight(e), e == this.init_Entity, blaze.getHealth() < blaze.getMaxHealth() - 0.5, ground
                        );
                        if (score < bestScore) {
                           bestScore = score;
                           best = e;
                        }

                        if (!AiBlazeFight.init(mc, e) && score < dryScore) {
                           dryScore = score;
                           dryBest = e;
                        }
                     }
                  }
               }
            }

            return AiBlazeFight.init(best, dryBest);
         }

         Vec3 init_Vec3_Entity_D(Entity blaze, double reach) {
            AABB box = blaze.getBoundingBox();
            Predicate<BlockPos> loaded = p -> AiTerrain.init(mc.level, p);
            Predicate<AABB> free = b -> mc.level.noCollision(b);
            Vec3 here = mc.player.position();
            if (AiBlazeFight.init(mc.level, box, here, reach, loaded, free)) {
               return null;
            }

            Vec3 best = null;
            double bestDistance = 1.7976931348623157E308;

            for (int k = 0; k < 16; k++) {
               double a = k * 3.141592653589793 / 8.0;
               double r = 2.3499999999999996;
               Vec3 at = new Vec3(blaze.getX() + Math.cos(a) * r, here.y, blaze.getZ() + Math.sin(a) * r);
               double distance = at.distanceTo(here);
               if (!(distance >= bestDistance) && !(distance > 5.0)) {
                  BlockPos feet = BlockPos.containing(at.x, here.y + 0.01, at.z);
                  if (this.init(feet) && this.init_Z_Vec3_Vec3(here, at)) {
                     Vec3 eyeAt = at.add(0.0, mc.player.getEyeHeight(), 0.0);
                     if (!(box.distanceToSqr(eyeAt) > (reach - 0.1) * (reach - 0.1))
                        && this.load(eyeAt, box.getCenter())
                        && AiBlazeFight.init(mc.level, box, at, reach, loaded, free)) {
                        best = at;
                        bestDistance = distance;
                     }
                  }
               }
            }

            return best;
         }

         boolean init(Entity from) {
            double dx = mc.player.getX() - from.getX();
            double dz = mc.player.getZ() - from.getZ();
            double length = Math.hypot(dx, dz);
            if (length < 0.001) {
               dx = -Math.sin(Math.toRadians(mc.player.getYRot()));
               dz = Math.cos(Math.toRadians(mc.player.getYRot()));
               length = 1.0;
            }

            for (double d = 1.0; d <= 3.0; d++) {
               BlockPos feet = BlockPos.containing(mc.player.getX() + dx / length * d, mc.player.getY() + 0.01, mc.player.getZ() + dz / length * d);
               boolean floor = false;

               for (int down = 1; down <= 2 && !floor; down++) {
                  BlockPos cell = feet.below(down);
                  if (!AiTerrain.init(mc.level, cell) || mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
                     break;
                  }

                  floor = !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
               }

               if (!floor && mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()) {
                  return false;
               }
            }

            return true;
         }

         double[] init_DArr() {
            double ax = 0.0;
            double az = 0.0;
            boolean edge = false;

            for (int k = 0; k < 8; k++) {
               double a = k * 3.141592653589793 / 4.0;
               double dx = Math.cos(a);
               double dz = Math.sin(a);
               boolean safe = true;

               for (double out : new double[]{1.0, 1.6}) {
                  BlockPos feet = BlockPos.containing(mc.player.getX() + dx * out, mc.player.getY() + 0.01, mc.player.getZ() + dz * out);
                  boolean floor = false;

                  for (int down = 1; down <= 2 && !floor; down++) {
                     BlockPos cell = feet.below(down);
                     if (!AiTerrain.init(mc.level, cell) || mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
                        break;
                     }

                     floor = !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
                  }

                  boolean wall = !mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty();
                  if (wall) {
                     break;
                  }

                  if (!floor) {
                     safe = false;
                     break;
                  }
               }

               if (safe) {
                  ax += dx;
                  az += dz;
               } else {
                  edge = true;
                  ax -= dx;
                  az -= dz;
               }
            }

            return edge && Math.hypot(ax, az) > 0.3 ? new double[]{ax, az} : null;
         }

         boolean init_Z_10() {
            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.init_Entity && e.isAlive() && e.distanceToSqr(mc.player) < 6.760000000000001) {
                  return true;
               }
            }

            return false;
         }

         boolean load_Z_5() {
            if (!this.init(this.init_Entity)) {
               return false;
            }

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.init_Entity && e.isAlive() && e.distanceToSqr(mc.player) < 12.25 && !this.init(e)) {
                  return false;
               }
            }

            return true;
         }

         boolean init(BlockPos feet) {
            if (AiTerrain.init(mc.level, feet.below()) && AiTerrain.init(mc.level, feet.above())) {
               for (int dy = -1; dy <= 1; dy++) {
                  if (mc.level.getFluidState(feet.above(dy)).is(FluidTags.LAVA)) {
                     return false;
                  }
               }

               return !mc.level.getBlockState(feet.below()).getCollisionShape(mc.level, feet.below()).isEmpty()
                  && mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()
                  && mc.level.getBlockState(feet.above()).getCollisionShape(mc.level, feet.above()).isEmpty();
            } else {
               return false;
            }
         }

         boolean init_Z_Vec3_Vec3(Vec3 from, Vec3 to) {
            double length = from.distanceTo(to);
            int steps = Math.max(1, (int)Math.ceil(length / 0.5));

            for (int i = 1; i <= steps; i++) {
               Vec3 at = from.lerp(to, (double)i / steps);
               if (!this.init(BlockPos.containing(at.x, from.y + 0.01, at.z))) {
                  return false;
               }
            }

            return true;
         }

         void init(String reason, double gap, int secondsx) {
            this.load_Str = reason;
            boolean wounded = this.init_Entity instanceof Blaze b && b.getHealth() < b.getMaxHealth() - 0.5;
            int hold = wounded ? Math.min(seconds, 6) : seconds;
            AiRunTrace.init_V_Str_JsonObject(
               "blaze_unreachable",
               AiJson.init_JsonObject_ObjArr("target", this.init_Entity.getId(), "reason", reason, "gap", gap, "wounded", wounded, "seconds", hold)
            );
            AutismClientAddon.LOG
               .info(
                  "[AI] blaze deferred: target {} at {}, player {}, gap {}, {} ({} s)",
                  this.init_Entity.getId(),
                  this.init_Entity.blockPosition(),
                  mc.player.blockPosition(),
                  String.format("%.1f", gap),
                  reason,
                  hold
               );
            AiBlazeFight.init_Map.put(this.init_Entity.getUUID(), System.nanoTime() + hold * 1000000000L);
            this.run = 0;
            this.read_Z = false;
            this.update_V();
            this.init_Entity = null;
         }

         double init(float charge) {
            return Math.max(mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE), 1.0 + AiItems.init_D(mc.player.getMainHandItem()))
               * (0.2 + 0.8 * charge * charge);
         }

         void init_V_Vec3_Z(Vec3 eye, boolean mayMove) {
            List<Entity> others = new ArrayList<>();

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.init_Entity && e.isAlive() && e.distanceToSqr(eye) < 400.0) {
                  others.add(e);
               }
            }

            this.get = this.init_I_Vec3_List(eye, others);
            int charged = 0;

            for (Entity o : others) {
               if (o.isOnFire() && this.load(o.getEyePosition(), eye)) {
                  charged++;
               }
            }

            this.put = charged;
            if (this.get != 0 && mayMove && mc.player.onGround()) {
               int best = this.get;
               Vec3 targetPoint = this.init_Entity.getBoundingBox().getCenter();

               for (int k = 0; k < 8; k++) {
                  double a = k * 3.141592653589793 / 4.0;
                  double dx = Math.cos(a) * 1.3;
                  double dz = Math.sin(a) * 1.3;
                  Vec3 at = eye.add(dx, 0.0, dz);
                  double h = Math.hypot(this.init_Entity.getX() - at.x, this.init_Entity.getZ() - at.z);
                  if (!(h < 1.4) && !(h > 3.4) && this.load(at, targetPoint)) {
                     int[] keys = this.init_IArr_D_D(dx, dz);
                     if (this.init_Z_I_I(keys[0], keys[1])) {
                        int e = this.init_I_Vec3_List(at, others);
                        if (e < best) {
                           best = e;
                           this.load_D = dx;
                           this.read_D = dz;
                           this.set = 6;
                        }
                     }
                  }
               }
            } else {
               this.set = 0;
            }
         }

         int init_I_Vec3_List(Vec3 at, List<Entity> others) {
            int n = 0;

            for (Entity o : others) {
               if (this.load(o.getEyePosition(), at)) {
                  n++;
               }
            }

            return n;
         }

         boolean load(Vec3 from, Vec3 to) {
            return mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player)).getType() == Type.MISS;
         }

         int[] init_IArr_D_D(double dx, double dz) {
            double yaw = Math.toRadians(mc.player.getYRot());
            double length = Math.hypot(dx, dz);
            double f = (-dx * Math.sin(yaw) + dz * Math.cos(yaw)) / length;
            double l = (dx * Math.cos(yaw) + dz * Math.sin(yaw)) / length;
            return new int[]{f > 0.38 ? 1 : (f < -0.38 ? -1 : 0), l > 0.38 ? 1 : (l < -0.38 ? -1 : 0)};
         }

         Entity load_Entity() {
            Entity best = null;
            double bestGap = 1.0;

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Mob mob
                  && !(e instanceof Blaze)
                  && mob.isAlive()
                  && e instanceof Enemy
                  && !(mob.distanceToSqr(mc.player) > 36.0)
                  && AiThreats.load(AiThreats.init_cv_DataUtil(e))
                  && AiThreats.init_Z(mc, e)) {
                  double gap = AiThreats.init(mob, mc.player.getBoundingBox());
                  if (gap < bestGap) {
                     bestGap = gap;
                     best = e;
                  }
               }
            }

            return best;
         }

         int init(AiProjectiles.IncomingProjectile t) {
            double vx = t.vx();
            double vz = t.vz();
            double v = Math.hypot(vx, vz);
            double px = mc.player.getX() - t.x();
            double pz = mc.player.getZ() - t.z();
            double wx = px;
            double wz = pz;
            if (v > 1.0E-6) {
               double along = (px * vx + pz * vz) / (v * v);
               wx = px - vx * along;
               wz = pz - vz * along;
            }

            if (Math.hypot(wx, wz) < 0.05) {
               return this.sync_I == 0 ? 1 : this.sync_I;
            }

            double yaw = Math.toRadians(mc.player.getYRot());
            return wx * Math.cos(yaw) + wz * Math.sin(yaw) >= 0.0 ? 1 : -1;
         }

         boolean read_Z_2() {
            BlockPos head = BlockPos.containing(mc.player.getX(), mc.player.getEyeY() + 0.9, mc.player.getZ());
            return mc.level.getBlockState(head).getCollisionShape(mc.level, head).isEmpty() && !mc.player.isInWater();
         }

         boolean init_Z_I_I(int forward, int strafe) {
            if (mc.player.onGround()) {
               AABB end = autismclient.ai.engine.AiCombatMotion.init(mc, forward, strafe, 6);
               if (end == null) {
                  return false;
               }

               if (forward == 0 && strafe == 0) {
                  return true;
               }

               AABB core = end.deflate(0.2, 0.0, 0.2);
               return !mc.level.noCollision(new AABB(core.minX, core.minY - 0.55, core.minZ, core.maxX, core.minY - 0.01, core.maxZ));
            } else {
               if (!autismclient.ai.engine.AiMotionSafety.putAt_Z_D(forward, strafe, false, false)) {
                  return false;
               }

               if (forward == 0 && strafe == 0) {
                  return true;
               }

               if (mc.player.onGround() && !mc.player.isInWater() && autismclient.ai.engine.AiCombatMotion.init(mc, forward, strafe, 6) == null) {
                  return false;
               }

               if (!autismclient.ai.engine.AiMotionSafety.putAt_Z_D(forward, strafe, false, false)) {
                  return false;
               }

               double yaw = Math.toRadians(mc.player.getYRot());
               double dx = strafe * Math.cos(yaw) - forward * Math.sin(yaw);
               double dz = forward * Math.cos(yaw) + strafe * Math.sin(yaw);
               double length = Math.hypot(dx, dz);
               Vec3 at = mc.player.position().add(dx / length * 1.2, 0.0, dz / length * 1.2);
               BlockPos feet = BlockPos.containing(at.x, mc.player.getY() + 0.01, at.z);

               for (int up = 0; up <= 1; up++) {
                  if (mc.level.getFluidState(feet.above(up)).is(FluidTags.LAVA)) {
                     return false;
                  }
               }

               for (int down = 1; down <= 4; down++) {
                  BlockPos cell = feet.below(down);
                  if (mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
                     return false;
                  }

                  if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return down <= 3;
                  }
               }

               return false;
            }
         }

         boolean write_Z() {
            return AiBlazeFight.init(mc.level, mc.player.blockPosition(), p -> AiTerrain.init(mc.level, p));
         }

         boolean update_Z() {
            return this.init_Entity != null
               && this.init_Entity.isAlive()
               && AiBlazeFight.init(
                  this.init_Entity.getBoundingBox(),
                  mc.player.getEyePosition(),
                  AiDefense.init_D(mc) - 0.05,
                  mc.player.hasLineOfSight(this.init_Entity),
                  this.read_Z_2(),
                  this.open_I < 4 && mc.player.onGround() && this.write_Z()
               );
         }

         void init_V_11() {
            if (this.push % 10 == 1) {
               int slot = AiItems.init(36);
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  AiItems.load(slot);
               }
            }
         }

         void load_V_5() {
            if (!this.init_Z) {
               this.init_Z = true;
               mc.options.keySprint.setDown(false);
               mc.options.keyUse.setDown(true);
            }
         }

         void read_V_3() {
            if (this.init_Z) {
               this.init_Z = false;
               mc.options.keyUse.setDown(false);
               if (mc.player.isUsingItem()) {
                  mc.gameMode.releaseUsingItem(mc.player);
               }
            }
         }

         void write_V_2() {
            for (KeyMapping key : new KeyMapping[]{
               mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint
            }) {
               key.setDown(false);
            }
         }

         void update_V() {
            if (this.load_AiOperation != null) {
               this.flush_Z = this.flush_Z | this.init_bb.init_Z(this.load_AiOperation);
               this.init_bb.init_V(this.load_AiOperation);
            }

            this.load_AiOperation = null;
            this.read_Str = "";
         }

         AiOperation.Result init(String message) {
            JsonObject data = AiJson.init_JsonObject_ObjArr(
               "kills",
               this.load_I,
               "requestedKills",
               count,
               "swings",
               this.read_I,
               "crits",
               this.write_I,
               "shieldRaises",
               this.update_I,
               "shielding",
               this.init_Z,
               "otherBlazesInSight",
               this.get,
               "targetSwitches",
               this.init_I,
               "selectionReason",
               this.init_Str,
               "availability",
               this.init_JsonObject_21(),
               "pendingChangesNeedObservation",
               this.flush_Z,
               "pillars",
               this.open_I,
               "travel",
               this.read_Str,
               "detour",
               this.write_Z,
               "firesExtinguished",
               this.init_K.init_I(),
               "knockStance",
               this.init_Vec3 != null,
               "knockGrace",
               this.add,
               "targetGround",
               this.close_Z,
               "positioning",
               "stable-footing; cover only between swings; no routine circling"
            );
            data.add("spawnerSetup", this.init_JsonObject);
            if (this.init_Entity != null) {
               data.add(
                  "target",
                  AiJson.init_JsonObject_ObjArr(
                     "id",
                     this.init_Entity.getId(),
                     "uuid",
                     this.init_Entity.getUUID().toString(),
                     "x",
                     this.init_Entity.getX(),
                     "y",
                     this.init_Entity.getY(),
                     "z",
                     this.init_Entity.getZ(),
                     "alive",
                     this.init_Entity.isAlive(),
                     "health",
                     this.init_Entity instanceof LivingEntity living ? living.getHealth() : -1
                  )
               );
            }

            return AiGameOperations.init_AiOperation_Result(AiOperation.Status.RUNNING, message, data);
         }

         AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status status, String message) {
            boolean pending = this.requiresReconciliationAfterEmergency();
            this.sync_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            this.update_V();
            this.write_V_2();
            this.read_V_3();
            this.init_K.init_V();
            if (this.init_SyncManager != null) {
               this.init_SyncManager.cancel();
               this.init_SyncManager = null;
            }

            this.init_DataUtil.init_V();
            AiBlazeFight.init_J = 0L;
            return this.init_bb
               .init(
                  AiGameOperations.init_AiOperation_Result(
                     status,
                     message + " (" + this.read_I + " swings, " + this.write_I + " crits, " + this.update_I + " shield raises)",
                     AiJson.init_JsonObject_ObjArr(
                        "kills",
                        this.load_I,
                        "targetSwitches",
                        this.init_I,
                        "selectionReason",
                        this.init_Str,
                        "availability",
                        this.init_JsonObject_21(),
                        "verifiedDeathTypes",
                        this.init_DataPipe.init_JsonArray(),
                        "swings",
                        this.read_I,
                        "crits",
                        this.write_I,
                        "shieldRaises",
                        this.update_I,
                        "firesExtinguished",
                        this.init_K.init_I(),
                        "verifiedBlocks",
                        "not_observable",
                        "requestedKills",
                        count,
                        "retreated",
                        this.update_Z,
                        "spawnerSetup",
                        this.init_JsonObject,
                        "positioning",
                        "stable-footing; cover only between swings; no routine circling"
                     )
                  ),
                  pending
               );
         }

         @Override
         public boolean canInterruptNow() {
            if (this.flush_Z || this.init_bb.init_Z(this.load_AiOperation)) {
               return false;
            }

            if (this.init_AiOperation != null && !this.init_AiOperation.canInterruptNow()) {
               return false;
            }

            if (!this.init_K.load_Z()) {
               return false;
            }

            if (this.init_SyncManager != null && !this.init_SyncManager.canInterruptNow()) {
               return false;
            }

            if (this.load_AiOperation != null && !this.load_AiOperation.canInterruptNow()) {
               return false;
            }

            if (this.init_Entity != null && mc.player != null) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Monster m && !(e instanceof Blaze) && m.isAlive() && m.distanceToSqr(mc.player) < 9.0 && AiThreats.init_Z(mc, m)) {
                     return true;
                  }
               }

               return false;
            } else {
               return true;
            }
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.flush_Z
               || this.init_bb.init_Z(this.load_AiOperation)
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
               || this.init_K.read()
               || this.init_SyncManager != null && this.init_SyncManager.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "cancelled");
         }
      };
   }

   static List<AiProjectiles.IncomingProjectile> init_List(Minecraft mc) {
      List<AiProjectiles.IncomingProjectile> out = new ArrayList<>();

      for (AiProjectiles.IncomingProjectile t : AiProjectiles.init(mc)) {
         if (AiProjectiles.init(t)) {
            out.add(t);
         }
      }

      return out;
   }
}
