package autismclient.ai;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CancellationException;
import java.util.function.IntConsumer;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

final class SchematicDocument {
   static final long init_J = 134217728L;
   static final long load_J = 268435456L;
   static final int init_I = 2097152;
   final List<SchematicDocument.Region> regions;
   final List<SchematicDocument.BlockEntityData> blockEntities;
   final List<SchematicDocument.EntityData> entities;
   final int sizeX;
   final int sizeY;
   final int sizeZ;
   final long volume;
   private final Long2ObjectOpenHashMap<SchematicDocument.Region[]> regionsBySection = new Long2ObjectOpenHashMap<>();
   private volatile long cachedBlockCount = -1L;

   static int bitsForPaletteSize(int size) {
      return Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, size - 1)));
   }

   static void pack(long[] packed, int index, int bits, int id) {
      long bit = (long)index * bits;
      int word = (int)(bit >>> 6);
      int shift = (int)(bit & 63L);
      packed[word] |= (long)id << shift;
      if (shift + bits > 64) {
         packed[word + 1] = packed[word + 1] | (long)id >>> 64 - shift;
      }
   }

   SchematicDocument(List<SchematicDocument.Region> raw, List<SchematicDocument.BlockEntityData> bes, List<SchematicDocument.EntityData> entities) {
      if (!raw.isEmpty() && raw.size() <= 8192) {
         int minX = 2147483647;
         int minY = minX;
         int minZ = minX;
         int maxX = -2147483648;
         int maxY = maxX;
         int maxZ = maxX;
         long volume = 0L;

         for (SchematicDocument.Region r : raw) {
            volume += r.cellCount;
            if (volume > 134217728L) {
               throw new IllegalArgumentException("Schematic exceeds the 128 million voxel limit");
            }

            minX = Math.min(minX, r.originX);
            minY = Math.min(minY, r.originY);
            minZ = Math.min(minZ, r.originZ);
            maxX = Math.max(maxX, Math.addExact(r.originX, r.width));
            maxY = Math.max(maxY, Math.addExact(r.originY, r.height));
            maxZ = Math.max(maxZ, Math.addExact(r.originZ, r.length));
         }

         this.sizeX = Math.subtractExact(maxX, minX);
         this.sizeY = Math.subtractExact(maxY, minY);
         this.sizeZ = Math.subtractExact(maxZ, minZ);
         if (this.sizeX <= 4096 && this.sizeY <= 4096 && this.sizeZ <= 4096) {
            this.volume = volume;
            ArrayList<SchematicDocument.Region> normalized = new ArrayList<>();

            for (SchematicDocument.Region r : raw) {
               normalized.add(
                  new SchematicDocument.Region(
                     r.originX - minX, r.originY - minY, r.originZ - minZ, r.width, r.height, r.length, r.palette, r.packed, r.values
                  )
               );
            }

            this.regions = List.copyOf(normalized);
            ArrayList<SchematicDocument.BlockEntityData> normalizedBes = new ArrayList<>();

            for (SchematicDocument.BlockEntityData be : bes) {
               normalizedBes.add(new SchematicDocument.BlockEntityData(be.position.offset(-minX, -minY, -minZ), be.data));
            }

            this.blockEntities = List.copyOf(normalizedBes);
            ArrayList<SchematicDocument.EntityData> normalizedEntities = new ArrayList<>();

            for (SchematicDocument.EntityData e : entities) {
               normalizedEntities.add(new SchematicDocument.EntityData(e.x - minX, e.y - minY, e.z - minZ, e.data));
            }

            this.entities = List.copyOf(normalizedEntities);
            Long2ObjectOpenHashMap<ArrayList<SchematicDocument.Region>> build = new Long2ObjectOpenHashMap<>();

            for (SchematicDocument.Region r : this.regions) {
               for (int sx = r.originX >> 4; sx <= r.originX + r.width - 1 >> 4; sx++) {
                  for (int sy = r.originY >> 4; sy <= r.originY + r.height - 1 >> 4; sy++) {
                     for (int sz = r.originZ >> 4; sz <= r.originZ + r.length - 1 >> 4; sz++) {
                        long key = BlockPos.asLong(sx, sy, sz);
                        build.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                        if (build.size() > 262144) {
                           throw new IllegalArgumentException("Schematic section index is too large");
                        }
                     }
                  }
               }
            }

            build.forEach((keyx, value) -> this.regionsBySection.put(keyx, value.toArray(SchematicDocument.Region[]::new)));
         } else {
            throw new IllegalArgumentException("Schematic span exceeds 4096 blocks");
         }
      } else {
         throw new IllegalArgumentException("Schematic has no regions or too many regions");
      }
   }

   SchematicDocument.Region regionAt(int x, int y, int z) {
      SchematicDocument.Region[] candidates = this.regionsBySection.get(BlockPos.asLong(x >> 4, y >> 4, z >> 4));
      if (candidates != null) {
         for (int i = candidates.length - 1; i >= 0; i--) {
            if (candidates[i].contains(x, y, z)) {
               return candidates[i];
            }
         }
      }

      return null;
   }

   BlockState blockAt(int x, int y, int z) {
      SchematicDocument.Region r = this.regionAt(x, y, z);
      return r == null ? Blocks.AIR.defaultBlockState() : r.stateAt(x, y, z);
   }

   boolean unusedHasRegionAt(int x, int y, int z) {
      return this.regionAt(x, y, z) != null;
   }

   long countBlocks(IntConsumer progress) {
      if (this.cachedBlockCount >= 0L) {
         return this.cachedBlockCount;
      }

      long count = 0L;
      long done = 0L;

      for (SchematicDocument.Region r : this.regions) {
         for (int i = 0; i < r.cellCount; i++) {
            if ((i & 4095) == 0) {
               checkInterrupted();
               progress.accept((int)(100L * done / Math.max(1L, this.volume)));
            }

            int y = i / (r.width * r.length);
            int rest = i % (r.width * r.length);
            int z = rest / r.width;
            int x = rest % r.width;
            if (!r.stateAtIndex(i).isAir() && this.regionAt(r.originX + x, r.originY + y, r.originZ + z) == r) {
               count++;
            }

            done++;
         }
      }

      this.cachedBlockCount = count;
      progress.accept(100);
      return count;
   }

   SchematicDocument.Preview buildPreview(int rotation, String mirror, IntConsumer progress) {
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>();
      long count = 0L;
      long done = 0L;
      int minX = 2147483647;
      int minY = minX;
      int minZ = minX;
      IdentityHashMap<BlockState, AiBuilder.CellSpec> cache = new IdentityHashMap<>();

      for (SchematicDocument.Region r : this.regions) {
         for (int i = 0; i < r.cellCount; i++) {
            if ((i & 4095) == 0) {
               checkInterrupted();
               progress.accept((int)(100L * done / Math.max(1L, this.volume)));
            }

            done++;
            BlockState s = r.stateAtIndex(i);
            if (!s.isAir()) {
               int y = r.originY + i / (r.width * r.length);
               int rest = i % (r.width * r.length);
               int z = r.originZ + rest / r.width;
               int x = r.originX + rest % r.width;
               if (this.regionAt(x, y, z) == r) {
                  count++;
                  BlockPos at = this.transform(x, y, z, rotation, mirror);
                  minX = Math.min(minX, at.getX());
                  minY = Math.min(minY, at.getY());
                  minZ = Math.min(minZ, at.getZ());
                  if (!s.isSolidRender()
                     || !this.blockAt(x - 1, y, z).isSolidRender()
                     || !this.blockAt(x + 1, y, z).isSolidRender()
                     || !this.blockAt(x, y - 1, z).isSolidRender()
                     || !this.blockAt(x, y + 1, z).isSolidRender()
                     || !this.blockAt(x, y, z - 1).isSolidRender()
                     || !this.blockAt(x, y, z + 1).isSolidRender()) {
                     if (cells.size() >= 2097152) {
                        throw new IllegalArgumentException("Too many exposed blocks for a hologram; single-player Paste is still available");
                     }

                     AiBuilder.CellSpec template = cache.computeIfAbsent(
                        s, k -> cellSpecOf(transformState(k, rotation, mirror))
                     );
                     cells.add(new AiBuilder.CellSpec(at.getX(), at.getY(), at.getZ(), template.block(), template.properties()));
                  }
               }
            }
         }
      }

      this.cachedBlockCount = count;
      progress.accept(100);
      return new SchematicDocument.Preview(List.copyOf(cells), count == 0L ? BlockPos.ZERO : new BlockPos(minX, minY, minZ), count);
   }

   boolean isSolidAt(BlockPos at, int rotation, String mirror) {
      BlockPos source = this.sourcePosFor(at.getX(), at.getY(), at.getZ(), rotation, mirror);
      return this.blockAt(source.getX(), source.getY(), source.getZ()).isSolidRender();
   }

   int rotatedSizeX(int rotation, String mirror) {
      return rotation % 180 == 0 ? this.sizeX : this.sizeZ;
   }

   int rotatedSizeZ(int rotation) {
      return rotation % 180 == 0 ? this.sizeZ : this.sizeX;
   }

   BlockPos transform(int x, int y, int z, int rotation, String mirror) {
      if (mirror.equals("x")) {
         x = this.sizeX - 1 - x;
      } else if (mirror.equals("z")) {
         z = this.sizeZ - 1 - z;
      }

      BlockPos var10000;
      switch (Math.floorMod(rotation, 360)) {
         case 90:
            var10000 = new BlockPos(this.sizeZ - 1 - z, y, x);
            break;
         case 180:
            var10000 = new BlockPos(this.sizeX - 1 - x, y, this.sizeZ - 1 - z);
            break;
         case 270:
            var10000 = new BlockPos(z, y, this.sizeX - 1 - x);
            break;
         default:
            var10000 = new BlockPos(x, y, z);
      }

      return var10000;
   }

   BlockPos sourcePosFor(int x, int y, int z, int rotation, String mirror) {
      int sx;
      int sz;
      switch (Math.floorMod(rotation, 360)) {
         case 90:
            sx = z;
            sz = this.sizeZ - 1 - x;
            break;
         case 180:
            sx = this.sizeX - 1 - x;
            sz = this.sizeZ - 1 - z;
            break;
         case 270:
            sx = this.sizeX - 1 - z;
            sz = x;
            break;
         default:
            sx = x;
            sz = z;
      }

      if (mirror.equals("x")) {
         sx = this.sizeX - 1 - sx;
      } else if (mirror.equals("z")) {
         sz = this.sizeZ - 1 - sz;
      }

      return new BlockPos(sx, y, sz);
   }

   static BlockState transformState(BlockState state, int rotation, String mirror) {
      Mirror m = mirror.equals("x") ? Mirror.FRONT_BACK : (mirror.equals("z") ? Mirror.LEFT_RIGHT : Mirror.NONE);
      Rotation var10000;
      switch (Math.floorMod(rotation, 360)) {
         case 90:
            var10000 = Rotation.CLOCKWISE_90;
            break;
         case 180:
            var10000 = Rotation.CLOCKWISE_180;
            break;
         case 270:
            var10000 = Rotation.COUNTERCLOCKWISE_90;
            break;
         default:
            var10000 = Rotation.NONE;
      }

      Rotation r = var10000;
      BlockState mirrored = state.mirror(m);
      // Vanilla keeps a chest's half when mirroring, which splits a double chest; swap it like the build does.
      if (m != Mirror.NONE && mirrored.getBlock() instanceof ChestBlock) {
         mirrored = mirrored.setValue(ChestBlock.TYPE, mirrored.getValue(ChestBlock.TYPE).getOpposite());
      }

      return mirrored.rotate(r);
   }

   static AiBuilder.CellSpec cellSpecOf(BlockState state) {
      TreeMap<String, String> properties = new TreeMap<>();

      for (Property<?> p : state.getProperties()) {
         properties.put(p.getName(), propertyValue(state, p));
      }

      return new AiBuilder.CellSpec(0, 0, 0, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), Map.copyOf(properties));
   }

   private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }

   static void checkInterrupted() {
      if (Thread.currentThread().isInterrupted()) {
         throw new CancellationException();
      }
   }

   static SchematicDocument loadFile(Path file) {
      CompoundTag root = AiSchematic.readNbt(file, 268435456L);
      if (root.getCompound("Regions").isPresent()) {
         return parseLitematic(root);
      }

      CompoundTag body = root.getCompound("Schematic").orElse(root);
      if (!body.getCompound("Palette").isPresent() && !body.getCompound("Blocks").flatMap(b -> b.getCompound("Palette")).isPresent()) {
         AiSchematic.DecodedFile decoded = AiSchematic.decodeSponge(body, false);
         int minX = body.getIntOr("WEOffsetX", 0);
         int minY = body.getIntOr("WEOffsetY", 0);
         int minZ = body.getIntOr("WEOffsetZ", 0);
         int w = body.getShortOr("Width", (short)0) & '\uffff';
         int h = body.getShortOr("Height", (short)0) & '\uffff';
         int l = body.getShortOr("Length", (short)0) & '\uffff';
         if ((long)w * h * l > 134217728L) {
            throw new IllegalArgumentException("Schematic is too large");
         }

         int[] ids = new int[w * h * l];
         ArrayList<BlockState> palette = new ArrayList<>();
         palette.add(Blocks.AIR.defaultBlockState());
         IdentityHashMap<BlockState, Integer> lookup = new IdentityHashMap<>();
         lookup.put(palette.get(0), 0);

         for (AiBuilder.CellSpec c : decoded.cells()) {
            BlockState s = AiBuilder.toBlockState(c);
            int id = lookup.computeIfAbsent(s, k -> {
               palette.add(k);
               return palette.size() - 1;
            });
            ids[((c.dy() - minY) * l + c.dz() - minZ) * w + c.dx() - minX] = id;
         }

         return new SchematicDocument(List.of(new SchematicDocument.Region(minX, minY, minZ, w, h, l, palette.toArray(BlockState[]::new), null, ids)), List.of(), List.of());
      } else {
         return parseSponge(body);
      }
   }

   static SchematicDocument parseLitematic(CompoundTag root) {
      int version = root.getIntOr("Version", 0);
      if (version >= 1 && version <= 7) {
         int dataVersion = root.getIntOr("MinecraftDataVersion", 1631);
         CompoundTag data = root.getCompoundOrEmpty("Regions");
         ArrayList<SchematicDocument.Region> regions = new ArrayList<>();
         ArrayList<SchematicDocument.BlockEntityData> bes = new ArrayList<>();
         ArrayList<SchematicDocument.EntityData> entities = new ArrayList<>();

         for (String name : data.keySet()) {
            checkInterrupted();
            CompoundTag r = data.getCompoundOrEmpty(name);
            CompoundTag p = r.getCompoundOrEmpty("Position");
            CompoundTag s = r.getCompoundOrEmpty("Size");
            int sx = s.getIntOr("x", 0);
            int sy = s.getIntOr("y", 0);
            int sz = s.getIntOr("z", 0);
            if (sx != 0 && sy != 0 && sz != 0) {
               int px = p.getIntOr("x", 0);
               int py = p.getIntOr("y", 0);
               int pz = p.getIntOr("z", 0);
               int x = sx < 0 ? Math.addExact(px, sx + 1) : px;
               int y = sy < 0 ? Math.addExact(py, sy + 1) : py;
               int z = sz < 0 ? Math.addExact(pz, sz + 1) : pz;
               ListTag entries = r.getListOrEmpty("BlockStatePalette");
               if (entries.isEmpty() || entries.size() > 65536) {
                  throw new IllegalArgumentException("Invalid Litematic palette size");
               }

               BlockState[] palette = new BlockState[entries.size()];

               for (int i = 0; i < palette.length; i++) {
                  AiSchematic.PaletteState parsed = AiSchematic.parsePaletteEntry(entries.getCompoundOrEmpty(i), dataVersion, version < 4 || dataVersion < 1631, "region " + name);
                  palette[i] = AiBuilder.toBlockState(new AiBuilder.CellSpec(0, 0, 0, parsed.block(), parsed.properties()));
               }

               regions.add(
                  new SchematicDocument.Region(
                     x,
                     y,
                     z,
                     Math.abs(sx),
                     Math.abs(sy),
                     Math.abs(sz),
                     palette,
                     r.getLongArray("BlockStates").orElseThrow(() -> new IllegalArgumentException("Missing BlockStates")),
                     null
                  )
               );

               for (Tag tag : r.getListOrEmpty("TileEntities")) {
                  if (tag instanceof CompoundTag be) {
                     BlockPos at = new BlockPos(
                        Math.addExact(x, be.getIntOr("x", 0)), Math.addExact(y, be.getIntOr("y", 0)), Math.addExact(z, be.getIntOr("z", 0))
                     );
                     bes.add(new SchematicDocument.BlockEntityData(at, updateTag(be, dataVersion, References.BLOCK_ENTITY)));
                  }
               }

               readEntities(r.getListOrEmpty("Entities"), px, py, pz, dataVersion, entities);
            }
         }

         return new SchematicDocument(regions, bes, entities);
      } else {
         throw new IllegalArgumentException("Unsupported Litematic version " + version);
      }
   }

   static SchematicDocument parseSponge(CompoundTag body) {
      int version = body.getIntOr("Version", 1);
      if (version >= 1 && version <= 3) {
         int w = body.getShortOr("Width", (short)0) & '\uffff';
         int h = body.getShortOr("Height", (short)0) & '\uffff';
         int l = body.getShortOr("Length", (short)0) & '\uffff';
         long volume = (long)w * h * l;
         if (w >= 1 && h >= 1 && l >= 1 && volume <= 134217728L) {
            int dataVersion = body.getIntOr("DataVersion", 1631);
            CompoundTag blockData = body.getCompound("Blocks").orElse(body);
            CompoundTag paletteTag = blockData.getCompoundOrEmpty("Palette");
            ArrayList<BlockState> palette = new ArrayList<>();
            HashMap<Integer, Integer> ids = new HashMap<>();

            for (String state : paletteTag.keySet()) {
               int id = paletteTag.getIntOr(state, -1);
               if (id < 0 || ids.containsKey(id)) {
                  throw new IllegalArgumentException("Invalid Sponge palette id");
               }

               AiSchematic.PaletteState parsed = AiSchematic.parseState(state, dataVersion);
               ids.put(id, palette.size());
               palette.add(AiBuilder.toBlockState(new AiBuilder.CellSpec(0, 0, 0, parsed.block(), parsed.properties())));
            }

            byte[] raw = blockData.getByteArray("Data")
               .orElseGet(() -> blockData.getByteArray("BlockData").orElseThrow(() -> new IllegalArgumentException("Missing Sponge block data")));
            if (!palette.isEmpty() && palette.size() <= 65536) {
               int bits = bitsForPaletteSize(palette.size());
               long[] packed = new long[(int)((volume * bits + 63L) / 64L)];
               int cursor = 0;
               int i = 0;

               label82:
               while (i < volume) {
                  if ((i & 4095) == 0) {
                     checkInterrupted();
                  }

                  int value = 0;

                  for (int shift = 0; cursor < raw.length && shift < 35; shift += 7) {
                     int next = raw[cursor++] & 255;
                     if (shift == 28 && (next & 248) != 0) {
                        throw new IllegalArgumentException("Invalid Sponge palette index");
                     }

                     value |= (next & 127) << shift;
                     if ((next & 128) == 0) {
                        Integer mapped = ids.get(value);
                        if (mapped == null) {
                           throw new IllegalArgumentException("Unknown Sponge palette index " + value);
                        }

                        pack(packed, i, bits, mapped);
                        i++;
                        continue label82;
                     }
                  }

                  throw new IllegalArgumentException("Invalid Sponge block data");
               }

               if (cursor != raw.length) {
                  throw new IllegalArgumentException("Unexpected Sponge block data");
               }

               ArrayList<SchematicDocument.BlockEntityData> bes = new ArrayList<>();
               ListTag tags = blockData.getListOrEmpty("BlockEntities");
               if (tags.isEmpty()) {
                  tags = body.getListOrEmpty("BlockEntities");
               }

               for (Tag tag : tags) {
                  if (tag instanceof CompoundTag entry) {
                     int[] p = entry.getIntArray("Pos").orElse(null);
                     if (p != null && p.length == 3) {
                        CompoundTag be = entry.getCompound("Data").orElse(entry).copy();
                        String id = entry.getStringOr("Id", be.getStringOr("id", ""));
                        if (!id.isEmpty()) {
                           be.putString("id", id);
                        }

                        bes.add(new SchematicDocument.BlockEntityData(new BlockPos(p[0], p[1], p[2]), updateTag(be, dataVersion, References.BLOCK_ENTITY)));
                     }
                  }
               }

               ArrayList<SchematicDocument.EntityData> entities = new ArrayList<>();
               readEntities(body.getListOrEmpty("Entities"), 0.0, 0.0, 0.0, dataVersion, entities);
               return new SchematicDocument(List.of(new SchematicDocument.Region(0, 0, 0, w, h, l, palette.toArray(BlockState[]::new), packed, null)), bes, entities);
            } else {
               throw new IllegalArgumentException("Invalid Sponge palette size");
            }
         } else {
            throw new IllegalArgumentException("Invalid Sponge dimensions");
         }
      } else {
         throw new IllegalArgumentException("Unsupported Sponge version " + version);
      }
   }

   private static CompoundTag updateTag(CompoundTag data, int from, TypeReference type) {
      int current = SharedConstants.getCurrentVersion().dataVersion().version();
      if (from > 0 && from < current) {
         Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, data.copy());
         return (CompoundTag)DataFixers.getDataFixer().update(type, dynamic, from, current).getValue();
      } else {
         return data.copy();
      }
   }

   static void readEntities(ListTag entries, double ox, double oy, double oz, int dataVersion, List<SchematicDocument.EntityData> out) {
      for (Tag tag : entries) {
         if (tag instanceof CompoundTag outer) {
            CompoundTag entity = outer.getCompound("Data").orElse(outer).copy();
            String id = outer.getStringOr("Id", entity.getStringOr("id", ""));
            if (!id.isEmpty()) {
               entity.putString("id", id);
               ListTag pos = outer.getListOrEmpty("Pos");
               if (pos.size() != 3) {
                  pos = entity.getListOrEmpty("Pos");
               }

               if (pos.size() == 3) {
                  double x = pos.getDoubleOr(0, 0.0);
                  double y = pos.getDoubleOr(1, 0.0);
                  double z = pos.getDoubleOr(2, 0.0);
                  if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
                     throw new IllegalArgumentException("Invalid saved entity position");
                  }

                  entity.put("Pos", pos.copy());
                  out.add(new SchematicDocument.EntityData(ox + x, oy + y, oz + z, updateTag(entity, dataVersion, References.ENTITY_TREE)));
                  if (out.size() > 16384) {
                     throw new IllegalArgumentException("Too many saved entities");
                  }
               }
            }
         }
      }
   }

   record EntityData(double x, double y, double z, CompoundTag data) {
   }

   static final class Region {
      final int originX;
      final int originY;
      final int originZ;
      final int width;
      final int height;
      final int length;
      final int cellCount;
      final BlockState[] palette;
      final long[] packed;
      final int[] values;
      final int bitsPerEntry;

      Region(int x, int y, int z, int width, int height, int length, BlockState[] palette, long[] packed, int[] values) {
         this.originX = x;
         this.originY = y;
         this.originZ = z;
         this.width = width;
         this.height = height;
         this.length = length;
         if (width >= 1 && height >= 1 && length >= 1 && width <= 4096 && height <= 4096 && length <= 4096) {
            long volume = (long)width * height * length;
            if (volume <= 134217728L && palette.length >= 1 && palette.length <= 65536) {
               this.cellCount = (int)volume;
               this.palette = palette;
               this.packed = packed;
               this.values = values;
               this.bitsPerEntry = SchematicDocument.bitsForPaletteSize(palette.length);
               if (values != null && values.length != volume
                  || values == null && (packed == null || packed.length != ((long)this.cellCount * this.bitsPerEntry + 63L) / 64L)) {
                  throw new IllegalArgumentException("Schematic region data does not match its size");
               }
            } else {
               throw new IllegalArgumentException("Invalid schematic region size or palette");
            }
         } else {
            throw new IllegalArgumentException("Schematic region span must be between 1 and 4096 blocks");
         }
      }

      int valueAt(int index) {
         if (this.values != null) {
            return this.values[index];
         }

         long bit = (long)index * this.bitsPerEntry;
         int word = (int)(bit >>> 6);
         int shift = (int)(bit & 63L);
         long value = this.packed[word] >>> shift;
         if (shift + this.bitsPerEntry > 64) {
            value |= this.packed[word + 1] << 64 - shift;
         }

         return (int)(value & (1L << this.bitsPerEntry) - 1L);
      }

      BlockState stateAtIndex(int index) {
         int id = this.valueAt(index);
         if (id >= 0 && id < this.palette.length) {
            return this.palette[id];
         } else {
            throw new IllegalArgumentException("Invalid schematic palette index " + id);
         }
      }

      BlockState stateAt(int px, int py, int pz) {
         return this.stateAtIndex(((py - this.originY) * this.length + pz - this.originZ) * this.width + px - this.originX);
      }

      boolean contains(int px, int py, int pz) {
         return px >= this.originX
            && px < this.originX + this.width
            && py >= this.originY
            && py < this.originY + this.height
            && pz >= this.originZ
            && pz < this.originZ + this.length;
      }
   }

   record BlockEntityData(BlockPos position, CompoundTag data) {
   }

   record Preview(List<AiBuilder.CellSpec> cells, BlockPos nonAirMinimum, long blocks) {
   }
}
