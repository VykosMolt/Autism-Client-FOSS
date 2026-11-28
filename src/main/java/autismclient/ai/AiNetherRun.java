package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismBaseSpawnerAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

final class AiNetherRun {
   static final int init_I = 96;
   static final int load_I = 12;
   static final int read_I = 5;
   static final int write_I = 14;
   static final long init_J = 300000000000L;
   static final long load_J = 90000000000L;
   static final long read_J = 1080000000000L;
   static final long write_J = -9223372036854775808L;
   static final int update_I = 160;
   static final Set<Long> init_Set = ConcurrentHashMap.newKeySet();
   static final List<BlockPos> init_List = new CopyOnWriteArrayList<>();
   static Object init_Obj;
   static final long update_J = 150000000000L;
   static final int sync_I = 6;
   static volatile boolean init_Z;
   static final Map<Long, Long> init_Map = new ConcurrentHashMap<>();
   static volatile int flush_I;
   static volatile Object load_Obj;
   static final int close = 64;
   static final int open = 48;
   static final int poll = 5;
   static final Predicate<BlockState> FORTRESS_BLOCK = s -> s.is(Blocks.NETHER_BRICKS) || s.is(Blocks.NETHER_BRICK_FENCE) || s.is(Blocks.NETHER_BRICK_STAIRS);
   static final Predicate<BlockState> WARPED_FOREST_BLOCK = s -> s.is(Blocks.WARPED_NYLIUM) || s.is(Blocks.WARPED_STEM);
   static final Predicate<BlockState> BASTION_BLOCK = s -> s.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
      || s.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
      || s.is(Blocks.CHISELED_POLISHED_BLACKSTONE)
      || s.is(Blocks.GILDED_BLACKSTONE);
   static final List<BlockPos> load_List = new CopyOnWriteArrayList<>();
   static final long sync_J = 30000000000L;
   static volatile BlockPos rememberedPortal;
   static volatile String routeWorldKey = "";
   static volatile int savedConfirmedBarters;
   static volatile BlockPos rememberedSpawner;
   static volatile Object routeConnection;
   private static boolean routeMemoryRequested;
   private static long routeGeneration;
   private static boolean read_Z;

   private AiNetherRun() {
   }

   static BlockPos firstPlausiblePortal(BlockPos loadedPortal, BlockPos historicalPortal, BlockPos rememberedPortal, Predicate<BlockPos> plausible) {
      for (BlockPos candidate : new BlockPos[]{loadedPortal, historicalPortal, rememberedPortal}) {
         if (candidate != null && plausible.test(candidate)) {
            return candidate.immutable();
         }
      }

      return null;
   }

   static int buildSeconds(long deadline, long now) {
      long seconds = (deadline - now) / 1000000000L;
      return seconds < 10L ? 0 : (int)Math.min(120L, seconds);
   }

   static void recordPathEndpoints(BlockPos origin, BlockPos target) {
      if (origin != null) {
         init_List.add(origin.immutable());
      }

      if (target != null) {
         init_List.add(target.immutable());
      }
   }

   static boolean nearFailedTarget(BlockPos target) {
      return target != null && init_List.stream().anyMatch(failed -> failed.distSqr(target) < 1024.0);
   }

   static int rodGoal(int requested, boolean campaign, boolean pearlRecovery, boolean routeProven, int powderHeld) {
      return pearlRecovery && routeProven ? Math.max(0, powderHeld / 2) : Math.max(campaign ? 8 : 1, Math.min(16, requested));
   }

   static boolean isPearlPhaseReady(int rodsHeld, int rodGoal, boolean campaign, boolean spawnerConfirmed, int barters) {
      return rodsHeld >= rodGoal && (!campaign || spawnerConfirmed);
   }

   static boolean init_Z_I(int rodsHeld, int rodGoal, boolean committedVein, boolean adultPartner, double distanceSquared) {
      return rodsHeld >= rodGoal || committedVein || adultPartner && distanceSquared <= 64.0;
   }

   static boolean isRouteFailure(AiOperation.Result r) {
      return r.status() != AiOperation.Status.SUCCEEDED
         && r.message() != null
         && (r.message().contains("No route") || r.message().contains("Route search budget exhausted"));
   }

   static AiNetherRun.PearlBudget pearlBudget(int wanted, int eyes, int rawPearls, int confirmedBarters) {
      boolean proofOnly = confirmedBarters == 0 && (long)eyes + rawPearls >= wanted;
      return new AiNetherRun.PearlBudget(proofOnly ? 0 : Math.max(0, wanted - eyes), proofOnly ? 1 : 32);
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (mc.level.dimension() != Level.NETHER) {
            throw new IllegalArgumentException("The Nether run starts in the Nether");
         }

         refreshRouteMemory();
         boolean routeProven = load_Z();
         final boolean pearlRecovery = AiJson.getBoolean(args, "pearlRecovery", false) && routeProven;
         final int rods = rodGoal(AiJson.getInt(args, "rods", 8), AiRunResources.isCampaignActive(), pearlRecovery, routeProven, AiRunResources.blazePowderEquivalents());
         final int pearls = Math.max(0, Math.min(32, AiJson.getInt(args, "pearls", 16)));
         final int timeoutSeconds = Math.max(120, Math.min(5400, AiJson.getInt(args, "timeoutSeconds", 2400)));
         BlockPos portal = nearestPortalBlock(mc, mc.player.blockPosition(), 8);
         final String currentWorld = AiWorldIdentity.currentWorldKey(mc);
         if (portal != null) {
            rememberedPortal = portal;
         }

         init_Z = false;
         final BlockPos remembered = firstPlausiblePortal(portal, AiPortalLinks.nearestKnownPortal(2147483647), rememberedPortal, at -> isPortalOrUnloaded(mc, at));
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final String startWorld = currentWorld;
            final Object startConnection = mc.getConnection();
            final UUID startPlayerId = mc.player.getUUID();
            final long startDeathRevision = AiServerEvidence.deathRevision();
            final JsonArray init_JsonArray = new JsonArray();
            BlockPos portal = remembered;
            final AiNetherRun.PortalScanCache init_aB_BufferHelper = new AiNetherRun.PortalScanCache();
            final Set<Long> init_Set = new HashSet<>();
            final AiNetherRun.MiningMemory init_aB_StreamUtil = new AiNetherRun.MiningMemory();
            AiOperation child;
            String label = "";
            String lastLabel = "";
            int targetEntityId = -1;
            AiNetherRun.RunPhase phase;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            long read_J;
            BlockPos load_BlockPos;
            boolean shieldAttempted;
            int sync_I;
            int flush_I;
            int close_I;
            int open_I;
            int poll_I;
            int push_I;
            int pop_I;
            BlockPos read_BlockPos;
            AiOperation.Result childResult;
            int confirmedBarters = AiNetherRun.savedConfirmedBarters;
            int goldSpent;
            BlockPos write_BlockPos;
            boolean load_Z;
            String stopReason;
            long write_J;
            final AiGuardLayer guard;
            long nextHealAt;
            int get_I;
            long sync_J;
            final AiNetherRun.PearlHuntTracker pearlTracker;
            long flush_J;
            long close_J;
            long open_J;
            long poll_J;
            long push_J;
            boolean read_Z;
            final Set<Long> load_Set;
            final Map<String, Integer> lootAttempts;
            BlockPos pathTarget;
            BlockPos pathOrigin;
            long unusedLong;
            int set_I;
            int put_I;
            int headForFailures;
            int returnRouteFailures;
            boolean pearlsGivenUp;
            boolean update_Z;
            long tick_J;
            long run_J;
            long get_J;
            int endermanHuntAttempts;
            int peakPearls;
            int spawnerApproachFailures;
            BlockPos flush_BlockPos;
            final Set<Long> read_Set;
            int lavaWaits;
            final Map<Integer, Long> huntCooldowns;
            int lastRodCount;
            int rodStallChecks;
            final int wantedRods;
            long set_J;
            long put_J;
            JsonObject init_JsonObject;
            boolean reobserveBeforeResuming;

            {
               this.write_BlockPos = AiNetherRun.rememberedSpawner;
               this.stopReason = "";
               this.write_J = -9223372036854775808L;
               this.guard = new AiGuardLayer(
                  AiGuard.GuardPolicy.standard()
                     .withoutFlee()
                     .keeping(List.of("minecraft:blaze_rod", "minecraft:blaze_powder", "minecraft:ender_pearl", "minecraft:ender_eye", "minecraft:gold_ingot")),
                  "nether"
               );
               this.pearlTracker = new AiNetherRun.PearlHuntTracker();
               this.load_Set = new HashSet<>();
               this.lootAttempts = new HashMap<>();
               this.read_Set = new HashSet<>();
               this.huntCooldowns = new HashMap<>();
               this.lastRodCount = -1;
               this.wantedRods = rods;
               this.init_JsonObject = new JsonObject();
            }

            boolean hasBarterPiglinWithin(double range) {
               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:piglin", range)) {
                  if (e instanceof Piglin p && p.getOffhandItem().isEmpty() && AiNetherStructures.canBarter(mc, p)) {
                     return true;
                  }
               }

               return false;
            }

            boolean init_Z_BlockPos_5(BlockPos at) {
               return AiNetherStructures.init_Z(at);
            }

            boolean routePassesNear(BlockPos at, BlockPos to, BlockPos bastion) {
               double ax = at.getX();
               double az = at.getZ();
               double dx = to.getX() - ax;
               double dz = to.getZ() - az;
               double bx = bastion.getX() - ax;
               double bz = bastion.getZ() - az;
               double len2 = dx * dx + dz * dz;
               double t = len2 < 1.0 ? 0.0 : Math.max(0.0, Math.min(1.0, (bx * dx + bz * dz) / len2));
               double px = ax + dx * t - bastion.getX();
               double pz = az + dz * t - bastion.getZ();
               double endX = to.getX() - bastion.getX();
               double endZ = to.getZ() - bastion.getZ();
               double now = Math.hypot(bx, bz);
               double end = Math.hypot(endX, endZ);
               return now < 40.0 ? end < now + 20.0 : end < 48.0 || px * px + pz * pz < 1024.0;
            }

            boolean piglinWatching() {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof AbstractPiglin p && p.isAlive() && p.distanceToSqr(mc.player) < 256.0 && p.hasLineOfSight(mc.player)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos nextGoldTarget(Predicate<BlockState> kind, boolean block) {
               if (!this.piglinWatching() && (!block || AiWorldScan.findEntitiesByType("minecraft:piglin_brute", 40.0).isEmpty())) {
                  long now = System.nanoTime();
                  List<BlockPos> observed = new ArrayList<>();

                  for (BlockPos at : AiWorldScan.findBlocks(kind, 2, 16, 128)) {
                     if (at.distSqr(mc.player.blockPosition()) > 576.0) {
                        break;
                     }

                     if (!this.load_Z_5(at) && (!block || !this.init_Z_BlockPos_5(at))) {
                        observed.add(at);
                     }
                  }

                  autismclient.ai.engine.AiMiningCommitment vein = this.init_aB_StreamUtil.commitmentFor(block);
                  Predicate<BlockPos> allowed = atxx -> this.init_aB_StreamUtil.isCellAvailable(atxx.asLong(), now);
                  vein.extend(observed, allowed);
                  Set<Long> present = new HashSet<>();

                  for (BlockPos at : observed) {
                     if (allowed.test(at)) {
                        present.add(at.asLong());
                     }
                  }

                  vein.dropMissing(present, atxx -> !allowed.test(atxx) || AiTerrain.isLoaded(mc.level, atxx) && !kind.test(mc.level.getBlockState(atxx)));
                  if (vein.isActive() && vein.isFinished(now, true, 250000000L)) {
                     AiRunTrace.emitServiceEvent(
                        "nether_gold_batch",
                        AiJson.objectOf("transition", "complete", "kind", block ? "block" : "ore", "anchor", vein.getAnchor().toShortString())
                     );
                     vein.clear();
                  }

                  if (!vein.isActive()) {
                     BlockPos seed = null;

                     for (BlockPos at : observed) {
                        if (allowed.test(at) && (seed == null || at.distSqr(mc.player.blockPosition()) < seed.distSqr(mc.player.blockPosition()))) {
                           seed = at;
                        }
                     }

                     if (seed == null) {
                        return null;
                     }

                     vein.begin(seed, observed, allowed);
                     AiRunTrace.emitServiceEvent(
                        "nether_gold_batch",
                        AiJson.objectOf(
                           "transition", "start", "kind", block ? "block" : "ore", "anchor", seed.toShortString(), "remaining", vein.remainingCount()
                        )
                     );
                  }

                  BlockPos best = null;

                  for (BlockPos at : vein.remainingCells()) {
                     if (best == null || at.distSqr(mc.player.blockPosition()) < best.distSqr(mc.player.blockPosition())) {
                        best = at;
                     }
                  }

                  if (best != null) {
                     AiRunTrace.emitServiceEvent(
                        "nether_gold_target",
                        AiJson.objectOf(
                           "kind",
                           block ? "block" : "ore",
                           "anchor",
                           vein.getAnchor().toShortString(),
                           "target",
                           best.toShortString(),
                           "remaining",
                           vein.remainingCount()
                        )
                     );
                  }

                  return best;
               } else {
                  return null;
               }
            }

            boolean load_Z_5(BlockPos at) {
               for (int down = 1; down <= 4; down++) {
                  BlockPos cell = at.below(down);
                  if (!AiTerrain.isLoaded(mc.level, cell)) {
                     return false;
                  }

                  if (mc.level.getFluidState(cell).is(FluidTags.LAVA) && mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return true;
                  }

                  if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return false;
                  }
               }

               return false;
            }

