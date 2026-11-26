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
   static final int init_I = 1800;
   static final int load_I = 300;
   static final int read_I = 3600;
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
   static final long init_J = 20000L;
   static final int run = 3;
   static final int get = 2;
   static final int set = 10;
   static final int put = 6;
   static final int add = 12;
   static final double init_D = 750.0;
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
   static final long load_J = 12500L;
   static final long read_J = 23460L;
   private static final Map<BlockPos, Long> init_Map = new ConcurrentHashMap<>();
   private static final long write_J = 600000000000L;
   private static final Map<BlockPos, Long> load_Map = new ConcurrentHashMap<>();

   private AiNetherEntry() {
   }

   static boolean init_Z(int stonePickaxes) {
      return stonePickaxes >= 1;
   }

   static boolean init(boolean openingDone, boolean legActive) {
      return openingDone && !legActive;
   }

   static boolean load(boolean nearby, boolean legActive) {
      return nearby && !legActive;
   }

   static boolean init_Z(int foodLevel, int foodItems) {
      return foodLevel <= 10 || foodItems < 6;
   }

   static boolean init_Z_D(double distance, boolean canInterrupt, boolean requiresReconciliation) {
      return distance > 40.0 && canInterrupt && !requiresReconciliation;
   }

   static boolean init_Z_I(int ironIngots, int flint, int buckets, int flintAndSteel, int minIron) {
      return buckets >= 1 && flintAndSteel >= 1;
   }

   static int init_I_I(int ironIngots, int buckets, int flintAndSteel) {
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

   static boolean init_Z_Z(boolean skipVillage, double villageDistance, int bedsHeld, int bedsTarget) {
      return !skipVillage && villageDistance <= 750.0 && bedsHeld < bedsTarget;
   }

   static String init_Str_J(long dayTime, boolean overworld, boolean hasBed) {
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

   static int init_I(AiNetherEntry.Inner_CachePool phase) {
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

   static int init_I(int baseCap, int attempt) {
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

   static int init_I(int seconds) {
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

   static JsonObject init_JsonObject_Str(String legId, String status, String message, String dataJson) {
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

   static JsonObject init_JsonObject(String dataJson) {
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

   static BlockPos init_BlockPos_Str(String dataJson, String arrayKey, double maxDistance) {
      List<BlockPos> all = init_List_Str(dataJson, arrayKey, 1, maxDistance);
      return all.isEmpty() ? null : all.get(0);
   }

   static List<BlockPos> init_List_Str(String dataJson, String arrayKey, int cap) {
      return init_List_Str(dataJson, arrayKey, cap, 1.7976931348623157E308);
   }

   static List<BlockPos> init_List_Str(String dataJson, String arrayKey, int cap, double maxDistance) {
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

   static String init_Str(String itemId) {
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
      AiDimension.init_V();
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
         final int timeoutSeconds = init_I(AiJson.getInt(args, "timeoutSeconds", 1800));
         final AiItems.ItemSpec logTarget = AiItems.resolve("log");
         final AiItems.ItemSpec bedTarget = AiItems.resolve("bed");
         return new AiOperation() {
            final long init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
            final long load_J = System.currentTimeMillis();
            final String init_Str = AiWorldIdentity.init(mc);
            final Object init_Obj = mc.getConnection();
            final UUID init_UUID = mc.player == null ? null : mc.player.getUUID();
            final long read_J = AiServerEvidence.deathRevision();
            final AiGuard.GuardPolicy init_X_CachePool = AiGuard.GuardPolicy.standard();
            final JsonArray init_JsonArray = new JsonArray();
            final List<Entry<String, Integer>> init_List = new ArrayList<>();
            final JsonArray load_JsonArray = new JsonArray();
            AiNetherEntry.Inner_CachePool init_aA_CachePool;
            long write_J = this.load_J;
            int init_I;
            int load_I;
            int read_I;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close_I = 1;
            int open_I;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            boolean write_Z;
            boolean update_Z;
            boolean sync_Z;
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiOperation read_AiOperation;
            String load_Str = "";
            String read_Str = "";
            String write_Str = "";
            String update_Str;
            String sync_Str = "";
            BlockPos init_BlockPos;
            BlockPos load_BlockPos;
            BlockPos read_BlockPos;
            BlockPos write_BlockPos;
            boolean flush_Z;
            long update_J;
            int poll_I = 20;
            AiOperation write_AiOperation;
            int push_I;
            int pop_I;
            int tick_I;
            AiOperation update_AiOperation;
            final AiPreparationReceipt init_bb = new AiPreparationReceipt();
            boolean close_Z;
            int run_I;
            int get;
            int set;
            int put;
            boolean open_Z;
            boolean poll_Z;
            boolean push_Z;
            boolean pop_Z;
            boolean tick_Z;
            boolean run_Z;
            int add;
            long sync_J;
            long flush_J;
            AiOperation sync_AiOperation;
            int clear;
            BlockPos update_BlockPos;
            BlockPos sync_BlockPos;
            List<BlockPos> load_List = List.of();
            int reset;
            long close_J = -1L;
            long open_J = -1L;
            long poll_J = -1L;
            long push_J = 0L;
            JsonObject init_JsonObject;
            JsonObject load_JsonObject;
            String flush_Str;
            AiOperation.Result init_AiOperation_Result;
            final AiNetherEntry.Inner_DataUtil init_aA_DataUtil = new AiNetherEntry.Inner_DataUtil();
            final AiNetherEntry.Inner_StreamUtil init_aA_StreamUtil = new AiNetherEntry.Inner_StreamUtil();
            int start;
            int stop;

            double init_D_3(BlockPos pos) {
               return Math.hypot(pos.getX() + 0.5 - mc.player.getX(), pos.getZ() + 0.5 - mc.player.getZ());
            }

            boolean init_Z(int radius) {
               for (BlockPos pos : AiWorldScan.init(autismclient.modules.am.init_I(), 2, 32)) {
                  if (pos.distSqr(mc.player.blockPosition()) <= (double)radius * radius) {
                     return true;
                  }
               }

               return false;
            }

            @Override
            public AiOperation.Result tick() {
               if (!this.sync_Z && mc.player != null && mc.level != null) {
                  if (this.init_Obj != mc.getConnection() || !this.init_Str.equals(AiWorldIdentity.init(mc)) || !mc.player.getUUID().equals(this.init_UUID)) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(
                        AiOperation.Status.UNCERTAIN, null, "World, connection or player changed during Nether entry"
                     );
                  }

                  if (!mc.player.isAlive() || mc.player.getHealth() <= 0.0F || this.read_J != AiServerEvidence.deathRevision()) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.FAILED, AiFailure.LOW_HEALTH, "Death ended this Nether entry attempt");
                  }

                  if (mc.level.dimension() != Level.OVERWORLD && !this.init_Z_15()) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(
                        AiOperation.Status.UNCERTAIN, null, "Nether entry moved into an unsupported dimension"
                     );
                  }

                  autismclient.ai.engine.AiRestock.init(Map.of(Items.OBSIDIAN, 64));
                  if (System.nanoTime() > this.init_J) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(
                        this.canInterruptNow() ? AiOperation.Status.TIMED_OUT : AiOperation.Status.UNCERTAIN,
                        AiFailure.TIMEOUT,
                        "Nether entry deadline reached"
                           + (this.init_aA_CachePool == null ? " between phases" : " in " + this.init_aA_CachePool.name().toLowerCase(Locale.ROOT))
                     );
                  }

                  if (this.init_Z_15()) {
                     if (this.init_aA_CachePool == AiNetherEntry.Inner_CachePool.ENTER && this.init_AiOperation != null && "game:dimension".equals(this.load_Str)) {
                        AiOperation.Result arrival = this.poll_AiOperation_Result();
                        if (arrival == null) {
                           return this.write("Verifying Nether arrival");
                        } else {
                           return arrival.status() != AiOperation.Status.SUCCEEDED
                              ? this.init_AiOperation_Result_AiOperation_Status_2(arrival.status(), null, arrival.message())
                              : this.read("Verified Nether arrival");
                        }
                     } else {
                        return this.init_aA_CachePool != null
                           ? this.init_AiOperation_Result_AiOperation_Status_2(
                              AiOperation.Status.UNCERTAIN, null, "Entered the Nether outside the expected portal action"
                           )
                           : this.read("Already in the Nether");
                     }
                  } else {
                     if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                        return this.init_AiOperation_Result_AiOperation_Status_2(
                           AiOperation.Status.FAILED, AiFailure.LOW_HEALTH, AiGuard.formatLowHealthAbort("nether entry", mc.player.getHealth(), mc.player.getMaxHealth())
                        );
                     }

                     if (this.init_aA_DataUtil.init_Z()) {
                        return this.init_AiOperation_Result_AiOperation_Status_2(
                           AiOperation.Status.UNCERTAIN, null, "Interrupted Nether entry action needs observation before resuming"
                        );
                     }

                     AiOperation.Result guarded = this.init_AiOperation_Result_15();
                     if (guarded != null) {
                        return guarded;
                     }

                     AiOperation.Result nighted = this.load_AiOperation_Result_11();
                     if (nighted != null) {
                        return nighted;
                     }

                     if (this.init_aA_CachePool == null) {
                        AiNetherEntry.Inner_CachePool next = AiNetherEntry.init(
                           false, this.read_Z_6(), this.write_Z_5(), this.sync_Z_4(), this.update_Z_5(), this.open_Z_2(), this.flush_Z_3()
                        );
                        if ((next == AiNetherEntry.Inner_CachePool.LAVA || next == AiNetherEntry.Inner_CachePool.PORTAL) && !this.flush_Z) {
                           this.write_BlockPos = AiPortalLinks.init(192);
                           if (this.write_BlockPos == null && this.poll_I-- > 0) {
                              return this.write("Checking observed portal history before making another frame");
                           }

                           this.flush_Z = true;
                           if (this.write_BlockPos != null) {
                              this.update_J = System.nanoTime() + 120000000000L;
                              this.init(AiNetherEntry.Inner_CachePool.LAVA);
                              this.init_I = this.write_BlockPos.getY() - mc.player.blockPosition().getY() > 16 && !this.read_Z(3) ? 89 : 90;
                              return this.write("Rechecking the remembered portal at " + this.write_BlockPos.toShortString());
                           }
                        }

                        this.init(next);
                     }

                     AiOperation.Result var6;
                     switch (this.init_aA_CachePool) {
                        case OPENING:
                           var6 = this.read_AiOperation_Result_7();
                           break;
                        case FOOD:
                           var6 = this.write_AiOperation_Result_4();
                           break;
                        case VILLAGE:
                           var6 = this.update_AiOperation_Result_2();
                           break;
                        case IRON_FIRE:
                           var6 = this.sync_AiOperation_Result_2();
                           break;
                        case LAVA:
                           var6 = this.flush_AiOperation_Result_2();
                           break;
                        case PORTAL:
                           var6 = this.close_AiOperation_Result_2();
                           break;
                        case ENTER:
                           var6 = this.open_AiOperation_Result_2();
                           break;
                        default:
                           throw new MatchException(null, null);
                     }

                     AiOperation.Result phaseResult = var6;
                     return this.init_AiOperation_Result == null && !this.init_aA_DataUtil.init_Z()
                        ? phaseResult
                        : this.init_AiOperation_Result_AiOperation_Status_2(
                           AiOperation.Status.UNCERTAIN,
                           null,
                           this.init_AiOperation_Result == null
                              ? "Interrupted Nether entry action needs observation before resuming"
                              : this.init_AiOperation_Result.message()
                        );
                  }
               } else {
                  return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.CANCELLED, null, "Nether entry cancelled");
               }
            }

            AiOperation.Result init_AiOperation_Result_15() {
               if (this.load_AiOperation == null) {
                  if (System.nanoTime() < this.flush_J && mc.player.getHealth() > 4.0F) {
                     return null;
                  }

                  if (!this.canInterruptNow()) {
                     return null;
                  }

                  AiGuard.StatusSnapshot vitals = AiGuard.sampleStatus();
                  AiGuard.GuardDecision decision = AiGuard.decide(this.init_X_CachePool, vitals);
                  if (AiNetherEntry.init_Z_X_DataUtil(
                     decision.action(), AiSurvival.init(mc.player.getInventory()) >= 0, mc.player.containerMenu != mc.player.inventoryMenu, mc.gui.screen() != null
                  )) {
                     this.sync_Str = decision.reason();
                     return null;
                  }

                  AiOperation action = AiGuard.createResponseOperation(this.init_X_CachePool, vitals, this.update_I_2());
                  if (action == null) {
                     return null;
                  }

                  this.read_Str = AiGuard.decisionReason(this.init_X_CachePool, vitals);
                  this.flush_I++;
                  this.init_V_4("interrupted: " + this.read_Str);
                  this.write_V_5();
                  if (this.read_AiOperation != null) {
                     this.init_aA_DataUtil.init_V_AiOperation(this.read_AiOperation);
                     this.read_AiOperation = null;
                  }

                  this.load_AiOperation = AiRunDiagnostics.init_AiOperation_Str("survival", this.read_Str, action);
                  this.sync_J = System.nanoTime();
                  return this.write("Interrupted: " + this.read_Str);
               } else {
                  AiOperation.Result r = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation(this.load_AiOperation);
                  if (!r.terminal()) {
                     return this.write("Interrupted: " + this.read_Str + " - " + r.message());
                  }

                  this.init_aA_DataUtil.init_V_AiOperation(this.load_AiOperation);
                  this.load_AiOperation = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  boolean quick = System.nanoTime() - this.sync_J < 1000000000L;
                  if (r.status() != AiOperation.Status.SUCCEEDED && quick) {
                     this.flush_J = System.nanoTime() + 3000000000L;
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED || !quick) {
                     AutismClientAddon.LOG.info("[AI] nether entry: guard {} after {}: {}", r.status(), this.read_Str, r.message());
                  }

                  return this.write("Resuming after " + this.read_Str);
               }
            }

            AiOperation.Result load_AiOperation_Result_11() {
               if (this.read_AiOperation == null && !this.canInterruptNow()) {
                  return null;
               }

               if (this.read_AiOperation != null) {
                  AiOperation.Result r = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation(this.read_AiOperation);
                  if (!r.terminal()) {
                     return this.write("Night: " + this.write_Str + " - " + r.message());
                  }

                  this.init_aA_DataUtil.init_V_AiOperation(this.read_AiOperation);
                  this.read_AiOperation = null;
                  if (r.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, r.message());
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.open_J = this.close_J;
                  } else {
                     if ("sleep".equals(this.write_Str)) {
                        this.poll_J = this.close_J;
                     }

                     this.push_J = System.currentTimeMillis() + 20000L;
                  }

                  return this.write("Resuming after the night (" + this.write_Str + ")");
               } else {
                  boolean sleepable = this.read_I_5() > 0 && this.poll_J != this.init_J();
                  if (AiOracle.init_Z() && !sleepable) {
                     return null;
                  }

                  if (!AiNetherEntry.init(AiSleep.init_J(), this.load_Z_9(), false, this.open_J, this.init_J(), this.push_J, System.currentTimeMillis())) {
                     return null;
                  }

                  String action = AiNetherEntry.init_Str_J(AiSleep.init_J(), true, sleepable);
                  if (action == null) {
                     return null;
                  }

                  this.init_V_4("night falls");
                  this.write_V_5();
                  this.flush_I++;
                  this.write_Str = action;
                  this.close_J = this.init_J();
                  int cap = Math.max(60, Math.min(600, this.update_I_2()));
                  if ("sleep".equals(action)) {
                     JsonObject sleepArgs = AiJson.objectOf("timeoutSeconds", Math.min(240, cap));
                     this.read_AiOperation = this.init_AiOperation_Str("game:sleep", sleepArgs, AiSleep.init(sleepArgs));
                  } else {
                     this.read_AiOperation = AiBurrow.init(AiJson.objectOf("timeoutSeconds", cap));
                  }

                  return this.write("Night falls; " + ("sleep".equals(action) ? "sleeping until dawn" : "burrowing until dawn"));
               }
            }

            AiOperation.Result read_AiOperation_Result_7() {
               if (AiNetherEntry.init(this.read_Z_6(), this.init_AiOperation != null)) {
                  return this.init("skipped", "tools already present");
               }

               if (this.init_I == 0) {
                  if (this.init_I_17() >= AiNetherEntry.init()) {
                     this.init_I = 3;
                     return this.write("Logs already in the bag");
                  }

                  AiOperation.Result r = this.read("log", 4, 96);
                  if (r == null) {
                     return this.write("Locating the nearest tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  List<BlockPos> sites = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_List_Str(r.dataJson(), "results", 4) : List.of();
                  this.read_BlockPos = AiNetherEntry.init(sites, this.open_I);
                  this.init_I = 1;
                  return this.write(
                     this.read_BlockPos == null
                        ? "No log located within 96 blocks; chopping from here"
                        : "Tree site " + (sites.indexOf(this.read_BlockPos) + 1) + "/" + sites.size() + " at " + this.read_BlockPos.toShortString()
                  );
               } else if (this.init_I == 1) {
                  int action = AiNetherEntry.init_I_Z(
                     this.read_BlockPos != null,
                     this.read_BlockPos != null && mc.player.blockPosition().distSqr(this.read_BlockPos) <= 64.0,
                     this.init_AiOperation != null
                  );
                  if (action != 0) {
                     this.init_V_17();
                     this.init_I = 2;
                     return this.write("At the woodline");
                  }

                  AiOperation.Result r = this.init_AiOperation_Result_BlockPos(this.read_BlockPos, 8.0, 300);
                  if (r == null) {
                     return this.write("Travelling to the tree");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.init_I = 2;
                  return this.write("At the woodline");
               } else if (this.init_I == 2) {
                  if (this.init_I_17() >= AiNetherEntry.init()) {
                     this.init_V_17();
                     this.init_I = 3;
                     return this.write("Logs already in the bag");
                  }

                  if (this.init_AiOperation == null) {
                     this.init("game:chop", AiJson.objectOf("block", "log", "count", AiNetherEntry.init(), "radius", 64, "timeoutSeconds", this.load_I(300)));
                     return this.write("Felling trees for the opening");
                  }

                  AiOperation.Result r = this.poll_AiOperation_Result();
                  if (r == null) {
                     return this.write("Felling trees for the opening");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (this.init_I_17() < AiNetherEntry.load()) {
                     return this.init_AiOperation_Result_A(
                        AiFailure.TARGET_NOT_FOUND,
                        "Only " + this.init_I_17() + " log(s) after the wood run (" + r.message() + ")",
                        AiNetherEntry.init("minecraft:oak_log", AiNetherEntry.load() - this.init_I_17())
                     );
                  }

                  this.init_I = 3;
                  return this.write("Logs gathered");
               } else if (this.init_I == 3) {
                  if (this.init_AiOperation != null || this.init_I("minecraft:wooden_pickaxe") <= 0 && this.init_I("minecraft:crafting_table") <= 0) {
                     AiOperation.Result r = this.init_AiOperation_Result_Str("minecraft:wooden_pickaxe", 1, this.init_I_2(300), this.poll_Z());
                     if (r == null) {
                        return this.write("Obtaining the wooden pickaxe");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init_AiOperation_Result_A(AiNetherEntry.init(r), "Wooden pickaxe unobtainable: " + r.message(), AiNetherEntry.init("minecraft:wooden_pickaxe", 1));
                     }

                     this.init_I = 4;
                     return this.write("Wooden pickaxe obtained");
                  } else {
                     this.init_I = 4;
                     return this.write("Wooden pickaxe covered");
                  }
               } else if (this.init_I == 4) {
                  if (this.init_AiOperation == null && this.init_I("minecraft:stone_pickaxe") > 0) {
                     this.init_I = 5;
                     return this.write("Stone pickaxe covered");
                  }

                  AiOperation.Result r = this.init_AiOperation_Result_Str("minecraft:stone_pickaxe", 1, this.init_I_2(300), this.poll_Z());
                  if (r == null) {
                     return this.write("Obtaining the stone pickaxe");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init_AiOperation_Result_A(AiNetherEntry.init(r), "Stone pickaxe unobtainable: " + r.message(), AiNetherEntry.init("minecraft:stone_pickaxe", 1));
                  }

                  this.init_I = 5;
                  return this.write("Stone pickaxe obtained");
               } else {
                  if (this.init_I == 5) {
                     if (this.init_AiOperation != null || this.init_I("minecraft:cobblestone") >= 3 && this.init_I("minecraft:stone_axe") <= 0) {
                        AiOperation.Result r = this.init_AiOperation_Result_Str("minecraft:stone_axe", 1, this.init_I_2(240), this.poll_Z());
                        if (r == null) {
                           return this.write("Cheap stone axe");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.init_I = 6;
                        return this.write("Stone axe done");
                     }

                     this.init_I = 6;
                  }

                  if (this.init_I == 6) {
                     if (this.init_AiOperation != null || this.init_I("minecraft:cobblestone") >= 2 && this.init_I("minecraft:stone_sword") <= 0) {
                        AiOperation.Result r = this.init_AiOperation_Result_Str("minecraft:stone_sword", 1, this.init_I_2(240), this.poll_Z());
                        if (r == null) {
                           return this.write("Cheap stone sword");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.init_I = 7;
                        return this.write("Stone sword done");
                     }

                     this.init_I = 7;
                  }

                  return this.read_Z_6()
                     ? this.init("succeeded", "opening tools ready")
                     : this.init_AiOperation_Result_A(AiFailure.MISSING_MATERIALS, "Opening did not produce the tools", AiNetherEntry.init("minecraft:stone_pickaxe", 1));
               }
            }

            AiOperation.Result write_AiOperation_Result_4() {
               if (!this.write_Z_5()) {
                  this.write_Z = true;
                  return this.init("skipped", "food is fine");
               }

               if (this.init_I == 0) {
                  this.init_I = 1;
                  this.update_I = this.load_I_10();
               }

               if (this.init_AiOperation == null) {
                  AiOperation hunt = AiSpeedrun.init_AiOperation_D(48.0, 12, this.load_I(180));
                  if (hunt == null) {
                     this.write_Z = true;
                     return this.init("skipped", "no food animal in sight");
                  } else {
                     this.load_Str = "game:obtain";
                     this.init(AiNetherEntry.init_JsonObject_Str(this.load_Str, "STARTED", "", null));
                     this.init_AiOperation = hunt;
                     return this.write("Hunting food");
                  }
               } else {
                  AiOperation.Result r = this.poll_AiOperation_Result();
                  if (r == null) {
                     return this.write("Hunting food");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.write_Z = true;
                     return this.init("succeeded", "food " + this.update_I + " -> " + this.load_I_10());
                  } else if (this.close_I < AiNetherEntry.init_I(AiNetherEntry.Inner_CachePool.FOOD) && AiNetherEntry.init(AiNetherEntry.Inner_CachePool.FOOD, r.failure())) {
                     this.close_I++;
                     this.init_I = 0;
                     return this.write("The hunt came up short; hunting again (attempt " + this.close_I + "/" + AiNetherEntry.init_I(AiNetherEntry.Inner_CachePool.FOOD) + ")");
                  } else {
                     this.write_Z = true;
                     return this.init("failed", "food " + this.update_I + " -> " + this.load_I_10() + " (" + r.message() + ")");
                  }
               }
            }

            AiOperation.Result update_AiOperation_Result_2() {
               if (this.init_I == 0) {
                  this.init_I = 1;
                  if (explicitVillage != null) {
                     if (!AiNetherEntry.init_Z_Z(skipVillage, this.load(explicitVillage), this.read_I_5(), bedsTarget)) {
                        this.read_Z = true;
                        return this.init("skipped", "village out of range or beds covered");
                     } else {
                        this.init_BlockPos = explicitVillage;
                        return this.write("Village at " + this.init_BlockPos.toShortString());
                     }
                  } else if (!skipVillage && this.read_I_5() < bedsTarget) {
                     this.init_I = 1;
                     return this.write("Predicting the nearest village");
                  } else {
                     this.read_Z = true;
                     return this.init("skipped", "village disabled or beds covered");
                  }
               } else if (this.init_I == 1) {
                  if (this.init_BlockPos == null) {
                     AiOperation.Result r = this.init_AiOperation_Result_8("structure:village");
                     if (r == null) {
                        return this.write("Predicting the nearest village");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_BlockPos_Str(r.dataJson(), "positions", 750.0) : null;
                     if (hit == null) {
                        this.read_Z = true;
                        return this.init(
                           "skipped",
                           r.status() == AiOperation.Status.SUCCEEDED
                              ? "no village predicted within 750 blocks"
                              : "no village prediction available (" + r.message() + ")"
                        );
                     }

                     this.init_BlockPos = hit;
                  }

                  this.init_I = 2;
                  return this.write("Travelling to the village at " + this.init_BlockPos.toShortString());
               } else if (this.init_I == 2) {
                  AiOperation.Result r = this.load_AiOperation_Result_BlockPos(this.init_BlockPos, 14.0, 300);
                  if (r == null) {
                     return this.write("Travelling to the village");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     this.read_Z = true;
                     return this.init("failed", "no route to the village (" + r.message() + ")");
                  } else {
                     this.init_I = 3;
                     return this.write("At the village; scanning for chests");
                  }
               } else if (this.init_I == 3) {
                  AiOperation.Result r = this.read("chest", 8, 48);
                  if (r == null) {
                     return this.write("Scanning the village for chests");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.load_List = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_List_Str(r.dataJson(), "results", 4) : List.of();
                  this.reset = 0;
                  this.init_I = 4;
                  return this.write(this.load_List.size() + " chest(s) to loot");
               } else if (this.init_I == 4) {
                  if (this.reset < this.load_List.size()) {
                     BlockPos chest = this.load_List.get(this.reset);
                     if (this.init_AiOperation == null) {
                        this.init(
                           "game:container",
                           AiJson.objectOf(
                              "action", "loot", "x", chest.getX(), "y", chest.getY(), "z", chest.getZ(), "timeoutSeconds", Math.min(60, this.load_I(300))
                           )
                        );
                        return this.write("Looting a village chest");
                     }

                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Looting a village chest");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.read_I++;
                     }

                     this.reset++;
                     return this.write("Chests looted: " + this.read_I);
                  } else {
                     this.init_I = 5;
                     return this.write("Scanning for hay bales");
                  }
               } else if (this.init_I == 5) {
                  AiOperation.Result r = this.read("hay_block", 8, 48);
                  if (r == null) {
                     return this.write("Scanning for hay bales");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  this.load_List = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_List_Str(r.dataJson(), "results", 8) : List.of();
                  this.reset = 0;
                  this.init_I = 6;
                  return this.write(this.load_List.size() + " hay bale(s) to harvest");
               } else if (this.init_I == 6) {
                  if (this.reset < this.load_List.size()) {
                     AiOperation.Result r = this.init_AiOperation_Result_3(this.load_List.get(this.reset));
                     if (r == null) {
                        return this.write("Harvesting a hay bale");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.write_I++;
                     }

                     this.reset++;
                     this.load_I = 20;
                     return this.write("Hay bales harvested: " + this.write_I);
                  } else {
                     this.init_I = 7;
                     this.load_List = List.of();
                     this.reset = 0;
                     return this.write(this.read_I_5() < bedsTarget ? "Scanning for beds" : "Beds covered");
                  }
               } else {
                  if (this.init_I == 7) {
                     if (this.read_I_5() < bedsTarget && !this.update_Z) {
                        AiOperation.Result r = this.read("bed", 8, 48);
                        if (r == null) {
                           return this.write("Scanning for beds");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.load_List = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_List_Str(r.dataJson(), "results", 8) : List.of();
                        this.reset = 0;
                        this.update_Z = true;
                     }

                     if (this.reset < this.load_List.size() && this.read_I_5() < bedsTarget) {
                        AiOperation.Result r = this.init_AiOperation_Result_3(this.load_List.get(this.reset));
                        if (r == null) {
                           return this.write("Taking a village bed");
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.reset++;
                        this.load_I = 20;
                        return this.write("Beds held: " + this.read_I_5());
                     }

                     this.init_I = 8;
                  }

                  this.read_Z = true;
                  return this.init(
                     "succeeded", "looted " + this.read_I + " chest(s), " + this.write_I + " hay bale(s), beds " + this.read_I_5() + "/" + bedsTarget
                  );
               }
            }

            AiOperation.Result sync_AiOperation_Result_2() {
               if (this.update_Z_5() && this.init_AiOperation == null) {
                  return this.init("skipped", "iron and fire gear already present");
               }

               if (this.init_I == 0) {
                  int ironNeed = Math.max(AiNetherEntry.init_I_I(0, this.write_I_3(), this.init_I("minecraft:flint_and_steel")), 0);
                  if (this.init_AiOperation == null && this.init_I("minecraft:iron_ingot") >= ironNeed) {
                     this.init_I = 1;
                     return this.write("Iron already in the bag");
                  }

                  AiOperation.Result r = this.load_AiOperation_Result_Str("minecraft:iron_ingot", ironNeed, this.init_I_2(420));
                  if (r == null) {
                     return this.write("Obtaining iron for the bucket and lighter");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init_AiOperation_Result_A(
                        AiNetherEntry.init(r),
                        "Iron unobtainable: " + r.message(),
                        AiNetherEntry.init(
                           "minecraft:iron_ingot",
                           Math.max(1, AiNetherEntry.init_I_I(this.init_I("minecraft:iron_ingot"), this.write_I_3(), this.init_I("minecraft:flint_and_steel")))
                        )
                     );
                  }

                  this.init_I = 1;
                  return this.write("Iron obtained");
               } else if (this.init_I != 1) {
                  if (this.init_I == 2) {
                     if (this.init_AiOperation == null && this.write_I_3() > 0) {
                        this.init_I = 3;
                        return this.write("Bucket already in the bag");
                     }

                     AiOperation.Result r = this.load("minecraft:bucket", this.init_I_2(420));
                     if (r == null) {
                        return this.write("Crafting the bucket");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init_AiOperation_Result_A(AiNetherEntry.init(r), "Bucket not craftable: " + r.message(), this.init(r, "minecraft:bucket"));
                     }

                     this.init_I = 3;
                     return this.write("Bucket crafted");
                  } else if (this.init_I == 3) {
                     if (this.init_AiOperation == null && this.init_I("minecraft:flint_and_steel") > 0) {
                        this.init_I = 4;
                        return this.write("Lighter already in the bag");
                     }

                     AiOperation.Result r = this.load("minecraft:flint_and_steel", this.init_I_2(420));
                     if (r == null) {
                        return this.write("Crafting flint and steel");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED) {
                        return this.init_AiOperation_Result_A(
                           AiNetherEntry.init(r), "Flint and steel not craftable: " + r.message(), this.init(r, "minecraft:flint_and_steel")
                        );
                     }

                     this.init_I = 4;
                     return this.write("Flint and steel crafted");
                  } else {
                     return this.update_Z_5()
                        ? this.init("succeeded", "bucket and flint_and_steel ready")
                        : this.init_AiOperation_Result_A(AiFailure.MISSING_MATERIALS, "Iron/fire gear incomplete", AiNetherEntry.init("minecraft:flint_and_steel", 1));
                  }
               } else if (this.init_AiOperation != null || this.init_I("minecraft:flint") <= 0 && this.init_I("minecraft:flint_and_steel") <= 0) {
                  AiOperation.Result r = this.load_AiOperation_Result_Str("minecraft:flint", 1, this.init_I_2(420));
                  if (r == null) {
                     return this.write("Obtaining flint from gravel");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED) {
                     return this.init_AiOperation_Result_A(AiNetherEntry.init(r), "Flint unobtainable: " + r.message(), AiNetherEntry.init("minecraft:flint", 1));
                  }

                  this.init_I = 2;
                  return this.write("Flint obtained");
               } else {
                  this.init_I = 2;
                  return this.write("Flint already in the bag");
               }
            }

            AiOperation.Result flush_AiOperation_Result_2() {
               if (this.init_I == 89) {
                  if (System.nanoTime() > this.update_J && this.canInterruptNow()) {
                     this.init_V_17();
                     return this.init("skipped", "remembered portal ascent exhausted its bounded budget; using local resources");
                  }

                  if (this.init_AiOperation == null) {
                     this.load_Str = "game:surface";
                     this.init(AiNetherEntry.init_JsonObject_Str(this.load_Str, "STARTED", "", null));
                     int seconds = (int)Math.max(1L, (this.update_J - System.nanoTime()) / 1000000000L);
                     this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", Math.min(120, seconds)));
                  }

                  AiOperation.Result ascent = this.poll_AiOperation_Result();
                  if (ascent == null) {
                     return this.write("Surfacing toward the remembered portal at " + this.write_BlockPos.toShortString());
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

                  this.init_I = 90;
                  return this.write("Surface reached; rechecking the remembered portal");
               } else if (this.init_I == 90) {
                  if (System.nanoTime() > this.update_J && this.canInterruptNow()) {
                     this.init_V_17();
                     return this.init("skipped", "remembered portal walk exhausted its bounded budget; using local resources");
                  } else {
                     AiOperation.Result route = this.init_AiOperation_Result_BlockPos(this.write_BlockPos, 4.0, 120);
                     if (route == null) {
                        return this.write("Rechecking the remembered portal at " + this.write_BlockPos.toShortString());
                     } else {
                        return route.status() == AiOperation.Status.UNCERTAIN
                           ? route
                           : this.init(
                              "skipped",
                              this.open_Z_2()
                                 ? "remembered portal re-observed; no new frame needed"
                                 : "remembered portal could not be re-observed; using local resources (" + route.message() + ")"
                           );
                     }
                  }
               } else {
                  if (this.init_I >= 40) {
                     return this.run();
                  }

                  if (this.init_I >= 30) {
                     return this.pop();
                  }

                  if (this.init_I == 0 && this.clear == 0 && this.init_I("minecraft:water_bucket") == 0 && this.init_I("minecraft:bucket") > 0) {
                     this.init_I = 30;
                     return this.write("Filling a water bucket before the lava");
                  }

                  if (this.init_I == 0 && AiDimension.init_I() > 0 && this.close_Z_2()) {
                     this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                     return this.load_AiOperation_Result("the frame begun at " + AiDimension.init_BlockPos.toShortString() + " holds " + AiDimension.init_I() + " obsidian");
                  }

                  if (AiNetherEntry.load(this.flush_Z_3(), this.init_AiOperation != null)) {
                     if (this.poll_Z) {
                        this.poll_Z = false;
                        this.init_V_17();
                        this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                        return this.load_AiOperation_Result("no pool in loaded terrain takes a cast");
                     }

                     if (!this.push_Z || this.init_BlockPos_8() != null) {
                        return this.init("skipped", "already at lava");
                     }
                  }

                  if (this.init_I == 0) {
                     this.init_I = 1;
                     return this.write("Scanning loaded terrain for lava");
                  }

                  if (this.init_I == 1) {
                     BlockPos hit = null;
                     BlockPos open = null;
                     int checked = 0;

                     for (BlockPos pos : AiWorldScan.init(autismclient.modules.am.init_I(), 2, 32)) {
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
                        this.init_I = 3;
                        return this.write("No castable pool; obsidian from the pool at " + open.toShortString());
                     }

                     if (hit == null && !this.push_Z) {
                        hit = open;
                     }

                     if (hit != null) {
                        this.load_BlockPos = hit;
                        this.close_Z = true;
                        this.init_I = 3;
                        return this.write("Open lava observed at " + hit.toShortString());
                     }

                     if (AiOracle.init_Z()) {
                        if (this.run_I >= 8) {
                           return this.init_AiOperation_Result_A(AiFailure.TARGET_NOT_FOUND, "No open lava after " + this.run_I + " exploration legs", null);
                        }

                        this.init_I = 5;
                        return this.write("No open lava in loaded terrain; exploring");
                     } else {
                        if (this.load_Z) {
                           return this.init_AiOperation_Result_A(
                              AiFailure.TARGET_NOT_FOUND, "No lava observed near the predicted ruined portal; explore or re-issue closer to a pool", null
                           );
                        }

                        this.init_I = 2;
                        return this.write("No lava in loaded terrain; predicting a ruined portal");
                     }
                  } else if (this.init_I == 2) {
                     AiOperation.Result r = this.init_AiOperation_Result_8("structure:ruined_portal");
                     if (r == null) {
                        return this.write("Predicting a ruined portal");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     BlockPos hit = r.status() == AiOperation.Status.SUCCEEDED ? AiNetherEntry.init_BlockPos_Str(r.dataJson(), "positions", 1.7976931348623157E308) : null;
                     if (hit == null) {
                        return this.init_AiOperation_Result_A(AiFailure.TARGET_NOT_FOUND, "No lava in loaded terrain and no ruined portal prediction (" + r.message() + ")", null);
                     }

                     this.load_Z = true;
                     this.load_BlockPos = hit;
                     this.close_Z = false;
                     this.init_I = 3;
                     return this.write("Ruined portal predicted at " + hit.toShortString());
                  } else {
                     if (this.init_I == 3) {
                        AiOperation.Result r = this.close_Z
                           ? this.init(this.load_BlockPos, 240)
                           : this.init_AiOperation_Result_BlockPos(this.load_BlockPos, 16.0, 240);
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

                           return this.init_AiOperation_Result_A(AiFailure.UNREACHABLE, "No route to " + this.load_BlockPos.toShortString() + " (" + r.message() + ")", null);
                        }

                        if (!this.close_Z) {
                           this.init_I = 0;
                           return this.write("At the ruined portal; scanning for its lava");
                        }

                        if (!this.flush_Z_3()) {
                           return this.init_AiOperation_Result_A(
                              AiFailure.TARGET_NOT_FOUND, "Reached " + this.load_BlockPos.toShortString() + " but no lava is observed here", null
                           );
                        }

                        this.init_I = 4;
                        if (this.poll_Z) {
                           this.poll_Z = false;
                           this.init(AiNetherEntry.Inner_CachePool.PORTAL);
                           return this.load_AiOperation_Result("no pool in loaded terrain takes a cast");
                        }
                     }

                     if (this.init_I == 5) {
                        if (this.init_AiOperation == null) {
                           int dx = this.get == 0 ? 1 : (this.get == 2 ? -1 : 0);
                           int dz = this.get == 1 ? 1 : (this.get == 3 ? -1 : 0);
                           BlockPos at = mc.player.blockPosition();
                           this.update_BlockPos = new BlockPos(at.getX() + dx * 64, at.getY(), at.getZ() + dz * 64);
                           this.load_Str = "game:goto";
                           this.init(AiNetherEntry.init_JsonObject_Str("game:goto", "STARTED", "", null));
                           this.init_AiOperation = AiGoto.init(
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

                        AiOperation.Result r = this.poll_AiOperation_Result();
                        if (r == null) {
                           return this.write("Exploring for lava toward " + this.update_BlockPos.toShortString());
                        }

                        if (r.status() == AiOperation.Status.CANCELLED) {
                           return this.get();
                        }

                        this.run_I++;
                        if (r.status() != AiOperation.Status.SUCCEEDED) {
                           this.get = this.get + 1 & 3;
                        }

                        this.init_I = 1;
                        return this.write("Exploration leg " + this.run_I + " ended (" + r.status() + "); scanning again");
                     } else {
                        return this.init("succeeded", "at lava " + (this.load_BlockPos != null ? this.load_BlockPos.toShortString() : ""));
                     }
                  }
               }
            }

            AiOperation.Result close_AiOperation_Result_2() {
               if (this.init_I >= 20) {
                  return this.push();
               }

               if (this.init_I == 0
                  && this.init_AiOperation == null
                  && !this.init_Z
                  && this.clear == 0
                  && this.init_I("minecraft:water_bucket") == 0
                  && this.init_I("minecraft:bucket") > 0) {
                  this.init(AiNetherEntry.Inner_CachePool.LAVA);
                  this.init_I = 30;
                  return this.write("Filling a water bucket before the cast");
               }

               if (this.open_Z_2()) {
                  return this.init("skipped", "a lit portal is already here");
               }

               if (this.init_I == 3) {
                  AiOperation.Result r = this.init(this.sync_BlockPos, 240);
                  if (r == null) {
                     return this.write("Walking to the lava pool at " + this.sync_BlockPos.toShortString());
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else if (r.status() != AiOperation.Status.SUCCEEDED) {
                     AiNetherEntry.init_V(this.sync_BlockPos);
                     BlockPos left = this.sync_BlockPos;
                     this.sync_BlockPos = null;
                     return this.set++ < 3
                        ? this.init("failed", "no route to the pool at " + left.toShortString())
                        : this.load_AiOperation_Result_A(AiFailure.UNREACHABLE, "No route to the lava pool at " + left.toShortString() + " (" + r.message() + ")", null);
                  } else {
                     this.init_I = 0;
                     return this.write("At the lava pool");
                  }
               } else if (this.init_I != 0) {
                  if (this.init_I == 1) {
                     if (this.init_List.isEmpty()) {
                        this.init_I = 0;
                        return this.write("Re-issuing the portal cast");
                     }

                     Entry<String, Integer> need = this.init_List.get(0);
                     if (this.init_AiOperation == null) {
                        this.init_V(need.getKey(), need.getValue());
                        return this.write("Obtaining " + need.getKey() + " for the cast");
                     }

                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Obtaining " + need.getKey() + " for the cast");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() != AiOperation.Status.SUCCEEDED && this.init_I(need.getKey()) <= 0) {
                        return this.load_AiOperation_Result_A(
                           AiFailure.MISSING_MATERIALS, "Cannot supply " + need.getKey() + " for the portal cast (" + r.message() + ")", this.init_JsonObject
                        );
                     }

                     this.init_List.remove(0);
                     return this.write("Supplied " + need.getKey());
                  } else {
                     return this.init("succeeded", "portal cast and lit");
                  }
               } else {
                  if (this.sync_I >= 3 && !this.init_Z) {
                     return this.close_Z_2()
                        ? this.load_AiOperation_Result("no cast went in 3 attempts")
                        : this.load_AiOperation_Result_A(AiFailure.MISSING_MATERIALS, "Portal cast still short of materials after 3 attempts", this.init_JsonObject);
                  }

                  if (this.init_AiOperation == null) {
                     if (this.sync_BlockPos == null) {
                        this.sync_BlockPos = this.load_BlockPos != null
                              && this.close_Z
                              && !AiNetherEntry.init_Z(this.load_BlockPos)
                              && AiPortalCast.init_Z(mc.level, this.load_BlockPos)
                           ? this.load_BlockPos
                           : this.load_BlockPos_4();
                     }

                     if (!this.init_Z && (this.sync_BlockPos == null || !AiPortalCast.init_Z(mc.level, this.sync_BlockPos)) && this.close_Z_2()) {
                        this.sync_BlockPos = null;
                        return this.load_AiOperation_Result("no pool in reach takes a cast");
                     }

                     if (this.sync_BlockPos != null && mc.player.blockPosition().distSqr(this.sync_BlockPos) > 16.0) {
                        this.init_I = 3;
                        return this.write("Walking to the lava pool at " + this.sync_BlockPos.toShortString());
                     }

                     if (!this.init_Z) {
                        this.sync_I++;
                        this.init_Z = true;
                     }

                     JsonObject castArgs = AiJson.objectOf("timeoutSeconds", this.read_I(300));
                     if (this.sync_BlockPos != null) {
                        castArgs.addProperty("x", this.sync_BlockPos.getX());
                        castArgs.addProperty("y", this.sync_BlockPos.getY());
                        castArgs.addProperty("z", this.sync_BlockPos.getZ());
                     }

                     this.init("game:portal_cast", castArgs);
                     return this.write("Casting the portal (attempt " + this.sync_I + ")");
                  } else {
                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Casting the portal (attempt " + this.sync_I + ")");
                     }

                     this.init_Z = false;
                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     if (r.status() == AiOperation.Status.SUCCEEDED) {
                        this.init_I = 2;
                        return this.write("Portal cast and lit");
                     }

                     JsonObject miss = AiNetherEntry.init_JsonObject(r.dataJson());
                     if (miss == null) {
                        if (r.status() == AiOperation.Status.TIMED_OUT) {
                           return this.write("Cast timed out; resuming (finished cells are skipped)");
                        } else {
                           BlockPos pool = this.sync_BlockPos != null ? this.sync_BlockPos : this.read_BlockPos_3();
                           this.sync_BlockPos = null;
                           AutismClientAddon.LOG
                              .info("[AI] nether entry: cast at the pool {} failed: {}", pool == null ? "?" : pool.toShortString(), r.message());
                           if (this.close_Z_2()) {
                              return this.load_AiOperation_Result(
                                 "the cast at " + (pool == null ? "the pool" : pool.toShortString()) + " failed (" + r.message() + ")"
                              );
                           } else if (pool != null && this.set < 3) {
                              this.set++;
                              AiNetherEntry.load(pool);
                              return this.init("failed", "pool at " + pool.toShortString() + " left (" + r.message() + ")");
                           } else {
                              return this.close_Z_2()
                                 ? this.load_AiOperation_Result("no pool took a cast (" + r.message() + ")")
                                 : this.load_AiOperation_Result_A(r.failure(), "Portal cast failed: " + r.message(), null);
                           }
                        }
                     } else {
                        this.init_JsonObject = miss;
                        this.init_List.clear();

                        for (Entry<String, JsonElement> e : miss.entrySet()) {
                           this.init_List.add(Map.entry(e.getKey(), Math.max(1, e.getValue().getAsInt())));
                        }

                        this.init_I = 1;
                        return this.write("Portal cast is missing " + miss + "; obtaining");
                     }
                  }
               }
            }

            AiOperation.Result open_AiOperation_Result_2() {
               if (this.init_bb.init_Z(null)) {
                  return this.init_AiOperation_Result_AiOperation_Status_2(
                     AiOperation.Status.UNCERTAIN, null, "The entry food action was interrupted before its server effects were confirmed"
                  );
               }

               if (AiRunResources.isCampaignActive()
                  && this.init_AiOperation == null
                  && this.update_AiOperation == null
                  && this.write_AiOperation == null
                  && this.sync_AiOperation == null) {
                  if (!AiRunResources.missingMandatoryGear().isEmpty() || !AiRunResources.isMandatoryArmorEquipped()) {
                     return this.load_AiOperation_Result_A(
                        AiFailure.MISSING_MATERIALS,
                        "Required diamond kit and equipped golden boots are not ready for Nether entry",
                        AiJson.GSON.toJsonTree(AiRunResources.missingMandatoryGear()).getAsJsonObject()
                     );
                  }

                  if (AiRunResources.totalSafeFoodNutrition() < 32) {
                     if (++this.tick_I > 8) {
                        return this.load_AiOperation_Result_A(
                           AiFailure.MISSING_MATERIALS,
                           "Nether entry needs carried safe food; raw meat and exhausted hunt attempts do not satisfy the gate",
                           AiJson.objectOf("safeNutrition", 32 - AiRunResources.totalSafeFoodNutrition())
                        );
                     }

                     Map<String, Integer> raw = AiSpeedrun.init_Map();
                     if (!raw.isEmpty()) {
                        this.update_AiOperation = AiForage.init_AiOperation_I(this.load_I(this.init_I_2(180)));
                     } else {
                        this.update_AiOperation = AiSpeedrun.init_AiOperation_I_I(AiRunResources.totalSafeFoodItems() + 12, 180);
                     }

                     return this.write("Securing safe nutrition before Nether entry");
                  }

                  if (mc.player.getHealth() < 16.0F || mc.player.getFoodData().getFoodLevel() < 18) {
                     this.update_AiOperation = mc.player.getFoodData().getFoodLevel() < 18
                        ? AiSurvival.init(AiJson.objectOf("food", 20, "timeoutSeconds", 20))
                        : AiCover.init(AiJson.objectOf("healTo", 18, "timeoutSeconds", 90));
                     return this.write("Restoring health and hunger before Nether entry");
                  }
               }

               if (this.update_AiOperation != null) {
                  AiOperation.Result f = this.init_bb.init_AiOperation_Result(this.update_AiOperation);
                  if (!f.terminal()) {
                     return this.write("Food for the Nether: " + f.message());
                  }

                  this.init_bb.init_V(this.update_AiOperation);
                  this.update_AiOperation = null;
                  if (f.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, f.message());
                  }
               }

               if (this.init_AiOperation == null && this.pop_I < 3 && this.load_I_10() < 14) {
                  this.pop_I++;
                  this.update_AiOperation = this.pop_I == 1 && this.read_Z(8) ? AiSpeedrun.init_AiOperation_I_I(14, 180) : AiSpeedrun.init_AiOperation_D(48.0, 14, 90);
                  if (this.update_AiOperation != null) {
                     return this.write("Food for the Nether");
                  }
               }

               if (this.write_AiOperation != null) {
                  AiOperation.Result w = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation(this.write_AiOperation);
                  if (!w.terminal()) {
                     return this.write("Filling a water bucket before the Nether: " + w.message());
                  }

                  if (w.status() == AiOperation.Status.UNCERTAIN) {
                     return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, w.message());
                  }

                  this.init_aA_DataUtil.init_V_AiOperation(this.write_AiOperation);
                  this.write_AiOperation = null;
                  if (w.status() != AiOperation.Status.SUCCEEDED) {
                     this.push_I = 9;
                  }
               }

               if (this.init_AiOperation == null && this.push_I < 4) {
                  this.write_AiOperation = AiForage.init_AiOperation_Minecraft(mc, 32.0, 2);
                  if (this.write_AiOperation != null) {
                     this.push_I++;
                     return this.write("Filling a water bucket before the Nether");
                  }
               }

               if (this.init_AiOperation == null && this.write_AiOperation == null && AiRunResources.isCampaignActive() && AiRunResources.countUsableItem("minecraft:water_bucket", 0) < 2) {
                  if (this.push_I >= 4) {
                     return this.load_AiOperation_Result_A(
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
                           this.init_D_3(home), this.sync_AiOperation.canInterruptNow(), this.sync_AiOperation.requiresReconciliationAfterEmergency()
                        )) {
                        this.init_aA_DataUtil.init_V_AiOperation(this.sync_AiOperation);
                        this.sync_AiOperation = null;
                        AutismClientAddon.LOG
                           .info(
                              "[AI] nether entry: spare obsidian left at {} ({} blocks from the portal)",
                              this.init_I("minecraft:obsidian"),
                              (int)this.init_D_3(home)
                           );
                        return this.write("Spare obsidian given up; back to the portal");
                     }

                     AiOperation.Result o = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation(this.sync_AiOperation);
                     if (!o.terminal()) {
                        return this.write("Spare obsidian for the way home (" + this.init_I("minecraft:obsidian") + "/10)");
                     }

                     if (o.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, o.message());
                     }

                     this.init_aA_DataUtil.init_V_AiOperation(this.sync_AiOperation);
                     this.sync_AiOperation = null;
                     AutismClientAddon.LOG.info("[AI] nether entry: spare obsidian {} ({})", this.init_I("minecraft:obsidian"), o.message());
                  }

                  if (this.init_AiOperation == null
                     && !this.pop_Z
                     && !AiNetherEntry.init_Z
                     && this.init_I("minecraft:obsidian") < 10
                     && this.init_I("minecraft:water_bucket") > 0
                     && (this.init_I("minecraft:diamond_pickaxe") > 0 || this.init_I("minecraft:netherite_pickaxe") > 0)
                     && this.init_Z(40)) {
                     this.pop_Z = true;
                     AiNetherEntry.init_Z = true;
                     this.sync_AiOperation = AiObsidian.load(10, 240);
                     return this.write("Spare obsidian for the way home");
                  }

                  BlockPos lit = AiDimension.load_BlockPos;
                  if (this.init_AiOperation == null && lit != null && this.add < 3 && mc.player.blockPosition().distSqr(lit) > 1600.0) {
                     this.add++;
                     this.tick_Z = true;
                     this.run_Z = false;
                     this.load_Str = "game:goto";
                     this.init(AiNetherEntry.init_JsonObject_Str("game:goto", "STARTED", "", null));
                     this.init_AiOperation = AiGoto.init(
                        lit, 3.0, true, true, true, this.read_I(240), autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST, true, 2.0
                     );
                     return this.write("Back to the portal built at " + lit.toShortString());
                  }

                  if (this.init_AiOperation != null && "game:goto".equals(this.load_Str) && this.tick_Z && !this.run_Z) {
                     AiOperation.Result back = this.poll_AiOperation_Result();
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

                  if (this.init_AiOperation != null) {
                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Entering the Nether");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.UNCERTAIN, null, r.message());
                     } else if (this.init_Z_15() && r.status() == AiOperation.Status.SUCCEEDED) {
                        this.load_V(r.dataJson());
                        return this.init("succeeded", "in the Nether" + (this.flush_Str != null ? " (" + this.flush_Str + ")" : ""));
                     } else {
                        return r.status() == AiOperation.Status.CANCELLED
                           ? this.get()
                           : this.load_AiOperation_Result_A(r.failure(), "Dimension travel failed: " + r.message(), null);
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
               this.load_Str = id;
               this.init(AiNetherEntry.init_JsonObject_Str(id, "STARTED", "", null));

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
                        var10004 = AiOracle.init_AiOperation_JsonObject(legArgs);
                        break;
                     case "game:find":
                        var10004 = AiGameplay.sync(legArgs);
                        break;
                     case "game:container":
                        var10004 = AiContainer.init_AiOperation(legArgs);
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

                  this.init_AiOperation = this.init_AiOperation_Str(id, legArgs, var10004);
               } catch (RuntimeException refused) {
                  this.init_AiOperation = AiGameOperations.immediate(AiOperation.Status.FAILED, "Leg " + id + " refused: " + refused.getMessage(), new JsonObject());
               }

               this.init_AiOperation = AiRunDiagnostics.init_AiOperation_Str("nether_entry", id + " " + AiRunResources.compactEvidenceJson(legArgs.toString()), this.init_AiOperation);
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

            AiOperation.Result poll_AiOperation_Result() {
               AiOperation.Result r = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation(this.init_AiOperation);
               if (!r.terminal()) {
                  return null;
               }

               this.init_aA_DataUtil.init_V_AiOperation(this.init_AiOperation);
               this.init_AiOperation = null;
               this.init(AiNetherEntry.init_JsonObject_Str(this.load_Str, r.status().name(), r.message(), r.dataJson()));
               if (r.status() == AiOperation.Status.UNCERTAIN) {
                  this.init_AiOperation_Result = r;
               }

               return r;
            }

            AiOperation.Result init_AiOperation_Result_Str(String item, int amount, int cap) {
               return this.init_AiOperation_Result_Str(item, amount, cap, false);
            }

            AiOperation.Result load_AiOperation_Result_Str(String item, int amount, int cap) {
               return this.init_AiOperation_Result_Str(item, amount, cap, true);
            }

            AiOperation.Result init_AiOperation_Result_Str(String item, int amount, int cap, boolean uncapped) {
               if (this.init_AiOperation == null) {
                  this.init(
                     "game:obtain", AiJson.objectOf("item", item, "count", amount, "timeoutSeconds", uncapped ? this.read_I(cap) : this.load_I(cap))
                  );
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result init_AiOperation_Result(String item, int cap) {
               return this.init_AiOperation_Result_Str_2(item, cap, false);
            }

            AiOperation.Result load(String item, int cap) {
               return this.init_AiOperation_Result_Str_2(item, cap, true);
            }

            AiOperation.Result init_AiOperation_Result_Str_2(String item, int cap, boolean uncapped) {
               if (this.init_AiOperation == null) {
                  this.init("game:craft", AiJson.objectOf("item", item, "count", 1, "timeoutSeconds", uncapped ? this.read_I(cap) : this.load_I(cap)));
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result read(String block, int limit, int radius) {
               if (this.init_AiOperation == null) {
                  this.init("game:find", AiJson.objectOf("block", block, "radius", radius, "limit", limit));
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result init_AiOperation_Result_8(String query) {
               if (this.init_AiOperation == null) {
                  this.init("game:oracle", AiJson.objectOf("query", query, "cap", 1));
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result init_AiOperation_Result_BlockPos(BlockPos target, double radius, int cap) {
               return this.init_AiOperation_Result_BlockPos(target, radius, cap, false);
            }

            AiOperation.Result load_AiOperation_Result_BlockPos(BlockPos target, double radius, int cap) {
               return this.init_AiOperation_Result_BlockPos(target, radius, cap, true);
            }

            AiOperation.Result init_AiOperation_Result_BlockPos(BlockPos target, double radius, int cap, boolean uncapped) {
               if (this.init_AiOperation == null) {
                  this.load_Str = "game:goto";
                  this.init(AiNetherEntry.init_JsonObject_Str("game:goto", "STARTED", "", null));
                  this.init_AiOperation = AiGoto.init_AiOperation_BlockPos(target, radius, false, true, true, uncapped ? this.read_I(cap) : this.load_I(cap));
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result init(BlockPos target, int cap) {
               if (this.init_AiOperation == null) {
                  this.load_Str = "game:goto";
                  this.init(AiNetherEntry.init_JsonObject_Str("game:goto", "STARTED", "", null));
                  this.init_AiOperation = AiGoto.init(target, 3.0, true, true, true, this.load_I(cap), autismclient.ai.engine.AiPathfinder.RouteStrategy.SAFEST);
               }

               return this.poll_AiOperation_Result();
            }

            AiOperation.Result init_AiOperation_Result_3(BlockPos pos) {
               if (this.init_AiOperation == null && this.load_I > 0) {
                  this.load_I--;
                  return null;
               }

               if (this.init_AiOperation == null) {
                  this.init("game:break", AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
               }

               return this.poll_AiOperation_Result();
            }

            void init_V_17() {
               if (this.init_AiOperation != null) {
                  AiOperation.Result r = this.poll_AiOperation_Result();
                  if (r == null) {
                     this.init_V_4("drained at a step boundary");
                  }
               }
            }

            void init_V(String itemId, int amount) {
               int cap = this.load_I(120);
               switch (AiNetherEntry.init_Str(itemId)) {
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

            void load_V_10() {
               if (this.init_AiOperation != null) {
                  this.init_aA_DataUtil.init_V_AiOperation(this.init_AiOperation);
                  this.init_AiOperation = null;
               }
            }

            void init_V_4(String why) {
               if (this.init_AiOperation != null) {
                  this.init_aA_DataUtil.init_V_AiOperation(this.init_AiOperation);
                  this.init_AiOperation = null;
                  this.init(AiNetherEntry.init_JsonObject_Str(this.load_Str, "CANCELLED", why, null));
               }
            }

            int init_I(String itemId) {
               return AiCraft.countItem(itemId);
            }

            int init_I_17() {
               return AiItems.countInInventory(logTarget);
            }

            int load_I_10() {
               return AiRunResources.totalSafeFoodItems();
            }

            int read_I_5() {
               return AiItems.countInInventory(bedTarget);
            }

            int write_I_3() {
               return this.init_I("minecraft:bucket") + this.init_I("minecraft:water_bucket") + this.init_I("minecraft:lava_bucket");
            }

            boolean init_Z_15() {
               return mc.level != null && "minecraft:the_nether".equals(mc.level.dimension().identifier().toString());
            }

            boolean load_Z_9() {
               return mc.level != null && "minecraft:overworld".equals(mc.level.dimension().identifier().toString());
            }

            long init_J() {
               return mc.level.getOverworldClockTime() / 24000L;
            }

            boolean read_Z_6() {
               return AiProgression.init_bx_CachePool(mc).ordinal() >= AiProgression.Inner_CachePool.STONE.ordinal();
            }

            boolean write_Z_5() {
               return !this.write_Z && AiNetherEntry.init_Z(mc.player.getFoodData().getFoodLevel(), this.load_I_10());
            }

            boolean update_Z_5() {
               return AiNetherEntry.init_Z_I(
                  this.init_I("minecraft:iron_ingot"), this.init_I("minecraft:flint"), this.write_I_3(), this.init_I("minecraft:flint_and_steel"), minIron
               );
            }

            boolean sync_Z_4() {
               return !this.read_Z && !skipVillage && this.read_I_5() < bedsTarget
                  ? explicitVillage == null || AiNetherEntry.init_Z_Z(false, this.load(explicitVillage), this.read_I_5(), bedsTarget)
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
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.init_Z_ClientLevel_BlockPos(mc.level, pos) && !AiNetherEntry.init_Z(pos)) {
                     return true;
                  }
               }

               return false;
            }

            boolean close_Z_2() {
               return !this.open_Z
                  && (this.init_I("minecraft:diamond_pickaxe") > 0 || this.init_I("minecraft:netherite_pickaxe") > 0 || this.init_I("minecraft:obsidian") >= 10);
            }

            AiOperation.Result load_AiOperation_Result(String why) {
               this.open_Z = true;
               this.init_V_17();
               this.init_I = 20;
               AutismClientAddon.LOG.info("[AI] nether entry: building the portal from obsidian: {}", why);
               return this.write("Building the portal from obsidian: " + why);
            }

            AiOperation.Result push() {
               if (this.init_I == 20) {
                  int framed = AiDimension.init_I();
                  if (this.init_AiOperation == null && this.init_I("minecraft:obsidian") + framed >= 10) {
                     this.init_I = 21;
                     return this.write(framed > 0 ? "Obsidian for the frame begun at " + AiDimension.init_BlockPos.toShortString() : "Ten obsidian held");
                  }

                  if (this.init_AiOperation == null) {
                     this.load_Str = "game:obtain";
                     this.init(AiNetherEntry.init_JsonObject_Str("game:obtain", "STARTED", "", null));
                     this.start = this.init_I("minecraft:obsidian");
                     this.init_AiOperation = AiObsidian.load(Math.max(1, 10 - AiDimension.init_I()), this.read_I(300));
                  }

                  AiOperation.Result r = this.poll_AiOperation_Result();
                  if (r == null) {
                     return this.write("Mining obsidian for the frame (" + this.init_I("minecraft:obsidian") + "/10)");
                  }

                  if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  }

                  int held = this.init_I("minecraft:obsidian");
                  held += AiDimension.init_I();
                  if (held < 10 && held - AiDimension.init_I() > this.start && this.stop < 4) {
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
                     this.init_I = 40;
                     return this.write("No obsidian (" + r.message() + "); up to the surface for a castable pool");
                  }

                  if (held < 10) {
                     return this.load_AiOperation_Result_A(AiFailure.MISSING_MATERIALS, "Obsidian for the frame: " + r.message(), AiNetherEntry.init("minecraft:obsidian", 10 - held));
                  }

                  this.init_I = 21;
                  return this.write("Ten obsidian held");
               } else if (this.init_I == 21) {
                  BlockPos begun = AiDimension.init_I() > 0 ? AiDimension.init_BlockPos : null;
                  if (begun != null && this.init_AiOperation == null && mc.player.blockPosition().distSqr(begun) <= 16.0) {
                     this.init_I = 22;
                     return this.write("At the frame begun");
                  }

                  if (begun != null) {
                     if (this.init_AiOperation == null) {
                        this.load_Str = "game:goto";
                        this.init(AiNetherEntry.init_JsonObject_Str("game:goto", "STARTED", "", null));
                        this.init_AiOperation = AiGoto.init(
                           begun,
                           3.0,
                           true,
                           true,
                           true,
                           this.read_I(240),
                           autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                           true,
                           2.0,
                           AiGoto.read(
                              autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(begun, 3.0), AiDimension.init_Set(begun, AiDimension.init_Direction)
                           )
                        );
                     }

                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Back to the frame begun at " + begun.toShortString());
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     this.init_I = 22;
                     return this.write("Building the frame begun");
                  } else {
                     if (this.init_AiOperation == null && this.read_Z(3)) {
                        this.init_I = 22;
                        return this.write("Under open sky");
                     }

                     if (this.init_AiOperation == null && !this.init_aA_StreamUtil.init() && mc.player.onGround() && this.canInterruptNow()) {
                        this.init_aA_StreamUtil.init_V(this.init_J);
                        this.init_I = 23;
                        return this.write("Trying a portal frame near the pool");
                     }

                     if (this.init_AiOperation == null) {
                        this.load_Str = "game:surface";
                        this.init(AiNetherEntry.init_JsonObject_Str("game:surface", "STARTED", "", null));
                        this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
                     }

                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Up to open ground for the frame");
                     }

                     if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     }

                     this.init_I = 22;
                     return this.write("Building the frame here");
                  }
               } else if (this.init_I == 23) {
                  if (this.init_BlockPos_Block(Blocks.NETHER_PORTAL, 8, 6) == null
                     || this.init_AiOperation != null
                        && (!this.init_AiOperation.canInterruptNow() || this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                     if (this.init_AiOperation == null) {
                        int seconds = this.init_aA_StreamUtil.init_I(this.init_J);
                        if (seconds == 0) {
                           this.init_I = AiDimension.init_I() > 0 ? 22 : 21;
                           return this.write(this.init_I == 22 ? "Finishing the local frame begun" : "Local frame budget exhausted; up to open ground");
                        }

                        this.init(
                           "game:dimension", AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", seconds)
                        );
                     }

                     AiOperation.Result r = this.poll_AiOperation_Result();
                     if (r == null) {
                        return this.write("Building the local portal frame");
                     } else if (r.status() == AiOperation.Status.UNCERTAIN) {
                        return r;
                     } else if (r.status() == AiOperation.Status.CANCELLED) {
                        return this.get();
                     } else if (this.init_BlockPos_Block(Blocks.NETHER_PORTAL, 8, 6) != null) {
                        return this.init("succeeded", "portal built locally from obsidian");
                     } else if (AiDimension.init_I() > 0) {
                        this.init_I = 22;
                        return this.write("Finishing the local frame begun");
                     } else if (AiNetherEntry.init_Z_AiOperation_Result(r, 0, this.init_aA_DataUtil.init_Z())) {
                        this.init_I = 21;
                        return this.write("No local frame completed (" + r.message() + "); up to open ground");
                     } else {
                        return this.load_AiOperation_Result_A(r.failure(), "Local portal build failed: " + r.message(), null);
                     }
                  } else {
                     this.init_V_17();
                     return this.init("succeeded", "portal built locally from obsidian");
                  }
               } else if (this.put++ % 5 != 0
                  || this.init_BlockPos_Block(Blocks.NETHER_PORTAL, 8, 6) == null
                  || this.init_AiOperation != null
                     && (!this.init_AiOperation.canInterruptNow() || this.init_AiOperation.requiresReconciliationAfterEmergency())) {
                  if (this.init_AiOperation == null) {
                     this.init(
                        "game:dimension",
                        AiJson.objectOf("action", "enter", "target", "the_nether", "build", true, "timeoutSeconds", this.read_I(300))
                     );
                  }

                  AiOperation.Result r = this.poll_AiOperation_Result();
                  if (r == null) {
                     return this.write("Building the portal frame");
                  } else if (r.status() == AiOperation.Status.CANCELLED) {
                     return this.get();
                  } else {
                     return this.init_BlockPos_Block(Blocks.NETHER_PORTAL, 8, 6) != null
                        ? this.init("succeeded", "portal built from obsidian")
                        : this.load_AiOperation_Result_A(r.failure(), "Portal build failed: " + r.message(), null);
                  }
               } else {
                  this.init_V_17();
                  return this.init("succeeded", "portal built from obsidian");
               }
            }

            AiOperation.Result pop() {
               if (this.init_AiOperation == null && this.init_I("minecraft:water_bucket") > 0) {
                  this.init_I = 0;
                  return this.write("Water bucket held");
               }

               if (this.init_AiOperation == null) {
                  if (this.init_I == 30 && !this.read_Z(3) && !this.load_Z(48)) {
                     this.init_I = 31;
                  }

                  this.clear++;
                  if (this.init_I == 31) {
                     this.load_Str = "game:surface";
                     this.init(AiNetherEntry.init_JsonObject_Str("game:surface", "STARTED", "", null));
                     this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
                  } else {
                     this.load_Str = "game:bucket";
                     this.init(AiNetherEntry.init_JsonObject_Str("game:bucket", "STARTED", "", null));
                     this.init_AiOperation = AiObsidian.init_AiOperation_I(this.read_I(240));
                  }
               }

               AiOperation.Result r = this.poll_AiOperation_Result();
               if (r == null) {
                  return this.write(this.init_I == 31 ? "Up to open ground for water" : "Filling a water bucket");
               } else if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.get();
               } else if (this.init_I("minecraft:water_bucket") > 0) {
                  this.init_I = 0;
                  return this.write("Water bucket held");
               } else if (this.init_I == 30 && this.clear < 3) {
                  this.init_I = 31;
                  return this.write("No water reachable here (" + r.message() + "); up to the surface");
               } else if (this.init_I == 31) {
                  this.init_I = 30;
                  return this.write("Under open sky; looking for water");
               } else {
                  this.init_I = 0;
                  return this.write("No water bucket filled: " + r.message());
               }
            }

            AiOperation.Result run() {
               if (this.init_AiOperation == null && this.read_Z(3)) {
                  this.init_I = 1;
                  return this.write("Under open sky; scanning for a castable pool");
               }

               if (this.init_AiOperation == null) {
                  this.load_Str = "game:surface";
                  this.init(AiNetherEntry.init_JsonObject_Str("game:surface", "STARTED", "", null));
                  this.init_AiOperation = autismclient.ai.engine.AiSeek.init(AiJson.objectOf("timeoutSeconds", this.read_I(240)));
               }

               AiOperation.Result r = this.poll_AiOperation_Result();
               if (r == null) {
                  return this.write("Up to the surface for a castable pool");
               }

               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.get();
               }

               this.init_I = 1;
               return this.write("Scanning for a castable pool");
            }

            BlockPos init_BlockPos_8() {
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
                  if (AiTerrain.isLoaded(mc.level, pos) && AiWorldScan.init_Z_ClientLevel_BlockPos(mc.level, pos) && !AiNetherEntry.init_Z(pos)) {
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
                     && AiWorldScan.init_Z_ClientLevel_BlockPos(mc.level, pos)
                     && !AiNetherEntry.init_Z(pos)
                     && (best == null || origin.distSqr(pos) < origin.distSqr(best))) {
                     best = pos.immutable();
                  }
               }

               return best;
            }

            boolean open_Z_2() {
               return this.init_BlockPos_Block(Blocks.NETHER_PORTAL, 48, 16) != null;
            }

            BlockPos init_BlockPos_Block(Block block, int horizontal, int vertical) {
               BlockPos origin = mc.player.blockPosition();

               for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-horizontal, -vertical, -horizontal), origin.offset(horizontal, vertical, horizontal))) {
                  if (AiTerrain.isLoaded(mc.level, pos) && mc.level.getBlockState(pos).getBlock() == block) {
                     return pos.immutable();
                  }
               }

               return null;
            }

            void load_V(String dataJson) {
               try {
                  JsonElement parsed = JsonParser.parseString(dataJson == null ? "{}" : dataJson);
                  if (parsed.isJsonObject() && parsed.getAsJsonObject().has("warning")) {
                     this.flush_Str = parsed.getAsJsonObject().get("warning").getAsString();
                  }
               } catch (RuntimeException var3) {
               }
            }

            void init(AiNetherEntry.Inner_CachePool next) {
               this.init_aA_CachePool = next;
               this.write_J = System.currentTimeMillis();
               this.init_I = 0;
               this.close_I = 1;
               this.open_I = 0;
               this.load_List = List.of();
               this.reset = 0;
               this.init_List.clear();

               while (this.load_JsonArray.size() > 0) {
                  this.load_JsonArray.remove(0);
               }

               AiRunTrace.init_V_Str_Str("nether_entry_phase", next.name().toLowerCase(Locale.ROOT));
            }

            void init(AiNetherEntry.Inner_CachePool p, String reason) {
               AiRunTrace.init_V_Str_Str(
                  "nether_entry_phase", "FAILED " + p.name().toLowerCase(Locale.ROOT) + ": " + (reason.length() > 80 ? reason.substring(0, 80) : reason)
               );
            }

            JsonObject init_JsonObject_aA_CachePool(AiNetherEntry.Inner_CachePool p, String status, String detail) {
               JsonObject out = AiJson.objectOf(
                  "name",
                  p.name().toLowerCase(Locale.ROOT),
                  "status",
                  status,
                  "step",
                  this.init_I,
                  "attempts",
                  this.close_I,
                  "elapsedMs",
                  System.currentTimeMillis() - this.write_J
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
               this.init_V_17();
               this.init_JsonArray.add(this.init_JsonObject_aA_CachePool(this.init_aA_CachePool, status, detail));
               if ("failed".equals(status)) {
                  this.init(this.init_aA_CachePool, detail);
               }

               AiNetherEntry.Inner_CachePool finished = this.init_aA_CachePool;
               this.init_aA_CachePool = null;
               return this.write("Phase " + finished.name().toLowerCase(Locale.ROOT) + " " + status + ": " + detail);
            }

            AiOperation.Result init_AiOperation_Result_A(AiFailure failure, String reason, JsonObject miss) {
               if (this.close_I < AiNetherEntry.init_I(this.init_aA_CachePool) && AiNetherEntry.init(this.init_aA_CachePool, failure)) {
                  this.close_I++;
                  this.read_V_7();
                  return this.write(
                     "Phase "
                        + this.init_aA_CachePool.name().toLowerCase(Locale.ROOT)
                        + ": attempt "
                        + this.close_I
                        + "/"
                        + AiNetherEntry.init_I(this.init_aA_CachePool)
                        + " after: "
                        + reason
                  );
               } else {
                  return this.load_AiOperation_Result_A(failure, reason, miss);
               }
            }

            void read_V_7() {
               this.init_I = 0;
               if (this.init_aA_CachePool == AiNetherEntry.Inner_CachePool.OPENING) {
                  this.open_I++;
               }
            }

            int init_I_2(int baseCap) {
               return AiNetherEntry.init_I(baseCap, this.close_I);
            }

            boolean poll_Z() {
               return this.close_I >= 3;
            }

            AiOperation.Result load_AiOperation_Result_A(AiFailure failure, String reason, JsonObject miss) {
               this.init_V_17();
               this.init_JsonArray.add(this.init_JsonObject_aA_CachePool(this.init_aA_CachePool, "failed", reason));
               this.init(this.init_aA_CachePool, reason);
               if (miss != null) {
                  this.init_JsonObject = miss;
               }

               if (!AiNetherEntry.init_Z(this.init_aA_CachePool)) {
                  AiNetherEntry.Inner_CachePool failedPhase = this.init_aA_CachePool;
                  this.init_aA_CachePool = null;
                  return this.write("Optional phase " + failedPhase.name().toLowerCase(Locale.ROOT) + " failed: " + reason);
               } else {
                  this.update_Str = this.init_aA_CachePool.name().toLowerCase(Locale.ROOT);
                  this.init_aA_CachePool = null;
                  return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.FAILED, failure, reason);
               }
            }

            AiOperation.Result read(String message) {
               if (this.init_aA_CachePool != null) {
                  this.init_JsonArray.add(this.init_JsonObject_aA_CachePool(this.init_aA_CachePool, "succeeded", ""));
                  this.init_aA_CachePool = null;
               }

               AiRunTrace.init_V_Str_Str("nether_entered", mc.player.blockPosition().toShortString());
               return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.SUCCEEDED, null, message);
            }

            AiOperation.Result get() {
               return this.init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status.CANCELLED, null, "Nether entry cancelled");
            }

            JsonObject init(AiOperation.Result r, String itemId) {
               JsonObject legMissing = AiNetherEntry.init_JsonObject(r.dataJson());
               return legMissing != null ? legMissing : AiNetherEntry.init(itemId, 1);
            }

            int load_I(int cap) {
               return AiNetherEntry.load(cap, this.update_I_2());
            }

            int read_I(int cap) {
               return AiNetherEntry.read(cap, this.update_I_2());
            }

            int update_I_2() {
               return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            }

            JsonObject init_JsonObject_30() {
               JsonObject out = AiJson.objectOf("timeoutSeconds", timeoutSeconds, "elapsedMs", System.currentTimeMillis() - this.load_J);
               JsonArray reports = this.init_JsonArray.deepCopy();
               if (this.init_aA_CachePool != null) {
                  reports.add(this.init_JsonObject_aA_CachePool(this.init_aA_CachePool, "running", ""));
               }

               out.add("phases", reports);
               out.addProperty(
                  "phase",
                  this.update_Str != null
                     ? this.update_Str
                     : (this.init_aA_CachePool != null ? this.init_aA_CachePool.name().toLowerCase(Locale.ROOT) : "done")
               );
               out.addProperty("step", this.init_I);
               if (this.init_Z_15()) {
                  BlockPos pos = mc.player.blockPosition();
                  out.add("netherPosition", AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ()));
               }

               JsonObject bag = new JsonObject();
               this.init_V_JsonObject(bag, "logs", this.init_I_17());
               this.init_V_JsonObject(bag, "wooden_pickaxe", this.init_I("minecraft:wooden_pickaxe"));
               this.init_V_JsonObject(bag, "stone_pickaxe", this.init_I("minecraft:stone_pickaxe"));
               this.init_V_JsonObject(bag, "stone_axe", this.init_I("minecraft:stone_axe"));
               this.init_V_JsonObject(bag, "stone_sword", this.init_I("minecraft:stone_sword"));
               this.init_V_JsonObject(bag, "iron_ingot", this.init_I("minecraft:iron_ingot"));
               this.init_V_JsonObject(bag, "buckets", this.write_I_3());
               this.init_V_JsonObject(bag, "water_bucket", this.init_I("minecraft:water_bucket"));
               this.init_V_JsonObject(bag, "lava_bucket", this.init_I("minecraft:lava_bucket"));
               this.init_V_JsonObject(bag, "flint", this.init_I("minecraft:flint"));
               this.init_V_JsonObject(bag, "flint_and_steel", this.init_I("minecraft:flint_and_steel"));
               this.init_V_JsonObject(bag, "beds", this.read_I_5());
               this.init_V_JsonObject(bag, "food", this.load_I_10());
               this.init_V_JsonObject(bag, "throwaway", this.init_I("minecraft:dirt") + this.init_I("minecraft:cobblestone"));
               out.add("inventory", bag);
               JsonObject survival = AiGuard.statusToJson(AiGuard.sampleStatus(), this.flush_I, this.read_Str);
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

               if (this.init_bb.init() != null) {
                  out.add("entryFoodChild", AiRunResources.compactEvidenceJson(this.init_bb.init().dataJson()));
                  out.addProperty("entryFoodChildStatus", this.init_bb.init().status().name());
               }

               if (this.flush_Str != null) {
                  out.addProperty("warning", this.flush_Str);
               }

               this.init_aA_DataUtil.init_V_JsonObject(out);
               return out;
            }

            void init_V_JsonObject(JsonObject bag, String key, int value) {
               if (value > 0) {
                  bag.addProperty(key, value);
               }
            }

            AiOperation.Result write(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_30());
            }

            AiOperation.Result init_AiOperation_Result_AiOperation_Status_2(AiOperation.Status status, AiFailure failure, String message) {
               this.init_aA_DataUtil
                  .init_V_AiOperationArr(this.init_AiOperation, this.write_AiOperation, this.sync_AiOperation, this.load_AiOperation, this.read_AiOperation);
               JsonObject data = this.init_JsonObject_30();
               AiOperation.Result result = failure == null
                  ? new AiOperation.Result(status, message, data.toString(), List.of())
                  : new AiOperation.Result(status, message, data.toString(), List.of(), failure);
               result = this.init_bb.init(result, this.init_bb.init_Z(this.update_AiOperation));
               result = this.init_aA_DataUtil.init_AiOperation_Result_AiOperation_Result_A(result, failure);
               this.cancel();
               return result;
            }

            void write_V_5() {
               if (this.update_AiOperation != null) {
                  this.init_bb.init_V(this.update_AiOperation);
                  this.update_AiOperation = null;
               }

               if (this.write_AiOperation != null) {
                  this.init_aA_DataUtil.init_V_AiOperation(this.write_AiOperation);
                  this.write_AiOperation = null;
               }

               if (this.sync_AiOperation != null) {
                  this.init_aA_DataUtil.init_V_AiOperation(this.sync_AiOperation);
                  this.sync_AiOperation = null;
               }
            }

            @Override
            public void cancel() {
               this.init_aA_DataUtil
                  .init_V_AiOperationArr(this.init_AiOperation, this.write_AiOperation, this.sync_AiOperation, this.load_AiOperation, this.read_AiOperation);
               if (this.update_AiOperation != null) {
                  this.init_bb.init_V(this.update_AiOperation);
                  this.update_AiOperation = null;
               }

               if (!this.sync_Z) {
                  this.sync_Z = true;
                  this.load_V_10();
                  if (this.write_AiOperation != null) {
                     this.init_aA_DataUtil.init_V_AiOperation(this.write_AiOperation);
                     this.write_AiOperation = null;
                  }

                  if (this.sync_AiOperation != null) {
                     this.init_aA_DataUtil.init_V_AiOperation(this.sync_AiOperation);
                     this.sync_AiOperation = null;
                  }

                  if (this.load_AiOperation != null) {
                     this.init_aA_DataUtil.init_V_AiOperation(this.load_AiOperation);
                     this.load_AiOperation = null;
                  }

                  if (this.read_AiOperation != null) {
                     this.init_aA_DataUtil.init_V_AiOperation(this.read_AiOperation);
                     this.read_AiOperation = null;
                  }
               }
            }

            @Override
            public boolean canInterruptNow() {
               return !this.init_aA_DataUtil.init_Z()
                  && !this.init_bb.init_Z(this.update_AiOperation)
                  && (this.init_AiOperation == null || this.init_AiOperation.canInterruptNow())
                  && (this.update_AiOperation == null || this.update_AiOperation.canInterruptNow())
                  && (this.write_AiOperation == null || this.write_AiOperation.canInterruptNow())
                  && (this.sync_AiOperation == null || this.sync_AiOperation.canInterruptNow())
                  && (this.load_AiOperation == null || this.load_AiOperation.canInterruptNow())
                  && (this.read_AiOperation == null || this.read_AiOperation.canInterruptNow());
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.init_aA_DataUtil.init_Z()
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.init_bb.init_Z(this.update_AiOperation)
                  || this.write_AiOperation != null && this.write_AiOperation.requiresReconciliationAfterEmergency()
                  || this.sync_AiOperation != null && this.sync_AiOperation.requiresReconciliationAfterEmergency()
                  || this.load_AiOperation != null && this.load_AiOperation.requiresReconciliationAfterEmergency()
                  || this.read_AiOperation != null && this.read_AiOperation.requiresReconciliationAfterEmergency();
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
      private final IdentityHashMap<AiOperation, AiOperation.Result> init_IdentityHashMap = new IdentityHashMap<>();
      private AiOperation init_AiOperation;
      private AiOperation.Result init_AiOperation_Result;
      private AiOperation.Result load;
      private boolean init_Z;

      AiOperation.Result init_AiOperation_Result_AiOperation(AiOperation child) {
         boolean before = child.requiresReconciliationAfterEmergency();
         AiOperation.Result actual = child.tick();
         if (actual.terminal()) {
            boolean needsObservation = child.requiresReconciliationAfterEmergency()
               || actual.status() != AiOperation.Status.SUCCEEDED && before
               || init_Z_AiOperation_Result(actual);
            actual = AiGuardLayer.init_AiOperation_Result_Z_AiOperation_Result(needsObservation, actual);
            this.init_V_AiOperation(child, needsObservation, actual);
         }

         this.init_IdentityHashMap.put(child, actual);
         this.init_AiOperation_Result = actual;
         return actual;
      }

      private static boolean init_Z_AiOperation_Result(AiOperation.Result actual) {
         if (actual == null) {
            return false;
         }

         JsonObject data = AiRunResources.parseEvidenceJson(actual.dataJson());
         return AiJson.getBoolean(data, "interruptedActionNeedsReconciliation", false) || AiJson.getBoolean(data, "reobserveBeforeResuming", false);
      }

      private void init_V_AiOperation(AiOperation owner, boolean needsObservation, AiOperation.Result actual) {
         if (needsObservation || init_Z_AiOperation_Result(actual)) {
            this.init_Z = true;
            if (this.init_AiOperation == null) {
               this.init_AiOperation = owner;
            }

            if (owner == this.init_AiOperation && actual != null) {
               this.load = actual;
            }
         }
      }

      void init_V_AiOperationArr(AiOperation... children) {
         for (AiOperation child : children) {
            if (child != null) {
               this.init_V_AiOperation(child, child.requiresReconciliationAfterEmergency(), this.init_IdentityHashMap.get(child));
            }
         }
      }

      void init_V_AiOperation(AiOperation child) {
         if (child != null) {
            this.init_V_AiOperationArr(child);
            child.cancel();
            this.init_IdentityHashMap.remove(child);
         }
      }

      boolean init_Z() {
         return this.init_Z;
      }

      void init_V_JsonObject(JsonObject data) {
         if (this.init_AiOperation_Result != null) {
            data.add("entryChild", AiRunResources.compactEvidenceJson(this.init_AiOperation_Result.dataJson()));
            data.addProperty("entryChildStatus", this.init_AiOperation_Result.status().name());
         }

         if (this.load != null) {
            data.add("pendingChild", AiRunResources.compactEvidenceJson(this.load.dataJson()));
            data.addProperty("pendingChildStatus", this.load.status().name());
            data.addProperty("pendingChildFailure", this.load.failure().name());
         }
      }

      AiOperation.Result init_AiOperation_Result_AiOperation_Result_A(AiOperation.Result boundary, AiFailure failure) {
         JsonObject data = AiRunResources.parseEvidenceJson(boundary.dataJson());
         this.init_V_JsonObject(data);
         AiOperation.Result actual = this.init_AiOperation_Result != null ? this.init_AiOperation_Result : this.load;
         AiOperation.Result result = AiNetherRun.init_AiOperation_Result_AiOperation_Status(boundary.status(), boundary.message(), data, this.init_Z, actual);
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
      static final int init_I = 90;
      static final int load = 10;
      private final LongSupplier init_LongSupplier;
      private long init_J;
      private boolean init_Z;

      Inner_StreamUtil() {
         this(System::nanoTime);
      }

      Inner_StreamUtil(LongSupplier now) {
         this.init_LongSupplier = now;
      }

      boolean init() {
         return this.init_Z;
      }

      void init_V(long entryDeadline) {
         if (!this.init_Z) {
            this.init_Z = true;
            this.init_J = Math.min(entryDeadline, this.init_LongSupplier.getAsLong() + 90000000000L);
         }
      }

      int init_I(long entryDeadline) {
         int seconds = this.init_Z ? (int)Math.max(0L, (Math.min(this.init_J, entryDeadline) - this.init_LongSupplier.getAsLong()) / 1000000000L) : 0;
         return seconds >= 10 ? seconds : 0;
      }
   }
}
