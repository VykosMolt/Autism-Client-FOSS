package autismclient.util;

import autismclient.gui.macro.editor.ActionEditorOverlay;
import net.minecraft.client.gui.Font;

public final class OverlaySuiteController {
   private final AutismLANSyncOverlay lanSyncOverlay;
   private final AutismMacroListOverlay macroListOverlay;
   private final AutismQueueEditorOverlay queueEditorOverlay;
   private final AutismCustomFilterOverlay customFilterOverlay;
   private final AutismCustomFilterPresetOverlay customFilterPresetOverlay;
   private final AutismMacroEditorOverlay macroEditorOverlay;
   private final AutismKeybindOverlay keybindOverlay;
   private final AutismLauncherOverlay launcherOverlay;
   private AutismPacketLoggerOverlay packetLoggerOverlay;
   private AutismServerInfoOverlay serverInfoOverlay;

   private OverlaySuiteController(Font font, AutismOverlay tiedOverlay) {
      LanSyncManager.getInstance().setOnSessionChanged(() -> {});
      this.lanSyncOverlay = AutismLANSyncOverlay.getOrCreate(font);
      this.macroListOverlay = tiedOverlay instanceof AutismMacroListOverlay library ? library : new AutismMacroListOverlay(font);
      this.queueEditorOverlay = new AutismQueueEditorOverlay(font);
      this.customFilterOverlay = new AutismCustomFilterOverlay(font);
      this.customFilterPresetOverlay = this.customFilterOverlay.getPresetOverlay();
      this.lanSyncOverlay.restoreState();
      if (this.macroListOverlay != tiedOverlay) {
         this.macroListOverlay.restoreState();
      }

      this.queueEditorOverlay.restoreState();
      this.customFilterOverlay.loadBounds();
      if (this.customFilterPresetOverlay != null) {
         this.customFilterPresetOverlay.loadBounds();
      }

      this.macroEditorOverlay = AutismMacroEditorOverlay.getInstance();
      if (this.macroEditorOverlay != null) {
         this.macroEditorOverlay.restoreState();
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

      manager.register(ActionEditorOverlay.getInstance());
      AutismItemNbtInspectOverlay itemNbtInspectOverlay = AutismItemNbtInspectOverlay.getOrCreate(font);
      if (itemNbtInspectOverlay != null) {
         manager.register(itemNbtInspectOverlay);
      }

      autismclient.modules.ModuleRuntimeManager autismModule = autismclient.modules.ModuleRuntimeManager.getInstance();
      if (!AutismLiteVariant.enabled()) {
         autismclient.ai.AiAssistant.getInstance().restoreOverlay();
         autismclient.ai.SchematicWorkspace.restoreWindow();
      }

      this.keybindOverlay = new AutismKeybindOverlay();
      this.keybindOverlay.loadBounds();
      manager.register(this.keybindOverlay);
      this.launcherOverlay = new AutismLauncherOverlay(
         this.macroListOverlay,
         null,
         this.lanSyncOverlay,
         this.queueEditorOverlay,
         this.packetLoggerOverlay,
         this.customFilterOverlay
      );
      this.launcherOverlay.setKeybindOverlay(this.keybindOverlay);
      this.launcherOverlay.setPacketLoggerSupplier(() -> {
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
      this.launcherOverlay.setServerInfoSupplier(() -> {
         if (this.serverInfoOverlay == null) {
            this.serverInfoOverlay = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         }

         if (this.serverInfoOverlay != null) {
            manager.register(this.serverInfoOverlay);
         }

         return this.serverInfoOverlay;
      });
      if (this.packetLoggerOverlay == null && AutismPacketLoggerOverlay.isSavedVisible()) {
         this.packetLoggerOverlay = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreatePacketLoggerOverlay();
         if (this.packetLoggerOverlay != null) {
            this.packetLoggerOverlay.restoreState();
            if (this.packetLoggerOverlay.isVisible()) {
               manager.register(this.packetLoggerOverlay);
            }
         }
      }

      if (this.serverInfoOverlay == null && AutismServerInfoOverlay.isSavedVisible()) {
         this.serverInfoOverlay = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         if (this.serverInfoOverlay != null) {
            this.serverInfoOverlay.restoreState();
            if (this.serverInfoOverlay.isVisible()) {
               manager.register(this.serverInfoOverlay);
            }
         }
      }

      if (!AutismLiteVariant.enabled() && autismModule != null) {
         AutismOverlay profilesOverlay = autismModule.getOrCreateProfilesOverlay();
         if (profilesOverlay != null && profilesOverlay.isVisible()) {
            manager.register(profilesOverlay);
         }
      }

      this.launcherOverlay.loadBounds();
      manager.register(this.launcherOverlay);
   }

   public static OverlaySuiteController create(Font font) {
      return create(font, null);
   }

   public static OverlaySuiteController create(Font font, AutismOverlay tiedOverlay) {
      return new OverlaySuiteController(font, tiedOverlay);
   }

   public void saveStates() {
      if (this.lanSyncOverlay != null) {
         this.lanSyncOverlay.saveState();
      }

      if (this.macroListOverlay != null) {
         this.macroListOverlay.saveState();
      }

      if (this.queueEditorOverlay != null) {
         this.queueEditorOverlay.saveState();
      }

      if (this.macroEditorOverlay != null) {
         this.macroEditorOverlay.saveState();
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

      OverlayManager.getInstance().clearNonPersistent();
   }
}
