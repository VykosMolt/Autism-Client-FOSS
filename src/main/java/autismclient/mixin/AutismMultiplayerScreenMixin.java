package autismclient.mixin;

import autismclient.c.ExternalButtonScreenDuck;
import autismclient.gui.d.ProxiesScreen;
import autismclient.gui.d.JoinMacroScreen;
import autismclient.gui.d.PluginLibraryScreen;
import autismclient.gui.d.SimpleVoiceChatDetectedScreen;
import autismclient.gui.f.RenderState;
import autismclient.gui.f.UiHelper;
import autismclient.gui.f.b.h;
import autismclient.modules.ModuleRuntimeManager;
import autismclient.modules.aD;
import autismclient.util.AutismConfig;
import autismclient.util.JoinMacroController;
import autismclient.util.MacroLibraryManager;
import autismclient.util.bW;
import autismclient.util.ProxyListManager;
import autismclient.util.cA;
import java.util.Locale;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = JoinMultiplayerScreen.class, priority = 2000)
public abstract class AutismMultiplayerScreenMixin extends Screen implements ExternalButtonScreenDuck {
   @Unique
   private static final int BUTTON_HEIGHT = 20;
   @Unique
   private static final int BUTTON_WIDTH = 60;
   @Unique
   private static final int MACRO_BUTTON_WIDTH = 50;
   @Unique
   private static final int STACK_WIDTH = 104;
   @Unique
   private static final int MARGIN = 4;
   @Unique
   private static final int GAP = 3;
   @Unique
   private static final int TOP_BUTTON_HEIGHT = 16;
   @Unique
   private static final int EXTERNAL_NONE = 0;
   @Unique
   private static final int EXTERNAL_VIA_FABRIC_PLUS = 1;
   @Unique
   private static final int EXTERNAL_OPSEC = 3;
   @Unique
   private Button autism$joinMacroButton;
   @Unique
   private Button autism$proxiesButton;
   @Unique
   private Button autism$spoofButton;
   @Unique
   private Button autism$packsButton;
   @Unique
   private Button autism$libraryButton;
   @Unique
   private boolean autism$voicePromptChecked;
   @Unique
   private int autism$topButtonsLeft = 4;
   @Shadow
   protected ServerSelectionList serverSelectionList;

   protected AutismMultiplayerScreenMixin(Component title) {
      super(title);
   }

   @Inject(method = "repositionElements", at = @At("TAIL"))
   private void autism$repositionElements(CallbackInfo ci) {
      this.autism$layoutButtons();
      this.autism$maybeShowVoiceChatPrompt();
   }

   @Unique
   private void autism$maybeShowVoiceChatPrompt() {
      if (!this.autism$voicePromptChecked) {
         this.autism$voicePromptChecked = true;
         if (!aD.init_Z() && FabricLoader.getInstance().isModLoaded("voicechat")) {
            ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
            if (module != null && module.tick_Z()) {
               AutismConfig config = AutismConfig.getGlobal();
               if (config != null && !config.voiceChatModdedPromptShown) {
                  config.voiceChatModdedPromptShown = true;
                  config.save();
                  Screen parent = this;
                  this.minecraft.execute(() -> {
                     if (this.minecraft.gui.screen() == parent) {
                        this.minecraft.gui.setScreen(new SimpleVoiceChatDetectedScreen(parent));
                     }
                  });
               }
            }
         }
      }
   }

