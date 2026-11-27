package autismclient.mixin;

import autismclient.gui.screens.AutismModulesScreen;
import autismclient.gui.screens.AutismOverlaysScreen;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.aD;
import autismclient.util.AutismProfilesOverlay;
import autismclient.util.aJ;
import autismclient.util.OverlayManager;
import autismclient.util.AutismOverlay;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class AutismTitleScreenSupportMixin extends Screen {
   protected AutismTitleScreenSupportMixin() {
      super(null);
   }

   @Inject(method = "init", at = @At("TAIL"))
   private void autism$addSupportButtons(CallbackInfo ci) {
      if (!aD.init_Z()) {
         this.autism$rightButton(Component.literal("Modules & Macros"), 4, b -> {
            if (!aD.read_Z()) {
               this.minecraft.gui.setScreen(new AutismModulesScreen(this, AutismModulesScreen.Inner_DataUtil.TITLE_SETUP));
            }
         });
         this.autism$rightButton(Component.literal("Profiles"), 28, b -> this.autism$openProfilesOverlay());
         this.autism$leftButton(Component.literal("AUTISM INC"), 4, "https://discord.gg/V8GsKP6k5u");
         this.autism$leftButton(Component.literal("AUTISM Client"), 28, "https://discord.com/invite/JZ7XgUCtBu");
      }
   }

   @Unique
   private void autism$rightButton(Component label, int y, OnPress onPress) {
      int w = this.autism$buttonWidth(label);
      this.addRenderableWidget(Button.builder(label, onPress).bounds(this.width - w - 4, y, w, 20).build());
   }

   @Unique
   private void autism$leftButton(Component label, int y, String url) {
      this.addRenderableWidget(Button.builder(label, b -> aJ.init_V(url)).bounds(4, y, this.autism$buttonWidth(label), 20).build());
   }

   @Unique
   private int autism$buttonWidth(Component label) {
      return Math.max(96, Math.min(this.width - 8, this.font.width(label) + 20));
   }

   @Unique
   private void autism$openProfilesOverlay() {
      if (!aD.read_Z()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.getInstance();
         if (mod != null) {
            AutismOverlay overlay = mod.getOrCreateProfilesOverlay();
            if (overlay != null) {
               OverlayManager.getInstance().register(overlay);
               if (overlay instanceof AutismProfilesOverlay pf) {
                  pf.load(true);
               }

               overlay.setVisible(true);
               this.minecraft.gui.setScreen(new AutismOverlaysScreen(overlay, this, true));
            }
         }
      }
   }
}
