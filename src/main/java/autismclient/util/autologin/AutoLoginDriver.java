package autismclient.util.autologin;

import autismclient.api.custommenu.CustomMenuButton;
import autismclient.api.custommenu.CustomMenuInput;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmission;
import autismclient.util.macro.CustomMenuActionPlanner;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AutoLoginDriver {
   private final AutoLoginBackend host;
   private volatile AuthPromptParser.AuthPrompt detection;
   private volatile long pendingStartAt;
   private volatile AutoLoginTimings newConfig;
   private volatile AutoLoginDriver.Inner_DataUtil state;
   private long startedAt;
   private long read_J;
   private long nextCommandAt;
   private long nextMenuTryAt;
   private long loginSettleAt;
   private long followUpLoginAt;
   private boolean loginAfterRegister;
   private int attempts;
   private boolean failureReported;
   private boolean missingPasswordReported;
   private long submittedMenuGeneration;
   private String registerCommand;
   private String loginCommand;

   public AutoLoginDriver(AutoLoginBackend host) {
      this.detection = AuthPromptParser.AuthPrompt.NONE;
      this.newConfig = AutoLoginTimings.defaults();
      this.state = AutoLoginDriver.Inner_DataUtil.IDLE;
      this.submittedMenuGeneration = -9223372036854775808L;
      this.registerCommand = "";
      this.loginCommand = "";
      this.host = host;
   }

   public void setTimings(AutoLoginTimings newConfig) {
      if (newConfig != null) {
         this.newConfig = newConfig;
      }
   }

   public void reset(long now) {
      this.detection = AuthPromptParser.AuthPrompt.NONE;
      this.pendingStartAt = now == 0L ? 1L : now;
   }

   public void onChat(String line) {
      AuthPromptParser.AuthPrompt detection = AuthPromptParser.parse(line);
      if (detection.kind() != AuthPromptParser.PromptKind.NONE) {
         this.detection = detection;
      }
   }

   public AutoLoginDriver.Inner_DataUtil init_CachePool_DataUtil() {
      return this.state;
   }

   public boolean isFinished() {
      AutoLoginDriver.Inner_DataUtil current = this.state;
      return current == AutoLoginDriver.Inner_DataUtil.DONE || current == AutoLoginDriver.Inner_DataUtil.GAVE_UP;
   }

   public int init_I() {
      return this.attempts;
   }

   public void tick(long now) {
      this.applyPendingStart();
      if (this.state != AutoLoginDriver.Inner_DataUtil.IDLE && !this.isFinished()) {
         if (now - this.startedAt >= this.newConfig.giveUpMs()) {
            this.state = AutoLoginDriver.Inner_DataUtil.GAVE_UP;
         } else if (!this.handleMenu(now)) {
            this.handleChatPrompt(now);
         }
      }
   }

   private void applyPendingStart() {
      long requested = this.pendingStartAt;
      if (requested != 0L) {
         this.pendingStartAt = 0L;
         this.startedAt = requested;
         this.read_J = 0L;
         this.nextCommandAt = 0L;
         this.nextMenuTryAt = 0L;
         this.loginSettleAt = 0L;
         this.followUpLoginAt = 0L;
         this.loginAfterRegister = false;
         this.attempts = 0;
         this.failureReported = false;
         this.missingPasswordReported = false;
         this.submittedMenuGeneration = -9223372036854775808L;
         this.registerCommand = "";
         this.loginCommand = "";
         this.state = AutoLoginDriver.Inner_DataUtil.WATCHING;
      }
   }

   private boolean handleMenu(long now) {
      CustomMenuSnapshot snapshot = this.host.currentMenu();
      if (snapshot == null) {
         this.submittedMenuGeneration = -9223372036854775808L;
         return false;
      }

      if (this.host.isBusy()) {
         return true;
      }

      if (snapshot.generation() == this.submittedMenuGeneration) {
         return true;
      }

      if (now < this.nextMenuTryAt) {
         return true;
      }

      boolean hasText = false;

      for (CustomMenuInput input : snapshot.inputs()) {
         if (input.kind() == CustomMenuInput.Kind.TEXT) {
            hasText = true;
            break;
         }
      }

      String password = this.host.password();
      if (hasText && password.isEmpty()) {
         this.reportMissingPassword(snapshot.title());
         return true;
      }

      CustomMenuButton button;
      Map<String, String> values;
      try {
         CustomMenuButton accept = hasText ? null : CustomMenuActionPlanner.acceptButton(snapshot.buttons());
         button = accept != null ? accept : CustomMenuActionPlanner.fallbackButton(snapshot.buttons());
         if (button == null) {
            this.nextMenuTryAt = now + this.newConfig.write();
            return true;
         }

         values = new LinkedHashMap<>();

         for (CustomMenuInput input : snapshot.inputs()) {
            values.put(input.key(), input.kind() == CustomMenuInput.Kind.TEXT ? password : input.initialValue());
         }
      } catch (RuntimeException error) {
         this.nextMenuTryAt = now + this.newConfig.write();
         return true;
      }

      if (!this.host.submit(snapshot, new CustomMenuSubmission(values, button))) {
         this.nextMenuTryAt = now + this.newConfig.write();
         return true;
      }

      this.submittedMenuGeneration = snapshot.generation();
      if (hasText) {
         this.state = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
         this.loginSettleAt = now + this.newConfig.read();
      }

      return true;
   }

   private void handleChatPrompt(long now) {
      if (!this.host.isConnected()) {
         this.read_J = 0L;
      } else {
         if (this.read_J == 0L) {
            this.read_J = now;
         }

         if (now - this.read_J >= this.newConfig.promptDelayMs()) {
            AuthPromptParser.AuthPrompt detection = this.detection;
            if (detection.kind() != AuthPromptParser.PromptKind.NONE) {
               if (now >= this.nextCommandAt) {
                  if (this.attempts >= this.newConfig.maxAttempts()) {
                     this.giveUp();
                  } else {
                     String password = this.host.password();
                     if (password.isEmpty()) {
                        this.reportMissingPassword("AuthMe login");
                     } else if (this.host.hasConnection()) {
                        this.detection = AuthPromptParser.AuthPrompt.NONE;
                        if (!this.host.sendCommand(detection.loginCommand(password))) {
                           this.detection = detection;
                           this.nextCommandAt = now + 250L;
                        } else {
                           this.attempts++;
                           this.nextCommandAt = now + this.newConfig.read();
                           if (detection.kind() == AuthPromptParser.PromptKind.REGISTER) {
                              this.registerCommand = detection.command();
                              this.state = AutoLoginDriver.Inner_DataUtil.REGISTERING;
                              this.loginAfterRegister = true;
                              this.followUpLoginAt = now + this.newConfig.update();
                           } else {
                              this.loginCommand = detection.command();
                              this.state = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
                              this.loginAfterRegister = false;
                              this.loginSettleAt = now + this.newConfig.read();
                           }
                        }
                     }
                  }
               }
            } else if (this.loginAfterRegister && now >= this.followUpLoginAt) {
               this.sendLoginAfterRegister(now);
            } else if (this.state == AutoLoginDriver.Inner_DataUtil.LOGGING_IN && this.loginSettleAt != 0L && now >= this.loginSettleAt) {
               this.state = AutoLoginDriver.Inner_DataUtil.DONE;
            }
         }
      }
   }

   private void sendLoginAfterRegister(long now) {
      this.loginAfterRegister = false;
      if (this.attempts >= this.newConfig.maxAttempts()) {
         this.giveUp();
      } else {
         String password = this.host.password();
         if (!password.isEmpty() && this.host.hasConnection()) {
            String token = AuthPromptParser.init(this.registerCommand, this.loginCommand);
            if (!this.host.sendCommand(token + " " + password)) {
               this.loginAfterRegister = true;
               this.followUpLoginAt = now + 250L;
            } else {
               this.attempts++;
               this.nextCommandAt = now + this.newConfig.read();
               this.state = AutoLoginDriver.Inner_DataUtil.LOGGING_IN;
               this.loginSettleAt = now + this.newConfig.read();
            }
         }
      }
   }

   private void giveUp() {
      if (!this.failureReported) {
         this.failureReported = true;
         this.host.report("Auto login failed after " + this.newConfig.maxAttempts() + " attempts - check the password.");
      }

      this.state = AutoLoginDriver.Inner_DataUtil.GAVE_UP;
   }

   private void reportMissingPassword(String context) {
      if (!this.missingPasswordReported) {
         this.missingPasswordReported = true;
         this.host.onMissingPassword(context == null ? "" : context);
      }
   }

   public enum Inner_DataUtil {
      IDLE,
      WATCHING,
      REGISTERING,
      LOGGING_IN,
      DONE,
      GAVE_UP;
   }
}
