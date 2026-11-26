package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.mixin.accessor.AutismConnectScreenAccessor;
import autismclient.util.bi;
import autismclient.util.cx;
import io.netty.channel.ChannelFuture;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerData.Type;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;

public class JoinAction implements MacroAction {
   public static final autismclient.util.aA.Inner_StreamUtil DEFAULT_WHEN = autismclient.util.aA.Inner_StreamUtil.PLAYABLE;
   public static final int DEFAULT_TIMEOUT_SEC = 60;
   public static final int MAX_TIMEOUT_SEC = 300;
   static final long init_J = 5000L;
   private static final long load = 30000L;
   private static final long read = 2000L;
   private static final ReentrantLock init_ReentrantLock = new ReentrantLock();
   public String server = "";
   public String when = DEFAULT_WHEN.init_Str();
   public int timeoutSec = 60;
   private boolean init_Z = true;

   @Override
   public void execute(Minecraft mc) {
   }

   public static boolean init(List<MacroAction> actions, int from, boolean loops) {
      if (actions == null) {
         return false;
      }

      for (int i = loops ? 0 : Math.max(0, from); i < actions.size(); i++) {
         MacroAction action = actions.get(i);
         if (action instanceof JoinAction && action.isEnabled()) {
            return true;
         }
      }

      return false;
   }

   public static boolean init(boolean playerLeft, boolean stopOnLeave, boolean joinsAgain) {
      return playerLeft ? !stopOnLeave && !joinsAgain : joinsAgain || !stopOnLeave;
   }

   public static boolean init_Z_Str_Str(String a, String b) {
      if (a != null && b != null && !a.isBlank() && !b.isBlank()) {
         ServerAddress first = ServerAddress.parseString(a.trim());
         ServerAddress second = ServerAddress.parseString(b.trim());
         return !first.getHost().isEmpty() && !second.getHost().isEmpty()
            ? first.getPort() == second.getPort() && first.getHost().toLowerCase(Locale.ROOT).equals(second.getHost().toLowerCase(Locale.ROOT))
            : false;
      } else {
         return false;
      }
   }

   public String init_Str() {
      String typed = this.server == null ? "" : this.server.trim();
      if (!typed.isEmpty()) {
         return typed;
      }

      ServerData last = autismclient.modules.aB.init_ServerData();
      return last != null && last.ip != null ? last.ip.trim() : "";
   }

   public autismclient.util.aA.Inner_StreamUtil init_aA_StreamUtil() {
      return autismclient.util.aA.Inner_StreamUtil.init(this.when);
   }

   public boolean init_Z_Minecraft_Consumer(Minecraft mc, Consumer<String> status) throws InterruptedException {
      String address = this.init_Str();
      if (address.isEmpty()) {
         return init_Z("Join: no server - set one or join a server first");
      }

      if (!ServerAddress.isValidAddress(address)) {
         return init_Z("Join: \"" + address + "\" is not a server address");
      }

      if (!load(mc)) {
         return init_Z("Join: multiplayer is not allowed on this account");
      }

      init_ReentrantLock.lockInterruptibly();

      try {
         long budgetMs = Math.max(1, Math.min(300, this.timeoutSec)) * 1000L;
         return new JoinAction.Inner_CachePool(mc, address, this.init_aA_StreamUtil(), budgetMs, status == null ? s -> {} : status).init_Z();
      } finally {
         init_ReentrantLock.unlock();
      }
   }

   public static void init_V_Minecraft_J(Minecraft mc, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;

      while (init(mc) && System.currentTimeMillis() < deadline) {
         init_V_2();
      }
   }

   private static boolean init(Minecraft mc) {
      return mc.getConnection() != null || mc.level != null || mc.hasSingleplayerServer();
   }

   private static boolean load(Minecraft mc) {
      try {
         return mc.allowsMultiplayer();
      } catch (RuntimeException e) {
         return true;
      }
   }

   private static boolean init_Z(String message) {
      bi.read(message);
      return false;
   }

