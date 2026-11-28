package autismclient.ai;

import autismclient.ai.engine.AiGoto;
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

final class AiDragon {
   static final double init_D = 3.0;
   static final double load_D = 7.0;
   static final int[][] RETREAT_OFFSETS = new int[][]{{4, 0}, {4, 1}, {4, -1}, {3, 1}, {3, -1}, {3, 2}, {3, -2}, {4, 2}, {4, -2}, {3, 0}};
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

   private AiDragon() {
   }

   static boolean needsHealthRecovery(float health, boolean bedPending, boolean recoveryActive, boolean childCommitted) {
      return health < 10.0F && !bedPending && !recoveryActive && !childCommitted;
   }

   static boolean isCountableClearFailure(AiFailure failure, String message) {
      return failure != AiFailure.MISSING_MATERIALS && (message == null || !message.contains("dragon near the towers"));
   }

   static boolean shouldSwitchToBeds(int failedClears, int usableBeds) {
      return failedClears >= 2 && usableBeds >= 4;
   }

   static int endStoneTarget(int endStoneHeld, int shortage) {
      return endStoneHeld + (int)Math.min(96L, Math.max(1L, shortage) + 8L);
   }

   static int missingDirt(JsonObject evidence) {
      return evidence.has("missing") && evidence.get("missing").isJsonObject()
         ? Math.max(0, AiJson.getInt(evidence.getAsJsonObject("missing"), "minecraft:dirt", 0))
         : 0;
   }

   static AiDragon.DragonState stateFromPhase(String phaseName) {
      AiDragon.DragonState var10000;
      switch (phaseName) {
         case "Landing":
         case "SittingScanning":
         case "SittingFlaming":
         case "SittingAttacking":
            var10000 = AiDragon.DragonState.PERCHED;
            break;
         case "LandingApproach":
            var10000 = AiDragon.DragonState.APPROACHING;
            break;
         case "Dying":
            var10000 = AiDragon.DragonState.DYING;
            break;
         default:
            var10000 = AiDragon.DragonState.FLYING;
      }

      return var10000;
   }

   static AiDragon.DragonState stateFromMotion(double horizontal, double dy, double speed) {
      if (horizontal <= 8.0 && dy >= -2.0 && dy <= 10.0 && speed < 2.0) {
         return AiDragon.DragonState.PERCHED;
      } else {
         return horizontal <= 18.0 && dy >= -2.0 && dy <= 25.0 && speed < 8.0 ? AiDragon.DragonState.APPROACHING : AiDragon.DragonState.FLYING;
      }
   }

   static String pickAttack(boolean crystalsRemain, AiDragon.DragonState perch, boolean armed, boolean window, boolean canBed, boolean meleeOk, boolean bowOk) {
      if (crystalsRemain && !canBed) {
         return "crystals";
      }

      if (armed) {
         return window ? "detonate" : "arm_wait";
      }

      if (perch == AiDragon.DragonState.DYING) {
         return "wait";
      }

      if (!canBed || perch != AiDragon.DragonState.PERCHED && perch != AiDragon.DragonState.APPROACHING) {
         if (perch == AiDragon.DragonState.PERCHED && meleeOk) {
            return "melee";
         } else if (crystalsRemain && canBed) {
            return "wait";
         } else {
            return perch == AiDragon.DragonState.FLYING && bowOk ? "bow" : "wait";
         }
      } else {
         return "bed";
      }
   }

   static boolean isHeadInBedRange(boolean perched, double headDistanceToBed) {
      return headDistanceToBed <= 3.5 ? true : perched && headDistanceToBed <= 5.5;
   }

   static int[] radialPoint(int fx, int fz, double px, double pz, double radius) {
      double dx = px - (fx + 0.5);
      double dz = pz - (fz + 0.5);
      double len = Math.hypot(dx, dz);
      double ux = len < 1.0E-6 ? 1.0 : dx / len;
      double uz = len < 1.0E-6 ? 0.0 : dz / len;
      return new int[]{(int)Math.round(fx + ux * radius), (int)Math.round(fz + uz * radius)};
   }

   static int[] radialPoint(int fx, int fz, double px, double pz) {
      return radialPoint(fx, fz, px, pz, 3.0);
   }

   static int[] standPointToward(int bx, int bz, double px, double pz, double distance) {
      return radialPoint(bx, bz, px, pz, distance);
   }

   static List<double[]> breathClouds(Minecraft mc) {
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

   static boolean isInBreath(Minecraft mc) {
      return mc.player != null && !isClearOfClouds(mc.player.getX(), mc.player.getZ(), breathClouds(mc), -0.45);
   }

   static AiOperation escapeBreath(Minecraft mc) {
      return planBreathEscape(mc, 1.0);
   }

   static AiOperation planBreathEscape(Minecraft mc, double extra) {
      List<double[]> clouds = breathClouds(mc);
      AiDragonZones.DangerZones zones = AiDragonZones.isDragonPerched(mc) ? AiDragonZones.zones(mc) : AiDragonZones.DangerZones.NONE;
      if (clouds.isEmpty()) {
         return null;
      }

      BlockPos feet = mc.player.blockPosition();
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(feet.offset(-8, -2, -8), feet.offset(8, 2, 8))) {
         if (isClearOfClouds(p.getX() + 0.5, p.getZ() + 0.5, clouds, extra) && !zones.hits(AiDragonZones.cellBox(p.getX(), p.getY(), p.getZ()), 1.0)) {
            BlockPos below = p.below();
            if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
               && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
               && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
               && AiCombat.isSafeStep(mc, p)) {
               double d = p.distSqr(feet);
               if (d < bestD) {
                  bestD = d;
                  best = p.immutable();
               }
            }
         }
      }

