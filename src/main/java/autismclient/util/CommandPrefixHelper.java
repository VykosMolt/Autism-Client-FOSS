package autismclient.util;

import java.util.ArrayList;
import java.util.List;

public final class CommandPrefixHelper {
   public static final List<String> init_List = List.of(".", "%", "-", "_", "*", "#", "@", "&", "=");
   private static volatile String write;
   private static volatile String update;
   private static volatile int init_I = -2147483648;
   private static volatile List<String> load_List = List.of();

   private CommandPrefixHelper() {
   }

   public static String init_Str() {
      autismclient.modules.ModuleRuntimeManager module = autismclient.modules.ModuleRuntimeManager.getInstance();
      String stored = module == null ? AutismConfig.getGlobal().commandPrefix : module.getCommandPrefix();
      String safeStored = stored == null ? "" : stored;
      String cachedStored = write;
      String cachedPrefix = update;
      if (safeStored.equals(cachedStored) && cachedPrefix != null) {
         return cachedPrefix;
      }

      String normalized = init_Str(safeStored);
      write = safeStored;
      update = normalized;
      return normalized;
   }

   public static String load_Str() {
      return ".";
   }

   public static String init_Str(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      return init_List.contains(selected) ? selected : load_Str();
   }

   public static boolean init_Z(String prefix) {
      String selected = prefix == null ? "" : prefix.trim();
      return init_List.contains(selected);
   }

   public static List<String> read_List() {
      int revision = autismclient.modules.al.init_I();
      List<String> names = load_List;
      if (init_I != revision) {
         List<String> rebuilt = new ArrayList<>(autismclient.modules.al.init_List());
         rebuilt.removeIf(name -> name == null || name.isBlank());
         rebuilt.sort(String::compareToIgnoreCase);
         names = List.copyOf(rebuilt);
         load_List = names;
         init_I = revision;
      }

      return new ArrayList<>(names);
   }

   public static boolean init(String moduleName, autismclient.util.macro.ak.Inner_StreamUtil toggleMode) {
      if (autismclient.modules.aD.init_Z() && !autismclient.modules.aD.init(moduleName)) {
         return false;
      }

      return autismclient.modules.al.init_Z_Str_ak_StreamUtil(moduleName, toggleMode);
   }
}
