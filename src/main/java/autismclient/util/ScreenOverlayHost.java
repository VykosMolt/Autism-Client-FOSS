package autismclient.util;

import autismclient.compat263.Compat263;
import autismclient.gui.macro.editor.ActionEditorOverlay;
import autismclient.modules.ModuleRuntimeManager;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.client.gui.screens.dialog.WaitingForResponseScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public final class ScreenOverlayHost {
   private final boolean dialogMode;
   private AutismLauncherOverlay launcherOverlay;
   private AutismLANSyncOverlay lanSyncOverlay;
   private AutismMacroListOverlay macroListOverlay;
   private AutismQueueEditorOverlay queueEditorOverlay;
   private AutismPacketLoggerOverlay packetLoggerOverlay;
   private AutismCustomFilterOverlay customFilterOverlay;
   private AutismCustomFilterPresetOverlay customFilterPresetOverlay;
   private AutismMacroEditorOverlay macroEditorOverlay;
   private AutismKeybindOverlay keybindOverlay;
   private AutismServerInfoOverlay serverInfoOverlay;
   private boolean overlaysInitialized;

   private ScreenOverlayHost(boolean dialogMode) {
      this.dialogMode = dialogMode;
   }

   public static ScreenOverlayHost forBook() {
      return new ScreenOverlayHost(false);
   }

   public static ScreenOverlayHost forDialog() {
      return new ScreenOverlayHost(true);
   }

   public boolean isActive() {
      ModuleRuntimeManager module = ModuleRuntimeManager.getInstance();
      return module != null && (this.dialogMode ? module.isInitializedAndVisible() : module.isActive());
   }

   public AutismQueueEditorOverlay queueEditorOverlay() {
      return this.queueEditorOverlay;
   }

   public void onInit(Screen screen, Font font) {
      if (this.dialogMode) {
         ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
            if (this.isActive()) {
               try {
                  OverlayManager.getInstance().render(drawContext, mouseX, mouseY, tickDelta);
               } catch (Throwable ignored) {
               }
            }
         });
         ScreenEvents.remove(screen).register(scrn -> {
            Minecraft mc = Minecraft.getInstance();
            boolean stillOnDialog = mc != null && (mc.gui.screen() instanceof DialogScreen || mc.gui.screen() instanceof WaitingForResponseScreen);
            if (!stillOnDialog) {
               this.overlaysInitialized = false;
               this.saveDialogOverlays();
            }
         });
         if (this.isActive() && !OverlayScreenController.shouldInterceptBackgroundScreen()) {
            if (!this.overlaysInitialized || !OverlayManager.getInstance().hasOverlays()) {
               try {
                  this.buildOverlays(font);
                  this.overlaysInitialized = true;
               } catch (Throwable ignored) {
               }
            } else if (this.launcherOverlay != null) {
               this.launcherOverlay.setVisible(true);
            }
         }
      } else if (this.isActive() && !OverlayScreenController.shouldInterceptBackgroundScreen()) {
         this.buildOverlays(font);
         this.overlaysInitialized = true;
         ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
            if (this.isActive()) {
               OverlayManager.getInstance().render(drawContext, mouseX, mouseY, tickDelta);
            }
         });
      }
   }

   private void buildOverlays(Font font) {
      LanSyncManager.getInstance().setOnSessionChanged(() -> {});
      this.lanSyncOverlay = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(font);
      this.macroListOverlay = new AutismMacroListOverlay(font);
      this.queueEditorOverlay = new AutismQueueEditorOverlay(font);
      this.customFilterOverlay = new AutismCustomFilterOverlay(font);
      this.customFilterPresetOverlay = this.customFilterOverlay.getPresetOverlay();
      this.lanSyncOverlay.write_V_10();
      this.macroListOverlay.update_V_11();
      this.queueEditorOverlay.sync_V_17();
      this.customFilterOverlay.loadBounds();
      if (this.customFilterPresetOverlay != null) {
         this.customFilterPresetOverlay.loadBounds();
      }

      Minecraft mc = Minecraft.getInstance();
      boolean inWorld = mc != null && mc.player != null && mc.level != null;
      this.macroEditorOverlay = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (this.macroEditorOverlay != null) {
         this.macroEditorOverlay.poll_V_5();
         if (this.dialogMode) {
            this.macroEditorOverlay.load_V_Z_3(!inWorld);
         }
      }

      OverlayManager manager = OverlayManager.getInstance();
      manager.clearNonPersistent();
      manager.register(this.lanSyncOverlay);
      manager.register(this.macroListOverlay);
      manager.register(this.queueEditorOverlay);
      manager.register(this.customFilterOverlay);
      if (this.customFilterPresetOverlay != null) {
         manager.register(this.customFilterPresetOverlay);
      }

      if (this.macroEditorOverlay != null) {
         manager.register(this.macroEditorOverlay);
      }

      if (this.dialogMode) {
         ActionEditorOverlay actionEditor = ActionEditorOverlay.getInstance();
         actionEditor.setCaptureAllowed(inWorld);
         manager.register(actionEditor);
      }

      ModuleRuntimeManager autismModule = ModuleRuntimeManager.getInstance();
      this.keybindOverlay = new AutismKeybindOverlay();
      this.keybindOverlay.loadBounds();
      manager.register(this.keybindOverlay);
      this.launcherOverlay = new AutismLauncherOverlay(
         this.macroListOverlay, null, this.lanSyncOverlay, this.queueEditorOverlay, this.packetLoggerOverlay, this.customFilterOverlay
      );
      this.launcherOverlay.init_V_AutismKeybindOverl(this.keybindOverlay);
      this.launcherOverlay.init_V_Supplier(() -> {
         if (this.packetLoggerOverlay == null && autismModule != null) {
            this.packetLoggerOverlay = autismModule.getOrCreatePacketLoggerOverlay();
            if (this.packetLoggerOverlay != null) {
               this.packetLoggerOverlay.restoreState();
            }
         }

         if (this.packetLoggerOverlay != null) {
            manager.register(this.packetLoggerOverlay);
         }

         return this.packetLoggerOverlay;
      });
      this.launcherOverlay.load_V_Supplier(() -> {
         if (this.serverInfoOverlay == null) {
            ModuleRuntimeManager mod = ModuleRuntimeManager.getInstance();
            if (mod != null) {
               this.serverInfoOverlay = mod.getOrCreateServerInfoOverlay();
            }
         }

         if (this.serverInfoOverlay != null) {
            manager.register(this.serverInfoOverlay);
         }

         return this.serverInfoOverlay;
      });
      if (this.packetLoggerOverlay == null && AutismPacketLoggerOverlay.isSavedVisible()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.getInstance();
         this.packetLoggerOverlay = mod == null ? null : mod.getOrCreatePacketLoggerOverlay();
         if (this.packetLoggerOverlay != null) {
            this.packetLoggerOverlay.restoreState();
            if (this.packetLoggerOverlay.isVisible()) {
               manager.register(this.packetLoggerOverlay);
            }
         }
      }

      if (this.serverInfoOverlay == null && AutismServerInfoOverlay.isSavedVisible()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.getInstance();
         this.serverInfoOverlay = mod == null ? null : mod.getOrCreateServerInfoOverlay();
         if (this.serverInfoOverlay != null) {
            this.serverInfoOverlay.restoreState();
            if (this.serverInfoOverlay.isVisible()) {
               manager.register(this.serverInfoOverlay);
            }
         }
      }

      if (this.dialogMode && !AutismLiteVariant.enabled()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.getInstance();
         AutismOverlay profilesOverlay = mod == null ? null : mod.getOrCreateProfilesOverlay();
         if (profilesOverlay != null && profilesOverlay.isVisible()) {
            manager.register(profilesOverlay);
         }
      }

      this.launcherOverlay.loadBounds();
      if (this.dialogMode) {
         this.launcherOverlay.setVisible(true);
      }

      manager.register(this.launcherOverlay);
   }

   public void onBookRemoved() {
      if ((this.overlaysInitialized || this.isActive()) && !OverlayScreenController.shouldInterceptBackgroundScreen()) {
         this.overlaysInitialized = false;
         this.persistOverlays();
         OverlayManager.getInstance().clearNonPersistent();
      }
   }

   private void saveDialogOverlays() {
      if (this.isActive() && !OverlayScreenController.shouldInterceptBackgroundScreen()) {
         try {
            OverlayManager.getInstance().restoreStashedIfFits();
            this.persistOverlays();
         } finally {
            OverlayManager.getInstance().clearNonPersistent();
         }
      }
   }

   private void persistOverlays() {
      if (this.lanSyncOverlay != null) {
         this.lanSyncOverlay.read_V_5();
      }

      if (this.macroListOverlay != null) {
         this.macroListOverlay.write_V_13();
      }

      if (this.queueEditorOverlay != null) {
         this.queueEditorOverlay.update_V_20();
      }

      if (this.macroEditorOverlay != null) {
         this.macroEditorOverlay.open_V_7();
      }

      if (this.launcherOverlay != null) {
         this.launcherOverlay.saveBounds();
      }

      if (this.packetLoggerOverlay != null) {
         this.packetLoggerOverlay.saveState();
      }

      if (this.customFilterOverlay != null) {
         this.customFilterOverlay.saveBounds();
      }

      if (this.customFilterPresetOverlay != null) {
         this.customFilterPresetOverlay.saveBounds();
      }

      if (this.keybindOverlay != null) {
         this.keybindOverlay.saveBounds();
      }

      if (this.serverInfoOverlay != null) {
         this.serverInfoOverlay.saveState();
      }
   }

   public boolean mouseClicked(MouseButtonEvent click) {
      return this.isActive() && OverlayManager.getInstance().mouseClicked(click.x(), click.y(), Compat263.mouseEventGlfwButton(click));
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      return this.isActive() && OverlayManager.getInstance().mouseReleased(click.x(), click.y(), Compat263.mouseEventGlfwButton(click));
   }

   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.isActive() && OverlayManager.getInstance().mouseDragged(click.x(), click.y(), Compat263.mouseEventGlfwButton(click), deltaX, deltaY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
      return this.isActive() && OverlayManager.getInstance().mouseScrolled(mouseX, mouseY, verticalAmount);
   }

   public boolean keyPressed(KeyEvent input) {
      return this.isActive() && OverlayManager.getInstance().keyPressed(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
   }

   public boolean charTyped(CharacterEvent input) {
      return this.isActive() && OverlayManager.getInstance().charTyped((char)input.codepoint(), 0);
   }
}
