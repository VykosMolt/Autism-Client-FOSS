package autismclient.protector;

import autismclient.AutismClientAddon;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

public final class RegistryCompatGuard {
   private static final int MAX_WARNINGS = 64;
   private static final ThreadLocal<RegistryCompatGuard.Inner_DataUtil> STATE = new ThreadLocal<>();
   private static final ThreadLocal<ConnectionPrivacyRegistry.ConnectionSession> currentSession = new ThreadLocal<>();

   private RegistryCompatGuard() {
   }

   public static void enter(ConnectionPrivacyRegistry.ConnectionSession context) {
      currentSession.set(context);
   }

   public static void exit() {
      currentSession.remove();
   }

   public static ConnectionPrivacyRegistry.ConnectionSession current() {
      ConnectionPrivacyRegistry.ConnectionSession context = currentSession.get();
      if (context == null || context.isLocalOrMemory()) {
         return null;
      }

      if (init_Z(context)) {
         return context;
      }

      currentSession.remove();
      return null;
   }

   public static void load(ConnectionPrivacyRegistry.ConnectionSession context) {
      if (init_Z(context)) {
         RegistryCompatGuard.Inner_DataUtil state = STATE.get();
         if (state == null) {
            state = new RegistryCompatGuard.Inner_DataUtil();
            STATE.set(state);
         }

         state.init_I++;
      }
   }

   private static boolean init_Z(ConnectionPrivacyRegistry.ConnectionSession context) {
      return context != null
         && !context.isLocalOrMemory()
         && ConnectionPrivacyRegistry.isActive(context)
         && (context.privacyMode() == ConnectionPrivacyRegistry.PrivacyMode.MODDED || autismclient.compat.via.ViaFabricPlusBridge.init_Z(context.connection()));
   }

   public static void load() {
      RegistryCompatGuard.Inner_DataUtil state = STATE.get();
      if (state != null) {
         state.init_I--;
         if (state.init_I <= 0) {
            STATE.remove();
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
         if (addWarning(state.init_Set, id)) {
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
         if (addWarning(state.load, message)) {
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
      RegistryCompatGuard.Inner_DataUtil state = STATE.get();
      return state != null && state.init_I > 0 ? state : null;
   }

   private static boolean addWarning(Set<String> warnings, String warning) {
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
