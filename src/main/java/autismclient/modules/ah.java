package autismclient.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ah {
   public static final String init_Str = "frame-esp";
   private static final int load_I = 1024;
   public static final List<ah.Inner_StreamUtil> init_List = List.of(
      new ah.Inner_StreamUtil("minecraft:elytra", "Elytra", -865100801),
      new ah.Inner_StreamUtil("minecraft:totem_of_undying", "Totem of Undying", -855649729),
      new ah.Inner_StreamUtil("minecraft:enchanted_golden_apple", "Enchanted Golden Apple", -855679797),
      new ah.Inner_StreamUtil("minecraft:netherite_ingot", "Netherite Ingot", -855672006),
      new ah.Inner_StreamUtil("minecraft:diamond", "Diamond", -867507242),
      new ah.Inner_StreamUtil("minecraft:nether_star", "Nether Star", -856493326),
      new ah.Inner_StreamUtil("minecraft:shulker_box", "Shulker Box", -860656641)
   );
   public static final String load_Str;
   public static final int init_I = -859848769;
   private static final Map<String, ah.Inner_StreamUtil> init_Map = new LinkedHashMap<>();
   private static volatile String read;
   private static volatile List<String> load_List = List.of();
   private static volatile ah.Inner_CachePool init_ah_CachePool;

   private ah() {
   }

   public static List<String> init(Module module) {
      String value = module == null ? "" : module.value("item-list");
      if (value == null) {
         value = "";
      }

      if (!value.equals(read)) {
         List<String> ids = new ArrayList<>();

         for (String raw : value.split("\\|")) {
            String token = raw.trim().toLowerCase(Locale.ROOT);
            if (!token.isEmpty()) {
               ids.add(token.contains(":") ? token : "minecraft:" + token);
            }
         }

         load_List = List.copyOf(ids);
         read = value;
      }

      return load_List;
   }

   public static boolean init_Z(Module module, String itemId) {
      return itemId != null && init(module).contains(itemId.toLowerCase(Locale.ROOT));
   }

   public static boolean init_Z(String itemId) {
      return itemId != null && init_Map.containsKey(itemId.toLowerCase(Locale.ROOT));
   }

   public static int init_I(String itemId) {
      ah.Inner_StreamUtil target = itemId == null ? null : init_Map.get(itemId.toLowerCase(Locale.ROOT));
      if (target != null) {
         return target.init_I();
      }

      Item item = init_Item(itemId);
      return item == null ? -859848769 : -872415232 | am.init(new ItemStack(item));
   }

   public static int init_I(Module module, String itemId) {
      String key = "color-" + itemId;
      String stored = module == null ? "" : module.value(key);
      int fallback = init_I(itemId);
      return stored != null && !stored.isBlank() ? am.init_I_Module(module, key, fallback) : fallback;
   }

   static Item init_Item(String itemId) {
      if (itemId != null && !itemId.isBlank()) {
         Identifier identifier = Identifier.tryParse(itemId.contains(":") ? itemId : "minecraft:" + itemId);
         return identifier == null ? null : BuiltInRegistries.ITEM.getOptional(identifier).orElse(null);
      } else {
         return null;
      }
   }

   static ah.Inner_CachePool init_ah_CachePool() {
      ah.Inner_CachePool current = init_ah_CachePool;
      int revision = al.init_I();
      boolean hidden = am.init_Z();
      if (current.init_I() == revision && current.init_Z() == hidden) {
         return current;
      }

      synchronized (ah.class) {
         current = init_ah_CachePool;
         if (current.init_I() == revision && current.init_Z() == hidden) {
            return current;
         }

         current = init(revision, hidden);
         init_ah_CachePool = current;
         return current;
      }
   }

   private static ah.Inner_CachePool init(int revision, boolean hidden) {
      if (hidden) {
         return ah.Inner_CachePool.init(revision, true);
      }

      Module module = al.init_Module_Str("frame-esp");
      if (module != null && module.isEnabled()) {
         Map<Item, Integer> colors = new IdentityHashMap<>();

         for (String id : init(module)) {
            Item item = init_Item(id);
            if (item != null) {
               colors.put(item, init_I(module, id));
            }
         }

         boolean shader = !"Box".equals(module.value("mode"));
         return new ah.Inner_CachePool(
            revision,
            false,
            true,
            shader,
            !shader,
            Boolean.parseBoolean(module.value("fill")),
            Boolean.parseBoolean(module.value("tracers")),
            Boolean.parseBoolean(module.value("other-frames")),
            Boolean.parseBoolean(module.value("empty-frames")),
            am.init_I_Module(module, "other-color", -859848769),
            Map.copyOf(colors)
         );
      } else {
         return ah.Inner_CachePool.init(revision, false);
      }
   }

   static int init(ah.Inner_CachePool snapshot, ItemFrame frame) {
      ItemStack stack = frame.getItem();
      if (stack.isEmpty()) {
         return snapshot.close() ? snapshot.load_I() : 0;
      } else {
         Integer color = snapshot.init_Map().get(stack.getItem());
         if (color != null) {
            return color;
         } else {
            return snapshot.flush() ? snapshot.load_I() : 0;
         }
      }
   }

   public static boolean init_Z() {
      ah.Inner_CachePool current = init_ah_CachePool();
      return current.load_Z() && current.read();
   }

   public static int init_I(Entity entity) {
      if (entity instanceof ItemFrame frame) {
         ah.Inner_CachePool current = init_ah_CachePool();
         if (!current.load_Z() || !current.read()) {
            return 0;
         }

         if (am.clear()) {
            return 0;
         }

         int color = init(current, frame);
         return color == 0 ? 0 : color | 0xFF000000;
      } else {
         return 0;
      }
   }

   public static boolean init_Z(Entity entity) {
      return init_I(entity) != 0;
   }

   static boolean init(ah.Inner_CachePool current) {
      return current.load_Z() && (current.write() || current.update() || current.sync());
   }

   static void init(ah.Inner_CachePool current, ClientLevel level, Player player, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
      if (level != null && player != null && current.load_Z()) {
         record Inner_DataUtil(ItemFrame init_ItemFrame, int init_I, double init_D) {
         }

         List<Inner_DataUtil> hits = new ArrayList<>();
         Vec3 eye = player.position();

         for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ItemFrame frame && !frame.isRemoved()) {
               int color = init(current, frame);
               if (color != 0) {
                  hits.add(new Inner_DataUtil(frame, color, frame.position().distanceToSqr(eye)));
               }
            }
         }

         if (hits.size() > 1024) {
            hits.sort(Comparator.comparingDouble(Inner_DataUtil::init_D));
            hits = new ArrayList<>(hits.subList(0, 1024));
         }

         hits.sort(
            Comparator.<Inner_DataUtil>comparingDouble(hitx -> hitx.init_ItemFrame().getX())
               .thenComparingDouble(hitx -> hitx.init_ItemFrame().getY())
               .thenComparingDouble(hitx -> hitx.init_ItemFrame().getZ())
         );

         for (Inner_DataUtil hit : hits) {
            AABB box = hit.init_ItemFrame().getBoundingBox().inflate(0.02);
            if (boxEmit != null) {
               boxEmit.accept(box, hit.init_I());
            }

            if (traceEmit != null) {
               traceEmit.accept(box.getCenter(), hit.init_I());
            }
         }
      }
   }

   static {
      StringBuilder def = new StringBuilder();

      for (ah.Inner_StreamUtil target : init_List) {
         init_Map.put(target.init_Str(), target);
         if (def.length() > 0) {
            def.append('|');
         }

         def.append(target.init_Str());
      }

      load_Str = def.toString();
      init_ah_CachePool = ah.Inner_CachePool.init(-2147483648, false);
   }

   record Inner_CachePool(
      int init_I,
      boolean init_Z,
      boolean load_Z,
      boolean read,
      boolean write,
      boolean update,
      boolean sync,
      boolean flush,
      boolean close,
      int load_I,
      Map<Item, Integer> init_Map
   ) {
      static ah.Inner_CachePool init(int revision, boolean hidden) {
         return new ah.Inner_CachePool(revision, hidden, false, false, false, false, false, false, false, 0, Map.of());
      }
   }

   public record Inner_StreamUtil(String init_Str, String load, int init_I) {
   }
}
