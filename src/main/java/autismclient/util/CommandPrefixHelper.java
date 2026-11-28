package autismclient.util;

import java.util.ArrayList;
import java.util.List;

public final class CommandPrefixHelper {
   public static final List<String> ALLOWED_PREFIXES = List.of(".", "%", "-", "_", "*", "#", "@", "&", "=");
   private static volatile String storedCache;
   private static volatile String prefixCache;
   private static volatile int namesRevision = -2147483648;
   private static volatile List<String> cachedNames = List.of();

   private CommandPrefixHelper() {
   }

   public static String currentPrefix() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      String stored = module == null ? AutismConfig.getGlobal().commandPrefix : module.getCommandPrefix();
      String safeStored = stored == null ? "" : stored;
      String cachedStored = storedCache;
      String cachedPrefix = prefixCache;
      if (safeStored.equals(cachedStored) && cachedPrefix != null) {
         return cachedPrefix;
      }

      String normalized = normalizePrefix(safeStored);
      storedCache = safeStored;
      prefixCache = normalized;
      return normalized;
   }

   public static String defaultPrefix() {
      return ".";
   }

   public static String normalizePrefix(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      return ALLOWED_PREFIXES.contains(selected) ? selected : defaultPrefix();
   }

   public static boolean isAllowedPrefix(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      return ALLOWED_PREFIXES.contains(selected);
   }

   public static List<String> moduleNames() {
      int revision = autismclient.modules.al.init_I();
      List<String> names = cachedNames;
      if (namesRevision != revision) {
         List<String> rebuilt = new ArrayList<>(autismclient.modules.al.init_List());
         rebuilt.removeIf(name -> name == null || name.isBlank());
         rebuilt.sort(String::compareToIgnoreCase);
         names = List.copyOf(rebuilt);
         cachedNames = names;
         namesRevision = revision;
      }

      return new ArrayList<>(names);
   }

   public static boolean toggleModule(String moduleName, autismclient.util.macro.ak.Inner_StreamUtil toggleMode) {
      if (autismclient.modules.aD.init_Z() && !autismclient.modules.aD.init(moduleName)) {
         return false;
      }

      return autismclient.modules.al.init_Z_Str_ak_StreamUtil(moduleName, toggleMode);
   }
}
