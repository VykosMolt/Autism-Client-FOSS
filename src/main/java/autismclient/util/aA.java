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

public final class aA {
   private static boolean init_Z;
   private static boolean load;
   private static final aA.Inner_DataUtil init_aA_DataUtil = new aA.Inner_DataUtil();
   private static int init_I;
   private static volatile AutismMacro init_aM;
   private static volatile ClientConfigurationPacketListenerImpl init_ClientConfiguratio;
   private static volatile Thread init_Thread;

   private aA() {
   }

   public static aA.Inner_StreamUtil init_aA_StreamUtil() {
      return aA.Inner_StreamUtil.init(AutismConfig.getGlobal().joinMacroTiming);
   }

   public static void init(aA.Inner_StreamUtil timing) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroTiming = (timing == null ? aA.Inner_StreamUtil.flush : timing).init_Str();
      sync();
      config.save();
   }

   public static void init_V(String macroName) {
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

   public static aA.Inner_CachePool init_aA_CachePool() {
      return init(aA.Inner_CachePool.init_aA_CachePool_Str(AutismConfig.getGlobal().joinMacroTriggerJoin), init_Z());
   }

   public static void init(aA.Inner_CachePool triggerJoin) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroTriggerJoin = init(triggerJoin == null ? aA.Inner_CachePool.FIRST : triggerJoin, config.joinMacroKeepEnabled).init();
      sync();
      config.save();
   }

   public static boolean init_Z() {
      return AutismConfig.getGlobal().joinMacroKeepEnabled;
   }

   public static void load(boolean keepEnabled) {
      AutismConfig config = AutismConfig.getGlobal();
      config.joinMacroKeepEnabled = keepEnabled;
      config.joinMacroTriggerJoin = init(aA.Inner_CachePool.init_aA_CachePool_Str(config.joinMacroTriggerJoin), keepEnabled).init();
      sync();
      config.save();
   }

   public static String init_Str() {
      String name = AutismConfig.getGlobal().joinMacroName;
      return name == null ? "" : name.trim();
   }

   public static boolean load_Z() {
      return init_Str().length() > 0;
   }

   public static void init_V() {
      load = false;
      init_aA_DataUtil.init();
   }

   public static void load_V() {
      init_V();
      init_Z = false;
      load = true;
      if (init_aA_StreamUtil() == aA.Inner_StreamUtil.JOINING) {
         load(aA.Inner_StreamUtil.JOINING);
      }
   }

   public static void read_V() {
      if (init_aA_StreamUtil() == aA.Inner_StreamUtil.WORLD) {
         load(aA.Inner_StreamUtil.WORLD);
      }
   }

   public static void init_V(Minecraft mc) {
      if (load_Z() && !init_Z && mc != null) {
         aA.Inner_StreamUtil timing = init_aA_StreamUtil();
         if (init_aA_DataUtil.init(mc, timing, load)) {
            load(timing);
         }
      }
   }

   public static void write() {
      init_V();
      init_Z = false;
      init_aM = null;
      autismclient.util.b.CustomMenuTracker.init_V();
      init_ClientConfiguratio = null;
      Thread worker = init_Thread;
      init_Thread = null;
      if (worker != null) {
         worker.interrupt();
      }
   }

   public static void init(ClientConfigurationPacketListenerImpl listener) {
      write();
      init_ClientConfiguratio = listener;
      init_I++;
      if (load_Z() && init_aA_CachePool().init_Z_I(init_I)) {
         AutismMacro selected = aN.init_aN().init_aM_Str(init_Str());
         if (selected != null) {
            AutismMacro copy = selected.init_aM();
            int prefix = 0;
            boolean sawCustomMenu = false;

            while (prefix < copy.init_List.size()) {
               MacroAction action = copy.init_List.get(prefix);
               if (action instanceof autismclient.util.macro.CustomMenuAction) {
                  sawCustomMenu = true;
               } else if (!init_Z(action)) {
                  break;
               }

               prefix++;
            }

            init_aM = copy.init_aM();
            if (prefix > 0) {
               init_aM.init_List.subList(0, prefix).clear();
            }

            if (prefix != 0 && sawCustomMenu) {
               List<MacroAction> leading = List.copyOf(copy.init_List.subList(0, prefix));
               Connection connection = ((ClientCommonNetworkHandlerAccessor) listener).getConnection();
               Thread worker = new Thread(() -> init(leading, connection), "AUTISM-Join-CustomMenu");
               worker.setDaemon(true);
               init_Thread = worker;
               worker.start();
            }
         }
      }
   }

   private static boolean init_Z(MacroAction action) {
      return action instanceof autismclient.util.macro.WaitForGuiAction || action instanceof autismclient.util.macro.WaitGuiTypeAction || action instanceof autismclient.util.macro.DelayAction;
   }

   public static boolean read_Z() {
      Thread worker = init_Thread;
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
            ClientConfigurationPacketListenerImpl listener = init_ClientConfiguratio;
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
      ClientConfigurationPacketListenerImpl config = init_ClientConfiguratio;
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
               autismclient.util.macro.RenderState.Inner_DataUtil prepared = null;
               String lastError = "";

               while (System.currentTimeMillis() <= deadline && !Thread.currentThread().isInterrupted()) {
                  seenGeneration = autismclient.util.b.CustomMenuTracker.init_J();
                  snapshot = autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot();
                  if (snapshot != null) {
                     seenGeneration = snapshot.generation();
                  } else {
                     snapshot = autismclient.util.b.DialogScreenBridge.init_CustomMenuSnapshot(mc);
                  }

                  if (snapshot == null) {
                     LockSupport.parkNanos(20000000L);
                  } else {
                     prepared = autismclient.util.macro.RenderState.init(action, snapshot, aA::load_Str);
                     if (prepared.init_Z() || prepared.init_Str() == null || !prepared.init_Str().contains("unavailable")) {
                        break;
                     }

                     lastError = prepared.init_Str();
                     prepared = null;
                     LockSupport.parkNanos(20000000L);
                  }
               }

               if (!auditStillOn(connection)) {
                  return;
               }

               if (snapshot != null && prepared != null) {
                  if (!prepared.init_Z()) {
                     load_V(prepared.init_Str());
                  } else {
                     Screen answered = autismclient.util.b.DialogScreenBridge.init_Screen(mc);
                     CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, prepared.init_CustomMenuSubmissi());
                     if (!result.success()) {
                        load_V(result.error());
                     } else {
                        for (Packet<?> packet : result.packets()) {
                           if (!auditStillOn(connection)) {
                              return;
                           }

                           if (!init(packet)) {
                              load_V("Connection closed");
                              return;
                           }
                        }

                        autismclient.util.b.CustomMenuTracker.init(seenGeneration, result.replacement(), snapshot.phase());
                        autismclient.util.b.DialogScreenBridge.init(mc, result.clientAction(), answered);
                     }
                  }
               } else {
                  load_V(lastError.isBlank() ? "Custom screen never appeared (timed out)" : lastError + " (timed out)");
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
         if (autismclient.util.b.CustomMenuTracker.init_CustomMenuSnapshot() != null) {
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

   private static void load_V(String reason) {
      bi.read(reason != null && !reason.isBlank() ? reason : "Join custom menu skipped");
   }

   private static String load_Str(String template) {
      return init_Str(template);
   }

   public static String init_Str(String template) {
      String out = template == null ? "" : template;

      for (Entry<String, String> entry : init_Map().entrySet()) {
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

   public static Map<String, String> init_Map() {
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
            String name = aL.init(entry.getKey());
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

   private static void load(aA.Inner_StreamUtil timing) {
      if (load_Z() && !init_Z && init_aA_CachePool().init_Z_I(init_I)) {
         init_Z = true;
         String macroName = init_Str();
         AutismMacro macro = init_aM != null ? init_aM : aN.init_aN().init_aM_Str(macroName);
         if (macro == null) {
            bi.read("Join macro missing: " + macroName);
            init_V("");
         } else {
            boolean keepEnabled = init_Z();
            if (!keepEnabled) {
               AutismConfig config = AutismConfig.getGlobal();
               config.joinMacroName = "";
               config.joinMacroEnabled = false;
               config.save();
            } else if (init_aA_CachePool() != aA.Inner_CachePool.ANY) {
               init_I = 0;
            }

            Minecraft mc = Minecraft.getInstance();
            Runnable run = () -> {
               try {
                  if (!macro.init_List.isEmpty()) {
                     macro.init_V();
                  }

                  bi.init("Join macro: " + macro.init_Str, -13248397);
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

   private static boolean init_Z(Minecraft mc) {
      return mc.getConnection() != null && mc.player != null && mc.level != null;
   }

   private static boolean load(Minecraft mc) {
      return init_Z(mc) && mc.gui.screen() == null;
   }

   public static String load_Str() {
      boolean keepEnabled = init_Z();
      return init_aA_StreamUtil().load() + " / " + init_aA_CachePool().init_Str_Z(keepEnabled) + " / " + (keepEnabled ? "Stays Enabled" : "Clears After Run");
   }

   private static void sync() {
      init_I = 0;
      init_Z = false;
      init_V();
   }

   private static aA.Inner_CachePool init(aA.Inner_CachePool triggerJoin, boolean keepEnabled) {
      aA.Inner_CachePool value = triggerJoin == null ? aA.Inner_CachePool.FIRST : triggerJoin;
      if (keepEnabled && value == aA.Inner_CachePool.FIRST) {
         return aA.Inner_CachePool.ANY;
      } else {
         return !keepEnabled && value == aA.Inner_CachePool.ANY ? aA.Inner_CachePool.FIRST : value;
      }
   }

   public enum Inner_CachePool {
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

      Inner_CachePool(String id, String label, int number) {
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

      public String init_Str_Z(boolean keepEnabled) {
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

      public static aA.Inner_CachePool init_aA_CachePool_Str(String value) {
         if (value != null) {
            for (aA.Inner_CachePool target : values()) {
               if (target.init_Str.equalsIgnoreCase(value)) {
                  return target;
               }
            }

            try {
               int ordinal = Integer.parseInt(value.trim());
               aA.Inner_CachePool var10000;
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

   public static final class Inner_DataUtil {
      private int init;
      private int load;

      public void init() {
         this.init = 0;
         this.load = 0;
      }

      public boolean init(Minecraft mc, aA.Inner_StreamUtil timing, boolean joined) {
         if (!aA.init_Z(mc)) {
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
               var10000 = aA.load(mc);
               break;
            case FULLY_READY:
               if (aA.load(mc)) {
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

   public enum Inner_StreamUtil {
      JOINING("JOINING", "Joining"),
      WORLD("WORLD", "In World"),
      FIRST_TICK("FIRST_TICK", "First Tick"),
      INVENTORY_READY("INVENTORY_READY", "Inventory Ready"),
      PLAYABLE("PLAYABLE", "Playable"),
      FULLY_READY("FULLY_READY", "Fully Ready");

      private final String init_Str;
      private final String load_Str;
      public static final aA.Inner_StreamUtil flush = PLAYABLE;

      Inner_StreamUtil(String id, String label) {
         this.init_Str = id;
         this.load_Str = label;
      }

      public String init_Str() {
         return this.init_Str;
      }

      public String load() {
         return this.load_Str;
      }

      public aA.Inner_StreamUtil init_aA_StreamUtil() {
         aA.Inner_StreamUtil[] values = values();
         return values[(this.ordinal() + 1) % values.length];
      }

      public static aA.Inner_StreamUtil init(String value) {
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
