package autismclient.util.g;

import autismclient.AutismClientAddon;
import autismclient.modules.aC;
import autismclient.util.ax;
import autismclient.util.bi;
import autismclient.util.bk;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;

public final class ValueSink {
   private static volatile ValueSink.Inner_DataUtil init_ValueSink_DataUtil = ValueSink.Inner_DataUtil.init;
   private static volatile String init_Str;
   private static final ConcurrentHashMap<String, long[]> init_ConcurrentHashMap = new ConcurrentHashMap<>();
   private static final long init_J = 400L;

   private ValueSink() {
   }

   public static boolean init_Z() {
      return init_ValueSink_DataUtil != ValueSink.Inner_DataUtil.init;
   }

   public static boolean init_Z(String accountId) {
      return accountId != null && accountId.equals(init_Str);
   }

   public static String init_Str() {
      return init_Str;
   }

   public static boolean load_Z(String accountId) {
      if (accountId == null) {
         return false;
      }

      if (accountId.equals(init_Str)) {
         return true;
      }

      if (init_ValueSink_DataUtil != ValueSink.Inner_DataUtil.init) {
         return false;
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.player != null && mc.getConnection() != null) {
         PacketUtil mgr = PacketUtil.load_PacketUtil();
         if (mgr != null && mgr.load_Z()) {
            ClassCache session = mgr.init_ClassCache_Str(accountId);
            if (session != null && session.run_Z()) {
               long now = System.currentTimeMillis();
               long[] cached = init_ConcurrentHashMap.get(accountId);
               if (cached != null && now < cached[0]) {
                  return cached[1] != 0L;
               }

               boolean ok = init(mc, session) != null;
               if (init_ConcurrentHashMap.size() >= 512) {
                  init_ConcurrentHashMap.entrySet().removeIf(e -> now >= e.getValue()[0]);
                  if (init_ConcurrentHashMap.size() >= 512) {
                     init_ConcurrentHashMap.clear();
                  }
               }
               init_ConcurrentHashMap.put(accountId, new long[]{now + 400L, ok ? 1L : 0L});
               return ok;
            } else {
               return false;
            }
         } else {
            init_ConcurrentHashMap.clear();
            return false;
         }
      } else {
         init_ConcurrentHashMap.clear();
         return false;
      }
   }

   public static void init(String accountId, Screen screen) {
      if (accountId != null) {
         if (accountId.equals(init_Str)) {
            init_V();
         } else if (init_ValueSink_DataUtil != ValueSink.Inner_DataUtil.init) {
            load_V("Leave the current bot first.");
         } else {
            init_V(accountId);
         }
      }
   }