   private static void init_V_2() throws InterruptedException {
      if (Thread.currentThread().isInterrupted()) {
         throw new InterruptedException();
      }

      try {
         MacroWaitRegistry.load_CompletableFuture().get(250L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         throw e;
      } catch (Exception var2) {
      }

      if (Thread.currentThread().isInterrupted()) {
         throw new InterruptedException();
      }
   }

   private static void init_V_Minecraft_ConnectScreen(Minecraft mc, ConnectScreen screen) {
      AutismConnectScreenAccessor access = (AutismConnectScreenAccessor)screen;
      synchronized (screen) {
         access.autism$setAborted(true);
         ChannelFuture future = access.autism$channelFuture();
         if (future != null) {
            future.cancel(true);
            access.autism$setChannelFuture(null);
         }

         Connection connection = access.autism$connection();
         if (connection != null) {
            connection.disconnect(ConnectScreen.ABORT_CONNECTION);
         }
      }

      if (cx.init_Screen(mc) == screen) {
         mc.gui.setScreen(access.autism$parent());
      }
   }

   private static String init_Str(String reason) {
      if (reason == null) {
         return "";
      }

      String line = reason.replace('\n', ' ').replaceAll("\\s+", " ").trim();
      return line.length() > 60 ? line.substring(0, 57) + "..." : line;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", this.getType().name());
      tag.putString("server", this.server == null ? "" : this.server);
      tag.putString("when", this.init_aA_StreamUtil().init_Str());
      tag.putInt("timeoutSec", this.timeoutSec);
      tag.putBoolean("enabled", this.init_Z);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.server = tag.getStringOr("server", "");
      this.when = autismclient.util.aA.Inner_StreamUtil.init(tag.getStringOr("when", DEFAULT_WHEN.init_Str())).init_Str();
      this.timeoutSec = Math.max(1, Math.min(300, tag.getIntOr("timeoutSec", 60)));
      if (tag.contains("enabled")) {
         this.init_Z = tag.getBooleanOr("enabled", true);
      }
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.JOIN;
   }

   @Override
   public String getDisplayName() {
      String typed = this.server == null ? "" : this.server.trim();
      return typed.isEmpty() ? "Join Last Server" : "Join " + typed;
   }

   @Override
   public String getIcon() {
      return "JN";
   }

   @Override
   public boolean isEnabled() {
      return this.init_Z;
   }

   @Override
   public void setEnabled(boolean e) {
      this.init_Z = e;
   }

   private static final class Inner_CachePool {
      private final Minecraft init_Minecraft;
      private final String init_Str;
      private final autismclient.util.aA.Inner_StreamUtil init_aA_StreamUtil;
      private final long init_J;
      private final Consumer<String> init_Consumer;
      private final autismclient.util.aA.Inner_DataUtil init_aA_DataUtil = new autismclient.util.aA.Inner_DataUtil();
      private JoinAction.Inner_DataUtil init_ClassCache_DataUti;
      private int init_I;
      private long load_J;
      private long read_J;
      private long write;
      private long update;
      private String load_Str = "";
      private Screen init_Screen;
      private String read_Str;

      Inner_CachePool(Minecraft mc, String address, autismclient.util.aA.Inner_StreamUtil timing, long budgetMs, Consumer<String> status) {
         this.init_Minecraft = mc;
         this.init_Str = address;
         this.init_aA_StreamUtil = timing;
         this.init_J = budgetMs;
         this.init_Consumer = status;
      }

      boolean init_Z() throws InterruptedException {
         this.read_J = System.currentTimeMillis();
         if (cx.init_Screen(this.init_Minecraft) instanceof DisconnectedScreen earlier) {
            this.init_Screen = earlier;
         }

         try {
            while (true) {
               while (true) {
                  long now = System.currentTimeMillis();
                  long elapsed = Math.max(0L, now - this.read_J);
                  this.read_J = now;
                  if (this.write()) {
                     this.init_ClassCache_DataUti = null;
                     this.init_V_Str("Waiting: " + this.init_aA_StreamUtil.load());
                     if (this.init_aA_DataUtil.init(this.init_Minecraft, this.init_aA_StreamUtil, true)) {
                        return true;
                     }
                     break;
                  }

                  this.init_aA_DataUtil.init();
                  if (this.init_Minecraft.getConnection() == null && !this.init_Minecraft.hasSingleplayerServer()) {
                     Boolean done = this.init(now, elapsed);
                     if (done != null) {
                        return done;
                     }
                     break;
                  }

                  if (!this.update()) {
                     return JoinAction.init_Z("Join: could not leave the current world");
                  }

                  this.read_J = System.currentTimeMillis();
               }

               JoinAction.init_V_2();
            }
         } catch (InterruptedException e) {
            if (!this.write()) {
               this.init_V();
            }

            throw e;
         }
      }

      private Boolean init(long now, long elapsed) {
         Screen screen = cx.init_Screen(this.init_Minecraft);
         if (this.init_ClassCache_DataUti == null && screen instanceof ConnectScreen connecting && this.init_Minecraft.level == null) {
            ServerData pending = autismclient.modules.aB.init_ServerData();
            if (pending == null || !JoinAction.init_Z_Str_Str(pending.ip, this.init_Str)) {
               this.load_J += elapsed;
               this.init_V_Str("Waiting for another connection");
               return this.load() ? this.read() : null;
            }

            this.init_ClassCache_DataUti = JoinAction.Inner_DataUtil.init_ClassCache_DataUti_ConnectScreen_J(connecting, now);
            this.write = now;
         }

         if (this.init_ClassCache_DataUti != null) {
            switch (this.init_ClassCache_DataUti.init_ClassCache_StreamU_Minecraft_J(this.init_Minecraft, now)) {
               case ALIVE:
                  if (!this.init_ClassCache_DataUti.init_Z_Minecraft(this.init_Minecraft)) {
                     this.load_J += elapsed;
                  }

                  this.init_V_Str(this.init_I > 1 ? "Joining " + this.init_Str + " (try " + this.init_I + ")" : "Joining " + this.init_Str);
                  break;
               case FAILED:
                  this.load_J += elapsed;
                  this.init(screen, now);
                  this.init_ClassCache_DataUti = null;
                  break;
               case CANCELLED:
                  bi.init("Join cancelled - macro stopped", -14249);
                  return false;
            }
         } else {
            this.load_J += elapsed;
            this.init(screen, now);
            if (now >= this.update && !this.load()) {
               this.init_V_J(now);
            } else {
               long waitS = Math.max(1L, (this.update - now + 999L) / 1000L);
               this.init_V_Str(this.load_Str.isEmpty() ? "Joining " + this.init_Str + " in " + waitS + "s" : "Retry in " + waitS + "s: " + this.load_Str);
            }
         }

         return this.load() ? this.read() : null;
      }

      private boolean load() {
         return this.load_J > this.init_J;
      }

      private boolean read() {
         this.init_V();
         long seconds = this.init_J / 1000L;
         return JoinAction.init_Z(
            "Join: could not get into " + this.init_Str + " within " + seconds + "s" + (this.load_Str.isEmpty() ? "" : " - " + this.load_Str)
         );
      }

      private boolean write() {
         if (this.init_Minecraft.getConnection() != null && this.init_Minecraft.level != null && !this.init_Minecraft.hasSingleplayerServer()) {
            ServerData current = this.init_Minecraft.getCurrentServer();
            return current != null && JoinAction.init_Z_Str_Str(current.ip, this.init_Str);
         } else {
            return false;
         }
      }

      private void init(Screen screen, long now) {
         if (screen instanceof DisconnectedScreen && screen != this.init_Screen) {
            this.init_Screen = screen;
            String reason = screen instanceof autismclient.c.DisconnectedScreenDuck access ? access.autism$reason() : "";
            this.load_Str = JoinAction.init_Str(reason);
            this.update = Math.max(this.write + 5000L, now + 1000L);
         }
      }

      private void init_V_J(long now) {
         this.init_I++;
         this.write = now;
         this.update = now + 5000L;
         JoinAction.Inner_DataUtil next = new JoinAction.Inner_DataUtil(now, true);
         this.init_ClassCache_DataUti = next;
         this.init_V_Str(this.init_I > 1 ? "Joining " + this.init_Str + " (try " + this.init_I + ")" : "Joining " + this.init_Str);
         ServerAddress target = ServerAddress.parseString(this.init_Str);
         this.init_Minecraft.execute(() -> {
            try {
               Screen current = cx.init_Screen(this.init_Minecraft);
               if (current instanceof ConnectScreen already) {
                  next.init_Z = false;
                  next.init_ConnectScreen = already;
               } else {
                  ConnectScreen.startConnecting(init_Screen_Screen(current), this.init_Minecraft, target, this.init_ServerData(), false, null);
                  if (cx.init_Screen(this.init_Minecraft) instanceof ConnectScreen connecting) {
                     next.init_ConnectScreen = connecting;
                  }
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Join could not start connecting to {}", this.init_Str, t);
            } finally {
               next.load_Z = true;
            }
         });
      }

      private ServerData init_ServerData() {
         try {
            ServerList list = new ServerList(this.init_Minecraft);
            list.load();

            for (int i = 0; i < list.size(); i++) {
               ServerData entry = list.get(i);
               if (entry != null && JoinAction.init_Z_Str_Str(entry.ip, this.init_Str)) {
                  return entry;
               }
            }
         } catch (Throwable var4) {
         }

         ServerData last = autismclient.modules.aB.init_ServerData();
         return last != null && JoinAction.init_Z_Str_Str(last.ip, this.init_Str) ? last : new ServerData(this.init_Str, this.init_Str, Type.OTHER);
      }

      private static Screen init_Screen_Screen(Screen current) {
         if (current instanceof DisconnectedScreen && current instanceof autismclient.c.DisconnectedScreenDuck access && access.autism$parent() != null) {
            return access.autism$parent();
         } else {
            return current instanceof JoinMultiplayerScreen ? current : new JoinMultiplayerScreen(new TitleScreen());
         }
      }

      private boolean update() throws InterruptedException {
         ServerData current = this.init_Minecraft.getCurrentServer();
         this.init_V_Str(this.init_Minecraft.hasSingleplayerServer() ? "Leaving the world" : "Leaving " + (current == null ? "the server" : current.ip));
         this.init_Minecraft.execute(() -> MacroExecutor.init_V_Runnable(() -> {
            if (JoinAction.init(this.init_Minecraft)) {
               this.init_Minecraft.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
            }
         }));
         long deadline = System.currentTimeMillis() + 30000L;

         while (JoinAction.init(this.init_Minecraft)) {
            if (System.currentTimeMillis() > deadline) {
               return false;
            }

            JoinAction.init_V_2();
         }

         return true;
      }

      private void init_V() {
         JoinAction.Inner_DataUtil current = this.init_ClassCache_DataUti;
         if (current != null && current.init_Z) {
            current.read = true;
            ConnectScreen screen = current.init_ConnectScreen;
            if (screen != null) {
               this.init_Minecraft.execute(() -> JoinAction.init_V_Minecraft_ConnectScreen(this.init_Minecraft, screen));
            }
         }
      }

      private void init_V_Str(String line) {
         if (!line.equals(this.read_Str)) {
            this.read_Str = line;
            this.init_Consumer.accept(line);
         }
      }
   }

   private static final class Inner_DataUtil {
      volatile boolean init_Z;
      volatile ConnectScreen init_ConnectScreen;
      volatile boolean load_Z;
      volatile boolean read;
      private final long init_J;
      private long load_J = -1L;

      Inner_DataUtil(long startedAt, boolean ours) {
         this.init_J = startedAt;
         this.init_Z = ours;
      }

      static JoinAction.Inner_DataUtil init_ClassCache_DataUti_ConnectScreen_J(ConnectScreen screen, long now) {
         JoinAction.Inner_DataUtil attempt = new JoinAction.Inner_DataUtil(now, false);
         attempt.init_ConnectScreen = screen;
         attempt.load_Z = true;
         return attempt;
      }

      JoinAction.Inner_StreamUtil init_ClassCache_StreamU_Minecraft_J(Minecraft mc, long now) {
         if (!this.load_Z) {
            return now - this.init_J > 10000L ? JoinAction.Inner_StreamUtil.FAILED : JoinAction.Inner_StreamUtil.ALIVE;
         }

         ConnectScreen connecting = this.init_ConnectScreen;
         if (connecting == null) {
            return JoinAction.Inner_StreamUtil.FAILED;
         }

         AutismConnectScreenAccessor access = (AutismConnectScreenAccessor)connecting;
         if (access.autism$aborted()) {
            return this.read ? JoinAction.Inner_StreamUtil.FAILED : JoinAction.Inner_StreamUtil.CANCELLED;
         }

         Screen current = cx.init_Screen(mc);
         if (current == connecting || init_Z_AutismConnectScree(access)) {
            this.load_J = -1L;
            return JoinAction.Inner_StreamUtil.ALIVE;
         }

         if (current instanceof DisconnectedScreen) {
            return JoinAction.Inner_StreamUtil.FAILED;
         }

         if (this.load_J < 0L) {
            this.load_J = now;
         }

         return now - this.load_J > 2000L ? JoinAction.Inner_StreamUtil.FAILED : JoinAction.Inner_StreamUtil.ALIVE;
      }

      boolean init_Z_Minecraft(Minecraft mc) {
         ConnectScreen connecting = this.init_ConnectScreen;
         if (connecting != null && mc.level == null) {
            Screen current = cx.init_Screen(mc);
            return current != connecting && !(current instanceof DisconnectedScreen) && init_Z_AutismConnectScree((AutismConnectScreenAccessor)connecting);
         } else {
            return false;
         }
      }

      private static boolean init_Z_AutismConnectScree(AutismConnectScreenAccessor access) {
         Connection connection = access.autism$connection();
         return connection != null && connection.isConnected();
      }
   }

   enum Inner_StreamUtil {
      ALIVE,
      FAILED,
      CANCELLED;
   }
}
