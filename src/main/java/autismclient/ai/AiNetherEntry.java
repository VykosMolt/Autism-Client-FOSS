package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;

final class AiNetherEntry {
   static final int DEFAULT_TIMEOUT_SECONDS = 1800;
   static final int MIN_TIMEOUT_SECONDS = 300;
   static final int MAX_TIMEOUT_SECONDS = 3600;
   static final int write_I = 4;
   static final int update = 300;
   static final int sync = 180;
   static final int flush = 300;
   static final int close = 420;
   static final int open = 240;
   static final int poll = 300;
   static final int push = 180;
   static final int pop = 240;
   static final int MAX_CAPPED_TIMEOUT_SECONDS = 360;
   static final long SLEEP_RETRY_DELAY_MS = 20000L;
   static final int run = 3;
   static final int get = 2;
   static final int set = 10;
   static final int put = 6;
   static final int add = 12;
   static final double VILLAGE_MAX_DISTANCE = 750.0;
   static final int clear = 24;
   static final int reset = 48;
   static final int start = 64;
   static final int stop = 8;
   static final int next = 3;
   static final int prev = 48;
   static final int valid = 40;
   static final int SAFE_FOOD_TARGET = 14;
   static volatile boolean init_Z;
   static final int data = 12;
   static final int value = 3;
   static final int cache = 4;
   static final int state = 8;
   static final int queue = 8;
   static final int buffer = 20;
   static final long NIGHT_START_TICK = 12500L;
   static final long NIGHT_END_TICK = 23460L;
   private static final Map<BlockPos, Long> unreachablePools = new ConcurrentHashMap<>();
   private static final long MARK_TTL_NANOS = 600000000000L;
   private static final Map<BlockPos, Long> failedCastPools = new ConcurrentHashMap<>();

   private AiNetherEntry() {
   }

   static boolean hasStonePickaxe(int stonePickaxes) {
      return stonePickaxes >= 1;
   }

   static boolean shouldSkipOpening(boolean openingDone, boolean legActive) {
      return openingDone && !legActive;
   }

   static boolean shouldSkipLavaSearch(boolean nearby, boolean legActive) {
      return nearby && !legActive;
   }

   static boolean needsFood(int foodLevel, int foodItems) {
      return foodLevel <= 10 || foodItems < 6;
   }

   static boolean shouldAbandonSpareObsidian(double distance, boolean canInterrupt, boolean requiresReconciliation) {
      return distance > 40.0 && canInterrupt && !requiresReconciliation;
   }

   static boolean hasIronFireGear(int ironIngots, int flint, int buckets, int flintAndSteel, int minIron) {
      return buckets >= 1 && flintAndSteel >= 1;
   }

   static int ironStillNeeded(int ironIngots, int buckets, int flintAndSteel) {
      int need = (buckets >= 1 ? 0 : 3) + (flintAndSteel >= 1 ? 0 : 1);
      return Math.max(0, need - ironIngots);
   }

   static boolean readyToEnterNether(boolean gearReady, int nutrition, float health, int hunger, int waterBuckets) {
      return gearReady && nutrition >= 32 && health >= 16.0F && hunger >= 18 && waterBuckets >= 2;
   }

   static void markUnreachablePool(BlockPos pos) {
      unreachablePools.put(pos.immutable(), System.nanoTime());
   }

   static void markCastFailed(BlockPos pos) {
      failedCastPools.put(pos.immutable(), System.nanoTime());
   }

   static boolean nearMarkedPool(BlockPos pos) {
      long now = System.nanoTime();
      unreachablePools.values().removeIf(at -> now - at > 600000000000L);
      failedCastPools.values().removeIf(at -> now - at > 600000000000L);

      for (BlockPos bad : unreachablePools.keySet()) {
         if (bad.distSqr(pos) <= 36.0) {
            return true;
         }
      }

      for (BlockPos bad : failedCastPools.keySet()) {
         if (bad.distSqr(pos) <= 256.0) {
            return true;
         }
      }

      return false;
   }

   static boolean shouldVisitVillage(boolean skipVillage, double villageDistance, int bedsHeld, int bedsTarget) {
      return !skipVillage && villageDistance <= 750.0 && bedsHeld < bedsTarget;
   }

   static String nightAction(long dayTime, boolean overworld, boolean hasBed) {
      if (overworld && dayTime >= 12500L && dayTime <= 23460L) {
         return hasBed ? "sleep" : "burrow";
      } else {
         return null;
      }
   }

   static boolean shouldSleepNow(long dayTime, boolean overworld, boolean nightActive, long handledDay, long dayIndex, long retryAtMs, long nowMs) {
      if (!overworld || nightActive) {
         return false;
      } else if (dayTime >= 12500L && dayTime <= 23460L) {
         return handledDay == dayIndex ? false : nowMs >= retryAtMs;
      } else {
         return false;
      }
   }

   static AiNetherEntry.Inner_CachePool pickNextPhase(
      boolean inNether, boolean openingDone, boolean foodNeeded, boolean villageWanted, boolean ironFireDone, boolean portalNearby, boolean lavaNearby
   ) {
      if (inNether) {
         return null;
      } else if (villageWanted) {
         return AiNetherEntry.Inner_CachePool.VILLAGE;
      } else if (!openingDone) {
         return AiNetherEntry.Inner_CachePool.OPENING;
      } else if (foodNeeded) {
         return AiNetherEntry.Inner_CachePool.FOOD;
      } else if (!ironFireDone) {
         return AiNetherEntry.Inner_CachePool.IRON_FIRE;
      } else if (portalNearby) {
         return AiNetherEntry.Inner_CachePool.ENTER;
      } else {
         return lavaNearby ? AiNetherEntry.Inner_CachePool.PORTAL : AiNetherEntry.Inner_CachePool.LAVA;
      }
   }

   static boolean isRequiredPhase(AiNetherEntry.Inner_CachePool phase) {
      return phase != AiNetherEntry.Inner_CachePool.FOOD && phase != AiNetherEntry.Inner_CachePool.VILLAGE;
   }

   static int maxAttempts(AiNetherEntry.Inner_CachePool phase) {
      byte var10000;
      switch (phase) {
         case OPENING:
         case IRON_FIRE:
         case LAVA:
            var10000 = 3;
            break;
         case FOOD:
            var10000 = 2;
            break;
         case VILLAGE:
         default:
            var10000 = 1;
      }

      return var10000;
   }

