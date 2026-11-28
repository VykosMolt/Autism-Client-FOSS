package autismclient.util;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.mixin.accessor.ClientCommonNetworkHandlerAccessor;
import autismclient.util.macro.MacroAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;

public final class JoinMacroController {
   private static boolean init_Z;
   private static boolean load;
   private static final JoinMacroController.ReadinessDetector detector = new JoinMacroController.ReadinessDetector();
   private static int init_I;
   private static volatile AutismMacro pendingMacro;
   private static volatile ClientConfigurationPacketListenerImpl configListener;
   private static volatile Thread workerThread;

   private JoinMacroController() {
   }

   public static JoinMacroController.JoinTiming timing() {
      return JoinMacroController.JoinTiming.init(AutismConfig.getGlobal().joinMacroTiming);
   }

   public static void init(JoinMacroController.JoinTiming timing) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroTiming = (timing == null ? JoinMacroController.JoinTiming.flush : timing).id();
      sync();
      config.save();
   }

   public static void setMacroName(String macroName) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroName = macroName == null ? "" : macroName.trim();
      config.joinMacroEnabled = !config.joinMacroName.isBlank();
      sync();
      config.save();
   }

   public static void init(boolean enabled) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroEnabled = enabled && config.joinMacroName != null && !config.joinMacroName.isBlank();
      sync();
      config.save();
   }

   public static JoinMacroController.JoinTrigger trigger() {
      return init(JoinMacroController.JoinTrigger.init_aA_CachePool_Str(AutismConfig.getGlobal().joinMacroTriggerJoin), keepEnabled());
   }

   public static void init(JoinMacroController.JoinTrigger triggerJoin) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroTriggerJoin = init(triggerJoin == null ? JoinMacroController.JoinTrigger.FIRST : triggerJoin, config.joinMacroKeepEnabled).init();
      sync();
      config.save();
   }

   public static boolean keepEnabled() {
      return AutismConfig.getGlobal().joinMacroKeepEnabled;
   }

   public static void load(boolean keepEnabled) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroKeepEnabled = keepEnabled;
      config.joinMacroTriggerJoin = init(JoinMacroController.JoinTrigger.init_aA_CachePool_Str(config.joinMacroTriggerJoin), keepEnabled).init();
      sync();
      config.save();
   }

   public static String macroName() {
      String name = AutismConfig.getGlobal().joinMacroName;
      return name == null ? "" : name.trim();
   }

   public static boolean hasMacro() {
      return macroName().length() > 0;
   }

   public static void init_V() {
      load = false;
      detector.init();
   }

   public static void onJoin() {
      init_V();
      init_Z = false;
      load = true;
      if (timing() == JoinMacroController.JoinTiming.JOINING) {
         load(JoinMacroController.JoinTiming.JOINING);
      }
   }

   public static void onWorldLoaded() {
      if (timing() == JoinMacroController.JoinTiming.WORLD) {
         load(JoinMacroController.JoinTiming.WORLD);
      }
   }

   public static void tick(Minecraft mc) {
      if (hasMacro() && !init_Z && mc != null) {
         JoinMacroController.JoinTiming timing = timing();
         if (detector.init(mc, timing, load)) {
            load(timing);
         }
      }
   }

   public static void write() {
      init_V();
      init_Z = false;
      pendingMacro = null;
      autismclient.util.custommenu.CustomMenuTracker.clear();
      configListener = null;
      Thread worker = workerThread;
      workerThread = null;
      if (worker != null) {
         worker.interrupt();
      }
   }

   public static void init(ClientConfigurationPacketListenerImpl listener) {
      write();
      configListener = listener;
      init_I++;
      if (hasMacro() && trigger().init_Z_I(init_I)) {
         AutismMacro selected = MacroLibraryManager.getInstance().findByName(macroName());
         if (selected != null) {
            AutismMacro copy = selected.copy();
            int prefix = 0;
            boolean sawCustomMenu = false;

            while (prefix < copy.actions.size()) {
               MacroAction action = copy.actions.get(prefix);
               if (action instanceof autismclient.util.macro.CustomMenuAction) {
                  sawCustomMenu = true;
               } else if (!init_Z(action)) {
                  break;
               }

               prefix++;
            }

            pendingMacro = copy.copy();
            if (prefix > 0) {
               pendingMacro.actions.subList(0, prefix).clear();
            }

            if (prefix != 0 && sawCustomMenu) {
               List<MacroAction> leading = List.copyOf(copy.actions.subList(0, prefix));
               Connection connection = ((ClientCommonNetworkHandlerAccessor) listener).getConnection();
               Thread worker = new Thread(() -> init(leading, connection), "AUTISM-Join-CustomMenu");
               worker.setDaemon(true);
               workerThread = worker;
               worker.start();
            }
         }
      }
   }

   private static boolean init_Z(MacroAction action) {
      return action instanceof autismclient.util.macro.WaitForGuiAction || action instanceof autismclient.util.macro.WaitGuiTypeAction || action instanceof autismclient.util.macro.DelayAction;
   }

   public static boolean isRunning() {
      Thread worker = workerThread;
      return worker != null && worker.isAlive();
   }

   public static boolean init(Packet<?> packet) {
      if (packet == null) {
         return false;
      } else {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.getConnection() != null) {
            mc.getConnection().send(packet);
            return true;
         } else {
            ClientConfigurationPacketListenerImpl listener = configListener;
            if (listener != null) {
               listener.send(packet);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public static void update() {
      write();
   }

   private static boolean auditStillOn(Connection bound) {
      Minecraft mc = Minecraft.getInstance();
      ClientPacketListener play = mc.getConnection();
      ClientConfigurationPacketListenerImpl config = configListener;
      Connection live = play != null ? play.getConnection() : config != null ? ((ClientCommonNetworkHandlerAccessor) config).getConnection() : null;
      return !Thread.currentThread().isInterrupted() && live == bound && bound.isConnected();
   }

   private static void init(List<MacroAction> actions, Connection connection) {
      for (MacroAction macroAction : actions) {
         if (macroAction != null && macroAction.isEnabled()) {
            if (init_Z(macroAction)) {
               init_V(macroAction);
               if (Thread.currentThread().isInterrupted()) {
                  return;
               }
            } else if (macroAction instanceof autismclient.util.macro.CustomMenuAction action) {
               long deadline = System.currentTimeMillis() + action.init();
               Minecraft mc = Minecraft.getInstance();
               CustomMenuSnapshot snapshot = null;
               long seenGeneration = 0L;
               autismclient.util.macro.CustomMenuActionPlanner.Inner_DataUtil prepared = null;
               String lastError = "";

               while (System.currentTimeMillis() <= deadline && !Thread.currentThread().isInterrupted()) {
                  seenGeneration = autismclient.util.custommenu.CustomMenuTracker.generation();
                  snapshot = autismclient.util.custommenu.CustomMenuTracker.snapshot();
                  if (snapshot != null) {
                     seenGeneration = snapshot.generation();
                  } else {
                     snapshot = autismclient.util.custommenu.DialogScreenBridge.snapshot(mc);
                  }

                  if (snapshot == null) {
                     LockSupport.parkNanos(20000000L);
                  } else {
                     prepared = autismclient.util.macro.CustomMenuActionPlanner.init(action, snapshot, JoinMacroController::load_Str);
                     if (prepared.isReady() || prepared.error() == null || !prepared.error().contains("unavailable")) {
                        break;
                     }

                     lastError = prepared.error();
                     prepared = null;
                     LockSupport.parkNanos(20000000L);
                  }
               }

               if (!auditStillOn(connection)) {
                  return;
               }

               if (snapshot != null && prepared != null) {
                  if (!prepared.isReady()) {
                     notifySkipped(prepared.error());
                  } else {
                     Screen answered = autismclient.util.custommenu.DialogScreenBridge.currentScreen(mc);
                     CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, prepared.submission());
                     if (!result.success()) {
                        notifySkipped(result.error());
                     } else {
                        for (Packet<?> packet : result.packets()) {
                           if (!auditStillOn(connection)) {
                              return;
                           }

                           if (!init(packet)) {
                              notifySkipped("Connection closed");
                              return;
                           }
                        }

                        autismclient.util.custommenu.CustomMenuTracker.init(seenGeneration, result.replacement(), snapshot.phase());
                        autismclient.util.custommenu.DialogScreenBridge.init(mc, result.clientAction(), answered);
                     }
                  }
               } else {
                  notifySkipped(lastError.isBlank() ? "Custom screen never appeared (timed out)" : lastError + " (timed out)");
               }
            }
         }
      }
   }

   private static void init_V(MacroAction action) {
      long timeoutMs;
      if (action instanceof autismclient.util.macro.WaitForGuiAction w) {
         timeoutMs = w.timeoutMs > 0 ? w.timeoutMs : 30000L;
      } else {
         if (!(action instanceof autismclient.util.macro.WaitGuiTypeAction w)) {
            if (action instanceof autismclient.util.macro.DelayAction d) {
               long ms = d.useTicks ? d.delayTicks * 50L : d.delayMs;
               init(Math.max(0L, Math.min(120000L, ms)));
               return;
            }

            return;
         }

         timeoutMs = w.timeoutMs > 0 ? w.timeoutMs : 30000L;
      }

      long deadline = System.currentTimeMillis() + Math.max(100L, Math.min(120000L, timeoutMs));

      while (System.currentTimeMillis() <= deadline && !Thread.currentThread().isInterrupted()) {
         if (autismclient.util.custommenu.CustomMenuTracker.snapshot() != null) {
            return;
         }

         LockSupport.parkNanos(20000000L);
      }
   }

   private static void init(long ms) {
      long deadline = System.currentTimeMillis() + ms;

      while (System.currentTimeMillis() < deadline && !Thread.currentThread().isInterrupted()) {
         LockSupport.parkNanos(20000000L);
      }
   }

   private static void notifySkipped(String reason) {
      bi.read(reason != null && !reason.isBlank() ? reason : "Join custom menu skipped");
   }

   private static String load_Str(String template) {
      return fillSecrets(template);
   }

   public static String fillSecrets(String template) {
      String out = template == null ? "" : template;

      for (Entry<String, String> entry : formValues().entrySet()) {
         out = out.replace("{secret." + entry.getKey() + "}", entry.getValue());
      }

      if (out.matches("(?s).*\\{secret\\.[^}]+}.*")) {
         return null;
      }

      Minecraft mc = Minecraft.getInstance();
      String username = mc != null && mc.getUser() != null ? mc.getUser().getName() : "";
      String accountId = mc != null && mc.getUser() != null ? mc.getUser().getProfileId().toString() : "";
      return out.replace("{username}", username).replace("{account_id}", accountId).replace("{profile_name}", "Rendered client");
   }

   public static Map<String, String> formValues() {
      Map<String, String> result = new LinkedHashMap<>();
      Map<String, String> stored = AutismConfig.getGlobal().joinMacroFormValues;
      if (stored == null) {
         return result;
      }

      stored.forEach((name, encoded) -> {
         try {
            byte[] plain = autismclient.util.mm.a.DataUtil.read(Base64.getDecoder().decode(encoded));
            if (plain != null) {
               result.put(name, new String(plain, StandardCharsets.UTF_8));
            }
         } catch (IllegalArgumentException var4) {
         }
      });
      return result;
   }

   public static boolean init(Map<String, String> values) {
      Map<String, String> sealed = new LinkedHashMap<>();
      if (values != null) {
         for (Entry<String, String> entry : values.entrySet()) {
            String name = JoinSecretKeyHelper.init(entry.getKey());
            if (!name.isEmpty()) {
               byte[] blob = autismclient.util.mm.a.DataUtil.init((entry.getValue() == null ? "" : entry.getValue()).getBytes(StandardCharsets.UTF_8));
               sealed.put(name, Base64.getEncoder().encodeToString(blob));
            }
         }
      }

      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroFormValues = sealed;
      config.save();
      return true;
   }

   private static void load(JoinMacroController.JoinTiming timing) {
      if (hasMacro() && !init_Z && trigger().init_Z_I(init_I)) {
         init_Z = true;
         String macroName = macroName();
         AutismMacro macro = pendingMacro != null ? pendingMacro : MacroLibraryManager.getInstance().findByName(macroName);
         if (macro == null) {
            bi.read("Join macro missing: " + macroName);
            setMacroName("");
         } else {
            boolean keepEnabled = keepEnabled();
            if (!keepEnabled) {
               AutismConfig config = AutismConfig.getGlobal();
               config.joinMacroName = "";
               config.joinMacroEnabled = false;
               config.save();
            } else if (trigger() != JoinMacroController.JoinTrigger.ANY) {
               init_I = 0;
            }

            Minecraft mc = Minecraft.getInstance();
            Runnable run = () -> {
               try {
                  if (!macro.actions.isEmpty()) {
                     macro.execute();
                  }

                  bi.init("Join macro: " + macro.name, -13248397);
               } catch (Throwable t) {
                  bi.write("Join macro failed.");
               }
            };
            if (mc != null) {
               mc.execute(run);
            } else {
               run.run();
            }
         }
      }
   }

   private static boolean isInWorld(Minecraft mc) {
      return mc.getConnection() != null && mc.player != null && mc.level != null;
   }

   private static boolean load(Minecraft mc) {
      return isInWorld(mc) && mc.gui.screen() == null;
   }

   public static String summary() {
      boolean keepEnabled = keepEnabled();
      return timing().load() + " / " + trigger().label(keepEnabled) + " / " + (keepEnabled ? "Stays Enabled" : "Clears After Run");
   }

   private static void sync() {
      init_I = 0;
      init_Z = false;
      init_V();
   }

   private static JoinMacroController.JoinTrigger init(JoinMacroController.JoinTrigger triggerJoin, boolean keepEnabled) {
      JoinMacroController.JoinTrigger value = triggerJoin == null ? JoinMacroController.JoinTrigger.FIRST : triggerJoin;
      if (keepEnabled && value == JoinMacroController.JoinTrigger.FIRST) {
         return JoinMacroController.JoinTrigger.ANY;
      } else {
         return !keepEnabled && value == JoinMacroController.JoinTrigger.ANY ? JoinMacroController.JoinTrigger.FIRST : value;
      }
   }

   public enum JoinTrigger {
      ANY("ANY", "Every Join", 0),
      FIRST("FIRST", "1st Join", 1),
      SECOND("SECOND", "2nd Join", 2),
      THIRD("THIRD", "3rd Join", 3),
      FOURTH("FOURTH", "4th Join", 4),
      FIFTH("FIFTH", "5th Join", 5),
      SIXTH_PLUS("SIXTH_PLUS", "6th+ Join", 6);

      private final String init_Str;
      private final String load_Str;
      private final int init_I;

      JoinTrigger(String id, String label, int number) {
         this.init_Str = id;
         this.load_Str = label;
         this.init_I = number;
      }

      public String init() {
         return this.init_Str;
      }

      public String load() {
         return this.load_Str;
      }

      public String label(boolean keepEnabled) {
         if (keepEnabled) {
            String var2;
            switch (this) {
               case ANY:
               case FIRST:
                  var2 = "Every Join";
                  break;
               case SECOND:
                  var2 = "Every 2nd Join";
                  break;
               case THIRD:
                  var2 = "Every 3rd Join";
                  break;
               case FOURTH:
                  var2 = "Every 4th Join";
                  break;
               case FIFTH:
                  var2 = "Every 5th Join";
                  break;
               case SIXTH_PLUS:
                  var2 = "Every 6th Join";
                  break;
               default:
                  throw new MatchException(null, null);
            }

            return var2;
         } else {
            String var10000;
            switch (this) {
               case ANY:
               case FIRST:
                  var10000 = "Next Join";
                  break;
               case SECOND:
                  var10000 = "After 1 Transfer";
                  break;
               case THIRD:
                  var10000 = "After 2 Transfers";
                  break;
               case FOURTH:
                  var10000 = "After 3 Transfers";
                  break;
               case FIFTH:
                  var10000 = "After 4 Transfers";
                  break;
               case SIXTH_PLUS:
                  var10000 = "After 5+ Transfers";
                  break;
               default:
                  throw new MatchException(null, null);
            }

            return var10000;
         }
      }

      public boolean init_Z_I(int joinOrdinal) {
         if (this == ANY) {
            return true;
         } else {
            return this == SIXTH_PLUS ? joinOrdinal >= this.init_I : joinOrdinal == this.init_I;
         }
      }

      public static JoinMacroController.JoinTrigger init_aA_CachePool_Str(String value) {
         if (value != null) {
            for (JoinMacroController.JoinTrigger target : values()) {
               if (target.init_Str.equalsIgnoreCase(value)) {
                  return target;
               }
            }

            try {
               int ordinal = Integer.parseInt(value.trim());
               JoinMacroController.JoinTrigger var10000;
               switch (ordinal) {
                  case 0:
                     var10000 = ANY;
                     break;
                  case 1:
                  default:
                     var10000 = ordinal >= 6 ? SIXTH_PLUS : FIRST;
                     break;
                  case 2:
                     var10000 = SECOND;
                     break;
                  case 3:
                     var10000 = THIRD;
                     break;
                  case 4:
                     var10000 = FOURTH;
                     break;
                  case 5:
                     var10000 = FIFTH;
               }

               return var10000;
            } catch (NumberFormatException var5) {
            }
         }

         return FIRST;
      }
   }

   public static final class ReadinessDetector {
      private int init;
      private int load;

      public void init() {
         this.init = 0;
         this.load = 0;
      }

      public boolean init(Minecraft mc, JoinMacroController.JoinTiming timing, boolean joined) {
         if (!JoinMacroController.isInWorld(mc)) {
            this.init();
            return false;
         }

         this.init++;
         boolean var10000;
         switch (timing) {
            case JOINING:
               var10000 = joined;
               break;
            case WORLD:
               var10000 = true;
               break;
            case FIRST_TICK:
               var10000 = this.init >= 1;
               break;
            case INVENTORY_READY:
               var10000 = mc.player != null && mc.player.inventoryMenu != null && mc.player.getInventory() != null;
               break;
            case PLAYABLE:
               var10000 = JoinMacroController.load(mc);
               break;
            case FULLY_READY:
               if (JoinMacroController.load(mc)) {
                  this.load++;
                  var10000 = this.load >= 2;
               } else {
                  this.load = 0;
                  var10000 = false;
               }
               break;
            default:
               throw new MatchException(null, null);
         }

         return var10000;
      }
   }

   public enum JoinTiming {
      JOINING("JOINING", "Joining"),
      WORLD("WORLD", "In World"),
      FIRST_TICK("FIRST_TICK", "First Tick"),
      INVENTORY_READY("INVENTORY_READY", "Inventory Ready"),
      PLAYABLE("PLAYABLE", "Playable"),
      FULLY_READY("FULLY_READY", "Fully Ready");

      private final String init_Str;
      private final String load_Str;
      public static final JoinMacroController.JoinTiming flush = PLAYABLE;

      JoinTiming(String id, String label) {
         this.init_Str = id;
         this.load_Str = label;
      }

      public String id() {
         return this.init_Str;
      }

      public String load() {
         return this.load_Str;
      }

      public JoinMacroController.JoinTiming init_aA_StreamUtil() {
         JoinMacroController.JoinTiming[] values = values();
         return values[(this.ordinal() + 1) % values.length];
      }

      public static JoinMacroController.JoinTiming init(String value) {
         if ("JOINING".equalsIgnoreCase(value)) {
            return JOINING;
         } else if ("WORLD".equalsIgnoreCase(value)) {
            return WORLD;
         } else if ("FIRST_TICK".equalsIgnoreCase(value) || "FIRSTTICK".equalsIgnoreCase(value)) {
            return FIRST_TICK;
         } else if ("INVENTORY_READY".equalsIgnoreCase(value) || "INVENTORY".equalsIgnoreCase(value)) {
            return INVENTORY_READY;
         } else if ("PLAYABLE".equalsIgnoreCase(value)) {
            return PLAYABLE;
         } else {
            return !"FULLY_READY".equalsIgnoreCase(value) && !"FULLYREADY".equalsIgnoreCase(value) ? flush : FULLY_READY;
         }
      }
   }
}
