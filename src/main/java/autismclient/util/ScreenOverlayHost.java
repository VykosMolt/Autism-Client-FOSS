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
      ModuleRuntimeManager module = ModuleRuntimeManager.init_PoolEntry();
      return module != null && (this.dialogMode ? module.load_Z() : module.init_Z());
   }

   public AutismQueueEditorOverlay queueEditorOverlay() {
      return this.queueEditorOverlay;
   }

   public void onInit(Screen screen, Font font) {
      if (this.dialogMode) {
         ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
            if (this.isActive()) {
               try {
                  OverlayManager.init_bk().init(drawContext, mouseX, mouseY, tickDelta);
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
         if (this.isActive() && !OverlayScreenController.read_Z()) {
            if (!this.overlaysInitialized || !OverlayManager.init_bk().init_Z()) {
               try {
                  this.buildOverlays(font);
                  this.overlaysInitialized = true;
               } catch (Throwable ignored) {
               }
            } else if (this.launcherOverlay != null) {
               this.launcherOverlay.init_V_Z(true);
            }
         }
      } else if (this.isActive() && !OverlayScreenController.read_Z()) {
         this.buildOverlays(font);
         this.overlaysInitialized = true;
         ScreenEvents.afterExtract(screen).register((scrn, drawContext, mouseX, mouseY, tickDelta) -> {
            if (this.isActive()) {
               OverlayManager.init_bk().init(drawContext, mouseX, mouseY, tickDelta);
            }
         });
      }
   }

   private void buildOverlays(Font font) {
      LanSyncManager.init_aE().flush(() -> {});
      this.lanSyncOverlay = AutismLANSyncOverlay.init_AutismLANSyncOverl_Font(font);
      this.macroListOverlay = new AutismMacroListOverlay(font);
      this.queueEditorOverlay = new AutismQueueEditorOverlay(font);
      this.customFilterOverlay = new AutismCustomFilterOverlay(font);
      this.customFilterPresetOverlay = this.customFilterOverlay.init_AutismCustomFilter();
      this.lanSyncOverlay.write_V_10();
      this.macroListOverlay.update_V_11();
      this.queueEditorOverlay.sync_V_17();
      this.customFilterOverlay.b__V();
      if (this.customFilterPresetOverlay != null) {
         this.customFilterPresetOverlay.b__V();
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

      OverlayManager manager = OverlayManager.init_bk();
      manager.load_V();
      manager.init_V(this.lanSyncOverlay);
      manager.init_V(this.macroListOverlay);
      manager.init_V(this.queueEditorOverlay);
      manager.init_V(this.customFilterOverlay);
      if (this.customFilterPresetOverlay != null) {
         manager.init_V(this.customFilterPresetOverlay);
      }

      if (this.macroEditorOverlay != null) {
         manager.init_V(this.macroEditorOverlay);
      }

      if (this.dialogMode) {
         ActionEditorOverlay actionEditor = ActionEditorOverlay.init_ActionEditorOverla_2();
         actionEditor.load_V_Z(inWorld);
         manager.init_V(actionEditor);
      }

      ModuleRuntimeManager autismModule = ModuleRuntimeManager.init_PoolEntry();
      this.keybindOverlay = new AutismKeybindOverlay();
      this.keybindOverlay.b__V();
      manager.init_V(this.keybindOverlay);
      this.launcherOverlay = new AutismLauncherOverlay(
         this.macroListOverlay, null, this.lanSyncOverlay, this.queueEditorOverlay, this.packetLoggerOverlay, this.customFilterOverlay
      );
      this.launcherOverlay.init_V_AutismKeybindOverl(this.keybindOverlay);
      this.launcherOverlay.init_V_Supplier(() -> {
         if (this.packetLoggerOverlay == null && autismModule != null) {
            this.packetLoggerOverlay = autismModule.init_AutismPacketLogger();
            if (this.packetLoggerOverlay != null) {
               this.packetLoggerOverlay.flush_V_13();
            }
         }

         if (this.packetLoggerOverlay != null) {
            manager.init_V(this.packetLoggerOverlay);
         }

         return this.packetLoggerOverlay;
      });
      this.launcherOverlay.load_V_Supplier(() -> {
         if (this.serverInfoOverlay == null) {
            ModuleRuntimeManager mod = ModuleRuntimeManager.init_PoolEntry();
            if (mod != null) {
               this.serverInfoOverlay = mod.init_AutismServerInfoOv();
            }
         }

         if (this.serverInfoOverlay != null) {
            manager.init_V(this.serverInfoOverlay);
         }

         return this.serverInfoOverlay;
      });
      if (this.packetLoggerOverlay == null && AutismPacketLoggerOverlay.poll_Z_13()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.init_PoolEntry();
         this.packetLoggerOverlay = mod == null ? null : mod.init_AutismPacketLogger();
         if (this.packetLoggerOverlay != null) {
            this.packetLoggerOverlay.flush_V_13();
            if (this.packetLoggerOverlay.sync_Z()) {
               manager.init_V(this.packetLoggerOverlay);
            }
         }
      }

      if (this.serverInfoOverlay == null && AutismServerInfoOverlay.poll_Z_16()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.init_PoolEntry();
         this.serverInfoOverlay = mod == null ? null : mod.init_AutismServerInfoOv();
         if (this.serverInfoOverlay != null) {
            this.serverInfoOverlay.update_V_21();
            if (this.serverInfoOverlay.sync_Z()) {
               manager.init_V(this.serverInfoOverlay);
            }
         }
      }

      if (this.dialogMode && !AutismLiteVariant.enabled()) {
         ModuleRuntimeManager mod = ModuleRuntimeManager.init_PoolEntry();
         AutismOverlay profilesOverlay = mod == null ? null : mod.load_cX();
         if (profilesOverlay != null && profilesOverlay.sync_Z()) {
            manager.init_V(profilesOverlay);
         }
      }

      this.launcherOverlay.b__V();
      if (this.dialogMode) {
         this.launcherOverlay.init_V_Z(true);
      }

      manager.init_V(this.launcherOverlay);
   }

   public void onBookRemoved() {
      if ((this.overlaysInitialized || this.isActive()) && !OverlayScreenController.read_Z()) {
         this.overlaysInitialized = false;
         this.persistOverlays();
         OverlayManager.init_bk().load_V();
      }
   }

   private void saveDialogOverlays() {
      if (this.isActive() && !OverlayScreenController.read_Z()) {
         try {
            OverlayManager.init_bk().close();
            this.persistOverlays();
         } finally {
            OverlayManager.init_bk().load_V();
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
         this.launcherOverlay.a__V();
      }

      if (this.packetLoggerOverlay != null) {
         this.packetLoggerOverlay.write_V_18();
      }

      if (this.customFilterOverlay != null) {
         this.customFilterOverlay.a__V();
      }

      if (this.customFilterPresetOverlay != null) {
         this.customFilterPresetOverlay.a__V();
      }

      if (this.keybindOverlay != null) {
         this.keybindOverlay.a__V();
      }

      if (this.serverInfoOverlay != null) {
         this.serverInfoOverlay.write_V_24();
      }
   }

   public boolean mouseClicked(MouseButtonEvent click) {
      return this.isActive() && OverlayManager.init_bk().init_Z_D_2(click.x(), click.y(), Compat263.mouseEventGlfwButton(click));
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      return this.isActive() && OverlayManager.init_bk().load(click.x(), click.y(), Compat263.mouseEventGlfwButton(click));
   }

   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.isActive() && OverlayManager.init_bk().init(click.x(), click.y(), Compat263.mouseEventGlfwButton(click), deltaX, deltaY);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double verticalAmount) {
      return this.isActive() && OverlayManager.init_bk().init_Z_D(mouseX, mouseY, verticalAmount);
   }

   public boolean keyPressed(KeyEvent input) {
      return this.isActive() && OverlayManager.init_bk().init_Z_I(Compat263.keyEventGlfwKey(input), input.keycode(), Compat263.keyEventGlfwModifiers(input));
   }

   public boolean charTyped(CharacterEvent input) {
      return this.isActive() && OverlayManager.init_bk().init_Z_C_I((char)input.codepoint(), 0);
   }
}
