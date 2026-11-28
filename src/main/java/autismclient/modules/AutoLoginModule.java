package autismclient.modules;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.api.module.DoubleSetting;
import autismclient.api.module.StringSetting;
import autismclient.util.AutismClientMessaging;
import java.util.Optional;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public final class AutoLoginModule extends Module {
   private static volatile AutoLoginModule init_CharEncoder;
   private final autismclient.util.autologin.AutoLoginDriver init_CachePool = new autismclient.util.autologin.AutoLoginDriver(new AutoLoginModule.ModuleBackend());

   AutoLoginModule() {
      super("auto-login", "AutoLogin", ae.read, "Answers server login and register gates.");
      init_CharEncoder = this;
      this.add(new StringSetting("password", "Password", "").description("Sent to login and register gates.").build());
      this.add(new DoubleSetting("chat-delay", "Chat Delay", 2.0, 0.0, 10.0, 0.1).description("Wait after spawning before sending.").unit("s").build());
      this.add(new DoubleSetting("window", "Window", 10.0, 3.0, 60.0, 0.5).description("Stop watching this long after joining.").unit("s").build());
   }

   @Override
   public boolean ticksWhenDisabled() {
      return false;
   }

   @Override
   public boolean settingsShareable() {
      return false;
   }

   @Override
   public void onGameJoin() {
      this.init_CachePool.reset(System.currentTimeMillis());
   }

   @Override
   public void onGameLeft() {
      this.init_CachePool.reset(System.currentTimeMillis());
   }

   @Override
   public void tick() {
      this.init_CachePool
         .init(new autismclient.util.autologin.AutoLoginTimings((long)(this.decimal("window") * 1000.0), (long)(this.decimal("chat-delay") * 1000.0), 2500L, 4, 1000L, 3000L));
      this.init_CachePool.load(System.currentTimeMillis());
   }

   public static void init(Packet<?> packet) {
      AutoLoginModule module = init_CharEncoder;
      if (module != null && module.isEnabled()) {
         try {
            if (packet instanceof ClientboundSystemChatPacket system) {
               module.init_CachePool.init(system.content().getString());
            } else if (packet instanceof ClientboundDisguisedChatPacket disguised) {
               module.init_CachePool.init(disguised.message().getString());
            }
         } catch (Throwable var4) {
         }
      }
   }

   private final class ModuleBackend implements autismclient.util.autologin.AutoLoginBackend {
      @Override
      public String password() {
         String configured = AutoLoginModule.this.text("password");
         if (configured != null && !configured.isBlank()) {
            return configured;
         }

         try {
            String shared = autismclient.util.JoinMacroController.formValues().get("password");
            if (shared != null && !shared.isBlank()) {
               return shared;
            }
         } catch (RuntimeException var3) {
         }

         return "";
      }

      @Override
      public boolean isConnected() {
         return Module.MC != null && Module.MC.getConnection() != null && Module.MC.player != null && Module.MC.level != null;
      }

      @Override
      public boolean load() {
         return Module.MC != null && Module.MC.getConnection() != null;
      }

      @Override
      public CustomMenuSnapshot currentMenu() {
         return autismclient.util.custommenu.CustomMenuTracker.snapshot();
      }

      @Override
      public boolean init(CustomMenuSnapshot snapshot, CustomMenuSubmission submission) {
         CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, submission);
         if (result != null && result.success()) {
            for (Packet<?> packet : result.packets()) {
               if (!autismclient.util.JoinMacroController.init(packet)) {
                  return false;
               }
            }

            autismclient.util.custommenu.CustomMenuTracker.init(snapshot, result.replacement());
            if (result.clientAction() != null && Module.MC != null) {
               Module.MC.execute(() -> {
                  if (Module.MC.gui.screen() instanceof DialogScreen<?> dialog) {
                     dialog.runAction(Optional.of(result.clientAction()));
                  }
               });
            }

            return true;
         } else {
            return false;
         }
      }

      @Override
      public boolean sendCommand(String line) {
         if (!this.load()) {
            return false;
         }

         AutoLoginModule.this.sendCommand(line);
         return true;
      }

      @Override
      public boolean read() {
         return autismclient.util.JoinMacroController.isRunning();
      }

      @Override
      public void report(String message) {
         AutismClientMessaging.sendPrefixed("§c" + message);
      }

      @Override
      public void load(String context) {
         AutismClientMessaging.sendPrefixed("§eAutoLogin: set a password in the module settings.");
      }
   }
}
