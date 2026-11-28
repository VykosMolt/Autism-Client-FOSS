package autismclient.util.macro;

import autismclient.AutismClientAddon;
import autismclient.mixin.accessor.AutismConnectScreenAccessor;
import autismclient.util.bi;
import autismclient.util.OverlayScreenController;
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
   public static final autismclient.util.JoinMacroController.JoinTiming DEFAULT_WHEN = autismclient.util.JoinMacroController.JoinTiming.PLAYABLE;
   public static final int DEFAULT_TIMEOUT_SEC = 60;
   public static final int MAX_TIMEOUT_SEC = 300;
   static final long init_J = 5000L;
   private static final long load = 30000L;
   private static final long read = 2000L;
   private static final ReentrantLock JOIN_LOCK = new ReentrantLock();
   public String server = "";
   public String when = DEFAULT_WHEN.id();
   public int timeoutSec = 60;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
   }

   public static boolean hasJoinAhead(List<MacroAction> actions, int from, boolean loops) {
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

   public static boolean shouldContinue(boolean playerLeft, boolean stopOnLeave, boolean joinsAgain) {
      return playerLeft ? !stopOnLeave && !joinsAgain : joinsAgain || !stopOnLeave;
   }

   public static boolean sameAddress(String a, String b) {
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

   public String resolveAddress() {
      String typed = this.server == null ? "" : this.server.trim();
      if (!typed.isEmpty()) {
         return typed;
      }

      ServerData last = autismclient.modules.AutoReconnectController.lastServer();
      return last != null && last.ip != null ? last.ip.trim() : "";
   }

   public autismclient.util.JoinMacroController.JoinTiming timing() {
      return autismclient.util.JoinMacroController.JoinTiming.fromConfig(this.when);
   }

   public boolean join(Minecraft mc, Consumer<String> status) throws InterruptedException {
      String address = this.resolveAddress();
      if (address.isEmpty()) {
         return failWith("Join: no server - set one or join a server first");
      }

      if (!ServerAddress.isValidAddress(address)) {
         return failWith("Join: \"" + address + "\" is not a server address");
      }

      if (!allowsMultiplayer(mc)) {
         return failWith("Join: multiplayer is not allowed on this account");
      }

      JOIN_LOCK.lockInterruptibly();

      try {
         long budgetMs = Math.max(1, Math.min(300, this.timeoutSec)) * 1000L;
         return new JoinAction.JoinAttempt(mc, address, this.timing(), budgetMs, status == null ? s -> {} : status).run();
      } finally {
         JOIN_LOCK.unlock();
      }
   }

   public static void awaitDisconnect(Minecraft mc, long timeoutMs) throws InterruptedException {
      long deadline = System.currentTimeMillis() + timeoutMs;

      while (isInWorld(mc) && System.currentTimeMillis() < deadline) {
         awaitTick();
      }
   }

   private static boolean isInWorld(Minecraft mc) {
      return mc.getConnection() != null || mc.level != null || mc.hasSingleplayerServer();
   }

   private static boolean allowsMultiplayer(Minecraft mc) {
      try {
         return mc.allowsMultiplayer();
      } catch (RuntimeException e) {
         return true;
      }
   }

   private static boolean failWith(String message) {
      bi.read(message);
      return false;
   }

   private static void awaitTick() throws InterruptedException {
      if (Thread.currentThread().isInterrupted()) {
         throw new InterruptedException();
      }

      try {
         MacroWaitRegistry.awaitTickSync().get(250L, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
         throw e;
      } catch (Exception var2) {
      }

      if (Thread.currentThread().isInterrupted()) {
         throw new InterruptedException();
      }
   }

   private static void abortConnectScreen(Minecraft mc, ConnectScreen screen) {
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

      if (OverlayScreenController.getActualScreen(mc) == screen) {
         mc.gui.setScreen(access.autism$parent());
      }
   }

   private static String cleanReason(String reason) {
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
      tag.putString("when", this.timing().id());
      tag.putInt("timeoutSec", this.timeoutSec);
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.server = tag.getStringOr("server", "");
      this.when = autismclient.util.JoinMacroController.JoinTiming.fromConfig(tag.getStringOr("when", DEFAULT_WHEN.id())).id();
      this.timeoutSec = Math.max(1, Math.min(300, tag.getIntOr("timeoutSec", 60)));
      if (tag.contains("enabled")) {
         this.enabled = tag.getBooleanOr("enabled", true);
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
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean e) {
      this.enabled = e;
   }

   private static final class JoinAttempt {
      private final Minecraft mc;
      private final String address;
      private final autismclient.util.JoinMacroController.JoinTiming timing;
      private final long budgetMs;
      private final Consumer<String> status;
      private final autismclient.util.JoinMacroController.ReadinessDetector readiness = new autismclient.util.JoinMacroController.ReadinessDetector();
      private JoinAction.ConnectAttempt connectAttempt;
      private int tries;
      private long elapsedMs;
      private long lastTickMs;
      private long lastConnectMs;
      private long nextRetryMs;
      private String lastFailure = "";
      private Screen screen;
      private String line;

      JoinAttempt(Minecraft mc, String address, autismclient.util.JoinMacroController.JoinTiming timing, long budgetMs, Consumer<String> status) {
         this.mc = mc;
         this.address = address;
         this.timing = timing;
         this.budgetMs = budgetMs;
         this.status = status;
      }

      boolean run() throws InterruptedException {
         this.lastTickMs = System.currentTimeMillis();
         if (OverlayScreenController.getActualScreen(this.mc) instanceof DisconnectedScreen earlier) {
            this.screen = earlier;
         }

         try {
            while (true) {
               while (true) {
                  long now = System.currentTimeMillis();
                  long elapsed = Math.max(0L, now - this.lastTickMs);
                  this.lastTickMs = now;
                  if (this.isOnTargetServer()) {
                     this.connectAttempt = null;
                     this.setStatusLine("Waiting: " + this.timing.label());
                     if (this.readiness.isReady(this.mc, this.timing, true)) {
                        return true;
                     }
                     break;
                  }

                  this.readiness.reset();
                  if (this.mc.getConnection() == null && !this.mc.hasSingleplayerServer()) {
                     Boolean done = this.pollConnect(now, elapsed);
                     if (done != null) {
                        return done;
                     }
                     break;
                  }

                  if (!this.leaveCurrentWorld()) {
                     return JoinAction.failWith("Join: could not leave the current world");
                  }

                  this.lastTickMs = System.currentTimeMillis();
               }

               JoinAction.awaitTick();
            }
         } catch (InterruptedException e) {
            if (!this.isOnTargetServer()) {
               this.abortConnect();
            }

            throw e;
         }
      }

      private Boolean pollConnect(long now, long elapsed) {
         Screen screen = OverlayScreenController.getActualScreen(this.mc);
         if (this.connectAttempt == null && screen instanceof ConnectScreen connecting && this.mc.level == null) {
            ServerData pending = autismclient.modules.AutoReconnectController.lastServer();
            if (pending == null || !JoinAction.sameAddress(pending.ip, this.address)) {
               this.elapsedMs += elapsed;
               this.setStatusLine("Waiting for another connection");
               return this.budgetExceeded() ? this.failTimedOut() : null;
            }

            this.connectAttempt = JoinAction.ConnectAttempt.adopt(connecting, now);
            this.lastConnectMs = now;
         }

         if (this.connectAttempt != null) {
            switch (this.connectAttempt.status(this.mc, now)) {
               case ALIVE:
                  if (!this.connectAttempt.connectedPastScreen(this.mc)) {
                     this.elapsedMs += elapsed;
                  }

                  this.setStatusLine(this.tries > 1 ? "Joining " + this.address + " (try " + this.tries + ")" : "Joining " + this.address);
                  break;
               case FAILED:
                  this.elapsedMs += elapsed;
                  this.noteDisconnect(screen, now);
                  this.connectAttempt = null;
                  break;
               case CANCELLED:
                  bi.init("Join cancelled - macro stopped", -14249);
                  return false;
            }
         } else {
            this.elapsedMs += elapsed;
            this.noteDisconnect(screen, now);
            if (now >= this.nextRetryMs && !this.budgetExceeded()) {
               this.startConnect(now);
            } else {
               long waitS = Math.max(1L, (this.nextRetryMs - now + 999L) / 1000L);
               this.setStatusLine(this.lastFailure.isEmpty() ? "Joining " + this.address + " in " + waitS + "s" : "Retry in " + waitS + "s: " + this.lastFailure);
            }
         }

         return this.budgetExceeded() ? this.failTimedOut() : null;
      }

      private boolean budgetExceeded() {
         return this.elapsedMs > this.budgetMs;
      }

      private boolean failTimedOut() {
         this.abortConnect();
         long seconds = this.budgetMs / 1000L;
         return JoinAction.failWith(
            "Join: could not get into " + this.address + " within " + seconds + "s" + (this.lastFailure.isEmpty() ? "" : " - " + this.lastFailure)
         );
      }

      private boolean isOnTargetServer() {
         if (this.mc.getConnection() != null && this.mc.level != null && !this.mc.hasSingleplayerServer()) {
            ServerData current = this.mc.getCurrentServer();
            return current != null && JoinAction.sameAddress(current.ip, this.address);
         } else {
            return false;
         }
      }

      private void noteDisconnect(Screen screen, long now) {
         if (screen instanceof DisconnectedScreen && screen != this.screen) {
            this.screen = screen;
            String reason = screen instanceof autismclient.mixin.duck.DisconnectedScreenDuck access ? access.autism$reason() : "";
            this.lastFailure = JoinAction.cleanReason(reason);
            this.nextRetryMs = Math.max(this.lastConnectMs + 5000L, now + 1000L);
         }
      }

      private void startConnect(long now) {
         this.tries++;
         this.lastConnectMs = now;
         this.nextRetryMs = now + 5000L;
         JoinAction.ConnectAttempt next = new JoinAction.ConnectAttempt(now, true);
         this.connectAttempt = next;
         this.setStatusLine(this.tries > 1 ? "Joining " + this.address + " (try " + this.tries + ")" : "Joining " + this.address);
         ServerAddress target = ServerAddress.parseString(this.address);
         this.mc.execute(() -> {
            try {
               Screen current = OverlayScreenController.getActualScreen(this.mc);
               if (current instanceof ConnectScreen already) {
                  next.ours = false;
                  next.screen = already;
               } else {
                  ConnectScreen.startConnecting(unwrapDisconnected(current), this.mc, target, this.findSavedServer(), false, null);
                  if (OverlayScreenController.getActualScreen(this.mc) instanceof ConnectScreen connecting) {
                     next.screen = connecting;
                  }
               }
            } catch (Throwable t) {
               AutismClientAddon.LOG.warn("[Autism] Join could not start connecting to {}", this.address, t);
            } finally {
               next.attached = true;
            }
         });
      }

      private ServerData findSavedServer() {
         try {
            ServerList list = new ServerList(this.mc);
            list.load();

            for (int i = 0; i < list.size(); i++) {
               ServerData entry = list.get(i);
               if (entry != null && JoinAction.sameAddress(entry.ip, this.address)) {
                  return entry;
               }
            }
         } catch (Throwable var4) {
         }

         ServerData last = autismclient.modules.AutoReconnectController.lastServer();
         return last != null && JoinAction.sameAddress(last.ip, this.address) ? last : new ServerData(this.address, this.address, Type.OTHER);
      }

      private static Screen unwrapDisconnected(Screen current) {
         if (current instanceof DisconnectedScreen && current instanceof autismclient.mixin.duck.DisconnectedScreenDuck access && access.autism$parent() != null) {
            return access.autism$parent();
         } else {
            return current instanceof JoinMultiplayerScreen ? current : new JoinMultiplayerScreen(new TitleScreen());
         }
      }

      private boolean leaveCurrentWorld() throws InterruptedException {
         ServerData current = this.mc.getCurrentServer();
         this.setStatusLine(this.mc.hasSingleplayerServer() ? "Leaving the world" : "Leaving " + (current == null ? "the server" : current.ip));
         this.mc.execute(() -> MacroExecutor.runWithSuppressedLeaveMark(() -> {
            if (JoinAction.isInWorld(this.mc)) {
               this.mc.disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
            }
         }));
         long deadline = System.currentTimeMillis() + 30000L;

         while (JoinAction.isInWorld(this.mc)) {
            if (System.currentTimeMillis() > deadline) {
               return false;
            }

            JoinAction.awaitTick();
         }

         return true;
      }

      private void abortConnect() {
         JoinAction.ConnectAttempt current = this.connectAttempt;
         if (current != null && current.ours) {
            current.abortRequested = true;
            ConnectScreen screen = current.screen;
            if (screen != null) {
               this.mc.execute(() -> JoinAction.abortConnectScreen(this.mc, screen));
            }
         }
      }

      private void setStatusLine(String line) {
         if (!line.equals(this.line)) {
            this.line = line;
            this.status.accept(line);
         }
      }
   }

   private static final class ConnectAttempt {
      volatile boolean ours;
      volatile ConnectScreen screen;
      volatile boolean attached;
      volatile boolean abortRequested;
      private final long startedAt;
      private long detachedSince = -1L;

      ConnectAttempt(long startedAt, boolean ours) {
         this.startedAt = startedAt;
         this.ours = ours;
      }

      static JoinAction.ConnectAttempt adopt(ConnectScreen screen, long now) {
         JoinAction.ConnectAttempt attempt = new JoinAction.ConnectAttempt(now, false);
         attempt.screen = screen;
         attempt.attached = true;
         return attempt;
      }

      JoinAction.AttemptStatus status(Minecraft mc, long now) {
         if (!this.attached) {
            return now - this.startedAt > 10000L ? JoinAction.AttemptStatus.FAILED : JoinAction.AttemptStatus.ALIVE;
         }

         ConnectScreen connecting = this.screen;
         if (connecting == null) {
            return JoinAction.AttemptStatus.FAILED;
         }

         AutismConnectScreenAccessor access = (AutismConnectScreenAccessor)connecting;
         if (access.autism$aborted()) {
            return this.abortRequested ? JoinAction.AttemptStatus.FAILED : JoinAction.AttemptStatus.CANCELLED;
         }

         Screen current = OverlayScreenController.getActualScreen(mc);
         if (current == connecting || hasLiveConnection(access)) {
            this.detachedSince = -1L;
            return JoinAction.AttemptStatus.ALIVE;
         }

         if (current instanceof DisconnectedScreen) {
            return JoinAction.AttemptStatus.FAILED;
         }

         if (this.detachedSince < 0L) {
            this.detachedSince = now;
         }

         return now - this.detachedSince > 2000L ? JoinAction.AttemptStatus.FAILED : JoinAction.AttemptStatus.ALIVE;
      }

      boolean connectedPastScreen(Minecraft mc) {
         ConnectScreen connecting = this.screen;
         if (connecting != null && mc.level == null) {
            Screen current = OverlayScreenController.getActualScreen(mc);
            return current != connecting && !(current instanceof DisconnectedScreen) && hasLiveConnection((AutismConnectScreenAccessor)connecting);
         } else {
            return false;
         }
      }

      private static boolean hasLiveConnection(AutismConnectScreenAccessor access) {
         Connection connection = access.autism$connection();
         return connection != null && connection.isConnected();
      }
   }

   enum AttemptStatus {
      ALIVE,
      FAILED,
      CANCELLED;
   }
}