   static boolean canRetryPhase(AiNetherEntry.Inner_CachePool phase, AiFailure failure) {
      if (failure == AiFailure.MISSING_MATERIALS) {
         return false;
      }

      if (phase == AiNetherEntry.Inner_CachePool.FOOD) {
         return true;
      }

      boolean var10000;
      switch (failure) {
         case TIMEOUT:
         case TARGET_NOT_FOUND:
         case NO_REACHABLE_TARGET:
         case UNREACHABLE:
         case UNREACHABLE_AIRBORNE:
         case NO_ROUTE:
         case STUCK:
         case TARGET_GONE:
         case WORLD_CHANGED:
         case THREAT_INTERRUPT:
         case CANCELLED:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static int scaledCap(int baseCap, int attempt) {
      return attempt >= 3 ? baseCap + baseCap / 2 : baseCap;
   }

   static boolean shouldRetryBuildInOpen(AiOperation.Result result, int framed, boolean pending) {
      return !pending && framed == 0 && result != null && (result.status() == AiOperation.Status.FAILED || result.status() == AiOperation.Status.TIMED_OUT);
   }

   static AiFailure failureForResult(AiOperation.Result r) {
      return r.status() == AiOperation.Status.TIMED_OUT ? AiFailure.TIMEOUT : AiFailure.MISSING_MATERIALS;
   }

   static BlockPos siteForAttempt(List<BlockPos> sites, int attemptIndex) {
      return sites.isEmpty() ? null : sites.get(Math.min(attemptIndex, sites.size() - 1));
   }

   static boolean shouldSuppressEat(AiGuard.GuardAction action, boolean hasEdibleFood, boolean containerOpen, boolean screenOpen) {
      return action == AiGuard.GuardAction.EAT && (!hasEdibleFood || containerOpen || screenOpen);
   }

   static int clampTimeout(int seconds) {
      return Math.max(300, Math.min(3600, seconds));
   }

   static int clampCappedTimeout(int configured, int remaining) {
      return Math.max(15, Math.min(configured, Math.min(360, remaining)));
   }

   static int clampUncappedTimeout(int configured, int remaining) {
      return Math.max(15, Math.min(configured, remaining));
   }

   static int approachDecision(boolean haveTarget, boolean closeEnough, boolean legActive) {
      if (!haveTarget) {
         return 2;
      } else if (closeEnough) {
         return legActive ? 1 : 2;
      } else {
         return 0;
      }
   }

   static int logsToChop() {
      return 5;
   }

   static int minLogsNeeded() {
      return 4;
   }

   static JsonObject legEvent(String legId, String status, String message, String dataJson) {
      JsonObject out = new JsonObject();
      out.addProperty("leg", legId);
      out.addProperty("status", status);
      if (message != null && !message.isEmpty()) {
         out.addProperty("message", message);
      }

      try {
         if (dataJson != null) {
            JsonElement parsed = JsonParser.parseString(dataJson);
            if (parsed.isJsonObject() && !parsed.getAsJsonObject().keySet().isEmpty()) {
               out.add("data", parsed);
            }
         }
      } catch (RuntimeException var6) {
      }

      return out;
   }

   static JsonObject parseObject(String dataJson) {
      try {
         if (dataJson == null) {
            return null;
         } else {
            JsonElement parsed = JsonParser.parseString(dataJson);
            if (!parsed.isJsonObject()) {
               return null;
            } else {
               JsonObject data = parsed.getAsJsonObject();
               if (data.has("missing") && data.get("missing").isJsonObject()) {
                  JsonObject missing = data.getAsJsonObject("missing");
                  return missing.keySet().isEmpty() ? null : missing;
               } else {
                  return null;
               }
            }
         }
      } catch (RuntimeException e) {
         return null;
      }
   }

   static BlockPos firstPosition(String dataJson, String arrayKey, double maxDistance) {
      List<BlockPos> all = positions(dataJson, arrayKey, 1, maxDistance);
      return all.isEmpty() ? null : all.get(0);
   }

   static List<BlockPos> positions(String dataJson, String arrayKey, int cap) {
      return positions(dataJson, arrayKey, cap, 1.7976931348623157E308);
   }

   static List<BlockPos> positions(String dataJson, String arrayKey, int cap, double maxDistance) {
      List<BlockPos> out = new ArrayList<>();

      try {
         if (dataJson == null) {
            return out;
         }

         JsonElement parsed = JsonParser.parseString(dataJson);
         if (!parsed.isJsonObject()) {
            return out;
         }

         JsonObject data = parsed.getAsJsonObject();
         if (!data.has(arrayKey) || !data.get(arrayKey).isJsonArray()) {
            return out;
         }

         for (JsonElement element : data.getAsJsonArray(arrayKey)) {
            if (out.size() >= cap) {
               break;
            }

            if (element.isJsonObject()) {
               JsonObject hit = element.getAsJsonObject();
               if (!hit.has("distance") || !(hit.get("distance").getAsDouble() > maxDistance)) {
                  out.add(new BlockPos(hit.get("x").getAsInt(), hit.get("y").getAsInt(), hit.get("z").getAsInt()));
               }
            }
         }
      } catch (RuntimeException var11) {
      }

      return out;
   }

   static String obtainMethodFor(String itemId) {
      String var10000;
      switch (itemId) {
         case "minecraft:water_bucket":
            var10000 = "fill_water";
            break;
         case "minecraft:lava_bucket":
            var10000 = "fill_lava";
            break;
         case "minecraft:dirt":
         case "minecraft:cobblestone":
            var10000 = "collect";
            break;
         default:
            var10000 = "obtain";
      }

      return var10000;
   }

   static JsonObject missingItem(String itemId, int count) {
      JsonObject missing = new JsonObject();
      missing.addProperty(itemId, Math.max(1, count));
      return missing;
   }

   static AiOperation init(JsonObject args) {
      AiDimension.refreshWorld();
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         if (args.has("villageX") != args.has("villageZ")) {
            throw new IllegalArgumentException("villageX and villageZ are given together, or omitted for the oracle prediction");
         }

         final BlockPos explicitVillage = args.has("villageX")
            ? new BlockPos(AiJson.getInt(args, "villageX", 0), mc.player.blockPosition().getY(), AiJson.getInt(args, "villageZ", 0))
            : null;
         final boolean skipVillage = AiJson.getBoolean(args, "skipVillage", false);
         final int bedsTarget = Math.max(0, Math.min(8, AiJson.getInt(args, "beds", 2)));
         final int minIron = Math.max(1, Math.min(16, AiJson.getInt(args, "minIron", 4)));
         final int timeoutSeconds = clampTimeout(AiJson.getInt(args, "timeoutSeconds", 1800));
         final AiItems.ItemSpec logTarget = AiItems.resolve("log");
         final AiItems.ItemSpec bedTarget = AiItems.resolve("bed");
         return new AiOperation() {
            final long deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
            final long startMillis = System.currentTimeMillis();
            final String worldKey = AiWorldIdentity.currentWorldKey(mc);
            final Object connection = mc.getConnection();
            final UUID playerId = mc.player == null ? null : mc.player.getUUID();
            final long deathRevision = AiServerEvidence.deathRevision();
            final AiGuard.GuardPolicy guardPolicy = AiGuard.GuardPolicy.standard();
            final JsonArray phaseHistory = new JsonArray();
            final List<Entry<String, Integer>> missingMaterials = new ArrayList<>();
            final JsonArray legEvents = new JsonArray();
            AiNetherEntry.Inner_CachePool phase;
            long phaseStartedMillis = this.startMillis;
            int step;
            int lootDelayTicks;
            int chestsLooted;
            int hayBalesHarvested;
            int foodBefore;
            int castAttempts;
            int interruptions;
            int attempt = 1;
            int openingSiteIndex;
            boolean castPending;
            boolean ruinedPortalPredicted;
            boolean villageDone;
            boolean foodDone;
            boolean bedsScanned;
            boolean finishRecorded;
            AiOperation child;
            AiOperation guardOp;
            AiOperation nightOp;
            String legId = "";
            String guardReason = "";
            String nightActionName = "";
            String failedPhaseName;
            String suppressedEatReason = "";
            BlockPos village;
            BlockPos travelTarget;
            BlockPos openingSite;
            BlockPos knownPortal;
            boolean portalHistoryChecked;
            long rememberedPortalDeadline;
            int portalLookupRetries = 20;
            AiOperation waterBucketOp;
            int waterBucketAttempts;
            int huntAttempts;
            int safeFoodChecks;
            AiOperation foodOp;
            final AiPreparationReceipt preparation = new AiPreparationReceipt();
            boolean travelTargetIsLava;
            int lavaExplorations;
            int explorationHeading;
            int poolFailures;
            int portalScanCounter;
            boolean portalBuildStarted;
            boolean buildFromUncastablePool;
            boolean castablePoolOnly;
            boolean spareObsidianStarted;
            boolean returningToPortal;
            boolean portalReturnDone;
            int portalReturnAttempts;
            long guardStartNanos;
            long guardCooldownUntil;
            AiOperation spareObsidianOp;
            int surfaceForWaterAttempts;
            BlockPos explorationTarget;
            BlockPos lavaPool;
            List<BlockPos> chests = List.of();
            int chestIndex;
            long nightDay = -1L;
            long nightHandledDay = -1L;
            long sleepFailedDay = -1L;
            long sleepRetryAtMillis = 0L;
            JsonObject missingMaterialsReport;
            JsonObject lastLegEvent;
            String entryWarning;
            AiOperation.Result uncertainLegResult;
            final AiNetherEntry.Inner_DataUtil childTracker = new AiNetherEntry.Inner_DataUtil();
            final AiNetherEntry.Inner_StreamUtil entryTimer = new AiNetherEntry.Inner_StreamUtil();
            int obsidianBeforeMining;
            int obsidianShortfallRetries;

            double horizontalDistanceTo(BlockPos pos) {
               return Math.hypot(pos.getX() + 0.5 - mc.player.getX(), pos.getZ() + 0.5 - mc.player.getZ());
            }

            boolean lavaSourceWithin(int radius) {
               for (BlockPos pos : AiWorldScan.findExposedLavaSources(autismclient.modules.am.init_I(), 2, 32)) {
                  if (pos.distSqr(mc.player.blockPosition()) <= (double)radius * radius) {
                     return true;
                  }
               }

               return false;
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.finishRecorded && mc.player != null && mc.level != null) {
                  if (this.connection != mc.getConnection() || !this.worldKey.equals(AiWorldIdentity.currentWorldKey(mc)) || !mc.player.getUUID().equals(this.playerId)) {
                     return this.finish(
                        AiOperation.Status.UNCERTAIN, null, "World, connection or player changed during Nether entry"
                     );
                  }

                  if (!mc.player.isAlive() || mc.player.getHealth() <= 0.0F || this.deathRevision != AiServerEvidence.deathRevision()) {
                     return this.finish(AiOperation.Status.FAILED, AiFailure.LOW_HEALTH, "Death ended this Nether entry attempt");
                  }

                  if (mc.level.dimension() != Level.OVERWORLD && !this.inNether()) {
                     return this.finish(
                        AiOperation.Status.UNCERTAIN, null, "Nether entry moved into an unsupported dimension"
                     );
                  }

                  autismclient.ai.engine.AiRestock.reserveItemsForJob(Map.of(Items.OBSIDIAN, 64));
                  if (System.nanoTime() > this.deadline) {
                     return this.finish(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        AiFailure.TIMEOUT,
                        "Nether entry deadline reached"
                           + (this.phase == null ? " between phases" : " in " + this.phase.name().toLowerCase(Locale.ROOT))
                     );
                  }

                  if (this.inNether()) {
                     if (this.phase == AiNetherEntry.Inner_CachePool.ENTER && this.child != null && "game:dimension".equals(this.legId)) {
                        AiOperation.Result arrival = this.pollChild();
                        if (arrival == null) {
                           return this.running("Verifying Nether arrival");
                        } else {
                           return arrival.status() != AiOperation.Status.SUCCEEDED
                              ? this.finish(arrival.status(), null, arrival.message())
                              : this.completeEntry("Verified Nether arrival");
                        }
                     } else {
                        return this.phase != null
                           ? this.finish(
                              AiOperation.Status.UNCERTAIN, null, "Entered the Nether outside the expected portal action"
                           )
                           : this.completeEntry("Already in the Nether");
                     }
                  } else {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.finish(
                           AiOperation.Status.FAILED, AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("nether entry", mc.player.getHealth(), mc.player.getMaxHealth())
                        );
                     }

                     if (this.childTracker.requiresReconciliation()) {
                        return this.finish(
                           AiOperation.Status.UNCERTAIN, null, "Interrupted Nether entry action needs observation before resuming"
                        );
                     }

                     AiOperation.Result guarded = this.tickGuard();
                     if (guarded != null) {
                        return guarded;
                     }

                     AiOperation.Result nighted = this.tickNight();
                     if (nighted != null) {
                        return nighted;
                     }

                     if (this.phase == null) {
                        AiNetherEntry.Inner_CachePool next = AiNetherEntry.pickNextPhase(
                           false, this.hasStoneTier(), this.needsFood(), this.needsVillage(), this.hasIronFireGear(), this.portalNearby(), this.lavaNearby()
                        );
                        if ((next == AiNetherEntry.Inner_CachePool.LAVA || next == AiNetherEntry.Inner_CachePool.PORTAL) && !this.portalHistoryChecked) {
                           this.knownPortal = AiPortalLinks.init(192);
                           if (this.knownPortal == null && this.portalLookupRetries-- > 0) {
                              return this.running("Checking observed portal history before making another frame");
                           }

                           this.portalHistoryChecked = true;
                           if (this.knownPortal != null) {
                              this.rememberedPortalDeadline = System.nanoTime() + 120000000000L;
                              this.enterPhase(AiNetherEntry.Inner_CachePool.LAVA);
                              this.step = this.knownPortal.getY() - mc.player.blockPosition().getY() > 16 && !this.skyVisibleWithin(3) ? 89 : 90;
                              return this.running("Rechecking the remembered portal at " + this.knownPortal.toShortString());
                           }
                        }

                        this.enterPhase(next);
                     }

                     AiOperation.Result var6;
                     switch (this.phase) {
                        case OPENING:
                           var6 = this.runOpeningPhase();
                           break;
                        case FOOD:
                           var6 = this.runFoodPhase();
                           break;
                        case VILLAGE:
                           var6 = this.runVillagePhase();
                           break;
                        case IRON_FIRE:
                           var6 = this.runIronFirePhase();
                           break;
                        case LAVA:
                           var6 = this.runLavaPhase();
                           break;
                        case PORTAL:
                           var6 = this.runPortalPhase();
                           break;
                        case ENTER:
                           var6 = this.runEnterPhase();
                           break;
                        default:
                           throw new MatchException(null, null);
                     }

                     AiOperation.Result phaseResult = var6;
                     return this.uncertainLegResult == null && !this.childTracker.requiresReconciliation()
                        ? phaseResult
                        : this.finish(
                           AiOperation.Status.UNCERTAIN,
                           null,
                           this.uncertainLegResult == null
                              ? "Interrupted Nether entry action needs observation before resuming"
                              : this.uncertainLegResult.message()
                        );
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, null, "Nether entry cancelled");
               }
            }

            AiOperation.Result tickGuard() {
               if (this.guardOp == null) {
                  if (System.nanoTime() < this.guardCooldownUntil && mc.player.getHealth() > 4.0F) {
                     return null;
                  }

                  if (!this.canInterruptNow()) {
                     return null;
                  }

                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiGuard.GuardDecision decision = AiGuard.decide(this.guardPolicy, vitals);
                  if (AiNetherEntry.shouldSuppressEat(
                     decision.action(), AiSurvival.init(mc.player.getInventory()) >= 0, mc.player.containerMenu != mc.player.inventoryMenu, mc.gui.screen() != null
                  )) {
                     this.suppressedEatReason = decision.reason();
                     return null;
                  }

                  AiOperation action = AiGuard.createResponseOperation(this.guardPolicy, vitals, this.secondsLeft());
                  if (action == null) {
                     return null;
                  }

                  this.guardReason = AiGuard.decisionReason(this.guardPolicy, vitals);
                  this.interruptions++;
                  this.interruptChild("interrupted: " + this.guardReason);
                  this.cancelSideOps();
                  if (this.nightOp != null) {
                     this.childTracker.cancel(this.nightOp);
                     this.nightOp = null;
                  }

                  this.guardOp = AiRunDiagnostics.trace("survival", this.guardReason, action);
                  this.guardStartNanos = System.nanoTime();
                  return this.running("Interrupted: " + this.guardReason);
               } else {
                  AiOperation.Result r = this.childTracker.tick(this.guardOp);
                  if (!r.terminal()) {
                     return this.running("Interrupted: " + this.guardReason + " - " + r.message());
                  }

                  this.childTracker.cancel(this.guardOp);
                  this.guardOp = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  boolean quick = System.nanoTime() - this.guardStartNanos < 1000000000L;
                  if (r.status() != AiOperation.Status.SUCCEEDED && quick) {
                     this.guardCooldownUntil = System.nanoTime() + 3000000000L;
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED || !quick) {
                     AutismClientAddon.LOG.info("[AI] nether entry: guard {} after {}: {}", r.status(), this.guardReason, r.message());
                  }

                  return this.running("Resuming after " + this.guardReason);
               }
            }

            AiOperation.Result tickNight() {
               if (this.nightOp == null && !this.canInterruptNow()) {
                  return null;
               }

               if (this.nightOp != null) {
                  AiOperation.Result r = this.childTracker.tick(this.nightOp);
                  if (!r.terminal()) {
                     return this.running("Night: " + this.nightActionName + " - " + r.message());
                  }

                  this.childTracker.cancel(this.nightOp);
                  this.nightOp = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.nightHandledDay = this.nightDay;
                  } else {
                     if ("sleep".equals(this.nightActionName)) {
                        this.sleepFailedDay = this.nightDay;
                     }

                     this.sleepRetryAtMillis = System.currentTimeMillis() + 20000L;
                  }

                  return this.running("Resuming after the night (" + this.nightActionName + ")");
               } else {
                  boolean sleepable = this.bedCount() > 0 && this.sleepFailedDay != this.overworldDay();
                  if (AiOracle.isActive() && !sleepable) {
                     return null;
                  }

                  if (!AiNetherEntry.shouldSleepNow(AiSleep.dayTime(), this.inOverworld(), false, this.nightHandledDay, this.overworldDay(), this.sleepRetryAtMillis, System.currentTimeMillis())) {
                     return null;
                  }

                  String action = AiNetherEntry.nightAction(AiSleep.dayTime(), true, sleepable);
                  if (action == null) {
                     return null;
                  }

                  this.interruptChild("night falls");
                  this.cancelSideOps();
                  this.interruptions++;
                  this.nightActionName = action;
                  this.nightDay = this.overworldDay();
                  int cap = Math.max(60, Math.min(600, this.secondsLeft()));
                  if ("sleep".equals(action)) {
                     JsonObject sleepArgs = AiJson.objectOf("timeoutSeconds", Math.min(240, cap));
                     this.nightOp = this.allowLowHealthIfRecovery("game:sleep", sleepArgs, AiSleep.init(sleepArgs));
                  } else {
                     this.nightOp = AiBurrow.init(AiJson.objectOf("timeoutSeconds", cap));
                  }

                  return this.running("Night falls; " + ("sleep".equals(action) ? "sleeping until dawn" : "burrowing until dawn"));
               }
            }

            AiOperation.Result runOpeningPhase() {
               if (AiNetherEntry.shouldSkipOpening(this.hasStoneTier(), this.child != null)) {
                  return this.finishPhase("skipped", "tools already present");
               }

               if (this.step == 0) {
                  if (this.logCount() >= AiNetherEntry.logsToChop()) {
                     this.step = 3;
                     return this.running("Logs already in the bag");
                  }

                  AiOperation.Result r = this.findBlocks("log", 4, 96);
                  if (r == null) {
                     return this.running("Locating the nearest tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  List<BlockPos> sites = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 4) : List.of();
                  this.openingSite = AiNetherEntry.siteForAttempt(sites, this.openingSiteIndex);
                  this.step = 1;
                  return this.running(
                     this.openingSite == null
                        ? "No log located within 96 blocks; chopping from here"
                        : "Tree site " + (sites.indexOf(this.openingSite) + 1) + "/" + sites.size() + " at " + this.openingSite.toShortString()
                  );
               } else if (this.step == 1) {
                  int action = AiNetherEntry.approachDecision(
                     this.openingSite != null,
                     this.openingSite != null && mc.player.blockPosition().distSqr(this.openingSite) <= 64.0,
                     this.child != null
                  );
                  if (action != 0) {
                     this.drainChild();
                     this.step = 2;
                     return this.running("At the woodline");
                  }

                  AiOperation.Result r = this.walkTo(this.openingSite, 8.0, 300);
                  if (r == null) {
                     return this.running("Travelling to the tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  this.step = 2;
                  return this.running("At the woodline");
               } else if (this.step == 2) {
                  if (this.logCount() >= AiNetherEntry.logsToChop()) {
                     this.drainChild();
                     this.step = 3;
                     return this.running("Logs already in the bag");
                  }

                  if (this.child == null) {
                     this.startLeg("game:chop", AiJson.objectOf("block", "log", "count", AiNetherEntry.logsToChop(), "radius", 64, "timeoutSeconds", this.cappedTimeout(300)));
                     return this.running("Felling trees for the opening");
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.running("Felling trees for the opening");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (this.logCount() < AiNetherEntry.minLogsNeeded()) {
                     return this.retryOrFail(
                        AiFailure.TARGET_NOT_FOUND,
                        "Only " + this.logCount() + " log(s) after the wood run (" + r.message() + ")",
                        AiNetherEntry.missingItem("minecraft:oak_log", AiNetherEntry.minLogsNeeded() - this.logCount())
                     );
                  }

                  this.step = 3;
                  return this.running("Logs gathered");
               } else if (this.step == 3) {
                  if (this.child != null || this.count("minecraft:wooden_pickaxe") <= 0 && this.count("minecraft:crafting_table") <= 0) {
                     AiOperation.Result r = this.obtainItem("minecraft:wooden_pickaxe", 1, this.attemptCap(300), this.isLateAttempt());
                     if (r == null) {
                        return this.running("Obtaining the wooden pickaxe");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(AiNetherEntry.failureForResult(r), "Wooden pickaxe unobtainable: " + r.message(), AiNetherEntry.missingItem("minecraft:wooden_pickaxe", 1));
                     }

                     this.step = 4;
                     return this.running("Wooden pickaxe obtained");
                  } else {
                     this.step = 4;
                     return this.running("Wooden pickaxe covered");
                  }
               } else if (this.step == 4) {
                  if (this.child == null && this.count("minecraft:stone_pickaxe") > 0) {
                     this.step = 5;
                     return this.running("Stone pickaxe covered");
                  }

                  AiOperation.Result r = this.obtainItem("minecraft:stone_pickaxe", 1, this.attemptCap(300), this.isLateAttempt());
                  if (r == null) {
                     return this.running("Obtaining the stone pickaxe");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(AiNetherEntry.failureForResult(r), "Stone pickaxe unobtainable: " + r.message(), AiNetherEntry.missingItem("minecraft:stone_pickaxe", 1));
                  }

                  this.step = 5;
                  return this.running("Stone pickaxe obtained");
               } else {
                  if (this.step == 5) {
                     if (this.child != null || this.count("minecraft:cobblestone") >= 3 && this.count("minecraft:stone_axe") <= 0) {
                        AiOperation.Result r = this.obtainItem("minecraft:stone_axe", 1, this.attemptCap(240), this.isLateAttempt());
                        if (r == null) {
                           return this.running("Cheap stone axe");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        this.step = 6;
                        return this.running("Stone axe done");
                     }

                     this.step = 6;
                  }

                  if (this.step == 6) {
                     if (this.child != null || this.count("minecraft:cobblestone") >= 2 && this.count("minecraft:stone_sword") <= 0) {
                        AiOperation.Result r = this.obtainItem("minecraft:stone_sword", 1, this.attemptCap(240), this.isLateAttempt());
                        if (r == null) {
                           return this.running("Cheap stone sword");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        this.step = 7;
                        return this.running("Stone sword done");
                     }

                     this.step = 7;
                  }

                  return this.hasStoneTier()
                     ? this.finishPhase("succeeded", "opening tools ready")
                     : this.retryOrFail(AiFailure.MISSING_MATERIALS, "Opening did not produce the tools", AiNetherEntry.missingItem("minecraft:stone_pickaxe", 1));
               }
            }

            AiOperation.Result runFoodPhase() {
               if (!this.needsFood()) {
                  this.foodDone = true;
                  return this.finishPhase("skipped", "food is fine");
               }

               if (this.step == 0) {
                  this.step = 1;
                  this.foodBefore = this.safeFoodCount();
               }

               if (this.child == null) {
                  AiOperation hunt = AiSpeedrun.hunt(48.0, 12, this.cappedTimeout(180));
                  if (hunt == null) {
                     this.foodDone = true;
                     return this.finishPhase("skipped", "no food animal in sight");
                  } else {
                     this.legId = "game:obtain";
                     this.recordLegEvent(AiNetherEntry.legEvent(this.legId, "STARTED", "", null));
                     this.child = hunt;
                     return this.running("Hunting food");
                  }
               } else {
                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.running("Hunting food");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  } else if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.foodDone = true;
                     return this.finishPhase("succeeded", "food " + this.foodBefore + " -> " + this.safeFoodCount());
                  } else if (this.attempt < AiNetherEntry.maxAttempts(AiNetherEntry.Inner_CachePool.FOOD) && AiNetherEntry.canRetryPhase(AiNetherEntry.Inner_CachePool.FOOD, r.failure())) {
                     this.attempt++;
                     this.step = 0;
                     return this.running("The hunt came up short; hunting again (attempt " + this.attempt + "/" + AiNetherEntry.maxAttempts(AiNetherEntry.Inner_CachePool.FOOD) + ")");
                  } else {
                     this.foodDone = true;
                     return this.finishPhase("failed", "food " + this.foodBefore + " -> " + this.safeFoodCount() + " (" + r.message() + ")");
                  }
               }
            }

            AiOperation.Result runVillagePhase() {
               if (this.step == 0) {
                  this.step = 1;
                  if (explicitVillage != null) {
                     if (!AiNetherEntry.shouldVisitVillage(skipVillage, this.cornerDistanceTo(explicitVillage), this.bedCount(), bedsTarget)) {
                        this.villageDone = true;
                        return this.finishPhase("skipped", "village out of range or beds covered");
                     } else {
                        this.village = explicitVillage;
                        return this.running("Village at " + this.village.toShortString());
                     }
                  } else if (!skipVillage && this.bedCount() < bedsTarget) {
                     this.step = 1;
                     return this.running("Predicting the nearest village");
                  } else {
                     this.villageDone = true;
                     return this.finishPhase("skipped", "village disabled or beds covered");
                  }
               } else if (this.step == 1) {
                  if (this.village == null) {
                     AiOperation.Result r = this.locate("structure:village");
                     if (r == null) {
                        return this.running("Predicting the nearest village");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.firstPosition(r.dataJson(), "positions", 750.0) : null;
                     if (hit == null) {
                        this.villageDone = true;
                        return this.finishPhase(
                           "skipped",
                           r.status() == AiOperation.Status.SUCCEEDED
                              ? "no village predicted within 750 blocks"
                              : "no village prediction available (" + r.message() + ")"
                        );
                     }

                     this.village = hit;
                  }

                  this.step = 2;
                  return this.running("Travelling to the village at " + this.village.toShortString());
               } else if (this.step == 2) {
                  AiOperation.Result r = this.walkToUncapped(this.village, 14.0, 300);
                  if (r == null) {
                     return this.running("Travelling to the village");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     this.villageDone = true;
                     return this.finishPhase("failed", "no route to the village (" + r.message() + ")");
                  } else {
                     this.step = 3;
                     return this.running("At the village; scanning for chests");
                  }
               } else if (this.step == 3) {
                  AiOperation.Result r = this.findBlocks("chest", 8, 48);
                  if (r == null) {
                     return this.running("Scanning the village for chests");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 4) : List.of();
                  this.chestIndex = 0;
                  this.step = 4;
                  return this.running(this.chests.size() + " chest(s) to loot");
               } else if (this.step == 4) {
                  if (this.chestIndex < this.chests.size()) {
                     BlockPos chest = this.chests.get(this.chestIndex);
                     if (this.child == null) {
                        this.startLeg(
                           "game:container",
                           AiJson.objectOf(
                              "action", "loot", "x", chest.getX(), "y", chest.getY(), "z", chest.getZ(), "timeoutSeconds", Math.min(60, this.cappedTimeout(300))
                           )
                        );
                        return this.running("Looting a village chest");
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Looting a village chest");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.chestsLooted++;
                     }

                     this.chestIndex++;
                     return this.running("Chests looted: " + this.chestsLooted);
                  } else {
                     this.step = 5;
                     return this.running("Scanning for hay bales");
                  }
               } else if (this.step == 5) {
                  AiOperation.Result r = this.findBlocks("hay_block", 8, 48);
                  if (r == null) {
                     return this.running("Scanning for hay bales");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 8) : List.of();
                  this.chestIndex = 0;
                  this.step = 6;
                  return this.running(this.chests.size() + " hay bale(s) to harvest");
               } else if (this.step == 6) {
                  if (this.chestIndex < this.chests.size()) {
                     AiOperation.Result r = this.breakBlockAt(this.chests.get(this.chestIndex));
                     if (r == null) {
                        return this.running("Harvesting a hay bale");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.hayBalesHarvested++;
                     }

                     this.chestIndex++;
                     this.lootDelayTicks = 20;
                     return this.running("Hay bales harvested: " + this.hayBalesHarvested);
                  } else {
                     this.step = 7;
                     this.chests = List.of();
                     this.chestIndex = 0;
                     return this.running(this.bedCount() < bedsTarget ? "Scanning for beds" : "Beds covered");
                  }
               } else {
                  if (this.step == 7) {
                     if (this.bedCount() < bedsTarget && !this.bedsScanned) {
                        AiOperation.Result r = this.findBlocks("bed", 8, 48);
                        if (r == null) {
                           return this.running("Scanning for beds");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 8) : List.of();
                        this.chestIndex = 0;
                        this.bedsScanned = true;
                     }

                     if (this.chestIndex < this.chests.size() && this.bedCount() < bedsTarget) {
                        AiOperation.Result r = this.breakBlockAt(this.chests.get(this.chestIndex));
                        if (r == null) {
                           return this.running("Taking a village bed");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        this.chestIndex++;
                        this.lootDelayTicks = 20;
                        return this.running("Beds held: " + this.bedCount());
                     }

                     this.step = 8;
                  }

                  this.villageDone = true;
                  return this.finishPhase(
                     "succeeded", "looted " + this.chestsLooted + " chest(s), " + this.hayBalesHarvested + " hay bale(s), beds " + this.bedCount() + "/" + bedsTarget
                  );
               }
            }

            AiOperation.Result runIronFirePhase() {
               if (this.hasIronFireGear() && this.child == null) {
                  return this.finishPhase("skipped", "iron and fire gear already present");
               }

               if (this.step == 0) {
                  int ironNeed = Math.max(AiNetherEntry.ironStillNeeded(0, this.bucketCount(), this.count("minecraft:flint_and_steel")), 0);
                  if (this.child == null && this.count("minecraft:iron_ingot") >= ironNeed) {
                     this.step = 1;
                     return this.running("Iron already in the bag");
                  }

                  AiOperation.Result r = this.obtainItemUncapped("minecraft:iron_ingot", ironNeed, this.attemptCap(420));
                  if (r == null) {
                     return this.running("Obtaining iron for the bucket and lighter");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(
                        AiNetherEntry.failureForResult(r),
                        "Iron unobtainable: " + r.message(),
                        AiNetherEntry.missingItem(
                           "minecraft:iron_ingot",
                           Math.max(1, AiNetherEntry.ironStillNeeded(this.count("minecraft:iron_ingot"), this.bucketCount(), this.count("minecraft:flint_and_steel")))
                        )
                     );
                  }

                  this.step = 1;
                  return this.running("Iron obtained");
               } else if (this.step != 1) {
                  if (this.step == 2) {
                     if (this.child == null && this.bucketCount() > 0) {
                        this.step = 3;
                        return this.running("Bucket already in the bag");
                     }

                     AiOperation.Result r = this.craftItemUncapped("minecraft:bucket", this.attemptCap(420));
                     if (r == null) {
                        return this.running("Crafting the bucket");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(AiNetherEntry.failureForResult(r), "Bucket not craftable: " + r.message(), this.missingFromResult(r, "minecraft:bucket"));
                     }

                     this.step = 3;
                     return this.running("Bucket crafted");
                  } else if (this.step == 3) {
                     if (this.child == null && this.count("minecraft:flint_and_steel") > 0) {
                        this.step = 4;
                        return this.running("Lighter already in the bag");
                     }

                     AiOperation.Result r = this.craftItemUncapped("minecraft:flint_and_steel", this.attemptCap(420));
                     if (r == null) {
                        return this.running("Crafting flint and steel");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(
                           AiNetherEntry.failureForResult(r), "Flint and steel not craftable: " + r.message(), this.missingFromResult(r, "minecraft:flint_and_steel")
                        );
                     }

                     this.step = 4;
                     return this.running("Flint and steel crafted");
                  } else {
                     return this.hasIronFireGear()
                        ? this.finishPhase("succeeded", "bucket and flint_and_steel ready")
                        : this.retryOrFail(AiFailure.MISSING_MATERIALS, "Iron/fire gear incomplete", AiNetherEntry.missingItem("minecraft:flint_and_steel", 1));
                  }
               } else if (this.child != null || this.count("minecraft:flint") <= 0 && this.count("minecraft:flint_and_steel") <= 0) {
                  AiOperation.Result r = this.obtainItemUncapped("minecraft:flint", 1, this.attemptCap(420));
                  if (r == null) {
                     return this.running("Obtaining flint from gravel");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(AiNetherEntry.failureForResult(r), "Flint unobtainable: " + r.message(), AiNetherEntry.missingItem("minecraft:flint", 1));
                  }

                  this.step = 2;
                  return this.running("Flint obtained");
               } else {
                  this.step = 2;
                  return this.running("Flint already in the bag");
               }
            }

            AiOperation.Result runLavaPhase() {
               if (this.step == 89) {
                  if (System.nanoTime() > this.rememberedPortalDeadline && this.canInterruptNow()) {
                     this.drainChild();
                     return this.finishPhase("skipped", "remembered portal ascent exhausted its bounded budget; using local resources");
                  }

                  if (this.child == null) {
                     this.legId = "game:surface";
                     this.recordLegEvent(AiNetherEntry.legEvent(this.legId, "STARTED", "", null));
                     int seconds = (int)Math.max(1L, (this.rememberedPortalDeadline - System.nanoTime()) / 1000000000L);
                     this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", Math.min(120, seconds)));
                  }

                  AiOperation.Result ascent = this.pollChild();
                  if (ascent == null) {
                     return this.running("Surfacing toward the remembered portal at " + this.knownPortal.toShortString());
                  }

                  if (ascent.status() == AiOperation.Status.UNCERTAIN) {
                     return ascent;
                  }

                  if (ascent.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  if (ascent.status() != AiOperation.Status.SUCCEEDED) {
                     return this.finishPhase("skipped", "remembered portal ascent failed; using local resources (" + ascent.message() + ")");
                  }

                  this.step = 90;
                  return this.running("Surface reached; rechecking the remembered portal");
               } else if (this.step == 90) {
                  if (System.nanoTime() > this.rememberedPortalDeadline && this.canInterruptNow()) {
                     this.drainChild();
                     return this.finishPhase("skipped", "remembered portal walk exhausted its bounded budget; using local resources");
                  } else {
                     AiOperation.Result route = this.walkTo(this.knownPortal, 4.0, 120);
                     if (route == null) {
                        return this.running("Rechecking the remembered portal at " + this.knownPortal.toShortString());
                     } else {
                        return route.status() == AiOperation.Status.UNCERTAIN
                           ? route
                           : this.finishPhase(
                              "skipped",
                              this.portalNearby()
                                 ? "remembered portal re-observed; no new frame needed"
                                 : "remembered portal could not be re-observed; using local resources (" + route.message() + ")"
                           );
                     }
                  }
               } else {
                  if (this.step >= 40) {
                     return this.surfaceStep();
                  }

                  if (this.step >= 30) {
                     return this.fetchWaterStep();
                  }

                  if (this.step == 0 && this.surfaceForWaterAttempts == 0 && this.count("minecraft:water_bucket") == 0 && this.count("minecraft:bucket") > 0) {
                     this.step = 30;
                     return this.running("Filling a water bucket before the lava");
                  }

                  if (this.step == 0 && AiDimension.framedObsidian() > 0 && this.canBuildPortalFrame()) {
                     this.enterPhase(AiNetherEntry.Inner_CachePool.PORTAL);
                     return this.startPortalBuild("the frame begun at " + AiDimension.frameOrigin.toShortString() + " holds " + AiDimension.framedObsidian() + " obsidian");
                  }

                  if (AiNetherEntry.shouldSkipLavaSearch(this.lavaNearby(), this.child != null)) {
                     if (this.buildFromUncastablePool) {
                        this.buildFromUncastablePool = false;
                        this.drainChild();
                        this.enterPhase(AiNetherEntry.Inner_CachePool.PORTAL);
                        return this.startPortalBuild("no pool in loaded terrain takes a cast");
                     }

                     if (!this.castablePoolOnly || this.castableLavaPool() != null) {
                        return this.finishPhase("skipped", "already at lava");
                     }
                  }

                  if (this.step == 0) {
                     this.step = 1;
                     return this.running("Scanning loaded terrain for lava");
                  }

                  if (this.step == 1) {
                     BlockPos hit = null;
                     BlockPos open = null;
                     int checked = 0;

                     for (BlockPos pos : AiWorldScan.findExposedLavaSources(autismclient.modules.am.init_I(), 2, 32)) {
                        if (!AiNetherEntry.nearMarkedPool(pos)) {
                           if (open == null) {
                              open = pos;
                           }

                           if (checked++ < 12 && AiPortalCast.init_Z(mc.level, pos)) {
                              hit = pos;
                              break;
                           }
                        }
                     }

                     if (hit == null && open != null && this.canBuildPortalFrame()) {
                        AutismClientAddon.LOG.info("[AI] nether entry: no castable pool among {} open ones; obsidian from {}", checked, open.toShortString());
                        this.travelTarget = open;
                        this.travelTargetIsLava = true;
                        this.buildFromUncastablePool = true;
                        this.step = 3;
                        return this.running("No castable pool; obsidian from the pool at " + open.toShortString());
                     }

                     if (hit == null && !this.castablePoolOnly) {
                        hit = open;
                     }

                     if (hit != null) {
                        this.travelTarget = hit;
                        this.travelTargetIsLava = true;
                        this.step = 3;
                        return this.running("Open lava observed at " + hit.toShortString());
                     }

                     if (AiOracle.isActive()) {
                        if (this.lavaExplorations >= 8) {
                           return this.retryOrFail(AiFailure.TARGET_NOT_FOUND, "No open lava after " + this.lavaExplorations + " exploration legs", null);
                        }

                        this.step = 5;
                        return this.running("No open lava in loaded terrain; exploring");
                     } else {
                        if (this.ruinedPortalPredicted) {
                           return this.retryOrFail(
                              AiFailure.TARGET_NOT_FOUND, "No lava observed near the predicted ruined portal; explore or re-issue closer to a pool", null
                           );
                        }

                        this.step = 2;
                        return this.running("No lava in loaded terrain; predicting a ruined portal");
                     }
                  } else if (this.step == 2) {
                     AiOperation.Result r = this.locate("structure:ruined_portal");
                     if (r == null) {
                        return this.running("Predicting a ruined portal");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.firstPosition(r.dataJson(), "positions", 1.7976931348623157E308) : null;
                     if (hit == null) {
                        return this.retryOrFail(AiFailure.TARGET_NOT_FOUND, "No lava in loaded terrain and no ruined portal prediction (" + r.message() + ")", null);
                     }

                     this.ruinedPortalPredicted = true;
                     this.travelTarget = hit;
                     this.travelTargetIsLava = false;
                     this.step = 3;
                     return this.running("Ruined portal predicted at " + hit.toShortString());
                  } else {
                     if (this.step == 3) {
                        AiOperation.Result r = this.travelTargetIsLava
                           ? this.travelTo(this.travelTarget, 240)
                           : this.walkTo(this.travelTarget, 16.0, 240);
                        if (r == null) {
                           return this.running("Travelling to " + (this.travelTargetIsLava ? "lava" : "the ruined portal"));
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           if (this.travelTargetIsLava) {
                              AiNetherEntry.markUnreachablePool(this.travelTarget);
                           }

                           return this.retryOrFail(AiFailure.UNREACHABLE, "No route to " + this.travelTarget.toShortString() + " (" + r.message() + ")", null);
                        }

                        if (!this.travelTargetIsLava) {
                           this.step = 0;
                           return this.running("At the ruined portal; scanning for its lava");
                        }

                        if (!this.lavaNearby()) {
                           return this.retryOrFail(
                              AiFailure.TARGET_NOT_FOUND, "Reached " + this.travelTarget.toShortString() + " but no lava is observed here", null
                           );
                        }

                        this.step = 4;
                        if (this.buildFromUncastablePool) {
                           this.buildFromUncastablePool = false;
                           this.enterPhase(AiNetherEntry.Inner_CachePool.PORTAL);
                           return this.startPortalBuild("no pool in loaded terrain takes a cast");
                        }
                     }

                     if (this.step == 5) {
                        if (this.child == null) {
                           int dx = this.explorationHeading == 0 ? 1 : (this.explorationHeading == 2 ? -1 : 0);
                           int dz = this.explorationHeading == 1 ? 1 : (this.explorationHeading == 3 ? -1 : 0);
                           BlockPos at = mc.player.blockPosition();
                           this.explorationTarget = new BlockPos(at.getX() + dx * 64, at.getY(), at.getZ() + dz * 64);
                           this.legId = "game:goto";
                           this.recordLegEvent(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                           this.child = AiGoto.travelToCompat(
                              this.explorationTarget,
                              0.6,
                              true,
                              true,
                              true,
                              this.cappedTimeout(240),
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              true,
                              2.0,
                              new AiNetherRun.Inner_NetHelper(this.explorationTarget.getX(), this.explorationTarget.getZ(), 8)
                           );
                        }

                        AiOperation.Result r = this.pollChild();
                        if (r == null) {
                           return this.running("Exploring for lava toward " + this.explorationTarget.toShortString());
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.cancelEntry();
                        }

                        this.lavaExplorations++;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.explorationHeading = this.explorationHeading + 1 & 3;
                        }

                        this.step = 1;
                        return this.running("Exploration leg " + this.lavaExplorations + " ended (" + r.status() + "); scanning again");
                     } else {
                        return this.finishPhase("succeeded", "at lava " + (this.travelTarget != null ? this.travelTarget.toShortString() : ""));
                     }
                  }
               }
            }

            AiOperation.Result runPortalPhase() {
               if (this.step >= 20) {
                  return this.mineObsidianStep();
               }

               if (this.step == 0
                  && this.child == null
                  && !this.castPending
                  && this.surfaceForWaterAttempts == 0
                  && this.count("minecraft:water_bucket") == 0
                  && this.count("minecraft:bucket") > 0) {
                  this.enterPhase(AiNetherEntry.Inner_CachePool.LAVA);
                  this.step = 30;
                  return this.running("Filling a water bucket before the cast");
               }

               if (this.portalNearby()) {
                  return this.finishPhase("skipped", "a lit portal is already here");
               }

               if (this.step == 3) {
                  AiOperation.Result r = this.travelTo(this.lavaPool, 240);
                  if (r == null) {
                     return this.running("Walking to the lava pool at " + this.lavaPool.toShortString());
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     AiNetherEntry.markUnreachablePool(this.lavaPool);
                     BlockPos left = this.lavaPool;
                     this.lavaPool = null;
                     return this.poolFailures++ < 3
                        ? this.finishPhase("failed", "no route to the pool at " + left.toShortString())
                        : this.failPhase(AiFailure.UNREACHABLE, "No route to the lava pool at " + left.toShortString() + " (" + r.message() + ")", null);
                  } else {
                     this.step = 0;
                     return this.running("At the lava pool");
                  }
               } else if (this.step != 0) {
                  if (this.step == 1) {
                     if (this.missingMaterials.isEmpty()) {
                        this.step = 0;
                        return this.running("Re-issuing the portal cast");
                     }

                     Entry<String, Integer> need = this.missingMaterials.get(0);
                     if (this.child == null) {
                        this.startSupplyLeg(need.getKey(), need.getValue());
                        return this.running("Obtaining " + need.getKey() + " for the cast");
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Obtaining " + need.getKey() + " for the cast");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && this.count(need.getKey()) <= 0) {
                        return this.failPhase(
                           AiFailure.MISSING_MATERIALS, "Cannot supply " + need.getKey() + " for the portal cast (" + r.message() + ")", this.missingMaterialsReport
                        );
                     }

                     this.missingMaterials.remove(0);
                     return this.running("Supplied " + need.getKey());
                  } else {
                     return this.finishPhase("succeeded", "portal cast and lit");
                  }
               } else {
                  if (this.castAttempts >= 3 && !this.castPending) {
                     return this.canBuildPortalFrame()
                        ? this.startPortalBuild("no cast went in 3 attempts")
                        : this.failPhase(AiFailure.MISSING_MATERIALS, "Portal cast still short of materials after 3 attempts", this.missingMaterialsReport);
                  }

                  if (this.child == null) {
                     if (this.lavaPool == null) {
                        this.lavaPool = this.travelTarget != null
                              && this.travelTargetIsLava
                              && !AiNetherEntry.nearMarkedPool(this.travelTarget)
                              && AiPortalCast.init_Z(mc.level, this.travelTarget)
                           ? this.travelTarget
                           : this.preferredLavaPool();
                     }

                     if (!this.castPending && (this.lavaPool == null || !AiPortalCast.init_Z(mc.level, this.lavaPool)) && this.canBuildPortalFrame()) {
                        this.lavaPool = null;
                        return this.startPortalBuild("no pool in reach takes a cast");
                     }

                     if (this.lavaPool != null && mc.player.blockPosition().distSqr(this.lavaPool) > 16.0) {
                        this.step = 3;
                        return this.running("Walking to the lava pool at " + this.lavaPool.toShortString());
                     }

                     if (!this.castPending) {
                        this.castAttempts++;
                        this.castPending = true;
                     }

                     JsonObject castArgs = AiJson.objectOf("timeoutSeconds", this.uncappedTimeout(300));
                     if (this.lavaPool != null) {
                        castArgs.addProperty("x", this.lavaPool.getX());
                        castArgs.addProperty("y", this.lavaPool.getY());
                        castArgs.addProperty("z", this.lavaPool.getZ());
                     }

                     this.startLeg("game:portal_cast", castArgs);
                     return this.running("Casting the portal (attempt " + this.castAttempts + ")");
                  } else {
                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Casting the portal (attempt " + this.castAttempts + ")");
                     }

                     this.castPending = false;
                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.step = 2;
                        return this.running("Portal cast and lit");
                     }

                     JsonObject miss = AiNetherEntry.parseObject(r.dataJson());
                     if (miss == null) {
                        if (r.status() == AiOperation.Status.TIMED_OUT) {
                           return this.running("Cast timed out; resuming (finished cells are skipped)");
                        } else {
                           BlockPos pool = this.lavaPool != null ? this.lavaPool : this.nearestOpenLava();
                           this.lavaPool = null;
                           AutismClientAddon.LOG
                              .info("[AI] nether entry: cast at the pool {} failed: {}", pool == null ? "?" : pool.toShortString(), r.message());
                           if (this.canBuildPortalFrame()) {
                              return this.startPortalBuild(
                                 "the cast at " + (pool == null ? "the pool" : pool.toShortString()) + " failed (" + r.message() + ")"
                              );
                           } else if (pool != null && this.poolFailures < 3) {
                              this.poolFailures++;
                              AiNetherEntry.markCastFailed(pool);
                              return this.finishPhase("failed", "pool at " + pool.toShortString() + " left (" + r.message() + ")");
                           } else {
                              return this.canBuildPortalFrame()
                                 ? this.startPortalBuild("no pool took a cast (" + r.message() + ")")
                                 : this.failPhase(r.failure(), "Portal cast failed: " + r.message(), null);
                           }
                        }
                     } else {
                        this.missingMaterialsReport = miss;
                        this.missingMaterials.clear();

                        for (Entry<String, JsonElement> e : miss.entrySet()) {
                           this.missingMaterials.add(Map.entry(e.getKey(), Math.max(1, e.getValue().getAsInt())));
                        }

                        this.step = 1;
                        return this.running("Portal cast is missing " + miss + "; obtaining");
                     }
                  }
               }
            }

            AiOperation.Result runEnterPhase() {
               if (this.preparation.requiresReconciliation(null)) {
                  return this.finish(
                     AiOperation.Status.UNCERTAIN, null, "The entry food action was interrupted before its server effects were confirmed"
                  );
               }

               if (AiRunResources.isCampaignActive()
                  && this.child == null
                  && this.foodOp == null
                  && this.waterBucketOp == null
                  && this.spareObsidianOp == null) {
                  if (!AiRunResources.missingMandatoryGear().isEmpty() || !AiRunResources.isMandatoryArmorEquipped()) {
                     return this.failPhase(
                        AiFailure.MISSING_MATERIALS,
                        "Required diamond kit and equipped golden boots are not ready for Nether entry",
                        AiJson.GSON.toJsonTree(AiRunResources.missingMandatoryGear()).getAsJsonObject()
                     );
                  }

                  if (AiRunResources.totalSafeFoodNutrition() < 32) {
                     if (++this.safeFoodChecks > 8) {
                        return this.failPhase(
                           AiFailure.MISSING_MATERIALS,
                           "Nether entry needs carried safe food; raw meat and exhausted hunt attempts do not satisfy the gate",
                           AiJson.objectOf("safeNutrition", 32 - AiRunResources.totalSafeFoodNutrition())
                        );
                     }

                     Map<String, Integer> raw = AiSpeedrun.rawMeatCounts();
                     if (!raw.isEmpty()) {
                        this.foodOp = AiForage.cook(this.cappedTimeout(this.attemptCap(180)));
                     } else {
                        this.foodOp = AiSpeedrun.secureFood(AiRunResources.totalSafeFoodItems() + 12, 180);
                     }

                     return this.running("Securing safe nutrition before Nether entry");
                  }

                  if (mc.player.getHealth() < 16.0F || mc.player.getFoodData().getFoodLevel() < 18) {
                     this.foodOp = mc.player.getFoodData().getFoodLevel() < 18
                        ? AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20))
                        : AiCover.init(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90));
                     return this.running("Restoring health and hunger before Nether entry");
                  }
               }

               if (this.foodOp != null) {
                  AiOperation.Result f = this.preparation.tickChild(this.foodOp);
                  if (!f.terminal()) {
                     return this.running("Food for the Nether: " + f.message());
                  }

                  this.preparation.cancelChild(this.foodOp);
                  this.foodOp = null;
                  if (f.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, f.message());
                  }
               }

               if (this.child == null && this.huntAttempts < 3 && this.safeFoodCount() < 14) {
                  this.huntAttempts++;
                  this.foodOp = this.huntAttempts == 1 && this.skyVisibleWithin(8) ? AiSpeedrun.secureFood(14, 180) : AiSpeedrun.hunt(48.0, 14, 90);
                  if (this.foodOp != null) {
                     return this.running("Food for the Nether");
                  }
               }

               if (this.waterBucketOp != null) {
                  AiOperation.Result w = this.childTracker.tick(this.waterBucketOp);
                  if (!w.terminal()) {
                     return this.running("Filling a water bucket before the Nether: " + w.message());
                  }

                  if (w.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, w.message());
                  }

                  this.childTracker.cancel(this.waterBucketOp);
                  this.waterBucketOp = null;
                  if (w.status() != AiOperation.Status.SUCCEEDED) {
                     this.waterBucketAttempts = 9;
                  }
               }

               if (this.child == null && this.waterBucketAttempts < 4) {
                  this.waterBucketOp = AiForage.init_AiOperation_Minecraft(mc, 32.0, 2);
                  if (this.waterBucketOp != null) {
                     this.waterBucketAttempts++;
                     return this.running("Filling a water bucket before the Nether");
                  }
               }

               if (this.child == null && this.waterBucketOp == null && AiRunResources.isCampaignActive() && AiRunResources.countUsableItem("minecraft:water_bucket", 0) < 2) {
                  if (this.waterBucketAttempts >= 4) {
                     return this.failPhase(
                        AiFailure.MISSING_MATERIALS, "Both water buckets must be refilled before entering the Nether", AiJson.objectOf("minecraft:water_bucket", 2)
                     );
                  }

                  this.waterBucketAttempts++;
                  this.waterBucketOp = AiObsidian.init(2, 120);
                  return this.running("Refilling both mandatory water buckets");
               } else {
                  if (this.spareObsidianOp != null) {
                     BlockPos home = AiDimension.load_BlockPos;
                     if (home != null
                        && AiNetherEntry.shouldAbandonSpareObsidian(
                           this.horizontalDistanceTo(home), this.spareObsidianOp.canInterruptNow(), this.spareObsidianOp.requiresReconciliationAfterEmergency()
                        )) {
                        this.childTracker.cancel(this.spareObsidianOp);
                        this.spareObsidianOp = null;
                        AutismClientAddon.LOG
                           .info(
                              "[AI] nether entry: spare obsidian left at {} ({} blocks from the portal)",
                              this.count("minecraft:obsidian"),
                              (int)this.horizontalDistanceTo(home)
                           );
                        return this.running("Spare obsidian given up; back to the portal");
                     }

                     AiOperation.Result o = this.childTracker.tick(this.spareObsidianOp);
                     if (!o.terminal()) {
                        return this.running("Spare obsidian for the way home (" + this.count("minecraft:obsidian") + "/10)");
                     }

                     if (o.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, o.message());
                     }

                     this.childTracker.cancel(this.spareObsidianOp);
                     this.spareObsidianOp = null;
                     AutismClientAddon.LOG.info("[AI] nether entry: spare obsidian {} ({})", this.count("minecraft:obsidian"), o.message());
                  }

                  if (this.child == null
                     && !this.spareObsidianStarted
                     && !AiNetherEntry.init_Z
                     && this.count("minecraft:obsidian") < 10
                     && this.count("minecraft:water_bucket") > 0
                     && (this.count("minecraft:diamond_pickaxe") > 0 || this.count("minecraft:netherite_pickaxe") > 0)
                     && this.lavaSourceWithin(40)) {
                     this.spareObsidianStarted = true;
                     AiNetherEntry.init_Z = true;
                     this.spareObsidianOp = AiObsidian.load(10, 240);
                     return this.running("Spare obsidian for the way home");
                  }

                  BlockPos lit = AiDimension.load_BlockPos;
                  if (this.child == null && lit != null && this.portalReturnAttempts < 3 && mc.player.blockPosition().distSqr(lit) > 1600.0) {
                     this.portalReturnAttempts++;
                     this.returningToPortal = true;
                     this.portalReturnDone = false;
                     this.legId = "game:goto";
                     this.recordLegEvent(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                     this.child = AiGoto.travelToCompat(
                        lit, 3.0, true, true, true, this.uncappedTimeout(240), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true, 2.0
                     );
                     return this.running("Back to the portal built at " + lit.toShortString());
                  }

                  if (this.child != null && "game:goto".equals(this.legId) && this.returningToPortal && !this.portalReturnDone) {
                     AiOperation.Result back = this.pollChild();
                     if (back == null) {
                        return this.running("Back to the portal built at " + AiDimension.load_BlockPos.toShortString());
                     }

                     if (back.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     this.portalReturnDone = true;
                     if (mc.player.blockPosition().distSqr(AiDimension.load_BlockPos) > 1600.0 && this.portalReturnAttempts < 3) {
                        return this.running("Still far from the portal (" + back.message() + "); walking on");
                     }
                  }

                  if (this.child != null) {
                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Entering the Nether");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                     } else if (this.inNether() && r.status() == AiOperation.Status.SUCCEEDED) {
                        this.captureWarning(r.dataJson());
                        return this.finishPhase("succeeded", "in the Nether" + (this.entryWarning != null ? " (" + this.entryWarning + ")" : ""));
                     } else {
                        return r.status() == AiOperation.Status.CANCELLED
                           ? this.cancelEntry()
                           : this.failPhase(r.failure(), "Dimension travel failed: " + r.message(), null);
                     }
                  } else {
                     if (AiRunResources.isCampaignActive()
                        && !AiNetherEntry.readyToEnterNether(
                           AiRunResources.missingMandatoryGear().isEmpty() && AiRunResources.isMandatoryArmorEquipped(),
                           AiRunResources.totalSafeFoodNutrition(),
                           mc.player.getHealth(),
                           mc.player.getFoodData().getFoodLevel(),
                           AiRunResources.countUsableItem("minecraft:water_bucket", 0)
                        )) {
                        return this.running("Rechecking supplies and health before entering the Nether");
                     }

                     this.startLeg("game:dimension", AiJson.objectOf("action", "enter", "target", "the_nether", "timeoutSeconds", this.uncappedTimeout(180)));
                     return this.running("Entering the Nether");
                  }
               }
            }

            void startLeg(String id, JsonObject legArgs) {
               this.legId = id;
               this.recordLegEvent(AiNetherEntry.legEvent(id, "STARTED", "", null));

               try {
                  AiOperation var10004;
                  switch (id) {
                     case "game:chop":
                        var10004 = AiChop.init(legArgs);
                        break;
                     case "game:obtain":
                        var10004 = AiObtain.init(legArgs);
                        break;
                     case "game:craft":
                        var10004 = AiCraft.craft(legArgs);
                        break;
                     case "game:collect":
                        var10004 = AiCollect.init(legArgs);
                        break;
                     case "game:bucket":
                        var10004 = AiBucket.init(legArgs);
                        break;
                     case "game:oracle":
                        var10004 = AiOracle.create(legArgs);
                        break;
                     case "game:find":
                        var10004 = AiGameplay.sync(legArgs);
                        break;
                     case "game:container":
                        var10004 = AiContainer.create(legArgs);
                        break;
                     case "game:break":
                        var10004 = AiGameOperations.blockAction("break", legArgs);
                        break;
                     case "game:portal_cast":
                        var10004 = AiPortalCast.init(legArgs);
                        break;
                     case "game:dimension":
                        var10004 = AiDimension.init(legArgs);
                        break;
                     default:
                        throw new IllegalArgumentException("No nested leg " + id);
                  }

                  this.child = this.allowLowHealthIfRecovery(id, legArgs, var10004);
               } catch (RuntimeException refused) {
                  this.child = AiGameOperations.immediate(AiOperation.Status.FAILED, "Leg " + id + " refused: " + refused.getMessage(), new JsonObject());
               }

               this.child = AiRunDiagnostics.trace("nether_entry", id + " " + AiRunResources.compactEvidenceJson(legArgs.toString()), this.child);
            }

            AiOperation allowLowHealthIfRecovery(String id, JsonObject legArgs, AiOperation op) {
               return AiGuard.isLowHealthRecoveryOp(id, legArgs) ? AiGuard.allowDuringLowHealth(op) : op;
            }

            void recordLegEvent(JsonObject detail) {
               if (this.legEvents.size() >= 24) {
                  this.legEvents.remove(0);
               }

               this.legEvents.add(detail);
               this.lastLegEvent = detail;
            }

            AiOperation.Result pollChild() {
               AiOperation.Result r = this.childTracker.tick(this.child);
               if (!r.terminal()) {
                  return null;
               }

               this.childTracker.cancel(this.child);
               this.child = null;
               this.recordLegEvent(AiNetherEntry.legEvent(this.legId, r.status().name(), r.message(), r.dataJson()));
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  this.uncertainLegResult = r;
               }

               return r;
            }

            AiOperation.Result obtainItem(String item, int amount, int cap) {
               return this.obtainItem(item, amount, cap, false);
            }

            AiOperation.Result obtainItemUncapped(String item, int amount, int cap) {
               return this.obtainItem(item, amount, cap, true);
            }

            AiOperation.Result obtainItem(String item, int amount, int cap, boolean uncapped) {
               if (this.child == null) {
                  this.startLeg(
                     "game:obtain", AiJson.objectOf("item", item, "count", amount, "timeoutSeconds", uncapped ? this.uncappedTimeout(cap) : this.cappedTimeout(cap))
                  );
               }

               return this.pollChild();
            }

            AiOperation.Result craftItem(String item, int cap) {
               return this.craftItem(item, cap, false);
            }

            AiOperation.Result craftItemUncapped(String item, int cap) {
               return this.craftItem(item, cap, true);
            }

            AiOperation.Result craftItem(String item, int cap, boolean uncapped) {
               if (this.child == null) {
                  this.startLeg("game:craft", AiJson.objectOf("item", item, "count", 1, "timeoutSeconds", uncapped ? this.uncappedTimeout(cap) : this.cappedTimeout(cap)));
               }

               return this.pollChild();
            }

            AiOperation.Result findBlocks(String block, int limit, int radius) {
               if (this.child == null) {
                  this.startLeg("game:find", AiJson.objectOf("block", block, "radius", radius, "limit", limit));
               }

               return this.pollChild();
            }

            AiOperation.Result locate(String query) {
               if (this.child == null) {
                  this.startLeg("game:oracle", AiJson.objectOf("query", query, "cap", 1));
               }

               return this.pollChild();
            }

            AiOperation.Result walkTo(BlockPos target, double radius, int cap) {
               return this.walkTo(target, radius, cap, false);
            }

            AiOperation.Result walkToUncapped(BlockPos target, double radius, int cap) {
               return this.walkTo(target, radius, cap, true);
            }

            AiOperation.Result walkTo(BlockPos target, double radius, int cap, boolean uncapped) {
               if (this.child == null) {
                  this.legId = "game:goto";
                  this.recordLegEvent(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                  this.child = AiGoto.travelToCompat(target, radius, false, true, true, uncapped ? this.uncappedTimeout(cap) : this.cappedTimeout(cap));
               }

               return this.pollChild();
            }

            AiOperation.Result travelTo(BlockPos target, int cap) {
               if (this.child == null) {
                  this.legId = "game:goto";
                  this.recordLegEvent(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                  this.child = AiGoto.travelToCompat(target, 3.0, true, true, true, this.cappedTimeout(cap), autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST);
               }

               return this.pollChild();
            }

            AiOperation.Result breakBlockAt(BlockPos pos) {
               if (this.child == null && this.lootDelayTicks > 0) {
                  this.lootDelayTicks--;
                  return null;
               }

               if (this.child == null) {
                  this.startLeg("game:break", AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
               }

               return this.pollChild();
            }

            void drainChild() {
               if (this.child != null) {
                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     this.interruptChild("drained at a step boundary");
                  }
               }
            }

            void startSupplyLeg(String itemId, int amount) {
               int cap = this.cappedTimeout(120);
               switch (AiNetherEntry.obtainMethodFor(itemId)) {
                  case "fill_water":
                     this.startLeg("game:bucket", AiJson.objectOf("action", "fill", "fluid", "water", "timeoutSeconds", cap));
                     break;
                  case "fill_lava":
                     this.startLeg("game:bucket", AiJson.objectOf("action", "fill", "fluid", "lava", "timeoutSeconds", cap));
                     break;
                  case "collect":
                     this.startLeg("game:collect", AiJson.objectOf("item", itemId, "count", amount, "radius", 32, "timeoutSeconds", cap));
                     break;
                  default:
                     this.startLeg("game:obtain", AiJson.objectOf("item", itemId, "count", amount, "timeoutSeconds", cap));
               }
            }

            void cancelChild() {
               if (this.child != null) {
                  this.childTracker.cancel(this.child);
                  this.child = null;
               }
            }

            void interruptChild(String why) {
               if (this.child != null) {
                  this.childTracker.cancel(this.child);
                  this.child = null;
                  this.recordLegEvent(AiNetherEntry.legEvent(this.legId, "CANCELLED", why, null));
               }
            }

            int count(String itemId) {
               return AiCraft.countItem(itemId);
            }

            int logCount() {
               return AiItems.countInInventory(logTarget);
            }

            int safeFoodCount() {
               return AiRunResources.totalSafeFoodItems();
            }

            int bedCount() {
               return AiItems.countInInventory(bedTarget);
            }

            int bucketCount() {
               return this.count("minecraft:bucket") + this.count("minecraft:water_bucket") + this.count("minecraft:lava_bucket");
            }

            boolean inNether() {
               return mc.level != null && "minecraft:the_nether".equals(mc.level.dimension().identifier().toString());
            }

            boolean inOverworld() {
               return mc.level != null && "minecraft:overworld".equals(mc.level.dimension().identifier().toString());
            }

            long overworldDay() {
               return mc.level.getOverworldClockTime() / 24000L;
            }

            boolean hasStoneTier() {
               return AiProgression.bestTier(mc).ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal();
            }

            boolean needsFood() {
               return !this.foodDone && AiNetherEntry.needsFood(mc.player.getFoodData().getFoodLevel(), this.safeFoodCount());
            }

            boolean hasIronFireGear() {
               return AiNetherEntry.hasIronFireGear(
                  this.count("minecraft:iron_ingot"), this.count("minecraft:flint"), this.bucketCount(), this.count("minecraft:flint_and_steel"), minIron
               );
            }

            boolean needsVillage() {
               return !this.villageDone && !skipVillage && this.bedCount() < bedsTarget
                  ? explicitVillage == null || AiNetherEntry.shouldVisitVillage(false, this.cornerDistanceTo(explicitVillage), this.bedCount(), bedsTarget)
                  : false;
            }

            double cornerDistanceTo(BlockPos pos) {
               double dx = pos.getX() - mc.player.getX();
               double dz = pos.getZ() - mc.player.getZ();
               return Math.hypot(dx, dz);
            }

            boolean lavaNearby() {
               BlockPos origin = mc.player.blockPosition();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.isExposedLavaSource(mc.level, pos) && !AiNetherEntry.nearMarkedPool(pos)) {
                     return true;
                  }
               }

               return false;
            }

            boolean canBuildPortalFrame() {
               return !this.portalBuildStarted
                  && (this.count("minecraft:diamond_pickaxe") > 0 || this.count("minecraft:netherite_pickaxe") > 0 || this.count("minecraft:obsidian") >= 10);
            }

            AiOperation.Result startPortalBuild(String why) {
               this.portalBuildStarted = true;
               this.drainChild();
               this.step = 20;
               AutismClientAddon.LOG.info("[AI] nether entry: building the portal from obsidian: {}", why);
               return this.running("Building the portal from obsidian: " + why);
            }

            AiOperation.Result mineObsidianStep() {
               if (this.step == 20) {
                  int framed = AiDimension.framedObsidian();
                  if (this.child == null && this.count("minecraft:obsidian") + framed >= 10) {
                     this.step = 21;
                     return this.running(framed > 0 ? "Obsidian for the frame begun at " + AiDimension.frameOrigin.toShortString() : "Ten obsidian held");
                  }

                  if (this.child == null) {
                     this.legId = "game:obtain";
                     this.recordLegEvent(AiNetherEntry.legEvent("game:obtain", "STARTED", "", null));
                     this.obsidianBeforeMining = this.count("minecraft:obsidian");
                     this.child = AiObsidian.load(Math.max(1, 10 - AiDimension.framedObsidian()), this.uncappedTimeout(300));
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.running("Mining obsidian for the frame (" + this.count("minecraft:obsidian") + "/10)");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  }

                  int held = this.count("minecraft:obsidian");
                  held += AiDimension.framedObsidian();
                  if (held < 10 && held - AiDimension.framedObsidian() > this.obsidianBeforeMining && this.obsidianShortfallRetries < 4) {
                     this.obsidianShortfallRetries++;
                     AutismClientAddon.LOG.info("[AI] nether entry: obsidian {} of 10 ({}); casting on", held, r.message());
                     return this.running("Obsidian " + held + "/10; casting on");
                  }

                  if (held < 10 && !this.castablePoolOnly) {
                     this.castablePoolOnly = true;
                     this.castAttempts = 0;
                     this.poolFailures = 0;
                     AutismClientAddon.LOG.info("[AI] nether entry: no obsidian ({}); looking for a castable pool from the surface", r.message());
                     this.enterPhase(AiNetherEntry.Inner_CachePool.LAVA);
                     this.step = 40;
                     return this.running("No obsidian (" + r.message() + "); up to the surface for a castable pool");
                  }

                  if (held < 10) {
                     return this.failPhase(AiFailure.MISSING_MATERIALS, "Obsidian for the frame: " + r.message(), AiNetherEntry.missingItem("minecraft:obsidian", 10 - held));
                  }

                  this.step = 21;
                  return this.running("Ten obsidian held");
               } else if (this.step == 21) {
                  BlockPos begun = AiDimension.framedObsidian() > 0 ? AiDimension.frameOrigin : null;
                  if (begun != null && this.child == null && mc.player.blockPosition().distSqr(begun) <= 16.0) {
                     this.step = 22;
                     return this.running("At the frame begun");
                  }

                  if (begun != null) {
                     if (this.child == null) {
                        this.legId = "game:goto";
                        this.recordLegEvent(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                        this.child = AiGoto.travelToCompat(
                           begun,
                           3.0,
                           true,
                           true,
                           true,
                           this.uncappedTimeout(240),
                           autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                           true,
                           2.0,
                           AiGoto.protectingCellsCompat(
                              autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(begun, 3.0), AiDimension.frameCells(begun, AiDimension.frameAxis)
                           )
                        );
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Back to the frame begun at " + begun.toShortString());
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     this.step = 22;
                     return this.running("Building the frame begun");
                  } else {
                     if (this.child == null && this.skyVisibleWithin(3)) {
                        this.step = 22;
                        return this.running("Under open sky");
                     }

                     if (this.child == null && !this.entryTimer.isStarted() && mc.player.onGround() && this.canInterruptNow()) {
                        this.entryTimer.start(this.deadline);
                        this.step = 23;
                        return this.running("Trying a portal frame near the pool");
                     }

                     if (this.child == null) {
                        this.legId = "game:surface";
                        this.recordLegEvent(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                        this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.uncappedTimeout(240)));
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Up to open ground for the frame");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     }

                     this.step = 22;
                     return this.running("Building the frame here");
                  }
               } else if (this.step == 23) {
                  if (this.findBlock(Blocks.NETHER_PORTAL, 8, 6) == null
                     || this.child != null
                        && (!this.child.canInterruptNow() || this.child.requiresReconciliationAfterEmergency())) {
                     if (this.child == null) {
                        int seconds = this.entryTimer.secondsLeft(this.deadline);
                        if (seconds == 0) {
                           this.step = AiDimension.framedObsidian() > 0 ? 22 : 21;
                           return this.running(this.step == 22 ? "Finishing the local frame begun" : "Local frame budget exhausted; up to open ground");
                        }

                        this.startLeg(
                           "game:dimension", AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", seconds)
                        );
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.running("Building the local portal frame");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return r;
                     } else if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.cancelEntry();
                     } else if (this.findBlock(Blocks.NETHER_PORTAL, 8, 6) != null) {
                        return this.finishPhase("succeeded", "portal built locally from obsidian");
                     } else if (AiDimension.framedObsidian() > 0) {
                        this.step = 22;
                        return this.running("Finishing the local frame begun");
                     } else if (AiNetherEntry.shouldRetryBuildInOpen(r, 0, this.childTracker.requiresReconciliation())) {
                        this.step = 21;
                        return this.running("No local frame completed (" + r.message() + "); up to open ground");
                     } else {
                        return this.failPhase(r.failure(), "Local portal build failed: " + r.message(), null);
                     }
                  } else {
                     this.drainChild();
                     return this.finishPhase("succeeded", "portal built locally from obsidian");
                  }
               } else if (this.portalScanCounter++ % 5 != 0
                  || this.findBlock(Blocks.NETHER_PORTAL, 8, 6) == null
                  || this.child != null
                     && (!this.child.canInterruptNow() || this.child.requiresReconciliationAfterEmergency())) {
                  if (this.child == null) {
                     this.startLeg(
                        "game:dimension",
                        AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", this.uncappedTimeout(300))
                     );
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.running("Building the portal frame");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.cancelEntry();
                  } else {
                     return this.findBlock(Blocks.NETHER_PORTAL, 8, 6) != null
                        ? this.finishPhase("succeeded", "portal built from obsidian")
                        : this.failPhase(r.failure(), "Portal build failed: " + r.message(), null);
                  }
               } else {
                  this.drainChild();
                  return this.finishPhase("succeeded", "portal built from obsidian");
               }
            }

            AiOperation.Result fetchWaterStep() {
               if (this.child == null && this.count("minecraft:water_bucket") > 0) {
                  this.step = 0;
                  return this.running("Water bucket held");
               }

               if (this.child == null) {
                  if (this.step == 30 && !this.skyVisibleWithin(3) && !this.waterSourceWithin(48)) {
                     this.step = 31;
                  }

                  this.surfaceForWaterAttempts++;
                  if (this.step == 31) {
                     this.legId = "game:surface";
                     this.recordLegEvent(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                     this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.uncappedTimeout(240)));
                  } else {
                     this.legId = "game:bucket";
                     this.recordLegEvent(AiNetherEntry.legEvent("game:bucket", "STARTED", "", null));
                     this.child = AiObsidian.fetchOne(this.uncappedTimeout(240));
                  }
               }

               AiOperation.Result r = this.pollChild();
               if (r == null) {
                  return this.running(this.step == 31 ? "Up to open ground for water" : "Filling a water bucket");
               } else if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.cancelEntry();
               } else if (this.count("minecraft:water_bucket") > 0) {
                  this.step = 0;
                  return this.running("Water bucket held");
               } else if (this.step == 30 && this.surfaceForWaterAttempts < 3) {
                  this.step = 31;
                  return this.running("No water reachable here (" + r.message() + "); up to the surface");
               } else if (this.step == 31) {
                  this.step = 30;
                  return this.running("Under open sky; looking for water");
               } else {
                  this.step = 0;
                  return this.running("No water bucket filled: " + r.message());
               }
            }

            AiOperation.Result surfaceStep() {
               if (this.child == null && this.skyVisibleWithin(3)) {
                  this.step = 1;
                  return this.running("Under open sky; scanning for a castable pool");
               }

               if (this.child == null) {
                  this.legId = "game:surface";
                  this.recordLegEvent(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                  this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.uncappedTimeout(240)));
               }

               AiOperation.Result r = this.pollChild();
               if (r == null) {
                  return this.running("Up to the surface for a castable pool");
               }

               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.cancelEntry();
               }

