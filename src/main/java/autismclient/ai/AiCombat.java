package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiCombat {
   private static final double REACH_FACTOR = 0.92;
   private static final float READY_STRENGTH = 0.92F;
   private static final double flush_D = 6.0;
   private static final int close_I = 40;
   static final int init_I = 3;
   static final int load_I = 12;
   private static final int open_I = 8;
   static final long IGNORE_NANOS = 15000000000L;
   static final int read_I = 60;
   static final int write_I = 45;
   static final int update_I = 12;
   static final int sync_I = 8;
   static final double init_D = 0.5;
   static final double COVER_YAW_DEG = 70.0;
   private static final Map<Integer, Long> blazeFirstSeen = new HashMap<>();
   private static long blazeScanTick = -9223372036854775808L;
   static final long NO_PROGRESS_NANOS = 2500000000L;
   static final double read_D = 10.0;
   static final long ENDERMAN_IGNORE_NANOS = 8000000000L;
   static final double write_D = 2.0;
   static final int flush_I = 8;
   static final Map<UUID, Long> ignoredUntil = new ConcurrentHashMap<>();
   private static final int poll = 12;
   private static final int push = 3;
   private static final double PILLAR_MAX_HORIZONTAL = 1.4;
   private static final double open_D = 1.0;
   private static final int pop = 12;
   static final double update_D = 16.0;

   private AiCombat() {
   }

   static double effectiveReach(double reach, boolean holding) {
      return holding ? reach - 0.03 : reach * 0.92;
   }

   static AiProjectiles.IncomingProjectile nextIncoming(Minecraft mc) {
      AiProjectiles.IncomingProjectile best = null;

      for (AiProjectiles.IncomingProjectile t : AiProjectiles.incoming(mc)) {
         if (AiProjectiles.isImminent(t) && (best == null || t.secondsToClosest() < best.secondsToClosest())) {
            best = t;
         }
      }

      return best;
   }

   static AiProjectiles.IncomingProjectile nearestIncoming(Minecraft mc) {
      AiProjectiles.IncomingProjectile best = null;

      for (AiProjectiles.IncomingProjectile shot : AiProjectiles.incoming(mc)) {
         if (AiProjectiles.isImminent(shot)
            && AiDefense.canBlock(mc, mc.level.getEntity(shot.entityId()))
            && AiDefense.shieldReadyFor(mc, ticksToClosest(shot))
            && (best == null || shot.secondsToClosest() < best.secondsToClosest())) {
            best = shot;
         }
      }

      return best;
   }

   static int ticksToClosest(AiProjectiles.IncomingProjectile t) {
      return (int)Math.ceil(t.secondsToClosest() * 20.0);
   }

   static Vec3 shieldAimPoint(Minecraft mc, Vec3 preferred) {
      List<AiProjectiles.IncomingProjectile> threats = new ArrayList<>();

      for (AiProjectiles.IncomingProjectile shot : AiProjectiles.incoming(mc)) {
         if (AiProjectiles.isImminent(shot) && AiDefense.canBlock(mc, mc.level.getEntity(shot.entityId())) && AiDefense.shieldReadyFor(mc, ticksToClosest(shot))) {
            threats.add(shot);
         }
      }

      Vec3 eye = mc.player.getEyePosition();
      Vec3 keep = eye.add(mc.player.getViewVector(1.0F).scale(4.0));
      if (threats.isEmpty()) {
         return preferred != null ? preferred : keep;
      }

      threats.sort(Comparator.comparingDouble(AiProjectiles.IncomingProjectile::secondsToClosest).thenComparingInt(AiProjectiles.IncomingProjectile::entityId));
      int window = Math.max(8, ticksToClosest(threats.getFirst()));
      List<Float> yaws = new ArrayList<>();

      for (AiProjectiles.IncomingProjectile shot : threats) {
         if (ticksToClosest(shot) <= window) {
            yaws.add(yawTo(mc, shot.x(), shot.z()));
         }
      }

      float bestYaw = AiCombatPriority.coveringYaw(mc.player.getYRot(), preferred == null ? null : yawTo(mc, preferred.x, preferred.z), yaws, 70.0);
      double r = Math.toRadians(bestYaw);
      return eye.add(new Vec3(-Math.sin(r), 0.0, Math.cos(r)).scale(4.0));
   }

   static float yawTo(Minecraft mc, double x, double z) {
      return (float)Math.toDegrees(Math.atan2(-(x - mc.player.getX()), z - mc.player.getZ()));
   }

   static boolean isShotInFront(Minecraft mc, AiProjectiles.IncomingProjectile t, float yaw) {
      double dx = t.x() - mc.player.getX();
      double dz = t.z() - mc.player.getZ();
      return dx * dx + dz * dz < 1.0E-4 ? true : Math.abs(Mth.wrapDegrees(yawTo(mc, t.x(), t.z()) - yaw)) <= 70.0;
   }

   static boolean allWithinCoverYaw(Minecraft mc, List<AiProjectiles.IncomingProjectile> ts, float yaw) {
      for (AiProjectiles.IncomingProjectile t : ts) {
         if (!isShotInFront(mc, t, yaw)) {
            return false;
         }
      }

      return true;
   }

   static Creeper mostDangerousCreeper(Minecraft mc) {
      Creeper best = null;
      double score = -1.0 / 0.0;

      for (Entity entity : mc.level.entitiesForRendering()) {
         if (entity instanceof Creeper creeper
            && creeper.isAlive()
            && creeper.getSwellDir() > 0
            && !(creeper.distanceToSqr(mc.player) >= 56.25)
            && mc.player.hasLineOfSight(creeper)) {
            double urgency = creeper.getSwelling(1.0F) * 20.0F - creeper.distanceTo(mc.player);
            if (urgency > score || urgency == score && best != null && creeper.getId() < best.getId()) {
               best = creeper;
               score = urgency;
            }
         }
      }

      return best;
   }

   static Entity findFiringBlaze(Minecraft mc) {
      if (mc.player != null && mc.level != null) {
         long now = autismclient.util.AutismSessionState.getInstance().getClientTick();
         if (now != blazeScanTick) {
            blazeScanTick = now;
            Set<Integer> seen = new HashSet<>();

            for (Entity entity : mc.level.entitiesForRendering()) {
               if (entity instanceof Blaze blaze && blaze.isAlive() && !(blaze.distanceToSqr(mc.player) > 1600.0) && blaze.isOnFire()) {
                  blazeFirstSeen.putIfAbsent(blaze.getId(), now);
                  seen.add(blaze.getId());
               }
            }

            blazeFirstSeen.keySet().retainAll(seen);
         }

         Entity best = null;
         double bestD = 1.7976931348623157E308;

         for (Entry<Integer, Long> entry : blazeFirstSeen.entrySet()) {
            if (now - entry.getValue() >= 45L) {
               Entity blaze = mc.level.getEntity(entry.getKey());
               if (blaze != null && blaze.isAlive()) {
                  double d2 = blaze.distanceToSqr(mc.player);
                  if (!(d2 < 4.840000000000001) && !(d2 > 576.0) && mc.player.hasLineOfSight(blaze) && d2 < bestD) {
                     bestD = d2;
                     best = blaze;
                  }
               }
            }
         }

         return best;
      } else {
         return null;
      }
   }

   static AiOperation rest(final int ticks) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
         int ticksLeft = ticks;
         boolean shieldUp;

         {
            this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
         }

         @Override
         public AiOperation.Result tick() {
            if (mc.player == null) {
               return this.finish(AiOperation.Status.CANCELLED);
            }

            if (--this.ticksLeft <= 0) {
               return this.finish(AiOperation.Status.SUCCEEDED);
            }

            boolean hasShield = AiDefense.hasShield(mc);
            Entity shooter = !hasShield ? null : (AiThreats.isLegacyCombatEnabled() ? AiCombat.findFiringBlaze(mc) : AiThreats.findImminentShooter(mc, 12));
            AiProjectiles.IncomingProjectile coming = hasShield && !AiThreats.isLegacyCombatEnabled() ? AiCombat.nearestIncoming(mc) : null;
            if (coming != null) {
               this.aim
                  .aimAt(AiCombat.shieldAimPoint(mc, shooter == null ? null : autismclient.ai.engine.AiAim.bestEntityAimPoint(shooter)));
               this.aim.tick();
            } else if (shooter != null) {
               this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(shooter));
               this.aim.tick();
            }

            boolean want = shooter != null || coming != null;
            if (want != this.shieldUp) {
               this.shieldUp = want;
               mc.options.keyUse.setDown(want);
            }

            return new AiOperation.Result(AiOperation.Status.RUNNING, this.shieldUp ? "Shield up against a blaze" : "Waiting", "{}", List.of());
         }

         AiOperation.Result finish(AiOperation.Status status) {
            if (this.shieldUp && mc.options != null) {
               mc.options.keyUse.setDown(false);
            }

            this.shieldUp = false;
            this.aim.clear();
            return new AiOperation.Result(status, status == AiOperation.Status.SUCCEEDED ? "waited" : "cancelled", "{}", List.of());
         }

         @Override
         public void cancel() {
            this.finish(AiOperation.Status.CANCELLED);
         }
      };
   }

   static int radialStep(double gap, double slack, double readyIn, double chase, boolean kite) {
      if (readyIn <= 1.0) {
         return slack < 0.05 ? 1 : 0;
      } else if (!kite) {
         return slack < -0.5 ? 1 : 0;
      } else {
         double arriving = gap - chase * readyIn;
         if (!(gap < 0.6) && !(arriving < 0.2)) {
            return slack < -0.4 && arriving > 1.2 && readyIn < 5.0 ? 1 : 0;
         } else {
            return -1;
         }
      }
   }

   private static void ignoreUntil(Entity entity, long until) {
      long now = System.nanoTime();
      ignoredUntil.values().removeIf(expiry -> expiry < now);
      ignoredUntil.put(entity.getUUID(), until);

      while (ignoredUntil.size() > 1024) {
         ignoredUntil.remove(ignoredUntil.keySet().iterator().next());
      }
   }

   static boolean isIgnored(Entity entity) {
      if (entity == null) {
         return true;
      }

      Long until = ignoredUntil.get(entity.getUUID());
      return until != null && until > System.nanoTime();
   }

   static boolean isInDeepWater(Entity entity) {
      return entity.isUnderWater() ? true : entity.isInWater() && entity.level().getFluidState(entity.blockPosition().below()).is(FluidTags.WATER);
   }

   static boolean engageable(boolean unreachable, double distanceSq, double reachGap, boolean deepWater, boolean legacy) {
      return (!unreachable || distanceSq < 9.0 && reachGap <= 1.0) && (legacy || !deepWater || reachGap <= 1.0);
   }

   static boolean isEngageable(Minecraft mc, Entity entity) {
      if (mc.player != null && entity != null && entity.isAlive()) {
         double gap = entity instanceof Mob mob ? AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox()) : 0.0;
         return engageable(isIgnored(entity), entity.distanceToSqr(mc.player), gap, isInDeepWater(entity), AiThreats.isLegacyCombatEnabled());
      } else {
         return false;
      }
   }

   static Comparator<AiCombat.Inner_StreamUtil> targetOrder(AiCombat.Inner_CachePool priority) {
      Comparator var10000;
      switch (priority) {
         case NEAREST:
            var10000 = Comparator.<AiCombat.Inner_StreamUtil>comparingDouble(c -> c.distanceSqr() - (c.hurtPlayer() ? 64 : 0));
            break;
         case WEAKEST:
            var10000 = Comparator.comparingDouble(AiCombat.Inner_StreamUtil::health).thenComparingDouble(AiCombat.Inner_StreamUtil::distanceSqr);
            break;
         case STRONGEST:
            var10000 = Comparator.<AiCombat.Inner_StreamUtil>comparingDouble(c -> -c.maxHealth()).thenComparingDouble(AiCombat.Inner_StreamUtil::distanceSqr);
            break;
         case ATTACKER:
            var10000 = Comparator.<AiCombat.Inner_StreamUtil, Boolean>comparing(c -> !c.attacking())
               .thenComparingDouble(AiCombat.Inner_StreamUtil::distanceSqr);
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   static String lowHealthReason(boolean healAttempted) {
      return healAttempted ? "healed_and_reengaging_exhausted" : "health_threshold";
   }

   static AiCombat.Inner_DataUtil reachPlan(boolean targetOnGround, double horizontal, double verticalGap, double reach) {
      if (!targetOnGround && !(verticalGap <= 1.0)) {
         if (Math.hypot(horizontal, verticalGap) <= reach * 0.92) {
            return AiCombat.Inner_DataUtil.GROUND;
         } else if (pillarHeightToReach(verticalGap, reach) < 0) {
            return AiCombat.Inner_DataUtil.UNREACHABLE;
         } else {
            return horizontal <= 1.4 ? AiCombat.Inner_DataUtil.PILLAR : AiCombat.Inner_DataUtil.APPROACH_UNDER;
         }
      } else {
         return AiCombat.Inner_DataUtil.GROUND;
      }
   }

   static int pillarHeightToReach(double verticalGap, double reach) {
      double effective = reach * 0.92;

      for (int pillar = 1; pillar <= 3; pillar++) {
         if (Math.hypot(1.0, Math.max(0.0, verticalGap - pillar)) <= effective) {
            return pillar;
         }
      }

      return -1;
   }

   static boolean isSafeStep(Minecraft mc, BlockPos step) {
      if (!AiTerrain.isLoaded(mc.level, step)) {
         return false;
      }

      if (mc.level.getFluidState(step).is(FluidTags.LAVA)) {
         return false;
      }

      for (int drop = 1; drop <= 4; drop++) {
         BlockPos below = step.below(drop);
         if (!AiTerrain.isLoaded(mc.level, below)) {
            return false;
         }

         if (mc.level.getFluidState(below).is(FluidTags.LAVA)) {
            return false;
         }

         VoxelShape shape = mc.level.getBlockState(below).getCollisionShape(mc.level, below);
         if (!shape.isEmpty()) {
            return drop <= 3
               && autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(
                  mc.player.position(), new Vec3(step.getX() + 0.5, below.getY() + shape.max(Axis.Y), step.getZ() + 0.5)
               );
         }
      }

      return false;
   }

   static int checkStraightRun(Minecraft mc, Entity target, double maxHorizontal) {
      double dx = target.getX() - mc.player.getX();
      double dz = target.getZ() - mc.player.getZ();
      double horizontal = Math.hypot(dx, dz);
      double below = mc.player.getY() - target.getY();
      double dropAllowance = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() ? 1.3 : 1.2;
      if (!(horizontal > maxHorizontal) && !(below > dropAllowance) && !(below < -1.2)) {
         if (!mc.player.hasLineOfSight(target)) {
            return -1;
         }

         int floorY = mc.player.blockPosition().getY();
         int startY = floorY;
         int stepAhead = 0;
         MutableBlockPos cell = new MutableBlockPos();
         int steps = (int)Math.ceil(horizontal * 2.0);

         for (int i = 1; i <= steps; i++) {
            double t = (double)i / steps;
            int x = Mth.floor(mc.player.getX() + dx * t);
            int z = Mth.floor(mc.player.getZ() + dz * t);
            int found = -2147483648;

            for (int y = floorY + 1; y >= floorY - 1 && found == -2147483648; y--) {
               if (isStandableCell(mc, cell, x, y, z)) {
                  found = y;
               }
            }

            if (found == -2147483648) {
               return -1;
            }

            if (found > startY && horizontal * t <= 1.2) {
               stepAhead = 1;
            }

            floorY = found;
         }

         return stepAhead;
      } else {
         return -1;
      }
   }

   private static boolean isStandableCell(Minecraft mc, MutableBlockPos cell, int x, int y, int z) {
      BlockState floor = mc.level.getBlockState(cell.set(x, y - 1, z));
      if (floor.getFluidState().isEmpty() && !floor.getCollisionShape(mc.level, cell).isEmpty()) {
         for (int h = 0; h <= 1; h++) {
            BlockState state = mc.level.getBlockState(cell.set(x, y + h, z));
            if (!state.getFluidState().isEmpty() || !state.getCollisionShape(mc.level, cell).isEmpty()) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static AiItems.ItemSpec shieldSpec() {
      return AiItems.resolveItem("minecraft:shield");
   }

   static boolean nearFarmland(Minecraft mc) {
      BlockPos feet = mc.player.blockPosition();

      for (int dx = -1; dx <= 1; dx++) {
         for (int dz = -1; dz <= 1; dz++) {
            for (int dy = -1; dy <= 0; dy++) {
               if (mc.level.getBlockState(feet.offset(dx, dy, dz)).is(Blocks.FARMLAND)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   static boolean reachableFromJumpApex(AABB target, Vec3 eye, double reach) {
      return target.distanceToSqr(eye.add(0.0, 1.25, 0.0)) <= reach * reach;
   }

   static Vec3 edgePointToward(BlockPos base, Vec3 toward) {
      double x = base.getX() + 0.5;
      double z = base.getZ() + 0.5;
      double dx = toward.x - x;
      double dz = toward.z - z;
      double scale = Math.max(Math.abs(dx), Math.abs(dz));
      return scale < 0.01 ? new Vec3(x, base.getY(), z) : new Vec3(x + dx / scale * 0.56, base.getY(), z + dz / scale * 0.56);
   }

   static AiOperation fight(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final String spec = AiJson.getString(args, "target", "hostiles").trim();
         final int explicitId = AiJson.getInt(args, "entityId", -1);
         if ((spec.equals("minecraft:blaze") || spec.equals("blaze") || explicitId >= 0 && mc.level.getEntity(explicitId) instanceof Blaze)
            && AiJson.getBoolean(args, "approach", true)
            && !AiJson.getBoolean(args, "hold", false)
            && AiJson.getInt(args, "perch", 0) == 0) {
            return AiBlazeFight.blazeFight(args);
         }

         final int kills = Math.max(1, Math.min(256, AiJson.getInt(args, "count", 1)));
         final double radius = Math.max(4.0, Math.min(128.0, AiJson.getDouble(args, "radius", 16.0)));
         final float retreatHealth = (float)Math.max(0.0, Math.min(20.0, AiJson.getDouble(args, "retreatHealth", 7.0)));
         final int perch = Math.max(0, Math.min(3, AiJson.getInt(args, "perch", 0)));
         final boolean approach = perch == 0 && AiJson.getBoolean(args, "approach", true);
         final boolean hold = perch > 0 || AiJson.getBoolean(args, "hold", false);
         final int timeoutSeconds = Math.max(5, Math.min(1800, AiJson.getInt(args, "timeoutSeconds", 120)));
         final boolean useShield = AiJson.getBoolean(args, "useShield", false);
         final boolean untilClear = AiJson.getBoolean(args, "untilClear", false);
         final int eatBelow = Math.max(0, Math.min(20, AiJson.getInt(args, "eatBelow", 0)));
         final String healWith = AiJson.getString(args, "healWith", "");
         final AiItems.ItemSpec healTarget = healWith.isBlank() ? null : AiItems.resolveItem(healWith);
         final AiCombat.Inner_CachePool priority = AiCombat.Inner_CachePool.parse(AiJson.getString(args, "priority", "nearest"));
         AiService service = AiService.getExistingInstance();
         final boolean silentAim = service != null && service.getSettings().aiSilentAim;
         final Set<EntityType<?>> types = explicitId >= 0 ? Set.of() : parseTargets(spec);
         final Set<String> sheepColors = new HashSet<>();

         for (String color : AiJson.getString(args, "sheepColors", "").split(",")) {
            if (!color.isBlank()) {
               sheepColors.add(color.trim());
            }
         }

         return new AiOperation() {
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final Object startLevel = mc.level;
            final Object startConnection = mc.getConnection();
            final LocalPlayer startPlayer = mc.player;
            final UUID explicitTargetUuid = explicitId >= 0 && mc.level.getEntity(explicitId) != null ? mc.level.getEntity(explicitId).getUUID() : null;
            final autismclient.ai.engine.AiAim aim = new autismclient.ai.engine.AiAim();
            final int startSlot = mc.player.getInventory().getSelectedSlot();
            final float startHealth = mc.player.getHealth();
            final AiCombatDeaths deaths = new AiCombatDeaths();
            Entity target;
            AiOperation travelLeg;
            AiOperation eatOp;
            AiOperation healOp;
            AiOperation dodgeOp;
            int killed;
            int trackingTicks;
            int unhittableDrops;
            int strikerScanCooldown;
            int tickCount;
            int targetSelectedTick;
            int targetSwitches;
            String selectionReason = "initial target";
            boolean defensiveDetour;
            int outOfReachTicks;
            int strafeFlipTicks;
            int swings;
            int lavaJumpsRefused;
            boolean strafeRight = true;
            boolean cancelled;
            boolean childUncertain;
            JsonObject childUncertainty = new JsonObject();
            boolean shieldRaised;
            boolean healAttempted;
            boolean legIsPillar;
            boolean legObstructed;
            int pillarBlocksUsed;
            String airborneReason;
            String retreatReason;
            float lowestHealth;
            AiOperation perchTravelOp;
            int pillarLandingTicks;
            BlockPos perchBase;
            boolean onPerch;
            int noneNearTicks;
            int keepDistanceTicks;
            int stallTargetId = -1;
            int stallTicks;
            double stallBestDistance = 1.7976931348623157E308;
            int perchKnockoffs;
            int pillarCentreTicks;
            boolean placingBackstop;
            boolean backstopFailed;
            final autismclient.ai.engine.AiPlacer placer = new autismclient.ai.engine.AiPlacer().fast();
            boolean shieldPlacing;
            Entity shieldShooter;
            int shooterMemoryTicks;
            int shieldStanceTicks;
            int next;
            Vec3 exposePoint;
            int shieldBlockCount;
            int progressTargetId = -1;
            double bestProgressDistance;
            long lastProgressNanos;
            int blazeWaitTicks;
            int critJumpTicks;
            int crowdSwitchTicks;
            final Map<UUID, Long> ignoredTargets;

            {
               this.aim.setProfile(autismclient.ai.engine.AiAim.AimProfile.FAST);
               this.aim.setSilent(silentAim);
               this.lowestHealth = this.startHealth;
               if (useShield && !AiDefense.hasShield(mc)) {
                  AiItems.equipOffhand(AiCombat.shieldSpec());
               }

               this.ignoredTargets = AiCombat.ignoredUntil;
            }

            @Override
            public AiOperation.Result tick() {
               if (this.cancelled
                  || mc.player == null
                  || mc.level == null
                  || mc.level != this.startLevel
                  || mc.getConnection() != this.startConnection
                  || mc.player != this.startPlayer) {
                  return this.finish(AiOperation.Status.CANCELLED, "Fight cancelled or world/player changed");
               }

               if (!mc.player.isAlive()) {
                  return this.finish(AiOperation.Status.FAILED, "Player died during the fight");
               }

               this.deaths.sweep(mc);
               this.killed = this.deaths.killCount();
               this.tickCount++;
               this.lowestHealth = Math.min(this.lowestHealth, mc.player.getHealth());
               if (System.nanoTime() > this.deadlineNanos) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Fight deadline reached");
               }

               if (this.onPerch) {
                  mc.options.keyShift.setDown(true);
               }

               if (!AiThreats.isLegacyCombatEnabled() && !this.onPerch && mc.player.onGround() && AiEndRun.isInFire(mc)) {
                  BlockPos out = AiEndRun.fireEscapeCell(mc);
                  if (out != null) {
                     this.cancelChild();
                     this.lowerShield();
                     int[] keys = autismclient.ai.engine.AiMotionSafety.relativeMoveAxes(
                        mc.player.getYRot(), autismclient.ai.engine.AiPhysics.yawTowards(mc.player.getX(), mc.player.getZ(), out.getX() + 0.5, out.getZ() + 0.5)
                     );
                     mc.options.keyUp.setDown(keys[0] > 0);
                     mc.options.keyDown.setDown(keys[0] < 0);
                     mc.options.keyRight.setDown(keys[1] > 0);
                     mc.options.keyLeft.setDown(keys[1] < 0);
                     return this.running("Out of the fire");
                  }
               }

               if (!AiThreats.isLegacyCombatEnabled() && !this.onPerch) {
                  Creeper blast = AiCombat.mostDangerousCreeper(mc);
                  if (blast != null && (blast != this.target || blast.getSwelling(1.0F) >= 0.08F)) {
                     this.childUncertain = this.childUncertain | this.requiresReconciliationAfterEmergency();
                     this.cancelChild();
                     if (this.eatOp != null) {
                        this.eatOp.cancel();
                        this.eatOp = null;
                     }

                     if (this.healOp != null) {
                        this.healOp.cancel();
                        this.healOp = null;
                     }

                     this.lowerShield();
                     this.retreatFrom(blast);
                     return this.running("Escaping the most urgent creeper fuse before attacking");
                  }
               }

               if (this.healOp != null) {
                  AiOperation.Result r = this.healOp.tick();
                  if (!r.terminal()) {
                     return this.running("Using " + healWith + " before re-engaging");
                  }

                  this.recordChildResult(r);
                  this.healOp.cancel();
                  this.healOp = null;
                  return this.running("Re-engaging after " + healWith);
               } else {
                  if (this.dodgeOp != null) {
                     AiOperation.Result defense = this.dodgeOp.tick();
                     if (!defense.terminal()) {
                        return this.running("Answering an unblockable projectile: " + defense.message());
                     }

                     this.dodgeOp.cancel();
                     this.dodgeOp = null;
                     if (defense.status() == AiOperation.Status.UNCERTAIN) {
                        this.cancel();
                        return defense;
                     }
                  }

                  if (!AiThreats.isLegacyCombatEnabled() && this.canInterruptNow() && autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                     AiProjectiles.IncomingProjectile shot = AiCombat.nextIncoming(mc);
                     if (shot != null && AiCombat.ticksToClosest(shot) <= 8 && (!AiDefense.canBlock(mc, mc.level.getEntity(shot.entityId())) || !AiDefense.shieldReadyFor(mc, AiCombat.ticksToClosest(shot)))) {
                        this.cancelChild();
                        this.releaseAllInputs();
                        this.dodgeOp = AiDodge.dodge(AiJson.objectOf("deflect", true, "shield", true, "timeoutSeconds", 4));
                        AiOperation.Result defense = this.dodgeOp.tick();
                        if (defense.terminal()) {
                           this.dodgeOp.cancel();
                           this.dodgeOp = null;
                           if (defense.status() == AiOperation.Status.UNCERTAIN) {
                              this.cancel();
                              return defense;
                           }
                        }

                        return this.running("Dodging a projectile the shield cannot stop in time: " + defense.message());
                     }
                  }

                  if (perch > 0) {
                     AiOperation.Result held = this.holdPerch();
                     if (held != null) {
                        return held;
                     }
                  } else if (hold) {
                     AiOperation.Result held = this.holdGround();
                     if (held != null) {
                        return held;
                     }
                  }

                  if (mc.player.getHealth() <= retreatHealth && !this.onPerch) {
                     if (healTarget != null && !this.healAttempted && AiItems.countInInventory(healTarget) > 0) {
                        this.healAttempted = true;
                        this.cancelChild();
                        this.releaseAllInputs();
                        this.healOp = this.healOperation();
                        return this.running("Retreating to use " + healWith);
                     } else {
                        this.retreatReason = AiCombat.lowHealthReason(this.healAttempted);
                        return this.fail(
                           AiFailure.LOW_HEALTH,
                           "Disengaged at " + (int)mc.player.getHealth() + " health (retreat threshold " + (int)retreatHealth + "); heal before re-engaging"
                        );
                     }
                  } else {
                     if (this.killed >= kills) {
                        return this.finish(AiOperation.Status.SUCCEEDED, "Killed " + this.killed + " target(s)");
                     }

                     if (eatBelow > 0) {
                        if (this.eatOp != null) {
                           if (!this.noThreatsNearby()) {
                              this.childUncertain = this.childUncertain | this.eatOp.requiresReconciliationAfterEmergency();
                              this.eatOp.cancel();
                              this.eatOp = null;
                              return this.running("A threat closed in while eating; re-engaging");
                           }

                           AiOperation.Result r = this.eatOp.tick();
                           if (!r.terminal()) {
                              return this.running("Eating before re-engaging");
                           }

                           this.recordChildResult(r);
                           this.eatOp.cancel();
                           this.eatOp = null;
                           return this.running("Re-engaging after eating");
                        }

                        if (mc.player.getFoodData().getFoodLevel() < eatBelow && this.noThreatsNearby()) {
                           this.cancelChild();
                           this.releaseAllInputs();
                           this.eatOp = AiSurvival.eat(AiJson.objectOf("timeoutSeconds", 15));
                           return this.running("Disengaging to eat");
                        }
                     }

                     if (this.defensiveDetour
                        && this.target != null
                        && (
                           !this.target.isAlive()
                              || this.target.isRemoved()
                              || this.target instanceof LivingEntity dying && dying.isDeadOrDying()
                        )) {
                        this.defensiveDetour = false;
                        this.target = null;
                        this.cancelChild();
                        this.releaseAllInputs();
                     }

                     if (this.target != null && this.target instanceof LivingEntity living && living.isDeadOrDying()) {
                        this.target = null;
                        this.cancelChild();
                        this.releaseAllInputs();
                        if (this.killed >= kills) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Killed " + this.killed + " target(s)");
                        }
                     }

                     if (this.target != null
                        && (!this.target.isAlive() || this.target.isRemoved() || mc.level.getEntity(this.target.getId()) != this.target)) {
                        this.target = null;
                        this.cancelChild();
                        this.releaseAllInputs();
                     }

                     if (this.target == null) {
                        this.target = this.selectTarget();
                        this.targetSelectedTick = this.tickCount;
                        if (this.target instanceof LivingEntity requested) {
                           this.deaths.watch(requested);
                        }

                        this.trackingTicks = 0;
                        this.outOfReachTicks = 0;
                        this.next = 0;
                        this.exposePoint = null;
                        this.pillarBlocksUsed = 0;
                        if (this.target == null) {
                           AiOperation.Result waiting = this.waitForAirborneMob();
                           if (waiting != null) {
                              return waiting;
                           }

                           this.releaseAllInputs();
                           if (AiCombatPriority.isKillGoalMet(this.killed, kills, untilClear, untilClear && AiGuard.sampleStatus().hostilesNear() == 0)) {
                              return this.finish(AiOperation.Status.SUCCEEDED, "Threats cleared after " + this.killed + " verified kill(s)");
                           }

                           return this.fail(AiFailure.TARGET_GONE, "No matching live target within " + (int)radius + " blocks");
                        }
                     }

                     if (this.defensiveDetour && this.target.distanceToSqr(mc.player) > 49.0) {
                        this.target = null;
                        this.defensiveDetour = false;
                        this.cancelChild();
                        this.releaseAllInputs();
                        return this.running("Defensive threat left; reacquiring the requested target");
                     }

                     if (!AiThreats.isLegacyCombatEnabled() && !this.defensiveDetour && --this.strikerScanCooldown <= 0) {
                        this.strikerScanCooldown = 4;
                        Entity striker = this.findMeleeStriker();
                        if (striker != null && striker != this.target && !this.isInMeleeContact(this.target)) {
                           this.switchTarget(striker, "melee attacker at the body");
                           this.trackingTicks = 0;
                           this.outOfReachTicks = 0;
                           this.next = 0;
                           this.exposePoint = null;
                        }
                     }

                     this.selectBestWeapon();
                     double distance = mc.player.distanceTo(this.target);
                     double reach = AiDefense.attackReach(mc);
                     boolean creeperSwing = !AiThreats.isLegacyCombatEnabled()
                        && this.target instanceof Creeper early
                        && early.getSwelling(1.0F) < 0.08F
                        && mc.player.getAttackStrengthScale(0.0F) >= 0.92F
                        && this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition())
                           <= AiCombat.effectiveReach(reach, false) * AiCombat.effectiveReach(reach, false);
                     if (!creeperSwing && this.target instanceof Creeper creeper && creeper.getSwellDir() > 0 && distance < (AiThreats.isLegacyCombatEnabled() ? 6.0 : 7.5)) {
                        this.cancelChild();
                        this.retreatFrom(this.target);
                        if (AiThreats.isLegacyCombatEnabled()) {
                           this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target));
                           this.aim.tick();
                        }

                        return this.running("Backing out of creeper blast range");
                     } else {
                        if (!AiThreats.isLegacyCombatEnabled() && AiDefense.hasShield(mc)) {
                           AiProjectiles.IncomingProjectile coming = AiCombat.nearestIncoming(mc);
                           int in = coming == null ? 2147483647 : AiCombat.ticksToClosest(coming);
                           boolean raised = mc.player.isUsingItem() && mc.player.getUseItem().is(Items.SHIELD);
                           if (coming != null && in <= 8 && (raised || in >= 5)) {
                              this.cancelChild();
                              this.releaseMovementKeys();
                              this.aim
                                 .aimAt(AiCombat.shieldAimPoint(mc, autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target)));
                              this.aim.tick();
                              if (!this.shieldRaised) {
                                 this.shieldRaised = true;
                                 mc.options.keyUse.setDown(true);
                              }

                              return this.running("Shield up against the incoming " + coming.type().replace("minecraft:", ""));
                           }
                        }

                        double hitDistance = Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                        double attackDistance = AiCombat.effectiveReach(reach, this.onPerch || hold);
                        if ((this.onPerch || hold) && hitDistance > attackDistance && ++this.outOfReachTicks > 12) {
                           Entity next = this.selectTarget();
                           if (next != null
                              && next != this.target
                              && next.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= attackDistance * attackDistance) {
                              this.switchTarget(next, "reachable target while holding ground");
                              hitDistance = Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                              this.trackingTicks = this.outOfReachTicks = 0;
                              this.next = 0;
                              this.exposePoint = null;
                           }
                        }

                        if (!(hitDistance > attackDistance)) {
                           this.outOfReachTicks = 0;
                           Vec3 hitPoint = autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target);
                           if (!this.onPerch && !hold && !autismclient.ai.engine.AiAim.canHitEntityAt(this.target, hitPoint)) {
                              return !approach ? this.fail(AiFailure.UNREACHABLE, "Target's attack ray is obstructed and approach is disabled") : this.approachTarget(distance, true);
                           }

                           this.cancelChild();
                           this.progressTargetId = -1;
                           if (this.onPerch
                              && !autismclient.ai.engine.AiAim.canHitEntityAt(this.target, hitPoint)
                              && this.next < 20
                              && mc.player.onGround()) {
                              if (this.exposePoint == null || !this.canWalkTo(this.exposePoint)) {
                                 this.exposePoint = this.findExposurePoint();
                              }

                              if (this.exposePoint != null && Math.hypot(this.exposePoint.x - mc.player.getX(), this.exposePoint.z - mc.player.getZ()) > 0.04) {
                                 double x = this.exposePoint.x;
                                 double z = this.exposePoint.z;
                                 this.next++;
                                 this.aim.aimAt(hitPoint);
                                 this.aim.tick();
                                 this.sneakToward(x, z);
                                 return this.running("Exposing the target past the pillar");
                              }
                           }

                           if (this.onPerch) {
                              this.releaseAllInputs();
                           }

                           this.aim.aimAt(hitPoint);
                           this.aim.tick();
                           if (this.aim.isAimedAtEntity(this.target)) {
                              this.trackingTicks = 0;
                              this.unhittableDrops = 0;
                              this.selectBestWeapon();
                              if (!AiThreats.isLegacyCombatEnabled()) {
                                 return this.meleeInReach(hitPoint);
                              }

                              this.updateShield();
                              if (this.canStrafe()) {
                                 this.strafeAround();
                              }

                              Entity volley = AiDefense.hasShield(mc) ? AiCombat.findFiringBlaze(mc) : null;
                              if (volley != null && volley != this.target) {
                                 this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(volley));
                                 this.aim.tick();
                                 return this.running("Shield up against a blaze");
                              }

                              if (mc.player.getAttackStrengthScale(0.0F) >= 0.92F && volley == null && AiDefense.canSwingAt(mc, this.target, this.aim)) {
                                 mc.options.keySprint.setDown(false);
                                 this.lowerShield();
                                 if (!this.defensiveDetour && this.target instanceof LivingEntity requested) {
                                    this.deaths.watch(requested);
                                 }

                                 mc.gameMode.attack(mc.player, this.target);
                                 Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                                 mc.player.resetAttackStrengthTicker();
                                 this.swings++;
                                 return this.running(
                                    "Hit "
                                       + BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).getPath()
                                       + " ("
                                       + this.killed
                                       + "/"
                                       + kills
                                       + ")"
                                 );
                              } else {
                                 this.tryCritJump();
                                 return this.running("Waiting out the attack cooldown");
                              }
                           } else if (++this.trackingTicks <= 40) {
                              if (this.canStrafe()) {
                                 this.strafeAround();
                              }

                              return this.running("Tracking target");
                           } else {
                              Entity dropped = this.target;
                              this.target = null;
                              if (!this.onPerch && !hold) {
                                 AiCombat.ignoreUntil(dropped, System.nanoTime() + 15000000000L);
                              } else if (++this.unhittableDrops >= 3) {
                                 AiCombat.ignoreUntil(dropped, System.nanoTime() + 15000000000L);
                                 this.releaseAllInputs();
                                 return this.finish(
                                    AiOperation.Status.SUCCEEDED,
                                    "The " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath() + " cannot be hit from here; going on"
                                 );
                              }

                              return this.running("Could not acquire " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                           }
                        } else if (this.onPerch || hold) {
                           this.releaseAllInputs();
                           boolean hasShield = AiDefense.hasShield(mc);
                           Entity shooter = hasShield ? (AiThreats.isLegacyCombatEnabled() ? AiCombat.findFiringBlaze(mc) : AiThreats.findImminentShooter(mc, 12)) : null;
                           AiProjectiles.IncomingProjectile coming = hasShield && !AiThreats.isLegacyCombatEnabled() ? AiCombat.nearestIncoming(mc) : null;
                           if (coming != null) {
                              this.aim
                                 .aimAt(
                                    AiCombat.shieldAimPoint(
                                       mc, autismclient.ai.engine.AiAim.bestEntityAimPoint(shooter != null ? shooter : this.target)
                                    )
                                 );
                           } else {
                              this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(shooter != null ? shooter : this.target));
                           }

                           this.aim.tick();
                           boolean wantShield = hasShield && (shooter != null || coming != null || this.projectileIncoming());
                           if (wantShield != this.shieldRaised) {
                              this.shieldRaised = wantShield;
                              mc.options.keyUse.setDown(wantShield);
                           }

                           return this.running(shooter != null ? "Shield up against a blaze" : "Holding the pillar top");
                        } else if (!approach) {
                           this.releaseAllInputs();
                           return this.fail(AiFailure.UNREACHABLE, "Target is out of reach and approach is disabled");
                        } else if (!AiThreats.isLegacyCombatEnabled()
                           && AiThreats.isMeleeThreat(AiThreats.classify(this.target))
                           && distance < 7.0
                           && AiThreats.hasBeenApproachingFor(this.target, 3)
                           && mc.player.hasLineOfSight(this.target)) {
                           this.cancelChild();
                           this.releaseAllInputs();
                           this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target));
                           this.aim.tick();
                           return this.running("Letting the " + BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).getPath() + " come");
                        } else if (!AiThreats.isLegacyCombatEnabled() && types.isEmpty() && explicitId < 0 && this.target instanceof Enderman && distance > 10.0) {
                           Entity dropped = this.target;
                           this.target = null;
                           this.cancelChild();
                           this.releaseAllInputs();
                           AiCombat.ignoreUntil(dropped, System.nanoTime() + 8000000000L);
                           return this.running("The enderman teleported off; not chasing it");
                        } else {
                           int run = AiCombat.checkStraightRun(mc, this.target, 16.0);
                           return run >= 0 ? this.runInApproach(distance, run == 1) : this.approachClear(distance);
                        }
                     }
                  }
               }
            }

            AiOperation.Result meleeInReach(Vec3 hitPoint) {
               boolean shield = AiDefense.hasShield(mc);
               float strength = mc.player.getAttackStrengthScale(0.0F);
               double recharge = 20.0 / Math.max(0.1, mc.player.getAttributeValue(Attributes.ATTACK_SPEED));
               double readyIn = Math.max(0.0, (0.92F - strength) * recharge);
               boolean ready = strength >= 0.92F;
               AiThreats.ThreatKind profile = AiThreats.classify(this.target);
               Entity volley = shield ? AiThreats.findImminentShooter(mc, 12) : null;
               if (volley == null || volley == this.target && ready && AiThreats.ticksUntilRangedShot(mc, volley) > 11) {
                  if (!AiThreats.isLegacyCombatEnabled() && !this.onPerch && !hold && AiThreats.isMeleeThreat(profile)) {
                     double knock = profile == AiThreats.ThreatKind.HOGLIN ? 6.0 : 2.5;
                     if (this.knockbackIsUnsafe(mc.player.getX(), mc.player.getZ(), knock)) {
                        int side = this.knockbackSide(knock);
                        if (side != 0) {
                           mc.options.keySprint.setDown(false);
                           mc.options.keyJump.setDown(false);
                           mc.options.keyUp.setDown(false);
                           mc.options.keyDown.setDown(false);
                           mc.options.keyRight.setDown(side > 0 && mc.player.onGround());
                           mc.options.keyLeft.setDown(side < 0 && mc.player.onGround());
                           this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target));
                           this.aim.tick();
                           boolean up = shield && !ready;
                           if (up != this.shieldRaised) {
                              this.shieldRaised = up;
                              mc.options.keyUse.setDown(up);
                           }

                           return this.running("Turning the fight off the edge behind");
                        }
                     }
                  }

                  double gap = this.target instanceof Mob mob ? AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox()) : 10.0;
                  double reach = AiDefense.attackReach(mc);
                  double slack = AiCombat.effectiveReach(reach, this.onPerch || hold)
                     - Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                  boolean meleeMob = AiThreats.isMeleeThreat(profile) && !this.onPerch;
                  boolean kite = meleeMob && AiThreats.estimateThreatSpeedScore(this.target) < 0.19;
                  boolean cornered = kite && !AiThreats.isLegacyCombatEnabled() && !this.onPerch && !this.hasRoomToBackOff(this.target);
                  if (cornered) {
                     kite = false;
                  }

                  if (ready && !mc.player.onGround() && mc.player.getDeltaMovement().y > 0.0 && this.critJumpTicks > 0 && this.critJumpTicks < 12) {
                     this.critJumpTicks++;
                     return this.running("Waiting for the fall to crit");
                  }

                  if (ready && AiDefense.canSwingAt(mc, this.target, this.aim)) {
                     this.lowerShield();
                     mc.options.keySprint.setDown(false);
                     if (!this.defensiveDetour && this.target instanceof LivingEntity requested) {
                        this.deaths.watch(requested);
                     }

                     mc.gameMode.attack(mc.player, this.target);
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     mc.player.resetAttackStrengthTicker();
                     this.swings++;
                     if (kite && gap < 1.0) {
                        this.applyRadialStep(-1);
                     } else {
                        this.releaseMovementKeys();
                     }

                     return this.running(
                        "Hit " + BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).getPath() + " (" + this.killed + "/" + kills + ")"
                     );
                  } else {
                     Entity crowd = null;
                     double crowdGap = 0.8;

                     for (Entity other : mc.level.entitiesForRendering()) {
                        if (other != this.target && other instanceof Mob mob && mob.isAlive() && !(mob.distanceToSqr(mc.player) > 64.0)) {
                           if (other instanceof Creeper creeper && creeper.getSwellDir() > 0 && creeper.distanceTo(mc.player) < 7.5) {
                              this.lowerShield();
                              this.retreatFrom(creeper);
                              return this.running("Out of the creeper's blast");
                           }

                           if (AiThreats.isMeleeThreat(AiThreats.classify(other)) && AiThreats.isActiveThreat(mc, other)) {
                              double g = AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox());
                              if (g < crowdGap) {
                                 crowdGap = g;
                                 crowd = other;
                              }
                           }
                        }
                     }

                     if (crowd != null) {
                        boolean room = this.hasRoomToBackOff(crowd);
                        double attackRange = AiCombat.effectiveReach(AiDefense.attackReach(mc), this.onPerch || hold);
                        boolean otherHittable = mc.player.hasLineOfSight(crowd)
                           && crowd.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= attackRange * attackRange;
                        if (!AiThreats.isLegacyCombatEnabled()
                           && (!room || ++this.crowdSwitchTicks > 10)
                           && AiCombatPriority.shouldSwitchTarget(gap, crowdGap, this.tickCount - this.targetSelectedTick, slack >= 0.0, otherHittable)) {
                           this.crowdSwitchTicks = 0;
                           this.switchTarget(crowd, "closer reachable melee threat");
                           return this.running("Turning on the " + BuiltInRegistries.ENTITY_TYPE.getKey(crowd.getType()).getPath() + " at the player");
                        }

                        if (room) {
                           this.stepAwayFrom(crowd);
                           return this.running("Out of the " + BuiltInRegistries.ENTITY_TYPE.getKey(crowd.getType()).getPath() + "'s reach");
                        }

                        kite = false;
                        cornered = true;
                     }

                     this.crowdSwitchTicks = 0;
                     boolean wantShield = shield && (this.projectileIncoming() || meleeMob && !kite && gap < 1.0 && readyIn > 3.0);
                     if (wantShield != this.shieldRaised) {
                        this.shieldRaised = wantShield;
                        mc.options.keyUse.setDown(wantShield);
                     }

                     if (!meleeMob) {
                        if (this.canStrafe()) {
                           this.strafeAround();
                        }

                        this.tryCritJump();
                        return this.running("Waiting out the attack cooldown");
                     } else {
                        int radial = AiCombat.radialStep(gap, slack, readyIn, AiThreats.estimateThreatSpeedScore(this.target), kite);
                        this.applyRadialStep(radial);
                        if (cornered && radial == 0 && slack > 0.2 && readyIn <= 7.0 && this.hasHeadroom()) {
                           this.tryCritJump();
                        } else if (radial == 0 && gap > 0.6 && slack > 0.2 && readyIn < 8.0 && !wantShield) {
                           this.tryCritJump();
                        } else {
                           mc.options.keyJump.setDown(false);
                        }

                        if (mc.options.keyJump.isDown()) {
                           if (this.critJumpTicks == 0) {
                              this.critJumpTicks = 1;
                           }
                        } else if (mc.player.onGround()) {
                           this.critJumpTicks = 0;
                        } else if (this.critJumpTicks > 0) {
                           this.critJumpTicks++;
                        }

                        return this.running(
                           radial < 0 ? "Keeping out of its reach" : (radial > 0 ? "Stepping in for the swing" : "Waiting out the attack cooldown")
                        );
                     }
                  }
               } else {
                  this.releaseMovementKeys();
                  this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(volley));
                  this.aim.tick();
                  if (!this.shieldRaised) {
                     this.shieldRaised = true;
                     mc.options.keyUse.setDown(true);
                  }

                  return this.running("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(volley.getType()).getPath());
               }
            }

            boolean knockbackIsUnsafe(double x, double z, double knock) {
               double dx = x - this.target.getX();
               double dz = z - this.target.getZ();
               double len = Math.hypot(dx, dz);
               if (len < 0.001) {
                  return false;
               }

               dx /= len;
               dz /= len;
               int feetY = Mth.floor(mc.player.getY() + 0.01);

               for (double t = 0.5; t <= knock; t += 0.5) {
                  int cx = Mth.floor(x + dx * t);
                  int cz = Mth.floor(z + dz * t);
                  boolean floor = false;

                  for (int dy = 1; dy >= -4; dy--) {
                     BlockPos cell = new BlockPos(cx, feetY + dy, cz);
                     if (mc.level.getFluidState(cell).is(FluidTags.LAVA)) {
                        return true;
                     }

                     if (dy <= 0 && !mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                        if (dy == 0) {
                           return false;
                        }

                        floor = true;
                        break;
                     }
                  }

                  if (!floor) {
                     return true;
                  }
               }

               return false;
            }

            int knockbackSide(double knock) {
               double yaw = Math.toRadians(mc.player.getYRot());
               double rx = -Math.cos(yaw);
               double rz = -Math.sin(yaw);

               for (int side : new int[]{1, -1}) {
                  double x = mc.player.getX() + rx * side * 1.5;
                  double z = mc.player.getZ() + rz * side * 1.5;
                  if (AiCombat.isSafeStep(mc, BlockPos.containing(x, mc.player.getY(), z))
                     && (!mc.player.onGround() || autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, 0.0, side > 0 ? -1.0 : 1.0, 6) != null)
                     && !this.knockbackIsUnsafe(x, z, knock)) {
                     return side;
                  }
               }

               return 0;
            }

            void applyRadialStep(int radial) {
               mc.options.keySprint.setDown(false);
               mc.options.keyJump.setDown(false);
               if (radial > 0) {
                  boolean safe = this.isForwardStepSafe()
                     && autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(1.0, 0.0)
                     && (!mc.player.onGround() || autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, 1.0, 0.0, 6) != null);
                  mc.options.keyUp.setDown(safe);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               } else if (radial >= 0) {
                  this.releaseMovementKeys();
               } else {
                  double radians = Math.toRadians(mc.player.getYRot());
                  boolean behind = AiCombat.isSafeStep(
                        mc, BlockPos.containing(mc.player.getX() + Math.sin(radians), mc.player.getY(), mc.player.getZ() - Math.cos(radians))
                     )
                     && autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(-1.0, 0.0);
                  behind &= !mc.player.onGround() || autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, -1.0, 0.0, 6) != null;
                  if (behind) {
                     mc.options.keyUp.setDown(false);
                     mc.options.keyDown.setDown(true);
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                  } else {
                     mc.options.keyDown.setDown(false);
                     if (this.canStrafe()) {
                        this.strafeAround();
                     }
                  }
               }
            }

            void stepAwayFrom(Entity from) {
               float away = autismclient.ai.engine.AiPhysics.yawTowards(from.getX(), from.getZ(), mc.player.getX(), mc.player.getZ());
               int[] keys = autismclient.ai.engine.AiMotionSafety.relativeMoveAxes(mc.player.getYRot(), away);
               double radians = Math.toRadians(away);
               boolean safe = AiCombat.isSafeStep(
                     mc, BlockPos.containing(mc.player.getX() - Math.sin(radians), mc.player.getY(), mc.player.getZ() + Math.cos(radians))
                  )
                  && autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(keys[0], -keys[1]);
               mc.options.keySprint.setDown(false);
               mc.options.keyJump.setDown(false);
               mc.options.keyUp.setDown(safe && keys[0] > 0);
               mc.options.keyDown.setDown(safe && keys[0] < 0);
               mc.options.keyRight.setDown(safe && keys[1] > 0);
               mc.options.keyLeft.setDown(safe && keys[1] < 0);
            }

            void releaseMovementKeys() {
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               mc.options.keyJump.setDown(false);
               mc.options.keySprint.setDown(false);
            }

            AiOperation.Result approachClear(double distance) {
               return this.approachTarget(distance, false);
            }

            AiOperation.Result approachTarget(double distance, boolean obstructed) {
               double reach = AiDefense.attackReach(mc);
               double horizontal = Math.hypot(this.target.getX() - mc.player.getX(), this.target.getZ() - mc.player.getZ());
               BlockPos goalCell = this.target.blockPosition();
               if (!AiThreats.isLegacyCombatEnabled()) {
                  Entity due = AiDefense.hasShield(mc) ? AiThreats.findImminentShooter(mc, 12) : null;
                  if (due != null) {
                     this.cancelChild();
                     this.releaseMovementKeys();
                     this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(due));
                     this.aim.tick();
                     if (!this.shieldRaised) {
                        this.shieldRaised = true;
                        mc.options.keyUse.setDown(true);
                     }

                     this.lastProgressNanos = System.nanoTime();
                     return this.running("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(due.getType()).getPath() + " on the way in");
                  }

                  this.lowerShield();
                  if (!this.target.onGround() && this.target.getBoundingBox().minY - mc.player.getEyeY() < reach * 0.8) {
                     BlockPos under = this.groundUnder(this.target);
                     if (under != null) {
                        goalCell = under;
                     }
                  }
               }

               boolean flier = this.target instanceof Blaze
                  || this.target instanceof Ghast
                  || this.target instanceof Phantom
                  || this.target instanceof Vex
                  || this.target.isNoGravity();
               AiCombat.Inner_DataUtil plan = goalCell != this.target.blockPosition() || !AiThreats.isLegacyCombatEnabled() && !flier
                  ? AiCombat.Inner_DataUtil.GROUND
                  : AiCombat.reachPlan(this.target.onGround(), horizontal, this.target.getY() - mc.player.getY(), reach);
               if (plan == AiCombat.Inner_DataUtil.UNREACHABLE) {
                  if (!AiThreats.isLegacyCombatEnabled() && this.target instanceof Blaze) {
                     this.cancelChild();
                     this.releaseMovementKeys();
                     Entity due = AiDefense.hasShield(mc) ? AiThreats.findImminentShooter(mc, 12) : null;
                     this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(due != null ? due : this.target));
                     this.aim.tick();
                     boolean up = due != null;
                     if (up != this.shieldRaised) {
                        this.shieldRaised = up;
                        mc.options.keyUse.setDown(up);
                     }

                     this.lastProgressNanos = System.nanoTime();
                     return this.running("Waiting for the blaze to come down");
                  } else {
                     return this.failAirborne("hovering above the 3-block pillar cap");
                  }
               } else {
                  if (plan != AiCombat.Inner_DataUtil.GROUND) {
                     return this.reachElevatedTarget(plan, distance, reach);
                  }

                  if (this.travelLeg != null && obstructed && !this.legObstructed) {
                     this.cancelChild();
                  }

                  if (this.travelLeg == null) {
                     this.legObstructed = obstructed;
                     this.travelLeg = AiGoto.travelToCompat(
                        goalCell, obstructed ? 1.2 : 2.0, true, true, true, 8, autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST
                     );
                     this.legIsPillar = false;
                  }

                  long now = System.nanoTime();
                  if (this.progressTargetId != this.target.getId() || distance < this.bestProgressDistance - 1.0) {
                     this.progressTargetId = this.target.getId();
                     this.bestProgressDistance = distance;
                     this.lastProgressNanos = now;
                  } else if (autismclient.ai.engine.AiSight.isRangedShooter(this.target)
                     && (
                        now - this.lastProgressNanos > 2500000000L
                           || distance > this.bestProgressDistance + 2.0
                              && autismclient.ai.engine.AiSight.hasLineOfSight(
                                 mc.level, this.target.getEyePosition(), mc.player.getEyePosition(), -9223372036854775808L, -9223372036854775808L
                              )
                     )) {
                     Entity dropped = this.target;
                     this.target = null;
                     this.cancelChild();
                     this.releaseAllInputs();
                     AiCombat.ignoreUntil(dropped, now + 15000000000L);
                     return this.running("Not closing on the " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                  }

                  AiOperation.Result legResult = this.travelLeg.tick();
                  if (legResult.terminal()) {
                     this.cancelChild();
                     if (legResult.status() == AiOperation.Status.FAILED) {
                        Entity dropped = this.target;
                        this.target = null;
                        AiCombat.ignoreUntil(dropped, System.nanoTime() + 15000000000L);
                        return this.running("No route to " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
                     }
                  }

                  return this.running("Closing on target (" + (int)distance + "m)");
               }
            }

            AiOperation.Result runInApproach(double distance, boolean stepUp) {
               this.cancelChild();
               if (!AiThreats.isLegacyCombatEnabled() && AiDefense.hasShield(mc)) {
                  Entity due = AiThreats.findImminentShooter(mc, 12);
                  double reach = AiDefense.attackReach(mc);
                  if (due != null
                     && (due != this.target || Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > reach + 1.2)) {
                     this.releaseMovementKeys();
                     this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(due));
                     this.aim.tick();
                     if (!this.shieldRaised) {
                        this.shieldRaised = true;
                        mc.options.keyUse.setDown(true);
                     }

                     this.lastProgressNanos = System.nanoTime();
                     return this.running("Shield up against the " + BuiltInRegistries.ENTITY_TYPE.getKey(due.getType()).getPath() + " on the run in");
                  }
               }

               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(false);
               mc.options.keyRight.setDown(false);
               this.lowerShield();
               long now = System.nanoTime();
               if (this.progressTargetId != this.target.getId() || distance < this.bestProgressDistance - 1.0) {
                  this.progressTargetId = this.target.getId();
                  this.bestProgressDistance = distance;
                  this.lastProgressNanos = now;
               } else if (autismclient.ai.engine.AiSight.isRangedShooter(this.target) && now - this.lastProgressNanos > 2500000000L) {
                  Entity dropped = this.target;
                  this.target = null;
                  this.releaseAllInputs();
                  AiCombat.ignoreUntil(dropped, now + 15000000000L);
                  return this.running("Not closing on the " + BuiltInRegistries.ENTITY_TYPE.getKey(dropped.getType()).getPath());
               }

               this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(this.target));
               this.aim.tick();
               boolean facing = Math.abs(Mth.wrapDegrees(this.yawToEntity(this.target) - mc.player.getYRot())) < 35.0F;
               boolean jump = facing && stepUp && mc.player.onGround();
               boolean safe = facing && autismclient.ai.engine.AiMotionSafety.isPlayerMoveSafe(1.0, 0.0, jump, true);
               mc.options.keyUp.setDown(safe);
               mc.options.keySprint.setDown(safe);
               mc.options.keyJump.setDown(safe && jump);
               if (facing && !safe) {
                  AiCombat.ignoreUntil(this.target, now + 15000000000L);
                  this.target = null;
                  if (!autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(0.0, 0.0)) {
                     autismclient.ai.engine.AiMotionSafety.applyEmergencyBrake();
                  }

                  return this.running("Leaving a chase whose movement would touch lava");
               } else {
                  return this.running("Running at the " + BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).getPath() + " (" + (int)distance + "m)");
               }
            }

            float yawToEntity(Entity entity) {
               return (float)(Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90.0);
            }

            AiOperation.Result reachElevatedTarget(AiCombat.Inner_DataUtil plan, double distance, double reach) {
               if (this.travelLeg == null) {
                  BlockPos under = this.groundUnder(this.target);
                  if (under == null) {
                     return this.failAirborne("no standable, lava-free ground under the target");
                  }

                  if (plan == AiCombat.Inner_DataUtil.PILLAR) {
                     int needed = AiCombat.pillarHeightToReach(this.target.getY() - mc.player.getY(), reach);
                     if (needed < 0 || this.pillarBlocksUsed + needed > 3) {
                        return this.failAirborne("the target out-climbs the 3-block pillar cap");
                     }

                     if (!AiCombat.isSafeStep(mc, under)) {
                        return this.failAirborne("unsafe footing under the target");
                     }

                     if (!this.hasScaffoldBlock()) {
                        return this.failAirborne("no hotbar construction blocks to pillar with");
                     }

                     this.travelLeg = AiGoto.travelToCompat(under.above(needed), 0.8, false, true, false, 8);
                     this.legIsPillar = true;
                     this.pillarBlocksUsed += needed;
                  } else {
                     this.travelLeg = AiGoto.travelToCompat(under, 1.2, false, false, false, 8);
                     this.legIsPillar = false;
                  }
               }

               AiOperation.Result legResult = this.travelLeg.tick();
               if (legResult.terminal()) {
                  boolean pillar = this.legIsPillar;
                  this.cancelChild();
                  return legResult.status() != AiOperation.Status.SUCCEEDED
                     ? this.failAirborne(pillar ? "the pillar climb could not complete" : "no route to the ground under the target")
                     : this.running(pillar ? "Pillared up under the target" : "Under the target; re-evaluating");
               } else {
                  return this.running(this.legIsPillar ? "Pillaring up to the target" : "Moving under the target (" + (int)distance + "m)");
               }
            }

            BlockPos groundUnder(Entity entity) {
               BlockPos at = entity.blockPosition();

               for (int drop = 0; drop < 12; at = at.below()) {
                  if (!AiTerrain.isLoaded(mc.level, at)) {
                     return null;
                  }

                  if (mc.level.getFluidState(at).is(FluidTags.LAVA)) {
                     return null;
                  }

                  if (!this.isPassable(at)) {
                     return null;
                  }

                  if (this.isPassable(at.above()) && !this.isPassable(at.below())) {
                     return at;
                  }

                  drop++;
               }

               return null;
            }

            boolean isPassable(BlockPos cell) {
               return mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty();
            }

            boolean hasScaffoldBlock() {
               for (int slot = 0; slot < 9; slot++) {
                  ItemStack stack = mc.player.getInventory().getItem(slot);
                  if (stack.getItem() instanceof BlockItem && autismclient.ai.engine.AiRestock.isScaffoldBlockId(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
                     return true;
                  }
               }

               return AiPlanks.plankYield(mc.player.getInventory()) > 0;
            }

            AiOperation.Result waitForAirborneMob() {
               if (!AiThreats.isLegacyCombatEnabled() && this.blazeWaitTicks <= 400) {
                  Entity blaze = null;
                  double wait = radius + 16.0;

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if ((entity instanceof Blaze || entity instanceof Enderman)
                        && entity.isAlive()
                        && this.matchesTargetFilter(entity)
                        && entity.distanceToSqr(mc.player) <= wait * wait) {
                        blaze = entity;
                        break;
                     }
                  }

                  if (blaze == null) {
                     return null;
                  }

                  double reachNow = AiDefense.attackReach(mc);
                  if (blaze.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= reachNow * reachNow) {
                     this.ignoredTargets.remove(blaze.getId());
                     this.target = blaze;
                     this.trackingTicks = 0;
                     this.outOfReachTicks = 0;
                     return this.running("In reach: fighting the " + BuiltInRegistries.ENTITY_TYPE.getKey(blaze.getType()).getPath());
                  }

                  this.blazeWaitTicks++;
                  this.cancelChild();
                  this.releaseMovementKeys();
                  Entity due = AiDefense.hasShield(mc) ? AiThreats.findImminentShooter(mc, 12) : null;
                  this.aim.aimAt(autismclient.ai.engine.AiAim.bestEntityAimPoint(due != null ? due : blaze));
                  this.aim.tick();
                  boolean up = due != null;
                  if (up != this.shieldRaised) {
                     this.shieldRaised = up;
                     mc.options.keyUse.setDown(up);
                  }

                  if (this.blazeWaitTicks % 40 == 0) {
                     this.ignoredTargets.remove(blaze.getId());
                  }

                  return this.running("Waiting for the blaze to come into reach");
               } else {
                  return null;
               }
            }

            AiOperation.Result failAirborne(String why) {
               if (this.target instanceof Blaze || this.target instanceof Enderman) {
                  AiCombat.ignoreUntil(this.target, System.nanoTime() + 2000000000L);
                  this.target = null;
                  AiOperation.Result waiting = this.waitForAirborneMob();
                  if (waiting != null) {
                     return waiting;
                  }
               }

               this.airborneReason = "unreachable_airborne";
               return this.fail(
                  AiFailure.UNREACHABLE_AIRBORNE, "Target is airborne out of melee reach (" + why + "); use a bow, carry construction blocks, or fight it where it must descend"
               );
            }

            boolean canStrafe() {
               if (this.onPerch) {
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
                  return false;
               } else {
                  return this.pillarBlocksUsed == 0 || this.target == null || this.target.onGround();
               }
            }

            void strafeAround() {
               if (++this.strafeFlipTicks >= 12) {
                  this.strafeFlipTicks = 0;
                  this.strafeRight = mc.player.getRandom().nextBoolean();
               }

               boolean rightSafe = this.isSideStepSafe(true);
               boolean leftSafe = this.isSideStepSafe(false);
               if (this.strafeRight && !rightSafe) {
                  this.strafeRight = false;
               } else if (!this.strafeRight && !leftSafe) {
                  this.strafeRight = true;
               }

               boolean safe = (this.strafeRight ? rightSafe : leftSafe) && autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(0.0, this.strafeRight ? -1.0 : 1.0);
               mc.options.keyUp.setDown(false);
               mc.options.keyDown.setDown(false);
               mc.options.keyLeft.setDown(safe && !this.strafeRight);
               mc.options.keyRight.setDown(safe && this.strafeRight);
               mc.options.keySprint.setDown(false);
            }

            boolean isSideStepSafe(boolean right) {
               float yaw = mc.player.getYRot() + (right ? 90.0F : -90.0F);
               double radians = Math.toRadians(yaw);
               Vec3 step = mc.player.position().add(-Math.sin(radians), 0.0, Math.cos(radians));
               if (!AiCombat.isSafeStep(mc, BlockPos.containing(step))) {
                  return false;
               }

               if (autismclient.ai.engine.AiCombatMotion.predictEndBox(mc, 0.0, right ? -1.0 : 1.0, 6) == null) {
                  return false;
               }

               if (this.target == null) {
                  return true;
               }

               double reach = AiCombat.effectiveReach(AiDefense.attackReach(mc), false);
               Vec3 eye = mc.player.getEyePosition().add(step.x - mc.player.getX(), 0.0, step.z - mc.player.getZ());
               return this.target.getBoundingBox().distanceToSqr(eye) <= reach * reach && this.canWalkTo(step);
            }

            boolean isForwardStepSafe() {
               double radians = Math.toRadians(mc.player.getYRot());
               return AiCombat.isSafeStep(
                  mc, BlockPos.containing(mc.player.getX() - Math.sin(radians), mc.player.getY(), mc.player.getZ() + Math.cos(radians))
               );
            }

            void retreatFrom(Entity source) {
               Float safeRetreat = source instanceof Creeper creeper ? AiBlastEscape.retreatYaw(mc, creeper) : null;
               if (safeRetreat != null) {
                  AiBlastEscape.retreat(mc, safeRetreat);
               } else {
                  Vec3 away = mc.player.position().subtract(source.position());
                  if (away.lengthSqr() < 1.0E-4) {
                     away = new Vec3(1.0, 0.0, 0.0);
                  }

                  float yaw = (float)(Math.toDegrees(Math.atan2(away.z, away.x)) - 90.0);
                  mc.player.setYRot(yaw);
                  if (this.isForwardStepSafe() && autismclient.ai.engine.AiMotionSafety.isPlayerMoveSafe(1.0, 0.0, false, mc.player.getFoodData().getFoodLevel() > 6)) {
                     mc.options.keyLeft.setDown(false);
                     mc.options.keyRight.setDown(false);
                     mc.options.keyUp.setDown(true);
                     mc.options.keySprint.setDown(mc.player.getFoodData().getFoodLevel() > 6);
                  } else {
                     this.strafeAround();
                  }
               }
            }

            boolean hasRoomToBackOff(Entity from) {
               double dx = mc.player.getX() - from.getX();
               double dz = mc.player.getZ() - from.getZ();
               double len = Math.sqrt(dx * dx + dz * dz);
               if (len < 0.001) {
                  return false;
               }

               dx /= len;
               dz /= len;
               int y = mc.player.getBlockY();

               for (double d = 1.0; d <= 2.0; d++) {
                  BlockPos cell = BlockPos.containing(mc.player.getX() + dx * d, y, mc.player.getZ() + dz * d);
                  if (!AiTerrain.isLoaded(mc.level, cell)) {
                     return false;
                  }

                  if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()
                     || !mc.level.getBlockState(cell.above()).getCollisionShape(mc.level, cell.above()).isEmpty()) {
                     return false;
                  }

                  if (!mc.level.getFluidState(cell).isEmpty()) {
                     return false;
                  }

                  if (mc.level.getBlockState(cell.below()).getCollisionShape(mc.level, cell.below()).isEmpty()) {
                     return false;
                  }
               }

               return true;
            }

            boolean hasHeadroom() {
               BlockPos over = mc.player.blockPosition().above(2);
               return mc.level.getBlockState(over).getCollisionShape(mc.level, over).isEmpty();
            }

            void tryCritJump() {
               boolean nearlyReady = mc.player.getAttackStrengthScale(0.0F) > 0.6F;
               double reach = AiCombat.effectiveReach(AiDefense.attackReach(mc), false);
               double forward = (mc.options.keyUp.isDown() ? 1 : 0) - (mc.options.keyDown.isDown() ? 1 : 0);
               double strafe = (mc.options.keyLeft.isDown() ? 1 : 0) - (mc.options.keyRight.isDown() ? 1 : 0);
               boolean attempt = nearlyReady
                  && mc.player.onGround()
                  && !this.onPerch
                  && !AiCombat.nearFarmland(mc)
                  && !mc.player.isInWater()
                  && !mc.player.onClimbable()
                  && !mc.player.isPassenger()
                  && !mc.player.hasEffect(MobEffects.BLINDNESS)
                  && !this.shieldRaised
                  && !this.projectileIncoming()
                  && this.hasHeadroom()
                  && AiCombat.reachableFromJumpApex(this.target.getBoundingBox(), mc.player.getEyePosition(), reach);
               boolean safe = attempt && autismclient.ai.engine.AiMotionSafety.isPlayerMoveSafe(forward, strafe, true, mc.player.isSprinting());
               if (attempt && !safe) {
                  this.lavaJumpsRefused++;
               }

               mc.options.keyJump.setDown(safe);
            }

            void selectBestWeapon() {
               int slot = AiItems.bestWeaponSlot(36);
               if (slot >= 0 && mc.player.getInventory().getSelectedSlot() != slot) {
                  AiItems.selectOrMoveToHotbar(slot);
               }
            }

            void updateShield() {
               boolean hasShield = AiDefense.hasShield(mc);
               boolean wantShield = hasShield && (AiCombat.findFiringBlaze(mc) != null || mc.player.getAttackStrengthScale(0.0F) < 0.92F && this.projectileIncoming());
               if (wantShield != this.shieldRaised) {
                  this.shieldRaised = wantShield;
                  if (wantShield) {
                     mc.options.keyUse.setDown(true);
                  } else {
                     mc.options.keyUse.setDown(false);
                  }
               }
            }

            void lowerShield() {
               if (this.shieldRaised) {
                  this.shieldRaised = false;
                  mc.options.keyUse.setDown(false);
               }
            }

            boolean noThreatsNearby() {
               return !this.projectileIncoming() && AiSurvival.nearestThreat(mc) == null;
            }

            AiOperation healOperation() {
               return new AiOperation() {
                  final int startCount = AiItems.countInInventory(healTarget);
                  int waitTicks;
                  boolean healCancelled;

                  @Override
                  public AiOperation.Result tick() {
                     if (!this.healCancelled && mc.player != null) {
                        if (AiItems.countInInventory(healTarget) < this.startCount) {
                           return this.finish(AiOperation.Status.SUCCEEDED);
                        }

                        if (++this.waitTicks > 60) {
                           return this.finish(AiOperation.Status.FAILED);
                        }

                        if (AiItems.equipHotbar(healTarget) < 0) {
                           return this.finish(AiOperation.Status.FAILED);
                        }

                        mc.options.keyUse.setDown(true);
                        return new AiOperation.Result(AiOperation.Status.RUNNING, "Using " + healWith, "{}", List.of());
                     } else {
                        return this.finish(AiOperation.Status.CANCELLED);
                     }
                  }

                  AiOperation.Result finish(AiOperation.Status status) {
                     if (mc.options != null) {
                        mc.options.keyUse.setDown(false);
                     }

                     return new AiOperation.Result(status, status.name(), "{}", List.of());
                  }

                  @Override
                  public void cancel() {
                     this.healCancelled = true;
                     if (mc.options != null) {
                        mc.options.keyUse.setDown(false);
                     }
                  }
               };
            }

            boolean projectileIncoming() {
               for (AiProjectiles.IncomingProjectile shot : AiProjectiles.incoming(mc)) {
                  if (AiProjectiles.isImminent(shot) && AiDefense.canBlock(mc, mc.level.getEntity(shot.entityId())) && AiDefense.shieldReadyFor(mc, AiCombat.ticksToClosest(shot))) {
                     return true;
                  }
               }

               return false;
            }

            Entity selectTarget() {
               if (explicitId >= 0) {
                  Entity explicit = mc.level.getEntity(explicitId);
                  if (explicit != null && this.explicitTargetUuid != null && this.explicitTargetUuid.equals(explicit.getUUID())) {
                     Long until = this.ignoredTargets.get(this.explicitTargetUuid);
                     return explicit != null && explicit.isAlive() && (until == null || until <= System.nanoTime()) ? explicit : null;
                  } else {
                     return null;
                  }
               } else {
                  List<Entity> found = new ArrayList<>();
                  List<AiCombat.Inner_StreamUtil> candidates = new ArrayList<>();
                  double eyeReach = AiDefense.attackReach(mc) * 0.8;
                  long now = System.nanoTime();
                  this.ignoredTargets.values().removeIf(until -> until < now);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity != mc.player && entity.isAlive() && entity instanceof LivingEntity living && this.matchesTargetFilter(entity)) {
                        if (!AiThreats.isLegacyCombatEnabled() && AiCombat.isInDeepWater(entity) && entity instanceof Mob swimmer && AiThreats.horizontalGapToAttackBox(swimmer, mc.player.getBoundingBox()) > 1.0) {
                           AiCombat.ignoreUntil(entity, now + 15000000000L);
                        } else if (AiCombat.isEngageable(mc, entity)) {
                           double distanceSq = entity.distanceToSqr(mc.player);
                           if (!(distanceSq > radius * radius)) {
                              if (!AiThreats.isLegacyCombatEnabled() && !entity.onGround() && entity.getBoundingBox().minY - mc.player.getEyeY() > eyeReach) {
                                 distanceSq += 400.0;
                              }

                              found.add(entity);
                              candidates.add(
                                 new AiCombat.Inner_StreamUtil(distanceSq, living.getHealth(), living.getMaxHealth(), AiThreats.isActiveThreat(mc, entity), AiThreats.recentlyDamagedPlayer(entity))
                              );
                           }
                        }
                     }
                  }

                  if (candidates.isEmpty()) {
                     return null;
                  }

                  Comparator<AiCombat.Inner_StreamUtil> order = AiCombat.targetOrder(priority);
                  int best = 0;
                  double range = AiCombat.effectiveReach(AiDefense.attackReach(mc), true);

                  for (int i = 1; i < candidates.size(); i++) {
                     boolean in = found.get(i).getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= range * range;
                     boolean bestIn = found.get(best).getBoundingBox().distanceToSqr(mc.player.getEyePosition()) <= range * range;
                     boolean preferred = (this.onPerch || hold) && in != bestIn ? in : order.compare(candidates.get(i), candidates.get(best)) < 0;
                     if (preferred
                        || order.compare(candidates.get(i), candidates.get(best)) == 0 && in == bestIn && found.get(i).getId() < found.get(best).getId()) {
                        best = i;
                     }
                  }

                  return found.get(best);
               }
            }

            Entity findMeleeStriker() {
               Entity best = null;
               double bestGap = 0.5;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Mob mob && mob.isAlive() && entity instanceof Enemy && !(mob.distanceToSqr(mc.player) > 36.0)) {
                     AiThreats.ThreatKind profile = AiThreats.classify(entity);
                     if ((AiThreats.isMeleeThreat(profile) || profile == AiThreats.ThreatKind.BLAZE) && AiThreats.isActiveThreat(mc, entity) && mc.player.hasLineOfSight(entity)) {
                        double gap = AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox());
                        if (gap < bestGap) {
                           bestGap = gap;
                           best = entity;
                        }
                     }
                  }
               }

               return best;
            }

            void switchTarget(Entity next, String why) {
               if (next != this.target) {
                  Entity previous = this.target;
                  this.target = next;
                  this.defensiveDetour = explicitId >= 0 && next.getId() != explicitId || !this.matchesTargetFilter(next);
                  if (!this.defensiveDetour && next instanceof LivingEntity requested) {
                     this.deaths.watch(requested);
                  }

                  this.targetSelectedTick = this.tickCount;
                  this.targetSwitches++;
                  this.selectionReason = why;
                  this.cancelChild();
                  this.trackingTicks = this.outOfReachTicks = 0;
                  this.next = 0;
                  this.exposePoint = null;
                  AiRunTrace.emitServiceEvent(
                     "combat_target_switch",
                     AiJson.objectOf(
                        "from", previous == null ? -1 : previous.getId(), "to", next.getId(), "reason", why, "defensiveDetour", this.defensiveDetour, "heldTicks", 12
                     )
                  );
               }
            }

            boolean isInMeleeContact(Entity entity) {
               return entity instanceof Mob mob && mob.isAlive() && AiThreats.horizontalGapToAttackBox(mob, mc.player.getBoundingBox()) < 0.5;
            }

            boolean matchesTargetFilter(Entity entity) {
               if (!(!sheepColors.isEmpty() && entity instanceof Sheep sheep)
                  || !sheep.isSheared() && sheepColors.contains(sheep.getColor().getSerializedName())) {
                  return !types.isEmpty()
                     ? types.contains(entity.getType())
                     : entity instanceof Enemy
                        && (
                           AiThreats.isLegacyCombatEnabled()
                              || AiThreats.classify(entity) != AiThreats.ThreatKind.AVOID && (!AiThreats.isNormallyPassiveOrNeutral(AiThreats.classify(entity)) || AiThreats.isActiveThreat(mc, entity))
                        );
               } else {
                  return false;
               }
            }

            void cancelChild() {
               if (this.travelLeg != null) {
                  this.childUncertain = this.childUncertain | this.travelLeg.requiresReconciliationAfterEmergency();
                  this.travelLeg.cancel();
                  this.travelLeg = null;
               }
            }

            void releaseAllInputs() {
               if (mc.options != null) {
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
                  mc.options.keyJump.setDown(false);
                  mc.options.keySprint.setDown(false);
                  if (this.shieldRaised) {
                     mc.options.keyUse.setDown(false);
                     this.shieldRaised = false;
                  }
               }
            }

            JsonObject summary() {
               JsonObject out = AiJson.objectOf(
                  "target",
                  spec,
                  "killed",
                  this.killed,
                  "requested",
                  kills,
                  "untilClear",
                  untilClear,
                  "partialCompletion",
                  this.killed > 0 && this.killed < kills,
                  "swings",
                  this.swings,
                  "targetSwitches",
                  this.targetSwitches,
                  "selectionReason",
                  this.selectionReason,
                  "defensiveDetour",
                  this.defensiveDetour,
                  "lavaJumpsRefused",
                  this.lavaJumpsRefused,
                  "health",
                  mc.player == null ? 0.0F : mc.player.getHealth(),
                  "healthLost",
                  Math.max(0.0F, this.startHealth - (mc.player == null ? this.startHealth : mc.player.getHealth())),
                  "lowestHealth",
                  this.lowestHealth,
                  "shieldEquipped",
                  mc.player != null && AiDefense.hasShield(mc)
               );
               if (this.retreatReason != null) {
                  out.addProperty("retreatReason", this.retreatReason);
               }

               if (this.airborneReason != null) {
                  out.addProperty("airborneReason", this.airborneReason);
               }

               out.add("killedTypes", this.deaths.killedTypes());
               out.addProperty("pendingChangesNeedObservation", this.childUncertain);
               if (!this.childUncertainty.isEmpty()) {
                  out.add("pendingChild", this.childUncertainty);
               }

               if (this.target != null && mc.player != null) {
                  out.add(
                     "engaging",
                     AiJson.objectOf(
                        "entityId",
                        this.target.getId(),
                        "type",
                        BuiltInRegistries.ENTITY_TYPE.getKey(this.target.getType()).toString(),
                        "distance",
                        Math.round(mc.player.distanceTo(this.target) * 10.0F) / 10.0,
                        "health",
                        this.target instanceof LivingEntity living ? living.getHealth() : 0
                     )
                  );
               }

               ItemStack weapon = mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandItem();
               if (!weapon.isEmpty()) {
                  out.add(
                     "weapon",
                     AiJson.objectOf(
                        "item",
                        BuiltInRegistries.ITEM.getKey(weapon.getItem()).toString(),
                        "durabilityLeft",
                        weapon.isDamageableItem() ? weapon.getMaxDamage() - weapon.getDamageValue() : -1
                     )
                  );
               }

               return out;
            }

            AiOperation.Result holdPerch() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof Creeper creeper && creeper.isAlive() && creeper.distanceToSqr(mc.player) < 25.0) {
                     if (this.perchTravelOp != null) {
                        this.perchTravelOp.cancel();
                        this.perchTravelOp = null;
                     }

                     return this.fail(AiFailure.TARGET_GONE, "A creeper at the pillar; leaving the perch to the guard");
                  }
               }

               if (this.onPerch) {
                  if (mc.player.onGround() && mc.player.getY() < this.perchBase.getY() + perch - 0.5) {
                     this.releaseAllInputs();
                     mc.options.keyShift.setDown(false);
                     if (++this.perchKnockoffs > 2) {
                        return this.fail(AiFailure.UNREACHABLE, "Knocked off the perch three times");
                     }

                     this.onPerch = false;
                     this.pillarLandingTicks = 0;
                     this.shieldBlockCount = 0;
                     if (this.shieldPlacing) {
                        this.placer.reset();
                        this.shieldPlacing = false;
                     }

                     return this.running("Knocked off the perch; back up");
                  } else {
                     if (!mc.player.onGround() && !mc.player.isInWater()) {
                        this.releaseAllInputs();
                        mc.options.keyShift.setDown(true);
                        return this.running("Landing on the pillar before choosing cover");
                     }

                     if (mc.player.onGround() && this.pillarCentreTicks < 40) {
                        double cx = this.perchBase.getX() + 0.5;
                        double cz = this.perchBase.getZ() + 0.5;
                        double dx = cx - mc.player.getX();
                        double dz = cz - mc.player.getZ();
                        if (dx * dx + dz * dz > 0.0144) {
                           this.pillarCentreTicks++;
                           int[] keys = autismclient.ai.engine.AiMotionSafety.relativeMoveAxes(
                              mc.player.getYRot(), autismclient.ai.engine.AiPhysics.yawTowards(mc.player.getX(), mc.player.getZ(), cx, cz)
                           );
                           mc.options.keyShift.setDown(true);
                           if (!autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(keys[0], -keys[1])) {
                              autismclient.ai.engine.AiMotionSafety.applyEmergencyBrake();
                              this.pillarCentreTicks = 40;
                              return this.running("Holding the safe side of the pillar");
                           }

                           mc.options.keyUp.setDown(keys[0] > 0);
                           mc.options.keyDown.setDown(keys[0] < 0);
                           mc.options.keyRight.setDown(keys[1] > 0);
                           mc.options.keyLeft.setDown(keys[1] < 0);
                           mc.options.keyShift.setDown(true);
                           return this.running("Stepping to the middle of the pillar");
                        }

                        this.pillarCentreTicks = 40;
                        this.releaseAllInputs();
                        mc.options.keyShift.setDown(true);
                     }

                     return this.holdGround();
                  }
               } else {
                  if (this.perchTravelOp == null) {
                     this.releaseAllInputs();
                     if (!mc.player.onGround() && ++this.pillarLandingTicks < 10) {
                        return this.running("Landing before the pillar");
                     }

                     int up = this.currentPerchHeight();
                     if (up >= perch) {
                        this.perchBase = mc.player.blockPosition().below(perch);
                        this.onPerch = true;
                        this.pillarCentreTicks = 0;
                        return this.running("On the pillar");
                     }

                     this.perchBase = mc.player.blockPosition().below(up);

                     for (int h = 2; h <= perch - up + 1; h++) {
                        BlockPos over = mc.player.blockPosition().above(h);
                        if (!mc.level.getBlockState(over).getCollisionShape(mc.level, over).isEmpty()) {
                           return this.fail(AiFailure.UNREACHABLE, "No head room for a pillar here");
                        }
                     }

                     this.perchTravelOp = AiGoto.travelToCompat(this.perchBase.above(perch), 0.5, false, true, false, 4);
                  }

                  AiOperation.Result r = this.perchTravelOp.tick();
                  if (!r.terminal()) {
                     return this.running("Pillaring up out of reach");
                  } else {
                     this.perchTravelOp.cancel();
                     this.perchTravelOp = null;
                     AutismClientAddon.LOG.info("[AI] fight: perch {} ({}) at {}", r.status(), r.message(), mc.player.blockPosition().toShortString());
                     if (r.status() == AiOperation.Status.SUCCEEDED && !(mc.player.getY() < this.perchBase.getY() + perch - 0.5)) {
                        this.onPerch = true;
                        this.pillarCentreTicks = 0;
                        return this.running("On the pillar");
                     } else {
                        return this.fail(AiFailure.UNREACHABLE, "Could not pillar up out of reach: " + r.message());
                     }
                  }
               }
            }

            int currentPerchHeight() {
               BlockPos feet = mc.player.blockPosition();
               if (mc.level.getBlockState(feet.below()).getCollisionShape(mc.level, feet.below()).isEmpty()) {
                  return 0;
               }

               int height = 2147483647;

               for (Direction side : Plane.HORIZONTAL) {
                  int open = 0;

                  for (int d = 0; d <= 6; d++) {
                     BlockPos cell = feet.relative(side).below(d);
                     if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                        break;
                     }

                     open++;
                  }

                  height = Math.min(height, Math.max(0, open - 1));
               }

               return height == 2147483647 ? 0 : height;
            }

            AiOperation.Result holdGround() {
               if (perch == 0 && this.tickCount % 10 == 0 && Boolean.getBoolean("autism.ai.quick")) {
                  System.out.println("[AI quick] hold support " + AiGuard.hasSafeFootingAroundPlayer() + " at " + mc.player.blockPosition().toShortString());
               }

               if (perch == 0 && !AiGuard.hasSafeFootingAroundPlayer()) {
                  return this.fail(AiFailure.UNREACHABLE, "Holding ground is unsafe here");
               }

               Entity shooter = AiShield.rangedShooterBeyond(mc, AiDefense.attackReach(mc) * 0.92);
               if (shooter == null && this.shieldPlacing && this.shieldShooter != null && this.shieldShooter.isAlive() && ++this.shooterMemoryTicks <= 12) {
                  shooter = this.shieldShooter;
               } else if (shooter != null) {
                  this.shooterMemoryTicks = 0;
               }

               if (shooter != null) {
                  if (!AiShield.hasEnemyWithin(mc, 6.0)) {
                     this.releaseAllInputs();
                     return this.finish(
                        AiOperation.Status.SUCCEEDED,
                        "A " + BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath() + " shoots from out of reach; leaving it to the guard"
                     );
                  }

                  AiOperation.Result shielding = this.shieldAgainst(shooter);
                  if (shielding != null) {
                     return shielding;
                  }
               } else if (this.shieldPlacing) {
                  this.placer.reset();
                  this.shieldPlacing = false;
               }

               boolean near = false;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity != mc.player
                     && entity.isAlive()
                     && this.matchesTargetFilter(entity)
                     && entity.distanceToSqr(mc.player) <= radius * radius
                     && mc.player.hasLineOfSight(entity)) {
                     near = true;
                     break;
                  }
               }

               this.noneNearTicks = near ? 0 : this.noneNearTicks + 1;
               if (this.noneNearTicks >= 40) {
                  return this.finish(
                     AiOperation.Status.SUCCEEDED, this.killed > 0 ? "Killed " + this.killed + " holding ground; none left near" : "None left near"
                  );
               }

               if (this.target != null
                  && !(Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition())) > 3.5)
                  && mc.player.hasLineOfSight(this.target)) {
                  this.keepDistanceTicks = 0;
               } else {
                  this.keepDistanceTicks++;
               }

               if (this.keepDistanceTicks > 200) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "The mobs keep their distance; going on");
               }

               if (perch == 0 && this.target != null) {
                  double d = Math.sqrt(this.target.getBoundingBox().distanceToSqr(mc.player.getEyePosition()));
                  if (this.stallTargetId == this.target.getId() && !(d <= 3.5) && !(d < this.stallBestDistance - 1.0)) {
                     if (++this.stallTicks > 60) {
                        long until = System.nanoTime() + 15000000000L;

                        for (Entity entity : mc.level.entitiesForRendering()) {
                           if (entity != mc.player
                              && entity.isAlive()
                              && this.matchesTargetFilter(entity)
                              && entity.distanceToSqr(mc.player) <= radius * radius
                              && entity.getBoundingBox().distanceToSqr(mc.player.getEyePosition()) > 12.25) {
                              AiCombat.ignoreUntil(entity, until);
                           }
                        }

                        this.releaseAllInputs();
                        return this.finish(AiOperation.Status.SUCCEEDED, "The mobs come no closer; going on");
                     }
                  } else {
                     this.stallTargetId = this.target.getId();
                     this.stallBestDistance = d;
                     this.stallTicks = 0;
                  }
               }

               return null;
            }

            double targetDistance() {
               return this.target == null ? 1.7976931348623157E308 : mc.player.distanceTo(this.target);
            }

            Vec3 findExposurePoint() {
               List<Vec3> edges = new ArrayList<>();
               edges.add(AiCombat.edgePointToward(this.perchBase, this.target.position()));
               double x = this.perchBase.getX() + 0.5;
               double z = this.perchBase.getZ() + 0.5;

               for (int dx = -1; dx <= 1; dx++) {
                  for (int dz = -1; dz <= 1; dz++) {
                     if (dx != 0 || dz != 0) {
                        edges.add(new Vec3(x + dx * 0.56, mc.player.getY(), z + dz * 0.56));
                     }
                  }
               }

               Vec3 best = null;
               double bestDistance = 1.7976931348623157E308;
               int bestExposure = 2147483647;

               for (Vec3 edge : edges) {
                  double distance = Math.hypot(edge.x - mc.player.getX(), edge.z - mc.player.getZ());
                  if (this.canWalkTo(edge)) {
                     Vec3 eye = mc.player.getEyePosition().add(edge.x - mc.player.getX(), 0.0, edge.z - mc.player.getZ());
                     int exposure = 0;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof Enemy
                           && entity.isAlive()
                           && autismclient.ai.engine.AiSight.isRangedShooter(entity)
                           && entity.distanceTo(mc.player) <= 16.5
                           && autismclient.ai.engine.AiSight.hasLineOfSight(mc.level, entity.getEyePosition(), eye, -9223372036854775808L, -9223372036854775808L)) {
                           exposure++;
                        }
                     }

                     if (exposure < bestExposure || exposure == bestExposure && distance < bestDistance) {
                        best = edge;
                        bestDistance = distance;
                        bestExposure = exposure;
                     }
                  }
               }

               return best;
            }

            boolean canWalkTo(Vec3 edge) {
               double dx = edge.x - mc.player.getX();
               double dz = edge.z - mc.player.getZ();

               for (int i = 1; i <= 4; i++) {
                  if (!mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(dx * i / 4.0, 0.0, dz * i / 4.0))) {
                     return false;
                  }
               }

               Vec3 eye = mc.player.getEyePosition().add(dx, 0.0, dz);
               return autismclient.ai.engine.AiAim.canHitEntityFrom(this.target, eye, autismclient.ai.engine.AiAim.bestEntityAimPointFrom(this.target, eye));
            }

            void sneakToward(double x, double z) {
               int[] keys = autismclient.ai.engine.AiMotionSafety.relativeMoveAxes(
                  mc.player.getYRot(), autismclient.ai.engine.AiPhysics.yawTowards(mc.player.getX(), mc.player.getZ(), x, z)
               );
               mc.options.keyShift.setDown(true);
               if (!autismclient.ai.engine.AiMotionSafety.isHorizontalMoveSafe(keys[0], -keys[1])) {
                  autismclient.ai.engine.AiMotionSafety.applyEmergencyBrake();
               } else {
                  mc.options.keyUp.setDown(keys[0] > 0);
                  mc.options.keyDown.setDown(keys[0] < 0);
                  mc.options.keyRight.setDown(keys[1] > 0);
                  mc.options.keyLeft.setDown(keys[1] < 0);
                  mc.options.keyShift.setDown(true);
               }
            }

            AiOperation.Result shieldAgainst(Entity shooter) {
               if (!this.onPerch) {
                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof Enemy && entity.isAlive() && !autismclient.ai.engine.AiSight.isRangedShooter(entity) && mc.player.distanceTo(entity) <= 3.2) {
                        if (this.shieldPlacing) {
                           this.placer.reset();
                           this.shieldPlacing = false;
                        }

                        return null;
                     }
                  }
               }

               if (this.shieldPlacing) {
                  if (this.onPerch
                     && this.placer.targetPos() != null
                     && mc.player.getBoundingBox().intersects(new AABB(this.placer.targetPos()))
                     && this.shieldStanceTicks < 40
                     && mc.player.onGround()) {
                     double x = this.perchBase.getX() + 0.5;
                     double z = this.perchBase.getZ() + 0.5;
                     this.sneakToward(x, z);
                     this.shieldStanceTicks++;
                     return this.running("Clearing the shield cell");
                  }

                  if (this.onPerch
                     && this.shieldStanceTicks < 12
                     && this.placer.targetPos() != null
                     && !autismclient.ai.engine.AiPlacer.canPlaceFromHere(this.placer.targetPos(), AiShield.bestBlock(mc))) {
                     BlockPos cell = this.placer.targetPos();
                     if (mc.player.onGround()) {
                        Vec3 edge = AiCombat.edgePointToward(this.perchBase, Vec3.atCenterOf(cell));
                        double x = edge.x;
                        double z = edge.z;
                        if (Math.hypot(x - mc.player.getX(), z - mc.player.getZ()) > 0.04) {
                           this.sneakToward(x, z);
                           this.shieldStanceTicks++;
                           return this.running("Exposing the pillar's support face");
                        }
                     }
                  }

                  this.releaseAllInputs();
                  if (this.onPerch) {
                     mc.options.keyShift.setDown(true);
                  }

                  autismclient.ai.engine.AiPlacer.PlaceStatus s = this.placer.tick();
                  if (s == autismclient.ai.engine.AiPlacer.PlaceStatus.WORKING) {
                     return this.running("Shielding the pillar from the " + BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath());
                  }

                  this.shieldPlacing = false;
                  if (s == autismclient.ai.engine.AiPlacer.PlaceStatus.PLACED) {
                     this.shieldBlockCount++;
                     this.pillarCentreTicks = 0;
                     this.exposePoint = null;
                     this.next = 0;
                     AutismClientAddon.LOG
                        .info(
                           "[AI] fight: shield block at {} against the {}",
                           this.placer.targetPos().toShortString(),
                           BuiltInRegistries.ENTITY_TYPE.getKey(shooter.getType()).getPath()
                        );
                  } else if (this.placingBackstop) {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] fight: backstop failed at {}: {}", this.placer.targetPos(), this.placer.failureReason());
                     }

                     this.backstopFailed = true;
                  } else {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] fight: shield failed at {}: {} (body {})",
                              this.placer.targetPos(),
                              this.placer.failureReason(),
                              mc.player.position()
                           );
                     }

                     this.shieldBlockCount = 8;
                  }

                  return this.running("Checking the shield");
               } else {
                  if (this.shieldBlockCount >= 8) {
                     return null;
                  }

                  Item block = AiShield.bestBlock(mc);
                  BlockPos behind = this.onPerch && block != null && !this.backstopFailed ? AiShield.behindCell(mc, shooter) : null;
                  BlockPos cell = block == null ? null : (behind != null ? behind : AiShield.coverCell(mc, shooter));
                  this.placingBackstop = behind != null;
                  if (cell == null) {
                     if (Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] fight: no shield cell after {} blocks (body {}, shooter {})", this.shieldBlockCount, mc.player.position(), shooter.position());
                     }

                     this.shieldBlockCount = 8;
                     return null;
                  } else {
                     this.placer.start(cell, block);
                     this.shieldShooter = shooter;
                     this.shooterMemoryTicks = 0;
                     this.shieldPlacing = true;
                     this.shieldStanceTicks = 0;
                     return this.shieldAgainst(shooter);
                  }
               }
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.summary().toString(), List.of());
            }

            void recordChildResult(AiOperation.Result child) {
               if (child.status() == AiOperation.Status.UNCERTAIN) {
                  this.childUncertain = true;
                  this.childUncertainty = AiJson.objectOf(
                     "status",
                     child.status().name(),
                     "message",
                     child.message(),
                     "failure",
                     child.failure().name(),
                     "data",
                     AiRunResources.parseEvidenceJson(child.dataJson())
                  );
               }
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.summary();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(pending, new AiOperation.Result(status, message, data.toString(), List.of()));
            }

            AiOperation.Result fail(AiFailure code, String message) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               JsonObject data = this.summary();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(pending, AiOperation.Result.failed(code, message, data.toString()));
            }

            @Override
            public boolean canInterruptNow() {
               return !this.placer.needsReconciliation()
                  && (this.travelLeg == null || this.travelLeg.canInterruptNow())
                  && (this.perchTravelOp == null || this.perchTravelOp.canInterruptNow())
                  && (this.eatOp == null || this.eatOp.canInterruptNow())
                  && (this.healOp == null || this.healOp.canInterruptNow())
                  && (this.dodgeOp == null || this.dodgeOp.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.childUncertain
                  || this.placer.needsReconciliation()
                  || this.travelLeg != null && this.travelLeg.requiresReconciliationAfterEmergency()
                  || this.perchTravelOp != null && this.perchTravelOp.requiresReconciliationAfterEmergency()
                  || this.eatOp != null && this.eatOp.requiresReconciliationAfterEmergency()
                  || this.healOp != null && this.healOp.requiresReconciliationAfterEmergency()
                  || this.dodgeOp != null && this.dodgeOp.requiresReconciliationAfterEmergency();
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.cancelChild();
                  if (this.perchTravelOp != null) {
                     this.perchTravelOp.cancel();
                     this.perchTravelOp = null;
                  }

                  if (this.dodgeOp != null) {
                     this.dodgeOp.cancel();
                     this.dodgeOp = null;
                  }

                  if (this.eatOp != null) {
                     this.eatOp.cancel();
                     this.eatOp = null;
                  }

                  if (this.healOp != null) {
                     this.healOp.cancel();
                     this.healOp = null;
                  }

                  this.releaseAllInputs();
                  if (this.onPerch && mc.options != null) {
                     mc.options.keyShift.setDown(false);
                  }

                  if (this.shieldPlacing) {
                     this.placer.reset();
                     this.shieldPlacing = false;
                  }

                  this.aim.clear();
                  if (mc.level == this.startLevel && mc.player == this.startPlayer && mc.player.getInventory().getSelectedSlot() != this.startSlot) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.startSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Combat requires a connected living player");
      }
   }

   static Set<EntityType<?>> parseTargets(String spec) {
      String clean = spec.toLowerCase(Locale.ROOT).trim();
      if (!clean.isEmpty() && !clean.equals("hostiles") && !clean.equals("hostile") && !clean.equals("monsters")) {
         Set<EntityType<?>> types = new LinkedHashSet<>();

         for (String part : clean.split("\\s*,\\s*")) {
            if (!part.isEmpty()) {
               Identifier id = Identifier.tryParse(part.contains(":") ? part : "minecraft:" + part);
               if (id != null) {
                  BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(types::add);
               }
            }
         }

         if (!types.isEmpty()) {
            return Set.copyOf(types);
         }

         List<String> near = new ArrayList<>();

         for (Identifier key : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            if (key.getPath().contains(clean)) {
               near.add(key.toString());
            }

            if (near.size() >= 5) {
               break;
            }
         }

         throw new IllegalArgumentException(
            "Unknown entity type '"
               + spec
               + "'"
               + (near.isEmpty() ? "" : " (did you mean " + String.join(", ", near) + "?)")
               + "; use a registry id, a comma-separated list, or \"hostiles\""
         );
      } else {
         return Set.of();
      }
   }

   enum Inner_CachePool {
      NEAREST,
      WEAKEST,
      STRONGEST,
      ATTACKER;

      static AiCombat.Inner_CachePool parse(String text) {
         try {
            return valueOf(text.trim().toUpperCase(Locale.ROOT));
         } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("priority must be nearest, weakest, strongest or attacker");
         }
      }
   }

   enum Inner_DataUtil {
      GROUND,
      APPROACH_UNDER,
      PILLAR,
      UNREACHABLE;
   }

   record Inner_StreamUtil(double distanceSqr, float health, float maxHealth, boolean attacking, boolean hurtPlayer) {
   }
}
