package autismclient.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

// Build (bQ) and preview (cR) decoding of small in-memory litematic/Sponge files.
public final class SchematicDecodeCheck {
   static final List<String> failures = new ArrayList<>();
   static Path dir;

   public static void main(String[] a) throws Exception {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      dir = Files.createTempDirectory("schematic-check");

      // S3: skipUnknown drops unknown palette entries for litematic and Sponge too.
      Path lit = write("unknown.litematic", litematic(region(0, 0, 0, 3, 1, 1, List.of("minecraft:air", "minecraft:not_a_real_block", "minecraft:stone"), new int[]{1, 2, 2})));
      Path sponge = write("unknown.schem", sponge(3, 1, 1, List.of("minecraft:not_a_real_block", "minecraft:stone"), new byte[]{0, 1, 1}));
      for (Path p : List.of(lit, sponge)) {
         check(throwsOn(() -> AiSchematic.decodeFile(p, false)), "S3 strict import of " + p.getFileName() + " rejects the unknown block");
         check(Integer.valueOf(2).equals(cellsOrNull(() -> AiSchematic.decodeFile(p, true).cells().size())),"S3 skipUnknown keeps the 2 known cells of " + p.getFileName());
      }

      // S8: trailing Sponge BlockData bytes are rejected by build like preview.
      CompoundTag trailing = sponge(1, 1, 1, List.of("minecraft:stone"), new byte[]{0, 0});
      check(throwsOn(() -> SchematicDocument.parseSponge(trailing)), "S8 preview rejects trailing Sponge bytes");
      check(throwsOn(() -> AiSchematic.decodeFile(write("trailing.schem", trailing), false)), "S8 build rejects trailing Sponge bytes");
      check(!throwsOn(() -> AiSchematic.decodeFile(write("ok.schem", sponge(1, 1, 1, List.of("minecraft:stone"), new byte[]{0})), false)), "S8 exact Sponge data still loads");

      // S4: excess BlockStates longs are rejected by both; the library keeps the stock 256 MiB NBT budget.
      CompoundTag extra = region(0, 0, 0, 1, 1, 1, List.of("minecraft:stone"), new int[]{0});
      extra.putLongArray("BlockStates", new long[2]);
      CompoundTag extraRoot = litematic(extra);
      check(throwsOn(() -> SchematicDocument.parseLitematic(extraRoot)), "S4 preview rejects excess BlockStates longs");
      check(throwsOn(() -> AiSchematic.decodeFile(write("extra.litematic", extraRoot), false)), "S4 build rejects excess BlockStates longs");
      CompoundTag padded = litematic(region(0, 0, 0, 1, 1, 1, List.of("minecraft:stone"), new int[]{0}));
      CompoundTag meta = new CompoundTag();
      meta.putLongArray("Pad", new long[9_000_000]);
      padded.put("Metadata", meta);
      Path big = write("padded.litematic", padded);
      check(throwsOn(() -> AiSchematic.decodeFile(big, false)), "S4 build rejects 72 MB of NBT");
      check(!throwsOn(() -> SchematicDocument.loadFile(big)), "library scanner and paste read 72 MB of NBT (256 MiB budget)");

      // Sol-3: overlapping regions resolve the same in build and preview (later region wins, air included).
      CompoundTag overlap = litematic(
         region(0, 0, 0, 3, 1, 1, List.of("minecraft:stone"), new int[]{0, 0, 0}),
         region(2, 0, 0, 1, 1, 1, List.of("minecraft:air"), new int[]{0}),
         region(1, 0, 0, 1, 1, 1, List.of("minecraft:glass"), new int[]{0})
      );
      Path overlapFile = write("overlap.litematic", overlap);
      Set<String> build = new TreeSet<>();
      for (AiBuilder.CellSpec c : AiSchematic.load(overlapFile, BlockPos.ZERO, 0, "none", false, 2097152).blueprint().cells()) {
         build.add(c.dx() + "," + c.dy() + "," + c.dz() + "=" + c.block());
      }

      SchematicDocument.Preview shown = SchematicDocument.loadFile(overlapFile).buildPreview(0, "none", v -> {});
      Set<String> preview = new TreeSet<>();
      BlockPos min = shown.nonAirMinimum();
      for (AiBuilder.CellSpec c : shown.cells()) {
         preview.add((c.dx() - min.getX()) + "," + (c.dy() - min.getY()) + "," + (c.dz() - min.getZ()) + "=" + c.block());
      }

      check(build.equals(preview), "Sol-3 overlapping regions: build " + build + " equals preview " + preview);

      // S7: AI attachment import uses the build's span limit (1023), not 256.
      Path wide = write("wide600.litematic", litematic(region(0, 0, 0, 600, 1, 1, List.of("minecraft:stone"), new int[600])));
      Path wider = write("wide1100.litematic", litematic(region(0, 0, 0, 1100, 1, 1, List.of("minecraft:stone"), new int[1100])));
      Path target = Files.createTempDirectory(dir, "attached");
      check(!throwsOn(() -> AiAttachments.init(wide, target)), "S7 a 600-wide schematic attaches (direct build accepts it)");
      check(!throwsOn(() -> AiSchematic.load(wide, BlockPos.ZERO, 0, "none", false, 2097152)), "S7 a 600-wide schematic builds");
      check(throwsOn(() -> AiAttachments.init(wider, target)), "S7 a 1100-wide schematic is refused on attach like build");

      // S9: the capture writer refuses a palette its own reader would reject.
      check(throwsOn(() -> SchematicCapture.init(new int[1], 65537)), "S9 capture writer refuses 65537 block states");
      BlockState[] states = new BlockState[65536];
      java.util.Arrays.fill(states, Blocks.AIR.defaultBlockState());
      check(!throwsOn(() -> new SchematicDocument.Region(0, 0, 0, 1, 1, 1, states, SchematicCapture.init(new int[1], 65536), null)), "S9 65536 states round-trip");

      if (!failures.isEmpty()) {
         failures.forEach(f -> System.out.println("FAIL SchematicDecodeCheck: " + f));
         System.exit(1);
      }

      System.out.println("PASS SchematicDecodeCheck");
      System.exit(0);
   }

