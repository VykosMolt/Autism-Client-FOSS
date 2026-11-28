package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;

final class AiSpeedrun {
   static final Map<String, Integer> KIT = load();
   static final int init_I = 16;
   static final int load_I = 32;
   static final long QUICK_FAIL_NANOS = 5000000000L;
   static final int read_I = 6;
   static final int write = 8;
   static final int update = 6;
   static final int sync = 16;
   static final int flush = 10;
   static final int close = 6;
   static final int open = 240;
   static final int poll = 6;
   static final int push = 32;
   static final int pop = 600;
   static final float HEALTH_FLOOR = 10.0F;
   static final int tick = 16;
   static final int run = 8;
   static final int get = 16;
   static final int set = 12;
   static final int put = 6;
   static final int add = 3;
   static final int clear = 3;
   static final long load_J = 60000000000L;
   static final int reset = 100;
   static final Map<String, String> MOB_DROPS = Map.of(
      "minecraft:cow",
      "minecraft:beef",
      "minecraft:mooshroom",
      "minecraft:beef",
      "minecraft:pig",
      "minecraft:porkchop",
      "minecraft:sheep",
      "minecraft:mutton",
      "minecraft:chicken",
      "minecraft:chicken",
      "minecraft:rabbit",
      "minecraft:rabbit"
   );
   static final Map<String, String> COOKED = Map.of(
      "minecraft:beef",
      "minecraft:cooked_beef",
      "minecraft:porkchop",
      "minecraft:cooked_porkchop",
      "minecraft:mutton",
      "minecraft:cooked_mutton",
      "minecraft:chicken",
      "minecraft:cooked_chicken",
      "minecraft:rabbit",
      "minecraft:cooked_rabbit"
   );

   private AiSpeedrun() {
   }

   private static Map<String, Integer> load() {
      Map<String, Integer> kit = new LinkedHashMap<>();
      kit.put("minecraft:diamond_axe", 1);
      kit.put("minecraft:diamond_shovel", 1);
      kit.put("minecraft:diamond_helmet", 1);
      kit.put("minecraft:diamond_chestplate", 1);
      kit.put("minecraft:diamond_leggings", 1);
      kit.put("minecraft:golden_boots", 1);
      kit.put("minecraft:bucket", 3);
      kit.put("minecraft:shield", 1);
      kit.put("minecraft:flint_and_steel", 1);
      return kit;
   }

   static Map<String, Integer> ironGearTargets(AiProgression.Inner_CachePool pick, int ironPickCount, Map<String, Integer> missing) {
      Map<String, Integer> targets = new LinkedHashMap<>();
      if (pick.ordinal() < AiProgression.Inner_CachePool.IRON.ordinal()) {
         targets.put("minecraft:iron_pickaxe", Math.max(0, ironPickCount) + 1);
      }

      for (String id : List.of("minecraft:shield", "minecraft:bucket", "minecraft:flint_and_steel")) {
         if (missing.containsKey(id)) {
            targets.put(id, missing.get(id));
         }
      }

      return targets;
   }

   static boolean shouldTopUpFood(boolean attempted, int nutrition, int secondsLeft, Map<String, Integer> missing, Map<String, Integer> targets) {
      return !attempted && !targets.isEmpty() && nutrition >= 32 && secondsLeft >= 60 && nutrition < 40 && foodTarget(missing) == 40;
   }

   static int breadGoal(int bread, int wheat, int hay, int needed) {
      int available = (Math.max(0, wheat) + 9 * Math.max(0, hay)) / 3;
      return Math.max(0, bread) + Math.min(Math.max(0, needed), available);
   }

   static AiCraftPlanner.CraftPlan breadPlan(AiCraftPlanner.RecipeGraph graph, int needed) {
      int held = graph.held("minecraft:bread");
      int goal = breadGoal(held, graph.held("minecraft:wheat"), graph.held("minecraft:hay_block"), needed);
      if (goal <= held) {
         return null;
      }

      AiCraftPlanner.CraftPlan plan = AiCraftPlanner.init(graph, "minecraft:bread", goal);
      return plan.complete() && !plan.steps().isEmpty() ? plan : null;
   }

   static boolean mayTryHay(int trips, int secondsLeft) {
      return trips < 2 && secondsLeft >= 60;
   }

   static int foodTarget(Map<String, Integer> missing) {
      return missing.keySet().stream().anyMatch(id -> id.startsWith("minecraft:diamond_")) ? 40 : 32;
   }

   static int eyeGoal(int eyes, int pearls, int missing) {
      return Math.min(32, Math.max(0, eyes) + Math.max(0, pearls) + Math.max(1, missing));
   }

   static boolean shouldCookNow(int hunger, int preparedNutrition, int rawMeat, int cookingSteps, int secondsLeft, boolean quiet, AiOperation current) {
      return hunger <= 16
         && preparedNutrition < Math.max(4, 20 - hunger)
         && rawMeat >= 4
         && cookingSteps < 6
         && secondsLeft >= 60
         && quiet
         && (current == null || current.canInterruptNow() && !current.requiresReconciliationAfterEmergency());
   }

   static autismclient.ai.engine.AiPathfinder.PathGoal surfaceGoal(BlockPos surface) {
      return autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(surface, 2.0);
   }

   static BlockPos findSurface(BlockPos from, int x, int z, Function<BlockPos, BlockPos> observedSurface) {
      for (int quarter = 4; quarter >= 1; quarter--) {
         int cx = from.getX() + (int)Math.round((x - from.getX()) * quarter / 4.0);
         int cz = from.getZ() + (int)Math.round((z - from.getZ()) * quarter / 4.0);

         for (int radius = 0; radius <= 4; radius++) {
            BlockPos best = null;
            double nearest = 1.0 / 0.0;

            for (int dx = -radius; dx <= radius; dx++) {
               for (int dz = -radius; dz <= radius; dz++) {
                  if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                     BlockPos column = new BlockPos(cx + dx, from.getY(), cz + dz);
                     if (!(Math.hypot(column.getX() - from.getX(), column.getZ() - from.getZ()) < 8.0)) {
                        BlockPos surface = observedSurface.apply(column);
                        if (surface != null) {
                           double distance = surface.distSqr(from);
                           if (distance < nearest) {
                              best = surface;
                              nearest = distance;
                           }
                        }
                     }
                  }
               }
            }

            if (best != null) {
               return best;
            }
         }
      }

