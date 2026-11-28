package autismclient.ai;

import autismclient.util.AutismLiteVariant;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

final class AiSchematic {
   private static final long load_J = 67108864L;
   private static final long read_J = 67108864L;
   private static final int write_I = 1631;
   static final int init_I = 2097152;
   static final int load_I = 4096;
   static final int read_I = 1024;
   static final long init_J = 66049L;
   private static final int update_I = 2097152;
   static final String[] EXTENSIONS = new String[]{".schem", ".schematic", ".litematic"};
   private static final String[] DYE_COLORS = new String[]{
      "white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"
   };
   private static final String[] WOOD_TYPES = new String[]{"oak", "spruce", "birch", "jungle", "acacia", "dark_oak"};
   private static final String[] TORCH_FACINGS = new String[]{"east", "west", "south", "north"};
   private static final String[] HORIZONTAL_FACINGS = new String[]{"south", "west", "north", "east"};
   private static final String[] sync = new String[]{"east", "south", "west", "north"};
   private static final String[] flush = new String[]{"north", "south", "west", "east"};
   private static final String[] close = new String[]{"west", "north", "east", "south"};
   private static final String[] open = new String[]{null, null, "north", "south", "west", "east"};
   private static final String[] poll = new String[]{"down", "up", "north", "south", "west", "east"};

   private AiSchematic() {
   }