   @Unique
   private void autism$layoutButtons() {
      AbstractWidget via = null;
      AbstractWidget opsec = null;

      for (GuiEventListener child : this.children()) {
         if (child instanceof AbstractWidget widget && !this.autism$isOwned(widget)) {
            int kind = this.autism$externalKind(widget);
            if (kind == 1) {
               via = widget;
            } else if (kind == 3) {
               opsec = widget;
            }
         }
      }

      boolean hidden = aD.init_Z();
      if (hidden) {
         autism$setVisible(this.autism$joinMacroButton, false);
         autism$setVisible(this.autism$proxiesButton, false);
         autism$setVisible(this.autism$spoofButton, false);
         autism$setVisible(this.autism$packsButton, false);
         autism$setVisible(this.autism$libraryButton, false);
         autism$setVisible(via, false);
         autism$setVisible(opsec, false);
      } else {
         this.autism$ensureOwnedButtons();
         int topRight = this.width - 4;
         int topAvailable = Math.max(4, this.width - 8);
         int topGap = 2;
         int topCell = Math.max(1, (topAvailable - topGap * 2) / 3);
         int proxiesW = Math.min(this.autism$hugWidth("Proxies"), topCell);
         int libraryW = Math.min(this.autism$hugWidth("Library"), topCell);
         int macroW = Math.min(this.autism$hugWidth("Macro"), topCell);
         int cursor = topRight;
         cursor -= proxiesW;
         autism$place(this.autism$proxiesButton, cursor, 4, proxiesW, 16);
         cursor -= topGap + libraryW;
         this.autism$libraryButton.setMessage(this.autism$fitLabel(libraryW, "Library"));
         autism$place(this.autism$libraryButton, cursor, 4, libraryW, 16);
         cursor -= topGap + macroW;
         autism$place(this.autism$joinMacroButton, cursor, 4, macroW, 16);

         this.autism$topButtonsLeft = Math.max(4, cursor);
         int footerRight = this.width / 2 - 154 - 3;
         int footerWidth = Math.max(60, Math.min(104, footerRight - 4));
         int footerX = Math.max(4, footerRight - footerWidth);
         int footerY = Math.max(4, this.height - 51);
         this.autism$spoofButton.setMessage(this.autism$spoofClientLabel(footerWidth));
         this.autism$packsButton.setMessage(this.autism$bypassPacksLabel(footerWidth));
         autism$place(this.autism$spoofButton, footerX, footerY, footerWidth, 20);
         autism$place(this.autism$packsButton, footerX, footerY + 20 + 3, footerWidth, 20);
         int rightX = Math.min(this.width - 4 - 104, this.width / 2 + 154 + 3);
         int rightWidth = Math.max(60, Math.min(104, this.width - 4 - rightX));
         int count = (via != null ? 1 : 0) + (opsec != null ? 1 : 0);
         int rightY = Math.max(4, this.height - 8 - Math.max(0, count * 20 + Math.max(0, count - 1) * 3));
         int slot = 0;
         if (via != null) {
            autism$place(via, rightX, rightY + slot++ * 23, rightWidth, 20);
         }

         if (opsec != null) {
            autism$place(opsec, rightX, rightY + slot * 23, rightWidth, 20);
         }
      }
   }

   @Unique
   private void autism$ensureOwnedButtons() {
      if (this.autism$joinMacroButton == null) {
         this.autism$joinMacroButton = this.addRenderableWidget(
            Button.builder(Component.literal("Macro"), ignored -> this.minecraft.gui.setScreen(new JoinMacroScreen(this))).bounds(0, 0, 50, 20).build()
         );
      }

      if (this.autism$proxiesButton == null) {
         this.autism$proxiesButton = this.addRenderableWidget(
            Button.builder(Component.literal("Proxies"), ignored -> this.minecraft.gui.setScreen(new ProxiesScreen(this))).bounds(0, 0, 60, 20).build()
         );
      }

      if (this.autism$spoofButton == null) {
         this.autism$spoofButton = this.addRenderableWidget(Button.builder(Component.literal("Client"), ignored -> {
            ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
            if (module != null) {
               module.update(!module.tick_Z());
            }

            this.autism$layoutButtons();
         }).bounds(0, 0, 104, 20).build());
      }

      if (this.autism$packsButton == null) {
         this.autism$packsButton = this.addRenderableWidget(Button.builder(Component.literal("Packs"), ignored -> {
            ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
            if (module != null) {
               module.write(!module.pop_Z());
            }

            this.autism$layoutButtons();
         }).bounds(0, 0, 104, 20).build());
      }

      if (this.autism$libraryButton == null) {
         this.autism$libraryButton = this.addRenderableWidget(
            Button.builder(Component.literal("Library"), ignored -> this.autism$openPluginLibrary()).bounds(0, 0, 60, 20).build()
         );
      }
   }

   @Unique
   private void autism$openPluginLibrary() {
      this.minecraft.gui.setScreen(new PluginLibraryScreen(this));
   }

   @Unique
   private boolean autism$isOwned(AbstractWidget widget) {
      return widget == this.autism$joinMacroButton
         || widget == this.autism$proxiesButton
         || widget == this.autism$spoofButton
         || widget == this.autism$packsButton
         || widget == this.autism$libraryButton;
   }

