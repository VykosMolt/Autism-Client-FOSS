package autismclient.ai;

import autismclient.api.ai.AiOperation;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

final class AiFill {
   private static final int init_I = 4096;
   private static final Set<String> init_Set = Set.of("box", "walls", "shell", "sphere", "hsphere", "cylinder", "hcylinder");
   private static final Set<String> load_Set = Set.of("expand", "contract", "shift");
   private static final int load_I = 64;

   private AiFill() {
   }

   static AiFill.Inner_DataUtil init_J_DataUtil(JsonObject args) {
      if (args.has("x") && args.has("y") && args.has("z")) {
         int x = AiJson.init_I_JsonObject(args, "x", 0);
         int y = AiJson.init_I_JsonObject(args, "y", 0);
         int z = AiJson.init_I_JsonObject(args, "z", 0);
         boolean corner2 = args.has("x2") && args.has("y2") && args.has("z2");
         boolean extents = args.has("dx") && args.has("dy") && args.has("dz");
         if (!corner2 && !extents) {
            throw new IllegalArgumentException("Supply the second corner x2,y2,z2 or extents dx,dy,dz");
         }

         long x2 = corner2 ? AiJson.init_I_JsonObject(args, "x2", 0) : (long)x + Math.max(1, AiJson.init_I_JsonObject(args, "dx", 1)) - 1L;
         long y2 = corner2 ? AiJson.init_I_JsonObject(args, "y2", 0) : (long)y + Math.max(1, AiJson.init_I_JsonObject(args, "dy", 1)) - 1L;
         long z2 = corner2 ? AiJson.init_I_JsonObject(args, "z2", 0) : (long)z + Math.max(1, AiJson.init_I_JsonObject(args, "dz", 1)) - 1L;
         long ex = Math.abs(x2 - x) + 1L;
         long ey = Math.abs(y2 - y) + 1L;
         long ez = Math.abs(z2 - z) + 1L;
         if (ex <= 4096L && ey <= 4096L && ez <= 4096L) {
            if (ex * ey * ez > 4096L) {
               throw new IllegalArgumentException("Box volume " + ex * ey * ez + " exceeds 4096 cells");
            } else {
               return new AiFill.Inner_DataUtil(
                  (int)Math.min(x, x2), (int)Math.min(y, y2), (int)Math.min(z, z2), (int)Math.max(x, x2), (int)Math.max(y, y2), (int)Math.max(z, z2)
               );
            }
         } else {
            throw new IllegalArgumentException("Box axis extents " + ex + "x" + ey + "x" + ez + " exceed 4096 per axis");
         }
      } else {
         throw new IllegalArgumentException("Explicit first corner x,y,z required");
      }
   }

   static boolean init(AiFill.Inner_DataUtil box, int x, int y, int z) {
      return x == box.minX() || x == box.maxX() || y == box.minY() || y == box.maxY() || z == box.minZ() || z == box.maxZ();
   }

   static boolean init(String shape, String axis, AiFill.Inner_DataUtil box, int x, int y, int z) {
      boolean var10000;
      switch (shape) {
         case "walls":
            var10000 = x == box.minX() || x == box.maxX() || z == box.minZ() || z == box.maxZ();
            break;
         case "shell":
            var10000 = init(box, x, y, z);
            break;
         case "sphere":
            var10000 = load(box, x, y, z);
            break;
         case "hsphere":
            var10000 = load(box, x, y, z) && read(box, x, y, z);
            break;
         case "cylinder":
            var10000 = init(axis, box, x, y, z);
            break;
         case "hcylinder":
            var10000 = init(axis, box, x, y, z) && load(axis, box, x, y, z);
            break;
         default:
            var10000 = true;
      }

      return var10000;
   }

   private static boolean load(AiFill.Inner_DataUtil box, int x, int y, int z) {
      double sx = box.maxX() - box.minX() + 1;
      double sy = box.maxY() - box.minY() + 1;
      double sz = box.maxZ() - box.minZ() + 1;
      double dx = (x - (box.minX() + box.maxX()) / 2.0) / (sx / 2.0);
      double dy = (y - (box.minY() + box.maxY()) / 2.0) / (sy / 2.0);
      double dz = (z - (box.minZ() + box.maxZ()) / 2.0) / (sz / 2.0);
      return dx * dx + dy * dy + dz * dz <= 1.000000001;
   }

   private static boolean read(AiFill.Inner_DataUtil box, int x, int y, int z) {
      return !load(box, x + 1, y, z)
         || !load(box, x - 1, y, z)
         || !load(box, x, y + 1, z)
         || !load(box, x, y - 1, z)
         || !load(box, x, y, z + 1)
         || !load(box, x, y, z - 1);
   }

