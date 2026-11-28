package autismclient.ai;

import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import autismclient.compat263.Compat263;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiStronghold {
   static final long init_J = 180000000000L;
   static final int init_I = 40;
   static final long load_J = 480000000000L;
   static final int load_I = 160;
   static final int read_I = 640;
   static final int write_I = 12;
   static final long read_J = 600000000000L;
   static final float[] EYE_THROW_PITCHES = new float[]{-25.0F, -45.0F, -65.0F, -85.0F};
   static final int update = 120;
   private static String navigationWorld = "";
   private static Object navigationConnection;
   private static AiEyeNavigation navigation;
   private static Vec3 savedBearing;
   private static int savedThrowCount;
   private static long write_J;
   private static BlockPos init_BlockPos;

   private AiStronghold() {
   }

   static AiOperation.Result resultWithChild(AiOperation.Status status, String message, JsonObject data, boolean pending, AiOperation.Result child) {
      JsonObject evidence = data.deepCopy();
      if (child != null) {
         JsonObject receipt = AiRunResources.parseEvidenceJson(child.dataJson());
         evidence.add("terminalChild", AiRunResources.compactEvidenceJson(child.dataJson()));
         evidence.addProperty("terminalChildStatus", child.status().name());
         pending |= AiJson.getBoolean(receipt, "interruptedActionNeedsReconciliation", false)
            || AiJson.getBoolean(receipt, "reobserveBeforeResuming", false);
      }

      return AiGuardLayer.wrapPendingTermination(pending, AiGameOperations.result(status, message, evidence));
   }

   private static AiEyeNavigation navigationFor(Minecraft mc) {
      String world = AiWorldIdentity.currentWorldKey(mc);
      if (navigation == null || !world.equals(navigationWorld) || navigationConnection != mc.getConnection()) {
         navigationWorld = world;
         navigationConnection = mc.getConnection();
         navigation = new AiEyeNavigation();
         savedBearing = null;
         savedThrowCount = 0;
         write_J = 0L;
         init_BlockPos = null;
      }

      return navigation;
   }

   static AiOperation create(JsonObject args) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final int timeoutSeconds = Math.max(120, Math.min(5400, AiJson.getInt(args, "timeoutSeconds", 2400)));
         return new AiOperation() {
            final long deadlineNanos = System.nanoTime() + timeoutSeconds * 1000000000L;
            final JsonArray steps = new JsonArray();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            final AiEyeNavigation navigation = AiStronghold.navigationFor(mc);
            final AiStrongholdSearch.SearchMemory search = new AiStrongholdSearch.SearchMemory();
            final String worldKey = AiWorldIdentity.currentWorldKey(mc);
            final Object startConnection = mc.getConnection();
            final UUID playerId = mc.player.getUUID();
            final long startDeathRevision = AiServerEvidence.deathRevision();
            BlockPos endEntrance;
            JsonObject init_JsonObject;
            int arrivalTicks;
            boolean entryAuthorized;
            final Set<UUID> init_Set = new HashSet<>();
            final Set<Integer> load_Set = new HashSet<>();
            Vec3 init_Vec3;
            boolean load_Z;
            int load_I;
            int read_I;
            AiOperation activeChild;
            final AiStronghold.ChildRunner childRunner = new AiStronghold.ChildRunner();
            AiOperation.Result init_AiOperation_Result;
            String load_Str = "";
            Vec3 bearing;
            Vec3 read_Vec3;
            EyeOfEnder init_EyeOfEnder;
            AiStronghold.EyeThrowReceipt init_co_StreamUtil = new AiStronghold.EyeThrowReceipt();
            Vec3 write_Vec3;
            AiEyeNavigation.Inner_StreamUtil init_z_StreamUtil;
            int write_I;
            int update_I;
            int throwCount;
            int flush_I;
            int forageFailures;
            int endermanHuntFailures;
            int kitSearchLeg;
            int frameApproachFailures;
            boolean read_Z;
            long read_J;
            long write_J;
            long update_J;
            BlockPos load_BlockPos;
            final AiStronghold.DarkSearchTimer pearlSearch = new AiStronghold.DarkSearchTimer();
            boolean write_Z;
            BlockPos read_BlockPos;
            long sync_J;
            int endermanSearchLeg;
            int portalNearTicks;
            int portalStanceAttempts;
            int frameStanceMoves;
            int eyePlacementFailures;
            boolean update_Z;
            int gearSupplyAttempts;
            boolean sync_Z;
            boolean flush_Z;
            int foodSupplyTrips;
            final AiOperation load_AiOperation;
            int blockedThrowViews;
            int skyClimbs;
            int surfaceClimbs;
            BlockPos portalFrame;
            int portalRoomLoadTrips;
            int corridorExplorations;
            int spawnerDisables;
            final AiGuardLayer guard;
            long inventoryFullCheckNanos;

            {
               this.bearing = AiStronghold.savedBearing;
               this.read_Vec3 = this.bearing == null ? null : mc.player.position();
               this.throwCount = AiStronghold.savedThrowCount;
               this.read_J = AiStronghold.write_J;
               this.load_BlockPos = AiStronghold.init_BlockPos;
               this.load_AiOperation = new AiOperation() {
                  @Override
                  public AiOperation.Result tick() {
                     return read();
                  }

                  @Override
                  public boolean canInterruptNow() {
                     return !sync_Z && !init_co_StreamUtil.init_Z();
                  }

                  @Override
                  public boolean requiresReconciliationAfterEmergency() {
                     return init_co_StreamUtil.init_Z();
                  }

                  @Override
                  public void cancel() {
                     sync_Z = false;
                     init_DataUtil.clear();
                  }
               };
               this.guard = new AiGuardLayer(
                  AiGuard.GuardPolicy.standard()
                     .keeping(List.of("minecraft:blaze_rod", "minecraft:blaze_powder", "minecraft:ender_pearl", "minecraft:ender_eye")),
                  "stronghold"
               );
            }

            Vec3 init_Vec3_2(BlockPos frame) {
               return Vec3.atLowerCornerOf(frame).add(0.5, 0.81, 0.5);
            }

            boolean canReachFrame(Vec3 eye, BlockPos frame) {
               return AiEndPortal.frameHit(mc, eye, frame, mc.player.blockInteractionRange() - 0.3) != null;
            }

            BlockPos init_BlockPos_6(BlockPos frame) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(frame.offset(-3, -1, -3), frame.offset(3, 2, 3))) {
                  if (AiTerrain.isLoaded(mc.level, p) && AiTerrain.isLoaded(mc.level, p.below())) {
                     VoxelShape floor = mc.level.getBlockState(p.below()).getCollisionShape(mc.level, p.below());
                     if (!floor.isEmpty()
                        && mc.level.getBlockState(p).getCollisionShape(mc.level, p).isEmpty()
                        && mc.level.getBlockState(p.above()).getCollisionShape(mc.level, p.above()).isEmpty()
                        && mc.level.getFluidState(p).isEmpty()
                        && mc.level.getFluidState(p.above()).isEmpty()
                        && !mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                        Vec3 eye = new Vec3(p.getX() + 0.5, p.getY() - 1 + floor.max(Axis.Y) + 1.62, p.getZ() + 0.5);
                        if (this.canReachFrame(eye, frame)) {
                           double d = p.distToCenterSqr(mc.player.position());
                           if (d < bestD) {
                              bestD = d;
                              best = p.immutable();
                           }
                        }
                     }
                  }
               }

               return best;
            }

            BlockPos portalFrameStance(BlockPos portal) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(portal.offset(-3, 0, -3), portal.offset(3, 0, 3))) {
                  if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                     for (Direction dir : Plane.HORIZONTAL) {
                        BlockPos frame = p.relative(dir);
                        if (mc.level.getBlockState(frame).is(Blocks.END_PORTAL_FRAME)) {
                           BlockPos s = frame.relative(dir);
                           if (!mc.level.getBlockState(s.below()).getCollisionShape(mc.level, s.below()).isEmpty()
                              && mc.level.getBlockState(s).getCollisionShape(mc.level, s).isEmpty()
                              && mc.level.getBlockState(s.above()).getCollisionShape(mc.level, s.above()).isEmpty()) {
                              double d = mc.player.distanceToSqr(s.getX() + 0.5, s.getY(), s.getZ() + 0.5);
                              if (d < bestD) {
                                 bestD = d;
                                 best = s.immutable();
                              }
                           }
                        }
                     }
                  }
               }

               return best;
            }

            BlockPos nearestPortalBlock(BlockPos stance) {
               BlockPos best = null;
               double bestD = 1.7976931348623157E308;

               for (BlockPos p : BlockPos.betweenClosed(stance.offset(-2, 0, -2), stance.offset(2, 0, 2))) {
                  if (mc.level.getBlockState(p).is(Blocks.END_PORTAL)) {
                     double d = p.distSqr(stance);
                     if (d < bestD) {
                        bestD = d;
                        best = p.immutable();
                     }
                  }
               }

               return best == null ? stance : best;
            }

            boolean isNearSurface(int blocks) {
               int top = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, mc.player.getBlockX(), mc.player.getBlockZ());
               return top - mc.player.getBlockY() <= blocks;
            }

            @Override
            public AiOperation.Result tick() {
               if (mc.player != null && mc.level != null) {
                  if (!this.worldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || this.startConnection != mc.getConnection()) {
                     return this.finish(AiOperation.Status.CANCELLED, "World or connection changed during stronghold navigation");
                  }

                  if (!mc.player.getUUID().equals(this.playerId)) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "Player changed during stronghold navigation");
                  }

                  if (mc.player.isAlive() && this.startDeathRevision == AiServerEvidence.deathRevision()) {
                     long timeOfDay = Math.floorMod(mc.level.getOverworldClockTime(), 24000);
                     this.pearlSearch.accumulate(System.nanoTime(), this.write_Z && mc.level.dimension() == Level.OVERWORLD && timeOfDay >= 12500L);
                     if (mc.level.dimension() == Level.END) {
                        this.releaseActiveChild();
                        this.init_DataUtil.clear();
                        if (this.endEntrance == null || !this.entryAuthorized) {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Entered the End outside a ready, verified portal traversal");
                        }

                        if (++this.arrivalTicks < 20) {
                           return this.running("Verifying loaded, supported End arrival");
                        }

                        String hazard = AiPortalLinks.arrivalHazard();
                        AiPortalLinks.recordLink("minecraft:overworld", this.endEntrance, "minecraft:the_end", mc.player.blockPosition(), hazard == null, this.init_JsonObject);
                        return this.finish(
                           hazard == null ? AiOperation.Status.SUCCEEDED : AiOperation.Status.UNCERTAIN,
                           hazard == null ? "Verified End arrival after " + this.throwCount + " throws" : "End arrival requires recovery: " + hazard
                        );
                     } else {
                        if (mc.level.dimension() != Level.OVERWORLD) {
                           return this.finish(AiOperation.Status.UNCERTAIN, "Stronghold navigation requires the Overworld; unexpected dimension observed");
                        }

                        if (System.nanoTime() > this.deadlineNanos) {
                           return this.finish(
                              this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN, "Out of time finding the stronghold"
                           );
                        }

                        AiOperation.Result g = this.guard
                           .tick(this.sync_Z ? this.load_AiOperation : this.activeChild, () -> {
                              if (this.sync_Z) {
                                 this.load_AiOperation.cancel();
                              } else {
                                 this.releaseActiveChild();
                              }
                           }, () -> (int)Math.max(10L, (this.deadlineNanos - System.nanoTime()) / 1000000000L));
                        if (g != null) {
                           this.init_AiOperation_Result = g;
                           if (this.sync_Z) {
                              this.sync_Z = false;
                              this.init_DataUtil.clear();
                           }

                           return g.terminal() ? this.finish(g.status(), g.message(), g) : g;
                        } else {
                           if (this.childRunner.needsReconciliation()) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "Interrupted Stronghold action needs observation before resuming");
                           }

                           if (this.sync_Z) {
                              return this.read();
                           }

                           if (this.activeChild != null) {
                              AiOperation.Result r = this.childRunner.tickChild(this.activeChild);
                              if (!r.terminal()) {
                                 return this.running(this.load_Str + ": " + r.message());
                              }

                              r = AiGuardLayer.wrapPendingTermination(this.activeChild.requiresReconciliationAfterEmergency(), r);
                              this.releaseActiveChild();
                              AiSpeedrun.log("[AI] stronghold step {}: {} - {}", r.status(), this.load_Str, AiSpeedrun.clip(r.message()));
                              this.steps
                                 .add(AiJson.objectOf("step", this.load_Str, "status", r.status().name(), "message", AiSpeedrun.clip(r.message())));
                              if (r.status() == AiOperation.Status.CANCELLED) {
                                 return this.finish(AiOperation.Status.CANCELLED, "Cancelled at " + this.load_Str, r);
                              }

                              if (r.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.finish(AiOperation.Status.UNCERTAIN, this.load_Str + ": " + r.message(), r);
                              }

                              if (this.load_Str.startsWith("forage") && r.status() != AiOperation.Status.SUCCEEDED && ++this.forageFailures >= 4) {
                                 this.sync_J = System.nanoTime() + 180000000000L;
                              }

                              if (this.load_Str.startsWith("walk to the portal frame")) {
                                 this.frameApproachFailures = r.status() == AiOperation.Status.SUCCEEDED ? 0 : this.frameApproachFailures + 1;
                              }

                              if (this.load_Str.startsWith("follow the bearing") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.read_Z = true;
                              }

                              if (this.load_Str.startsWith("forage the End kit")
                                 && r.status() != AiOperation.Status.SUCCEEDED
                                 && System.nanoTime() - this.update_J < 1000000000L) {
                                 this.write_J = System.nanoTime() + 5000000000L;
                              }

                              if (this.load_Str.startsWith("hunt") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.endermanHuntFailures++;
                              }

                              if (this.load_Str.startsWith("up to the sky") && r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.skyClimbs++;
                              }

                              if (this.load_Str.startsWith("hunt") && r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.endermanHuntFailures = 0;
                              }

                              if (this.load_Str.equals("secure travel food")) {
                                 this.foodSupplyTrips++;
                              }

                              boolean eyeSent = AiJson.getBoolean(AiRunResources.parseEvidenceJson(r.dataJson()), "sent", true);
                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.SUCCEEDED) {
                                 this.frameStanceMoves = 0;
                                 this.eyePlacementFailures = 0;
                              }

                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.FAILED && !eyeSent && ++this.eyePlacementFailures >= 12) {
                                 return this.finish(AiOperation.Status.FAILED, "No stance found that sees the frame's top; twelve clicks met another block");
                              }

                              if (this.load_Str.startsWith("put an eye")
                                 && r.status() != AiOperation.Status.SUCCEEDED
                                 && (eyeSent || r.status() == AiOperation.Status.UNCERTAIN)
                                 && ++this.load_I >= 3) {
                                 return this.finish(AiOperation.Status.UNCERTAIN, "Three frame insertions lacked confirmation; remaining Eyes are protected");
                              }

                              if (this.load_Str.startsWith("put an eye") && r.status() == AiOperation.Status.UNCERTAIN) {
                                 return this.finish(
                                    AiOperation.Status.UNCERTAIN,
                                    "Frame insertion lacked inventory confirmation; stop and reobserve before spending another Eye"
                                 );
                              }
                           }

                           return !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()
                              ? this.running("Landing")
                              : this.init_AiOperation_Result_35();
                        }
                     }
                  } else {
                     return this.finish(AiOperation.Status.FAILED, "Player died during stronghold navigation");
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Left the world");
               }
            }

            AiOperation.Result init_AiOperation_Result_35() {
               if (AiRunResources.isCampaignActive() && AiRunResources.totalSafeFoodNutrition() < 24) {
                  AiOperation cooking = AiForage.cook(180);
                  if (cooking != null) {
                     return this.startChild("cook safe travel food", cooking);
                  }

                  if (!this.seesSkyNearby(5)) {
                     return this.startChild("up to the surface for travel food", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 120)));
                  }

                  long dayTime = mc.level.getOverworldClockTime() % 24000L;
                  if (dayTime > 13000L && dayTime < 23000L && !AiSpeedrun.hasHuntable(mc, 48.0)) {
                     return this.startChild(
                        "shelter until daylight for travel food",
                        AiBurrow.burrow(
                           AiJson.objectOf("timeoutSeconds", Math.max(60L, Math.min(600L, (this.deadlineNanos - System.nanoTime()) / 1000000000L)))
                        )
                     );
                  }

                  if (this.foodSupplyTrips >= 3) {
                     return this.finish(AiOperation.Status.FAILED, "Travel food reserve could not be restored after three supply trips");
                  }

                  int goal = AiRunResources.totalSafeFoodItems() + Math.max(4, (32 - AiRunResources.totalSafeFoodNutrition() + 5) / 6);
                  return this.startChild("secure travel food", AiSpeedrun.secureFood(goal, 120));
               } else {
                  this.foodSupplyTrips = 0;
                  if (this.spawnerDisables < 2 && !AiWorldScan.findEntitiesByType("minecraft:silverfish", 12.0).isEmpty()) {
                     BlockPos spawner = AiWorldScan.findMobSpawners("minecraft:silverfish", 2)
                        .stream()
                        .filter(p -> p.distSqr(mc.player.blockPosition()) <= 144.0)
                        .findFirst()
                        .orElse(null);
                     if (spawner != null) {
                        this.spawnerDisables++;
                        return this.startChild(
                           "disable the threatening silverfish spawner",
                           AiGameOperations.blockAction(
                              "break",
                              AiJson.objectOf(
                                 "x", spawner.getX(), "y", spawner.getY(), "z", spawner.getZ(), "requireDrops", false, "timeoutSeconds", 40
                              )
                           )
                        );
                     }
                  }

                  BlockPos portal = AiWorldScan.findNearestBlock(s -> s.is(Blocks.END_PORTAL), 4);
                  if (portal != null) {
                     AiEndPortal.Inner_StreamUtil verified = AiEndPortal.inspect(portal);
                     if (verified == null) {
                        if (portal.distSqr(mc.player.blockPosition()) > 36.0) {
                           return this.startChild("inspect the portal without entering", AiGoto.travelToCompat(portal, 3.0, true, true, true, 90));
                        }

                        return this.finish(AiOperation.Status.UNCERTAIN, "End portal lacks a fully loaded inward-facing ring and nine active cells");
                     }

                     this.endEntrance = portal.immutable();
                     this.init_JsonObject = AiJson.objectOf(
                        "kind",
                        "end_portal",
                        "world",
                        this.worldKey,
                        "dimension",
                        "minecraft:overworld",
                        "center",
                        verified.center().toShortString(),
                        "inwardFrames",
                        12,
                        "filledFrames",
                        12,
                        "activeInteriorCells",
                        9,
                        "observedTick",
                        AiThreats.currentClientTick(),
                        "geometryVerified",
                        true
                     );
                  }

                  long nowKit = System.nanoTime();
                  boolean kitSearching = this.read_J != 0L && nowKit - this.read_J < 480000000000L;
                  JsonObject deficits = AiRunResources.readinessDeficits(AiRunResources.isCampaignActive());
                  if ((portal != null || kitSearching) && !deficits.isEmpty() && mc.level.dimension() == Level.OVERWORLD) {
                     long now = nowKit;
                     if (this.read_J == 0L || this.load_BlockPos == null) {
                        this.read_J = now;
                        this.load_BlockPos = portal != null ? portal : mc.player.blockPosition();
                        AiStronghold.write_J = this.read_J;
                        AiStronghold.init_BlockPos = this.load_BlockPos;
                     }

                     boolean searchTime = now - this.read_J < 480000000000L;
                     JsonObject hard = deficits.deepCopy();
                     hard.remove("fightSupplies");
                     hard.remove("crystalFallback");
                     if (searchTime || !hard.isEmpty() && this.gearSupplyAttempts++ < 10) {
                        if (AiRunResources.isCampaignActive() && !AiRunResources.missingMandatoryGear().isEmpty()) {
                           Map<String, Integer> missing = AiRunResources.missingMandatoryGear();
                           int pickGoal = missing.getOrDefault("minecraft:diamond_pickaxe", 1);
                           missing.remove("minecraft:diamond_pickaxe");
                           return this.startChild(
                              "replace worn End equipment",
                              AiProgression.obtain(
                                 AiJson.objectOf("kit", AiJson.GSON.toJsonTree(missing), "kitPlanks", 8, "timeoutSeconds", 300),
                                 "minecraft:diamond_pickaxe",
                                 pickGoal
                              )
                           );
                        }

                        if (AiRunResources.isCampaignActive() && !AiRunResources.isMandatoryArmorEquipped()) {
                           return this.startChild("equip End armor", AiEquip.create(AiJson.objectOf("what", "armor")));
                        }

                        if (mc.player.getFoodData().getFoodLevel() < 18 && AiSurvival.bestFoodSlot(mc.player.getInventory()) >= 0) {
                           return this.startChild("eat before entering the End", AiSurvival.eat(AiJson.objectOf("food", 20, "timeoutSeconds", 20)));
                        }

                        if (mc.player.getHealth() < 16.0F && mc.player.getFoodData().getFoodLevel() >= 18) {
                           return this.startChild("heal before entering the End", AiCover.create(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90)));
                        }

                        if (AiRunResources.totalSafeFoodNutrition() < 32) {
                           AiOperation cooking = AiForage.cook(180);
                           if (cooking != null) {
                              return this.startChild("cook safe End food", cooking);
                           }

                           int foodGoal = AiRunResources.totalSafeFoodItems() + Math.max(4, (32 - AiRunResources.totalSafeFoodNutrition() + 5) / 6);
                           return this.startChild("secure End food", AiSpeedrun.secureFood(foodGoal, 120));
                        }

                        if (AiRunResources.spareBuildingBlockCount() < 32) {
                           return this.startChild(
                              "secure End building blocks",
                              AiObtain.obtain(AiJson.objectOf("item", "minecraft:cobblestone", "count", 32, "timeoutSeconds", 120))
                           );
                        }

                        if (deficits.has("fallbackWeapon")) {
                           return this.startChild(
                              "secure a fallback weapon", AiObtain.obtain(AiJson.objectOf("item", "minecraft:iron_sword", "count", 1, "timeoutSeconds", 120))
                           );
                        }

                        if (deficits.has("waterBuckets")) {
                           return this.startChild("refill both End water buckets", AiObsidian.init(2, 120));
                        }
                     }

                     if (searchTime) {
                        AiOperation errand = now < this.write_J ? null : AiForage.forageFood(48.0);
                        if (errand != null) {
                           return this.startChild("forage the End kit before the portal (" + AiForage.kitSummary() + ")", errand);
                        }

                        if (!this.seesSkyNearby(5)) {
                           return this.startChild("up to the surface for the End kit", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 120)));
                        }

                        double a = this.kitSearchLeg++ * 2.399;
                        int x = this.load_BlockPos.getX() + (int)Math.round(Math.cos(a) * 72.0);
                        int z = this.load_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 72.0);
                        return this.startChild(
                           "look for the End kit round the portal (" + AiForage.kitSummary() + ", leg " + this.kitSearchLeg + ")",
                           AiGoto.travelToCompat(
                              new BlockPos(x, mc.player.getBlockY(), z),
                              4.0,
                              true,
                              true,
                              true,
                              60,
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              true,
                              2.0,
                              new AiNetherRun.RadiusGoal(x, z, 6),
                              AiGoto.GotoOptions.DEFAULT
                           )
                        );
                     }

                     if (!hard.isEmpty()) {
                        return this.finish(AiOperation.Status.FAILED, "End entry blocked by unresolved supplies: " + hard);
                     }

                     if (!this.update_Z) {
                        this.update_Z = true;
                        AiSpeedrun.log("[AI] stronghold: End kit search over ({}); entering with melee and towers", AiForage.kitSummary());
                     }
                  }

                  boolean portalNear = portal != null
                     && Math.hypot(mc.player.getX() - (portal.getX() + 0.5), mc.player.getZ() - (portal.getZ() + 0.5)) <= 6.0
                     && Math.abs(mc.player.getY() - portal.getY()) <= 4.0;
                  if (portal != null && !portalNear) {
                     this.portalNearTicks = 0;
                  }

                  if (portalNear) {
                     AiEndPortal.Inner_StreamUtil openRing = AiEndPortal.inspect(portal);
                     if (openRing == null
                        || !openRing.active()
                        || Math.abs(openRing.center().getX() - portal.getX()) > 1
                        || Math.abs(openRing.center().getZ() - portal.getZ()) > 1
                        || openRing.center().getY() != portal.getY()) {
                        return this.finish(
                           AiOperation.Status.UNCERTAIN, "End entry requires a complete inward-facing frame ring and its nine active interior cells"
                        );
                     }
                  }

                  if (portal != null && portalNear && this.portalNearTicks++ >= 1) {
                     BlockPos stance = this.portalFrameStance(portal);
                     if (stance == null
                        || this.portalStanceAttempts >= 3
                        || !(Math.hypot(mc.player.getX() - (stance.getX() + 0.5), mc.player.getZ() - (stance.getZ() + 0.5)) > 0.8)
                           && !(Math.abs(mc.player.getY() - stance.getY()) > 0.6)) {
                        BlockPos into = stance == null ? portal : this.nearestPortalBlock(stance);
                        return this.startChild("onto the end portal at " + into.toShortString(), AiEndRun.enterPortal(mc, into));
                     } else {
                        this.portalStanceAttempts++;
                        return this.startChild(
                           "to the portal frame at " + stance.toShortString(),
                           AiGoto.travelToCompat(
                              stance,
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
                  } else {
                     if (portal != null) {
                        return this.startChild(
                           "into the end portal at " + portal.toShortString(),
                           AiGoto.travelToCompat(
                              portal,
                              0.6,
                              true,
                              true,
                              true,
                              120,
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              true,
                              2.0,
                              null,
                              AiGoto.GotoOptions.DEFAULT
                           )
                        );
                     }

                     BlockPos frame = AiWorldScan.findNearestBlock(s -> s.is(Blocks.END_PORTAL_FRAME), 12);
                     if (frame != null) {
                        if (this.portalFrame == null) {
                           AiSpeedrun.log("[AI] stronghold: portal frame seen at {} after {} throws", frame.toShortString(), this.throwCount);
                        }

                        this.portalFrame = frame;
                        AiEndPortal.Inner_StreamUtil ring = AiEndPortal.ringAt(frame);
                        if (ring != null) {
                           this.portalRoomLoadTrips = 0;
                           List<BlockPos> empty = ring.empty();
                           int eyes = AiCraft.countItem("minecraft:ender_eye");
                           if (!empty.isEmpty() && eyes < empty.size()) {
                              int canMake = Math.min(
                                 AiCraft.countItem("minecraft:ender_pearl"), AiCraft.countItem("minecraft:blaze_powder") + 2 * AiCraft.countItem("minecraft:blaze_rod")
                              );
                              if (canMake > 0) {
                                 return this.init_AiOperation_Result(Math.min(canMake, empty.size() - eyes));
                              }

                              this.write_Z = true;
                              if (this.pearlSearch.darkSearchExceeded(600000000000L)) {
                                 return this.load_AiOperation_Result(empty.size() - eyes);
                              }

                              if (AiCraft.countItem("minecraft:blaze_powder") + AiCraft.countItem("minecraft:blaze_rod") > 0
                                 && this.endermanHuntFailures < 8
                                 && AiForage.findEntity(mc, "minecraft:enderman", 48.0) != null) {
                                 return this.startChild("hunt an enderman for a pearl (" + empty.size() + " frames empty, " + eyes + " eyes)", AiForage.load(48.0));
                              }

                              if (AiCraft.countItem("minecraft:blaze_powder") + AiCraft.countItem("minecraft:blaze_rod") <= 0 || this.pearlSearch.darkSearchExceeded(600000000000L)) {
                                 return this.finish(AiOperation.Status.FAILED, empty.size() + " frames are empty and only " + eyes + " eyes can be made");
                              }

                              if (!this.seesSkyNearby(5)) {
                                 return this.startChild("up to the surface for endermen", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 120)));
                              }

                              double a = this.endermanSearchLeg++ * 2.399;
                              int x = frame.getX() + (int)Math.round(Math.cos(a) * 80.0);
                              int z = frame.getZ() + (int)Math.round(Math.sin(a) * 80.0);
                              return this.startChild(
                                 "look for endermen near the room (" + empty.size() + " frames empty, " + eyes + " eyes)",
                                 AiGoto.travelToCompat(
                                    new BlockPos(x, mc.player.getBlockY(), z),
                                    4.0,
                                    true,
                                    true,
                                    true,
                                    60,
                                    autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                    true,
                                    2.0,
                                    new AiNetherRun.RadiusGoal(x, z, 6),
                                    AiGoto.GotoOptions.DEFAULT
                                 )
                              );
                           } else {
                              this.write_Z = false;
                              BlockPos target = empty.isEmpty() ? frame : empty.get(0);
                              if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(target)) > 16.0) {
                                 if (this.frameApproachFailures >= 12) {
                                    return this.finish(AiOperation.Status.FAILED, "No supported route into the observed portal room after bounded approaches");
                                 }

                                 if (this.frameApproachFailures >= 4 && this.frameApproachFailures % 4 == 0) {
                                    this.frameApproachFailures++;
                                    BlockPos feet = mc.player.blockPosition();
                                    BlockPos escape = null;

                                    for (BlockPos p : BlockPos.betweenClosed(feet.offset(-3, 0, -3), feet.offset(3, 1, 3))) {
                                       if (!(p.distSqr(feet) < 4.0)
                                          && AiTerrain.isLoaded(mc.level, p)
                                          && AiTerrain.isLoaded(mc.level, p.below())
                                          && AiCombat.isSafeStep(mc, p)
                                          && (escape == null || p.distSqr(target) < escape.distSqr(target))) {
                                          escape = p.immutable();
                                       }
                                    }

                                    return escape != null
                                       ? this.startChild(
                                          "leave the pocket along supported terrain", AiGoto.travelToCompat(escape, 1.0, true, true, false, 30)
                                       )
                                       : this.finish(
                                          AiOperation.Status.UNCERTAIN, "Portal approach is enclosed; no observed supported escape, so no blind downward dig"
                                       );
                                 } else {
                                    return this.startChild(
                                       "walk to the portal frame at " + target.toShortString(),
                                       AiGoto.travelToCompat(
                                          target,
                                          2.5,
                                          true,
                                          true,
                                          true,
                                          300,
                                          this.frameApproachFailures % 2 == 1
                                             ? autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST
                                             : autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                          true,
                                          2.0,
                                          null,
                                          AiGoto.GotoOptions.DEFAULT
                                       )
                                    );
                                 }
                              } else {
                                 if (empty.isEmpty()) {
                                    return !ring.active() && ++this.read_I > 100
                                       ? this.finish(AiOperation.Status.UNCERTAIN, "Twelve filled inward-facing frames did not produce nine active portal blocks")
                                       : this.running("Waiting for the portal to open");
                                 }

                                 if (!this.isHolding("minecraft:ender_eye")) {
                                    return this.startChild("hold an eye", AiEquip.create(AiJson.objectOf("what", "minecraft:ender_eye")));
                                 }

                                 if (!this.canReachFrame(mc.player.getEyePosition(), target) && mc.player.onGround() && this.frameStanceMoves < 8) {
                                    BlockPos stance = this.init_BlockPos_6(target);
                                    if (stance != null) {
                                       this.frameStanceMoves++;
                                       return this.startChild(
                                          "to a stance over the frame at " + target.toShortString(),
                                          AiGoto.travelToCompat(
                                             stance,
                                             0.4,
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

                                 return this.startChild("put an eye in the frame at " + target.toShortString(), AiEndPortal.placeEye(target));
                              }
                           }
                        } else {
                           return this.portalRoomLoadTrips++ < 3 && mc.player.blockPosition().distSqr(frame) > 36.0
                              ? this.startChild("load the complete portal room", AiGoto.travelToCompat(frame, 3.0, true, true, true, 120))
                              : this.finish(AiOperation.Status.UNCERTAIN, "Observed frame is not a complete loaded inward-facing twelve-frame ring");
                        }
                     } else {
                        if (mc.level.dimension() == Level.OVERWORLD
                           && AiCraft.countItem("minecraft:ender_eye") + AiCraft.countItem("minecraft:ender_pearl") < 13
                           && this.endermanHuntFailures < 6
                           && AiForage.findEntity(mc, "minecraft:enderman", 40.0) != null) {
                           return this.startChild("hunt an enderman for a pearl", AiForage.load(40.0));
                        }

                        if (this.forageFailures >= 4 && System.nanoTime() > this.sync_J) {
                           this.forageFailures = 0;
                        }

                        if (this.forageFailures < 4 && mc.level.dimension() == Level.OVERWORLD) {
                           AiOperation errand = AiForage.forageFood(32.0);
                           if (errand != null) {
                              return this.startChild("forage for the End (" + AiForage.kitSummary() + ")", errand);
                           }
                        }

                        AiEyeNavigation.Inner_DataUtil located = this.navigation.getLocated();
                        if (located != null && (located.uncertainty() >= 160.0 || this.corridorExplorations >= 2)) {
                           located = null;
                        }

                        int makeable = Math.min(AiCraft.countItem("minecraft:ender_pearl"), AiCraft.countItem("minecraft:blaze_powder") + 2 * AiCraft.countItem("minecraft:blaze_rod"));
                        boolean spareEye = AiCraft.countItem("minecraft:ender_eye") > 12 || makeable > 0;
                        if (this.bearing == null || this.init_D_3() > 640.0 && located == null && spareEye) {
                           if (AiCraft.countItem("minecraft:ender_eye") <= 12) {
                              int canMake = makeable;
                              if (canMake == 0) {
                                 this.write_Z = true;
                                 if (this.pearlSearch.darkSearchExceeded(600000000000L)) {
                                    return this.load_AiOperation_Result(Math.max(1, 16 - AiCraft.countItem("minecraft:ender_eye")));
                                 }

                                 if (AiCraft.countItem("minecraft:blaze_powder") + AiCraft.countItem("minecraft:blaze_rod") > 0 && mc.level.dimension() == Level.OVERWORLD) {
                                    if (this.read_BlockPos == null) {
                                       this.read_BlockPos = mc.player.blockPosition();
                                    }

                                    if (!this.pearlSearch.darkSearchExceeded(600000000000L)) {
                                       if (!this.seesSkyNearby(5)) {
                                          return this.startChild(
                                             "up to the surface for endermen", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 120))
                                          );
                                       }

                                       double a = this.endermanSearchLeg++ * 2.399;
                                       int x = this.read_BlockPos.getX() + (int)Math.round(Math.cos(a) * 80.0);
                                       int z = this.read_BlockPos.getZ() + (int)Math.round(Math.sin(a) * 80.0);
                                       return this.startChild(
                                          "look for endermen for the eyes (no pearls)",
                                          AiGoto.travelToCompat(
                                             new BlockPos(x, mc.player.getBlockY(), z),
                                             4.0,
                                             true,
                                             true,
                                             true,
                                             60,
                                             autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                                             true,
                                             2.0,
                                             new AiNetherRun.RadiusGoal(x, z, 6),
                                             AiGoto.GotoOptions.DEFAULT
                                          )
                                       );
                                    }
                                 }

                                 return this.finish(AiOperation.Status.FAILED, "No eye of ender to throw and none to make");
                              }

                              if (canMake > 0) {
                                 return this.init_AiOperation_Result(Math.min(canMake, Math.max(1, 16 - AiCraft.countItem("minecraft:ender_eye"))));
                              }
                           }

                           if (AiCraft.countItem("minecraft:ender_eye") <= 12) {
                              return this.finish(AiOperation.Status.FAILED, "Navigation Eyes exhausted; the twelve activation Eyes remain reserved");
                           } else {
                              this.write_Z = false;
                              if (!mc.level.canSeeSky(BlockPos.containing(mc.player.getEyePosition())) && this.skyClimbs < 2 && this.isNearSurface(24)) {
                                 return this.startChild("up to the sky for a throw", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 120)));
                              } else if (this.blockedThrowViews >= 3 && this.bearing != null) {
                                 this.blockedThrowViews = 0;
                                 Vec3 at = mc.player.position();
                                 int tx = (int)Math.floor(at.x + this.bearing.x * 12.0);
                                 int tz = (int)Math.floor(at.z + this.bearing.z * 12.0);
                                 return this.startChild(
                                    "step out from under the trees",
                                    AiNetherRun.init(AiGoto.createPersistentOpCompat(new BlockPos(tx, (int)at.y, tz), new AiNetherRun.RadiusGoal(tx, tz, 3), 30))
                                 );
                              } else {
                                 return !this.isHolding("minecraft:ender_eye")
                                    ? this.startChild("hold an eye", AiEquip.create(AiJson.objectOf("what", "minecraft:ender_eye")))
                                    : this.startEyeThrow();
                              }
                           }
                        } else {
                           if (!this.isNearSurface(12) && this.surfaceClimbs < 3) {
                              this.surfaceClimbs++;
                              return this.startChild("up to the surface for the walk", autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", 180)));
                           }

                           Vec3 at = mc.player.position();
                           if (this.init_D_3() < 32.0 && this.navigation.getLocated() == null && this.throwCount < 5) {
                              Vec3 side = this.navigation.sidestepTarget(at, this.bearing);
                              if (side != null) {
                                 this.load_Z = true;
                                 this.read_Vec3 = null;
                                 int sx = (int)Math.floor(side.x);
                                 int sz = (int)Math.floor(side.z);
                                 return this.startChild(
                                    "establish a lateral Eye baseline",
                                    AiNetherRun.init(AiGoto.createPersistentOpCompat(new BlockPos(sx, (int)at.y, sz), new AiNetherRun.RadiusGoal(sx, sz, 8), 90))
                                 );
                              }
                           }

                           AiEyeNavigation.Inner_DataUtil estimate = this.navigation.getLocated();
                           if (estimate != null && estimate.uncertainty() < 160.0) {
                              Vec3 toward = estimate.position().subtract(at);
                              double length = Math.hypot(toward.x, toward.z);
                              if (length < Math.max(64.0, estimate.uncertainty()) && this.corridorExplorations < 2) {
                                 this.corridorExplorations++;
                                 return this.startChild(
                                    "explore the observed stronghold corridors",
                                    AiStrongholdSearch.search(
                                       estimate.position(),
                                       (int)Math.max(30L, Math.min(360L, (this.deadlineNanos - System.nanoTime()) / 1000000000L)),
                                       this.search
                                    )
                                 );
                              }

                              if (length > 24.0) {
                                 this.bearing = new Vec3(toward.x / length, 0.0, toward.z / length);
                              }
                           }

                           int tx = (int)Math.floor(at.x + this.bearing.x * 160.0);
                           int tz = (int)Math.floor(at.z + this.bearing.z * 160.0);
                           AiGoto.GotoOptions dry = new AiGoto.GotoOptions(true, true, 3, !this.read_Z, true, true);
                           this.read_Z = false;
                           return this.startChild(
                              "follow the bearing toward " + tx + ", " + tz,
                              AiNetherRun.init(AiGoto.createPersistentOpCompat(new BlockPos(tx, (int)at.y, tz), new AiNetherRun.RadiusGoal(tx, tz, 12), 120, dry))
                           );
                        }
                     }
                  }
               }
            }

            double init_D_3() {
               return this.read_Vec3 == null ? 1.7976931348623157E308 : Math.hypot(mc.player.getX() - this.read_Vec3.x, mc.player.getZ() - this.read_Vec3.z);
            }

            AiOperation.Result startEyeThrow() {
               if (this.init_co_StreamUtil.init_Z()) {
                  return this.finish(AiOperation.Status.UNCERTAIN, "The previous Eye throw still needs a server receipt");
               }

               this.init_co_StreamUtil = new AiStronghold.EyeThrowReceipt();
               this.sync_Z = true;
               this.flush_Z = false;
               this.update_I = 0;
               this.init_EyeOfEnder = null;
               this.write_Vec3 = null;
               this.init_Vec3 = mc.player.position();
               this.init_Set.clear();
               this.load_Set.clear();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof EyeOfEnder) {
                     this.init_Set.add(entity.getUUID());
                  }

                  if (entity instanceof ItemEntity item && item.getItem().is(Items.ENDER_EYE)) {
                     this.load_Set.add(item.getId());
                  }
               }

               return this.running("Throwing an eye of ender");
            }

            AiOperation.Result read() {
               this.update_I++;
               if (!this.flush_Z) {
                  float pitch = AiStronghold.EYE_THROW_PITCHES[Math.min(AiStronghold.EYE_THROW_PITCHES.length - 1, (this.update_I - 1) / 6)];
                  this.init_DataUtil.setTargetRotation(new autismclient.util.RotationMath.Rotation(mc.player.getYRot(), pitch));
                  this.init_DataUtil.tick();
                  boolean clear = mc.hitResult == null
                     || mc.hitResult.getType() != Type.BLOCK
                     || !mc.level.canSeeSky(BlockPos.containing(mc.player.getEyePosition()))
                        && mc.hitResult instanceof BlockHitResult blockHit
                        && !mc.level.getBlockState(blockHit.getBlockPos()).is(Blocks.END_PORTAL_FRAME);
                  if (this.update_I >= 4 && clear && Math.abs(mc.player.getXRot() - pitch) < 2.0F) {
                     if (!this.isHolding("minecraft:ender_eye")) {
                        this.sync_Z = false;
                        this.init_DataUtil.clear();
                        return this.running("Reequipping the Eye before its throw");
                     }

                     this.init_Vec3 = mc.player.position();
                     this.init_Set.clear();

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof EyeOfEnder) {
                           this.init_Set.add(entity.getUUID());
                        }
                     }

                     BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                     this.init_co_StreamUtil
                        .markSent(
                           AiCraft.countItem("minecraft:ender_eye"),
                           mc.player.getMainHandItem().getCount(),
                           AiServerEvidence.inventoryRevision(),
                           AiServerEvidence.containerReplyCount(mc.player.inventoryMenu.containerId),
                           prediction.currentSequence()
                        );
                     mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                     this.init_co_StreamUtil.noteSentSequence(prediction.currentSequence());
                     Compat263.swing(mc.player, InteractionHand.MAIN_HAND);
                     this.throwCount++;
                     this.flush_Z = true;
                     this.update_I = 6;
                     AiSpeedrun.log("[AI] stronghold: throw {} at pitch {}", this.throwCount, pitch);
                     return this.running("Throwing an eye of ender");
                  } else if (this.update_I > AiStronghold.EYE_THROW_PITCHES.length * 6) {
                     this.sync_Z = false;
                     this.init_DataUtil.clear();
                     this.blockedThrowViews++;
                     AiSpeedrun.log("[AI] stronghold: no clear view up to throw from {}", mc.player.blockPosition().toShortString());
                     return this.running("No clear view to throw");
                  } else {
                     return this.running("Looking up for a clear throw");
                  }
               } else {
                  boolean firstObservation = this.init_EyeOfEnder == null;
                  ArrayList<EyeOfEnder> candidates = new ArrayList<>();
                  if (firstObservation) {
                     for (Entity e : mc.level.entitiesForRendering()) {
                        if (e instanceof EyeOfEnder found
                           && !this.init_Set.contains(found.getUUID())
                           && found.isAlive()
                           && found.position().distanceToSqr(this.init_Vec3) < 256.0) {
                           candidates.add(found);
                        }
                     }
                  } else {
                     candidates.add(this.init_EyeOfEnder);
                  }

                  AiStronghold.ThrowOutcome outcome = this.checkThrowReceipt(candidates.isEmpty() ? null : candidates.getFirst().getUUID());

                  for (int i = 1; i < candidates.size(); i++) {
                     outcome = this.checkThrowReceipt(candidates.get(i).getUUID());
                  }

                  if (outcome == AiStronghold.ThrowOutcome.AMBIGUOUS) {
                     return this.finish(AiOperation.Status.UNCERTAIN, "The Eye throw has ambiguous inventory or entity evidence; reobserve before another throw");
                  }

                  if (firstObservation) {
                     if (!candidates.isEmpty()) {
                        this.init_EyeOfEnder = candidates.getFirst();
                     }

                     if (this.init_EyeOfEnder == null) {
                        if (this.update_I - 6 > 120) {
                           if (this.init_co_StreamUtil.init_Z()) {
                              return this.finish(AiOperation.Status.UNCERTAIN, "The sent Eye throw lacks a server receipt; reobserve before spending another Eye");
                           }

                           if (outcome == AiStronghold.ThrowOutcome.CONSUMED) {
                              this.flush_I++;
                           }

                           return this.init_AiOperation_Result_14(
                              outcome == AiStronghold.ThrowOutcome.CONSUMED
                                 ? "One Eye was consumed, but no usable flight was observed; retry navigation from the receipt"
                                 : "The server acknowledged an unchanged Eye inventory after the use; retry navigation from the receipt"
                           );
                        } else {
                           return this.running("Waiting for the Eye flight and server receipt");
                        }
                     } else {
                        this.write_Vec3 = this.init_EyeOfEnder.position();
                        this.init_z_StreamUtil = new AiEyeNavigation.Inner_StreamUtil(this.write_Vec3);
                        this.write_I = this.update_I;
                        return this.running("Watching the eye");
                     }
                  } else {
                     Vec3 now = this.init_EyeOfEnder.position();
                     double moved = Math.hypot(now.x - this.write_Vec3.x, now.z - this.write_Vec3.z);
                     Vec3 fitted = this.init_z_StreamUtil.addSample(now);
                     if (fitted == null
                        && (!(moved >= 4.0) || this.update_I - this.write_I <= 40)
                        && this.init_EyeOfEnder.isAlive()
                        && this.update_I - this.write_I <= 70) {
                        return this.running("Watching the eye (" + String.format("%.1f", moved) + " blocks)");
                     }

                     if (this.init_co_StreamUtil.init_Z()) {
                        return this.update_I - 6 > 120
                           ? this.finish(AiOperation.Status.UNCERTAIN, "The observed Eye flight lacks its inventory receipt; reobserve before another throw")
                           : this.running("Waiting for the Eye throw inventory receipt");
                     }

                     if (moved >= 1.0) {
                        Vec3 line = fitted != null ? fitted : (moved >= 4.0 ? this.init_z_StreamUtil.fitLine() : null);
                        this.bearing = line != null ? line : new Vec3((now.x - this.write_Vec3.x) / moved, 0.0, (now.z - this.write_Vec3.z) / moved);
                        this.read_Vec3 = mc.player.position();
                        AiStronghold.savedBearing = this.bearing;
                        AiStronghold.savedThrowCount = this.throwCount;
                        this.navigation
                           .addThrow(
                              line != null ? this.init_z_StreamUtil.meanPoint() : this.write_Vec3,
                              line != null ? line.scale(Math.max(4.0, moved)) : now.subtract(this.write_Vec3)
                           );
                        AiRunTrace.emitServiceEvent(
                           "eye_bearing",
                           AiJson.objectOf(
                              "throw",
                              this.throwCount,
                              "origin",
                              this.init_Vec3.toString(),
                              "bearing",
                              this.bearing.toString(),
                              "navigation",
                              this.navigation.statusJson()
                           )
                        );
                        AiSpeedrun.log(
                           "[AI] stronghold: throw {} from {} points {} (yaw {})",
                           this.throwCount,
                           mc.player.blockPosition().toShortString(),
                           String.format("%.3f, %.3f", this.bearing.x, this.bearing.z),
                           Math.round(Math.toDegrees(Math.atan2(-this.bearing.x, this.bearing.z)))
                        );
                     }

                     this.sync_Z = false;
                     this.init_DataUtil.clear();
                     this.activeChild = this.init_AiOperation_6();
                     this.load_Str = "pick the eye back up";
                     return this.running("Bearing read");
                  }
               }
            }

            AiStronghold.ThrowOutcome checkThrowReceipt(UUID identity) {
               int container = mc.player.inventoryMenu.containerId;
               AiStronghold.ThrowOutcome outcome = this.init_co_StreamUtil
                  .observe(
                     AiCraft.countItem("minecraft:ender_eye"),
                     AiServerEvidence.inventoryRevision(),
                     AiServerEvidence.minItemCountAfterReply(container, this.init_co_StreamUtil.repliesBefore, "minecraft:ender_eye"),
                     AiServerEvidence.containerReplyCount(container),
                     autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(mc.level, this.init_co_StreamUtil.sentSequence),
                     identity,
                     this.update_I - 6
                  );
               return outcome == AiStronghold.ThrowOutcome.WAITING && this.init_co_StreamUtil.repliesAtAcknowledgement >= 0L
                  ? this.init_co_StreamUtil.classifyAfterAcknowledgement(AiServerEvidence.minItemCountAfterReply(container, this.init_co_StreamUtil.repliesAtAcknowledgement, "minecraft:ender_eye"), this.update_I - 6)
                  : outcome;
            }

            AiOperation.Result init_AiOperation_Result_14(String message) {
               AiOperation.Result stopped = this.finish(AiOperation.Status.FAILED, message);
               return new AiOperation.Result(
                  stopped.status(),
                  stopped.message(),
                  stopped.dataJson(),
                  stopped.evidence(),
                  stopped.status() == AiOperation.Status.UNCERTAIN ? AiFailure.UNKNOWN : AiFailure.TARGET_NOT_FOUND
               );
            }

            AiOperation init_AiOperation_6() {
               final int before = AiCraft.countItem("minecraft:ender_eye");
               return AiNetherRun.delayThen(
                  20,
                  new AiOperation() {
                     AiOperation init_AiOperation;
                     int init_I;

                     @Override
                     public AiOperation.Result tick() {
                        if (AiCraft.countItem("minecraft:ender_eye") > before) {
                           return AiGameOperations.result(
                              AiOperation.Status.SUCCEEDED, "An Eye pickup is confirmed in inventory", AiJson.objectOf("recovered", true)
                           );
                        }

                        if (this.init_AiOperation == null) {
                           boolean inAir = false;
                           inAir = init_EyeOfEnder != null && init_EyeOfEnder.isAlive() && mc.level.getEntity(init_EyeOfEnder.getId()) == init_EyeOfEnder;
                           if (inAir && ++this.init_I < 120) {
                              return AiGameOperations.result(AiOperation.Status.RUNNING, "Waiting for the eye to come down", new JsonObject());
                           }

                           boolean dropped = false;

                           for (Entity e : mc.level.entitiesForRendering()) {
                              if (e instanceof ItemEntity item
                                 && item.getItem().is(Items.ENDER_EYE)
                                 && !load_Set.contains(item.getId())
                                 && item.distanceToSqr(mc.player) < 576.0
                                 && (init_EyeOfEnder == null || Math.hypot(item.getX() - init_EyeOfEnder.getX(), item.getZ() - init_EyeOfEnder.getZ()) < 5.0)) {
                                 dropped = true;
                                 break;
                              }
                           }

                           if (!dropped) {
                              flush_I++;
                              return AiGameOperations.result(
                                 AiOperation.Status.SUCCEEDED,
                                 "Eye not recovered; shatter or lost drop remains unconfirmed",
                                 AiJson.objectOf("recovered", false)
                              );
                           }

                           this.init_AiOperation = AiGameplay.write(
                              AiJson.objectOf("item", "minecraft:ender_eye", "count", before + 1, "radius", 24, "timeoutSeconds", 20)
                           );
                        }

                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (r.terminal() && AiCraft.countItem("minecraft:ender_eye") <= before) {
                           flush_I++;
                        }

                        return r;
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return this.init_AiOperation == null || this.init_AiOperation.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                        }
                     }
                  }
               );
            }

            AiOperation.Result init_AiOperation_Result(int count) {
               if (mc.player.getInventory().getFreeSlot() < 0 && AiCraft.countItem("minecraft:ender_eye") == 0 && System.nanoTime() - this.inventoryFullCheckNanos > 30000000000L) {
                  this.inventoryFullCheckNanos = System.nanoTime();
                  JsonArray keep = new JsonArray();

                  for (String id : List.of(
                     "minecraft:ender_eye",
                     "minecraft:ender_pearl",
                     "minecraft:blaze_rod",
                     "minecraft:blaze_powder",
                     "minecraft:bow",
                     "minecraft:arrow",
                     "minecraft:water_bucket",
                     "minecraft:bucket",
                     "minecraft:flint_and_steel",
                     "minecraft:string",
                     "minecraft:white_bed",
                     "minecraft:white_wool"
                  )) {
                     keep.add(id);
                  }

                  return this.startChild("tidy the bag for the eyes", AiTidy.create(AiJson.objectOf("action", "drop", "freeSlots", 4, "keep", keep)));
               } else {
                  return this.startChild(
                     "craft " + count + " eyes of ender",
                     AiCraft.craft(AiJson.objectOf("item", "minecraft:ender_eye", "count", AiCraft.countItem("minecraft:ender_eye") + count, "timeoutSeconds", 60))
                  );
               }
            }

            boolean seesSkyNearby(int radius) {
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

            boolean isHolding(String id) {
               return BuiltInRegistries.ITEM.getKey(mc.player.getMainHandItem().getItem()).toString().equals(id);
            }

            AiOperation.Result startChild(String label, AiOperation op) {
               this.entryAuthorized = label.startsWith("into the end portal") || label.startsWith("onto the end portal");
               this.load_Str = label;
               this.update_J = System.nanoTime();
               AiSpeedrun.log("[AI] stronghold: {}", label);
               this.activeChild = AiRunDiagnostics.trace("stronghold", label, op);
               return this.running(label);
            }

            JsonObject evidence() {
               return AiJson.objectOf(
                  "throws",
                  this.throwCount,
                  "eyesLost",
                  this.flush_I,
                  "eyes",
                  AiCraft.countItem("minecraft:ender_eye"),
                  "bearing",
                  this.bearing == null ? "" : String.format("%.3f,%.3f", this.bearing.x, this.bearing.z),
                  "frame",
                  this.portalFrame == null ? "" : this.portalFrame.toShortString(),
                  "navigation",
                  this.navigation.statusJson(),
                  "endEntrance",
                  this.endEntrance == null ? "" : this.endEntrance.toShortString(),
                  "entryAuthorized",
                  this.entryAuthorized,
                  "arrivalTicks",
                  this.arrivalTicks,
                  "resources",
                  AiRunResources.eyeBudget(16, 12, 0).data(),
                  "pearlSearchDarkSeconds",
                  this.pearlSearch.darkSearchTotal() / 1000000000L,
                  "eyeThrow",
                  this.init_co_StreamUtil.evidence(),
                  "steps",
                  this.steps
               );
            }

            AiOperation.Result load_AiOperation_Result(int missing) {
               AiOperation.Result stopped = this.finish(
                  AiOperation.Status.FAILED, "The bounded dark-time pearl search is exhausted; change the pearl acquisition route"
               );
               JsonObject evidence = AiRunResources.parseEvidenceJson(stopped.dataJson());
               evidence.addProperty("pearlRouteChange", AiRunResources.isCampaignActive() && AiNetherRun.load_Z());
               evidence.addProperty("pearlsNeeded", missing);
               return new AiOperation.Result(stopped.status(), stopped.message(), evidence.toString(), stopped.evidence(), AiFailure.MISSING_MATERIALS);
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.evidence());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finish(status, message, null);
            }

            AiOperation.Result finish(AiOperation.Status status, String message, AiOperation.Result child) {
               boolean pending = this.requiresReconciliationAfterEmergency();
               this.childRunner.init_V(this.activeChild);
               this.childRunner.notePending(pending, child);
               this.childRunner.notePending(this.guard.requiresReconciliation(), this.init_AiOperation_Result);
               AiOperation.Result result = this.childRunner.result(status, message, this.evidence(), child);
               this.releaseActiveChild();
               this.guard.cancel();
               this.init_DataUtil.clear();
               return result;
            }

            void releaseActiveChild() {
               if (this.activeChild != null) {
                  this.childRunner.cancelChild(this.activeChild);
                  this.activeChild = null;
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.childRunner.needsReconciliation()
                  && !this.sync_Z
                  && !this.init_co_StreamUtil.init_Z()
                  && (this.activeChild == null || this.activeChild.canInterruptNow())
                  && this.guard.canInterruptNow();
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.childRunner.needsReconciliation()
                  || this.init_co_StreamUtil.init_Z()
                  || this.activeChild != null && this.activeChild.requiresReconciliationAfterEmergency()
                  || this.guard.requiresReconciliation();
            }

            @Override
            public void cancel() {
               this.childRunner.notePending(this.guard.requiresReconciliation(), this.init_AiOperation_Result);
               this.releaseActiveChild();
               this.guard.cancel();
               this.init_DataUtil.clear();
            }
         };
      } else {
         throw new IllegalArgumentException("The stronghold run needs a connected living player");
      }
   }

   enum ThrowOutcome {
      WAITING,
      CONSUMED,
      REJECTED,
      AMBIGUOUS;
   }

   static final class DarkSearchTimer {
      private long nextAt = -9223372036854775808L;
      private long darkSearchNanos;
      private boolean darkSearch;

      void accumulate(long now, boolean darkSearch) {
         if (this.nextAt == -9223372036854775808L || now >= this.nextAt) {
            if (this.nextAt != -9223372036854775808L && this.darkSearch) {
               this.darkSearchNanos = this.darkSearchNanos + (now - this.nextAt);
            }

            this.nextAt = now;
            this.darkSearch = darkSearch;
         }
      }

      long darkSearchTotal() {
         return this.darkSearchNanos;
      }

      boolean darkSearchExceeded(long limit) {
         return this.darkSearchNanos >= limit;
      }
   }

   static final class ChildRunner {
      private AiOperation child;
      private AiOperation.Result lastResult;
      private AiOperation.Result pendingResult;
      private boolean pendingReconciliation;

      AiOperation.Result tickChild(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         this.child = child;
         this.lastResult = child.tick();
         if (this.lastResult.terminal()) {
            JsonObject data = AiRunResources.parseEvidenceJson(this.lastResult.dataJson());
            boolean needsObservation = child.requiresReconciliationAfterEmergency()
               || this.lastResult.status() != AiOperation.Status.SUCCEEDED && before
               || AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false)
               || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
            this.lastResult = AiGuardLayer.wrapPendingTermination(needsObservation, this.lastResult);
            this.notePending(needsObservation, this.lastResult);
         }

         return this.lastResult;
      }

      void init_V(AiOperation child) {
         if (child != null) {
            AiOperation.Result actual = this.child == child ? this.lastResult : null;
            this.notePending(child.requiresReconciliationAfterEmergency(), actual);
            if (this.child != child) {
               this.child = child;
               this.lastResult = null;
            }
         }
      }

      void notePending(boolean needsObservation, AiOperation.Result actual) {
         JsonObject receipt = actual == null ? new JsonObject() : AiRunResources.parseEvidenceJson(actual.dataJson());
         if (needsObservation
            || AiJson.getBoolean(receipt, "interruptedActionNeedsReconciliation", false)
            || AiJson.getBoolean(receipt, "reobserveBeforeResuming", false)) {
            this.pendingReconciliation = true;
            if (actual != null && (this.pendingResult == null || actual == this.lastResult)) {
               this.pendingResult = actual;
            }
         }
      }

      void cancelChild(AiOperation child) {
         if (child != null) {
            this.init_V(child);
            child.cancel();
         }
      }

      boolean needsReconciliation() {
         return this.pendingReconciliation;
      }

      AiOperation.Result result(AiOperation.Status status, String message, JsonObject phaseData, AiOperation.Result actual) {
         JsonObject data = phaseData.deepCopy();
         if (this.pendingResult != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.pendingResult.dataJson()));
         }

         if (actual == null) {
            actual = this.lastResult != null ? this.lastResult : this.pendingResult;
         }

         AiOperation.Result result = AiStronghold.resultWithChild(status, message, data, this.pendingReconciliation, actual);
         return actual == null ? result : new AiOperation.Result(result.status(), result.message(), result.dataJson(), actual.evidence(), actual.failure());
      }
   }

   static final class EyeThrowReceipt {
      int beforeEyes;
      int heldEyes;
      int sequenceBefore;
      int sentSequence = -1;
      int observedEyes;
      long inventoryBefore;
      long repliesBefore;
      long repliesAtAcknowledgement = -1L;
      long observedInventoryRevision;
      long observedContainerReplies;
      int serverEyeSnapshot = -1;
      UUID identity;
      boolean sent;
      boolean load_Z;
      boolean read_Z;
      boolean write_Z;
      boolean acknowledged;

      void markSent(int eyes, int held, long inventory, long replies, int sequence) {
         if (this.sent) {
            throw new IllegalStateException("An Eye throw receipt cannot send another use");
         }

         this.beforeEyes = this.observedEyes = eyes;
         this.heldEyes = held;
         this.inventoryBefore = this.observedInventoryRevision = inventory;
         this.repliesBefore = this.observedContainerReplies = replies;
         this.sequenceBefore = sequence;
         this.sent = true;
      }

      void noteSentSequence(int sequence) {
         if (sequence > this.sequenceBefore) {
            this.sentSequence = sequence;
         }
      }

      AiStronghold.ThrowOutcome observe(int eyes, long inventory, OptionalInt snapshot, long replies, boolean acknowledged, UUID identity, int elapsedTicks) {
         if (!this.sent) {
            return AiStronghold.ThrowOutcome.WAITING;
         }

         this.observedEyes = eyes;
         this.observedInventoryRevision = inventory;
         this.observedContainerReplies = replies;
         boolean freshSnapshot = snapshot.isPresent() && replies > this.repliesBefore;
         this.serverEyeSnapshot = freshSnapshot ? snapshot.getAsInt() : -1;
         this.acknowledged = this.acknowledged | (acknowledged && this.sentSequence >= 0);
         if (identity != null) {
            if (this.identity == null) {
               this.identity = identity;
            } else if (!this.identity.equals(identity)) {
               this.write_Z = true;
            }
         }

         if (this.acknowledged && this.repliesAtAcknowledgement < 0L) {
            this.repliesAtAcknowledgement = replies;
         }

         if (this.write_Z) {
            return AiStronghold.ThrowOutcome.AMBIGUOUS;
         } else if (this.load_Z) {
            return AiStronghold.ThrowOutcome.CONSUMED;
         } else if (this.read_Z) {
            return AiStronghold.ThrowOutcome.REJECTED;
         } else {
            int serverEyes = freshSnapshot ? Math.min(eyes, snapshot.getAsInt()) : eyes;
            if (this.beforeEyes - serverEyes > 1) {
               this.write_Z = true;
               return AiStronghold.ThrowOutcome.AMBIGUOUS;
            } else if (this.beforeEyes > 0
               && this.heldEyes > 0
               && (eyes == this.beforeEyes - 1 && inventory > this.inventoryBefore || freshSnapshot && snapshot.getAsInt() == this.beforeEyes - 1)) {
               this.load_Z = true;
               return AiStronghold.ThrowOutcome.CONSUMED;
            } else {
               return AiStronghold.ThrowOutcome.WAITING;
            }
         }
      }

      AiStronghold.ThrowOutcome classifyAfterAcknowledgement(OptionalInt snapshotAfterAcknowledgement, int elapsedTicks) {
         if (this.write_Z) {
            return AiStronghold.ThrowOutcome.AMBIGUOUS;
         } else if (this.load_Z) {
            return AiStronghold.ThrowOutcome.CONSUMED;
         } else if (this.read_Z) {
            return AiStronghold.ThrowOutcome.REJECTED;
         } else if (this.sent
            && elapsedTicks > 120
            && this.acknowledged
            && this.repliesAtAcknowledgement >= 0L
            && this.observedContainerReplies > this.repliesAtAcknowledgement
            && snapshotAfterAcknowledgement.isPresent()
            && snapshotAfterAcknowledgement.getAsInt() == this.beforeEyes
            && this.observedEyes == this.beforeEyes
            && this.identity == null) {
            this.read_Z = true;
            return AiStronghold.ThrowOutcome.REJECTED;
         } else {
            return AiStronghold.ThrowOutcome.WAITING;
         }
      }

      boolean init_Z() {
         return this.sent && (this.write_Z || !this.load_Z && !this.read_Z);
      }

      JsonObject evidence() {
         return AiJson.objectOf(
            "sent",
            this.sent,
            "beforeEyes",
            this.beforeEyes,
            "heldEyes",
            this.heldEyes,
            "observedEyes",
            this.observedEyes,
            "inventoryBefore",
            this.inventoryBefore,
            "repliesBefore",
            this.repliesBefore,
            "sequenceBefore",
            this.sequenceBefore,
            "sentSequence",
            this.sentSequence,
            "repliesAtAcknowledgement",
            this.repliesAtAcknowledgement,
            "observedInventoryRevision",
            this.observedInventoryRevision,
            "observedContainerReplies",
            this.observedContainerReplies,
            "serverEyeSnapshot",
            this.serverEyeSnapshot,
            "useAcknowledged",
            this.acknowledged,
            "eyeIdentity",
            this.identity == null ? "" : this.identity.toString(),
            "consumed",
            this.load_Z,
            "rejected",
            this.read_Z,
            "ambiguous",
            this.write_Z,
            "pending",
            this.init_Z()
         );
      }
   }
}
