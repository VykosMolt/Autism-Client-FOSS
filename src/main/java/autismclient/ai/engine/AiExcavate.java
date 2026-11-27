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
   private static final int[][] init_IArr2 = new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};

   static boolean init(BlockState state) {
      String path = BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
      return !path.endsWith("_ore") && !path.equals("ancient_debris") ? state.getBlock().defaultDestroyTime() >= 5.0F : true;
   }

   private AiExcavate() {
   }

   static List<BlockPos> init(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockPos anchor) {
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

   static List<BlockPos> init(BlockPos origin, int stepX, int stepZ, int length, int width, int height) {
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

   static List<BlockPos> init(BlockPos origin, int stepX, int stepZ, int targetY) {
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

   static int init(int originY, int cleared) {
      return originY - Math.max(0, cleared - 1);
   }

   static int[] init(int x, int y, int z, int dx, int dy, int dz) {
      int minX = x - (dx - 1) / 2;
      int minZ = z - (dz - 1) / 2;
      return new int[]{minX, y, minZ, minX + dx - 1, y + dy - 1, minZ + dz - 1};
   }

   static int[] init(String name) {
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

   static int[] init(float yaw) {
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
      for (int[] d : init_IArr2) {
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
            "mine_area", init(minX, minY, minZ, maxX, maxY, maxZ, client.player.blockPosition()), maxY - minY + 1, collect, maxBlocks, timeoutSeconds, backfill
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
                  ? init(AiJson.getString(args, "direction", ""))
                  : init((float)AiJson.getDouble(args, "yaw", client.player.getYRot()));
               if (step == null) {
                  throw new IllegalArgumentException("direction must be north, south, east or west (or pass yaw)");
               }

               int length = Math.max(1, Math.min(128, AiJson.getInt(args, "length", 8)));
               int width = Math.max(1, Math.min(3, AiJson.getInt(args, "width", 1)));
               int height = Math.max(1, Math.min(3, AiJson.getInt(args, "height", 2)));
               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:tunnel", init(origin, step[0], step[1], length, width, height), length, collect, length * width * height, timeoutSeconds, backfill
               );
               break;
            case "room":
               int dx = Math.max(1, Math.min(16, AiJson.getInt(args, "dx", 4)));
               int dy = Math.max(1, Math.min(16, AiJson.getInt(args, "dy", 4)));
               int dz = Math.max(1, Math.min(16, AiJson.getInt(args, "dz", 4)));
               int[] box = init(origin.getX(), origin.getY(), origin.getZ(), dx, dy, dz);
               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:room", init(box[0], box[1], box[2], box[3], box[4], box[5], origin), dy, collect, dx * dy * dz, timeoutSeconds, backfill
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
                  ? init(AiJson.getString(args, "direction", ""))
                  : init((float)AiJson.getDouble(args, "yaw", client.player.getYRot()));
               if (step2 == null) {
                  throw new IllegalArgumentException("direction must be north, south, east or west (or pass yaw)");
               }

               var10000 = new AiExcavate.Inner_CachePool(
                  "dig:staircase",
                  init(origin, step2[0], step2[1], targetY),
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
      final Minecraft init_Minecraft = Minecraft.getInstance();
      final String init_Str;
      final List<BlockPos> init_List;
      final int init_I;
      final boolean init_Z;
      final int load_I;
      final long init_J;
      final int read_I;
      final AiExcavate.Inner_ConfigStore init_ByteReader_ConfigS;
      AiOperation init_AiOperation;
      final Object init_Obj = this.init_Minecraft.level;
      final AiBackfill init_StreamUtil;
      final Map<String, Integer> init_Map;
      final JsonArray init_JsonArray = new JsonArray();
      final AiExcavate.Inner_StreamUtil init_ByteReader_StreamU;
      final AiExcavate.Inner_DataUtil init_ByteReader_DataUti = new AiExcavate.Inner_DataUtil();
      long load_J;
      AiOperation load_AiOperation;
      boolean load_Z;
      Vec3 init_Vec3;
      final BlockPos init_BlockPos = this.init_Minecraft.player.blockPosition().immutable();
      final AiAim init_DataUtil = new AiAim();
      final ItemStack[] init_ItemStackArr = new ItemStack[9];
      int write_I;
      int update_I;
      int sync;
      int flush;
      int close;
      int open;
      BlockPos load_BlockPos;
      AiOperation read_AiOperation;
      boolean read_Z;
      int poll = -1;
      int push;
      List<ItemEntity> load_List = new ArrayList<>();
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
         this.init_Str = label;
         this.init_List = order;
         this.init_ByteReader_StreamU = new AiExcavate.Inner_StreamUtil(order);
         this.init_I = layers;
         this.init_Z = collect;
         this.load_I = maxBlocks;
         this.init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
         this.read_I = this.init_Minecraft.player.getInventory().getSelectedSlot();
         this.init_StreamUtil = backfill ? new AiBackfill(this.init_Minecraft) : null;
         this.init_Map = this.init_Map_2();
         this.init_ByteReader_ConfigS = stair;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.update_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
            if (this.init_Minecraft.level != this.init_Obj) {
               this.cancel();
               return AiOperation.Result.failed(AiFailure.WORLD_CHANGED, "World changed during excavation", this.load_JsonObject_2().toString());
            }

            if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiLowHealth.isOverrideActive()) {
               this.cancel();
               return AiOperation.Result.failed(
                  AiFailure.LOW_HEALTH,
                  AiLowHealth.init("excavation", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth()),
                  this.load_JsonObject_2().toString()
               );
            }

            if (System.nanoTime() > this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, this.init_Str + " deadline reached");
            }

            this.flush++;
            if (this.read_AiOperation != null) {
               AiOperation.Result r = this.read_AiOperation.tick();
               if (!r.terminal()) {
                  return this.load("Moving to the next cell");
               }

               this.read_AiOperation.cancel();
               this.read_AiOperation = null;
               if (r.status() == AiOperation.Status.SUCCEEDED) {
                  return this.load("Arrived; digging");
               }

               if (this.read_Z) {
                  this.read_Z = false;
                  if (!this.load_List.isEmpty()) {
                     this.load_List.remove(0);
                  }

                  return this.load("Drop unreachable; continuing");
               } else {
                  this.init(this.load_BlockPos, "unreachable");
                  this.load_BlockPos = null;
                  return this.load("Cell unreachable; continuing");
               }
            } else if (this.load_BlockPos != null) {
               if (this.init_AiOperation == null) {
                  this.init_AiOperation = AiBreakBlock.breakBlock(this.load_BlockPos, this.init(60), AiExcavate.init(this.init_BlockState(this.load_BlockPos)));
               }

               AiOperation.Result result = this.init_AiOperation.tick();
               if (!result.terminal()) {
                  return this.load("Clearing " + this.load_BlockPos.toShortString() + ": " + result.message());
               }

               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
               if (result.status() != AiOperation.Status.SUCCEEDED) {
                  if (result.failure() == AiFailure.TOOL_WORN) {
                     JsonObject failureData = this.load_JsonObject_2();
                     failureData.addProperty("reason", "TOOL_WORN");
                     Item needed = AiItems.cheapestRequiredTool(this.init_BlockState(this.load_BlockPos));
                     JsonObject missing = new JsonObject();
                     missing.addProperty(needed == null ? "minecraft:iron_pickaxe" : BuiltInRegistries.ITEM.getKey(needed).toString(), 1);
                     failureData.add("missing", missing);
                     this.cancel();
                     return AiOperation.Result.failed(result.failure(), result.message(), failureData.toString());
                  }

                  if (this.init_ByteReader_DataUti.init(this.load_BlockPos, 3)) {
                     this.init_ByteReader_StreamU.init(this.load_BlockPos);
                  } else {
                     if (this.init_ByteReader_ConfigS != null) {
                        return this.init("Could not break a staircase cell", AiFailure.NO_REACHABLE_TARGET);
                     }

                     this.init(this.load_BlockPos, "break_failed");
                  }

                  this.load_BlockPos = null;
                  return this.load("Break failed; continuing");
               } else {
                  this.write_I++;
                  if (this.init_StreamUtil != null) {
                     this.init_StreamUtil.recordDugCell(this.load_BlockPos);
                  }

                  if (this.init_Z) {
                     this.write_Z = true;
                     this.open = 0;
                     this.init_V_4(this.load_BlockPos);
                  }

                  this.load_BlockPos = null;
                  return this.load("Dug " + this.write_I + "/" + this.init_I_13() + " cells");
               }
            } else {
               if (this.init_Z && (this.write_Z || !this.load_List.isEmpty())) {
                  AiOperation.Result collecting = this.init_AiOperation_Result_12();
                  if (collecting != null) {
                     return collecting;
                  }
               }

               if (this.init_StreamUtil != null && !this.init_StreamUtil.isIdle()) {
                  this.init_StreamUtil.tick();
                  if (this.init_StreamUtil.isBackfilling()) {
                     return this.load("Backfilling behind the dig");
                  }
               }

               long sweepDeadline = System.nanoTime() + 750000L;

               while (this.init_ByteReader_StreamU.init_Z() && System.nanoTime() < sweepDeadline) {
                  if (this.write_I >= this.load_I) {
                     return this.init("maxBlocks cap reached; cleared " + this.write_I + " of " + this.init_I_13() + " cells");
                  }

                  BlockPos cell = this.init_ByteReader_StreamU.init_BlockPos();
                  AiPathfinder.CellInfo probe = this.init_ClassCache_FileCac(cell);
                  if (!probe.loaded()) {
                     this.init_ByteReader_StreamU.init(cell);
                  } else if (!this.init_BlockState(cell).isAir()) {
                     if (AiExcavate.init((x, y, z) -> this.init_ClassCache_FileCac(new BlockPos(x, y, z)).lava(), cell.getX(), cell.getY(), cell.getZ())) {
                        if (this.init_ByteReader_ConfigS != null) {
                           return this.init("Lava beside the staircase", AiFailure.LAVA_DETECTED);
                        }

                        this.sync++;
                     } else if (!(this.init_BlockState(cell).getDestroySpeed(this.init_Minecraft.level, cell) < 0.0F)
                        && !this.init_BlockState(cell).hasBlockEntity()
                        && this.init_BlockState(cell).getFluidState().isEmpty()
                        && AiPathfinder.LiveGridView.isSafeToDigCompat(this.init_Minecraft.level, cell)) {
                        BlockState cellState = this.init_BlockState(cell);
                        if (!this.init_Minecraft.player.hasInfiniteMaterials()
                           && AiPathfinder.LiveGridView.lacksRequiredToolCompat(
                              cellState.requiresCorrectToolForDrops(), AiPathfinder.LiveGridView.hasCorrectToolInSlotsCompat(this.init_Minecraft, cellState, 36)
                           )) {
                           if (this.init_ByteReader_ConfigS != null) {
                              return this.init("A block requires a harvesting tool that is not carried", AiFailure.UNBREAKABLE_OBSTACLE);
                           }

                           this.init(cell, "no_tool");
                        } else {
                           if (!cell.equals(this.init_Minecraft.player.blockPosition().below()) || !this.init_ClassCache_FileCac(cell.below(2)).passable()) {
                              this.load_BlockPos = cell;
                              return this.load("Digging " + cell.toShortString());
                           }

                           if (this.init_ByteReader_ConfigS != null) {
                              return this.init("No floor under the staircase", AiFailure.NO_REACHABLE_TARGET);
                           }

                           this.init(cell, "footing");
                        }
                     } else {
                        if (this.init_ByteReader_ConfigS != null) {
                           return this.init("An undiggable or fluid cell blocks the staircase", AiFailure.UNBREAKABLE_OBSTACLE);
                        }

                        this.init(cell, "unbreakable");
                     }
                  }
               }

               if (this.init_ByteReader_StreamU.init_Z()) {
                  return this.load("Scanning for the next cell");
               }

               int waiting = this.init_ByteReader_StreamU.init_List().size();
               if (waiting > 0) {
                  if (this.init_ByteReader_StreamU.init(this.write_I, 100)) {
                     return this.load("Re-sweeping " + waiting + " deferred cells");
                  }

                  for (BlockPos cell : this.init_ByteReader_StreamU.init_List()) {
                     this.init(cell, "unreachable");
                  }
               }

               if (this.write_I == 0 && this.update_I == 0 && this.sync == 0) {
                  return this.init(AiOperation.Status.SUCCEEDED, "Nothing to dig: every cell is already air");
               }

               if (this.update_I <= 0 && this.sync <= 0) {
                  return this.init("Excavation complete: dug " + this.write_I + " of " + this.init_I_13() + " cells");
               }

               this.cancel();
               return AiOperation.Result.failed(AiFailure.NO_REACHABLE_TARGET, "Excavation incomplete; some cells could not be cleared", this.load_JsonObject_2().toString());
            }
         } else {
            return this.init(AiOperation.Status.CANCELLED, this.init_Str + " cancelled");
         }
      }

      AiOperation.Result init(String message) {
         if (this.init_StreamUtil != null && !this.init_StreamUtil.isIdle()) {
            if (!this.load_Z) {
               if (this.load_AiOperation == null) {
                  this.load_AiOperation = AiGoto.travelToCompat(this.init_BlockPos, 1.5, false, false, false, 20);
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

                  for (BlockPos cell : this.init_List) {
                     cx += cell.getX() + 0.5;
                     cz += cell.getZ() + 0.5;
                  }

                  cx /= this.init_List.size();
                  cz /= this.init_List.size();
                  this.init_Vec3 = new Vec3(
                     2.0 * (this.init_BlockPos.getX() + 0.5) - cx, this.init_BlockPos.getY() + 1.62, 2.0 * (this.init_BlockPos.getZ() + 0.5) - cz
                  );
               }

               this.init_DataUtil.aimAt(this.init_Vec3);
               this.init_DataUtil.tick();
               this.init_StreamUtil.tick();
               return this.load("Backfilling behind the dig");
            }
         }

         return this.init(AiOperation.Status.SUCCEEDED, message);
      }

      AiOperation.Result init_AiOperation_Result_12() {
         if (this.load_List.isEmpty()) {
            if (++this.open > 12) {
               this.write_Z = false;
               return null;
            }

            if (this.init_StreamUtil != null) {
               this.init_StreamUtil.tick();
            }

            return this.load("Waiting for drops");
         } else {
            this.write_Z = false;
            ItemEntity drop = this.load_List.get(0);
            if (!drop.isAlive()) {
               this.load_List.remove(0);
               this.close = 0;
               this.poll = -1;
               return this.load("Collecting drops");
            }

            if (this.poll != drop.getId()) {
               this.poll = drop.getId();
               this.push = 0;
            }

            if (++this.push > 120) {
               this.load_List.remove(0);
               this.poll = -1;
               return this.load("Drop unreachable; continuing");
            }

            if (this.init_Minecraft.player.distanceToSqr(drop) <= 2.25) {
               if (this.init_StreamUtil != null) {
                  this.init_StreamUtil.tick();
               }

               if (++this.close > 20) {
                  this.load_List.remove(0);
                  this.close = 0;
               }

               return this.load("Waiting for drop pickup; check inventory space");
            } else {
               this.close = 0;
               this.read_Z = true;
               this.read_AiOperation = AiGoto.travelToCompat(drop.blockPosition(), 1.2, true, true, true, this.init(8));
               return this.load("Collecting drops");
            }
         }
      }

      void init_V_4(BlockPos near) {
         this.load_List = new ArrayList<>();
         Vec3 center = Vec3.atCenterOf(near);

         for (Entity entity : this.init_Minecraft.level.entitiesForRendering()) {
            if (entity instanceof ItemEntity item && item.isAlive() && !(item.position().distanceToSqr(center) > 64.0)) {
               if (this.load_List.size() >= 8) {
                  break;
               }

               this.load_List.add(item);
            }
         }

         this.load_List.sort(Comparator.comparingDouble(this.init_Minecraft.player::distanceToSqr));
         this.close = 0;
      }

      AiPathfinder.CellInfo init_ClassCache_FileCac(BlockPos pos) {
         return AiPathfinder.LiveGridView.classifyCellCompat(this.init_Minecraft, this.init_Minecraft.level, pos, this.init_ItemStackArr);
      }

      BlockState init_BlockState(BlockPos pos) {
         return this.init_Minecraft.level == null ? null : this.init_Minecraft.level.getBlockState(pos);
      }

      void init(BlockPos cell, String reason) {
         if (this.init_JsonArray.size() < 32) {
            this.init_JsonArray.add(AiJson.objectOf("x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "reason", reason));
         }

         this.update_I++;
      }

      int init_I_13() {
         return this.init_List.size();
      }

      int init(int cap) {
         return (int)Math.max(1L, Math.min(cap, (this.init_J - System.nanoTime()) / 1000000000L));
      }

      Map<String, Integer> init_Map_2() {
         Map<String, Integer> counts = new LinkedHashMap<>();

         for (int i = 0; i < 36; i++) {
            ItemStack stack = this.init_Minecraft.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
               counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
            }
         }

         return counts;
      }

      JsonObject init_JsonObject_26() {
         JsonObject out = new JsonObject();
         this.init_Map_2().forEach((item, count) -> {
            int delta = count - this.init_Map.getOrDefault(item, 0);
            if (delta > 0 && out.size() < 32) {
               out.addProperty(item, delta);
            }
         });
         return out;
      }

      AiOperation.Result init(String why, AiFailure code) {
         int reachedY = AiExcavate.init(this.init_ByteReader_ConfigS.origin().getY(), this.load_I_7());
         this.cancel();
         return AiOperation.Result.failed(
            code,
            why + "; staircase descended to y=" + reachedY + " (target " + this.init_ByteReader_ConfigS.targetY() + ")",
            this.load_JsonObject_2().toString()
         );
      }

      int load_I_7() {
         int cleared = 0;

         for (int i = 0; i <= this.init_ByteReader_ConfigS.steps(); i++) {
            BlockPos feet = new BlockPos(
               this.init_ByteReader_ConfigS.origin().getX() + i * this.init_ByteReader_ConfigS.stepX(),
               this.init_ByteReader_ConfigS.origin().getY() - i,
               this.init_ByteReader_ConfigS.origin().getZ() + i * this.init_ByteReader_ConfigS.stepZ()
            );
            BlockState feetState = this.init_BlockState(feet);
            BlockState headState = this.init_BlockState(feet.above());
            if (feetState == null || headState == null || !feetState.isAir() || !headState.isAir()) {
               break;
            }

            cleared++;
         }

         return cleared;
      }

      JsonObject load_JsonObject_2() {
         JsonObject out = AiJson.objectOf(
            "op",
            this.init_Str,
            "dug",
            this.write_I,
            "planned",
            this.init_I_13(),
            "layers",
            this.init_I,
            "skipped",
            this.init_JsonArray,
            "skippedTotal",
            this.update_I,
            "skippedLava",
            this.sync,
            "maxBlocks",
            this.load_I,
            "drops",
            this.init_JsonObject_26(),
            "backfilled",
            this.init_StreamUtil == null ? 0 : this.init_StreamUtil.getBackfilledCount(),
            "position",
            this.init_Minecraft.player == null ? null : this.init_Minecraft.player.blockPosition().toShortString()
         );
         if (this.init_ByteReader_ConfigS != null) {
            out.addProperty("targetY", this.init_ByteReader_ConfigS.targetY());
            out.addProperty("reachedY", AiExcavate.init(this.init_ByteReader_ConfigS.origin().getY(), this.load_I_7()));
         }

         return out;
      }

      AiOperation.Result load(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.load_JsonObject_2().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.load_JsonObject_2().toString(), List.of());
      }

      @Override
      public void cancel() {
         if (!this.update_Z) {
            this.update_Z = true;
            if (this.read_AiOperation != null) {
               this.read_AiOperation.cancel();
               this.read_AiOperation = null;
            }

            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
            }

            if (this.init_StreamUtil != null) {
               this.init_StreamUtil.reset();
            }

            if (this.init_Minecraft.options != null) {
               for (KeyMapping key : List.of(
                  this.init_Minecraft.options.keyUp,
                  this.init_Minecraft.options.keyDown,
                  this.init_Minecraft.options.keyLeft,
                  this.init_Minecraft.options.keyRight,
                  this.init_Minecraft.options.keyJump,
                  this.init_Minecraft.options.keySprint,
                  this.init_Minecraft.options.keyShift
               )) {
                  key.setDown(KeyMappingControl.of(key).autism$physicalForBody());
               }
            }

            if (this.init_Minecraft.player != null) {
               this.init_Minecraft.player.getInventory().setSelectedSlot(this.read_I);
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
      private final Map<BlockPos, Integer> init = new HashMap<>();

      boolean init(BlockPos cell, int maxAttempts) {
         return this.init.merge(cell, 1, Integer::sum) < maxAttempts;
      }
   }

   static final class Inner_StreamUtil {
      private List<BlockPos> init_List;
      private final List<BlockPos> load_List = new ArrayList<>();
      private int init_I;
      private int load_I = -1;
      private int read;

      Inner_StreamUtil(List<BlockPos> order) {
         this.init_List = order;
      }

      boolean init_Z() {
         return this.init_I < this.init_List.size();
      }

      BlockPos init_BlockPos() {
         return this.init_List.get(this.init_I++);
      }

      void init(BlockPos cell) {
         this.load_List.add(cell);
      }

      List<BlockPos> init_List() {
         return this.load_List;
      }

      boolean init(int dug, int maxDryPasses) {
         if (this.load_List.isEmpty()) {
            return false;
         }

         if (dug > this.load_I) {
            this.load_I = dug;
            this.read = 0;
         } else if (++this.read > maxDryPasses) {
            return false;
         }

         this.init_List = new ArrayList<>(this.load_List);
         this.load_List.clear();
         this.init_I = 0;
         return true;
      }
   }
}
