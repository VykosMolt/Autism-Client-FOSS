package autismclient.modules;

import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.bD;
import autismclient.util.bI;
import autismclient.util.bk;
import autismclient.util.ck;
import autismclient.util.g.PkgCls_a;
import autismclient.util.macro.MacroExecutor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class aD {
   public static final String init_Str = "hide";
   private static boolean init_Z;
   private static volatile aD.Inner_DataUtil init_aD_DataUtil;

   private aD() {
   }

   public static boolean init_Z() {
      aD.Inner_DataUtil flag = init_aD_DataUtil;
      if (flag != null) {
         return flag.init;
      }

      AutismConfig config = AutismConfig.getGlobal();
      AutismConfig.ModuleState state = config.modules.get("hide");
      boolean active = state != null && state.enabled;
      init_aD_DataUtil = new aD.Inner_DataUtil(active);
      return active;
   }

   public static void init(AutismConfig config) {
      AutismConfig.ModuleState state = config != null && config.modules != null ? config.modules.get("hide") : null;
      init_aD_DataUtil = new aD.Inner_DataUtil(state != null && state.enabled);
   }

   public static void init_V() {
      init_aD_DataUtil = null;
   }

   public static boolean load_Z() {
      return init_Z || init_Z();
   }

   public static boolean read_Z() {
      return init_Z();
   }

   public static boolean write_Z() {
      return init_Z || read_Z();
   }

   public static boolean init_Z(Module module) {
      return module != null && "hide".equals(module.id());
   }

   public static boolean init(String idOrName) {
      if (idOrName != null && !idOrName.isBlank()) {
         String normalized = idOrName.toLowerCase(Locale.ROOT).replace(' ', '-').replace("_", "-");
         return "hide".equals(normalized) || "panic-mode".equals(normalized) || "panicmode".equals(normalized) || "panic".equals(normalized);
      } else {
         return false;
      }
   }

   public static boolean load_Z(Module module) {
      return init_Z() && !init_Z(module);
   }

   public static void init_V(Module hideModule) {
      init_V();
      init(() -> {
         write_V();
         AutismClientMessaging.clearClientMessages();
         AutismConfig config = AutismConfig.getGlobal();
         Set<String> enabled = new LinkedHashSet<>();

         for (Module module : al.init_Collection()) {
            if (module != null && !init_Z(module) && module.isEnabledRaw()) {
               enabled.add(module.id());
            }
         }

         config.hideRestoreModules = new ArrayList<>(enabled);
         read_V();

         for (Module module : al.init_Collection()) {
            if (module != null && !init_Z(module) && module.isEnabledRaw()) {
               module.setEnabledSilently(false);
            }
         }

         am.init_V();
         config.save();
      });
   }

   public static void load_V(Module hideModule) {
      init_V();
      init(() -> {
         AutismConfig config = AutismConfig.getGlobal();
         List<String> restore = config.hideRestoreModules == null ? List.of() : new ArrayList<>(config.hideRestoreModules);
         config.hideRestoreModules = new ArrayList<>();
         config.save();

         for (String id : restore) {
            Module module = al.init_Module_Str(id);
            if (module != null && !init_Z(module)) {
               module.setEnabledSilently(true);
            }
         }

         am.init_V();
         config.save();
         if (config.lanSyncEnabled) {
            autismclient.util.aE.init_aE().init_V();
         }

         al.tick_V();
         autismclient.util.as.pop();
      });
   }

   public static void load_V() {
      init_V();
      if (init_Z()) {
         init(() -> {
            write_V();
            read_V();

            for (Module module : al.init_Collection()) {
               if (module != null && !init_Z(module) && module.isEnabledRaw()) {
                  module.setEnabledSilently(false);
               }
            }

            am.init_V();
            AutismClientMessaging.clearClientMessages();
         });
      }
   }

   private static void write_V() {
      read_V();
      autismclient.util.bi.init_V();
      bk.init_bk().update_V();
      autismclient.util.au.load();
      bI.init_V();
      bD.load();
      AutismClientMessaging.clearClientMessages();
      AutismClientMessaging.clearChatInputHistory();
   }

   public static void read_V() {
      autismclient.util.bg.update();
      al.tick_V();
      autismclient.util.as.pop();
      autismclient.util.aE.init_aE().read_V();
      if (MacroExecutor.write_Z()) {
         MacroExecutor.read_V();
      }

      autismclient.util.D.init_V();
      autismclient.util.macro.C.load_V();
      HashUtil.load_V();
      autismclient.util.aD.load_V();
      autismclient.util.macro.J.load_V();
      autismclient.util.macro.J.read_V();
      if (!AutismLiteVariant.enabled()) {
         PkgCls_a.init_V_Str("panic mode");
      }

      ck shared = ck.init_ck();
      shared.read_V_Z(true);
      shared.write(false);
      shared.run(false);
      shared.cache(false);
      shared.write_I();
      shared.init_V_Set_Z(Collections.emptySet(), false);
      shared.sync(false);
      shared.update(false);
      shared.next(false);
   }

   private static void init(Runnable action) {
      boolean previous = init_Z;
      init_Z = true;

      try {
         action.run();
      } finally {
         init_Z = previous;
      }
   }

   private record Inner_DataUtil(boolean init) {
   }
}