   @Unique
   private static void autism$place(AbstractWidget widget, int x, int y, int width, int height) {
      if (widget != null) {
         widget.setX(Math.max(4, x));
         widget.setY(Math.max(4, y));
         widget.setSize(Math.max(1, width), Math.max(1, height));
         autism$setVisible(widget, true);
      }
   }

   @Unique
   private static void autism$setVisible(AbstractWidget widget, boolean visible) {
      if (widget != null) {
         widget.visible = visible;
         widget.active = visible;
      }
   }

   @Unique
   private Component autism$spoofClientLabel(int width) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      boolean enabled = module != null && module.tick_Z();
      return this.autism$fitLabel(width, enabled ? "Client: Vanilla" : "Client: Modded", enabled ? "Vanilla" : "Modded", "Client");
   }

   @Unique
   private Component autism$bypassPacksLabel(int width) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      boolean vanilla = module != null && module.tick_Z();
      boolean bypass = module != null && module.pop_Z();
      String label = autismclient.g.ProtectorModeDescriptions.init(vanilla, bypass);
      return this.autism$fitLabel(width, label, bypass ? "Bypass" : "Normal", "Packs");
   }

   @Unique
   private int autism$hugWidth(String label) {
      return RenderState.load(this.font.width(label));
   }

   @Unique
   private Component autism$fitLabel(int width, String... candidates) {
      int available = Math.max(1, width - 8);

      for (String candidate : candidates) {
         if (this.font.width(candidate) <= available) {
            return Component.literal(candidate);
         }
      }

      return Component.literal(candidates[candidates.length - 1]);
   }

   @Unique
   private int autism$externalKind(AbstractWidget widget) {
      String label = widget.getMessage().getString();
      String normalized = label.toLowerCase(Locale.ROOT).replace(" ", "").replace("_", "").replace(".", "");
      String className = widget.getClass().getName().toLowerCase(Locale.ROOT);
      if (FabricLoader.getInstance().isModLoaded("viafabricplus") && "ViaFabricPlus".equals(label)) {
         return 1;
      } else {
         return !className.contains("opsec") && !normalized.contains("opsec") ? 0 : 3;
      }
   }

   @Override
   public void autism$renderExternalButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
      this.autism$layoutButtons();
      if (!aD.init_Z()) {
         int leftTextWidth = Math.max(0, this.autism$topButtonsLeft - 4 - 3);
         String username = this.minecraft.getUser().getName();
         if (leftTextWidth > 8) {
            graphics.text(this.font, this.autism$fitPlain("Logged in as " + username, leftTextWidth), 4, 4, -1, false);
         }

         bW proxy = ProxyListManager.init_bY().init_bW();
         int statusY = 16;
         if (proxy != null) {
            String proxyLabel = "Using proxy " + proxy.read + ":" + proxy.init_I;
            if (leftTextWidth > 8) {
               graphics.text(this.font, this.autism$fitPlain(proxyLabel, leftTextWidth), 4, statusY, -5263441, false);
            }

            statusY += 12;
         }

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

         if (leftTextWidth > 8) {
            graphics.text(this.font, this.autism$fitPlain(macroLabel, leftTextWidth), 4, statusY, macroColor, false);
         }

         this.autism$renderButtonGuidance(graphics, mouseX, mouseY);
      }
   }

   @Unique
   private void autism$renderButtonGuidance(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      boolean vanilla = module != null && module.tick_Z();
      boolean bypass = module != null && module.pop_Z();
      String guidance = null;
      if (this.autism$spoofButton != null && this.autism$spoofButton.isMouseOver(mouseX, mouseY)) {
         guidance = autismclient.g.ProtectorModeDescriptions.init(vanilla);
      } else if (this.autism$packsButton != null && this.autism$packsButton.isMouseOver(mouseX, mouseY)) {
         guidance = autismclient.g.ProtectorModeDescriptions.load(vanilla, bypass);
      }

      if (guidance != null) {
         int virtualMouseX = cA.init_I_D(mouseX);
         int virtualMouseY = cA.init_I_D(mouseY);
         cA.init_V_GuiGraphicsExtract(graphics);

         try {
            graphics.nextStratum();
            h.init(UiHelper.init(graphics, this.font, virtualMouseX, virtualMouseY), guidance, virtualMouseX, virtualMouseY, 240);
         } finally {
            cA.load_V_GuiGraphicsExtract(graphics);
         }
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
