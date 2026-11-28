package autismclient.ai.engine;

import autismclient.ai.AiFailure;
import autismclient.ai.AiItems;
import autismclient.ai.AiJson;
import autismclient.api.ai.AiOperation;
import autismclient.util.KeyMappingControl;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class AiExcavate {
   private static final String init_Str = "unbreakable";
   private static final String load_Str = "no_tool";
   private static final String read_Str = "unreachable";
   private static final String write_Str = "break_failed";
   private static final String update_Str = "footing";
   private static final int init_I = 32;
   private static final long init_J = 750000L;
   private static final long load_J = 4000000000L;
   private static final int load_I = 100;
   private static final int read_I = 3;
   private static final int write_I = 32768;
   private static final int update_I = 8;
   private static final double init_D = 64.0;
   private static final double load_D = 2.25;
   private static final int sync = 20;
   private static final int flush = 12;
   private static final int[][] NEIGHBOR_OFFSETS = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

   static boolean isNonOre(BlockState state) {
      String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      return !path.endsWith("_ore") && !path.equals("ancient_debris") ? state.getBlock().defaultDestroyTime() >= 5.0F : true;
   }

   private AiExcavate() {
   }

   static List<BlockPos> areaCells(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos anchor) {
      List<BlockPos> out = new ArrayList<>();

      for (int y = maxY; y >= minY; y--) {
         List<BlockPos> layer = new ArrayList<>();

         for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
               layer.add(new BlockPos(x, y, z));
            }
         }

         int layerY = y;
         layer.sort(Comparator.comparingDouble(p -> p.distSqr(new BlockPos(anchor.getX(), layerY, anchor.getZ()))));
         out.addAll(layer);
      }

      return out;
   }

   static List<BlockPos> tunnelCells(BlockPos origin, int stepX, int stepZ, int length, int width, int height) {
      int perpX = -stepZ;
      int perpZ = stepX;
      List<BlockPos> out = new ArrayList<>();

      for (int slice = 0; slice < length; slice++) {
         List<BlockPos> cells = new ArrayList<>();

         for (int w = -(width - 1) / 2; w < -(width - 1) / 2 + width; w++) {
            for (int y = origin.getY() + height - 1; y >= origin.getY(); y--) {
               cells.add(new BlockPos(origin.getX() + slice * stepX + w * perpX, y, origin.getZ() + slice * stepZ + w * perpZ));
            }
         }

         cells.sort(Comparator.<BlockPos>comparingInt(p -> -p.getY()).thenComparingDouble(p -> p.distSqr(origin)));
         out.addAll(cells);
      }

      return out;
   }

   static List<BlockPos> staircaseCells(BlockPos origin, int stepX, int stepZ, int targetY) {
      List<BlockPos> out = new ArrayList<>();
      int steps = Math.max(1, origin.getY() - targetY);

      for (int i = 0; i <= steps; i++) {
         int x = origin.getX() + i * stepX;
         int z = origin.getZ() + i * stepZ;
         int feetY = origin.getY() - i;
         out.add(new BlockPos(x, feetY + 1, z));
         out.add(new BlockPos(x, feetY, z));
      }

      return out;
   }

   static int reachedY(int originY, int cleared) {
      return originY - Math.max(0, cleared - 1);
   }

   static int[] centredBox(int x, int y, int z, int dx, int dy, int dz) {
      int minX = x - (dx - 1) / 2;
      int minZ = z - (dz - 1) / 2;
      return new int[]{minX, y, minZ, minX + dx - 1, y + dy - 1, minZ + dz - 1};
   }

   static int[] directionStep(String name) {
      int[] var10000;
      switch (name == null ? "" : name.trim().toLowerCase(Locale.ROOT)) {
         case "north":
            var10000 = new int[]{0, -1};
            break;
         case "south":
            var10000 = new int[]{0, 1};
            break;
         case "east":
            var10000 = new int[]{1, 0};
            break;
         case "west":
            var10000 = new int[]{-1, 0};
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static int[] yawStep(float yaw) {
      int[] var10000;
      switch (Math.floorMod(Math.round(yaw / 90.0F), 4)) {
         case 0:
            var10000 = new int[]{0, 1};
            break;
         case 1:
            var10000 = new int[]{-1, 0};
            break;
         case 2:
            var10000 = new int[]{0, -1};
            break;
         default:
            var10000 = new int[]{1, 0};
      }

      return var10000;
   }

   public static boolean init(AiExcavate.Inner_BufferHelper lava, int x, int y, int z) {
      for (int[] d : NEIGHBOR_OFFSETS) {
         if (lava.test(x + d[0], y + d[1], z + d[2])) {
            return true;
         }
      }

      return false;
   }

   public static AiOperation init(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.level == null) {
         throw new IllegalArgumentException("Excavation requires a connected living player");
      }

      if (args.has("x") && args.has("y") && args.has("z")) {
         int x = AiJson.getInt(args, "x", 0);
         int y = AiJson.getInt(args, "y", 0);
         int z = AiJson.getInt(args, "z", 0);
         boolean corner2 = args.has("x2") && args.has("y2") && args.has("z2");
         boolean extents = args.has("dx") && args.has("dy") && args.has("dz");
         if (!corner2 && !extents) {
            throw new IllegalArgumentException("Supply the second corner x2,y2,z2 or extents dx,dy,dz");
         }

         int x2 = corner2 ? AiJson.getInt(args, "x2", 0) : x + Math.max(1, AiJson.getInt(args, "dx", 1)) - 1;
         int y2 = corner2 ? AiJson.getInt(args, "y2", 0) : y + Math.max(1, AiJson.getInt(args, "dy", 1)) - 1;
         int z2 = corner2 ? AiJson.getInt(args, "z2", 0) : z + Math.max(1, AiJson.getInt(args, "dz", 1)) - 1;
         int minX = Math.min(x, x2);
         int maxX = Math.max(x, x2);
         int minY = Math.min(y, y2);
         int maxY = Math.max(y, y2);
         int minZ = Math.min(z, z2);
         int maxZ = Math.max(z, z2);
         long cells = ((long)maxX - minX + 1L) * ((long)maxY - minY + 1L) * ((long)maxZ - minZ + 1L);
         if (cells > 32768L) {
            throw new IllegalArgumentException("Box volume " + cells + " exceeds 32768 cells");
         }

         boolean collect = AiJson.getBoolean(args, "collect", true);
         boolean backfill = AiJson.getBoolean(args, "backfill", false);
         int maxBlocks = Math.max(1, Math.min(4096, AiJson.getInt(args, "maxBlocks", 1024)));
         int timeoutSeconds = Math.max(5, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600)));
         return new AiExcavate.Inner_CachePool(
            "mine_area", areaCells(minX, minY, minZ, maxX, maxY, maxZ, client.player.blockPosition()), maxY - minY + 1, collect, maxBlocks, timeoutSeconds, backfill
         );
      } else {
         throw new IllegalArgumentException("Explicit first corner x,y,z required");
      }
   }

   public static AiOperation load(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         String shape = AiJson.getString(args, "shape", "");
         boolean collect = AiJson.getBoolean(args, "collect", true);
         boolean backfill = AiJson.getBoolean(args, "backfill", false);
         int timeoutSeconds = Math.max(5, Math.min(3600, AiJson.getInt(args, "timeoutSeconds", 600)));
         BlockPos origin = client.player.blockPosition();
         AiExcavate.Inner_CachePool var10000;
         switch (shape) {
            case "tunnel":
               int[] step = args.has("direction")
                  ? directionStep(AiJson.getString(args, "direction", ""))
                  : yawStep((float)AiJson.getDouble(args, "yaw", client.player.getYRot()));
               if (step == null) {
                  throw new IllegalArgumentException("direction must be north, south, east or west (or pass yaw)");
               }

               int length = Math.max(1, Math.min(128, AiJson.getInt(args, "length", 8)));
               int width = Math.max(1, Math.min(3, AiJson.getInt(args, "width", 1)));
               int height = Math.max(1, Math.min(3, AiJson.getInt(args, "height", 2)));
               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:tunnel", tunnelCells(origin, step[0], step[1], length, width, height), length, collect, length * width * height, timeoutSeconds, backfill
               );
               break;
            case "room":
               int dx = Math.max(1, Math.min(16, AiJson.getInt(args, "dx", 4)));
               int dy = Math.max(1, Math.min(16, AiJson.getInt(args, "dy", 4)));
               int dz = Math.max(1, Math.min(16, AiJson.getInt(args, "dz", 4)));
               int[] box = centredBox(origin.getX(), origin.getY(), origin.getZ(), dx, dy, dz);
               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:room", areaCells(box[0], box[1], box[2], box[3], box[4], box[5], origin), dy, collect, dx * dy * dz, timeoutSeconds, backfill
               );
               break;
            case "staircase":
               if (!args.has("targetY")) {
                  throw new IllegalArgumentException("staircase needs targetY (the Y level to descend to)");
               }

               int targetY = Math.max(client.level.getMinY(), AiJson.getInt(args, "targetY", 0));
               if (targetY >= origin.getY()) {
                  throw new IllegalArgumentException(
                     "targetY must be below the player's feet (y=" + origin.getY() + "); a staircase descends - use tunnel for level digs"
                  );
               }

               if (origin.getY() - targetY > 128) {
                  throw new IllegalArgumentException("staircase of " + (origin.getY() - targetY) + " steps exceeds 128");
               }

               int[] step2 = args.has("direction")
                  ? directionStep(AiJson.getString(args, "direction", ""))
                  : yawStep((float)AiJson.getDouble(args, "yaw", client.player.getYRot()));
               if (step2 == null) {
                  throw new IllegalArgumentException("direction must be north, south, east or west (or pass yaw)");
               }

               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:staircase",
                  staircaseCells(origin, step2[0], step2[1], targetY),
                  origin.getY() - targetY + 1,
                  collect,
                  2 * (origin.getY() - targetY + 1),
                  timeoutSeconds,
                  false,
                  new AiExcavate.Inner_ConfigStore(origin, step2[0], step2[1], targetY)
               );
               break;
            default:
               throw new IllegalArgumentException("shape must be tunnel, room or staircase");
         }

         return var10000;
      } else {
         throw new IllegalArgumentException("Excavation requires a connected living player");
      }
   }

   public interface Inner_BufferHelper {
      boolean test(int var1, int var2, int var3);
   }

   private static final class Inner_CachePool implements AiOperation {
      final Minecraft mc = Minecraft.getInstance();
      final String label;
      final List<BlockPos> order;
      final int layers;
      final boolean collect;
      final int maxBlocks;
      final long deadline;
      final int originalSlot;
      final AiExcavate.Inner_ConfigStore stair;
      AiOperation init_AiOperation;
      final Object startLevel = this.mc.level;
      final AiBackfill backfill;
      final Map<String, Integer> startCounts;
      final JsonArray skipped = new JsonArray();
      final AiExcavate.Inner_StreamUtil orderCursor;
      final AiExcavate.Inner_DataUtil init_ByteReader_DataUti = new AiExcavate.Inner_DataUtil();
      long load_J;
      AiOperation load_AiOperation;
      boolean load_Z;
      Vec3 init_Vec3;
      final BlockPos startPos = this.mc.player.blockPosition().immutable();
      final AiAim aim = new AiAim();
      final ItemStack[] hotbar = new ItemStack[9];
      int dug;
      int update_I;
      int skippedLava;
      int unusedTickCounter;
      int dropPickupTicks;
      int dropWaitTicks;
      BlockPos currentCell;
      AiOperation dropTravel;
      boolean read_Z;
      int trackedDropId = -1;
      int dropChaseTicks;
      List<ItemEntity> drops = new ArrayList<>();
      boolean write_Z;
      boolean update_Z;

      Inner_CachePool(String label, List<BlockPos> order, int layers, boolean collect, int maxBlocks, int timeoutSeconds, boolean backfill) {
         this(label, order, layers, collect, maxBlocks, timeoutSeconds, backfill, null);
      }

      Inner_CachePool(
         String label,
         List<BlockPos> order,
         int layers,
         boolean collect,
         int maxBlocks,
         int timeoutSeconds,
         boolean backfill,
         AiExcavate.Inner_ConfigStore stair
      ) {
         this.label = label;
         this.order = order;
         this.orderCursor = new AiExcavate.Inner_StreamUtil(order);
         this.layers = layers;
         this.collect = collect;
         this.maxBlocks = maxBlocks;
         this.deadline = System.nanoTime() + timeoutSeconds * 1000000000L;
         this.originalSlot = this.mc.player.getInventory().getSelectedSlot();
         this.backfill = backfill ? new AiBackfill(this.mc) : null;
         this.startCounts = this.inventoryCounts();
         this.stair = stair;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.update_Z && this.mc.player != null && this.mc.level != null) {
            if (this.mc.level != this.startLevel) {
               this.cancel();
               return AiOperation.Result.failed(AiFailure.WORLD_CHANGED, "World changed during excavation", this.statusJson().toString());
            }

            if (this.mc.player.getHealth() <= 4.0F && !AiLowHealth.isOverrideActive()) {
               this.cancel();
               return AiOperation.Result.failed(
                  AiFailure.LOW_HEALTH,
                  AiLowHealth.init("excavation", this.mc.player.getHealth(), this.mc.player.getMaxHealth()),
                  this.statusJson().toString()
               );
            }

            if (System.nanoTime() > this.deadline) {
               return this.finish(AiOperation.Status.TIMED_OUT, this.label + " deadline reached");
            }

            this.unusedTickCounter++;
            if (this.dropTravel != null) {
               AiOperation.Result r = this.dropTravel.tick();
               if (!r.terminal()) {
                  return this.running("Moving to the next cell");
               }

               this.dropTravel.cancel();
               this.dropTravel = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.running("Arrived; digging");
               }

               if (this.read_Z) {
                  this.read_Z = false;
                  if (!this.drops.isEmpty()) {
                     this.drops.remove(0);
                  }

                  return this.running("Drop unreachable; continuing");
               } else {
                  this.recordSkip(this.currentCell, "unreachable");
                  this.currentCell = null;
                  return this.running("Cell unreachable; continuing");
               }
            } else if (this.currentCell != null) {
               if (this.init_AiOperation == null) {
                  this.init_AiOperation = AiBreakBlock.breakBlock(this.currentCell, this.secondsLeft(60), AiExcavate.isNonOre(this.stateAt(this.currentCell)));
               }

               AiOperation.Result result = this.init_AiOperation.tick();
               if (!result.terminal()) {
                  return this.running("Clearing " + this.currentCell.toShortString() + ": " + result.message());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  if (result.failure() == AiFailure.TOOL_WORN) {
                     JsonObject failureData = this.statusJson();
                     failureData.addProperty("reason", "TOOL_WORN");
                     Item needed = AiItems.cheapestRequiredTool(this.stateAt(this.currentCell));
                     JsonObject missing = new JsonObject();
                     missing.addProperty(needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString(), 1);
                     failureData.add("missing", missing);
                     this.cancel();
                     return AiOperation.Result.failed(result.failure(), result.message(), failureData.toString());
                  }

                  if (this.init_ByteReader_DataUti.allowAttempt(this.currentCell, 3)) {
                     this.orderCursor.defer(this.currentCell);
                  } else {
                     if (this.stair != null) {
                        return this.fail("Could not break a staircase cell", AiFailure.NO_REACHABLE_TARGET);
                     }

                     this.recordSkip(this.currentCell, "break_failed");
                  }

                  this.currentCell = null;
                  return this.running("Break failed; continuing");
               } else {
                  this.dug++;
                  if (this.backfill != null) {
                     this.backfill.recordDugCell(this.currentCell);
                  }

                  if (this.collect) {
                     this.write_Z = true;
                     this.dropWaitTicks = 0;
                     this.collectDrops(this.currentCell);
                  }

                  this.currentCell = null;
                  return this.running("Dug " + this.dug + "/" + this.init_I_13() + " cells");
               }
            } else {
               if (this.collect && (this.write_Z || !this.drops.isEmpty())) {
                  AiOperation.Result collecting = this.init_AiOperation_Result_12();
                  if (collecting != null) {
                     return collecting;
                  }
               }

               if (this.backfill != null && !this.backfill.isIdle()) {
                  this.backfill.tick();
                  if (this.backfill.isBackfilling()) {
                     return this.running("Backfilling behind the dig");
                  }
               }

               long sweepDeadline = System.nanoTime() + 750000L;

               while (this.orderCursor.hasNext() && System.nanoTime() < sweepDeadline) {
                  if (this.dug >= this.maxBlocks) {
                     return this.completeWithBackfill("maxBlocks cap reached; cleared " + this.dug + " of " + this.init_I_13() + " cells");
                  }

                  BlockPos cell = this.orderCursor.next();
                  AiPathfinder.CellInfo probe = this.classifyCell(cell);
                  if (!probe.loaded()) {
                     this.orderCursor.defer(cell);
                  } else if (!this.stateAt(cell).isAir()) {
                     if (AiExcavate.init((x, y, z) -> this.classifyCell(new BlockPos(x, y, z)).lava(), cell.getX(), cell.getY(), cell.getZ())) {
                        if (this.stair != null) {
                           return this.fail("Lava beside the staircase", AiFailure.LAVA_DETECTED);
                        }

                        this.skippedLava++;
                     } else if (!(this.stateAt(cell).getDestroySpeed(this.mc.level, cell) < 0.0F)
                        && !this.stateAt(cell).hasBlockEntity()
                        && this.stateAt(cell).getFluidState().isEmpty()
                        && AiPathfinder.LiveGridView.isSafeToDigCompat(this.mc.level, cell)) {
                        BlockState cellState = this.stateAt(cell);
                        if (!this.mc.player.hasInfiniteMaterials()
                           && AiPathfinder.LiveGridView.lacksRequiredToolCompat(
                              cellState.requiresCorrectToolForDrops(), AiPathfinder.LiveGridView.hasCorrectToolInSlotsCompat(this.mc, cellState, 36)
                           )) {
                           if (this.stair != null) {
                              return this.fail("A block requires a harvesting tool that is not carried", AiFailure.UNBREAKABLE_OBSTACLE);
                           }

                           this.recordSkip(cell, "no_tool");
                        } else {
                           if (!cell.equals(this.mc.player.blockPosition().below()) || !this.classifyCell(cell.below(2)).passable()) {
                              this.currentCell = cell;
                              return this.running("Digging " + cell.toShortString());
                           }

                           if (this.stair != null) {
                              return this.fail("No floor under the staircase", AiFailure.NO_REACHABLE_TARGET);
                           }

                           this.recordSkip(cell, "footing");
                        }
                     } else {
                        if (this.stair != null) {
                           return this.fail("An undiggable or fluid cell blocks the staircase", AiFailure.UNBREAKABLE_OBSTACLE);
                        }

                        this.recordSkip(cell, "unbreakable");
                     }
                  }
               }

               if (this.orderCursor.hasNext()) {
                  return this.running("Scanning for the next cell");
               }

               int waiting = this.orderCursor.init_List().size();
               if (waiting > 0) {
                  if (this.orderCursor.retryDeferred(this.dug, 100)) {
                     return this.running("Re-sweeping " + waiting + " deferred cells");
                  }

                  for (BlockPos cell : this.orderCursor.init_List()) {
                     this.recordSkip(cell, "unreachable");
                  }
               }

               if (this.dug == 0 && this.update_I == 0 && this.skippedLava == 0) {
                  return this.finish(AiOperation.Status.SUCCEEDED, "Nothing to dig: every cell is already air");
               }

               if (this.update_I <= 0 && this.skippedLava <= 0) {
                  return this.completeWithBackfill("Excavation complete: dug " + this.dug + " of " + this.init_I_13() + " cells");
               }

               this.cancel();
               return AiOperation.Result.failed(AiFailure.NO_REACHABLE_TARGET, "Excavation incomplete; some cells could not be cleared", this.statusJson().toString());
            }
         } else {
            return this.finish(AiOperation.Status.CANCELLED, this.label + " cancelled");
         }
      }

      AiOperation.Result completeWithBackfill(String message) {
         if (this.backfill != null && !this.backfill.isIdle()) {
            if (!this.load_Z) {
               if (this.load_AiOperation == null) {
                  this.load_AiOperation = AiGoto.travelToCompat(this.startPos, 1.5, false, false, false, 20);
               }

               AiOperation.Result r = this.load_AiOperation.tick();
               if (!r.terminal()) {
                  return r;
               }

               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               this.load_Z = true;
            }

            if (this.load_J == 0L) {
               this.load_J = System.nanoTime() + 4000000000L;
            }

            if (System.nanoTime() < this.load_J) {
               if (this.init_Vec3 == null) {
                  double cx = 0.0;
                  double cz = 0.0;

                  for (BlockPos cell : this.order) {
                     cx += cell.getX() + 0.5;
                     cz += cell.getZ() + 0.5;
                  }

                  cx /= this.order.size();
                  cz /= this.order.size();
                  this.init_Vec3 = new Vec3(
                     2.0 * (this.startPos.getX() + 0.5) - cx, this.startPos.getY() + 1.62, 2.0 * (this.startPos.getZ() + 0.5) - cz
                  );
               }

               this.aim.aimAt(this.init_Vec3);
               this.aim.tick();
               this.backfill.tick();
               return this.running("Backfilling behind the dig");
            }
         }

         return this.finish(AiOperation.Status.SUCCEEDED, message);
      }

      AiOperation.Result init_AiOperation_Result_12() {
         if (this.drops.isEmpty()) {
            if (++this.dropWaitTicks > 12) {
               this.write_Z = false;
               return null;
            }

            if (this.backfill != null) {
               this.backfill.tick();
            }

            return this.running("Waiting for drops");
         } else {
            this.write_Z = false;
            ItemEntity drop = this.drops.get(0);
            if (!drop.isAlive()) {
               this.drops.remove(0);
               this.dropPickupTicks = 0;
               this.trackedDropId = -1;
               return this.running("Collecting drops");
            }

            if (this.trackedDropId != drop.getId()) {
               this.trackedDropId = drop.getId();
               this.dropChaseTicks = 0;
            }

            if (++this.dropChaseTicks > 120) {
               this.drops.remove(0);
               this.trackedDropId = -1;
               return this.running("Drop unreachable; continuing");
            }

            if (this.mc.player.distanceToSqr(drop) <= 2.25) {
               if (this.backfill != null) {
                  this.backfill.tick();
               }

               if (++this.dropPickupTicks > 20) {
                  this.drops.remove(0);
                  this.dropPickupTicks = 0;
               }

               return this.running("Waiting for drop pickup; check inventory space");
            } else {
               this.dropPickupTicks = 0;
               this.read_Z = true;
               this.dropTravel = AiGoto.travelToCompat(drop.blockPosition(), 1.2, true, true, true, this.secondsLeft(8));
               return this.running("Collecting drops");
            }
         }
      }

      void collectDrops(BlockPos near) {
         this.drops = new ArrayList<>();
         Vec3 center = Vec3.atCenterOf(near);

         for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
               if (this.drops.size() >= 8) {
                  break;
               }

               this.drops.add(item);
            }
         }

         this.drops.sort(Comparator.comparingDouble(this.mc.player::distanceToSqr));
         this.dropPickupTicks = 0;
      }

      AiPathfinder.CellInfo classifyCell(BlockPos pos) {
         return AiPathfinder.LiveGridView.classifyCellCompat(this.mc, this.mc.level, pos, this.hotbar);
      }

      BlockState stateAt(BlockPos pos) {
         return this.mc.level == null ? null : this.mc.level.getBlockState(pos);
      }

      void recordSkip(BlockPos cell, String reason) {
         if (this.skipped.size() < 32) {
            this.skipped.add(AiJson.objectOf("x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "reason", reason));
         }

         this.update_I++;
      }

      int init_I_13() {
         return this.order.size();
      }

      int secondsLeft(int cap) {
         return (int)Math.max(1L, Math.min(cap, (this.deadline - System.nanoTime()) / 1000000000L));
      }

      Map<String, Integer> inventoryCounts() {
         Map<String, Integer> counts = new LinkedHashMap<>();

         for (int i = 0; i < 36; i++) {
            ItemStack stack = this.mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }

         return counts;
      }

      JsonObject gainedItems() {
         JsonObject out = new JsonObject();
         this.inventoryCounts().forEach((item, count) -> {
            int delta = count - this.startCounts.getOrDefault(item, 0);
            if (delta > 0 && out.size() < 32) {
               out.addProperty(item, delta);
            }
         });
         return out;
      }

      AiOperation.Result fail(String why, AiFailure code) {
         int reachedY = AiExcavate.reachedY(this.stair.origin().getY(), this.clearedSteps());
         this.cancel();
         return AiOperation.Result.failed(
            code,
            why + "; staircase descended to y=" + reachedY + " (target " + this.stair.targetY() + ")",
            this.statusJson().toString()
         );
      }

      int clearedSteps() {
         int cleared = 0;

         for (int i = 0; i <= this.stair.steps(); i++) {
            BlockPos feet = new BlockPos(
               this.stair.origin().getX() + i * this.stair.stepX(),
               this.stair.origin().getY() - i,
               this.stair.origin().getZ() + i * this.stair.stepZ()
            );
            BlockState feetState = this.stateAt(feet);
            BlockState headState = this.stateAt(feet.above());
            if (feetState == null || headState == null || !feetState.isAir() || !headState.isAir()) {
               break;
            }

            cleared++;
         }

         return cleared;
      }

      JsonObject statusJson() {
         JsonObject out = AiJson.objectOf(
            "op",
            this.label,
            "dug",
            this.dug,
            "planned",
            this.init_I_13(),
            "layers",
            this.layers,
            "skipped",
            this.skipped,
            "skippedTotal",
            this.update_I,
            "skippedLava",
            this.skippedLava,
            "maxBlocks",
            this.maxBlocks,
            "drops",
            this.gainedItems(),
            "backfilled",
            this.backfill == null ? 0 : this.backfill.getBackfilledCount(),
            "position",
            this.mc.player == null ? null : this.mc.player.blockPosition().toShortString()
         );
         if (this.stair != null) {
            out.addProperty("targetY", this.stair.targetY());
            out.addProperty("reachedY", AiExcavate.reachedY(this.stair.origin().getY(), this.clearedSteps()));
         }

         return out;
      }

      AiOperation.Result running(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.statusJson().toString(), List.of());
      }

      AiOperation.Result finish(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.statusJson().toString(), List.of());
      }

      @Override
      public void cancel() {
         if (!this.update_Z) {
            this.update_Z = true;
            if (this.dropTravel != null) {
               this.dropTravel.cancel();
               this.dropTravel = null;
            }

            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            if (this.backfill != null) {
               this.backfill.reset();
            }

            if (this.mc.options != null) {
               for (KeyMapping key : List.of(
                  this.mc.options.keyUp,
                  this.mc.options.keyDown,
                  this.mc.options.keyLeft,
                  this.mc.options.keyRight,
                  this.mc.options.keyJump,
                  this.mc.options.keySprint,
                  this.mc.options.keyShift
               )) {
                  key.setDown(KeyMappingControl.of(key).autism$physicalForBody());
               }
            }

            if (this.mc.player != null) {
               this.mc.player.getInventory().setSelectedSlot(this.originalSlot);
            }
         }
      }
   }

   record Inner_ConfigStore(BlockPos origin, int stepX, int stepZ, int targetY) {
      int steps() {
         return Math.max(1, this.origin.getY() - this.targetY);
      }
   }

   static final class Inner_DataUtil {
      private final Map<BlockPos, Integer> attempts = new HashMap<>();

      boolean allowAttempt(BlockPos cell, int maxAttempts) {
         return this.attempts.merge(cell, 1, Integer::sum) < maxAttempts;
      }
   }

   static final class Inner_StreamUtil {
      private List<BlockPos> order;
      private final List<BlockPos> load_List = new ArrayList<>();
      private int cursor;
      private int dug = -1;
      private int dryPasses;

      Inner_StreamUtil(List<BlockPos> order) {
         this.order = order;
      }

      boolean hasNext() {
         return this.cursor < this.order.size();
      }

      BlockPos next() {
         return this.order.get(this.cursor++);
      }

      void defer(BlockPos cell) {
         this.load_List.add(cell);
      }

      List<BlockPos> init_List() {
         return this.load_List;
      }

      boolean retryDeferred(int dug, int maxDryPasses) {
         if (this.load_List.isEmpty()) {
            return false;
         }

         if (dug > this.dug) {
            this.dug = dug;
            this.dryPasses = 0;
         } else if (++this.dryPasses > maxDryPasses) {
            return false;
         }

         this.order = new ArrayList<>(this.load_List);
         this.load_List.clear();
         this.cursor = 0;
         return true;
      }
   }
}
