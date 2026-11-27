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
   final List<SchematicDocument.Inner_CachePool> init_List;
   final List<SchematicDocument.Inner_DataUtil> load_List;
   final List<SchematicDocument.Inner_BufferHelper> read_List;
   final int load_I;
   final int read_I;
   final int write_I;
   final long read_J;
   private final Long2ObjectOpenHashMap<SchematicDocument.Inner_CachePool[]> init_Long2ObjectOpenHas = new Long2ObjectOpenHashMap<>();
   private volatile long write_J = -1L;

   static int init_I_I(int size) {
      return Math.max(2, 32 - Integer.numberOfLeadingZeros(Math.max(1, size - 1)));
   }

   static void init(long[] packed, int index, int bits, int id) {
      long bit = (long)index * bits;
      int word = (int)(bit >>> 6);
      int shift = (int)(bit & 63L);
      packed[word] |= (long)id << shift;
      if (shift + bits > 64) {
         packed[word + 1] = packed[word + 1] | (long)id >>> 64 - shift;
      }
   }

   SchematicDocument(List<SchematicDocument.Inner_CachePool> raw, List<SchematicDocument.Inner_DataUtil> bes, List<SchematicDocument.Inner_BufferHelper> entities) {
      if (!raw.isEmpty() && raw.size() <= 8192) {
         int minX = 2147483647;
         int minY = minX;
         int minZ = minX;
         int maxX = -2147483648;
         int maxY = maxX;
         int maxZ = maxX;
         long volume = 0L;

         for (SchematicDocument.Inner_CachePool r : raw) {
            volume += r.flush;
            if (volume > 134217728L) {
               throw new IllegalArgumentException("Schematic exceeds the 128 million voxel limit");
            }

            minX = Math.min(minX, r.init_I);
            minY = Math.min(minY, r.load);
            minZ = Math.min(minZ, r.read);
            maxX = Math.max(maxX, Math.addExact(r.init_I, r.write));
            maxY = Math.max(maxY, Math.addExact(r.load, r.update));
            maxZ = Math.max(maxZ, Math.addExact(r.read, r.sync));
         }

         this.load_I = Math.subtractExact(maxX, minX);
         this.read_I = Math.subtractExact(maxY, minY);
         this.write_I = Math.subtractExact(maxZ, minZ);
         if (this.load_I <= 4096 && this.read_I <= 4096 && this.write_I <= 4096) {
            this.read_J = volume;
            ArrayList<SchematicDocument.Inner_CachePool> normalized = new ArrayList<>();

            for (SchematicDocument.Inner_CachePool r : raw) {
               normalized.add(
                  new SchematicDocument.Inner_CachePool(
                     r.init_I - minX, r.load - minY, r.read - minZ, r.write, r.update, r.sync, r.init_BlockStateArr, r.init_JArr, r.init_IArr
                  )
               );
            }

            this.init_List = List.copyOf(normalized);
            ArrayList<SchematicDocument.Inner_DataUtil> normalizedBes = new ArrayList<>();

            for (SchematicDocument.Inner_DataUtil be : bes) {
               normalizedBes.add(new SchematicDocument.Inner_DataUtil(be.position.offset(-minX, -minY, -minZ), be.data));
            }

            this.load_List = List.copyOf(normalizedBes);
            ArrayList<SchematicDocument.Inner_BufferHelper> normalizedEntities = new ArrayList<>();

            for (SchematicDocument.Inner_BufferHelper e : entities) {
               normalizedEntities.add(new SchematicDocument.Inner_BufferHelper(e.x - minX, e.y - minY, e.z - minZ, e.data));
            }

            this.read_List = List.copyOf(normalizedEntities);
            Long2ObjectOpenHashMap<ArrayList<SchematicDocument.Inner_CachePool>> build = new Long2ObjectOpenHashMap<>();

            for (SchematicDocument.Inner_CachePool r : this.init_List) {
               for (int sx = r.init_I >> 4; sx <= r.init_I + r.write - 1 >> 4; sx++) {
                  for (int sy = r.load >> 4; sy <= r.load + r.update - 1 >> 4; sy++) {
                     for (int sz = r.read >> 4; sz <= r.read + r.sync - 1 >> 4; sz++) {
                        long key = BlockPos.asLong(sx, sy, sz);
                        build.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                        if (build.size() > 262144) {
                           throw new IllegalArgumentException("Schematic section index is too large");
                        }
                     }
                  }
               }
            }

            build.forEach((keyx, value) -> this.init_Long2ObjectOpenHas.put(keyx, value.toArray(SchematicDocument.Inner_CachePool[]::new)));
         } else {
            throw new IllegalArgumentException("Schematic span exceeds 4096 blocks");
         }
      } else {
         throw new IllegalArgumentException("Schematic has no regions or too many regions");
      }
   }

   SchematicDocument.Inner_CachePool init_cQ_CachePool_I(int x, int y, int z) {
      SchematicDocument.Inner_CachePool[] candidates = this.init_Long2ObjectOpenHas.get(BlockPos.asLong(x >> 4, y >> 4, z >> 4));
      if (candidates != null) {
         for (int i = candidates.length - 1; i >= 0; i--) {
            if (candidates[i].init_Z(x, y, z)) {
               return candidates[i];
            }
         }
      }

      return null;
   }

   BlockState init_BlockState_I(int x, int y, int z) {
      SchematicDocument.Inner_CachePool r = this.init_cQ_CachePool_I(x, y, z);
      return r == null ? Blocks.AIR.defaultBlockState() : r.init_BlockState(x, y, z);
   }

   boolean init_Z_I(int x, int y, int z) {
      return this.init_cQ_CachePool_I(x, y, z) != null;
   }

   long init_J_IntConsumer(IntConsumer progress) {
      if (this.write_J >= 0L) {
         return this.write_J;
      }

      long count = 0L;
      long done = 0L;

      for (SchematicDocument.Inner_CachePool r : this.init_List) {
         for (int i = 0; i < r.flush; i++) {
            if ((i & 4095) == 0) {
               init();
               progress.accept((int)(100L * done / Math.max(1L, this.read_J)));
            }

            int y = i / (r.write * r.sync);
            int rest = i % (r.write * r.sync);
            int z = rest / r.write;
            int x = rest % r.write;
            if (!r.init_BlockState(i).isAir() && this.init_cQ_CachePool_I(r.init_I + x, r.load + y, r.read + z) == r) {
               count++;
            }

            done++;
         }
      }

      this.write_J = count;
      progress.accept(100);
      return count;
   }

   SchematicDocument.Inner_StreamUtil init_cQ_StreamUtil_I(int rotation, String mirror, IntConsumer progress) {
      ArrayList<AiBuilder.CellSpec> cells = new ArrayList<>();
      long count = 0L;
      long done = 0L;
      int minX = 2147483647;
      int minY = minX;
      int minZ = minX;
      IdentityHashMap<BlockState, AiBuilder.CellSpec> cache = new IdentityHashMap<>();

      for (SchematicDocument.Inner_CachePool r : this.init_List) {
         for (int i = 0; i < r.flush; i++) {
            if ((i & 4095) == 0) {
               init();
               progress.accept((int)(100L * done / Math.max(1L, this.read_J)));
            }

            done++;
            BlockState s = r.init_BlockState(i);
            if (!s.isAir()) {
               int y = r.load + i / (r.write * r.sync);
               int rest = i % (r.write * r.sync);
               int z = r.read + rest / r.write;
               int x = r.init_I + rest % r.write;
               if (this.init_cQ_CachePool_I(x, y, z) == r) {
                  count++;
                  BlockPos at = this.init(x, y, z, rotation, mirror);
                  minX = Math.min(minX, at.getX());
                  minY = Math.min(minY, at.getY());
                  minZ = Math.min(minZ, at.getZ());
                  if (!s.isSolidRender()
                     || !this.init_BlockState_I(x - 1, y, z).isSolidRender()
                     || !this.init_BlockState_I(x + 1, y, z).isSolidRender()
                     || !this.init_BlockState_I(x, y - 1, z).isSolidRender()
                     || !this.init_BlockState_I(x, y + 1, z).isSolidRender()
                     || !this.init_BlockState_I(x, y, z - 1).isSolidRender()
                     || !this.init_BlockState_I(x, y, z + 1).isSolidRender()) {
                     if (cells.size() >= 2097152) {
                        throw new IllegalArgumentException("Too many exposed blocks for a hologram; single-player Paste is still available");
                     }

                     AiBuilder.CellSpec template = cache.computeIfAbsent(
                        s, k -> init_TableIndex_StreamU_BlockState(init_BlockState_BlockState(k, rotation, mirror))
                     );
                     cells.add(new AiBuilder.CellSpec(at.getX(), at.getY(), at.getZ(), template.block(), template.properties()));
                  }
               }
            }
         }
      }

      this.write_J = count;
      progress.accept(100);
      return new SchematicDocument.Inner_StreamUtil(List.copyOf(cells), count == 0L ? BlockPos.ZERO : new BlockPos(minX, minY, minZ), count);
   }

   boolean init_Z_BlockPos(BlockPos at, int rotation, String mirror) {
      BlockPos source = this.load(at.getX(), at.getY(), at.getZ(), rotation, mirror);
      return this.init_BlockState_I(source.getX(), source.getY(), source.getZ()).isSolidRender();
   }

   int init_I_I_Str(int rotation, String mirror) {
      return rotation % 180 == 0 ? this.load_I : this.write_I;
   }

   int load_I_I(int rotation) {
      return rotation % 180 == 0 ? this.write_I : this.load_I;
   }

   BlockPos init(int x, int y, int z, int rotation, String mirror) {
      if (mirror.equals("x")) {
         x = this.load_I - 1 - x;
      } else if (mirror.equals("z")) {
         z = this.write_I - 1 - z;
      }

      BlockPos var10000;
      switch (Math.floorMod(rotation, 360)) {
         case 90:
            var10000 = new BlockPos(this.write_I - 1 - z, y, x);
            break;
         case 180:
            var10000 = new BlockPos(this.load_I - 1 - x, y, this.write_I - 1 - z);
            break;
         case 270:
            var10000 = new BlockPos(z, y, this.load_I - 1 - x);
            break;
         default:
            var10000 = new BlockPos(x, y, z);
      }

      return var10000;
   }

   BlockPos load(int x, int y, int z, int rotation, String mirror) {
      int sx;
      int sz;
      switch (Math.floorMod(rotation, 360)) {
         case 90:
            sx = z;
            sz = this.write_I - 1 - x;
            break;
         case 180:
            sx = this.load_I - 1 - x;
            sz = this.write_I - 1 - z;
            break;
         case 270:
            sx = this.load_I - 1 - z;
            sz = x;
            break;
         default:
            sx = x;
            sz = z;
      }

      if (mirror.equals("x")) {
         sx = this.load_I - 1 - sx;
      } else if (mirror.equals("z")) {
         sz = this.write_I - 1 - sz;
      }

      return new BlockPos(sx, y, sz);
   }

   static BlockState init_BlockState_BlockState(BlockState state, int rotation, String mirror) {
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

   static AiBuilder.CellSpec init_TableIndex_StreamU_BlockState(BlockState state) {
      TreeMap<String, String> properties = new TreeMap<>();

      for (Property<?> p : state.getProperties()) {
         properties.put(p.getName(), init_Str_BlockState_Property(state, p));
      }

      return new AiBuilder.CellSpec(0, 0, 0, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), Map.copyOf(properties));
   }

   private static <T extends Comparable<T>> String init_Str_BlockState_Property(BlockState state, Property<T> property) {
      return property.getName(state.getValue(property));
   }

   static void init() {
      if (Thread.currentThread().isInterrupted()) {
         throw new CancellationException();
      }
   }

   static SchematicDocument init_cQ_Path(Path file) {
      CompoundTag root = AiSchematic.init_CompoundTag_Path_J(file, 268435456L);
      if (root.getCompound("Regions").isPresent()) {
         return init_cQ_CompoundTag(root);
      }

      CompoundTag body = root.getCompound("Schematic").orElse(root);
      if (!body.getCompound("Palette").isPresent() && !body.getCompound("Blocks").flatMap(b -> b.getCompound("Palette")).isPresent()) {
         AiSchematic.Inner_DataUtil decoded = AiSchematic.init_bP_DataUtil_CompoundTag_Z(body, false);
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

         return new SchematicDocument(List.of(new SchematicDocument.Inner_CachePool(minX, minY, minZ, w, h, l, palette.toArray(BlockState[]::new), null, ids)), List.of(), List.of());
      } else {
         return load_cQ_CompoundTag(body);
      }
   }

   static SchematicDocument init_cQ_CompoundTag(CompoundTag root) {
      int version = root.getIntOr("Version", 0);
      if (version >= 1 && version <= 7) {
         int dataVersion = root.getIntOr("MinecraftDataVersion", 1631);
         CompoundTag data = root.getCompoundOrEmpty("Regions");
         ArrayList<SchematicDocument.Inner_CachePool> regions = new ArrayList<>();
         ArrayList<SchematicDocument.Inner_DataUtil> bes = new ArrayList<>();
         ArrayList<SchematicDocument.Inner_BufferHelper> entities = new ArrayList<>();

         for (String name : data.keySet()) {
            init();
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
                  AiSchematic.Inner_ConfigStore parsed = AiSchematic.init(entries.getCompoundOrEmpty(i), dataVersion, version < 4 || dataVersion < 1631, "region " + name);
                  palette[i] = AiBuilder.toBlockState(new AiBuilder.CellSpec(0, 0, 0, parsed.block(), parsed.properties()));
               }

               regions.add(
                  new SchematicDocument.Inner_CachePool(
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
                     bes.add(new SchematicDocument.Inner_DataUtil(at, init_CompoundTag_CompoundTag(be, dataVersion, References.BLOCK_ENTITY)));
                  }
               }

               init(r.getListOrEmpty("Entities"), px, py, pz, dataVersion, entities);
            }
         }

         return new SchematicDocument(regions, bes, entities);
      } else {
         throw new IllegalArgumentException("Unsupported Litematic version " + version);
      }
   }

   static SchematicDocument load_cQ_CompoundTag(CompoundTag body) {
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

               AiSchematic.Inner_ConfigStore parsed = AiSchematic.init_bP_ConfigStore_Str_I(state, dataVersion);
               ids.put(id, palette.size());
               palette.add(AiBuilder.toBlockState(new AiBuilder.CellSpec(0, 0, 0, parsed.block(), parsed.properties())));
            }

            byte[] raw = blockData.getByteArray("Data")
               .orElseGet(() -> blockData.getByteArray("BlockData").orElseThrow(() -> new IllegalArgumentException("Missing Sponge block data")));
            if (!palette.isEmpty() && palette.size() <= 65536) {
               int bits = init_I_I(palette.size());
               long[] packed = new long[(int)((volume * bits + 63L) / 64L)];
               int cursor = 0;
               int i = 0;

               label82:
               while (i < volume) {
                  if ((i & 4095) == 0) {
                     init();
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

                        init(packed, i, bits, mapped);
                        i++;
                        continue label82;
                     }
                  }

                  throw new IllegalArgumentException("Invalid Sponge block data");
               }

               if (cursor != raw.length) {
                  throw new IllegalArgumentException("Unexpected Sponge block data");
               }

               ArrayList<SchematicDocument.Inner_DataUtil> bes = new ArrayList<>();
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

                        bes.add(new SchematicDocument.Inner_DataUtil(new BlockPos(p[0], p[1], p[2]), init_CompoundTag_CompoundTag(be, dataVersion, References.BLOCK_ENTITY)));
                     }
                  }
               }

               ArrayList<SchematicDocument.Inner_BufferHelper> entities = new ArrayList<>();
               init(body.getListOrEmpty("Entities"), 0.0, 0.0, 0.0, dataVersion, entities);
               return new SchematicDocument(List.of(new SchematicDocument.Inner_CachePool(0, 0, 0, w, h, l, palette.toArray(BlockState[]::new), packed, null)), bes, entities);
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

   private static CompoundTag init_CompoundTag_CompoundTag(CompoundTag data, int from, TypeReference type) {
      int current = SharedConstants.getCurrentVersion().dataVersion().version();
      if (from > 0 && from < current) {
         Dynamic<Tag> dynamic = new Dynamic<>(NbtOps.INSTANCE, data.copy());
         return (CompoundTag)DataFixers.getDataFixer().update(type, dynamic, from, current).getValue();
      } else {
         return data.copy();
      }
   }

   static void init(ListTag entries, double ox, double oy, double oz, int dataVersion, List<SchematicDocument.Inner_BufferHelper> out) {
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
                  out.add(new SchematicDocument.Inner_BufferHelper(ox + x, oy + y, oz + z, init_CompoundTag_CompoundTag(entity, dataVersion, References.ENTITY_TREE)));
                  if (out.size() > 16384) {
                     throw new IllegalArgumentException("Too many saved entities");
                  }
               }
            }
         }
      }
   }

   record Inner_BufferHelper(double x, double y, double z, CompoundTag data) {
   }

   static final class Inner_CachePool {
      final int init_I;
      final int load;
      final int read;
      final int write;
      final int update;
      final int sync;
      final int flush;
      final BlockState[] init_BlockStateArr;
      final long[] init_JArr;
      final int[] init_IArr;
      final int close;

      Inner_CachePool(int x, int y, int z, int width, int height, int length, BlockState[] palette, long[] packed, int[] values) {
         this.init_I = x;
         this.load = y;
         this.read = z;
         this.write = width;
         this.update = height;
         this.sync = length;
         if (width >= 1 && height >= 1 && length >= 1 && width <= 4096 && height <= 4096 && length <= 4096) {
            long volume = (long)width * height * length;
            if (volume <= 134217728L && palette.length >= 1 && palette.length <= 65536) {
               this.flush = (int)volume;
               this.init_BlockStateArr = palette;
               this.init_JArr = packed;
               this.init_IArr = values;
               this.close = SchematicDocument.init_I_I(palette.length);
               if (values != null && values.length != volume
                  || values == null && (packed == null || packed.length != ((long)this.flush * this.close + 63L) / 64L)) {
                  throw new IllegalArgumentException("Schematic region data does not match its size");
               }
            } else {
               throw new IllegalArgumentException("Invalid schematic region size or palette");
            }
         } else {
            throw new IllegalArgumentException("Schematic region span must be between 1 and 4096 blocks");
         }
      }

      int init_I(int index) {
         if (this.init_IArr != null) {
            return this.init_IArr[index];
         }

         long bit = (long)index * this.close;
         int word = (int)(bit >>> 6);
         int shift = (int)(bit & 63L);
         long value = this.init_JArr[word] >>> shift;
         if (shift + this.close > 64) {
            value |= this.init_JArr[word + 1] << 64 - shift;
         }

         return (int)(value & (1L << this.close) - 1L);
      }

      BlockState init_BlockState(int index) {
         int id = this.init_I(index);
         if (id >= 0 && id < this.init_BlockStateArr.length) {
            return this.init_BlockStateArr[id];
         } else {
            throw new IllegalArgumentException("Invalid schematic palette index " + id);
         }
      }

      BlockState init_BlockState(int px, int py, int pz) {
         return this.init_BlockState(((py - this.load) * this.sync + pz - this.read) * this.write + px - this.init_I);
      }

      boolean init_Z(int px, int py, int pz) {
         return px >= this.init_I
            && px < this.init_I + this.write
            && py >= this.load
            && py < this.load + this.update
            && pz >= this.read
            && pz < this.read + this.sync;
      }
   }

   record Inner_DataUtil(BlockPos position, CompoundTag data) {
   }

   record Inner_StreamUtil(List<AiBuilder.CellSpec> cells, BlockPos nonAirMinimum, long blocks) {
   }
}
