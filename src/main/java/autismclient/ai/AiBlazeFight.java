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
   private static final boolean TRACE_NAVIGATION = Boolean.getBoolean("autism.navigation.trace");
   static final int write_I = 10;
   static final int update_I = 4;
   static final double update_D = 4.4;
   static final int sync = 40;
   static final Map<UUID, Long> deferredUntil = new ConcurrentHashMap<>();
   private static String lastWorld = "";
   private static String lastDimension = "";
   static volatile long init_J;
   static final int flush = 24;

   private AiBlazeFight() {
   }

   private static void resetForWorld(Minecraft mc) {
      String world = AiWorldIdentity.currentWorldKey(mc);
      String dimension = AiWorldIdentity.currentDimensionId(mc);
      if (!world.equals(lastWorld) || !dimension.equals(lastDimension)) {
         deferredUntil.clear();
         lastWorld = world;
         lastDimension = dimension;
      }

      long now = System.nanoTime();
      deferredUntil.entrySet().removeIf(entry -> entry.getValue() <= now);

      while (deferredUntil.size() > 256) {
         deferredUntil.remove(deferredUntil.keySet().iterator().next());
      }
   }

   static boolean init() {
      return System.nanoTime() < init_J;
   }

   static autismclient.ai.engine.CoverGoal chooseCoverGoal(
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
                     if (canStrikeFrom(target, eye, reach, true, jump, allowPillar)
                        && safe.test(feet)
                        && level.getFluidState(feet).isEmpty()
                        && level.getFluidState(feet.above()).isEmpty()
                        && level.getFluidState(feet.below()).isEmpty()
                        && autismclient.ai.engine.AiSight.hasLineOfSight(level, eye, targetEye, -9223372036854775808L, -9223372036854775808L)) {
                        boolean vertical = target.minY > eye.y && Math.sqrt(target.distanceToSqr(eye.add(0.0, 1.15, 0.0))) > reach;
                        if (!vertical || jump && isLoadedAndLavaFree(level, feet, loaded)) {
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

   static boolean canStrikeFrom(AABB target, Vec3 eye, double reach, boolean seen, boolean jump, boolean pillar) {
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

   private static boolean isLoadedAndLavaFree(BlockGetter level, BlockPos feet, Predicate<BlockPos> loaded) {
      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-1, -3, -1), feet.offset(1, 3, 1))) {
         if (!loaded.test(p) || level.getFluidState(p).is(FluidTags.LAVA)) {
            return false;
         }
      }

      return true;
   }

   static boolean isAboveLava(Minecraft mc, Entity blaze) {
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

   static <T> T preferDry(T nearest, T dry) {
      return dry != null ? dry : nearest;
   }

   static double targetScore(double distance, boolean seen, boolean committed, boolean wounded, boolean groundAccess) {
      return distance + (seen ? 0 : 6) - (committed ? 3 : 0) - (wounded ? 6 : 0) + (groundAccess ? 0 : 5);
   }

   static boolean hasStanceWithinReach(BlockGetter level, AABB box, double reach, Predicate<BlockPos> loaded) {
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

   static AABB slideFreeBox(AABB box, double dx, double dz, Predicate<AABB> free) {
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

   static boolean hasGroundAccessFrom(BlockGetter level, AABB box, Vec3 from, double reach, Predicate<BlockPos> loaded, Predicate<AABB> free) {
      double dx = box.getCenter().x - from.x;
      double dz = box.getCenter().z - from.z;
      double length = Math.hypot(dx, dz);
      return length < 1.0E-4 ? hasStanceWithinReach(level, box, reach + 0.25, loaded) : hasStanceWithinReach(level, slideFreeBox(box, dx / length, dz / length, free), reach + 0.25, loaded);
   }

   static boolean canSwingFirst(boolean shotBlockable, int ticksToShot, int raiseTicks) {
      return !shotBlockable || ticksToShot >= raiseTicks + 2;
   }

   static boolean canFinishTarget(float health, float floor, float targetHealth, double finishDamage, double gap, double reach, boolean seen, int finishTicks) {
      return seen && targetHealth > 0.0F && targetHealth <= finishDamage && gap <= reach + 1.0 && health > floor && finishTicks < 30;
   }

   static AiOperation blazeFight(final JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      final int count = Math.max(1, AiJson.getInt(args, "count", 1));
      final double radius = Math.max(6.0, Math.min(32.0, AiJson.getDouble(args, "radius", 24.0)));
      final float retreat = (float)AiJson.getDouble(args, "retreatHealth", 8.0);
      final int seconds = Math.max(10, Math.min(300, AiJson.getInt(args, "timeoutSeconds", 90)));
      return new AiOperation() {
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final Object startPlayer = mc.player;
         final long startDeathRevision = AiServerEvidence.deathRevision();
         final long deadline = System.nanoTime() + seconds * 1000000000L;
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         final AiFireControl fireControl = new AiFireControl();
         final Random random = new Random();
         Entity target;
         final AiCombatDeaths deaths;
         int retargetCount;
         String lastRetargetReason;
         String load_Str;
         AiOperation init_AiOperation;
         int kills;
         int read_I;
         int write_I;
         int update_I;
         int strafeDirection;
         int flush_I;
         int close_I;
         int open_I;
         int belowTargetTicks;
         int tickCount;
         boolean init_Z;
         boolean load_Z;
         boolean read_Z;
         boolean defensive;
         boolean update_Z;
         boolean sync_Z;
         boolean flush_Z;
         double lastGap;
         int progressTargetId;
         int stuckTicks;
         int approachAttempts;
         int othersInSight;
         int sideStepTicks;
         int chargedOthersInSight;
         int postHitGrace;
         int finishTicks;
         int noStanceHoldUntil;
         int lastRetryTick;
         int ticksSinceSprint;
         int ticksWithoutStance;
         boolean close_Z;
         Vec3 aimPoint;
         int aimPointSteps;
         int aimPointTick;
         double moveDx;
         double moveDz;
         double approachGap;
         float healthBeforeShot;
         float lastCharge;
         Blaze shotBlaze;
         int shotConfirmTicks;
         AiOperation child;
         final AiPreparationReceipt preparation;
         AiBlazeArena arena;
         boolean open_Z;
         JsonObject evidence;
         BlockPos init_BlockPos;
         String movementMode;
         long read_J;

         {
            this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
            if (AiJson.getBoolean(args, "useShield", false) && !AiDefense.hasShield(mc)) {
               AiItems.equipOffhand(AiItems.resolveItem("minecraft:shield"));
            }

            this.deaths = new AiCombatDeaths();
            this.lastRetargetReason = "initial blaze";
            this.load_Str = "";
            this.strafeDirection = this.random.nextBoolean() ? 1 : -1;
            this.lastGap = 1.7976931348623157E308;
            this.progressTargetId = -1;
            this.lastRetryTick = -100;
            this.ticksSinceSprint = 99;
            this.close_Z = true;
            this.aimPointTick = -100;
            this.approachGap = 1.7976931348623157E308;
            this.preparation = new AiPreparationReceipt();
            this.evidence = new JsonObject();
            this.movementMode = "";
         }

         @Override
         public AiOperation.Result tick() {
            if (this.sync_Z) {
               return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Blaze fight cancelled");
            }

            if (mc.player != null && mc.level != null) {
               if (mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, "Blaze fight world, connection or player changed");
               }

               if (!mc.player.isAlive() || this.startDeathRevision != AiServerEvidence.deathRevision()) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.FAILED, "Death ended this blaze fight");
               }

               if (this.preparation.requiresReconciliation(null)) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                     AiOperation.Status.UNCERTAIN, "Blaze access changes need observation before another action"
                  );
               }

               AiBlazeFight.resetForWorld(mc);
               AiBlazeFight.init_J = System.nanoTime() + 400000000L;
               AiFallGuard.markDriven();
               this.tickCount++;
               this.deaths.sweep(mc);
               this.kills = this.deaths.killCount();
               if (System.nanoTime() > this.deadline) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                     this.kills >= count ? AiOperation.Status.SUCCEEDED : AiOperation.Status.TIMED_OUT,
                     "Fight time over after " + this.kills + " of " + count + " blazes"
                  );
               }

               float line = retreat + Math.min(6, 2 * Math.max(0, this.othersInSight - 1));
               if (mc.player.getHealth() <= line) {
                  boolean finishing = this.target instanceof LivingEntity living
                     && this.target.isAlive()
                     && AiBlazeFight.canFinishTarget(
                        mc.player.getHealth(),
                        Math.max(3.0F, retreat - 4.0F),
                        living.getHealth(),
                        this.meleeDamage(1.0F) * 0.95,
                        Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition())),
                        AiDefense.attackReach(mc) - 0.05,
                        mc.player.hasLineOfSight(this.target),
                        this.finishTicks
                     );
                  if (!finishing) {
                     this.update_Z = true;
                     return this.init_AiOperation_Result_AiOperation_Status_Str_5(
                        AiOperation.Status.FAILED,
                        "Disengaged at "
                           + (int)mc.player.getHealth()
                           + " health with "
                           + this.othersInSight
                           + " other blazes in sight ("
                           + this.chargedOthersInSight
                           + " charged); "
                           + this.kills
                           + " blazes down"
                     );
                  }

                  this.finishTicks++;
               }

               this.ticksSinceSprint = mc.player.isSprinting() ? 0 : Math.min(99, this.ticksSinceSprint + 1);
               if (this.target != null && (!this.target.isAlive() || this.target.isRemoved())) {
                  this.target = null;
                  this.defensive = false;
               }

               if (this.kills >= count) {
                  return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.SUCCEEDED, "Killed " + this.kills + " blazes");
               }

               if (this.target != null && mc.level.getEntity(this.target.getId()) != this.target) {
                  this.target = null;
                  this.defensive = false;
                  this.cancelChild();
               }

               if (this.defensive && (this.target == null || this.target.distanceToSqr(mc.player) > 49.0)) {
                  this.defensive = false;
                  this.target = null;
               }

               if (this.tickCount % 4 == 0 && !this.defensive) {
                  Entity striker = this.load_Entity();
                  if (striker != null) {
                     this.switchTarget(striker, true, "melee attacker at the body");
                  }
               }

               Entity next = this.defensive ? this.target : this.pickTarget();
               if (next != this.target) {
                  this.switchTarget(next, this.defensive, this.defensive ? "defensive threat" : "requested blaze after defense");
               }

               if (!this.open_Z
                  && (this.target == null || Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > AiDefense.attackReach(mc))) {
                  this.open_Z = true;
                  BlockPos observed = AiWorldScan.findMobSpawners("minecraft:blaze", 1)
                     .stream()
                     .filter(p -> p.distSqr(mc.player.blockPosition()) < 36.0)
                     .findFirst()
                     .orElse(null);
                  if (observed != null) {
                     this.arena = new AiBlazeArena(observed, 8);
                  }
               }

               if (this.arena != null && this.defensive && this.arena.canInterruptNow()) {
                  this.arena.cancel();
                  this.arena = null;
               }

               if ((
                     this.fireControl.hasPendingFire()
                        || (this.child == null || this.child.canInterruptNow())
                           && (this.arena == null || this.arena.canInterruptNow())
                  )
                  && (this.fireControl.hasPendingFire() || AiEndRun.init_Z(mc))
                  && this.fireControl.init(false, null)) {
                  if (this.child != null && this.child.canInterruptNow()) {
                     this.cancelChild();
                  }

                  return this.running(this.fireControl.status());
               } else {
                  Creeper creeper = AiCombat.mostDangerousCreeper(mc);
                  if (creeper == null && this.init_AiOperation != null) {
                     this.init_AiOperation.cancel();
                     this.init_AiOperation = null;
                  }

                  if (creeper != null
                     && this.fireControl.canInterrupt()
                     && (this.child == null || this.child.canInterruptNow())
                     && (this.arena == null || this.arena.canInterruptNow())
                     && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                     if (this.init_AiOperation == null) {
                        this.cancelChild();
                        this.releaseMovementKeys();
                        this.stopUsingItem();
                        if (this.arena != null) {
                           this.arena.cancel();
                           this.arena = null;
                        }

                        this.init_AiOperation = AiSurvival.flee(AiJson.objectOf("radius", 10, "distance", 10, "timeoutSeconds", 6));
                        AiRunTrace.emitServiceEvent(
                           "blaze_combat_priority", AiJson.objectOf("reason", "nearby creeper fuse", "entityId", creeper.getId())
                        );
                     }

                     AiOperation.Result escape = this.init_AiOperation.tick();
                     if (!escape.terminal()) {
                        return this.running("Leaving the creeper blast before killing blazes: " + escape.message());
                     } else {
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        if (escape.status() == AiOperation.Status.UNCERTAIN) {
                           this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Blast escape uncertain");
                           return escape;
                        } else {
                           return this.running("Reassessing the creeper blast before resuming blaze combat");
                        }
                     }
                  } else {
                     if (this.arena != null) {
                        AiOperation.Result r = this.arena.tick();
                        this.evidence = AiRunResources.parseEvidenceJson(r.dataJson());
                        if (!r.terminal()) {
                           return this.running(r.message());
                        }

                        this.arena.cancel();
                        this.arena = null;
                        if (r.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                        }
                     }

                     if (this.target == null) {
                        this.releaseMovementKeys();
                        this.stopUsingItem();
                        return ++this.close_I > 20
                           ? this.init_AiOperation_Result_AiOperation_Status_Str_5(
                              AiOperation.Status.FAILED,
                              (
                                    this.init_JsonObject_21().get("loadedBlazes").getAsInt() > 0
                                       ? "Loaded blazes have no usable access: " + this.load_Str
                                       : "No loaded blaze available"
                                 )
                                 + "; defeated "
                                 + this.kills
                                 + " of "
                                 + count
                           )
                           : this.running("Looking for the next blaze");
                     }

                     this.close_I = 0;
                     if ((this.child == null || this.child.canInterruptNow()) && this.fireControl.init(true, this.target.position())) {
                        if (this.child != null && this.child.canInterruptNow()) {
                           this.cancelChild();
                        }

                        this.stopUsingItem();
                        return this.running(this.fireControl.status());
                     } else {
                        if (this.shotConfirmTicks > 0 && --this.shotConfirmTicks == 0 && this.healthBeforeShot >= 0.0F && this.shotBlaze != null) {
                           float after = this.shotBlaze.getHealth();
                           boolean confirmed = !this.shotBlaze.isRemoved() || after <= 0.0F;
                           JsonObject observation = AiJson.objectOf(
                              "targetId",
                              this.shotBlaze.getId(),
                              "targetUuid",
                              this.shotBlaze.getUUID().toString(),
                              "healthBefore",
                              this.healthBeforeShot,
                              "crit",
                              this.load_Z,
                              "charge",
                              this.lastCharge,
                              "healthObserved",
                              confirmed,
                              "targetRemoved",
                              this.shotBlaze.isRemoved()
                           );
                           if (confirmed) {
                              observation.addProperty("healthAfter", after);
                              observation.addProperty("observedHealthDelta", this.healthBeforeShot - after);
                           }

                           AiRunTrace.emitServiceEvent("blaze_attack_observation", observation);
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] blaze fight: target {} observed health {} -> {} ({}, charge {})",
                                 this.shotBlaze.getId(),
                                 String.format("%.1f", this.healthBeforeShot),
                                 confirmed ? String.format("%.1f", after) : "unavailable",
                                 this.load_Z ? "crit" : "plain",
                                 String.format("%.2f", this.lastCharge)
                              );
                           this.shotBlaze = null;
                        }

                        Vec3 eye = mc.player.getEyePosition();
                        AABB box = this.target.getBoundingBox();
                        double horizontal = Math.hypot(this.target.getX() - mc.player.getX(), this.target.getZ() - mc.player.getZ());
                        boolean seen = mc.player.hasLineOfSight(this.target);
                        double reach = AiDefense.attackReach(mc) - 0.05;
                        if (this.postHitGrace > 0) {
                           this.postHitGrace--;
                        }

                        if (this.tickCount % 4 == 0) {
                           this.close_Z = AiBlazeFight.hasStanceWithinReach(mc.level, box, reach + 0.3, p -> AiTerrain.isLoaded(mc.level, p));
                        }

                        this.ticksWithoutStance = !this.close_Z && this.postHitGrace <= 0 ? this.ticksWithoutStance + 1 : 0;
                        this.selectBestWeapon();
                        double gapNow = Math.sqrt(box.distanceToSqr(eye));
                        Vec3 jumpEye = eye.add(0.0, 1.15, 0.0);
                        double jumpGap = Math.sqrt(box.distanceToSqr(jumpEye));
                        boolean reachableJumping = jumpGap <= reach;
                        boolean attackStance = AiBlazeFight.canStrikeFrom(box, eye, reach, seen, this.read_Z_2(), false);
                        boolean verticalAccess = seen && horizontal < 3.5 && box.minY > eye.y && !reachableJumping;
                        if (attackStance) {
                           this.approachAttempts = 0;
                           this.read_Z = false;
                        }

                        if (verticalAccess) {
                           this.read_Z = false;
                        }

                        if (this.postHitGrace > 0 || this.aimPoint != null) {
                           this.stuckTicks = 0;
                           this.lastGap = gapNow;
                           this.progressTargetId = this.target.getId();
                        } else if (!attackStance && !verticalAccess && gapNow > this.lastGap - 0.3) {
                           this.stuckTicks++;
                        } else {
                           this.stuckTicks = 0;
                        }

                        if (gapNow < this.lastGap - 0.3 || this.target.getId() != this.progressTargetId) {
                           this.lastGap = gapNow;
                           this.progressTargetId = this.target.getId();
                        }

                        if (this.ticksWithoutStance < 20
                           || this.defensive
                           || this.child != null && (!this.child.canInterruptNow() || this.preparation.requiresReconciliation(this.child))) {
                           if (this.stuckTicks > 30 && (this.child == null || !this.movementMode.equals("approach"))) {
                              this.stuckTicks = 0;
                              this.lastGap = gapNow;
                              if (++this.approachAttempts > 3) {
                                 this.markUnreachable("no approach progress for three attempts", gapNow, 10);
                                 return this.running("Leaving a blaze no route reaches for now");
                              }

                              this.read_Z = true;
                           }

                           boolean chase = seen && box.maxY >= mc.player.getY() - 1.0 && box.minY <= mc.player.getEyeY() + 3.5;
                           if ((this.tickCount < this.noStanceHoldUntil || !(horizontal > (chase ? 12.0 : 7.0)) && (seen || !(horizontal > 2.5)) && !this.read_Z)
                              && (
                                 this.child == null
                                    || !this.movementMode.equals("approach")
                                    || this.child.canInterruptNow() && !this.preparation.requiresReconciliation(this.child)
                              )) {
                              if (this.child != null && this.movementMode.equals("approach")) {
                                 this.cancelChild();
                              }

                              if (this.child != null && this.movementMode.equals("pillar")) {
                                 AiOperation.Result r = this.preparation.tickChild(this.child);
                                 if (r.status() == AiOperation.Status.UNCERTAIN) {
                                    return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                                 }

                                 if (!r.terminal()) {
                                    return this.running("Building up to the blaze");
                                 }

                                 this.cancelChild();
                              }

                              this.belowTargetTicks = !reachableJumping && box.minY > eye.y && horizontal < 3.5 ? this.belowTargetTicks + 1 : 0;
                              if (this.belowTargetTicks > 30 && this.open_I < 4 && mc.player.onGround() && this.isStandingSafe()) {
                                 this.belowTargetTicks = 0;
                                 this.open_I++;
                                 this.releaseMovementKeys();
                                 this.stopUsingItem();
                                 this.movementMode = "pillar";
                                 this.child = AiGoto.travelToCompat(mc.player.blockPosition().above(), 0.5, false, true, false, 4);
                                 return this.running("Building up to the blaze");
                              }

                              AiProjectiles.IncomingProjectile coming = AiCombat.nextIncoming(mc);
                              boolean hasShield = AiDefense.hasShield(mc);
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
                                 && AiDefense.canBlock(mc, mc.level.getEntity(coming.entityId()))
                                 && AiDefense.shieldReadyFor(mc, AiCombat.ticksToClosest(coming))
                                 && AiCombat.ticksToClosest(coming) <= 10;
                              boolean swingFirst = AiBlazeFight.canSwingFirst(blockNow, coming == null ? 2147483647 : AiCombat.ticksToClosest(coming), AiDefense.blockDelayTicks(mc));
                              boolean swingSoon = inReach && charge >= 0.85F && swingFirst;
                              Vec3 look = autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target);
                              if (blockNow && !swingSoon) {
                                 look = AiCombat.init_Vec3_Minecraft_Vec3(mc, look);
                              }

                              this.aim.aimAt(look);
                              this.aim.tick();
                              boolean onTarget = this.aim.isAimedAtEntity(this.target);
                              boolean killsNow = this.target instanceof LivingEntity dying
                                 && dying.getHealth() <= this.meleeDamage(charge) * 0.95
                                 && charge >= 0.5F;
                              boolean swingDue = inReach && (charge >= 0.92F || killsNow) && onTarget && AiDefense.canSwingAt(mc, this.target, this.aim);
                              int forward = horizontal > 2.55 ? 1 : (horizontal < 2.15 ? -1 : 0);
                              this.strafeDirection = 0;
                              if (!mc.player.onGround() || !this.init_Z_I_I(forward, 0)) {
                                 forward = 0;
                              }

                              boolean knockHold = false;
                              boolean finishes = this.target instanceof LivingEntity hurt && hurt.getHealth() <= this.meleeDamage(1.0F) * 0.95;
                              if (!finishes && !(gap > reach + 1.5) && mc.player.onGround() && this.aimPointSteps < 40) {
                                 if (this.tickCount - this.aimPointTick >= 4) {
                                    this.aimPointTick = this.tickCount;
                                    this.aimPoint = this.standingPointFor(this.target, reach);
                                 }
                              } else {
                                 this.aimPoint = null;
                              }

                              if (this.aimPoint != null) {
                                 double sx = this.aimPoint.x - mc.player.getX();
                                 double sz = this.aimPoint.z - mc.player.getZ();
                                 if (sx * sx + sz * sz < 0.12249999999999998) {
                                    this.aimPoint = null;
                                 } else {
                                    this.aimPointSteps++;
                                    int[] keys = this.movementKeysFor(sx, sz);
                                    if (this.init_Z_I_I(keys[0], keys[1])) {
                                       forward = keys[0];
                                       this.strafeDirection = keys[1];
                                       knockHold = true;
                                    } else {
                                       this.aimPoint = null;
                                    }
                                 }
                              }

                              if (this.tickCount % 4 == 0) {
                                 this.init_V_Vec3_Z(eye, !swingDue && !knockHold);
                              }

                              if (this.sideStepTicks > 0 && !swingDue && !knockHold && mc.player.onGround()) {
                                 this.sideStepTicks--;
                                 int[] keys = this.movementKeysFor(this.moveDx, this.moveDz);
                                 if (this.init_Z_I_I(keys[0], keys[1])) {
                                    forward = keys[0];
                                    this.strafeDirection = keys[1];
                                    this.flush_I = 0;
                                 } else {
                                    this.sideStepTicks = 0;
                                 }
                              }

                              AiProjectiles.IncomingProjectile dodge = null;

                              for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(mc)) {
                                 if (AiProjectiles.isImminent(t) && AiCombat.ticksToClosest(t) <= 20 && (dodge == null || AiCombat.ticksToClosest(t) < AiCombat.ticksToClosest(dodge))) {
                                    dodge = t;
                                 }
                              }

                              if (dodge != null && mc.player.onGround() && (!blockNow || !AiCombat.init_Z_Minecraft(mc, dodge, mc.player.getYRot()))) {
                                 int away = this.strafeSideFor(dodge);
                                 if (this.init_Z_I_I(forward, away)) {
                                    this.strafeDirection = away;
                                    this.flush_I = 0;
                                 }
                              }

                              if (!swingDue
                                 && !knockHold
                                 && mc.player.onGround()
                                 && (
                                    dodge != null && AiCombat.ticksToClosest(dodge) <= 20
                                       || this.target.distanceToSqr(mc.player) < 6.760000000000001
                                       || this.isBlazeTooClose()
                                       || this.chargedOthersInSight > 0
                                       || this.target.isOnFire() && seen
                                 )) {
                                 double[] away = this.init_DArr();
                                 if (away != null) {
                                    int[] keys = this.movementKeysFor(away[0], away[1]);
                                    if (this.init_Z_I_I(keys[0], keys[1])) {
                                       forward = keys[0];
                                       this.strafeDirection = keys[1];
                                    }
                                 }
                              }

                              if (AiEndRun.init_Z(mc)) {
                                 if (this.init_Z_I_I(-1, this.strafeDirection)) {
                                    forward = -1;
                                 } else if (this.init_Z_I_I(0, -this.strafeDirection)) {
                                    this.strafeDirection = -this.strafeDirection;
                                 }
                              }

                              mc.options.keyUp.setDown(forward > 0);
                              mc.options.keyDown.setDown(forward < 0);
                              mc.options.keyLeft.setDown(this.strafeDirection > 0);
                              mc.options.keyRight.setDown(this.strafeDirection < 0);
                              boolean sprint = forward > 0
                                 && this.strafeDirection == 0
                                 && (gap > reach + 1.5 || finishes && gap > reach - 0.3)
                                 && !this.init_Z
                                 && !blockNow
                                 && mc.player.onGround()
                                 && autismclient.ai.engine.AiCombatMotion.predictTrace(mc, mc.player.getYRot(), 1.0, 0.0, 8, 4, true) != null;
                              if (!sprint && mc.player.isSprinting()) {
                                 mc.player.setSprinting(false);
                              }

                              mc.options.keySprint.setDown(sprint);
                              boolean canFinish = this.target instanceof LivingEntity living && living.getHealth() <= this.meleeDamage(charge) * 0.95;
                              boolean canJump = mc.player.onGround() && this.read_Z_2() && autismclient.ai.engine.AiCombatMotion.jumpSafe(mc) && this.load_Z_5();
                              boolean jump = canJump
                                 && !canFinish
                                 && !blockNow
                                 && !knockHold
                                 && !sprint
                                 && this.ticksSinceSprint >= 2
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
                                 && AiDefense.canSwingAt(mc, this.target, this.aim)
                                 && swingFirst
                                 && (this.ticksSinceSprint >= 2 || killsNow)
                                 && !knockHold
                                 && (falling && charge >= 0.98F || killsNow || mc.player.onGround() && (canFinish || !canJump))) {
                                 if (falling) {
                                    this.write_I++;
                                 }

                                 this.healthBeforeShot = this.target instanceof Blaze b ? b.getHealth() : -1.0F;
                                 this.shotBlaze = this.target instanceof Blaze b ? b : null;
                                 this.shotConfirmTicks = 3;
                                 this.load_Z = falling;
                                 this.lastCharge = charge;
                                 this.stopUsingItem();
                                 mc.options.keySprint.setDown(false);
                                 mc.player.setSprinting(false);
                                 mc.gameMode.attack(mc.player, this.target);
                                 Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                                 mc.player.resetAttackStrengthTicker();
                                 this.read_I++;
                                 this.postHitGrace = 25;
                                 this.aimPoint = null;
                                 this.aimPointSteps = 0;
                              }

                              if (blockNow && !this.init_Z) {
                                 this.update_I++;
                              }

                              if (blockNow) {
                                 this.startUsingItem();
                              } else {
                                 this.stopUsingItem();
                              }

                              if (AiBlazeFight.TRACE_NAVIGATION && autismclient.ai.engine.AiNavDebug.isEnabled()) {
                                 autismclient.ai.engine.AiNavDebug.log(
                                    String.format(
                                       Locale.ROOT,
                                       "blaze ring t=%d target %d hp %.1f gap %.2f hz %.2f dy %.2f seen %b ground %b charge %.2f keys %d/%d sprint %b jump %b due %b first %b sprintTicks %d knock %b grace %d stuck %d",
                                       this.tickCount,
                                       this.target.getId(),
                                       this.target instanceof Blaze b ? b.getHealth() : -1.0F,
                                       gap,
                                       horizontal,
                                       box.minY - mc.player.getY(),
                                       seen,
                                       this.close_Z,
                                       charge,
                                       forward,
                                       this.strafeDirection,
                                       sprint,
                                       jump,
                                       swingDue,
                                       swingFirst,
                                       this.ticksSinceSprint,
                                       knockHold,
                                       this.postHitGrace,
                                       this.stuckTicks
                                    )
                                 );
                              }

                              return this.running(
                                 (this.init_Z ? "Shield up, " : "")
                                    + (
                                       this.defensive
                                          ? "fighting the " + this.target.getType().getDescription().getString().toLowerCase() + " at the player ("
                                          : "fighting the blaze ("
                                    )
                                    + this.kills
                                    + " down, "
                                    + String.format("%.1f", gap)
                                    + "m)"
                              );
                           } else {
                              if (autismclient.ai.engine.AiNavDebug.isEnabled() && (this.child == null || this.tickCount % 10 == 0)) {
                                 autismclient.ai.engine.AiNavDebug.log(
                                    "blaze travel branch t="
                                       + this.tickCount
                                       + " target "
                                       + this.target.getId()
                                       + " hz "
                                       + String.format("%.2f", horizontal)
                                       + " seen "
                                       + seen
                                       + " chase "
                                       + chase
                                       + " force "
                                       + this.read_Z
                                       + " leg "
                                       + this.movementMode
                                       + " tries "
                                       + this.approachAttempts
                                 );
                              }

                              AiProjectiles.IncomingProjectile onTheWay = AiDefense.hasShield(mc) ? AiCombat.nearestIncoming(mc) : null;
                              if (onTheWay == null
                                 || !AiDefense.canBlock(mc, mc.level.getEntity(onTheWay.entityId()))
                                 || !AiDefense.shieldReadyFor(mc, AiCombat.ticksToClosest(onTheWay))
                                 || AiCombat.ticksToClosest(onTheWay) > 8
                                 || this.child != null && (!this.child.canInterruptNow() || this.preparation.requiresReconciliation(this.child))) {
                                 this.stopUsingItem();
                                 double gapApproach = Math.sqrt(box.distanceToSqr(eye));
                                 if (this.child != null
                                    && this.movementMode.equals("approach")
                                    && System.nanoTime() - this.read_J > 6000000000L
                                    && this.child.canInterruptNow()
                                    && !this.preparation.requiresReconciliation(this.child)) {
                                    if (gapApproach > this.approachGap - 1.5) {
                                       this.cancelChild();
                                       this.approachAttempts++;
                                    } else {
                                       this.approachGap = gapApproach;
                                       this.read_J = System.nanoTime();
                                    }
                                 }

                                 if (this.approachAttempts >= 2) {
                                    this.markUnreachable("two failed or unproductive approach legs", gapNow, 15);
                                    return this.running("Leaving a blaze no route reaches for now");
                                 }

                                 BlockPos under = BlockPos.containing(this.target.getX(), this.target.getY() - 0.5, this.target.getZ());
                                 if (this.child == null
                                    || !this.movementMode.equals("approach")
                                    || this.init_BlockPos.distSqr(under) > 25.0
                                       && this.child.canInterruptNow()
                                       && !this.preparation.requiresReconciliation(this.child)) {
                                    this.cancelChild();
                                    autismclient.ai.engine.CoverGoal goal = AiBlazeFight.chooseCoverGoal(
                                       mc.level,
                                       box,
                                       this.target.getEyePosition(),
                                       mc.player.getEyeHeight(),
                                       reach,
                                       mc.player.blockPosition(),
                                       this.update_Z(),
                                       this.open_I < 4,
                                       p -> AiTerrain.isLoaded(mc.level, p),
                                       AiGoto::isStandableCompat
                                    );
                                    if (goal.init_List().isEmpty()) {
                                       if (this.tickCount - this.lastRetryTick >= 20) {
                                          this.approachAttempts++;
                                          this.lastRetryTick = this.tickCount;
                                       }

                                       this.read_Z = false;
                                       this.noStanceHoldUntil = this.tickCount + 20;
                                       return this.running("No observed safe attack stance for this blaze");
                                    }

                                    this.releaseMovementKeys();
                                    this.movementMode = "approach";
                                    this.init_BlockPos = under;
                                    this.read_J = System.nanoTime();
                                    this.approachGap = gapApproach;
                                    this.child = AiGoto.travelToCompat(
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

                                 AiOperation.Result r = this.preparation.tickChild(this.child);
                                 if (autismclient.ai.engine.AiNavDebug.isEnabled() && this.tickCount % 20 == 0) {
                                    autismclient.ai.engine.AiNavDebug.log(
                                       "blaze approach target "
                                          + this.target.getId()
                                          + " at "
                                          + this.target.blockPosition().toShortString()
                                          + " player "
                                          + mc.player.blockPosition().toShortString()
                                          + " gap "
                                          + String.format("%.1f", gapApproach)
                                          + " tries "
                                          + this.approachAttempts
                                          + " status "
                                          + r.message()
                                    );
                                 }

                                 if (r.status() == AiOperation.Status.UNCERTAIN) {
                                    return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.UNCERTAIN, r.message());
                                 }

                                 if (r.terminal()) {
                                    this.approachAttempts = AiBlazeFight.init_I_I(
                                       this.approachAttempts, r.status(), this.approachGap, Math.sqrt(box.distanceToSqr(mc.player.getEyePosition())), this.update_Z()
                                    );
                                    AiRunTrace.emitServiceEvent(
                                       "blaze_approach_result",
                                       AiJson.objectOf(
                                          "target",
                                          this.target.getId(),
                                          "status",
                                          r.status().name(),
                                          "reason",
                                          r.message(),
                                          "gapBefore",
                                          this.approachGap,
                                          "gapAfter",
                                          Math.sqrt(box.distanceToSqr(mc.player.getEyePosition())),
                                          "attempts",
                                          this.approachAttempts
                                       )
                                    );
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] blaze approach: target {} {} gap {} -> {}, attempts {}, {}",
                                          this.target.getId(),
                                          r.status(),
                                          String.format("%.1f", this.approachGap),
                                          String.format("%.1f", Math.sqrt(box.distanceToSqr(mc.player.getEyePosition()))),
                                          this.approachAttempts,
                                          r.message()
                                       );
                                    this.cancelChild();
                                    this.read_Z = false;
                                 } else if (this.read_Z
                                    && this.update_Z()
                                    && this.child.canInterruptNow()
                                    && !this.preparation.requiresReconciliation(this.child)) {
                                    this.cancelChild();
                                    this.read_Z = false;
                                 }

                                 return this.running("Closing on the blaze (" + (int)horizontal + "m)");
                              } else {
                                 if (autismclient.ai.engine.AiNavDebug.isEnabled() && this.tickCount % 20 == 0) {
                                    autismclient.ai.engine.AiNavDebug.log(
                                       "blaze approach shield target "
                                          + this.target.getId()
                                          + " player "
                                          + mc.player.blockPosition().toShortString()
                                          + " gap "
                                          + String.format("%.1f", horizontal)
                                    );
                                 }

                                 this.cancelChild();
                                 this.releaseMovementKeys();
                                 this.aim
                                    .aimAt(AiCombat.init_Vec3_Minecraft_Vec3(mc, autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target)));
                                 this.aim.tick();
                                 if (!this.init_Z) {
                                    this.update_I++;
                                 }

                                 this.startUsingItem();
                                 return this.running("Shield up on the way to the blaze");
                              }
                           }
                        } else {
                           this.markUnreachable("no floor within reach of it", gapNow, 6);
                           return this.running("Leaving a blaze out over the drop");
                        }
                     }
                  }
               }
            } else {
               return this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "Left the world");
            }
         }

         void switchTarget(Entity next, boolean defensive, String reason) {
            this.target = next;
            this.defensive = defensive;
            if (!defensive && next instanceof Blaze blaze) {
               this.deaths.watch(blaze);
            }

            this.cancelChild();
            this.releaseMovementKeys();
            this.approachAttempts = this.stuckTicks = this.belowTargetTicks = this.sideStepTicks = 0;
            this.ticksWithoutStance = this.postHitGrace = this.finishTicks = this.noStanceHoldUntil = this.aimPointSteps = 0;
            this.close_Z = true;
            this.aimPoint = null;
            this.read_Z = false;
            this.lastGap = 1.7976931348623157E308;
            this.progressTargetId = -1;
            this.retargetCount++;
            this.lastRetargetReason = reason;
            AiRunTrace.emitServiceEvent(
               "blaze_target_switch", AiJson.objectOf("to", next == null ? -1 : next.getId(), "reason", reason, "defensiveDetour", defensive)
            );
         }

         JsonObject init_JsonObject_21() {
            int loaded = 0;
            int deferred = 0;
            if (mc.level != null && mc.player != null) {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Blaze && entity.isAlive() && entity.distanceToSqr(mc.player) <= radius * radius) {
                     loaded++;
                     if (AiBlazeFight.deferredUntil.containsKey(entity.getUUID())) {
                        deferred++;
                     }
                  }
               }

               return AiJson.objectOf("loadedBlazes", loaded, "deferredBlazes", deferred, "lastUnreachableReason", this.load_Str);
            } else {
               return AiJson.objectOf("contextAvailable", false);
            }
         }

         Entity pickTarget() {
            Entity best = null;
            Entity dryBest = null;
            double bestScore = 1.7976931348623157E308;
            double dryScore = 1.7976931348623157E308;
            if (this.target != null) {
               Long until = AiBlazeFight.deferredUntil.get(this.target.getUUID());
               boolean deferred = until != null && until > System.nanoTime();
               boolean keep = AiCombatPriority.keepTarget(this.target.isAlive(), this.target.distanceTo(mc.player) <= radius, deferred);
               double reachSq = (AiDefense.attackReach(mc) - 0.05) * (AiDefense.attackReach(mc) - 0.05);
               Vec3 eye = mc.player.getEyePosition();
               double farGap = AiDefense.attackReach(mc) + 2.5;
               if (keep && this.postHitGrace == 0 && this.target.getBoundingBox().distanceToSqr(eye) > farGap * farGap) {
                  Entity close = null;

                  for (Entity e : mc.level.entitiesForRendering()) {
                     if (e instanceof Blaze
                        && e != this.target
                        && e.isAlive()
                        && e.distanceToSqr(mc.player) < 6.25
                        && e.getBoundingBox().distanceToSqr(eye) <= reachSq
                        && mc.player.hasLineOfSight(e)
                        && (!args.has("entityId") || e.getId() == AiJson.getInt(args, "entityId", -1))) {
                        close = e;
                        break;
                     }
                  }

                  if (close != null) {
                     AiBlazeFight.deferredUntil.remove(close.getUUID());
                     return close;
                  }
               }

               if (keep && (this.ticksWithoutStance < 8 || this.tickCount % 4 != 0)) {
                  return this.target;
               }
            }

            double reachNow = AiDefense.attackReach(mc) + 0.25;

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze blaze && e.isAlive() && (!args.has("entityId") || e.getId() == AiJson.getInt(args, "entityId", -1))) {
                  Long until = AiBlazeFight.deferredUntil.get(e.getUUID());
                  if (until == null || until <= System.nanoTime()) {
                     double d = e.distanceTo(mc.player);
                     if (!(d > radius)) {
                        boolean ground = e == this.target
                           ? this.close_Z
                           : AiBlazeFight.hasStanceWithinReach(mc.level, e.getBoundingBox(), reachNow, p -> AiTerrain.isLoaded(mc.level, p));
                        double score = AiBlazeFight.targetScore(
                           d, mc.player.hasLineOfSight(e), e == this.target, blaze.getHealth() < blaze.getMaxHealth() - 0.5, ground
                        );
                        if (score < bestScore) {
                           bestScore = score;
                           best = e;
                        }

                        if (!AiBlazeFight.isAboveLava(mc, e) && score < dryScore) {
                           dryScore = score;
                           dryBest = e;
                        }
                     }
                  }
               }
            }

            return AiBlazeFight.preferDry(best, dryBest);
         }

         Vec3 standingPointFor(Entity blaze, double reach) {
            AABB box = blaze.getBoundingBox();
            Predicate<BlockPos> loaded = p -> AiTerrain.isLoaded(mc.level, p);
            Predicate<AABB> free = b -> mc.level.noCollision(b);
            Vec3 here = mc.player.position();
            if (AiBlazeFight.hasGroundAccessFrom(mc.level, box, here, reach, loaded, free)) {
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
                  if (this.isSafeStance(feet) && this.init_Z_Vec3_Vec3(here, at)) {
                     Vec3 eyeAt = at.add(0.0, mc.player.getEyeHeight(), 0.0);
                     if (!(box.distanceToSqr(eyeAt) > (reach - 0.1) * (reach - 0.1))
                        && this.hasClearLine(eyeAt, box.getCenter())
                        && AiBlazeFight.hasGroundAccessFrom(mc.level, box, at, reach, loaded, free)) {
                        best = at;
                        bestDistance = distance;
                     }
                  }
               }
            }

            return best;
         }

         boolean knockbackAwayIsSafe(Entity from) {
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
                  if (!AiTerrain.isLoaded(mc.level, cell) || mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
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
                     if (!AiTerrain.isLoaded(mc.level, cell) || mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
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

         boolean isBlazeTooClose() {
            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.target && e.isAlive() && e.distanceToSqr(mc.player) < 6.760000000000001) {
                  return true;
               }
            }

            return false;
         }

         boolean load_Z_5() {
            if (!this.knockbackAwayIsSafe(this.target)) {
               return false;
            }

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.target && e.isAlive() && e.distanceToSqr(mc.player) < 12.25 && !this.knockbackAwayIsSafe(e)) {
                  return false;
               }
            }

            return true;
         }

         boolean isSafeStance(BlockPos feet) {
            if (AiTerrain.isLoaded(mc.level, feet.below()) && AiTerrain.isLoaded(mc.level, feet.above())) {
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
               if (!this.isSafeStance(BlockPos.containing(at.x, from.y + 0.01, at.z))) {
                  return false;
               }
            }

            return true;
         }

         void markUnreachable(String reason, double gap, int secondsx) {
            this.load_Str = reason;
            boolean wounded = this.target instanceof Blaze b && b.getHealth() < b.getMaxHealth() - 0.5;
            int hold = wounded ? Math.min(seconds, 6) : seconds;
            AiRunTrace.emitServiceEvent(
               "blaze_unreachable",
               AiJson.objectOf("target", this.target.getId(), "reason", reason, "gap", gap, "wounded", wounded, "seconds", hold)
            );
            AutismClientAddon.LOG
               .info(
                  "[AI] blaze deferred: target {} at {}, player {}, gap {}, {} ({} s)",
                  this.target.getId(),
                  this.target.blockPosition(),
                  mc.player.blockPosition(),
                  String.format("%.1f", gap),
                  reason,
                  hold
               );
            AiBlazeFight.deferredUntil.put(this.target.getUUID(), System.nanoTime() + hold * 1000000000L);
            this.approachAttempts = 0;
            this.read_Z = false;
            this.cancelChild();
            this.target = null;
         }

         double meleeDamage(float charge) {
            return Math.max(mc.player.getAttributeValue(Attributes.ATTACK_DAMAGE), 1.0 + AiItems.attackDamageBonus(mc.player.getMainHandItem()))
               * (0.2 + 0.8 * charge * charge);
         }

         void init_V_Vec3_Z(Vec3 eye, boolean mayMove) {
            List<Entity> others = new ArrayList<>();

            for (Entity e : mc.level.entitiesForRendering()) {
               if (e instanceof Blaze && e != this.target && e.isAlive() && e.distanceToSqr(eye) < 400.0) {
                  others.add(e);
               }
            }

            this.othersInSight = this.countSeenBy(eye, others);
            int charged = 0;

            for (Entity o : others) {
               if (o.isOnFire() && this.hasClearLine(o.getEyePosition(), eye)) {
                  charged++;
               }
            }

            this.chargedOthersInSight = charged;
            if (this.othersInSight != 0 && mayMove && mc.player.onGround()) {
               int best = this.othersInSight;
               Vec3 targetPoint = this.target.getBoundingBox().getCenter();

               for (int k = 0; k < 8; k++) {
                  double a = k * 3.141592653589793 / 4.0;
                  double dx = Math.cos(a) * 1.3;
                  double dz = Math.sin(a) * 1.3;
                  Vec3 at = eye.add(dx, 0.0, dz);
                  double h = Math.hypot(this.target.getX() - at.x, this.target.getZ() - at.z);
                  if (!(h < 1.4) && !(h > 3.4) && this.hasClearLine(at, targetPoint)) {
                     int[] keys = this.movementKeysFor(dx, dz);
                     if (this.init_Z_I_I(keys[0], keys[1])) {
                        int e = this.countSeenBy(at, others);
                        if (e < best) {
                           best = e;
                           this.moveDx = dx;
                           this.moveDz = dz;
                           this.sideStepTicks = 6;
                        }
                     }
                  }
               }
            } else {
               this.sideStepTicks = 0;
            }
         }

         int countSeenBy(Vec3 at, List<Entity> others) {
            int n = 0;

            for (Entity o : others) {
               if (this.hasClearLine(o.getEyePosition(), at)) {
                  n++;
               }
            }

            return n;
         }

         boolean hasClearLine(Vec3 from, Vec3 to) {
            return mc.level.clip(new ClipContext(from, to, Block.COLLIDER, Fluid.NONE, mc.player)).getType() == Type.MISS;
         }

         int[] movementKeysFor(double dx, double dz) {
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
                  && AiThreats.isMeleeThreat(AiThreats.classify(e))
                  && AiThreats.isActiveThreat(mc, e)) {
                  double gap = AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox());
                  if (gap < bestGap) {
                     bestGap = gap;
                     best = e;
                  }
               }
            }

            return best;
         }

         int strafeSideFor(AiProjectiles.IncomingProjectile t) {
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
               return this.strafeDirection == 0 ? 1 : this.strafeDirection;
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
               AABB end = autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, forward, strafe, 6);
               if (end == null) {
                  return false;
               }

               if (forward == 0 && strafe == 0) {
                  return true;
               }

               AABB core = end.deflate(0.2, 0.0, 0.2);
               return !mc.level.noCollision(new AABB(core.minX, core.minY - 0.55, core.minZ, core.maxX, core.minY - 0.01, core.maxZ));
            } else {
               if (!autismclient.ai.engine.AiMotionSafety.isPlayerMoveSafe(forward, strafe, false, false)) {
                  return false;
               }

               if (forward == 0 && strafe == 0) {
                  return true;
               }

               if (mc.player.onGround() && !mc.player.isInWater() && autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, forward, strafe, 6) == null) {
                  return false;
               }

               if (!autismclient.ai.engine.AiMotionSafety.isPlayerMoveSafe(forward, strafe, false, false)) {
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

         boolean isStandingSafe() {
            return AiBlazeFight.isLoadedAndLavaFree(mc.level, mc.player.blockPosition(), p -> AiTerrain.isLoaded(mc.level, p));
         }

         boolean update_Z() {
            return this.target != null
               && this.target.isAlive()
               && AiBlazeFight.canStrikeFrom(
                  this.target.getBoundingBox(),
                  mc.player.getEyePosition(),
                  AiDefense.attackReach(mc) - 0.05,
                  mc.player.hasLineOfSight(this.target),
                  this.read_Z_2(),
                  this.open_I < 4 && mc.player.onGround() && this.isStandingSafe()
               );
         }

         void selectBestWeapon() {
            if (this.tickCount % 10 == 1) {
               int slot = AiItems.bestWeaponSlot(36);
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  AiItems.selectOrMoveToHotbar(slot);
               }
            }
         }

         void startUsingItem() {
            if (!this.init_Z) {
               this.init_Z = true;
               mc.options.keySprint.setDown(false);
               mc.options.keyUse.setDown(true);
            }
         }

         void stopUsingItem() {
            if (this.init_Z) {
               this.init_Z = false;
               mc.options.keyUse.setDown(false);
               if (mc.player.isUsingItem()) {
                  mc.gameMode.releaseUsingItem(mc.player);
               }
            }
         }

         void releaseMovementKeys() {
            for (KeyMapping key : new KeyMapping[]{
               mc.options.keyUp, mc.options.keyDown, mc.options.keyLeft, mc.options.keyRight, mc.options.keyJump, mc.options.keySprint
            }) {
               key.setDown(false);
            }
         }

         void cancelChild() {
            if (this.child != null) {
               this.flush_Z = this.flush_Z | this.preparation.requiresReconciliation(this.child);
               this.preparation.cancelChild(this.child);
            }

            this.child = null;
            this.movementMode = "";
         }

         AiOperation.Result running(String message) {
            JsonObject data = AiJson.objectOf(
               "kills",
               this.kills,
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
               this.othersInSight,
               "targetSwitches",
               this.retargetCount,
               "selectionReason",
               this.lastRetargetReason,
               "availability",
               this.init_JsonObject_21(),
               "pendingChangesNeedObservation",
               this.flush_Z,
               "pillars",
               this.open_I,
               "travel",
               this.movementMode,
               "detour",
               this.defensive,
               "firesExtinguished",
               this.fireControl.firesExtinguished(),
               "knockStance",
               this.aimPoint != null,
               "knockGrace",
               this.postHitGrace,
               "targetGround",
               this.close_Z,
               "positioning",
               "stable-footing; cover only between swings; no routine circling"
            );
            data.add("spawnerSetup", this.evidence);
            if (this.target != null) {
               data.add(
                  "target",
                  AiJson.objectOf(
                     "id",
                     this.target.getId(),
                     "uuid",
                     this.target.getUUID().toString(),
                     "x",
                     this.target.getX(),
                     "y",
                     this.target.getY(),
                     "z",
                     this.target.getZ(),
                     "alive",
                     this.target.isAlive(),
                     "health",
                     this.target instanceof LivingEntity living ? living.getHealth() : -1
                  )
               );
            }

            return AiGameOperations.result(AiOperation.Status.RUNNING, message, data);
         }

         AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status status, String message) {
            boolean pending = this.requiresReconciliationAfterEmergency();
            this.sync_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            this.cancelChild();
            this.releaseMovementKeys();
            this.stopUsingItem();
            this.fireControl.reset();
            if (this.arena != null) {
               this.arena.cancel();
               this.arena = null;
            }

            this.aim.clear();
            AiBlazeFight.init_J = 0L;
            return this.preparation
               .finalizeResult(
                  AiGameOperations.result(
                     status,
                     message + " (" + this.read_I + " swings, " + this.write_I + " crits, " + this.update_I + " shield raises)",
                     AiJson.objectOf(
                        "kills",
                        this.kills,
                        "targetSwitches",
                        this.retargetCount,
                        "selectionReason",
                        this.lastRetargetReason,
                        "availability",
                        this.init_JsonObject_21(),
                        "verifiedDeathTypes",
                        this.deaths.killedTypes(),
                        "swings",
                        this.read_I,
                        "crits",
                        this.write_I,
                        "shieldRaises",
                        this.update_I,
                        "firesExtinguished",
                        this.fireControl.firesExtinguished(),
                        "verifiedBlocks",
                        "not_observable",
                        "requestedKills",
                        count,
                        "retreated",
                        this.update_Z,
                        "spawnerSetup",
                        this.evidence,
                        "positioning",
                        "stable-footing; cover only between swings; no routine circling"
                     )
                  ),
                  pending
               );
         }

         @Override
         public boolean canInterruptNow() {
            if (this.flush_Z || this.preparation.requiresReconciliation(this.child)) {
               return false;
            }

            if (this.init_AiOperation != null && !this.init_AiOperation.canInterruptNow()) {
               return false;
            }

            if (!this.fireControl.canInterrupt()) {
               return false;
            }

            if (this.arena != null && !this.arena.canInterruptNow()) {
               return false;
            }

            if (this.child != null && !this.child.canInterruptNow()) {
               return false;
            }

            if (this.target != null && mc.player != null) {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof Monster m && !(e instanceof Blaze) && m.isAlive() && m.distanceToSqr(mc.player) < 9.0 && AiThreats.isActiveThreat(mc, m)) {
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
               || this.preparation.requiresReconciliation(this.child)
               || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
               || this.fireControl.isBusy()
               || this.arena != null && this.arena.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_AiOperation_Result_AiOperation_Status_Str_5(AiOperation.Status.CANCELLED, "cancelled");
         }
      };
   }

   static List<AiProjectiles.IncomingProjectile> incomingProjectiles(Minecraft mc) {
      List<AiProjectiles.IncomingProjectile> out = new ArrayList<>();

      for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(mc)) {
         if (AiProjectiles.isImminent(t)) {
            out.add(t);
         }
      }

      return out;
   }
}
