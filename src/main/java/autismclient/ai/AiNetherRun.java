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
   static final Predicate<BlockState> init_Predicate = s -> s.is(Blocks.NETHER_BRICKS) || s.is(Blocks.NETHER_BRICK_FENCE) || s.is(Blocks.NETHER_BRICK_STAIRS);
   static final Predicate<BlockState> load_Predicate = s -> s.is(Blocks.WARPED_NYLIUM) || s.is(Blocks.WARPED_STEM);
   static final Predicate<BlockState> read_Predicate = s -> s.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
      || s.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS)
      || s.is(Blocks.CHISELED_POLISHED_BLACKSTONE)
      || s.is(Blocks.GILDED_BLACKSTONE);
   static final List<BlockPos> load_List = new CopyOnWriteArrayList<>();
   static final long sync_J = 30000000000L;
   static volatile BlockPos init_BlockPos;
   static volatile String init_Str = "";
   static volatile int push;
   static volatile BlockPos load_BlockPos;
   static volatile Object read_Obj;
   private static boolean load_Z;
   private static long flush_J;
   private static boolean read_Z;

   private AiNetherRun() {
   }

   static BlockPos init(BlockPos loadedPortal, BlockPos historicalPortal, BlockPos rememberedPortal, Predicate<BlockPos> plausible) {
      for (BlockPos candidate : new BlockPos[]{loadedPortal, historicalPortal, rememberedPortal}) {
         if (candidate != null && plausible.test(candidate)) {
            return candidate.immutable();
         }
      }

      return null;
   }

   static int init_I_J_J(long deadline, long now) {
      long seconds = (deadline - now) / 1000000000L;
      return seconds < 10L ? 0 : (int)Math.min(120L, seconds);
   }

   static void init_V_BlockPos_BlockPos(BlockPos origin, BlockPos target) {
      if (origin != null) {
         init_List.add(origin.immutable());
      }

      if (target != null) {
         init_List.add(target.immutable());
      }
   }

   static boolean init(BlockPos target) {
      return target != null && init_List.stream().anyMatch(failed -> failed.distSqr(target) < 1024.0);
   }

   static int init_I_I(int requested, boolean campaign, boolean pearlRecovery, boolean routeProven, int powderHeld) {
      return pearlRecovery && routeProven ? Math.max(0, powderHeld / 2) : Math.max(campaign ? 8 : 1, Math.min(16, requested));
   }

   static boolean init_Z_I_2(int rodsHeld, int rodGoal, boolean campaign, boolean spawnerConfirmed, int barters) {
      return rodsHeld >= rodGoal && (!campaign || spawnerConfirmed);
   }

   static boolean init_Z_I(int rodsHeld, int rodGoal, boolean committedVein, boolean adultPartner, double distanceSquared) {
      return rodsHeld >= rodGoal || committedVein || adultPartner && distanceSquared <= 64.0;
   }

   static boolean init(AiOperation.Result r) {
      return r.status() != AiOperation.Status.SUCCEEDED
         && r.message() != null
         && (r.message().contains("No route") || r.message().contains("Route search budget exhausted"));
   }

   static AiNetherRun.Inner_DataUtil init(int wanted, int eyes, int rawPearls, int confirmedBarters) {
      boolean proofOnly = confirmedBarters == 0 && (long)eyes + rawPearls >= wanted;
      return new AiNetherRun.Inner_DataUtil(proofOnly ? 0 : Math.max(0, wanted - eyes), proofOnly ? 1 : 32);
   }

   static AiOperation init_AiOperation(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (mc.level.dimension() != Level.NETHER) {
            throw new IllegalArgumentException("The Nether run starts in the Nether");
         }

         read();
         boolean routeProven = load_Z();
         final boolean pearlRecovery = AiJson.getBoolean(args, "pearlRecovery", false) && routeProven;
         final int rods = init_I_I(AiJson.getInt(args, "rods", 8), AiRunResources.isCampaignActive(), pearlRecovery, routeProven, AiRunResources.blazePowderEquivalents());
         final int pearls = Math.max(0, Math.min(32, AiJson.getInt(args, "pearls", 16)));
         final int timeoutSeconds = Math.max(120, Math.min(5400, AiJson.getInt(args, "timeoutSeconds", 2400)));
         BlockPos portal = init(mc, mc.player.blockPosition(), 8);
         final String currentWorld = AiWorldIdentity.currentWorldKey(mc);
         if (portal != null) {
            init_BlockPos = portal;
         }

         init_Z = false;
         final BlockPos remembered = init(portal, AiPortalLinks.init(2147483647), init_BlockPos, at -> init_Z_Minecraft_BlockPos(mc, at));
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final String init_Str = currentWorld;
            final Object init_Obj = mc.getConnection();
            final UUID init_UUID = mc.player.getUUID();
            final long load_J = AiServerEvidence.deathRevision();
            final JsonArray init_JsonArray = new JsonArray();
            BlockPos init_BlockPos = remembered;
            final AiNetherRun.Inner_BufferHelper init_aB_BufferHelper = new AiNetherRun.Inner_BufferHelper();
            final Set<Long> init_Set = new HashSet<>();
            final AiNetherRun.Inner_StreamUtil init_aB_StreamUtil = new AiNetherRun.Inner_StreamUtil();
            AiOperation init_AiOperation;
            String load_Str = "";
            String read_Str = "";
            int init_I = -1;
            AiNetherRun.Inner_TaskQueue init_aB_TaskQueue;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            long read_J;
            BlockPos load_BlockPos;
            boolean init_Z;
            int sync_I;
            int flush_I;
            int close_I;
            int open_I;
            int poll_I;
            int push_I;
            int pop_I;
            BlockPos read_BlockPos;
            AiOperation.Result init_AiOperation_Result;
            int tick_I = AiNetherRun.push;
            int run_I;
            BlockPos write_BlockPos;
            boolean load_Z;
            String write_Str;
            long write_J;
            final AiGuardLayer init_Y;
            long update_J;
            int get_I;
            long sync_J;
            final AiNetherRun.Inner_CachePool init_aB_CachePool;
            long flush_J;
            long close_J;
            long open_J;
            long poll_J;
            long push_J;
            boolean read_Z;
            final Set<Long> load_Set;
            final Map<String, Integer> init_Map;
            BlockPos update_BlockPos;
            BlockPos sync_BlockPos;
            long pop_J;
            int set_I;
            int put_I;
            int add;
            int clear;
            boolean write_Z;
            boolean update_Z;
            long tick_J;
            long run_J;
            long get_J;
            int reset;
            int start;
            int stop;
            BlockPos flush_BlockPos;
            final Set<Long> read_Set;
            int next;
            final Map<Integer, Long> load_Map;
            int prev;
            int valid;
            final int size;
            long set_J;
            long put_J;
            JsonObject init_JsonObject;
            boolean sync_Z;

            {
               this.write_BlockPos = AiNetherRun.load_BlockPos;
               this.write_Str = "";
               this.write_J = -9223372036854775808L;
               this.init_Y = new AiGuardLayer(
                  AiGuard.GuardPolicy.standard()
                     .withoutFlee()
                     .keeping(List.of("minecraft:blaze_rod", "minecraft:blaze_powder", "minecraft:ender_pearl", "minecraft:ender_eye", "minecraft:gold_ingot")),
                  "nether"
               );
               this.init_aB_CachePool = new AiNetherRun.Inner_CachePool();
               this.load_Set = new HashSet<>();
               this.init_Map = new HashMap<>();
               this.read_Set = new HashSet<>();
               this.load_Map = new HashMap<>();
               this.prev = -1;
               this.size = rods;
               this.init_JsonObject = new JsonObject();
            }

            boolean init_Z_D(double range) {
               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:piglin", range)) {
                  if (e instanceof Piglin p && p.getOffhandItem().isEmpty() && AiNetherStructures.init_Z(mc, p)) {
                     return true;
                  }
               }

               return false;
            }

            boolean init_Z_BlockPos_5(BlockPos at) {
               return AiNetherStructures.init_Z(at);
            }

            boolean init(BlockPos at, BlockPos to, BlockPos bastion) {
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

            boolean init_Z_16() {
               for (Entity e : mc.level.entitiesForRendering()) {
                  if (e instanceof AbstractPiglin p && p.isAlive() && p.distanceToSqr(mc.player) < 256.0 && p.hasLineOfSight(mc.player)) {
                     return true;
                  }
               }

               return false;
            }

            BlockPos init(Predicate<BlockState> kind, boolean block) {
               if (!this.init_Z_16() && (!block || AiWorldScan.findEntitiesByType("minecraft:piglin_brute", 40.0).isEmpty())) {
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

                  autismclient.ai.engine.AiMiningCommitment vein = this.init_aB_StreamUtil.init(block);
                  Predicate<BlockPos> allowed = atxx -> this.init_aB_StreamUtil.init_Z(atxx.asLong(), now);
                  vein.init(observed, allowed);
                  Set<Long> present = new HashSet<>();

                  for (BlockPos at : observed) {
                     if (allowed.test(at)) {
                        present.add(at.asLong());
                     }
                  }

                  vein.init(present, atxx -> !allowed.test(atxx) || AiTerrain.isLoaded(mc.level, atxx) && !kind.test(mc.level.getBlockState(atxx)));
                  if (vein.isActive() && vein.init(now, true, 250000000L)) {
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

                     vein.init(seed, observed, allowed);
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

            AiOperation init_AiOperation(BlockPos at, int seconds) {
               this.read_BlockPos = at;
               Object level = mc.level;
               return AiNetherRun.init(
                  AiGameOperations.blockAction("break", AiJson.objectOf("x", at.getX(), "y", at.getY(), "z", at.getZ(), "timeoutSeconds", seconds)),
                  () -> mc.level == level && mc.player != null && mc.getConnection() == this.init_Obj,
                  () -> mc.level != null
                     && AiTerrain.isLoaded(mc.level, at)
                     && (mc.level.getBlockState(at).isAir() || !mc.level.getBlockState(at).getFluidState().isEmpty()),
                  this::init_Z_16,
                  () -> AiServerEvidence.blockRevision(at),
                  () -> this.init_aB_StreamUtil.init_V(at.asLong(), System.nanoTime())
               );
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.init_Str.equals(AiWorldIdentity.currentWorldKey(mc)) || this.init_Obj != mc.getConnection()) {
                     return this.init(AiOperation.Status.CANCELLED, "World or connection changed during Nether expedition");
                  }

                  if (!mc.player.getUUID().equals(this.init_UUID)) {
                     return this.init(AiOperation.Status.UNCERTAIN, "Player changed during Nether expedition");
                  }

                  if (mc.player.isAlive() && this.load_J == AiServerEvidence.deathRevision()) {
                     AiGoal.init(
                        this,
                        1,
                        "Nether",
                        AiGoal.init(this.load_Str, this.init_aB_TaskQueue == null ? "" : this.init_aB_TaskQueue.name()),
                        this.init_I_18() + " of " + this.size + " blaze rods, " + this.load_I_11() + " of " + pearls + " pearls"
                     );
                     if (++this.get_I % 200 == 0 && mc.level.dimension() == Level.NETHER) {
                        BlockPos seen = AiWorldScan.findNearestBlock(AiNetherRun.load_Predicate, 12);
                        if (seen != null && AiNetherRun.load_List.stream().noneMatch(w -> w.distSqr(seen) < 2304.0)) {
                           AiNetherRun.load_List.add(seen.immutable());
                        }
                     }

                     autismclient.ai.engine.AiRestock.reserveItemsForJob(Map.of(Items.OBSIDIAN, 64));
                     if (System.nanoTime() > this.init_J) {
                        return this.init(
                           this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                           "Out of time in the Nether at " + this.init_aB_TaskQueue
                        );
                     }

                     if (mc.level.dimension() != Level.NETHER) {
                        if (mc.level.dimension() != Level.OVERWORLD) {
                           return this.init(AiOperation.Status.UNCERTAIN, "Unexpected destination after Nether expedition");
                        }

                        if (this.init_aB_TaskQueue == AiNetherRun.Inner_TaskQueue.RETURN
                           && this.init_AiOperation != null
                           && (this.load_Str.equals("through the portal") || this.load_Str.startsWith("build a portal home"))) {
                           AiOperation.Result arrival = this.init_AiOperation.tick();
                           if (!arrival.terminal()) {
                              return this.load("Verifying Overworld arrival: " + arrival.message());
                           }

                           arrival = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, arrival);
                           this.init_AiOperation = null;
                           if (arrival.status() != AiOperation.Status.SUCCEEDED) {
                              return this.init(arrival.status(), arrival.message(), arrival);
                           }

                           boolean route = !AiRunResources.isCampaignActive() || this.tick_I > 0 && this.write_BlockPos != null;
                           return this.init(
                              (this.init_I_18() < this.size || !route) && !AiNetherRun.init_Z ? AiOperation.Status.FAILED : AiOperation.Status.SUCCEEDED,
                              (
                                    AiNetherRun.init_Z
                                       ? "Home for supplies (" + (this.write_Str.isEmpty() ? "food or equipment" : this.write_Str) + ") with "
                                       : "Verified Overworld return with "
                                 )
                                 + this.init_I_18()
                                 + " rod equivalents and "
                                 + this.load_I_11()
                                 + " pearl equivalents",
                              arrival
                           );
                        } else {
                           return this.init(AiOperation.Status.UNCERTAIN, "Nether dimension changed outside the expected return action");
                        }
                     } else {
                        if (!AiThreats.isLegacyCombatEnabled()) {
                           autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(5000000000L);
                        }

                        if (this.init_aB_TaskQueue == AiNetherRun.Inner_TaskQueue.RODS
                           && this.init_AiOperation != null
                           && AiNetherRun.init(this.load_Str)
                           && !this.init_Y.isActive()
                           && this.init_AiOperation.canInterruptNow()
                           && System.nanoTime() > this.open_J
                           && this.init_I_18() < this.size
                           && mc.player.getHealth() >= 12.0F
                           && mc.player.onGround()
                           && !AiEndRun.init_Z(mc)
                           && !mc.player.isInLava()
                           && mc.level.dimension() == Level.NETHER) {
                           Entity inSight = this.read(16);
                           if (inSight != null) {
                              AiSpeedrun.init_V_Str_ObjArr(
                                 "[AI] nether: a blaze in sight at {} during '{}'; fighting it", inSight.blockPosition().toShortString(), this.load_Str
                              );
                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                              this.flush_J = System.nanoTime();
                              return this.init(
                                 "fight a blaze",
                                 AiNetherRun.init(
                                    AiBlazeFight.init(
                                       AiJson.objectOf(
                                          "count", AiNetherRun.init_I_I_I(this.size, this.init_I_18()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 120
                                       )
                                    ),
                                    "minecraft:blaze_rod",
                                    () -> AiGameplay.write(
                                       AiJson.objectOf(
                                          "item",
                                          "minecraft:blaze_rod",
                                          "count",
                                          Math.min(this.size, AiCraft.countItem("minecraft:blaze_rod") + 1),
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

                        if (this.init_Y.isActive()
                           || !this.load_Str.startsWith("fight a blaze")
                           || mc.player.getHealth() < 10.0F
                           || AiEndRun.init_Z(mc)
                           || mc.player.isInLava()) {
                           AiOperation.Result g = this.init_Y.tick(this.init_AiOperation, () -> {
                              this.init_V_18();
                              if (this.init_AiOperation != null && this.load_Str.startsWith("barter") && this.init_AiOperation_Result != null) {
                                 this.load(this.init_AiOperation_Result);
                              }

                              this.init_AiOperation_Result = null;
                              if (this.init_AiOperation != null && this.load_Str.startsWith("barter") && this.close_I < 4) {
                                 this.close_I++;
                                 this.flush_I--;
                              }

                              if (this.init_AiOperation != null && this.write_J != -9223372036854775808L && this.poll_I < 6) {
                                 this.poll_I++;
                                 this.init_Set.remove(this.write_J);
                              }

                              if (this.init_AiOperation != null && this.load_Str.startsWith("build a portal home")) {
                                 this.load_Z = false;
                              }

                              this.init_AiOperation.cancel();
                              this.init_AiOperation = null;
                           }, () -> (int)Math.max(10L, (this.init_J - System.nanoTime()) / 1000000000L));
                           if (g != null) {
                              return g.terminal() ? this.init(g.status(), g.message(), g) : g;
                           }
                        }

                        if (this.init_AiOperation != null) {
                           AiOperation.Result r = this.init_AiOperation.tick();
                           this.init_AiOperation_Result = r;
                           if (!r.terminal()) {
                              return this.load(this.load_Str + ": " + r.message());
                           }

                           r = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, r);
                           this.init_AiOperation = null;
                           AiSpeedrun.init_V_Str_ObjArr("[AI] nether step {}: {} - {}", r.status(), this.load_Str, AiSpeedrun.init_Str_Str(r.message()));
                           this.init_JsonArray
                              .add(AiJson.objectOf("step", this.load_Str, "status", r.status().name(), "message", AiSpeedrun.init_Str_Str(r.message())));
                           if (r.status() == AiOperation.Status.CANCELLED) {
                              return this.init(AiOperation.Status.CANCELLED, "Cancelled at " + this.load_Str, r);
                           }

                           this.init_V_AiOperation_Result_2(r);
                           this.init_AiOperation_Result = null;
                           if (r.status() == AiOperation.Status.UNCERTAIN) {
                              return this.init(AiOperation.Status.UNCERTAIN, this.load_Str + ": " + r.message(), r);
                           }
                        }

                        return !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable() && !mc.player.isInLava()
                           ? this.load("Landing")
                           : this.load_AiOperation_Result_12();
                     }
                  } else {
                     return this.init(AiOperation.Status.FAILED, "Player died during Nether expedition");
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result init_AiOperation_Result_16() {
               if (!this.read_Str.isEmpty()) {
                  String label = this.read_Str;
                  this.read_Str = "";
                  BlockPos ore = this.open_I < 48 ? this.init(s -> s.is(Blocks.NETHER_GOLD_ORE), false) : null;
                  if (ore != null) {
                     this.open_I++;
                     this.pop_I = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                     AiRunTrace.emitServiceEvent("nether_leg_resume", AiJson.objectOf("kind", "gold", "target", ore.toShortString()));
                     return this.init(
                        label,
                        AiNetherRun.read(
                           this.init_AiOperation(ore, 30),
                           () -> AiGameplay.write(
                              AiJson.objectOf("item", "minecraft:gold_nugget", "count", 1, "additional", true, "radius", 10, "timeoutSeconds", 15)
                           )
                        )
                     );
                  }
               }

               if (this.init_I >= 0) {
                  Entity entity = mc.level.getEntity(this.init_I);
                  if (entity instanceof Blaze && entity.isAlive()) {
                     if (mc.player.getHealth() < 10.0F) {
                        return null;
                     }

                     int id = this.init_I;
                     this.init_I = -1;
                     this.flush_J = System.nanoTime();
                     AiRunTrace.emitServiceEvent("nether_leg_resume", AiJson.objectOf("kind", "blaze", "entityId", id));
                     return this.init(
                        "fight a blaze",
                        AiNetherRun.init(
                           AiBlazeFight.init(
                              AiJson.objectOf(
                                 "entityId", id, "count", AiNetherRun.init_I_I_I(this.size, this.init_I_18()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 60
                              )
                           ),
                           "minecraft:blaze_rod",
                           () -> AiGameplay.write(
                              AiJson.objectOf(
                                 "item",
                                 "minecraft:blaze_rod",
                                 "count",
                                 Math.min(this.size, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                 "radius",
                                 16,
                                 "timeoutSeconds",
                                 15
                              )
                           )
                        )
                     );
                  } else {
                     this.init_I = -1;
                     return null;
                  }
               } else {
                  return null;
               }
            }

            void init_V_AiOperation_Result_2(AiOperation.Result r) {
               if (this.load_Str.startsWith("barter")) {
                  this.load(r);
               }

               if (this.load_Str.startsWith("convert") && r.status() != AiOperation.Status.SUCCEEDED) {
                  this.poll_J = System.nanoTime() + 60000000000L;
               }

               if (this.load_Str.startsWith("barter")
                  && r.status() != AiOperation.Status.SUCCEEDED
                  && r.message() != null
                  && r.message().startsWith("No safe unprovoked adult piglin")) {
                  this.flush_I = Math.max(0, this.flush_I - 1);
                  this.push_J = System.nanoTime() + 10000000000L;
               }

               if (this.read_BlockPos != null
                  && (AiNetherRun.init(r) || r.status() != AiOperation.Status.SUCCEEDED && r.message() != null && r.message().contains("drop would fall into lava"))) {
                  this.init_aB_StreamUtil.init_V(this.read_BlockPos.asLong(), System.nanoTime());
               }

               this.read_BlockPos = null;
               if (AiNetherRun.init(r)) {
                  this.put_I++;
               } else if (r.status() == AiOperation.Status.SUCCEEDED && this.load_Str.startsWith("walk")) {
                  this.put_I = 0;
               }

               if (this.load_Str.startsWith("explore")
                  && r.status() != AiOperation.Status.SUCCEEDED
                  && r.message() != null
                  && r.message().startsWith("Health too low")) {
                  this.load_I--;
               } else if (this.load_Str.startsWith("explore") && r.status() != AiOperation.Status.SUCCEEDED && this.update_BlockPos != null) {
                  AiNetherRun.init_V_BlockPos_BlockPos(this.sync_BlockPos, this.update_BlockPos);
                  AiWorldMemory.load(this.update_BlockPos);
               }

               if (this.load_Str.startsWith("fight a blaze")) {
                  this.update_I = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.update_I + 1;
                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     this.open_J = System.nanoTime() + 10000000000L;
                  }

                  if (AiNetherRun.init_Z(AiRunResources.parseEvidenceJson(r.dataJson()))) {
                     this.read_Z = true;
                  }
               }

               if (this.load_Str.startsWith("mine safe barter gold")) {
                  int now = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                  this.push_I = now > this.pop_I ? 0 : this.push_I + 1;
               }

               if (this.load_Str.startsWith("head for")) {
                  this.add = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.add + 1;
               }

               if (this.load_BlockPos == null || !this.load_BlockPos.equals(this.flush_BlockPos)) {
                  this.flush_BlockPos = this.load_BlockPos;
                  this.stop = 0;
               }

               if ((this.load_Str.startsWith("walk to a stand by the blaze spawner") || this.load_Str.startsWith("walk to the blaze spawner"))
                  && r.status() != AiOperation.Status.SUCCEEDED) {
                  this.stop++;
               }

               if (this.load_Str.startsWith("walk back")) {
                  this.clear = AiNetherRun.init(r) ? this.clear + 1 : 0;
               }
            }

            void load(AiOperation.Result r) {
               JsonObject evidence = AiRunResources.parseEvidenceJson(r.dataJson());
               this.tick_I = this.tick_I + AiJson.getInt(evidence, "bartered", 0);
               boolean changed = this.tick_I > AiNetherRun.push;
               AiNetherRun.push = this.tick_I;
               if (changed) {
                  AiNetherRun.write();
               }

               this.run_I = this.run_I + AiJson.getInt(evidence, "goldSpent", 0);
            }

            AiOperation.Result load_AiOperation_Result_12() {
               this.tick_I = Math.max(this.tick_I, AiNetherRun.push);
               if (this.write_BlockPos == null) {
                  this.write_BlockPos = AiNetherRun.load_BlockPos;
               }

               if (this.read_Z) {
                  if (mc.player.getHealth() >= 18.0F && !AiEndRun.init_Z(mc) && !mc.player.isInLava()) {
                     this.read_Z = false;
                  } else {
                     if (AiSurvival.init(mc.player.getInventory()) >= 0 || mc.player.getFoodData().getFoodLevel() >= 18) {
                        return this.init("recover after blaze retreat", AiCover.init(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90)));
                     }

                     AiNetherRun.init_Z = true;
                     this.write_Str = "no food for recovery after blaze retreat";
                  }
               }

               if (AiRunResources.isCampaignActive() && AiRunResources.missingMandatoryGear().keySet().stream().anyMatch(id -> !id.equals("minecraft:golden_boots"))) {
                  AiNetherRun.init_Z = true;
                  this.write_Str = "required equipment repair: " + AiRunResources.missingMandatoryGear().keySet();
                  return this.read_AiOperation_Result_8();
               }

               if (AiRunResources.isCampaignActive() && AiRunResources.countUsableItem("minecraft:golden_boots", 16) == 0) {
                  if (AiCraft.countItem("minecraft:gold_ingot") >= 4) {
                     return this.init(
                        "replace worn golden boots",
                        AiCraft.craft(
                           AiJson.objectOf("item", "minecraft:golden_boots", "count", AiCraft.countItem("minecraft:golden_boots") + 1, "timeoutSeconds", 30)
                        )
                     );
                  }

                  AiNetherRun.init_Z = true;
                  this.write_Str = "golden boots below their durability reserve";
                  return this.read_AiOperation_Result_8();
               } else {
                  if (AiRunResources.isCampaignActive() && !AiRunResources.isMandatoryArmorEquipped()) {
                     return this.init("restore required Nether armor", AiEquip.init(AiJson.objectOf("what", "armor")));
                  }

                  if (!AiNetherRun.init_Z
                     && AiSurvival.init(mc.player.getInventory()) < 0
                     && (mc.player.getHealth() < 18.0F && mc.player.getFoodData().getFoodLevel() < 18 || mc.player.getFoodData().getFoodLevel() <= 6)) {
                     AiNetherRun.init_Z = true;
                     this.write_Str = "no carried food for continued travel or healing";
                     AiSpeedrun.init_V_Str_ObjArr(
                        "[AI] nether: out of food at {} health, food {}; home for food with {} rods",
                        String.format("%.1f", mc.player.getHealth()),
                        mc.player.getFoodData().getFoodLevel(),
                        this.init_I_18()
                     );
                  }

                  if (AiNetherRun.init_Z) {
                     return this.read_AiOperation_Result_8();
                  }

                  AiOperation.Result resumed = this.init_AiOperation_Result_16();
                  if (resumed != null) {
                     return resumed;
                  }

                  boolean pearlPhaseReady = AiNetherRun.init_Z_I_2(this.init_I_18(), this.size, AiRunResources.isCampaignActive(), this.write_BlockPos != null, this.tick_I);
                  int pearlsNow = this.load_I_11();
                  boolean pearlGround = false;
                  if (pearlPhaseReady && pearlsNow == 0 && !this.init_aB_CachePool.load_Z) {
                     BlockPos forest = AiWorldScan.findNearestBlock(AiNetherRun.load_Predicate, 3);
                     pearlGround = AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).size() >= 2
                        || forest != null && forest.distSqr(mc.player.blockPosition()) <= 1600.0;
                  }

                  boolean wasStarted = this.init_aB_CachePool.init_Z;
                  boolean wasGround = this.init_aB_CachePool.load_Z;
                  boolean optionalPearls = this.init_aB_CachePool
                     .init(System.nanoTime(), pearlPhaseReady, pearlsNow, pearls, pearlGround, this.write_Z, AiNetherRun.init_Z);
                  if (!wasStarted && this.init_aB_CachePool.init_Z) {
                     this.load_I = 0;
                  }

                  if (!wasGround && this.init_aB_CachePool.load_Z) {
                     AiSpeedrun.init_V_Str_ObjArr("[AI] nether: pearl ground reached at {}; the pearl clock starts", mc.player.blockPosition().toShortString());
                  }

                  if (optionalPearls
                     && (this.load_I_11() < pearls || this.tick_I == 0)
                     && this.init_Z_D(32.0)
                     && this.flush_I < 20
                     && System.nanoTime() > this.push_J) {
                     if (AiCraft.countItem("minecraft:gold_ingot") > 0) {
                        this.flush_I++;
                        AiNetherRun.Inner_DataUtil goal = AiNetherRun.init(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.tick_I);
                        return this.init(
                           "barter an economical gold batch",
                           AiBarter.init(AiJson.objectOf("pearls", goal.pearls(), "gold", goal.gold(), "partners", 3, "timeoutSeconds", 180))
                        );
                     }

                     if (AiCraft.countItem("minecraft:gold_nugget") >= 9 && System.nanoTime() > this.poll_J) {
                        return this.init(
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
                  boolean earlyGoldPartner = this.init_I_18() < this.size && !committedGoldVein && this.init_Z_D(32.0);
                  if (this.load_I_11() < pearls
                     && !this.write_Z
                     && !AiNetherRun.init_Z
                     && !this.init_aB_CachePool.load_Z
                     && this.open_I < 48
                     && this.push_I < 3
                     && (this.init_I_18() >= this.size || committedGoldVein || earlyGoldPartner)) {
                     BlockPos ore = this.init(s -> s.is(Blocks.NETHER_GOLD_ORE), false);
                     if (ore != null && AiNetherRun.init_Z_I(this.init_I_18(), this.size, committedGoldVein, earlyGoldPartner, ore.distSqr(mc.player.blockPosition()))) {
                        this.open_I++;
                        this.pop_I = AiCraft.countItem("minecraft:gold_nugget") + 9 * AiCraft.countItem("minecraft:gold_ingot");
                        return this.init(
                           "mine safe barter gold",
                           AiNetherRun.read(
                              this.init_AiOperation(ore, 30),
                              () -> AiGameplay.write(
                                 AiJson.objectOf("item", "minecraft:gold_nugget", "count", 1, "additional", true, "radius", 10, "timeoutSeconds", 15)
                              )
                           )
                        );
                     }
                  }

                  if (System.nanoTime() > this.close_J) {
                     for (String loot : List.of("minecraft:ender_pearl", "minecraft:blaze_rod")) {
                        boolean short_ = loot.endsWith("pearl") ? this.load_I_11() < pearls : this.init_I_18() < this.size;
                        if (short_ && AiNetherRun.init_Z_Str_D(loot, 12.0) && this.init_Map.merge(loot, 1, Integer::sum) <= 6) {
                           this.close_J = System.nanoTime() + 10000000000L;
                           return this.init(
                              "pick up the " + loot.substring(10).replace('_', ' ') + "s lying near",
                              AiGameplay.write(AiJson.objectOf("item", loot, "count", 64, "radius", 12, "timeoutSeconds", 15))
                           );
                        }
                     }
                  }

                  if (mc.player.getHealth() < 14.0F && (System.nanoTime() > this.update_J || mc.player.getHealth() < 10.0F)) {
                     this.update_J = System.nanoTime() + 30000000000L;
                     if ((mc.player.getHealth() < 10.0F || this.read_Z_7())
                        && (!AiWorldScan.findEntitiesByType("minecraft:blaze", 24.0).isEmpty() || !AiWorldScan.findEntitiesByType("minecraft:wither_skeleton", 16.0).isEmpty())) {
                        return this.init("hide and heal", AiCover.init(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90)));
                     }

                     if (mc.player.getFoodData().getFoodLevel() < 20 && AiSurvival.init(mc.player.getInventory()) >= 0) {
                        return this.init("eat to heal", AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20)));
                     }
                  }

                  if (mc.player.getHealth() < 10.0F && mc.player.getFoodData().getFoodLevel() >= 18) {
                     return this.init("rest to heal at " + (int)mc.player.getHealth() + " health", AiCombat.init(100));
                  }

                  if (!this.init_Z && !mc.player.getOffhandItem().is(Items.SHIELD) && AiCraft.countItem("minecraft:shield") > 0) {
                     this.init_Z = true;
                     return this.init("shield to the offhand", AiEquip.init(AiJson.objectOf("what", "minecraft:shield", "hand", "offhand")));
                  }

                  float provokeHealth = AiForage.init(mc, mc.player.blockPosition()) ? 12.0F : 16.0F;
                  Entity provokable = optionalPearls
                        && this.load_I_11() < pearls
                        && mc.player.getHealth() >= provokeHealth
                        && System.nanoTime() > this.tick_J
                        && AiSurvival.init(mc.player.getInventory()) >= 0
                        && mc.player.onGround()
                        && AiForage.write(mc, mc.player.blockPosition())
                     ? this.init_Entity_I(48)
                     : null;
                  if (provokable != null) {
                     this.tick_J = System.nanoTime() + 5000000000L;
                     return this.init(
                        "fight an enderman",
                        AiNetherRun.init(
                           this.init_AiOperation_I(provokable.getId()),
                           "minecraft:ender_pearl",
                           () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:ender_pearl", "count", 64, "radius", 16, "timeoutSeconds", 15))
                        )
                     );
                  }

                  if (optionalPearls
                     && this.load_I_11() < pearls
                     && mc.player.getHealth() >= 16.0F
                     && mc.player.onGround()
                     && System.nanoTime() > this.get_J
                     && !AiForage.write(mc, mc.player.blockPosition())
                     && AiForage.load(mc, mc.player.blockPosition())
                     && !AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).isEmpty()) {
                     this.get_J = System.nanoTime() + 10000000000L;
                     BlockPos under = mc.player.blockPosition().below();
                     return this.init(
                        "dig a fighting hole at " + under.toShortString(),
                        AiGameOperations.blockAction(
                           "break",
                           AiJson.objectOf("x", under.getX(), "y", under.getY(), "z", under.getZ(), "requireDrops", false, "timeoutSeconds", 8)
                        )
                     );
                  }

                  if (optionalPearls
                     && this.load_I_11() < pearls
                     && mc.player.getHealth() >= 16.0F
                     && mc.player.onGround()
                     && System.nanoTime() > this.run_J
                     && !AiForage.write(mc, mc.player.blockPosition())
                     && AiWorldScan.findEntitiesByType("minecraft:enderman", 24.0).size() >= 2) {
                     this.run_J = System.nanoTime() + 15000000000L;
                     BlockPos footing = this.init_BlockPos_I(12);
                     if (footing != null) {
                        return this.init(
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

                  Entity huntable = optionalPearls && this.load_I_11() < pearls && mc.player.getHealth() >= 16.0F ? this.init(24, true) : null;
                  if (huntable != null) {
                     this.load_Map.put(huntable.getId(), System.nanoTime() + 20000000000L);
                  }

                  if (huntable != null) {
                     return this.init(
                        "fight an enderman",
                        AiNetherRun.init(
                           this.init(
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
                     && this.init_I_18() < this.size
                     && (this.load_I_11() < pearls || this.tick_I == 0)
                     && AiCraft.countItem("minecraft:gold_ingot") >= 8
                     && this.flush_I < 1
                     && this.init_Z_D(32.0)) {
                     this.flush_I++;
                     AiNetherRun.Inner_DataUtil goal = AiNetherRun.init(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.tick_I);
                     return this.init(
                        "barter with the piglins before the blazes",
                        AiBarter.init(
                           AiJson.objectOf(
                              "pearls",
                              goal.pearls(),
                              "gold",
                              goal.gold(),
                              "partners",
                              3,
                              "timeoutSeconds",
                              (int)Math.max(60L, Math.min(600L, (this.init_J - System.nanoTime()) / 1000000000L))
                           )
                        )
                     );
                  }

                  if (this.init_I_18() < this.size || AiRunResources.isCampaignActive() && this.write_BlockPos == null) {
                     this.init_aB_TaskQueue = AiNetherRun.Inner_TaskQueue.FORTRESS;
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
                              AiSpeedrun.init_V_Str_ObjArr(
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
                           if (!this.write_BlockPos.equals(AiNetherRun.load_BlockPos)) {
                              AiNetherRun.load_BlockPos = this.write_BlockPos;
                              AiNetherRun.write();
                           }
                        }

                        if (this.init_I_18() < this.size) {
                           this.init_aB_TaskQueue = AiNetherRun.Inner_TaskQueue.RODS;
                           Entity blaze = this.load(24);
                           Entity inView = this.read(24);
                           if (inView != null) {
                              blaze = inView;
                           }

                           if (blaze != null && mc.player.hasLineOfSight(blaze)) {
                              boolean var42 = true;
                           } else {
                              boolean var41 = false;
                           }

                           if (this.init_I_18() != this.prev) {
                              this.prev = this.init_I_18();
                              this.set_J = System.nanoTime();
                           }

                           if (this.set_J == 0L || this.read(this.load_BlockPos) || this.init_D_BlockPos(this.load_BlockPos) > 24.0) {
                              this.set_J = System.nanoTime();
                           }

                           if (System.nanoTime() - this.set_J > 150000000000L) {
                              this.set_J = System.nanoTime();
                              this.valid++;
                              int around = AiWorldScan.findEntitiesByType("minecraft:blaze", 24.0).size();
                              if (blaze != null && around <= 2 && mc.player.getHealth() >= 16.0F && this.valid <= 2) {
                                 AiSpeedrun.init_V_Str_ObjArr(
                                    "[AI] nether: no rod for {} s at the spawner at {}; going after the blaze at {}",
                                    150L,
                                    this.load_BlockPos.toShortString(),
                                    blaze.blockPosition().toShortString()
                                 );
                                 this.flush_J = System.nanoTime();
                                 return this.init(
                                    "fight a blaze after a stall",
                                    AiNetherRun.init(
                                       AiBlazeFight.init(
                                          AiJson.objectOf(
                                             "count", AiNetherRun.init_I_I_I(this.size, this.init_I_18()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 60
                                          )
                                       ),
                                       "minecraft:blaze_rod",
                                       () -> AiGameplay.write(
                                          AiJson.objectOf(
                                             "item",
                                             "minecraft:blaze_rod",
                                             "count",
                                             Math.min(this.size, AiCraft.countItem("minecraft:blaze_rod") + 1),
                                             "radius",
                                             16,
                                             "timeoutSeconds",
                                             15
                                          )
                                       )
                                    )
                                 );
                              }

                              if (this.valid >= 2) {
                                 AiSpeedrun.init_V_Str_ObjArr(
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
                                 this.valid = 0;
                                 return this.load("Leaving a spawner that gave no rods");
                              }
                           }

                           boolean lavaLocked = this.init_Z(this.load_BlockPos, 4);
                           if (lavaLocked
                              && !this.read(this.load_BlockPos)
                              && (this.init_Z(mc.player.blockPosition(), 3) || mc.player.blockPosition().distSqr(this.load_BlockPos) > 225.0)) {
                              BlockPos wait = this.next < 6 ? this.load_BlockPos_2(this.load_BlockPos) : null;
                              if (wait == null && mc.player.blockPosition().distSqr(this.load_BlockPos) > 225.0) {
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] nether: the spawner at {} sits in lava with no spot to wait from; leaving it", this.load_BlockPos.toShortString()
                                    );
                                 this.read_Set.add(this.load_BlockPos.asLong());
                                 this.load_BlockPos = null;
                                 return this.load("Leaving a spawner in lava");
                              }

                              if (wait != null) {
                                 this.next++;
                                 return this.init(
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

                           if (this.stop >= 4 && blaze == null) {
                              AiSpeedrun.init_V_Str_ObjArr(
                                 "[AI] nether: {} walks to the spawner at {} made no progress; leaving it", this.stop, this.load_BlockPos.toShortString()
                              );
                              this.read_Set.add(this.load_BlockPos.asLong());
                              this.load_BlockPos = null;
                              this.stop = 0;
                              return this.load("Leaving a spawner no walk reaches");
                           }

                           if (blaze != null && AiPotion.init(mc) && this.set_I < 3) {
                              this.set_I++;
                              return this.init("drink fire resistance", AiPotion.init());
                           }

                           if (blaze != null && mc.player.getHealth() >= 10.0F) {
                              this.flush_J = System.nanoTime();
                              return this.init(
                                 "fight a blaze",
                                 AiNetherRun.init(
                                    AiBlazeFight.init(
                                       AiJson.objectOf(
                                          "count", AiNetherRun.init_I_I_I(this.size, this.init_I_18()), "radius", 24, "retreatHealth", 8, "timeoutSeconds", 120
                                       )
                                    ),
                                    "minecraft:blaze_rod",
                                    () -> AiGameplay.write(
                                       AiJson.objectOf(
                                          "item",
                                          "minecraft:blaze_rod",
                                          "count",
                                          Math.min(this.size, AiCraft.countItem("minecraft:blaze_rod") + 1),
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

                           if (this.read(this.load_BlockPos)) {
                              return this.init(
                                 "head for the blaze spawner at " + this.load_BlockPos.toShortString(), this.init_AiOperation_BlockPos_2(this.load_BlockPos)
                              );
                           }

                           if (!this.update(this.load_BlockPos)
                              || this.init_D_BlockPos(this.load_BlockPos) > 4.5
                              || Math.abs(mc.player.getY() - this.load_BlockPos.getY()) > 2.5) {
                              BlockPos stand = lavaLocked ? null : this.init_BlockPos_BlockPos(this.load_BlockPos);
                              if (stand != null && !stand.equals(mc.player.blockPosition())) {
                                 return this.init(
                                    "walk to a stand by the blaze spawner at " + stand.toShortString(),
                                    AiGoto.travelToCompat(
                                       stand, 0.5, true, true, true, 90, this.init_ClassCache_IoWorke(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                                    )
                                 );
                              }

                              if (stand == null
                                 && !lavaLocked
                                 && (this.init_D_BlockPos(this.load_BlockPos) > 2.5 || Math.abs(mc.player.getY() - this.load_BlockPos.getY()) > 2.0)) {
                                 return this.init(
                                    "walk to the blaze spawner at " + this.load_BlockPos.toShortString(),
                                    AiGoto.travelToCompat(
                                       this.load_BlockPos,
                                       2.0,
                                       true,
                                       true,
                                       true,
                                       120,
                                       this.init_ClassCache_IoWorke(),
                                       true,
                                       2.0,
                                       null,
                                       AiGoto.GotoOptions.DEFAULT
                                    )
                                 );
                              }

                              if (lavaLocked && this.init_Z(mc.player.blockPosition(), 3)) {
                                 BlockPos wait = this.load_BlockPos_2(this.load_BlockPos);
                                 if (wait != null) {
                                    return this.init(
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
                              return this.init("prepare minimal blaze-spawner cap and fireball screen", new AiBlazeArena(this.load_BlockPos, 10));
                           } else {
                              return this.load_Z_10()
                                 ? this.init("eat while waiting for blazes", AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 15)))
                                 : this.init("wait for blazes at the spawner", AiCombat.init(60));
                           }
                        } else {
                           if (this.write_BlockPos != null) {
                              return this.load("Actual blaze spawner visited and verified; material already sufficient");
                           }

                           BlockPos stand = this.init_BlockPos_BlockPos(this.load_BlockPos);
                           return this.init(
                              "walk to the blaze spawner for verification",
                              stand != null
                                 ? AiGoto.travelToCompat(stand, 0.8, true, true, true, 90, this.init_ClassCache_IoWorke(), true)
                                 : this.init_AiOperation_BlockPos_2(this.load_BlockPos)
                           );
                        }
                     } else {
                        BlockPos bricks = AiWorldScan.findNearestBlock(AiNetherRun.init_Predicate, 12);
                        if (bricks != null && this.read(bricks)) {
                           return this.init("head for the fortress at " + bricks.toShortString(), this.init_AiOperation_BlockPos_2(bricks));
                        } else if (bricks != null && this.init_Set.add(bricks.asLong() >> 6)) {
                           AiOperation.Result walk = this.init(
                              "walk into the fortress at " + bricks.toShortString(),
                              AiGoto.travelToCompat(
                                 bricks, 6.0, true, true, true, 180, this.init_ClassCache_IoWorke(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                              )
                           );
                           this.write_J = bricks.asLong() >> 6;
                           return walk;
                        } else {
                           return this.init_AiOperation_Result_Str_7("a fortress");
                        }
                     }
                  } else {
                     if (this.load_I_11() < pearls && optionalPearls) {
                        this.init_aB_TaskQueue = AiNetherRun.Inner_TaskQueue.PEARLS;
                        if (AiCraft.countItem("minecraft:gold_block") > 0 && this.sync_I < 3) {
                           this.sync_I++;
                           return this.init(
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

                        if (AiCraft.countItem("minecraft:gold_ingot") > 0 && this.flush_I < 20 && this.init_Z_D(32.0) && System.nanoTime() > this.push_J) {
                           this.flush_I++;
                           AiNetherRun.Inner_DataUtil goal = AiNetherRun.init(pearls, AiCraft.countItem("minecraft:ender_eye"), AiCraft.countItem("minecraft:ender_pearl"), this.tick_I);
                           return this.init(
                              "barter with the piglins",
                              AiBarter.init(
                                 AiJson.objectOf(
                                    "pearls",
                                    goal.pearls(),
                                    "gold",
                                    goal.gold(),
                                    "partners",
                                    3,
                                    "timeoutSeconds",
                                    (int)Math.max(60L, Math.min(600L, (this.init_J - System.nanoTime()) / 1000000000L))
                                 )
                              )
                           );
                        }

                        if (AiCraft.countItem("minecraft:gold_nugget") >= 9 && System.nanoTime() > this.poll_J) {
                           return this.init(
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

                        if (this.open_I < 48 && AiCraft.countItem("minecraft:gold_ingot") < 16 && this.init_Z_D(32.0)) {
                           BlockPos ore = this.init(s -> s.is(Blocks.NETHER_GOLD_ORE), false);
                           if (ore != null) {
                              this.open_I++;
                              return this.init(
                                 "extract a safe Nether gold batch",
                                 AiNetherRun.read(
                                    this.init_AiOperation(ore, 30),
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
                           BlockPos gold = this.init(s -> s.is(Blocks.GOLD_BLOCK), true);
                           if (gold != null) {
                              this.open_I++;
                              return this.init(
                                 "mine the gold block at " + gold.toShortString(),
                                 AiNetherRun.read(
                                    this.init_AiOperation(gold, 20),
                                    () -> AiGameplay.write(AiJson.objectOf("item", "minecraft:gold_block", "count", 64, "radius", 8, "timeoutSeconds", 10))
                                 )
                              );
                           }
                        }

                        Entity far = mc.player.getHealth() >= 16.0F ? this.init(48, false) : null;
                        if (far != null) {
                           this.load_Map.put(far.getId(), System.nanoTime() + 20000000000L);
                        }

                        if (far != null) {
                           return this.init(
                              "fight an enderman",
                              AiNetherRun.init_AiOperation_AiOperation_Supplier(
                                 AiGoto.travelToCompat(
                                    far.blockPosition(),
                                    6.0,
                                    true,
                                    true,
                                    true,
                                    60,
                                    this.init_ClassCache_IoWorke(),
                                    true,
                                    2.0,
                                    null,
                                    AiGoto.GotoOptions.DEFAULT
                                 ),
                                 () -> AiNetherRun.init(
                                    this.init(
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

                        BlockPos warped = AiWorldScan.findNearestBlock(AiNetherRun.load_Predicate, 12);
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

                        if (warped != null && this.read(warped) && !this.init_Set.contains(warped.asLong() >> 6)) {
                           return this.init("head for the warped forest at " + warped.toShortString(), this.init_AiOperation_BlockPos_2(warped));
                        }

                        if (warped != null && this.init_D_BlockPos(warped) > 12.0 && this.init_Set.add(warped.asLong() >> 6)) {
                           AiOperation.Result walk = this.init(
                              "walk into the warped forest at " + warped.toShortString(),
                              AiGoto.travelToCompat(
                                 warped, 6.0, true, true, true, 180, this.init_ClassCache_IoWorke(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                              )
                           );
                           this.write_J = warped.asLong() >> 6;
                           return walk;
                        }

                        if (this.load_I_11() > this.start) {
                           this.start = this.load_I_11();
                           this.reset = 0;
                        }

                        if (!AiWorldScan.findEntitiesByType("minecraft:enderman", 48.0).isEmpty() && this.reset < 24) {
                           if (!AiForage.init(mc, mc.player.blockPosition())
                              && mc.player.onGround()
                              && AiForage.load(mc, mc.player.blockPosition())
                              && System.nanoTime() > this.get_J) {
                              this.get_J = System.nanoTime() + 10000000000L;
                              BlockPos under = mc.player.blockPosition().below();
                              return this.init(
                                 "dig a fighting hole at " + under.toShortString(),
                                 AiGameOperations.blockAction(
                                    "break",
                                    AiJson.objectOf(
                                       "x", under.getX(), "y", under.getY(), "z", under.getZ(), "requireDrops", false, "timeoutSeconds", 8
                                    )
                                 )
                              );
                           }

                           if (this.load_Z_10()) {
                              return this.init("eat while waiting for endermen", AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 15)));
                           }

                           this.reset++;
                           return this.init("wait for the endermen to come round (" + this.reset + " of 24)", AiCombat.init(100));
                        }

                        if (this.load_I < 14 || this.init_aB_CachePool.load_Z && AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).size() >= 2) {
                           if (this.load_I >= 14 && !AiWorldScan.findEntitiesByType("minecraft:enderman", 32.0).isEmpty()) {
                              return this.init("wait for the endermen to come round", AiCombat.init(40));
                           }

                           return this.init_AiOperation_Result_Str_7("endermen");
                        }
                     }

                     return this.read_AiOperation_Result_8();
                  }
               }
            }

            AiOperation.Result read_AiOperation_Result_8() {
               boolean routeRootProven = AiNetherRun.init(
                  this.write_BlockPos != null, this.tick_I > 0, AiRunResources.isCampaignActive(), AiRunResources.blazePowderEquivalents(), 16, this.load_I_11() + AiCraft.countItem("minecraft:ender_eye")
               );
               if (AiRunResources.isCampaignActive() && !AiNetherRun.init_Z && this.write_BlockPos == null) {
                  return this.load_I < 14
                     ? this.init_AiOperation_Result_Str_7("an actual reachable blaze spawner")
                     : this.init(AiOperation.Status.FAILED, "Required blaze spawner visit remains unverified");
               }

               if (AiRunResources.isCampaignActive() && !AiNetherRun.init_Z && this.tick_I == 0 && !routeRootProven) {
                  return this.load_I < 14
                     ? this.init_AiOperation_Result_Str_7("safe adult piglins and barter gold")
                     : this.init(
                        AiOperation.Status.FAILED,
                        "No genuine barter completed and the expedition's materials are short; required barter route remains unfinished"
                     );
               }

               this.init_aB_TaskQueue = AiNetherRun.Inner_TaskQueue.RETURN;
               BlockPos nearby = AiNetherRun.init(mc, mc.player.blockPosition(), 8);
               BlockPos loadedPortal = nearby != null
                  ? nearby
                  : this.init_aB_BufferHelper
                     .init(
                        () -> AiWorldScan.findNearestBlock(s -> s.is(Blocks.NETHER_PORTAL), 12),
                        at -> AiTerrain.isLoaded(mc.level, at) && mc.level.getBlockState(at).is(Blocks.NETHER_PORTAL)
                     );
               this.init_BlockPos = AiNetherRun.init(loadedPortal, AiPortalLinks.init(2147483647), this.init_BlockPos, at -> AiNetherRun.init_Z_Minecraft_BlockPos(mc, at));
               if (this.init_BlockPos != null) {
                  AiNetherRun.init_BlockPos = this.init_BlockPos;
               }

               int buildSeconds = AiNetherRun.init_I_J_J(this.init_J, System.nanoTime());
               if (this.init_BlockPos == null) {
                  if (this.load_Z
                     || AiCraft.countItem("minecraft:obsidian") < 10
                     || AiCraft.countItem("minecraft:flint_and_steel") <= 0 && AiCraft.countItem("minecraft:fire_charge") <= 0) {
                     String message = "No observed return portal in loaded terrain or recorded transitions; the entry is unknown";
                     AiOperation.Result unknown = this.init(AiOperation.Status.FAILED, message);
                     return new AiOperation.Result(
                        unknown.status(),
                        unknown.message(),
                        unknown.dataJson(),
                        unknown.evidence(),
                        unknown.status() == AiOperation.Status.FAILED ? AiFailure.TARGET_NOT_FOUND : unknown.failure()
                     );
                  }

                  if (buildSeconds == 0) {
                     return this.init(AiOperation.Status.TIMED_OUT, "Not enough expedition time remains to build a confirmed portal home");
                  }

                  this.load_Z = true;
                  AiRunTrace.emitServiceEvent(
                     "nether_exit_plan",
                     AiJson.objectOf(
                        "choice", "remote_frame", "entryKnown", false, "loadedPortalSearchCompleted", this.init_aB_BufferHelper.init(), "predictionOnly", true
                     )
                  );
                  return this.init(
                     "build a portal home from the unknown entry",
                     AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "build", true, "timeoutSeconds", buildSeconds))
                  );
               } else {
                  BlockPos door = AiNetherRun.init(mc, this.init_BlockPos, 6);
                  double returnTicks = this.init_D_BlockPos(this.init_BlockPos) / 0.22
                     + Math.abs(mc.player.getY() - this.init_BlockPos.getY()) * 6.0
                     + this.clear * 1200;
                  double remoteTicks = 500.0;
                  if (door != null
                     || !(remoteTicks < returnTicks)
                     || this.load_Z
                     || AiCraft.countItem("minecraft:obsidian") < 10
                     || AiCraft.countItem("minecraft:flint_and_steel") <= 0 && AiCraft.countItem("minecraft:fire_charge") <= 0) {
                     if (door == null && this.read(this.init_BlockPos)) {
                        return this.init(
                           "head for the entry portal at " + this.init_BlockPos.toShortString(), this.init_AiOperation_BlockPos_2(this.init_BlockPos)
                        );
                     }

                     if (door != null || !(this.init_D_BlockPos(this.init_BlockPos) > 12.0) && !(Math.abs(mc.player.getY() - this.init_BlockPos.getY()) > 6.0)) {
                        if (door == null) {
                           return this.init(AiOperation.Status.FAILED, "The entry portal at " + this.init_BlockPos.toShortString() + " is gone");
                        } else {
                           return !(this.init_D_BlockPos(door) > 2.0) && !(Math.abs(mc.player.getY() - door.getY()) > 2.0)
                              ? this.init(
                                 "through the portal", AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "timeoutSeconds", 60))
                              )
                              : this.init(
                                 "walk back to the portal at " + door.toShortString(),
                                 AiGoto.travelToCompat(
                                    door, 1.0, true, true, true, 300, this.init_ClassCache_IoWorke(), true, 2.0, null, AiGoto.GotoOptions.DEFAULT
                                 )
                              );
                        }
                     } else {
                        return this.init(
                           "walk back toward the entry portal at " + this.init_BlockPos.toShortString(),
                           AiGoto.travelToCompat(
                              this.init_BlockPos,
                              3.0,
                              true,
                              true,
                              true,
                              300,
                              this.init_ClassCache_IoWorke(),
                              true,
                              2.0,
                              null,
                              AiGoto.GotoOptions.DEFAULT
                           )
                        );
                     }
                  } else {
                     if (buildSeconds == 0) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Not enough expedition time remains to build a confirmed portal home");
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
                     return this.init(
                        "build a portal home here",
                        AiDimension.init(AiJson.objectOf("action", "enter", "target", "overworld", "build", true, "timeoutSeconds", buildSeconds))
                     );
                  }
               }
            }

            AiOperation.Result init_AiOperation_Result_Str_7(String what) {
               if (this.load_I >= 28) {
                  return this.init(AiOperation.Status.FAILED, "Explored " + this.load_I + " legs and found no " + what);
               }

               this.load_I++;
               ClientLevel level = mc.level;
               AiNetherRun.init_Obj = level;
               BlockPos at = mc.player.blockPosition();
               this.sync_BlockPos = at;
               int cx = at.getX() >> 4;
               int cz = at.getZ() >> 4;

               for (int dx = -5; dx <= 5; dx++) {
                  for (int dz = -5; dz <= 5; dz++) {
                     AiNetherRun.init_Set.add(ChunkPos.pack(cx + dx, cz + dz));
                  }
               }

               int best = -1;
               int bestUnseen = -1;
               BlockPos bastion = AiWorldScan.findNearestBlock(AiNetherRun.read_Predicate, 12);
               if (bastion != null && AiNetherStructures.init_aC_DataUtil(bastion).confidence() < 0.5) {
                  bastion = null;
               }

               for (int i = 0; i < 8; i++) {
                  int h = this.read_I + i & 7;
                  BlockPos to = this.init_BlockPos(at, h);
                  if (!AiNetherRun.init(to) && (bastion == null || !this.init(at, to, bastion))) {
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
                  this.write_Z = true;
                  return this.load("No way left to look for endermen; heading home");
               }

               if (best < 0 && !this.update_Z) {
                  this.update_Z = true;
                  AiNetherRun.init_List.clear();
                  return this.load("Every way out failed once; trying them again");
               }

               if (best < 0) {
                  return this.init(AiOperation.Status.FAILED, "Every way out from here failed; no " + what + " found");
               }

               this.read_I = best;
               BlockPos to = this.init_BlockPos(at, this.read_I);
               this.update_BlockPos = to;
               AiOperation.Result leg = this.init(
                  "explore for " + what + " toward " + to.toShortString(),
                  AiGoto.createPersistentOpCompat(to, new AiNetherRun.Inner_NetHelper(to.getX(), to.getZ(), 10), 90),
                  0,
                  true
               );
               this.update_BlockPos = to;
               return leg;
            }

            BlockPos init_BlockPos(BlockPos at, int h) {
               double a = Math.toRadians(h * 45.0);
               return new BlockPos(at.getX() + (int)Math.round(Math.cos(a) * 96.0), at.getY(), at.getZ() + (int)Math.round(Math.sin(a) * 96.0));
            }

            void init_V_18() {
               if (this.init_AiOperation != null) {
                  if (this.load_Str.startsWith("mine safe barter gold") || this.load_Str.startsWith("extract a safe Nether gold batch")) {
                     this.read_Str = this.load_Str;
                  } else if (this.load_Str.startsWith("fight a blaze") && this.init_AiOperation_Result != null) {
                     JsonObject data = AiRunResources.parseEvidenceJson(this.init_AiOperation_Result.dataJson());
                     if (data.get("target") instanceof JsonObject target && AiJson.getInt(target, "id", -1) >= 0) {
                        Entity entity = mc.level.getEntity(AiJson.getInt(target, "id", -1));
                        if (entity instanceof Blaze && entity.isAlive()) {
                           this.init_I = entity.getId();
                        }
                     }
                  }
               }
            }

            AiOperation.Result init(String label, AiOperation op) {
               return this.init(label, op, 0);
            }

            AiOperation.Result init(String label, AiOperation op, int waitTicks) {
               return this.init(label, op, waitTicks, false);
            }

            AiOperation.Result init(String label, AiOperation op, int waitTicks, boolean session) {
               this.load_Str = label;
               this.init_AiOperation_Result = null;
               this.write_J = -9223372036854775808L;
               this.sync_J = System.nanoTime();
               AiSpeedrun.init_V_Str_ObjArr("[AI] nether: {}", label);
               AiOperation inner = op;
               if (session && op instanceof AiGoto.GotoOp travel) {
                  inner = AiNetherRun.init(travel);
               }

               if (waitTicks > 0) {
                  inner = AiNetherRun.init_AiOperation_I_AiOperation(waitTicks, inner);
               }

               this.init_AiOperation = AiRunDiagnostics.init_AiOperation_Str("nether", label, inner);
               return this.load(label);
            }

            AiOperation init_AiOperation_BlockPos_2(BlockPos target) {
               double dx = target.getX() + 0.5 - mc.player.getX();
               double dz = target.getZ() + 0.5 - mc.player.getZ();
               int turn = this.add == 0 ? 0 : (this.add % 2 == 1 ? 1 : -1) * ((this.add + 1) / 2);
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
                  this.init_ClassCache_IoWorke(),
                  true,
                  2.0,
                  new AiNetherRun.Inner_NetHelper(x, z, 6),
                  AiGoto.GotoOptions.DEFAULT
               );
            }

            boolean read(BlockPos target) {
               return this.init_D_BlockPos(target) > 64.0 && this.add < 5;
            }

            autismclient.ai.engine.AiPathfinder.RouteStrategy init_ClassCache_IoWorke() {
               return this.put_I > 0 ? autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST : autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST;
            }

            AiOperation init(final int enderman, final AiOperation fight) {
               return new AiOperation() {
                  int init;

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
                           this.init = mc.player.onGround() && !write(mc.player.blockPosition()) ? this.init + 1 : 0;
                           if (this.init > 10) {
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

            boolean load_Z_10() {
               return mc.player.getHealth() < mc.player.getMaxHealth() && mc.player.getFoodData().getFoodLevel() < 18 && AiSurvival.init(mc.player.getInventory()) >= 0;
            }

            Entity init_Entity_I(int radius) {
               long now = System.nanoTime();

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:enderman", radius)) {
                  Long until = this.load_Map.get(e.getId());
                  if ((until == null || now >= until)
                     && !(e.getY() < mc.player.getY() - 16.0)
                     && !(e.getY() > mc.player.getY() + 16.0)
                     && mc.player.hasLineOfSight(e)) {
                     return e;
                  }
               }

               return null;
            }

            AiOperation init_AiOperation_I(int enderman) {
               return AiForage.init_AiOperation_I(enderman, p -> AiForage.write(mc, p), () -> this.load_Map.put(enderman, System.nanoTime() + 120000000000L));
            }

            Entity init(int radius, boolean here) {
               if (here && !this.write(mc.player.blockPosition())) {
                  return null;
               }

               long now = System.nanoTime();

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:enderman", radius)) {
                  Long until = this.load_Map.get(e.getId());
                  if ((until == null || now >= until) && this.write(e.blockPosition())) {
                     return e;
                  }
               }

               return null;
            }

            boolean write(BlockPos at) {
               return AiForage.read(mc, at);
            }

            boolean update(BlockPos pos) {
               return this.init(mc.player.getEyePosition(), pos);
            }

            boolean init(Vec3 eye, BlockPos pos) {
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
                        && this.init(new Vec3(p.getX() + 0.5, p.getY() + 1.62, p.getZ() + 0.5), spawner)
                        && AiCombat.init_Z_Minecraft_BlockPos(mc, p)
                        && !this.init_Z(p, 3)) {
                        double d = mc.player.distanceToSqr(p.getX() + 0.5, p.getY(), p.getZ() + 0.5) + (this.sync(p) ? 0 : 400);
                        if (d < bestD) {
                           bestD = d;
                           best = p.immutable();
                        }
                     }
                  }
               }

               return best;
            }

            boolean init_Z(BlockPos feet, int r) {
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
                        if (!(d >= bestD) && AiCombat.init_Z_Minecraft_BlockPos(mc, p) && !this.init_Z(p, 3) && this.sync(p)) {
                           if (!this.init(new Vec3(p.getX() + 0.5, p.getY() + 1.62, p.getZ() + 0.5), spawner)) {
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

            boolean sync(BlockPos feet) {
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

            Entity load(int radius) {
               Entity best = null;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (e.isAlive() && !this.init_Z_Entity_2(e) && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
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

            boolean init_Z_Entity_2(Entity e) {
               Long until = AiBlazeFight.init_Map.get(e.getUUID());
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

            Entity read(int radius) {
               Entity best = null;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (e.isAlive()
                     && !this.init_Z_Entity_2(e)
                     && mc.player.hasLineOfSight(e)
                     && (best == null || e.distanceToSqr(mc.player) < best.distanceToSqr(mc.player))) {
                     best = e;
                  }
               }

               return best;
            }

            int init_I_I_2(int radius) {
               int n = 0;

               for (Entity e : AiWorldScan.findEntitiesByType("minecraft:blaze", radius)) {
                  if (mc.player.hasLineOfSight(e)) {
                     n++;
                  }
               }

               return n;
            }

            int init_I_18() {
               return AiRunResources.blazePowderEquivalents() / 2;
            }

            int load_I_11() {
               return AiCraft.countItem("minecraft:ender_pearl") + AiCraft.countItem("minecraft:ender_eye");
            }

            double init_D_BlockPos(BlockPos p) {
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
                           Math.max(0L, (this.load_Map.getOrDefault(target.getId(), 0L) - now) / 1000000000L)
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
                     AiSurvival.init(mc.player.getInventory()) >= 0,
                     "provokeRetryInSeconds",
                     Math.max(0L, (this.tick_J - now) / 1000000000L),
                     "waitsUsed",
                     this.reset,
                     "waitsLimit",
                     24
                  );
                  return this.init_JsonObject;
               } else {
                  return this.init_JsonObject;
               }
            }

            JsonObject read_JsonObject() {
               return AiJson.objectOf(
                  "step",
                  this.init_aB_TaskQueue == null ? "" : this.init_aB_TaskQueue.name(),
                  "rods",
                  this.init_I_18(),
                  "pearls",
                  this.load_I_11(),
                  "legs",
                  this.load_I,
                  "waits",
                  this.write_I,
                  "rawRods",
                  AiCraft.countItem("minecraft:blaze_rod"),
                  "powderSideEyes",
                  AiRunResources.blazePowderEquivalents(),
                  "genuineBarters",
                  this.tick_I,
                  "goldSpent",
                  this.run_I,
                  "pearlRecovery",
                  pearlRecovery,
                  "rodGoal",
                  this.size,
                  "returningForSupplies",
                  AiNetherRun.init_Z,
                  "returnReason",
                  this.write_Str,
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
                  this.init_BlockPos == null ? "" : this.init_BlockPos.toShortString(),
                  "entryKnown",
                  this.init_BlockPos != null,
                  "loadedReturnPortalSearchCompleted",
                  this.init_aB_BufferHelper.init(),
                  "steps",
                  this.init_JsonArray
               );
            }

            AiOperation.Result load(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.read_JsonObject());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               return this.init(status, message, this.init_AiOperation_Result);
            }

            AiOperation.Result init(AiOperation.Status status, String message, AiOperation.Result child) {
               AiGoal.init(this);
               boolean pending = this.requiresReconciliationAfterEmergency();
               AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(status, message, this.read_JsonObject(), pending, child);
               this.sync_Z = this.sync_Z | (pending || AiJson.getBoolean(AiRunResources.parseEvidenceJson(result.dataJson()), "reobserveBeforeResuming", false));
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.init_Y.cancel();
               return result;
            }

            @Override
            public boolean canInterruptNow() {
               return !this.sync_Z && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow()) && this.init_Y.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.sync_Z || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency() || this.init_Y.requiresReconciliation();
            }

            @Override
            public void cancel() {
               AiGoal.init(this);
               this.sync_Z = this.sync_Z | this.requiresReconciliationAfterEmergency();
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               this.init_Y.cancel();
            }
         };
      } else {
         throw new IllegalArgumentException("The Nether run needs a connected living player");
      }
   }

   static AiNetherRun.Inner_ConfigStore init_aB_ConfigStore(JsonObject stored) {
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
                  ? new AiNetherRun.Inner_ConfigStore(barters, new BlockPos(x, y, z), at)
                  : null;
            } else {
               return null;
            }
         }
      } catch (RuntimeException malformed) {
         return null;
      }
   }

   private static void read() {
      Minecraft mc = Minecraft.getInstance();
      String currentWorld = AiWorldIdentity.currentWorldKey(mc);
      if (!currentWorld.equals(init_Str) || read_Obj != mc.getConnection()) {
         init_Str = currentWorld;
         read_Obj = mc.getConnection();
         flush_J++;
         load_Z = false;
         init_BlockPos = null;
         load_List.clear();
         init_Set.clear();
         init_List.clear();
         init_Map.clear();
         flush_I = 0;
         load_Obj = null;
         push = 0;
         load_BlockPos = null;
         init_Z = false;
         read_Z = false;
      }

      AiService service = AiService.getExistingInstance();
      if (!load_Z && service != null && mc.getConnection() != null) {
         String requestedWorld = init_Str;
         Object requestedConnection = read_Obj;
         long generation = flush_J;
         load_Z = service.loadNetherRouteMemory(
            requestedWorld,
            entries -> {
               if (generation == flush_J
                  && requestedWorld.equals(init_Str)
                  && read_Obj == requestedConnection
                  && mc.getConnection() == requestedConnection
                  && AiWorldIdentity.currentWorldKey(mc).equals(requestedWorld)) {
                  for (JsonElement entry : entries) {
                     AiNetherRun.Inner_ConfigStore receipt = entry.isJsonObject() ? init_aB_ConfigStore(entry.getAsJsonObject()) : null;
                     if (receipt != null) {
                        push = Math.max(push, receipt.barters());
                        if (load_BlockPos == null) {
                           load_BlockPos = receipt.spawner();
                        }

                        AiRunTrace.emitServiceEvent("nether_route_history", receipt.data());
                     }
                  }
               }
            }
         );
      }
   }

   private static void write() {
      AiService service = AiService.getExistingInstance();
      if (service != null && push > 0 && load_BlockPos != null) {
         JsonArray receipts = new JsonArray();
         receipts.add(new AiNetherRun.Inner_ConfigStore(push, load_BlockPos, System.currentTimeMillis()).data());
         service.saveNetherRouteMemory(init_Str, receipts);
      }
   }

   static void init_V() {
      read();
      read_Z = true;
   }

   static boolean init_Z() {
      read();
      return read_Z;
   }

   static boolean load_Z() {
      read();
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && init_Str.equals(AiWorldIdentity.currentWorldKey(mc)) && read_Obj == mc.getConnection()) {
         return read_Z
            ? true
            : init(load_BlockPos != null, push > 0, AiRunResources.isCampaignActive(), AiRunResources.blazePowderEquivalents(), 16, AiCraft.countItem("minecraft:ender_eye") + AiCraft.countItem("minecraft:ender_pearl"));
      } else {
         return false;
      }
   }

   static void load_V() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         read();
         push = Math.max(push, 1);
         if (load_BlockPos == null) {
            load_BlockPos = BlockPos.ZERO;
         }
      }
   }

   static boolean init(boolean spawnerConfirmed, boolean bartersObserved, boolean campaign, int rodPowder, int wantedPowder, int pearlEquivalents) {
      return spawnerConfirmed && (bartersObserved || campaign && rodPowder >= wantedPowder && pearlEquivalents >= 16);
   }

   static BlockPos init(Minecraft mc, BlockPos around, int radius) {
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

   private static boolean init_Z_Minecraft_BlockPos(Minecraft mc, BlockPos at) {
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

   static AiOperation init_AiOperation_AiOperation_Supplier(AiOperation first, Supplier<AiOperation> second) {
      return init(first, second, false);
   }

   static AiOperation load(AiOperation first, Supplier<AiOperation> second) {
      return init(first, second, true);
   }

   static AiOperation read(AiOperation first, Supplier<AiOperation> second) {
      return init(first, second, true);
   }

   private static AiOperation init(final AiOperation first, final Supplier<AiOperation> second, final boolean successOnly) {
      return new AiOperation() {
         AiOperation init_AiOperation = first;
         boolean init_Z;
         boolean load;
         AiOperation.Result init_AiOperation_Result;

         @Override
         public AiOperation.Result tick() {
            if (this.init_AiOperation_Result != null) {
               return this.init_AiOperation_Result;
            }

            AiOperation.Result r = this.init_AiOperation.tick();
            if (!r.terminal()) {
               return r;
            }

            this.load = this.init_AiOperation.requiresReconciliationAfterEmergency();
            r = AiNetherRun.init_AiOperation_Result_AiOperation_AiOperation_Result(this.init_AiOperation, r);
            this.load = this.load | AiJson.getBoolean(AiRunResources.parseEvidenceJson(r.dataJson()), "reobserveBeforeResuming", false);
            if (this.init_Z
               || r.status() == AiOperation.Status.UNCERTAIN
               || r.status() == AiOperation.Status.CANCELLED
               || successOnly && r.status() != AiOperation.Status.SUCCEEDED) {
               return this.init_AiOperation_Result = r;
            }

            this.init_Z = true;
            this.init_AiOperation = second.get();
            return AiGameOperations.result(AiOperation.Status.RUNNING, r.message(), new JsonObject());
         }

         @Override
         public boolean canInterruptNow() {
            return !this.load && this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.load || this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_AiOperation.cancel();
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

   static AiOperation init(
      final AiOperation mining,
      final BooleanSupplier contextValid,
      final BooleanSupplier targetGone,
      final BooleanSupplier watched,
      final LongSupplier revision,
      final Runnable refused
   ) {
      return new AiOperation() {
         long init_J = revision.getAsLong();
         int init_I;
         boolean init_Z;
         boolean load;
         boolean read;
         AiOperation.Result init_AiOperation_Result;

         @Override
         public AiOperation.Result tick() {
            if (this.init_AiOperation_Result != null) {
               return this.init_AiOperation_Result;
            }

            if (!this.load && contextValid.getAsBoolean()) {
               boolean gone = targetGone.getAsBoolean();
               if (gone && !this.read) {
                  refused.run();
                  return this.init(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "Gold target already disappeared; trying another", "{}"));
               }

               if (gone) {
                  this.init_Z = true;
                  mining.cancel();
                  if (revision.getAsLong() > this.init_J) {
                     this.init_Z = false;
                     return this.init(new AiOperation.Result(AiOperation.Status.SUCCEEDED, "Gold removal acknowledged", "{}", List.of()));
                  }
               } else if (this.init_Z && revision.getAsLong() > this.init_J) {
                  this.init_Z = false;
                  refused.run();
                  return this.init(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "Server restored the gold target; trying another", "{}"));
               }

               if (this.init_Z) {
                  return ++this.init_I > 60
                     ? this.init(new AiOperation.Result(AiOperation.Status.UNCERTAIN, "Gold removal was not acknowledged", "{}", List.of()))
                     : new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for gold removal acknowledgement", "{}", List.of());
               }

               if (watched.getAsBoolean()) {
                  refused.run();
                  return this.init(AiOperation.Result.failed(AiFailure.TARGET_NOT_FOUND, "A piglin sees this mining stance; trying another gold target", "{}"));
               }

               this.init_J = revision.getAsLong();
               this.read = true;
               AiOperation.Result result = mining.tick();
               if (targetGone.getAsBoolean()) {
                  this.init_Z = revision.getAsLong() <= this.init_J;
               }

               if (!result.terminal() || result.status() == AiOperation.Status.SUCCEEDED && this.init_Z) {
                  if (this.init_Z) {
                     mining.cancel();
                     return new AiOperation.Result(AiOperation.Status.RUNNING, "Waiting for gold removal acknowledgement", "{}", List.of());
                  } else {
                     return result;
                  }
               } else {
                  return this.init(result);
               }
            } else {
               return this.init(new AiOperation.Result(AiOperation.Status.CANCELLED, "Gold mining context changed", "{}", List.of()));
            }
         }

         AiOperation.Result init(AiOperation.Result result) {
            this.init_Z = this.requiresReconciliationAfterEmergency();
            this.init_AiOperation_Result = AiGuardLayer.wrapPendingTermination(this.init_Z, result);
            mining.cancel();
            return this.init_AiOperation_Result;
         }

         @Override
         public boolean canInterruptNow() {
            return !this.requiresReconciliationAfterEmergency() && mining.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_Z
               || mining.requiresReconciliationAfterEmergency()
               || this.read && contextValid.getAsBoolean() && targetGone.getAsBoolean() && revision.getAsLong() <= this.init_J;
         }

         @Override
         public void cancel() {
            this.init_Z = this.requiresReconciliationAfterEmergency();
            this.load = true;
            mining.cancel();
         }
      };
   }

   static AiOperation init(final AiOperation fight, final String item, final Supplier<AiOperation> pickup) {
      return new AiOperation() {
         AiOperation init_AiOperation = fight;
         boolean init_Z;
         AiOperation.Result init_AiOperation_Result;

         @Override
         public AiOperation.Result tick() {
            AiOperation.Result r = this.init_AiOperation.tick();
            if (this.init_Z) {
               JsonObject data = AiRunResources.parseEvidenceJson(r.dataJson());
               data.add("fight", AiRunResources.compactEvidenceJson(this.init_AiOperation_Result.dataJson()));
               data.addProperty("fightStatus", this.init_AiOperation_Result.status().name());
               data.addProperty("fightMessage", AiSpeedrun.init_Str_Str(this.init_AiOperation_Result.message()));
               if (AiNetherRun.init_Z_Z_A(this.init_AiOperation_Result.status() == AiOperation.Status.SUCCEEDED, r.failure())) {
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

               this.init_AiOperation_Result = r;
               this.init_AiOperation.cancel();
               if (r.status() != AiOperation.Status.SUCCEEDED && !AiNetherRun.init_Z_Str_D(item, 16.0)) {
                  return r;
               }

               this.init_Z = true;
               this.init_AiOperation = pickup.get();
               return AiGameOperations.result(
                  AiOperation.Status.RUNNING,
                  r.message(),
                  AiJson.objectOf("fight", AiRunResources.compactEvidenceJson(r.dataJson()), "fightStatus", r.status().name())
               );
            }
         }

         @Override
         public boolean canInterruptNow() {
            return this.init_AiOperation.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_AiOperation.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            this.init_AiOperation.cancel();
         }
      };
   }

   static boolean init(String label) {
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
         HashSet<String> ids = new HashSet<>(AiGameplay.init_List(item));
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

   static boolean init_Z(JsonObject evidence) {
      return AiJson.getBoolean(evidence, "retreated", false)
         || evidence.has("fight") && evidence.get("fight").isJsonObject() && AiJson.getBoolean(evidence.getAsJsonObject("fight"), "retreated", false);
   }

   static AiOperation init_AiOperation_I_AiOperation(final int ticks, final AiOperation then) {
      return new AiOperation() {
         int init_I = Math.max(0, ticks);
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_I > 0) {
               this.init_I--;
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting", new JsonObject());
            } else {
               this.init_Z = true;
               return then.tick();
            }
         }

         @Override
         public boolean canInterruptNow() {
            return !this.init_Z || then.canInterruptNow();
         }

         @Override
         public boolean requiresReconciliationAfterEmergency() {
            return this.init_Z && then.requiresReconciliationAfterEmergency();
         }

         @Override
         public void cancel() {
            then.cancel();
         }
      };
   }

   static final class Inner_BufferHelper {
      private boolean init_Z;
      private BlockPos init_BlockPos;

      BlockPos init(Supplier<BlockPos> scan, Predicate<BlockPos> stillLoadedPortal) {
         if (!this.init_Z) {
            this.init_Z = true;
            this.init_BlockPos = scan.get();
         }

         return this.init_BlockPos != null && stillLoadedPortal.test(this.init_BlockPos) ? this.init_BlockPos : null;
      }

      boolean init() {
         return this.init_Z;
      }
   }

   static final class Inner_CachePool {
      long init_J;
      long load_J;
      long read;
      int init_I;
      boolean init_Z;
      boolean load_Z;

      boolean init(long now, boolean routeComplete, int held, int wanted, boolean pearlGround, boolean givenUp, boolean returningForSupplies) {
         if (returningForSupplies) {
            return false;
         }

         if (!routeComplete) {
            return true;
         }

         if (held < wanted && !givenUp) {
            if (!this.init_Z) {
               this.init_Z = true;
               this.init_J = this.load_J = now;
               this.init_I = held;
            }

            if (held == 0 && !this.load_Z) {
               if (pearlGround) {
                  this.load_Z = true;
               } else if (now - this.init_J < 1080000000000L) {
                  this.load_J = now;
               }
            }

            if (held > this.init_I) {
               this.init_I = held;
               this.read = now;
            }

            long hunted = now - this.load_J;
            boolean paying = this.read != 0L && now - this.read < 90000000000L && hunted < 1080000000000L;
            return hunted < 300000000000L || paying;
         } else {
            return false;
         }
      }
   }

   record Inner_ConfigStore(int barters, BlockPos spawner, long observedAtMs) {
      JsonObject data() {
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

   record Inner_DataUtil(int pearls, int gold) {
   }

   record Inner_NetHelper(int x, int z, int radius) implements autismclient.ai.engine.AiPathfinder.PathGoal {
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

   static final class Inner_StreamUtil {
      private final LinkedHashMap<Long, Long> init_LinkedHashMap = new LinkedHashMap<>();
      final autismclient.ai.engine.AiMiningCommitment init_FieldCache = new autismclient.ai.engine.AiMiningCommitment();
      final autismclient.ai.engine.AiMiningCommitment load = new autismclient.ai.engine.AiMiningCommitment();

      autismclient.ai.engine.AiMiningCommitment init(boolean block) {
         return block ? this.load : this.init_FieldCache;
      }

      void init_V(long cell, long now) {
         this.init_LinkedHashMap.remove(cell);
         this.init_LinkedHashMap.put(cell, now + 30000000000L);

         while (this.init_LinkedHashMap.size() > 64) {
            this.init_LinkedHashMap.remove(this.init_LinkedHashMap.keySet().iterator().next());
         }
      }

      boolean init_Z(long cell, long now) {
         Long until = this.init_LinkedHashMap.get(cell);
         if (until == null) {
            return true;
         }

         if (now < until) {
            return false;
         }

         this.init_LinkedHashMap.remove(cell);
         return true;
      }
   }

   enum Inner_TaskQueue {
      FORTRESS,
      RODS,
      PEARLS,
      RETURN;
   }
}
