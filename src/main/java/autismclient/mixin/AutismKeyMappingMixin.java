package autismclient.mixin;

import autismclient.compat263.Compat263;
import autismclient.compat263.CompatGLFW;
import autismclient.mixin.accessor.AutismKeyboardHandlerAccessor;
import autismclient.mixin.accessor.AutismMouseHandlerAccessor;
import autismclient.modules.AutoTotemModule;
import autismclient.modules.TableIndex;
import autismclient.modules.aC;
import autismclient.modules.al;
import autismclient.util.AutismLiteVariant;
import autismclient.util.aB;
import autismclient.util.bA;
import autismclient.util.w;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.List;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class AutismKeyMappingMixin implements aB {
   @Shadow
   protected Key key;
   @Shadow
   @Final
   private static Map<String, KeyMapping> ALL;
   @Shadow
   @Final
   private static Map<Key, List<KeyMapping>> MAP;
   @Shadow
   private int clickCount;
   @Unique
   private static boolean autism$simulating;

   @Shadow
   public abstract void setDown(boolean var1);

   @Shadow
   protected abstract boolean shouldSetOnIngameFocus();

   @Inject(method = "set", at = @At("HEAD"), cancellable = true)
   private static void autism$freecamOwnsPhysicalSet(Key key, boolean state, CallbackInfo ci) {
      if (!autism$simulating && aC.init_Z()) {
         List<KeyMapping> mappings = MAP.get(key);
         if (mappings != null) {
            for (KeyMapping mapping : mappings) {
               if (!aC.init_Z_KeyMapping(mapping)) {
                  mapping.setDown(state);
               }
            }
         }

         ci.cancel();
      }
   }

   @Inject(method = "click", at = @At("HEAD"), cancellable = true)
   private static void autism$freecamOwnsPhysicalClick(Key key, CallbackInfo ci) {
      if (!autism$simulating && aC.init_Z()) {
         List<KeyMapping> mappings = MAP.get(key);
         if (mappings != null) {
            for (KeyMapping mapping : mappings) {
               if (!aC.init_Z_KeyMapping(mapping)) {
                  aB.init(mapping).autism$countClick();
               }
            }
         }

         ci.cancel();
      }
   }

   @Inject(method = "setAll", at = @At("HEAD"), cancellable = true)
   private static void autism$freecamOwnsPhysicalSetAll(CallbackInfo ci) {
      if (aC.init_Z()) {
         Window window = Minecraft.getInstance().getWindow();

         for (KeyMapping mapping : ALL.values()) {
            aB.init(mapping).autism$refocus(window);
         }

         ci.cancel();
      }
   }

   @Unique
   @Override
   public void autism$countClick() {
      this.clickCount++;
   }

   @Unique
   @Override
   public void autism$refocus(Window window) {
      if (this.shouldSetOnIngameFocus() && !aC.init_Z_KeyMapping((KeyMapping)(Object)this)) {
         int code = this.key.getValue();
         boolean down = false;
         if (code >= 0) {
            Type type = this.key.getType();
            if (type == Type.KEYBOARD) {
               down = Compat263.isKeyDown(window, code);
            } else if (type == Type.MOUSE) {
               down = CompatGLFW.glfwGetMouseButton(window.handle(), CompatGLFW.sdlMouseToGlfw(code)) == 1;
            }
         }

         this.setDown(down);
      }
   }

   @Unique
   @Override
   public boolean autism$physicalForBody() {
      return !aC.init_Z_KeyMapping((KeyMapping)(Object)this) && this.autism$isActuallyDown();
   }

   @Inject(method = "releaseAll", at = @At("TAIL"))
   private static void autism$reassertModuleHeldKeys(CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.options != null && al.tick_Z()) {
         mc.options.keyShift.setDown(true);
      }

      bA.read();
   }

   @Unique
   @Override
   public boolean autism$isActuallyDown() {
      if (!AutismLiteVariant.enabled()) {
         Boolean injected = w.init((KeyMapping)(Object)this);
         if (injected != null) {
            return injected;
         }
      }

      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null) {
         Window window = mc.getWindow();
         int code = this.key.getValue();
         if (code < 0) {
            return false;
         } else {
            return this.key.getType() == Type.MOUSE
               ? CompatGLFW.glfwGetMouseButton(window.handle(), CompatGLFW.sdlMouseToGlfw(code)) == 1
               : Compat263.isKeyDown(window, code);
         }
      } else {
         return false;
      }
   }

   @Unique
   @Override
   public void autism$resetPressedState() {
      this.setDown(this.autism$physicalForBody());
   }

   @Unique
   @Override
   public void autism$simulatePress(boolean pressed) {
      Minecraft mc = Minecraft.getInstance();
      if (mc != null && mc.getWindow() != null && mc.keyboardHandler != null && mc.mouseHandler != null) {
         Window window = mc.getWindow();
         int action = pressed ? 1 : 0;
         boolean outer = autism$simulating;
         autism$simulating = true;

         try {
            switch (this.key.getType()) {
               case KEYBOARD:
                  ((AutismKeyboardHandlerAccessor)mc.keyboardHandler).autism$invokeKeyPress(window.handle(), action, new KeyEvent(this.key.getValue(), 0, 0));
                  break;
               case MOUSE:
                  ((AutismMouseHandlerAccessor)mc.mouseHandler).autism$invokeOnButton(window.handle(), new MouseButtonInfo(this.key.getValue(), 0), action);
                  break;
               default:
                  this.setDown(pressed);
            }
         } finally {
            autism$simulating = outer;
         }
      }
   }

   @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
   private void autism$consumeHotbarKeysDuringTotemOperation(CallbackInfoReturnable<Boolean> cir) {
      if (AutoTotemModule.init_Z_6() || TableIndex.init_Z_20()) {
         Minecraft mc = Minecraft.getInstance();
         if (mc != null && mc.options != null) {
            KeyMapping self = (KeyMapping)(Object)this;

            for (KeyMapping hotbar : mc.options.keyHotbarSlots) {
               if (self == hotbar) {
                  cir.setReturnValue(false);
                  return;
               }
            }

            if (self == mc.options.keySwapOffhand || self == mc.options.keyDrop || self == mc.options.keyPickItem) {
               cir.setReturnValue(false);
            }
         }
      }
   }
}