            AiOperation breakBlock(BlockPos at, int seconds) {
               this.read_BlockPos = at;
               Object level = mc.level;
               return AiNetherRun.watchedMining(
                  AiGameOperations.blockAction("break", AiJson.objectOf("x", at.getX(), "y", at.getY(), "z", at.getZ(), "timeoutSeconds", seconds)),
                  () -> mc.level == level && mc.player != null && mc.getConnection() == this.startConnection,
                  () -> mc.level != null
                     && AiTerrain.isLoaded(mc.level, at)
                     && (mc.level.getBlockState(at).isAir() || !mc.level.getBlockState(at).getFluidState().isEmpty()),
                  this::piglinWatching,
                  () -> AiServerEvidence.blockRevision(at),
                  () -> this.init_aB_StreamUtil.deferCell(at.asLong(), System.nanoTime())
               );
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.startWorld.equals(AiWorldIdentity.currentWorldKey(mc)) || this.startConnection != mc.getConnection()) {
                     return this.finish(AiOperation.Status.CANCELLED, "World or connection changed during Nether expedition");
                  }

                  if (!mc.player.getUUID().equals(this.startPlayerId)) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Player changed during Nether expedition");
                  }

                  if (mc.player.isAlive() && this.startDeathRevision == AiServerEvidence.deathRevision()) {
                     AiGoal.set(
                        this,
                        1,
                        "Nether",
                        AiGoal.init(this.label, this.phase == null ? "" : this.phase.name()),
                        this.heldRods() + " of " + this.wantedRods + " blaze rods, " + this.heldPearls() + " of " + pearls + " pearls"
                     );
                     if (++this.get_I % 200 == 0 && mc.level.dimension() == Level.NETHER) {
                        BlockPos seen = AiWorldScan.findNearestBlock(AiNetherRun.WARPED_FOREST_BLOCK, 12);
                        if (seen != null && AiNetherRun.load_List.stream().noneMatch(w -> w.distSqr(seen) < 2304.0)) {
                           AiNetherRun.load_List.add(seen.immutable());
                        }
                     }

                     autismclient.ai.engine.AiRestock.reserveItemsForJob(Map.of(Items.OBSIDIAN, 64));
                     if (System.nanoTime() > this.deadline) {
                        return this.finish(
                           this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                           "Out of time in the Nether at " + this.phase
                        );
                     }

                     if (mc.level.dimension() != Level.NETHER) {
                        if (mc.level.dimension() != Level.OVERWORLD) {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Unexpected destination after Nether expedition");
                        }

                        if (this.phase == AiNetherRun.RunPhase.RETURN
                           && this.child != null
                           && (this.label.equals("through the portal") || this.label.startsWith("build a portal home"))) {
                           AiOperation.Result arrival = this.child.tick();
                           if (!arrival.terminal()) {
                              return this.running("Verifying Overworld arrival: " + arrival.message());
                           }

                           arrival = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.child, arrival);
                           this.child = null;
                           if (arrival.status() != AiOperation.Status.SUCCEEDED) {
                              return this.finish(arrival.status(), arrival.message(), arrival);
                           }

                           boolean route = !AiRunResources.isCampaignActive() || this.confirmedBarters > 0 && this.write_BlockPos != null;
                           return this.finish(
                              (this.heldRods() < this.wantedRods || !route) && !AiNetherRun.init_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED,
                              (
                                    AiNetherRun.init_Z
                                       ? "Home for supplies (" + (this.stopReason.isEmpty() ? "food or equipment" : this.stopReason) + ") with "
                                       : "Verified Overworld return with "
                                 )
                                 + this.heldRods()
                                 + " rod equivalents and "
                                 + this.heldPearls()
                                 + " pearl equivalents",
                              arrival
                           );
                        } else {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Nether dimension changed outside the expected return action");
                        }
                     } else {
                        if (!AiThreats.isLegacyCombatEnabled()) {
                           autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(5000000000L);
                        }

                        if (this.phase == AiNetherRun.RunPhase.RODS
                           && this.child != null
                           && AiNetherRun.isSpawnerApproachLabel(this.label)
                           && !this.guard.isActive()
                           && this.child.canInterruptNow()
                           && System.nanoTime() > this.open_J
                           && this.heldRods() < this.wantedRods
                           && mc.player.getHealth() >= 12.0F
                           && mc.player.onGround()
                           && !AiEndRun.init_Z(mc)
                           && !mc.player.isInLava()
                           && mc.level.dimension() == Level.NETHER) {
                           Entity inSight = this.nearestVisibleBlaze(16);
                           if (inSight != null) {
                              AiSpeedrun.log(
                                 "[AI] nether: a blaze in sight at {} during '{}'; fighting it", inSight.blockPosition().toShortString(), this.label
                              );
                              this.child.cancel();
                              this.child = null;
                              this.flush_J = System.nanoTime();
                              return this.startChild(
                                 "fight a blaze",
                                 AiNetherRun.fightThenPickup(
                                    AiBlazeFight.init(
                                       AiJson.objectOf(
                                          "count", AiNetherRun.init_I_I_I(this.wantedRods, this.heldRods()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 120
                                       )
                                    ),
                                    "minecraft:blaze_rod",
                                    () -> AiGameplay.write(
                                       AiJson.objectOf(
                                          "item",
                                          "minecraft:blaze_rod",
                                          "count",
                                          Math.min(this.wantedRods, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                          "radius",
                                          16,
                                          "timeoutSeconds",
                                          15
                                       )
                                    )
                                 )
                              );
                           }
                        }

                        if (this.guard.isActive()
                           || !this.label.startsWith("fight a blaze")
                           || mc.player.getHealth() < 10.0F
                           || AiEndRun.init_Z(mc)
                           || mc.player.isInLava()) {
                           AiOperation.Result g = this.guard.tick(this.child, () -> {
                              this.init_V_18();
                              if (this.child != null && this.label.startsWith("barter") && this.childResult != null) {
                                 this.absorbChildEvidence(this.childResult);
                              }

                              this.childResult = null;
                              if (this.child != null && this.label.startsWith("barter") && this.close_I < 4) {
                                 this.close_I++;
                                 this.flush_I--;
                              }

                              if (this.child != null && this.write_J != -9223372036854775808L && this.poll_I < 6) {
                                 this.poll_I++;
                                 this.init_Set.remove(this.write_J);
                              }

                              if (this.child != null && this.label.startsWith("build a portal home")) {
                                 this.load_Z = false;
                              }

                              this.child.cancel();
                              this.child = null;
                           }, () -> (int)Math.max(10L, (this.deadline - System.nanoTime()) / 1000000000L));
                           if (g != null) {
                              return g.terminal() ? this.finish(g.status(), g.message(), g) : g;
                           }
                        }

                        if (this.child != null) {
                           AiOperation.Result r = this.child.tick();
                           this.childResult = r;
                           if (!r.terminal()) {
                              return this.running(this.label + ": " + r.message());
                           }

                           r = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.child, r);
                           this.child = null;
                           AiSpeedrun.log("[AI] nether step {}: {} - {}", r.status(), this.label, AiSpeedrun.clip(r.message()));
                           this.init_JsonArray
                              .add(AiJson.objectOf("step", this.label, "status", r.status().name(), "message", AiSpeedrun.clip(r.message())));
                           if (r.status() == AiOperation.Status.CANCELLED) {
                              return this.finish(AiOperation.Status.CANCELLED, "Cancelled at " + this.label, r);
                           }

                           this.init_V_AiOperation_Result_2(r);
                           this.childResult = null;
                           if (r.status() == AiOperation.Status.UNCERTAIN) {
                              return this.finish(AiOperation.Status.UNCERTAIN, this.label + ": " + r.message(), r);
                           }
                        }

                        return !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() && !mc.player.isInLava()
                           ? this.running("Landing")
                           : this.load_AiOperation_Result_12();
                     }
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "Player died during Nether expedition");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result init_AiOperation_Result_16() {
               if (!this.lastLabel.isEmpty()) {
                  String label = this.lastLabel;
                  this.lastLabel = "";
                  BlockPos ore = this.open_I < 48 ? this.nextGoldTarget(s -> s.is(Blocks.NETHER_GOLD_ORE), false) : null;
                  if (ore != null) {
                     this.open_I++;
                     this.pop_I = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                     AiRunTrace.emitServiceEvent("nether_leg_resume", AiJson.objectOf("kind", "gold", "target", ore.toShortString()));
                     return this.startChild(
                        label,
                        AiNetherRun.thenOnSuccess(
                           this.breakBlock(ore, 30),
                           () -> AiGameplay.write(
                              AiJson.objectOf("item", "minecraft:gold_nugget", "count", 1, "additional", true, "radius", 10, "timeoutSeconds", 15)
                           )
                        )
                     );
                  }
               }

               if (this.targetEntityId >= 0) {
                  Entity entity = mc.level.getEntity(this.targetEntityId);
                  if (entity instanceof Blaze && entity.isAlive()) {
                     if (mc.player.getHealth() < 10.0F) {
                        return null;
                     }

                     int id = this.targetEntityId;
                     this.targetEntityId = -1;
                     this.flush_J = System.nanoTime();
                     AiRunTrace.emitServiceEvent("nether_leg_resume", AiJson.objectOf("kind", "blaze", "entityId", id));
                     return this.startChild(
                        "fight a blaze",
                        AiNetherRun.fightThenPickup(
                           AiBlazeFight.init(
                              AiJson.objectOf(
                                 "entityId", id, "count", AiNetherRun.init_I_I_I(this.wantedRods, this.heldRods()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 60
                              )
                           ),
                           "minecraft:blaze_rod",
                           () -> AiGameplay.write(
                              AiJson.objectOf(
                                 "item",
                                 "minecraft:blaze_rod",
                                 "count",
                                 Math.min(this.wantedRods, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                 "radius",
                                 16,
                                 "timeoutSeconds",
                                 15
                              )
                           )
                        )
                     );
                  } else {
                     this.targetEntityId = -1;
                     return null;
                  }
               } else {
                  return null;
               }
            }

            void init_V_AiOperation_Result_2(AiOperation.Result r) {
               if (this.label.startsWith("barter")) {
                  this.absorbChildEvidence(r);
               }

               if (this.label.startsWith("convert") && r.status() != AiOperation.Status.SUCCEEDED) {
                  this.poll_J = System.nanoTime() + 60000000000L;
               }

               if (this.label.startsWith("barter")
                  && r.status() != AiOperation.Status.SUCCEEDED
                  && r.message() != null
                  && r.message().startsWith("No safe unprovoked adult piglin")) {
                  this.flush_I = Math.max(0, this.flush_I - 1);
                  this.push_J = System.nanoTime() + 10000000000L;
               }

               if (this.read_BlockPos != null
                  && (AiNetherRun.isRouteFailure(r) || r.status() != AiOperation.Status.SUCCEEDED && r.message() != null && r.message().contains("drop would fall into lava"))) {
                  this.init_aB_StreamUtil.deferCell(this.read_BlockPos.asLong(), System.nanoTime());
               }

               this.read_BlockPos = null;
               if (AiNetherRun.isRouteFailure(r)) {
                  this.put_I++;
               } else if (r.status() == AiOperation.Status.SUCCEEDED && this.label.startsWith("walk")) {
                  this.put_I = 0;
               }

               if (this.label.startsWith("explore")
                  && r.status() != AiOperation.Status.SUCCEEDED
                  && r.message() != null
                  && r.message().startsWith("Health too low")) {
                  this.load_I--;
               } else if (this.label.startsWith("explore") && r.status() != AiOperation.Status.SUCCEEDED && this.pathTarget != null) {
                  AiNetherRun.recordPathEndpoints(this.pathOrigin, this.pathTarget);
                  AiWorldMemory.load(this.pathTarget);
               }

               if (this.label.startsWith("fight a blaze")) {
                  this.update_I = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.update_I + 1;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     this.open_J = System.nanoTime() + 10000000000L;
                  }

                  if (AiNetherRun.hasRetreated(AiRunResources.parseEvidenceJson(r.dataJson()))) {
                     this.read_Z = true;
                  }
               }

               if (this.label.startsWith("mine safe barter gold")) {
                  int now = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                  this.push_I = now > this.pop_I ? 0 : this.push_I + 1;
               }

               if (this.label.startsWith("head for")) {
                  this.headForFailures = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.headForFailures + 1;
               }

               if (this.load_BlockPos == null || !this.load_BlockPos.equals(this.flush_BlockPos)) {
                  this.flush_BlockPos = this.load_BlockPos;
                  this.spawnerApproachFailures = 0;
               }

               if ((this.label.startsWith("walk to a stand by the blaze spawner") || this.label.startsWith("walk to the blaze spawner"))
                  && r.status() != AiOperation.Status.SUCCEEDED) {
                  this.spawnerApproachFailures++;
               }

               if (this.label.startsWith("walk back")) {
                  this.returnRouteFailures = AiNetherRun.isRouteFailure(r) ? this.returnRouteFailures + 1 : 0;
               }
            }

            void absorbChildEvidence(AiOperation.Result r) {
               JsonObject evidence = AiRunResources.parseEvidenceJson(r.dataJson());
               this.confirmedBarters = this.confirmedBarters + AiJson.getInt(evidence, "bartered", 0);
               boolean changed = this.confirmedBarters > AiNetherRun.savedConfirmedBarters;
               AiNetherRun.savedConfirmedBarters = this.confirmedBarters;
               if (changed) {
                  AiNetherRun.publishBarterReceipt();
               }

               this.goldSpent = this.goldSpent + AiJson.getInt(evidence, "goldSpent", 0);
            }

            AiOperation.Result load_AiOperation_Result_12() {
               this.confirmedBarters = Math.max(this.confirmedBarters, AiNetherRun.savedConfirmedBarters);
               if (this.write_BlockPos == null) {
                  this.write_BlockPos = AiNetherRun.rememberedSpawner;
               }

               if (this.read_Z) {
                  if (mc.player.getHealth() >= 18.0F && !AiEndRun.init_Z(mc) && !mc.player.isInLava()) {
                     this.read_Z = false;
                  } else {
                     if (AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0 || mc.player.getFoodData().getFoodLevel() >= 18) {
                        return this.startChild("recover after blaze retreat", AiCover.create(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90)));
                     }

                     AiNetherRun.init_Z = true;
                     this.stopReason = "no food for recovery after blaze retreat";
                  }
               }

               if (AiRunResources.isCampaignActive() && AiRunResources.missingMandatoryGear().keySet().stream().anyMatch(id -> !id.equals("minecraft:golden_boots"))) {
                  AiNetherRun.init_Z = true;
                  this.stopReason = "required equipment repair: " + AiRunResources.missingMandatoryGear().keySet();
                  return this.read_AiOperation_Result_8();
               }

               if (AiRunResources.isCampaignActive() && AiRunResources.countUsableItem("minecraft:golden_boots", 16) == 0) {
                  if (AiCraft.countItem("minecraft:gold_ingot") >= 4) {
                     return this.startChild(
                        "replace worn golden boots",
                        AiCraft.craft(
                           AiJson.objectOf("item", "minecraft:golden_boots", "count", AiCraft.countItem("minecraft:golden_boots") + 1, "timeoutSeconds", 30)
                        )
                     );
                  }

                  AiNetherRun.init_Z = true;
                  this.stopReason = "golden boots below their durability reserve";
                  return this.read_AiOperation_Result_8();
               } else {
                  if (AiRunResources.isCampaignActive() && !AiRunResources.isMandatoryArmorEquipped()) {
                     return this.startChild("restore required Nether armor", AiEquip.create(AiJson.objectOf("what", "armor")));
                  }

                  if (!AiNetherRun.init_Z
                     && AiSurvival.bestFoodSlot(mc.player.getInventory()) < 0
                     && (mc.player.getHealth() < 18.0F && mc.player.getFoodData().getFoodLevel() < 18 || mc.player.getFoodData().getFoodLevel() <= 6)) {
                     AiNetherRun.init_Z = true;
                     this.stopReason = "no carried food for continued travel or healing";
                     AiSpeedrun.log(
                        "[AI] nether: out of food at {} health, food {}; home for food with {} rods",
                        String.format("%.1f", mc.player.getHealth()),
                        mc.player.getFoodData().getFoodLevel(),
                        this.heldRods()
                     );
                  }

                  if (AiNetherRun.init_Z) {
                     return this.read_AiOperation_Result_8();
                  }

                  AiOperation.Result resumed = this.init_AiOperation_Result_16();
                  if (resumed != null) {
                     return resumed;
                  }

                  boolean pearlPhaseReady = AiNetherRun.isPearlPhaseReady(this.heldRods(), this.wantedRods, AiRunResources.isCampaignActive(), this.write_BlockPos != null, this.confirmedBarters);
                  int pearlsNow = this.heldPearls();
                  boolean pearlGround = false;
                  if (pearlPhaseReady && pearlsNow == 0 && !this.pearlTracker.groundPhase) {
                     BlockPos forest = AiWorldScan.findNearestBlock(AiNetherRun.WARPED_FOREST_BLOCK, 3);
                     pearlGround = AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).size() >= 2
                        || forest != null && forest.distSqr(mc.player.blockPosition()) <= 1600.0;
                  }

                  boolean wasStarted = this.pearlTracker.started;
                  boolean wasGround = this.pearlTracker.groundPhase;
                  boolean optionalPearls = this.pearlTracker
                     .pearlHuntWorthwhile(System.nanoTime(), pearlPhaseReady, pearlsNow, pearls, pearlGround, this.pearlsGivenUp, AiNetherRun.init_Z);
                  if (!wasStarted && this.pearlTracker.started) {
                     this.load_I = 0;
                  }

                  if (!wasGround && this.pearlTracker.groundPhase) {
                     AiSpeedrun.log("[AI] nether: pearl ground reached at {}; the pearl clock starts", mc.player.blockPosition().toShortString());
                  }

                  if (optionalPearls
                     && (this.heldPearls() < pearls || this.confirmedBarters == 0)
                     && this.hasBarterPiglinWithin(32.0)
                     && this.flush_I < 20
                     && System.nanoTime() > this.push_J) {
                     if (AiCraft.countItem("minecraft:gold_ingot") > 0) {
                        this.flush_I++;
                        AiNetherRun.PearlBudget goal = AiNetherRun.pearlBudget(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.confirmedBarters);
                        return this.startChild(
                           "barter an economical gold batch",
                           AiBarter.create(AiJson.objectOf("pearls", goal.pearls(), "gold", goal.gold(), "partners", 3, "timeoutSeconds", 180))
                        );
                     }

                     if (AiCraft.countItem("minecraft:gold_nugget") >= 9 && System.nanoTime() > this.poll_J) {
                        return this.startChild(
                           "convert nuggets for the next barter batch",
                           AiCraft.craft(
                              AiJson.objectOf(
                                 "item",
                                 "minecraft:gold_ingot",
                                 "count",
                                 AiCraft.countItem("minecraft:gold_ingot") + AiCraft.countItem("minecraft:gold_nugget") / 9,
                                 "timeoutSeconds",
                                 30
                              )
                           )
                        );
                     }
                  }

                  boolean committedGoldVein = this.init_aB_StreamUtil.init_FieldCache.isActive() && this.init_aB_StreamUtil.init_FieldCache.remainingCount() > 0;
                  boolean earlyGoldPartner = this.heldRods() < this.wantedRods && !committedGoldVein && this.hasBarterPiglinWithin(32.0);
                  if (this.heldPearls() < pearls
                     && !this.pearlsGivenUp
                     && !AiNetherRun.init_Z
                     && !this.pearlTracker.groundPhase
                     && this.open_I < 48
                     && this.push_I < 3
                     && (this.heldRods() >= this.wantedRods || committedGoldVein || earlyGoldPartner)) {
                     BlockPos ore = this.nextGoldTarget(s -> s.is(Blocks.NETHER_GOLD_ORE), false);
                     if (ore != null && AiNetherRun.init_Z_I(this.heldRods(), this.wantedRods, committedGoldVein, earlyGoldPartner, ore.distSqr(mc.player.blockPosition()))) {
                        this.open_I++;
                        this.pop_I = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                        return this.startChild(
                           "mine safe barter gold",
                           AiNetherRun.thenOnSuccess(
                              this.breakBlock(ore, 30),
                              () -> AiGameplay.write(
                                 AiJson.objectOf("item", "minecraft:gold_nugget", "count", 1, "additional", true, "radius", 10, "timeoutSeconds", 15)
                              )
                           )
                        );
                     }
                  }

                  if (System.nanoTime() > this.close_J) {
                     for (String loot : List.of("minecraft:ender_pearl", "minecraft:blaze_rod")) {
                        boolean short_ = loot.endsWith("pearl") ? this.heldPearls() < pearls : this.heldRods() < this.wantedRods;
                        if (short_ && AiNetherRun.init_Z_Str_D(loot, 12.0) && this.lootAttempts.merge(loot, 1, Integer::sum) <= 6) {
                           this.close_J = System.nanoTime() + 10000000000L;
                           return this.startChild(
                              "pick up the " + loot.substring(10).replace('_', ' ') + "s lying near",
                              AiGameplay.write(AiJson.objectOf("item", loot, "count", 64, "radius", 12, "timeoutSeconds", 15))
                           );
                        }
                     }
                  }

                  if (mc.player.getHealth() < 14.0F && (System.nanoTime() > this.nextHealAt || mc.player.getHealth() < 10.0F)) {
                     this.nextHealAt = System.nanoTime() + 30000000000L;
                     if ((mc.player.getHealth() < 10.0F || this.read_Z_7())
                        && (!AiWorldScan.findEntitiesByType("minecraft:blaze", 24.0).isEmpty() || !AiWorldScan.findEntitiesByType("minecraft:wither_skeleton", 16.0).isEmpty())) {
                        return this.startChild("hide and heal", AiCover.create(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90)));
                     }

                     if (mc.player.getFoodData().getFoodLevel() < 20 && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0) {
                        return this.startChild("eat to heal", AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 20)));
                     }
                  }

                  if (mc.player.getHealth() < 10.0F && mc.player.getFoodData().getFoodLevel() >= 18) {
                     return this.startChild("rest to heal at " + (int)mc.player.getHealth() + " health", AiCombat.init(100));
                  }

                  if (!this.shieldAttempted && !mc.player.getOffhandItem().is(Items.SHIELD) && AiCraft.countItem("minecraft:shield") > 0) {
                     this.shieldAttempted = true;
                     return this.startChild("shield to the offhand", AiEquip.create(AiJson.objectOf("what", "minecraft:shield", "hand", "offhand")));
                  }

                  float provokeHealth = AiForage.init(mc, mc.player.blockPosition()) ? 12.0F : 16.0F;
                  Entity provokable = optionalPearls
                        && this.heldPearls() < pearls
                        && mc.player.getHealth() >= provokeHealth
                        && System.nanoTime() > this.tick_J
                        && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0
                        && mc.player.onGround()
                        && AiForage.write(mc, mc.player.blockPosition())
                     ? this.findHuntableEnderman(48)
                     : null;
                  if (provokable != null) {
                     this.tick_J = System.nanoTime() + 5000000000L;
                     return this.startChild(
                        "fight an enderman",
                        AiNetherRun.fightThenPickup(
                           this.huntEnderman(provokable.getId()),
                           "minecraft:ender_pearl",
                           () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:ender_pearl", "count", 64, "radius", 16, "timeoutSeconds", 15))
                        )
                     );
                  }

                  if (optionalPearls
                     && this.heldPearls() < pearls
                     && mc.player.getHealth() >= 16.0F
                     && mc.player.onGround()
                     && System.nanoTime() > this.get_J
                     && !AiForage.write(mc, mc.player.blockPosition())
                     && AiForage.load(mc, mc.player.blockPosition())
                     && !AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).isEmpty()) {
                     this.get_J = System.nanoTime() + 10000000000L;
                     BlockPos under = mc.player.blockPosition().below();
                     return this.startChild(
                        "dig a fighting hole at " + under.toShortString(),
                        AiGameOperations.blockAction(
                           "break",
                           AiJson.objectOf("x", under.getX(), "y", under.getY(), "z", under.getZ(), "requireDrops", false, "timeoutSeconds", 8)
                        )
                     );
                  }

                  if (optionalPearls
                     && this.heldPearls() < pearls
                     && mc.player.getHealth() >= 16.0F
                     && mc.player.onGround()
                     && System.nanoTime() > this.run_J
                     && !AiForage.write(mc, mc.player.blockPosition())
                     && AiWorldScan.findEntitiesByType("minecraft:enderman", 24.0).size() >= 2) {
                     this.run_J = System.nanoTime() + 15000000000L;
                     BlockPos footing = this.init_BlockPos_I(12);
                     if (footing != null) {
                        return this.startChild(
                           "to ledge-free footing at " + footing.toShortString(),
                           AiGoto.travelToCompat(
                              footing,
                              0.5,
                              true,
                              true,
                              false,
                              20,
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              true,
                              2.0,
                              null,
                              AiGoto.GotoOptions.DEFAULT
                           )
                        );
                     }
                  }

                  Entity huntable = optionalPearls && this.heldPearls() < pearls && mc.player.getHealth() >= 16.0F ? this.endermanTarget(24, true) : null;
                  if (huntable != null) {
                     this.huntCooldowns.put(huntable.getId(), System.nanoTime() + 20000000000L);
                  }

                  if (huntable != null) {
                     return this.startChild(
                        "fight an enderman",
                        AiNetherRun.fightThenPickup(
                           this.guardedEndermanFight(
                              huntable.getId(),
                              AiCombat.init(
                                 AiJson.objectOf(
                                    "entityId", huntable.getId(), "count", 1, "radius", 24, "useShield", true, "retreatHealth", 6, "timeoutSeconds", 45
                                 )
                              )
                           ),
                           "minecraft:ender_pearl",
                           () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:ender_pearl", "count", 64, "radius", 16, "timeoutSeconds", 15))
                        )
                     );
                  }

                  if (optionalPearls
                     && this.heldRods() < this.wantedRods
                     && (this.heldPearls() < pearls || this.confirmedBarters == 0)
                     && AiCraft.countItem("minecraft:gold_ingot") >= 8
                     && this.flush_I < 1
                     && this.hasBarterPiglinWithin(32.0)) {
                     this.flush_I++;
                     AiNetherRun.PearlBudget goal = AiNetherRun.pearlBudget(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.confirmedBarters);
                     return this.startChild(
                        "barter with the piglins before the blazes",
                        AiBarter.create(
                           AiJson.objectOf(
                              "pearls",
                              goal.pearls(),
                              "gold",
                              goal.gold(),
                              "partners",
                              3,
                              "timeoutSeconds",
                              (int)Math.max(60L, Math.min(600L, (this.deadline - System.nanoTime()) / 1000000000L))
                           )
                        )
                     );
                  }

                  if (this.heldRods() < this.wantedRods || AiRunResources.isCampaignActive() && this.write_BlockPos == null) {
                     this.phase = AiNetherRun.RunPhase.FORTRESS;
                     if (this.load_BlockPos == null || System.nanoTime() > this.read_J) {
                        this.read_J = System.nanoTime() + 2000000000L;
                        List<BlockPos> found = AiWorldScan.findMobSpawners("minecraft:blaze", 12);
                        this.load_BlockPos = null;
                        long now = System.nanoTime();

                        for (BlockPos f : found) {
                           if (!this.read_Set.contains(f.asLong()) && AiNetherRun.init_Map.getOrDefault(f.asLong(), 0L) <= now) {
                              int heavy = this.init_I(f, 20);
                              if (heavy < 2) {
                                 this.load_BlockPos = f;
                                 break;
                              }

                              AiNetherRun.init_Map.put(f.asLong(), now + 180000000000L);
                              AiSpeedrun.log(
                                 "[AI] nether: {} hoglins round the spawner at {}; passing it over for three minutes", heavy, f.toShortString()
                              );
                           }
                        }
                     }

                     if (this.load_BlockPos != null) {
                        if (mc.player.distanceToSqr(Vec3.atCenterOf(this.load_BlockPos)) <= 256.0
                           && mc.level.getBlockEntity(this.load_BlockPos) instanceof SpawnerBlockEntity block
                           && "minecraft:blaze".equals(AiWorldScan.spawnerEntityId(block))) {
                           this.write_BlockPos = this.load_BlockPos.immutable();
                           if (!this.write_BlockPos.equals(AiNetherRun.rememberedSpawner)) {
                              AiNetherRun.rememberedSpawner = this.write_BlockPos;
                              AiNetherRun.publishBarterReceipt();
                           }
                        }

                        if (this.heldRods() < this.wantedRods) {
                           this.phase = AiNetherRun.RunPhase.RODS;
                           Entity blaze = this.nearestBlaze(24);
                           Entity inView = this.nearestVisibleBlaze(24);
                           if (inView != null) {
                              blaze = inView;
                           }

                           if (blaze != null && mc.player.hasLineOfSight(blaze)) {
                              boolean var42 = true;
                           } else {
                              boolean var41 = false;
                           }

                           if (this.heldRods() != this.lastRodCount) {
                              this.lastRodCount = this.heldRods();
                              this.set_J = System.nanoTime();
                           }

                           if (this.set_J == 0L || this.canHeadFor(this.load_BlockPos) || this.horizontalDistance(this.load_BlockPos) > 24.0) {
                              this.set_J = System.nanoTime();
                           }

                           if (System.nanoTime() - this.set_J > 150000000000L) {
                              this.set_J = System.nanoTime();
                              this.rodStallChecks++;
                              int around = AiWorldScan.findEntitiesByType("minecraft:blaze", 24.0).size();
                              if (blaze != null && around <= 2 && mc.player.getHealth() >= 16.0F && this.rodStallChecks <= 2) {
                                 AiSpeedrun.log(
                                    "[AI] nether: no rod for {} s at the spawner at {}; going after the blaze at {}",
                                    150L,
                                    this.load_BlockPos.toShortString(),
                                    blaze.blockPosition().toShortString()
                                 );
                                 this.flush_J = System.nanoTime();
                                 return this.startChild(
                                    "fight a blaze after a stall",
                                    AiNetherRun.fightThenPickup(
                                       AiBlazeFight.init(
                                          AiJson.objectOf(
                                             "count", AiNetherRun.init_I_I_I(this.wantedRods, this.heldRods()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 60
                                          )
                                       ),
                                       "minecraft:blaze_rod",
                                       () -> AiGameplay.write(
                                          AiJson.objectOf(
                                             "item",
                                             "minecraft:blaze_rod",
                                             "count",
                                             Math.min(this.wantedRods, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                             "radius",
                                             16,
                                             "timeoutSeconds",
                                             15
                                          )
                                       )
                                    )
                                 );
                              }

                              if (this.rodStallChecks >= 2) {
                                 AiSpeedrun.log(
                                    "[AI] nether: no rod for {} s at the spawner at {} ({} blazes round); leaving it",
                                    150L,
                                    this.load_BlockPos.toShortString(),
                                    around
                                 );

                                 for (BlockPos other : AiWorldScan.findMobSpawners("minecraft:blaze", 12)) {
                                    if (other.distSqr(this.load_BlockPos) <= 576.0) {
                                       this.read_Set.add(other.asLong());
                                    }
                                 }

                                 this.read_Set.add(this.load_BlockPos.asLong());
                                 this.load_BlockPos = null;
                                 this.rodStallChecks = 0;
                                 return this.running("Leaving a spawner that gave no rods");
                              }
                           }

                           boolean lavaLocked = this.isLavaNearby(this.load_BlockPos, 4);
                           if (lavaLocked
                              && !this.canHeadFor(this.load_BlockPos)
                              && (this.isLavaNearby(mc.player.blockPosition(), 3) || mc.player.blockPosition().distSqr(this.load_BlockPos) > 225.0)) {
                              BlockPos wait = this.lavaWaits < 6 ? this.load_BlockPos_2(this.load_BlockPos) : null;
                              if (wait == null && mc.player.blockPosition().distSqr(this.load_BlockPos) > 225.0) {
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] nether: the spawner at {} sits in lava with no spot to wait from; leaving it", this.load_BlockPos.toShortString()
                                    );
                                 this.read_Set.add(this.load_BlockPos.asLong());
                                 this.load_BlockPos = null;
                                 return this.running("Leaving a spawner in lava");
                              }

                              if (wait != null) {
                                 this.lavaWaits++;
                                 return this.startChild(
                                    "away from the lava round the spawner to " + wait.toShortString(),
                                    AiGoto.travelToCompat(
                                       wait,
                                       0.5,
                                       true,
                                       true,
                                       true,
                                       60,
                                       autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                       true,
                                       2.0,
                                       null,
                                       AiGoto.GotoOptions.DEFAULT
                                    )
                                 );
                              }
                           }

                           if (this.spawnerApproachFailures >= 4 && blaze == null) {
                              AiSpeedrun.log(
                                 "[AI] nether: {} walks to the spawner at {} made no progress; leaving it", this.spawnerApproachFailures, this.load_BlockPos.toShortString()
                              );
                              this.read_Set.add(this.load_BlockPos.asLong());
                              this.load_BlockPos = null;
                              this.spawnerApproachFailures = 0;
                              return this.running("Leaving a spawner no walk reaches");
                           }

                           if (blaze != null && AiPotion.shouldDrinkFireResistance(mc) && this.set_I < 3) {
                              this.set_I++;
                              return this.startChild("drink fire resistance", AiPotion.drinkFireResistance());
                           }

                           if (blaze != null && mc.player.getHealth() >= 10.0F) {
                              this.flush_J = System.nanoTime();
                              return this.startChild(
                                 "fight a blaze",
                                 AiNetherRun.fightThenPickup(
                                    AiBlazeFight.init(
                                       AiJson.objectOf(
                                          "count", AiNetherRun.init_I_I_I(this.wantedRods, this.heldRods()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 120
                                       )
                                    ),
                                    "minecraft:blaze_rod",
                                    () -> AiGameplay.write(
                                       AiJson.objectOf(
                                          "item",
                                          "minecraft:blaze_rod",
                                          "count",
                                          Math.min(this.wantedRods, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                          "radius",
                                          16,
                                          "timeoutSeconds",
                                          15
                                       )
                                    )
                                 )
                              );
                           }

                           if (this.flush_J == 0L) {
                              this.flush_J = System.nanoTime();
                           }

                           if (this.canHeadFor(this.load_BlockPos)) {
                              return this.startChild(
                                 "head for the blaze spawner at " + this.load_BlockPos.toShortString(), this.init_AiOperation_BlockPos_2(this.load_BlockPos)
                              );
                           }

                           if (!this.canSeeFromEye(this.load_BlockPos)
                              || this.horizontalDistance(this.load_BlockPos) > 4.5
                              || Math.abs(mc.player.getY() - this.load_BlockPos.getY()) > 2.5) {
                              BlockPos stand = lavaLocked ? null : this.init_BlockPos_BlockPos(this.load_BlockPos);
                              if (stand != null && !stand.equals(mc.player.blockPosition())) {
                                 return this.startChild(
                                    "walk to a stand by the blaze spawner at " + stand.toShortString(),
                                    AiGoto.travelToCompat(
                                       stand, 0.5, true, true, true, 90, this.routeStrategy(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                                    )
                                 );
                              }

                              if (stand == null
                                 && !lavaLocked
                                 && (this.horizontalDistance(this.load_BlockPos) > 2.5 || Math.abs(mc.player.getY() - this.load_BlockPos.getY()) > 2.0)) {
                                 return this.startChild(
                                    "walk to the blaze spawner at " + this.load_BlockPos.toShortString(),
                                    AiGoto.travelToCompat(
                                       this.load_BlockPos,
                                       2.0,
                                       true,
                                       true,
                                       true,
                                       120,
                                       this.routeStrategy(),
                                       true,
                                       2.0,
                                       null,
                                       AiGoto.GotoOptions.DEFAULT
                                    )
                                 );
                              }

                              if (lavaLocked && this.isLavaNearby(mc.player.blockPosition(), 3)) {
                                 BlockPos wait = this.load_BlockPos_2(this.load_BlockPos);
                                 if (wait != null) {
                                    return this.startChild(
                                       "wait for blazes away from the lava at " + wait.toShortString(),
                                       AiGoto.travelToCompat(
                                          wait,
                                          0.5,
                                          true,
                                          true,
                                          true,
                                          60,
                                          autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                          true,
                                          2.0,
                                          null,
                                          AiGoto.GotoOptions.DEFAULT
                                       )
                                    );
                                 }
                              }
                           }

                           this.write_I++;
                           if (!lavaLocked && mc.player.blockPosition().distSqr(this.load_BlockPos) < 36.0 && this.load_Set.add(this.load_BlockPos.asLong())) {
                              return this.startChild("prepare minimal blaze-spawner cap and fireball screen", new AiBlazeArena(this.load_BlockPos, 10));
                           } else {
                              return this.canEatToHeal()
                                 ? this.startChild("eat while waiting for blazes", AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 15)))
                                 : this.startChild("wait for blazes at the spawner", AiCombat.init(60));
                           }
                        } else {
                           if (this.write_BlockPos != null) {
                              return this.running("Actual blaze spawner visited and verified; material already sufficient");
                           }

                           BlockPos stand = this.init_BlockPos_BlockPos(this.load_BlockPos);
                           return this.startChild(
                              "walk to the blaze spawner for verification",
                              stand != null
                                 ? AiGoto.travelToCompat(stand, 0.8, true, true, true, 90, this.routeStrategy(), true)
                                 : this.init_AiOperation_BlockPos_2(this.load_BlockPos)
                           );
                        }
                     } else {
                        BlockPos bricks = AiWorldScan.findNearestBlock(AiNetherRun.FORTRESS_BLOCK, 12);
                        if (bricks != null && this.canHeadFor(bricks)) {
                           return this.startChild("head for the fortress at " + bricks.toShortString(), this.init_AiOperation_BlockPos_2(bricks));
                        } else if (bricks != null && this.init_Set.add(bricks.asLong() >> 6)) {
                           AiOperation.Result walk = this.startChild(
                              "walk into the fortress at " + bricks.toShortString(),
                              AiGoto.travelToCompat(
                                 bricks, 6.0, true, true, true, 180, this.routeStrategy(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                              )
                           );
                           this.write_J = bricks.asLong() >> 6;
                           return walk;
                        } else {
                           return this.exploreFor("a fortress");
                        }
                     }
                  } else {
                     if (this.heldPearls() < pearls && optionalPearls) {
                        this.phase = AiNetherRun.RunPhase.PEARLS;
                        if (AiCraft.countItem("minecraft:gold_block") > 0 && this.sync_I < 3) {
                           this.sync_I++;
                           return this.startChild(
                              "gold blocks into ingots",
                              AiCraft.craft(
                                 AiJson.objectOf(
                                    "item",
                                    "minecraft:gold_ingot",
                                    "count",
                                    AiCraft.countItem("minecraft:gold_ingot") + AiCraft.countItem("minecraft:gold_block") * 9,
                                    "timeoutSeconds",
                                    30
                                 )
                              )
                           );
                        }

                        if (AiCraft.countItem("minecraft:gold_ingot") > 0 && this.flush_I < 20 && this.hasBarterPiglinWithin(32.0) && System.nanoTime() > this.push_J) {
                           this.flush_I++;
                           AiNetherRun.PearlBudget goal = AiNetherRun.pearlBudget(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.confirmedBarters);
                           return this.startChild(
                              "barter with the piglins",
                              AiBarter.create(
                                 AiJson.objectOf(
                                    "pearls",
                                    goal.pearls(),
                                    "gold",
                                    goal.gold(),
                                    "partners",
                                    3,
                                    "timeoutSeconds",
                                    (int)Math.max(60L, Math.min(600L, (this.deadline - System.nanoTime()) / 1000000000L))
                                 )
                              )
                           );
                        }

                        if (AiCraft.countItem("minecraft:gold_nugget") >= 9 && System.nanoTime() > this.poll_J) {
                           return this.startChild(
                              "convert observed gold nuggets",
                              AiCraft.craft(
                                 AiJson.objectOf(
                                    "item",
                                    "minecraft:gold_ingot",
                                    "count",
                                    AiCraft.countItem("minecraft:gold_ingot") + AiCraft.countItem("minecraft:gold_nugget") / 9,
                                    "timeoutSeconds",
                                    30
                                 )
                              )
                           );
                        }

                        if (this.open_I < 48 && AiCraft.countItem("minecraft:gold_ingot") < 16 && this.hasBarterPiglinWithin(32.0)) {
                           BlockPos ore = this.nextGoldTarget(s -> s.is(Blocks.NETHER_GOLD_ORE), false);
                           if (ore != null) {
                              this.open_I++;
                              return this.startChild(
                                 "extract a safe Nether gold batch",
                                 AiNetherRun.thenOnSuccess(
                                    this.breakBlock(ore, 30),
                                    () -> AiGameplay.write(
                                       AiJson.objectOf(
                                          "item", "minecraft:gold_nugget", "count", 1, "additional", true, "radius", 10, "timeoutSeconds", 15
                                       )
                                    )
                                 )
                              );
                           }
                        }

                        if (this.open_I < 48) {
                           BlockPos gold = this.nextGoldTarget(s -> s.is(Blocks.GOLD_BLOCK), true);
                           if (gold != null) {
                              this.open_I++;
                              return this.startChild(
                                 "mine the gold block at " + gold.toShortString(),
                                 AiNetherRun.thenOnSuccess(
                                    this.breakBlock(gold, 20),
                                    () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:gold_block", "count", 64, "radius", 8, "timeoutSeconds", 10))
                                 )
                              );
                           }
                        }

                        Entity far = mc.player.getHealth() >= 16.0F ? this.endermanTarget(48, false) : null;
                        if (far != null) {
                           this.huntCooldowns.put(far.getId(), System.nanoTime() + 20000000000L);
                        }

                        if (far != null) {
                           return this.startChild(
                              "fight an enderman",
                              AiNetherRun.sequence(
                                 AiGoto.travelToCompat(
                                    far.blockPosition(),
                                    6.0,
                                    true,
                                    true,
                                    true,
                                    60,
                                    this.routeStrategy(),
                                    true,
                                    2.0,
                                    null,
                                    AiGoto.GotoOptions.DEFAULT
                                 ),
                                 () -> AiNetherRun.fightThenPickup(
                                    this.guardedEndermanFight(
                                       far.getId(),
                                       AiCombat.init(
                                          AiJson.objectOf(
                                             "entityId", far.getId(), "count", 1, "radius", 24, "useShield", true, "retreatHealth", 6, "timeoutSeconds", 45
                                          )
                                       )
                                    ),
                                    "minecraft:ender_pearl",
                                    () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:ender_pearl", "count", 64, "radius", 16, "timeoutSeconds", 15))
                                 )
                              )
                           );
                        }

                        BlockPos warped = AiWorldScan.findNearestBlock(AiNetherRun.WARPED_FOREST_BLOCK, 12);
                        if (warped != null && this.init_Set.contains(warped.asLong() >> 6)) {
                           warped = null;
                        }

                        if (warped == null) {
                           warped = AiNetherRun.load_List
                              .stream()
                              .filter(w -> !this.init_Set.contains(w.asLong() >> 6))
                              .min(Comparator.comparingDouble(w -> w.distSqr(mc.player.blockPosition())))
                              .orElse(null);
                        }

                        if (warped != null && this.canHeadFor(warped) && !this.init_Set.contains(warped.asLong() >> 6)) {
                           return this.startChild("head for the warped forest at " + warped.toShortString(), this.init_AiOperation_BlockPos_2(warped));
                        }

                        if (warped != null && this.horizontalDistance(warped) > 12.0 && this.init_Set.add(warped.asLong() >> 6)) {
                           AiOperation.Result walk = this.startChild(
                              "walk into the warped forest at " + warped.toShortString(),
                              AiGoto.travelToCompat(
                                 warped, 6.0, true, true, true, 180, this.routeStrategy(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                              )
                           );
                           this.write_J = warped.asLong() >> 6;
                           return walk;
                        }

                        if (this.heldPearls() > this.peakPearls) {
                           this.peakPearls = this.heldPearls();
                           this.endermanHuntAttempts = 0;
                        }

                        if (!AiWorldScan.findEntitiesByType("minecraft:enderman", 48.0).isEmpty() && this.endermanHuntAttempts < 24) {
                           if (!AiForage.init(mc, mc.player.blockPosition())
                              && mc.player.onGround()
                              && AiForage.load(mc, mc.player.blockPosition())
                              && System.nanoTime() > this.get_J) {
                              this.get_J = System.nanoTime() + 10000000000L;
                              BlockPos under = mc.player.blockPosition().below();
                              return this.startChild(
                                 "dig a fighting hole at " + under.toShortString(),
                                 AiGameOperations.blockAction(
                                    "break",
                                    AiJson.objectOf(
                                       "x", under.getX(), "y", under.getY(), "z", under.getZ(), "requireDrops", false, "timeoutSeconds", 8
                                    )
                                 )
                              );
                           }

                           if (this.canEatToHeal()) {
                              return this.startChild("eat while waiting for endermen", AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 15)));
                           }

                           this.endermanHuntAttempts++;
                           return this.startChild("wait for the endermen to come round (" + this.endermanHuntAttempts + " of 24)", AiCombat.init(100));
                        }

                        if (this.load_I < 14 || this.pearlTracker.groundPhase && AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).size() >= 2) {
                           if (this.load_I >= 14 && !AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).isEmpty()) {
                              return this.startChild("wait for the endermen to come round", AiCombat.init(40));
                           }

                           return this.exploreFor("endermen");
                        }
                     }

                     return this.read_AiOperation_Result_8();
                  }
               }
            }

            AiOperation.Result read_AiOperation_Result_8() {
               boolean routeRootProven = AiNetherRun.spawnerRouteReady(
                  this.write_BlockPos != null, this.confirmedBarters > 0, AiRunResources.isCampaignActive(), AiRunResources.blazePowderEquivalents(), 16, this.heldPearls() + AiCraft.countItem("minecraft:ender_eye")
               );
               if (AiRunResources.isCampaignActive() && !AiNetherRun.init_Z && this.write_BlockPos == null) {
                  return this.load_I < 14
                     ? this.exploreFor("an actual reachable blaze spawner")
                     : this.finish(AiOperation.Status.FAILED, "Required blaze spawner visit remains unverified");
               }

               if (AiRunResources.isCampaignActive() && !AiNetherRun.init_Z && this.confirmedBarters == 0 && !routeRootProven) {
                  return this.load_I < 14
                     ? this.exploreFor("safe adult piglins and barter gold")
                     : this.finish(
                        AiOperation.Status.FAILED,
                        "No genuine barter completed and the expedition's materials are short; required barter route remains unfinished"
                     );
               }

               this.phase = AiNetherRun.RunPhase.RETURN;
               BlockPos nearby = AiNetherRun.nearestPortalBlock(mc, mc.player.blockPosition(), 8);
               BlockPos loadedPortal = nearby != null
                  ? nearby
                  : this.init_aB_BufferHelper
                     .portal(
                        () -> AiWorldScan.findNearestBlock(s -> s.is(Blocks.NETHER_PORTAL), 12),
                        at -> AiTerrain.isLoaded(mc.level, at) && mc.level.getBlockState(at).is(Blocks.NETHER_PORTAL)
                     );
               this.portal = AiNetherRun.firstPlausiblePortal(loadedPortal, AiPortalLinks.nearestKnownPortal(2147483647), this.portal, at -> AiNetherRun.isPortalOrUnloaded(mc, at));
               if (this.portal != null) {
                  AiNetherRun.rememberedPortal = this.portal;
               }

               int buildSeconds = AiNetherRun.buildSeconds(this.deadline, System.nanoTime());
               if (this.portal == null) {
                  if (this.load_Z
                     || AiCraft.countItem("minecraft:obsidian") < 10
                     || AiCraft.countItem("minecraft:flint_and_steel") <= 0 && AiCraft.countItem("minecraft:fire_charge") <= 0) {
                     String message = "No observed return portal in loaded terrain or recorded transitions; the entry is unknown";
                     AiOperation.Result unknown = this.finish(AiOperation.Status.FAILED, message);
                     return new AiOperation.Result(
                        unknown.status(),
                        unknown.message(),
                        unknown.dataJson(),
                        unknown.evidence(),
                        unknown.status() == AiOperation.Status.FAILED ? AiFailure.TARGET_NOT_FOUND : unknown.failure()
                     );
                  }

                  if (buildSeconds == 0) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Not enough expedition time remains to build a confirmed portal home");
                  }

                  this.load_Z = true;
                  AiRunTrace.emitServiceEvent(
                     "nether_exit_plan",
                     AiJson.objectOf(
                        "choice", "remote_frame", "entryKnown", false, "loadedPortalSearchCompleted", this.init_aB_BufferHelper.isScanned(), "predictionOnly", true
                     )
                  );
                  return this.startChild(
                     "build a portal home from the unknown entry",
                     AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "build", true, "timeoutSeconds", buildSeconds))
                  );
               } else {
                  BlockPos door = AiNetherRun.nearestPortalBlock(mc, this.portal, 6);
                  double returnTicks = this.horizontalDistance(this.portal) / 0.22
                     + Math.abs(mc.player.getY() - this.portal.getY()) * 6.0
                     + this.returnRouteFailures * 1200;
                  double remoteTicks = 500.0;
                  if (door != null
                     || !(remoteTicks < returnTicks)
                     || this.load_Z
                     || AiCraft.countItem("minecraft:obsidian") < 10
                     || AiCraft.countItem("minecraft:flint_and_steel") <= 0 && AiCraft.countItem("minecraft:fire_charge") <= 0) {
                     if (door == null && this.canHeadFor(this.portal)) {
                        return this.startChild(
                           "head for the entry portal at " + this.portal.toShortString(), this.init_AiOperation_BlockPos_2(this.portal)
                        );
                     }

                     if (door != null || !(this.horizontalDistance(this.portal) > 12.0) && !(Math.abs(mc.player.getY() - this.portal.getY()) > 6.0)) {
                        if (door == null) {
                           return this.finish(AiOperation.Status.FAILED, "The entry portal at " + this.portal.toShortString() + " is gone");
                        } else {
                           return !(this.horizontalDistance(door) > 2.0) && !(Math.abs(mc.player.getY() - door.getY()) > 2.0)
                              ? this.startChild(
                                 "through the portal", AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "timeoutSeconds", 60))
                              )
                              : this.startChild(
                                 "walk back to the portal at " + door.toShortString(),
                                 AiGoto.travelToCompat(
                                    door, 1.0, true, true, true, 300, this.routeStrategy(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                                 )
                              );
                        }
                     } else {
                        return this.startChild(
                           "walk back toward the entry portal at " + this.portal.toShortString(),
                           AiGoto.travelToCompat(
                              this.portal,
                              3.0,
                              true,
                              true,
                              true,
                              300,
                              this.routeStrategy(),
                              true,
                              2.0,
                              null,
                              AiGoto.GotoOptions.DEFAULT
                           )
                        );
                     }
                  } else {
                     if (buildSeconds == 0) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Not enough expedition time remains to build a confirmed portal home");
                     }

                     this.load_Z = true;
                     AiRunTrace.emitServiceEvent(
                        "nether_exit_plan",
                        AiJson.objectOf(
                           "choice",
                           "remote_frame",
                           "estimatedReturnTicks",
                           returnTicks,
                           "estimatedBuildAndArrivalTicks",
                           remoteTicks,
                           "arrivalUncertaintyTicks",
                           240,
                           "predictionOnly",
                           true
                        )
                     );
                     return this.startChild(
                        "build a portal home here",
                        AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "build", true, "timeoutSeconds", buildSeconds))
                     );
                  }
               }
            }

            AiOperation.Result exploreFor(String what) {
               if (this.load_I >= 28) {
                  return this.finish(AiOperation.Status.FAILED, "Explored " + this.load_I + " legs and found no " + what);
               }

               this.load_I++;
               ClientLevel level = mc.level;
               AiNetherRun.init_Obj = level;
               BlockPos at = mc.player.blockPosition();
               this.pathOrigin = at;
               int cx = at.getX() >> 4;
               int cz = at.getZ() >> 4;

               for (int dx = -5; dx <= 5; dx++) {
                  for (int dz = -5; dz <= 5; dz++) {
                     AiNetherRun.init_Set.add(ChunkPos.pack(cx + dx, cz + dz));
                  }
               }

               int best = -1;
               int bestUnseen = -1;
               BlockPos bastion = AiWorldScan.findNearestBlock(AiNetherRun.BASTION_BLOCK, 12);
               if (bastion != null && AiNetherStructures.bastionAt(bastion).confidence() < 0.5) {
                  bastion = null;
               }

               for (int i = 0; i < 8; i++) {
                  int h = this.read_I + i & 7;
                  BlockPos to = this.exploreWaypoint(at, h);
                  if (!AiNetherRun.nearFailedTarget(to) && (bastion == null || !this.routePassesNear(at, to, bastion))) {
                     int unseen = 0;
                     int tx = to.getX() >> 4;
                     int tz = to.getZ() >> 4;

                     for (int dx = -12; dx <= 12; dx += 2) {
                        for (int dz = -12; dz <= 12; dz += 2) {
                           if (!AiNetherRun.init_Set.contains(ChunkPos.pack(tx + dx, tz + dz))) {
                              unseen++;
                           }
                        }
                     }

                     if (unseen > bestUnseen) {
                        bestUnseen = unseen;
                        best = h;
                     }
                  }
               }

               if (best < 0 && what.equals("endermen")) {
                  this.pearlsGivenUp = true;
                  return this.running("No way left to look for endermen; heading home");
               }

               if (best < 0 && !this.update_Z) {
                  this.update_Z = true;
                  AiNetherRun.init_List.clear();
                  return this.running("Every way out failed once; trying them again");
               }

               if (best < 0) {
                  return this.finish(AiOperation.Status.FAILED, "Every way out from here failed; no " + what + " found");
               }

               this.read_I = best;
               BlockPos to = this.exploreWaypoint(at, this.read_I);
               this.pathTarget = to;
               AiOperation.Result leg = this.startChild(
                  "explore for " + what + " toward " + to.toShortString(),
                  AiGoto.createPersistentOpCompat(to, new AiNetherRun.RadiusGoal(to.getX(), to.getZ(), 10), 90),
                  0,
                  true
               );
               this.pathTarget = to;
               return leg;
            }

            BlockPos exploreWaypoint(BlockPos at, int h) {
               double a = Math.toRadians(h * 45.0);
               return new BlockPos(at.getX() + (int)Math.round(Math.cos(a) * 96.0), at.getY(), at.getZ() + (int)Math.round(Math.sin(a) * 96.0));
            }

            void init_V_18() {
               if (this.child != null) {
                  if (this.label.startsWith("mine safe barter gold") || this.label.startsWith("extract a safe Nether gold batch")) {
                     this.lastLabel = this.label;
                  } else if (this.label.startsWith("fight a blaze") && this.childResult != null) {
                     JsonObject data = AiRunResources.parseEvidenceJson(this.childResult.dataJson());
                     if (data.get("target") instanceof JsonObject target && AiJson.getInt(target, "id", -1) >= 0) {
                        Entity entity = mc.level.getEntity(AiJson.getInt(target, "id", -1));
                        if (entity instanceof Blaze && entity.isAlive()) {
                           this.targetEntityId = entity.getId();
                        }
                     }
                  }
               }
            }

            AiOperation.Result startChild(String label, AiOperation op) {
               return this.startChild(label, op, 0);
            }

            AiOperation.Result startChild(String label, AiOperation op, int waitTicks) {
               return this.startChild(label, op, waitTicks, false);
            }

            AiOperation.Result startChild(String label, AiOperation op, int waitTicks, boolean session) {
               this.label = label;
               this.childResult = null;
               this.write_J = -9223372036854775808L;
               this.sync_J = System.nanoTime();
               AiSpeedrun.log("[AI] nether: {}", label);
               AiOperation inner = op;
               if (session && op instanceof AiGoto.GotoOp travel) {
                  inner = AiNetherRun.init(travel);
               }

               if (waitTicks > 0) {
                  inner = AiNetherRun.delayThen(waitTicks, inner);
               }

               this.child = AiRunDiagnostics.trace("nether", label, inner);
               return this.running(label);
            }

            AiOperation init_AiOperation_BlockPos_2(BlockPos target) {
               double dx = target.getX() + 0.5 - mc.player.getX();
               double dz = target.getZ() + 0.5 - mc.player.getZ();
               int turn = this.headForFailures == 0 ? 0 : (this.headForFailures % 2 == 1 ? 1 : -1) * ((this.headForFailures + 1) / 2);
               double bearing = Math.atan2(dz, dx) + Math.toRadians(45.0) * turn;
               int x = mc.player.getBlockX() + (int)Math.round(Math.cos(bearing) * 48.0);
               int z = mc.player.getBlockZ() + (int)Math.round(Math.sin(bearing) * 48.0);
               return AiGoto.travelToCompat(
                  new BlockPos(x, mc.player.getBlockY(), z),
                  4.0,
                  true,
                  true,
                  true,
                  90,
                  this.routeStrategy(),
                  true,
                  2.0,
                  new AiNetherRun.RadiusGoal(x, z, 6),
                  AiGoto.GotoOptions.DEFAULT
               );
            }

            boolean canHeadFor(BlockPos target) {
               return this.horizontalDistance(target) > 64.0 && this.headForFailures < 5;
            }

            autismclient.ai.engine.AiPathfinder.RouteStrategy routeStrategy() {
               return this.put_I > 0 ? autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST : autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST;
            }

            AiOperation guardedEndermanFight(final int enderman, final AiOperation fight) {
               return new AiOperation() {
                  int offGroundTicks;

                  @Override
                  public AiOperation.Result tick() {
                     if (mc.player != null && mc.level != null) {
                        Entity target = mc.level.getEntity(enderman);
                        if (target != null && target.isAlive() && !AiForage.init(mc, mc.player.blockPosition()) && !write(target.blockPosition())) {
                           fight.cancel();
                           return AiGameOperations.result(
                              AiOperation.Status.FAILED, "The enderman went where a knock would not land on ground", new JsonObject()
                           );
                        } else {
                           this.offGroundTicks = mc.player.onGround() && !write(mc.player.blockPosition()) ? this.offGroundTicks + 1 : 0;
                           if (this.offGroundTicks > 10) {
                              fight.cancel();
                              return AiGameOperations.result(AiOperation.Status.FAILED, "Off safe ground in the hunt", new JsonObject());
                           } else {
                              return fight.tick();
                           }
                        }
                     } else {
                        fight.cancel();
                        return AiGameOperations.result(AiOperation.Status.CANCELLED, "Left the world", new JsonObject());
                     }
                  }

                  @Override
                  public boolean canInterruptNow() {
                     return fight.canInterruptNow();
                  }

                  @Override
                  public boolean requiresReconciliationAfterEmergency() {
                     return fight.requiresReconciliationAfterEmergency();
                  }

                  @Override
                  public void cancel() {
                     fight.cancel();
                  }
               };
            }

            BlockPos init_BlockPos_I(int r) {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(feet.offset(-r, -3, -r), feet.offset(r, 3, r))) {
                  if (mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                     && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                     && !mc.level.getBlockState(p.below()).getCollisionShape(mc.level, p.below()).isEmpty()
                     && mc.level.getFluidState(p).isEmpty()
                     && AiForage.write(mc, p)) {
                     double d = p.distSqr(feet);
                     if (d < bestD) {
                        bestD = d;
                        best = p.immutable();
                     }
                  }
               }

               return best;
            }

            boolean canEatToHeal() {
               return mc.player.getHealth() < mc.player.getMaxHealth() && mc.player.getFoodData().getFoodLevel() < 18 && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0;
            }

            Entity findHuntableEnderman(int radius) {
               long now = System.nanoTime();

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:enderman", radius)) {
                  Long until = this.huntCooldowns.get(e.getId());
                  if ((until == null || now >= until)
                     && !(e.getY() < mc.player.getY() - 16.0)
                     && !(e.getY() > mc.player.getY() + 16.0)
                     && mc.player.hasLineOfSight(e)) {
                     return e;
                  }
               }

               return null;
            }

            AiOperation huntEnderman(int enderman) {
               return AiForage.huntEnderman(enderman, p -> AiForage.write(mc, p), () -> this.huntCooldowns.put(enderman, System.nanoTime() + 120000000000L));
            }

            Entity endermanTarget(int radius, boolean here) {
               if (here && !this.write(mc.player.blockPosition())) {
                  return null;
               }

               long now = System.nanoTime();

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:enderman", radius)) {
                  Long until = this.huntCooldowns.get(e.getId());
                  if ((until == null || now >= until) && this.write(e.blockPosition())) {
                     return e;
                  }
               }

               return null;
            }

            boolean write(BlockPos at) {
               return AiForage.read(mc, at);
            }

            boolean canSeeFromEye(BlockPos pos) {
               return this.canSee(mc.player.getEyePosition(), pos);
            }

            boolean canSee(Vec3 eye, BlockPos pos) {
               BlockHitResult hit = mc.level.clip(new ClipContext(eye, Vec3.atCenterOf(pos), Block.COLLIDER, Fluid.NONE, mc.player));
               return hit.getType() == Type.MISS || hit.getBlockPos().equals(pos);
            }

            BlockPos init_BlockPos_BlockPos(BlockPos spawner) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(spawner.offset(-4, -2, -4), spawner.offset(4, 1, 4))) {
                  double dx = p.getX() + 0.5 - (spawner.getX() + 0.5);
                  double dz = p.getZ() + 0.5 - (spawner.getZ() + 0.5);
                  double flat = Math.hypot(dx, dz);
                  if (!(flat < 1.5) && !(flat > 4.5)) {
                     BlockPos below = p.below();
                     BlockState floor = mc.level.getBlockState(below);
                     if (!floor.getCollisionShape(mc.level, below).isEmpty()
                        && floor.getFluidState().isEmpty()
                        && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                        && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                        && mc.level.getFluidState(p).isEmpty()
                        && mc.level.getFluidState(p.above()).isEmpty()
                        && this.canSee(new Vec3(p.getX() + 0.5, p.getY() + 1.62, p.getZ() + 0.5), spawner)
                        && AiCombat.isSafeStep(mc, p)
                        && !this.isLavaNearby(p, 3)) {
                        double d = mc.player.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5) + (this.hasGroundAround(p) ? 0 : 400);
                        if (d < bestD) {
                           bestD = d;
                           best = p.immutable();
                        }
                     }
                  }
               }

               return best;
            }

            boolean isLavaNearby(BlockPos feet, int r) {
               for (BlockPos c : BlockPos.betweenClosed(feet.offset(-r, -1, -r), feet.offset(r, 4, r))) {
                  if (mc.level.getFluidState(c).is(FluidTags.LAVA)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos load_BlockPos_2(BlockPos spawner) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(spawner.offset(-14, -4, -14), spawner.offset(14, 3, 14))) {
                  double flat = Math.hypot(p.getX() - spawner.getX(), p.getZ() - spawner.getZ());
                  if (!(flat < 6.0) && !(flat > 14.0)) {
                     BlockPos below = p.below();
                     if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()
                        && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                        && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                        && mc.level.getFluidState(p).isEmpty()
                        && mc.level.getFluidState(p.above()).isEmpty()) {
                        double d = mc.player.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
                        if (!(d >= bestD) && AiCombat.isSafeStep(mc, p) && !this.isLavaNearby(p, 3) && this.hasGroundAround(p)) {
                           if (!this.canSee(new Vec3(p.getX() + 0.5, p.getY() + 1.62, p.getZ() + 0.5), spawner)) {
                              d += 400.0;
                           }

                           if (!(d >= bestD)) {
                              bestD = d;
                              best = p.immutable();
                           }
                        }
                     }
                  }
               }

               return best;
            }

            boolean hasGroundAround(BlockPos feet) {
               for (int dx = -2; dx <= 2; dx++) {
                  for (int dz = -2; dz <= 2; dz++) {
                     boolean ground = false;

                     for (int dy = 0; dy >= -6; dy--) {
                        BlockPos c = feet.offset(dx, dy - 1, dz);
                        if (mc.level.getFluidState(c).is(FluidTags.LAVA)) {
                           return false;
                        }

                        if (dy >= -2 && !mc.level.getBlockState(c).getCollisionShape(mc.level, c).isEmpty()) {
                           ground = true;
                           break;
                        }
                     }

                     if (!ground) {
                        return false;
                     }
                  }
               }

               return true;
            }

            Entity nearestBlaze(int radius) {
               Entity best = null;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (e.isAlive() && !this.isBlazeDeferred(e) && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
                     best = e;
                  }
               }

               return best;
            }

            boolean read_Z_7() {
               List<Entity> blazes = AiWorldScan.findEntitiesByType("minecraft:blaze", 24.0);
               if (blazes.size() >= 2) {
                  return true;
               }

               for (Entity e : blazes) {
                  if (e.isAlive() && mc.player.hasLineOfSight(e)) {
                     return true;
                  }
               }

               return false;
            }

            boolean isBlazeDeferred(Entity e) {
               Long until = AiBlazeFight.deferredUntil.get(e.getUUID());
               return until != null && until > System.nanoTime();
            }

            int init_I(BlockPos at, int radius) {
               int n = 0;

               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e.isAlive() && !(e.blockPosition().distSqr(at) > radius * radius)) {
                     if (e instanceof Hoglin || e instanceof Zoglin) {
                        n++;
                     } else if (e instanceof PiglinBrute) {
                        n += 2;
                     }
                  }
               }

               return n;
            }

            Entity nearestVisibleBlaze(int radius) {
               Entity best = null;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (e.isAlive()
                     && !this.isBlazeDeferred(e)
                     && mc.player.hasLineOfSight(e)
                     && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
                     best = e;
                  }
               }

               return best;
            }

            int visibleBlazeCount(int radius) {
               int n = 0;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (mc.player.hasLineOfSight(e)) {
                     n++;
                  }
               }

               return n;
            }

            int heldRods() {
               return AiRunResources.blazePowderEquivalents() / 2;
            }

            int heldPearls() {
               return AiCraft.countItem("minecraft:ender_pearl") + AiCraft.countItem("minecraft:ender_eye");
            }

            double horizontalDistance(BlockPos p) {
               return Math.hypot(p.getX() + 0.5 - mc.player.getX(), p.getZ() + 0.5 - mc.player.getZ());
            }

            JsonObject init_JsonObject_31() {
               if (this.load_BlockPos != null && AiTerrain.isLoaded(mc.level, this.load_BlockPos)) {
                  if (mc.level.getBlockEntity(this.load_BlockPos) instanceof SpawnerBlockEntity block) {
                     AutismBaseSpawnerAccessor var4 = (AutismBaseSpawnerAccessor)block.getSpawner();
                     int nearby = (int)AiWorldScan.findEntitiesByType("minecraft:blaze", 32.0)
                        .stream()
                        .filter(e -> e.blockPosition().distSqr(this.load_BlockPos) < 256.0)
                        .count();
                     return AiJson.objectOf(
                        "loaded",
                        true,
                        "mob",
                        AiWorldScan.spawnerEntityId(block),
                        "clientDelay",
                        var4.autism$getSpawnDelay(),
                        "clientActivationRange",
                        var4.autism$getRequiredPlayerRange(),
                        "clientNearbyLimit",
                        var4.autism$getMaxNearbyEntities(),
                        "clientSpawnRange",
                        var4.autism$getSpawnRange(),
                        "distance",
                        Math.sqrt(mc.player.distanceToSqr(Vec3.atCenterOf(this.load_BlockPos))),
                        "nearbyBlazesObserved",
                        nearby,
                        "possibleNearbyCap",
                        nearby >= var4.autism$getMaxNearbyEntities(),
                        "serverConfigurationFullyKnown",
                        false
                     );
                  } else {
                     return AiJson.objectOf("loaded", true, "stillSpawner", false);
                  }
               } else {
                  return AiJson.objectOf("loaded", false);
               }
            }

            JsonObject load_JsonObject_3() {
               if (mc.player != null && mc.level != null) {
                  long now = System.nanoTime();
                  if (now < this.put_J) {
                     return this.init_JsonObject;
                  }

                  this.put_J = now + 1000000000L;
                  List<Entity> nearby = AiWorldScan.findEntitiesByType("minecraft:enderman", 48.0);
                  JsonArray candidates = new JsonArray();

                  for (Entity target : nearby) {
                     if (candidates.size() >= 8) {
                        break;
                     }

                     candidates.add(
                        AiJson.objectOf(
                           "id",
                           target.getId(),
                           "uuid",
                           target.getUUID().toString(),
                           "position",
                           target.blockPosition().toShortString(),
                           "distance",
                           target.distanceTo(mc.player),
                           "heightDifference",
                           target.getY() - mc.player.getY(),
                           "withinProvokeHeight",
                           Math.abs(target.getY() - mc.player.getY()) <= 16.0,
                           "lineOfSight",
                           mc.player.hasLineOfSight(target),
                           "targetArenaSafe",
                           this.write(target.blockPosition()),
                           "retryInSeconds",
                           Math.max(0L, (this.huntCooldowns.getOrDefault(target.getId(), 0L) - now) / 1000000000L)
                        )
                     );
                  }

                  this.init_JsonObject = AiJson.objectOf(
                     "observedAtMs",
                     System.currentTimeMillis(),
                     "trackedWithin48",
                     nearby.size(),
                     "candidates",
                     candidates,
                     "candidateListCapped",
                     nearby.size() > candidates.size(),
                     "bodyOnGround",
                     mc.player.onGround(),
                     "bodyLedgeFree",
                     AiForage.write(mc, mc.player.blockPosition()),
                     "bodyArenaSafe",
                     this.write(mc.player.blockPosition()),
                     "bodyWalled",
                     AiForage.init(mc, mc.player.blockPosition()),
                     "safeFoodAvailable",
                     AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0,
                     "provokeRetryInSeconds",
                     Math.max(0L, (this.tick_J - now) / 1000000000L),
                     "waitsUsed",
                     this.endermanHuntAttempts,
                     "waitsLimit",
                     24
                  );
                  return this.init_JsonObject;
               } else {
                  return this.init_JsonObject;
               }
            }

            JsonObject progressSnapshot() {
               return AiJson.objectOf(
                  "step",
                  this.phase == null ? "" : this.phase.name(),
                  "rods",
                  this.heldRods(),
                  "pearls",
                  this.heldPearls(),
                  "legs",
                  this.load_I,
                  "waits",
                  this.write_I,
                  "rawRods",
                  AiCraft.countItem("minecraft:blaze_rod"),
                  "powderSideEyes",
                  AiRunResources.blazePowderEquivalents(),
                  "genuineBarters",
                  this.confirmedBarters,
                  "goldSpent",
                  this.goldSpent,
                  "pearlRecovery",
                  pearlRecovery,
                  "rodGoal",
                  this.wantedRods,
                  "returningForSupplies",
                  AiNetherRun.init_Z,
                  "returnReason",
                  this.stopReason,
                  "pearlSearchEvidence",
                  this.load_JsonObject_3(),
                  "spawnerEvidence",
                  this.init_JsonObject_31(),
                  "confirmedBlazeSpawner",
                  this.write_BlockPos == null ? "" : this.write_BlockPos.toShortString(),
                  "eyeBudget",
                  AiRunResources.eyeBudget(pearls, 12, 0).data(),
                  "spawner",
                  this.load_BlockPos == null ? "" : this.load_BlockPos.toShortString(),
                  "entry",
                  this.portal == null ? "" : this.portal.toShortString(),
                  "entryKnown",
                  this.portal != null,
                  "loadedReturnPortalSearchCompleted",
                  this.init_aB_BufferHelper.isScanned(),
                  "steps",
                  this.init_JsonArray
               );
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressSnapshot());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finish(status, message, this.childResult);
            }

            AiOperation.Result finish(AiOperation.Status status, String message, AiOperation.Result child) {
               AiGoal.clear(this);
               boolean pending = this.requiresReconciliationAfterEmergency();
               AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, this.progressSnapshot(), pending, child);
               this.reobserveBeforeResuming = this.reobserveBeforeResuming | (pending || AiJson.getBoolean(AiRunResources.parseEvidenceJson(result.dataJson()), "reobserveBeforeResuming", false));
               if (this.child != null) {
                  this.child.cancel();
                  this.child = null;
               }

               this.guard.cancel();
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.reobserveBeforeResuming && (this.child == null || this.child.canInterruptNow()) && this.guard.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.reobserveBeforeResuming || this.child != null && this.child.requiresReconciliationAfterEmergency() || this.guard.requiresReconciliation();
            }

            @Override
            public void cancel() {
               AiGoal.clear(this);
               this.reobserveBeforeResuming = this.reobserveBeforeResuming | this.requiresReconciliationAfterEmergency();
               if (this.child != null) {
                  this.child.cancel();
                  this.child = null;
               }

               this.guard.cancel();
            }
         };
      } else {
         throw new IllegalArgumentException("The Nether run needs a connected living player");
      }
   }

   static AiNetherRun.RouteReceipt parseRouteReceipt(JsonObject stored) {
      try {
         if (!"OBSERVED_NETHER_ROUTE".equals(AiJson.getString(stored, "source", ""))) {
            return null;
         } else {
            int barters = AiJson.getInt(stored, "barters", 0);
            if (barters >= 1 && barters <= 1000000 && stored.has("spawnerX") && stored.has("spawnerY") && stored.has("spawnerZ")) {
               int x = stored.get("spawnerX").getAsInt();
               int y = stored.get("spawnerY").getAsInt();
               int z = stored.get("spawnerZ").getAsInt();
               long at = stored.get("observedAtMs").getAsLong();
               return Math.abs((long)x) <= 30000000L && Math.abs((long)z) <= 30000000L && y >= -2048 && y <= 2048 && at > 0L
                  ? new AiNetherRun.RouteReceipt(barters, new BlockPos(x, y, z), at)
                  : null;
            } else {
               return null;
            }
         }
      } catch (RuntimeException malformed) {
         return null;
      }
   }

   private static void refreshRouteMemory() {
      Minecraft mc = Minecraft.getInstance();
      String currentWorld = AiWorldIdentity.currentWorldKey(mc);
      if (!currentWorld.equals(routeWorldKey) || routeConnection != mc.getConnection()) {
         routeWorldKey = currentWorld;
         routeConnection = mc.getConnection();
         routeGeneration++;
         routeMemoryRequested = false;
         rememberedPortal = null;
         load_List.clear();
         init_Set.clear();
         init_List.clear();
         init_Map.clear();
         flush_I = 0;
         load_Obj = null;
         savedConfirmedBarters = 0;
         rememberedSpawner = null;
         init_Z = false;
         read_Z = false;
      }

      AiService service = AiService.getExistingInstance();
      if (!routeMemoryRequested && service != null && mc.getConnection() != null) {
         String requestedWorld = routeWorldKey;
         Object requestedConnection = routeConnection;
         long generation = routeGeneration;
         routeMemoryRequested = service.loadNetherRouteMemory(
            requestedWorld,
            entries -> {
               if (generation == routeGeneration
                  && requestedWorld.equals(routeWorldKey)
                  && routeConnection == requestedConnection
                  && mc.getConnection() == requestedConnection
                  && AiWorldIdentity.currentWorldKey(mc).equals(requestedWorld)) {
                  for (JsonElement entry : entries) {
                     AiNetherRun.RouteReceipt receipt = entry.isJsonObject() ? parseRouteReceipt(entry.getAsJsonObject()) : null;
                     if (receipt != null) {
                        savedConfirmedBarters = Math.max(savedConfirmedBarters, receipt.barters());
                        if (rememberedSpawner == null) {
                           rememberedSpawner = receipt.spawner();
                        }

                        AiRunTrace.emitServiceEvent("nether_route_history", receipt.toJson());
                     }
                  }
               }
            }
         );
      }
   }

   private static void publishBarterReceipt() {
      AiService service = AiService.getExistingInstance();
      if (service != null && savedConfirmedBarters > 0 && rememberedSpawner != null) {
         JsonArray receipts = new JsonArray();
         receipts.add(new AiNetherRun.RouteReceipt(savedConfirmedBarters, rememberedSpawner, System.currentTimeMillis()).toJson());
         service.saveNetherRouteMemory(routeWorldKey, receipts);
      }
   }

   static void init_V() {
      refreshRouteMemory();
      read_Z = true;
   }

   static boolean init_Z() {
      refreshRouteMemory();
      return read_Z;
   }

   static boolean load_Z() {
      refreshRouteMemory();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && routeWorldKey.equals(AiWorldIdentity.currentWorldKey(mc)) && routeConnection == mc.getConnection()) {
         return read_Z
            ? true
            : spawnerRouteReady(rememberedSpawner != null, savedConfirmedBarters > 0, AiRunResources.isCampaignActive(), AiRunResources.blazePowderEquivalents(), 16, AiCraft.countItem("minecraft:ender_eye") + AiCraft.countItem("minecraft:ender_pearl"));
      } else {
         return false;
      }
   }

   static void load_V() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         refreshRouteMemory();
         savedConfirmedBarters = Math.max(savedConfirmedBarters, 1);
         if (rememberedSpawner == null) {
            rememberedSpawner = BlockPos.ZERO;
         }
      }
   }

   static boolean spawnerRouteReady(boolean spawnerConfirmed, boolean bartersObserved, boolean campaign, int rodPowder, int wantedPowder, int pearlEquivalents) {
      return spawnerConfirmed && (bartersObserved || campaign && rodPowder >= wantedPowder && pearlEquivalents >= 16);
   }

   static BlockPos nearestPortalBlock(Minecraft mc, BlockPos around, int radius) {
      BlockPos best = null;
      double bestD = 1.7976931348623157E308;

      for (BlockPos p : BlockPos.betweenClosed(around.offset(-radius, -radius, -radius), around.offset(radius, radius, radius))) {
         if (AiTerrain.isLoaded(mc.level, p) && mc.level.getBlockState(p).is(Blocks.NETHER_PORTAL)) {
            double d = p.distSqr(around);
            if (d < bestD) {
               bestD = d;
               best = p.immutable();
            }
         }
      }

      return best;
   }

   private static boolean isPortalOrUnloaded(Minecraft mc, BlockPos at) {
      return !AiTerrain.isLoaded(mc.level, at) || mc.level.getBlockState(at).is(Blocks.NETHER_PORTAL);
   }

   static AiOperation init(final AiGoto.GotoOp travel) {
      return new AiOperation() {
         @Override
         public AiOperation.Result tick() {
            if (travel.isAtGoal()) {
               travel.cancel();
               return AiGameOperations.result(AiOperation.Status.SUCCEEDED, "Arrived", new JsonObject());
            } else {
               return travel.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return travel.canInterrupt();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return travel.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            travel.cancel();
         }
      };
   }

   static AiOperation sequence(AiOperation first, Supplier<AiOperation> second) {
      return sequence(first, second, false);
   }

   static AiOperation then(AiOperation first, Supplier<AiOperation> second) {
      return sequence(first, second, true);
   }

   static AiOperation thenOnSuccess(AiOperation first, Supplier<AiOperation> second) {
      return sequence(first, second, true);
   }

   private static AiOperation sequence(final AiOperation first, final Supplier<AiOperation> second, final boolean successOnly) {
      return new AiOperation() {
         AiOperation current = first;
         boolean switched;
         boolean needsReconciliation;
         AiOperation.Result result;

         @Override
         public AiOperation.Result tick() {
            if (this.result != null) {
               return this.result;
            }

            AiOperation.Result r = this.current.tick();
            if (!r.terminal()) {
               return r;
            }

            this.needsReconciliation = this.current.requiresReconciliationAfterEmergency();
            r = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.current, r);
            this.needsReconciliation = this.needsReconciliation | AiJson.getBoolean(AiRunResources.parseEvidenceJson(r.dataJson()), "reobserveBeforeResuming", false);
            if (this.switched
               || r.status() == AiOperation.Status.UNCERTAIN
               || r.status() == AiOperation.Status.CANCELLED
               || successOnly && r.status() != AiOperation.Status.SUCCEEDED) {
               return this.result = r;
            }

            this.switched = true;
            this.current = second.get();
            return AiGameOperations.result(AiOperation.Status.RUNNING, r.message(), new JsonObject());
         }

         @Override
         public boolean canInterruptNow() {
            return !this.needsReconciliation && this.current.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.needsReconciliation || this.current.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.current.cancel();
         }
      };
   }

   static AiOperation.Result init_AiOperation_Result_AiOperation_AiOperation_Result(AiOperation child, AiOperation.Result result) {
      JsonObject evidence = AiRunResources.parseEvidenceJson(result.dataJson());
      boolean pending = child.requiresReconciliationAfterEmergency()
         || AiJson.getBoolean(evidence, "interruptedActionNeedsReconciliation", false)
         || AiJson.getBoolean(evidence, "reobserveBeforeResuming", false);
      AiOperation.Result captured = AiGuardLayer.wrapPendingTermination(pending, result);
      child.cancel();
      return captured;
   }

   static AiOperation.Result init_AiOperation_Result_AiOperation_Status(
      AiOperation.Status status, String message, JsonObject phaseData, boolean pending, AiOperation.Result child
   ) {
      JsonObject data = phaseData.deepCopy();
      if (child != null) {
         if (status == AiOperation.Status.SUCCEEDED && (child.status() == AiOperation.Status.UNCERTAIN || child.status() == AiOperation.Status.CANCELLED)) {
            status = child.status();
         }

         JsonObject evidence = AiRunResources.parseEvidenceJson(child.dataJson());
         data.add("terminalChild", AiRunResources.compactEvidenceJson(child.dataJson()));
         data.addProperty("terminalChildStatus", child.status().name());
         pending |= AiJson.getBoolean(evidence, "interruptedActionNeedsReconciliation", false)
            || AiJson.getBoolean(evidence, "reobserveBeforeResuming", false);
      }

      return AiGuardLayer.wrapPendingTermination(pending, AiGameOperations.result(status, message, data));
   }

   static AiOperation watchedMining(
      final AiOperation mining,
      final BooleanSupplier contextValid,
      final BooleanSupplier targetGone,
      final BooleanSupplier watched,
      final LongSupplier revision,
      final Runnable refused
   ) {
      return new AiOperation() {
         long revisionAtStart = revision.getAsLong();
         int waitedTicks;
         boolean pendingTermination;
         boolean cancelled;
         boolean targetGoneSeen;
         AiOperation.Result result;

         @Override
         public AiOperation.Result tick() {
            if (this.result != null) {
               return this.result;
            }

            if (!this.cancelled && contextValid.getAsBoolean()) {
               boolean gone = targetGone.getAsBoolean();
               if (gone && !this.targetGoneSeen) {
                  refused.run();
                  return this.finish(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "Gold target already disappeared; trying another", "{}"));
               }

               if (gone) {
                  this.pendingTermination = true;
                  mining.cancel();
                  if (revision.getAsLong() > this.revisionAtStart) {
                     this.pendingTermination = false;
                     return this.finish(new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Gold removal acknowledged", "{}", List.of()));
                  }
               } else if (this.pendingTermination && revision.getAsLong() > this.revisionAtStart) {
                  this.pendingTermination = false;
                  refused.run();
                  return this.finish(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "Server restored the gold target; trying another", "{}"));
               }

               if (this.pendingTermination) {
                  return ++this.waitedTicks > 60
                     ? this.finish(new AiOperation.Result(AiOperation.Status.UNCERTAIN, "Gold removal was not acknowledged", "{}", List.of()))
                     : new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for gold removal acknowledgement", "{}", List.of());
               }

               if (watched.getAsBoolean()) {
                  refused.run();
                  return this.finish(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "A piglin sees this mining stance; trying another gold target", "{}"));
               }

               this.revisionAtStart = revision.getAsLong();
               this.targetGoneSeen = true;
               AiOperation.Result result = mining.tick();
               if (targetGone.getAsBoolean()) {
                  this.pendingTermination = revision.getAsLong() <= this.revisionAtStart;
               }

               if (!result.terminal() || result.status() == AiOperation.Status.SUCCEEDED && this.pendingTermination) {
                  if (this.pendingTermination) {
                     mining.cancel();
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for gold removal acknowledgement", "{}", List.of());
                  } else {
                     return result;
                  }
               } else {
                  return this.finish(result);
               }
            } else {
               return this.finish(new AiOperation.Result(AiOperation.Status.CANCELLED, "Gold mining context changed", "{}", List.of()));
            }
         }

         AiOperation.Result finish(AiOperation.Result result) {
            this.pendingTermination = this.requiresReconciliationAfterEmergency();
            this.result = AiGuardLayer.wrapPendingTermination(this.pendingTermination, result);
            mining.cancel();
            return this.result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.requiresReconciliationAfterEmergency() && mining.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.pendingTermination
               || mining.requiresReconciliationAfterEmergency()
               || this.targetGoneSeen && contextValid.getAsBoolean() && targetGone.getAsBoolean() && revision.getAsLong() <= this.revisionAtStart;
         }

         @Override
         public void cancel() {
            this.pendingTermination = this.requiresReconciliationAfterEmergency();
            this.cancelled = true;
            mining.cancel();
         }
      };
   }

   static AiOperation fightThenPickup(final AiOperation fight, final String item, final Supplier<AiOperation> pickup) {
      return new AiOperation() {
         AiOperation current = fight;
         boolean switched;
         AiOperation.Result fightResult;

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result r = this.current.tick();
            if (this.switched) {
               JsonObject data = AiRunResources.parseEvidenceJson(r.dataJson());
               data.add("fight", AiRunResources.compactEvidenceJson(this.fightResult.dataJson()));
               data.addProperty("fightStatus", this.fightResult.status().name());
               data.addProperty("fightMessage", AiSpeedrun.clip(this.fightResult.message()));
               if (AiNetherRun.init_Z_Z_A(this.fightResult.status() == AiOperation.Status.SUCCEEDED, r.failure())) {
                  data.addProperty("pickupYieldedKill", true);
                  return new AiOperation.Result(
                     AiOperation.Status.SUCCEEDED, r.message() + "; the kill's drop is gone from the ground", data.toString(), r.evidence(), null
                  );
               } else {
                  return new AiOperation.Result(r.status(), r.message(), data.toString(), r.evidence(), r.failure());
               }
            } else {
               if (!r.terminal()) {
                  return r;
               }

               this.fightResult = r;
               this.current.cancel();
               if (r.status() != AiOperation.Status.SUCCEEDED && !AiNetherRun.init_Z_Str_D(item, 16.0)) {
                  return r;
               }

               this.switched = true;
               this.current = pickup.get();
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING,
                  r.message(),
                  AiJson.objectOf("fight", AiRunResources.compactEvidenceJson(r.dataJson()), "fightStatus", r.status().name())
               );
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.current.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.current.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.current.cancel();
         }
      };
   }

   static boolean isSpawnerApproachLabel(String label) {
      return label.startsWith("walk to a stand by the blaze spawner")
         || label.startsWith("walk to the blaze spawner")
         || label.startsWith("head for the blaze spawner")
         || label.startsWith("pick up the blaze rods")
         || label.startsWith("wait for blazes");
   }

   static boolean init_Z_Z_A(boolean fightSucceeded, AiFailure failure) {
      return fightSucceeded && failure == AiFailure.NO_REACHABLE_TARGET;
   }

   static boolean init_Z_Str_D(String item, double radius) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         HashSet<String> ids = new HashSet<>(AiGameplay.expandIds(item));
         return !mc.level
            .getEntitiesOfClass(
               ItemEntity.class,
               mc.player.getBoundingBox().inflate(radius),
               e -> e.isAlive() && ids.contains(BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString())
            )
            .isEmpty();
      } else {
         return false;
      }
   }

   static int init_I_I_I(int goal, int held) {
      return (int)Math.max(1L, Math.min(4L, 2L * Math.max(0L, (long)goal - held)));
   }

   static boolean hasRetreated(JsonObject evidence) {
      return AiJson.getBoolean(evidence, "retreated", false)
         || evidence.has("fight") && evidence.get("fight").isJsonObject() && AiJson.getBoolean(evidence.getAsJsonObject("fight"), "retreated", false);
   }

   static AiOperation delayThen(final int ticks, final AiOperation then) {
      return new AiOperation() {
         int remainingTicks = Math.max(0, ticks);
         boolean thenStarted;

         @Override
         public AiOperation.Result tick() {
            if (this.remainingTicks > 0) {
               this.remainingTicks--;
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting", new JsonObject());
            } else {
               this.thenStarted = true;
               return then.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return !this.thenStarted || then.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.thenStarted && then.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            then.cancel();
         }
      };
   }

   static final class PortalScanCache {
      private boolean scanned;
      private BlockPos portal;

      BlockPos portal(Supplier<BlockPos> scan, Predicate<BlockPos> stillLoadedPortal) {
         if (!this.scanned) {
            this.scanned = true;
            this.portal = scan.get();
         }

         return this.portal != null && stillLoadedPortal.test(this.portal) ? this.portal : null;
      }

      boolean isScanned() {
         return this.scanned;
      }
   }

   static final class PearlHuntTracker {
      long startedAt;
      long lastSeenAt;
      long lastGainAt;
      int held;
      boolean started;
      boolean groundPhase;

      boolean pearlHuntWorthwhile(long now, boolean routeComplete, int held, int wanted, boolean pearlGround, boolean givenUp, boolean returningForSupplies) {
         if (returningForSupplies) {
            return false;
         }

         if (!routeComplete) {
            return true;
         }

         if (held < wanted && !givenUp) {
            if (!this.started) {
               this.started = true;
               this.startedAt = this.lastSeenAt = now;
               this.held = held;
            }

            if (held == 0 && !this.groundPhase) {
               if (pearlGround) {
                  this.groundPhase = true;
               } else if (now - this.startedAt < 1080000000000L) {
                  this.lastSeenAt = now;
               }
            }

            if (held > this.held) {
               this.held = held;
               this.lastGainAt = now;
            }

            long hunted = now - this.lastSeenAt;
            boolean paying = this.lastGainAt != 0L && now - this.lastGainAt < 90000000000L && hunted < 1080000000000L;
            return hunted < 300000000000L || paying;
         } else {
            return false;
         }
      }
   }

   record RouteReceipt(int barters, BlockPos spawner, long observedAtMs) {
      JsonObject toJson() {
         return AiJson.objectOf(
            "source",
            "OBSERVED_NETHER_ROUTE",
            "barters",
            this.barters,
            "spawnerX",
            this.spawner.getX(),
            "spawnerY",
            this.spawner.getY(),
            "spawnerZ",
            this.spawner.getZ(),
            "observedAtMs",
            this.observedAtMs
         );
      }
   }

   record PearlBudget(int pearls, int gold) {
   }

   record RadiusGoal(int x, int z, int radius) implements autismclient.ai.engine.AiPathfinder.PathGoal {
      @Override
      public boolean isInGoal(int px, int py, int pz) {
         return (long)(px - this.x) * (px - this.x) + (long)(pz - this.z) * (pz - this.z) <= (long)this.radius * this.radius;
      }

      @Override
      public double heuristic(int px, int py, int pz) {
         return 3.2 * Math.max(0.0, Math.hypot(px - this.x, pz - this.z) - this.radius);
      }

      @Override
      public boolean outsideSearchBox(BlockPos origin, int horizontal, int vertical) {
         return Math.abs(this.x - origin.getX()) > horizontal + this.radius || Math.abs(this.z - origin.getZ()) > horizontal + this.radius;
      }
   }

   static final class MiningMemory {
      private final LinkedHashMap<Long, Long> deferredCells = new LinkedHashMap<>();
      final autismclient.ai.engine.AiMiningCommitment init_FieldCache = new autismclient.ai.engine.AiMiningCommitment();
      final autismclient.ai.engine.AiMiningCommitment blockCommitment = new autismclient.ai.engine.AiMiningCommitment();

      autismclient.ai.engine.AiMiningCommitment commitmentFor(boolean block) {
         return block ? this.blockCommitment : this.init_FieldCache;
      }

      void deferCell(long cell, long now) {
         this.deferredCells.remove(cell);
         this.deferredCells.put(cell, now + 30000000000L);

         while (this.deferredCells.size() > 64) {
            this.deferredCells.remove(this.deferredCells.keySet().iterator().next());
         }
      }

      boolean isCellAvailable(long cell, long now) {
         Long until = this.deferredCells.get(cell);
         if (until == null) {
            return true;
         }

         if (now < until) {
            return false;
         }

         this.deferredCells.remove(cell);
         return true;
      }
   }

   enum RunPhase {
      FORTRESS,
      RODS,
      PEARLS,
      RETURN;
   }
}