      return best == null ? null : AiGuard.allowDuringLowHealth(AiGoto.travelToCompat(best, 0.35, true, true, false, 6, autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true));
   }

   static boolean isClearOfClouds(double x, double z, List<double[]> clouds) {
      return isClearOfClouds(x, z, clouds, 0.0);
   }

   static boolean isClearOfClouds(double x, double z, List<double[]> clouds, double extra) {
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

   static double[] clearSpotNear(double x, double z, List<double[]> clouds) {
      if (isClearOfClouds(x, z, clouds)) {
         return new double[]{x, z};
      }

      for (double radius : new double[]{2.0, 4.0, 6.0}) {
         for (int step = 0; step < 8; step++) {
            double angle = step * 3.141592653589793 / 4.0;
            double cx = x + Math.cos(angle) * radius;
            double cz = z + Math.sin(angle) * radius;
            if (isClearOfClouds(cx, cz, clouds)) {
               return new double[]{cx, cz};
            }
         }
      }

      return new double[]{x, z};
   }

   static boolean hasDragonWeapons(int beds, int bows, int crossbows, int arrows) {
      return beds > 0 || bows + crossbows > 0 && arrows > 0;
   }

   static boolean isBedPath(String itemPath) {
      return itemPath.endsWith("_bed");
   }

   static JsonObject missingKit() {
      JsonObject missing = new JsonObject();
      missing.addProperty("minecraft:white_bed", 1);
      missing.addProperty("minecraft:bow", 1);
      missing.addProperty("minecraft:arrow", 8);
      return missing;
   }

   static boolean isDragonDeadConfirmed(boolean statMet, boolean portalActive) {
      return statMet || portalActive;
   }

   static AiOperation create(final JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (mc.level.dimension() != Level.END) {
            throw new IllegalArgumentException("The dragon fight requires the End; beds must never be used as explosions in the Overworld");
         }

         final int bedsMax = Math.max(0, Math.min(64, AiJson.getInt(args, "beds", 0)));
         final boolean melee = AiJson.getBoolean(args, "melee", true);
         final boolean bow = AiJson.getBoolean(args, "bow", true);
         final int timeoutSeconds = Math.max(30, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 900)));
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object startLevel = mc.level;
            final Object startConnection = mc.getConnection();
            final UUID playerId = mc.player.getUUID();
            final long startDeathRevision = AiServerEvidence.deathRevision();
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final AiDragon.ChildRegistry children = new AiDragon.ChildRegistry();
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard().withoutFlee();
            boolean zeroHealthSeen;
            AiOperation currentLeg;
            AiOperation guardOperation;
            AiOperation dodgeOperation;
            AiOperation douseOperation;
            AiOperation zoneLeaveOperation;
            AiOperation chargeDodgeOperation;
            BlockPos fountain;
            BlockPos standing;
            BlockPos retreatSpot;
            BlockPos plannedBedCell;
            BlockPos bestFoot;
            BlockPos coverCell;
            EnderDragon dragon;
            AiDragon.DragonState dragonState = AiDragon.DragonState.FLYING;
            AiDragon.DragonState lastPerch = AiDragon.DragonState.FLYING;
            Inner_DataUtil phase = Inner_DataUtil.SELECT;
            String dragonPhase = "";
            String guardReason = "";
            String lastFailureCode;
            JsonObject missingItems;
            long dragonGoneSinceNanos;
            int bedsUsed;
            int bowShots;
            int meleeSwings;
            int perchCount;
            int dodges;
            int guardDecisions;
            int bowFailures;
            int unusedResetCounter;
            int meleeApproachTicks;
            int standoffFailures;
            int bedFailures;
            int zoneEscapeRepeats;
            int lastZoneEscapeTick;
            int bedPlaceRetries;
            BlockPos stand;
            int meleeState;
            float perchEndHealth = -1.0F;
            int failedClears;
            int endStoneTrips;
            int endStoneTarget;
            boolean retryableMaterials;
            boolean bedsDisabledUntilPerch;
            boolean crystalsFirst = AiJson.getBoolean(args, "crystalsFirst", this.bedCount() < 4);
            BlockPos bedStance;
            BlockPos safeStance;
            int lastNoPlanLogTick = -1000;
            boolean walkingToBedStance;
            boolean fountainLegActive;
            int coverRetries;
            int detonateWaitTicks;
            int lowDamageDetonations;
            int bedCellBlockedTicks;
            float dragonHealthAtBed;
            boolean bedClicked;
            BlockPos bedHead;
            BlockPos bedFoot;
            long headRevisionBefore;
            long footRevisionBefore;
            double bedSelfDamageEstimate;
            double bedDragonDamage;
            int selfDamageDeferrals;
            BlockPos placedCover;
            BlockPos at;
            long nextBedPlanNanos;
            final Set<Long> rejectedBedCells = new HashSet<>();
            boolean fightStarted;
            boolean weaponsChecked;
            boolean bedsUsable = true;
            boolean bowEnabled = true;
            boolean dragonDead;
            boolean cancelled;
            String lastAction = "";

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.CALM);
               this.aim.setSilent(silentAim);
            }

            @Override
            public AiOperation.Result tick() {
               AiGuard.suppressBriefly();
               if (this.cancelled || mc.player == null || mc.level == null) {
                  return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Dragon fight cancelled");
               }

               if (this.children.pendingReconciliation()) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, "Interrupted dragon interaction needs observation");
               }

               if (mc.level != this.startLevel || mc.getConnection() != this.startConnection || !mc.player.getUUID().equals(this.playerId)) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, "Dragon fight world, connection or player changed");
               }

               if (!mc.player.isAlive() || this.startDeathRevision != AiServerEvidence.deathRevision()) {
                  return this.finish(AiOperation.Status.FAILED, AiFailure.LOW_HEALTH, "Death ended this dragon fight");
               }

               if (System.nanoTime() > this.deadline) {
                  return this.finish(
                     this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                     AiFailure.TIMEOUT,
                     "Dragon fight deadline reached (" + this.bedsUsed + " beds, " + this.bowShots + " bow shots, " + this.meleeSwings + " swings)"
                  );
               }

               if (!this.weaponsChecked) {
                  this.weaponsChecked = true;
                  if (!AiDragon.hasDragonWeapons(this.bedCount(), AiGameState.countInventoryItem("minecraft:bow"), AiGameState.countInventoryItem("minecraft:crossbow"), AiRunResources.totalArrowCount()) && !this.hasWeapon()) {
                     this.missingItems = AiDragon.missingKit();
                     return this.fail(
                        AiFailure.MISSING_MATERIALS,
                        "No beds and no bow with arrows; the circling dragon cannot be punched - obtain a bed (beds explode in the End) or a bow with arrows"
                     );
                  }
               }

               EnderDragon dragon = this.findDragon();
               this.dragon = dragon;
               if (dragon == null && this.bedClicked) {
                  return this.tickDetonate();
               }

               if (dragon != null) {
                  this.fightStarted = true;
               }

               if (this.fountain == null) {
                  this.fountain = this.findFountain();
               }

               if (dragon != null) {
                  this.dragon = dragon;
                  if (dragon.getHealth() <= 0.0F) {
                     this.zeroHealthSeen = true;
                     return this.bedClicked ? this.tickDetonate() : this.tickFight();
                  }

                  this.dragonPhase = this.phaseLabel(dragon);
                  this.dragonState = this.classifyDragonState(dragon, this.dragonPhase);
                  this.notePerchState(this.dragonState);
                  if (AiDragon.needsHealthRecovery(
                     mc.player.getHealth(),
                     this.bedClicked,
                     AiGuard.isLowHealthOverrideActive(),
                     this.phase == Inner_DataUtil.CRYSTALS && this.currentLeg != null && !this.currentLeg.canInterruptNow()
                  )) {
                     return this.fail(AiFailure.LOW_HEALTH, "Health " + String.format("%.1f", mc.player.getHealth()) + ": healing before the next perch");
                  }

                  AiOperation.Result charged = this.tickChargeDodge(dragon);
                  if (charged != null) {
                     return charged;
                  }

                  AiOperation.Result zoned = this.tickZoneLeave();
                  if (zoned != null) {
                     return zoned;
                  }

                  AiOperation.Result guarded = this.tickGuard();
                  if (guarded != null) {
                     return guarded;
                  }

                  AiOperation.Result doused = this.tickDouse();
                  if (doused != null) {
                     return doused;
                  }

                  if (this.phase != Inner_DataUtil.DETONATE
                     && this.phase != Inner_DataUtil.MELEE
                     && this.phase != Inner_DataUtil.RETREAT) {
                     AiOperation.Result dodged = this.tickDodge();
                     if (dodged != null) {
                        return dodged;
                     }
                  }

                  if (this.bedClicked) {
                     return this.tickDetonate();
                  }

                  AiOperation.Result var10000;
                  switch (this.phase) {
                     case SELECT:
                        var10000 = this.chooseTactic();
                        break;
                     case CRYSTALS:
                        var10000 = this.tickCrystalClear();
                        break;
                     case CRYSTAL_STOCK:
                        var10000 = this.tickCrystalStock();
                        break;
                     case PLACE_BED:
                        var10000 = this.tickPlaceBed();
                        break;
                     case RETREAT:
                        var10000 = this.tickRetreat();
                        break;
                     case COVER:
                        var10000 = this.tickCover();
                        break;
                     case ARMED:
                        var10000 = this.tickArmed();
                        break;
                     case DETONATE:
                        var10000 = this.tickDetonate();
                        break;
                     case MELEE:
                        var10000 = this.tickMelee();
                        break;
                     case BOW:
                        var10000 = this.tickBow();
                        break;
                     case HOLD:
                        var10000 = this.tickHold();
                        break;
                     default:
                        throw new MatchException(null, null);
                  }

                  return var10000;
               } else if (this.fightStarted) {
                  return this.tickFight();
               } else {
                  return this.isPortalActive()
                     ? this.dragonDefeated("The dragon is already defeated; its active central exit is observed")
                     : this.fail(AiFailure.TARGET_NOT_FOUND, "No ender dragon is loaded and no kill is recorded; go to the End's central island");
               }
            }

            AiOperation.Result tickFight() {
               this.releaseLeg();
               boolean portal = this.isPortalActive();
               if (AiDragon.isDragonDeadConfirmed(this.zeroHealthSeen, portal)) {
                  return this.dragonDefeated("Ender dragon dead (confirmed by " + (this.zeroHealthSeen ? "observed zero health" : "the active exit portal") + ")");
               }

               if (this.dragonGoneSinceNanos == 0L) {
                  this.dragonGoneSinceNanos = System.nanoTime();
               }

               return System.nanoTime() - this.dragonGoneSinceNanos > 15000000000L
                  ? this.finish(AiOperation.Status.UNCERTAIN, null, "Dragon gone without observed zero health or an active central exit; check the fountain")
                  : this.running("Dragon gone; confirming the kill");
            }

            AiOperation.Result tickChargeDodge(EnderDragon dragon) {
               if (this.chargeDodgeOperation != null) {
                  AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.CHARGE_DODGE, this.chargeDodgeOperation);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.running("Out of the dragon's charge - " + r.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.CHARGE_DODGE, this.chargeDodgeOperation);
                  this.chargeDodgeOperation = null;
                  return this.running("Clear of the charge");
               } else if (!AiThreats.isLegacyCombatEnabled() && "ChargingPlayer".equals(this.dragonPhase)) {
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
                              && AiCombat.isSafeStep(mc, p)
                              && AiDragon.isClearOfClouds(p.getX() + 0.5, p.getZ() + 0.5, this.nearbyBreathClouds())) {
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

                     if (this.guardOperation != null) {
                        this.children.cancelChild(AiDragon.ChildRole.GUARD, this.guardOperation);
                        this.guardOperation = null;
                     }

                     this.releaseLeg();
                     this.chargeDodgeOperation = AiGoto.travelToCompat(best, 0.8, false, false, false, 5, autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true);
                     AiSpeedrun.log(
                        "[AI] dragon: charging at the player ({} off its line); stepping to {}", String.format(Locale.ROOT, "%.1f", off), best.toShortString()
                     );
                     return this.running("The dragon charges; stepping out of its line");
                  } else {
                     return null;
                  }
               } else {
                  return null;
               }
            }

            AiOperation.Result tickZoneLeave() {
               if (this.zoneLeaveOperation != null) {
                  AiOperation.Result result = this.children
                     .tickChild(AiDragon.ChildRole.ZONE_LEAVE, this.zoneLeaveOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.running("Out of the wing push - " + result.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.ZONE_LEAVE, this.zoneLeaveOperation);
                  this.zoneLeaveOperation = null;
                  return this.running("Clear of the dragon's body");
               } else {
                  if (AiThreats.isLegacyCombatEnabled()) {
                     return null;
                  }

                  boolean inZone = AiDragonZones.isDragonPerched(mc) && AiDragonZones.zones(mc).hits(mc.player.getBoundingBox(), 0.6);
                  boolean clouded = this.phase != Inner_DataUtil.DETONATE && !mc.player.isInWater() && AiDragon.isInBreath(mc);
                  if (!inZone && !clouded) {
                     return null;
                  }

                  BlockPos at = mc.player.blockPosition();
                  this.zoneEscapeRepeats = at.equals(this.at) && mc.player.tickCount - this.lastZoneEscapeTick < 60 ? this.zoneEscapeRepeats + 1 : 0;
                  double extra = 1.0 + Math.min(4, this.zoneEscapeRepeats) * 1.5;
                  AiOperation out = inZone ? AiDragonZones.init(mc, 1.0 + extra) : AiDragon.planBreathEscape(mc, extra);
                  if (out == null && inZone && clouded) {
                     out = AiDragon.planBreathEscape(mc, extra);
                  }

                  if (out == null && clouded) {
                     out = AiDragonZones.init_AiOperation(mc);
                  }

                  if (out == null) {
                     return null;
                  }

                  this.at = at;
                  this.lastZoneEscapeTick = mc.player.tickCount;
                  if (this.phase == Inner_DataUtil.PLACE_BED && this.plannedBedCell != null && ++this.bedPlaceRetries >= 2) {
                     this.rejectedBedCells.add(this.plannedBedCell.asLong());
                     this.plannedBedCell = null;
                     this.bedStance = null;
                     this.bestFoot = null;
                     this.nextBedPlanNanos = System.nanoTime() + 1000000000L;
                  }

                  if (this.guardOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.GUARD, this.guardOperation);
                     this.guardOperation = null;
                  }

                  if (this.douseOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.DOUSE, this.douseOperation);
                     this.douseOperation = null;
                  }

                  this.releaseLeg();
                  if (this.phase == Inner_DataUtil.DETONATE && !this.bedClicked) {
                     this.phase = Inner_DataUtil.SELECT;
                  }

                  this.zoneLeaveOperation = out;
                  AiSpeedrun.log(
                     "[AI] dragon: inside the wing push or the breath at {} ({}); stepping out",
                     mc.player.blockPosition().toShortString(),
                     this.phase
                  );
                  return this.running("Inside the dragon's wing push or breath; stepping out");
               }
            }

            AiOperation.Result tickGuard() {
               if (this.guardOperation != null) {
                  AiOperation.Result result = this.children
                     .tickChild(AiDragon.ChildRole.GUARD, this.guardOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.running("Interrupted: " + this.guardReason + " - " + result.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.GUARD, this.guardOperation);
                  this.guardOperation = null;
                  return this.running("Resuming after " + this.guardReason);
               } else {
                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  if (!this.canInterruptNow()) {
                     return null;
                  }

                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.guardReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                  this.guardDecisions++;
                  this.releaseLeg();
                  this.guardOperation = action;
                  return this.running("Interrupted: " + this.guardReason);
               }
            }

            AiOperation.Result tickDouse() {
               if (this.douseOperation != null) {
                  AiOperation.Result result = this.children
                     .tickChild(AiDragon.ChildRole.DOUSE, this.douseOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.running("Putting the fire out - " + result.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.DOUSE, this.douseOperation);
                  this.douseOperation = null;
                  return this.running("Fire handled");
               } else if (this.phase != Inner_DataUtil.DETONATE && !mc.player.isInWater()) {
                  if (AiDragon.isInBreath(mc)) {
                     AiOperation out = AiDragon.escapeBreath(mc);
                     if (out != null) {
                        this.releaseLeg();
                        this.douseOperation = out;
                        return this.running("In the dragon's breath; out of it");
                     }
                  }

                  boolean inFire = AiEndRun.init_Z(mc);
                  if (!inFire && !mc.player.isOnFire()) {
                     return null;
                  }

                  AiOperation op = AiEndRun.douseFire(mc);
                  if (op == null && inFire) {
                     op = AiEndRun.load_AiOperation(mc);
                  }

                  if (op == null) {
                     return null;
                  }

                  this.releaseLeg();
                  this.douseOperation = op;
                  return this.running("On fire; putting it out");
               } else {
                  return null;
               }
            }

            AiOperation.Result tickDodge() {
               if (this.dodgeOperation != null) {
                  AiOperation.Result result = this.children
                     .tickChild(AiDragon.ChildRole.DODGE, this.dodgeOperation);
                  if (result.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, result.message());
                  }

                  if (!result.terminal()) {
                     return this.running("Dodging a fireball - " + result.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.DODGE, this.dodgeOperation);
                  this.dodgeOperation = null;
                  return this.running("Resuming after the dodge");
               } else {
                  boolean threat = false;

                  for (AiProjectiles.IncomingProjectile tracked : AiProjectiles.incoming(mc)) {
                     if (AiProjectiles.isImminent(tracked)) {
                        threat = true;
                        break;
                     }
                  }

                  if (!threat) {
                     return null;
                  }

                  this.releaseLeg();
                  this.dodges++;
                  this.dodgeOperation = AiDodge.dodge(AiJson.objectOf("deflect", true, "timeoutSeconds", 4));
                  return this.running("Dragon fireball inbound; dodging");
               }
            }

            AiOperation.Result chooseTactic() {
               if (this.fountain == null) {
                  this.fountain = this.findFountain();
               }

               if (!this.bedsUsable && !this.bedClicked) {
                  this.standing = null;
                  this.placedCover = null;
               }

               boolean finishable = this.dragon != null && this.dragon.getHealth() <= 90.0F;
               boolean canBed = this.canUseBeds() && (!this.crystalsFirst || !this.crystalsRemain() || finishable);
               boolean meleeOk = melee && this.fountain != null;
               boolean bowReady = bow && this.bowEnabled && this.hasRangedWeapon();
               boolean window = this.standing != null && this.headInBedRange();
               String action = AiDragon.pickAttack(this.crystalsRemain(), this.dragonState, this.standing != null, window, canBed, meleeOk, bowReady);
               if (this.crystalsRemain() && this.dragon != null && this.dragon.getHealth() > 90.0F && !"detonate".equals(action)) {
                  action = "crystals";
               }

               if (!action.equals(this.lastAction)) {
                  AiSpeedrun.log(
                     "[AI] dragon: {} (perch {}, phase {}, health {}, beds {} usable {}, fountain {}, armed {}, bow {})",
                     action,
                     this.dragonState,
                     this.dragonPhase,
                     this.dragon == null ? "?" : String.format(Locale.ROOT, "%.0f", this.dragon.getHealth()),
                     this.bedCount(),
                     this.bedsUsable,
                     this.fountain == null ? "unknown" : this.fountain.toShortString(),
                     this.standing != null,
                     bowReady
                  );
                  this.lastAction = action;
               }

               AiOperation.Result var10000;
               switch (action) {
                  case "crystals":
                     this.phase = Inner_DataUtil.CRYSTALS;
                     var10000 = this.running("End crystals remain; clearing them first");
                     break;
                  case "detonate":
                     this.phase = Inner_DataUtil.DETONATE;
                     var10000 = this.running("The head is over the fountain; detonating the bed");
                     break;
                  case "arm_wait":
                     this.phase = Inner_DataUtil.ARMED;
                     var10000 = this.running("Bed armed; holding for the head");
                     break;
                  case "bed":
                     this.phase = Inner_DataUtil.PLACE_BED;
                     var10000 = this.running(
                        this.dragonState == AiDragon.DragonState.APPROACHING ? "Landing approach; pre-placing a bed" : "Perch window; placing a bed"
                     );
                     break;
                  case "melee":
                     this.phase = Inner_DataUtil.MELEE;
                     this.meleeApproachTicks = 0;
                     var10000 = this.running("Meleeing the perched dragon");
                     break;
                  case "bow":
                     this.phase = Inner_DataUtil.BOW;
                     var10000 = this.running("Chipping the circling dragon with the bow");
                     break;
                  default:
                     this.phase = Inner_DataUtil.HOLD;
                     var10000 = this.running("Holding by the fountain for the next perch");
               }

               return var10000;
            }

            AiOperation.Result tickCrystalClear() {
               if (!this.crystalsFirst
                  || this.dragonState != AiDragon.DragonState.PERCHED
                  || !this.bedsUsable
                  || this.bedCount() <= 0
                  || this.dragon == null
                  || !(this.dragon.getHealth() <= 90.0F)
                  || this.currentLeg != null && !this.currentLeg.canInterruptNow()) {
                  if (this.currentLeg == null) {
                     if (this.secondsLeft() < 35) {
                        return this.fail(AiFailure.TIMEOUT, "Too little time left to clear the remaining crystals");
                     }

                     this.currentLeg = AiCrystals.create(AiJson.objectOf("timeoutSeconds", this.clampSeconds(600), "skipCaged", !AiThreats.isLegacyCombatEnabled()));
                  }

                  AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.running("Clearing crystals: " + r.message());
                  }

                  this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  this.currentLeg = null;
                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Dragon fight cancelled");
                  }

                  if (r.failure() == AiFailure.MISSING_MATERIALS && !AiThreats.isLegacyCombatEnabled()) {
                     this.absorbChildData(r);
                     int shortage = AiDragon.missingDirt(AiRunResources.parseEvidenceJson(r.dataJson()));
                     if (shortage > 0 && this.endStoneTrips < 3 && this.secondsLeft() >= 45) {
                        this.endStoneTrips++;
                        this.endStoneTarget = AiDragon.endStoneTarget(AiGameState.countInventoryItem("minecraft:end_stone"), shortage);
                        this.phase = Inner_DataUtil.CRYSTAL_STOCK;
                        return this.running("Gathering nearby End stone for the remaining crystal towers");
                     } else {
                        this.retryableMaterials = true;
                        return this.finish(
                           AiOperation.Status.FAILED, AiFailure.MISSING_MATERIALS, "Crystal construction remains short; bounded local recovery is unavailable: " + r.message()
                        );
                     }
                  } else if ((r.status() == AiOperation.Status.FAILED || r.status() == AiOperation.Status.TIMED_OUT) && !AiThreats.isLegacyCombatEnabled() && !this.hasLiveCrystal()) {
                     if (mc.player.getHealth() < 16.0F) {
                        return this.fail(AiFailure.LOW_HEALTH, "Health " + String.format("%.1f", mc.player.getHealth()) + ": healing before the caged crystals");
                     }

                     if (AiDragon.isCountableClearFailure(r.failure(), r.message())) {
                        this.failedClears++;
                     }

                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Open crystals cleared; the caged wait");
                  } else if (r.status() != AiOperation.Status.FAILED && r.status() != AiOperation.Status.TIMED_OUT) {
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Crystals cleared; engaging the dragon");
                  } else {
                     this.absorbChildData(r);
                     AiSpeedrun.log(
                        "[AI] dragon: crystal outcomes {}",
                        String.valueOf(JsonParser.parseString(r.dataJson() == null ? "{}" : r.dataJson()).getAsJsonObject().get("outcomes"))
                     );
                     return this.finish(r.status(), r.failure(), "Crystal clearing failed: " + r.message());
                  }
               } else {
                  this.releaseLeg();
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("It sits; beds before the remaining crystals");
               }
            }

            AiOperation.Result tickCrystalStock() {
               if (this.currentLeg == null) {
                  this.currentLeg = autismclient.ai.engine.AiBlockMine.create(
                     AiJson.objectOf(
                        "block",
                        "minecraft:end_stone",
                        "drop",
                        "minecraft:end_stone",
                        "held",
                        this.endStoneTarget,
                        "radius",
                        16,
                        "explore",
                        false,
                        "toolPrepared",
                        true,
                        "timeoutSeconds",
                        this.clampSeconds(90)
                     )
                  );
               }

               AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
               if (!r.terminal()) {
                  return this.running("Gathering nearby crystal construction blocks: " + r.message());
               } else {
                  this.releaseLeg();
                  if (r.status() == AiOperation.Status.UNCERTAIN || r.status() == AiOperation.Status.CANCELLED) {
                     return this.finish(r.status(), r.failure(), r.message());
                  } else if (r.status() == AiOperation.Status.SUCCEEDED && AiGameState.countInventoryItem("minecraft:end_stone") >= this.endStoneTarget) {
                     this.missingItems = null;
                     this.lastFailureCode = null;
                     this.retryableMaterials = false;
                     this.phase = Inner_DataUtil.CRYSTALS;
                     return this.running("Crystal construction replenished; retrying the remaining towers");
                  } else {
                     this.retryableMaterials = true;
                     return this.finish(AiOperation.Status.FAILED, AiFailure.MISSING_MATERIALS, "Nearby crystal construction recovery did not finish: " + r.message());
                  }
               }
            }

            AiOperation.Result tickPlaceBed() {
               if (this.dragonState != AiDragon.DragonState.PERCHED && this.dragonState != AiDragon.DragonState.APPROACHING) {
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("The perch window closed; re-planning");
               }

               EnderDragon sitter = AiDragonZones.perchedDragon(mc);
               double fromFountain = this.fountain == null
                  ? 0.0
                  : Math.hypot(mc.player.getX() - this.fountain.getX() - 0.5, mc.player.getZ() - this.fountain.getZ() - 0.5);
               boolean seated = sitter != null && sitter.getPhaseManager().getCurrentPhase().isSitting();
               if (this.fountain != null && (fromFountain > 16.0 || fromFountain < 11.0 && !seated)) {
                  if (this.currentLeg == null || !this.fountainLegActive) {
                     BlockPos near = this.fountainRingStance();
                     if (near != null) {
                        if (this.currentLeg != null) {
                           this.releaseLeg();
                        }

                        this.currentLeg = AiGoto.travelToCompat(
                           near, 1.5, false, true, true, this.clampSeconds(20), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                        );
                        this.fountainLegActive = true;
                     }
                  }

                  if (this.currentLeg != null && this.fountainLegActive) {
                     AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.running("Closing in on the perch");
                     }

                     this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                     this.currentLeg = null;
                     this.fountainLegActive = false;
                  }
               }

               if (sitter != null && sitter.getPhaseManager().getCurrentPhase().isSitting()) {
                  if (this.currentLeg == null && this.standing == null) {
                     BlockPos standing = this.findStanding();
                     if (standing != null) {
                        this.standing = standing;
                        this.bestFoot = null;
                        this.plannedBedCell = null;
                        this.bedStance = null;
                        this.phase = Inner_DataUtil.RETREAT;
                        AiSpeedrun.log("[AI] dragon: a bed already stands at {}; arming it", standing.toShortString());
                        return this.running("A bed already stands by the fountain; backing out of the blast");
                     }
                  }

                  if (this.plannedBedCell == null) {
                     if (System.nanoTime() < this.nextBedPlanNanos) {
                        return this.running("Waiting for the dragon to turn; no bed cell clear of its wings yet");
                     }

                     this.plannedBedCell = AiDragonZones.isDragonPerched(mc) ? this.perchedBedCell() : this.fountainBedCell();
                     this.bedPlaceRetries = 0;
                     if (this.plannedBedCell == null && AiDragonZones.isDragonPerched(mc)) {
                        this.nextBedPlanNanos = System.nanoTime() + 500000000L;
                        return this.running("Waiting for the dragon to turn; no bed cell clear of its wings yet");
                     }

                     if (this.plannedBedCell == null) {
                        this.bedsUsable = false;
                        AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "No placeable bed cell by the fountain; falling back to melee and bow");
                        this.phase = Inner_DataUtil.SELECT;
                        return this.running("No placeable bed cell by the fountain; falling back to melee and bow");
                     }
                  }

                  if (this.currentLeg == null) {
                     String bedId = this.bedItemId();
                     if (bedId == null || AiItems.equipHotbar(AiItems.resolveItem(bedId)) < 0) {
                        this.plannedBedCell = null;
                        this.bestFoot = null;
                        this.bedsUsable = false;
                        AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "No bed can be brought to hand; falling back to melee and bow");
                        this.phase = Inner_DataUtil.SELECT;
                        return this.running("No bed can be brought to hand; falling back to melee and bow");
                     }

                     if (this.bestFoot != null && this.cellBlockedByEntity(this.bestFoot)) {
                        if (++this.bedCellBlockedTicks > 20) {
                           this.bedCellBlockedTicks = 0;
                           if (this.plannedBedCell != null) {
                              this.rejectedBedCells.add(this.plannedBedCell.asLong());
                           }

                           this.plannedBedCell = null;
                           this.bedStance = null;
                           this.bestFoot = null;
                           this.nextBedPlanNanos = 0L;
                           return this.running("Dragon body blocks that bed cell; choosing another");
                        }

                        return this.running("Waiting for the dragon's body to clear the bed cell");
                     }

                     this.bedCellBlockedTicks = 0;
                     if (this.bestFoot != null && this.isBedAt(this.bestFoot)) {
                        this.standing = this.bestFoot;
                        this.plannedBedCell = null;
                        this.bedStance = null;
                        this.bestFoot = null;
                        this.placedCover = null;
                        this.phase = Inner_DataUtil.RETREAT;
                        return this.running("A bed already stands in the planned cell; arming it");
                     }

                     if (this.bedStance != null && mc.player.position().distanceToSqr(Vec3.atBottomCenterOf(this.bedStance)) > 0.48999999999999994) {
                        this.currentLeg = AiGoto.travelToCompat(
                           this.bedStance, 0.3, false, true, true, this.clampSeconds(15), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                        );
                        this.walkingToBedStance = true;
                     } else {
                        this.currentLeg = AiGameOperations.blockAction(
                           "place",
                           AiJson.objectOf("x", this.plannedBedCell.getX(), "y", this.plannedBedCell.getY(), "z", this.plannedBedCell.getZ())
                        );
                        this.walkingToBedStance = false;
                     }
                  }

                  AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  if (!r.terminal()) {
                     return this.running(this.walkingToBedStance ? "To the bed stance clear of the dragon" : "Placing the bed beside the fountain");
                  }

                  this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  this.currentLeg = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     if (this.bestFoot != null && this.isBedAt(this.bestFoot)) {
                        this.standing = this.bestFoot;
                        this.bestFoot = null;
                        this.bedsUsed++;
                        this.phase = Inner_DataUtil.RETREAT;
                        return this.running("Bed appeared unconfirmed; arming it rather than replacing it");
                     }

                     BlockPos missed = this.plannedBedCell;
                     if (missed != null) {
                        this.rejectedBedCells.add(missed.asLong());
                     }

                     this.plannedBedCell = null;
                     this.bedStance = null;
                     this.bestFoot = null;
                     this.nextBedPlanNanos = 0L;
                     this.bedFailures++;
                     this.phase = Inner_DataUtil.SELECT;
                     if (this.bedFailures < 3) {
                        return this.running("Bed click unconfirmed; choosing another cell");
                     }

                     this.bedsUsable = false;
                     this.bedsDisabledUntilPerch = true;
                     AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "three unconfirmed bed clicks (until the next perch)");
                     return this.running("Bed click unconfirmed three times; falling back to melee and bow");
                  } else {
                     if (this.walkingToBedStance) {
                        this.walkingToBedStance = false;
                        this.bedStance = null;
                        return this.running("At the bed stance");
                     }

                     BlockPos triedSupport = this.plannedBedCell;
                     this.plannedBedCell = null;
                     if (r.status() != AiOperation.Status.SUCCEEDED && (this.bestFoot == null || !this.isBedAt(this.bestFoot))) {
                        this.bestFoot = null;
                        this.bedFailures++;
                        if (triedSupport != null) {
                           this.rejectedBedCells.add(triedSupport.asLong());
                        }

                        AiSpeedrun.log(
                           "[AI] dragon: bed placement failed at {} ({}: {}), failure {}",
                           triedSupport == null ? "?" : triedSupport.toShortString(),
                           r.status(),
                           r.message(),
                           this.bedFailures
                        );
                        this.phase = Inner_DataUtil.SELECT;
                        if (this.bedFailures < 3) {
                           return this.running("Bed placement failed (" + r.message() + "); trying another cell");
                        }

                        this.bedsUsable = false;
                        this.bedsDisabledUntilPerch = true;
                        AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "three bed placements failed (until the next perch)");
                        return this.running("Bed placement failed (" + r.message() + "); falling back to melee and bow");
                     } else {
                        this.standing = this.bestFoot;
                        this.placedCover = null;
                        this.bestFoot = null;
                        this.bedsUsed++;
                        this.phase = Inner_DataUtil.RETREAT;
                        return this.running("Bed armed; backing out of the blast");
                     }
                  }
               } else {
                  if (this.currentLeg != null && !this.fountainLegActive) {
                     this.releaseLeg();
                  }

                  this.plannedBedCell = null;
                  this.bedStance = null;
                  return this.running("Waiting for the dragon to sit");
               }
            }

            AiOperation.Result tickRetreat() {
               if (this.retreatSpot == null) {
                  this.coverCell = null;
                  BlockState bedState = mc.level.getBlockState(this.standing);
                  if (bedState.hasProperty(BedBlock.FACING)) {
                     Direction back = bedState.getValue(BedBlock.FACING).getOpposite();
                     Direction side = back.getClockWise();
                     AiDragonZones.DangerZones zones = AiDragonZones.zones(mc);
                     double[] breath = AiDragonZones.breath(mc);
                     List<double[]> lying = this.nearbyBreathClouds();
                     if (this.safeStance != null
                        && AiDragonZones.isSafeCell(zones, breath, lying, this.safeStance.getX(), this.safeStance.getY(), this.safeStance.getZ())) {
                        this.retreatSpot = this.safeStance;
                        int dx = this.standing.getX() - this.safeStance.getX();
                        int dz = this.standing.getZ() - this.safeStance.getZ();
                        this.coverCell = Math.abs(dx) >= Math.abs(dz)
                           ? new BlockPos(this.safeStance.getX() + Integer.signum(dx), this.safeStance.getY(), this.safeStance.getZ())
                           : new BlockPos(this.safeStance.getX(), this.safeStance.getY(), this.safeStance.getZ() + Integer.signum(dz));
                     }

                     for (int[] o : this.retreatSpot != null ? new int[0][] : AiDragon.RETREAT_OFFSETS) {
                        int x = this.standing.getX() + back.getStepX() * o[0] + side.getStepX() * o[1];
                        int z = this.standing.getZ() + back.getStepZ() * o[0] + side.getStepZ() * o[1];
                        BlockPos support = this.groundAt(x, z);
                        if (support != null
                           && Math.abs(support.getY() + 1 - this.standing.getY()) <= 1
                           && AiDragonZones.isSafeCell(zones, breath, lying, x, support.getY() + 1, z)) {
                           this.retreatSpot = support.above();
                           int dx = this.standing.getX() - x;
                           int dz = this.standing.getZ() - z;
                           this.coverCell = Math.abs(dx) >= Math.abs(dz)
                              ? new BlockPos(x + Integer.signum(dx), this.retreatSpot.getY(), z)
                              : new BlockPos(x, this.retreatSpot.getY(), z + Integer.signum(dz));
                           break;
                        }
                     }
                  }
               }

               if (this.retreatSpot == null && this.safeStance != null) {
                  this.retreatSpot = this.safeStance;
                  this.coverCell = null;
               }

               if (this.retreatSpot == null) {
                  int[] xz = AiDragon.standPointToward(
                     this.standing.getX(),
                     this.standing.getZ(),
                     mc.player.getX(),
                     mc.player.getZ(),
                     Math.max(3.5, mc.player.blockInteractionRange() - 0.4)
                  );
                  BlockPos support = this.groundAt(xz[0], xz[1]);
                  if (support == null) {
                     this.standing = null;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("No stand-off ground; abandoning the armed bed");
                  }

                  this.retreatSpot = support.above();
               }

               if (this.currentLeg == null) {
                  this.currentLeg = AiGoto.travelToCompat(
                     this.retreatSpot, 0.8, false, true, true, this.clampSeconds(30), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.running("Backing out of the blast radius");
               }

               this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
               this.currentLeg = null;
               this.retreatSpot = null;
               if (AiDanger.assess(mc).lethal()) {
                  this.standing = null;
                  this.bedsUsable = false;
                  AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "The blast at click range would be lethal; leaving beds out of the plan");
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("The blast at click range would be lethal; leaving beds out of the plan");
               }

               if (this.coverCell == null && this.standing != null) {
                  BlockPos feet = mc.player.blockPosition();
                  int dx = this.standing.getX() - feet.getX();
                  int dz = this.standing.getZ() - feet.getZ();
                  BlockPos c = Math.abs(dx) >= Math.abs(dz) ? feet.offset(Integer.signum(dx), 0, 0) : feet.offset(0, 0, Integer.signum(dz));
                  if (!c.equals(this.standing)
                     && mc.level.getBlockState(c).canBeReplaced()
                     && !mc.level.getBlockState(c.below()).getCollisionShape(mc.level, c.below()).isEmpty()) {
                     this.coverCell = c;
                  }
               }

               if (this.coverCell != null) {
                  this.phase = Inner_DataUtil.COVER;
                  return this.running("At stand-off; setting the cover block");
               } else {
                  this.phase = Inner_DataUtil.ARMED;
                  return this.running("At stand-off; waiting for the head");
               }
            }

            AiOperation.Result tickCover() {
               if (this.currentLeg == null) {
                  BlockPos support = this.coverCell.below();
                  boolean open = mc.level.getBlockState(this.coverCell).canBeReplaced()
                     && !mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty();
                  String block = this.bestPillarBlock();
                  if (!open || block == null || AiItems.equipHotbar(AiItems.resolveItem(block)) < 0) {
                     if (!mc.level.getBlockState(this.coverCell).getCollisionShape(mc.level, this.coverCell).isEmpty()) {
                        this.placedCover = this.coverCell.immutable();
                     }

                     this.coverCell = null;
                     this.phase = Inner_DataUtil.ARMED;
                     return this.running(open ? "No block to set as cover; waiting for the head" : "Cover cell taken; waiting for the head");
                  }

                  this.currentLeg = AiGameOperations.blockAction(
                     "place", AiJson.objectOf("x", support.getX(), "y", support.getY(), "z", support.getZ(), "face", "UP", "timeoutSeconds", 6)
                  );
               }

               AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.running("Setting a block between the bed and the body");
               }

               this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
               this.currentLeg = null;
               AiSpeedrun.log("[AI] dragon: cover block in {}: {} ({})", this.coverCell.toShortString(), r.status(), AiSpeedrun.clip(r.message()));
               if (r.status() != AiOperation.Status.SUCCEEDED && ++this.coverRetries < 2 && mc.level.getBlockState(this.coverCell).canBeReplaced()) {
                  return this.running("Cover not confirmed; setting it again");
               }

               this.coverRetries = 0;
               if (!mc.level.getBlockState(this.coverCell).getCollisionShape(mc.level, this.coverCell).isEmpty()) {
                  this.placedCover = this.coverCell.immutable();
               }

               this.coverCell = null;
               this.phase = Inner_DataUtil.ARMED;
               return this.running(
                  r.status() == AiOperation.Status.SUCCEEDED ? "Cover set; waiting for the head" : "No cover (" + r.message() + "); waiting for the head"
               );
            }

            String bestPillarBlock() {
               String best = null;
               int bestRank = 2147483647;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty()
                     && AiRunResources.countUsableItem(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), 0) > autismclient.ai.engine.AiRestock.reservedItemCount(stack.getItem())) {
                     String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                     int rank = autismclient.ai.engine.AiRestock.scaffoldRank(id);
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

            AiOperation.Result tickArmed() {
               if (!this.isBedAt(this.standing)) {
                  this.standing = null;
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("The armed bed was destroyed; re-planning");
               }

               if (this.headInBedRange()) {
                  this.phase = Inner_DataUtil.DETONATE;
                  return this.running("The head is in range of the bed; detonating");
               }

               double reach = mc.player.blockInteractionRange();
               Vec3 eye = mc.player.getEyePosition();
               double distance = eye.distanceTo(
                  new Vec3(
                     Math.clamp(eye.x, this.standing.getX(), this.standing.getX() + 1.0),
                     Math.clamp(eye.y, this.standing.getY(), this.standing.getY() + 0.5625),
                     Math.clamp(eye.z, this.standing.getZ(), this.standing.getZ() + 1.0)
                  )
               );
               if (!AiDanger.assess(mc).lethal() && AiDragon.isClearOfClouds(mc.player.getX(), mc.player.getZ(), this.nearbyBreathClouds()) && !(distance > reach - 0.3)) {
                  return this.running("Bed armed; waiting for the head (" + this.dragonPhase + ")");
               }

               if (this.currentLeg == null) {
                  double[] spot = AiDragon.clearSpotNear(mc.player.getX(), mc.player.getZ(), this.nearbyBreathClouds());
                  int[] xz = AiDragon.standPointToward(this.standing.getX(), this.standing.getZ(), spot[0], spot[1], Math.max(3.5, reach - 0.4));
                  BlockPos support = this.groundAt(xz[0], xz[1]);
                  if (support == null) {
                     return this.running("Holding for the head");
                  }

                  this.currentLeg = AiGoto.travelToCompat(
                     support.above(), 0.8, false, true, true, this.clampSeconds(20), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                  );
               }

               AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
               } else if (!r.terminal()) {
                  return this.running("Holding at stand-off for the head");
               } else {
                  this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  this.currentLeg = null;
                  if (AiDanger.assess(mc).lethal()) {
                     this.standing = null;
                     this.bedsUsable = false;
                     AiSpeedrun.log("[AI] dragon: beds out of the plan: {}", "Stand-off is no longer survivable; leaving beds out of the plan");
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Stand-off is no longer survivable; leaving beds out of the plan");
                  } else if (r.status() != AiOperation.Status.SUCCEEDED && ++this.standoffFailures >= 3) {
                     this.standoffFailures = 0;
                     this.standing = null;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Cannot hold the stand-off; leaving that bed");
                  } else {
                     this.standoffFailures = 0;
                     return this.running("Repositioned at stand-off");
                  }
               }
            }

            AiOperation.Result tickDetonate() {
               if (this.standing == null) {
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("No armed bed; re-planning");
               }

               if (this.bedClicked) {
                  this.detonateWaitTicks++;
                  boolean damaged = this.dragon != null && this.dragon.getHealth() < this.dragonHealthAtBed;
                  boolean acknowledged = damaged
                     || this.bedHead != null
                        && this.bedFoot != null
                        && AiServerEvidence.blockRevision(this.bedHead) > this.headRevisionBefore
                        && AiServerEvidence.blockRevision(this.bedFoot) > this.footRevisionBefore
                        && !this.isBedAt(this.bedHead)
                        && !this.isBedAt(this.bedFoot);
                  if (this.detonateWaitTicks >= 12 && acknowledged) {
                     this.bedDragonDamage = this.dragon == null ? 0.0 : Math.max(0.0F, this.dragonHealthAtBed - this.dragon.getHealth());
                     if (this.bedDragonDamage < 1.0 && ++this.lowDamageDetonations >= 2) {
                        this.bedsUsable = false;
                        this.crystalsFirst = true;
                     } else if (this.bedDragonDamage >= 1.0) {
                        this.lowDamageDetonations = 0;
                     }

                     this.standing = null;
                     this.bedClicked = false;
                     this.unusedResetCounter = 0;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running(
                        "Bed removed; observed dragon damage "
                           + String.format(Locale.ROOT, "%.1f", this.bedDragonDamage)
                           + (this.bedsUsable ? "" : "; ineffective bed plan retired for crystals and melee")
                     );
                  } else {
                     return this.detonateWaitTicks >= 60
                        ? this.finish(AiOperation.Status.UNCERTAIN, null, "Bed use lacks confirmed server removal of both halves; no repeat click authorized")
                        : this.running("Observing server acknowledgement of both bed halves and dragon health");
                  }
               } else {
                  if (!this.isBedAt(this.standing)) {
                     this.standing = null;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Bed disappeared before use; no detonation credited");
                  }

                  BlockPos head = this.standingBedHead();
                  if (head != null && this.headInBedRange() && this.dragon.hurtTime <= 0) {
                     Vec3 blast = Vec3.atCenterOf(head);
                     BlockState state = mc.level.getBlockState(head);
                     BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
                     autismclient.util.ExplosionDamageCalculator.SimOptions options = autismclient.util.ExplosionDamageCalculator.SimOptions.DEFAULT
                        .withExcludedBlocks(List.of(head, foot))
                        .withDamageSource(autismclient.util.ExplosionDamageCalculator.bedAnchorDamageSource(mc.level, blast));
                     this.bedSelfDamageEstimate = autismclient.util.ExplosionDamageCalculator.calculateDamage(mc.player, blast, 5.0F, options);
                     double health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
                     boolean covered = this.placedCover != null
                        && mc.level
                           .getBlockState(this.placedCover)
                           .getCollisionShape(mc.level, this.placedCover)
                           .toAabbs()
                           .stream()
                           .anyMatch(box -> box.move(this.placedCover).clip(blast, mc.player.position().add(0.0, 0.4, 0.0)).isPresent());
                     double expectedSelfDamage = Math.max(this.bedSelfDamageEstimate, 8.0);
                     boolean safeBlast = (covered || this.bedSelfDamageEstimate > 0.0)
                        && health - expectedSelfDamage >= 11.5
                        && !AiDanger.assess(mc).lethal()
                        && !AiDragon.isInBreath(mc)
                        && mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse();
                     if (safeBlast) {
                        this.aim.aimAt(Vec3.atCenterOf(this.standing));
                        this.aim.tick();
                        BlockHitResult ray = autismclient.ai.engine.AiAim.raycastLookBlock();
                        if (ray == null || !ray.getBlockPos().equals(head) && !ray.getBlockPos().equals(foot)) {
                           if (++this.detonateWaitTicks > 40) {
                              this.phase = Inner_DataUtil.ARMED;
                              this.detonateWaitTicks = 0;
                           }

                           return this.running("Waiting for an unobstructed bed interaction ray");
                        } else {
                           this.dragonHealthAtBed = this.dragon.getHealth();
                           this.bedHead = head.immutable();
                           this.bedFoot = foot.immutable();
                           this.headRevisionBefore = AiServerEvidence.blockRevision(this.bedHead);
                           this.footRevisionBefore = AiServerEvidence.blockRevision(this.bedFoot);
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, ray);
                           this.bedClicked = true;
                           this.detonateWaitTicks = 0;
                           return this.running("Bed clicked; waiting for observed removal and dragon damage");
                        }
                     } else if (!AiDragon.isInBreath(mc) && !AiDanger.assess(mc).lethal()) {
                        if (this.bedSelfDamageEstimate > 0.0 && expectedSelfDamage + 11.5 > mc.player.getMaxHealth() + mc.player.getAbsorptionAmount()) {
                           if (++this.selfDamageDeferrals <= 3) {
                              this.placedCover = null;
                              this.retreatSpot = null;
                              this.phase = Inner_DataUtil.ARMED;
                              return this.running("Blast priced too strong from here; re-planning the stand and cover");
                           } else {
                              this.selfDamageDeferrals = 0;
                              this.standing = null;
                              this.placedCover = null;
                              this.phase = Inner_DataUtil.SELECT;
                              return this.running("Blast too strong from every stand tried; dropping this bed plan");
                           }
                        } else if (this.bedSelfDamageEstimate > 0.0 && health - expectedSelfDamage < 11.5) {
                           return this.fail(
                              AiFailure.LOW_HEALTH,
                              "Health " + String.format(Locale.ROOT, "%.1f", health) + " leaves too little after a bed blast; healing before the next perch"
                           );
                        } else if (!covered && this.bedSelfDamageEstimate <= 0.0) {
                           this.phase = Inner_DataUtil.ARMED;
                           return this.running("Bed armed; waiting for cover or a priced blast");
                        } else if (!mc.level.environmentAttributes().getDimensionValue(EnvironmentAttributes.BED_RULE).destroyOnUse()) {
                           this.bedsUsable = false;
                           this.crystalsFirst = true;
                           this.standing = null;
                           this.placedCover = null;
                           this.phase = Inner_DataUtil.SELECT;
                           return this.running("Beds do not explode in this dimension; use the melee/bow fallback");
                        } else {
                           this.bedsUsable = false;
                           this.crystalsFirst = true;
                           this.standing = null;
                           this.placedCover = null;
                           this.phase = Inner_DataUtil.SELECT;
                           return this.running("Bed survival/cover gate failed; use the crystal and melee fallback");
                        }
                     } else {
                        this.phase = Inner_DataUtil.ARMED;
                        return this.running("Bed armed; waiting out the breath/dragon attack before clicking");
                     }
                  } else {
                     this.phase = Inner_DataUtil.ARMED;
                     return this.running("Head/phase/damage window changed; hold the bed");
                  }
               }
            }

            BlockPos standingBedHead() {
               if (!this.isBedAt(this.standing)) {
                  return null;
               } else {
                  BlockState state = mc.level.getBlockState(this.standing);
                  BlockPos head = state.getValue(BedBlock.PART) == BedPart.HEAD
                     ? this.standing
                     : this.standing.relative(state.getValue(BedBlock.FACING));
                  BlockPos foot = head.relative(state.getValue(BedBlock.FACING).getOpposite());
                  if (AiTerrain.isLoaded(mc.level, head) && AiTerrain.isLoaded(mc.level, foot)) {
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

            AiOperation.Result tickMelee() {
               if (this.dragonState != AiDragon.DragonState.PERCHED) {
                  this.releaseLeg();
                  this.stand = null;
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("The perch ended; re-planning");
               }

               if (++this.meleeApproachTicks > 300) {
                  this.releaseLeg();
                  this.stand = null;
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("Long perch; re-evaluating");
               }

               EnderDragonPhase<?> dragonPhase = this.dragon.getPhaseManager().getCurrentPhase().getPhase();
               List<double[]> clouds = this.nearbyBreathClouds();
               AiDragonZones.DangerZones zones = AiDragonZones.zones(mc);
               boolean window = dragonPhase == EnderDragonPhase.SITTING_SCANNING || dragonPhase == EnderDragonPhase.SITTING_ATTACKING;
               if (!window) {
                  this.stand = null;
                  if (this.meleeState == 1) {
                     this.releaseLeg();
                  }

                  List<double[]> danger = new ArrayList<>(clouds);
                  double[] breath = AiDragonZones.breath(mc);
                  if (breath != null) {
                     danger.add(breath);
                  }

                  if (this.currentLeg == null && !AiDragon.isClearOfClouds(mc.player.getX(), mc.player.getZ(), danger, 1.0)) {
                     BlockPos out = this.nearestSafeCell(danger, zones);
                     if (out != null) {
                        this.currentLeg = AiGuard.allowDuringLowHealth(
                           AiGoto.travelToCompat(out, 0.35, false, false, false, this.clampSeconds(8), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true)
                        );
                        this.meleeState = 2;
                     }
                  }

                  if (this.currentLeg == null && this.meleeState == 0) {
                     BlockPos near = this.fountainRingStance();
                     if (near != null && Math.hypot(mc.player.getX() - (near.getX() + 0.5), mc.player.getZ() - (near.getZ() + 0.5)) > 2.5) {
                        this.currentLeg = AiGoto.travelToCompat(
                           near, 0.8, false, false, false, this.clampSeconds(10), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                        );
                        this.meleeState = 3;
                     }
                  }

                  if (this.currentLeg != null) {
                     AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.running(this.meleeState == 3 ? "Closing in while it lands" : "Out of the dragon's flame");
                     }

                     this.releaseLeg();
                     this.meleeState = 0;
                  }

                  return this.running("Waiting for the flame to end (" + dragonPhase + ")");
               } else {
                  if (this.meleeState == 2 || this.meleeState == 3) {
                     this.releaseLeg();
                     this.meleeState = 0;
                  }

                  if (this.stand == null || this.meleeApproachTicks % 10 == 0) {
                     BlockPos stand = this.bestHeadStand(zones, clouds);
                     if (stand == null || !stand.equals(this.stand)) {
                        this.releaseLeg();
                        this.meleeState = 0;
                     }

                     this.stand = stand;
                  }

                  if (this.stand == null) {
                     return this.running("No stand reaches the head clear of the wings");
                  }

                  this.aim.aimAt(autismclient.ai.engine.AiAim.defaultEntityAimPoint(this.dragon.head));
                  this.selectWeapon();
                  double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.1;
                  boolean inReach = this.dragon.head.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reach * reach;
                  boolean there = Math.hypot(mc.player.getX() - (this.stand.getX() + 0.5), mc.player.getZ() - (this.stand.getZ() + 0.5))
                        < 0.5
                     && Math.abs(mc.player.getY() - this.stand.getY()) < 0.6;
                  if (!there && (!inReach || !mc.player.onGround())) {
                     if (this.currentLeg == null) {
                        this.currentLeg = AiGoto.travelToCompat(
                           this.stand, 0.3, false, false, false, this.clampSeconds(10), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                        );
                        this.meleeState = 1;
                     }

                     AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                     if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                     }

                     if (!r.terminal()) {
                        return this.running("Closing on the head");
                     }

                     this.releaseLeg();
                     this.meleeState = 0;
                  }

                  this.releaseLeg();
                  this.meleeState = 0;
                  this.aim.tick();
                  if (inReach
                     && this.aim.isAimedAtEntity(this.dragon.head)
                     && mc.player.getAttackStrengthScale(0.0F) >= 0.92F
                     && AiDefense.canSwingAt(mc, this.dragon.head, this.aim)) {
                     mc.gameMode.attack(mc.player, this.dragon.head);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     mc.player.resetAttackStrengthTicker();
                     this.meleeSwings++;
                     AiSpeedrun.log(
                        "[AI] dragon: struck the head ({}); dragon health {}",
                        this.meleeSwings,
                        String.format(Locale.ROOT, "%.0f", this.dragon.getHealth())
                     );
                     return this.running("Struck the head (" + this.meleeSwings + ")");
                  } else {
                     return this.running(inReach ? "Waiting out the attack cooldown" : "Waiting for the head to come into reach");
                  }
               }
            }

            BlockPos bestHeadStand(AiDragonZones.DangerZones zones, List<double[]> clouds) {
               AABB headBox = this.dragon.head.getBoundingBox();
               double reach = Math.max(3.0, mc.player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE)) - 0.4;
               BlockPos h = this.dragon.head.blockPosition();
               BlockPos best = null;
               double bestScore = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(h.offset(-4, -4, -4), h.offset(4, 1, 4))) {
                  BlockPos below = p.below();
                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                     && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                     && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                     && !zones.hits(AiDragonZones.cellBox(p.getX(), p.getY(), p.getZ()), 0.8)
                     && AiDragon.isClearOfClouds(p.getX() + 0.5, p.getZ() + 0.5, clouds, 0.5)) {
                     Vec3 eye = new Vec3(p.getX() + 0.5, p.getY() + mc.player.getEyeHeight(), p.getZ() + 0.5);
                     if (!(headBox.distanceToSqr(eye) > reach * reach) && AiCombat.isSafeStep(mc, p)) {
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

            BlockPos nearestSafeCell(List<double[]> danger, AiDragonZones.DangerZones zones) {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(feet.offset(-8, -2, -8), feet.offset(8, 2, 8))) {
                  if (AiDragon.isClearOfClouds(p.getX() + 0.5, p.getZ() + 0.5, danger, 1.5) && !zones.hits(AiDragonZones.cellBox(p.getX(), p.getY(), p.getZ()), 1.0)) {
                     BlockPos below = p.below();
                     if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                        && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                        && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                        && AiCombat.isSafeStep(mc, p)) {
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

            AiOperation.Result tickBow() {
               if (this.dragonState == AiDragon.DragonState.PERCHED) {
                  this.releaseLeg();
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("The dragon perched; switching plans");
               }

               if (this.currentLeg == null) {
                  if (!this.hasRangedWeapon()) {
                     this.bowEnabled = false;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("Out of arrows");
                  }

                  this.currentLeg = AiRanged.create(
                     AiJson.objectOf("entityId", this.dragon.getId(), "count", 3, "timeoutSeconds", this.clampSeconds(45))
                  );
               }

               AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
               }

               if (!r.terminal()) {
                  return this.running("Shooting the circling dragon");
               }

               this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
               this.currentLeg = null;
               this.bowShots = this.bowShots + this.firedCount(r);
               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.finish(AiOperation.Status.CANCELLED, AiFailure.CANCELLED, "Dragon fight cancelled");
               }

               if (r.status() == AiOperation.Status.FAILED) {
                  if (r.dataJson() != null && r.dataJson().contains("\"missing\"")) {
                     this.absorbChildData(r);
                     this.bowEnabled = false;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("The bow is out of ammunition");
                  }

                  if (++this.bowFailures >= 3) {
                     this.bowEnabled = false;
                     this.phase = Inner_DataUtil.SELECT;
                     return this.running("The bow cannot connect from here; holding for perches");
                  }
               }

               this.phase = Inner_DataUtil.SELECT;
               return this.running("Re-evaluating");
            }

            AiOperation.Result tickHold() {
               if (this.dragonState != AiDragon.DragonState.PERCHED && this.dragonState != AiDragon.DragonState.APPROACHING && this.standing == null) {
                  if (this.currentLeg == null) {
                     int fx = this.fountain != null ? this.fountain.getX() : 0;
                     int fz = this.fountain != null ? this.fountain.getZ() : 0;
                     int[] xz = AiDragon.radialPoint(fx, fz, mc.player.getX(), mc.player.getZ(), 12.0);
                     double[] spot = AiDragon.clearSpotNear(xz[0] + 0.5, xz[1] + 0.5, this.nearbyBreathClouds());
                     BlockPos support = this.groundAt((int)Math.floor(spot[0]), (int)Math.floor(spot[1]));
                     if (support == null) {
                        return this.running("Holding position");
                     }

                     if (mc.player.distanceToSqr(support.getX() + 0.5, support.getY() + 1.0, support.getZ() + 0.5) <= 4.0) {
                        return this.running("Holding by the fountain for the next perch (" + this.dragonPhase + ")");
                     }

                     this.currentLeg = AiGoto.travelToCompat(
                        support.above(), 1.5, false, true, true, this.clampSeconds(30), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true
                     );
                  }

                  AiOperation.Result r = this.children.tickChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (!r.terminal()) {
                     return this.running("Repositioning toward the fountain");
                  }

                  this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  this.currentLeg = null;
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("Re-evaluating");
               } else {
                  this.releaseLeg();
                  this.phase = Inner_DataUtil.SELECT;
                  return this.running("Perch incoming; re-planning");
               }
            }

            EnderDragon findDragon() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EnderDragon dragon && !entity.isRemoved()) {
                     return dragon;
                  }
               }

               return null;
            }

            boolean crystalsRemain() {
               if (!AiThreats.isLegacyCombatEnabled() && !this.hasLiveCrystal()) {
                  return false;
               }

               if (!AiThreats.isLegacyCombatEnabled() && AiDragon.shouldSwitchToBeds(this.failedClears, this.usableBeds())) {
                  return this.hasLiveCrystal();
               }

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved()) {
                     return true;
                  }
               }

               return false;
            }

            boolean hasLiveCrystal() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EndCrystal && !entity.isRemoved() && !AiCrystals.init(mc, entity.blockPosition())) {
                     return true;
                  }
               }

               return false;
            }

            List<double[]> nearbyBreathClouds() {
               List<double[]> out = new ArrayList<>();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof AreaEffectCloud cloud && !entity.isRemoved()) {
                     out.add(new double[]{cloud.getX(), cloud.getZ(), cloud.getRadius()});
                  }
               }

               return out;
            }

            String phaseLabel(EnderDragon dragon) {
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

            AiDragon.DragonState classifyDragonState(EnderDragon dragon, String phaseName) {
               if (!phaseName.isEmpty()) {
                  return AiDragon.stateFromPhase(phaseName);
               }

               double fx = this.fountain != null ? this.fountain.getX() + 0.5 : 0.5;
               double fz = this.fountain != null ? this.fountain.getZ() + 0.5 : 0.5;
               double fy = this.fountain != null ? this.fountain.getY() : 64.0;
               double horizontal = Math.hypot(dragon.getX() - fx, dragon.getZ() - fz);
               return AiDragon.stateFromMotion(horizontal, dragon.getY() - fy, dragon.getDeltaMovement().length() * 20.0);
            }

            void notePerchState(AiDragon.DragonState perch) {
               if (perch == AiDragon.DragonState.PERCHED && this.lastPerch != AiDragon.DragonState.PERCHED) {
                  this.perchCount++;
                  if (this.bedsDisabledUntilPerch) {
                     this.bedsDisabledUntilPerch = false;
                     this.bedsUsable = true;
                     this.bedFailures = 0;
                     this.rejectedBedCells.clear();
                  }

                  if (this.dragon != null
                     && this.perchEndHealth > 0.0F
                     && this.dragon.getHealth() > this.perchEndHealth + 10.0F
                     && !this.crystalsFirst
                     && this.crystalsRemain()) {
                     this.crystalsFirst = true;
                     AiSpeedrun.log(
                        "[AI] dragon: healed from {} to {} between perches; crystals first while it flies",
                        String.format(Locale.ROOT, "%.0f", this.perchEndHealth),
                        String.format(Locale.ROOT, "%.0f", this.dragon.getHealth())
                     );
                  }
               }

               if (perch != AiDragon.DragonState.PERCHED && this.lastPerch == AiDragon.DragonState.PERCHED && this.dragon != null) {
                  this.perchEndHealth = this.dragon.getHealth();
               }

               this.lastPerch = perch;
            }

            BlockPos findFountain() {
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

            boolean isPortalActive() {
               if (this.fountain == null) {
                  return false;
               }

               for (int dx = -3; dx <= 3; dx++) {
                  for (int dz = -3; dz <= 3; dz++) {
                     for (int y = this.fountain.getY() - 4; y <= this.fountain.getY() + 1; y++) {
                        BlockPos p = new BlockPos(this.fountain.getX() + dx, y, this.fountain.getZ() + dz);
                        if ("end_portal".equals(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(p).getBlock()).getPath())) {
                           return true;
                        }
                     }
                  }
               }

               return false;
            }

            BlockPos fountainBedCell() {
               int fx = this.fountain.getX();
               int fz = this.fountain.getZ();
               double cx = fx + 0.5;
               double cz = fz + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               double ux = len < 1.0E-6 ? 1.0 : dx / len;
               double uz = len < 1.0E-6 ? 0.0 : dz / len;
               List<double[]> dirs = new ArrayList<>();
               Vec3 facing = AiDragonZones.facing(mc);
               if (facing != null && facing.x * ux + facing.z * uz > 0.35) {
                  dirs.add(new double[]{facing.x, facing.z});
               }

               dirs.add(new double[]{ux, uz});
               AiDragonZones.DangerZones zones = AiDragonZones.zones(mc);
               BlockPos fallback = null;
               List<double[]> clouds = this.nearbyBreathClouds();

               for (double[] dir : dirs) {
                  for (double radius = 3.0; radius <= 7.0; radius++) {
                     BlockPos support = this.groundAt((int)Math.round(fx + dir[0] * radius), (int)Math.round(fz + dir[1] * radius));
                     if (support != null && !this.rejectedBedCells.contains(support.asLong())) {
                        this.bedStance = null;
                        if (zones.isEmpty() || this.findStandAlong(cx, cz, dir, radius, support.getY() + 1, zones)) {
                           if (AiDragon.isClearOfClouds(support.getX() + 0.5, support.getZ() + 0.5, clouds)) {
                              this.bestFoot = support.above();
                              AiSpeedrun.log(
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
                  this.bestFoot = fallback.above();
                  return fallback;
               } else {
                  return null;
               }
            }

            BlockPos perchedBedCell() {
               Entity head = AiDragonZones.head(mc);
               if (head == null) {
                  return null;
               }

               AiDragonZones.DangerZones zones = AiDragonZones.zones(mc);
               double[] breath = AiDragonZones.breath(mc);
               List<double[]> clouds = this.nearbyBreathClouds();
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
                        BlockPos ground = this.groundAt(sx, sz);
                        if (ground != null) {
                           BlockPos stance = ground.above();
                           if (AiDragonZones.isSafeCell(zones, breath, clouds, sx, stance.getY(), sz) && AiCombat.isSafeStep(mc, stance)) {
                              safeStances++;
                              double walk = Math.hypot(sx - me.getX(), sz - me.getZ());

                              for (int bx = sx - 5; bx <= sx + 5; bx++) {
                                 for (int bz = sz - 5; bz <= sz + 5; bz++) {
                                    double r = Math.hypot(bx - sx, bz - sz);
                                    if (!(r < 2.5) && !(r > 5.0)) {
                                       BlockPos support = this.groundAt(bx, bz);
                                       if (support != null && !this.rejectedBedCells.contains(support.asLong())) {
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
                  if (mc.player.tickCount - this.lastNoPlanLogTick > 100) {
                     this.lastNoPlanLogTick = mc.player.tickCount;
                     AiSpeedrun.log(
                        "[AI] dragon: no bed plan: {} safe stances, beds out of reach {}, blasts too close {}, too far from the head {}",
                        safeStances,
                        farBeds,
                        closeBlasts,
                        weakBlasts
                     );
                  }

                  return null;
               } else {
                  this.bedStance = bestStance;
                  this.safeStance = bestStance;
                  this.bestFoot = bestFoot;
                  AiSpeedrun.log(
                     "[AI] dragon: bed plan: stance {}, bed {}, blast {} from the head",
                     bestStance.toShortString(),
                     bestFoot.toShortString(),
                     String.format(Locale.ROOT, "%.1f", bestBlast)
                  );
                  return bestFoot.below();
               }
            }

            boolean findStandAlong(double cx, double cz, double[] dir, double radius, int feetY, AiDragonZones.DangerZones zones) {
               for (double k = 2.0; k <= 4.0; k += 0.25) {
                  double x = cx + dir[0] * (radius + k);
                  double z = cz + dir[1] * (radius + k);
                  if (!zones.hits(AiDragonZones.bodyBox(x, feetY, z), 1.0)) {
                     BlockPos ground = this.groundAt(Mth.floor(x), Mth.floor(z));
                     if (ground != null
                        && Math.abs(ground.getY() + 1 - feetY) <= 1
                        && !zones.hits(AiDragonZones.cellBox(ground.getX(), ground.getY() + 1, ground.getZ()), 1.0)) {
                        this.bedStance = ground.above();
                        return true;
                     }
                  }
               }

               return false;
            }

            BlockPos findStanding() {
               if (this.fountain == null) {
                  return null;
               }

               for (BlockPos p : BlockPos.betweenClosed(this.fountain.offset(-8, -4, -8), this.fountain.offset(8, 2, 8))) {
                  BlockState state = mc.level.getBlockState(p);
                  if (state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                     return p.immutable();
                  }
               }

               return null;
            }

            BlockPos fountainRingStance() {
               double cx = this.fountain.getX() + 0.5;
               double cz = this.fountain.getZ() + 0.5;
               double dx = mc.player.getX() - cx;
               double dz = mc.player.getZ() - cz;
               double len = Math.hypot(dx, dz);
               double ux = len < 1.0 ? 1.0 : dx / len;
               double uz = len < 1.0 ? 0.0 : dz / len;
               AiDragonZones.DangerZones zones = AiDragonZones.zones(mc);
               double[] breath = AiDragonZones.breath(mc);
               List<double[]> lying = this.nearbyBreathClouds();

               for (double r = 12.0; r <= 15.0; r++) {
                  for (double turn = 0.0; turn <= 1.2; turn += 0.3) {
                     for (int sign : new int[]{1, -1}) {
                        double a = sign * turn;
                        double cos = Math.cos(a);
                        double sin = Math.sin(a);
                        int x = (int)Math.floor(cx + (ux * cos - uz * sin) * r);
                        int z = (int)Math.floor(cz + (ux * sin + uz * cos) * r);
                        BlockPos ground = this.groundAt(x, z);
                        if (ground != null) {
                           BlockPos stand = ground.above();
                           if (autismclient.ai.AiDragonZones.isSafeCell(zones, breath, lying, x, stand.getY(), z) && AiCombat.isSafeStep(mc, stand)) {
                              return stand;
                           }
                        }
                     }
                  }
               }

               return null;
            }

            boolean cellBlockedByEntity(BlockPos cell) {
               AABB box = new AABB(cell);

               for (Entity entity : mc.level.getEntities(mc.player, box.inflate(8.0))) {
                  if (entity.blocksBuilding && !entity.isRemoved() && entity.getBoundingBox().intersects(box)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos groundAt(int x, int z) {
               int topY = this.fountain != null ? this.fountain.getY() + 1 : mc.player.blockPosition().getY() + 2;

               for (int y = topY; y > topY - 16; y--) {
                  BlockPos p = new BlockPos(x, y, z);
                  if (!AiTerrain.isLoaded(mc.level, p)) {
                     return null;
                  }

                  if (!mc.level.getBlockState(p).isAir() && mc.level.getBlockState(p.above()).isAir() && mc.level.getBlockState(p.above(2)).isAir()) {
                     return p;
                  }
               }

               return null;
            }

            boolean isBedAt(BlockPos pos) {
               return pos != null && AiDragon.isBedPath(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(pos).getBlock()).getPath());
            }

            boolean headInBedRange() {
               if (this.standing != null && this.dragon != null && this.dragon.isAlive()) {
                  BlockPos head = this.standingBedHead();
                  if (head != null && this.dragonState != AiDragon.DragonState.DYING && this.dragonState != AiDragon.DragonState.FLYING) {
                     double headDistance = Math.sqrt(this.dragon.head.getBoundingBox().distanceToSqr(Vec3.atCenterOf(head)));
                     return AiDragon.isHeadInBedRange(this.dragonState == AiDragon.DragonState.PERCHED, headDistance);
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            int bedCount() {
               int total = 0;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && AiDragon.isBedPath(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            boolean canUseBeds() {
               return this.bedsUsable && this.fountain != null && this.bedCount() > 0 && (bedsMax == 0 || this.bedsUsed < bedsMax);
            }

            int usableBeds() {
               if (!this.canUseBeds()) {
                  return 0;
               } else {
                  return bedsMax == 0 ? this.bedCount() : Math.min(this.bedCount(), Math.max(0, bedsMax - this.bedsUsed));
               }
            }

            String bedItemId() {
               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (!stack.isEmpty() && AiDragon.isBedPath(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath())) {
                     return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                  }
               }

               return null;
            }

            boolean hasRangedWeapon() {
               return AiGameState.countInventoryItem("minecraft:bow") + AiGameState.countInventoryItem("minecraft:crossbow") > 0 && AiRunResources.totalArrowCount() > 0;
            }

            boolean hasWeapon() {
               Inventory inventory = mc.player.getInventory();

               for (int i = 0; i < 36; i++) {
                  if (inventory.getItem(i).has(DataComponents.WEAPON)) {
                     return true;
                  }
               }

               return false;
            }

            void selectWeapon() {
               int slot = AiItems.bestHotbarWeaponSlot();
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, slot);
               }
            }

            int firedCount(AiOperation.Result r) {
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

            void absorbChildData(AiOperation.Result r) {
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
                     this.missingItems = data.getAsJsonObject("missing");
                  }

                  if (data.has("code")) {
                     this.lastFailureCode = data.get("code").getAsString();
                  }
               } catch (RuntimeException var4) {
               }
            }

            int clampSeconds(int cap) {
               return Math.max(10, Math.min(cap, this.secondsLeft()));
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            void releaseLeg() {
               if (this.currentLeg != null) {
                  this.children.cancelChild(AiDragon.ChildRole.LEG, this.currentLeg);
                  this.currentLeg = null;
               }
            }

            void registerChildren() {
               this.children.register(AiDragon.ChildRole.LEG, this.currentLeg);
               this.children.register(AiDragon.ChildRole.GUARD, this.guardOperation);
               this.children.register(AiDragon.ChildRole.DODGE, this.dodgeOperation);
               this.children.register(AiDragon.ChildRole.DOUSE, this.douseOperation);
               this.children.register(AiDragon.ChildRole.ZONE_LEAVE, this.zoneLeaveOperation);
               this.children.register(AiDragon.ChildRole.CHARGE_DODGE, this.chargeDodgeOperation);
            }

            JsonObject statsJson() {
               JsonObject out = AiJson.objectOf(
                  "bedsUsed",
                  this.bedsUsed,
                  "bedsLeft",
                  this.bedCount(),
                  "bowShots",
                  this.bowShots,
                  "meleeSwings",
                  this.meleeSwings,
                  "perchCount",
                  this.perchCount,
                  "dodges",
                  this.dodges,
                  "dragonDead",
                  this.dragonDead,
                  "ineffectiveBedWindows",
                  this.lowDamageDetonations,
                  "lastBedSelfDamageEstimate",
                  this.bedSelfDamageEstimate,
                  "lastBedDragonDamageObserved",
                  this.bedDragonDamage,
                  "bedsUsable",
                  this.bedsUsable,
                  "crystalStockRuns",
                  this.endStoneTrips,
                  "retryableMaterials",
                  this.retryableMaterials,
                  "phase",
                  this.phase.name().toLowerCase(Locale.ROOT),
                  "bedClicked",
                  this.bedClicked
               );
               if (this.bedClicked) {
                  out.addProperty("headRevisionBefore", this.headRevisionBefore);
                  out.addProperty("footRevisionBefore", this.footRevisionBefore);
               }

               if (!this.dragonPhase.isEmpty()) {
                  out.addProperty("dragonPhase", this.dragonPhase);
               }

               if (this.fountain != null) {
                  out.add("fountain", AiJson.objectOf("x", this.fountain.getX(), "y", this.fountain.getY(), "z", this.fountain.getZ()));
               }

               if (this.standing != null) {
                  out.add("armedBed", AiJson.objectOf("x", this.standing.getX(), "y", this.standing.getY(), "z", this.standing.getZ()));
               }

               out.add("survival", AiGuard.statusToJson(AiGuard.sampleStatus(), this.guardDecisions, this.guardReason));
               if (this.missingItems != null) {
                  out.add("missing", this.missingItems);
               }

               if (this.lastFailureCode != null) {
                  out.addProperty("code", this.lastFailureCode);
               }

               return out;
            }

            AiOperation.Result running(String message) {
               return this.children.pendingReconciliation()
                  ? this.finish(AiOperation.Status.UNCERTAIN, null, message)
                  : AiGameOperations.result(AiOperation.Status.RUNNING, message, this.statsJson());
            }

            AiOperation.Result dragonDefeated(String message) {
               this.dragonDead = true;
               AiRunTrace.recordGlobalSplit("dragon_dead", "beds " + this.bedsUsed + ", bow " + this.bowShots + ", melee " + this.meleeSwings + ", perches " + this.perchCount);
               return this.finish(AiOperation.Status.SUCCEEDED, null, message);
            }

            AiOperation.Result finish(AiOperation.Status status, AiFailure failure, String message) {
               this.registerChildren();
               AiOperation.Result result = this.children.result(status, message, this.statsJson(), this.bedClicked);
               this.cancel();
               return new AiOperation.Result(result.status(), result.message(), result.dataJson(), result.evidence(), failure);
            }

            AiOperation.Result fail(AiFailure failure, String message) {
               return this.finish(AiOperation.Status.FAILED, failure, message);
            }

            @Override
            public boolean canInterruptNow() {
               return !this.bedClicked
                  && !this.children.pendingReconciliation()
                  && (this.currentLeg == null || this.currentLeg.canInterruptNow())
                  && (this.guardOperation == null || this.guardOperation.canInterruptNow())
                  && (this.dodgeOperation == null || this.dodgeOperation.canInterruptNow())
                  && (this.douseOperation == null || this.douseOperation.canInterruptNow())
                  && (this.zoneLeaveOperation == null || this.zoneLeaveOperation.canInterruptNow())
                  && (this.chargeDodgeOperation == null || this.chargeDodgeOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.bedClicked
                  || this.children.pendingReconciliation()
                  || this.currentLeg != null && this.currentLeg.requiresReconciliationAfterEmergency()
                  || this.guardOperation != null && this.guardOperation.requiresReconciliationAfterEmergency()
                  || this.dodgeOperation != null && this.dodgeOperation.requiresReconciliationAfterEmergency()
                  || this.douseOperation != null && this.douseOperation.requiresReconciliationAfterEmergency()
                  || this.zoneLeaveOperation != null && this.zoneLeaveOperation.requiresReconciliationAfterEmergency()
                  || this.chargeDodgeOperation != null && this.chargeDodgeOperation.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.releaseLeg();
                  if (this.guardOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.GUARD, this.guardOperation);
                     this.guardOperation = null;
                  }

                  if (this.dodgeOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.DODGE, this.dodgeOperation);
                     this.dodgeOperation = null;
                  }

                  if (this.douseOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.DOUSE, this.douseOperation);
                     this.douseOperation = null;
                  }

                  if (this.zoneLeaveOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.ZONE_LEAVE, this.zoneLeaveOperation);
                     this.zoneLeaveOperation = null;
                  }

                  if (this.chargeDodgeOperation != null) {
                     this.children.cancelChild(AiDragon.ChildRole.CHARGE_DODGE, this.chargeDodgeOperation);
                     this.chargeDodgeOperation = null;
                  }

                  this.aim.clear();
               }
            }

            enum Inner_DataUtil {
               SELECT,
               CRYSTALS,
               CRYSTAL_STOCK,
               PLACE_BED,
               RETREAT,
               COVER,
               ARMED,
               DETONATE,
               MELEE,
               BOW,
               HOLD;
            }
         };
      } else {
         throw new IllegalArgumentException("The dragon fight requires a connected living player");
      }
   }

   enum DragonState {
      PERCHED,
      APPROACHING,
      FLYING,
      DYING;
   }

   enum ChildRole {
      LEG,
      GUARD,
      DODGE,
      DOUSE,
      ZONE_LEAVE,
      CHARGE_DODGE;
   }

   static final class ChildRegistry {
      private final EnumMap<AiDragon.ChildRole, AiDragon.ChildRegistry.LastChildResult> lastByRole = new EnumMap<>(AiDragon.ChildRole.class);
      private final JsonObject pending = new JsonObject();
      private boolean pendingReconciliation;

      AiOperation.Result tickChild(AiDragon.ChildRole role, AiOperation child) {
         AiOperation.Result result = child.tick();
         this.lastByRole.put(role, new AiDragon.ChildRegistry.LastChildResult(child, result));
         if (result.terminal()) {
            result = AiGuardLayer.wrapPendingTermination(needsReconciliation(child, result), result);
         }

         return result;
      }

      void register(AiDragon.ChildRole role, AiOperation child) {
         if (child != null) {
            AiDragon.ChildRegistry.LastChildResult last = this.lastByRole.get(role);
            AiOperation.Result result = last != null && last.child() == child ? last.result() : null;
            if (needsReconciliation(child, result)) {
               this.pendingReconciliation = true;
               JsonObject receipt = AiJson.objectOf("interruptedActionNeedsReconciliation", true);
               if (result != null) {
                  receipt.addProperty("status", result.status().name());
                  receipt.add("receipt", AiRunResources.compactEvidenceJson(result.dataJson()));
               }

               this.pending.add(role.name().toLowerCase(Locale.ROOT), receipt);
            }
         }
      }

      void cancelChild(AiDragon.ChildRole role, AiOperation child) {
         if (child != null) {
            this.register(role, child);
            child.cancel();
         }
      }

      boolean pendingReconciliation() {
         return this.pendingReconciliation;
      }

      AiOperation.Result result(AiOperation.Status status, String message, JsonObject phaseData, boolean bedClicked) {
         JsonObject data = phaseData.deepCopy();
         if (this.pending.size() != 0) {
            data.add("pendingChildren", this.pending.deepCopy());
         }

         return AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, data, bedClicked || this.pendingReconciliation, null);
      }

      private static boolean needsReconciliation(AiOperation child, AiOperation.Result result) {
         JsonObject evidence = result == null ? new JsonObject() : AiRunResources.parseEvidenceJson(result.dataJson());
         return child.requiresReconciliationAfterEmergency()
            || AiJson.getBoolean(evidence, "interruptedActionNeedsReconciliation", false)
            || AiJson.getBoolean(evidence, "reobserveBeforeResuming", false);
      }

      private record LastChildResult(AiOperation child, AiOperation.Result result) {
      }
   }
}
