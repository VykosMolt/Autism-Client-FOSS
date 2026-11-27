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
      this.init_AutismLANSyncOverl = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(font);
      this.init_AutismMacroListOve = tiedOverlay instanceof AutismMacroListOverlay library ? library : new AutismMacroListOverlay(font);
      this.init_AutismQueueEditorO = new AutismQueueEditorOverlay(font);
      this.init_AutismCustomFilter = new AutismCustomFilterOverlay(font);
      this.init_AutismCustomFilter_2 = this.init_AutismCustomFilter.init_AutismCustomFilter();
      this.init_AutismLANSyncOverl.write_V_10();
      if (this.init_AutismMacroListOve != tiedOverlay) {
         this.init_AutismMacroListOve.update_V_11();
      }

      this.init_AutismQueueEditorO.sync_V_17();
      this.init_AutismCustomFilter.loadBounds();
      if (this.init_AutismCustomFilter_2 != null) {
         this.init_AutismCustomFilter_2.loadBounds();
      }

      this.init_AutismMacroEditorO = AutismMacroEditorOverlay.init_AutismMacroEditorO_2();
      if (this.init_AutismMacroEditorO != null) {
         this.init_AutismMacroEditorO.poll_V_5();
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
      AutismItemNbtInspectOverlay itemNbtInspectOverlay = AutismItemNbtInspectOverlay.init_AutismItemNbtInspe_Font(font);
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
      this.init_AutismLauncherOver.init_V_AutismKeybindOverl(this.init_AutismKeybindOverl);
      this.init_AutismLauncherOver.init_V_Supplier(() -> {
         if (this.init_AutismPacketLogger == null && autismModule != null) {
            this.init_AutismPacketLogger = autismModule.getOrCreatePacketLoggerOverlay();
            if (this.init_AutismPacketLogger != null) {
               this.init_AutismPacketLogger.flush_V_13();
            }
         }

         if (this.init_AutismPacketLogger != null) {
            manager.register(this.init_AutismPacketLogger);
         }

         return this.init_AutismPacketLogger;
      });
      this.init_AutismLauncherOver.load_V_Supplier(() -> {
         if (this.init_AutismServerInfoOv == null) {
            this.init_AutismServerInfoOv = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         }

         if (this.init_AutismServerInfoOv != null) {
            manager.register(this.init_AutismServerInfoOv);
         }

         return this.init_AutismServerInfoOv;
      });
      if (this.init_AutismPacketLogger == null && AutismPacketLoggerOverlay.poll_Z_13()) {
         this.init_AutismPacketLogger = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreatePacketLoggerOverlay();
         if (this.init_AutismPacketLogger != null) {
            this.init_AutismPacketLogger.flush_V_13();
            if (this.init_AutismPacketLogger.isVisible()) {
               manager.register(this.init_AutismPacketLogger);
            }
         }
      }

      if (this.init_AutismServerInfoOv == null && AutismServerInfoOverlay.poll_Z_16()) {
         this.init_AutismServerInfoOv = autismclient.modules.ModuleRuntimeManager.getInstance().getOrCreateServerInfoOverlay();
         if (this.init_AutismServerInfoOv != null) {
            this.init_AutismServerInfoOv.update_V_21();
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
         this.init_AutismLANSyncOverl.read_V_5();
      }

      if (this.init_AutismMacroListOve != null) {
         this.init_AutismMacroListOve.write_V_13();
      }

      if (this.init_AutismQueueEditorO != null) {
         this.init_AutismQueueEditorO.update_V_20();
      }

      if (this.init_AutismMacroEditorO != null) {
         this.init_AutismMacroEditorO.open_V_7();
      }

      if (this.init_AutismLauncherOver != null) {
         this.init_AutismLauncherOver.saveBounds();
      }

      if (this.init_AutismPacketLogger != null) {
         this.init_AutismPacketLogger.write_V_18();
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
         this.init_AutismServerInfoOv.write_V_24();
      }

      OverlayManager.getInstance().clearNonPersistent();
   }
}
