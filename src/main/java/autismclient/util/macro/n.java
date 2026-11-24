package autismclient.util.macro;

import autismclient.util.bi;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;

public final class n {
   private static final ThreadLocal<m> init = ThreadLocal.withInitial(m::new);

   private n() {
   }

   public static void init_V() {
      load().init_V();
   }

   public static void init_V_Str_Obj(String name, Object value) {
      init_V_Str_l(name, value instanceof l typed ? typed : l.init_l_Obj(value));
   }

   public static void init_V_Str_l(String name, l value) {
      load().init(name, value);
   }

   public static void init_V_Map(Map<String, l> values) {
      load().init_V_Map(values);
   }

   public static void init_V_Str(String name) {
      load().init_V_Str(name);
   }

   public static String init_Str_Str_Minecraft(String name, Minecraft mc) {
      j.Inner_CachePool resolution = j.init_j_CachePool("{" + name + "}", load(), mc);
      return resolution.init_Z() ? resolution.init_Str() : "";
   }

   public static Optional<l> init_Optional_Str(String name) {
      return load().init_Optional_Str(name);
   }

   public static String load(String text, Minecraft mc) {
      j.Inner_CachePool resolution = init_j_CachePool_Str_Minecraft(text, mc);
      return resolution.init_Z() ? resolution.init_Str() : "";
   }

   public static j.Inner_CachePool init_j_CachePool_Str_Minecraft(String text, Minecraft mc) {
      return j.init_j_CachePool(text, load(), mc);
   }

   public static boolean init_Z_Str_j_CachePoolArr(String label, j.Inner_CachePool... resolutions) {
      LinkedHashSet<String> missing = new LinkedHashSet<>();
      boolean failed = false;

      for (j.Inner_CachePool resolution : resolutions) {
         if (resolution != null && !resolution.init_Z()) {
            failed = true;
            missing.addAll(resolution.init_List());
         }
      }

      if (!failed) {
         return true;
      }

      bi.write(missing.isEmpty() ? label + ": a value could not be resolved" : label + ": no value for {" + String.join("}, {", missing) + "}");
      return false;
   }

   public static m init_m() {
      return load();
   }

   private static m load() {
      m active = MacroExecutor.init_m();
      return active == null ? init.get() : active;
   }
}
