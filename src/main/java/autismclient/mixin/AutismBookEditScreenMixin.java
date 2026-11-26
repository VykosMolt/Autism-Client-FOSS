package autismclient.mixin;

import autismclient.util.AutismClientMessaging;
import autismclient.util.ScreenOverlayHost;
import autismclient.util.AutismSessionState;
import autismclient.util.DesyncableScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookEditScreen.class)
public abstract class AutismBookEditScreenMixin extends Screen implements DesyncableScreen {
   @Unique
   private static final Minecraft MC = Minecraft.getInstance();
   @Unique
   private final ScreenOverlayHost autism$host = ScreenOverlayHost.forBook();

   protected AutismBookEditScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void yang$init(CallbackInfo ci) {
      this.autism$host.onInit(this, this.font);
   }

   @Override
   public void removed() {
      this.autism$host.onBookRemoved();
      super.removed();
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.autism$host.mouseClicked(click) || super.mouseClicked(click, doubled);
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent click) {
      return this.autism$host.mouseReleased(click) || super.mouseReleased(click);
   }

   @Override
   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.autism$host.mouseDragged(click, deltaX, deltaY) || super.mouseDragged(click, deltaX, deltaY);
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.autism$host.mouseScrolled(mouseX, mouseY, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
   }

   @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
   private void yang$keyPressed(KeyEvent input, CallbackInfoReturnable<Boolean> cir) {
      if (this.autism$host.keyPressed(input)) {
         cir.setReturnValue(true);
      }
   }

   @Override
   public boolean charTyped(CharacterEvent input) {
      return this.autism$host.charTyped(input) || super.charTyped(input);
   }

   @Override
   public void autism$closeWithPacket() {
      this.autism$closeWithPacket(true);
   }

   @Override
   public void autism$closeWithPacket(boolean notify) {
      if (MC.getConnection() != null) {
         AutismSessionState.init_ck().data(true);
         this.autism$invokeSaveChanges();
      }

      MC.gui.setScreen(null);
   }

   @Override
   public void autism$closeWithoutPacket() {
      this.autism$closeWithoutPacket(true);
   }

   @Override
   public void autism$closeWithoutPacket(boolean notify) {
      MC.gui.setScreen(null);
      if (notify) {
         AutismClientMessaging.sendPrefixed("Book edit closed without packet.");
      }
   }

   @Override
   public void autism$desync() {
      this.autism$desync(true);
   }

   @Override
   public void autism$desync(boolean notify) {
      if (MC.getConnection() == null) {
         if (notify) {
            AutismClientMessaging.sendPrefixed("Failed to desync: no network.");
         }
      } else {
         AutismSessionState.init_ck().data(true);
         this.autism$invokeSaveChanges();
         if (notify) {
            AutismClientMessaging.sendPrefixed("Book update packet sent; GUI intentionally stays open.");
         }
      }
   }

   @Invoker("saveChanges")
   protected abstract void autism$invokeSaveChanges();
}