   interface Body {
      Object run() throws Exception;
   }

   static boolean throwsOn(Body body) {
      try {
         body.run();
         return false;
      } catch (Exception e) {
         return true;
      }
   }

   static Integer cellsOrNull(Body body) {
      try {
         return (Integer)body.run();
      } catch (Exception e) {
         return null;
      }
   }

   static void check(boolean ok, String what) {
      if (!ok) {
         failures.add(what);
      }
   }

   static Path write(String name, CompoundTag root) throws Exception {
      Path p = dir.resolve(name);
      NbtIo.writeCompressed(root, p);
      return p;
   }

   static int dataVersion() {
      return SharedConstants.getCurrentVersion().dataVersion().version();
   }

   static CompoundTag xyz(int x, int y, int z) {
      CompoundTag t = new CompoundTag();
      t.putInt("x", x);
      t.putInt("y", y);
      t.putInt("z", z);
      return t;
   }

   static CompoundTag region(int x, int y, int z, int w, int h, int l, List<String> palette, int[] values) {
      CompoundTag r = new CompoundTag();
      r.put("Position", xyz(x, y, z));
      r.put("Size", xyz(w, h, l));
      ListTag entries = new ListTag();
      for (String name : palette) {
         CompoundTag e = new CompoundTag();
         e.putString("Name", name);
         entries.add(e);
      }

      r.put("BlockStatePalette", entries);
      int bits = SchematicDocument.bitsForPaletteSize(palette.size());
      long[] packed = new long[(int)(((long)values.length * bits + 63L) / 64L)];
      for (int i = 0; i < values.length; i++) {
         SchematicDocument.init(packed, i, bits, values[i]);
      }

      r.putLongArray("BlockStates", packed);
      r.put("TileEntities", new ListTag());
      r.put("Entities", new ListTag());
      return r;
   }

   static CompoundTag litematic(CompoundTag... regions) {
      CompoundTag root = new CompoundTag();
      root.putInt("Version", 6);
      root.putInt("MinecraftDataVersion", dataVersion());
      CompoundTag all = new CompoundTag();
      for (int i = 0; i < regions.length; i++) {
         all.put("r" + i, regions[i]);
      }

      root.put("Regions", all);
      return root;
   }

   static CompoundTag sponge(int w, int h, int l, List<String> palette, byte[] data) {
      CompoundTag root = new CompoundTag();
      root.putInt("Version", 2);
      root.putInt("DataVersion", dataVersion());
      root.putShort("Width", (short)w);
      root.putShort("Height", (short)h);
      root.putShort("Length", (short)l);
      CompoundTag p = new CompoundTag();
      for (int i = 0; i < palette.size(); i++) {
         p.putInt(palette.get(i), i);
      }

      root.put("Palette", p);
      root.putByteArray("BlockData", data);
      return root;
   }
}
