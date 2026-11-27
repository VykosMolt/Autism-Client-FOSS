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
   private static final boolean init_Z = Boolean.getBoolean("autism.navigation.trace");
   static final int load_I = 14;
   static final long init_J = 250000000L;
   static final int read_I = 64;
   static final int write_I = 20;
   static final double init_D = 8.0;
   static final int update_I = 14;
   static final double load_D = 9.0;
   static final int sync_I = 5;
   static final int flush_I = 256;
   static final long load_J = 1500000L;
   static final int close_I = 64;
   static final int open_I = 30;
   static final long read_J = 90000000000L;
   static final double read_D = 1.3;
   static final double write_D = 0.5;
   static final double update_D = 2.3;
   static final double sync_D = 5.0;
   static final double flush_D = 2.2026431718061676;
   static final double close_D = 2.4271844660194173;
   static final double open_D = 0.125;
   static final int poll_I = 4;
   static final long write_J = 12000000000L;
   static final int push_I = 6;
   static final int pop_I = 8;
   static final double poll_D = 20.0;
   static final double push_D = 0.3;
   static final int tick_I = 2;
   static final long update_J = 600000000000L;
   static final double pop_D = 4.3;
   static final double tick_D = 0.5;
   private static final Map<Long, long[]> init_Map = new HashMap<>();
   private static final long[] init_JArr = new long[2];
   static final long sync_J = 180000000000L;
   static final long flush_J = 300000000000L;
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

   static AiFailure init(boolean dropsOnly, Container inventory, List<ItemStack> observed) {
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

   static boolean init(boolean dropsOnly, Vec3 body, List<Vec3> observed) {
      if (dropsOnly) {
         for (Vec3 item : observed) {
            if (init(body, item)) {
               return true;
            }
         }
      }

      return false;
   }

   static Set<Block> init_Set(Set<Item> wanted, Predicate<Block> matches) {
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

   static Predicate<Block> init_Predicate(Set<Item> wanted, Predicate<Block> requested) {
      if (wanted.isEmpty()) {
         return requested;
      }

      Set<Block> sources = init_Set(wanted, requested);
      return sources::contains;
   }

   static boolean init_Z(BlockPos feet, BlockPos target, int radius) {
      return target.distSqr(feet) <= (double)radius * radius;
   }

   static boolean init(boolean checked, int heldGoal, int remaining, boolean ordinaryLog, boolean supported, boolean interruptible, boolean pending) {
      return !checked && heldGoal > 0 && remaining >= 8 && ordinaryLog && supported && interruptible && !pending;
   }

   static boolean init_Z_D(double dropDistance, double workDistance, int available, int needed) {
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

   static boolean init_Z_I(int x, int y, int z, Vec3 item) {
      return Math.abs(item.x - (x + 0.5)) <= 1.3 && Math.abs(item.z - (z + 0.5)) <= 1.3 && item.y >= y - 0.5 && item.y <= y + 2.3;
   }

   static boolean init(Vec3 body, Vec3 item) {
      double across = 1.425;
      return Math.abs(item.x - body.x) < across && Math.abs(item.z - body.z) < across && item.y + 0.25 >= body.y - 0.5 + 0.05 && item.y <= body.y + 2.3 - 0.05;
   }

   static Long init(Collection<Long> pending, Vec3 position, int age) {
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

   static BlockPos init(AiDigLine.Inner_DataUtil next, Vec3 eye, BlockPos current, AiDigLine.Inner_CachePool shapes, double reach) {
      List<BlockPos> chain = AiDigLine.init_List_Vec3_2(eye, next.direction(), reach, new HashSet<>(next.chain()), Set.of(current), shapes, null);
      return chain.isEmpty() ? null : chain.get(0);
   }

   static double init(BlockPos feet, BlockPos target) {
      int dy = target.getY() - feet.getY();
      double across = Math.abs(target.getX() - feet.getX()) + Math.abs(target.getZ() - feet.getZ());
      double deepAcross = target.getY() < 0 ? 2.0 : 1.0;
      int deepDown = dy < 0 ? Math.max(0, Math.min(-dy, -target.getY())) : 0;
      return across * 2.0 * deepAcross + (dy < 0 ? -dy + deepDown : dy * 2.5);
   }

   static double init_D(BlockPos feet, BlockPos target, int surfaceThere) {
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
            cost = Math.min(cost, init_D(feet, target, there));
         }
      }

      return cost;
   }

   static List<BlockPos> init(Predicate<BlockPos> matches, BlockPos start, int cap) {
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

   static double init(RotationMath.Rotation facing, Vec3 eye, BlockPos target) {
      Vec3 centre = Vec3.atCenterOf(target);
      RotationMath.Rotation look = RotationMath.lookAt(centre, eye);
      double turn = Math.abs(Mth.wrapDegrees(look.yaw() - facing.yaw())) + Math.abs(look.pitch() - facing.pitch());
      return turn + 8.0 * Math.sqrt(centre.distanceToSqr(eye));
   }

   static int[][] init(int px, int py, int pz, int chunkRadius, int minSectionY, int sections, boolean ore) {
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

   static List<int[]> init(int chunkRadius) {
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

   public static AiOperation init_AiOperation(JsonObject args) {
      if (!AutismLiteVariant.enabled()) {
         AiOperation preparation = AiMiningPreparation.load(args);
         if (preparation != null) {
            return preparation;
         }
      }

      return load(args, Set.of());
   }

   static Set<Long> init_Set(JsonObject args) {
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
      return load(options, Set.copyOf(protectedCells));
   }

   private static AiOperation load(final JsonObject args, Set<Long> callerCells) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         final Set<Long> avoid = init_Set(args);
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
         final AiMiningQuota quota = AiMiningQuota.init_as_JsonObject(args);
         final int count = quota == null ? Math.max(1, Math.min(640, AiJson.getInt(args, "count", 1))) : quota.init_I();
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
            final Object init_Obj = mc.level;
            final Set<Long> init_Set = new HashSet<>(avoid);
            final Set<Long> load_Set = new HashSet<>();
            final Predicate<Block> init_Predicate = AiBlockMine.init_Predicate(
               wantedDrops, block -> this.init_Z_3(BuiltInRegistries.BLOCK.getKey(block).toString())
            );
            final Map<Block, Boolean> init_Map = new IdentityHashMap<>();
            final Map<Item, Boolean> load_Map = new IdentityHashMap<>();
            final BiPredicate<BlockState, ItemStack> init_BiPredicate = this.init_BiPredicate();
            final Map<Long, Double> read_Map = new HashMap<>();
            BlockPos init_BlockPos;
            int init_I = -1;
            final Map<Long, Long> write_Map = new LinkedHashMap<>();
            final AiBlockMine.Inner_CachePool init_BufferHelper_Cache = new AiBlockMine.Inner_CachePool();
            final Set<Long> read_Set = new HashSet<>();
            final Map<Long, String> update_Map = new HashMap<>();
            final Map<Long, Integer> sync_Map = new HashMap<>();
            final int load_I = this.load_I_6();
            final AiAim init_DataUtil = new AiAim();
            final AiBlockDigger init_RefQueue_DataUtil = new AiBlockDigger(
                  mc,
                  s -> mc.player.getInventory().setSelectedSlot(s),
                  this.init_DataUtil::aimAt,
                  true,
                  true,
                  this.init_DataUtil::tick,
                  () -> !this.init_DataUtil.isSilent()
               )
               .init_RefQueue_DataUtil_Consumer(rotation -> this.init_DataUtil.setTargetRotation(rotation))
               .init_RefQueue_DataUtil_BiPredicate((state, tool) -> AiItems.canDropWantedItem(mc, state, tool, wantedDrops));
            final AiBlockDigger load_RefQueue_DataUtil = new AiBlockDigger(
                  mc,
                  s -> mc.player.getInventory().setSelectedSlot(s),
                  this.init_DataUtil::aimAt,
                  true,
                  false,
                  this.init_DataUtil::tick,
                  () -> !this.init_DataUtil.isSilent()
               )
               .init_RefQueue_DataUtil_Consumer(rotation -> this.init_DataUtil.setTargetRotation(rotation));
            AiBlockDigger read_RefQueue_DataUtil = this.init_RefQueue_DataUtil;
            List<BlockPos> init_List = new ArrayList<>();
            final Set<Long> write_Set = new HashSet<>();
            final AiMiningCommitment init_FieldCache = new AiMiningCommitment();
            final Set<Long> update_Set = new HashSet<>();
            final Set<Long> sync_Set = new HashSet<>();
            BlockPos load_BlockPos = args.has("focusX")
               ? new BlockPos(AiJson.getInt(args, "focusX", 0), AiJson.getInt(args, "focusY", 0), AiJson.getInt(args, "focusZ", 0))
               : null;
            final Set<Long> flush_Set = new HashSet<>();
            final Set<Long> close_Set = new LinkedHashSet<>();
            AiGoto.GotoOp init_HashUtil_CachePool;
            AiBlockMine.Inner_TaskQueue init_BufferHelper_TaskQ;
            AiBlockMine.Inner_TaskQueue load_BufferHelper_TaskQ;
            BlockPos read_BlockPos;
            int read_I;
            BlockPos write_BlockPos;
            long init_J;
            boolean init_Z;
            boolean load_Z;
            boolean read_Z;
            AiOperation init_AiOperation;
            AiOperation.Result init_AiOperation_Result;
            AiOperation load_AiOperation;
            boolean write_Z;
            boolean update_Z = AiJson.getBoolean(args, "toolPrepared", false);
            boolean sync_Z;
            final AiPlacer init_DataPipe = new AiPlacer();
            BlockPos update_BlockPos;
            BlockPos sync_BlockPos;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            int close_I;
            int open_I;
            int poll_I;
            int push_I;
            int pop_I;
            long load_J;
            long read_J;
            AiPathfinder.PathGoal init_ClassCache_JsonUti;
            AiBlockMine.Inner_BufferHelper init_BufferHelper_Buffe;
            AiBlockMine.Inner_StreamUtil init_BufferHelper_Strea;
            BlockPos flush_BlockPos;
            final Map<String, Integer> flush_Map = new HashMap<>();
            final Set<Vec3> open_Set = new HashSet<>();
            final Map<Vec3, Integer> close_Map = new HashMap<>();
            boolean flush_Z;
            int tick = -1;
            final List<Vec3> load_List = new ArrayList<>();
            double init_D;
            BlockPos close_BlockPos;
            long write_J;
            int run;
            int get;
            long update_J;
            final AiBlockMine.Inner_ConfigStore init_BufferHelper_Confi = new AiBlockMine.Inner_ConfigStore();
            int set = 2147483647;
            int put = radius;
            List<BlockPos> read_List = new ArrayList<>();
            int add;
            boolean close_Z;
            boolean open_Z;
            int[][] init_IArr2;
            long sync_J;
            BlockPos open_BlockPos;
            boolean poll_Z;
            final Map<Long, Double> open_Map = new HashMap<>();
            long flush_J = -1L;
            AiDigLine.Inner_DataUtil init_EventBus_DataUtil;
            Set<BlockPos> poll_Set = Set.of();
            final List<AiDigLine.Inner_DataUtil> write_List = new ArrayList<>();
            Set<BlockPos> push_Set = Set.of();
            BlockPos poll_BlockPos;
            List<BlockPos> update_List = List.of();
            int clear = -2147483648;
            Vec3 init_Vec3;
            BlockPos push_BlockPos;
            BlockPos pop_BlockPos;
            Vec3 load_Vec3;
            private final Map<Long, long[]> push_Map = new HashMap<>();
            private int data = -2147483648;
            final Map<Long, long[]> poll_Map = new HashMap<>();

            void init_V_14() {
               LocalPlayer p = mc.player;
               if (!p.isEyeInFluid(FluidTags.WATER)) {
                  this.write_Z = false;
               } else {
                  if (p.getAirSupply() >= p.getMaxAirSupply() - 10) {
                     this.write_Z = false;
                  }

                  if (!this.write_Z && !(p.getAirSupply() > p.getMaxAirSupply() * 0.45)) {
                     BlockPos target = this.read_BlockPos;
                     if (target == null) {
                        BlockPos feet = p.blockPosition();

                        for (BlockPos g : this.init_List_5()) {
                           if (g.distSqr(feet) <= 64.0 && AiBlockMine.init(mc, g) && (target == null || g.distSqr(feet) < target.distSqr(feet))) {
                              target = g;
                           }
                        }
                     }

                     if (target != null && AiBlockMine.init(mc, target)) {
                        this.write_Z = true;
                        AiBlockMine.init_V(target);
                        AutismClientAddon.LOG
                           .info(
                              "[AI] block mine {}: low on air under water going for {} ({} dive strikes)",
                              label,
                              target.toShortString(),
                              AiBlockMine.init_I(target)
                           );
                        if (AiBlockMine.init_I(target) >= 2) {
                           this.init_Set.add(target.asLong());
                        }
                     }
                  }
               }
            }

            AiOperation.Result init_AiOperation_Result_2(BlockPos cell) {
               if (!this.write_Z) {
                  this.write_Z = true;
                  AiBlockMine.init_V(cell);
               }

               BlockState state = mc.level.getBlockState(cell);
               double breakTicks = MiningCost.init_D_Minecraft(mc, mc.player.getMainHandItem(), state, state.getDestroySpeed(mc.level, cell));
               if (AiBlockMine.init_I(cell) >= 2 || breakTicks + AiBlockDigger.init_I_Minecraft(mc) + 40.0 > mc.player.getMaxAirSupply()) {
                  this.init_Set.add(cell.asLong());
               }

               AutismClientAddon.LOG
                  .info(
                     "[AI] block mine {}: too little air to break {} under water ({} dive strikes){}",
                     label,
                     cell.toShortString(),
                     AiBlockMine.init_I(cell),
                     this.init_Set.contains(cell.asLong()) ? "; left" : "; surfacing first"
                  );
               if (this.init_HashUtil_CachePool != null) {
                  this.init_HashUtil_CachePool.load(true);
               }

               if (!AutismLiteVariant.enabled()) {
                  this.load_AiOperation = autismclient.ai.AiBreathe.init(AiJson.objectOf("timeoutSeconds", 25));
               }

               return this.init_AiOperation_Result_6("Too little air for " + cell.toShortString() + " under water; surfacing first");
            }

            Set<Long> init_Set_3() {
               Set<Long> out = new HashSet<>();

               for (BlockPos t : this.init_List) {
                  out.add(t.asLong());
               }

               return out;
            }

            AiOperation.Result init_AiOperation_Result_11() {
               if (this.open_Z && !this.init_List.isEmpty()) {
                  BlockPos feet = mc.player.blockPosition();
                  BlockPos nearest = null;

                  for (BlockPos t : this.init_List) {
                     if (!this.init_Set.contains(t.asLong())) {
                        if (AiBlockMine.init(feet, t, false) && t.distSqr(feet) <= 16.0) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Beside the " + label + " at " + t.toShortString());
                        }

                        if (nearest == null || t.distSqr(feet) < nearest.distSqr(feet)) {
                           nearest = t;
                        }
                     }
                  }

                  return descendOnly && nearest != null && Math.abs(feet.getY() - nearest.getY()) <= 4 && (mc.player.onGround() || mc.player.isInWater())
                     ? this.init(
                        AiOperation.Status.SUCCEEDED, "Down at the " + label + " level (y " + feet.getY() + ", nearest " + nearest.toShortString() + ")"
                     )
                     : null;
               } else {
                  return null;
               }
            }

            AiOperation.Result init_AiOperation_Result_List(List<BlockPos> goalCells, BlockPos nearest, BlockPos feet) {
               double d = 1.7976931348623157E308;

               for (BlockPos t : goalCells) {
                  d = Math.min(d, Math.sqrt(t.distSqr(feet)));
               }

               long now = System.nanoTime();
               int heldx = this.load_I_6();
               int toGo = this.init_HashUtil_CachePool == null ? -1 : this.init_HashUtil_CachePool.init_I_14();
               if (toGo < 0 && this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.init_BlockPos_6() != null) {
                  BlockPos end = this.init_HashUtil_CachePool.init_BlockPos_6();
                  double e = 1.7976931348623157E308;

                  for (BlockPos t : goalCells) {
                     e = Math.min(e, Math.sqrt(t.distSqr(end)));
                  }

                  if (e < this.init_D - 2.0) {
                     toGo = this.init_HashUtil_CachePool.load_I_8();
                  }
               }

               boolean changedWork = this.update_J == 0L
                  || this.update_I != this.run
                  || heldx != this.get
                  || this.close_BlockPos != null && !goalCells.contains(this.close_BlockPos);
               if (changedWork) {
                  this.init_BufferHelper_Confi.init();
               }

               boolean along = this.init_BufferHelper_Confi
                  .init(this.init_HashUtil_CachePool == null ? null : this.init_HashUtil_CachePool.init_BlockPos_6(), toGo);
               boolean down = nearest != null && feet.getY() - nearest.getY() > 4 && feet.getY() < this.set - 1;
               if (!changedWork && !(d < this.init_D - 1.0) && !along && !down) {
                  if (now - this.update_J < 12000000000L) {
                     return null;
                  }

                  int left = 0;

                  for (BlockPos t : goalCells) {
                     if (!(t.distSqr(nearest) > 36.0)) {
                        this.init_Set.add(t.asLong());
                        this.write_Set.remove(t.asLong());
                        left++;
                     }
                  }

                  AutismClientAddon.LOG
                     .info(
                        "[AI] NO PROGRESS: block mine {} got no closer than {} to {} in {} s; left {} targets there",
                        label,
                        String.format(Locale.ROOT, "%.1f", this.init_D),
                        nearest.toShortString(),
                        12L,
                        left
                     );
                  AutismClientAddon.LOG.info("[AI] {}", AiTerrainDump.init(mc.level, feet, nearest, 6, 4));
                  this.init_BufferHelper_Strea = null;
                  this.flush_BlockPos = null;
                  this.update_J = 0L;
                  if (this.init_HashUtil_CachePool != null) {
                     this.init_HashUtil_CachePool.cancel();
                     this.init_HashUtil_CachePool = null;
                     this.init_BufferHelper_TaskQ = null;
                     this.init_ClassCache_JsonUti = null;
                  }

                  return ++this.write_I >= 14
                     ? this.init(AiOperation.Status.FAILED, "No reachable " + label + " after " + this.write_I + " attempts (" + this.init_Str_9() + ")")
                     : this.init_AiOperation_Result_6("No way to the " + label + " near " + nearest.toShortString() + "; trying elsewhere");
               } else {
                  this.set = changedWork ? feet.getY() : Math.min(this.set, feet.getY());
                  this.init_D = changedWork ? d : Math.min(this.init_D, d);
                  this.run = this.update_I;
                  this.get = heldx;
                  this.update_J = now;
                  this.close_BlockPos = nearest;
                  return null;
               }
            }

            @Override
            public boolean canInterruptNow() {
               if (this.sync_Z) {
                  return false;
               } else {
                  return this.init_AiOperation != null
                     ? this.init_AiOperation.canInterruptNow()
                     : (this.read_BlockPos == null || !this.read_RefQueue_DataUtil.init_Z_BlockPos(this.read_BlockPos))
                        && !this.init_DataPipe.needsReconciliation()
                        && (this.init_HashUtil_CachePool == null || this.init_HashUtil_CachePool.load_Z_8());
               }
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               return this.sync_Z
                  || this.init_AiOperation != null && this.init_AiOperation.requiresReconciliationAfterEmergency()
                  || this.read_BlockPos != null && this.read_RefQueue_DataUtil.init_Z_BlockPos(this.read_BlockPos)
                  || this.init_DataPipe.needsReconciliation()
                  || this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.requiresReconciliationAfterEmergency();
            }

            @Override
            public AiOperation.Result tick() {
               AiOperation.Result result = this.load_AiOperation_Result_9();
               if (!AutismLiteVariant.enabled() && result.terminal() && this.sync_Z) {
                  result = AiMiningPreparation.init(result, true, this.init_AiOperation_Result);
               }

               if (!AutismLiteVariant.enabled() && !result.terminal() && this.init_AiOperation == null) {
                  AiVisualization.init(this, this.init_List_5());
                  AiVisualization.init(
                     this,
                     this.read_Str_2(),
                     this.write_Str(),
                     held <= 0 && !dropsOnly ? this.update_I : Math.max(0, this.load_I_6() - this.load_I),
                     held > 0 ? Math.max(0, held - this.load_I) : (dropsOnly ? 0 : count)
                  );
               }

               if (!result.terminal() && this.init_AiOperation == null && mc.player != null) {
                  AiBlockDigger.init_V_Minecraft(mc);
               }

               return result;
            }

            AiOperation.Result load_AiOperation_Result_9() {
               if (!this.read_Z && mc.player != null && mc.level != null) {
                  if (mc.level != this.init_Obj) {
                     return this.init(AiOperation.Status.FAILED, "World changed");
                  }

                  if (this.init_AiOperation != null) {
                     if (System.nanoTime() > deadline) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.init_Str_9() + ")");
                     }

                     AiOperation.Result result = this.init_AiOperation.tick();
                     this.init_AiOperation_Result = result;
                     if (result.terminal()) {
                        this.sync_Z = this.sync_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
                        this.init_AiOperation.cancel();
                        this.init_AiOperation = null;
                        this.flush_V();
                     }

                     return result;
                  } else {
                     if (this.init_Z_12()) {
                        return this.init(AiOperation.Status.SUCCEEDED, this.load_Str_2());
                     }

                     if (approach) {
                        AiOperation.Result there = this.init_AiOperation_Result_11();
                        if (there != null) {
                           return there;
                        }
                     }

                     if (System.nanoTime() > deadline) {
                        return this.init(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.init_Str_9() + ")");
                     }

                     long tickNow = System.nanoTime();
                     if (this.write_J != 0L && tickNow - this.write_J > 1000000000L) {
                        this.update_J = 0L;
                     }

                     this.write_J = tickNow;
                     this.flush_I++;
                     if (this.load_AiOperation != null) {
                        AiOperation.Result breathing = this.load_AiOperation.tick();
                        if (!breathing.terminal()) {
                           return this.init_AiOperation_Result_6("Surfacing for air: " + breathing.message());
                        }

                        this.load_AiOperation.cancel();
                        this.load_AiOperation = null;
                        AiMotionSafety.releaseMovementKeys();
                        return this.init_AiOperation_Result_6("Breathing again; back to " + label);
                     } else {
                        this.init_V_14();
                        if (this.flush_I == 1
                           && this.load_BlockPos != null
                           && this.write_Z_2(this.load_BlockPos)
                           && this.init_Z(this.load_BlockPos, mc.player.blockPosition())
                           && this.sync(this.load_BlockPos)) {
                           this.init_List.add(this.load_BlockPos);
                           this.init_V_3(this.load_BlockPos);
                           this.load_BlockPos = null;
                        }

                        if (this.flush_I % 200 == 0) {
                           AutismClientAddon.LOG
                              .info(
                                 "[AI] block mine {}: tick {} at {} breaking {} ({}, {} known, goal {}, tree {}, travel {})",
                                 label,
                                 this.flush_I,
                                 mc.player.blockPosition().toShortString(),
                                 this.read_BlockPos == null ? "-" : this.read_BlockPos.toShortString(),
                                 this.init_Str_9(),
                                 this.init_List.size(),
                                 this.close_Set.size(),
                                 this.write_Set.size(),
                                 this.init_HashUtil_CachePool == null ? "none" : (this.init_HashUtil_CachePool.init_Z_13() ? "at goal" : "on")
                              );
                        }

                        if (this.read_BlockPos != null && this.flush_I - this.poll_I > 30 && !this.read_RefQueue_DataUtil.init_Z_BlockPos(this.read_BlockPos)) {
                           AutismClientAddon.LOG.info("[AI] block mine {}: gave up breaking {} in place", label, this.read_BlockPos.toShortString());
                           this.init_Set.add(this.read_BlockPos.asLong());
                           this.read_RefQueue_DataUtil.init_V();
                           this.read_BlockPos = null;
                        }

                        if (this.read_BlockPos != null) {
                           if (this.init_HashUtil_CachePool != null) {
                              this.init_HashUtil_CachePool.tick();
                           }

                           if (AiMotionAudit.init_Z()) {
                              AiMotionAudit.init_V("gather dig " + this.read_BlockPos.toShortString());
                           }

                           AiBlockDigger.Inner_DataUtil_2 status = this.init_RefQueue_DataUtil_(this.read_BlockPos);
                           if (status == AiBlockDigger.Inner_DataUtil_2.init) {
                              return this.init_AiOperation_Result_6("Breaking " + mc.level.getBlockState(this.read_BlockPos).getBlock().getName().getString());
                           }

                           BlockPos was = this.read_BlockPos;
                           this.read_BlockPos = null;
                           if (status == AiBlockDigger.Inner_DataUtil_2.load) {
                              this.open_I = 0;
                              if (this.init_Z) {
                                 this.write_V(was);
                              }
                           } else {
                              if (this.read_RefQueue_DataUtil.init_Str().equals("no tool that gets its drops")) {
                                 return this.load_AiOperation_Result_2(was);
                              }

                              if (this.read_RefQueue_DataUtil.init_Str().equals("too little air to break it under water")) {
                                 return this.init_AiOperation_Result_2(was);
                              }

                              this.init(was, "could not break it in place (" + this.read_RefQueue_DataUtil.init_Str() + ")");
                              if (was.equals(this.write_BlockPos)) {
                                 this.init_Set.add(was.asLong());
                                 this.write_BlockPos = null;
                              }

                              if (++this.open_I >= 14) {
                                 return this.init(AiOperation.Status.FAILED, "Could not break " + label + " (" + this.init_Str_9() + ")");
                              }
                           }

                           if (this.init_Z_12()) {
                              return this.init(AiOperation.Status.SUCCEEDED, this.load_Str_2());
                           }
                        }

                        this.update_V_3();
                        this.sync_V_2();
                        this.write_V_4();
                        this.write_Map.values().removeIf(until -> until < System.nanoTime());
                        this.init_BufferHelper_Cache.init_Map.keySet().retainAll(this.write_Map.keySet());
                        List<Vec3> drops = this.load_List_2();
                        if (held > 0 && this.load_I_6() + this.pop_I + this.write_Map.size() >= held) {
                           drops = this.init(true);
                        }

                        boolean enough = held > 0 ? this.load_I_6() + this.pop_I + this.write_Map.size() >= held : this.load_Z_7();
                        boolean collectFirst = enough
                           || held > 0 && this.push_I >= Math.max(1, held - this.load_I_6())
                           || this.init_Z(drops) && !this.write_Z_3();
                        if (this.update_BlockPos != null) {
                           AiPlacer.PlaceStatus status = this.init_DataPipe.tick();
                           if (status == AiPlacer.PlaceStatus.WORKING) {
                              return this.init_AiOperation_Result_6("Blocking the water next to " + this.sync_BlockPos.toShortString());
                           }

                           if (status == AiPlacer.PlaceStatus.FAILED) {
                              this.init(this.sync_BlockPos, "could not block the water beside it (" + this.init_DataPipe.failureReason() + ")");
                           }

                           this.update_BlockPos = null;
                           return this.init_AiOperation_Result_6("Water blocked");
                        } else {
                           if (this.init_HashUtil_CachePool != null
                              && this.init_ClassCache_JsonUti != null
                              && mc.gameMode instanceof AutismMultiPlayerGameModeAccessor activeDig
                              && (activeDig.autism$isDestroying() || activeDig.autism$getDestroyProgress() > 0.0F)) {
                              BlockPos cell = activeDig.autism$getDestroyBlockPos();
                              if (cell != null && this.init_HashUtil_CachePool.init_Set_4().contains(cell)) {
                                 this.flush_Z = false;
                                 this.load_List.clear();
                                 return this.init(this.init_ClassCache_JsonUti, "Finishing the route block at " + cell.toShortString());
                              }
                           }

                           if (!dropsOnly && !collectFirst && !approach) {
                              boolean unsettled = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()
                                 || this.init_HashUtil_CachePool != null && !this.init_HashUtil_CachePool.load_Z_8()
                                 || this.update_Z_3();
                              BlockPos inReach = unsettled ? null : this.load_BlockPos_2();
                              if (inReach == null
                                 && !unsettled
                                 && treeMode
                                 && held > 0
                                 && this.update_I > 0
                                 && this.write_Set.isEmpty()
                                 && this.load_I_6() + this.pop_I < held
                                 && !this.close()
                                 && this.sync_Z_2()
                                 && (this.write_BlockPos == null || !mc.level.getBlockState(this.write_BlockPos).isAir())) {
                                 BlockPos dirt = this.write_BlockPos != null ? this.write_BlockPos : this.write_BlockPos();
                                 if (dirt != null) {
                                    if (this.write_BlockPos == null) {
                                       this.init_J = System.nanoTime();
                                    }

                                    this.write_BlockPos = dirt;
                                    inReach = dirt;
                                 }
                              }

                              if (!AutismLiteVariant.enabled()
                                 && inReach != null
                                 && AiBlockMine.init(
                                    this.update_Z,
                                    held,
                                    held - this.load_I_6() - this.pop_I - this.write_Map.size(),
                                    mc.level.getBlockState(inReach).is(BlockTags.LOGS),
                                    AiMotionSafety.isPlayerSupported(),
                                    this.canInterruptNow(),
                                    this.requiresReconciliationAfterEmergency() || !this.write_Map.isEmpty()
                                 )) {
                                 this.update_Z = true;
                                 JsonObject investment = args.deepCopy();
                                 investment.addProperty("timeoutSeconds", (int)Math.max(1L, (deadline - System.nanoTime()) / 1000000000L));
                                 AiOperation prepared = AiMiningPreparation.init(investment);
                                 if (prepared != null) {
                                    this.flush_V();
                                    this.init_AiOperation = prepared;
                                    return this.init_AiOperation_Result_6("Preparing a faster axe for the remaining log batch");
                                 }
                              }

                              BlockPos water = inReach != null && this.write_Z_2(inReach) ? this.init_BlockPos_4(inReach) : null;
                              if (water != null) {
                                 Item item = this.init_Item_2();
                                 if (item == null) {
                                    this.init_Set.add(inReach.asLong());
                                    return this.init_AiOperation_Result_6("No block to hold back the water at " + inReach.toShortString());
                                 }

                                 if (this.init_HashUtil_CachePool != null) {
                                    this.init_HashUtil_CachePool.load(true);
                                 }

                                 this.read_RefQueue_DataUtil.init_V();
                                 this.update_BlockPos = water;
                                 this.sync_BlockPos = inReach;
                                 this.init_DataPipe.start(water, item);
                                 AutismClientAddon.LOG
                                    .info("[AI] block mine {}: blocking water at {} before breaking {}", label, water.toShortString(), inReach.toShortString());
                                 return this.init_AiOperation_Result_6("Blocking the water next to " + inReach.toShortString());
                              }

                              if (inReach != null) {
                                 this.load_V_7();
                                 if (this.init_HashUtil_CachePool != null) {
                                    this.init_HashUtil_CachePool.load(true);
                                 }

                                 this.init_Z = this.write_Z_2(inReach);
                                 this.read_BlockPos = inReach;
                                 this.poll_I = this.flush_I;
                                 this.read_RefQueue_DataUtil.init_V();
                                 this.read_RefQueue_DataUtil = this.init_Z ? this.init_RefQueue_DataUtil : this.load_RefQueue_DataUtil;
                                 if (AiMotionAudit.init_Z()) {
                                    AiMotionAudit.init_V("gather dig " + inReach.toShortString());
                                 }

                                 AiBlockDigger.Inner_DataUtil_2 status = this.init_RefQueue_DataUtil_(inReach);
                                 if (status == AiBlockDigger.Inner_DataUtil_2.read) {
                                    if (this.read_RefQueue_DataUtil.init_Str().equals("no tool that gets its drops")) {
                                       return this.load_AiOperation_Result_2(inReach);
                                    }

                                    if (this.read_RefQueue_DataUtil.init_Str().equals("too little air to break it under water")) {
                                       this.read_BlockPos = null;
                                       return this.init_AiOperation_Result_2(inReach);
                                    }

                                    this.init(inReach, "could not start breaking it (" + this.read_RefQueue_DataUtil.init_Str() + ")");
                                    this.read_BlockPos = null;
                                    if (inReach.equals(this.write_BlockPos)) {
                                       this.init_Set.add(inReach.asLong());
                                       this.write_BlockPos = null;
                                    }
                                 }

                                 return this.init_AiOperation_Result_6(
                                    "Breaking " + (this.init_Z ? label : "the way to " + label) + " at " + inReach.toShortString()
                                 );
                              }
                           }

                           if (collectFirst && !dropsOnly) {
                              Vec3 body = mc.player.position();
                              drops = new ArrayList<>(drops);
                              drops.removeIf(itemx -> itemx.distanceToSqr(body) > 64.0);
                           }

                           List<BlockPos> goalBlocks = collectFirst && (!drops.isEmpty() || enough) ? List.of() : this.init_List_5();
                           List<BlockPos> goalCells = new ArrayList<>(goalBlocks);
                           List<Vec3> goalItems = new ArrayList<>(drops);
                           if (!dropsOnly && !treeMode && !approach && held > 0 && !goalBlocks.isEmpty()) {
                              Vec3 body = mc.player.position();
                              double workDistance = 1.7976931348623157E308;

                              for (BlockPos block : goalBlocks) {
                                 workDistance = Math.min(workDistance, Math.sqrt(block.distToCenterSqr(body)));
                              }

                              double closerWork = workDistance;
                              int needed = Math.max(1, held - this.load_I_6());
                              goalItems.removeIf(itemx -> {
                                 int available = 0;

                                 for (Entry<Vec3, Integer> entry : this.close_Map.entrySet()) {
                                    if (entry.getKey().distanceToSqr(itemx) <= 9.0) {
                                       available += entry.getValue();
                                    }
                                 }

                                 return AiBlockMine.init_Z_D(itemx.distanceTo(body), closerWork, available, needed);
                              });
                           }

                           if (this.write_BlockPos != null) {
                              if (this.close()) {
                                 this.write_BlockPos = null;
                              } else {
                                 Vec3 stock = this.load_Vec3();
                                 if (stock != null) {
                                    goalItems.add(stock);
                                 } else if (this.flush_Z() && mc.level.getBlockState(this.write_BlockPos).isAir()) {
                                    if (this.init_HashUtil_CachePool != null) {
                                       this.init_HashUtil_CachePool.load(true);
                                    }

                                    return this.init_AiOperation_Result_6("Taking the dirt for a pillar");
                                 }
                              }
                           }

                           for (Long cell : this.write_Map.keySet()) {
                              goalItems.add(this.init_Vec3(BlockPos.of(cell)));
                           }

                           this.flush_Z = false;
                           this.load_List.clear();
                           if (!goalItems.isEmpty()) {
                              Vec3 body = mc.player.position();
                              List<Vec3> inBox = new ArrayList<>();
                              Set<Vec3> spawning = new HashSet<>();

                              for (Long cell : this.write_Map.keySet()) {
                                 spawning.add(this.init_Vec3(BlockPos.of(cell)));
                              }

                              for (Vec3 item : goalItems) {
                                 if (this.open_Set.contains(item)
                                    ? AiBlockMine.init(body, item)
                                    : spawning.contains(item)
                                       && AiBlockMine.init_Z_I(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ(), item)) {
                                    inBox.add(item);
                                 }
                              }

                              if (!inBox.isEmpty() && (!goalCells.isEmpty() || goalItems.size() > inBox.size())) {
                                 if (this.tick < 0) {
                                    this.tick = this.flush_I;
                                 }

                                 if (this.flush_I - this.tick <= 20) {
                                    goalItems.removeAll(inBox);
                                    this.load_List.addAll(inBox);
                                    this.flush_Z = true;
                                 }
                              }
                           }

                           if (!this.flush_Z) {
                              this.tick = -1;
                           }

                           if (goalCells.isEmpty() && goalItems.isEmpty()) {
                              if (this.init_Z_12()) {
                                 return this.init(AiOperation.Status.SUCCEEDED, this.load_Str_2());
                              }

                              if (held <= 0 && this.load_Z_7()) {
                                 return this.init_AiOperation_Result_6("Waiting for the last mined drops");
                              }

                              if (this.init_FieldCache.init_Z()) {
                                 return this.init_AiOperation_Result_6("Finishing the committed " + label + " vein scan");
                              }

                              if (!this.open_Z) {
                                 return this.init_AiOperation_Result_6("Scanning loaded terrain for " + label);
                              }

                              if (obsidianJob && !dropsOnly && !this.load_Set.isEmpty()) {
                                 return this.init(
                                    AiOperation.Status.FAILED,
                                    "Known obsidian is sealed by lava; cool or seal the lava before mining it (" + this.init_Str_9() + ")"
                                 );
                              }

                              if (dropsOnly) {
                                 if (this.flush_I < 30) {
                                    return this.init_AiOperation_Result_6("Waiting for the drop to land");
                                 }

                                 JsonObject out = this.init_JsonObject_25();
                                 this.flush_V();
                                 return AiOperation.Result.failed(
                                    AiFailure.NO_REACHABLE_TARGET, "No matching observed drops remain nearby (" + this.init_Str_9() + ")", out.toString()
                                 );
                              } else {
                                 if (!explore) {
                                    return this.init(AiOperation.Status.FAILED, "No " + label + " in loaded terrain around here (" + this.init_Str_9() + ")");
                                 }

                                 int loaded = Math.min(256, mc.options.getEffectiveRenderDistance() * 16);
                                 if (treeMode && !this.load_Z && this.put < loaded) {
                                    this.put = loaded;
                                    this.open_Z = false;
                                    this.close_Z = false;
                                    AutismClientAddon.LOG
                                       .info("[AI] block mine {}: none within {}, scanning every loaded chunk ({} across)", label, radius, loaded);
                                    return this.init_AiOperation_Result_6("Looking further out for " + label);
                                 }

                                 if (!this.load_Z) {
                                    this.load_Z = true;
                                    this.load_J = System.nanoTime();
                                    BlockPos wood = treeMode ? this.read_BlockPos() : null;
                                    if (wood != null) {
                                       AutismClientAddon.LOG
                                          .info("[AI] block mine {}: no tree loaded; heading for the wooded biome at {}", label, wood.toShortString());
                                    }

                                    this.init_BufferHelper_Buffe = new AiBlockMine.Inner_BufferHelper(mc.player.blockPosition(), exploreY, wood);
                                 }

                                 if (this.init_BufferHelper_Buffe.load != null
                                    && Math.hypot(
                                          mc.player.getX() - this.init_BufferHelper_Buffe.load.getX(),
                                          mc.player.getZ() - this.init_BufferHelper_Buffe.load.getZ()
                                       )
                                       < 16.0) {
                                    this.init_BufferHelper_Buffe = new AiBlockMine.Inner_BufferHelper(mc.player.blockPosition(), exploreY);
                                 }

                                 return System.nanoTime() - this.load_J > 90000000000L
                                    ? this.init(AiOperation.Status.FAILED, "No " + label + " found after exploring (" + this.init_Str_9() + ")")
                                    : this.init(this.init_BufferHelper_Buffe, "Exploring for " + label);
                              }
                           } else {
                              this.load_Z = false;
                              this.init_BufferHelper_Buffe = null;
                              if (goalItems.isEmpty() && !goalCells.isEmpty()) {
                                 BlockPos feet = mc.player.blockPosition();
                                 BlockPos nearest = null;

                                 for (BlockPos t : goalCells) {
                                    if (nearest == null || this.init_D(t, feet) < this.init_D(nearest, feet)) {
                                       nearest = t;
                                    }
                                 }

                                 AiOperation.Result stuck = this.init_AiOperation_Result_List(goalCells, nearest, feet);
                                 if (stuck != null) {
                                    return stuck;
                                 }

                                 if (this.init_BufferHelper_Strea != null
                                    && feet.getY() <= this.init_BufferHelper_Strea.init_I
                                    && this.init_BufferHelper_Strea.init_I <= nearest.getY()) {
                                    this.flush_BlockPos = nearest;
                                 }

                                 if (feet.getY() - nearest.getY() <= 4) {
                                    this.flush_BlockPos = nearest;
                                 }

                                 boolean sameCluster = this.flush_BlockPos != null && this.flush_BlockPos.distSqr(nearest) <= 36.0;
                                 if (feet.getY() - nearest.getY() > 4 && !sameCluster) {
                                    if (this.init_BufferHelper_Strea == null || !this.init_BufferHelper_Strea.init_Z_BlockPos_BlockPos(feet, nearest)) {
                                       String key = feet.getY() + ">" + nearest.toShortString();
                                       if (this.flush_Map.merge(key, 1, Integer::sum) >= 3) {
                                          int left = 0;

                                          for (BlockPos t : goalCells) {
                                             if (t.distSqr(nearest) <= 36.0) {
                                                this.init_Set.add(t.asLong());
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
                                          this.flush_Map.clear();
                                          this.init_BufferHelper_Strea = null;
                                          if (this.init_HashUtil_CachePool != null) {
                                             this.init_HashUtil_CachePool.cancel();
                                             this.init_HashUtil_CachePool = null;
                                             this.init_BufferHelper_TaskQ = null;
                                             this.init_ClassCache_JsonUti = null;
                                          }

                                          if (++this.write_I >= 14) {
                                             return this.init(
                                                AiOperation.Status.FAILED,
                                                "No reachable " + label + " after " + this.write_I + " attempts (" + this.init_Str_9() + ")"
                                             );
                                          }

                                          return this.init_AiOperation_Result_6(
                                             "No way down to the " + label + " near " + nearest.toShortString() + "; trying elsewhere"
                                          );
                                       }

                                       this.init_BufferHelper_Strea = AiBlockMine.Inner_StreamUtil.init_BufferHelper_Strea_BlockPos_BlockPos(feet, nearest);
                                       if (this.init_FieldCache.load_Z()) {
                                          this.init_V_3(nearest);
                                       }

                                       if (approach) {
                                          this.init_BufferHelper_Strea.init_Set = this.init_Set_3();
                                       }

                                       AutismClientAddon.LOG
                                          .info(
                                             "[AI] block mine {}: descending from y {} to y {} toward {} ({} goal cells, {} known, focus {}, nearest known {})",
                                             label,
                                             feet.getY(),
                                             this.init_BufferHelper_Strea.init_I,
                                             nearest.toShortString(),
                                             goalCells.size(),
                                             this.init_List.size(),
                                             this.init_FieldCache.init_I(),
                                             this.init_List.isEmpty()
                                                ? "none"
                                                : this.init_Str(this.init_List.stream().min(Comparator.comparingDouble(t -> t.distSqr(feet))).get())
                                          );
                                    }

                                    this.read_I = feet.getY() - nearest.getY();
                                    return this.init(this.init_BufferHelper_Strea, "Digging down toward " + label + " at y " + nearest.getY());
                                 }
                              }

                              if (goalItems.isEmpty() && !this.flush_Z) {
                                 this.init_BufferHelper_Strea = null;
                              }

                              Set<Long> noShaft = new HashSet<>();

                              for (BlockPos cell : goalCells) {
                                 if (this.update(cell)) {
                                    noShaft.add(cell.asLong());
                                 }
                              }

                              Set<Long> wet = new HashSet<>();

                              for (BlockPos cell : goalCells) {
                                 if (approach || this.poll(cell)) {
                                    wet.add(cell.asLong());
                                 }
                              }

                              AiBlockMine.Inner_TaskQueue goal = new AiBlockMine.Inner_TaskQueue(goalCells, goalItems, noShaft, wet);
                              if (treeMode) {
                                 goal.init_I = 5;

                                 for (BlockPos t : goalCells) {
                                    this.init(t, goal.update);
                                 }
                              }

                              for (BlockPos t : goalCells) {
                                 if (!wet.contains(t.asLong())) {
                                    for (long cell : this.init_JArr_BlockPos(t, treeMode && this.write_Set.contains(t.asLong()), goal.init_I)) {
                                       goal.update.add(cell);
                                    }
                                 }
                              }

                              if (approach) {
                                 goal.write = this.init_Set_3();
                              }

                              if (!this.update_Set.equals(goal.init_Set)) {
                                 this.update_Set.clear();
                                 this.update_Set.addAll(goal.init_Set);
                                 this.sync_Set.clear();
                              }

                              goal.read_Set.addAll(this.sync_Set);
                              if (!approach
                                 && !goalCells.isEmpty()
                                 && goalItems.isEmpty()
                                 && goal.read_List.isEmpty()
                                 && this.init_HashUtil_CachePool != null
                                 && this.init_HashUtil_CachePool.init_Z_13()
                                 && goal.init(this.init_BufferHelper_TaskQ)
                                 && mc.player.onGround()
                                 && this.init_HashUtil_CachePool.load_Z_8()) {
                                 boolean visible = false;
                                 Vec3 eye = mc.player.getEyePosition();

                                 for (BlockPos block : goalCells) {
                                    if (AiBlockMine.init(mc.player.blockPosition(), block, treeMode && this.write_Set.contains(block.asLong()))
                                       && (AiBlockInteraction.findReachableHit(mc, block) != null || this.init_BlockPos_Vec3(eye, block, mc.player.blockInteractionRange()) != null)
                                       )
                                     {
                                       visible = true;
                                       break;
                                    }
                                 }

                                 if (!visible) {
                                    BlockPos feet = mc.player.blockPosition();
                                    this.sync_Set.add(feet.asLong());
                                    goal.read_Set.add(feet.asLong());
                                    this.init_HashUtil_CachePool.cancel();
                                    this.init_HashUtil_CachePool = null;
                                    this.init_BufferHelper_TaskQ = null;
                                    this.init_ClassCache_JsonUti = null;
                                    this.close_I = 0;
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] block mine {}: stance {} cannot see its ore; approach the same targets from another cell",
                                          label,
                                          feet.toShortString()
                                       );
                                 }
                              }

                              if (this.init_HashUtil_CachePool != null && this.init_HashUtil_CachePool.init_Z_13() && goal.init(this.init_BufferHelper_TaskQ)) {
                                 if (dropsOnly) {
                                    List<ItemStack> observed = new ArrayList<>();
                                    List<Vec3> positions = new ArrayList<>();

                                    for (Entity entity : mc.level.entitiesForRendering()) {
                                       if (entity instanceof ItemEntity item
                                          && item.isAlive()
                                          && this.init(item.getItem().getItem())
                                          && item.distanceToSqr(mc.player) <= (double)this.put * this.put) {
                                          observed.add(item.getItem());
                                          positions.add(item.position());
                                       }
                                    }

                                    AiFailure failure = AiBlockMine.init(true, mc.player.getInventory(), observed);
                                    if (failure != null) {
                                       JsonObject out = this.init_JsonObject_25();
                                       out.addProperty("observedDropStacks", observed.size());
                                       JsonArray dropGoals = new JsonArray();

                                       for (Vec3 item : drops) {
                                          dropGoals.add(AiJson.objectOf("x", item.x, "y", item.y, "z", item.z));
                                       }

                                       out.add("dropGoals", dropGoals);
                                       this.flush_V();
                                       return AiOperation.Result.failed(
                                          failure, "Matching drops are observed but the inventory cannot accept them", out.toString()
                                       );
                                    }

                                    if (AiBlockMine.init(true, mc.player.position(), positions)) {
                                       this.close_I = 0;
                                       return this.init_AiOperation_Result_6("Waiting for the observed " + label + " pickup; inventory has room");
                                    }
                                 }

                                 if (++this.close_I > 20) {
                                    BlockPos feet = mc.player.blockPosition();
                                    List<Vec3> inBox = new ArrayList<>();

                                    for (Vec3 item : drops) {
                                       if (AiBlockMine.init_Z_I(feet.getX(), feet.getY(), feet.getZ(), item)) {
                                          inBox.add(item);
                                       }
                                    }

                                    if (inBox.isEmpty() && goalCells.isEmpty() && !drops.isEmpty()) {
                                       if (!goal.init(this.load_BufferHelper_TaskQ)) {
                                          this.load_BufferHelper_TaskQ = goal;
                                          this.close_I = 0;
                                          if (this.init_HashUtil_CachePool != null) {
                                             this.init_HashUtil_CachePool.cancel();
                                             this.init_HashUtil_CachePool = null;
                                          }

                                          this.init_BufferHelper_TaskQ = null;
                                          this.init_ClassCache_JsonUti = null;
                                          return this.init_AiOperation_Result_6("Planning to the " + label + " drop afresh");
                                       }

                                       inBox.addAll(drops);
                                    }

                                    BlockPos gone = inBox.isEmpty() ? this.init_BlockPos_5() : this.init_BlockPos(inBox);
                                    AutismClientAddon.LOG
                                       .info(
                                          "[AI] block mine {}: nothing breakable from the goal, blacklisted {} (at {}, drops {}; {})",
                                          label,
                                          gone == null ? "-" : gone.toShortString(),
                                          mc.player.position(),
                                          drops.size() > 3 ? drops.subList(0, 3) : drops,
                                          this.update_Str()
                                       );
                                    this.close_I = 0;
                                 }
                              } else {
                                 this.close_I = 0;
                              }

                              if (goalCells.isEmpty()
                                 && this.init_HashUtil_CachePool != null
                                 && this.init_HashUtil_CachePool.init_Z_13()
                                 && goal.init(this.init_BufferHelper_TaskQ)
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
                                    if (this.init(near)) {
                                       this.poll_Z = true;
                                       return this.init_AiOperation_Result_6("Stepping onto the " + label + " drop");
                                    }

                                    this.load_V_7();
                                    AiMotionSafety.releaseMovementKeys();
                                    if (!AiMotionSafety.isHorizontalMoveSafe(0.0, 0.0)) {
                                       AiMotionSafety.applyEmergencyBrake();
                                    }

                                    this.init_BlockPos(List.of(near));
                                    return this.init_AiOperation_Result_6("Leaving a drop whose pickup step would touch lava or leave the floor");
                                 }
                              }

                              this.load_V_7();
                              return this.init(
                                 goal,
                                 (goalCells.isEmpty() ? "Collecting " : "Mining ")
                                    + label
                                    + " ("
                                    + this.init_List.size()
                                    + " known, "
                                    + drops.size()
                                    + " drops)"
                              );
                           }
                        }
                     }
                  }
               } else {
                  return this.init(AiOperation.Status.CANCELLED, "Cancelled");
               }
            }

            AiOperation.Result init(AiPathfinder.PathGoal goal, String message) {
               if (this.init_FieldCache.load_Z() && goal instanceof AiBlockMine.Inner_TaskQueue t && !t.init_List.isEmpty()) {
                  this.init_V_3(t.init_List.getFirst());
               }

               boolean changed = goal instanceof AiBlockMine.Inner_TaskQueue t ? !t.init(this.init_BufferHelper_TaskQ) : goal != this.init_ClassCache_JsonUti;
               if (AiBlockMine.init_Z && changed && goal instanceof AiBlockMine.Inner_TaskQueue t) {
                  AutismClientAddon.LOG
                     .info(
                        "[AI] gather goal {} blocks (first {}), {} items (first {}), was {} blocks / {} items",
                        t.init_List.size(),
                        t.init_List.isEmpty() ? "-" : t.init_List.subList(0, Math.min(3, t.init_List.size())),
                        t.load_List.size(),
                        t.load_List.isEmpty() ? "-" : t.load_List.get(0),
                        this.init_BufferHelper_TaskQ == null ? -1 : this.init_BufferHelper_TaskQ.init_List.size(),
                        this.init_BufferHelper_TaskQ == null ? -1 : this.init_BufferHelper_TaskQ.load_List.size()
                     );
               }

               if (this.init_HashUtil_CachePool == null) {
                  if (goal instanceof AiBlockMine.Inner_TaskQueue t && !t.init_List.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] block mine {}: route from {} to {} targets, nearest {} (h {})",
                           label,
                           mc.player.blockPosition().toShortString(),
                           t.init_List.size(),
                           t.init_List.get(0).toShortString(),
                           String.format(Locale.ROOT, "%.1f", t.heuristic(mc.player.getBlockX(), mc.player.getBlockY(), mc.player.getBlockZ()))
                        );
                  }

                  this.init_HashUtil_CachePool = AiGoto.createPersistentOpCompat(
                     mc.player.blockPosition(), AiGoto.protectingCellsCompat(goal, protectedCells), (int)Math.max(20L, (deadline - System.nanoTime()) / 1000000000L)
                  );
                  this.init_HashUtil_CachePool.init_V_BiPredicate(this.init_BiPredicate);

                  for (Long cell : this.flush_Set) {
                     this.init_HashUtil_CachePool.init_V_BlockPos(BlockPos.of(cell));
                  }
               } else {
                  this.init_HashUtil_CachePool.load(false);
                  boolean airborne = !mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable();
                  if (changed && airborne) {
                     AiOperation.Result r = this.init_HashUtil_CachePool.tick();
                     return r.terminal() ? this.init_AiOperation_Result_6(message + ": landing") : this.init_AiOperation_Result_6(message + ": " + r.message());
                  }

                  if (changed) {
                     this.init_HashUtil_CachePool.init_V_ClassCache_JsonUti(AiGoto.protectingCellsCompat(goal, protectedCells));
                  }
               }

               this.init_HashUtil_CachePool.init(this.flush_Z, this.flush_Z ? this::read_Z_2 : null);
               this.init_HashUtil_CachePool.init_V_I_2(goal instanceof AiBlockMine.Inner_StreamUtil ? this.read_I : 0);
               List<BlockPos> work;
               List var20 = work = goal instanceof AiBlockMine.Inner_TaskQueue t
                  ? (t.init_List.isEmpty() && !t.load_List.isEmpty() && !dropsOnly ? this.init_List_5() : t.init_List)
                  : List.of();
               if (!work.isEmpty()) {
                  boolean trees = treeMode;
                  this.init_HashUtil_CachePool.init_V_Function(end -> this.init_Vec3_BlockPos(end, work, trees));
               } else {
                  this.init_HashUtil_CachePool.init_V_Function(null);
               }

               this.init_ClassCache_JsonUti = goal;
               this.init_BufferHelper_TaskQ = goal instanceof AiBlockMine.Inner_TaskQueue t ? t : null;
               AiOperation.Result r = this.init_HashUtil_CachePool.tick();
               if (!r.terminal()) {
                  return this.init_AiOperation_Result_6(message + ": " + r.message());
               }

               AiBlockMine.Inner_TaskQueue failedGoal = this.init_BufferHelper_TaskQ;
               BlockPos stuck = this.init_HashUtil_CachePool.load_BlockPos_3();
               if (stuck != null) {
                  this.flush_Set.add(stuck.asLong());
               }

               this.init_HashUtil_CachePool.cancel();
               this.init_HashUtil_CachePool = null;
               this.init_BufferHelper_TaskQ = null;
               this.init_ClassCache_JsonUti = null;
               this.init_BufferHelper_Strea = null;
               if (r.status() == AiOperation.Status.CANCELLED) {
                  return this.init(AiOperation.Status.CANCELLED, "Cancelled");
               }

               if (r.failure() == AiFailure.MISSING_TOOL || r.failure() == AiFailure.TOOL_WORN) {
                  this.flush_V();
                  return r;
               }

               if (r.status() == AiOperation.Status.TIMED_OUT) {
                  return this.init(AiOperation.Status.TIMED_OUT, "Ran out of time mining " + label + " (" + this.init_Str_9() + ")");
               }

               if (r.message() != null && r.message().startsWith("Health too low")) {
                  return this.init(AiOperation.Status.FAILED, r.message());
               }

               BlockPos closest = !this.write_Set.isEmpty()
                  ? BlockPos.of(this.write_Set.iterator().next())
                  : (failedGoal != null && failedGoal.init_List.isEmpty() ? this.init_BlockPos(failedGoal.load_List) : this.init_BlockPos_5());
               if (!this.write_Set.isEmpty()) {
                  this.init_Set.addAll(this.write_Set);
                  this.write_Set.clear();
               }

               this.init_FieldCache.init_V();
               AutismClientAddon.LOG
                  .info(
                     "[AI] block mine {}: route failed ({}), blacklisted {}: {}",
                     label,
                     this.write_I + 1,
                     closest == null ? "-" : closest.toShortString(),
                     r.message()
                  );
               return ++this.write_I >= 14
                  ? this.init(AiOperation.Status.FAILED, "No reachable " + label + " after " + this.write_I + " attempts (" + this.init_Str_9() + ")")
                  : this.init_AiOperation_Result_6("Could not reach it; trying the next");
            }

            void init(BlockPos cell, String why) {
               if (!this.update(cell)) {
                  int n = this.sync_Map.merge(cell.asLong(), 1, Integer::sum);
                  AutismClientAddon.LOG.info("[AI] block mine {}: {} at {} ({}/3)", label, why, cell.toShortString(), n);
                  if (n >= 3) {
                     this.init_Set.add(cell.asLong());
                     this.write_Set.remove(cell.asLong());
                  }
               }
            }

            BlockPos init_BlockPos_5() {
               BlockPos feet = mc.player.blockPosition();
               BlockPos best = null;

               for (BlockPos t : this.init_List_5()) {
                  if (best == null || t.distSqr(feet) < best.distSqr(feet)) {
                     best = t;
                  }
               }

               for (Vec3 item : this.load_List_2()) {
                  BlockPos cell = BlockPos.containing(item);
                  if (best == null || cell.distSqr(feet) < best.distSqr(feet)) {
                     best = cell;
                  }
               }

               if (best != null) {
                  this.init_Set.add(best.asLong());
                  this.write_Set.remove(best.asLong());
               }

               return best;
            }

            BlockPos init_BlockPos(List<Vec3> items) {
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
               this.init_Set.add(cell.asLong());
               return cell;
            }

            void load_V_7() {
               if (this.poll_Z) {
                  this.poll_Z = false;
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               }
            }

            boolean init(Vec3 point) {
               double dx = point.x - mc.player.getX();
               double dz = point.z - mc.player.getZ();
               double yaw = Math.toRadians(mc.player.getYRot());
               double forward = dx * -Math.sin(yaw) + dz * Math.cos(yaw);
               double right = dx * -Math.cos(yaw) + dz * -Math.sin(yaw);
               if (!AiMotionSafety.isSegmentHazardFree(mc.player.position(), new Vec3(point.x, mc.player.getY(), point.z))
                  || !AiMotionSafety.isHorizontalMoveSafe(forward > 0.2 ? 1.0 : (forward < -0.2 ? -1.0 : 0.0), right > 0.2 ? -1.0 : (right < -0.2 ? 1.0 : 0.0))) {
                  return false;
               } else if (this.init(point.x, point.z) && this.init((point.x + mc.player.getX()) / 2.0, (point.z + mc.player.getZ()) / 2.0)) {
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

            boolean init(double x, double z) {
               BlockPos cell = BlockPos.containing(x, mc.player.getY() - 0.5, z);

               for (int d = 0; d <= 2; d++) {
                  BlockPos below = cell.below(d);
                  if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                     return true;
                  }
               }

               return false;
            }

            void init_V_3(BlockPos seed) {
               if (!treeMode && !approach && !dropsOnly) {
                  this.init_FieldCache.init(seed, this.init_List, cell -> !this.init_Set.contains(cell.asLong()));
                  this.close_Set.clear();
               } else {
                  this.init_FieldCache.init_V();
               }
            }

            void read_V_5() {
               this.init_FieldCache.init(this.init_List, cell -> !this.init_Set.contains(cell.asLong()));
            }

            Vec3 init_Vec3(BlockPos cell) {
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

            void load_V_2(BlockPos cell) {
               if (this.read_Set.add(cell.asLong()) && (quota == null || quota.load(this.update_Map.get(cell.asLong())))) {
                  this.update_I++;
               }
            }

            void read_V(BlockPos cell) {
               if (quota != null) {
                  if (mc.level.getChunkSource().getChunk(cell.getX() >> 4, cell.getZ() >> 4, ChunkStatus.FULL, false) == null) {
                     return;
                  }

                  if (this.init_Z_3(BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell).getBlock()).toString())) {
                     return;
                  }
               }

               this.load_V_2(cell);
            }

            void write_V(BlockPos cell) {
               this.load_V_2(cell);
               if (this.init_FieldCache.load_Z()) {
                  this.init_V_3(cell);
               } else {
                  this.read_V_5();
               }

               if (cell.distSqr(mc.player.blockPosition()) <= 64.0) {
                  this.write_Map.put(cell.asLong(), System.nanoTime() + 250000000L);
               }

               if (treeMode) {
                  boolean fresh = this.write_Set.isEmpty();
                  this.write_Set.remove(cell.asLong());

                  for (int dx = -1; dx <= 1; dx++) {
                     for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                           BlockPos n = cell.offset(dx, dy, dz);
                           if (this.write_Z_2(n) && !this.write_Set.contains(n.asLong())) {
                              for (BlockPos log : AiBlockMine.init(this::write_Z_2, n, 64)) {
                                 if (!this.init_Set.contains(log.asLong()) && !this.open(log)) {
                                    this.write_Set.add(log.asLong());
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (fresh) {
                     this.sync_I++;
                  }
               }
            }

            AiBlockDigger.Inner_DataUtil_2 init_RefQueue_DataUtil_(BlockPos cell) {
               Set<UUID> before = null;
               if (this.init_Z && !this.read_RefQueue_DataUtil.init_Z_BlockPos(cell) && this.write_Z_2(cell)) {
                  before = new HashSet<>();
                  Vec3 centre = Vec3.atCenterOf(cell);

                  for (Entity entity : mc.level.entitiesForRendering()) {
                     if (entity instanceof ItemEntity item
                        && item.isAlive()
                        && this.init(item.getItem().getItem())
                        && item.position().distanceToSqr(centre) <= 64.0) {
                        before.add(item.getUUID());
                     }
                  }
               }

               AiBlockDigger.Inner_DataUtil_2 result = this.read_RefQueue_DataUtil
                  .init_RefQueue_DataUtil__BlockPos(cell, this.write_Vec3(cell), this.load_Vec3(cell));
               if (before != null && (this.read_RefQueue_DataUtil.init_Z_BlockPos(cell) || result == AiBlockDigger.Inner_DataUtil_2.load)) {
                  this.init_BufferHelper_Cache.init(cell.asLong(), before);
               }

               return result;
            }

            List<BlockPos> init_List_5() {
               BlockPos feet = mc.player.blockPosition();
               List<BlockPos> out = new ArrayList<>();
               if (this.load_BlockPos != null && this.open_Z) {
                  BlockPos near = null;

                  for (BlockPos t : this.init_List) {
                     if (!this.init_Set.contains(t.asLong())
                        && t.distSqr(this.load_BlockPos) <= 36.0
                        && (near == null || t.distSqr(this.load_BlockPos) < near.distSqr(this.load_BlockPos))) {
                        near = t;
                     }
                  }

                  if (near != null && this.init_FieldCache.load_Z()) {
                     this.init_V_3(near);
                  }

                  this.load_BlockPos = null;
               }

               if (this.init_FieldCache.init_Z()) {
                  this.init_FieldCache.init(this.init_Set_3());
                  if (this.init_FieldCache.init(System.nanoTime(), this.open_Z, 250000000L)) {
                     this.init_FieldCache.init_V();
                  }
               }

               if (!this.write_Set.isEmpty()) {
                  for (Long cell : this.write_Set) {
                     out.add(BlockPos.of(cell));
                  }
               } else if (this.init_FieldCache.init_Z() && !dropsOnly) {
                  out.addAll(this.init_FieldCache.init_List());
               } else if (!dropsOnly) {
                  Set<Long> known = new HashSet<>();

                  for (BlockPos t : this.init_List) {
                     known.add(t.asLong());
                  }

                  this.close_Set.removeIf(cell -> !known.contains(cell));

                  for (BlockPos t : this.init_List) {
                     if (this.close_Set.size() >= 64) {
                        break;
                     }

                     this.close_Set.add(t.asLong());
                  }

                  for (Long cell : this.close_Set) {
                     out.add(BlockPos.of(cell));
                  }
               }

               out.removeIf(gx -> gx.equals(feet) || gx.equals(feet.above()) || gx.equals(feet.above(2)));
               if (!dropsOnly) {
                  out = AiBlockMine.init_Z()
                     ? new ArrayList<>(out.stream().filter(gx -> !AiBlockMine.init(mc, gx)).toList())
                     : AiBlockMine.init_List_List(out, feet, gx -> AiBlockMine.init(mc, gx), this::init_D_2);
               }

               if (oreJob && this.write_Set.isEmpty() && this.init_FieldCache.load_Z()) {
                  List<BlockPos> buried = new ArrayList<>();

                  for (BlockPos g : out) {
                     if (!this.init_Z_5(g) && !this.poll(g)) {
                        buried.add(g);
                     }
                  }

                  int still = held > 0 ? Math.max(1, held - this.load_I_6()) : Math.max(1, count - this.update_I);
                  if (buried.size() >= still && buried.size() < out.size()) {
                     out = buried;
                  }
               }

               out.sort(Comparator.comparingDouble(gx -> this.init_D(gx, feet)));
               return out;
            }

            String init_Str(BlockPos cell) {
               for (Direction side : Direction.values()) {
                  BlockPos n = cell.relative(side);
                  if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !this.load_Z_3(n)) {
                     return cell.toShortString() + " open to a cave at " + n.toShortString();
                  }
               }

               return cell.toShortString()
                  + (this.poll(cell) ? " wet" : " buried")
                  + (this.init_Set.contains(cell.asLong()) ? ", blacklisted" : "")
                  + (this.close_Set.contains(cell.asLong()) ? ", in the goal set" : ", not in the goal set");
            }

            double init_D(BlockPos target, BlockPos feet) {
               if (!oreJob) {
                  return target.distSqr(feet);
               }

               if (this.init_I != this.flush_I || !feet.equals(this.init_BlockPos)) {
                  this.read_Map.clear();
                  this.init_BlockPos = feet;
                  this.init_I = this.flush_I;
               }

               return this.read_Map.computeIfAbsent(target.asLong(), key -> AiBlockMine.init(mc.level, feet, target));
            }

            double init_D_2(BlockPos target) {
               if (AiBlockMine.init_Z()) {
                  return 1.0 / 0.0;
               }

               if (this.flush_J != this.flush_I) {
                  this.open_Map.clear();
                  this.flush_J = this.flush_I;
               }

               return this.open_Map.computeIfAbsent(target.asLong(), key -> AiBlockMine.init(mc, target, true));
            }

            boolean init_Z_5(BlockPos cell) {
               for (Direction side : Direction.values()) {
                  BlockPos n = cell.relative(side);
                  if (mc.level.getBlockState(n).isAir() && mc.level.getBrightness(LightLayer.SKY, n) == 0 && !this.load_Z_3(n)) {
                     return true;
                  }
               }

               return false;
            }

            boolean load_Z_3(BlockPos air) {
               return AiEditLedger.update(air);
            }

            void write_V_4() {
               BlockPos feet = mc.player.blockPosition();
               this.init_List.removeIf(t -> {
                  if (this.init_Set.contains(t.asLong())) {
                     return true;
                  }

                  if (this.write_Z_2(t)) {
                     return false;
                  }

                  if (t.distSqr(feet) <= 49.0) {
                     this.read_V(t);
                  }

                  return true;
               });
               this.write_Set.removeIf(cell -> {
                  BlockPos p = BlockPos.of(cell);
                  if (this.write_Z_2(p)) {
                     return this.init_Set.contains(cell);
                  }

                  if (p.distSqr(feet) <= 49.0) {
                     this.read_V(p);
                  }

                  return true;
               });
            }

            boolean init_Z_12() {
               if (held > 0) {
                  return this.load_I_6() >= held
                     || heldMin > 0
                        && this.load_I_6() >= heldMin
                        && this.open_Z
                        && this.write_Set.isEmpty()
                        && this.init(nearRadius)
                        && this.read_I_3() == 0
                        && (!treeMode || !this.sync_Z_2() || !this.close() && this.write_BlockPos == null && this.write_BlockPos() == null)
                        && (!treeMode || !this.read_Z_4());
               }

               if (!this.load_Z_7()) {
                  return false;
               }

               if (this.read_J == 0L) {
                  this.read_J = System.nanoTime();
               }

               long settling = System.nanoTime() - this.read_J;
               return settling >= 250000000L && this.read_I_3() == 0 || settling > 3000000000L;
            }

            boolean load_Z_7() {
               return quota != null
                  ? quota.init_Z()
                  : this.update_I >= count || treeLimit > 0 && this.sync_I >= treeLimit && this.write_Set.isEmpty() && this.update_I > 0;
            }

            boolean read_Z_4() {
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos t : this.init_List) {
                  if (!this.init_Set.contains(t.asLong()) && this.write_Z_2(t) && AiBlockMine.init(feet, t, true) && AiBlockInteraction.findReachableHit(mc, t) != null) {
                     return true;
                  }
               }

               return false;
            }

            boolean init(int radiusx) {
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos t : this.init_List) {
                  if (!this.init_Set.contains(t.asLong()) && AiBlockMine.init_Z(feet, t, radius)) {
                     return false;
                  }
               }

               return true;
            }

            String init_Str_9() {
               return held > 0 ? this.load_I_6() + "/" + held + " held" : this.update_I + "/" + count + " broken";
            }

            String load_Str_2() {
               return "Mined "
                  + this.update_I
                  + " "
                  + label
                  + (held > 0 ? ", holding " + this.load_I_6() : "")
                  + (this.sync_I > 0 ? " from " + this.sync_I + " tree(s)" : "");
            }

            String read_Str_2() {
               String item = !dropArg.isEmpty() ? dropArg : (dropIds.size() == 1 ? dropIds.iterator().next() : label);
               item = item.replace("minecraft:", "").replace('_', ' ');
               return (dropsOnly ? "Collecting " : "Mining ") + item;
            }

            String write_Str() {
               if (this.update_BlockPos != null) {
                  return "Securing the next block";
               } else if (this.read_BlockPos != null) {
                  return this.init_Z ? "Breaking blocks" : "Clearing access";
               } else if (mc.gameMode instanceof AutismMultiPlayerGameModeAccessor active && active.autism$isDestroying()) {
                  BlockPos cell = active.autism$getDestroyBlockPos();
                  return cell != null && this.write_Z_2(cell) ? "Breaking blocks" : "Clearing access";
               } else if (this.poll_Z
                  || this.flush_Z
                  || this.init_ClassCache_JsonUti instanceof AiBlockMine.Inner_TaskQueue target && target.init_List.isEmpty() && !target.load_List.isEmpty()) {
                  return "Collecting drops";
               } else if (!this.load_Z && !this.init_List.isEmpty()) {
                  return this.init_HashUtil_CachePool != null && !this.init_HashUtil_CachePool.init_Z_13()
                     ? "Moving to the next blocks"
                     : "Mining the nearby batch";
               } else {
                  return "Searching nearby terrain";
               }
            }

            int init_I_12() {
               return Math.max(1, held > 0 ? held - this.load_I_6() - this.pop_I - this.write_Map.size() : count - this.update_I);
            }

            ToDoubleFunction<BlockPos> init_ToDoubleFunction() {
               Map<BlockState, Double> costs = new IdentityHashMap<>();
               return cell -> costs.computeIfAbsent(mc.level.getBlockState(cell), state -> {
                  int slot = MiningCost.init_I_Minecraft_2(mc, state, true, (block, tool) -> AiItems.canDropWantedItem(mc, block, tool, wantedDrops));
                  return slot < 0 ? 1.0 / 0.0 : MiningCost.init_D_Minecraft(mc, mc.player.getInventory().getItem(slot), state, state.getDestroySpeed(mc.level, cell));
               });
            }

            boolean init(Item item) {
               return this.load_Map.computeIfAbsent(item, it -> {
                  String id = BuiltInRegistries.ITEM.getKey(it).toString();
                  if (!dropIds.isEmpty()) {
                     return dropIds.contains(id);
                  }

                  if (dropArg.isEmpty() || !id.equals(dropArg) && !AiMine.init(dropArg, id)) {
                     if (this.init_Z_3(id)) {
                        return true;
                     }

                     for (Block source : AiItems.sourceBlocksFor(it)) {
                        if (this.init_Z_3(BuiltInRegistries.BLOCK.getKey(source).toString())) {
                           return true;
                        }
                     }

                     return false;
                  } else {
                     return true;
                  }
               });
            }

            int load_I_6() {
               int total = 0;
               Inventory inventory = mc.player.getInventory();

               for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
                  ItemStack stack = inventory.getItem(slot);
                  if (!stack.isEmpty() && this.init(stack.getItem())) {
                     total += stack.getCount();
                  }
               }

               return total;
            }

            boolean read_Z_2(BlockPos cell) {
               for (Vec3 item : this.load_List) {
                  if (Math.abs(item.x - (cell.getX() + 0.5)) > 1.0
                     || Math.abs(item.z - (cell.getZ() + 0.5)) > 1.0
                     || item.y < cell.getY() - 0.5
                     || item.y > cell.getY() + 2.3) {
                     return false;
                  }
               }

               return true;
            }

            Vec3 init(ItemEntity item) {
               Vec3 at = item.position();
               Vec3 v = item.getDeltaMovement();
               double glide = item.onGround() ? 2.4271844660194173 : 4.0;
               double gx = Math.max(-1.5, Math.min(1.5, v.x * glide));
               double gz = Math.max(-1.5, Math.min(1.5, v.z * glide));
               if (gx * gx + gz * gz > 1.0E-4 && this.init_Z_Vec3(at, gx, gz)) {
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

            boolean init_Z_Vec3(Vec3 at, double gx, double gz) {
               BlockPos to = BlockPos.containing(at.x + gx, at.y + 0.05, at.z + gz);
               return mc.level.getBlockState(to).getCollisionShape(mc.level, to).isEmpty();
            }

            boolean init_Z(List<Vec3> drops) {
               if (dropsOnly) {
                  return false;
               }

               Vec3 at = mc.player.position();

               for (Vec3 item : drops) {
                  if (!(item.distanceToSqr(at) > 25.0) && !AiBlockMine.init(at, item)) {
                     return true;
                  }
               }

               return false;
            }

            boolean write_Z_3() {
               if (!dropsOnly && !approach && mc.player.onGround()) {
                  BlockPos feet = mc.player.blockPosition();

                  for (BlockPos block : this.init_List_5()) {
                     if (AiBlockMine.init(feet, block, treeMode && this.write_Set.contains(block.asLong())) && AiBlockInteraction.findReachableHit(mc, block) != null) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            }

            int read_I_3() {
               int n = 0;

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item && item.isAlive() && this.init(item.getItem().getItem()) && entity.distanceToSqr(mc.player) <= 64.0) {
                     n++;
                  }
               }

               return n;
            }

            List<Vec3> load_List_2() {
               return this.init(false);
            }

            List<Vec3> init(boolean all) {
               List<Vec3> out = new ArrayList<>();
               this.push_I = 0;
               this.pop_I = 0;
               this.open_Set.clear();
               this.close_Map.clear();
               Vec3 at = mc.player.position();

               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && this.init(item.getItem().getItem())
                     && !(entity.position().distanceToSqr(at) > (double)radius * radius)
                     && !this.init_Set.contains(entity.blockPosition().asLong())) {
                     Long spawned = this.init_BufferHelper_Cache.init(this.write_Map.keySet(), item.getUUID(), item.position(), item.getAge());
                     if (spawned != null) {
                        this.write_Map.remove(spawned);
                     }

                     boolean nearTree = false;
                     if (!all) {
                        for (Long cell : this.write_Set) {
                           if (BlockPos.of(cell).distToCenterSqr(entity.position()) <= 9.0) {
                              nearTree = true;
                              break;
                           }
                        }
                     }

                     boolean counts = entity.position().distanceToSqr(at) <= 64.0;
                     if (counts) {
                        this.pop_I = this.pop_I + item.getItem().getCount();
                     }

                     if (!nearTree) {
                        Vec3 rest = this.init(item);
                        out.add(rest);
                        this.close_Map.merge(rest, item.getItem().getCount(), Integer::sum);
                        if (counts) {
                           this.push_I = this.push_I + item.getItem().getCount();
                        }

                        if (item.getAge() < 14) {
                           this.open_Set.add(rest);
                        }
                     }

                     if (out.size() >= 24) {
                        break;
                     }
                  }
               }

               return out;
            }

            AiDigLine.Inner_CachePool init_EventBus_CachePool() {
               return pos -> mc.level.getBlockState(pos).getShape(mc.level, pos);
            }

            void init_V_Vec3(Vec3 from, List<BlockPos> heads, Set<BlockPos> cells, double reach) {
               while (!this.write_List.isEmpty() && Collections.disjoint(this.write_List.get(0).chain(), cells)) {
                  this.write_List.remove(0);
               }

               boolean valid = !this.write_List.isEmpty()
                  && !Collections.disjoint(this.push_Set, cells)
                  && AiDigLine.init(this.write_List.get(0), from, cells, this.init_EventBus_CachePool(), reach) != null;
               if (!valid) {
                  this.write_List.clear();
                  this.write_List
                     .addAll(
                        AiDigLine.init_List_Vec3(
                           from,
                           mc.player.getYRot(),
                           mc.player.getXRot(),
                           heads,
                           cells,
                           this.init_EventBus_CachePool(),
                           reach,
                           this.init_I_12(),
                           this.init_ToDoubleFunction()
                        )
                     );
                  this.push_Set = Set.copyOf(cells);
                  if (AiBlockMine.init_Z && !this.write_List.isEmpty()) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] dig plan from {}: {}",
                           mc.player.blockPosition().toShortString(),
                           this.write_List
                              .stream()
                              .map(l -> String.format(Locale.ROOT, "%.0f/%.0f %s", l.yaw(), l.pitch(), l.chain().stream().map(Vec3i::toShortString).toList()))
                              .toList()
                        );
                  }
               }
            }

            Vec3 load_Vec3(BlockPos cell) {
               if (this.write_List.size() >= 2 && this.init_EventBus_DataUtil != null && this.write_List.get(0).equals(this.init_EventBus_DataUtil)) {
                  List<BlockPos> chain = this.init_EventBus_DataUtil.chain();
                  int at = chain.indexOf(cell);
                  if (at < 0) {
                     return null;
                  }

                  for (int i = at + 1; i < chain.size(); i++) {
                     if (!mc.level.getBlockState(chain.get(i)).isAir()) {
                        return null;
                     }
                  }

                  AiDigLine.Inner_DataUtil next = this.write_List.get(1);
                  BlockPos led = AiBlockMine.init(next, mc.player.getEyePosition(), cell, this.init_EventBus_CachePool(), mc.player.blockInteractionRange());
                  if (led != null
                     && !this.init_Set.contains(led.asLong())
                     && this.write_Z_2(led)
                     && this.sync(led)
                     && AiBlockMine.init(mc.player.blockPosition(), led, treeMode && this.write_Set.contains(led.asLong()))) {
                     return MiningCost.init_I_Minecraft_2(mc, mc.level.getBlockState(led), true, (state, tool) -> AiItems.canDropWantedItem(mc, state, tool, wantedDrops)) < 0
                        ? null
                        : next.direction();
                  } else {
                     return null;
                  }
               } else {
                  return null;
               }
            }

            Vec3 read_Vec3(BlockPos cell) {
               if (mc.player == null) {
                  return null;
               }

               Vec3 from = this.init_Vec3_6();
               double reach = mc.player.blockInteractionRange();
               BlockPos led = this.init_EventBus_DataUtil == null
                  ? null
                  : AiDigLine.init(this.init_EventBus_DataUtil, from, List.of(cell), this.init_EventBus_CachePool(), reach);
               if (led == null && this.push_Set.contains(cell) && !this.read_RefQueue_DataUtil.init_Z_BlockPos(cell)) {
                  Set<BlockPos> rest = new HashSet<>();

                  for (BlockPos c : this.push_Set) {
                     if (!mc.level.getBlockState(c).isAir()) {
                        rest.add(c);
                     }
                  }

                  AiDigLine.Inner_DataUtil again = AiDigLine.init_EventBus_DataUtil_Vec3(
                     from, mc.player.getYRot(), mc.player.getXRot(), List.of(cell), rest, this.init_EventBus_CachePool(), reach
                  );
                  if (again != null && again.chain().get(0).equals(cell)) {
                     this.init_EventBus_DataUtil = again;
                     if (!this.write_List.isEmpty() && this.write_List.get(0).chain().contains(cell)) {
                        this.write_List.set(0, again);
                     } else {
                        this.write_List.add(0, again);
                     }

                     led = cell;
                  }
               }

               if (led == null) {
                  return null;
               }

               Vec3 eye = mc.player.getEyePosition();
               BlockHitResult hit = AiDigLine.init(eye, this.init_EventBus_DataUtil.direction(), reach, Set.of(), this.init_EventBus_CachePool());
               if (hit != null && hit.getBlockPos().equals(cell)) {
                  return hit.getLocation().add(this.init_EventBus_DataUtil.direction().scale(0.02));
               }

               Set<BlockPos> rest = new HashSet<>();

               for (BlockPos c : this.push_Set) {
                  if (!mc.level.getBlockState(c).isAir()) {
                     rest.add(c);
                  }
               }

               rest.add(cell);
               AiDigLine.Inner_DataUtil here = AiDigLine.init_EventBus_DataUtil_Vec3(
                  eye, mc.player.getYRot(), mc.player.getXRot(), List.of(cell), rest, this.init_EventBus_CachePool(), reach
               );
               if (here != null && here.chain().get(0).equals(cell) && !this.read_RefQueue_DataUtil.init_Z_BlockPos(cell)) {
                  this.init_EventBus_DataUtil = here;
                  if (!this.write_List.isEmpty() && this.write_List.get(0).chain().contains(cell)) {
                     this.write_List.set(0, here);
                  } else {
                     this.write_List.add(0, here);
                  }

                  return AiDigLine.init(here, eye, this.init_EventBus_CachePool(), reach);
               } else {
                  return null;
               }
            }

            Vec3 init_Vec3_BlockPos(BlockPos end, List<BlockPos> blocks, boolean trees) {
               if (end.equals(this.poll_BlockPos) && blocks.equals(this.update_List) && mc.player.tickCount - this.clear < 2) {
                  return this.init_Vec3;
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
                  List<AiDigLine.Inner_DataUtil> lines = AiDigLine.init_List_Vec3(
                     eye, mc.player.getYRot(), mc.player.getXRot(), beside, beside, this.init_EventBus_CachePool(), reach, 1, this.init_ToDoubleFunction()
                  );
                  if (!lines.isEmpty()) {
                     point = AiDigLine.init(lines.get(0), eye, this.init_EventBus_CachePool(), reach);
                  }
               }

               this.poll_BlockPos = end.immutable();
               this.update_List = List.copyOf(blocks);
               this.clear = mc.player.tickCount;
               this.init_Vec3 = point;
               return point;
            }

            boolean update_Z_3() {
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

            Vec3 init_Vec3_6() {
               Vec3 eye = mc.player.getEyePosition();
               Vec3 drift = mc.player.getDeltaMovement();
               return mc.player.onGround() && drift.horizontalDistanceSqr() > 1.0E-4
                  ? eye.add(drift.x * 2.2026431718061676, 0.0, drift.z * 2.2026431718061676)
                  : eye;
            }

            BlockPos load_BlockPos_2() {
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.2;
               List<BlockPos> candidates = new ArrayList<>(this.init_List_5());
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos nearby : this.init_List) {
                  if (!candidates.contains(nearby)
                     && !this.init_Set.contains(nearby.asLong())
                     && this.write_Z_2(nearby)
                     && (!this.init_FieldCache.init_Z() || this.init_FieldCache.init(nearby.asLong()))
                     && (!treeMode || this.write_Set.isEmpty() || this.write_Set.contains(nearby.asLong()))
                     && AiBlockMine.init(feet, nearby, treeMode && this.write_Set.contains(nearby.asLong()))) {
                     candidates.add(nearby);
                  }
               }

               BlockPos over = mc.player.blockPosition().above(2);
               RotationMath.Rotation facing = RotationMath.ofPlayer(mc.player);
               BlockPos best = null;
               this.push_BlockPos = null;
               double bestScore = 1.7976931348623157E308;
               List<BlockPos> direct = new ArrayList<>();
               List<BlockPos> hidden = new ArrayList<>();
               boolean moving = this.init_HashUtil_CachePool != null
                  && !this.init_HashUtil_CachePool.init_Z_13()
                  && mc.player.getDeltaMovement().horizontalDistanceSqr() > 0.003;
               Vec3 drift = mc.player.getDeltaMovement();
               Vec3 rest = drift.horizontalDistanceSqr() > 1.0E-4 ? eye.add(drift.x * 2.2026431718061676, 0.0, drift.z * 2.2026431718061676) : null;
               Set<BlockPos> routeCells = this.init_HashUtil_CachePool != null && !this.init_HashUtil_CachePool.init_Z_13()
                  ? this.init_HashUtil_CachePool.init_Set_4()
                  : Set.of();
               BlockPos end = moving ? this.init_HashUtil_CachePool.init_BlockPos_6() : null;

               for (BlockPos t : candidates) {
                  if (!routeCells.contains(t)
                     && (end == null || end.equals(feet) || !AiBlockMine.init(end, t, treeMode && this.write_Set.contains(t.asLong())))
                     && AiBlockMine.init(feet, t, treeMode && this.write_Set.contains(t.asLong()))) {
                     if (moving) {
                        float yaw = RotationMath.lookAt(Vec3.atCenterOf(t), eye).yaw();
                        if (Math.abs(Mth.wrapDegrees(yaw - facing.yaw())) > 70.0F) {
                           continue;
                        }
                     }

                     if (!t.equals(over) || !this.update(t)) {
                        BlockPos in = AiBlockInteraction.findReachableHit(mc, t) != null && (rest == null || AiBlockInteraction.findReachableHitFrom(mc, t, rest) != null)
                           ? t
                           : this.init_BlockPos_Vec3(eye, t, reach);
                        if (in == null && !this.init_Set.contains(t.asLong())) {
                           hidden.add(t);
                        }

                        if (in != null && (in.equals(t) || !this.init_Set.contains(in.asLong()))) {
                           if (in.equals(t)) {
                              direct.add(t);
                           }

                           double score = AiBlockMine.init(facing, eye, t);
                           if (score < bestScore) {
                              bestScore = score;
                              best = in;
                              this.push_BlockPos = in.equals(t) ? null : t;
                           }
                        }
                     }
                  }
               }

               if (!direct.isEmpty()) {
                  BlockPos base = treeMode && held > 0 ? AiBlockMine.init(direct, candidates, held - this.load_I_6()) : null;
                  if (base != null) {
                     this.init_EventBus_DataUtil = null;
                     this.write_List.clear();
                     this.poll_Set = Set.of();
                     this.pop_BlockPos = null;
                     this.load_Vec3 = null;
                     return base;
                  }

                  if (treeMode && held > 0 && this.pop_I > 0) {
                     BlockPos support = null;
                     double distance = 1.7976931348623157E308;

                     for (Entity entity : mc.level.entitiesForRendering()) {
                        if (entity instanceof ItemEntity item && item.isAlive() && item.onGround() && this.init(item.getItem().getItem())) {
                           BlockPos below = BlockPos.containing(item.getX(), item.getY() - 0.02, item.getZ());
                           if (direct.contains(below) && this.write_Z_2(below)) {
                              double d = below.distToCenterSqr(eye);
                              if (d < distance) {
                                 distance = d;
                                 support = below;
                              }
                           }
                        }
                     }

                     if (support != null) {
                        this.init_EventBus_DataUtil = null;
                        this.write_List.clear();
                        this.poll_Set = Set.of();
                        this.pop_BlockPos = null;
                        this.load_Vec3 = null;
                        return support;
                     }
                  }

                  double lineReach = mc.player.blockInteractionRange();
                  Set<BlockPos> cells = new LinkedHashSet<>(direct);
                  cells.addAll(hidden);
                  Vec3 from = this.init_Vec3_6();
                  this.init_V_Vec3(from, direct, cells, lineReach);
                  this.init_EventBus_DataUtil = this.write_List.isEmpty() ? null : this.write_List.get(0);
                  this.poll_Set = cells;
                  BlockPos led = this.init_EventBus_DataUtil == null
                     ? null
                     : AiDigLine.init(this.init_EventBus_DataUtil, from, cells, this.init_EventBus_CachePool(), lineReach);
                  if (led != null && direct.contains(led)) {
                     return led;
                  }
               }

               this.pop_BlockPos = null;
               this.load_Vec3 = null;
               if (best != null && this.push_BlockPos != null) {
                  Vec3 to = Vec3.atCenterOf(this.push_BlockPos);
                  BlockHitResult hit = mc.level.clip(new ClipContext(eye, to, net.minecraft.world.level.ClipContext.Block.OUTLINE, Fluid.NONE, mc.player));
                  if (hit.getType() == Type.BLOCK && hit.getBlockPos().equals(best)) {
                     this.pop_BlockPos = best;
                     this.load_Vec3 = hit.getLocation().add(to.subtract(eye).normalize().scale(0.05));
                  }
               }

               return best;
            }

            String update_Str() {
               Vec3 eye = mc.player.getEyePosition();
               double reach = mc.player.blockInteractionRange() - 0.2;
               BlockPos feet = mc.player.blockPosition();
               StringBuilder out = new StringBuilder(
                  "ground=" + mc.player.onGround() + " pause=" + (this.init_HashUtil_CachePool == null || this.init_HashUtil_CachePool.load_Z_8())
               );
               int n = 0;

               for (BlockPos t : this.init_List_5()) {
                  if (AiBlockMine.init(feet, t, treeMode && this.write_Set.contains(t.asLong()))) {
                     if (n++ >= 4) {
                        break;
                     }

                     out.append(" | ").append(t.toShortString()).append(' ').append(mc.level.getBlockState(t).getBlock().getName().getString());
                     if (AiBlockInteraction.findReachableHit(mc, t) == null) {
                        out.append(" unseen");
                     }

                     BlockPos in = this.init_BlockPos_Vec3(eye, t, reach);
                     if (in != null) {
                        out.append(" behind ").append(in.toShortString());
                     }

                     if (this.init_Set.contains(t.asLong())) {
                        out.append(" blacklisted");
                     }
                  }
               }

               if (n == 0) {
                  out.append(" | no goal block beside the feet (").append(this.init_List_5().size()).append(" in the goal)");
               }

               return out.toString();
            }

            Vec3 write_Vec3(BlockPos cell) {
               Vec3 along = this.read_Vec3(cell);
               if (along != null) {
                  return along;
               } else {
                  return cell.equals(this.pop_BlockPos) ? this.load_Vec3 : null;
               }
            }

            BlockPos init_BlockPos_Vec3(Vec3 eye, BlockPos target, double reach) {
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

            void update_V_3() {
               if (!treeMode && !oreJob && !dropsOnly && !approach) {
                  BlockPos feet = mc.player.blockPosition();
                  Set<Long> known = this.init_Set_3();

                  for (int dx = -2; dx <= 2; dx++) {
                     for (int dz = -2; dz <= 2; dz++) {
                        for (int dy = 0; dy <= 2; dy++) {
                           BlockPos cell = feet.offset(dx, dy, dz);
                           long key = cell.asLong();
                           if (!cell.equals(feet)
                              && !cell.equals(feet.above())
                              && !this.init_Set.contains(key)
                              && this.write_Z_2(cell)
                              && (!known.contains(key) || !this.init_FieldCache.load(key) && (!this.init_FieldCache.load_Z() || !this.close_Set.contains(key)))
                              && this.sync(cell)) {
                              if (known.add(key)) {
                                 this.init_List.add(cell);
                              }

                              if (this.init_FieldCache.load_Z()) {
                                 this.init_V_Set(this.close_Set, cell, feet);
                              }
                           }
                        }
                     }
                  }

                  if (this.init_FieldCache.init_Z()) {
                     this.read_V_5();
                  }
               }
            }

            void init_V_Set(Set<Long> batch, BlockPos cell, BlockPos feet) {
               if (!batch.contains(cell.asLong())) {
                  if (batch.size() >= 64) {
                     Long farthest = null;
                     double cost = this.init_D(cell, feet);

                     for (Long key : batch) {
                        double remote = this.init_D(BlockPos.of(key), feet);
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

            void sync_V_2() {
               if (!this.close_Z) {
                  if (this.open_Z && this.flush_I % 5 != 0) {
                     return;
                  }

                  this.close_Z = true;
                  this.read_List = new ArrayList<>();
                  this.add = 0;
                  this.open_BlockPos = mc.player.blockPosition();
                  long key = this.open_BlockPos.getX() >> 4 & 1048575L
                     | (this.open_BlockPos.getZ() >> 4 & 1048575L) << 20
                     | (this.open_BlockPos.getY() >> 4 & 4095L) << 40
                     | (long)this.put << 52;
                  if (this.init_IArr2 == null || key != this.sync_J) {
                     this.init_IArr2 = AiBlockMine.init(
                        this.open_BlockPos.getX(),
                        this.open_BlockPos.getY(),
                        this.open_BlockPos.getZ(),
                        this.put + 15 >> 4,
                        mc.level.getMinSectionY(),
                        mc.level.getSectionsCount(),
                        oreJob
                     );
                     this.sync_J = key;
                  }
               }

               long until = System.nanoTime() + 1500000L;
               MutableBlockPos p = new MutableBlockPos();

               while (this.add < this.init_IArr2.length && this.read_List.size() < 256 && System.nanoTime() <= until) {
                  int[] unit = this.init_IArr2[this.add++];
                  int cx = (this.open_BlockPos.getX() >> 4) + unit[0];
                  int cz = (this.open_BlockPos.getZ() >> 4) + unit[1];
                  int si = unit[2];
                  if (mc.level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false) instanceof LevelChunk chunk) {
                     LevelChunkSection[] sections = chunk.getSections();
                     LevelChunkSection section = si < sections.length ? sections[si] : null;
                     if (section != null && !section.hasOnlyAir() && section.maybeHas(this::init)) {
                        int baseY = mc.level.getSectionYFromSectionIndex(si) << 4;
                        List<BlockPos> sectionHits = new ArrayList<>();

                        for (int i = 0; i < 4096; i++) {
                           int x = i & 15;
                           int z = i >> 4 & 15;
                           int y = i >> 8;
                           if (this.init(section.getBlockState(x, y, z))) {
                              p.set((cx << 4) + x, baseY + y, (cz << 4) + z);
                              if (!this.init_Set.contains(p.asLong()) && this.init_Z(p, this.open_BlockPos)) {
                                 sectionHits.add(p.immutable());
                              }
                           }
                        }

                        sectionHits.sort(Comparator.comparingDouble(cellx -> cellx.distSqr(this.open_BlockPos)));

                        for (BlockPos cell : sectionHits) {
                           if (this.read_List.size() >= 256) {
                              break;
                           }

                           if (this.sync(cell)) {
                              if (quota != null) {
                                 this.update_Map.put(cell.asLong(), BuiltInRegistries.BLOCK.getKey(mc.level.getBlockState(cell).getBlock()).toString());
                              }

                              this.read_List.add(cell);
                           }
                        }
                     }
                  }
               }

               BlockPos feet = mc.player.blockPosition();
               Set<Long> have = new HashSet<>();

               for (BlockPos hit : this.read_List) {
                  have.add(hit.asLong());
               }

               List<BlockPos> merged = new ArrayList<>(this.read_List);

               for (BlockPos old : this.init_List) {
                  if (have.add(old.asLong()) && this.init_Z(old, feet)) {
                     merged.add(old);
                  }
               }

               merged.sort(Comparator.comparingDouble(b -> this.init_D(b, feet)));
               this.init_List = merged.size() > 1024 ? new ArrayList<>(merged.subList(0, 1024)) : merged;
               this.read_V_5();
               if (this.add >= this.init_IArr2.length || this.read_List.size() >= 256) {
                  this.close_Z = false;
                  this.open_Z = true;
               }
            }

            boolean init_Z(BlockPos p, BlockPos centre) {
               double dx = p.getX() - centre.getX();
               double dz = p.getZ() - centre.getZ();
               return dx * dx + dz * dz <= (double)this.put * this.put;
            }

            BiPredicate<BlockState, ItemStack> init_BiPredicate() {
               Set<Block> sources = AiBlockMine.init_Set(wantedDrops, block -> this.init_Z_3(BuiltInRegistries.BLOCK.getKey(block).toString()));
               BiPredicate<BlockState, ItemStack> wanted = AiItems.dropFilterFor(mc, wantedDrops);
               return (state, tool) -> !sources.contains(state.getBlock()) || wanted.test(state, tool);
            }

            boolean init(BlockState state) {
               return this.init_Map.computeIfAbsent(state.getBlock(), this.init_Predicate::test)
                  && (quota == null || quota.init_Z_Str(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()));
            }

            boolean init_Z_3(String id) {
               return blockIds.contains(id) ? true : !blockArg.isEmpty() && AiMine.init(blockArg, id);
            }

            boolean write_Z_2(BlockPos pos) {
               if (mc.level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false) == null) {
                  return false;
               }

               BlockState state = mc.level.getBlockState(pos);
               boolean wanted = this.init(state);
               if (wanted && quota != null) {
                  this.update_Map.put(pos.asLong(), BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
               }

               return wanted;
            }

            boolean update(BlockPos cell) {
               return mc.level.getBlockState(cell.above()).getBlock() instanceof FallingBlock;
            }

            boolean sync(BlockPos pos) {
               if (AiEditLedger.init_Z(pos)) {
                  return false;
               }

               if (AiPathfinder.LiveGridView.init_Z_BlockGetter_BlockPos(mc.level, pos)) {
                  return false;
               }

               if (treeMode && this.open(pos) && !this.close()) {
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

                  if (AiBlockMine.init_I(pos) >= 2 && AiBlockMine.init(mc, pos)) {
                     return false;
                  }

                  for (Direction side : Direction.values()) {
                     if (mc.level.getFluidState(pos.relative(side)).is(FluidTags.LAVA)) {
                        if (obsidianJob) {
                           this.load_Set.add(pos.asLong());
                        }

                        return false;
                     }
                  }

                  return this.init_BlockPos_4(pos) == null || this.init_Item_2() != null;
               } else {
                  return false;
               }
            }

            BlockPos read_BlockPos() {
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

            long[] init_JArr_BlockPos(BlockPos t, boolean trunk, int reachAbove) {
               if (this.flush_I - this.data > 20) {
                  this.push_Map.clear();
                  this.data = this.flush_I;
               }

               long key = t.asLong() * 31L + (trunk ? 1 : 0);
               long[] cached = this.push_Map.get(key);
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
                                 visible = this.init_Z_I(x, z, y + 2, t.getY()) || dy == 2 && this.flush(t.below());
                              } else {
                                 visible = (this.flush(new BlockPos(x, t.getY(), t.getZ())) || this.flush(new BlockPos(t.getX(), t.getY(), z)))
                                    && (dy <= 1 || this.init_Z_I(x, z, y + 2, t.getY()));
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
               this.push_Map.put(key, cells);
               return cells;
            }

            boolean init_Z_I(int x, int z, int fromY, int toY) {
               for (int y = fromY; y <= toY; y++) {
                  if (!this.flush(new BlockPos(x, y, z))) {
                     return false;
                  }
               }

               return true;
            }

            boolean flush(BlockPos p) {
               BlockState state = mc.level.getBlockState(p);
               return !state.isAir() && !state.is(BlockTags.LEAVES) ? state.getFluidState().isEmpty() && state.getCollisionShape(mc.level, p).isEmpty() : true;
            }

            void init(BlockPos log, Set<Long> out) {
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

            boolean sync_Z_2() {
               BlockPos feet = mc.player.blockPosition();

               for (int dx = -2; dx <= 2; dx++) {
                  for (int dz = -2; dz <= 2; dz++) {
                     for (int dy = 5; dy <= 8; dy++) {
                        BlockPos p = feet.offset(dx, dy, dz);
                        if (this.write_Z_2(p) && !this.init_Set.contains(p.asLong()) && this.open(p) && !this.close(p)) {
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

            BlockPos write_BlockPos() {
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
                                 && !AiEditLedger.init_Z(p)
                                 && AiBlockInteraction.findReachableHit(mc, p) != null
                                 && !this.init_Set.contains(p.asLong())
                                 && AiPathfinder.LiveGridView.init_Z_ClientLevel_BlockPos(mc.level, p)
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

            boolean flush_Z() {
               return this.write_BlockPos != null && System.nanoTime() - this.init_J < 5000000000L;
            }

            Vec3 load_Vec3() {
               for (Entity entity : mc.level.entitiesForRendering()) {
                  if (entity instanceof ItemEntity item
                     && item.isAlive()
                     && item.getItem().is(Items.DIRT)
                     && item.position().distanceToSqr(Vec3.atCenterOf(this.write_BlockPos)) <= 9.0) {
                     return this.init(item);
                  }
               }

               return null;
            }

            boolean close() {
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

            boolean poll(BlockPos pos) {
               long now = System.nanoTime();
               long[] seen = this.poll_Map.get(pos.asLong());
               if (seen != null && now - seen[1] < 5000000000L) {
                  return seen[0] != 0L;
               }

               boolean wet = this.init_BlockPos_4(pos) != null;
               this.poll_Map.put(pos.asLong(), new long[]{wet ? 1L : 0L, now});
               return wet;
            }

            BlockPos init_BlockPos_4(BlockPos pos) {
               for (Direction side : new Direction[]{Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                  BlockPos n = pos.relative(side);
                  if (mc.level.getFluidState(n).is(FluidTags.WATER)) {
                     return n;
                  }
               }

               return null;
            }

            Item init_Item_2() {
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

            JsonObject init_JsonObject_25() {
               JsonObject out = AiJson.objectOf(
                  "block",
                  label,
                  "broken",
                  this.update_I,
                  "held",
                  this.load_I_6(),
                  "gained",
                  this.load_I_6() - this.load_I,
                  "known",
                  this.init_List.size(),
                  "failures",
                  this.write_I,
                  "trees",
                  this.sync_I,
                  "exploring",
                  this.load_Z
               );
               if (quota != null) {
                  out.add("brokenByBlock", quota.init_JsonObject());
                  out.add("remainingByBlock", quota.load());
               }

               if (!this.write_Set.isEmpty()) {
                  out.addProperty("treeLogsLeft", this.write_Set.size());
               }

               return out;
            }

            AiOperation.Result load_AiOperation_Result_2(BlockPos target) {
               Item needed = AiItems.cheapestRequiredTool(mc.level.getBlockState(target));
               JsonObject out = this.init_JsonObject_25();
               out.addProperty("targetX", target.getX());
               out.addProperty("targetY", target.getY());
               out.addProperty("targetZ", target.getZ());
               if (needed != null) {
                  out.addProperty("neededTool", BuiltInRegistries.ITEM.getKey(needed).toString());
               }

               this.flush_V();
               return AiOperation.Result.failed(
                  AiFailure.MISSING_TOOL, "No usable harvesting tool for " + mc.level.getBlockState(target).getBlock().getName().getString(), out.toString()
               );
            }

            AiOperation.Result init_AiOperation_Result_6(String message) {
               return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_25().toString(), List.of());
            }

            AiOperation.Result init(AiOperation.Status status, String message) {
               this.flush_V();
               return new AiOperation.Result(status, message, this.init_JsonObject_25().toString(), List.of());
            }

            void flush_V() {
               if (this.init_AiOperation != null) {
                  this.sync_Z = this.sync_Z | this.init_AiOperation.requiresReconciliationAfterEmergency();
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               if (!AutismLiteVariant.enabled()) {
                  AiVisualization.init_V(this);
               }

               if (this.poll_Z) {
                  this.poll_Z = false;
                  mc.options.keyUp.setDown(false);
                  mc.options.keyDown.setDown(false);
                  mc.options.keyLeft.setDown(false);
                  mc.options.keyRight.setDown(false);
               }

               if (this.init_HashUtil_CachePool != null) {
                  this.init_HashUtil_CachePool.cancel();
                  this.init_HashUtil_CachePool = null;
               }

               if (this.load_AiOperation != null) {
                  this.load_AiOperation.cancel();
                  this.load_AiOperation = null;
               }

               this.init_RefQueue_DataUtil.init_V();
               this.load_RefQueue_DataUtil.init_V();
               this.init_DataPipe.reset();
               this.update_BlockPos = null;
               this.init_DataUtil.clear();
               AiMotionSafety.releaseMovementKeys();
            }

            @Override
            public void cancel() {
               if (!this.read_Z) {
                  this.read_Z = true;
                  this.flush_V();
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Mining requires a connected living player");
      }
   }

   public static synchronized void init_V() {
      init_JArr[0] = init_JArr[1];
      init_JArr[1] = System.nanoTime();
   }

   static synchronized boolean init_Z() {
      long now = System.nanoTime();
      return init_JArr[0] != 0L && init_JArr[1] - init_JArr[0] < 180000000000L && now - init_JArr[1] < 300000000000L;
   }

   static synchronized int init_I(BlockPos pos) {
      long[] strike = init_Map.get(pos.asLong());
      if (strike == null) {
         return 0;
      } else if (System.nanoTime() - strike[1] > 600000000000L) {
         init_Map.remove(pos.asLong());
         return 0;
      } else {
         return (int)strike[0];
      }
   }

   static synchronized void init_V(BlockPos pos) {
      long[] strike = init_Map.computeIfAbsent(pos.asLong(), key -> new long[2]);
      strike[0]++;
      strike[1] = System.nanoTime();
      if (init_Map.size() > 256) {
         init_Map.entrySet().removeIf(e -> System.nanoTime() - e.getValue()[1] > 600000000000L);
      }
   }

   static double init(Minecraft mc, BlockPos pos, boolean requireDrops) {
      int depth = 0;
      int buried = 0;

      for (BlockPos cell = pos.above(); depth + buried < 24; cell = cell.above()) {
         if (load(mc, cell)) {
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

      double dryTicks = MiningCost.init_D_Minecraft(mc, mc.player.getInventory().getItem(slot), state, state.getDestroySpeed(mc.level, pos), MiningCost.Inner_DataUtil.of(mc));
      double submergedSpeed = mc.player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
      double slowdown = submergedSpeed > 0.0 && submergedSpeed < 1.0 ? 1.0 / submergedSpeed : 1.0;
      return swim + dryTicks / 20.0 * (slowdown - 1.0);
   }

   static List<BlockPos> init_List_List(List<BlockPos> targets, BlockPos feet, Predicate<BlockPos> submerged, ToDoubleFunction<BlockPos> dive) {
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
            if (!load(mc, pos.above())) {
               for (int dy = 2; dy <= 16; dy++) {
                  BlockPos cell = pos.above(dy);
                  if (load(mc, cell)) {
                     return true;
                  }

                  if (mc.level.getBlockState(cell).getCollisionShape(mc.level, cell).isEmpty()) {
                     return false;
                  }
               }

               return false;
            } else {
               if (load(mc, pos.above(2))) {
                  return true;
               }

               int sides = 0;

               for (Direction side : Plane.HORIZONTAL) {
                  if (load(mc, pos.relative(side))) {
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

   private static boolean load(Minecraft mc, BlockPos pos) {
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
      final BlockPos init_BlockPos;
      final int init_I;
      final BlockPos load;

      Inner_BufferHelper(BlockPos origin, int level) {
         this(origin, level, null);
      }

      Inner_BufferHelper(BlockPos origin, int level, BlockPos toward) {
         this.init_BlockPos = origin;
         this.init_I = level;
         this.load = toward;
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return false;
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double away = this.load != null
            ? 1.92 * Math.hypot(x - this.load.getX(), z - this.load.getZ())
            : -1.92 * Math.hypot(x - this.init_BlockPos.getX(), z - this.init_BlockPos.getZ());
         return this.init_I == -2147483648 ? away : away + Math.abs(y - this.init_I) * 6.0;
      }
   }

   static final class Inner_CachePool {
      final Map<Long, Set<UUID>> init_Map = new HashMap<>();
      final Set<UUID> init_Set = new HashSet<>();

      void init(long cell, Collection<UUID> present) {
         this.init_Map.put(cell, Set.copyOf(present));
      }

      Long init(Collection<Long> pending, UUID entity, Vec3 position, int age) {
         if (this.init_Set.contains(entity)) {
            return null;
         }

         List<Long> eligible = new ArrayList<>();

         for (Long cell : pending) {
            Set<UUID> before = this.init_Map.get(cell);
            if (before != null && !before.contains(entity)) {
               eligible.add(cell);
            }
         }

         Long cell = AiBlockMine.init(eligible, position, age);
         if (cell != null) {
            this.init_Set.add(entity);
            this.init_Map.remove(cell);
         }

         return cell;
      }
   }

   static final class Inner_ConfigStore {
      final Map<Long, Integer> init = new HashMap<>();

      boolean init(BlockPos end, int remaining) {
         if (end != null && remaining >= 0) {
            Integer best = this.init.get(end.asLong());
            if (best == null || remaining < best) {
               this.init.put(end.asLong(), remaining);
            }

            return best != null && remaining < best;
         } else {
            return false;
         }
      }

      void init() {
         this.init.clear();
      }
   }

   static final class Inner_DataUtil implements AiPathfinder.PathGoal {
      final Set<Long> init_Set = new HashSet<>();
      final List<BlockPos> init_List;

      Inner_DataUtil(List<BlockPos> targets) {
         this.init_List = List.copyOf(targets);

         for (BlockPos p : targets) {
            this.init_Set.add(p.asLong());
         }
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         return this.init_Set.contains(BlockPos.asLong(x, y, z)) || this.init_Set.contains(BlockPos.asLong(x, y + 1, z));
      }

      @Override
      public double heuristic(int x, int y, int z) {
         double best = 1.7976931348623157E308;

         for (BlockPos p : this.init_List) {
            int dy = p.getY() - y;
            if (dy == 1) {
               dy = 0;
            }

            best = Math.min(best, AiPathfinder.MoveCosts.init_D_I(p.getX() - x, dy, p.getZ() - z));
         }

         return best;
      }
   }

   static final class Inner_StreamUtil implements AiPathfinder.PathGoal {
      final int init_I;
      final BlockPos init_BlockPos;
      final double init_D;
      Set<Long> init_Set = Set.of();

      Inner_StreamUtil(int y, BlockPos toward) {
         this(y, toward, 1.7976931348623157E308);
      }

      Inner_StreamUtil(int y, BlockPos toward, double maxAcross) {
         this.init_I = y;
         this.init_BlockPos = toward;
         this.init_D = maxAcross;
      }

      @Override
      public boolean load(BlockPos cell) {
         return !this.init_Set.contains(cell.asLong());
      }

      static AiBlockMine.Inner_StreamUtil init_BufferHelper_Strea_BlockPos_BlockPos(BlockPos feet, BlockPos target) {
         double across = Math.hypot(feet.getX() - target.getX(), feet.getZ() - target.getZ());
         return new AiBlockMine.Inner_StreamUtil(Math.max(target.getY(), feet.getY() - 8), target, Math.max(3.0, across - 8.0));
      }

      boolean init_Z_BlockPos_BlockPos(BlockPos feet, BlockPos target) {
         return feet.getY() > this.init_I && this.init_BlockPos.distSqr(target) <= 36.0;
      }

      @Override
      public boolean isInGoal(int x, int yy, int z) {
         return yy <= this.init_I;
      }

      @Override
      public double heuristic(int x, int yy, int z) {
         double across = Math.hypot(x - this.init_BlockPos.getX(), z - this.init_BlockPos.getZ());
         double shortfall = this.init_D == 1.7976931348623157E308 ? 0.0 : Math.max(0.0, across - this.init_D);
         return Math.max(0, yy - this.init_I) * 20.0 + shortfall * 20.0 + across;
      }

      @Override
      public String toString() {
         return "descent(y<=" + this.init_I + " toward " + this.init_BlockPos.toShortString() + ")";
      }
   }

   static final class Inner_TaskQueue implements AiPathfinder.PathGoal {
      final Set<Long> init_Set = new HashSet<>();
      final Set<Long> load_Set;
      final List<BlockPos> init_List;
      final List<Vec3> load_List;
      final List<BlockPos> read_List;
      final Set<Long> read_Set = new HashSet<>();
      Set<Long> write = Set.of();
      int init_I = 2;
      final Set<Long> update = new HashSet<>();

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items) {
         this(blocks, items, Set.of());
      }

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items, Set<Long> noShaft) {
         this(blocks, items, noShaft, Set.of());
      }

      Inner_TaskQueue(List<BlockPos> blocks, List<Vec3> items, Set<Long> noShaft, Set<Long> wet) {
         this.init_List = List.copyOf(blocks);
         this.load_List = List.copyOf(items);
         this.load_Set = Set.copyOf(noShaft);
         List<BlockPos> beside = new ArrayList<>();

         for (BlockPos p : blocks) {
            if (wet.contains(p.asLong())) {
               beside.add(p);
            } else {
               this.init_Set.add(p.asLong());
            }
         }

         this.read_List = List.copyOf(beside);
      }

      @Override
      public boolean isInGoal(int x, int y, int z) {
         for (Vec3 item : this.load_List) {
            if (AiBlockMine.init_Z_I(x, y, z, item)) {
               return true;
            }
         }

         if (this.read_Set.contains(BlockPos.asLong(x, y, z))) {
            return false;
         }

         if (!this.read_List.isEmpty()) {
            BlockPos feet = new BlockPos(x, y, z);

            for (BlockPos t : this.read_List) {
               if (AiBlockMine.init(feet, t, false) && t.getY() - y <= 3) {
                  return true;
               }
            }
         }

         if (!this.init_Set.contains(BlockPos.asLong(x, y, z)) && !this.init_Set.contains(BlockPos.asLong(x, y + 1, z))) {
            long over = BlockPos.asLong(x, y + 2, z);
            return this.init_Set.contains(over) && !this.load_Set.contains(over) ? true : this.update.contains(BlockPos.asLong(x, y, z));
         } else {
            return true;
         }
      }

      @Override
      public boolean init_Z_I(int x, int y, int z, AiPathfinder.GridView planned) {
         if (!this.isInGoal(x, y, z)) {
            return false;
         }

         if (planned != null && planned.init_ClassCache_FileCac_I(x, y, z).water()) {
            for (Vec3 item : this.load_List) {
               if (AiBlockMine.init_Z_I(x, y, z, item)) {
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

         for (BlockPos p : this.init_List) {
            int dy = p.getY() - y;
            dy = dy >= 0 && dy <= this.init_I ? 0 : (dy > this.init_I ? dy - this.init_I : dy);
            best = Math.min(best, AiPathfinder.MoveCosts.init_D_I(p.getX() - x, dy, p.getZ() - z));
         }

         for (Vec3 item : this.load_List) {
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
         return this.init_Set.contains(key) && !this.write.contains(key) && !this.load_Set.contains(key);
      }

      @Override
      public boolean load(BlockPos cell) {
         return !this.write.contains(cell.asLong());
      }

      boolean init(AiBlockMine.Inner_TaskQueue other) {
         if (other != null && this.init_Set.equals(other.init_Set) && this.read_Set.equals(other.read_Set) && this.load_List.size() == other.load_List.size()) {
            for (int i = 0; i < this.load_List.size(); i++) {
               if (this.load_List.get(i).distanceToSqr(other.load_List.get(i)) > 0.25) {
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
