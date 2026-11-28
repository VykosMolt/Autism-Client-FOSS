package autismclient.util.macro;

import autismclient.api.custommenu.CustomMenuAdapterRegistry;
import autismclient.api.custommenu.CustomMenuSnapshot;
import autismclient.api.custommenu.CustomMenuSubmitResult;
import autismclient.util.bi;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.locks.LockSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;

public final class CustomMenuAction implements MacroAction {
   private static final long init_J = 15000L;
   private static String lastError = "";
   private static long lastErrorAt;
   public final ArrayList<String> fieldValues = new ArrayList<>();
   public String clickButton = "";
   public int timeoutMs = 30000;
   private boolean enabled = true;

   @Override
   public void execute(Minecraft mc) {
      long deadline = System.currentTimeMillis() + this.timeout();
      boolean gateOnRun = MacroExecutor.isCurrentRunActive();
      String lastError = "";

      while (System.currentTimeMillis() <= deadline && !Thread.currentThread().isInterrupted()) {
         if (gateOnRun && !MacroExecutor.isCurrentRunActive()) {
            return;
         }

         long seenGeneration = autismclient.util.custommenu.CustomMenuTracker.generation();
         CustomMenuSnapshot snapshot = autismclient.util.custommenu.CustomMenuTracker.snapshot();
         if (snapshot != null) {
            seenGeneration = snapshot.generation();
         } else {
            snapshot = autismclient.util.custommenu.DialogScreenBridge.snapshot(mc);
         }

         if (snapshot == null) {
            LockSupport.parkNanos(20000000L);
         } else {
            CustomMenuActionPlanner.PreparedSubmission prepared = CustomMenuActionPlanner.prepare(this, snapshot, value -> {
               String withSecrets = autismclient.util.JoinMacroController.fillSecrets(value);
               if (withSecrets == null) {
                  throw new IllegalStateException("Missing form value");
               } else {
                  MacroTemplateResolver.ResolveResult resolved = MacroVariableContext.resolveTemplate(withSecrets, mc);
                  if (!resolved.success()) {
                     throw new IllegalStateException("Missing macro value");
                  } else {
                     return resolved.value();
                  }
               }
            });
            if (!prepared.isReady()) {
               lastError = prepared.error() == null ? "" : prepared.error();
               if (prepared.error() != null && prepared.error().contains("unavailable")) {
                  LockSupport.parkNanos(20000000L);
                  continue;
               }

               this.reportFailure(prepared.error());
               return;
            }

            Screen answered = autismclient.util.custommenu.DialogScreenBridge.currentScreen(mc);
            CustomMenuSubmitResult result = CustomMenuAdapterRegistry.submit(snapshot, prepared.submission());
            if (!result.success()) {
               this.reportFailure(result.error());
               return;
            }

            for (Packet<?> packet : result.packets()) {
               if (!autismclient.util.JoinMacroController.init(packet)) {
                  this.reportFailure("Connection closed before custom-menu submission");
                  return;
               }
            }

            autismclient.util.custommenu.CustomMenuTracker.replace(seenGeneration, result.replacement(), snapshot.phase());
            autismclient.util.custommenu.DialogScreenBridge.answer(mc, result.clientAction(), answered);
            return;
         }
      }

      this.reportFailure(lastError.isBlank() ? "Custom screen never appeared (timed out)" : lastError + " (timed out)");
   }

   private void reportFailure(String reason) {
      showThrottled(reason != null && !reason.isBlank() ? reason : "Custom screen action failed");
   }

   private static void showThrottled(String message) {
      long now = System.currentTimeMillis();
      synchronized (CustomMenuAction.class) {
         if (message.equals(lastError) && now - lastErrorAt < 15000L) {
            return;
         }

         lastError = message;
         lastErrorAt = now;
      }

      bi.read(message);
   }

   public int timeout() {
      return Math.max(100, Math.min(120000, this.timeoutMs));
   }

   @Override
   public MacroActionType getType() {
      return MacroActionType.CUSTOM_MENU;
   }

   @Override
   public CompoundTag toTag() {
      CompoundTag tag = new CompoundTag();
      tag.putString("type", MacroActionType.CUSTOM_MENU.name());
      tag.put("fieldValues", MacroNbtCodecHelper.toStringListTag(this.fieldValues));
      tag.putString("clickButton", this.clickButton);
      tag.putInt("timeoutMs", this.timeout());
      tag.putBoolean("enabled", this.enabled);
      return tag;
   }

   @Override
   public void fromTag(CompoundTag tag) {
      this.fieldValues.clear();
      this.clickButton = tag.getStringOr("clickButton", "");
      if (tag.getList("fieldValues").isPresent()) {
         this.fieldValues.addAll(MacroNbtCodecHelper.fromStringListTag(tag.getList("fieldValues").orElse(new ListTag())));
      } else {
         String primary = tag.getStringOr("primaryValue", "");
         if (!primary.isBlank()) {
            this.fieldValues.add(primary);
         }

         this.fieldValues.addAll(MacroNbtCodecHelper.fromStringListTag(tag.getList("inputValues").orElse(new ListTag())));
         if (this.clickButton.isBlank()) {
            String buttonMode = tag.getStringOr("buttonMode", "SAFE_AUTO");
            String selector = tag.getStringOr("buttonSelector", "");
            int index = Math.max(1, tag.getIntOr("buttonIndex", 1));
            String var10001;
            switch (buttonMode.toUpperCase(Locale.ROOT)) {
               case "ACTION_ID":
               case "LABEL":
                  var10001 = selector;
                  break;
               case "INDEX":
                  var10001 = "#" + index;
                  break;
               default:
                  var10001 = "";
            }

            this.clickButton = var10001;
         }
      }

      this.timeoutMs = Math.max(100, Math.min(120000, tag.getIntOr("timeoutMs", 30000)));
      this.enabled = tag.getBooleanOr("enabled", true);
   }

   @Override
   public String getDisplayName() {
      String button = this.clickButton.isBlank() ? "auto" : this.clickButton;
      return "Custom Screen (press " + button + ")";
   }

   @Override
   public String getIcon() {
      return "CS";
   }

   @Override
   public boolean isEnabled() {
      return this.enabled;
   }

   @Override
   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }
}