      return null;
   }

   static BlockPos observedSurface(Minecraft mc, BlockPos column) {
      if (!mc.level.hasChunkAt(column)) {
         return null;
      }

      int y = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
      BlockPos surface = new BlockPos(column.getX(), y, column.getZ());
      return AiGoto.isStandableCompat(surface)
            && mc.level.getFluidState(surface).isEmpty()
            && mc.level.getFluidState(surface.above()).isEmpty()
            && !mc.level.getBlockState(surface.below()).is(BlockTags.LEAVES)
            && !mc.level.getBlockState(surface.below()).is(BlockTags.LOGS)
         ? surface
         : null;
   }

   static boolean withinPhaseBudget(Map<AiSpeedrun.RunPhase, Integer> attemptsByPhase, AiSpeedrun.RunPhase phase, int budget) {
      return withinPhaseBudget(attemptsByPhase, new HashMap<>(), phase, budget, System.nanoTime());
   }

   static boolean withinPhaseBudget(
      Map<AiSpeedrun.RunPhase, Integer> attemptsByPhase, Map<AiSpeedrun.RunPhase, Long> lastAtNanos, AiSpeedrun.RunPhase phase, int budget, long now
   ) {
      return withinPhaseBudget(attemptsByPhase, lastAtNanos, new HashMap<>(), phase, budget, now);
   }

   static boolean withinPhaseBudget(
      Map<AiSpeedrun.RunPhase, Integer> attemptsByPhase,
      Map<AiSpeedrun.RunPhase, Long> lastAtNanos,
      Map<AiSpeedrun.RunPhase, Integer> repeatsByPhase,
      AiSpeedrun.RunPhase phase,
      int budget,
      long now
   ) {
      Long last = lastAtNanos.get(phase);
      if (last == null || now - last > 60000000000L || repeatsByPhase.merge(phase, 1, Integer::sum) >= 100) {
         attemptsByPhase.merge(phase, 1, Integer::sum);
         lastAtNanos.put(phase, now);
         repeatsByPhase.put(phase, 0);
      }

      return attemptsByPhase.getOrDefault(phase, 0) <= budget;
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(600, Math.min(14400, AiJson.getInt(args, "timeoutSeconds", 7200)));
         AiOracle.enable(true);
         AiNetherEntry.init_Z = false;
         AiNetherRun.init_Z = false;
         return new AiOperation() {
            final AiRunDiagnostics diagnostics = AiRunDiagnostics.start();
            final Object campaignToken = AiRunResources.beginCampaign();
            final String worldKey = AiWorldIdentity.currentWorldKey(mc);
            final UUID playerId = mc.player.getUUID();
            final Object connection = mc.getConnection();
            final long deathRevision = AiServerEvidence.deathRevision();
            final int startTick = autismclient.util.AutismSessionState.getInstance().getClientTick();
            final long startNanos = System.nanoTime();
            final long deadline = this.startNanos + timeoutSeconds * 1000000000L;
            final Map<AiSpeedrun.RunPhase, Integer> failuresByPhase = new LinkedHashMap<>();
            final Map<String, Integer> failuresByMessage = new LinkedHashMap<>();
            final Map<AiSpeedrun.RunPhase, Integer> uncertainByPhase = new LinkedHashMap<>();
            final Map<AiSpeedrun.RunPhase, Long> uncertainLastAt = new LinkedHashMap<>();
            final Map<AiSpeedrun.RunPhase, Integer> uncertainRepeats = new LinkedHashMap<>();
            final Map<AiSpeedrun.RunPhase, Long> phaseNanos = new LinkedHashMap<>();
            final JsonArray legs = new JsonArray();
            AiOperation phaseOp;
            AiSpeedrun.RunPhase phase;
            long phaseStartNanos;
            boolean logStockTried;
            boolean unusedFoodTripStarted;
            boolean armourEquipTried;
            boolean campaignEnded;
            boolean woodenAxeTried;
            boolean ironBootstrapTried;
            final AiSpeedrun.TableCarry tableCarry = new AiSpeedrun.TableCarry();
            int foodTrips;
            long nextCheckpointAt;
            String dimension = AiWorldIdentity.currentDimensionId(mc);
            int netherFoodTrips;
            int nightBurrows;
            int unusedCounter;
            int cookFailures;
            int foodReserveAttempts;
            int breadAttempts;
            boolean pearlRecoveryPending;
            long failedNightBurrowDay = -1L;
            boolean nightBurrowing;
            boolean foodShortLogged;
            int pearlRecoveryReturns;
            int pearlRecoveryGoal;
            long nextFoodTripAt;
            long healCooldownUntil;
            final AiGuardLayer guardLayer = new AiGuardLayer(
               AiGuard.GuardPolicy.standard()
                  .keeping(
                     List.of(
                        "minecraft:blaze_rod",
                        "minecraft:ender_pearl",
                        "minecraft:ender_eye",
                        "minecraft:diamond",
                        "minecraft:gold_ingot",
                        "minecraft:iron_ingot"
                     )
                  ),
               "speedrun"
            );
            String lastFailure = "";
            float lastHealth = -1.0F;
            int lastFood = -1;
            int heartbeatTicks;
            final ArrayDeque<String> motionTrail = new ArrayDeque<>();
            boolean fallLogged;
            String lastMessage = "";
            long pearlHuntCooldownUntil;
            boolean pearlHunting;
            int mealAttempts;
            int healFailures;
            boolean guardLayerActive;
            AiOperation emergencyOp;
            boolean emergencyNeedsReconcile;
            boolean steppingAside;
            int portalStepAsides;
            long legStartNanos;
            BlockPos quickFailFeet;
            int set;
            boolean finished;

            void trackVitals() {
               float health = mc.player.getHealth();
               int food = mc.player.getFoodData().getFoodLevel();
               if (this.lastHealth >= 0.0F && health < this.lastHealth - 0.01F) {
                  DamageSource source = mc.player.getLastDamageSource();
                  AiSpeedrun.log(
                     "[AI] speedrun hurt {} -> {} by {} in {} at {}",
                     String.format("%.1f", this.lastHealth),
                     String.format("%.1f", health),
                     source == null
                        ? "?"
                        : source.getMsgId()
                           + (source.getEntity() == null ? "" : " " + BuiltInRegistries.ENTITY_TYPE.getKey(source.getEntity().getType()).getPath()),
                     this.phase,
                     mc.player.blockPosition().toShortString()
                  );
               }

               if (food != this.lastFood && (food <= 14 || this.lastFood <= 14)) {
                  AiSpeedrun.log("[AI] speedrun food {} (health {})", food, String.format("%.1f", health));
               }

               this.lastHealth = health;
               this.lastFood = food;
               Options o = mc.options;
               this.motionTrail
                  .addLast(
                     String.format(
                        "%d %.1f,%.1f,%.1f %s%s%s%s%s%s%s%s",
                        mc.player.tickCount,
                        mc.player.getX(),
                        mc.player.getY(),
                        mc.player.getZ(),
                        mc.player.onGround() ? "g" : "a",
                        o.keyUp.isDown() ? "W" : "",
                        o.keyLeft.isDown() ? "A" : "",
                        o.keyDown.isDown() ? "S" : "",
                        o.keyRight.isDown() ? "D" : "",
                        o.keyJump.isDown() ? "J" : "",
                        o.keyShift.isDown() ? "C" : "",
                        o.keySprint.isDown() ? "R" : ""
                     )
                  );

               while (this.motionTrail.size() > 30) {
                  this.motionTrail.removeFirst();
               }

               if (!mc.player.onGround() && mc.player.fallDistance > 6.0 && !this.fallLogged) {
                  this.fallLogged = true;
                  AiSpeedrun.log(
                     "[AI] speedrun falling {} in {} during '{}': {}",
                     String.format("%.1f", mc.player.fallDistance),
                     this.phase,
                     AiSpeedrun.clip(this.lastMessage),
                     String.join(" | ", this.motionTrail)
                  );
               } else if (mc.player.onGround()) {
                  this.fallLogged = false;
               }
            }

            @Override
            public AiOperation.Result tick() {
               this.diagnostics.init(this.phase == null ? "START" : this.phase.name(), this.lastMessage);
               if (mc.player != null && mc.level != null) {
                  if (!this.worldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || this.connection != mc.getConnection() || !this.playerId.equals(mc.player.getUUID())) {
                     return this.init(AiOperation.Status.CANCELLED, "World or player identity changed; revalidate before resuming");
                  }

                  if (mc.player.isAlive() && AiServerEvidence.deathRevision() == this.deathRevision) {
                     AiRunResources.refreshReservations();
                     if (this.phase != null) {
                        AiGoal.init(this, 0, "Beating the game", AiGoal.load(this.phase.name()), "");
                     }

                     String dimension = AiWorldIdentity.currentDimensionId(mc);
                     if (!this.dimension.equals(dimension)) {
                        this.dimension = dimension;
                        this.armourEquipTried = false;
                        this.portalStepAsides = 0;
                        this.foodReserveAttempts = 0;
                     }

                     if (System.nanoTime() >= this.nextCheckpointAt) {
                        this.nextCheckpointAt = System.nanoTime() + 10000000000L;
                        this.saveCheckpoint("RUNNING");
                     }

                     if (System.nanoTime() > this.deadline) {
                        return this.init(
                           this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                           "Out of time in phase " + this.phase
                        );
                     }

                     this.trackVitals();
                     if (++this.heartbeatTicks >= 600) {
                        this.heartbeatTicks = 0;
                        AiSpeedrun.log(
                           "[AI] speedrun heartbeat {} at {} health {} after {} s: {} [endermen {}, piglins {}, pearls {}, rods {}, eyes {}, food {}, time {}]",
                           this.phase,
                           mc.player.blockPosition().toShortString(),
                           String.format("%.1f", mc.player.getHealth()),
                           this.elapsedSeconds(),
                           AiSpeedrun.clip(this.lastMessage),
                           AiWorldScan.findEntitiesByType("minecraft:enderman", 64.0).size(),
                           AiWorldScan.findEntitiesByType("minecraft:piglin", 64.0).size(),
                           AiCraft.countItem("minecraft:ender_pearl"),
                           AiCraft.countItem("minecraft:blaze_rod"),
                           AiCraft.countItem("minecraft:ender_eye"),
                           this.safeFoodItems(),
                           mc.level.getOverworldClockTime() % 24000L
                        );
                     }

                     AiOperation pearlOp = this.phaseOp != null && !this.pearlHunting && this.shouldHuntEnderman() && this.phaseOp.canInterruptNow()
                        ? AiForage.read(32.0)
                        : null;
                     if (pearlOp != null) {
                        AiSpeedrun.log(
                           "[AI] speedrun: an enderman within {} blocks in {}; hunting it for a pearl ({} held)",
                           16,
                           this.phase,
                           AiCraft.countItem("minecraft:ender_pearl")
                        );
                        this.phaseOp.cancel();
                        this.phaseOp = null;
                        this.pearlHuntCooldownUntil = System.nanoTime() + 60000000000L;
                        this.pearlHunting = true;
                        return this.init(this.phase, pearlOp, null);
                     }

                     if (this.phaseOp != null && this.shouldHealNow()) {
                        AiSpeedrun.log(
                           "[AI] speedrun: health {} in {} with nothing hostile near; healing before going on",
                           String.format("%.1f", mc.player.getHealth()),
                           this.phase
                        );
                        this.phaseOp.cancel();
                        this.phaseOp = null;
                        this.healCooldownUntil = 0L;
                     }

                     if (this.phaseOp != null
                        && this.emergencyOp == null
                        && this.phase != AiSpeedrun.RunPhase.NETHER
                        && this.phase != AiSpeedrun.RunPhase.STRONGHOLD
                        && this.phase != AiSpeedrun.RunPhase.END) {
                        this.emergencyOp = AiRunDiagnostics.trace("survival", "campaign emergency", AiGuardLayer.createEmergencyEscapeOp(mc));
                        if (this.emergencyOp != null) {
                           this.emergencyNeedsReconcile = this.phaseOp.requiresReconciliationAfterEmergency();
                           this.phaseOp.cancel();
                           this.phaseOp = null;

                           for (KeyMapping key : new KeyMapping[]{
                              mc.options.keyUp,
                              mc.options.keyDown,
                              mc.options.keyLeft,
                              mc.options.keyRight,
                              mc.options.keyJump,
                              mc.options.keyShift,
                              mc.options.keySprint,
                              mc.options.keyAttack,
                              mc.options.keyUse
                           }) {
                              key.setDown(false);
                           }

                           AiSpeedrun.log(
                              "[AI] speedrun: {} at {} in {}; answering it first",
                              mc.player.isInLava() ? "in lava" : (AiBreathe.init(mc.player) ? "out of air" : "on fire"),
                              mc.player.blockPosition().toShortString(),
                              this.phase
                           );
                        }
                     }

                     if (this.emergencyOp != null) {
                        AiOperation.Result e = this.emergencyOp.tick();
                        if (!e.terminal()) {
                           return this.running(this.phase + ": " + e.message());
                        }

                        this.emergencyOp.cancel();
                        this.emergencyOp = null;
                        if (this.emergencyNeedsReconcile || e.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Emergency handled; interrupted action requires fresh reconciliation before resuming");
                        }
                     }

                     if (this.phaseOp != null && this.guardLayerActive) {
                        AiOperation.Result g = this.guardLayer.tick(this.phaseOp, () -> {
                           this.phaseOp.cancel();
                           this.phaseOp = null;
                           this.guardLayerActive = false;
                        }, this::secondsLeft);
                        if (g != null) {
                           return g.terminal() ? this.init(g.status(), g.message()) : g;
                        }
                     }

                     if (this.phaseOp != null) {
                        AiOperation.Result r = this.phaseOp.tick();
                        if (!r.terminal()) {
                           this.lastMessage = r.message();
                           return this.running(this.phase + ": " + r.message());
                        }

                        this.phaseOp.cancel();
                        this.phaseOp = null;
                        if (this.phase == AiSpeedrun.RunPhase.COOK) {
                           this.cookFailures = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.cookFailures + 1;
                        }

                        AiSpeedrun.log("[AI] speedrun step {} in {}: {}", r.status(), this.phase, AiSpeedrun.clip(r.message()));
                        if (this.nightBurrowing) {
                           this.nightBurrowing = false;
                           if (r.status() != AiOperation.Status.SUCCEEDED) {
                              this.failedNightBurrowDay = mc.level.getOverworldClockTime() / 24000L;
                           }
                        }

                        this.legs
                           .add(
                              AiJson.objectOf(
                                 "phase",
                                 this.phase.name(),
                                 "status",
                                 r.status().name(),
                                 "message",
                                 AiSpeedrun.clip(r.message()),
                                 "seconds",
                                 (System.nanoTime() - this.legStartNanos) / 1000000000L,
                                 "tick",
                                 autismclient.util.AutismSessionState.getInstance().getClientTick(),
                                 "data",
                                 AiRunResources.compactEvidenceJson(r.dataJson())
                              )
                           );

                        while (this.legs.size() > 256) {
                           this.legs.remove(0);
                        }

                        AiRunTrace.emitServiceEvent("speedrun_leg", this.legs.get(this.legs.size() - 1).getAsJsonObject());
                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.init(AiOperation.Status.CANCELLED, "Cancelled during " + this.phase);
                        }

                        if (r.status() == AiOperation.Status.UNCERTAIN
                           && AiSpeedrun.withinPhaseBudget(this.uncertainByPhase, this.uncertainLastAt, this.uncertainRepeats, this.phase, 3, System.nanoTime())) {
                           AiSpeedrun.log(
                              "[AI] speedrun: {} could not be established in {} ({} of {}); observing the world again",
                              AiSpeedrun.clip(r.message()),
                              this.phase,
                              this.uncertainByPhase.get(this.phase),
                              3
                           );
                           return this.nextPhase();
                        }

                        if (r.status() == AiOperation.Status.UNCERTAIN) {
                           return this.init(AiOperation.Status.UNCERTAIN, this.phase + ": " + r.message());
                        }

                        JsonObject legEvidence = AiRunResources.parseEvidenceJson(r.dataJson());
                        boolean pearlRouteChange = this.phase == AiSpeedrun.RunPhase.STRONGHOLD
                           && r.status() == AiOperation.Status.FAILED
                           && AiJson.getBoolean(legEvidence, "pearlRouteChange", false);
                        if (pearlRouteChange) {
                           if (this.pearlRecoveryReturns >= 1) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "Pearl supplies remain short after an Overworld search and a Nether recovery; the route needs a new strategy"
                              );
                           }

                           this.pearlRecoveryReturns++;
                           this.pearlRecoveryGoal = AiSpeedrun.eyeGoal(
                              AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), AiJson.getInt(legEvidence, "pearlsNeeded", 1)
                           );
                           this.pearlRecoveryPending = true;
                           AiSpeedrun.log("[AI] speedrun: bounded surface pearl search exhausted; Nether recovery for {} total eyes/pearls", this.pearlRecoveryGoal);
                        }

                        if (this.phase == AiSpeedrun.RunPhase.END
                           && r.status() == AiOperation.Status.SUCCEEDED
                           && !this.steppingAside
                           && !this.pearlHunting
                           && mc.level.dimension() == Level.OVERWORLD) {
                           return this.init(
                              AiOperation.Status.SUCCEEDED, "Beat the game: the dragon is dead and the exit portal taken, in " + this.elapsedSeconds() + " s"
                           );
                        }

                        boolean lowHealth = r.status() != AiOperation.Status.SUCCEEDED
                           && this.phase != AiSpeedrun.RunPhase.HEAL
                           && (r.failure() == AiFailure.LOW_HEALTH || mc.player.getHealth() <= 6.0F);
                        if (lowHealth) {
                           this.healCooldownUntil = 0L;
                        }

                        boolean wasPearlLeg = this.pearlHunting || this.steppingAside;
                        this.pearlHunting = false;
                        this.steppingAside = false;
                        boolean quickFail = r.status() != AiOperation.Status.SUCCEEDED
                           && r.status() != AiOperation.Status.CANCELLED
                           && System.nanoTime() - this.legStartNanos < 5000000000L;
                        BlockPos feetNow = mc.player.blockPosition();
                        boolean stuck = quickFail && feetNow.equals(this.quickFailFeet);
                        this.quickFailFeet = quickFail ? feetNow : null;
                        if (stuck && this.set < 6 && !wasPearlLeg) {
                           this.set++;
                           this.quickFailFeet = null;
                           AiSpeedrun.log(
                              "[AI] speedrun: {} failed at once twice from {}; moving off first ({} of {})",
                              this.phase,
                              feetNow.toShortString(),
                              this.set,
                              6
                           );
                           this.steppingAside = true;
                           return this.init(this.phase, this.stepAside(), null);
                        }

                        if (this.phase == AiSpeedrun.RunPhase.HEAL
                           && r.status() != AiOperation.Status.SUCCEEDED
                           && r.status() != AiOperation.Status.CANCELLED) {
                           this.healFailures++;
                        }

                        if (r.status() != AiOperation.Status.SUCCEEDED
                           && !lowHealth
                           && this.phase != AiSpeedrun.RunPhase.FOOD
                           && this.phase != AiSpeedrun.RunPhase.HEAL
                           && this.phase != AiSpeedrun.RunPhase.COOK
                           && !wasPearlLeg
                           && !pearlRouteChange) {
                           this.lastFailure = this.phase + ": " + AiSpeedrun.clip(r.message());
                           int n = this.failuresByPhase.merge(this.phase, 1, Integer::sum);
                           int same = this.failuresByMessage.merge(this.phase + ": " + AiSpeedrun.clip(r.message()), 1, Integer::sum);
                           if (n >= 6 || same >= 3) {
                              return this.init(
                                 AiOperation.Status.FAILED,
                                 "Phase "
                                    + this.phase
                                    + " failed "
                                    + (same >= 3 ? same + " times the same way" : n + " times")
                                    + ": "
                                    + AiSpeedrun.clip(r.message())
                              );
                           }
                        }
                     }

                     if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                        return this.running("Landing");
                     } else {
                        AiOperation.Result g = this.guardLayer.tick(null, () -> {}, this::secondsLeft);
                        if (g != null) {
                           this.lastMessage = g.message();
                           return g;
                        } else {
                           AiOperation.Result heal = this.healStep();
                           return heal != null ? heal : this.nextPhase();
                        }
                     }
                  } else {
                     return this.init(
                        AiOperation.Status.FAILED, "The deathless attempt ended: server/player death was observed in " + this.phase
                     );
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            boolean shouldHuntEnderman() {
               if (this.phase != AiSpeedrun.RunPhase.NETHER_ENTRY
                  && this.phase != AiSpeedrun.RunPhase.FOOD
                  && this.phase != AiSpeedrun.RunPhase.COOK) {
                  return false;
               }

               if (mc.level.dimension() == Level.OVERWORLD && this.armourEquipped() && !(mc.player.getHealth() < 14.0F)) {
                  if (System.nanoTime() < this.pearlHuntCooldownUntil) {
                     return false;
                  }

                  if (AiCraft.countItem("minecraft:ender_pearl") + AiCraft.countItem("minecraft:ender_eye") >= 16) {
                     return false;
                  }

                  if (AiForage.findEntity(mc, "minecraft:enderman", 16.0) != null) {
                     return true;
                  }

                  for (Entity e : AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0)) {
                     if (mc.player.hasLineOfSight(e)) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            }

            boolean shouldHealNow() {
               return this.phase != AiSpeedrun.RunPhase.HEAL
                  && this.phase != AiSpeedrun.RunPhase.FOOD
                  && mc.level.dimension() == Level.OVERWORLD
                  && mc.player.getHealth() < 10.0F
                  && System.nanoTime() > this.healCooldownUntil
                  && mc.player.onGround()
                  && AiGuard.sampleStatus().hostilesNear() == 0
                  && this.phaseOp.canInterruptNow();
            }

            AiOperation.Result healStep() {
               if (mc.player.getHealth() >= 12.0F) {
                  this.healFailures = 0;
               }

               if (!(mc.player.getHealth() >= 12.0F) && System.nanoTime() >= this.healCooldownUntil) {
                  if (mc.player.getFoodData().getFoodLevel() < 18 && AiSurvival.init(mc.player.getInventory()) >= 0 && this.mealAttempts++ < 3) {
                     AiOperation.Result meal = this.init(AiSpeedrun.RunPhase.HEAL, AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20)), null);
                     this.guardLayerActive = true;
                     return meal;
                  } else {
                     this.mealAttempts = 0;
                     if (mc.player.getFoodData().getFoodLevel() < 18
                        && AiSurvival.init(mc.player.getInventory()) < 0
                        && mc.level.dimension() == Level.OVERWORLD
                        && System.nanoTime() > this.nextFoodTripAt) {
                        this.nextFoodTripAt = System.nanoTime() + 180000000000L;
                        return this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(10, 240), null);
                     } else if (this.healFailures > 0) {
                        this.healCooldownUntil = System.nanoTime() + 10000000000L;
                        AiOperation.Result rest = this.init(AiSpeedrun.RunPhase.HEAL, AiCombat.init(200), null);
                        this.guardLayerActive = true;
                        return rest;
                     } else {
                        this.healCooldownUntil = System.nanoTime() + 60000000000L;
                        return (mc.player.getFoodData().getFoodLevel() >= 18 || mc.player.getHealth() <= 6.0F) && mc.level.dimension() != Level.END
                           ? this.init(
                              AiSpeedrun.RunPhase.HEAL,
                              AiGuard.allowDuringLowHealth(AiBurrow.init(AiJson.objectOf("healTo", 18, "stay", true, "timeoutSeconds", 180))),
                              null
                           )
                           : null;
                     }
                  }
               } else {
                  return null;
               }
            }

            AiOperation stepAside() {
               BlockPos feet = mc.player.blockPosition();
               int[][] dirs = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
               int[] best = dirs[this.set % 4];
               int bestOpen = -1;

               for (int[] d : dirs) {
                  int open = 0;

                  for (int i = 1; i <= 6; i++) {
                     BlockPos c = feet.offset(d[0] * i, 0, d[1] * i);
                     if (mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty() && mc.level.getFluidState(c).isEmpty()) {
                        open++;
                     }
                  }

                  if (open > bestOpen) {
                     bestOpen = open;
                     best = d;
                  }
               }

               int x = feet.getX() + best[0] * 6;
               int z = feet.getZ() + best[1] * 6;
               return AiGoto.travelToCompat(
                  new BlockPos(x, feet.getY(), z),
                  2.0,
                  true,
                  true,
                  true,
                  40,
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                  true,
                  2.0,
                  new AiNetherRun.RadiusGoal(x, z, 3),
                  AiGoto.GotoOptions.DEFAULT
               );
            }

            BlockPos portalClearSpot() {
               BlockPos feet = mc.player.blockPosition();
               List<BlockPos> portals = new ArrayList<>();

               for (BlockPos c : BlockPos.betweenClosed(feet.offset(-11, -5, -11), feet.offset(11, 6, 11))) {
                  if (mc.level.getBlockState(c).is(Blocks.NETHER_PORTAL)) {
                     portals.add(c.immutable());
                  }
               }

               if (!portals.isEmpty() && this.init(feet, portals, 1)) {
                  BlockPos best = null;
                  double bestDistance = 1.7976931348623157E308;

                  for (BlockPos c : BlockPos.betweenClosed(feet.offset(-8, -3, -8), feet.offset(8, 3, 8))) {
                     if (!this.init(c, portals, 2)) {
                        BlockPos floor = c.below();
                        BlockState floorState = mc.level.getBlockState(floor);
                        if (!floorState.getCollisionShape(mc.level, floor).isEmpty()
                           && !floorState.is(Blocks.MAGMA_BLOCK)
                           && !floorState.is(BlockTags.LEAVES)
                           && floorState.getFluidState().isEmpty()) {
                           boolean open = true;

                           for (BlockPos body : List.of(c, c.above())) {
                              BlockState state = mc.level.getBlockState(body);
                              if (!state.getCollisionShape(mc.level, body).isEmpty() || !state.getFluidState().isEmpty() || state.is(BlockTags.FIRE)) {
                                 open = false;
                                 break;
                              }
                           }

                           if (open) {
                              double distance = c.distSqr(feet);
                              if (distance < bestDistance) {
                                 bestDistance = distance;
                                 best = c.immutable();
                              }
                           }
                        }
                     }
                  }

                  return best;
               } else {
                  return null;
               }
            }

            boolean init(BlockPos c, List<BlockPos> cells, int range) {
               for (BlockPos p : cells) {
                  if (Math.abs(p.getX() - c.getX()) <= range && Math.abs(p.getY() - c.getY()) <= range + 1 && Math.abs(p.getZ() - c.getZ()) <= range) {
                     return true;
                  }
               }

               return false;
            }

            AiOperation.Result nextPhase() {
               ResourceKey<Level> dimension = mc.level.dimension();
               if (dimension != Level.END && this.portalStepAsides < 3) {
                  BlockPos clear = this.portalClearSpot();
                  if (clear != null) {
                     this.portalStepAsides++;
                     this.steppingAside = true;
                     AiSpeedrun.log("[AI] speedrun: stepping clear of the portal to {}", clear.toShortString());
                     return this.init(
                        this.phase == null ? AiSpeedrun.RunPhase.WOOD : this.phase,
                        AiGoto.travelToCompat(clear, 0.6, true, false, false, 20),
                        null
                     );
                  }

                  this.portalStepAsides = 0;
               }

               if (dimension == Level.END) {
                  return !AiNetherRun.load_Z()
                     ? this.init(
                        AiOperation.Status.UNCERTAIN,
                        "Already in the End, but the required barter and blaze-spawner route cannot be verified for this campaign"
                     )
                     : this.init(AiSpeedrun.RunPhase.END, AiEndRun.init(AiJson.objectOf("timeoutSeconds", Math.min(3600, this.secondsLeft()))), null);
               }

               if (this.pearlRecoveryPending && AiCraft.countItem("minecraft:ender_eye") + AiCraft.countItem("minecraft:ender_pearl") >= this.pearlRecoveryGoal) {
                  this.pearlRecoveryPending = false;
               }

               if (dimension == Level.NETHER) {
                  return this.init(
                     AiSpeedrun.RunPhase.NETHER,
                     AiNetherRun.create(
                        AiJson.objectOf(
                           "rods",
                           8,
                           "pearls",
                           this.pearlRecoveryPending ? this.pearlRecoveryGoal : 16,
                           "pearlRecovery",
                           this.pearlRecoveryPending,
                           "timeoutSeconds",
                           Math.min(5400, this.secondsLeft())
                        )
                     ),
                     null
                  );
               }

               if (dimension != Level.OVERWORLD) {
                  return this.init(AiOperation.Status.UNCERTAIN, "Campaign entered an unsupported dimension; revalidate the route");
               }

               if (AiNetherRun.init_Z && this.missingGear().isEmpty() && this.armourEquipped()) {
                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  boolean night = dayTime > 13000L && dayTime < 23000L;
                  if (this.safeFoodItems() < 10 && night && this.netherFoodTrips >= 1 && this.nightBurrows < 1 && AiBurrow.init()) {
                     this.nightBurrows++;
                     AiSpeedrun.log("[AI] speedrun: home for food at night (day time {}); a hole until dawn", dayTime);
                     return this.init(AiSpeedrun.RunPhase.FOOD, AiBurrow.init(AiJson.objectOf("timeoutSeconds", 600)), null);
                  }

                  if (this.safeFoodItems() < 10 && this.netherFoodTrips < 3) {
                     this.netherFoodTrips++;
                     return this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(10, 240), null);
                  }

                  AiOperation.Result cook = this.cookStep();
                  if (cook != null) {
                     return cook;
                  }

                  if (AiRunResources.totalSafeFoodNutrition() < 32) {
                     return this.foodReserveAttempts++ >= 4
                        ? this.init(AiOperation.Status.FAILED, "Nether revisit blocked: safe food reserve is still insufficient")
                        : this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(this.safeFoodItems() + Math.max(4, (32 - AiRunResources.totalSafeFoodNutrition() + 5) / 6), 240), null);
                  }

                  if (AiCraft.countItem("minecraft:water_bucket") < 2) {
                     return this.init(AiSpeedrun.RunPhase.KIT, AiObsidian.init(2, Math.min(180, this.secondsLeft())), null);
                  }

                  if (AiRunResources.spareBuildingBlockCount() < 64) {
                     return this.init(
                        AiSpeedrun.RunPhase.KIT,
                        AiObtain.init(AiJson.objectOf("item", "minecraft:cobblestone", "count", 64, "timeoutSeconds", Math.min(180, this.secondsLeft()))),
                        null
                     );
                  }

                  this.netherFoodTrips = 0;
                  this.nightBurrows = 0;
                  return this.init(
                     AiSpeedrun.RunPhase.NETHER_ENTRY,
                     AiNetherEntry.init(AiJson.objectOf("skipVillage", true, "beds", 0, "timeoutSeconds", Math.min(3600, this.secondsLeft()))),
                     null
                  );
               } else {
                  boolean strongholdLeft = AiNetherRun.init_Z() && AiRunResources.blazePowderEquivalents() + AiCraft.countItem("minecraft:ender_pearl") > 0;
                  if (this.pearlRecoveryPending
                     || !strongholdLeft && (!AiNetherRun.load_Z() || AiRunResources.blazePowderEquivalents() < 16 && this.pearlRecoveryReturns <= 0 || !this.missingGear().isEmpty() || !this.armourEquipped())) {
                     Map<String, Integer> missing = this.missingGear();
                     if (!missing.isEmpty()) {
                        int nutritionGoal = AiSpeedrun.foodTarget(missing);
                        if (!this.logStockTried && this.pickaxeCount() == 0 && AiItems.countInInventory(AiItems.resolveItem("log")) < 16) {
                           this.logStockTried = true;
                           return this.init(
                              AiSpeedrun.RunPhase.WOOD, AiObtain.init(AiJson.objectOf("item", "log", "count", 16, "timeoutSeconds", 300)), null
                           );
                        }

                        if (!this.woodenAxeTried
                           && !AiSpeedrun.hasDurableWeapon(mc)
                           && AiRunResources.totalSafeFoodNutrition() < 40
                           && 4 * AiItems.countInInventory(AiItems.resolveItem("log")) + AiItems.countInInventory(AiItems.resolveItem("planks")) >= 12) {
                           this.woodenAxeTried = true;
                           return this.init(
                              AiSpeedrun.RunPhase.KIT,
                              AiObtain.init(AiJson.objectOf("item", "minecraft:wooden_axe", "count", 1, "staged", true, "timeoutSeconds", 60)),
                              null
                           );
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < nutritionGoal && this.breadAttempts < 2) {
                           AiOperation bread = AiSpeedrun.load(Math.max(1, (nutritionGoal - AiRunResources.totalSafeFoodNutrition() + 4) / 5), Math.min(45, this.secondsLeft()));
                           if (bread != null) {
                              this.breadAttempts++;
                              return this.init(AiSpeedrun.RunPhase.COOK, bread, null);
                           }
                        }

                        AiOperation.Result cooked = this.cookStep();
                        if (cooked != null) {
                           return cooked;
                        }

                        Map<String, Integer> bootstrap = AiSpeedrun.ironGearTargets(AiProgression.bestTier(mc), AiCraft.countItem("minecraft:iron_pickaxe"), missing);
                        if (AiSpeedrun.shouldTopUpFood(this.ironBootstrapTried, AiRunResources.totalSafeFoodNutrition(), this.secondsLeft(), missing, bootstrap)) {
                           this.ironBootstrapTried = true;
                           String first = bootstrap.containsKey("minecraft:iron_pickaxe") ? "minecraft:iron_pickaxe" : "minecraft:shield";
                           int firstGoal = bootstrap.getOrDefault(first, Math.max(1, AiCraft.countItem(first)));
                           JsonObject earlyKit = new JsonObject();
                           bootstrap.forEach((id, n) -> {
                              if (!id.equals(first)) {
                                 earlyKit.addProperty(id, n);
                              }
                           });
                           AiSpeedrun.log(
                              "[AI] speedrun: prepare required iron gear before the diamond food reserve ({} nutrition, {} s maximum)",
                              AiRunResources.totalSafeFoodNutrition(),
                              Math.min(600, this.secondsLeft())
                           );
                           return this.init(
                              AiSpeedrun.RunPhase.KIT,
                              AiProgression.obtain(
                                 AiJson.objectOf(
                                    "kit",
                                    earlyKit,
                                    "kitPlanks",
                                    bootstrap.containsKey("minecraft:shield") ? 6 : 0,
                                    "timeoutSeconds",
                                    Math.min(600, this.secondsLeft())
                                 ),
                                 first,
                                 firstGoal
                              ),
                              null
                           );
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < nutritionGoal
                           && this.foodTrips > 0
                           && mc.level.getOverworldClockTime() % 24000L > 13000L
                           && mc.level.getOverworldClockTime() % 24000L < 23000L
                           && this.failedNightBurrowDay != mc.level.getOverworldClockTime() / 24000L
                           && !AiSpeedrun.hasHuntable(mc, 48.0)
                           && AiBurrow.init()) {
                           this.nightBurrowing = true;
                           return this.init(
                              AiSpeedrun.RunPhase.FOOD, AiBurrow.init(AiJson.objectOf("timeoutSeconds", Math.min(600, this.secondsLeft()))), null
                           );
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < nutritionGoal && this.foodTrips < 4) {
                           this.foodTrips++;
                           this.unusedFoodTripStarted = true;
                           int foodGoal = this.safeFoodItems() + Math.max(4, (nutritionGoal - AiRunResources.totalSafeFoodNutrition() + 5) / 6);
                           return this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(foodGoal, 240), null);
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < Math.min(nutritionGoal, 12)) {
                           return this.init(
                              AiOperation.Status.FAILED,
                              "Food preparation could not secure the " + nutritionGoal + " nutrition required for the remaining kit work"
                           );
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < nutritionGoal && !this.foodShortLogged) {
                           this.foodShortLogged = true;
                           AiSpeedrun.log(
                              "[AI] speedrun: {} nutrition of the {} wanted after {} food trips; going on with the kit", AiRunResources.totalSafeFoodNutrition(), nutritionGoal, this.foodTrips
                           );
                        }

                        String main = "minecraft:diamond_pickaxe";
                        JsonObject kit = new JsonObject();
                        missing.forEach((id, n) -> {
                           if (!id.equals(main)) {
                              kit.addProperty(id, n);
                           }
                        });
                        return this.init(
                           AiSpeedrun.RunPhase.KIT,
                           AiProgression.obtain(
                              AiJson.objectOf("kit", kit, "kitPlanks", 8, "timeoutSeconds", this.secondsLeft()), main, missing.getOrDefault(main, 1)
                           ),
                           null
                        );
                     } else if (this.armourEquipped()) {
                        AiOperation carryTable = this.tableCarry
                           .init(AiCraft.countItem("minecraft:crafting_table"), this.secondsLeft(), seconds -> AiProgression.init(seconds, true));
                        if (carryTable != null) {
                           return this.init(AiSpeedrun.RunPhase.KIT, carryTable, null);
                        }

                        if (this.safeFoodItems() < (AiSpeedrun.init_Z_Minecraft_I(mc, 8) ? 6 : 2) && System.nanoTime() > this.nextFoodTripAt) {
                           this.nextFoodTripAt = System.nanoTime() + 600000000000L;
                           return this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(10, 240), null);
                        }

                        AiOperation.Result cook = this.cookStep();
                        if (cook != null) {
                           return cook;
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < 32) {
                           if (this.foodReserveAttempts++ >= 4) {
                              return this.init(AiOperation.Status.FAILED, "Nether entry blocked: safe food reserve could not be replenished");
                           }

                           int goal = this.safeFoodItems() + Math.max(4, (32 - AiRunResources.totalSafeFoodNutrition() + 5) / 6);
                           return this.init(AiSpeedrun.RunPhase.FOOD, AiSpeedrun.secureFood(goal, 240), null);
                        } else if (AiCraft.countItem("minecraft:water_bucket") < 2) {
                           return this.init(AiSpeedrun.RunPhase.KIT, AiObsidian.init(2, Math.min(180, this.secondsLeft())), null);
                        } else {
                           return AiRunResources.spareBuildingBlockCount() < 64
                              ? this.init(
                                 AiSpeedrun.RunPhase.KIT,
                                 AiObtain.init(
                                    AiJson.objectOf("item", "minecraft:cobblestone", "count", 64, "timeoutSeconds", Math.min(180, this.secondsLeft()))
                                 ),
                                 null
                              )
                              : this.init(
                                 AiSpeedrun.RunPhase.NETHER_ENTRY,
                                 AiNetherEntry.init(AiJson.objectOf("skipVillage", true, "beds", 0, "timeoutSeconds", Math.min(3600, this.secondsLeft()))),
                                 null
                              );
                        }
                     } else {
                        if (this.armourEquipTried) {
                           return this.init(AiOperation.Status.FAILED, "Required armor or golden boots were not equipped; the Nether gate remains closed");
                        }

                        this.armourEquipTried = true;
                        return this.init(AiSpeedrun.RunPhase.EQUIP, AiEquip.init(AiJson.objectOf("what", "armor")), null);
                     }
                  } else {
                     AiNetherRun.init_V();
                     return this.init(AiSpeedrun.RunPhase.STRONGHOLD, AiStronghold.init(AiJson.objectOf("timeoutSeconds", Math.min(5400, this.secondsLeft()))), null);
                  }
               }
            }

            AiOperation.Result cookStep() {
               if (AiSpeedrun.rawMeatCounts().isEmpty()) {
                  return null;
               }

               if (this.cookFailures >= 4) {
                  return this.init(AiOperation.Status.FAILED, "Cooking repeatedly failed; raw food cannot satisfy the safe nutrition reserve");
               }

               AiOperation cooking = AiForage.cook(Math.min(600, this.secondsLeft()));
               return cooking == null ? null : this.init(AiSpeedrun.RunPhase.COOK, cooking, null);
            }

            Map<String, Integer> missingGear() {
               return AiRunResources.missingMandatoryGear();
            }

            boolean armourEquipped() {
               return AiRunResources.isMandatoryArmorEquipped();
            }

            boolean init(EquipmentSlot slot, String id) {
               ItemStack stack = mc.player.getItemBySlot(slot);
               return !stack.isEmpty() && BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(id);
            }

            int pickaxeCount() {
               int n = 0;

               for (String id : AiProgression.PICKAXES) {
                  n += AiCraft.countItem(id);
               }

               return n;
            }

            int safeFoodItems() {
               return AiRunResources.totalSafeFoodItems();
            }

            AiOperation.Result init(AiSpeedrun.RunPhase next, AiOperation op, String notBuilt) {
               if (next != this.phase) {
                  if (this.phase != null) {
                     this.phaseNanos.merge(this.phase, System.nanoTime() - this.phaseStartNanos, Long::sum);
                  }

                  this.phase = next;
                  this.phaseStartNanos = System.nanoTime();
                  AiSpeedrun.log("[AI] speedrun phase {}: at {} after {} s", next, mc.player.blockPosition().toShortString(), this.elapsedSeconds());
                  AiRunTrace.emitServiceEvent(
                     "speedrun_phase",
                     AiJson.objectOf(
                        "phase",
                        next.name(),
                        "world",
                        this.worldKey,
                        "dimension",
                        AiWorldIdentity.currentDimensionId(mc),
                        "at",
                        mc.player.blockPosition().toShortString(),
                        "elapsedSeconds",
                        this.elapsedSeconds(),
                        "resources",
                        AiRunResources.inventorySnapshotJson()
                     )
                  );
               }

               if (notBuilt != null) {
                  return this.init(AiOperation.Status.FAILED, "Reached phase " + next + ", but " + notBuilt);
               }

               this.phaseOp = AiRunDiagnostics.trace("campaign", next.name(), op);
               this.guardLayerActive = false;
               this.legStartNanos = System.nanoTime();
               return this.running(next + ": starting");
            }

            int secondsLeft() {
               return (int)Math.max(60L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            long elapsedSeconds() {
               return (System.nanoTime() - this.startNanos) / 1000000000L;
            }

            JsonObject reportJson() {
               JsonObject phases = new JsonObject();
               this.phaseNanos.forEach((p, n) -> phases.addProperty(p.name(), n / 1000000000L));
               if (this.phase != null) {
                  phases.addProperty(
                     this.phase.name(),
                     (this.phaseNanos.getOrDefault(this.phase, 0L) + System.nanoTime() - this.phaseStartNanos) / 1000000000L
                  );
               }

               return AiJson.objectOf(
                  "phase",
                  this.phase == null ? "" : this.phase.name(),
                  "elapsedSeconds",
                  this.elapsedSeconds(),
                  "phases",
                  phases,
                  "steps",
                  this.legs,
                  "lastFailure",
                  this.lastFailure,
                  "world",
                  this.worldKey,
                  "dimension",
                  AiWorldIdentity.currentDimensionId(mc),
                  "simulationTicks",
                  autismclient.util.AutismSessionState.getInstance().getClientTick() - this.startTick,
                  "resources",
                  AiRunResources.inventorySnapshotJson(),
                  "memoryCapabilities",
                  AiWorldMemory.snapshotJson(),
                  "requiredNetherRouteVerified",
                  AiNetherRun.load_Z(),
                  "seedFree",
                  AiOracle.isActive(),
                  "ironBootstrapTried",
                  this.ironBootstrapTried,
                  "pearlRecoveryPending",
                  this.pearlRecoveryPending,
                  "pearlRecoveryGoal",
                  this.pearlRecoveryGoal,
                  "pearlRecoveryReturns",
                  this.pearlRecoveryReturns,
                  "deathless",
                  mc.player != null && mc.player.isAlive() && AiServerEvidence.deathRevision() == this.deathRevision
               );
            }

            void saveCheckpoint(String status) {
               AiService service = AiService.getExistingInstance();
               if (service != null) {
                  JsonObject snapshot = this.reportJson();
                  snapshot.addProperty("status", status);
                  service.saveSpeedrunCheckpoint(snapshot);
               }
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.reportJson());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.finished = true;
               AiGoal.init(this);
               JsonObject debug = this.diagnostics
                  .snapshot(status.name(), this.phase == null ? "START" : this.phase.name(), message);
               if (this.phaseOp != null) {
                  this.phaseOp.cancel();
                  this.phaseOp = null;
               }

               this.guardLayer.cancel();
               if (this.emergencyOp != null) {
                  this.emergencyOp.cancel();
                  this.emergencyOp = null;
               }

               this.saveCheckpoint(status.name());
               JsonObject finalData = this.reportJson();
               finalData.add("diagnostics", debug);
               AiRunTrace.emitServiceEvent("speedrun_outcome", AiJson.objectOf("status", status.name(), "message", message, "report", finalData));
               this.endCampaign();
               AiSpeedrun.log("[AI] speedrun {} after {} s: {}", status, this.elapsedSeconds(), message);
               return AiGameOperations.result(status, message, finalData);
            }

            void endCampaign() {
               if (!this.campaignEnded) {
                  this.campaignEnded = true;
                  AiOracle.enable(false);
                  AiRunResources.endCampaign(this.campaignToken);
               }
            }

            @Override
            public boolean canInterruptNow() {
               return (this.phaseOp == null || this.phaseOp.canInterruptNow())
                  && (this.emergencyOp == null || this.emergencyOp.canInterruptNow())
                  && this.guardLayer.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.phaseOp != null && this.phaseOp.requiresReconciliationAfterEmergency()
                  || this.emergencyOp != null && this.emergencyOp.requiresReconciliationAfterEmergency()
                  || this.guardLayer.requiresReconciliation();
            }

            @Override
            public void cancel() {
               AiGoal.init(this);
               if (!this.finished) {
                  this.finished = true;
                  this.diagnostics.snapshot("CANCELLED", this.phase == null ? "START" : this.phase.name(), this.lastMessage);
                  if (this.phaseOp != null) {
                     this.phaseOp.cancel();
                     this.phaseOp = null;
                  }

                  this.guardLayer.cancel();
                  if (this.emergencyOp != null) {
                     this.emergencyOp.cancel();
                     this.emergencyOp = null;
                  }

                  this.saveCheckpoint("CANCELLED");
                  this.endCampaign();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("A speedrun needs a connected living player");
      }
   }

   static boolean hasDurableWeapon(Minecraft mc) {
      AiItems.ItemSpec swords = AiItems.resolve("#minecraft:swords");
      AiItems.ItemSpec axes = AiItems.resolve("#minecraft:axes");

      for (int slot = 0; slot < 36; slot++) {
         ItemStack stack = mc.player.getInventory().getItem(slot);
         if ((swords.matches(stack) || axes.matches(stack)) && AiRunResources.hasMinDurability(stack, 8)) {
            return true;
         }
      }

      return false;
   }

   static AiOperation hunt(double range, int items, int seconds) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         Entity best = null;

         for (Entity e : mc.level.entitiesForRendering()) {
            if (isHuntable(mc, e, range) && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
               best = e;
            }
         }

         if (best == null) {
            return null;
         }

         String type = BuiltInRegistries.ENTITY_TYPE.getKey(best.getType()).toString();
         String drop = MOB_DROPS.get(type);
         int herd = 0;

         for (Entity e : mc.level.entitiesForRendering()) {
            if (e.getType() == best.getType() && isHuntable(mc, e, range)) {
               herd++;
            }
         }

         int count = AiCraft.countItem(drop) + 1;
         log("[AI] speedrun food: {} {} in sight, hunting for {} {}", herd, type, count, drop);
         return AiObtain.init(
            AiJson.objectOf(
               "item", drop, "count", count, "allowCombat", true, "staged", true, "radius", (int)Math.ceil(range), "timeoutSeconds", seconds
            )
         );
      } else {
         return null;
      }
   }

   private static AiOperation load(int needed, int seconds) {
      if (seconds < 15) {
         return null;
      } else {
         AiCraftPlanner.RecipeGraph graph = AiCraft.buildRecipeGraph();
         AiCraftPlanner.CraftPlan plan = breadPlan(graph, needed);
         if (plan == null) {
            return null;
         } else {
            return plan.requiresTable() && !AiCraft.isCraftingTableInReach() && AiCraft.countItem("minecraft:crafting_table") == 0 && !AiCraftPlanner.init(graph, "minecraft:crafting_table", 1).complete()
               ? null
               : AiCraft.craft(AiJson.objectOf("item", "minecraft:bread", "count", plan.target(), "timeoutSeconds", seconds));
         }
      }
   }

   private static AiSpeedrun.HayTarget nearestHay(Minecraft mc, Set<Long> attempted) {
      AiSpeedrun.HayTarget[] nearest = new AiSpeedrun.HayTarget[]{null};
      AiTerrain.nearestBlockDistSqr(
         mc,
         List.of(Blocks.HAY_BLOCK),
         24,
         pos -> {
            if (!attempted.contains(pos.asLong()) && !(pos.distSqr(mc.player.blockPosition()) > 576.0)) {
               for (Direction side : List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)) {
                  for (int dy = -2; dy <= 1; dy++) {
                     BlockPos stand = pos.relative(side).offset(0, dy, 0);
                     if (Math.abs(stand.getY() - mc.player.getBlockY()) <= 3
                        && mc.level.hasChunkAt(stand)
                        && AiGoto.isStandableCompat(stand)
                        && mc.level.getFluidState(stand).isEmpty()
                        && mc.level.getFluidState(stand.above()).isEmpty()) {
                        BlockState floor = mc.level.getBlockState(stand.below());
                        if (!floor.is(BlockTags.LEAVES) && !floor.is(BlockTags.LOGS)) {
                           nearest[0] = new AiSpeedrun.HayTarget(pos.immutable(), stand.immutable());
                           return true;
                        }
                     }
                  }
               }

               return false;
            } else {
               return false;
            }
         }
      );
      return nearest[0];
   }

   static AiOperation secureFood(final int items, final int seconds) {
      final Minecraft mc = Minecraft.getInstance();
      return new AiOperation() {
         long deadline = System.nanoTime() + seconds * 1000000000L;
         boolean graceUsed;
         final BlockPos origin = mc.player.blockPosition();
         final Object startLevel = mc.level;
         final Object startConnection = mc.getConnection();
         final Object startPlayer = mc.player;
         final AiGuardLayer guardLayer = new AiGuardLayer(AiGuard.GuardPolicy.standard(), "food trip");
         final AiSpeedrun.ChildTracker childTracker = new AiSpeedrun.ChildTracker();
         AiOperation.Result lastGuardResult;
         AiOperation legOp;
         Supplier<AiOperation> legFactory;
         String legName = "";
         int explorationLegs;
         int surfaceAttempts;
         int cookingSteps;
         int write;
         int update;
         final Set<Long> triedHay = new HashSet<>();
         AiSpeedrun.HayTarget hayTarget;
         boolean cancelled;

         @Override
         public AiOperation.Result tick() {
            if (this.cancelled || mc.player == null || mc.level != this.startLevel || mc.getConnection() != this.startConnection || mc.player != this.startPlayer) {
               return this.init(AiOperation.Status.CANCELLED, "Food trip cancelled or world/player changed");
            }

            if (!mc.player.isAlive()) {
               return this.init(AiOperation.Status.FAILED, "Player died during the food trip");
            }

            int have = AiRunResources.totalSafeFoodItems();
            if (have >= items && this.canInterruptNow() && !this.requiresReconciliationAfterEmergency() && !this.guardLayer.isActive()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Food in the bag: " + have);
            }

            if (System.nanoTime() > this.deadline) {
               return this.init(
                  this.requiresReconciliationAfterEmergency()
                     ? AiOperation.Status.UNCERTAIN
                     : (have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED),
                  "Food trip out of time with " + have
               );
            }

            if (!this.legName.equals("cook") && !this.guardLayer.isActive() && mc.player.getFoodData().getFoodLevel() <= 16) {
               AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
               int raw = AiSpeedrun.rawMeatCounts().values().stream().mapToInt(Integer::intValue).sum();
               boolean quiet = this.guardLayer.canInterruptNow()
                  && !this.guardLayer.requiresReconciliation()
                  && vitals.hostilesNear() == 0
                  && !vitals.underAttack()
                  && mc.player.getHealth() >= 10.0F
                  && !mc.player.isOnFire()
                  && !AiLavaEscape.isInLava(mc)
                  && !AiBreathe.init(mc.player);
               if (AiSpeedrun.shouldCookNow(vitals.food(), AiRunResources.totalSafeFoodNutrition(), raw, this.cookingSteps, this.secondsLeft(), quiet, this.legOp)) {
                  AiOperation cooking = this.cookLeg();
                  if (cooking != null) {
                     this.releaseLeg();
                     this.legFactory = this::cookLeg;
                     this.legOp = cooking;
                     this.legName = "cook";
                     return this.init("Cooking the gathered meat before spending raw rations");
                  }
               }
            }

            AiOperation.Result guarded = this.guardLayer.tick(this.legOp, this::releaseLeg, this::secondsLeft);
            if (guarded != null) {
               this.lastGuardResult = guarded;
               if (!guarded.terminal()) {
                  return this.init(guarded.message());
               }

               JsonObject receipt = this.progressJson();
               receipt.add("survival", AiRunResources.parseEvidenceJson(guarded.dataJson()));
               return this.init(guarded.status(), guarded.message(), receipt, guarded);
            } else {
               if (this.legOp == null && this.legFactory != null) {
                  this.legOp = this.legFactory.get();
                  if (this.legOp != null) {
                     return this.init("Resuming " + this.legName + " after survival care");
                  }

                  this.legFactory = null;
               }

               if (this.legOp != null) {
                  long before = System.nanoTime();
                  boolean transferWork = this.legName.equals("cook") || this.legName.equals("bread") || this.legName.equals("hay-mine");
                  AiOperation.Result r = this.childTracker.init(this.legOp, transferWork);
                  if (this.legName.equals("surface")) {
                     this.deadline = this.deadline + System.nanoTime() - before + 50000000L;
                  }

                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init(AiOperation.Status.UNCERTAIN, this.legName + ": " + r.message());
                  }

                  boolean seen = this.legName.startsWith("walk")
                     && this.legOp.canInterruptNow()
                     && !this.legOp.requiresReconciliationAfterEmergency()
                     && AiSpeedrun.hasHuntable(mc, 48.0);
                  if (!r.terminal() && !seen) {
                     return this.init(this.legName + ": " + r.message());
                  }

                  boolean gatherHay = this.legName.equals("hay-walk") && r.status() == AiOperation.Status.SUCCEEDED;
                  this.childTracker.load(this.legOp);
                  this.legOp = null;
                  this.legFactory = null;
                  this.legName = "";
                  if (gatherHay
                     && this.hayTarget != null
                     && this.secondsLeft() >= 20
                     && mc.level.getBlockState(this.hayTarget.block()).is(Blocks.HAY_BLOCK)
                     && mc.player.blockPosition().distSqr(this.hayTarget.stand()) <= 4.0) {
                     int needed = Math.max(1, (items - AiRunResources.totalSafeFoodItems() + 2) / 3);
                     int target = AiCraft.countItem("minecraft:hay_block") + Math.min(4, needed);
                     this.legFactory = () -> AiRunDiagnostics.trace(
                        "food_trip",
                        "gather observed hay",
                        autismclient.ai.engine.AiBlockMine.create(
                           AiJson.objectOf(
                              "block",
                              "minecraft:hay_block",
                              "drop",
                              "minecraft:hay_block",
                              "held",
                              target,
                              "radius",
                              16,
                              "explore",
                              false,
                              "timeoutSeconds",
                              Math.min(45, this.secondsLeft())
                           )
                        )
                     );
                     this.legOp = this.legFactory.get();
                     this.legName = "hay-mine";
                     return this.init("Gathering nearby hay for bread");
                  }
               }

               AiOperation bread = this.load();
               if (bread != null) {
                  this.legFactory = this::load;
                  this.legOp = bread;
                  this.legName = "bread";
                  return this.init("Making bread from gathered food");
               }

               if (AiSpeedrun.mayTryHay(this.update, this.secondsLeft()) && (this.hayTarget = AiSpeedrun.nearestHay(mc, this.triedHay)) != null) {
                  this.update++;
                  this.triedHay.add(this.hayTarget.block().asLong());
                  BlockPos stand = this.hayTarget.stand();
                  this.legFactory = () -> AiRunDiagnostics.trace(
                     "food_trip",
                     "walk to observed hay",
                     AiGoto.travelToCompat(
                        stand,
                        1.0,
                        false,
                        false,
                        false,
                        Math.min(30, this.secondsLeft()),
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                        true,
                        2.0,
                        autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(stand, 1.0),
                        new AiGoto.GotoOptions(true, false, 3, true, false, true)
                     )
                  );
                  this.legOp = this.legFactory.get();
                  this.legName = "hay-walk";
                  return this.init("Nearby hay can provide bread");
               }

               AiFoodHuntBudget.Inner_DataUtil admission = AiFoodHuntBudget.init(this.deadline, System.nanoTime(), this.graceUsed);
               if (admission.admitted()) {
                  AiOperation hunt = AiSpeedrun.hunt(48.0, Math.max(1, items - have), admission.childSeconds());
                  if (hunt != null) {
                     this.deadline = admission.deadlineNanos();
                     this.graceUsed = admission.graceUsed();
                     this.legOp = AiRunDiagnostics.trace("food_trip", "hunt " + have + "/" + items, hunt);
                     this.legName = "hunt";
                     return this.init("Hunting food");
                  }

                  if (!AiSpeedrun.init_Z_Minecraft_I(mc, 5) && this.surfaceAttempts < 2) {
                     this.surfaceAttempts++;
                     this.legFactory = () -> AiRunDiagnostics.trace(
                        "food_trip",
                        "surface for animals",
                        autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", Math.min(120, this.secondsLeft())))
                     );
                     this.legOp = this.legFactory.get();
                     this.legName = "surface";
                     return this.init("Up to open sky for food animals");
                  }

                  if (this.explorationLegs >= 6) {
                     return this.init(have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "No food animal found; " + have + " food held");
                  }

                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  if (mc.level.dimension() == Level.OVERWORLD && dayTime > 13000L && dayTime < 23000L) {
                     return this.init(
                        have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "Night: no walk out for food; " + have + " food held"
                     );
                  }

                  double a = this.explorationLegs++ * 2.399;
                  int x = this.origin.getX() + (int)Math.round(Math.cos(a) * 64.0);
                  int z = this.origin.getZ() + (int)Math.round(Math.sin(a) * 64.0);
                  BlockPos surface = AiSpeedrun.findSurface(mc.player.blockPosition(), x, z, column -> AiSpeedrun.observedSurface(mc, column));
                  if (surface == null) {
                     return this.init("No observed safe surface in this direction; trying another bearing");
                  }

                  this.legFactory = () -> AiRunDiagnostics.trace(
                     "food_trip",
                     "explore " + x + "," + z,
                     AiGoto.travelToCompat(
                        surface,
                        2.0,
                        false,
                        false,
                        false,
                        Math.min(60, this.secondsLeft()),
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                        true,
                        2.0,
                        AiSpeedrun.surfaceGoal(surface),
                        AiGoto.GotoOptions.DEFAULT
                     )
                  );
                  this.legName = "walk";
                  this.legOp = this.legFactory.get();
                  return this.init("Looking for food animals");
               } else {
                  return !AiFoodHuntBudget.init(this.canInterruptNow() && autismclient.ai.engine.AiMotionSafety.isPlayerSupported(), this.requiresReconciliationAfterEmergency())
                     ? this.init("Waiting for the food trip's safe handoff")
                     : this.init(
                        have > 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED, "No time for another complete hunt; " + have + " food held"
                     );
               }
            }
         }

         AiOperation.Result init(String message) {
            return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
         }

         JsonObject progressJson() {
            return AiJson.objectOf(
               "foodHeld",
               AiRunResources.totalSafeFoodItems(),
               "target",
               items,
               "leg",
               this.legName,
               "explorationLegs",
               this.explorationLegs,
               "surfaceAttempts",
               this.surfaceAttempts,
               "cookingSteps",
               this.cookingSteps,
               "breadSteps",
               this.write,
               "hayTrips",
               this.update,
               "survivalActive",
               this.guardLayer.isActive()
            );
         }

         int secondsLeft() {
            return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
         }

         AiOperation cookLeg() {
            if (this.cookingSteps < 6 && this.secondsLeft() >= 60) {
               AiOperation cooking = AiForage.cook(Math.min(120, this.secondsLeft()));
               if (cooking == null) {
                  return null;
               }

               this.cookingSteps++;
               return AiRunDiagnostics.trace("food_trip", "cook gathered meat", cooking);
            } else {
               return null;
            }
         }

         AiOperation load() {
            if (this.write >= 2) {
               return null;
            }

            AiOperation bread = AiSpeedrun.load(Math.max(1, items - AiRunResources.totalSafeFoodItems()), Math.min(45, this.secondsLeft()));
            if (bread == null) {
               return null;
            }

            this.write++;
            return AiRunDiagnostics.trace("food_trip", "bread from held wheat or hay", bread);
         }

         void releaseLeg() {
            if (this.legOp != null) {
               this.childTracker.load(this.legOp);
               this.legOp = null;
            }
         }

         AiOperation.Result init(AiOperation.Status status, String message) {
            return this.init(status, message, this.progressJson(), null);
         }

         AiOperation.Result init(AiOperation.Status status, String message, JsonObject receipt, AiOperation.Result actual) {
            this.childTracker.observeChild(this.legOp);
            this.childTracker.init(this.guardLayer.requiresReconciliation(), this.lastGuardResult);
            AiOperation.Result result = this.childTracker.init(status, message, receipt, actual);
            this.cancel();
            AiSpeedrun.log("[AI] speedrun food trip {}: {}", result.status(), result.message());
            return result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.childTracker.init() && this.guardLayer.canInterruptNow() && (this.legOp == null || this.legOp.canInterruptNow());
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.childTracker.init()
               || this.guardLayer.requiresReconciliation()
               || this.legOp != null && this.legOp.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.childTracker.observeChild(this.legOp);
            this.childTracker.init(this.guardLayer.requiresReconciliation(), this.lastGuardResult);
            this.cancelled = true;
            this.guardLayer.cancel();
            this.releaseLeg();
            this.legFactory = null;
         }
      };
   }

   static boolean isHuntable(Minecraft mc, Entity e, double range) {
      if (mc.player != null && e != null && e.isAlive() && !(e.distanceToSqr(mc.player) > range * range)) {
         String id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString();
         return MOB_DROPS.containsKey(id) && !(e instanceof AgeableMob mob && mob.isBaby()) && AiCombat.init_Z_Minecraft_Entity(mc, e);
      } else {
         return false;
      }
   }

   static boolean hasHuntable(Minecraft mc, double range) {
      if (mc.player != null && mc.level != null) {
         for (Entity e : mc.level.entitiesForRendering()) {
            if (isHuntable(mc, e, range)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean init_Z_Minecraft_I(Minecraft mc, int radius) {
      BlockPos feet = mc.player.blockPosition();

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            if (mc.level.canSeeSky(feet.offset(dx, 1, dz))) {
               return true;
            }
         }
      }

      return false;
   }

   static String fuelFor(int items) {
      long needed = items * 200L;

      for (String coal : List.of("minecraft:coal", "minecraft:charcoal")) {
         if ((AiCraft.countItem(coal) - AiRunResources.reservedItemCount(coal)) * 1600L >= needed) {
            return coal;
         }
      }

      for (Item plank : AiItems.resolveItem("planks").items()) {
         String id = BuiltInRegistries.ITEM.getKey(plank).toString();
         if ((AiCraft.countItem(id) - AiRunResources.reservedItemCount(id)) * 300L >= needed) {
            return id;
         }
      }

      return null;
   }

   static Map<String, Integer> rawMeatCounts() {
      Map<String, Integer> out = new LinkedHashMap<>();

      for (String raw : COOKED.keySet()) {
         int n = AiCraft.countItem(raw);
         if (n > 0) {
            out.put(raw, n);
         }
      }

      return out;
   }

   static void log(String format, Object... args) {
      AutismClientAddon.LOG.info(format, args);
   }

   static String clip(String s) {
      return s == null ? "" : (s.length() > 300 ? s.substring(0, 300) + "..." : s);
   }

   enum RunPhase {
      WOOD,
      FOOD,
      KIT,
      EQUIP,
      COOK,
      HEAL,
      NETHER_ENTRY,
      NETHER,
      STRONGHOLD,
      END;
   }

   static final class ChildTracker {
      private AiOperation child;
      private AiOperation.Result childResult;
      private AiOperation.Result load;
      private boolean needsReconciliation;

      AiOperation.Result init(AiOperation child, boolean transfer) {
         boolean before = transfer && child.requiresReconciliationAfterEmergency();
         AiOperation.Result result = child.tick();
         boolean after = child.requiresReconciliationAfterEmergency();
         if (result.terminal()) {
            result = AiGuardLayer.wrapPendingTermination(
               transfer && (after || result.status() != AiOperation.Status.SUCCEEDED && before) || needsReobserve(result), result
            );
         }

         this.child = child;
         this.childResult = result;
         if (result.terminal() && needsReobserve(result)) {
            this.init(true, result);
         }

         return result;
      }

      void observeChild(AiOperation child) {
         if (child != null) {
            AiOperation.Result actual = this.child == child ? this.childResult : null;
            this.init(child.requiresReconciliationAfterEmergency(), actual);
            if (this.child != child) {
               this.child = child;
               this.childResult = null;
            }
         }
      }

      void init(boolean needsObservation, AiOperation.Result actual) {
         if (needsObservation || needsReobserve(actual)) {
            this.needsReconciliation = true;
            if (actual != null && (this.load == null || actual == this.childResult)) {
               this.load = actual;
            }
         }
      }

      void load(AiOperation child) {
         if (child != null) {
            this.observeChild(child);
            child.cancel();
         }
      }

      boolean init() {
         return this.needsReconciliation;
      }

      AiOperation.Result init(AiOperation.Status status, String message, JsonObject phaseData, AiOperation.Result actual) {
         JsonObject data = phaseData.deepCopy();
         if (this.load != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.load.dataJson()));
         }

         if (actual == null) {
            actual = this.childResult != null ? this.childResult : this.load;
         }

         AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, data, this.needsReconciliation, actual);
         return actual == null ? result : new AiOperation.Result(result.status(), result.message(), result.dataJson(), actual.evidence(), actual.failure());
      }

      private static boolean needsReobserve(AiOperation.Result result) {
         if (result == null) {
            return false;
         }

         JsonObject data = AiRunResources.parseEvidenceJson(result.dataJson());
         return AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false) || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
      }
   }

   static final class TableCarry {
      private boolean init;

      AiOperation init(int held, int secondsLeft, IntFunction<AiOperation> observed) {
         if (!this.init && held <= 0 && secondsLeft > 0) {
            AiOperation operation = observed.apply(Math.min(90, secondsLeft));
            if (operation != null) {
               this.init = true;
            }

            return operation;
         } else {
            return null;
         }
      }
   }

   record HayTarget(BlockPos block, BlockPos stand) {
   }
}