   static void init(List<Tag> entries, int offsetX, int offsetY, int offsetZ, List<AiSchematic.RailVehicleSpec> into) {
      for (Tag entry : entries) {
         if (entry instanceof CompoundTag outer) {
            CompoundTag data = outer.getCompound("Data").orElse(outer);
            String id = outer.getString("id").or(() -> outer.getString("Id")).or(() -> data.getString("id")).orElse("");
            if (!id.contains(":")) {
               id = "minecraft:" + id;
            }

            if (id.equals("minecraft:minecart") || id.endsWith("_minecart")) {
               ListTag pos = outer.getList("Pos").orElseGet(() -> data.getListOrEmpty("Pos"));
               if (pos.size() != 3) {
                  throw new IllegalArgumentException("Saved minecart has no three-coordinate position");
               }

               double x = pos.getDoubleOr(0, 0.0 / 0.0) + offsetX;
               double y = pos.getDoubleOr(1, 0.0 / 0.0) + offsetY;
               double z = pos.getDoubleOr(2, 0.0 / 0.0) + offsetZ;
               if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z) || Math.abs(x) > 3.0E7 || Math.abs(y) > 3.0E7 || Math.abs(z) > 3.0E7) {
                  throw new IllegalArgumentException("Saved minecart position is invalid");
               }

               if (into.size() >= 16384) {
                  throw new IllegalArgumentException("Schematic exceeds 16384 saved rail vehicles");
               }

               into.add(new AiSchematic.RailVehicleSpec(id, new Vec3(x, y, z)));
            }
         }
      }
   }

   static void init(List<Tag> entries, int offsetX, int offsetY, int offsetZ, int dataVersion, List<AiSchematic.ItemFrameSpec> into) {
      int current = SharedConstants.getCurrentVersion().dataVersion().version();

      for (Tag entry : entries) {
         if (entry instanceof CompoundTag outer) {
            CompoundTag data = outer.getCompound("Data").orElse(outer);
            String id = outer.getString("id").or(() -> outer.getString("Id")).or(() -> data.getString("id")).orElse("");
            if (!id.contains(":")) {
               id = "minecraft:" + id;
            }

            if (id.equals("minecraft:item_frame") || id.equals("minecraft:glow_item_frame")) {
               int[] block = data.getIntArray("block_pos").filter(b -> b.length == 3).orElse(null);
               int x;
               int y;
               int z;
               if (block != null) {
                  x = block[0];
                  y = block[1];
                  z = block[2];
               } else if (data.contains("TileX")) {
                  x = data.getIntOr("TileX", 0);
                  y = data.getIntOr("TileY", 0);
                  z = data.getIntOr("TileZ", 0);
               } else {
                  ListTag pos = data.getListOrEmpty("Pos");
                  if (pos.size() != 3) {
                     continue;
                  }

                  x = (int)Math.floor(pos.getDoubleOr(0, 0.0));
                  y = (int)Math.floor(pos.getDoubleOr(1, 0.0));
                  z = (int)Math.floor(pos.getDoubleOr(2, 0.0));
               }

               int facing = data.getByteOr("Facing", (byte)-1);
               if (facing >= 0 && facing <= 5) {
                  CompoundTag item = data.getCompound("Item").map(CompoundTag::copy).orElse(null);
                  if (item != null && item.isEmpty()) {
                     item = null;
                  }

                  if (item != null && dataVersion > 0 && dataVersion < current) {
                     try {
                        item = (CompoundTag)DataFixers.getDataFixer()
                           .update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, item), dataVersion, current)
                           .getValue();
                     } catch (RuntimeException failure) {
                        item = null;
                     }
                  }

                  if (into.size() >= 16384) {
                     throw new IllegalArgumentException("Schematic exceeds 16384 saved item frames");
                  }

                  into.add(
                     new AiSchematic.ItemFrameSpec(
                        id,
                        new BlockPos(x + offsetX, y + offsetY, z + offsetZ),
                        Direction.from3DDataValue(facing),
                        item,
                        Math.floorMod(data.getByteOr("ItemRotation", (byte)0), 8),
                        data.getBooleanOr("Invisible", false),
                        data.getBooleanOr("Fixed", false)
                     )
                  );
               }
            }
         }
      }
   }

   private static List<AiSchematic.ItemFrameSpec> itemFrames(AiSchematic.DecodedFile decoded, int rotation, String mirror, BlockPos minimum) {
      if (decoded.itemFrames().isEmpty()) {
         return List.of();
      }

      AiBuilder.CellSpec origin = AiBuilder.transformCell(
         new AiBuilder.CellSpec(0, 0, 0, "minecraft:stone"), rotation, mirror
      );
      ArrayList<AiSchematic.ItemFrameSpec> out = new ArrayList<>();

      for (AiSchematic.ItemFrameSpec frame : decoded.itemFrames()) {
         BlockPos c = frame.cell();
         AiBuilder.CellSpec moved = AiBuilder.transformCell(
            new AiBuilder.CellSpec(c.getX(), c.getY(), c.getZ(), "minecraft:stone"), rotation, mirror
         );
         Direction facing = frame.facing();
         int turned = frame.rotation();
         if (facing.getAxis().isHorizontal()) {
            AiBuilder.CellSpec step = AiBuilder.transformCell(
               new AiBuilder.CellSpec(facing.getStepX(), 0, facing.getStepZ(), "minecraft:stone"), rotation, mirror
            );
            int sx = step.dx() - origin.dx();
            int sz = step.dz() - origin.dz();

            for (Direction side : Plane.HORIZONTAL) {
               if (side.getStepX() == sx && side.getStepZ() == sz) {
                  facing = side;
               }
            }
         } else {
            turned = Math.floorMod(turned + (facing == Direction.UP ? 2 : -2) * (rotation / 90), 8);
         }

         out.add(
            new AiSchematic.ItemFrameSpec(
               frame.id(),
               new BlockPos(moved.dx() - minimum.getX(), moved.dy() - minimum.getY(), moved.dz() - minimum.getZ()),
               facing,
               frame.item(),
               turned,
               frame.invisible(),
               frame.fixed()
            )
         );
      }

      return List.copyOf(out);
   }

   static void countEntities(List<Tag> list, Map<String, Integer> into) {
      for (Tag entry : list) {
         if (entry instanceof CompoundTag entity) {
            String id = entity.getString("id").or(() -> entity.getString("Id")).orElse("unknown");
            if (!id.isBlank()) {
               into.merge(id.replace("minecraft:", ""), 1, Integer::sum);
            }
         }
      }
   }

   static Path init() {
      return Minecraft.getInstance().gameDirectory.toPath().resolve("schematics");
   }

   static String validateName(String name) {
      String clean = name == null ? "" : name.trim();
      if (clean.isEmpty() || clean.length() > 128) {
         throw new IllegalArgumentException("schematic name must be 1..128 characters");
      } else if (!clean.contains("..") && !clean.contains("/") && !clean.contains("\\") && !clean.contains(":")) {
         return clean;
      } else {
         throw new IllegalArgumentException("schematic name must not contain path separators or '..': " + name);
      }
   }

   static Path resolveFile(Path schematicsDir, String name) {
      if (!AutismLiteVariant.enabled()) {
         Path registered = SchematicLibrary.resolveToken(name == null ? "" : name.trim());
         if (registered != null) {
            return registered;
         }
      }

      String clean = validateName(name);
      String lower = clean.toLowerCase(Locale.ROOT);
      boolean suffixed = false;

      for (String extension : EXTENSIONS) {
         suffixed |= lower.endsWith(extension);
      }

      List<String> candidates = new ArrayList<>();
      if (suffixed) {
         candidates.add(clean);
      } else {
         for (String extension : EXTENSIONS) {
            candidates.add(clean + extension);
         }
      }

      Path base = schematicsDir.normalize();

      for (String candidate : candidates) {
         Path path = base.resolve(candidate).normalize();
         if (path.startsWith(base) && Files.isRegularFile(path)) {
            return path;
         }
      }

      throw new IllegalArgumentException("No schematic named \"" + clean + "\" in " + schematicsDir + " (tried " + String.join(", ", candidates) + ")");
   }

   static AiBuilder.Blueprint init(Path file, BlockPos origin, int rotation, String mirror, boolean skipUnknown) {
      return loadBlueprint(file, origin, rotation, mirror, skipUnknown, 2097152);
   }

   static AiBuilder.Blueprint loadBlueprint(Path file, BlockPos origin, int rotation, String mirror, boolean skipUnknown, int maxCells) {
      return load(file, origin, rotation, mirror, skipUnknown, maxCells).blueprint();
   }

   static AiBuilder.Blueprint loadBlueprint(
      Path file, BlockPos origin, int rotation, String mirror, boolean skipUnknown, int maxCells, int maxOffset
   ) {
      return loadSchematic(file, origin, rotation, mirror, skipUnknown, maxCells, maxOffset).blueprint();
   }

   static AiSchematic.LoadedSchematic load(Path file, BlockPos origin, int rotation, String mirror, boolean skipUnknown, int maxCells) {
      AiSchematic.LoadedSchematic loaded = loadSchematic(file, origin, rotation, mirror, skipUnknown, maxCells, 1023);
      init(loaded.blueprint().cells());
      return loaded;
   }

   static void init(List<AiBuilder.CellSpec> cells) {
      if (!cells.isEmpty()) {
         int minX = 2147483647;
         int minY = minX;
         int minZ = minX;
         int maxX = -2147483648;
         int maxY = maxX;
         int maxZ = maxX;

         for (AiBuilder.CellSpec cell : cells) {
            minX = Math.min(minX, cell.dx());
            maxX = Math.max(maxX, cell.dx());
            minY = Math.min(minY, cell.dy());
            maxY = Math.max(maxY, cell.dy());
            minZ = Math.min(minZ, cell.dz());
            maxZ = Math.max(maxZ, cell.dz());
         }

         long width = (long)maxX - minX + 1L;
         long height = (long)maxY - minY + 1L;
         long depth = (long)maxZ - minZ + 1L;
         if (width > 1024L || depth > 1024L) {
            throw new IllegalArgumentException("Schematic horizontal span exceeds 1024 blocks (" + width + " x " + depth + "); split it before building");
         }

         if (height > 257L) {
            throw new IllegalArgumentException("Schematic height exceeds 257 blocks; split it into layers before building");
         }

         if (width * depth > 66049L) {
            throw new IllegalArgumentException(
               "Schematic X/Z footprint has " + width * depth + " columns (" + width + " x " + depth + "); the build limit is 66049. Split it before building"
            );
         }
      }
   }

   static AiSchematic.LoadedSchematic loadSchematic(Path file, BlockPos origin, int rotation, String mirror, boolean skipUnknown, int maxCells, int maxOffset) {
      AiSchematic.DecodedFile decoded = decodeFile(file, skipUnknown);
      List<AiBuilder.CellSpec> cells = AiBuilder.transformCells(decoded.cells(), rotation, mirror);
      if (cells.isEmpty()) {
         throw new IllegalArgumentException("Schematic contains no blocks to build");
      }

      BlockPos minimum = AiBuilder.minCellOffset(cells);
      return new AiSchematic.LoadedSchematic(
         new AiBuilder.Blueprint(origin, AiBuilder.normalizeCells(cells, maxCells, maxOffset, minimum)),
         signTexts(decoded, rotation, mirror, minimum),
         decoded.entities(),
         containerContents(decoded, rotation, mirror, minimum),
         railVehicles(decoded, rotation, mirror, minimum),
         itemFrames(decoded, rotation, mirror, minimum)
      );
   }

   static CompoundTag readNbt(Path file, long nbtBudget) {
      String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

      long size;
      try {
         size = Files.size(file);
      } catch (IOException e) {
         throw new IllegalArgumentException("Could not read schematic " + file.getFileName() + ": " + e.getMessage());
      }

      if (size > 67108864L) {
         throw new IllegalArgumentException("Schematic " + file.getFileName() + " is " + size + " bytes; the limit is 67108864");
      }

      if (Arrays.stream(EXTENSIONS).noneMatch(name::endsWith)) {
         throw new IllegalArgumentException("Unsupported schematic extension for " + file.getFileName() + "; supported: " + String.join(", ", EXTENSIONS));
      }

      try (BufferedInputStream input = new BufferedInputStream(Files.newInputStream(file))) {
         input.mark(2);
         boolean gzip = input.read() == 31 && input.read() == 139;
         input.reset();
         NbtAccounter budget = NbtAccounter.create(nbtBudget);

         CompoundTag root;
         try (DataInputStream data = new DataInputStream(gzip ? new GZIPInputStream(input) : input)) {
            CollectToTag collector = new CollectToTag();
            NbtIo.parse(data, collector, budget);
            if (!(collector.getResult() instanceof CompoundTag compound)) {
               throw new IOException("Root tag must be a compound");
            }

            root = compound;
            if (data.read() != -1) {
               throw new IOException("Unexpected data after schematic NBT");
            }
         }

         return root;
      } catch (Exception e) {
         throw new IllegalArgumentException("Could not read schematic " + file.getFileName() + ": " + e.getMessage(), e);
      }
   }

   static AiSchematic.DecodedFile decodeFile(Path file, boolean skipUnknown) {
      String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
      CompoundTag root = readNbt(file, 67108864L);
      if (root.getCompound("Regions").isPresent()) {
         return load(root, skipUnknown);
      } else {
         CompoundTag body = root.getCompound("Schematic").orElse(root);
         if (body.getCompound("Palette").isPresent() || body.getCompound("Blocks").flatMap(blocks -> blocks.getCompound("Palette")).isPresent()) {
            return init_bP_DataUtil(root, skipUnknown);
         } else if (body.getByteArray("Blocks").isPresent()) {
            return decodeSponge(body, skipUnknown);
         } else if (name.endsWith(".schem")) {
            return init_bP_DataUtil(root, skipUnknown);
         } else {
            return name.endsWith(".schematic") ? decodeSponge(root, skipUnknown) : load(root, skipUnknown);
         }
      }
   }

   static AiSchematic.DecodedFile decode(CompoundTag root) {
      return init_bP_DataUtil(root, false);
   }

   static AiSchematic.DecodedFile init_bP_DataUtil(CompoundTag root, boolean skipUnknown) {
      root = root.getCompound("Schematic").orElse(root);
      int version = root.getIntOr("Version", 1);
      if (version >= 1 && version <= 3) {
         int width = requiredDimension(root, "Width");
         int height = requiredDimension(root, "Height");
         int length = requiredDimension(root, "Length");
         CompoundTag blocks = version == 3
            ? root.getCompound("Blocks").orElseThrow(() -> new IllegalArgumentException("Sponge v3 schematic has no Blocks compound"))
            : root;
         CompoundTag paletteTag = blocks.getCompound("Palette").orElseThrow(() -> new IllegalArgumentException("Sponge schematic has no Palette compound"));
         int dataVersion = root.getIntOr("DataVersion", 1631);
         Map<Integer, AiSchematic.PaletteState> byIndex = new HashMap<>();
         Set<String> unknown = new LinkedHashSet<>();
         int unknownCells = 0;
         if (paletteTag.isEmpty()) {
            throw new IllegalArgumentException("Sponge schematic has no block palette");
         }

         for (String state : paletteTag.keySet()) {
            int index = paletteTag.getIntOr(state, -1);
            if (index < 0) {
               throw new IllegalArgumentException("Sponge Palette index out of range for " + state + ": " + index);
            }

            if (byIndex.containsKey(index)) {
               throw new IllegalArgumentException("Duplicate Sponge Palette index: " + index);
            }

            try {
               byIndex.put(index, init(state, "palette index " + index, dataVersion));
            } catch (AiSchematic.UnknownBlock e) {
               if (!skipUnknown) {
                  throw e;
               }

               byIndex.put(index, null);
               unknown.add(state);
            }
         }

         CompoundTag metadata = root.getCompoundOrEmpty("Metadata");
         int[] offset = root.getIntArray("Offset")
            .filter(o -> o.length == 3)
            .orElse(new int[]{metadata.getIntOr("WEOffsetX", 0), metadata.getIntOr("WEOffsetY", 0), metadata.getIntOr("WEOffsetZ", 0)});
         byte[] blockData = blocks.getByteArray(version == 3 ? "Data" : "BlockData")
            .orElseThrow(() -> new IllegalArgumentException("Sponge schematic has no BlockData array"));
         int volume = init(width, height, length);
         List<AiBuilder.CellSpec> cells = new ArrayList<>();
         int air = 0;
         int cursor = 0;
         int index = 0;

         label98:
         while (index < volume) {
            if (cursor >= blockData.length) {
               throw new IllegalArgumentException("Sponge BlockData is truncated: " + index + " of " + volume + " cells present");
            }

            int value = 0;
            int shift = 0;

            while (cursor < blockData.length) {
               int b = blockData[cursor++] & 255;
               if (shift == 28 && (b & 248) != 0) {
                  throw new IllegalArgumentException("Sponge BlockData varint is too large");
               }

               value |= (b & 127) << shift;
               shift += 7;
               if ((b & 128) == 0) {
                  int y = index / (width * length);
                  int rest = index % (width * length);
                  int z = rest / width;
                  int x = rest % width;
                  AiSchematic.PaletteState state = byIndex.get(value);
                  if (state == null && !byIndex.containsKey(value)) {
                     throw new IllegalArgumentException("Sponge BlockData names missing palette index " + value);
                  }

                  if (state == null) {
                     unknownCells++;
                  } else if (isAir(state.block())) {
                     air++;
                  } else {
                     if (cells.size() >= 2097152) {
                        throw new IllegalArgumentException("Schematic exceeds 2097152 non-air cells");
                     }

                     cells.add(new AiBuilder.CellSpec(offset[0] + x, offset[1] + y, offset[2] + z, state.block(), state.properties()));
                  }

                  index++;
                  continue label98;
               }
            }

            throw new IllegalArgumentException("Sponge BlockData ends mid-varint");
         }

         if (cursor != blockData.length) {
            throw new IllegalArgumentException("Sponge BlockData has " + (blockData.length - cursor) + " bytes after the last cell");
         }

         auditCheckUnknown(unknown, unknownCells, cells.size());
         HashMap<Long, String> signText = new HashMap<>();
         HashMap<Long, ListTag> contents = new HashMap<>();

         for (Tag entry : blocks.getListOrEmpty("BlockEntities").isEmpty() ? root.getListOrEmpty("BlockEntities") : blocks.getListOrEmpty("BlockEntities")) {
            if (entry instanceof CompoundTag entity) {
               int[] pos = entity.getIntArray("Pos").orElse(null);
               if (pos != null && pos.length == 3) {
                  String text = signText(entity.getCompound("Data").orElse(entity));
                  ListTag items = entity.getCompound("Data").orElse(entity).getListOrEmpty("Items");
                  if (!items.isEmpty()) {
                     contents.put(BlockPos.asLong(offset[0] + pos[0], offset[1] + pos[1], offset[2] + pos[2]), upgradeItems(items, dataVersion));
                  }

                  if (text != null) {
                     signText.put(BlockPos.asLong(offset[0] + pos[0], offset[1] + pos[1], offset[2] + pos[2]), text);
                  }
               }
            }
         }

         TreeMap<String, Integer> entities = new TreeMap<>();
         countEntities(root.getListOrEmpty("Entities"), entities);
         ArrayList<AiSchematic.RailVehicleSpec> vehicles = new ArrayList<>();
         init(root.getListOrEmpty("Entities"), offset[0], offset[1], offset[2], vehicles);
         ArrayList<AiSchematic.ItemFrameSpec> frames = new ArrayList<>();
         init(root.getListOrEmpty("Entities"), offset[0], offset[1], offset[2], dataVersion, frames);
         return new AiSchematic.DecodedFile(
            List.copyOf(cells),
            Set.of(),
            air + unknownCells,
            "sponge-v" + version,
            Map.copyOf(signText),
            Map.copyOf(entities),
            Map.copyOf(contents),
            List.copyOf(vehicles),
            List.copyOf(frames)
         );
      } else {
         throw new IllegalArgumentException("Unsupported Sponge schematic version " + version + "; supported: 1, 2, 3");
      }
   }

   private static int init(int width, int height, int length) {
      long volume = (long)width * height * length;
      if (width > 0 && height > 0 && length > 0 && volume <= 16777216L) {
         return (int)volume;
      } else {
         throw new IllegalArgumentException("Schematic dimensions exceed the 16777216-cell decode limit");
      }
   }

   private static int requiredDimension(CompoundTag root, String key) {
      int value = root.getShortOr(key, (short)0) & '\uffff';
      if (value == 0) {
         throw new IllegalArgumentException("Schematic is missing its " + key + " tag");
      } else {
         return value;
      }
   }

   static AiSchematic.PaletteState init_bP_ConfigStore_Str_Str(String raw, String context) {
      return init(raw, context, 0);
   }

   static AiSchematic.PaletteState parseState(String raw, int dataVersion) {
      return init(raw, "palette state " + raw, Math.max(1, dataVersion));
   }

   private static AiSchematic.PaletteState init(String raw, String context, int dataVersion) {
      int bracket = raw.indexOf(91);
      String id = (bracket < 0 ? raw : raw.substring(0, bracket)).trim();
      if (!id.contains(":")) {
         id = "minecraft:" + id;
      }

      Map<String, String> properties = new LinkedHashMap<>();
      if (bracket >= 0) {
         int close = raw.lastIndexOf(93);
         if (close < bracket) {
            throw new IllegalArgumentException(context + ": malformed block state \"" + raw + "\"");
         }

         for (String pair : raw.substring(bracket + 1, close).split(",")) {
            if (!pair.isBlank()) {
               String[] kv = pair.split("=", 2);
               if (kv.length != 2) {
                  throw new IllegalArgumentException(context + ": malformed property \"" + pair + "\" in \"" + raw + "\"");
               }

               properties.put(kv[0].trim(), kv[1].trim());
            }
         }
      }

      if (dataVersion <= 0) {
         return new AiSchematic.PaletteState(id, init(id, properties, context));
      }

      CompoundTag state = new CompoundTag();
      CompoundTag props = new CompoundTag();
      state.putString("Name", id);
      properties.forEach(props::putString);
      if (!properties.isEmpty()) {
         state.put("Properties", props);
      }

      return init(state, dataVersion, dataVersion < 1631, context);
   }

   static AiSchematic.PaletteState init(CompoundTag entry, int dataVersion, boolean preFlattening, String context) {
      String name = entry.getStringOr("Name", "");
      if (name.isBlank()) {
         throw new IllegalArgumentException(context + " has no block Name");
      }

      CompoundTag state = entry.copy();
      if (!name.contains(":")) {
         state.putString("Name", "minecraft:" + name);
      }

      int sourceVersion = dataVersion;
      if (preFlattening) {
         state = (CompoundTag)BlockStateData.upgradeBlockStateTag(new Dynamic<>(NbtOps.INSTANCE, state)).getValue();
         sourceVersion = Math.max(sourceVersion, 1631);
      }

      int current = SharedConstants.getCurrentVersion().dataVersion().version();
      if (sourceVersion > 0 && sourceVersion < current) {
         try {
            state = (CompoundTag)DataFixers.getDataFixer()
               .update(References.BLOCK_STATE, new Dynamic<>(NbtOps.INSTANCE, state), sourceVersion, current)
               .getValue();
         } catch (RuntimeException e) {
            throw new IllegalArgumentException(context + ": could not update " + name + " from Minecraft data version " + sourceVersion, e);
         }
      }

      String id = state.getStringOr("Name", "");
      Map<String, String> properties = new LinkedHashMap<>();
      CompoundTag props = state.getCompoundOrEmpty("Properties");

      for (String key : props.keySet()) {
         String value = props.getString(key).orElseThrow(() -> new IllegalArgumentException(context + ": property " + key + " must be a string"));
         properties.put(key, value);
      }

      return new AiSchematic.PaletteState(id, init(id, properties, context));
   }

   static Map<String, String> init(String blockId, Map<String, String> properties, String context) {
      return init(blockId, properties, context, true);
   }

   private static Map<String, String> init(String blockId, Map<String, String> properties, String context, boolean defaults) {
      Identifier id = Identifier.tryParse(blockId);
      Block block = id == null ? null : BuiltInRegistries.BLOCK.getOptional(id).orElse(null);
      if (block == null) {
         throw new AiSchematic.UnknownBlock(context + " names an unknown block id: " + blockId);
      }

      LinkedHashMap<String, String> resolved = new LinkedHashMap<>();
      if (defaults) {
         block.defaultBlockState().getValues().forEach(value -> resolved.put(value.property().getName(), value.valueName()));
      }

      if (!properties.isEmpty()) {
         JsonObject entry = new JsonObject();
         JsonObject props = new JsonObject();
         properties.forEach(props::addProperty);
         entry.add("properties", props);
         resolved.putAll(AiBuilder.parseBlockProperties(entry, context + " (" + blockId + ")", block));
      }

      return Map.copyOf(resolved);
   }

   static String signText(CompoundTag entity) {
      ArrayList<String> lines = new ArrayList<>();
      Optional<CompoundTag> front = entity.getCompound("front_text");
      if (front.isPresent()) {
         for (Tag message : front.get().getListOrEmpty("messages")) {
            lines.add(init(message));
         }
      } else {
         for (int i = 1; i <= 4; i++) {
            Tag line = entity.get("Text" + i);
            if (line != null) {
               lines.add(init(line));
            }
         }
      }

      if (lines.stream().allMatch(String::isBlank)) {
         return null;
      }

      while (lines.size() < 4) {
         lines.add("");
      }

      return String.join("\n", lines.subList(0, 4));
   }

   private static String init(Tag tag) {
      try {
         if (tag instanceof StringTag(String value)) {
            String trimmed = value.trim();
            return !trimmed.startsWith("{") && !trimmed.startsWith("[") && !trimmed.startsWith("\"")
               ? value
               : ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(trimmed)).result().map(Component::getString).orElse(value);
         } else {
            return ComponentSerialization.CODEC.parse(NbtOps.INSTANCE, tag).result().map(Component::getString).orElse("");
         }
      } catch (RuntimeException e) {
         return tag.asString().orElse("");
      }
   }

   static Map<Long, String> signTexts(AiSchematic.DecodedFile decoded, int rotation, String mirror) {
      return decoded.signText().isEmpty()
         ? Map.of()
         : signTexts(decoded, rotation, mirror, AiBuilder.minCellOffset(AiBuilder.transformCells(decoded.cells(), rotation, mirror)));
   }

   private static Map<Long, String> signTexts(AiSchematic.DecodedFile decoded, int rotation, String mirror, BlockPos minimum) {
      if (decoded.signText().isEmpty()) {
         return Map.of();
      }

      int minX = minimum.getX();
      int minY = minimum.getY();
      int minZ = minimum.getZ();
      HashMap<Long, String> out = new HashMap<>();

      for (Entry<Long, String> entry : decoded.signText().entrySet()) {
         BlockPos at = BlockPos.of(entry.getKey());
         AiBuilder.CellSpec moved = AiBuilder.transformCell(
            new AiBuilder.CellSpec(at.getX(), at.getY(), at.getZ(), "minecraft:stone"), rotation, mirror
         );
         out.put(BlockPos.asLong(moved.dx() - minX, moved.dy() - minY, moved.dz() - minZ), entry.getValue());
      }

      return Map.copyOf(out);
   }

   static Map<Long, ListTag> load(AiSchematic.DecodedFile decoded, int rotation, String mirror) {
      return decoded.containerContents().isEmpty()
         ? Map.of()
         : containerContents(decoded, rotation, mirror, AiBuilder.minCellOffset(AiBuilder.transformCells(decoded.cells(), rotation, mirror)));
   }

   private static Map<Long, ListTag> containerContents(AiSchematic.DecodedFile decoded, int rotation, String mirror, BlockPos minimum) {
      if (decoded.containerContents().isEmpty()) {
         return Map.of();
      }

      int minX = minimum.getX();
      int minY = minimum.getY();
      int minZ = minimum.getZ();
      HashMap<Long, ListTag> out = new HashMap<>();

      for (Entry<Long, ListTag> entry : decoded.containerContents().entrySet()) {
         BlockPos at = BlockPos.of(entry.getKey());
         AiBuilder.CellSpec moved = AiBuilder.transformCell(
            new AiBuilder.CellSpec(at.getX(), at.getY(), at.getZ(), "minecraft:stone"), rotation, mirror
         );
         out.put(BlockPos.asLong(moved.dx() - minX, moved.dy() - minY, moved.dz() - minZ), entry.getValue().copy());
      }

      return Map.copyOf(out);
   }

   static ListTag upgradeItems(ListTag source, int sourceVersion) {
      int current = SharedConstants.getCurrentVersion().dataVersion().version();
      ListTag result = new ListTag();
      HashSet<Integer> slots = new HashSet<>();

      for (Tag value : source) {
         AiBuilder.checkNotCancelled();
         if (!(value instanceof CompoundTag original)) {
            throw new IllegalArgumentException("Saved storage item is not a compound");
         }

         int slot = original.getIntOr("Slot", -1);
         if (slot < 0 || slot > 255 || !slots.add(slot)) {
            throw new IllegalArgumentException("Invalid or duplicate saved storage slot");
         }

         CompoundTag item = original.copy();
         item.remove("Slot");
         if (sourceVersion > 0 && sourceVersion < current) {
            try {
               item = (CompoundTag)DataFixers.getDataFixer()
                  .update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, item), sourceVersion, current)
                  .getValue();
            } catch (RuntimeException failure) {
               throw new IllegalArgumentException("Could not update saved storage item from Minecraft data version " + sourceVersion, failure);
            }
         }

         item.putInt("Slot", slot);
         result.add(item);
      }

      return result;
   }

   static List<AiSchematic.RailVehicleSpec> railVehicles(AiSchematic.DecodedFile decoded, int rotation, String mirror) {
      return decoded.railVehicles().isEmpty()
         ? List.of()
         : railVehicles(decoded, rotation, mirror, AiBuilder.minCellOffset(AiBuilder.transformCells(decoded.cells(), rotation, mirror)));
   }

   private static List<AiSchematic.RailVehicleSpec> railVehicles(AiSchematic.DecodedFile decoded, int rotation, String mirror, BlockPos minimum) {
      if (decoded.railVehicles().isEmpty()) {
         return List.of();
      }

      int minX = minimum.getX();
      int minY = minimum.getY();
      int minZ = minimum.getZ();
      ArrayList<AiSchematic.RailVehicleSpec> out = new ArrayList<>();

      for (AiSchematic.RailVehicleSpec vehicle : decoded.railVehicles()) {
         BlockPos rail = vehicle.rail();
         Vec3 pos = vehicle.position();
         double dx = pos.x - rail.getX() - 0.5;
         double dz = pos.z - rail.getZ() - 0.5;
         if (mirror.equals("x")) {
            dx = -dx;
         } else if (mirror.equals("z")) {
            dz = -dz;
         }

         for (int turn = 0; turn < rotation / 90; turn++) {
            double oldX = dx;
            dx = -dz;
            dz = oldX;
         }

         AiBuilder.CellSpec moved = AiBuilder.transformCell(
            new AiBuilder.CellSpec(rail.getX(), rail.getY(), rail.getZ(), "minecraft:stone"), rotation, mirror
         );
         out.add(new AiSchematic.RailVehicleSpec(vehicle.id(), new Vec3(moved.dx() - minX + 0.5 + dx, pos.y - minY, moved.dz() - minZ + 0.5 + dz)));
      }

      return List.copyOf(out);
   }

   private static boolean isAir(String blockId) {
      return blockId.equals("minecraft:air") || blockId.equals("minecraft:cave_air") || blockId.equals("minecraft:void_air");
   }

   private static void auditCheckUnknown(Set<String> unknown, int unknownCells, int known) {
      if (unknownCells > known) {
         throw new IllegalArgumentException(
            "Schematic has " + unknownCells + " cells of unknown blocks " + unknown + " (" + known + " known); refusing to turn most of the build into air"
         );
      }
   }

   static AiSchematic.DecodedFile decodeSponge(CompoundTag root, boolean skipUnknown) {
      int width = requiredDimension(root, "Width");
      int height = requiredDimension(root, "Height");
      int length = requiredDimension(root, "Length");
      byte[] blocks = root.getByteArray("Blocks").orElseThrow(() -> new IllegalArgumentException("Legacy schematic has no Blocks array"));
      byte[] data = root.getByteArray("Data").orElse(new byte[0]);
      byte[] add = root.getByteArray("AddBlocks").orElse(new byte[0]);
      int volume = init(width, height, length);
      if (blocks.length < volume) {
         throw new IllegalArgumentException("Legacy Blocks array is truncated: " + blocks.length + " of " + volume + " cells present");
      }

      int offsetX = root.getIntOr("WEOffsetX", 0);
      int offsetY = root.getIntOr("WEOffsetY", 0);
      int offsetZ = root.getIntOr("WEOffsetZ", 0);
      List<AiBuilder.CellSpec> cells = new ArrayList<>();
      Set<Integer> unmapped = new LinkedHashSet<>();
      Map<Integer, AiBuilder.CellSpec> mapped = new HashMap<>();
      int air = 0;
      int unknownCells = 0;

      for (int index = 0; index < volume; index++) {
         int id = blocks[index] & 255;
         if (add.length > index >> 1) {
            id |= (add[index >> 1] >> (index & 1) * 4 & 15) << 8;
         }

         if (id == 0) {
            air++;
         } else {
            int meta = index < data.length ? data[index] & 0xFF : 0;
            int y = index / (width * length);
            int rest = index % (width * length);
            int z = rest / width;
            int x = rest % width;
            int key = id << 8 | meta;
            AiBuilder.CellSpec template = mapped.get(key);
            if (template == null) {
               AiSchematic.LegacyBlockState state = legacyBlock(id, meta);
               if (state == null) {
                  unmapped.add(id);
                  unknownCells++;
                  continue;
               }

               if (isAir(state.block())) {
                  air++;
                  continue;
               }

               template = new AiBuilder.CellSpec(
                  0, 0, 0, state.block(), init(state.block(), state.properties(), "legacy id " + id + ":" + meta, false)
               );
               mapped.put(key, template);
            }

            cells.add(new AiBuilder.CellSpec(offsetX + x, offsetY + y, offsetZ + z, template.block(), template.properties()));
            if (cells.size() > 2097152) {
               throw new IllegalArgumentException("Schematic exceeds 2097152 non-air cells");
            }
         }
      }

      if (!unmapped.isEmpty() && !skipUnknown) {
         throw new IllegalArgumentException(
            "Legacy schematic uses " + unmapped.size() + " unmapped block id(s) " + unmapped + "; retry with skipUnknown:true to build without those cells"
         );
      }

      if (skipUnknown && unknownCells > cells.size()) {
         throw new IllegalArgumentException(
            "Legacy schematic has "
               + unknownCells
               + " unmapped cells ("
               + cells.size()
               + " mapped); refusing to turn most of the build into air. Add the missing block ids "
               + unmapped
               + " or convert the file to a modern format"
         );
      }

      TreeMap<String, Integer> entities = new TreeMap<>();
      countEntities(root.getListOrEmpty("Entities"), entities);
      ArrayList<AiSchematic.RailVehicleSpec> vehicles = new ArrayList<>();
      init(root.getListOrEmpty("Entities"), offsetX, offsetY, offsetZ, vehicles);
      return new AiSchematic.DecodedFile(
         List.copyOf(cells),
         Set.copyOf(unmapped),
         air + (skipUnknown ? unknownCells : 0),
         "legacy",
         Map.of(),
         Map.copyOf(entities),
         Map.of(),
         List.copyOf(vehicles)
      );
   }

   static AiSchematic.DecodedFile load(CompoundTag root) {
      return load(root, false);
   }

   static AiSchematic.DecodedFile load(CompoundTag root, boolean skipUnknown) {
      int version = root.getIntOr("Version", 0);
      if (version >= 1 && version <= 7) {
         int dataVersion = root.getIntOr("MinecraftDataVersion", 1631);
         boolean preFlattening = version < 4 || dataVersion > 0 && dataVersion < 1631;
         CompoundTag regions = root.getCompound("Regions").orElseThrow(() -> new IllegalArgumentException("Litematic has no Regions compound"));
         if (regions.size() == 0) {
            throw new IllegalArgumentException("Litematic has no regions");
         }

         List<AiBuilder.CellSpec> cells = new ArrayList<>();
         HashMap<Long, String> signText = new HashMap<>();
         HashMap<Long, ListTag> contents = new HashMap<>();
         TreeMap<String, Integer> entities = new TreeMap<>();
         ArrayList<AiSchematic.RailVehicleSpec> vehicles = new ArrayList<>();
         ArrayList<AiSchematic.ItemFrameSpec> frames = new ArrayList<>();
         ArrayList<BoundingBox> boxes = new ArrayList<>();
         Set<String> unknown = new LinkedHashSet<>();
         int unknownCells = 0;
         int air = 0;
         long totalVolume = 0L;

         for (String regionName : regions.keySet()) {
            CompoundTag region = regions.getCompoundOrEmpty(regionName);
            CompoundTag position = region.getCompoundOrEmpty("Position");
            CompoundTag size = region.getCompoundOrEmpty("Size");
            int px = position.getIntOr("x", 0);
            int py = position.getIntOr("y", 0);
            int pz = position.getIntOr("z", 0);
            int sx = size.getIntOr("x", 0);
            int sy = size.getIntOr("y", 0);
            int sz = size.getIntOr("z", 0);
            int ax = Math.abs(sx);
            int ay = Math.abs(sy);
            int az = Math.abs(sz);
            if (ax != 0 && ay != 0 && az != 0) {
               int minX = sx >= 0 ? px : px + sx + 1;
               int minY = sy >= 0 ? py : py + sy + 1;
               int minZ = sz >= 0 ? pz : pz + sz + 1;
               ListTag palette = region.getListOrEmpty("BlockStatePalette");
               long[] packed = region.getLongArray("BlockStates")
                  .orElseThrow(() -> new IllegalArgumentException("Litematic region \"" + regionName + "\" has no BlockStates array"));
               int bits = Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, palette.size() - 1)));
               long mask = (1L << bits) - 1L;
               int volume = init(ax, ay, az);
               if (packed.length != ((long)volume * bits + 63L) / 64L) {
                  throw new IllegalArgumentException(
                     "Litematic region \""
                        + regionName
                        + "\" BlockStates has the wrong length: "
                        + packed.length
                        + " longs for "
                        + volume
                        + " cells at "
                        + bits
                        + " bits"
                  );
               }

               totalVolume += volume;
               if (totalVolume > 16777216L) {
                  throw new IllegalArgumentException("Litematic regions exceed the 16777216-cell decode limit");
               }

               if (palette.isEmpty()) {
                  throw new IllegalArgumentException("Litematic region \"" + regionName + "\" has no block palette");
               }

               AiSchematic.PaletteState[] states = new AiSchematic.PaletteState[palette.size()];

               for (int value = 0; value < palette.size(); value++) {
                  try {
                     states[value] = init(
                        palette.getCompoundOrEmpty(value), dataVersion, preFlattening, "Litematic region \"" + regionName + "\" palette entry " + value
                     );
                  } catch (AiSchematic.UnknownBlock e) {
                     if (!skipUnknown) {
                        throw e;
                     }

                     unknown.add(palette.getCompoundOrEmpty(value).getStringOr("Name", ""));
                  }
               }

               // A later region overwrites earlier ones where they overlap, air included, as the library preview shows it.
               BoundingBox box = new BoundingBox(minX, minY, minZ, minX + ax - 1, minY + ay - 1, minZ + az - 1);
               if (boxes.stream().anyMatch(box::intersects)) {
                  cells.removeIf(c -> box.isInside(c.dx(), c.dy(), c.dz()));
                  signText.keySet().removeIf(k -> box.isInside(BlockPos.of(k)));
                  contents.keySet().removeIf(k -> box.isInside(BlockPos.of(k)));
               }

               boxes.add(box);
               countEntities(region.getListOrEmpty("Entities"), entities);
               init(region.getListOrEmpty("Entities"), px, py, pz, vehicles);
               init(region.getListOrEmpty("Entities"), px, py, pz, dataVersion, frames);

               for (Tag entry : region.getListOrEmpty("TileEntities")) {
                  if (entry instanceof CompoundTag entity) {
                     String text = signText(entity);
                     ListTag items = entity.getListOrEmpty("Items");
                     if (!items.isEmpty()) {
                        contents.put(
                           BlockPos.asLong(minX + entity.getIntOr("x", 0), minY + entity.getIntOr("y", 0), minZ + entity.getIntOr("z", 0)),
                           upgradeItems(items, dataVersion)
                        );
                     }

                     if (text != null) {
                        signText.put(BlockPos.asLong(minX + entity.getIntOr("x", 0), minY + entity.getIntOr("y", 0), minZ + entity.getIntOr("z", 0)), text);
                     }
                  }
               }

               for (int index = 0; index < volume; index++) {
                  long bit = (long)index * bits;
                  int word = (int)(bit >>> 6);
                  int shift = (int)(bit & 63L);
                  long valueBits = packed[word] >>> shift;
                  if (shift + bits > 64) {
                     valueBits |= packed[word + 1] << 64 - shift;
                  }

                  int value = (int)(valueBits & mask);
                  if (value >= palette.size()) {
                     throw new IllegalArgumentException(
                        "Litematic region \"" + regionName + "\" names palette index " + value + " but the palette has " + palette.size() + " entries"
                     );
                  }

                  AiSchematic.PaletteState state = states[value];
                  int y = index / (ax * az);
                  int rest = index % (ax * az);
                  int z = rest / ax;
                  int x = rest % ax;
                  if (state == null) {
                     unknownCells++;
                  } else if (isAir(state.block())) {
                     air++;
                  } else {
                     if (cells.size() >= 2097152) {
                        throw new IllegalArgumentException("Schematic exceeds 2097152 non-air cells");
                     }

                     cells.add(new AiBuilder.CellSpec(minX + x, minY + y, minZ + z, state.block(), state.properties()));
                  }
               }
            }
         }

         auditCheckUnknown(unknown, unknownCells, cells.size());
         return new AiSchematic.DecodedFile(
            List.copyOf(cells),
            Set.of(),
            air + unknownCells,
            "litematic-v" + version,
            Map.copyOf(signText),
            Map.copyOf(entities),
            Map.copyOf(contents),
            List.copyOf(vehicles),
            List.copyOf(frames)
         );
      } else {
         throw new IllegalArgumentException("Unsupported Litematic version " + version + "; supported: 1 through 7");
      }
   }

   private static String legacyFacing(int data) {
      String facing = open[Math.min(data & 7, 5)];
      return facing == null ? "north" : facing;
   }

   private static AiSchematic.LegacyBlockState simpleBlock(String block) {
      return new AiSchematic.LegacyBlockState("minecraft:" + block, Map.of());
   }

   private static AiSchematic.LegacyBlockState init(String block, String k1, String v1) {
      return new AiSchematic.LegacyBlockState("minecraft:" + block, Map.of(k1, v1));
   }

   private static AiSchematic.LegacyBlockState init(String block, String k1, String v1, String k2, String v2) {
      return new AiSchematic.LegacyBlockState("minecraft:" + block, Map.of(k1, v1, k2, v2));
   }

   private static AiSchematic.LegacyBlockState init(String block, String k1, String v1, String k2, String v2, String k3, String v3) {
      return new AiSchematic.LegacyBlockState("minecraft:" + block, Map.of(k1, v1, k2, v2, k3, v3));
   }

   static AiSchematic.LegacyBlockState legacyBlock(int id, int data) {
      String color = DYE_COLORS[data & 15];
      String wood = WOOD_TYPES[Math.min(data & 7, 5)];
      AiSchematic.LegacyBlockState var10000;
      switch (id) {
         case 1:
            String var10;
            switch (data) {
               case 1:
                  var10 = "granite";
                  break;
               case 2:
                  var10 = "polished_granite";
                  break;
               case 3:
                  var10 = "diorite";
                  break;
               case 4:
                  var10 = "polished_diorite";
                  break;
               case 5:
                  var10 = "andesite";
                  break;
               case 6:
                  var10 = "polished_andesite";
                  break;
               default:
                  var10 = "stone";
            }

            var10000 = simpleBlock(var10);
            break;
         case 2:
            var10000 = simpleBlock("grass_block");
            break;
         case 3:
            var10000 = simpleBlock(data == 1 ? "coarse_dirt" : (data == 2 ? "podzol" : "dirt"));
            break;
         case 4:
            var10000 = simpleBlock("cobblestone");
            break;
         case 5:
            var10000 = simpleBlock(WOOD_TYPES[Math.min(data & 7, 5)] + "_planks");
            break;
         case 6:
            var10000 = init(WOOD_TYPES[Math.min(data & 7, 5)] + "_sapling", "stage", String.valueOf(data >> 3 & 1));
            break;
         case 7:
            var10000 = simpleBlock("bedrock");
            break;
         case 8:
         case 9:
            var10000 = init("water", "level", String.valueOf(data & 15));
            break;
         case 10:
         case 11:
            var10000 = init("lava", "level", String.valueOf(data & 15));
            break;
         case 12:
            var10000 = simpleBlock(data == 1 ? "red_sand" : "sand");
            break;
         case 13:
            var10000 = simpleBlock("gravel");
            break;
         case 14:
            var10000 = simpleBlock("gold_ore");
            break;
         case 15:
            var10000 = simpleBlock("iron_ore");
            break;
         case 16:
            var10000 = simpleBlock("coal_ore");
            break;
         case 17:
            var10000 = legacyLog(WOOD_TYPES[data & 3], data);
            break;
         case 18:
            var10000 = init(WOOD_TYPES[data & 3] + "_leaves", "persistent", (data & 4) != 0 ? "true" : "false");
            break;
         case 19:
            var10000 = simpleBlock(data == 1 ? "wet_sponge" : "sponge");
            break;
         case 20:
            var10000 = simpleBlock("glass");
            break;
         case 21:
            var10000 = simpleBlock("lapis_ore");
            break;
         case 22:
            var10000 = simpleBlock("lapis_block");
            break;
         case 23:
            var10000 = init("dispenser", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 24:
            var10000 = simpleBlock(data == 1 ? "chiseled_sandstone" : (data == 2 ? "cut_sandstone" : "sandstone"));
            break;
         case 25:
            var10000 = simpleBlock("note_block");
            break;
         case 26:
            var10000 = init("red_bed", "facing", HORIZONTAL_FACINGS[data & 3], "part", (data & 8) != 0 ? "head" : "foot");
            break;
         case 27:
            var10000 = sync("powered_rail", data);
            break;
         case 28:
            var10000 = sync("detector_rail", data);
            break;
         case 29:
            var10000 = init("sticky_piston", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 30:
            var10000 = simpleBlock("cobweb");
            break;
         case 31:
            var10000 = simpleBlock(data == 2 ? "fern" : "short_grass");
            break;
         case 32:
            var10000 = simpleBlock("dead_bush");
            break;
         case 33:
            var10000 = init("piston", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 34:
            var10000 = init("piston_head", "type", (data & 1) != 0 ? "sticky" : "normal");
            break;
         case 35:
            var10000 = simpleBlock(color + "_wool");
            break;
         case 36:
            var10000 = simpleBlock("moving_piston");
            break;
         case 37:
            var10000 = simpleBlock("dandelion");
            break;
         case 38:
            String var9;
            switch (data) {
               case 1:
                  var9 = "blue_orchid";
                  break;
               case 2:
                  var9 = "allium";
                  break;
               case 3:
                  var9 = "azure_bluet";
                  break;
               case 4:
                  var9 = "red_tulip";
                  break;
               case 5:
                  var9 = "orange_tulip";
                  break;
               case 6:
                  var9 = "white_tulip";
                  break;
               case 7:
                  var9 = "pink_tulip";
                  break;
               case 8:
                  var9 = "oxeye_daisy";
                  break;
               default:
                  var9 = "poppy";
            }

            var10000 = simpleBlock(var9);
            break;
         case 39:
            var10000 = simpleBlock("brown_mushroom");
            break;
         case 40:
            var10000 = simpleBlock("red_mushroom");
            break;
         case 41:
            var10000 = simpleBlock("gold_block");
            break;
         case 42:
            var10000 = simpleBlock("iron_block");
            break;
         case 43:
            var10000 = load(data, false);
            break;
         case 44:
            var10000 = legacySlab(data, false);
            break;
         case 45:
            var10000 = simpleBlock("bricks");
            break;
         case 46:
            var10000 = init("tnt", "unstable", (data & 1) != 0 ? "true" : "false");
            break;
         case 47:
            var10000 = simpleBlock("bookshelf");
            break;
         case 48:
            var10000 = simpleBlock("mossy_cobblestone");
            break;
         case 49:
            var10000 = simpleBlock("obsidian");
            break;
         case 50:
            var10000 = data == 5 ? simpleBlock("torch") : init("wall_torch", "facing", TORCH_FACINGS[Math.min(Math.max(data - 1, 0), 3)]);
            break;
         case 51:
            var10000 = simpleBlock("fire");
            break;
         case 52:
            var10000 = simpleBlock("spawner");
            break;
         case 53:
            var10000 = load("oak_stairs", data);
            break;
         case 54:
            var10000 = init("chest", "facing", legacyFacing(data));
            break;
         case 55:
            var10000 = init("redstone_wire", "power", String.valueOf(data & 15));
            break;
         case 56:
            var10000 = simpleBlock("diamond_ore");
            break;
         case 57:
            var10000 = simpleBlock("diamond_block");
            break;
         case 58:
            var10000 = simpleBlock("crafting_table");
            break;
         case 59:
            var10000 = init("wheat", "age", String.valueOf(data & 7));
            break;
         case 60:
            var10000 = init("farmland", "moisture", String.valueOf(data & 7));
            break;
         case 61:
            var10000 = init("furnace", "facing", legacyFacing(data));
            break;
         case 62:
            var10000 = init("furnace", "facing", legacyFacing(data), "lit", "true");
            break;
         case 63:
            var10000 = init("oak_sign", "rotation", String.valueOf(data & 15));
            break;
         case 64:
            var10000 = read("oak_door", data);
            break;
         case 65:
            var10000 = init("ladder", "facing", legacyFacing(data));
            break;
         case 66:
            var10000 = sync("rail", data);
            break;
         case 67:
            var10000 = load("cobblestone_stairs", data);
            break;
         case 68:
            var10000 = init("oak_wall_sign", "facing", legacyFacing(data));
            break;
         case 69:
            var10000 = legacyLever(data);
            break;
         case 70:
            var10000 = simpleBlock("stone_pressure_plate");
            break;
         case 71:
            var10000 = read("iron_door", data);
            break;
         case 72:
            var10000 = simpleBlock("oak_pressure_plate");
            break;
         case 73:
            var10000 = simpleBlock("redstone_ore");
            break;
         case 74:
            var10000 = init("redstone_ore", "lit", "true");
            break;
         case 75:
            var10000 = data == 5
               ? init("redstone_torch", "lit", "false")
               : init("redstone_wall_torch", "facing", TORCH_FACINGS[Math.min(Math.max(data - 1, 0), 3)], "lit", "false");
            break;
         case 76:
            var10000 = data == 5
               ? simpleBlock("redstone_torch")
               : init("redstone_wall_torch", "facing", TORCH_FACINGS[Math.min(Math.max(data - 1, 0), 3)]);
            break;
         case 77:
            var10000 = update("stone_button", data);
            break;
         case 78:
            var10000 = init("snow", "layers", String.valueOf(Math.min((data & 7) + 1, 8)));
            break;
         case 79:
            var10000 = simpleBlock("ice");
            break;
         case 80:
            var10000 = simpleBlock("snow_block");
            break;
         case 81:
            var10000 = init("cactus", "age", String.valueOf(data & 15));
            break;
         case 82:
            var10000 = simpleBlock("clay");
            break;
         case 83:
            var10000 = init("sugar_cane", "age", String.valueOf(data & 15));
            break;
         case 84:
            var10000 = simpleBlock("jukebox");
            break;
         case 85:
            var10000 = simpleBlock("oak_fence");
            break;
         case 86:
            var10000 = simpleBlock("pumpkin");
            break;
         case 87:
            var10000 = simpleBlock("netherrack");
            break;
         case 88:
            var10000 = simpleBlock("soul_sand");
            break;
         case 89:
            var10000 = simpleBlock("glowstone");
            break;
         case 90:
            var10000 = init("nether_portal", "axis", (data & 3) == 2 ? "z" : "x");
            break;
         case 91:
            var10000 = init("jack_o_lantern", "facing", HORIZONTAL_FACINGS[data & 3]);
            break;
         case 92:
            var10000 = init("cake", "bites", String.valueOf(Math.min(data & 7, 6)));
            break;
         case 93:
            var10000 = init("repeater", "facing", HORIZONTAL_FACINGS[data & 3], "delay", String.valueOf((data >> 2 & 3) + 1), "powered", "false");
            break;
         case 94:
            var10000 = init("repeater", "facing", HORIZONTAL_FACINGS[data & 3], "delay", String.valueOf((data >> 2 & 3) + 1), "powered", "true");
            break;
         case 95:
            var10000 = simpleBlock(color + "_stained_glass");
            break;
         case 96:
            var10000 = init("oak_trapdoor", "facing", flush[data & 3], "half", (data & 8) != 0 ? "top" : "bottom", "open", (data & 4) != 0 ? "true" : "false");
            break;
         case 97:
            String var8;
            switch (data) {
               case 1:
                  var8 = "infested_cobblestone";
                  break;
               case 2:
                  var8 = "infested_stone_bricks";
                  break;
               case 3:
                  var8 = "infested_mossy_stone_bricks";
                  break;
               case 4:
                  var8 = "infested_cracked_stone_bricks";
                  break;
               case 5:
                  var8 = "infested_chiseled_stone_bricks";
                  break;
               default:
                  var8 = "infested_stone";
            }

            var10000 = simpleBlock(var8);
            break;
         case 98:
            String var7;
            switch (data) {
               case 1:
                  var7 = "mossy_stone_bricks";
                  break;
               case 2:
                  var7 = "cracked_stone_bricks";
                  break;
               case 3:
                  var7 = "chiseled_stone_bricks";
                  break;
               default:
                  var7 = "stone_bricks";
            }

            var10000 = simpleBlock(var7);
            break;
         case 99:
            var10000 = simpleBlock("brown_mushroom_block");
            break;
         case 100:
            var10000 = simpleBlock("red_mushroom_block");
            break;
         case 101:
            var10000 = simpleBlock("iron_bars");
            break;
         case 102:
            var10000 = simpleBlock("glass_pane");
            break;
         case 103:
            var10000 = simpleBlock("melon");
            break;
         case 104:
            var10000 = init("pumpkin_stem", "age", String.valueOf(data & 7));
            break;
         case 105:
            var10000 = init("melon_stem", "age", String.valueOf(data & 7));
            break;
         case 106:
            var10000 = load(data);
            break;
         case 107:
            var10000 = write("oak_fence_gate", data);
            break;
         case 108:
            var10000 = load("brick_stairs", data);
            break;
         case 109:
            var10000 = load("stone_brick_stairs", data);
            break;
         case 110:
            var10000 = simpleBlock("mycelium");
            break;
         case 111:
            var10000 = simpleBlock("lily_pad");
            break;
         case 112:
            var10000 = simpleBlock("nether_bricks");
            break;
         case 113:
            var10000 = simpleBlock("nether_brick_fence");
            break;
         case 114:
            var10000 = load("nether_brick_stairs", data);
            break;
         case 115:
            var10000 = init("nether_wart", "age", String.valueOf(data & 3));
            break;
         case 116:
            var10000 = simpleBlock("enchanting_table");
            break;
         case 117:
            var10000 = simpleBlock("brewing_stand");
            break;
         case 118:
            var10000 = (data & 3) == 0 ? simpleBlock("cauldron") : init("water_cauldron", "level", String.valueOf(Math.min(data & 3, 3)));
            break;
         case 119:
            var10000 = simpleBlock("end_portal");
            break;
         case 120:
            var10000 = init("end_portal_frame", "facing", HORIZONTAL_FACINGS[data & 3], "eye", (data & 4) != 0 ? "true" : "false");
            break;
         case 121:
            var10000 = simpleBlock("end_stone");
            break;
         case 122:
            var10000 = simpleBlock("dragon_egg");
            break;
         case 123:
            var10000 = simpleBlock("redstone_lamp");
            break;
         case 124:
            var10000 = init("redstone_lamp", "lit", "true");
            break;
         case 125:
            var10000 = load(data, true);
            break;
         case 126:
            var10000 = legacySlab(data, true);
            break;
         case 127:
            var10000 = init("cocoa", "age", String.valueOf(Math.min(data >> 2 & 3, 2)), "facing", HORIZONTAL_FACINGS[data & 3]);
            break;
         case 128:
            var10000 = load("sandstone_stairs", data);
            break;
         case 129:
            var10000 = simpleBlock("emerald_ore");
            break;
         case 130:
            var10000 = init("ender_chest", "facing", legacyFacing(data));
            break;
         case 131:
            var10000 = init(
               "tripwire_hook",
               "facing",
               HORIZONTAL_FACINGS[data & 3],
               "attached",
               (data & 4) != 0 ? "true" : "false",
               "powered",
               (data & 8) != 0 ? "true" : "false"
            );
            break;
         case 132:
            var10000 = simpleBlock("tripwire");
            break;
         case 133:
            var10000 = simpleBlock("emerald_block");
            break;
         case 134:
            var10000 = load("spruce_stairs", data);
            break;
         case 135:
            var10000 = load("birch_stairs", data);
            break;
         case 136:
            var10000 = load("jungle_stairs", data);
            break;
         case 137:
            var10000 = init("command_block", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 138:
            var10000 = simpleBlock("beacon");
            break;
         case 139:
            var10000 = simpleBlock(data == 1 ? "mossy_cobblestone_wall" : "cobblestone_wall");
            break;
         case 140:
            var10000 = simpleBlock("flower_pot");
            break;
         case 141:
            var10000 = init("carrots", "age", String.valueOf(data & 7));
            break;
         case 142:
            var10000 = init("potatoes", "age", String.valueOf(data & 7));
            break;
         case 143:
            var10000 = update("oak_button", data);
            break;
         case 144:
            String var6;
            switch (data) {
               case 1:
                  var6 = "wither_skeleton_skull";
                  break;
               case 2:
                  var6 = "zombie_head";
                  break;
               case 3:
                  var6 = "player_head";
                  break;
               case 4:
                  var6 = "creeper_head";
                  break;
               case 5:
                  var6 = "dragon_head";
                  break;
               default:
                  var6 = "skeleton_skull";
            }

            var10000 = simpleBlock(var6);
            break;
         case 145:
            String var5;
            switch (data >> 2 & 3) {
               case 1:
                  var5 = "chipped_anvil";
                  break;
               case 2:
                  var5 = "damaged_anvil";
                  break;
               default:
                  var5 = "anvil";
            }

            var10000 = init(var5, "facing", close[data & 3]);
            break;
         case 146:
            var10000 = init("trapped_chest", "facing", legacyFacing(data));
            break;
         case 147:
            var10000 = init("light_weighted_pressure_plate", "power", String.valueOf(data & 15));
            break;
         case 148:
            var10000 = init("heavy_weighted_pressure_plate", "power", String.valueOf(data & 15));
            break;
         case 149:
            var10000 = init("comparator", "facing", HORIZONTAL_FACINGS[data & 3], "powered", "false");
            break;
         case 150:
            var10000 = init("comparator", "facing", HORIZONTAL_FACINGS[data & 3], "powered", "true");
            break;
         case 151:
            var10000 = simpleBlock("daylight_detector");
            break;
         case 152:
            var10000 = simpleBlock("redstone_block");
            break;
         case 153:
            var10000 = simpleBlock("nether_quartz_ore");
            break;
         case 154:
            var10000 = init("hopper", "facing", (data & 7) == 1 ? "down" : poll[Math.min(data & 7, 5)]);
            break;
         case 155:
            switch (data) {
               case 1:
                  return simpleBlock("chiseled_quartz_block");
               case 2:
                  return init("quartz_pillar", "axis", "y");
               case 3:
                  return init("quartz_pillar", "axis", "x");
               case 4:
                  return init("quartz_pillar", "axis", "z");
               default:
                  return simpleBlock("quartz_block");
            }
         case 156:
            var10000 = load("quartz_stairs", data);
            break;
         case 157:
            var10000 = sync("activator_rail", data);
            break;
         case 158:
            var10000 = init("dropper", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 159:
            var10000 = simpleBlock(color + "_terracotta");
            break;
         case 160:
            var10000 = simpleBlock(color + "_stained_glass_pane");
            break;
         case 161:
            var10000 = init((data & 1) != 0 ? "dark_oak_leaves" : "acacia_leaves", "persistent", (data & 4) != 0 ? "true" : "false");
            break;
         case 162:
            var10000 = legacyLog((data & 1) != 0 ? "dark_oak" : "acacia", data);
            break;
         case 163:
            var10000 = load("acacia_stairs", data);
            break;
         case 164:
            var10000 = load("dark_oak_stairs", data);
            break;
         case 165:
            var10000 = simpleBlock("slime_block");
            break;
         case 166:
            var10000 = simpleBlock("barrier");
            break;
         case 167:
            var10000 = init("iron_trapdoor", "facing", flush[data & 3], "half", (data & 8) != 0 ? "top" : "bottom", "open", (data & 4) != 0 ? "true" : "false");
            break;
         case 168:
            String var4;
            switch (data) {
               case 1:
                  var4 = "prismarine_bricks";
                  break;
               case 2:
                  var4 = "dark_prismarine";
                  break;
               default:
                  var4 = "prismarine";
            }

            var10000 = simpleBlock(var4);
            break;
         case 169:
            var10000 = simpleBlock("sea_lantern");
            break;
         case 170:
            String var12;
            switch (data & 12) {
               case 4:
                  var12 = "x";
                  break;
               case 8:
                  var12 = "z";
                  break;
               default:
                  var12 = "y";
            }

            var10000 = init("hay_block", "axis", var12);
            break;
         case 171:
            var10000 = simpleBlock(color + "_carpet");
            break;
         case 172:
            var10000 = simpleBlock("terracotta");
            break;
         case 173:
            var10000 = simpleBlock("coal_block");
            break;
         case 174:
            var10000 = simpleBlock("packed_ice");
            break;
         case 175:
            var10000 = read(data);
            break;
         case 176:
            var10000 = init("white_banner", "rotation", String.valueOf(data & 15));
            break;
         case 177:
            var10000 = init("white_wall_banner", "facing", legacyFacing(data));
            break;
         case 178:
            var10000 = init("daylight_detector", "inverted", "true");
            break;
         case 179:
            var10000 = simpleBlock(data == 1 ? "chiseled_red_sandstone" : (data == 2 ? "cut_red_sandstone" : "red_sandstone"));
            break;
         case 180:
            var10000 = load("red_sandstone_stairs", data);
            break;
         case 181:
            var10000 = init("red_sandstone_slab", "type", "double");
            break;
         case 182:
            var10000 = init("red_sandstone_slab", "type", (data & 8) != 0 ? "top" : "bottom");
            break;
         case 183:
            var10000 = write("spruce_fence_gate", data);
            break;
         case 184:
            var10000 = write("birch_fence_gate", data);
            break;
         case 185:
            var10000 = write("jungle_fence_gate", data);
            break;
         case 186:
            var10000 = write("dark_oak_fence_gate", data);
            break;
         case 187:
            var10000 = write("acacia_fence_gate", data);
            break;
         case 188:
            var10000 = simpleBlock("spruce_fence");
            break;
         case 189:
            var10000 = simpleBlock("birch_fence");
            break;
         case 190:
            var10000 = simpleBlock("jungle_fence");
            break;
         case 191:
            var10000 = simpleBlock("dark_oak_fence");
            break;
         case 192:
            var10000 = simpleBlock("acacia_fence");
            break;
         case 193:
            var10000 = read("spruce_door", data);
            break;
         case 194:
            var10000 = read("birch_door", data);
            break;
         case 195:
            var10000 = read("jungle_door", data);
            break;
         case 196:
            var10000 = read("acacia_door", data);
            break;
         case 197:
            var10000 = read("dark_oak_door", data);
            break;
         case 198:
            var10000 = init("end_rod", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 199:
            var10000 = simpleBlock("chorus_plant");
            break;
         case 200:
            var10000 = init("chorus_flower", "age", String.valueOf(Math.min(data & 7, 5)));
            break;
         case 201:
            var10000 = simpleBlock("purpur_block");
            break;
         case 202:
            String var11;
            switch (data & 12) {
               case 4:
                  var11 = "x";
                  break;
               case 8:
                  var11 = "z";
                  break;
               default:
                  var11 = "y";
            }

            var10000 = init("purpur_pillar", "axis", var11);
            break;
         case 203:
            var10000 = load("purpur_stairs", data);
            break;
         case 204:
            var10000 = init("purpur_slab", "type", "double");
            break;
         case 205:
            var10000 = init("purpur_slab", "type", (data & 8) != 0 ? "top" : "bottom");
            break;
         case 206:
            var10000 = simpleBlock("end_stone_bricks");
            break;
         case 207:
            var10000 = init("beetroots", "age", String.valueOf(data & 3));
            break;
         case 208:
            var10000 = simpleBlock("dirt_path");
            break;
         case 209:
            var10000 = simpleBlock("end_gateway");
            break;
         case 210:
            var10000 = init("repeating_command_block", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 211:
            var10000 = init("chain_command_block", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 212:
            var10000 = init("frosted_ice", "age", String.valueOf(data & 3));
            break;
         case 213:
            var10000 = simpleBlock("magma_block");
            break;
         case 214:
            var10000 = simpleBlock("nether_wart_block");
            break;
         case 215:
            var10000 = simpleBlock("red_nether_bricks");
            break;
         case 216:
            String var10002;
            switch (data & 12) {
               case 4:
                  var10002 = "x";
                  break;
               case 8:
                  var10002 = "z";
                  break;
               default:
                  var10002 = "y";
            }

            var10000 = init("bone_block", "axis", var10002);
            break;
         case 217:
            var10000 = simpleBlock("structure_void");
            break;
         case 218:
            var10000 = init("observer", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 219:
         case 220:
         case 221:
         case 222:
         case 223:
         case 224:
         case 225:
         case 226:
         case 227:
         case 228:
         case 229:
         case 230:
         case 231:
         case 232:
         case 233:
         case 234:
            var10000 = init(DYE_COLORS[id - 219] + "_shulker_box", "facing", poll[Math.min(data & 7, 5)]);
            break;
         case 235:
         case 236:
         case 237:
         case 238:
         case 239:
         case 240:
         case 241:
         case 242:
         case 243:
         case 244:
         case 245:
         case 246:
         case 247:
         case 248:
         case 249:
         case 250:
            var10000 = init(DYE_COLORS[id - 235] + "_glazed_terracotta", "facing", HORIZONTAL_FACINGS[data & 3]);
            break;
         case 251:
            var10000 = simpleBlock(color + "_concrete");
            break;
         case 252:
            var10000 = simpleBlock(color + "_concrete_powder");
            break;
         case 253:
         case 254:
         default:
            var10000 = null;
            break;
         case 255:
            var10000 = simpleBlock("structure_block");
      }

      return var10000;
   }

   private static AiSchematic.LegacyBlockState legacyLog(String type, int data) {
      AiSchematic.LegacyBlockState var10000;
      switch (data & 12) {
         case 4:
            var10000 = init(type + "_log", "axis", "x");
            break;
         case 8:
            var10000 = init(type + "_log", "axis", "z");
            break;
         case 12:
            var10000 = simpleBlock(type + "_wood");
            break;
         default:
            var10000 = init(type + "_log", "axis", "y");
      }

      return var10000;
   }

   private static AiSchematic.LegacyBlockState load(String block, int data) {
      return init(block, "facing", TORCH_FACINGS[data & 3], "half", (data & 4) != 0 ? "top" : "bottom");
   }

   private static AiSchematic.LegacyBlockState legacySlab(int data, boolean wooden) {
      String[] stone = new String[]{
         "stone_slab", "sandstone_slab", "petrified_oak_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"
      };
      String block = wooden ? WOOD_TYPES[Math.min(data & 7, 5)] + "_slab" : stone[data & 7];
      return init(block, "type", (data & 8) != 0 ? "top" : "bottom");
   }

   private static AiSchematic.LegacyBlockState load(int data, boolean wooden) {
      if (!wooden && (data & 8) != 0) {
         String var10000;
         switch (data & 7) {
            case 1:
               var10000 = "smooth_sandstone";
               break;
            case 7:
               var10000 = "smooth_quartz";
               break;
            default:
               var10000 = "smooth_stone";
         }

         return simpleBlock(var10000);
      } else {
         String[] stone = new String[]{
            "stone_slab", "sandstone_slab", "petrified_oak_slab", "cobblestone_slab", "brick_slab", "stone_brick_slab", "nether_brick_slab", "quartz_slab"
         };
         String block = wooden ? WOOD_TYPES[Math.min(data & 7, 5)] + "_slab" : stone[data & 7];
         return init(block, "type", "double");
      }
   }

   private static AiSchematic.LegacyBlockState read(String block, int data) {
      return (data & 8) != 0
         ? init(block, "half", "upper", "hinge", (data & 1) != 0 ? "right" : "left")
         : init(block, "facing", sync[data & 3], "half", "lower", "open", (data & 4) != 0 ? "true" : "false");
   }

   private static AiSchematic.LegacyBlockState write(String block, int data) {
      return init(block, "facing", HORIZONTAL_FACINGS[data & 3], "open", (data & 4) != 0 ? "true" : "false");
   }

   private static AiSchematic.LegacyBlockState legacyLever(int data) {
      String powered = (data & 8) != 0 ? "true" : "false";
      AiSchematic.LegacyBlockState var10000;
      switch (data & 7) {
         case 0:
            var10000 = init("lever", "face", "ceiling", "facing", "east", "powered", powered);
            break;
         case 1:
            var10000 = init("lever", "face", "wall", "facing", "east", "powered", powered);
            break;
         case 2:
            var10000 = init("lever", "face", "wall", "facing", "west", "powered", powered);
            break;
         case 3:
            var10000 = init("lever", "face", "wall", "facing", "south", "powered", powered);
            break;
         case 4:
            var10000 = init("lever", "face", "wall", "facing", "north", "powered", powered);
            break;
         case 5:
            var10000 = init("lever", "face", "floor", "facing", "north", "powered", powered);
            break;
         case 6:
            var10000 = init("lever", "face", "floor", "facing", "west", "powered", powered);
            break;
         default:
            var10000 = init("lever", "face", "ceiling", "facing", "north", "powered", powered);
      }

      return var10000;
   }

   private static AiSchematic.LegacyBlockState update(String block, int data) {
      String powered = (data & 8) != 0 ? "true" : "false";
      AiSchematic.LegacyBlockState var10000;
      switch (data & 7) {
         case 1:
            var10000 = init(block, "face", "wall", "facing", "east", "powered", powered);
            break;
         case 2:
            var10000 = init(block, "face", "wall", "facing", "west", "powered", powered);
            break;
         case 3:
            var10000 = init(block, "face", "wall", "facing", "south", "powered", powered);
            break;
         case 4:
            var10000 = init(block, "face", "wall", "facing", "north", "powered", powered);
            break;
         case 5:
            var10000 = init(block, "face", "floor", "facing", "north", "powered", powered);
            break;
         default:
            var10000 = init(block, "face", "ceiling", "facing", "north", "powered", powered);
      }

      return var10000;
   }

   private static AiSchematic.LegacyBlockState sync(String block, int data) {
      String[] shapes = new String[]{
         "north_south",
         "east_west",
         "ascending_east",
         "ascending_west",
         "ascending_north",
         "ascending_south",
         "south_east",
         "south_west",
         "north_west",
         "north_east"
      };
      if (block.equals("rail")) {
         return init(block, "shape", shapes[Math.min(data & 15, 9)]);
      }

      String shape = shapes[Math.min(data & 7, 5)];
      return block.equals("powered_rail") ? init(block, "shape", shape, "powered", (data & 8) != 0 ? "true" : "false") : init(block, "shape", shape);
   }

   private static AiSchematic.LegacyBlockState load(int data) {
      Map<String, String> properties = new LinkedHashMap<>();
      properties.put("south", (data & 1) != 0 ? "true" : "false");
      properties.put("west", (data & 2) != 0 ? "true" : "false");
      properties.put("north", (data & 4) != 0 ? "true" : "false");
      properties.put("east", (data & 8) != 0 ? "true" : "false");
      properties.put("up", "false");
      return new AiSchematic.LegacyBlockState("minecraft:vine", Map.copyOf(properties));
   }

   private static AiSchematic.LegacyBlockState read(int data) {
      String var10000;
      switch (data & 7) {
         case 1:
            var10000 = "lilac";
            break;
         case 2:
            var10000 = "tall_grass";
            break;
         case 3:
            var10000 = "large_fern";
            break;
         case 4:
            var10000 = "rose_bush";
            break;
         case 5:
            var10000 = "peony";
            break;
         default:
            var10000 = "sunflower";
      }

      String block = var10000;
      return init(block, "half", (data & 8) != 0 ? "upper" : "lower");
   }

   record LoadedSchematic(
      AiBuilder.Blueprint blueprint,
      Map<Long, String> signText,
      Map<String, Integer> entities,
      Map<Long, ListTag> containerContents,
      List<AiSchematic.RailVehicleSpec> railVehicles,
      List<AiSchematic.ItemFrameSpec> itemFrames
   ) {
      LoadedSchematic(
         AiBuilder.Blueprint blueprint,
         Map<Long, String> signText,
         Map<String, Integer> entities,
         Map<Long, ListTag> containerContents,
         List<AiSchematic.RailVehicleSpec> railVehicles
      ) {
         this(blueprint, signText, entities, containerContents, railVehicles, List.of());
      }

      LoadedSchematic(AiBuilder.Blueprint blueprint, Map<Long, String> signText, Map<String, Integer> entities, Map<Long, ListTag> containerContents) {
         this(blueprint, signText, entities, containerContents, List.of());
      }
   }

   record LegacyBlockState(String block, Map<String, String> properties) {
   }

   record PaletteState(String block, Map<String, String> properties) {
   }

   record DecodedFile(
      List<AiBuilder.CellSpec> cells,
      Set<Integer> unmapped,
      int airCells,
      String format,
      Map<Long, String> signText,
      Map<String, Integer> entities,
      Map<Long, ListTag> containerContents,
      List<AiSchematic.RailVehicleSpec> railVehicles,
      List<AiSchematic.ItemFrameSpec> itemFrames
   ) {
      DecodedFile(
         List<AiBuilder.CellSpec> cells,
         Set<Integer> unmapped,
         int airCells,
         String format,
         Map<Long, String> signText,
         Map<String, Integer> entities,
         Map<Long, ListTag> containerContents,
         List<AiSchematic.RailVehicleSpec> railVehicles
      ) {
         this(cells, unmapped, airCells, format, signText, entities, containerContents, railVehicles, List.of());
      }

      DecodedFile(
         List<AiBuilder.CellSpec> cells,
         Set<Integer> unmapped,
         int airCells,
         String format,
         Map<Long, String> signText,
         Map<String, Integer> entities,
         Map<Long, ListTag> containerContents
      ) {
         this(cells, unmapped, airCells, format, signText, entities, containerContents, List.of());
      }

      DecodedFile(
         List<AiBuilder.CellSpec> cells, Set<Integer> unmapped, int airCells, String format, Map<Long, String> signText, Map<String, Integer> entities
      ) {
         this(cells, unmapped, airCells, format, signText, entities, Map.of());
      }

      DecodedFile(List<AiBuilder.CellSpec> cells, Set<Integer> unmapped, int airCells, String format) {
         this(cells, unmapped, airCells, format, Map.of(), Map.of());
      }

      DecodedFile(List<AiBuilder.CellSpec> cells, Set<Integer> unmapped, int airCells, String format, Map<Long, String> signText) {
         this(cells, unmapped, airCells, format, signText, Map.of());
      }
   }

   record ItemFrameSpec(String id, BlockPos cell, Direction facing, CompoundTag item, int rotation, boolean invisible, boolean fixed) {
      BlockPos support() {
         return this.cell.relative(this.facing.getOpposite());
      }
   }

   record RailVehicleSpec(String id, Vec3 position) {
      BlockPos rail() {
         return BlockPos.containing(this.position);
      }
   }

   static final class UnknownBlock extends IllegalArgumentException {
      UnknownBlock(String message) {
         super(message);
      }
   }
}
