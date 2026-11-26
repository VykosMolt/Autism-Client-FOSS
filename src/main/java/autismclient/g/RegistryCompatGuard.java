package autismclient.g;

import autismclient.AutismClientAddon;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class RegistryCompatGuard {
   private static final int init_I = 64;
   private static final ThreadLocal<RegistryCompatGuard.Inner_DataUtil> init_ThreadLocal = new ThreadLocal<>();
   private static final ThreadLocal<ConnectionPrivacyRegistry.Inner_StreamUtil> load = new ThreadLocal<>();

   private RegistryCompatGuard() {
   }

   public static void init_V(ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      load.set(context);
   }

   public static void init_V() {
      load.remove();
   }

   public static ConnectionPrivacyRegistry.Inner_StreamUtil init_BufferHelper_Strea() {
      ConnectionPrivacyRegistry.Inner_StreamUtil context = load.get();
      if (context == null || context.read()) {
         return null;
      }

      if (init_Z(context)) {
         return context;
      }

      load.remove();
      return null;
   }

   public static void load(ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      if (init_Z(context)) {
         RegistryCompatGuard.Inner_DataUtil state = init_ThreadLocal.get();
         if (state == null) {
            state = new RegistryCompatGuard.Inner_DataUtil();
            init_ThreadLocal.set(state);
         }

         state.init_I++;
      }
   }

   private static boolean init_Z(ConnectionPrivacyRegistry.Inner_StreamUtil context) {
      return context != null
         && !context.read()
         && ConnectionPrivacyRegistry.init_Z(context)
         && (context.init_BufferHelper_Cache() == ConnectionPrivacyRegistry.Inner_CachePool.MODDED || autismclient.compat.a.ViaFabricPlusBridge.init_Z(context.init_Connection()));
   }

   public static void load() {
      RegistryCompatGuard.Inner_DataUtil state = init_ThreadLocal.get();
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
      RegistryCompatGuard.Inner_DataUtil state = init_TaskTimer_DataUtil();
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
      RegistryCompatGuard.Inner_DataUtil state = init_TaskTimer_DataUtil();
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
      RegistryCompatGuard.Inner_DataUtil state = init_TaskTimer_DataUtil();
      return state == null ? 0 : state.init_Set.size() + state.load.size();
   }

   private static RegistryCompatGuard.Inner_DataUtil init_TaskTimer_DataUtil() {
      RegistryCompatGuard.Inner_DataUtil state = init_ThreadLocal.get();
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
