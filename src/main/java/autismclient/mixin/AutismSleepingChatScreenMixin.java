package autismclient.mixin;

import autismclient.gui.screens.StyledButton;
import autismclient.gui.f.b.CachePool;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.util.BedSleepDesyncHelper;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InBedChatScreen.class)
public abstract class AutismSleepingChatScreenMixin extends Screen {
   protected AutismSleepingChatScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "init()V", at = @At("TAIL"))
   private void autism$init(CallbackInfo ci) {
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      if (module != null && module.isActive()) {
         this.addRenderableWidget(new StyledButton(5, 5, 140, 20, Component.literal("Client wake up"), CachePool.Inner_DataUtil.read, button -> BedSleepDesyncHelper.init_V()));
      }
   }
}
