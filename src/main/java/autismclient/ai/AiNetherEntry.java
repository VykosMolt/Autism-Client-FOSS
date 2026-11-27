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
   static final int tick = 360;
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
   static final int size = 14;
   static volatile boolean init_Z;
   static final int data = 12;
   static final int value = 3;
   static final int cache = 4;
   static final int state = 8;
   static final int queue = 8;
   static final int buffer = 20;
   static final long NIGHT_START_TICK = 12500L;
   static final long NIGHT_END_TICK = 23460L;
   private static final Map<BlockPos, Long> init_Map = new ConcurrentHashMap<>();
   private static final long MARK_TTL_NANOS = 600000000000L;
   private static final Map<BlockPos, Long> load_Map = new ConcurrentHashMap<>();

   private AiNetherEntry() {
   }

   static boolean hasStonePickaxe(int stonePickaxes) {
      return stonePickaxes >= 1;
   }

   static boolean init(boolean openingDone, boolean legActive) {
      return openingDone && !legActive;
   }

   static boolean load(boolean nearby, boolean legActive) {
      return nearby && !legActive;
   }

   static boolean needsFood(int foodLevel, int foodItems) {
      return foodLevel <= 10 || foodItems < 6;
   }

   static boolean init_Z_D(double distance, boolean canInterrupt, boolean requiresReconciliation) {
      return distance > 40.0 && canInterrupt && !requiresReconciliation;
   }

   static boolean hasIronFireGear(int ironIngots, int flint, int buckets, int flintAndSteel, int minIron) {
      return buckets >= 1 && flintAndSteel >= 1;
   }

   static int ironStillNeeded(int ironIngots, int buckets, int flintAndSteel) {
      int need = (buckets >= 1 ? 0 : 3) + (flintAndSteel >= 1 ? 0 : 1);
      return Math.max(0, need - ironIngots);
   }

   static boolean init_Z_Z(boolean gearReady, int nutrition, float health, int hunger, int waterBuckets) {
      return gearReady && nutrition >= 32 && health >= 16.0F && hunger >= 18 && waterBuckets >= 2;
   }

   static void init_V(BlockPos pos) {
      init_Map.put(pos.immutable(), System.nanoTime());
   }

   static void load(BlockPos pos) {
      load_Map.put(pos.immutable(), System.nanoTime());
   }

   static boolean init_Z(BlockPos pos) {
      long now = System.nanoTime();
      init_Map.values().removeIf(at -> now - at > 600000000000L);
      load_Map.values().removeIf(at -> now - at > 600000000000L);

      for (BlockPos bad : init_Map.keySet()) {
         if (bad.distSqr(pos) <= 36.0) {
            return true;
         }
      }

      for (BlockPos bad : load_Map.keySet()) {
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

   static boolean init(long dayTime, boolean overworld, boolean nightActive, long handledDay, long dayIndex, long retryAtMs, long nowMs) {
      if (!overworld || nightActive) {
         return false;
      } else if (dayTime >= 12500L && dayTime <= 23460L) {
         return handledDay == dayIndex ? false : nowMs >= retryAtMs;
      } else {
         return false;
      }
   }

   static AiNetherEntry.Inner_CachePool init(
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

   static boolean init_Z(AiNetherEntry.Inner_CachePool phase) {
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

   static boolean init(AiNetherEntry.Inner_CachePool phase, AiFailure failure) {
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

   static boolean init_Z_AiOperation_Result(AiOperation.Result result, int framed, boolean pending) {
      return !pending && framed == 0 && result != null && (result.status() == AiOperation.Status.FAILED || result.status() == AiOperation.Status.TIMED_OUT);
   }

   static AiFailure init(AiOperation.Result r) {
      return r.status() == AiOperation.Status.TIMED_OUT ? AiFailure.TIMEOUT : AiFailure.MISSING_MATERIALS;
   }

   static BlockPos init(List<BlockPos> sites, int attemptIndex) {
      return sites.isEmpty() ? null : sites.get(Math.min(attemptIndex, sites.size() - 1));
   }

   static boolean init_Z_X_DataUtil(AiGuard.GuardAction action, boolean hasEdibleFood, boolean containerOpen, boolean screenOpen) {
      return action == AiGuard.GuardAction.EAT && (!hasEdibleFood || containerOpen || screenOpen);
   }

   static int clampTimeout(int seconds) {
      return Math.max(300, Math.min(3600, seconds));
   }

   static int load(int configured, int remaining) {
      return Math.max(15, Math.min(configured, Math.min(360, remaining)));
   }

   static int read(int configured, int remaining) {
      return Math.max(15, Math.min(configured, remaining));
   }

   static int init_I_Z(boolean haveTarget, boolean closeEnough, boolean legActive) {
      if (!haveTarget) {
         return 2;
      } else if (closeEnough) {
         return legActive ? 1 : 2;
      } else {
         return 0;
      }
   }

   static int init() {
      return 5;
   }

   static int load() {
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

   static JsonObject init(String itemId, int count) {
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
            final JsonArray load_JsonArray = new JsonArray();
            AiNetherEntry.Inner_CachePool phase;
            long phaseStartedMillis = this.startMillis;
            int step;
            int lootDelayTicks;
            int chestsLooted;
            int hayBalesHarvested;
            int foodBefore;
            int sync_I;
            int interruptions;
            int attempt = 1;
            int openingSiteIndex;
            boolean init_Z;
            boolean load_Z;
            boolean villageDone;
            boolean foodDone;
            boolean update_Z;
            boolean sync_Z;
            AiOperation child;
            AiOperation guardOp;
            AiOperation nightOp;
            String legId = "";
            String guardReason = "";
            String nightActionName = "";
            String update_Str;
            String sync_Str = "";
            BlockPos village;
            BlockPos load_BlockPos;
            BlockPos openingSite;
            BlockPos knownPortal;
            boolean flush_Z;
            long update_J;
            int portalLookupRetries = 20;
            AiOperation write_AiOperation;
            int push_I;
            int huntAttempts;
            int tick_I;
            AiOperation foodOp;
            final AiPreparationReceipt preparation = new AiPreparationReceipt();
            boolean close_Z;
            int lavaExplorations;
            int get;
            int set;
            int put;
            boolean portalBuildStarted;
            boolean poll_Z;
            boolean push_Z;
            boolean pop_Z;
            boolean tick_Z;
            boolean run_Z;
            int add;
            long guardStartNanos;
            long guardCooldownUntil;
            AiOperation sync_AiOperation;
            int clear;
            BlockPos update_BlockPos;
            BlockPos lavaPool;
            List<BlockPos> chests = List.of();
            int reset;
            long close_J = -1L;
            long open_J = -1L;
            long poll_J = -1L;
            long sleepRetryAtMillis = 0L;
            JsonObject init_JsonObject;
            JsonObject load_JsonObject;
            String entryWarning;
            AiOperation.Result init_AiOperation_Result;
            final AiNetherEntry.Inner_DataUtil childTracker = new AiNetherEntry.Inner_DataUtil();
            final AiNetherEntry.Inner_StreamUtil entryTimer = new AiNetherEntry.Inner_StreamUtil();
            int start;
            int stop;

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
               if (!this.sync_Z && mc.player != null && mc.level != null) {
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
                           return this.write("Verifying Nether arrival");
                        } else {
                           return arrival.status() != AiOperation.Status.SUCCEEDED
                              ? this.finish(arrival.status(), null, arrival.message())
                              : this.read("Verified Nether arrival");
                        }
                     } else {
                        return this.phase != null
                           ? this.finish(
                              AiOperation.Status.UNCERTAIN, null, "Entered the Nether outside the expected portal action"
                           )
                           : this.read("Already in the Nether");
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
                        AiNetherEntry.Inner_CachePool next = AiNetherEntry.init(
                           false, this.hasStoneTier(), this.needsFood(), this.needsVillage(), this.hasIronFireGear(), this.portalNearby(), this.flush_Z_3()
                        );
                        if ((next == AiNetherEntry.Inner_CachePool.LAVA || next == AiNetherEntry.Inner_CachePool.PORTAL) && !this.flush_Z) {
                           this.knownPortal = AiPortalLinks.init(192);
                           if (this.knownPortal == null && this.portalLookupRetries-- > 0) {
                              return this.write("Checking observed portal history before making another frame");
                           }

                           this.flush_Z = true;
                           if (this.knownPortal != null) {
                              this.update_J = System.nanoTime() + 120000000000L;
                              this.init(AiNetherEntry.Inner_CachePool.LAVA);
                              this.step = this.knownPortal.getY() - mc.player.blockPosition().getY() > 16 && !this.read_Z(3) ? 89 : 90;
                              return this.write("Rechecking the remembered portal at " + this.knownPortal.toShortString());
                           }
                        }

                        this.init(next);
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
                     return this.init_AiOperation_Result == null && !this.childTracker.requiresReconciliation()
                        ? phaseResult
                        : this.finish(
                           AiOperation.Status.UNCERTAIN,
                           null,
                           this.init_AiOperation_Result == null
                              ? "Interrupted Nether entry action needs observation before resuming"
                              : this.init_AiOperation_Result.message()
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
                  if (AiNetherEntry.init_Z_X_DataUtil(
                     decision.action(), AiSurvival.init(mc.player.getInventory()) >= 0, mc.player.containerMenu != mc.player.inventoryMenu, mc.gui.screen() != null
                  )) {
                     this.sync_Str = decision.reason();
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
                  return this.write("Interrupted: " + this.guardReason);
               } else {
                  AiOperation.Result r = this.childTracker.tick(this.guardOp);
                  if (!r.terminal()) {
                     return this.write("Interrupted: " + this.guardReason + " - " + r.message());
                  }

                  this.childTracker.cancel(this.guardOp);
                  this.guardOp = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  boolean quick = System.nanoTime() - this.guardStartNanos < 1000000000L;
                  if (r.status() != AiOperation.Status.SUCCEEDED && quick) {
                     this.guardCooldownUntil = System.nanoTime() + 3000000000L;
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED || !quick) {
                     AutismClientAddon.LOG.info("[AI] nether entry: guard {} after {}: {}", r.status(), this.guardReason, r.message());
                  }

                  return this.write("Resuming after " + this.guardReason);
               }
            }

            AiOperation.Result tickNight() {
               if (this.nightOp == null && !this.canInterruptNow()) {
                  return null;
               }

               if (this.nightOp != null) {
                  AiOperation.Result r = this.childTracker.tick(this.nightOp);
                  if (!r.terminal()) {
                     return this.write("Night: " + this.nightActionName + " - " + r.message());
                  }

                  this.childTracker.cancel(this.nightOp);
                  this.nightOp = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.open_J = this.close_J;
                  } else {
                     if ("sleep".equals(this.nightActionName)) {
                        this.poll_J = this.close_J;
                     }

                     this.sleepRetryAtMillis = System.currentTimeMillis() + 20000L;
                  }

                  return this.write("Resuming after the night (" + this.nightActionName + ")");
               } else {
                  boolean sleepable = this.bedCount() > 0 && this.poll_J != this.overworldDay();
                  if (AiOracle.isActive() && !sleepable) {
                     return null;
                  }

                  if (!AiNetherEntry.init(AiSleep.dayTime(), this.inOverworld(), false, this.open_J, this.overworldDay(), this.sleepRetryAtMillis, System.currentTimeMillis())) {
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
                  this.close_J = this.overworldDay();
                  int cap = Math.max(60, Math.min(600, this.secondsLeft()));
                  if ("sleep".equals(action)) {
                     JsonObject sleepArgs = AiJson.objectOf("timeoutSeconds", Math.min(240, cap));
                     this.nightOp = this.init_AiOperation_Str("game:sleep", sleepArgs, AiSleep.init(sleepArgs));
                  } else {
                     this.nightOp = AiBurrow.init(AiJson.objectOf("timeoutSeconds", cap));
                  }

                  return this.write("Night falls; " + ("sleep".equals(action) ? "sleeping until dawn" : "burrowing until dawn"));
               }
            }

            AiOperation.Result runOpeningPhase() {
               if (AiNetherEntry.init(this.hasStoneTier(), this.child != null)) {
                  return this.init("skipped", "tools already present");
               }

               if (this.step == 0) {
                  if (this.logCount() >= AiNetherEntry.init()) {
                     this.step = 3;
                     return this.write("Logs already in the bag");
                  }

                  AiOperation.Result r = this.read("log", 4, 96);
                  if (r == null) {
                     return this.write("Locating the nearest tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  List<BlockPos> sites = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 4) : List.of();
                  this.openingSite = AiNetherEntry.init(sites, this.openingSiteIndex);
                  this.step = 1;
                  return this.write(
                     this.openingSite == null
                        ? "No log located within 96 blocks; chopping from here"
                        : "Tree site " + (sites.indexOf(this.openingSite) + 1) + "/" + sites.size() + " at " + this.openingSite.toShortString()
                  );
               } else if (this.step == 1) {
                  int action = AiNetherEntry.init_I_Z(
                     this.openingSite != null,
                     this.openingSite != null && mc.player.blockPosition().distSqr(this.openingSite) <= 64.0,
                     this.child != null
                  );
                  if (action != 0) {
                     this.drainChild();
                     this.step = 2;
                     return this.write("At the woodline");
                  }

                  AiOperation.Result r = this.walkTo(this.openingSite, 8.0, 300);
                  if (r == null) {
                     return this.write("Travelling to the tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.step = 2;
                  return this.write("At the woodline");
               } else if (this.step == 2) {
                  if (this.logCount() >= AiNetherEntry.init()) {
                     this.drainChild();
                     this.step = 3;
                     return this.write("Logs already in the bag");
                  }

                  if (this.child == null) {
                     this.init("game:chop", AiJson.objectOf("block", "log", "count", AiNetherEntry.init(), "radius", 64, "timeoutSeconds", this.load_I(300)));
                     return this.write("Felling trees for the opening");
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.write("Felling trees for the opening");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (this.logCount() < AiNetherEntry.load()) {
                     return this.retryOrFail(
                        AiFailure.TARGET_NOT_FOUND,
                        "Only " + this.logCount() + " log(s) after the wood run (" + r.message() + ")",
                        AiNetherEntry.init("minecraft:oak_log", AiNetherEntry.load() - this.logCount())
                     );
                  }

                  this.step = 3;
                  return this.write("Logs gathered");
               } else if (this.step == 3) {
                  if (this.child != null || this.count("minecraft:wooden_pickaxe") <= 0 && this.count("minecraft:crafting_table") <= 0) {
                     AiOperation.Result r = this.obtainItem("minecraft:wooden_pickaxe", 1, this.attemptCap(300), this.isLateAttempt());
                     if (r == null) {
                        return this.write("Obtaining the wooden pickaxe");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(AiNetherEntry.init(r), "Wooden pickaxe unobtainable: " + r.message(), AiNetherEntry.init("minecraft:wooden_pickaxe", 1));
                     }

                     this.step = 4;
                     return this.write("Wooden pickaxe obtained");
                  } else {
                     this.step = 4;
                     return this.write("Wooden pickaxe covered");
                  }
               } else if (this.step == 4) {
                  if (this.child == null && this.count("minecraft:stone_pickaxe") > 0) {
                     this.step = 5;
                     return this.write("Stone pickaxe covered");
                  }

                  AiOperation.Result r = this.obtainItem("minecraft:stone_pickaxe", 1, this.attemptCap(300), this.isLateAttempt());
                  if (r == null) {
                     return this.write("Obtaining the stone pickaxe");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(AiNetherEntry.init(r), "Stone pickaxe unobtainable: " + r.message(), AiNetherEntry.init("minecraft:stone_pickaxe", 1));
                  }

                  this.step = 5;
                  return this.write("Stone pickaxe obtained");
               } else {
                  if (this.step == 5) {
                     if (this.child != null || this.count("minecraft:cobblestone") >= 3 && this.count("minecraft:stone_axe") <= 0) {
                        AiOperation.Result r = this.obtainItem("minecraft:stone_axe", 1, this.attemptCap(240), this.isLateAttempt());
                        if (r == null) {
                           return this.write("Cheap stone axe");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.step = 6;
                        return this.write("Stone axe done");
                     }

                     this.step = 6;
                  }

                  if (this.step == 6) {
                     if (this.child != null || this.count("minecraft:cobblestone") >= 2 && this.count("minecraft:stone_sword") <= 0) {
                        AiOperation.Result r = this.obtainItem("minecraft:stone_sword", 1, this.attemptCap(240), this.isLateAttempt());
                        if (r == null) {
                           return this.write("Cheap stone sword");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.step = 7;
                        return this.write("Stone sword done");
                     }

                     this.step = 7;
                  }

                  return this.hasStoneTier()
                     ? this.init("succeeded", "opening tools ready")
                     : this.retryOrFail(AiFailure.MISSING_MATERIALS, "Opening did not produce the tools", AiNetherEntry.init("minecraft:stone_pickaxe", 1));
               }
            }

            AiOperation.Result runFoodPhase() {
               if (!this.needsFood()) {
                  this.foodDone = true;
                  return this.init("skipped", "food is fine");
               }

               if (this.step == 0) {
                  this.step = 1;
                  this.foodBefore = this.safeFoodCount();
               }

               if (this.child == null) {
                  AiOperation hunt = AiSpeedrun.hunt(48.0, 12, this.load_I(180));
                  if (hunt == null) {
                     this.foodDone = true;
                     return this.init("skipped", "no food animal in sight");
                  } else {
                     this.legId = "game:obtain";
                     this.init(AiNetherEntry.legEvent(this.legId, "STARTED", "", null));
                     this.child = hunt;
                     return this.write("Hunting food");
                  }
               } else {
                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.write("Hunting food");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.foodDone = true;
                     return this.init("succeeded", "food " + this.foodBefore + " -> " + this.safeFoodCount());
                  } else if (this.attempt < AiNetherEntry.maxAttempts(AiNetherEntry.Inner_CachePool.FOOD) && AiNetherEntry.init(AiNetherEntry.Inner_CachePool.FOOD, r.failure())) {
                     this.attempt++;
                     this.step = 0;
                     return this.write("The hunt came up short; hunting again (attempt " + this.attempt + "/" + AiNetherEntry.maxAttempts(AiNetherEntry.Inner_CachePool.FOOD) + ")");
                  } else {
                     this.foodDone = true;
                     return this.init("failed", "food " + this.foodBefore + " -> " + this.safeFoodCount() + " (" + r.message() + ")");
                  }
               }
            }

            AiOperation.Result runVillagePhase() {
               if (this.step == 0) {
                  this.step = 1;
                  if (explicitVillage != null) {
                     if (!AiNetherEntry.shouldVisitVillage(skipVillage, this.load(explicitVillage), this.bedCount(), bedsTarget)) {
                        this.villageDone = true;
                        return this.init("skipped", "village out of range or beds covered");
                     } else {
                        this.village = explicitVillage;
                        return this.write("Village at " + this.village.toShortString());
                     }
                  } else if (!skipVillage && this.bedCount() < bedsTarget) {
                     this.step = 1;
                     return this.write("Predicting the nearest village");
                  } else {
                     this.villageDone = true;
                     return this.init("skipped", "village disabled or beds covered");
                  }
               } else if (this.step == 1) {
                  if (this.village == null) {
                     AiOperation.Result r = this.locate("structure:village");
                     if (r == null) {
                        return this.write("Predicting the nearest village");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.firstPosition(r.dataJson(), "positions", 750.0) : null;
                     if (hit == null) {
                        this.villageDone = true;
                        return this.init(
                           "skipped",
                           r.status() == AiOperation.Status.SUCCEEDED
                              ? "no village predicted within 750 blocks"
                              : "no village prediction available (" + r.message() + ")"
                        );
                     }

                     this.village = hit;
                  }

                  this.step = 2;
                  return this.write("Travelling to the village at " + this.village.toShortString());
               } else if (this.step == 2) {
                  AiOperation.Result r = this.walkToUncapped(this.village, 14.0, 300);
                  if (r == null) {
                     return this.write("Travelling to the village");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     this.villageDone = true;
                     return this.init("failed", "no route to the village (" + r.message() + ")");
                  } else {
                     this.step = 3;
                     return this.write("At the village; scanning for chests");
                  }
               } else if (this.step == 3) {
                  AiOperation.Result r = this.read("chest", 8, 48);
                  if (r == null) {
                     return this.write("Scanning the village for chests");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 4) : List.of();
                  this.reset = 0;
                  this.step = 4;
                  return this.write(this.chests.size() + " chest(s) to loot");
               } else if (this.step == 4) {
                  if (this.reset < this.chests.size()) {
                     BlockPos chest = this.chests.get(this.reset);
                     if (this.child == null) {
                        this.init(
                           "game:container",
                           AiJson.objectOf(
                              "action", "loot", "x", chest.getX(), "y", chest.getY(), "z", chest.getZ(), "timeoutSeconds", Math.min(60, this.load_I(300))
                           )
                        );
                        return this.write("Looting a village chest");
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Looting a village chest");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.chestsLooted++;
                     }

                     this.reset++;
                     return this.write("Chests looted: " + this.chestsLooted);
                  } else {
                     this.step = 5;
                     return this.write("Scanning for hay bales");
                  }
               } else if (this.step == 5) {
                  AiOperation.Result r = this.read("hay_block", 8, 48);
                  if (r == null) {
                     return this.write("Scanning for hay bales");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 8) : List.of();
                  this.reset = 0;
                  this.step = 6;
                  return this.write(this.chests.size() + " hay bale(s) to harvest");
               } else if (this.step == 6) {
                  if (this.reset < this.chests.size()) {
                     AiOperation.Result r = this.init_AiOperation_Result_3(this.chests.get(this.reset));
                     if (r == null) {
                        return this.write("Harvesting a hay bale");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.hayBalesHarvested++;
                     }

                     this.reset++;
                     this.lootDelayTicks = 20;
                     return this.write("Hay bales harvested: " + this.hayBalesHarvested);
                  } else {
                     this.step = 7;
                     this.chests = List.of();
                     this.reset = 0;
                     return this.write(this.bedCount() < bedsTarget ? "Scanning for beds" : "Beds covered");
                  }
               } else {
                  if (this.step == 7) {
                     if (this.bedCount() < bedsTarget && !this.update_Z) {
                        AiOperation.Result r = this.read("bed", 8, 48);
                        if (r == null) {
                           return this.write("Scanning for beds");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.chests = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.positions(r.dataJson(), "results", 8) : List.of();
                        this.reset = 0;
                        this.update_Z = true;
                     }

                     if (this.reset < this.chests.size() && this.bedCount() < bedsTarget) {
                        AiOperation.Result r = this.init_AiOperation_Result_3(this.chests.get(this.reset));
                        if (r == null) {
                           return this.write("Taking a village bed");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.reset++;
                        this.lootDelayTicks = 20;
                        return this.write("Beds held: " + this.bedCount());
                     }

                     this.step = 8;
                  }

                  this.villageDone = true;
                  return this.init(
                     "succeeded", "looted " + this.chestsLooted + " chest(s), " + this.hayBalesHarvested + " hay bale(s), beds " + this.bedCount() + "/" + bedsTarget
                  );
               }
            }

            AiOperation.Result runIronFirePhase() {
               if (this.hasIronFireGear() && this.child == null) {
                  return this.init("skipped", "iron and fire gear already present");
               }

               if (this.step == 0) {
                  int ironNeed = Math.max(AiNetherEntry.ironStillNeeded(0, this.bucketCount(), this.count("minecraft:flint_and_steel")), 0);
                  if (this.child == null && this.count("minecraft:iron_ingot") >= ironNeed) {
                     this.step = 1;
                     return this.write("Iron already in the bag");
                  }

                  AiOperation.Result r = this.obtainItemUncapped("minecraft:iron_ingot", ironNeed, this.attemptCap(420));
                  if (r == null) {
                     return this.write("Obtaining iron for the bucket and lighter");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(
                        AiNetherEntry.init(r),
                        "Iron unobtainable: " + r.message(),
                        AiNetherEntry.init(
                           "minecraft:iron_ingot",
                           Math.max(1, AiNetherEntry.ironStillNeeded(this.count("minecraft:iron_ingot"), this.bucketCount(), this.count("minecraft:flint_and_steel")))
                        )
                     );
                  }

                  this.step = 1;
                  return this.write("Iron obtained");
               } else if (this.step != 1) {
                  if (this.step == 2) {
                     if (this.child == null && this.bucketCount() > 0) {
                        this.step = 3;
                        return this.write("Bucket already in the bag");
                     }

                     AiOperation.Result r = this.load("minecraft:bucket", this.attemptCap(420));
                     if (r == null) {
                        return this.write("Crafting the bucket");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(AiNetherEntry.init(r), "Bucket not craftable: " + r.message(), this.init(r, "minecraft:bucket"));
                     }

                     this.step = 3;
                     return this.write("Bucket crafted");
                  } else if (this.step == 3) {
                     if (this.child == null && this.count("minecraft:flint_and_steel") > 0) {
                        this.step = 4;
                        return this.write("Lighter already in the bag");
                     }

                     AiOperation.Result r = this.load("minecraft:flint_and_steel", this.attemptCap(420));
                     if (r == null) {
                        return this.write("Crafting flint and steel");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.retryOrFail(
                           AiNetherEntry.init(r), "Flint and steel not craftable: " + r.message(), this.init(r, "minecraft:flint_and_steel")
                        );
                     }

                     this.step = 4;
                     return this.write("Flint and steel crafted");
                  } else {
                     return this.hasIronFireGear()
                        ? this.init("succeeded", "bucket and flint_and_steel ready")
                        : this.retryOrFail(AiFailure.MISSING_MATERIALS, "Iron/fire gear incomplete", AiNetherEntry.init("minecraft:flint_and_steel", 1));
                  }
               } else if (this.child != null || this.count("minecraft:flint") <= 0 && this.count("minecraft:flint_and_steel") <= 0) {
                  AiOperation.Result r = this.obtainItemUncapped("minecraft:flint", 1, this.attemptCap(420));
                  if (r == null) {
                     return this.write("Obtaining flint from gravel");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.retryOrFail(AiNetherEntry.init(r), "Flint unobtainable: " + r.message(), AiNetherEntry.init("minecraft:flint", 1));
                  }

                  this.step = 2;
                  return this.write("Flint obtained");
               } else {
                  this.step = 2;
                  return this.write("Flint already in the bag");
               }
            }

            AiOperation.Result runLavaPhase() {
               if (this.step == 89) {
                  if (System.nanoTime() > this.update_J && this.canInterruptNow()) {
                     this.drainChild();
                     return this.init("skipped", "remembered portal ascent exhausted its bounded budget; using local resources");
                  }

                  if (this.child == null) {
                     this.legId = "game:surface";
                     this.init(AiNetherEntry.legEvent(this.legId, "STARTED", "", null));
                     int seconds = (int)Math.max(1L, (this.update_J - System.nanoTime()) / 1000000000L);
                     this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", Math.min(120, seconds)));
                  }

                  AiOperation.Result ascent = this.pollChild();
                  if (ascent == null) {
                     return this.write("Surfacing toward the remembered portal at " + this.knownPortal.toShortString());
                  }

                  if (ascent.status() == AiOperation.Status.UNCERTAIN) {
                     return ascent;
                  }

                  if (ascent.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (ascent.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init("skipped", "remembered portal ascent failed; using local resources (" + ascent.message() + ")");
                  }

                  this.step = 90;
                  return this.write("Surface reached; rechecking the remembered portal");
               } else if (this.step == 90) {
                  if (System.nanoTime() > this.update_J && this.canInterruptNow()) {
                     this.drainChild();
                     return this.init("skipped", "remembered portal walk exhausted its bounded budget; using local resources");
                  } else {
                     AiOperation.Result route = this.walkTo(this.knownPortal, 4.0, 120);
                     if (route == null) {
                        return this.write("Rechecking the remembered portal at " + this.knownPortal.toShortString());
                     } else {
                        return route.status() == AiOperation.Status.UNCERTAIN
                           ? route
                           : this.init(
                              "skipped",
                              this.portalNearby()
                                 ? "remembered portal re-observed; no new frame needed"
                                 : "remembered portal could not be re-observed; using local resources (" + route.message() + ")"
                           );
                     }
                  }
               } else {
                  if (this.step >= 40) {
                     return this.run();
                  }

                  if (this.step >= 30) {
                     return this.pop();
                  }

                  if (this.step == 0 && this.clear == 0 && this.count("minecraft:water_bucket") == 0 && this.count("minecraft:bucket") > 0) {
                     this.step = 30;
                     return this.write("Filling a water bucket before the lava");
                  }

                  if (this.step == 0 && AiDimension.framedObsidian() > 0 && this.close_Z_2()) {
                     this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                     return this.startPortalBuild("the frame begun at " + AiDimension.frameOrigin.toShortString() + " holds " + AiDimension.framedObsidian() + " obsidian");
                  }

                  if (AiNetherEntry.load(this.flush_Z_3(), this.child != null)) {
                     if (this.poll_Z) {
                        this.poll_Z = false;
                        this.drainChild();
                        this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                        return this.startPortalBuild("no pool in loaded terrain takes a cast");
                     }

                     if (!this.push_Z || this.castableLavaPool() != null) {
                        return this.init("skipped", "already at lava");
                     }
                  }

                  if (this.step == 0) {
                     this.step = 1;
                     return this.write("Scanning loaded terrain for lava");
                  }

                  if (this.step == 1) {
                     BlockPos hit = null;
                     BlockPos open = null;
                     int checked = 0;

                     for (BlockPos pos : AiWorldScan.findExposedLavaSources(autismclient.modules.am.init_I(), 2, 32)) {
                        if (!AiNetherEntry.init_Z(pos)) {
                           if (open == null) {
                              open = pos;
                           }

                           if (checked++ < 12 && AiPortalCast.init_Z(mc.level, pos)) {
                              hit = pos;
                              break;
                           }
                        }
                     }

                     if (hit == null && open != null && this.close_Z_2()) {
                        AutismClientAddon.LOG.info("[AI] nether entry: no castable pool among {} open ones; obsidian from {}", checked, open.toShortString());
                        this.load_BlockPos = open;
                        this.close_Z = true;
                        this.poll_Z = true;
                        this.step = 3;
                        return this.write("No castable pool; obsidian from the pool at " + open.toShortString());
                     }

                     if (hit == null && !this.push_Z) {
                        hit = open;
                     }

                     if (hit != null) {
                        this.load_BlockPos = hit;
                        this.close_Z = true;
                        this.step = 3;
                        return this.write("Open lava observed at " + hit.toShortString());
                     }

                     if (AiOracle.isActive()) {
                        if (this.lavaExplorations >= 8) {
                           return this.retryOrFail(AiFailure.TARGET_NOT_FOUND, "No open lava after " + this.lavaExplorations + " exploration legs", null);
                        }

                        this.step = 5;
                        return this.write("No open lava in loaded terrain; exploring");
                     } else {
                        if (this.load_Z) {
                           return this.retryOrFail(
                              AiFailure.TARGET_NOT_FOUND, "No lava observed near the predicted ruined portal; explore or re-issue closer to a pool", null
                           );
                        }

                        this.step = 2;
                        return this.write("No lava in loaded terrain; predicting a ruined portal");
                     }
                  } else if (this.step == 2) {
                     AiOperation.Result r = this.locate("structure:ruined_portal");
                     if (r == null) {
                        return this.write("Predicting a ruined portal");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.firstPosition(r.dataJson(), "positions", 1.7976931348623157E308) : null;
                     if (hit == null) {
                        return this.retryOrFail(AiFailure.TARGET_NOT_FOUND, "No lava in loaded terrain and no ruined portal prediction (" + r.message() + ")", null);
                     }

                     this.load_Z = true;
                     this.load_BlockPos = hit;
                     this.close_Z = false;
                     this.step = 3;
                     return this.write("Ruined portal predicted at " + hit.toShortString());
                  } else {
                     if (this.step == 3) {
                        AiOperation.Result r = this.close_Z
                           ? this.init(this.load_BlockPos, 240)
                           : this.walkTo(this.load_BlockPos, 16.0, 240);
                        if (r == null) {
                           return this.write("Travelling to " + (this.close_Z ? "lava" : "the ruined portal"));
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           if (this.close_Z) {
                              AiNetherEntry.init_V(this.load_BlockPos);
                           }

                           return this.retryOrFail(AiFailure.UNREACHABLE, "No route to " + this.load_BlockPos.toShortString() + " (" + r.message() + ")", null);
                        }

                        if (!this.close_Z) {
                           this.step = 0;
                           return this.write("At the ruined portal; scanning for its lava");
                        }

                        if (!this.flush_Z_3()) {
                           return this.retryOrFail(
                              AiFailure.TARGET_NOT_FOUND, "Reached " + this.load_BlockPos.toShortString() + " but no lava is observed here", null
                           );
                        }

                        this.step = 4;
                        if (this.poll_Z) {
                           this.poll_Z = false;
                           this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                           return this.startPortalBuild("no pool in loaded terrain takes a cast");
                        }
                     }

                     if (this.step == 5) {
                        if (this.child == null) {
                           int dx = this.get == 0 ? 1 : (this.get == 2 ? -1 : 0);
                           int dz = this.get == 1 ? 1 : (this.get == 3 ? -1 : 0);
                           BlockPos at = mc.player.blockPosition();
                           this.update_BlockPos = new BlockPos(at.getX() + dx * 64, at.getY(), at.getZ() + dz * 64);
                           this.legId = "game:goto";
                           this.init(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                           this.child = AiGoto.travelToCompat(
                              this.update_BlockPos,
                              0.6,
                              true,
                              true,
                              true,
                              this.load_I(240),
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST,
                              true,
                              2.0,
                              new AiNetherRun.Inner_NetHelper(this.update_BlockPos.getX(), this.update_BlockPos.getZ(), 8)
                           );
                        }

                        AiOperation.Result r = this.pollChild();
                        if (r == null) {
                           return this.write("Exploring for lava toward " + this.update_BlockPos.toShortString());
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.lavaExplorations++;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.get = this.get + 1 & 3;
                        }

                        this.step = 1;
                        return this.write("Exploration leg " + this.lavaExplorations + " ended (" + r.status() + "); scanning again");
                     } else {
                        return this.init("succeeded", "at lava " + (this.load_BlockPos != null ? this.load_BlockPos.toShortString() : ""));
                     }
                  }
               }
            }

            AiOperation.Result runPortalPhase() {
               if (this.step >= 20) {
                  return this.push();
               }

               if (this.step == 0
                  && this.child == null
                  && !this.init_Z
                  && this.clear == 0
                  && this.count("minecraft:water_bucket") == 0
                  && this.count("minecraft:bucket") > 0) {
                  this.init(AiNetherEntry.Inner_CachePool.LAVA);
                  this.step = 30;
                  return this.write("Filling a water bucket before the cast");
               }

               if (this.portalNearby()) {
                  return this.init("skipped", "a lit portal is already here");
               }

               if (this.step == 3) {
                  AiOperation.Result r = this.init(this.lavaPool, 240);
                  if (r == null) {
                     return this.write("Walking to the lava pool at " + this.lavaPool.toShortString());
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     AiNetherEntry.init_V(this.lavaPool);
                     BlockPos left = this.lavaPool;
                     this.lavaPool = null;
                     return this.set++ < 3
                        ? this.init("failed", "no route to the pool at " + left.toShortString())
                        : this.failPhase(AiFailure.UNREACHABLE, "No route to the lava pool at " + left.toShortString() + " (" + r.message() + ")", null);
                  } else {
                     this.step = 0;
                     return this.write("At the lava pool");
                  }
               } else if (this.step != 0) {
                  if (this.step == 1) {
                     if (this.missingMaterials.isEmpty()) {
                        this.step = 0;
                        return this.write("Re-issuing the portal cast");
                     }

                     Entry<String, Integer> need = this.missingMaterials.get(0);
                     if (this.child == null) {
                        this.startSupplyLeg(need.getKey(), need.getValue());
                        return this.write("Obtaining " + need.getKey() + " for the cast");
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Obtaining " + need.getKey() + " for the cast");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && this.count(need.getKey()) <= 0) {
                        return this.failPhase(
                           AiFailure.MISSING_MATERIALS, "Cannot supply " + need.getKey() + " for the portal cast (" + r.message() + ")", this.init_JsonObject
                        );
                     }

                     this.missingMaterials.remove(0);
                     return this.write("Supplied " + need.getKey());
                  } else {
                     return this.init("succeeded", "portal cast and lit");
                  }
               } else {
                  if (this.sync_I >= 3 && !this.init_Z) {
                     return this.close_Z_2()
                        ? this.startPortalBuild("no cast went in 3 attempts")
                        : this.failPhase(AiFailure.MISSING_MATERIALS, "Portal cast still short of materials after 3 attempts", this.init_JsonObject);
                  }

                  if (this.child == null) {
                     if (this.lavaPool == null) {
                        this.lavaPool = this.load_BlockPos != null
                              && this.close_Z
                              && !AiNetherEntry.init_Z(this.load_BlockPos)
                              && AiPortalCast.init_Z(mc.level, this.load_BlockPos)
                           ? this.load_BlockPos
                           : this.load_BlockPos_4();
                     }

                     if (!this.init_Z && (this.lavaPool == null || !AiPortalCast.init_Z(mc.level, this.lavaPool)) && this.close_Z_2()) {
                        this.lavaPool = null;
                        return this.startPortalBuild("no pool in reach takes a cast");
                     }

                     if (this.lavaPool != null && mc.player.blockPosition().distSqr(this.lavaPool) > 16.0) {
                        this.step = 3;
                        return this.write("Walking to the lava pool at " + this.lavaPool.toShortString());
                     }

                     if (!this.init_Z) {
                        this.sync_I++;
                        this.init_Z = true;
                     }

                     JsonObject castArgs = AiJson.objectOf("timeoutSeconds", this.read_I(300));
                     if (this.lavaPool != null) {
                        castArgs.addProperty("x", this.lavaPool.getX());
                        castArgs.addProperty("y", this.lavaPool.getY());
                        castArgs.addProperty("z", this.lavaPool.getZ());
                     }

                     this.init("game:portal_cast", castArgs);
                     return this.write("Casting the portal (attempt " + this.sync_I + ")");
                  } else {
                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Casting the portal (attempt " + this.sync_I + ")");
                     }

                     this.init_Z = false;
                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.step = 2;
                        return this.write("Portal cast and lit");
                     }

                     JsonObject miss = AiNetherEntry.parseObject(r.dataJson());
                     if (miss == null) {
                        if (r.status() == AiOperation.Status.TIMED_OUT) {
                           return this.write("Cast timed out; resuming (finished cells are skipped)");
                        } else {
                           BlockPos pool = this.lavaPool != null ? this.lavaPool : this.read_BlockPos_3();
                           this.lavaPool = null;
                           AutismClientAddon.LOG
                              .info("[AI] nether entry: cast at the pool {} failed: {}", pool == null ? "?" : pool.toShortString(), r.message());
                           if (this.close_Z_2()) {
                              return this.startPortalBuild(
                                 "the cast at " + (pool == null ? "the pool" : pool.toShortString()) + " failed (" + r.message() + ")"
                              );
                           } else if (pool != null && this.set < 3) {
                              this.set++;
                              AiNetherEntry.load(pool);
                              return this.init("failed", "pool at " + pool.toShortString() + " left (" + r.message() + ")");
                           } else {
                              return this.close_Z_2()
                                 ? this.startPortalBuild("no pool took a cast (" + r.message() + ")")
                                 : this.failPhase(r.failure(), "Portal cast failed: " + r.message(), null);
                           }
                        }
                     } else {
                        this.init_JsonObject = miss;
                        this.missingMaterials.clear();

                        for (Entry<String, JsonElement> e : miss.entrySet()) {
                           this.missingMaterials.add(Map.entry(e.getKey(), Math.max(1, e.getValue().getAsInt())));
                        }

                        this.step = 1;
                        return this.write("Portal cast is missing " + miss + "; obtaining");
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
                  && this.write_AiOperation == null
                  && this.sync_AiOperation == null) {
                  if (!AiRunResources.missingMandatoryGear().isEmpty() || !AiRunResources.isMandatoryArmorEquipped()) {
                     return this.failPhase(
                        AiFailure.MISSING_MATERIALS,
                        "Required diamond kit and equipped golden boots are not ready for Nether entry",
                        AiJson.GSON.toJsonTree(AiRunResources.missingMandatoryGear()).getAsJsonObject()
                     );
                  }

                  if (AiRunResources.totalSafeFoodNutrition() < 32) {
                     if (++this.tick_I > 8) {
                        return this.failPhase(
                           AiFailure.MISSING_MATERIALS,
                           "Nether entry needs carried safe food; raw meat and exhausted hunt attempts do not satisfy the gate",
                           AiJson.objectOf("safeNutrition", 32 - AiRunResources.totalSafeFoodNutrition())
                        );
                     }

                     Map<String, Integer> raw = AiSpeedrun.rawMeatCounts();
                     if (!raw.isEmpty()) {
                        this.foodOp = AiForage.cook(this.load_I(this.attemptCap(180)));
                     } else {
                        this.foodOp = AiSpeedrun.secureFood(AiRunResources.totalSafeFoodItems() + 12, 180);
                     }

                     return this.write("Securing safe nutrition before Nether entry");
                  }

                  if (mc.player.getHealth() < 16.0F || mc.player.getFoodData().getFoodLevel() < 18) {
                     this.foodOp = mc.player.getFoodData().getFoodLevel() < 18
                        ? AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20))
                        : AiCover.init(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90));
                     return this.write("Restoring health and hunger before Nether entry");
                  }
               }

               if (this.foodOp != null) {
                  AiOperation.Result f = this.preparation.tickChild(this.foodOp);
                  if (!f.terminal()) {
                     return this.write("Food for the Nether: " + f.message());
                  }

                  this.preparation.cancelChild(this.foodOp);
                  this.foodOp = null;
                  if (f.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, f.message());
                  }
               }

               if (this.child == null && this.huntAttempts < 3 && this.safeFoodCount() < 14) {
                  this.huntAttempts++;
                  this.foodOp = this.huntAttempts == 1 && this.read_Z(8) ? AiSpeedrun.secureFood(14, 180) : AiSpeedrun.hunt(48.0, 14, 90);
                  if (this.foodOp != null) {
                     return this.write("Food for the Nether");
                  }
               }

               if (this.write_AiOperation != null) {
                  AiOperation.Result w = this.childTracker.tick(this.write_AiOperation);
                  if (!w.terminal()) {
                     return this.write("Filling a water bucket before the Nether: " + w.message());
                  }

                  if (w.status() == AiOperation.Status.UNCERTAIN) {
                     return this.finish(AiOperation.Status.UNCERTAIN, null, w.message());
                  }

                  this.childTracker.cancel(this.write_AiOperation);
                  this.write_AiOperation = null;
                  if (w.status() != AiOperation.Status.SUCCEEDED) {
                     this.push_I = 9;
                  }
               }

               if (this.child == null && this.push_I < 4) {
                  this.write_AiOperation = AiForage.init_AiOperation_Minecraft(mc, 32.0, 2);
                  if (this.write_AiOperation != null) {
                     this.push_I++;
                     return this.write("Filling a water bucket before the Nether");
                  }
               }

               if (this.child == null && this.write_AiOperation == null && AiRunResources.isCampaignActive() && AiRunResources.countUsableItem("minecraft:water_bucket", 0) < 2) {
                  if (this.push_I >= 4) {
                     return this.failPhase(
                        AiFailure.MISSING_MATERIALS, "Both water buckets must be refilled before entering the Nether", AiJson.objectOf("minecraft:water_bucket", 2)
                     );
                  }

                  this.push_I++;
                  this.write_AiOperation = AiObsidian.init(2, 120);
                  return this.write("Refilling both mandatory water buckets");
               } else {
                  if (this.sync_AiOperation != null) {
                     BlockPos home = AiDimension.load_BlockPos;
                     if (home != null
                        && AiNetherEntry.init_Z_D(
                           this.horizontalDistanceTo(home), this.sync_AiOperation.canInterruptNow(), this.sync_AiOperation.requiresReconciliationAfterEmergency()
                        )) {
                        this.childTracker.cancel(this.sync_AiOperation);
                        this.sync_AiOperation = null;
                        AutismClientAddon.LOG
                           .info(
                              "[AI] nether entry: spare obsidian left at {} ({} blocks from the portal)",
                              this.count("minecraft:obsidian"),
                              (int)this.horizontalDistanceTo(home)
                           );
                        return this.write("Spare obsidian given up; back to the portal");
                     }

                     AiOperation.Result o = this.childTracker.tick(this.sync_AiOperation);
                     if (!o.terminal()) {
                        return this.write("Spare obsidian for the way home (" + this.count("minecraft:obsidian") + "/10)");
                     }

                     if (o.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, o.message());
                     }

                     this.childTracker.cancel(this.sync_AiOperation);
                     this.sync_AiOperation = null;
                     AutismClientAddon.LOG.info("[AI] nether entry: spare obsidian {} ({})", this.count("minecraft:obsidian"), o.message());
                  }

                  if (this.child == null
                     && !this.pop_Z
                     && !AiNetherEntry.init_Z
                     && this.count("minecraft:obsidian") < 10
                     && this.count("minecraft:water_bucket") > 0
                     && (this.count("minecraft:diamond_pickaxe") > 0 || this.count("minecraft:netherite_pickaxe") > 0)
                     && this.lavaSourceWithin(40)) {
                     this.pop_Z = true;
                     AiNetherEntry.init_Z = true;
                     this.sync_AiOperation = AiObsidian.load(10, 240);
                     return this.write("Spare obsidian for the way home");
                  }

                  BlockPos lit = AiDimension.load_BlockPos;
                  if (this.child == null && lit != null && this.add < 3 && mc.player.blockPosition().distSqr(lit) > 1600.0) {
                     this.add++;
                     this.tick_Z = true;
                     this.run_Z = false;
                     this.legId = "game:goto";
                     this.init(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                     this.child = AiGoto.travelToCompat(
                        lit, 3.0, true, true, true, this.read_I(240), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true, 2.0
                     );
                     return this.write("Back to the portal built at " + lit.toShortString());
                  }

                  if (this.child != null && "game:goto".equals(this.legId) && this.tick_Z && !this.run_Z) {
                     AiOperation.Result back = this.pollChild();
                     if (back == null) {
                        return this.write("Back to the portal built at " + AiDimension.load_BlockPos.toShortString());
                     }

                     if (back.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     this.run_Z = true;
                     if (mc.player.blockPosition().distSqr(AiDimension.load_BlockPos) > 1600.0 && this.add < 3) {
                        return this.write("Still far from the portal (" + back.message() + "); walking on");
                     }
                  }

                  if (this.child != null) {
                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Entering the Nether");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.finish(AiOperation.Status.UNCERTAIN, null, r.message());
                     } else if (this.inNether() && r.status() == AiOperation.Status.SUCCEEDED) {
                        this.captureWarning(r.dataJson());
                        return this.init("succeeded", "in the Nether" + (this.entryWarning != null ? " (" + this.entryWarning + ")" : ""));
                     } else {
                        return r.status() == AiOperation.Status.CANCELLED
                           ? this.get()
                           : this.failPhase(r.failure(), "Dimension travel failed: " + r.message(), null);
                     }
                  } else {
                     if (AiRunResources.isCampaignActive()
                        && !AiNetherEntry.init_Z_Z(
                           AiRunResources.missingMandatoryGear().isEmpty() && AiRunResources.isMandatoryArmorEquipped(),
                           AiRunResources.totalSafeFoodNutrition(),
                           mc.player.getHealth(),
                           mc.player.getFoodData().getFoodLevel(),
                           AiRunResources.countUsableItem("minecraft:water_bucket", 0)
                        )) {
                        return this.write("Rechecking supplies and health before entering the Nether");
                     }

                     this.init("game:dimension", AiJson.objectOf("action", "enter", "target", "the_nether", "timeoutSeconds", this.read_I(180)));
                     return this.write("Entering the Nether");
                  }
               }
            }

            void init(String id, JsonObject legArgs) {
               this.legId = id;
               this.init(AiNetherEntry.legEvent(id, "STARTED", "", null));

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

                  this.child = this.init_AiOperation_Str(id, legArgs, var10004);
               } catch (RuntimeException refused) {
                  this.child = AiGameOperations.immediate(AiOperation.Status.FAILED, "Leg " + id + " refused: " + refused.getMessage(), new JsonObject());
               }

               this.child = AiRunDiagnostics.trace("nether_entry", id + " " + AiRunResources.compactEvidenceJson(legArgs.toString()), this.child);
            }

            AiOperation init_AiOperation_Str(String id, JsonObject legArgs, AiOperation op) {
               return AiGuard.isLowHealthRecoveryOp(id, legArgs) ? AiGuard.allowDuringLowHealth(op) : op;
            }

            void init(JsonObject detail) {
               if (this.load_JsonArray.size() >= 24) {
                  this.load_JsonArray.remove(0);
               }

               this.load_JsonArray.add(detail);
               this.load_JsonObject = detail;
            }

            AiOperation.Result pollChild() {
               AiOperation.Result r = this.childTracker.tick(this.child);
               if (!r.terminal()) {
                  return null;
               }

               this.childTracker.cancel(this.child);
               this.child = null;
               this.init(AiNetherEntry.legEvent(this.legId, r.status().name(), r.message(), r.dataJson()));
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  this.init_AiOperation_Result = r;
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
                  this.init(
                     "game:obtain", AiJson.objectOf("item", item, "count", amount, "timeoutSeconds", uncapped ? this.read_I(cap) : this.load_I(cap))
                  );
               }

               return this.pollChild();
            }

            AiOperation.Result craftItem(String item, int cap) {
               return this.craftItem(item, cap, false);
            }

            AiOperation.Result load(String item, int cap) {
               return this.craftItem(item, cap, true);
            }

            AiOperation.Result craftItem(String item, int cap, boolean uncapped) {
               if (this.child == null) {
                  this.init("game:craft", AiJson.objectOf("item", item, "count", 1, "timeoutSeconds", uncapped ? this.read_I(cap) : this.load_I(cap)));
               }

               return this.pollChild();
            }

            AiOperation.Result read(String block, int limit, int radius) {
               if (this.child == null) {
                  this.init("game:find", AiJson.objectOf("block", block, "radius", radius, "limit", limit));
               }

               return this.pollChild();
            }

            AiOperation.Result locate(String query) {
               if (this.child == null) {
                  this.init("game:oracle", AiJson.objectOf("query", query, "cap", 1));
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
                  this.init(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                  this.child = AiGoto.travelToCompat(target, radius, false, true, true, uncapped ? this.read_I(cap) : this.load_I(cap));
               }

               return this.pollChild();
            }

            AiOperation.Result init(BlockPos target, int cap) {
               if (this.child == null) {
                  this.legId = "game:goto";
                  this.init(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                  this.child = AiGoto.travelToCompat(target, 3.0, true, true, true, this.load_I(cap), autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST);
               }

               return this.pollChild();
            }

            AiOperation.Result init_AiOperation_Result_3(BlockPos pos) {
               if (this.child == null && this.lootDelayTicks > 0) {
                  this.lootDelayTicks--;
                  return null;
               }

               if (this.child == null) {
                  this.init("game:break", AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
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
               int cap = this.load_I(120);
               switch (AiNetherEntry.obtainMethodFor(itemId)) {
                  case "fill_water":
                     this.init("game:bucket", AiJson.objectOf("action", "fill", "fluid", "water", "timeoutSeconds", cap));
                     break;
                  case "fill_lava":
                     this.init("game:bucket", AiJson.objectOf("action", "fill", "fluid", "lava", "timeoutSeconds", cap));
                     break;
                  case "collect":
                     this.init("game:collect", AiJson.objectOf("item", itemId, "count", amount, "radius", 32, "timeoutSeconds", cap));
                     break;
                  default:
                     this.init("game:obtain", AiJson.objectOf("item", itemId, "count", amount, "timeoutSeconds", cap));
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
                  this.init(AiNetherEntry.legEvent(this.legId, "CANCELLED", why, null));
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
                  ? explicitVillage == null || AiNetherEntry.shouldVisitVillage(false, this.load(explicitVillage), this.bedCount(), bedsTarget)
                  : false;
            }

            double load(BlockPos pos) {
               double dx = pos.getX() - mc.player.getX();
               double dz = pos.getZ() - mc.player.getZ();
               return Math.hypot(dx, dz);
            }

            boolean flush_Z_3() {
               BlockPos origin = mc.player.blockPosition();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.isExposedLavaSource(mc.level, pos) && !AiNetherEntry.init_Z(pos)) {
                     return true;
                  }
               }

               return false;
            }

            boolean close_Z_2() {
               return !this.portalBuildStarted
                  && (this.count("minecraft:diamond_pickaxe") > 0 || this.count("minecraft:netherite_pickaxe") > 0 || this.count("minecraft:obsidian") >= 10);
            }

            AiOperation.Result startPortalBuild(String why) {
               this.portalBuildStarted = true;
               this.drainChild();
               this.step = 20;
               AutismClientAddon.LOG.info("[AI] nether entry: building the portal from obsidian: {}", why);
               return this.write("Building the portal from obsidian: " + why);
            }

            AiOperation.Result push() {
               if (this.step == 20) {
                  int framed = AiDimension.framedObsidian();
                  if (this.child == null && this.count("minecraft:obsidian") + framed >= 10) {
                     this.step = 21;
                     return this.write(framed > 0 ? "Obsidian for the frame begun at " + AiDimension.frameOrigin.toShortString() : "Ten obsidian held");
                  }

                  if (this.child == null) {
                     this.legId = "game:obtain";
                     this.init(AiNetherEntry.legEvent("game:obtain", "STARTED", "", null));
                     this.start = this.count("minecraft:obsidian");
                     this.child = AiObsidian.load(Math.max(1, 10 - AiDimension.framedObsidian()), this.read_I(300));
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.write("Mining obsidian for the frame (" + this.count("minecraft:obsidian") + "/10)");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  int held = this.count("minecraft:obsidian");
                  held += AiDimension.framedObsidian();
                  if (held < 10 && held - AiDimension.framedObsidian() > this.start && this.stop < 4) {
                     this.stop++;
                     AutismClientAddon.LOG.info("[AI] nether entry: obsidian {} of 10 ({}); casting on", held, r.message());
                     return this.write("Obsidian " + held + "/10; casting on");
                  }

                  if (held < 10 && !this.push_Z) {
                     this.push_Z = true;
                     this.sync_I = 0;
                     this.set = 0;
                     AutismClientAddon.LOG.info("[AI] nether entry: no obsidian ({}); looking for a castable pool from the surface", r.message());
                     this.init(AiNetherEntry.Inner_CachePool.LAVA);
                     this.step = 40;
                     return this.write("No obsidian (" + r.message() + "); up to the surface for a castable pool");
                  }

                  if (held < 10) {
                     return this.failPhase(AiFailure.MISSING_MATERIALS, "Obsidian for the frame: " + r.message(), AiNetherEntry.init("minecraft:obsidian", 10 - held));
                  }

                  this.step = 21;
                  return this.write("Ten obsidian held");
               } else if (this.step == 21) {
                  BlockPos begun = AiDimension.framedObsidian() > 0 ? AiDimension.frameOrigin : null;
                  if (begun != null && this.child == null && mc.player.blockPosition().distSqr(begun) <= 16.0) {
                     this.step = 22;
                     return this.write("At the frame begun");
                  }

                  if (begun != null) {
                     if (this.child == null) {
                        this.legId = "game:goto";
                        this.init(AiNetherEntry.legEvent("game:goto", "STARTED", "", null));
                        this.child = AiGoto.travelToCompat(
                           begun,
                           3.0,
                           true,
                           true,
                           true,
                           this.read_I(240),
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
                        return this.write("Back to the frame begun at " + begun.toShortString());
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     this.step = 22;
                     return this.write("Building the frame begun");
                  } else {
                     if (this.child == null && this.read_Z(3)) {
                        this.step = 22;
                        return this.write("Under open sky");
                     }

                     if (this.child == null && !this.entryTimer.init() && mc.player.onGround() && this.canInterruptNow()) {
                        this.entryTimer.start(this.deadline);
                        this.step = 23;
                        return this.write("Trying a portal frame near the pool");
                     }

                     if (this.child == null) {
                        this.legId = "game:surface";
                        this.init(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                        this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Up to open ground for the frame");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     this.step = 22;
                     return this.write("Building the frame here");
                  }
               } else if (this.step == 23) {
                  if (this.findBlock(Blocks.NETHER_PORTAL, 8, 6) == null
                     || this.child != null
                        && (!this.child.canInterruptNow() || this.child.requiresReconciliationAfterEmergency())) {
                     if (this.child == null) {
                        int seconds = this.entryTimer.secondsLeft(this.deadline);
                        if (seconds == 0) {
                           this.step = AiDimension.framedObsidian() > 0 ? 22 : 21;
                           return this.write(this.step == 22 ? "Finishing the local frame begun" : "Local frame budget exhausted; up to open ground");
                        }

                        this.init(
                           "game:dimension", AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", seconds)
                        );
                     }

                     AiOperation.Result r = this.pollChild();
                     if (r == null) {
                        return this.write("Building the local portal frame");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return r;
                     } else if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     } else if (this.findBlock(Blocks.NETHER_PORTAL, 8, 6) != null) {
                        return this.init("succeeded", "portal built locally from obsidian");
                     } else if (AiDimension.framedObsidian() > 0) {
                        this.step = 22;
                        return this.write("Finishing the local frame begun");
                     } else if (AiNetherEntry.init_Z_AiOperation_Result(r, 0, this.childTracker.requiresReconciliation())) {
                        this.step = 21;
                        return this.write("No local frame completed (" + r.message() + "); up to open ground");
                     } else {
                        return this.failPhase(r.failure(), "Local portal build failed: " + r.message(), null);
                     }
                  } else {
                     this.drainChild();
                     return this.init("succeeded", "portal built locally from obsidian");
                  }
               } else if (this.put++ % 5 != 0
                  || this.findBlock(Blocks.NETHER_PORTAL, 8, 6) == null
                  || this.child != null
                     && (!this.child.canInterruptNow() || this.child.requiresReconciliationAfterEmergency())) {
                  if (this.child == null) {
                     this.init(
                        "game:dimension",
                        AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", this.read_I(300))
                     );
                  }

                  AiOperation.Result r = this.pollChild();
                  if (r == null) {
                     return this.write("Building the portal frame");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else {
                     return this.findBlock(Blocks.NETHER_PORTAL, 8, 6) != null
                        ? this.init("succeeded", "portal built from obsidian")
                        : this.failPhase(r.failure(), "Portal build failed: " + r.message(), null);
                  }
               } else {
                  this.drainChild();
                  return this.init("succeeded", "portal built from obsidian");
               }
            }

            AiOperation.Result pop() {
               if (this.child == null && this.count("minecraft:water_bucket") > 0) {
                  this.step = 0;
                  return this.write("Water bucket held");
               }

               if (this.child == null) {
                  if (this.step == 30 && !this.read_Z(3) && !this.load_Z(48)) {
                     this.step = 31;
                  }

                  this.clear++;
                  if (this.step == 31) {
                     this.legId = "game:surface";
                     this.init(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                     this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
                  } else {
                     this.legId = "game:bucket";
                     this.init(AiNetherEntry.legEvent("game:bucket", "STARTED", "", null));
                     this.child = AiObsidian.fetchOne(this.read_I(240));
                  }
               }

               AiOperation.Result r = this.pollChild();
               if (r == null) {
                  return this.write(this.step == 31 ? "Up to open ground for water" : "Filling a water bucket");
               } else if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.get();
               } else if (this.count("minecraft:water_bucket") > 0) {
                  this.step = 0;
                  return this.write("Water bucket held");
               } else if (this.step == 30 && this.clear < 3) {
                  this.step = 31;
                  return this.write("No water reachable here (" + r.message() + "); up to the surface");
               } else if (this.step == 31) {
                  this.step = 30;
                  return this.write("Under open sky; looking for water");
               } else {
                  this.step = 0;
                  return this.write("No water bucket filled: " + r.message());
               }
            }

            AiOperation.Result run() {
               if (this.child == null && this.read_Z(3)) {
                  this.step = 1;
                  return this.write("Under open sky; scanning for a castable pool");
               }

               if (this.child == null) {
                  this.legId = "game:surface";
                  this.init(AiNetherEntry.legEvent("game:surface", "STARTED", "", null));
                  this.child = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
               }

               AiOperation.Result r = this.pollChild();
               if (r == null) {
                  return this.write("Up to the surface for a castable pool");
               }

               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.get();
               }

               this.step = 1;
               return this.write("Scanning for a castable pool");
            }

            BlockPos castableLavaPool() {
               BlockPos found = this.load_BlockPos_4();
               return found != null && AiPortalCast.init_Z(mc.level, found) ? found : null;
            }

            boolean load_Z(int radius) {
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

            boolean read_Z(int radius) {
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

            BlockPos load_BlockPos_4() {
               BlockPos origin = mc.player.blockPosition();
               List<BlockPos> open = new ArrayList<>();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.isExposedLavaSource(mc.level, pos) && !AiNetherEntry.init_Z(pos)) {
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

            BlockPos read_BlockPos_3() {
               BlockPos origin = mc.player.blockPosition();
               BlockPos best = null;

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-24, -8, -24), origin.offset(24, 8, 24))) {
                  if (AiTerrain.isLoaded(mc.level, pos)
                     && AiWorldScan.isExposedLavaSource(mc.level, pos)
                     && !AiNetherEntry.init_Z(pos)
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

            void init(AiNetherEntry.Inner_CachePool next) {
               this.phase = next;
               this.phaseStartedMillis = System.currentTimeMillis();
               this.step = 0;
               this.attempt = 1;
               this.openingSiteIndex = 0;
               this.chests = List.of();
               this.reset = 0;
               this.missingMaterials.clear();

               while (this.load_JsonArray.size() > 0) {
                  this.load_JsonArray.remove(0);
               }

               AiRunTrace.recordGlobalSplit("nether_entry_phase", next.name().toLowerCase(Locale.ROOT));
            }

            void init(AiNetherEntry.Inner_CachePool p, String reason) {
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

               if (this.load_JsonArray.size() > 0) {
                  out.add("legs", this.load_JsonArray.deepCopy());
               }

               return out;
            }

            AiOperation.Result init(String status, String detail) {
               this.drainChild();
               this.phaseHistory.add(this.phaseEntry(this.phase, status, detail));
               if ("failed".equals(status)) {
                  this.init(this.phase, detail);
               }

               AiNetherEntry.Inner_CachePool finished = this.phase;
               this.phase = null;
               return this.write("Phase " + finished.name().toLowerCase(Locale.ROOT) + " " + status + ": " + detail);
            }

            AiOperation.Result retryOrFail(AiFailure failure, String reason, JsonObject miss) {
               if (this.attempt < AiNetherEntry.maxAttempts(this.phase) && AiNetherEntry.init(this.phase, failure)) {
                  this.attempt++;
                  this.read_V_7();
                  return this.write(
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

            void read_V_7() {
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
               this.init(this.phase, reason);
               if (miss != null) {
                  this.init_JsonObject = miss;
               }

               if (!AiNetherEntry.init_Z(this.phase)) {
                  AiNetherEntry.Inner_CachePool failedPhase = this.phase;
                  this.phase = null;
                  return this.write("Optional phase " + failedPhase.name().toLowerCase(Locale.ROOT) + " failed: " + reason);
               } else {
                  this.update_Str = this.phase.name().toLowerCase(Locale.ROOT);
                  this.phase = null;
                  return this.finish(AiOperation.Status.FAILED, failure, reason);
               }
            }

            AiOperation.Result read(String message) {
               if (this.phase != null) {
                  this.phaseHistory.add(this.phaseEntry(this.phase, "succeeded", ""));
                  this.phase = null;
               }

               AiRunTrace.recordGlobalSplit("nether_entered", mc.player.blockPosition().toShortString());
               return this.finish(AiOperation.Status.SUCCEEDED, null, message);
            }

            AiOperation.Result get() {
               return this.finish(AiOperation.Status.CANCELLED, null, "Nether entry cancelled");
            }

            JsonObject init(AiOperation.Result r, String itemId) {
               JsonObject legMissing = AiNetherEntry.parseObject(r.dataJson());
               return legMissing != null ? legMissing : AiNetherEntry.init(itemId, 1);
            }

            int load_I(int cap) {
               return AiNetherEntry.load(cap, this.secondsLeft());
            }

            int read_I(int cap) {
               return AiNetherEntry.read(cap, this.secondsLeft());
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
                  this.update_Str != null
                     ? this.update_Str
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
               if (!this.sync_Str.isEmpty()) {
                  survival.addProperty("suppressedEat", this.sync_Str);
               }

               out.add("survival", survival);
               if (this.init_JsonObject != null) {
                  out.add("missing", this.init_JsonObject);
               }

               if (this.load_JsonObject != null) {
                  out.add("leg", this.load_JsonObject);
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

            AiOperation.Result write(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressJson());
            }

            AiOperation.Result finish(AiOperation.Status status, AiFailure failure, String message) {
               this.childTracker
                  .observeAll(this.child, this.write_AiOperation, this.sync_AiOperation, this.guardOp, this.nightOp);
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

               if (this.write_AiOperation != null) {
                  this.childTracker.cancel(this.write_AiOperation);
                  this.write_AiOperation = null;
               }

               if (this.sync_AiOperation != null) {
                  this.childTracker.cancel(this.sync_AiOperation);
                  this.sync_AiOperation = null;
               }
            }

            @Override
            public void cancel() {
               this.childTracker
                  .observeAll(this.child, this.write_AiOperation, this.sync_AiOperation, this.guardOp, this.nightOp);
               if (this.foodOp != null) {
                  this.preparation.cancelChild(this.foodOp);
                  this.foodOp = null;
               }

               if (!this.sync_Z) {
                  this.sync_Z = true;
                  this.cancelChild();
                  if (this.write_AiOperation != null) {
                     this.childTracker.cancel(this.write_AiOperation);
                     this.write_AiOperation = null;
                  }

                  if (this.sync_AiOperation != null) {
                     this.childTracker.cancel(this.sync_AiOperation);
                     this.sync_AiOperation = null;
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
                  && (this.write_AiOperation == null || this.write_AiOperation.canInterruptNow())
                  && (this.sync_AiOperation == null || this.sync_AiOperation.canInterruptNow())
                  && (this.guardOp == null || this.guardOp.canInterruptNow())
                  && (this.nightOp == null || this.nightOp.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.childTracker.requiresReconciliation()
                  || this.child != null && this.child.requiresReconciliationAfterEmergency()
                  || this.preparation.requiresReconciliation(this.foodOp)
                  || this.write_AiOperation != null && this.write_AiOperation.requiresReconciliationAfterEmergency()
                  || this.sync_AiOperation != null && this.sync_AiOperation.requiresReconciliationAfterEmergency()
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
      private AiOperation.Result load;
      private boolean needsReconciliation;

      AiOperation.Result tick(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result actual = child.tick();
         if (actual.terminal()) {
            boolean needsObservation = child.requiresReconciliationAfterEmergency()
               || actual.status() != AiOperation.Status.SUCCEEDED && before
               || init_Z_AiOperation_Result(actual);
            actual = AiGuardLayer.wrapPendingTermination(needsObservation, actual);
            this.recordChild(child, needsObservation, actual);
         }

         this.results.put(child, actual);
         this.ownerResult = actual;
         return actual;
      }

      private static boolean init_Z_AiOperation_Result(AiOperation.Result actual) {
         if (actual == null) {
            return false;
         }

         JsonObject data = AiRunResources.parseEvidenceJson(actual.dataJson());
         return AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false) || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
      }

      private void recordChild(AiOperation owner, boolean needsObservation, AiOperation.Result actual) {
         if (needsObservation || init_Z_AiOperation_Result(actual)) {
            this.needsReconciliation = true;
            if (this.owner == null) {
               this.owner = owner;
            }

            if (owner == this.owner && actual != null) {
               this.load = actual;
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

         if (this.load != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.load.dataJson()));
            data.addProperty("pendingChildStatus", this.load.status().name());
            data.addProperty("pendingChildFailure", this.load.failure().name());
         }
      }

      AiOperation.Result withEvidence(AiOperation.Result boundary, AiFailure failure) {
         JsonObject data = AiRunResources.parseEvidenceJson(boundary.dataJson());
         this.appendEvidence(data);
         AiOperation.Result actual = this.ownerResult != null ? this.ownerResult : this.load;
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
      static final int load = 10;
      private final LongSupplier clock;
      private long deadline;
      private boolean started;

      Inner_StreamUtil() {
         this(System::nanoTime);
      }

      Inner_StreamUtil(LongSupplier now) {
         this.clock = now;
      }

      boolean init() {
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
