package autismclient.mixin;

import autismclient.mixin.duck.ExternalButtonScreenDuck;
import autismclient.gui.screens.JoinMacroScreen;
import autismclient.modules.aD;
import autismclient.util.JoinMacroController;
import autismclient.util.MacroLibraryManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SelectWorldScreen.class, priority = 2000)
public abstract class AutismSelectWorldScreenMixin extends Screen implements ExternalButtonScreenDuck {
   @Unique
   private static final int BUTTON_HEIGHT = 20;
   @Unique
   private static final int MACRO_BUTTON_WIDTH = 50;
   @Unique
   private static final int BUTTON_GAP = 3;
   @Unique
   private Button autism$macroButton;

   protected AutismSelectWorldScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "repositionElements", at = @At("TAIL"))
   private void autism$repositionElements(CallbackInfo ci) {
      this.autism$layoutMacroButton();
   }

   @Unique
   private void autism$layoutMacroButton() {
      AbstractWidget back = null;

      for (GuiEventListener child : this.children()) {
         if (child instanceof AbstractWidget widget && widget != this.autism$macroButton && this.autism$isBackButton(widget)) {
            back = widget;
         }
      }

      if (aD.init_Z()) {
         if (this.autism$macroButton != null) {
            this.autism$macroButton.visible = false;
            this.autism$macroButton.active = false;
         }
      } else {
         if (this.autism$macroButton == null) {
            this.autism$macroButton = this.addRenderableWidget(
               Button.builder(Component.literal("Macro"), ignored -> this.minecraft.gui.setScreen(new JoinMacroScreen(this))).bounds(0, 0, 50, 20).build()
            );
         }

         int macroX = back != null ? back.getRight() + 3 : this.width / 2 + 104;
         int y = back != null ? back.getY() : this.height - 28;
         if (macroX + 50 > this.width - 4) {
            macroX = Math.max(4, this.width - 4 - 50);
            y = Math.max(4, y - 20 - 3);
         }

         this.autism$macroButton.setX(macroX);
         this.autism$macroButton.setY(y);
         this.autism$macroButton.setSize(50, 20);
         this.autism$macroButton.visible = true;
         this.autism$macroButton.active = true;
      }
   }

   @Unique
   private boolean autism$isBackButton(AbstractWidget widget) {
      String label = widget.getMessage().getString();
      return label.equals(Component.translatable("gui.back").getString()) || "back".equalsIgnoreCase(label);
   }

   @Override
   public void autism$renderExternalButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
      this.autism$layoutMacroButton();
      if (!aD.init_Z()) {
         String macroName = JoinMacroController.init_Str();
         String macroLabel;
         int macroColor;
         if (macroName.isBlank()) {
            macroLabel = "Join Macro: none";
            macroColor = -7370102;
         } else if (MacroLibraryManager.init_aN().init_aM_Str(macroName) == null) {
            macroLabel = "Join Macro missing: " + macroName;
            macroColor = -38037;
         } else {
            macroLabel = "Join Macro: " + macroName + " - " + JoinMacroController.load_Str();
            macroColor = -10035062;
         }

         graphics.text(this.font, this.autism$fitPlain(macroLabel, 220), 4, 4, macroColor, false);
      }
   }

   @Unique
   private String autism$fitPlain(String label, int maxWidth) {
      if (label == null) {
         return "";
      } else {
         return this.font.width(label) <= maxWidth ? label : this.font.plainSubstrByWidth(label, Math.max(1, maxWidth - 4));
      }
   }
}
