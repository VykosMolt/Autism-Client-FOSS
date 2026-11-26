package autismclient.util.oresim;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class OreBlockCatalog {
   public static final List<String> init_List = List.of(
      "minecraft:coal_ore",
      "minecraft:deepslate_coal_ore",
      "minecraft:iron_ore",
      "minecraft:deepslate_iron_ore",
      "minecraft:gold_ore",
      "minecraft:deepslate_gold_ore",
      "minecraft:lapis_ore",
      "minecraft:deepslate_lapis_ore",
      "minecraft:redstone_ore",
      "minecraft:deepslate_redstone_ore",
      "minecraft:diamond_ore",
      "minecraft:deepslate_diamond_ore",
      "minecraft:emerald_ore",
      "minecraft:deepslate_emerald_ore",
      "minecraft:copper_ore",
      "minecraft:deepslate_copper_ore",
      "minecraft:raw_iron_block",
      "minecraft:raw_copper_block",
      "minecraft:nether_gold_ore",
      "minecraft:nether_quartz_ore",
      "minecraft:ancient_debris"
   );
   private static volatile Set<Block> init_Set;

   private OreBlockCatalog() {
   }

   public static boolean init(Block block) {
      if (block == null) {
         return false;
      }

      Set<Block> known = init_Set;
      if (known == null) {
         Set<Block> built = Collections.newSetFromMap(new IdentityHashMap<>());

         for (String id : init_List) {
            Identifier parsed = Identifier.tryParse(id);
            if (parsed != null) {
               BuiltInRegistries.BLOCK.getOptional(parsed).ifPresent(built::add);
            }
         }

         known = built;
         init_Set = known;
      }

      return known.contains(block);
   }

   public static OreBlockCatalog.Inner_DataUtil init(String blockId) {
      if (blockId == null) {
         return null;
      }

      String path = blockId.strip().toLowerCase(Locale.ROOT);
      int colon = path.indexOf(58);
      if (colon >= 0) {
         path = path.substring(colon + 1);
      }

      for (OreBlockCatalog.Inner_DataUtil kind : OreBlockCatalog.Inner_DataUtil.values()) {
         if (path.contains(kind.read_Str)) {
            return kind;
         }
      }

      return null;
   }

   public enum Inner_DataUtil {
      COAL("coal", "Coal", -11645352, "coal"),
      IRON("iron", "Iron", -2579078, "iron"),
      GOLD("gold", "Gold", -204725, "gold"),
      REDSTONE("redstone", "Redstone", -50373, "redstone"),
      DIAMOND("diamond", "Diamond", -11869223, "diamond"),
      LAPIS("lapis", "Lapis", -12950816, "lapis"),
      COPPER("copper", "Copper", -2065590, "copper"),
      EMERALD("emerald", "Emerald", -13510293, "emerald"),
      QUARTZ("quartz", "Quartz", -1187110, "quartz"),
      DEBRIS("debris", "Ancient Debris", -4689959, "ancient_debris");

      public final String init_Str;
      public final String load_Str;
      public final int init_I;
      public final String read_Str;

      Inner_DataUtil(String id, String label, int defaultColor, String match) {
         this.init_Str = id;
         this.load_Str = label;
         this.init_I = defaultColor;
         this.read_Str = match;
      }

      public String init() {
         return "ore-color-" + this.init_Str;
      }
   }

   public static final class Inner_StreamUtil {
      private static final Map<BlockState, Integer> init_Map = new IdentityHashMap<>();
      private static volatile BlockState[] init_BlockStateArr = new BlockState[0];
      private static volatile OreBlockCatalog.Inner_DataUtil[] init_CachePool_DataUtilArr = new OreBlockCatalog.Inner_DataUtil[0];

      private Inner_StreamUtil() {
      }

      private static synchronized int init(BlockState state, OreBlockCatalog.Inner_DataUtil kind) {
         Integer existing = init_Map.get(state);
         if (existing != null) {
            return existing;
         }

         int id = init_BlockStateArr.length;
         BlockState[] nextStates = Arrays.copyOf(init_BlockStateArr, id + 1);
         OreBlockCatalog.Inner_DataUtil[] nextKinds = Arrays.copyOf(init_CachePool_DataUtilArr, id + 1);
         nextStates[id] = state;
         nextKinds[id] = kind;
         init_Map.put(state, id);
         init_CachePool_DataUtilArr = nextKinds;
         init_BlockStateArr = nextStates;
         return id;
      }

      public static BlockState init_BlockState(int id) {
         BlockState[] snapshot = init_BlockStateArr;
         return id >= 0 && id < snapshot.length ? snapshot[id] : null;
      }

      public static OreBlockCatalog.Inner_DataUtil init_CachePool_DataUtil(int id) {
         OreBlockCatalog.Inner_DataUtil[] snapshot = init_CachePool_DataUtilArr;
         return id >= 0 && id < snapshot.length ? snapshot[id] : null;
      }

      public static int init() {
         return init_BlockStateArr.length;
      }

      public static int init(BlockState state) {
         if (state != null && OreBlockCatalog.init(state.getBlock())) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
            OreBlockCatalog.Inner_DataUtil kind = id == null ? null : OreBlockCatalog.init(id.toString());
            return kind == null ? -1 : init(state, kind);
         } else {
            return -1;
         }
      }
   }
}
