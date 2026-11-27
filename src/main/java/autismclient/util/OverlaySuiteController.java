package autismclient.util;

import autismclient.gui.macro.editor.ActionEditorOverlay;
import net.minecraft.client.gui.Font;

public final class OverlaySuiteController {
   private final AutismLANSyncOverlay init_AutismLANSyncOverl;
   private final AutismMacroListOverlay init_AutismMacroListOve;
   private final AutismQueueEditorOverlay init_AutismQueueEditorO;
   private final AutismCustomFilterOverlay init_AutismCustomFilter;
   private final AutismCustomFilterPresetOverlay init_AutismCustomFilter_2;
   private final AutismMacroEditorOverlay init_AutismMacroEditorO;
   private final AutismKeybindOverlay init_AutismKeybindOverl;
   private final AutismLauncherOverlay init_AutismLauncherOver;
   private AutismPacketLoggerOverlay init_AutismPacketLogger;
   private AutismServerInfoOverlay init_AutismServerInfoOv;

   private OverlaySuiteController(Font font, AutismOverlay tiedOverlay) {
      LanSyncManager.getInstance().setOnSessionChanged(() -> {});
      this.init_AutismLANSyncOverl = AutismLANSyncOverlay.getOrCreate(font);
      this.init_AutismMacroListOve = tiedOverlay instanceof AutismMacroListOverlay library ? library : new AutismMacroListOverlay(font);
      this.init_AutismQueueEditorO = new AutismQueueEditorOverlay(font);
      this.init_AutismCustomFilter = new AutismCustomFilterOverlay(font);
      this.init_AutismCustomFilter_2 = this.init_AutismCustomFilter.getPresetOverlay();
      this.init_AutismLANSyncOverl.restoreState();
      if (this.init_AutismMacroListOve != tiedOverlay) {
         this.init_AutismMacroListOve.restoreState();
      }

      this.init_AutismQueueEditorO.restoreState();
      this.init_AutismCustomFilter.loadBounds();
      if (this.init_AutismCustomFilter_2 != null) {
         this.init_AutismCustomFilter_2.loadBounds();
      }

      this.init_AutismMacroEditorO = AutismMacroEditorOverlay.getInstance();
      if (this.init_AutismMacroEditorO != null) {
         this.init_AutismMacroEditorO.restoreState();
      }

      OverlayManager manager = OverlayManager.getInstance();
      manager.clearNonPersistent();
      manager.register(this.init_AutismLANSyncOverl);
      manager.register(this.init_AutismMacroListOve);
      manager.register(this.init_AutismQueueEditorO);
      manager.register(this.init_AutismCustomFilter);
      if (this.init_AutismCustomFilter_2 != null) {
         manager.register(this.init_AutismCustomFilter_2);
      }

      if (this.init_AutismMacroEditorO != null) {
         manager.register(this.init_AutismMacroEditorO);
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

      this.init_AutismKeybindOverl = new AutismKeybindOverlay();
      this.init_AutismKeybindOverl.loadBounds();
      manager.register(this.init_AutismKeybindOverl);
      this.init_AutismLauncherOver = new AutismLauncherOverlay(
         this.init_AutismMacroListOve,
         null,
         this.init_AutismLANSyncOverl,
         this.init_AutismQueueEditorO,
         this.init_AutismPacketLogger,
         this.init_AutismCustomFilter
      );
      this.init_AutismLauncherOver.setKeybindOverlay(this.init_AutismKeybindOverl);
      this.init_AutismLauncherOver.setPacketLoggerSupplier(() -> {
         if (this.init_AutismPacketLogger == null && autismModule != null) {
            this.init_AutismPacketLogger = autismModule.getOrCreatePacketLoggerOverlay();
            if (this.init_AutismPacketLogger != null) {
               this.init_AutismPacketLogger.restoreState();
            }
         }

         if (this.init_AutismPacketLogger != null) {
            manager.register(this.init_AutismPacketLogger);
         }

         return this.init_AutismPacketLogger;
      });
      this.init_AutismLauncherOver.setServerInfoSupplier(() -> {
         if (this.init_AutismServerInfoOv == null) {
            this.init_AutismServerInfoOv = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         }

         if (this.init_AutismServerInfoOv != null) {
            manager.register(this.init_AutismServerInfoOv);
         }

         return this.init_AutismServerInfoOv;
      });
      if (this.init_AutismPacketLogger == null && AutismPacketLoggerOverlay.isSavedVisible()) {
         this.init_AutismPacketLogger = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreatePacketLoggerOverlay();
         if (this.init_AutismPacketLogger != null) {
            this.init_AutismPacketLogger.restoreState();
            if (this.init_AutismPacketLogger.isVisible()) {
               manager.register(this.init_AutismPacketLogger);
            }
         }
      }

      if (this.init_AutismServerInfoOv == null && AutismServerInfoOverlay.isSavedVisible()) {
         this.init_AutismServerInfoOv = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         if (this.init_AutismServerInfoOv != null) {
            this.init_AutismServerInfoOv.restoreState();
            if (this.init_AutismServerInfoOv.isVisible()) {
               manager.register(this.init_AutismServerInfoOv);
            }
         }
      }

      if (!AutismLiteVariant.enabled() && autismModule != null) {
         AutismOverlay profilesOverlay = autismModule.getOrCreateProfilesOverlay();
         if (profilesOverlay != null && profilesOverlay.isVisible()) {
            manager.register(profilesOverlay);
         }
      }

      this.init_AutismLauncherOver.loadBounds();
      manager.register(this.init_AutismLauncherOver);
   }

   public static OverlaySuiteController init(Font font) {
      return init(font, null);
   }

   public static OverlaySuiteController init(Font font, AutismOverlay tiedOverlay) {
      return new OverlaySuiteController(font, tiedOverlay);
   }

   public void init() {
      if (this.init_AutismLANSyncOverl != null) {
         this.init_AutismLANSyncOverl.saveState();
      }

      if (this.init_AutismMacroListOve != null) {
         this.init_AutismMacroListOve.saveState();
      }

      if (this.init_AutismQueueEditorO != null) {
         this.init_AutismQueueEditorO.saveState();
      }

      if (this.init_AutismMacroEditorO != null) {
         this.init_AutismMacroEditorO.saveState();
      }

      if (this.init_AutismLauncherOver != null) {
         this.init_AutismLauncherOver.saveBounds();
      }

      if (this.init_AutismPacketLogger != null) {
         this.init_AutismPacketLogger.saveState();
      }

      if (this.init_AutismCustomFilter != null) {
         this.init_AutismCustomFilter.saveBounds();
      }

      if (this.init_AutismCustomFilter_2 != null) {
         this.init_AutismCustomFilter_2.saveBounds();
      }

      if (this.init_AutismKeybindOverl != null) {
         this.init_AutismKeybindOverl.saveBounds();
      }

      if (this.init_AutismServerInfoOv != null) {
         this.init_AutismServerInfoOv.saveState();
      }

      OverlayManager.getInstance().clearNonPersistent();
   }
}