   private static boolean init(String axis, AiFill.Inner_DataUtil box, int x, int y, int z) {
      boolean var10000;
      switch (axis) {
         case "x":
            var10000 = init(box.minY(), box.maxY(), box.minZ(), box.maxZ(), y, z);
            break;
         case "z":
            var10000 = init(box.minX(), box.maxX(), box.minY(), box.maxY(), x, y);
            break;
         default:
            var10000 = init(box.minX(), box.maxX(), box.minZ(), box.maxZ(), x, z);
      }

      return var10000;
   }

   private static boolean load(String axis, AiFill.Inner_DataUtil box, int x, int y, int z) {
      boolean var10000;
      switch (axis) {
         case "x":
            var10000 = !init(axis, box, x, y + 1, z) || !init(axis, box, x, y - 1, z) || !init(axis, box, x, y, z + 1) || !init(axis, box, x, y, z - 1);
            break;
         case "z":
            var10000 = !init(axis, box, x + 1, y, z) || !init(axis, box, x - 1, y, z) || !init(axis, box, x, y + 1, z) || !init(axis, box, x, y - 1, z);
            break;
         default:
            var10000 = !init(axis, box, x + 1, y, z) || !init(axis, box, x - 1, y, z) || !init(axis, box, x, y, z + 1) || !init(axis, box, x, y, z - 1);
      }

      return var10000;
   }

   private static boolean init(int minA, int maxA, int minB, int maxB, int a, int b) {
      double sa = maxA - minA + 1;
      double sb = maxB - minB + 1;
      double da = (a - (minA + maxA) / 2.0) / (sa / 2.0);
      double db = (b - (minB + maxB) / 2.0) / (sb / 2.0);
      return da * da + db * db <= 1.000000001;
   }

   static List<BlockPos> init(AiFill.Inner_DataUtil box, boolean hollow) {
      return init(box, hollow ? "shell" : "box", "y");
   }

   static List<BlockPos> init(AiFill.Inner_DataUtil box, String shape, String axis) {
      List<BlockPos> out = new ArrayList<>();
      int centerX = (box.minX() + box.maxX()) / 2;
      int centerZ = (box.minZ() + box.maxZ()) / 2;

      for (int y = box.minY(); y <= box.maxY(); y++) {
         List<BlockPos> layer = new ArrayList<>();

         for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
               if (init(shape, axis, box, x, y, z)) {
                  layer.add(new BlockPos(x, y, z));
               }
            }
         }

