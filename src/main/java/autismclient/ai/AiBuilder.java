package autismclient.ai;

import autismclient.AutismClientAddon;
import autismclient.ai.engine.AiGoto;
import autismclient.ai.engine.AiFallGuard;
import autismclient.ai.engine.AiScaffold;
import autismclient.ai.engine.AiSchematicApproach;
import autismclient.api.ai.AiOperation;
import autismclient.mixin.accessor.AutismClientLevelPredictionAccessor;
import autismclient.util.AutismLiteVariant;
import autismclient.util.BlockPlacementPlanner;
import autismclient.util.macro.MacroItemTarget;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CalibratedSculkSensorBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.InfestedRotatedPillarBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.RedstoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.SculkCatalystBlock;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SegmentableBlock;
import net.minecraft.world.level.block.ShelfBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.SnifferEggBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TargetBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.WeightedPressurePlateBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class AiBuilder {
   static final int MAX_BLUEPRINT_CELLS = 4096;
   static final int MAX_SCHEMATIC_CELLS = 2097152;
   static final int MAX_OFFSET_COORD = 256;
   private static final int MAX_CELL_ATTEMPTS = 3;
   private static final int MAX_PLACEMENTS_PER_TICK = 4;
   private static final int MAX_CELLS_EVALUATED_PER_TICK = 64;
   private static final int SERVER_REFUSAL_TICKS = 4;
   private static final int VERIFY_TIMEOUT_TICKS = 100;
   private static final int VERIFY_STABLE_TICKS = 5;
   private static final int UNVERIFIED_WAIT_TICKS = 2;
   private static final int MAX_FAILED_REPORTED = 32;
   private static final int MAX_RECOVERY_ATTEMPTS = 6;
   private static final int MAX_INTERACT_ATTEMPTS = 6;
   private static final int MAX_DEFERRAL_PASSES = 12;
   private static final int MAX_AUDIT_PASSES = 2;
   private static final int NEARBY_LOOKAHEAD_WINDOW = 96;
   private static final int SUB_OP_TIMEOUT_SECONDS = 30;
   private static final Set<String> REPLACEABLE_FOLIAGE_BLOCKS = Set.of(
      "minecraft:short_grass", "minecraft:tall_grass", "minecraft:short_dry_grass", "minecraft:tall_dry_grass", "minecraft:fern", "minecraft:large_fern"
   );

   static Path checkpointPath(AiBuilder.Blueprint blueprint) {
      Minecraft mc = Minecraft.getInstance();
      List<AiBuilder.CellSpec> cells = blueprint.cells()
         .stream()
         .sorted(
            Comparator.comparingInt(AiBuilder.CellSpec::dy)
               .thenComparingInt(AiBuilder.CellSpec::dx)
               .thenComparingInt(AiBuilder.CellSpec::dz)
         )
         .toList();
      StringBuilder identity = new StringBuilder(blueprint.origin().toShortString());

      for (AiBuilder.CellSpec cell : cells) {
         identity.append('|')
            .append(cell.dx())
            .append(',')
            .append(cell.dy())
            .append(',')
            .append(cell.dz())
            .append(':')
            .append(
               Set.of("minecraft:dirt", "minecraft:grass_block", "minecraft:dirt_path", "minecraft:farmland").contains(cell.block()) ? "soil" : cell.block()
            );
      }

      String world = AiJson.sha256Hex(AiWorldIdentity.currentWorldKey(mc) + "/" + AiWorldIdentity.currentDimensionId(mc));
      return mc.gameDirectory.toPath().resolve("config/autism/ai/scaffold-jobs").resolve(world).resolve(AiJson.sha256Hex(identity.toString()) + ".json");
   }

   static int pickTopScaffoldTeardownIndex(List<BlockPos> cells, Vec3 worker, Predicate<BlockPos> reachable) {
      if (cells.isEmpty()) {
         throw new IllegalArgumentException("No scaffold remains");
      }

      int highest = cells.stream().mapToInt(Vec3i::getY).max().orElseThrow();
      int best = -1;
      boolean bestReachable = false;
      double bestDistance = 1.7976931348623157E308;

      for (int i = 0; i < cells.size(); i++) {
         BlockPos cell = cells.get(i);
         if (cell.getY() == highest) {
            boolean visible = reachable.test(cell);
            double distance = worker.distanceToSqr(Vec3.atCenterOf(cell));
            if (best < 0 || visible && !bestReachable || visible == bestReachable && distance < bestDistance) {
               best = i;
               bestReachable = visible;
               bestDistance = distance;
            }
         }
      }

      return best;
   }

   private AiBuilder() {
   }

   static List<BlockPos> filterCommittedInLookahead(
      List<BlockPos> committed, List<AiBuilder.CellSpec> order, int cursor, BlockPos origin, Map<Long, AiBuilder.CellSpec> wantedCells
   ) {
      Set<Long> unprocessed = new HashSet<>();

      for (int i = cursor; i < Math.min(order.size(), cursor + 192); i++) {
         AiBuilder.CellSpec cell = order.get(i);
         unprocessed.add(origin.offset(cell.dx(), cell.dy(), cell.dz()).asLong());
      }

      List<BlockPos> ready = new ArrayList<>(committed.size());

      for (BlockPos pos : committed) {
         if (unprocessed.contains(pos.asLong()) && wantedCells.containsKey(pos.asLong())) {
            ready.add(pos);
         }
      }

      return ready;
   }

   static boolean incrementAndCheckAttemptLimit(Map<Long, Integer> attempts, BlockPos pos, int maxAttempts) {
      long key = pos.asLong();
      int next = attempts.getOrDefault(key, 0) + 1;
      if (next >= maxAttempts) {
         attempts.remove(key);
         return true;
      } else {
         attempts.put(key, next);
         return false;
      }
   }

   static void raiseAttemptCount(Map<Long, Integer> attempts, BlockPos pos, int count) {
      attempts.computeIfPresent(pos.asLong(), (key, previous) -> Math.max(previous, count));
   }

   static int deferQueuedCell(
      List<AiBuilder.CellSpec> order,
      List<AiBuilder.CellSpec> deferred,
      List<String> reasons,
      int cursor,
      BlockPos origin,
      AiBuilder.CellSpec cell,
      BlockPos pos,
      String reason
   ) {
      for (int i = cursor; i < order.size(); i++) {
         AiBuilder.CellSpec candidate = order.get(i);
         if (candidate.equals(cell) && origin.offset(candidate.dx(), candidate.dy(), candidate.dz()).equals(pos)) {
            order.remove(i);
            deferred.add(cell);
            reasons.add(reason);
            return i;
         }
      }

      return -1;
   }

   static boolean isExpectedItemSelected(Item expected, ItemStack held, boolean serverMatches) {
      return serverMatches && held.is(expected);
   }

   static boolean matchesPlacementFace(BlockPos expectedSupport, Direction expectedFace, BlockPos actualSupport, Direction actualFace) {
      return expectedSupport.equals(actualSupport) && expectedFace == actualFace;
   }

   static AiBuilder.BuildOptions parseBuildOptions(JsonObject args) {
      int rotation = AiJson.getInt(args, "rotation", 0);
      if (rotation != 0 && rotation != 90 && rotation != 180 && rotation != 270) {
         throw new IllegalArgumentException("rotation must be 0, 90, 180 or 270 (clockwise seen from above), got " + rotation);
      }

      String mirror = AiJson.getString(args, "mirror", "none").trim().toLowerCase(Locale.ROOT);
      if (!mirror.equals("none") && !mirror.equals("x") && !mirror.equals("z")) {
         throw new IllegalArgumentException("mirror must be \"none\", \"x\" or \"z\", got \"" + mirror + "\"");
      }

      int layerStart = 0;
      int layerHeight = -1;
      boolean skipFailed = false;
      if (args.has("layers") && !args.get("layers").isJsonNull()) {
         if (!args.get("layers").isJsonObject()) {
            throw new IllegalArgumentException("layers must be a {start, height, skipFailed} object");
         }

         JsonObject layers = args.getAsJsonObject("layers");
         layerStart = AiJson.getInt(layers, "start", 0);
         layerHeight = AiJson.getInt(layers, "height", 1);
         if (layerHeight < 1 || layerHeight > 512) {
            throw new IllegalArgumentException("layers.height must be 1..512");
         }

         skipFailed = AiJson.getBoolean(layers, "skipFailed", false);
      }

      AiBuilder.RepeatSpec repeat = null;
      if (args.has("repeat") && !args.get("repeat").isJsonNull()) {
         if (!args.get("repeat").isJsonObject()) {
            throw new IllegalArgumentException("repeat must be a {dx,dy,dz, count} object");
         }

         JsonObject r = args.getAsJsonObject("repeat");
         int rdx = AiJson.getInt(r, "dx", 0);
         int rdy = AiJson.getInt(r, "dy", 0);
         int rdz = AiJson.getInt(r, "dz", 0);
         if (rdx == 0 && rdy == 0 && rdz == 0) {
            throw new IllegalArgumentException("repeat vector must not be (0,0,0); omit repeat for a single build");
         }

         if (Math.abs(rdx) > 256 || Math.abs(rdy) > 256 || Math.abs(rdz) > 256) {
            throw new IllegalArgumentException("repeat vector components must be within +/-256");
         }

         int count = AiJson.getInt(r, "count", -2);
         if (count == -2) {
            throw new IllegalArgumentException("repeat.count is required: extra copies after the first (-1 = until cancelled or materials out)");
         }

         if (count < -1 || count > 4096) {
            throw new IllegalArgumentException("repeat.count must be -1 or 0..4096");
         }

         repeat = new AiBuilder.RepeatSpec(rdx, rdy, rdz, count);
      }

      Map<String, List<String>> substitutes = parseSubstitutes(args);
      boolean clearTerrain = AiJson.getBoolean(args, "clearTerrain", false);
      boolean shulkerStock = AiJson.getBoolean(args, "shulkerStock", true);
      return new AiBuilder.BuildOptions(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock)
         .withAssumeFly(AiJson.getBoolean(args, "assumeFly", false));
   }

   static Map<String, List<String>> parseSubstitutes(JsonObject args) {
      if (args.has("substitutes") && !args.get("substitutes").isJsonNull()) {
         if (!args.get("substitutes").isJsonObject()) {
            throw new IllegalArgumentException("substitutes must be an object of {\"<block>\": [\"alt\", ...]}");
         }

         Map<String, List<String>> out = new LinkedHashMap<>();

         label54:
         for (Entry<String, JsonElement> entry : args.getAsJsonObject("substitutes").entrySet()) {
            if (out.size() >= 32) {
               throw new IllegalArgumentException("substitutes is limited to 32 blocks");
            }

            String key = entry.getKey();
            validateBlockId(key, "substitutes key");
            if (entry.getValue().isJsonArray() && !entry.getValue().getAsJsonArray().isEmpty()) {
               List<String> alts = new ArrayList<>();
               Iterator var6 = entry.getValue().getAsJsonArray().iterator();

               while (true) {
                  if (var6.hasNext()) {
                     JsonElement alt = (JsonElement)var6.next();
                     if (alts.size() >= 8) {
                        throw new IllegalArgumentException("substitutes." + key + " is limited to 8 alternatives");
                     }

                     if (alt.isJsonPrimitive() && alt.getAsJsonPrimitive().isString()) {
                        String altId = alt.getAsString();
                        validateBlockId(altId, "substitutes." + key);
                        if (altId.equals(key)) {
                           throw new IllegalArgumentException("substitutes." + key + " lists the block itself as an alternative");
                        }

                        if (alts.contains(altId)) {
                           throw new IllegalArgumentException("substitutes." + key + " repeats " + altId);
                        }

                        alts.add(altId);
                        continue;
                     }

                     throw new IllegalArgumentException("substitutes." + key + " entries must be block id strings");
                  }

                  out.put(key, List.copyOf(alts));
                  continue label54;
               }
            }

            throw new IllegalArgumentException("substitutes." + key + " must be a non-empty array of block ids");
         }

         return Map.copyOf(out);
      } else {
         return Map.of();
      }
   }

   private static void validateBlockId(String id, String context) {
      Identifier parsed = Identifier.tryParse(id);
      Block block = parsed == null ? null : BuiltInRegistries.BLOCK.getOptional(parsed).orElse(null);
      if (block == null) {
         throw new IllegalArgumentException(context + " names an unknown block id: " + id);
      }
   }

   static List<AiBuilder.CellSpec> transformCells(List<AiBuilder.CellSpec> cells, int rotation, String mirror) {
      if (rotation == 0 && mirror.equals("none")) {
         return cells;
      }

      List<AiBuilder.CellSpec> out = new ArrayList<>(cells.size());

      for (AiBuilder.CellSpec cell : cells) {
         out.add(transformCell(cell, rotation, mirror));
      }

      return out;
   }

   static AiBuilder.CellSpec transformCell(AiBuilder.CellSpec cell, int rotation, String mirror) {
      int dx = cell.dx();
      int dz = cell.dz();
      if (mirror.equals("x")) {
         dx = -dx;
      } else if (mirror.equals("z")) {
         dz = -dz;
      }

      int turns = rotation / 90;

      for (int i = 0; i < turns; i++) {
         int swap = dx;
         dx = -dz;
         dz = swap;
      }

      if (cell.properties().isEmpty()) {
         return new AiBuilder.CellSpec(dx, cell.dy(), dz, cell.block(), cell.properties());
      }

      BlockState var10000 = toBlockState(cell);
      Mirror var10000x;
      switch (mirror) {
         case "x":
            var10000x = Mirror.FRONT_BACK;
            break;
         case "z":
            var10000x = Mirror.LEFT_RIGHT;
            break;
         default:
            var10000x = Mirror.NONE;
      }

      Mirror var10001 = var10000x;
      BlockState var20 = var10000.mirror(var10001);
      Rotation var10001x;
      switch (turns) {
         case 1:
            var10001x = Rotation.CLOCKWISE_90;
            break;
         case 2:
            var10001x = Rotation.CLOCKWISE_180;
            break;
         case 3:
            var10001x = Rotation.COUNTERCLOCKWISE_90;
            break;
         default:
            var10001x = Rotation.NONE;
      }

      BlockState state = var20.rotate(var10001x);
      if (!mirror.equals("none") && state.getBlock() instanceof ChestBlock) {
         ChestType type = state.getValue(ChestBlock.TYPE);
         if (type != ChestType.SINGLE) {
            state = state.setValue(ChestBlock.TYPE, type == ChestType.LEFT ? ChestType.RIGHT : ChestType.LEFT);
         }
      }

      Map<String, String> props = new LinkedHashMap<>();

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (key.equals("north") || key.equals("east") || key.equals("south") || key.equals("west")) {
            if (!mirror.equals("none")) {
               key = mirrorFacing(key, mirror);
            }

            for (int i = 0; i < turns; i++) {
               key = rotateFacingClockwise(key);
            }
         }

         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)state.getBlock().getStateDefinition().getProperty(key);
         props.put(key, property == null ? entry.getValue() : String.valueOf(state.getValue(property)).toLowerCase(Locale.ROOT));
      }

      return new AiBuilder.CellSpec(dx, cell.dy(), dz, cell.block(), Map.copyOf(props));
   }

   static String rotateFacingClockwise(String facing) {
      String var10000;
      switch (facing.toLowerCase(Locale.ROOT)) {
         case "north":
            var10000 = "east";
            break;
         case "east":
            var10000 = "south";
            break;
         case "south":
            var10000 = "west";
            break;
         case "west":
            var10000 = "north";
            break;
         default:
            var10000 = facing;
      }

      return var10000;
   }

   static String mirrorFacing(String facing, String axis) {
      String f = facing.toLowerCase(Locale.ROOT);
      if (axis.equals("x")) {
         String var7;
         switch (f) {
            case "east":
               var7 = "west";
               break;
            case "west":
               var7 = "east";
               break;
            default:
               var7 = facing;
         }

         return var7;
      } else {
         String var10000;
         switch (f) {
            case "north":
               var10000 = "south";
               break;
            case "south":
               var10000 = "north";
               break;
            default:
               var10000 = facing;
         }

         return var10000;
      }
   }

   static List<AiBuilder.CellSpec> normalizeCells(List<AiBuilder.CellSpec> cells, int maxCells) {
      return normalizeCells(cells, maxCells, 256);
   }

   static List<AiBuilder.CellSpec> normalizeCells(List<AiBuilder.CellSpec> cells, int maxCells, int maxOffset) {
      return cells.isEmpty() ? List.of() : normalizeCells(cells, maxCells, maxOffset, minCellOffset(cells));
   }

   static BlockPos minCellOffset(List<AiBuilder.CellSpec> cells) {
      int x = 2147483647;
      int y = 2147483647;
      int z = 2147483647;

      for (AiBuilder.CellSpec cell : cells) {
         x = Math.min(x, cell.dx());
         y = Math.min(y, cell.dy());
         z = Math.min(z, cell.dz());
      }

      return new BlockPos(x, y, z);
   }

   static List<AiBuilder.CellSpec> normalizeCells(List<AiBuilder.CellSpec> cells, int maxCells, int maxOffset, BlockPos minimum) {
      int minX = minimum.getX();
      int minY = minimum.getY();
      int minZ = minimum.getZ();
      Map<Long, AiBuilder.CellSpec> unique = new LinkedHashMap<>();

      for (AiBuilder.CellSpec cell : cells) {
         int dx = cell.dx() - minX;
         int dy = cell.dy() - minY;
         int dz = cell.dz() - minZ;
         if (dx > maxOffset || dy > maxOffset || dz > maxOffset) {
            throw new IllegalArgumentException(
               "blueprint spans more than +/-"
                  + maxOffset
                  + " cells from its minimum corner ("
                  + dx
                  + ","
                  + dy
                  + ","
                  + dz
                  + "); split it into smaller schematics"
            );
         }

         unique.putIfAbsent(packOffsetKey(dx, dy, dz), cell);
      }

      if (unique.size() > maxCells) {
         throw new IllegalArgumentException("blueprint has " + unique.size() + " cells; the limit for this source is " + maxCells);
      }

      List<AiBuilder.CellSpec> out = new ArrayList<>(unique.size());

      for (AiBuilder.CellSpec cell : unique.values()) {
         out.add(new AiBuilder.CellSpec(cell.dx() - minX, cell.dy() - minY, cell.dz() - minZ, cell.block(), cell.properties()));
      }

      return List.copyOf(out);
   }

   static AiBuilder.Blueprint sliceLayerBand(AiBuilder.Blueprint blueprint, int start, int height) {
      List<AiBuilder.CellSpec> kept = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         if (cell.dy() >= start && cell.dy() < start + height) {
            kept.add(cell);
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(kept));
   }

   static AiBuilder.Blueprint parseBlueprint(JsonObject args, BlockPos fallbackOrigin) {
      JsonObject source = args;
      if (args.has("blueprint")) {
         String raw = AiJson.getString(args, "blueprint", "");
         if (raw.isBlank()) {
            throw new IllegalArgumentException("blueprint must be a JSON object string with origin and blocks");
         }

         source = AiJson.parseObject(raw);
      }

      BlockPos origin = auditOrigin(source, fallbackOrigin);
      if (source.has("blocks") && source.get("blocks").isJsonArray()) {
         JsonArray blocks = source.getAsJsonArray("blocks");
         if (blocks.size() > 4096) {
            throw new IllegalArgumentException("blueprint has " + blocks.size() + " cells; the limit is 4096");
         }

         List<AiBuilder.CellSpec> cells = new ArrayList<>(blocks.size());
         Set<Long> seen = new HashSet<>();
         int i = 0;

         while (i < blocks.size()) {
            String context = "blocks[" + i + "]";
            if (!blocks.get(i).isJsonObject()) {
               throw new IllegalArgumentException(context + " must be a {dx,dy,dz,block} object");
            }

            JsonObject entry = blocks.get(i).getAsJsonObject();
            int dx = requireExactInt(entry, "dx", context);
            int dy = requireExactInt(entry, "dy", context);
            int dz = requireExactInt(entry, "dz", context);
            if (Math.abs(dx) <= 256 && Math.abs(dy) <= 256 && Math.abs(dz) <= 256) {
               if (entry.has("block") && entry.get("block").isJsonPrimitive() && entry.getAsJsonPrimitive("block").isString()) {
                  String block = entry.get("block").getAsString();
                  Identifier id = Identifier.tryParse(block);
                  Block blockObj = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                  if (blockObj == null) {
                     throw new IllegalArgumentException(context + " names an unknown block id: " + block);
                  }

                  Map<String, String> properties = parseBlockProperties(entry, context, blockObj);
                  if (!seen.add(packOffsetKey(dx, dy, dz))) {
                     throw new IllegalArgumentException(context + " duplicates the cell at " + dx + "," + dy + "," + dz);
                  }

                  cells.add(new AiBuilder.CellSpec(dx, dy, dz, block, properties));
                  i++;
                  continue;
               }

               throw new IllegalArgumentException(context + ".block must be a block registry id string");
            }

            throw new IllegalArgumentException(context + " offsets must be within +/-256: " + dx + "," + dy + "," + dz);
         }

         return new AiBuilder.Blueprint(origin, List.copyOf(cells));
      } else {
         throw new IllegalArgumentException("blocks must be an array of {dx,dy,dz,block} objects");
      }
   }

   static BlockPos auditOrigin(JsonObject args, BlockPos fallback) {
      if (!args.has("origin") || args.get("origin").isJsonNull()) {
         return fallback;
      }

      if (!args.get("origin").isJsonObject()) {
         throw new IllegalArgumentException("origin must be an {x,y,z} object");
      }

      JsonObject o = args.getAsJsonObject("origin");
      return new BlockPos(requireExactInt(o, "x", "origin"), requireExactInt(o, "y", "origin"), requireExactInt(o, "z", "origin"));
   }

   static int requireExactInt(JsonObject object, String key, String context) {
      if (object.has(key) && object.get(key).isJsonPrimitive() && object.getAsJsonPrimitive(key).isNumber()) {
         try {
            return object.get(key).getAsBigDecimal().intValueExact();
         } catch (ArithmeticException e) {
            throw new IllegalArgumentException(context + "." + key + " must be a whole integer");
         }
      } else {
         throw new IllegalArgumentException(context + "." + key + " must be an integer");
      }
   }

   static Map<String, String> parseBlockProperties(JsonObject entry, String context, Block block) {
      if (!entry.has("properties")) {
         return Map.of();
      }

      if (entry.get("properties").isJsonNull()) {
         return Map.of();
      }

      if (!entry.get("properties").isJsonObject()) {
         throw new IllegalArgumentException(context + ".properties must be an object of {name: value} strings");
      }

      StateDefinition<Block, BlockState> definition = block.getStateDefinition();
      Map<String, String> out = new LinkedHashMap<>();
      String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();

      for (Entry<String, JsonElement> property : entry.getAsJsonObject("properties").entrySet()) {
         String name = property.getKey();
         if (!property.getValue().isJsonPrimitive() || !property.getValue().getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException(context + ".properties." + name + " must be a string value");
         }

         String value = property.getValue().getAsString();
         Property<?> prop = definition.getProperty(name);
         if (prop == null) {
            throw new IllegalArgumentException(context + ".properties." + name + " is not a property of " + blockId + "; valid properties: " + formatPropertyNames(definition));
         }

         if (prop.getValue(value).isEmpty()) {
            throw new IllegalArgumentException(
               context + ".properties." + name + " has illegal value \"" + value + "\" for " + blockId + "; valid values: " + prop.getPossibleValues()
            );
         }

         out.put(name, value);
      }

      return Map.copyOf(out);
   }

   private static String formatPropertyNames(StateDefinition<Block, BlockState> definition) {
      List<String> names = new ArrayList<>();

      for (Property<?> property : definition.getProperties()) {
         names.add(property.getName());
      }

      return names.isEmpty() ? "(none)" : String.join(", ", names);
   }

   static boolean isStackableCountProperty(Block block, String property) {
      return block instanceof SnowLayerBlock && property.equals("layers")
         || block instanceof CandleBlock && property.equals("candles")
         || block instanceof SeaPickleBlock && property.equals("pickles")
         || block instanceof TurtleEggBlock && property.equals("eggs")
         || block instanceof SegmentableBlock && (property.equals("flower_amount") || property.equals("segment_amount"));
   }

   static Item postInteractToolItem(Block block, String property) {
      if (block instanceof EndPortalFrameBlock && property.equals("eye")) {
         return Items.ENDER_EYE;
      } else if (block instanceof RespawnAnchorBlock && property.equals("charges")) {
         return Items.GLOWSTONE;
      } else if (block instanceof AbstractCandleBlock && property.equals("lit")) {
         return Items.FLINT_AND_STEEL;
      } else {
         return block instanceof CampfireBlock && property.equals("lit") ? Items.DIAMOND_SHOVEL : null;
      }
   }

   static boolean isSixWayFaceDerivedBlock(Block block) {
      return block instanceof ShulkerBoxBlock || block instanceof RodBlock || block instanceof AmethystClusterBlock;
   }

   static Float rotationPropertyYaw(Block block, int rotation) {
      float yaw = rotation * 22.5F;
      if (!(block instanceof StandingSignBlock) && !(block instanceof BannerBlock) && !(block instanceof CeilingHangingSignBlock)) {
         return block instanceof SkullBlock ? Mth.wrapDegrees(yaw) : null;
      } else {
         return Mth.wrapDegrees(yaw - 180.0F);
      }
   }

   static AiBuilder.OrientationKind orientationKindForProperty(Block block, String property) {
      if (block instanceof MultifaceBlock && Direction.byName(property) != null) {
         return AiBuilder.OrientationKind.POST_INTERACT;
      }

      if (property.equals("rotation") && rotationPropertyYaw(block, 0) != null) {
         return AiBuilder.OrientationKind.PLAYER_ROTATION;
      }

      if (property.equals("orientation") && block instanceof CrafterBlock) {
         return AiBuilder.OrientationKind.PLAYER_ORIENTATION;
      }

      if (property.equals("type") && block instanceof ChestBlock) {
         return AiBuilder.OrientationKind.CHEST_PAIR;
      }

      if (property.equals("note") && block instanceof NoteBlock) {
         return AiBuilder.OrientationKind.POST_INTERACT;
      }

      if (property.equals("facing") && isSixWayFaceDerivedBlock(block)) {
         return AiBuilder.OrientationKind.FACE_DERIVED;
      }

      if (!property.equals("facing")
         || !(block instanceof CampfireBlock)
            && !(block instanceof CalibratedSculkSensorBlock)
            && !(block instanceof DecoratedPotBlock)
            && !(block instanceof BellBlock)) {
         if (!property.equals("facing")
            || !(block instanceof BeehiveBlock)
               && !(block instanceof ChiseledBookShelfBlock)
               && !(block instanceof CopperGolemStatueBlock)
               && !(block instanceof EndPortalFrameBlock)
               && !(block instanceof SmallDripleafBlock)
               && !(block instanceof StonecutterBlock)
               && !(block instanceof VaultBlock)
               && !(block instanceof ShelfBlock)
               && !(block instanceof LecternBlock)
               && !(block instanceof SegmentableBlock)) {
            if (!property.equals("axis") || !(block instanceof InfestedRotatedPillarBlock) && !(block instanceof CreakingHeartBlock)) {
               if (property.equals("attachment") && block instanceof BellBlock) {
                  return AiBuilder.OrientationKind.FACE_DERIVED;
               }

               if (postInteractToolItem(block, property) != null || isStackableCountProperty(block, property)) {
                  return AiBuilder.OrientationKind.POST_INTERACT;
               }

               if (property.equals("inverted") && block instanceof DaylightDetectorBlock) {
                  return AiBuilder.OrientationKind.POST_INTERACT;
               }

               if (property.equals("waterlogged")
                  && block.defaultBlockState().hasProperty(BlockStateProperties.WATERLOGGED)
                  && block instanceof LiquidBlockContainer) {
                  return AiBuilder.OrientationKind.FLUID_INTERACT;
               }

               if (property.equals("face") && block instanceof FaceAttachedHorizontalDirectionalBlock) {
                  return AiBuilder.OrientationKind.FACE_DERIVED;
               }

               if (block instanceof DoorBlock && property.equals("hinge")) {
                  return AiBuilder.OrientationKind.CLICK_POSITION;
               }

               if (block instanceof LanternBlock && property.equals("hanging")) {
                  return AiBuilder.OrientationKind.FACE_DERIVED;
               }

               if (property.equals("axis") && block instanceof RotatedPillarBlock) {
                  return AiBuilder.OrientationKind.FACE_AXIS;
               }

               if ((!(block instanceof SlabBlock) || !property.equals("type"))
                  && (!(block instanceof StairBlock) && !(block instanceof TrapDoorBlock) || !property.equals("half"))) {
                  if (property.equals("facing")) {
                     if (block instanceof AnvilBlock) {
                        return AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE;
                     }

                     if (block instanceof BarrelBlock || block instanceof DispenserBlock || block instanceof PistonBaseBlock) {
                        return AiBuilder.OrientationKind.PLAYER_DIRECTION_OPPOSITE;
                     }

                     if (block instanceof ObserverBlock) {
                        return AiBuilder.OrientationKind.PLAYER_DIRECTION;
                     }

                     if (block instanceof HopperBlock || block instanceof ShulkerBoxBlock) {
                        return AiBuilder.OrientationKind.FACE_DERIVED;
                     }

                     if (isWallMountedBlock(block) || block instanceof FaceAttachedHorizontalDirectionalBlock) {
                        return AiBuilder.OrientationKind.FACE_DERIVED;
                     }

                     if (block instanceof StairBlock || block instanceof BedBlock || block instanceof DoorBlock || block instanceof FenceGateBlock) {
                        return AiBuilder.OrientationKind.PLAYER_FACING;
                     }

                     if (block instanceof TrapDoorBlock
                        || block instanceof HorizontalDirectionalBlock
                        || block instanceof AbstractFurnaceBlock
                        || block instanceof AbstractChestBlock) {
                        return AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE;
                     }
                  }

                  if (property.equals("open") && (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock)) {
                     return isPowerOnlyOpenable(block) ? AiBuilder.OrientationKind.POWER_STATE : AiBuilder.OrientationKind.POST_INTERACT;
                  } else if (property.equals("powered") && block instanceof LeverBlock) {
                     return AiBuilder.OrientationKind.POST_INTERACT;
                  } else {
                     return property.equals("delay") && block instanceof RepeaterBlock || property.equals("mode") && block instanceof ComparatorBlock
                        ? AiBuilder.OrientationKind.POST_INTERACT
                        : AiBuilder.OrientationKind.UNSUPPORTED;
                  }
               } else {
                  return AiBuilder.OrientationKind.CLICK_HEIGHT;
               }
            } else {
               return AiBuilder.OrientationKind.FACE_AXIS;
            }
         } else {
            return AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE;
         }
      } else {
         return AiBuilder.OrientationKind.PLAYER_FACING;
      }
   }

   static boolean isPowerOnlyOpenable(Block block) {
      return block == Blocks.IRON_TRAPDOOR || block instanceof DoorBlock door && !door.type().canOpenByHand();
   }

   static boolean isWallTorchBlock(Block block) {
      return block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock;
   }

   static boolean isWallMountedBlock(Block block) {
      return isWallTorchBlock(block)
         || block instanceof TripWireHookBlock
         || block instanceof BaseCoralWallFanBlock
         || block instanceof LadderBlock
         || block instanceof WallSignBlock
         || block instanceof WallBannerBlock
         || block instanceof WallSkullBlock;
   }

   static boolean usesWallClickFace(Block block, AiBuilder.CellSpec cell) {
      if (isWallMountedBlock(block)) {
         return true;
      }

      if (!(block instanceof FaceAttachedHorizontalDirectionalBlock)) {
         return false;
      }

      String face = cell.properties().get("face");
      return face == null || face.equalsIgnoreCase("wall");
   }

   static AiBuilder.OrientationKind facingOrientationKind(AiBuilder.CellSpec cell, Block block) {
      AiBuilder.OrientationKind strategy = orientationKindForProperty(block, "facing");
      return strategy == AiBuilder.OrientationKind.FACE_DERIVED && block instanceof FaceAttachedHorizontalDirectionalBlock && !usesWallClickFace(block, cell)
         ? AiBuilder.OrientationKind.PLAYER_FACING
         : strategy;
   }

   static List<String> unsupportedProperties(AiBuilder.CellSpec cell) {
      if (isTransientPistonCell(cell)) {
         return List.of();
      }

      if (AiSchematicActions.needsAction(cell)) {
         return AiSchematicActions.actionsFor(cell);
      }

      Block block = resolveBlock(cell);
      if (block.asItem() == Items.AIR && !cell.block().equals("minecraft:grass_block")) {
         return List.of("no item form");
      }

      if (!isRegisteredBlockItem(block) && !cell.block().equals("minecraft:grass_block")) {
         return List.of("its item places another block");
      }

      ArrayList<String> gaps = new ArrayList<>();

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (!isAutoComputedProperty(block, key)) {
            AiBuilder.OrientationKind strategy = key.equals("facing") ? facingOrientationKind(cell, block) : orientationKindForProperty(block, key);
            if (strategy == AiBuilder.OrientationKind.UNSUPPORTED
               && (!(block instanceof LeavesBlock) || !key.equals("persistent") || !entry.getValue().equals("true"))) {
               Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)block.getStateDefinition().getProperty(key);
               if (property == null || !String.valueOf(block.defaultBlockState().getValue(property)).equalsIgnoreCase(entry.getValue())) {
                  gaps.add(key + "=" + entry.getValue());
               }
            }
         }
      }

      return gaps;
   }

   static boolean hasPistonPowerAt(Level level, BlockPos pos, Direction facing) {
      return autismclient.ai.engine.AiPistonPower.isPowered(level, pos, facing);
   }

   static boolean willRedstoneTorchBeLit(Level level, BlockPos pos, AiBuilder.CellSpec cell) {
      if (resolveBlock(cell) instanceof RedstoneWallTorchBlock) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         return facing == null ? false : !level.hasSignal(pos.relative(facing.getOpposite()), facing.getOpposite());
      } else {
         return !level.hasSignal(pos.below(), Direction.DOWN);
      }
   }

   static Direction chestPartnerDirection(AiBuilder.CellSpec cell) {
      String type = cell.properties().get("type");
      Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      if (facing != null && facing.getAxis().isHorizontal() && type != null && !type.equals("single")) {
         return type.equals("left") ? facing.getClockWise() : facing.getCounterClockWise();
      } else {
         return null;
      }
   }

   static boolean hasUnpairedChestPartner(AiBuilder.CellSpec cell, BlockPos pos, BlockGetter world) {
      Direction side = chestPartnerDirection(cell);
      if (side == null) {
         return false;
      }

      BlockState partner = world.getBlockState(pos.relative(side));
      return partner.getBlock() == resolveBlock(cell)
         && partner.getValue(ChestBlock.FACING).getName().equals(cell.properties().get("facing"))
         && partner.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
   }

   static autismclient.ai.engine.PlacementGoal.PlaceConstraint buildPlaceConstraint(AiBuilder.CellSpec cell, Block block, BlockPos pos, BlockGetter world) {
      Direction clickFace = null;
      Direction look = null;
      Float yaw = null;
      double tolerance = 35.0;
      if (block instanceof LanternBlock) {
         clickFace = "true".equals(cell.properties().get("hanging")) ? Direction.DOWN : Direction.UP;
      } else if (usesWallClickFace(block, cell)) {
         clickFace = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      } else if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
         clickFace = faceAttachedClickDirection(cell);
      } else if (isSixWayFaceDerivedBlock(block)) {
         clickFace = Direction.byName(cell.properties().getOrDefault("facing", "up"));
      } else if (block instanceof HopperBlock) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "down"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            clickFace = facing.getOpposite();
         }
      }

      AiBuilder.OrientationKind strategy = facingOrientationKind(cell, block);
      boolean pairing = block instanceof ChestBlock && hasUnpairedChestPartner(cell, pos, world);
      if (pairing) {
         clickFace = chestPartnerDirection(cell).getOpposite();
         strategy = AiBuilder.OrientationKind.UNSUPPORTED;
      } else if (block instanceof ChestBlock) {
         BlockState below = world.getBlockState(pos.below());
         if (!below.getCollisionShape(world, pos.below()).isEmpty()) {
            clickFace = Direction.UP;
         }
      }

      if (strategy == AiBuilder.OrientationKind.PLAYER_FACING
         || strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE
         || strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE
         || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION
         || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION_OPPOSITE) {
         look = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         if (look != null && strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE) {
            look = look.getCounterClockWise();
         } else if (look != null && strategy != AiBuilder.OrientationKind.PLAYER_FACING && strategy != AiBuilder.OrientationKind.PLAYER_DIRECTION) {
            look = look.getOpposite();
         }
      }

      boolean fullDirection = strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION_OPPOSITE || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION;
      String rotation = cell.properties().get("rotation");
      if (rotation != null && orientationKindForProperty(block, "rotation") == AiBuilder.OrientationKind.PLAYER_ROTATION) {
         yaw = rotationPropertyYaw(block, Integer.parseInt(rotation));
         tolerance = 8.0;
         look = Direction.fromYRot(yaw.floatValue());
      }

      String orientation = cell.properties().get("orientation");
      if (orientation != null && orientationKindForProperty(block, "orientation") == AiBuilder.OrientationKind.PLAYER_ORIENTATION) {
         String[] parts = orientation.split("_");
         Direction front = Direction.byName(parts[0]);
         Direction top = parts.length > 1 ? Direction.byName(parts[1]) : null;
         if (front != null) {
            look = front.getOpposite();
            fullDirection = true;
            if (front == Direction.DOWN && top != null) {
               yaw = top.getOpposite().toYRot();
            } else if (front == Direction.UP && top != null) {
               yaw = top.toYRot();
            }

            if (yaw != null) {
               tolerance = 40.0;
            }
         }
      }

      if (isStandingFormOfWallItem(block)) {
         look = block.asItem() instanceof HangingSignItem ? Direction.UP : Direction.DOWN;
         fullDirection = true;
      } else if (block.asItem() instanceof StandingAndWallBlockItem) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            look = facing.getOpposite();
            fullDirection = true;
         }
      }

      Axis axis = block instanceof RotatedPillarBlock && cell.properties().containsKey("axis")
         ? Axis.valueOf(cell.properties().get("axis").toUpperCase(Locale.ROOT))
         : null;
      return new autismclient.ai.engine.PlacementGoal.PlaceConstraint(
         clickFace,
         look,
         axis,
         requiresUpperHalfClick(cell, block),
         fullDirection,
         block instanceof TrapDoorBlock ? Direction.byName(cell.properties().getOrDefault("facing", "north")) : null,
         autismclient.ai.engine.AiShapes.shapeId(toBlockState(cell).getCollisionShape(world, pos).toAabbs()),
         yaw,
         tolerance
      );
   }

   static boolean isStandingFormOfWallItem(Block block) {
      return block.asItem() instanceof StandingAndWallBlockItem item && item.getBlock() == block;
   }

   static boolean hasSolidSupportShape(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return !state.isAir() && !state.canBeReplaced() && !state.getShape(world, pos).isEmpty();
   }

   static boolean isRegisteredBlockItem(Block block) {
      if (block.asItem() instanceof BlockItem blockItem) {
         HashMap var3 = new HashMap();
         blockItem.registerBlocks(var3, blockItem);
         return var3.containsKey(block);
      } else {
         return false;
      }
   }

   static Direction faceAttachedClickDirection(AiBuilder.CellSpec cell) {
      Direction var10000;
      switch ((String)cell.properties().getOrDefault("face", "wall")) {
         case "floor":
            var10000 = Direction.UP;
            break;
         case "ceiling":
            var10000 = Direction.DOWN;
            break;
         default:
            var10000 = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      }

      return var10000;
   }

   static boolean isDoubleSlabCell(AiBuilder.CellSpec cell) {
      return "double".equalsIgnoreCase(cell.properties().get("type"))
         && BuiltInRegistries.BLOCK.getOptional(Identifier.parse(cell.block())).map(b -> b instanceof SlabBlock).orElse(false);
   }

   static Boolean requiresUpperHalfClick(AiBuilder.CellSpec cell, Block target) {
      if (isDoubleSlabCell(cell)) {
         return false;
      }

      String type = cell.properties().get("type");
      if (type != null && !isDoubleSlabCell(cell) && orientationKindForProperty(target, "type") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
         return type.equalsIgnoreCase("top");
      }

      String half = cell.properties().get("half");
      return half != null && orientationKindForProperty(target, "half") == AiBuilder.OrientationKind.CLICK_HEIGHT ? half.equalsIgnoreCase("top") : null;
   }

   static boolean clickFaceSatisfiesHalf(Boolean requestedHigh, Direction face) {
      if (requestedHigh != null && face != null) {
         return face.getAxis().isHorizontal() ? true : requestedHigh == (face == Direction.DOWN);
      } else {
         return true;
      }
   }

   static int itemCountForCell(AiBuilder.CellSpec cell) {
      if (!"upper".equals(cell.properties().get("half")) && !"head".equals(cell.properties().get("part"))) {
         Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(cell.block())).orElse(null);
         if (block != null) {
            for (Entry<String, String> entry : cell.properties().entrySet()) {
               if (isStackableCountProperty(block, entry.getKey())) {
                  return Math.max(1, Integer.parseInt(entry.getValue()));
               }
            }
         }

         return isDoubleSlabCell(cell) ? 2 : 1;
      } else {
         return 0;
      }
   }

   static boolean propertyValuesMatch(Block block, String property, String actual, String requested) {
      if (actual.equalsIgnoreCase(requested)) {
         return true;
      } else if (block instanceof FenceGateBlock && property.equals("facing")) {
         Direction a = Direction.byName(actual.toLowerCase(Locale.ROOT));
         Direction r = Direction.byName(requested.toLowerCase(Locale.ROOT));
         return a != null && r != null && a == r.getOpposite();
      } else {
         return false;
      }
   }

   static boolean isDynamicRedstoneProperty(Block block, String property) {
      if (block instanceof RedstoneTorchBlock) {
         return property.equals("lit");
      } else if (block instanceof RedstoneLampBlock) {
         return property.equals("lit");
      } else if (block instanceof RedStoneOreBlock) {
         return property.equals("lit");
      } else if (block instanceof DiodeBlock) {
         return Set.of("powered", "locked").contains(property);
      } else if (block instanceof ObserverBlock) {
         return property.equals("powered");
      } else if (block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock) {
         return property.equals("powered");
      } else if (block instanceof PistonBaseBlock) {
         return property.equals("extended");
      } else if (block instanceof DispenserBlock) {
         return property.equals("triggered");
      } else if (block instanceof RedstoneWireBlock) {
         return Set.of("power", "north", "south", "east", "west").contains(property);
      } else if (block instanceof BaseRailBlock) {
         return Set.of("shape", "powered").contains(property);
      } else if (block instanceof CrafterBlock) {
         return Set.of("crafting", "triggered").contains(property);
      } else if (block instanceof HopperBlock) {
         return property.equals("enabled");
      } else if (block instanceof BellBlock || block instanceof LecternBlock || block instanceof LightningRodBlock || block instanceof NoteBlock) {
         return property.equals("powered");
      } else if (block instanceof TripWireBlock || block instanceof TripWireHookBlock) {
         return Set.of("attached", "disarmed", "powered").contains(property);
      } else if (block instanceof DaylightDetectorBlock || block instanceof TargetBlock || block instanceof WeightedPressurePlateBlock) {
         return property.equals("power");
      } else if (block instanceof BasePressurePlateBlock) {
         return property.equals("powered");
      } else {
         return block instanceof SculkSensorBlock
            ? Set.of("power", "sculk_sensor_phase").contains(property)
            : block instanceof AbstractFurnaceBlock && property.equals("lit");
      }
   }

   static boolean isAutoComputedProperty(Block block, String property) {
      if (isDynamicRedstoneProperty(block, property)) {
         return true;
      } else if (block instanceof LeavesBlock) {
         return property.equals("distance");
      } else if (block instanceof StairBlock) {
         return property.equals("shape");
      } else if (block instanceof FenceBlock
         || block instanceof WallBlock
         || block instanceof IronBarsBlock
         || block instanceof HugeMushroomBlock
         || block instanceof ChorusPlantBlock) {
         return Set.of("north", "south", "east", "west", "up", "down").contains(property);
      } else if (block instanceof BedBlock) {
         return property.equals("occupied");
      } else if (block instanceof ScaffoldingBlock) {
         return property.equals("distance") || property.equals("bottom");
      } else if (block instanceof NoteBlock) {
         return property.equals("instrument");
      } else if (block instanceof CampfireBlock) {
         return property.equals("signal_fire");
      } else if (block instanceof FenceGateBlock) {
         return property.equals("in_wall");
      } else if (block instanceof BarrelBlock) {
         return property.equals("open");
      } else if (block instanceof SnowyBlock) {
         return property.equals("snowy");
      } else if (block instanceof PointedDripstoneBlock) {
         return property.equals("thickness");
      } else if (block instanceof BigDripleafBlock) {
         return property.equals("tilt");
      } else {
         return block instanceof BeehiveBlock ? property.equals("honey_level") : isGrowthAgeProperty(block, property);
      }
   }

   static boolean isGrowthAgeProperty(Block block, String property) {
      if (block instanceof SaplingBlock) {
         return property.equals("stage");
      } else if (block instanceof CactusBlock
         || block instanceof SugarCaneBlock
         || block instanceof GrowingPlantHeadBlock
         || block instanceof ChorusFlowerBlock) {
         return property.equals("age");
      } else if (block instanceof CaveVinesBlock) {
         return property.equals("berries") || property.equals("age");
      } else if (block instanceof BambooStalkBlock) {
         return Set.of("age", "leaves", "stage").contains(property);
      } else if (block instanceof MangrovePropaguleBlock) {
         return property.equals("age") || property.equals("stage");
      } else if (block instanceof TurtleEggBlock || block instanceof SnifferEggBlock) {
         return property.equals("hatch");
      } else if (block instanceof AbstractSkullBlock || block instanceof ButtonBlock) {
         return property.equals("powered");
      } else if (block instanceof ShelfBlock) {
         return property.equals("powered") || property.equals("side_chain");
      } else if (block instanceof CopperBulbBlock) {
         return property.equals("powered") || property.equals("lit");
      } else if (block instanceof SculkShriekerBlock) {
         return property.equals("shrieking") || property.equals("can_summon");
      } else if (block instanceof SculkCatalystBlock) {
         return property.equals("bloom");
      } else if (block instanceof BrushableBlock) {
         return property.equals("dusted");
      } else if (block instanceof DecoratedPotBlock) {
         return property.equals("cracked");
      } else if (block instanceof JukeboxBlock) {
         return property.equals("has_record");
      } else if (block instanceof LecternBlock) {
         return property.equals("has_book");
      } else if (block instanceof BrewingStandBlock) {
         return property.startsWith("has_bottle_");
      } else {
         return block instanceof ChiseledBookShelfBlock ? property.startsWith("slot_") : false;
      }
   }

   static boolean isUnpairedDoubleChest(AiBuilder.CellSpec cell, BlockState live) {
      return live.getBlock() instanceof ChestBlock
         && live.getValue(ChestBlock.TYPE) == ChestType.SINGLE
         && cell.properties().containsKey("type")
         && !"single".equals(cell.properties().get("type"));
   }

   static boolean differsOnlyInAutoProperties(AiBuilder.CellSpec cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      boolean differs = false;

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
         if (property != null && !propertyValuesMatch(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
            if (!isAutoComputedProperty(live.getBlock(), entry.getKey())) {
               return false;
            }

            differs = true;
         }
      }

      return differs;
   }

   static boolean matchesIgnoringUnpairedChest(AiBuilder.CellSpec cell, BlockState live) {
      if (matchesDirtGrassEquivalence(cell, live)) {
         return true;
      }

      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         if (!isAutoComputedProperty(live.getBlock(), entry.getKey()) && (!entry.getKey().equals("type") || !isUnpairedDoubleChest(cell, live))) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null || !propertyValuesMatch(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean isOccupiedOrMatchedScaffold(AiBuilder.CellSpec wanted, BlockState live, boolean ownedScaffold) {
      return !live.isAir() && (!ownedScaffold || wanted == null || matchesIgnoringUnpairedChest(wanted, live));
   }

   static boolean canClearDuringRecovery(boolean recovery, boolean breach, Integer columnBottom, int y, BlockState live) {
      return recovery && !live.hasBlockEntity() && live.getFluidState().isEmpty() && (breach || columnBottom != null && y >= columnBottom);
   }

   static boolean matchesExactCell(AiBuilder.CellSpec cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      if (live.getBlock() instanceof MultifaceBlock && !autismclient.ai.print.PrintRules.facesWithinSavedShape(toBlockState(cell), live)) {
         return false;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         if (!isAutoComputedProperty(live.getBlock(), entry.getKey())) {
            Property<?> property = live.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
               return false;
            }

            if (!propertyValuesMatch(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean matchesCellOrSubstitute(AiBuilder.CellSpec cell, BlockState live, Map<String, List<String>> substitutes) {
      if (matchesExactCell(cell, live)) {
         return true;
      }

      List<String> alts = substitutes.get(cell.block());
      if (alts != null && !alts.isEmpty()) {
         if (!alts.contains(BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString())) {
            return false;
         }

         for (Entry<String, String> entry : cell.properties().entrySet()) {
            if (!isAutoComputedProperty(live.getBlock(), entry.getKey())) {
               Property<?> property = live.getBlock().getStateDefinition().getProperty(entry.getKey());
               if (property != null && !propertyValuesMatch(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean matchesDirtGrassEquivalence(AiBuilder.CellSpec cell, BlockState live) {
      return (cell.block().equals("minecraft:dirt") && cell.properties().isEmpty() || cell.block().equals("minecraft:grass_block"))
         && (live.is(Blocks.DIRT) || live.is(Blocks.GRASS_BLOCK));
   }

   static boolean matchesSchematicCell(AiBuilder.CellSpec cell, BlockState live) {
      if (matchesDirtGrassEquivalence(cell, live)) {
         return true;
      }

      Fluid fluid = fluidOfCell(cell);
      if (isFlowingFluidCell(cell)) {
         return live.getFluidState().getType().isSame(fluid) && !live.getFluidState().isSource();
      }

      if (fluid != null
         && "0".equals(cell.properties().getOrDefault("level", "0"))
         && live.getFluidState().getType().isSame(fluid)
         && live.getFluidState().isSource()) {
         return true;
      }

      if (resolveBlock(cell) == live.getBlock() && isAgeableCropBlock(live.getBlock())) {
         for (Entry<String, String> entry : cell.properties().entrySet()) {
            if (!entry.getKey().equals("age")) {
               Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
               if (property == null || !propertyValuesMatch(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return cell.block().equals("minecraft:farmland") && live.is(Blocks.FARMLAND)
            || cell.block().equals("minecraft:torchflower_crop") && live.is(Blocks.TORCHFLOWER)
            || cell.block().equals("minecraft:melon_stem") && live.is(Blocks.ATTACHED_MELON_STEM)
            || cell.block().equals("minecraft:pumpkin_stem") && live.is(Blocks.ATTACHED_PUMPKIN_STEM)
            || matchesExactCell(cell, live);
      }
   }

   static boolean matchesStrictState(AiBuilder.CellSpec cell, BlockState live) {
      return compareCellState(cell, live).matches();
   }

   static AiBuilder.MatchResult compareCellState(AiBuilder.CellSpec cell, BlockState live) {
      BlockState expected = toBlockState(cell);
      if (cell.block().equals("minecraft:dirt") && cell.properties().isEmpty() && live.is(Blocks.GRASS_BLOCK)) {
         return AiBuilder.MatchResult.MATCH;
      }

      Fluid fluid = fluidOfCell(cell);
      if (isFlowingFluidCell(cell)) {
         return live.getFluidState().getType().isSame(fluid) && !live.getFluidState().isSource()
            ? AiBuilder.MatchResult.MATCH
            : new AiBuilder.MatchResult(false, "fluid expected=flowing " + cell.block() + " actual=" + live.getFluidState());
      }

      if (fluid != null && "0".equals(cell.properties().getOrDefault("level", "0"))) {
         return live.getFluidState().getType().isSame(fluid) && live.getFluidState().isSource()
            ? AiBuilder.MatchResult.MATCH
            : new AiBuilder.MatchResult(false, "fluid expected=source " + cell.block() + " actual=" + live.getFluidState());
      }

      if ((!cell.block().equals("minecraft:torchflower_crop") || !live.is(Blocks.TORCHFLOWER))
         && (!cell.block().equals("minecraft:melon_stem") || !live.is(Blocks.ATTACHED_MELON_STEM))
         && (!cell.block().equals("minecraft:pumpkin_stem") || !live.is(Blocks.ATTACHED_PUMPKIN_STEM))) {
         if (live.getBlock() != expected.getBlock()) {
            String actual = BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString();
            return new AiBuilder.MatchResult(false, "block expected=" + cell.block() + " actual=" + actual);
         }

         for (Property<?> property : expected.getBlock().getStateDefinition().getProperties()) {
            String name = property.getName();
            if (!isAutoComputedProperty(expected.getBlock(), name)
               && (!(expected.getBlock() instanceof FarmlandBlock) || !name.equals("moisture"))
               && (!isAgeableCropBlock(expected.getBlock()) || !name.equals("age"))) {
               String wanted = String.valueOf(expected.getValue(property));
               String actual = String.valueOf(live.getValue(property));
               if (!wanted.equals(actual)) {
                  return new AiBuilder.MatchResult(false, name + " expected=" + wanted + " actual=" + actual);
               }
            }
         }

         return AiBuilder.MatchResult.MATCH;
      } else {
         return AiBuilder.MatchResult.MATCH;
      }
   }

   static boolean isFlowingFluidCell(AiBuilder.CellSpec cell) {
      return fluidOfCell(cell) != null && !"0".equals(cell.properties().getOrDefault("level", "0"));
   }

   static Fluid fluidOfCell(AiBuilder.CellSpec cell) {
      FlowingFluid var10000;
      switch (cell.block()) {
         case "minecraft:water":
            var10000 = Fluids.WATER;
            break;
         case "minecraft:lava":
            var10000 = Fluids.LAVA;
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static boolean isTransientPistonCell(AiBuilder.CellSpec cell) {
      return cell.block().equals("minecraft:piston_head") || cell.block().equals("minecraft:moving_piston");
   }

   static boolean needsWaterlogToggleOnly(AiBuilder.CellSpec cell, BlockState live) {
      if (orientationKindForProperty(live.getBlock(), "waterlogged") == AiBuilder.OrientationKind.FLUID_INTERACT
         && cell.properties().containsKey("waterlogged")
         && !String.valueOf(live.getValue(BlockStateProperties.WATERLOGGED)).equals(cell.properties().get("waterlogged"))) {
         LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
         properties.remove("waterlogged");
         return matchesIgnoringUnpairedChest(new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties), live);
      } else {
         return false;
      }
   }

   private static boolean isCellSatisfied(AiBuilder.CellSpec cell, BlockState live, Map<String, List<String>> substitutes, boolean schematic) {
      return schematic && matchesSchematicCell(cell, live) || matchesCellOrSubstitute(cell, live, substitutes);
   }

   static long packOffsetKey(int dx, int dy, int dz) {
      return (long)(dx + 256) << 42 | (long)(dy + 256) << 21 | dz + 256;
   }

   static List<AiBuilder.CellSpec> orderBlueprintCells(AiBuilder.Blueprint blueprint) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      Set<Long> present = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         present.add(packOffsetKey(cell.dx(), cell.dy(), cell.dz()));
      }

      Map<Long, Integer> depth = new HashMap<>();

      for (AiBuilder.CellSpec cell : cells) {
         computeSupportDepth(cell, present, depth);
      }

      List<AiBuilder.CellSpec> ordered = new ArrayList<>(cells);
      ordered.sort(
         Comparator.comparingInt(AiBuilder.CellSpec::dy)
            .thenComparingInt(cellx -> depth.get(packOffsetKey(cellx.dx(), cellx.dy(), cellx.dz())))
            .thenComparingLong(cellx -> (long)cellx.dx() * cellx.dx() + (long)cellx.dy() * cellx.dy() + (long)cellx.dz() * cellx.dz())
            .thenComparingInt(AiBuilder.CellSpec::dx)
            .thenComparingInt(AiBuilder.CellSpec::dz)
            .thenComparing(AiBuilder.CellSpec::block)
      );
      return ordered;
   }

   static List<AiBuilder.CellSpec> orderSchematicCells(AiBuilder.Blueprint blueprint) {
      ArrayList<AiBuilder.CellSpec> ordered = new ArrayList<>(blueprint.cells());
      ordered.sort(
         Comparator.comparingInt(AiBuilder.CellSpec::dy)
            .thenComparingInt(
               cellx -> {
                  Block block = resolveBlock(cellx);
                  if (fluidOfCell(cellx) != null) {
                     return 3;
                  } else if (block instanceof BedBlock) {
                     return -1;
                  } else {
                     return !(block instanceof DoublePlantBlock) && !(block instanceof CropBlock)
                        ? (block.defaultBlockState().isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) ? 0 : 1)
                        : 2;
                  }
               }
            )
            .thenComparingInt(AiBuilder.CellSpec::dz)
            .thenComparingInt(cellx -> (cellx.dz() & 1) == 0 ? cellx.dx() : -cellx.dx())
            .thenComparing(AiBuilder.CellSpec::block)
      );
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.level != null) {
         ordered = new ArrayList<>(topologicalSupportOrder(blueprint, ordered, neighbor -> AiTerrain.isLoaded(mc.level, neighbor) && hasSolidSupportShape(mc.level, neighbor)));
      }

      ordered = new ArrayList<>(deferFacingBlockers(ordered));
      HashMap<Long, AiBuilder.CellSpec> crops = new HashMap<>();

      for (AiBuilder.CellSpec cell : ordered) {
         if (resolveBlock(cell) instanceof CropBlock) {
            crops.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      ArrayList<AiBuilder.CellSpec> seeded = new ArrayList<>(ordered.size());
      HashSet<AiBuilder.CellSpec> moved = new HashSet<>();

      for (AiBuilder.CellSpec cell : ordered) {
         if (!moved.contains(cell)) {
            seeded.add(cell);
            if (cell.block().equals("minecraft:farmland")) {
               AiBuilder.CellSpec crop = crops.get(packOffsetKey(cell.dx(), cell.dy() + 1, cell.dz()));
               if (crop != null) {
                  seeded.add(crop);
                  moved.add(crop);
               }
            }
         }
      }

      return orderFluidsAfterContainment(seeded);
   }

   static List<AiBuilder.CellSpec> deferFacingBlockers(List<AiBuilder.CellSpec> ordered) {
      int n = ordered.size();
      if (n < 2) {
         return ordered;
      }

      HashMap<Long, Integer> index = new HashMap<>(n * 2);
      int bottom = 2147483647;

      for (int i = 0; i < n; i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         index.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), i);
         bottom = Math.min(bottom, cell.dy());
      }

      Direction[] looks = new Direction[n];
      HashMap<AiBuilder.CellSpec, Direction> looked = new HashMap<>();

      for (int i = 0; i < n; i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         looks[i] = looked.computeIfAbsent(new AiBuilder.CellSpec(0, 0, 0, cell.block(), cell.properties()), AiBuilder::horizontalLookDirection);
      }

      double[] key = new double[n];

      for (int i = 0; i < n; i++) {
         key[i] = i;
      }

      boolean[] moved = new boolean[n];
      boolean any = false;

      for (int round = 0; round < 3; round++) {
         boolean changed = false;

         for (int i = 0; i < n; i++) {
            Direction look = looks[i];
            if (look != null) {
               AiBuilder.CellSpec part = ordered.get(i);
               Integer blocker = index.get(packOffsetKey(part.dx() - look.getStepX(), part.dy(), part.dz() - look.getStepZ()));
               if (blocker != null
                  && !moved[blocker]
                  && !(key[blocker] > key[i])
                  && !toBlockState(ordered.get(blocker)).getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
                  boolean supported = part.dy() == bottom;

                  for (Direction side : Direction.values()) {
                     if (!supported && side != Direction.UP && side != look.getOpposite()) {
                        Integer other = index.get(packOffsetKey(part.dx() + side.getStepX(), part.dy() + side.getStepY(), part.dz() + side.getStepZ()));
                        supported = other != null && key[other] < key[i];
                     }
                  }

                  if (supported) {
                     key[blocker] = key[i] + 0.5;
                     int var10001 = blocker;
                     any = true;
                     changed = true;
                     moved[var10001] = true;
                  }
               }
            }
         }

         if (!changed) {
            break;
         }
      }

      if (!any) {
         return ordered;
      }

      Integer[] order = new Integer[n];

      for (int i = 0; i < n; i++) {
         order[i] = i;
      }

      Arrays.sort(order, Comparator.comparingDouble(ix -> key[ix]));
      ArrayList<AiBuilder.CellSpec> out = new ArrayList<>(n);

      for (Integer i : order) {
         out.add(ordered.get(i));
      }

      return out;
   }

   static Direction horizontalLookDirection(AiBuilder.CellSpec cell) {
      Block block = resolveBlock(cell);
      if (block != Blocks.AIR && fluidOfCell(cell) == null) {
         try {
            autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = buildPlaceConstraint(cell, block, BlockPos.ZERO, EmptyBlockGetter.INSTANCE);
            Direction look = constraint.playerFacing();
            return look != null && look.getAxis().isHorizontal() ? look : null;
         } catch (RuntimeException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   static List<AiBuilder.CellSpec> orderFluidsAfterContainment(List<AiBuilder.CellSpec> ordered) {
      HashMap<Long, Integer> index = new HashMap<>();

      for (int i = 0; i < ordered.size(); i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         index.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), i);
      }

      HashMap<Integer, List<AiBuilder.CellSpec>> after = new HashMap<>();
      HashSet<Integer> water = new HashSet<>();
      int[][] sides = new int[][]{{0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

      for (int i = 0; i < ordered.size(); i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         if (fluidOfCell(cell) != null) {
            int last = -1;

            for (int[] side : sides) {
               Integer at = index.get(packOffsetKey(cell.dx() + side[0], cell.dy() + side[1], cell.dz() + side[2]));
               if (at != null && fluidOfCell(ordered.get(at)) == null) {
                  last = Math.max(last, at);
               }
            }

            if (last < 0) {
               last = i - 1;
            }

            water.add(i);
            after.computeIfAbsent(last, k -> new ArrayList<>()).add(cell);
         }
      }

      if (water.isEmpty()) {
         return ordered;
      }

      ArrayList<AiBuilder.CellSpec> out = new ArrayList<>(ordered.size());
      out.addAll(after.getOrDefault(-1, List.of()));

      for (int i = 0; i < ordered.size(); i++) {
         if (!water.contains(i)) {
            out.add(ordered.get(i));
         }

         out.addAll(after.getOrDefault(i, List.of()));
      }

      out.sort(Comparator.comparingInt(cell -> isFlowingFluidCell(cell) ? 1 : 0));
      return out;
   }

   static List<AiBuilder.CellSpec> topologicalSupportOrder(
      AiBuilder.Blueprint blueprint, List<AiBuilder.CellSpec> ordered, Predicate<BlockPos> observedSupport
   ) {
      Map<Long, Integer> index = new HashMap<>();
      BlockPos[] positions = new BlockPos[ordered.size()];
      boolean[] support = new boolean[ordered.size()];
      boolean[] queued = new boolean[ordered.size()];
      Direction[] required = new Direction[ordered.size()];
      PriorityQueue<Integer> ready = new PriorityQueue<>();

      for (int i = 0; i < ordered.size(); i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         Block block = resolveBlock(cell);
         positions[i] = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
         index.put(positions[i].asLong(), i);
         BlockState placed = toBlockState(cell);
         support[i] = !placed.canBeReplaced() && !placed.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
         if (block instanceof LanternBlock) {
            required[i] = "true".equals(cell.properties().get("hanging")) ? Direction.UP : Direction.DOWN;
         } else if (isWallMountedBlock(block)) {
            Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
            required[i] = facing == null ? null : facing.getOpposite();
         } else if (block instanceof DoorBlock
            || block instanceof BedBlock
            || block instanceof CropBlock
            || block instanceof FlowerPotBlock
            || block instanceof CarpetBlock
            || block instanceof DoublePlantBlock) {
            required[i] = Direction.DOWN;
         }

         for (Direction direction : Direction.values()) {
            if ((required[i] == null || required[i] == direction) && allowsSupportOnSide(cell, block, direction)) {
               BlockPos neighbor = positions[i].relative(direction);
               if (observedSupport.test(neighbor)) {
                  queued[i] = true;
                  ready.add(i);
                  break;
               }
            }
         }
      }

      ArrayList<AiBuilder.CellSpec> frontier = new ArrayList<>(ordered.size());

      while (!ready.isEmpty()) {
         int i = ready.remove();
         frontier.add(ordered.get(i));
         if (support[i]) {
            for (Direction direction : Direction.values()) {
               Integer next = index.get(positions[i].relative(direction).asLong());
               if (next != null
                  && !queued[next]
                  && (required[next] == null || required[next] == direction.getOpposite())
                  && allowsSupportOnSide(ordered.get(next), resolveBlock(ordered.get(next)), direction.getOpposite())) {
                  queued[next] = true;
                  ready.add(next);
               }
            }
         }
      }

      for (int i = 0; i < ordered.size(); i++) {
         if (!queued[i]) {
            frontier.add(ordered.get(i));
         }
      }

      return frontier;
   }

   static boolean allowsSupportOnSide(AiBuilder.CellSpec cell, Block block, Direction supportDirection) {
      if (block instanceof ShulkerBoxBlock && cell.properties().containsKey("facing")) {
         Direction facing = Direction.byName(cell.properties().get("facing"));
         return facing != null && supportDirection == facing.getOpposite();
      }

      if (block instanceof HopperBlock && cell.properties().containsKey("facing")) {
         Direction facing = Direction.byName(cell.properties().get("facing"));
         return facing == Direction.DOWN ? supportDirection.getAxis() == Axis.Y : supportDirection == facing;
      }

      if (block instanceof LanternBlock) {
         return supportDirection == ("true".equals(cell.properties().get("hanging")) ? Direction.UP : Direction.DOWN);
      }

      if (usesWallClickFace(block, cell)) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            return supportDirection == facing.getOpposite();
         }
      }

      String attachment = cell.properties().get("face");
      if ("ceiling".equals(attachment)) {
         return supportDirection == Direction.UP;
      } else if ("floor".equals(attachment)) {
         return supportDirection == Direction.DOWN;
      } else {
         Direction click = supportDirection.getOpposite();
         Boolean high = requiresUpperHalfClick(cell, block);
         if (high != null && click.getAxis() == Axis.Y && high != (click == Direction.DOWN)) {
            return false;
         } else {
            return block instanceof RotatedPillarBlock
                  && cell.properties().containsKey("axis")
                  && !click.getAxis().getName().equals(cell.properties().get("axis"))
               ? false
               : !(block instanceof TrapDoorBlock)
                  || !click.getAxis().isHorizontal()
                  || !cell.properties().containsKey("facing")
                  || click.getName().equals(cell.properties().get("facing"));
         }
      }
   }

   private static int computeSupportDepth(AiBuilder.CellSpec cell, Set<Long> present, Map<Long, Integer> memo) {
      List<Long> chain = new ArrayList<>();
      int x = cell.dx();
      int y = cell.dy();
      int z = cell.dz();

      int depth;
      while (true) {
         long key = packOffsetKey(x, y, z);
         Integer known = memo.get(key);
         if (known != null) {
            depth = known;
            break;
         }

         chain.add(key);
         if (!present.contains(packOffsetKey(x, y - 1, z))) {
            depth = 0;
            break;
         }

         y--;
      }

      for (int i = chain.size() - 1; i >= 0; i--) {
         memo.put(chain.get(i), depth++);
      }

      return memo.get(packOffsetKey(cell.dx(), cell.dy(), cell.dz()));
   }

   static JsonObject missingMaterials(AiBuilder.Blueprint blueprint, Inventory inventory) {
      return missingMaterials(blueprint, inventory, Map.of());
   }

   static JsonObject missingMaterials(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes) {
      return missingMaterials(blueprint, inventory, substitutes, false);
   }

   static JsonObject missingMaterials(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes, boolean schematic) {
      Map<Item, Integer> available = new HashMap<>();

      for (int i = 0; i < inventory.getContainerSize(); i++) {
         ItemStack stack = inventory.getItem(i);
         if (!stack.isEmpty()) {
            available.merge(stack.getItem(), stack.getCount(), Integer::sum);
         }
      }

      Map<String, Integer> required = new TreeMap<>();
      Map<String, List<Item>> pooled = new TreeMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         Block block = BuiltInRegistries.BLOCK
            .getOptional(Identifier.parse(cell.block()))
            .orElseThrow(() -> new IllegalArgumentException("Unknown block id: " + cell.block()));
         if (schematic && cell.block().equals("minecraft:grass_block")) {
            block = Blocks.DIRT;
         }

         Item item = block.asItem();
         if (item != Items.AIR) {
            String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
            required.merge(itemId, itemCountForCell(cell), Integer::sum);
            List<Item> candidates = pooled.computeIfAbsent(itemId, k -> new ArrayList<>(List.of(item)));

            for (String candidateId : substitutes.getOrDefault(cell.block(), List.of())) {
               Item alt = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(candidateId)).map(Block::asItem).orElse(Items.AIR);
               if (alt != Items.AIR && !candidates.contains(alt)) {
                  candidates.add(alt);
               }
            }
         }
      }

      ArrayList<AiCellMaterials.MaterialRequirement> plans = new ArrayList<>(required.size());
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (Entry<String, Integer> entry : required.entrySet()) {
         if (entry.getValue() != 0) {
            List<Item> candidates = pooled.get(entry.getKey());
            plans.add(
               new AiCellMaterials.MaterialRequirement(
                  AiCellMaterials.CellAction.PLACE, entry.getKey(), candidates.getFirst(), entry.getValue(), AiCellMaterials.MaterialUse.CONSUME
               )
            );
            Set<String> specs = alternatives.computeIfAbsent(entry.getKey(), k -> new TreeSet<>());

            for (Item candidate : candidates) {
               specs.add(BuiltInRegistries.ITEM.getKey(candidate).toString());
            }
         }
      }

      return solveMissingMaterialsFlow(
         plans, alternatives, spec -> available.getOrDefault(BuiltInRegistries.ITEM.getOptional(Identifier.parse(spec)).orElse(Items.AIR), 0)
      );
   }

   static AiOperation checkSchematicReadiness(AiBuilder.Blueprint blueprint) {
      int reportLimit = 16;
      AiSchematicReadiness.ReadinessReport audit = AiSchematicReadiness.assessBlueprint(blueprint, reportLimit);
      return readinessFailureOp(blueprint, audit);
   }

   static AiOperation readinessFailureOp(AiBuilder.Blueprint blueprint, AiSchematicReadiness.ReadinessReport audit) {
      if (audit.ready()) {
         return null;
      }

      List<AiSchematicReadiness.ReadinessIssue> issues = audit.issues();
      JsonArray reported = new JsonArray();

      for (AiSchematicReadiness.ReadinessIssue issue : issues) {
         AiBuilder.CellSpec cell = issue.cell();
         JsonObject properties = new JsonObject();
         cell.properties().forEach(properties::addProperty);
         reported.add(
            AiJson.objectOf(
               "kind",
               issue.kind().name(),
               "block",
               cell.block(),
               "dx",
               cell.dx(),
               "dy",
               cell.dy(),
               "dz",
               cell.dz(),
               "properties",
               properties,
               "detail",
               issue.detail()
            )
         );
      }

      AiSchematicReadiness.ReadinessIssue first = issues.getFirst();
      AiBuilder.CellSpec cell = first.cell();
      boolean contextOnly = audit.issues().stream().allMatch(issue -> issue.kind() == AiSchematicReadiness.IssueKind.MISSING_CONTEXT);
      String message = (contextOnly ? "Cannot build here: " : "Cannot build this schematic as saved: ")
         + "schematic readiness audit found "
         + audit.issueCount()
         + (audit.issueCount() == 1L ? " issue" : " issues")
         + "; first: "
         + first.kind()
         + " "
         + cell.block()
         + " at "
         + cell.dx()
         + ","
         + cell.dy()
         + ","
         + cell.dz()
         + ": "
         + first.detail();
      AiFailure code = contextOnly ? AiFailure.PLACEMENT_IMPOSSIBLE : AiFailure.IMPOSSIBLE_STATE;
      return AiGameOperations.immediateFailure(
         AiOperation.Status.FAILED,
         code,
         message,
         AiJson.objectOf(
            "issueCount",
            audit.issueCount(),
            "reportedIssueCount",
            reported.size(),
            "issuesTruncated",
            audit.truncated(),
            "issues",
            reported,
            "placed",
            0,
            "skipped",
            0,
            "cells",
            blueprint.cells().size()
         )
      );
   }

   static JsonObject missingSchematicMaterials(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes) {
      ArrayList<AiCellMaterials.MaterialRequirement> plans = new ArrayList<>();
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         List<AiCellMaterials.MaterialRequirement> cellPlans = AiSchematicActions.materialPlans(cell);
         plans.addAll(cellPlans);
         collectSubstituteAlternatives(cell, cellPlans, substitutes, alternatives);
      }

      return missingMaterialsForInventory(plans, inventory, alternatives);
   }

   static JsonObject missingSchematicMaterialsInWorld(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes, LevelReader world) {
      ArrayList<AiBuilder.CellSpec> auditCells = new ArrayList<>(blueprint.cells().size());
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         BlockPos target = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
         BlockState live = world.getBlockState(target);
         AiBuilder.CellSpec planned = matchesCellOrSubstitute(cell, live, substitutes) && !matchesSchematicCell(cell, live)
            ? cellSpecFromLiveState(cell, live)
            : cell;
         auditCells.add(planned);
         if (!substitutes.getOrDefault(cell.block(), List.of()).isEmpty()) {
            boolean waterAbove = world.getFluidState(target.above()).is(FluidTags.WATER);
            collectSubstituteAlternatives(cell, AiCellMaterials.requirements(planned, live, waterAbove), substitutes, alternatives);
         }
      }

      List<AiCellMaterials.MaterialRequirement> plans = AiCellMaterials.requirementsInWorld(auditCells, blueprint.origin(), world);
      return missingMaterialsForInventory(plans, inventory, alternatives);
   }

   private static AiBuilder.CellSpec cellSpecFromLiveState(AiBuilder.CellSpec cell, BlockState live) {
      Map<String, String> properties = new LinkedHashMap<>();
      live.getValues().forEach(value -> properties.put(value.property().getName(), value.valueName()));
      return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString(), properties);
   }

   private static void collectSubstituteAlternatives(
      AiBuilder.CellSpec cell,
      Iterable<AiCellMaterials.MaterialRequirement> plans,
      Map<String, List<String>> substitutes,
      Map<String, Set<String>> alternatives
   ) {
      for (AiCellMaterials.MaterialRequirement requirement : plans) {
         if (requirement.action() == AiCellMaterials.CellAction.PLACE || requirement.action() == AiCellMaterials.CellAction.ADD) {
            for (String candidateId : substitutes.getOrDefault(cell.block(), List.of())) {
               Item alternative = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(candidateId)).map(Block::asItem).orElse(Items.AIR);
               if (alternative != Items.AIR) {
                  String alternativeSpec = BuiltInRegistries.ITEM.getKey(alternative).toString();
                  if (!alternativeSpec.equals(requirement.itemSpec())) {
                     alternatives.computeIfAbsent(requirement.itemSpec(), k -> new HashSet<>()).add(alternativeSpec);
                  }
               }
            }
         }
      }
   }

   static JsonObject missingMaterialsForInventory(Iterable<AiCellMaterials.MaterialRequirement> plans, Inventory inventory, Map<String, Set<String>> alternatives) {
      return solveMissingMaterialsFlow(plans, alternatives, spec -> AiSchematicPlacement.countItem(inventory, spec));
   }

   static JsonObject missingMaterialsForContainer(Iterable<AiCellMaterials.MaterialRequirement> plans, Container inventory, boolean carriedStock) {
      Map<Item, Integer> counts = AiCarriedShulker.countItems(inventory, carriedStock);
      return solveMissingMaterialsFlow(plans, Map.of(), spec -> AiItems.resolveItem(spec).items().stream().mapToInt(item -> counts.getOrDefault(item, 0)).sum());
   }

   private static JsonObject solveMissingMaterialsFlow(
      Iterable<AiCellMaterials.MaterialRequirement> plans, Map<String, ? extends Iterable<String>> alternatives, ToIntFunction<String> stockCount
   ) {
      Map<String, Integer> required = new TreeMap<>();
      Map<String, Set<String>> pools = new TreeMap<>();

      for (AiCellMaterials.MaterialRequirement requirement : AiCellMaterials.combine(plans)) {
         required.merge(requirement.itemSpec(), requirement.count(), Integer::sum);
         Set<String> pool = pools.computeIfAbsent(requirement.itemSpec(), k -> new TreeSet<>());
         pool.add(requirement.itemSpec());
         Iterable<String> accepted = (Iterable<String>)alternatives.get(requirement.itemSpec());
         if (accepted != null) {
            for (String candidate : accepted) {
               pool.add(candidate);
            }
         }
      }

      JsonObject missing = new JsonObject();
      if (required.isEmpty()) {
         return missing;
      }

      Map<String, Integer> stock = new TreeMap<>();

      for (Set<String> pool : pools.values()) {
         for (String candidate : pool) {
            stock.computeIfAbsent(candidate, key -> Math.max(0, stockCount.applyAsInt(key)));
         }
      }

      int stockBase = 1 + required.size();
      int sink = stockBase + stock.size();
      Map<String, Integer> stockNodes = new HashMap<>();
      int stockIndex = 0;

      for (String candidate : stock.keySet()) {
         stockNodes.put(candidate, stockBase + stockIndex++);
      }

      AiBuilder.FlowGraph flow = new AiBuilder.FlowGraph(sink + 1);
      int[] sourceEdges = new int[required.size()];
      int requirementIndex = 0;

      for (Entry<String, Integer> entry : required.entrySet()) {
         int requirementNode = 1 + requirementIndex;
         sourceEdges[requirementIndex] = flow.addEdge(0, requirementNode, entry.getValue());
         flow.addEdge(requirementNode, stockNodes.get(entry.getKey()), entry.getValue());

         for (String candidate : pools.get(entry.getKey())) {
            if (!candidate.equals(entry.getKey())) {
               flow.addEdge(requirementNode, stockNodes.get(candidate), entry.getValue());
            }
         }

         requirementIndex++;
      }

      for (Entry<String, Integer> entry : stock.entrySet()) {
         flow.addEdge(stockNodes.get(entry.getKey()), sink, entry.getValue());
      }

      flow.computeMaxFlow(0, sink);
      requirementIndex = 0;

      for (Entry<String, Integer> entry : required.entrySet()) {
         int have = entry.getValue() - flow.remainingCapacity(0, sourceEdges[requirementIndex++]);
         if (have < entry.getValue()) {
            missing.add(entry.getKey(), AiJson.objectOf("required", entry.getValue(), "available", have, "missing", entry.getValue() - have));
         }
      }

      return missing;
   }

   private static Block resolveBlock(AiBuilder.CellSpec cell) {
      return BuiltInRegistries.BLOCK
         .getOptional(Identifier.parse(cell.block()))
         .orElseThrow(() -> new IllegalArgumentException("Unknown block id: " + cell.block()));
   }

   static AiBuilder.CellSpec markLeavesPersistent(AiBuilder.CellSpec cell) {
      if (cell.block().endsWith("_leaves") && "false".equals(cell.properties().get("persistent"))) {
         LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
         properties.put("persistent", "true");
         return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties);
      } else {
         return cell;
      }
   }

   static BlockState toBlockState(AiBuilder.CellSpec cell) {
      BlockState state = resolveBlock(cell).defaultBlockState();

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)state.getBlock().getStateDefinition().getProperty(entry.getKey());
         if (property != null) {
            state = applyPropertyValue(state, property, entry.getValue());
         }
      }

      return state;
   }

   private static <T extends Comparable<T>> BlockState applyPropertyValue(BlockState state, Property<T> property, String value) {
      return state.setValue(property, property.getValue(value).orElseThrow());
   }

   static boolean collidesWithEntityBox(AiBuilder.CellSpec cell, BlockPos pos, AABB body, BlockGetter world) {
      return Shapes.joinIsNotEmpty(
         toBlockState(cell).getCollisionShape(world, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(body), BooleanOp.AND
      );
   }

   static AiBuilder.Blueprint normalizeSchematicBlueprint(AiBuilder.Blueprint blueprint) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel world = mc == null ? null : mc.level;
      return normalizeSchematicBlueprintWithWorld(blueprint, pos -> world != null && AiTerrain.isLoaded(world, pos) ? world.getBlockState(pos) : Blocks.AIR.defaultBlockState());
   }

   static AiBuilder.Blueprint normalizeSchematicBlueprint(AiBuilder.Blueprint blueprint, Map<Long, BlockState> leafSnapshot) {
      return normalizeSchematicBlueprintWithWorld(blueprint, pos -> leafSnapshot.getOrDefault(pos.asLong(), Blocks.AIR.defaultBlockState()));
   }

   private static AiBuilder.Blueprint normalizeSchematicBlueprintWithWorld(AiBuilder.Blueprint blueprint, Function<BlockPos, BlockState> externalState) {
      HashMap<Long, AiBuilder.CellSpec> source = new HashMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         checkNotCancelled();
         if (!isOmittedSchematicCell(cell) && !isTransientPistonCell(cell)) {
            source.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      Map<AiBuilder.CellSpec, Integer> leafDistances = computeLeafDistances(
         blueprint.cells(),
         cellx -> LeavesBlock.getOptionalDistanceAt(toBlockState(cellx)).orElse(7) == 0,
         cellx -> externalLeafDistance(source, blueprint.origin(), cellx, externalState)
      );
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         checkNotCancelled();
         if (!isOmittedSchematicCell(cell) && !isTransientPistonCell(cell)) {
            AiBuilder.CellSpec adjusted = markLeavesPersistent(cell);
            if (cell.block().equals("minecraft:attached_melon_stem") || cell.block().equals("minecraft:attached_pumpkin_stem")) {
               LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
               properties.remove("facing");
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block().replace("attached_", ""), properties);
            }

            Block plant = resolveBlock(adjusted);
            if (isAgeableCropBlock(plant)) {
               LinkedHashMap<String, String> properties = new LinkedHashMap<>(adjusted.properties());
               properties.remove("age");
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), adjusted.block(), properties);
            }

            Integer distance = leafDistances.get(cell);
            if (distance != null) {
               LinkedHashMap<String, String> properties = new LinkedHashMap<>(adjusted.properties());
               properties.put("distance", Integer.toString(distance));
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties);
            }

            AiBuilder.CellSpec above = source.get(packOffsetKey(cell.dx(), cell.dy() + 1, cell.dz()));
            if (cell.block().equals("minecraft:grass_block")) {
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:dirt");
            }

            if (cell.block().equals("minecraft:seagrass") || cell.block().equals("minecraft:tall_seagrass") || cell.block().equals("minecraft:bubble_column")) {
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:water", Map.of("level", "0"));
            }

            if (cell.block().equals("minecraft:farmland") || cell.block().equals("minecraft:dirt_path")) {
               BlockState aboveState = above == null ? Blocks.AIR.defaultBlockState() : toBlockState(above);
               boolean water = false;
               if (cell.block().equals("minecraft:farmland")) {
                  for (int dx = -4; dx <= 4 && !water; dx++) {
                     for (int dz = -4; dz <= 4 && !water; dz++) {
                        for (int dy = 0; dy <= 1 && !water; dy++) {
                           AiBuilder.CellSpec neighbour = source.get(packOffsetKey(cell.dx() + dx, cell.dy() + dy, cell.dz() + dz));
                           water = neighbour != null && toBlockState(neighbour).getFluidState().is(FluidTags.WATER);
                        }
                     }
                  }
               }

               adjusted = normalizeSoilCell(cell, aboveState.isSolid(), aboveState.is(BlockTags.MAINTAINS_FARMLAND), aboveState.getBlock() instanceof FenceGateBlock, water);
            }

            cells.add(adjusted);
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(cells));
   }

   static List<BlockPos> collectLeafNeighborPositions(AiBuilder.Blueprint blueprint) {
      HashSet<Long> source = new HashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         checkNotCancelled();
         if (!isOmittedSchematicCell(cell) && !isTransientPistonCell(cell)) {
            source.add(packOffsetKey(cell.dx(), cell.dy(), cell.dz()));
         }
      }

      LinkedHashSet<BlockPos> neighbors = new LinkedHashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         checkNotCancelled();
         if (resolveBlock(cell) instanceof LeavesBlock) {
            for (Direction direction : Direction.values()) {
               int x = cell.dx() + direction.getStepX();
               int y = cell.dy() + direction.getStepY();
               int z = cell.dz() + direction.getStepZ();
               if (!source.contains(packOffsetKey(x, y, z))) {
                  neighbors.add(blueprint.origin().offset(x, y, z));
               }
            }
         }
      }

      return List.copyOf(neighbors);
   }

   static void checkNotCancelled() {
      if (Thread.currentThread().isInterrupted()) {
         throw new CancellationException("Schematic preparation cancelled");
      }
   }

   static boolean isReplaceableFoliage(AiBuilder.CellSpec cell) {
      return REPLACEABLE_FOLIAGE_BLOCKS.contains(cell.block());
   }

   private static boolean isAgeableCropBlock(Block plant) {
      return plant instanceof CropBlock
         || plant instanceof StemBlock
         || plant instanceof CocoaBlock
         || plant instanceof PitcherCropBlock
         || plant instanceof NetherWartBlock
         || plant instanceof SweetBerryBushBlock;
   }

   static boolean isOmittedSchematicCell(AiBuilder.CellSpec cell) {
      return isReplaceableFoliage(cell) || cell.block().equals("minecraft:pitcher_crop") && "upper".equals(cell.properties().get("half"));
   }

   static Map<AiBuilder.CellSpec, Integer> computeLeafDistances(
      List<AiBuilder.CellSpec> cells, Predicate<AiBuilder.CellSpec> log, ToIntFunction<AiBuilder.CellSpec> surrounding
   ) {
      HashMap<Long, AiBuilder.CellSpec> source = new HashMap<>();
      HashMap<Long, AiBuilder.CellSpec> leaves = new HashMap<>();
      HashMap<AiBuilder.CellSpec, Integer> distances = new HashMap<>();
      ArrayDeque<AiBuilder.CellSpec> queue = new ArrayDeque<>();

      for (AiBuilder.CellSpec cell : cells) {
         checkNotCancelled();
         source.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), cell);
         if (resolveBlock(cell) instanceof LeavesBlock) {
            leaves.put(packOffsetKey(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      for (AiBuilder.CellSpec cell : leaves.values()) {
         checkNotCancelled();
         int distance = Math.clamp(surrounding.applyAsInt(cell), 1, 7);

         for (Direction direction : Direction.values()) {
            AiBuilder.CellSpec neighbor = source.get(
               packOffsetKey(cell.dx() + direction.getStepX(), cell.dy() + direction.getStepY(), cell.dz() + direction.getStepZ())
            );
            if (neighbor != null && log.test(neighbor)) {
               distance = 1;
               break;
            }
         }

         distances.put(cell, distance);
         if (distance < 7) {
            queue.add(cell);
         }
      }

      while (!queue.isEmpty()) {
         checkNotCancelled();
         AiBuilder.CellSpec cell = queue.removeFirst();
         int next = distances.get(cell) + 1;
         if (next < 7) {
            for (Direction direction : Direction.values()) {
               AiBuilder.CellSpec neighbor = leaves.get(
                  packOffsetKey(cell.dx() + direction.getStepX(), cell.dy() + direction.getStepY(), cell.dz() + direction.getStepZ())
               );
               if (neighbor != null && next < distances.get(neighbor)) {
                  distances.put(neighbor, next);
                  queue.addLast(neighbor);
               }
            }
         }
      }

      return Map.copyOf(distances);
   }

   private static int externalLeafDistance(
      Map<Long, AiBuilder.CellSpec> source, BlockPos origin, AiBuilder.CellSpec cell, Function<BlockPos, BlockState> externalState
   ) {
      int distance = 7;

      for (Direction direction : Direction.values()) {
         int x = cell.dx() + direction.getStepX();
         int y = cell.dy() + direction.getStepY();
         int z = cell.dz() + direction.getStepZ();
         if (!source.containsKey(packOffsetKey(x, y, z))) {
            BlockPos pos = origin.offset(x, y, z);
            BlockState state = externalState.apply(pos);
            distance = Math.min(distance, LeavesBlock.getOptionalDistanceAt(state).orElse(7) + 1);
         }
      }

      return distance;
   }

   static AiBuilder.CellSpec normalizeSoilCell(
      AiBuilder.CellSpec cell, boolean solidAbove, boolean maintainsFarmland, boolean fenceGateAbove, boolean nearWater
   ) {
      if (cell.block().equals("minecraft:dirt_path")) {
         return solidAbove && !fenceGateAbove ? new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:dirt") : cell;
      } else if (!cell.block().equals("minecraft:farmland")) {
         return cell;
      } else if ((!solidAbove || maintainsFarmland) && (nearWater || maintainsFarmland)) {
         LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
         properties.put("moisture", nearWater ? "7" : "0");
         return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties);
      } else {
         return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:dirt");
      }
   }

   static AiOperation buildFromArgs(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.player.isAlive()) {
         AiBuilder.BuildOptions power = parseBuildOptions(args);
         if (!AutismLiteVariant.enabled() && args.has("schematic") && !args.get("schematic").isJsonNull()) {
            return new AiPrintPreparation(args, power);
         }

         AiSchematic.LoadedSchematic decodedSchematic = null;
         AiBuilder.Blueprint blueprint;
         if (args.has("schematic") && !args.get("schematic").isJsonNull()) {
            if (args.has("blocks") || args.has("blueprint")) {
               throw new IllegalArgumentException("schematic is mutually exclusive with blocks/blueprint; pass one source");
            }

            BlockPos origin = auditOrigin(args, mc.player.blockPosition());
            AiSchematic.LoadedSchematic loaded = AiSchematic.load(
               AiSchematic.resolveFile(AiSchematic.directory(), AiJson.getString(args, "schematic", "")),
               origin,
               power.rotation(),
               power.mirror(),
               AiJson.getBoolean(args, "skipUnknown", false),
               2097152
            );
            blueprint = loaded.blueprint();
            decodedSchematic = loaded;
            power = power.withSignText(loaded.signText());
            AutismClientAddon.LOG
               .info(
                  "[AI] build: schematic \"{}\" decoded to {} cells at {}",
                  AiJson.getString(args, "schematic", ""),
                  blueprint.cells().size(),
                  origin.toShortString()
               );
         } else {
            blueprint = parseBlueprint(args, mc.player.blockPosition());
            if (power.rotation() != 0 || !power.mirror().equals("none")) {
               blueprint = new AiBuilder.Blueprint(blueprint.origin(), transformCells(blueprint.cells(), power.rotation(), power.mirror()));
            }
         }

         if (power.layers()) {
            blueprint = sliceLayerBand(blueprint, power.layerStart(), power.layerHeight());
         }

         if (decodedSchematic != null) {
            power = power.withMachine(AiSchematicMachinePlan.fromSchematic(decodedSchematic, blueprint));
         }

         boolean schematic = args.has("schematic");
         int decodedCells = blueprint.cells().size();
         if (schematic) {
            blueprint = normalizeSchematicBlueprint(blueprint);
            power = power.withMachine(power.machine().init(blueprint));
            AiOperation readinessFailure = checkSchematicReadiness(blueprint);
            if (readinessFailure != null) {
               return readinessFailure;
            }

            AutismClientAddon.LOG
               .info(
                  "[AI] build: survival blueprint has {} of {} decoded cells ({} decorative/plant cells omitted)",
                  blueprint.cells().size(),
                  decodedCells,
                  decodedCells - blueprint.cells().size()
               );
         }

         AiBuilder.Blueprint stockPlan = schematic ? stripPoweredLevers(blueprint) : blueprint;
         boolean creativeSupply = schematic && mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.CREATIVE && mc.player.getAbilities().instabuild;
         JsonObject missing = creativeSupply
            ? new JsonObject()
            : (
               schematic
                  ? missingSchematicMaterialsInWorld(stockPlan, mc.player.getInventory(), power.substitutes(), mc.level)
                  : missingMaterials(stockPlan, mc.player.getInventory(), power.substitutes())
            );
         if (schematic && missing.size() > 0 && (!power.shulkerStock() || !hasNearbyExternalShulkerStock(blueprint))) {
            return AiGameOperations.immediateFailure(
               AiOperation.Status.FAILED,
               AiFailure.MISSING_MATERIALS,
               "Missing materials for the schematic; "
                  + (power.shulkerStock() ? "no nearby external shulker stock" : "shulker stock is disabled")
                  + ": "
                  + missing,
               AiJson.objectOf(
                  "missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray(), "shulkerStock", false, "cells", blueprint.cells().size()
               )
            );
         }

         if (!schematic && missing.size() > 0) {
            return AiJson.getBoolean(args, "craftMissing", true)
               ? craftMissingThenBuild(args, missing)
               : AiGameOperations.immediateFailure(
                  AiOperation.Status.FAILED,
                  AiFailure.MISSING_MATERIALS,
                  "Missing materials: " + missing,
                  AiJson.objectOf("missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray())
               );
         }

         boolean verify = AiJson.getBoolean(args, "verify", true);
         int timeoutSeconds = AiJson.getInt(args, "timeoutSeconds", 600);
         return power.repeat() != null
            ? new AiBuilder.BuildOp(blueprint, power, verify, timeoutSeconds)
            : buildBlueprint(blueprint, verify, timeoutSeconds, power, schematic);
      } else {
         throw new IllegalArgumentException("Building requires a connected living player");
      }
   }

   private static boolean hasNearbyExternalShulkerStock(AiBuilder.Blueprint blueprint) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null) {
         BlockPos center = mc.player.blockPosition();
         HashSet<Long> source = new HashSet<>();
         int minX = 2147483647;
         int minZ = 2147483647;
         int maxX = -2147483648;
         int maxZ = -2147483648;

         for (AiBuilder.CellSpec cell : blueprint.cells()) {
            BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
            source.add(at.asLong());
            minX = Math.min(minX, at.getX());
            maxX = Math.max(maxX, at.getX());
            minZ = Math.min(minZ, at.getZ());
            maxZ = Math.max(maxZ, at.getZ());
         }

         ArrayList<BlockPos[]> boxes = new ArrayList<>();
         boxes.add(new BlockPos[]{center.offset(-24, -8, -24), center.offset(24, 8, 24)});
         if (minX <= maxX) {
            int y = blueprint.origin().getY();
            boxes.add(new BlockPos[]{new BlockPos(minX - 24, y - 8, minZ - 24), new BlockPos(maxX + 24, y + 8, maxZ + 24)});
         }

         for (BlockPos[] box : boxes) {
            for (BlockPos p : BlockPos.betweenClosed(box[0], box[1])) {
               if (AiTerrain.isLoaded(mc.level, p) && !source.contains(p.asLong()) && mc.level.getBlockState(p).getBlock() instanceof ShulkerBoxBlock) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   static AiBuilder.Blueprint filterUnsatisfiedCells(
      AiBuilder.Blueprint blueprint, Map<String, List<String>> substitutes, Function<BlockPos, BlockState> state
   ) {
      ArrayList<AiBuilder.CellSpec> remaining = new ArrayList<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         BlockState live = state.apply(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()));
         if (!isCellSatisfied(cell, live, substitutes, true) && !needsWaterlogToggleOnly(cell, live)) {
            if (isDoubleSlabCell(cell) && live.getBlock() == resolveBlock(cell)) {
               LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
               properties.put("type", "bottom");
               remaining.add(new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties));
            } else {
               remaining.add(cell);
            }
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(remaining));
   }

   static AiBuilder.Blueprint stripPoweredLevers(AiBuilder.Blueprint blueprint) {
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         checkNotCancelled();
         if (cell.block().equals("minecraft:lever") && "true".equals(cell.properties().get("powered"))) {
            LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
            properties.remove("powered");
            cells.add(new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties));
         } else {
            cells.add(cell);
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(cells));
   }

   private static AiOperation craftMissingThenBuild(final JsonObject args, final JsonObject missing) {
      final List<String> items = new ArrayList<>(missing.keySet());
      return new AiOperation() {
         final JsonArray craftedItems = new JsonArray();
         AiOperation craftOp;
         int craftItemIndex;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.craftOp != null) {
               AiOperation.Result r = this.craftOp.tick();
               if (this.init_Z) {
                  return r.terminal() ? this.withCraftedFirst(r) : r;
               }

               if (!r.terminal()) {
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Crafting missing " + items.get(this.craftItemIndex) + ": " + r.message(), new JsonObject()
                  );
               }

               this.craftOp = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.missingFailure(items.get(this.craftItemIndex), r.message());
               }

               this.craftedItems.add(items.get(this.craftItemIndex));
               this.craftItemIndex++;
            }

            if (this.craftItemIndex < items.size()) {
               String item = items.get(this.craftItemIndex);
               int shortBy = missing.getAsJsonObject(item).get("missing").getAsInt();

               try {
                  this.craftOp = AiCraft.craft(AiJson.objectOf("item", item, "count", shortBy, "additional", true));
               } catch (IllegalArgumentException e) {
                  return this.missingFailure(item, e.getMessage());
               }

               return AiGameOperations.result(AiOperation.Status.RUNNING, "Crafting missing " + item, new JsonObject());
            } else {
               JsonObject again = args.deepCopy();
               again.addProperty("craftMissing", false);

               try {
                  this.craftOp = AiBuilder.buildFromArgs(again);
               } catch (IllegalArgumentException e) {
                  return AiOperation.Result.failed(AiFailure.EXECUTION_FAILED, e.getMessage(), "{}");
               }

               this.init_Z = true;
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Materials crafted; building", new JsonObject());
            }
         }

         AiOperation.Result missingFailure(String item, String reason) {
            JsonObject data = AiJson.objectOf(
               "missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray(), "crafted", this.craftedItems
            );
            return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "Missing materials: " + missing + " (could not craft " + item + ": " + reason + ")", data.toString());
         }

         AiOperation.Result withCraftedFirst(AiOperation.Result r) {
            if (this.craftedItems.isEmpty()) {
               return r;
            }

            try {
               JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
               data.add("craftedFirst", this.craftedItems);
               return new AiOperation.Result(r.status(), r.message(), data.toString(), r.evidence(), r.failure());
            } catch (RuntimeException e) {
               return r;
            }
         }

         @Override
         public void cancel() {
            if (this.craftOp != null) {
               this.craftOp.cancel();
            }
         }
      };
   }

   static AiOperation buildBlueprint(AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds) {
      return buildBlueprint(blueprint, verify, timeoutSeconds, AiBuilder.BuildOptions.DEFAULT);
   }

   static AiOperation buildBlueprint(AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power) {
      return buildBlueprint(blueprint, verify, timeoutSeconds, power, false);
   }

   private static AiOperation buildBlueprint(
      AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power, boolean schematic
   ) {
      return buildBlueprint(blueprint, verify, timeoutSeconds, power, schematic, Set.of());
   }

   static List<AiBuilder.CellSpec> deferMechanismsToEnd(List<AiBuilder.CellSpec> order) {
      ArrayList<AiBuilder.CellSpec> plain = new ArrayList<>();
      ArrayList<AiBuilder.CellSpec> mechanisms = new ArrayList<>();

      for (AiBuilder.CellSpec cell : order) {
         (isDeferredMechanismBlock(cell.block()) ? mechanisms : plain).add(cell);
      }

      plain.addAll(mechanisms);
      return plain;
   }

   static boolean isDeferredMechanismBlock(String block) {
      if (block == null) {
         return false;
      }

      boolean var10000;
      switch (block) {
         case "minecraft:piston":
         case "minecraft:sticky_piston":
         case "minecraft:dropper":
         case "minecraft:dispenser":
         case "minecraft:observer":
         case "minecraft:redstone_torch":
         case "minecraft:redstone_wall_torch":
         case "minecraft:redstone_block":
         case "minecraft:lever":
         case "minecraft:comparator":
         case "minecraft:repeater":
         case "minecraft:powered_rail":
         case "minecraft:rail":
         case "minecraft:note_block":
         case "minecraft:tnt":
         case "minecraft:crafter":
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   static AiOperation buildSchematicSubtask(AiBuilder.Blueprint blueprint, Set<Long> protectedCells) {
      return buildBlueprint(blueprint, true, 60, AiBuilder.BuildOptions.DEFAULT, true, protectedCells, false);
   }

   static AiOperation buildSchematic(AiBuilder.Blueprint blueprint, int timeoutSeconds) {
      return buildBlueprint(blueprint, true, timeoutSeconds, AiBuilder.BuildOptions.DEFAULT, true);
   }

   static AiOperation buildSchematicSneaking(AiBuilder.Blueprint blueprint, Set<Long> protectedCells) {
      return buildBlueprint(blueprint, true, 60, AiBuilder.BuildOptions.DEFAULT, true, protectedCells, false, true);
   }

   private static AiOperation buildBlueprint(
      AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power, boolean schematic, Set<Long> protectedCells
   ) {
      return buildBlueprint(blueprint, verify, timeoutSeconds, power, schematic, protectedCells, schematic);
   }

   private static AiOperation buildBlueprint(
      AiBuilder.Blueprint blueprint,
      boolean verify,
      int timeoutSeconds,
      AiBuilder.BuildOptions power,
      boolean schematic,
      Set<Long> protectedCells,
      boolean scaffoldingOwner
   ) {
      return buildBlueprint(blueprint, verify, timeoutSeconds, power, schematic, protectedCells, scaffoldingOwner, false);
   }

   private static AiOperation buildBlueprint(
      final AiBuilder.Blueprint blueprint,
      final boolean verify,
      final int timeoutSeconds,
      final AiBuilder.BuildOptions power,
      final boolean schematic,
      final Set<Long> protectedCells,
      final boolean scaffoldingOwner,
      final boolean holdSneak
   ) {
      final Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.player.isAlive()) {
         if (schematic && scaffoldingOwner) {
            List<AiBuilder.CellSpec> controls = blueprint.cells()
               .stream()
               .filter(cellx -> cellx.block().equals("minecraft:lever") && "true".equals(cellx.properties().get("powered")))
               .toList();
            if (!controls.isEmpty()) {
               HashSet<Long> keep = new HashSet<>(protectedCells);
               HashSet<Long> closedDoors = new HashSet<>();

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  keep.add(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong());
               }

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  if ("false".equals(cell.properties().get("open"))) {
                     closedDoors.add(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong());
                  }
               }

               long deadline = System.nanoTime() + Math.max(1, timeoutSeconds) * 1000000000L;
               AiOperation construction = buildBlueprint(
                  stripPoweredLevers(blueprint), verify, timeoutSeconds, power, schematic, protectedCells, scaffoldingOwner, holdSneak
               );
               return new AiDeferredControls(
                  construction,
                  controls,
                  cellx -> AiSchematicActions.init(cellx, blueprint.origin().offset(cellx.dx(), cellx.dy(), cellx.dz()), keep),
                  deadline,
                  System::nanoTime,
                  owner -> {
                     autismclient.ai.engine.AiRoutePolicy.setClosedDoors(closedDoors);
                     AiVisualization.showPlan(owner, blueprint.origin(), blueprint.cells(), blueprint.cells().size(), false, true);
                     AiVisualization.showCount(owner, "Building schematic", "Restoring captured switch positions", -1, -1);
                  },
                  owner -> {
                     autismclient.ai.engine.AiRoutePolicy.setClosedDoors(Set.of());
                     AiVisualization.clearOwner(owner);
                  }
               );
            }
         }

         return new AiOperation() {
            final List<AiBuilder.CellSpec> orderedCells = schematic ? AiBuilder.deferMechanismsToEnd(AiBuilder.orderSchematicCells(blueprint)) : AiBuilder.orderBlueprintCells(blueprint);
            final AABB buildBounds = new AABB(
               blueprint.origin().getX() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dx).min().orElse(0),
               blueprint.origin().getY() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dy).min().orElse(0),
               blueprint.origin().getZ() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dz).min().orElse(0),
               blueprint.origin().getX() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dx).max().orElse(0) + 1,
               blueprint.origin().getY() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dy).max().orElse(0) + 1,
               blueprint.origin().getZ() + this.orderedCells.stream().mapToInt(AiBuilder.CellSpec::dz).max().orElse(0) + 1
            );
            final int placeAttemptLimit = power.skipFailed() ? 1 : 3;
            final long deadlineNanos = System.nanoTime() + Math.max(1L, Math.min(schematic ? 2592000L : 3600L, timeoutSeconds)) * 1000000000L;
            final int originalSelectedSlot = mc.player.getInventory().getSelectedSlot();
            final List<JsonObject> failureEntries = new ArrayList<>();
            final autismclient.ai.engine.AiAim placementAim = new autismclient.ai.engine.AiAim();
            AiOperation activeSubtask;
            AiOperation batchApproachOp;
            AiSchematicApproach.Inner_DataUtil prefetchedApproachJob;
            AiSchematicApproach.Inner_DataUtil activeApproachJob;
            AiSchematicApproach.Inner_DataUtil lastApproachJob;
            int prefetchStartCursor = -1;
            int prefetchCooldown;
            int prefetchScanStart;
            int prefetchScanCursor;
            int prefetchLayerY;
            List<autismclient.ai.engine.PlacementGoal.PlacementRequest> prefetchRequests;
            Set<BlockPos> prefetchOmitted = Set.of();
            List<BlockPos> batchPositions = List.of();
            final Set<Long> rejectedBatchCells = new HashSet<>();
            AiBuilder.CellSpec walkingPlacementCell;
            AiBuilder.CellSpec pendingAckCell;
            final AiBuilder.ReadyQueue readyQueue = new AiBuilder.ReadyQueue();
            final AiBuilder.PlaceAckTracker placeAckTracker = new AiBuilder.PlaceAckTracker();
            final Map<Long, Integer> placeAttempts = new HashMap<>();
            final Set<Long> read_Set = new HashSet<>();
            BlockPlacementPlanner.PlacementPlan walkingPlacementPlan;
            Predicate<AABB> walkingClearCheck;
            int walkingPlacementCount;
            int walkingClickCount;
            int walkingRefusalCount;
            final Set<Long> write_Set = new HashSet<>();
            AiOperation.Result heldSubtaskResult;
            BlockPos batchFocusPos;
            int permanentAccessSteps;
            int sourceFloorWalkingTicks;
            int batchPlannedCount;
            int batchPrefetchedCount;
            int batchCellCount;
            int itineraryStopCount;
            int continuedStopCount;
            int overlapTicks;
            int estimatedSupportBlocks;
            Vec3 lastWorkerPosition;
            double planningMillis;
            double estimatedSeconds;
            boolean init_Z;
            AiOperation cellConstructionOp;
            long lastFluidPlacementTick = -4611686018427387904L;
            AiOperation shulkerRefillOp;
            final Set<Long> triedShulkerSources = new HashSet<>();
            int shulkerRefillCount;
            BlockPos pendingPlacePos;
            Block pendingPlaceBlock;
            long pendingPlaceRevision;
            AiBuilder.CellSpec pendingPlaceCell;
            Vec3 pendingAimPoint;
            BlockPlacementPlanner.PlacementPlan currentPlacementPlan;
            int start_I;
            final JsonArray unappliedProperties = new JsonArray();
            final Set<String> reportedPropertyKeys = new HashSet<>();
            float pendingYaw = 0.0F / 0.0F;
            double yawTolerance = 40.0;
            boolean load_Z;
            int stop_I;
            boolean read_Z;
            int next_I;
            String init_Str;
            int prev;
            int matchedConfirmations;
            int verifyTicks;
            int cellCursor;
            int currentCellAttempts;
            int cache;
            int placedCount;
            int skippedCount;
            int failedTotal;
            int entry;
            int node;
            int task;
            boolean write_Z;
            int auditPassCount;
            int reachFirstCount;
            int sweepPassCount;
            final Set<Long> flush_Set = new HashSet<>();
            AiOperation auditRepairOp;
            boolean sweepActive;
            final List<AiBuilder.CellSpec> repairCells = new ArrayList<>();
            int auditCursor;
            boolean sync_Z;
            final Set<Long> close_Set = new HashSet<>(this.orderedCells.stream().map(c -> blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong()).toList());
            final Map<Long, AiBuilder.CellSpec> load_Map;
            final Set<Long> existingSupportKeys;
            boolean outerScaffoldCleared;
            final boolean ownsScaffolding;
            final AiGuardLayer guardLayer;
            int survivalInterruptions;
            final Set<Long> scaffoldSupportKeys;
            final Map<Long, Integer> read_Map;
            final Set<Long> push_Set;
            final Set<Long> pop_Set;
            AiOperation accessStairsOp;
            BlockPos pendingFoundation;
            final Set<Long> foundationKeys;
            int stairLayerY;
            boolean accessStairsReady;
            List<BlockPos> terrainToClear;
            int terrainClearIndex;
            int terrainLeftCount;
            AiOperation terrainBreakOp;
            boolean terrainCleared;
            boolean includeCheckpointSupports;
            final AiScaffold.JobScope scaffoldTracker;
            final List<BlockPos> outerScaffoldCells;
            final List<BlockPos> drainSupports;
            boolean pop_Z;
            int drainInitialSupports;
            int drainSteps;
            final Map<Long, Integer> drainAttemptCounts;
            final Map<Long, Long> drainRetryAt;
            BlockPos drainCandidate;
            Set<Long> detachedSupports;
            Set<Long> activeKeep;
            boolean retryingDetached;
            int drainRemoved;
            long drainLastRemovalMs;
            long drainStartMs;
            AiOperation supportStepOffOp;
            AiOperation enclosureEscapeOp;
            final Set<Long> exitAttemptColumns;
            final Set<Long> stepOffStances;
            final Set<Long> set_Set;
            boolean tick_Z;
            boolean run_Z;
            boolean get_Z;
            boolean set_Z;
            boolean put_Z;
            String lastSubtaskData;
            AiOperation drainFloorBreakOp;
            final Set<Long> put_Set;
            AiOperation outerScaffoldBreakOp;
            int outerScaffoldBreaks;
            final Set<Long> add_Set;
            boolean add_Z;
            final Map<Long, Set<Long>> sync_Map;
            int breakOutTries;
            int scan;
            int parse;
            final List<AiBuilder.CellSpec> deferredCells;
            final List<String> deferredReasons;
            int deferPassCount;
            boolean cancelled;
            int scaffoldSaveTicks;
            final Map<String, Integer> phaseTicks;
            final long startNanos;
            int lastLoggedPlaced;
            int reserveTickCooldown;
            Map<Item, Integer> reservedItems;
            Set<Long> closedRouteCells;
            Set<Long> avoidPlacementCells;
            int reserveRecountSkips;
            final Set<AiBuilder.CellSpec> failedCells;
            int protectedCacheTick;
            BlockPos protectedCacheWorker;
            Set<Long> protectedCellsCache;
            final Map<AiBuilder.CellSpec, Integer> supportPlanAttempts;
            final Map<AiBuilder.CellSpec, Integer> holdCounts;
            BlockPos holdDependency;
            AiOperation bodyRescueOp;
            int bodyRescueAttempts;
            long approachCacheWorker;
            int approachCacheTick;
            boolean approachCacheResult;
            final Set<Long> ceilingSupportTried;

            {
               this.close_Set.addAll(protectedCells);
               this.load_Map = new HashMap<>();

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  this.load_Map.put(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong(), cell);
               }

               this.existingSupportKeys = new HashSet<>();

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  Block block = AiBuilder.resolveBlock(cell);
                  BlockState requested = AiBuilder.toBlockState(cell);
                  if (!Block.isShapeFullBlock(requested.getCollisionShape(mc.level, at)) || block instanceof FallingBlock) {
                     for (Direction direction : Direction.values()) {
                        if (AiBuilder.allowsSupportOnSide(cell, block, direction)) {
                           BlockPos support = at.relative(direction);
                           if (!mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                              this.existingSupportKeys.add(support.asLong());
                           }
                        }
                     }
                  }
               }

               this.outerScaffoldCleared = schematic;
               this.ownsScaffolding = scaffoldingOwner;
               this.guardLayer = this.ownsScaffolding ? new AiGuardLayer(new AiGuard.GuardPolicy(true, true, true, 16.0F, 7.0F, 14), "schematic construction") : null;
               if (this.guardLayer != null) {
                  AiGuard.suppressBriefly();
               }

               this.scaffoldSupportKeys = new HashSet<>();
               this.read_Map = new HashMap<>();
               this.push_Set = new HashSet<>();
               this.pop_Set = new HashSet<>();
               this.foundationKeys = new HashSet<>();
               this.stairLayerY = blueprint.cells().stream().mapToInt(AiBuilder.CellSpec::dy).min().orElse(0) - 1;
               this.accessStairsReady = true;
               this.terrainCleared = !this.ownsScaffolding || !power.clearTerrain();
               this.scaffoldTracker = this.ownsScaffolding ? AiScaffold.openScope(AiBuilder.checkpointPath(blueprint), List.of()) : null;
               if (this.scaffoldTracker != null) {
                  for (BlockPos step : AiSchematicActions.init(blueprint)) {
                     for (BlockPos support : List.of(step, step.below())) {
                        if (AiScaffold.matches(this.scaffoldTracker.recorded(support), mc.level.getBlockState(support).getBlock())) {
                           this.scaffoldSupportKeys.add(support.asLong());
                        }
                     }
                  }
               }

               this.outerScaffoldCells = new ArrayList<>();
               this.drainSupports = new ArrayList<>();
               this.drainAttemptCounts = new HashMap<>();
               this.drainRetryAt = new HashMap<>();
               this.activeKeep = Set.of();
               this.exitAttemptColumns = new HashSet<>();
               this.stepOffStances = new HashSet<>();
               this.set_Set = new HashSet<>();
               this.put_Set = new HashSet<>();
               this.add_Set = new HashSet<>();
               this.sync_Map = new HashMap<>();
               this.deferredCells = new ArrayList<>();
               this.deferredReasons = new ArrayList<>();
               this.phaseTicks = new TreeMap<>();
               this.startNanos = System.nanoTime();
               this.failedCells = new HashSet<>();
               this.protectedCacheTick = -1;
               this.supportPlanAttempts = new HashMap<>();
               this.holdCounts = new HashMap<>();
               this.approachCacheWorker = -9223372036854775808L;
               this.ceilingSupportTried = new HashSet<>();
            }

            void releaseJobReservations() {
               autismclient.ai.engine.AiRestock.reserveItemsForJob(Map.of());
               autismclient.ai.engine.AiRoutePolicy.setClosedDoors(Set.of());
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(0L);
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousBridgeCompat(0L);
               if (schematic) {
                  autismclient.ai.engine.AiRoutePolicy.putAt_V();
               }

               autismclient.ai.engine.AiPathfinder.MoveCosts.setAvoidPlacementCellsCompat(Set.of());
            }

            void refreshReservations() {
               if (this.reservedItems == null || this.reserveRecountSkips-- <= 0) {
                  this.reserveRecountSkips = blueprint.cells().size() / 8192;
                  HashMap<Item, Integer> counts = new HashMap<>();

                  for (AiBuilder.CellSpec cell : blueprint.cells()) {
                     BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                     if (!AiBuilder.isCellSatisfied(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                        Item item = schematic && cell.block().equals("minecraft:grass_block") ? Items.DIRT : AiBuilder.resolveBlock(cell).asItem();
                        int cost = AiBuilder.itemCountForCell(cell);
                        if (item != Items.AIR && cost > 0) {
                           counts.merge(item, cost, Integer::sum);
                        }
                     }
                  }

                  this.reservedItems = Map.copyOf(counts);
                  HashSet<Long> closed = new HashSet<>();

                  for (AiBuilder.CellSpec cell : blueprint.cells()) {
                     if ("false".equals(cell.properties().get("open"))
                        && AiBuilder.resolveBlock(cell).defaultBlockState().hasProperty(BlockStateProperties.OPEN)) {
                        closed.add(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong());
                     }
                  }

                  this.closedRouteCells = Set.copyOf(closed);
                  if (schematic) {
                     HashSet<Long> empty = new HashSet<>();

                     for (long key : this.load_Map.keySet()) {
                        BlockState live = mc.level.getBlockState(BlockPos.of(key));
                        if (live.canBeReplaced() && live.getFluidState().isEmpty()) {
                           empty.add(key);
                        }
                     }

                     this.avoidPlacementCells = Set.copyOf(empty);
                  }
               }

               autismclient.ai.engine.AiRestock.reserveItemsForJob(this.reservedItems);
               autismclient.ai.engine.AiRoutePolicy.setClosedDoors(this.closedRouteCells);
               autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousParkourCompat(3000000000L);
               if (schematic) {
                  autismclient.ai.engine.AiPathfinder.MoveCosts.enableCautiousBridgeCompat(3000000000L);
               }

               if (schematic) {
                  autismclient.ai.engine.AiRoutePolicy.putAt(this.close_Set::contains);
               }

               if (schematic && this.avoidPlacementCells != null) {
                  autismclient.ai.engine.AiPathfinder.MoveCosts.setAvoidPlacementCellsCompat(this.avoidPlacementCells);
               }
            }

            @Override
            public AiOperation.Result tick() {
               AiFallGuard.markDriven();
               if (schematic
                  && this.ownsScaffolding
                  && mc.player != null
                  && mc.level != null
                  && mc.player.onGround()
                  && mc.player.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) {
                  BlockPos floor = autismclient.ai.engine.AiStance.standingPos(mc).below();
                  AiBuilder.CellSpec source = this.load_Map.get(floor.asLong());
                  if (source != null && AiBuilder.isCellSatisfied(source, mc.level.getBlockState(floor), power.substitutes(), true)) {
                     this.sourceFloorWalkingTicks++;
                  }
               }

               boolean wholePlan = schematic && this.ownsScaffolding;
               AiVisualization.showPlan(this, blueprint.origin(), wholePlan ? blueprint.cells() : this.orderedCells, this.cellCursor, false, wholePlan);
               boolean reserves = this.ownsScaffolding || !schematic;
               if (reserves && mc.level != null && this.reserveTickCooldown-- <= 0) {
                  this.reserveTickCooldown = 20;
                  this.refreshReservations();
               }

               AiOperation.Result r = !this.cancelled && this.guardLayer != null && System.nanoTime() <= this.deadlineNanos
                  ? this.guardLayer
                     .tick(this, this::poll, () -> Math.max(1, (int)((this.deadlineNanos - System.nanoTime()) / 1000000000L)))
                  : null;
               if (r != null && r.terminal()) {
                  this.cancel();
                  return r;
               }

               if (r == null && this.ownsScaffolding && !this.cancelled && mc.player != null && mc.level != null) {
                  this.tickPrefetch();
               }

               r = r == null ? this.tickBuild() : this.running(r.message());
               if (!r.terminal()) {
                  AiVisualization.showCount(
                     this,
                     schematic ? "Building schematic" : "Building",
                     AiTaskStatus.phaseLabel(r.message(), "Preparing the build") + " / " + this.placedCount + " blocks placed",
                     -1,
                     -1
                  );
               }

               if (reserves && r.terminal()) {
                  this.releaseJobReservations();
               }

               if ((holdSneak || this.set_Z) && !r.terminal()) {
                  mc.options.keyShift.setDown(true);
               }

               String m = r.message() == null ? "" : r.message();
               String phase = m.startsWith("Verifying")
                  ? "verify"
                  : (
                     m.startsWith("Approaching")
                        ? "approach"
                        : (
                           m.startsWith("Aiming") || m.startsWith("Placing")
                              ? "place"
                              : (m.isEmpty() ? "other" : m.split("[ :(]", 2)[0].toLowerCase(Locale.ROOT))
                        )
                  );
               this.phaseTicks.merge(phase, 1, Integer::sum);
               if (r.terminal() || this.placedCount >= this.lastLoggedPlaced + 8) {
                  this.lastLoggedPlaced = this.placedCount;
                  AutismClientAddon.LOG
                     .info(
                        "[AI] build {}: placed {}/{} skipped {} failed {} in {} s, reach-first {}, ticks {}",
                        r.terminal() ? r.status() : "progress",
                        this.placedCount,
                        this.orderedCells.size(),
                        this.skippedCount,
                        this.failedTotal,
                        (System.nanoTime() - this.startNanos) / 1000000000L,
                        this.reachFirstCount,
                        this.phaseTicks
                     );
               }

               return r;
            }

            AiOperation.Result tickBuild() {
               if (!this.cancelled && mc.player != null && mc.level != null) {
                  this.pollPlaceAck();
                  if (mc.gui.screen() instanceof AbstractSignEditScreen editor) {
                     this.fillSignEditor(editor);
                  }

                  if (this.scaffoldTracker != null && ++this.scaffoldSaveTicks % 200 == 0) {
                     this.scaffoldTracker.persist();
                  }

                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.finish(
                        AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("building", mc.player.getHealth(), mc.player.getMaxHealth())
                     );
                  }

                  if (System.nanoTime() > this.deadlineNanos) {
                     return this.finish(AiOperation.Status.TIMED_OUT, "Build deadline reached");
                  }

                  if (this.ownsScaffolding) {
                     if (this.bodyRescueOp != null) {
                        AiOperation.Result rescued = this.bodyRescueOp.tick();
                        if (!rescued.terminal()) {
                           return this.running("Freeing the body from a block: " + rescued.message());
                        }

                        this.bodyRescueOp.cancel();
                        this.bodyRescueOp = null;
                     }

                     BlockPos stuck = this.blockInsideBody();
                     if (stuck != null && this.bodyRescueAttempts++ < 20) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] build: the body is inside {} at {}; breaking it",
                              mc.level.getBlockState(stuck).getBlock().getName().getString(),
                              stuck.toShortString()
                           );
                        if (this.activeSubtask != null) {
                           this.activeSubtask.cancel();
                           this.activeSubtask = null;
                        }

                        this.bodyRescueOp = autismclient.ai.engine.AiBreakBlock.breakBlock(stuck, 5, false, Set.of(), false);
                        return this.running("Freeing the body from a block");
                     }
                  }

                  if (!this.terrainCleared) {
                     AiOperation.Result cleared = this.tickTerrainClear();
                     if (cleared != null) {
                        return cleared;
                     }

                     this.terrainCleared = true;
                  }

                  if (!this.accessStairsReady) {
                     if (this.accessStairsOp == null) {
                        this.accessStairsOp = AiSchematicActions.init_AiOperation_TableIndex_DataUti(blueprint, this.cachedProtectedCells(), this.scaffoldSupportKeys, this.stairLayerY);
                     }

                     AiOperation.Result accessResult = this.accessStairsOp.tick();
                     if (!accessResult.terminal()) {
                        return this.running("Preparing construction access: " + accessResult.message());
                     }

                     this.accessStairsOp.cancel();
                     this.accessStairsOp = null;
                     if (accessResult.status() != AiOperation.Status.SUCCEEDED) {
                        AutismClientAddon.LOG.info("[AI] build: construction access stairs failed ({}); building without them", accessResult.message());
                     }

                     this.accessStairsReady = true;
                  }

                  if (this.shulkerRefillOp != null) {
                     AiOperation.Result refill = this.shulkerRefillOp.tick();
                     if (!refill.terminal()) {
                        return this.running("Withdrawing building stock from a nearby shulker box: " + refill.message());
                     }

                     this.shulkerRefillOp.cancel();
                     this.shulkerRefillOp = null;
                     if (refill.status() != AiOperation.Status.UNCERTAIN
                        && refill.status() != AiOperation.Status.CANCELLED
                        && refill.status() != AiOperation.Status.TIMED_OUT) {
                        if (mc.player.containerMenu != mc.player.inventoryMenu) {
                           mc.player.closeContainer();
                           mc.gui.setScreen(null);
                        }

                        boolean moved = false;

                        try {
                           JsonObject data = AiJson.parseObject(refill.dataJson());
                           moved = data.has("moved") && data.get("moved").isJsonObject() && data.getAsJsonObject("moved").size() > 0;
                        } catch (RuntimeException var23) {
                        }

                        if (refill.status() == AiOperation.Status.SUCCEEDED && moved) {
                           this.shulkerRefillCount++;
                           return this.running("Shulker restock finished; resuming the build");
                        } else {
                           AiOperation next = this.nextShulkerRefill(this.orderedCells.get(this.cellCursor));
                           if (next != null) {
                              this.shulkerRefillOp = next;
                              return this.running("Trying the next nearby shulker box");
                           } else {
                              return this.finish(
                                 AiOperation.Status.FAILED,
                                 "No nearby shulker box could supply the needed stock"
                                    + (refill.status() == AiOperation.Status.SUCCEEDED ? "" : ": " + refill.message())
                              );
                           }
                        }
                     } else {
                        JsonObject receipt = this.progressData();
                        receipt.add("shulkerTransfer", AiJson.parseObject(refill.dataJson()));
                        return this.finishWithData(
                           refill.status(), "Shulker transfer did not settle; reconcile inventory before resuming: " + refill.message(), receipt
                        );
                     }
                  } else if (this.cellConstructionOp != null) {
                     AiOperation.Result r = this.cellConstructionOp.tick();
                     if (!r.terminal()) {
                        return this.running("Constructing cell " + (this.cellCursor + 1) + "/" + this.orderedCells.size() + ": " + r.message());
                     }

                     this.cellConstructionOp.cancel();
                     this.cellConstructionOp = null;
                     if (r.status() != AiOperation.Status.UNCERTAIN && r.status() != AiOperation.Status.CANCELLED && r.status() != AiOperation.Status.TIMED_OUT
                        )
                      {
                        if (r.status() == AiOperation.Status.SUCCEEDED) {
                           AiBuilder.CellSpec finished = this.orderedCells.get(this.cellCursor);
                           if (AiBuilder.fluidOfCell(finished) != null || finished.properties().containsKey("waterlogged")) {
                              this.lastFluidPlacementTick = mc.player.tickCount;
                           }

                           this.placedCount++;
                           this.advanceCell();
                        } else if (schematic && this.deferPassCount < 2) {
                           this.deferCurrentCell(r.message());
                        } else {
                           this.failCurrentCell(r.message());
                        }

                        return this.running("Special placement finished");
                     } else {
                        return this.finish(
                           r.status(), "Special placement did not settle; reobserve before resuming: " + r.message()
                        );
                     }
                  } else {
                     if (this.pendingPlacePos != null) {
                        if (this.pendingPlaceBlock instanceof SignBlock && mc.gui.screen() instanceof AbstractSignEditScreen editor) {
                           this.fillSignEditor(editor);
                        }

                        if (this.pendingAimPoint != null) {
                           this.placementAim.aimAt(this.pendingAimPoint);
                           this.placementAim.tick();
                        }

                        BlockState liveState = mc.level.getBlockState(this.pendingPlacePos);
                        boolean matched = liveState.getBlock() == this.pendingPlaceBlock;
                        if (!verify) {
                           if (++this.verifyTicks >= 2) {
                              if (!matched) {
                                 this.abandonPendingPlacement("placement did not appear");
                              } else if (this.init_Z_TableIndex_StreamU_2(this.pendingPlaceCell, this.pendingPlacePos, liveState)
                                 && this.pendingPlacePos != null) {
                                 this.placedCount++;
                                 this.cache = 0;
                                 this.advanceCell();
                              }
                           }
                        } else {
                           this.matchedConfirmations = matched ? this.matchedConfirmations + 1 : 0;
                           boolean answered = AiServerEvidence.blockRevision(this.pendingPlacePos) > this.pendingPlaceRevision;
                           if (this.matchedConfirmations >= (schematic ? 1 : 5) && answered) {
                              if (this.init_Z_TableIndex_StreamU_2(this.pendingPlaceCell, this.pendingPlacePos, liveState) && this.pendingPlacePos != null) {
                                 this.placedCount++;
                                 this.cache = 0;
                                 this.advanceCell();
                              }
                           } else if (schematic && answered && !matched && ++this.verifyTicks > 4) {
                              this.abandonPendingPlacement("the server refused the placement");
                           } else if (++this.verifyTicks > this.verifyTimeoutTicks()) {
                              this.abandonPendingPlacement("no applied server block confirmation");
                           }
                        }

                        if (this.pendingPlacePos != null) {
                           return this.running("Verifying placed block");
                        }

                        if (this.cellConstructionOp != null) {
                           return this.running("Finishing the placed block's fluid state");
                        }
                     }

                     if (this.activeSubtask != null) {
                        if (schematic && this.ownsScaffolding && this.activeSubtask instanceof AiSchematicApproach approach) {
                           approach.bindActions(this::planWalkingPlacement, this::tryWalkingClick, () -> this.placeAckTracker.pendingPos() != null);
                        }

                        if (this.activeApproachJob != null && this.activeSubtask != this.batchApproachOp) {
                           this.activeApproachJob.cancel();
                           this.activeApproachJob = null;
                           this.lastApproachJob = null;
                           this.batchApproachOp = null;
                        }

                        AiOperation.Result r = this.heldSubtaskResult == null ? this.activeSubtask.tick() : this.heldSubtaskResult;
                        if (r.terminal() && this.placeAckTracker.pendingPos() != null) {
                           this.heldSubtaskResult = r;
                           return this.running("Finishing the moving placement acknowledgement");
                        }

                        this.heldSubtaskResult = null;
                        if (Boolean.getBoolean("autism.navigation.debug")) {
                           this.lastSubtaskData = r.dataJson();
                        }

                        if (!r.terminal()) {
                           return this.running("Approaching cell " + (this.cellCursor + 1) + "/" + this.orderedCells.size() + ": " + r.message());
                        }

                        this.activeSubtask = null;
                        if (this.activeApproachJob != null) {
                           this.planningMillis = this.activeApproachJob.elapsedMs();
                           if (r.status() != AiOperation.Status.SUCCEEDED) {
                              this.lastApproachJob = null;
                              BlockPos rejected = this.activeApproachJob.getRejected();
                              if (rejected != null) {
                                 this.rejectedBatchCells.add(rejected.asLong());
                              }
                           } else {
                              BlockPos feet = autismclient.ai.engine.AiStance.standingPos(mc);
                              List<BlockPos> positions = this.activeApproachJob.positionsFrom(feet);
                              ArrayList<AiBuilder.CellSpec> scheduled = new ArrayList<>();

                              for (BlockPos at : positions) {
                                 AiBuilder.CellSpec wanted = this.load_Map.get(at.asLong());
                                 if (wanted != null && !AiBuilder.isCellSatisfied(wanted, mc.level.getBlockState(at), power.substitutes(), true)) {
                                    scheduled.add(wanted);
                                 }
                              }

                              AiBuilder.CellSpec previous = this.cellCursor < this.orderedCells.size() ? this.orderedCells.get(this.cellCursor) : null;
                              AiSchematicApproach.Inner_DataUtil.fillBatch(this.orderedCells, this.cellCursor, scheduled);
                              if (this.cellCursor < this.orderedCells.size() && !this.orderedCells.get(this.cellCursor).equals(previous)) {
                                 this.resetCellState();
                              }

                              this.batchPositions = List.copyOf(positions);
                              this.lastApproachJob = positions.isEmpty() ? null : this.activeApproachJob;
                              this.batchCellCount = scheduled.size();
                              this.itineraryStopCount = this.activeApproachJob.stopCount();
                              this.estimatedSeconds = this.activeApproachJob.estimatedSeconds(feet);
                              this.estimatedSupportBlocks = this.activeApproachJob.supportCount(feet);
                           }

                           this.activeApproachJob = null;
                           this.batchApproachOp = null;
                        }

                        if (r.status() == AiOperation.Status.SUCCEEDED && this.cellCursor < this.orderedCells.size() && AiBuilder.isFlowingFluidCell(this.orderedCells.get(this.cellCursor))
                           )
                         {
                           this.lastFluidPlacementTick = mc.player.tickCount;
                        }

                        boolean noRoute = r.status() != AiOperation.Status.SUCCEEDED
                           && r.message() != null
                           && r.message().startsWith("No route toward the target");
                        BlockPos worker = mc.player.blockPosition();
                        if (noRoute && this.openExit(worker)) {
                           return this.running("Breaking out of the closed room");
                        }

                        if (this.pendingFoundation != null) {
                           BlockPos foundation = this.pendingFoundation;
                           this.pendingFoundation = null;
                           if (r.status() == AiOperation.Status.SUCCEEDED) {
                              AiScaffold.forget(foundation);
                              this.foundationKeys.add(foundation.asLong());
                           } else {
                              this.deferCurrentCell("Required foundation failed: " + r.message());
                           }

                           return this.running("Required foundation finished; re-evaluating the source cell");
                        } else {
                           if (this.run_Z) {
                              this.run_Z = false;
                              if (r.status() == AiOperation.Status.SUCCEEDED) {
                                 return this.running("Construction stance ready; re-evaluating the placement");
                              }

                              this.set_Z = false;
                           }

                           if (this.init_Z) {
                              this.init_Z = false;
                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.deferCurrentCell(r.message());
                              }

                              return this.running("Scaffold construction finished; re-evaluating");
                           } else {
                              if (this.add_Z) {
                                 this.add_Z = false;
                              }

                              this.read_V_2();
                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 if (schematic
                                    && r.message() != null
                                    && (r.message().startsWith("Batch planning") || r.message().startsWith("Construction planning"))) {
                                    this.load_V(this.currentCellPos());
                                    return this.running("Refreshing the bounded batch plan against the changed work site");
                                 }

                                 if (schematic && this.push(this.currentCellPos())) {
                                    return this.running("Building a schematic step before temporary access");
                                 }

                                 if (schematic && r.failure() != AiFailure.PLACEMENT_IMPOSSIBLE && this.load(this.orderedCells.get(this.cellCursor))) {
                                    return this.running("Preparing access to the unreachable construction layer");
                                 }

                                 if (schematic && this.sync(this.orderedCells.get(this.cellCursor), this.currentCellPos())) {
                                    return this.running("Constructing a stance aligned with the requested placement");
                                 }

                                 if (schematic && r.failure() == AiFailure.PLACEMENT_IMPOSSIBLE && this.write(this.orderedCells.get(this.cellCursor), this.currentCellPos())) {
                                    return this.running("Building an accessible support instead of repeating the failed approach");
                                 }

                                 if (!this.sync_Z && r.message() != null && r.message().startsWith("No route toward the target")) {
                                    this.sync_Z = true;
                                    this.cache = 0;
                                    this.activeSubtask = this.climbDownOp();
                                    return this.running("Climbing down after the approach lost its route");
                                 }

                                 if (++this.cache >= 6) {
                                    this.deferCurrentCell("no reachable approach");
                                    return this.running("Approach keeps failing; cell deferred to the post-pass");
                                 }

                                 this.load_V(this.currentCellPos());
                              }

                              return this.running("Approach finished; re-evaluating");
                           }
                        }
                     } else {
                        if (!schematic && this.orderedCells.size() - this.cellCursor <= 8 && this.load_Z_4()) {
                           this.add_Z = true;
                           this.activeSubtask = AiGoto.travelToCompat(blueprint.origin().below().north(3), 1.5, true, false, false, 20);
                           return this.running("Stepping out of the closing structure");
                        }

                        if (!this.outerScaffoldCleared) {
                           AiOperation.Result drained = this.tickOuterScaffoldCleanup();
                           if (drained != null) {
                              return drained;
                           }

                           this.outerScaffoldCleared = true;
                        }

                        if (schematic && this.ownsScaffolding && this.cellCursor < this.orderedCells.size() && !this.put_Z && mc.player.getY() < blueprint.origin().getY()) {
                           Set<Long> keep = this.keepCells();
                           BlockPos worker = mc.player.blockPosition();
                           BlockPos stance = new BlockPos(worker.getX(), blueprint.origin().getY() + 2, worker.getZ());
                           this.put_Z = this.run_Z = true;
                           this.activeSubtask = AiGoto.travelToCompat(
                              stance,
                              0.25,
                              true,
                              true,
                              false,
                              20,
                              autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                              true,
                              2.0,
                              AiGoto.protectingCellsBelowYCompat(
                                 autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(stance, 0.25),
                                 keep,
                                 blueprint.origin().getY() - 2
                              )
                           );
                           return this.running("Returning from below the construction foundation");
                        }

                        int evaluated = 0;
                        int placements = 0;
                        if (this.placeAckTracker.pendingPos() != null) {
                           return this.running("Confirming the last moving placement");
                        }

                        while (
                           this.cellCursor < this.orderedCells.size() && evaluated < 64 && placements < 4 && this.pendingPlacePos == null && this.activeSubtask == null
                        ) {
                           AiBuilder.CellSpec cell = this.orderedCells.get(this.cellCursor);
                           evaluated++;
                           BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                           BlockState live = mc.level.getBlockState(pos);
                           if (!AiTerrain.isLoaded(mc.level, pos)
                              || !AiBuilder.isCellSatisfied(cell, live, power.substitutes(), schematic)
                                 && (
                                    !schematic
                                       || !AiBuilder.matchesIgnoringUnpairedChest(cell, live)
                                       || AiBuilder.isUnpairedDoubleChest(cell, live) && AiBuilder.hasUnpairedChestPartner(cell, pos, mc.level)
                                 )) {
                              if (schematic
                                 && this.ownsScaffolding
                                 && this.activeSubtask == null
                                 && this.cellConstructionOp == null
                                 && !this.batchPositions.contains(pos)
                                 && !this.init_Z_TableIndex_StreamU_BlockPos(cell, pos)) {
                                 int swap = -1;

                                 for (int j = this.cellCursor + 1; j < Math.min(this.orderedCells.size(), this.cellCursor + 96); j++) {
                                    AiBuilder.CellSpec next = this.orderedCells.get(j);
                                    if (next.dy() == cell.dy()) {
                                       BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                                       if (!AiBuilder.isCellSatisfied(next, mc.level.getBlockState(at), power.substitutes(), schematic)
                                          && this.init_Z_TableIndex_StreamU_BlockPos(next, at)) {
                                          swap = j;
                                          break;
                                       }
                                    }
                                 }

                                 if (swap > 0) {
                                    AiBuilder.CellSpec nearby = this.orderedCells.remove(swap);
                                    this.orderedCells.add(this.cellCursor, nearby);
                                    this.currentCellAttempts = this.placeAttempts.getOrDefault(this.currentCellPos().asLong(), 0);
                                    this.currentPlacementPlan = null;
                                    this.start_I = 0;
                                    this.pendingYaw = 0.0F / 0.0F;
                                    this.load_Z = false;
                                    this.reachFirstCount++;
                                    continue;
                                 }
                              }

                              if (schematic && this.ownsScaffolding && this.holdCounts.getOrDefault(cell, 0) < 4) {
                                 String hold = this.holdReason(cell, pos);
                                 if (hold != null && this.deferBehindDependency(cell, hold)) {
                                    continue;
                                 }
                              }

                              if (schematic && !AiBuilder.toBlockState(cell).getCollisionShape(mc.level, pos).isEmpty()) {
                                 BlockPos interior = AiSchematicActions.init(mc.level, pos, this.load_Map);
                                 if (interior != null) {
                                    this.holdDependency = interior;
                                    if (this.holdCounts.getOrDefault(cell, 0) >= 4
                                       || !this.deferBehindDependency(cell, "the ceiling waits for its interior bed")) {
                                       this.deferCurrentCell("the ceiling waits for its interior bed at " + interior.toShortString());
                                    }
                                    continue;
                                 }
                              }

                              if (schematic && this.ownsScaffolding && this.redstoneConflict(cell, pos) != null) {
                                 this.deferCurrentCell(this.redstoneConflict(cell, pos));
                              } else {
                                 if (schematic
                                    && this.ownsScaffolding
                                    && this.constraintFor(cell, AiBuilder.resolveBlock(cell)).crouchForLook()
                                    && mc.level.getBlockState(pos.above()).getCollisionShape(mc.level, pos.above()).isEmpty()
                                    && mc.level.getBlockState(pos).canBeReplaced()) {
                                    AiBuilder.CellSpec above = this.load_Map.get(pos.above().asLong());
                                    if (above != null
                                       && Block.isShapeFullBlock(AiBuilder.toBlockState(above).getCollisionShape(mc.level, pos.above()))
                                       && this.holdCounts.getOrDefault(cell, 0) < 4) {
                                       this.holdDependency = pos.above();
                                       if (this.deferBehindDependency(cell, "an upward-facing part waits for the block above it")) {
                                          continue;
                                       }
                                    }

                                    if (this.placeCeilingSupport(cell, pos)) {
                                       return this.running("Placing a temporary block above an upward-facing part");
                                    }
                                 }

                                 if (schematic) {
                                    BlockPos foundation = AiSchematicActions.foundationFor(cell, pos, mc.level, this.close_Set);
                                    if (foundation != null) {
                                       this.pendingFoundation = foundation;
                                       this.activeSubtask = AiBuilder.buildSchematicSubtask(
                                          new AiBuilder.Blueprint(foundation, List.of(new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"))),
                                          this.cachedProtectedCells()
                                       );
                                       return this.running("Restoring the source cell's permanent foundation");
                                    }

                                    BlockPos dependency = AiSchematicActions.dependencyFor(cell, pos, mc.level, this.load_Map);
                                    if (AiTerrain.isLoaded(mc.level, pos) && (dependency != null || !AiBuilder.toBlockState(cell).canSurvive(mc.level, pos))) {
                                       if (dependency != null && this.holdCounts.getOrDefault(cell, 0) < 4) {
                                          boolean later = false;

                                          for (int j = this.cellCursor + 1; j < this.orderedCells.size(); j++) {
                                             AiBuilder.CellSpec next = this.orderedCells.get(j);
                                             if (blueprint.origin().offset(next.dx(), next.dy(), next.dz()).equals(dependency)) {
                                                later = true;
                                                break;
                                             }
                                          }

                                          this.holdDependency = dependency;
                                          if (later && this.deferBehindDependency(cell, "waiting for its native survival support")) {
                                             continue;
                                          }
                                       }

                                       this.deferCurrentCell("waiting for native survival support" + (dependency == null ? "" : " at " + dependency.toShortString()));
                                       continue;
                                    }
                                 }

                                 if (schematic && AiBuilder.fluidOfCell(cell) != null) {
                                    if (AiBuilder.isFlowingFluidCell(cell)) {
                                       if (!live.canBeReplaced() && live.getFluidState().isEmpty()) {
                                          if (!live.hasBlockEntity()) {
                                             HashSet<BlockPos> plannedWater = new HashSet<>();

                                             for (Direction side : Direction.values()) {
                                                BlockPos neighbor = pos.relative(side);
                                                AiBuilder.CellSpec wanted = this.load_Map.get(neighbor.asLong());
                                                if (wanted != null
                                                   && (AiBuilder.fluidOfCell(wanted) != null || "true".equals(wanted.properties().get("waterlogged")))) {
                                                   plannedWater.add(neighbor);
                                                }
                                             }

                                             this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(pos, 30, true, this.cachedProtectedCells(), true, plannedWater);
                                             return this.running("Clearing the requested flowing-water channel");
                                          }

                                          this.failCurrentCell("flowing water channel occupied by protected contents");
                                          continue;
                                       }

                                       if (mc.player.tickCount - this.lastFluidPlacementTick < 100L) {
                                          autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                          return this.running("Waiting for water to propagate from the verified source cells");
                                       }

                                       this.deferCurrentCell("waiting for surrounding source cells to produce flowing water");
                                       continue;
                                    }

                                    String leak = AiSchematicActions.init(pos, mc.level, this.load_Map);
                                    if (leak != null) {
                                       this.deferCurrentCell(leak);
                                       continue;
                                    }

                                    if (!AiBuildIntent.isSatisfied(pos, mc.level, this.load_Map)) {
                                       this.deferCurrentCell("waiting for the fluid volume to be held");
                                       continue;
                                    }
                                 }

                                 if (schematic && AiSchematicActions.needsAction(cell)) {
                                    if (this.pop(pos)) {
                                       return this.running("Opening access to the buried interaction cell");
                                    }

                                    this.cellConstructionOp = AiSchematicActions.load(cell, pos, this.cachedProtectedCells());
                                    return this.running("Constructing a survival interaction cell");
                                 }

                                 if (schematic
                                    && this.task < 20
                                    && live.canBeReplaced()
                                    && mc.player.blockPosition().getX() == pos.getX()
                                    && mc.player.blockPosition().getZ() == pos.getZ()
                                    && mc.player.getY() >= pos.getY()
                                    && mc.player.getY() < pos.getY() + 1
                                    && AiBuilder.matchesExactCell(cell, AiBuilder.resolveBlock(cell).defaultBlockState())
                                    && Block.isShapeFullBlock(AiBuilder.toBlockState(cell).getCollisionShape(mc.level, pos))
                                    && !mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()).isEmpty()
                                    && mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()).max(Axis.Y) >= 1.0
                                    && mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(0.0, 1.42, 0.0))) {
                                    int itemSlot = AiItems.equipHotbar(AiItems.resolveItem(cell.block()));
                                    if (itemSlot >= 0) {
                                       this.task++;
                                       autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                       this.placementAim.aimAt(Vec3.atCenterOf(pos.below()));
                                       this.placementAim.tick();
                                       mc.options.keyJump.setDown(mc.player.onGround() || mc.player.isInWater());
                                       return this.running("Jumping to backfill the access shaft with " + cell.block());
                                    }
                                 }

                                 if (this.task > 0) {
                                    mc.options.keyJump.setDown(false);
                                 }

                                 if (schematic && live.getBlock() == AiBuilder.resolveBlock(cell) && !live.canBeReplaced()) {
                                    if (AiBuilder.needsWaterlogToggleOnly(cell, live)) {
                                       this.cellConstructionOp = AiSchematicActions.init(cell, pos, this.cachedProtectedCells());
                                       return this.running("Restoring the placed block's waterlogging");
                                    }

                                    boolean toggleOnly = true;

                                    for (Entry<String, String> entry : cell.properties().entrySet()) {
                                       Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock()
                                          .getStateDefinition()
                                          .getProperty(entry.getKey());
                                       if (property != null
                                          && !String.valueOf(live.getValue(property)).equals(entry.getValue())
                                          && !AiBuilder.isAutoComputedProperty(live.getBlock(), entry.getKey())
                                          && AiBuilder.orientationKindForProperty(live.getBlock(), entry.getKey()) != AiBuilder.OrientationKind.POST_INTERACT) {
                                          toggleOnly = false;
                                       }
                                    }

                                    if (toggleOnly) {
                                       this.cellConstructionOp = AiSchematicActions.init(cell, pos, this.cachedProtectedCells());
                                       return this.running("Restoring the placed block's interaction state");
                                    }

                                    if (!AiBuilder.isDoubleSlabCell(cell)) {
                                       this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(pos, 30, true, this.cachedProtectedCells(), true);
                                       return this.running("Recovering a block with conflicting placement properties");
                                    }
                                 }

                                 if (schematic
                                    && AiBuilder.resolveBlock(cell) instanceof VegetationBlock
                                    && !AiBuilder.toBlockState(cell).canSurvive(mc.level, pos)) {
                                    this.deferCurrentCell("waiting for the soil under the plant");
                                 } else {
                                    if (schematic) {
                                       Block targetBlock = AiBuilder.resolveBlock(cell);
                                       boolean supportReady = AiBuilder.isDoubleSlabCell(cell) && live.getBlock() == targetBlock;

                                       for (Direction direction : Direction.values()) {
                                          if (AiBuilder.allowsSupportOnSide(cell, targetBlock, direction)
                                             && AiBuilder.hasSolidSupportShape(mc.level, pos.relative(direction))) {
                                             supportReady = true;
                                          }
                                       }

                                       if (!supportReady) {
                                          boolean planned = false;

                                          for (Direction direction : Direction.values()) {
                                             planned |= AiBuilder.allowsSupportOnSide(cell, targetBlock, direction)
                                                && this.close_Set.contains(pos.relative(direction).asLong());
                                          }

                                          if ((!planned || this.deferPassCount > 0) && this.write(cell, pos)) {
                                             return this.running("Constructing a temporary support for the floating cell");
                                          }

                                          if (!planned || this.deferPassCount != 0 || !this.planSupportPlacement(cell, pos, targetBlock)) {
                                             this.deferCurrentCell("waiting for a support face that produces the requested state");
                                          }
                                          continue;
                                       }

                                       if (live.getBlock() != targetBlock && !live.canBeReplaced() && live.getFluidState().isEmpty()) {
                                          if (live.hasBlockEntity()) {
                                             this.failCurrentCell("target occupied by a block entity");
                                          } else {
                                             if (++this.scan <= 3) {
                                                this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(pos, 30, true, this.cachedProtectedCells(), true);
                                                return this.running("Clearing a conflicting block at the schematic cell");
                                             }

                                             this.deferCurrentCell("placement keeps producing " + BuiltInRegistries.BLOCK.getKey(live.getBlock()));
                                          }
                                          continue;
                                       }

                                       BlockPos requiredSupport = null;
                                       if (targetBlock instanceof LanternBlock) {
                                          requiredSupport = "true".equals(cell.properties().get("hanging")) ? pos.above() : pos.below();
                                       } else if (AiBuilder.isWallMountedBlock(targetBlock)) {
                                          Direction facing = this.parseDirection(cell.properties().getOrDefault("facing", "north"));
                                          if (facing != null) {
                                             requiredSupport = pos.relative(facing.getOpposite());
                                          }
                                       }

                                       if (requiredSupport != null
                                          && this.close_Set.contains(requiredSupport.asLong())
                                          && mc.level.getBlockState(requiredSupport).isAir()) {
                                          this.deferCurrentCell("waiting for the schematic support cell");
                                          continue;
                                       }
                                    }

                                    Block target = null;
                                    Item item = null;
                                    boolean sourced = false;
                                    boolean grassCell = schematic && cell.block().equals("minecraft:grass_block");
                                    List<String> candidates = grassCell ? List.of("minecraft:dirt") : power.candidates(cell.block());
                                    Iterator var111 = candidates.iterator();

                                    while (true) {
                                       if (var111.hasNext()) {
                                          String candidateId = (String)var111.next();
                                          Block candidate = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(candidateId)).orElse(null);
                                          if (candidate == null) {
                                             continue;
                                          }

                                          Item candidateItem = candidate.asItem();
                                          if (target == null) {
                                             target = candidate;
                                             item = candidateItem;
                                          }

                                          if (candidateItem == Items.AIR) {
                                             continue;
                                          }

                                          boolean candidateUpgrade = live.getBlock() == candidate
                                             && AiBuilder.isDoubleSlabCell(cell)
                                             && !"double".equals(String.valueOf(live.getValue(SlabBlock.TYPE)));
                                          if (this.countInInventory(candidateItem) < (candidateUpgrade ? 1 : AiBuilder.itemCountForCell(cell))) {
                                             continue;
                                          }

                                          target = candidate;
                                          item = candidateItem;
                                          sourced = true;
                                       }

                                       if (grassCell && !sourced) {
                                          target = Blocks.DIRT;
                                          item = Items.DIRT;
                                       }

                                       if (target == null) {
                                          this.failCurrentCell("block id is not resolvable");
                                       } else {
                                          if (item != Items.AIR) {
                                             boolean upgrade = live.getBlock() == target
                                                && AiBuilder.isDoubleSlabCell(cell)
                                                && !"double".equals(String.valueOf(live.getValue(SlabBlock.TYPE)));
                                             if (upgrade && live.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                                                this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(
                                                   pos, 20, true, schematic ? this.cachedProtectedCells() : Set.of(), true
                                                );
                                                return this.running("Recovering a top slab before the double-slab merge");
                                             }

                                             if (!sourced) {
                                                if (schematic && ++this.entry <= 20) {
                                                   autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                                   return this.running("Awaiting material: " + BuiltInRegistries.ITEM.getKey(item));
                                                }

                                                this.entry = 0;
                                                if (schematic && power.shulkerStock()) {
                                                   AiOperation refill = this.nextShulkerRefill(cell);
                                                   if (refill != null) {
                                                      this.shulkerRefillOp = refill;
                                                      return this.running("Withdrawing building stock from a nearby shulker box");
                                                   }
                                                }

                                                return this.finishWithData(
                                                   AiOperation.Status.FAILED,
                                                   "Missing material: " + BuiltInRegistries.ITEM.getKey(item),
                                                   this.materialFailureData("missing stock")
                                                );
                                             }

                                             this.entry = 0;
                                             if (AiTerrain.isLoaded(mc.level, pos)
                                                && (
                                                   schematic
                                                      ? !AiBuilder.collidesWithEntityBox(cell, pos, mc.player.getBoundingBox(), mc.level)
                                                      : !mc.player.getBoundingBox().intersects(this.buildBounds)
                                                )) {
                                                if (schematic && this.read(cell, pos)) {
                                                   if (this.openExit(mc.player.blockPosition())) {
                                                      return this.running("Opening access out of the closed room");
                                                   }

                                                   this.load_V(pos);
                                                   return this.running("Leaving the room " + pos.toShortString() + " closes");
                                                }

                                                if (mc.player.containerMenu != mc.player.inventoryMenu) {
                                                   if (schematic) {
                                                      mc.player.closeContainer();
                                                      mc.gui.setScreen(null);
                                                   }

                                                   this.abandonPendingPlacement("a container screen is open");
                                                   return this.running("Container screen blocks item selection");
                                                }

                                                String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
                                                if (autismclient.util.InventorySlotHelper.ensureItemInHotbar(mc, MacroItemTarget.ofRegistryId(itemId), mc.player.getInventory().getSelectedSlot()) < 0) {
                                                   if (schematic && ++this.node <= 20) {
                                                      autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                                      return this.running("Awaiting material: selecting " + itemId);
                                                   }

                                                   return this.finishWithData(
                                                      AiOperation.Status.FAILED,
                                                      "Could not select material: " + itemId,
                                                      this.materialFailureData("item selection refused")
                                                   );
                                                }

                                                this.node = 0;
                                                boolean placementSneak = schematic
                                                   && (
                                                      holdSneak
                                                         || this.set_Z
                                                         || autismclient.ai.engine.PlacementGoal.canPlace(
                                                            mc, pos, item, this.constraintFor(cell, target)
                                                         )
                                                   );
                                                if (schematic) {
                                                   mc.options.keyShift.setDown(placementSneak);
                                                   if (placementSneak
                                                         && (
                                                            !mc.player.isShiftKeyDown()
                                                               || mc.player.getEyeHeight() > mc.player.getEyeHeight(Pose.CROUCHING) + 0.01
                                                         )
                                                      || !placementSneak && mc.player.isShiftKeyDown()) {
                                                      this.currentPlacementPlan = null;
                                                      return this.running("Settling the placement pose before solving the click");
                                                   }
                                                }

                                                for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                   if (AiBuilder.orientationKindForProperty(target, entry.getKey()) == AiBuilder.OrientationKind.UNSUPPORTED
                                                      && (
                                                         !(target instanceof LeavesBlock)
                                                            || !entry.getKey().equals("persistent")
                                                            || !entry.getValue().equals("true")
                                                      )
                                                      && (!schematic || !AiBuilder.isAutoComputedProperty(target, entry.getKey()))) {
                                                      Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)target.getStateDefinition()
                                                         .getProperty(entry.getKey());
                                                      if (property == null
                                                         || !String.valueOf(target.defaultBlockState().getValue(property)).equalsIgnoreCase(entry.getValue())) {
                                                         this.recordUnappliedProperty(cell, pos, entry, "not achievable via vanilla placement");
                                                      }
                                                   }
                                                }

                                                BlockHitResult hit;
                                                Direction hitFace;
                                                if (upgrade) {
                                                   hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
                                                   hitFace = Direction.UP;
                                                } else {
                                                   Vec3 eye = mc.player.getEyePosition();
                                                   double reach = mc.player.blockInteractionRange();
                                                   BlockPlacementPlanner.Query request = new BlockPlacementPlanner.Query(
                                                         pos, eye, reach, BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND)
                                                      )
                                                      .withSneaking(mc.player.isShiftKeyDown())
                                                      .withAutoSneak(schematic)
                                                      .withRayBudget(new BlockPlacementPlanner.RayBudget(4));
                                                   BlockPlacementPlanner.PlacementPlan candidate = this.currentPlacementPlan != null
                                                      ? this.currentPlacementPlan
                                                      : (
                                                         schematic
                                                            ? autismclient.ai.engine.PlacementGoal.planPlacement(
                                                               mc, pos, item, this.constraintFor(cell, target), 72
                                                            )
                                                            : BlockPlacementPlanner.plan(request)
                                                      );
                                                   if (candidate == null) {
                                                      this.abandonPendingPlacement("no reachable support face");
                                                      return this.running("No reachable support face; re-approaching");
                                                   }

                                                   if (!AiBuilder.clickFaceSatisfiesHalf(AiBuilder.requiresUpperHalfClick(cell, target), candidate.hit().getDirection())) {
                                                      BlockPlacementPlanner.PlacementPlan side = BlockPlacementPlanner.plan(
                                                         new BlockPlacementPlanner.Query(
                                                               pos, eye, reach, BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND)
                                                            )
                                                            .withSneaking(mc.player.isShiftKeyDown())
                                                            .withAutoSneak(schematic)
                                                            .withAllowedFaces(
                                                               EnumSet.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)
                                                            )
                                                            .withRayBudget(new BlockPlacementPlanner.RayBudget(4))
                                                      );
                                                      if (side != null) {
                                                         candidate = side;
                                                      }
                                                   }

                                                   hit = this.orientPlacementHit(cell, pos, target, candidate.hit());
                                                   if (!BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND).lands(hit, pos)) {
                                                      this.abandonPendingPlacement("shaped click does not land on the target cell");
                                                      return this.running("Re-solving the placement click");
                                                   }

                                                   hitFace = hit.getDirection();
                                                }

                                                if (schematic) {
                                                   BlockState supportState = mc.level.getBlockState(hit.getBlockPos());
                                                   boolean secondary = placementSneak
                                                      || supportState.hasBlockEntity()
                                                      || BlockPlacementPlanner.requiresSneakToPlace(supportState, hit.getBlockPos())
                                                      || target instanceof ChestBlock
                                                      || supportState.getBlock() instanceof DoorBlock
                                                      || supportState.getBlock() instanceof TrapDoorBlock
                                                      || supportState.getBlock() instanceof FenceGateBlock;
                                                   mc.options.keyShift.setDown(secondary);
                                                   if (secondary && !mc.player.isShiftKeyDown()) {
                                                      this.currentPlacementPlan = null;
                                                      return this.running("Preparing to place against the support without activating it");
                                                   }
                                                }

                                                if (!Float.isNaN(this.pendingYaw)) {
                                                   autismclient.util.RotationMath.Rotation hitRotation = autismclient.util.RotationMath.lookAt(
                                                      hit.getLocation(), mc.player.getEyePosition()
                                                   );
                                                   float goalYaw = Math.abs(
                                                            autismclient.util.RotationMath.angularDistance(
                                                               hitRotation, new autismclient.util.RotationMath.Rotation(this.pendingYaw, hitRotation.pitch())
                                                            )
                                                         )
                                                         <= this.yawTolerance
                                                      ? hitRotation.yaw()
                                                      : this.pendingYaw;
                                                   this.placementAim.setTargetRotation(new autismclient.util.RotationMath.Rotation(goalYaw, hitRotation.pitch()));
                                                   this.placementAim.tick();
                                                } else {
                                                   AiGameOperations.aimAndStep(this.placementAim, hit.getLocation());
                                                }

                                                if (!this.placementAim.isSettled()
                                                   || autismclient.ai.engine.AiAim.lookedBlockFace(hit.getBlockPos()) != hitFace
                                                   || !Float.isNaN(this.pendingYaw)
                                                      && Math.abs(
                                                            autismclient.util.RotationMath.angularDistance(
                                                               autismclient.util.RotationMath.ofPlayer(mc.player),
                                                               new autismclient.util.RotationMath.Rotation(this.pendingYaw, mc.player.getXRot())
                                                            )
                                                         )
                                                         > this.yawTolerance
                                                   || !Float.isNaN(this.pendingYaw) && !init_Z_Minecraft(mc, this.pendingYaw, this.yawTolerance)) {
                                                   if (++this.start_I > 30) {
                                                      this.start_I = 0;
                                                      if (!Float.isNaN(this.pendingYaw) && !this.load_Z) {
                                                         this.load_Z = true;
                                                         Direction look = Direction.fromYRot(this.pendingYaw);
                                                         this.currentPlacementPlan = null;
                                                         if (schematic) {
                                                            this.load_V(pos);
                                                         } else {
                                                            this.activeSubtask = AiGoto.travelToCompat(
                                                               AiGoto.findStandableNearCompat(pos.relative(look.getOpposite(), 3), 8), 0.6, true, false, false, 25
                                                            );
                                                         }

                                                         return this.running(
                                                            "Re-approaching from the " + look.getOpposite().getName() + " side for facing"
                                                         );
                                                      }

                                                      if (!Float.isNaN(this.pendingYaw)) {
                                                         for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                            AiBuilder.OrientationKind strategy = AiBuilder.orientationKindForProperty(target, entry.getKey());
                                                            if (strategy == AiBuilder.OrientationKind.PLAYER_FACING || strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE) {
                                                               this.recordUnappliedProperty(cell, pos, entry, "player facing could not be aligned with the support ray");
                                                            }
                                                         }

                                                         this.abandonPendingPlacement("player facing could not be aligned with the support ray");
                                                      } else {
                                                         this.currentPlacementPlan = null;
                                                         this.abandonPendingPlacement("could not aim at the support face");
                                                      }
                                                   }

                                                   return this.running("Turning toward " + pos.toShortString());
                                                }

                                                if (schematic
                                                   ? !AiBuilder.collidesWithEntityBox(cell, pos, mc.player.getBoundingBox(), mc.level)
                                                   : !mc.player.getBoundingBox().intersects(new AABB(pos))) {
                                                   this.currentPlacementPlan = null;
                                                   this.start_I = 0;
                                                   if (!init_Z_Minecraft_Item(mc, item)) {
                                                      return this.running("Waiting for the selected construction item to reach the server");
                                                   }

                                                   this.pendingPlaceRevision = AiServerEvidence.blockRevision(pos);
                                                   this.pendingPlacePos = pos;
                                                   this.pendingPlaceBlock = target;
                                                   this.pendingPlaceCell = cell;
                                                   this.matchedConfirmations = 0;
                                                   this.verifyTicks = 0;
                                                   this.pendingAimPoint = hit.getLocation();
                                                   BlockState predicted = target.getStateForPlacement(
                                                      new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, new ItemStack(item), hit)
                                                   );
                                                   boolean stateMatches = predicted != null;
                                                   if (predicted != null) {
                                                      for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                         AiBuilder.OrientationKind strategy = AiBuilder.orientationKindForProperty(target, entry.getKey());
                                                         if (strategy != AiBuilder.OrientationKind.UNSUPPORTED
                                                            && strategy != AiBuilder.OrientationKind.POST_INTERACT
                                                            && strategy != AiBuilder.OrientationKind.FLUID_INTERACT
                                                            && !AiBuilder.isAutoComputedProperty(target, entry.getKey())
                                                            && (!AiBuilder.isDoubleSlabCell(cell) || upgrade || !entry.getKey().equals("type"))
                                                            && (
                                                               strategy != AiBuilder.OrientationKind.CHEST_PAIR
                                                                  || !AiBuilder.isUnpairedDoubleChest(cell, predicted)
                                                                  || AiBuilder.hasUnpairedChestPartner(cell, pos, mc.level)
                                                            )) {
                                                            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)predicted.getBlock()
                                                               .getStateDefinition()
                                                               .getProperty(entry.getKey());
                                                            if (property == null
                                                               || !AiBuilder.propertyValuesMatch(
                                                                  predicted.getBlock(),
                                                                  entry.getKey(),
                                                                  String.valueOf(predicted.getValue(property)),
                                                                  entry.getValue()
                                                               )) {
                                                               stateMatches = false;
                                                            }
                                                         }
                                                      }
                                                   }

                                                   if (!stateMatches) {
                                                      this.abandonPendingPlacement("placement context would produce the wrong block state");
                                                      return this.running("Re-solving the requested block state");
                                                   }

                                                   InteractionResult clickResult = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                                                   if (clickResult == InteractionResult.FAIL) {
                                                      if (Boolean.getBoolean("autism.navigation.debug")) {
                                                         AutismClientAddon.LOG
                                                            .info(
                                                               "[AI] build: click for {} at {} refused: cell {}, support {} {}, feet {}",
                                                               cell.block(),
                                                               pos.toShortString(),
                                                               mc.level.getBlockState(pos),
                                                               hit.getBlockPos().toShortString(),
                                                               hit.getDirection(),
                                                               mc.player.position()
                                                            );
                                                      }

                                                      this.abandonPendingPlacement("placement click refused");
                                                      return this.running("Placement click refused; re-evaluating");
                                                   }

                                                   if (Boolean.getBoolean("autism.ai.dbg")) {
                                                      System.out
                                                         .println(
                                                            "[AI dbg] place cell="
                                                               + cell.block()
                                                               + "@"
                                                               + pos
                                                               + " hand="
                                                               + mc.player.getMainHandItem().getItem()
                                                               + " hit="
                                                               + hit.getBlockPos()
                                                               + "/"
                                                               + hitFace
                                                               + " loc="
                                                               + hit.getLocation()
                                                               + " player="
                                                               + mc.player.blockPosition()
                                                               + " result="
                                                               + clickResult
                                                         );
                                                   }

                                                   placements++;
                                                   return this.running(
                                                      "Placed " + cell.block() + " at " + pos.toShortString() + "; verifying"
                                                   );
                                                }

                                                this.activeSubtask = schematic
                                                   ? AiGoto.approachSchematicPlacementCompat(pos, item, 10, this.buildBounds, this.cachedProtectedCells())
                                                   : AiGoto.approachToPlaceCompat(pos, item, 10, this.buildBounds);
                                                return this.running("Stepping out of the placement cell");
                                             }

                                             this.load_V(pos);
                                             return this.running("Moving into reach of " + pos.toShortString());
                                          }

                                          this.failCurrentCell("block has no item form and cannot be placed");
                                       }
                                       break;
                                    }
                                 }
                              }
                           } else {
                              if (!this.write_Set.remove(pos.asLong())) {
                                 this.skippedCount++;
                              }

                              this.cache = 0;
                              this.advanceCell();
                           }
                        }

                        if (this.cellCursor < this.orderedCells.size()) {
                           return this.running("Building " + this.cellCursor + "/" + this.orderedCells.size());
                        }

                        if (!this.deferredCells.isEmpty()) {
                           this.logDeferredSummary();
                        }

                        if (!this.deferredCells.isEmpty() && this.deferPassCount < 2) {
                           this.placeAttempts.clear();
                           this.read_Set.clear();
                           this.orderedCells.addAll(this.deferredCells);
                           this.deferredCells.clear();
                           this.deferredReasons.clear();
                           this.deferPassCount++;
                           return this.running("Re-attempting deferred cells against the grown structure");
                        }

                        for (int i = 0; i < this.deferredCells.size(); i++) {
                           this.recordCellFailure(this.deferredCells.get(i), this.deferredReasons.get(i));
                        }

                        this.deferredCells.clear();
                        this.deferredReasons.clear();
                        AiOperation.Result finalDrain = (!this.sweepActive || this.terrainBreakOp == null) && this.auditRepairOp == null
                           ? this.tickSupportDrain()
                           : null;
                        if (finalDrain != null) {
                           return finalDrain;
                        }

                        int audited = 0;

                        while (this.auditCursor < this.orderedCells.size() && audited++ < (schematic ? 128 : 2147483647)) {
                           AiBuilder.CellSpec audit = this.orderedCells.get(this.auditCursor++);
                           BlockPos auditPos = blueprint.origin().offset(audit.dx(), audit.dy(), audit.dz());
                           BlockState auditLive = mc.level.getBlockState(auditPos);
                           if (AiBuilder.isCellSatisfied(audit, auditLive, power.substitutes(), schematic)) {
                              if (this.failedCells.remove(audit)) {
                                 this.failedTotal--;
                                 this.failureEntries
                                    .removeIf(
                                       entryx -> {
                                          JsonObject value = entryx.getAsJsonObject();
                                          return value.get("dx").getAsInt() == audit.dx()
                                             && value.get("dy").getAsInt() == audit.dy()
                                             && value.get("dz").getAsInt() == audit.dz();
                                       }
                                    );
                              }
                           } else if (schematic && this.auditPassCount < 2 && !this.repairCells.contains(audit) && !AiBuilder.differsOnlyInAutoProperties(audit, auditLive)) {
                              this.repairCells.add(audit);
                           } else {
                              this.recordCellFailure(audit, "divergent after verify: " + BuiltInRegistries.BLOCK.getKey(auditLive.getBlock()));
                           }
                        }

                        if (this.auditCursor < this.orderedCells.size()) {
                           return this.running("Verifying finished schematic " + this.auditCursor + "/" + this.orderedCells.size());
                        }

                        if (!this.repairCells.isEmpty()) {
                           TreeMap<String, Integer> why = new TreeMap<>();

                           for (AiBuilder.CellSpec repair : this.repairCells) {
                              BlockState live = mc.level.getBlockState(blueprint.origin().offset(repair.dx(), repair.dy(), repair.dz()));
                              why.merge(
                                 repair.block().replace("minecraft:", "") + " <- " + BuiltInRegistries.BLOCK.getKey(live.getBlock()).getPath(), 1, Integer::sum
                              );
                           }

                           AutismClientAddon.LOG.info("[AI] build audit {}: {} cells to repair {}", this.auditPassCount, this.repairCells.size(), why);
                           this.read_Map.clear();
                           this.push_Set.clear();
                           this.orderedCells.addAll(this.repairCells);
                           this.repairCells.clear();
                           this.auditPassCount++;
                           this.auditCursor = 0;
                           if (schematic) {
                              this.pop_Z = false;
                              this.drainSteps = 0;
                           }

                           return this.running("Repairing block states changed during construction");
                        } else {
                           if (this.ownsScaffolding && !this.includeCheckpointSupports) {
                              this.includeCheckpointSupports = true;
                              this.pop_Z = false;
                              this.drainSteps = 0;
                              this.pop_Set.clear();
                              this.read_Map.clear();
                              this.push_Set.clear();
                              this.auditPassCount = 0;
                              this.stepOffStances.clear();
                              this.put_Set.clear();
                              this.auditCursor = 0;
                              return this.running("Removing access stairs after the construction audit");
                           }

                           if (schematic && this.ownsScaffolding && power.clearTerrain() && this.sweepPassCount < 3) {
                              if (!this.sweepActive) {
                                 this.sweepActive = true;
                                 this.terrainToClear = null;
                                 this.terrainClearIndex = 0;
                                 this.terrainLeftCount = 0;
                              }

                              AiOperation.Result swept = this.tickTerrainClear();
                              if (swept != null) {
                                 return swept;
                              }

                              this.sweepActive = false;
                              this.sweepPassCount++;
                              this.pop_Z = false;
                              this.drainSteps = 0;
                              this.stepOffStances.clear();
                              this.put_Set.clear();
                              if (this.terrainToClear.size() - this.terrainLeftCount > 0) {
                                 AutismClientAddon.LOG
                                    .info("[AI] schematic sweep {}: {} leftover blocks cleared out of the air", this.sweepPassCount, this.terrainToClear.size() - this.terrainLeftCount);
                                 this.auditCursor = 0;
                                 return this.running("Verifying the schematic after clearing leftover blocks");
                              }

                              this.sweepPassCount = 3;
                           }

                           if (this.auditRepairOp != null) {
                              AiOperation.Result r = this.auditRepairOp.tick();
                              if (!r.terminal()) {
                                 return this.running("Closing a door left open: " + r.message());
                              }

                              this.auditRepairOp.cancel();
                              this.auditRepairOp = null;
                              AutismClientAddon.LOG.info("[AI] build: closing a door {}: {}", r.status(), r.message());
                              this.auditCursor = 0;
                              return this.running("Door closed; verifying the schematic again");
                           } else {
                              if (schematic && this.ownsScaffolding) {
                                 for (AiBuilder.CellSpec cell : blueprint.cells()) {
                                    if ("false".equals(cell.properties().get("open")) && !"upper".equals(cell.properties().get("half"))) {
                                       BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                                       BlockState live = mc.level.getBlockState(pos);
                                       if (live.getBlock() == AiBuilder.resolveBlock(cell)
                                          && live.hasProperty(BlockStateProperties.OPEN)
                                          && live.getValue(BlockStateProperties.OPEN)
                                          && this.flush_Set.add(pos.asLong())) {
                                          this.auditRepairOp = AiSchematicActions.init(cell, pos, this.cachedProtectedCells());
                                          return this.running(
                                             "Closing " + BuiltInRegistries.BLOCK.getKey(live.getBlock()).getPath() + " at " + pos.toShortString()
                                          );
                                       }
                                    }
                                 }
                              }

                              int supportsLeft = this.ownsScaffolding ? this.ownedScaffoldCells().size() : 0;
                              return this.finish(
                                 this.failedTotal == 0 && supportsLeft == 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                 this.failedTotal != 0
                                    ? "Blueprint incomplete: " + this.failedTotal + " failed cells"
                                    : (
                                       supportsLeft != 0
                                          ? "Blocks complete; " + supportsLeft + " temporary supports could not be removed"
                                          : "Blueprint complete"
                                    )
                              );
                           }
                        }
                     }
                  }
               } else {
                  return this.finish(AiOperation.Status.CANCELLED, "Build cancelled");
               }
            }

            static boolean init_Z_Minecraft(Minecraft mc, float pendingYaw, double tolerance) {
               try {
                  IntegratedServer server = mc.getSingleplayerServer();
                  if (server == null) {
                     return true;
                  } else {
                     ServerPlayer sp = server.getPlayerList().getPlayer(mc.player.getUUID());
                     if (sp == null) {
                        return true;
                     } else {
                        return tolerance >= 40.0
                           ? Direction.fromYRot(sp.getYRot()) == Direction.fromYRot(pendingYaw)
                           : Math.abs(Mth.wrapDegrees(sp.getYRot() - pendingYaw)) <= 11.0F;
                     }
                  }
               } catch (Throwable t) {
                  return true;
               }
            }

            static boolean init_Z_Minecraft_Item(Minecraft mc, Item item) {
               if (!mc.player.getMainHandItem().is(item)) {
                  return false;
               }

               IntegratedServer server = mc.getSingleplayerServer();
               if (server == null) {
                  return true;
               }

               ServerPlayer player = server.getPlayerList().getPlayer(mc.player.getUUID());
               return player == null || player.getMainHandItem().is(item);
            }

            void abandonPendingPlacement(String reason) {
               this.pendingPlacePos = null;
               this.pendingAimPoint = null;
               this.currentPlacementPlan = null;
               this.start_I = 0;
               this.pendingYaw = 0.0F / 0.0F;
               this.stop_I = 0;
               this.read_Z = false;
               this.next_I = 0;
               this.init_Str = null;
               this.prev = 0;
               this.triedShulkerSources.clear();
               if (this.activeSubtask != null) {
                  this.activeSubtask.cancel();
                  this.activeSubtask = null;
               }

               if (schematic) {
                  BlockState live = mc.level.getBlockState(this.currentCellPos());
                  if (live.getBlock() != AiBuilder.resolveBlock(this.orderedCells.get(this.cellCursor))
                     && !live.canBeReplaced()
                     && live.getFluidState().isEmpty()
                     && !live.hasBlockEntity()) {
                     this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(this.currentCellPos(), 30, true, this.cachedProtectedCells(), true);
                     return;
                  }
               }

               if (this.hasItemEntityAt(this.currentCellPos())) {
                  this.activeSubtask = AiGoto.travelToCompat(this.currentCellPos(), 0.6, false, false, false, 15);
               } else if (!schematic
                  || !reason.contains("face") && !reason.contains("placement context")
                  || !this.close(this.orderedCells.get(this.cellCursor), this.currentCellPos())
                     && !this.pop(this.currentCellPos())
                     && !this.sync(this.orderedCells.get(this.cellCursor), this.currentCellPos())) {
                  if (!this.sync_Z && mc.player.getY() > (schematic ? this.currentCellPos().getY() + 3 : blueprint.origin().getY() + 1.5)) {
                     this.sync_Z = true;
                     this.cache = 0;
                     this.activeSubtask = this.climbDownOp();
                  } else {
                     this.currentCellAttempts = Math.max(this.currentCellAttempts, this.placeAttempts.getOrDefault(this.currentCellPos().asLong(), 0)) + 1;
                     AiBuilder.raiseAttemptCount(this.placeAttempts, this.currentCellPos(), this.currentCellAttempts);
                     if (this.currentCellAttempts >= this.placeAttemptLimit) {
                        this.deferCurrentCell(reason);
                     } else {
                        this.load_V(this.currentCellPos());
                     }
                  }
               }
            }

            int verifyTimeoutTicks() {
               IntegratedServer server = mc.getSingleplayerServer();
               if (server == null) {
                  return 100;
               }

               double behindMs = Math.max(0.0, server.getCurrentSmoothedTickTime() - 50.0);
               return 100 + (int)Math.min(400.0, behindMs / 25.0 + 20.0);
            }

            boolean hasItemEntityAt(BlockPos pos) {
               return !mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)).isEmpty();
            }

            AiOperation.Result tickOuterScaffoldCleanup() {
               if (this.outerScaffoldCells.isEmpty() && this.outerScaffoldBreakOp == null) {
                  AABB box = this.buildBounds.inflate(3.0);

                  for (BlockPos cell : AiScaffold.recordedIn(
                     (int)Math.floor(box.minX),
                     (int)Math.floor(box.minY),
                     (int)Math.floor(box.minZ),
                     (int)Math.ceil(box.maxX) - 1,
                     (int)Math.ceil(box.maxY) - 1,
                     (int)Math.ceil(box.maxZ) - 1
                  )) {
                     if (!this.buildBounds.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5)
                        && AiScaffold.matches(AiScaffold.recordedBlock(cell), mc.level.getBlockState(cell).getBlock())) {
                        this.outerScaffoldCells.add(cell);
                     }
                  }

                  if (this.outerScaffoldCells.isEmpty()) {
                     return null;
                  }
               }

               if (this.outerScaffoldBreaks >= 30) {
                  this.outerScaffoldCells.clear();
                  return null;
               }

               if (this.outerScaffoldBreakOp == null) {
                  BlockPos cell = null;
                  AABB playerBox = mc.player.getBoundingBox();

                  while (!this.outerScaffoldCells.isEmpty()) {
                     BlockPos candidate = this.outerScaffoldCells.remove(0);
                     if (!this.existingSupportKeys.contains(candidate.asLong()) && !AiSchematicActions.init(candidate, this.load_Map, mc.level)) {
                        if (!(candidate.getX() >= Math.floor(playerBox.minX))
                           || !(candidate.getX() <= Math.floor(playerBox.maxX))
                           || !(candidate.getZ() >= Math.floor(playerBox.minZ))
                           || !(candidate.getZ() <= Math.floor(playerBox.maxZ))
                           || candidate.getY() < (int)Math.floor(mc.player.getY()) - 2
                           || candidate.getY() > (int)Math.floor(mc.player.getY())) {
                           cell = candidate;
                           break;
                        }
                     } else {
                        this.foundationKeys.add(candidate.asLong());
                        AiScaffold.forget(candidate);
                     }
                  }

                  if (cell == null) {
                     return null;
                  }

                  this.outerScaffoldBreakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(cell, 10, true, this.cachedProtectedCells(), false);
               }

               AiOperation.Result r = this.outerScaffoldBreakOp.tick();
               if (!r.terminal()) {
                  return r;
               }

               this.outerScaffoldBreakOp.cancel();
               this.outerScaffoldBreakOp = null;
               this.outerScaffoldBreaks++;
               return this.running("Clearing old scaffolding");
            }

            AiOperation.Result tickSupportDrain() {
               if (schematic && !this.ownsScaffolding) {
                  return null;
               }

               if (this.enclosureEscapeOp != null) {
                  AiOperation.Result escaped = this.enclosureEscapeOp.tick();
                  if (!escaped.terminal()) {
                     return this.running("Leaving the enclosed room before scaffold removal: " + escaped.message());
                  }

                  this.enclosureEscapeOp.cancel();
                  this.enclosureEscapeOp = null;
                  this.pop_Z = false;
                  this.drainSupports.clear();
                  return this.running("Re-evaluating scaffold access after leaving the room");
               } else if (schematic
                  && this.outerScaffoldBreakOp == null
                  && this.supportStepOffOp == null
                  && this.drainFloorBreakOp == null
                  && !autismclient.ai.engine.AiMotionSafety.isPlayerSupported()) {
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  if (mc.player.tickCount % 100 == 0) {
                     AutismClientAddon.LOG
                        .info(
                           "[AI] build: waiting to land at {} on {} (ground {}, motion {})",
                           mc.player.position(),
                           mc.level.getBlockState(mc.player.blockPosition().below()),
                           mc.player.onGround(),
                           mc.player.getDeltaMovement()
                        );
                  }

                  return this.running("Waiting for the scaffold descent to land before removing its next support");
               } else {
                  if (schematic && this.outerScaffoldBreakOp == null && this.supportStepOffOp == null && this.drainFloorBreakOp == null && this.tryDoorExit()) {
                     return this.running("Opening a temporary exit from the enclosed room");
                  }

                  if (schematic && !this.pop_Z) {
                     if (this.drainFloorBreakOp != null) {
                        AiOperation.Result dropped = this.drainFloorBreakOp.tick();
                        if (!dropped.terminal()) {
                           return this.running("Removing the temporary support underfoot");
                        }

                        this.drainFloorBreakOp.cancel();
                        this.drainFloorBreakOp = null;
                        return this.running("Temporary support removed; re-evaluating the footing");
                     }

                     if (this.supportStepOffOp != null) {
                        AiOperation.Result moved = this.supportStepOffOp.tick();
                        if (!moved.terminal()) {
                           return this.running("Stepping onto the finished structure before removing scaffolding");
                        }

                        this.supportStepOffOp.cancel();
                        this.supportStepOffOp = null;
                     }

                     boolean standingOnSupport = this.removableScaffoldCells().stream().anyMatch(this::isStandingOn);
                     if (standingOnSupport && this.stepOffStances.size() < 8) {
                        BlockPos stance = null;
                        double distance = 1.7976931348623157E308;

                        for (AiBuilder.CellSpec cell : blueprint.cells()) {
                           BlockPos floor = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                           BlockState state = mc.level.getBlockState(floor);
                           if (Block.isShapeFullBlock(state.getCollisionShape(mc.level, floor)) && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state)) {
                              BlockPos feet = floor.above();
                              if (!this.stepOffStances.contains(feet.asLong()) && !(Math.abs(feet.getY() - mc.player.getY()) > 2.0)) {
                                 AABB body = mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(feet).subtract(mc.player.position()));
                                 double d = mc.player.position().distanceToSqr(Vec3.atBottomCenterOf(feet));
                                 if (!(d >= distance) && !(d > 576.0) && mc.level.noCollision(mc.player, body)) {
                                    stance = feet;
                                    distance = d;
                                 }
                              }
                           }
                        }

                        if (stance != null) {
                           this.stepOffStances.add(stance.asLong());
                           this.supportStepOffOp = AiGoto.travelToCompat(stance, 0.35, false, false, false, 10);
                           return this.running("Moving off the temporary support");
                        }
                     }

                     if (standingOnSupport && this.removableScaffoldCells().stream().noneMatch(p -> p.getY() > mc.player.getY() + 3.0)) {
                        for (BlockPos floor : this.removableScaffoldCells()) {
                           if (this.isStandingOn(floor) && !this.put_Set.contains(floor.asLong()) && this.read(floor)) {
                              this.put_Set.add(floor.asLong());
                              this.drainFloorBreakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(floor, 10, true, this.close_Set);
                              return this.running("Dismantling the temporary column toward a safe landing");
                           }
                        }
                     }
                  }

                  if (!this.pop_Z) {
                     this.pop_Z = true;
                     if (this.close_Set.isEmpty()) {
                        for (AiBuilder.CellSpec c : this.orderedCells) {
                           this.close_Set.add(blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong());
                        }
                     }

                     AABB cleanupVolume = schematic ? this.buildBounds.inflate(3.0) : this.buildBounds;
                     List<BlockPos> supports = schematic
                        ? this.removableScaffoldCells()
                        : AiScaffold.recordedIn(
                           (int)Math.floor(cleanupVolume.minX),
                           (int)Math.floor(cleanupVolume.minY),
                           (int)Math.floor(cleanupVolume.minZ),
                           (int)Math.ceil(cleanupVolume.maxX) - 1,
                           (int)Math.ceil(cleanupVolume.maxY) - 1,
                           (int)Math.ceil(cleanupVolume.maxZ) - 1
                        );
                     if (schematic && this.detachedSupports == null) {
                        this.detachedSupports = new HashSet<>();

                        for (BlockPos support : supports) {
                           this.detachedSupports.add(support.asLong());
                        }
                     }

                     this.drainInitialSupports = supports.size();
                     this.drainStartMs = System.currentTimeMillis();
                     this.drainLastRemovalMs = this.drainStartMs;

                     for (BlockPos cell : supports) {
                        if (!this.close_Set.contains(cell.asLong())
                           && !this.existingSupportKeys.contains(cell.asLong())
                           && AiScaffold.matches(this.scaffoldBlockAt(cell), mc.level.getBlockState(cell).getBlock())) {
                           this.drainSupports.add(cell);
                        }
                     }
                  }

                  while (this.outerScaffoldBreakOp == null) {
                     long drainNow = System.currentTimeMillis();
                     if (this.drainStartMs == 0L) {
                        this.drainStartMs = drainNow;
                        this.drainLastRemovalMs = drainNow;
                     }

                     if (this.drainSupports.isEmpty() || drainNow - this.drainLastRemovalMs <= 5000L && drainNow - this.drainStartMs <= 60000L) {
                        if (!this.drainSupports.isEmpty() && this.drainSteps < (schematic ? 2097152 : 30)) {
                           BlockPos candidate = this.drainSupports
                              .remove(AiBuilder.pickTopScaffoldTeardownIndex(this.drainSupports, mc.player.position(), cell -> autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, cell) != null));
                           long candidateNow = System.currentTimeMillis();
                           int drainAttempts = this.drainAttemptCounts.getOrDefault(candidate.asLong(), 0);
                           if (candidateNow < this.drainRetryAt.getOrDefault(candidate.asLong(), 0L)) {
                              this.drainSupports.add(candidate);
                              continue;
                           }

                           if (drainAttempts < 3 && autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, candidate) == null) {
                              BlockPos beside = this.standableBeside(candidate);
                              if (beside != null) {
                                 this.drainAttemptCounts.put(candidate.asLong(), drainAttempts + 1);
                                 this.drainRetryAt.put(candidate.asLong(), candidateNow + 20L);
                                 this.drainSupports.add(candidate);
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] build: step-off attempt {}/3 at {} toward {}",
                                       drainAttempts + 1,
                                       candidate.toShortString(),
                                       beside.toShortString()
                                    );
                                 this.drainCandidate = null;
                                 this.outerScaffoldBreakOp = AiGoto.travelToCompat(beside, 0.35, false, false, false, 10);
                                 return this.running("Stepping beside a temporary support to remove it");
                              }
                           }

                           if (drainAttempts >= 3) {
                              this.drainAttemptCounts.put(candidate.asLong(), drainAttempts + 1);
                              AutismClientAddon.LOG
                                 .info("[AI] build: drain support retired at {} reason=step-off-cap (attempts 3/3); forcing removal", candidate.toShortString());
                           }

                           if (AiSchematicActions.init(candidate, this.load_Map, mc.level)) {
                              this.foundationKeys.add(candidate.asLong());
                              AiScaffold.forget(candidate);
                              continue;
                           }

                           if (this.isStandingOn(candidate)) {
                              continue;
                           }

                           HashSet<Long> keep = schematic ? new HashSet<>(this.cachedProtectedCells()) : new HashSet<>();
                           if (schematic) {
                              keep.addAll(this.close_Set);
                           }

                           if (schematic) {
                              for (BlockPos support : this.ownedScaffoldCells()) {
                                 keep.add(support.asLong());
                              }
                           }

                           keep.remove(candidate.asLong());
                           this.drainCandidate = candidate;
                           this.activeKeep = keep;
                           this.outerScaffoldBreakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(candidate, 10, true, keep, false);
                           continue;
                        }

                        if (schematic) {
                           int left = this.removableScaffoldCells().size();
                           if (left > 0 && left < this.drainInitialSupports) {
                              this.drainSupports.clear();
                              this.pop_Z = false;
                              this.drainSteps = 0;
                              this.stepOffStances.clear();
                              this.put_Set.clear();
                              return this.running("Rechecking supports exposed by scaffold removal");
                           }
                        }

                        return null;
                     }

                     int drainLeft = this.removableScaffoldCells().size();
                     AutismClientAddon.LOG
                        .info("[AI] build: drain pass bounded out (no removal for {} ms); leaving {} supports", drainNow - this.drainLastRemovalMs, drainLeft);
                     this.drainSupports.clear();
                     this.drainAttemptCounts.clear();
                     this.drainRetryAt.clear();
                     return null;
                  }

                  AiOperation.Result r = this.outerScaffoldBreakOp.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.outerScaffoldBreakOp.cancel();
                  this.outerScaffoldBreakOp = null;
                  if (this.drainCandidate == null) {
                     return this.running("Rechecking reach from beside the temporary support");
                  }

                  if (schematic
                     && !this.retryingDetached
                     && r.failure() == AiFailure.UNREACHABLE
                     && this.drainCandidate.getY() > mc.player.getEyeY() + 1.0
                     && this.detachedSupports != null
                     && this.detachedSupports.remove(this.drainCandidate.asLong())) {
                     this.retryingDetached = true;
                     this.outerScaffoldBreakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(this.drainCandidate, 15, true, this.activeKeep, true);
                     return this.running("Reaching the detached temporary support");
                  }

                  this.drainSteps++;
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.drainRemoved++;
                     this.drainLastRemovalMs = System.currentTimeMillis();
                     if (this.drainCandidate != null) {
                        AiScaffold.forget(this.drainCandidate);
                     }
                  } else if (this.drainCandidate != null) {
                     int failedAttempts = this.drainAttemptCounts.getOrDefault(this.drainCandidate.asLong(), 0) + 1;
                     this.drainAttemptCounts.put(this.drainCandidate.asLong(), failedAttempts);
                     this.drainRetryAt.put(this.drainCandidate.asLong(), System.currentTimeMillis() + 20L);
                     if (failedAttempts >= 4 && Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] build: retired support could not be removed at {} (drainFailed {})", this.drainCandidate.toShortString(), failedAttempts);
                     }
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED && Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG.info("[AI] scaffold cleanup failed: {}", r.message());
                  }

                  if (this.retryingDetached) {
                     this.retryingDetached = false;
                     this.pop_Z = false;
                     this.drainSupports.clear();
                  }

                  this.drainCandidate = null;
                  this.activeKeep = Set.of();
                  return this.running("Clearing scaffolding from the finished work");
               }
            }

            BlockPos standableBeside(BlockPos candidate) {
               for (Direction dir : Plane.HORIZONTAL) {
                  BlockPos feet = candidate.relative(dir);
                  if (mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()
                     && mc.level.getBlockState(feet.above()).getCollisionShape(mc.level, feet.above()).isEmpty()) {
                     BlockState below = mc.level.getBlockState(feet.below());
                     if (Block.isShapeFullBlock(below.getCollisionShape(mc.level, feet.below())) && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(below)) {
                        AABB body = mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(feet).subtract(mc.player.position()));
                        if (mc.level.noCollision(mc.player, body) && !(mc.player.position().distanceToSqr(Vec3.atCenterOf(candidate)) > 20.25)) {
                           return feet;
                        }
                     }
                  }
               }

               return null;
            }

            boolean isStandingOn(BlockPos candidate) {
               AABB body = mc.player.getBoundingBox();
               if (!mc.player.onGround()) {
                  return false;
               }

               for (AABB local : mc.level.getBlockState(candidate).getCollisionShape(mc.level, candidate).toAabbs()) {
                  AABB box = local.move(candidate.getX(), candidate.getY(), candidate.getZ());
                  if (Math.abs(box.maxY - body.minY) < 0.05 && box.maxX > body.minX && box.minX < body.maxX && box.maxZ > body.minZ && box.minZ < body.maxZ) {
                     return true;
                  }
               }

               return false;
            }

            boolean read(BlockPos removed) {
               AABB body = mc.player.getBoundingBox();

               for (int x = (int)Math.floor(body.minX); x <= Math.floor(body.maxX); x++) {
                  for (int z = (int)Math.floor(body.minZ); z <= Math.floor(body.maxZ); z++) {
                     for (int y = (int)Math.floor(body.minY) - 1; y >= Math.floor(body.minY - 3.0); y--) {
                        BlockPos floor = new BlockPos(x, y, z);
                        if (!floor.equals(removed)) {
                           BlockState state = mc.level.getBlockState(floor);
                           if (!autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(state)
                              && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFeetHazardCompat(state)
                              && state.getFluidState().isEmpty()) {
                              for (AABB local : state.getCollisionShape(mc.level, floor).toAabbs()) {
                                 AABB box = local.move(x, y, z);
                                 if (box.maxY >= body.minY - 2.5
                                    && box.maxY <= body.minY + 0.05
                                    && box.maxX > body.minX
                                    && box.minX < body.maxX
                                    && box.maxZ > body.minZ
                                    && box.minZ < body.maxZ
                                    && autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(mc.player.position(), new Vec3(mc.player.getX(), box.maxY, mc.player.getZ()))
                                    )
                                  {
                                    return true;
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               return false;
            }

            boolean tryDoorExit() {
               if (!this.removableScaffoldCells().isEmpty() && this.exitAttemptColumns.size() < 2 && this.load_Z_4()) {
                  BlockPos worker = mc.player.blockPosition();
                  long column = BlockPos.asLong(worker.getX(), 0, worker.getZ());
                  if (this.exitAttemptColumns.contains(column)) {
                     return false;
                  }

                  BlockPos doorExit = null;
                  double distance = 1.7976931348623157E308;

                  for (BlockPos candidate : List.of(
                     new BlockPos((int)this.buildBounds.minX - 1, worker.getY(), worker.getZ()),
                     new BlockPos((int)this.buildBounds.maxX, worker.getY(), worker.getZ()),
                     new BlockPos(worker.getX(), worker.getY(), (int)this.buildBounds.minZ - 1),
                     new BlockPos(worker.getX(), worker.getY(), (int)this.buildBounds.maxZ)
                  )) {
                     BlockState floor = mc.level.getBlockState(candidate.below());
                     AABB body = mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(candidate).subtract(mc.player.position()));
                     double d = worker.distSqr(candidate);
                     if (!(d >= distance)
                        && !(d > 1024.0)
                        && !floor.is(Blocks.MAGMA_BLOCK)
                        && !floor.is(Blocks.CACTUS)
                        && floor.getFluidState().isEmpty()
                        && Block.isShapeFullBlock(floor.getCollisionShape(mc.level, candidate.below()))
                        && mc.level.noCollision(mc.player, body)
                        && autismclient.ai.engine.AiMotionSafety.isSegmentHazardFree(mc.player.position(), Vec3.atBottomCenterOf(candidate))) {
                        doorExit = candidate;
                        distance = d;
                     }
                  }

                  if (doorExit == null) {
                     BlockPos ceiling = worker.above(2);
                     BlockState state = mc.level.getBlockState(ceiling);
                     if (this.load_Map.containsKey(ceiling.asLong())
                        && !state.hasBlockEntity()
                        && state.getFluidState().isEmpty()
                        && Block.isShapeFullBlock(state.getCollisionShape(mc.level, ceiling))) {
                        int roof = ceiling.getY();

                        for (AiBuilder.CellSpec cell : blueprint.cells()) {
                           BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                           BlockState live = mc.level.getBlockState(pos);
                           if (pos.getX() == worker.getX() && pos.getZ() == worker.getZ() && !live.getCollisionShape(mc.level, pos).isEmpty()) {
                              roof = Math.max(roof, pos.getY());
                           }
                        }

                        this.exitAttemptColumns.add(column);
                        this.read_Map.merge(column, ceiling.getY(), Math::min);
                        BlockPos exit = new BlockPos(worker.getX(), roof + 1, worker.getZ());
                        this.enclosureEscapeOp = AiGoto.travelToCompat(
                           exit,
                           0.25,
                           true,
                           true,
                           false,
                           30,
                           autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                           true,
                           2.0,
                           AiGoto.protectingCellsBelowYCompat(
                              autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(exit, 0.25),
                              this.keepCells(),
                              blueprint.origin().getY()
                           )
                        );
                        return true;
                     } else {
                        return false;
                     }
                  } else {
                     int dx = Integer.signum(doorExit.getX() - worker.getX());
                     int dz = Integer.signum(doorExit.getZ() - worker.getZ());

                     for (BlockPos p = worker; !p.equals(doorExit); p = p.offset(dx, 0, dz)) {
                        for (BlockPos breach : List.of(p, p.above())) {
                           BlockState live = mc.level.getBlockState(breach);
                           if (this.load_Map.containsKey(breach.asLong()) && !live.hasBlockEntity() && live.getFluidState().isEmpty()) {
                              this.push_Set.add(breach.asLong());
                           }
                        }
                     }

                     this.exitAttemptColumns.add(column);
                     this.enclosureEscapeOp = AiGoto.travelToCompat(
                        doorExit,
                        0.25,
                        true,
                        false,
                        false,
                        20,
                        autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                        true,
                        2.0,
                        AiGoto.protectingCellsBelowYCompat(
                           autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(doorExit, 0.25),
                           this.keepCells(),
                           blueprint.origin().getY()
                        )
                     );
                     return true;
                  }
               } else {
                  return false;
               }
            }

            List<BlockPos> ownedScaffoldCells() {
               LinkedHashSet<BlockPos> owned = new LinkedHashSet<>();
               if (this.scaffoldTracker != null) {
                  owned.addAll(this.scaffoldTracker.ownedCells());
               }

               for (long key : this.scaffoldSupportKeys) {
                  owned.add(BlockPos.of(key));
               }

               return owned.stream()
                  .filter(
                     p -> !this.close_Set.contains(p.asLong())
                        && !this.existingSupportKeys.contains(p.asLong())
                        && AiScaffold.matches(this.scaffoldBlockAt(p), mc.level.getBlockState(p).getBlock())
                  )
                  .filter(p -> {
                     if (!AiSchematicActions.init(p, this.load_Map, mc.level)) {
                        return true;
                     }

                     this.foundationKeys.add(p.asLong());
                     this.scaffoldSupportKeys.remove(p.asLong());
                     AiScaffold.forget(p);
                     return false;
                  })
                  .sorted(Comparator.<BlockPos>comparingInt(p -> p.getY()).reversed())
                  .toList();
            }

            List<BlockPos> removableScaffoldCells() {
               return this.ownedScaffoldCells().stream().filter(p -> this.includeCheckpointSupports || !this.scaffoldSupportKeys.contains(p.asLong())).toList();
            }

            Block scaffoldBlockAt(BlockPos pos) {
               Block tracked = this.scaffoldTracker == null ? null : this.scaffoldTracker.recorded(pos);
               return tracked == null ? AiScaffold.recordedBlock(pos) : tracked;
            }

            AiOperation climbDownOp() {
               if (this.close_Set.isEmpty()) {
                  for (AiBuilder.CellSpec c : this.orderedCells) {
                     this.close_Set.add(blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong());
                  }
               }

               return new AiOperation() {
                  final Minecraft descentClient = Minecraft.getInstance();
                  AiOperation breakBelowOp;
                  int breakBelowAttempts;

                  BlockPos solidCellBelowFeet() {
                     AABB box = this.descentClient.player.getBoundingBox();
                     int[] xs = new int[]{(int)Math.floor(box.minX + 0.01), (int)Math.floor(box.maxX - 0.01)};
                     int[] zs = new int[]{(int)Math.floor(box.minZ + 0.01), (int)Math.floor(box.maxZ - 0.01)};
                     int y = (int)Math.floor(this.descentClient.player.getY()) - 1;

                     for (int dy = 0; dy >= -1; dy--) {
                        for (int x : xs) {
                           for (int z : zs) {
                              BlockPos cell = new BlockPos(x, y + dy, z);
                              if (!this.descentClient.level.getBlockState(cell).isAir()) {
                                 return cell;
                              }
                           }
                        }
                     }

                     return null;
                  }

                  @Override
                  public AiOperation.Result tick() {
                     if (this.descentClient.player != null && this.descentClient.level != null) {
                        if (this.descentClient.player.getY() <= blueprint.origin().getY() + 0.5) {
                           return this.finishDescent(AiOperation.Status.SUCCEEDED, "Back on the ground");
                        }

                        if (this.breakBelowAttempts >= 64) {
                           return this.finishDescent(AiOperation.Status.FAILED, "Could not climb down from the structure");
                        }

                        BlockPos below = this.solidCellBelowFeet();
                        if (below == null) {
                           return this.finishDescent(AiOperation.Status.FAILED, "No solid support below the player");
                        }

                        if (close_Set.contains(below.asLong())) {
                           return this.finishDescent(AiOperation.Status.FAILED, "Standing on the blueprint itself");
                        }

                        if (!read(below)) {
                           return this.finishDescent(AiOperation.Status.FAILED, "Removing this support would cause an unsafe fall");
                        }

                        if (this.breakBelowOp == null) {
                           this.breakBelowAttempts++;
                           this.breakBelowOp = autismclient.ai.engine.AiBreakBlock.breakBlock(below, 15);
                        }

                        AiOperation.Result r = this.breakBelowOp.tick();
                        if (!r.terminal()) {
                           return r;
                        }

                        this.breakBelowOp = null;
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.finishDescent(AiOperation.Status.FAILED, r.message())
                           : new AiOperation.Result(AiOperation.Status.RUNNING, "Climbing down from the structure", "{}", List.of());
                     } else {
                        return this.finishDescent(AiOperation.Status.CANCELLED, "Climb-down cancelled");
                     }
                  }

                  @Override
                  public void cancel() {
                     if (this.breakBelowOp != null) {
                        this.breakBelowOp.cancel();
                     }
                  }

                  AiOperation.Result finishDescent(AiOperation.Status status, String message) {
                     this.cancel();
                     return AiGameOperations.result(status, message, new JsonObject());
                  }
               };
            }

            AiOperation.Result tickTerrainClear() {
               if (this.terrainBreakOp != null) {
                  AiOperation.Result r = this.terrainBreakOp.tick();
                  if (!r.terminal()) {
                     return this.running("Clearing terrain " + this.terrainClearIndex + "/" + this.terrainToClear.size() + ": " + r.message());
                  }

                  this.terrainBreakOp.cancel();
                  this.terrainBreakOp = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED && !mc.level.getBlockState(this.terrainToClear.get(this.terrainClearIndex)).isAir()) {
                     this.terrainLeftCount++;
                  }

                  this.terrainClearIndex++;
               }

               if (this.terrainToClear == null) {
                  this.terrainToClear = new ArrayList<>(AiSchematicActions.cellsToClear(blueprint, this.load_Map, mc.level, this.sweepActive ? AiSchematicActions::load : AiSchematicActions::init));
                  this.terrainToClear.removeIf(px -> this.existingSupportKeys.contains(px.asLong()));
                  if (this.sweepActive) {
                     for (BlockPos stray : AiSchematicActions.load(blueprint, this.load_Map, mc.level)) {
                        if (!this.existingSupportKeys.contains(stray.asLong()) && !this.terrainToClear.contains(stray)) {
                           this.terrainToClear.add(stray);
                        }
                     }
                  }

                  if (!this.terrainToClear.isEmpty()) {
                     AutismClientAddon.LOG.info("[AI] schematic terrain: {} natural blocks stand in its air", this.terrainToClear.size());
                  }
               }

               while (this.terrainClearIndex < this.terrainToClear.size()) {
                  int top = this.terrainToClear.get(this.terrainClearIndex).getY();
                  int best = this.terrainClearIndex;
                  double bestSq = 1.7976931348623157E308;

                  for (int i = this.terrainClearIndex; i < this.terrainToClear.size() && this.terrainToClear.get(i).getY() == top; i++) {
                     double sq = this.terrainToClear.get(i).distToCenterSqr(mc.player.position());
                     if (sq < bestSq) {
                        bestSq = sq;
                        best = i;
                     }
                  }

                  Collections.swap(this.terrainToClear, this.terrainClearIndex, best);
                  BlockPos p = this.terrainToClear.get(this.terrainClearIndex);
                  BlockState state = mc.level.getBlockState(p);
                  if (this.sweepActive ? !AiSchematicActions.load(state) : !AiSchematicActions.init(state)) {
                     this.terrainClearIndex++;
                  } else {
                     if (AiSchematicActions.init(mc.level, p)) {
                        HashSet<Long> keep = new HashSet<>(this.cachedProtectedCells());
                        if (this.sweepActive) {
                           keep.addAll(this.close_Set);
                        }

                        this.terrainBreakOp = autismclient.ai.engine.AiBreakBlock.breakBlock(p, this.sweepActive ? 15 : 20, false, keep, false);
                        return this.running(
                           "Clearing " + BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath() + " out of the schematic's air at " + p.toShortString()
                        );
                     }

                     this.terrainLeftCount++;
                     this.terrainClearIndex++;
                  }
               }

               if (this.terrainLeftCount > 0) {
                  AutismClientAddon.LOG.info("[AI] schematic terrain: {} blocks left (fluid beside them or unbreakable)", this.terrainLeftCount);
               }

               return null;
            }

            boolean init_Z_TableIndex_StreamU_BlockPos(AiBuilder.CellSpec cell, BlockPos pos) {
               if (!AiSchematicActions.needsAction(cell) && !AiBuilder.isDoubleSlabCell(cell)) {
                  Block block = AiBuilder.resolveBlock(cell);
                  Item item = cell.block().equals("minecraft:grass_block") ? Items.DIRT : block.asItem();
                  if (item != Items.AIR && this.countInInventory(item) > 0) {
                     BlockState live = mc.level.getBlockState(pos);
                     if (live.canBeReplaced() && live.getFluidState().isEmpty() && live.getBlock() != block) {
                        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Math.pow(mc.player.blockInteractionRange() + 0.5, 2.0)) {
                           return false;
                        } else if (AiBuilder.collidesWithEntityBox(cell, pos, mc.player.getBoundingBox(), mc.level)) {
                           return false;
                        } else {
                           return AiBuilder.toBlockState(cell).canSurvive(mc.level, pos) && !this.mustHoldCell(cell, pos)
                              ? autismclient.ai.engine.PlacementGoal.planPlacement(mc, pos, item, this.constraintFor(cell, block), 72) != null
                              : false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            boolean mustHoldCell(AiBuilder.CellSpec cell, BlockPos pos) {
               if (this.redstoneConflict(cell, pos) != null) {
                  return true;
               }

               if (this.holdCounts.getOrDefault(cell, 0) >= 4) {
                  return false;
               }

               BlockPos previous = this.holdDependency;

               try {
                  return this.holdReason(cell, pos) != null;
               } finally {
                  this.holdDependency = previous;
               }
            }

            void failCurrentCell(String reason) {
               this.recordCellFailure(this.orderedCells.get(this.cellCursor), reason);
               this.advanceCell();
            }

            void recordCellFailure(AiBuilder.CellSpec cell, String reason) {
               if (this.failedCells.add(cell)) {
                  if (this.failureEntries.size() < 32) {
                     this.failureEntries.add(AiJson.objectOf("dx", cell.dx(), "dy", cell.dy(), "dz", cell.dz(), "block", cell.block(), "reason", reason));
                  }

                  this.failedTotal++;
               }
            }

            boolean load_Z_4() {
               if (!this.buildBounds.intersects(mc.player.getBoundingBox())) {
                  return false;
               }

               BlockPos start = mc.player.blockPosition();
               ArrayDeque<BlockPos> queue = new ArrayDeque<>();
               HashSet<Long> seen = new HashSet<>();
               queue.add(start);
               seen.add(start.asLong());

               while (!queue.isEmpty() && seen.size() < 4000) {
                  BlockPos cell = queue.poll();
                  if (this.isClimbableCell(cell)) {
                     for (BlockPos next : List.of(cell.above(), cell.below())) {
                        if (this.buildBounds.inflate(4.0).contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5)
                           && seen.add(next.asLong())
                           && (this.isClimbableCell(next) || this.isStandableCell(next))) {
                           if (!this.buildBounds.contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5)) {
                              return false;
                           }

                           queue.add(next);
                        }
                     }
                  }

                  for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                     for (int dy = -3; dy <= 1; dy++) {
                        BlockPos next = cell.offset(d[0], dy, d[1]);
                        if (this.buildBounds.inflate(4.0).contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5) && seen.add(next.asLong())) {
                           boolean inside = this.buildBounds.contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5);
                           if (this.isStandableCell(next) || dy == 0 && this.isClimbableCell(next)) {
                              if (!inside) {
                                 return false;
                              }

                              queue.add(next);
                           }
                        }
                     }
                  }
               }

               return true;
            }

            boolean isStandableCell(BlockPos cell) {
               return this.isPassableCell(cell)
                  && this.isPassableCell(cell.above())
                  && !mc.level.getBlockState(cell.below()).getCollisionShape(mc.level, cell.below()).isEmpty();
            }

            boolean isPassableCell(BlockPos cell) {
               BlockState state = mc.level.getBlockState(cell);
               return state.getCollisionShape(mc.level, cell).isEmpty()
                  || state.getBlock() instanceof DoorBlock
                  || state.getBlock() instanceof FenceGateBlock
                  || state.getBlock() instanceof TrapDoorBlock;
            }

            boolean isClimbableCell(BlockPos cell) {
               return mc.level.getBlockState(cell).is(BlockTags.CLIMBABLE) && this.isPassableCell(cell.above());
            }

            void read_V_2() {
               if (!schematic) {
                  for (int i = 0; i < this.cellCursor && i < this.orderedCells.size(); i++) {
                     AiBuilder.CellSpec cell = this.orderedCells.get(i);
                     BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                     if (!AiBuilder.isCellSatisfied(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)
                        && !this.deferredCells.contains(cell)
                        && !this.hasRecordedFailure(cell)) {
                        this.deferredCells.add(cell);
                        this.deferredReasons.add("breached during escape");
                     }
                  }
               }
            }

            boolean hasRecordedFailure(AiBuilder.CellSpec cell) {
               for (JsonObject entry : this.failureEntries) {
                  JsonObject object = entry.getAsJsonObject();
                  if (object.get("dx").getAsInt() == cell.dx() && object.get("dy").getAsInt() == cell.dy() && object.get("dz").getAsInt() == cell.dz()) {
                     return true;
                  }
               }

               return false;
            }

            void logDeferredSummary() {
               TreeMap<String, Integer> counts = new TreeMap<>();

               for (int i = 0; i < this.deferredCells.size(); i++) {
                  String reason = this.deferredReasons.get(i) == null ? "?" : this.deferredReasons.get(i).replaceAll("-?[0-9]+, -?[0-9]+, -?[0-9]+", "<pos>");
                  counts.merge(
                     this.deferredCells.get(i).block().replace("minecraft:", "") + ": " + (reason.length() > 90 ? reason.substring(0, 90) : reason),
                     1,
                     Integer::sum
                  );
               }

               AutismClientAddon.LOG.info("[AI] build pass {} deferred {} cells: {}", this.deferPassCount, this.deferredCells.size(), counts);
            }

            void deferCurrentCell(String reason) {
               BlockPos pos = this.currentCellPos();
               long key = pos.asLong();
               if (Boolean.getBoolean("autism.navigation.debug")) {
                  AutismClientAddon.LOG.info("[AI] schematic defer {} {}: {}", pos.toShortString(), this.orderedCells.get(this.cellCursor).block(), reason);
               }

               if (this.placeAttempts.containsKey(key)
                  || this.write_Set.contains(key)
                  || pos.equals(this.placeAckTracker.pendingPos())
                  || pos.equals(this.readyQueue.activePos())) {
                  this.read_Set.add(key);
               }

               this.deferredCells.add(this.orderedCells.get(this.cellCursor));
               this.deferredReasons.add(reason);
               this.cache = 0;
               this.advanceCell();
            }

            void advanceCell() {
               if (schematic) {
                  mc.options.keyShift.setDown(false);
               }

               if (this.ownsScaffolding && this.cellCursor < this.orderedCells.size()) {
                  AiBuilder.CellSpec completed = this.orderedCells.get(this.cellCursor);
                  BlockPos pos = blueprint.origin().offset(completed.dx(), completed.dy(), completed.dz());
                  if (AiBuilder.isCellSatisfied(completed, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     AiScaffold.forget(pos);
                     if (schematic && this.auditPassCount > 0) {
                        this.pop_Set.add(pos.asLong());
                     }
                  }
               }

               this.cellCursor++;
               this.resetCellState();
            }

            void resetCellState() {
               this.rejectedBatchCells.clear();
               this.currentCellAttempts = this.cellCursor < this.orderedCells.size() ? this.placeAttempts.getOrDefault(this.currentCellPos().asLong(), 0) : 0;
               this.sync_Z = false;
               this.cache = 0;
               this.parse = 0;
               this.scan = 0;
               this.entry = this.node = this.task = 0;
               this.triedShulkerSources.clear();
               this.write_Z = false;
               this.pendingPlacePos = null;
               this.pendingPlaceCell = null;
               this.currentPlacementPlan = null;
               this.start_I = 0;
               this.pendingYaw = 0.0F / 0.0F;
               this.load_Z = false;
               this.stop_I = 0;
               this.read_Z = false;
               this.next_I = 0;
               this.init_Str = null;
               this.prev = 0;
               this.reportedPropertyKeys.clear();
               this.set_Set.clear();
               this.tick_Z = false;
               this.get_Z = this.set_Z = false;
               this.put_Z = false;
            }

            Set<Long> cachedProtectedCells() {
               BlockPos worker = mc.player.blockPosition();
               if (this.protectedCellsCache != null && this.protectedCacheTick == mc.player.tickCount && worker.equals(this.protectedCacheWorker)) {
                  return this.protectedCellsCache;
               }

               this.protectedCellsCache = Collections.unmodifiableSet(this.collectProtectedCells(false));
               this.protectedCacheTick = mc.player.tickCount;
               this.protectedCacheWorker = worker;
               return this.protectedCellsCache;
            }

            Set<Long> keepCells() {
               LinkedHashSet<Long> cells = new LinkedHashSet<>(this.collectProtectedCells(true));
               if (schematic) {
                  cells.addAll(this.close_Set);
               }

               return Collections.unmodifiableSet(cells);
            }

            Set<Long> collectProtectedCells(boolean recovery) {
               return this.init_Set_Z_BlockPos(recovery, null);
            }

            Set<Long> init_Set_Z_BlockPos(boolean recovery, BlockPos ignore) {
               BlockPos worker = mc.player.blockPosition();
               if (recovery && this.ownsScaffolding && worker.getY() + 2 < this.buildBounds.maxY && (worker.getY() < blueprint.origin().getY() || this.cachedHasApproach(worker))) {
                  for (BlockPos ceiling : List.of(worker.above(), worker.above(2))) {
                     BlockState state = mc.level.getBlockState(ceiling);
                     if (this.load_Map.containsKey(ceiling.asLong())
                        && !this.pop_Set.contains(ceiling.asLong())
                        && !state.hasBlockEntity()
                        && state.getFluidState().isEmpty()
                        && !state.getCollisionShape(mc.level, ceiling).isEmpty()) {
                        this.read_Map.merge(BlockPos.asLong(worker.getX(), 0, worker.getZ()), ceiling.getY(), Math::min);
                     }
                  }
               }

               if (recovery && this.ownsScaffolding && AiGoto.recentNoRouteCountCompat(autismclient.ai.engine.AiStance.standingPos(mc)) >= 3 && this.isStandingHighAboveOrigin(worker) && this.cachedHasApproach(worker)) {
                  Integer before = this.read_Map.get(BlockPos.asLong(worker.getX(), 0, worker.getZ()));
                  if (before == null || before > blueprint.origin().getY()) {
                     AutismClientAddon.LOG.info("[AI] build: stranded on the work at {}; its column opens down to the ground", worker.toShortString());
                     this.read_Map.put(BlockPos.asLong(worker.getX(), 0, worker.getZ()), blueprint.origin().getY());
                  }
               }

               HashSet<Long> keep = new HashSet<>();
               keep.addAll(this.scaffoldSupportKeys);
               keep.addAll(this.foundationKeys);
               if (this.scaffoldTracker != null) {
                  for (BlockPos support : this.scaffoldTracker.ownedCells()) {
                     if (!this.close_Set.contains(support.asLong())
                        && AiScaffold.matches(this.scaffoldTracker.recorded(support), mc.level.getBlockState(support).getBlock())
                        && AiSchematicActions.init(support, this.load_Map, mc.level)) {
                        keep.add(support.asLong());
                     }
                  }
               }

               if (recovery && this.ownsScaffolding && this.cellCursor < this.orderedCells.size()) {
                  this.init_V_2(this.currentCellPos());
               }

               for (long key : this.close_Set) {
                  BlockPos pos = BlockPos.of(key);
                  BlockState live = mc.level.getBlockState(pos);
                  if (this.pop_Set.contains(key)) {
                     keep.add(key);
                  } else {
                     Integer opening = this.read_Map.get(BlockPos.asLong(pos.getX(), 0, pos.getZ()));
                     if (!AiBuilder.canClearDuringRecovery(recovery, this.push_Set.contains(key), opening, pos.getY(), live)) {
                        AiBuilder.CellSpec wantedHere = this.load_Map.get(key);
                        boolean stray = schematic && wantedHere != null && !live.hasBlockEntity() && AiSchematicActions.load(live) && !AiBuilder.matchesIgnoringUnpairedChest(wantedHere, live);
                        if (AiBuilder.isOccupiedOrMatchedScaffold(this.load_Map.get(key), live, stray || AiScaffold.matches(this.scaffoldBlockAt(pos), live.getBlock()))) {
                           keep.add(key);
                        }
                     }
                  }
               }

               for (long key : this.existingSupportKeys) {
                  BlockPos pos = BlockPos.of(key);
                  if ((ignore == null || !pos.equals(ignore.below()) || AiSchematicActions.init_Z_BlockPos(pos, this.load_Map, mc.level, ignore))
                     && !mc.level.getBlockState(pos).isAir()) {
                     keep.add(key);
                  }
               }

               return keep;
            }

            void init_V_2(BlockPos target) {
               this.init_V_BlockPos_Z(target, false);
            }

            String holdReason(AiBuilder.CellSpec cell, BlockPos pos) {
               this.holdDependency = null;
               Block target = AiBuilder.resolveBlock(cell);
               ClientLevel level = mc.level;
               String conflict = this.redstoneConflict(cell, pos);
               if (conflict != null) {
                  return conflict;
               }

               if (target instanceof ObserverBlock) {
                  Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
                  if (facing == null) {
                     return null;
                  } else {
                     BlockPos watched = pos.relative(facing);
                     AiBuilder.CellSpec wanted = this.load_Map.get(watched.asLong());
                     if (wanted != null
                        && Block.isShapeFullBlock(AiBuilder.toBlockState(wanted).getCollisionShape(level, watched))
                        && !AiBuilder.matchesIgnoringUnpairedChest(wanted, level.getBlockState(watched))) {
                        this.holdDependency = watched;
                        return "an observer waits for the cell it watches";
                     } else {
                        return null;
                     }
                  }
               } else {
                  if (this.isRedstoneReactive(target)) {
                     for (Direction direction : Direction.values()) {
                        BlockPos observer = pos.relative(direction);
                        AiBuilder.CellSpec wanted = this.load_Map.get(observer.asLong());
                        if (wanted != null
                           && AiBuilder.resolveBlock(wanted) instanceof ObserverBlock
                           && Direction.byName(wanted.properties().getOrDefault("facing", "north")) == direction) {
                           BlockPos watched = observer.relative(direction);

                           for (BlockPos near : List.of(
                              watched, watched.north(), watched.south(), watched.east(), watched.west(), watched.above(), watched.below()
                           )) {
                              if (!near.equals(observer) && !near.equals(pos)) {
                                 AiBuilder.CellSpec around = this.load_Map.get(near.asLong());
                                 if (around != null && !AiBuilder.matchesIgnoringUnpairedChest(around, level.getBlockState(near))) {
                                    this.holdDependency = near;
                                    return "a part an observer powers waits until the observer's view is finished";
                                 }
                              }
                           }
                        }
                     }
                  }

                  return null;
               }
            }

            String redstoneConflict(AiBuilder.CellSpec cell, BlockPos pos) {
               Block target = AiBuilder.resolveBlock(cell);
               ClientLevel level = mc.level;
               if (target instanceof RedstoneTorchBlock && "false".equals(cell.properties().get("lit")) && AiBuilder.willRedstoneTorchBeLit(level, pos, cell)) {
                  return "an unlit redstone torch waits for its block's power";
               }

               if (target instanceof PistonBaseBlock && !"true".equals(cell.properties().get("extended"))) {
                  Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
                  if (facing != null && AiBuilder.hasPistonPowerAt(level, pos, facing)) {
                     return "a retracted piston waits until nothing powers it";
                  }
               }

               return target instanceof TntBlock && level.hasNeighborSignal(pos) ? "TNT waits until nothing powers it" : null;
            }

            boolean isRedstoneReactive(Block target) {
               return target instanceof PistonBaseBlock
                  || target instanceof DispenserBlock
                  || target instanceof CrafterBlock
                  || target instanceof TntBlock
                  || target instanceof DoorBlock
                  || target instanceof TrapDoorBlock
                  || target instanceof FenceGateBlock;
            }

            boolean deferBehindDependency(AiBuilder.CellSpec cell, String reason) {
               int after = -1;
               if (this.holdDependency != null) {
                  for (int j = this.cellCursor + 1; j < this.orderedCells.size(); j++) {
                     AiBuilder.CellSpec next = this.orderedCells.get(j);
                     if (blueprint.origin().offset(next.dx(), next.dy(), next.dz()).equals(this.holdDependency)) {
                        after = j;
                        break;
                     }
                  }
               }

               if (after < 0) {
                  after = Math.min(this.orderedCells.size() - 1, this.cellCursor + 32);
               }

               if (after <= this.cellCursor) {
                  return false;
               }

               this.holdCounts.merge(cell, 1, Integer::sum);
               if (Boolean.getBoolean("autism.navigation.debug")) {
                  AutismClientAddon.LOG.info("[AI] schematic hold {} {}: {}", this.currentCellPos().toShortString(), cell.block(), reason);
               }

               this.orderedCells.add(after + 1, cell);
               this.orderedCells.remove(this.cellCursor);
               this.currentCellAttempts = this.placeAttempts.getOrDefault(this.currentCellPos().asLong(), 0);
               this.currentPlacementPlan = null;
               this.start_I = 0;
               this.pendingYaw = 0.0F / 0.0F;
               this.load_Z = false;
               return true;
            }

            boolean planSupportPlacement(AiBuilder.CellSpec cell, BlockPos pos, Block targetBlock) {
               if (this.supportPlanAttempts.getOrDefault(cell, 0) >= 2) {
                  return false;
               }

               HashSet<Long> supports = new HashSet<>();

               for (Direction direction : Direction.values()) {
                  if (AiBuilder.allowsSupportOnSide(cell, targetBlock, direction)) {
                     supports.add(pos.relative(direction).asLong());
                  }
               }

               int after = -1;

               for (int j = this.cellCursor + 1; j < this.orderedCells.size() && after < 0; j++) {
                  AiBuilder.CellSpec next = this.orderedCells.get(j);
                  if (supports.contains(blueprint.origin().offset(next.dx(), next.dy(), next.dz()).asLong())) {
                     after = j;
                  }
               }

               if (after < 0) {
                  return false;
               }

               this.supportPlanAttempts.merge(cell, 1, Integer::sum);
               this.orderedCells.add(after + 1, cell);
               this.orderedCells.remove(this.cellCursor);
               this.currentCellAttempts = this.placeAttempts.getOrDefault(this.currentCellPos().asLong(), 0);
               this.currentPlacementPlan = null;
               this.start_I = 0;
               this.pendingYaw = 0.0F / 0.0F;
               this.load_Z = false;
               return true;
            }

            BlockPos blockInsideBody() {
               AABB body = mc.player.getBoundingBox().deflate(0.05);
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos cell : List.of(feet, feet.above())) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity() && !(state.getDestroySpeed(mc.level, cell) < 0.0F)) {
                     VoxelShape shape = state.getCollisionShape(mc.level, cell);
                     if (!shape.isEmpty() && shape.bounds().move(cell).intersects(body)) {
                        AiBuilder.CellSpec wanted = this.load_Map.get(cell.asLong());
                        if ((wanted == null || !AiBuilder.matchesIgnoringUnpairedChest(wanted, state)) && (AiSchematicActions.load(state) || wanted != null)) {
                           return cell.immutable();
                        }
                     }
                  }
               }

               return null;
            }

            boolean isStandingHighAboveOrigin(BlockPos worker) {
               return mc.player.onGround() && worker.getY() > blueprint.origin().getY() + 3;
            }

            boolean cachedHasApproach(BlockPos worker) {
               if (worker.asLong() == this.approachCacheWorker && mc.player.tickCount == this.approachCacheTick) {
                  return this.approachCacheResult;
               }

               this.approachCacheWorker = worker.asLong();
               this.approachCacheTick = mc.player.tickCount;
               return this.approachCacheResult = AiSchematicActions.hasApproach(worker, this.buildBounds.inflate(1.0), 600, this::isTraversable);
            }

            boolean hasApproachLongSearch(BlockPos worker) {
               return AiSchematicActions.hasApproach(worker, this.buildBounds.inflate(1.0), 24000, this::isTraversable);
            }

            boolean openExit(BlockPos worker) {
               if (schematic && this.breakOutTries < 6) {
                  final Set<Long> room = AiSchematicActions.init_Set_BlockPos(worker, this.buildBounds.inflate(1.0), 24000, this::isTraversable);
                  if (room.isEmpty()) {
                     return false;
                  }

                  final ArrayList<BlockPos> placedInRoom = new ArrayList<>();

                  for (long key : room) {
                     AiBuilder.CellSpec cell = this.load_Map.get(key);
                     BlockPos p = BlockPos.of(key);
                     if (cell != null && AiBuilder.matchesIgnoringUnpairedChest(cell, mc.level.getBlockState(p))) {
                        placedInRoom.add(p);
                     }
                  }

                  this.breakOutTries++;
                  this.pendingFoundation = null;
                  this.run_Z = this.init_Z = false;
                  final AiOperation escape = this.breakOutOp();
                  this.activeSubtask = new AiOperation() {
                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result res = escape.tick();
                        if (res.status() == AiOperation.Status.SUCCEEDED) {
                           for (BlockPos p : placedInRoom) {
                              if (!AiBuilder.matchesIgnoringUnpairedChest(load_Map.get(p.asLong()), mc.level.getBlockState(p))) {
                                 mergeSyncRoom(p.asLong(), room);
                              }
                           }
                        }

                        return res;
                     }

                     @Override
                     public boolean canInterruptNow() {
                        return escape.canInterruptNow();
                     }

                     @Override
                     public boolean requiresReconciliationAfterEmergency() {
                        return escape.requiresReconciliationAfterEmergency();
                     }

                     @Override
                     public void cancel() {
                        escape.cancel();
                     }
                  };
                  return true;
               } else {
                  return false;
               }
            }

            void mergeSyncRoom(long key, Set<Long> room) {
               this.sync_Map.merge(key, room, (existing, added) -> {
                  if (existing.containsAll(added)) {
                     return existing;
                  }

                  HashSet<Long> merged = new HashSet<>(existing);
                  merged.addAll(added);
                  return merged;
               });
            }

            boolean read(AiBuilder.CellSpec cell, BlockPos target) {
               if (!this.buildBounds.inflate(1.0).contains(mc.player.position())) {
                  return false;
               }

               BlockState placed = AiBuilder.toBlockState(cell);
               if (!placed.getCollisionShape(mc.level, target).isEmpty()
                  && !(placed.getBlock() instanceof DoorBlock)
                  && !(placed.getBlock() instanceof TrapDoorBlock)
                  && !(placed.getBlock() instanceof FenceGateBlock)) {
                  AABB site = this.buildBounds.inflate(1.0);
                  BlockPos feet = autismclient.ai.engine.AiStance.standingPos(mc);
                  if (this.sync_Map.getOrDefault(target.asLong(), Set.of()).contains(feet.asLong())) {
                     return true;
                  }

                  HashSet<Long> seen = new HashSet<>();
                  ArrayDeque<BlockPos> queue = new ArrayDeque<>();
                  seen.add(target.asLong());

                  for (BlockPos start : List.of(feet, feet.above())) {
                     if (!start.equals(target) && seen.add(start.asLong())) {
                        queue.add(start);
                     }
                  }

                  while (!queue.isEmpty()) {
                     if (seen.size() > 4096) {
                        return false;
                     }

                     BlockPos p = queue.poll();
                     if (!site.contains(Vec3.atCenterOf(p))) {
                        return false;
                     }

                     for (Direction direction : Direction.values()) {
                        BlockPos next = p.relative(direction);
                        if (seen.add(next.asLong()) && this.isTraversable(next)) {
                           queue.add(next);
                        }
                     }
                  }

                  seen.remove(target.asLong());
                  this.mergeSyncRoom(target.asLong(), seen);
                  AutismClientAddon.LOG
                     .info("[AI] build: placing {} at {} would close the body in; leaving first ({} cells)", cell.block(), target.toShortString(), seen.size());
                  return true;
               } else {
                  return false;
               }
            }

            AiOperation breakOutOp() {
               HashSet<Long> keep = new HashSet<>();

               for (long key : this.close_Set) {
                  BlockPos pos = BlockPos.of(key);
                  BlockState live = mc.level.getBlockState(pos);
                  if (!live.isAir()
                     && (
                        live.hasBlockEntity()
                           || !live.getFluidState().isEmpty()
                           || this.existingSupportKeys.contains(key)
                           || AiItems.requiresSilkTouchOrUnobtainable(BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString())
                           || this.breakOutTries > 2 && live.isSignalSource()
                     )) {
                     keep.add(key);
                  }
               }

               final AABB box = this.buildBounds;
               var outside = new autismclient.ai.engine.AiPathfinder.PathGoal() {
                  @Override
                  public boolean isInGoal(int x, int y, int z) {
                     return !box.contains(x + 0.5, y + 0.5, z + 0.5) && !box.contains(x + 0.5, y + 1.5, z + 0.5);
                  }

                  @Override
                  public double heuristic(int x, int y, int z) {
                     double cx = x + 0.5;
                     double cy = y + 0.5;
                     double cz = z + 0.5;
                     if (!box.contains(cx, cy, cz)) {
                        return 0.0;
                     }

                     double edge = Math.min(Math.min(cx - box.minX, box.maxX - cx), Math.min(cz - box.minZ, box.maxZ - cz));
                     return Math.min(edge, cy - box.minY) * 4.0;
                  }
               };
               AutismClientAddon.LOG.info("[AI] build: closed in at {}; breaking out (try {})", mc.player.blockPosition().toShortString(), this.breakOutTries);
               return AiGoto.travelToCompat(
                  mc.player.blockPosition(),
                  1.5,
                  true,
                  true,
                  false,
                  30,
                  autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                  true,
                  2.0,
                  AiGoto.protectingCellsCompat(outside, keep)
               );
            }

            void fillSignEditor(AbstractSignEditScreen editor) {
               try {
                  Class<AbstractSignEditScreen> screen = AbstractSignEditScreen.class;
                  Field signField = screen.getDeclaredField("sign");
                  Field slotField = screen.getDeclaredField("slot");
                  Field messagesField = screen.getDeclaredField("messages");
                  signField.setAccessible(true);
                  slotField.setAccessible(true);
                  messagesField.setAccessible(true);
                  SignBlockEntity sign = (SignBlockEntity)signField.get(editor);
                  String text = power.signText().get(sign.getBlockPos().subtract(blueprint.origin()).asLong());
                  if (text != null && slotField.get(editor) == SignTextSlot.FRONT) {
                     String[] messages = (String[])messagesField.get(editor);
                     String[] lines = text.split("\n", -1);

                     for (int i = 0; i < messages.length && i < lines.length; i++) {
                        messages[i] = lines[i];
                     }
                  }
               } catch (ReflectiveOperationException | RuntimeException e) {
                  AutismClientAddon.LOG.warn("[AI] build: sign text not written: {}", e.toString());
               }

               mc.gui.setScreen(null);
            }

            boolean isTraversable(BlockPos p) {
               BlockState state = mc.level.getBlockState(p);
               return state.getCollisionShape(mc.level, p).isEmpty()
                  || state.is(BlockTags.CLIMBABLE)
                  || state.getBlock() instanceof DoorBlock
                  || state.getBlock() instanceof TrapDoorBlock
                  || state.getBlock() instanceof FenceGateBlock;
            }

            void init_V_BlockPos_Z(BlockPos target, boolean cleanup) {
               AiBuilder.CellSpec wantedTarget = this.load_Map.get(target.asLong());
               if ((cleanup || wantedTarget != null && !AiBuilder.matchesIgnoringUnpairedChest(wantedTarget, mc.level.getBlockState(target)))
                  && this.ceilingAbove(target.above()) != null) {
                  if (!cleanup) {
                     for (Direction side : Plane.HORIZONTAL) {
                        BlockPos next = target.relative(side);
                        if (mc.level.getBlockState(next).getCollisionShape(mc.level, next).isEmpty()) {
                           return;
                        }
                     }
                  }

                  BlockPos column = null;
                  double nearest = 1.7976931348623157E308;

                  for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                     BlockPos candidate = target.relative(direction);
                     AiBuilder.CellSpec wanted = this.load_Map.get(candidate.asLong());
                     BlockState live = mc.level.getBlockState(candidate);
                     if (wanted != null
                        && !live.hasBlockEntity()
                        && live.getFluidState().isEmpty()
                        && Block.isShapeFullBlock(AiBuilder.toBlockState(wanted).getCollisionShape(mc.level, candidate))
                        && AiSchematicActions.init(mc.level, candidate, (int)Math.ceil(mc.player.getY()) + 1)) {
                        double d = mc.player.position().distanceToSqr(Vec3.atCenterOf(candidate));
                        if (d < nearest) {
                           column = candidate;
                           nearest = d;
                        }
                     }
                  }

                  if (column != null) {
                     Integer before = this.read_Map.get(BlockPos.asLong(column.getX(), 0, column.getZ()));
                     if (before == null || before > target.getY()) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] build: {} is buried; access shaft down column {} from y {}", target.toShortString(), column.toShortString(), target.getY()
                           );
                     }

                     this.read_Map.merge(BlockPos.asLong(column.getX(), 0, column.getZ()), target.getY(), Math::min);
                  }
               }
            }

            BlockPos ceilingAbove(BlockPos bottom) {
               BlockPos ceiling = null;
               int top = Math.min((int)this.buildBounds.maxY - 1, (int)Math.ceil(mc.player.getY()) + 1);

               for (int y = bottom.getY(); y <= top; y++) {
                  BlockPos pos = new BlockPos(bottom.getX(), y, bottom.getZ());
                  BlockState live = mc.level.getBlockState(pos);
                  if (this.load_Map.containsKey(pos.asLong())
                     && !live.hasBlockEntity()
                     && live.getFluidState().isEmpty()
                     && !live.getCollisionShape(mc.level, pos).isEmpty()) {
                     ceiling = pos;
                  }
               }

               return ceiling;
            }

            boolean write(AiBuilder.CellSpec cell, BlockPos pos) {
               Block block = AiBuilder.resolveBlock(cell);
               Set<Long> protectedNow = this.cachedProtectedCells();
               ArrayList<Direction> directions = new ArrayList<>(
                  List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)
               );
               Direction look = this.parseDirection(cell.properties().getOrDefault("facing", ""));
               AiBuilder.OrientationKind strategy = AiBuilder.orientationKindForProperty(block, "facing");
               if (look != null) {
                  if (strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE) {
                     look = look.getOpposite();
                  } else if (strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE) {
                     look = look.getCounterClockWise();
                  } else if (strategy == AiBuilder.OrientationKind.FACE_DERIVED) {
                     look = look.getOpposite();
                  }

                  directions.remove(look);
                  directions.add(0, look);
               }

               for (Direction direction : directions) {
                  if (AiBuilder.allowsSupportOnSide(cell, block, direction)) {
                     BlockPos support = pos.relative(direction);
                     BlockState state = mc.level.getBlockState(support);
                     if (!protectedNow.contains(support.asLong())
                        && !this.set_Set.contains(support.asLong())
                        && state.canBeReplaced()
                        && state.getFluidState().isEmpty()) {
                        this.set_Set.add(support.asLong());
                        this.init_Z = true;
                        this.activeSubtask = AiSchematicActions.init_AiOperation_BlockPos(support, pos, protectedNow, blueprint.origin().getY());
                        return true;
                     }
                  }
               }

               return false;
            }

            boolean placeCeilingSupport(AiBuilder.CellSpec cell, BlockPos pos) {
               BlockPos ceiling = pos.above();
               if (this.ceilingSupportTried.contains(pos.asLong())) {
                  return false;
               }

               BlockState state = mc.level.getBlockState(ceiling);
               if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                  HashSet<Long> protectedNow = new HashSet<>(this.cachedProtectedCells());
                  protectedNow.remove(ceiling.asLong());
                  boolean anchored = false;

                  for (Direction direction : Direction.values()) {
                     BlockPos next = ceiling.relative(direction);
                     if (!next.equals(pos) && !mc.level.getBlockState(next).getCollisionShape(mc.level, next).isEmpty()) {
                        anchored = true;
                     }
                  }

                  if (!anchored) {
                     return false;
                  }

                  this.ceilingSupportTried.add(pos.asLong());
                  AutismClientAddon.LOG
                     .info("[AI] build: temporary ceiling at {} for the upward-facing {} at {}", ceiling.toShortString(), cell.block(), pos.toShortString());
                  this.init_Z = true;
                  this.activeSubtask = AiSchematicActions.init_AiOperation_BlockPos(ceiling, pos, protectedNow, blueprint.origin().getY());
                  return true;
               } else {
                  return false;
               }
            }

            boolean sync(AiBuilder.CellSpec cell, BlockPos pos) {
               if (this.push(pos)) {
                  return true;
               }

               if (this.tick_Z) {
                  return false;
               }

               this.tick_Z = true;
               Set<Long> protectedNow = this.cachedProtectedCells();
               autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.constraintFor(cell, AiBuilder.resolveBlock(cell));
               Item item = AiBuilder.resolveBlock(cell).asItem();
               if (item != Items.AIR && this.countInInventory(item) != 0) {
                  Direction side = constraint.playerFacing() == null ? constraint.clickFace() : constraint.playerFacing().getOpposite();
                  ArrayList<Direction> directions = new ArrayList<>(List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST));
                  if (side != null && side.getAxis().isHorizontal()) {
                     directions.remove(side);
                     directions.addFirst(side);
                  }

                  BlockPos best = null;
                  double cost = 1.7976931348623157E308;

                  for (Direction direction : directions) {
                     for (int distance = 2; distance <= 3; distance++) {
                        for (int rise = -2; rise <= 0; rise++) {
                           BlockPos feet = pos.relative(direction, distance).above(rise);
                           BlockState floor = mc.level.getBlockState(feet.below());
                           VoxelShape floorShape = floor.getCollisionShape(mc.level, feet.below());
                           if (feet.getY() >= blueprint.origin().getY()
                              && (!protectedNow.contains(feet.below().asLong()) || !floorShape.isEmpty() && !(Math.abs(floorShape.max(Axis.Y) - 1.0) > 0.001))) {
                              Vec3 position = Vec3.atBottomCenterOf(feet);
                              if (mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(position.subtract(mc.player.position())))) {
                                 BlockPlacementPlanner.PlacementPlan proved = autismclient.ai.engine.PlacementGoal.planPlacement(mc, pos, item, constraint, position, 48);
                                 if (proved != null
                                    && (
                                       !floorShape.isEmpty()
                                          || !new AABB(feet.below())
                                             .clip(
                                                position.add(0.0, constraint.crouchForLook() ? 1.27 : mc.player.getEyeHeight(), 0.0),
                                                proved.hit().getLocation()
                                             )
                                             .isPresent()
                                    )) {
                                    if (protectedNow.contains(feet.below().asLong())) {
                                       AABB body = mc.player.getBoundingBox().move(position.subtract(mc.player.position()));
                                       boolean supported = false;

                                       for (AABB local : floorShape.toAabbs()) {
                                          AABB box = local.move(feet.getX(), feet.getY() - 1, feet.getZ());
                                          if (Math.abs(box.maxY - body.minY) < 0.001
                                             && Math.min(box.maxX, body.maxX) - Math.max(box.minX, body.minX) > 0.15
                                             && Math.min(box.maxZ, body.maxZ) - Math.max(box.minZ, body.minZ) > 0.15) {
                                             supported = true;
                                          }
                                       }

                                       if (!supported) {
                                          continue;
                                       }
                                    }

                                    double score = position.distanceToSqr(mc.player.position())
                                       + (direction == side ? 0 : 16)
                                       + (floorShape.isEmpty() ? 40.0 : 0.0);
                                    if (score < cost) {
                                       best = feet;
                                       cost = score;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (best == null) {
                     return false;
                  }

                  this.run_Z = true;
                  this.activeSubtask = AiGoto.travelToCompat(
                     best,
                     0.35,
                     true,
                     true,
                     true,
                     60,
                     autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                     true,
                     2.0,
                     AiGoto.protectingCellsBelowYCompat(
                        autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(best, 0.35), protectedNow, blueprint.origin().getY()
                     )
                  );
                  return true;
               } else {
                  return false;
               }
            }

            boolean push(BlockPos target) {
               if (schematic && this.ownsScaffolding && this.cellCursor < this.orderedCells.size()) {
                  BlockPos worker = autismclient.ai.engine.AiStance.standingPos(mc);
                  if (target.getY() < worker.getY() + 2) {
                     return false;
                  }

                  int best = -1;
                  boolean fromDeferred = false;
                  double score = 1.0 / 0.0;

                  for (int pass = 0; pass < 2; pass++) {
                     List<AiBuilder.CellSpec> source = pass == 0 ? this.orderedCells : this.deferredCells;
                     int start = pass == 0 ? this.cellCursor + 1 : Math.max(0, source.size() - 192);
                     int end = pass == 0 ? Math.min(source.size(), this.cellCursor + 192) : source.size();

                     for (int i = start; i < end; i++) {
                        AiBuilder.CellSpec candidate = source.get(i);
                        BlockPos pos = blueprint.origin().offset(candidate.dx(), candidate.dy(), candidate.dz());
                        if (!pos.equals(target)
                           && pos.getY() >= worker.getY() - 1
                           && pos.getY() <= worker.getY()
                           && Math.abs(pos.getX() - worker.getX()) <= 2
                           && Math.abs(pos.getZ() - worker.getZ()) <= 2
                           && this.isWalkingPlaceable(candidate, pos)
                           && this.init_Z_TableIndex_StreamU_BlockPos(candidate, pos)
                           && !AiBuilder.isCellSatisfied(candidate, mc.level.getBlockState(pos), power.substitutes(), true)
                           && !this.read(candidate, pos)
                           && AiSchematicActions.init(mc.level, pos, this.load_Map) == null) {
                           BlockPos above = pos.above();
                           if (mc.level
                              .noCollision(
                                 mc.player,
                                 new AABB(above.getX() + 0.2, above.getY(), above.getZ() + 0.2, above.getX() + 0.8, above.getY() + 1.8, above.getZ() + 0.8)
                              )) {
                              double progress = above.distSqr(target) + pos.distSqr(worker) * 0.25;
                              if (!(above.distSqr(target) >= worker.distSqr(target)) && progress < score) {
                                 score = progress;
                                 best = i;
                                 fromDeferred = pass == 1;
                              }
                           }
                        }
                     }
                  }

                  if (best < 0) {
                     return false;
                  }

                  AiBuilder.CellSpec step;
                  if (fromDeferred) {
                     step = this.deferredCells.remove(best);
                     this.deferredReasons.remove(best);
                  } else {
                     step = this.orderedCells.remove(best);
                  }

                  this.orderedCells.add(this.cellCursor, step);
                  this.permanentAccessSteps++;
                  this.resetCellState();
                  return true;
               } else {
                  return false;
               }
            }

            boolean load(AiBuilder.CellSpec cell) {
               if (this.cellCursor < this.orderedCells.size() && this.push(this.currentCellPos())) {
                  return true;
               }

               if (this.ownsScaffolding && cell.dy() > this.stairLayerY) {
                  this.stairLayerY = cell.dy();
                  if (AiSchematicActions.init(blueprint, this.stairLayerY).isEmpty()) {
                     return false;
                  }

                  this.accessStairsReady = false;
                  return true;
               } else {
                  return false;
               }
            }

            BlockHitResult orientPlacementHit(AiBuilder.CellSpec cell, BlockPos pos, Block target, BlockHitResult hit) {
               this.pendingYaw = 0.0F / 0.0F;
               this.yawTolerance = 40.0;
               if (cell.properties().isEmpty()) {
                  return hit;
               }

               BlockPos support = hit.getBlockPos();
               Direction face = hit.getDirection();
               String axis = cell.properties().get("axis");
               Direction wantedFace = axis != null ? this.faceForAxis(axis, face) : null;
               String hanging = cell.properties().get("hanging");
               if (target instanceof LanternBlock && hanging != null) {
                  wantedFace = "true".equals(hanging) ? Direction.DOWN : Direction.UP;
               }

               String facingProp = cell.properties().get("facing");
               if (facingProp != null && AiBuilder.isSixWayFaceDerivedBlock(target)) {
                  wantedFace = Direction.byName(facingProp);
               }

               boolean pairing = target instanceof ChestBlock && AiBuilder.hasUnpairedChestPartner(cell, pos, mc.level);
               if (pairing) {
                  wantedFace = AiBuilder.chestPartnerDirection(cell).getOpposite();
               }

               if (facingProp != null && target instanceof HopperBlock) {
                  Direction wanted = Direction.byName(facingProp);
                  wantedFace = wanted == Direction.DOWN ? (face.getAxis() == Axis.Y ? face : Direction.UP) : (wanted == null ? null : wanted.getOpposite());
               }

               if (facingProp != null && target instanceof TrapDoorBlock && face.getAxis().isHorizontal()) {
                  wantedFace = this.parseDirection(facingProp);
               }

               if (facingProp != null && AiBuilder.usesWallClickFace(target, cell)) {
                  Direction wanted = this.parseDirection(facingProp);
                  if (wanted == null) {
                     this.recordUnappliedProperty(cell, pos, Map.entry("facing", facingProp), "not a horizontal direction");
                  } else {
                     wantedFace = wanted;
                  }
               }

               if (wantedFace != null && face != wantedFace) {
                  BlockHitResult refaced = this.hitOnFace(pos, wantedFace);
                  if (refaced != null) {
                     hit = refaced;
                     face = wantedFace;
                     support = refaced.getBlockPos();
                  } else {
                     this.recordUnappliedProperty(
                        cell,
                        pos,
                        Map.entry(
                           axis != null ? "axis" : (hanging != null ? "hanging" : "facing"), axis != null ? axis : (hanging != null ? hanging : facingProp)
                        ),
                        "no solid " + wantedFace + " face of the target cell is clickable"
                     );
                  }
               }

               Boolean high = null;
               String type = cell.properties().get("type");
               if (type != null && AiBuilder.orientationKindForProperty(target, "type") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
                  high = !AiBuilder.isDoubleSlabCell(cell) && type.equalsIgnoreCase("top");
               }

               String half = cell.properties().get("half");
               if (half != null && AiBuilder.orientationKindForProperty(target, "half") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
                  high = half.equalsIgnoreCase("top");
               }

               if (high != null) {
                  if (face == Direction.UP && high || face == Direction.DOWN && !high) {
                     this.recordUnappliedProperty(
                        cell,
                        pos,
                        Map.entry(half != null ? "half" : "type", half != null ? half : type),
                        "an " + face.getName() + " face always yields the other half with vanilla clicks"
                     );
                  } else if (face.getAxis().isHorizontal() && !schematic) {
                     hit = new BlockHitResult(new Vec3(hit.getLocation().x, support.getY() + (high ? 0.75 : 0.25), hit.getLocation().z), face, support, false);
                  }
               }

               if (facingProp != null && !pairing) {
                  AiBuilder.OrientationKind strategy = AiBuilder.facingOrientationKind(cell, target);
                  if (strategy == AiBuilder.OrientationKind.FACE_DERIVED
                     && !AiBuilder.usesWallClickFace(target, cell)
                     && !(target instanceof HopperBlock)
                     && !AiBuilder.isSixWayFaceDerivedBlock(target)) {
                     strategy = AiBuilder.OrientationKind.PLAYER_FACING;
                  }

                  if (strategy == AiBuilder.OrientationKind.PLAYER_FACING
                     || strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE
                     || strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE
                     || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION
                     || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION_OPPOSITE) {
                     Direction wanted = Direction.byName(facingProp.toLowerCase(Locale.ROOT));
                     if (wanted == null) {
                        this.recordUnappliedProperty(cell, pos, Map.entry("facing", facingProp), "not a horizontal direction");
                     } else if (wanted.getAxis().isHorizontal()) {
                        this.pendingYaw = (strategy != AiBuilder.OrientationKind.PLAYER_FACING && strategy != AiBuilder.OrientationKind.PLAYER_DIRECTION
                              ? (strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE ? wanted.getCounterClockWise() : wanted.getOpposite())
                              : wanted)
                           .toYRot();
                     }
                  }
               }

               autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.constraintFor(cell, target);
               if (constraint.yaw() != null) {
                  this.pendingYaw = constraint.yaw();
                  this.yawTolerance = constraint.yawTolerance();
               }

               if (target instanceof DoorBlock && cell.properties().containsKey("hinge") && facingProp != null && face.getAxis() == Axis.Y) {
                  Direction facing = this.parseDirection(facingProp);
                  if (facing != null) {
                     Direction side = "left".equals(cell.properties().get("hinge")) ? facing.getCounterClockWise() : facing.getClockWise();
                     hit = new BlockHitResult(
                        new Vec3(pos.getX() + 0.5 + side.getStepX() * 0.25, hit.getLocation().y, pos.getZ() + 0.5 + side.getStepZ() * 0.25),
                        face,
                        support,
                        false
                     );
                  }
               }

               return hit;
            }

            BlockHitResult hitOnFace(BlockPos pos, Direction face) {
               BlockPos support = pos.relative(face.getOpposite());
               return mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()
                  ? null
                  : new BlockHitResult(
                     new Vec3(support.getX() + 0.5 + face.getStepX() * 0.5, support.getY() + 0.5, support.getZ() + 0.5 + face.getStepZ() * 0.5),
                     face,
                     support,
                     false
                  );
            }

            Direction faceForAxis(String axis, Direction current) {
               Axis var10000;
               switch (axis.toLowerCase(Locale.ROOT)) {
                  case "x":
                     var10000 = Axis.X;
                     break;
                  case "y":
                     var10000 = Axis.Y;
                     break;
                  default:
                     var10000 = Axis.Z;
               }

               Axis wanted = var10000;
               return current.getAxis() == wanted ? current : Direction.fromAxisAndDirection(wanted, AxisDirection.POSITIVE);
            }

            Direction parseDirection(String name) {
               Direction var10000;
               switch (name.toLowerCase(Locale.ROOT)) {
                  case "north":
                     var10000 = Direction.NORTH;
                     break;
                  case "east":
                     var10000 = Direction.EAST;
                     break;
                  case "south":
                     var10000 = Direction.SOUTH;
                     break;
                  case "west":
                     var10000 = Direction.WEST;
                     break;
                  default:
                     var10000 = null;
               }

               return var10000;
            }

            boolean init_Z_TableIndex_StreamU_2(AiBuilder.CellSpec cell, BlockPos pos, BlockState live) {
               if (AiBuilder.isDoubleSlabCell(cell) && live.getBlock() instanceof SlabBlock && live.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                  this.pendingPlacePos = null;
                  this.pendingAimPoint = null;
                  this.pendingPlaceCell = null;
                  this.matchedConfirmations = this.verifyTicks = 0;
                  return false;
               }

               if (schematic && AiBuilder.needsWaterlogToggleOnly(cell, live)) {
                  this.pendingPlacePos = null;
                  this.pendingAimPoint = null;
                  this.pendingPlaceCell = null;
                  this.matchedConfirmations = this.verifyTicks = 0;
                  this.cellConstructionOp = AiSchematicActions.init(cell, pos, this.cachedProtectedCells());
                  return false;
               }

               Iterator mismatch = cell.properties().entrySet().iterator();

               Property<? extends Comparable<?>> open;
               boolean cycling;
               while (true) {
                  if (!mismatch.hasNext()) {
                     String mismatchx = this.propertyMismatch(cell, pos, live);
                     if (mismatchx != null) {
                        if (schematic && this.deferPassCount < 2) {
                           this.deferCurrentCell(mismatchx);
                        } else {
                           this.failCurrentCell(mismatchx);
                        }
                     }

                     return true;
                  }

                  Entry<String, String> entry = (Entry<String, String>)mismatch.next();
                  if (AiBuilder.orientationKindForProperty(this.pendingPlaceBlock, entry.getKey()) == AiBuilder.OrientationKind.POST_INTERACT) {
                     open = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
                     if (open != null && !String.valueOf(live.getValue(open)).equalsIgnoreCase(entry.getValue())) {
                        cycling = !entry.getKey().equals("open") && !entry.getKey().equals("powered");
                        if (!AiBuilder.isStackableCountProperty(live.getBlock(), entry.getKey())
                           || Integer.parseInt(String.valueOf(live.getValue(open))) <= Integer.parseInt(entry.getValue())) {
                           Item needed = AiBuilder.postInteractToolItem(live.getBlock(), entry.getKey());
                           if (needed != null
                              && !mc.player.getMainHandItem().is(needed)
                              && AiItems.equipHotbar(AiItems.resolveItem(BuiltInRegistries.ITEM.getKey(needed).toString())) < 0) {
                              this.recordUnappliedProperty(cell, pos, entry, "no " + BuiltInRegistries.ITEM.getKey(needed).getPath() + " to set it with");
                           } else if (cycling) {
                              if (++this.next_I <= (entry.getKey().equals("note") ? 900 : 160)) {
                                 String now = String.valueOf(live.getValue(open));
                                 if (now.equals(this.init_Str) && ++this.prev < 20) {
                                    return false;
                                 }

                                 this.prev = 0;
                                 break;
                              }

                              this.recordUnappliedProperty(cell, pos, entry, "the interaction cycle did not reach the requested value");
                           } else if (this.read_Z) {
                              if (++this.stop_I <= 20) {
                                 return false;
                              }

                              this.recordUnappliedProperty(cell, pos, entry, "the requested toggle was not accepted");
                           } else {
                              if (++this.stop_I <= 20) {
                                 break;
                              }

                              this.recordUnappliedProperty(cell, pos, entry, "the requested toggle timed out");
                           }
                        } else {
                           this.recordUnappliedProperty(cell, pos, entry, "more were placed than requested");
                        }
                     }
                  }
               }

               BlockHitResult visible = live.getBlock() instanceof SnowLayerBlock
                  ? autismclient.ai.engine.AiBlockInteraction.findFaceHit(mc, pos, Direction.UP)
                  : autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, pos);
               if (visible == null) {
                  return false;
               }

               this.pendingAimPoint = visible.getLocation();
               AiGameOperations.aimAndStep(this.placementAim, visible.getLocation());
               if (autismclient.ai.engine.AiAim.isLookingAtBlock(pos)) {
                  BlockHitResult hitResult = autismclient.ai.engine.AiAim.raycastLookBlock();
                  if (hitResult != null) {
                     mc.options.keyShift.setDown(false);
                     if (mc.player.isShiftKeyDown()) {
                        return false;
                     }

                     mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hitResult);
                     if (!cycling) {
                        this.read_Z = true;
                     } else if (!(live.getBlock() instanceof DiodeBlock)) {
                        this.init_Str = String.valueOf(live.getValue(open));
                     }

                     this.stop_I = 0;
                     this.pendingAimPoint = visible.getLocation();
                  }
               }

               return false;
            }

            String propertyMismatch(AiBuilder.CellSpec cell, BlockPos pos, BlockState live) {
               for (Entry<String, String> entry : cell.properties().entrySet()) {
                  if (!AiBuilder.isAutoComputedProperty(live.getBlock(), entry.getKey())
                     && (
                        !entry.getKey().equals("type")
                           || !AiBuilder.isUnpairedDoubleChest(cell, live)
                           || AiBuilder.chestPartnerDirection(cell) == null
                           || mc.level.getBlockState(pos.relative(AiBuilder.chestPartnerDirection(cell))).getBlock() instanceof ChestBlock
                     )) {
                     Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock()
                        .getStateDefinition()
                        .getProperty(entry.getKey());
                     if (property == null) {
                        return "requested property is absent: " + entry.getKey();
                     }

                     String actual = String.valueOf(live.getValue(property));
                     if (!AiBuilder.propertyValuesMatch(live.getBlock(), entry.getKey(), actual, entry.getValue())) {
                        return "state mismatch for " + entry.getKey() + ": expectedState=" + entry.getValue() + ", actualState=" + actual;
                     }
                  }
               }

               return null;
            }

            void recordUnappliedProperty(AiBuilder.CellSpec cell, BlockPos pos, Entry<String, String> property, String reason) {
               if (!AiBuilder.isAutoComputedProperty(AiBuilder.resolveBlock(cell), property.getKey()) && this.reportedPropertyKeys.add(property.getKey()) && this.unappliedProperties.size() < 32) {
                  this.unappliedProperties
                     .add(
                        AiJson.objectOf(
                           "x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "property", property.getKey(), "value", property.getValue(), "reason", reason
                        )
                     );
               }
            }

            BlockPos currentCellPos() {
               AiBuilder.CellSpec cell = this.orderedCells.get(this.cellCursor);
               return blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
            }

            boolean isWalkingPlaceable(AiBuilder.CellSpec cell, BlockPos pos) {
               Block target = AiBuilder.resolveBlock(cell);
               return cell.properties().isEmpty()
                  && target.asItem() != Items.AIR
                  && !(target instanceof FallingBlock)
                  && !this.isRedstoneReactive(target)
                  && !target.defaultBlockState().hasBlockEntity()
                  && !target.defaultBlockState().isSignalSource()
                  && !autismclient.ai.engine.AiPathfinder.LiveGridView.isFloorHazardCompat(target.defaultBlockState())
                  && Block.isShapeFullBlock(AiBuilder.toBlockState(cell).getCollisionShape(mc.level, pos));
            }

            Vec3 planWalkingPlacement(Predicate<AABB> clear) {
               this.walkingClearCheck = clear;
               this.walkingPlacementPlan = null;
               AiSchematicApproach.Inner_DataUtil job = this.activeApproachJob != null ? this.activeApproachJob : this.lastApproachJob;
               Set<Long> excluded = new HashSet<>(this.write_Set);
               excluded.addAll(this.read_Set);
               this.readyQueue
                  .sync(
                     this,
                     AiBuilder.filterCommittedInLookahead(this.batchPositions, this.orderedCells, this.cellCursor, blueprint.origin(), this.load_Map),
                     excluded,
                     this.placeAckTracker.pendingPos()
                  );
               if (this.walkingPlacementCell != null) {
                  BlockPos pos = blueprint.origin()
                     .offset(this.walkingPlacementCell.dx(), this.walkingPlacementCell.dy(), this.walkingPlacementCell.dz());
                  if (!pos.equals(this.readyQueue.activePos())) {
                     this.walkingPlacementCell = null;
                  }
               }

               if (this.placeAckTracker.pendingPos() == null
                  && !holdSneak
                  && !this.set_Z
                  && !mc.player.isShiftKeyDown()
                  && mc.player.containerMenu == mc.player.inventoryMenu
                  && job != null) {
                  Set<Long> tried = new HashSet<>();

                  for (int scan = 0; scan < 32; scan++) {
                     BlockPos pos = this.readyQueue.nextPos();
                     if (pos == null) {
                        break;
                     }

                     if (!tried.add(pos.asLong())) {
                        this.readyQueue.complete(pos);
                     } else {
                        AiBuilder.CellSpec cell = this.load_Map.get(pos.asLong());
                        if (cell == null) {
                           this.readyQueue.complete(pos);
                        } else {
                           this.walkingPlacementCell = cell;
                           if (this.isWalkingCandidate(cell, pos, job, clear)) {
                              return this.walkingPlacementPlan.hit().getLocation();
                           }

                           this.readyQueue.requeue(pos);
                        }
                     }
                  }

                  this.walkingPlacementCell = null;
                  return null;
               } else {
                  return null;
               }
            }

            boolean isWalkingCandidate(AiBuilder.CellSpec cell, BlockPos pos, AiSchematicApproach.Inner_DataUtil job, Predicate<AABB> clear) {
               if (job.read(pos)
                  && this.isWalkingPlaceable(cell, pos)
                  && AiTerrain.isLoaded(mc.level, pos)
                  && !(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Math.pow(mc.player.blockInteractionRange() - 0.4, 2.0))
                  && clear.test(new AABB(pos))
                  && mc.level.getBlockState(pos).canBeReplaced()
                  && mc.level.getBlockState(pos).getFluidState().isEmpty()
                  && AiBuilder.toBlockState(cell).canSurvive(mc.level, pos)
                  && AiSchematicActions.init(mc.level, pos, this.load_Map) == null) {
                  Item item = AiBuilder.resolveBlock(cell).asItem();
                  if (this.countInInventory(item) != 0
                     && !autismclient.ai.engine.PlacementGoal.canPlace(mc, pos, item, this.constraintFor(cell, AiBuilder.resolveBlock(cell)))) {
                     BlockPlacementPlanner.PlacementPlan candidate = autismclient.ai.engine.PlacementGoal.planPlacement(
                        mc, pos, item, this.constraintFor(cell, AiBuilder.resolveBlock(cell)), 32
                     );
                     if (candidate == null || this.read(cell, pos)) {
                        return false;
                     }

                     if (autismclient.util.InventorySlotHelper.ensureItemInHotbar(
                           mc, MacroItemTarget.ofRegistryId(BuiltInRegistries.ITEM.getKey(item).toString()), mc.player.getInventory().getSelectedSlot()
                        )
                        < 0) {
                        return false;
                     }

                     this.walkingPlacementPlan = candidate;
                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            void tryWalkingClick() {
               if (this.walkingPlacementCell != null
                  && this.walkingPlacementPlan != null
                  && this.placeAckTracker.pendingPos() == null
                  && this.walkingClearCheck != null
                  && mc.player.onGround()
                  && !(mc.player.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4)) {
                  BlockPos pos = blueprint.origin()
                     .offset(this.walkingPlacementCell.dx(), this.walkingPlacementCell.dy(), this.walkingPlacementCell.dz());
                  AiBuilder.CellSpec cell = this.load_Map.get(pos.asLong());
                  AiSchematicApproach.Inner_DataUtil job = this.activeApproachJob != null ? this.activeApproachJob : this.lastApproachJob;
                  if (cell != null && job != null && pos.equals(this.readyQueue.activePos()) && this.isWalkingCandidate(cell, pos, job, this.walkingClearCheck)) {
                     BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                     if (hit != null
                        && AiBuilder.matchesPlacementFace(
                           this.walkingPlacementPlan.hit().getBlockPos(),
                           this.walkingPlacementPlan.hit().getDirection(),
                           hit.getBlockPos(),
                           hit.getDirection()
                        )) {
                        Item item = AiBuilder.resolveBlock(cell).asItem();
                        if (AiBuilder.isExpectedItemSelected(item, mc.player.getMainHandItem(), init_Z_Minecraft_Item(mc, item))
                           && BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND).lands(hit, pos)) {
                           BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                           int beforeSequence = prediction.currentSequence();
                           long revision = AiServerEvidence.blockRevision(pos);
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           this.walkingClickCount++;
                           int sentSequence = prediction.currentSequence();
                           if (sentSequence == beforeSequence) {
                              this.retryWalkingPlacement(cell, pos);
                           } else {
                              this.placeAckTracker.track(pos, revision, sentSequence, mc.player.tickCount);
                              this.pendingAckCell = cell;
                              if (AiBuilder.fluidOfCell(cell) != null || cell.properties().containsKey("waterlogged")) {
                                 this.lastFluidPlacementTick = mc.player.tickCount;
                              }

                              this.readyQueue.complete(pos);
                              this.walkingPlacementCell = null;
                              this.walkingPlacementPlan = null;
                           }
                        }
                     }
                  }
               }
            }

            void pollPlaceAck() {
               BlockPos pos = this.placeAckTracker.pendingPos();
               if (pos != null) {
                  AiBuilder.CellSpec cell = this.pendingAckCell;
                  AiBuilder.PlaceAckTracker.AckState outcome = this.placeAckTracker
                     .poll(
                        AiServerEvidence.blockRevision(pos),
                        AiBuilder.isCellSatisfied(cell, mc.level.getBlockState(pos), power.substitutes(), true),
                        autismclient.util.villager.BlockChangeAckTracker.isAcknowledged(mc.level, this.placeAckTracker.sentSequence()),
                        mc.player.tickCount
                     );
                  if (outcome == AiBuilder.PlaceAckTracker.AckState.CONFIRMED) {
                     if (this.write_Set.add(pos.asLong())) {
                        this.placedCount++;
                        this.walkingPlacementCount++;
                     }

                     AiScaffold.forget(pos);
                     this.pendingAckCell = null;
                     this.placeAttempts.remove(pos.asLong());
                  } else if (outcome == AiBuilder.PlaceAckTracker.AckState.RETRY) {
                     this.pendingAckCell = null;
                     this.walkingRefusalCount++;
                     this.retryWalkingPlacement(cell, pos);
                  }
               }
            }

            void retryWalkingPlacement(AiBuilder.CellSpec cell, BlockPos pos) {
               this.walkingPlacementCell = null;
               this.walkingPlacementPlan = null;
               if (!AiBuilder.incrementAndCheckAttemptLimit(this.placeAttempts, pos, this.placeAttemptLimit)) {
                  this.readyQueue.requeue(pos);
               } else {
                  this.readyQueue.complete(pos);
                  this.read_Set.add(pos.asLong());
                  int removed = AiBuilder.deferQueuedCell(
                     this.orderedCells, this.deferredCells, this.deferredReasons, this.cellCursor, blueprint.origin(), cell, pos, "the server refused the moving placement"
                  );
                  if (removed == this.cellCursor) {
                     if (schematic) {
                        mc.options.keyShift.setDown(false);
                     }

                     this.resetCellState();
                  }
               }
            }

            void load_V(BlockPos pos) {
               if (!pos.equals(this.batchFocusPos)) {
                  this.batchFocusPos = pos;
                  this.parse = 0;
                  this.rejectedBatchCells.clear();
               }

               if (schematic
                  && this.cellCursor < this.orderedCells.size()
                  && !AiBuilder.collidesWithEntityBox(this.orderedCells.get(this.cellCursor), pos, mc.player.getBoundingBox(), mc.level)
                  && this.init_Z_TableIndex_StreamU_BlockPos(this.orderedCells.get(this.cellCursor), pos)
                  && this.currentCellAttempts < 2) {
                  this.currentPlacementPlan = null;
               } else if (!schematic || !this.push(pos)) {
                  if (schematic && ++this.parse > 12) {
                     this.deferCurrentCell("approaches end where the cell cannot be placed");
                  } else {
                     for (String id : power.candidates(this.orderedCells.get(this.cellCursor).block())) {
                        Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
                        if (block != null && block.asItem() != Items.AIR && this.countInInventory(block.asItem()) > 0) {
                           if (this.deferPassCount > 0 && this.add_Set.add(pos.asLong())) {
                              BlockPos blocking = null;

                              for (int dx = -2; dx <= 2 && blocking == null; dx++) {
                                 for (int dy = -2; dy <= 2 && blocking == null; dy++) {
                                    for (int dz = -2; dz <= 2 && blocking == null; dz++) {
                                       BlockPos cell = pos.offset(dx, dy, dz);
                                       if (!this.buildBounds.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5)
                                          && AiScaffold.recordedBlock(cell) != null
                                          && AiScaffold.matches(this.scaffoldBlockAt(cell), mc.level.getBlockState(cell).getBlock())) {
                                          blocking = cell;
                                       }
                                    }
                                 }
                              }

                              if (blocking != null) {
                                 this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(blocking, 10);
                                 return;
                              }
                           }

                           AiBuilder.CellSpec cell = this.orderedCells.get(this.cellCursor);
                           autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.constraintFor(cell, block);
                           if (this.pop(pos)) {
                              return;
                           }

                           BlockState occupied = mc.level.getBlockState(pos);
                           if (schematic
                              && !this.write_Z
                              && occupied.canBeReplaced()
                              && occupied.getFluidState().is(FluidTags.WATER)
                              && Block.isShapeFullBlock(AiBuilder.toBlockState(cell).getCollisionShape(mc.level, pos))
                              && Block.isShapeFullBlock(mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()))
                              && mc.level
                                 .noCollision(
                                    mc.player,
                                    new AABB(pos.getX() + 0.2, pos.getY() + 1, pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 2.8, pos.getZ() + 0.8)
                                 )) {
                              this.write_Z = true;
                              this.activeSubtask = AiGoto.travelToCompat(
                                 pos,
                                 0.45,
                                 false,
                                 false,
                                 false,
                                 10,
                                 autismclient.ai.engine.AiPathfinder.RouteStrategy.FASTEST,
                                 true,
                                 2.0,
                                 AiGoto.protectingCellsBelowYCompat(
                                    autismclient.ai.engine.AiPathfinder.PathGoal.nearCompat(pos, 0.45), this.cachedProtectedCells(), pos.getY()
                                 )
                              );
                              return;
                           }

                           if (schematic
                              && occupied.getBlock() == block
                              && !occupied.canBeReplaced()
                              && occupied.getFluidState().isEmpty()
                              && !AiBuilder.isDoubleSlabCell(this.orderedCells.get(this.cellCursor))) {
                              this.activeSubtask = AiGoto.approachBlockCompat(pos, 30, this.cachedProtectedCells());
                              return;
                           }

                           if (schematic && occupied.getBlock() != block && !occupied.canBeReplaced() && occupied.getFluidState().isEmpty()) {
                              if (occupied.hasBlockEntity()) {
                                 this.failCurrentCell("target occupied by a block entity");
                                 return;
                              }

                              this.activeSubtask = autismclient.ai.engine.AiBreakBlock.breakBlock(pos, 30, true, this.cachedProtectedCells(), true);
                              return;
                           }

                           if (this.close(cell, pos)) {
                              return;
                           }

                           if (schematic) {
                              Set<Long> batchKeep = AiSchematicApproach.Inner_DataUtil.batchKeepCells(
                                 this.orderedCells, this.cellCursor, this.cachedProtectedCells(), next -> blueprint.origin().offset(next.dx(), next.dy(), next.dz()).asLong()
                              );
                              if (this.lastApproachJob != null && this.lastApproachJob.load(pos)) {
                                 this.activeApproachJob = this.lastApproachJob;
                                 this.continuedStopCount++;
                              } else if (this.read(cell)) {
                                 this.lastApproachJob = null;
                                 this.batchPositions = List.of();
                                 this.activeApproachJob = this.prefetchedApproachJob;
                                 this.prefetchedApproachJob = null;
                                 this.batchPrefetchedCount++;
                              } else {
                                 this.lastApproachJob = null;
                                 this.batchPositions = List.of();
                                 if (this.prefetchedApproachJob != null) {
                                    this.prefetchedApproachJob.cancel();
                                    this.prefetchedApproachJob = null;
                                 }

                                 List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets = this.batchRequests(cell, pos, block, constraint);
                                 this.activeApproachJob = new AiSchematicApproach.Inner_DataUtil(
                                    targets, this.buildBounds, batchKeep, this.batchAvoidCells(cell, pos), this.init(targets)
                                 );
                                 this.activeApproachJob.init_V_Set(this.close_Set);
                                 this.batchPlannedCount++;
                              }

                              this.activeSubtask = this.batchApproachOp = this.activeApproachJob
                                 .operation(30, batchKeep, this.batchAvoidCells(cell, pos));
                           } else {
                              this.activeSubtask = this.deferPassCount > 0
                                 ? AiGoto.approachToPlaceWithDigCompat(pos, block.asItem(), 30, this.buildBounds)
                                 : AiGoto.approachToPlaceCompat(pos, block.asItem(), 30, this.buildBounds);
                           }

                           return;
                        }
                     }

                     this.failCurrentCell("no placement material available");
                  }
               }
            }

            Set<Long> batchAvoidCells(AiBuilder.CellSpec cell, BlockPos pos) {
               HashSet<Long> avoid = new HashSet<>(this.sync_Map.getOrDefault(pos.asLong(), Set.of()));
               avoid.addAll(this.rejectedBatchCells);
               if (!AiBuilder.toBlockState(cell).getCollisionShape(mc.level, pos).isEmpty()) {
                  avoid.add(pos.asLong());
                  avoid.add(pos.below().asLong());
               }

               return avoid;
            }

            List<autismclient.ai.engine.PlacementGoal.PlacementRequest> batchRequests(
               AiBuilder.CellSpec cell, BlockPos pos, Block block, autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint
            ) {
               ArrayList<autismclient.ai.engine.PlacementGoal.PlacementRequest> batch = new ArrayList<>();
               batch.add(new autismclient.ai.engine.PlacementGoal.PlacementRequest(pos, block.asItem(), constraint, AiBuilder.toBlockState(cell)));

               for (int j = this.cellCursor + 1; j < this.orderedCells.size() && j < this.cellCursor + 192; j++) {
                  AiBuilder.CellSpec next = this.orderedCells.get(j);
                  if (next.dy() == cell.dy() && !AiSchematicActions.needsAction(next) && !AiBuilder.isDoubleSlabCell(next)) {
                     Block nextBlock = AiBuilder.resolveBlock(next);
                     Item item = next.block().equals("minecraft:grass_block") ? Items.DIRT : nextBlock.asItem();
                     if (item != Items.AIR && this.countInInventory(item) > 0) {
                        BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                        BlockState live = mc.level.getBlockState(at);
                        if (live.canBeReplaced()
                           && live.getFluidState().isEmpty()
                           && !AiBuilder.isCellSatisfied(next, live, power.substitutes(), schematic)
                           && AiTerrain.isLoaded(mc.level, at)
                           && AiBuilder.toBlockState(next).canSurvive(mc.level, at)
                           && AiSchematicActions.dependencyFor(next, at, mc.level, this.load_Map) == null
                           && !this.mustHoldCell(next, at)) {
                           boolean supported = false;

                           for (Direction direction : Direction.values()) {
                              if (AiBuilder.allowsSupportOnSide(next, nextBlock, direction) && AiBuilder.hasSolidSupportShape(mc.level, at.relative(direction))) {
                                 supported = true;
                              }
                           }

                           if (supported) {
                              batch.add(
                                 new autismclient.ai.engine.PlacementGoal.PlacementRequest(
                                    at, item, this.constraintFor(next, nextBlock), AiBuilder.toBlockState(next)
                                 )
                              );
                           }
                        }
                     }
                  }
               }

               return AiSchematicApproach.Inner_DataUtil.orderByRoute(batch, mc.player.position());
            }

            boolean read(AiBuilder.CellSpec currentCell) {
               if (this.prefetchedApproachJob == null) {
                  return false;
               }

               for (int i = this.cellCursor; i < this.orderedCells.size() && i < this.cellCursor + 192; i++) {
                  AiBuilder.CellSpec next = this.orderedCells.get(i);
                  if (next.dy() == currentCell.dy()) {
                     BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                     if (this.prefetchedApproachJob.covers(at) && !AiBuilder.isCellSatisfied(next, mc.level.getBlockState(at), power.substitutes(), true)) {
                        return true;
                     }
                  }
               }

               return false;
            }

            void tickPrefetch() {
               boolean working = this.activeSubtask != null || this.cellConstructionOp != null || this.pendingPlacePos != null || this.accessStairsOp != null;
               if (working && this.cellCursor < this.orderedCells.size()) {
                  if (this.prefetchedApproachJob != null && this.cellCursor - this.prefetchStartCursor >= 192) {
                     this.prefetchedApproachJob.cancel();
                     this.prefetchedApproachJob = null;
                  }

                  if (this.prefetchedApproachJob == null && this.prefetchRequests == null && this.prefetchCooldown-- <= 0) {
                     this.prefetchRequests = new ArrayList<>();
                     this.prefetchScanStart = this.prefetchScanCursor = this.cellCursor;
                     this.prefetchLayerY = -2147483648;
                     this.prefetchOmitted = this.activeApproachJob != null
                        ? this.activeApproachJob.getTargets()
                        : (this.lastApproachJob != null ? this.lastApproachJob.getTargets() : Set.copyOf(this.batchPositions));
                  }

                  if (this.prefetchRequests != null) {
                     long stop = System.nanoTime() + 750000L;
                     int end = Math.min(this.orderedCells.size(), this.prefetchScanStart + 192);

                     while (this.prefetchScanCursor < end && System.nanoTime() < stop) {
                        AiBuilder.CellSpec next = this.orderedCells.get(this.prefetchScanCursor++);
                        if (this.prefetchLayerY == -2147483648 || next.dy() == this.prefetchLayerY) {
                           autismclient.ai.engine.PlacementGoal.PlacementRequest target = this.requestForCell(next, this.prefetchOmitted);
                           if (target != null && !this.prefetchRequests.stream().anyMatch(t -> t.pos().equals(target.pos()))) {
                              this.prefetchLayerY = next.dy();
                              this.prefetchRequests.add(target);
                           }
                        }
                     }

                     if (this.prefetchScanCursor >= end) {
                        if (!this.prefetchRequests.isEmpty()) {
                           List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets = AiSchematicApproach.Inner_DataUtil.orderByRoute(this.prefetchRequests, this.prefetchStartPosition());
                           autismclient.ai.engine.PlacementGoal.PlacementRequest first = targets.getFirst();
                           AiBuilder.CellSpec wanted = this.load_Map.get(first.pos().asLong());
                           this.prefetchedApproachJob = new AiSchematicApproach.Inner_DataUtil(
                              targets,
                              this.buildBounds,
                              Collections.unmodifiableSet(this.close_Set),
                              this.batchAvoidCells(wanted, first.pos()),
                              this.init(targets)
                           );
                           this.prefetchedApproachJob.init_V_Set(this.close_Set);
                           this.prefetchStartCursor = this.cellCursor;
                           this.batchPlannedCount++;
                        }

                        this.prefetchRequests = null;
                        this.prefetchOmitted = Set.of();
                        this.prefetchCooldown = 20;
                     }
                  }

                  if (this.prefetchedApproachJob != null) {
                     if (!this.prefetchedApproachJob.isFinished() && this.lastWorkerPosition != null && mc.player.position().distanceToSqr(this.lastWorkerPosition) > 1.0E-4) {
                        this.overlapTicks++;
                     }

                     this.prefetchedApproachJob.setWorkerPosition(this.prefetchStartPosition());
                     this.prefetchedApproachJob.planSlice(750000L);
                  }

                  this.lastWorkerPosition = mc.player.position();
               }
            }

            Vec3 prefetchStartPosition() {
               return this.activeApproachJob != null
                  ? this.activeApproachJob.endPosition()
                  : (this.lastApproachJob != null ? this.lastApproachJob.endPosition() : mc.player.position());
            }

            BlockPos init(List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets) {
               Set<BlockPos> selected = targets.stream().map(autismclient.ai.engine.PlacementGoal.PlacementRequest::pos).collect(Collectors.toSet());
               BlockPos best = null;
               double distance = 1.0 / 0.0;

               for (int i = this.cellCursor; i < this.orderedCells.size() && i < this.cellCursor + 192; i++) {
                  autismclient.ai.engine.PlacementGoal.PlacementRequest next = this.requestForCell(this.orderedCells.get(i), selected);
                  if (next != null) {
                     double near = targets.stream().mapToDouble(target -> target.pos().distSqr(next.pos())).min().orElse(1.0 / 0.0);
                     if (near < distance) {
                        best = next.pos();
                        distance = near;
                     }
                  }
               }

               return best;
            }

            autismclient.ai.engine.PlacementGoal.PlacementRequest requestForCell(AiBuilder.CellSpec cell, Set<BlockPos> omit) {
               BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
               if (!omit.contains(at) && !at.equals(this.pendingPlacePos) && !AiSchematicActions.needsAction(cell) && !AiBuilder.isDoubleSlabCell(cell) && AiTerrain.isLoaded(mc.level, at)) {
                  BlockState live = mc.level.getBlockState(at);
                  Block target = AiBuilder.resolveBlock(cell);
                  Item item = cell.block().equals("minecraft:grass_block") ? Items.DIRT : target.asItem();
                  return item != Items.AIR
                        && this.countInInventory(item) != 0
                        && live.canBeReplaced()
                        && live.getFluidState().isEmpty()
                        && !AiBuilder.isCellSatisfied(cell, live, power.substitutes(), true)
                        && AiBuilder.toBlockState(cell).canSurvive(mc.level, at)
                        && AiSchematicActions.dependencyFor(cell, at, mc.level, this.load_Map) == null
                        && !this.mustHoldCell(cell, at)
                     ? new autismclient.ai.engine.PlacementGoal.PlacementRequest(
                        at, item, this.constraintFor(cell, target), AiBuilder.toBlockState(cell)
                     )
                     : null;
               } else {
                  return null;
               }
            }

            boolean close(AiBuilder.CellSpec cell, BlockPos pos) {
               if (schematic && !this.get_Z) {
                  AiOperation lean = AiSchematicActions.init(cell, pos, this.constraintFor(cell, AiBuilder.resolveBlock(cell)));
                  if (lean == null) {
                     return false;
                  }

                  this.get_Z = this.set_Z = this.run_Z = true;
                  this.activeSubtask = lean;
                  return true;
               } else {
                  return false;
               }
            }

            boolean pop(BlockPos pos) {
               if (schematic && this.ownsScaffolding && !this.put_Z && !(mc.player.getY() < pos.getY() + 2) && this.ceilingAbove(pos.above()) != null) {
                  this.init_V_2(pos);
                  BlockPos stance = null;
                  double distance = 1.7976931348623157E308;

                  for (Direction direction : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
                     BlockPos candidate = pos.relative(direction);
                     Integer bottom = this.read_Map.get(BlockPos.asLong(candidate.getX(), 0, candidate.getZ()));
                     BlockState floor = mc.level.getBlockState(candidate.below());
                     if (bottom != null
                        && bottom <= pos.getY()
                        && Block.isShapeFullBlock(floor.getCollisionShape(mc.level, candidate.below()))
                        && floor.getFluidState().isEmpty()
                        && !floor.is(Blocks.MAGMA_BLOCK)
                        && !floor.is(Blocks.CACTUS)
                        && AiSchematicActions.init(mc.level, candidate, (int)Math.ceil(mc.player.getY()) + 1)) {
                        double d = mc.player.position().distanceToSqr(Vec3.atBottomCenterOf(candidate));
                        if (d < distance) {
                           distance = d;
                           stance = candidate;
                        }
                     }
                  }

                  if (stance == null) {
                     return false;
                  }

                  BlockPos roof = this.ceilingAbove(stance);
                  if (roof == null) {
                     return false;
                  }

                  BlockPos plant = roof.above();
                  BlockState canopy = mc.level.getBlockState(plant);
                  boolean hasCanopy = !canopy.isAir() && canopy.getCollisionShape(mc.level, plant).isEmpty();
                  Set<Long> protectedCells = this.init_Set_Z_BlockPos(true, hasCanopy ? plant : null);
                  if (!hasCanopy
                     || !canopy.hasBlockEntity()
                        && canopy.getFluidState().isEmpty()
                        && !(canopy.getDestroySpeed(mc.level, plant) < 0.0F)
                        && !protectedCells.contains(plant.asLong())) {
                     for (BlockPos shaftCell : BlockPos.betweenClosed(stance, roof)) {
                        if (!mc.level.getBlockState(shaftCell).isAir() && protectedCells.contains(shaftCell.asLong())) {
                           return false;
                        }
                     }

                     HashSet<Long> digProtected = new HashSet<>(protectedCells);
                     digProtected.addAll(this.close_Set);
                     final AiOperation descent = AiSchematicActions.init_AiOperation_BlockPos_2(plant, stance, protectedCells, digProtected);
                     this.put_Z = this.run_Z = true;
                     if (hasCanopy) {
                        final AiOperation clear = autismclient.ai.engine.AiBreakBlock.breakBlock(plant, 15, false, digProtected, false);
                        this.activeSubtask = new AiOperation() {
                           boolean descentStarted;

                           @Override
                           public AiOperation.Result tick() {
                              if (!this.descentStarted) {
                                 AiOperation.Result r = clear.tick();
                                 if (!r.terminal()) {
                                    return r;
                                 }

                                 clear.cancel();
                                 this.descentStarted = true;
                                 if (r.status() != AiOperation.Status.SUCCEEDED) {
                                    return r;
                                 }
                              }

                              return descent.tick();
                           }

                           @Override
                           public boolean canInterruptNow() {
                              return (this.descentStarted ? descent : clear).canInterruptNow();
                           }

                           @Override
                           public boolean requiresReconciliationAfterEmergency() {
                              return (this.descentStarted ? descent : clear).requiresReconciliationAfterEmergency();
                           }

                           @Override
                           public void cancel() {
                              clear.cancel();
                              descent.cancel();
                           }
                        };
                     } else {
                        this.activeSubtask = descent;
                     }

                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            autismclient.ai.engine.PlacementGoal.PlaceConstraint constraintFor(AiBuilder.CellSpec cell, Block block) {
               return AiBuilder.buildPlaceConstraint(cell, block, blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()), mc.level);
            }

            int countInInventory(Item item) {
               int count = 0;
               Inventory inventory = mc.player.getInventory();

               for (int i = 0; i < inventory.getContainerSize(); i++) {
                  ItemStack stack = inventory.getItem(i);
                  if (stack.getItem() == item) {
                     count += stack.getCount();
                  }
               }

               return count;
            }

            AiOperation nextShulkerRefill(AiBuilder.CellSpec currentCell) {
               if (this.shulkerRefillOp == null && power.shulkerStock() && mc.level != null && mc.player != null) {
                  List<String> candidates = currentCell.block().equals("minecraft:grass_block")
                     ? List.of("minecraft:dirt")
                     : power.candidates(currentCell.block());
                  Item want = null;

                  for (String candidateId : candidates) {
                     Block candidate = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(candidateId)).orElse(null);
                     if (candidate != null && candidate.asItem() != Items.AIR) {
                        want = candidate.asItem();
                        break;
                     }
                  }

                  if (want == null) {
                     return null;
                  }

                  BlockPos center = mc.player.blockPosition();

                  record Inner_DataUtil(BlockPos pos, double distance) {
                  }

                  List<Inner_DataUtil> sources = new ArrayList<>();

                  for (BlockPos cursorPos : BlockPos.betweenClosed(center.offset(-24, -8, -24), center.offset(24, 8, 24))) {
                     long key = cursorPos.asLong();
                     if (!this.triedShulkerSources.contains(key) && !this.close_Set.contains(key)) {
                        BlockPos p = cursorPos.immutable();
                        if (AiTerrain.isLoaded(mc.level, p) && mc.level.getBlockState(p).getBlock() instanceof ShulkerBoxBlock) {
                           sources.add(new Inner_DataUtil(p, mc.player.position().distanceToSqr(Vec3.atCenterOf(p))));
                        }
                     }
                  }

                  sources.sort(Comparator.comparingDouble(Inner_DataUtil::distance));
                  if (sources.isEmpty()) {
                     return null;
                  }

                  Inner_DataUtil source = sources.getFirst();
                  this.triedShulkerSources.add(source.pos().asLong());
                  int need = this.upcomingNeed(want, currentCell);
                  if (need <= 0) {
                     return null;
                  }

                  JsonArray items = new JsonArray();
                  items.add(AiJson.objectOf("item", BuiltInRegistries.ITEM.getKey(want).toString(), "count", need));
                  JsonObject args = AiJson.objectOf(
                     "action",
                     "withdraw",
                     "x",
                     source.pos().getX(),
                     "y",
                     source.pos().getY(),
                     "z",
                     source.pos().getZ(),
                     "items",
                     items,
                     "navigate",
                     true,
                     "timeoutSeconds",
                     60
                  );
                  AutismClientAddon.LOG
                     .info("[AI] build: withdrawing {} x{} from shulker at {}", BuiltInRegistries.ITEM.getKey(want), need, source.pos().toShortString());
                  return AiContainer.create(args, this.cachedProtectedCells());
               } else {
                  return null;
               }
            }

            int upcomingNeed(Item item, AiBuilder.CellSpec currentCell) {
               int need = 0;

               for (int i = this.cellCursor; i < this.orderedCells.size() && need < 576; i++) {
                  AiBuilder.CellSpec cell = this.orderedCells.get(i);
                  List<String> ids = cell.block().equals("minecraft:grass_block") ? List.of("minecraft:dirt") : power.candidates(cell.block());
                  boolean uses = false;
                  Iterator var8 = ids.iterator();

                  while (true) {
                     if (var8.hasNext()) {
                        String id = (String)var8.next();
                        Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
                        if (block == null || block.asItem() != item) {
                           continue;
                        }

                        uses = true;
                     }

                     if (uses) {
                        need += AiBuilder.itemCountForCell(cell);
                     }
                     break;
                  }
               }

               return Math.max(0, need - this.countInInventory(item));
            }

            JsonObject progressData() {
               JsonArray fails = new JsonArray();

               for (JsonObject failure : this.failureEntries) {
                  fails.add(failure);
               }

               LinkedHashSet<String> nextMaterials = new LinkedHashSet<>();
               int remaining = 0;

               for (int i = this.cellCursor; i < this.orderedCells.size() && remaining < 64 && nextMaterials.size() < 20; i++) {
                  AiBuilder.CellSpec cell = this.orderedCells.get(i);
                  BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  if (!AiBuilder.isCellSatisfied(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     remaining++;

                     for (Item item : AiSchematicActions.materialItems(cell, pos)) {
                        if (item != Items.AIR) {
                           nextMaterials.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }
                     }
                  }
               }

               int repairs = 0;

               for (AiBuilder.CellSpec cell : this.deferredCells) {
                  if (nextMaterials.size() >= 24 || repairs++ >= 64) {
                     break;
                  }

                  BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  if (!AiBuilder.isCellSatisfied(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     for (Item item : AiSchematicActions.materialItems(cell, pos)) {
                        if (item != Items.AIR && nextMaterials.size() < 24) {
                           nextMaterials.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }
                     }
                  }
               }

               return AiJson.objectOf(
                  "placed",
                  this.placedCount,
                  "skipped",
                  this.skippedCount,
                  "failed",
                  fails,
                  "nextMaterials",
                  nextMaterials,
                  "shulkerRefills",
                  this.shulkerRefillCount,
                  "survivalInterruptions",
                  this.survivalInterruptions,
                  "walkingPlacements",
                  this.walkingPlacementCount,
                  "walkingClicks",
                  this.walkingClickCount,
                  "walkingRefusals",
                  this.walkingRefusalCount,
                  "walkingPending",
                  this.placeAckTracker.pendingPos() == null ? null : this.placeAckTracker.pendingPos().toShortString(),
                  "permanentAccessSteps",
                  this.permanentAccessSteps,
                  "sourceFloorWalkingTicks",
                  this.sourceFloorWalkingTicks,
                  "batchPlanning",
                  AiJson.objectOf(
                     "planned",
                     this.batchPlannedCount,
                     "prefetched",
                     this.batchPrefetchedCount,
                     "cells",
                     this.batchCellCount,
                     "planningMillis",
                     this.planningMillis,
                     "estimatedSeconds",
                     this.estimatedSeconds,
                     "estimatedSupportBlocks",
                     this.estimatedSupportBlocks,
                     "itineraryStops",
                     this.itineraryStopCount,
                     "continuedStops",
                     this.continuedStopCount,
                     "nextReady",
                     this.prefetchedApproachJob != null && this.prefetchedApproachJob.isFinished(),
                     "overlapTicks",
                     this.overlapTicks
                  ),
                  "currentCell",
                  this.cellCursor < this.orderedCells.size() ? this.orderedCells.get(this.cellCursor) : null,
                  "currentActual",
                  this.cellCursor < this.orderedCells.size() ? mc.level.getBlockState(this.currentCellPos()).toString() : null,
                  "failedTotal",
                  this.failedTotal,
                  "cells",
                  this.orderedCells.size(),
                  "cursor",
                  this.cellCursor,
                  "unappliedProperties",
                  this.unappliedProperties,
                  "stance",
                  Boolean.getBoolean("autism.navigation.debug")
                     ? AiJson.objectOf(
                        "feet",
                        autismclient.ai.engine.AiStance.standingPos(mc).toShortString(),
                        "onGround",
                        mc.player.onGround(),
                        "floor",
                        mc.level.getBlockState(autismclient.ai.engine.AiStance.standingPos(mc).below()).toString(),
                        "head",
                        mc.level.getBlockState(autismclient.ai.engine.AiStance.standingPos(mc).above()).toString()
                     )
                     : null,
                  "navigation",
                  Boolean.getBoolean("autism.navigation.debug") && this.lastSubtaskData != null ? JsonParser.parseString(this.lastSubtaskData) : null,
                  "scaffoldingRemaining",
                  this.ownsScaffolding && this.pop_Z ? this.ownedScaffoldCells().size() : 0,
                  "survivalAdjustments",
                  schematic
                     ? "Decorative grass and ferns are omitted; crops are planted without required growth ages or bone meal; grass blocks are placed as dirt and existing grass or natural spread stays valid; seagrass cells are plain water. Item-placed leaves use persistent=true and distance follows adjacent leaves/logs; buried soil becomes dirt; farmland moisture may change naturally"
                     : ""
               );
            }

            JsonObject progressWithMissing() {
               JsonObject data = this.progressData();
               data.add(
                  "missing",
                  AiBuilder.missingMaterials(
                     new AiBuilder.Blueprint(blueprint.origin(), this.orderedCells.subList(this.cellCursor, this.orderedCells.size())),
                     mc.player.getInventory(),
                     power.substitutes(),
                     schematic
                  )
               );
               return data;
            }

            JsonObject materialFailureData(String reason) {
               JsonObject data = this.progressWithMissing();
               data.addProperty("materialFailure", reason);
               data.addProperty("carried", mc.player.containerMenu.getCarried().toString());
               if (Boolean.getBoolean("autism.navigation.debug")) {
                  JsonArray inventory = new JsonArray();

                  for (int i = 0; i < 36; i++) {
                     ItemStack stack = mc.player.getInventory().getItem(i);
                     if (!stack.isEmpty()) {
                        inventory.add(
                           AiJson.objectOf("slot", i, "item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), "count", stack.getCount())
                        );
                     }
                  }

                  data.add("inventory", inventory);
               }

               return data;
            }

            AiOperation.Result running(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.progressData());
            }

            AiOperation.Result finish(AiOperation.Status status, String message) {
               return this.finishWithData(status, message, this.progressData());
            }

            AiOperation.Result finishWithData(AiOperation.Status status, String message, JsonObject data) {
               boolean pendingEffects = this.requiresReconciliationAfterEmergency();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(pendingEffects, AiGameOperations.result(status, message, data));
            }

            @Override
            public boolean canInterruptNow() {
               if (this.guardLayer != null && !this.guardLayer.canInterruptNow()) {
                  return false;
               }

               if (this.pendingPlacePos == null && this.placeAckTracker.pendingPos() == null) {
                  for (AiOperation child : new AiOperation[]{
                     this.terrainBreakOp,
                     this.cellConstructionOp,
                     this.shulkerRefillOp,
                     this.accessStairsOp,
                     this.supportStepOffOp,
                     this.enclosureEscapeOp,
                     this.drainFloorBreakOp,
                     this.outerScaffoldBreakOp,
                     this.auditRepairOp,
                     this.bodyRescueOp,
                     this.activeSubtask
                  }) {
                     if (child != null && !child.canInterruptNow()) {
                        return false;
                     }
                  }

                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public boolean requiresReconciliationAfterEmergency() {
               if (this.guardLayer != null && this.guardLayer.requiresReconciliation()) {
                  return true;
               }

               if (this.pendingPlacePos == null && this.placeAckTracker.pendingPos() == null) {
                  for (AiOperation child : new AiOperation[]{
                     this.terrainBreakOp,
                     this.cellConstructionOp,
                     this.shulkerRefillOp,
                     this.accessStairsOp,
                     this.supportStepOffOp,
                     this.enclosureEscapeOp,
                     this.drainFloorBreakOp,
                     this.outerScaffoldBreakOp,
                     this.auditRepairOp,
                     this.bodyRescueOp,
                     this.activeSubtask
                  }) {
                     if (child != null && child.requiresReconciliationAfterEmergency()) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return true;
               }
            }

            void poll() {
               this.survivalInterruptions++;
               this.cancelApproachJobs();
               this.push();
               this.run_Z = this.init_Z = this.add_Z = false;
               this.pendingFoundation = null;
               this.currentPlacementPlan = null;
               this.start_I = 0;
               this.pendingYaw = 0.0F / 0.0F;
               this.load_Z = this.sync_Z = this.put_Z = this.write_Z = this.get_Z = false;
               this.pop_Z = false;
               this.drainSupports.clear();
               this.drainSteps = 0;
               this.stepOffStances.clear();
               this.put_Set.clear();
               this.flush_Set.clear();
               this.protectedCellsCache = null;
               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
            }

            void push() {
               if (this.readyQueue.activePos() != null) {
                  this.readyQueue.complete(this.readyQueue.activePos());
               }

               this.walkingPlacementCell = null;
               this.walkingPlacementPlan = null;
               this.walkingClearCheck = null;
               this.heldSubtaskResult = null;
               this.cancelApproachJobs();
               if (this.cellConstructionOp != null) {
                  this.cellConstructionOp.cancel();
                  this.cellConstructionOp = null;
               }

               if (this.shulkerRefillOp != null) {
                  this.shulkerRefillOp.cancel();
                  this.shulkerRefillOp = null;
                  if (mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu) {
                     mc.player.closeContainer();
                     mc.gui.setScreen(null);
                  }
               }

               if (this.accessStairsOp != null) {
                  this.accessStairsOp.cancel();
                  this.accessStairsOp = null;
               }

               if (this.supportStepOffOp != null) {
                  this.supportStepOffOp.cancel();
                  this.supportStepOffOp = null;
               }

               if (this.enclosureEscapeOp != null) {
                  this.enclosureEscapeOp.cancel();
                  this.enclosureEscapeOp = null;
               }

               if (this.drainFloorBreakOp != null) {
                  this.drainFloorBreakOp.cancel();
                  this.drainFloorBreakOp = null;
               }

               if (this.outerScaffoldBreakOp != null) {
                  this.outerScaffoldBreakOp.cancel();
                  this.outerScaffoldBreakOp = null;
               }

               if (this.retryingDetached) {
                  if (this.detachedSupports != null && this.drainCandidate != null) {
                     this.detachedSupports.add(this.drainCandidate.asLong());
                  }

                  this.retryingDetached = false;
               }

               this.drainCandidate = null;
               this.activeKeep = Set.of();

               if (this.auditRepairOp != null) {
                  this.auditRepairOp.cancel();
                  this.auditRepairOp = null;
               }

               if (this.bodyRescueOp != null) {
                  this.bodyRescueOp.cancel();
                  this.bodyRescueOp = null;
               }

               if (this.terrainBreakOp != null) {
                  this.terrainBreakOp.cancel();
                  this.terrainBreakOp = null;
               }

               if (this.activeSubtask != null) {
                  this.activeSubtask.cancel();
                  this.activeSubtask = null;
               }
            }

            void cancelApproachJobs() {
               if (this.prefetchedApproachJob != null) {
                  this.prefetchedApproachJob.cancel();
               }

               if (this.activeApproachJob != null) {
                  this.activeApproachJob.cancel();
               }

               if (this.lastApproachJob != null) {
                  this.lastApproachJob.cancel();
               }

               this.prefetchedApproachJob = this.activeApproachJob = this.lastApproachJob = null;
               this.batchApproachOp = null;
               this.prefetchRequests = null;
               this.prefetchOmitted = Set.of();
               this.lastWorkerPosition = null;
               this.batchPositions = List.of();
               this.prefetchCooldown = 0;
            }

            @Override
            public void cancel() {
               AiVisualization.clearOwner(this);
               if (!this.cancelled) {
                  if (this.guardLayer != null) {
                     this.guardLayer.cancel();
                  }

                  this.push();
                  if (this.scaffoldTracker != null) {
                     this.scaffoldTracker.close();
                  }

                  if (this.ownsScaffolding || !schematic) {
                     this.releaseJobReservations();
                  }

                  this.cancelled = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.originalSelectedSlot) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.originalSelectedSlot);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Building requires a connected living player");
      }
   }

   private static final class FlowGraph {
      private final List<List<AiBuilder.FlowGraph.FlowEdge>> adjacency = new ArrayList<>();
      private final int[] level;
      private final int[] edgeCursor;

      private FlowGraph(int size) {
         this.level = new int[size];
         this.edgeCursor = new int[size];

         for (int i = 0; i < size; i++) {
            this.adjacency.add(new ArrayList<>());
         }
      }

      private int addEdge(int from, int to, int capacity) {
         int index = this.adjacency.get(from).size();
         this.adjacency.get(from).add(new AiBuilder.FlowGraph.FlowEdge(to, this.adjacency.get(to).size(), capacity));
         this.adjacency.get(to).add(new AiBuilder.FlowGraph.FlowEdge(from, index, 0));
         return index;
      }

      private int remainingCapacity(int from, int index) {
         return this.adjacency.get(from).get(index).capacity;
      }

      private void computeMaxFlow(int source, int sink) {
         while (this.buildLevelGraph(source, sink)) {
            Arrays.fill(this.edgeCursor, 0);

            while (this.pushFlow(source, sink, 2147483647) != 0) {
            }
         }
      }

      private boolean buildLevelGraph(int source, int sink) {
         Arrays.fill(this.level, -1);
         ArrayDeque<Integer> queue = new ArrayDeque<>();
         this.level[source] = 0;
         queue.addLast(source);

         while (!queue.isEmpty()) {
            int at = queue.removeFirst();

            for (AiBuilder.FlowGraph.FlowEdge edge : this.adjacency.get(at)) {
               if (edge.capacity != 0 && this.level[edge.to] < 0) {
                  this.level[edge.to] = this.level[at] + 1;
                  queue.addLast(edge.to);
               }
            }
         }

         return this.level[sink] >= 0;
      }

      private int pushFlow(int at, int sink, int amount) {
         if (at == sink) {
            return amount;
         }

         for (List<AiBuilder.FlowGraph.FlowEdge> outgoing = this.adjacency.get(at); this.edgeCursor[at] < outgoing.size(); this.edgeCursor[at]++) {
            AiBuilder.FlowGraph.FlowEdge edge = outgoing.get(this.edgeCursor[at]);
            if (edge.capacity > 0 && this.level[edge.to] == this.level[at] + 1) {
               int pushed = this.pushFlow(edge.to, sink, Math.min(amount, edge.capacity));
               if (pushed > 0) {
                  edge.capacity -= pushed;
                  this.adjacency.get(edge.to).get(edge.reverseIndex).capacity += pushed;
                  return pushed;
               }
            }
         }

         return 0;
      }

      private static final class FlowEdge {
         private final int to;
         private final int reverseIndex;
         private int capacity;

         private FlowEdge(int to, int reverse, int remaining) {
            this.to = to;
            this.reverseIndex = reverse;
            this.capacity = remaining;
         }
      }
   }

   record MatchResult(boolean matches, String difference) {
      private static final AiBuilder.MatchResult MATCH = new AiBuilder.MatchResult(true, "");
   }

   record BuildOptions(
      int rotation,
      String mirror,
      int layerStart,
      int layerHeight,
      boolean skipFailed,
      Map<String, List<String>> substitutes,
      AiBuilder.RepeatSpec repeat,
      boolean clearTerrain,
      boolean shulkerStock,
      Map<Long, String> signText,
      AiSchematicMachinePlan machine,
      boolean assumeFly
   ) {
      static final AiBuilder.BuildOptions DEFAULT = new AiBuilder.BuildOptions(0, "none", 0, -1, false, Map.of(), null, false, true);

      BuildOptions {
         signText = Map.copyOf(signText);
         Objects.requireNonNull(machine);
      }

      BuildOptions(
         int rotation,
         String mirror,
         int layerStart,
         int layerHeight,
         boolean skipFailed,
         Map<String, List<String>> substitutes,
         AiBuilder.RepeatSpec repeat,
         boolean clearTerrain,
         boolean shulkerStock,
         Map<Long, String> signText,
         AiSchematicMachinePlan machine
      ) {
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock, signText, machine, false);
      }

      BuildOptions(
         int rotation,
         String mirror,
         int layerStart,
         int layerHeight,
         boolean skipFailed,
         Map<String, List<String>> substitutes,
         AiBuilder.RepeatSpec repeat,
         boolean clearTerrain,
         boolean shulkerStock,
         Map<Long, String> signText
      ) {
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock, signText, AiSchematicMachinePlan.EMPTY);
      }

      BuildOptions(
         int rotation,
         String mirror,
         int layerStart,
         int layerHeight,
         boolean skipFailed,
         Map<String, List<String>> substitutes,
         AiBuilder.RepeatSpec repeat,
         boolean clearTerrain
      ) {
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, true);
      }

      BuildOptions(
         int rotation,
         String mirror,
         int layerStart,
         int layerHeight,
         boolean skipFailed,
         Map<String, List<String>> substitutes,
         AiBuilder.RepeatSpec repeat,
         boolean clearTerrain,
         boolean shulkerStock
      ) {
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock, Map.of());
      }

      BuildOptions(
         int rotation,
         String mirror,
         int layerStart,
         int layerHeight,
         boolean skipFailed,
         Map<String, List<String>> substitutes,
         AiBuilder.RepeatSpec repeat
      ) {
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, false, true);
      }

      AiBuilder.BuildOptions withSignText(Map<Long, String> text) {
         return new AiBuilder.BuildOptions(
            this.rotation,
            this.mirror,
            this.layerStart,
            this.layerHeight,
            this.skipFailed,
            this.substitutes,
            this.repeat,
            this.clearTerrain,
            this.shulkerStock,
            text,
            this.machine,
            this.assumeFly
         );
      }

      AiBuilder.BuildOptions withMachine(AiSchematicMachinePlan value) {
         return new AiBuilder.BuildOptions(
            this.rotation,
            this.mirror,
            this.layerStart,
            this.layerHeight,
            this.skipFailed,
            this.substitutes,
            this.repeat,
            this.clearTerrain,
            this.shulkerStock,
            this.signText,
            value,
            this.assumeFly
         );
      }

      AiBuilder.BuildOptions withAssumeFly(boolean value) {
         return new AiBuilder.BuildOptions(
            this.rotation,
            this.mirror,
            this.layerStart,
            this.layerHeight,
            this.skipFailed,
            this.substitutes,
            this.repeat,
            this.clearTerrain,
            this.shulkerStock,
            this.signText,
            this.machine,
            value
         );
      }

      boolean layers() {
         return this.layerHeight > 0;
      }

      List<String> candidates(String block) {
         List<String> alts = this.substitutes.get(block);
         if (alts != null && !alts.isEmpty()) {
            List<String> out = new ArrayList<>(alts.size() + 1);
            out.add(block);
            out.addAll(alts);
            return out;
         } else {
            return List.of(block);
         }
      }
   }

   record Blueprint(BlockPos origin, List<AiBuilder.CellSpec> cells) {
   }

   private static final class BuildOp implements AiOperation {
      final Minecraft minecraft = Minecraft.getInstance();
      final AiBuilder.Blueprint baseBlueprint;
      final AiBuilder.BuildOptions options;
      final boolean verify;
      final long deadlineNanos;
      int completedCopies;
      int placedTotal;
      int skippedTotal;
      int failedTotal;
      final JsonArray failedSamples = new JsonArray();
      AiOperation activeCopyOp;
      boolean cancelled;

      BuildOp(AiBuilder.Blueprint base, AiBuilder.BuildOptions power, boolean verify, int timeoutSeconds) {
         this.baseBlueprint = base;
         this.options = power;
         this.verify = verify;
         this.deadlineNanos = System.nanoTime() + Math.max(1, Math.min(3600, timeoutSeconds)) * 1000000000L;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.cancelled && this.minecraft.player != null && this.minecraft.level != null) {
            if (this.minecraft.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
               return this.finishResult(
                  AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("building", this.minecraft.player.getHealth(), this.minecraft.player.getMaxHealth())
               );
            }

            if (System.nanoTime() > this.deadlineNanos) {
               return this.finishResult(AiOperation.Status.TIMED_OUT, "Build deadline reached");
            }

            if (this.activeCopyOp != null) {
               AiOperation.Result r = this.activeCopyOp.tick();
               if (!r.terminal()) {
                  return this.runningResult("Building copy " + (this.completedCopies + 1) + " of " + this.formatCopyTotal());
               }

               this.activeCopyOp.cancel();
               this.activeCopyOp = null;
               JsonObject legData = r.dataJson().isBlank() ? new JsonObject() : AiJson.parseObject(r.dataJson());
               this.placedTotal = this.placedTotal + AiJson.getInt(legData, "placed", 0);
               this.skippedTotal = this.skippedTotal + AiJson.getInt(legData, "skipped", 0);
               this.failedTotal = this.failedTotal + AiJson.getInt(legData, "failedTotal", 0);
               if (legData.has("failed") && this.failedSamples.size() < 32) {
                  for (JsonElement failure : legData.getAsJsonArray("failed")) {
                     if (this.failedSamples.size() >= 32) {
                        break;
                     }

                     this.failedSamples.add(failure);
                  }
               }

               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.finishResult(r.status(), r.message());
               }

               this.completedCopies++;
            }

            AiBuilder.RepeatSpec repeat = this.options.repeat();
            if (repeat.count() >= 0 && this.completedCopies > repeat.count()) {
               return this.finishResult(AiOperation.Status.SUCCEEDED, "Built " + this.completedCopies + " copies");
            }

            AiBuilder.Blueprint shifted = new AiBuilder.Blueprint(
               this.baseBlueprint.origin().offset(repeat.dx() * this.completedCopies, repeat.dy() * this.completedCopies, repeat.dz() * this.completedCopies),
               this.baseBlueprint.cells()
            );
            JsonObject missing = AiBuilder.missingMaterials(
               shifted, this.minecraft.player.getInventory(), this.options.substitutes()
            );
            if (missing.size() > 0) {
               JsonObject data = this.buildStatsJson();
               data.add("missing", missing);
               return this.completedCopies > 0 && repeat.count() < 0
                  ? this.finishResult(AiOperation.Status.SUCCEEDED, "Materials exhausted after " + this.completedCopies + " copies", data)
                  : this.finishResult(AiOperation.Status.FAILED, "Missing materials for copy " + (this.completedCopies + 1) + ": " + missing, data);
            }

            int remaining = (int)Math.max(1L, (this.deadlineNanos - System.nanoTime()) / 1000000000L);
            if (!AutismLiteVariant.enabled()
               && (this.options.machine().hasActions() || this.options.assumeFly())
               && !System.getProperty("autism.ai.build.schematic.engine", "printer").equalsIgnoreCase("old")) {
               this.activeCopyOp = new AiPrintOp(shifted, remaining, this.options);
            } else {
               this.activeCopyOp = AiBuilder.buildBlueprint(shifted, this.verify, remaining, this.options);
            }

            return this.runningResult("Building copy " + (this.completedCopies + 1) + " of " + this.formatCopyTotal());
         } else {
            return this.finishResult(AiOperation.Status.CANCELLED, "Build cancelled");
         }
      }

      String formatCopyTotal() {
         return this.options.repeat().count() < 0 ? "many" : String.valueOf(this.options.repeat().count() + 1);
      }

      JsonObject buildStatsJson() {
         AiBuilder.RepeatSpec repeat = this.options.repeat();
         return AiJson.objectOf(
            "copies",
            this.completedCopies,
            "placed",
            this.placedTotal,
            "skipped",
            this.skippedTotal,
            "failed",
            this.failedSamples,
            "failedTotal",
            this.failedTotal,
            "cells",
            this.baseBlueprint.cells().size(),
            "repeat",
            AiJson.objectOf("dx", repeat.dx(), "dy", repeat.dy(), "dz", repeat.dz(), "count", repeat.count())
         );
      }

      AiOperation.Result runningResult(String message) {
         return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.buildStatsJson());
      }

      AiOperation.Result finishResult(AiOperation.Status status, String message) {
         return this.finishResult(status, message, this.buildStatsJson());
      }

      AiOperation.Result finishResult(AiOperation.Status status, String message, JsonObject data) {
         this.cancel();
         return AiGameOperations.result(status, message, data);
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            if (this.activeCopyOp != null) {
               this.activeCopyOp.cancel();
               this.activeCopyOp = null;
            }
         }
      }
   }

   record RepeatSpec(int dx, int dy, int dz, int count) {
   }

   static final class PlaceAckTracker {
      private BlockPos pendingPos;
      private long sentRevision;
      private int sentSequence;
      private int sentTick;

      BlockPos pendingPos() {
         return this.pendingPos;
      }

      int sentSequence() {
         return this.sentSequence;
      }

      void track(BlockPos pos, long revision, int sequence, int sentTick) {
         if (this.pendingPos != null) {
            throw new IllegalStateException("A moving placement is already awaiting server evidence");
         }

         this.pendingPos = pos;
         this.sentRevision = revision;
         this.sentSequence = sequence;
         this.sentTick = sentTick;
      }

      AiBuilder.PlaceAckTracker.AckState poll(long currentRevision, boolean satisfied, boolean acknowledged, int nowTick) {
         if (this.pendingPos == null) {
            return AiBuilder.PlaceAckTracker.AckState.IDLE;
         }

         if (satisfied) {
            if (currentRevision <= this.sentRevision) {
               return AiBuilder.PlaceAckTracker.AckState.WAITING;
            }

            this.pendingPos = null;
            return AiBuilder.PlaceAckTracker.AckState.CONFIRMED;
         } else {
            if (nowTick - this.sentTick < 4 || currentRevision <= this.sentRevision && !acknowledged) {
               return AiBuilder.PlaceAckTracker.AckState.WAITING;
            }

            this.pendingPos = null;
            return AiBuilder.PlaceAckTracker.AckState.RETRY;
         }
      }

      enum AckState {
         IDLE,
         WAITING,
         CONFIRMED,
         RETRY;
      }
   }

   record CellSpec(int dx, int dy, int dz, String block, Map<String, String> properties) {
      CellSpec(int dx, int dy, int dz, String block) {
         this(dx, dy, dz, block, Map.of());
      }
   }

   static final class ReadyQueue {
      private final ArrayDeque<BlockPos> queue = new ArrayDeque<>();
      private final Set<Long> queuedKeys = new HashSet<>();
      private final Set<Long> eligibleKeys = new HashSet<>();
      private Object owner;
      private BlockPos activePos;
      private BlockPos pendingPos;

      void sync(Object owner, List<BlockPos> ready, Set<Long> excluded, BlockPos pending) {
         if (this.owner != owner) {
            this.clear();
         }

         this.owner = owner;
         this.pendingPos = pending;
         Set<Long> nextEligible = new HashSet<>();
         List<BlockPos> unique = new ArrayList<>(ready.size());

         for (BlockPos pos : ready) {
            long key = pos.asLong();
            if (!excluded.contains(key) && nextEligible.add(key)) {
               unique.add(pos);
            }
         }

         if (pending != null && !excluded.contains(pending.asLong()) && nextEligible.add(pending.asLong())) {
            unique.add(pending);
         }

         this.eligibleKeys.clear();
         this.eligibleKeys.addAll(nextEligible);
         if (this.activePos != null && (!this.eligibleKeys.contains(this.activePos.asLong()) || this.activePos.equals(pending))) {
            this.activePos = null;
         }

         this.queue.removeIf(posx -> {
            long keyx = posx.asLong();
            boolean keep = this.eligibleKeys.contains(keyx) && !matchesKey(pending, keyx);
            if (!keep) {
               this.queuedKeys.remove(keyx);
            }

            return !keep;
         });

         for (BlockPos pos : unique) {
            long key = pos.asLong();
            if (!pos.equals(this.activePos) && !matchesKey(pending, key) && this.queuedKeys.add(key)) {
               this.queue.addLast(pos);
            }
         }
      }

      private static boolean matchesKey(BlockPos pos, long key) {
         return pos != null && pos.asLong() == key;
      }

      BlockPos nextPos() {
         if (this.pendingPos != null) {
            return null;
         }

         if (this.activePos == null) {
            this.activePos = this.queue.pollFirst();
            if (this.activePos != null) {
               this.queuedKeys.remove(this.activePos.asLong());
            }
         }

         return this.activePos;
      }

      BlockPos activePos() {
         return this.activePos;
      }

      void requeue(BlockPos pos) {
         if (pos.equals(this.activePos)) {
            this.activePos = null;
         }

         if (pos.equals(this.pendingPos)) {
            this.pendingPos = null;
         }

         long key = pos.asLong();
         if (this.eligibleKeys.contains(key) && this.queuedKeys.add(key)) {
            this.queue.addLast(pos);
         }
      }

      void complete(BlockPos pos) {
         if (pos.equals(this.activePos)) {
            this.activePos = null;
         }
      }

      int queuedSize() {
         return this.queue.size();
      }

      void clear() {
         this.owner = null;
         this.activePos = this.pendingPos = null;
         this.queue.clear();
         this.queuedKeys.clear();
         this.eligibleKeys.clear();
      }
   }

   enum OrientationKind {
      FACE_AXIS,
      CLICK_HEIGHT,
      CLICK_POSITION,
      FACE_DERIVED,
      PLAYER_FACING,
      PLAYER_FACING_OPPOSITE,
      PLAYER_FACING_CLOCKWISE,
      PLAYER_DIRECTION,
      PLAYER_DIRECTION_OPPOSITE,
      POST_INTERACT,
      FLUID_INTERACT,
      POWER_STATE,
      PLAYER_ROTATION,
      PLAYER_ORIENTATION,
      CHEST_PAIR,
      UNSUPPORTED;
   }
}