               this.step = 1;
               return this.running("Scanning for a castable pool");
            }

            BlockPos castableLavaPool() {
               BlockPos found = this.preferredLavaPool();
               return found != null && AiPortalCast.init_Z(mc.level, found) ? found : null;
            }

            boolean waterSourceWithin(int radius) {
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos pos : BlockPos.betweenClosed(feet.offset(-radius, -radius / 2, -radius), feet.offset(radius, radius / 2, radius))) {
                  if (AiTerrain.isLoaded(mc.level, pos)) {
                     FluidState fluid = mc.level.getFluidState(pos);
                     if (fluid.isSource() && fluid.is(FluidTags.WATER) && mc.level.getBlockState(pos.above()).isAir()) {
                        return true;
                     }
                  }
               }

               return false;
            }

            boolean skyVisibleWithin(int radius) {
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

            BlockPos preferredLavaPool() {
               BlockPos origin = mc.player.blockPosition();
               List<BlockPos> open = new ArrayList<>();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.isExposedLavaSource(mc.level, pos) && !AiNetherEntry.nearMarkedPool(pos)) {
                     open.add(pos.immutable());
                  }
               }

               if (open.isEmpty()) {
                  return null;
               }

               open.sort(Comparator.comparingDouble(origin::distSqr));
               List<BlockPos> checked = new ArrayList<>();

               for (BlockPos pos : open) {
                  if (checked.size() >= 12) {
                     break;
                  }

                  if (!checked.stream().anyMatch(c -> c.distSqr(pos) <= 36.0)) {
                     checked.add(pos);
                     if (AiPortalCast.init_Z(mc.level, pos)) {
                        return pos;
                     }
                  }
               }

               return open.get(0);
            }

            BlockPos nearestOpenLava() {
               BlockPos origin = mc.player.blockPosition();
               BlockPos best = null;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos)
                     && AiWorldScan.isExposedLavaSource(mc.level, pos)
                     && !AiNetherEntry.nearMarkedPool(pos)
                     && (best == null || origin.distSqr(pos) < origin.distSqr(best))) {
                     best = pos.immutable();
                  }
               }

               return best;
            }

            boolean portalNearby() {
               return this.findBlock(Blocks.NETHER_PORTAL, 48, 16) != null;
            }

            BlockPos findBlock(Block block, int horizontal, int vertical) {
               BlockPos origin = mc.player.blockPosition();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-horizontal, -vertical, -horizontal), origin.offset(horizontal, vertical, horizontal))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == block) {
                     return pos.immutable();
                  }
               }

               return null;
            }

            void captureWarning(String dataJson) {
               try {
                  JsonElement parsed = JsonParser.parseString(dataJson == null ? "{}" : dataJson);
                  if (parsed.isJsonObject() && parsed.getAsJsonObject().has("warning")) {
                     this.entryWarning = parsed.getAsJsonObject().get("warning").getAsString();
                  }
               } catch (RuntimeException var3) {
               }
            }

            void enterPhase(AiNetherEntry.Inner_CachePool next) {
               this.phase = next;
               this.phaseStartedMillis = System.currentTimeMillis();
               this.step = 0;
               this.attempt = 1;
               this.openingSiteIndex = 0;
               this.chests = List.of();
               this.chestIndex = 0;
               this.missingMaterials.clear();

               while (this.legEvents.size() > 0) {
                  this.legEvents.remove(0);
               }

               AiRunTrace.recordGlobalSplit("nether_entry_phase", next.name().toLowerCase(Locale.ROOT));
            }

            void tracePhaseFailure(AiNetherEntry.Inner_CachePool p, String reason) {
               AiRunTrace.recordGlobalSplit(
                  "nether_entry_phase", "FAILED " + p.name().toLowerCase(Locale.ROOT) + ": " + (reason.length() > 80 ? reason.substring(0, 80) : reason)
               );
            }

            JsonObject phaseEntry(AiNetherEntry.Inner_CachePool p, String status, String detail) {
               JsonObject out = AiJson.objectOf(
                  "name",
                  p.name().toLowerCase(Locale.ROOT),
                  "status",
                  status,
                  "step",
                  this.step,
                  "attempts",
                  this.attempt,
                  "elapsedMs",
                  System.currentTimeMillis() - this.phaseStartedMillis
               );
               if (!detail.isEmpty()) {
                  out.addProperty("detail", detail);
               }

               if (this.legEvents.size() > 0) {
                  out.add("legs", this.legEvents.deepCopy());
               }

               return out;
            }

            AiOperation.Result finishPhase(String status, String detail) {
               this.drainChild();
               this.phaseHistory.add(this.phaseEntry(this.phase, status, detail));
               if ("failed".equals(status)) {
                  this.tracePhaseFailure(this.phase, detail);
               }

               AiNetherEntry.Inner_CachePool finished = this.phase;
               this.phase = null;
               return this.running("Phase " + finished.name().toLowerCase(Locale.ROOT) + " " + status + ": " + detail);
            }

            AiOperation.Result retryOrFail(AiFailure failure, String reason, JsonObject miss) {
               if (this.attempt < AiNetherEntry.maxAttempts(this.phase) && AiNetherEntry.canRetryPhase(this.phase, failure)) {
                  this.attempt++;
                  this.restartPhase();
                  return this.running(
                     "Phase "
                        + this.phase.name().toLowerCase(Locale.ROOT)
                        + ": attempt "
                        + this.attempt
                        + "/"
                        + AiNetherEntry.maxAttempts(this.phase)
                        + " after: "
                        + reason
                  );
               } else {
                  return this.failPhase(failure, reason, miss);
               }
            }

            void restartPhase() {
               this.step = 0;
               if (this.phase == AiNetherEntry.Inner_CachePool.OPENING) {
                  this.openingSiteIndex++;
               }
            }

            int attemptCap(int baseCap) {
               return AiNetherEntry.scaledCap(baseCap, this.attempt);
            }

            boolean isLateAttempt() {
               return this.attempt >= 3;
            }

            AiOperation.Result failPhase(AiFailure failure, String reason, JsonObject miss) {
               this.drainChild();
               this.phaseHistory.add(this.phaseEntry(this.phase, "failed", reason));
               this.tracePhaseFailure(this.phase, reason);
               if (miss != null) {
                  this.missingMaterialsReport = miss;
               }

               if (!AiNetherEntry.isRequiredPhase(this.phase)) {
                  AiNetherEntry.Inner_CachePool failedPhase = this.phase;
                  this.phase = null;
                  return this.running("Optional phase " + failedPhase.name().toLowerCase(Locale.ROOT) + " failed: " + reason);
               } else {
                  this.failedPhaseName = this.phase.name().toLowerCase(Locale.ROOT);
                  this.phase = null;
                  return this.finish(AiOperation.Status.FAILED, failure, reason);
               }
            }

            AiOperation.Result completeEntry(String message) {
               if (this.phase != null) {
                  this.phaseHistory.add(this.phaseEntry(this.phase, "succeeded", ""));
                  this.phase = null;
               }

               AiRunTrace.recordGlobalSplit("nether_entered", mc.player.blockPosition().toShortString());
               return this.finish(AiOperation.Status.SUCCEEDED, null, message);
            }

            AiOperation.Result cancelEntry() {
               return this.finish(AiOperation.Status.CANCELLED, null, "Nether entry cancelled");
            }

            JsonObject missingFromResult(AiOperation.Result r, String itemId) {
               JsonObject legMissing = AiNetherEntry.parseObject(r.dataJson());
               return legMissing != null ? legMissing : AiNetherEntry.missingItem(itemId, 1);
            }

            int cappedTimeout(int cap) {
               return AiNetherEntry.clampCappedTimeout(cap, this.secondsLeft());
            }

            int uncappedTimeout(int cap) {
               return AiNetherEntry.clampUncappedTimeout(cap, this.secondsLeft());
            }

            int secondsLeft() {
               return (int)Math.max(1L, (this.deadline - System.nanoTime()) / 1000000000L);
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf("timeoutSeconds", timeoutSeconds, "elapsedMs", System.currentTimeMillis() - this.startMillis);
               JsonArray reports = this.phaseHistory.deepCopy();
               if (this.phase != null) {
                  reports.add(this.phaseEntry(this.phase, "running", ""));
               }

               out.add("phases", reports);
               out.addProperty(
                  "phase",
                  this.failedPhaseName != null
                     ? this.failedPhaseName
                     : (this.phase != null ? this.phase.name().toLowerCase(Locale.ROOT) : "done")
               );
               out.addProperty("step", this.step);
               if (this.inNether()) {
                  BlockPos pos = mc.player.blockPosition();
                  out.add("netherPosition", AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
               }

               JsonObject bag = new JsonObject();
               this.putPositive(bag, "logs", this.logCount());
               this.putPositive(bag, "wooden_pickaxe", this.count("minecraft:wooden_pickaxe"));
               this.putPositive(bag, "stone_pickaxe", this.count("minecraft:stone_pickaxe"));
               this.putPositive(bag, "stone_axe", this.count("minecraft:stone_axe"));
               this.putPositive(bag, "stone_sword", this.count("minecraft:stone_sword"));
               this.putPositive(bag, "iron_ingot", this.count("minecraft:iron_ingot"));
               this.putPositive(bag, "buckets", this.bucketCount());
               this.putPositive(bag, "water_bucket", this.count("minecraft:water_bucket"));
               this.putPositive(bag, "lava_bucket", this.count("minecraft:lava_bucket"));
               this.putPositive(bag, "flint", this.count("minecraft:flint"));
               this.putPositive(bag, "flint_and_steel", this.count("minecraft:flint_and_steel"));
               this.putPositive(bag, "beds", this.bedCount());
               this.putPositive(bag, "food", this.safeFoodCount());
               this.putPositive(bag, "throwaway", this.count("minecraft:dirt") + this.count("minecraft:cobblestone"));
               out.add("inventory", bag);
               JsonObject survival = AiGuard.statusToJson(AiGuard.sampleStatus(), this.interruptions, this.guardReason);
               if (!this.suppressedEatReason.isEmpty()) {
                  survival.addProperty("suppressedEat", this.suppressedEatReason);
               }

               out.add("survival", survival);
               if (this.missingMaterialsReport != null) {
                  out.add("missing", this.missingMaterialsReport);
               }

               if (this.lastLegEvent != null) {
                  out.add("leg", this.lastLegEvent);
               }

               if (this.preparation.lastChildResult() != null) {
                  out.add("entryFoodChild", AiRunResources.compactEvidenceJson(this.preparation.lastChildResult().dataJson()));
                  out.addProperty("entryFoodChildStatus", this.preparation.lastChildResult().status().name());
               }

               if (this.entryWarning != null) {
                  out.addProperty("warning", this.entryWarning);
               }

               this.childTracker.appendEvidence(out);
               return out;
            }

            void putPositive(JsonObject bag, String key, int value) {
               if (value > 0) {
                  bag.addProperty(key, value);
               }
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            AiOperation.Result finish(AiOperation.Status status, AiFailure failure, String message) {
               this.childTracker
                  .observeAll(this.child, this.waterBucketOp, this.spareObsidianOp, this.guardOp, this.nightOp);
               JsonObject data = this.progressJson();
               AiOperation.Result result = failure == null
                  ? new AiOperation.Result(status, message, data.toString(), List.of())
                  : new AiOperation.Result(status, message, data.toString(), List.of(), failure);
               result = this.preparation.finalizeResult(result, this.preparation.requiresReconciliation(this.foodOp));
               result = this.childTracker.withEvidence(result, failure);
               this.cancel();
               return result;
            }

            void cancelSideOps() {
               if (this.foodOp != null) {
                  this.preparation.cancelChild(this.foodOp);
                  this.foodOp = null;
               }

               if (this.waterBucketOp != null) {
                  this.childTracker.cancel(this.waterBucketOp);
                  this.waterBucketOp = null;
               }

               if (this.spareObsidianOp != null) {
                  this.childTracker.cancel(this.spareObsidianOp);
                  this.spareObsidianOp = null;
               }
            }

            @Override
            public void cancel() {
               this.childTracker
                  .observeAll(this.child, this.waterBucketOp, this.spareObsidianOp, this.guardOp, this.nightOp);
               if (this.foodOp != null) {
                  this.preparation.cancelChild(this.foodOp);
                  this.foodOp = null;
               }

               if (!this.finishRecorded) {
                  this.finishRecorded = true;
                  this.cancelChild();
                  if (this.waterBucketOp != null) {
                     this.childTracker.cancel(this.waterBucketOp);
                     this.waterBucketOp = null;
                  }

                  if (this.spareObsidianOp != null) {
                     this.childTracker.cancel(this.spareObsidianOp);
                     this.spareObsidianOp = null;
                  }

                  if (this.guardOp != null) {
                     this.childTracker.cancel(this.guardOp);
                     this.guardOp = null;
                  }

                  if (this.nightOp != null) {
                     this.childTracker.cancel(this.nightOp);
                     this.nightOp = null;
                  }
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.childTracker.requiresReconciliation()
                  && !this.preparation.requiresReconciliation(this.foodOp)
                  && (this.child == null || this.child.canInterruptNow())
                  && (this.foodOp == null || this.foodOp.canInterruptNow())
                  && (this.waterBucketOp == null || this.waterBucketOp.canInterruptNow())
                  && (this.spareObsidianOp == null || this.spareObsidianOp.canInterruptNow())
                  && (this.guardOp == null || this.guardOp.canInterruptNow())
                  && (this.nightOp == null || this.nightOp.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.childTracker.requiresReconciliation()
                  || this.child != null && this.child.requiresReconciliationAfterEmergency()
                  || this.preparation.requiresReconciliation(this.foodOp)
                  || this.waterBucketOp != null && this.waterBucketOp.requiresReconciliationAfterEmergency()
                  || this.spareObsidianOp != null && this.spareObsidianOp.requiresReconciliationAfterEmergency()
                  || this.guardOp != null && this.guardOp.requiresReconciliationAfterEmergency()
                  || this.nightOp != null && this.nightOp.requiresReconciliationAfterEmergency();
            }
         };
      } else {
         throw new IllegalArgumentException("Nether entry requires a connected living player");
      }
   }

   enum Inner_CachePool {
      OPENING,
      FOOD,
      VILLAGE,
      IRON_FIRE,
      LAVA,
      PORTAL,
      ENTER;
   }

   static final class Inner_DataUtil {
      private final IdentityHashMap<AiOperation, AiOperation.Result> results = new IdentityHashMap<>();
      private AiOperation owner;
      private AiOperation.Result ownerResult;
      private AiOperation.Result pendingChildResult;
      private boolean needsReconciliation;

      AiOperation.Result tick(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result actual = child.tick();
         if (actual.terminal()) {
            boolean needsObservation = child.requiresReconciliationAfterEmergency()
               || actual.status() != AiOperation.Status.SUCCEEDED && before
               || evidenceRequestsReobservation(actual);
            actual = AiGuardLayer.wrapPendingTermination(needsObservation, actual);
            this.recordChild(child, needsObservation, actual);
         }

         this.results.put(child, actual);
         this.ownerResult = actual;
         return actual;
      }

      private static boolean evidenceRequestsReobservation(AiOperation.Result actual) {
         if (actual == null) {
            return false;
         }

         JsonObject data = AiRunResources.parseEvidenceJson(actual.dataJson());
         return AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false) || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
      }

      private void recordChild(AiOperation owner, boolean needsObservation, AiOperation.Result actual) {
         if (needsObservation || evidenceRequestsReobservation(actual)) {
            this.needsReconciliation = true;
            if (this.owner == null) {
               this.owner = owner;
            }

            if (owner == this.owner && actual != null) {
               this.pendingChildResult = actual;
            }
         }
      }

      void observeAll(AiOperation... children) {
         for (AiOperation child : children) {
            if (child != null) {
               this.recordChild(child, child.requiresReconciliationAfterEmergency(), this.results.get(child));
            }
         }
      }

      void cancel(AiOperation child) {
         if (child != null) {
            this.observeAll(child);
            child.cancel();
            this.results.remove(child);
         }
      }

      boolean requiresReconciliation() {
         return this.needsReconciliation;
      }

      void appendEvidence(JsonObject data) {
         if (this.ownerResult != null) {
            data.add("entryChild", AiRunResources.compactEvidenceJson(this.ownerResult.dataJson()));
            data.addProperty("entryChildStatus", this.ownerResult.status().name());
         }

         if (this.pendingChildResult != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.pendingChildResult.dataJson()));
            data.addProperty("pendingChildStatus", this.pendingChildResult.status().name());
            data.addProperty("pendingChildFailure", this.pendingChildResult.failure().name());
         }
      }

      AiOperation.Result withEvidence(AiOperation.Result boundary, AiFailure failure) {
         JsonObject data = AiRunResources.parseEvidenceJson(boundary.dataJson());
         this.appendEvidence(data);
         AiOperation.Result actual = this.ownerResult != null ? this.ownerResult : this.pendingChildResult;
         AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(boundary.status(), boundary.message(), data, this.needsReconciliation, actual);
         return new AiOperation.Result(
            result.status(),
            result.message(),
            result.dataJson(),
            actual == null ? boundary.evidence() : actual.evidence(),
            failure != null ? failure : (actual == null ? boundary.failure() : actual.failure())
         );
      }
   }

   static final class Inner_StreamUtil {
      static final int ENTRY_WINDOW_SECONDS = 90;
      static final int MIN_REPORTED_SECONDS = 10;
      private final LongSupplier clock;
      private long deadline;
      private boolean started;

      Inner_StreamUtil() {
         this(System::nanoTime);
      }

      Inner_StreamUtil(LongSupplier now) {
         this.clock = now;
      }

      boolean isStarted() {
         return this.started;
      }

      void start(long entryDeadline) {
         if (!this.started) {
            this.started = true;
            this.deadline = Math.min(entryDeadline, this.clock.getAsLong() + 90000000000L);
         }
      }

      int secondsLeft(long entryDeadline) {
         int seconds = this.started ? (int)Math.max(0L, (Math.min(this.deadline, entryDeadline) - this.clock.getAsLong()) / 1000000000L) : 0;
         return seconds >= 10 ? seconds : 0;
      }
   }
}
