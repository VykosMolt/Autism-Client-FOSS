package autismclient.ai;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;

final class AiFarmArea {
   static final String FARMLAND = "farmland";
   static final String load = "soul_sand";
   private static final Set<String> init_Set = Set.of("minecraft:wheat", "minecraft:carrots", "minecraft:potatoes", "minecraft:beetroots");

   private AiFarmArea() {
   }

   static boolean init_Z(JsonObject args) {
      boolean from = args.has("from");
      boolean to = args.has("to");
      if (from != to) {
         throw new IllegalArgumentException("Choose both farm corners before starting");
      } else {
         return from;
      }
   }

   static AiFarmArea.Inner_DataUtil fromArgs(JsonObject args) {
      return !init_Z(args) ? null : new AiFarmArea.Inner_DataUtil(init_BlockPos(args.getAsJsonObject("from"), "from"), init_BlockPos(args.getAsJsonObject("to"), "to"));
   }

   static String substrateFor(JsonObject args, String cropBlock) {
      String expected = defaultSubstrate(cropBlock);
      String raw = AiJson.getString(args, "substrate", expected).trim().toLowerCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
      String var10000;
      switch (raw) {
         case "":
         case "auto":
            var10000 = expected;
            break;
         case "normal":
         case "dirt":
         case "water":
         case "farmland":
            var10000 = "farmland";
            break;
         case "soul":
         case "soulsand":
         case "soul_sand":
         case "nether":
         case "nether_wart":
            var10000 = "soul_sand";
            break;
         default:
            throw new IllegalArgumentException("substrate must be farmland or soul_sand");
      }

      String substrate = var10000;
      if (!expected.equals(substrate)) {
         if ("soul_sand".equals(expected)) {
            throw new IllegalArgumentException("Nether wart needs soul sand; ordinary crops need farmland and water");
         } else {
            throw new IllegalArgumentException("This crop needs farmland and water; choose nether wart for a soul-sand farm");
         }
      } else {
         return substrate;
      }
   }

   static String defaultSubstrate(String cropBlock) {
      if ("minecraft:nether_wart".equals(cropBlock)) {
         return "soul_sand";
      } else if (init_Set.contains(cropBlock)) {
         return "farmland";
      } else {
         throw new IllegalArgumentException("Build supports wheat, carrots, potatoes, beetroots and nether wart");
      }
   }

   static boolean isSoulSand(String substrate) {
      return "soul_sand".equals(substrate);
   }

   static String load(String substrate) {
      return isSoulSand(substrate) ? "minecraft:soul_sand" : "minecraft:dirt";
   }

   static String read(String substrate) {
      return isSoulSand(substrate) ? "minecraft:soul_sand" : "minecraft:farmland";
   }

   private static BlockPos init_BlockPos(JsonObject value, String name) {
      if (value == null) {
         throw new IllegalArgumentException("Missing farm corner " + name);
      }

      for (String axis : List.of("x", "y", "z")) {
         if (!value.has(axis)) {
            throw new IllegalArgumentException("Farm corner " + name + " needs x, y and z");
         }
      }

      try {
         return new BlockPos(value.get("x").getAsInt(), value.get("y").getAsInt(), value.get("z").getAsInt());
      } catch (RuntimeException e) {
         throw new IllegalArgumentException("Farm corner " + name + " must contain integer x, y and z", e);
      }
   }

   private static JsonObject init(BlockPos pos) {
      return AiJson.objectOf("x", pos.getX(), "y", pos.getY(), "z", pos.getZ());
   }

   record Inner_DataUtil(BlockPos from, BlockPos to) {
      Inner_DataUtil {
         from = from.immutable();
         to = to.immutable();
      }

      int minX() {
         return Math.min(this.from.getX(), this.to.getX());
      }

      int minY() {
         return Math.min(this.from.getY(), this.to.getY());
      }

      int minZ() {
         return Math.min(this.from.getZ(), this.to.getZ());
      }

      int maxX() {
         return Math.max(this.from.getX(), this.to.getX());
      }

      int maxY() {
         return Math.max(this.from.getY(), this.to.getY());
      }

      int maxZ() {
         return Math.max(this.from.getZ(), this.to.getZ());
      }

      int width() {
         return this.maxX() - this.minX() + 1;
      }

      int length() {
         return this.maxZ() - this.minZ() + 1;
      }

      BlockPos origin() {
         return new BlockPos(this.minX(), this.minY(), this.minZ());
      }

      int heightDifference() {
         return this.maxY() - this.minY();
      }

      AiFarmBuild.Inner_StreamUtil layout(String substrate) {
         return new AiFarmBuild.Inner_StreamUtil(this.width(), this.length(), "soul_sand".equals(substrate) ? List.of() : AiFarmBuild.init(this.width(), this.length()));
      }

      List<BlockPos> cells(int groundY) {
         List<BlockPos> out = new ArrayList<>(this.width() * this.length());

         for (int x = this.minX(); x <= this.maxX(); x++) {
            for (int z = this.minZ(); z <= this.maxZ(); z++) {
               out.add(new BlockPos(x, groundY, z));
            }
         }

         return out;
      }

      String validate(String substrate) {
         if (this.width() > 27 || this.length() > 27) {
            return "Farm sides must be at most 27 blocks; selected " + this.width() + "x" + this.length();
         } else if (this.heightDifference() > 3) {
            return "The selected corners differ by more than 3 blocks in height";
         } else {
            return this.layout(substrate).plots() < 1 ? "The selected area leaves no room for a crop beside its water" : "";
         }
      }

      JsonObject arguments(String substrate) {
         return AiJson.objectOf("substrate", substrate, "from", AiFarmArea.init(this.from), "to", AiFarmArea.init(this.to));
      }
   }
}
