package autismclient.mixin;

import autismclient.ai.aP;
import autismclient.commands.CommandManager;
import autismclient.compat263.Compat263;
import autismclient.util.AutismLiteVariant;
import autismclient.util.cx;
import autismclient.util.n;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Util;
import net.minecraft.util.Util.OS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyboardHandler.class)
public class AutismKeyboardHandlerMixin {
   private static long autism$lastScreenSeenMs;

   @WrapMethod(method = "keyPress")
   private void autism$userKey(long window, int action, KeyEvent event, Operation<Void> original) {
      cx.init_V();

      try {
         if (action == 1 && event != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.player != null && window == mc.getWindow().handle()) {
               if (mc.gui.screen() != null) {
                  autism$lastScreenSeenMs = System.currentTimeMillis();
               } else if (System.currentTimeMillis() - autism$lastScreenSeenMs >= 250L && Compat263.keyEventGlfwModifiers(event) == 0) {
                  String prefix = CommandManager.init_Str();
                  if (prefix != null
                     && prefix.length() == 1
                     && !"/".equals(prefix)
                     && autism$matchesPrefixKey(prefix.charAt(0), Compat263.keyEventGlfwKey(event))) {
                     mc.gui.setScreen(new ChatScreen(prefix, false));
                     autism$lastScreenSeenMs = System.currentTimeMillis();
                     return;
                  }
               }
            }
         }

         if (AutismLiteVariant.enabled() || event == null || window != Minecraft.getInstance().getWindow().handle() || !aP.init_Z_I_KeyEvent(action, event)) {
            original.call(window, action, event);
         }
      } finally {
         cx.load_V();
      }
   }

   private static boolean autism$matchesPrefixKey(char ch, int glfwKey) {
      boolean var10000;
      switch (ch) {
         case '\'':
            var10000 = glfwKey == 39;
            break;
         case ',':
            var10000 = glfwKey == 44;
            break;
         case '-':
            var10000 = glfwKey == 45;
            break;
         case '.':
            var10000 = glfwKey == 46;
            break;
         case ';':
            var10000 = glfwKey == 59;
            break;
         case '=':
            var10000 = glfwKey == 61;
            break;
         case '[':
            var10000 = glfwKey == 91;
            break;
         case '\\':
            var10000 = glfwKey == 92;
            break;
         case ']':
            var10000 = glfwKey == 93;
            break;
         case '`':
            var10000 = glfwKey == 96;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }

   @WrapMethod(method = "charTyped")
   private void autism$userText(long window, CharacterEvent event, Operation<Void> original) {
      cx.init_V();

      try {
         if (AutismLiteVariant.enabled() || !aP.load_Z()) {
            original.call(window, event);
            return;
         }
      } finally {
         cx.load_V();
      }
   }

   @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
   private void autism$prefixOpensChat(long window, CharacterEvent event, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && event != null && mc.player != null) {
         if (mc.gui.screen() != null) {
            autism$lastScreenSeenMs = System.currentTimeMillis();
         } else if (System.currentTimeMillis() - autism$lastScreenSeenMs >= 250L) {
            String prefix = CommandManager.init_Str();
            if (prefix != null && prefix.length() == 1 && !"/".equals(prefix) && event.codepoint() == prefix.charAt(0)) {
               mc.gui.setScreen(new ChatScreen(prefix, false));
               autism$lastScreenSeenMs = System.currentTimeMillis();
               ci.cancel();
            }
         }
      }
   }

   @Inject(method = "getClipboard", at = @At("HEAD"), cancellable = true)
   private void autism$linuxClipboardGet(CallbackInfoReturnable<String> cir) {
      if (Util.getPlatform() == OS.LINUX) {
         cir.setReturnValue(n.init_Str());
      }
   }

   @Inject(method = "setClipboard", at = @At("HEAD"), cancellable = true)
   private void autism$linuxClipboardSet(String text, CallbackInfo ci) {
      if (Util.getPlatform() == OS.LINUX) {
         n.init_V(text);
         ci.cancel();
      }
   }
}
