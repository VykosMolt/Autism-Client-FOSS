package autismclient.mixin;

import autismclient.gui.d.HashUtil;
import autismclient.gui.d.TaskTimer;
import autismclient.modules.PoolEntry;
import autismclient.modules.aD;
import autismclient.util.AutismProfilesOverlay;
import autismclient.util.aJ;
import autismclient.util.bk;
import autismclient.util.cX;
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
               this.minecraft.gui.setScreen(new HashUtil(this, HashUtil.Inner_DataUtil.load));
            }
         });
         this.autism$rightButton(Component.literal("Profiles"), 28, b -> this.autism$openMenuOverlay());
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
   private void autism$openMenuOverlay() {
      if (!aD.read_Z()) {
         PoolEntry mod = PoolEntry.init_PoolEntry();
         if (mod != null) {
            cX overlay = mod.load_cX();
            if (overlay != null) {
               bk.init_bk().init_V(overlay);
               if (overlay instanceof AutismProfilesOverlay pf) {
                  pf.load(true);
               }

               overlay.init_V_Z(true);
               this.minecraft.gui.setScreen(new TaskTimer(overlay, this, true));
            }
         }
      }
   }
}
