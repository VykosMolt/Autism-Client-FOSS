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

public final class FrameEspCatalog {
   public static final String init_Str = "frame-esp";
   private static final int load_I = 1024;
   public static final List<FrameEspCatalog.Inner_StreamUtil> TARGETS = List.of(
      new FrameEspCatalog.Inner_StreamUtil("minecraft:elytra", "Elytra", -865100801),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:totem_of_undying", "Totem of Undying", -855649729),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:enchanted_golden_apple", "Enchanted Golden Apple", -855679797),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:netherite_ingot", "Netherite Ingot", -855672006),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:diamond", "Diamond", -867507242),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:nether_star", "Nether Star", -856493326),
      new FrameEspCatalog.Inner_StreamUtil("minecraft:shulker_box", "Shulker Box", -860656641)
   );
   public static final String DEFAULT_IDS;
   public static final int DEFAULT_COLOR = -859848769;
   private static final Map<String, FrameEspCatalog.Inner_StreamUtil> TARGETS_BY_ID = new LinkedHashMap<>();
   private static volatile String read;
   private static volatile List<String> selectedIds = List.of();
   private static volatile FrameEspCatalog.Inner_CachePool currentSnapshot;

   private FrameEspCatalog() {
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

         selectedIds = List.copyOf(ids);
         read = value;
      }

      return selectedIds;
   }

   public static boolean isSelected(Module module, String itemId) {
      return itemId != null && init(module).contains(itemId.toLowerCase(Locale.ROOT));
   }

   public static boolean isKnown(String itemId) {
      return itemId != null && TARGETS_BY_ID.containsKey(itemId.toLowerCase(Locale.ROOT));
   }

   public static int defaultColor(String itemId) {
      FrameEspCatalog.Inner_StreamUtil target = itemId == null ? null : TARGETS_BY_ID.get(itemId.toLowerCase(Locale.ROOT));
      if (target != null) {
         return target.defaultColor();
      }

      Item item = parseItem(itemId);
      return item == null ? -859848769 : -872415232 | am.init(new ItemStack(item));
   }

   public static int colorFor(Module module, String itemId) {
      String key = "color-" + itemId;
      String stored = module == null ? "" : module.value(key);
      int fallback = defaultColor(itemId);
      return stored != null && !stored.isBlank() ? am.init_I_Module(module, key, fallback) : fallback;
   }

   static Item parseItem(String itemId) {
      if (itemId != null && !itemId.isBlank()) {
         Identifier identifier = Identifier.tryParse(itemId.contains(":") ? itemId : "minecraft:" + itemId);
         return identifier == null ? null : BuiltInRegistries.ITEM.getOptional(identifier).orElse(null);
      } else {
         return null;
      }
   }

   static FrameEspCatalog.Inner_CachePool snapshot() {
      FrameEspCatalog.Inner_CachePool current = currentSnapshot;
      int revision = al.init_I();
      boolean hidden = am.init_Z();
      if (current.revision() == revision && current.hidden() == hidden) {
         return current;
      }

      synchronized (FrameEspCatalog.class) {
         current = currentSnapshot;
         if (current.revision() == revision && current.hidden() == hidden) {
            return current;
         }

         current = init(revision, hidden);
         currentSnapshot = current;
         return current;
      }
   }

   private static FrameEspCatalog.Inner_CachePool init(int revision, boolean hidden) {
      if (hidden) {
         return FrameEspCatalog.Inner_CachePool.init(revision, true);
      }

      Module module = al.init_Module_Str("frame-esp");
      if (module != null && module.isEnabled()) {
         Map<Item, Integer> colors = new IdentityHashMap<>();

         for (String id : init(module)) {
            Item item = parseItem(id);
            if (item != null) {
               colors.put(item, colorFor(module, id));
            }
         }

         boolean shader = !"Box".equals(module.value("mode"));
         return new FrameEspCatalog.Inner_CachePool(
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
         return FrameEspCatalog.Inner_CachePool.init(revision, false);
      }
   }

   static int init(FrameEspCatalog.Inner_CachePool snapshot, ItemFrame frame) {
      ItemStack stack = frame.getItem();
      if (stack.isEmpty()) {
         return snapshot.close() ? snapshot.load_I() : 0;
      } else {
         Integer color = snapshot.colors().get(stack.getItem());
         if (color != null) {
            return color;
         } else {
            return snapshot.flush() ? snapshot.load_I() : 0;
         }
      }
   }

   public static boolean init_Z() {
      FrameEspCatalog.Inner_CachePool current = snapshot();
      return current.load_Z() && current.read();
   }

   public static int outlineColor(Entity entity) {
      if (entity instanceof ItemFrame frame) {
         FrameEspCatalog.Inner_CachePool current = snapshot();
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

   public static boolean hasOutline(Entity entity) {
      return outlineColor(entity) != 0;
   }

   static boolean init(FrameEspCatalog.Inner_CachePool current) {
      return current.load_Z() && (current.write() || current.update() || current.sync());
   }

   static void init(FrameEspCatalog.Inner_CachePool current, ClientLevel level, Player player, BiConsumer<AABB, Integer> boxEmit, BiConsumer<Vec3, Integer> traceEmit) {
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

      for (FrameEspCatalog.Inner_StreamUtil target : TARGETS) {
         TARGETS_BY_ID.put(target.id(), target);
         if (def.length() > 0) {
            def.append('|');
         }

         def.append(target.id());
      }

      DEFAULT_IDS = def.toString();
      currentSnapshot = FrameEspCatalog.Inner_CachePool.init(-2147483648, false);
   }

   record Inner_CachePool(
      int revision,
      boolean hidden,
      boolean load_Z,
      boolean read,
      boolean write,
      boolean update,
      boolean sync,
      boolean flush,
      boolean close,
      int load_I,
      Map<Item, Integer> colors
   ) {
      static FrameEspCatalog.Inner_CachePool init(int revision, boolean hidden) {
         return new FrameEspCatalog.Inner_CachePool(revision, hidden, false, false, false, false, false, false, false, 0, Map.of());
      }
   }

   public record Inner_StreamUtil(String id, String load, int defaultColor) {
   }
}
