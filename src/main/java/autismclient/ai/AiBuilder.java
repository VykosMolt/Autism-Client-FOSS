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
   static final int init_I = 4096;
   static final int load = 2097152;
   static final int read = 256;
   private static final int write = 3;
   private static final int update = 4;
   private static final int sync = 64;
   private static final int flush = 4;
   private static final int close = 100;
   private static final int open = 5;
   private static final int poll = 2;
   private static final int push = 32;
   private static final int pop = 6;
   private static final int tick = 6;
   private static final int run = 12;
   private static final int get = 2;
   private static final int set = 96;
   private static final int put = 30;
   private static final Set<String> init_Set = Set.of(
      "minecraft:short_grass", "minecraft:tall_grass", "minecraft:short_dry_grass", "minecraft:tall_dry_grass", "minecraft:fern", "minecraft:large_fern"
   );

   static Path init_Path(AiBuilder.Blueprint blueprint) {
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

   static int init_I_List(List<BlockPos> cells, Vec3 worker, Predicate<BlockPos> reachable) {
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

   static List<BlockPos> init(
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

   static boolean init_Z_Map(Map<Long, Integer> attempts, BlockPos pos, int maxAttempts) {
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

   static void init_V_Map(Map<Long, Integer> attempts, BlockPos pos, int count) {
      attempts.computeIfPresent(pos.asLong(), (key, previous) -> Math.max(previous, count));
   }

   static int init(
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

   static boolean init_Z_Item(Item expected, ItemStack held, boolean serverMatches) {
      return serverMatches && held.is(expected);
   }

   static boolean init(BlockPos expectedSupport, Direction expectedFace, BlockPos actualSupport, Direction actualFace) {
      return expectedSupport.equals(actualSupport) && expectedFace == actualFace;
   }

   static AiBuilder.BuildOptions init_TableIndex_ConfigS(JsonObject args) {
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

      Map<String, List<String>> substitutes = init_Map(args);
      boolean clearTerrain = AiJson.getBoolean(args, "clearTerrain", false);
      boolean shulkerStock = AiJson.getBoolean(args, "shulkerStock", true);
      return new AiBuilder.BuildOptions(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock)
         .withAssumeFly(AiJson.getBoolean(args, "assumeFly", false));
   }

   static Map<String, List<String>> init_Map(JsonObject args) {
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
            init_V(key, "substitutes key");
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
                        init_V(altId, "substitutes." + key);
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

   private static void init_V(String id, String context) {
      Identifier parsed = Identifier.tryParse(id);
      Block block = parsed == null ? null : BuiltInRegistries.BLOCK.getOptional(parsed).orElse(null);
      if (block == null) {
         throw new IllegalArgumentException(context + " names an unknown block id: " + id);
      }
   }

   static List<AiBuilder.CellSpec> init_List_List_2(List<AiBuilder.CellSpec> cells, int rotation, String mirror) {
      if (rotation == 0 && mirror.equals("none")) {
         return cells;
      }

      List<AiBuilder.CellSpec> out = new ArrayList<>(cells.size());

      for (AiBuilder.CellSpec cell : cells) {
         out.add(init_TableIndex_StreamU_TableIndex_StreamU(cell, rotation, mirror));
      }

      return out;
   }

   static AiBuilder.CellSpec init_TableIndex_StreamU_TableIndex_StreamU(AiBuilder.CellSpec cell, int rotation, String mirror) {
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

      BlockState var10000 = init_BlockState(cell);
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
               key = init_Str(key, mirror);
            }

            for (int i = 0; i < turns; i++) {
               key = init(key);
            }
         }

         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)state.getBlock().getStateDefinition().getProperty(key);
         props.put(key, property == null ? entry.getValue() : String.valueOf(state.getValue(property)).toLowerCase(Locale.ROOT));
      }

      return new AiBuilder.CellSpec(dx, cell.dy(), dz, cell.block(), Map.copyOf(props));
   }

   static String init(String facing) {
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

   static String init_Str(String facing, String axis) {
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

   static List<AiBuilder.CellSpec> init(List<AiBuilder.CellSpec> cells, int maxCells) {
      return init_List_List(cells, maxCells, 256);
   }

   static List<AiBuilder.CellSpec> init_List_List(List<AiBuilder.CellSpec> cells, int maxCells, int maxOffset) {
      return cells.isEmpty() ? List.of() : init(cells, maxCells, maxOffset, init_BlockPos(cells));
   }

   static BlockPos init_BlockPos(List<AiBuilder.CellSpec> cells) {
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

   static List<AiBuilder.CellSpec> init(List<AiBuilder.CellSpec> cells, int maxCells, int maxOffset, BlockPos minimum) {
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

         unique.putIfAbsent(init_J_I(dx, dy, dz), cell);
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

   static AiBuilder.Blueprint init_TableIndex_DataUti_TableIndex_DataUti(AiBuilder.Blueprint blueprint, int start, int height) {
      List<AiBuilder.CellSpec> kept = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         if (cell.dy() >= start && cell.dy() < start + height) {
            kept.add(cell);
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(kept));
   }

   static AiBuilder.Blueprint init(JsonObject args, BlockPos fallbackOrigin) {
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
            int dx = init_I_JsonObject(entry, "dx", context);
            int dy = init_I_JsonObject(entry, "dy", context);
            int dz = init_I_JsonObject(entry, "dz", context);
            if (Math.abs(dx) <= 256 && Math.abs(dy) <= 256 && Math.abs(dz) <= 256) {
               if (entry.has("block") && entry.get("block").isJsonPrimitive() && entry.getAsJsonPrimitive("block").isString()) {
                  String block = entry.get("block").getAsString();
                  Identifier id = Identifier.tryParse(block);
                  Block blockObj = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
                  if (blockObj == null) {
                     throw new IllegalArgumentException(context + " names an unknown block id: " + block);
                  }

                  Map<String, String> properties = init_Map_JsonObject(entry, context, blockObj);
                  if (!seen.add(init_J_I(dx, dy, dz))) {
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
      return new BlockPos(init_I_JsonObject(o, "x", "origin"), init_I_JsonObject(o, "y", "origin"), init_I_JsonObject(o, "z", "origin"));
   }

   static int init_I_JsonObject(JsonObject object, String key, String context) {
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

   static Map<String, String> init_Map_JsonObject(JsonObject entry, String context, Block block) {
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
            throw new IllegalArgumentException(context + ".properties." + name + " is not a property of " + blockId + "; valid properties: " + init(definition));
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

   private static String init(StateDefinition<Block, BlockState> definition) {
      List<String> names = new ArrayList<>();

      for (Property<?> property : definition.getProperties()) {
         names.add(property.getName());
      }

      return names.isEmpty() ? "(none)" : String.join(", ", names);
   }

   static boolean init_Z(Block block, String property) {
      return block instanceof SnowLayerBlock && property.equals("layers")
         || block instanceof CandleBlock && property.equals("candles")
         || block instanceof SeaPickleBlock && property.equals("pickles")
         || block instanceof TurtleEggBlock && property.equals("eggs")
         || block instanceof SegmentableBlock && (property.equals("flower_amount") || property.equals("segment_amount"));
   }

   static Item init_Item(Block block, String property) {
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

   static boolean init(Block block) {
      return block instanceof ShulkerBoxBlock || block instanceof RodBlock || block instanceof AmethystClusterBlock;
   }

   static Float init(Block block, int rotation) {
      float yaw = rotation * 22.5F;
      if (!(block instanceof StandingSignBlock) && !(block instanceof BannerBlock) && !(block instanceof CeilingHangingSignBlock)) {
         return block instanceof SkullBlock ? Mth.wrapDegrees(yaw) : null;
      } else {
         return Mth.wrapDegrees(yaw - 180.0F);
      }
   }

   static AiBuilder.OrientationKind init_TableIndex_TaskQue(Block block, String property) {
      if (block instanceof MultifaceBlock && Direction.byName(property) != null) {
         return AiBuilder.OrientationKind.POST_INTERACT;
      }

      if (property.equals("rotation") && init(block, 0) != null) {
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

      if (property.equals("facing") && init(block)) {
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

               if (init_Item(block, property) != null || init_Z(block, property)) {
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

                     if (write(block) || block instanceof FaceAttachedHorizontalDirectionalBlock) {
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
                     return load(block) ? AiBuilder.OrientationKind.POWER_STATE : AiBuilder.OrientationKind.POST_INTERACT;
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

   static boolean load(Block block) {
      return block == Blocks.IRON_TRAPDOOR || block instanceof DoorBlock door && !door.type().canOpenByHand();
   }

   static boolean read(Block block) {
      return block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock;
   }

   static boolean write(Block block) {
      return read(block)
         || block instanceof TripWireHookBlock
         || block instanceof BaseCoralWallFanBlock
         || block instanceof LadderBlock
         || block instanceof WallSignBlock
         || block instanceof WallBannerBlock
         || block instanceof WallSkullBlock;
   }

   static boolean init(Block block, AiBuilder.CellSpec cell) {
      if (write(block)) {
         return true;
      }

      if (!(block instanceof FaceAttachedHorizontalDirectionalBlock)) {
         return false;
      }

      String face = cell.properties().get("face");
      return face == null || face.equalsIgnoreCase("wall");
   }

   static AiBuilder.OrientationKind init_TableIndex_TaskQue(AiBuilder.CellSpec cell, Block block) {
      AiBuilder.OrientationKind strategy = init_TableIndex_TaskQue(block, "facing");
      return strategy == AiBuilder.OrientationKind.FACE_DERIVED && block instanceof FaceAttachedHorizontalDirectionalBlock && !init(block, cell)
         ? AiBuilder.OrientationKind.PLAYER_FACING
         : strategy;
   }

   static List<String> init_List(AiBuilder.CellSpec cell) {
      if (read_Z(cell)) {
         return List.of();
      }

      if (AiSchematicActions.init_Z(cell)) {
         return AiSchematicActions.init_List(cell);
      }

      Block block = init_Block(cell);
      if (block.asItem() == Items.AIR && !cell.block().equals("minecraft:grass_block")) {
         return List.of("no item form");
      }

      if (!sync(block) && !cell.block().equals("minecraft:grass_block")) {
         return List.of("its item places another block");
      }

      ArrayList<String> gaps = new ArrayList<>();

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         String key = entry.getKey();
         if (!read(block, key)) {
            AiBuilder.OrientationKind strategy = key.equals("facing") ? init_TableIndex_TaskQue(cell, block) : init_TableIndex_TaskQue(block, key);
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

   static boolean init_Z_Level_2(Level level, BlockPos pos, Direction facing) {
      return autismclient.ai.engine.AiPistonPower.init(level, pos, facing);
   }

   static boolean init_Z_Level(Level level, BlockPos pos, AiBuilder.CellSpec cell) {
      if (init_Block(cell) instanceof RedstoneWallTorchBlock) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
         return facing == null ? false : !level.hasSignal(pos.relative(facing.getOpposite()), facing.getOpposite());
      } else {
         return !level.hasSignal(pos.below(), Direction.DOWN);
      }
   }

   static Direction init_Direction(AiBuilder.CellSpec cell) {
      String type = cell.properties().get("type");
      Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      if (facing != null && facing.getAxis().isHorizontal() && type != null && !type.equals("single")) {
         return type.equals("left") ? facing.getClockWise() : facing.getCounterClockWise();
      } else {
         return null;
      }
   }

   static boolean init_Z_TableIndex_StreamU(AiBuilder.CellSpec cell, BlockPos pos, BlockGetter world) {
      Direction side = init_Direction(cell);
      if (side == null) {
         return false;
      }

      BlockState partner = world.getBlockState(pos.relative(side));
      return partner.getBlock() == init_Block(cell)
         && partner.getValue(ChestBlock.FACING).getName().equals(cell.properties().get("facing"))
         && partner.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
   }

   static autismclient.ai.engine.PlacementGoal.PlaceConstraint init(AiBuilder.CellSpec cell, Block block, BlockPos pos, BlockGetter world) {
      Direction clickFace = null;
      Direction look = null;
      Float yaw = null;
      double tolerance = 35.0;
      if (block instanceof LanternBlock) {
         clickFace = "true".equals(cell.properties().get("hanging")) ? Direction.DOWN : Direction.UP;
      } else if (init(block, cell)) {
         clickFace = Direction.byName(cell.properties().getOrDefault("facing", "north"));
      } else if (block instanceof FaceAttachedHorizontalDirectionalBlock) {
         clickFace = load_Direction(cell);
      } else if (init(block)) {
         clickFace = Direction.byName(cell.properties().getOrDefault("facing", "up"));
      } else if (block instanceof HopperBlock) {
         Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "down"));
         if (facing != null && facing.getAxis().isHorizontal()) {
            clickFace = facing.getOpposite();
         }
      }

      AiBuilder.OrientationKind strategy = init_TableIndex_TaskQue(cell, block);
      boolean pairing = block instanceof ChestBlock && init_Z_TableIndex_StreamU(cell, pos, world);
      if (pairing) {
         clickFace = init_Direction(cell).getOpposite();
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
      if (rotation != null && init_TableIndex_TaskQue(block, "rotation") == AiBuilder.OrientationKind.PLAYER_ROTATION) {
         yaw = init(block, Integer.parseInt(rotation));
         tolerance = 8.0;
         look = Direction.fromYRot(yaw.floatValue());
      }

      String orientation = cell.properties().get("orientation");
      if (orientation != null && init_TableIndex_TaskQue(block, "orientation") == AiBuilder.OrientationKind.PLAYER_ORIENTATION) {
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

      if (update(block)) {
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
         init_Boolean(cell, block),
         fullDirection,
         block instanceof TrapDoorBlock ? Direction.byName(cell.properties().getOrDefault("facing", "north")) : null,
         autismclient.ai.engine.AiShapes.init_S_List(init_BlockState(cell).getCollisionShape(world, pos).toAabbs()),
         yaw,
         tolerance
      );
   }

   static boolean update(Block block) {
      return block.asItem() instanceof StandingAndWallBlockItem item && item.getBlock() == block;
   }

   static boolean init(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return !state.isAir() && !state.canBeReplaced() && !state.getShape(world, pos).isEmpty();
   }

   static boolean sync(Block block) {
      if (block.asItem() instanceof BlockItem blockItem) {
         HashMap var3 = new HashMap();
         blockItem.registerBlocks(var3, blockItem);
         return var3.containsKey(block);
      } else {
         return false;
      }
   }

   static Direction load_Direction(AiBuilder.CellSpec cell) {
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

   static boolean init_Z(AiBuilder.CellSpec cell) {
      return "double".equalsIgnoreCase(cell.properties().get("type"))
         && BuiltInRegistries.BLOCK.getOptional(Identifier.parse(cell.block())).map(b -> b instanceof SlabBlock).orElse(false);
   }

   static Boolean init_Boolean(AiBuilder.CellSpec cell, Block target) {
      if (init_Z(cell)) {
         return false;
      }

      String type = cell.properties().get("type");
      if (type != null && !init_Z(cell) && init_TableIndex_TaskQue(target, "type") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
         return type.equalsIgnoreCase("top");
      }

      String half = cell.properties().get("half");
      return half != null && init_TableIndex_TaskQue(target, "half") == AiBuilder.OrientationKind.CLICK_HEIGHT ? half.equalsIgnoreCase("top") : null;
   }

   static boolean init(Boolean requestedHigh, Direction face) {
      if (requestedHigh != null && face != null) {
         return face.getAxis().isHorizontal() ? true : requestedHigh == (face == Direction.DOWN);
      } else {
         return true;
      }
   }

   static int init_I(AiBuilder.CellSpec cell) {
      if (!"upper".equals(cell.properties().get("half")) && !"head".equals(cell.properties().get("part"))) {
         Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(cell.block())).orElse(null);
         if (block != null) {
            for (Entry<String, String> entry : cell.properties().entrySet()) {
               if (init_Z(block, entry.getKey())) {
                  return Math.max(1, Integer.parseInt(entry.getValue()));
               }
            }
         }

         return init_Z(cell) ? 2 : 1;
      } else {
         return 0;
      }
   }

   static boolean init(Block block, String property, String actual, String requested) {
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

   static boolean load(Block block, String property) {
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

   static boolean read(Block block, String property) {
      if (load(block, property)) {
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
         return block instanceof BeehiveBlock ? property.equals("honey_level") : write(block, property);
      }
   }

   static boolean write(Block block, String property) {
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

   static boolean init_Z(AiBuilder.CellSpec cell, BlockState live) {
      return live.getBlock() instanceof ChestBlock
         && live.getValue(ChestBlock.TYPE) == ChestType.SINGLE
         && cell.properties().containsKey("type")
         && !"single".equals(cell.properties().get("type"));
   }

   static boolean load(AiBuilder.CellSpec cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      boolean differs = false;

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
         if (property != null && !init(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
            if (!read(live.getBlock(), entry.getKey())) {
               return false;
            }

            differs = true;
         }
      }

      return differs;
   }

   static boolean read(AiBuilder.CellSpec cell, BlockState live) {
      if (close(cell, live)) {
         return true;
      }

      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         if (!read(live.getBlock(), entry.getKey()) && (!entry.getKey().equals("type") || !init_Z(cell, live))) {
            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null || !init(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean init_Z_TableIndex_StreamU_4(AiBuilder.CellSpec wanted, BlockState live, boolean ownedScaffold) {
      return !live.isAir() && (!ownedScaffold || wanted == null || read(wanted, live));
   }

   static boolean init(boolean recovery, boolean breach, Integer columnBottom, int y, BlockState live) {
      return recovery && !live.hasBlockEntity() && live.getFluidState().isEmpty() && (breach || columnBottom != null && y >= columnBottom);
   }

   static boolean write(AiBuilder.CellSpec cell, BlockState live) {
      if (!BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString().equals(cell.block())) {
         return false;
      }

      if (live.getBlock() instanceof MultifaceBlock && !autismclient.ai.print.PrintRules.load_Z_BlockState_BlockState(init_BlockState(cell), live)) {
         return false;
      }

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         if (!read(live.getBlock(), entry.getKey())) {
            Property<?> property = live.getBlock().getStateDefinition().getProperty(entry.getKey());
            if (property == null) {
               return false;
            }

            if (!init(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
               return false;
            }
         }
      }

      return true;
   }

   static boolean init_Z_TableIndex_StreamU_3(AiBuilder.CellSpec cell, BlockState live, Map<String, List<String>> substitutes) {
      if (write(cell, live)) {
         return true;
      }

      List<String> alts = substitutes.get(cell.block());
      if (alts != null && !alts.isEmpty()) {
         if (!alts.contains(BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString())) {
            return false;
         }

         for (Entry<String, String> entry : cell.properties().entrySet()) {
            if (!read(live.getBlock(), entry.getKey())) {
               Property<?> property = live.getBlock().getStateDefinition().getProperty(entry.getKey());
               if (property != null && !init(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static boolean close(AiBuilder.CellSpec cell, BlockState live) {
      return (cell.block().equals("minecraft:dirt") && cell.properties().isEmpty() || cell.block().equals("minecraft:grass_block"))
         && (live.is(Blocks.DIRT) || live.is(Blocks.GRASS_BLOCK));
   }

   static boolean update(AiBuilder.CellSpec cell, BlockState live) {
      if (close(cell, live)) {
         return true;
      }

      Fluid fluid = init_Fluid(cell);
      if (load_Z(cell)) {
         return live.getFluidState().getType().isSame(fluid) && !live.getFluidState().isSource();
      }

      if (fluid != null
         && "0".equals(cell.properties().getOrDefault("level", "0"))
         && live.getFluidState().getType().isSame(fluid)
         && live.getFluidState().isSource()) {
         return true;
      }

      if (init_Block(cell) == live.getBlock() && flush(live.getBlock())) {
         for (Entry<String, String> entry : cell.properties().entrySet()) {
            if (!entry.getKey().equals("age")) {
               Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
               if (property == null || !init(live.getBlock(), entry.getKey(), String.valueOf(live.getValue(property)), entry.getValue())) {
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
            || write(cell, live);
      }
   }

   static boolean sync(AiBuilder.CellSpec cell, BlockState live) {
      return init_TableIndex_CachePo(cell, live).matches();
   }

   static AiBuilder.MatchResult init_TableIndex_CachePo(AiBuilder.CellSpec cell, BlockState live) {
      BlockState expected = init_BlockState(cell);
      if (cell.block().equals("minecraft:dirt") && cell.properties().isEmpty() && live.is(Blocks.GRASS_BLOCK)) {
         return AiBuilder.MatchResult.MATCH;
      }

      Fluid fluid = init_Fluid(cell);
      if (load_Z(cell)) {
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
            if (!read(expected.getBlock(), name)
               && (!(expected.getBlock() instanceof FarmlandBlock) || !name.equals("moisture"))
               && (!flush(expected.getBlock()) || !name.equals("age"))) {
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

   static boolean load_Z(AiBuilder.CellSpec cell) {
      return init_Fluid(cell) != null && !"0".equals(cell.properties().getOrDefault("level", "0"));
   }

   static Fluid init_Fluid(AiBuilder.CellSpec cell) {
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

   static boolean read_Z(AiBuilder.CellSpec cell) {
      return cell.block().equals("minecraft:piston_head") || cell.block().equals("minecraft:moving_piston");
   }

   static boolean flush(AiBuilder.CellSpec cell, BlockState live) {
      if (init_TableIndex_TaskQue(live.getBlock(), "waterlogged") == AiBuilder.OrientationKind.FLUID_INTERACT
         && cell.properties().containsKey("waterlogged")
         && !String.valueOf(live.getValue(BlockStateProperties.WATERLOGGED)).equals(cell.properties().get("waterlogged"))) {
         LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
         properties.remove("waterlogged");
         return read(new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties), live);
      } else {
         return false;
      }
   }

   private static boolean init(AiBuilder.CellSpec cell, BlockState live, Map<String, List<String>> substitutes, boolean schematic) {
      return schematic && update(cell, live) || init_Z_TableIndex_StreamU_3(cell, live, substitutes);
   }

   static long init_J_I(int dx, int dy, int dz) {
      return (long)(dx + 256) << 42 | (long)(dy + 256) << 21 | dz + 256;
   }

   static List<AiBuilder.CellSpec> init_List(AiBuilder.Blueprint blueprint) {
      List<AiBuilder.CellSpec> cells = blueprint.cells();
      Set<Long> present = new HashSet<>();

      for (AiBuilder.CellSpec cell : cells) {
         present.add(init_J_I(cell.dx(), cell.dy(), cell.dz()));
      }

      Map<Long, Integer> depth = new HashMap<>();

      for (AiBuilder.CellSpec cell : cells) {
         init_I_TableIndex_StreamU(cell, present, depth);
      }

      List<AiBuilder.CellSpec> ordered = new ArrayList<>(cells);
      ordered.sort(
         Comparator.comparingInt(AiBuilder.CellSpec::dy)
            .thenComparingInt(cellx -> depth.get(init_J_I(cellx.dx(), cellx.dy(), cellx.dz())))
            .thenComparingLong(cellx -> (long)cellx.dx() * cellx.dx() + (long)cellx.dy() * cellx.dy() + (long)cellx.dz() * cellx.dz())
            .thenComparingInt(AiBuilder.CellSpec::dx)
            .thenComparingInt(AiBuilder.CellSpec::dz)
            .thenComparing(AiBuilder.CellSpec::block)
      );
      return ordered;
   }

   static List<AiBuilder.CellSpec> load_List(AiBuilder.Blueprint blueprint) {
      ArrayList<AiBuilder.CellSpec> ordered = new ArrayList<>(blueprint.cells());
      ordered.sort(
         Comparator.comparingInt(AiBuilder.CellSpec::dy)
            .thenComparingInt(
               cellx -> {
                  Block block = init_Block(cellx);
                  if (init_Fluid(cellx) != null) {
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
         ordered = new ArrayList<>(init_List_TableIndex_DataUti(blueprint, ordered, neighbor -> AiTerrain.isLoaded(mc.level, neighbor) && init(mc.level, neighbor)));
      }

      ordered = new ArrayList<>(init_List(ordered));
      HashMap<Long, AiBuilder.CellSpec> crops = new HashMap<>();

      for (AiBuilder.CellSpec cell : ordered) {
         if (init_Block(cell) instanceof CropBlock) {
            crops.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      ArrayList<AiBuilder.CellSpec> seeded = new ArrayList<>(ordered.size());
      HashSet<AiBuilder.CellSpec> moved = new HashSet<>();

      for (AiBuilder.CellSpec cell : ordered) {
         if (!moved.contains(cell)) {
            seeded.add(cell);
            if (cell.block().equals("minecraft:farmland")) {
               AiBuilder.CellSpec crop = crops.get(init_J_I(cell.dx(), cell.dy() + 1, cell.dz()));
               if (crop != null) {
                  seeded.add(crop);
                  moved.add(crop);
               }
            }
         }
      }

      return load(seeded);
   }

   static List<AiBuilder.CellSpec> init_List(List<AiBuilder.CellSpec> ordered) {
      int n = ordered.size();
      if (n < 2) {
         return ordered;
      }

      HashMap<Long, Integer> index = new HashMap<>(n * 2);
      int bottom = 2147483647;

      for (int i = 0; i < n; i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         index.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), i);
         bottom = Math.min(bottom, cell.dy());
      }

      Direction[] looks = new Direction[n];
      HashMap<AiBuilder.CellSpec, Direction> looked = new HashMap<>();

      for (int i = 0; i < n; i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         looks[i] = looked.computeIfAbsent(new AiBuilder.CellSpec(0, 0, 0, cell.block(), cell.properties()), AiBuilder::read_Direction);
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
               Integer blocker = index.get(init_J_I(part.dx() - look.getStepX(), part.dy(), part.dz() - look.getStepZ()));
               if (blocker != null
                  && !moved[blocker]
                  && !(key[blocker] > key[i])
                  && !init_BlockState(ordered.get(blocker)).getCollisionShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty()) {
                  boolean supported = part.dy() == bottom;

                  for (Direction side : Direction.values()) {
                     if (!supported && side != Direction.UP && side != look.getOpposite()) {
                        Integer other = index.get(init_J_I(part.dx() + side.getStepX(), part.dy() + side.getStepY(), part.dz() + side.getStepZ()));
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

   static Direction read_Direction(AiBuilder.CellSpec cell) {
      Block block = init_Block(cell);
      if (block != Blocks.AIR && init_Fluid(cell) == null) {
         try {
            autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = init(cell, block, BlockPos.ZERO, EmptyBlockGetter.INSTANCE);
            Direction look = constraint.playerFacing();
            return look != null && look.getAxis().isHorizontal() ? look : null;
         } catch (RuntimeException e) {
            return null;
         }
      } else {
         return null;
      }
   }

   static List<AiBuilder.CellSpec> load(List<AiBuilder.CellSpec> ordered) {
      HashMap<Long, Integer> index = new HashMap<>();

      for (int i = 0; i < ordered.size(); i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         index.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), i);
      }

      HashMap<Integer, List<AiBuilder.CellSpec>> after = new HashMap<>();
      HashSet<Integer> water = new HashSet<>();
      int[][] sides = new int[][]{{0, -1, 0}, {1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}};

      for (int i = 0; i < ordered.size(); i++) {
         AiBuilder.CellSpec cell = ordered.get(i);
         if (init_Fluid(cell) != null) {
            int last = -1;

            for (int[] side : sides) {
               Integer at = index.get(init_J_I(cell.dx() + side[0], cell.dy() + side[1], cell.dz() + side[2]));
               if (at != null && init_Fluid(ordered.get(at)) == null) {
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

      out.sort(Comparator.comparingInt(cell -> load_Z(cell) ? 1 : 0));
      return out;
   }

   static List<AiBuilder.CellSpec> init_List_TableIndex_DataUti(
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
         Block block = init_Block(cell);
         positions[i] = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
         index.put(positions[i].asLong(), i);
         BlockState placed = init_BlockState(cell);
         support[i] = !placed.canBeReplaced() && !placed.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO).isEmpty();
         if (block instanceof LanternBlock) {
            required[i] = "true".equals(cell.properties().get("hanging")) ? Direction.UP : Direction.DOWN;
         } else if (write(block)) {
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
            if ((required[i] == null || required[i] == direction) && init_Z_TableIndex_StreamU_2(cell, block, direction)) {
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
                  && init_Z_TableIndex_StreamU_2(ordered.get(next), init_Block(ordered.get(next)), direction.getOpposite())) {
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

   static boolean init_Z_TableIndex_StreamU_2(AiBuilder.CellSpec cell, Block block, Direction supportDirection) {
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

      if (init(block, cell)) {
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
         Boolean high = init_Boolean(cell, block);
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

   private static int init_I_TableIndex_StreamU(AiBuilder.CellSpec cell, Set<Long> present, Map<Long, Integer> memo) {
      List<Long> chain = new ArrayList<>();
      int x = cell.dx();
      int y = cell.dy();
      int z = cell.dz();

      int depth;
      while (true) {
         long key = init_J_I(x, y, z);
         Integer known = memo.get(key);
         if (known != null) {
            depth = known;
            break;
         }

         chain.add(key);
         if (!present.contains(init_J_I(x, y - 1, z))) {
            depth = 0;
            break;
         }

         y--;
      }

      for (int i = chain.size() - 1; i >= 0; i--) {
         memo.put(chain.get(i), depth++);
      }

      return memo.get(init_J_I(cell.dx(), cell.dy(), cell.dz()));
   }

   static JsonObject init(AiBuilder.Blueprint blueprint, Inventory inventory) {
      return init_JsonObject_TableIndex_DataUti(blueprint, inventory, Map.of());
   }

   static JsonObject init_JsonObject_TableIndex_DataUti(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes) {
      return init(blueprint, inventory, substitutes, false);
   }

   static JsonObject init(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes, boolean schematic) {
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
            required.merge(itemId, init_I(cell), Integer::sum);
            List<Item> candidates = pooled.computeIfAbsent(itemId, k -> new ArrayList<>(List.of(item)));

            for (String candidateId : substitutes.getOrDefault(cell.block(), List.of())) {
               Item alt = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(candidateId)).map(Block::asItem).orElse(Items.AIR);
               if (alt != Items.AIR && !candidates.contains(alt)) {
                  candidates.add(alt);
               }
            }
         }
      }

      ArrayList<AiCellMaterials.Inner_CachePool> plans = new ArrayList<>(required.size());
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (Entry<String, Integer> entry : required.entrySet()) {
         if (entry.getValue() != 0) {
            List<Item> candidates = pooled.get(entry.getKey());
            plans.add(
               new AiCellMaterials.Inner_CachePool(
                  AiCellMaterials.Inner_DataUtil.PLACE, entry.getKey(), candidates.getFirst(), entry.getValue(), AiCellMaterials.Inner_BufferHelper.CONSUME
               )
            );
            Set<String> specs = alternatives.computeIfAbsent(entry.getKey(), k -> new TreeSet<>());

            for (Item candidate : candidates) {
               specs.add(BuiltInRegistries.ITEM.getKey(candidate).toString());
            }
         }
      }

      return init_JsonObject_Iterable(
         plans, alternatives, spec -> available.getOrDefault(BuiltInRegistries.ITEM.getOptional(Identifier.parse(spec)).orElse(Items.AIR), 0)
      );
   }

   static AiOperation init_AiOperation(AiBuilder.Blueprint blueprint) {
      int reportLimit = 16;
      AiSchematicReadiness.Inner_TaskQueue audit = AiSchematicReadiness.init(blueprint, reportLimit);
      return init(blueprint, audit);
   }

   static AiOperation init(AiBuilder.Blueprint blueprint, AiSchematicReadiness.Inner_TaskQueue audit) {
      if (audit.ready()) {
         return null;
      }

      List<AiSchematicReadiness.Inner_StreamUtil> issues = audit.issues();
      JsonArray reported = new JsonArray();

      for (AiSchematicReadiness.Inner_StreamUtil issue : issues) {
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

      AiSchematicReadiness.Inner_StreamUtil first = issues.getFirst();
      AiBuilder.CellSpec cell = first.cell();
      boolean contextOnly = audit.issues().stream().allMatch(issue -> issue.kind() == AiSchematicReadiness.Inner_CachePool.MISSING_CONTEXT);
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

   static JsonObject load(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes) {
      ArrayList<AiCellMaterials.Inner_CachePool> plans = new ArrayList<>();
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         List<AiCellMaterials.Inner_CachePool> cellPlans = AiSchematicActions.load_List(cell);
         plans.addAll(cellPlans);
         init(cell, cellPlans, substitutes, alternatives);
      }

      return init_JsonObject_Iterable_3(plans, inventory, alternatives);
   }

   static JsonObject init(AiBuilder.Blueprint blueprint, Inventory inventory, Map<String, List<String>> substitutes, LevelReader world) {
      ArrayList<AiBuilder.CellSpec> auditCells = new ArrayList<>(blueprint.cells().size());
      Map<String, Set<String>> alternatives = new TreeMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         BlockPos target = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
         BlockState live = world.getBlockState(target);
         AiBuilder.CellSpec planned = init_Z_TableIndex_StreamU_3(cell, live, substitutes) && !update(cell, live)
            ? init_TableIndex_StreamU(cell, live)
            : cell;
         auditCells.add(planned);
         if (!substitutes.getOrDefault(cell.block(), List.of()).isEmpty()) {
            boolean waterAbove = world.getFluidState(target.above()).is(FluidTags.WATER);
            init(cell, AiCellMaterials.init(planned, live, waterAbove), substitutes, alternatives);
         }
      }

      List<AiCellMaterials.Inner_CachePool> plans = AiCellMaterials.init(auditCells, blueprint.origin(), world);
      return init_JsonObject_Iterable_3(plans, inventory, alternatives);
   }

   private static AiBuilder.CellSpec init_TableIndex_StreamU(AiBuilder.CellSpec cell, BlockState live) {
      Map<String, String> properties = new LinkedHashMap<>();
      live.getValues().forEach(value -> properties.put(value.property().getName(), value.valueName()));
      return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString(), properties);
   }

   private static void init(
      AiBuilder.CellSpec cell,
      Iterable<AiCellMaterials.Inner_CachePool> plans,
      Map<String, List<String>> substitutes,
      Map<String, Set<String>> alternatives
   ) {
      for (AiCellMaterials.Inner_CachePool requirement : plans) {
         if (requirement.action() == AiCellMaterials.Inner_DataUtil.PLACE || requirement.action() == AiCellMaterials.Inner_DataUtil.ADD) {
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

   static JsonObject init_JsonObject_Iterable_3(Iterable<AiCellMaterials.Inner_CachePool> plans, Inventory inventory, Map<String, Set<String>> alternatives) {
      return init_JsonObject_Iterable(plans, alternatives, spec -> AiSchematicPlacement.init_I_Inventory_Str(inventory, spec));
   }

   static JsonObject init_JsonObject_Iterable_2(Iterable<AiCellMaterials.Inner_CachePool> plans, Container inventory, boolean carriedStock) {
      Map<Item, Integer> counts = AiCarriedShulker.init_Map_Container_Z(inventory, carriedStock);
      return init_JsonObject_Iterable(plans, Map.of(), spec -> AiItems.resolveItem(spec).items().stream().mapToInt(item -> counts.getOrDefault(item, 0)).sum());
   }

   private static JsonObject init_JsonObject_Iterable(
      Iterable<AiCellMaterials.Inner_CachePool> plans, Map<String, ? extends Iterable<String>> alternatives, ToIntFunction<String> stockCount
   ) {
      Map<String, Integer> required = new TreeMap<>();
      Map<String, Set<String>> pools = new TreeMap<>();

      for (AiCellMaterials.Inner_CachePool requirement : AiCellMaterials.init_List(plans)) {
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
         sourceEdges[requirementIndex] = flow.init(0, requirementNode, entry.getValue());
         flow.init(requirementNode, stockNodes.get(entry.getKey()), entry.getValue());

         for (String candidate : pools.get(entry.getKey())) {
            if (!candidate.equals(entry.getKey())) {
               flow.init(requirementNode, stockNodes.get(candidate), entry.getValue());
            }
         }

         requirementIndex++;
      }

      for (Entry<String, Integer> entry : stock.entrySet()) {
         flow.init(stockNodes.get(entry.getKey()), sink, entry.getValue());
      }

      flow.init_V(0, sink);
      requirementIndex = 0;

      for (Entry<String, Integer> entry : required.entrySet()) {
         int have = entry.getValue() - flow.init_I(0, sourceEdges[requirementIndex++]);
         if (have < entry.getValue()) {
            missing.add(entry.getKey(), AiJson.objectOf("required", entry.getValue(), "available", have, "missing", entry.getValue() - have));
         }
      }

      return missing;
   }

   private static Block init_Block(AiBuilder.CellSpec cell) {
      return BuiltInRegistries.BLOCK
         .getOptional(Identifier.parse(cell.block()))
         .orElseThrow(() -> new IllegalArgumentException("Unknown block id: " + cell.block()));
   }

   static AiBuilder.CellSpec init_TableIndex_StreamU(AiBuilder.CellSpec cell) {
      if (cell.block().endsWith("_leaves") && "false".equals(cell.properties().get("persistent"))) {
         LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
         properties.put("persistent", "true");
         return new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block(), properties);
      } else {
         return cell;
      }
   }

   static BlockState init_BlockState(AiBuilder.CellSpec cell) {
      BlockState state = init_Block(cell).defaultBlockState();

      for (Entry<String, String> entry : cell.properties().entrySet()) {
         Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)state.getBlock().getStateDefinition().getProperty(entry.getKey());
         if (property != null) {
            state = init_BlockState_BlockState(state, property, entry.getValue());
         }
      }

      return state;
   }

   private static <T extends Comparable<T>> BlockState init_BlockState_BlockState(BlockState state, Property<T> property, String value) {
      return state.setValue(property, property.getValue(value).orElseThrow());
   }

   static boolean init(AiBuilder.CellSpec cell, BlockPos pos, AABB body, BlockGetter world) {
      return Shapes.joinIsNotEmpty(
         init_BlockState(cell).getCollisionShape(world, pos).move(pos.getX(), pos.getY(), pos.getZ()), Shapes.create(body), BooleanOp.AND
      );
   }

   static AiBuilder.Blueprint init_TableIndex_DataUti(AiBuilder.Blueprint blueprint) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel world = mc == null ? null : mc.level;
      return init(blueprint, pos -> world != null && AiTerrain.isLoaded(world, pos) ? world.getBlockState(pos) : Blocks.AIR.defaultBlockState());
   }

   static AiBuilder.Blueprint init(AiBuilder.Blueprint blueprint, Map<Long, BlockState> leafSnapshot) {
      return init(blueprint, pos -> leafSnapshot.getOrDefault(pos.asLong(), Blocks.AIR.defaultBlockState()));
   }

   private static AiBuilder.Blueprint init(AiBuilder.Blueprint blueprint, Function<BlockPos, BlockState> externalState) {
      HashMap<Long, AiBuilder.CellSpec> source = new HashMap<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         init();
         if (!update(cell) && !read_Z(cell)) {
            source.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      Map<AiBuilder.CellSpec, Integer> leafDistances = init_Map_List(
         blueprint.cells(),
         cellx -> LeavesBlock.getOptionalDistanceAt(init_BlockState(cellx)).orElse(7) == 0,
         cellx -> init(source, blueprint.origin(), cellx, externalState)
      );
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         init();
         if (!update(cell) && !read_Z(cell)) {
            AiBuilder.CellSpec adjusted = init_TableIndex_StreamU(cell);
            if (cell.block().equals("minecraft:attached_melon_stem") || cell.block().equals("minecraft:attached_pumpkin_stem")) {
               LinkedHashMap<String, String> properties = new LinkedHashMap<>(cell.properties());
               properties.remove("facing");
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), cell.block().replace("attached_", ""), properties);
            }

            Block plant = init_Block(adjusted);
            if (flush(plant)) {
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

            AiBuilder.CellSpec above = source.get(init_J_I(cell.dx(), cell.dy() + 1, cell.dz()));
            if (cell.block().equals("minecraft:grass_block")) {
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:dirt");
            }

            if (cell.block().equals("minecraft:seagrass") || cell.block().equals("minecraft:tall_seagrass") || cell.block().equals("minecraft:bubble_column")) {
               adjusted = new AiBuilder.CellSpec(cell.dx(), cell.dy(), cell.dz(), "minecraft:water", Map.of("level", "0"));
            }

            if (cell.block().equals("minecraft:farmland") || cell.block().equals("minecraft:dirt_path")) {
               BlockState aboveState = above == null ? Blocks.AIR.defaultBlockState() : init_BlockState(above);
               boolean water = false;
               if (cell.block().equals("minecraft:farmland")) {
                  for (int dx = -4; dx <= 4 && !water; dx++) {
                     for (int dz = -4; dz <= 4 && !water; dz++) {
                        for (int dy = 0; dy <= 1 && !water; dy++) {
                           AiBuilder.CellSpec neighbour = source.get(init_J_I(cell.dx() + dx, cell.dy() + dy, cell.dz() + dz));
                           water = neighbour != null && init_BlockState(neighbour).getFluidState().is(FluidTags.WATER);
                        }
                     }
                  }
               }

               adjusted = init(cell, aboveState.isSolid(), aboveState.is(BlockTags.MAINTAINS_FARMLAND), aboveState.getBlock() instanceof FenceGateBlock, water);
            }

            cells.add(adjusted);
         }
      }

      return new AiBuilder.Blueprint(blueprint.origin(), List.copyOf(cells));
   }

   static List<BlockPos> read(AiBuilder.Blueprint blueprint) {
      HashSet<Long> source = new HashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         init();
         if (!update(cell) && !read_Z(cell)) {
            source.add(init_J_I(cell.dx(), cell.dy(), cell.dz()));
         }
      }

      LinkedHashSet<BlockPos> neighbors = new LinkedHashSet<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         init();
         if (init_Block(cell) instanceof LeavesBlock) {
            for (Direction direction : Direction.values()) {
               int x = cell.dx() + direction.getStepX();
               int y = cell.dy() + direction.getStepY();
               int z = cell.dz() + direction.getStepZ();
               if (!source.contains(init_J_I(x, y, z))) {
                  neighbors.add(blueprint.origin().offset(x, y, z));
               }
            }
         }
      }

      return List.copyOf(neighbors);
   }

   static void init() {
      if (Thread.currentThread().isInterrupted()) {
         throw new CancellationException("Schematic preparation cancelled");
      }
   }

   static boolean write(AiBuilder.CellSpec cell) {
      return init_Set.contains(cell.block());
   }

   private static boolean flush(Block plant) {
      return plant instanceof CropBlock
         || plant instanceof StemBlock
         || plant instanceof CocoaBlock
         || plant instanceof PitcherCropBlock
         || plant instanceof NetherWartBlock
         || plant instanceof SweetBerryBushBlock;
   }

   static boolean update(AiBuilder.CellSpec cell) {
      return write(cell) || cell.block().equals("minecraft:pitcher_crop") && "upper".equals(cell.properties().get("half"));
   }

   static Map<AiBuilder.CellSpec, Integer> init_Map_List(
      List<AiBuilder.CellSpec> cells, Predicate<AiBuilder.CellSpec> log, ToIntFunction<AiBuilder.CellSpec> surrounding
   ) {
      HashMap<Long, AiBuilder.CellSpec> source = new HashMap<>();
      HashMap<Long, AiBuilder.CellSpec> leaves = new HashMap<>();
      HashMap<AiBuilder.CellSpec, Integer> distances = new HashMap<>();
      ArrayDeque<AiBuilder.CellSpec> queue = new ArrayDeque<>();

      for (AiBuilder.CellSpec cell : cells) {
         init();
         source.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), cell);
         if (init_Block(cell) instanceof LeavesBlock) {
            leaves.put(init_J_I(cell.dx(), cell.dy(), cell.dz()), cell);
         }
      }

      for (AiBuilder.CellSpec cell : leaves.values()) {
         init();
         int distance = Math.clamp(surrounding.applyAsInt(cell), 1, 7);

         for (Direction direction : Direction.values()) {
            AiBuilder.CellSpec neighbor = source.get(
               init_J_I(cell.dx() + direction.getStepX(), cell.dy() + direction.getStepY(), cell.dz() + direction.getStepZ())
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
         init();
         AiBuilder.CellSpec cell = queue.removeFirst();
         int next = distances.get(cell) + 1;
         if (next < 7) {
            for (Direction direction : Direction.values()) {
               AiBuilder.CellSpec neighbor = leaves.get(
                  init_J_I(cell.dx() + direction.getStepX(), cell.dy() + direction.getStepY(), cell.dz() + direction.getStepZ())
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

   private static int init(
      Map<Long, AiBuilder.CellSpec> source, BlockPos origin, AiBuilder.CellSpec cell, Function<BlockPos, BlockState> externalState
   ) {
      int distance = 7;

      for (Direction direction : Direction.values()) {
         int x = cell.dx() + direction.getStepX();
         int y = cell.dy() + direction.getStepY();
         int z = cell.dz() + direction.getStepZ();
         if (!source.containsKey(init_J_I(x, y, z))) {
            BlockPos pos = origin.offset(x, y, z);
            BlockState state = externalState.apply(pos);
            distance = Math.min(distance, LeavesBlock.getOptionalDistanceAt(state).orElse(7) + 1);
         }
      }

      return distance;
   }

   static AiBuilder.CellSpec init(
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

   static AiOperation init_AiOperation(JsonObject args) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null && mc.level != null && mc.player.isAlive()) {
         AiBuilder.BuildOptions power = init_TableIndex_ConfigS(args);
         if (!AutismLiteVariant.enabled() && args.has("schematic") && !args.get("schematic").isJsonNull()) {
            return new AiPrintPreparation(args, power);
         }

         AiSchematic.Inner_BufferHelper decodedSchematic = null;
         AiBuilder.Blueprint blueprint;
         if (args.has("schematic") && !args.get("schematic").isJsonNull()) {
            if (args.has("blocks") || args.has("blueprint")) {
               throw new IllegalArgumentException("schematic is mutually exclusive with blocks/blueprint; pass one source");
            }

            BlockPos origin = auditOrigin(args, mc.player.blockPosition());
            AiSchematic.Inner_BufferHelper loaded = AiSchematic.init_bP_BufferHelper(
               AiSchematic.init_Path_Path_Str(AiSchematic.init(), AiJson.getString(args, "schematic", "")),
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
            blueprint = init(args, mc.player.blockPosition());
            if (power.rotation() != 0 || !power.mirror().equals("none")) {
               blueprint = new AiBuilder.Blueprint(blueprint.origin(), init_List_List_2(blueprint.cells(), power.rotation(), power.mirror()));
            }
         }

         if (power.layers()) {
            blueprint = init_TableIndex_DataUti_TableIndex_DataUti(blueprint, power.layerStart(), power.layerHeight());
         }

         if (decodedSchematic != null) {
            power = power.withMachine(AiSchematicMachinePlan.init_bV_bP_BufferHelper_TableIndex_DataUti(decodedSchematic, blueprint));
         }

         boolean schematic = args.has("schematic");
         int decodedCells = blueprint.cells().size();
         if (schematic) {
            blueprint = init_TableIndex_DataUti(blueprint);
            power = power.withMachine(power.machine().init(blueprint));
            AiOperation readinessFailure = init_AiOperation(blueprint);
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

         AiBuilder.Blueprint stockPlan = schematic ? load_TableIndex_DataUti(blueprint) : blueprint;
         boolean creativeSupply = schematic && mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.CREATIVE && mc.player.getAbilities().instabuild;
         JsonObject missing = creativeSupply
            ? new JsonObject()
            : (
               schematic
                  ? init(stockPlan, mc.player.getInventory(), power.substitutes(), mc.level)
                  : init_JsonObject_TableIndex_DataUti(stockPlan, mc.player.getInventory(), power.substitutes())
            );
         if (schematic && missing.size() > 0 && (!power.shulkerStock() || !init_Z(blueprint))) {
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
               ? init(args, missing)
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
            : init(blueprint, verify, timeoutSeconds, power, schematic);
      } else {
         throw new IllegalArgumentException("Building requires a connected living player");
      }
   }

   private static boolean init_Z(AiBuilder.Blueprint blueprint) {
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

   static AiBuilder.Blueprint init_TableIndex_DataUti_TableIndex_DataUti_2(
      AiBuilder.Blueprint blueprint, Map<String, List<String>> substitutes, Function<BlockPos, BlockState> state
   ) {
      ArrayList<AiBuilder.CellSpec> remaining = new ArrayList<>();

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         BlockState live = state.apply(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()));
         if (!init(cell, live, substitutes, true) && !flush(cell, live)) {
            if (init_Z(cell) && live.getBlock() == init_Block(cell)) {
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

   static AiBuilder.Blueprint load_TableIndex_DataUti(AiBuilder.Blueprint blueprint) {
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>(blueprint.cells().size());

      for (AiBuilder.CellSpec cell : blueprint.cells()) {
         init();
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

   private static AiOperation init(final JsonObject args, final JsonObject missing) {
      final List<String> items = new ArrayList<>(missing.keySet());
      return new AiOperation() {
         final JsonArray init_JsonArray = new JsonArray();
         AiOperation init_AiOperation;
         int init_I;
         boolean init_Z;

         @Override
         public AiOperation.Result tick() {
            if (this.init_AiOperation != null) {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (this.init_Z) {
                  return r.terminal() ? this.init(r) : r;
               }

               if (!r.terminal()) {
                  return AiGameOperations.result(
                     AiOperation.Status.RUNNING, "Crafting missing " + items.get(this.init_I) + ": " + r.message(), new JsonObject()
                  );
               }

               this.init_AiOperation = null;
               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init(items.get(this.init_I), r.message());
               }

               this.init_JsonArray.add(items.get(this.init_I));
               this.init_I++;
            }

            if (this.init_I < items.size()) {
               String item = items.get(this.init_I);
               int shortBy = missing.getAsJsonObject(item).get("missing").getAsInt();

               try {
                  this.init_AiOperation = AiCraft.craft(AiJson.objectOf("item", item, "count", shortBy, "additional", true));
               } catch (IllegalArgumentException e) {
                  return this.init(item, e.getMessage());
               }

               return AiGameOperations.result(AiOperation.Status.RUNNING, "Crafting missing " + item, new JsonObject());
            } else {
               JsonObject again = args.deepCopy();
               again.addProperty("craftMissing", false);

               try {
                  this.init_AiOperation = AiBuilder.init_AiOperation(again);
               } catch (IllegalArgumentException e) {
                  return AiOperation.Result.failed(AiFailure.EXECUTION_FAILED, e.getMessage(), "{}");
               }

               this.init_Z = true;
               return AiGameOperations.result(AiOperation.Status.RUNNING, "Materials crafted; building", new JsonObject());
            }
         }

         AiOperation.Result init(String item, String reason) {
            JsonObject data = AiJson.objectOf(
               "missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray(), "crafted", this.init_JsonArray
            );
            return AiOperation.Result.failed(AiFailure.MISSING_MATERIALS, "Missing materials: " + missing + " (could not craft " + item + ": " + reason + ")", data.toString());
         }

         AiOperation.Result init(AiOperation.Result r) {
            if (this.init_JsonArray.isEmpty()) {
               return r;
            }

            try {
               JsonObject data = JsonParser.parseString(r.dataJson()).getAsJsonObject();
               data.add("craftedFirst", this.init_JsonArray);
               return new AiOperation.Result(r.status(), r.message(), data.toString(), r.evidence(), r.failure());
            } catch (RuntimeException e) {
               return r;
            }
         }

         @Override
         public void cancel() {
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
            }
         }
      };
   }

   static AiOperation init_AiOperation_TableIndex_DataUti(AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds) {
      return init(blueprint, verify, timeoutSeconds, AiBuilder.BuildOptions.DEFAULT);
   }

   static AiOperation init(AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power) {
      return init(blueprint, verify, timeoutSeconds, power, false);
   }

   private static AiOperation init(
      AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power, boolean schematic
   ) {
      return init(blueprint, verify, timeoutSeconds, power, schematic, Set.of());
   }

   static List<AiBuilder.CellSpec> read(List<AiBuilder.CellSpec> order) {
      ArrayList<AiBuilder.CellSpec> plain = new ArrayList<>();
      ArrayList<AiBuilder.CellSpec> mechanisms = new ArrayList<>();

      for (AiBuilder.CellSpec cell : order) {
         (load(cell.block()) ? mechanisms : plain).add(cell);
      }

      plain.addAll(mechanisms);
      return plain;
   }

   static boolean load(String block) {
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

   static AiOperation init(AiBuilder.Blueprint blueprint, Set<Long> protectedCells) {
      return init(blueprint, true, 60, AiBuilder.BuildOptions.DEFAULT, true, protectedCells, false);
   }

   static AiOperation init(AiBuilder.Blueprint blueprint, int timeoutSeconds) {
      return init(blueprint, true, timeoutSeconds, AiBuilder.BuildOptions.DEFAULT, true);
   }

   static AiOperation load(AiBuilder.Blueprint blueprint, Set<Long> protectedCells) {
      return init(blueprint, true, 60, AiBuilder.BuildOptions.DEFAULT, true, protectedCells, false, true);
   }

   private static AiOperation init(
      AiBuilder.Blueprint blueprint, boolean verify, int timeoutSeconds, AiBuilder.BuildOptions power, boolean schematic, Set<Long> protectedCells
   ) {
      return init(blueprint, verify, timeoutSeconds, power, schematic, protectedCells, schematic);
   }

   private static AiOperation init(
      AiBuilder.Blueprint blueprint,
      boolean verify,
      int timeoutSeconds,
      AiBuilder.BuildOptions power,
      boolean schematic,
      Set<Long> protectedCells,
      boolean scaffoldingOwner
   ) {
      return init(blueprint, verify, timeoutSeconds, power, schematic, protectedCells, scaffoldingOwner, false);
   }

   private static AiOperation init(
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
               AiOperation construction = init(
                  load_TableIndex_DataUti(blueprint), verify, timeoutSeconds, power, schematic, protectedCells, scaffoldingOwner, holdSneak
               );
               return new AiDeferredControls(
                  construction,
                  controls,
                  cellx -> AiSchematicActions.init(cellx, blueprint.origin().offset(cellx.dx(), cellx.dy(), cellx.dz()), keep),
                  deadline,
                  System::nanoTime,
                  owner -> {
                     autismclient.ai.engine.AiRoutePolicy.putAt(closedDoors);
                     AiVisualization.init(owner, blueprint.origin(), blueprint.cells(), blueprint.cells().size(), false, true);
                     AiVisualization.init(owner, "Building schematic", "Restoring captured switch positions", -1, -1);
                  },
                  owner -> {
                     autismclient.ai.engine.AiRoutePolicy.putAt(Set.of());
                     AiVisualization.init_V(owner);
                  }
               );
            }
         }

         return new AiOperation() {
            final List<AiBuilder.CellSpec> init_List = schematic ? AiBuilder.read(AiBuilder.load_List(blueprint)) : AiBuilder.init_List(blueprint);
            final AABB init_AABB = new AABB(
               blueprint.origin().getX() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dx).min().orElse(0),
               blueprint.origin().getY() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dy).min().orElse(0),
               blueprint.origin().getZ() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dz).min().orElse(0),
               blueprint.origin().getX() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dx).max().orElse(0) + 1,
               blueprint.origin().getY() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dy).max().orElse(0) + 1,
               blueprint.origin().getZ() + this.init_List.stream().mapToInt(AiBuilder.CellSpec::dz).max().orElse(0) + 1
            );
            final int init_I = power.skipFailed() ? 1 : 3;
            final long init_J = System.nanoTime() + Math.max(1L, Math.min(schematic ? 2592000L : 3600L, timeoutSeconds)) * 1000000000L;
            final int load_I = mc.player.getInventory().getSelectedSlot();
            final List<JsonObject> load_List = new ArrayList<>();
            final autismclient.ai.engine.AiAim init_DataUtil = new autismclient.ai.engine.AiAim();
            AiOperation init_AiOperation;
            AiOperation load_AiOperation;
            AiSchematicApproach.Inner_DataUtil init_b_DataUtil;
            AiSchematicApproach.Inner_DataUtil load_b_DataUtil;
            AiSchematicApproach.Inner_DataUtil read_b_DataUtil;
            int read_I = -1;
            int write_I;
            int update_I;
            int sync_I;
            int flush_I;
            List<autismclient.ai.engine.PlacementGoal.PlacementRequest> read_List;
            Set<BlockPos> init_Set = Set.of();
            List<BlockPos> write_List = List.of();
            final Set<Long> load_Set = new HashSet<>();
            AiBuilder.CellSpec init_TableIndex_StreamU;
            AiBuilder.CellSpec load_TableIndex_StreamU;
            final AiBuilder.ReadyQueue init_TableIndex_SyncMan = new AiBuilder.ReadyQueue();
            final AiBuilder.PlaceAckTracker init_TableIndex_StateCa = new AiBuilder.PlaceAckTracker();
            final Map<Long, Integer> init_Map = new HashMap<>();
            final Set<Long> read_Set = new HashSet<>();
            BlockPlacementPlanner.PlacementPlan init_U_CachePool;
            Predicate<AABB> init_Predicate;
            int close_I;
            int open_I;
            int poll_I;
            final Set<Long> write_Set = new HashSet<>();
            AiOperation.Result init_AiOperation_Result;
            BlockPos init_BlockPos;
            int push_I;
            int pop_I;
            int tick_I;
            int run_I;
            int get_I;
            int set_I;
            int put_I;
            int add_I;
            int clear_I;
            Vec3 init_Vec3;
            double init_D;
            double load_D;
            boolean init_Z;
            AiOperation read_AiOperation;
            long load_J = -4611686018427387904L;
            AiOperation write_AiOperation;
            final Set<Long> update_Set = new HashSet<>();
            int reset_I;
            BlockPos load_BlockPos;
            Block init_Block;
            long read_J;
            AiBuilder.CellSpec read_TableIndex_StreamU;
            Vec3 load_Vec3;
            BlockPlacementPlanner.PlacementPlan load_U_CachePool;
            int start_I;
            final JsonArray init_JsonArray = new JsonArray();
            final Set<String> sync_Set = new HashSet<>();
            float init_F = 0.0F / 0.0F;
            double read_D = 40.0;
            boolean load_Z;
            int stop_I;
            boolean read_Z;
            int next_I;
            String init_Str;
            int prev;
            int valid;
            int size;
            int data;
            int value;
            int cache;
            int state;
            int queue;
            int buffer;
            int entry;
            int node;
            int task;
            boolean write_Z;
            int pool;
            int index;
            int count;
            final Set<Long> flush_Set = new HashSet<>();
            AiOperation update_AiOperation;
            boolean update_Z;
            final List<AiBuilder.CellSpec> update_List = new ArrayList<>();
            int flags;
            boolean sync_Z;
            final Set<Long> close_Set = new HashSet<>(this.init_List.stream().map(c -> blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong()).toList());
            final Map<Long, AiBuilder.CellSpec> load_Map;
            final Set<Long> open_Set;
            boolean flush_Z;
            final boolean close_Z;
            final AiGuardLayer init_Y;
            int level;
            final Set<Long> poll_Set;
            final Map<Long, Integer> read_Map;
            final Set<Long> push_Set;
            final Set<Long> pop_Set;
            AiOperation sync_AiOperation;
            BlockPos read_BlockPos;
            final Set<Long> tick_Set;
            int frame;
            boolean open_Z;
            List<BlockPos> sync_List;
            int block;
            int chunk;
            AiOperation flush_AiOperation;
            boolean poll_Z;
            boolean push_Z;
            final AiScaffold.Inner_StreamUtil init_a_StreamUtil;
            final List<BlockPos> flush_List;
            final List<BlockPos> close_List;
            boolean pop_Z;
            int table;
            int store;
            final Map<Long, Integer> write_Map;
            final Map<Long, Long> update_Map;
            BlockPos write_BlockPos;
            Set<Long> detachedSupports;
            Set<Long> activeKeep;
            boolean retryingDetached;
            int drain;
            long write_J;
            long update_J;
            AiOperation close_AiOperation;
            AiOperation open_AiOperation;
            final Set<Long> run_Set;
            final Set<Long> get_Set;
            final Set<Long> set_Set;
            boolean tick_Z;
            boolean run_Z;
            boolean get_Z;
            boolean set_Z;
            boolean put_Z;
            String load_Str;
            AiOperation poll_AiOperation;
            final Set<Long> put_Set;
            AiOperation push_AiOperation;
            int fill;
            final Set<Long> add_Set;
            boolean add_Z;
            final Map<Long, Set<Long>> sync_Map;
            int mark;
            int scan;
            int parse;
            final List<AiBuilder.CellSpec> open_List;
            final List<String> poll_List;
            int bind;
            boolean clear_Z;
            int f_a_I;
            final Map<String, Integer> flush_Map;
            final long sync_J;
            int f_b_I;
            int f_c_I;
            Map<Item, Integer> close_Map;
            Set<Long> clear_Set;
            Set<Long> reset_Set;
            int f_d_I;
            final Set<AiBuilder.CellSpec> start_Set;
            int f_e_I;
            BlockPos update_BlockPos;
            Set<Long> stop_Set;
            final Map<AiBuilder.CellSpec, Integer> open_Map;
            final Map<AiBuilder.CellSpec, Integer> poll_Map;
            BlockPos sync_BlockPos;
            AiOperation pop_AiOperation;
            int f_f_I;
            long flush_J;
            int f_g_I;
            boolean reset_Z;
            final Set<Long> next_Set;

            {
               this.close_Set.addAll(protectedCells);
               this.load_Map = new HashMap<>();

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  this.load_Map.put(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong(), cell);
               }

               this.open_Set = new HashSet<>();

               for (AiBuilder.CellSpec cell : blueprint.cells()) {
                  BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  Block block = AiBuilder.init_Block(cell);
                  BlockState requested = AiBuilder.init_BlockState(cell);
                  if (!Block.isShapeFullBlock(requested.getCollisionShape(mc.level, at)) || block instanceof FallingBlock) {
                     for (Direction direction : Direction.values()) {
                        if (AiBuilder.init_Z_TableIndex_StreamU_2(cell, block, direction)) {
                           BlockPos support = at.relative(direction);
                           if (!mc.level.getBlockState(support).getCollisionShape(mc.level, support).isEmpty()) {
                              this.open_Set.add(support.asLong());
                           }
                        }
                     }
                  }
               }

               this.flush_Z = schematic;
               this.close_Z = scaffoldingOwner;
               this.init_Y = this.close_Z ? new AiGuardLayer(new AiGuard.GuardPolicy(true, true, true, 16.0F, 7.0F, 14), "schematic construction") : null;
               if (this.init_Y != null) {
                  AiGuard.suppressBriefly();
               }

               this.poll_Set = new HashSet<>();
               this.read_Map = new HashMap<>();
               this.push_Set = new HashSet<>();
               this.pop_Set = new HashSet<>();
               this.tick_Set = new HashSet<>();
               this.frame = blueprint.cells().stream().mapToInt(AiBuilder.CellSpec::dy).min().orElse(0) - 1;
               this.open_Z = true;
               this.poll_Z = !this.close_Z || !power.clearTerrain();
               this.init_a_StreamUtil = this.close_Z ? AiScaffold.init(AiBuilder.init_Path(blueprint), List.of()) : null;
               if (this.init_a_StreamUtil != null) {
                  for (BlockPos step : AiSchematicActions.init(blueprint)) {
                     for (BlockPos support : List.of(step, step.below())) {
                        if (AiScaffold.init(this.init_a_StreamUtil.init(support), mc.level.getBlockState(support).getBlock())) {
                           this.poll_Set.add(support.asLong());
                        }
                     }
                  }
               }

               this.flush_List = new ArrayList<>();
               this.close_List = new ArrayList<>();
               this.write_Map = new HashMap<>();
               this.update_Map = new HashMap<>();
               this.activeKeep = Set.of();
               this.run_Set = new HashSet<>();
               this.get_Set = new HashSet<>();
               this.set_Set = new HashSet<>();
               this.put_Set = new HashSet<>();
               this.add_Set = new HashSet<>();
               this.sync_Map = new HashMap<>();
               this.open_List = new ArrayList<>();
               this.poll_List = new ArrayList<>();
               this.flush_Map = new TreeMap<>();
               this.sync_J = System.nanoTime();
               this.start_Set = new HashSet<>();
               this.f_e_I = -1;
               this.open_Map = new HashMap<>();
               this.poll_Map = new HashMap<>();
               this.flush_J = -9223372036854775808L;
               this.next_Set = new HashSet<>();
            }

            void init_V_10() {
               autismclient.ai.engine.AiRestock.reserveItemsForJob(Map.of());
               autismclient.ai.engine.AiRoutePolicy.putAt(Set.of());
               autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_J(0L);
               autismclient.ai.engine.AiPathfinder.MoveCosts.load_V_J(0L);
               if (schematic) {
                  autismclient.ai.engine.AiRoutePolicy.putAt_V();
               }

               autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_Set(Set.of());
            }

            void load_V_4() {
               if (this.close_Map == null || this.f_d_I-- <= 0) {
                  this.f_d_I = blueprint.cells().size() / 8192;
                  HashMap<Item, Integer> counts = new HashMap<>();

                  for (AiBuilder.CellSpec cell : blueprint.cells()) {
                     BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                     if (!AiBuilder.init(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                        Item item = schematic && cell.block().equals("minecraft:grass_block") ? Items.DIRT : AiBuilder.init_Block(cell).asItem();
                        int cost = AiBuilder.init_I(cell);
                        if (item != Items.AIR && cost > 0) {
                           counts.merge(item, cost, Integer::sum);
                        }
                     }
                  }

                  this.close_Map = Map.copyOf(counts);
                  HashSet<Long> closed = new HashSet<>();

                  for (AiBuilder.CellSpec cell : blueprint.cells()) {
                     if ("false".equals(cell.properties().get("open"))
                        && AiBuilder.init_Block(cell).defaultBlockState().hasProperty(BlockStateProperties.OPEN)) {
                        closed.add(blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()).asLong());
                     }
                  }

                  this.clear_Set = Set.copyOf(closed);
                  if (schematic) {
                     HashSet<Long> empty = new HashSet<>();

                     for (long key : this.load_Map.keySet()) {
                        BlockState live = mc.level.getBlockState(BlockPos.of(key));
                        if (live.canBeReplaced() && live.getFluidState().isEmpty()) {
                           empty.add(key);
                        }
                     }

                     this.reset_Set = Set.copyOf(empty);
                  }
               }

               autismclient.ai.engine.AiRestock.reserveItemsForJob(this.close_Map);
               autismclient.ai.engine.AiRoutePolicy.putAt(this.clear_Set);
               autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_J(3000000000L);
               if (schematic) {
                  autismclient.ai.engine.AiPathfinder.MoveCosts.load_V_J(3000000000L);
               }

               if (schematic) {
                  autismclient.ai.engine.AiRoutePolicy.putAt(this.close_Set::contains);
               }

               if (schematic && this.reset_Set != null) {
                  autismclient.ai.engine.AiPathfinder.MoveCosts.init_V_Set(this.reset_Set);
               }
            }

            @Override
            public AiOperation.Result tick() {
               AiFallGuard.init_V();
               if (schematic
                  && this.close_Z
                  && mc.player != null
                  && mc.level != null
                  && mc.player.onGround()
                  && mc.player.getDeltaMovement().horizontalDistanceSqr() > 4.0E-4) {
                  BlockPos floor = autismclient.ai.engine.AiStance.init(mc).below();
                  AiBuilder.CellSpec source = this.load_Map.get(floor.asLong());
                  if (source != null && AiBuilder.init(source, mc.level.getBlockState(floor), power.substitutes(), true)) {
                     this.pop_I++;
                  }
               }

               boolean wholePlan = schematic && this.close_Z;
               AiVisualization.init(this, blueprint.origin(), wholePlan ? blueprint.cells() : this.init_List, this.data, false, wholePlan);
               boolean reserves = this.close_Z || !schematic;
               if (reserves && mc.level != null && this.f_c_I-- <= 0) {
                  this.f_c_I = 20;
                  this.load_V_4();
               }

               AiOperation.Result r = !this.clear_Z && this.init_Y != null && System.nanoTime() <= this.init_J
                  ? this.init_Y
                     .tick(this, this::poll, () -> Math.max(1, (int)((this.init_J - System.nanoTime()) / 1000000000L)))
                  : null;
               if (r != null && r.terminal()) {
                  this.cancel();
                  return r;
               }

               if (r == null && this.close_Z && !this.clear_Z && mc.player != null && mc.level != null) {
                  this.open();
               }

               r = r == null ? this.init_AiOperation_Result_8() : this.init_AiOperation_Result_5(r.message());
               if (!r.terminal()) {
                  AiVisualization.init(
                     this,
                     schematic ? "Building schematic" : "Building",
                     AiTaskStatus.init(r.message(), "Preparing the build") + " / " + this.state + " blocks placed",
                     -1,
                     -1
                  );
               }

               if (reserves && r.terminal()) {
                  this.init_V_10();
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
               this.flush_Map.merge(phase, 1, Integer::sum);
               if (r.terminal() || this.state >= this.f_b_I + 8) {
                  this.f_b_I = this.state;
                  AutismClientAddon.LOG
                     .info(
                        "[AI] build {}: placed {}/{} skipped {} failed {} in {} s, reach-first {}, ticks {}",
                        r.terminal() ? r.status() : "progress",
                        this.state,
                        this.init_List.size(),
                        this.queue,
                        this.buffer,
                        (System.nanoTime() - this.sync_J) / 1000000000L,
                        this.index,
                        this.flush_Map
                     );
               }

               return r;
            }

            AiOperation.Result init_AiOperation_Result_8() {
               if (!this.clear_Z && mc.player != null && mc.level != null) {
                  this.close();
                  if (mc.gui.screen() instanceof AbstractSignEditScreen editor) {
                     this.init(editor);
                  }

                  if (this.init_a_StreamUtil != null && ++this.f_a_I % 200 == 0) {
                     this.init_a_StreamUtil.init_V();
                  }

                  if (mc.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
                     return this.init_AiOperation_Result_AiOperation_Status_Str_4(
                        AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("building", mc.player.getHealth(), mc.player.getMaxHealth())
                     );
                  }

                  if (System.nanoTime() > this.init_J) {
                     return this.init_AiOperation_Result_AiOperation_Status_Str_4(AiOperation.Status.TIMED_OUT, "Build deadline reached");
                  }

                  if (this.close_Z) {
                     if (this.pop_AiOperation != null) {
                        AiOperation.Result rescued = this.pop_AiOperation.tick();
                        if (!rescued.terminal()) {
                           return this.init_AiOperation_Result_5("Freeing the body from a block: " + rescued.message());
                        }

                        this.pop_AiOperation.cancel();
                        this.pop_AiOperation = null;
                     }

                     BlockPos stuck = this.init_BlockPos_3();
                     if (stuck != null && this.f_f_I++ < 20) {
                        AutismClientAddon.LOG
                           .info(
                              "[AI] build: the body is inside {} at {}; breaking it",
                              mc.level.getBlockState(stuck).getBlock().getName().getString(),
                              stuck.toShortString()
                           );
                        if (this.init_AiOperation != null) {
                           this.init_AiOperation.cancel();
                           this.init_AiOperation = null;
                        }

                        this.pop_AiOperation = autismclient.ai.engine.AiBreakBlock.init(stuck, 5, false, Set.of(), false);
                        return this.init_AiOperation_Result_5("Freeing the body from a block");
                     }
                  }

                  if (!this.poll_Z) {
                     AiOperation.Result cleared = this.write_AiOperation_Result_2();
                     if (cleared != null) {
                        return cleared;
                     }

                     this.poll_Z = true;
                  }

                  if (!this.open_Z) {
                     if (this.sync_AiOperation == null) {
                        this.sync_AiOperation = AiSchematicActions.init_AiOperation_TableIndex_DataUti(blueprint, this.init_Set_2(), this.poll_Set, this.frame);
                     }

                     AiOperation.Result accessResult = this.sync_AiOperation.tick();
                     if (!accessResult.terminal()) {
                        return this.init_AiOperation_Result_5("Preparing construction access: " + accessResult.message());
                     }

                     this.sync_AiOperation.cancel();
                     this.sync_AiOperation = null;
                     if (accessResult.status() != AiOperation.Status.SUCCEEDED) {
                        AutismClientAddon.LOG.info("[AI] build: construction access stairs failed ({}); building without them", accessResult.message());
                     }

                     this.open_Z = true;
                  }

                  if (this.write_AiOperation != null) {
                     AiOperation.Result refill = this.write_AiOperation.tick();
                     if (!refill.terminal()) {
                        return this.init_AiOperation_Result_5("Withdrawing building stock from a nearby shulker box: " + refill.message());
                     }

                     this.write_AiOperation.cancel();
                     this.write_AiOperation = null;
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
                           this.reset_I++;
                           return this.init_AiOperation_Result_5("Shulker restock finished; resuming the build");
                        } else {
                           AiOperation next = this.init_AiOperation(this.init_List.get(this.data));
                           if (next != null) {
                              this.write_AiOperation = next;
                              return this.init_AiOperation_Result_5("Trying the next nearby shulker box");
                           } else {
                              return this.init_AiOperation_Result_AiOperation_Status_Str_4(
                                 AiOperation.Status.FAILED,
                                 "No nearby shulker box could supply the needed stock"
                                    + (refill.status() == AiOperation.Status.SUCCEEDED ? "" : ": " + refill.message())
                              );
                           }
                        }
                     } else {
                        JsonObject receipt = this.init_JsonObject_18();
                        receipt.add("shulkerTransfer", AiJson.parseObject(refill.dataJson()));
                        return this.init_AiOperation_Result_AiOperation_Status(
                           refill.status(), "Shulker transfer did not settle; reconcile inventory before resuming: " + refill.message(), receipt
                        );
                     }
                  } else if (this.read_AiOperation != null) {
                     AiOperation.Result r = this.read_AiOperation.tick();
                     if (!r.terminal()) {
                        return this.init_AiOperation_Result_5("Constructing cell " + (this.data + 1) + "/" + this.init_List.size() + ": " + r.message());
                     }

                     this.read_AiOperation.cancel();
                     this.read_AiOperation = null;
                     if (r.status() != AiOperation.Status.UNCERTAIN && r.status() != AiOperation.Status.CANCELLED && r.status() != AiOperation.Status.TIMED_OUT
                        )
                      {
                        if (r.status() == AiOperation.Status.SUCCEEDED) {
                           AiBuilder.CellSpec finished = this.init_List.get(this.data);
                           if (AiBuilder.init_Fluid(finished) != null || finished.properties().containsKey("waterlogged")) {
                              this.load_J = mc.player.tickCount;
                           }

                           this.state++;
                           this.update();
                        } else if (schematic && this.bind < 2) {
                           this.read(r.message());
                        } else {
                           this.load(r.message());
                        }

                        return this.init_AiOperation_Result_5("Special placement finished");
                     } else {
                        return this.init_AiOperation_Result_AiOperation_Status_Str_4(
                           r.status(), "Special placement did not settle; reobserve before resuming: " + r.message()
                        );
                     }
                  } else {
                     if (this.load_BlockPos != null) {
                        if (this.init_Block instanceof SignBlock && mc.gui.screen() instanceof AbstractSignEditScreen editor) {
                           this.init(editor);
                        }

                        if (this.load_Vec3 != null) {
                           this.init_DataUtil.aimAt(this.load_Vec3);
                           this.init_DataUtil.tick();
                        }

                        BlockState liveState = mc.level.getBlockState(this.load_BlockPos);
                        boolean matched = liveState.getBlock() == this.init_Block;
                        if (!verify) {
                           if (++this.size >= 2) {
                              if (!matched) {
                                 this.init_V_2("placement did not appear");
                              } else if (this.init_Z_TableIndex_StreamU_2(this.read_TableIndex_StreamU, this.load_BlockPos, liveState)
                                 && this.load_BlockPos != null) {
                                 this.state++;
                                 this.cache = 0;
                                 this.update();
                              }
                           }
                        } else {
                           this.valid = matched ? this.valid + 1 : 0;
                           boolean answered = AiServerEvidence.blockRevision(this.load_BlockPos) > this.read_J;
                           if (this.valid >= (schematic ? 1 : 5) && answered) {
                              if (this.init_Z_TableIndex_StreamU_2(this.read_TableIndex_StreamU, this.load_BlockPos, liveState) && this.load_BlockPos != null) {
                                 this.state++;
                                 this.cache = 0;
                                 this.update();
                              }
                           } else if (schematic && answered && !matched && ++this.size > 4) {
                              this.init_V_2("the server refused the placement");
                           } else if (++this.size > this.init_I_10()) {
                              this.init_V_2("no applied server block confirmation");
                           }
                        }

                        if (this.load_BlockPos != null) {
                           return this.init_AiOperation_Result_5("Verifying placed block");
                        }

                        if (this.read_AiOperation != null) {
                           return this.init_AiOperation_Result_5("Finishing the placed block's fluid state");
                        }
                     }

                     if (this.init_AiOperation != null) {
                        if (schematic && this.close_Z && this.init_AiOperation instanceof AiSchematicApproach approach) {
                           approach.init(this::init, this::flush, () -> this.init_TableIndex_StateCa.init_BlockPos() != null);
                        }

                        if (this.load_b_DataUtil != null && this.init_AiOperation != this.load_AiOperation) {
                           this.load_b_DataUtil.init_V();
                           this.load_b_DataUtil = null;
                           this.read_b_DataUtil = null;
                           this.load_AiOperation = null;
                        }

                        AiOperation.Result r = this.init_AiOperation_Result == null ? this.init_AiOperation.tick() : this.init_AiOperation_Result;
                        if (r.terminal() && this.init_TableIndex_StateCa.init_BlockPos() != null) {
                           this.init_AiOperation_Result = r;
                           return this.init_AiOperation_Result_5("Finishing the moving placement acknowledgement");
                        }

                        this.init_AiOperation_Result = null;
                        if (Boolean.getBoolean("autism.navigation.debug")) {
                           this.load_Str = r.dataJson();
                        }

                        if (!r.terminal()) {
                           return this.init_AiOperation_Result_5("Approaching cell " + (this.data + 1) + "/" + this.init_List.size() + ": " + r.message());
                        }

                        this.init_AiOperation = null;
                        if (this.load_b_DataUtil != null) {
                           this.init_D = this.load_b_DataUtil.init_D();
                           if (r.status() != AiOperation.Status.SUCCEEDED) {
                              this.read_b_DataUtil = null;
                              BlockPos rejected = this.load_b_DataUtil.init_BlockPos();
                              if (rejected != null) {
                                 this.load_Set.add(rejected.asLong());
                              }
                           } else {
                              BlockPos feet = autismclient.ai.engine.AiStance.init(mc);
                              List<BlockPos> positions = this.load_b_DataUtil.init_List_BlockPos(feet);
                              ArrayList<AiBuilder.CellSpec> scheduled = new ArrayList<>();

                              for (BlockPos at : positions) {
                                 AiBuilder.CellSpec wanted = this.load_Map.get(at.asLong());
                                 if (wanted != null && !AiBuilder.init(wanted, mc.level.getBlockState(at), power.substitutes(), true)) {
                                    scheduled.add(wanted);
                                 }
                              }

                              AiBuilder.CellSpec previous = this.data < this.init_List.size() ? this.init_List.get(this.data) : null;
                              AiSchematicApproach.Inner_DataUtil.init_V_List(this.init_List, this.data, scheduled);
                              if (this.data < this.init_List.size() && !this.init_List.get(this.data).equals(previous)) {
                                 this.sync();
                              }

                              this.write_List = List.copyOf(positions);
                              this.read_b_DataUtil = positions.isEmpty() ? null : this.load_b_DataUtil;
                              this.get_I = scheduled.size();
                              this.set_I = this.load_b_DataUtil.init_I();
                              this.load_D = this.load_b_DataUtil.init_D_BlockPos(feet);
                              this.clear_I = this.load_b_DataUtil.init_I_BlockPos(feet);
                           }

                           this.load_b_DataUtil = null;
                           this.load_AiOperation = null;
                        }

                        if (r.status() == AiOperation.Status.SUCCEEDED && this.data < this.init_List.size() && AiBuilder.load_Z(this.init_List.get(this.data))
                           )
                         {
                           this.load_J = mc.player.tickCount;
                        }

                        boolean noRoute = r.status() != AiOperation.Status.SUCCEEDED
                           && r.message() != null
                           && r.message().startsWith("No route toward the target");
                        BlockPos worker = mc.player.blockPosition();
                        if (noRoute && this.openExit(worker)) {
                           return this.init_AiOperation_Result_5("Breaking out of the closed room");
                        }

                        if (this.read_BlockPos != null) {
                           BlockPos foundation = this.read_BlockPos;
                           this.read_BlockPos = null;
                           if (r.status() == AiOperation.Status.SUCCEEDED) {
                              AiScaffold.init_V(foundation);
                              this.tick_Set.add(foundation.asLong());
                           } else {
                              this.read("Required foundation failed: " + r.message());
                           }

                           return this.init_AiOperation_Result_5("Required foundation finished; re-evaluating the source cell");
                        } else {
                           if (this.run_Z) {
                              this.run_Z = false;
                              if (r.status() == AiOperation.Status.SUCCEEDED) {
                                 return this.init_AiOperation_Result_5("Construction stance ready; re-evaluating the placement");
                              }

                              this.set_Z = false;
                           }

                           if (this.init_Z) {
                              this.init_Z = false;
                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 this.read(r.message());
                              }

                              return this.init_AiOperation_Result_5("Scaffold construction finished; re-evaluating");
                           } else {
                              if (this.add_Z) {
                                 this.add_Z = false;
                              }

                              this.read_V_2();
                              if (r.status() != AiOperation.Status.SUCCEEDED) {
                                 if (schematic
                                    && r.message() != null
                                    && (r.message().startsWith("Batch planning") || r.message().startsWith("Construction planning"))) {
                                    this.load_V(this.load_BlockPos());
                                    return this.init_AiOperation_Result_5("Refreshing the bounded batch plan against the changed work site");
                                 }

                                 if (schematic && this.push(this.load_BlockPos())) {
                                    return this.init_AiOperation_Result_5("Building a schematic step before temporary access");
                                 }

                                 if (schematic && r.failure() != AiFailure.PLACEMENT_IMPOSSIBLE && this.load(this.init_List.get(this.data))) {
                                    return this.init_AiOperation_Result_5("Preparing access to the unreachable construction layer");
                                 }

                                 if (schematic && this.sync(this.init_List.get(this.data), this.load_BlockPos())) {
                                    return this.init_AiOperation_Result_5("Constructing a stance aligned with the requested placement");
                                 }

                                 if (schematic && r.failure() == AiFailure.PLACEMENT_IMPOSSIBLE && this.write(this.init_List.get(this.data), this.load_BlockPos())) {
                                    return this.init_AiOperation_Result_5("Building an accessible support instead of repeating the failed approach");
                                 }

                                 if (!this.sync_Z && r.message() != null && r.message().startsWith("No route toward the target")) {
                                    this.sync_Z = true;
                                    this.cache = 0;
                                    this.init_AiOperation = this.init_AiOperation();
                                    return this.init_AiOperation_Result_5("Climbing down after the approach lost its route");
                                 }

                                 if (++this.cache >= 6) {
                                    this.read("no reachable approach");
                                    return this.init_AiOperation_Result_5("Approach keeps failing; cell deferred to the post-pass");
                                 }

                                 this.load_V(this.load_BlockPos());
                              }

                              return this.init_AiOperation_Result_5("Approach finished; re-evaluating");
                           }
                        }
                     } else {
                        if (!schematic && this.init_List.size() - this.data <= 8 && this.load_Z_4()) {
                           this.add_Z = true;
                           this.init_AiOperation = AiGoto.travelToCompat(blueprint.origin().below().north(3), 1.5, true, false, false, 20);
                           return this.init_AiOperation_Result_5("Stepping out of the closing structure");
                        }

                        if (!this.flush_Z) {
                           AiOperation.Result drained = this.load_AiOperation_Result_7();
                           if (drained != null) {
                              return drained;
                           }

                           this.flush_Z = true;
                        }

                        if (schematic && this.close_Z && this.data < this.init_List.size() && !this.put_Z && mc.player.getY() < blueprint.origin().getY()) {
                           Set<Long> keep = this.load_Set();
                           BlockPos worker = mc.player.blockPosition();
                           BlockPos stance = new BlockPos(worker.getX(), blueprint.origin().getY() + 2, worker.getZ());
                           this.put_Z = this.run_Z = true;
                           this.init_AiOperation = AiGoto.travelToCompat(
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
                                 autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(stance, 0.25),
                                 keep,
                                 blueprint.origin().getY() - 2
                              )
                           );
                           return this.init_AiOperation_Result_5("Returning from below the construction foundation");
                        }

                        int evaluated = 0;
                        int placements = 0;
                        if (this.init_TableIndex_StateCa.init_BlockPos() != null) {
                           return this.init_AiOperation_Result_5("Confirming the last moving placement");
                        }

                        while (
                           this.data < this.init_List.size() && evaluated < 64 && placements < 4 && this.load_BlockPos == null && this.init_AiOperation == null
                        ) {
                           AiBuilder.CellSpec cell = this.init_List.get(this.data);
                           evaluated++;
                           BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                           BlockState live = mc.level.getBlockState(pos);
                           if (!AiTerrain.isLoaded(mc.level, pos)
                              || !AiBuilder.init(cell, live, power.substitutes(), schematic)
                                 && (
                                    !schematic
                                       || !AiBuilder.read(cell, live)
                                       || AiBuilder.init_Z(cell, live) && AiBuilder.init_Z_TableIndex_StreamU(cell, pos, mc.level)
                                 )) {
                              if (schematic
                                 && this.close_Z
                                 && this.init_AiOperation == null
                                 && this.read_AiOperation == null
                                 && !this.write_List.contains(pos)
                                 && !this.init_Z_TableIndex_StreamU_BlockPos(cell, pos)) {
                                 int swap = -1;

                                 for (int j = this.data + 1; j < Math.min(this.init_List.size(), this.data + 96); j++) {
                                    AiBuilder.CellSpec next = this.init_List.get(j);
                                    if (next.dy() == cell.dy()) {
                                       BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                                       if (!AiBuilder.init(next, mc.level.getBlockState(at), power.substitutes(), schematic)
                                          && this.init_Z_TableIndex_StreamU_BlockPos(next, at)) {
                                          swap = j;
                                          break;
                                       }
                                    }
                                 }

                                 if (swap > 0) {
                                    AiBuilder.CellSpec nearby = this.init_List.remove(swap);
                                    this.init_List.add(this.data, nearby);
                                    this.value = this.init_Map.getOrDefault(this.load_BlockPos().asLong(), 0);
                                    this.load_U_CachePool = null;
                                    this.start_I = 0;
                                    this.init_F = 0.0F / 0.0F;
                                    this.load_Z = false;
                                    this.index++;
                                    continue;
                                 }
                              }

                              if (schematic && this.close_Z && this.poll_Map.getOrDefault(cell, 0) < 4) {
                                 String hold = this.init_Str_TableIndex_StreamU_BlockPos(cell, pos);
                                 if (hold != null && this.init_Z_TableIndex_StreamU_Str(cell, hold)) {
                                    continue;
                                 }
                              }

                              if (schematic && !AiBuilder.init_BlockState(cell).getCollisionShape(mc.level, pos).isEmpty()) {
                                 BlockPos interior = AiSchematicActions.init(mc.level, pos, this.load_Map);
                                 if (interior != null) {
                                    this.sync_BlockPos = interior;
                                    if (this.poll_Map.getOrDefault(cell, 0) >= 4
                                       || !this.init_Z_TableIndex_StreamU_Str(cell, "the ceiling waits for its interior bed")) {
                                       this.read("the ceiling waits for its interior bed at " + interior.toShortString());
                                    }
                                    continue;
                                 }
                              }

                              if (schematic && this.close_Z && this.load_Str(cell, pos) != null) {
                                 this.read(this.load_Str(cell, pos));
                              } else {
                                 if (schematic
                                    && this.close_Z
                                    && this.init_u_DataUtil_TableIndex_StreamU_Block(cell, AiBuilder.init_Block(cell)).crouchForLook()
                                    && mc.level.getBlockState(pos.above()).getCollisionShape(mc.level, pos.above()).isEmpty()
                                    && mc.level.getBlockState(pos).canBeReplaced()) {
                                    AiBuilder.CellSpec above = this.load_Map.get(pos.above().asLong());
                                    if (above != null
                                       && Block.isShapeFullBlock(AiBuilder.init_BlockState(above).getCollisionShape(mc.level, pos.above()))
                                       && this.poll_Map.getOrDefault(cell, 0) < 4) {
                                       this.sync_BlockPos = pos.above();
                                       if (this.init_Z_TableIndex_StreamU_Str(cell, "an upward-facing part waits for the block above it")) {
                                          continue;
                                       }
                                    }

                                    if (this.update(cell, pos)) {
                                       return this.init_AiOperation_Result_5("Placing a temporary block above an upward-facing part");
                                    }
                                 }

                                 if (schematic) {
                                    BlockPos foundation = AiSchematicActions.init_BlockPos_TableIndex_StreamU_2(cell, pos, mc.level, this.close_Set);
                                    if (foundation != null) {
                                       this.read_BlockPos = foundation;
                                       this.init_AiOperation = AiBuilder.init(
                                          new AiBuilder.Blueprint(foundation, List.of(new AiBuilder.CellSpec(0, 0, 0, "minecraft:dirt"))),
                                          this.init_Set_2()
                                       );
                                       return this.init_AiOperation_Result_5("Restoring the source cell's permanent foundation");
                                    }

                                    BlockPos dependency = AiSchematicActions.init_BlockPos_TableIndex_StreamU(cell, pos, mc.level, this.load_Map);
                                    if (AiTerrain.isLoaded(mc.level, pos) && (dependency != null || !AiBuilder.init_BlockState(cell).canSurvive(mc.level, pos))) {
                                       if (dependency != null && this.poll_Map.getOrDefault(cell, 0) < 4) {
                                          boolean later = false;

                                          for (int j = this.data + 1; j < this.init_List.size(); j++) {
                                             AiBuilder.CellSpec next = this.init_List.get(j);
                                             if (blueprint.origin().offset(next.dx(), next.dy(), next.dz()).equals(dependency)) {
                                                later = true;
                                                break;
                                             }
                                          }

                                          this.sync_BlockPos = dependency;
                                          if (later && this.init_Z_TableIndex_StreamU_Str(cell, "waiting for its native survival support")) {
                                             continue;
                                          }
                                       }

                                       this.read("waiting for native survival support" + (dependency == null ? "" : " at " + dependency.toShortString()));
                                       continue;
                                    }
                                 }

                                 if (schematic && AiBuilder.init_Fluid(cell) != null) {
                                    if (AiBuilder.load_Z(cell)) {
                                       if (!live.canBeReplaced() && live.getFluidState().isEmpty()) {
                                          if (!live.hasBlockEntity()) {
                                             HashSet<BlockPos> plannedWater = new HashSet<>();

                                             for (Direction side : Direction.values()) {
                                                BlockPos neighbor = pos.relative(side);
                                                AiBuilder.CellSpec wanted = this.load_Map.get(neighbor.asLong());
                                                if (wanted != null
                                                   && (AiBuilder.init_Fluid(wanted) != null || "true".equals(wanted.properties().get("waterlogged")))) {
                                                   plannedWater.add(neighbor);
                                                }
                                             }

                                             this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(pos, 30, true, this.init_Set_2(), true, plannedWater);
                                             return this.init_AiOperation_Result_5("Clearing the requested flowing-water channel");
                                          }

                                          this.load("flowing water channel occupied by protected contents");
                                          continue;
                                       }

                                       if (mc.player.tickCount - this.load_J < 100L) {
                                          autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                          return this.init_AiOperation_Result_5("Waiting for water to propagate from the verified source cells");
                                       }

                                       this.read("waiting for surrounding source cells to produce flowing water");
                                       continue;
                                    }

                                    String leak = AiSchematicActions.init(pos, mc.level, this.load_Map);
                                    if (leak != null) {
                                       this.read(leak);
                                       continue;
                                    }

                                    if (!AiBuildIntent.init(pos, mc.level, this.load_Map)) {
                                       this.read("waiting for the fluid volume to be held");
                                       continue;
                                    }
                                 }

                                 if (schematic && AiSchematicActions.init_Z(cell)) {
                                    if (this.pop(pos)) {
                                       return this.init_AiOperation_Result_5("Opening access to the buried interaction cell");
                                    }

                                    this.read_AiOperation = AiSchematicActions.load(cell, pos, this.init_Set_2());
                                    return this.init_AiOperation_Result_5("Constructing a survival interaction cell");
                                 }

                                 if (schematic
                                    && this.task < 20
                                    && live.canBeReplaced()
                                    && mc.player.blockPosition().getX() == pos.getX()
                                    && mc.player.blockPosition().getZ() == pos.getZ()
                                    && mc.player.getY() >= pos.getY()
                                    && mc.player.getY() < pos.getY() + 1
                                    && AiBuilder.write(cell, AiBuilder.init_Block(cell).defaultBlockState())
                                    && Block.isShapeFullBlock(AiBuilder.init_BlockState(cell).getCollisionShape(mc.level, pos))
                                    && !mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()).isEmpty()
                                    && mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()).max(Axis.Y) >= 1.0
                                    && mc.level.noCollision(mc.player, mc.player.getBoundingBox().move(0.0, 1.42, 0.0))) {
                                    int itemSlot = AiItems.equipHotbar(AiItems.resolveItem(cell.block()));
                                    if (itemSlot >= 0) {
                                       this.task++;
                                       autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                       this.init_DataUtil.aimAt(Vec3.atCenterOf(pos.below()));
                                       this.init_DataUtil.tick();
                                       mc.options.keyJump.setDown(mc.player.onGround() || mc.player.isInWater());
                                       return this.init_AiOperation_Result_5("Jumping to backfill the access shaft with " + cell.block());
                                    }
                                 }

                                 if (this.task > 0) {
                                    mc.options.keyJump.setDown(false);
                                 }

                                 if (schematic && live.getBlock() == AiBuilder.init_Block(cell) && !live.canBeReplaced()) {
                                    if (AiBuilder.flush(cell, live)) {
                                       this.read_AiOperation = AiSchematicActions.init(cell, pos, this.init_Set_2());
                                       return this.init_AiOperation_Result_5("Restoring the placed block's waterlogging");
                                    }

                                    boolean toggleOnly = true;

                                    for (Entry<String, String> entry : cell.properties().entrySet()) {
                                       Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock()
                                          .getStateDefinition()
                                          .getProperty(entry.getKey());
                                       if (property != null
                                          && !String.valueOf(live.getValue(property)).equals(entry.getValue())
                                          && !AiBuilder.read(live.getBlock(), entry.getKey())
                                          && AiBuilder.init_TableIndex_TaskQue(live.getBlock(), entry.getKey()) != AiBuilder.OrientationKind.POST_INTERACT) {
                                          toggleOnly = false;
                                       }
                                    }

                                    if (toggleOnly) {
                                       this.read_AiOperation = AiSchematicActions.init(cell, pos, this.init_Set_2());
                                       return this.init_AiOperation_Result_5("Restoring the placed block's interaction state");
                                    }

                                    if (!AiBuilder.init_Z(cell)) {
                                       this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(pos, 30, true, this.init_Set_2(), true);
                                       return this.init_AiOperation_Result_5("Recovering a block with conflicting placement properties");
                                    }
                                 }

                                 if (schematic
                                    && AiBuilder.init_Block(cell) instanceof VegetationBlock
                                    && !AiBuilder.init_BlockState(cell).canSurvive(mc.level, pos)) {
                                    this.read("waiting for the soil under the plant");
                                 } else {
                                    if (schematic) {
                                       Block targetBlock = AiBuilder.init_Block(cell);
                                       boolean supportReady = AiBuilder.init_Z(cell) && live.getBlock() == targetBlock;

                                       for (Direction direction : Direction.values()) {
                                          if (AiBuilder.init_Z_TableIndex_StreamU_2(cell, targetBlock, direction)
                                             && AiBuilder.init(mc.level, pos.relative(direction))) {
                                             supportReady = true;
                                          }
                                       }

                                       if (!supportReady) {
                                          boolean planned = false;

                                          for (Direction direction : Direction.values()) {
                                             planned |= AiBuilder.init_Z_TableIndex_StreamU_2(cell, targetBlock, direction)
                                                && this.close_Set.contains(pos.relative(direction).asLong());
                                          }

                                          if ((!planned || this.bind > 0) && this.write(cell, pos)) {
                                             return this.init_AiOperation_Result_5("Constructing a temporary support for the floating cell");
                                          }

                                          if (!planned || this.bind != 0 || !this.init_Z_TableIndex_StreamU(cell, pos, targetBlock)) {
                                             this.read("waiting for a support face that produces the requested state");
                                          }
                                          continue;
                                       }

                                       if (live.getBlock() != targetBlock && !live.canBeReplaced() && live.getFluidState().isEmpty()) {
                                          if (live.hasBlockEntity()) {
                                             this.load("target occupied by a block entity");
                                          } else {
                                             if (++this.scan <= 3) {
                                                this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(pos, 30, true, this.init_Set_2(), true);
                                                return this.init_AiOperation_Result_5("Clearing a conflicting block at the schematic cell");
                                             }

                                             this.read("placement keeps producing " + BuiltInRegistries.BLOCK.getKey(live.getBlock()));
                                          }
                                          continue;
                                       }

                                       BlockPos requiredSupport = null;
                                       if (targetBlock instanceof LanternBlock) {
                                          requiredSupport = "true".equals(cell.properties().get("hanging")) ? pos.above() : pos.below();
                                       } else if (AiBuilder.write(targetBlock)) {
                                          Direction facing = this.init_Direction(cell.properties().getOrDefault("facing", "north"));
                                          if (facing != null) {
                                             requiredSupport = pos.relative(facing.getOpposite());
                                          }
                                       }

                                       if (requiredSupport != null
                                          && this.close_Set.contains(requiredSupport.asLong())
                                          && mc.level.getBlockState(requiredSupport).isAir()) {
                                          this.read("waiting for the schematic support cell");
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
                                             && AiBuilder.init_Z(cell)
                                             && !"double".equals(String.valueOf(live.getValue(SlabBlock.TYPE)));
                                          if (this.init(candidateItem) < (candidateUpgrade ? 1 : AiBuilder.init_I(cell))) {
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
                                          this.load("block id is not resolvable");
                                       } else {
                                          if (item != Items.AIR) {
                                             boolean upgrade = live.getBlock() == target
                                                && AiBuilder.init_Z(cell)
                                                && !"double".equals(String.valueOf(live.getValue(SlabBlock.TYPE)));
                                             if (upgrade && live.getValue(SlabBlock.TYPE) == SlabType.TOP) {
                                                this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(
                                                   pos, 20, true, schematic ? this.init_Set_2() : Set.of(), true
                                                );
                                                return this.init_AiOperation_Result_5("Recovering a top slab before the double-slab merge");
                                             }

                                             if (!sourced) {
                                                if (schematic && ++this.entry <= 20) {
                                                   autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                                   return this.init_AiOperation_Result_5("Awaiting material: " + BuiltInRegistries.ITEM.getKey(item));
                                                }

                                                this.entry = 0;
                                                if (schematic && power.shulkerStock()) {
                                                   AiOperation refill = this.init_AiOperation(cell);
                                                   if (refill != null) {
                                                      this.write_AiOperation = refill;
                                                      return this.init_AiOperation_Result_5("Withdrawing building stock from a nearby shulker box");
                                                   }
                                                }

                                                return this.init_AiOperation_Result_AiOperation_Status(
                                                   AiOperation.Status.FAILED,
                                                   "Missing material: " + BuiltInRegistries.ITEM.getKey(item),
                                                   this.init_JsonObject("missing stock")
                                                );
                                             }

                                             this.entry = 0;
                                             if (AiTerrain.isLoaded(mc.level, pos)
                                                && (
                                                   schematic
                                                      ? !AiBuilder.init(cell, pos, mc.player.getBoundingBox(), mc.level)
                                                      : !mc.player.getBoundingBox().intersects(this.init_AABB)
                                                )) {
                                                if (schematic && this.read(cell, pos)) {
                                                   if (this.openExit(mc.player.blockPosition())) {
                                                      return this.init_AiOperation_Result_5("Opening access out of the closed room");
                                                   }

                                                   this.load_V(pos);
                                                   return this.init_AiOperation_Result_5("Leaving the room " + pos.toShortString() + " closes");
                                                }

                                                if (mc.player.containerMenu != mc.player.inventoryMenu) {
                                                   if (schematic) {
                                                      mc.player.closeContainer();
                                                      mc.gui.setScreen(null);
                                                   }

                                                   this.init_V_2("a container screen is open");
                                                   return this.init_AiOperation_Result_5("Container screen blocks item selection");
                                                }

                                                String itemId = BuiltInRegistries.ITEM.getKey(item).toString();
                                                if (autismclient.util.InventorySlotHelper.ensureItemInHotbar(mc, MacroItemTarget.ofRegistryId(itemId), mc.player.getInventory().getSelectedSlot()) < 0) {
                                                   if (schematic && ++this.node <= 20) {
                                                      autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                                                      return this.init_AiOperation_Result_5("Awaiting material: selecting " + itemId);
                                                   }

                                                   return this.init_AiOperation_Result_AiOperation_Status(
                                                      AiOperation.Status.FAILED,
                                                      "Could not select material: " + itemId,
                                                      this.init_JsonObject("item selection refused")
                                                   );
                                                }

                                                this.node = 0;
                                                boolean placementSneak = schematic
                                                   && (
                                                      holdSneak
                                                         || this.set_Z
                                                         || autismclient.ai.engine.PlacementGoal.init_Z_Minecraft(
                                                            mc, pos, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, target)
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
                                                      this.load_U_CachePool = null;
                                                      return this.init_AiOperation_Result_5("Settling the placement pose before solving the click");
                                                   }
                                                }

                                                for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                   if (AiBuilder.init_TableIndex_TaskQue(target, entry.getKey()) == AiBuilder.OrientationKind.UNSUPPORTED
                                                      && (
                                                         !(target instanceof LeavesBlock)
                                                            || !entry.getKey().equals("persistent")
                                                            || !entry.getValue().equals("true")
                                                      )
                                                      && (!schematic || !AiBuilder.read(target, entry.getKey()))) {
                                                      Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)target.getStateDefinition()
                                                         .getProperty(entry.getKey());
                                                      if (property == null
                                                         || !String.valueOf(target.defaultBlockState().getValue(property)).equalsIgnoreCase(entry.getValue())) {
                                                         this.init(cell, pos, entry, "not achievable via vanilla placement");
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
                                                   BlockPlacementPlanner.PlacementPlan candidate = this.load_U_CachePool != null
                                                      ? this.load_U_CachePool
                                                      : (
                                                         schematic
                                                            ? autismclient.ai.engine.PlacementGoal.init(
                                                               mc, pos, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, target), 72
                                                            )
                                                            : BlockPlacementPlanner.plan(request)
                                                      );
                                                   if (candidate == null) {
                                                      this.init_V_2("no reachable support face");
                                                      return this.init_AiOperation_Result_5("No reachable support face; re-approaching");
                                                   }

                                                   if (!AiBuilder.init(AiBuilder.init_Boolean(cell, target), candidate.hit().getDirection())) {
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

                                                   hit = this.init(cell, pos, target, candidate.hit());
                                                   if (!BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND).lands(hit, pos)) {
                                                      this.init_V_2("shaped click does not land on the target cell");
                                                      return this.init_AiOperation_Result_5("Re-solving the placement click");
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
                                                      this.load_U_CachePool = null;
                                                      return this.init_AiOperation_Result_5("Preparing to place against the support without activating it");
                                                   }
                                                }

                                                if (!Float.isNaN(this.init_F)) {
                                                   autismclient.util.RotationMath.Rotation hitRotation = autismclient.util.RotationMath.lookAt(
                                                      hit.getLocation(), mc.player.getEyePosition()
                                                   );
                                                   float goalYaw = Math.abs(
                                                            autismclient.util.RotationMath.angularDistance(
                                                               hitRotation, new autismclient.util.RotationMath.Rotation(this.init_F, hitRotation.pitch())
                                                            )
                                                         )
                                                         <= this.read_D
                                                      ? hitRotation.yaw()
                                                      : this.init_F;
                                                   this.init_DataUtil.setTargetRotation(new autismclient.util.RotationMath.Rotation(goalYaw, hitRotation.pitch()));
                                                   this.init_DataUtil.tick();
                                                } else {
                                                   AiGameOperations.aimAndStep(this.init_DataUtil, hit.getLocation());
                                                }

                                                if (!this.init_DataUtil.isSettled()
                                                   || autismclient.ai.engine.AiAim.lookedBlockFace(hit.getBlockPos()) != hitFace
                                                   || !Float.isNaN(this.init_F)
                                                      && Math.abs(
                                                            autismclient.util.RotationMath.angularDistance(
                                                               autismclient.util.RotationMath.ofPlayer(mc.player),
                                                               new autismclient.util.RotationMath.Rotation(this.init_F, mc.player.getXRot())
                                                            )
                                                         )
                                                         > this.read_D
                                                   || !Float.isNaN(this.init_F) && !init_Z_Minecraft(mc, this.init_F, this.read_D)) {
                                                   if (++this.start_I > 30) {
                                                      this.start_I = 0;
                                                      if (!Float.isNaN(this.init_F) && !this.load_Z) {
                                                         this.load_Z = true;
                                                         Direction look = Direction.fromYRot(this.init_F);
                                                         this.load_U_CachePool = null;
                                                         if (schematic) {
                                                            this.load_V(pos);
                                                         } else {
                                                            this.init_AiOperation = AiGoto.travelToCompat(
                                                               AiGoto.findStandableNearCompat(pos.relative(look.getOpposite(), 3), 8), 0.6, true, false, false, 25
                                                            );
                                                         }

                                                         return this.init_AiOperation_Result_5(
                                                            "Re-approaching from the " + look.getOpposite().getName() + " side for facing"
                                                         );
                                                      }

                                                      if (!Float.isNaN(this.init_F)) {
                                                         for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                            AiBuilder.OrientationKind strategy = AiBuilder.init_TableIndex_TaskQue(target, entry.getKey());
                                                            if (strategy == AiBuilder.OrientationKind.PLAYER_FACING || strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE) {
                                                               this.init(cell, pos, entry, "player facing could not be aligned with the support ray");
                                                            }
                                                         }

                                                         this.init_V_2("player facing could not be aligned with the support ray");
                                                      } else {
                                                         this.load_U_CachePool = null;
                                                         this.init_V_2("could not aim at the support face");
                                                      }
                                                   }

                                                   return this.init_AiOperation_Result_5("Turning toward " + pos.toShortString());
                                                }

                                                if (schematic
                                                   ? !AiBuilder.init(cell, pos, mc.player.getBoundingBox(), mc.level)
                                                   : !mc.player.getBoundingBox().intersects(new AABB(pos))) {
                                                   this.load_U_CachePool = null;
                                                   this.start_I = 0;
                                                   if (!init_Z_Minecraft_Item(mc, item)) {
                                                      return this.init_AiOperation_Result_5("Waiting for the selected construction item to reach the server");
                                                   }

                                                   this.read_J = AiServerEvidence.blockRevision(pos);
                                                   this.load_BlockPos = pos;
                                                   this.init_Block = target;
                                                   this.read_TableIndex_StreamU = cell;
                                                   this.valid = 0;
                                                   this.size = 0;
                                                   this.load_Vec3 = hit.getLocation();
                                                   BlockState predicted = target.getStateForPlacement(
                                                      new BlockPlaceContext(mc.player, InteractionHand.MAIN_HAND, new ItemStack(item), hit)
                                                   );
                                                   boolean stateMatches = predicted != null;
                                                   if (predicted != null) {
                                                      for (Entry<String, String> entry : cell.properties().entrySet()) {
                                                         AiBuilder.OrientationKind strategy = AiBuilder.init_TableIndex_TaskQue(target, entry.getKey());
                                                         if (strategy != AiBuilder.OrientationKind.UNSUPPORTED
                                                            && strategy != AiBuilder.OrientationKind.POST_INTERACT
                                                            && strategy != AiBuilder.OrientationKind.FLUID_INTERACT
                                                            && !AiBuilder.read(target, entry.getKey())
                                                            && (!AiBuilder.init_Z(cell) || upgrade || !entry.getKey().equals("type"))
                                                            && (
                                                               strategy != AiBuilder.OrientationKind.CHEST_PAIR
                                                                  || !AiBuilder.init_Z(cell, predicted)
                                                                  || AiBuilder.init_Z_TableIndex_StreamU(cell, pos, mc.level)
                                                            )) {
                                                            Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)predicted.getBlock()
                                                               .getStateDefinition()
                                                               .getProperty(entry.getKey());
                                                            if (property == null
                                                               || !AiBuilder.init(
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
                                                      this.init_V_2("placement context would produce the wrong block state");
                                                      return this.init_AiOperation_Result_5("Re-solving the requested block state");
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

                                                      this.init_V_2("placement click refused");
                                                      return this.init_AiOperation_Result_5("Placement click refused; re-evaluating");
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
                                                   return this.init_AiOperation_Result_5(
                                                      "Placed " + cell.block() + " at " + pos.toShortString() + "; verifying"
                                                   );
                                                }

                                                this.init_AiOperation = schematic
                                                   ? AiGoto.approachSchematicPlacementCompat(pos, item, 10, this.init_AABB, this.init_Set_2())
                                                   : AiGoto.approachToPlaceCompat(pos, item, 10, this.init_AABB);
                                                return this.init_AiOperation_Result_5("Stepping out of the placement cell");
                                             }

                                             this.load_V(pos);
                                             return this.init_AiOperation_Result_5("Moving into reach of " + pos.toShortString());
                                          }

                                          this.load("block has no item form and cannot be placed");
                                       }
                                       break;
                                    }
                                 }
                              }
                           } else {
                              if (!this.write_Set.remove(pos.asLong())) {
                                 this.queue++;
                              }

                              this.cache = 0;
                              this.update();
                           }
                        }

                        if (this.data < this.init_List.size()) {
                           return this.init_AiOperation_Result_5("Building " + this.data + "/" + this.init_List.size());
                        }

                        if (!this.open_List.isEmpty()) {
                           this.write_V();
                        }

                        if (!this.open_List.isEmpty() && this.bind < 2) {
                           this.init_Map.clear();
                           this.read_Set.clear();
                           this.init_List.addAll(this.open_List);
                           this.open_List.clear();
                           this.poll_List.clear();
                           this.bind++;
                           return this.init_AiOperation_Result_5("Re-attempting deferred cells against the grown structure");
                        }

                        for (int i = 0; i < this.open_List.size(); i++) {
                           this.init_V_TableIndex_StreamU_Str(this.open_List.get(i), this.poll_List.get(i));
                        }

                        this.open_List.clear();
                        this.poll_List.clear();
                        AiOperation.Result finalDrain = (!this.update_Z || this.flush_AiOperation == null) && this.update_AiOperation == null
                           ? this.read_AiOperation_Result_4()
                           : null;
                        if (finalDrain != null) {
                           return finalDrain;
                        }

                        int audited = 0;

                        while (this.flags < this.init_List.size() && audited++ < (schematic ? 128 : 2147483647)) {
                           AiBuilder.CellSpec audit = this.init_List.get(this.flags++);
                           BlockPos auditPos = blueprint.origin().offset(audit.dx(), audit.dy(), audit.dz());
                           BlockState auditLive = mc.level.getBlockState(auditPos);
                           if (AiBuilder.init(audit, auditLive, power.substitutes(), schematic)) {
                              if (this.start_Set.remove(audit)) {
                                 this.buffer--;
                                 this.load_List
                                    .removeIf(
                                       entryx -> {
                                          JsonObject value = entryx.getAsJsonObject();
                                          return value.get("dx").getAsInt() == audit.dx()
                                             && value.get("dy").getAsInt() == audit.dy()
                                             && value.get("dz").getAsInt() == audit.dz();
                                       }
                                    );
                              }
                           } else if (schematic && this.pool < 2 && !this.update_List.contains(audit) && !AiBuilder.load(audit, auditLive)) {
                              this.update_List.add(audit);
                           } else {
                              this.init_V_TableIndex_StreamU_Str(audit, "divergent after verify: " + BuiltInRegistries.BLOCK.getKey(auditLive.getBlock()));
                           }
                        }

                        if (this.flags < this.init_List.size()) {
                           return this.init_AiOperation_Result_5("Verifying finished schematic " + this.flags + "/" + this.init_List.size());
                        }

                        if (!this.update_List.isEmpty()) {
                           TreeMap<String, Integer> why = new TreeMap<>();

                           for (AiBuilder.CellSpec repair : this.update_List) {
                              BlockState live = mc.level.getBlockState(blueprint.origin().offset(repair.dx(), repair.dy(), repair.dz()));
                              why.merge(
                                 repair.block().replace("minecraft:", "") + " <- " + BuiltInRegistries.BLOCK.getKey(live.getBlock()).getPath(), 1, Integer::sum
                              );
                           }

                           AutismClientAddon.LOG.info("[AI] build audit {}: {} cells to repair {}", this.pool, this.update_List.size(), why);
                           this.read_Map.clear();
                           this.push_Set.clear();
                           this.init_List.addAll(this.update_List);
                           this.update_List.clear();
                           this.pool++;
                           this.flags = 0;
                           if (schematic) {
                              this.pop_Z = false;
                              this.store = 0;
                           }

                           return this.init_AiOperation_Result_5("Repairing block states changed during construction");
                        } else {
                           if (this.close_Z && !this.push_Z) {
                              this.push_Z = true;
                              this.pop_Z = false;
                              this.store = 0;
                              this.pop_Set.clear();
                              this.read_Map.clear();
                              this.push_Set.clear();
                              this.pool = 0;
                              this.get_Set.clear();
                              this.put_Set.clear();
                              this.flags = 0;
                              return this.init_AiOperation_Result_5("Removing access stairs after the construction audit");
                           }

                           if (schematic && this.close_Z && power.clearTerrain() && this.count < 3) {
                              if (!this.update_Z) {
                                 this.update_Z = true;
                                 this.sync_List = null;
                                 this.block = 0;
                                 this.chunk = 0;
                              }

                              AiOperation.Result swept = this.write_AiOperation_Result_2();
                              if (swept != null) {
                                 return swept;
                              }

                              this.update_Z = false;
                              this.count++;
                              this.pop_Z = false;
                              this.store = 0;
                              this.get_Set.clear();
                              this.put_Set.clear();
                              if (this.sync_List.size() - this.chunk > 0) {
                                 AutismClientAddon.LOG
                                    .info("[AI] schematic sweep {}: {} leftover blocks cleared out of the air", this.count, this.sync_List.size() - this.chunk);
                                 this.flags = 0;
                                 return this.init_AiOperation_Result_5("Verifying the schematic after clearing leftover blocks");
                              }

                              this.count = 3;
                           }

                           if (this.update_AiOperation != null) {
                              AiOperation.Result r = this.update_AiOperation.tick();
                              if (!r.terminal()) {
                                 return this.init_AiOperation_Result_5("Closing a door left open: " + r.message());
                              }

                              this.update_AiOperation.cancel();
                              this.update_AiOperation = null;
                              AutismClientAddon.LOG.info("[AI] build: closing a door {}: {}", r.status(), r.message());
                              this.flags = 0;
                              return this.init_AiOperation_Result_5("Door closed; verifying the schematic again");
                           } else {
                              if (schematic && this.close_Z) {
                                 for (AiBuilder.CellSpec cell : blueprint.cells()) {
                                    if ("false".equals(cell.properties().get("open")) && !"upper".equals(cell.properties().get("half"))) {
                                       BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                                       BlockState live = mc.level.getBlockState(pos);
                                       if (live.getBlock() == AiBuilder.init_Block(cell)
                                          && live.hasProperty(BlockStateProperties.OPEN)
                                          && live.getValue(BlockStateProperties.OPEN)
                                          && this.flush_Set.add(pos.asLong())) {
                                          this.update_AiOperation = AiSchematicActions.init(cell, pos, this.init_Set_2());
                                          return this.init_AiOperation_Result_5(
                                             "Closing " + BuiltInRegistries.BLOCK.getKey(live.getBlock()).getPath() + " at " + pos.toShortString()
                                          );
                                       }
                                    }
                                 }
                              }

                              int supportsLeft = this.close_Z ? this.init_List_4().size() : 0;
                              return this.init_AiOperation_Result_AiOperation_Status_Str_4(
                                 this.buffer == 0 && supportsLeft == 0 ? AiOperation.Status.SUCCEEDED : AiOperation.Status.FAILED,
                                 this.buffer != 0
                                    ? "Blueprint incomplete: " + this.buffer + " failed cells"
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
                  return this.init_AiOperation_Result_AiOperation_Status_Str_4(AiOperation.Status.CANCELLED, "Build cancelled");
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

            void init_V_2(String reason) {
               this.load_BlockPos = null;
               this.load_Vec3 = null;
               this.load_U_CachePool = null;
               this.start_I = 0;
               this.init_F = 0.0F / 0.0F;
               this.stop_I = 0;
               this.read_Z = false;
               this.next_I = 0;
               this.init_Str = null;
               this.prev = 0;
               this.update_Set.clear();
               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }

               if (schematic) {
                  BlockState live = mc.level.getBlockState(this.load_BlockPos());
                  if (live.getBlock() != AiBuilder.init_Block(this.init_List.get(this.data))
                     && !live.canBeReplaced()
                     && live.getFluidState().isEmpty()
                     && !live.hasBlockEntity()) {
                     this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(this.load_BlockPos(), 30, true, this.init_Set_2(), true);
                     return;
                  }
               }

               if (this.init_Z_4(this.load_BlockPos())) {
                  this.init_AiOperation = AiGoto.travelToCompat(this.load_BlockPos(), 0.6, false, false, false, 15);
               } else if (!schematic
                  || !reason.contains("face") && !reason.contains("placement context")
                  || !this.close(this.init_List.get(this.data), this.load_BlockPos())
                     && !this.pop(this.load_BlockPos())
                     && !this.sync(this.init_List.get(this.data), this.load_BlockPos())) {
                  if (!this.sync_Z && mc.player.getY() > (schematic ? this.load_BlockPos().getY() + 3 : blueprint.origin().getY() + 1.5)) {
                     this.sync_Z = true;
                     this.cache = 0;
                     this.init_AiOperation = this.init_AiOperation();
                  } else {
                     this.value = Math.max(this.value, this.init_Map.getOrDefault(this.load_BlockPos().asLong(), 0)) + 1;
                     AiBuilder.init_V_Map(this.init_Map, this.load_BlockPos(), this.value);
                     if (this.value >= this.init_I) {
                        this.read(reason);
                     } else {
                        this.load_V(this.load_BlockPos());
                     }
                  }
               }
            }

            int init_I_10() {
               IntegratedServer server = mc.getSingleplayerServer();
               if (server == null) {
                  return 100;
               }

               double behindMs = Math.max(0.0, server.getCurrentSmoothedTickTime() - 50.0);
               return 100 + (int)Math.min(400.0, behindMs / 25.0 + 20.0);
            }

            boolean init_Z_4(BlockPos pos) {
               return !mc.level.getEntitiesOfClass(ItemEntity.class, new AABB(pos)).isEmpty();
            }

            AiOperation.Result load_AiOperation_Result_7() {
               if (this.flush_List.isEmpty() && this.push_AiOperation == null) {
                  AABB box = this.init_AABB.inflate(3.0);

                  for (BlockPos cell : AiScaffold.init(
                     (int)Math.floor(box.minX),
                     (int)Math.floor(box.minY),
                     (int)Math.floor(box.minZ),
                     (int)Math.ceil(box.maxX) - 1,
                     (int)Math.ceil(box.maxY) - 1,
                     (int)Math.ceil(box.maxZ) - 1
                  )) {
                     if (!this.init_AABB.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5)
                        && AiScaffold.init(AiScaffold.init_Block(cell), mc.level.getBlockState(cell).getBlock())) {
                        this.flush_List.add(cell);
                     }
                  }

                  if (this.flush_List.isEmpty()) {
                     return null;
                  }
               }

               if (this.fill >= 30) {
                  this.flush_List.clear();
                  return null;
               }

               if (this.push_AiOperation == null) {
                  BlockPos cell = null;
                  AABB playerBox = mc.player.getBoundingBox();

                  while (!this.flush_List.isEmpty()) {
                     BlockPos candidate = this.flush_List.remove(0);
                     if (!this.open_Set.contains(candidate.asLong()) && !AiSchematicActions.init(candidate, this.load_Map, mc.level)) {
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
                        this.tick_Set.add(candidate.asLong());
                        AiScaffold.init_V(candidate);
                     }
                  }

                  if (cell == null) {
                     return null;
                  }

                  this.push_AiOperation = autismclient.ai.engine.AiBreakBlock.init(cell, 10, true, this.init_Set_2(), false);
               }

               AiOperation.Result r = this.push_AiOperation.tick();
               if (!r.terminal()) {
                  return r;
               }

               this.push_AiOperation.cancel();
               this.push_AiOperation = null;
               this.fill++;
               return this.init_AiOperation_Result_5("Clearing old scaffolding");
            }

            AiOperation.Result read_AiOperation_Result_4() {
               if (schematic && !this.close_Z) {
                  return null;
               }

               if (this.open_AiOperation != null) {
                  AiOperation.Result escaped = this.open_AiOperation.tick();
                  if (!escaped.terminal()) {
                     return this.init_AiOperation_Result_5("Leaving the enclosed room before scaffold removal: " + escaped.message());
                  }

                  this.open_AiOperation.cancel();
                  this.open_AiOperation = null;
                  this.pop_Z = false;
                  this.close_List.clear();
                  return this.init_AiOperation_Result_5("Re-evaluating scaffold access after leaving the room");
               } else if (schematic
                  && this.push_AiOperation == null
                  && this.close_AiOperation == null
                  && this.poll_AiOperation == null
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

                  return this.init_AiOperation_Result_5("Waiting for the scaffold descent to land before removing its next support");
               } else {
                  if (schematic && this.push_AiOperation == null && this.close_AiOperation == null && this.poll_AiOperation == null && this.init_Z_8()) {
                     return this.init_AiOperation_Result_5("Opening a temporary exit from the enclosed room");
                  }

                  if (schematic && !this.pop_Z) {
                     if (this.poll_AiOperation != null) {
                        AiOperation.Result dropped = this.poll_AiOperation.tick();
                        if (!dropped.terminal()) {
                           return this.init_AiOperation_Result_5("Removing the temporary support underfoot");
                        }

                        this.poll_AiOperation.cancel();
                        this.poll_AiOperation = null;
                        return this.init_AiOperation_Result_5("Temporary support removed; re-evaluating the footing");
                     }

                     if (this.close_AiOperation != null) {
                        AiOperation.Result moved = this.close_AiOperation.tick();
                        if (!moved.terminal()) {
                           return this.init_AiOperation_Result_5("Stepping onto the finished structure before removing scaffolding");
                        }

                        this.close_AiOperation.cancel();
                        this.close_AiOperation = null;
                     }

                     boolean standingOnSupport = this.load_List().stream().anyMatch(this::load_Z_2);
                     if (standingOnSupport && this.get_Set.size() < 8) {
                        BlockPos stance = null;
                        double distance = 1.7976931348623157E308;

                        for (AiBuilder.CellSpec cell : blueprint.cells()) {
                           BlockPos floor = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                           BlockState state = mc.level.getBlockState(floor);
                           if (Block.isShapeFullBlock(state.getCollisionShape(mc.level, floor)) && !autismclient.ai.engine.AiPathfinder.LiveGridView.read(state)) {
                              BlockPos feet = floor.above();
                              if (!this.get_Set.contains(feet.asLong()) && !(Math.abs(feet.getY() - mc.player.getY()) > 2.0)) {
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
                           this.get_Set.add(stance.asLong());
                           this.close_AiOperation = AiGoto.travelToCompat(stance, 0.35, false, false, false, 10);
                           return this.init_AiOperation_Result_5("Moving off the temporary support");
                        }
                     }

                     if (standingOnSupport && this.load_List().stream().noneMatch(p -> p.getY() > mc.player.getY() + 3.0)) {
                        for (BlockPos floor : this.load_List()) {
                           if (this.load_Z_2(floor) && !this.put_Set.contains(floor.asLong()) && this.read(floor)) {
                              this.put_Set.add(floor.asLong());
                              this.poll_AiOperation = autismclient.ai.engine.AiBreakBlock.init(floor, 10, true, this.close_Set);
                              return this.init_AiOperation_Result_5("Dismantling the temporary column toward a safe landing");
                           }
                        }
                     }
                  }

                  if (!this.pop_Z) {
                     this.pop_Z = true;
                     if (this.close_Set.isEmpty()) {
                        for (AiBuilder.CellSpec c : this.init_List) {
                           this.close_Set.add(blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong());
                        }
                     }

                     AABB cleanupVolume = schematic ? this.init_AABB.inflate(3.0) : this.init_AABB;
                     List<BlockPos> supports = schematic
                        ? this.load_List()
                        : AiScaffold.init(
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

                     this.table = supports.size();
                     this.update_J = System.currentTimeMillis();
                     this.write_J = this.update_J;

                     for (BlockPos cell : supports) {
                        if (!this.close_Set.contains(cell.asLong())
                           && !this.open_Set.contains(cell.asLong())
                           && AiScaffold.init(this.init_Block(cell), mc.level.getBlockState(cell).getBlock())) {
                           this.close_List.add(cell);
                        }
                     }
                  }

                  while (this.push_AiOperation == null) {
                     long drainNow = System.currentTimeMillis();
                     if (this.update_J == 0L) {
                        this.update_J = drainNow;
                        this.write_J = drainNow;
                     }

                     if (this.close_List.isEmpty() || drainNow - this.write_J <= 5000L && drainNow - this.update_J <= 60000L) {
                        if (!this.close_List.isEmpty() && this.store < (schematic ? 2097152 : 30)) {
                           BlockPos candidate = this.close_List
                              .remove(AiBuilder.init_I_List(this.close_List, mc.player.position(), cell -> autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, cell) != null));
                           long candidateNow = System.currentTimeMillis();
                           int drainAttempts = this.write_Map.getOrDefault(candidate.asLong(), 0);
                           if (candidateNow < this.update_Map.getOrDefault(candidate.asLong(), 0L)) {
                              this.close_List.add(candidate);
                              continue;
                           }

                           if (drainAttempts < 3 && autismclient.ai.engine.AiBlockInteraction.findReachableHit(mc, candidate) == null) {
                              BlockPos beside = this.init_BlockPos_3(candidate);
                              if (beside != null) {
                                 this.write_Map.put(candidate.asLong(), drainAttempts + 1);
                                 this.update_Map.put(candidate.asLong(), candidateNow + 20L);
                                 this.close_List.add(candidate);
                                 AutismClientAddon.LOG
                                    .info(
                                       "[AI] build: step-off attempt {}/3 at {} toward {}",
                                       drainAttempts + 1,
                                       candidate.toShortString(),
                                       beside.toShortString()
                                    );
                                 this.write_BlockPos = null;
                                 this.push_AiOperation = AiGoto.travelToCompat(beside, 0.35, false, false, false, 10);
                                 return this.init_AiOperation_Result_5("Stepping beside a temporary support to remove it");
                              }
                           }

                           if (drainAttempts >= 3) {
                              this.write_Map.put(candidate.asLong(), drainAttempts + 1);
                              AutismClientAddon.LOG
                                 .info("[AI] build: drain support retired at {} reason=step-off-cap (attempts 3/3); forcing removal", candidate.toShortString());
                           }

                           if (AiSchematicActions.init(candidate, this.load_Map, mc.level)) {
                              this.tick_Set.add(candidate.asLong());
                              AiScaffold.init_V(candidate);
                              continue;
                           }

                           if (this.load_Z_2(candidate)) {
                              continue;
                           }

                           HashSet<Long> keep = schematic ? new HashSet<>(this.init_Set_2()) : new HashSet<>();
                           if (schematic) {
                              keep.addAll(this.close_Set);
                           }

                           if (schematic) {
                              for (BlockPos support : this.init_List_4()) {
                                 keep.add(support.asLong());
                              }
                           }

                           keep.remove(candidate.asLong());
                           this.write_BlockPos = candidate;
                           this.activeKeep = keep;
                           this.push_AiOperation = autismclient.ai.engine.AiBreakBlock.init(candidate, 10, true, keep, false);
                           continue;
                        }

                        if (schematic) {
                           int left = this.load_List().size();
                           if (left > 0 && left < this.table) {
                              this.close_List.clear();
                              this.pop_Z = false;
                              this.store = 0;
                              this.get_Set.clear();
                              this.put_Set.clear();
                              return this.init_AiOperation_Result_5("Rechecking supports exposed by scaffold removal");
                           }
                        }

                        return null;
                     }

                     int drainLeft = this.load_List().size();
                     AutismClientAddon.LOG
                        .info("[AI] build: drain pass bounded out (no removal for {} ms); leaving {} supports", drainNow - this.write_J, drainLeft);
                     this.close_List.clear();
                     this.write_Map.clear();
                     this.update_Map.clear();
                     return null;
                  }

                  AiOperation.Result r = this.push_AiOperation.tick();
                  if (!r.terminal()) {
                     return r;
                  }

                  this.push_AiOperation.cancel();
                  this.push_AiOperation = null;
                  if (this.write_BlockPos == null) {
                     return this.init_AiOperation_Result_5("Rechecking reach from beside the temporary support");
                  }

                  if (schematic
                     && !this.retryingDetached
                     && r.failure() == AiFailure.UNREACHABLE
                     && this.write_BlockPos.getY() > mc.player.getEyeY() + 1.0
                     && this.detachedSupports != null
                     && this.detachedSupports.remove(this.write_BlockPos.asLong())) {
                     this.retryingDetached = true;
                     this.push_AiOperation = autismclient.ai.engine.AiBreakBlock.init(this.write_BlockPos, 15, true, this.activeKeep, true);
                     return this.init_AiOperation_Result_5("Reaching the detached temporary support");
                  }

                  this.store++;
                  if (r.status() == AiOperation.Status.SUCCEEDED) {
                     this.drain++;
                     this.write_J = System.currentTimeMillis();
                     if (this.write_BlockPos != null) {
                        AiScaffold.init_V(this.write_BlockPos);
                     }
                  } else if (this.write_BlockPos != null) {
                     int failedAttempts = this.write_Map.getOrDefault(this.write_BlockPos.asLong(), 0) + 1;
                     this.write_Map.put(this.write_BlockPos.asLong(), failedAttempts);
                     this.update_Map.put(this.write_BlockPos.asLong(), System.currentTimeMillis() + 20L);
                     if (failedAttempts >= 4 && Boolean.getBoolean("autism.navigation.debug")) {
                        AutismClientAddon.LOG
                           .info("[AI] build: retired support could not be removed at {} (drainFailed {})", this.write_BlockPos.toShortString(), failedAttempts);
                     }
                  }

                  if (r.status() != AiOperation.Status.SUCCEEDED && Boolean.getBoolean("autism.navigation.debug")) {
                     AutismClientAddon.LOG.info("[AI] scaffold cleanup failed: {}", r.message());
                  }

                  if (this.retryingDetached) {
                     this.retryingDetached = false;
                     this.pop_Z = false;
                     this.close_List.clear();
                  }

                  this.write_BlockPos = null;
                  this.activeKeep = Set.of();
                  return this.init_AiOperation_Result_5("Clearing scaffolding from the finished work");
               }
            }

            BlockPos init_BlockPos_3(BlockPos candidate) {
               for (Direction dir : Plane.HORIZONTAL) {
                  BlockPos feet = candidate.relative(dir);
                  if (mc.level.getBlockState(feet).getCollisionShape(mc.level, feet).isEmpty()
                     && mc.level.getBlockState(feet.above()).getCollisionShape(mc.level, feet.above()).isEmpty()) {
                     BlockState below = mc.level.getBlockState(feet.below());
                     if (Block.isShapeFullBlock(below.getCollisionShape(mc.level, feet.below())) && !autismclient.ai.engine.AiPathfinder.LiveGridView.read(below)) {
                        AABB body = mc.player.getBoundingBox().move(Vec3.atBottomCenterOf(feet).subtract(mc.player.position()));
                        if (mc.level.noCollision(mc.player, body) && !(mc.player.position().distanceToSqr(Vec3.atCenterOf(candidate)) > 20.25)) {
                           return feet;
                        }
                     }
                  }
               }

               return null;
            }

            boolean load_Z_2(BlockPos candidate) {
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
                           if (!autismclient.ai.engine.AiPathfinder.LiveGridView.read(state)
                              && !autismclient.ai.engine.AiPathfinder.LiveGridView.load(state)
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

            boolean init_Z_8() {
               if (!this.load_List().isEmpty() && this.run_Set.size() < 2 && this.load_Z_4()) {
                  BlockPos worker = mc.player.blockPosition();
                  long column = BlockPos.asLong(worker.getX(), 0, worker.getZ());
                  if (this.run_Set.contains(column)) {
                     return false;
                  }

                  BlockPos doorExit = null;
                  double distance = 1.7976931348623157E308;

                  for (BlockPos candidate : List.of(
                     new BlockPos((int)this.init_AABB.minX - 1, worker.getY(), worker.getZ()),
                     new BlockPos((int)this.init_AABB.maxX, worker.getY(), worker.getZ()),
                     new BlockPos(worker.getX(), worker.getY(), (int)this.init_AABB.minZ - 1),
                     new BlockPos(worker.getX(), worker.getY(), (int)this.init_AABB.maxZ)
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

                        this.run_Set.add(column);
                        this.read_Map.merge(column, ceiling.getY(), Math::min);
                        BlockPos exit = new BlockPos(worker.getX(), roof + 1, worker.getZ());
                        this.open_AiOperation = AiGoto.travelToCompat(
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
                              autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(exit, 0.25),
                              this.load_Set(),
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

                     this.run_Set.add(column);
                     this.open_AiOperation = AiGoto.travelToCompat(
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
                           autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(doorExit, 0.25),
                           this.load_Set(),
                           blueprint.origin().getY()
                        )
                     );
                     return true;
                  }
               } else {
                  return false;
               }
            }

            List<BlockPos> init_List_4() {
               LinkedHashSet<BlockPos> owned = new LinkedHashSet<>();
               if (this.init_a_StreamUtil != null) {
                  owned.addAll(this.init_a_StreamUtil.init_List());
               }

               for (long key : this.poll_Set) {
                  owned.add(BlockPos.of(key));
               }

               return owned.stream()
                  .filter(
                     p -> !this.close_Set.contains(p.asLong())
                        && !this.open_Set.contains(p.asLong())
                        && AiScaffold.init(this.init_Block(p), mc.level.getBlockState(p).getBlock())
                  )
                  .filter(p -> {
                     if (!AiSchematicActions.init(p, this.load_Map, mc.level)) {
                        return true;
                     }

                     this.tick_Set.add(p.asLong());
                     this.poll_Set.remove(p.asLong());
                     AiScaffold.init_V(p);
                     return false;
                  })
                  .sorted(Comparator.<BlockPos>comparingInt(p -> p.getY()).reversed())
                  .toList();
            }

            List<BlockPos> load_List() {
               return this.init_List_4().stream().filter(p -> this.push_Z || !this.poll_Set.contains(p.asLong())).toList();
            }

            Block init_Block(BlockPos pos) {
               Block tracked = this.init_a_StreamUtil == null ? null : this.init_a_StreamUtil.init(pos);
               return tracked == null ? AiScaffold.init_Block(pos) : tracked;
            }

            AiOperation init_AiOperation() {
               if (this.close_Set.isEmpty()) {
                  for (AiBuilder.CellSpec c : this.init_List) {
                     this.close_Set.add(blueprint.origin().offset(c.dx(), c.dy(), c.dz()).asLong());
                  }
               }

               return new AiOperation() {
                  final Minecraft init_Minecraft = Minecraft.getInstance();
                  AiOperation init_AiOperation;
                  int init_I;

                  BlockPos init() {
                     AABB box = this.init_Minecraft.player.getBoundingBox();
                     int[] xs = new int[]{(int)Math.floor(box.minX + 0.01), (int)Math.floor(box.maxX - 0.01)};
                     int[] zs = new int[]{(int)Math.floor(box.minZ + 0.01), (int)Math.floor(box.maxZ - 0.01)};
                     int y = (int)Math.floor(this.init_Minecraft.player.getY()) - 1;

                     for (int dy = 0; dy >= -1; dy--) {
                        for (int x : xs) {
                           for (int z : zs) {
                              BlockPos cell = new BlockPos(x, y + dy, z);
                              if (!this.init_Minecraft.level.getBlockState(cell).isAir()) {
                                 return cell;
                              }
                           }
                        }
                     }

                     return null;
                  }

                  @Override
                  public AiOperation.Result tick() {
                     if (this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
                        if (this.init_Minecraft.player.getY() <= blueprint.origin().getY() + 0.5) {
                           return this.init(AiOperation.Status.SUCCEEDED, "Back on the ground");
                        }

                        if (this.init_I >= 64) {
                           return this.init(AiOperation.Status.FAILED, "Could not climb down from the structure");
                        }

                        BlockPos below = this.init();
                        if (below == null) {
                           return this.init(AiOperation.Status.FAILED, "No solid support below the player");
                        }

                        if (close_Set.contains(below.asLong())) {
                           return this.init(AiOperation.Status.FAILED, "Standing on the blueprint itself");
                        }

                        if (!read(below)) {
                           return this.init(AiOperation.Status.FAILED, "Removing this support would cause an unsafe fall");
                        }

                        if (this.init_AiOperation == null) {
                           this.init_I++;
                           this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(below, 15);
                        }

                        AiOperation.Result r = this.init_AiOperation.tick();
                        if (!r.terminal()) {
                           return r;
                        }

                        this.init_AiOperation = null;
                        return r.status() != AiOperation.Status.SUCCEEDED
                           ? this.init(AiOperation.Status.FAILED, r.message())
                           : new AiOperation.Result(AiOperation.Status.RUNNING, "Climbing down from the structure", "{}", List.of());
                     } else {
                        return this.init(AiOperation.Status.CANCELLED, "Climb-down cancelled");
                     }
                  }

                  @Override
                  public void cancel() {
                     if (this.init_AiOperation != null) {
                        this.init_AiOperation.cancel();
                     }
                  }

                  AiOperation.Result init(AiOperation.Status status, String message) {
                     this.cancel();
                     return AiGameOperations.result(status, message, new JsonObject());
                  }
               };
            }

            AiOperation.Result write_AiOperation_Result_2() {
               if (this.flush_AiOperation != null) {
                  AiOperation.Result r = this.flush_AiOperation.tick();
                  if (!r.terminal()) {
                     return this.init_AiOperation_Result_5("Clearing terrain " + this.block + "/" + this.sync_List.size() + ": " + r.message());
                  }

                  this.flush_AiOperation.cancel();
                  this.flush_AiOperation = null;
                  if (r.status() != AiOperation.Status.SUCCEEDED && !mc.level.getBlockState(this.sync_List.get(this.block)).isAir()) {
                     this.chunk++;
                  }

                  this.block++;
               }

               if (this.sync_List == null) {
                  this.sync_List = new ArrayList<>(AiSchematicActions.init_List_TableIndex_DataUti(blueprint, this.load_Map, mc.level, this.update_Z ? AiSchematicActions::load : AiSchematicActions::init));
                  this.sync_List.removeIf(px -> this.open_Set.contains(px.asLong()));
                  if (this.update_Z) {
                     for (BlockPos stray : AiSchematicActions.load(blueprint, this.load_Map, mc.level)) {
                        if (!this.open_Set.contains(stray.asLong()) && !this.sync_List.contains(stray)) {
                           this.sync_List.add(stray);
                        }
                     }
                  }

                  if (!this.sync_List.isEmpty()) {
                     AutismClientAddon.LOG.info("[AI] schematic terrain: {} natural blocks stand in its air", this.sync_List.size());
                  }
               }

               while (this.block < this.sync_List.size()) {
                  int top = this.sync_List.get(this.block).getY();
                  int best = this.block;
                  double bestSq = 1.7976931348623157E308;

                  for (int i = this.block; i < this.sync_List.size() && this.sync_List.get(i).getY() == top; i++) {
                     double sq = this.sync_List.get(i).distToCenterSqr(mc.player.position());
                     if (sq < bestSq) {
                        bestSq = sq;
                        best = i;
                     }
                  }

                  Collections.swap(this.sync_List, this.block, best);
                  BlockPos p = this.sync_List.get(this.block);
                  BlockState state = mc.level.getBlockState(p);
                  if (this.update_Z ? !AiSchematicActions.load(state) : !AiSchematicActions.init(state)) {
                     this.block++;
                  } else {
                     if (AiSchematicActions.init(mc.level, p)) {
                        HashSet<Long> keep = new HashSet<>(this.init_Set_2());
                        if (this.update_Z) {
                           keep.addAll(this.close_Set);
                        }

                        this.flush_AiOperation = autismclient.ai.engine.AiBreakBlock.init(p, this.update_Z ? 15 : 20, false, keep, false);
                        return this.init_AiOperation_Result_5(
                           "Clearing " + BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath() + " out of the schematic's air at " + p.toShortString()
                        );
                     }

                     this.chunk++;
                     this.block++;
                  }
               }

               if (this.chunk > 0) {
                  AutismClientAddon.LOG.info("[AI] schematic terrain: {} blocks left (fluid beside them or unbreakable)", this.chunk);
               }

               return null;
            }

            boolean init_Z_TableIndex_StreamU_BlockPos(AiBuilder.CellSpec cell, BlockPos pos) {
               if (!AiSchematicActions.init_Z(cell) && !AiBuilder.init_Z(cell)) {
                  Block block = AiBuilder.init_Block(cell);
                  Item item = cell.block().equals("minecraft:grass_block") ? Items.DIRT : block.asItem();
                  if (item != Items.AIR && this.init(item) > 0) {
                     BlockState live = mc.level.getBlockState(pos);
                     if (live.canBeReplaced() && live.getFluidState().isEmpty() && live.getBlock() != block) {
                        if (mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Math.pow(mc.player.blockInteractionRange() + 0.5, 2.0)) {
                           return false;
                        } else if (AiBuilder.init(cell, pos, mc.player.getBoundingBox(), mc.level)) {
                           return false;
                        } else {
                           return AiBuilder.init_BlockState(cell).canSurvive(mc.level, pos) && !this.load_Z(cell, pos)
                              ? autismclient.ai.engine.PlacementGoal.init(mc, pos, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, block), 72) != null
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

            boolean load_Z(AiBuilder.CellSpec cell, BlockPos pos) {
               if (this.load_Str(cell, pos) != null) {
                  return true;
               }

               if (this.poll_Map.getOrDefault(cell, 0) >= 4) {
                  return false;
               }

               BlockPos previous = this.sync_BlockPos;

               try {
                  return this.init_Str_TableIndex_StreamU_BlockPos(cell, pos) != null;
               } finally {
                  this.sync_BlockPos = previous;
               }
            }

            void load(String reason) {
               this.init_V_TableIndex_StreamU_Str(this.init_List.get(this.data), reason);
               this.update();
            }

            void init_V_TableIndex_StreamU_Str(AiBuilder.CellSpec cell, String reason) {
               if (this.start_Set.add(cell)) {
                  if (this.load_List.size() < 32) {
                     this.load_List.add(AiJson.objectOf("dx", cell.dx(), "dy", cell.dy(), "dz", cell.dz(), "block", cell.block(), "reason", reason));
                  }

                  this.buffer++;
               }
            }

            boolean load_Z_4() {
               if (!this.init_AABB.intersects(mc.player.getBoundingBox())) {
                  return false;
               }

               BlockPos start = mc.player.blockPosition();
               ArrayDeque<BlockPos> queue = new ArrayDeque<>();
               HashSet<Long> seen = new HashSet<>();
               queue.add(start);
               seen.add(start.asLong());

               while (!queue.isEmpty() && seen.size() < 4000) {
                  BlockPos cell = queue.poll();
                  if (this.sync(cell)) {
                     for (BlockPos next : List.of(cell.above(), cell.below())) {
                        if (this.init_AABB.inflate(4.0).contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5)
                           && seen.add(next.asLong())
                           && (this.sync(next) || this.write(next))) {
                           if (!this.init_AABB.contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5)) {
                              return false;
                           }

                           queue.add(next);
                        }
                     }
                  }

                  for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                     for (int dy = -3; dy <= 1; dy++) {
                        BlockPos next = cell.offset(d[0], dy, d[1]);
                        if (this.init_AABB.inflate(4.0).contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5) && seen.add(next.asLong())) {
                           boolean inside = this.init_AABB.contains(next.getX() + 0.5, next.getY(), next.getZ() + 0.5);
                           if (this.write(next) || dy == 0 && this.sync(next)) {
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

            boolean write(BlockPos cell) {
               return this.update(cell)
                  && this.update(cell.above())
                  && !mc.level.getBlockState(cell.below()).getCollisionShape(mc.level, cell.below()).isEmpty();
            }

            boolean update(BlockPos cell) {
               BlockState state = mc.level.getBlockState(cell);
               return state.getCollisionShape(mc.level, cell).isEmpty()
                  || state.getBlock() instanceof DoorBlock
                  || state.getBlock() instanceof FenceGateBlock
                  || state.getBlock() instanceof TrapDoorBlock;
            }

            boolean sync(BlockPos cell) {
               return mc.level.getBlockState(cell).is(BlockTags.CLIMBABLE) && this.update(cell.above());
            }

            void read_V_2() {
               if (!schematic) {
                  for (int i = 0; i < this.data && i < this.init_List.size(); i++) {
                     AiBuilder.CellSpec cell = this.init_List.get(i);
                     BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                     if (!AiBuilder.init(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)
                        && !this.open_List.contains(cell)
                        && !this.init_Z(cell)) {
                        this.open_List.add(cell);
                        this.poll_List.add("breached during escape");
                     }
                  }
               }
            }

            boolean init_Z(AiBuilder.CellSpec cell) {
               for (JsonObject entry : this.load_List) {
                  JsonObject object = entry.getAsJsonObject();
                  if (object.get("dx").getAsInt() == cell.dx() && object.get("dy").getAsInt() == cell.dy() && object.get("dz").getAsInt() == cell.dz()) {
                     return true;
                  }
               }

               return false;
            }

            void write_V() {
               TreeMap<String, Integer> counts = new TreeMap<>();

               for (int i = 0; i < this.open_List.size(); i++) {
                  String reason = this.poll_List.get(i) == null ? "?" : this.poll_List.get(i).replaceAll("-?[0-9]+, -?[0-9]+, -?[0-9]+", "<pos>");
                  counts.merge(
                     this.open_List.get(i).block().replace("minecraft:", "") + ": " + (reason.length() > 90 ? reason.substring(0, 90) : reason),
                     1,
                     Integer::sum
                  );
               }

               AutismClientAddon.LOG.info("[AI] build pass {} deferred {} cells: {}", this.bind, this.open_List.size(), counts);
            }

            void read(String reason) {
               BlockPos pos = this.load_BlockPos();
               long key = pos.asLong();
               if (Boolean.getBoolean("autism.navigation.debug")) {
                  AutismClientAddon.LOG.info("[AI] schematic defer {} {}: {}", pos.toShortString(), this.init_List.get(this.data).block(), reason);
               }

               if (this.init_Map.containsKey(key)
                  || this.write_Set.contains(key)
                  || pos.equals(this.init_TableIndex_StateCa.init_BlockPos())
                  || pos.equals(this.init_TableIndex_SyncMan.load())) {
                  this.read_Set.add(key);
               }

               this.open_List.add(this.init_List.get(this.data));
               this.poll_List.add(reason);
               this.cache = 0;
               this.update();
            }

            void update() {
               if (schematic) {
                  mc.options.keyShift.setDown(false);
               }

               if (this.close_Z && this.data < this.init_List.size()) {
                  AiBuilder.CellSpec completed = this.init_List.get(this.data);
                  BlockPos pos = blueprint.origin().offset(completed.dx(), completed.dy(), completed.dz());
                  if (AiBuilder.init(completed, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     AiScaffold.init_V(pos);
                     if (schematic && this.pool > 0) {
                        this.pop_Set.add(pos.asLong());
                     }
                  }
               }

               this.data++;
               this.sync();
            }

            void sync() {
               this.load_Set.clear();
               this.value = this.data < this.init_List.size() ? this.init_Map.getOrDefault(this.load_BlockPos().asLong(), 0) : 0;
               this.sync_Z = false;
               this.cache = 0;
               this.parse = 0;
               this.scan = 0;
               this.entry = this.node = this.task = 0;
               this.update_Set.clear();
               this.write_Z = false;
               this.load_BlockPos = null;
               this.read_TableIndex_StreamU = null;
               this.load_U_CachePool = null;
               this.start_I = 0;
               this.init_F = 0.0F / 0.0F;
               this.load_Z = false;
               this.stop_I = 0;
               this.read_Z = false;
               this.next_I = 0;
               this.init_Str = null;
               this.prev = 0;
               this.sync_Set.clear();
               this.set_Set.clear();
               this.tick_Z = false;
               this.get_Z = this.set_Z = false;
               this.put_Z = false;
            }

            Set<Long> init_Set_2() {
               BlockPos worker = mc.player.blockPosition();
               if (this.stop_Set != null && this.f_e_I == mc.player.tickCount && worker.equals(this.update_BlockPos)) {
                  return this.stop_Set;
               }

               this.stop_Set = Collections.unmodifiableSet(this.init(false));
               this.f_e_I = mc.player.tickCount;
               this.update_BlockPos = worker;
               return this.stop_Set;
            }

            Set<Long> load_Set() {
               LinkedHashSet<Long> cells = new LinkedHashSet<>(this.init(true));
               if (schematic) {
                  cells.addAll(this.close_Set);
               }

               return Collections.unmodifiableSet(cells);
            }

            Set<Long> init(boolean recovery) {
               return this.init_Set_Z_BlockPos(recovery, null);
            }

            Set<Long> init_Set_Z_BlockPos(boolean recovery, BlockPos ignore) {
               BlockPos worker = mc.player.blockPosition();
               if (recovery && this.close_Z && worker.getY() + 2 < this.init_AABB.maxY && (worker.getY() < blueprint.origin().getY() || this.close(worker))) {
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

               if (recovery && this.close_Z && AiGoto.recentNoRouteCountCompat(autismclient.ai.engine.AiStance.init(mc)) >= 3 && this.flush(worker) && this.close(worker)) {
                  Integer before = this.read_Map.get(BlockPos.asLong(worker.getX(), 0, worker.getZ()));
                  if (before == null || before > blueprint.origin().getY()) {
                     AutismClientAddon.LOG.info("[AI] build: stranded on the work at {}; its column opens down to the ground", worker.toShortString());
                     this.read_Map.put(BlockPos.asLong(worker.getX(), 0, worker.getZ()), blueprint.origin().getY());
                  }
               }

               HashSet<Long> keep = new HashSet<>();
               keep.addAll(this.poll_Set);
               keep.addAll(this.tick_Set);
               if (this.init_a_StreamUtil != null) {
                  for (BlockPos support : this.init_a_StreamUtil.init_List()) {
                     if (!this.close_Set.contains(support.asLong())
                        && AiScaffold.init(this.init_a_StreamUtil.init(support), mc.level.getBlockState(support).getBlock())
                        && AiSchematicActions.init(support, this.load_Map, mc.level)) {
                        keep.add(support.asLong());
                     }
                  }
               }

               if (recovery && this.close_Z && this.data < this.init_List.size()) {
                  this.init_V_2(this.load_BlockPos());
               }

               for (long key : this.close_Set) {
                  BlockPos pos = BlockPos.of(key);
                  BlockState live = mc.level.getBlockState(pos);
                  if (this.pop_Set.contains(key)) {
                     keep.add(key);
                  } else {
                     Integer opening = this.read_Map.get(BlockPos.asLong(pos.getX(), 0, pos.getZ()));
                     if (!AiBuilder.init(recovery, this.push_Set.contains(key), opening, pos.getY(), live)) {
                        AiBuilder.CellSpec wantedHere = this.load_Map.get(key);
                        boolean stray = schematic && wantedHere != null && !live.hasBlockEntity() && AiSchematicActions.load(live) && !AiBuilder.read(wantedHere, live);
                        if (AiBuilder.init_Z_TableIndex_StreamU_4(this.load_Map.get(key), live, stray || AiScaffold.init(this.init_Block(pos), live.getBlock()))) {
                           keep.add(key);
                        }
                     }
                  }
               }

               for (long key : this.open_Set) {
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

            String init_Str_TableIndex_StreamU_BlockPos(AiBuilder.CellSpec cell, BlockPos pos) {
               this.sync_BlockPos = null;
               Block target = AiBuilder.init_Block(cell);
               ClientLevel level = mc.level;
               String conflict = this.load_Str(cell, pos);
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
                        && Block.isShapeFullBlock(AiBuilder.init_BlockState(wanted).getCollisionShape(level, watched))
                        && !AiBuilder.read(wanted, level.getBlockState(watched))) {
                        this.sync_BlockPos = watched;
                        return "an observer waits for the cell it watches";
                     } else {
                        return null;
                     }
                  }
               } else {
                  if (this.init(target)) {
                     for (Direction direction : Direction.values()) {
                        BlockPos observer = pos.relative(direction);
                        AiBuilder.CellSpec wanted = this.load_Map.get(observer.asLong());
                        if (wanted != null
                           && AiBuilder.init_Block(wanted) instanceof ObserverBlock
                           && Direction.byName(wanted.properties().getOrDefault("facing", "north")) == direction) {
                           BlockPos watched = observer.relative(direction);

                           for (BlockPos near : List.of(
                              watched, watched.north(), watched.south(), watched.east(), watched.west(), watched.above(), watched.below()
                           )) {
                              if (!near.equals(observer) && !near.equals(pos)) {
                                 AiBuilder.CellSpec around = this.load_Map.get(near.asLong());
                                 if (around != null && !AiBuilder.read(around, level.getBlockState(near))) {
                                    this.sync_BlockPos = near;
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

            String load_Str(AiBuilder.CellSpec cell, BlockPos pos) {
               Block target = AiBuilder.init_Block(cell);
               ClientLevel level = mc.level;
               if (target instanceof RedstoneTorchBlock && "false".equals(cell.properties().get("lit")) && AiBuilder.init_Z_Level(level, pos, cell)) {
                  return "an unlit redstone torch waits for its block's power";
               }

               if (target instanceof PistonBaseBlock && !"true".equals(cell.properties().get("extended"))) {
                  Direction facing = Direction.byName(cell.properties().getOrDefault("facing", "north"));
                  if (facing != null && AiBuilder.init_Z_Level_2(level, pos, facing)) {
                     return "a retracted piston waits until nothing powers it";
                  }
               }

               return target instanceof TntBlock && level.hasNeighborSignal(pos) ? "TNT waits until nothing powers it" : null;
            }

            boolean init(Block target) {
               return target instanceof PistonBaseBlock
                  || target instanceof DispenserBlock
                  || target instanceof CrafterBlock
                  || target instanceof TntBlock
                  || target instanceof DoorBlock
                  || target instanceof TrapDoorBlock
                  || target instanceof FenceGateBlock;
            }

            boolean init_Z_TableIndex_StreamU_Str(AiBuilder.CellSpec cell, String reason) {
               int after = -1;
               if (this.sync_BlockPos != null) {
                  for (int j = this.data + 1; j < this.init_List.size(); j++) {
                     AiBuilder.CellSpec next = this.init_List.get(j);
                     if (blueprint.origin().offset(next.dx(), next.dy(), next.dz()).equals(this.sync_BlockPos)) {
                        after = j;
                        break;
                     }
                  }
               }

               if (after < 0) {
                  after = Math.min(this.init_List.size() - 1, this.data + 32);
               }

               if (after <= this.data) {
                  return false;
               }

               this.poll_Map.merge(cell, 1, Integer::sum);
               if (Boolean.getBoolean("autism.navigation.debug")) {
                  AutismClientAddon.LOG.info("[AI] schematic hold {} {}: {}", this.load_BlockPos().toShortString(), cell.block(), reason);
               }

               this.init_List.add(after + 1, cell);
               this.init_List.remove(this.data);
               this.value = this.init_Map.getOrDefault(this.load_BlockPos().asLong(), 0);
               this.load_U_CachePool = null;
               this.start_I = 0;
               this.init_F = 0.0F / 0.0F;
               this.load_Z = false;
               return true;
            }

            boolean init_Z_TableIndex_StreamU(AiBuilder.CellSpec cell, BlockPos pos, Block targetBlock) {
               if (this.open_Map.getOrDefault(cell, 0) >= 2) {
                  return false;
               }

               HashSet<Long> supports = new HashSet<>();

               for (Direction direction : Direction.values()) {
                  if (AiBuilder.init_Z_TableIndex_StreamU_2(cell, targetBlock, direction)) {
                     supports.add(pos.relative(direction).asLong());
                  }
               }

               int after = -1;

               for (int j = this.data + 1; j < this.init_List.size() && after < 0; j++) {
                  AiBuilder.CellSpec next = this.init_List.get(j);
                  if (supports.contains(blueprint.origin().offset(next.dx(), next.dy(), next.dz()).asLong())) {
                     after = j;
                  }
               }

               if (after < 0) {
                  return false;
               }

               this.open_Map.merge(cell, 1, Integer::sum);
               this.init_List.add(after + 1, cell);
               this.init_List.remove(this.data);
               this.value = this.init_Map.getOrDefault(this.load_BlockPos().asLong(), 0);
               this.load_U_CachePool = null;
               this.start_I = 0;
               this.init_F = 0.0F / 0.0F;
               this.load_Z = false;
               return true;
            }

            BlockPos init_BlockPos_3() {
               AABB body = mc.player.getBoundingBox().deflate(0.05);
               BlockPos feet = mc.player.blockPosition();

               for (BlockPos cell : List.of(feet, feet.above())) {
                  BlockState state = mc.level.getBlockState(cell);
                  if (!state.isAir() && state.getFluidState().isEmpty() && !state.hasBlockEntity() && !(state.getDestroySpeed(mc.level, cell) < 0.0F)) {
                     VoxelShape shape = state.getCollisionShape(mc.level, cell);
                     if (!shape.isEmpty() && shape.bounds().move(cell).intersects(body)) {
                        AiBuilder.CellSpec wanted = this.load_Map.get(cell.asLong());
                        if ((wanted == null || !AiBuilder.read(wanted, state)) && (AiSchematicActions.load(state) || wanted != null)) {
                           return cell.immutable();
                        }
                     }
                  }
               }

               return null;
            }

            boolean flush(BlockPos worker) {
               return mc.player.onGround() && worker.getY() > blueprint.origin().getY() + 3;
            }

            boolean close(BlockPos worker) {
               if (worker.asLong() == this.flush_J && mc.player.tickCount == this.f_g_I) {
                  return this.reset_Z;
               }

               this.flush_J = worker.asLong();
               this.f_g_I = mc.player.tickCount;
               return this.reset_Z = AiSchematicActions.init_Z_BlockPos(worker, this.init_AABB.inflate(1.0), 600, this::poll);
            }

            boolean open(BlockPos worker) {
               return AiSchematicActions.init_Z_BlockPos(worker, this.init_AABB.inflate(1.0), 24000, this::poll);
            }

            boolean openExit(BlockPos worker) {
               if (schematic && this.mark < 6) {
                  final Set<Long> room = AiSchematicActions.init_Set_BlockPos(worker, this.init_AABB.inflate(1.0), 24000, this::poll);
                  if (room.isEmpty()) {
                     return false;
                  }

                  final ArrayList<BlockPos> placedInRoom = new ArrayList<>();

                  for (long key : room) {
                     AiBuilder.CellSpec cell = this.load_Map.get(key);
                     BlockPos p = BlockPos.of(key);
                     if (cell != null && AiBuilder.read(cell, mc.level.getBlockState(p))) {
                        placedInRoom.add(p);
                     }
                  }

                  this.mark++;
                  this.read_BlockPos = null;
                  this.run_Z = this.init_Z = false;
                  final AiOperation escape = this.load_AiOperation();
                  this.init_AiOperation = new AiOperation() {
                     @Override
                     public AiOperation.Result tick() {
                        AiOperation.Result res = escape.tick();
                        if (res.status() == AiOperation.Status.SUCCEEDED) {
                           for (BlockPos p : placedInRoom) {
                              if (!AiBuilder.read(load_Map.get(p.asLong()), mc.level.getBlockState(p))) {
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
               if (!this.init_AABB.inflate(1.0).contains(mc.player.position())) {
                  return false;
               }

               BlockState placed = AiBuilder.init_BlockState(cell);
               if (!placed.getCollisionShape(mc.level, target).isEmpty()
                  && !(placed.getBlock() instanceof DoorBlock)
                  && !(placed.getBlock() instanceof TrapDoorBlock)
                  && !(placed.getBlock() instanceof FenceGateBlock)) {
                  AABB site = this.init_AABB.inflate(1.0);
                  BlockPos feet = autismclient.ai.engine.AiStance.init(mc);
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
                        if (seen.add(next.asLong()) && this.poll(next)) {
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

            AiOperation load_AiOperation() {
               HashSet<Long> keep = new HashSet<>();

               for (long key : this.close_Set) {
                  BlockPos pos = BlockPos.of(key);
                  BlockState live = mc.level.getBlockState(pos);
                  if (!live.isAir()
                     && (
                        live.hasBlockEntity()
                           || !live.getFluidState().isEmpty()
                           || this.open_Set.contains(key)
                           || AiItems.requiresSilkTouchOrUnobtainable(BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString())
                           || this.mark > 2 && live.isSignalSource()
                     )) {
                     keep.add(key);
                  }
               }

               final AABB box = this.init_AABB;
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
               AutismClientAddon.LOG.info("[AI] build: closed in at {}; breaking out (try {})", mc.player.blockPosition().toShortString(), this.mark);
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

            void init(AbstractSignEditScreen editor) {
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

            boolean poll(BlockPos p) {
               BlockState state = mc.level.getBlockState(p);
               return state.getCollisionShape(mc.level, p).isEmpty()
                  || state.is(BlockTags.CLIMBABLE)
                  || state.getBlock() instanceof DoorBlock
                  || state.getBlock() instanceof TrapDoorBlock
                  || state.getBlock() instanceof FenceGateBlock;
            }

            void init_V_BlockPos_Z(BlockPos target, boolean cleanup) {
               AiBuilder.CellSpec wantedTarget = this.load_Map.get(target.asLong());
               if ((cleanup || wantedTarget != null && !AiBuilder.read(wantedTarget, mc.level.getBlockState(target)))
                  && this.load_BlockPos(target.above()) != null) {
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
                        && Block.isShapeFullBlock(AiBuilder.init_BlockState(wanted).getCollisionShape(mc.level, candidate))
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

            BlockPos load_BlockPos(BlockPos bottom) {
               BlockPos ceiling = null;
               int top = Math.min((int)this.init_AABB.maxY - 1, (int)Math.ceil(mc.player.getY()) + 1);

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
               Block block = AiBuilder.init_Block(cell);
               Set<Long> protectedNow = this.init_Set_2();
               ArrayList<Direction> directions = new ArrayList<>(
                  List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.DOWN, Direction.UP)
               );
               Direction look = this.init_Direction(cell.properties().getOrDefault("facing", ""));
               AiBuilder.OrientationKind strategy = AiBuilder.init_TableIndex_TaskQue(block, "facing");
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
                  if (AiBuilder.init_Z_TableIndex_StreamU_2(cell, block, direction)) {
                     BlockPos support = pos.relative(direction);
                     BlockState state = mc.level.getBlockState(support);
                     if (!protectedNow.contains(support.asLong())
                        && !this.set_Set.contains(support.asLong())
                        && state.canBeReplaced()
                        && state.getFluidState().isEmpty()) {
                        this.set_Set.add(support.asLong());
                        this.init_Z = true;
                        this.init_AiOperation = AiSchematicActions.init_AiOperation_BlockPos(support, pos, protectedNow, blueprint.origin().getY());
                        return true;
                     }
                  }
               }

               return false;
            }

            boolean update(AiBuilder.CellSpec cell, BlockPos pos) {
               BlockPos ceiling = pos.above();
               if (this.next_Set.contains(pos.asLong())) {
                  return false;
               }

               BlockState state = mc.level.getBlockState(ceiling);
               if (state.canBeReplaced() && state.getFluidState().isEmpty()) {
                  HashSet<Long> protectedNow = new HashSet<>(this.init_Set_2());
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

                  this.next_Set.add(pos.asLong());
                  AutismClientAddon.LOG
                     .info("[AI] build: temporary ceiling at {} for the upward-facing {} at {}", ceiling.toShortString(), cell.block(), pos.toShortString());
                  this.init_Z = true;
                  this.init_AiOperation = AiSchematicActions.init_AiOperation_BlockPos(ceiling, pos, protectedNow, blueprint.origin().getY());
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
               Set<Long> protectedNow = this.init_Set_2();
               autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.init_u_DataUtil_TableIndex_StreamU_Block(cell, AiBuilder.init_Block(cell));
               Item item = AiBuilder.init_Block(cell).asItem();
               if (item != Items.AIR && this.init(item) != 0) {
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
                                 BlockPlacementPlanner.PlacementPlan proved = autismclient.ai.engine.PlacementGoal.init(mc, pos, item, constraint, position, 48);
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
                  this.init_AiOperation = AiGoto.travelToCompat(
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
                        autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(best, 0.35), protectedNow, blueprint.origin().getY()
                     )
                  );
                  return true;
               } else {
                  return false;
               }
            }

            boolean push(BlockPos target) {
               if (schematic && this.close_Z && this.data < this.init_List.size()) {
                  BlockPos worker = autismclient.ai.engine.AiStance.init(mc);
                  if (target.getY() < worker.getY() + 2) {
                     return false;
                  }

                  int best = -1;
                  boolean fromDeferred = false;
                  double score = 1.0 / 0.0;

                  for (int pass = 0; pass < 2; pass++) {
                     List<AiBuilder.CellSpec> source = pass == 0 ? this.init_List : this.open_List;
                     int start = pass == 0 ? this.data + 1 : Math.max(0, source.size() - 192);
                     int end = pass == 0 ? Math.min(source.size(), this.data + 192) : source.size();

                     for (int i = start; i < end; i++) {
                        AiBuilder.CellSpec candidate = source.get(i);
                        BlockPos pos = blueprint.origin().offset(candidate.dx(), candidate.dy(), candidate.dz());
                        if (!pos.equals(target)
                           && pos.getY() >= worker.getY() - 1
                           && pos.getY() <= worker.getY()
                           && Math.abs(pos.getX() - worker.getX()) <= 2
                           && Math.abs(pos.getZ() - worker.getZ()) <= 2
                           && this.flush(candidate, pos)
                           && this.init_Z_TableIndex_StreamU_BlockPos(candidate, pos)
                           && !AiBuilder.init(candidate, mc.level.getBlockState(pos), power.substitutes(), true)
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
                     step = this.open_List.remove(best);
                     this.poll_List.remove(best);
                  } else {
                     step = this.init_List.remove(best);
                  }

                  this.init_List.add(this.data, step);
                  this.push_I++;
                  this.sync();
                  return true;
               } else {
                  return false;
               }
            }

            boolean load(AiBuilder.CellSpec cell) {
               if (this.data < this.init_List.size() && this.push(this.load_BlockPos())) {
                  return true;
               }

               if (this.close_Z && cell.dy() > this.frame) {
                  this.frame = cell.dy();
                  if (AiSchematicActions.init(blueprint, this.frame).isEmpty()) {
                     return false;
                  }

                  this.open_Z = false;
                  return true;
               } else {
                  return false;
               }
            }

            BlockHitResult init(AiBuilder.CellSpec cell, BlockPos pos, Block target, BlockHitResult hit) {
               this.init_F = 0.0F / 0.0F;
               this.read_D = 40.0;
               if (cell.properties().isEmpty()) {
                  return hit;
               }

               BlockPos support = hit.getBlockPos();
               Direction face = hit.getDirection();
               String axis = cell.properties().get("axis");
               Direction wantedFace = axis != null ? this.init_Direction_Str_Direction(axis, face) : null;
               String hanging = cell.properties().get("hanging");
               if (target instanceof LanternBlock && hanging != null) {
                  wantedFace = "true".equals(hanging) ? Direction.DOWN : Direction.UP;
               }

               String facingProp = cell.properties().get("facing");
               if (facingProp != null && AiBuilder.init(target)) {
                  wantedFace = Direction.byName(facingProp);
               }

               boolean pairing = target instanceof ChestBlock && AiBuilder.init_Z_TableIndex_StreamU(cell, pos, mc.level);
               if (pairing) {
                  wantedFace = AiBuilder.init_Direction(cell).getOpposite();
               }

               if (facingProp != null && target instanceof HopperBlock) {
                  Direction wanted = Direction.byName(facingProp);
                  wantedFace = wanted == Direction.DOWN ? (face.getAxis() == Axis.Y ? face : Direction.UP) : (wanted == null ? null : wanted.getOpposite());
               }

               if (facingProp != null && target instanceof TrapDoorBlock && face.getAxis().isHorizontal()) {
                  wantedFace = this.init_Direction(facingProp);
               }

               if (facingProp != null && AiBuilder.init(target, cell)) {
                  Direction wanted = this.init_Direction(facingProp);
                  if (wanted == null) {
                     this.init(cell, pos, Map.entry("facing", facingProp), "not a horizontal direction");
                  } else {
                     wantedFace = wanted;
                  }
               }

               if (wantedFace != null && face != wantedFace) {
                  BlockHitResult refaced = this.init_BlockHitResult_BlockPos_Direction(pos, wantedFace);
                  if (refaced != null) {
                     hit = refaced;
                     face = wantedFace;
                     support = refaced.getBlockPos();
                  } else {
                     this.init(
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
               if (type != null && AiBuilder.init_TableIndex_TaskQue(target, "type") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
                  high = !AiBuilder.init_Z(cell) && type.equalsIgnoreCase("top");
               }

               String half = cell.properties().get("half");
               if (half != null && AiBuilder.init_TableIndex_TaskQue(target, "half") == AiBuilder.OrientationKind.CLICK_HEIGHT) {
                  high = half.equalsIgnoreCase("top");
               }

               if (high != null) {
                  if (face == Direction.UP && high || face == Direction.DOWN && !high) {
                     this.init(
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
                  AiBuilder.OrientationKind strategy = AiBuilder.init_TableIndex_TaskQue(cell, target);
                  if (strategy == AiBuilder.OrientationKind.FACE_DERIVED
                     && !AiBuilder.init(target, cell)
                     && !(target instanceof HopperBlock)
                     && !AiBuilder.init(target)) {
                     strategy = AiBuilder.OrientationKind.PLAYER_FACING;
                  }

                  if (strategy == AiBuilder.OrientationKind.PLAYER_FACING
                     || strategy == AiBuilder.OrientationKind.PLAYER_FACING_OPPOSITE
                     || strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE
                     || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION
                     || strategy == AiBuilder.OrientationKind.PLAYER_DIRECTION_OPPOSITE) {
                     Direction wanted = Direction.byName(facingProp.toLowerCase(Locale.ROOT));
                     if (wanted == null) {
                        this.init(cell, pos, Map.entry("facing", facingProp), "not a horizontal direction");
                     } else if (wanted.getAxis().isHorizontal()) {
                        this.init_F = (strategy != AiBuilder.OrientationKind.PLAYER_FACING && strategy != AiBuilder.OrientationKind.PLAYER_DIRECTION
                              ? (strategy == AiBuilder.OrientationKind.PLAYER_FACING_CLOCKWISE ? wanted.getCounterClockWise() : wanted.getOpposite())
                              : wanted)
                           .toYRot();
                     }
                  }
               }

               autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.init_u_DataUtil_TableIndex_StreamU_Block(cell, target);
               if (constraint.yaw() != null) {
                  this.init_F = constraint.yaw();
                  this.read_D = constraint.yawTolerance();
               }

               if (target instanceof DoorBlock && cell.properties().containsKey("hinge") && facingProp != null && face.getAxis() == Axis.Y) {
                  Direction facing = this.init_Direction(facingProp);
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

            BlockHitResult init_BlockHitResult_BlockPos_Direction(BlockPos pos, Direction face) {
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

            Direction init_Direction_Str_Direction(String axis, Direction current) {
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

            Direction init_Direction(String name) {
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
               if (AiBuilder.init_Z(cell) && live.getBlock() instanceof SlabBlock && live.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                  this.load_BlockPos = null;
                  this.load_Vec3 = null;
                  this.read_TableIndex_StreamU = null;
                  this.valid = this.size = 0;
                  return false;
               }

               if (schematic && AiBuilder.flush(cell, live)) {
                  this.load_BlockPos = null;
                  this.load_Vec3 = null;
                  this.read_TableIndex_StreamU = null;
                  this.valid = this.size = 0;
                  this.read_AiOperation = AiSchematicActions.init(cell, pos, this.init_Set_2());
                  return false;
               }

               Iterator mismatch = cell.properties().entrySet().iterator();

               Property<? extends Comparable<?>> open;
               boolean cycling;
               while (true) {
                  if (!mismatch.hasNext()) {
                     String mismatchx = this.init_Str_TableIndex_StreamU(cell, pos, live);
                     if (mismatchx != null) {
                        if (schematic && this.bind < 2) {
                           this.read(mismatchx);
                        } else {
                           this.load(mismatchx);
                        }
                     }

                     return true;
                  }

                  Entry<String, String> entry = (Entry<String, String>)mismatch.next();
                  if (AiBuilder.init_TableIndex_TaskQue(this.init_Block, entry.getKey()) == AiBuilder.OrientationKind.POST_INTERACT) {
                     open = (Property<? extends Comparable<?>>)live.getBlock().getStateDefinition().getProperty(entry.getKey());
                     if (open != null && !String.valueOf(live.getValue(open)).equalsIgnoreCase(entry.getValue())) {
                        cycling = !entry.getKey().equals("open") && !entry.getKey().equals("powered");
                        if (!AiBuilder.init_Z(live.getBlock(), entry.getKey())
                           || Integer.parseInt(String.valueOf(live.getValue(open))) <= Integer.parseInt(entry.getValue())) {
                           Item needed = AiBuilder.init_Item(live.getBlock(), entry.getKey());
                           if (needed != null
                              && !mc.player.getMainHandItem().is(needed)
                              && AiItems.equipHotbar(AiItems.resolveItem(BuiltInRegistries.ITEM.getKey(needed).toString())) < 0) {
                              this.init(cell, pos, entry, "no " + BuiltInRegistries.ITEM.getKey(needed).getPath() + " to set it with");
                           } else if (cycling) {
                              if (++this.next_I <= (entry.getKey().equals("note") ? 900 : 160)) {
                                 String now = String.valueOf(live.getValue(open));
                                 if (now.equals(this.init_Str) && ++this.prev < 20) {
                                    return false;
                                 }

                                 this.prev = 0;
                                 break;
                              }

                              this.init(cell, pos, entry, "the interaction cycle did not reach the requested value");
                           } else if (this.read_Z) {
                              if (++this.stop_I <= 20) {
                                 return false;
                              }

                              this.init(cell, pos, entry, "the requested toggle was not accepted");
                           } else {
                              if (++this.stop_I <= 20) {
                                 break;
                              }

                              this.init(cell, pos, entry, "the requested toggle timed out");
                           }
                        } else {
                           this.init(cell, pos, entry, "more were placed than requested");
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

               this.load_Vec3 = visible.getLocation();
               AiGameOperations.aimAndStep(this.init_DataUtil, visible.getLocation());
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
                     this.load_Vec3 = visible.getLocation();
                  }
               }

               return false;
            }

            String init_Str_TableIndex_StreamU(AiBuilder.CellSpec cell, BlockPos pos, BlockState live) {
               for (Entry<String, String> entry : cell.properties().entrySet()) {
                  if (!AiBuilder.read(live.getBlock(), entry.getKey())
                     && (
                        !entry.getKey().equals("type")
                           || !AiBuilder.init_Z(cell, live)
                           || AiBuilder.init_Direction(cell) == null
                           || mc.level.getBlockState(pos.relative(AiBuilder.init_Direction(cell))).getBlock() instanceof ChestBlock
                     )) {
                     Property<? extends Comparable<?>> property = (Property<? extends Comparable<?>>)live.getBlock()
                        .getStateDefinition()
                        .getProperty(entry.getKey());
                     if (property == null) {
                        return "requested property is absent: " + entry.getKey();
                     }

                     String actual = String.valueOf(live.getValue(property));
                     if (!AiBuilder.init(live.getBlock(), entry.getKey(), actual, entry.getValue())) {
                        return "state mismatch for " + entry.getKey() + ": expectedState=" + entry.getValue() + ", actualState=" + actual;
                     }
                  }
               }

               return null;
            }

            void init(AiBuilder.CellSpec cell, BlockPos pos, Entry<String, String> property, String reason) {
               if (!AiBuilder.read(AiBuilder.init_Block(cell), property.getKey()) && this.sync_Set.add(property.getKey()) && this.init_JsonArray.size() < 32) {
                  this.init_JsonArray
                     .add(
                        AiJson.objectOf(
                           "x", pos.getX(), "y", pos.getY(), "z", pos.getZ(), "property", property.getKey(), "value", property.getValue(), "reason", reason
                        )
                     );
               }
            }

            BlockPos load_BlockPos() {
               AiBuilder.CellSpec cell = this.init_List.get(this.data);
               return blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
            }

            boolean flush(AiBuilder.CellSpec cell, BlockPos pos) {
               Block target = AiBuilder.init_Block(cell);
               return cell.properties().isEmpty()
                  && target.asItem() != Items.AIR
                  && !(target instanceof FallingBlock)
                  && !this.init(target)
                  && !target.defaultBlockState().hasBlockEntity()
                  && !target.defaultBlockState().isSignalSource()
                  && !autismclient.ai.engine.AiPathfinder.LiveGridView.read(target.defaultBlockState())
                  && Block.isShapeFullBlock(AiBuilder.init_BlockState(cell).getCollisionShape(mc.level, pos));
            }

            Vec3 init(Predicate<AABB> clear) {
               this.init_Predicate = clear;
               this.init_U_CachePool = null;
               AiSchematicApproach.Inner_DataUtil job = this.load_b_DataUtil != null ? this.load_b_DataUtil : this.read_b_DataUtil;
               Set<Long> excluded = new HashSet<>(this.write_Set);
               excluded.addAll(this.read_Set);
               this.init_TableIndex_SyncMan
                  .init(
                     this,
                     AiBuilder.init(this.write_List, this.init_List, this.data, blueprint.origin(), this.load_Map),
                     excluded,
                     this.init_TableIndex_StateCa.init_BlockPos()
                  );
               if (this.init_TableIndex_StreamU != null) {
                  BlockPos pos = blueprint.origin()
                     .offset(this.init_TableIndex_StreamU.dx(), this.init_TableIndex_StreamU.dy(), this.init_TableIndex_StreamU.dz());
                  if (!pos.equals(this.init_TableIndex_SyncMan.load())) {
                     this.init_TableIndex_StreamU = null;
                  }
               }

               if (this.init_TableIndex_StateCa.init_BlockPos() == null
                  && !holdSneak
                  && !this.set_Z
                  && !mc.player.isShiftKeyDown()
                  && mc.player.containerMenu == mc.player.inventoryMenu
                  && job != null) {
                  Set<Long> tried = new HashSet<>();

                  for (int scan = 0; scan < 32; scan++) {
                     BlockPos pos = this.init_TableIndex_SyncMan.init_BlockPos();
                     if (pos == null) {
                        break;
                     }

                     if (!tried.add(pos.asLong())) {
                        this.init_TableIndex_SyncMan.load(pos);
                     } else {
                        AiBuilder.CellSpec cell = this.load_Map.get(pos.asLong());
                        if (cell == null) {
                           this.init_TableIndex_SyncMan.load(pos);
                        } else {
                           this.init_TableIndex_StreamU = cell;
                           if (this.init(cell, pos, job, clear)) {
                              return this.init_U_CachePool.hit().getLocation();
                           }

                           this.init_TableIndex_SyncMan.init(pos);
                        }
                     }
                  }

                  this.init_TableIndex_StreamU = null;
                  return null;
               } else {
                  return null;
               }
            }

            boolean init(AiBuilder.CellSpec cell, BlockPos pos, AiSchematicApproach.Inner_DataUtil job, Predicate<AABB> clear) {
               if (job.read(pos)
                  && this.flush(cell, pos)
                  && AiTerrain.isLoaded(mc.level, pos)
                  && !(mc.player.getEyePosition().distanceToSqr(Vec3.atCenterOf(pos)) > Math.pow(mc.player.blockInteractionRange() - 0.4, 2.0))
                  && clear.test(new AABB(pos))
                  && mc.level.getBlockState(pos).canBeReplaced()
                  && mc.level.getBlockState(pos).getFluidState().isEmpty()
                  && AiBuilder.init_BlockState(cell).canSurvive(mc.level, pos)
                  && AiSchematicActions.init(mc.level, pos, this.load_Map) == null) {
                  Item item = AiBuilder.init_Block(cell).asItem();
                  if (this.init(item) != 0
                     && !autismclient.ai.engine.PlacementGoal.init_Z_Minecraft(mc, pos, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, AiBuilder.init_Block(cell)))) {
                     BlockPlacementPlanner.PlacementPlan candidate = autismclient.ai.engine.PlacementGoal.init(
                        mc, pos, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, AiBuilder.init_Block(cell)), 32
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

                     this.init_U_CachePool = candidate;
                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            void flush() {
               if (this.init_TableIndex_StreamU != null
                  && this.init_U_CachePool != null
                  && this.init_TableIndex_StateCa.init_BlockPos() == null
                  && this.init_Predicate != null
                  && mc.player.onGround()
                  && !(mc.player.getDeltaMovement().horizontalDistanceSqr() < 4.0E-4)) {
                  BlockPos pos = blueprint.origin()
                     .offset(this.init_TableIndex_StreamU.dx(), this.init_TableIndex_StreamU.dy(), this.init_TableIndex_StreamU.dz());
                  AiBuilder.CellSpec cell = this.load_Map.get(pos.asLong());
                  AiSchematicApproach.Inner_DataUtil job = this.load_b_DataUtil != null ? this.load_b_DataUtil : this.read_b_DataUtil;
                  if (cell != null && job != null && pos.equals(this.init_TableIndex_SyncMan.load()) && this.init(cell, pos, job, this.init_Predicate)) {
                     BlockHitResult hit = autismclient.ai.engine.AiAim.raycastLookBlock();
                     if (hit != null
                        && AiBuilder.init(
                           this.init_U_CachePool.hit().getBlockPos(),
                           this.init_U_CachePool.hit().getDirection(),
                           hit.getBlockPos(),
                           hit.getDirection()
                        )) {
                        Item item = AiBuilder.init_Block(cell).asItem();
                        if (AiBuilder.init_Z_Item(item, mc.player.getMainHandItem(), init_Z_Minecraft_Item(mc, item))
                           && BlockPlacementPlanner.blockItemValidator(new ItemStack(item), mc.player, InteractionHand.MAIN_HAND).lands(hit, pos)) {
                           BlockStatePredictionHandler prediction = ((AutismClientLevelPredictionAccessor)mc.level).autism$getBlockPredictionHandler();
                           int beforeSequence = prediction.currentSequence();
                           long revision = AiServerEvidence.blockRevision(pos);
                           mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                           this.open_I++;
                           int sentSequence = prediction.currentSequence();
                           if (sentSequence == beforeSequence) {
                              this.init_V_TableIndex_StreamU_BlockPos(cell, pos);
                           } else {
                              this.init_TableIndex_StateCa.init(pos, revision, sentSequence, mc.player.tickCount);
                              this.load_TableIndex_StreamU = cell;
                              if (AiBuilder.init_Fluid(cell) != null || cell.properties().containsKey("waterlogged")) {
                                 this.load_J = mc.player.tickCount;
                              }

                              this.init_TableIndex_SyncMan.load(pos);
                              this.init_TableIndex_StreamU = null;
                              this.init_U_CachePool = null;
                           }
                        }
                     }
                  }
               }
            }

            void close() {
               BlockPos pos = this.init_TableIndex_StateCa.init_BlockPos();
               if (pos != null) {
                  AiBuilder.CellSpec cell = this.load_TableIndex_StreamU;
                  AiBuilder.PlaceAckTracker.AckState outcome = this.init_TableIndex_StateCa
                     .init(
                        AiServerEvidence.blockRevision(pos),
                        AiBuilder.init(cell, mc.level.getBlockState(pos), power.substitutes(), true),
                        autismclient.util.villager.BlockChangeAckTracker.init_Z(mc.level, this.init_TableIndex_StateCa.init_I()),
                        mc.player.tickCount
                     );
                  if (outcome == AiBuilder.PlaceAckTracker.AckState.CONFIRMED) {
                     if (this.write_Set.add(pos.asLong())) {
                        this.state++;
                        this.close_I++;
                     }

                     AiScaffold.init_V(pos);
                     this.load_TableIndex_StreamU = null;
                     this.init_Map.remove(pos.asLong());
                  } else if (outcome == AiBuilder.PlaceAckTracker.AckState.RETRY) {
                     this.load_TableIndex_StreamU = null;
                     this.poll_I++;
                     this.init_V_TableIndex_StreamU_BlockPos(cell, pos);
                  }
               }
            }

            void init_V_TableIndex_StreamU_BlockPos(AiBuilder.CellSpec cell, BlockPos pos) {
               this.init_TableIndex_StreamU = null;
               this.init_U_CachePool = null;
               if (!AiBuilder.init_Z_Map(this.init_Map, pos, this.init_I)) {
                  this.init_TableIndex_SyncMan.init(pos);
               } else {
                  this.init_TableIndex_SyncMan.load(pos);
                  this.read_Set.add(pos.asLong());
                  int removed = AiBuilder.init(
                     this.init_List, this.open_List, this.poll_List, this.data, blueprint.origin(), cell, pos, "the server refused the moving placement"
                  );
                  if (removed == this.data) {
                     if (schematic) {
                        mc.options.keyShift.setDown(false);
                     }

                     this.sync();
                  }
               }
            }

            void load_V(BlockPos pos) {
               if (!pos.equals(this.init_BlockPos)) {
                  this.init_BlockPos = pos;
                  this.parse = 0;
                  this.load_Set.clear();
               }

               if (schematic
                  && this.data < this.init_List.size()
                  && !AiBuilder.init(this.init_List.get(this.data), pos, mc.player.getBoundingBox(), mc.level)
                  && this.init_Z_TableIndex_StreamU_BlockPos(this.init_List.get(this.data), pos)
                  && this.value < 2) {
                  this.load_U_CachePool = null;
               } else if (!schematic || !this.push(pos)) {
                  if (schematic && ++this.parse > 12) {
                     this.read("approaches end where the cell cannot be placed");
                  } else {
                     for (String id : power.candidates(this.init_List.get(this.data).block())) {
                        Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElse(null);
                        if (block != null && block.asItem() != Items.AIR && this.init(block.asItem()) > 0) {
                           if (this.bind > 0 && this.add_Set.add(pos.asLong())) {
                              BlockPos blocking = null;

                              for (int dx = -2; dx <= 2 && blocking == null; dx++) {
                                 for (int dy = -2; dy <= 2 && blocking == null; dy++) {
                                    for (int dz = -2; dz <= 2 && blocking == null; dz++) {
                                       BlockPos cell = pos.offset(dx, dy, dz);
                                       if (!this.init_AABB.contains(cell.getX() + 0.5, cell.getY() + 0.5, cell.getZ() + 0.5)
                                          && AiScaffold.init_Block(cell) != null
                                          && AiScaffold.init(this.init_Block(cell), mc.level.getBlockState(cell).getBlock())) {
                                          blocking = cell;
                                       }
                                    }
                                 }
                              }

                              if (blocking != null) {
                                 this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(blocking, 10);
                                 return;
                              }
                           }

                           AiBuilder.CellSpec cell = this.init_List.get(this.data);
                           autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint = this.init_u_DataUtil_TableIndex_StreamU_Block(cell, block);
                           if (this.pop(pos)) {
                              return;
                           }

                           BlockState occupied = mc.level.getBlockState(pos);
                           if (schematic
                              && !this.write_Z
                              && occupied.canBeReplaced()
                              && occupied.getFluidState().is(FluidTags.WATER)
                              && Block.isShapeFullBlock(AiBuilder.init_BlockState(cell).getCollisionShape(mc.level, pos))
                              && Block.isShapeFullBlock(mc.level.getBlockState(pos.below()).getCollisionShape(mc.level, pos.below()))
                              && mc.level
                                 .noCollision(
                                    mc.player,
                                    new AABB(pos.getX() + 0.2, pos.getY() + 1, pos.getZ() + 0.2, pos.getX() + 0.8, pos.getY() + 2.8, pos.getZ() + 0.8)
                                 )) {
                              this.write_Z = true;
                              this.init_AiOperation = AiGoto.travelToCompat(
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
                                    autismclient.ai.engine.AiPathfinder.PathGoal.init_ClassCache_JsonUti_BlockPos_D(pos, 0.45), this.init_Set_2(), pos.getY()
                                 )
                              );
                              return;
                           }

                           if (schematic
                              && occupied.getBlock() == block
                              && !occupied.canBeReplaced()
                              && occupied.getFluidState().isEmpty()
                              && !AiBuilder.init_Z(this.init_List.get(this.data))) {
                              this.init_AiOperation = AiGoto.approachBlockCompat(pos, 30, this.init_Set_2());
                              return;
                           }

                           if (schematic && occupied.getBlock() != block && !occupied.canBeReplaced() && occupied.getFluidState().isEmpty()) {
                              if (occupied.hasBlockEntity()) {
                                 this.load("target occupied by a block entity");
                                 return;
                              }

                              this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(pos, 30, true, this.init_Set_2(), true);
                              return;
                           }

                           if (this.close(cell, pos)) {
                              return;
                           }

                           if (schematic) {
                              Set<Long> batchKeep = AiSchematicApproach.Inner_DataUtil.init_Set_List(
                                 this.init_List, this.data, this.init_Set_2(), next -> blueprint.origin().offset(next.dx(), next.dy(), next.dz()).asLong()
                              );
                              if (this.read_b_DataUtil != null && this.read_b_DataUtil.load(pos)) {
                                 this.load_b_DataUtil = this.read_b_DataUtil;
                                 this.put_I++;
                              } else if (this.read(cell)) {
                                 this.read_b_DataUtil = null;
                                 this.write_List = List.of();
                                 this.load_b_DataUtil = this.init_b_DataUtil;
                                 this.init_b_DataUtil = null;
                                 this.run_I++;
                              } else {
                                 this.read_b_DataUtil = null;
                                 this.write_List = List.of();
                                 if (this.init_b_DataUtil != null) {
                                    this.init_b_DataUtil.init_V();
                                    this.init_b_DataUtil = null;
                                 }

                                 List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets = this.init(cell, pos, block, constraint);
                                 this.load_b_DataUtil = new AiSchematicApproach.Inner_DataUtil(
                                    targets, this.init_AABB, batchKeep, this.init_Set_TableIndex_StreamU_BlockPos(cell, pos), this.init(targets)
                                 );
                                 this.load_b_DataUtil.init_V_Set(this.close_Set);
                                 this.tick_I++;
                              }

                              this.init_AiOperation = this.load_AiOperation = this.load_b_DataUtil
                                 .init_AiOperation_I(30, batchKeep, this.init_Set_TableIndex_StreamU_BlockPos(cell, pos));
                           } else {
                              this.init_AiOperation = this.bind > 0
                                 ? AiGoto.approachToPlaceWithDigCompat(pos, block.asItem(), 30, this.init_AABB)
                                 : AiGoto.approachToPlaceCompat(pos, block.asItem(), 30, this.init_AABB);
                           }

                           return;
                        }
                     }

                     this.load("no placement material available");
                  }
               }
            }

            Set<Long> init_Set_TableIndex_StreamU_BlockPos(AiBuilder.CellSpec cell, BlockPos pos) {
               HashSet<Long> avoid = new HashSet<>(this.sync_Map.getOrDefault(pos.asLong(), Set.of()));
               avoid.addAll(this.load_Set);
               if (!AiBuilder.init_BlockState(cell).getCollisionShape(mc.level, pos).isEmpty()) {
                  avoid.add(pos.asLong());
                  avoid.add(pos.below().asLong());
               }

               return avoid;
            }

            List<autismclient.ai.engine.PlacementGoal.PlacementRequest> init(
               AiBuilder.CellSpec cell, BlockPos pos, Block block, autismclient.ai.engine.PlacementGoal.PlaceConstraint constraint
            ) {
               ArrayList<autismclient.ai.engine.PlacementGoal.PlacementRequest> batch = new ArrayList<>();
               batch.add(new autismclient.ai.engine.PlacementGoal.PlacementRequest(pos, block.asItem(), constraint, AiBuilder.init_BlockState(cell)));

               for (int j = this.data + 1; j < this.init_List.size() && j < this.data + 192; j++) {
                  AiBuilder.CellSpec next = this.init_List.get(j);
                  if (next.dy() == cell.dy() && !AiSchematicActions.init_Z(next) && !AiBuilder.init_Z(next)) {
                     Block nextBlock = AiBuilder.init_Block(next);
                     Item item = next.block().equals("minecraft:grass_block") ? Items.DIRT : nextBlock.asItem();
                     if (item != Items.AIR && this.init(item) > 0) {
                        BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                        BlockState live = mc.level.getBlockState(at);
                        if (live.canBeReplaced()
                           && live.getFluidState().isEmpty()
                           && !AiBuilder.init(next, live, power.substitutes(), schematic)
                           && AiTerrain.isLoaded(mc.level, at)
                           && AiBuilder.init_BlockState(next).canSurvive(mc.level, at)
                           && AiSchematicActions.init_BlockPos_TableIndex_StreamU(next, at, mc.level, this.load_Map) == null
                           && !this.load_Z(next, at)) {
                           boolean supported = false;

                           for (Direction direction : Direction.values()) {
                              if (AiBuilder.init_Z_TableIndex_StreamU_2(next, nextBlock, direction) && AiBuilder.init(mc.level, at.relative(direction))) {
                                 supported = true;
                              }
                           }

                           if (supported) {
                              batch.add(
                                 new autismclient.ai.engine.PlacementGoal.PlacementRequest(
                                    at, item, this.init_u_DataUtil_TableIndex_StreamU_Block(next, nextBlock), AiBuilder.init_BlockState(next)
                                 )
                              );
                           }
                        }
                     }
                  }
               }

               return AiSchematicApproach.Inner_DataUtil.init_List_List_Vec3(batch, mc.player.position());
            }

            boolean read(AiBuilder.CellSpec currentCell) {
               if (this.init_b_DataUtil == null) {
                  return false;
               }

               for (int i = this.data; i < this.init_List.size() && i < this.data + 192; i++) {
                  AiBuilder.CellSpec next = this.init_List.get(i);
                  if (next.dy() == currentCell.dy()) {
                     BlockPos at = blueprint.origin().offset(next.dx(), next.dy(), next.dz());
                     if (this.init_b_DataUtil.init_Z_BlockPos(at) && !AiBuilder.init(next, mc.level.getBlockState(at), power.substitutes(), true)) {
                        return true;
                     }
                  }
               }

               return false;
            }

            void open() {
               boolean working = this.init_AiOperation != null || this.read_AiOperation != null || this.load_BlockPos != null || this.sync_AiOperation != null;
               if (working && this.data < this.init_List.size()) {
                  if (this.init_b_DataUtil != null && this.data - this.read_I >= 192) {
                     this.init_b_DataUtil.init_V();
                     this.init_b_DataUtil = null;
                  }

                  if (this.init_b_DataUtil == null && this.read_List == null && this.write_I-- <= 0) {
                     this.read_List = new ArrayList<>();
                     this.update_I = this.sync_I = this.data;
                     this.flush_I = -2147483648;
                     this.init_Set = this.load_b_DataUtil != null
                        ? this.load_b_DataUtil.init_Set()
                        : (this.read_b_DataUtil != null ? this.read_b_DataUtil.init_Set() : Set.copyOf(this.write_List));
                  }

                  if (this.read_List != null) {
                     long stop = System.nanoTime() + 750000L;
                     int end = Math.min(this.init_List.size(), this.update_I + 192);

                     while (this.sync_I < end && System.nanoTime() < stop) {
                        AiBuilder.CellSpec next = this.init_List.get(this.sync_I++);
                        if (this.flush_I == -2147483648 || next.dy() == this.flush_I) {
                           autismclient.ai.engine.PlacementGoal.PlacementRequest target = this.init_u_CachePool_TableIndex_StreamU_Set(next, this.init_Set);
                           if (target != null && !this.read_List.stream().anyMatch(t -> t.pos().equals(target.pos()))) {
                              this.flush_I = next.dy();
                              this.read_List.add(target);
                           }
                        }
                     }

                     if (this.sync_I >= end) {
                        if (!this.read_List.isEmpty()) {
                           List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets = AiSchematicApproach.Inner_DataUtil.init_List_List_Vec3(this.read_List, this.init_Vec3_3());
                           autismclient.ai.engine.PlacementGoal.PlacementRequest first = targets.getFirst();
                           AiBuilder.CellSpec wanted = this.load_Map.get(first.pos().asLong());
                           this.init_b_DataUtil = new AiSchematicApproach.Inner_DataUtil(
                              targets,
                              this.init_AABB,
                              Collections.unmodifiableSet(this.close_Set),
                              this.init_Set_TableIndex_StreamU_BlockPos(wanted, first.pos()),
                              this.init(targets)
                           );
                           this.init_b_DataUtil.init_V_Set(this.close_Set);
                           this.read_I = this.data;
                           this.tick_I++;
                        }

                        this.read_List = null;
                        this.init_Set = Set.of();
                        this.write_I = 20;
                     }
                  }

                  if (this.init_b_DataUtil != null) {
                     if (!this.init_b_DataUtil.init_Z() && this.init_Vec3 != null && mc.player.position().distanceToSqr(this.init_Vec3) > 1.0E-4) {
                        this.add_I++;
                     }

                     this.init_b_DataUtil.init_V_Vec3(this.init_Vec3_3());
                     this.init_b_DataUtil.init_V_J(750000L);
                  }

                  this.init_Vec3 = mc.player.position();
               }
            }

            Vec3 init_Vec3_3() {
               return this.load_b_DataUtil != null
                  ? this.load_b_DataUtil.init_Vec3()
                  : (this.read_b_DataUtil != null ? this.read_b_DataUtil.init_Vec3() : mc.player.position());
            }

            BlockPos init(List<autismclient.ai.engine.PlacementGoal.PlacementRequest> targets) {
               Set<BlockPos> selected = targets.stream().map(autismclient.ai.engine.PlacementGoal.PlacementRequest::pos).collect(Collectors.toSet());
               BlockPos best = null;
               double distance = 1.0 / 0.0;

               for (int i = this.data; i < this.init_List.size() && i < this.data + 192; i++) {
                  autismclient.ai.engine.PlacementGoal.PlacementRequest next = this.init_u_CachePool_TableIndex_StreamU_Set(this.init_List.get(i), selected);
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

            autismclient.ai.engine.PlacementGoal.PlacementRequest init_u_CachePool_TableIndex_StreamU_Set(AiBuilder.CellSpec cell, Set<BlockPos> omit) {
               BlockPos at = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
               if (!omit.contains(at) && !at.equals(this.load_BlockPos) && !AiSchematicActions.init_Z(cell) && !AiBuilder.init_Z(cell) && AiTerrain.isLoaded(mc.level, at)) {
                  BlockState live = mc.level.getBlockState(at);
                  Block target = AiBuilder.init_Block(cell);
                  Item item = cell.block().equals("minecraft:grass_block") ? Items.DIRT : target.asItem();
                  return item != Items.AIR
                        && this.init(item) != 0
                        && live.canBeReplaced()
                        && live.getFluidState().isEmpty()
                        && !AiBuilder.init(cell, live, power.substitutes(), true)
                        && AiBuilder.init_BlockState(cell).canSurvive(mc.level, at)
                        && AiSchematicActions.init_BlockPos_TableIndex_StreamU(cell, at, mc.level, this.load_Map) == null
                        && !this.load_Z(cell, at)
                     ? new autismclient.ai.engine.PlacementGoal.PlacementRequest(
                        at, item, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, target), AiBuilder.init_BlockState(cell)
                     )
                     : null;
               } else {
                  return null;
               }
            }

            boolean close(AiBuilder.CellSpec cell, BlockPos pos) {
               if (schematic && !this.get_Z) {
                  AiOperation lean = AiSchematicActions.init(cell, pos, this.init_u_DataUtil_TableIndex_StreamU_Block(cell, AiBuilder.init_Block(cell)));
                  if (lean == null) {
                     return false;
                  }

                  this.get_Z = this.set_Z = this.run_Z = true;
                  this.init_AiOperation = lean;
                  return true;
               } else {
                  return false;
               }
            }

            boolean pop(BlockPos pos) {
               if (schematic && this.close_Z && !this.put_Z && !(mc.player.getY() < pos.getY() + 2) && this.load_BlockPos(pos.above()) != null) {
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

                  BlockPos roof = this.load_BlockPos(stance);
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
                        final AiOperation clear = autismclient.ai.engine.AiBreakBlock.init(plant, 15, false, digProtected, false);
                        this.init_AiOperation = new AiOperation() {
                           boolean init;

                           @Override
                           public AiOperation.Result tick() {
                              if (!this.init) {
                                 AiOperation.Result r = clear.tick();
                                 if (!r.terminal()) {
                                    return r;
                                 }

                                 clear.cancel();
                                 this.init = true;
                                 if (r.status() != AiOperation.Status.SUCCEEDED) {
                                    return r;
                                 }
                              }

                              return descent.tick();
                           }

                           @Override
                           public boolean canInterruptNow() {
                              return (this.init ? descent : clear).canInterruptNow();
                           }

                           @Override
                           public boolean requiresReconciliationAfterEmergency() {
                              return (this.init ? descent : clear).requiresReconciliationAfterEmergency();
                           }

                           @Override
                           public void cancel() {
                              clear.cancel();
                              descent.cancel();
                           }
                        };
                     } else {
                        this.init_AiOperation = descent;
                     }

                     return true;
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            }

            autismclient.ai.engine.PlacementGoal.PlaceConstraint init_u_DataUtil_TableIndex_StreamU_Block(AiBuilder.CellSpec cell, Block block) {
               return AiBuilder.init(cell, block, blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz()), mc.level);
            }

            int init(Item item) {
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

            AiOperation init_AiOperation(AiBuilder.CellSpec currentCell) {
               if (this.write_AiOperation == null && power.shulkerStock() && mc.level != null && mc.player != null) {
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
                     if (!this.update_Set.contains(key) && !this.close_Set.contains(key)) {
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
                  this.update_Set.add(source.pos().asLong());
                  int need = this.init_I_Item_TableIndex_StreamU(want, currentCell);
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
                  return AiContainer.init(args, this.init_Set_2());
               } else {
                  return null;
               }
            }

            int init_I_Item_TableIndex_StreamU(Item item, AiBuilder.CellSpec currentCell) {
               int need = 0;

               for (int i = this.data; i < this.init_List.size() && need < 576; i++) {
                  AiBuilder.CellSpec cell = this.init_List.get(i);
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
                        need += AiBuilder.init_I(cell);
                     }
                     break;
                  }
               }

               return Math.max(0, need - this.init(item));
            }

            JsonObject init_JsonObject_18() {
               JsonArray fails = new JsonArray();

               for (JsonObject failure : this.load_List) {
                  fails.add(failure);
               }

               LinkedHashSet<String> nextMaterials = new LinkedHashSet<>();
               int remaining = 0;

               for (int i = this.data; i < this.init_List.size() && remaining < 64 && nextMaterials.size() < 20; i++) {
                  AiBuilder.CellSpec cell = this.init_List.get(i);
                  BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  if (!AiBuilder.init(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     remaining++;

                     for (Item item : AiSchematicActions.load(cell, pos)) {
                        if (item != Items.AIR) {
                           nextMaterials.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }
                     }
                  }
               }

               int repairs = 0;

               for (AiBuilder.CellSpec cell : this.open_List) {
                  if (nextMaterials.size() >= 24 || repairs++ >= 64) {
                     break;
                  }

                  BlockPos pos = blueprint.origin().offset(cell.dx(), cell.dy(), cell.dz());
                  if (!AiBuilder.init(cell, mc.level.getBlockState(pos), power.substitutes(), schematic)) {
                     for (Item item : AiSchematicActions.load(cell, pos)) {
                        if (item != Items.AIR && nextMaterials.size() < 24) {
                           nextMaterials.add(BuiltInRegistries.ITEM.getKey(item).toString());
                        }
                     }
                  }
               }

               return AiJson.objectOf(
                  "placed",
                  this.state,
                  "skipped",
                  this.queue,
                  "failed",
                  fails,
                  "nextMaterials",
                  nextMaterials,
                  "shulkerRefills",
                  this.reset_I,
                  "survivalInterruptions",
                  this.level,
                  "walkingPlacements",
                  this.close_I,
                  "walkingClicks",
                  this.open_I,
                  "walkingRefusals",
                  this.poll_I,
                  "walkingPending",
                  this.init_TableIndex_StateCa.init_BlockPos() == null ? null : this.init_TableIndex_StateCa.init_BlockPos().toShortString(),
                  "permanentAccessSteps",
                  this.push_I,
                  "sourceFloorWalkingTicks",
                  this.pop_I,
                  "batchPlanning",
                  AiJson.objectOf(
                     "planned",
                     this.tick_I,
                     "prefetched",
                     this.run_I,
                     "cells",
                     this.get_I,
                     "planningMillis",
                     this.init_D,
                     "estimatedSeconds",
                     this.load_D,
                     "estimatedSupportBlocks",
                     this.clear_I,
                     "itineraryStops",
                     this.set_I,
                     "continuedStops",
                     this.put_I,
                     "nextReady",
                     this.init_b_DataUtil != null && this.init_b_DataUtil.init_Z(),
                     "overlapTicks",
                     this.add_I
                  ),
                  "currentCell",
                  this.data < this.init_List.size() ? this.init_List.get(this.data) : null,
                  "currentActual",
                  this.data < this.init_List.size() ? mc.level.getBlockState(this.load_BlockPos()).toString() : null,
                  "failedTotal",
                  this.buffer,
                  "cells",
                  this.init_List.size(),
                  "cursor",
                  this.data,
                  "unappliedProperties",
                  this.init_JsonArray,
                  "stance",
                  Boolean.getBoolean("autism.navigation.debug")
                     ? AiJson.objectOf(
                        "feet",
                        autismclient.ai.engine.AiStance.init(mc).toShortString(),
                        "onGround",
                        mc.player.onGround(),
                        "floor",
                        mc.level.getBlockState(autismclient.ai.engine.AiStance.init(mc).below()).toString(),
                        "head",
                        mc.level.getBlockState(autismclient.ai.engine.AiStance.init(mc).above()).toString()
                     )
                     : null,
                  "navigation",
                  Boolean.getBoolean("autism.navigation.debug") && this.load_Str != null ? JsonParser.parseString(this.load_Str) : null,
                  "scaffoldingRemaining",
                  this.close_Z && this.pop_Z ? this.init_List_4().size() : 0,
                  "survivalAdjustments",
                  schematic
                     ? "Decorative grass and ferns are omitted; crops are planted without required growth ages or bone meal; grass blocks are placed as dirt and existing grass or natural spread stays valid; seagrass cells are plain water. Item-placed leaves use persistent=true and distance follows adjacent leaves/logs; buried soil becomes dirt; farmland moisture may change naturally"
                     : ""
               );
            }

            JsonObject load_JsonObject() {
               JsonObject data = this.init_JsonObject_18();
               data.add(
                  "missing",
                  AiBuilder.init(
                     new AiBuilder.Blueprint(blueprint.origin(), this.init_List.subList(this.data, this.init_List.size())),
                     mc.player.getInventory(),
                     power.substitutes(),
                     schematic
                  )
               );
               return data;
            }

            JsonObject init_JsonObject(String reason) {
               JsonObject data = this.load_JsonObject();
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

            AiOperation.Result init_AiOperation_Result_5(String message) {
               return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_18());
            }

            AiOperation.Result init_AiOperation_Result_AiOperation_Status_Str_4(AiOperation.Status status, String message) {
               return this.init_AiOperation_Result_AiOperation_Status(status, message, this.init_JsonObject_18());
            }

            AiOperation.Result init_AiOperation_Result_AiOperation_Status(AiOperation.Status status, String message, JsonObject data) {
               boolean pendingEffects = this.requiresReconciliationAfterEmergency();
               this.cancel();
               return AiGuardLayer.wrapPendingTermination(pendingEffects, AiGameOperations.result(status, message, data));
            }

            @Override
            public boolean canInterruptNow() {
               if (this.init_Y != null && !this.init_Y.canInterruptNow()) {
                  return false;
               }

               if (this.load_BlockPos == null && this.init_TableIndex_StateCa.init_BlockPos() == null) {
                  for (AiOperation child : new AiOperation[]{
                     this.flush_AiOperation,
                     this.read_AiOperation,
                     this.write_AiOperation,
                     this.sync_AiOperation,
                     this.close_AiOperation,
                     this.open_AiOperation,
                     this.poll_AiOperation,
                     this.push_AiOperation,
                     this.update_AiOperation,
                     this.pop_AiOperation,
                     this.init_AiOperation
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
               if (this.init_Y != null && this.init_Y.requiresReconciliation()) {
                  return true;
               }

               if (this.load_BlockPos == null && this.init_TableIndex_StateCa.init_BlockPos() == null) {
                  for (AiOperation child : new AiOperation[]{
                     this.flush_AiOperation,
                     this.read_AiOperation,
                     this.write_AiOperation,
                     this.sync_AiOperation,
                     this.close_AiOperation,
                     this.open_AiOperation,
                     this.poll_AiOperation,
                     this.push_AiOperation,
                     this.update_AiOperation,
                     this.pop_AiOperation,
                     this.init_AiOperation
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
               this.level++;
               this.pop();
               this.push();
               this.run_Z = this.init_Z = this.add_Z = false;
               this.read_BlockPos = null;
               this.load_U_CachePool = null;
               this.start_I = 0;
               this.init_F = 0.0F / 0.0F;
               this.load_Z = this.sync_Z = this.put_Z = this.write_Z = this.get_Z = false;
               this.pop_Z = false;
               this.close_List.clear();
               this.store = 0;
               this.get_Set.clear();
               this.put_Set.clear();
               this.flush_Set.clear();
               this.stop_Set = null;
               autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
            }

            void push() {
               if (this.init_TableIndex_SyncMan.load() != null) {
                  this.init_TableIndex_SyncMan.load(this.init_TableIndex_SyncMan.load());
               }

               this.init_TableIndex_StreamU = null;
               this.init_U_CachePool = null;
               this.init_Predicate = null;
               this.init_AiOperation_Result = null;
               this.pop();
               if (this.read_AiOperation != null) {
                  this.read_AiOperation.cancel();
                  this.read_AiOperation = null;
               }

               if (this.write_AiOperation != null) {
                  this.write_AiOperation.cancel();
                  this.write_AiOperation = null;
                  if (mc.player != null && mc.player.containerMenu != mc.player.inventoryMenu) {
                     mc.player.closeContainer();
                     mc.gui.setScreen(null);
                  }
               }

               if (this.sync_AiOperation != null) {
                  this.sync_AiOperation.cancel();
                  this.sync_AiOperation = null;
               }

               if (this.close_AiOperation != null) {
                  this.close_AiOperation.cancel();
                  this.close_AiOperation = null;
               }

               if (this.open_AiOperation != null) {
                  this.open_AiOperation.cancel();
                  this.open_AiOperation = null;
               }

               if (this.poll_AiOperation != null) {
                  this.poll_AiOperation.cancel();
                  this.poll_AiOperation = null;
               }

               if (this.push_AiOperation != null) {
                  this.push_AiOperation.cancel();
                  this.push_AiOperation = null;
               }

               if (this.retryingDetached) {
                  if (this.detachedSupports != null && this.write_BlockPos != null) {
                     this.detachedSupports.add(this.write_BlockPos.asLong());
                  }

                  this.retryingDetached = false;
               }

               this.write_BlockPos = null;
               this.activeKeep = Set.of();

               if (this.update_AiOperation != null) {
                  this.update_AiOperation.cancel();
                  this.update_AiOperation = null;
               }

               if (this.pop_AiOperation != null) {
                  this.pop_AiOperation.cancel();
                  this.pop_AiOperation = null;
               }

               if (this.flush_AiOperation != null) {
                  this.flush_AiOperation.cancel();
                  this.flush_AiOperation = null;
               }

               if (this.init_AiOperation != null) {
                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
               }
            }

            void pop() {
               if (this.init_b_DataUtil != null) {
                  this.init_b_DataUtil.init_V();
               }

               if (this.load_b_DataUtil != null) {
                  this.load_b_DataUtil.init_V();
               }

               if (this.read_b_DataUtil != null) {
                  this.read_b_DataUtil.init_V();
               }

               this.init_b_DataUtil = this.load_b_DataUtil = this.read_b_DataUtil = null;
               this.load_AiOperation = null;
               this.read_List = null;
               this.init_Set = Set.of();
               this.init_Vec3 = null;
               this.write_List = List.of();
               this.write_I = 0;
            }

            @Override
            public void cancel() {
               AiVisualization.init_V(this);
               if (!this.clear_Z) {
                  if (this.init_Y != null) {
                     this.init_Y.cancel();
                  }

                  this.push();
                  if (this.init_a_StreamUtil != null) {
                     this.init_a_StreamUtil.close();
                  }

                  if (this.close_Z || !schematic) {
                     this.init_V_10();
                  }

                  this.clear_Z = true;
                  autismclient.ai.engine.AiMotionSafety.releaseMovementKeys();
                  if (mc.player != null && mc.player.getInventory().getSelectedSlot() != this.load_I) {
                     autismclient.util.InventorySlotHelper.selectHotbarSlot(mc, this.load_I);
                  }
               }
            }
         };
      } else {
         throw new IllegalArgumentException("Building requires a connected living player");
      }
   }

   private static final class FlowGraph {
      private final List<List<AiBuilder.FlowGraph.FlowEdge>> init_List = new ArrayList<>();
      private final int[] init_IArr;
      private final int[] load;

      private FlowGraph(int size) {
         this.init_IArr = new int[size];
         this.load = new int[size];

         for (int i = 0; i < size; i++) {
            this.init_List.add(new ArrayList<>());
         }
      }

      private int init(int from, int to, int capacity) {
         int index = this.init_List.get(from).size();
         this.init_List.get(from).add(new AiBuilder.FlowGraph.FlowEdge(to, this.init_List.get(to).size(), capacity));
         this.init_List.get(to).add(new AiBuilder.FlowGraph.FlowEdge(from, index, 0));
         return index;
      }

      private int init_I(int from, int index) {
         return this.init_List.get(from).get(index).read;
      }

      private void init_V(int source, int sink) {
         while (this.init_Z(source, sink)) {
            Arrays.fill(this.load, 0);

            while (this.load(source, sink, 2147483647) != 0) {
            }
         }
      }

      private boolean init_Z(int source, int sink) {
         Arrays.fill(this.init_IArr, -1);
         ArrayDeque<Integer> queue = new ArrayDeque<>();
         this.init_IArr[source] = 0;
         queue.addLast(source);

         while (!queue.isEmpty()) {
            int at = queue.removeFirst();

            for (AiBuilder.FlowGraph.FlowEdge edge : this.init_List.get(at)) {
               if (edge.read != 0 && this.init_IArr[edge.init] < 0) {
                  this.init_IArr[edge.init] = this.init_IArr[at] + 1;
                  queue.addLast(edge.init);
               }
            }
         }

         return this.init_IArr[sink] >= 0;
      }

      private int load(int at, int sink, int amount) {
         if (at == sink) {
            return amount;
         }

         for (List<AiBuilder.FlowGraph.FlowEdge> outgoing = this.init_List.get(at); this.load[at] < outgoing.size(); this.load[at]++) {
            AiBuilder.FlowGraph.FlowEdge edge = outgoing.get(this.load[at]);
            if (edge.read > 0 && this.init_IArr[edge.init] == this.init_IArr[at] + 1) {
               int pushed = this.load(edge.init, sink, Math.min(amount, edge.read));
               if (pushed > 0) {
                  edge.read -= pushed;
                  this.init_List.get(edge.init).get(edge.load).read += pushed;
                  return pushed;
               }
            }
         }

         return 0;
      }

      private static final class FlowEdge {
         private final int init;
         private final int load;
         private int read;

         private FlowEdge(int to, int reverse, int remaining) {
            this.init = to;
            this.load = reverse;
            this.read = remaining;
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
         this(rotation, mirror, layerStart, layerHeight, skipFailed, substitutes, repeat, clearTerrain, shulkerStock, signText, AiSchematicMachinePlan.init_bV);
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
      final Minecraft init_Minecraft = Minecraft.getInstance();
      final AiBuilder.Blueprint init_TableIndex_DataUti;
      final AiBuilder.BuildOptions init_TableIndex_ConfigS;
      final boolean init_Z;
      final long init_J;
      int init_I;
      int load_I;
      int read;
      int write;
      final JsonArray init_JsonArray = new JsonArray();
      AiOperation init_AiOperation;
      boolean load_Z;

      BuildOp(AiBuilder.Blueprint base, AiBuilder.BuildOptions power, boolean verify, int timeoutSeconds) {
         this.init_TableIndex_DataUti = base;
         this.init_TableIndex_ConfigS = power;
         this.init_Z = verify;
         this.init_J = System.nanoTime() + Math.max(1, Math.min(3600, timeoutSeconds)) * 1000000000L;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.load_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
            if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.isLowHealthOverrideActive()) {
               return this.init(
                  AiOperation.Status.FAILED, AiGuard.formatLowHealthAbort("building", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
               );
            }

            if (System.nanoTime() > this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, "Build deadline reached");
            }

            if (this.init_AiOperation != null) {
               AiOperation.Result r = this.init_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Building copy " + (this.init_I + 1) + " of " + this.init_Str_7());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               JsonObject legData = r.dataJson().isBlank() ? new JsonObject() : AiJson.parseObject(r.dataJson());
               this.load_I = this.load_I + AiJson.getInt(legData, "placed", 0);
               this.read = this.read + AiJson.getInt(legData, "skipped", 0);
               this.write = this.write + AiJson.getInt(legData, "failedTotal", 0);
               if (legData.has("failed") && this.init_JsonArray.size() < 32) {
                  for (JsonElement failure : legData.getAsJsonArray("failed")) {
                     if (this.init_JsonArray.size() >= 32) {
                        break;
                     }

                     this.init_JsonArray.add(failure);
                  }
               }

               if (r.status() != AiOperation.Status.SUCCEEDED) {
                  return this.init(r.status(), r.message());
               }

               this.init_I++;
            }

            AiBuilder.RepeatSpec repeat = this.init_TableIndex_ConfigS.repeat();
            if (repeat.count() >= 0 && this.init_I > repeat.count()) {
               return this.init(AiOperation.Status.SUCCEEDED, "Built " + this.init_I + " copies");
            }

            AiBuilder.Blueprint shifted = new AiBuilder.Blueprint(
               this.init_TableIndex_DataUti.origin().offset(repeat.dx() * this.init_I, repeat.dy() * this.init_I, repeat.dz() * this.init_I),
               this.init_TableIndex_DataUti.cells()
            );
            JsonObject missing = AiBuilder.init_JsonObject_TableIndex_DataUti(
               shifted, this.init_Minecraft.player.getInventory(), this.init_TableIndex_ConfigS.substitutes()
            );
            if (missing.size() > 0) {
               JsonObject data = this.init_JsonObject_19();
               data.add("missing", missing);
               return this.init_I > 0 && repeat.count() < 0
                  ? this.init(AiOperation.Status.SUCCEEDED, "Materials exhausted after " + this.init_I + " copies", data)
                  : this.init(AiOperation.Status.FAILED, "Missing materials for copy " + (this.init_I + 1) + ": " + missing, data);
            }

            int remaining = (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
            if (!AutismLiteVariant.enabled()
               && (this.init_TableIndex_ConfigS.machine().load_Z() || this.init_TableIndex_ConfigS.assumeFly())
               && !System.getProperty("autism.ai.build.schematic.engine", "printer").equalsIgnoreCase("old")) {
               this.init_AiOperation = new AiPrintOp(shifted, remaining, this.init_TableIndex_ConfigS);
            } else {
               this.init_AiOperation = AiBuilder.init(shifted, this.init_Z, remaining, this.init_TableIndex_ConfigS);
            }

            return this.init("Building copy " + (this.init_I + 1) + " of " + this.init_Str_7());
         } else {
            return this.init(AiOperation.Status.CANCELLED, "Build cancelled");
         }
      }

      String init_Str_7() {
         return this.init_TableIndex_ConfigS.repeat().count() < 0 ? "many" : String.valueOf(this.init_TableIndex_ConfigS.repeat().count() + 1);
      }

      JsonObject init_JsonObject_19() {
         AiBuilder.RepeatSpec repeat = this.init_TableIndex_ConfigS.repeat();
         return AiJson.objectOf(
            "copies",
            this.init_I,
            "placed",
            this.load_I,
            "skipped",
            this.read,
            "failed",
            this.init_JsonArray,
            "failedTotal",
            this.write,
            "cells",
            this.init_TableIndex_DataUti.cells().size(),
            "repeat",
            AiJson.objectOf("dx", repeat.dx(), "dy", repeat.dy(), "dz", repeat.dz(), "count", repeat.count())
         );
      }

      AiOperation.Result init(String message) {
         return AiGameOperations.result(AiOperation.Status.RUNNING, message, this.init_JsonObject_19());
      }

      AiOperation.Result init(AiOperation.Status status, String message) {
         return this.init(status, message, this.init_JsonObject_19());
      }

      AiOperation.Result init(AiOperation.Status status, String message, JsonObject data) {
         this.cancel();
         return AiGameOperations.result(status, message, data);
      }

      @Override
      public void cancel() {
         if (!this.load_Z) {
            this.load_Z = true;
            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }
         }
      }
   }

   record RepeatSpec(int dx, int dy, int dz, int count) {
   }

   static final class PlaceAckTracker {
      private BlockPos init_BlockPos;
      private long init_J;
      private int init_I;
      private int load;

      BlockPos init_BlockPos() {
         return this.init_BlockPos;
      }

      int init_I() {
         return this.init_I;
      }

      void init(BlockPos pos, long revision, int sequence, int sentTick) {
         if (this.init_BlockPos != null) {
            throw new IllegalStateException("A moving placement is already awaiting server evidence");
         }

         this.init_BlockPos = pos;
         this.init_J = revision;
         this.init_I = sequence;
         this.load = sentTick;
      }

      AiBuilder.PlaceAckTracker.AckState init(long currentRevision, boolean satisfied, boolean acknowledged, int nowTick) {
         if (this.init_BlockPos == null) {
            return AiBuilder.PlaceAckTracker.AckState.IDLE;
         }

         if (satisfied) {
            if (currentRevision <= this.init_J) {
               return AiBuilder.PlaceAckTracker.AckState.WAITING;
            }

            this.init_BlockPos = null;
            return AiBuilder.PlaceAckTracker.AckState.CONFIRMED;
         } else {
            if (nowTick - this.load < 4 || currentRevision <= this.init_J && !acknowledged) {
               return AiBuilder.PlaceAckTracker.AckState.WAITING;
            }

            this.init_BlockPos = null;
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
      private final ArrayDeque<BlockPos> init_ArrayDeque = new ArrayDeque<>();
      private final Set<Long> init_Set = new HashSet<>();
      private final Set<Long> load_Set = new HashSet<>();
      private Object init_Obj;
      private BlockPos init_BlockPos;
      private BlockPos load_BlockPos;

      void init(Object owner, List<BlockPos> ready, Set<Long> excluded, BlockPos pending) {
         if (this.init_Obj != owner) {
            this.init_V();
         }

         this.init_Obj = owner;
         this.load_BlockPos = pending;
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

         this.load_Set.clear();
         this.load_Set.addAll(nextEligible);
         if (this.init_BlockPos != null && (!this.load_Set.contains(this.init_BlockPos.asLong()) || this.init_BlockPos.equals(pending))) {
            this.init_BlockPos = null;
         }

         this.init_ArrayDeque.removeIf(posx -> {
            long keyx = posx.asLong();
            boolean keep = this.load_Set.contains(keyx) && !init(pending, keyx);
            if (!keep) {
               this.init_Set.remove(keyx);
            }

            return !keep;
         });

         for (BlockPos pos : unique) {
            long key = pos.asLong();
            if (!pos.equals(this.init_BlockPos) && !init(pending, key) && this.init_Set.add(key)) {
               this.init_ArrayDeque.addLast(pos);
            }
         }
      }

      private static boolean init(BlockPos pos, long key) {
         return pos != null && pos.asLong() == key;
      }

      BlockPos init_BlockPos() {
         if (this.load_BlockPos != null) {
            return null;
         }

         if (this.init_BlockPos == null) {
            this.init_BlockPos = this.init_ArrayDeque.pollFirst();
            if (this.init_BlockPos != null) {
               this.init_Set.remove(this.init_BlockPos.asLong());
            }
         }

         return this.init_BlockPos;
      }

      BlockPos load() {
         return this.init_BlockPos;
      }

      void init(BlockPos pos) {
         if (pos.equals(this.init_BlockPos)) {
            this.init_BlockPos = null;
         }

         if (pos.equals(this.load_BlockPos)) {
            this.load_BlockPos = null;
         }

         long key = pos.asLong();
         if (this.load_Set.contains(key) && this.init_Set.add(key)) {
            this.init_ArrayDeque.addLast(pos);
         }
      }

      void load(BlockPos pos) {
         if (pos.equals(this.init_BlockPos)) {
            this.init_BlockPos = null;
         }
      }

      int init_I() {
         return this.init_ArrayDeque.size();
      }

      void init_V() {
         this.init_Obj = null;
         this.init_BlockPos = this.load_BlockPos = null;
         this.init_ArrayDeque.clear();
         this.init_Set.clear();
         this.load_Set.clear();
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
