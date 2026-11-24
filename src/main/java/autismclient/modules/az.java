package autismclient.modules;

import autismclient.util.AutismLiteVariant;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public final class az {
   private static volatile boolean f_c_Z;
   static volatile boolean init_Z;
   static volatile boolean load_Z;
   static volatile boolean read;
   static volatile boolean write;
   static volatile boolean update;
   static volatile boolean sync;
   static volatile boolean flush;
   static volatile boolean close;
   static volatile boolean open;
   static volatile boolean poll;
   static volatile boolean push;
   static volatile boolean pop;
   static volatile boolean tick;
   static volatile boolean run;
   static volatile boolean get;
   static volatile boolean set;
   static volatile boolean put;
   static volatile boolean add;
   static volatile boolean clear;
   static volatile boolean reset;
   static volatile boolean start;
   static volatile boolean stop;
   static volatile boolean next;
   static volatile boolean prev;
   static volatile boolean valid;
   static volatile boolean size;
   static volatile boolean data;
   static volatile boolean value;
   static volatile boolean cache;
   static volatile boolean state;
   static volatile boolean queue;
   static volatile boolean buffer;
   static volatile boolean entry;
   static volatile boolean node;
   static volatile boolean task;
   static volatile boolean pool;
   static volatile boolean index;
   static volatile boolean count;
   static volatile boolean flags;
   static volatile boolean level;
   static volatile boolean frame;
   static volatile boolean block;
   static volatile boolean chunk;
   static volatile boolean table;
   static volatile boolean store;
   static volatile long init_J;
   static volatile boolean drain;
   static volatile float init_F;
   static volatile float load_F;
   static volatile boolean fill;
   static volatile boolean mark;
   static volatile boolean scan;
   static volatile boolean parse;
   static volatile boolean bind;
   static volatile boolean f_a_Z;
   static volatile boolean f_b_Z;
   static volatile List<String> init_List = List.of();
   static volatile List<String> load_List = List.of();

   private az() {
   }

   static void init_V() {
      f_c_Z = false;
   }

   static void load_V() {
      f_c_Z = true;
   }

   private static boolean bind() {
      return AutismLiteVariant.enabled() || autismclient.e.TokenStore.init();
   }

   public static boolean init_Z() {
      return f_c_Z && bind() && init_Z;
   }

   public static boolean load_Z() {
      return f_c_Z && bind() && load_Z;
   }

   public static boolean read() {
      return f_c_Z && bind() && read;
   }

   public static boolean write() {
      return f_c_Z && bind() && write;
   }

   public static boolean update() {
      return f_c_Z && bind() && update;
   }

   public static boolean sync() {
      return f_c_Z && bind() && sync;
   }

   public static boolean flush() {
      return f_c_Z && bind() && flush;
   }

   public static boolean close() {
      return f_c_Z && bind() && close;
   }

   public static boolean open() {
      return f_c_Z && bind() && open;
   }

   public static boolean poll() {
      return f_c_Z && bind() && poll;
   }

   public static boolean push() {
      return f_c_Z && bind() && push;
   }

   public static boolean pop() {
      return f_c_Z && bind() && pop;
   }

   public static boolean tick() {
      return f_c_Z && bind() && tick;
   }

   public static boolean run() {
      return f_c_Z && bind() && run;
   }

   public static boolean get() {
      return f_c_Z && bind() && get;
   }

   public static boolean set() {
      return f_c_Z && bind() && set;
   }

   public static boolean put() {
      return f_c_Z && bind() && put;
   }

   public static boolean add() {
      return f_c_Z && bind() && add;
   }

   public static boolean clear() {
      return f_c_Z && bind() && clear;
   }

   public static boolean reset() {
      return f_c_Z && bind() && reset;
   }

   public static boolean start() {
      return f_c_Z && bind() && start;
   }

   public static boolean stop() {
      return f_c_Z && bind() && stop;
   }

   public static boolean next() {
      return f_c_Z && bind() && next;
   }

   public static boolean prev() {
      return f_c_Z && bind() && prev;
   }

   public static boolean valid() {
      return f_c_Z && bind() && valid;
   }

   public static boolean size() {
      return f_c_Z && bind() && size;
   }

   public static boolean data() {
      return f_c_Z && bind() && data;
   }

   public static boolean value() {
      return f_c_Z && bind() && value;
   }

   public static boolean cache() {
      return f_c_Z && bind() && cache;
   }

   public static boolean state() {
      return f_c_Z && bind() && state;
   }

   public static boolean queue() {
      return f_c_Z && bind() && queue;
   }

   public static boolean buffer() {
      return f_c_Z && bind() && buffer;
   }

   public static boolean entry() {
      return f_c_Z && bind() && entry;
   }

   public static boolean node() {
      return f_c_Z && bind() && node;
   }

   public static boolean task() {
      return f_c_Z && bind() && task;
   }

   public static boolean pool() {
      return f_c_Z && bind() && count;
   }

   public static boolean index() {
      return f_c_Z && bind() && flags;
   }

   public static boolean count() {
      return f_c_Z && bind() && level;
   }

   public static boolean flags() {
      return f_c_Z && bind() && frame;
   }

   public static boolean level() {
      return f_c_Z && bind() && block;
   }

   public static boolean frame() {
      return f_c_Z && bind() && chunk;
   }

   public static boolean block() {
      return f_c_Z && bind() && table;
   }

   public static boolean chunk() {
      return f_c_Z && bind() && store;
   }

   public static long init_J() {
      return init_J;
   }

   public static boolean table() {
      return f_c_Z && bind() && drain;
   }

   public static float init_F() {
      return init_F;
   }

   public static float load_F() {
      return load_F;
   }

   public static boolean store() {
      return f_c_Z && bind() && fill;
   }

   public static boolean drain() {
      return f_c_Z && bind() && mark;
   }

   public static boolean fill() {
      return f_c_Z && bind() && scan;
   }

   public static boolean mark() {
      return f_c_Z && bind() && parse;
   }

   public static boolean scan() {
      return f_c_Z && bind() && bind;
   }

   public static boolean parse() {
      return f_c_Z && bind() && f_a_Z;
   }

   public static boolean init(ParticleType<?> type) {
      if (!f_c_Z || !bind()) {
         return false;
      }

      if (index) {
         return true;
      }

      if (pool && type != null) {
         Identifier id = BuiltInRegistries.PARTICLE_TYPE.getKey(type);
         if (id != null && id.getPath().equals("firework")) {
            return true;
         }
      }

      return false;
   }

   public static boolean init(Entity entity) {
      return entity != null && init(entity.getType());
   }

   public static boolean init(EntityType<?> type) {
      if (f_c_Z && type != null && bind()) {
         List<String> list = init_List;
         if (list.isEmpty()) {
            return false;
         }

         Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
         return id != null && init(list, id);
      } else {
         return false;
      }
   }

   public static boolean load(EntityType<?> type) {
      return f_c_Z && bind() && f_b_Z && init(type);
   }

   public static boolean init(Block block) {
      if (f_c_Z && block != null && bind()) {
         List<String> list = load_List;
         if (list.isEmpty()) {
            return false;
         }

         Identifier id = BuiltInRegistries.BLOCK.getKey(block);
         return id != null && init(list, id);
      } else {
         return false;
      }
   }

   private static boolean init(List<String> list, Identifier id) {
      String full = id.toString();
      String path = id.getPath();

      for (String entry : list) {
         if (entry != null) {
            String e = entry.trim().toLowerCase(Locale.ROOT);
            if (e.equals(full) || e.equals(path)) {
               return true;
            }
         }
      }

      return false;
   }
}