   private static void init_V(String accountId) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || mc.player == null || mc.getConnection() == null) {
         load_V("Join a world near the bot first.");
      } else if (aC.init_Z()) {
         load_V("Disable freecam first.");
      } else {
         PacketUtil mgr = PacketUtil.load_PacketUtil();
         if (mgr != null && mgr.load_Z()) {
            ClassCache session = mgr.init_ClassCache_Str(accountId);
            if (session != null && session.run_Z()) {
               if (session.update_F() <= 0.0F) {
                  load_V("That bot is dead - wait for its respawn.");
               } else {
                  RemotePlayer bot = init(mc, session);
                  if (bot == null) {
                     load_V("That bot isn't near you (must be visible in your world).");
                  } else {
                     try {
                        session.sync_V_Z(true);
                        PoolEntry.load(session, bot);
                        PoolEntry.init_V_Minecraft(mc);
                        mc.setCameraEntity(bot);
                        mc.gui.setScreen((Screen)null);
                        bk.init_bk().open();
                        init_Str = accountId;
                        init_ValueSink_DataUtil = ValueSink.Inner_DataUtil.load;
                        load_V("You are now " + init_Str(accountId) + ".");
                     } catch (Throwable error) {
                        AutismClientAddon.LOG.error("POV pilot enter failed", error);
                        load_V("Could not become that bot: " + error.getClass().getSimpleName());

                        try {
                           PoolEntry.init_V_RemotePlayer(bot);
                        } catch (Throwable var7) {
                        }

                        session.sync_V_Z(false);
                        if (mc.player != null) {
                           mc.setCameraEntity(mc.player);
                        }

                        read();
                     }
                  }
               }
            } else {
               load_V("That bot isn't ready.");
            }
         } else {
            load_V("No bot batch is running.");
         }
      }
   }

   public static void init_V() {
      if (init_ValueSink_DataUtil != ValueSink.Inner_DataUtil.init) {
         Minecraft mc = Minecraft.getInstance();

         try {
            Screen current = mc.gui.screen();
            if (current instanceof AbstractSignEditScreen || current instanceof BookEditScreen || current instanceof BookViewScreen) {
               mc.gui.setScreen((Screen)null);
            }
         } catch (Throwable var8) {
         }

         PacketUtil mgr = PacketUtil.load_PacketUtil();
         ClassCache session = mgr == null ? null : mgr.init_ClassCache_Str(init_Str);
         RemotePlayer bot = session == null ? null : init(mc, session);
         boolean macroAuthority = session != null && session.put_Z();

         try {
            PoolEntry.init_V_RemotePlayer(bot);
         } catch (Throwable error) {
            AutismClientAddon.LOG.error("POV pilot end failed", error);
         }

         if (mc.player != null) {
            mc.setCameraEntity(mc.player);
         }

         if (session != null) {
            try {
               if (!macroAuthority && session.run_Z()) {
                  session.open_V();
               }
            } catch (Throwable error) {
               AutismClientAddon.LOG.warn("POV exit position sync failed", error);
            }

            session.sync_V_Z(false);
         }

         init(mc);
         read();
         ax.read();
      }
   }

   public static void load() {
      if (init_ValueSink_DataUtil != ValueSink.Inner_DataUtil.init) {
         Minecraft mc = Minecraft.getInstance();
         if (!aC.init_Z() && (mc.gui.screen() instanceof InventoryScreen || mc.gui.screen() instanceof CreativeModeInventoryScreen)) {
            mc.gui.setScreen((Screen)null);
         }

         PacketUtil mgr = PacketUtil.load_PacketUtil();
         ClassCache session = mgr == null ? null : mgr.init_ClassCache_Str(init_Str);
         if (session == null || mc.level == null || mc.player == null) {
            init_V();
         } else if (!session.run_Z()) {
            ClassCache.Inner_PathHelper status = session.init_ClassCache_PathHel();
            boolean switching = status == ClassCache.Inner_PathHelper.update
               || status == ClassCache.Inner_PathHelper.read
               || status == ClassCache.Inner_PathHelper.write
               || status == ClassCache.Inner_PathHelper.sync;
            init_V();
            if (switching) {
               load_V("Bot switched servers - session continues headless.");
            }
         } else if (session.update_F() <= 0.0F) {
            init_V();
            load_V("Bot died - POV ended.");
         } else {
            RemotePlayer bot = init(mc, session);
            if (bot != null && !bot.isRemoved() && !bot.isPassenger()) {
               if (mc.getCameraEntity() != bot) {
                  init_V();
               }
            } else {
               init_V();
               if (bot == null || bot.isRemoved()) {
                  load_V("Bot changed world - session stays connected.");
               }
            }
         }
      }
   }

   private static RemotePlayer init(Minecraft mc, ClassCache session) {
      if (mc.level == null) {
         return null;
      }

      try {
         UUID uuid = session.init_UUID();
         if (uuid != null && mc.level.getPlayerByUUID(uuid) instanceof RemotePlayer byUuid) {
            return byUuid;
         }

         String name = session.init_GameProfile() == null ? null : session.init_GameProfile().name();
         if (name != null && !name.isBlank()) {
            for (Player player : mc.level.players()) {
               if (player != mc.player && player instanceof RemotePlayer remote && player.getName().getString().equalsIgnoreCase(name)) {
                  return remote;
               }
            }
         }
      } catch (Throwable var7) {
      }

      return null;
   }

   private static void init(Minecraft mc) {
      try {
         if (mc.gui.screen() instanceof autismclient.gui.d.TaskTimer) {
            mc.gui.setScreen((Screen)null);
         }
      } catch (Throwable var2) {
      }
   }

   private static void read() {
      init_ValueSink_DataUtil = ValueSink.Inner_DataUtil.init;
      init_Str = null;
      init_ConcurrentHashMap.clear();
   }

   private static String init_Str(String accountId) {
      PacketUtil mgr = PacketUtil.load_PacketUtil();
      if (mgr != null) {
         for (ClassCache.Inner_FieldCache s : mgr.load_List()) {
            if (s.init_Str().equals(accountId)) {
               return s.load_Str();
            }
         }
      }

      return accountId == null ? "bot" : accountId;
   }

   private static void load_V(String message) {
      try {
         bi.init(message, -14249);
      } catch (Throwable var2) {
      }
   }

   private enum Inner_DataUtil {
      init,
      load;
   }
}
