package autismclient.modules;

import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import autismclient.util.PayloadSubscriptionManager;
import autismclient.util.PayloadStudyTracker;
import autismclient.util.OverlayManager;
import autismclient.util.AutismSessionState;
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
            autismclient.util.LanSyncManager.getInstance().startService();
         }

         al.tick_V();
         autismclient.util.VirtualInputDispatcher.pop();
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
      OverlayManager.getInstance().closeAll();
      autismclient.util.BlockPreviewRenderer.clear();
      PayloadStudyTracker.reset();
      PayloadSubscriptionManager.reset();
      AutismClientMessaging.clearClientMessages();
      AutismClientMessaging.clearChatInputHistory();
   }

   public static void read_V() {
      autismclient.util.PacketCaptureState.update();
      al.tick_V();
      autismclient.util.VirtualInputDispatcher.pop();
      autismclient.util.LanSyncManager.getInstance().shutdownSilently();
      if (MacroExecutor.isRunning()) {
         MacroExecutor.stopAll();
      }

      autismclient.util.ContainerCloseGuard.clear();
      autismclient.util.macro.PacketGateManager.clearAll();
      BlinkPacketManager.disableAndFlush();
      autismclient.util.SilentRotationManager.resetAll();
      autismclient.util.macro.PingSpoofScheduler.clearRunOverrides();
      autismclient.util.macro.PingSpoofScheduler.disable();
      if (!AutismLiteVariant.enabled()) {
         PkgCls_a.init_V_Str("panic mode");
      }

      AutismSessionState shared = AutismSessionState.getInstance();
      shared.setSendPacketsEnabled(true);
      shared.setDelayPacketsEnabled(false);
      shared.setStaggeredPacketSend(false);
      shared.setTimedCaptureMode(false);
      shared.cancelAndClearPacketQueues();
      shared.setXCarrySlotMask(Collections.emptySet(), false);
      shared.setXCarryForced(false);
      shared.setXCarryActive(false);
      shared.setDropNextContainerClose(false);
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
