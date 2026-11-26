package autismclient.mixin;

import autismclient.commands.CommandManager;
import autismclient.e.RefQueue;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.AuditHideOff;
import autismclient.util.AutismClientMessaging;
import autismclient.util.AutismConfig;
import autismclient.util.AutismLiteVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class AutismChatScreenMixin {
   @Shadow
   protected EditBox input;

   @Inject(method = "init()V", at = @At("TAIL"))
   private void autism$infiniChatMaxLength(CallbackInfo ci) {
      if ((AutismLiteVariant.enabled() || RefQueue.init()) && this.input != null && AutismConfig.getGlobal().infiniChat) {
         this.input.setMaxLength(2147483647);
      }
   }

   @Inject(method = "normalizeChatMessage", at = @At("HEAD"), cancellable = true)
   private void autism$infiniChatNoTruncate(String message, CallbackInfoReturnable<String> cir) {
      if ((AutismLiteVariant.enabled() || RefQueue.init()) && AutismConfig.getGlobal().infiniChat) {
         cir.setReturnValue(StringUtils.normalizeSpace(message.trim()));
      }
   }

   @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
   private void yang$onSendMessage(String message, boolean addToHistory, CallbackInfo ci) {
      if (message != null) {
         String trimmed = message.trim();
         if (!trimmed.isEmpty()) {
            if (!"^toggleautism".equalsIgnoreCase(trimmed) && CommandManager.load_Z(trimmed)) {
               ci.cancel();
            } else if ("^toggleautism".equalsIgnoreCase(trimmed)) {
               if (AuditHideOff.disable()) {
                  ci.cancel();
                  return;
               }

               ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
               module.flush_V();
               Minecraft mc = Minecraft.getInstance();
               AutismClientMessaging.sendPrefixed("Autism is now " + (module.init_Z() ? "enabled" : "disabled") + ".");
               mc.gui.setScreen(null);
               ci.cancel();
            }
         }
      }
   }
}