         int layerY = y;
         layer.sort(Comparator.comparingDouble(p -> p.distSqr(new BlockPos(centerX, layerY, centerZ))));
         out.addAll(layer);
      }

      return out;
   }

   static boolean init_Z(String from, String blockId) {
      return !from.equalsIgnoreCase("any")
         ? autismclient.ai.engine.AiMine.init(from, blockId)
         : !blockId.equals("minecraft:air") && !blockId.equals("minecraft:cave_air") && !blockId.equals("minecraft:void_air");
   }

   static AiOperation init_AiOperation(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         AiFill.Inner_DataUtil box = init_J_DataUtil(args);
         if (box.volume() > 4096L) {
            throw new IllegalArgumentException("Box volume " + box.volume() + " exceeds 4096 cells");
         }

         Block block = init_Block(AiJson.init_Str_JsonObject(args, "block", ""), "block");
         Map<String, String> properties = AiBuilder.init_Map_JsonObject(args, "fill", block);
         String shape = AiJson.init_Str_JsonObject(args, "shape", AiJson.init_Z_JsonObject(args, "hollow", false) ? "shell" : "box").trim().toLowerCase(Locale.ROOT);
         if (!init_Set.contains(shape)) {
            throw new IllegalArgumentException("shape must be one of " + String.join(", ", init_Set) + ", got \"" + shape + "\"");
         }

         String axis = AiJson.init_Str_JsonObject(args, "axis", "y").trim().toLowerCase(Locale.ROOT);
         if (!axis.equals("x") && !axis.equals("y") && !axis.equals("z")) {
            throw new IllegalArgumentException("axis must be x, y or z, got \"" + axis + "\"");
         }

         int timeoutSeconds = Math.max(5, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 600)));
         Minecraft mc = Minecraft.getInstance();
         String blockId = BuiltInRegistries.BLOCK.getKey(block).toString();
         List<AiBuilder.CellSpec> cells = new ArrayList<>();

         for (BlockPos pos : init(box, shape, axis)) {
            AiBuilder.CellSpec cell = new AiBuilder.CellSpec(
               pos.getX() - box.minX(), pos.getY() - box.minY(), pos.getZ() - box.minZ(), blockId, properties
            );
            if (!AiBuilder.write(cell, mc.level.getBlockState(pos))) {
               cells.add(cell);
            }
         }

         AiBuilder.Blueprint blueprint = new AiBuilder.Blueprint(new BlockPos(box.minX(), box.minY(), box.minZ()), cells);
         JsonObject missing = AiBuilder.init(blueprint, mc.player.getInventory());
         return missing.size() > 0
            ? AiGameOperations.init(
               AiOperation.Status.FAILED,
               AiFailure.MISSING_MATERIALS,
               "Missing materials: " + missing,
               AiJson.init_JsonObject_ObjArr("missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray())
            )
            : AiBuilder.init_AiOperation_TableIndex_DataUti(blueprint, true, timeoutSeconds);
      } else {
         throw new IllegalArgumentException("Filling requires a connected living player");
      }
   }

   static AiOperation load(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         AiFill.Inner_DataUtil box = init_J_DataUtil(args);
         if (box.volume() > 4096L) {
            throw new IllegalArgumentException("Box volume " + box.volume() + " exceeds 4096 cells");
         }

         String name = AiClipboard.init_Str(AiJson.init_Str_JsonObject(args, "name", ""));
         List<AiBuilder.CellSpec> cells = init(box, Minecraft.getInstance().level);
         AiClipboard.init(name, List.copyOf(cells));
         return AiGameOperations.init_AiOperation(
            AiOperation.Status.SUCCEEDED,
            "Copied " + cells.size() + " cells to clipboard \"" + name + "\"",
            AiJson.init_JsonObject_ObjArr("name", name, "cells", cells.size(), "persisted", true)
         );
      } else {
         throw new IllegalArgumentException("Copying requires a connected living player");
      }
   }

   private static List<AiBuilder.CellSpec> init(AiFill.Inner_DataUtil box, ClientLevel level) {
      List<AiBuilder.CellSpec> cells = new ArrayList<>();
      int unloaded = 0;

      for (int y = box.minY(); y <= box.maxY(); y++) {
         for (int z = box.minZ(); z <= box.maxZ(); z++) {
            for (int x = box.minX(); x <= box.maxX(); x++) {
               BlockPos pos = new BlockPos(x, y, z);
               if (!AiTerrain.init(level, pos)) {
                  unloaded++;
               } else {
                  BlockState state = level.getBlockState(pos);
                  LinkedHashMap<String, String> properties = new LinkedHashMap<>();

                  for (Property<?> property : state.getProperties()) {
                     properties.put(property.getName(), String.valueOf(state.getValue(property)).toLowerCase(Locale.ROOT));
                  }

                  cells.add(
                     new AiBuilder.CellSpec(
                        x - box.minX(), y - box.minY(), z - box.minZ(), BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), Map.copyOf(properties)
                     )
                  );
               }
            }
         }
      }

      if (unloaded > 0) {
         throw new IllegalArgumentException("The box reaches into unloaded terrain (" + unloaded + " cells); copy only loaded, observed boxes");
      } else {
         return cells;
      }
   }

   static AiOperation read(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         String name = AiClipboard.init_Str(AiJson.init_Str_JsonObject(args, "name", ""));
         List<AiBuilder.CellSpec> stored = AiClipboard.init_List(name);
         AiBuilder.BuildOptions power = AiBuilder.init_TableIndex_ConfigS(args);
         if (power.repeat() == null && !power.layers() && power.substitutes().isEmpty()) {
            boolean air = AiJson.init_Z_JsonObject(args, "air", false);
            Minecraft mc = Minecraft.getInstance();
            BlockPos origin = mc.player.blockPosition();
            if (args.has("x") || args.has("y") || args.has("z")) {
               origin = AiGameOperations.init_BlockPos(args);
            }

            List<AiBuilder.CellSpec> transformed = AiBuilder.init(AiBuilder.init_List_List_2(stored, power.rotation(), power.mirror()), 4096);
            List<AiBuilder.CellSpec> place = new ArrayList<>();
            List<BlockPos> clearCells = new ArrayList<>();

            for (AiBuilder.CellSpec cell : transformed) {
               boolean airCell = cell.block().equals("minecraft:air") || cell.block().equals("minecraft:cave_air") || cell.block().equals("minecraft:void_air");
               if (!airCell) {
                  place.add(cell);
               } else if (air) {
                  BlockPos pos = origin.offset(cell.dx(), cell.dy(), cell.dz());
                  if (AiTerrain.init(mc.level, pos) && !mc.level.getBlockState(pos).isAir()) {
                     clearCells.add(pos);
                  }
               }
            }

            AiBuilder.Blueprint blueprint = new AiBuilder.Blueprint(origin, List.copyOf(place));
            JsonObject missing = AiBuilder.init(blueprint, mc.player.getInventory());
            if (missing.size() > 0) {
               return AiGameOperations.init(
                  AiOperation.Status.FAILED,
                  AiFailure.MISSING_MATERIALS,
                  "Missing materials: " + missing,
                  AiJson.init_JsonObject_ObjArr("missing", missing, "placed", 0, "skipped", 0, "failed", new JsonArray())
               );
            }

            int timeoutSeconds = Math.max(5, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 600)));
            boolean verify = AiJson.init_Z_JsonObject(args, "verify", true);
            return !clearCells.isEmpty()
               ? new AiFill.Inner_BufferHelper(clearCells, blueprint, timeoutSeconds)
               : AiBuilder.init_AiOperation_TableIndex_DataUti(blueprint, verify, timeoutSeconds);
         } else {
            throw new IllegalArgumentException("game:paste supports rotation and mirror only; repeat/layers/substitutes belong to game:build");
         }
      } else {
         throw new IllegalArgumentException("Pasting requires a connected living player");
      }
   }

   static int[] init(String name) {
      int[] var10000;
      switch (name == null ? "" : name.trim().toLowerCase(Locale.ROOT)) {
         case "north":
            var10000 = new int[]{0, 0, -1};
            break;
         case "south":
            var10000 = new int[]{0, 0, 1};
            break;
         case "east":
            var10000 = new int[]{1, 0, 0};
            break;
         case "west":
            var10000 = new int[]{-1, 0, 0};
            break;
         case "up":
            var10000 = new int[]{0, 1, 0};
            break;
         case "down":
            var10000 = new int[]{0, -1, 0};
            break;
         default:
            var10000 = null;
      }

      return var10000;
   }

   static AiFill.Inner_StreamUtil init(AiFill.Inner_DataUtil box, String action, int[] step, int amount, int minY, int maxY) {
      int minX = box.minX();
      int lo = box.minY();
      int minZ = box.minZ();
      int maxX = box.maxX();
      int hi = box.maxY();
      int maxZ = box.maxZ();
      boolean clamped = false;
      switch (action) {
         case "expand":
            if (step[0] > 0) {
               maxX += amount;
            } else if (step[0] < 0) {
               minX -= amount;
            }

            if (step[2] > 0) {
               maxZ += amount;
            } else if (step[2] < 0) {
               minZ -= amount;
            }

            if (step[1] > 0) {
               if (hi + amount > maxY) {
                  clamped = true;
               }

               hi = Math.min(hi + amount, maxY);
            } else if (step[1] < 0) {
               if (lo - amount < minY) {
                  clamped = true;
               }

               lo = Math.max(lo - amount, minY);
            }
            break;
         case "contract":
            if (step[0] > 0) {
               if (maxX - amount < minX) {
                  clamped = true;
               }

               maxX = Math.max(maxX - amount, minX);
            } else if (step[0] < 0) {
               if (minX + amount > maxX) {
                  clamped = true;
               }

               minX = Math.min(minX + amount, maxX);
            }

            if (step[2] > 0) {
               if (maxZ - amount < minZ) {
                  clamped = true;
               }

               maxZ = Math.max(maxZ - amount, minZ);
            } else if (step[2] < 0) {
               if (minZ + amount > maxZ) {
                  clamped = true;
               }

               minZ = Math.min(minZ + amount, maxZ);
            }

            if (step[1] > 0) {
               if (hi - amount < lo) {
                  clamped = true;
               }

               hi = Math.max(hi - amount, lo);
            } else if (step[1] < 0) {
               if (lo + amount > hi) {
                  clamped = true;
               }

               lo = Math.min(lo + amount, hi);
            }
            break;
         default:
            minX += step[0] * amount;
            maxX += step[0] * amount;
            minZ += step[2] * amount;
            maxZ += step[2] * amount;
            if (step[1] != 0) {
               int dy = Math.max(minY - lo, Math.min(maxY - hi, step[1] * amount));
               if (dy != step[1] * amount) {
                  clamped = true;
               }

               lo += dy;
               hi += dy;
            }
      }

      return new AiFill.Inner_StreamUtil(new AiFill.Inner_DataUtil(minX, lo, minZ, maxX, hi, maxZ), clamped);
   }

   static AiFill.Inner_CachePool init(List<AiBuilder.CellSpec> cells, String action, int[] step, int amount, int maxCells) {
      int minX = 2147483647;
      int minY = 2147483647;
      int minZ = 2147483647;
      int maxX = -2147483648;
      int maxY = -2147483648;
      int maxZ = -2147483648;

      for (AiBuilder.CellSpec cell : cells) {
         minX = Math.min(minX, cell.dx());
         maxX = Math.max(maxX, cell.dx());
         minY = Math.min(minY, cell.dy());
         maxY = Math.max(maxY, cell.dy());
         minZ = Math.min(minZ, cell.dz());
         maxZ = Math.max(maxZ, cell.dz());
      }

      if (cells.isEmpty()) {
         throw new IllegalArgumentException("The clipboard is empty; nothing to transform");
      }

      List<AiBuilder.CellSpec> out = new ArrayList<>(cells);
      boolean clamped = false;
      if (action.equals("expand")) {
         List<AiBuilder.CellSpec> grown = new ArrayList<>();

         for (AiBuilder.CellSpec cell : cells) {
            if (init(cell, step, minX, minY, minZ, maxX, maxY, maxZ)) {
               for (int i = 1; i <= amount; i++) {
                  grown.add(
                     new AiBuilder.CellSpec(cell.dx() + step[0] * i, cell.dy() + step[1] * i, cell.dz() + step[2] * i, cell.block(), cell.properties())
                  );
               }
            }
         }

         out.addAll(grown);
      } else {
         int extent = step[0] != 0 ? maxX - minX + 1 : (step[1] != 0 ? maxY - minY + 1 : maxZ - minZ + 1);
         int drop = amount;
         if (drop >= extent) {
            clamped = true;
            drop = extent - 1;
         }

         int layers = drop;
         int loX = minX;
         int loY = minY;
         int loZ = minZ;
         int hiX = maxX;
         int hiY = maxY;
         int hiZ = maxZ;
         out.removeIf(cell -> init(cell, step, layers, loX, loY, loZ, hiX, hiY, hiZ));
      }

      return new AiFill.Inner_CachePool(AiBuilder.init(out, maxCells), clamped);
   }

   private static boolean init(AiBuilder.CellSpec cell, int[] step, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      if (step[0] > 0) {
         return cell.dx() == maxX;
      } else if (step[0] < 0) {
         return cell.dx() == minX;
      } else if (step[1] > 0) {
         return cell.dy() == maxY;
      } else if (step[1] < 0) {
         return cell.dy() == minY;
      } else if (step[2] > 0) {
         return cell.dz() == maxZ;
      } else {
         return step[2] < 0 ? cell.dz() == minZ : false;
      }
   }

   private static boolean init(AiBuilder.CellSpec cell, int[] step, int layers, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      if (step[0] > 0) {
         return cell.dx() > maxX - layers;
      } else if (step[0] < 0) {
         return cell.dx() < minX + layers;
      } else if (step[1] > 0) {
         return cell.dy() > maxY - layers;
      } else if (step[1] < 0) {
         return cell.dy() < minY + layers;
      } else if (step[2] > 0) {
         return cell.dz() > maxZ - layers;
      } else {
         return step[2] < 0 ? cell.dz() < minZ + layers : false;
      }
   }

   private static int[] init(List<AiBuilder.CellSpec> cells) {
      int minX = 0;
      int minY = 0;
      int minZ = 0;
      int maxX = 0;
      int maxY = 0;
      int maxZ = 0;
      boolean first = true;

      for (AiBuilder.CellSpec cell : cells) {
         if (first) {
            minX = maxX = cell.dx();
            minY = maxY = cell.dy();
            minZ = maxZ = cell.dz();
            first = false;
         } else {
            minX = Math.min(minX, cell.dx());
            maxX = Math.max(maxX, cell.dx());
            minY = Math.min(minY, cell.dy());
            maxY = Math.max(maxY, cell.dy());
            minZ = Math.min(minZ, cell.dz());
            maxZ = Math.max(maxZ, cell.dz());
         }
      }

      return new int[]{maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1};
   }

   static AiOperation write(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         String action = AiJson.init_Str_JsonObject(args, "action", "").trim().toLowerCase(Locale.ROOT);
         if (!load_Set.contains(action)) {
            throw new IllegalArgumentException("action must be one of " + String.join(", ", load_Set) + ", got \"" + action + "\"");
         }

         int[] step = init(AiJson.init_Str_JsonObject(args, "direction", ""));
         if (step == null) {
            throw new IllegalArgumentException(
               "direction must be north, south, east, west, up or down, got \"" + AiJson.init_Str_JsonObject(args, "direction", "") + "\""
            );
         }

         int amount = AiJson.init_I_JsonObject(args, "amount", 1);
         if (amount >= 1 && amount <= 64) {
            String clipboard = AiJson.init_Str_JsonObject(args, "clipboard", "").trim();
            boolean hasBox = args.has("x") && args.has("y") && args.has("z");
            if (hasBox == !clipboard.isEmpty()) {
               throw new IllegalArgumentException("Supply a box (x,y,z + x2,y2,z2 or dx,dy,dz) or clipboard:\"name\", not both");
            }

            int minY = client.level.getMinY();
            int maxY = client.level.getMaxY() - 1;
            if (clipboard.isEmpty()) {
               AiFill.Inner_DataUtil box = init_J_DataUtil(args);
               AiFill.Inner_StreamUtil transformed = init(box, action, step, amount, minY, maxY);
               AiFill.Inner_DataUtil out = transformed.box();
               if (out.volume() > 4096L) {
                  throw new IllegalArgumentException("Transformed box volume " + out.volume() + " exceeds 4096 cells; expand in smaller steps");
               }

               JsonObject data = AiJson.init_JsonObject_ObjArr(
                  "action",
                  action,
                  "x",
                  out.minX(),
                  "y",
                  out.minY(),
                  "z",
                  out.minZ(),
                  "x2",
                  out.maxX(),
                  "y2",
                  out.maxY(),
                  "z2",
                  out.maxZ(),
                  "volume",
                  out.volume(),
                  "clamped",
                  transformed.clamped()
               );
               String store = AiJson.init_Str_JsonObject(args, "store", "").trim();
               if (store.isEmpty()) {
                  return AiGameOperations.init_AiOperation(
                     AiOperation.Status.SUCCEEDED,
                     "Region "
                        + action
                        + "ed to "
                        + out.minX()
                        + ","
                        + out.minY()
                        + ","
                        + out.minZ()
                        + " .. "
                        + out.maxX()
                        + ","
                        + out.maxY()
                        + ","
                        + out.maxZ(),
                     data
                  );
               }

               String name = AiClipboard.init_Str(store);
               List<AiBuilder.CellSpec> cells = init(out, client.level);
               AiClipboard.init(name, List.copyOf(cells));
               data.addProperty("name", name);
               data.addProperty("cells", cells.size());
               data.addProperty("persisted", true);
               return AiGameOperations.init_AiOperation(
                  AiOperation.Status.SUCCEEDED, "Region " + action + "ed and stored " + cells.size() + " cells to clipboard \"" + name + "\"", data
               );
            } else {
               if (action.equals("shift")) {
                  throw new IllegalArgumentException(
                     "shift is identity for a clipboard: game:paste re-anchors at the minimum corner - move the paste origin (x,y,z) instead"
                  );
               }

               String name = AiClipboard.init_Str(clipboard);
               AiFill.Inner_CachePool transformed = init(AiClipboard.init_List(name), action, step, amount, 4096);
               AiClipboard.init(name, transformed.cells());
               int[] extents = init(transformed.cells());
               return AiGameOperations.init_AiOperation(
                  AiOperation.Status.SUCCEEDED,
                  "Clipboard \"" + name + "\" " + action + "ed by " + amount,
                  AiJson.init_JsonObject_ObjArr(
                     "name",
                     name,
                     "action",
                     action,
                     "cells",
                     transformed.cells().size(),
                     "dx",
                     extents[0],
                     "dy",
                     extents[1],
                     "dz",
                     extents[2],
                     "clamped",
                     transformed.clamped(),
                     "persisted",
                     true
                  )
               );
            }
         } else {
            throw new IllegalArgumentException("amount must be 1..64, got " + amount);
         }
      } else {
         throw new IllegalArgumentException("Region transforms require a connected living player");
      }
   }

   static AiOperation update(JsonObject args) {
      Minecraft client = Minecraft.getInstance();
      if (client.player != null && client.level != null) {
         AiFill.Inner_DataUtil box = init_J_DataUtil(args);
         if (box.volume() > 4096L) {
            throw new IllegalArgumentException("Box volume " + box.volume() + " exceeds 4096 cells");
         }

         String from = AiJson.init_Str_JsonObject(args, "from", "").trim();
         if (from.isEmpty()) {
            throw new IllegalArgumentException("from is required: a block spec or \"any\"");
         }

         Block to = init_Block(AiJson.init_Str_JsonObject(args, "to", ""), "to");
         Map<String, String> properties = AiBuilder.init_Map_JsonObject(args, "replace", to);
         int timeoutSeconds = Math.max(5, Math.min(3600, AiJson.init_I_JsonObject(args, "timeoutSeconds", 600)));
         Minecraft mc = Minecraft.getInstance();
         String toId = BuiltInRegistries.BLOCK.getKey(to).toString();
         List<AiBuilder.CellSpec> cells = new ArrayList<>();
         List<BlockPos> digCells = new ArrayList<>();

         for (BlockPos pos : init(box, false)) {
            BlockState live = mc.level.getBlockState(pos);
            AiBuilder.CellSpec cell = new AiBuilder.CellSpec(
               pos.getX() - box.minX(), pos.getY() - box.minY(), pos.getZ() - box.minZ(), toId, properties
            );
            if (!AiBuilder.write(cell, live) && init_Z(from, BuiltInRegistries.BLOCK.getKey(live.getBlock()).toString())) {
               cells.add(cell);
               digCells.add(pos);
            }
         }

         AiBuilder.Blueprint blueprint = new AiBuilder.Blueprint(new BlockPos(box.minX(), box.minY(), box.minZ()), cells);
         JsonObject missing = AiBuilder.init(blueprint, mc.player.getInventory());
         return missing.size() > 0
            ? AiGameOperations.init(
               AiOperation.Status.FAILED,
               AiFailure.MISSING_MATERIALS,
               "Missing materials: " + missing,
               AiJson.init_JsonObject_ObjArr("missing", missing, "replaced", 0, "dug", 0, "failed", new JsonArray())
            )
            : new AiFill.Inner_BufferHelper(digCells, blueprint, timeoutSeconds);
      } else {
         throw new IllegalArgumentException("Replacing requires a connected living player");
      }
   }

   private static Block init_Block(String spec, String arg) {
      if (spec.isBlank()) {
         throw new IllegalArgumentException(arg + " is required");
      } else {
         AiItems.ItemSpec target = AiItems.read_af_DataUtil(spec);
         if (target.blocks().size() != 1) {
            throw new IllegalArgumentException(
               arg + " must resolve to a single block id, got " + target.blocks().size() + " from \"" + spec + "\"; use a full registry id"
            );
         } else {
            return target.blocks().iterator().next();
         }
      }
   }

   private static final class Inner_BufferHelper implements AiOperation {
      final Minecraft init_Minecraft = Minecraft.getInstance();
      final List<BlockPos> init_List;
      final AiBuilder.Blueprint init_TableIndex_DataUti;
      final long init_J;
      AiOperation init_AiOperation;
      final JsonArray init_JsonArray = new JsonArray();
      int init_I;
      int load_I;
      int read;
      int write;
      BlockPos init_BlockPos;
      AiOperation load_AiOperation;
      boolean init_Z;

      Inner_BufferHelper(List<BlockPos> digCells, AiBuilder.Blueprint blueprint, int timeoutSeconds) {
         this.init_List = digCells;
         this.init_TableIndex_DataUti = blueprint;
         this.init_J = System.nanoTime() + timeoutSeconds * 1000000000L;
      }

      @Override
      public AiOperation.Result tick() {
         if (!this.init_Z && this.init_Minecraft.player != null && this.init_Minecraft.level != null) {
            if (this.init_Minecraft.player.getHealth() <= 4.0F && !AiGuard.close()) {
               return this.init(
                  AiOperation.Status.FAILED,
                  AiFailure.LOW_HEALTH,
                  AiGuard.init_Str_Str("replacing", this.init_Minecraft.player.getHealth(), this.init_Minecraft.player.getMaxHealth())
               );
            }

            if (System.nanoTime() > this.init_J) {
               return this.init(AiOperation.Status.TIMED_OUT, "Replace deadline reached");
            }

            if (this.load_AiOperation == null) {
               if (this.init_AiOperation != null) {
                  AiOperation.Result result = this.init_AiOperation.tick();
                  if (!result.terminal()) {
                     return this.init("Breaking replacement cell " + this.init_BlockPos.toShortString());
                  }

                  this.init_AiOperation.cancel();
                  this.init_AiOperation = null;
                  if (result.status() == AiOperation.Status.SUCCEEDED) {
                     this.load_I++;
                  } else {
                     this.init(this.init_BlockPos, result.failure().name());
                  }

                  this.init_BlockPos = null;
               }

               while (this.init_I < this.init_List.size()) {
                  BlockPos cell = this.init_List.get(this.init_I++);
                  autismclient.ai.engine.AiPathfinder.CellInfo probe = autismclient.ai.engine.AiPathfinder.LiveGridView.init_ClassCache_FileCac_Minecraft(
                     this.init_Minecraft, this.init_Minecraft.level, cell, new ItemStack[9]
                  );
                  if (!probe.loaded()) {
                     this.init(cell, "WORLD_UNKNOWN");
                  } else if (!this.init_Minecraft.level.getBlockState(cell).isAir()) {
                     if (!autismclient.ai.engine.AiExcavate.init(
                        (x, y, z) -> autismclient.ai.engine.AiPathfinder.LiveGridView.init_ClassCache_FileCac_Minecraft(
                              this.init_Minecraft, this.init_Minecraft.level, new BlockPos(x, y, z), new ItemStack[9]
                           )
                           .lava(),
                        cell.getX(),
                        cell.getY(),
                        cell.getZ()
                     )) {
                        this.init_BlockPos = cell;
                        this.init_AiOperation = autismclient.ai.engine.AiBreakBlock.init(cell, Math.min(60, this.init_I_5()));
                        return this.init("Approaching replacement cell " + cell.toShortString());
                     }

                     this.read++;
                  }
               }

               if (this.load_AiOperation == null) {
                  this.load_AiOperation = AiBuilder.init_AiOperation_TableIndex_DataUti(this.init_TableIndex_DataUti, true, this.init_I_5());
                  return this.init("Placing replacement blocks");
               } else {
                  return this.init("Placing replacement blocks");
               }
            } else {
               AiOperation.Result r = this.load_AiOperation.tick();
               if (!r.terminal()) {
                  return this.init("Placing replacement blocks");
               }

               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
               JsonObject buildData = AiJson.init_JsonObject_Str(r.dataJson());
               this.write = buildData.has("placed") ? buildData.get("placed").getAsInt() : 0;
               if (buildData.has("failed")) {
                  this.init_JsonArray.addAll(buildData.getAsJsonArray("failed"));
               }

               return this.init(
                  r.status() == AiOperation.Status.SUCCEEDED && (!this.init_JsonArray.isEmpty() || this.read > 0) ? AiOperation.Status.FAILED : r.status(),
                  r.message()
               );
            }
         } else {
            return this.init(AiOperation.Status.CANCELLED, "Replace cancelled");
         }
      }

      void init(BlockPos cell, String reason) {
         if (this.init_JsonArray.size() < 32) {
            this.init_JsonArray.add(AiJson.init_JsonObject_ObjArr("x", cell.getX(), "y", cell.getY(), "z", cell.getZ(), "reason", reason));
         }
      }

      int init_I_5() {
         return (int)Math.max(1L, (this.init_J - System.nanoTime()) / 1000000000L);
      }

      JsonObject init_JsonObject_10() {
         return AiJson.init_JsonObject_ObjArr(
            "replaced",
            this.write,
            "dug",
            this.load_I,
            "digTotal",
            this.init_List.size(),
            "failed",
            this.init_JsonArray,
            "skipped",
            this.read,
            "position",
            this.init_Minecraft.player == null ? null : this.init_Minecraft.player.blockPosition().toShortString()
         );
      }

      AiOperation.Result init(String message) {
         return new AiOperation.Result(AiOperation.Status.RUNNING, message, this.init_JsonObject_10().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, String message) {
         this.cancel();
         return new AiOperation.Result(status, message, this.init_JsonObject_10().toString(), List.of());
      }

      AiOperation.Result init(AiOperation.Status status, AiFailure code, String message) {
         this.cancel();
         return AiOperation.Result.failed(code, message, this.init_JsonObject_10().toString());
      }

      @Override
      public void cancel() {
         if (!this.init_Z) {
            this.init_Z = true;
            if (this.load_AiOperation != null) {
               this.load_AiOperation.cancel();
               this.load_AiOperation = null;
            }

            if (this.init_AiOperation != null) {
               this.init_AiOperation.cancel();
               this.init_AiOperation = null;
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
                  key.setDown(autismclient.util.KeyMappingControl.init(key).autism$physicalForBody());
               }
            }
         }
      }
   }

   record Inner_CachePool(List<AiBuilder.CellSpec> cells, boolean clamped) {
   }

   record Inner_DataUtil(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      long volume() {
         return (long)(this.maxX - this.minX + 1) * (this.maxY - this.minY + 1) * (this.maxZ - this.minZ + 1);
      }
   }

   record Inner_StreamUtil(AiFill.Inner_DataUtil box, boolean clamped) {
   }
}
