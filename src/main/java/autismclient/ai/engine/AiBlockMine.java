package autismclient.ai.engine;

import autismclient.AutismClientAddon;
import autismclient.ai.AiFailure;
import autismclient.ai.AiItems;
import autismclient.ai.AiJson;
import autismclient.ai.AiMiningPreparation;
import autismclient.ai.AiMiningQuota;
import autismclient.ai.AiVisualization;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismMultiPlayerGameModeAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.RotationMath;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public final class AiBlockMine {
   static final int init_I = 64;
   private static final boolean NAVIGATION_TRACE = Boolean.getBoolean("autism.navigation.trace");
   static final int load_I = 14;
   static final long init_J = 250000000L;
   static final int read_I = 64;
   static final int write_I = 20;
   static final double init_D = 8.0;
   static final int update_I = 14;
   static final double load_D = 9.0;
   static final int sync_I = 5;
   static final int flush_I = 256;
   static final long SCAN_BUDGET_NANOS = 1500000L;
   static final int close_I = 64;
   static final int open_I = 30;
   static final long EXPLORE_TIMEOUT_NANOS = 90000000000L;
   static final double PICKUP_HALF_WIDTH = 1.3;
   static final double write_D = 0.5;
   static final double PICKUP_HEIGHT = 2.3;
   static final double sync_D = 5.0;
   static final double GROUND_DRIFT_FACTOR = 2.2026431718061676;
   static final double ITEM_GROUND_GLIDE = 2.4271844660194173;
   static final double open_D = 0.125;
   static final int poll_I = 4;
   static final long NO_PROGRESS_NANOS = 12000000000L;
   static final int push_I = 6;
   static final int pop_I = 8;
   static final double poll_D = 20.0;
   static final double SURFACE_ACROSS_COST = 0.3;
   static final int tick_I = 2;
   static final long STRIKE_TTL_NANOS = 600000000000L;
   static final double WALK_SPEED = 4.3;
   static final double tick_D = 0.5;
   private static final Map<Long, long[]> STRIKES_BY_CELL = new HashMap<>();
   private static final long[] BREATHE_START_NANOS = new long[2];
   static final long BREATHE_REPEAT_WINDOW_NANOS = 180000000000L;
   static final long BREATHE_RECENT_NANOS = 300000000000L;
   static final int run_I = 16;
   static final double run_D = 1.5;

   private AiBlockMine() {
   }

   public static boolean init(Container inventory, ItemStack incoming) {
      if (incoming.isEmpty()) {
         return false;
      }

      for (int slot = 0; slot < Math.min(36, inventory.getContainerSize()); slot++) {
         ItemStack held = inventory.getItem(slot);
         if (held.isEmpty() || ItemStack.isSameItemSameComponents(held, incoming) && held.getCount() < held.getMaxStackSize()) {
            return true;
         }
      }

      return false;
   }

   static AiFailure inventoryFullFailure(boolean dropsOnly, Container inventory, List<ItemStack> observed) {
      if (dropsOnly && !observed.isEmpty()) {
         for (ItemStack stack : observed) {
            if (init(inventory, stack)) {
               return null;
            }
         }

         return AiFailure.INVENTORY_FULL;
      } else {
         return null;
      }
   }

   static boolean anyDropAtBody(boolean dropsOnly, Vec3 body, List<Vec3> observed) {
      if (dropsOnly) {
         for (Vec3 item : observed) {
            if (itemAtBody(body, item)) {
               return true;
            }
         }
      }

      return false;
   }

   static Set<Block> sourceBlocks(Set<Item> wanted, Predicate<Block> matches) {
      Set<Block> sources = new HashSet<>();

      for (Item item : wanted) {
         for (Block block : AiItems.sourceBlocksFor(item)) {
            if (matches.test(block)) {
               sources.add(block);
            }
         }
      }

      return Set.copyOf(sources);
   }

   static Predicate<Block> sourceBlockFilter(Set<Item> wanted, Predicate<Block> requested) {
      if (wanted.isEmpty()) {
         return requested;
      }

      Set<Block> sources = sourceBlocks(wanted, requested);
      return sources::contains;
   }

   static boolean withinRadius(BlockPos feet, BlockPos target, int radius) {
      return target.distSqr(feet) <= (double)radius * radius;
   }

   static boolean init(boolean checked, int heldGoal, int remaining, boolean ordinaryLog, boolean supported, boolean interruptible, boolean pending) {
      return !checked && heldGoal > 0 && remaining >= 8 && ordinaryLog && supported && interruptible && !pending;
   }

   static boolean dropNotWorthDetour(double dropDistance, double workDistance, int available, int needed) {
      return available < needed && dropDistance > 8.0 && dropDistance > workDistance + 8.0;
   }

   static BlockPos init(List<BlockPos> reachable, List<BlockPos> known, int needed) {
      if (needed <= 0) {
         return null;
      }

      BlockPos base = null;

      for (BlockPos cell : reachable) {
         int remaining = 0;

         for (BlockPos log : known) {
            if (log.getX() == cell.getX() && log.getZ() == cell.getZ()) {
               remaining++;
            }
         }

         if (remaining > needed && (base == null || cell.getY() < base.getY())) {
            base = cell;
         }
      }

      return base;
   }

   static boolean init(BlockPos feet, BlockPos target, boolean trunk) {
      int dx = Math.abs(target.getX() - feet.getX());
      int dz = Math.abs(target.getZ() - feet.getZ());
      int dy = target.getY() - feet.getY();
      if (dx == 0 && dz == 0) {
         return dy >= 2;
      } else {
         return Math.max(dx, dz) <= 1 && dy >= 0 ? trunk || dy <= 2 : false;
      }
   }

   static boolean isItemAtBlock(int x, int y, int z, Vec3 item) {
      return Math.abs(item.x - (x + 0.5)) <= 1.3 && Math.abs(item.z - (z + 0.5)) <= 1.3 && item.y >= y - 0.5 && item.y <= y + 2.3;
   }

   static boolean itemAtBody(Vec3 body, Vec3 item) {
      double across = 1.425;
      return Math.abs(item.x - body.x) < across && Math.abs(item.z - body.z) < across && item.y + 0.25 >= body.y - 0.5 + 0.05 && item.y <= body.y + 2.3 - 0.05;
   }

   static Long spawnCellFor(Collection<Long> pending, Vec3 position, int age) {
      if (age >= 14) {
         return null;
      }

      Long spawned = null;
      double closest = 2.25;

      for (Long cell : pending) {
         double distance = Vec3.atCenterOf(BlockPos.of(cell)).distanceToSqr(position);
         if (distance < closest) {
            spawned = cell;
            closest = distance;
         }
      }

      return spawned;
   }

   static BlockPos nextChainBlock(AiDigLine.Inner_DataUtil next, Vec3 eye, BlockPos current, AiDigLine.Inner_CachePool shapes, double reach) {
      List<BlockPos> chain = AiDigLine.chainAlong(eye, next.direction(), reach, new HashSet<>(next.chain()), Set.of(current), shapes, null);
      return chain.isEmpty() ? null : chain.get(0);
   }

   static double init(BlockPos feet, BlockPos target) {
      int dy = target.getY() - feet.getY();
      double across = Math.abs(target.getX() - feet.getX()) + Math.abs(target.getZ() - feet.getZ());
      double deepAcross = target.getY() < 0 ? 2.0 : 1.0;
      int deepDown = dy < 0 ? Math.max(0, Math.min(-dy, -target.getY())) : 0;
      return across * 2.0 * deepAcross + (dy < 0 ? -dy + deepDown : dy * 2.5);
   }

   static double surfaceRouteCost(BlockPos feet, BlockPos target, int surfaceThere) {
      double across = Math.hypot(target.getX() - feet.getX(), target.getZ() - feet.getZ());
      int down = Math.max(0, surfaceThere - 1 - target.getY());
      int deep = Math.max(0, Math.min(down, -target.getY()));
      return across * 0.3 + Math.abs(surfaceThere - feet.getY()) * 0.5 + down + deep;
   }

   static boolean init(BlockPos feet, BlockPos target, IntBinaryOperator heightAt) {
      int dx = target.getX() - feet.getX();
      int dz = target.getZ() - feet.getZ();
      int steps = Math.max(Math.abs(dx), Math.abs(dz));
      int previous = heightAt.applyAsInt(feet.getX(), feet.getZ());
      if (Math.abs(previous - feet.getY()) > 1) {
         return false;
      }

      for (int i = 1; i <= steps; i++) {
         int x = feet.getX() + (int)Math.round((double)dx * i / steps);
         int z = feet.getZ() + (int)Math.round((double)dz * i / steps);
         int next = heightAt.applyAsInt(x, z);
         if (next - previous > 1 || previous - next > 3) {
            return false;
         }

         previous = next;
      }

      return true;
   }

   public static double init(Level level, BlockPos feet, BlockPos target) {
      double cost = init(feet, target);
      int here = level.getHeight(Types.MOTION_BLOCKING, feet.getX(), feet.getZ());
      if (feet.getY() >= here - 1) {
         int there = level.getHeight(Types.MOTION_BLOCKING, target.getX(), target.getZ());
         if (there > target.getY() && init(feet, target, (x, z) -> level.getHeight(Types.MOTION_BLOCKING, x, z))) {
            cost = Math.min(cost, surfaceRouteCost(feet, target, there));
         }
      }

      return cost;
   }

   static List<BlockPos> floodConnected(Predicate<BlockPos> matches, BlockPos start, int cap) {
      List<BlockPos> out = new ArrayList<>();
      if (!matches.test(start)) {
         return out;
      }

      Set<Long> seen = new HashSet<>();
      ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
      seen.add(start.asLong());
      frontier.add(start);

      while (!frontier.isEmpty() && out.size() < cap) {
         BlockPos next = frontier.poll();
         out.add(next);

         for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
               for (int dz = -1; dz <= 1; dz++) {
                  if (dx != 0 || dy != 0 || dz != 0) {
                     BlockPos n = next.offset(dx, dy, dz);
                     if (seen.add(n.asLong()) && matches.test(n)) {
                        frontier.add(n);
                     }
                  }
               }
            }
         }
      }

      return out;
   }

   static double turnCost(RotationMath.Rotation facing, Vec3 eye, BlockPos target) {
      Vec3 centre = Vec3.atCenterOf(target);
      RotationMath.Rotation look = RotationMath.lookAt(centre, eye);
      double turn = Math.abs(Mth.wrapDegrees(look.yaw() - facing.yaw())) + Math.abs(look.pitch() - facing.pitch());
      return turn + 8.0 * Math.sqrt(centre.distanceToSqr(eye));
   }

   static int[][] sectionScanOrder(int px, int py, int pz, int chunkRadius, int minSectionY, int sections, boolean ore) {
      int ccx = px >> 4;
      int ccz = pz >> 4;
      int side = 2 * chunkRadius + 1;
      int[][] out = new int[side * side * sections][];
      double[] key = new double[out.length];
      int n = 0;

      for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
         for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
            int x0 = ccx + dx << 4;
            int z0 = ccz + dz << 4;
            double hx = px < x0 ? x0 - px : (px > x0 + 15 ? px - x0 - 15 : 0.0);
            double hz = pz < z0 ? z0 - pz : (pz > z0 + 15 ? pz - z0 - 15 : 0.0);

            for (int si = 0; si < sections; si++) {
               int y0 = minSectionY + si << 4;
               double v = py < y0 ? (y0 - py) * (ore ? 1.5 : 1.0) : (py > y0 + 15 ? py - y0 - 15 : 0.0);
               out[n] = new int[]{dx, dz, si};
               key[n++] = hx * hx + hz * hz + v * v;
            }
         }
      }

      Integer[] idx = new Integer[n];

      for (int i = 0; i < n; i++) {
         idx[i] = i;
      }

      Arrays.sort(idx, Comparator.comparingDouble(ix -> key[ix]));
      int[][] sorted = new int[n][];

      for (int i = 0; i < n; i++) {
         sorted[i] = out[idx[i]];
      }

      return sorted;
   }

   static List<int[]> chunkRingOrder(int chunkRadius) {
      List<int[]> out = new ArrayList<>();

      for (int r = 0; r <= chunkRadius; r++) {
         for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
               if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                  out.add(new int[]{dx, dz});
               }
            }
         }
      }

      return out;
   }

   public static AiOperation create(JsonObject args) {
      if (!AutismLiteVariant.enabled()) {
         AiOperation preparation = AiMiningPreparation.load(args);
         if (preparation != null) {
            return preparation;
         }
      }

      return createMine(args, Set.of());
   }

   static Set<Long> parseAvoidBox(JsonObject args) {
      if (args.has("avoid") && args.get("avoid").isJsonObject()) {
         JsonObject box = args.getAsJsonObject("avoid");
         int minX = AiJson.getInt(box, "minX", 0);
         int minY = AiJson.getInt(box, "minY", 0);
         int minZ = AiJson.getInt(box, "minZ", 0);
         int maxX = AiJson.getInt(box, "maxX", minX);
         int maxY = AiJson.getInt(box, "maxY", minY);
         int maxZ = AiJson.getInt(box, "maxZ", minZ);
         if (maxX >= minX && maxY >= minY && maxZ >= minZ && (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1) <= 65536L) {
            Set<Long> cells = new HashSet<>();

            for (int x = minX; x <= maxX; x++) {
               for (int y = minY; y <= maxY; y++) {
                  for (int z = minZ; z <= maxZ; z++) {
                     cells.add(BlockPos.asLong(x, y, z));
                  }
               }
            }

            return cells;
         } else {
            throw new IllegalArgumentException("avoid must be a box of at most 65536 cells");
         }
      } else {
         return Set.of();
      }
   }

   public static AiOperation init(JsonObject args, Set<Long> protectedCells) {
      JsonObject options = args.deepCopy();
      options.addProperty("dropsOnly", true);
      return createMine(options, Set.copyOf(protectedCells));
   }

   private static AiOperation createMine(final JsonObject args, Set<Long> callerCells) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final Set<Long> avoid = parseAvoidBox(args);
         Set<Long> keep = callerCells;
         if (!avoid.isEmpty()) {
            keep = new HashSet<>(callerCells);
            keep.addAll(avoid);
         }

         final Set<Long> protectedCells = keep;
         final String blockArg = AiJson.getString(args, "block", "").trim();
         final Set<String> blockIds = new HashSet<>();
         if (args.has("blocks") && args.get("blocks").isJsonArray()) {
            for (JsonElement e : args.getAsJsonArray("blocks")) {
               if (e.isJsonPrimitive()) {
                  blockIds.add(e.getAsString().trim());
               }
            }
         }

         if (blockArg.isEmpty() && blockIds.isEmpty()) {
            throw new IllegalArgumentException("block must be a registry id or a family like \"log\"");
         }

         final String label = !blockArg.isEmpty()
            ? blockArg
            : blockIds.stream().sorted().limit(2L).map(id -> id.substring(id.indexOf(58) + 1)).reduce((a, b) -> a + "/" + b).orElse("blocks")
               + (blockIds.size() > 2 ? "/..." : "");
         final String dropArg = AiJson.getString(args, "drop", "").trim();
         final Set<String> dropIds = new HashSet<>();
         if (args.has("drops") && args.get("drops").isJsonArray()) {
            for (JsonElement e : args.getAsJsonArray("drops")) {
               if (e.isJsonPrimitive()) {
                  dropIds.add(e.getAsString().trim());
               }
            }
         }

         final Set<Item> wantedDrops = new HashSet<>();
         if (!dropArg.isEmpty()) {
            wantedDrops.addAll(AiItems.resolveItem(dropArg).items());
         }

         for (String drop : dropIds) {
            wantedDrops.addAll(AiItems.resolveItem(drop).items());
         }

         final int held = AiJson.getInt(args, "held", 0);
         final int heldMin = Math.max(0, AiJson.getInt(args, "heldMin", 0));
         final int nearRadius = Math.max(0, AiJson.getInt(args, "nearRadius", 0));
         final AiMiningQuota quota = AiMiningQuota.fromArgs(args);
         final int count = quota == null ? Math.max(1, Math.min(640, AiJson.getInt(args, "count", 1))) : quota.totalCount();
         final int radius = Math.max(16, Math.min(128, AiJson.getInt(args, "radius", 64)));
         final boolean explore = AiJson.getBoolean(args, "explore", !AiJson.getBoolean(args, "dropsOnly", false));
         final boolean dropsOnly = AiJson.getBoolean(args, "dropsOnly", false);
         final boolean obsidianJob = blockArg.equals("obsidian")
            || blockArg.equals("minecraft:obsidian")
            || !blockIds.isEmpty() && blockIds.stream().allMatch(id -> id.equals("obsidian") || id.equals("minecraft:obsidian"));
         final boolean approach = AiJson.getBoolean(args, "approach", false);
         final boolean descendOnly = AiJson.getBoolean(args, "descendOnly", false);
         final int exploreY = args.has("exploreY") ? AiJson.getInt(args, "exploreY", 0) : -2147483648;
         final int treeLimit = Math.max(0, AiJson.getInt(args, "treeLimit", 0));
         final boolean oreJob = blockArg.contains("ore") || blockIds.stream().anyMatch(id -> id.endsWith("_ore"));
         final boolean treeMode = AiJson.getBoolean(
            args,
            "trees",
            !blockArg.isEmpty() && (blockArg.equals("log") || blockArg.endsWith("_log")) || blockIds.stream().anyMatch(id -> id.endsWith("_log"))
         );
         final long deadline = System.nanoTime() + Math.max(20, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600))) * 1000000000L;
         return new AiOperation() {
            final Object startLevel = mc.level;
            final Set<Long> blacklistedCells = new HashSet<>(avoid);
            final Set<Long> lavaSealedCells = new HashSet<>();
            final Predicate<Block> targetBlockFilter = AiBlockMine.sourceBlockFilter(
               wantedDrops, block -> this.matchesBlockId(BuiltInRegistries.BLOCK.getKey(block).toString())
            );
            final Map<Block, Boolean> blockMatchCache = new IdentityHashMap<>();
            final Map<Item, Boolean> wantedItemCache = new IdentityHashMap<>();
            final BiPredicate<BlockState, ItemStack> acceptedTool = this.buildAcceptedTool();
            final Map<Long, Double> travelCostCache = new HashMap<>();
            BlockPos travelCostFeet;
            int travelCostTick = -1;
            final Map<Long, Long> pendingDropDeadlines = new LinkedHashMap<>();
            final AiBlockMine.Inner_CachePool dropAttribution = new AiBlockMine.Inner_CachePool();
            final Set<Long> countedCells = new HashSet<>();
            final Map<Long, String> blockIdByCell = new HashMap<>();
            final Map<Long, Integer> failuresByCell = new HashMap<>();
            final int heldAtStart = this.heldWanted();
            final AiAim aim = new AiAim();
            final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
                  mc,
                  s -> mc.player.getInventory().setSelectedSlot(s),
                  this.aim::aimAt,
                  true,
                  true,
                  this.aim::tick,
                  () -> !this.aim.isSilent()
               )
               .withRotationConsumerCompat(rotation -> this.aim.setTargetRotation(rotation))
               .withAcceptedToolCompat((state, tool) -> AiItems.canDropWantedItem(mc, state, tool, wantedDrops));
            final AiBlockDigger load_RefQueue_DataUtil = new AiBlockDigger(
                  mc,
                  s -> mc.player.getInventory().setSelectedSlot(s),
                  this.aim::aimAt,
                  true,
                  false,
                  this.aim::tick,
                  () -> !this.aim.isSilent()
               )
               .withRotationConsumerCompat(rotation -> this.aim.setTargetRotation(rotation));
            AiBlockDigger read_RefQueue_DataUtil = this.init_RefQueue_DataUtil;
            List<BlockPos> knownTargets = new ArrayList<>();
            final Set<Long> treeLogs = new HashSet<>();
            final AiMiningCommitment veinCommitment = new AiMiningCommitment();
            final Set<Long> lastGoalCells = new HashSet<>();
            final Set<Long> blindStances = new HashSet<>();
            BlockPos focus = args.has("focusX")
               ? new BlockPos(AiJson.getInt(args, "focusX", 0), AiJson.getInt(args, "focusY", 0), AiJson.getInt(args, "focusZ", 0))
               : null;
            final Set<Long> stuckCells = new HashSet<>();
            final Set<Long> goalSet = new LinkedHashSet<>();
            AiGoto.GotoOp travel;
            AiBlockMine.Inner_TaskQueue currentGatherGoal;
            AiBlockMine.Inner_TaskQueue replannedDropGoal;
            BlockPos breaking;
            int descentDepth;
            BlockPos pillarDirt;
            long pillarDirtSince;
            boolean breakingTarget;
            boolean exploring;
            boolean cancelled;
            AiOperation preparation;
            AiOperation.Result lastPreparationResult;
            AiOperation breathe;
            boolean diveStrikeCounted;
            boolean toolPrepared = AiJson.getBoolean(args, "toolPrepared", false);
            boolean reconcileAfterPreparation;
            final AiPlacer waterPlacer = new AiPlacer();
            BlockPos waterCell;
            BlockPos wetTarget;
            int reachFailures;
            int brokenCount;
            int treesStarted;
            int tickCount;
            int idleAtGoalTicks;
            int breakFailures;
            int breakStartTick;
            int collectableNearbyCount;
            int nearbyDropCount;
            long exploreSince;
            long settleSince;
            AiPathfinder.PathGoal travelGoal;
            AiBlockMine.Inner_BufferHelper exploreGoal;
            AiBlockMine.Inner_StreamUtil descent;
            BlockPos descentTarget;
            final Map<String, Integer> descentFailures = new HashMap<>();
            final Set<Vec3> freshDropSpots = new HashSet<>();
            final Map<Vec3, Integer> dropCountBySpot = new HashMap<>();
            boolean collectingInBox;
            int pickupWaitStart = -1;
            final List<Vec3> inBoxItems = new ArrayList<>();
            double bestDistance;
            BlockPos progressNearest;
            long lastTickNanos;
            int progressBrokenSnapshot;
            int progressHeldSnapshot;
            long progressSince;
            final AiBlockMine.Inner_ConfigStore pathProgress = new AiBlockMine.Inner_ConfigStore();
            int lowestWorkY = 2147483647;
            int scanRadius = radius;
            List<BlockPos> scanHits = new ArrayList<>();
            int scanCursor;
            boolean scanning;
            boolean scanned;
            int[][] scanOrder;
            long scanOrderKey;
            BlockPos scanCentre;
            boolean steppingToDrop;
            final Map<Long, Double> diveCostCache = new HashMap<>();
            long diveCostTick = -1L;
            AiDigLine.Inner_DataUtil digLine;
            Set<BlockPos> unusedDigCells = Set.of();
            final List<AiDigLine.Inner_DataUtil> digLines = new ArrayList<>();
            Set<BlockPos> digLinesCells = Set.of();
            BlockPos aimCacheEnd;
            List<BlockPos> aimCacheBlocks = List.of();
            int aimCacheTick = -2147483648;
            Vec3 aimCachePoint;
            BlockPos hiddenTarget;
            BlockPos softBlocker;
            Vec3 softBlockerAim;
            private final Map<Long, long[]> standCellCache = new HashMap<>();
            private int standCellCacheTick = -2147483648;
            final Map<Long, long[]> wetCache = new HashMap<>();

            void checkAirDive() {
               LocalPlayer p = mc.player;
               if (!p.isEyeInFluid(FluidTags.WATER)) {
                  this.diveStrikeCounted = false;
               } else {
                  if (p.getAirSupply() >= p.getMaxAirSupply() - 10) {
                     this.diveStrikeCounted = false;
                  }

                  if (!this.diveStrikeCounted && !(p.getAirSupply() > p.getMaxAirSupply() * 0.45)) {
                     BlockPos target = this.breaking;
                     if (target == null) {
                        BlockPos feet = p.blockPosition();

                        for (BlockPos g : this.goalBlocks()) {
                           if (g.distSqr(feet) <= 64.0 && AiBlockMine.init(mc, g) && (target == null || g.distSqr(feet) < target.distSqr(feet))) {
                              target = g;
                           }
                        }
                     }

                     if (target != null && AiBlockMine.init(mc, target)) {
                        this.diveStrikeCounted = true;
                        AiBlockMine.recordStrike(target);
                        AutismClientAddon.LOG
                           .info(
                              "[AI] block mine {}: low on air under water going for {} ({} dive strikes)",
                              label,
                              target.toShortString(),
                              AiBlockMine.strikeCount(target)
                           );
                        if (AiBlockMine.strikeCount(target) >= 2) {
                           this.blacklistedCells.add(target.asLong());
                        }
                     }
                  }
               }
            }

            AiOperation.Result surfaceForAir(BlockPos cell) {
               if (!this.diveStrikeCounted) {
                  this.diveStrikeCounted = true;
                  AiBlockMine.recordStrike(cell);
               }

               BlockState state = mc.level.getBlockState(cell);
               double breakTicks = MiningCost.breakTicks(mc, mc.player.getMainHandItem(), state, state.getDestroySpeed(mc.level, cell));
               if (AiBlockMine.strikeCount(cell) >= 2 || breakTicks + AiBlockDigger.airTicksToSurfaceCompat(mc) + 40.0 > mc.player.getMaxAirSupply()) {
                  this.blacklistedCells.add(cell.asLong());
               }

               AutismClientAddon.LOG
                  .info(
                     "[AI] block mine {}: too little air to break {} under water ({} dive strikes){}",
                     label,
                     cell.toShortString(),
                     AiBlockMine.strikeCount(cell),
                     this.blacklistedCells.contains(cell.asLong()) ? "; left" : "; surfacing first"
                  );
               if (this.travel != null) {
                  this.travel.load(true);
               }

               if (!AutismLiteVariant.enabled()) {
                  this.breathe = autismclient.ai.AiBreathe.init(AiJson.objectOf("timeoutSeconds", 25));
               }

               return this.running("Too little air for " + cell.toShortString() + " under water; surfacing first");
            }

            Set<Long> knownTargetCells() {
               Set<Long> out = new HashSet<>();

               for (BlockPos t : this.knownTargets) {
                  out.add(t.asLong());
               }

               return out;
            }

            AiOperation.Result approachResult() {
               if (this.scanned && !this.knownTargets.isEmpty()) {
                  BlockPos feet = mc.player.blockPosition();
                  BlockPos nearest = null;

                  for (BlockPos t : this.knownTargets) {
                     if (!this.blacklistedCells.contains(t.asLong())) {
                        if (AiBlockMine.init(feet, t, false) && t.distSqr(feet) <= 16.0) {
                           return this.finish(AiOperation.Status.SUCCEEDED, "Beside the " + label + " at " + t.toShortString());
                        }

                        if (nearest == null || t.distSqr(feet) < nearest.distSqr(feet)) {
                           nearest = t;
                        }
                     }
                  }

                  return descendOnly && nearest != null && Math.abs(feet.getY() - nearest.getY()) <= 4 && (mc.player.onGround() || mc.player.isInWater())
                     ? this.finish(
                        AiOperation.Status.SUCCEEDED, "Down at the " + label + " level (y " + feet.getY() + ", nearest " + nearest.toShortString() + ")"
                     )
                     : null;
               } else {
                  return null;
               }
            }

            AiOperation.Result checkProgress(List<BlockPos> goalCells, BlockPos nearest, BlockPos feet) {
               double d = 1.7976931348623157E308;

               for (BlockPos t : goalCells) {
                  d = Math.min(d, Math.sqrt(t.distSqr(feet)));
               }

               long now = System.nanoTime();
               int heldx = this.heldWanted();
               int toGo = this.travel == null ? -1 : this.travel.remainingSteps();
               if (toGo < 0 && this.travel != null && this.travel.getPathEnd() != null) {
                  BlockPos end = this.travel.getPathEnd();
                  double e = 1.7976931348623157E308;

                  for (BlockPos t : goalCells) {
                     e = Math.min(e, Math.sqrt(t.distSqr(end)));
                  }

                  if (e < this.bestDistance - 2.0) {
                     toGo = this.travel.load_I_8();
                  }
               }

               boolean changedWork = this.progressSince == 0L
                  || this.brokenCount != this.progressBrokenSnapshot
                  || heldx != this.progressHeldSnapshot
                  || this.progressNearest != null && !goalCells.contains(this.progressNearest);
               if (changedWork) {
                  this.pathProgress.clear();
               }

               boolean along = this.pathProgress
                  .recordImproved(this.travel == null ? null : this.travel.getPathEnd(), toGo);
               boolean down = nearest != null && feet.getY() - nearest.getY() > 4 && feet.getY() < this.lowestWorkY - 1;
               if (!changedWork && !(d < this.bestDistance - 1.0) && !along && !down) {
                  if (now - this.progressSince < 12000000000L) {
                     return null;
                  }

                  int left = 0;

                  for (BlockPos t : goalCells) {
                     if (!(t.distSqr(nearest) > 36.0)) {
                        this.blacklistedCells.add(t.asLong());
                        this.treeLogs.remove(t.asLong());
                        left++;
                     }
                  }

                  AutismClientAddon.LOG
                     .info(
                        "[AI] NO PROGRESS: block mine {} got no closer than {} to {} in {} s; left {} targets there",
                        label,
                        String.format(Locale.ROOT, "%.1f", this.bestDistance),
                        nearest.toShortString(),
                        12L,
                        left
                     );
                  AutismClientAddon.LOG.info("[AI] {}", AiTerrainDump.init(mc.level, feet, nearest, 6, 4));
                  this.descent = null;
                  this.descentTarget = null;
                  this.progressSince = 0L;
                  if (this.travel != null) {
                     this.travel.cancel();
                     this.travel = null;
                     this.currentGatherGoal = null;
                     this.travelGoal = null;
                  }

                  return ++this.reachFailures >= 14
                     ? this.finish(AiOperation.Status.FAILED, "No reachable " + label + " after " + this.reachFailures + " attempts (" + this.progressText() + ")")
                     : this.running("No way to the " + label + " near " + nearest.toShortString() + "; trying elsewhere");
               } else {
                  this.lowestWorkY = changedWork ? feet.getY() : Math.min(this.lowestWorkY, feet.getY());
                  this.bestDistance = changedWork ? d : Math.min(this.bestDistance, d);
                  this.progressBrokenSnapshot = this.brokenCount;
                  this.progressHeldSnapshot = heldx;
                  this.progressSince = now;
                  this.progressNearest = nearest;
                  return null;
               }
            }

            @Override
            public boolean canInterruptNow() {
               if (this.reconcileAfterPreparation) {
                  return false;
               } else {
                  return this.preparation != null
                     ? this.preparation.canInterruptNow()
                     : (this.breaking == null || !this.read_RefQueue_DataUtil.isDiggingTargetCompat(this.breaking))
                        && !this.waterPlacer.needsReconciliation()
                        && (this.travel == null || this.travel.canInterrupt());
               }
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.reconcileAfterPreparation
                  || this.preparation != null && this.preparation.requiresReconciliationAfterEmergency()
                  || this.breaking != null && this.read_RefQueue_DataUtil.isDiggingTargetCompat(this.breaking)
                  || this.waterPlacer.needsReconciliation()
                  || this.travel != null && this.travel.requiresReconciliationAfterEmergency();
            }

            @Override
            public AiOperation.Result tick() {
               AiOperation.Result result = this.step();
               if (!AutismLiteVariant.enabled() && result.terminal() && this.reconcileAfterPreparation) {
                  result = AiMiningPreparation.init(result, true, this.lastPreparationResult);
               }

               if (!AutismLiteVariant.enabled() && !result.terminal() && this.preparation == null) {
                  AiVisualization.init(this, this.goalBlocks());
                  AiVisualization.init(
                     this,
                     this.visualTitle(),
                     this.activityLabel(),
                     held <= 0 && !dropsOnly ? this.brokenCount : Math.max(0, this.heldWanted() - this.heldAtStart),
                     held > 0 ? Math.max(0, held - this.heldAtStart) : (dropsOnly ? 0 : count)
                  );
               }

               if (!result.terminal() && this.preparation == null && mc.player != null) {
                  AiBlockDigger.tickDestroyDelayCompat(mc);
               }

               return result;
            }

            AiOperation.Result step() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  if (mc.level != this.startLevel) {
                     return this.finish(AiOperation.Status.FAILED, "World changed");
                  }

                  if (this.preparation != null) {
                     if (System.nanoTime() > deadline) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.progressText() + ")");
                     }

                     AiOperation.Result result = this.preparation.tick();
                     this.lastPreparationResult = result;
                     if (result.terminal()) {
                        this.reconcileAfterPreparation = this.reconcileAfterPreparation | this.preparation.requiresReconciliationAfterEmergency();
                        this.preparation.cancel();
                        this.preparation = null;
                        this.stopActivity();
                     }

                     return result;
                  } else {
                     if (this.goalReached()) {
                        return this.finish(AiOperation.Status.SUCCEEDED, this.doneMessage());
                     }

                     if (approach) {
                        AiOperation.Result there = this.approachResult();
                        if (there != null) {
                           return there;
                        }
                     }

                     if (System.nanoTime() > deadline) {
                        return this.finish(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.progressText() + ")");
                     }

                     long tickNow = System.nanoTime();
                     if (this.lastTickNanos != 0L && tickNow - this.lastTickNanos > 1000000000L) {
                        this.progressSince = 0L;
                     }

                     this.lastTickNanos = tickNow;
                     this.tickCount++;
                     if (this.breathe != null) {
                        AiOperation.Result breathing = this.breathe.tick();
                        if (!breathing.terminal()) {
                           return this.running("Surfacing for air: " + breathing.message());
                        }

                        this.breathe.cancel();
                        this.breathe = null;
                        AiMotionSafety.releaseMovementKeys();
                        return this.running("Breathing again; back to " + label);
                     } else {
                        this.checkAirDive();
                        if (this.tickCount == 1
                           && this.focus != null
                           && this.isTargetBlock(this.focus)
                           && this.withinScanRadius(this.focus, mc.player.blockPosition())
                           && this.isEligibleTarget(this.focus)) {
                           this.knownTargets.add(this.focus);
                           this.commitVein(this.focus);
                           this.focus = null;
                        }

                        if (this.tickCount % 200 == 0) {
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] block mine {}: tick {} at {} breaking {} ({}, {} known, goal {}, tree {}, travel {})",
                                 label,
                                 this.tickCount,
                                 mc.player.blockPosition().toShortString(),
                                 this.breaking == null ? "-" : this.breaking.toShortString(),
                                 this.progressText(),
                                 this.knownTargets.size(),
                                 this.goalSet.size(),
                                 this.treeLogs.size(),
                                 this.travel == null ? "none" : (this.travel.isAtGoal() ? "at goal" : "on")
                              );
                        }

                        if (this.breaking != null && this.tickCount - this.breakStartTick > 30 && !this.read_RefQueue_DataUtil.isDiggingTargetCompat(this.breaking)) {
                           AutismClientAddon.LOG.info("[AI] block mine {}: gave up breaking {} in place", label, this.breaking.toShortString());
                           this.blacklistedCells.add(this.breaking.asLong());
                           this.read_RefQueue_DataUtil.stopCompat();
                           this.breaking = null;
                        }

                        if (this.breaking != null) {
                           if (this.travel != null) {
                              this.travel.tick();
                           }

                           if (AiMotionAudit.isEnabled()) {
                              AiMotionAudit.note("gather dig " + this.breaking.toShortString());
                           }

                           AiBlockDigger.Inner_DataUtil_2 status = this.init_RefQueue_DataUtil_(this.breaking);
                           if (status == AiBlockDigger.Inner_DataUtil_2.init) {
                              return this.running("Breaking " + mc.level.getBlockState(this.breaking).getBlock().getName().getString());
                           }

                           BlockPos was = this.breaking;
                           this.breaking = null;
                           if (status == AiBlockDigger.Inner_DataUtil_2.load) {
                              this.breakFailures = 0;
                              if (this.breakingTarget) {
                                 this.onBroken(was);
                              }
                           } else {
                              if (this.read_RefQueue_DataUtil.lastFailureReasonCompat().equals("no tool that gets its drops")) {
                                 return this.missingTool(was);
                              }

                              if (this.read_RefQueue_DataUtil.lastFailureReasonCompat().equals("too little air to break it under water")) {
                                 return this.surfaceForAir(was);
                              }

                              this.noteCellFailure(was, "could not break it in place (" + this.read_RefQueue_DataUtil.lastFailureReasonCompat() + ")");
                              if (was.equals(this.pillarDirt)) {
                                 this.blacklistedCells.add(was.asLong());
                                 this.pillarDirt = null;
                              }

                              if (++this.breakFailures >= 14) {
                                 return this.finish(AiOperation.Status.FAILED, "Could not break " + label + " (" + this.progressText() + ")");
                              }
                           }

                           if (this.goalReached()) {
                              return this.finish(AiOperation.Status.SUCCEEDED, this.doneMessage());
                           }
                        }

                        this.scanAroundFeet();
                        this.scanTerrain();
                        this.pruneKnownTargets();
                        this.pendingDropDeadlines.values().removeIf(until -> until < System.nanoTime());
                        this.dropAttribution.entitiesByCell.keySet().retainAll(this.pendingDropDeadlines.keySet());
                        List<Vec3> drops = this.visibleDrops();
                        if (held > 0 && this.heldWanted() + this.nearbyDropCount + this.pendingDropDeadlines.size() >= held) {
                           drops = this.dropSpots(true);
                        }

                        boolean enough = held > 0 ? this.heldWanted() + this.nearbyDropCount + this.pendingDropDeadlines.size() >= held : this.countReached();
                        boolean collectFirst = enough
                           || held > 0 && this.collectableNearbyCount >= Math.max(1, held - this.heldWanted())
                           || this.hasLooseDropNearby(drops) && !this.goalInReach();
                        if (this.waterCell != null) {
                           AiPlacer.PlaceStatus status = this.waterPlacer.tick();
                           if (status == AiPlacer.PlaceStatus.WORKING) {
                              return this.running("Blocking the water next to " + this.wetTarget.toShortString());
                           }

                           if (status == AiPlacer.PlaceStatus.FAILED) {
                              this.noteCellFailure(this.wetTarget, "could not block the water beside it (" + this.waterPlacer.failureReason() + ")");
                           }

                           this.waterCell = null;
                           return this.running("Water blocked");
                        } else {
                           if (this.travel != null
                              && this.travelGoal != null
                              && mc.gameMode instanceof AutismMultiPlayerGameModeAccessor activeDig
                              && (activeDig.autism$isDestroying() || activeDig.autism$getDestroyProgress() > 0.0F)) {
                              BlockPos cell = activeDig.autism$getDestroyBlockPos();
                              if (cell != null && this.travel.getRouteDigCells().contains(cell)) {
                                 this.collectingInBox = false;
                                 this.inBoxItems.clear();
                                 return this.startTravel(this.travelGoal, "Finishing the route block at " + cell.toShortString());
                              }
                           }

                           if (!dropsOnly && !collectFirst && !approach) {
                              boolean unsettled = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()
                                 || this.travel != null && !this.travel.canInterrupt()
                                 || this.driftingOffEdge();
                              BlockPos inReach = unsettled ? null : this.pickInReach();
                              if (inReach == null
                                 && !unsettled
                                 && treeMode
                                 && held > 0
                                 && this.brokenCount > 0
                                 && this.treeLogs.isEmpty()
                                 && this.heldWanted() + this.nearbyDropCount < held
                                 && !this.hasScaffoldBlock()
                                 && this.tallTreeOverhead()
                                 && (this.pillarDirt == null || !mc.level.getBlockState(this.pillarDirt).isAir())) {
                                 BlockPos dirt = this.pillarDirt != null ? this.pillarDirt : this.findPillarDirt();
                                 if (dirt != null) {
                                    if (this.pillarDirt == null) {
                                       this.pillarDirtSince = System.nanoTime();
                                    }

                                    this.pillarDirt = dirt;
                                    inReach = dirt;
                                 }
                              }

                              if (!AutismLiteVariant.enabled()
                                 && inReach != null
                                 && AiBlockMine.init(
                                    this.toolPrepared,
                                    held,
                                    held - this.heldWanted() - this.nearbyDropCount - this.pendingDropDeadlines.size(),
                                    mc.level.getBlockState(inReach).is(BlockTags.LOGS),
                                    AiMotionSafety.isPlayerSupported(),
                                    this.canInterruptNow(),
                                    this.requiresReconciliationAfterEmergency() || !this.pendingDropDeadlines.isEmpty()
                                 )) {
                                 this.toolPrepared = true;
                                 JsonObject investment = args.deepCopy();
                                 investment.addProperty("timeoutSeconds", (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L));
                                 AiOperation prepared = AiMiningPreparation.init(investment);
                                 if (prepared != null) {
                                    this.stopActivity();
                                    this.preparation = prepared;
                                    return this.running("Preparing a faster axe for the remaining log batch");
                                 }
                              }

                              BlockPos water = inReach != null && this.isTargetBlock(inReach) ? this.adjacentWater(inReach) : null;
                              if (water != null) {
                                 Item item = this.bestScaffoldItem();
                                 if (item == null) {
                                    this.blacklistedCells.add(inReach.asLong());
                                    return this.running("No block to hold back the water at " + inReach.toShortString());
                                 }

                                 if (this.travel != null) {
                                    this.travel.load(true);
                                 }

                                 this.read_RefQueue_DataUtil.stopCompat();
                                 this.waterCell = water;
                                 this.wetTarget = inReach;
                                 this.waterPlacer.start(water, item);
                                 AutismClientAddon.LOG
                                    .info("[AI] block mine {}: blocking water at {} before breaking {}", label, water.toShortString(), inReach.toShortString());
                                 return this.running("Blocking the water next to " + inReach.toShortString());
                              }

                              if (inReach != null) {
                                 this.releaseStepKeys();
                                 if (this.travel != null) {
                                    this.travel.load(true);
                                 }

                                 this.breakingTarget = this.isTargetBlock(inReach);
                                 this.breaking = inReach;
                                 this.breakStartTick = this.tickCount;
                                 this.read_RefQueue_DataUtil.stopCompat();
                                 this.read_RefQueue_DataUtil = this.breakingTarget ? this.init_RefQueue_DataUtil : this.load_RefQueue_DataUtil;
                                 if (AiMotionAudit.isEnabled()) {
                                    AiMotionAudit.note("gather dig " + inReach.toShortString());
                                 }

                                 AiBlockDigger.Inner_DataUtil_2 status = this.init_RefQueue_DataUtil_(inReach);
                                 if (status == AiBlockDigger.Inner_DataUtil_2.read) {
                                    if (this.read_RefQueue_DataUtil.lastFailureReasonCompat().equals("no tool that gets its drops")) {
                                       return this.missingTool(inReach);
                                    }

                                    if (this.read_RefQueue_DataUtil.lastFailureReasonCompat().equals("too little air to break it under water")) {
                                       this.breaking = null;
                                       return this.surfaceForAir(inReach);
                                    }

                                    this.noteCellFailure(inReach, "could not start breaking it (" + this.read_RefQueue_DataUtil.lastFailureReasonCompat() + ")");
                                    this.breaking = null;
                                    if (inReach.equals(this.pillarDirt)) {
                                       this.blacklistedCells.add(inReach.asLong());
                                       this.pillarDirt = null;
                                    }
                                 }

                                 return this.running(
                                    "Breaking " + (this.breakingTarget ? label : "the way to " + label) + " at " + inReach.toShortString()
                                 );
                              }
                           }

                           if (collectFirst && !dropsOnly) {
                              Vec3 body = mc.player.position();
                              drops = new ArrayList<>(drops);
                              drops.removeIf(itemx -> itemx.distanceToSqr(body) > 64.0);
                           }

                           List<BlockPos> goalBlocks = collectFirst && (!drops.isEmpty() || enough) ? List.of() : this.goalBlocks();
                           List<BlockPos> goalCells = new ArrayList<>(goalBlocks);
                           List<Vec3> goalItems = new ArrayList<>(drops);
                           if (!dropsOnly && !treeMode && !approach && held > 0 && !goalBlocks.isEmpty()) {
                              Vec3 body = mc.player.position();
                              double workDistance = 1.7976931348623157E308;

                              for (BlockPos block : goalBlocks) {
                                 workDistance = Math.min(workDistance, Math.sqrt(block.distToCenterSqr(body)));
                              }

                              double closerWork = workDistance;
                              int needed = Math.max(1, held - this.heldWanted());
                              goalItems.removeIf(itemx -> {
                                 int available = 0;

                                 for (Entry<Vec3, Integer> entry : this.dropCountBySpot.entrySet()) {
                                    if (entry.getKey().distanceToSqr(itemx) <= 9.0) {
                                       available += entry.getValue();
                                    }
                                 }

                                 return AiBlockMine.dropNotWorthDetour(itemx.distanceTo(body), closerWork, available, needed);
                              });
                           }

                           if (this.pillarDirt != null) {
                              if (this.hasScaffoldBlock()) {
                                 this.pillarDirt = null;
                              } else {
                                 Vec3 stock = this.pillarDirtDrop();
                                 if (stock != null) {
                                    goalItems.add(stock);
                                 } else if (this.pillarDirtFresh() && mc.level.getBlockState(this.pillarDirt).isAir()) {
                                    if (this.travel != null) {
                                       this.travel.load(true);
                                    }

                                    return this.running("Taking the dirt for a pillar");
                                 }
                              }
                           }

                           for (Long cell : this.pendingDropDeadlines.keySet()) {
                              goalItems.add(this.dropLandingSpot(BlockPos.of(cell)));
                           }

                           this.collectingInBox = false;
                           this.inBoxItems.clear();
                           if (!goalItems.isEmpty()) {
                              Vec3 body = mc.player.position();
                              List<Vec3> inBox = new ArrayList<>();
                              Set<Vec3> spawning = new HashSet<>();

                              for (Long cell : this.pendingDropDeadlines.keySet()) {
                                 spawning.add(this.dropLandingSpot(BlockPos.of(cell)));
                              }

                              for (Vec3 item : goalItems) {
                                 if (this.freshDropSpots.contains(item)
                                    ? AiBlockMine.itemAtBody(body, item)
                                    : spawning.contains(item)
                                       && AiBlockMine.isItemAtBlock(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), item)) {
                                    inBox.add(item);
                                 }
                              }

                              if (!inBox.isEmpty() && (!goalCells.isEmpty() || goalItems.size() > inBox.size())) {
                                 if (this.pickupWaitStart < 0) {
                                    this.pickupWaitStart = this.tickCount;
                                 }

                                 if (this.tickCount - this.pickupWaitStart <= 20) {
                                    goalItems.removeAll(inBox);
                                    this.inBoxItems.addAll(inBox);
                                    this.collectingInBox = true;
                                 }
                              }
                           }

                           if (!this.collectingInBox) {
                              this.pickupWaitStart = -1;
                           }

                           if (goalCells.isEmpty() && goalItems.isEmpty()) {
                              if (this.goalReached()) {
                                 return this.finish(AiOperation.Status.SUCCEEDED, this.doneMessage());
                              }

                              if (held <= 0 && this.countReached()) {
                                 return this.running("Waiting for the last mined drops");
                              }

                              if (this.veinCommitment.isActive()) {
                                 return this.running("Finishing the committed " + label + " vein scan");
                              }

                              if (!this.scanned) {
                                 return this.running("Scanning loaded terrain for " + label);
                              }

                              if (obsidianJob && !dropsOnly && !this.lavaSealedCells.isEmpty()) {
                                 return this.finish(
                                    AiOperation.Status.FAILED,
                                    "Known obsidian is sealed by lava; cool or seal the lava before mining it (" + this.progressText() + ")"
                                 );
                              }

                              if (dropsOnly) {
                                 if (this.tickCount < 30) {
                                    return this.running("Waiting for the drop to land");
                                 }

                                 JsonObject out = this.progressJson();
                                 this.stopActivity();
                                 return AiOperation.Result.failed(
                                    AiFailure.NO_REACHABLE_TARGET, "No matching observed drops remain nearby (" + this.progressText() + ")", out.toString()
                                 );
                              } else {
                                 if (!explore) {
                                    return this.finish(AiOperation.Status.FAILED, "No " + label + " in loaded terrain around here (" + this.progressText() + ")");
                                 }

                                 int loaded = Math.min(256, mc.options.getEffectiveRenderDistance() * 16);
                                 if (treeMode && !this.exploring && this.scanRadius < loaded) {
                                    this.scanRadius = loaded;
                                    this.scanned = false;
                                    this.scanning = false;
                                    AutismClientAddon.LOG
                                       .info("[AI] block mine {}: none within {}, scanning every loaded chunk ({} across)", label, radius, loaded);
                                    return this.running("Looking further out for " + label);
                                 }

                                 if (!this.exploring) {
                                    this.exploring = true;
                                    this.exploreSince = System.nanoTime();
                                    BlockPos wood = treeMode ? this.nearestWoodedBiome() : null;
                                    if (wood != null) {
                                       AutismClientAddon.LOG
                                          .info("[AI] block mine {}: no tree loaded; heading for the wooded biome at {}", label, wood.toShortString());
                                    }

                                    this.exploreGoal = new AiBlockMine.Inner_BufferHelper(mc.player.blockPosition(), exploreY, wood);
                                 }

                                 if (this.exploreGoal.toward != null
                                    && Math.hypot(
                                          mc.player.getX() - this.exploreGoal.toward.getX(),
                                          mc.player.getZ() - this.exploreGoal.toward.getZ()
                                       )
                                       < 16.0) {
                                    this.exploreGoal = new AiBlockMine.Inner_BufferHelper(mc.player.blockPosition(), exploreY);
                                 }

                                 return System.nanoTime() - this.exploreSince > 90000000000L
                                    ? this.finish(AiOperation.Status.FAILED, "No " + label + " found after exploring (" + this.progressText() + ")")
                                    : this.startTravel(this.exploreGoal, "Exploring for " + label);
                              }
                           } else {
                              this.exploring = false;
                              this.exploreGoal = null;
                              if (goalItems.isEmpty() && !goalCells.isEmpty()) {
                                 BlockPos feet = mc.player.blockPosition();
                                 BlockPos nearest = null;

                                 for (BlockPos t : goalCells) {
                                    if (nearest == null || this.travelCost(t, feet) < this.travelCost(nearest, feet)) {
                                       nearest = t;
                                    }
                                 }

                                 AiOperation.Result stuck = this.checkProgress(goalCells, nearest, feet);
                                 if (stuck != null) {
                                    return stuck;
                                 }

                                 if (this.descent != null
                                    && feet.getY() <= this.descent.maxY
                                    && this.descent.maxY <= nearest.getY()) {
                                    this.descentTarget = nearest;
                                 }

                                 if (feet.getY() - nearest.getY() <= 4) {
                                    this.descentTarget = nearest;
                                 }

                                 boolean sameCluster = this.descentTarget != null && this.descentTarget.distSqr(nearest) <= 36.0;
                                 if (feet.getY() - nearest.getY() > 4 && !sameCluster) {
                                    if (this.descent == null || !this.descent.stillDescendingToward(feet, nearest)) {
                                       String key = feet.getY() + ">" + nearest.toShortString();
                                       if (this.descentFailures.merge(key, 1, Integer::sum) >= 3) {
                                          int left = 0;

                                          for (BlockPos t : goalCells) {
                                             if (t.distSqr(nearest) <= 36.0) {
                                                this.blacklistedCells.add(t.asLong());
                                                left++;
                                             }
                                          }

                                          AutismClientAddon.LOG
                                             .info(
                                                "[AI] block mine {}: the way down to {} keeps failing; left {} targets there",
                                                label,
                                                nearest.toShortString(),
                                                left
                                             );
                                          this.descentFailures.clear();
                                          this.descent = null;
                                          if (this.travel != null) {
                                             this.travel.cancel();
                                             this.travel = null;
                                             this.currentGatherGoal = null;
                                             this.travelGoal = null;
                                          }

                                          if (++this.reachFailures >= 14) {
                                             return this.finish(
                                                AiOperation.Status.FAILED,
                                                "No reachable " + label + " after " + this.reachFailures + " attempts (" + this.progressText() + ")"
                                             );
                                          }

                                          return this.running(
                                             "No way down to the " + label + " near " + nearest.toShortString() + "; trying elsewhere"
                                          );
                                       }

                                       this.descent = AiBlockMine.Inner_StreamUtil.descentToward(feet, nearest);
                                       if (this.veinCommitment.isIdle()) {
                                          this.commitVein(nearest);
                                       }

                                       if (approach) {
                                          this.descent.blocked = this.knownTargetCells();
                                       }

                                       AutismClientAddon.LOG
                                          .info(
                                             "[AI] block mine {}: descending from y {} to y {} toward {} ({} goal cells, {} known, focus {}, nearest known {})",
                                             label,
                                             feet.getY(),
                                             this.descent.maxY,
                                             nearest.toShortString(),
                                             goalCells.size(),
                                             this.knownTargets.size(),
                                             this.veinCommitment.remainingCount(),
                                             this.knownTargets.isEmpty()
                                                ? "none"
                                                : this.describeTarget(this.knownTargets.stream().min(Comparator.comparingDouble(t -> t.distSqr(feet))).get())
                                          );
                                    }

                                    this.descentDepth = feet.getY() - nearest.getY();
                                    return this.startTravel(this.descent, "Digging down toward " + label + " at y " + nearest.getY());
                                 }
                              }

                              if (goalItems.isEmpty() && !this.collectingInBox) {
                                 this.descent = null;
                              }

                              Set<Long> noShaft = new HashSet<>();

                              for (BlockPos cell : goalCells) {
                                 if (this.hasFallingBlockAbove(cell)) {
                                    noShaft.add(cell.asLong());
                                 }
                              }

                              Set<Long> wet = new HashSet<>();

                              for (BlockPos cell : goalCells) {
                                 if (approach || this.isWet(cell)) {
                                    wet.add(cell.asLong());
                                 }
                              }

                              AiBlockMine.Inner_TaskQueue goal = new AiBlockMine.Inner_TaskQueue(goalCells, goalItems, noShaft, wet);
                              if (treeMode) {
                                 goal.reachAbove = 5;

                                 for (BlockPos t : goalCells) {
                                    this.collectTrunkStances(t, goal.update);
                                 }
                              }

                              for (BlockPos t : goalCells) {
                                 if (!wet.contains(t.asLong())) {
                                    for (long cell : this.standCells(t, treeMode && this.treeLogs.contains(t.asLong()), goal.reachAbove)) {
                                       goal.update.add(cell);
                                    }
                                 }
                              }

                              if (approach) {
                                 goal.write = this.knownTargetCells();
                              }

                              if (!this.lastGoalCells.equals(goal.dryTargetCells)) {
                                 this.lastGoalCells.clear();
                                 this.lastGoalCells.addAll(goal.dryTargetCells);
                                 this.blindStances.clear();
                              }

                              goal.excludedStances.addAll(this.blindStances);
                              if (!approach
                                 && !goalCells.isEmpty()
                                 && goalItems.isEmpty()
                                 && goal.wetTargets.isEmpty()
                                 && this.travel != null
                                 && this.travel.isAtGoal()
                                 && goal.sameGoal(this.currentGatherGoal)
                                 && mc.player.onGround()
                                 && this.travel.canInterrupt()) {
                                 boolean visible = false;
                                 Vec3 eye = mc.player.getEyePosition();

                                 for (BlockPos block : goalCells) {
                                    if (AiBlockMine.init(mc.player.blockPosition(), block, treeMode && this.treeLogs.contains(block.asLong()))
                                       && (AiBlockInteraction.findReachableHit(mc, block) != null || this.softBlockerToward(eye, block, mc.player.blockInteractionRange()) != null)
                                       )
                                     {
                                       visible = true;
                                       break;
                                    }
                                 }

                                 if (!visible) {
                                    BlockPos feet = mc.player.blockPosition();
                                    this.blindStances.add(feet.asLong());
                                    goal.excludedStances.add(feet.asLong());
                                    this.travel.cancel();
                                    this.travel = null;
                                    this.currentGatherGoal = null;
                                    this.travelGoal = null;
                                    this.idleAtGoalTicks = 0;
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] block mine {}: stance {} cannot see its ore; approach the same targets from another cell",
                                          label,
                                          feet.toShortString()
                                       );
                                 }
                              }

                              if (this.travel != null && this.travel.isAtGoal() && goal.sameGoal(this.currentGatherGoal)) {
                                 if (dropsOnly) {
                                    List<ItemStack> observed = new ArrayList<>();
                                    List<Vec3> positions = new ArrayList<>();

                                    for (Entity entity : mc.level.entitiesForRendering()) {
                                       if (entity instanceof ItemEntity item
                                          && item.isAlive()
                                          && this.isWantedItem(item.getItem().getItem())
                                          && item.distanceToSqr(mc.player) <= (double)this.scanRadius * this.scanRadius) {
                                          observed.add(item.getItem());
                                          positions.add(item.position());
                                       }
                                    }

                                    AiFailure failure = AiBlockMine.inventoryFullFailure(true, mc.player.getInventory(), observed);
                                    if (failure != null) {
                                       JsonObject out = this.progressJson();
                                       out.addProperty("observedDropStacks", observed.size());
                                       JsonArray dropGoals = new JsonArray();

                                       for (Vec3 item : drops) {
                                          dropGoals.add(AiJson.objectOf("x", item.x, "y", item.y, "z", item.z));
                                       }

                                       out.add("dropGoals", dropGoals);
                                       this.stopActivity();
                                       return AiOperation.Result.failed(
                                          failure, "Matching drops are observed but the inventory cannot accept them", out.toString()
                                       );
                                    }

                                    if (AiBlockMine.anyDropAtBody(true, mc.player.position(), positions)) {
                                       this.idleAtGoalTicks = 0;
                                       return this.running("Waiting for the observed " + label + " pickup; inventory has room");
                                    }
                                 }

                                 if (++this.idleAtGoalTicks > 20) {
                                    BlockPos feet = mc.player.blockPosition();
                                    List<Vec3> inBox = new ArrayList<>();

                                    for (Vec3 item : drops) {
                                       if (AiBlockMine.isItemAtBlock(feet.getX(), feet.getY(), feet.getZ(), item)) {
                                          inBox.add(item);
                                       }
                                    }

                                    if (inBox.isEmpty() && goalCells.isEmpty() && !drops.isEmpty()) {
                                       if (!goal.sameGoal(this.replannedDropGoal)) {
                                          this.replannedDropGoal = goal;
                                          this.idleAtGoalTicks = 0;
                                          if (this.travel != null) {
                                             this.travel.cancel();
                                             this.travel = null;
                                          }

                                          this.currentGatherGoal = null;
                                          this.travelGoal = null;
                                          return this.running("Planning to the " + label + " drop afresh");
                                       }

                                       inBox.addAll(drops);
                                    }

                                    BlockPos gone = inBox.isEmpty() ? this.blacklistNearestGoal() : this.blacklistNearestDrop(inBox);
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] block mine {}: nothing breakable from the goal, blacklisted {} (at {}, drops {}; {})",
                                          label,
                                          gone == null ? "-" : gone.toShortString(),
                                          mc.player.position(),
                                          drops.size() > 3 ? drops.subList(0, 3) : drops,
                                          this.reachReport()
                                       );
                                    this.idleAtGoalTicks = 0;
                                 }
                              } else {
                                 this.idleAtGoalTicks = 0;
                              }

                              if (goalCells.isEmpty()
                                 && this.travel != null
                                 && this.travel.isAtGoal()
                                 && goal.sameGoal(this.currentGatherGoal)
                                 && mc.player.onGround()) {
                                 Vec3 at = mc.player.position();
                                 Vec3 near = null;

                                 for (Vec3 item : goalItems) {
                                    if (near == null || item.distanceToSqr(at) < near.distanceToSqr(at)) {
                                       near = item;
                                    }
                                 }

                                 if (near != null
                                    && Math.abs(near.y - at.y) < 1.2
                                    && (Math.abs(near.x - at.x) > 1.15 || Math.abs(near.z - at.z) > 1.15)
                                    && near.distanceToSqr(at) < 9.0) {
                                    if (this.canWalkStraightTo(near)) {
                                       this.steppingToDrop = true;
                                       return this.running("Stepping onto the " + label + " drop");
                                    }

                                    this.releaseStepKeys();
                                    AiMotionSafety.releaseMovementKeys();
                                    if (!AiMotionSafety.isHorizontalMoveSafe(0.0, 0.0)) {
                                       AiMotionSafety.applyEmergencyBrake();
                                    }

                                    this.blacklistNearestDrop(List.of(near));
                                    return this.running("Leaving a drop whose pickup step would touch lava or leave the floor");
                                 }
                              }

                              this.releaseStepKeys();
                              return this.startTravel(
                                 goal,
                                 (goalCells.isEmpty() ? "Collecting " : "Mining ")
                                    + label
                                    + " ("
                                    + this.knownTargets.size()
                                    + " known, "
                                    + drops.size()
                                    + " drops)"
                              );
                           }
                        }
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result startTravel(AiPathfinder.PathGoal goal, String message) {
               if (this.veinCommitment.isIdle() && goal instanceof AiBlockMine.Inner_TaskQueue t && !t.targetBlocks.isEmpty()) {
                  this.commitVein(t.targetBlocks.getFirst());
               }

               boolean changed = goal instanceof AiBlockMine.Inner_TaskQueue t ? !t.sameGoal(this.currentGatherGoal) : goal != this.travelGoal;
               if (AiBlockMine.NAVIGATION_TRACE && changed && goal instanceof AiBlockMine.Inner_TaskQueue t) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] gather goal {} blocks (first {}), {} items (first {}), was {} blocks / {} items",
                        t.targetBlocks.size(),
                        t.targetBlocks.isEmpty() ? "-" : t.targetBlocks.subList(0, Math.min(3, t.targetBlocks.size())),
                        t.targetItems.size(),
                        t.targetItems.isEmpty() ? "-" : t.targetItems.get(0),
                        this.currentGatherGoal == null ? -1 : this.currentGatherGoal.targetBlocks.size(),
                        this.currentGatherGoal == null ? -1 : this.currentGatherGoal.targetItems.size()
                     );
               }

               if (this.travel == null) {
                  if (goal instanceof AiBlockMine.Inner_TaskQueue t && !t.targetBlocks.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] block mine {}: route from {} to {} targets, nearest {} (h {})",
                           label,
                           mc.player.blockPosition().toShortString(),
                           t.targetBlocks.size(),
                           t.targetBlocks.get(0).toShortString(),
                           String.format(Locale.ROOT, "%.1f", t.heuristic(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()))
                        );
                  }

                  this.travel = AiGoto.createPersistentOpCompat(
                     mc.player.blockPosition(), AiGoto.protectingCellsCompat(goal, protectedCells), (int)Math.max(20L, (deadline - System.nanoTime()) / 1000000000L)
                  );
                  this.travel.setAcceptedTool(this.acceptedTool);

                  for (Long cell : this.stuckCells) {
                     this.travel.blacklistCell(BlockPos.of(cell));
                  }
               } else {
                  this.travel.load(false);
                  boolean airborne = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable();
                  if (changed && airborne) {
                     AiOperation.Result r = this.travel.tick();
                     return r.terminal() ? this.running(message + ": landing") : this.running(message + ": " + r.message());
                  }

                  if (changed) {
                     this.travel.retarget(AiGoto.protectingCellsCompat(goal, protectedCells));
                  }
               }

               this.travel.init(this.collectingInBox, this.collectingInBox ? this::coversInBoxItems : null);
               this.travel.init_V_I_2(goal instanceof AiBlockMine.Inner_StreamUtil ? this.descentDepth : 0);
               List<BlockPos> work;
               List var20 = work = goal instanceof AiBlockMine.Inner_TaskQueue t
                  ? (t.targetBlocks.isEmpty() && !t.targetItems.isEmpty() && !dropsOnly ? this.goalBlocks() : t.targetBlocks)
                  : List.of();
               if (!work.isEmpty()) {
                  boolean trees = treeMode;
                  this.travel.setAimFunction(end -> this.aimFromPathEnd(end, work, trees));
               } else {
                  this.travel.setAimFunction(null);
               }

               this.travelGoal = goal;
               this.currentGatherGoal = goal instanceof AiBlockMine.Inner_TaskQueue t ? t : null;
               AiOperation.Result r = this.travel.tick();
               if (!r.terminal()) {
                  return this.running(message + ": " + r.message());
               }

               AiBlockMine.Inner_TaskQueue failedGoal = this.currentGatherGoal;
               BlockPos stuck = this.travel.getStuckCell();
               if (stuck != null) {
                  this.stuckCells.add(stuck.asLong());
               }

               this.travel.cancel();
               this.travel = null;
               this.currentGatherGoal = null;
               this.travelGoal = null;
               this.descent = null;
               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.finish(AiOperation.Status.CANCELLED, "Cancelled");
               }

               if (r.failure() == AiFailure.MISSING_TOOL || r.failure() == AiFailure.TOOL_WORN) {
                  this.stopActivity();
                  return r;
               }

               if (r.status() == AiOperation.Status.TIMED_OUT) {
                  return this.finish(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.progressText() + ")");
               }

               if (r.message() != null && r.message().startsWith("Health too low")) {
                  return this.finish(AiOperation.Status.FAILED, r.message());
               }

               BlockPos closest = !this.treeLogs.isEmpty()
                  ? BlockPos.of(this.treeLogs.iterator().next())
                  : (failedGoal != null && failedGoal.targetBlocks.isEmpty() ? this.blacklistNearestDrop(failedGoal.targetItems) : this.blacklistNearestGoal());
               if (!this.treeLogs.isEmpty()) {
                  this.blacklistedCells.addAll(this.treeLogs);
                  this.treeLogs.clear();
               }

               this.veinCommitment.clear();
               AutismClientAddon.LOG
                  .info(
                     "[AI] block mine {}: route failed ({}), blacklisted {}: {}",
                     label,
                     this.reachFailures + 1,
                     closest == null ? "-" : closest.toShortString(),
                     r.message()
                  );
               return ++this.reachFailures >= 14
                  ? this.finish(AiOperation.Status.FAILED, "No reachable " + label + " after " + this.reachFailures + " attempts (" + this.progressText() + ")")
                  : this.running("Could not reach it; trying the next");
            }

            void noteCellFailure(BlockPos cell, String why) {
               if (!this.hasFallingBlockAbove(cell)) {
                  int n = this.failuresByCell.merge(cell.asLong(), 1, Integer::sum);
                  AutismClientAddon.LOG.info("[AI] block mine {}: {} at {} ({}/3)", label, why, cell.toShortString(), n);
                  if (n >= 3) {
                     this.blacklistedCells.add(cell.asLong());
                     this.treeLogs.remove(cell.asLong());
                  }
               }
            }

            BlockPos blacklistNearestGoal() {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;

               for (BlockPos t : this.goalBlocks()) {
                  if (best == null || t.distSqr(feet) < best.distSqr(feet)) {
                     best = t;
                  }
               }

               for (Vec3 item : this.visibleDrops()) {
                  BlockPos cell = BlockPos.containing(item);
                  if (best == null || cell.distSqr(feet) < best.distSqr(feet)) {
                     best = cell;
                  }
               }

               if (best != null) {
                  this.blacklistedCells.add(best.asLong());
                  this.treeLogs.remove(best.asLong());
               }

               return best;
            }

            BlockPos blacklistNearestDrop(List<Vec3> items) {
               Vec3 at = mc.player.position();
               Vec3 best = null;

               for (Vec3 item : items) {
                  if (best == null || item.distanceToSqr(at) < best.distanceToSqr(at)) {
                     best = item;
                  }
               }

               if (best == null) {
                  return null;
               }

               BlockPos cell = BlockPos.containing(best);
               this.blacklistedCells.add(cell.asLong());
               return cell;
            }

            void releaseStepKeys() {
               if (this.steppingToDrop) {
                  this.steppingToDrop = false;
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               }
            }

            boolean canWalkStraightTo(Vec3 point) {
               double dx = point.x - mc.player.getX();
               double dz = point.z - mc.player.getZ();
               double yaw = Math.toRadians(mc.player.getYRot());
               double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
               double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
               if (!AiMotionSafety.isSegmentHazardFree(mc.player.position(), new Vec3(point.x, mc.player.getY(), point.z))
                  || !AiMotionSafety.isHorizontalMoveSafe(forward > 0.2 ? 1.0 : (forward < -0.2 ? -1.0 : 0.0), right > 0.2 ? -1.0 : (right < -0.2 ? 1.0 : 0.0))) {
                  return false;
               } else if (this.hasGroundAt(point.x, point.z) && this.hasGroundAt((point.x + mc.player.getX()) / 2.0, (point.z + mc.player.getZ()) / 2.0)) {
                  mc.options.keyUp.setDown(forward > 0.2);
                  mc.options.keyDown.setDown(forward < -0.2);
                  mc.options.keyRight.setDown(right > 0.2);
                  mc.options.keyLeft.setDown(right < -0.2);
                  mc.options.keySprint.setDown(false);
                  return true;
               } else {
                  return false;
               }
            }

            boolean hasGroundAt(double x, double z) {
               BlockPos cell = BlockPos.containing(x, mc.player.getY() - 0.5, z);

               for (int d = 0; d <= 2; d++) {
                  BlockPos below = cell.below(d);
                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                     return true;
                  }
               }

               return false;
            }

            void commitVein(BlockPos seed) {
               if (!treeMode && !approach && !dropsOnly) {
                  this.veinCommitment.init(seed, this.knownTargets, cell -> !this.blacklistedCells.contains(cell.asLong()));
                  this.goalSet.clear();
               } else {
                  this.veinCommitment.clear();
               }
            }

            void refreshVein() {
               this.veinCommitment.init(this.knownTargets, cell -> !this.blacklistedCells.contains(cell.asLong()));
            }

            Vec3 dropLandingSpot(BlockPos cell) {
               BlockPos at = cell;

               for (int i = 0; i < 8; i++) {
                  BlockPos below = at.below();
                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                     break;
                  }

                  at = below;
               }

               return Vec3.atBottomCenterOf(at);
            }

            void countBroken(BlockPos cell) {
               if (this.countedCells.add(cell.asLong()) && (quota == null || quota.load(this.blockIdByCell.get(cell.asLong())))) {
                  this.brokenCount++;
               }
            }

            void countIfBroken(BlockPos cell) {
               if (quota != null) {
                  if (mc.level.getChunkSource().getChunk(cell.getX() >> 4, cell.getZ() >> 4, ChunkStatus.FULL, false) == null) {
                     return;
                  }

                  if (this.matchesBlockId(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell).getBlock()).toString())) {
                     return;
                  }
               }

               this.countBroken(cell);
            }

            void onBroken(BlockPos cell) {
               this.countBroken(cell);
               if (this.veinCommitment.isIdle()) {
                  this.commitVein(cell);
               } else {
                  this.refreshVein();
               }

               if (cell.distSqr(mc.player.blockPosition()) <= 64.0) {
                  this.pendingDropDeadlines.put(cell.asLong(), System.nanoTime() + 250000000L);
               }

               if (treeMode) {
                  boolean fresh = this.treeLogs.isEmpty();
                  this.treeLogs.remove(cell.asLong());

                  for (int dx = -1; dx <= 1; dx++) {
                     for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                           BlockPos n = cell.offset(dx, dy, dz);
                           if (this.isTargetBlock(n) && !this.treeLogs.contains(n.asLong())) {
                              for (BlockPos log : AiBlockMine.floodConnected(this::isTargetBlock, n, 64)) {
                                 if (!this.blacklistedCells.contains(log.asLong()) && !this.open(log)) {
                                    this.treeLogs.add(log.asLong());
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (fresh) {
                     this.treesStarted++;
                  }
               }
            }

            AiBlockDigger.Inner_DataUtil_2 init_RefQueue_DataUtil_(BlockPos cell) {
               Set<UUID> before = null;
               if (this.breakingTarget && !this.read_RefQueue_DataUtil.isDiggingTargetCompat(cell) && this.isTargetBlock(cell)) {
                  before = new HashSet<>();
                  Vec3 centre = Vec3.atCenterOf(cell);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity item
                        && item.isAlive()
                        && this.isWantedItem(item.getItem().getItem())
                        && item.position().distanceToSqr(centre) <= 64.0) {
                        before.add(item.getUUID());
                     }
                  }
               }

               AiBlockDigger.Inner_DataUtil_2 result = this.read_RefQueue_DataUtil
                  .digCompat(cell, this.aimPoint(cell), this.nextLineDirection(cell));
               if (before != null && (this.read_RefQueue_DataUtil.isDiggingTargetCompat(cell) || result == AiBlockDigger.Inner_DataUtil_2.load)) {
                  this.dropAttribution.recordPresent(cell.asLong(), before);
               }

               return result;
            }

            List<BlockPos> goalBlocks() {
               BlockPos feet = mc.player.blockPosition();
               List<BlockPos> out = new ArrayList<>();
               if (this.focus != null && this.scanned) {
                  BlockPos near = null;

                  for (BlockPos t : this.knownTargets) {
                     if (!this.blacklistedCells.contains(t.asLong())
                        && t.distSqr(this.focus) <= 36.0
                        && (near == null || t.distSqr(this.focus) < near.distSqr(this.focus))) {
                        near = t;
                     }
                  }

                  if (near != null && this.veinCommitment.isIdle()) {
                     this.commitVein(near);
                  }

                  this.focus = null;
               }

               if (this.veinCommitment.isActive()) {
                  this.veinCommitment.init(this.knownTargetCells());
                  if (this.veinCommitment.init(System.nanoTime(), this.scanned, 250000000L)) {
                     this.veinCommitment.clear();
                  }
               }

               if (!this.treeLogs.isEmpty()) {
                  for (Long cell : this.treeLogs) {
                     out.add(BlockPos.of(cell));
                  }
               } else if (this.veinCommitment.isActive() && !dropsOnly) {
                  out.addAll(this.veinCommitment.remainingCells());
               } else if (!dropsOnly) {
                  Set<Long> known = new HashSet<>();

                  for (BlockPos t : this.knownTargets) {
                     known.add(t.asLong());
                  }

                  this.goalSet.removeIf(cell -> !known.contains(cell));

                  for (BlockPos t : this.knownTargets) {
                     if (this.goalSet.size() >= 64) {
                        break;
                     }

                     this.goalSet.add(t.asLong());
                  }

                  for (Long cell : this.goalSet) {
                     out.add(BlockPos.of(cell));
                  }
               }

               out.removeIf(gx -> gx.equals(feet) || gx.equals(feet.above()) || gx.equals(feet.above(2)));
               if (!dropsOnly) {
                  out = AiBlockMine.breathingOften()
                     ? new ArrayList<>(out.stream().filter(gx -> !AiBlockMine.init(mc, gx)).toList())
                     : AiBlockMine.pruneCostlyWetTargets(out, feet, gx -> AiBlockMine.init(mc, gx), this::diveCost);
               }

               if (oreJob && this.treeLogs.isEmpty() && this.veinCommitment.isIdle()) {
                  List<BlockPos> buried = new ArrayList<>();

                  for (BlockPos g : out) {
                     if (!this.opensToCave(g) && !this.isWet(g)) {
                        buried.add(g);
                     }
                  }

                  int still = held > 0 ? Math.max(1, held - this.heldWanted()) : Math.max(1, count - this.brokenCount);
                  if (buried.size() >= still && buried.size() < out.size()) {
                     out = buried;
                  }
               }

               out.sort(Comparator.comparingDouble(gx -> this.travelCost(gx, feet)));
               return out;
            }

            String describeTarget(BlockPos cell) {
               for (Direction side : Direction.values()) {
                  BlockPos n = cell.relative(side);
                  if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !this.nearOwnDig(n)) {
                     return cell.toShortString() + " open to a cave at " + n.toShortString();
                  }
               }

               return cell.toShortString()
                  + (this.isWet(cell) ? " wet" : " buried")
                  + (this.blacklistedCells.contains(cell.asLong()) ? ", blacklisted" : "")
                  + (this.goalSet.contains(cell.asLong()) ? ", in the goal set" : ", not in the goal set");
            }

            double travelCost(BlockPos target, BlockPos feet) {
               if (!oreJob) {
                  return target.distSqr(feet);
               }

               if (this.travelCostTick != this.tickCount || !feet.equals(this.travelCostFeet)) {
                  this.travelCostCache.clear();
                  this.travelCostFeet = feet;
                  this.travelCostTick = this.tickCount;
               }

               return this.travelCostCache.computeIfAbsent(target.asLong(), key -> AiBlockMine.init(mc.level, feet, target));
            }

            double diveCost(BlockPos target) {
               if (AiBlockMine.breathingOften()) {
                  return 1.0 / 0.0;
               }

               if (this.diveCostTick != this.tickCount) {
                  this.diveCostCache.clear();
                  this.diveCostTick = this.tickCount;
               }

               return this.diveCostCache.computeIfAbsent(target.asLong(), key -> AiBlockMine.init(mc, target, true));
            }

            boolean opensToCave(BlockPos cell) {
               for (Direction side : Direction.values()) {
                  BlockPos n = cell.relative(side);
                  if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !this.nearOwnDig(n)) {
                     return true;
                  }
               }

               return false;
            }

            boolean nearOwnDig(BlockPos air) {
               return AiEditLedger.update(air);
            }

            void pruneKnownTargets() {
               BlockPos feet = mc.player.blockPosition();
               this.knownTargets.removeIf(t -> {
                  if (this.blacklistedCells.contains(t.asLong())) {
                     return true;
                  }

                  if (this.isTargetBlock(t)) {
                     return false;
                  }

                  if (t.distSqr(feet) <= 49.0) {
                     this.countIfBroken(t);
                  }

                  return true;
               });
               this.treeLogs.removeIf(cell -> {
                  BlockPos p = BlockPos.of(cell);
                  if (this.isTargetBlock(p)) {
                     return this.blacklistedCells.contains(cell);
                  }

                  if (p.distSqr(feet) <= 49.0) {
                     this.countIfBroken(p);
                  }

                  return true;
               });
            }

            boolean goalReached() {
               if (held > 0) {
                  return this.heldWanted() >= held
                     || heldMin > 0
                        && this.heldWanted() >= heldMin
                        && this.scanned
                        && this.treeLogs.isEmpty()
                        && this.hasKnownTargetWithin(nearRadius)
                        && this.nearbyDropEntities() == 0
                        && (!treeMode || !this.tallTreeOverhead() || !this.hasScaffoldBlock() && this.pillarDirt == null && this.findPillarDirt() == null)
                        && (!treeMode || !this.reachableLogLeft());
               }

               if (!this.countReached()) {
                  return false;
               }

               if (this.settleSince == 0L) {
                  this.settleSince = System.nanoTime();
               }

               long settling = System.nanoTime() - this.settleSince;
               return settling >= 250000000L && this.nearbyDropEntities() == 0 || settling > 3000000000L;
            }

            boolean countReached() {
               return quota != null
                  ? quota.isMet()
                  : this.brokenCount >= count || treeLimit > 0 && this.treesStarted >= treeLimit && this.treeLogs.isEmpty() && this.brokenCount > 0;
            }

            boolean reachableLogLeft() {
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos t : this.knownTargets) {
                  if (!this.blacklistedCells.contains(t.asLong()) && this.isTargetBlock(t) && AiBlockMine.init(feet, t, true) && AiBlockInteraction.findReachableHit(mc, t) != null) {
                     return true;
                  }
               }

               return false;
            }

            boolean hasKnownTargetWithin(int radiusx) {
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos t : this.knownTargets) {
                  if (!this.blacklistedCells.contains(t.asLong()) && AiBlockMine.withinRadius(feet, t, radius)) {
                     return false;
                  }
               }

               return true;
            }

            String progressText() {
               return held > 0 ? this.heldWanted() + "/" + held + " held" : this.brokenCount + "/" + count + " broken";
            }

            String doneMessage() {
               return "Mined "
                  + this.brokenCount
                  + " "
                  + label
                  + (held > 0 ? ", holding " + this.heldWanted() : "")
                  + (this.treesStarted > 0 ? " from " + this.treesStarted + " tree(s)" : "");
            }

            String visualTitle() {
               String item = !dropArg.isEmpty() ? dropArg : (dropIds.size() == 1 ? dropIds.iterator().next() : label);
               item = item.replace("minecraft:", "").replace('_', ' ');
               return (dropsOnly ? "Collecting " : "Mining ") + item;
            }

            String activityLabel() {
               if (this.waterCell != null) {
                  return "Securing the next block";
               } else if (this.breaking != null) {
                  return this.breakingTarget ? "Breaking blocks" : "Clearing access";
               } else if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor active && active.autism$isDestroying()) {
                  BlockPos cell = active.autism$getDestroyBlockPos();
                  return cell != null && this.isTargetBlock(cell) ? "Breaking blocks" : "Clearing access";
               } else if (this.steppingToDrop
                  || this.collectingInBox
                  || this.travelGoal instanceof AiBlockMine.Inner_TaskQueue target && target.targetBlocks.isEmpty() && !target.targetItems.isEmpty()) {
                  return "Collecting drops";
               } else if (!this.exploring && !this.knownTargets.isEmpty()) {
                  return this.travel != null && !this.travel.isAtGoal()
                     ? "Moving to the next blocks"
                     : "Mining the nearby batch";
               } else {
                  return "Searching nearby terrain";
               }
            }

            int remainingNeeded() {
               return Math.max(1, held > 0 ? held - this.heldWanted() - this.nearbyDropCount - this.pendingDropDeadlines.size() : count - this.brokenCount);
            }

            ToDoubleFunction<BlockPos> breakCost() {
               Map<BlockState, Double> costs = new IdentityHashMap<>();
               return cell -> costs.computeIfAbsent(mc.level.getBlockState(cell), state -> {
                  int slot = MiningCost.bestToolSlot(mc, state, true, (block, tool) -> AiItems.canDropWantedItem(mc, block, tool, wantedDrops));
                  return slot < 0 ? 1.0 / 0.0 : MiningCost.breakTicks(mc, mc.player.getInventory().getItem(slot), state, state.getDestroySpeed(mc.level, cell));
               });
            }

            boolean isWantedItem(Item item) {
               return this.wantedItemCache.computeIfAbsent(item, it -> {
                  String id = BuiltInRegistries.ITEM.getKey(it).toString();
                  if (!dropIds.isEmpty()) {
                     return dropIds.contains(id);
                  }

                  if (dropArg.isEmpty() || !id.equals(dropArg) && !AiMine.init(dropArg, id)) {
                     if (this.matchesBlockId(id)) {
                        return true;
                     }

                     for (Block source : AiItems.sourceBlocksFor(it)) {
                        if (this.matchesBlockId(BuiltInRegistries.BLOCK.getKey(source).toString())) {
                           return true;
                        }
                     }

                     return false;
                  } else {
                     return true;
                  }
               });
            }

            int heldWanted() {
               int total = 0;
               Inventory inventory = mc.player.getInventory();

               for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (!stack.isEmpty() && this.isWantedItem(stack.getItem())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            boolean coversInBoxItems(BlockPos cell) {
               for (Vec3 item : this.inBoxItems) {
                  if (Math.abs(item.x - (cell.getX() + 0.5)) > 1.0
                     || Math.abs(item.z - (cell.getZ() + 0.5)) > 1.0
                     || item.y < cell.getY() - 0.5
                     || item.y > cell.getY() + 2.3) {
                     return false;
                  }
               }

               return true;
            }

            Vec3 predictedRest(ItemEntity item) {
               Vec3 at = item.position();
               Vec3 v = item.getDeltaMovement();
               double glide = item.onGround() ? 2.4271844660194173 : 4.0;
               double gx = Math.max(-1.5, Math.min(1.5, v.x * glide));
               double gz = Math.max(-1.5, Math.min(1.5, v.z * glide));
               if (gx * gx + gz * gz > 1.0E-4 && this.glidePathClear(at, gx, gz)) {
                  at = at.add(gx, 0.0, gz);
               }

               if (item.onGround()) {
                  return at;
               }

               MutableBlockPos cell = BlockPos.containing(at).mutable();

               for (int i = 0; i < 24; i++) {
                  cell.move(Direction.DOWN);
                  if (!mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return new Vec3(at.x, cell.getY() + 1.0, at.z);
                  }
               }

               return at;
            }

            boolean glidePathClear(Vec3 at, double gx, double gz) {
               BlockPos to = BlockPos.containing(at.x + gx, at.y + 0.05, at.z + gz);
               return mc.level.getBlockState(to).getCollisionShape(mc.level, to).isEmpty();
            }

            boolean hasLooseDropNearby(List<Vec3> drops) {
               if (dropsOnly) {
                  return false;
               }

               Vec3 at = mc.player.position();

               for (Vec3 item : drops) {
                  if (!(item.distanceToSqr(at) > 25.0) && !AiBlockMine.itemAtBody(at, item)) {
                     return true;
                  }
               }

               return false;
            }

            boolean goalInReach() {
               if (!dropsOnly && !approach && mc.player.onGround()) {
                  BlockPos feet = mc.player.blockPosition();

                  for (BlockPos block : this.goalBlocks()) {
                     if (AiBlockMine.init(feet, block, treeMode && this.treeLogs.contains(block.asLong())) && AiBlockInteraction.findReachableHit(mc, block) != null) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            }

            int nearbyDropEntities() {
               int n = 0;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && this.isWantedItem(item.getItem().getItem()) && entity.distanceToSqr(mc.player) <= 64.0) {
                     n++;
                  }
               }

               return n;
            }

            List<Vec3> visibleDrops() {
               return this.dropSpots(false);
            }

            List<Vec3> dropSpots(boolean all) {
               List<Vec3> out = new ArrayList<>();
               this.collectableNearbyCount = 0;
               this.nearbyDropCount = 0;
               this.freshDropSpots.clear();
               this.dropCountBySpot.clear();
               Vec3 at = mc.player.position();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && this.isWantedItem(item.getItem().getItem())
                     && !(entity.position().distanceToSqr(at) > (double)radius * radius)
                     && !this.blacklistedCells.contains(entity.blockPosition().asLong())) {
                     Long spawned = this.dropAttribution.attribute(this.pendingDropDeadlines.keySet(), item.getUUID(), item.position(), item.getAge());
                     if (spawned != null) {
                        this.pendingDropDeadlines.remove(spawned);
                     }

                     boolean nearTree = false;
                     if (!all) {
                        for (Long cell : this.treeLogs) {
                           if (BlockPos.of(cell).distToCenterSqr(entity.position()) <= 9.0) {
                              nearTree = true;
                              break;
                           }
                        }
                     }

                     boolean counts = entity.position().distanceToSqr(at) <= 64.0;
                     if (counts) {
                        this.nearbyDropCount = this.nearbyDropCount + item.getItem().getCount();
                     }

                     if (!nearTree) {
                        Vec3 rest = this.predictedRest(item);
                        out.add(rest);
                        this.dropCountBySpot.merge(rest, item.getItem().getCount(), Integer::sum);
                        if (counts) {
                           this.collectableNearbyCount = this.collectableNearbyCount + item.getItem().getCount();
                        }

                        if (item.getAge() < 14) {
                           this.freshDropSpots.add(rest);
                        }
                     }

                     if (out.size() >= 24) {
                        break;
                     }
                  }
               }

               return out;
            }

            AiDigLine.Inner_CachePool blockShapes() {
               return pos -> mc.level.getBlockState(pos).getShape(mc.level, pos);
            }

            void planDigLines(Vec3 from, List<BlockPos> heads, Set<BlockPos> cells, double reach) {
               while (!this.digLines.isEmpty() && Collections.disjoint(this.digLines.get(0).chain(), cells)) {
                  this.digLines.remove(0);
               }

               boolean valid = !this.digLines.isEmpty()
                  && !Collections.disjoint(this.digLinesCells, cells)
                  && AiDigLine.init(this.digLines.get(0), from, cells, this.blockShapes(), reach) != null;
               if (!valid) {
                  this.digLines.clear();
                  this.digLines
                     .addAll(
                        AiDigLine.planLines(
                           from,
                           mc.player.getYRot(),
                           mc.player.getXRot(),
                           heads,
                           cells,
                           this.blockShapes(),
                           reach,
                           this.remainingNeeded(),
                           this.breakCost()
                        )
                     );
                  this.digLinesCells = Set.copyOf(cells);
                  if (AiBlockMine.NAVIGATION_TRACE && !this.digLines.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] dig plan from {}: {}",
                           mc.player.blockPosition().toShortString(),
                           this.digLines
                              .stream()
                              .map(l -> String.format(Locale.ROOT, "%.0f/%.0f %s", l.yaw(), l.pitch(), l.chain().stream().map(Vec3i::toShortString).toList()))
                              .toList()
                        );
                  }
               }
            }

            Vec3 nextLineDirection(BlockPos cell) {
               if (this.digLines.size() >= 2 && this.digLine != null && this.digLines.get(0).equals(this.digLine)) {
                  List<BlockPos> chain = this.digLine.chain();
                  int at = chain.indexOf(cell);
                  if (at < 0) {
                     return null;
                  }

                  for (int i = at + 1; i < chain.size(); i++) {
                     if (!mc.level.getBlockState(chain.get(i)).isAir()) {
                        return null;
                     }
                  }

                  AiDigLine.Inner_DataUtil next = this.digLines.get(1);
                  BlockPos led = AiBlockMine.nextChainBlock(next, mc.player.getEyePosition(), cell, this.blockShapes(), mc.player.blockInteractionRange());
                  if (led != null
                     && !this.blacklistedCells.contains(led.asLong())
                     && this.isTargetBlock(led)
                     && this.isEligibleTarget(led)
                     && AiBlockMine.init(mc.player.blockPosition(), led, treeMode && this.treeLogs.contains(led.asLong()))) {
                     return MiningCost.bestToolSlot(mc, mc.level.getBlockState(led), true, (state, tool) -> AiItems.canDropWantedItem(mc, state, tool, wantedDrops)) < 0
                        ? null
                        : next.direction();
                  } else {
                     return null;
                  }
               } else {
                  return null;
               }
            }

            Vec3 digLineAim(BlockPos cell) {
               if (mc.player == null) {
                  return null;
               }

               Vec3 from = this.driftedEye();
               double reach = mc.player.blockInteractionRange();
               BlockPos led = this.digLine == null
                  ? null
                  : AiDigLine.init(this.digLine, from, List.of(cell), this.blockShapes(), reach);
               if (led == null && this.digLinesCells.contains(cell) && !this.read_RefQueue_DataUtil.isDiggingTargetCompat(cell)) {
                  Set<BlockPos> rest = new HashSet<>();

                  for (BlockPos c : this.digLinesCells) {
                     if (!mc.level.getBlockState(c).isAir()) {
                        rest.add(c);
                     }
                  }

                  AiDigLine.Inner_DataUtil again = AiDigLine.planLine(
                     from, mc.player.getYRot(), mc.player.getXRot(), List.of(cell), rest, this.blockShapes(), reach
                  );
                  if (again != null && again.chain().get(0).equals(cell)) {
                     this.digLine = again;
                     if (!this.digLines.isEmpty() && this.digLines.get(0).chain().contains(cell)) {
                        this.digLines.set(0, again);
                     } else {
                        this.digLines.add(0, again);
                     }

                     led = cell;
                  }
               }

               if (led == null) {
                  return null;
               }

               Vec3 eye = mc.player.getEyePosition();
               BlockHitResult hit = AiDigLine.init(eye, this.digLine.direction(), reach, Set.of(), this.blockShapes());
               if (hit != null && hit.getBlockPos().equals(cell)) {
                  return hit.getLocation().add(this.digLine.direction().scale(0.02));
               }

               Set<BlockPos> rest = new HashSet<>();

               for (BlockPos c : this.digLinesCells) {
                  if (!mc.level.getBlockState(c).isAir()) {
                     rest.add(c);
                  }
               }

               rest.add(cell);
               AiDigLine.Inner_DataUtil here = AiDigLine.planLine(
                  eye, mc.player.getYRot(), mc.player.getXRot(), List.of(cell), rest, this.blockShapes(), reach
               );
               if (here != null && here.chain().get(0).equals(cell) && !this.read_RefQueue_DataUtil.isDiggingTargetCompat(cell)) {
                  this.digLine = here;
                  if (!this.digLines.isEmpty() && this.digLines.get(0).chain().contains(cell)) {
                     this.digLines.set(0, here);
                  } else {
                     this.digLines.add(0, here);
                  }

                  return AiDigLine.init(here, eye, this.blockShapes(), reach);
               } else {
                  return null;
               }
            }

            Vec3 aimFromPathEnd(BlockPos end, List<BlockPos> blocks, boolean trees) {
               if (end.equals(this.aimCacheEnd) && blocks.equals(this.aimCacheBlocks) && mc.player.tickCount - this.aimCacheTick < 2) {
                  return this.aimCachePoint;
               }

               List<BlockPos> beside = new ArrayList<>();
               BlockPos nearest = null;

               for (BlockPos b : blocks) {
                  if (AiBlockMine.init(end, b, trees)) {
                     beside.add(b);
                     if (nearest == null || b.distSqr(end.above()) < nearest.distSqr(end.above())) {
                        nearest = b;
                     }
                  }
               }

               Vec3 point = nearest == null ? null : Vec3.atCenterOf(nearest);
               if (!beside.isEmpty()) {
                  Vec3 eye = new Vec3(end.getX() + 0.5, end.getY() + mc.player.getEyeHeight(), end.getZ() + 0.5);
                  double reach = mc.player.blockInteractionRange();
                  List<AiDigLine.Inner_DataUtil> lines = AiDigLine.planLines(
                     eye, mc.player.getYRot(), mc.player.getXRot(), beside, beside, this.blockShapes(), reach, 1, this.breakCost()
                  );
                  if (!lines.isEmpty()) {
                     point = AiDigLine.init(lines.get(0), eye, this.blockShapes(), reach);
                  }
               }

               this.aimCacheEnd = end.immutable();
               this.aimCacheBlocks = List.copyOf(blocks);
               this.aimCacheTick = mc.player.tickCount;
               this.aimCachePoint = point;
               return point;
            }

            boolean driftingOffEdge() {
               if (mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
                  Vec3 v = mc.player.getDeltaMovement();
                  if (v.horizontalDistanceSqr() < 1.0E-4) {
                     return false;
                  }

                  AABB box = mc.player.getBoundingBox();
                  AABB under = new AABB(box.minX, box.minY - 0.1, box.minZ, box.maxX, box.minY, box.maxZ)
                     .move(v.x * 2.2026431718061676, 0.0, v.z * 2.2026431718061676);
                  return mc.level.noCollision(mc.player, under);
               } else {
                  return false;
               }
            }

            Vec3 driftedEye() {
               Vec3 eye = mc.player.getEyePosition();
               Vec3 drift = mc.player.getDeltaMovement();
               return mc.player.onGround() && drift.horizontalDistanceSqr() > 1.0E-4
                  ? eye.add(drift.x * 2.2026431718061676, 0.0, drift.z * 2.2026431718061676)
                  : eye;
            }

            BlockPos pickInReach() {
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.2;
               List<BlockPos> candidates = new ArrayList<>(this.goalBlocks());
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos nearby : this.knownTargets) {
                  if (!candidates.contains(nearby)
                     && !this.blacklistedCells.contains(nearby.asLong())
                     && this.isTargetBlock(nearby)
                     && (!this.veinCommitment.isActive() || this.veinCommitment.init(nearby.asLong()))
                     && (!treeMode || this.treeLogs.isEmpty() || this.treeLogs.contains(nearby.asLong()))
                     && AiBlockMine.init(feet, nearby, treeMode && this.treeLogs.contains(nearby.asLong()))) {
                     candidates.add(nearby);
                  }
               }

               BlockPos over = mc.player.blockPosition().above(2);
               RotationMath.Rotation facing = RotationMath.ofPlayer(mc.player);
               BlockPos best = null;
               this.hiddenTarget = null;
               double bestScore = 1.7976931348623157E308;
               List<BlockPos> direct = new ArrayList<>();
               List<BlockPos> hidden = new ArrayList<>();
               boolean moving = this.travel != null
                  && !this.travel.isAtGoal()
                  && mc.player.getDeltaMovement().horizontalDistanceSqr() > 0.003;
               Vec3 drift = mc.player.getDeltaMovement();
               Vec3 rest = drift.horizontalDistanceSqr() > 1.0E-4 ? eye.add(drift.x * 2.2026431718061676, 0.0, drift.z * 2.2026431718061676) : null;
               Set<BlockPos> routeCells = this.travel != null && !this.travel.isAtGoal()
                  ? this.travel.getRouteDigCells()
                  : Set.of();
               BlockPos end = moving ? this.travel.getPathEnd() : null;

               for (BlockPos t : candidates) {
                  if (!routeCells.contains(t)
                     && (end == null || end.equals(feet) || !AiBlockMine.init(end, t, treeMode && this.treeLogs.contains(t.asLong())))
                     && AiBlockMine.init(feet, t, treeMode && this.treeLogs.contains(t.asLong()))) {
                     if (moving) {
                        float yaw = RotationMath.lookAt(Vec3.atCenterOf(t), eye).yaw();
                        if (Math.abs(Mth.wrapDegrees(yaw - facing.yaw())) > 70.0F) {
                           continue;
                        }
                     }

                     if (!t.equals(over) || !this.hasFallingBlockAbove(t)) {
                        BlockPos in = AiBlockInteraction.findReachableHit(mc, t) != null && (rest == null || AiBlockInteraction.findReachableHitFrom(mc, t, rest) != null)
                           ? t
                           : this.softBlockerToward(eye, t, reach);
                        if (in == null && !this.blacklistedCells.contains(t.asLong())) {
                           hidden.add(t);
                        }

                        if (in != null && (in.equals(t) || !this.blacklistedCells.contains(in.asLong()))) {
                           if (in.equals(t)) {
                              direct.add(t);
                           }

                           double score = AiBlockMine.turnCost(facing, eye, t);
                           if (score < bestScore) {
                              bestScore = score;
                              best = in;
                              this.hiddenTarget = in.equals(t) ? null : t;
                           }
                        }
                     }
                  }
               }

               if (!direct.isEmpty()) {
                  BlockPos base = treeMode && held > 0 ? AiBlockMine.init(direct, candidates, held - this.heldWanted()) : null;
                  if (base != null) {
                     this.digLine = null;
                     this.digLines.clear();
                     this.unusedDigCells = Set.of();
                     this.softBlocker = null;
                     this.softBlockerAim = null;
                     return base;
                  }

                  if (treeMode && held > 0 && this.nearbyDropCount > 0) {
                     BlockPos support = null;
                     double distance = 1.7976931348623157E308;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof ItemEntity item && item.isAlive() && item.onGround() && this.isWantedItem(item.getItem().getItem())) {
                           BlockPos below = BlockPos.containing(item.getX(), item.getY() - 0.02, item.getZ());
                           if (direct.contains(below) && this.isTargetBlock(below)) {
                              double d = below.distToCenterSqr(eye);
                              if (d < distance) {
                                 distance = d;
                                 support = below;
                              }
                           }
                        }
                     }

                     if (support != null) {
                        this.digLine = null;
                        this.digLines.clear();
                        this.unusedDigCells = Set.of();
                        this.softBlocker = null;
                        this.softBlockerAim = null;
                        return support;
                     }
                  }

                  double lineReach = mc.player.blockInteractionRange();
                  Set<BlockPos> cells = new LinkedHashSet<>(direct);
                  cells.addAll(hidden);
                  Vec3 from = this.driftedEye();
                  this.planDigLines(from, direct, cells, lineReach);
                  this.digLine = this.digLines.isEmpty() ? null : this.digLines.get(0);
                  this.unusedDigCells = cells;
                  BlockPos led = this.digLine == null
                     ? null
                     : AiDigLine.init(this.digLine, from, cells, this.blockShapes(), lineReach);
                  if (led != null && direct.contains(led)) {
                     return led;
                  }
               }

               this.softBlocker = null;
               this.softBlockerAim = null;
               if (best != null && this.hiddenTarget != null) {
                  Vec3 to = Vec3.atCenterOf(this.hiddenTarget);
                  BlockHitResult hit = mc.level.clip(new ClipContext(eye, to, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
                  if (hit.getType() == Type.BLOCK && hit.getBlockPos().equals(best)) {
                     this.softBlocker = best;
                     this.softBlockerAim = hit.getLocation().add(to.subtract(eye).normalize().scale(0.05));
                  }
               }

               return best;
            }

            String reachReport() {
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.2;
               BlockPos feet = mc.player.blockPosition();
               StringBuilder out = new StringBuilder(
                  "ground=" + mc.player.onGround() + " pause=" + (this.travel == null || this.travel.canInterrupt())
               );
               int n = 0;

               for (BlockPos t : this.goalBlocks()) {
                  if (AiBlockMine.init(feet, t, treeMode && this.treeLogs.contains(t.asLong()))) {
                     if (n++ >= 4) {
                        break;
                     }

                     out.append(" | ").append(t.toShortString()).append(' ').append(mc.level.getBlockState(t).getBlock().getName().getString());
                     if (AiBlockInteraction.findReachableHit(mc, t) == null) {
                        out.append(" unseen");
                     }

                     BlockPos in = this.softBlockerToward(eye, t, reach);
                     if (in != null) {
                        out.append(" behind ").append(in.toShortString());
                     }

                     if (this.blacklistedCells.contains(t.asLong())) {
                        out.append(" blacklisted");
                     }
                  }
               }

               if (n == 0) {
                  out.append(" | no goal block beside the feet (").append(this.goalBlocks().size()).append(" in the goal)");
               }

               return out.toString();
            }

            Vec3 aimPoint(BlockPos cell) {
               Vec3 along = this.digLineAim(cell);
               if (along != null) {
                  return along;
               } else {
                  return cell.equals(this.softBlocker) ? this.softBlockerAim : null;
               }
            }

            BlockPos softBlockerToward(Vec3 eye, BlockPos target, double reach) {
               BlockHitResult hit = mc.level
                  .clip(new ClipContext(eye, Vec3.atCenterOf(target), net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
               if (hit.getType() != Type.BLOCK) {
                  return null;
               } else {
                  BlockPos in = hit.getBlockPos();
                  if (!in.equals(target) && !(eye.distanceToSqr(hit.getLocation()) > reach * reach)) {
                     BlockState state = mc.level.getBlockState(in);
                     boolean soft = state.is(BlockTags.LEAVES) || state.getDestroySpeed(mc.level, in) == 0.0F && !state.hasBlockEntity();
                     return soft && state.getFluidState().isEmpty() && AiBlockInteraction.findReachableHit(mc, in) != null ? in : null;
                  } else {
                     return null;
                  }
               }
            }

            void scanAroundFeet() {
               if (!treeMode && !oreJob && !dropsOnly && !approach) {
                  BlockPos feet = mc.player.blockPosition();
                  Set<Long> known = this.knownTargetCells();

                  for (int dx = -2; dx <= 2; dx++) {
                     for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = 0; dy <= 2; dy++) {
                           BlockPos cell = feet.offset(dx, dy, dz);
                           long key = cell.asLong();
                           if (!cell.equals(feet)
                              && !cell.equals(feet.above())
                              && !this.blacklistedCells.contains(key)
                              && this.isTargetBlock(cell)
                              && (!known.contains(key) || !this.veinCommitment.load(key) && (!this.veinCommitment.isIdle() || !this.goalSet.contains(key)))
                              && this.isEligibleTarget(cell)) {
                              if (known.add(key)) {
                                 this.knownTargets.add(cell);
                              }

                              if (this.veinCommitment.isIdle()) {
                                 this.addToGoalBatch(this.goalSet, cell, feet);
                              }
                           }
                        }
                     }
                  }

                  if (this.veinCommitment.isActive()) {
                     this.refreshVein();
                  }
               }
            }

            void addToGoalBatch(Set<Long> batch, BlockPos cell, BlockPos feet) {
               if (!batch.contains(cell.asLong())) {
                  if (batch.size() >= 64) {
                     Long farthest = null;
                     double cost = this.travelCost(cell, feet);

                     for (Long key : batch) {
                        double remote = this.travelCost(BlockPos.of(key), feet);
                        if (remote > cost) {
                           farthest = key;
                           cost = remote;
                        }
                     }

                     if (farthest == null) {
                        return;
                     }

                     batch.remove(farthest);
                  }

                  batch.add(cell.asLong());
               }
            }

            void scanTerrain() {
               if (!this.scanning) {
                  if (this.scanned && this.tickCount % 5 != 0) {
                     return;
                  }

                  this.scanning = true;
                  this.scanHits = new ArrayList<>();
                  this.scanCursor = 0;
                  this.scanCentre = mc.player.blockPosition();
                  long key = this.scanCentre.getX() >> 4 & 1048575L
                     | (this.scanCentre.getZ() >> 4 & 1048575L) << 20
                     | (this.scanCentre.getY() >> 4 & 4095L) << 40
                     | (long)this.scanRadius << 52;
                  if (this.scanOrder == null || key != this.scanOrderKey) {
                     this.scanOrder = AiBlockMine.sectionScanOrder(
                        this.scanCentre.getX(),
                        this.scanCentre.getY(),
                        this.scanCentre.getZ(),
                        this.scanRadius + 15 >> 4,
                        mc.level.getMinSectionY(),
                        mc.level.getSectionsCount(),
                        oreJob
                     );
                     this.scanOrderKey = key;
                  }
               }

               long until = System.nanoTime() + 1500000L;
               MutableBlockPos p = new MutableBlockPos();

               while (this.scanCursor < this.scanOrder.length && this.scanHits.size() < 256 && System.nanoTime() <= until) {
                  int[] unit = this.scanOrder[this.scanCursor++];
                  int cx = (this.scanCentre.getX() >> 4) + unit[0];
                  int cz = (this.scanCentre.getZ() >> 4) + unit[1];
                  int si = unit[2];
                  if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
                     LevelChunkSection[] sections = chunk.getSections();
                     LevelChunkSection section = si < sections.length ? sections[si] : null;
                     if (section != null && !section.hasOnlyAir() && section.maybeHas(this::isTargetState)) {
                        int baseY = mc.level.getSectionYFromSectionIndex(si) << 4;
                        List<BlockPos> sectionHits = new ArrayList<>();

                        for (int i = 0; i < 4096; i++) {
                           int x = i & 15;
                           int z = i >> 4 & 15;
                           int y = i >> 8;
                           if (this.isTargetState(section.getBlockState(x, y, z))) {
                              p.set((cx << 4) + x, baseY + y, (cz << 4) + z);
                              if (!this.blacklistedCells.contains(p.asLong()) && this.withinScanRadius(p, this.scanCentre)) {
                                 sectionHits.add(p.immutable());
                              }
                           }
                        }

                        sectionHits.sort(Comparator.comparingDouble(cellx -> cellx.distSqr(this.scanCentre)));

                        for (BlockPos cell : sectionHits) {
                           if (this.scanHits.size() >= 256) {
                              break;
                           }

                           if (this.isEligibleTarget(cell)) {
                              if (quota != null) {
                                 this.blockIdByCell.put(cell.asLong(), BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell).getBlock()).toString());
                              }

                              this.scanHits.add(cell);
                           }
                        }
                     }
                  }
               }

               BlockPos feet = mc.player.blockPosition();
               Set<Long> have = new HashSet<>();

               for (BlockPos hit : this.scanHits) {
                  have.add(hit.asLong());
               }

               List<BlockPos> merged = new ArrayList<>(this.scanHits);

               for (BlockPos old : this.knownTargets) {
                  if (have.add(old.asLong()) && this.withinScanRadius(old, feet)) {
                     merged.add(old);
                  }
               }

               merged.sort(Comparator.comparingDouble(b -> this.travelCost(b, feet)));
               this.knownTargets = merged.size() > 1024 ? new ArrayList<>(merged.subList(0, 1024)) : merged;
               this.refreshVein();
               if (this.scanCursor >= this.scanOrder.length || this.scanHits.size() >= 256) {
                  this.scanning = false;
                  this.scanned = true;
               }
            }

            boolean withinScanRadius(BlockPos p, BlockPos centre) {
               double dx = p.getX() - centre.getX();
               double dz = p.getZ() - centre.getZ();
               return dx * dx + dz * dz <= (double)this.scanRadius * this.scanRadius;
            }

            BiPredicate<BlockState, ItemStack> buildAcceptedTool() {
               Set<Block> sources = AiBlockMine.sourceBlocks(wantedDrops, block -> this.matchesBlockId(BuiltInRegistries.BLOCK.getKey(block).toString()));
               BiPredicate<BlockState, ItemStack> wanted = AiItems.dropFilterFor(mc, wantedDrops);
               return (state, tool) -> !sources.contains(state.getBlock()) || wanted.test(state, tool);
            }

            boolean isTargetState(BlockState state) {
               return this.blockMatchCache.computeIfAbsent(state.getBlock(), this.targetBlockFilter::test)
                  && (quota == null || quota.covers(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()));
            }

            boolean matchesBlockId(String id) {
               return blockIds.contains(id) ? true : !blockArg.isEmpty() && AiMine.init(blockArg, id);
            }

            boolean isTargetBlock(BlockPos pos) {
               if (mc.level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) == null) {
                  return false;
               }

               BlockState state = mc.level.getBlockState(pos);
               boolean wanted = this.isTargetState(state);
               if (wanted && quota != null) {
                  this.blockIdByCell.put(pos.asLong(), BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
               }

               return wanted;
            }

            boolean hasFallingBlockAbove(BlockPos cell) {
               return mc.level.getBlockState(cell.above()).getBlock() instanceof FallingBlock;
            }

            boolean isEligibleTarget(BlockPos pos) {
               if (AiEditLedger.wasRecentlyPlaced(pos)) {
                  return false;
               }

               if (AiPathfinder.LiveGridView.isLitPortalFrameCompat(mc.level, pos)) {
                  return false;
               }

               if (treeMode && this.open(pos) && !this.hasScaffoldBlock()) {
                  return false;
               }

               if (treeMode && this.close(pos)) {
                  return false;
               }

               BlockState state = mc.level.getBlockState(pos);
               if (!(state.getDestroySpeed(mc.level, pos) < 0.0F) && state.getFluidState().isEmpty()) {
                  if (mc.level.getBlockState(pos.above()).is(Blocks.BEDROCK) && mc.level.getBlockState(pos.below()).is(Blocks.BEDROCK)) {
                     return false;
                  }

                  if (AiBlockMine.strikeCount(pos) >= 2 && AiBlockMine.init(mc, pos)) {
                     return false;
                  }

                  for (Direction side : Direction.values()) {
                     if (mc.level.getFluidState(pos.relative(side)).is(FluidTags.LAVA)) {
                        if (obsidianJob) {
                           this.lavaSealedCells.add(pos.asLong());
                        }

                        return false;
                     }
                  }

                  return this.adjacentWater(pos) == null || this.bestScaffoldItem() != null;
               } else {
                  return false;
               }
            }

            BlockPos nearestWoodedBiome() {
               BlockPos feet = mc.player.blockPosition();
               int r = Math.min(16, mc.options.getEffectiveRenderDistance());
               BlockPos best = null;
               double bestSq = 1.7976931348623157E308;

               for (int dx = -r; dx <= r; dx++) {
                  for (int dz = -r; dz <= r; dz++) {
                     int cx = (feet.getX() >> 4) + dx;
                     int cz = (feet.getZ() >> 4) + dz;
                     if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) != null) {
                        int x = (cx << 4) + 8;
                        int z = (cz << 4) + 8;
                        double sq = (double)(x - feet.getX()) * (x - feet.getX()) + (double)(z - feet.getZ()) * (z - feet.getZ());
                        if (!(sq >= bestSq)) {
                           int y = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                           Holder<Biome> biome = mc.level.getBiome(new BlockPos(x, y, z));
                           if (biome.is(BiomeTags.IS_FOREST)
                              || biome.is(BiomeTags.IS_TAIGA)
                              || biome.is(BiomeTags.IS_JUNGLE)
                              || biome.is(BiomeTags.IS_SAVANNA)
                              || biome.is(Biomes.SWAMP)
                              || biome.is(Biomes.MANGROVE_SWAMP)) {
                              bestSq = sq;
                              best = new BlockPos(x, y, z);
                           }
                        }
                     }
                  }
               }

               return best;
            }

            long[] standCells(BlockPos t, boolean trunk, int reachAbove) {
               if (this.tickCount - this.standCellCacheTick > 20) {
                  this.standCellCache.clear();
                  this.standCellCacheTick = this.tickCount;
               }

               long key = t.asLong() * 31L + (trunk ? 1 : 0);
               long[] cached = this.standCellCache.get(key);
               if (cached != null) {
                  return cached;
               }

               List<Long> out = new ArrayList<>();
               int top = trunk ? Math.max(2, reachAbove) : 2;
               Vec3 centre = Vec3.atCenterOf(t);

               for (int ox = -1; ox <= 1; ox++) {
                  for (int oz = -1; oz <= 1; oz++) {
                     if (ox != 0 || oz != 0) {
                        boolean diagonal = ox != 0 && oz != 0;

                        for (int dy = 0; dy <= top && (!diagonal || dy <= 1 || trunk); dy++) {
                           int x = t.getX() - ox;
                           int y = t.getY() - dy;
                           int z = t.getZ() - oz;
                           if (!(centre.distanceToSqr(x + 0.5, y + 1.62, z + 0.5) > 20.25)) {
                              boolean visible;
                              if (!diagonal && dy <= 1) {
                                 visible = true;
                              } else if (!diagonal) {
                                 visible = this.columnPassable(x, z, y + 2, t.getY()) || dy == 2 && this.isSolidSupport(t.below());
                              } else {
                                 visible = (this.isSolidSupport(new BlockPos(x, t.getY(), t.getZ())) || this.isSolidSupport(new BlockPos(t.getX(), t.getY(), z)))
                                    && (dy <= 1 || this.columnPassable(x, z, y + 2, t.getY()));
                              }

                              if (visible) {
                                 out.add(BlockPos.asLong(x, y, z));
                              }
                           }
                        }
                     }
                  }
               }

               long[] cells = out.stream().mapToLong(Long::longValue).toArray();
               this.standCellCache.put(key, cells);
               return cells;
            }

            boolean columnPassable(int x, int z, int fromY, int toY) {
               for (int y = fromY; y <= toY; y++) {
                  if (!this.isSolidSupport(new BlockPos(x, y, z))) {
                     return false;
                  }
               }

               return true;
            }

            boolean isSolidSupport(BlockPos p) {
               BlockState state = mc.level.getBlockState(p);
               return !state.isAir() && !state.is(BlockTags.LEAVES) ? state.getFluidState().isEmpty() && state.getCollisionShape(mc.level, p).isEmpty() : true;
            }

            void collectTrunkStances(BlockPos log, Set<Long> out) {
               for (int k = 3; k <= 5; k++) {
                  BlockPos feet = log.below(k);
                  boolean open = true;

                  for (int y = feet.getY() + 2; y < log.getY() && open; y++) {
                     BlockState state = mc.level.getBlockState(new BlockPos(log.getX(), y, log.getZ()));
                     open = state.isAir() || state.is(BlockTags.LEAVES);
                  }

                  if (open) {
                     out.add(feet.asLong());
                  }
               }
            }

            boolean close(BlockPos log) {
               int top = mc.level.getHeight(Types.MOTION_BLOCKING_NO_LEAVES, log.getX(), log.getZ());
               int y = log.getY() + 1;

               for (int n = 0; y < top && n < 64; n++) {
                  BlockPos cell = new BlockPos(log.getX(), y, log.getZ());
                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.isAir() && !state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.getCollisionShape(mc.level, cell).isEmpty()) {
                     return true;
                  }

                  y++;
               }

               return false;
            }

            boolean open(BlockPos log) {
               boolean high = true;

               for (int d = 1; d <= 12; d++) {
                  BlockPos below = log.below(d);
                  BlockState state = mc.level.getBlockState(below);
                  if (!state.is(BlockTags.LOGS) && !state.is(BlockTags.LEAVES) && !state.isAir()) {
                     high = d > 6;
                     break;
                  }
               }

               if (!high) {
                  return false;
               }

               if (mc.player != null && mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(log)) <= 25.0) {
                  return false;
               }

               for (int dx = -2; dx <= 2; dx++) {
                  for (int dz = -2; dz <= 2; dz++) {
                     if (dx != 0 || dz != 0) {
                        for (int dy = 2; dy >= -5; dy--) {
                           BlockPos ground = log.offset(dx, dy - 1, dz);
                           BlockState state = mc.level.getBlockState(ground);
                           if (!state.isAir()
                              && !state.is(BlockTags.LEAVES)
                              && !state.is(BlockTags.LOGS)
                              && !state.getCollisionShape(mc.level, ground).isEmpty()) {
                              BlockPos feet = ground.above();
                              if (mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()
                                 && mc.level.getBlockState(feet.above()).getCollisionShape(mc.level, feet.above()).isEmpty()) {
                                 return false;
                              }
                              break;
                           }
                        }
                     }
                  }
               }

               return true;
            }

            boolean tallTreeOverhead() {
               BlockPos feet = mc.player.blockPosition();

               for (int dx = -2; dx <= 2; dx++) {
                  for (int dz = -2; dz <= 2; dz++) {
                     for (int dy = 5; dy <= 8; dy++) {
                        BlockPos p = feet.offset(dx, dy, dz);
                        if (this.isTargetBlock(p) && !this.blacklistedCells.contains(p.asLong()) && this.open(p) && !this.close(p)) {
                           int ground = 0;

                           while (ground < 12 && !mc.level.getBlockState(p.below(ground + 1)).isSolidRender()) {
                              ground++;
                           }

                           if (ground <= 7) {
                              return true;
                           }
                        }
                     }
                  }
               }

               return false;
            }

            BlockPos findPillarDirt() {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;

               for (int dy = 0; dy >= -1 && best == null; dy--) {
                  for (int dx = -1; dx <= 1; dx++) {
                     for (int dz = -1; dz <= 1; dz++) {
                        if (dx != 0 || dz != 0) {
                           BlockPos p = feet.offset(dx, dy, dz);
                           BlockState state = mc.level.getBlockState(p);
                           if (state.is(Blocks.DIRT) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM)) {
                              BlockState over = mc.level.getBlockState(p.above());
                              if (!over.is(BlockTags.LOGS)
                                 && over.getCollisionShape(mc.level, p.above()).isEmpty()
                                 && !AiEditLedger.wasRecentlyPlaced(p)
                                 && AiBlockInteraction.findReachableHit(mc, p) != null
                                 && !this.blacklistedCells.contains(p.asLong())
                                 && AiPathfinder.LiveGridView.isSafeToDigCompat(mc.level, p)
                                 && (best == null || dx * dz == 0)) {
                                 best = p;
                              }
                           }
                        }
                     }
                  }
               }

               return best;
            }

            boolean pillarDirtFresh() {
               return this.pillarDirt != null && System.nanoTime() - this.pillarDirtSince < 5000000000L;
            }

            Vec3 pillarDirtDrop() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && item.getItem().is(Items.DIRT)
                     && item.position().distanceToSqr(Vec3.atCenterOf(this.pillarDirt)) <= 9.0) {
                     return this.predictedRest(item);
                  }
               }

               return null;
            }

            boolean hasScaffoldBlock() {
               Inventory inventory = mc.player.getInventory();

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (stack.getItem() instanceof BlockItem block
                     && !(block.getBlock() instanceof FallingBlock)
                     && AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()) >= 0
                     && AiRestock.canSpendStack(stack, inventory)) {
                     return true;
                  }
               }

               return false;
            }

            boolean isWet(BlockPos pos) {
               long now = System.nanoTime();
               long[] seen = this.wetCache.get(pos.asLong());
               if (seen != null && now - seen[1] < 5000000000L) {
                  return seen[0] != 0L;
               }

               boolean wet = this.adjacentWater(pos) != null;
               this.wetCache.put(pos.asLong(), new long[]{wet ? 1L : 0L, now});
               return wet;
            }

            BlockPos adjacentWater(BlockPos pos) {
               for (Direction side : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                  BlockPos n = pos.relative(side);
                  if (mc.level.getFluidState(n).is(FluidTags.WATER)) {
                     return n;
                  }
               }

               return null;
            }

            Item bestScaffoldItem() {
               Inventory inventory = mc.player.getInventory();
               Item best = null;
               int bestRank = 2147483647;

               for (int slot = 0; slot < 36; slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (stack.getItem() instanceof BlockItem block && !(block.getBlock() instanceof FallingBlock)) {
                     int rank = AiRestock.scaffoldRank(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
                     if (rank >= 0 && rank < bestRank && AiRestock.canSpendStack(stack, inventory)) {
                        bestRank = rank;
                        best = stack.getItem();
                     }
                  }
               }

               return best;
            }

            JsonObject progressJson() {
               JsonObject out = AiJson.objectOf(
                  "block",
                  label,
                  "broken",
                  this.brokenCount,
                  "held",
                  this.heldWanted(),
                  "gained",
                  this.heldWanted() - this.heldAtStart,
                  "known",
                  this.knownTargets.size(),
                  "failures",
                  this.reachFailures,
                  "trees",
                  this.treesStarted,
                  "exploring",
                  this.exploring
               );
               if (quota != null) {
                  out.add("brokenByBlock", quota.brokenByBlock());
                  out.add("remainingByBlock", quota.load());
               }

               if (!this.treeLogs.isEmpty()) {
                  out.addProperty("treeLogsLeft", this.treeLogs.size());
               }

               return out;
            }

            AiOperation.Result missingTool(BlockPos target) {
               Item needed = AiItems.cheapestRequiredTool(mc.level.getBlockState(target));
               JsonObject out = this.progressJson();
               out.addProperty("targetX", target.getX());
               out.addProperty("targetY", target.getY());
               out.addProperty("targetZ", target.getZ());
               if (needed != null) {
                  out.addProperty("neededTool", BuiltInRegistries.ITEM.getKey(needed).toString());
               }

               this.stopActivity();
               return AiOperation.Result.failed(
                  AiFailure.MISSING_TOOL, "No usable harvesting tool for " + mc.level.getBlockState(target).getBlock().getName().getString(), out.toString()
               );
            }

            AiOperation.Result running(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.progressJson().toString(), List.of());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               this.stopActivity();
               return new AiOperation.Result(status, message, this.progressJson().toString(), List.of());
            }

            void stopActivity() {
               if (this.preparation != null) {
                  this.reconcileAfterPreparation = this.reconcileAfterPreparation | this.preparation.requiresReconciliationAfterEmergency();
                  this.preparation.cancel();
                  this.preparation = null;
               }

               if (!AutismLiteVariant.enabled()) {
                  AiVisualization.clearOwner(this);
               }

               if (this.steppingToDrop) {
                  this.steppingToDrop = false;
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               }

               if (this.travel != null) {
                  this.travel.cancel();
                  this.travel = null;
               }

               if (this.breathe != null) {
                  this.breathe.cancel();
                  this.breathe = null;
               }

               this.init_RefQueue_DataUtil.stopCompat();
               this.load_RefQueue_DataUtil.stopCompat();
               this.waterPlacer.reset();
               this.waterCell = null;
               this.aim.clear();
               AiMotionSafety.releaseMovementKeys();
            }

            @Override
            public void cancel() {
               if (!this.cancelled) {
                  this.cancelled = true;
                  this.stopActivity();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Mining requires a connected living player");
      }
   }

   public static synchronized void init_V() {
      BREATHE_START_NANOS[0] = BREATHE_START_NANOS[1];
      BREATHE_START_NANOS[1] = System.nanoTime();
   }

   static synchronized boolean breathingOften() {
      long now = System.nanoTime();
      return BREATHE_START_NANOS[0] != 0L && BREATHE_START_NANOS[1] - BREATHE_START_NANOS[0] < 180000000000L && now - BREATHE_START_NANOS[1] < 300000000000L;
   }

   static synchronized int strikeCount(BlockPos pos) {
      long[] strike = STRIKES_BY_CELL.get(pos.asLong());
      if (strike == null) {
         return 0;
      } else if (System.nanoTime() - strike[1] > 600000000000L) {
         STRIKES_BY_CELL.remove(pos.asLong());
         return 0;
      } else {
         return (int)strike[0];
      }
   }

   static synchronized void recordStrike(BlockPos pos) {
      long[] strike = STRIKES_BY_CELL.computeIfAbsent(pos.asLong(), key -> new long[2]);
      strike[0]++;
      strike[1] = System.nanoTime();
      if (STRIKES_BY_CELL.size() > 256) {
         STRIKES_BY_CELL.entrySet().removeIf(e -> System.nanoTime() - e.getValue()[1] > 600000000000L);
      }
   }

   static double init(Minecraft mc, BlockPos pos, boolean requireDrops) {
      int depth = 0;
      int buried = 0;

      for (BlockPos cell = pos.above(); depth + buried < 24; cell = cell.above()) {
         if (isWater(mc, cell)) {
            depth++;
         } else {
            if (mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
               break;
            }

            buried++;
         }
      }

      double swim = 2.0 * (depth + buried) * 0.5 + buried * 1.5;
      BlockState state = mc.level.getBlockState(pos);
      int slot = MiningCost.load(mc, state, requireDrops);
      if (slot < 0) {
         return 1.0 / 0.0;
      }

      double dryTicks = MiningCost.breakTicks(mc, mc.player.getInventory().getItem(slot), state, state.getDestroySpeed(mc.level, pos), MiningCost.Inner_DataUtil.of(mc));
      double submergedSpeed = mc.player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
      double slowdown = submergedSpeed > 0.0 && submergedSpeed < 1.0 ? 1.0 / submergedSpeed : 1.0;
      return swim + dryTicks / 20.0 * (slowdown - 1.0);
   }

   static List<BlockPos> pruneCostlyWetTargets(List<BlockPos> targets, BlockPos feet, Predicate<BlockPos> submerged, ToDoubleFunction<BlockPos> dive) {
      double bestDry = 1.0 / 0.0;
      boolean anyWet = false;

      for (BlockPos t : targets) {
         if (submerged.test(t)) {
            anyWet = true;
         } else {
            bestDry = Math.min(bestDry, Math.sqrt(t.distSqr(feet)) / 4.3);
         }
      }

      if (anyWet && bestDry != 1.0 / 0.0) {
         List<BlockPos> kept = new ArrayList<>(targets.size());

         for (BlockPos t : targets) {
            if (!submerged.test(t) || Math.sqrt(t.distSqr(feet)) / 4.3 + dive.applyAsDouble(t) < bestDry) {
               kept.add(t);
            }
         }

         return kept;
      } else {
         return targets;
      }
   }

   static boolean init(Minecraft mc, BlockPos pos) {
      if (mc.player != null && mc.level != null) {
         if (!mc.player.hasEffect(MobEffects.WATER_BREATHING) && !mc.player.hasEffect(MobEffects.CONDUIT_POWER)) {
            if (!isWater(mc, pos.above())) {
               for (int dy = 2; dy <= 16; dy++) {
                  BlockPos cell = pos.above(dy);
                  if (isWater(mc, cell)) {
                     return true;
                  }

                  if (mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return false;
                  }
               }

               return false;
            } else {
               if (isWater(mc, pos.above(2))) {
                  return true;
               }

               int sides = 0;

               for (Direction side : Plane.HORIZONTAL) {
                  if (isWater(mc, pos.relative(side))) {
                     sides++;
                  }
               }

               return sides >= 2;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean isWater(Minecraft mc, BlockPos pos) {
      return mc.level.getFluidState(pos).is(FluidTags.WATER);
   }

   public static JsonArray init(Iterable<String> ids) {
      JsonArray out = new JsonArray();

      for (String id : ids) {
         out.add(id);
      }

      return out;
   }

   static final class Inner_BufferHelper implements AiPathfinder.PathGoal {
      final BlockPos origin;
      final int level;
      final BlockPos toward;

      Inner_BufferHelper(BlockPos origin, int level) {
         this(origin, level, null);
      }

      Inner_BufferHelper(BlockPos origin, int level, BlockPos toward) {
         this.origin = origin;
         this.level = level;
         this.toward = toward;
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return false;
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double away = this.toward != null
            ? 1.92 * Math.hypot(x - this.toward.getX(), z - this.toward.getZ())
            : -1.92 * Math.hypot(x - this.origin.getX(), z - this.origin.getZ());
         return this.level == -2147483648 ? away : away + Math.abs(y - this.level) * 6.0;
      }
   }

   static final class Inner_CachePool {
      final Map<Long, Set<UUID>> entitiesByCell = new HashMap<>();
      final Set<UUID> attributedEntities = new HashSet<>();

      void recordPresent(long cell, Collection<UUID> present) {
         this.entitiesByCell.put(cell, Set.copyOf(present));
      }

      Long attribute(Collection<Long> pending, UUID entity, Vec3 position, int age) {
         if (this.attributedEntities.contains(entity)) {
            return null;
         }

         List<Long> eligible = new ArrayList<>();

         for (Long cell : pending) {
            Set<UUID> before = this.entitiesByCell.get(cell);
            if (before != null && !before.contains(entity)) {
               eligible.add(cell);
            }
         }

         Long cell = AiBlockMine.spawnCellFor(eligible, position, age);
         if (cell != null) {
            this.attributedEntities.add(entity);
            this.entitiesByCell.remove(cell);
         }

         return cell;
      }
   }

   static final class Inner_ConfigStore {
      final Map<Long, Integer> bestRemainingByEnd = new HashMap<>();

      boolean recordImproved(BlockPos end, int remaining) {
         if (end != null && remaining >= 0) {
            Integer best = this.bestRemainingByEnd.get(end.asLong());
            if (best == null || remaining < best) {
               this.bestRemainingByEnd.put(end.asLong(), remaining);
            }

            return best != null && remaining < best;
         } else {
            return false;
         }
      }

      void clear() {
         this.bestRemainingByEnd.clear();
      }
   }

   static final class Inner_DataUtil implements AiPathfinder.PathGoal {
      final Set<Long> targetCells = new HashSet<>();
      final List<BlockPos> targets;

      Inner_DataUtil(List<BlockPos> targets) {
         this.targets = List.copyOf(targets);

         for (BlockPos p : targets) {
            this.targetCells.add(p.asLong());
         }
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return this.targetCells.contains(BlockPos.asLong(x, y, z)) || this.targetCells.contains(BlockPos.asLong(x, y + 1, z));
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double best = 1.7976931348623157E308;

         for (BlockPos p : this.targets) {
            int dy = p.getY() - y;
            if (dy == 1) {
               dy = 0;
            }

            best = Math.min(best, AiPathfinder.MoveCosts.estimateHeuristicCompat(p.getX() - x, dy, p.getZ() - z));
         }

         return best;
      }
   }

   static final class Inner_StreamUtil implements AiPathfinder.PathGoal {
      final int maxY;
      final BlockPos toward;
      final double maxAcross;
      Set<Long> blocked = Set.of();

      Inner_StreamUtil(int y, BlockPos toward) {
         this(y, toward, 1.7976931348623157E308);
      }

      Inner_StreamUtil(int y, BlockPos toward, double maxAcross) {
         this.maxY = y;
         this.toward = toward;
         this.maxAcross = maxAcross;
      }

      @Override
      public boolean load(BlockPos cell) {
         return !this.blocked.contains(cell.asLong());
      }

      static AiBlockMine.Inner_StreamUtil descentToward(BlockPos feet, BlockPos target) {
         double across = Math.hypot(feet.getX() - target.getX(), feet.getZ() - target.getZ());
         return new AiBlockMine.Inner_StreamUtil(Math.max(target.getY(), feet.getY() - 8), target, Math.max(3.0, across - 8.0));
      }

      boolean stillDescendingToward(BlockPos feet, BlockPos target) {
         return feet.getY() > this.maxY && this.toward.distSqr(target) <= 36.0;
      }

      @Override
      public boolean isInGoal(int x, int yy, int z) {
         return yy <= this.maxY;
      }

      @Override
      public double heuristic(int x, int yy, int z) {
         double across = Math.hypot(x - this.toward.getX(), z - this.toward.getZ());
         double shortfall = this.maxAcross == 1.7976931348623157E308 ? 0.0 : Math.max(0.0, across - this.maxAcross);
         return Math.max(0, yy - this.maxY) * 20.0 + shortfall * 20.0 + across;
      }

      @Override
      public String toString() {
         return "descent(y<=" + this.maxY + " toward " + this.toward.toShortString() + ")";
      }
   }

   static final class Inner_TaskQueue implements AiPathfinder.PathGoal {
      final Set<Long> dryTargetCells = new HashSet<>();
      final Set<Long> noShaftCells;
      final List<BlockPos> targetBlocks;
      final List<Vec3> targetItems;
      final List<BlockPos> wetTargets;
      final Set<Long> excludedStances = new HashSet<>();
      Set<Long> write = Set.of();
      int reachAbove = 2;
      final Set<Long> update = new HashSet<>();

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items) {
         this(blocks, items, Set.of());
      }

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items, Set<Long> noShaft) {
         this(blocks, items, noShaft, Set.of());
      }

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items, Set<Long> noShaft, Set<Long> wet) {
         this.targetBlocks = List.copyOf(blocks);
         this.targetItems = List.copyOf(items);
         this.noShaftCells = Set.copyOf(noShaft);
         List<BlockPos> beside = new ArrayList<>();

         for (BlockPos p : blocks) {
            if (wet.contains(p.asLong())) {
               beside.add(p);
            } else {
               this.dryTargetCells.add(p.asLong());
            }
         }

         this.wetTargets = List.copyOf(beside);
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         for (Vec3 item : this.targetItems) {
            if (AiBlockMine.isItemAtBlock(x, y, z, item)) {
               return true;
            }
         }

         if (this.excludedStances.contains(BlockPos.asLong(x, y, z))) {
            return false;
         }

         if (!this.wetTargets.isEmpty()) {
            BlockPos feet = new BlockPos(x, y, z);

            for (BlockPos t : this.wetTargets) {
               if (AiBlockMine.init(feet, t, false) && t.getY() - y <= 3) {
                  return true;
               }
            }
         }

         if (!this.dryTargetCells.contains(BlockPos.asLong(x, y, z)) && !this.dryTargetCells.contains(BlockPos.asLong(x, y + 1, z))) {
            long over = BlockPos.asLong(x, y + 2, z);
            return this.dryTargetCells.contains(over) && !this.noShaftCells.contains(over) ? true : this.update.contains(BlockPos.asLong(x, y, z));
         } else {
            return true;
         }
      }

      @Override
      public boolean isInGoalWithGrid(int x, int y, int z, AiPathfinder.GridView planned) {
         if (!this.isInGoal(x, y, z)) {
            return false;
         }

         if (planned != null && planned.probeCell(x, y, z).water()) {
            for (Vec3 item : this.targetItems) {
               if (AiBlockMine.isItemAtBlock(x, y, z, item)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double best = 1.7976931348623157E308;

         for (BlockPos p : this.targetBlocks) {
            int dy = p.getY() - y;
            dy = dy >= 0 && dy <= this.reachAbove ? 0 : (dy > this.reachAbove ? dy - this.reachAbove : dy);
            best = Math.min(best, AiPathfinder.MoveCosts.estimateHeuristicCompat(p.getX() - x, dy, p.getZ() - z));
         }

         for (Vec3 item : this.targetItems) {
            double dx = Math.max(0.0, Math.abs(item.x - (x + 0.5)) - 1.3);
            double dz = Math.max(0.0, Math.abs(item.z - (z + 0.5)) - 1.3);
            double dy = item.y < y - 0.5 ? item.y - (y - 0.5) : (item.y > y + 2.3 ? item.y - (y + 2.3) : 0.0);
            best = Math.min(best, Math.hypot(dx, dz) * 2.9000000000000004 + Math.abs(dy) * 5.5);
         }

         return best == 1.7976931348623157E308 ? 0.0 : best;
      }

      @Override
      public boolean write(BlockPos cell) {
         long key = cell.asLong();
         return this.dryTargetCells.contains(key) && !this.write.contains(key) && !this.noShaftCells.contains(key);
      }

      @Override
      public boolean load(BlockPos cell) {
         return !this.write.contains(cell.asLong());
      }

      boolean sameGoal(AiBlockMine.Inner_TaskQueue other) {
         if (other != null && this.dryTargetCells.equals(other.dryTargetCells) && this.excludedStances.equals(other.excludedStances) && this.targetItems.size() == other.targetItems.size()) {
            for (int i = 0; i < this.targetItems.size(); i++) {
               if (this.targetItems.get(i).distanceToSqr(other.targetItems.get(i)) > 0.25) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
