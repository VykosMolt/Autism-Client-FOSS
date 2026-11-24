package autismclient.g;

import autismclient.AutismClientAddon;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class TaskTimer {
   private static final int init_I = 64;
   private static final ThreadLocal<TaskTimer.Inner_DataUtil> init_ThreadLocal = new ThreadLocal<>();
   private static final ThreadLocal<BufferHelper.Inner_StreamUtil> load = new ThreadLocal<>();

   private TaskTimer() {
   }

   public static void init_V(BufferHelper.Inner_StreamUtil context) {
      load.set(context);
   }

   public static void init_V() {
      load.remove();
   }

   public static BufferHelper.Inner_StreamUtil init_BufferHelper_Strea() {
      BufferHelper.Inner_StreamUtil context = load.get();
      if (context == null || context.read()) {
         return null;
      }

      if (init_Z(context)) {
         return context;
      }

      load.remove();
      return null;
   }

   public static void load(BufferHelper.Inner_StreamUtil context) {
      if (init_Z(context)) {
         TaskTimer.Inner_DataUtil state = init_ThreadLocal.get();
         if (state == null) {
            state = new TaskTimer.Inner_DataUtil();
            init_ThreadLocal.set(state);
         }

         state.init_I++;
      }
   }

   private static boolean init_Z(BufferHelper.Inner_StreamUtil context) {
      return context != null
         && !context.read()
         && BufferHelper.init_Z(context)
         && (context.init_BufferHelper_Cache() == BufferHelper.Inner_CachePool.load || autismclient.compat.a.DataUtil.init_Z(context.init_Connection()));
   }

   public static void load() {
      TaskTimer.Inner_DataUtil state = init_ThreadLocal.get();
      if (state != null) {
         state.init_I--;
         if (state.init_I <= 0) {
            init_ThreadLocal.remove();
         }
      }
   }

   public static boolean init(ResourceKey<?> valueKey, Throwable error) {
      if (valueKey == null || error == null || init_TaskTimer_DataUtil() == null) {
         return false;
      }

      if (!valueKey.isFor(Registries.TRIM_MATERIAL)) {
         return false;
      }

      String message = error.getMessage();
      return error instanceof IllegalStateException && message != null && message.startsWith("Missing element ResourceKey[");
   }

   public static void init(ResourceKey<?> valueKey) {
      TaskTimer.Inner_DataUtil state = init_TaskTimer_DataUtil();
      if (valueKey != null && state != null) {
         String id = valueKey.identifier().toString();
         if (init(state.init_Set, id)) {
            AutismClientAddon.LOG
               .warn("[Autism] Server registry is missing trim material '{}'; omitted that item component so configuration can continue.", id);
         }
      }
   }

   public static boolean init_Z(Throwable error) {
      if (error == null || init_TaskTimer_DataUtil() == null) {
         return false;
      }

      if (!(error instanceof IllegalStateException)) {
         return false;
      }

      String message = error.getMessage();
      return message == null ? false : message.startsWith("Missing tag ") || message.startsWith("Missing element ");
   }

   public static void init_V(Throwable error) {
      TaskTimer.Inner_DataUtil state = init_TaskTimer_DataUtil();
      if (state != null) {
         String message = error != null && error.getMessage() != null ? error.getMessage() : "unknown";
         if (init(state.load, message)) {
            AutismClientAddon.LOG
               .warn(
                  "[Autism] Server registry is missing data for a delayed item component ({}); omitted that component so configuration can continue.", message
               );
         }
      }
   }

   static int init_I() {
      TaskTimer.Inner_DataUtil state = init_TaskTimer_DataUtil();
      return state == null ? 0 : state.init_Set.size() + state.load.size();
   }

   private static TaskTimer.Inner_DataUtil init_TaskTimer_DataUtil() {
      TaskTimer.Inner_DataUtil state = init_ThreadLocal.get();
      return state != null && state.init_I > 0 ? state : null;
   }

   private static boolean init(Set<String> warnings, String warning) {
      if (!warnings.contains(warning) && warnings.size() < 64) {
         warnings.add(warning);
         return true;
      } else {
         return false;
      }
   }

   private static final class Inner_DataUtil {
      private int init_I;
      private final Set<String> init_Set = new HashSet<>();
      private final Set<String> load = new HashSet<>();
   }
}
